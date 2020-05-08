 package db.util;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PreparedStatementExecutionItem extends AExecutionItem {		
 	private String query = null;
 	private ISetter[] params = null;
 	private ResultSet resultSet = null;
 	private boolean wasExecuted = false;
 	private List<PreparedStatementExecutionItem> executionItems = new ArrayList<PreparedStatementExecutionItem>();
 	
 	public PreparedStatementExecutionItem(String query, ISetter[] params) {
 		this.query = query;
 		this.params = params;
 	}
 	
 	public void execute(Connection conn) {
 		try {
 			PreparedStatement s = conn.prepareStatement(query);
 			if (params != null) {
 				for (ISetter setter : params) {
 					s = setter.set(s);
 				}
 			}
 			for (PreparedStatementExecutionItem ei : executionItems)
 				s = ei.addToBatch(s);
 			if (query.toLowerCase().startsWith("select")) {
 				resultSet = s.executeQuery();
 			} else {
 				if (executionItems.isEmpty())
 					s.execute();
 				else
 					s.executeBatch();
				s.close();
 			}
 		}
 		catch (SQLException e) {
 			System.err.println("===> Batch start");
 			this.print();
 			for (PreparedStatementExecutionItem ei : this.executionItems)
 				ei.print();
 			System.err.println("===> Batch end");
 			e.printStackTrace();
 		}
 		for (PreparedStatementExecutionItem ei : executionItems)
 			ei.wasExecuted = true;
 		wasExecuted = true;
 	}
 	
 	public boolean wasExecuted() {
 		return wasExecuted;
 	}
 	
 	public ResultSet getResult() {
 		return this.resultSet;
 	}
 
 	@Override
 	public boolean combine(AExecutionItem itemToAdd) {
 		if (itemToAdd != null && itemToAdd.getClass() == PreparedStatementExecutionItem.class) {
 			if (this.query.toUpperCase().startsWith("INSERT") && !this.query.toUpperCase().contains(";SELECT")) {
 				PreparedStatementExecutionItem otherItem = (PreparedStatementExecutionItem) itemToAdd;
 				if (otherItem.query.toUpperCase().startsWith("INSERT") && !otherItem.query.toUpperCase().contains(";SELECT") && otherItem.query.toLowerCase().equals(query.toLowerCase())) {
 					this.executionItems.add(otherItem);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void print() {
 		System.err.println(this.query);
 		for (ISetter s : this.params) s.print();
 	}
 	
 	private PreparedStatement addToBatch(PreparedStatement statement) throws SQLException {
 		statement.addBatch();
 		if (params != null) {
 			for (ISetter s : params) {
 				s.set(statement);
 			}
 		}
 		return statement;
 	}
 }
