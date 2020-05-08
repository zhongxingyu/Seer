 package org.hibernate.ogm.demo.intro.action;
 
 import java.util.Collection;
 import java.util.Map;
 
 import org.infinispan.Cache;
 import org.infinispan.manager.CacheContainer;
 import org.jboss.weld.environment.se.Weld;
 import org.jboss.weld.environment.se.WeldContainer;
 import org.junit.Test;
 
 import org.hibernate.ogm.grid.RowKey;
 
 /**
  * @author Emmanuel Bernard
  */
 public class BookManagerTest {
 
 	@Test
 	public void testQuery() {
 		final Weld weld = new Weld();
 		WeldContainer weldContainer = weld.initialize();
 
		final BookManager userManager = weldContainer.instance().select( BookManager.class ).get();
		userManager.getAllBooksWhoseAuthorIsEmmanuel();
 
 		weld.shutdown();
 	}
 
 	@Test
 	public void testUserCreation() {
 		final Weld weld = new Weld();
 		WeldContainer weldContainer = weld.initialize();
 
		final BookManager userManager = weldContainer.instance().select( BookManager.class ).get();
 		long start = System.nanoTime();
		userManager.createNewUsers();
 		long end = System.nanoTime();
 		System.out.println("Creation took " + ( end-start ) / 1000000 + " ms");
 
 		//exploreCache( userManager );
 
 		weld.shutdown();
 	}
 
 	private void exploreCache(BookManager userManager) {
 		final CacheContainer cacheContainer = userManager.getCacheManager();
 		StringBuilder data = new StringBuilder( "Cache dump" );
 
 		final Cache<Object,Object> entities = cacheContainer.getCache( "ENTITIES" );
 		data.append( "\n" ).append( "ENTITIES (" ).append( entities.size() ).append( ")\n" );
 		for ( Map.Entry<Object,Object> entry : entities.entrySet() ) {
 			data.append( entry.getKey() ).append( "\n" );
 			displayTuple( entry.getValue(), data);
 		}
 
 		final Cache<Object, Object> associations = cacheContainer.getCache( "ASSOCIATIONS" );
 		data.append( "\n" ).append( "ASSOCIATIONS (" ).append( associations.size() ).append( ")\n" );
 		for ( Map.Entry<Object,Object> entry : associations.entrySet() ) {
 			data.append( entry.getKey() ).append( "\n" );
 			displayAssociationTuples( entry.getValue(), data );
 		}
 		System.out.println( data );
 	}
 
 	private void displayTuple(Object value, StringBuilder data) {
 		Map<String,Object> tuple = (Map<String,Object>) value;
 		data.append( "\t[\n" );
 		for (Map.Entry<String,Object> entry : tuple.entrySet()) {
 			data.append( "\t" ).append( entry.getKey() ).append( ": " ).append( entry.getValue() ).append( "\n" );
 		}
 		data.append( "\t]\n\n" );
 	}
 
 	private void displayAssociationTuples(Object value, StringBuilder data) {
 		Collection<Map<String,Object>> tuples = ((Map<RowKey,Map<String,Object>>) value).values();
 		data.append( "\t[\n" );
 		for ( Map<String,Object> tuple : tuples ) {
 			data.append( "\t\t[\n" );
 			for (Map.Entry<String,Object> entry : tuple.entrySet()) {
 				data.append( "\t\t" ).append( entry.getKey() ).append( ": " ).append( entry.getValue() ).append( "\n" );
 			}
 			data.append( "\t\t[\n" );
 		}
 		data.append( "\t]\n" );
 	}
 
 }
