 package fulbot.persistence;
 
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 
 import com.github.jknack.mwa.morphia.MorphiaModule;
 import com.github.jmkgreen.morphia.Datastore;
 
 @Configuration
 @Import(MorphiaModule.class)
 public class PersistenceModule {
 
	private PersistenceModule(Datastore datastore) {
 		
 		//ensure indexes are created in mongodb
 		datastore.ensureIndexes();
 	}
 	
 	
 
 }
