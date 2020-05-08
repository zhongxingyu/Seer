 import com.mockgenerator.configuration.Schema;
 import com.mockgenerator.configuration.SchemaParser;
 import com.mockgenerator.generator.FileGenerator;
 import com.mockgenerator.generator.Options;
 import org.junit.Test;
 
 import java.io.File;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * User: rixonmathew
  * Date: 20/01/13
  * Time: 12:31 PM
  */
 public class TestMockFileGenerator {
 
     @Test
     public void testMockFileGeneration() {
         Schema schema = SchemaParser.parse("positions.json");
         Options options = new Options();
         options.setGenerationType("random");
         options.setNumberOfFileSplits(3);
        options.setNumberOfRecordsPerSplit(5000);
         final String outputDirectory = "test-target";
         options.setOutputDirectory(outputDirectory);
         options.setCompressOutput(true);
         FileGenerator fileGenerator = new FileGenerator(options,schema);
         fileGenerator.generateFiles();
         File file = new File(outputDirectory);
         assertThat(file.isDirectory(),is(true));
         File[] files = file.listFiles();
         assertNotNull(files);
         assertThat((long) files.length,is(options.getNumberOfFileSplits()));
         for (File file1:files) {
             System.out.println("file1 = " + file1.getName());
         }
         //assert that directory has been created
     }
 }
 
