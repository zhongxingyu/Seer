 package org.piraso.domain.service;
 
import com.piraso.core.carbonfive.Carbon5AbstractDataDrivenTest;
 import org.piraso.domain.vo.AccountVO;
 import org.piraso.domain.vo.AccountVOBuilder;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 import java.util.Arrays;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 
 /**
  * Test for {@link AccountManagementServiceImpl} class.
  */
 @Carbon5AbstractDataDrivenTest.DataSet(locations = {"dataset/account-data-set.xml"})
 @ContextConfiguration(locations = { "classpath:spring-domain-db-test.xml" })
 public class AccountManagementServiceImplTest extends Carbon5AbstractDataDrivenTest {
 
     @Autowired
     private AccountManagementService service;
 
     @Test
     public void testGetSessionAccount() throws Exception {
         assertThat(service.getSessionAccounts("session1"), hasSize(2));
         assertThat(service.getSessionAccounts("session2"), hasSize(1));
     }
 
     @Test
     public void testAddAccount() throws Exception {
         AccountVO account = new AccountVOBuilder()
                 .setName("name5")
                 .setDescription("desc5")
                 .setSessionID("session3")
                 .build();
 
         service.addAccount(account);
 
         assertThat(service.getSessionAccounts("session3"), hasSize(1));
 
         account = new AccountVOBuilder()
                 .setName("name6")
                 .setDescription("desc6")
                 .setSessionID("session3")
                 .build();
 
         service.addAccount(account);
 
         assertThat(service.getSessionAccounts("session3"), hasSize(2));
     }
 
     @Test
     public void testActivate() throws Exception {
         AccountVO account = new AccountVOBuilder()
                 .setName("activate1")
                 .setDescription("activate1 desc")
                 .setSessionID("activate")
                 .build();
 
         service.addAccount(account);
 
         account = new AccountVOBuilder()
                 .setName("activate2")
                 .setDescription("activate2 desc")
                 .setSessionID("activate")
                 .build();
 
         service.addAccount(account);
 
         assertThat(service.getSessionAccounts("activate"), hasSize(2));
 
         assertThat(service.activate("activate", Arrays.asList("activate1", "activate2")), is(2));
     }
 
     @Test
     public void testArchive() throws Exception {
         AccountVO account = new AccountVOBuilder()
                 .setName("archive1")
                 .setDescription("archive1 desc")
                 .setSessionID("archive")
                 .build();
 
         service.addAccount(account);
 
         account = new AccountVOBuilder()
                 .setName("archive2")
                 .setDescription("archive2 desc")
                 .setSessionID("archive")
                 .build();
 
         service.addAccount(account);
 
         assertThat(service.getSessionAccounts("archive"), hasSize(2));
 
         assertThat(service.archive("archive", Arrays.asList("archive1", "archive2")), is(2));
     }
 
     @Test
     public void testDelete() throws Exception {
         AccountVO account = new AccountVOBuilder()
                 .setName("delete1")
                 .setDescription("delete1 desc")
                 .setSessionID("delete")
                 .build();
 
         service.addAccount(account);
 
         account = new AccountVOBuilder()
                 .setName("delete2")
                 .setDescription("delete2 desc")
                 .setSessionID("delete")
                 .build();
 
         service.addAccount(account);
 
         assertThat(service.getSessionAccounts("delete"), hasSize(2));
 
         assertThat(service.delete("delete", Arrays.asList("delete1", "delete2")), is(2));
 
         assertThat(service.getSessionAccounts("delete"), hasSize(0));
     }
 }
