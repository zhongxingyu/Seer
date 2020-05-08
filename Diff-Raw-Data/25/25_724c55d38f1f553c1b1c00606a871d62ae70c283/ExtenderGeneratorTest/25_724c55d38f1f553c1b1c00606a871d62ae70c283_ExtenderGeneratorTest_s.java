 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.API;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorJobConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.ScannerConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.WhitelistRule;
 import cz.cvut.fit.hybljan2.apitestingcg.scanner.APIScanner;
 import cz.cvut.fit.hybljan2.apitestingcg.scanner.SourceScanner;
 import cz.cvut.fit.hybljan2.apitestingcg.test.TestUtils;
 import junitx.framework.FileAssert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jan Hýbl
  * Date: 6.2.12
  * Time: 9:44
  */
 public class ExtenderGeneratorTest {
 
     private static API api;
 
     @BeforeClass
     public static void Setup() throws IOException {
         ScannerConfiguration scannerConfiguration = new ScannerConfiguration();
         scannerConfiguration.setApiName("Extender test");
         scannerConfiguration.setPath("testres/extender");
         scannerConfiguration.setSource(ScannerConfiguration.APISource.SOURCECODE);
         scannerConfiguration.setSourceVersion("1.7");
         scannerConfiguration.setId("extender");
 
         APIScanner scanner = new SourceScanner();
         scanner.setConfiguration(scannerConfiguration);
 
         api = scanner.scan();
 
         // delete output files from previous run of whiteListTest2
         TestUtils.delete(new File("output/tests/extender"));
     }
 
     /**
      * Test of generating simple constructor for extender class
      */
     @Test
     public void TestConstructors() {
 
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.ClassA.ClassA");
         job.addWhitelistRule(r);
 
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/ClassAExtender.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/extender_exp/ClassAExtender.java");
 
         FileAssert.assertEquals(expected, resultFile);
 
     }
 
     @Test
     public void TestMethods() {
 
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.ClassB");
         job.addWhitelistRule(r);
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/ClassBExtender.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/extender_exp/ClassBExtender.java");
 
         FileAssert.assertEquals(expected, resultFile);
 
     }
 
     @Test
     public void TestGenerics() {
 
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.Box");
         job.addWhitelistRule(r);
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/BoxExtender.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/extender_exp/BoxExtender.java");
 
         FileAssert.assertEquals(expected, resultFile);
 
     }
 
     @Test
     public void TestFields1() {
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.Fields1");
         job.addWhitelistRule(r);
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/Fields1Extender.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/extender_exp/Fields1Extender.java");
 
         FileAssert.assertEquals(expected, resultFile);
     }
 
     @Test
     public void TestFields2() {
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.Fields2");
         job.addWhitelistRule(r);
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/Fields2Extender.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/extender_exp/Fields2Extender.java");
 
         FileAssert.assertEquals(expected, resultFile);
     }
 
     @Test
     public void TestFields3() {
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.Fields3");
         job.addWhitelistRule(r);
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/Fields3Extender.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/extender_exp/Fields3Extender.java");
 
         FileAssert.assertEquals(expected, resultFile);
     }
 
     @Test
     public void TestFields4() {
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.Fields4");
         job.addWhitelistRule(r);
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/Fields4Extender.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/extender_exp/Fields4Extender.java");
 
         FileAssert.assertEquals(expected, resultFile);
     }
 
     @Test
     public void TestAbstract() {
 
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.AbstractClass");
         job.addWhitelistRule(r);
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/AbstractClassExtender.java");
         assertTrue(resultFile.exists());
 
         File expected = new File("testres/extender_exp/AbstractClassExtender.java");
 
         FileAssert.assertEquals(expected, resultFile);
 
     }
 
     @Test
     public void TestPrivateConstr() {
 
         Generator generator = new ExtenderGenerator(new GeneratorConfiguration());
         GeneratorJobConfiguration job = new GeneratorJobConfiguration();
         job.setApiId("extender");
         job.setOutputDir("output/tests/extender");
         job.setOutputPackage("test.%s");
         WhitelistRule r = new WhitelistRule();
         r.setItem(WhitelistRule.RuleItem.EXTENDER);
         r.setRule("lib.PrivateConstr");
         job.addWhitelistRule(r);
         generator.generate(api, job);
 
         File resultFile = new File("output/tests/extender/test/lib/PrivateConstrExtender.java");
 
         // Extender of a class with only private constructor can't be generated.
         assertFalse(resultFile.exists());
 
     }
 }
