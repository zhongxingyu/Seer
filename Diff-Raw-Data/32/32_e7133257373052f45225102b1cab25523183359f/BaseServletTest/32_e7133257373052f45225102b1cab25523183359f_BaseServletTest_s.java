 package com.ibm.opensocial.landos;
 
 import static org.junit.Assert.assertEquals;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.easymock.EasyMock;
 import org.easymock.IMocksControl;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class BaseServletTest extends EasyMock { 
   private BaseServlet servlet;
   private IMocksControl control;
   private HttpServletRequest req;
   
   @Before
   public void before() throws Exception {
     servlet = new BaseServlet();
     control = createControl();
   }
   
   @After
   public void after() {
     
   }
   
   @Test
   public void testGetLastSegmentPath() throws Exception {
     req = TestControlUtils.mockRequest(control, null, null, "/foo/bar");
     
     control.replay();
     assertEquals("Last path segment", "bar", servlet.getPathSegment(req, 1));
     control.verify();
   }
   
   @Test
   public void testFirstSegmentPathTrailingSlash() throws Exception {
     req = TestControlUtils.mockRequest(control, null, null, "/foo/bar/");
     
     control.replay();
     assertEquals("First path", "foo", servlet.getPathSegment(req, 0));
     control.verify();
   }
   
   @Test
   public void testFirstLastSegmentPath() throws Exception {
     req = TestControlUtils.mockRequest(control, null, null, "/foo");
     
     control.replay();
     assertEquals("First path", "foo", servlet.getPathSegment(req, 0));
     control.verify();
   }
   
   @Test
   public void testNumSegments() {
     // First
    req = TestUtils.mockRequest(null, null, "/foo/bar");
     replay(req);
     assertEquals(2, servlet.numSegments(req));
     verify(req);
     // Second
    req = TestUtils.mockRequest(null, null, "//foo//bar");
     replay(req);
     assertEquals(2, servlet.numSegments(req));
     verify(req);
     // Third
    req = TestUtils.mockRequest(null, null, "");
     replay(req);
     assertEquals(0, servlet.numSegments(req));
     verify(req);
     // Fourth
    req = TestUtils.mockRequest(null, null, "/foo//bar///baz/hello/world");
     replay(req);
     assertEquals(5, servlet.numSegments(req));
     verify(req);
   }
 }
 
