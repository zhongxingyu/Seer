 /* *****************************************************************************
  * ToplevelCompiler.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.util.*;
 import java.io.*;
 
 import org.jdom.Element;
 import org.openlaszlo.compiler.ViewCompiler.*;
 import org.openlaszlo.server.*;
 import org.openlaszlo.sc.ScriptCompiler;
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
             || LibraryCompiler.isElement(element);
     }
     
     public void compile(Element element) {
         // Check if children are valid tags to be contained 
         mEnv.checkValidChildContainment(element);
 
         for (Iterator iter = element.getChildren().iterator();
              iter.hasNext(); ) {
             Element child = (Element) iter.next();
             if (!NodeModel.isPropertyElement(child)) {
                 Compiler.compileElement(child, mEnv);
             }
         }
     }
 
   /**
    * Outputs the tag map entries for the tags defined in this
    * top-level form
    */
   public void outputTagMap(CompilationEnvironment env) {
         // Output the tag->class map.
         String tagmap = "";
         for (Iterator v = env.getTags().entrySet().iterator(); v.hasNext(); ) {
           Map.Entry entry = (Map.Entry) v.next();
           String tagName = (String) entry.getKey();
           String className = (String) entry.getValue();
           // Install in constructor map
           tagmap += ("lz[" + ScriptCompiler.quote(tagName) + "] = " + className + ";\n");
         }
         env.compileScript(tagmap);
   }
 
     /** Parses out user class definitions.
      *
      * <p>
      * Iterates the direct children of the top level of the DOM tree and
      * look for elements named "class", find the "name" and "extends"
      * attributes, and enter them in the ViewSchema.
      *
      * @param visited {canonical filenames} for libraries whose
      * schemas have been visited; used to prevent recursive
      * processing.
      * 
      */
     void updateSchema(Element element, ViewSchema schema, Set visited) {
         // Make a copy of the list because we may insert interstitials
         // into the real DOM
         Iterator iterator = new ArrayList(element.getChildren()).iterator();
         while (iterator.hasNext()) {
             Element child = (Element) iterator.next();
             if (!NodeModel.isPropertyElement(child)) {
                 Compiler.updateSchema(child, mEnv, schema, visited);
             }
         }
     }
 
     /** This also collects "attribute", "method", and HTML element
      * names, but that's okay since none of them has an autoinclude
      * entry.
      */
     static void collectReferences(CompilationEnvironment env,
                                   Element element, Set defined,
                                   Set referenced, Map libsVisited) {
         ElementCompiler compiler = Compiler.getElementCompiler(element, env);
         ViewCompiler.collectLayoutElement(element, referenced);
         if (compiler instanceof ToplevelCompiler) {
             Set libStart = null;
             Set libFound = null;
             Element library = null;
             File libFile = null;
             if (compiler instanceof LibraryCompiler || compiler instanceof ImportCompiler) {
                 libStart = new LinkedHashSet(libsVisited.keySet());
                 libFound = new LinkedHashSet(libStart);
                 library = LibraryCompiler.resolveLibraryElement(element, env, libFound);
                 if (library == element) {
                     // Not an external library
                     library = null;
                 }
                 if (library != null) {
                     element = library;
                     try {
                         libFile = new File(Parser.getSourcePathname(library)).getCanonicalFile();
                         libsVisited.put(libFile, null);
                     } catch (IOException f) {
                         assert false : "Can't happen";
                     }
                 }
             }
             for (Iterator iter = element.getChildren().iterator(); iter.hasNext(); ) {
                 collectReferences(env, (Element) iter.next(), defined, referenced,
                                   libsVisited);
             }
             if (library != null) {
                 Set includes = new LinkedHashSet(libsVisited.keySet());
                 includes.removeAll(libStart);
                 libsVisited.put(libFile, includes);
             }
         } else if (compiler instanceof ClassCompiler) {
             String name = element.getAttributeValue("name");
             if (name != null) {
                 defined.add(name);
             }
             String superclass = element.getAttributeValue("extends");
             if (superclass != null) {
                 referenced.add(superclass);
             }
             String mixinSpec = element.getAttributeValue("with");
             if (mixinSpec != null) {
               String mixins[] = mixinSpec.trim().split("\\s+,\\s+");
               for (int i = 0; i < mixins.length; i++) {
                 referenced.add(mixins[i]);
               }
             }
             ViewCompiler.collectElementNames(element, referenced);
         } else if (compiler instanceof ViewCompiler) {
             ViewCompiler.collectElementNames(element, referenced);
         }
     }
 
     static List getLibraries(CompilationEnvironment env, Element element, Map explanations, Map autoIncluded, Map visited) {
         String librariesAttr = element.getAttributeValue("libraries");
         assert librariesAttr == null : "unsupported attribute `libraries`";
         List libraryNames = new ArrayList();
         String base = new File(Parser.getSourcePathname(element)).getParent();
 
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
                         if (visited.containsKey(canonical)) {
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
               for (Iterator i = visited.keySet().iterator(); i.hasNext(); ) {
                 File file = (File)i.next();
                 String path = file.getCanonicalPath();
                 if (! path.startsWith(basePrefix)) {
                   autoIncluded.put(file, visited.get(file));
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
                 autoIncluded.put(file, visited.get(file));
               }
             } catch (IOException e) {
                 throw new CompilationError(element, e);
             }
         }
         
        // If canvas debug=true, and we're not running a remote
        // debugger, add the debugger-window component library
        if (includesDebuggerWindow(env)) {
             if (explanations != null) {
                 explanations.put("debugger", "the canvas debug attribute is true");
             }
             String pathname = LPS.getComponentsDirectory() +
                 File.separator + "debugger" +
                 File.separator + "library.lzx";
             libraries.add(new File(pathname));
         }
         return libraries;
     }
     
    /** Decide whether to include the application GUI debugger component.
     *
     * Include the debugger window if debug=true no remote-debugging
     * modes are enabled
     */
    static boolean includesDebuggerWindow(CompilationEnvironment env) {
        return (env.getBooleanProperty(env.DEBUG_PROPERTY) &&
                !env.getBooleanProperty(env.CONSOLEDEBUG_PROPERTY) &&
                !env.getBooleanProperty(env.REMOTEDEBUG_PROPERTY));
    }

     static List getLibraries(CompilationEnvironment env, Element element, Map explanations, Set autoIncluded, Set visited) {
         Map externalMap = null;
         Map visitedMap = null;
         if (autoIncluded != null) {
             externalMap = new LinkedHashMap();
         }
         if (visited != null) {
             visitedMap = new LinkedHashMap();
         }
         List libs = getLibraries(env, element, explanations, externalMap, visitedMap);
         if (autoIncluded != null) {
             autoIncluded.addAll(externalMap.keySet());
         }
         if (visited != null) {
             visited.addAll(visitedMap.keySet());
         }
         return libs;
     }
 
     List getLibraries(Element element) {
         return getLibraries(mEnv, element, null, null, new HashSet());
     }
 
 
     static String getBaseLibraryName (CompilationEnvironment env) {
       return LPS.getLFCname(env.getRuntime(),
                             env.getBooleanProperty(env.DEBUG_PROPERTY),
                             env.getBooleanProperty(env.PROFILE_PROPERTY),
                             env.getBooleanProperty(env.BACKTRACE_PROPERTY) ||
                             env.getBooleanProperty(env.SOURCE_ANNOTATIONS_PROPERTY));
     }
 
     static void handleAutoincludes(CompilationEnvironment env, Element element) {
         // import required libraries, and collect explanations as to
         // why they were required
         Canvas canvas = env.getCanvas();
 
         Map explanations = new HashMap();
         for (Iterator iter = getLibraries(env, element, explanations, null, new HashSet()).iterator();
              iter.hasNext(); ) {
             File file = (File) iter.next();
             Compiler.importLibrary(file, env);
         }
         
         Element info;
         // canvas info += <include name= explanation= [size=]/> for LFC
         if (env.isSWF()) {
           String baseLibraryName = getBaseLibraryName(env);
           String baseLibraryBecause = "Required for all applications";
           info = new Element("include");
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
         }
 
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
