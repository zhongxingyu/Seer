 package de.flower.rmt.test;
 
 /**
  * @author flowerrrr
  */
 
 import ch.qos.logback.classic.spi.ILoggingEvent;
 import de.flower.common.test.mock.IListAppender;
 import de.flower.common.test.mock.LogBackListAppender;
 import de.flower.rmt.repository.IClubRepo;
 import de.flower.rmt.repository.ITeamRepo;
 import de.flower.rmt.repository.IUserRepo;
 import de.flower.rmt.service.*;
 import de.flower.rmt.service.geocoding.IGeocodingService;
 import org.apache.commons.lang3.Validate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.TestingAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolderStrategy;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Listeners;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.sql.DataSource;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author flowerrrr
  */
 @Listeners({de.flower.common.test.ExceptionLoggerTestListener.class})
 // in-memory database -> no need for transactional support and rollbacks
 //@TestExecutionListeners(Array(classOf[TransactionalTestExecutionListener]))
 // @Transactional
 @ContextConfiguration(locations = {"/applicationContext-test.xml"})
 @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
 public class AbstractIntegrationTests extends AbstractTestNGSpringContextTests {
 
     protected Logger log = LoggerFactory.getLogger(this.getClass());
 
     /**************************************************************************/
     /*                        Daos                                            */
 
     /**
      * **********************************************************************
      */
 
     @Autowired
     protected ITeamRepo teamRepo;
 
     @Autowired
     protected IClubRepo clubRepo;
 
     @Autowired
     protected IUserRepo userRepo;
 
     /**************************************************************************/
     /*                        Services                                        */
 
     /**
      * **********************************************************************
      */
 
     @Autowired
     protected ITeamManager teamManager;
 
     @Autowired
     protected IUserManager userManager;
 
     @Autowired
     protected IEventManager eventManager;
 
     @Autowired
     protected IPlayerManager playerManager;
 
     @Autowired
     protected IResponseManager responseManager;
 
     @Autowired
     protected UserDetailsService userDetailService;
 
     @Autowired
     protected IGeocodingService geocodingService;
 
     @Autowired
     protected SecurityContextHolderStrategy securityContextHolderStrategy;
 
     @PersistenceContext
     protected EntityManager em;
 
     @Autowired
     protected PlatformTransactionManager transactionManager;
 
     @Autowired
     protected DataSource dataSource;
 
     protected IListAppender<ILoggingEvent> listAppender;
 
     @Autowired
     protected TestData testData;
 
     private Database db;
 
     public static final String testUserName = "manager-rmt@mailinator.com";
 
     @BeforeClass
    protected void initClass() {
         listAppender = LogBackListAppender.configureListAppender();
         // use db-unit to refresh the the test data
         Map properties = new HashMap();
         properties.put("http://www.dbunit.org/properties/datatypeFactory", new org.dbunit.ext.h2.H2DataTypeFactory());
         db = new Database(properties);
         db.setDataSource(dataSource);
     }
 
     @BeforeMethod
    protected void initTest() throws Exception {
         try {
             // reset database
             resetTestdata();
             resetListAppender();
 
             initializeSecurityContextWithTestUser();
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw e;
         }
     }
 
     @AfterMethod
    protected void afterTest() throws Exception {
         try {
             // dump database to disk
             // full database export (cannot export users table: always a dbunit error wiht this table.)
             String[] tables = new String[]{"club", "team", "venue", "player", "event", "response", "invitation"};
             db.export("src/test/database/data/export.xml", tables);
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw e;
         }
     }
 
     /**
      * Initialize security context with test user.
      */
     protected void initializeSecurityContextWithTestUser() {
         // set the security context with a test user.
         Validate.notNull(userRepo.findByEmail(testUserName), "Make sure test user is present in test database.");
         UserDetails principal = userDetailService.loadUserByUsername(testUserName);
         Authentication authentication = new TestingAuthenticationToken(principal, principal.getPassword(), new ArrayList(principal.getAuthorities()));
         securityContextHolderStrategy.getContext().setAuthentication(authentication);
     }
 
     /**
      * Reset testdata. Most workflow unit tests are @NotTransactional annotated to have a more realistic transactional behaviour of the workflow-methods. That means we have to reset any test-data that
      * was modified during a testmethod.
      */
     private void resetTestdata() {
         log.info("Resetting test data in database.");
 
         db.deleteAll(Database.createDataSet("/data/test_data_delete.xml"));
         db.cleanInsert(Database.createDataSet("/data/test_data.xml"));
 
         testData.checkDataConsistency(em);
     }
 
     /**
      * Reset the internal buffer of the mock appender before each test method is started.
      */
     private void resetListAppender() {
         if (listAppender != null) {
             listAppender.reset();
         }
     }
 
     /**
      * Prints out "test started" message.
      * Requires junit 4.8 test runner that is able to process @Rule annotations.
      * Does not work with eclipse 3.5, which has junit 4.5 bundled.
      */
     @BeforeMethod(alwaysRun = true)
     public void testStarted(Method method) {
         log.info("***************************************************************");
         log.info("Test [" + method.getName() + "] started.");
         log.info("***************************************************************");
     }
 
     /**
      * Prints out "test finished" message.
      */
     @AfterMethod
     public void testFinished(Method method) {
         log.info("***************************************************************");
         log.info("Test [" + method.getName() + "()] finshed.");
         log.info("***************************************************************");
     }
 }
