 package fatworm.util;
 
 import java.util.List;
 import java.util.Set;
 
 import fatworm.planner.QueryPlanner;
 import fatworm.query.ConditionJoinPlan;
 import fatworm.query.DistinctContainer;
 import fatworm.query.Env;
 import fatworm.query.GroupContainer;
 import fatworm.query.JoinPlan;
 import fatworm.query.MemoryDistinctContainer;
 import fatworm.query.MemoryGroupContainer;
 import fatworm.query.MemoryOrderContainer;
 import fatworm.query.OrderContainer;
 import fatworm.query.QueryPlan;
 import fatworm.query.AdvancedOrderContainer;
 import fatworm.query.Scan;
 import fatworm.query.SimpleEnv;
 import fatworm.record.RecordFile;
 import fatworm.record.Schema;
 import fatworm.storagemanager.StorageManagerInterface;
 import fatworm.absyn.OrderByColumn;
 import fatworm.database.*;
 
 public class Util
 {
     public static QueryPlanner getQueryPlanner() {
         return Database.getInstance().getQueryPlanner();
     }
 
 	public static RecordFile getTable(String name) {
 		return Database.getInstance().getStorageManager().getTable(name);
 	}
 
 	public static boolean isFieldSuffix(String column) {
 		for (int i = 0; i < column.length(); i++) {
 			if (!legalCharInColName(column.charAt(i)))
 				return false;
 		}
 		//return (!column.contains("(") && column.contains("."));
 		return column.contains(".");
 	}
 
 	static boolean legalCharInColName(char ch) {
 		if (ch == '.' || (ch >='A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >='0' && ch <='9') || (ch == '_'))
 			return true;
 		return false;
 	}
 	public static Object getColumnTableName(String column) {
 		return column.substring(0, column.indexOf("."));
 	}
 
 	public static boolean isSimpleColumn(String column) {
 		return (!column.contains("(") && !column.contains("."));	
 	}
 
 	public static String getColumnFieldName(String column) {
 		return column.substring(column.indexOf(".")+1);	
 	}
 
 	public static Env getEmptyEnv() {
 		return new SimpleEnv();
 	}
 
 	public static boolean isFunction(String column) {
 		return column.startsWith("AVG(") 
 				|| column.startsWith("COUNT(") 
 				|| column.startsWith("MAX(")
 				|| column.startsWith("MIN(") 
 				|| column.startsWith("SUM(");
 	}
 
 	public static String makeColumnName(String table, String field) {
 		return table + "." + field;
 	}
 
 	public static boolean hasField(String string, String fldname) {
 		if (Util.isFieldSuffix(string)) {
 			return Util.getColumnFieldName(string).equals(fldname);
         }
         if (Util.isSimpleColumn(string)) {
         	return string.equals(fldname);
         }
 		return false;
 	}
 
 	public static String getFuncVariable(String s) {
 		return s.substring(s.indexOf('(')+1, s.indexOf(')'));
 	}
 	
 	public static String getFuncName(String s) {
 		return s.substring(0, s.indexOf("("));
 	}
 
 	public static void dropTable(String name) {
 		Database.getInstance().getStorageManager().dropTable(name);			
 	}
 	
 	public static void createTable(String name, Schema schema) {
 		Database.getInstance().getStorageManager().insertTable(name, schema);
 	}
 
 	public static StorageManagerInterface getStorageManager() {
 		return Database.getInstance().getStorageManager();
 	}
 
 	public static GroupContainer getGroupContainer(String keyName,
 			Set<String> funcSet) {
 		return new MemoryGroupContainer(keyName, funcSet);
 	}
 
 	public static OrderContainer getOrderContainer(Scan scan,
 			List<OrderByColumn> order) {
 		return new AdvancedOrderContainer(scan, order);
 	}
 
 	public static DistinctContainer getDistinctContainer() {
 		return new MemoryDistinctContainer();
 	}
 	
 	public static JoinPlan getJoinPlan(List<QueryPlan> planList) {
 	    return new ConditionJoinPlan(planList);
 	}
 
     public static void createTotalIndex(String name) {
         RecordFile rf = Util.getTable(name);
         Schema schema = rf.getSchema();
         for (int i = 0; i < schema.columnCount(); i++) {
            rf.createIndex(schema.name(i));
         }
     }
 
 }
