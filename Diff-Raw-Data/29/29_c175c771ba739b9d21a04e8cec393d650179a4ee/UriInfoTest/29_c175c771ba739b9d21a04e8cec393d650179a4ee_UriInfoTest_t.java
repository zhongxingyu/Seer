 package com.jayway.forest.roles;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import static org.junit.Assert.assertEquals;
 
 public class UriInfoTest {
 
     private UriInfo tested;
    private MockHttpServletRequest request;
 
     @Before
     public void setUp() {
        request = new MockHttpServletRequest(null, "/mywebapp/servlet/MyServlet/a/b;c=123");
         request.setServletPath("/servlet/MyServlet");
         request.setPathInfo("/a/b;c=123");
 
         tested = new UriInfo(request);
     }
 
     @Test
     public void testGetBaseUrl() throws Exception {
         assertEquals("http://localhost:80/mywebapp/servlet/MyServlet/", tested.getBaseUrl());
     }
 
     @Test
     public void testGetRelativeUrl() throws Exception {
         assertEquals("", tested.getRelativeUrl());
     }
 
     @Test
     public void testGetSelf() throws Exception {
         assertEquals("http://localhost:80/mywebapp/servlet/MyServlet/", tested.getSelf());
     }
 
     @Test
     public void testAddPath() throws Exception {
         tested.addPath("apa");
         assertEquals("apa/", tested.getRelativeUrl());
         assertEquals("http://localhost:80/mywebapp/servlet/MyServlet/apa/", tested.getSelf());
     }

    @Test
    public void emptyPathInfoShouldNotCauseError() throws Exception {
        request = new MockHttpServletRequest(null, "/mywebapp/servlet/MyServlet/");
        request.setServletPath("/servlet/MyServlet");
        request.setPathInfo(null);
        tested = new UriInfo(request);
        assertEquals("http://localhost:80/mywebapp/servlet/MyServlet/", tested.getBaseUrl());
    }
 }
