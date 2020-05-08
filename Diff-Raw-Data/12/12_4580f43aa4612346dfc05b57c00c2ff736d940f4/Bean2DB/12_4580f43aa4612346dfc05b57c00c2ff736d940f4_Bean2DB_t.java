 package org.jmxdatamart.Extractor;
 
 import org.jmxdatamart.JMXTestServer.TestBean;
 import org.jmxdatamart.common.*;
 import org.slf4j.LoggerFactory;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
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
     private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Extractor.class);
 
     public static void main(String[] args) {
         // TODO code application logic here
         int expected = 42;
         //Create new test MBean
         TestBean tb = new TestBean();
         tb.setA(new Integer(expected));
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         String mbName = "org.jmxdatamart.JMXTestServer:type=TestBean";
         ObjectName mbeanName = null;
         try {
         	mbeanName = new ObjectName(mbName);
         } catch (MalformedObjectNameException e) {
         	logger.error("Error creating MBean object name", e);
         	System.exit(0); //this is a fatal error and cannot be resolved later
         } catch (NullPointerException e) {
         	logger.error("Error: no MBean object name provided", e);
         	System.exit(0); //this is a fatal error and cannot be resolved later
         }
         
         try {
         	mbs.registerMBean(tb, mbeanName);
         } catch (InstanceAlreadyExistsException e) {
         	logger.error("Error: " + mbeanName + " already registered with MBeanServer", e);
         } catch (MBeanRegistrationException e) {
         	logger.error("Error registering " + mbeanName + " with MBeanServer", e);
         	System.exit(0); //this is a fatal error and cannot be resolved later
         } catch (NotCompliantMBeanException e) {
         	logger.error("Error: " + mbeanName + " is not compliant with MBeanServer", e);
         	System.exit(0); //this is a fatal error and cannot be resolved later
         }
 
         //Create test MBean's MBeanData
         Attribute a = new Attribute("A", "Alpha", DataType.INT);
 
         MBeanData mbd = new MBeanData(mbName, "testMBean", Collections.singletonList(a), true);
 
         //Init MBeanExtract
         MBeanExtract instance = null;
        try {
        	instance = new MBeanExtract(mbd, mbs);
        } catch (MalformedObjectNameException e) {
        	logger.error(e.getMessage(), e);
        	System.exit(0); //this is a fatal error and cannot be resolved later
        }
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
         Connection conn = null;
         try {
         	conn = hsql.connectDatabase(dbname, props);
         } catch (SQLException e) {
         	logger.error("Error connecting to SQL database", e);
         	System.exit(0); //this is a fatal error and cannot be resolved later
         }
 
         bd.export2DB(conn,mbd,result);
 
         ResultSet rs = null;
         String query = "select * from " + bd.convertIllegalTableName(mbd.getName());
         try {
         	rs = conn.createStatement().executeQuery(query);
         } catch (SQLException e) {
         	logger.error("Error executing SQL query: " + query, e);
         }
         
         try {
         	while(rs.next()){
         		System.out.println(rs.getObject(1) + "\t" + rs.getObject(2));
         	}
         } catch (SQLException e) {
         	logger.error(e.getMessage(), e);
         }
         
         try {
         	hsql.shutdownDatabase(conn);
         } catch (SQLException e) {
         	logger.error("Error shutting down SQL database", e);
         }
         DBHandler.disconnectDatabase(rs, null, null, conn);
     }
 
     //get rid of the . : =, which are illegal for a table name
     public  String convertIllegalTableName(String tablename){
         return tablename.replaceAll("\\.","_").replaceAll(":","__").replaceAll("=","___");
     }
     public  String recoverOriginalTableName(String tablename){
         return tablename.replaceAll("___","=").replaceAll("__",":").replaceAll("_","\\.");
     }
 
     private void dealWithDynamicBean(Connection conn, String tableName , Map<Attribute, Object> result) {
         try {
         	if (!DBHandler.tableExists(tableName,conn))
         		logger.error("Error: " + tableName + " does not exist");
         } catch (SQLException e) {
         	logger.error("Error accessing database handler", e);
         	System.exit(0); //this is a fatal error and cannot be resolved later
         }
         
         String sql;
         boolean bl = false;
         try {
         	bl = conn.getAutoCommit();
         } catch (SQLException e) {
         	logger.error("Error getting auto commit from SQL database connection", e);
         }
         
         try {
         	conn.setAutoCommit(false);
         } catch (SQLException e) {
         	logger.error("Error setting auto commit in SQL database connection", e);
         }
         for (Map.Entry<Attribute, Object> m : result.entrySet()) {
         	try {
         		if (!DBHandler.columnExists(m.getKey().getName(),tableName,conn)){
         			sql = "Alter table " + tableName +" add " +m.getKey().getName()+ " "+m.getKey().getDataType();
         			conn.createStatement().executeUpdate(sql);
         		}
         	} catch (SQLException e) {
         		logger.error("Error accessing SQL database handler", e);
         	}
         }
         
         try {
         	conn.commit();
         } catch (SQLException e) {
         	logger.error("Error with commit to SQL database", e);
         }
         
         try {
         	conn.setAutoCommit(bl);
         } catch (SQLException e) {
         	logger.error("Error setting auto commit in SQL database connection", e);
         }
     }
 
     public void export2DB(Connection conn, BeanData mbd, Map<Attribute, Object> result) {
 
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
         try {
         	ps = conn.prepareStatement(sql);
         } catch (SQLException e1) {
         	logger.error("Error preparing statement for SQL database connection", e1);
         	System.exit(0); //this is a fatal error and cannot be resolved later
         }
 
         //need to think about how to avoid retrieving the map twice
         int i=0;
         for (Map.Entry<Attribute, Object> m : result.entrySet()) {
             switch (((Attribute)m.getKey()).getDataType())
             {
                 case INT:
                 	try {
                 		ps.setInt(++i,(Integer)m.getValue());
                 	} catch (SQLException e) {
                 		logger.error("Error setting statement for SQL prepared statement", e);
                 	}
                 	break;
                 case STRING:
                 	try {
                 		ps.setString(++i,m.getValue().toString());
                 	} catch (SQLException e) {
                 		logger.error("Error setting statement for SQL prepared statement", e);
                 	}
                 	break;
                 case FLOAT:
                 	try {
                 		ps.setFloat(++i,(Float)m.getValue());
                 	} catch (SQLException e) {
                 		logger.error("Error setting statement for SQL prepared statement", e);
                 	}
                 	break;
             }
         }
         try {
         	ps.setTimestamp(++i, new Timestamp((new java.util.Date()).getTime()));
         } catch (SQLException e1) {
         	logger.error("Error setting timestamp for SQL prepared statement", e1);
         }
 
         boolean bl=false;
         try {
         	bl = conn.getAutoCommit();
         	conn.setAutoCommit(false);
         	ps.executeUpdate();
         	conn.commit();
         } catch (SQLException e){
         	try {
         		conn.rollback();
         	} catch (SQLException e1) {
         		logger.error("Error rolling back SQL database connection", e);
         	}
         } finally {
         	try {
         		ps.close();
         	} catch (SQLException e) {
         		logger.error("Error closing SQL prepared statement", e);
         	}
         	
         	try {
         		conn.setAutoCommit(bl);
         	} catch (SQLException e) {
         		logger.error("Error setting auto commit for SQL database connection", e);
         	}
         }
     }
 
     public String generateMBeanDB(Settings s) {
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
         } catch (Exception e){
         	System.err.println(e.getMessage());
         	try {
         		conn.rollback();
         	} catch (SQLException e1) {
         		logger.error("Error rolling back SQL database connection", e);
         	}
         	return null;
         }
         finally {
         	try {
         		hypersql.shutdownDatabase(conn);
         	} catch (SQLException e) {
         		logger.error("Error shutting down SQL database", e);
         	} //we should shut down a Hsql DB
         	DBHandler.disconnectDatabase(null, st, null, conn);
         }
         return dbName;
     }
 }
