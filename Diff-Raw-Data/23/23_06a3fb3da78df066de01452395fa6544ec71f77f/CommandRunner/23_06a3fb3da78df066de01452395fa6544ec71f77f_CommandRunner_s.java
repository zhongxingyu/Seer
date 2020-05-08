 package cyberwaste.kuzoff.command;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import cyberwaste.kuzoff.database.DatabaseManager;
 import cyberwaste.kuzoff.database.Row;
 import cyberwaste.kuzoff.database.Table;
 import cyberwaste.kuzoff.database.Type;
 import cyberwaste.kuzoff.database.Value;
 import cyberwaste.kuzoff.outputmanager.OutputManager;
 
 public class CommandRunner {
     
     private final DatabaseManager databaseManager;
     private final OutputManager outputManager;
     
     public CommandRunner(String databaseFolder, OutputManager outputManager) {
         this.outputManager = outputManager;
         this.databaseManager = new DatabaseManager(databaseFolder);
     }
 
     public void runCommand(String commandName, Map<String, String> parameters) {
         if ("lstbl".equals(commandName)) {
             String tableName = getStringParameter(parameters, "name");
             
             if (tableName != null) {
                 showTable(tableName);
             } else {
                 listTables();
             }
         }
         
         else if ("mktbl".equals(commandName)) {
             String tableName = getStringParameter(parameters, "name");
             List<String> columnTypes = getListParameter(parameters, "column");
             
             createTable(tableName, columnTypes);
         }
         
         else if ("rmtbl".equals(commandName)) {
             String tableName = getStringParameter(parameters, "name");
             
             removeTable(tableName);
         }
         
         else if("addrw".equals(commandName)){
             String tableName = getStringParameter(parameters, "name");
             List<String> columnData = getListParameter(parameters,"column");
             
             addRow(tableName, columnData);
         }
         
         else if("rmvrw".equals(commandName)){
             String tableName = getStringParameter(parameters, "name");
            Map<Integer,String> columnData = getMapParameter(parameters, "column");
            
            removeRow(tableName, columnData);
         }
         
         else if("drpdb".equals(commandName)){   
             dropDatabase();
         }
         
         else if("swtbl".equals(commandName)){
             String tableName = getStringParameter(parameters, "name");
             showTableData(tableName);
         }
         else{
             outputManager.outputMessage("UNKNOWN OPERATION");
         }
     }
     
     private void showTableData(String tableName) {
         try {
             Table result = databaseManager.loadTable(tableName);
             outputManager.outputTable(result);
             List<Row> tableData = databaseManager.loadTableData(tableName);
             for(Row curRow : tableData){
                 outputManager.outputRow(curRow);
             }
         } catch (IOException e) {
             outputManager.outputError(e);
         }
     }
 
     private void dropDatabase() {
         try{
             databaseManager.dropDatabase();
             outputManager.outputMessage("DATABASE " + databaseManager.getDatabaseName() + " deleted");
         }catch (IOException e) {
             outputManager.outputError(e);
         }
         
     }
 
    private Map<Integer, String> getMapParameter(Map<String,String> parameters, String key){
         Map<Integer, String> result = new HashMap<Integer, String>();
         
        int index = 1;
         String columnData;
        while ((columnData = getStringParameter(parameters, key + "-" + index)) != null) {
            result.put(index, columnData);
            index++;
         }
         
         return result;
     }
     
     private List<String> getListParameter(Map<String, String> parameters, String key) {
         List<String> result = new ArrayList<String>();
         
         int index = 1;
         String columnType;
         while ((columnType = getStringParameter(parameters, key + "-" + index)) != null) {
             result.add(columnType);
             index++;
         }
         
         return result;
     }
 
     private String getStringParameter(Map<String, String> parameters, String key) {
         return parameters.get(key);
     }
 
     private void listTables() {
         try {
             Collection<Table> result = databaseManager.listTables();
             outputManager.outputListTables(result);
         } catch (IOException e) {
             outputManager.outputError(e);
         }
     }
 
     private void createTable(String tableName, List<String> columnTypes) {
         try {
             Table result = databaseManager.createTable(tableName, columnTypes);
             outputManager.outputTableCreated(result);
         } catch (IOException e) {
             outputManager.outputError(e);
         }
     }
 
     private void removeTable(String tableName) {
         try {
             databaseManager.removeTable(tableName);
             outputManager.outputTableRemoved(tableName);
         } catch (IOException e) {
             outputManager.outputError(e);
         }
     }
 
     private void showTable(String tableName) {
         try {
             Table result = databaseManager.loadTable(tableName);
             outputManager.outputTable(result);
         } catch (IOException e) {
             outputManager.outputError(e);
         }
     }
     
     private void addRow(String tableName, List<String> params){
         try{
             Table table = databaseManager.loadTable(tableName);
             if(table.columnTypes().size() != params.size()){
                 outputManager.outputError(new Exception("Type of row is not valid"));
                 return;
             }
             List<Type> types = table.columnTypes();
             for(int i=0;i<params.size();i++){
                 if(!types.get(i).isValid(new Value(params.get(i)))){
                     outputManager.outputError(new Exception("Type of row is not valid"));
                     return;
                 }
             }
             Row new_row = databaseManager.addRow(tableName,params);
             outputManager.outputRow(new_row);
             
         }catch(IOException e){
             outputManager.outputError(e);
         }
     }
     
     private void removeRow(String tableName, Map<Integer,String> params){
         try{
             Table table = databaseManager.loadTable(tableName);
             List<Type> types = table.columnTypes();
             for(int curKey : params.keySet()){
                 if(!types.get(curKey-1).isValid(new Value(params.get(curKey)))){
                     outputManager.outputError(new Exception("Type of row is not valid"));
                     return;
                 }
             }
             
             List<Row> rowList = databaseManager.removeRow(tableName,params);
             outputManager.outputMessage("Deleted Rows:");
             for(Row row : rowList) outputManager.outputRow(row);
         }catch(IOException e){
             outputManager.outputError(e);
         }
     }
 }
