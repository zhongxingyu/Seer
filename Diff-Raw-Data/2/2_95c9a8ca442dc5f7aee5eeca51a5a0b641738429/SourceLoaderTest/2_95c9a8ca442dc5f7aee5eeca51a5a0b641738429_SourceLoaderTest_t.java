 package test.info.plagiatsjaeger;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import info.plagiatsjaeger.SourceLoader;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class SourceLoaderTest
 {
 
 	@Before
 	public void setUp() throws Exception
 	{
 	}
 
 	@Test
 	public void testLoadURL()
 	{
 
 		String ergebnisstring1 = "testseite Hallo ich bin ein Body";
 		// String ergebnisstring2 =
 		// "Das B�se � Wikipedia Das B�se aus Wikipedia, der freien Enzyklop�die Wechseln zu: Navigation, Suche Dieser Artikel behandelt den philosophischen Begriff";
 
 		String teststring1 = "http://192.168.4.28/testfiles/testseite.html";
 		String teststring2 = "http://www.assdsdadasdasdasda.de";
 		//String teststring3 = "httpxyz://www.plagiatsjaeger.de";
 		String teststring4 = "";
 		// String teststring5 =
 		// "www.plagiatsjaeger.info/projektplan/projektteam/";
 		// String teststring6 = "plagiatsjaeger.info/projektplan/projektteam/";
 		// String teststring7 = "http://de.wikipedia.org/wiki/Das_B�se";
 
 		assertEquals(ergebnisstring1, SourceLoader.loadURL(teststring1, true, false));
 		// assertTrue(SourceLoader.loadURL(teststring7).contains(ergebnisstring2));
 		assertTrue(SourceLoader.loadURL(teststring2).contains("FAIL IOException"));
 		// System.out.println(SourceLoader.loadURL(teststring3));
 		//assertTrue(SourceLoader.loadURL(teststring3).contains("FAIL MalformedURLException"));
 		System.out.println(SourceLoader.loadURL(teststring4));
		assertTrue(SourceLoader.loadURL(teststring4).contains("FAIL IOException"));
 		// assertTrue(SourceLoader.loadURL(teststring5).contains("FAIL MalformedURLException"));
 		// assertTrue(SourceLoader.loadURL(teststring6).contains("FAIL MalformedURLException"));
 
 	}
 
 	@Test
 	public void testLoadFile()
 	{
 
 		// String ergebnisstring1 = "";
 		// String ergebnisstring2 = "Hallo, ich bin ein/nZeilenumbruch";
 		// String ergebnisstring3 = "Gesperrtes File";
 		// // String ergebnisstring4 =
 		// // "D�ner mit So�e & einer b�rigen t�rkischen Bananen & Co KG";
 		//
 		// assertTrue(SourceLoader.loadFile("/var/www/testfiles/testfile3.txt").contains("FAIL FileNotFoundException"));
 		// assertEquals(ergebnisstring1,
 		// SourceLoader.loadFile("/var/www/testfiles/testfile1.txt"));
 		// assertEquals(ergebnisstring2,
 		// SourceLoader.loadFile("/var/www/testfiles/fehlendesfile.txt"));
 		// assertTrue(SourceLoader.loadFile("/var/www/testfiles/testfile3.txt").contains("FAIL IOException"));
 		// // assertEquals(ergebnisstring4,
 		// // SourceLoader.loadFile("/var/www/testfiles/testfile4.txt"));
 
 	}
 
 }
