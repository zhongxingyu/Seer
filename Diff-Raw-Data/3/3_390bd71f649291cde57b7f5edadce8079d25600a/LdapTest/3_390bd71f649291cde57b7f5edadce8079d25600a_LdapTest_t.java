 package eu.ydp.ldapgroups.ldap;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.inject.Inject;
 import java.util.Set;
 
 import static org.hamcrest.Matchers.containsInAnyOrder;
 import static org.hamcrest.Matchers.equalToIgnoringCase;
 import static org.junit.Assert.assertThat;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class LdapTest {
     @Inject
     Ldap ldap;
 
     @Test
     public void testGetMembersShouldReturnLogins() throws Exception {
         Set<String> members = ldap.getMembers("soft-tools@ydp.eu");
 
         assertThat(members, containsInAnyOrder(
                 equalToIgnoringCase("czawadka"),
                 equalToIgnoringCase("bdymowski"),
                 equalToIgnoringCase("akomuda"),
                equalToIgnoringCase("pwalas"),
                equalToIgnoringCase("mduch")
         ));
     }
 
 }
