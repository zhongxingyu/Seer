 package kanbannow;
 
 import com.yammer.dropwizard.jdbi.DBIFactory;
 import com.yammer.dropwizard.jdbi.bundles.DBIExceptionsBundle;
 import com.yammer.metrics.reporting.GraphiteReporter;
 import kanbannow.health.CardServiceHealthCheck;
//import kanbannow.health.CardServicePostponeCardHealthCheck;
 import kanbannow.jdbi.CardDAO;
 import kanbannow.resources.CardResource;
 
 import com.yammer.dropwizard.Service;
 import com.yammer.dropwizard.config.Bootstrap;
 import com.yammer.dropwizard.config.Environment;
 import org.skife.jdbi.v2.DBI;
 
 import java.util.concurrent.TimeUnit;
 
 public class CardService extends Service<CardServiceConfiguration> {
     public static void main(String[] args) throws Exception {
         new CardService().run(args);
     }
 
 
     @Override
     public void initialize(Bootstrap<CardServiceConfiguration> bootstrap) {
         bootstrap.setName("card-service");
         bootstrap.addBundle(new DBIExceptionsBundle());
     }
 
     @Override
     public void run(CardServiceConfiguration configuration, Environment environment) throws Exception {
         final DBIFactory factory = new DBIFactory();
         final DBI jdbi = factory.build(environment, configuration.getDatabase(), "oracle");
         final CardDAO cardDAO = jdbi.onDemand(CardDAO.class);
         environment.addResource(new CardResource( cardDAO ));
         environment.addHealthCheck(new CardServiceHealthCheck(configuration, cardDAO));
 //        environment.addHealthCheck(new CardServicePostponeCardHealthCheck(configuration, cardDAO));
         // Oops, need to pull my graphite key out and put it in config......
         GraphiteReporter.enable(15, TimeUnit.SECONDS, "carbon.hostedgraphite.com", 2003, "0cb986a9-f3e9-4292-8d08-0d3a759e448f");
     }
 
 
 }
