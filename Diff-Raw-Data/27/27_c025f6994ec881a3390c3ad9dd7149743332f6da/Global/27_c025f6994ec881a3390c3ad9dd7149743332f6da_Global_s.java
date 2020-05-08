 //https://github.com/natoine/Argumentea/blob/master/ArgumenteaServer/app/Global.java
 
 import java.net.UnknownHostException;
 
 import models.Annotation;
 import models.Quadrant;
 import models.Video;
 import play.GlobalSettings;
 import play.Logger;
 
 import com.google.code.morphia.Morphia;
 import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
 
 import controllers.MorphiaObject;
 
 public class Global extends GlobalSettings 
 {
 
 	@Override
 	public void onStart(play.Application arg0) {
 		super.beforeStart(arg0);
 		Logger.debug("** onStart **");
 		try {
 			MorphiaObject.mongo = new Mongo("linus.mongohq.com", 10076);
 			//MorphiaObject.mongo = new Mongo("localhost", 27017);
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		MorphiaObject.morphia = new Morphia();
 		
 		MorphiaObject.morphia.map(Annotation.class);
 		MorphiaObject.morphia.map(Video.class);
 		MorphiaObject.morphia.map(Quadrant.class);
 
 		/*
 		MorphiaObject.morphia.mapPackageFromClass(Annotation.class);
 		MorphiaObject.morphia.mapPackageFromClass(Video.class);
 		MorphiaObject.morphia.mapPackageFromClass(Timecode.class);
 		MorphiaObject.morphia.mapPackageFromClass(User.class);
 		MorphiaObject.morphia.mapPackageFromClass(Intervenant.class);
 		*/
 		
		//MorphiaObject.datastore = MorphiaObject.morphia.createDatastore(MorphiaObject.mongo, "TERannot", "ter", "1234".toCharArray());
 		//MorphiaObject.fs = new GridFS(MorphiaObject.datastore.getDB(), "Video");
 		//MorphiaObject.datastore.ensureCaps();
 		
		//MorphiaObject.datastore.ensureIndexes();
 		
 
 
 
 	}
 }
 
