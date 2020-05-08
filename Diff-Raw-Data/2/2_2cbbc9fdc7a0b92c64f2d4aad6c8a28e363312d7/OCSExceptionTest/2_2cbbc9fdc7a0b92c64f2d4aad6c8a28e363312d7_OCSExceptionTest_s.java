 package org.duraspace.dfr.ocs.core;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit tests for {@link OCSException}.
  */
 public class OCSExceptionTest {
     @Test
     public void initWithMessage() {
         OCSException ocsException = new OCSException("test");
         Assert.assertEquals("test", ocsException.getMessage());
        Assert.assertNull(ocsException.getMessage());
     }
 
     @Test
     public void initWithMessageAndCause() {
         Exception cause = new Exception();
         OCSException ocsException = new OCSException("test", cause);
         Assert.assertEquals("test", ocsException.getMessage());
         Assert.assertEquals(cause, ocsException.getCause());
     }
 }
