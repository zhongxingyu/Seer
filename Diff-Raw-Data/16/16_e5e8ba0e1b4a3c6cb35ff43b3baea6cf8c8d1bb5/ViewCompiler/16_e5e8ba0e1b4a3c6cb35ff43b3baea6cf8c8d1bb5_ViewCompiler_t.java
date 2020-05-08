 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * LZX View Compiler
  */
 
 package org.openlaszlo.compiler;
 
 import java.io.*;
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.util.*;
 
 import org.openlaszlo.css.CSSParser;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.xml.internal.Schema;
 import org.openlaszlo.xml.internal.MissingAttributeException;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.apache.log4j.*;
 import java.util.regex.*;
 import org.openlaszlo.compiler.ViewSchema.ColorFormatException;
 import org.openlaszlo.utils.FileUtils;
 
 import java.io.*;
 import java.util.*;
 import org.jdom.Attribute;
 import org.jdom.Element;
 
 /** Responsible for compiling elements that compile into instances of
  * LzNode.  This is called ViewCompiler for historical reasons; it
  * would more appropriately be called NodeCompiler.
  *
  * Node compilation consists of these steps:<ul>
  * <li> compute text metrics
  * <li> call the XML compiler to generate bytecodes that recreate the
  * view template subtree and pass it to a runtime view instantiation
  * library
  * </ul>
  */
 public class ViewCompiler extends ElementCompiler {
     // +=2 so you can see cursor at end of line
     private static final int INPUT_TEXTWIDTH_FUDGE_FACTOR = 2;
 
     private static final String FONTSTYLE_ATTRIBUTE = "fontstyle";
     private static final String WHEN_IMMEDIATELY = "immediately";
     private static final String WHEN_ONCE = "once";
     private static final String WHEN_ALWAYS = "always";
     private static final String WHEN_PATH = "path";
 
     private ViewSchema mSchema;
 
     private static Logger mLogger  = Logger.getLogger(ViewCompiler.class);
     private static Logger mTraceLogger  = Logger.getLogger("trace.xml");
 
     public ViewCompiler(CompilationEnvironment env) {
         super(env);
         mSchema = env.getSchema();
         mTraceLogger.setLevel(Level.INFO);
     }
 
 
     private static final String SERVERLESS_WARNINGS_PROPERTY_FILE = (
         LPS.getMiscDirectory() + File.separator + "lzx-server-only-apis.properties"
         );
     private static final Properties sServerOnlyTags = new Properties();
 
     static {
         try {
             InputStream is = new FileInputStream(SERVERLESS_WARNINGS_PROPERTY_FILE);
             try {
                 sServerOnlyTags.load(is);
             } finally {
                 is.close();
             }
         } catch (java.io.IOException e) {
             mLogger.warn("Can't find server-only APIs config file  " + SERVERLESS_WARNINGS_PROPERTY_FILE);
         }
     }
 
 
   /** An instance may have mixins, we use this opportunity to declare
    * the interstitial classes here if needed, and rewrite the element
    * to have a base class being the most-specific class of the
    * interstitials.
    *
    * <bar name="foo" with="bletch, crud">...</bar>
    *
    * becomes:
    * 
    * <class name="$crud$bar" extends="bar">...crud body...</class>
    * <class name="$bletch$crud$bar" extends="crud$bar">...bletch body</class>
    * <$bletch$crud$bar name="foo">...foo body...</$bletch$crud$bar>
    *
    */
     void updateSchema(Element element, ViewSchema schema, Set visited) {
       super.updateSchema(element, schema, visited);
       // If there's a non-null value for 'with', the schema builder will
       // create the needed interstitial classes for us. 
       String mixinSpec = element.getAttributeValue("with");
       String tagname = element.getName();
      if ("anonymous".equals(tagname)) {
        // User is not allowed to put <anonymous> tags in their code
        throw new CompilationError("You may not use the tag name 'anonymous' in user code", element);
      }
       boolean isclassdef = ("class".equals(tagname) || "interface".equals(tagname) || "mixin".equals(tagname));
       // We only need to invoke the schema builder if we are an
       // instance, and we have mixins.
       //
       // If ELEMENT is a <class>,<interface>, or <mixin>, then
       // ClassCompiler.updateSchema method will already have called
       // schema.addElement to declare the class.
       //
       if (!isclassdef && mixinSpec != null) {
         element.setAttribute("extends", element.getName());
         element.setName("anonymous");
         schema.addElement(element, "anonymous", mEnv, false, false);
       }
 
       Iterator iterator = element.getChildren().iterator();
       while (iterator.hasNext()) {
         Element child = (Element) iterator.next();
         String childname = child.getName();
         if ("class".equals(childname) || "interface".equals(childname) || "mixin".equals(childname)) {
           mEnv.warn(
               // TODO [2007-09-26 hqm] i18n this
               "The tag '" + child.getName() +
               "' cannot be used as a child of " + tagname,
               element);
         }
 
         if (!NodeModel.isPropertyElement(child) &&
             !(child.getName().equals("doc"))) {
           // Ignore documentation nodes
           Compiler.updateSchema(child, mEnv, schema, visited);
         }
       }
     }
 
     public void compile(Element element) throws CompilationError
     {
         FontInfo fontInfo = null;
 
         String name = element.getName();
         if (mEnv.isCanvas()) {
           if (name != null
               && sServerOnlyTags.containsKey(name)
               && !mEnv.getCanvas().isProxied()) {
               mEnv.warn(
   /* (non-Javadoc)
    * @i18n.test
    * @org-mes="The tag '" + p[0] + "' will not work as expected when used in a serverless application."
    */
               org.openlaszlo.i18n.LaszloMessages.getMessage(
                   ViewCompiler.class.getName(),"051018-99", new Object[] {name})
               );
           }
 
           fontInfo = new FontInfo(mEnv.getCanvas().getFontInfo());
           try {
               fontInfo = new FontInfo(mEnv.getCanvas().getFontInfo());
               mapTextMetricsCompilation(element, mEnv, fontInfo, new HashSet());
           } catch (NumberFormatException e) {
               throw new CompilationError(e.getMessage());
           }
         }
         compileXML(element, fontInfo);
     }
 
     /** Returns true if this node applies to the element.  Anything
      * that the interface compiler doesn't recognize is considered a
      * view node, so this always returns true.
      * @param element an element
      * @return see doc
      */
     static boolean isElement(Element element) {
         return true;
     }
 
     /** Collect the names of classes that are referenced. */
     static void collectElementNames(Element element, Set names) {
         // This also collects "attribute", "method", and HTML element
         // names, but that's okay since none of them has an autoinclude
         // entry.
         names.add(element.getName());
         collectLayoutElement(element, names);
         collectMixinElement(element, names);
         for (Iterator iter = element.getChildren().iterator();
              iter.hasNext(); ) {
             collectElementNames((Element) iter.next(), names);
         }
     }
 
     static void collectMixinElement(Element element, Set names) {
         String mixinSpec = element.getAttributeValue("with");
         if (mixinSpec != null) {
           String mixins[] = mixinSpec.trim().split("\\s+,\\s+");
           for (int i = 0; i < mixins.length; i++) {
             names.add(mixins[i]);
           }
         }
     }
 
     static void collectLayoutElement(Element element, Set names) {
         if (element.getAttributeValue("layout") != null) {
             try {
                 Map properties = new CSSParser
                   (new AttributeStream(element, "layout")).Parse();
                 String layoutClass = (String) properties.get("class");
                 if (layoutClass == null)
                     layoutClass = "simplelayout";
                 names.add(layoutClass);
             } catch (org.openlaszlo.css.ParseException e) {
             } catch (org.openlaszlo.css.TokenMgrError e) {
                 // The compilation phase will report the error.
             }
         }
     }
 
     /** Compile a XML element and generate code that binds it to a
      * runtime data structure named _lzViewTemplate.
      *
      * @param element an element
      * @param fontInfo font info inherited from canvas
      */
     void compileXML(Element element, FontInfo fontInfo)
     {
         // TODO: [2007-09-03 ptw] (LPP-4634) You should not be able to
         // get here.  This appears to happen when a binary file has
         // already included a sub-library and that same sub-library is
         // included by a source file.  Since the include is already
         // loaded, it is not expanded in expandIncludes, but because
         // ViewCompiler.isElement returns true for everything, we end up
         // here.  For now, just don't do anything.
         if ("include".equals(element.getName())) return;
 
         // TODO: [12-27-2002 ows] use the log4j API instead of this property
         boolean tracexml = mEnv.getBooleanProperty("trace.xml");
         if (tracexml) {
             mTraceLogger.info("compiling XML:");
             org.jdom.output.XMLOutputter outputter =
                 new org.jdom.output.XMLOutputter();
             mTraceLogger.info(outputter.outputString(element));
         }
 
         NodeModel model = NodeModel.elementAsModel(element, mSchema, mEnv);
         String script = VIEW_INSTANTIATION_FNAME + "(" +
           model.asJavascript() + ", " + model.totalSubnodes() +
           ");";
 
         // Don't keep non-class models around
         if (!element.getName().equals("class")) {
             ((ElementWithLocationInfo) element).model = null;
         }
 
         if (tracexml) {
             mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="compiled to:\n" + p[0] + "\n"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ViewCompiler.class.getName(),"051018-186", new Object[] {script})
 );
         }
         try {
             mEnv.compileScript(script, element);
         } catch (CompilationError e) {
             String solution = SolutionMessages.findSolution(e.getMessage());
             e.setSolution(solution);
             throw e;
         }
     }
 
     /**
      * Modify the DOM in place, to what the runtime expects.  This
      * function encapsulates the behavior that is common to root
      * views, and class definitions.
      *
      * Preprocessing consists of compiling resources, and turning
      * view-specific source-format attributes into runtime format
      *
      * @param elt an <code>Element</code> value
      * @param env a <code>CompilationEnvironment</code> value
      */
   void preprocess(Element elt, CompilationEnvironment env) {
       compileResources(elt, env);
       compileClickResources(elt, env);
       compileAttributes(elt, env);
     }
 
     /**
      * Modify elt and its children to replace source attribute values
      * by runtime values.
      */
     static void compileAttributes(Element elt, CompilationEnvironment env) {
         if (elt.getName().equals("dataset")) {
             String src = elt.getAttributeValue("src");
             if (src == null) {
                 // This is a local dataset.  DataCompiler has already
                 // processed it.  TBD: move this check to isElement,
                 // and make it an assert since DataCompiler should
                 // have already processed it.
                 return;
             }
             src = env.adjustRelativeURL(src, elt);
             elt.setAttribute("src", src);
         }
         Iterator iter;
         for (iter = elt.getChildren().iterator(); iter.hasNext(); ) {
                 compileAttributes((Element)iter.next(), env);
         }
     }
 
     static HashMap sUnsupportedServerlessFiletypes = new HashMap();
     static HashMap sUnsupportedServerlessFiletypesSWF7 = new HashMap();
     {
         sUnsupportedServerlessFiletypes.put("bmp", "true");
         sUnsupportedServerlessFiletypes.put("tiff", "true");
         sUnsupportedServerlessFiletypes.put("tif", "true");
         sUnsupportedServerlessFiletypes.put("wmf", "true");
         sUnsupportedServerlessFiletypes.put("wmv", "true");
 
         sUnsupportedServerlessFiletypesSWF7.put("png", "true");
         sUnsupportedServerlessFiletypesSWF7.put("gif", "true");
 
     }
 
     static void checkUnsupportedMediaTypes(CompilationEnvironment env, Element elt, String url) {
         String suffix = FileUtils.getExtension(url);
         if (env.isSWF()) {
           if ( (sUnsupportedServerlessFiletypes.containsKey(suffix.toLowerCase())) ||
                (env.getSWFVersionInt() < 8 &&
                 sUnsupportedServerlessFiletypesSWF7.containsKey(suffix.toLowerCase())))
                {
               env.warn(
   /* (non-Javadoc)
    * @i18n.test
    * @org-mes="The runtime loadable resource type '" + p[0] + " is not supported by the Flash runtime. Supported resource types are JPEG (non-interlaced), SWF, and MP3"
    */
               org.openlaszlo.i18n.LaszloMessages.getMessage(
                   ViewCompiler.class.getName(),"051018-258", new Object[] {url})
   , elt);
           }
         } else {
           // TODO: [2006-06-19 ptw] Handle media types for DHTML, etc.
         }
     }
 
     /**
      * Compiles all resources under the current element
      *
      * @param env
      * @param elt
      */
     static void compileResources(Element elt,
                                  CompilationEnvironment env) {
         final String RESOURCE_ATTR_NAME = "resource";
 
         // check for immediate <attribute name="resource" .../> children
         for (Iterator iter = elt.getChildren().iterator();
              iter.hasNext(); ) {
             Element child = (Element) iter.next();
             if (child.getName().equals("attribute") &&
                 RESOURCE_ATTR_NAME.equals(child.getAttributeValue("name"))) {
                 String val = child.getAttributeValue("value");
                 // You are not allowed declare a resource attribute value twice
                 if (val == null) {
                     continue;
                 } else {
                     String val2 = elt.getAttributeValue(RESOURCE_ATTR_NAME);
                     if (val2 != null) {
                         env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The resource attribute on this view was declared more than once, as '" + p[0] + "', and as '" + p[1] + "'"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ViewCompiler.class.getName(),"051018-292", new Object[] {val2, val})
 , elt);
                     }
 
                     // This is needed for backward compatibility with
                     // the deprecated "when='...'" syntax
                     String when = child.getAttributeValue("when");
                     if (when != null) {
                         val = "$" + when + "{" + val + "}";
                     }
 
                     elt.setAttribute(RESOURCE_ATTR_NAME, val);
                     // remove this <attribute name="resource" .../>
                     // child because we just copied the value to the
                     // parent elt.
                     iter.remove();
                 }
             }
         }
 
         String value = elt.getAttributeValue(RESOURCE_ATTR_NAME);
 
         if (value != null) {
             if (value.matches("\\s*\\$\\s*\\(")) {
                 env.warn(
                     "The syntax '$(...)' is not valid, "
                     + "you probably meant to use curly-braces instead '${...}'", elt);
                 //} else if (value.startsWith("$") && value.endsWith("}")) {
             } else if (value.matches(sConstraintPatStr)) {
                 // It's a $xxx{...} attribute initializer, let's not
                 // do anything at all, and let the viewsystem takes
                 // care of finding the resource by id.
             } else if (ScriptCompiler.isIdentifier(value)) {
                 // id: leave intact: nothing to do
                 Set resourceNames = env.getResourceNames();
                 if (!resourceNames.contains(value)) {
                     // Add this reference to be checked again after
                     // we've fully parsed the whole app.
                     env.addResourceReference(value, elt);
                 }
             } else if (XMLUtils.isURL(value)) {
                 if (env.isCanvas() && !env.getCanvas().isProxied()) {
                     checkUnsupportedMediaTypes(env, elt, value);
                 }
                 // URL: relativize, and rename to "source" for runtime
                 value = env.adjustRelativeURL(value, elt);
                 // If it's a relative pathname with no hostname
                 // (e.g. "http:resource"), the runtime expects a
                 // bare name (e.g. "resource")
                 try {
                     java.net.URL url = new java.net.URL(value);
                     if (url.getHost().equals("") && !url.getPath().startsWith("/")) {
                         value = url.getPath();
                         if  (url.getQuery() != null && url.getQuery().length() > 0) {
                             value += "?" + url.getQuery();
                         }
                     }
                 } catch (java.net.MalformedURLException e) {
                     throw new ChainedException(e);
                 }
                 elt.removeAttribute(RESOURCE_ATTR_NAME);
                 elt.setAttribute("source", value);
 
             } else {
                 // pathname: turn into an id
                 File file = env.resolveReference(elt, RESOURCE_ATTR_NAME);
                 // N.B.: Resources are always imported into the main
                 // program for the Flash target, hence the use of
                 // getResourceGenerator below
                 try {
                     value = env.getResourceGenerator().importResource(file);
                 } catch (ObjectWriter.ImportResourceError e) {
                     env.warn(e, elt);
                 }
                 elt.setAttribute(RESOURCE_ATTR_NAME, value);
 
                 if (env.isCanvas()) {
                   Element info = new Element("resolve");
                   info.setAttribute("src", elt.getAttributeValue(RESOURCE_ATTR_NAME));
                   try {
                     info.setAttribute("pathname", file.getCanonicalPath());
                   } catch (java.io.IOException ioe) {
                     mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't canonicalize " + p[0]
  */
                       org.openlaszlo.i18n.LaszloMessages.getMessage(
                         ViewCompiler.class.getName(),"051018-384", new Object[] {file.toString()})
                                  );
                   }
                   env.getCanvas().addInfo(info);
                 }
             }
         }
 
         // Recurse
         Iterator iter;
         for (iter = elt.getChildren().iterator();
              iter.hasNext(); ) {
             compileResources((Element) iter.next(), env);
         }
     }
 
     static void compileClickResources(Element elt,
                                       CompilationEnvironment env) {
         final String ATTR_NAME = "clickregion";
         String value = elt.getAttributeValue(ATTR_NAME);
 
         if (value != null) {
             if (value.matches(sConstraintPatStr) ||
                 ScriptCompiler.isIdentifier(value) ||
                 XMLUtils.isURL(value)) {
                 env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The value of the " + p[0] + "attribute" + "must be a file name."
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ViewCompiler.class.getName(),"051018-414", new Object[] {ATTR_NAME})
                         , elt);
             } else {
                 // pathname: turn into an id
                 File file = env.resolveReference(elt, ATTR_NAME);
                 try {
                     value = env.getResourceGenerator().importClickResource(file);
                 } catch (ObjectWriter.ImportResourceError e) {
                     env.warn(e, elt);
                 }
                 elt.setAttribute(ATTR_NAME, value);
             }
         }
 
         // Recurse
         Iterator iter;
         for (iter = elt.getChildren().iterator();
              iter.hasNext(); ) {
             compileClickResources((Element) iter.next(), env);
         }
     }
 
     static void checkUnresolvedResourceReferences (CompilationEnvironment env) {
         Map refs = env.resourceReferences();
         Set resourceNames = env.getResourceNames();
         for (Iterator iter = refs.keySet().iterator();
              iter.hasNext(); ) {
             String resourceId = (String) iter.next();
             Element elt = (Element) refs.get(resourceId);
             if (!resourceNames.contains(resourceId)) {
                 env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The resource named '" + p[0] + "' has not been declared"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ViewCompiler.class.getName(),"051018-450", new Object[] {resourceId})
 , elt);
             }
         }
     }
 
     /**
      * Walk the whole superclass chain, starting at the root, merging fontInfo.
      */
     protected static void mergeClassFontInfo (Element elt, FontInfo fontInfo,
                                               CompilationEnvironment env) {
         String classname = elt.getName();
         // check for a cached fontInfo on the class
         FontInfo cachedInfo = env.getClassFontInfo(classname);
         if (cachedInfo != null) {
             fontInfo.mergeFontInfoFrom(cachedInfo);
             return;
         }
 
         ViewSchema schema = env.getSchema();
         ClassModel classinfo =  schema.getClassModel(classname);
         if (classinfo == null || classinfo.definition == null) {
             return;
         }
 
         // Build a list of superclasses
         Vector parents = new Vector();
         ClassModel lzxclass = classinfo;
         // walk
         while (lzxclass != null) {
             parents.insertElementAt(lzxclass, 0);
             lzxclass = lzxclass.superModel;
         }
 
         // A blank FontInfo with all empty slots
         FontInfo cinfo = FontInfo.blankFontInfo();
 
         // Pop off elements starting at base class
         while (parents.size() > 0) {
             lzxclass = (ClassModel) parents.firstElement();
             parents.removeElementAt(0); // pop
             mergeClassFontInfo(lzxclass, cinfo);
         }
 
         env.addClassFontInfo(classname, cinfo);
         // apply the class' style changes, if any, to our fontInfo arg
         fontInfo.mergeFontInfoFrom(cinfo);
     }
 
     /**
      * Merge FontInfo from a class definition.
      */
     protected static void mergeClassFontInfo (ClassModel classinfo,
                                               FontInfo fontInfo) {
         if (classinfo != null && classinfo.definition != null) {
             Element celt = classinfo.definition;
             mergeFontInfo(celt, fontInfo);
         }
     }
 
     /**
      * Adds in text widths for all text views below this element that
      * need them.  This walks down into class definitions, merging
      * font info as it goes.  We don't need to walk into class defs
      * for measuring text, since we have no way to pass those text
      * widths to the runtime, but we do need this to check if we need
      * to import the default bold or italic fonts.
      *
      *
      *
      * @param env
      * @param elt
      * @param fontInfo the current font name/style/size
      */
     protected void mapTextMetricsCompilation(Element elt,
                                              CompilationEnvironment env,
                                              FontInfo fontInfo,
                                              Set classList) {
 
 
         // Clone a copy of the font info
         fontInfo = new FontInfo(fontInfo);
 
         // Check class defaults for font info
         mergeClassFontInfo (elt, fontInfo, env);
         // Now override with any directly declared attributes
         mergeFontInfo(elt, fontInfo);
 
         String fontName = fontInfo.getName();
 
         // If it inherits from text or inputttext, annotate it with font info
         String eltName = elt.getName();
         if ("text".equals(eltName) ||
             "text".equals(mSchema.getBaseClassname(eltName)) ||
             "inputtext".equals(eltName) ||
             "inputtext".equals(mSchema.getBaseClassname(eltName))) {
             compileTextMetrics(elt, env, fontInfo);
         }
         ClassModel classinfo =  env.getSchema().getClassModel(eltName);
 
         // If this invokes a 'user-defined' class, let's walk that
         // class's source tree now
         if (classinfo != null && classinfo.definition != null) {
             classList = new HashSet(classList);
             // check if we are in an instance of a class that we are
             // already descended into (loop detection)
             if (classList.contains(eltName.intern())) {
                 return;
             }
             for (Iterator iter = classinfo.definition.getChildren().iterator(); iter.hasNext();
                  ) {
                 Element e = (Element) iter.next();
                 String ename = e.getName();
                 if (!(ename.equals("method") || ename.equals("attribute"))) {
                     // Avoid recursively traversing class definitions.
                     // Mark this class as having been traversed, to
                     // avoid loops.
                     classList.add(classinfo.tagName.intern());
                     mapTextMetricsCompilation(e, env, fontInfo, classList);
                 }
             }
         }
 
         // Now do immediate children
         for (Iterator iter = elt.getChildren().iterator(); iter.hasNext();
              ) {
             Element e = (Element) iter.next();
             mapTextMetricsCompilation(e, env, fontInfo, classList);
         }
     }
 
     /** Merges font name/size/style from an element's direct
      * attributes into a FontInfo */
     protected static void mergeFontAttributes (Element elt, FontInfo fontInfo) {
         String myfont = getAttributeValue(elt, "font");
         if (myfont != null) {
             if (myfont.matches("\\s*[^${}]*\\s*")) {
                 fontInfo.setName(myfont);
             } else {
                 // we don't know what font value is, so set back to the 'unknown' value
                 fontInfo.setName(FontInfo.NULL_FONT);
             }
         }
 
         String mysize = getAttributeValue(elt, "fontsize");
         if (mysize != null) {
             if (mysize.matches(sFontSizePatStr)) {
                 fontInfo.setSize(mysize);
             } else {
                 // we don't know what font size is, so set back to the
                 // 'unknown' value
                 fontInfo.setSize(FontInfo.NULL_SIZE);
             }
         }
 
         String mystyle = getAttributeValue(elt, NodeModel.FONTSTYLE_ATTRIBUTE);
         if (mystyle != null) {
             if (mystyle.matches(sFontstylePatStr)) {
                 fontInfo.setStyle(mystyle);
             } else {
                 // we don't know what font size is, so set back to the 'unknown' value
                 fontInfo.setStyleBits(FontInfo.NULL_STYLE);
             }
         }
     }
 
     /** Merge in font attribute info from an element into a FontInfo.
      *
      * @param elt the element to look for font attributes on
      * @param fontInfo  merge font attribute info into this struct
      */
     private static void mergeFontInfo(Element elt, FontInfo fontInfo) {
         mergeFontAttributes(elt, fontInfo);
 
         // Static sized textfield optimization; need to cascade resizable
         String resizable = getAttributeValue(elt, "resizable");
         if ("true".equals(resizable)) {
             fontInfo.resizable = FontInfo.FONTINFO_TRUE;
         } else if ("false".equals(resizable)) {
             fontInfo.resizable = FontInfo.FONTINFO_FALSE;
         }
 
         // Static sized textfield optimization; need to cascade multiline
         String multiline = getAttributeValue(elt, "multiline");
         if ("true".equals(multiline)) {
             fontInfo.multiline = FontInfo.FONTINFO_TRUE;
         } else if ("false".equals(multiline)) {
             fontInfo.multiline = FontInfo.FONTINFO_FALSE;
         }
     }
 
     /** Pattern matcher for compile-time optimizations */
     static String sConstPatStr = "\\s*(\\d*)\\s*";
     static String sFontstylePatStr = "\\s*(bold italic|bold-italic|bold|plain|italic)\\s*";
     static String sFontSizePatStr = "\\s*\\d*\\s*";
     static String sFontNamePatStr = "\\s*[^${}]*\\s*";
     static String sConstraintPatStr = "^\\s*\\$(\\w*)\\{(.*)}\\s*";
     static final Pattern sConstraintPat;
 
     static {
         // $once{parent +|- DDDDD}
         sConstraintPat   = Pattern.compile(sConstraintPatStr);
     }
 
     /** return true if element has an attribute named ATTRIBUTE in
      * it's attribute list, or has a child lzx element
      * <attribute name="ATTRIBUTE"/>
      */
     protected static boolean hasAttribute(Element elt, String attrName) {
         if (elt.getAttributeValue(attrName) != null) {
             return true;
         }
 
         Iterator iter;
         for (iter = elt.getChildren().iterator(); iter.hasNext(); ) {
             Element child = (Element)iter.next();
             if ((child.getName().equals("attribute"))  &&
                 (child.getAttribute(attrName) != null)) {
                 return true;
             }
         }
         return false;
     }
 
 
     /** return value if element has an attribute named ATTRNAME in
      * it's attribute list, or has a child lzx element
      * <attribute name="ATTRNAME" value="VAL"/>
      */
     protected static String getAttributeValue(Element elt, String attrName) {
         String attrval = elt.getAttributeValue(attrName);
         if (attrval != null) {
             return attrval;
         }
 
         Iterator iter;
         for (iter = elt.getChildren().iterator(); iter.hasNext(); ) {
             Element child = (Element)iter.next();
             if ((child.getName().equals("attribute"))  &&
                 attrName.equals(child.getAttributeValue("name"))) {
                 return child.getAttributeValue("value");
             }
         }
         return null;
     }
 
 
     /**
      * Adds in text metrics for this element.
      *
      * @param env
      * @param elt
      * @param fontInfo font information for this element
      */
     private void compileTextMetrics(Element elt,
                                     CompilationEnvironment env,
                                     FontInfo fontInfo) {
 
 
         if (fontInfo.getName() != null) {
             elt.setAttribute("font", fontInfo.getName());
         }
 
         if (fontInfo.getSize() != -1) {
             elt.setAttribute("fontsize", "" + fontInfo.getSize());
         }
 
         if (fontInfo.getStyle() != null) {
             elt.setAttribute("fontstyle", fontInfo.getStyle());
         }
 
     }
 
 
     static void setFontInfo(FontInfo info, Element elt) {
         String face = elt.getAttributeValue("face");
         String size = elt.getAttributeValue("size");
 
         if (face != null) {
             info.setName(face);
         }
         if (size != null) {
             info.setSize(size);
         }
     }
 
 }
 
 /**
  * @copyright Copyright 2001-2009 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
