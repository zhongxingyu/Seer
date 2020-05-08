 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package ${package}.web.testsupport;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import javax.servlet.ServletContext;
 import javax.sql.DataSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 import org.springframework.core.io.FileSystemResourceLoader;
 import org.springframework.mock.web.MockServletContext;
 import org.springframework.web.context.support.XmlWebApplicationContext;
 
 /** Utility class to give support to test cases.
  *
  * @author nicolas.frontini
  */
 public final class SpringTestUtils {
 
   /** A logger.
    */
   private static Log log = LogFactory.getLog(SpringTestUtils.class);
 
   /** a data source.
    */
   private static DataSource dataSource = null;
 
   /** Bean factory.
    */
   private static ApplicationContext beanFactory;
 
   /** User module Bean factory.
    */
   private static ApplicationContext userModuleBeanFactory;
 
  /** Time entry module Bean factory.
   */
  private static ApplicationContext timeModuleBeanFactory;

   /** A private constructor so no instances are created.
    */
   private SpringTestUtils() {
   }
 
   /** Gets the configured data source.
    *
    * @return a DataSource.
    */
   public static synchronized DataSource getDataSource() {
     if (dataSource == null) {
       beanFactory = getBeanFactory();
       dataSource = (DataSource) beanFactory.getBean("dataSource");
     }
     return dataSource;
   }
 
   /** Gets the connection to the database.
    *
    * @return a Connection.
    *
    * @exception SQLException if a database access error occurs
    */
   public static synchronized Connection getConnection() throws SQLException {
     if (dataSource == null) {
       beanFactory = getBeanFactory();
       dataSource = (DataSource) beanFactory.getBean("dataSource");
     }
     Connection connection = dataSource.getConnection();
     return connection;
   }
 
   /** This method returns a BeanFactory.
    *
    * @return a BeanFactory
    */
   public static synchronized ApplicationContext getBeanFactory() {
     if (beanFactory == null) {
       log.info("Creating a beanFactory");
       ServletContext sc = new MockServletContext("./src/main/webapp",
           new FileSystemResourceLoader());
       XmlWebApplicationContext appContext = new XmlWebApplicationContext();
       appContext.setServletContext(sc);
       appContext.setConfigLocations(new String[] {
         "/WEB-INF/applicationContext.xml",
         "/WEB-INF/applicationContextRuntime.xml",
         "classpath:/com/globant/katari/user/view/spring-servlet.xml" });
       appContext.refresh();
       beanFactory = appContext;
     }
     return beanFactory;
   }
 
   /** This method returns a BeanFactory.
    *
    * @return a BeanFactory
    */
   public static synchronized ApplicationContext getUserModuleBeanFactory() {
     if (userModuleBeanFactory == null) {
       log.info("Creating a beanFactory");
       userModuleBeanFactory = new FileSystemXmlApplicationContext(
           new String[]
           {"classpath:/com/globant/katari/user/view/spring-servlet.xml"},
           getBeanFactory());
     }
     return userModuleBeanFactory;
   }
 }
 
