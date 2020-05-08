 /* *****************************************************************************
  * FileResolver.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.*;
 import org.openlaszlo.server.*;
 import org.apache.log4j.*;
 
 import org.openlaszlo.utils.FileUtils;
 
 /**
  * Provides an interface for resolving a pathname to a File.  The
  * Compiler class uses this to resolve includes (hrefs).
  *
  * @author Oliver Steele
  */
 public interface FileResolver {
     /** An instance of the DefaultFileResolver */
     FileResolver DEFAULT_FILE_RESOLVER = new DefaultFileResolver();
 
     /** Given a pathname, return the File that it names.
      * @param pathname a path to resolve
      * @param base a relative URI against which to resolve it
      * @param asLibrary whether this URI is to a library or not
      * @return see doc
      * @exception java.io.FileNotFoundException if an error occurs
      */
     File resolve(String pathname, String base, boolean asLibrary)
         throws java.io.FileNotFoundException;
 
     File resolve(CompilationEnvironment env, String pathname, String base, boolean asLibrary)
         throws java.io.FileNotFoundException;
 
     /**
      * The Set of Files that represents the includes that have been
      * implicitly included by binary libraries.  This is updated by
      * the library compiler and used to resolve paths that may not
      * exist in the binary distribution.
      */
     Set getBinaryIncludes();
 }
 
 /** DefaultFileResolver maps each pathname onto the File that
  * it names, without doing any directory resolution or other
  * magic.  (The operating system's notion of a working directory
  * supplies the context for partial pathnames.)
  */
 class DefaultFileResolver implements FileResolver {
 
     public Set binaryIncludes = new HashSet();
 
     public Set getBinaryIncludes() { return binaryIncludes; }
 
     public DefaultFileResolver() {
     }
 
     public File resolve(String pathname, String base, boolean asLibrary)
         throws java.io.FileNotFoundException {
         return resolve(null, pathname, base, asLibrary);
     }
 
     /** @see FileResolver */
     public File resolve(CompilationEnvironment env, String pathname, String base, boolean asLibrary)
         throws java.io.FileNotFoundException {
         if (asLibrary) {
             // If it is a library, search for .lzo's, consider the
             // path may be just to the directory of the library
             File library = null;
             if (pathname.endsWith(".lzx")) {
                 library = resolveInternal(env, pathname.substring(0, pathname.length()-4) +".lzo", base);
                 if (library != null) {
                   return library;
                 }
             } else {
                 // Try pathname as a directory
                 library = resolveInternal(env, (new File(pathname, "library.lzo").getPath()), base);
                 if (library != null) {
                   return library;
                 }
                 library = resolveInternal(env, (new File(pathname, "library.lzx").getPath()), base);
                 if (library != null) {
                   return library;
                 }
             }
         }
         // Last resort for a library, normal case for plain files
         File resolved = resolveInternal(env, pathname, base);
         if (resolved != null) {
             return resolved;
         }
         throw new java.io.FileNotFoundException(pathname);
     }
 
     protected File resolveInternal(CompilationEnvironment env, String pathname, String base) {
         Logger mLogger = Logger.getLogger(FileResolver.class);
 
         final String FILE_PROTOCOL = "file";
         String protocol = FILE_PROTOCOL;
 
         // The >1 test allows file pathnames to start with DOS
         // drive letters.
         int pos = pathname.indexOf(':');
         if (pos > 1) {
             protocol = pathname.substring(0, pos);
             pathname = pathname.substring(pos + 1);
         }
         mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Resolving pathname: " + p[0] + " and base: " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 FileResolver.class.getName(),"051018-68", new Object[] {pathname, base})
 );
         if (!FILE_PROTOCOL.equals(protocol)) {
             throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="unknown protocol: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 FileResolver.class.getName(),"051018-77", new Object[] {protocol})
 );
         }
 
         // Determine whether to substitue resource files initially just swf to png
         String pnext = FileUtils.getExtension(pathname);
         boolean dhtmlResourceFlag = false;
         if (env != null) {
             dhtmlResourceFlag = env.isDHTML();
         }
         boolean SWFtoPNG = dhtmlResourceFlag && pnext.equalsIgnoreCase("swf");
         if (SWFtoPNG) {
             String pnbase = FileUtils.getBase(pathname);
             pathname = pnbase + ".png";
         }
 
         // FIXME: [ebloch] this vector should be in a properties file
         Vector v = new Vector();
         if (pathname.startsWith("/")) {
           // Try absolute
           v.add("");
          v.add(LPS.getComponentsDirectory());
         }
         v.add(base);
         if (SWFtoPNG) {
           String toAdd = FileUtils.insertSubdir(base + "/", "autoPng");
           mLogger.debug("Default File Resolver Adding " + toAdd + '\n');
           v.add(toAdd);
         }
         if (!pathname.startsWith("./") && !pathname.startsWith("../")) {
           v.add(LPS.getComponentsDirectory());
           v.add(LPS.getFontDirectory());
           v.add(LPS.getLFCDirectory());
         }
 
         Enumeration e = v.elements();
         while (e.hasMoreElements()) {
             String dir = (String)e.nextElement();
             try {
               File f = (new File(dir, pathname)).getCanonicalFile();
               if (f.exists() ||
                   ((binaryIncludes != null) && binaryIncludes.contains(f))) {
                 mLogger.debug("Resolved " + pathname + " to "  +
                               f.getAbsolutePath());
                 return f;
               } else if (SWFtoPNG) {
                 String autoPngPath = FileUtils.insertSubdir(f.getPath(), "autoPng");
                 mLogger.debug("Default File Resolver Looking for " + autoPngPath + '\n');
                 File autoPngFile = new File(autoPngPath);
                 if (autoPngFile.exists()) {
                   mLogger.debug("Default File Resolver " + pathname + " to "  +
                                 autoPngFile.getAbsolutePath() + '\n');
                   return autoPngFile;
                 } else {
                   File [] pathArray = FileUtils.matchPlusSuffix(autoPngFile);
                   if (pathArray != null && pathArray.length != 0)  {
                     return autoPngFile;
                   }
                 }
               }
             } catch (java.io.IOException ex) {
               // Not a valid file?
             }
         }
         return null;
     }
 }
 
 
