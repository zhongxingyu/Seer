 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.API;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorJobConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.ScannerConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.scanner.APIScanner;
 import cz.cvut.fit.hybljan2.apitestingcg.scanner.SourceScanner;
 import cz.cvut.fit.hybljan2.apitestingcg.test.TestUtils;
 import junitx.framework.FileAssert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jan Hýbl
  * Date: 9.4.12
  * Time: 13:53
  */
 
 public class NestedClassTests {
 
     private static API api;
     private static API apime;
 
     @BeforeClass
     public static void Setup() throws IOException {
         ScannerConfiguration scannerConfiguration = new ScannerConfiguration();
         scannerConfiguration.setApiName("Nested classes test");
         scannerConfiguration.setPath("testres/nested");
         scannerConfiguration.setSource(ScannerConfiguration.APISource.SOURCECODE);
         scannerConfiguration.setSourceVersion("1.7");
         scannerConfiguration.setId("nested");
 
         APIScanner scanner = new SourceScanner();
         scanner.setConfiguration(scannerConfiguration);
 
         api = scanner.scan();
 
         ScannerConfiguration mecfg = new ScannerConfiguration();
         mecfg.setApiName("Map entry test");
         mecfg.setPath("testres/nestedme");
         mecfg.setId("nestedme");
 
         scanner.setConfiguration(mecfg);
         apime = scanner.scan();
 
         // delete output files from previous run of tests
         TestUtils.delete(new File("output/tests/nested"));
         TestUtils.delete(new File("output/tests/nestedme"));
     }
 
     @Test
     public void TestMethods() {
 
        Generator instantiatorGenerator = new InstantiatorGenerator(new GeneratorConfiguration());
        Generator extenderGenerator = new ExtenderGenerator(instantiatorGenerator.configuration);
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("nested");
         job.setOutputDir("output/tests/nested");
         job.setOutputPackage("test.%s");
        instantiatorGenerator.generate(api, job);
        extenderGenerator.generate(api,job);
 
         File resultFile = new File("output/tests/nested/test/lib/NestedInstantiator.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/nested_exp/lib/NestedInstantiator.java");
 
         FileAssert.assertEquals(expected, resultFile);
 
     }
 
     @Test
     public void TestMapEntry() {
         Generator igenerator = new InstantiatorGenerator(new GeneratorConfiguration());
         Generator egenerator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("meapi");
         job.setOutputDir("output/tests/nestedme");
         igenerator.generate(apime, job);
         egenerator.generate(apime, job);
 
         File resultFile = new File("output/tests/nestedme/test/lib/NestedInstantiator.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/nestedme_exp/lib/NestedInstantiator.java");
 
         FileAssert.assertEquals(expected, resultFile);
 
         resultFile = new File("output/tests/nestedme/test/lib/NestedExtender.java");
         assertTrue(resultFile.exists());
 
         expected = new File("testres/nestedme_exp/lib/NestedExtender.java");
 
         FileAssert.assertEquals(expected, resultFile);
 
     }
 }
