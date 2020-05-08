 package com.jtbdevelopment.e_eye_o.hibernate.DAO;
 
 import org.hibernate.SessionFactory;
 import org.jmock.Mockery;
 import org.springframework.orm.hibernate4.HibernateTransactionManager;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
 import org.springframework.transaction.support.AbstractPlatformTransactionManager;
 import org.testng.annotations.Test;
 
 import javax.sql.DataSource;
 import java.util.Properties;
 
 import static org.testng.AssertJUnit.*;
 
 /**
  * Date: 1/6/13
  * Time: 9:28 PM
  */
 public class HibernateDAOSpringConfigTest {
     private final HibernateDAOSpringConfig config = new HibernateDAOSpringConfig();
     private final Mockery context = new Mockery();
     private final SessionFactory sessionFactory = context.mock(SessionFactory.class);
     private final DataSource dataSource = context.mock(DataSource.class);
 
     @Test
     public void testHibernateOverrideProperties() throws Exception {
         Properties props = config.hibernateOverrideProperties();
         assertTrue(props.isEmpty());
     }
 
     @Test
     public void testTimestampIntercepter() {
         assertTrue(config.timestampInterceptor() instanceof ModificationTimestampGenerator);
     }
 
     @Test
     public void testHibernatePropertiesNoOverrides() throws Exception {
         Properties overrides = new Properties();
         final String mydialect = "MYDIALECT";
         Properties props = config.hibernateProperties(mydialect, overrides);
         assertEquals(4, props.size());
         assertEquals(mydialect, props.get("hibernate.dialect"));
         assertEquals("true", props.getProperty("hibernate.show_sql"));
         assertEquals("false", props.getProperty("hibernate.format_sql"));
        assertEquals("validate", props.getProperty("hibernate.hbm2ddl.auto"));
 
     }
 
     @Test
     public void testHibernatePropertiesWithOverrides() throws Exception {
         Properties overrides = new Properties();
         overrides.put("additional", "value");
         overrides.put("hibernate.hbm2ddl.auto", "update");
         final String mydialect = "MYDIALECT";
         Properties props = config.hibernateProperties(mydialect, overrides);
         assertEquals(5, props.size());
         assertEquals(mydialect, props.get("hibernate.dialect"));
         assertEquals("true", props.getProperty("hibernate.show_sql"));
         assertEquals("false", props.getProperty("hibernate.format_sql"));
         assertEquals("update", props.getProperty("hibernate.hbm2ddl.auto"));
         assertEquals("value", props.getProperty("additional"));
     }
 
     @Test
     public void testSessionFactory() throws Exception {
         Properties props = new Properties();
         LocalSessionFactoryBean sf = config.sessionFactory(dataSource, props, null);
         assertSame(props, sf.getHibernateProperties());
         //  Can't currently verify packages to scan or data source
     }
 
     @Test
     public void testTransactionManager() throws Exception {
         AbstractPlatformTransactionManager mgr = config.transactionManager(sessionFactory);
         assertTrue(mgr instanceof HibernateTransactionManager);
     }
 }
