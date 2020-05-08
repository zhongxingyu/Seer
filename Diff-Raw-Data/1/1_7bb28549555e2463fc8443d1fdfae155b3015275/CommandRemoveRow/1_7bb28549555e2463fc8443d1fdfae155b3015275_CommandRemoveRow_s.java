 package cyberwaste.kuzoff.io.command;
 
 import java.util.Map;
 import java.util.List;
import java.util.Map;
 
 import cyberwaste.kuzoff.core.DatabaseManager;
 import cyberwaste.kuzoff.core.domain.Row;
 import cyberwaste.kuzoff.io.IOManager;
 
 public class CommandRemoveRow implements Command {
 
     private Map<String,String> parameters;
     private DatabaseManager databaseManager;
     
     public void setState(Map<String,String> parameters, DatabaseManager databaseManager){
         this.databaseManager = databaseManager;
         this.parameters = parameters;
     }
     
     @Override
     public void execute(IOManager ioManager) throws Exception {
         final String tableName = CommandBuilder.getStringParameter(parameters, "name");
         int numColumns = databaseManager.loadTable(tableName).columnTypes().size();
         final Map<Integer,String> columnData = CommandBuilder.getMapParameter(parameters, "column", numColumns);
         List<Row> rowList = databaseManager.removeRow(tableName,columnData);
         ioManager.outputRowDeleted(rowList);
     }
 
 }
