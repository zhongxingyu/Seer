 import java.net.UnknownHostException;
 
 import org.xorrr.util.EnvironmentVars;
 
 import com.mongodb.MongoClient;
 import com.mongodb.MongoClientURI;
 
 import earth.xor.rest.SparkRestApi;
 
 public class Main {
 
     public static void main(String args[]) throws UnknownHostException {

         MongoClientURI mongoUri = new MongoClientURI(
                 System.getenv(EnvironmentVars.URI));
         MongoClient client = new MongoClient(mongoUri);
 
        SparkRestApi rest = new SparkRestApi();
        rest.launchServer(client);
     }
 }
