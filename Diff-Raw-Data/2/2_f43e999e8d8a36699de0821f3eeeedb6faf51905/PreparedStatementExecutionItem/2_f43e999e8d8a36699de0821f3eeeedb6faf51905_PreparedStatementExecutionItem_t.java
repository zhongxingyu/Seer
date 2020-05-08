 package db.util;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import db.util.ISetter;
 
 public class PreparedStatementExecutionItem extends AExecutionItem {		
 	private String query = null;
 	private List<ISetter[]> params = new ArrayList<ISetter[]>();
 	private ResultSet resultSet = null;
 	private boolean wasExecuted = false;
 	
 	public PreparedStatementExecutionItem(String query, ISetter[] params) {
 		this.query = query;
 		this.params.add(params);
 	}
 	
 	public void execute(Connection conn) {
 		try {
 			Iterator<ISetter[]> it = this.params.iterator();
 			PreparedStatement s = conn.prepareStatement(query);
 			while (it.hasNext()) {
 				ISetter[] params = it.next();
 				if (params != null) {
 					for (ISetter setter : params) {
 						s = setter.set(s);
 					}
 				}
 				if (it.hasNext()) {
 					s.addBatch();
 				}
 			}
 			if (query.toLowerCase().startsWith("select")) {
 				resultSet = s.executeQuery();
 			} else {
 				s.execute();
 			}
 		}
 		catch (SQLException e) {
 			for (ISetter[] params : this.params) {
 				PreparedStatement s = null;
 				try {
 					s = conn.prepareCall(query);
 					if (params != null) {
 						for (ISetter setter : params) {
 							s = setter.set(s);
 						}
 					}
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 				System.err.println(s.toString());
 			}
 			e.printStackTrace();
 		}
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
 				if (otherItem.query.matches(this.query)) {
 					this.params.addAll(otherItem.params);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
