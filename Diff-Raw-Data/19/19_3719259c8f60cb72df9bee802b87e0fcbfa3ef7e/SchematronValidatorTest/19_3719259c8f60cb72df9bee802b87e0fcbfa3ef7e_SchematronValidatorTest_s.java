 package dk.statsbiblioteket.newspaper.metadatachecker;
 
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.SchematronAttributeValidator;
 import dk.statsbiblioteket.util.Streams;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Schematron validator test
  */
 public class SchematronValidatorTest {
 
     /**
      * Test that a valid jpylyzer document will validate
      * @throws Exception
      */
     @Test
     public void testValidateValid()
             throws
             Exception {
         byte[] jpylizerFile = getByteArrayOutputStream(Thread.currentThread().getContextClassLoader()
                                          .getResourceAsStream("valid.xml"));
         SchematronAttributeValidator validator = new SchematronAttributeValidator("sb-jp2.sch");
         ResultCollector results = new ResultCollector("test", "0.1");
         validator.validate("valid.xml",jpylizerFile,results);
         System.out.println(results.toReport());
         Assert.assertTrue(results.isSuccess());
 
 
     }
 
 
     private byte[] getByteArrayOutputStream(InputStream jpylizerOutput)
              throws
              IOException {
          ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
          Streams.pipe(jpylizerOutput, byteStream);
          return byteStream.toByteArray();
      }
 
     /**
      * Tests that an invalid jpylyzer document will not validate
      * @throws Exception
      */
     @Test
     public void testValidateInvalid()
             throws
             Exception {
         byte[] jpylizerFile = getByteArrayOutputStream(Thread.currentThread().getContextClassLoader()
                                                   .getResourceAsStream("invalid.xml"));
         SchematronAttributeValidator validator = new SchematronAttributeValidator("sb-jp2.sch");
         ResultCollector results = new ResultCollector("test", "0.1");
         validator.validate("invalid.xml",jpylizerFile,results);
         System.out.println(results.toReport());
         Assert.assertFalse(results.isSuccess());
        Assert.assertTrue(results.toReport().contains("<failures>\n" +
                                                      "        <failure>\n" +
                                                       "            <filereference>invalid.xml</filereference>\n" +
                                                       "            <type>jp2file</type>\n" +
                                                       "            <component>dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.SchematronAttributeValidator-null</component>\n"
 
                                                       +
                                                       "            <description>Invalid JP2</description>\n" +
                                                      "            <details>Location: '/jpylyzer[0]' Test: " +
                                                      "'isValidJP2 = 'True''</details>\n"
                                                      +
                                                      "        </failure>\n" +
                                                      "    </failures>"));
 
     }
 
 }
