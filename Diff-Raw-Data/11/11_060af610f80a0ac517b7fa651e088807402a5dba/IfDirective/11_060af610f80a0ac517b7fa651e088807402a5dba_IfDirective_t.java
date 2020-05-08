 package org.webmacro.directive;
 
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.engine.*;
 
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
 
   private static final int IF_COND      = 1;
   private static final int IF_BLOCK     = 2;
   private static final int IF_ELSEIF    = 3;
   private static final int IF_ELSE      = 4;
   private static final int ELSEIF_COND  = 5;
   private static final int ELSEIF_BLOCK = 6;
   private static final int ELSE_BLOCK   = 7;
 
   private int      nConditions;
   private Macro[]  conditions;
   private Block[]  blocks;
   private Block    elseBlock;
 
   private static final ArgDescriptor[] 
     elseifArgs = new ArgDescriptor[] {
       new ConditionArg(ELSEIF_COND), 
       new BlockArg(ELSEIF_BLOCK)
     }, 
     elseArgs = new ArgDescriptor[] {
       new BlockArg(ELSE_BLOCK)
     }, 
     ifArgs = new ArgDescriptor[] {
       new ConditionArg(IF_COND), 
       new BlockArg(IF_BLOCK)
     };
   private static final Subdirective[] 
     ifSubdirectives = new Subdirective[] {
       new OptionalRepeatingSubdirective(IF_ELSEIF, "elseif", elseifArgs), 
       new OptionalSubdirective(IF_ELSE, "else", elseArgs)
     };
 
   private static final DirectiveDescriptor 
     ifDescr = new DirectiveDescriptor("if", IfDirective.class, ifArgs, 
                                       ifSubdirectives);
   
 
   public static DirectiveDescriptor getDescriptor() {
     return ifDescr;
   }
 
   public Object build(DirectiveBuilder builder, 
                       BuildContext bc) 
   throws BuildException {
     Object c = builder.getArg(IF_COND, bc);
     boolean cMacro = (c instanceof Macro);
     int elseifCount;
     DirectiveArgs elseArgs;
     DirectiveArgs[] elseifArgs = null;
 
     // If condition is static and true -- just return the block
    if (!cMacro && Expression.isTrue(c)) {
       return (Block) builder.getArg(IF_BLOCK, bc);
    }
 
     elseArgs = builder.getSubdirective(IF_ELSE);
     elseifArgs = builder.getRepeatingSubdirective(IF_ELSEIF);
     elseifCount = (elseifArgs == null) ? 0 : elseifArgs.length;
 
     // OK, how about no else-if subdirectives?  
     if (elseifCount == 0) {
       // If condition is static and false -- just return the else block 
       if (!cMacro) {
         return (elseArgs != null) 
           ? elseArgs.getArg(ELSE_BLOCK, bc) : "";
       }
       else {
         // Just one condition -- the IF condition, and maybe an ELSE block
        nConditions = 1;
         conditions = new Macro[1];
         blocks     = new Block[1];
         conditions[0] = (Macro) c;
         blocks[0]     = (Block) builder.getArg(IF_BLOCK, bc);
         if (elseArgs != null)
           elseBlock = (Block) elseArgs.getArg(ELSE_BLOCK, bc);
         return this;
       }
     }
     else {
       // This is the ugly case -- we have to guess at how many conditions
       // we'll have.  We start with 1 + count(#elseof), and if any can be
       // folded out at compile time, we just won't use the whole thing
       int i=0;
      nConditions=elseifCount + (cMacro? 1 : 0);
       conditions = new Macro[nConditions];
       blocks     = new Block[nConditions];
       if (cMacro) {
         conditions[0] = (Macro) c;
         blocks[0]     = (Block) builder.getArg(IF_BLOCK, bc);
         ++i;
       }
       for (int j=0; j < elseifCount; j++) {
         c = elseifArgs[j].getArg(ELSEIF_COND, bc);
         if (c instanceof Macro) {
           conditions[i] = (Macro) c;
           blocks[i] = (Block) elseifArgs[j].getArg(ELSEIF_BLOCK, bc);
           ++i;
         }
         else if (Expression.isTrue(c)) {
           elseBlock = (Block) elseifArgs[j].getArg(ELSEIF_BLOCK, bc);
           // If all the previous got folded out as false, then just return the 
           // block from this condition, otherwise stash it in the elseBlock
           // and we're done with #elseif directives
           if (i == 0)
             return elseBlock;
           else 
             break;
         }
         else {
           // Just skip this #elseif directive
         }
       }
       // If we didn't promote one of the elseif blocks to else, get the else
       if (elseBlock == null && elseArgs != null) {
           elseBlock = (Block) elseArgs.getArg(ELSE_BLOCK, bc);
         // If there are no valid conditions, just return the else block
         if (i == 0)
           return elseBlock;
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
     throws ContextException, IOException {
 
     for (int i=0; i<nConditions; i++) {
       if (Expression.isTrue(conditions[i].evaluate(context))) {
         blocks[i].write(out, context);
         return;
       }
     }
 
     // If we fell out, we ran out of conditions, try the else block if any
     if (elseBlock != null)
       elseBlock.write(out, context);
   } 
   
 }
