 package org.acme;
 
 import javax.inject.Inject;
 
 import org.cotrix.domain.memory.IdentifiedMS;
 import org.cotrix.test.ApplicationTest;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
public class DomainApplicationTest extends ApplicationTest {
 
 	@Inject
 	SubjectProvider provider;
 		
 	@BeforeClass
 	public static void start() {
 		IdentifiedMS.testmode=true;
 	}
 	
 	@AfterClass
 	public static void end() {
 		IdentifiedMS.testmode=false;
 	}
 		
 		
 	protected <T> T like(T object) {
 		return provider.like(object);
 	}
 }
