 package osa3;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.*;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 
 import org.apache.commons.dbutils.DbUtils;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.taskdefs.SQLExec;
 
 public abstract class AbstractDAO {
 	
    private final String dbUrl = "jdbc:hsqldb:file:${user.home}/data/veikovx/PisiPaha;hsqldb.lock_file=false";
 //    private final String dbUrl = "jdbc:hsqldb:mem:PisiPaha";
 
     private Connection connection;
     protected PreparedStatement pst;
     protected Statement st;
     protected ResultSet rs;
 
     static {
         try {
             Class.forName("org.hsqldb.jdbcDriver");
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     protected Connection getConnection() {
         try {
             connection = DriverManager.getConnection(dbUrl, "sa", "");
             return connection;
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void closeResources() {
         DbUtils.closeQuietly(rs);
         DbUtils.closeQuietly(pst);
         DbUtils.closeQuietly(st);
         DbUtils.closeQuietly(connection);
     }
 
     protected boolean tableExists(String tableName) {
         boolean exists = false;
 		try {
 			DatabaseMetaData md = getConnection().getMetaData();
 			ResultSet rs = md.getTables(null, null, tableName.toUpperCase(), null);
 			exists = rs.next();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		} finally {
             closeResources();
         }
 		return exists;
     }
 
     protected void executeQuery(String queryString) {
         try {
              st = getConnection().createStatement();
              rs = st.executeQuery(queryString);
          } catch (Exception e) {
              throw new RuntimeException(e);
          } finally {
              closeResources();
          }
     }
 
     protected void executeUpdate(String queryString) {
         try {
              st = getConnection().createStatement();
              st.executeUpdate(queryString);
          } catch (Exception e) {
              throw new RuntimeException(e);
          } finally {
              closeResources();
          }
     }
 
     protected void executeQueryPrep(String queryString, LinkedHashMap<Integer, String> map) {
         try {
              pst = getConnection().prepareStatement(queryString);
              Iterator<Entry<Integer, String>> iterator = map.entrySet().iterator();
              while(iterator.hasNext()){
                  Entry<Integer, String> entry = iterator.next();
                  pst.setString(entry.getKey(), entry.getValue());
              }
              pst.execute();
              rs = pst.getResultSet();
          } catch (Exception e) {
              throw new RuntimeException(e);
          } finally {
              closeResources();
          }
     }
     
     protected URI getClassPathFile(String fileName) {
     	URI path = null;
         try {
         	path = getClass().getResource(fileName).toURI();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 		return path;
     }
     
     protected void executeSqlFromFile(URI sqlFileURI) {
 
         Project project = new Project();
         project.init();
 
         SQLExec e = new SQLExec();
         e.setProject(project);
         e.setTaskType("sql");
         e.setTaskName("sql");
         e.setSrc(new File(sqlFileURI));
         e.setDriver("org.hsqldb.jdbcDriver");
         e.setUserid("sa");
         e.setPassword("");
         e.setUrl(dbUrl);
         e.execute();
     }
 
 }
