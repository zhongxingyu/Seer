 package net.sf.jguard.core.authentication.callbackhandler;
 
 import com.google.common.collect.Lists;
 import net.sf.jguard.core.authentication.callbacks.AsynchronousCallbackException;
 import net.sf.jguard.core.authentication.callbacks.AuthenticationSchemeHandlerCallback;
 import net.sf.jguard.core.authentication.schemes.AuthenticationSchemeHandler;
 import net.sf.jguard.core.authentication.schemes.DummyAuthenticationSchemeHandler;
 import net.sf.jguard.core.lifecycle.MockRequest;
 import net.sf.jguard.core.lifecycle.MockRequestAdapter;
 import net.sf.jguard.core.lifecycle.MockResponse;
 import net.sf.jguard.core.lifecycle.MockResponseAdapter;
 import org.junit.Test;
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 
 public class JGuardCallbackHandlerTest {
 
     public static final String DUMMY_PROMPT = "dummyPrompt";
 
     @Test
     public void testHandle_populate_authenticationSchemeHandlerCallback_with_theAuthenticationSchemeHandler_name() throws Exception {
         //given
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter> authenticationSchemeHandler = new DummyAuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>();
         authenticationSchemeHandlers.add(authenticationSchemeHandler);
         MockCallbackHandler mockCallbackHandler = new MockCallbackHandler(new MockRequestAdapter(new MockRequest()), new MockResponseAdapter(new MockResponse()), authenticationSchemeHandlers
         );
         AuthenticationSchemeHandlerCallback authenticationSchemeHandlerCallback = new AuthenticationSchemeHandlerCallback();
         List<Callback> callbackList = new ArrayList<Callback>();
         callbackList.add(authenticationSchemeHandlerCallback);
         Callback[] callbacks = callbackList.toArray(new Callback[callbackList.size()]);
         //when
         mockCallbackHandler.handle(callbacks);
 
         //then
         assertThat(authenticationSchemeHandlerCallback.getAuthenticationSchemeHandlerName(), is(DummyAuthenticationSchemeHandler.MOCK_AUTHENTICATION_SCHEME_HANDLER_NAME));
     }
 
 
     @Test(expected = IllegalArgumentException.class)
     public void testHandle_throw_IllegalArgumentException_when_authenticationSchemeHandlerList_is_empty() throws Exception {
         //given
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         new MockCallbackHandler(new MockRequestAdapter(new MockRequest()), new MockResponseAdapter(new MockResponse()), authenticationSchemeHandlers
         );
 
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testHandle_throw_IllegalArgumentException_when_authenticationSchemeHandlerList_is_null() throws Exception {
         //given
         new MockCallbackHandler(new MockRequestAdapter(new MockRequest()), new MockResponseAdapter(new MockResponse()), null);
 
     }
 
 
     @Test
     public void test_handle_implies_call_to_authenticationSchemeHandler_when_authentication_succeed() throws Exception {
         //given
         MockRequestAdapter request = new MockRequestAdapter(new MockRequest());
         MockResponseAdapter response = new MockResponseAdapter(new MockResponse());
 
         AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter> authenticationSchemeHandler = mock(AuthenticationSchemeHandler.class);
         Collection<Class<? extends Callback>> callbackClasses = Lists.newArrayList();
         callbackClasses.add(NameCallback.class);
         when(authenticationSchemeHandler.getCallbackTypes()).thenReturn(callbackClasses);
 
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         authenticationSchemeHandlers.add(authenticationSchemeHandler);
 
        MockCallbackHandler mockCallbackHandler = new MockCallbackHandler(request, response, authenticationSchemeHandlers
         );
         List<Callback> callbackList = new ArrayList<Callback>();
         NameCallback nameCallback = new NameCallback(DUMMY_PROMPT);
         callbackList.add(nameCallback);
         Callback[] callbacks = callbackList.toArray(new Callback[callbackList.size()]);
 
         //when
         mockCallbackHandler.handle(callbacks);
         Subject subject = new Subject();
         mockCallbackHandler.authenticationSucceed(subject);
         //then
         verify(authenticationSchemeHandler).authenticationSucceed(subject, request, response);
     }
 
     @Test
     public void test_handle_implies_call_to_authenticationSchemeHandler_when_authentication_failed() throws Exception {
         //given
         MockRequestAdapter request = new MockRequestAdapter(new MockRequest());
         MockResponseAdapter response = new MockResponseAdapter(new MockResponse());
 
         AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter> authenticationSchemeHandler = mock(AuthenticationSchemeHandler.class);
         Collection<Class<? extends Callback>> callbackClasses = Lists.newArrayList();
         callbackClasses.add(NameCallback.class);
         when(authenticationSchemeHandler.getCallbackTypes()).thenReturn(callbackClasses);
 
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         authenticationSchemeHandlers.add(authenticationSchemeHandler);
 
         MockCallbackHandler mockCallbackHandler = new MockCallbackHandler(request, response, authenticationSchemeHandlers);
         List<Callback> callbackList = new ArrayList<Callback>();
         NameCallback nameCallback = new NameCallback(DUMMY_PROMPT);
         callbackList.add(nameCallback);
         Callback[] callbacks = callbackList.toArray(new Callback[callbackList.size()]);
 
         //when
         mockCallbackHandler.handle(callbacks);
         Subject subject = new Subject();
         mockCallbackHandler.authenticationFailed();
         //then
         verify(authenticationSchemeHandler).authenticationFailed(request, response);
     }
 
     @Test
     public void test_handle_answer_to_challenge_false_and_authentication_needed_implies_build_challenge() throws Exception {
         //given
         MockRequestAdapter request = new MockRequestAdapter(new MockRequest());
         MockResponseAdapter response = new MockResponseAdapter(new MockResponse());
 
         AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter> authenticationSchemeHandler = mock(AuthenticationSchemeHandler.class);
         when(authenticationSchemeHandler.answerToChallenge(request, response)).thenReturn(false);
         when(authenticationSchemeHandler.challengeNeeded(request, response)).thenReturn(true);
         Collection<Class<? extends Callback>> callbackClasses = Lists.newArrayList();
         callbackClasses.add(NameCallback.class);
         when(authenticationSchemeHandler.getCallbackTypes()).thenReturn(callbackClasses);
 
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         authenticationSchemeHandlers.add(authenticationSchemeHandler);
 
         MockCallbackHandler mockCallbackHandler = new MockCallbackHandler(request, response, authenticationSchemeHandlers);
         List<Callback> callbackList = new ArrayList<Callback>();
         NameCallback nameCallback = new NameCallback(DUMMY_PROMPT);
         callbackList.add(nameCallback);
         Callback[] callbacks = callbackList.toArray(new Callback[callbackList.size()]);
 
         //when
         mockCallbackHandler.handle(callbacks);
         //then
         verify(authenticationSchemeHandler).buildChallenge(request, response);
     }
 
     @Test
     public void test_handle_answer_to_challenge_false_and_authentication_not_needed_does_not_imply_build_challenge() throws Exception {
         //given
         MockRequestAdapter request = new MockRequestAdapter(new MockRequest());
         MockResponseAdapter response = new MockResponseAdapter(new MockResponse());
 
         AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter> authenticationSchemeHandler = mock(AuthenticationSchemeHandler.class);
         when(authenticationSchemeHandler.answerToChallenge(request, response)).thenReturn(false);
         when(authenticationSchemeHandler.challengeNeeded(request, response)).thenReturn(false);
         Collection<Class<? extends Callback>> callbackClasses = Lists.newArrayList();
         callbackClasses.add(NameCallback.class);
         when(authenticationSchemeHandler.getCallbackTypes()).thenReturn(callbackClasses);
 
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         authenticationSchemeHandlers.add(authenticationSchemeHandler);
 
         MockCallbackHandler mockCallbackHandler = new MockCallbackHandler(request, response, authenticationSchemeHandlers);
         List<Callback> callbackList = new ArrayList<Callback>();
         NameCallback nameCallback = new NameCallback(DUMMY_PROMPT);
         callbackList.add(nameCallback);
         Callback[] callbacks = callbackList.toArray(new Callback[callbackList.size()]);
 
         //when
         mockCallbackHandler.handle(callbacks);
         //then
         verify(authenticationSchemeHandler, never()).buildChallenge(request, response);
     }
 
 
     @Test
     public void test_handle_does_not_imply_authentication_when_no_authenticationSchemeHandler_match_callback_classes_requirements() throws Exception {
         //given
         MockRequestAdapter request = new MockRequestAdapter(new MockRequest());
         MockResponseAdapter response = new MockResponseAdapter(new MockResponse());
 
         AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter> authenticationSchemeHandler = mock(AuthenticationSchemeHandler.class);
         Collection<Class<? extends Callback>> callbackClasses = Lists.newArrayList();
         callbackClasses.add(PasswordCallback.class);
         when(authenticationSchemeHandler.getCallbackTypes()).thenReturn(callbackClasses);
 
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         authenticationSchemeHandlers.add(authenticationSchemeHandler);
 
         MockCallbackHandler mockCallbackHandler = new MockCallbackHandler(request, response, authenticationSchemeHandlers);
         List<Callback> callbackList = new ArrayList<Callback>();
         NameCallback nameCallback = new NameCallback(DUMMY_PROMPT);
         callbackList.add(nameCallback);
         Callback[] callbacks = callbackList.toArray(new Callback[callbackList.size()]);
 
         //when
         mockCallbackHandler.handle(callbacks);
         Subject subject = new Subject();
         mockCallbackHandler.authenticationFailed();
         //then
         verify(authenticationSchemeHandler, never()).authenticationFailed(request, response);
     }
 
     @Test
     public void test_handle_does_not_imply_authenticationschemeHandler_call_when_no_callback_classes_are_present() throws Exception {
         //given
         MockRequestAdapter request = new MockRequestAdapter(new MockRequest());
         MockResponseAdapter response = new MockResponseAdapter(new MockResponse());
 
         AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter> authenticationSchemeHandler = mock(AuthenticationSchemeHandler.class);
         Collection<Class<? extends Callback>> callbackClasses = Lists.newArrayList();
         callbackClasses.add(PasswordCallback.class);
         when(authenticationSchemeHandler.getCallbackTypes()).thenReturn(callbackClasses);
 
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         authenticationSchemeHandlers.add(authenticationSchemeHandler);
 
         MockCallbackHandler mockCallbackHandler = new MockCallbackHandler(request, response, authenticationSchemeHandlers);
         List<Callback> callbackList = new ArrayList<Callback>();
         Callback[] callbacks = callbackList.toArray(new Callback[callbackList.size()]);
 
         //when
         mockCallbackHandler.handle(callbacks);
         Subject subject = new Subject();
         mockCallbackHandler.authenticationFailed();
         //then
         verify(authenticationSchemeHandler, never()).authenticationFailed(request, response);
     }
 
 
     @Test(expected = AsynchronousCallbackException.class)
     public void test_AsynchronousCallbackException_thrown_when_challenge_needed_and_asynchronous() throws UnsupportedCallbackException, IOException {
         //given
         MockRequestAdapter request = new MockRequestAdapter(new MockRequest());
         MockResponseAdapter response = new MockResponseAdapter(new MockResponse());
 
         AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter> authenticationSchemeHandler = mock(AuthenticationSchemeHandler.class);
         when(authenticationSchemeHandler.answerToChallenge(request, response)).thenReturn(false);
         when(authenticationSchemeHandler.challengeNeeded(request, response)).thenReturn(true);
         Collection<Class<? extends Callback>> callbackClasses = Lists.newArrayList();
         callbackClasses.add(NameCallback.class);
         when(authenticationSchemeHandler.getCallbackTypes()).thenReturn(callbackClasses);
 
         Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers = new ArrayList<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>>();
         authenticationSchemeHandlers.add(authenticationSchemeHandler);
 
         MockCallbackHandler mockCallbackHandler = new AsynchronousMockCallbackHandler(request, response, authenticationSchemeHandlers);
         List<Callback> callbackList = new ArrayList<Callback>();
         NameCallback nameCallback = new NameCallback(DUMMY_PROMPT);
         callbackList.add(nameCallback);
         Callback[] callbacks = callbackList.toArray(new Callback[callbackList.size()]);
 
         //when
         mockCallbackHandler.handle(callbacks);
     }
 
 
 }
