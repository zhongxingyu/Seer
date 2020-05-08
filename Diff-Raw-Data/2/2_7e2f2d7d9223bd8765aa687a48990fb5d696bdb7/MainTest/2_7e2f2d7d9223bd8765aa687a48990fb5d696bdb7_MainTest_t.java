 package net.codjo.ontology.generator;
 import static net.codjo.ontology.generator.RelationshipManager.that;
 import net.codjo.test.common.PathUtil;
 import net.codjo.util.file.FileUtil;
 import java.io.File;
 import junit.framework.TestCase;
 /**
  *
  */
 public class MainTest extends TestCase {
     public void test_full() throws Exception {
         File basedir = PathUtil.findBaseDirectory(getClass());
         File outputdir = new File(basedir, "target/sample-sources");
         outputdir.delete();
 
         Main.main(new String[]{new File(basedir, "src/test/sample/configuration.xml").getAbsolutePath()});
 
         assertTrue(new File(outputdir, "xsd").exists());
         assertTrue(new File(outputdir, "xsd/sample.xsd").exists());
         assertTrue(new File(outputdir, "java/net/codjo/fubar").exists());
 
         File ontologyFile = new File(outputdir, "java/net/codjo/fubar/JadeSampleOntology.java");
         assertTrue(ontologyFile.exists());
         assertTrue(FileUtil.loadContent(ontologyFile).contains("class JadeSampleOntology"));
     }
 
 
     public void test_noConfigurationFile() throws Exception {
         try {
             Main.main(new String[]{"bad/path/sample.pont"});
             fail();
         }
         catch (IllegalArgumentException ex) {
             assertEquals("Fichier de configuration introuvable : bad/path/sample.pont",
                         ex.getLocalizedMessage().replaceAll("\\\\", "/"));
         }
     }
 
 
     public void test_loadArguments() throws Exception {
         OntologyConfiguration configuration = Main.load(PathUtil.find(MainTest.class, "MainTest.xml"));
         assertNotNull(configuration);
 
         assertEquals(new File("src/main/protege/sample.pont"), configuration.getProtegeFile());
 
         assertEquals("product", configuration.getName());
 
         assertEquals("net.codjo.fubar", configuration.getGeneration().getPackageName());
         assertEquals(new File("./target/generated-sources/"), configuration.getGeneration().getOutputDirectory());
         assertEquals("maRacine", configuration.getGeneration().getXsdRootNode());
     }
 
 
     public void test_loadArguments_relationShip() throws Exception {
         OntologyConfiguration configuration = Main.load(PathUtil.find(MainTest.class, "MainTest.xml"));
 
         RelationshipManager actual = configuration.getRelationships();
         assertNotNull(actual);
 
         RelationshipManager expected = RelationshipManager.create()
               .define(that("GetProductAction").returnsOne("GetProductResponse"));
 
         assertEquals(Main.createXstream().toXML(expected),
                      Main.createXstream().toXML(actual));
     }
 }
