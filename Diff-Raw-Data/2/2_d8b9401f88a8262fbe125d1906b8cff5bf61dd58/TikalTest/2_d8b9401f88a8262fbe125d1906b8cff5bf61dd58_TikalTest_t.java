 package net.sf.okapi.applications.tikal;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import net.sf.okapi.common.FileCompare;
 import net.sf.okapi.common.ZipFileCompare;
 import net.sf.okapi.common.StreamGobbler;
 import net.sf.okapi.common.Util;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class TikalTest {
 	
 	private String javaTikal;
 	private String root;
 	private File rootAsFile;
 	private FileCompare fc = new FileCompare();
 	private ZipFileCompare zfc = new ZipFileCompare();
 
 	@Before
 	public void setUp () throws URISyntaxException {
 		File file = new File(getClass().getResource("/htmltest.html").toURI());
 		root = Util.getDirectoryName(file.getAbsolutePath());
 		rootAsFile = new File(root);
 
 		String distDir;
 		String osName = System.getProperty("os.name");
 		if ( osName.startsWith("Mac OS") ) { // Macintosh case
 			distDir = "dist_cocoa-macosx";
 			//TODO: How to detect carbon vs cocoa?
 		}
 		else if ( osName.startsWith("Windows") ) { // Windows case
 			distDir = "dist_win32-x86";
 		}
 		else { // Assumes Unix or Linux
 			if ( System.getProperty("os.arch").equals("x86_64") || System.getProperty("os.arch").equals("amd64") ) {
 				distDir = "dist_gtk2-linux-x86_64";
 			}
 			else {
 				distDir = "dist_gtk2-linux-x86";
 			}
 		}
 		
 		// Set the path for the jar
 		String libDir = Util.getDirectoryName(root); // Go up one dir
 		libDir = Util.getDirectoryName(libDir); // Go up one dir
 		libDir = Util.getDirectoryName(libDir); // Go up one dir
 		libDir = Util.getDirectoryName(libDir); // Go up one dir
 		libDir += String.format("%sdeployment%smaven%s%s%slib%s",
 			File.separator, File.separator, File.separator, distDir, File.separator, File.separator);
		javaTikal = "java -jar \"" + libDir + "tikal.jar\"";
 	}
 	
     @Test
     public void testSimpleCall () throws IOException, InterruptedException {
     	// Simple run, no arguments
     	assertEquals(0, runTikal(null));
     }
 
     @Test
     public void testHelpCall () throws IOException, InterruptedException {
     	// Simple run, one invalid argument
     	assertEquals(0, runTikal("-?"));
     }
 
     @Test
     public void testExtractMergeDTD () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("dtdtest.dtd.xlf"));
     	assertTrue(deleteOutputFile("dtdtest.out.dtd"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr dtdtest.dtd -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("dtdtest.dtd.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr dtdtest.dtd.xlf -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("dtdtest.out.dtd", "windows-1252"));
     }
 
     @Test
     public void testExtractMergeHTML () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("htmltest.html.xlf"));
     	assertTrue(deleteOutputFile("htmltest.out.html"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr htmltest.html -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("htmltest.html.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr htmltest.html.xlf -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("htmltest.out.html", "windows-1252"));
     }
 
     @Test
     public void testExtractMergeJSON () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("jsontest.json.xlf"));
     	assertTrue(deleteOutputFile("jsontest.out.json"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr jsontest.json -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("jsontest.json.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr jsontest.json.xlf -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("jsontest.out.json", "windows-1252"));
     }
 
     @Test
     public void testExtractMergePO () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("potest.po.xlf"));
     	assertTrue(deleteOutputFile("potest.out.po"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr potest.po -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr potest.po.xlf -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.out.po", "windows-1252"));
     }
 
     @Test
     public void testExtractMergePOMono () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("potest-mono.po.xlf"));
     	assertTrue(deleteOutputFile("potest-mono.out.po"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr potest-mono.po -fc okf_po-monolingual -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("potest-mono.po.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr potest-mono.po.xlf -fc okf_po-monolingual -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("potest-mono.out.po", "windows-1252"));
     }
 
     @Test
     public void testExtractMergeProperties () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("proptest.properties.xlf"));
     	assertTrue(deleteOutputFile("proptest.out.properties"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr proptest.properties -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("proptest.properties.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr proptest.properties.xlf -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("proptest.out.properties", "windows-1252"));
     }
 
     @Test
     public void testExtractMergePropertiesNoEscapes () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("proptest.properties.xlf"));
     	assertTrue(deleteOutputFile("proptest.out.noesc.properties"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr proptest.properties -fc okf_properties-outputNotEscaped -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("proptest.properties.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr proptest.properties.xlf -fc okf_properties-outputNotEscaped -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("proptest.out.properties", "proptest.out.noesc.properties", "windows-1252"));
     }
 
     @Test
     public void testExtractMergeODT () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("odttest.odt.xlf"));
     	assertTrue(deleteOutputFile("odttest.out.odt"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr odttest.odt -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("odttest.odt.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr odttest.odt.xlf"));
     	assertTrue("File different from gold", compareZipWithGoldFile("odttest.out.odt"));
     }
 
     @Test
     public void testExtractMergeDOCX () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("docxtest.docx.xlf"));
     	assertTrue(deleteOutputFile("docxtest.out.docx"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr docxtest.docx -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("docxtest.docx.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr docxtest.docx.xlf"));
     	assertTrue("File different from gold", compareZipWithGoldFile("docxtest.out.docx"));
     }
 
     @Test
     public void testExtractSegmentMergeDOCX () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("docxsegtest.docx.xlf"));
     	assertTrue(deleteOutputFile("docxsegtest.out.docx"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr docxsegtest.docx -seg -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("docxsegtest.docx.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr docxsegtest.docx.xlf"));
     	assertTrue("File different from gold", compareZipWithGoldFile("docxsegtest.out.docx"));
     }
 
     @Test
     public void testExtractMergeTMX () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("tmxtest-attributes.tmx.xlf"));
     	assertTrue(deleteOutputFile("tmxtest-attributes.tmx.out.po"));
     	// Extract
     	assertEquals(0, runTikal("-x tmxtest-attributes.tmx -sl EN-US -tl FR-FR -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("tmxtest-attributes.tmx.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m tmxtest-attributes.tmx.xlf -sl EN-US -tl FR-FR -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("tmxtest-attributes.out.tmx", "windows-1252"));
     }
 
     @Test
     public void testExtractMergeIDML () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("idmltest.idml.xlf"));
     	assertTrue(deleteOutputFile("idmltest.out.idml"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr idmltest.idml -ie UTF-8"));
     	assertTrue("File different from gold", compareWithGoldFile("idmltest.idml.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr idmltest.idml.xlf"));
     	assertTrue("File different from gold", compareZipWithGoldFile("idmltest.out.idml"));
     }
 
     @Test
     public void testExtractMergeTS () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("tstest.ts.xlf"));
     	assertTrue(deleteOutputFile("tstest.out.ts"));
 
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr tstest.ts -sl EN-US -tl FR-FR -ie UTF-8"));
     	assertTrue("File different from gold", compareWithGoldFile("tstest.ts.xlf", "UTF-8"));
   	
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr tstest.ts.xlf -sl EN-US -tl FR-FR -oe UTF-8"));
     	assertTrue("File different from gold", compareWithGoldFile("tstest.out.ts", "windows-1252"));
     }
     
     @Test
     public void testExtractMergeXLIFF () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("xlifftest.xlf.xlf"));
     	assertTrue(deleteOutputFile("xlifftest.out.xlf"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr xlifftest.xlf"));
     	assertTrue("File different from gold", compareWithGoldFile("xlifftest.xlf.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr xlifftest.xlf.xlf -oe windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("xlifftest.out.xlf", "UTF-8"));
     }
 
     @Test
     public void testExtractMergeXML () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("xmltest1.xml.xlf"));
     	assertTrue(deleteOutputFile("xmltest1.out.xml"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr xmltest1.xml -ie UTF-8"));
     	assertTrue("File different from gold", compareWithGoldFile("xmltest1.xml.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr xmltest1.xml.xlf -oe UTF-8"));
     	assertTrue("File different from gold", compareWithGoldFile("xmltest1.out.xml", "UTF-8"));
     }
 
     @Test
     public void testExtractMergeStreamedXML () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("xmltest3.xml.xlf"));
     	assertTrue(deleteOutputFile("xmltest3.out.xml"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr xmltest3.xml -fc okf_xmlstream -ie UTF-8"));
     	assertTrue("File different from gold", compareWithGoldFile("xmltest3.xml.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr xmltest3.xml.xlf -fc okf_xmlstream -oe UTF-8"));
     	assertTrue("File different from gold", compareWithGoldFile("xmltest3.out.xml", "UTF-8"));
     }
 
     @Test
     public void testExtractMergeTSV () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("tsvtest.txt.xlf"));
     	assertTrue(deleteOutputFile("tsvtest.out.txt"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr tsvtest.txt -ie windows-1252"));
     	assertTrue("File different from gold", compareWithGoldFile("tsvtest.txt.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr tsvtest.txt.xlf -oe UTF-8"));
     	assertTrue("File different from gold", compareWithGoldFile("tsvtest.out.txt", "UTF-8"));
     }
 
     @Test
     public void testExtractMergeRESX () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("resxtest.resx.xlf"));
     	assertTrue(deleteOutputFile("resxtest.out.resx"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr resxtest.resx")); // Auto-assign okf_xml-resx
     	assertTrue("File different from gold", compareWithGoldFile("resxtest.resx.xlf", "UTF-8"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr resxtest.resx.xlf -ie windows-1252")); // Auto-assign okf_xml-resx
     	assertTrue("File different from gold", compareWithGoldFile("resxtest.out.resx", "windows-1252"));
     }
 
     @Test
     public void testExtractMergeSRT () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("srttest.srt.xlf"));
     	assertTrue(deleteOutputFile("srttest.out.srt"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr srttest.srt -ie windows-1252")); // Auto-assign okf_regex-srt
     	assertTrue("File different from gold", compareWithGoldFile("srttest.srt.xlf", "windows-1252"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr srttest.srt.xlf -ie UTF-8")); // Auto-assign okf_regex-srt
     	assertTrue("File different from gold", compareWithGoldFile("srttest.out.srt", "UTF-8"));
     }
 
     @Test
     public void testExtractMergeYAML () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("yamltest.yml.xlf"));
     	assertTrue(deleteOutputFile("yamltest.out.yml"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr yamltest.yml -ie UTF-8")); // Auto-assign okf_railsyaml
     	assertTrue("File different from gold", compareWithGoldFile("yamltest.yml.xlf", "windows-1252"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr yamltest.yml.xlf -ie UTF-8")); // Auto-assign okf_railsyaml
     	assertTrue("File different from gold", compareWithGoldFile("yamltest.out.yml", "UTF-8"));
     }
 
     @Test
     public void testExtractMergeVersified () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("versifiedtest.txt.xlf"));
     	assertTrue(deleteOutputFile("versifiedtest.out.txt"));
     	// Extract
     	assertEquals(0, runTikal("-x -sl en -tl fr versifiedtest.txt -ie UTF-8 -fc okf_versifiedtxt"));
     	assertTrue("File different from gold", compareWithGoldFile("versifiedtest.txt.xlf", "windows-1252"));
     	// Merge
     	assertEquals(0, runTikal("-m -sl en -tl fr versifiedtest.txt.xlf -ie UTF-8 -fc okf_versifiedtxt"));
     	assertTrue("File different from gold", compareWithGoldFile("versifiedtest.out.txt", "UTF-8"));
     }
 
     @Test
     public void testImportExportPensieve () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputDir("pensieveTM.pentm", true));
     	// Import
     	assertEquals(0, runTikal("-imp pensieveTM.pentm tmxtest-attributes.tmx -sl EN-US -tl FR-FR"));
     	// Check if we can query the TM (does not check the result)
     	assertEquals(0, runTikal("-q \"One entry in the TM.\" -pen pensieveTM.pentm -sl EN-US -tl FR-FR"));
     	
     	// Export now
     	assertTrue(deleteOutputFile("pensieveTM.tmx"));
     	assertEquals(0, runTikal("-2tmx pensieveTM.pentm -fc okf_pensieve -sl EN-US -tl FR-FR"));
     	assertTrue("File different from gold", compareWithGoldFile("pensieveTM.pentm.tmx", "UTF-8"));
 
     	// Export again, using -exp 
     	assertTrue(deleteOutputFile("pensieveTM.tmx"));
     	assertEquals(0, runTikal("-exp pensieveTM.pentm -sl EN-US -tl FR-FR"));
     	assertTrue("File different from gold", compareWithGoldFile("pensieveTM.pentm.tmx", "UTF-8"));
     }
 
     @Test
     public void testConvertToTMX () throws IOException, InterruptedException {
     	// Test normal conversion
     	assertTrue(deleteOutputFile("potest.po.tmx"));
     	assertEquals(0, runTikal("-2tmx -sl en -tl fr potest.po"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.tmx", "potest.po.normal.tmx", "UTF-8"));
     	// Test normal conversion (target=source)
     	assertTrue(deleteOutputFile("potest.po.tmx"));
     	assertEquals(0, runTikal("-2tmx -sl en -tl fr potest.po -trgsource"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.tmx", "potest.po.source.tmx", "UTF-8"));
     	// Test normal conversion (target=empty)
     	assertTrue(deleteOutputFile("potest.po.tmx"));
     	assertEquals(0, runTikal("-2tmx -sl en -tl fr potest.po -trgempty"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.tmx", "potest.po.empty.tmx", "UTF-8"));
     }
 
     @Test
     public void testConvertToTable () throws IOException, InterruptedException {
     	// Test csv conversion
     	assertTrue(deleteOutputFile("potest.po.txt"));
     	assertEquals(0, runTikal("-2tbl potest.po -csv -all"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.txt", "potest.po.normal.csv", "UTF-8"));
     	// Test csv conversion (codes=generic)
     	assertTrue(deleteOutputFile("potest.po.txt"));
     	assertEquals(0, runTikal("-2tbl potest.po -csv -generic -all"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.txt", "potest.po.generic.csv", "UTF-8"));
     	// Test csv conversion (codes=tmx)
     	assertTrue(deleteOutputFile("potest.po.txt"));
     	assertEquals(0, runTikal("-2tbl potest.po -csv -tmx -all"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.txt", "potest.po.tmx.csv", "UTF-8"));
     	// Test table conversion
     	assertTrue(deleteOutputFile("potest.po.txt"));
     	assertEquals(0, runTikal("-2tbl potest.po -tab"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.txt", "potest.po.normal.tab", "UTF-8"));
     	// Test table conversion (codes=generic)
     	assertTrue(deleteOutputFile("potest.po.txt"));
     	assertEquals(0, runTikal("-2tbl potest.po -tab -generic -all"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.txt", "potest.po.generic.tab", "UTF-8"));
     	// Test table conversion (codes=xliffgx)
     	assertTrue(deleteOutputFile("potest.po.txt"));
     	assertEquals(0, runTikal("-2tbl potest.po -tab -xliffgx -all"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.po.txt", "potest.po.xliffgx.tab", "UTF-8"));
     }
 
     @Test
     public void testConvertToPO () throws IOException, InterruptedException {
     	// Test normal conversion
     	assertTrue(deleteOutputFile("tmxtest-attributes.tmx.po"));
     	assertEquals(0, runTikal("-2po tmxtest-attributes.tmx -sl EN-US -tl FR-FR"));
     	assertTrue("File different from gold", compareWithGoldFile("tmxtest-attributes.tmx.po", "UTF-8"));
     }
 
     @Test
     // Keep this one the last test
     public void testQuerySeveralConnectors () throws IOException, InterruptedException {
     	assertEquals(0, runTikal("-imp pensieveTM tmxtest-attributes.tmx -sl EN-US -tl FR-FR"));
     	// test local TM
     	assertEquals(0, runTikal("-q \"Close the <b>application</b>.\" -sl EN-US -tl FR-FR -pen pensieveTM"));
     	// Query several services
     	int res = runTikal("-q \"Close the <b>application</b>.\" -sl EN-US -tl FR-FR -pen pensieveTM -mm mmDemo123");
     	if ( res != 0 ) {
     		System.err.println("\n=============== WARNING ===============");
     		System.err.println("The test of querying several translation resources failed.");
     		System.err.println("This may be due to connection issues, or to real API problems with one of the connectors. Please check.");
     		System.err.println("=======================================");
     	}
     }
 
     @Test
     public void testTranslateXML () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("xmltest2out.xml"));
     	int res = runTikal("-t -sl en -tl eo xmltest2.xml -apertium myApertium.cfg -oe UTF-8 -trace");
     	if ( res == 0 ) {
     		assertTrue("File different from gold", compareWithGoldFile("xmltest2.out.xml", "UTF-8"));
     	}
     	else {
     		System.err.println("\n=============== WARNING ===============");
     		System.err.println("This test did not pass because the translation resources failed.");
     		System.err.println("This may be due to connection issues, or to real API problems with one of the connectors. Please check.");
     		System.err.println("=======================================");
     	}
     }
 
     
     
     private boolean compareZipWithGoldFile (String outputBase) {
     	
     	String outputPath = root + File.separator + outputBase;
     	String goldPath = root + File.separator + "gold" + File.separator + outputBase; 
     	System.out.println("Zipped file comparison");
     	System.out.println("Output File: "+outputPath);
     	System.out.println("Gold File: "+goldPath+"\n");
     	return zfc.compareFilesPerLines(outputPath, goldPath, "UTF-8", true);
     }
 
     
     private boolean compareWithGoldFile (String outputBase, String encoding) {
     	String outputPath = root + File.separator + outputBase;
     	String goldPath = root + File.separator + "gold" + File.separator + outputBase; 
     	return fc.compareFilesPerLines(outputPath, goldPath, encoding);
     }
     
     private boolean compareWithGoldFile (String outputBase,
     	String goldBase,
     	String encoding)
     {
     	String outputPath = root + File.separator + outputBase;
     	String goldPath = root + File.separator + "gold" + File.separator + goldBase; 
     	return fc.compareFilesPerLines(outputPath, goldPath, encoding);
     }
     
     private boolean deleteOutputFile (String filename) {
     	File f = new File(root + File.separator + filename);
     	if ( f.exists() ) {
     		return f.delete();
     	}
     	else return true;
     }
     
     private boolean deleteOutputDir (String dirname, boolean relative) {
     	File d;
     	if ( relative ) d = new File(root + File.separator + dirname);
     	else d = new File(dirname);
     	if ( d.isDirectory() ) {
     		String[] children = d.list();
     		for ( int i=0; i<children.length; i++ ) {
     			boolean success = deleteOutputDir(d.getAbsolutePath() + File.separator + children[i], false);
     			if ( !success ) {
     				return false;
     			}
     		}
     	}
     	if ( d.exists() ) return d.delete();
     	else return true;
     }
     
     private int runTikal (String extraArgs) throws IOException, InterruptedException {
     	System.out.println("");
     	System.out.println("cmd>===============================================================================");
     	System.out.println("cmd>"+((extraArgs==null) ? "" : " "+extraArgs));
     	Process p = Runtime.getRuntime().exec(javaTikal + ((extraArgs==null) ? "" : " "+extraArgs),
     		null, rootAsFile);
     	StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "err");            
     	StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "out");
     	errorGobbler.start();
     	outputGobbler.start();
     	return p.waitFor();
     }
 
 }
