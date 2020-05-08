 package db_sample;
 
 import tuple.ICompletionProvider;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DbSqlSelector<T extends TableScheme> implements ICompletionProvider {
     private String selectFrom, selectWhat;
 
     public DbSqlSelector() {
     }
 
     public DbSqlSelector(String selectFrom) {
         this.selectFrom = selectFrom;
     }
 
     public DbSqlResult select(String what) {
         this.selectWhat = what;
         return new DbSqlResult();
     }
 
    public List<String> getCompletionList(TableScheme scheme) {
         try {
             List<String> result = new ArrayList<String>();
 
             for (String field : scheme.getFieldNames()) {
                 result.add("select(\"" + field + "\")");
             }
 
             return result;
         } catch (Exception e) {
             e.printStackTrace();
             return new ArrayList<String>();
         }
 
     }
 
     public class DbSqlResult {
         public String getSql() {
             return "select " + selectWhat + " from " + selectFrom;
         }
     }
 }
