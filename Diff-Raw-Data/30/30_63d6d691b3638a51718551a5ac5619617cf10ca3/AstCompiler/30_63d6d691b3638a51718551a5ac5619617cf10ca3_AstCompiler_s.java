 package org.clafer.ast.compiler;
 
 import org.clafer.ast.AstUtil;
 import org.clafer.ast.AstConstraint;
 import java.util.Set;
 import org.clafer.ast.AstSetTest;
 import org.clafer.ast.AstExpr;
 import org.clafer.ast.AstGlobal;
 import org.clafer.ast.AstSetExpr;
 import org.clafer.ast.AstCard;
 import org.clafer.ast.AstCompare;
 import org.clafer.ast.AstConstant;
 import org.clafer.ast.AstJoin;
 import org.clafer.ast.AstJoinParent;
 import org.clafer.ast.AstJoinRef;
 import org.clafer.ast.AstLocal;
 import org.clafer.ast.AstQuantify;
 import org.clafer.ast.AstThis;
 import org.clafer.ast.AstUpcast;
 import org.clafer.ir.IrExpr;
 import org.clafer.collection.ReadWriteHashMap;
 import org.clafer.ir.IrIntVar;
 import org.clafer.ir.IrSetExpr;
 import org.clafer.common.Util;
 import org.clafer.ir.IrBoolVar;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.clafer.common.Check;
 import org.clafer.scope.Scope;
 import org.clafer.ast.analysis.Analysis;
 import org.clafer.ast.analysis.Format;
 import org.clafer.ast.analysis.PartialSolution;
 import org.clafer.ast.AstAbstractClafer;
 import org.clafer.ast.AstArithm;
 import org.clafer.ast.AstBoolArithm;
 import org.clafer.ast.AstBoolExpr;
 import org.clafer.ast.AstClafer;
 import org.clafer.ast.AstConcreteClafer;
 import org.clafer.ast.AstDecl;
 import org.clafer.ast.AstException;
 import org.clafer.ast.AstExprVisitor;
 import org.clafer.ast.AstIfThenElse;
 import org.clafer.ast.AstIntClafer;
 import org.clafer.ast.AstMembership;
 import org.clafer.ast.AstMinus;
 import org.clafer.ast.AstModel;
 import org.clafer.ast.AstNot;
 import org.clafer.ast.AstQuantify.Quantifier;
 import org.clafer.ast.AstRef;
 import org.clafer.ast.AstSetArithm;
 import org.clafer.ast.AstTernary;
 import org.clafer.ast.Card;
 import org.clafer.collection.Pair;
 import org.clafer.collection.Triple;
 import org.clafer.graph.KeyGraph;
 import org.clafer.graph.Vertex;
 import org.clafer.graph.TopologicalSort;
 import org.clafer.ir.IrBoolExpr;
 import org.clafer.ir.IrDomain;
 import org.clafer.ir.IrIntExpr;
 import org.clafer.ir.IrModule;
 import org.clafer.ir.IrSetVar;
 import static org.clafer.ir.Irs.*;
 
 /**
  * Compile from AST -> IR
  *
  * @author jimmy
  */
 public class AstCompiler {
 
     private final AstModel model;
     private final Analysis analysis;
     private final IrModule module;
 
     private AstCompiler(AstModel model, Scope scope, IrModule module) {
         this.model = Check.notNull(model);
         this.analysis = Analysis.analyze(model, scope);
         this.module = Check.notNull(module);
     }
 
     public static AstSolutionMap compile(AstModel in, Scope scope, IrModule out) {
         AstCompiler compiler = new AstCompiler(in, scope, out);
         return compiler.compile();
     }
 
     private AstSolutionMap compile() {
         List<AstAbstractClafer> abstractClafers = model.getAbstractClafers();
         List<AstConcreteClafer> concreteClafers = AstUtil.getConcreteClafers(model);
 
         IrSetVar rootSet = constant(new int[]{0});
         set.put(model, $(rootSet));
         childrenSet.put(model, new IrSetVar[]{rootSet});
         membership.put(model, new IrBoolExpr[]{$(True)});
 
         KeyGraph<AstClafer> dependency = new KeyGraph<AstClafer>();
         for (AstAbstractClafer abstractClafer : abstractClafers) {
             Vertex<AstClafer> node = dependency.getVertex(abstractClafer);
             for (AstClafer sub : abstractClafer.getSubs()) {
                 node.addNeighbour(dependency.getVertex(sub));
             }
         }
         for (AstConcreteClafer concreteClafer : concreteClafers) {
             Vertex<AstClafer> node = dependency.getVertex(concreteClafer);
             if (Format.ParentGroup.equals(getFormat(concreteClafer))) {
                 node.addNeighbour(dependency.getVertex(concreteClafer.getParent()));
             }
         }
         List<Set<AstClafer>> components = TopologicalSort.computeStronglyConnectedComponents(dependency);
         List<AstClafer> clafers = new ArrayList<AstClafer>();
         for (Set<AstClafer> component : components) {
             if (component.size() != 1) {
                 throw new AstException("Cannot satisfy the cycle " + component);
             }
             clafers.addAll(component);
         }
 
         for (AstClafer clafer : clafers) {
             if (AstUtil.isRoot(clafer)) {
                 continue;
             }
             if (clafer instanceof AstConcreteClafer) {
                 initConcrete((AstConcreteClafer) clafer);
             } else if (clafer instanceof AstAbstractClafer) {
                 initAbstract((AstAbstractClafer) clafer);
             } else {
                 throw new AstException();
             }
         }
         for (AstClafer clafer : clafers) {
             if (AstUtil.isRoot(clafer)) {
                 continue;
             }
             if (clafer instanceof AstConcreteClafer) {
                 constrainConcrete((AstConcreteClafer) clafer);
             } else if (clafer instanceof AstAbstractClafer) {
                 constrainAbstract((AstAbstractClafer) clafer);
             } else {
                 throw new AstException();
             }
             constrainGroupCardinality(clafer);
         }
 
         List<IrBoolVar> softVars = new ArrayList<IrBoolVar>();
         for (AstClafer clafer : clafers) {
             List<AstConstraint> constraints = clafer.getConstraints();
             int scope = getScope(clafer);
             for (int i = 0; i < constraints.size(); i++) {
                 AstConstraint constraint = constraints.get(i);
                 if (constraint.isHard()) {
                     for (int j = 0; j < scope; j++) {
                         ExpressionCompiler expressionCompiler = new ExpressionCompiler(j);
                         IrBoolExpr thisConstraint = expressionCompiler.compile(constraint.getExpr());
                         module.addConstraint(implies(membership.get(clafer)[j], thisConstraint));
                     }
                 } else {
 //                    IrBoolVar soft = bool("Constraint#" + i + " under " + clafer + i);
                     IrBoolVar soft = bool(constraint.toString());
                     softVars.add(soft);
                     for (int j = 0; j < scope; j++) {
                         ExpressionCompiler expressionCompiler = new ExpressionCompiler(j);
                         IrBoolExpr thisConstraint = expressionCompiler.compile(constraint.getExpr());
                         module.addConstraint(implies($(soft), implies(membership.get(clafer)[j], thisConstraint)));
                     }
                 }
             }
         }
         for (IrSetVar[] childSet : childrenSet.getValues()) {
             module.addSetVars(childSet);
         }
         for (IrIntVar[] refs : refPointers.getValues()) {
             module.addIntVars(refs);
         }
 
         return new AstSolutionMap(model, childrenSet, refPointers, softVars.toArray(new IrBoolVar[softVars.size()]), analysis);
     }
 
     private void initConcrete(AstConcreteClafer clafer) {
         parentPointers.put(clafer, buildParentPointers(clafer));
         if (clafer.hasRef()) {
             refPointers.put(clafer.getRef(), buildRefPointers(clafer.getRef()));
         }
         switch (getFormat(clafer)) {
             case LowGroup:
                 initLowGroupConcrete(clafer);
                 break;
             case ParentGroup:
                 initParentGroupConcrete(clafer);
                 break;
             default:
                 throw new AstException();
         }
     }
 
     private void constrainConcrete(AstConcreteClafer clafer) {
         IrIntVar[] parents = parentPointers.get(clafer);
         if (!getPartialSolution(clafer).parentSolutionKnown()) {
             IrSetExpr[] childSet = $(childrenSet.get(clafer));
             if (getGlobalCard(clafer).isExact()) {
                 // No unused
                 module.addConstraint(intChannel($(parents), childSet));
             } else {
                 IrSetVar unused = set(clafer.getName() + "@Unused", getPartialSolution(clafer).getUnknownClafers());
                 module.addConstraint(intChannel($(parents), Util.snoc(childSet, $(unused))));
             }
         }
         AstClafer superClafer = clafer;
         AstRef ref = null;
         int refOffset = 0;
         do {
             if (superClafer.hasRef()) {
                 ref = superClafer.getRef();
                 break;
             }
             if (superClafer.hasSuperClafer()) {
                 refOffset += getOffset(superClafer.getSuperClafer(), superClafer);
             }
             superClafer = superClafer.getSuperClafer();
         } while (superClafer != null);
 
         if (ref != null) {
             IrIntVar[] refs = Arrays.copyOfRange(refPointers.get(ref),
                     refOffset, refOffset + getScope(clafer));
             if (ref.isUnique() && clafer.getCard().getHigh() > 1) {
                 if (AstUtil.isTop(clafer)) {
                     if (clafer.getCard().isExact()) {
                         assert clafer.getCard().getLow() == refs.length;
                         module.addConstraint(allDifferent($(refs)));
                     } else {
                         IrBoolExpr[] members = membership.get(clafer);
                         for (int i = 0; i < refs.length; i++) {
                             for (int j = i + 1; j < refs.length; j++) {
                                 module.addConstraint(
                                         implies(and(members[i], members[j]),
                                         notEqual($(refs[i]), $(refs[j]))));
                             }
                         }
                     }
                 } else {
                     int unused = getScope(clafer.getParent());
                     for (int i = 0; i < refs.length; i++) {
                         for (int j = i + 1; j < refs.length; j++) {
                             module.addConstraint(
                                     implies(and(notEqual($(parents[i]), unused),
                                     equal($(parents[i]), $(parents[j]))),
                                     notEqual($(refs[i]), $(refs[j]))));
                         }
                     }
                 }
             }
             IrBoolExpr[] members = membership.get(clafer);
             assert refs.length == members.length;
             for (int i = 0; i < members.length; i++) {
                 module.addConstraint(implies(not(members[i]), equal($(refs[i]), 0)));
             }
 
             if (!(ref.getTargetType() instanceof AstIntClafer)) {
                 IrSetExpr targetSet = set.get(ref.getTargetType());
                 for (IrIntVar refPointer : refs) {
                     module.addConstraint(member($(refPointer), targetSet));
                 }
             }
         }
         /**
          * What is this optimization?
          *
          * This optimization is to remove isomorphic solutions due to swapping
          * of children.
          *
          * Consider the following Clafer model.
          *
          * Diner 2 Burger 1..*
          *
          * Let the scope be {Diner=2, Food=3}. What this says is that we have 2
          * Diners and 3 Burgers but each Diner gets at least one serving.
          * Logically, there are two solutions:
          *
          * 1. Each Diner gets 1 Burger each (the last Burger is unused) 2. One
          * Diner gets 2 Burgers and the other Diner gets 1 Burger
          *
          * There are two unique solutions, other isomorphic solutions may arise
          * from the solver, but ideally we want to eliminate the isomorphic
          * duplicates. For example, here are two isomorphic instances that will
          * arrise with symmetry breaking:
          *
          * Diner0 Burger0 Burger1 Diner1 Burger2
          *
          *
          * Diner0 Burger0 Diner1 Burger1 Burger2
          *
          * We add the constraint |Diner0.Burger| >= |Diner1.Burger| to break the
          * symmetry. This is how it works when Diner only has one type as a
          * child. This optimization generalizes to multi-children. For example,
          * consider the Clafer model:
          *
          * Diner 2 Burger 1..* Drink 1..*
          *
          * Let the scope be {Diner=2, Food=3, Drink=4}. First we'll see a wrong
          * generalization. Adding the two constraints DO NOT WORK:
          *
          * 1. |Diner0.Burger| >= |Diner1.Burger| 2. |Diner0.Drink| >=
          * |Diner1.Drink|
          *
          * The two constraints above DO NOT WORK because it rules out the case
          * where one Diner has more Burgers but the other Diner has more drinks.
          * Instead, we want tuple comparison like the following constraint
          * (tuple comparision as implemented in Haskell, ie. compare the first
          * indices, then use the second index to break ties, then use the third
          * index to break the next tie, etc.):
          *
          * (|Diner0.Burger|, |Diner0.Drink|) >= (|Diner1.Burger|,
          * |Diner1.Drink|)
          *
          * However, Choco does not implement tuple comparision but it's we can
          * simulate it with the equation constraint since the size of the sets
          * are bounded.
          *
          * 5 * |Diner0.Burger| + 1 * |Diner0.Drink| >= 5 * |Diner1.Burger| + 1 *
          * |Diner1.Drink|
          *
          * The "5" is coefficient comes from the fact that scope(Drink) = 4.
          */
         IrIntExpr[][] terms = new IrIntExpr[getScope(clafer)][];
 
         for (int i = 0; i < terms.length; i++) {
             List<IrIntExpr> string = new ArrayList<IrIntExpr>();
             for (AstConcreteClafer child : clafer.getChildren()) {
                 // Children with exact cards will always contribute the same score hence it
                 // is a waste of computation time to include them in the scoring constraints.
                 if (!child.getCard().isExact()) {
                     string.add(card($(childrenSet.get(child)[i])));
                 }
             }
             AstClafer sup = clafer;
             int offset = 0;
             while (sup.hasSuperClafer()) {
                 offset += getOffset(sup.getSuperClafer(), sup);
                 for (AstConcreteClafer child : sup.getSuperClafer().getChildren()) {
                     if (!child.getCard().isExact()) {
                         string.add(card($(childrenSet.get(child)[i + offset])));
                     }
                 }
                 sup = sup.getSuperClafer();
             }
             if (string.isEmpty()) {
                 assert terms[0] == null;
                 break;
             }
             terms[i] = string.toArray(new IrIntExpr[string.size()]);
         }
         if (terms[0] != null) {
             // TODO: Do not need to separte in two different steps with optimzations.
             if (AstUtil.isTop(clafer)) {
                 /*
                  * Reversing is so that earlier children have higher scores. Not necessary,
                  * but the solutions will have instances that are similar closer together.
                  */
                 Util.reverse(terms);
                 module.addConstraint(sort(terms));
             } else {
                 for (int i = 0; i < getScope(clafer) - 1; i++) {
                     module.addConstraint(
                             implies(equal($(parents[i]), $(parents[i + 1])),
                             sort(terms[i + 1], terms[i])));
                 }
             }
         }
 
         switch (getFormat(clafer)) {
             case LowGroup:
                 constrainLowGroupConcrete(clafer);
                 break;
             case ParentGroup:
                 constrainParentGroupConcrete(clafer);
                 break;
             default:
                 throw new AstException();
         }
     }
 
     private void constrainGroupCardinality(AstClafer clafer) {
         Card groupCard = clafer.getGroupCard();
         List<AstConcreteClafer> children = clafer.getChildren();
         if (groupCard.isBounded()) {
             IrBoolExpr[] members = membership.get(clafer);
             IrSetVar[][] childrenSets = new IrSetVar[children.size()][];
             boolean featureGroup = true;
             for (int i = 0; i < childrenSets.length; i++) {
                 AstConcreteClafer child = children.get(i);
                 childrenSets[i] = childrenSet.get(child);
                 featureGroup &= child.getCard().getHigh() == 1;
             }
             int scope = getScope(clafer);
             for (int i = 0; i < scope; i++) {
                 IrIntExpr[] cards = new IrIntExpr[childrenSets.length];
                 for (int j = 0; j < cards.length; j++) {
                     cards[j] = card($(childrenSets[j][i]));
                 }
                 if (featureGroup) {
                     // Translate common cases more efficiently.
                     if (groupCard.getLow() == 0 && groupCard.getHigh() == 1) {
                         // "lone"
                         module.addConstraint(implies(members[i], lone(asBools(cards))));
                         continue;
                     }
                     if (groupCard.getLow() == 1 && !groupCard.hasHigh()) {
                         // "or"
                         module.addConstraint(implies(members[i], or(asBools(cards))));
                         continue;
                     }
                     if (groupCard.isExact() && groupCard.getLow() == 1) {
                         // "xor"
                         // TODO constrain as sum(cards) = card(this)
                         module.addConstraint(implies(members[i], one(asBools(cards))));
                         continue;
                     }
                 }
                 module.addConstraint(implies(members[i], constrainCard(add(cards), groupCard)));
             }
         }
     }
 
     private void initLowGroupConcrete(AstConcreteClafer clafer) {
         PartialSolution partialSolution = getPartialSolution(clafer);
 
         IrSetVar[] childSet = buildChildSet(clafer);
         childrenSet.put(clafer, childSet);
         set.put(clafer, union($(childSet)));
 
         IrBoolExpr[] members = new IrBoolExpr[getScope(clafer)];
         for (int i = 0; i < members.length; i++) {
             if (partialSolution.hasClafer(i)) {
                 members[i] = $(True);
             } else {
                 members[i] =
                         childSet.length == 1 && members.length == 1
                         ? asBool(card($(childSet[0])))
                         : $(bool(clafer.getName() + "@Membership#" + i));
             }
         }
         membership.put(clafer, members);
     }
 
     private void constrainLowGroupConcrete(AstConcreteClafer clafer) {
         IrBoolExpr[] members = membership.get(clafer);
 
         module.addConstraint(selectN(members, card(set.get(clafer))));
 
         IrBoolExpr[] parentMembership = membership.get(clafer.getParent());
         Card card = clafer.getCard();
 
         IrSetVar[] childSet = childrenSet.get(clafer);
         for (int i = 0; i < parentMembership.length; i++) {
             IrBoolExpr parentMember = parentMembership[i];
             if (card.isBounded()) {
                 // Enforce cardinality.
                 module.addConstraint(implies(parentMember, constrainCard(card($(childSet[i])), card)));
             }
             module.addConstraint(implies(not(parentMember), equal($(childSet[i]), $(EmptySet))));
         }
 
         IrSetExpr claferSet = set.get(clafer);
 
         if (!(childSet.length == 1 && members.length == 1)) {
             module.addConstraint(boolChannel(members, claferSet));
         }
 
         /**
          * What is this optimization?
          *
          * Force the lower number atoms to choose lower number parents. For
          * example consider the following Clafer model:
          *
          * Person 2 Hand 2
          *
          * The constraint forbids the case where Hand0 belongs to Person1 and
          * Hand1 belongs to Person0. Otherwise, the children can swap around
          * creating many isomorphic solutions.
          */
         module.addConstraint(sort($(parentPointers.get(clafer))));
     }
 
     private void initParentGroupConcrete(AstConcreteClafer clafer) {
         PartialSolution partialParentSolution = getPartialParentSolution(clafer);
 
         IrSetVar[] children = new IrSetVar[partialParentSolution.size()];
         assert clafer.getCard().getLow() == clafer.getCard().getHigh();
         int lowCard = clafer.getCard().getLow();
         for (int i = 0; i < children.length; i++) {
             if (partialParentSolution.hasClafer(i)) {
                 children[i] = constant(Util.fromTo(i * lowCard, i * lowCard + lowCard));
             } else {
                 children[i] = set(clafer.getName() + "#" + i, Util.fromTo(i * lowCard, i * lowCard + lowCard));
             }
         }
 
         childrenSet.put(clafer, children);
         set.put(clafer, union($(children)));
 
         IrBoolExpr[] members = new IrBoolExpr[getScope(clafer)];
         IrBoolExpr[] parentMembership = membership.get(clafer.getParent());
         if (lowCard == 1) {
             members = parentMembership;
         } else {
             for (int i = 0; i < parentMembership.length; i++) {
                 for (int j = 0; j < lowCard; j++) {
                     members[i * lowCard + j] = parentMembership[i];
                 }
             }
         }
         membership.put(clafer, members);
     }
 
     private void constrainParentGroupConcrete(AstConcreteClafer clafer) {
         PartialSolution partialParentSolution = getPartialParentSolution(clafer);
 
         IrSetVar[] children = childrenSet.get(clafer);
         assert clafer.getCard().getLow() == clafer.getCard().getHigh();
         int lowCard = clafer.getCard().getLow();
         for (int i = 0; i < children.length; i++) {
             if (!partialParentSolution.hasClafer(i)) {
                 module.addConstraint(implies(membership.get(clafer.getParent())[i],
                         equal($(children[i]), $(constant(Util.fromTo(i * lowCard, i * lowCard + lowCard))))));
                 module.addConstraint(implies(not(membership.get(clafer.getParent())[i]),
                         equal($(children[i]), $(EmptySet))));
             }
         }
     }
 
     private void initAbstract(AstAbstractClafer clafer) {
         IrSetExpr[] subSets = new IrSetExpr[clafer.getSubs().size()];
         IrBoolExpr[] members = new IrBoolExpr[getScope(clafer)];
         for (int i = 0; i < subSets.length; i++) {
             AstClafer sub = clafer.getSubs().get(i);
             subSets[i] = set.get(sub);
             IrBoolExpr[] subMembers = membership.get(sub);
             int offset = getOffset(clafer, sub);
             for (int j = 0; j < subMembers.length; j++) {
                 assert members[offset + j] == null;
                 members[offset + j] = Check.notNull(subMembers[j]);
             }
         }
         set.put(clafer, union(subSets));
         Check.noNulls(members);
         membership.put(clafer, members);
 
         if (clafer.hasRef()) {
             refPointers.put(clafer.getRef(), buildRefPointers(clafer.getRef()));
         }
     }
 
     private void constrainAbstract(AstAbstractClafer clafer) {
         // Do nothing.
     }
     private final ReadWriteHashMap<AstClafer, IrSetExpr> set = new ReadWriteHashMap<AstClafer, IrSetExpr>();
     private final ReadWriteHashMap<AstClafer, IrSetVar[]> childrenSet = new ReadWriteHashMap<AstClafer, IrSetVar[]>();
     private final ReadWriteHashMap<AstClafer, IrBoolExpr[]> membership = new ReadWriteHashMap<AstClafer, IrBoolExpr[]>();
     private final ReadWriteHashMap<AstConcreteClafer, IrIntVar[]> parentPointers = new ReadWriteHashMap<AstConcreteClafer, IrIntVar[]>();
     private final ReadWriteHashMap<AstRef, IrIntVar[]> refPointers = new ReadWriteHashMap<AstRef, IrIntVar[]>();
 
     private class ExpressionCompiler implements AstExprVisitor<Void, IrExpr> {
 
         private final int thisId;
         private final Map<AstLocal, IrIntExpr> locals = new HashMap<AstLocal, IrIntExpr>();
 
         private ExpressionCompiler(int thisId) {
             this.thisId = thisId;
         }
 
         private IrExpr compile(AstExpr expr) {
             return expr.accept(this, null);
         }
 
         private IrExpr[] compile(AstExpr[] exprs) {
             IrExpr[] compiled = new IrExpr[exprs.length];
             for (int i = 0; i < compiled.length; i++) {
                 compiled[i] = compile(exprs[i]);
             }
             return compiled;
         }
 
         private IrBoolExpr compile(AstBoolExpr expr) {
             return (IrBoolExpr) compile((AstExpr) expr);
         }
 
         private IrBoolExpr[] compile(AstBoolExpr[] exprs) {
             IrBoolExpr[] compiled = new IrBoolExpr[exprs.length];
             for (int i = 0; i < compiled.length; i++) {
                 compiled[i] = compile(exprs[i]);
             }
             return compiled;
         }
 
         private IrIntExpr asInt(IrExpr expr) {
             if (expr instanceof IrIntExpr) {
                 return (IrIntExpr) expr;
             }
             if (expr instanceof IrSetExpr) {
                 return sum((IrSetExpr) expr);
             }
             // Bug.
             throw new AstException("Should not have passed type checking.");
         }
 
         private IrIntExpr[] asInts(IrExpr[] exprs) {
             IrIntExpr[] ints = new IrIntExpr[exprs.length];
             for (int i = 0; i < ints.length; i++) {
                 ints[i] = asInt(exprs[i]);
             }
             return ints;
         }
 
         private IrSetExpr asSet(IrExpr expr) {
             if (expr instanceof IrIntExpr) {
                 return singleton((IrIntExpr) expr);
             }
             if (expr instanceof IrSetExpr) {
                 return (IrSetExpr) expr;
             }
             // Bug.
             throw new AstException("Should not have passed type checking.");
         }
 
         private IrSetExpr[] asSets(IrExpr[] exprs) {
             IrSetExpr[] sets = new IrSetExpr[exprs.length];
             for (int i = 0; i < sets.length; i++) {
                 sets[i] = asSet(exprs[i]);
             }
             return sets;
         }
 
         @Override
         public IrExpr visit(AstThis ast, Void a) {
             return $(constant(thisId));
         }
 
         @Override
         public IrExpr visit(AstGlobal ast, Void a) {
             return set.get(ast.getType());
         }
 
         @Override
         public IrExpr visit(AstConstant ast, Void a) {
             return $(constant(ast.getValue()));
         }
 
         @Override
         public IrExpr visit(AstJoin ast, Void a) {
             AstSetExpr left = ast.getLeft();
             AstConcreteClafer right = ast.getRight();
 
             IrExpr $left = compile(left);
             if ($left instanceof IrIntExpr) {
                 IrIntExpr $intLeft = (IrIntExpr) $left;
                 if (Format.ParentGroup.equals(getFormat(right)) && right.getCard().getLow() == 1) {
                     assert right.getCard().isExact();
                     return $intLeft;
                 }
                 // Why empty set? The "take" var can contain unused.
                 return join(singleton($intLeft), $(Util.snoc(childrenSet.get(right), EmptySet)));
             } else if ($left instanceof IrSetExpr) {
                 IrSetExpr $setLeft = (IrSetExpr) $left;
                 // Why empty set? The "take" var can contain unused.
                 return join($setLeft, $(Util.snoc(childrenSet.get(right), EmptySet)));
             }
             throw new AstException();
         }
 
         @Override
         public IrExpr visit(AstJoinParent ast, Void a) {
             AstSetExpr children = ast.getChildren();
             AstConcreteClafer childrenType = (AstConcreteClafer) getType(children);
 
             IrExpr $children = compile(children);
             if ($children instanceof IrIntExpr) {
                 // TODO: remove this, and do this as an optimization step
                 if (getScope(childrenType.getParent()) == 1) {
                     return $(Zero);
                 }
 
                 IrIntExpr $intChildren = (IrIntExpr) $children;
                 switch (getFormat(childrenType)) {
                     case ParentGroup:
                         assert childrenType.getCard().isExact();
                         int lowCard = childrenType.getCard().getLow();
                         return div($intChildren, $(constant(lowCard)));
                     case LowGroup:
                         return element($(parentPointers.get(childrenType)), $intChildren);
                 }
             } else if ($children instanceof IrSetExpr) {
                 IrSetExpr $setChildren = (IrSetExpr) $children;
                 return joinRef($setChildren, $(parentPointers.get(childrenType)));
             }
             throw new AstException();
         }
 
         @Override
         public IrExpr visit(AstJoinRef ast, Void a) {
             AstSetExpr deref = ast.getDeref();
             AstClafer derefType = getType(deref);
 
             IrExpr $deref = compile(deref);
             if ($deref instanceof IrIntExpr) {
                 IrIntExpr $intDeref = (IrIntExpr) $deref;
                 return element($(refPointers.get(derefType.getRef())), $intDeref);
             } else if ($deref instanceof IrSetExpr) {
                 IrSetExpr $setDeref = (IrSetExpr) $deref;
                 return joinRef($setDeref, $(refPointers.get(derefType.getRef())));
             }
             throw new AstException();
         }
 
         @Override
         public IrExpr visit(AstCard ast, Void a) {
             IrExpr set = compile(ast.getSet());
             if (set instanceof IrIntExpr) {
                 return $(One);
             }
             return card((IrSetExpr) set);
         }
 
         @Override
         public IrExpr visit(AstNot ast, Void a) {
             return not(compile(ast.getExpr()));
         }
 
         @Override
         public IrExpr visit(AstMinus ast, Void a) {
             return minus(asInt(compile(ast)));
         }
 
         @Override
         public IrExpr visit(AstSetTest ast, Void a) {
             IrExpr left = compile(ast.getLeft());
             IrExpr right = compile(ast.getRight());
 
             if (left instanceof IrIntExpr && right instanceof IrIntExpr) {
                 IrIntExpr $intLeft = (IrIntExpr) left;
                 IrIntExpr $intRight = (IrIntExpr) right;
 
                 switch (ast.getOp()) {
                     case Equal:
                         return equal($intLeft, $intRight);
                     case NotEqual:
                         return notEqual($intLeft, $intRight);
                 }
             }
 
             switch (ast.getOp()) {
                 case Equal:
                     return equal(asSet(left), asSet(right));
                 case NotEqual:
                     return notEqual(asSet(left), asSet(right));
                 default:
                     throw new AstException();
             }
         }
 
         @Override
         public IrExpr visit(AstCompare ast, Void a) {
             IrIntExpr left = asInt(compile(ast.getLeft()));
             IrIntExpr right = asInt(compile(ast.getRight()));
             switch (ast.getOp()) {
                 case LessThan:
                     return lessThan(left, right);
                 case LessThanEqual:
                     return lessThanEqual(left, right);
                 case GreaterThan:
                     return greaterThan(left, right);
                 case GreaterThanEqual:
                     return greaterThanEqual(left, right);
                 default:
                     throw new AstException();
             }
         }
 
         @Override
         public IrExpr visit(AstArithm ast, Void a) {
             IrIntExpr[] operands = asInts(compile(ast.getOperands()));
             switch (ast.getOp()) {
                 case Add:
                     return add(operands);
                 case Sub:
                     return sub(operands);
                 case Mul:
                     IrIntExpr product = operands[0];
                     for (int i = 1; i < operands.length; i++) {
                         product = mul(product, operands[i]);
                     }
                     return product;
                 case Div:
                     IrIntExpr quotient = operands[0];
                     for (int i = 1; i < operands.length; i++) {
                         quotient = div(quotient, operands[i]);
                     }
                     return quotient;
                 default:
                     throw new AstException();
             }
         }
 
         @Override
         public IrExpr visit(AstBoolArithm ast, Void a) {
             IrBoolExpr[] operands = compile(ast.getOperands());
             switch (ast.getOp()) {
                 case And:
                     return and(operands);
                 case IfOnlyIf:
                     IrBoolExpr ifOnlyIf = operands[0];
                     for (int i = 1; i < operands.length; i++) {
                         ifOnlyIf = ifOnlyIf(ifOnlyIf, operands[i]);
                     }
                     return ifOnlyIf;
                 case Implies:
                     IrBoolExpr implies = operands[0];
                     for (int i = 1; i < operands.length; i++) {
                         implies = implies(implies, operands[i]);
                     }
                     return implies;
                 case Or:
                     return or(operands);
                 case Xor:
                     IrBoolExpr xor = operands[0];
                     for (int i = 1; i < operands.length; i++) {
                         xor = xor(xor, operands[i]);
                     }
                     return xor;
                 default:
                     throw new AstException();
             }
         }
 
         @Override
         public IrExpr visit(AstSetArithm ast, Void a) {
             IrSetExpr[] operands = asSets(compile(ast.getOperands()));
             switch (ast.getOp()) {
                 case Union:
                     return union(operands);
                 case Difference:
                     IrSetExpr difference = operands[0];
                     for (int i = 1; i < operands.length; i++) {
                         difference = difference(difference, operands[i]);
                     }
                     return difference;
                 case Intersection:
                     return intersection(operands);
                 default:
                     throw new AstException();
             }
         }
 
         @Override
         public IrExpr visit(AstMembership ast, Void a) {
             IrExpr member = compile(ast.getMember());
             IrExpr set = compile(ast.getSet());
             if (member instanceof IrIntExpr && set instanceof IrIntExpr) {
                 return AstMembership.Op.In.equals(ast.getOp())
                         ? equal((IrIntExpr) member, (IrIntExpr) set)
                         : notEqual((IrIntExpr) member, (IrIntExpr) set);
             }
             if (member instanceof IrIntExpr && set instanceof IrSetExpr) {
                 return AstMembership.Op.In.equals(ast.getOp())
                         ? member((IrIntExpr) member, (IrSetExpr) set)
                         : notMember((IrIntExpr) member, (IrSetExpr) set);
             }
             if (member instanceof IrSetExpr && set instanceof IrIntExpr) {
                 return AstMembership.Op.In.equals(ast.getOp())
                         ? equal((IrSetExpr) member, singleton((IrIntExpr) set))
                         : notEqual((IrSetExpr) member, singleton((IrIntExpr) set));
             }
             return AstMembership.Op.In.equals(ast.getOp())
                     ? subsetEq(asSet(member), asSet(set))
                     : not(subsetEq(asSet(member), asSet(set)));
         }
 
         @Override
         public IrExpr visit(AstTernary ast, Void a) {
             IrBoolExpr antecedent = compile(ast.getAntecedent());
             IrExpr consequent = compile(ast.getConsequent());
             IrExpr alternative = compile(ast.getAlternative());
             if (consequent instanceof IrIntExpr && alternative instanceof IrIntExpr) {
                 return ternary(antecedent, (IrIntExpr) consequent, (IrIntExpr) alternative);
             }
             return ternary(antecedent, asSet(consequent), asSet(alternative));
         }
 
         @Override
         public IrExpr visit(AstIfThenElse ast, Void a) {
             return ifThenElse(compile(ast.getAntecedent()), compile(ast.getConsequent()), compile(ast.getAlternative()));
         }
 
         @Override
         public IrExpr visit(AstUpcast ast, Void a) {
             AstSetExpr base = ast.getBase();
             int offset = getOffset(ast.getTarget(), getType(base));
 
             IrExpr $base = compile(ast.getBase());
             if ($base instanceof IrIntExpr) {
                 IrIntExpr intBase = (IrIntExpr) $base;
                 return add(intBase, $(constant(offset)));
             }
             return offset((IrSetExpr) $base, offset);
         }
 
         @Override
         public IrExpr visit(AstLocal ast, Void a) {
             return locals.get(ast);
         }
 
         private Triple<AstLocal, IrIntExpr, IrBoolExpr>[][] compileDecl(AstDecl decl) {
             IrExpr body = compile(decl.getBody());
             if (body instanceof IrIntExpr) {
                 IrIntExpr intBody = (IrIntExpr) body;
                 @SuppressWarnings("unchecked")
                 Triple<AstLocal, IrIntExpr, IrBoolExpr>[] labeledPermutation = new Triple[decl.getLocals().length];
                 for (int i = 0; i < labeledPermutation.length; i++) {
                     labeledPermutation[i] = new Triple<AstLocal, IrIntExpr, IrBoolExpr>(
                             decl.getLocals()[i], intBody, $(True));
                 }
                 @SuppressWarnings("unchecked")
                 Triple<AstLocal, IrIntExpr, IrBoolExpr>[][] labeledSequence = new Triple[][]{labeledPermutation};
                 return labeledSequence;
             }
             if (body instanceof IrSetExpr) {
                 IrSetExpr setBody = (IrSetExpr) body;
                 IrDomain env = setBody.getEnv();
                 IrDomain ker = setBody.getKer();
                 // TODO: need a different strategy otherwise
                 assert env.getLowBound() >= 0;
                 @SuppressWarnings("unchecked")
                 Pair<IrIntExpr, IrBoolExpr>[] members = new Pair[env.getHighBound() + 1];
                 for (int i = 0; i < env.getLowBound(); i++) {
                     members[i] = new Pair<IrIntExpr, IrBoolExpr>($(constant(i)), $(False));
                 }
                 for (int i = env.getLowBound(); i <= env.getHighBound(); i++) {
                     members[i] = new Pair<IrIntExpr, IrBoolExpr>($(constant(i)),
                             $(ker.contains(i) ? True
                             : bool(Util.intercalate("/", AstUtil.getNames(decl.getLocals())) + "#" + i)));
                 }
                 module.addConstraint(boolChannel(Util.mapSnd(Arrays.asList(members)), setBody));
                 Pair<IrIntExpr, IrBoolExpr>[][] sequence = decl.isDisjoint() ? Util.permutations(members,
                         decl.getLocals().length) : Util.sequence(members, decl.getLocals().length);
 
                 @SuppressWarnings("unchecked")
                 Triple<AstLocal, IrIntExpr, IrBoolExpr>[][] labeledSequence = new Triple[sequence.length][];
                 for (int i = 0; i < labeledSequence.length; i++) {
                     Pair<IrIntExpr, IrBoolExpr>[] permutation = sequence[i];
                     @SuppressWarnings("unchecked")
                     Triple<AstLocal, IrIntExpr, IrBoolExpr>[] labeledPermutation = new Triple[permutation.length];
                     for (int j = 0; j < labeledPermutation.length; j++) {
                         labeledPermutation[j] = new Triple<AstLocal, IrIntExpr, IrBoolExpr>(
                                 decl.getLocals()[j], permutation[j]);
                     }
                     labeledSequence[i] = labeledPermutation;
                 }
                 return labeledSequence;
             }
             throw new AstException();
         }
 
         // TODO optimize SOME
         @Override
         public IrExpr visit(AstQuantify ast, Void a) {
             AstDecl decls[] = ast.getDecls();
             @SuppressWarnings("unchecked")
             Triple<AstLocal, IrIntExpr, IrBoolExpr>[][][] compiledDecls = new Triple[decls.length][][];
             for (int i = 0; i < compiledDecls.length; i++) {
                 compiledDecls[i] = compileDecl(decls[i]);
             }
             compiledDecls = Util.sequence(compiledDecls);
 
             List<IrBoolExpr> compiled = new ArrayList<IrBoolExpr>();
             for (Triple<AstLocal, IrIntExpr, IrBoolExpr>[][] quants : compiledDecls) {
                 List<IrBoolExpr> constraints = new ArrayList<IrBoolExpr>();
                 for (Triple<AstLocal, IrIntExpr, IrBoolExpr>[] quantDecls : quants) {
                     for (Triple<AstLocal, IrIntExpr, IrBoolExpr> quantLocals : quantDecls) {
                         constraints.add(quantLocals.getThd());
                         locals.put(quantLocals.getFst(), quantLocals.getSnd());
                     }
                 }
                 IrBoolExpr compiledBody = compile(ast.getBody());
                 if (Quantifier.All.equals(ast.getQuantifier())) {
                     compiled.add(implies(and(constraints), compiledBody));
                 } else {
                     constraints.add(compiledBody);
                     compiled.add(and(constraints));
                 }
             }
 
             switch (ast.getQuantifier()) {
                 case All:
                     return and(compiled);
                 case Lone:
                     return lone(compiled);
                 case None:
                     return not(or(compiled));
                 case One:
                     return one(compiled);
                 case Some:
                     return or(compiled);
                 default:
                     throw new AstException();
 
             }
         }
     };
 
     /*
      ******************
      * Build functions.
      ******************
      */
     /**
      * Build the child set for the Clafer.
      *
      * @param clafer the Clafer
      * @return the variables to represent the child relation
      */
     private IrSetVar[] buildChildSet(AstConcreteClafer clafer) {
         assert Format.LowGroup.equals(getFormat(clafer));
 
         int parentScope = getScope(clafer.getParent());
         PartialSolution partialParentSolution = getPartialSolution(clafer.getParent());
 
         int claferScope = getScope(clafer);
         Card card = clafer.getCard();
         assert card.hasHigh();
 
         int low = 0;
         int high = card.getHigh();
         int max = claferScope - 1;
 
         IrSetVar[] skip = new IrSetVar[parentScope];
         for (int i = 0; i < skip.length; i++) {
             if (low <= max) {
                 IrDomain _env = boundDomain(low, Math.min(high - 1, max));
                 IrDomain _ker = EmptyDomain;
                 IrDomain _card = boundDomain(0, card.getHigh());
                 if (partialParentSolution.hasClafer(i)) {
                     int prevHigh = high - card.getHigh();
                     int nextLow = low + card.getLow();
                     if (nextLow > prevHigh) {
                         _ker = boundDomain(prevHigh, Math.min(nextLow - 1, max));
                     }
                     _card = boundDomain(card.getLow(), card.getHigh());
                 }
                 skip[i] = set(clafer.getName() + "#" + i, _env, _ker, _card);
             } else {
                 skip[i] = EmptySet;
             }
             if (partialParentSolution.hasClafer(i)) {
                 low += card.getLow();
             }
             high += card.getHigh();
         }
         return skip;
     }
 
     /**
      * Create the parent pointers for the Clafer.
      *
      * @param clafer the Clafer
      * @return the variables to represent the parent relation
      */
     private IrIntVar[] buildParentPointers(AstConcreteClafer clafer) {
         PartialSolution solution = getPartialSolution(clafer);
         boolean known = solution.parentSolutionKnown();
         IrIntVar[] pointers = new IrIntVar[solution.size()];
         for (int i = 0; i < pointers.length; i++) {
             int[] possibleParents = solution.getPossibleParents(i);
             pointers[i] = enumInt(clafer.getName() + "@Parent#" + i,
                     solution.hasClafer(i) || known
                     ? possibleParents
                     : Util.snoc(possibleParents, getScope(clafer.getParent())));
         }
         return pointers;
     }
 
     /**
      * Create the references pointers for the Clafer.
      *
      * @param ref the reference Clafer
      * @return the variables to represent the reference relation
      */
     private IrIntVar[] buildRefPointers(AstRef ref) {
         AstClafer src = ref.getSourceType();
         AstClafer tar = ref.getTargetType();
 
         PartialSolution partialSolution = getPartialSolution(src);
         int[][] partialInts = getPartialInts(ref);
         IrIntVar[] ivs = new IrIntVar[getScope(src)];
         for (int i = 0; i < ivs.length; i++) {
             if (partialInts[i] == null) {
                // BUG: TODO: what if '0' is not between intlow and inthigh
                ivs[i] = boundInt(src.getName() + "@Ref" + i, getScopeLow(tar), getScopeHigh(tar));
             } else {
                 if (partialSolution.hasClafer(i)) {
                     ivs[i] = enumInt(src.getName() + "@Ref" + i, partialInts[i]);
                 } else {
                     ivs[i] = enumInt(src.getName() + "@Ref" + i, Util.cons(0, partialInts[i]));
                 }
             }
         }
 
         return ivs;
     }
 
     /**
      * Enforce the size of a set to be within the cardinality.
      *
      * @param setCard the set to constrain
      * @param card the cardinality
      * @return card.low &le; |setCard| &le; card.high
      */
     private IrBoolExpr constrainCard(IrIntExpr setCard, Card card) {
         if (card.isExact()) {
             return equal(setCard, card.getLow());
         }
         if (card.hasLow() && card.hasHigh()) {
             return between(setCard, card.getLow(), card.getHigh());
         }
         if (card.hasLow()) {
             return greaterThanEqual(setCard, card.getLow());
         }
         if (card.hasHigh()) {
             return lessThanEqual(setCard, card.getHigh());
         }
         return $(True);
     }
 
     /*
      ************************
      * Convenience functions.
      ************************
      */
     private int getScopeLow(AstClafer clafer) {
         return clafer instanceof AstIntClafer ? analysis.getScope().getIntLow() : 0;
     }
 
     private int getScopeHigh(AstClafer clafer) {
         return clafer instanceof AstIntClafer ? analysis.getScope().getIntHigh() : getScope(clafer) - 1;
     }
 
     private int getScope(AstClafer clafer) {
         return analysis.getScope().getScope(clafer);
     }
 
     private Format getFormat(AstClafer clafer) {
         return analysis.getFormat(clafer);
     }
 
     private PartialSolution getPartialSolution(AstClafer clafer) {
         return analysis.getPartialSolution(clafer);
     }
 
     private PartialSolution getPartialParentSolution(AstConcreteClafer clafer) {
         return getPartialSolution(clafer.getParent());
     }
 
     private int[][] getPartialInts(AstRef ref) {
         return analysis.getPartialInts(ref);
     }
 
     private int getOffset(AstAbstractClafer sup, AstClafer sub) {
         return analysis.getOffset(sup, sub);
     }
 
     private Card getGlobalCard(AstClafer clafer) {
         return analysis.getGlobalCard(clafer);
     }
 
     private int getDepth(AstAbstractClafer clafer) {
         return analysis.getDepth(clafer);
     }
 
     private AstClafer getType(AstExpr expr) {
         return analysis.getType(expr);
     }
 }
