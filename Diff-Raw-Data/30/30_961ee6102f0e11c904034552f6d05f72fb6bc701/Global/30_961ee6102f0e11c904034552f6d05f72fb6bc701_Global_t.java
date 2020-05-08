 import play.Application;
 import play.GlobalSettings;
 
 import com.github.jmkgreen.morphia.logging.MorphiaLoggerFactory;
 import com.github.jmkgreen.morphia.logging.slf4j.SLF4JLogrImplFactory;
 import java.net.UnknownHostException;
 import java.lang.RuntimeException;
 
 import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
 import model.dao.DaoConfiguration;
 
 public class Global extends GlobalSettings {
 
 
 	@Override
 	public void beforeStart(Application app) {
 		MorphiaLoggerFactory.reset();
 		MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory.class);
 
 		try {
			DaoConfiguration.dBAddress = new MongoClientURI(app.configuration().getString("mongodb.uri"));
 			DaoConfiguration.mongo = new MongoClient(DaoConfiguration.dBAddress);
 		} catch (UnknownHostException e) {
 			throw new RuntimeException("Couldn't connect to mongo", e);
 		}
 	}
 
 }
