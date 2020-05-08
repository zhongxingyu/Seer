 package com.timgroup.tucker.info.component;
 
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 
 import org.junit.Test;
 
 import com.timgroup.tucker.info.Status;
 
 public final class ServletVersionComponentTest {
 
     @Test
     public void defaultIdAndLabelAreSuitable() throws Exception {
         final ServletConfig servlet = mock(ServletConfig.class);
         
         assertEquals("version", new ServletVersionComponent(servlet).getId());
         assertEquals("Version", new ServletVersionComponent(servlet).getLabel());
     }
     
     @Test
     public void reportStatusIsInfo() throws Exception {
         final ServletConfig servlet = mock(ServletConfig.class);
         
         assertEquals(Status.INFO, new ServletVersionComponent(servlet).getReport().getStatus());
     }
     
     @Test
     public void reportValueIsImplementationVersionOfPackageContainingAnchorClass() throws Exception {
         final ServletConfig servlet = mock(ServletConfig.class);
         final ServletContext context = mock(ServletContext.class);
         final String versionEntry = "Implementation-Version: 1.2.3\n";
         final ByteArrayInputStream value = new ByteArrayInputStream(versionEntry.getBytes());
      
         when(servlet.getServletContext()).thenReturn(context);
         when(context.getResourceAsStream("/META-INF/MANIFEST.MF")).thenReturn(value);
         
         
         assertEquals("1.2.3", new ServletVersionComponent(servlet).getReport().getValue());
     }
 
     @Test
     public void reportHasNoValueIfExceptionThrown() throws Exception {
         final ServletConfig servlet = mock(ServletConfig.class);
      
         when(servlet.getServletContext()).thenThrow(new RuntimeException("Failed"));
 
         assertFalse(new ServletVersionComponent(servlet).getReport().hasValue());
     }
     
     @Test
     public void streamIsClosed() throws Exception {
         final ServletConfig servlet = mock(ServletConfig.class);
         final ServletContext context = mock(ServletContext.class);
         final AtomicBoolean streamClosed = new AtomicBoolean(false);
         final ByteArrayInputStream value = new ByteArrayInputStream(new byte[] {}) {
             @Override public void close() throws IOException {
                 streamClosed.set(true);
             }
         };
      
         when(servlet.getServletContext()).thenReturn(context);
         when(context.getResourceAsStream("/META-INF/MANIFEST.MF")).thenReturn(value);
         
         assertNotNull(new ServletVersionComponent(servlet).getReport().getValue());
         assertTrue(streamClosed.get());
     }
 }
