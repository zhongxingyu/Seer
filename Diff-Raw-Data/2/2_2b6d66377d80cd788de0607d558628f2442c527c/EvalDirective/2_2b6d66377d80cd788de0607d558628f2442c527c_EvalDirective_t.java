 /*
  * EvalDirective.java
  *
  * Created on May 12, 2003, 2:25 PM
  *
  * Copyright (C) 1998-2003 Semiotek Inc.  All Rights Reserved.
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
 
 import org.webmacro.*;
 import org.webmacro.engine.BuildContext;
 import org.webmacro.engine.BuildException;
 import org.webmacro.engine.Variable;
 
 import org.webmacro.directive.*;
 
 /**
  *
  * @author  kkirsch
  * Usage:
  *   #eval $macroVar
  * or
  *   #eval $macroVar using $mapVar
  *
  */
 
 public class EvalDirective extends org.webmacro.directive.Directive
 {
     
     private static final int EVAL_VAR = 1;
     private static final int EVAL_USING = 2;
     private static final int EVAL_MAP_EXPR = 3;
     private static final int MAX_RECURSION_DEPTH = 100;
     
     //private Macro _evalMacro;
     private Variable _evalTarget;
     private Object _mapExpr = null;
     
     private static final ArgDescriptor[]
     myArgs = new ArgDescriptor[]
     {
         new LValueArg(EVAL_VAR),
         new OptionalGroup(2),
         new KeywordArg(EVAL_USING, "using"),
         new RValueArg(EVAL_MAP_EXPR)
     };
     
     private static final DirectiveDescriptor
     myDescr = new DirectiveDescriptor("eval", null, myArgs, null);
     
     public static DirectiveDescriptor getDescriptor()
     {
         return myDescr;
     }
     
     /** Creates a new instance of EvalDirective */
     public EvalDirective()
     {
     }
     
     public Object build(DirectiveBuilder builder, BuildContext bc)
     throws BuildException
     {
         try
         {
             _evalTarget = (Variable)builder.getArg(EVAL_VAR, bc);
             //_evalMacro = (Macro)o;
         }
         catch (ClassCastException e)
         {
             throw new NotVariableBuildException(myDescr.name, e);
         }
         if (builder.getArg(EVAL_USING) != null)
         {
             // "using" keyword specified, get map expression
             _mapExpr = builder.getArg(EVAL_MAP_EXPR, bc);
         }
         //_result = (org.webmacro.engine.Block)builder.getArg(TEMPLET_RESULT, bc);
         return this;
     }
     
     public void write(org.webmacro.FastWriter out, org.webmacro.Context context) throws org.webmacro.PropertyException, java.io.IOException
     {
         try
         {
             String s = null;
             Context c = null;
             
             Macro macro = (Macro)_evalTarget.getValue(context);
             if (_mapExpr == null)
             {
                 // no map specified, use current context
                 s = (String)macro.evaluate(context);
             }
             else
             {
                 Object o = _mapExpr;
                 if (o instanceof Macro)
                 {
                     o = ((Macro)o).evaluate(context);
                 }
                 if (!(o instanceof java.util.Map))
                 {
                     throw new PropertyException("The supplied expression did not evaluate to a java.util.Map instance.");
                 }
                 // check for max recursion
                 int recursionDepth = 0;
                 if (context.containsKey("EvalDepth"))
                 { // check the value
                     try {
                         recursionDepth = ((Integer)context.get("EvalDepth")).intValue();
                         recursionDepth++;
                         if (recursionDepth > MAX_RECURSION_DEPTH)
                         {
                             throw new PropertyException(
                             "ERROR: A recursive call to #eval exceeded the maximum depth of " 
                             + MAX_RECURSION_DEPTH);
                         }
                     }
                     catch (Exception e){
                         // something bad happend, leave depth at default
                     }
                 }
                 java.util.Map outerVars = null;
                 if (context.containsKey("OuterVars"))
                 { // check the value
                     try {
                         outerVars = (java.util.Map)context.get("OuterVars");
                     }
                     catch (Exception e){
                         // something bad happend, use vars from calling context
                     }
                 }
                 if (outerVars == null) outerVars = context.getMap();
                 c = new Context(context.getBroker());                
                 // replace _variables map with supplied map
                 c.setMap((java.util.Map)o);
                 // put current depth into the new context
                 c.put("EvalDepth", recursionDepth);
                 // add a reference to parent context variables
                 c.put("OuterVars", outerVars);     
                 // add a reference to this macro
                 c.put("Self", macro);
                 s = (String)macro.evaluate(c);
             }
             out.write(s);
         }
         catch (Exception e)
         {
            if (e instanceof PropertyException) throw (PropertyException)e;
             throw new PropertyException("#eval: Unable to evaluate macro.", e);
         }
     }
     
     public void accept(TemplateVisitor v)
     {
         v.beginDirective(myDescr.name);
         v.visitDirectiveArg("EvalTarget", _evalTarget);
         if (_mapExpr != null){
             v.visitDirectiveArg("EvalKeyword", "using");
             v.visitDirectiveArg("EvalMap", _mapExpr);
         }        
         v.endDirective();
     }
     
 }
