 package net.cscott.sdr.calls.transform;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.junit.runner.RunWith;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.Breather;
 import net.cscott.sdr.calls.CallDB;
 import net.cscott.sdr.calls.DanceState;
 import net.cscott.sdr.calls.Dancer;
 import net.cscott.sdr.calls.DancerPath;
 import net.cscott.sdr.calls.Formation;
 import net.cscott.sdr.calls.FormationList;
 import net.cscott.sdr.calls.FormationMatch;
 import net.cscott.sdr.calls.NoMatchException;
 import net.cscott.sdr.calls.Position;
 import net.cscott.sdr.calls.Predicate;
 import net.cscott.sdr.calls.Selector;
 import net.cscott.sdr.calls.TaggedFormation;
 import net.cscott.sdr.calls.TimedFormation;
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.ast.AstNode;
 import net.cscott.sdr.calls.ast.Comp;
 import net.cscott.sdr.calls.ast.Condition;
 import net.cscott.sdr.calls.ast.If;
 import net.cscott.sdr.calls.ast.In;
 import net.cscott.sdr.calls.ast.Opt;
 import net.cscott.sdr.calls.ast.OptCall;
 import net.cscott.sdr.calls.ast.Par;
 import net.cscott.sdr.calls.ast.ParCall;
 import net.cscott.sdr.calls.ast.Part;
 import net.cscott.sdr.calls.ast.Prim;
 import net.cscott.sdr.calls.ast.Seq;
 import net.cscott.sdr.calls.ast.SeqCall;
 import net.cscott.sdr.util.Fraction;
 import net.cscott.sdr.util.ListUtils;
 
 /**
  * An {@link Evaluator} represents a current dance context.
  * Evaluators are usually stacked: the standard evaluator
  * might call down into a child evaluator for the "tandem"
  * concept, for example, which will then reinvoke the standard
  * evaluator to evaluate "trade".  Evaluators keep a continuation
  * context, since each call to {@link Evaluator#evaluate} does
  * only "one part" of a call &mdash; the remaining parts go
  * in the continuation.
  * <p>
  * As the {@link Evaluator}s operate, they accumulate dancer movements
  * and actions in a {@link DanceState}.</p>
  *
  * @author C. Scott Ananian
  * @doc.test Simplest invocation: "heads start" from squared set.
  *  js> importPackage(net.cscott.sdr.calls);
  *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |     3Gv  3Bv
  *  |
  *  |4B>            2G<
  *  |
  *  |4G>            2B<
  *  |
  *  |     1B^  1G^
  *  js> comp = CallDB.INSTANCE.parse(ds.dance.program, "heads start");
  *  (Apply heads start)
  *  js> comp = new net.cscott.sdr.calls.ast.Seq(comp);
  *  (Seq (Apply heads start))
  *  js> e = new Evaluator.Standard(comp);
  *  net.cscott.sdr.calls.transform.Evaluator$Standard@166cb16
  *  js> e.evaluateAll(ds);
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |4B>  3Gv  3Bv  2G<
  *  |
  *  |4G>  1B^  1G^  2B<
  * @doc.test "Heads pair off" from squared set -- this is a bit of a hack, it
  *  should eventually be "heads start, heads (pair off)" or some such.
  *  js> importPackage(net.cscott.sdr.calls);
  *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |     3Gv  3Bv
  *  |
  *  |4B>            2G<
  *  |
  *  |4G>            2B<
  *  |
  *  |     1B^  1G^
  *  js> comp = CallDB.INSTANCE.parse(ds.dance.program, "heads pair off");
  *  (Apply heads pair off)
  *  js> comp = new net.cscott.sdr.calls.ast.Seq(comp);
  *  (Seq (Apply heads pair off))
  *  js> e = new Evaluator.Standard(comp);
  *  net.cscott.sdr.calls.transform.Evaluator$Standard@166cb16
  *  js> e.evaluateAll(ds);
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |4B>  3G<  3B>  2G<
  *  |
  *  |4G>  1B<  1G>  2B<
  * @doc.test More complex calls from facing couples.
  *  js> importPackage(net.cscott.sdr.calls);
  *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.FOUR_SQUARE); undefined;
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |3Gv  3Bv
  *  |
  *  |1B^  1G^
  *  js> Evaluator.parseAndEval(ds, "boys walk girls dodge");
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |1B^  3Gv
  *  |
  *  |1G^  3Bv
  *  js> Evaluator.parseAndEval(ds, "girls walk others dodge");
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |1G^  1B^
  *  |
  *  |3Bv  3Gv
  *  js> Evaluator.parseAndEval(ds, "trade", "roll");
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |1B>  1G<
  *  |
  *  |3G>  3B<
  * @doc.test Recursive evaluation with fractionalization, left concept,
  *  breathing, etc:
  *  js> importPackage(net.cscott.sdr.calls);
  *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.FOUR_SQUARE); undefined;
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |3Gv  3Bv
  *  |
  *  |1B^  1G^
  *  js> Evaluator.parseAndEval(ds, "square thru three and a half");
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |3G<
  *  |
  *  |3B>
  *  |
  *  |1B<
  *  |
  *  |1G>
  * @doc.test Matching waves; fan the top; even timing:
  *  js> importPackage(net.cscott.sdr.calls);
  *  js> ds = new DanceState(new DanceProgram(Program.A1), Formation.SQUARED_SET); undefined;
  *  js> Evaluator.parseAndEval(ds, "heads pair off; do half of a pass thru");
  *  js> ds = ds.cloneAndClear(Breather.breathe(ds.currentFormation()))
  *  net.cscott.sdr.calls.DanceState@831e35
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |4B>  3B>
  *  |
  *  |3G<  2G<
  *  |
  *  |4G>  1G>
  *  |
  *  |1B<  2B<
  *  js> Evaluator.parseAndEval(ds, "fan the top")
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |1B^  4Gv  3G^  4Bv  2B^  1Gv  2G^  3Bv
  *  js> ds.movements(StandardDancer.COUPLE_1_BOY)
  *  [DancerPath[from=-1,-3,w,to=-7,-3,nw,[ROLL_RIGHT],time=2,pointOfRotation=FOUR_DANCERS], DancerPath[from=-7,-3,nw,[ROLL_RIGHT],to=-7,0,n,[ROLL_RIGHT],time=2,pointOfRotation=FOUR_DANCERS]]
  *  js> ds.movements(StandardDancer.COUPLE_4_GIRL)
  *  [DancerPath[from=-1,-1,e,to=-1,0,n,[ROLL_LEFT],time=1 1/3,pointOfRotation=FOUR_DANCERS], DancerPath[from=-1,0,n,[ROLL_LEFT],to=-3,1,nw,[ROLL_LEFT],time=2/3,pointOfRotation=FOUR_DANCERS], DancerPath[from=-3,1,nw,[ROLL_LEFT],to=-3,1,w,[ROLL_LEFT],time=2/3,pointOfRotation=FOUR_DANCERS], DancerPath[from=-3,1,w,[ROLL_LEFT],to=-5,0,s,[ROLL_LEFT],time=1 1/3,pointOfRotation=FOUR_DANCERS]]
  * @doc.test Four-person "pass thru":
  *  js> importPackage(net.cscott.sdr.calls);
  *  js> ds = new DanceState(new DanceProgram(Program.BASIC), Formation.FOUR_SQUARE); undefined;
  *  js> Evaluator.parseAndEval(ds, "pass thru")
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |1B^  1G^
  *  |
  *  |3Gv  3Bv
  *  js> ds.movements(StandardDancer.COUPLE_1_BOY)
  *  [DancerPath[from=-1,-1,n,to=-3,0,n,time=1,pointOfRotation=<null>], DancerPath[from=-3,0,n,to=-1,1,n,time=1,pointOfRotation=<null>]]
  *  js> ds.movements(StandardDancer.COUPLE_3_GIRL)
  *  [DancerPath[from=-1,1,s,to=-1,0,s,time=1,pointOfRotation=<null>], DancerPath[from=-1,0,s,to=-1,-1,s,time=1,pointOfRotation=<null>]]
  */
 @RunWith(value=JDoctestRunner.class)
 public abstract class Evaluator {
     /**
      * Do "one part" of the continuation, and return an {@link Evaluator}
      * which will do the remaining parts, or null if there are no
      * additional parts to evaluate.
      * @param ds The dynamic dance state.  Accumulates dancer actions and
      *   movements and tracks static dance information like the level of the
      *   dance.
      * @return An {@link Evaluator} for the remaining parts, or null.
      */
     public abstract Evaluator evaluate(DanceState ds);
 
     public final void evaluateAll(DanceState ds) {
         for(Evaluator e = this; e!=null; )
             e = e.evaluate(ds);
     }
 
     /** Convenience method for easy testing. */
     public static void parseAndEval(DanceState ds, String... calls) {
         List<Apply> l = new ArrayList<Apply>(calls.length);
         for (String s : calls)
             l.add(CallDB.INSTANCE.parse(ds.dance.getProgram(), s));
         Comp c = new Seq(l.toArray(new SeqCall[l.size()]));
         new Standard(c).evaluateAll(ds);
     }
     /** Create an evaluator which breathes each formation to resolve
      *  collisions.  Good to use as a top-level evaluator. */
     public static Evaluator breathedEval(Formation f, Comp c) {
         Formation meta = FormationList.SINGLE_DANCER;
         Dancer rep = meta.dancers().iterator().next();
         TaggedFormation tf = TaggedFormation.coerce(f);
         FormationMatch fm = new FormationMatch
             (meta, Collections.singletonMap(rep, tf),
                    Collections.<Dancer>emptySet());
         return new MetaEvaluator(fm, c);
     }
 
     /**
      * This is the standard top level evaluator.  It contains a number
      * of convenience methods for adding elements dynamically to the
      * continuation and accessing the current top-level formation.
      */
     public static class Standard extends Evaluator {
         final AstNode continuation;
         /** Create a standard evaluator which wll dance the specified calls. */
         public Standard(Comp continuation) { this((AstNode)continuation); }
         /** Private constructor for internal use. */
         Standard(AstNode continuation) {
             this.continuation = continuation;
         }
         @Override
         public Evaluator evaluate(DanceState ds) {
             return this.continuation.accept(new StandardVisitor(),ds);
         }
         private class StandardVisitor extends ValueVisitor<Evaluator,DanceState> {
             /**
              * Expand any 'Apply' node we come to, possibly switching
              * Evaluators. Does not impose a part boundary.
              */
             @Override
             public Evaluator visit(Apply a, DanceState ds) {
                 Evaluator e = a.evaluator();
                 if (e==null)
                     // use standard evaluator if call is simple
                     e = new Standard(a.expand());
                 return e.evaluate(ds);
             }
             /* Evaluate predicates, and either evaluate the child or
              * throw a BadCallException. (Does not impose a part boundary.) */
             @Override
             public Evaluator visit(If iff, DanceState ds) {
                 // evaluate the predicate
                 Predicate p = iff.condition.getPredicate();
                 if (!p.evaluate(ds.dance, ds.currentFormation(), iff.condition)) 
                     throw new BadCallException(iff.message, iff.priority);
                 Comp c = iff.child;
                 return c.accept(this, ds); // keep going
             }
             /**
              * Evaluate the child in a substate, and then adjust its timing.
              * Does impose a part boundary.
              */
             @Override
             public Evaluator visit(In in, DanceState ds) {
                 Comp inRemoved = RemoveIn.removeIn(in);
                 // if inRemoved is not an in, then just evaluate it
                 if (inRemoved != in)
                     return inRemoved.accept(this, ds);
                 // otherwise:
                 DanceState nds = ds.cloneAndClear();
                 new Standard(in.child).evaluateAll(nds);
                 // figure out how much we have to adjust the timing
                 Fraction finalTime = nds.currentTime();
                 Fraction multiplier = in.count.divide(finalTime);
                 // okay, iterate through all the dancer paths and adjust them
                 // XXX: would be more efficient if we grouped the paths by
                 //      time
                 for (Dancer d: ds.currentFormation().dancers())
                     for (DancerPath dp : nds.movements(d))
                         ds.add(d, dp.scaleTime(multiplier));
                 // ok, done.
                 return null; // no further parts
             }
             /** Try all the options, keeping the first one which works. */
             @Override
             public Evaluator visit(Opt opt, DanceState ds) {
                 List<String> reasons=new ArrayList<String>(opt.children.size());
                 for (OptCall oc: opt.children) {
                     try {
                         return oc.accept(this, ds);
                     } catch (NoMatchException bce) {
                         /* ignore; try the next one */
                         reasons.add(bce.reason);
                     }
                 }
                 /* Hmm, none of the options worked. */
 		// XXX: this only reports outermost formations
 		//  if requires OCEAN WAVES and then within that
 		//    requires BOYS ARE ENDS (or whatever) will only
 		//  report that OCEAN WAVES is invalid.  We need to
 		//  percolate information out from inner matches.
 		//  Something like:
 		//    couldn't evaluate from ocean waves (boys are not ends) or
 		//    from lines (not found)
 		String msg = "Invalid formation";
 		msg += " ("+ListUtils.join(reasons, ", ")+")";
                 throw new BadCallException(msg, Fraction.mONE);
             }
             /** Try all the selectors. */
             @Override
             public Evaluator visit(OptCall oc, DanceState ds) {
                 // Match from the breathed version of the formation.
                 Formation f = Breather.breathe(ds.currentFormation());
                 List<String> reasons = new ArrayList<String>(oc.selectors.size());
                 for (Selector s: oc.selectors) {
                     FormationMatch fm;
                     try {
                         fm = s.match(f);
                     } catch (NoMatchException nme) {
                         /* ignore; try the next selector */
                         reasons.add(nme.target+" ("+nme.reason+")");
                         continue;
                     }
                     // we distinguish call errors from match errors:
                     try {
                         return new MetaEvaluator(fm, oc.child).evaluate(ds);
                     } catch (BadCallException bce) {
                         reasons.add(s.toString()+" ("+bce.getMessage()+")");
                         /* continue with the next selector */
                     }
                 }
                 /* Hmm, none of the selectors matched. */
 		// this exception should only be seen internally
                 throw new NoMatchException(oc.selectors.toString(),
                                            ListUtils.join(reasons, ", "));
             }
             /**
              * Evaluate multiple "do your parts" against particularly-tagged
              * dancers, then superimpose the results.  Ensure that every
              * dancer in the current formation matches at least one tag.
              */
             @Override
             public Evaluator visit(Par p, DanceState ds) {
                 // get the current tagged formation
                 Formation f = ds.currentFormation();
                 // hm, dynamically apply tags here?
                 TaggedFormation tf = TaggedFormation.coerce(f);
                 // we're going to want to ensure that every dancer matches
                 // some tag.
                 Set<Dancer> unmatched = new LinkedHashSet<Dancer>(f.dancers());
                 PartsCombineEvaluator pce = new PartsCombineEvaluator();
                 for (ParCall pc : p.children) {
                     // find the dancers matched, adjusting unmatched set
                     Set<Dancer> matched = tf.tagged(pc.tags);
                     matched.retainAll(unmatched);
                     unmatched.removeAll(matched);
                     // create a "do your part" evaluator.
                     if (!matched.isEmpty())
                         pce.add(matched, pc.child, ds);
                 }
                 // all dancers must match a part.
                 if (!unmatched.isEmpty())
                    throw new BadCallException("unmatched dancers");
                 // ok, now do one step of the evaluation.
                 return pce.evaluate(ds);
             }
             /** Simply recurse into child. */
             @Override
             public Evaluator visit(Part p, DanceState ds) {
                 return p.child.accept(this, ds);
             }
             /**
              * Use {@link EvalPrim} to create a {@link DancerPath} for each
              * selected dancer.
              */
             // XXX: FACTOR 'selected' out of the Formation class and make it
             // a private property of this evaluator?  Or of the DancerState?
             @Override
             public Evaluator visit(Prim p, DanceState ds) {
                 Formation f = ds.currentFormation(); // xxx is this right?
                 for (Dancer d: f.selectedDancers()) {
                     DancerPath dp = EvalPrim.apply(d, f, p);
                     ds.add(d, dp);
                 }
                 // that was easy!
                 return null;
             }
             /**
              * Evaluate the first part, and return the rest as a continuation.
              * Each child is a part boundary.
              */
             @Override
             public Evaluator visit(Seq s, DanceState ds) {
                 // make an evaluator chain out of s's children.
                 ListIterator<SeqCall> it = s.children.listIterator(s.children.size());
                 Evaluator e = new Standard(it.previous());
                 while (it.hasPrevious()) {
                     e = new EvaluatorChain(new Standard(it.previous()), e);
                 }
                 // okay, do one step of this evaluation.
                 return e.evaluate(ds);
             }
             @Override
             public Evaluator visit(Comp c, DanceState ds) {
                 assert false : "missing case!";
                 return null;
             }
             @Override
             public Evaluator visit(Condition c, DanceState ds) {
                 assert false : "not a Comp";
                 return null;
             }
             @Override
             public Evaluator visit(SeqCall s, DanceState ds) {
                 assert false : "missing SeqCall case!";
                 return null;
             }
             @Override
             public Evaluator visit(ParCall pc, DanceState ds) {
                 assert false : "case handled in Par parent";
                 return null;
             }
         }
     }
     /**
      * Chains multiple evaluators together.  Used to implement {@link Seq}.
      */
     public static class EvaluatorChain extends Evaluator {
         final Evaluator head;
         final Evaluator next;
         public EvaluatorChain(Evaluator head, Evaluator next) {
             this.head = head;
             this.next = next;
         }
         @Override
         public Evaluator evaluate(DanceState ds) {
             Evaluator e = head.evaluate(ds);
             return (e!=null) ? new EvaluatorChain(e, this.next) : this.next;
         }
     }
     /** Implements {@link Opt}: evaluates a call in a meta formation. */
     private static class MetaEvaluator extends Evaluator {
         private final int metaSize;
         private final Formation meta;
         private final Map<Dancer,? extends Formation> parts;
         private final Map<Dancer,Evaluator> emap;
         MetaEvaluator(Formation meta, Map<Dancer,? extends Formation> parts,
                       Map<Dancer,Evaluator> emap) {
             this.meta = meta;
             this.parts = parts;
             this.emap = emap;
             this.metaSize = meta.dancers().size();
         }
         MetaEvaluator(FormationMatch fm, Comp child) {
             this(fm.meta, fm.matches, _makeStandardEvaluators(fm, child));
         }
         private static Map<Dancer,Evaluator>
         _makeStandardEvaluators(FormationMatch fm, Comp child) {
             boolean matchedOne = false;
             Map<Dancer,Evaluator> emap = new HashMap<Dancer,Evaluator>
                 (fm.meta.dancers().size());
             for (Dancer d : fm.meta.dancers()) {
                 Evaluator e = fm.unmatched.contains(d)
                         ? null : new Standard(child);
                 if (e!=null) matchedOne = true;
                 emap.put(d, e);
             }
             assert matchedOne;
             return emap;
         }
         @Override
         public Evaluator evaluate(DanceState ds) {
             List<Dancer> metaDancers=new ArrayList<Dancer>(this.meta.dancers());
             Map<Dancer,DanceState> substates =
                 new HashMap<Dancer,DanceState>(this.metaSize);
             // maintain a list of unique time values.
             TreeSet<Fraction> moments = new TreeSet<Fraction>();
             // do a sub-evaluation in each part of the match
             // use evaluateAll here because the subformations could
             // have different #s of parts (for the same call) and we
             // don't want to get them out of sync.
             //  xxx: is this really a problem?
             for (Dancer metaDancer : metaDancers) {
                 Formation sub = this.parts.get(metaDancer);
                 Evaluator e = emap.get(metaDancer);
                 DanceState nds = ds.cloneAndClear(sub);
                 if (e != null)
                     e.evaluateAll(nds);
                 substates.put(metaDancer, nds);
                 for (TimedFormation tf: nds.formations())
                     moments.add(tf.time);
             }
             // go through all the moments in time, constructing an appropriately
             // breathed formation.
             TreeMap<Fraction,Formation> breathed =
                 new TreeMap<Fraction,Formation>();
             for (Fraction t : moments) {
                 Map<Dancer,Formation> components =
                     new HashMap<Dancer,Formation>();
                 for (Dancer metaDancer : metaDancers) {
                     components.put(metaDancer, Breather.breathe
                                    (substates.get(metaDancer).formationAt(t)));
                 }
                 // insert the results into a new formation, breathing as necessary
                 breathed.put(t, Breather.insert(this.meta, components));
             }
             // okay, now go through the individual dancer paths, adjusting the
             // 'to' and 'from' positions to match breathed.
             for (Dancer metaDancer : metaDancers) {
                 DanceState nds = substates.get(metaDancer);
                 nds.syncDancers(moments.last());
                 for (Dancer d : nds.dancers()) {
                     Fraction t = Fraction.ZERO;
                     for (DancerPath dp : nds.movements(d)) {
                         Position nfrom = breathed.get(t).location(d);
                         t = t.add(dp.time);
                         Position nto = breathed.get(t).location(d);
                         DancerPath ndp = dp.translate(nfrom, nto);
                         ds.add(d, ndp);
                     }
                 }
             }
             // dancers should all be in sync at this point.
             return null;
         }
     }
     /**
      * Implements {@link Par}: evaluates several "do your part" calls, and
      * then mashes the results together.
      */
     private static class PartsCombineEvaluator extends Evaluator {
         private static class SubPart {
             public final Set<Dancer> matched;
             public final DanceState ds;
             public final Evaluator eval;
             public SubPart(Set<Dancer> matched, Evaluator eval, DanceState ds) {
                 this.matched = matched;
                 this.eval = eval;
                 this.ds = ds;
             }
         }
         private List<SubPart> parts = new ArrayList<SubPart>();
         void add(Set<Dancer> matched, Comp subcall, DanceState ds) {
             this.add(matched, new Standard(subcall), ds);
         }
         void add(Set<Dancer> matched, Evaluator eval, DanceState ds) {
             // transform dance state in 'do your parts'
             // xxx: maybe change unselected dancers to phantoms?
             //      consider "do your part star turns" in a facing plenty
             DanceState nds = ds.cloneAndClear
                 (ds.currentFormation().select(matched));
             this.parts.add(new SubPart(matched, eval, nds));
         }
         @Override
         public Evaluator evaluate(DanceState ds) {
             PartsCombineEvaluator pce = new PartsCombineEvaluator();
             // do one part of each subcall
             for (SubPart p: parts) {
                 Evaluator ne = p.eval.evaluate(p.ds);
                 // add only the selected dancer's actions
                 for (Dancer d: p.matched)
                     for (DancerPath dp : p.ds.movements(d))
                         ds.add(d, dp);
                 if (ne==null) continue;
                 // create an evaluator for the next part
                 pce.add(p.matched, ne, p.ds);
             }
             // is there a continuation?
             return pce.parts.isEmpty() ? null : pce;
         }
     }
 }
