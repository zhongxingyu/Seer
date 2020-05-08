 package com.vance.quest2012;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * Unit test for {@link WebRetriever}
  *
  * @author srvance
  */
 public class WebRetrieverTest {
     @Test
     public void testWebRetriever() {
         String expectedProtocol = "http";
         String expectedHost = "localhost";
         String expectedTarget = expectedProtocol + "://" + expectedHost;
 
         WebRetriever sut = new WebRetriever(expectedTarget);
 
         Assert.assertNotNull(sut);
         Assert.assertEquals(expectedTarget, sut.getTarget());
         Assert.assertEquals(expectedProtocol, sut.getProtocol());
     }
 }
