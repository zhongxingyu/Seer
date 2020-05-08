 package org.codehaus.xfire.util;
 
 import org.codehaus.xfire.test.AbstractXFireTest;
 
 public class ResolverTestCase extends AbstractXFireTest
 {
     public void testFile() throws Exception
     {
         Resolver resolver = new Resolver(getTestFile("").getAbsolutePath(), "pom.xml");
         assertNotNull(resolver.getFile());
         assertNotNull(resolver.getInputStream());
     }
     
     public void testFileBaseAsURI() throws Exception
     {
        Resolver resolver = new Resolver(getTestFile("src/test").toURL().toString(), 
                                         "org/codehaus/xfire/util/amazon.xml");
        assertNull(resolver.getFile());
         assertNotNull(resolver.getInputStream());
        assertNotNull(resolver.getURL());
     }
 
     public void testClasspath() throws Exception
     {
         Resolver resolver = new Resolver("org/codehaus/xfire/util/amazon.xml");
         assertNull(resolver.getFile());
         assertNotNull(resolver.getInputStream());
     }
 }
