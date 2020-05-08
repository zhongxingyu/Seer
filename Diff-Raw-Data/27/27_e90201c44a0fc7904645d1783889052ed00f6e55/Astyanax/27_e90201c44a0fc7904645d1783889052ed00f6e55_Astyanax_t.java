 package smail.cli.astyanax;
 
 import com.netflix.astyanax.Keyspace;
 import com.netflix.astyanax.MutationBatch;
 import com.netflix.astyanax.connectionpool.OperationResult;
 import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
 import com.netflix.astyanax.model.Column;
 import com.netflix.astyanax.model.ColumnFamily;
 import com.netflix.astyanax.model.ColumnList;
 import com.netflix.astyanax.model.ConsistencyLevel;
 import com.netflix.astyanax.model.CqlResult;
 import com.netflix.astyanax.model.Row;
 import com.netflix.astyanax.serializers.BytesArraySerializer;
 import com.netflix.astyanax.serializers.IntegerSerializer;
 import com.netflix.astyanax.serializers.StringSerializer;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import smail.cli.util.DateUtil;
 import smail.cli.util.EmailCql;
 import smail.cli.util.EmailFormatter;
 
 // @author lyubentodorov
 // @licence - MIT
 // Available at http://lyuben.herokuapp.com/casstor/ 
 // Source at https://github.com/lyubent/CassTor/ 
 //
 public class Astyanax {
     
     private static final String __KEYSPACE__ = "MAIL_KS";//"KeyspaceName";
     private static final String __COLUMNFAMILY__ = "MESSAGE_CF";//"KeyspaceName";
     private static final String __CLUSTER__ = "Test Cluster";
     private static final String __SEEDS__ = "134.36.36.188"; //"94.168.172.237:9160, 127.0.0.1:9160";
     
     // Executes a CQL3 statement creatin a table with composite key
     // FAILS - Astyanax can not execute cql3 
     //
     @Deprecated
     public static boolean createCQL3Table(Keyspace keyspace, String cqlStatement){
         try {
             ColumnFamily<Integer, String> mail = new ColumnFamily<>(
             "Mail", // CF Name
             IntegerSerializer.get(),   // Key Serializer
             StringSerializer.get());  // Column Serializer
             
             OperationResult<CqlResult<Integer, String>> result;
                     result = keyspace.prepareQuery(mail)
             .withCql(//"CREATE TABLE employees (empID int, deptID int, first_name varchar, last_name varchar, PRIMARY KEY (deptID));")
                        "CREATE TABLE employees (empID int, deptID text, first_name text, last_name text, PRIMARY KEY (empID, deptID));")
             .execute();
         } catch (ConnectionException ex) { 
             Logger.getLogger(Astyanax.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
         
         return true;
     }
     
     
     
     // Executes CQL3 statements and returns the results
     // @return OperationResult<CqlResult<String, String>>
     //
     public static OperationResult<CqlResult<String, String>> execCQL(Keyspace keyspace, String cqlStatement){
         try {
             
             ColumnFamily<String, String> mail = new ColumnFamily<>(
             __COLUMNFAMILY__, // CF Name
             StringSerializer.get(),   // Key Serializer
             StringSerializer.get());  // Column Serializer
             
             OperationResult<CqlResult<String, String>> result = 
                     keyspace.prepareQuery(mail).withCql(cqlStatement).execute();
             
             return result;
                
         } catch (ConnectionException ex) { 
             Logger.getLogger(Astyanax.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return null;
     }
     
     
     
     // Processes the CQL3 result object displaying it's fiels
     // @param requires a CqlResult object
     //
     public static List<String> processResults(OperationResult<CqlResult<String, String>> result){
         
         List messageList = new LinkedList();
         
         for (Row <String, String> row : result.getResult().getRows()) {
             
             String key = "", body = "", subject = "", from = "", date = "";
             for (Column<String> column : row.getColumns()){
                 if(column.getName().equals("KEY")) {
                     key = column.getValue(new StringSerializer());
                 }
                 if(column.getName().equals("BODY")) {
                     body = column.getValue(new StringSerializer());
                 }
                 if(column.getName().equals("SUBJECT")) {
                     subject = column.getValue(new StringSerializer());
                 }
                 if(column.getName().equals("SENDER")) {
                     from = column.getValue(new StringSerializer());
                 }
                 if(column.getName().equals("DATE")) {
                     date = column.getValue(new StringSerializer());
                 }
             }
             
             messageList.add(EmailFormatter.structureMessage(key, body, from, date, subject));
         }
         
         return messageList;
     }
     
     
     
     // Inserts a dynamic range of colums specified bellow
     // @param Requires a keyspace context, the user's username and a list of input 
     //
     public static String insertEmail(Keyspace keyspace, String user, List<String> uinput){
         
         String __KEY__ = String.valueOf(user + "@" + DateUtil.getUnixTimestamp());
         ColumnFamily<String, String> mail = new ColumnFamily<>( __COLUMNFAMILY__, // CF Name
             StringSerializer.get(), StringSerializer.get());
         MutationBatch mbatch = keyspace.prepareMutationBatch();
 
         mbatch.withRow(mail, __KEY__)
             .putColumn("RECEIVER", uinput.get(0), null)
             .putColumn("SENDER", user, null)
             .putColumn("SUBJECT", uinput.get(1), null)
             .putColumn("BODY", uinput.get(2), null)
             .putColumn("DELETED_SENDER", "No", null)
             .putColumn("DELETED_RECEIVER", "No", null)
             .putColumn("DATE", DateUtil.getCurrentDate(), null);
         
         try { 
             OperationResult<Void> result = mbatch.execute();
             result.toString();
         }
         catch (ConnectionException ex) { 
             Logger.getLogger(Astyanax.class.getName()).log(Level.SEVERE, null, ex);
             return "";
         }
         
         return __KEY__;
     }
     
     
     
     // Inserts a dynamic range of colums specified bellow
     // @param Requires a keyspace context, the user's username and a list of input 
     //
     /*************************************
      * METHOD NOT YET FULLY IMPLEMENTED! *
      *************************************/
     public static boolean insertEmailAsBytes(Keyspace keyspace, String user, List<String> uinput){
         
         ColumnFamily<byte [], byte []> mail = new ColumnFamily<>( __COLUMNFAMILY__, // CF Name
             BytesArraySerializer.get(), BytesArraySerializer.get());
 
         MutationBatch mbatch = keyspace.prepareMutationBatch();
 
         String wiki = "You can use the cqlsh commands described in this section to create a keyspace. In creating an example keyspace for Twissandra, we will assume a desired replication factor of 3 and implementation of the NetworkTopologyStrategy replica placement strategy. For more information on these keyspace options, see About Replication in Cassandra.";
         String wiki2 = "Installing Thrift - All CQL drivers require Thrift 0.6. If you run CQL commands on a Cassandra 0.8 node, the required Thrift jar files are already present in the environment. However, to run CQL commands from a remote machine, you must install Thrift 0.6 as described in the Cassandra Wiki.";
         
 //        mbatch.withRow(mail, "")//String.valueOf(user + "@" + DateUtil.getUnixTimestamp()))
 //            .putColumn("RECEIVER", uinput.get(0), null)
 //            .putColumn("SENDER", user, null)
 //            .putColumn("SUBJECT", uinput.get(1), null)
 //            .putColumn("BODY", uinput.get(2), null)
 //            .putColumn("DELETED_SENDER", "No", null)
 //            .putColumn("DELETED_RECEIVER", "No", null)
 //            .putColumn("DATE", DateUtil.getCurrentDate(), null);
 //        
         try { 
             OperationResult<Void> result = mbatch.execute();
             result.toString();
         }
         catch (ConnectionException ex) { 
             Logger.getLogger(Astyanax.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
         
         return true;
     }
     
     
     
     // Deletes an email by Key
     // @param Keyspace object and UID Key of the email
     //
     public static boolean deleteEmail(Keyspace keyspace, String __KEY__){
         
        if(Astyanax.execCQL(keyspace, EmailCql.hardDeleteEmail(__KEY__)) == null) {
             return false;
         }
         
         return true;
     }
     
     
     
     // Originally used for reading emails
     // Superseeded by execCql using the get email strings from EmailCql.java
     //
     @Deprecated
     public static boolean readEmail(Keyspace keyspace){
             
         ColumnFamily<String, String> mail = new ColumnFamily<>(
                  "Mail", // CF Name
                  StringSerializer.get(),   // Key Serializer
                  StringSerializer.get());  // Column Serializer 
 
         try {
 
             OperationResult<ColumnList<String>> result = 
                     keyspace.prepareQuery(mail).getKey("lyubent").execute();
             ColumnList<String> columns = result.getResult();
 
             // Lookup columns in response by name
             //int age = columns.getColumnByName("age").getIntegerValue();
             //long counter = columns.getColumnByName("loginCount").getLongValue();
             String address = columns.getColumnByName("Body").getStringValue();
 
             // Or, iterate through the columns
             for (Column<String> c : result.getResult()) {
                 System.out.println("Name: " + c.getName() + " Val:" + c.getValue(new StringSerializer()));
             }
 
         } catch (ConnectionException ex){ 
             Logger.getLogger(Astyanax.class.getName()).log(Level.SEVERE, null, ex); 
         }
             
         return true;
     }
     
     
     
     // Builds an Astyanax context
     // @return The keyspace within the context
     //
     public static Keyspace getKeyspaceContext(){
         
         //Normal KS
         com.netflix.astyanax.AstyanaxContext<Keyspace> context = 
                 new com.netflix.astyanax.AstyanaxContext.Builder()
         .forCluster(__CLUSTER__)
         .forKeyspace(__KEYSPACE__) //NetworkKS
         .withAstyanaxConfiguration(
          new com.netflix.astyanax.impl.AstyanaxConfigurationImpl()      
        .setDefaultReadConsistencyLevel(ConsistencyLevel.CL_ALL) // Data should be consistent
        .setDefaultWriteConsistencyLevel(ConsistencyLevel.CL_ALL)
         .setDiscoveryType(com.netflix.astyanax.connectionpool.NodeDiscoveryType.NONE) // NONE FOR BASIK KS
         .setCqlVersion("3.0.0")) //using CQL3 (fails, its still CQL2)
         .withConnectionPoolConfiguration(
          new com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl("MyConnectionPool")
         .setPort(9160)
         .setMaxConnsPerHost(10)
         .setSeeds(__SEEDS__))
         .withConnectionPoolMonitor(
          new com.netflix.astyanax.connectionpool.impl.Slf4jConnectionPoolMonitorImpl())
         .buildKeyspace(com.netflix.astyanax.thrift.ThriftFamilyFactory.getInstance());
         
         context.start();
         
         return context.getEntity();
     }
     
     
     
     // Builds the CF structure for the MESSAGE column family
     // @return ColumnFamily<String, String> 
     //
     public static ColumnFamily<String, String> getColumnFamilyStructure(String colFamilyName){
         return ColumnFamily.newColumnFamily(colFamilyName, //default should be message 
                 StringSerializer.get(), StringSerializer.get());
     }
     
     
     
     // Builds the CF structure for the MESSAGE column family
     // @return ColumnFamily<String, String> 
     //
     public static ColumnFamily<String, String> getColumnFamilyStructure(){
         return ColumnFamily.newColumnFamily(__COLUMNFAMILY__, //default should be message 
                 StringSerializer.get(), StringSerializer.get());
     }
 }
