 /* *****************************************************************************
  * FileResolver.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.io.File;
 import java.util.Vector;
 import java.util.Enumeration;
 import org.openlaszlo.server.*;
 import org.apache.log4j.*;
 
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
 }
 
 /** DefaultFileResolver maps each pathname onto the File that
  * it names, without doing any directory resolution or other
  * magic.  (The operating system's notion of a working directory
  * supplies the context for partial pathnames.) 
  */
 class DefaultFileResolver implements FileResolver {
 
     public DefaultFileResolver() {
     }
 
     /** @see FileResolver */
     public File resolve(String pathname, String base, boolean asLibrary)
         throws java.io.FileNotFoundException {
         if (asLibrary) {
             File binary = null;
             if (pathname.endsWith(".lzx")) {
                 binary = resolveInternal(pathname.substring(0, pathname.length()-4) +".lzo", base);
             }
             if (binary != null) {
                 return binary;
             }
             File library = resolveInternal(pathname, base);
            if (library != null) {
              if (! library.isDirectory()) {
                 return library;
              }
              binary = new File(library, "library.lzo");
              if (binary.exists()) {
                 return binary;
              }
              library = new File(library, "library.lzx");
              if (library.exists()) {
                 return library;
              }
             }
         } else {
             File resolved = resolveInternal(pathname, base);
             if (resolved != null) {
                 return resolved;
             }
         }
         throw new java.io.FileNotFoundException(pathname);
     }
 
     protected File resolveInternal(String pathname, String base) {
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
 
         // FIXME: [ebloch] this vector should be in a properties file
         Vector v = new Vector();
         if (pathname.startsWith("/")) {
           // Try absolute
           v.add("");
         }
         v.add(base);
         if (!pathname.startsWith("./") && !pathname.startsWith("../")) {
           v.add(LPS.getComponentsDirectory());
           v.add(LPS.getFontDirectory());
           v.add(LPS.getLFCDirectory());
         }
         
         Enumeration e = v.elements();
         while (e.hasMoreElements()) {
             String dir = (String)e.nextElement();
             File f = new File(dir, pathname);
             mLogger.debug("Trying " + f.getAbsolutePath());
             if (f.exists()) {
                 // TODO: [2002-11-23 ows] check for case mismatch
                 mLogger.debug("Resolving " + pathname + " to "  +
                               f.getAbsolutePath());
                 /*
                  * TODO: [2004-06-01 bloch] The code below results in weird compiler errors. 
                  * I think I was going to put it here so we could add the canvas info
                  * resolver elements in one place.   
                 try {
                     return f.getCanonicalFile();
                 } catch (java.io.IOException ex) {
                     throw new CompilationError("Can't get canonical file name " + f.getPath());
                 }
                 */
                 return f;
             }
         }
         return null;
     }
 }
 
 
