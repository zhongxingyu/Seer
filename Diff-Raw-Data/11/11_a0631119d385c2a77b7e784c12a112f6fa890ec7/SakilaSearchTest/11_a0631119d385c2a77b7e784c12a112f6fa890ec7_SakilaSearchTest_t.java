 package client;
 
 import org.apache.cxf.endpoint.Server;
 import org.apache.cxf.interceptor.LoggingInInterceptor;
 import org.apache.cxf.interceptor.LoggingOutInterceptor;
 import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
 import org.apache.cxf.jaxrs.client.WebClient;
 import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
 import org.junit.*;
 import resources.SakilaResource;
 import sakila.Actor;
 import sakila.Film;
 import sakila.Rental;
 
 import java.util.Collection;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * User: aruld
  * Date: 10/16/10
  * Time: 11:53 PM
  */
 public class SakilaSearchTest {
 
     static Server server;
 
     @BeforeClass
     public static void setUp() {
         JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
         sf.setResourceClasses(SakilaResource.class);
         sf.getInInterceptors().add(new LoggingInInterceptor());
         sf.getOutInterceptors().add(new LoggingOutInterceptor());
         sf.setResourceProvider(SakilaResource.class, new SingletonResourceProvider(new SakilaResource()));
         sf.setAddress("http://localhost:9000");
         server = sf.create();
     }
 
     @Test
     public void searchActors() {
         //http://localhost:9000/sakila/searchActors?_s=firstname==PENELOPE
        WebClient wc = WebClient.create("http://localhost:9000/sakila/searchActors");
        wc.query("_s", "firstname==PENELOPE");
         Collection<? extends Actor> actors = wc.getCollection(Actor.class);
         assertEquals(4, actors.size());
     }
 
     @Test
     public void searchFilms() {
         //http://localhost:9000/sakila/searchFilms?_s=rating==PG;rentalduration!=0;title==SANTA*
        WebClient wc = WebClient.create("http://localhost:9000/sakila/searchFilms");
        wc.query("_s","rating==PG;rentalduration!=0;title==SANTA*");
         Collection<? extends Film> films = wc.getCollection(Film.class);
         assertEquals(1, films.size());
     }
 
     @Test
     public void searchRentals() {
         //http://localhost:9000/sakila/searchRentals?_s=rentaldate=lt=2005-05-27T00:00:00.000%2B00:00
        WebClient wc = WebClient.create("http://localhost:9000/sakila/searchRentals");
        wc.query("_s","rentaldate=lt=2005-05-27T00:00:00.000%2B00:00");
         Collection<? extends Rental> rentals = wc.getCollection(Rental.class);
         assertEquals(278, rentals.size());
     }
 
     @AfterClass
     public static void tearDown() {
         server.destroy();
     }
 }
