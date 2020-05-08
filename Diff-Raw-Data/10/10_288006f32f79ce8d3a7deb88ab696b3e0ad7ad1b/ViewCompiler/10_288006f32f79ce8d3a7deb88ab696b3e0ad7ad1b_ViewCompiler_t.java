 /* ****************************************************************************
  * ViewCompiler.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2006 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
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
 
     public void compile(Element element) throws CompilationError
     {
         preprocess(element, mEnv);
         FontInfo fontInfo;
 
         String name = element.getName();
         if (name != null
             && !mEnv.getCanvas().isProxied()
             && sServerOnlyTags.containsKey(name)) {
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
         names.add(element.getName());
         collectLayoutElement(element, names);
         for (Iterator iter = element.getChildren().iterator();
              iter.hasNext(); ) {
             collectElementNames((Element) iter.next(), names);
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
      * @param schema a schema
      * @param fontInfo font info inherited from canvas
      */
     void compileXML(Element element, FontInfo fontInfo)
     {
         // TODO: [12-27-2002 ows] use the log4j API instead of this property
         boolean tracexml = mEnv.getBooleanProperty("trace.xml");
         if (tracexml) {
             mTraceLogger.info("compiling XML:");
             org.jdom.output.XMLOutputter outputter =
                 new org.jdom.output.XMLOutputter();
             mTraceLogger.info(outputter.outputString(element));
         }
         
         NodeModel model = NodeModel.elementAsModel(element, mSchema, mEnv);
         model = model.expandClassDefinitions();
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
     static void preprocess(Element elt, CompilationEnvironment env) {
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
                 if (!env.getCanvas().isProxied()) {
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
                 elt.setAttribute("source", ScriptCompiler.quote(value));
 
             } else {
                 // pathname: turn into an id
                 Element info = new Element("resolve");
                 info.setAttribute("src", elt.getAttributeValue(RESOURCE_ATTR_NAME));
 
                 File file = env.resolveReference(elt, RESOURCE_ATTR_NAME);
                 Element rinfo = new Element("resolve");
                     rinfo.setAttribute("src", elt.getAttributeValue(RESOURCE_ATTR_NAME));
                     rinfo.setAttribute("pathname", file.getPath());
                 env.getCanvas().addInfo(rinfo);
                 // N.B.: Resources are always imported into the main
                 // program for the Flash target, hence the use of
                 // getResourceGenerator below
                 try {
                     value = env.getResourceGenerator().importResource(file);
                 } catch (SWFWriter.ImportResourceError e) {
                     env.warn(e, elt);
                 }
                 elt.setAttribute(RESOURCE_ATTR_NAME, value);
 
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
                 } catch (SWFWriter.ImportResourceError e) {
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
             lzxclass = lzxclass.superclass;
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
             // Propagate width/height info so children can compute
             // ${parent.width}, ${parent.height}
             mergeWidth(fontInfo, getAttributeValue(celt, "width"));
             mergeHeight(fontInfo, getAttributeValue(celt, "height"));
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
 
         classList = new HashSet(classList);
 
         // Clone a copy of the font info
         fontInfo = new FontInfo(fontInfo);
 
         // Check class defaults for font info
         mergeClassFontInfo (elt, fontInfo, env);
         // Now override with any directly declared attributes
         mergeFontInfo(elt, fontInfo);
         
         FontManager fmgr = env.getGenerator().getFontManager();
         String fontName = fontInfo.getName();
 
 
         // If it inherits from text or inputttext, annotate it with font info
         if ("text".equals(elt.getName()) ||
             "text".equals(mSchema.getBaseClassname(elt.getName())) ||
             "inputtext".equals(elt.getName()) ||
             "inputtext".equals(mSchema.getBaseClassname(elt.getName()))) {
             compileTextMetrics(elt, env, fontInfo);
         }
         ClassModel classinfo =  env.getSchema().getClassModel(elt.getName());
 
         // If this invokes a 'user-defined' class, let's walk that
         // class's source tree now
         if (classinfo != null && classinfo.definition != null) {
             // check if we are in an instance of a class that we are
             // already descended into (loop detection)
             if (classList.contains(elt.getName().intern())) {
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
                     classList.add(classinfo.className.intern());
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
 
     /** Merge a width attribute into a fontInfo. The width is a string
      * which could have these possible values:
      *
      * [1] numeric constant
      * [2] $once{parent.xxx +/- nnn}
      * [3] other... set width to NULL_SIZE 
     */
     protected static void mergeWidth(FontInfo fontInfo, String width) {
         if (width != null) {
             int val = evalWidth(fontInfo, width);
             fontInfo.setWidth(val);
         }
     }
 
     protected static void mergeHeight(FontInfo fontInfo, String height) {
         if (height != null) {
             int val = evalHeight(fontInfo, height);
             fontInfo.setHeight(val);
         }
     }
 
 
     /** Evaluate a width expression in the context of a parent view
      *(the context is held by the fontInfo).
      *
      * The width is a string which could have these
      *
      * possible values:
      * [1] numeric constant
      * [2] $once{parent.xxx +/- nnn}
      * [3] other... set width to NULL_SIZE 
      *
      * If the case is [2], a constraint, we evaluate with respect to the parent
      * width info, which is in fontInfo.width
      *
      * @return a width or if the width is not known, return the
      * special "null width" FontInfo.NULL_SIZE
     */
     protected static int evalWidth(FontInfo fontInfo, String width) {
         if (width == null) {
             return FontInfo.NULL_SIZE;
         }
 
         if (width.matches(sConstPatStr)) {
             return Integer.parseInt(width);
         } else if (width.matches(sParentWidthPatStr)) {
             // if there is no defined parent width, we can't compute
             // any meaningful result
             Matcher m = sParentWidthPat.matcher(width);
            if (m.groupCount() != 4) {
                return FontInfo.NULL_SIZE;
            }
             if (fontInfo.getWidth() == FontInfo.NULL_SIZE) {
                 return FontInfo.NULL_SIZE;
             }
             // Get numeric operator and arg
             String operator = m.group(3);
             if (operator.equals("")) {
                 operator = "+";
             }
 
             int arg = 0;
             String argstr = m.group(4);
             try {
                 if (argstr != null) {
                     arg = Integer.parseInt(argstr);
                 }
             } catch (NumberFormatException e) {}
             if (operator.equals("+")) {
                 return (fontInfo.getWidth() + arg);
             } else if (operator.equals("-")) {
                 return (fontInfo.getWidth() - arg);
             } else {
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Unknown operator in width attribute: " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ViewCompiler.class.getName(),"051018-721", new Object[] {operator})
 );
             }
         } else {
             // We cannot determine what the dimension is, so set it
             // return the unknown value
             return FontInfo.NULL_SIZE;
         }
     }
 
     protected static int evalHeight(FontInfo fontInfo, String height) {
         if (height == null) {
             return FontInfo.NULL_SIZE;
         }
 
         if (height.matches(sConstPatStr)) {
             return Integer.parseInt(height);
         } else if (height.matches(sParentHeightPatStr)) {
             // if there is no defined parent height, we can't computer
             // any meaningful result
             if (fontInfo.getHeight() == FontInfo.NULL_SIZE) {
                 return FontInfo.NULL_SIZE;
             }
             Matcher m = sParentHeightPat.matcher(height);
            if (m.groupCount() != 4) {
                return FontInfo.NULL_SIZE;
            }
             // Get numeric operator and arg
             String operator = m.group(3);
             if (operator.equals("")) {
                 operator = "+";
             }
 
             int arg = 0;
             String argstr = m.group(4);
             try {
                 if (argstr != null) {
                     arg = Integer.parseInt(argstr);
                 }
             } catch (NumberFormatException e) {}
             if (operator.equals("+")) {
                 return (fontInfo.getHeight() + arg);
             } else if (operator.equals("-")) {
                 return (fontInfo.getHeight() - arg);
             } else {
                 throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Unknown operator in height attribute: " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ViewCompiler.class.getName(),"051018-768", new Object[] {operator})
 );
             }
         } else {
             // We cannot determine what the dimension is, so set it return the unknown value
             return FontInfo.NULL_SIZE;
         }
     }
 
     /** Pattern matcher for compile-time optimizations */
     static String sConstPatStr = "\\s*(\\d*)\\s*";
     static String sParentWidthPatStr = "\\s*\\$(once|always)?\\s*[{](parent.width)\\s*([+-]?)\\s*(\\d*)\\s*}\\s*";
     static final Pattern sParentWidthPat; // $once{parent.width +|- DDDD}
     static String sParentHeightPatStr = "\\s*\\$(once|always)?\\s*[{](parent.height)\\s*([+-]?)\\s*(\\d*)\\s*}\\s*";
     static final Pattern sParentHeightPat; // $once{parent.height +|- DDDD}
     static String sFontstylePatStr = "\\s*(bold italic|bold-italic|bold|plain|italic)\\s*";
     static String sFontSizePatStr = "\\s*\\d*\\s*";
     static String sFontNamePatStr = "\\s*[^${}]*\\s*";
     static String sConstraintPatStr = "^\\s*\\$(\\w*)\\{(.*)}\\s*";
     static final Pattern sConstraintPat;
 
     static {
         // $once{parent +|- DDDDD}
         sParentWidthPat  = Pattern.compile(sParentWidthPatStr);
         sParentHeightPat = Pattern.compile(sParentHeightPatStr);
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
      * Collect font/size/style used by input text elements, and tell
      * the SWFWriter about them.
      *
      * @param env
      * @param elt
      * @param fontInfo the current font name/style/size
      */
     protected static void collectInputFonts(Element elt,
                                             CompilationEnvironment env,
                                             FontInfo fontInfo,
                                             Set classList)
     {
         // mLogger.debug("collectInputFonts");
         ViewSchema schema = env.getSchema();
 
         // Clone a copy of the font info
         fontInfo = new FontInfo(fontInfo);
         // Add in class defaults for font info
         mergeClassFontInfo (elt, fontInfo, env);
         mergeFontInfo(elt, fontInfo);
 
         // If this element is input text, tell the generator about what font it needs
         if (schema.isInputTextElement(elt)) {
             final SWFWriter generator = env.getGenerator();
             boolean password = elt.getAttributeValue("password", "false").equals("true");
             boolean multiline = (fontInfo.multiline == FontInfo.FONTINFO_TRUE);
             int resizable = fontInfo.resizable;
 
             int width  = evalWidth(fontInfo, elt.getAttributeValue("width"));
             // Use height attribute if any, otherwise check 'textheight' attribute
             String heightstr = elt.getAttributeValue("height");
             if (heightstr == null) {
                 heightstr = elt.getAttributeValue("textheight");
             }
             int height = evalHeight(fontInfo, heightstr);
 
             // If this is a single line textfield, then we can get the
             // height from the font height
             if (!multiline && (height == FontInfo.NULL_SIZE) && fontInfo.isFullySpecified()) {
                 height = (int) Math.ceil(generator.getLFCLineHeight(fontInfo, fontInfo.getSize()));
             }
 
             // Late breaking news: if we have no width yet, let's use
             // default text width.
             if (width == FontInfo.NULL_SIZE) {
                 width = env.getDefaultTextWidth();
             }
             
             // If width and height are not both integers, must be resizable
             if ((width == FontInfo.NULL_SIZE) || (height == FontInfo.NULL_SIZE)) {
                 resizable = FontInfo.FONTINFO_TRUE;
             }
             boolean fixedsize = (resizable != FontInfo.FONTINFO_TRUE);
 
             // must have concrete values for font name/size/style
             if (!fontInfo.isFullySpecified()) {
                 //mLogger.info("[1] elt="+elt.getName()+" font not fully specified: "+fontInfo);
                 return;
             }
 
             if (fixedsize) {
                 generator.addCustomInputText(fontInfo, width,  height, multiline, password);
             } 
             // failsafe - generate resizable text field
             // If password bit is set, allocate both a password and non-password resource,
             // in case it's a component which wants to do both.
             if (hasAttribute(elt, "password")) {
                 generator.addInputText(fontInfo, true);
                 generator.addInputText(fontInfo, false);
             } else {
                 //mLogger.info("[2] elt="+elt.getName()+" add inputfont "+fontInfo);
                 generator.addInputText(fontInfo, false);
             }
         }
         
         // Propagate width/height info so children can compute ${parent.width}, ${parent.height}
         mergeWidth(fontInfo, getAttributeValue(elt, "width"));
         mergeHeight(fontInfo, getAttributeValue(elt, "height"));
 
         ClassModel classinfo =  schema.getClassModel(elt.getName());
 
         // Build a list of superclasses 
         Vector parents = new Vector();
         ClassModel lzxclass = classinfo;
         // walk 
         while (lzxclass != null) {
             parents.insertElementAt(lzxclass, 0);
             lzxclass = lzxclass.superclass;
         }
 
         // Pop off elements starting at base class
         while (parents.size() > 0) {
             lzxclass = (ClassModel) parents.firstElement();
             parents.removeElementAt(0); // pop
             //mergeClassFontInfo(lzxclass, cinfo);
             if (lzxclass.definition != null) {
                 // check if we are in an instance of a class that we are already descended into (loop detection)
                 if (classList.contains(elt.getName().intern())) {
                     return;
                 }
                 for (Iterator iter = lzxclass.definition.getChildren().iterator(); iter.hasNext();
                      ) {
                     Element e = (Element) iter.next();
                     String ename = e.getName();
                     if (!(ename.equals("method") || ename.equals("attribute"))) {
                         // Avoid recursively traversing class definitions.
                         // Mark this class as having been traversed, to avoid loops.
                         HashSet nclassList = new HashSet(classList);
                         nclassList.add(lzxclass.className.intern());
                         collectInputFonts(e, env, fontInfo, nclassList);
                     }
                 }
             }
         }
 
         // Now do immediate children
         for (Iterator iter = elt.getChildren().iterator(); iter.hasNext();
              ) {
             Element e = (Element) iter.next();
             String ename = e.getName();
             if (!(ename.equals("method") || ename.equals("attribute"))) {
                 collectInputFonts(e, env, fontInfo, classList);
             }
         }
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
 
 
