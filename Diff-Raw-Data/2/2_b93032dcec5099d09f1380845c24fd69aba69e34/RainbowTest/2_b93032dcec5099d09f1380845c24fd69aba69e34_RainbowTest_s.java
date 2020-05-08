 package net.sf.okapi.applications.rainbow;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import net.sf.okapi.common.FileCompare;
 import net.sf.okapi.common.StreamGobbler;
 import net.sf.okapi.common.Util;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class RainbowTest {
 	
 	private String javaRainbow;
 	private String root;
 	private File rootAsFile;
 	private FileCompare fc = new FileCompare();
 
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
 			if ( System.getProperty("os.arch").equals("x86_64") ) {
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
 		javaRainbow = "java -jar " + libDir + "rainbow.jar";
 	}
 	
     @Test
     public void testRewriting () throws IOException, InterruptedException {
     	// Delete previous output
     	assertTrue(deleteOutputFile("potest.rbout.po"));
    	assertEquals(0, runRainbow("-np -x oku_textrewriting potest.po -o potest.rbout.po -opt rewriting.opt"));
     	assertTrue("File different from gold", compareWithGoldFile("potest.rbout.po"));
     }
 
     private boolean compareWithGoldFile (String outputBase) {
     	String outputPath = root + File.separator + outputBase;
     	String goldPath = root + File.separator + "gold" + File.separator + outputBase; 
     	return fc.filesExactlyTheSame(outputPath, goldPath);
     }
     
 //    private boolean compareWithGoldFile (String outputBase,
 //    	String goldBase)
 //    {
 //    	String outputPath = root + File.separator + outputBase;
 //    	String goldPath = root + File.separator + "gold" + File.separator + goldBase; 
 //    	return fc.filesExactlyTheSame(outputPath, goldPath);
 //    }
     
     private boolean deleteOutputFile (String filename) {
     	File f = new File(root + File.separator + filename);
     	if ( f.exists() ) {
     		return f.delete();
     	}
     	else return true;
     }
     
     public boolean deleteOutputDir (String dirname, boolean relative) {
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
     
     private int runRainbow (String extraArgs) throws IOException, InterruptedException {
     	Process p = Runtime.getRuntime().exec(javaRainbow + ((extraArgs==null) ? "" : " "+extraArgs),
     		null, rootAsFile);
     	StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "err");            
     	StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "out");
     	errorGobbler.start();
     	outputGobbler.start();
     	p.waitFor();
     	return p.exitValue();
     }
 
 }
