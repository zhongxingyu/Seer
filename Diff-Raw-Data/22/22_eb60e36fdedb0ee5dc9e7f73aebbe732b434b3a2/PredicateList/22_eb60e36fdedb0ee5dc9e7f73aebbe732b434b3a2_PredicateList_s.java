 package net.cscott.sdr.calls;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.runner.RunWith;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import net.cscott.sdr.calls.ast.Condition;
 import net.cscott.sdr.calls.ast.ParCall;
 import net.cscott.sdr.util.Fraction;
 
 /** This class contains all the predicates known to the system. */
 @RunWith(value=JDoctestRunner.class)
 public abstract class PredicateList {
     
     // zero-arg operators
     /**
      * Always true.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition true)');
      *  (Condition true)
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      */
     public final static Predicate TRUE = new _Predicate("true") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.isEmpty();
             return true;
         }        
     };
     /**
      * Always false.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition false)');
      *  (Condition false)
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate FALSE = new _Predicate("false") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.isEmpty();
             return false;
         }
     };
     // one-arg operators
     /**
      * Boolean negation.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition not (Condition false))');
      *  (Condition not (Condition false))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition not (Condition true))');
      *  (Condition not (Condition true))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate NOT = new _Predicate("not") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==1;
             Condition arg = c.getArg(0);
             return !arg.getPredicate().evaluate(ds,f,arg);
         }
     };
     // string/numeric literals
     public final static Predicate LITERAL = new _Predicate("literal") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             throw new IllegalArgumentException("type mismatch");
         }
         @Override
         public String evaluateAsString(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==1;
             Condition arg = c.getArg(0);
             assert arg.args.size()==0;
             return arg.predicate;
         }
     };
     // binary numerical operators
     /**
      * Numerical equality.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition equal (Condition literal (Condition "1 1/2")) (Condition literal (Condition "1 1/2")))');
      *  (Condition equal (Condition literal (Condition 1 1/2)) (Condition literal (Condition 1 1/2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition equal (Condition literal (Condition "1 1/2")) (Condition literal (Condition 2)))');
      *  (Condition equal (Condition literal (Condition 1 1/2)) (Condition literal (Condition 2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate EQUAL = new _Predicate("equal") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==2;
             Fraction f1 = c.getNumberArg(0, ds, f);
             Fraction f2 = c.getNumberArg(1, ds, f);
             return f1.equals(f2);
         }
     };
     /**
      * Numerical comparison.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition greater (Condition literal (Condition "1 1/2")) (Condition literal (Condition "1 1/2")))');
      *  (Condition greater (Condition literal (Condition 1 1/2)) (Condition literal (Condition 1 1/2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition greater (Condition literal (Condition 2)) (Condition literal (Condition "1 1/2")))');
      *  (Condition greater (Condition literal (Condition 2)) (Condition literal (Condition 1 1/2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      */
     public final static Predicate GREATER = new _Predicate("greater") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==2;
             Fraction f1 = c.getNumberArg(0, ds, f);
             Fraction f2 = c.getNumberArg(1, ds, f);
             return f1.compareTo(f2) > 0;
         }
     };
     // n-ary operators.
     /**
      * Short-circuit boolean conjunction.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition and (Condition true) (Condition true) (Condition false))');
      *  (Condition and (Condition true) (Condition true) (Condition false))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition and (Condition true) (Condition true) (Condition true))');
      *  (Condition and (Condition true) (Condition true) (Condition true))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> // short-circuits
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition and (Condition false) (Condition bogus))');
      *  (Condition and (Condition false) (Condition bogus))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate AND = new _Predicate("and") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()>0;
             boolean result = true;
             for (Condition cc : c.args) {
                 result = cc.getPredicate().evaluate(ds, f, cc);
                 if (!result) break; // short-circuit operator.
             }
             return result;
         }
     };
     /**
      * Short-circuit boolean disjunction.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition or (Condition false) (Condition false) (Condition false))');
      *  (Condition or (Condition false) (Condition false) (Condition false))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition or (Condition false) (Condition false) (Condition true))');
      *  (Condition or (Condition false) (Condition false) (Condition true))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> // short-circuits
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition or (Condition true) (Condition bogus))');
      *  (Condition or (Condition true) (Condition bogus))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      */
     public final static Predicate OR = new _Predicate("or") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()>0;
             boolean result = false;
             for (Condition cc : c.args) {
                 result = cc.getPredicate().evaluate(ds, f, cc);
                 if (result) break; // short-circuit operator.
             }
             return result;
         }
     };
     // okay, square-dance-specific operators.
     /**
      * Check the current dance program level.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition program at least (Condition literal (Condition BASIC)))');
      *  (Condition program at least (Condition literal (Condition BASIC)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition program at least (Condition literal (Condition A2)))');
      *  (Condition program at least (Condition literal (Condition A2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate PROGRAM_AT_LEAST = new _Predicate("program at least") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==1;
             Program p = Program.valueOf(c.getStringArg(0, ds, f).toUpperCase());
             return ds.getProgram().includes(p);
         }
     };
     public final static Predicate SELECTED_ARE = new _Predicate("selected are") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             List<String> args = new ArrayList<String>(c.args.size());
             for (int i=0; i<c.args.size(); i++)
                 args.add(c.getStringArg(i, ds, f));
             Set<Tag> tags = ParCall.parseTags(args);
             // each selected dancer must have all of these tags
             TaggedFormation tf = TaggedFormation.coerce(f);
             for (Dancer d: f.selectedDancers())
                 for (Tag t : tags)
                     if (!tf.isTagged(d, t))
                         return false;
             return true;
         }
     };
 
     // helper class ////////////////////////////////////
     private static abstract class _Predicate extends Predicate {
         private final String name;
         _Predicate(String name) { this.name = name; }
         public String getName() { return name; }
     }
 }
