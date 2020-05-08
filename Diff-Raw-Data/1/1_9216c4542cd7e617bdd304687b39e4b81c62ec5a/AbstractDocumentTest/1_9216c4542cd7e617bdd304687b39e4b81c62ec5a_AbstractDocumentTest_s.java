 package org.lawrencebower.docgen.core.document;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.lawrencebower.docgen.core.exception.DocGenException;
 import org.lawrencebower.docgen.core.generator.custom.CustomDocumentFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static junit.framework.Assert.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:META-INF/core-test-context.xml")
 public class AbstractDocumentTest {
 
     @Autowired
     private CustomDocumentFactory factory;
 
     @Test
     public void testSetName_emptyNameSet_throwsError() {
         try {
             factory.getCustomDocument("");
         } catch (DocGenException e) {
             String message = e.getMessage();
             assertEquals("Name is not set", message);
         }
     }
 
     @Test
     public void testSetName_nullNameSet_throwsError() {
         try {
             factory.getCustomDocument(null);
         } catch (DocGenException e) {
             String message = e.getMessage();
             assertEquals("Name is not set", message);
         }
     }
 }
