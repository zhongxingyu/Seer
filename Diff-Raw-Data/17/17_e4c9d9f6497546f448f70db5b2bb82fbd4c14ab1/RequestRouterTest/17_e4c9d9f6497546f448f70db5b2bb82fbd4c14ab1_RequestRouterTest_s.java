 package com.tjh.swivel.controller;
 
 import com.tjh.swivel.model.ShuntRequestHandler;
 import com.tjh.swivel.model.StubRequestHandler;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.net.URI;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.hasItem;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.sameInstance;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 @SuppressWarnings("unchecked")
 public class RequestRouterTest {
 
     public static final URI LOCAL_URI = URI.create("some/path");
     public static final URI DEEP_URI = URI.create("some/path/deep");
     public static final int STUB_HANDLER_ID = 456;
     private ShuntRequestHandler mockRequestHandler;
     private RequestRouter requestRouter;
     private HttpRequestBase mockRequestBase;
     private StubRequestHandler mockStubHandler;
 
     @Before
     public void setUp() throws Exception {
         requestRouter = new RequestRouter();
         mockRequestBase = mock(HttpRequestBase.class);
         mockRequestHandler = mock(ShuntRequestHandler.class);
         mockStubHandler = mock(StubRequestHandler.class);
 
         when(mockRequestBase.getURI()).thenReturn(LOCAL_URI);
         when(mockStubHandler.getId()).thenReturn(STUB_HANDLER_ID);
         when(mockStubHandler.matches(any(HttpUriRequest.class))).thenReturn(true);
     }
 
     @Test
     public void setShuntPutsRequestHandlerAtExpectedPath() {
         requestRouter.setShunt(LOCAL_URI, mockRequestHandler);
 
         assertThat((ShuntRequestHandler) requestRouter.uriHandlers
                 .get(LOCAL_URI.getPath())
                 .get(RequestRouter.SHUNT_NODE),
                 equalTo(mockRequestHandler));
     }
 
     @Test
     public void deleteShuntRemovesHandler() {
         requestRouter.setShunt(LOCAL_URI, mockRequestHandler);
         requestRouter.deleteShunt(LOCAL_URI);
 
        assertThat(requestRouter.uriHandlers.keySet().isEmpty(), is(true));
     }
 
     @Test
     public void routeDelegatesToShuntRequestHandler() {
         requestRouter.setShunt(LOCAL_URI, mockRequestHandler);
 
         requestRouter.route(mockRequestBase);
 
         verify(mockRequestHandler).handle(eq(mockRequestBase), any(URI.class), any(HttpClient.class));
     }
 
     @Test
     public void routeFindsHandlerOnShallowerPath() {
         requestRouter.setShunt(LOCAL_URI, mockRequestHandler);
         when(mockRequestBase.getURI()).thenReturn(DEEP_URI);
 
         requestRouter.route(mockRequestBase);
 
         verify(mockRequestHandler).handle(eq(mockRequestBase), any(URI.class), any(HttpClient.class));
     }
 
     @Test(expected = RuntimeException.class)
     public void routeThrowsOnUnknownURI() {
         requestRouter.route(mockRequestBase);
     }
 
     @Test
     public void routeRemovesMatchedPathFromURISentToHandler() {
         requestRouter.setShunt(LOCAL_URI, mockRequestHandler);
         when(mockRequestBase.getURI()).thenReturn(DEEP_URI);
 
         requestRouter.route(mockRequestBase);
 
         verify(mockRequestHandler).handle(eq(mockRequestBase), eq(LOCAL_URI), any(HttpClient.class));
     }
 
     @Test
     public void addStubPutsStubInListAtPath() {
         requestRouter.addStub(LOCAL_URI, mockStubHandler);
 
         assertThat(((List<StubRequestHandler>) requestRouter.uriHandlers
                 .get(LOCAL_URI.getPath())
                 .get(RequestRouter.STUB_NODE)).get(0),
                 sameInstance(mockStubHandler));
     }
 
     @Test
     public void removeStubRemovesStubInListAtPath() {
         requestRouter.addStub(LOCAL_URI, mockStubHandler);
 
         requestRouter.removeStub(LOCAL_URI, STUB_HANDLER_ID);
 
         assertThat((List<StubRequestHandler>) requestRouter.uriHandlers
                 .get(LOCAL_URI.getPath())
                 .get(RequestRouter.STUB_NODE),
                 not(hasItem(mockStubHandler)));
     }
 }
