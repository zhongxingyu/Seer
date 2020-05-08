 package org.cotrix.io.map;
 
 import static org.cotrix.domain.utils.Utils.*;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.cotrix.domain.Codelist;
 import org.cotrix.io.utils.Registry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Default implementation of {@link MapService}.
  * 
  * @author Fabio Simeoni
  *
  */
 @Singleton
 public class DefaultMapService implements MapService {
 
 	private static Logger log = LoggerFactory.getLogger(DefaultMapService.class);
 	
 	private final Registry<MapTask<Object,MapDirectives<Object>>> registry;
 	
 	@Inject
 	public DefaultMapService(Registry<MapTask<Object,MapDirectives<Object>>> registry) {
 		
 		notNull("parser registry",registry);
 		
 		this.registry=registry;
 		
 		log.info("configured with tasks {}",registry.getAll());
 	}
 	
 	@Override
 	public <T> Outcome map(T list, MapDirectives<T> directives) {
 		
 		notNull("external codelist",list);
 		notNull("map directives",directives);
 		
 		double time = System.currentTimeMillis();
 		
 		try {
 
 			//safe: map directives are always bound to map tasks, convince compiler
 			@SuppressWarnings("all")
 			MapTask<T,MapDirectives<T>> task = (MapTask) registry.get(directives);
 			
 			Codelist mapped =  task.map(list,directives);
 			
 			time = (System.currentTimeMillis()-time)/1000;
 			
 			log.info("mapped list in {} secs.",time);
 			
 			return new Outcome(mapped);
 			
 		}
 		catch(Exception e) {
 			throw new IllegalStateException("could not map codelist with directives "+directives+" (see cause) ",e);
 		}
 	}
 }
