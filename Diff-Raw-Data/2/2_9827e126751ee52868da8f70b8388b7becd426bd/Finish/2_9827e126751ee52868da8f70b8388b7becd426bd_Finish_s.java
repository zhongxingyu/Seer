 package net.cscott.sdr.calls.transform;
 
 import static net.cscott.sdr.calls.parser.CallFileLexer.APPLY;
 import static net.cscott.sdr.calls.parser.CallFileLexer.PART;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.DanceState;
 import net.cscott.sdr.calls.ExactRotation;
 import net.cscott.sdr.calls.ExprFunc.EvaluationException;
 import net.cscott.sdr.calls.ast.Comp;
 import net.cscott.sdr.calls.ast.Expr;
 import net.cscott.sdr.calls.ast.In;
 import net.cscott.sdr.calls.ast.OptCall;
 import net.cscott.sdr.calls.ast.Part;
 import net.cscott.sdr.calls.ast.Prim;
 import net.cscott.sdr.calls.ast.Seq;
 import net.cscott.sdr.calls.ast.SeqCall;
 import net.cscott.sdr.util.Fraction;
 
 import org.junit.runner.RunWith;
 
 /**
  * Transformation implementing
  * {@link net.cscott.sdr.calls.lists.C1List#FINISH}.
  * Removes the first part of the call.
  * @author C. Scott Ananian
  * @see net.cscott.sdr.calls.lists.C4List#LIKE_A
  * @doc.test
  *  Use Finish class to evaluate FINISH SWING THRU and FINISH FINISH SWING THRU:
  *  js> importPackage(net.cscott.sdr.calls)
  *  js> importPackage(net.cscott.sdr.calls.ast)
  *  js> importPackage(net.cscott.sdr.util)
  *  js> ds = new DanceState(new DanceProgram(Program.C1), Formation.SQUARED_SET); undefined;
  *  js> callname="swing thru"
  *  swing thru
  *  js> call = net.cscott.sdr.calls.CallDB.INSTANCE.lookup(callname)
  *  swing thru[basic]
  *  js> comp = call.getEvaluator(null, java.util.Arrays.asList()).simpleExpansion()
 *  (In '6 (Seq (Apply (Expr _maybe touch (Expr _quarter thru '1/2)))))
  *  js> fst = comp.accept(new Finish(ds), null)
  *  (Seq (Part 'DIVISIBLE '1 (Opt (From 'ANY (Seq (Part 'DIVISIBLE '1 (Seq (Apply (Expr _those who can turn left not grand '1/2)))))))))
  *  js> try {
  *    >   fst.accept(new Finish(ds), null)
  *    > } catch (e) {
  *    >   print(e.javaException)
  *    > }
  *  net.cscott.sdr.calls.BadCallException: Only one part
  */
 @RunWith(value=JDoctestRunner.class)
 public class Finish extends PartsVisitor<Void> {
     public Finish(DanceState ds) {
         this("finish", safeConcepts, ds);
     }
     // for (re)use by LikeA
     protected Finish(String conceptName, Set<String> safeConcepts,
                      DanceState ds) {
         super(conceptName, safeConcepts, ds);
     }
 
     /* (non-Javadoc)
      * @see net.cscott.sdr.calls.transform.TransformVisitor#visit(net.cscott.sdr.calls.ast.In, java.lang.Object)
      */
     @Override
     public Comp visit(In in, Void t) {
         // XXX should we scale duration of call?
         //     this would involve figuring out how many parts are in the call
         //     so that we could then scale by a reasonable number.
         // for now just remove the 'in' and trust the sub parts to have
         // appropriate durations
         return in.child.accept(this, t);
     }
 
     /* (non-Javadoc)
      * @see net.cscott.sdr.calls.transform.TransformVisitor#visit(net.cscott.sdr.calls.ast.Prim, java.lang.Object)
      */
     @Override
     public SeqCall visit(Prim p, Void t) {
         // nothing can be infinitely subdivided
         if (p.x.equals(Fraction.ZERO) &&
             p.y.equals(Fraction.ZERO) &&
             p.rot.equals(ExactRotation.ZERO))
             // XXX: scaleTime like we do for fractional?
             return p;
         throw new BadCallException("Primitives cannot be subdivided");
     }
 
     @Override
     public Part visit(Part p, Void t) {
         Fraction howMany;
         switch (p.divisibility) {
         case DIVISIBLE:
             try {
                 howMany = p.parts().evaluate(Fraction.class, ds);
             } catch (EvaluationException e) {
                 assert false : "bad call definition";
                 throw new BadCallException("Can't evaluate number of parts");
             }
             assert howMany.getProperNumerator()==0 : "non-integral parts?!";
             if (howMany.equals(Fraction.ZERO)) {
                 throw new BadCallException("Can't adjust to starting "+
                                            "formation when doing a finish");
             }
             if (howMany.compareTo(Fraction.TWO) < 0) {
                 throw new BadCallException("Only one part");
             }
             // okay, recurse to remove the first part, then subtract one from
             // the number of parts represented here.
             return p.build(p.divisibility,
                            Expr.literal(howMany.subtract(Fraction.ONE)),
                            p.child.accept(this, t));
         case INDETERMINATE:
             assert false : "should never recurse into seq containing indeterminate part";
         default:
             assert false : "case not handled in divisibility enum";
         case INDIVISIBLE:
             throw new BadCallException("Can't finish indivisible part");
         }
     }
     /* (non-Javadoc)
      * @see net.cscott.sdr.calls.transform.TransformVisitor#visit(net.cscott.sdr.calls.ast.Seq, java.lang.Object)
      */
     @Override
     public Comp visit(Seq s, Void t) {
         boolean singleCall = false;
         s = desugarAnd(s);
         if (s.children.size() == 1 && s.children.get(0).type==APPLY) {
             // fall through, this is a special case.
             // (one call defined as exactly another call gets the parts of
             //  the other call, instead of a single part.  use an explicit
             //  ipart if you actually wanted to define it as a single part)
             singleCall = true;
         } else if (s.children.size() < 2) {
             throw new BadCallException("Only one part");
         }
         // just look at first part, verify that 'howMany' is one
         SeqCall firstCall = s.children.get(0);
         if (firstCall.isIndeterminate()) {
             throw new BadCallException("Number of parts is not well-defined");
         }
         Fraction firstParts;
         try {
             firstParts = firstCall.parts().evaluate(Fraction.class, ds);
         } catch (EvaluationException e) {
             assert false : "bad call definition";
             throw new BadCallException("Can't evaluate number of parts");
         }
         if (firstParts.equals(Fraction.ONE) && !singleCall) {
             // easy case, just drop the first part
             return s.build(s.children.subList(1, s.children.size()));
         } else {
             // harder case: "finish" the first part
             assert firstCall.type == PART || firstCall.type == APPLY;
             SeqCall nFirst = firstCall.accept(this, t);
             List<SeqCall> nChildren = new ArrayList<SeqCall>(s.children);
             nChildren.set(0, nFirst);
             return s.build(nChildren);
         }
     }
     /* Remove starting formation from definition, since we're starting
      * from the middle of the definition.
      * finish(from: FORMATION...) -> from: ANY finish ....
      */
     @Override
     public OptCall visit(OptCall oc, Void t) {
         return oc.build(Expr.literal("ANY"), oc.child.accept(this, t));
     }
 
     /** A list of concepts which it is safe to hoist "finish" through.
     That is, "finish(as couples(swing thru))" == "as couples(finish(swing thru))". */
     static Set<String> safeConcepts = Fractional.safeConcepts;
 }
