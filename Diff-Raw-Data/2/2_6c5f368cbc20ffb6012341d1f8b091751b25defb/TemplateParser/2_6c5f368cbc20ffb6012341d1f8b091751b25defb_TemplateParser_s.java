 /* -*- java -*-
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.compiler;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.EOFException;
 import java.io.Reader;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 
 import java.util.List;
 import java.util.Stack;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.StringTokenizer;
 
 import java.lang.reflect.Method;
 import org.wings.SComponent;
 
 /**
  * parses a template for a PlafCG. A Template has the
  * form
  *   <template name="ButtonCG" for="org.wings.SButton">
  *   // common part, _within_ the class, outside the <write> function.
  *   <write>
  *     // write method. 'device' and 'component' are available.
  *   </write>
  *   </template>
  * <%@ import, include %> are supported.
  *
  * This is a simple parser, that does not yet acknowledge that the tags
  * may occur in string-constants, so don't confuse this parser 
  * by using them ;-)
  *
  * This class has just two buffers, containing the resulting java code:
  * a buffer for the common area and a buffer for the write method.
  *
  * @author <a href="mailto:H.Zeller@acm.org">Henner Zeller</a>
  */
 public class TemplateParser {
     private final static String INDENT       = "    ";
     private final static String VAR_PREFIX   = "__";
     private final static int    VAR_LEN      = 16;
     private final static Class[] NO_PARAMS = new Class[0];
 
     /*
      * All tags, that are relevant for the parsing process. These tags
      * in the input stream lead to state transitions. Of course, not
      * all tags are valid at any given time, but we are always
      * considering all of them in any state to detect errors.
      * If we encounter a tag, that is not expected,
      * it is reported as an error. This is necessary, since the JSP like
      * syntax is not very readable and it is likely, that the user will make
      * mistakes .. thus we need to have much errorchecking, and be nice
      * to the user.
      *
      * The tags must be ordered according to their length.
      */
     String stateTransitionTags[] = new String[] { "<%",                // 0 Start J.
                                                   "%>",                // 1 End java
                                                   "<write>",           // 2
                                                   "</write>",          // 3
                                                   "<import>",          // 4
                                                   "<include",          // 5
                                                   "</import>",         // 6
                                                   "<template",         // 7
                                                   "<property",         // 8
                                                   "<install>",         // 9
                                                   "</install>",        // 10
                                                   "</template>",       // 11
                                                   "</property>",       // 12
                                                   "<uninstall>",       // 13
                                                   "</uninstall>",      // 14
                                                   "<comp-property",    // 15
                                                   "</comp-property>"}; // 16
 
     // the index for the tags. Yes: c-preprocessor and enumerations would be
     // better.
     private final static int START_JAVA      = 0;
     private final static int END_JAVA        = 1;
     private final static int START_WRITE     = 2;
     private final static int END_WRITE       = 3;
     private final static int START_IMPORT    = 4;
     private final static int INCLUDE         = 5;
     private final static int END_IMPORT      = 6;
     private final static int TEMPLATE        = 7;
     private final static int START_PROP      = 8;
     private final static int START_INSTALL   = 9;
     private final static int END_INSTALL     = 10;
     private final static int END_TEMPLATE    = 11;
     private final static int END_PROP        = 12;
     private final static int START_UNINSTALL = 13;
     private final static int END_UNINSTALL   = 14;
     private final static int START_C_PROP    = 15;
     private final static int END_C_PROP      = 16;
     
     // current mode we are in - this is important for the brace depth check.
     private final static int JAVA_MODE     = 1;
     private final static int TEMPLATE_MODE = 2;
 
     private final String templateName;
     private final String pkg;
     private final String forClassName;
     private final String extendsClassName;
     private final Class  componentClass;
     private final Method  componentMethods[];
     private final JavaBuffer writeJavaCode;
     private final JavaBuffer commonJavaCode;
     private final SortedSet compProperties;
     private final SortedSet cgProperties;
     private final List importList;
     private final List installList;
     private final List uninstallList;
     private final File sourcefile;
     private final File cwd;
     private final StringPool stringPool;
 
     private PlafReader in;
     private boolean anyError;
     private int parseMode;
     private boolean writeBorder;
 
     /*
      * simple java validation. Counts open/closed braces.
      */
     private final Stack openBraces;
     private FilePosition closingBraceInTemplate;
     private FilePosition openingBraceInTemplate;
 
     public TemplateParser(String name, 
                           File cwd, File sourcefile,
                           String pkg, String forClass,
                           String extendsClass) 
         throws ClassNotFoundException {
 	this.templateName  = name;
 	this.pkg           = pkg;
 	this.forClassName  = forClass;
         this.extendsClassName=extendsClass;
         this.componentClass  = Class.forName(forClassName);
         this.componentMethods = componentClass.getMethods();
         this.sourcefile    = sourcefile;
         this.cwd           = cwd;
         this.openBraces    = new Stack();
         this.anyError      = false;
         this.cgProperties  = new TreeSet();
         this.compProperties= new TreeSet();
         this.importList    = new ArrayList();
         this.installList   = new ArrayList();
         this.uninstallList = new ArrayList();
         this.writeJavaCode = new JavaBuffer(2, INDENT);
         this.commonJavaCode= new JavaBuffer(1, INDENT);
         this.stringPool    = new StringPool( VAR_PREFIX, VAR_LEN );
         /*
          * should we write the border automatically ?. Currently, we
          * always generate code to write the border, unless we extend 
          * some class.
          */
         this.writeBorder  = (extendsClass == null);
     }
 
     /**
      * generates the Java-class that implements this CG. This method can
      * only be called after calling {@link #parse(PlafReader)}
      * @return true, if successful.
      */
     public void generate(File directory, List outProps) throws Exception {
         if (anyError)
             return;
         if (! directory.exists()) {
             directory.mkdir();
         }
         else {
             if (!directory.isDirectory())
                 throw new IllegalArgumentException(directory + " is not a directory");
         }
         File outFile = new File(directory, templateName + ".java");
         PrintWriter out = new PrintWriter(new FileWriter(outFile));
         outProps.add("#------------------- " + templateName);        
         out.println ("// DO NOT EDIT! Your changes will be lost: generated from '" + sourcefile.getCanonicalPath() + "'");
 
         if (pkg != null) {
             out.println ("package " + pkg + ";\n\n");
             outProps.add(templateName + "=" + pkg + "." + templateName);
         }
         else {
             out.println("// default package\n");
             outProps.add(templateName + "=" + templateName);
         }
 
         out.println ("import java.io.IOException;\n");
         out.println ("import org.wings.*;");
         out.println ("import org.wings.border.*;");
         out.println ("import org.wings.style.*;");
         out.println ("import org.wings.util.*;");
         out.println ("import org.wings.event.*;");
         out.println ("import org.wings.io.Device;");
 
         if (cgProperties.size() > 0 || compProperties.size() > 0 ||
             installList.size() > 0 || uninstallList.size() > 0) {
             out.println ("import org.wings.plaf.CGManager;");
             out.println ("import org.wings.session.SessionManager;");
         }
 
         Iterator imports = importList.iterator();
         if (imports.hasNext()) {
             out.println("\n// user provided imports");
         }
         while (imports.hasNext()) {
             out.println("import " + imports.next() + ";");
         }
 
         out.println();
         out.print ("public class " + templateName);
         // extend other CGs ?
         if (extendsClassName != null) {
             out.print(" extends " + extendsClassName);
         }
 
         out.print (" implements SConstants");
         /*
          * find out the name of the interface to be implemented.
          */
         Class cgInterface = null;
         for (int i=0; i < componentMethods.length; i++) {
             if ("setCG".equals(componentMethods[i].getName()) &&
                 !(org.wings.plaf.ComponentCG.class
                   .equals(componentMethods[i].getParameterTypes()[0]))) 
                 {
                     cgInterface = componentMethods[i].getParameterTypes()[0];
                     break;
                 }
         }
         if (cgInterface != null) {
             out.print(", " + cgInterface.getName());
         }
         else {
             out.print(", ComponentCG");
         }
 
         out.println(" {");
         
         // collected HTML snippets
         out.println ("\n//--- byte array converted template snippets.");
         Iterator n = stringPool.getNames();
         while (n.hasNext()) {
             String name = (String) n.next();
             out.print (INDENT + "private final static byte[] ");
             out.print (name);
             int fillNumber = VAR_LEN - name.length();
             for (int i=0; i < fillNumber; ++i) 
                 out.print(' ');
             out.print ("= ");
             out.print (stringPool.getQuotedString(name));
             out.println (".getBytes();");
         }
         
         if (cgProperties.size() > 0) {
             out.println("\n//--- properties of this plaf.");
             Iterator props = cgProperties.iterator();
             while (props.hasNext()) {
                 Property p = (Property) props.next();
                 out.print(INDENT + "private " + p.getTypeName());
                 out.print(" " + p.getName());
                 out.print(";\n");
             }
             out.println();
             
             /*
              * constructor ...
              */
             out.println(INDENT + "/**");
             out.println(INDENT + " * Initialize properties from config");
             out.println(INDENT + " */");
 
             // get settings of this CG from properties file in constructor.
             out.println(INDENT + "public " + templateName + "() {");
             out.println(INDENT + INDENT 
                         + "final CGManager manager = SessionManager.getSession().getCGManager();\n");
             props = cgProperties.iterator();
             while (props.hasNext()) {
                 Property p = (Property) props.next();
                 // see LookAndFeel::ResourceFactory
                 String globalPropName = (templateName + "." + p.getName()
                                          +(p.shouldCache() ? "" : ".nocache"));
                 out.print(INDENT + INDENT);
                 out.print("set" + capitalize(p.getName()));
                 out.print("((" + p.getTypeName() + ") ");
                 out.print("manager.getObject(\"" + globalPropName);
                 out.print("\", ");
                 out.println(p.getType().getName() + ".class));");
 
                 // properties come from a file with this value.
                 String pValue = p.getValue();
                 if (pValue == null || pValue.trim().length() == 0) {
                     outProps.add("#" + globalPropName + "=");
                 }
                 else {
                     outProps.add(globalPropName + "=" + pValue);
                 }
             }
             out.println(INDENT + "}\n"); // end constructor.
         }
         
         out.println();
         /*
          * installCG()
          */
         out.print(INDENT);
         out.println("public void installCG(final SComponent comp) {");
 
         if (compProperties.size() > 0 || installList.size() > 0) {
             out.println(INDENT + INDENT + "final " + forClassName 
                         + " component = (" + forClassName + ") comp;");
             out.println(INDENT + INDENT + "final CGManager manager = component.getSession().getCGManager();");
         }
         if (compProperties.size() > 0) {
             String shortClassName = forClassName.substring(forClassName.lastIndexOf(".") + 1);
             out.println(INDENT + INDENT + "Object value;");
             out.println(INDENT + INDENT + "Object previous;");
             outProps.add("# component properties set by "
                          + templateName);
             printConfigurationSetters(out, compProperties, shortClassName+".",
                                       outProps);
         }
         if (installList.size() > 0) {
             printCodeBlocks(out, installList);
         }
         out.println(INDENT + "}");
 
         /*
          * un-installCG()
          */
         out.print(INDENT);
        out.println("public void uninstallCG(final SComponent component) {");
         // nothing for now. We could reset this to old values ..
         if (uninstallList.size() > 0) {
             out.println(INDENT + INDENT + "final " + forClassName 
                         + " component = (" + forClassName + ") comp;");
             out.println(INDENT + INDENT + "final CGManager manager = component.getSession().getCGManager();");
             printCodeBlocks(out, uninstallList);
         }
         out.println(INDENT + "}");
         
         // common stuff.
         if (commonJavaCode.length() > 0) {
             out.println ("\n//--- code from common area in template.");
             out.print( commonJavaCode.toString() );
             out.println ("\n//--- end code from common area in template.");
         }
         
         // write function header.
         out.print ("\n\n" + INDENT + "public void write("
                    + "final org.wings.io.Device device,\n" + INDENT
                    + "                  final org.wings.SComponent _c)\n"
                    + INDENT + INDENT + "throws java.io.IOException {\n");
 
         out.println(INDENT + INDENT + "if ( !_c.isVisible() ) return;");
 
         out.println(INDENT + INDENT + "_c.fireRenderEvent(SComponent.START_RENDERING);");
 
         out.println(INDENT + INDENT
                     + "final " + forClassName + " component = ("
                     + forClassName + ") _c;");
 
         if (writeBorder) {
             out.println(INDENT + INDENT
                         + "final SBorder _border = component.getBorder();");
             out.println(INDENT + INDENT
                         + "if (_border != null) { _border.writePrefix(device); }");
         }
         
         out.println ("\n//--- code from write-template.");
         // collected write stuff.
         out.print ( writeJavaCode.toString());
         out.println ("\n//--- end code from write-template.");
 
         if (writeBorder) {
             out.println(INDENT + INDENT
                         + "if (_border != null) { _border.writePostfix(device); }");
         }
 
         out.println(INDENT + INDENT + "_c.fireRenderEvent(SComponent.DONE_RENDERING);");
 
         out.println ("\n" + INDENT + "}");
 
         if (cgProperties.size() > 0) {
             out.println("\n//--- setters and getters for the properties.");
             Iterator props = cgProperties.iterator();
             while (props.hasNext()) {
                 Property p = (Property) props.next();
                 out.print(INDENT + "public " + p.getTypeName() 
                           + " get" + capitalize(p.getName()));
                 out.print("() { return " + p.getName() + "; }\n");
                 out.print(INDENT 
                           + "public void set" + capitalize(p.getName()));
                 out.print("(" + p.getTypeName() + " " + p.getName() + ") { ");
                 out.print("this." + p.getName() + " = " + p.getName() + "; }\n\n");
             }
         }
 
         out.println ("}");
         out.close();
         outProps.add(""); // separator.
     }
 
     public void printConfigurationSetters(PrintWriter out,
                                           SortedSet props, String prefix,
                                           List outProps) 
         throws IllegalArgumentException, NoSuchMethodException {
         String previousPropertyName = null;
         Class  previousType = null;
         Iterator it = props.iterator();
         while (it.hasNext()) {
             Property p = (Property) it.next();
             StringBuffer setObject = new StringBuffer();
             Class setterClass = null;
             String resolveGetterPath = p.getName();
             /*
              * we either configure an object we just created, or configure
              * something that can be reached through a path of getters.
              */
             if (previousPropertyName != null
                 && p.getName().startsWith(previousPropertyName)) {
                 out.println(INDENT + INDENT + "previous = value;");
                 setObject.append("((")
                     .append(previousType.getName())
                     .append(") previous)");
                 setterClass = previousType;
                 resolveGetterPath = resolveGetterPath.substring(previousPropertyName.length()+1);
             }
             else {
                 setObject.append("component");
                 setterClass = componentClass;
             }
 
             StringTokenizer st = new StringTokenizer(resolveGetterPath, ".");
             int pathElements = st.countTokens();
             while (pathElements > 1) {
                 String subProperty = (String) st.nextElement();
                 String getterName = "get" + capitalize(subProperty);
                 Method getter = setterClass.getMethod(getterName, NO_PARAMS);
                 if (getter == null) {
                     throw new IllegalArgumentException("<property>: there is no getter '" + getterName + "'; needed to set Property " + p.getName());
                 }
                 setObject.append(".").append(getterName).append("()");
                 setterClass = getter.getReturnType();
                 if (setterClass.isPrimitive()) {
                     throw new IllegalArgumentException("<property>: there is only a getter '" + setterClass + " " + getterName + "()'; cannot invoke a setter on a primitive.");
                 }
                 --pathElements;
             }
             Method setter = null;
             String setterName = "set" 
                 + capitalize((String) st.nextElement());
             Method setClassMethods[] = setterClass.getMethods();
             for (int i=0; i < setClassMethods.length; ++i) {
                 Method m = setClassMethods[i];
                 if (m.getName().equals(setterName) && 
                     m.getParameterTypes().length == 1 &&
                     m.getParameterTypes()[0].isAssignableFrom(p.getType())) {
                     setter = m;
                     break;
                 }
             }
             if (setter == null) {
                 throw new IllegalArgumentException("<property>: there is no setter '" + setterName + "(" + p.getType().getName() + ")' in class " + setterClass.getName());
             }
             // see LookAndFeel::ResourceFactory
             String globalPropName = (prefix + p.getName()
                                      + (p.shouldCache() ? "" : ".nocache"));
             out.print(INDENT + INDENT + "value = manager.getObject(\"");
             out.print(globalPropName + "\", ");
             out.println(p.getTypeName() + ".class);");
             out.println(INDENT + INDENT + "if (value != null) {");
             out.print(INDENT + INDENT + INDENT);
             out.println(setObject.toString()
                         + "." + setter.getName() 
                         + "((" + p.getTypeName() + ") value);");
             out.println(INDENT + INDENT + "}");
             // append defaults to configuration file.
             String pValue = p.getValue();
             if (pValue == null || pValue.trim().length() == 0) {
                 outProps.add("#" + globalPropName + "=");
             }
             else {
                 outProps.add(globalPropName + "=" + pValue);
             }
             previousPropertyName = p.getName();
             previousType = p.getType();
         }
     }
     
     public void printCodeBlocks(PrintWriter out, List blocks) {
         Iterator it = blocks.iterator();
         while (it.hasNext()) {
             String code = (String)it.next();
             out.print(code);
         }
     }
 
     public void reportError(FilePosition pos, String msg) {
         System.err.println(pos.toString(cwd) + ": " + msg);
         anyError = true;
     }
 
     /**
      * like report error, but throws an Exception.
      */
     public void seriousError(String msg) throws ParseException {
         anyError = true;
         throw new ParseException(in.getFileStackTrace() + ": " + msg);
     }
 
     public void reportError(String msg) {
         System.err.println(in.getFileStackTrace() + ": " + msg);
         anyError = true;
     }
 
     public void reportWarning(String msg) {
         System.err.println(in.getFileStackTrace() + ": " + msg);
     }
 
     /**
      * parses this template until &lt;/template&gt; is reached.
      *
      * @param PlafReader the Reader the source is read from.
      * @exception ParseException on unrecoverable problem in input.
      */
     public synchronized void parse(PlafReader reader) 
         throws IOException, ParseException {
         in = reader;
         parseCommon();
     }
     
     /**
      * common-area: 
      *  (java-code (html-code java-code)* '<write>' write-area)* '</template>'
      */
     private void parseCommon() throws IOException, ParseException {
         for (;;) {
             switch (parseJavaCode(commonJavaCode)) {
             case END_JAVA:
                 writeJavaCode.removeTailNewline();
                 if ((parseHTMLTemplate(commonJavaCode)) == END_WRITE) {
                     seriousError("</write> not expected in common code");
                 }
                 break;
             case START_WRITE:
                 checkBracesClosed();
                 parseWrite();  // --> write-area
                 break;
             case START_PROP:
                 parseProperty(END_PROP);
                 break;
             case START_C_PROP:
                 parseProperty(END_C_PROP);
                 break;
             case START_IMPORT:
                 handleImport();
                 break;
             case START_INSTALL:
                 handleInstall();
                 break;
             case START_UNINSTALL:
                 handleUninstall();
                 break;
              case END_TEMPLATE:
                 return;
             }
         }
     }
 
     private void handleImport() throws IOException, ParseException {
         StringBuffer content = new StringBuffer();
         if ((findTransitions(content, stateTransitionTags)) != END_IMPORT) {
             seriousError("unexpected tag in <import> area");
         }
         importList.add(content.toString());
     }
 
     private void handleInstall() throws IOException, ParseException {
         StringBuffer content = new StringBuffer();
         if ((findTransitions(content, stateTransitionTags)) != END_INSTALL) {
             seriousError("unexpected tag in <install> area");
         }
         installList.add(content.toString());
     }
 
     private void handleUninstall() throws IOException, ParseException {
         StringBuffer content = new StringBuffer();
         if ((findTransitions(content, stateTransitionTags)) != END_UNINSTALL) {
             seriousError("unexpected tag in <uninstall> area");
         }
         uninstallList.add(content.toString());
     }
 
     private void parseProperty(int endTag) throws IOException, ParseException {
         StringBuffer propertyTag = new StringBuffer();
         consumeTextUntil(propertyTag, ">");
         in.read(); // consume last character: '>'
         AttributeParser ap = new AttributeParser(propertyTag.toString());
         String type = ap.getAttribute("type");
         String name = ap.getAttribute("name");
         Property p = null;
         if (type == null || name == null)
             reportError("<property>: 'type' and 'name' attribute expected");
         else {
             try {
                 p = new Property(type, name);        
             }
             catch (ClassNotFoundException e) {
                 seriousError("<property>: " + e.getMessage());
             }
         }
         String cache= ap.getAttribute("cache");
         p.setCached(cache == null 
                     || "1".equals(cache)
                     || cache.toLowerCase().startsWith("t"));
         StringBuffer defaultVal = new StringBuffer();
         if ((findTransitions(defaultVal, stateTransitionTags)) != endTag) {
             seriousError("unexpected tag in <property> area");
         }
         if (p != null) {
             p.setValue(defaultVal.toString());
             if (endTag == END_PROP)
                 cgProperties.add(p);
             else
                 compProperties.add(p);
         }
     }
 
     /**
      * write-area: html-code (java-code html-code)* '</write>'
      */
     private void parseWrite() throws IOException, ParseException {
         skipWhitespace();
         for (;;) {
             if ((parseHTMLTemplate(writeJavaCode)) == END_WRITE) {
                 checkBracesClosed();
                 return;
             }
             if (parseJavaCode(writeJavaCode) != END_JAVA) {
                 seriousError("unexpected tag in <write> area.");
             }
             writeJavaCode.removeTailNewline();
         }
     }
 
     /**
      * this method is to be called at the end of an java block. It will
      * report a warning, if the braces are unbalanced.
      */
     private void checkBracesClosed() {
         if (!openBraces.empty()) {
             reportError("missing closed '{'.");
             reportError((FilePosition) openBraces.pop(),
                         ".. that has been opened here");
             if (closingBraceInTemplate != null) {
                 reportError(closingBraceInTemplate,
                             " .. maybe this is the missing brace ? (it is in HTML code)");
                 closingBraceInTemplate = null;
             }
             openBraces.clear();
         }
     }
 
     /*
      * parses java code, until any of the end-java tags occur:
      * '%>' - END_JAVA, '<write>' - START_WRITE, '</template>' - END_TEMPLATE
      */
     private int parseJavaCode(JavaBuffer destination) 
         throws IOException, ParseException {
         parseMode = JAVA_MODE; // for the brace counter of the tokenizer.
         skipWhitespace();
         StringBuffer tempBuffer = new StringBuffer();
         for (;;) {
             tempBuffer.setLength(0);
             int trans = findTransitions(tempBuffer, stateTransitionTags);
             execJava(tempBuffer, destination);
             switch (trans) {
             case START_JAVA:               // <%  ERRORnous start java
                 seriousError("opening scriptlet while in scriptlet");
             case END_WRITE:                // </write> ERROR
                 seriousError("encountered </write> in java code; missing '%>'?");
             case INCLUDE:                  // <include
                 handleIncludeTag();
                 break;
             default:                       // '%>', <write>, <install>,
                 return trans;              // </install>,</template>
             }
         }
     }
 
     /*
      * reads HTML template, until any of the end-html tags occur.
      * '<%' - START_JAVA, '</write>' - END_WRITE
      */
     private int parseHTMLTemplate(JavaBuffer destination) 
         throws IOException, ParseException {
         parseMode = TEMPLATE_MODE; // for the brace counter of the tokenizer.
         StringBuffer tempBuffer = new StringBuffer();
         for (;;) {
             // read in HTML template and generate write() calls in java code.
             tempBuffer.setLength(0);
             int trans = findTransitions(tempBuffer, stateTransitionTags);
             generateTemplateWriteCall(tempBuffer, destination);
             
             switch (trans) {
             case END_JAVA:                  // '%>' ERRORnous end-java
                 seriousError("closing java tag in HTML code. Missing '<%' somewhere?");
             case INCLUDE:                   // <include
                 handleIncludeTag();
                 break;
             default:                        // '<%', </write>
                 return trans;
             }
         }
     }
 
     /**
      * read <include file=...> tag.
      */
     private void handleIncludeTag() 
         throws IOException {
         StringBuffer includeTag = new StringBuffer();
         consumeTextUntil(includeTag, ">");
         in.read(); // consume last character: '>'
         openIncludeFile(includeTag);
     }
 
     /**
      * open include file.
      */
     private void openIncludeFile(StringBuffer includeTag)
         throws IOException {
         AttributeParser p = new AttributeParser(includeTag.toString());
         String filename = p.getAttribute("file");
         if (filename != null && filename.length() > 0)
             in.open(filename);
         else
             reportError("cannot include file without name; 'file=\"..\"' attribute not set ?");
     }
 
     /**
      * generate java code that write()s the given template code.
      */
     private void generateTemplateWriteCall(StringBuffer template, 
                                             JavaBuffer javaBuffer) {
         if (template.length() == 0) return;
         javaBuffer.append ("\ndevice.write(")
             .append(stringPool.getNameFor(template.toString()))
             .append(");\n");
         template.setLength(0);
     }
     
 
     /**
      * Creates java source from the scriptlets within the JSP-tags. 
      * By default, it just outputs the given string as java, but there are 
      * some special modifiers like '!', '?', '@' that generates code 'around'.
      */
     private void execJava(StringBuffer input, JavaBuffer output) 
         throws IOException {
         if (input.length() == 0)
             return;
         char qualifier = input.charAt(0);
         switch (qualifier) {
         case '@':
             // the code describes an include tag (we are ignoring other
             // types like 'import' for now)
             openIncludeFile(input);
             break;
             
         case '=':
             input.deleteCharAt(0);
             output.append("\torg.wings.plaf.compiler.Utils.write( device, ")
                 .append(input)
                 .append(");\n");
             break;
 
         case '#':
             input.deleteCharAt(0);
             output.append("\torg.wings.plaf.compiler.Utils.writeRaw( device, ")
                 .append(input)
                 .append(");\n");
             break;
             
         case '|': {
             input.deleteCharAt(0);
             int pos;
             for (pos=0; pos < input.length(); ++pos) {
                 if ((input.charAt(pos)) == '=')
                     break;
             }
             if (pos >= input.length()) {
                 reportError("'=' expected in attribute expression");
                 return;
             }
             String attributeName = input.substring(0, pos).trim();
             String expression    = input.substring(pos+1);
             output.append("\torg.wings.plaf.compiler.Utils.optAttribute( device, ")
                 .append("\"").append(attributeName).append("\", ")
                 .append(expression)
                 .append(");\n");
             break;
         }
 
         case '?': 
             // FIXME: make introspection to find out the name of the getter.
             input.deleteCharAt(0);
             output.append("\torg.wings.plaf.compiler.Utils.write( device, ")
                 .append("component.get")
                 .append(capitalize(input.toString()))
                 .append("());\n");
             break;
 
         default:
             output.append(input);
         }
         input.setLength(0);
     }
     
     /**
      * reads from the reader until any of the given strings, given in
      * the options array, occurs in the input stream. Store the text, that
      * has been read up to that position in the StringBuffer 'buffer' and
      * return the index in the options array of the transition-tag found.
      */
     protected int findTransitions(StringBuffer buffer, String[] options)
         throws IOException {
         char startChars[] = new char [ options.length ];
         for (int i = 0; i < options.length; ++i)
             startChars[i] = options[i].charAt(0);
         String startSet = new String(startChars);
         int result = -1;
         do {
             consumeTextUntil(buffer, startSet);
             result = checkTransitions(options);
             if (result == -1) // false alert; append matched char.
                 buffer.append((char) in.read());
         }
         while (result < 0);
         return result;
     }
 
     /**
      * captialize a string. A String 'foo' becomes 'Foo'. Used to 
      * derive names of getters from the property name.
      */
     private  String capitalize(String s) {
 	s = s.trim();
 	return s.substring(0,1).toUpperCase() + s.substring(1);
     }
     
     /**
      * checks all possible transitions, starting from a reader
      * placed just at a possible match.
      * If any of the given options matches, the reader is placed after 
      * that token. This method assumes, that the options given are given 
      * sorted of their length.
      * @return the index in options reporting the tag found or -1 if
      *         nothing has been found.
      * @param in the reader to read from
      * @param options an array of all expected options, sorted by
      *                length, smallest first. Options must not
      *                start with the same prefix.
      */
     private int checkTransitions(String[] options) 
         throws IOException {
         int pos = 0;
         int maxLength = options[options.length-1].length();
         char cbuff[] = new char[ maxLength ];
         in.mark( maxLength );
         for (int i = 0; i < options.length; ++i) {
             String currentOption = options[i];
             int readLen = currentOption.length() - pos;
             int read;
             while ((read = in.read(cbuff, pos, readLen)) > 0) {
                 pos += read;
                 readLen -= read;
             }
             if (match(currentOption, cbuff))
                 return i;
         }
         in.reset();
         return -1; // no matches.
     }
     
     /*
      * returns true, if the string matches the character buffer.
      */
     private boolean match(String str, char cbuff[]) {
         int len = str.length();
         int i;
         for (i = 0; i < len; ++i) {
             if (str.charAt(i) != cbuff[i])
                 return false;
         }
         return (i == len);
     }
 
     private void skipWhitespace()
 	throws IOException {
 	int c;
 	do {
 	    in.mark(1);
 	    c = in.read();
 	} 
 	while (c >= 0 && Character.isWhitespace((char) c));
 	in.reset();
     }
 
     /**
      * consumes the text from the reader, until any of the given
      * characters in 'stopChars' occurs. Append any text found up to
      * this position in the 'consumed' StringBuffer.
      * Place reader _before_ the found character.
      */
     private StringBuffer consumeTextUntil(StringBuffer consumed,
                                           String stopChars)
 	throws IOException {
 	int c;
         in.mark(1);
 	while ((c = in.read()) != -1) {
             if (stopChars.indexOf((char) c) != -1) {
                 in.reset();
                 return consumed;
             }
 
 	    /*
 	     * ignore backslash + newline
 	     */
 	    else if (c == '\\') {
 		c = in.read();
 		if (c != '\n') {
 		    consumed.append('\\');
 		    if (c != -1)
 			consumed.append((char)c);
 		}
 	    }
 	    else
 		consumed.append((char)c);
             
             /*
              * do basic java validation. Count brace depth.
              */
             if (parseMode == JAVA_MODE) {
                 if (c == '{') {
                     openBraces.push(in.getFilePosition());
                 } 
                 else if (c == '}') {
                     if (openBraces.empty()) {
                         reportError("closing '}' that has not been opened.");
                         if (openingBraceInTemplate != null) {
                             reportError(openingBraceInTemplate,
                                         ".. maybe this is the missing brace ? (it is in HTML code)");
                             openingBraceInTemplate = null;
                         }
                     }
                     else
                         openBraces.pop();
                 }
             }
             /*
              * ok, we are in template mode. If here are braces, this is
              * probably errronous (braces in HTML code are rare).
              * So save this location, just in case we find a missing brace:
              * we can then report this position as a hint for the user.
              */
             else {
                 if (c == '{') {
                     openingBraceInTemplate = in.getFilePosition();
                 } else if (c == '}') {
                     closingBraceInTemplate = in.getFilePosition();
                 }
             }
             in.mark(1);
 	}
 	return consumed;
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
