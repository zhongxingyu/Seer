 package org.openengsb.openticket.ui.web;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
import java.util.Collections;
import java.util.List;
 
 import org.apache.wicket.spring.test.ApplicationContextMock;
 import org.apache.wicket.util.tester.WicketTester;
 import org.junit.Before;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.openengsb.core.api.context.ContextCurrentService;
import org.osgi.framework.ServiceReference;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 
 public abstract class PageTest {
     protected WicketTester tester;
     private ContextCurrentService contextService;
     protected ApplicationContextMock appContext;
 
     @Before
     public void baseSetup() {
         appContext = new ApplicationContextMock();
         
         mockAuthentication();
 
         contextService = mock(ContextCurrentService.class);
         when(contextService.getAvailableContexts()).thenReturn(Arrays.asList(new String[]{ "foo", "bar" }));
         appContext.putBean(contextService);
     }
 
     private void mockAuthentication() {
         AuthenticationManager authManager = mock(AuthenticationManager.class);
         final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
         authorities.add(new GrantedAuthorityImpl("LOGIN"));
         when(authManager.authenticate(any(Authentication.class))).thenAnswer(new Answer<Authentication>() {
             @Override
             public Authentication answer(InvocationOnMock invocation) {
                 Authentication auth = (Authentication) invocation.getArguments()[0];
                 if (auth.getCredentials().equals("password")) {
                     return new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
                         authorities);
                 }
                 throw new BadCredentialsException("wrong password");
             }
         });
         appContext.putBean("authenticationManager", authManager);
     }
 }
