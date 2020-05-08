 package org.jmxdatamart.Extractor;
 
 import org.jmxdatamart.JMXTestServer.TestBean;
 import org.jmxdatamart.common.*;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import java.lang.management.ManagementFactory;
 import java.sql.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 /**
  * Created with IntelliJ IDEA.
  * User: Xiao Han
  * To change this template use File | Settings | File Templates.
  */
 public class Bean2DB {
 
     public static void main(String[] args) throws Exception {
         // TODO code application logic here
         int expected = 42;
         //Create new test MBean
         TestBean tb = new TestBean();
         tb.setA(new Integer(expected));
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         String mbName = "org.jmxdatamart.JMXTestServer:type=TestBean";
         ObjectName mbeanName = new ObjectName(mbName);
         mbs.registerMBean(tb, mbeanName);
 
         //Create test MBean's MBeanData
         Attribute a = new Attribute("A", "Alpha", DataType.INT);
 
         MBeanData mbd = new MBeanData(mbName, "testMBean", Collections.singletonList(a), true);
 
         //Init MBeanExtract
         MBeanExtract instance = new MBeanExtract(mbd, mbs);
         Map result = instance.extract();
 
         Settings s = new Settings();
         s.setBeans(Collections.singletonList((BeanData)mbd));
         s.setFolderLocation("HyperSQL/");
         s.setPollingRate(2);
         s.setUrl("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
         Properties props = new Properties();
         props.put("username", "sa");
         props.put("password", "whatever");
 
         Bean2DB bd = new Bean2DB();
         String dbname = bd.generateMBeanDB(s);
         HypersqlHandler hsql = new HypersqlHandler();
         Connection conn= hsql.connectDatabase(dbname, props);
 
         bd.export2DB(conn,mbd,result);
 
         ResultSet rs = conn.createStatement().executeQuery("select * from org_jmxdatamart_JMXTestServer__type___TestBean");
         while(rs.next()){
             System.out.println(rs.getObject(1) + "\t" + rs.getObject(2));
         }
         hsql.shutdownDatabase(conn);
         DBHandler.disconnectDatabase(rs, null, null, conn);
     }
 
     //get rid of the . : =, which are illegal for a table name
     public  String convertIllegalTableName(String tablename){
         return tablename.replaceAll("\\.","_").replaceAll(":","__").replaceAll("=","___");
     }
     public  String recoverOriginalTableName(String tablename){
         return tablename.replaceAll("___","=").replaceAll("__",":").replaceAll("_","\\.");
     }
 
     private void dealWithDynamicBean(Connection conn, String tableName , Map<Attribute, Object> result) throws  SQLException,DBException{
         if (!DBHandler.tableExists(tableName,conn)){
             throw new DBException("Something wrong in the Dynamic Bean: bean table doesn't exists!");
         }
         String sql;
         boolean bl = conn.getAutoCommit();
         conn.setAutoCommit(false);
         for (Map.Entry<Attribute, Object> m : result.entrySet()) {
             if (!DBHandler.columnExists(m.getKey().getName(),tableName,conn)){
                 sql = "Alter table " + tableName +" add " +m.getKey().getName()+ " "+m.getKey().getDataType();
                 conn.createStatement().executeUpdate(sql);
             }
         }
         conn.commit();
         conn.setAutoCommit(bl);
     }
 
    public void export2DB(Connection conn, MBeanData mbd, Map<Attribute, Object> result) throws  SQLException,DBException{
 
         String tablename = convertIllegalTableName(mbd.getName());
         //deal with dynamic bean
         dealWithDynamicBean(conn,tablename,result);
 
         PreparedStatement ps = null;
 
         StringBuilder insertstring = new StringBuilder() ;
         insertstring.append("insert into ").append(tablename).append(" (");
         StringBuilder insertvalue = new StringBuilder();
         insertvalue.append(" values(");
 
         for (Map.Entry<Attribute, Object> m : result.entrySet()) {
             insertstring.append(((Attribute)m.getKey()).getName()).append(",");
             insertvalue.append("?,");
         }
 
         String sql = insertstring.append("time)").toString();
         sql += insertvalue.append("?)").toString();
         //System.out.println(sql);
         ps = conn.prepareStatement(sql);
 
         //need to think about how to avoid retrieving the map twice
         int i=0;
         for (Map.Entry<Attribute, Object> m : result.entrySet()) {
             switch (((Attribute)m.getKey()).getDataType())
             {
                 case INT:
                     ps.setInt(++i,(Integer)m.getValue());
                     break;
                 case STRING:
                     ps.setString(++i,m.getValue().toString());
                     break;
                 case FLOAT:
                     ps.setFloat(++i,(Float)m.getValue());
                     break;
             }
         }
         ps.setTimestamp(++i, new Timestamp((new java.util.Date()).getTime()));
 
         boolean bl=false;
         try{
             bl = conn.getAutoCommit();
             conn.setAutoCommit(false);
             ps.executeUpdate();
             conn.commit();
         }
         catch (SQLException e){
             conn.rollback();
         }
         finally {
             ps.close();
             conn.setAutoCommit(bl);
         }
 
     }
 
     public String generateMBeanDB(Settings s)  throws  SQLException{
         Connection conn = null;
         Statement st = null;
 
         HypersqlHandler hypersql = new HypersqlHandler();
         hypersql.loadDriver(hypersql.getDriver());
         String dbName = s.getFolderLocation()+"Extrator" + new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
         StringBuilder sb;
         String tablename = null;
         try{
             Properties props = new Properties();
             props.put("username", "sa");
             props.put("password", "whatever");
             conn = hypersql.connectDatabase(dbName, props);
             System.out.println("Database " + dbName + " is created.");
 
             st = conn.createStatement();
             conn.setAutoCommit(false);
 
             for (BeanData bean:s.getBeans()){
                 sb = new StringBuilder();
                 tablename = convertIllegalTableName(bean.getName());
 
                 //1.the bean names are unique, but alias not
                 //2.the field name of "time"(or whatever) should be reserved,  can't be used as an attribute name
                 //3.the datatyype should be valid in embedded SQL (ie. HyperSQL)
                 //All above requirements must be set to a DTD for the setting xml file!!!
                 sb.append("create table " + tablename + "(");
                 for (Attribute ab: bean.getAttributes()){
                     sb.append(ab.getName() + " " + ab.getDataType() + ",");
                 }
                 sb.append("time TIMESTAMP)");
                 st.executeUpdate(sb.toString());
                 System.out.println("Table " + recoverOriginalTableName(tablename) + " is created.");
             }
             conn.commit();
         }
         catch (Exception e){
             System.err.println(e.getMessage());
             conn.rollback();
             return null;
         }
         finally {
             hypersql.shutdownDatabase(conn); //we should shut down a Hsql DB
             DBHandler.disconnectDatabase(null, st, null, conn);
         }
 
         return dbName;
     }
 }
