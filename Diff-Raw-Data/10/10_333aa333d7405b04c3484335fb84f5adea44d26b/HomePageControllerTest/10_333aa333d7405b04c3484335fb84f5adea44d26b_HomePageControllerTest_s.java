 package smartpool.web;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.ui.ModelMap;
 import smartpool.domain.LDAPResultSet;
 import smartpool.service.BuddyService;
 import smartpool.service.LDAPService;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class HomePageControllerTest {
 
     @Mock
     private LDAPService ldapService;
     @Mock
     private BuddyService buddyService;
     @Mock
     private HttpServletRequest request;
     @Mock
     private HttpSession session;
     private HomePageController homePageController;
 
     @Before
     public void setUp() {
         initMocks(this);
        homePageController = new HomePageController(ldapService, buddyService);
     }
     
     @Test
     public void shouldSetLDAPUserNameInSession_OnRequestingToShowTheHomePage() {
         when(buddyService.getUserNameFromCAS(request)).thenReturn("mzhao");
         when(ldapService.searchByUserName("mzhao")).thenReturn(new LDAPResultSet("Ming Zhao", "mzhao@thoughtworks.com"));
         when(request.getSession()).thenReturn(session);
 
         homePageController.index(request, new ModelMap());
 
         verify(session).setAttribute("ldapUserName", "Ming Zhao");
     }
 }
