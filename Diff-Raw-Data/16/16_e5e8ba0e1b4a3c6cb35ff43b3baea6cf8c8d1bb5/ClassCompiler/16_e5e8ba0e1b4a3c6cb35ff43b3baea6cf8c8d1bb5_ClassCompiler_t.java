 /* *****************************************************************************
  * ClassNode.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.io.*;
 import java.util.*;
 import org.apache.log4j.*;
 import org.jdom.Element;
 import org.jdom.output.Format.TextMode;
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.openlaszlo.xml.internal.MissingAttributeException;
 import org.openlaszlo.xml.internal.Schema;
 import org.openlaszlo.xml.internal.Schema.Type;
 import org.openlaszlo.sc.*;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.css.CSSParser;
 
 /** Compiler for <code>class</code> class elements.
  */
 class ClassCompiler extends ViewCompiler  {
     /**
        For a declaration of a class named "foobar"
        
        <pre>&lt;class name="foobar" extends="baz" with="mixin1,mixin2"&gt;</pre>
 
        We are going to call
 
        <pre>
        LzInstantiateView(
       {
         name: 'class', 
         attrs: {
                 parent: "baz", 
                 initobj: {
                              name: "foobar",
                              attrs: { extends: "baz",
                                       with: "mixin1,mixin2"" }
                          }
                 }
         }
        </pre>
      */
     
     ClassCompiler(CompilationEnvironment env) {
         super(env);
     }
     
     /** Returns true iff this class applies to this element.
      * @param element an element
      * @return see doc
      */
     static boolean isElement(Element element) {
         return element.getName().equals("class");
     }
 
     /** Parse out an XML class definition, add the superclass and
      * attribute types to the schema.
      *
      * <p>
      * For each CLASS element, find child ATTRIBUTE tags, and add them
      * to the schema.
      *
      * Also, any EVENT tags will be added to the schema as
      * attributes of type "script".
      */
     void updateSchema(Element element, ViewSchema schema, Set visited) {
         super.updateSchema(element, schema, visited);
         Element elt = element;
         // Get meta attributes
         String classname = elt.getAttributeValue("name");
         String superName = elt.getAttributeValue("extends");
         String mixinSpec = elt.getAttributeValue("with");
         
         if (classname == null ||
             (schema.enforceValidIdentifier && !ScriptCompiler.isIdentifier(classname))) {
             CompilationError cerr = new CompilationError(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The classname attribute, \"name\" must be a valid identifier for a class definition"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ClassCompiler.class.getName(),"051018-77")
 , elt);
             throw(cerr);
         }
         
         if (superName != null && !ScriptCompiler.isIdentifier(superName)) {
             mEnv.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The value for the 'extends' attribute on a class definition must be a valid identifier"
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ClassCompiler.class.getName(),"051018-89")
 , elt);
             superName = null;
         }
         // Add this class to the schema
         schema.addElement(element, classname, mEnv);
     }
     
     public void compile(Element elt) {
       String tagName = elt.getAttributeValue("name");
       ClassModel classModel = mEnv.getSchema().getClassModel(tagName);
       // May have already been compiled by a forward reference
      // May have already been compiled by a forward reference
      if (tagName.equals("anonymous")) {
            CompilationError cerr = new CompilationError(
                "The classname 'anonymous' is reserved for system use, please choose another class name"
                , elt);
            throw(cerr);
      }
       if (! classModel.isCompiled()) {
         // NOTE: [2009-01-31 ptw] We force the class to be compiled,
         // because it is in the file we are compiling (as opposed to
         // being just analyzed for schema and inheritance, or being
         // conditionally compiled as a forward reference (which will
         // only be emitted if the class is not in an import and we are
         // linking)
         classModel.compile(mEnv, true);
       }
     }
 }
