 package org.maziarz.hbn;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 
 import org.hibernate.ejb.Ejb3Configuration;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Ignore
 public abstract class HibernateBaseTestUsingProgrammableConfig {
 
	private EntityManagerFactory emf;

 	protected EntityManager em;
 
 	private List<Class<?>> classes = new ArrayList<Class<?>>();
 	private List<String> packages = new ArrayList<String>();
 
 	protected EntityManager getEnitityManager() {
 		return em;
 	}
 
 	private static Logger logger = LoggerFactory.getLogger(HibernateBaseTestUsingProgrammableConfig.class);
 
 	private EntityManager createEntityManager(List<String> packages, List<Class<?>> annotatedClasses) {
 		Properties properties = new Properties();
 		properties.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
 		properties.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
 		properties.put("hibernate.connection.username", "om");
 		properties.put("hibernate.connection.password", "om");
 		properties.put("hibernate.connection.driver_class", "org.h2.Driver");
		properties.put("hibernate.connection.url", "jdbc:h2:db4tests");
 		properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
 		properties.put("hibernate.hbm2ddl.auto", "create");
 		properties.put("hibernate.show_sql", "false");
 		properties.put("hibernate.format_sql", "true");
 
 		@SuppressWarnings("deprecation")
 		Ejb3Configuration cfg = new Ejb3Configuration();
 		cfg.addProperties(properties);
 
 		for (String p : packages) {
 			cfg.addPackage(p);
 		}
 
 		for (Class<?> c : annotatedClasses) {
 			cfg.addAnnotatedClass(c);
 		}
 
 		EntityManagerFactory factory = cfg.buildEntityManagerFactory();
 		return factory.createEntityManager();
 	}
 
 	@Before
 	public void setUp() {
 		onInit();
 		logger.info("Model package: " + packages);
 		logger.info("Model classes: " + classes);
 		em = createEntityManager(packages, classes);
 	}
 
 	@After
 	public void tearDown() {
 		if (em != null) {
 			em.close();
 		}
 	}
 
 	@Test
 	public final void actualTest() throws Exception {
 		test();
 	}
 
 	protected void addClass(Class<?> c) {
 		classes.add(c);
 	}
 
 	protected void addClasses(Class<?> c, Class<?>... cs) {
 		addClass(c);
 		for (Class<?> csi : cs) {
 			addClass(csi);
 		}
 	}
 
 	/**
	 * Three methods are avialable
 	 * 
 	 * <ul>
 	 * <li>addClass(Class<?>)</li>
 	 * <li>addClasses(Class<?>, Class<?>...)</li>
 	 * </ul>
 	 */
 	protected abstract void onInit();
 
 	protected abstract void test();
 
 }
