 /*
  * JBoss, the OpenSource J2EE webOS
  * 
  * Distributable under LGPL license.
  * See terms of license at gnu.org.
  */
 package org.fedorahosted.openprops;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Reader;
 
 import org.fedorahosted.openprops.Properties;
 
 import junit.framework.TestCase;
 
 @SuppressWarnings("nls")
 public class PropertiesTest extends TestCase {
     
     public void testGetSetComment() throws Exception {
 	Properties beforeProps = new Properties();
 	String beforeComment = "comment";
 	beforeProps.setComment("key", beforeComment);
 	beforeProps.setProperty("key", "value");
 	ByteArrayOutputStream out = new ByteArrayOutputStream();
 	beforeProps.store(out, null);
 	Properties afterProps = new Properties();
 	afterProps.load(new ByteArrayInputStream(out.toByteArray()));
 	assertEquals(beforeComment, afterProps.getComment("key"));
     }
     
     public void testBasic() throws Exception {
 	roundTripForTestName();
     }
 
     public void testNewlines() throws Exception {
 	roundTripForTestName();
     }
     
     public void testNewlinesInValues() throws Exception {
 	roundTripForTestName();
     }
 
     public void testComplex() throws Exception {
 	roundTripForTestName();
     }
 
     public void testEmpty() throws Exception {
 	roundTripForTestName();
     }
     
     public void testLoneComment() throws Exception {
 	roundTripForTestName();
     }
     
     public void testBackslashEscapingInComment() throws Exception {
 	roundTripForTestName();
     }
     
     public void testTrailingBackslashInComment() throws Exception {
 	roundTripForTestName();
     }
     
     public void testWhitespaceBeforeComment() throws Exception {
 	roundTripForTestName();
     }
 
    public void testLastLineWhitespace() throws Exception {
	roundTripForTestName();
    }

     public void testAsymmetrical() throws Exception {
 	checkAsymmetrical("asymmetricalExpected.properties", "asymmetricalInput.properties");
     }
     
     public void testEmbeddedTabs() throws Exception {
 	checkAsymmetrical("embeddedTabsExpected.properties", "embeddedTabsInput.properties");
     }
     
     public void testLineNumber() throws Exception {
 	Properties props = new Properties();
 	props.load(getData(getPropertiesName()));
 	assertEquals(2, props.getLineNumber("keyOnLine2"));
 	assertEquals(3, props.getLineNumber("keyOnLine3"));
 	assertEquals(5, props.getLineNumber("keyOnLine5"));
     }
     
     /**
      * This method is used when the expected end result is expected to be 
      * different (in string terms) from the original input file.
      * 
      * @param expectedName
      * @param inputName
      * @throws IOException
      */
     private void checkAsymmetrical(String expectedName, String inputName) throws IOException {
 	assertEquivalentProperties(expectedName, inputName);
 	checkRoundTripResult(expectedName, inputName);
     }
 
     /**
      * Runs a round-trip test for the properties file whose name matches the running test.
      * 
      * @throws IOException
      * @throws FileNotFoundException
      */
     private void roundTripForTestName() throws IOException, FileNotFoundException {
 //	System.out.println(getName());
 	checkRoundTripResult(getPropertiesName(), getPropertiesName());
 	assertEquivalentProperties(getPropertiesName(), getPropertiesName());
     }
     
     /**
      * Reads inputName (a .properties file) into a Properties object and 
      * writes it out to another .properties file (ie a round trip), and 
      * checks that the content matches the file expectedName. 
      * 
      * @param expectedName name of a .properties file with the expected result
      * @param inputName name of a .properties file to go through a round trip
      * @throws IOException
      * @throws FileNotFoundException
      */
     private void checkRoundTripResult(String expectedName, String inputName) throws IOException, FileNotFoundException {
 	Properties props = new Properties();
 	InputStream orig = getData(inputName);
 	props.load(orig);
 	orig.close();
 	File tempFile = File.createTempFile("PropertiesTest", "_tmp.properties");
 //	File tempFile = new File("test", getPropertiesName());
 	tempFile.deleteOnExit();
 	OutputStream tempOut = new FileOutputStream(tempFile);	
 	props.store(tempOut, null);
 	tempOut.close();
 	InputStream tempIn = new FileInputStream(tempFile);
 	InputStream expected = getData(expectedName);
 	assertEqualData(expected, tempIn);
 	expected.close();
 	tempIn.close();
     }
 
     /**
      * Checks that two .properties files are equivalent, including comments/whitespace, but not 
      * including encoding differences such as escaping of embedded tabs.
      * 
      * @param expectedName
      * @param inputName
      * @throws IOException
      */
     private void assertEquivalentProperties(String expectedName, String inputName) throws IOException {
 	InputStream expected = getData(expectedName);
 	Properties exp = readAsProps(expected);
 	String expStr = propsToXML(exp);
 	InputStream input = getData(inputName);
 	Properties inp = readAsProps(input);
 	String inpStr = propsToXML(inp);
 	assertEquals("expected test results "+expectedName+" may be out of date", 
 		expStr, inpStr);
 //	System.out.println(expStr);
 	input.close();
 	expected.close();
     }
     
 //    private String propsToString(Properties props) throws IOException {
 //	StringWriter writer = new StringWriter();
 //	props.store(writer, null);
 //	return writer.toString();
 //    }
     
     private String propsToXML(Properties props) throws IOException {
 	ByteArrayOutputStream out = new ByteArrayOutputStream();
 	props.storeToXML(out, null);
 	return out.toString();
     }
     
     private Properties readAsProps(InputStream stream) throws IOException {
 	Properties props = new Properties();
 	props.load(stream);
 	return props;
     }
 
     private void assertEqualData(InputStream expected, InputStream actual) throws IOException {
 	assertEquals(readasString(expected), readasString(actual));
     }
 
     private String readasString(InputStream stream) throws IOException {
 	Reader reader = new InputStreamReader(stream);
 	StringBuilder sb = new StringBuilder();
 	int ch;
 	while ((ch = reader.read()) != -1)
 	    sb.append((char)ch);
 	return sb.toString();
     }
 
     private InputStream getData(String name) {
 	InputStream stream = getClass().getResourceAsStream(name);
 	assertNotNull("test file "+name+" not found", stream);
 	return stream;
     }
 
     private String getPropertiesName() {
 	return getName()+".properties";
     }
 }
