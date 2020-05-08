 package com.github.mjvesa.f4v.wordset;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
import java.util.ArrayList;
 
 import com.github.mjvesa.f4v.BaseWord;
 import com.github.mjvesa.f4v.CompiledWord;
 import com.github.mjvesa.f4v.DefinedWord;
 import com.github.mjvesa.f4v.Interpreter;
 import com.github.mjvesa.f4v.Util;
 import com.github.mjvesa.f4v.Word;
 import com.vaadin.data.Item;
 import com.vaadin.data.util.IndexedContainer;
 
 public class SQLWordSet extends WordSet {
 
 	@Override
 	public Word[] getWords() {
 		return new Word[] {
 
 		new BaseWord("create-SQL-container", "", Word.POSTPONED) {
 			@Override
 			public void execute(Interpreter interpreter) {
 
 				String str = (String) interpreter.popData();
 				interpreter.pushData(createIndexedContainerFromQuery(interpreter, str, false));
 
 			}
 		},
 
 		new BaseWord("create-filtered-SQL-container", "", Word.POSTPONED) {
 			@Override
 			public void execute(Interpreter interpreter) {
 				String str = (String) interpreter.popData();
 				interpreter.pushData(createIndexedContainerFromQuery(interpreter, str, true));
 			}
 		},
 
 		new BaseWord("do-query", "", Word.POSTPONED) {
 			@Override
 			public void execute(Interpreter interpreter) {
 				doQuery(interpreter, (String) interpreter.popData());
 			}
 		},
 
 		new BaseWord("get-property", "", Word.POSTPONED) {
 			@Override
 			public void execute(Interpreter interpreter) {
 				String str = (String) interpreter.popData();
 				Item item = (Item) interpreter.popData();
 				interpreter.pushData(item);
 				interpreter.pushData(item.getItemProperty(str).getValue());
 			}
 		},
 
 		new BaseWord("set-property", "", Word.POSTPONED) {
 			@Override
 			public void execute(Interpreter interpreter) {
 				String value = (String) interpreter.popData();
 				String property = (String) interpreter.popData();
 				Item item = (Item) interpreter.popData();
 				item.getItemProperty(property).setValue(value);
 			}
 		}
 
 		};
 	}
 
 	
 	 /**
 	 * Creates an indexed container by executing the supplied query.
 	 * @param interpreter 
 	 * 
 	 * @param query
 	 *            SQL query to execute
 	 * @param filtered
 	 *            Tells us if this query is a filtered one, which means it has
 	 *            parameters. if true, parameters are loaded from stack,
 	 *            otherwise the query is executed without parameters.
 	 * 
 	 * @return
 	 */
 	public Object createIndexedContainerFromQuery(Interpreter interpreter, String query, boolean filtered) {
 		IndexedContainer container = new IndexedContainer();
 		try {
 			Connection conn = getConnection();
 
 			PreparedStatement st = conn.prepareStatement(query);
 			if (filtered) {
 				applyParameterListToPreparedStatement(interpreter, st);
 			}
 			ResultSet rs = st.executeQuery();
 
 			// Initialize container
 			ResultSetMetaData meta = rs.getMetaData();
 			for (int i = 1; i <= meta.getColumnCount(); i++) {
 				String name = meta.getColumnName(i);
 				Class clazz = Object.class;
 
 				switch (meta.getColumnType(i)) {
 				case java.sql.Types.VARCHAR:
 				case java.sql.Types.LONGVARCHAR:
 					clazz = String.class;
 					break;
 				case java.sql.Types.INTEGER:
 					clazz = Integer.class;
 					break;
 				}
 				container.addContainerProperty(name, clazz, null);
 			}
 			// Go trough all the nasty (what?)
 			while (rs.next()) {
 				Object id = container.addItem();
 				Item item = container.getItem(id);
 				for (int i = 1; i <= meta.getColumnCount(); i++) {
 					String name = meta.getColumnName(i);
 
 					switch (meta.getColumnType(i)) {
 					case java.sql.Types.VARCHAR:
 					case java.sql.Types.LONGVARCHAR:
 						String s = rs.getString(i);
 						item.getItemProperty(name).setValue(s);
 						break;
 					case java.sql.Types.INTEGER:
 						Integer value = rs.getInt(i);
 						item.getItemProperty(name).setValue(value);
 						break;
 					}
 				}
 			}
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return container;
 	}
 	
 	/**
 	 * Applies a list of parameters to a prepared statement. The parameters are
 	 * on the stack as a list.
 	 * @param interpreter 
 	 * 
 	 * @param st
 	 * @throws SQLException
 	 */
 	private void applyParameterListToPreparedStatement(Interpreter interpreter, PreparedStatement st)
 			throws SQLException {
 
 		Word[] code = ((DefinedWord) interpreter.getDictionary().get("list[")).getCode();
 		int addr = (Integer) ((CompiledWord) interpreter.getCode()[0]).getParameter();
 		int i = 1;
 		while (interpreter.getHeap()[addr] != Util.LIST_TERMINATOR) {
 			st.setObject(i, interpreter.getHeap()[addr]);
 			addr++;
 			i++;
 		}
 	}
 
 	/**
 	 * Creates a jdbc connection.
 	 * 
 	 * @return A JDBC Connection that's ready for action.
 	 * @throws SQLException
 	 */
 	private Connection getConnection() throws SQLException {
 		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:forth",
 				"forth", "forth");
 		return conn;
 	}
 
 
 
 	/*
 	 * Can be used to do updates and inserts.
 	 */
 	public void doQuery(Interpreter interpreter, String sql) {
 
 		try {
 			Connection conn = getConnection();
 			PreparedStatement st = conn.prepareStatement(sql);
 			applyParameterListToPreparedStatement(interpreter, st);
 			st.execute();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 
 }
