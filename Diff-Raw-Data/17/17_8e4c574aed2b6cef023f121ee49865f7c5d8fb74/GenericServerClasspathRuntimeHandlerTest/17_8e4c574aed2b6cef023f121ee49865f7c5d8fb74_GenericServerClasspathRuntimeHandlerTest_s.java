 /*
  * Created on Dec 11, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.server.generic.tests;
 
 import java.util.HashMap;
 
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jst.server.generic.internal.core.GenericServerRuntime;
 import org.eclipse.jst.server.generic.internal.core.GenericServerRuntimeTargetHandler;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.model.RuntimeDelegate;
 
 import junit.framework.TestCase;
 
 /**
  * 
  *
  * @author Gorkem Ercan
  */
 public class GenericServerClasspathRuntimeHandlerTest extends TestCase {
 
    private static final String CLASSPATH_PREFIX = "C:\\dev\\java\\appservers\\JOnAS-4.1.4";
     private static final String SERVER_DEF_NAME = "JonAS 4.1.4";
     private IRuntime fRuntime;
     /*
      * @see TestCase#setUp()
      */
     protected void setUp() throws Exception {
         super.setUp();
         IRuntimeType type =ServerCore.findRuntimeType("org.eclipse.jst.server.generic.runtime");
         IRuntimeWorkingCopy wc = type.createRuntime("testRuntime",null);
         RuntimeDelegate delegate = (RuntimeDelegate)wc.getAdapter(RuntimeDelegate.class);
 		HashMap props = new HashMap();
 		props.put("mappernames", "");
 		props.put("classPathVariableName", "JONAS");
 		props.put("serverAddress", "127.0.0.1");
 		props.put("jonasBase", "D:\\dev\\java\\appservers\\JOnAS-4.1.4");
 		props.put("jonasRoot", "D:\\dev\\java\\appservers\\JOnAS-4.1.4");
 		props.put("classPath", CLASSPATH_PREFIX);
 		props.put("protocols", "jrmp");
 		props.put("port", "9000");		
 	    delegate.setAttribute(
 				GenericServerRuntime.SERVER_INSTANCE_PROPERTIES, props);
 	    delegate.setAttribute(GenericServerRuntime.SERVER_DEFINITION_ID,SERVER_DEF_NAME);
 		wc.save(false,null);
 		
 		fRuntime = wc.getOriginal();
     }
 
     /**
      * Constructor for ClasspathRuntimeHandlerTest.
      * @param name
      */
     public GenericServerClasspathRuntimeHandlerTest(String name) {
         super(name);
     }
 
     public void testGetClasspathContainerLabel() {
         GenericServerRuntimeTargetHandler handler = new GenericServerRuntimeTargetHandler();
         String name = handler.getClasspathContainerLabel(fRuntime,null);
         assertEquals(SERVER_DEF_NAME,name);
     }
 
     public void testResolveClasspathContainer() {
         GenericServerRuntimeTargetHandler handler = new GenericServerRuntimeTargetHandler();
         IClasspathEntry[] entries = handler.resolveClasspathContainer(fRuntime,null);
         assertNotNull("Failed to resolve classpath entries",entries);
         for (int i = 0; i < entries.length; i++) {
            assertTrue("the resolved classpath entry does not start with classpath prefix",entries[i].getPath().toOSString().startsWith(CLASSPATH_PREFIX));
         }
     }
 
 }
