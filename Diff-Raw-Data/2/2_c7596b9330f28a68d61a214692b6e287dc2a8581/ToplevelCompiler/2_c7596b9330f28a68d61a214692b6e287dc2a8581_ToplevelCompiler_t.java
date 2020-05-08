 /* *****************************************************************************
  * ToplevelCompiler.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.util.*;
 import java.io.*;
 
 import org.jdom.Element;
 import org.openlaszlo.compiler.ViewCompiler.*;
 import org.openlaszlo.server.*;
 import org.openlaszlo.utils.*;
 import org.jdom.*;
 import org.apache.log4j.*;
 
 /** Compiler for <code>canvas</code> and <code>library</code> elements.
  */
 abstract class ToplevelCompiler extends ElementCompiler {
     /** Logger */
     private static Logger mLogger = Logger.getLogger(ToplevelCompiler.class);
 
     ToplevelCompiler(CompilationEnvironment env) {
         super(env);
     }
     
     /** Returns true if the element is capable of acting as a toplevel
      * element.  This is independent of whether it's positioned as a
      * toplevel element; CompilerUtils.isTopLevel() tests for position
      * as well. */
     static boolean isElement(Element element) {
         return CanvasCompiler.isElement(element)
             || LibraryCompiler.isElement(element)
             || SwitchCompiler.isElement(element);
     }
     
     public void compile(Element element) {
         for (Iterator iter = element.getChildren().iterator();
              iter.hasNext(); ) {
             Element child = (Element) iter.next();
             if (!NodeModel.isPropertyElement(child)) {
                 Compiler.compileElement(child, mEnv);
             }
         }
     }
     
     /** Parses out user class definitions.
      *
      * <p>
      * Iterates the direct children of the top level of the DOM tree and
      * look for elements named "class", find the "name" and "extends"
      * attributes, and enter them in the ViewSchema.
      *
      * Also check for the "validate" attribute, to optionally disable validator.
      *
      * @param visited {canonical filenames} for libraries whose
      * schemas have been visited; used to prevent recursive
      * processing.
      * 
      */
     void updateSchema(Element element, ViewSchema schema, Set visited) {
         setValidateProperty(element, mEnv);
         Iterator iterator = element.getChildren().iterator();
         while (iterator.hasNext()) {
             Element child = (Element) iterator.next();
             if (!NodeModel.isPropertyElement(child)) {
                 Compiler.updateSchema(child, mEnv, schema, visited);
             }
         }
     }
 
 
     /**
      * Look for the "validate" attribute on canvas or at top level of imported libraries
      *
      * We look these places for the validate attribute:
      *   <li>  canvas (root) element
      *   <li>  direct child atttribute of canvas
      * @param root source code document root
      * @param env the CompilationEnvironment
      */
     void setValidateProperty(Element root , CompilationEnvironment env) {
         String validate = CompilationEnvironment.VALIDATE_PROPERTY;
         // Look for canvas attribute
         if (root.getAttributeValue(validate) != null) {
             if ("false".equals(root.getAttributeValue("validate"))) {
                 env.setProperty(validate, false);
             } else {
                 env.setProperty(validate, true);
             }
         }
 
         // Look for direct canvas children <attribute name="validate" value="false">
         for (Iterator iter = root.getChildren().iterator();
              iter.hasNext(); ) {
             Element child = (Element) iter.next();
             if (child.getName().equals("attribute")
                 && validate.equals(child.getAttributeValue("name"))) {
                 if ("false".equals(child.getAttributeValue("value"))) {
                     env.setProperty(validate, false);
                 }
             }
         }
     }
 
 
     static void collectReferences(CompilationEnvironment env,
                                   Element element, Set defined,
                                   Set referenced)
     {
         Set visited = new HashSet();
         ViewCompiler.collectLayoutElement(element, referenced);
         collectReferences(env, element, defined, referenced, visited);
     }
 
     /** This also collects "attribute", "method", and HTML element
      * names, but that's okay since none of them has an autoinclude
      * entry.
      */
     static void collectReferences(CompilationEnvironment env,
                                   Element element, Set defined,
                                   Set referenced, Set libsVisited) {
         ElementCompiler compiler = Compiler.getElementCompiler(element, env);
         if (compiler instanceof ToplevelCompiler) {
             if (compiler instanceof LibraryCompiler || compiler instanceof ImportCompiler ) {
                 Element library = LibraryCompiler.resolveLibraryElement(element, env, libsVisited, false);
                 if (library != null) {
                     element = library;
                 }
             }
 
             for (Iterator iter = element.getChildren().iterator();
                  iter.hasNext(); ) {
                 collectReferences(env, (Element) iter.next(), defined, referenced,
                                   libsVisited);
             }
         } else if (compiler instanceof ClassCompiler || compiler instanceof InterfaceCompiler) {
             String name = element.getAttributeValue("name");
             if (name != null) {
                 defined.add(name);
             }
             String superclass = element.getAttributeValue("extends");
             if (superclass != null) {
                 referenced.add(superclass);
             }
             ViewCompiler.collectElementNames(element, referenced);
         } else if (compiler instanceof ViewCompiler) {
             ViewCompiler.collectElementNames(element, referenced);
         }
     }
 
     static List getLibraries(CompilationEnvironment env, Element element, Map explanations, Set autoIncluded, Set visited) {
         List libraryNames = new ArrayList();
         String librariesAttr = element.getAttributeValue("libraries");
         String base = new File(Parser.getSourcePathname(element)).getParent();
         if (librariesAttr != null) {
             for (StringTokenizer st = new StringTokenizer(librariesAttr);
                  st.hasMoreTokens();) {
                 String name = (String) st.nextToken();
                 libraryNames.add(name);
             }
         }
         // figure out which tags are referenced but not defined, and
         // look up their libraries in the autoincludes file
         {
             Set defined = new HashSet();
             Set referenced = new HashSet();
             collectReferences(env, element, defined, referenced, visited);
             // keep the keys sorted so the order is deterministic for qa
             Set additionalLibraries = new TreeSet();
             Map autoincludes = env.getSchema().sAutoincludes;
             Map canonicalAuto = new HashMap();
             try {
               for (Iterator iter = autoincludes.keySet().iterator(); iter.hasNext(); ) {
                 String key = (String) iter.next();
                 canonicalAuto.put(key, env.resolveLibrary((String)autoincludes.get(key), base).getCanonicalFile());
               }
             } catch (IOException e) {
               throw new CompilationError(element, e);
             }
             // iterate undefined references
             for (Iterator iter = referenced.iterator(); iter.hasNext(); ) {
                 String key = (String) iter.next();
                 if (autoincludes.containsKey(key)) {
                     String value = (String) autoincludes.get(key);
                     // Ensure that a library that was explicitly
                     // included that would have been auto-included is
                     // emitted where the auto-include would have been.
                     if (defined.contains(key)) {
                         File canonical = (File)canonicalAuto.get(key);
                         if (visited.contains(canonical)) {
                             // Annotate as explicit
                             if (explanations != null) {
                                 explanations.put(value, "explicit include");
                             }
                             // but include as auto
                             additionalLibraries.add(value);
                         }
                     } else {
                         if (explanations != null) {
                             explanations.put(value, "reference to <" + key + "> tag");
                         }
                         additionalLibraries.add(value);
                     }
                 }
             }
             // If not linking, consider all external libraries as
             // 'auto'
             if (autoIncluded != null) {
             try {
              String basePrefix = (new File((base != null) ? base : ".")).getCanonicalPath();
               for (Iterator i = visited.iterator(); i.hasNext(); ) {
                 File file = (File)i.next();
                 String path = file.getCanonicalPath();
                 if (! path.startsWith(basePrefix)) {
                   autoIncluded.add(file);
                 }
               }
             } catch (IOException e) {
               throw new CompilationError(element, e);
             }
             }
             libraryNames.addAll(additionalLibraries);
         }
         // Turn the library names into pathnames
         List libraries = new ArrayList();
         for (Iterator iter = libraryNames.iterator(); iter.hasNext(); ) {
             String name = (String) iter.next();
             try {
               File file = env.resolveLibrary(name, base).getCanonicalFile();
               libraries.add(file);
               if (autoIncluded != null) {
                 autoIncluded.add(file);
               }
             } catch (IOException e) {
                 throw new CompilationError(element, e);
             }
         }
         
         // add the debugger, if canvas debug=true
         if (env.getBooleanProperty(env.DEBUG_PROPERTY)) {
             if (explanations != null) {
                 explanations.put("debugger", "the canvas debug attribute is true");
             }
             String pathname = LPS.getComponentsDirectory() +
                 File.separator + "debugger" +
                 File.separator + "debugger.lzx";
             libraries.add(new File(pathname));
         }
         return libraries;
     }
     
     List getLibraries(Element element) {
         return getLibraries(mEnv, element, null, null, new HashSet());
     }
 
 
     static String getBaseLibraryName (CompilationEnvironment env) {
         // returns 5 or 6; coerce to string
         String swfversion = "" + env.getSWFVersionInt();
 
         // Load the appropriate LFC Library according to debug,
         // profile, or krank
         
         // We will now have LFC library with swf version encoded after
         // the base name like:
         // LFC6.lzl
         // LFC5-debug.lzl
         // etc.
         String ext = swfversion; 
         
         ext += env.getBooleanProperty(env.PROFILE_PROPERTY)?"-profile":"";
         ext += env.getBooleanProperty(env.DEBUG_PROPERTY)?"-debug":"";
         return "LFC" + ext + ".lzl";
     }
 
     static void handleAutoincludes(CompilationEnvironment env, Element element) {
         // import required libraries, and collect explanations as to
         // why they were required
         Canvas canvas = env.getCanvas();
 
         String baseLibraryName = getBaseLibraryName(env);
         String baseLibraryBecause = "Required for all applications";
 
         Map explanations = new HashMap();
         for (Iterator iter = getLibraries(env, element, explanations, null, new HashSet()).iterator();
              iter.hasNext(); ) {
             File file = (File) iter.next();
             Compiler.importLibrary(file, env);
         }
         
         // canvas info += <include name= explanation= [size=]/> for LFC
         Element info = new Element("include");
         info.setAttribute("name", baseLibraryName);
         info.setAttribute("explanation", baseLibraryBecause);
         try {
             info.setAttribute("size", "" + 
                               FileUtils.getSize(env.resolveLibrary(baseLibraryName, "")));
         } catch (Exception e) {
             mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="exception getting library size"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ToplevelCompiler.class.getName(),"051018-228")
                                 , e);
         }
         canvas.addInfo(info);
         
         // canvas info += <include name= explanation=/> for each library
         for (Iterator iter = explanations.entrySet().iterator();
              iter.hasNext(); ) {
             Map.Entry entry = (Map.Entry) iter.next();
             info = new Element("include");
             info.setAttribute("name", entry.getKey().toString());
             info.setAttribute("explanation", entry.getValue().toString());
             canvas.addInfo(info);
         }
     }
 
 }
