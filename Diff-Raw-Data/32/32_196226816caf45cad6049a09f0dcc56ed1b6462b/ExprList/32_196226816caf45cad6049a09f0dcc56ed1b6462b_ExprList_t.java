 package net.cscott.sdr.calls;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.runner.RunWith;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.ExprFunc.EvaluationException;
 import net.cscott.sdr.calls.ast.Expr;
 import net.cscott.sdr.util.Fraction;
 import net.cscott.sdr.util.Point;
 
 /**
  * The {@link ExprList} contains {@link ExprFunc} definitions and the
  * basic machinery used to evaluate generic {@link Expr}s.
  */
 @RunWith(value=JDoctestRunner.class)
 public class ExprList {
     private ExprList() { assert false; }
 
     /** Map of all the {@link ExprFunc}s defined here. */
     @SuppressWarnings("unchecked")
     private final static Map<String, ExprFunc> exprGenericFuncs =
         new LinkedHashMap<String,ExprFunc>();
     private final static Map<String, ExprFunc<Fraction>> exprMathFuncs =
         new LinkedHashMap<String,ExprFunc<Fraction>>();
     private final static Map<String, ExprFunc<String>> exprStringFuncs =
         new LinkedHashMap<String,ExprFunc<String>>();
 
     /** This method evaluates {@link Expr} nodes. */
     public static <T> T evaluate(String atom, Class<T> type,
                                  DanceState ds, List<Expr> args)
         throws EvaluationException {
         return lookup(atom, type).evaluate(type, ds, args);
     }
     /** This method tells whether an {@link Expr} will result in a constant. */
     public static <T> boolean isConstant(String atom, Class<T> type, List<Expr> args) {
         try {
             return lookup(atom, type).isConstant(type, args);
         } catch (EvaluationException e) {
             assert false : "should never happen";
             throw new RuntimeException(e);
         }
     }
     // namespace mechanism.
     @SuppressWarnings("unchecked") // dispatch mechanism needs crazy casts
     private static final <T> ExprFunc<? extends T> lookup(String atom,
                                                           Class<T> type)
         throws EvaluationException {
         final String atomP = atom.toLowerCase();
         if (exprGenericFuncs.containsKey(atomP))
             return (ExprFunc<? extends T>) exprGenericFuncs.get(atomP);
         if (type.isAssignableFrom(Fraction.class) &&
                 exprMathFuncs.containsKey(atomP))
                 return (ExprFunc<? extends T>) exprMathFuncs.get(atomP);
         if (type.isAssignableFrom(String.class) &&
                 exprStringFuncs.containsKey(atomP))
                 return (ExprFunc<? extends T>) exprStringFuncs.get(atomP);
         if (type.isAssignableFrom(Boolean.class))
             try {
                 return (ExprFunc<? extends T>) (ExprFunc<Boolean>)
                     PredicateList.valueOf(atom);
             } catch (IllegalArgumentException iae) { /* fall through */ }
         if (type.isAssignableFrom(Evaluator.class))
             try {
                 return (ExprFunc<? extends T>) (ExprFunc<Evaluator>)
                     CallDB.INSTANCE.lookup(atom);
             } catch (IllegalArgumentException iae) { /* fall through */ }
         if (type.isAssignableFrom(Selector.class))
             try {
                 return (ExprFunc<? extends T>) (ExprFunc<Selector>)
                     SelectorList.valueOf(atom);
             } catch (IllegalArgumentException iae) { /* fall through */ }
         if (type.isAssignableFrom(Matcher.class))
             return (ExprFunc<? extends T>) MatcherList.valueOf(atom);
         throw new EvaluationException("Couldn't find function "+atom);
     }
     @SuppressWarnings("unchecked")
     public static final ExprFunc LITERAL = new ExprFunc() {
         @Override
         public String getName() { return "literal"; }
         @Override
         public Object evaluate(Class type, DanceState ds, List _args)
             throws EvaluationException {
             List<Expr> args = (List<Expr>) _args;
             if (type.isAssignableFrom(Expr.class))
                 return args.get(0);
             if (args.size() != 1)
                 throw new EvaluationException("Missing argument to LITERAL");
             if (type.isAssignableFrom(String.class))
                 return args.get(0).atom;
             if (type.isAssignableFrom(Fraction.class))
                 return Fraction.valueOf
                     ((String)evaluate(String.class, ds, args));
             if (type.isAssignableFrom(Boolean.class)) {
                 String name = (String) evaluate(String.class, ds, args);
                 if (name.equalsIgnoreCase("true"))
                     return Boolean.TRUE;
                 if (name.equalsIgnoreCase("false"))
                     return Boolean.FALSE;
             }
             if (type.isAssignableFrom(Matcher.class))
                 try {
                     // try to get matcher, otherwise make from formation.
                     return Matcher.valueOf
                         ((String)evaluate(String.class, ds, args));
                 } catch (IllegalArgumentException iae) {
                     return GeneralFormationMatcher.makeMatcher
                         ((TaggedFormation)evaluate(TaggedFormation.class, ds, args));
                 }
             if (type.isAssignableFrom(NamedTaggedFormation.class))
                 return FormationList.valueOf
                     ((String)evaluate(String.class, ds, args));
             if (type.isAssignableFrom(Evaluator.class))
                 // allow 'trade' to be an abbreviation of 'trade()'
                 return args.get(0).evaluate(type, ds);
             if (type.isAssignableFrom(Selector.class))
                 // allow 'beau' to be an abbreviation of 'beau()'
                 return args.get(0).evaluate(type, ds);
             throw new EvaluationException("Can't evaluate LITERAL as "+type);
         }
         /** Literals are constants. */
         @Override
         public boolean isConstant(Class type, List args) {
             return true;
         }
     };
     static { exprGenericFuncs.put(LITERAL.getName(), LITERAL); }
 
     // this is a generic cond function, for implementing conditional loops
     // without Opt/OptCall (which are not tail-recursive)
     @SuppressWarnings("unchecked")
     public static final ExprFunc _IF = new ExprFunc() {
         @Override
         public String getName() { return "_if"; }
         @Override
         public Object evaluate(Class type, DanceState ds, List args)
             throws EvaluationException {
             if (args.size() != 3)
                 throw new EvaluationException("needs 3 arguments");
             Expr condExpr = (Expr) args.get(0);
             Expr trueExpr = (Expr) args.get(1);
             Expr falseExpr = (Expr) args.get(2);
             // evaluate first args as a boolean
             boolean cond = condExpr.evaluate(Boolean.class, ds);
             if (cond)
                 return trueExpr.evaluate(type, ds);
             else
                 return falseExpr.evaluate(type, ds);
         }
     };
     static { exprGenericFuncs.put(_IF.getName(), _IF); }
 
     // mathematics
     private static abstract class MathFunc extends ExprFunc<Fraction> {
         private final String name;
         MathFunc(String name) { this.name = name; }
         @Override
         public String getName() { return name; }
         abstract int minArgs();
         abstract int maxArgs();
         abstract Fraction doOp(Fraction f1, Fraction f2);
         // the result of a math op is a constant if the args are.
         @Override
         public boolean isConstant(Class<? super Fraction> type,List<Expr> args){
             for (Expr e : args)
                 if (!ExprList.isConstant(e.atom, Fraction.class, e.args))
                     return false;
             return true;
         }
         @Override
         public Fraction evaluate(Class<? super Fraction> type,
                                  DanceState ds, List<Expr> args)
                 throws EvaluationException {
             if (type.isAssignableFrom(Fraction.class)) {
                 if (args.size() < minArgs())
                     throw new EvaluationException("Not enough arguments");
                 if (args.size() > maxArgs())
                     throw new EvaluationException("Too many arguments");
                 Fraction result = null;
                 for (Expr e : args) {
                     Fraction f = e.evaluate(Fraction.class, ds);
                     result = (result==null) ? f : doOp(result, f);
                 }
                 return result;
             }
             // should we allow conversions to string, boolean, etc?
             throw new EvaluationException("Type mismatch for math call");
         }
     }
     /**
      * Simple math: addition.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> fc = java.lang.Class.forName('net.cscott.sdr.util.Fraction'); undefined
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _add num '2 '1)")
      *  (Expr _add num '2 '1)
      *  js> c.evaluate(fc, ds).toProperString()
      *  3
      */
     public static final ExprFunc<Fraction> _ADD_NUM = new MathFunc("_add num") {
         @Override
         Fraction doOp(Fraction f1, Fraction f2) { return f1.add(f2); }
         @Override
         public int maxArgs() { return Integer.MAX_VALUE; }
         @Override
         public int minArgs() { return 1; }
     };
     static { exprMathFuncs.put(_ADD_NUM.getName(), _ADD_NUM); }
     /**
      * Simple math: multiplication.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> fc = java.lang.Class.forName('net.cscott.sdr.util.Fraction'); undefined
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _multiply num '3 '2)")
      *  (Expr _multiply num '3 '2)
      *  js> c.evaluate(fc, ds).toProperString()
      *  6
      */
     public static final ExprFunc<Fraction> _MULTIPLY_NUM = new MathFunc("_multiply num") {
         @Override
         Fraction doOp(Fraction f1, Fraction f2) { return f1.multiply(f2); }
         @Override
         public int maxArgs() { return Integer.MAX_VALUE; }
         @Override
         public int minArgs() { return 1; }
     };
     static { exprMathFuncs.put(_MULTIPLY_NUM.getName(), _MULTIPLY_NUM); }
     /**
      * Simple math: subtraction.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> fc = java.lang.Class.forName('net.cscott.sdr.util.Fraction'); undefined
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _subtract num '3 '2)")
      *  (Expr _subtract num '3 '2)
      *  js> c.evaluate(fc, ds).toProperString()
      *  1
      */
     public static final ExprFunc<Fraction> _SUBTRACT_NUM = new MathFunc("_subtract num") {
         @Override
         Fraction doOp(Fraction f1, Fraction f2) { return f1.subtract(f2); }
         @Override
         public int maxArgs() { return 2; }
         @Override
         public int minArgs() { return 2; }
     };
     static { exprMathFuncs.put(_SUBTRACT_NUM.getName(), _SUBTRACT_NUM); }
     /**
      * Simple math: division.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> fc = java.lang.Class.forName('net.cscott.sdr.util.Fraction'); undefined
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _divide num '3 '2)")
      *  (Expr _divide num '3 '2)
      *  js> c.evaluate(fc, ds).toProperString()
      *  1 1/2
      */
     public static final ExprFunc<Fraction> _DIVIDE_NUM = new MathFunc("_divide num") {
         @Override
         Fraction doOp(Fraction f1, Fraction f2) { return f1.divide(f2); }
         @Override
         public int maxArgs() { return 2; }
         @Override
         public int minArgs() { return 2; }
     };
     static { exprMathFuncs.put(_DIVIDE_NUM.getName(), _DIVIDE_NUM); }
     /**
      * Simple math: modulo.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> fc = java.lang.Class.forName('net.cscott.sdr.util.Fraction'); undefined
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _mod num '3/4 '1/2)")
      *  (Expr _mod num '3/4 '1/2)
      *  js> c.evaluate(fc, ds).toProperString()
      *  1/4
      */
     public static final ExprFunc<Fraction> _MOD_NUM = new MathFunc("_mod num") {
         @Override
         Fraction doOp(Fraction f1, Fraction f2) {
             Fraction f = f1.divide(f2);
             return f.subtract(Fraction.valueOf(f.floor())).multiply(f2);
         }
         @Override
         public int maxArgs() { return 2; }
         @Override
         public int minArgs() { return 2; }
     };
     static { exprMathFuncs.put(_MOD_NUM.getName(), _MOD_NUM); }
 
     /**
      * Simple math: return integer part of a fraction.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> fc = java.lang.Class.forName('net.cscott.sdr.util.Fraction'); undefined
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _floor '3 3/4)")
      *  (Expr _floor '3 3/4)
      *  js> c.evaluate(fc, ds).toProperString()
      *  3
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _floor (Expr _subtract num '0 '3 3/4))")
      *  (Expr _floor (Expr _subtract num '0 '3 3/4))
      *  js> c.evaluate(fc, ds).toProperString()
      *  -4
      */
     public static final ExprFunc<Fraction> _FLOOR = new ExprFunc<Fraction>() {
         @Override
         public String getName() { return "_floor"; }
         @Override
         public Fraction evaluate(Class<? super Fraction> type,
                                  DanceState ds, List<Expr> args)
                 throws EvaluationException {
             if (!type.isAssignableFrom(Fraction.class))
                 throw new EvaluationException("Type mismatch");
             if (args.size() != 1)
                 throw new EvaluationException("Wrong # of arguments");
             Fraction f = args.get(0).evaluate(Fraction.class, ds);
             return Fraction.valueOf(f.floor());
         }
         @Override
         public boolean isConstant(Class<? super Fraction> type, List<Expr> args){
             return args.get(0).isConstant(Fraction.class);
         }
     };
     static { exprMathFuncs.put(_FLOOR.getName(), _FLOOR); }
 
     /**
      * Simple math: return fractional part of a number.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> fc = java.lang.Class.forName('net.cscott.sdr.util.Fraction'); undefined
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _fraction '3 3/4)")
      *  (Expr _fraction '3 3/4)
      *  js> c.evaluate(fc, ds).toProperString()
      *  3/4
      *  js> // note that (floor(n) + fraction(n)) = n
      *  js> c=net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _fraction (Expr _subtract num '0 '3 3/4))")
      *  (Expr _fraction (Expr _subtract num '0 '3 3/4))
      *  js> c.evaluate(fc, ds).toProperString()
      *  1/4
      */
     public static final ExprFunc<Fraction> _FRACTION = new ExprFunc<Fraction>() {
         @Override
         public String getName() { return "_fraction"; }
         @Override
         public Fraction evaluate(Class<? super Fraction> type,
                                  DanceState ds, List<Expr> args)
                 throws EvaluationException {
             if (!type.isAssignableFrom(Fraction.class))
                 throw new EvaluationException("Type mismatch");
             if (args.size() != 1)
                 throw new EvaluationException("Wrong # of arguments");
             Fraction f = args.get(0).evaluate(Fraction.class, ds);
             return f.subtract(Fraction.valueOf(f.floor()));
         }
         @Override
         public boolean isConstant(Class<? super Fraction> type, List<Expr> args){
             return args.get(0).isConstant(Fraction.class);
         }
     };
     static { exprMathFuncs.put(_FRACTION.getName(), _FRACTION); }
 
     // square-dance related functions
     public static final ExprFunc<Fraction> NUM_DANCERS = new ExprFunc<Fraction>() {
         @Override
         public String getName() { return "num dancers"; }
         @Override
         public Fraction evaluate(Class<? super Fraction> type, DanceState ds,
                 List<Expr> args) throws EvaluationException {
             if (args.size()>1)
                 throw new EvaluationException("too many arguments");
             Selector s = (args.isEmpty() ? Expr.literal("all") : args.get(0))
                 .evaluate(Selector.class, ds);
             Set<Dancer> d =
                 s.select(TaggedFormation.coerce(ds.currentFormation()));
             return Fraction.valueOf(d.size());
         }
     };
     static { exprMathFuncs.put(NUM_DANCERS.getName(), NUM_DANCERS); }
 
     private static abstract class PatternFunc<T> extends ExprFunc<String> {
         private final String name;
         PatternFunc(String name) { this.name = name; }
         @Override
         public String getName() { return name; }
         @Override
         public String evaluate(Class<? super String> type, DanceState ds,
                                List<Expr> args) throws EvaluationException {
             T closure = parseArgs(ds, args);
             TaggedFormation tf = TaggedFormation.coerce(ds.currentFormation());
             StringBuilder sb = new StringBuilder();
             for (Dancer d : tf.sortedDancers())
                 sb.append(dancerToString(tf, d, closure));
             return sb.toString();
         }
         protected abstract String dancerToString(TaggedFormation tf, Dancer d,
                                                  T closure);
         protected T parseArgs(DanceState ds, List<Expr> args)
             throws EvaluationException { return null; }
     }
     private static abstract class SubsetPatternFunc extends PatternFunc<Set<Dancer>> {
         SubsetPatternFunc(String name) { super(name); }
         @Override
         protected Set<Dancer> parseArgs(DanceState ds, List<Expr> args)
             throws EvaluationException {
             Selector selector =
                 SelectorList.AND.evaluate(Selector.class, ds, args);
             TaggedFormation tf = TaggedFormation.coerce(ds.currentFormation());
             return selector.select(tf);
         }
         @Override
         protected String dancerToString(TaggedFormation tf, Dancer d, Set<Dancer> who) {
             if (!who.contains(d)) return "";
             return ""+dancerToString(tf, d);
         }
         protected abstract String dancerToString(TaggedFormation tf, Dancer d);
     }
     public static final ExprFunc<String> _ROLL_PATTERN =
         new SubsetPatternFunc("_roll pattern") {
         @Override
         protected String dancerToString(TaggedFormation tf, Dancer d) {
             Position p = tf.location(d);
             if (p.flags.contains(Position.Flag.ROLL_LEFT))
                 return "L";
             if (p.flags.contains(Position.Flag.ROLL_RIGHT))
                 return "R";
             return "_";
         }
     };
     static { exprStringFuncs.put(_ROLL_PATTERN.getName(), _ROLL_PATTERN); }
 
     public static final ExprFunc<String> _SWEEP_PATTERN =
         new SubsetPatternFunc("_sweep pattern") {
         @Override
         protected String dancerToString(TaggedFormation tf, Dancer d) {
             Position p = tf.location(d);
             if (p.flags.contains(Position.Flag.SWEEP_LEFT))
                 return "L";
             if (p.flags.contains(Position.Flag.SWEEP_RIGHT))
                 return "R";
             return "_";
         }
     };
     static { exprStringFuncs.put(_SWEEP_PATTERN.getName(), _SWEEP_PATTERN); }
 
     public static final ExprFunc<String> _FACING_PATTERN =
         new SubsetPatternFunc("_facing pattern") {
         @Override
         protected String dancerToString(TaggedFormation tf, Dancer d) {
             Position p = tf.location(d);
             return ""+p.facing.toDiagramChar();
         }
     };
     static { exprStringFuncs.put(_FACING_PATTERN.getName(), _FACING_PATTERN); }
 
     /**
      * The {@link #_COUPLE_NUM_PATTERN} is mostly for determining when we're
      * "at home".
      * @doc.test Dancers in pattern are in "reading order":
      *  js> let expr = net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _COUPLE NUM PATTERN)");
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), Formation.SQUARED_SET); undefined
      *  js> sc = java.lang.Class.forName("java.lang.String")
      *  class java.lang.String
      *  js> expr.evaluate(sc, ds)
      *  33424211
      */
     public static final ExprFunc<String> _COUPLE_NUM_PATTERN =
         new SubsetPatternFunc("_couple num pattern") {
         @Override
         protected String dancerToString(TaggedFormation tf, Dancer d) {
             if (!(d instanceof StandardDancer))
                 return " ";
             return ""+((StandardDancer)d).coupleNumber();
         }
     };
     static { exprStringFuncs.put(_COUPLE_NUM_PATTERN.getName(), _COUPLE_NUM_PATTERN); }
 
     /**
      * Check whether dancers are facing "in" or "out" of the center of the
      * formation.  Dancers facing south or west in the top-right quadrant
      * (positive x and y) are facing "in", dancers facing north or east in
      * the top quadrant are facing "out".  Where in/out direction can not be
      * determined, this function uses an "x".
      * @doc.test
      *  js> FormationList = FormationList.js(this); undefined;
      *  js> SD = StandardDancer; undefined
      *  js> e = net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _INOUT PATTERN)");
      *  (Expr _INOUT PATTERN)
      *  js> f=FormationList.PARALLEL_RH_WAVES ; f.toStringDiagram()
      *  ^    v    ^    v
      *
      *  ^    v    ^    v
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
      *  js> e.evaluate(java.lang.Class.forName("java.lang.String"), ds);
      *  oioiioio
      *  js> f = FormationList.TRADE_BY; f.toStringDiagram()
      *  ^    ^
      *
      *  v    v
      *
      *  ^    ^
      *
      *  v    v
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
      *  js> e.evaluate(java.lang.Class.forName("java.lang.String"), ds);
      *  ooiiiioo
      *  js> f = Formation.SQUARED_SET ; f.toStringDiagram()
      *       3Gv  3Bv
      *
      *  4B>            2G<
      *
      *  4G>            2B<
      *
      *       1B^  1G^
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
      *  js> e.evaluate(java.lang.Class.forName("java.lang.String"), ds);
      *  iiiiiiii
      *  js> f = Formation.SQUARED_SET.rotate(ExactRotation.ONE_EIGHTH) ; f.toStringDiagram()
      *       4BQ       3GL
      *  
      *  4GQ                 3BL
      *  
      *  
      *  
      *  1B7                 2G`
      *  
      *       1G7       2B`
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
      *  js> e.evaluate(java.lang.Class.forName("java.lang.String"), ds);
      *  iiiiiiii
     *  js> f = FormationList.SINGLE_STATIC_SQUARE ; f.toStringDiagram("|")
     *  |     v
     *  |
     *  |>         <
     *  |
     *  |     ^
     *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
     *  js> e.evaluate(java.lang.Class.forName("java.lang.String"), ds);
     *  iiii
     *  js> f = FormationList.THAR ; f.toStringDiagram("|")
     *  |       <
     *  |
     *  |       >
     *  |v    ^    v    ^
     *  |       <
     *  |
     *  |       >
     *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
     *  js> e.evaluate(java.lang.Class.forName("java.lang.String"), ds);
     *  xxxxxxxx
      */
     public static final ExprFunc<String> _INOUT_PATTERN =
         new SubsetPatternFunc("_inout pattern") {
         @Override
         protected String dancerToString(TaggedFormation tf, Dancer d) {
             return ""+inOut(tf.location(d));
         }
         private char inOut(Position p) {
             if (!p.facing.isExact()) return 'x';
             // normalize to positive quadrant
             ExactRotation facing = (ExactRotation) p.facing;
             Point location = new Point(p.x, p.y);
             if (location.x.compareTo(Fraction.ZERO) < 0) {
                 location = new Point(location.x.negate(), location.y);
                 facing = facing.negate();
             }
             if (location.y.compareTo(Fraction.ZERO) < 0) {
                 location = new Point(location.x, location.y.negate());
                 // -(f+1/4)-1/4 = -f-1/2
                 facing = facing.negate().subtract(Fraction.ONE_HALF);
             }
             // okay, now we're in positive quadrant.  Here: s/w is 'in' and
             // n/e is 'out'.  (We handle the axes specially.)
             if (location.x.compareTo(Fraction.ZERO) == 0) {
                 if (location.y.compareTo(Fraction.ZERO) == 0)
                     return 'x'; // origin doesn't have in/out
                 Fraction n = facing.minSweep(ExactRotation.NORTH).abs();
                 Fraction s = facing.minSweep(ExactRotation.SOUTH).abs();
                if (n.compareTo(Fraction.ONE_QUARTER) < 0)
                     return 'o';
                if (s.compareTo(Fraction.ONE_QUARTER) < 0)
                     return 'i';
                 return 'x';
             }
             if (location.y.compareTo(Fraction.ZERO) == 0) {
                 Fraction e = facing.minSweep(ExactRotation.EAST).abs();
                 Fraction w = facing.minSweep(ExactRotation.WEST).abs();
                if (w.compareTo(Fraction.ONE_QUARTER) < 0)
                     return 'i';
                if (e.compareTo(Fraction.ONE_QUARTER) < 0)
                     return 'o';
                 return 'x';
             }
             facing = facing.normalize();
             assert facing.amount.compareTo(Fraction.ZERO) >= 0;
             assert facing.amount.compareTo(Fraction.ONE) < 0;
             if (facing.amount.compareTo(Fraction.ONE_QUARTER) <= 0)
                 return 'o';
             if (facing.amount.compareTo(Fraction.ONE_HALF) >= 0 &&
                 facing.amount.compareTo(Fraction.THREE_QUARTERS) <= 0)
                 return 'i';
             return 'x';
         }
     };
     static { exprStringFuncs.put(_INOUT_PATTERN.getName(), _INOUT_PATTERN); }
 
     /**
      * Check the order of the selected dancers within the given formation.
      * @doc.test Dancers in pattern are in "reading order":
      *  js> let expr = net.cscott.sdr.calls.ast.AstNode.valueOf(
      *    >           "(Expr _SELECTION PATTERN 'BOY)");
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), Formation.SQUARED_SET); undefined
      *  js> sc = java.lang.Class.forName("java.lang.String")
      *  class java.lang.String
      *  js> expr.evaluate(sc, ds)
      *  _XX__XX_
      * @doc.test Examples using the MATCH predicate:
      *  js> FormationList = FormationList.js(this); undefined;
      *  js> SD = StandardDancer; undefined
      *  js> f = FormationList.RH_OCEAN_WAVE; f.toStringDiagram()
      *  ^    v    ^    v
      *  js> // label those dancers
      *  js> f= f.mapStd([SD.COUPLE_1_BOY, SD.COUPLE_1_GIRL,
      *    >              SD.COUPLE_3_BOY, SD.COUPLE_3_GIRL]); f.toStringDiagram()
      *  1B^  1Gv  3B^  3Gv
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
      *  js> function test(sel, pat) {
      *    >   let c = net.cscott.sdr.calls.ast.AstNode.valueOf(
      *    >           "(Expr MATCH (Expr _SELECTION PATTERN '"+sel+") '\""+pat+"\")");
      *    >    return PredicateList.valueOf(c.atom).evaluate(ds, c.args)
      *    > }
      *  js> test('BOY', '____')
      *  false
      *  js> test('BOY', 'x_x_')
      *  true
      *  js> test('BOY', '_x_x')
      *  false
      *  js> test('CENTER', '_xx_')
      *  true
      *  js> test('HEAD', 'xxxx')
      *  true
      *  js> test('SIDE', '____')
      *  true
      *  js> test('COUPLE 1', 'xx__')
      *  true
      *  js> test('SIDE', '_xx_')
      *  false
      *  js> // wildcards
      *  js> test('BOY', 'x...')
      *  true
      */
     public static final ExprFunc<String> _SELECTION_PATTERN =
         new PatternFunc<Set<Dancer>>("_selection pattern") {
         protected Set<Dancer> parseArgs(DanceState ds, List<Expr> args)
             throws EvaluationException {
             Selector selector =
                 SelectorList.OR.evaluate(Selector.class, ds, args);
             TaggedFormation tf = TaggedFormation.coerce(ds.currentFormation());
             return selector.select(tf);
         }
         @Override
         protected String dancerToString(TaggedFormation tf, Dancer d,
                                         Set<Dancer> selected) {
             return selected.contains(d) ? "X" : "_";
         }
     };
     static { exprStringFuncs.put(_SELECTION_PATTERN.getName(), _SELECTION_PATTERN); }
 
     /**
      * The {@link #_PROPERTY} function interrogates the dance engine's
      * environment, as exposed by {@link DanceState#property(String, String)}.
      * @doc.test
      *  js> let expr = net.cscott.sdr.calls.ast.AstNode.valueOf("(Expr _PROPERTY 'foo 'bar)");
      *  js> sc = java.lang.Class.forName("java.lang.String"); undefined
      *  js> // default dance state contains no property mappings
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), Formation.SQUARED_SET); undefined
      *  js> expr.evaluate(sc, ds)
      *  bar
      *  js> // construct a simple property map
      *  js> m = new java.util.HashMap(); m.put('foo', 'bat'); m.toString();
      *  {foo=bat}
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), Formation.SQUARED_SET, m); undefined
      *  js> expr.evaluate(sc, ds)
      *  bat
      */
     public static final ExprFunc<String> _PROPERTY = new ExprFunc<String>() {
         @Override
         public String getName() { return "_property"; }
         @Override
         public String evaluate(Class<? super String> type, DanceState ds,
                                List<Expr> args) throws EvaluationException {
             if (args.size() < 1)
                 throw new EvaluationException("not enough arguments");
             if (args.size() > 2)
                 throw new EvaluationException("too many arguments");
             String propName = args.get(0).evaluate(String.class, ds);
             String defaultValue = (args.size() < 2) ? "" :
                     args.get(1).evaluate(String.class, ds);
             return ds.property(propName, defaultValue);
         }
     };
     static { exprStringFuncs.put(_PROPERTY.getName(), _PROPERTY); }
 }
