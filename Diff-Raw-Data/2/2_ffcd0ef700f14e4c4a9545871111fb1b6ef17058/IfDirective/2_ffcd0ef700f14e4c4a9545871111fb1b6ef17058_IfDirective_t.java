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
 
 import org.webmacro.*;
 import org.webmacro.engine.Block;
 import org.webmacro.engine.BuildContext;
 import org.webmacro.engine.BuildException;
 import org.webmacro.engine.Expression;
 
 /**
  * Syntax:
  * #if (condition) { block }
  * [ #elseif (condition) { block } ] *
  * [ #else { block } ]
  *
  * IfDirective implements a WebMacro directive for an if..elseif..else
  * control structure.  This directive is more complicated than most others
  * because it has repeating optional subdirectives, and because it tries
  * to do as much constant folding in the build() method as possible.
  * Therefore, the build() method is complicated, but the write() method
  * is fairly simple.
  */
 
 class IfDirective extends Directive {
 
    private static final int IF_COND = 1;
    private static final int IF_BLOCK = 2;
    private static final int IF_ELSEIF = 3;
    private static final int IF_ELSE = 4;
    private static final int ELSEIF_COND = 5;
    private static final int ELSEIF_BLOCK = 6;
    private static final int ELSE_BLOCK = 7;
 
    private int nConditions;
    private Macro[] conditions;
    private Block[] blocks;
    private Block elseBlock;
 
    private static final ArgDescriptor[]
          elseifArgs = new ArgDescriptor[]{
             new ConditionArg(ELSEIF_COND),
             new BlockArg(ELSEIF_BLOCK)
          },
    elseArgs = new ArgDescriptor[]{
       new BlockArg(ELSE_BLOCK)
    },
    ifArgs = new ArgDescriptor[]{
       new ConditionArg(IF_COND),
       new BlockArg(IF_BLOCK)
    };
    private static final Subdirective[]
          ifSubdirectives = new Subdirective[]{
             new OptionalRepeatingSubdirective(IF_ELSEIF, "elseif", elseifArgs,
                                               Subdirective.BREAKING),
             new OptionalSubdirective(IF_ELSE, "else", elseArgs,
                                      Subdirective.BREAKING)
          };
 
    private static final DirectiveDescriptor
          myDescr = new DirectiveDescriptor("if", null, ifArgs, ifSubdirectives);
 
 
    public static DirectiveDescriptor getDescriptor() {
       return myDescr;
    }
 
    public Object build(DirectiveBuilder builder,
                        BuildContext bc)
          throws BuildException {
       Object c = builder.getArg(IF_COND, bc);
       boolean cMacro = (c instanceof Macro);
       int elseifCount;
       DirectiveArgs elseArgs;
       DirectiveArgs[] elseifArgs = null;
 
       // If condition is static and true -- just return the block (builder)
       if (!cMacro && Expression.isTrue(c))
          return builder.getArg(IF_BLOCK, bc);
 
       elseArgs = builder.getSubdirective(IF_ELSE);
       elseifArgs = builder.getRepeatingSubdirective(IF_ELSEIF);
       elseifCount = (elseifArgs == null) ? 0 : elseifArgs.length;
 
       // OK, how about no else-if subdirectives?
       if (elseifCount == 0) {
          // If condition is static and false -- just return the else block
          if (!cMacro) {
             // Must be false, since we already tested !cMacro && isTrue(c)
             return (elseArgs != null)
                   ? elseArgs.getArg(ELSE_BLOCK, bc) : "";
          }
          else {
             // Just one condition -- the IF condition, and maybe an ELSE block
             nConditions = 1;
             conditions = new Macro[1];
             blocks = new Block[1];
             conditions[0] = (Macro) c;
             blocks[0] = (Block) builder.getArg(IF_BLOCK, bc);
             if (elseArgs != null)
                elseBlock = (Block) elseArgs.getArg(ELSE_BLOCK, bc);
             return this;
          }
       }
       else {
          // This is the ugly case -- we have to guess at how many conditions
          // we'll have.  We start with 1 + count(#elseof), and if any can be
          // folded out at compile time, we just won't use the whole thing
          int i = 0;
          nConditions = elseifCount + (cMacro? 1 : 0);
          conditions = new Macro[nConditions];
          blocks = new Block[nConditions];
          // If we're here, !cMacro -> the condition is false
          if (cMacro) {
             conditions[0] = (Macro) c;
             blocks[0] = (Block) builder.getArg(IF_BLOCK, bc);
             ++i;
          }
          for (int j = 0; j < elseifCount; j++) {
             c = elseifArgs[j].getArg(ELSEIF_COND, bc);
             if (c instanceof Macro) {
                conditions[i] = (Macro) c;
                blocks[i] = (Block) elseifArgs[j].getArg(ELSEIF_BLOCK, bc);
                ++i;
             }
             else if (Expression.isTrue(c)) {
                // If all the previous got folded out as false, then just return the
                // block from this condition, otherwise stash it in the elseBlock
                // and we're done with #elseif directives
                if (i == 0)
                   return elseifArgs[j].getArg(ELSEIF_BLOCK, bc);
                else {
                   elseBlock = (Block) elseifArgs[j].getArg(ELSEIF_BLOCK, bc);
                   break;
                }
             }
             else {
                // Just skip this #elseif directive
             }
          }
          // If we didn't promote one of the elseif blocks to else, get the else
          if (elseBlock == null && elseArgs != null) {
             // If there are no valid conditions, just return the else block
             if (i == 0)
                return elseArgs.getArg(ELSE_BLOCK, bc);
             else
                elseBlock = (Block) elseArgs.getArg(ELSE_BLOCK, bc);
          }
 
          if (i < nConditions) {
             // If we folded out some cases, we would want to resize the arrays,
             // but since the space doesn't really matter, we'll save time by
             // just remembering how big they really are.
             nConditions = i;
          }
       }
 
       return this;
    }
 
 
    public void write(FastWriter out, Context context)
          throws PropertyException, IOException {
 
       for (int i = 0; i < nConditions; i++) {
          boolean b = false;
 
          try {
             b = Expression.isTrue(conditions[i].evaluate(context));
          }
          catch (Exception e) {
            String warning = "#if: Error evaluating condition: " + e + " at " + context.getCurrentLocation();
             context.getLog("engine").warning(warning);
             writeWarning(warning, context, out);
          }
          if (b) {
             blocks[i].write(out, context);
             return;
          }
       }
 
       // If we fell out, we ran out of conditions, try the else block if any
       if (elseBlock != null)
          elseBlock.write(out, context);
    }
 
    public void accept(TemplateVisitor v) {
       v.beginDirective(myDescr.name);
       for (int i = 0; i < nConditions; i++) {
          v.visitDirectiveArg((i == 0)? "IfCondition" : "ElseIfCondition",
                              conditions[i]);
          v.visitDirectiveArg((i == 0)? "IfBlock" : "ElseIfBlock", blocks[i]);
       }
       if (elseBlock != null)
          v.visitDirectiveArg("ElseBlock", elseBlock);
       v.endDirective();
    }
 
 }
 
