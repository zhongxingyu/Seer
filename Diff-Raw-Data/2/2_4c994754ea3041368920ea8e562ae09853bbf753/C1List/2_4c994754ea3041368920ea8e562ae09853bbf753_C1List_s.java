 package net.cscott.sdr.calls.lists;
 
 import static net.cscott.sdr.calls.ast.Part.Divisibility.DIVISIBLE;
 import static net.cscott.sdr.calls.parser.CallFileLexer.PART;
 import static net.cscott.sdr.util.Tools.l;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.Breather;
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.DanceState;
 import net.cscott.sdr.calls.Dancer;
 import net.cscott.sdr.calls.DancerPath;
 import net.cscott.sdr.calls.Evaluator;
 import net.cscott.sdr.calls.ExactRotation;
 import net.cscott.sdr.calls.Formation;
 import net.cscott.sdr.calls.FormationList;
 import net.cscott.sdr.calls.Position;
 import net.cscott.sdr.calls.Program;
 import net.cscott.sdr.calls.MatcherList;
 import net.cscott.sdr.calls.TaggedFormation;
 import net.cscott.sdr.calls.TimedFormation;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.ast.Comp;
 import net.cscott.sdr.calls.ast.Expr;
 import net.cscott.sdr.calls.ast.Part;
 import net.cscott.sdr.calls.ast.Seq;
 import net.cscott.sdr.calls.ast.SeqCall;
 import net.cscott.sdr.calls.grm.Grm;
 import net.cscott.sdr.calls.grm.Rule;
 import net.cscott.sdr.calls.lists.A1List.SolidEvaluator;
 import net.cscott.sdr.calls.lists.A1List.SolidMatch;
 import net.cscott.sdr.calls.lists.A1List.SolidType;
 import net.cscott.sdr.calls.transform.Finish;
 import net.cscott.sdr.util.Box;
 import net.cscott.sdr.util.Fraction;
 
 /**
  * The <code>C1List</code> class contains complex call
  * and concept definitions which are on the 'C1' program.
  * Note that "simple" calls and concepts are defined in
  * the resource file at
  * <a href="doc-files/c1.calls"><code>net/cscott/sdr/calls/lists/c1.calls</code></a>;
  * this class contains only those definitions for which an
  * executable component is required.
  * @author C. Scott Ananian
  */
 public abstract class C1List {
     // hide constructor.
     private C1List() { }
 
     private static abstract class C1Call extends Call {
         private final String name;
         C1Call(String name) { this.name = name; }
         @Override
         public final String getName() { return name; }
         @Override
         public final Program getProgram() { return Program.C1; }
         @Override
         public List<Expr> getDefaultArguments() {
             return Collections.emptyList();
         }
     }
 
     /** The "finish" concept.
      * @doc.test
      *  Evaluate FINISH SWING THRU.
      *  js> importPackage(net.cscott.sdr.calls)
      *  js> importPackage(net.cscott.sdr.calls.ast)
      *  js> ds = new DanceState(new DanceProgram(Program.C1), Formation.FOUR_SQUARE); undefined;
      *  js> a1 = Expr.literal("swing thru")
      *  'swing thru
      *  js> a = new Expr("finish", a1)
      *  (Expr finish 'swing thru)
      *  js> C1List.FINISH.getEvaluator(ds, a.args).simpleExpansion()
      *  (Seq (Apply '_finish swing thru))
      * @doc.test
      *  Evaluate FINISH RECYCLE.
      *  js> importPackage(net.cscott.sdr.calls)
      *  js> importPackage(net.cscott.sdr.calls.ast)
      *  js> ds = new DanceState(new DanceProgram(Program.C1), Formation.FOUR_SQUARE); undefined;
      *  js> Evaluator.parseAndEval(ds, "touch 1/2")
      *  js> a = new Expr("finish", Expr.literal("recycle"))
      *  (Expr finish 'recycle)
      *  js> C1List.FINISH.getEvaluator(ds, a.args).simpleExpansion()
      *  (Opt (From 'ANY (Seq (Part 'DIVISIBLE '1 (Opt (From 'ANY (If 'BEFORE (Expr PROGRAM AT LEAST 'C1) (Seq (Apply (Expr _box counter rotate '1/4)) (Apply 'roll)) "Fractional recycle not allowed below C1")))))))
      */
     public static final Call FINISH = new C1Call("finish") {
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             String rule = "finish (a|an)? <0=anything>";
             Grm g = Grm.parse(rule);
            return new Rule("anything", g, Fraction.valueOf(-10));
         }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args)
                 throws EvaluationException {
             Finish fv = new Finish(ds);
             assert args.size()==1;
             Apply a = new Apply(args.get(0));
             SeqCall sc = a.accept(fv, null);
             Comp result = new Seq(sc);
             // OPTIMIZATION: SEQ(PART(c)) = c
             if (sc.type==PART) {
                 Part p = (Part) sc;
                 if (p.divisibility==DIVISIBLE)
                     result = p.child;
             }
             return new Evaluator.Standard(result);
         }
     };
 
     public static final Call TANDEM = new C1Call("tandem") {
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             Grm g = Grm.parse("tandem <0=anything>");
             return new Rule("anything", g, Fraction.valueOf(-15));
         }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args) {
             assert args.size() == 1;
             return new SolidEvaluator(args.get(0), FormationList.TANDEM,
                                       SolidMatch.ALL, SolidType.SOLID);
         }
     };
 
     public static final Call SIAMESE = new C1Call("siamese") {
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             Grm g = Grm.parse("siamese <0=anything>");
             return new Rule("anything", g, Fraction.valueOf(-15));
         }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args) {
             assert args.size() == 1;
             return new SolidEvaluator(args.get(0), "siamese",
                                       MatcherList.SIAMESE, SolidType.SOLID);
         }
     };
 
 
     public static final Call CONCENTRIC = new C1Call("_concentric") {
         @Override
         public int getMinNumberOfArguments() { return 2; }
         @Override
         public Rule getRule() { return null; /* internal call */ }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args) {
             assert args.size() == 2;
             return new ConcentricEvaluator(args.get(0), args.get(1),
                                            ConcentricType.CONCENTRIC);
         }
     };
 
     /** Variant of 'concentric' to do. */
     public static enum ConcentricType { QUASI, CONCENTRIC, CROSS };
     /** Evaluator for concentric and quasi-concentric. */
     public static class ConcentricEvaluator extends Evaluator {
         private final Expr centersPart, endsPart;
         private final ConcentricType which;
         public ConcentricEvaluator(Expr centersPart, Expr endsPart,
                                    ConcentricType which) {
             this.centersPart = centersPart;
             this.endsPart = endsPart;
             this.which = which;
         }
         @Override
         public Evaluator evaluate(DanceState ds) {
             // step 1: pull out the centers/ends
             TaggedFormation f = TaggedFormation.coerce(ds.currentFormation());
             Set<Dancer> centerDancers = f.tagged(Tag.CENTER);
             Set<Dancer> endDancers = new HashSet<Dancer>(f.dancers());
             endDancers.removeAll(centerDancers); // all those who aren't CENTERs
             if (centerDancers.isEmpty())
                 throw new BadCallException("No centers!");
             if (endDancers.isEmpty())
                 throw new BadCallException("Everyone is a center!");
             Formation centerF = f.select(centerDancers).onlySelected();
             Formation endF = f.select(endDancers).onlySelected();
             // xxx should look at whether "eventual ends" (ie, the centers when
             //     doing CROSS concentric) think they are in lines or columns.
             boolean isWide = endF.bounds().width()
                                 .compareTo(endF.bounds().height()) > 0;
             // do the call in each separate formation.
             // (breathe to eliminate space left by centers in end formation)
             DanceState centerS = ds.cloneAndClear(centerF);
             DanceState endS = ds.cloneAndClear(Breather.breathe(endF));
             TreeSet<Fraction> moments = new TreeSet<Fraction>();
             new Apply(this.centersPart).evaluator(centerS).evaluateAll(centerS);
             for (TimedFormation tf: centerS.formations())
                 moments.add(tf.time);
             // in CROSS, have the ends wait until the centers are done
             Fraction endsOffset = Fraction.ZERO;
             if (which==ConcentricType.CROSS) {
                 endsOffset = moments.last();
                 endS.syncDancers(endsOffset);
             }
             new Apply(this.endsPart).evaluator(endS).evaluateAll(endS);
             for (TimedFormation tf: endS.formations())
                 moments.add(tf.time);
             for (DanceState nds: l(centerS, endS))
                 nds.syncDancers(moments.last());
 
             // XXX adjust isWide depending on ending formation, lines to lines,
             //     etc.
 
             // hard part! Merge the resulting dancer paths.
             // XXX this is largely cut-and-paste from MetaEvaluator; we should
             //     refactor out the common code.
 
             // go through all the moments in time, constructing an appropriately
             // breathed formation.
             TreeMap<Fraction,Formation> merged =
                 new TreeMap<Fraction,Formation>();
             for (Fraction t : moments) {
                 boolean isCross = (which==ConcentricType.CROSS &&
                                    t.compareTo(endsOffset) >=0);
                 Formation mergeF = merge(centerS.formationAt(t),
                                          endS.formationAt(t),
                                          isWide, isCross);
                 merged.put(t, mergeF);
             }
             // okay, now go through the individual dancer paths, adjusting the
             // 'to' and 'from' positions to match breathed.
             for (DanceState nds : l(centerS, endS)) {
                 for (Dancer d : nds.dancers()) {
                     Fraction t = Fraction.ZERO;
                     for (DancerPath dp : nds.movements(d)) {
                         Position nfrom = merged.get(t).location(d);
                         t = t.add(dp.time);
                         Position nto = merged.get(t).location(d);
                         DancerPath ndp = dp.translate(nfrom, nto);
                         ds.add(d, ndp);
                     }
                 }
             }
             // dancers should all be in sync at this point.
             // no more to evaluate
             return null;
         }
         private static Formation merge(Formation center, Formation end, boolean isWide, boolean isCross) {
             // recurse to deal with wide/cross flags.
             if (isCross)
                 return merge(end, center, isWide, !isCross);
             if (!isWide)
                 return merge(center.rotate(ExactRotation.ONE_QUARTER),
                              end.rotate(ExactRotation.ONE_QUARTER),
                              !isWide, isCross)
                        .rotate(ExactRotation.mONE_QUARTER);
             // XXX do we need to breathe the center/end formations here?
             return mergeWide(center, end);
         }
         private static Formation mergeWide(Formation center, Formation end) {
             Map<Dancer,Position> location = new HashMap<Dancer,Position>();
             for (Dancer d : center.dancers())
                 location.put(d, center.location(d));
             Box centerBounds = center.bounds();
             for (Dancer d : end.dancers()) {
                 Position p = end.location(d);
                 Fraction nx = p.x, ny = p.y;
                 if (p.x.equals(Fraction.ZERO) && p.y.equals(Fraction.ZERO))
                     throw new BadCallException("Outside dancer ends up at origin!");
                 if (p.x.compareTo(Fraction.ZERO) == 0) {
                     ny = ny.add((p.y.compareTo(Fraction.ZERO) < 0 ?
                                  centerBounds.ll : centerBounds.ur).y);
                 } else {
                     nx = nx.add((p.x.compareTo(Fraction.ZERO) < 0 ?
                                  centerBounds.ll : centerBounds.ur).x);
                 }
                 location.put(d, p.relocate(nx, ny, p.facing));
             }
             return new Formation(location);
         }
     };
 }
