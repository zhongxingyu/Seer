 package de.codecentric.vaadin.maven;
 
 /**
  * Some code from com.vaadin.buildhelpers.CompileDefaultTheme 
  */
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.maven.plugin.logging.Log;
 
 public class ThemeCombiner {
 
     private static final String THEME_DIR = "/VAADIN/themes/";
 
     private final Log log;
 
     private final File warSourceDirectory;
 
     private final File targetDirectory;
 
     public ThemeCombiner(Log log, File warSourceDirectory, File targetDirectory) {
 	this.log = log;
 	this.warSourceDirectory = warSourceDirectory;
 	this.targetDirectory = targetDirectory;
     }
 
     /**
      * 
      * @param webappDirectory
      * @param themeNames
      *            All themes that should be combined together (to include inheritance). The order is
      *            the same in which the styles are catenated. The resulted file is placed in the
      *            last specified theme folder.
      * 
      * @param
      * @throws IOException
      */
     public void combineTheme(String theme) throws IOException {
 	StringBuffer combinedCss = new StringBuffer();
 	combinedCss.append("/* Automatically compiled css file from subdirectories. */\n");
 
 	// Process
 	String stylesCssDir = warSourceDirectory.getAbsolutePath() + THEME_DIR + theme + "/";
 	processCSSFile(stylesCssDir, new File(stylesCssDir + "styles.css"), "", theme, combinedCss);
 
 	// Output
 	File themeTargetDir = new File(targetDirectory.getAbsolutePath() + THEME_DIR + theme);
 	themeTargetDir.mkdirs();
 
 	String stylesCssName = themeTargetDir.getAbsolutePath() + "/styles.css";
 	BufferedWriter out = new BufferedWriter(new FileWriter(stylesCssName));
 	out.write(combinedCss.toString());
 	out.close();
 	log.info("Compiled CSS to " + stylesCssName + " (" + combinedCss.toString().length()
 		+ " bytes)");
 
     }
 
     private void processCSSFile(String stylesCssDir, File cssFile, String folder, String themeName,
 	    StringBuffer combinedCss) throws FileNotFoundException, IOException {
 	if (cssFile.isFile()) {
 
 	    combinedCss.append("\n");
 
 	    FileInputStream fstream = new FileInputStream(cssFile);
 	    // Get the object of DataInputStream
 	    DataInputStream in = new DataInputStream(fstream);
 	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 	    String strLine;
 	    while ((strLine = br.readLine()) != null) {
 
 		// Parse import rules
 		if (strLine.startsWith("@import")) {
 		    // All import statements must be exactly
 		    // @import "file-to-import.css";
 		    // or
 		    // @import "subdir1[/subdir2]*/file-to-import.css"
 		    // ".." and other similar paths are not allowed in the url
 		    String importFilename = strLine.split("\"")[1];
 
 		    File importFile = new File(stylesCssDir + folder + "/" + importFilename);
 		    if (importFile.isFile()) {
 			String currentFolder = folder;
 			if (importFilename.contains("/")) {
			    if (currentFolder != null && currentFolder.length() > 0) {
				currentFolder = currentFolder
					+ "/"
					+ importFilename.substring(0,
						importFilename.lastIndexOf("/"));
			    } else {
				currentFolder = importFilename.substring(0,
					importFilename.lastIndexOf("/"));
			    }
 			}
 			processCSSFile(stylesCssDir, importFile, currentFolder, themeName,
 				combinedCss);
 		    } else {
 			log.error("File not found for @import statement " + stylesCssDir + folder
 				+ "/" + importFilename);
 		    }
 		}
 
 		strLine = updateUrls(folder, themeName, strLine);
 
 		if (!strLine.startsWith("@import")) {
 		    combinedCss.append(strLine);
 		    combinedCss.append("\n");
 		}
 	    }
 	    // Close the input stream
 	    in.close();
 	}
     }
 
     private String updateUrls(String folder, String themeName, String strLine) {
 	if (strLine.indexOf("url(/") > 0) {
 	    // Do nothing for urls beginning with /
 	} else if (strLine.indexOf("url(../") >= 0) {
 	    // eliminate a path segment in the folder name for every
 	    // "../"
 	    String[] folderSegments = folder.split("/");
 	    int segmentCount = folderSegments.length;
 	    while (segmentCount > 0 && strLine.indexOf("url(../") >= 0) {
 		segmentCount--;
 		strLine = strLine.replaceAll("url\\(../", ("url\\("));
 	    }
 	    // add remaining path segments to urlPrefix
	    StringBuilder sb = new StringBuilder();
 	    for (int i = 0; i < segmentCount; i++) {
 		sb.append(folderSegments[i]);
 		sb.append("/");
 	    }
 	    strLine = strLine.replaceAll("url\\(", ("url\\(" + sb.toString()));
 
 	} else {
	    strLine = strLine.replaceAll("url\\(", ("url\\(" + folder + "/"));
 
 	}
 	return strLine;
     }
 
 }
