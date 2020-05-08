 package br.com.caelum.vraptor.vaas;
 
 import static br.com.caelum.vraptor.vaas.AuthenticationAuthorizationFlow.LOGIN_URL_PARAMETER;
 import static br.com.caelum.vraptor.vaas.AuthenticationAuthorizationFlow.LOGOUT_URL_PARAMETER;
 import static java.util.Arrays.asList;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 
 import javax.enterprise.event.Event;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import br.com.caelum.vraptor.vaas.authentication.Authenticator;
 import br.com.caelum.vraptor.vaas.authentication.VaasPrincipalSession;
 import br.com.caelum.vraptor.vaas.authorization.PermissionVerifier;
 import br.com.caelum.vraptor.vaas.event.AuthorizationFailedEvent;
 import br.com.caelum.vraptor.vaas.event.RefreshUserEvent;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AuthenticationAuthorizationFlowTest {
 
 	@Mock private ServletContext context;
 	@Mock private HttpServletRequest request;
 	@Mock private Authenticator auth;
 	@Mock private PermissionVerifier permissions;
 	@Mock private VaasPrincipalSession principalSession;
 	@Mock private Event<AuthorizationFailedEvent> authorizationFailed;
 	@Mock private Event<RefreshUserEvent> refreshUserEvent;
 
 
 	private AuthenticationAuthorizationFlow flow;
 	private static final String LOGIN_URI = "login_uri";
 	private static final String LOGOUT_URI = "logout_uri";
 	private static final String ALLOWED_URI = "allowed_uri";
 	private static final String NOT_ALLOWED_URI = "not_allowed_uri";
 	
 	@Before
 	public void setUp(){
 		when(context.getContextPath()).thenReturn("");
 		when(context.getInitParameter(LOGIN_URL_PARAMETER)).thenReturn(LOGIN_URI);
 		when(context.getInitParameter(LOGOUT_URL_PARAMETER)).thenReturn(LOGOUT_URI);
 		when(permissions.verifyAccessFor(ALLOWED_URI)).thenReturn(new ArrayList<Rule>());
 		Rule someRule = mock(Rule.class);
 		when(permissions.verifyAccessFor(NOT_ALLOWED_URI)).thenReturn(asList(someRule));
 		when(principalSession.isLogged()).thenReturn(false);
 		
		flow = new AuthenticationAuthorizationFlow(permissions, auth, context, request, refreshUserEvent, principalSession, authorizationFailed, null, null);
 		flow.config();
 	}
 	
 	@Test
 	public void shouldTryToLoginIfIsLoginUrl() {
 		when(request.getRequestURI()).thenReturn(LOGIN_URI);
 
 		flow.intercept(null);
 		
 		verify(auth).tryToLogin();
 		verify(auth, never()).tryToLogout();
 	}
 	
 	@Test
 	public void shouldTryToLogoutIfIsLogoutUrl() {
 		when(request.getRequestURI()).thenReturn(LOGOUT_URI);
 		
 		flow.intercept(null);
 		
 		verify(auth).tryToLogout();
 		verify(auth, never()).tryToLogin();
 	}
 	
 	@Test
 	public void shouldContinueTheFrameworkFlowIfUserIsAllowedToAccess() {
 		when(request.getRequestURI()).thenReturn(ALLOWED_URI);
 		Runnable fakeFrameworkFlow = mock(Runnable.class);
 		
 		flow.intercept(fakeFrameworkFlow);
 
 		verify(fakeFrameworkFlow).run();
 	}
 	
 	@Test
 	public void shouldNotContinueTheFrameworkFlowIfUserIsNotAllowedToAccess() {
 		when(request.getRequestURI()).thenReturn(NOT_ALLOWED_URI);
 		Runnable fakeFrameworkFlow = mock(Runnable.class);
 		
 		flow.intercept(fakeFrameworkFlow);
 		
 		verify(fakeFrameworkFlow, never()).run();
 	}
 	
 	@Test
 	public void shouldFireAuthorizationFailedEventIfUserIsNotAllowedToAccess() {
 		when(request.getRequestURI()).thenReturn(NOT_ALLOWED_URI);
 		
 		flow.intercept(null);
 		
 		verify(authorizationFailed).fire(Mockito.any(AuthorizationFailedEvent.class));
 	}
 	
 	@Test
 	public void shouldRefreshUser() {
 		when(request.getRequestURI()).thenReturn(NOT_ALLOWED_URI);
 		when(principalSession.isLogged()).thenReturn(true);
 		
 		flow.intercept(null);
 		
 		verify(refreshUserEvent).fire(Mockito.any(RefreshUserEvent.class));
 	}
 	
 	@Test
 	public void shouldNotRefreshUser() {
 		when(request.getRequestURI()).thenReturn(NOT_ALLOWED_URI);
 		when(principalSession.isLogged()).thenReturn(false);
 		
 		flow.intercept(null);
 		
 		verify(refreshUserEvent, never()).fire(Mockito.any(RefreshUserEvent.class));
 	}
 
 }
