 package com.anasoft.os.daofusion.test;
 
 import org.junit.runner.RunWith;
 import org.springframework.test.annotation.IfProfileValue;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Base class for JUnit / Spring TestContext framework
  * integration tests interacting with a database instance
  * via JPA / Hibernate.
  * 
  * <p>
  * 
  * This class represents a generic integration test
  * which is agnostic of the underlying database instance
  * in use, providing a basic JPA / Hibernate integration
  * test infrastructure to build on.
  * 
  * <p>
  * 
  * There are several configuration steps necessary
  * in order to make integration tests work properly:
  * 
  * <ol>
  *  <li>Following Spring bean properties need to be set
 *      up via the {@link PropertyOverrideConfigurer}
  *      according to the database instance in use:
  *      <ul>
  *          <li><tt>dataSource.driverClass</tt>
  *          <li><tt>dataSource.jdbcUrl</tt>
  *          <li><tt>dataSource.user</tt>
  *          <li><tt>dataSource.password</tt>
  *          <li><tt>entityManagerFactory.persistenceUnitName</tt>
  *      </ul>
  *  <li>Within the <tt>META-INF/persistence.xml</tt>
  *      file a dedicated integration test persistence
  *      unit of type <tt>RESOURCE_LOCAL</tt> needs to
  *      be defined for the target database instance.
  *      Name of this persistence unit must match the
  *      <tt>entityManagerFactory.persistenceUnitName</tt>
  *      bean property value. The <tt>hibernate.dialect</tt>
  *      property needs to be set up as well within
  *      the persistence unit.
  *  <li>Additional persistence unit configuration
  *      (e.g. defining entity mappings or setting
  *      the default schema name) should be done via
  *      the <tt>mapping-file</tt> element(s) within
  *      <tt>META-INF/persistence.xml</tt>.
  * </ol>
  * 
  * In case of in-memory integration tests, it is
  * recommended that the database instance setup is
  * done within the child Spring test context (test
  * contexts are automatically cached for all test
  * cases unless explicitly marked as dirty).
  * 
  * <p>
  * 
  * Specific integration test subclasses interacting
  * with local or remote databases (excluding in-memory
  * databases) should use the test profile functionality
  * via the {@link IfProfileValue} annotation. Following
  * profile values are applicable for the
  * {@link #PROFILE_DBTYPE_NAME dbType} profile property
  * name:
  * 
  * <ul>
  *  <li>{@link #PROFILE_DBTYPE_VALUE_LOCAL local}
  *  <li>{@link #PROFILE_DBTYPE_VALUE_REMOTE remote}
  *  <li>{@link #PROFILE_DBTYPE_VALUE_ALL all}
  *      (<tt>local</tt> and <tt>remote</tt>)
  * </ul>
  * 
  * For example:
  * 
  * <pre>
  * <tt>@</tt>ContextConfiguration(locations = { ... })
  * <tt>@</tt>IfProfileValue(name = BaseHibernateIntegrationTest.PROFILE_DBTYPE_NAME,
  *      values = { BaseHibernateIntegrationTest.PROFILE_DBTYPE_VALUE_ALL, BaseHibernateIntegrationTest.PROFILE_DBTYPE_VALUE_REMOTE })
  * public class CustomRemoteIntegrationTest extends BaseHibernateIntegrationTest {
  *      ...
  * }
  * </pre>
  * 
  * <p>
  * 
  * Note that all integration tests should follow the default
  * method-level rollback strategy, ensuring a proper test
  * data separation between multiple test runs.
  * 
  * @author vojtech.szocs
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class })
 @TransactionConfiguration(defaultRollback = true)
 @Transactional
 @ContextConfiguration(locations = { BaseHibernateIntegrationTest.CONTEXT_LOCATION })
 public abstract class BaseHibernateIntegrationTest {
 
    private static final String CONTEXT_LOCATION = "classpath:testContext-hibernate-baseIntegration.xml";
     
     public static final String PROFILE_DBTYPE_NAME = "dao.test.dbType";
     public static final String PROFILE_DBTYPE_VALUE_ALL = "all";
     public static final String PROFILE_DBTYPE_VALUE_LOCAL = "local";
     public static final String PROFILE_DBTYPE_VALUE_REMOTE = "remote";
     
 }
