 package ch.hsr.bieridee.config;
 
 import org.codehaus.jackson.Version;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.codehaus.jackson.map.module.SimpleModule;
 
 import ch.hsr.bieridee.serializer.BeerListSerializer;
 import ch.hsr.bieridee.serializer.BeerSerializer;
 import ch.hsr.bieridee.serializer.BeertypeListSerializer;
 import ch.hsr.bieridee.serializer.BeertypeSerializer;
 import ch.hsr.bieridee.serializer.TagListSerializer;
 import ch.hsr.bieridee.serializer.TagSerializer;
 import ch.hsr.bieridee.serializer.UserListSerializer;
 import ch.hsr.bieridee.serializer.UserSerializer;
 
 /**
  * Configuration constants.
  *
  */
 public final class Config {
 	
 	private static ObjectMapper OBJECT_MAPPER;
 	
 	/**
 	 * Relative path to the neo4j graph database.
 	 */
 	public static final String DB_PATH = "var/TestDB";
 	//public static final String DB_PATH = "var/Brauhaus";
 	
 	// dummy constructor
 	private Config() {
 		// do not instanciate.
 	}
 	
 	/**
 	 * Returns the ObjectMapper for the JacksonSerializations.
 	 * 
 	 * @return ObjectMapper
 	 */
 	public static ObjectMapper getObjectMapper() {
 		
 		if(OBJECT_MAPPER != null) {
 			return OBJECT_MAPPER;
 		}
 		
 		synchronized(Config.class) {
			if(OBJECT_MAPPER == null) {
 				OBJECT_MAPPER= new ObjectMapper();
 				OBJECT_MAPPER.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
 				
 				final SimpleModule beerModule = new SimpleModule("BierIdeeModule", new Version(1, 0, 0, null));
 				beerModule.addSerializer(new BeerSerializer());
 				beerModule.addSerializer(new BeerListSerializer());
 				beerModule.addSerializer(new BeertypeSerializer());
 				beerModule.addSerializer(new BeertypeListSerializer());
 				beerModule.addSerializer(new TagSerializer());
 				beerModule.addSerializer(new TagListSerializer());
 				beerModule.addSerializer(new UserSerializer());
 				beerModule.addSerializer(new UserListSerializer());
 				
 				OBJECT_MAPPER.registerModule(beerModule);
 			}
 			return OBJECT_MAPPER;
 		}
 	}
 	
 }
