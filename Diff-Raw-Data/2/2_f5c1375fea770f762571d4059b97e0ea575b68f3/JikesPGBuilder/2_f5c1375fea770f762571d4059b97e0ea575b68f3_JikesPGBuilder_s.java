 package org.jikespg.uide.builder;
 
 /*
  * Licensed Materials - Property of IBM,
  * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.uide.core.SAFARIBuilderBase;
 import org.eclipse.uide.runtime.SAFARIPluginBase;
 import org.jikespg.uide.JikesPGPlugin;
 import org.jikespg.uide.preferences.JikesPGPreferenceCache;
 import org.jikespg.uide.views.JikesPGView;
 import org.osgi.framework.Bundle;
 
 /**
  * @author rfuhrer@watson.ibm.com
  * @author CLaffra
  */
 public class JikesPGBuilder extends SAFARIBuilderBase {
     /**
      * Extension ID of the JikesPG builder. Must match the ID in the corresponding
      * extension definition in plugin.xml.
      */
     public static final String BUILDER_ID= JikesPGPlugin.kPluginID + ".jikesPGBuilder";
 
     public static final String PROBLEM_MARKER_ID= JikesPGPlugin.kPluginID + ".problem";
 
     /**
      * ID of the LPG plugin, which houses the templates, the LPG executable,
      * and the LPG runtime library
      */
     public static final String LPG_PLUGIN_ID= "lpg";
 
     private static final String SYNTAX_MSG_REGEXP= "(.*):([0-9]+):([0-9]+):([0-9]+):([0-9]+):([0-9]+):([0-9]+): (.*)";
     private static final Pattern SYNTAX_MSG_PATTERN= Pattern.compile(SYNTAX_MSG_REGEXP);
 
     private static final String MISSING_MSG_REGEXP= "Input file \"([^\"]+)\" could not be read";
     private static final Pattern MISSING_MSG_PATTERN= Pattern.compile(MISSING_MSG_REGEXP);
 
     protected SAFARIPluginBase getPlugin() {
 	return JikesPGPlugin.getInstance();
     }
 
     protected String getErrorMarkerID() {
 	return PROBLEM_MARKER_ID;
     }
 
     protected String getWarningMarkerID() {
 	return PROBLEM_MARKER_ID;
     }
 
     protected String getInfoMarkerID() {
 	return PROBLEM_MARKER_ID;
     }
 
     protected boolean isSourceFile(IFile file) {
 	IPath path= file.getRawLocation();
 
 	if (path == null) return false;
 
 	String fileName= path.toString();
 
 	return (fileName.indexOf("/bin/") == -1 && "g".equals(path.getFileExtension()));
     }
 
     protected boolean isOutputFolder(IResource resource) {
 	return resource.getFullPath().lastSegment().equals("bin");
     }
 
     protected void compile(final IFile file) {
 	String fileName= file.getLocation().toOSString();
 	String templatePath= getTemplatePath();
 
 	JikesPGPlugin.getInstance().maybeWriteInfoMsg("Using template path '" + templatePath + "'.");
 
 	try {
 	    File parentDir= new File(fileName).getParentFile();
 	    String cmd[]= new String[] {
 		    getLPGExecutable(),
 		    "-quiet",
 		    (JikesPGPreferenceCache.generateListing ? "-list" : "-nolist"),
		    "-include-directory=" + templatePath,
 		    // TODO RMF 7/21/05 -- Don't specify -dat-directory; causes performance issues with Eclipse.
 		    // Lexer tables can get quite large, so large that Java as spec'ed can't swallow them
 		    // when translated to a switch statement, or even an array initializer. As a result,
 		    // JikesPG supports the "-dat-directory" option to spill the tables into external data
 		    // files loaded by the lexer at runtime. HOWEVER, loading these external data tables is
 		    // very slow when performed using the standard Eclipse/plugin classloader.
 		    // So: don't enable it by default.
 		    // "-dat-directory=" + getOutputDirectory(resource.getProject()),
 		    fileName};
 	    Process process= Runtime.getRuntime().exec(cmd, new String[0], parentDir);
 	    JikesPGView consoleView= JikesPGView.getDefault();
 
 	    processJikesPGOutput(file, process, consoleView);
 	    processJikesPGErrors(file, process, consoleView);
 	    doRefresh(file);
 	} catch (Exception e) {
 	    JikesPGPlugin.getInstance().writeErrorMsg(e.getMessage());
 	    e.printStackTrace();
 	}
     }
 
     private void processJikesPGErrors(IResource resource, Process process, JikesPGView view) throws IOException {
 	InputStream is= process.getErrorStream();
 	BufferedReader in2= new BufferedReader(new InputStreamReader(is));
 
 	String line;
 	while ((line= in2.readLine()) != null) {
 	    if (view != null)
 		JikesPGView.println(line);
 	    if (parseSyntaxMessageCreateMarker(line))
 		;
 	    else if (line.indexOf("Input file ") == 0) {
 		parseMissingFileMessage(line, resource);
 	    } else
 		handleMiscMessage(line, resource);
 //	    JikesPGPlugin.getInstance().writeErrorMsg(line);
 	}
 	is.close();
     }
 
     final String lineSep= System.getProperty("line.separator");
     final int lineSepBias= lineSep.length() - 1;
 
     private void processJikesPGOutput(final IResource resource, Process process, JikesPGView view) throws IOException {
 	InputStream is= process.getInputStream();
 	BufferedReader in= new BufferedReader(new InputStreamReader(is));
 	String line= null;
 
 	while ((line= in.readLine()) != null) {
 	    if (view != null)
 		JikesPGView.println(line);
 	    else {
 		System.out.println(line);
 	    }
 
 	    final String msg= line;
 
 	    if (parseSyntaxMessageCreateMarker(msg))
 		;
 	    else if (msg.indexOf("Input file ") == 0) {
 		parseMissingFileMessage(msg, resource);
 	    } else
 		handleMiscMessage(msg, resource);
 	}
     }
 
     private void handleMiscMessage(String msg, IResource file) {
 	if (msg.length() == 0) return;
 	if (msg.startsWith("Unable to open"))
 	    createMarker(file, 1, -1, -1, msg, IMarker.SEVERITY_ERROR);
 	if (msg.indexOf("Number of ") < 0 &&
 	    !msg.startsWith("(C) Copyright") &&
 	    !msg.startsWith("IBM LALR Parser"))
 	    createMarker(file, 1, -1, -1, msg, IMarker.SEVERITY_INFO);
     }
 
     private void parseMissingFileMessage(String msg, IResource file) {
 	Matcher matcher= MISSING_MSG_PATTERN.matcher(msg);
 
 	if (matcher.matches()) {
 	    String missingFile= matcher.group(1);
 	    int refLine= 1; // Integer.parseInt(matcher.group(2))
 
 	    createMarker(file, refLine, -1, -1, "Non-existent file referenced: " + missingFile, IMarker.SEVERITY_ERROR);
 	}
     }
 
     private boolean parseSyntaxMessageCreateMarker(final String msg) {
 	Matcher matcher= SYNTAX_MSG_PATTERN.matcher(msg);
 
 	if (matcher.matches()) {
 	    String errorFile= matcher.group(1);
 	    String projectLoc= getProject().getLocation().toOSString();
 
 	    if (errorFile.startsWith(projectLoc))
 		errorFile= errorFile.substring(projectLoc.length());
 
 	    IResource errorResource= getProject().getFile(errorFile);
 	    int startLine= Integer.parseInt(matcher.group(2));
 //	    int startCol= Integer.parseInt(matcher.group(3));
 //	    int endLine= Integer.parseInt(matcher.group(4));
 //	    int endCol= Integer.parseInt(matcher.group(5));
 	    int startChar= Integer.parseInt(matcher.group(6)) - 1;// - (startLine - 1) * lineSepBias + 1;
 	    int endChar= Integer.parseInt(matcher.group(7));// - (endLine - 1) * lineSepBias + 1;
 	    String descrip= matcher.group(8);
 
 	    if (startLine == 0) startLine= 1;
 	    createMarker(errorResource, startLine, startChar, endChar, descrip, IMarker.SEVERITY_ERROR);
 	    return true;
 	}
 	return false;
     }
 
     public static String getTemplatePath() {
 	if (JikesPGPreferenceCache.jikesPGTemplateDir != null &&
 	    JikesPGPreferenceCache.jikesPGTemplateDir.length() > 0)
 	    return JikesPGPreferenceCache.jikesPGTemplateDir;
 
 	return getDefaultTemplatePath();
     }
 
     public static String getDefaultTemplatePath() {
 	Bundle bundle= Platform.getBundle(LPG_PLUGIN_ID);
 
 	try {
 	    // Use getEntry() rather than getResource(), since the "templates" folder is
 	    // no longer inside the plugin jar (which is now expanded upon installation).
 	    String tmplPath= Platform.asLocalURL(bundle.getEntry("templates")).getFile();
 	    if (Platform.getOS().equals("win32"))
 		tmplPath= tmplPath.substring(1);
 	    return tmplPath;
 	} catch(IOException e) {
 	    return null;
 	}
     }
 
     private String getLPGExecutable() throws IOException {
 	return JikesPGPreferenceCache.jikesPGExecutableFile;
     }
 
     public static String getDefaultExecutablePath() {
 	Bundle bundle= Platform.getBundle(LPG_PLUGIN_ID);
 	String os= Platform.getOS();
 	String plat= Platform.getOSArch();
 	Path path= new Path("bin/lpg-" + os + "_" + plat + (os.equals("win32") ? ".exe" : ""));
 	URL execURL= Platform.find(bundle, path);
 
 	if (execURL == null) {
 	    String errMsg= "Unable to find JikesPG executable at " + path + " in bundle " + bundle.getSymbolicName();
 
 	    JikesPGPlugin.getInstance().writeErrorMsg(errMsg);
 	    throw new IllegalArgumentException(errMsg);
 	} else {
 	    // N.B.: The jikespg executable will normally be inside a jar file,
 	    //       so use asLocalURL() to extract to a local file if needed.
 	    URL url;
 
 	    try {
 		url= Platform.asLocalURL(execURL);
 	    } catch (IOException e) {
 		JikesPGPlugin.getInstance().writeErrorMsg("Unable to locate default JikesPG executable." + e.getMessage());
 		return "???";
 	    }
 
 	    String jikesPGExecPath= url.getFile();
 
 	    if (os.equals("win32")) // remove leading slash from URL that shows up on Win32(?)
 		jikesPGExecPath= jikesPGExecPath.substring(1);
 
 	    JikesPGPlugin.getInstance().maybeWriteInfoMsg("JikesPG executable apparently at '" + jikesPGExecPath + "'.");
 	    return jikesPGExecPath;
 	}
     }
 }
