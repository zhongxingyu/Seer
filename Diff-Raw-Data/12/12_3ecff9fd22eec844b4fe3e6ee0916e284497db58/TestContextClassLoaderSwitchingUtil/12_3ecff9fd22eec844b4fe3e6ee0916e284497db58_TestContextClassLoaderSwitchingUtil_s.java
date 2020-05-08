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
         ContextClassLoaderSwitchingUtil.switchClassLoader(newLoader, new Runnable()
         {
             public void run()
             {
                 assertEquals(newLoader, Thread.currentThread().getContextClassLoader());
                newLoader.getParent();
             }
         });
        verify(newLoader).getParent();
     }
 }
