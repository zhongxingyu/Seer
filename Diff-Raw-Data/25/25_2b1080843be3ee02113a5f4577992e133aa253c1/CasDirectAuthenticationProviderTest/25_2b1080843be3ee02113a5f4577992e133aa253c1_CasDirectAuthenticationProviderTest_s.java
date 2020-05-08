 package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;
 
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
 import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
 import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
 import org.acegisecurity.Authentication;
 import org.acegisecurity.AuthenticationException;
 import org.acegisecurity.AuthenticationServiceException;
 import org.acegisecurity.BadCredentialsException;
 import org.acegisecurity.GrantedAuthority;
 import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.easymock.IExpectationSetters;
 
 import java.io.IOException;
 import java.util.Map;
 
 import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertContains;
 import static org.easymock.EasyMock.expect;
 
 /**
  * @author Rhett Sutphin
  */
 public class CasDirectAuthenticationProviderTest extends AuthenticationTestCase {
     private static final String USERNAME = "joe";
     private static final String PASSWORD = "eoj";
 
     private CasDirectAuthenticationProvider provider;
     private DirectLoginHttpFacade loginFacade;
     private PscUserDetailsService userDetailsService;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         loginFacade = registerMockFor(DirectLoginHttpFacade.class);
         userDetailsService = registerMockFor(PscUserDetailsService.class);
 
         provider = new CasDirectAuthenticationProvider() {
             @Override
             protected DirectLoginHttpFacade createLoginFacade() {
                 return loginFacade;
             }
         };
         provider.setUserDetailsService(userDetailsService);
     }
 
     public void testSupportsDirectUsernamePasswordTokens() throws Exception {
         assertTrue(provider.supports(CasDirectUsernamePasswordAuthenticationToken.class));
     }
 
     public void testDoesNotSupportOtherTokens() throws Exception {
         assertFalse("Should not support UPAT", provider.supports(UsernamePasswordAuthenticationToken.class));
     }
 
     public void testAuthenticatePassesOnUnsupportedToken() throws Exception {
         replayMocks();
         assertNull(provider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD)));
         verifyMocks();
     }
 
     public void testSkipsAuthenticationIfAlreadyAuthenticated() throws Exception {
         Authentication alreadyAuthed = new CasDirectUsernamePasswordAuthenticationToken(
             USERNAME, PASSWORD, new GrantedAuthority[] { PscRole.SYSTEM_ADMINISTRATOR });
         replayMocks();
         assertSame(alreadyAuthed, provider.authenticate(alreadyAuthed));
         verifyMocks();
     }
 
     public void testBadLoginFormResultsInNoAuthentication() throws Exception {
         expect(loginFacade.getForm()).andThrow(new CasDirectException("Bad form"));
 
         try {
             doAuthenticate();
             fail("Exception not thrown");
         } catch (AuthenticationException ae) {
             assertContains("Wrong exception message", ae.getMessage(), "Direct CAS login failed");
             assertContains("Wrong exception message", ae.getMessage(), "Bad form");
         }
     }
 
     public void testUnavailableLoginFormResultsInNoAuthentication() throws Exception {
         expect(loginFacade.getForm()).andThrow(new IOException("Bad connection"));
 
         try {
             doAuthenticate();
             fail("Exception not thrown");
         } catch (AuthenticationException ae) {
             assertContains("Wrong exception message", ae.getMessage(), "Direct CAS login failed");
             assertContains("Wrong exception message", ae.getMessage(), "Bad connection");
         }
     }
 
     public void testFailedPostResultsInNoAuthentication() throws Exception {
         expect(loginFacade.getForm()).andReturn("<input name='lt' value='the-ticket'/>");
         expectPostCredentials(USERNAME, PASSWORD, "the-ticket").andReturn(false);
 
         try {
             doAuthenticate();
             fail("Exception not thrown");
         } catch (BadCredentialsException bce) {
             assertEquals("Credentials are invalid according to direct CAS login", bce.getMessage());
         }
     }
 
     public void testErrorInPostResultsInNoAuthentication() throws Exception {
         expect(loginFacade.getForm()).andReturn("<input name='lt' value='the-ticket'/>");
         String username = USERNAME;
         String password = PASSWORD;
         String ticket = "the-ticket";
         expectPostCredentials(username, password, ticket).
             andThrow(new IOException("Bad post"));
 
         try {
             doAuthenticate();
             fail("Exception not thrown");
         } catch (AuthenticationServiceException ase) {
             assertContains(ase.getMessage(), "Bad post");
             assertContains(ase.getMessage(), "Direct CAS login failed");
         }
     }
 
     public void testSuccessfulPostResultsInGoodAuthentication() throws Exception {
         expect(loginFacade.getForm()).andReturn("<input name='lt' value='some-ticket'/>");
         expectPostCredentials(USERNAME, PASSWORD, "some-ticket").andReturn(true);
         expect(userDetailsService.loadUserByUsername(USERNAME)).
            andReturn(AuthorizationObjectFactory.createPscUser(USERNAME, PscRole.STUDY_QA_MANAGER));
 
         Authentication actual = doAuthenticate();
         assertNotNull("No authentication token returned", actual);
         assertTrue("Actual token not authenticated", actual.isAuthenticated());
        assertEquals("Actual token not for correct user", USERNAME, actual.getPrincipal());
         assertEquals("Password not removed from token",
             "[REMOVED PASSWORD]", actual.getCredentials());
         assertEquals("Wrong authorities in actual token", 1, actual.getAuthorities().length);
         assertEquals("Wrong authorities in actual token",
             PscRole.STUDY_QA_MANAGER, actual.getAuthorities()[0]);
     }
     
     public void testNoSuchUserResultsInNoAuthentication() throws Exception {
         expect(loginFacade.getForm()).andReturn("<input name='lt' value='some-ticket'/>");
         expectPostCredentials(USERNAME, PASSWORD, "some-ticket").andReturn(true);
         expect(userDetailsService.loadUserByUsername(USERNAME)).
             andThrow(new UsernameNotFoundException(USERNAME));
 
         try {
             doAuthenticate();
             fail("Exception not thrown");
         } catch (BadCredentialsException bce) {
             assertContains(bce.getMessage(), USERNAME);
         }
     }
 
     private IExpectationSetters<Boolean> expectPostCredentials(String username, String password, String ticket) throws IOException {
         Map<String, String> parameters = new MapBuilder<String, String>().
             put("username", username).put("password", password).put("lt", ticket).
             toMap();
         return expect(loginFacade.postCredentials(parameters));
     }
 
     private Authentication doAuthenticate() {
         replayMocks();
         Authentication actual = provider.authenticate(
             new CasDirectUsernamePasswordAuthenticationToken(USERNAME, PASSWORD));
         verifyMocks();
         return actual;
     }
 }
