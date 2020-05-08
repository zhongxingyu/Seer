 package cyberwaste.kuzoff.core;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import cyberwaste.kuzoff.core.domain.Row;
 import cyberwaste.kuzoff.core.domain.Table;
 import cyberwaste.kuzoff.core.domain.Type;
 import cyberwaste.kuzoff.core.impl.DatabaseManagerImpl;
 
 @Ignore
 public class DatabaseManagerTest {
     
     private DatabaseManagerImpl manager;
     
     @Before
     public void setUp() throws Exception {
         manager = new DatabaseManagerImpl();
         manager.forDatabaseFolder("/home/vlg/Documents/db_test");
         List<String> types = new ArrayList<String>();
         types.add("int"); types.add("char");
         manager.createTable("table1",types);
         List<String> row = new ArrayList<String>();
         row.add("10"); row.add("a");
         manager.addRow("table1",row);
         row = new ArrayList<String>();
         row.add("20"); row.add("b");
         manager.addRow("table1", row);
         
     }
 
     @After
     public void tearDown() throws Exception {
         manager.dropDatabase();
     }
 
     @Test
     public void listTablesTest() throws IOException {
         String[] typeNames = {"int", "char"}; 
         Collection<Table> tables = manager.listTables();
         assertEquals(1, tables.size());
         Iterator<Table> it = tables.iterator();
         Table table = it.next();
         assertEquals(table.name(), "table1");
         List<Type> types = table.columnTypes();
         for(int i=0;i<types.size();i++){
             assertEquals(types.get(i).name(),typeNames[i]);
         }
     }
     
     @Test
     public void loadTableDataTest() throws IOException{
         List<Row> rows = manager.loadTableData("table1");
         assertEquals(2,rows.size());
         for(Row row : rows) assertEquals(2,row.length());
         String[][] rowDataActual = {{"10","a"},{"20","b"}};
         for(int i=0; i<rows.size();i++){
             Row row = rows.get(i);
             for(int j=0;j<row.length();j++){
                 assertEquals(row.getElement(j).getValue(), rowDataActual[i][j]);
             }
         }
     }
     
     @Test
    public void removeRowTest() throws Exception {
         Map<Integer,String> columnData = new HashMap<Integer, String>();
         columnData.put(2, "a");
         manager.removeRow("table1", columnData);
         List<Row> rows = manager.loadTableData("table1");
         assertEquals(1,rows.size());
         Row row = rows.get(0);
         String[] rowDataActual = {"20","b"};
         for(int i=0;i<row.length();i++){
             assertEquals(row.getElement(i).getValue(), rowDataActual[i]);
         }
     }
 
 }
