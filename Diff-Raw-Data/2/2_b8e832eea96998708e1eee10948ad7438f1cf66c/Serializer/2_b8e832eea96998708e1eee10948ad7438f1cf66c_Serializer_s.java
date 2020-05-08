 package org.yajug.users.json;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yajug.users.domain.DomainObject;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.google.gson.reflect.TypeToken;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * Shared JSON serializer based on the GSON library
  * 
  * @author Bertrand Chevrier <bertrand.chevrier@yajug.org>
  */
 @Singleton
 public class Serializer {
 
 	private final static Logger logger = LoggerFactory.getLogger(Serializer.class);
 	
 	@Inject private DateSerializer dateSerializer;
 	
 	private Gson gson;
 	
 	/**
 	 * Create a JSON deserializer adapted for the DomainObjects
 	 * @return the deserializer
 	 */
 	public Gson get(){
 		if(gson == null){
 			gson = buildGson();
 		}
 		return gson;
 	}
 	
 	/**
 	 * Build the serializer
 	 * @return the gson serializer
 	 */
 	private Gson buildGson(){
 		return new GsonBuilder()
 				.serializeNulls()
 				//manages dates
 				.registerTypeAdapter(Date.class, dateSerializer)
 				//manage domains lists
 				.registerTypeAdapter(List.class, new DomainListDeserializer())
 				.create();
 	}
 	
 	/**
 	 * Custom De-serializer that converts lists of ids to {@link DomainObject}
 	 */
 	private final class DomainListDeserializer implements
 			JsonDeserializer<List<? extends DomainObject>> {
 		
 		/**
 		 * create an other parser to avoid stack overflow with context parser
 		 */
 		private Gson gson =  new GsonBuilder().create();
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public List<? extends DomainObject> deserialize(JsonElement json, final Type typeOfT, JsonDeserializationContext context)
 				throws JsonParseException {
 			
 			Type[] typeArgs = ((ParameterizedType)typeOfT).getActualTypeArguments();
 			if(typeArgs.length > 0){
 				
 				boolean domainList = false;
 				final Class<?> generic = (Class<?>)typeArgs[0];
 				
 				//check if the list is a list of DomainObjects
 				if(DomainObject.class.isAssignableFrom(generic)){
 					 domainList = true;
 				}
 				
 				//if this is Domain Objects
 				if(domainList){
 					//we get the JSON array of ids
 					List<Long> ids = gson.fromJson(json, new TypeToken<ArrayList<Long>>(){}.getType());
 					//and transform it into a list of instance with only the key set
 					return Lists.transform(ids, new Function<Long, DomainObject>() {
 	
 						@Override public DomainObject apply(Long input) {
 							DomainObject domainObject = null;
 							try {
								 domainObject = (DomainObject) Class.forName(generic.getClass().getName()).newInstance() ;
 								 domainObject.setKey(input);
 							} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
 								logger.error(e.getLocalizedMessage(), e);
 							}
 							return domainObject;
 						}
 						
 					});
 				} 
 			}
 			//or do a simple parsing
 			return gson.fromJson(json, typeOfT);
 		}
 	}
 }
