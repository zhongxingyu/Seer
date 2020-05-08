 package org.clafer.ir.compiler;
 
 import org.clafer.collection.CacheMap;
 import org.clafer.ir.IrAllDifferent;
 import org.clafer.ir.IrArrayToSet;
 import org.clafer.ir.IrBoolCast;
 import org.clafer.ir.IrElement;
 import org.clafer.ir.IrIfOnlyIf;
 import org.clafer.ir.IrIfThenElse;
 import org.clafer.ir.IrIntExpr;
 import org.clafer.ir.IrBetween;
 import org.clafer.ir.IrMember;
 import org.clafer.ir.IrNotBetween;
 import org.clafer.ir.IrNotImplies;
 import org.clafer.ir.IrNotMember;
 import org.clafer.ir.IrOr;
 import org.clafer.ir.IrSelectN;
 import org.clafer.ir.IrSetExpr;
 import java.util.Deque;
 import java.util.LinkedList;
 import org.clafer.ir.IrNot;
 import org.clafer.ir.IrSetTest;
 import org.clafer.ir.IrSingleton;
 import org.clafer.ir.IrSortInts;
 import org.clafer.ir.IrSortStrings;
 import org.clafer.ir.IrSetUnion;
 import solver.constraints.nary.cnf.ConjunctiveNormalForm;
 import org.clafer.ir.IrAnd;
 import org.clafer.common.Check;
 import org.clafer.choco.constraint.Constraints;
 import org.clafer.collection.Triple;
 import org.clafer.ir.IrAdd;
 import org.clafer.ir.IrBoolChannel;
 import org.clafer.ir.IrIntChannel;
 import org.clafer.ir.IrJoin;
 import org.clafer.ir.IrJoinRef;
 import org.clafer.ir.IrCard;
 import solver.variables.SetVar;
 import solver.constraints.Constraint;
 import org.clafer.ir.IrBoolExpr;
 import org.clafer.ir.IrBoolExprVisitor;
 import org.clafer.ir.IrBoolLiteral;
 import org.clafer.ir.IrBoolVar;
 import org.clafer.ir.IrDomain;
 import org.clafer.ir.IrException;
 import org.clafer.ir.IrImplies;
 import org.clafer.ir.IrCompare;
 import org.clafer.ir.IrDiv;
 import org.clafer.ir.IrIntCast;
 import org.clafer.ir.IrIntExprVisitor;
 import org.clafer.ir.IrIntLiteral;
 import org.clafer.ir.IrIntVar;
 import org.clafer.ir.IrLone;
 import org.clafer.ir.IrMinus;
 import org.clafer.ir.IrModule;
 import org.clafer.ir.IrMul;
 import org.clafer.ir.IrOffset;
 import org.clafer.ir.IrOne;
 import org.clafer.ir.IrSetDifference;
 import org.clafer.ir.IrSetExprVisitor;
 import org.clafer.ir.IrSetIntersection;
 import org.clafer.ir.IrSetLiteral;
 import org.clafer.ir.IrSetSum;
 import org.clafer.ir.IrSetTernary;
 import org.clafer.ir.IrSetVar;
 import org.clafer.ir.IrSub;
 import org.clafer.ir.IrSubsetEq;
 import org.clafer.ir.IrTernary;
 import org.clafer.ir.IrUtil;
 import org.clafer.ir.IrXor;
 import org.clafer.ir.analysis.Canonicalizer;
 import org.clafer.ir.analysis.Optimizer;
 import solver.Solver;
 import solver.constraints.ICF;
 import solver.constraints.LCF;
 import solver.constraints.Operator;
 import solver.constraints.nary.Sum;
 import solver.constraints.nary.cnf.LogOp;
 import solver.constraints.set.SCF;
 import solver.variables.BoolVar;
 import solver.variables.IntVar;
 import solver.variables.VF;
 
 /**
  * Compile from IR to Choco.
  *
  * @author jimmy
  */
 public class IrCompiler {
 
     private final Solver solver;
     private int varNum = 0;
 
     private IrCompiler(Solver solver) {
         this.solver = Check.notNull(solver);
     }
 
     public static IrSolutionMap compile(IrModule in, Solver out) {
         IrCompiler compiler = new IrCompiler(out);
         return compiler.compile(in);
     }
 
     private IrSolutionMap compile(IrModule module) {
         IrModule optModule = Optimizer.optimize(Canonicalizer.canonical(module));
         for (IrBoolVar var : optModule.getBoolVars()) {
             boolVar.get(var);
         }
         for (IrIntVar var : optModule.getIntVars()) {
             intVar.get(var);
         }
         for (IrSetVar var : optModule.getSetVars()) {
             setVar.get(var);
         }
         for (IrBoolExpr constraint : optModule.getConstraints()) {
             solver.post(compileAsConstraint(constraint));
         }
         return new IrSolutionMap(boolVar, intVar, setVar);
     }
 
     private BoolVar numBoolVar(String name) {
         return VF.bool(name + "#" + varNum++, solver);
     }
 
     private IntVar intVar(String name, IrDomain domain) {
         if (domain.getLowBound() == 0 && domain.getHighBound() == 1) {
             return VF.bool(name, solver);
         }
         if (domain.isBounded()) {
             return VF.enumerated(name, domain.getLowBound(), domain.getHighBound(), solver);
         }
         return VF.enumerated(name, domain.getValues(), solver);
     }
 
     private IntVar numIntVar(String name, IrDomain domain) {
         return intVar(name + "#" + varNum++, domain);
     }
 
     private IntVar numIntVar(String name, int low, int high) {
         return VF.enumerated(name + "#" + varNum++, low, high, solver);
     }
 
     private IntVar numIntVar(String name, int[] dom) {
         return VF.enumerated(name + "#" + varNum++, dom, solver);
     }
 
     private SetVar numSetVar(String name, IrDomain env, IrDomain ker) {
         return VF.set(name + "#" + varNum++, env.getValues(), ker.getValues(), solver);
     }
     private final CacheMap<IrBoolVar, BoolVar> boolVar = new CacheMap<IrBoolVar, BoolVar>() {
         @Override
         protected BoolVar cache(IrBoolVar ir) {
             Boolean constant = IrUtil.getConstant(ir);
             if (constant != null) {
                 return constant.booleanValue() ? VF.one(solver) : VF.zero(solver);
             }
             return VF.bool(ir.getName(), solver);
         }
     };
     private final CacheMap<IrIntVar, IntVar> intVar = new CacheMap<IrIntVar, IntVar>() {
         @Override
         protected IntVar cache(IrIntVar ir) {
             Integer constant = IrUtil.getConstant(ir);
             if (constant != null) {
                 switch (constant.intValue()) {
                     case 0:
                         return VF.zero(solver);
                     case 1:
                         return VF.one(solver);
                     default:
                         return VF.fixed(constant, solver);
                 }
             }
             return intVar(ir.getName(), ir.getDomain());
         }
     };
     private final CacheMap<IrSetVar, SetVar> setVar = new CacheMap<IrSetVar, SetVar>() {
         @Override
         protected SetVar cache(IrSetVar a) {
             int[] constant = IrUtil.getConstant(a);
             if (constant != null) {
                 return VF.set(a.toString(), constant, constant, solver);
             }
             IrDomain env = a.getEnv();
             IrDomain ker = a.getKer();
             IrDomain card = a.getCard();
             SetVar set = VF.set(a.getName(), env.getValues(), ker.getValues(), solver);
 
             if (card.getHighBound() < env.size()) {
                 IntVar setCard = setCardVar.get(set);
                 solver.post(_arithm(setCard, "<=", card.getHighBound()));
             }
             if (card.getLowBound() > ker.size()) {
                 IntVar setCard = setCardVar.get(set);
                 solver.post(_arithm(setCard, ">=", card.getLowBound()));
             }
 
             return set;
         }
     };
     private final CacheMap<SetVar, IntVar> setCardVar = new CacheMap<SetVar, IntVar>() {
         @Override
         protected IntVar cache(SetVar a) {
             IntVar card = VF.enumerated("|" + a.getName() + "|", a.getKernelSize(), a.getEnvelopeSize(), solver);
             solver.post(SCF.cardinality(a, card));
             return card;
         }
     };
 
     private BoolVar asBoolVar(Object obj) {
         if (obj instanceof Constraint) {
             return asBoolVar((Constraint) obj);
         }
         return (BoolVar) obj;
     }
 
     private BoolVar asBoolVar(Constraint op) {
         return op.reif();
     }
 
     private BoolVar compileAsBoolVar(IrBoolExpr expr) {
         return asBoolVar(expr.accept(boolExprCompiler, BoolVarNoReify));
     }
 
     private BoolVar[] compileAsBoolVars(IrBoolExpr... expr) {
         BoolVar[] vars = new BoolVar[expr.length];
         for (int i = 0; i < vars.length; i++) {
             vars[i] = compileAsBoolVar(expr[i]);
         }
         return vars;
     }
 
     private IntVar compileAsIntVar(IrBoolExpr expr) {
         if (expr instanceof IrBoolCast) {
             IrBoolCast cast = (IrBoolCast) expr;
             if (!cast.isFlipped()) {
                 return compile(cast.getExpr());
             }
             // TODO: else view?
         }
         return asBoolVar(expr.accept(boolExprCompiler, BoolVarNoReify));
     }
 
     private IntVar[] compileAsIntVars(IrBoolExpr... expr) {
         IntVar[] vars = new IntVar[expr.length];
         for (int i = 0; i < vars.length; i++) {
             vars[i] = compileAsIntVar(expr[i]);
         }
         return vars;
     }
 
     private Constraint asConstraint(Object obj) {
         if (obj instanceof BoolVar) {
             return asConstraint((BoolVar) obj);
         }
         return (Constraint) obj;
     }
 
     private Constraint asConstraint(BoolVar var) {
         return _arithm(var, "=", 1);
     }
 
     private Constraint compileAsConstraint(IrBoolExpr expr) {
         return asConstraint(expr.accept(boolExprCompiler, ConstraintNoReify));
     }
 
     private Constraint compileAsConstraint(IrBoolExpr expr, BoolVar reify) {
         BoolArg arg = new BoolArg(reify, Preference.Constraint);
         Constraint constraint = asConstraint(expr.accept(boolExprCompiler, arg));
         if (arg.hasReify()) {
             // The compliation failed to reify, explicitly reify now.
             return _arithm(arg.useReify(), "=", constraint.reif());
         }
         return constraint;
     }
 
     private Constraint[] compileAsConstraints(IrBoolExpr... expr) {
         Constraint[] constraints = new Constraint[expr.length];
         for (int i = 0; i < constraints.length; i++) {
             constraints[i] = compileAsConstraint(expr[i]);
         }
         return constraints;
     }
 
     private IntVar compile(IrIntExpr expr) {
         return expr.accept(intExprCompiler, null);
     }
 
     private SetVar compile(IrSetExpr expr) {
         return expr.accept(setExprCompiler, null);
     }
 
     private SetVar[] compile(IrSetExpr[] expr) {
         SetVar[] vars = new SetVar[expr.length];
         for (int i = 0; i < vars.length; i++) {
             vars[i] = compile(expr[i]);
         }
         return vars;
     }
     private final IrBoolExprVisitor<BoolArg, Object> boolExprCompiler = new IrBoolExprVisitor<BoolArg, Object>() {
         @Override
         public Object visit(IrBoolLiteral ir, BoolArg a) {
             return boolVar.get(ir.getVar());
         }
 
         @Override
         public Object visit(IrNot ir, BoolArg a) {
             return compileAsBoolVar(ir.getExpr()).not();
         }
 
         @Override
         public Object visit(IrAnd ir, BoolArg a) {
             return _and(compileAsBoolVars(ir.getOperands()));
         }
 
         @Override
         public Object visit(IrLone ir, BoolArg a) {
             return _lone(compileAsBoolVars(ir.getOperands()));
         }
 
         @Override
         public Object visit(IrOne ir, BoolArg a) {
             return _one(compileAsBoolVars(ir.getOperands()));
         }
 
         @Override
         public Object visit(IrOr ir, BoolArg a) {
             return _or(compileAsBoolVars(ir.getOperands()));
         }
 
         @Override
         public Object visit(IrImplies ir, BoolArg a) {
             BoolVar $antecedent = compileAsBoolVar(ir.getAntecedent());
             IntVar $consequent = compileAsIntVar(ir.getConsequent());
             return _implies($antecedent, $consequent);
         }
 
         @Override
         public Object visit(IrNotImplies ir, BoolArg a) {
             BoolVar $antecedent = compileAsBoolVar(ir.getAntecedent());
             IntVar $consequent = compileAsIntVar(ir.getConsequent());
             return _not_implies($antecedent, $consequent);
         }
 
         @Override
         public Object visit(IrIfThenElse ir, BoolArg a) {
             BoolVar $antecedent = compileAsBoolVar(ir.getAntecedent());
             IntVar $consequent = compileAsIntVar(ir.getConsequent());
             IntVar $alternative = compileAsIntVar(ir.getAlternative());
             Constraint thenClause = _implies($antecedent, $consequent);
             Constraint elseClause = _implies($antecedent.not(), $alternative);
             return _and(thenClause.reif(), elseClause.reif());
         }
 
         @Override
         public Object visit(IrIfOnlyIf ir, BoolArg a) {
             if (ir.getLeft() instanceof IrCompare) {
                 BoolVar right = compileAsBoolVar(ir.getRight());
                 return compileAsConstraint(ir.getLeft(), right);
             }
             if (ir.getRight() instanceof IrCompare) {
                 BoolVar left = compileAsBoolVar(ir.getLeft());
                 return compileAsConstraint(ir.getRight(), left);
             }
             IntVar $left = compileAsIntVar(ir.getLeft());
             IntVar $right = compileAsIntVar(ir.getRight());
             return _arithm($left, "=", $right);
         }
 
         @Override
         public Object visit(IrXor ir, BoolArg a) {
             IntVar $left = compileAsIntVar(ir.getLeft());
             IntVar $right = compileAsIntVar(ir.getRight());
             return _arithm($left, "!=", $right);
         }
 
         @Override
         public Object visit(IrBetween ir, BoolArg a) {
             IntVar $var = ir.getVar().accept(intExprCompiler, null);
             return _between($var, ir.getLow(), ir.getHigh());
         }
 
         @Override
         public Object visit(IrNotBetween ir, BoolArg a) {
             IntVar $var = ir.getVar().accept(intExprCompiler, null);
             return _not_between($var, ir.getLow(), ir.getHigh());
         }
 
         @Override
         public Object visit(IrCompare ir, BoolArg a) {
             Object opt = compileCompareConstant(ir.getLeft(), ir.getOp(), ir.getRight(), a);
             if (opt == null) {
                 opt = compileCompareConstant(ir.getRight(), ir.getOp().reverse(), ir.getLeft(), a);
             }
             if (opt != null) {
                 return opt;
             }
             Triple<String, IrIntExpr, Integer> offset = getOffset(ir.getLeft());
             if (offset != null) {
                 return _arithm(compile(ir.getRight()), offset.getFst(),
                         compile(offset.getSnd()), ir.getOp().getSyntax(), offset.getThd().intValue());
             }
             offset = getOffset(ir.getRight());
             if (offset != null) {
                 return _arithm(compile(ir.getLeft()), offset.getFst(),
                         compile(offset.getSnd()), ir.getOp().getSyntax(), offset.getThd().intValue());
             }
             return _arithm(compile(ir.getLeft()), ir.getOp().getSyntax(), compile(ir.getRight()));
         }
 
         /*
          * Optimize when one of the operands is a constant.
          */
         private Object compileCompareConstant(IrIntExpr left, IrCompare.Op op, IrIntExpr right, BoolArg a) {
             boolean preferBoolVar = Preference.BoolVar.equals(a.getPreference());
             Integer constant = IrUtil.getConstant(right);
             if (constant != null) {
                 if (op.isEquality() && (a.hasReify() || preferBoolVar)) {
                     BoolVar reify = a.hasReify() ? a.useReify() : numBoolVar("ReifyEquality");
                     Constraint constraint =
                             IrCompare.Op.Equal.equals(op)
                             ? Constraints.reifyEqual(reify, compile(left), constant.intValue())
                             : Constraints.reifyNotEqual(reify, compile(left), constant.intValue());
                     if (preferBoolVar) {
                         solver.post(constraint);
                         return reify;
                     }
                     return constraint;
                 }
                 return _arithm(compile(left), op.getSyntax(), constant.intValue());
             }
             return null;
         }
 
         private Triple<String, IrIntExpr, Integer> getOffset(IrIntExpr expr) {
             if (expr instanceof IrAdd) {
                 IrAdd add = (IrAdd) expr;
                 IrIntExpr[] addends = add.getAddends();
                 if (addends.length == 2) {
                     Integer constant = IrUtil.getConstant(addends[0]);
                     if (constant != null) {
                         return new Triple<String, IrIntExpr, Integer>("-", addends[1], constant);
                     }
                     constant = IrUtil.getConstant(addends[1]);
                     if (constant != null) {
                         return new Triple<String, IrIntExpr, Integer>("-", addends[0], constant);
                     }
                 }
             } else if (expr instanceof IrSub) {
                 IrSub sub = (IrSub) expr;
                 IrIntExpr[] subtrahends = sub.getSubtrahends();
                 if (subtrahends.length == 2) {
                     Integer constant = IrUtil.getConstant(subtrahends[0]);
                     if (constant != null) {
                         return new Triple<String, IrIntExpr, Integer>("+", subtrahends[1], constant);
                     }
                     constant = IrUtil.getConstant(subtrahends[1]);
                     if (constant != null) {
                         return new Triple<String, IrIntExpr, Integer>("-", subtrahends[0], -constant);
                     }
                 }
             }
             return null;
         }
 
         @Override
         public Object visit(IrSetTest ir, BoolArg a) {
             switch (ir.getOp()) {
                 case Equal:
                     return _equal(compile(ir.getLeft()), compile(ir.getRight()));
                 case NotEqual:
                     return _all_different(compile(ir.getLeft()), compile(ir.getRight()));
                 default:
                     throw new IrException();
             }
         }
 
         @Override
         public Object visit(IrMember ir, BoolArg a) {
             return _member(compile(ir.getElement()), compile(ir.getSet()));
         }
 
         @Override
         public Object visit(IrNotMember ir, BoolArg a) {
             return _not_member(compile(ir.getElement()), compile(ir.getSet()));
         }
 
         @Override
         public Object visit(IrSubsetEq ir, BoolArg a) {
             return _subset_eq(compile(ir.getSubset()), compile(ir.getSuperset()));
         }
 
         @Override
         public Object visit(IrBoolCast ir, BoolArg a) {
             IntVar expr = compile(ir.getExpr());
             BoolVar boolExpr;
             if (expr instanceof BoolVar) {
                 boolExpr = (BoolVar) expr;
             } else {
                 // TODO: View?
                 boolExpr = numBoolVar("BoolCast");
                 solver.post(_arithm(expr, "=", boolExpr));
             }
             return ir.isFlipped() ? boolExpr.not() : boolExpr;
         }
 
         @Override
         public Constraint visit(IrBoolChannel ir, BoolArg a) {
             IrBoolExpr[] bools = ir.getBools();
             IrSetExpr set = ir.getSet();
 
             BoolVar[] $bools = new BoolVar[bools.length];
             for (int i = 0; i < $bools.length; i++) {
                 $bools[i] = compileAsBoolVar(bools[i]);
             }
             SetVar $set = compile(set);
             return SCF.bool_channel($bools, $set, 0);
         }
 
         @Override
         public Constraint visit(IrIntChannel ir, BoolArg a) {
             IrIntExpr[] ints = ir.getInts();
             IrSetExpr[] sets = ir.getSets();
             IntVar[] $ints = new IntVar[ints.length];
             for (int i = 0; i < $ints.length; i++) {
                 $ints[i] = compile(ints[i]);
             }
             SetVar[] $sets = new SetVar[sets.length];
             for (int i = 0; i < $sets.length; i++) {
                 $sets[i] = compile(sets[i]);
             }
             return Constraints.intChannel($sets, $ints);
         }
 
         @Override
         public Constraint visit(IrSortInts ir, BoolArg a) {
             IrIntExpr[] array = ir.getArray();
             IntVar[] $array = new IntVar[array.length];
             for (int i = 0; i < $array.length; i++) {
                 $array[i] = compile(array[i]);
             }
             return Constraints.increasing($array);
         }
 
         @Override
         public Object visit(IrSortStrings ir, BoolArg a) {
             IrIntExpr[][] strings = ir.getStrings();
             IntVar[][] $strings = new IntVar[strings.length][];
             for (int i = 0; i < $strings.length; i++) {
                 $strings[i] = new IntVar[strings[i].length];
                 for (int j = 0; j < $strings[i].length; j++) {
                     $strings[i][j] = compile(strings[i][j]);
                 }
             }
             return _lex_chain_less_eq($strings);
         }
 
         @Override
         public Constraint visit(IrAllDifferent ir, BoolArg a) {
             IrIntExpr[] operands = ir.getOperands();
 
             IntVar[] $operands = new IntVar[operands.length];
             for (int i = 0; i < $operands.length; i++) {
                 $operands[i] = compile(operands[i]);
             }
             return _all_different($operands);
         }
 
         @Override
         public Constraint visit(IrSelectN ir, BoolArg a) {
             IrBoolExpr[] bools = ir.getBools();
             IrIntExpr n = ir.getN();
             BoolVar[] $bools = new BoolVar[bools.length];
             for (int i = 0; i < $bools.length; i++) {
                 $bools[i] = compileAsBoolVar(bools[i]);
             }
             IntVar $n = compile(n);
             return Constraints.selectN($bools, $n);
         }
     };
     private final IrIntExprVisitor<Void, IntVar> intExprCompiler = new IrIntExprVisitor<Void, IntVar>() {
         /**
          * TODO: optimize
          *
          * 5 = x + y
          *
          * sum([x,y], newVar) newVar = 5
          *
          * Instead pass "5" in the Void param so sum([x,y], 5)
          */
         @Override
         public IntVar visit(IrIntLiteral ir, Void a) {
             return intVar.get(ir.getVar());
         }
 
         @Override
         public IntVar visit(IrIntCast ir, Void a) {
             return compileAsBoolVar(ir.getExpr());
         }
 
         @Override
         public IntVar visit(IrMinus ir, Void a) {
             return VF.minus(compile(ir.getExpr()));
         }
 
         @Override
         public IntVar visit(IrCard ir, Void a) {
             return setCardVar.get(ir.getSet().accept(setExprCompiler, a));
         }
 
         @Override
         public IntVar visit(IrAdd ir, Void a) {
             int constants = 0;
             Deque<IntVar> filter = new LinkedList<IntVar>();
             for (IrIntExpr addend : ir.getAddends()) {
                 Integer constant = IrUtil.getConstant(addend);
                 if (constant != null) {
                     constants += constant.intValue();
                 } else {
                     filter.add(compile(addend));
                 }
             }
             IntVar[] addends = filter.toArray(new IntVar[filter.size()]);
             switch (addends.length) {
                 case 0:
                     // This case should have already been optimized earlier.
                     return VF.fixed(constants, solver);
                 case 1:
                     return VF.offset(addends[0], constants);
                 case 2:
                     return VF.offset(_sum(addends[0], addends[1]), constants);
                 default:
                     IntVar sum = numIntVar("Sum", ir.getDomain());
                     solver.post(_sum(sum, addends));
                     return VF.offset(sum, constants);
             }
         }
 
         @Override
         public IntVar visit(IrSub ir, Void a) {
             int constants = 0;
             IrIntExpr[] operands = ir.getSubtrahends();
             Deque<IntVar> filter = new LinkedList<IntVar>();
             for (int i = 1; i < operands.length; i++) {
                 Integer constant = IrUtil.getConstant(operands[i]);
                 if (constant != null) {
                     constants += constant.intValue();
                 } else {
                     filter.add(compile(operands[i]));
                 }
             }
             Integer constant = IrUtil.getConstant(operands[0]);
             int minuend;
             if (constant != null) {
                 minuend = constant - constants;
             } else {
                 minuend = -constants;
             }
             filter.addFirst(compile(operands[0]));
             IntVar[] subtractends = filter.toArray(new IntVar[filter.size()]);
             switch (subtractends.length) {
                 case 0:
                     return VF.fixed(minuend, solver);
                 case 1:
                     return VF.offset(subtractends[0], -constants);
                 case 2:
                     return VF.offset(_sum(subtractends[0],
                             VF.minus(subtractends[1])), -constants);
                 default:
                     IntVar diff = numIntVar("Diff", ir.getDomain());
                     solver.post(_difference(diff, subtractends));
                     return VF.offset(diff, -constants);
             }
         }
 
         @Override
         public IntVar visit(IrMul ir, Void a) {
             IrIntExpr multiplicand = ir.getMultiplicand();
             IrIntExpr multiplier = ir.getMultiplier();
             Integer multiplicandConstant = IrUtil.getConstant(multiplicand);
             Integer multiplierConstant = IrUtil.getConstant(multiplier);
             if (multiplicandConstant != null) {
                 switch (multiplicandConstant.intValue()) {
                     case 0:
                         return compile(multiplicand);
                     case 1:
                         return compile(multiplier);
                     default:
                         if (multiplicandConstant.intValue() >= -1) {
                             return VF.scale(compile(multiplier), multiplicandConstant.intValue());
                         }
                 }
             }
             if (multiplierConstant != null) {
                 switch (multiplierConstant.intValue()) {
                     case 0:
                         return compile(multiplier);
                     case 1:
                         return compile(multiplicand);
                     default:
                         if (multiplierConstant.intValue() >= -1) {
                             return VF.scale(compile(multiplicand), multiplierConstant.intValue());
                         }
                 }
             }
             IntVar product = numIntVar("Mul", ir.getDomain());
             solver.post(_times(compile(multiplicand), compile(multiplier), product));
             return product;
         }
 
         @Override
         public IntVar visit(IrDiv ir, Void a) {
             IrIntExpr dividend = ir.getDividend();
             IrIntExpr divisor = ir.getDivisor();
             IntVar quotient = numIntVar("Div", ir.getDomain());
             solver.post(_div(compile(dividend), compile(divisor), quotient));
             return quotient;
         }
 
         @Override
         public IntVar visit(IrElement ir, Void a) {
             IrIntExpr index = ir.getIndex();
             IrIntExpr[] array = ir.getArray();
 
             IntVar $index = index.accept(intExprCompiler, a);
             IntVar[] $array = new IntVar[array.length];
             for (int i = 0; i < $array.length; i++) {
                 $array[i] = array[i].accept(intExprCompiler, a);
             }
             IntVar element = numIntVar("Element", getLB($array), getUB($array));
             solver.post(_element($index, $array, element));
             return element;
         }
 
         @Override
         public IntVar visit(IrSetSum ir, Void a) {
             IntVar sum = intVar("SetSum", ir.getDomain());
             int n = ir.getSet().getCard().getHighBound();
             solver.post(Constraints.setSumN(compile(ir.getSet()), sum, n));
             return sum;
         }
 
         @Override
         public IntVar visit(IrTernary ir, Void a) {
             BoolVar antecedent = compileAsBoolVar(ir.getAntecedent());
             IntVar ternary = numIntVar("Ternary", ir.getDomain());
             solver.post(_ifThenElse(antecedent,
                     _arithm(ternary, "=", compile(ir.getConsequent())),
                     _arithm(ternary, "=", compile(ir.getAlternative()))));
             return ternary;
         }
     };
     private final IrSetExprVisitor<Void, SetVar> setExprCompiler = new IrSetExprVisitor<Void, SetVar>() {
         @Override
         public SetVar visit(IrSetLiteral ir, Void a) {
             return setVar.get(ir.getVar());
         }
 
         @Override
         public SetVar visit(IrSingleton ir, Void a) {
             IrIntExpr value = ir.getValue();
             IntVar $value = value.accept(intExprCompiler, a);
             SetVar singleton = numSetVar("Singleton", ir.getEnv(), ir.getKer());
             solver.post(Constraints.singleton($value, singleton));
             return singleton;
         }
 
         @Override
         public SetVar visit(IrArrayToSet ir, Void a) {
             IrIntExpr[] array = ir.getArray();
             IntVar[] $array = new IntVar[array.length];
             for (int i = 0; i < $array.length; i++) {
                 $array[i] = array[i].accept(intExprCompiler, a);
             }
             SetVar set = numSetVar("ArrayToSet", ir.getEnv(), ir.getKer());
             solver.post(Constraints.arrayToSet($array, set));
             return set;
         }
 
         @Override
         public SetVar visit(IrJoin ir, Void a) {
             IrSetExpr take = ir.getTake();
             IrSetExpr[] children = ir.getChildren();
             SetVar $take = compile(take);
             SetVar[] $children = new SetVar[children.length];
             for (int i = 0; i < $children.length; i++) {
                 $children[i] = compile(children[i]);
             }
             SetVar join = numSetVar("Join", ir.getEnv(), ir.getKer());
             solver.post(Constraints.join($take, $children, join));
             return join;
         }
 
         @Override
         public SetVar visit(IrJoinRef ir, Void a) {
             IrSetExpr take = ir.getTake();
             IrIntExpr[] refs = ir.getRefs();
             SetVar $take = compile(take);
             IntVar[] $refs = new IntVar[refs.length];
             for (int i = 0; i < $refs.length; i++) {
                 $refs[i] = compile(refs[i]);
             }
             SetVar joinRef = numSetVar("JoinRef", ir.getEnv(), ir.getKer());
             solver.post(Constraints.joinRef($take, $refs, joinRef));
             return joinRef;
         }
 
         @Override
         public SetVar visit(IrSetDifference ir, Void a) {
             SetVar minuend = compile(ir.getMinuend());
             SetVar subtrahend = compile(ir.getSubtrahend());
             SetVar difference = numSetVar("Difference", ir.getEnv(), ir.getKer());
             solver.post(_difference(minuend, subtrahend, difference));
             return difference;
         }
 
         @Override
         public SetVar visit(IrSetIntersection ir, Void a) {
             SetVar[] operands = compile(ir.getOperands());
             SetVar union = numSetVar("Intersection", ir.getEnv(), ir.getKer());
             solver.post(_intersection(operands, union));
             return union;
         }
 
         @Override
         public SetVar visit(IrSetUnion ir, Void a) {
             SetVar[] operands = compile(ir.getOperands());
             SetVar union = numSetVar("Union", ir.getEnv(), ir.getKer());
             solver.post(_union(operands, union));
             return union;
         }
 
         @Override
         public SetVar visit(IrOffset ir, Void a) {
             SetVar set = compile(ir.getSet());
             SetVar offset = numSetVar("Offset", ir.getEnv(), ir.getKer());
             solver.post(_offset(set, offset, ir.getOffset()));
             return offset;
         }
 
         @Override
         public SetVar visit(IrSetTernary ir, Void a) {
             BoolVar antecedent = compileAsBoolVar(ir.getAntecedent());
             SetVar ternary = numSetVar("Ternary", ir.getEnv(), ir.getKer());
             solver.post(_ifThenElse(antecedent,
                     _equal(ternary, compile(ir.getConsequent())),
                     _equal(ternary, compile(ir.getAlternative()))));
             return ternary;
         }
     };
 
     private ConjunctiveNormalForm _clauses(LogOp tree) {
         return ICF.clauses(tree, solver);
     }
 
     private static Constraint _implies(BoolVar b, Constraint c) {
         return LCF.ifThen(b, c);
     }
 
     private static Constraint _ifThenElse(BoolVar b, Constraint c, Constraint d) {
         return LCF.ifThenElse(b, c, d);
     }
 
     private static IntVar _sum(IntVar var1, IntVar var2) {
         return Sum.var(var1, var2);
     }
 
     private static Constraint _difference(IntVar difference, IntVar... vars) {
         int[] coeffiecients = new int[vars.length];
         coeffiecients[0] = 1;
         for (int i = 1; i < coeffiecients.length; i++) {
             coeffiecients[i] = -1;
         }
         return ICF.scalar(vars, coeffiecients, difference);
     }
 
     private static Constraint _sum(IntVar sum, IntVar... vars) {
         return ICF.sum(vars, sum);
     }
 
     private static Constraint _sum(IntVar sum, BoolVar... vars) {
         return ICF.sum(vars, sum);
     }
 
     private static Constraint _times(IntVar multiplicand, IntVar multiplier, IntVar product) {
         return ICF.times(multiplicand, multiplier, product);
     }
 
     private static Constraint _div(IntVar dividend, IntVar divisor, IntVar quotient) {
         return ICF.eucl_div(dividend, divisor, quotient);
     }
 
     private static Constraint _arithm(IntVar var1, String op1, IntVar var2, String op2, int cste) {
         if (cste == 0) {
             switch (Operator.get(op2)) {
                 case PL:
                     return ICF.arithm(var1, op1, var2);
                 case MN:
                     return ICF.arithm(var1, op1, var2);
             }
         }
         return ICF.arithm(var1, op1, var2, op2, cste);
     }
 
     private static Constraint _and(BoolVar... vars) {
         switch (vars.length) {
             case 1:
                 return _arithm(vars[0], "=", 1);
             case 2:
                 return _arithm(vars[0], "+", vars[1], "=", 2);
             default:
                 return Constraints.and(vars);
         }
     }
 
     private static Constraint _lone(BoolVar... vars) {
         switch (vars.length) {
             case 1:
                 return vars[0].getSolver().TRUE;
             case 2:
                return _arithm(vars[0], "+", vars[1], "<=", 1);
             default:
                 return Constraints.lone(vars);
         }
     }
 
     private static Constraint _one(BoolVar... vars) {
         switch (vars.length) {
             case 1:
                 return _arithm(vars[0], "=", 1);
             case 2:
                 return _arithm(vars[0], "+", vars[1], "=", 1);
             default:
                 return Constraints.one(vars);
         }
     }
 
     private static Constraint _or(BoolVar... vars) {
         switch (vars.length) {
             case 1:
                 return _arithm(vars[0], "=", 1);
             case 2:
                 return _arithm(vars[0], "+", vars[1], ">=", 1);
             default:
                 return Constraints.or(vars);
         }
     }
 
     private static Constraint _implies(BoolVar antecedent, IntVar consequent) {
         return _arithm(antecedent, "<=", consequent);
     }
 
     private static Constraint _not_implies(BoolVar antecedent, IntVar consequent) {
         return _arithm(antecedent, ">", consequent);
     }
 
     private static Constraint _arithm(IntVar var1, String op, IntVar var2) {
         if (var2.instantiated()) {
             return ICF.arithm(var1, op, var2.getValue());
         }
         return ICF.arithm(var1, op, var2);
     }
 
     private static Constraint _arithm(IntVar var1, String op, int c) {
         return ICF.arithm(var1, op, c);
     }
 
     private static Constraint _element(IntVar index, IntVar[] array, IntVar value) {
         return ICF.element(value, array, index, 0);
     }
 
     private static Constraint _equal(SetVar var1, SetVar var2) {
         return Constraints.equal(var1, var2);
     }
 
     private static Constraint _all_different(SetVar... vars) {
         return SCF.all_different(vars);
     }
 
     private static Constraint _all_different(IntVar... vars) {
         return ICF.alldifferent(vars, "AC");
     }
 
     private static Constraint _between(IntVar var, int low, int high) {
         return ICF.member(var, low, high);
     }
 
     private static Constraint _not_between(IntVar var, int low, int high) {
         return ICF.not_member(var, low, high);
     }
 
     private static Constraint _member(IntVar element, SetVar set) {
         return SCF.member(element, set);
     }
 
     private static Constraint _not_member(IntVar element, SetVar set) {
         return Constraints.notMember(element, set);
     }
 
     private static Constraint _lex_chain_less_eq(IntVar[]... vars) {
         if (vars.length == 2) {
             return ICF.lex_less_eq(vars[0], vars[1]);
         }
         return ICF.lex_chain_less_eq(vars);
     }
 
     private static Constraint _difference(SetVar minuend, SetVar subtrahend, SetVar difference) {
         return Constraints.difference(minuend, subtrahend, difference);
     }
 
     private static Constraint _intersection(SetVar[] operands, SetVar union) {
         return SCF.intersection(operands, union);
     }
 
     private static Constraint _union(SetVar[] operands, SetVar union) {
         return Constraints.union(operands, union);
     }
 
     private static Constraint _offset(SetVar set, SetVar offseted, int offset) {
         return SCF.offSet(set, offseted, offset);
     }
 
     private static Constraint _subset_eq(SetVar... sets) {
         return SCF.subsetEq(sets);
     }
 
     private int getLB(IntVar... vars) {
         if (vars.length < 0) {
             throw new IllegalArgumentException();
         }
         int lb = vars[0].getLB();
         for (int i = 1; i < vars.length; i++) {
             lb = Math.min(lb, vars[i].getLB());
         }
         return lb;
     }
 
     private int getUB(IntVar... vars) {
         if (vars.length < 0) {
             throw new IllegalArgumentException();
         }
         int ub = vars[0].getUB();
         for (int i = 1; i < vars.length; i++) {
             ub = Math.max(ub, vars[i].getUB());
         }
         return ub;
     }
     private static final BoolArg ConstraintNoReify = new BoolArg(null, Preference.Constraint);
     private static final BoolArg BoolVarNoReify = new BoolArg(null, Preference.BoolVar);
 
     private static class BoolArg {
 
         // The solution needs to be reified in this variable.
         // Set to null if no reification needed.
         private BoolVar reify;
         // The prefered type of solution.
         private final Preference preference;
 
         private BoolArg(BoolVar reify, Preference preference) {
             this.reify = reify;
             this.preference = preference;
         }
 
         private boolean hasReify() {
             return reify != null;
         }
 
         private BoolVar useReify() {
             BoolVar tmp = reify;
             reify = null;
             return tmp;
         }
 
         private Preference getPreference() {
             return preference;
         }
     }
 
     private static enum Preference {
 
         Constraint,
         BoolVar;
     }
 }
