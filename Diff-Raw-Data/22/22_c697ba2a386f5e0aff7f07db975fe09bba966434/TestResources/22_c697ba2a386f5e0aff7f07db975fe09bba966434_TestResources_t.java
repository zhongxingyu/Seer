 package org.apache.felix.ipojo.everest.ipojo.test;
 
 import junit.framework.Assert;
 import org.apache.felix.ipojo.everest.impl.DefaultRequest;
 import org.apache.felix.ipojo.everest.services.*;
 import org.junit.Test;
 import org.ops4j.pax.exam.MavenUtils;
 import org.osgi.framework.Version;
 
 public class TestResources extends Common {
 
    public Resource get(String path) throws ResourceNotFoundException, IllegalActionOnResourceException {
         return everest.process(new DefaultRequest(Action.GET, Path.from(path), null));
     }
 
     /**
      * Check that the '/ipojo' resource is present.
      */
     @Test
     public void testRootIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         get("/ipojo");
     }
 
     /**
      * Check that the '/ipojo' resource metadata contains the iPOJO version.
      */
     @Test
     public void testIpojoVersion() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource ipojo = get("/ipojo");
         // Get the iPOJO version using Pax Exam and compare versions.
         String version = MavenUtils.getArtifactVersion("org.apache.felix", "org.apache.felix.ipojo").replace('-', '.');
         Assert.assertEquals(version, ipojo.getMetadata().get("version"));
     }
 
     /**
      * Check that the '/ipojo/factory' resource is present.
      */
     @Test
     public void testFactoryIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         get("/ipojo/factory");
     }
 
     /**
      * Check that the '/ipojo/factory' resource is present.
      */
     @Test
     public void testHandlerIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         get("/ipojo/handler");
     }
 
     /**
      * Check that the '/ipojo/instance' resource is present.
      */
     @Test
     public void testInstanceIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         get("/ipojo/instance");
     }
 
     /**
      * Check that the '/ipojo/declaration' resource is present.
      */
     @Test
     public void testDeclarationIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         get("/ipojo/declaration");
     }
 
 //    //TODO move this to TestFactories
 //    @Test
 //    public void testFooFactoryIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
 //        everest.process(new DefaultRequest(Action.GET, Path.from("/ipojo/factory/Foo/1.2.3.foo"), null));
 //    }
 
 }
