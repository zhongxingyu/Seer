 package net.sf.jlayercheck.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import net.sf.jlayercheck.util.exceptions.CycleFoundException;
 import net.sf.jlayercheck.util.exceptions.OrphanedSearchException;
 import net.sf.jlayercheck.util.exceptions.OverlappingModulesDefinitionException;
 import net.sf.jlayercheck.util.graph.GraphModuleDependencies;
 import net.sf.jlayercheck.util.model.ClassDependency;
 import de.java2html.Java2Html;
 import de.java2html.options.JavaSourceConversionOptions;
 
 /**
  * Creates HTML output of the retrieved dependeny information.
  * 
  * @author webmaster@earth3d.org
  */
 public class HTMLOutput {
 	protected static Logger logger = Logger.getLogger("JLayerCheck");
 	
 	/**
 	 * The directory to write the HTML output to.
 	 */
 	protected String outputDir;
 	
 	public HTMLOutput(String outputDir) {
 		this.outputDir = outputDir;
 	}
 	
     /**
      * Writes the dependency information that was found into human readable
      * HTML files containing the Java source code. The lines that are responsible
      * for the illegal dependencies are marked.
      * 
      * @param dv the DependencyVisitor containing the dependency information
      * @param xcp the configuration that determines the architecture
      * @throws IOException
      */
 	public void write(DependencyVisitor dv, XMLConfiguration xcp) throws IOException {
 		// create necessary output directories
 		new File(outputDir).mkdirs();
 		
 		// open output stream
 		FileOutputStream fos = new FileOutputStream(outputDir+File.separator+"unspecified.html");
 		PrintWriter pw = new PrintWriter(fos);
 		
 		// load and parse configuration, class and java files
 		Map<String, URL> javaSources = new TreeMap<String, URL>();
 		javaSources.putAll(xcp.getClassSources().get(0).getSourceFiles());
 
 		Set<String> unspecifiedPackages;
 		try {
 			unspecifiedPackages = xcp.getUnspecifiedPackages(dv.getDependencies());
 			Map<String, Map<String, ClassDependency>> unallowedDependencies = xcp.getUnallowedDependencies(dv.getDependencies()); 
 
 			// copy images to destination directory
 			copyImage("/error.png", "images/error.png", outputDir);
 			copyImage("/package.png", "images/package.png", outputDir);
 			copyImage("/class.png", "images/class.png", outputDir);
 			copyImage("/list.png", "images/list.png", outputDir);
 			copyImage("/jlayercheck.css", "jlayercheck.css", outputDir);
 
 			// find violations
 			pw.println("<html><head><title>Unspecified packages</title></head><body>");
 			for(String classPackageName : unspecifiedPackages) {
 				classPackageName = formatPackageName(classPackageName);
 				pw.println("Warning: Package "+classPackageName+" has no module.<br/>");
 			}
 			pw.println("</body>");
 			pw.close();
 			fos.close();
 
 			Map<String, URL> sourceFiles = xcp.getAllClassSources();
 
 			fos = new FileOutputStream(outputDir+File.separator+"violations.html");
 			pw = new PrintWriter(fos);
 			
 			pw.println("<html><head><title>Dependency violations</title>");
 			pw.println("<style type=\"text/css\" media=\"all\">@import \"jlayercheck.css\";</style>");
 			pw.println("</head><body>");
 			
 			// write module dependencies
 			pw.println("<h1>Module dependencies:</h1>");
 			try {
 				GraphModuleDependencies gmd = new GraphModuleDependencies(xcp.getModuleDependencies());
 				ImageIO.write(gmd.getImage(), "png", new File(outputDir+File.separator+"images"+File.separator+"hierarchy.png"));
 				pw.println("<img src=\"images/hierarchy.png\" />");
 			} catch (CycleFoundException e) {
 				pw.println("<b>The module dependency graph contains cycles (which should be removed)!</b>");
 			}
 			
 			// write dependency violations
 			writeDependencyViolations(dv, xcp, pw, unallowedDependencies, sourceFiles);
 
 			// write orphaned classes information
 			writeOrphanedClasses(dv, xcp, pw);
 
 			pw.println("</body>");
 			pw.close();
 			fos.close();
 		} catch (OverlappingModulesDefinitionException e1) {
 			pw.println("<html><head></head><body>Configuration error: "+e1.getMessage()+"</body>");
 			pw.close();
 			fos.close();
 		}
 	}
 
 	/**
 	 * Writes a list of dependency violations to the given PrintWriter using html.
 	 * It shows the classes that violate the architecture by accessing a module
 	 * which they are not allowed to access.
 	 * 
 	 * @param dv
 	 * @param xcp
 	 * @param pw
 	 * @param unallowedDependencies
 	 * @param sourceFiles
 	 * @throws IOException
 	 * @throws OverlappingModulesDefinitionException
 	 */
 	protected void writeDependencyViolations(DependencyVisitor dv, XMLConfiguration xcp, PrintWriter pw, Map<String, Map<String, ClassDependency>> unallowedDependencies, Map<String, URL> sourceFiles) throws IOException, OverlappingModulesDefinitionException {
 		pw.println("<h1>Dependency violations by packages:</h1>");
 		for(String packagename : dv.getPackages().keySet()) {
 			boolean wrotePackageHeader = false;
 			for(String classname : dv.getPackages().get(packagename)) {
 				String classmodule = xcp.getMatchingModule(classname);
 
 				if (unallowedDependencies.get(classname) != null) {
 					if (!wrotePackageHeader) {
 						wrotePackageHeader = true;
 						pw.println("<br/>");
 						pw.println("<h2><img src=\"images/package.png\" /> Package "+formatPackageName(packagename)+"</h2>");
 					}
 
 					// Create link to sources
 					String link = classname.replaceAll("/", "_")+".html";
 					pw.println("<h3><img src=\"images/class.png\" /> Class <a href=\""+link+"\">"+formatPackageName(classname)+"</a> ("+classmodule+")</h3>");
 					pw.println("<ul>");
 					Map<Integer, String> markedLines = new TreeMap<Integer, String>();
 					for(String dependency : unallowedDependencies.get(classname).keySet()) {
 						String dependencyPackageName = StringUtils.getPackageName(dependency);
 
 						String dependencymodule = xcp.getPackageModules().get(dependencyPackageName);
 
 //							System.out.print("Class "+classname+" ("+classmodule+") must not use class "+dependency+" ("+dependencymodule+") in line ");
 						pw.println("<li>"+formatPackageName(dependency)+" ("+dependencymodule+")</li>");
 
 						logger.finer("class="+classname+" dep="+dependency);
 						for(int line : unallowedDependencies.get(classname).get(dependency).getLineNumbers()) {
 							logger.finest(" "+line);
 							markedLines.put(line, "must not depend on "+formatPackageName(dependency)+" ("+dependencymodule+")");
 						}
 						logger.finest("");
 					}
 					pw.println("</ul>");
 
 					// write sources
 					URL url = sourceFiles.get(classname);
 					if (url != null) {
 						String content = readURL(url);
 						JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
 						options.setShowLineNumbers(true);
 						content = Java2Html.convertToHtml(content, options);
 
 						writeSourceFile(outputDir+File.separator+link,
 								content,
 								markedLines);
 					}
 				}
 			}
 		}
 	}
 
 	protected void writeOrphanedClasses(DependencyVisitor dv, XMLConfiguration xcp, PrintWriter pw) {
 		Set<String> orphanedClasses;
 		pw.println("<br/>");
 		pw.println("<h1><img src=\"images/class.png\" /> Orphaned classes:</h1>");
 		pw.println("<ul>");
 		try {
 			orphanedClasses = xcp.getOrphanedClasses(dv.getDependencies());
 			for(String classname : orphanedClasses) {
 				pw.println("<li>" + formatPackageName(classname) + "</li>");
 			}
 		} catch (OrphanedSearchException e) {
 			e.printStackTrace();
 
 			pw.println("<b>An error ocurred: "+e.getMessage()+"</b>");
 		}
 		pw.println("</ul>");
 	}
     
     /**
      * Copies the names resource to the given destination directory.
      * 
      * @param resourceName the name of the resource to load
      * @param filename the filename to save to
      * @param outputDir the destination directory
      * @throws IOException 
      */
 	protected void copyImage(String resourceName, String filename, String outputDir) throws IOException {
         new File(outputDir, filename).getParentFile().mkdirs();
         
         InputStream is = getClass().getResourceAsStream(resourceName);
         FileOutputStream fos = new FileOutputStream(new File(outputDir, filename));
         while(is.available() > 0) {
             byte content[] = new byte[is.available()];
             is.read(content);
             fos.write(content);
         }
         
         is.close();
         fos.close();
     }
 
     /**
      * Writes the given java source into the given file. The syntax is highlighted and the given
      * marked lines are marked with small error symbols.
      * 
      * @param filename output filename
      * @param content java source
      * @param markedLines a Map containing the line number and the message for this line
      * @throws IOException
      */
     protected void writeSourceFile(String filename, String content, Map<Integer, String> markedLines)  throws IOException {
         FileOutputStream fos = new FileOutputStream(filename);
         PrintWriter pw = new PrintWriter(fos);
 //        pw.write(content);
 //        pw.close();
 //        fos.close();
         
 //        if (true) return;
         content = content.substring(content.indexOf("<code>")+6);
         content = content.substring(0, content.lastIndexOf("</code>"));
         
         int linenumber = 1;
         pw.println("<html><head><title>"+filename+"</title>");
         pw.println("<style type=\"text/css\" media=\"all\">@import \"jlayercheck.css\";</style>");
         
         copyImage("/yahoo-debug.js", "yahoo-debug.js", outputDir);
         copyImage("/event-debug.js", "event-debug.js", outputDir);
         copyImage("/dom-debug.js", "dom-debug.js", outputDir);
         copyImage("/jlayercheck.js", "jlayercheck.js", outputDir);
 
         pw.println("<script src='yahoo-debug.js'></script>");
         pw.println("<script src='event-debug.js'></script>");
         pw.println("<script src='dom-debug.js'></script>");
         pw.println("<script src='jlayercheck.js'></script>");
         pw.println("<script>YAHOO.util.Event.addListener(window, 'load', jlayercheckInit);</script>");
         
         pw.println("</head><body>");
         pw.println("<div align=\"left\" class=\"java\"><table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" bgcolor=\"#ffffff\">");
 //        pw.println("<table>");
         while(content.length()>0) {
             int lastpos = content.indexOf("<br />");
             String token = "";
             if (lastpos>=0) {
                 token = content.substring(0, lastpos);
                 content = content.substring(lastpos + 6);
             } else {
                 token = content;
                 content = "";
             }
             pw.print("<tr><td>");
             if (markedLines.containsKey(linenumber)) {
             	pw.print("<div class='msgErr'>");
                 pw.print("<img class='msgErrImg' src=\"images/error.png\" title=\""+markedLines.get(linenumber)+"\" />");
                 pw.print("<div class='msgErrText' id='errText" + linenumber + "'>");
                 pw.print(markedLines.get(linenumber));
                 pw.print("</div>");
                 pw.print("</div>");
             }
             pw.print("</td><td nowrap=\"nowrap\" valign=\"top\" align=\"left\">");
             pw.println("<code>");
             pw.write(token);
             pw.println("</code>");
             pw.print("</td></tr>");
             linenumber++;
         }
         pw.println("</table></div>");
         pw.println("</body>");
         pw.close();
         fos.close();
     }
 
     private String readURL(URL url) throws IOException {
         String result = "";
         
         InputStream is = url.openStream();
         while(is.available() > 0) {
             byte content[] = new byte[is.available()];
             is.read(content);
 
             result = result + new String(content);
         }
         
         return result;
         
     }
 
     /**
 	 * Replaces "/" by "." to create a package name in the format the user expects
 	 * to see.
 	 * 
 	 * @param classPackageName
 	 * @return
 	 */
 	public static String formatPackageName(String classPackageName) {
 		return classPackageName.replaceAll("/", ".");
 	}
 }
