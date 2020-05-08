 package dcll.grp3.morphoQuizz;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;

import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.custommonkey.xmlunit.XMLTestCase;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class QuizzConverterTest extends XMLTestCase {
 	
 	protected String[] filesNames = { "quiz-moodle-exemple.xml", "quiz-moodle-exemple.json" };
 	protected File testFolder, testInput, testOutput, testOutput2;
 	protected String ext;
 	
 	public QuizzConverterTest (String testName) 
 	{
 		super(testName);
 	}
 	
 	public static Test suite()
     {
         return new TestSuite(QuizzConverterTest.class);
     }
 	
 	 public void testQuizzConverter()
 	 {
 		 assertTrue(true);
 	 }
 	 
 	 public void setUp() throws Exception {
 		 super.setUp();
 		 System.out.println("SetUp");
 
 		 //creation du dossier data
 		 testFolder = new File("data/tmp");
 		 testFolder.mkdir(); 
 		 
 		 
 	 }
 
 	 public void tearDown() throws Exception {
 		 super.tearDown();
 		 System.out.println("TearDown");
		 
		 //suppression des fichiers de tests		 
		 FileUtils.deleteDirectory(testFolder);
		 
 	 }
 	 
 	public void testXMLQuizzToJson ()
 			throws Exception {
 		System.out.println("testXMLQuizzToJson");
 		 //selection des fichiers xml dans data tmp
 		 for (int i=0; i<filesNames.length; i++) {
 			 testInput = new File("data/" + filesNames[i]);
 			 testOutput = new File(testFolder.getPath() + "/" + filesNames[i].substring(0,filesNames[i].indexOf(".")) + ".json");
 			 testOutput2 = new File(testFolder.getPath() + "/" + filesNames[i].substring(0,filesNames[i].indexOf(".")) + "2.xml");
 			 InputStream original = new FileInputStream(testInput);
 			 InputStream converti = new FileInputStream(testOutput2);
 			 if (testInput.getName().lastIndexOf(".") > 0) {
 				 ext = testInput.getName().substring(testInput.getName().lastIndexOf("."));
 				 //traitement selon l'extension
 				 if (ext.equals(".xml")) {
 					 QuizzConverter.xmlQuizzToJson(testInput, testOutput);
 					 assertTrue(testOutput.exists());
 				 }
 				 //verifie l'egalite entre 2 fichiers
 				 if (ext.equals(".json")) {
 					 QuizzConverter.jsonQuizzToXML(testOutput, testOutput2);
 					 String myControlXML = "<msg><uuid>0x00435A8C</uuid></msg>";
 					 String myTestXML = "<msg><uuid>0x00435A8C</uuid></msg>";
 					 assertXMLEqual(myControlXML, myTestXML);
 				 }
 				 assertXMLEqual(IOUtils.toString(original), IOUtils.toString(converti));
 			 }
 		 }
 	}
 	
 	public void testJsonQuizzToXML ()
 			throws Exception {
 		System.out.println("testJsonQuizzToXML");
 		//selection des fichiers json dans data tmp
 		 
 		 testInput = new File("data/quiz-moodle-exemple.json");
 		 testOutput = new File(testFolder.getPath() + "/testJSON2XML_json2xml.xml");
 		 testOutput2 = new File(testFolder.getPath() + "/testJSON2XML_xml2json.json");
 		 
 		 System.out.println("conversion JSON à XML");
 		 QuizzConverter.jsonQuizzToXML(testInput, testOutput);
 		 assertTrue("Conversion JSON en XML faite", testOutput.exists());
 		 
 		 System.out.println("reconversion XML à JSON");
 		 QuizzConverter.xmlQuizzToJson(testOutput, testOutput2);
 		 assertTrue("Reconversion XML en JSON faite", testOutput2.exists());
 		 
 		 
		 FileInputStream inputStream = new FileInputStream(testInput);
		 FileInputStream outputStream = new FileInputStream(testOutput2);
		 String inputJSON = IOUtils.toString(inputStream);
		 String outputJSON = IOUtils.toString(outputStream);
 		 
 		 //nettoyage des JSON
 		 inputJSON = inputJSON.replaceAll("[\r\n\t]+", "");
 		 outputJSON = outputJSON.replaceAll("[\r\n\t]+", "");
 		 
 		 assertEquals("JSON origine et JSON générés egaux", inputJSON, outputJSON);
 		 
		 inputStream.close();
		 outputStream.close();
		 
 	}
 	
 	
 
 }
