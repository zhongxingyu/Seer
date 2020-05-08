 package org.wings.macro;
 
 import org.mvel.MVEL;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * <code>AbstractMacro<code>.
  * <p/>
  * User: rrd
  * Date: 13.08.2007
  * Time: 15:04:43
  *
  * @author rrd
  * @version $Id
  */
 public abstract class AbstractMacro implements Macro, Instruction {
 
     protected Collection<Instruction> instructions = new ArrayList<Instruction>();
 
     public void addInstruction(Instruction instruction) {
         instructions.add(instruction);
     }
 
     protected static Object resolveValue(MacroContext ctx, String expr) {
         if (expr != null && expr.length() > 1) {
             if (expr.charAt(0) == '$') {
                // handle reference
                return MVEL.eval(expr.substring(1), ctx);
             }
         }
         return expr;
     }
 }
