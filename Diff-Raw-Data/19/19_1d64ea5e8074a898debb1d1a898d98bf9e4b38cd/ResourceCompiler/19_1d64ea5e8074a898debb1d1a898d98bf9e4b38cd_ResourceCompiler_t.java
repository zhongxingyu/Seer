 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * LZX Resource Compiler
  */
 
 package org.openlaszlo.compiler;
 
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.jdom.Element;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.*;
 import org.apache.log4j.*;
 
 /** 
  * Compiler for resource and audio elements.
  *
  * @author Oliver Steele
  */
 class ResourceCompiler extends ElementCompiler {
 
     Logger mLogger = Logger.getLogger(ResourceCompiler.class);
 
     ResourceCompiler(CompilationEnvironment env) {
         super(env);
     }
 
     /** Returns true iff this class applies to this element.
      * @param element an element
      * @return see doc
      */
     static boolean isElement(Element element) {
         String name = element.getName();
         return name.equals("resource")
             || name.equals("audio");
     }
 
     /*
      * @see <code>CompilerNode</code>
      * @param env a compilation environment
      */
     public void compile(Element element) 
         throws CompilationError
     {
         File file = null;
 
         if (!element.getChildren().isEmpty()) {
             file = mEnv.resolveParentReference(element);
         } else {
             file = mEnv.resolveReference(element);
         }
         
         if (element.getAttribute("src") != null) {
             Element info = new Element("resolve");
             info.setAttribute("src", element.getAttributeValue("src"));
             try {
                 info.setAttribute("pathname", file.getCanonicalPath());
             } catch (java.io.IOException ioe) {
                 mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't canonicalize " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResourceCompiler.class.getName(),"051018-69", new Object[] {file.toString()})
 );
             }
             if (mEnv.isCanvas()) {
               mEnv.getCanvas().addInfo(info);
             }
         }
         
         String tagName = element.getName();
         try {
             if (tagName.equals("resource") || tagName.equals("preloadresource")) {
                 String name = element.getAttributeValue("name");
                 String src = element.getAttributeValue("src");
 
                 if (name == null) {
                     throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="You must supply a value for the 'name' attribute, and it must be a valid Javascript identifier."
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResourceCompiler.class.getName(),"051018-88")
 , element);
                 }
 
                 if (!ScriptCompiler.isIdentifier(name)) {
                     mEnv.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Resource names must be valid Javascript identifiers. The resource name '" + p[0] + "' is not a valid Javascript identifier"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResourceCompiler.class.getName(),"051018-99", new Object[] {name})
 , element);
                 }
                 
                 // TODO: [1-02-2003 ows]
                 // XMLUtils.requireAttributeValue should work here,
                 // and the caller should add the source location info
                 if (name == null)
                     throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="resource name is required"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResourceCompiler.class.getName(),"051018-113")
 , element);
 
                 Set resourceNames = mEnv.getResourceNames();
                if (false) {
                  if (resourceNames.contains(name)) {
                     mEnv.warn(
                        /* (non-Javadoc)
                         * @i18n.test
                         * @org-mes="The resource name '" + p[0] + "' has already been defined"
                         */
                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                            ResourceCompiler.class.getName(),"051018-124", new Object[] {name})
                        , element);
                  }
                 }
                 resourceNames.add(name);
 
                 // N.B.: Resources are always imported into the main
                 // program for the Flash target, hence the use of
                 // getResourceGenerator below
                 if (src == null) {
                     List sources = new Vector();
                     for (Iterator iter = element.getChildren("frame", element.getNamespace()).iterator();
                          iter.hasNext(); ) {
                         Element child = (Element) iter.next();
                         mEnv.resolveReference(child);
                         
                         // throw error if 'src' attribute not found
                         XMLUtils.requireAttributeValue(child, "src");
                         File pathname = mEnv.resolveReference(child);
                         sources.add(pathname.getAbsolutePath());
                         
                         Element rinfo = new Element("resolve");
                         rinfo.setAttribute("src", child.getAttributeValue("src"));
                         rinfo.setAttribute("pathname", pathname.toString());
                         if (mEnv.isCanvas()) {
                           mEnv.getCanvas().addInfo(rinfo);
                         }
                     }
                     if (!sources.isEmpty()) {
                         if (tagName.equals("preloadresource")) {
                             mEnv.getResourceGenerator().importPreloadResource(sources, name, file);
                         } else {
                             mEnv.getResourceGenerator().importResource(sources, name, file);
                         }
                     } else {
                         throw new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="required src or children"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResourceCompiler.class.getName(),"051018-162")
 , element);
                     }
                 } else {
                     if (tagName.equals("preloadresource")) {
                         mEnv.getResourceGenerator().importPreloadResource(file.getAbsolutePath(), 
                                                            name);
                     } else {
                         mEnv.getResourceGenerator().importResource(file.getAbsolutePath(), 
                                                            name);
                     }
                 }
                 return;
             } else if (tagName.equals("audio")) {
                 String name = XMLUtils.requireAttributeValue(element, "name"); 
                 mEnv.getResourceGenerator().importResource(file.getAbsolutePath(),
                                                    name);
                 return;
             } else {
                 // Program error: this shouldn't happen
                 throw new RuntimeException(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Unknown resource tag: " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResourceCompiler.class.getName(),"051018-188", new Object[] {element.getName()})
                 );
             }
         } catch (SWFWriter.ImportResourceError e) {
             mEnv.warn(e, element);
             return;
         }
     }
 }
 
 /**
  * @copyright Copyright 2001-2007 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
