 package smartpool.web.form;
 
 import org.joda.time.LocalTime;
 import org.junit.Test;
 import smartpool.domain.JoinRequest;
 
 import static org.junit.Assert.assertThat;
 import static smartpool.util.matchers.ReflectionMatcher.reflectionEquals;
 
 public class JoinRequestFormTest {
     @Test
     public void shouldCreateValidDomainObject() throws Exception {
        JoinRequest joinRequest = new JoinRequestForm("test.twu", "test carpool", "Diamond District, Gate 8", "08:30", "Diamond District", "+91 9999 999 999").createDomainObject();
        assertThat(joinRequest, reflectionEquals(new JoinRequest("test.twu", "test carpool", "Diamond District, Gate 8", new LocalTime(8, 30), null)));
     }
 
     @Test
     public void shouldCreateValidDomainObjectIfPreferredTimeHasNoZeroPadding() throws Exception {
        JoinRequest joinRequest = new JoinRequestForm("test.twu", "test carpool", "Diamond District, Gate 8", "8:30", "Diamond District", "+91 9999 999 999").createDomainObject();
        assertThat(joinRequest, reflectionEquals(new JoinRequest("test.twu", "test carpool", "Diamond District, Gate 8", new LocalTime(8, 30), null)));
     }
 }
