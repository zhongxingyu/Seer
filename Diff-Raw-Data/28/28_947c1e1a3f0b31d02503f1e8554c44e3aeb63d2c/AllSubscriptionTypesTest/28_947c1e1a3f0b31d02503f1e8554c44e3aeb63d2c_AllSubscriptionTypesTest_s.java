 package org.motechproject.ghana.mtn.repository;
 
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbPath;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
import org.junit.runner.RunWith;
 import org.motechproject.ghana.mtn.BaseIntegrationTest;
 import org.motechproject.ghana.mtn.domain.SubscriptionType;
 import org.motechproject.ghana.mtn.domain.builder.SubscriptionTypeBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
@Ignore
 public class AllSubscriptionTypesTest extends BaseIntegrationTest {
 
     @Autowired
     private AllSubscriptionTypes allSubscriptionTypes;
 
     @Before
     public void setUp() {
        String databaseName = dbConnector.getDatabaseName();
        if (couchDbInstance.checkIfDbExists(new DbPath(databaseName)))
            couchDbInstance.deleteDatabase(databaseName);
        couchDbInstance.createDatabase(databaseName);
         createDefaultValues();
     }
 
     private void createDefaultValues() {
         SubscriptionType pregnancySubscriptionType = new SubscriptionTypeBuilder()
                 .withMinWeek(5)
                 .withMaxWeek(35)
                 .withProgramName("Pregnancy")
                 .withShortCode("P").build();
         SubscriptionType childCareSubscriptionType = new SubscriptionTypeBuilder()
                 .withMinWeek(1)
                 .withMaxWeek(52)
                 .withProgramName("Child Care")
                 .withShortCode("C").build();
 
         allSubscriptionTypes.add(pregnancySubscriptionType);
         allSubscriptionTypes.add(childCareSubscriptionType);
 
     }
 
     @Test
     public void ShouldReturnPregnancySubscriptionTypeForShortCodeP() {
         SubscriptionType subscriptionType = allSubscriptionTypes.findByCampaignShortCode("P");
         assertThat(subscriptionType.getProgramName(), is("Pregnancy"));
     }
 
     @After
     public void destroy() {
        couchDbInstance.deleteDatabase(dbConnector.getDatabaseName());
     }
 }
