 package org.javasimon.tomcat;
 
 import org.apache.catalina.connector.Request;
 import org.testng.annotations.Test;
 
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 /**
  * Unit test for {@link TomcatRequestStopwatchSource}
  */
 public class TomcatRequestStopwatchSourceTest {
     @Test
     public void testGetSimonName() {
         TomcatRequestStopwatchSource stopwatchSource=new TomcatRequestStopwatchSource();
         Request requestMock=mock(Request.class);
         // Special characters removal
         when(requestMock.getRequestURI()).thenReturn("/test/some!weird^thing");
        assertEquals(stopwatchSource.getMonitorName(requestMock),"org.javasimon.tomcat.http./test/some_weird_thing");
         // JSESSIONID removal
         when(requestMock.getRequestURI()).thenReturn("/test/url?JSESSIONID=1234");
        assertEquals(stopwatchSource.getMonitorName(requestMock), "org.javasimon.tomcat.http./test/url_");
     }
 }
