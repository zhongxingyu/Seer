 /* ****************************************************************************
  * CompilationEnvironment.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 
 import java.io.*;
 import java.util.*;
 import org.jdom.Element;
 import org.apache.log4j.*;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.xml.internal.XMLUtils;
 
 /** Encapsulates all the context that script compilation needs to
  * refer to.  Instances of this class are threaded through the calls
  * to instances of CompilerNode.
  *
  * Also contains utility functions for compiling to a file.
  */
 public class CompilationEnvironment {
     private final Properties mProperties;
     
     // TODO this is suspicious. What if we want to change the build folder?
     public static final String DEFAULT_OUTPUT_DIR = "build";
     
     public static final String RUNTIME_PROPERTY = "runtime";
     public static final String PROXIED_PROPERTY           = "lzproxied";
     public static final String DEBUG_PROPERTY             = "debug";
     public static final String DEBUG_EVAL_PROPERTY        = "debugEval";
 
     // matches the value of sc.Compiler.DEBUG_BACKTRACE
     public static final String BACKTRACE_PROPERTY         = "debugBacktrace";
 
     public static final String PROFILE_PROPERTY           = "profile";
     public static final String LINK_PROPERTY              = "link";
     // e_validate is defined if a user explicitly defined validate attribute
     public static final String VALIDATE_EXPLICIT_PROPERTY = "e_validate";
     public static final String CSSFILE_PROPERTY           = "cssfile";
     // Log all debug.write messages back to the server
     public static final String LOGDEBUG_PROPERTY      = "logdebug";
     public static final String REMOTEDEBUG_PROPERTY   = "remotedebug";
     public static final String CONSOLEDEBUG_PROPERTY  = "lzconsoledebug";
     public static final String EMBEDFONTS_PROPERTY    = "embedfonts";
     public static final String SOURCELOCATOR_PROPERTY = "sourcelocators";
 
     // Flag used internally, to mark whether the user instantiated a <debug>
     // tag manually. If they didn't, we need to add a call to instantiate one.
     public static final String USER_DEBUG_WINDOW     = "userdebugwindow";
 
     // Internal flag signals when the schema parser is parsing an
     // external library (for library compiling)
     public static final String _EXTERNAL_LIBRARY = "$externalLibrary";
 
     // Flag used by tooling - causes the compiler to skip code generation, and
     // build the ViewSchema only.
     public static final String NO_CODE_GENERATION = "nocodegeneration";
     
     /** Cache for holding DOM tree from parsing library file */
     public HashMap parsedLibraryCache = new HashMap();
 
     /** The root file being compiled.  This is used to resolve
      * relative pathnames. */
     protected File mApplicationFile = null;
     protected File mObjectFile = null;
 
     final SymbolGenerator methodNameGenerator;
 
     /** Output is written here.
      */
     private ObjectWriter mObjectWriter;
     /** Main program output when generating a loadable
      * library. Otherwise same as mObjectWriter */
     private ObjectWriter mMainObjectWriter;
 
     private final FileResolver mFileResolver;
 
     private final CompilationErrorHandler mCompilerErrors =
         new CompilationErrorHandler();
 
     /** A ViewSchema object, to allow adding of user defined classes
      * during compilation.
      */
     private final ViewSchema mSchema;
 
     /**
      * CompilerMediaCache
      */
     private CompilerMediaCache mMediaCache = null;
 
     /** {canonical filenames} for libraries that have been imported;
      * used to prevent recursive processing and including the same
      * library more than once. */
     private Set mImportedLibraryFiles = new HashSet();
 
     /**  {canonical filenames} for loadable libraries that have been imported;
          this is the set of all files included by loadable libraries (<import>'ed).
          We keep this so we can issue a warning if two different loadable libraries
          statically include the same library file. 
     */
     private Map mLoadableImportedLibraryFiles = new HashMap();
 
     /** Keep track of all named resources, so we can check if a view references a defined resource. */
     private Set mResourceNames = new HashSet();
 
     private final Parser mParser;
     private Canvas mCanvas = null;
 
     /** Keep a list of assigned global id's, so we can warn when one
      * is redefined */
     private final Map idTable = new LinkedHashMap();
     /** Keep a list of tag to class maps so we can generate the
      * entries in a batch */
     private final Map tagTable = new LinkedHashMap();
 
     /** Holds a set of unresolved references to resources, so we can
         check for undefined (possibly forward) references after all
         app sources have been parsed.
         Map contains resource-id => Element
     */
     private Map resourceReferences = new HashMap();
 
     /** Cache of the FontInfo information for each class. Computed by
         ViewCompiler walking a class' superclass chain from the base
         class.
     */
     private HashMap classFontInfoTable = new HashMap();
     private FontInfo mDefaultFontInfo;
 
     /** Default text view width */
     private static boolean mDefaultTextWidthInitialized = false;
     private static int mDefaultTextWidth = 100;
 
     /** Default SWF version to compile to */
     private String mDefaultRuntime = LPS.getRuntimeDefault();
 
     /** Used for compiling SWF loadable libraries to refer to _level0 */
     private String mGlobalPrefix = "";
 
     /** Constructs an instance.
      * @param properties compilation properties
      * @param resolver
      * @param mcache
      */
     CompilationEnvironment(Properties properties, FileResolver resolver, CompilerMediaCache mcache) {
         // Use a local symbol generator so that we recycle method
         // names for each new view, to keep the constant pool small.
         this.methodNameGenerator = new SymbolGenerator("$m");
         this.mSchema = new ViewSchema();
         // lzc depends on the properties being shared, because it sets
         // them after creating the environment
         this.mProperties = properties;
         this.mFileResolver = resolver;
         this.mParser = new Parser();
         this.mParser.setResolver(resolver);
         this.mMediaCache = mcache;
     }
 
     /** Copy fields from an existing CompilationEnvironment.
      */
     CompilationEnvironment(CompilationEnvironment srcEnv) {
         // Need to share name generator so we don't create non-unique
         // unique names!
         this.methodNameGenerator = srcEnv.methodNameGenerator;
         this.mProperties = (Properties) (srcEnv.getProperties().clone());
         this.mFileResolver = srcEnv.getFileResolver();
         this.mParser = new Parser();
         this.mParser.setResolver(this.mFileResolver);
         // Default property values
         this.mSchema = srcEnv.getSchema();
         this.mCanvas = srcEnv.getCanvas();
 
         this.mImportedLibraryFiles = new HashSet(srcEnv.getImportedLibraryFiles());
         this.mLoadableImportedLibraryFiles = srcEnv.getLoadableImportedLibraryFiles();
         this.mResourceNames = srcEnv.getResourceNames();
     }
     
     /** Use this constructor for unit testing.  The Compiler uses the
      * constructor that takes a FileResolver. */
     public CompilationEnvironment() {
         this(new Properties(), FileResolver.DEFAULT_FILE_RESOLVER, null);
     }
     
     void setApplicationFile(File file) {
         mApplicationFile = file;
         mCompilerErrors.setFileBase(file.getParent());
         if (file.getParent() == null) {
             mParser.basePathnames.add(0, "");
         } else {
             mParser.basePathnames.add(0, file.getParent());
         }
         // It appears that basePathnames is only used for error reporting.
         // TODO: [12-26-2002 ows] Consolidate this list with the one
         // in FileResolver.
         mParser.basePathnames.add(LPS.getComponentsDirectory());
         mParser.basePathnames.add(LPS.getFontDirectory());
         mParser.basePathnames.add(LPS.getLFCDirectory());
     }
 
     public void setObjectFile(File file) {
       mObjectFile = file;
     }
 
     // For an app named /path/to/myapp.lzx, returns /path/to/build/myapp
     public String getLibPrefix() {
         File appfile = getApplicationFile();
         String appname = appfile.getName();
 
         String basename = FileUtils.getBase(appname);
 
         String parent = appfile.getParent();
         if (parent == null) {
             parent = ".";
         }
 
         String path = parent + "/" + DEFAULT_OUTPUT_DIR + "/" + basename;
         return path;
     }
 
     // For an app named /path/to/myapp.lzx, returns build/myapp
     public String getLibPrefixRelative() {
         File appfile = getApplicationFile();
         String appname = appfile.getName();
 
         String basename = FileUtils.getBase(appname);
 
         String path = DEFAULT_OUTPUT_DIR + "/" + basename;
         return path;
     }
 
     public File getApplicationFile() {
         return mApplicationFile;
     }
 
   public File getObjectFile() {
     return mObjectFile;
   }
 
     public void setGlobalPrefix(String prefix) {
         mGlobalPrefix = prefix;
     }
 
     public String getGlobalPrefix() {
         return mGlobalPrefix;
     }
         
     public void setMediaCache(CompilerMediaCache cache) {
         this.mMediaCache = cache;
     }
     public CompilerMediaCache getMediaCache() {
         return this.mMediaCache;
     }
 
     public void setDefaultFontInfo(FontInfo fi) {
         mDefaultFontInfo = fi;
     }
 
     public FontInfo getDefaultFontInfo() {
         return mDefaultFontInfo;
     }
 
     /** Add canvas info.  It is an error to call this before calling
      * setCanvas (hand will currently result in a null reference
      * exception). */
     public void addClassFontInfo(String classname, FontInfo info) {
         classFontInfoTable.put(classname, info);
     }
 
     public FontInfo getClassFontInfo(String classname) {
         FontInfo cached = (FontInfo) classFontInfoTable.get(classname);
         return cached;
     }
 
     public boolean getEmbedFonts() {
         return this.getBooleanProperty(EMBEDFONTS_PROPERTY);
     }
 
     public void setEmbedFonts(boolean embed) {
         this.setProperty(EMBEDFONTS_PROPERTY, embed);
     }
 
     public void setObjectWriter(ObjectWriter writer) {
         assert mObjectWriter == null;
         this.mObjectWriter = writer;
         this.mMainObjectWriter = writer;
     }
     
     public void setMainObjectWriter(ObjectWriter writer) {
         assert mMainObjectWriter == null || mMainObjectWriter == mObjectWriter;
         this.mMainObjectWriter = writer;
     }
     
     public void setScriptLimits(int recursion, int timeout) {
         if (this.mMainObjectWriter != null) {
             this.mMainObjectWriter.setScriptLimits(recursion, timeout);
         }
     }
 
     public ViewSchema getSchema() {
         return mSchema;
     }
 
     public static synchronized int getDefaultTextWidth() {
         if (!mDefaultTextWidthInitialized) {
             mDefaultTextWidthInitialized = true;
             String dws = LPS.getProperty("compiler.defaultTextWidth", "100");
             try {
                 int dw = Integer.parseInt(dws);
                 mDefaultTextWidth = dw;
             } catch (NumberFormatException e) {
                 Logger.getLogger(CompilationEnvironment.class)
                     .error("could not parse property value for compiler.defaultTextWidth: " + dws);
             }
         }
         return mDefaultTextWidth;
     }
 
     public CompilationErrorHandler getErrorHandler() {
         return mCompilerErrors;
     }
 
     public void warn(CompilationError e) {
         mCompilerErrors.addError(e);
     }
 
     public void warn(Throwable e, Element element) {
         mCompilerErrors.addError(new CompilationError(element, e));
     }
 
     public void warn(String msg) {
         warn(new CompilationError(msg));
     }
 
     public void warn(String msg, Element element) {
         warn(new CompilationError(msg, element));
     }
 
     public Canvas getCanvas() {
         return mCanvas;
     }
 
     // We are compiling a canvas (whole program) not just a library
     public boolean isCanvas() {
         return mCanvas != null;
     }
 
     public void setCanvas(Canvas canvas, String constructorScript) {
         if (mCanvas != null)
             throw new RuntimeException(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="canvas set twice"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 CompilationEnvironment.class.getName(),"051018-316")
 );
         mCanvas = canvas;
         try {
             getGenerator().setCanvas(canvas, constructorScript);
         } catch (org.openlaszlo.sc.CompilerException e) {
             throw new CompilationError(e);
         }
     }
 
     public void addId(String name, Element element) {
       // Check that it is a valid identifier
       if (!org.openlaszlo.sc.ScriptCompiler.isIdentifier(name)) {
         throw(new CompilationError("\"" + name + "\" is not a valid javascript identifier " , element));
       }
       // Catch duplicated id/name attributes which may shadow each
       // other or overwrite each other. An id/name will be global
       // there is "id='foo'" or if "name='foo'" at the top level
       // (immediate child of the canvas).
       ElementWithLocationInfo dup = (ElementWithLocationInfo) idTable.get(name);
       // we don't want to give a warning in the case
       // where the id and name are on the same element,
       // i.e., <view id="foo" name="foo"/>
       if (dup != null && dup != element) {
         String locstring = CompilerUtils.sourceLocationPrettyString(dup);
         warn(
           /* (non-Javadoc)
            * @i18n.test
            * @org-mes="Duplicate id attribute \"" + p[0] + "\" at " + p[1]
            */
           org.openlaszlo.i18n.LaszloMessages.getMessage(
             NodeModel.class.getName(),"051018-576", new Object[] {name, locstring}),
           element);
       } else {
         idTable.put(name, element);
       }
     }
 
     public Map getIds () {
         return idTable;
     }
 
     public void addTag(String tagName, String className) {
         tagTable.put(tagName, className);
     }
 
    public boolean tagDefined(String tagName) {
        return tagTable.containsKey(tagName);
    }

     public Map getTags () {
         return tagTable;
     }
 
     public void addResourceReference(String name, Element elt) {
         resourceReferences.put(name, elt);
     }
 
     public Map resourceReferences() {
         return resourceReferences;
     }
 
 
     /** Returns the SWF writer that compilation within this
      * environment writes to.
      * @return the object writer
      */
     ObjectWriter getGenerator() {
         return mObjectWriter;
     }
 
     /** By pointing at the main SWFWriter, this makes the resources
         compile into the main app. We have to do this because we
         haven't figured out a way to get Flash to attach individual
         exported assets from a runtime loaded library into views in
         the main app.
      * @return the object writer
      */
 
     ObjectWriter getResourceGenerator() {
         return mMainObjectWriter;
         
         // Note: Returning the library's SWFWriter, as shown below,
         // would make the compiler compile the resources into the
         // loadable library:
         //return mObjectWriter; 
      }
      
     private boolean mSnippet = false;
 
     /** Returns true if we're compiling a loadable library file.
      * @return isLibrary
      */
     public boolean isImportLib() {
         return mSnippet;
     }
 
     public void setImportLib(boolean v) {
         mSnippet = v;
     }
 
     /** Returns the file resolver used in this environment.
      * @return the object writer
      */
     FileResolver getFileResolver() {
         return mFileResolver;
     }
 
     Set getImportedLibraryFiles() {
         return mImportedLibraryFiles;
     }
 
     Map getLoadableImportedLibraryFiles() {
         return mLoadableImportedLibraryFiles;
     }
 
     Set getResourceNames() {
         return mResourceNames;
     }
 
     Parser getParser() {
         return mParser;
     }
 
     /** Returns the Properties object used in this environment.
      * @return the properties
      */
     Properties getProperties() {
         return mProperties;
     }
 
     String getProperty(String name) {
         return mProperties.getProperty(name);
     }
 
     String getProperty(String name, String defval) {
         return mProperties.getProperty(name, defval);
     }
 
     void setProperty(String name, String value) {
         mProperties.setProperty(name, value);
     }
 
     /** Return target Flash version (5, 6, ...) **/
     public String getRuntime() {
         return getProperty(RUNTIME_PROPERTY, mDefaultRuntime);
     }
 
     public String getRuntime(String defaultVersion) {
         return getProperty(RUNTIME_PROPERTY, defaultVersion);
     }
 
     public int getSWFVersionInt() {
       String runtime = getRuntime();
         if ("swf7".equals(runtime)) {
             return 7;
         } else if ("swf8".equals(runtime)) {
             return 8;
         } else {
           throw new CompilationError("'" + runtime + "' is not a SWF runtime");
         }
     }
 
     boolean getBooleanProperty(String name) {
         return "true".equals(mProperties.getProperty(name));
     }
 
     void setProperty(String name, boolean value) {
         setProperty(name, value ? "true" : "false");
     }
 
     /** Compiles <var>script</var> to bytecodes, and adds them to the
      * output file.
      * @param script a script
      */
     void compileScript(String script) {
         try {
             int size = getGenerator().addScript(script);
             if (mCanvas != null) {
                 Element info = new Element("block");
                 info.setAttribute("size", "" + size);
                 mCanvas.addInfo(info);
             }
         } catch (org.openlaszlo.sc.CompilerException e) {
             throw new CompilationError(e);
         }
     }
 
     void compileScript(String script, Element elt) {
         try {
             int size = getGenerator().addScript(CompilerUtils.sourceLocationDirective(elt, true) + script);
             if (mCanvas != null) {
               Element info = new Element("block");
               info.setAttribute("pathname", Parser.getSourceMessagePathname(elt) );
               info.setAttribute("lineno", ""+Parser.getSourceLocation(elt, Parser.LINENO));
               info.setAttribute("tagname", elt.getName());
               if (elt.getAttribute("id") != null)
                 info.setAttribute("id", elt.getAttributeValue("id"));
               if (elt.getAttribute("name") != null)
                 info.setAttribute("name", elt.getAttributeValue("name"));
               info.setAttribute("size", "" + size);
               mCanvas.addInfo(info);
             }
         } catch (org.openlaszlo.sc.CompilerException e) {
             throw new CompilationError(elt, e);
         }
     }
 
     /**
      * @return a unique name in the SWF
      */
     String uniqueName() {
         return mObjectWriter.createName();
     }
 
     File resolve(String name, String base)
         throws FileNotFoundException
     {
         return mFileResolver.resolve(this, name, base, false);
     }
 
     File resolveLibrary(String name, String base)
         throws FileNotFoundException
     {
         return mFileResolver.resolve(this, name, base, true);
     }
 
     File resolveReference(Element element, String aname)
         throws CompilationError
     {
         return resolveReference(element, aname, element.getName().equals("include"));
     }
 
     /** Resolve the value of the named attribute, relative to the
      * source location of the element.
      */
     File resolveReference(Element element, String aname, boolean asLibrary)
         throws CompilationError
     {
         String base = new File(Parser.getSourcePathname(element)).getParent();
         String href =  XMLUtils.requireAttributeValue(element, aname);
 
         try {
             return mFileResolver.resolve(this, href, base, asLibrary);
         } catch (FileNotFoundException e) {
             throw new CompilationError(element, e);
         }
     }
 
     /** Resolve the value of the parent node
      */
     File resolveParentReference(Element element)
         throws CompilationError
     {
         return new File(Parser.getSourcePathname((Element) element.getParentElement()));
     }
 
     /** Resolve the value of the "src" attribute, relative to the
      * source location of the element.
      */
     File resolveReference(Element elt)
         throws CompilationError
     {
         return (resolveReference(elt, "src"));
     }
 
     /** If the argument is a relative URL with no host, return an URL
      * that resolves to the same address relative to the destDir as
      * the argument does relative to sourceDir.  Otherwise return the
      * argument unchanged. */
     static String adjustRelativeURL(String string, File sourceDir,
                                     File destDir)
     {
         try {
             java.net.URL url = new java.net.URL(string);
             if (!url.getHost().equals("")) {
                 // It's on a different host.  Don't resolve it.
                 return string;
             }
             if (url.getPath().startsWith("/")) {
                 // It's an absolute path.  Don't resolve it.
                 return string;
             }
             String path;
             try {
                 path = FileUtils.adjustRelativePath(
                     url.getPath(),
                     FileUtils.toURLPath(sourceDir),
                     FileUtils.toURLPath(destDir));
             } catch (FileUtils.RelativizationError e) {
                 throw new CompilationError(e);
             }
             if (url.getQuery() != null) {
                 path += "?" + url.getQuery();
             }
             return new java.net.URL(
                 url.getProtocol(), url.getHost(), url.getPort(), path)
                 .toExternalForm();
         } catch (java.net.MalformedURLException e) {
             return string;
         }
     }
 
     // [TODO hqm 01/06] this should be keyed off of the 'lzr' runtime
     // arg, it should return true for lzr=dhtml
     public boolean isDHTML() {
 
         if (this.getRuntime().equals("swf9")) {
             return false;
         }
         
         return Compiler.SCRIPT_RUNTIMES.contains(this.getRuntime());
     }
 
     public boolean isSWF() {
         return Compiler.SWF_RUNTIMES.contains(this.getRuntime());
     }
 
     /** If the argument is a relative URL with no host, return an URL
      * that resolves to the same address relative to the main source
      * file as the argument does relative to the file that contains
      * elt.  Otherwise return the argument unchanged. */
     String adjustRelativeURL(String string, Element elt) {
       try {
         File appdir = getApplicationFile().getCanonicalFile().getParentFile();
         File localdir = new File(Parser.getSourcePathname(elt)).getCanonicalFile().getParentFile();
         if (appdir == null) {
             appdir = new File(".").getCanonicalFile();
         }
         if (localdir == null) {
             localdir = new File(".").getCanonicalFile();
         }
         return adjustRelativeURL(string, appdir, localdir);
       } catch (java.io.IOException e) {
         throw new CompilationError(elt, e);
       }
     }
 
     public boolean warnIfCannotContain(Element parentTag, Element childTag) {
         if (!mSchema.canContainElement(parentTag.getName(), childTag.getName())) {
             this.warn(
                 // TODO [2006-08-22 hqm] i18n this
                 "The tag '" + childTag.getName() +
                 "' cannot be used as a child of " + parentTag.getName(),
                 parentTag);
             return false;
         } else {
             return true;
         }
         
     }
 
     /** Check if all children are allowed to be contained in this tags */
     public void checkValidChildContainment(Element element) {
         for (Iterator iter = element.getChildren().iterator();
              iter.hasNext(); ) {
             Element child = (Element) iter.next();
             this.warnIfCannotContain(element, child);
         }
     }
 
 }
