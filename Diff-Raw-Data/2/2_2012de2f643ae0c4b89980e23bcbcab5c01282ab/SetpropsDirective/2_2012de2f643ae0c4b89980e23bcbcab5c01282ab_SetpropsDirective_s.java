 /*
  * Copyright (C) 2005 Semiotek Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)
  *
  * This software is provided "as is", with NO WARRANTY, not even the
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.
  */
 
 package org.webmacro.directive;
 
 import java.io.IOException;
 
 import org.webmacro.Context;
 import org.webmacro.FastWriter;
 import org.webmacro.Macro;
 import org.webmacro.PropertyException;
 import org.webmacro.TemplateVisitor;
 import org.webmacro.WebMacroException;
 import org.webmacro.engine.BuildContext;
 import org.webmacro.engine.BuildException;
 import org.webmacro.engine.StringTemplate;
 import org.webmacro.engine.Variable;
 import org.webmacro.servlet.TextTool;
 import org.webmacro.util.Instantiator;
 
 /**
  * Set properties on an object using Java properties file
  * type syntax.
  * 
  * @author Keats Kirsch 
  */
 public class SetpropsDirective extends Directive
 {
 
     private static final String DEFAULT_CLASS_NAME = "java.util.Hashtable";
 
     private static final int PROPS_TARGET = 1;
 
     private static final int PROPS_CLASS = 2;
 
     private static final int PROPS_CLASSNAME = 3;
 
     private static final int PROPS_RESULT = 4;
 
     private Variable target;
 
     private Object result;
 
     private String _className;
 
     private static final ArgDescriptor[] myArgs = new ArgDescriptor[] {
             new LValueArg(PROPS_TARGET), new OptionalGroup(3),
             new KeywordArg(PROPS_CLASS, "class"), new AssignmentArg(),
             new QuotedStringArg(PROPS_CLASSNAME), new BlockArg(PROPS_RESULT) };
 
     private static final DirectiveDescriptor myDescr = new DirectiveDescriptor(
             "setprops", null, myArgs, null);
 
     public static DirectiveDescriptor getDescriptor ()
     {
         return myDescr;
     }
 
     public SetpropsDirective()
     {
     }
 
     public Object build (DirectiveBuilder builder, BuildContext bc)
             throws BuildException
     {
         try {
             target = (Variable) builder.getArg(PROPS_TARGET, bc);
         } catch (ClassCastException e) {
             throw new NotVariableBuildException(myDescr.name, e);
         }
         _className = (String) builder.getArg(PROPS_CLASSNAME, bc);
         if (_className == null)
             _className = DEFAULT_CLASS_NAME;
         result = builder.getArg(PROPS_RESULT, bc);
         return this;
     }
 
     public void write (FastWriter out, Context context)
             throws PropertyException, IOException
     {
 
         try {
             if (!context.containsKey(target.getName())) {
                 // target doesn't exist. Must create.
                 // TODO check for class loading restrictions as per bean directive
                 try {
                     Class c = Instantiator.getInstance(context.getBroker())
                             .classForName(_className);
                     Object o = c.newInstance();
                     target.setValue(context, o);
                 } catch (RuntimeException re) {
                     throw new PropertyException("Failed to create instance of "
                             + _className + " for the #properties directive. "
                             + re, re);
                 }
 
             }
             String res = (String) ((Macro) result).evaluate(context);
             String[] lines = TextTool.getLines(res);
             String s;
             String prevLine = "";
             String prefix = "#set $" + target.getVariableName() + ".";
             for (int i = 0; i < lines.length; i++) {
                 s = prevLine + lines[i].trim();
                 if (s.endsWith("\\")) {
                     // ends with continuation character. Add to next line.
                     prevLine = s.substring(0, s.length() - 1);
                 } else {
                     prevLine = "";
                     setProp(context, s, prefix);
                 }
             }
         } catch (PropertyException e) {
             throw e;
         } catch (Exception e) {
            String errorText = "#setprops: Unable to set " + target;
             writeWarning(errorText, context, out);
         }
     }
 
     private void setProp (Context context, String s, String prefix)
             throws PropertyException
     {
         String prop;
         String val;
         StringTemplate stringTemplate;
 
         if (s.length() > 0 && !s.startsWith("#")) {
             for (int j = 0; j < s.length(); j++) {
                 char ch = s.charAt(j);
                 if (ch == ':' || ch == '=') {
                     prop = s.substring(0, j).trim();
                     val = s.substring(j + 1).trim();
                     // convert to WM syntax and evaluate
                     // if (val.length() > 0)
                     // {
                     // try first as a string
                     s = prefix + prop + "=\"" + val + "\"";
                     try {
                         stringTemplate = new StringTemplate(context.getBroker(), s);
                         stringTemplate.evaluateAsString(context);
                     } catch (WebMacroException wme) {
                         // try again without quotes.
                         s = prefix + prop + "=" + val;
                         try {
                             stringTemplate = new StringTemplate(context.getBroker(), s);
                             stringTemplate.evaluateAsString(context);
                         } catch (WebMacroException wme2) {
                             PropertyException pex = new PropertyException(
                                     "Failed to set property \"" + prop
                                             + "\" to value \"" + val
                                             + "\" on variable \""
                                             + target.getVariableName()
                                             + "\" of type "
                                             + target.getClass().getName(), wme2);
                             context.getEvaluationExceptionHandler().evaluate(
                                     this.target, context, pex);
                         }
                     }
                     break;
                 }
             }
         }
 
     }
 
     public void accept (TemplateVisitor v)
     {
         v.beginDirective(myDescr.name);
         v.visitDirectiveArg("PropertiesClassKeyword", "class");
         v.visitDirectiveArg("PropertiesClassName", _className);
         v.visitDirectiveArg("PropertiesTarget", target);
         v.visitDirectiveArg("PropertiesValue", result);
         v.endDirective();
     }
 
 }
