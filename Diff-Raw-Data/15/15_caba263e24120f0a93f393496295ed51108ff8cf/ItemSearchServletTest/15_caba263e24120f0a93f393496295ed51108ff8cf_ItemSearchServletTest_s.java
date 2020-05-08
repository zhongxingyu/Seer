 package com.ibm.opensocial.landos;
 
 import static org.junit.Assert.assertEquals;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ItemSearchServletTest extends EasyMock { 
   private ItemSearchServlet servlet;
   private HttpServletRequest req;
   
   @Before
   public void before() throws Exception {
     servlet = new ItemSearchServlet();
   }
   
   @After
   public void after() {
     
   }
   
   @Test
   public void testGetLastSegmentPath() throws Exception {
    req = TestUtils.mockRequest(null, null, "/foo/bar");
     
    replay(req);
    
    verify(req);
   }
 }
 
