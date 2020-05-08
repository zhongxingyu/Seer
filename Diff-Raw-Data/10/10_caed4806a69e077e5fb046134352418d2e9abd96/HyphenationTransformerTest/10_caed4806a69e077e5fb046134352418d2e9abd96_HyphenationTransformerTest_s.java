 package ch.sbs;
 
 import static org.junit.Assert.*;
 
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.UnsupportedCharsetException;
 
 import javax.xml.stream.XMLStreamException;
 
 import org.custommonkey.xmlunit.Diff;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 public class HyphenationTransformerTest {
 	
 	static {
 		XMLUnit.setIgnoreWhitespace(true);
 	}
 	
 	@Test
 	public void testMisc()
 			throws UnsupportedCharsetException, FileNotFoundException, XMLStreamException, IOException, SAXException {
 
 		String correctlyHyphenated = "<dtbook version=\"2005-3\"" +
 									 "        xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\"" +
 									 "        xml:lang=\"de-DE\">" +
 									 "  <book>" +
 									 "    <bodymatter>" +
 									 "      <level1>" +
 									 "        <h1>Dampfschiff</h1>" +
 									 "        <p>Dampf\u00ADschiff</p>" +
 									 "        <p>Dampf-\u00ADschiff</p>" +
 									 "        <p><a>www.dampfschiff.ch</a></p>" +
 									 "      </level1>" +
 									 "    </bodymatter>" +
 									 "  </book>" +
 									 "</dtbook>";
 		
 		checkHyphenation(correctlyHyphenated);
 	}
 	
 	@Test
 	public void testVeryLongWord()
 			throws UnsupportedCharsetException, FileNotFoundException, XMLStreamException, IOException, SAXException {
 
 		String correctlyHyphenated = "<dtbook version=\"2005-3\"" +
 									 "        xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\"" +
 									 "        xml:lang=\"de-DE\">" +
 									 "  <book>" +
 									 "    <bodymatter>" +
 									 "      <level1>" +
									 "        <p>EinSehrLan­ge­sWor­tMitStud­lyCapsDassSichÜ­berMeh­re­reZei­lenHin­weg­ziehtUn­dZu­demEin­fachNochWahn­sin­nigGutAus­sieht</p>" +
 									 "      </level1>" +
 									 "    </bodymatter>" +
 									 "  </book>" +
 									 "</dtbook>";
 		
 		checkHyphenation(correctlyHyphenated);
 	}
 	
 	@Test
 	public void testLargeFile()
 			throws UnsupportedCharsetException, XMLStreamException, SAXException, IOException, URISyntaxException {
 		
 		String largeFile = getClass().getResource("/ch/sbs/resources/Franz Kafka - Der Prozeß.xml").getFile();
 		String largeHyphenatedFile = getClass().getResource("/ch/sbs/resources/Franz Kafka - Der Prozeß [hyphenated].xml").getFile();
 		StringWriter hyphenated = new StringWriter();
 		new HyphenationTransformer().transform(new FileReader(new URI(largeFile).getPath()), hyphenated);
 		hyphenated.flush();
 	    Diff myDiff = new Diff(new StringReader(hyphenated.toString()), new FileReader(new URI(largeHyphenatedFile).getPath()));
 	    
 	    assertTrue("not equal\n" + myDiff, myDiff.identical());
 	}
 	
 	/**
 	 * Check hyphenation by first removing all soft hyphens and then inserting them again
 	 *   with HyphenationTransformer
 	 * @param correctlyHyphenated
 	 */
 	public static void checkHyphenation(String correctlyHyphenated)
 			throws UnsupportedCharsetException, XMLStreamException, SAXException, IOException {
 		
 		String unhyphenated = correctlyHyphenated.replaceAll("\u00AD", "");
 		checkHyphenation(unhyphenated, correctlyHyphenated);
 	}
 	
 	public static void checkHyphenation(String unhyphenated, String correctlyHyphenated)
 			throws UnsupportedCharsetException, XMLStreamException, SAXException, IOException {
 		
 		StringWriter hyphenated = new StringWriter();
 		new HyphenationTransformer().transform(new StringReader(unhyphenated), hyphenated);
 		hyphenated.flush();
 	    Diff myDiff = new Diff(correctlyHyphenated, hyphenated.toString());
         
         assertTrue("not equal\n" + myDiff, myDiff.identical());
 	}
 }
