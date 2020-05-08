 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
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
 
 import java.io.*;
 import java.util.Iterator;
 import org.webmacro.*;
 import org.webmacro.engine.*;
 import org.webmacro.util.*;
 
 public class ForeachDirective extends Directive {
 
   private static final int FOREACH_TARGET     = 1;
   private static final int FOREACH_IN_K       = 2;
   private static final int FOREACH_LIST       = 3;
   private static final int FOREACH_BODY       = 4;
   private static final int FOREACH_INDEXING_K = 5;
   private static final int FOREACH_INDEX      = 6;
   private static final int FOREACH_LIMIT_K    = 7;
   private static final int FOREACH_LIMIT      = 8;
   private static final int FOREACH_FROM_K     = 9;
   private static final int FOREACH_FROM       = 10;
 
   private Variable target, index;
   private Object   list, indexFromExpr, limitExpr;
   private Macro    body;
 
   // Syntax:
   // #foreach list-var in list-expr 
   //   [ limit n ] [ indexing $i [ from m ] ] 
   // { block } 
 
   private static final ArgDescriptor[] 
     myArgs = new ArgDescriptor[] {
       new LValueArg(FOREACH_TARGET), 
       new KeywordArg(FOREACH_IN_K, "in"),
       new RValueArg(FOREACH_LIST), 
       new OptionChoice(2),
         new OptionalGroup(3), 
           new KeywordArg(FOREACH_INDEXING_K, "indexing"),
           new LValueArg(FOREACH_INDEX),
           new OptionalGroup(2), 
             new KeywordArg(FOREACH_FROM_K, "from"),
             new RValueArg(FOREACH_FROM),
         new OptionalGroup(2),
           new KeywordArg(FOREACH_LIMIT_K, "limit"),
           new RValueArg(FOREACH_LIMIT),
       new BlockArg(FOREACH_BODY)
     };
 
   private static final DirectiveDescriptor 
     myDescr = new DirectiveDescriptor("foreach", null, myArgs, null);
   
   public static DirectiveDescriptor getDescriptor() {
     return myDescr;
   }
 
   public Object build(DirectiveBuilder builder, 
                       BuildContext bc) 
   throws BuildException {
     try {
       target = (Variable) builder.getArg(FOREACH_TARGET, bc);
       index  = (Variable) builder.getArg(FOREACH_INDEX, bc);
     }
     catch (ClassCastException e) {
       throw new NotVariableBuildException(myDescr.name, e);
     }
     list   = builder.getArg(FOREACH_LIST, bc);
     body   = (Block) builder.getArg(FOREACH_BODY, bc);
     indexFromExpr = builder.getArg(FOREACH_FROM, bc);
     limitExpr     = builder.getArg(FOREACH_LIMIT, bc);
     return this;
   }
 
   public void write(FastWriter out, Context context) 
     throws PropertyException, IOException {
 
     Object l, limit, from;
     int loopLimit=-1, loopStart=1, loopIndex=0;
 
     l = list;
     while (l instanceof Macro) 
       l = ((Macro) l).evaluate(context);
 
     if (limitExpr != null) {
       limit = limitExpr;
       while (limit instanceof Macro)
         limit = ((Macro) limit).evaluate(context);
       if (Expression.isNumber(limit)) 
         loopLimit = (int) Expression.numberValue(limit);
       else {
         String warning = "#foreach: Cannot evaluate limit";
         context.getLog("engine").warning(warning);
         writeWarning(warning, context, out);
       }
     }
 
     if (index != null && indexFromExpr != null) {
       from = indexFromExpr;
       while (from instanceof Macro)
         from = ((Macro) from).evaluate(context);
       if (Expression.isNumber(from)) 
         loopStart = (int) Expression.numberValue(from);
       else {
         String warning = "#foreach: Cannot evaluate loop start";
         context.getLog("engine").warning(warning);
         writeWarning(warning, context, out);
       }
     }
 
     Iterator iter;
     try {
       iter = context.getBroker()._propertyOperators.getIterator(l);
     } catch (Exception e) {
       String warning = "#foreach: list argument is not a list: " + l;
       context.getLog("engine").warning(warning + "; " + e);
       writeWarning(warning, context, out);
       return;
     }
     while(iter.hasNext()
           && ((loopLimit == -1) 
               || (loopLimit > loopIndex))) {
       try {
         target.setValue(context, iter.next());
         if (index != null) 
           index.setValue(context, new Integer(loopIndex + loopStart));
       }
       catch (PropertyException e) {
         String errorText = "#foreach: Unable to set list index";
         context.getBroker().getLog("engine").error(errorText);
         writeWarning(errorText, context, out);
       }
       body.write(out, context);
       ++loopIndex;
     }
   } 
 
   public void accept(TemplateVisitor v) {
     v.beginDirective(myDescr.name);
     v.visitDirectiveArg("ForeachTarget", target);
     v.visitDirectiveArg("ForeachList", list);
     if (index!=null)
       v.visitDirectiveArg("ForeachIndex", index);
     if (indexFromExpr!=null)  
       v.visitDirectiveArg("ForeachFrom", indexFromExpr);
     if (limitExpr!=null)
       v.visitDirectiveArg("ForeachLimit", limitExpr);
     v.visitDirectiveArg("ForeachBlock", body);
     v.endDirective();
   }
   
 }
