 package net.cscott.sdr.calls.lists;
 
 import static net.cscott.sdr.calls.transform.AstTokenTypes.PART;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.Program;
 import net.cscott.sdr.calls.Warp;
 import net.cscott.sdr.calls.ast.*;
 import net.cscott.sdr.calls.grm.Grm;
 import net.cscott.sdr.calls.grm.Rule;
 import net.cscott.sdr.calls.transform.Fractional;
 import net.cscott.sdr.util.Fraction;
 
 /** 
  * The <code>BasicList</code> class contains complex call
  * and concept definitions which are on the 'basic' program.
  * Note that "simple" calls and concepts are defined in
 * the resource file at <code>net/cscott/sdr/calls/lists/basic.calls</code>;
  * this class contains only those definitions for which an
  * executable component is required.
  * @author C. Scott Ananian
 * @version $Id: BasicList.java,v 1.18 2006-11-14 16:12:39 cananian Exp $
  */
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
     }
     
     // simple combining concept.
     public static final Call AND = new BasicCall("and") {
         @Override
         public Comp apply(Apply ast) {
             assert ast.callName.equals(getName());
             assert ast.args.size()>=1;
             List<SeqCall> l = new ArrayList<SeqCall>(ast.args);
             return new Seq(l);
         }
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             if (true) return null; // we don't support left recursion yet
             // XXX would be nice if this could deal with a sequence of ANDs 
             Grm g = Grm.parse("<0=anything> and <1=anything>");
             return new Rule("anything", g, Fraction.ONE);
         }
     };
     // kludges for simple arithmetic.
     private static abstract class MathCall extends BasicCall {
         MathCall(String name) { super(name); }
         abstract Fraction getIdentity();
         abstract Fraction doOp(Fraction f1, Fraction f2);
         @Override
         public final Comp apply(Apply ast) {
             Fraction result = getIdentity();
             assert ast.args.size()>=1;
             for (int i=0; i<ast.args.size(); i++)
                 result = doOp(result, ast.getNumberArg(i));
             // here's the kludge
             return new Seq(Apply.makeApply(result.toProperString()));
         }
         @Override
         public int getMinNumberOfArguments() { return 1; }
     }
     public static final Call _ADD_NUM = new MathCall("_add_num") {
         @Override
         Fraction getIdentity() { return Fraction.ZERO; }
         @Override
         Fraction doOp(Fraction f1, Fraction f2) { return f1.add(f2); }
     };
     public static final Call _SUBTRACT_NUM = new MathCall("_subtract_num") {
         @Override
         Fraction getIdentity() { return Fraction.ZERO; }
         @Override
         Fraction doOp(Fraction f1, Fraction f2) { return f1.subtract(f2); }
     };
     public static final Call _MULTIPLY_NUM = new MathCall("_multiply_num") {
         @Override
         Fraction getIdentity() { return Fraction.ONE; }
         @Override
         Fraction doOp(Fraction f1, Fraction f2) { return f1.multiply(f2); }
     };
     public static final Call _DIVIDE_NUM = new BasicCall("_divide_num") {
         @Override
         public final Comp apply(Apply ast) {
             assert ast.args.size()==2;
             Fraction result = ast.getNumberArg(0).divide(ast.getNumberArg(1));
             // here's the kludge
             return new Seq(Apply.makeApply(result.toProperString()));
         }
         @Override
         public int getMinNumberOfArguments() { return 2; }
     };
     // XXX: this is not completely accurate: LEFT means 'do each part LEFT'
     // but collisions are still resolved to right hands. (opposed to MIRROR,
     // where collisions are to left hands).
     public static final Call LEFT = new BasicCall("left") {
         @Override
         public Comp apply(Apply ast) {
             assert ast.callName.equals(getName());
             assert ast.args.size()==1;
             Apply a = ast.getArg(0);
             Warp warp = Warp.MIRROR;
             return new Warped(warp, new Seq(a));
         }
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             // XXX would be nice if this could deal with a sequence of ANDs 
             Grm g = Grm.parse("left <0=leftable_anything>");
             return new Rule("anything", g, Fraction.TWO); // bind tight
         }
     };
     // complex concept -- not sure correct program here?
     public static final Call _FRACTIONAL = new BasicCall("_fractional") {
         private Fractional fv = new Fractional(); // visitor singleton
         @Override
         public Comp apply(Apply ast) {
             boolean isDivisible = true;
             assert ast.callName.equals(getName());
             assert ast.args.size()==2;
             Fraction n = ast.getNumberArg(0);
             Apply a = ast.getArg(1);
             if (n.compareTo(Fraction.ZERO) <= 0)
                 throw new BadCallException("0 fractions are not legal");
             int whole = n.getProperWhole();
             List<SeqCall> l = new ArrayList<SeqCall>(whole+1);
             // easy case: do the whole repetitions of the
             // call.
             for (int i=0; i<whole; i++)
                 l.add(new Part(true, new Seq(a)));
             // now add the fraction, if there is one.
             // note this does not get wrapped in a PART:
             // we can't further fractionalize (say)
             // swing thru 1 1/2.
             n=Fraction.valueOf(n.getProperNumerator(),
                     n.getDenominator());
             if (!Fraction.ZERO.equals(n)) {
                 l.add(fv.visit(a, n));
                 if (whole!=0)
                     isDivisible=false;
             }
             Comp result = new Seq(l);
             // OPTIMIZATION: SEQ(PART(c)) = c
             if (l.size()==1 && l.get(0).type==PART) {
                 Part p = (Part) l.get(0);
                 if (p.isDivisible)
                     result = p.child;
             } else if (!isDivisible)
                 // we don't support subdivision of "swing thru 2 1/2"
                 result = new Seq(new Part(isDivisible, result));
             return result;
         }
         @Override
         public int getMinNumberOfArguments() { return 2; }
         @Override
         public Rule getRule() {
             if (true) return null; // we don't support left recursion yet
             Grm g = Grm.parse("<1=anything> <0=cardinal>|do <0=fraction> (of (a)?)? <1=anything>");
             return new Rule("anything", g, Fraction.valueOf(-1)); // bind loosely
         }
     };
 }
