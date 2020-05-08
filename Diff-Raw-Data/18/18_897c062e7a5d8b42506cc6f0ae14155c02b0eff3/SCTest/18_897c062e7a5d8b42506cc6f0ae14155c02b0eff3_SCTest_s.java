 package org.vaadin.smartgwt.server.util;
 
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.HashMap;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.vaadin.rpc.server.ServerSideHandler;
 import org.vaadin.rpc.server.ServerSideProxy;
 import org.vaadin.smartgwt.server.util.SC.ServerSideProxyFactory;
 
 import com.google.common.collect.Maps;
 
 public class SCTest {
 	private SC sc;
 	private ServerSideProxy serverSideProxy;
 
 	@Before
 	public void before() {
 		final ServerSideProxyFactory serverSideProxyFactory = mock(ServerSideProxyFactory.class);
 		serverSideProxy = mock(ServerSideProxy.class);
 		when(serverSideProxyFactory.newServerSideProxy(any(ServerSideHandler.class))).thenReturn(serverSideProxy);
 
 		sc = new SC(serverSideProxyFactory);
 	}
 
 	@Test
 	public void test_sendsConfirmationRequest() {
 		final String message = "message";
 
 		sc.confirm(message, null);
 		verify(serverSideProxy).call(eq("confirm"), anyInt(), eq(message), anyString());
 	}
 
 	@Test
 	public void test_sendsConfirmationRequestWithTitle() {
 		final String title = "title";
 		final String message = "message";
 
 		sc.confirm(title, message, null);
 		verify(serverSideProxy).call(eq("confirm"), anyInt(), eq(message), eq(title));
 	}
 
 	@Test
 	public void test_confirmationRequestCallsCallbackOnVariablesChange() {
 		final BooleanCallback callback = mock(BooleanCallback.class);
 		final ArgumentCaptor<Integer> keyCaptor = ArgumentCaptor.forClass(Integer.class);
 		final HashMap<String, Object> variables = Maps.newHashMap();
 		final Boolean clientResult = true;
 
 		sc.confirm(null, callback);
 		verify(serverSideProxy, atLeastOnce()).call(anyString(), keyCaptor.capture(), anyString(), anyString());
 
 		variables.put("callbackKey", keyCaptor.getValue());
 		variables.put("callback", clientResult);
 
 		sc.changeVariables(null, variables);
 		verify(callback).execute(clientResult);
 	}
 
 	@Test
 	public void test_confirmationRequestsCallsCallbackOnVariablesChangeWithNullResult() {
 		final BooleanCallback callback = mock(BooleanCallback.class);
 		final ArgumentCaptor<Integer> keyCaptor = ArgumentCaptor.forClass(Integer.class);
 		final HashMap<String, Object> variables = Maps.newHashMap();
 		final Boolean clientResult = null;
 
 		sc.confirm(null, callback);
 		verify(serverSideProxy, atLeastOnce()).call(anyString(), keyCaptor.capture(), anyString(), anyString());
 
 		variables.put("callbackKey", keyCaptor.getValue());
 		variables.put("callback", clientResult);
 
 		sc.changeVariables(null, variables);
 		verify(callback).execute(clientResult);
 	}
 
 	@Test
 	public void test_callsCallbackOnConfirmationRequestWithPendingRequests() {
 		final BooleanCallback callback = mock(BooleanCallback.class);
 		final ArgumentCaptor<Integer> keyCaptor = ArgumentCaptor.forClass(Integer.class);
 		final HashMap<String, Object> variables = Maps.newHashMap();
 		final Boolean clientResult = true;
 
 		sc.confirm(null, mock(BooleanCallback.class));
 		sc.confirm(null, callback);
 		verify(serverSideProxy, atLeastOnce()).call(anyString(), keyCaptor.capture(), anyString(), anyString());
 		sc.confirm(null, mock(BooleanCallback.class));
 
 		variables.put("callbackKey", keyCaptor.getValue());
 		variables.put("callback", clientResult);
 		sc.changeVariables(null, variables);
 		verify(callback).execute(clientResult);
 	}
 
 	@Test
 	public void test_ask_sendsAskRequest() {
 		final String message = "message";
 
 		sc.ask(message, null);
		verify(serverSideProxy).call(eq("ask"), anyInt(), eq(message), anyString());
 	}
 
 	@Test
 	public void test_ask_sendsAskRequestWithTitle() {
 		final String title = "title";
 		final String message = "message";
 
 		sc.ask(title, message, null);
		verify(serverSideProxy).call(eq("ask"), anyInt(), eq(message), eq(title));
 	}
 
 	@Test
 	public void test_ask_AskRequestCallsCallbackOnVariablesChange() {
 		final BooleanCallback callback = mock(BooleanCallback.class);
 		final ArgumentCaptor<Integer> keyCaptor = ArgumentCaptor.forClass(Integer.class);
 		final HashMap<String, Object> variables = Maps.newHashMap();
 		final Boolean clientResult = true;
 
 		sc.ask(null, callback);
 		verify(serverSideProxy, atLeastOnce()).call(anyString(), keyCaptor.capture(), anyString(), anyString());
 
 		variables.put("callbackKey", keyCaptor.getValue());
 		variables.put("callback", clientResult);
 
 		sc.changeVariables(null, variables);
 		verify(callback).execute(clientResult);
 	}
 
 	@Test
 	public void test_ask_AskRequestsCallsCallbackOnVariablesChangeWithNullResult() {
 		final BooleanCallback callback = mock(BooleanCallback.class);
 		final ArgumentCaptor<Integer> keyCaptor = ArgumentCaptor.forClass(Integer.class);
 		final HashMap<String, Object> variables = Maps.newHashMap();
 		final Boolean clientResult = null;
 
 		sc.ask(null, callback);
 		verify(serverSideProxy, atLeastOnce()).call(anyString(), keyCaptor.capture(), anyString(), anyString());
 
 		variables.put("callbackKey", keyCaptor.getValue());
 		variables.put("callback", clientResult);
 
 		sc.changeVariables(null, variables);
 		verify(callback).execute(clientResult);
 	}
 
 	@Test
 	public void test_ask_callsCallbackOnAskRequestWithPendingRequests() {
 		final BooleanCallback callback = mock(BooleanCallback.class);
 		final ArgumentCaptor<Integer> keyCaptor = ArgumentCaptor.forClass(Integer.class);
 		final HashMap<String, Object> variables = Maps.newHashMap();
 		final Boolean clientResult = true;
 
 		sc.ask(null, mock(BooleanCallback.class));
 		sc.ask(null, callback);
 		verify(serverSideProxy, atLeastOnce()).call(anyString(), keyCaptor.capture(), anyString(), anyString());
 		sc.ask(null, mock(BooleanCallback.class));
 
 		variables.put("callbackKey", keyCaptor.getValue());
 		variables.put("callback", clientResult);
 		sc.changeVariables(null, variables);
 		verify(callback).execute(clientResult);
 	}
 }
