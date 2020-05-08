 /* *****************************************************************************
  * LibraryCompiler.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.io.*;
 import java.util.*;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.openlaszlo.utils.ChainedException;
 import org.apache.log4j.*;
 
 /** Compiler for <code>library</code> elements.
  *
  * @author  Oliver Steele
  */
 class LibraryCompiler extends ToplevelCompiler {
     final static String HREF_ANAME = "href";
 
     /** Logger
      */
     private static Logger mLogger  = Logger.getLogger(Compiler.class);
 
 
     LibraryCompiler(CompilationEnvironment env) {
         super(env);
     }
 
     static boolean isElement(Element element) {
         return element.getName().equals("library");
     }
 
     /** Return the library element and add the library to visited.  If
      * the library has already been visited, return null instead.
      */
     static Element resolveLibraryElement(File file,
                                          CompilationEnvironment env,
                                          Set visited,
                                          boolean validate)
     {
         try {
             File key = file.getCanonicalFile();
             if (!visited.contains(key)) {
                 visited.add(key);
 
                 // If we're compiling a loadable library, add this to
                 // the list of library files which which have been
                 // included by loadable libraries, so we can warn on
                 // duplicates.
                 if (env.isImportLib()) {
 
                     // compare this library file with the set of all known libraries that
                     // have been included in loadable modules. If this has been seen before,
                     // issue warning.
                     if (env.isImportLib() && env.getLoadableImportedLibraryFiles().containsKey(key)) {
                         env.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The library file \"" + p[0] + "\" included by loadable library \"" + p[1] + "\" was also included by another loadable library \"" + p[2] + "\". " + "This may lead to unexpected behavior, especially if the library defines new classes."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 LibraryCompiler.class.getName(),"051018-77", new Object[] {file, env.getApplicationFile(), env.getLoadableImportedLibraryFiles().get(key)})
                         );
                     }
 
                     env.getLoadableImportedLibraryFiles().put(key, env.getApplicationFile());
                 }
 
                 Document doc = env.getParser().parse(file);
                 if (validate)
                     Parser.validate(doc, file.getPath(), env);
                 Element root = doc.getRootElement();
                 // Look for and add any includes from a binary library
                 String includesAttr = root.getAttributeValue("includes");
                 String base = new File(Parser.getSourcePathname(root)).getParent();
                 if (includesAttr != null) {
                     for (StringTokenizer st = new StringTokenizer(includesAttr);
                          st.hasMoreTokens();) {
                         String name = (String) st.nextToken();
                        visited.add((new File(base, name)).getCanonicalFile());
                     }
                 }
 
                 return root;
             } else {
                 return null;
             }
         } catch (IOException e) {
             throw new CompilationError(e);
         }
     }
     
     /** Return the resolved library element and add the library to visited.
      * If the library has already been visited, return null instead.
      */
     static Element resolveLibraryElement(Element element,
                                          CompilationEnvironment env,
                                          Set visited,
                                          boolean validate)
     {
         String href = element.getAttributeValue(HREF_ANAME);
         if (href == null) {
             return element;
         }
         File file = env.resolveReference(element, HREF_ANAME, true);
         return resolveLibraryElement(file, env, visited, validate);
     }
     
     public void compile(Element element) throws CompilationError
     {
         element = resolveLibraryElement(
             element, mEnv, mEnv.getImportedLibraryFiles(),
             mEnv.getBooleanProperty(mEnv.VALIDATE_PROPERTY));
         if (element != null) {
             super.compile(element);
         }
     }
 
     void updateSchema(Element element, ViewSchema schema, Set visited) {
         element = resolveLibraryElement(element, mEnv, visited, false);
         if (element != null) {
             super.updateSchema(element, schema, visited);
             // TODO [hqm 2005-02-09] can we compare any 'proxied' attribute here
             // with the parent element (canvas) to warn if it conflicts.
         }
     }
 }
