 package com.zenika.cudf.parser;
 
 import com.zenika.cudf.parser.model.CUDFParsedDescriptor;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Antoine Rouaze <antoine.rouaze@zenika.com>
  */
 public class TestFileSerializer extends AbstractTestParser {
 
     private static final Logger LOG = Logger.getLogger(TestFileSerializer.class.getName());
 
     private File outputFile;
 
     @Before
     public void setUp() {
        outputFile = new File("target/tmp-com.zenika.cudf-output.com.zenika.cudf");
     }
 
     @Test
     public void testFileSerializer() throws Exception {
         FileSerializer serializer = new FileSerializer(outputFile);
         CUDFParsedDescriptor parsedDescriptor = createParsedDescriptor();
         serializer.parseCUDF(parsedDescriptor);
         String actualGeneration = readFile(outputFile);
        String expectedGeneration = readFile(new File("src/test/resource/com/zenika/com.zenika.cudf/resource/expected-generation.com.zenika.cudf"));
         assertEquals(expectedGeneration, actualGeneration);
     }
 
     private String readFile(File file) throws IOException {
         BufferedReader reader = new BufferedReader(new FileReader(file));
         StringBuilder stringBuilder = new StringBuilder(100);
         String currentLine;
         while ((currentLine = reader.readLine()) != null) {
             stringBuilder.append(currentLine).append("\n");
         }
         return stringBuilder.toString();
     }
 
     @After
     public void tearDown() {
         if (!outputFile.delete()) {
             LOG.log(Level.WARNING, "Unable to delete ");
         }
     }
 }
