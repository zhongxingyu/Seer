 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * LZX Library Compiler
  */
 
 package org.openlaszlo.compiler;
 import java.io.*;
 import java.util.*;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.utils.FileUtils;
 import org.apache.log4j.*;
 
 /** Compiler for <code>library</code> elements.
  *
  * @author  Oliver Steele
  */
 class LibraryCompiler extends ToplevelCompiler {
     final static String HREF_ANAME = "href";
     final static String INCLUDES_ANAME = "includes";
 
     /** Logger
      */
     private static Logger mLogger  = Logger.getLogger(LibraryCompiler.class);
 
 
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
                                          Set visited)
     {
         try {
             File key = file.getCanonicalFile();
             if (!visited.contains(key)) {
            	if (mLogger.isDebugEnabled()) {
            		mLogger.debug("Resolving: " + key);
            	}
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
 
                 Element root = null;
 
                 if (env.parsedLibraryCache.get(file) != null) {
                   root = (Element) env.parsedLibraryCache.get(file);
                 } else {
                   String keepParsedLibraries = env.getProperty("keepParsedLibraries");
                   File serializedFile = new File(key.getParent(), "." + key.getName() + ".ser");
                   if (keepParsedLibraries != null && serializedFile.exists() && serializedFile.lastModified() > file.lastModified()) {
                       try {
                           FileInputStream fis = new FileInputStream(serializedFile);
                           ObjectInputStream in = new ObjectInputStream(fis);
                          root = (Element)in.readObject();
                           in.close();
                          keepParsedLibraries = null;
                       }
                       catch(IOException ex) {
                           if (mLogger.isDebugEnabled()) {
                               mLogger.debug("Error loading " + serializedFile + " " + ex);
                           }
                       }
                       catch(ClassNotFoundException ex) {
                          if (mLogger.isDebugEnabled()) {
                             mLogger.debug("Error loading " + serializedFile + " " + ex);
                          }
                       }
                   }
                   if (root == null) {
                     Document doc = env.getParser().parse(file, env);
                     root = doc.getRootElement();
                   }
                   env.parsedLibraryCache.put(file, root);
                   if (mLogger.isDebugEnabled()) {
                	  mLogger.debug("" + file + ": " + root + " attributes: " + root.getAttributes());
                   }
                   // Look for and add any includes from a binary library
                   String includesAttr = root.getAttributeValue(INCLUDES_ANAME);
                   if (includesAttr != null) {
                      File base = new File(Parser.getSourcePathname(root)).getParentFile();
                      // This modularity sucks
                      Set binaryIncludes = env.getFileResolver().getBinaryIncludes();
                      for (StringTokenizer st = new StringTokenizer(includesAttr);
                        st.hasMoreTokens();) {
                        String name = FileUtils.fromURLPath((String)st.nextToken());
                        File canon = new File(base, name).getCanonicalFile();
                        if (mLogger.isDebugEnabled()) {
                          mLogger.debug("binary include: " + canon);
                        }
                        visited.add(canon);
                        binaryIncludes.add(canon);
                     }
                   }
                   if (keepParsedLibraries != null) {
                       try {
                   	      FileOutputStream fos = new FileOutputStream(serializedFile);
                           ObjectOutputStream out = new ObjectOutputStream(fos);
                           out.writeObject(root);
                           out.close();
                       }
                       catch(IOException ex) {
                           if (mLogger.isDebugEnabled()) {
                               mLogger.debug("Error saving " + serializedFile + " " + ex);
                           }
                       }
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
                                          Set visited)
     {
         String href = element.getAttributeValue(HREF_ANAME);
         if (href == null) {
             return element;
         }
         File file = env.resolveReference(element, HREF_ANAME, true);
         return resolveLibraryElement(file, env, visited);
     }
 
     public void compile(Element element) throws CompilationError
     {
         boolean toplevel = element.getParentElement() == null;
         boolean linking = (! "false".equals(mEnv.getProperty(CompilationEnvironment.LINK_PROPERTY)));
         element = resolveLibraryElement(element, mEnv, mEnv.getImportedLibraryFiles());
         // NOTE: [2009-02-23 ptw] (LPP-7750) If we are not linking, we
         // need to declare the global's in the binary library (as
         // opposed to in the application when the canvas is compiled).
         if (toplevel && (! linking)) {
           ViewSchema schema = mEnv.getSchema();
           NodeModel model = NodeModel.elementOnlyAsModel(element, schema, mEnv);
           computePropertiesAndGlobals(element, model, schema);
         }
 
         if (element != null) {
             super.compile(element);
         }
 
         // NOTE [2009-02-18 ptw] (LPP-7750) If we are not linking, we
         // need to dump the tag map into the binary library (as
         // opposed to dumping it when the canvas is compiled).
         if (toplevel && (! linking)) {
           outputTagMap(mEnv);
         }
     }
 
     void updateSchema(Element element, ViewSchema schema, Set visited) {
         element = resolveLibraryElement(element, mEnv, visited);
         if (element != null) {
             super.updateSchema(element, schema, visited);
             // TODO [hqm 2005-02-09] can we compare any 'proxied' attribute here
             // with the parent element (canvas) to warn if it conflicts.
         }
     }
 }
 
 /**
  * @copyright Copyright 2001-2007, 2009 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
