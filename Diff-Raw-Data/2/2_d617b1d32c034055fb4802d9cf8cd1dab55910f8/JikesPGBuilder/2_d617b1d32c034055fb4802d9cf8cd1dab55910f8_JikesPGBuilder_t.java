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
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.uide.core.SAFARIBuilderBase;
 import org.eclipse.uide.runtime.SAFARIPluginBase;
 import org.eclipse.uide.utils.StreamUtils;
 import org.jikespg.uide.JikesPGPlugin;
 import org.jikespg.uide.parser.JikesPGLexer;
 import org.jikespg.uide.parser.JikesPGParser;
 import org.jikespg.uide.parser.JikesPGParser.ASTNode;
 import org.jikespg.uide.parser.JikesPGParser.import_segment;
 import org.jikespg.uide.parser.JikesPGParser.include_segment;
 import org.jikespg.uide.parser.JikesPGParser.option;
 import org.jikespg.uide.parser.JikesPGParser.option_value0;
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
 
     private static final String SYNTAX_MSG_REGEXP= "(.*):([0-9]+):([0-9]+):([0-9]+):([0-9]+):([0-9]+):([0-9]+): (informative|Warning|Error): (.*)";
     private static final Pattern SYNTAX_MSG_PATTERN= Pattern.compile(SYNTAX_MSG_REGEXP);
 
     private static final String SYNTAX_MSG_NOSEV_REGEXP= "(.*):([0-9]+):([0-9]+):([0-9]+):([0-9]+):([0-9]+):([0-9]+): (.*)";
     private static final Pattern SYNTAX_MSG_NOSEV_PATTERN= Pattern.compile(SYNTAX_MSG_NOSEV_REGEXP);
 
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
 	return !file.isDerived() && JikesPGPreferenceCache.rootExtensionList.contains(file.getFileExtension());
     }
 
     protected boolean isNonRootSourceFile(IFile file) {
         return !file.isDerived() && JikesPGPreferenceCache.nonRootExtensionList.contains(file.getFileExtension());
     }
 
     protected boolean isOutputFolder(IResource resource) {
 	return resource.getFullPath().lastSegment().equals("bin");
     }
 
     protected void compile(final IFile file, IProgressMonitor monitor) {
 	String fileName= file.getLocation().toOSString();
 	String includePath= getIncludePath();
 	try {
 	    String executablePath= getLPGExecutable();
 	    File parentDir= new File(fileName).getParentFile();
 
 	    JikesPGPlugin.getInstance().maybeWriteInfoMsg("Running generator on grammar file '" + fileName + "'.");
 	    JikesPGPlugin.getInstance().maybeWriteInfoMsg("Using executable at '" + executablePath + "'.");
 	    JikesPGPlugin.getInstance().maybeWriteInfoMsg("Using template path '" + includePath + "'.");
 
 	    String cmd[]= new String[] {
 		    executablePath,
 		    "-quiet",
 		    (JikesPGPreferenceCache.generateListing ? "-list" : "-nolist"),
 		    // In order for Windows to treat the following template path argument as
 		    // a single argument, despite any embedded spaces, it has to be completely
 		    // enclosed in double quotes. It does not suffice to quote only the path
 		    // part. However, for jikespg to treat the path properly, the path itself
 		    // must also be quoted, since the outer quotes will be stripped by the
 		    // Windows shell (command/cmd.exe). As an added twist, if we used the same
 		    // kind of quote for both the inner and outer quoting, and the outer quotes
 		    // survived, the part that actually needed quoting would be "bare"! Hence
 		    // we use double quotes for the outer level and single quotes inside.
 		    "\"-include-directory='" + includePath + "'\"",
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
 	    collectDependencies(file);
 	    JikesPGPlugin.getInstance().maybeWriteInfoMsg("Generator exit code == " + process.waitFor());
 	} catch (Exception e) {
 	    JikesPGPlugin.getInstance().logException(e.getMessage(), e);
 	}
     }
 
     protected void collectDependencies(IFile file) {
         JikesPGLexer lexer= new JikesPGLexer(); // Create the lexer
         JikesPGParser parser= new JikesPGParser(lexer.getLexStream()); // Create the parser
         String filePath= file.getLocation().toOSString();
 
         JikesPGPlugin.getInstance().maybeWriteInfoMsg("Collecting dependencies from file '" + file.getLocation().toOSString() + "'.");
         try {
             String contents= StreamUtils.readStreamContents(file.getContents());
 
             lexer.initialize(contents.toCharArray(), filePath);
             lexer.lexer(null, parser.getParseStream());
 
             ASTNode ast= (ASTNode) parser.parser();
 
             if (ast != null)
         	findDependencies(ast, file.getFullPath().toString());
         } catch (CoreException ce) {
             
         }
     }
 
     /**
      * @param root
      * @param filePath 
      */
     private void findDependencies(ASTNode root, final String filePath) {
         root.accept(new JikesPGParser.AbstractVisitor() {
             public void unimplementedVisitor(String s) { }
             public boolean visit(option n) {
                 if (n.getSYMBOL().toString().equals("import_terminals")) {
                     String referent= ((option_value0) n.getoption_value()).getSYMBOL().toString();
                     String referentPath= filePath.substring(0, filePath.lastIndexOf("/")+1) + referent;
                     fDependencyInfo.addDependency(filePath, referentPath);
                 }
                 return false;
             }
             /* (non-Javadoc)
              * @see org.jikespg.uide.parser.JikesPGParser.AbstractVisitor#visit(org.jikespg.uide.parser.JikesPGParser.ImportSeg)
              */
             public boolean visit(import_segment n) {
                 fDependencyInfo.addDependency(filePath, n.getSYMBOL().toString());
                 return false;
             }
             /* (non-Javadoc)
              * @see org.jikespg.uide.parser.JikesPGParser.AbstractVisitor#visit(org.jikespg.uide.parser.JikesPGParser.include_segment1)
              */
             public boolean visit(include_segment n) {
                 fDependencyInfo.addDependency(filePath, n.getSYMBOL().toString());
                 return false;
             }
         });
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
 	    JikesPGPlugin.getInstance().writeErrorMsg(line);
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
 	    if (line.length() == 0)
 		continue;
 
 	    if (parseSyntaxMessageCreateMarker(line))
 		;
 	    else if (line.indexOf("Input file ") == 0) {
 		parseMissingFileMessage(line, resource);
 	    } else
 		handleMiscMessage(line, resource);
 	}
     }
 
     private void handleMiscMessage(String msg, IResource file) {
 	if (msg.length() == 0) return;
 	if (msg.startsWith("Unable to open")) {
 	    createMarker(file, 1, -1, -1, msg, IMarker.SEVERITY_ERROR);
 	    return;
 	}
 	if (msg.startsWith("***ERROR: ")) {
 	    createMarker(file, 1, 0, 1, msg.substring(10), IMarker.SEVERITY_ERROR);
 	    return;
 	}
 	if (msg.indexOf("Number of ") < 0 &&
 	    !msg.startsWith("(C) Copyright") &&
 	    !msg.startsWith("IBM LALR Parser")) {
 	    Matcher matcher= SYNTAX_MSG_NOSEV_PATTERN.matcher(msg);
 
 	    if (matcher.matches()) {
 		String errorFile= matcher.group(1);
 		String projectLoc= getProject().getLocation().toOSString();
 
 		if (errorFile.startsWith(projectLoc))
 		    errorFile= errorFile.substring(projectLoc.length());
 
 		IResource errorResource= getProject().getFile(errorFile);
 		int startLine= Integer.parseInt(matcher.group(2));
 //		int startCol= Integer.parseInt(matcher.group(3));
 //		int endLine= Integer.parseInt(matcher.group(4));
 //		int endCol= Integer.parseInt(matcher.group(5));
 		int startChar= Integer.parseInt(matcher.group(6)) - 1;// - (startLine - 1) * lineSepBias + 1;
 		int endChar= Integer.parseInt(matcher.group(7));// - (endLine - 1) * lineSepBias + 1;
 		String descrip= matcher.group(8);
 
 		if (startLine == 0) startLine= 1;
 		createMarker(errorResource, startLine, startChar, endChar, descrip, IMarker.SEVERITY_WARNING);
 	    } else
 		createMarker(file, 1, 0, 1, msg, IMarker.SEVERITY_INFO);
 	}
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
	    String projectLoc= getProject().getLocation().toString();
 
 	    if (errorFile.startsWith(projectLoc))
 		errorFile= errorFile.substring(projectLoc.length());
 
 	    IResource errorResource= getProject().getFile(errorFile);
 	    int startLine= Integer.parseInt(matcher.group(2));
 //	    int startCol= Integer.parseInt(matcher.group(3));
 //	    int endLine= Integer.parseInt(matcher.group(4));
 //	    int endCol= Integer.parseInt(matcher.group(5));
 	    int startChar= Integer.parseInt(matcher.group(6)) - 1;// - (startLine - 1) * lineSepBias + 1;
 	    int endChar= Integer.parseInt(matcher.group(7));// - (endLine - 1) * lineSepBias + 1;
             String severity= matcher.group(8);
 	    String descrip= matcher.group(9);
 
 	    if (startLine == 0) startLine= 1;
 	    createMarker(errorResource, startLine, startChar, endChar, descrip,
                     (severity.equals("Informative") ? IMarker.SEVERITY_INFO :
                         (severity.equals("Warning") ? IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR)));
 	    return true;
 	}
 	return false;
     }
 
     public static String getIncludePath() {
 	if (JikesPGPreferenceCache.jikesPGIncludeDirs != null &&
 	    JikesPGPreferenceCache.jikesPGIncludeDirs.length() > 0)
 	    return JikesPGPreferenceCache.jikesPGIncludeDirs;
 
 	return getDefaultIncludePath();
     }
 
     public static String getDefaultIncludePath() {
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
