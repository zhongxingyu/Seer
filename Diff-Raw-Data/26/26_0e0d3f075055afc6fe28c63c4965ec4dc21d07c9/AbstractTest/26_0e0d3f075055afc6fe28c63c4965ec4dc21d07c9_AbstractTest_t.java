 package de.shop.Util;
 
 import static javax.transaction.Status.STATUS_ACTIVE;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.Locale;
 import java.util.ServiceLoader;
 
 import javax.annotation.Resource;
 import javax.transaction.SystemException;
 import javax.transaction.UserTransaction;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.shrinkwrap.api.Archive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.ExpectedException;
 
 
 public abstract class AbstractTest {
	private static final String DB_SERVICE_IMPL = "de.shop.Util.impl.DbServiceImpl";

 	protected static final Locale LOCALE = Locale.GERMAN;
 	
 	@Resource(lookup = "java:jboss/UserTransaction")
 	private UserTransaction trans;
 	
 	@Rule
 	public ExpectedException thrown = ExpectedException.none();   // public wegen JUnit
 	
 	/**
 	 */
 	@Deployment
 	protected static Archive<?> deployment() {
 		return ArchiveService.getInstance().getArchive();
 	}
 	
 	@BeforeClass
	public static void dbreload() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		//final ServiceLoader<DbService> dbServiceLoader = ServiceLoader.load(DbService.class);
//		final DbService dbService = dbServiceLoader.iterator().next();
//		dbService.reload();
		
		
 		//DbServiceImpl ohne explizite Verwendung
		@SuppressWarnings("unchecked")
		final Class<? extends DbService> dbServiceClass = (Class<? extends DbService>) Class.forName(DB_SERVICE_IMPL);
		dbServiceClass.newInstance().reload();
 	}
 	
 	/**
 	 */
 	@Before
 	public void before() throws SystemException {   // NotSupportedException
 		// Arquillian mit Servlet-Protokoll: impliziter Start der Transaktion durch Seam Faces
 		assertThat(trans.getStatus(), is(STATUS_ACTIVE));
 
 		// Arquillian mit JMX-Protokoll: manueller Start der Transaktion erforderlich
 		//assertThat(trans.getStatus(), is(STATUS_NO_TRANSACTION));
 		//trans.begin();
 	}
 	
 	/**
 	 */
 	@After
 	public void after() {
 		//      throws SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
 		
 		// Arquillian mit JMX-Protokoll: manuelles Beenden der Transaktion erforderlich
 		//
 		//if (trans == null) {
 		//	return;
 		//}
 		//
 		//try {
 		//	switch (trans.getStatus()) {
 		//		case STATUS_ACTIVE:
 		//			trans.commit();
 		//			break;
 		//		    
 		//		case STATUS_MARKED_ROLLBACK:
 		//			trans.rollback();
 		//			break;
 	    //            
 	    //        default:
 	    //        	fail();
 	    //        	break;
 		//	}
 		//}
 		//catch (RollbackException e) {
 		//	// Commit ist fehlgeschlagen
 		//	final Throwable t = e.getCause();
 		//	// Gibt es "Caused by"
 		//	if (t instanceof ConstraintViolationException) {
 		//		// Es gibt Verletzungen bzgl. Bean Validation: auf der Console ausgeben
 		//		final ConstraintViolationException cve = (ConstraintViolationException) t;
 		//		final Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
 		//		for (ConstraintViolation<?> v : violations) {
 		//			System.err.println("!!! MESSAGE>>> " + v.getMessage());
 		//			System.err.println("!!! INVALID VALUE>>> " + v.getInvalidValue());
 		//			System.err.println("!!! ATTRIBUT>>> " + v.getPropertyPath());
 		//		}
 		//	}
 		//
 		//	throw new RuntimeException(e);
 		//}
 	}
 	
 	protected UserTransaction getUserTransaction() {
 		return trans;
 	}
 }
