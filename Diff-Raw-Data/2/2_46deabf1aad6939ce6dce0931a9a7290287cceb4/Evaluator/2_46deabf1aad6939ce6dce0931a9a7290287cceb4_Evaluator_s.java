 package net.cscott.sdr.calls.transform;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.DanceState;
 import net.cscott.sdr.calls.Dancer;
 import net.cscott.sdr.calls.DancerPath;
 import net.cscott.sdr.calls.Formation;
 import net.cscott.sdr.calls.FormationMatch;
 import net.cscott.sdr.calls.NoMatchException;
 import net.cscott.sdr.calls.Predicate;
 import net.cscott.sdr.calls.Selector;
 import net.cscott.sdr.calls.TaggedFormation;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
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
 import net.cscott.sdr.util.Tools;
 
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
  */
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
                     throw new BadCallException("condition failed");
                 Comp c = iff.child;
                 return c.accept(this, ds); // keep going
             }
             /**
              * Evaluate the child in a substate, and then adjust its timing.
              * Does impose a part boundary.
              */
             @Override
             public Evaluator visit(In in, DanceState ds) {
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
                 for (OptCall oc: opt.children) {
                     try {
                         return oc.accept(this, ds);
                     } catch (BadCallException bce) {
                         /* ignore; try the next one */
                     }
                 }
                 /* Hmm, none of the options worked. */
                 throw new BadCallException("no matching formation");
             }
             /** Try all the selectors. */
             @Override
             public Evaluator visit(OptCall oc, DanceState ds) {
                 for (Selector s: oc.selectors) {
                     try {
                         FormationMatch fm = s.match(ds.currentFormation());
                         return new MetaEvaluator(fm).evaluate(ds);
                     } catch (NoMatchException nme) {
                         /* ignore; try the next selector */
                     }
                 }
                 /* Hmm, none of the selectors matched. */
                 throw new BadCallException("no matching selectors");
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
                 TaggedFormation tf =
                     (f instanceof TaggedFormation) ? (TaggedFormation) f :
                     // hm, dynamically apply tags here?
                     new TaggedFormation(f, Tools.<Dancer,Tag>mml());
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
     /** Implements {@link Seq}: chains multiple evaluators together.
      */
     private static class EvaluatorChain extends Evaluator {
         final Evaluator head;
         final Evaluator next;
         EvaluatorChain(Evaluator head, Evaluator next) {
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
         private final FormationMatch fm;
         MetaEvaluator(FormationMatch fm) {
             this.fm = fm;
         }
         @Override
         public Evaluator evaluate(DanceState ds) {
             // TODO Auto-generated method stub
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
             public SubPart(Set<Dancer> matched, Evaluator eval, DanceState ds){
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
             DanceState nds = ds.cloneAndClear
                 (ds.currentFormation().select(matched));
            this.parts.add(new SubPart(matched, eval, ds));
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
