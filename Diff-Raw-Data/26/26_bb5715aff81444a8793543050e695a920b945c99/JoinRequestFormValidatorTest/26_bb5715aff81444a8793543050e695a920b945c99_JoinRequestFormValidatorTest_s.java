 package smartpool.web.form;
 
 import edu.yale.its.tp.cas.client.filter.CASFilter;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.validation.BindException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class JoinRequestFormValidatorTest {
 
     private JoinRequestFormValidator validator;
 
     @Before
     public void setUp() throws Exception {
         validator = new JoinRequestFormValidator();
         HttpSession stubSession = mock(HttpSession.class);
         when(stubSession.getAttribute(CASFilter.CAS_FILTER_USER)).thenReturn("twu.test");
         HttpServletRequest stubRequest = mock(HttpServletRequest.class);
         when(stubRequest.getSession()).thenReturn(stubSession);
     }
 
     @Test
     public void shouldAddErrorIfPreferredTimeIsNotProvided() throws Exception {
        JoinRequestForm form = new JoinRequestForm("twu.test", "test pool", "DD", "", null, null);
 
         BindException errors = new BindException(form, "joinRequestForm");
 
         validator.validate(form, errors);
 
         assertThat(errors.hasErrors(), is(true));
         assertThat(errors.getAllErrors().size(), is(1));
         assertThat(errors.getFieldError("preferredPickupTime").getCode(),is("field.required"));
     }
 
     @Test
     public void shouldAddErrorIfPreferredTimeIsNotInCorrectFormat() throws Exception {
        JoinRequestForm form = new JoinRequestForm("twu.test", "test pool", "DD", "abcd", null, null);
         BindException errors = new BindException(form, "joinRequestForm");
         validator.validate(form,errors);
 
         assertThat(errors.hasErrors(), is(true));
         assertThat(errors.getAllErrors().size(), is(1));
         assertThat(errors.getFieldError("preferredPickupTime").getCode(), is("field.invalid"));
     }
 }
