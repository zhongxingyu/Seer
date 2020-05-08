 package no.kantega.blog.initializer;
 
 import no.kantega.blog.dao.BlogDao;
 import no.kantega.blog.filter.CharEncodingFilter;
 import no.kantega.blog.listener.BlogSessionListener;
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.derby.jdbc.ClientDriver40;
 
 import javax.servlet.DispatcherType;
 import javax.servlet.ServletContainerInitializer;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import static no.kantega.blog.services.Services.addService;
 import static no.kantega.blog.services.Services.getService;
 
 /**
  * Initializes the container.
  */
 public class BlogInitializer implements ServletContainerInitializer {
     
     private static final String DB_ALREADY_EXISTS = "X0Y32";
 
     /**
      * Called when the container starts.
      * 
      * @param classes Classes loaded
      * @param servletContext Context we configure
      * @throws ServletException In case we fail to start
      */
     @Override
     public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
         Logger.getLogger(getClass().getName()).info("Starting up");
 
         configureSessionListener(servletContext);
         addService(DataSource.class, initializeDatasource(), servletContext);
         addService(BlogDao.class, new BlogDao(getService(DataSource.class, servletContext)), servletContext);
         configureCharEncFilter(servletContext);
     }
 
     private void configureSessionListener(ServletContext servletContext) throws ServletException {
         BlogSessionListener blogSessionListener = servletContext.createListener(BlogSessionListener.class);
         servletContext.addListener(blogSessionListener);
         addService(BlogSessionListener.class, blogSessionListener, servletContext);
     }
 
     private void configureCharEncFilter(ServletContext servletContext) {
         EnumSet<DispatcherType> enums = EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST, DispatcherType.FORWARD);
         servletContext.addFilter("charEncoding", CharEncodingFilter.class).addMappingForUrlPatterns(enums, false, "*.jsp");
     }
 
     /**
      * Prepare data source (database connection).
      * 
      * @return The data source to use
      * @throws ServletException In case we can't get a database connection
      */
     private DataSource initializeDatasource() throws ServletException {
         BasicDataSource dataSource = new BasicDataSource();
         dataSource.setDriverClassName(ClientDriver40.class.getName());
         dataSource.setUrl("jdbc:derby://localhost:1527/blogdb;create=true");
 
         //dataSource.setMaxActive(50);
 
         createTables(dataSource);
         return dataSource;
     }
 
     /**
      * Create tables used in the application.
      * 
      * @param dataSource Data source to create them in
      * @throws ServletException In case tables can't be created
      */
     private void createTables(DataSource dataSource) throws ServletException {
         try (Connection connection = dataSource.getConnection()) {
             List<String> statements = Arrays.asList(
                     "create table blog (blogid  integer NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                             " blogname varchar(255) NOT NULL UNIQUE, " +
                             " color varchar(7) )",
 
                     "create table blogpost (blogpostid  integer NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                             " blogid integer NOT NULL,  " +
                            " posttitle varchar(255) NOT NULL UNIQUE, " +
                             " postcontent clob (500K) NOT NULL, " +
                             " publishdate timestamp NOT NULL " +
                             " )",
                     "create table blogpostcomment (blogpostcommentid  integer NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                                                 " blogpostid integer NOT NULL,  " +
                                                 " commentauthor varchar(255) NOT NULL, " +
                                                 " commentcontent clob (500K) NOT NULL, " +
                                                 " commentpublishdate timestamp NOT NULL " +
                                                 " )");
 
             try (Statement statement = connection.createStatement()) {
                 for (String sql : statements) {
                     statement.execute(sql);
                 }
             }
         } catch (SQLException e) {
             if (!DB_ALREADY_EXISTS.equals(e.getSQLState())) {
                 throw new ServletException(e);
             }
         }
     }
 }
