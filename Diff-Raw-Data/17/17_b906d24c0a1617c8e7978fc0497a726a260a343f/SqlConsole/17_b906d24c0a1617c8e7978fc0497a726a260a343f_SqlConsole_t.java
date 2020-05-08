 package sqlengine_a1;
 import java.util.*;
 import sqlengine_a1.parser.*;
 
 public class SqlConsole {
 	private static Database db;
	private static String red, unRed;
 	
 	public static void main(String[] args) {
		String term = System.getenv("TERM");
		if (term != null && term.contains("xterm")) {
			red = (char)0x1b + "[1;31m";
			unRed = (char)0x1b + "[0m";
		}
		else {
			red = unRed = "";
		}
		
 		Scanner stdin = new Scanner(System.in);
 		while (true) {
 			System.out.print("|> ");
 			String line = stdin.nextLine();
 			
 			try {
 				SqlParser sp = new SqlParser(line);
 				ASTNode astRoot = sp.parseStatement();
 				System.out.println(astRoot.toString());
 				
 				if (astRoot.type == ASTNode.Type.QUIT_STATEMENT)
 					return;
 				else if (astRoot.type == ASTNode.Type.EVAL_STATEMENT) {
 					ArbitraryExpression expr = new ArbitraryExpression(astRoot.subnodes.get("expression"));
 					System.out.println(expr.evaluate(null).toString());
 				}
 				else {
 					DbResult result = execStatement(astRoot);
 					if (result == null)
 						System.out.println("No result returned.");
 					else if (result.getReturnedRows() == null)
 						System.out.println(result.getAffectedRowCount() + " rows affected.");
 					else {
 						for (String col : result.getColumnNames()) {
 							System.out.print(col + "  ");
 						}
 						System.out.println();
 						for (Row row : result.getReturnedRows()) {
 							for (String col : result.getColumnNames()) {
 								System.out.print(row.getData(col) + "  ");
 							}
 							System.out.println();
 						}
 					}
 				}
 			}
 			catch (ParseError e) {
				System.err.println(red + "Parse error: " + e.getMessage() + unRed);
 			}
 			catch (SqlException e) {
				System.err.println(red + "Error executing statement: " + e.getMessage() + unRed);
 			}
 		}
 	}
 	
 	public static DbResult execStatement(ASTNode rootNode) throws SqlException {
 		if (db == null
 			&& rootNode.type != ASTNode.Type.CREATE_DATABASE_STATEMENT
 			&& rootNode.type != ASTNode.Type.LOAD_STATEMENT
 			&& rootNode.type != ASTNode.Type.DROP_DATABASE_STATEMENT
 			&& rootNode.type != ASTNode.Type.EVAL_STATEMENT)
 			throw new SqlException("no database loaded");
 		
 		if (rootNode.type == ASTNode.Type.CREATE_TABLE_STATEMENT) {
 			String name = rootNode.sub("tableName").stringValue;
 			List<ColumnDefinition> cols = new ArrayList<ColumnDefinition>();
 			ASTNode thisNode = rootNode.sub("columns");
 			while (true) {
 				ASTNode here = thisNode.type == ASTNode.Type.FIELD_DEF_LIST? thisNode.sub("this") : thisNode;
 				
 				String colName = here.sub("columnName").stringValue;
 				String type = here.sub("type").stringValue;
 				int length = -1;
 				if (here.subnodes.containsKey("length"))
 					length = here.sub("length").sub("length").intValue;
 				boolean nullable = true;
 				if (here.subnodes.containsKey("nullity"))
 					nullable = here.sub("nullity").type == ASTNode.Type.COLUMN_NULLABLE;
 				
 				cols.add(new ColumnDefinition(colName, type, length, nullable));
 				
 				if (thisNode.type == ASTNode.Type.FIELD_DEF_LIST)
 					thisNode = thisNode.sub("next");
 				else
 					break;
 			}
 			
 			db.createTable(name, cols);
 			return null;
 		}
 		else if (rootNode.type == ASTNode.Type.DROP_TABLE_STATEMENT) {
 			String name = rootNode.sub("tableName").stringValue;
 			db.dropTable(name);
 			return null;
 		}
 		else if (rootNode.type == ASTNode.Type.CREATE_DATABASE_STATEMENT) {
 			String name = rootNode.sub("dbName").stringValue;
 			db = Database.create(name);
 			return null;
 		}
 		else if (rootNode.type == ASTNode.Type.LOAD_STATEMENT) {
 			String name = rootNode.sub("dbName").stringValue;
 			db = Database.load(name);
 			return null;
 		}
 		else if (rootNode.type == ASTNode.Type.SAVE_STATEMENT) {
 			db.save();
 			return null;
 		}
 		else if (rootNode.type == ASTNode.Type.DROP_DATABASE_STATEMENT) {
 			String name = rootNode.sub("dbName").stringValue;
 			Database.drop(name);
 			return null;
 		}
 		else if (rootNode.type == ASTNode.Type.SELECT_STATEMENT) {
 			List<String> cols = null;
 			if (rootNode.subnodes.containsKey("columns")) {
 				cols = new ArrayList<String>();
 				ASTNode thisNode = rootNode.sub("columns");
 				while (true) {
 					ASTNode here = thisNode.type == ASTNode.Type.COLUMN_LIST? thisNode.sub("this") : thisNode;
 					String colName = here.sub("columnName").stringValue;
 					cols.add(colName);
 					if (thisNode.type == ASTNode.Type.COLUMN_LIST)
 						thisNode = thisNode.sub("next");
 					else
 						break;
 				}
 			}
 			String tableName = rootNode.sub("tableName").stringValue;
 			ArbitraryExpression where = rootNode.subnodes.containsKey("whereClause")?
 				new ArbitraryExpression(rootNode.sub("whereClause").sub("condition")) : null;
 			Table table = db.getTable(tableName);
 			if (table == null)
 				throw new SqlException("table does not exist");
 			return table.select(cols, where);
 		}
 		else if (rootNode.type == ASTNode.Type.INSERT_STATEMENT) {
 			List<String> cols = null;
 			if (rootNode.subnodes.containsKey("columns")) {
 				cols = new ArrayList<String>();
 				ASTNode thisNode = rootNode.sub("columns").sub("columns");
 				while (true) {
 					ASTNode here = thisNode.type == ASTNode.Type.COLUMN_LIST? thisNode.sub("this") : thisNode;
 					String colName = here.stringValue;
 					cols.add(colName);
 					if (thisNode.type == ASTNode.Type.COLUMN_LIST)
 						thisNode = thisNode.sub("next");
 					else
 						break;
 				}
 			}
 			
 			List<List<Data>> rows = new ArrayList<List<Data>>();
 			ASTNode thisNode = rootNode.sub("rows");
 			while (true) {
 				ASTNode here = thisNode.type == ASTNode.Type.INSERT_ROW_LIST? thisNode.sub("this") : thisNode;
 				
 				List<Data> row = new ArrayList<Data>();
 				ASTNode thisField = here.sub("values");
 				while (true) {
 					ASTNode field = thisField.type == ASTNode.Type.INSERT_ROW? thisField.sub("this") : thisField;
 					row.add(new ArbitraryExpression(field).evaluate(null));
 					if (thisField.type == ASTNode.Type.INSERT_ROW)
 						thisField = thisField.sub("next");
 					else
 						break;
 				}
 				
 				rows.add(row);
 				
 				if (thisNode.type == ASTNode.Type.INSERT_ROW_LIST)
 					thisNode = thisNode.sub("next");
 				else
 					break;
 			}
 			
 			String tableName = rootNode.sub("tableName").stringValue;
 			Table table = db.getTable(tableName);
 			if (table == null)
 				throw new SqlException("table does not exist");
 			return table.insert(cols, rows);
 		}
 		else if (rootNode.type == ASTNode.Type.DELETE_STATEMENT) {
 			String tableName = rootNode.sub("tableName").stringValue;
 			ArbitraryExpression where = rootNode.subnodes.containsKey("whereClause")?
 				new ArbitraryExpression(rootNode.sub("whereClause").sub("condition")) : null;
 			Table table = db.getTable(tableName);
 			if (table == null)
 				throw new SqlException("table does not exist");
 			return table.delete(where);
 		}
 		else if (rootNode.type == ASTNode.Type.UPDATE_STATEMENT) {
 			List<String> cols = new ArrayList<String>();
 			List<ArbitraryExpression> values = new ArrayList<ArbitraryExpression>();
 			ASTNode thisNode = rootNode.sub("updateFields");
 			while (true) {
 				ASTNode here = thisNode.type == ASTNode.Type.UPDATE_FIELD_LIST? thisNode.sub("this") : thisNode;
 				String colName = here.sub("columnName").stringValue;
 				cols.add(colName);
 				ArbitraryExpression value = new ArbitraryExpression(here.sub("value"));
 				values.add(value);
 				if (thisNode.type == ASTNode.Type.UPDATE_FIELD_LIST)
 					thisNode = thisNode.sub("next");
 				else
 					break;
 			}
 			
 			String tableName = rootNode.sub("tableName").stringValue;
 			ArbitraryExpression where = rootNode.subnodes.containsKey("whereClause")?
 				new ArbitraryExpression(rootNode.sub("whereClause").sub("condition")) : null;
 			Table table = db.getTable(tableName);
 			if (table == null)
 				throw new SqlException("table does not exist");
 			return table.update(cols, values, where);
 		}
 		else return null;
 	}
 }
