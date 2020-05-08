 /*
 * Copyright (C) 2005. All Rights Reserved. Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of either of the following Open Source licenses: The GNU General Public
 * License, version 2, or any later version, as published by the Free Software Foundation
 * (http://www.fsf.org/copyleft/gpl.html); This software is provided "as is", with NO WARRANTY, not even the implied
 * warranties of fitness to purpose, or merchantability. You assume all risks and liabilities associated with its use.
 * See www.webmacro.org for more information on the WebMacro project. @author Mike Weerdenburg @author Marcel Huijkman
  *
  *
  */
 
 package org.webmacro.directive;
 
 import java.io.IOException;
 
 import org.webmacro.Context;
 import org.webmacro.FastWriter;
 import org.webmacro.Macro;
 import org.webmacro.PropertyException;
 import org.webmacro.TemplateVisitor;
 import org.webmacro.engine.Block;
 import org.webmacro.engine.BuildContext;
 import org.webmacro.engine.BuildException;
 import org.webmacro.engine.Expression;
 import org.webmacro.engine.UndefinedMacro;
 
 /**
  * Syntax: #while (condition) [limit (int)] { block } WhileDirective implements
  * a WebMacro directive for an while control structure. If you use it without
  * the limit option than it stops after 1000000 loops! Use a limit value < 0 to
  * create a loop without a limit, this could hang your template! Introduced at
  * release 2.0 as an experimental directive.
  * 
 * @todo Introduce unit test and mainline or drop the directive.
  * 
  * @author Mike Weerdenburg
  * @author Marcel Huijkman
  * 
  * @since   14-01-2005
  * @version 02-03-2005
  */
 class WhileDirective extends Directive
 {
     private static final int WHILE_COND = 1;
 
     private static final int WHILE_LIMIT_K = 2;
 
     private static final int WHILE_LIMIT = 3;
 
     private static final int WHILE_BLOCK = 4;
 
     /* The condition as Object. */
     private Object _obCondition;
 
     /* The condition as a Macro. */
     private Macro _macroCondition = null;
 
     /* The condition as a boolean. */
     private boolean boolExpression = false;
 
     private Block _whileBlock;
 
     private Object _limitExpr;
 
     private static final UndefinedMacro UNDEF = UndefinedMacro.getInstance();
 
     private static final ArgDescriptor[] ifArgs = new ArgDescriptor[] {
             new ConditionArg(WHILE_COND), new OptionalGroup(2),
             new KeywordArg(WHILE_LIMIT_K, "limit"), new RValueArg(WHILE_LIMIT),
 
             new BlockArg(WHILE_BLOCK), };
 
     private static final DirectiveDescriptor _desc = new DirectiveDescriptor(
             "while", null, ifArgs, null);
 
     public static DirectiveDescriptor getDescriptor ()
     {
         return _desc;
     }
 
     //
     // these values are customized for this directive during build()
     //
 
     /**
      * The name given to the directive by webmacro configuration.
      */
     protected String _directiveName;
 
     public Object build (DirectiveBuilder builder, BuildContext bc)
             throws BuildException
     {
         // name of this directive.
         _directiveName = builder.getName();
 
         // While condition.
         _obCondition = builder.getArg(WHILE_COND, bc);
 
         if (_obCondition instanceof Macro) {
             _macroCondition = (Macro) _obCondition;
         } else if (_obCondition instanceof Boolean) {
             boolExpression = Expression.isTrue(_obCondition);
         } else {
             // No valid condition!
             throw new BuildException("#" + _directiveName
                     + ": does not provide a valid condition!");
         }
 
         // While limit (number).
         _limitExpr = builder.getArg(WHILE_LIMIT, bc);
 
         // While block.
         _whileBlock = (Block) builder.getArg(WHILE_BLOCK, bc);
 
         return this;
     }
 
     public void write (FastWriter out, Context context)
             throws PropertyException, IOException
     {
         Object limit;
         int loopLimit = 1000000, loopIndex = 0;
 
         if (_limitExpr != null) {
             limit = _limitExpr;
             while (limit instanceof Macro && limit != UNDEF) {
                 limit = ((Macro) limit).evaluate(context);
             }
             if (Expression.isNumber(limit)) {
                 loopLimit = (int) Expression.numberValue(limit);
             } else {
                 String warning = "#" + _directiveName
                         + ": Cannot evaluate limit";
                 writeWarning(warning, context, out);
             }
         }
 
         // evaluate the condition against the current context
         if (_macroCondition != null)
             boolExpression = Expression.isTrue(_macroCondition
                     .evaluate(context));
 
         // while the expression is true and loopLimit <0 or loopIndex <
         // loopLimit not exeeded.
         while ((boolExpression) && ((loopLimit < 0) || (loopIndex < loopLimit))) {
             _whileBlock.write(out, context);
 
             // evaluate the condition against the current context.
             if (_macroCondition != null)
                 boolExpression = Expression.isTrue(_macroCondition
                         .evaluate(context));
             ++loopIndex;
         }
 
         if (loopIndex >= loopLimit) {
             // exeeded the limit!
             String warning = "#" + _directiveName + ": exeeded the limit of "
                     + loopLimit;
             writeWarning(warning, context, out);
         }
     }
 
     public void accept (TemplateVisitor v)
     {
         v.beginDirective(_desc.name);
         v.visitDirectiveArg("WhileCondition", _macroCondition);
         v.visitDirectiveArg("WhileBlock", _whileBlock);
         if (_limitExpr != null)
             v.visitDirectiveArg("WhileLimit", _limitExpr);
         v.endDirective();
     }
 }
 
