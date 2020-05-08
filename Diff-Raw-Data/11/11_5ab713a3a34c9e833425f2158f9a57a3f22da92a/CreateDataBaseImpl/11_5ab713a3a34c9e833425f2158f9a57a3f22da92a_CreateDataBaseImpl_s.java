 package serverMonitoring.dataBase.createDataBase;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.support.JdbcDaoSupport;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import serverMonitoring.dataBase.CreateDataBase;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * This class responsible for creation of entity tables in Data Base
  */
 @Transactional
 @Repository("CreateDataBaseImpl")
 public class CreateDataBaseImpl extends JdbcDaoSupport implements CreateDataBase {
 
     protected static Logger mylog = Logger.getLogger("CreateDataBaseImpl");
     private JdbcTemplate jdbcTemplate;
     private String db_name = "server_monitoring_db";
     private String employeeEntity_table = "employee_entity";
     private String serverEntity_table = "server_entity";
     private String driver = "com.mysql.jdbc.Driver";
     private String url = "jdbc:mysql://localhost:3306/" + db_name;
     private String login = "root";
     private String password = "";
     private Connection connection;
 
     @Resource(name = "dataSource")
     public void initDataSource(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     /**
      * Initialisation of Data Base
      * creation of new if not exists
      */
     @Override
     public void getDBExistsConfirmation() {
         try {
             Class.forName(driver);
             connection = DriverManager.getConnection(url, login, password);
             ResultSet resultSet = connection.getMetaData().getCatalogs();
 
             //iterate each catalog in the ResultSet
            if (!resultSet.next()) {
                createEmployeeEntityTable();
                createServerEntityTable();
             }
             resultSet.close();
         } catch (ClassNotFoundException | SQLException ignored) {
         }
     }
 
     /*
      * Execute SQL query to create employee_entity table
      */
     @Override
     public void createEmployeeEntityTable() {
         String query = "CREATE TABLE " +
                 db_name +
                 employeeEntity_table +
                 "(id int NOT NULL, " +
                 "employee_name varchar(20) NOT NULL, " +
                 "login varchar(20) NOT NULL, " +
                 "password varchar(64) NOT NULL, " +
                 "email varchar(20) NOT NULL, " +
                 "created datetime NOT NULL," +
                 "lastLogin datetime NOT NULL," +
                 "active int(1) NOT NULL," +
                 "admin int(1) NOT NULL)";
         this.jdbcTemplate.update(query);
     }
 
     /*
     * Execute SQL query to create server_entity table
     */
     @Override
     public void createServerEntityTable() {
         String query = "CREATE TABLE " +
                 db_name +
                 serverEntity_table +
                 "(id int NOT NULL, " +
                 "server_name varchar(20) NOT NULL, " +
                 "address varchar(20) NOT NULL, " +
                 "port int(16) NOT NULL, " +
                 "url varchar NOT NULL, " +
                 "state varchar NOT NULL," +
                 "response varchar NOT NULL," +
                 "created datetime NOT NULL," +
                 "lastCheck datetime NOT NULL," +
                 "active int(1) NOT NULL)";
         this.jdbcTemplate.update(query);
     }
 }
