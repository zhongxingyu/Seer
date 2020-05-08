 package shirorealm;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import org.apache.shiro.realm.jdbc.JdbcRealm;
 
 public class HardcodedJdbcRealm extends JdbcRealm {
 
     public HardcodedJdbcRealm() {
         super();
         //get the DataSource that JSecurity's JdbcRealm
         //should use to find the user's password
         //using the provided username
         //see context.xml for this DataSource's properties
         InitialContext ic;
         DataSource dataSource;
         try {
             ic = new InitialContext();
             //        dataSource = (DataSource) ic.lookup("java:/comp/env/jdbc/security");
             dataSource = (DataSource) ic.lookup("java:/jsf005db");
             this.setDataSource(dataSource);
         } catch (NamingException e) {
             e.printStackTrace();
         }
     }
 }
 
