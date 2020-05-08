 package net.cscott.sdr.calls;
 
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
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.ExprFunc.EvaluationException;
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.ast.AstNode;
 import net.cscott.sdr.calls.ast.Comp;
 import net.cscott.sdr.calls.ast.Expr;
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
 import net.cscott.sdr.calls.transform.RemoveIn;
 import net.cscott.sdr.calls.transform.ValueVisitor;
 import net.cscott.sdr.util.Fraction;
 import net.cscott.sdr.util.ListUtils;
 
 import org.junit.runner.RunWith;
 
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
  *  js> importPackage(net.cscott.sdr.calls.ast);
  *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |     3Gv  3Bv
  *  |
  *  |4B>            2G<
  *  |
  *  |4G>            2B<
  *  |
  *  |     1B^  1G^
  *  js> comp = AstNode.valueOf("(Seq (Apply (Expr _square start 'HEAD)))");
  *  (Seq (Apply (Expr _square start 'HEAD)))
  *  js> e = new Evaluator.Standard(comp); undefined
  *  js> e.evaluateAll(ds);
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |4B>  3Gv  3Bv  2G<
  *  |
  *  |4G>  1B^  1G^  2B<
  * @doc.test "Heads pair off" from squared set.
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
  *  (Apply (Expr anyone while others 'HEAD 'pair off 'nothing))
  *  js> comp = new net.cscott.sdr.calls.ast.Seq(comp);
  *  (Seq (Apply (Expr anyone while others 'HEAD 'pair off 'nothing)))
  *  js> e = new Evaluator.Standard(comp); undefined
  *  js> e.evaluateAll(ds);
  *  js> Breather.breathe(ds.currentFormation()).toStringDiagram("|");
  *  |4B>  3G<  3B>  2G<
  *  |
  *  |4G>  1B<  1G>  2B<
  * @doc.test More complex calls from facing couples.
  *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.FOUR_SQUARE); undefined;
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |3Gv  3Bv
  *  |
  *  |1B^  1G^
  *  js> Evaluator.parseAndEval(ds, "boys walk girls dodge");
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |1B^  3Gv
  *  |
  *  |1G^  3Bv
  *  js> Evaluator.parseAndEval(ds, "girls walk others dodge");
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |1G^  1B^
  *  |
  *  |3Bv  3Gv
  *  js> Evaluator.parseAndEval(ds, "trade", "roll");
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |1B>  1G<
  *  |
  *  |3G>  3B<
  * @doc.test Recursive evaluation with fractionalization, left concept,
  *  breathing, etc:
  *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.FOUR_SQUARE); undefined;
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |3Gv  3Bv
  *  |
  *  |1B^  1G^
  *  js> Evaluator.parseAndEval(ds, "square thru three and a half");
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |3G<
  *  |
  *  |3B>
  *  |
  *  |1B<
  *  |
  *  |1G>
  * @doc.test Matching waves; fan the top; even timing:
  *  js> ds = new DanceState(new DanceProgram(Program.A1), Formation.SQUARED_SET); undefined;
  *  js> Evaluator.parseAndEval(ds, "heads pair off; do half of a pass thru");
  *  js> ds = ds.cloneAndClear(); undefined
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |4B>  3B>
  *  |
  *  |3G<  2G<
  *  |
  *  |4G>  1G>
  *  |
  *  |1B<  2B<
  *  js> Evaluator.parseAndEval(ds, "fan the top")
  *  js> ds.currentFormation().toStringDiagram("|");
  *  |1B^  4Gv  3G^  4Bv  2B^  1Gv  2G^  3Bv
  *  js> ds.movements(StandardDancer.COUPLE_1_BOY)
  *  [DancerPath[from=-1,-3,w,to=-6,-4,nw,[ROLL_RIGHT, SWEEP_LEFT],time=2,pointOfRotation=FOUR_DANCERS], DancerPath[from=-6,-4,nw,[ROLL_RIGHT, SWEEP_LEFT],to=-7,0,n,[ROLL_RIGHT, SWEEP_LEFT],time=2,pointOfRotation=FOUR_DANCERS]]
  *  js> ds.movements(StandardDancer.COUPLE_4_GIRL)
  *  [DancerPath[from=-1,-1,e,to=-1,0,n,[ROLL_LEFT, SWEEP_RIGHT],time=1 1/3,pointOfRotation=FOUR_DANCERS], DancerPath[from=-1,0,n,[ROLL_LEFT, SWEEP_RIGHT],to=-2,0,nw,[ROLL_LEFT, SWEEP_RIGHT],time=2/3,pointOfRotation=FOUR_DANCERS], DancerPath[from=-2,0,nw,[ROLL_LEFT, SWEEP_RIGHT],to=-3,1,w,[ROLL_LEFT, SWEEP_RIGHT],time=2/3,pointOfRotation=FOUR_DANCERS], DancerPath[from=-3,1,w,[ROLL_LEFT, SWEEP_RIGHT],to=-5,0,s,[ROLL_LEFT, SWEEP_RIGHT],time=1 1/3,pointOfRotation=FOUR_DANCERS]]
  * @doc.test Four-person "pass thru":
  *  js> ds = new DanceState(new DanceProgram(Program.BASIC), Formation.FOUR_SQUARE); undefined;
  *  js> Evaluator.parseAndEval(ds, "pass thru")
  *  js> ds.currentFormation().toStringDiagram("|");
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
     /** Return true iff this Evaluator simply evaluates an Ast tree.
      *  That is, if we can "look inside" the definition of this call or concept
      *  by treating it as equivalent to its expansion.
      */
     public boolean hasSimpleExpansion() { return false; }
     /** Returns the equivalent simple expansion of the call, if it has one.
      * @throws IllegalArgumentException if {@link #hasSimpleExpansion()} is
      *         false.
      */
     public Comp simpleExpansion() {
         assert !hasSimpleExpansion();
         throw new IllegalArgumentException("Does not have a simple expansion");
     }
     @Override
     public String toString() {
         if (hasSimpleExpansion())
             return this.getClass().getName()+"("+simpleExpansion()+")";
         return super.toString();
     }
 
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
         breathedEval(ds.currentFormation(), c).evaluateAll(ds);
     }
     /** Create an evaluator which breathes each formation to resolve
      *  collisions.  Good to use as a top-level evaluator, but note
      *  that phantom concepts will have to return all the phantoms
      *  up to this step in order for them not to be breathed away. */
     // XXX: probably want a top-level breather which resolves collisions
     //      but does *not* screw with absolute position if there aren't collisions?
     public static Evaluator breathedEval(Formation f, Comp c) {
         Formation meta = FormationList.SINGLE_DANCER;
         Dancer rep = meta.dancers().iterator().next();
         TaggedFormation tf = TaggedFormation.coerce(f);
         FormationMatch fm = new FormationMatch
             (meta, Collections.singletonMap(rep, tf),
                    Collections.<Dancer>emptySet());
         return new MetaEvaluator(fm, c) {
             // crazy hack to reuse code -- I'm so lazy!
             @Override
             protected boolean breatheParts() { return true; }
         };
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
         public boolean hasSimpleExpansion() { return true; }
         @Override
         public Comp simpleExpansion() {
             if (continuation instanceof SeqCall)
                 return new Seq((SeqCall)continuation);
             return (Comp) continuation;
         }
         @Override
         public Evaluator evaluate(DanceState ds) {
             return this.continuation.accept(new StandardVisitor(),ds);
         }
         @Override
         public String toString() {
             return "StandardEvaluator("+continuation+")";
         }
         private class StandardVisitor extends ValueVisitor<Evaluator,DanceState> {
             /**
              * Expand any 'Apply' node we come to, possibly switching
              * Evaluators. Does not impose a part boundary.
              */
             @Override
             public Evaluator visit(Apply a, DanceState ds) {
                 Evaluator e = a.evaluator(ds);
                 return e.evaluate(ds);
             }
             /* Evaluate predicates, and either evaluate the child or
              * throw a BadCallException. (Does not impose a part boundary.) */
             @Override
             public Evaluator visit(If iff, DanceState ds) {
                 DanceState nds = ds;
                 if (iff.when==If.When.AFTER) {
                     nds = ds.cloneAndClear();
                     new Standard(iff.child).evaluateAll(nds);
                 }
                 // evaluate the predicate
                 boolean predicate;
                 try {
                     predicate = iff.condition.evaluate(Boolean.class, nds);
                 } catch (EvaluationException e) {
                     assert false : "Expression failed to evaluate: "+iff;
                     throw new BadCallException("Couldn't evaluate expression");
                 }
                 if (!predicate)
                     throw new BadCallException(iff.message, iff.priority);
                 if (iff.when==If.When.BEFORE) {
                     return iff.child.accept(this, nds); // keep going
                 }
                 assert iff.when==If.When.AFTER;
                 // transfer state from nds to ds
                 for (Dancer d : nds.dancers())
                     for (DancerPath dp : nds.movements(d))
                         ds.add(d, dp);
                 return null;
             }
             /**
              * Evaluate the child in a substate, and then adjust its timing.
              * Does impose a part boundary.
              */
             @Override
             public Evaluator visit(In in, DanceState ds) {
                 Comp inRemoved = RemoveIn.removeIn(ds, in);
                 // if inRemoved is not an in, then just evaluate it
                 if (inRemoved != in)
                     return inRemoved.accept(this, ds);
                 // otherwise:
                 Fraction desiredTime;
                 try {
                     desiredTime = in.count.evaluate(Fraction.class, ds);
                 } catch (EvaluationException e) {
                     throw new BadCallException("Evaluation error: "+e);
                 }
                 DanceState nds = ds.cloneAndClear();
                 new Standard(in.child).evaluateAll(nds);
                 // figure out how much we have to adjust the timing
                 Fraction finalTime = nds.currentTime();
                 Fraction multiplier = desiredTime.divide(finalTime);
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
             /** Try all the matchers. */
             @Override
             public Evaluator visit(OptCall oc, DanceState ds) {
                 // Match from the breathed version of the formation.
                 Formation f = Breather.breathe(ds.currentFormation());
                 List<String> reasons = new ArrayList<String>();
                 // XXX bit of a hack here: we don't want BadCallExceptions to
                 // bail out of the entire top-level OR.  Eventually we'll want
                 // to introduce some other sort of combiner for this.
                 List<Expr> matchers;
                 if (oc.matcher.atom=="or")
                     matchers = oc.matcher.args;
                 else
                     matchers = Collections.singletonList(oc.matcher);
                 for (Expr e: matchers) {
                     Matcher m;
                     FormationMatch fm;
                     try {
                         m = e.evaluate(Matcher.class, ds);
                     } catch (EvaluationException ee) {
                         assert false : "error in definition";
                         reasons.add(e.toShortString()+" ("+ee.getMessage()+")");
                         continue;
                     }
                     try {
                         fm = ds.tagDesignated(m.match(f));
                     } catch (NoMatchException nme) {
                         /* ignore; try the next matcher */
                         reasons.add(nme.target+" ("+nme.reason+")");
                         continue;
                     }
                     // we distinguish call errors from match errors:
                     try {
                         return new MetaEvaluator(fm, oc.child).evaluate(ds);
                     } catch (BadCallException bce) {
                         reasons.add(m.toString()+" ("+bce.getMessage()+")");
                         /* continue with the next matcher */
                     }
                 }
                 /* Hmm, none of the matchers matched. */
                 // this exception should only be seen internally
                 throw new NoMatchException(oc.matcher.toShortString(),
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
                     Set<Dancer> matched = pc.evaluate(ds).select(tf);
                     matched.retainAll(unmatched);
                     unmatched.removeAll(matched);
                     // create a "do your part" evaluator.
                     if (!matched.isEmpty())
                         pce.add(matched, pc.child, ds);
                 }
                 // all dancers must match a part.
                 if (!unmatched.isEmpty())
                     throw new BadCallException("Some dancers are not matched");
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
             public Evaluator visit(SeqCall s, DanceState ds) {
                 assert false : "missing SeqCall case!";
                 return null;
             }
             @Override
             public Evaluator visit(ParCall pc, DanceState ds) {
                 assert false : "case handled in Par parent";
                 return null;
             }
             @Override
             public Evaluator visit(Expr e, DanceState t) {
                 assert false : "expr should be handled in parent";
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
         @Override
         public boolean hasSimpleExpansion() {
             return head.hasSimpleExpansion() && next.hasSimpleExpansion();
         }
         @Override
         public Comp simpleExpansion() {
             return new Seq(new Part(Part.Divisibility.DIVISIBLE, Fraction.ONE,
                                     head.simpleExpansion()),
                            new Part(Part.Divisibility.DIVISIBLE, Fraction.ONE,
                                     next.simpleExpansion()));
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
         protected boolean breatheParts() { return false; }
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
                     Formation subF = substates.get(metaDancer).formationAt(t);
                     // in general, don't breathe subformations here!
                     // this would screw things up if (eg) this is a one-match
                     // 'from' intended only to tag dancers, and the subcall is
                     // a space-invader.
                     // But I'm lazy, and the top-level breathedEval can share
                     // 99% of this code if only I allow it to breathe here.
                     // So this is a HACK!
                     if (breatheParts())
                         subF = Breather.breathe(subF); // HACK!
                     components.put(metaDancer, subF);
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
                        assert dp.time.compareTo(Fraction.ZERO) > 0;
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
                     // sometimes phantoms introduced by a par can match a
                     // subsidiary par.  So make sure that this dancer actually
                     // belonged to the parent DanceState before we add its
                     // movements.
                     if (ds.dancers().contains(d))
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
