 package dbCommands;
 
 import dbEnvironment.DbContext;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nikita_kartashov
  * Date: 23/10/2013
  * Time: 17:36
  * To change this template use File | Settings | File Templates.
  */
 public class InsertRowsCommand implements DbCommand
 {
     public InsertRowsCommand(String tableName, TableRow[] rows)
     {
        _tableName = tableName;
         _rows = rows;
     }
 
     public void executeCommand(DbContext context)
     {
     }
 
     public String _tableName;
     public TableRow[] _rows;
 }
