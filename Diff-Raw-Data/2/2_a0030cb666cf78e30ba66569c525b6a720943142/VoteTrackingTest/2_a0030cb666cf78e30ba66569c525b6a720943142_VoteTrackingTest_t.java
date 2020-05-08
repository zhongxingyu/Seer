 package net.cyklotron.cms.poll.internal;
 
 import static net.cyklotron.cms.poll.internal.VoteTracking.LIMIT;
 import static net.cyklotron.cms.poll.internal.VoteTracking.STATE_ID;
 
 import java.net.URL;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.jmock.Mock;
 import org.jmock.MockObjectTestCase;
 import org.jmock.core.Invocation;
 import org.jmock.core.matcher.InvokedRecorder;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.web.HttpContext;
 
 import bak.pcj.list.LongArrayDeque;
 import bak.pcj.list.LongDeque;
 
 public class VoteTrackingTest
     extends MockObjectTestCase
 {
     private HttpContext httpContext;
 
     private Mock mockHttpServletRequest;
 
     private HttpServletRequest httpServletRequest;
 
     private Mock mockHttpSession;
 
     private HttpSession httpSession;
 
     private Mock mockHttpServletResponse;
 
     private HttpServletResponse httpServletResponse;
 
     private Mock mockResource;
 
     private Resource resource;
 
     private VoteTracking voteTracking;
 
     @Override
     public void setUp()
         throws Exception
     {
         mockHttpServletRequest = mock(HttpServletRequest.class);
         httpServletRequest = (HttpServletRequest)mockHttpServletRequest.proxy();
         mockHttpSession = mock(HttpSession.class);
         httpSession = (HttpSession)mockHttpSession.proxy();
         mockHttpServletResponse = mock(HttpServletResponse.class);
         httpServletResponse = (HttpServletResponse)mockHttpServletResponse.proxy();
         httpContext = new HttpContext(httpServletRequest, httpServletResponse);
         mockResource = mock(Resource.class);
         resource = (Resource)mockResource.proxy();
 
        mockHttpServletRequest.stubs().method("getSession").with(ANYTHING)
             .will(returnValue(httpSession));
         mockHttpServletRequest.stubs().method("getCookies").withNoArguments()
             .will(returnValue(null));
         mockHttpServletRequest.stubs().method("getContextPath").withNoArguments()
             .will(returnValue("/"));
         mockHttpServletRequest.stubs().method("getServletPath").withNoArguments()
             .will(returnValue("ledge"));
         mockHttpSession.stubs().method("getAttribute").with(isA(String.class))
             .will(returnValue(null));
         mockHttpSession.stubs().method("setAttribute").with(isA(String.class), ANYTHING).isVoid();
         mockHttpServletResponse.stubs().method("addCookie").with(isA(Cookie.class)).isVoid();
         mockResource.stubs().method("getId").withNoArguments().will(returnValue(1L));
 
         voteTracking = new VoteTracking(new URL("http://vote.cyklotron.org/"));
     }
 
     public void testEncode()
     {
         LongDeque list = new LongArrayDeque();
         list.add(1L);
         list.add(2L);
         list.add(3L);
         voteTracking.encode(list);
     }
 
     public void testEncodeDecode()
     {
         LongDeque list = new LongArrayDeque();
         list.add(1L);
         list.add(2L);
         list.add(3L);
         String enc = voteTracking.encode(list);
         LongDeque dec = voteTracking.decode(enc);
         assertEquals(list, dec);
     }
 
     public void testEncodeLimit()
     {
         LongDeque list = new LongArrayDeque();
         for(int i = 0; i < LIMIT + 1; i++)
         {
             list.add((long)i);
         }
         assertEquals(LIMIT + 1, list.size());
         String enc = voteTracking.encode(list);
         LongDeque dec = voteTracking.decode(enc);
         assertEquals(LIMIT, dec.size());
         System.out.println(enc.length());
         assertTrue(enc.length() + STATE_ID.length() + 2 < 4096);
     }
 
     public void testNoSessionNoCookies()
     {
 
         assertFalse(voteTracking.hasVoted(httpContext, resource));
     }
 
     public void testNoSessionOtherCookies()
     {
         Cookie[] cookies = new Cookie[1];
         cookies[0] = new Cookie("some_cookie", "value");
         mockHttpServletRequest.stubs().method("getCookies").withNoArguments()
             .will(returnValue(cookies));
 
         assertFalse(voteTracking.hasVoted(httpContext, resource));
     }
 
     public void testFromSession()
     {
         LongDeque list = new LongArrayDeque();
         list.add(1L);
         mockHttpSession.stubs().method("getAttribute").with(eq(STATE_ID)).will(returnValue(list));
 
         assertTrue(voteTracking.hasVoted(httpContext, resource));
     }
 
     public void testFromCookie()
     {
         LongDeque list = new LongArrayDeque();
         list.add(1L);
         Cookie[] cookies = new Cookie[1];
         cookies[0] = new Cookie(STATE_ID, voteTracking.encode(list));
         mockHttpServletRequest.stubs().method("getCookies").withNoArguments()
             .will(returnValue(cookies));
 
         assertTrue(voteTracking.hasVoted(httpContext, resource));
     }
 
     public void testTrackNoSession()
     {
         LongDeque list = new LongArrayDeque();
         list.add(1L);        
         final LongDeque[] listRef = new LongDeque[1];
         mockHttpSession.expects(once()).method("setAttribute")
             .with(eq(STATE_ID), isA(LongDeque.class)).match(new InvokedRecorder() {
                 @Override
                 public void invoked(Invocation paramInvocation)
                 {
                     listRef[0] = (LongDeque)paramInvocation.parameterValues.get(1);
                 }                
             });
         mockHttpServletResponse.expects(once()).method("addCookie").with(isA(Cookie.class))
             .isVoid();
 
         voteTracking.trackVote(httpContext, resource);
         assertEquals(list, listRef[0]);
     }
 
     public void testTrackSession()
     {
         LongDeque list = new LongArrayDeque();
         mockHttpSession.stubs().method("getAttribute").with(eq(STATE_ID)).will(returnValue(list));
         mockHttpServletResponse.expects(once()).method("addCookie").with(isA(Cookie.class))
             .isVoid();
 
         voteTracking.trackVote(httpContext, resource);
         assertTrue(voteTracking.hasVoted(httpContext, resource));
         assertEquals(1, list.size());
     }
 
     public void testTrackLimit()
     {
         LongDeque list = new LongArrayDeque();
         mockHttpSession.stubs().method("getAttribute").with(eq(STATE_ID)).will(returnValue(list));
         for(int i = 0; i < LIMIT; i++)
         {
             setMockResourceId(i);
             voteTracking.trackVote(httpContext, resource);
         }
         assertEquals(LIMIT, list.size());
         setMockResourceId(0);
         assertTrue(voteTracking.hasVoted(httpContext, resource));
 
         setMockResourceId(LIMIT+1);
         voteTracking.trackVote(httpContext, resource);
         assertEquals(LIMIT, list.size());
         assertTrue(voteTracking.hasVoted(httpContext, resource));
         
         setMockResourceId(0);
         assertFalse(voteTracking.hasVoted(httpContext, resource));
     }
 
     private void setMockResourceId(int i)
     {
         mockResource.stubs().method("getId").withNoArguments().will(returnValue((long)i));
     }
 }
