 package se.cygni.expenses;
 
 import com.yammer.dropwizard.Service;
 import com.yammer.dropwizard.config.Bootstrap;
 import com.yammer.dropwizard.config.Environment;
 import com.yammer.dropwizard.jdbi.DBIFactory;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.Handle;
 import se.cygni.expenses.jdbi.EventsRepository;
 import se.cygni.expenses.resources.EventsResource;
 
 public class MainService extends Service<MainConfiguration> {
     public static void main(String[] args) throws Exception {
         new MainService().run(args);
     }
 
     @Override
     public void initialize(Bootstrap<MainConfiguration> mainConfigurationBootstrap) {
 
     }
 
     @Override
     public void run(MainConfiguration config, Environment environment) throws Exception {
 
         DBI dbi = initDatabaseConnection(config, environment);
 
         final EventsRepository eventsRepository = dbi.onDemand(EventsRepository.class);
 
         //final UserDAO dao = jdbi.onDemand(UserDAO.class);
         //environment.addResource(new UserResource(dao));
 
         environment.addResource(new EventsResource(eventsRepository));
     }
 
     private DBI initDatabaseConnection(MainConfiguration config, Environment environment) throws ClassNotFoundException {
 
         final DBIFactory factory = new DBIFactory();
         final DBI jdbi = factory.build(environment, config.getDatabaseConfiguration(), "db");
 
         createTables(jdbi);
 
         return jdbi;
     }
 
     private void createTables(DBI dbi) {
 
         Handle handle = dbi.open();
 
         handle.execute("CREATE TABLE IF NOT EXISTS event (" +
                 "id BIGINT, " +
                "name VARCHAR(40), " +
                "date TIMESTAMP, " +
                 "PRIMARY KEY(id)" +
                 ")");
 
         handle.execute("CREATE TABLE IF NOT EXISTS expense (" +
                 "id BIGINT, " +
                 "description VARCHAR(40), " +
                 "person VARCHAR(55), " +
                 "date TIMESTAMP, " +
                 "amount INT, " +
                 "eventId BIGINT, " +
                 "PRIMARY KEY(id), " +
                 "FOREIGN KEY(eventId) REFERENCES event(id)" +
                 ")");
 
         handle.close();
     }
 }
