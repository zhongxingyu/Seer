 package org.jichigo.utility.xml.validation;
 
 import java.io.File;
 import java.net.URL;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 public class SchemaCacheTest {
 
     @Before
     public void setup() {
         SchemaCache.clearCache();
     }
 
     @Test
     public void getSchema_URL() throws SAXException {
         URL sourceUrl1 = Thread.currentThread().getContextClassLoader()
                 .getResource(getTestcaseResourcePath("test_normal.xsd"));
         URL sourceUrl2 = Thread.currentThread().getContextClassLoader()
                 .getResource(getTestcaseResourcePath("test_normal.xsd"));
         Schema schema1 = SchemaCache.getSchema(sourceUrl1);
         Schema schema2 = SchemaCache.getSchema(sourceUrl2);
         Assert.assertSame(schema1, schema2);
     }
 
     @Test
     public void getSchema_File() throws SAXException {
         URL sourceUrl = Thread.currentThread().getContextClassLoader()
                 .getResource(getTestcaseResourcePath("test_normal.xsd"));
         File sourceFile1 = new File(sourceUrl.getFile());
         File sourceFile2 = new File(sourceUrl.getFile());
         Schema schema1 = SchemaCache.getSchema(sourceFile1);
         Schema schema2 = SchemaCache.getSchema(sourceFile2);
         Assert.assertSame(schema1, schema2);
     }
 
     @Test
     public void getSchema_Source() throws SAXException {
         URL sourceUrl = Thread.currentThread().getContextClassLoader()
                 .getResource(getTestcaseResourcePath("test_normal.xsd"));
         File sourceFile = new File(sourceUrl.getFile());
         StreamSource source = new StreamSource(sourceFile);
         Schema schema1 = SchemaCache.getSchema(source);
         Schema schema2 = SchemaCache.getSchema(source);
         Assert.assertSame(schema1, schema2);
     }
 
     @Test
     public void getSchema_SourceArray() throws SAXException {
         URL sourceUrl = Thread.currentThread().getContextClassLoader()
                 .getResource(getTestcaseResourcePath("test_normal.xsd"));
         File sourceFile = new File(sourceUrl.getFile());
         StreamSource source = new StreamSource(sourceFile);
         Source[] sourceArray = new StreamSource[] { source, source };
         Schema schema1 = SchemaCache.getSchema(sourceArray);
         Schema schema2 = SchemaCache.getSchema(sourceArray);
         Assert.assertSame(schema1, schema2);
     }
 
     @Test
     public void getSchema_multiple() throws SAXException {
         URL sourceUrl1 = Thread.currentThread().getContextClassLoader()
                 .getResource(getTestcaseResourcePath("test_normal.xsd"));
         URL sourceUrl2 = Thread.currentThread().getContextClassLoader()
                 .getResource(getTestcaseResourcePath("test_normal2.xsd"));
         Schema schema1 = SchemaCache.getSchema(sourceUrl1);
         Schema schema2 = SchemaCache.getSchema(sourceUrl2);
         Assert.assertNotSame(schema1, schema2);
     }
 
     @Test
     public void getSchema_unsupportObject() throws SAXException {
         Object unsupportedObject = new Object();
         try {
             SchemaCache.getSchema(unsupportedObject);
             Assert.fail("not occur IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             Assert.assertEquals("source class is unsupported. class is java.lang.Object.", e.getMessage());
             Assert.assertNull(e.getCause());
         }
     }
 
     @Test
     public void getSchema_null() throws SAXException {
         try {
             SchemaCache.getSchema(null);
             Assert.fail("not occur IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             Assert.assertEquals("source class is unsupported. class is null.", e.getMessage());
             Assert.assertNull(e.getCause());
         }
     }
 
     @Test
     public void getSchema_SAXException() {
         try {
             URL sourceUrl = Thread.currentThread().getContextClassLoader()
                    .getResource(getTestcaseResourcePath("test_error.xsd"));
             SchemaCache.getSchema(sourceUrl);
             Assert.fail("not occur SAXException");
         } catch (SAXException e) {
             Assert.assertNotNull(e);
         }
     }
 
     @Test
     public void clearCache() throws SAXException {
 
         URL sourceUrl = Thread.currentThread().getContextClassLoader()
                 .getResource(getTestcaseResourcePath("test_normal.xsd"));
 
         Schema schema1 = SchemaCache.getSchema(sourceUrl);
         SchemaCache.clearCache();
         Schema schema2 = SchemaCache.getSchema(sourceUrl);
 
         Assert.assertNotSame(schema1, schema2);
     }
 
     private String getTestcaseResourcePath(String fileName) {
         String simpleClassName = getClass().getSimpleName();
         String testcaseDirName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
         return getClass().getPackage().getName().replace('.', '/') + '/' + testcaseDirName + '/' + fileName;
     }
 
 }
