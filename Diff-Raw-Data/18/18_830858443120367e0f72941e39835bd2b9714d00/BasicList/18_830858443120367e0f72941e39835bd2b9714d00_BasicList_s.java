 package net.cscott.sdr.calls.lists;
 
 import static java.util.Arrays.asList;
 import static net.cscott.sdr.calls.parser.CallFileLexer.PART;
 import static net.cscott.sdr.util.Tools.foreach;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.DanceState;
 import net.cscott.sdr.calls.Dancer;
 import net.cscott.sdr.calls.DancerPath;
 import net.cscott.sdr.calls.Evaluator;
 import net.cscott.sdr.calls.ExactRotation;
 import net.cscott.sdr.calls.Formation;
 import net.cscott.sdr.calls.FormationList;
 import net.cscott.sdr.calls.FormationMatch;
 import net.cscott.sdr.calls.MatcherList;
 import net.cscott.sdr.calls.Position;
 import net.cscott.sdr.calls.Program;
 import net.cscott.sdr.calls.Selector;
 import net.cscott.sdr.calls.StandardDancer;
 import static net.cscott.sdr.calls.StandardDancer.*;
 import net.cscott.sdr.calls.TaggedFormation;
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.ast.Comp;
 import net.cscott.sdr.calls.ast.Expr;
 import net.cscott.sdr.calls.ast.In;
 import net.cscott.sdr.calls.ast.Part;
 import static net.cscott.sdr.calls.ast.Part.Divisibility.*;
 import net.cscott.sdr.calls.ast.Seq;
 import net.cscott.sdr.calls.ast.SeqCall;
 import net.cscott.sdr.calls.grm.Grm;
 import net.cscott.sdr.calls.grm.Rule;
 import net.cscott.sdr.calls.lists.C1List.ConcentricEvaluator;
 import net.cscott.sdr.calls.lists.C1List.ConcentricType;
 import net.cscott.sdr.calls.transform.Fractional;
 import net.cscott.sdr.util.Fraction;
 import net.cscott.sdr.util.Point;
 import net.cscott.sdr.util.Tools.F;
 
 import org.junit.runner.RunWith;
 
 /** 
  * The <code>BasicList</code> class contains complex call
  * and concept definitions which are on the 'basic' program.
  * Note that "simple" calls and concepts are defined in
  * the resource file at
  * <a href="doc-files/basic.calls"><code>net/cscott/sdr/calls/lists/basic.calls</code></a>;
  * this class contains only those definitions for which an
  * executable component is required.
  * @author C. Scott Ananian
  * @version $Id: BasicList.java,v 1.20 2009-02-05 06:13:31 cananian Exp $
  */
 @RunWith(value=JDoctestRunner.class)
 public abstract class BasicList {
     // hide constructor.
     private BasicList() { }
 
     private static abstract class BasicCall extends Call {
         private final String name;
         BasicCall(String name) { this.name = name; }
         @Override
         public final String getName() { return name; }
         @Override
         public final Program getProgram() { return Program.BASIC; }
         @Override
         public Rule getRule() { return null; }
         @Override
         public List<Expr> getDefaultArguments() {
             return Collections.emptyList();
         }
     }
     
     /** Simple combining concept. */
     public static final Call AND = new BasicCall("and") {
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args) {
             assert args.size()>=1;
             List<SeqCall> l = foreach(args, new F<Expr,SeqCall>(){
                 @Override
                 public Apply map(Expr e) { return new Apply(e); }
             });
             return new Evaluator.Standard(new Seq(l));
         }
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             // this introduces ambiguities into the grammar; ban 'and'
             // as a simple connector.
 	    if (true) return null;
             Grm g = Grm.parse("<0=anything> and <1=anything>");
             return new Rule("anything", g, Fraction.valueOf(-30));
         }
     };
 
     /** Time readjustment. */
     public static final Call IN = new BasicCall("_in") {
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args)
             throws EvaluationException {
             Evaluator arg = args.get(1).evaluate(Evaluator.class, ds);
             // if this is a simple call, expand it directly
             Comp result;
             if (arg.hasSimpleExpansion())
                 result = arg.simpleExpansion();
             // for complicated calls, use a Seq(Apply(...))
             else
                 result = new Seq(new Apply(args.get(1)));
             return new Evaluator.Standard(new In(args.get(0), result));
         }
         @Override
         public int getMinNumberOfArguments() {
             return 2;
         }
     };
 
     // LEFT means 'do each part LEFT' but collisions are still resolved to
     // right hands. (opposed to MIRROR, where collisions are to left hands).
     public static final Call LEFT = new BasicCall("left") {
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             Grm g = Grm.parse("left <0=leftable_anything>");
             return new Rule("anything", g, Fraction.TWO); // bind tight
         }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args) {
             assert args.size()==1;
             return new LRMEvaluator(LRMType.LEFT, args.get(0));
         }
     };
 
     public static final Call REVERSE = new BasicCall("reverse") {
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             Grm g = Grm.parse("reverse <0=reversable_anything>");
             return new Rule("anything", g, Fraction.TWO); // bind tight
         }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args) {
             assert args.size()==1;
             return new LRMEvaluator(LRMType.REVERSE, args.get(0));
         }
     };
 
     /** Enumeration: left, reverse, or mirror. */
     public static enum LRMType { LEFT, MIRROR, REVERSE; }
     /** Evaluator for left, reverse, and mirror. */
     public static class LRMEvaluator extends Evaluator {
         private final LRMType which;
         private final Expr comp;
         public LRMEvaluator(LRMType which, Expr arg) {
             this.which = which;
             this.comp = arg;
         }
         @Override
         public Evaluator evaluate(DanceState ds) {
             boolean mirrorShoulderPass = (which == LRMType.MIRROR);
 	    // XXX NEED A LEFT MEANS MIRROR FLAG, which is does, generally;
 	    //     otherwise 'left pass thru' passes right shoulders (!)
 	    mirrorShoulderPass = true;
             // Mirror the current formation.
 	    // (any existing collisions should resolve 'mirror')
             Formation nf = ds.currentFormation().mirror(true);
             DanceState nds = ds.cloneAndClear(nf);
             // do the call in the mirrored formation
             new Apply(this.comp).evaluator(nds).evaluateAll(nds);
             // now re-mirror the resulting paths.
             for (Dancer d : nds.dancers()) {
                 for (DancerPath dp : nds.movements(d)) {
                     ds.add(d, dp.mirror(mirrorShoulderPass));
                 }
             }
             // no more to evaluate
             return null;
         }
     };
 
     /**
      * The "with designated" concept saves the designated dancers (in the
      * {@link DanceState}) so that they can be referred to later in the call.
      * This is used for '&lt;anyone&gt; hop' and even for the humble
      * '&lt;anyone&gt; run'.
      * Takes at least two arguments; all except the last are tag names; dancers
      * who match any of these tags are saved as the 'designated' ones. (Note
      * that you can add 'DESIGNATED' as one of the tags in order to grow the
      * designated tag set after performing another match; not sure if that
      * would ever be necessary.)
      * @doc.test
      *  Show how this concept is used for 'designees run':
      *  js> importPackage(net.cscott.sdr.calls.ast)
      *  js> a1 = Expr.literal("_designees run")
      *  '_designees run
      *  js> a = new Apply(new Expr("_with designated", Expr.literal("boy"), a1))
      *  (Apply (Expr _with designated 'boy '_designees run))
      */
     public static final Call _WITH_DESIGNATED = new BasicCall("_with designated") {
         @Override
         public int getMinNumberOfArguments() { return 2; }
 
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args)
             throws EvaluationException {
             // all but the last argument are names of tags
             assert args.size()==2;
             final Selector selector =
                 args.get(0).evaluate(Selector.class, ds);
 
             // fetch the subcall, and make an evaluator which will eventually
             // pop the designated dancers to clean up.
             final Evaluator subEval =
                 args.get(1).evaluate(Evaluator.class, ds);
             final Evaluator popEval = new Evaluator() {
                 private Evaluator next = subEval;
                 @Override
                 public Evaluator evaluate(DanceState ds) {
                     this.next = next.evaluate(ds);
                     if (this.next == null) {
                         // we're finally done with the subcall!
                         ds.popDesignated();
                         return null;
                     }
                     return this;
                 }
             };
 
             // return an evaluator which matches the tags against the current
             // formation, mutates the dance state, and then delegates to the
             // popEval to do the actual evaluation and eventual cleanup
             return new Evaluator() {
                 @Override
                 public Evaluator evaluate(DanceState ds) {
                     // get the current tagged formation, match, push
                     Formation f = ds.currentFormation();
                     TaggedFormation tf = TaggedFormation.coerce(f);
                     Set<Dancer> matched = selector.select(tf);
                     ds.pushDesignated(matched);
                     // delegate, eventually clean up
                     return popEval.evaluate(ds);
                 }
             };
         }
     };
 
     /** Like the "concentric" concept, but no adjustment for ends.  What's
      *  usually meant by "centers X while the ends do Y". */
     public static final Call QUASI_CONCENTRIC = new BasicCall("_quasi concentric") {
         @Override
         public int getMinNumberOfArguments() { return 2; }
         @Override
         public Rule getRule() { return null; /* internal call */ }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args) {
             assert args.size() == 2;
             return new ConcentricEvaluator(args.get(0), args.get(1),
                                            ConcentricType.QUASI);
         }
     };
 
     // complex concept -- not sure correct program here?
     // XXX: support further subdivision of DOSADO 1 1/2 by allowing an
     //      integer argument to Part which specifies how many parts
     //      it should be considered as?
     /**
      * The "fractional" concept.
      * @doc.test
      *  Evaluate TWICE DOSADO and the DOSADO 1 1/2.  Note that we prohibit
      *  further subdivision of the DOSADO 1 1/2.
      *  js> importPackage(net.cscott.sdr.calls)
      *  js> importPackage(net.cscott.sdr.calls.ast)
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> a1 = Expr.literal("dosado")
      *  'dosado
      *  js> a = new Expr("_fractional", Expr.literal("2"), a1)
      *  (Expr _fractional '2 'dosado)
      *  js> BasicList._FRACTIONAL.getEvaluator(ds, a.args).simpleExpansion()
      *  (Seq (Apply 'dosado) (Apply 'dosado))
      *  js> a = new Expr("_fractional", Expr.literal("1 1/2"), a1)
      *  (Expr _fractional '1 1/2 'dosado)
      *  js> BasicList._FRACTIONAL.getEvaluator(ds, a.args).simpleExpansion()
      *  (Seq (Part 'INDETERMINATE '1 (Seq (Apply 'dosado) (Part 'DIVISIBLE '1 (In (Expr _multiply num '1/2 '6) (Opt (From 'ANY (Seq (Part 'INDIVISIBLE '1 (Seq (Apply '_mixed touch))) (Part 'DIVISIBLE '3 (In (Expr _multiply num '1/3 '6) (Opt (From 'RH MINIWAVE (Seq (Prim 1, 1, none, 1, SASHAY_FINISH))))))))))))))
      */
     public static final Call _FRACTIONAL = new BasicCall("_fractional") {
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args)
             throws EvaluationException {
             Fractional fv = new Fractional(ds); // visitor singleton
             boolean isDivisible = true;
             assert args.size()==2;
             Fraction n = args.get(0).evaluate(Fraction.class, ds);
             Apply a = new Apply(args.get(1));
             if (n.compareTo(Fraction.ZERO) <= 0)
                 throw new BadCallException("Non-positive fractions are not allowed");
             int whole = n.floor();
             List<SeqCall> l = new ArrayList<SeqCall>(whole+1);
             // easy case: do the whole repetitions of the
             // call.
             for (int i=0; i<whole; i++)
                 l.add(a);
             // now add the fraction, if there is one.
             // note this does not get wrapped in a PART:
             // we can't further fractionalize (say)
             // swing thru 1 1/2.
 	    n=n.subtract(Fraction.valueOf(whole));
             if (!Fraction.ZERO.equals(n)) {
                 l.add(fv.visit(a, n));
                 if (whole!=0)
                     isDivisible=false;
             }
             Comp result = new Seq(l);
             // OPTIMIZATION: SEQ(PART(c)) = c
             if (l.size()==1 && l.get(0).type==PART) {
                 Part p = (Part) l.get(0);
                 if (p.divisibility==DIVISIBLE)
                     result = p.child;
             } else if (!isDivisible)
                 // we don't support subdivision of "swing thru 2 1/2"
                 result = new Seq(new Part(INDETERMINATE, Fraction.ONE, result));
             return new Evaluator.Standard(result);
         }
         @Override
         public int getMinNumberOfArguments() { return 2; }
         @Override
         public Rule getRule() {
             String rule = "do <0=fraction> (of (a|an)?)? <1=anything>" +
                           "| <1=anything> <0=times>";
             Grm g = Grm.parse(rule);
             return new Rule("anything", g, Fraction.valueOf(-10));
         }
     };
     /** Grammar tweak: allow "do half of a ..." in addition to the
      * longer-winded "do one half of a..." or "do a half of a...". */
     public static final Call _HALF = new BasicCall("_half") {
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args)
             throws EvaluationException {
             assert args.size()==1;
             return _FRACTIONAL.getEvaluator
                 (ds, asList(Expr.literal(Fraction.ONE_HALF), args.get(0)));
         }
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             String rule = "do half of (a|an)? <0=anything>";
             Grm g = Grm.parse(rule);
             return new Rule("anything", g, Fraction.valueOf(-10));
         }
     };
 
     /**
      * Adjust circle to its other orientation.  Used for 'circle choreography'
      * like allemande left, swing thru from an alamo ring, etc.
      *
      * @doc.test Basic application from a squared set/circle.
      *  js> importPackage(net.cscott.sdr.calls);
      *  js> importPackage(net.cscott.sdr.calls.ast);
      *  js> ds = new DanceState(new DanceProgram(Program.BASIC), Formation.SQUARED_SET); undefined;
      *  js> ds.currentFormation().toStringDiagram("|");
      *  |     3Gv  3Bv
      *  |
      *  |4B>            2G<
      *  |
      *  |4G>            2B<
      *  |
      *  |     1B^  1G^
      *  js> comp = AstNode.valueOf("(Seq (Apply '_circle adjust))");
      *  (Seq (Apply '_circle adjust))
      *  js> new Evaluator.Standard(comp).evaluateAll(ds);
      *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
      *  |     3GQ       3BL
      *  |
      *  |4BQ                 2GL
      *  |
      *  |
      *  |
      *  |4G7                 2B`
      *  |
      *  |     1B7       1G`
      *  js> new Evaluator.Standard(comp).evaluateAll(ds);
      *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
      *  |     3Gv  3Bv
      *  |
      *  |4B>            2G<
      *  |
      *  |4G>            2B<
      *  |
      *  |     1B^  1G^
      * @doc.test Works from non-squared O spots as well
      *  js> importPackage(net.cscott.sdr.calls);
      *  js> importPackage(net.cscott.sdr.calls.ast);
      *  js> ds = new DanceState(new DanceProgram(Program.BASIC), Formation.SQUARED_SET); undefined;
      *  js> comp = AstNode.valueOf("(Seq (Apply 'face out) (Apply '_circle adjust))");
      *  (Seq (Apply 'face out) (Apply '_circle adjust))
      *  js> new Evaluator.Standard(comp).evaluateAll(ds);
      *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
      *  |     3GL       3BQ
      *  |
      *  |4B7                 2G`
      *  |
      *  |
      *  |
      *  |4GQ                 2BL
      *  |
      *  |     1B`       1G7
      */
     public static final Call _CIRCLE_ADJUST = new BasicCall("_circle adjust") {
         @Override
         public int getMinNumberOfArguments() { return 0; }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args)
             throws EvaluationException {
             return new Evaluator() {
                 @Override
                 public Evaluator evaluate(DanceState ds) {
                     Formation f = ds.currentFormation();
                     /*
                      *    0 1
                      *  2     3
                      *  4     5
                      *    6 7
                      */
                     // moving one spot CW
                     int[] rOrder = new int[] { 1, 3, 0, 5, 2, 7, 4, 6 };
                     // some positions have to turn a corner
                     boolean[] rCorner= new boolean[]{ false, true, true, false,
                                                       false, true, true, false};
                     // match the O spots to get normalized formation
                     FormationMatch fm = MatcherList.O_SPOTS.match(f);
                     assert fm.meta.dancers().size() == 1;
                     Dancer metaD = fm.meta.dancers().iterator().next();
                     TaggedFormation from = fm.matches.get(metaD);
                     List<Dancer> sortedDancers = from.sortedDancers();
                     assert sortedDancers.size() == rOrder.length;
                     // the meta dancer is going to get an extra 1/8 rotation
                     Position mP = fm.meta.location(metaD)
                                          .turn(Fraction.valueOf(-1,8), false);
                     ExactRotation mR = (ExactRotation) mP.facing;
                     // now map every dancer to their new rotated formation
                     for (int i=0 ; i < sortedDancers.size() ; i++) {
                         Dancer d = sortedDancers.get(i);
                         Position fP = from.location(d);
                         // base position rotates one spot CW
                         Position tP = from.location
                                             (sortedDancers.get(rOrder[i]));
                         tP = fP.relocate(tP.x, tP.y, fP.facing);
                         // some dancers need to have facing dir turn the corner
                         if (rCorner[i])
                             tP = tP.relocate
                               (tP.x, tP.y, tP.facing.add(Fraction.ONE_QUARTER));
                         // rotate whole formation 1/8
                         tP = tP.rotateAroundOrigin(mR);
                         // make a dancer path
                         DancerPath dp = new DancerPath
                             (f.location(d), tP, Fraction.ONE, null);
                         ds.add(d, dp);
                     }
                     return null;
                 }
             };
         }
     };
 
     /** Used for 'scramble home' call. */
     public static final Call _SCRAMBLE_HOME = new BasicCall("_scramble home") {
         @Override
         public int getMinNumberOfArguments() { return 0; }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args)
             throws EvaluationException {
             return new Evaluator() {
                 @Override
                 public Evaluator evaluate(DanceState ds) {
                     Formation currentFormation = ds.currentFormation();
                     for (Dancer d : currentFormation.dancers()) {
                         if (!(d instanceof StandardDancer)) continue;
                         StandardDancer sd = (StandardDancer) d;
                         Position current = currentFormation.location(sd);
                         Position target = targetFormation.location(sd);
                         Fraction time = Fraction.ZERO;
                         DancerPath dp;
 
                         // roll if needed to face w/in 1/4 of proper direction.
                         ExactRotation curR = (ExactRotation) current.facing;
                         ExactRotation tarR = (ExactRotation) target.facing;
                         Fraction sweep = curR.minSweep(tarR);
                         if (sweep.abs().compareTo(Fraction.ONE_QUARTER) > 0) {
                             dp = rollToTarget(current, target);
                             ds.add(sd, dp);
                             current = dp.to;
                             curR = (ExactRotation) current.facing;
                             time = time.add(dp.time);
                         }
 
                         // odd special case: sometimes the 1/4-off rotations
                         // leaves the dancer initially walking *backwards*
                         // turn 1/4 more if necessary to prevent that.
                         dp = new DancerPath(current, target, time, null);
                         Point initialTangent = dp.tangentStart();
                        ExactRotation initialDir = ExactRotation.fromXY
                            (initialTangent.x, initialTangent.y);
                        sweep = curR.minSweep(initialDir);
                        if (sweep.abs().compareTo(Fraction.ONE_QUARTER) > 0) {
                            dp = rollToTarget(current, target);
                            ds.add(sd, dp);
                            current = dp.to;
                            time = time.add(dp.time);
                         }
 
                         // now make a DancerPath for the rest of the distance
                         // travel speed is 1 unit/beat, so estimate appropriate
                         // path time based on distance to travel
                         Fraction dist2 = target.x.subtract(current.x).pow(2);
                         dist2 = dist2.add(target.y.subtract(current.y).pow(2));
                         double dist = Math.sqrt(dist2.doubleValue());
                         if (false)
                             time = Fraction.valueOf(Math.ceil(dist));
                         else {
                             // XXX using real times screws up breathing.  Use fixed
                             //     time for now.
                             time = Fraction.THREE.subtract(time);
                         }
                         dp = new DancerPath(current, target, time, null);
                         ds.add(sd, dp);
                     }
                     return null;
                 }
                 private DancerPath rollToTarget(Position current, Position target) {
                     ExactRotation curR = (ExactRotation) current.facing;
                     ExactRotation tarR = (ExactRotation) target.facing;
                     Fraction sweep = curR.minSweep(tarR);
                     // can't rotate more than 1/4 in each step.
                     if (sweep.compareTo(Fraction.ONE_QUARTER) > 0)
                         sweep = Fraction.ONE_QUARTER;
                     if (sweep.compareTo(Fraction.mONE_QUARTER) < 0)
                         sweep = Fraction.mONE_QUARTER;
                     Position newPos = current.turn(sweep, false);
                     return new DancerPath(current, newPos, Fraction.ONE, null);
                 }
             };
         }
         final Formation targetFormation =
             FormationList.STATIC_SQUARE_FACING_OUT.mapStd
             (COUPLE_3_BOY, COUPLE_3_GIRL, COUPLE_4_GIRL, COUPLE_2_BOY);
     };
 }
