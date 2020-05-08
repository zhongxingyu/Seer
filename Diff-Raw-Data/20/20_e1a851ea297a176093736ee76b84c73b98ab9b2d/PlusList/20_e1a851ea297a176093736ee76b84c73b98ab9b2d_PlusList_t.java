 package net.cscott.sdr.calls.lists;
 
 import java.util.Collections;
 import java.util.List;
 
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.DanceState;
 import net.cscott.sdr.calls.Dancer;
 import net.cscott.sdr.calls.Formation;
 import net.cscott.sdr.calls.Position;
 import net.cscott.sdr.calls.Program;
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.ast.Comp;
 import net.cscott.sdr.calls.ast.Prim;
import net.cscott.sdr.calls.ast.Seq;
 import net.cscott.sdr.calls.grm.Rule;
 import net.cscott.sdr.calls.transform.EvalPrim;
 import net.cscott.sdr.calls.transform.Evaluator;
 import net.cscott.sdr.util.Fraction;
 
 /**
  * The <code>PlusList</code> class contains complex call
  * and concept definitions which are on the 'plus' program.
  * Note that "simple" calls and concepts are defined in
  * the resource file at
  * <a href="doc-files/plus.calls"><code>net/cscott/sdr/calls/lists/plus.calls</code></a>;
  * this class contains only those definitions for which an
  * executable component is required.
  * @author C. Scott Ananian
  */
 public abstract class PlusList {
     // hide constructor.
     private PlusList() { }
 
     private static abstract class PlusCall extends Call {
         private final String name;
         PlusCall(String name) { this.name = name; }
         @Override
         public final String getName() { return name; }
         @Override
         public final Program getProgram() { return Program.PLUS; }
         @Override
         public List<Apply> getDefaultArguments() {
             return Collections.emptyList();
         }
     }
 
     public static final Call ROLL = new PlusCall("_roll") {
         final Prim rightRoll = Prim.valueOf("(Prim 0, 0, right, 2)");
         final Prim leftRoll = Prim.valueOf("(Prim 0, 0, left, 2)");
         @Override
         public Comp apply(Apply ast) {
             assert false : "This call uses a custom Evaluator";
             return null;
         }
         @Override
         public int getMinNumberOfArguments() { return 0; }
         @Override
         public Rule getRule() { return null; /* internal call */ }
         @Override
         public Evaluator getEvaluator(Apply ast) {
             /* if we've got an argument this is "<anything> and roll"
              * otherwise it's just plain "roll" (ie, nothing and roll) */
             assert ast.args.size() == 0 || ast.args.size() == 1;
             Evaluator rolle = new Evaluator() {
                 @Override
                 public Evaluator evaluate(DanceState ds) {
                     // go through the dancer's last movements and add a roll
                     //  1) remove any trailing 'stand still' actions
                     ds.unsyncDancers();
                     //  2) add rolls.
                     Formation f = ds.currentFormation();
                     for (Dancer d : f.dancers()) {
                         Position from = f.location(d);
                         int rollDir = from.roll().compareTo(Fraction.ZERO);
                         if (rollDir < 0)
                             ds.add(d, EvalPrim.apply(leftRoll, from, 1));
                         else if (rollDir > 0)
                             ds.add(d, EvalPrim.apply(rightRoll, from, 1));
                     }
                     return null; /* ta-da! */
                 }
             };
             /* if there's an arg, do that first */
             if (ast.args.size() > 0) {
                final Apply arg = ast.getArg(0);
                Evaluator sub = new Evaluator() {
                    @Override
                    public Evaluator evaluate(DanceState ds) {
                        // breathe to resolve collisions before rolling.
                        return Evaluator.breathedEval
                            (ds.currentFormation(), new Seq(arg)).evaluate(ds);
                    }
                };
                 rolle =  new Evaluator.EvaluatorChain(sub, rolle);
             }
             return rolle;
         }
     };
 }
