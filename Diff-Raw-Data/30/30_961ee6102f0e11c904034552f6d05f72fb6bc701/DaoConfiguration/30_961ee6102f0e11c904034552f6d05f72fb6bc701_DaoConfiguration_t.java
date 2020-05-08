 package model.dao;
 
 import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
 
 import com.github.jmkgreen.morphia.logging.MorphiaLoggerFactory;
 import com.github.jmkgreen.morphia.logging.slf4j.SLF4JLogrImplFactory;
 
 public class DaoConfiguration {
 
	public static MongoClientURI dBAddress;
 
	public static MongoClient mongo;
 
 	public static Morphia morphia = new Morphia();
 
 }
