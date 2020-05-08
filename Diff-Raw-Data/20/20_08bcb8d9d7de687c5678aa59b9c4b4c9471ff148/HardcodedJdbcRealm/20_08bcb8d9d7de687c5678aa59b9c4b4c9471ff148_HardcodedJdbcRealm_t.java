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
            System.out.println("1");
             ic = new InitialContext();
            System.out.println("2");
             //        dataSource = (DataSource) ic.lookup("java:/comp/env/jdbc/security");
             dataSource = (DataSource) ic.lookup("java:/jsf005db");
            System.out.println("3");
            System.out.println("data source is: "+dataSource);
             this.setDataSource(dataSource);
            System.out.println("4");
         } catch (NamingException e) {
             e.printStackTrace();
            throw new RuntimeException(); // if we can't find the source, we should panic
         }
     }
 }
 
