 import java.net.UnknownHostException;
 
 import org.xorrr.util.EnvironmentVars;
 
 import com.mongodb.MongoClient;
 import com.mongodb.MongoClientURI;
 
import earth.xor.db.DatastoreFacade;
import earth.xor.db.LinksDatastore;
import earth.xor.db.MongoLinksDatastore;
 import earth.xor.rest.SparkRestApi;
 
 public class Main {
 
     public static void main(String args[]) throws UnknownHostException {
         MongoClientURI mongoUri = new MongoClientURI(
                 System.getenv(EnvironmentVars.URI));
         MongoClient client = new MongoClient(mongoUri);
        LinksDatastore ds = new MongoLinksDatastore(client);
        DatastoreFacade facade = new DatastoreFacade(ds);
 
        SparkRestApi rest = new SparkRestApi(facade);
        rest.startApi();
     }
 }
