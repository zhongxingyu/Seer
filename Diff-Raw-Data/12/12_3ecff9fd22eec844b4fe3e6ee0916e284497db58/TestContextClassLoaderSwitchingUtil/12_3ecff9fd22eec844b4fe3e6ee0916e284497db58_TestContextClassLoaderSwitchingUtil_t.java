 package com.atlassian.plugin.util;
 
 import junit.framework.TestCase;
 import org.mockito.Mock;
 
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.mockito.Mockito.verify;
 
 public class TestContextClassLoaderSwitchingUtil extends TestCase
 {
     @Mock private ClassLoader newLoader;
 
     public void setUp()
     {
         initMocks(this);
     }
 
     public void testSwitchClassLoader()
     {
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
         ContextClassLoaderSwitchingUtil.switchClassLoader(newLoader, new Runnable()
         {
             public void run()
             {
                 assertEquals(newLoader, Thread.currentThread().getContextClassLoader());
                newLoader.getResource("test");
             }
         });

        // Verify the loader is set back.
        assertEquals(currentLoader, Thread.currentThread().getContextClassLoader());

        // Verify the code was actually called
        verify(newLoader).getResource("test");
     }
 }
