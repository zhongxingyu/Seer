 package com.sporting.misc;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.seam.security.Credentials;
 import org.jboss.seam.security.Identity;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.solder.logging.Logger;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import com.sporting.DBEnum;
import com.sporting.Property;

 @RunWith(Arquillian.class)
 public class EverythingTest {
 
 	@Deployment
 	public static Archive<WebArchive> setup() {
 		return BaseIntegrationTest.createTestArchive();
 	}
 
 	private static final Logger logger = Logger.getLogger(EverythingTest.class);
 
 	@PersistenceContext(unitName = "testDS-PU")
 	private EntityManager em;
 
 	@Inject
 	private Credentials credentials;
 
 	@Inject
 	private Identity ident;
 
 	@Test
 	public void loginTest() {
 		// credentials.setCredential(new PasswordCredential("member0"));
 		// credentials.setUsername("member0@sportivity.com");
 		// ident.login();
 		if (!ident.isLoggedIn()) {
			Property p = new Property(DBEnum.TEST, "long weekend testing2");
			em.persist(p);
			Property property = Property.getProperty(DBEnum.TOMEE, em);
			if (property != null) {
				logger.info("found prop: " + property.getValue());
			}
 		}
 	}
 
 }
