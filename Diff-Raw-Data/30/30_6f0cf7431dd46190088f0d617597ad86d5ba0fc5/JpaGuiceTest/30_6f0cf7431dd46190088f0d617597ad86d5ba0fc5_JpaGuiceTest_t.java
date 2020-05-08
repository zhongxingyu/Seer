 /**
  * 
  */
 package com.chinarewards.qqgbpvn.main.test;
 
 import javax.persistence.EntityManager;
 
 import org.junit.After;
 import org.junit.Before;
 
 import com.google.inject.Provider;
 import com.google.inject.persist.PersistService;
 
 /**
  * 
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public abstract class JpaGuiceTest extends GuiceTest {
 
 	protected Provider<EntityManager> emp;
 
 	protected EntityManager getEm() {
 		return emp.get();
 	}
 
 	/**
	 * Returns whether the persistence service should be started by this class,
	 * or the client shall do it. If subclass managed their own start/stop of
	 * Guice's persistence service, please override this method and return
	 * <code>false</code>.
	 * <p>
	 * 
	 * Default value is <code>true</code>
	 * 
	 * @return
	 */
	protected boolean isTestManagedJpaPersistence() {
		return true;
	}

	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 
 		PersistService ps = getInjector().getInstance(PersistService.class);
 		ps.start();
 
 		emp = getInjector().getProvider(EntityManager.class);
 
 		getEm().getTransaction().begin();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 		if (getEm().getTransaction().isActive()) {
 			getEm().getTransaction().rollback();
 		}
 	}
 
 }
