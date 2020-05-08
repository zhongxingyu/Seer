 package br.ime.usp.commendans.components;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.servlet.ServletContext;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 import br.com.caelum.vraptor.ioc.ApplicationScoped;
 import br.com.caelum.vraptor.ioc.Component;
 import br.com.caelum.vraptor.ioc.ComponentFactory;
 
 @Component @ApplicationScoped
 public class SessionFactoryCreator implements ComponentFactory<SessionFactory> {
 
     private SessionFactory sessionFactory;
     private static Logger LOG = Logger.getLogger(SessionFactoryCreator.class);
     private String env;
     
     public SessionFactoryCreator(ServletContext context) {
         env = context.getInitParameter("environment");
     }
 
     @Override
     public SessionFactory getInstance() {
         return sessionFactory;
     }
     
     @PreDestroy
     public void destroy() {
         sessionFactory.close();
     }
     
     @PostConstruct
     public void create() {
         if (env.equals("heroku")) {
             
             URI dbUri;
             try {
                 dbUri = new URI(System.getenv("DATABASE_URL"));
             } catch (URISyntaxException e) {
                 throw new RuntimeException(e);
             }
 
             String username = dbUri.getUserInfo().split(":")[0];
             String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + "/" + dbUri.getPort();
             
             LOG.info("using heroku specific confs");
             LOG.info("username: " + username);
             LOG.info("password: " + password);
             LOG.info("dbUrl: " + dbUrl);
             Configuration configuration = new Configuration().configure("/hibernate-heroku.cfg.xml");
             configuration.setProperty("hibernate.connection.url", dbUrl);
             configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
             configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
             configuration.setProperty("hibernate.connection.username", username);
             configuration.setProperty("hibernate.connection.password", password);
             
             sessionFactory = configuration.buildSessionFactory();
         } else {
             LOG.info("using heroku specific normal confs");
             sessionFactory = new Configuration().configure("/hibernate.cfg.xml").buildSessionFactory();
         }
     }
 
 }
