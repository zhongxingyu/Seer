 package org.jboss.hibernateUniversity.criteria.action;
 
 import org.jboss.weld.environment.se.Weld;
 import org.jboss.weld.environment.se.WeldContainer;
 import org.junit.Test;
 
 /**
  * @author Emmanuel Bernard
  */
 public class BookManagerTest {
 
 	@Test
 	public void testQuery() {
 		final Weld weld = new Weld();
 		WeldContainer weldContainer = weld.initialize();
 
 		final BookManager userManager = weldContainer.instance().select( BookManager.class ).get();
		userManager.getAllBooksStarred5InTheLast10Years();
 
 		weld.shutdown();
 	}
 
 	@Test
 	public void testUserCreation() {
 		final Weld weld = new Weld();
 		WeldContainer weldContainer = weld.initialize();
 
 		final BookManager userManager = weldContainer.instance().select( BookManager.class ).get();
 		userManager.createNewUsers();
 		
 		weld.shutdown();
 	}
 
 }
