 package db_sample;
 
 import tuple.ICompletionProvider;
 
 import java.util.ArrayList;
 import java.util.List;
 
/**
 * Created by IntelliJ IDEA.
 * User: ttim
 * Date: 4/28/11
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
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
 
    public List<String> getCompletionList(Class genericClass) {
         try {
            TableScheme scheme = (TableScheme) genericClass.newInstance();
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
