 package com.nearinfinity.honeycomb.mysql;
 
 import com.google.common.collect.Lists;
 import com.nearinfinity.honeycomb.mysql.gen.ColumnSchema;
 import com.nearinfinity.honeycomb.mysql.gen.ColumnType;
 import com.nearinfinity.honeycomb.mysql.gen.IndexSchema;
 import com.nearinfinity.honeycomb.mysql.gen.TableSchema;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class HandleProxyIntegrationTest {
     private static HandlerProxyFactory factory;
 
     public static void suiteSetup() throws IOException, SAXException, ParserConfigurationException {
         factory = Bootstrap.startup();
     }
 
     public static void testSuccessfulRename() throws Exception {
         final String newTableName = "test2";
         HandlerProxy proxy = factory.createHandlerProxy();
         TableSchema schema = getTableSchema();
 
        proxy.createTable("hbase", "test", "hbase", Util.serializeTableSchema(schema), 0);
         proxy.renameTable(newTableName);
         assert (newTableName.equals(proxy.getTableName()));
         proxy.dropTable();
 
     }
 
     private static TableSchema getTableSchema() {
         HashMap<String, ColumnSchema> columns = new HashMap<String, ColumnSchema>();
         HashMap<String, IndexSchema> indices = new HashMap<String, IndexSchema>();
         columns.put("c1", new ColumnSchema(ColumnType.LONG, true, false, 8, 0, 0));
         columns.put("c2", new ColumnSchema(ColumnType.LONG, true, false, 8, 0, 0));
         indices.put("i1", new IndexSchema(Lists.newArrayList("c1"), false));
 
         return new TableSchema(columns, indices);
     }
 
     public static void main(String[] args) throws Exception {
         try {
             suiteSetup();
             testSuccessfulRename();
         } catch (Exception e) {
             System.out.println(e);
             System.exit(1);
         }
     }
 }
