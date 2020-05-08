 package org.clafer.ir;
 
 import gnu.trove.TIntCollection;
 import gnu.trove.iterator.TIntIterator;
 import gnu.trove.set.TIntSet;
 import gnu.trove.set.hash.TIntHashSet;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import org.clafer.common.Util;
 
 /**
  * Import this class to access all IR building functions.
  * <pre>
  * import static org.clafer.ast.Asts.*;
  * </pre>
  *
  * @author jimmy
  */
 public class Irs {
 
     private Irs() {
     }
     /**
      *******************
      *
      * Domain
      *
      *******************
      */
     public static final IrBoolDomain TrueDomain = IrBoolDomain.TrueDomain;
     public static final IrBoolDomain FalseDomain = IrBoolDomain.FalseDomain;
     public static final IrBoolDomain BoolDomain = IrBoolDomain.BoolDomain;
     public static final IrDomain EmptyDomain = new IrEmptyDomain();
     public static final IrDomain ZeroDomain = FalseDomain;
     public static final IrDomain OneDomain = TrueDomain;
     public static final IrDomain ZeroOneDomain = BoolDomain;
     public static final IrSetVar EmptySet = new IrSetConstant(EmptyDomain);
 
     public static IrBoolDomain domain(boolean value) {
         return value ? TrueDomain : FalseDomain;
     }
 
     public static IrDomain constantDomain(int value) {
         return boundDomain(value, value);
     }
 
     public static IrDomain fromToDomain(int from, int to) {
         if (from == to) {
             return EmptyDomain;
         }
         return boundDomain(from, to - 1);
     }
 
     public static IrDomain boundDomain(int low, int high) {
         if (low == 0 && high == 0) {
             return ZeroDomain;
         }
         if (low == 1 && high == 1) {
             return OneDomain;
         }
         if (low == 0 && high == 1) {
             return ZeroOneDomain;
         }
         return new IrBoundDomain(low, high);
     }
 
     public static IrDomain enumDomain(int... values) {
         return enumDomain(new TIntHashSet(values));
     }
 
     public static IrDomain enumDomain(TIntCollection values) {
         return enumDomain(new TIntHashSet(values));
     }
 
     public static IrDomain enumDomain(TIntSet values) {
         switch (values.size()) {
             case 0:
                 return EmptyDomain;
             case 1:
                 int value = values.iterator().next();
                 return boundDomain(value, value);
             default:
                 int[] array = values.toArray();
                 Arrays.sort(array);
                 // If the values are over a contiguous interval, then return a bound domain.
                 int low = array[0];
                 int high = array[array.length - 1];
                 if (high - low + 1 == array.length) {
                     // A contigious interval.
                     return boundDomain(low, high);
                 }
                 return new IrEnumDomain(array);
         }
     }
     /**
      *******************
      *
      * Boolean
      *
      *******************
      */
     public static final IrBoolVar True = new IrBoolConstant(true);
     public static final IrBoolVar False = new IrBoolConstant(false);
 
     public static IrBoolVar constant(boolean value) {
         return value ? True : False;
     }
 
     public static IrBoolVar bool(String name) {
         return new IrBoolVar(name, BoolDomain);
     }
 
     public static IrBoolExpr not(IrBoolExpr proposition) {
         Boolean constant = IrUtil.getConstant(proposition);
         if (constant != null) {
             // Reverse the boolean
             return constant.booleanValue() ? False : True;
         }
         return proposition.negate();
     }
 
     public static IrBoolExpr and(Collection<? extends IrBoolExpr> operands) {
         return and(operands.toArray(new IrBoolExpr[operands.size()]));
     }
 
     public static IrBoolExpr and(IrBoolExpr... operands) {
         List<IrBoolExpr> flatten = new ArrayList<>(operands.length);
         for (IrBoolExpr operand : operands) {
             if (operand instanceof IrAnd) {
                 // Invariant: No nested IrAnd
                 flatten.addAll(Arrays.asList(((IrAnd) operand).getOperands()));
             } else {
                 flatten.add(operand);
             }
         }
         List<IrBoolExpr> filter = new ArrayList<>(flatten.size());
         for (IrBoolExpr operand : flatten) {
             if (IrUtil.isFalse(operand)) {
                 return False;
             }
             if (!IrUtil.isTrue(operand)) {
                 filter.add(operand);
             }
         }
         switch (filter.size()) {
             case 0:
                 return True;
             case 1:
                 return filter.get(0);
             default:
                 return new IrAnd(filter.toArray(new IrBoolExpr[filter.size()]), BoolDomain);
         }
     }
 
     public static IrBoolExpr lone(Collection<? extends IrBoolExpr> operands) {
         return lone(operands.toArray(new IrBoolExpr[operands.size()]));
     }
 
     public static IrBoolExpr lone(IrBoolExpr... operands) {
         List<IrBoolExpr> filter = new ArrayList<>(operands.length);
         int count = 0;
         for (IrBoolExpr operand : operands) {
             if (IrUtil.isTrue(operand)) {
                 count++;
                 if (count > 1) {
                     return False;
                 }
             } else if (!IrUtil.isFalse(operand)) {
                 filter.add(operand);
             }
         }
         assert count == 0 || count == 1;
         switch (filter.size()) {
             case 0:
                 return True;
             case 1:
                 return count == 0 ? True : not(filter.get(0));
             default:
                 IrBoolExpr[] f = filter.toArray(new IrBoolExpr[filter.size()]);
                 return count == 0
                         ? new IrLone(f, BoolDomain)
                         : not(or(f));
         }
     }
 
     public static IrBoolExpr one(Collection<? extends IrBoolExpr> operands) {
         return one(operands.toArray(new IrBoolExpr[operands.size()]));
     }
 
     public static IrBoolExpr one(IrBoolExpr... operands) {
         List<IrBoolExpr> filter = new ArrayList<>(operands.length);
         int count = 0;
         for (IrBoolExpr operand : operands) {
             if (IrUtil.isTrue(operand)) {
                 count++;
                 if (count > 1) {
                     return False;
                 }
             } else if (!IrUtil.isFalse(operand)) {
                 filter.add(operand);
             }
         }
         assert count == 0 || count == 1;
         switch (filter.size()) {
             case 0:
                 return count == 0 ? False : True;
             case 1:
                 return count == 0 ? filter.get(0) : not(filter.get(0));
             case 2:
                 return count == 0
                         ? xor(filter.get(0), filter.get(1))
                         : and(not(filter.get(0)), not(filter.get(1)));
             default:
                 IrBoolExpr[] f = filter.toArray(new IrBoolExpr[filter.size()]);
                 return count == 0
                         ? new IrOne(f, BoolDomain)
                         : not(or(f));
         }
     }
 
     public static IrBoolExpr or(Collection<? extends IrBoolExpr> operands) {
         return or(operands.toArray(new IrBoolExpr[operands.size()]));
     }
 
     public static IrBoolExpr or(IrBoolExpr... operands) {
         List<IrBoolExpr> flatten = new ArrayList<>(operands.length);
         for (IrBoolExpr operand : operands) {
             if (operand instanceof IrOr) {
                 // Invariant: No nested IrOr
                 flatten.addAll(Arrays.asList(((IrOr) operand).getOperands()));
             } else {
                 flatten.add(operand);
             }
         }
         List<IrBoolExpr> filter = new ArrayList<>(flatten.size());
         for (IrBoolExpr operand : flatten) {
             if (IrUtil.isTrue(operand)) {
                 return True;
             }
             if (!IrUtil.isFalse(operand)) {
                 filter.add(operand);
             }
         }
         switch (filter.size()) {
             case 0:
                 return False;
             case 1:
                 return filter.get(0);
             default:
                 return new IrOr(filter.toArray(new IrBoolExpr[filter.size()]), BoolDomain);
         }
     }
 
     public static IrBoolExpr implies(IrBoolExpr antecedent, IrBoolExpr consequent) {
         if (antecedent.equals(consequent)) {
             return True;
         }
         if (IrUtil.isTrue(antecedent)) {
             return consequent;
         }
         if (IrUtil.isFalse(antecedent)) {
             return True;
         }
         if (IrUtil.isTrue(consequent)) {
             return True;
         }
         if (IrUtil.isFalse(consequent)) {
             return not(antecedent);
         }
         if (consequent instanceof IrImplies) {
             // a => (b => c) <=> !a or !b or c
             IrImplies consequentImplies = (IrImplies) consequent;
             return or(not(antecedent),
                     not(consequentImplies.getAntecedent()),
                     consequentImplies.getConsequent());
         }
         return new IrImplies(antecedent, consequent, BoolDomain);
     }
 
     public static IrBoolExpr notImplies(IrBoolExpr antecedent, IrBoolExpr consequent) {
         if (antecedent.equals(consequent)) {
             return False;
         }
         if (IrUtil.isTrue(antecedent)) {
             return not(consequent);
         }
         if (IrUtil.isFalse(antecedent)) {
             return False;
         }
         if (IrUtil.isTrue(consequent)) {
             return False;
         }
         if (IrUtil.isFalse(consequent)) {
             return antecedent;
         }
         return new IrNotImplies(antecedent, consequent, BoolDomain);
     }
 
     public static IrBoolExpr ifThenElse(IrBoolExpr antecedent, IrBoolExpr consequent, IrBoolExpr alternative) {
         if (IrUtil.isTrue(antecedent)) {
             return consequent;
         }
         if (IrUtil.isFalse(antecedent)) {
             return alternative;
         }
         if (IrUtil.isTrue(consequent)) {
             return or(antecedent, alternative);
         }
         if (IrUtil.isFalse(consequent)) {
             return and(antecedent.negate(), alternative);
         }
         if (IrUtil.isTrue(alternative)) {
             return or(antecedent.negate(), consequent);
         }
         if (IrUtil.isFalse(alternative)) {
             return and(antecedent, consequent);
         }
         return new IrIfThenElse(antecedent, consequent, alternative, BoolDomain);
     }
 
     public static IrBoolExpr ifOnlyIf(IrBoolExpr left, IrBoolExpr right) {
         if (left.equals(right)) {
             return True;
         }
         if (IrUtil.isTrue(left)) {
             return right;
         }
         if (IrUtil.isFalse(left)) {
             return not(right);
         }
         if (IrUtil.isTrue(right)) {
             return left;
         }
         if (IrUtil.isFalse(right)) {
             return not(left);
         }
         if (left instanceof IrNot) {
             return xor(((IrNot) left).getExpr(), right);
         }
         if (right instanceof IrNot) {
             return xor(left, ((IrNot) right).getExpr());
         }
         return new IrIfOnlyIf(left, right, BoolDomain);
     }
 
     public static IrBoolExpr xor(IrBoolExpr left, IrBoolExpr right) {
         if (IrUtil.isTrue(left)) {
             return not(right);
         }
         if (IrUtil.isFalse(left)) {
             return right;
         }
         if (IrUtil.isTrue(right)) {
             return not(left);
         }
         if (IrUtil.isFalse(right)) {
             return left;
         }
         if (left instanceof IrNot) {
             return ifOnlyIf(((IrNot) left).getExpr(), right);
         }
         if (right instanceof IrNot) {
             return ifOnlyIf(left, ((IrNot) right).getExpr());
         }
         return new IrXor(left, right, BoolDomain);
     }
 
     public static IrBoolExpr within(IrIntExpr value, IrDomain range) {
         IrDomain domain = value.getDomain();
         if (range.isBounded()
                 && domain.getLowBound() >= range.getLowBound()
                 && domain.getHighBound() <= range.getHighBound()) {
             return True;
        } else if (IrUtil.isSubsetOf(domain, range)) {
            return True;
         }
         if (domain.getLowBound() > range.getHighBound()
                 || domain.getHighBound() < range.getLowBound()) {
             return False;
         }
         if (range.size() == 1) {
             return equal(value, range.getLowBound());
         }
        if (value.getDomain().getLowBound() == range.getLowBound()
                && value.getDomain().getHighBound() - 1 == range.getHighBound()) {
            return notEqual(value, value.getDomain().getHighBound());
        }
        if (value.getDomain().getLowBound() + 1 == range.getLowBound()
                && value.getDomain().getHighBound() == range.getHighBound()) {
            return notEqual(value, value.getDomain().getLowBound());
         }
        return new IrWithin(value, range, BoolDomain);
     }
 
     public static IrBoolExpr notWithin(IrIntExpr value, IrDomain range) {
         IrDomain domain = value.getDomain();
         if (range.isBounded()
                 && domain.getLowBound() >= range.getLowBound()
                 && domain.getHighBound() <= range.getHighBound()) {
             return False;
         }
         if (domain.getLowBound() > range.getHighBound()
                 || domain.getHighBound() < range.getLowBound()) {
             return True;
         }
         if (range.size() == 1) {
             return notEqual(value, range.getLowBound());
         }
         return new IrNotWithin(value, range, BoolDomain);
     }
 
     public static IrBoolExpr compare(int left, IrCompare.Op op, IrIntExpr right) {
         return compare(constant(left), op, right);
     }
 
     public static IrBoolExpr compare(IrIntExpr left, IrCompare.Op op, int right) {
         return compare(left, op, constant(right));
     }
 
     public static IrBoolExpr compare(IrIntExpr left, IrCompare.Op op, IrIntExpr right) {
         IrDomain leftDomain = left.getDomain();
         IrDomain rightDomain = right.getDomain();
         switch (op) {
             case Equal:
                 if (left.equals(right)) {
                     return True;
                 }
                 if (!IrUtil.intersects(leftDomain, rightDomain)) {
                     return False;
                 }
                 if (left instanceof IrBoolExpr && right instanceof IrBoolExpr) {
                     return ifOnlyIf((IrBoolExpr) left, (IrBoolExpr) right);
                 }
                 break;
             case NotEqual:
                 if (left.equals(right)) {
                     return False;
                 }
                 if (!IrUtil.intersects(leftDomain, rightDomain)) {
                     return True;
                 }
                 if (left instanceof IrBoolExpr && right instanceof IrBoolExpr) {
                     return xor((IrBoolExpr) left, (IrBoolExpr) right);
                 }
                 break;
             case LessThan:
                 if (left.equals(right)) {
                     return False;
                 }
                 if (leftDomain.getHighBound() < rightDomain.getLowBound()) {
                     return True;
                 }
                 if (leftDomain.getLowBound() >= rightDomain.getHighBound()) {
                     return False;
                 }
                 if (left instanceof IrBoolExpr && right instanceof IrBoolExpr) {
                     return not(implies((IrBoolExpr) right, (IrBoolExpr) left));
                 }
                 if (left instanceof IrMinus && right instanceof IrMinus) {
                     return greaterThan(((IrMinus) left).getExpr(), ((IrMinus) right).getExpr());
                 }
                 break;
             case LessThanEqual:
                 if (left.equals(right)) {
                     return True;
                 }
                 if (leftDomain.getHighBound() <= rightDomain.getLowBound()) {
                     return True;
                 }
                 if (leftDomain.getLowBound() > rightDomain.getHighBound()) {
                     return False;
                 }
                 if (leftDomain.getLowBound() == rightDomain.getHighBound()) {
                     return equal(left, right);
                 }
                 if (left instanceof IrBoolExpr && right instanceof IrBoolExpr) {
                     return implies((IrBoolExpr) left, (IrBoolExpr) right);
                 }
                 if (left instanceof IrMinus && right instanceof IrMinus) {
                     return greaterThanEqual(((IrMinus) left).getExpr(), ((IrMinus) right).getExpr());
                 }
                 break;
             default:
                 throw new IllegalArgumentException("Unknown op: " + op);
         }
         return new IrCompare(left, op, right, BoolDomain);
     }
 
     public static IrBoolExpr equal(int left, IrIntExpr right) {
         return equal(constant(left), right);
     }
 
     public static IrBoolExpr equal(IrIntExpr left, int right) {
         return equal(left, constant(right));
     }
 
     public static IrBoolExpr equal(IrIntExpr left, IrIntExpr right) {
         return compare(left, IrCompare.Op.Equal, right);
     }
 
     public static IrBoolExpr equal(IrIntExpr[] left, IrIntExpr[] right) {
         if (left.length != right.length) {
             throw new IllegalArgumentException();
         }
         IrBoolExpr[] ands = new IrBoolExpr[left.length];
         for (int i = 0; i < ands.length; i++) {
             ands[i] = equal(left[i], right[i]);
         }
         return and(ands);
     }
 
     public static IrBoolExpr equality(IrSetExpr left, IrSetTest.Op op, IrSetExpr right) {
         switch (op) {
             case Equal:
                 if (left.equals(right)) {
                     return True;
                 }
                 if (!IrUtil.isSubsetOf(left.getKer(), right.getEnv())
                         || !IrUtil.isSubsetOf(right.getKer(), left.getEnv())) {
                     return False;
                 }
                 if (!IrUtil.intersects(left.getCard(), right.getCard())) {
                     return False;
                 }
                 int[] constant = IrUtil.getConstant(left);
                 if (constant != null) {
                     /*
                      * The idea is that integer constraints are easier to
                      * optimize than set constraints. If the expression is a top
                      * level expression than the cardinality propagator will
                      * optimize this expression away anyways.
                      */
                     if (constant.length == 0) {
                         return equal(card(right), 0);
                     }
                     if (constant.length == right.getEnv().size()) {
                         return IrUtil.containsAll(constant, right.getEnv())
                                 ? equal(card(right), constant.length) : False;
                     }
                 }
                 constant = IrUtil.getConstant(right);
                 if (constant != null) {
                     if (constant.length == 0) {
                         return equal(card(left), 0);
                     }
                     if (constant.length == left.getEnv().size()) {
                         return IrUtil.containsAll(constant, left.getEnv())
                                 ? equal(card(left), constant.length) : False;
                     }
                 }
                 IrIntExpr leftInt = IrUtil.asInt(left);
                 if (leftInt != null) {
                     IrIntExpr rightInt = IrUtil.asInt(right);
                     if (rightInt != null) {
                         return equal(leftInt, rightInt);
                     }
                 }
                 break;
             case NotEqual:
                 if (left.equals(right)) {
                     return False;
                 }
                 if (!IrUtil.isSubsetOf(left.getKer(), right.getEnv())
                         || !IrUtil.isSubsetOf(right.getKer(), left.getEnv())) {
                     return True;
                 }
                 if (!IrUtil.intersects(left.getCard(), right.getCard())) {
                     return True;
                 }
                 constant = IrUtil.getConstant(left);
                 if (constant != null) {
                     if (constant.length == 0) {
                         return notEqual(card(right), 0);
                     }
                     if (constant.length == right.getEnv().size()) {
                         return IrUtil.containsAll(constant, right.getEnv())
                                 ? notEqual(card(right), constant.length) : True;
                     }
                 }
                 constant = IrUtil.getConstant(right);
                 if (constant != null) {
                     if (constant.length == 0) {
                         return notEqual(card(left), 0);
                     }
                     if (constant.length == left.getEnv().size()) {
                         return IrUtil.containsAll(constant, left.getEnv())
                                 ? notEqual(card(left), constant.length) : True;
                     }
                 }
                 leftInt = IrUtil.asInt(left);
                 if (leftInt != null) {
                     IrIntExpr rightInt = IrUtil.asInt(right);
                     if (rightInt != null) {
                         return notEqual(leftInt, rightInt);
                     }
                 }
                 break;
             default:
                 throw new IllegalArgumentException();
         }
         return new IrSetTest(left, op, right, BoolDomain);
     }
 
     public static IrBoolExpr equal(IrSetExpr left, IrSetExpr right) {
         return equality(left, IrSetTest.Op.Equal, right);
     }
 
     public static IrBoolExpr notEqual(int left, IrIntExpr right) {
         return notEqual(constant(left), right);
     }
 
     public static IrBoolExpr notEqual(IrIntExpr left, int right) {
         return notEqual(left, constant(right));
     }
 
     public static IrBoolExpr notEqual(IrIntExpr left, IrIntExpr right) {
         return compare(left, IrCompare.Op.NotEqual, right);
     }
 
     public static IrBoolExpr notEqual(IrSetExpr left, IrSetExpr right) {
         return equality(left, IrSetTest.Op.NotEqual, right);
     }
 
     public static IrBoolExpr lessThan(int left, IrIntExpr right) {
         return lessThan(constant(left), right);
     }
 
     public static IrBoolExpr lessThan(IrIntExpr left, int right) {
         return lessThan(left, constant(right));
     }
 
     public static IrBoolExpr lessThan(IrIntExpr left, IrIntExpr right) {
         return compare(left, IrCompare.Op.LessThan, right);
     }
 
     public static IrBoolExpr lessThanEqual(int left, IrIntExpr right) {
         return lessThanEqual(constant(left), right);
     }
 
     public static IrBoolExpr lessThanEqual(IrIntExpr left, int right) {
         return lessThanEqual(left, constant(right));
     }
 
     public static IrBoolExpr lessThanEqual(IrIntExpr left, IrIntExpr right) {
         return compare(left, IrCompare.Op.LessThanEqual, right);
     }
 
     public static IrBoolExpr greaterThan(int left, IrIntExpr right) {
         return greaterThan(constant(left), right);
     }
 
     public static IrBoolExpr greaterThan(IrIntExpr left, int right) {
         return greaterThan(left, constant(right));
     }
 
     public static IrBoolExpr greaterThan(IrIntExpr left, IrIntExpr right) {
         return compare(right, IrCompare.Op.LessThan, left);
     }
 
     public static IrBoolExpr greaterThanEqual(int left, IrIntExpr right) {
         return greaterThanEqual(constant(left), right);
     }
 
     public static IrBoolExpr greaterThanEqual(IrIntExpr left, int right) {
         return greaterThanEqual(left, constant(right));
     }
 
     public static IrBoolExpr greaterThanEqual(IrIntExpr left, IrIntExpr right) {
         return compare(right, IrCompare.Op.LessThanEqual, left);
     }
 
     public static IrBoolExpr member(IrIntExpr element, IrSetExpr set) {
         if (IrUtil.isSubsetOf(element.getDomain(), set.getKer())) {
             return True;
         }
         if (!IrUtil.intersects(element.getDomain(), set.getEnv())) {
             return False;
         }
         if (IrUtil.isConstant(set)) {
             return within(element, set.getEnv());
         }
         return new IrMember(element, set, BoolDomain);
     }
 
     public static IrBoolExpr notMember(IrIntExpr element, IrSetExpr set) {
         if (!IrUtil.intersects(element.getDomain(), set.getEnv())) {
             return True;
         }
         if (IrUtil.isSubsetOf(element.getDomain(), set.getKer())) {
             return False;
         }
         if (IrUtil.isConstant(set)) {
             return notWithin(element, set.getEnv());
         }
         return new IrNotMember(element, set, BoolDomain);
     }
 
     public static IrBoolExpr subsetEq(IrSetExpr subset, IrSetExpr superset) {
         if (IrUtil.isSubsetOf(subset.getEnv(), superset.getKer())) {
             return True;
         }
         if (subset.getCard().getLowBound() == superset.getCard().getHighBound()) {
             return equal(subset, superset);
         }
         return new IrSubsetEq(subset, superset, BoolDomain);
     }
 
     public static IrBoolExpr boolChannel(IrBoolExpr[] bools, IrSetExpr set) {
         if (set.getEnv().isEmpty()
                 || (set.getEnv().getLowBound() >= 0 && set.getEnv().getHighBound() < bools.length)) {
             {
                 int[] constant = IrUtil.getConstant(set);
                 if (constant != null) {
                     IrBoolExpr[] ands = new IrBoolExpr[bools.length];
                     for (int i = 0; i < ands.length; i++) {
                         ands[i] = equal(bools[i], Util.in(i, constant) ? True : False);
                     }
                     return and(ands);
                 }
                 if (bools.length == 1) {
                     return equal(bools[0], card(set));
                 }
             }
             TIntHashSet values = new TIntHashSet();
             IrDomain env = set.getEnv();
             IrDomain ker = set.getKer();
             boolean entailed = true;
             for (int i = 0; i < bools.length; i++) {
                 Boolean constant = IrUtil.getConstant(bools[i]);
                 if (Boolean.TRUE.equals(constant)) {
                     if (values != null) {
                         values.add(i);
                     }
                     if (!env.contains(i)) {
                         return False;
                     }
                     if (!ker.contains(i)) {
                         entailed = false;
                     }
                 } else if (Boolean.FALSE.equals(constant)) {
                     if (ker.contains(i)) {
                         return False;
                     }
                     if (env.contains(i)) {
                         entailed = false;
                     }
                 } else {
                     values = null;
                     entailed = false;
                 }
             }
             if (entailed) {
                 return True;
             }
             if (values != null) {
                 return equal(set, constant(enumDomain(values)));
             }
         }
         return new IrBoolChannel(bools, set, BoolDomain);
     }
 
     public static IrBoolExpr intChannel(IrIntExpr[] ints, IrSetExpr[] sets) {
         boolean entailed = true;
         for (int i = 0; i < ints.length; i++) {
             Integer constant = IrUtil.getConstant(ints[i]);
             if (constant != null) {
                 IrSetExpr set = sets[constant.intValue()];
                 if (!set.getEnv().contains(i)) {
                     return False;
                 } else if (!set.getKer().contains(i)) {
                     entailed = false;
                 }
             } else {
                 entailed = false;
             }
         }
         if (entailed) {
             return True;
         }
         return new IrIntChannel(ints, sets, BoolDomain);
     }
 
     public static IrBoolExpr sort(IrIntExpr... array) {
         if (array.length <= 1) {
             return True;
         }
         IrBoolExpr[] sort = new IrBoolExpr[array.length - 1];
         for (int i = 0; i < array.length - 1; i++) {
             sort[i] = lessThanEqual(array[i], array[i + 1]);
         }
         return and(sort);
     }
 
     public static IrBoolExpr sortStrict(IrIntExpr... array) {
         if (array.length <= 1) {
             return True;
         }
         IrBoolExpr[] sort = new IrBoolExpr[array.length - 1];
         for (int i = 0; i < array.length - 1; i++) {
             sort[i] = lessThan(array[i], array[i + 1]);
         }
         return and(sort);
     }
 
     public static IrBoolExpr sort(IrSetExpr... sets) {
         List<IrSetExpr> filter = new ArrayList<>(sets.length);
         boolean fixedCard = true;
         for (IrSetExpr set : sets) {
             if (!set.getEnv().isEmpty()) {
                 filter.add(set);
                 fixedCard = fixedCard && set.getCard().size() == 1;
             }
         }
         if (filter.isEmpty()) {
             return True;
         }
         if (filter.size() == 1) {
             IrDomain env = filter.get(0).getEnv();
             IrDomain ker = filter.get(0).getKer();
             if (env.getLowBound() == 0) {
                 int i;
                 for (i = 0; i < env.getHighBound(); i++) {
                     if (!ker.contains(i)) {
                         break;
                     }
                 }
                 if (i == env.getHighBound()) {
                     // env = [0,1,...,n]
                     // ker = [0,1,...,n] or [0,1,...,n-1]
                     return True;
                 }
             }
         }
         if (fixedCard) {
             List<IrBoolExpr> ands = new ArrayList<>();
             int i = 0;
             for (IrSetExpr set : filter) {
                 assert set.getCard().size() == 1;
                 int card = set.getCard().getLowBound();
                 ands.add(equal(set, constant(Util.fromTo(i, i + card))));
                 i += card;
             }
             return and(ands);
         }
         return new IrSortSets(filter.toArray(new IrSetExpr[filter.size()]), BoolDomain);
     }
 
     private static IrBoolExpr sortStrings(IrIntExpr[][] strings, boolean strict) {
         List<IrIntExpr[]> filterStrings = new ArrayList<>(strings.length);
         for (int i = 0; i < strings.length - 1; i++) {
             switch (IrUtil.compareString(strings[i], strings[i + 1])) {
                 case EQ:
                 case LE:
                     if (!strict) {
                         break;
                     }
                 // fallthrough
                 case GT:
                 case GE:
                 case UNKNOWN:
                     filterStrings.add(strings[i]);
             }
         }
         filterStrings.add(strings[strings.length - 1]);
         IrIntExpr[][] fstrings = filterStrings.toArray(new IrIntExpr[filterStrings.size()][]);
 
         if (fstrings.length < 2) {
             return True;
         }
 
         IrIntExpr[] array = new IrIntExpr[fstrings.length];
         for (int i = 0; i < fstrings.length; i++) {
             IrIntExpr[] string = fstrings[i];
             if (string.length != 1) {
                 return new IrSortStrings(fstrings, strict, BoolDomain);
             }
             array[i] = fstrings[i][0];
         }
         return strict ? sortStrict(array) : sort(array);
     }
 
     public static IrBoolExpr sort(IrIntExpr[]... strings) {
         return sortStrings(strings, false);
     }
 
     public static IrBoolExpr sortStrict(IrIntExpr[]... strings) {
         return sortStrings(strings, true);
     }
 
     public static IrBoolExpr sortChannel(IrIntExpr[][] strings, IrIntExpr[] ints) {
         if (strings.length != ints.length) {
             throw new IllegalArgumentException();
         }
         for (int i = 1; i < strings.length; i++) {
             if (strings[0].length != strings[i].length) {
                 throw new IllegalArgumentException();
             }
         }
         List<IrIntExpr[]> filterStrings = new ArrayList<>(strings.length);
         List<IrIntExpr> filterInts = new ArrayList<>(ints.length);
         for (int i = 0; i < ints.length; i++) {
             boolean equivalence = false;
             for (int j = i + 1; j < ints.length; j++) {
                 if (IrUtil.Ordering.EQ.equals(IrUtil.compare(ints[i], ints[j]))
                         && IrUtil.Ordering.EQ.equals(IrUtil.compareString(strings[i], strings[j]))) {
                     equivalence = true;
                     break;
                 }
             }
             if (!equivalence) {
                 filterStrings.add(strings[i]);
                 filterInts.add(ints[i]);
             }
         }
         assert !filterInts.isEmpty();
         if (filterInts.size() == 1) {
             return equal(filterInts.get(0), 0);
         }
         IrIntExpr[][] fstrings = filterStrings.toArray(new IrIntExpr[filterStrings.size()][]);
         IrIntExpr[] fints = filterInts.toArray(new IrIntExpr[filterInts.size()]);
         int[] constant = IrUtil.getConstant(fints);
         if (constant != null) {
             IrIntExpr[][] partialOrdering = new IrIntExpr[constant.length][];
             for (int i = 0; i < constant.length; i++) {
                 int val = constant[i];
                 if (val < 0 || val >= constant.length) {
                     return False;
                 }
                 if (partialOrdering[constant[i]] != null) {
                     throw new IllegalStateException();
                 }
                 partialOrdering[constant[i]] = fstrings[i];
             }
             return sortStrict(partialOrdering);
         }
         return new IrSortStringsChannel(fstrings, fints, BoolDomain);
     }
 
     public static IrBoolExpr allDifferent(IrIntExpr[] ints) {
         if (ints.length < 2) {
             return True;
         }
         if (ints.length == 2) {
             return notEqual(ints[0], ints[1]);
         }
         IrDomain domain = ints[0].getDomain();
         int size = ints[0].getDomain().size();
         for (int i = 1; i < ints.length; i++) {
             domain = IrUtil.union(domain, ints[i].getDomain());
             size += ints[i].getDomain().size();
             if (size != domain.size()) {
                 return new IrAllDifferent(ints, BoolDomain);
             }
         }
         return True;
     }
 
     public static IrBoolExpr selectN(IrBoolExpr[] bools, IrIntExpr n) {
         if (bools.length == 1) {
             if (bools[0].equals(n)) {
                 return True;
             }
         }
         boolean entailed = true;
         IrDomain nDomain = n.getDomain();
         for (int i = 0; i < bools.length; i++) {
             Boolean constant = IrUtil.getConstant(bools[i]);
             if (Boolean.TRUE.equals(constant)) {
                 if (i >= nDomain.getHighBound()) {
                     return False;
                 }
                 if (i >= nDomain.getLowBound()) {
                     entailed = false;
                 }
             } else if (Boolean.FALSE.equals(constant)) {
                 if (i < nDomain.getLowBound()) {
                     return False;
                 }
                 if (i < nDomain.getHighBound()) {
                     entailed = false;
                 }
             } else {
                 entailed = false;
             }
         }
         if (entailed) {
             return True;
         }
         Integer constant = IrUtil.getConstant(n);
         if (constant != null) {
             IrBoolExpr[] ands = new IrBoolExpr[bools.length];
             System.arraycopy(bools, 0, ands, 0, constant.intValue());
             for (int i = constant.intValue(); i < bools.length; i++) {
                 ands[i] = not(bools[i]);
             }
             return and(ands);
         }
         return new IrSelectN(bools, n, BoolDomain);
     }
 
     public static IrBoolExpr acyclic(IrIntExpr[] edges) {
         return new IrAcyclic(edges, BoolDomain);
     }
 
     public static IrBoolExpr unreachable(IrIntExpr[] edges, int from, int to) {
         return new IrUnreachable(edges, from, to, BoolDomain);
     }
 
     public static IrBoolExpr filterString(IrSetExpr set, IrIntExpr[] string, IrIntExpr[] result) {
         if (set.getEnv().isEmpty()) {
             return filterString(set, 0, new IrIntExpr[0], result);
         }
         int offset = set.getEnv().getLowBound();
         int end = set.getEnv().getHighBound();
         return filterString(set, offset,
                 Arrays.copyOfRange(string, offset, end + 1),
                 result);
     }
 
     public static IrBoolExpr filterString(IrSetExpr set, int offset, IrIntExpr[] string, IrIntExpr[] result) {
         int[] constant = IrUtil.getConstant(set);
         if (constant != null) {
             IrBoolExpr[] ands = new IrBoolExpr[result.length];
             for (int i = 0; i < constant.length; i++) {
                 ands[i] = equal(string[constant[i] - offset], result[i]);
             }
             for (int i = constant.length; i < result.length; i++) {
                 ands[i] = equal(result[i], -1);
             }
             return and(ands);
         }
         IrIntExpr[] filterString = Arrays.copyOf(string, string.length);
         IrIntExpr[] filterResult = Arrays.copyOf(result, result.length);
         TIntIterator iter = set.getEnv().iterator();
         int i = 0;
         while (iter.hasNext()) {
             int env = iter.next();
             int x = env - offset;
             if (!set.getKer().contains(env) || !filterString[x].equals(filterResult[i])) {
                 break;
             }
             filterString[x] = Zero;
             filterResult[i] = Zero;
             i++;
         }
         int cut = filterResult.length;
         while (cut > 0 && Integer.valueOf(-1).equals(IrUtil.getConstant(filterResult[cut - 1]))) {
             cut--;
         }
         if (cut != filterResult.length) {
             filterResult = Arrays.copyOf(filterResult, cut);
         }
         return new IrFilterString(set, offset, filterString, filterResult, BoolDomain);
     }
     /**
      *******************
      *
      * Integer
      *
      *******************
      */
     public static IrIntVar Zero = False;
     public static IrIntVar One = True;
 
     public static IrIntVar domainInt(String name, IrDomain domain) {
         if (domain.size() == 1) {
             return constant(domain.getLowBound());
         }
         if (domain instanceof IrBoolDomain) {
             return new IrBoolVar(name, (IrBoolDomain) domain);
         }
         return new IrIntVar(name, domain);
     }
 
     public static IrIntVar boundInt(String name, int low, int high) {
         return domainInt(name, boundDomain(low, high));
     }
 
     public static IrIntVar enumInt(String name, int[] values) {
         return domainInt(name, enumDomain(values));
     }
 
     public static IrIntVar constant(int value) {
         switch (value) {
             case 0:
                 return Zero;
             case 1:
                 return One;
             default:
                 return new IrIntConstant(value);
         }
     }
 
     public static IrIntExpr minus(IrIntExpr expr) {
         Integer constant = IrUtil.getConstant(expr);
         if (constant != null) {
             return constant(-constant.intValue());
         }
         if (expr instanceof IrMinus) {
             IrMinus minus = (IrMinus) expr;
             return minus.getExpr();
         }
         return new IrMinus(expr, IrUtil.minus(expr.getDomain()));
 
     }
 
     public static IrIntExpr card(IrSetExpr set) {
         IrDomain domain = set.getCard();
         if (domain.size() == 1) {
             return constant(domain.getLowBound());
         }
         return new IrCard(set, domain);
     }
 
     public static IrIntExpr add(int addend1, IrIntExpr addend2) {
         return add(constant(addend1), addend2);
     }
 
     public static IrIntExpr add(IrIntExpr addend1, int addend2) {
         return add(addend1, constant(addend2));
     }
 
     public static IrIntExpr add(Collection<? extends IrIntExpr> addends) {
         return add(addends.toArray(new IrIntExpr[addends.size()]));
     }
 
     public static IrIntExpr add(IrIntExpr... addends) {
         int constants = 0;
         List<IrIntExpr> filter = new ArrayList<>(addends.length);
         for (IrIntExpr addend : addends) {
             if (addend instanceof IrAdd) {
                 IrAdd add = (IrAdd) addend;
                 // Invariant: No nested IrAdd or constants
                 filter.addAll(Arrays.asList(add.getAddends()));
                 constants += add.getOffset();
             } else {
                 Integer constant = IrUtil.getConstant(addend);
                 if (constant != null) {
                     constants += constant.intValue();
                 } else {
                     filter.add(addend);
                 }
             }
         }
         if (filter.isEmpty()) {
             return constant(constants);
         }
         if (filter.size() == 1) {
             IrIntExpr first = filter.get(0);
             if (constants == 0) {
                 return first;
             }
             return new IrAdd(new IrIntExpr[]{first}, constants,
                     IrUtil.offset(first.getDomain(), constants));
         }
         int low = constants;
         int high = constants;
         for (IrIntExpr addend : filter) {
             low += addend.getDomain().getLowBound();
             high += addend.getDomain().getHighBound();
         }
         IrDomain domain = boundDomain(low, high);
         return new IrAdd(filter.toArray(new IrIntExpr[filter.size()]), constants, domain);
     }
 
     public static IrIntExpr sub(int minuend, IrIntExpr subtrahend) {
         return sub(constant(minuend), subtrahend);
     }
 
     public static IrIntExpr sub(IrIntExpr minuend, int subtrahend) {
         return sub(minuend, constant(subtrahend));
     }
 
     public static IrIntExpr sub(Collection<? extends IrIntExpr> subtrahends) {
         return sub(subtrahends.toArray(new IrIntExpr[subtrahends.size()]));
     }
 
     public static IrIntExpr sub(IrIntExpr... subtrahends) {
         if (subtrahends.length == 0) {
             return Zero;
         }
         IrIntExpr[] flip = new IrIntExpr[subtrahends.length];
         flip[0] = subtrahends[0];
         for (int i = 1; i < flip.length; i++) {
             flip[i] = minus(subtrahends[i]);
         }
         return add(flip);
     }
 
     public static IrIntExpr mul(int multiplicand, IrIntExpr multiplier) {
         return mul(constant(multiplicand), multiplier);
     }
 
     public static IrIntExpr mul(IrIntExpr multiplicand, int multiplier) {
         return mul(multiplicand, constant(multiplier));
     }
 
     public static IrIntExpr mul(IrIntExpr multiplicand, IrIntExpr multiplier) {
         Integer multiplicandConstant = IrUtil.getConstant(multiplicand);
         Integer multiplierConstant = IrUtil.getConstant(multiplier);
         if (multiplicandConstant != null) {
             switch (multiplicandConstant.intValue()) {
                 case 0:
                     return multiplicand;
                 case 1:
                     return multiplier;
             }
         }
         if (multiplierConstant != null) {
             switch (multiplierConstant.intValue()) {
                 case 0:
                     return multiplier;
                 case 1:
                     return multiplicand;
             }
         }
         if (multiplicandConstant != null && multiplierConstant != null) {
             return constant(multiplicandConstant.intValue() * multiplierConstant.intValue());
         }
         int low1 = multiplicand.getDomain().getLowBound();
         int high1 = multiplicand.getDomain().getHighBound();
         int low2 = multiplier.getDomain().getLowBound();
         int high2 = multiplier.getDomain().getHighBound();
         int min = Util.min(low1 * low2, low1 * high2, high1 * low2, high1 * high2);
         int max = Util.max(low1 * low2, low1 * high2, high1 * low2, high1 * high2);
         return new IrMul(multiplicand, multiplier, boundDomain(min, max));
     }
 
     public static IrIntExpr div(int dividend, IrIntExpr divisor) {
         return div(constant(dividend), divisor);
     }
 
     public static IrIntExpr div(IrIntExpr dividend, int divisor) {
         return div(dividend, constant(divisor));
     }
 
     public static IrIntExpr div(IrIntExpr dividend, IrIntExpr divisor) {
         Integer dividendConstant = IrUtil.getConstant(dividend);
         Integer divisorConstant = IrUtil.getConstant(divisor);
         if (dividendConstant != null && dividendConstant.intValue() == 0) {
             return dividend;
         }
         if (divisorConstant != null && divisorConstant.intValue() == 1) {
             return dividend;
         }
         if (dividendConstant != null && divisorConstant != null) {
             return constant(dividendConstant.intValue() / divisorConstant.intValue());
         }
         int low1 = dividend.getDomain().getLowBound();
         int high1 = dividend.getDomain().getHighBound();
         int low2 = divisor.getDomain().getLowBound();
         int high2 = divisor.getDomain().getHighBound();
         int min = Util.min(low1, -low1, high1, -high1);
         int max = Util.max(low1, -low1, high1, -high1);
         return new IrDiv(dividend, divisor, boundDomain(min, max));
     }
 
     public static IrIntExpr element(IrIntExpr[] array, IrIntExpr index) {
         IrIntExpr[] $array = index.getDomain().getHighBound() + 1 < array.length
                 ? Arrays.copyOf(array, index.getDomain().getHighBound() + 1)
                 : array.clone();
         for (int i = 0; i < $array.length; i++) {
             if (!index.getDomain().contains(i)) {
                 $array[i] = Zero;
             }
         }
 
         Integer constant = IrUtil.getConstant(index);
         if (constant != null) {
             return $array[constant.intValue()];
         }
         TIntIterator iter = index.getDomain().iterator();
         assert iter.hasNext();
 
         IrDomain domain = $array[iter.next()].getDomain();
         int low = domain.getLowBound();
         int high = domain.getHighBound();
         while (iter.hasNext()) {
             int val = iter.next();
             if (val < $array.length) {
                 domain = $array[val].getDomain();
                 low = Math.min(low, domain.getLowBound());
                 high = Math.max(high, domain.getHighBound());
             }
         }
         domain = boundDomain(low, high);
         return new IrElement($array, index, domain);
     }
 
     public static IrIntExpr count(int value, IrIntExpr[] array) {
         List<IrIntExpr> filter = new ArrayList<>();
         int count = 0;
         for (IrIntExpr i : array) {
             Integer constant = IrUtil.getConstant(i);
             if (constant != null) {
                 if (constant.intValue() == value) {
                     count++;
                 }
             } else if (i.getDomain().contains(value)) {
                 filter.add(i);
             }
         }
         switch (filter.size()) {
             case 0:
                 return constant(count);
             case 1:
                 return add(equal(value, filter.get(0)), count);
             default:
                 return add(
                         new IrCount(value, filter.toArray(new IrIntExpr[filter.size()]), boundDomain(0, filter.size())),
                         count);
         }
     }
 
     public static IrIntExpr sum(IrSetExpr set) {
         int sum = Util.sum(set.getKer().iterator());
         int count = set.getKer().size();
 
         // Calculate low
         int low = sum;
         int lowCount = count;
         TIntIterator envIter = set.getEnv().iterator();
         while (lowCount < set.getCard().getHighBound() && envIter.hasNext()) {
             int env = envIter.next();
             if (env >= 0 && lowCount >= set.getCard().getLowBound()) {
                 break;
             }
             if (!set.getKer().contains(env)) {
                 low += env;
                 lowCount++;
             }
         }
 
         // Calculate high
         int high = sum;
         int highCount = count;
         envIter = set.getEnv().iterator(false);
         while (highCount < set.getCard().getHighBound() && envIter.hasNext()) {
             int env = envIter.next();
             if (env <= 0 && highCount >= set.getCard().getLowBound()) {
                 break;
             }
             if (!set.getKer().contains(env)) {
                 high += env;
                 highCount++;
             }
         }
 
         return new IrSetSum(set, boundDomain(low, high));
     }
 
     public static IrIntExpr ternary(IrBoolExpr antecedent, IrIntExpr consequent, IrIntExpr alternative) {
         if (IrUtil.isTrue(antecedent)) {
             return consequent;
         }
         if (IrUtil.isFalse(antecedent)) {
             return alternative;
         }
         if (consequent.equals(alternative)) {
             return consequent;
         }
         Integer consequentConstant = IrUtil.getConstant(consequent);
         Integer alternativeConstant = IrUtil.getConstant(alternative);
         if (consequentConstant != null && consequentConstant.equals(alternativeConstant)) {
             return constant(consequentConstant);
         }
         IrDomain domain = IrUtil.union(consequent.getDomain(), alternative.getDomain());
         return new IrTernary(antecedent, consequent, alternative, domain);
     }
 
     /**
      *******************
      *
      * Set
      *
      *******************
      */
     public static IrSetVar set(String name, int lowEnv, int highEnv) {
         return set(name, boundDomain(lowEnv, highEnv));
     }
 
     public static IrSetVar set(String name, int lowEnv, int highEnv, int lowKer, int highKer) {
         return set(name, boundDomain(lowEnv, highEnv), boundDomain(lowKer, highKer));
     }
 
     public static IrSetVar set(String name, int lowEnv, int highEnv, int[] ker) {
         return set(name, boundDomain(lowEnv, highEnv), enumDomain(ker));
     }
 
     public static IrSetVar set(String name, int[] env) {
         return set(name, enumDomain(env));
     }
 
     public static IrSetVar set(String name, int[] env, int lowKer, int highKer) {
         return set(name, enumDomain(env), boundDomain(lowKer, highKer));
     }
 
     public static IrSetVar set(String name, int[] env, int[] ker) {
         return set(name, enumDomain(env), enumDomain(ker));
     }
 
     public static IrSetVar set(String name, IrDomain env) {
         return set(name, env, EmptyDomain);
     }
 
     public static IrSetVar set(String name, IrDomain env, IrDomain ker) {
         return set(name, env, ker, boundDomain(ker.size(), env.size()));
     }
 
     public static IrSetVar set(String name, IrDomain env, IrDomain ker, IrDomain card) {
         return IrUtil.asConstant(new IrSetVar(name, env, ker, card));
     }
 
     public static IrSetVar constant(int[] value) {
         return constant(enumDomain(value));
     }
 
     public static IrSetVar constant(TIntCollection value) {
         return constant(enumDomain(value));
     }
 
     public static IrSetVar constant(TIntSet value) {
         return constant(enumDomain(value));
     }
 
     public static IrSetVar constant(IrDomain value) {
         if (value.isEmpty()) {
             return EmptySet;
         }
         return new IrSetConstant(value);
     }
 
     public static IrSetExpr singleton(IrIntExpr value) {
         Integer constant = IrUtil.getConstant(value);
         if (constant != null) {
             return constant(new int[]{constant.intValue()});
         }
         return new IrSingleton(value, value.getDomain(), EmptyDomain);
     }
 
     public static IrSetExpr arrayToSet(IrIntExpr[] array, Integer globalCardinality) {
         switch (array.length) {
             case 0:
                 return EmptySet;
             case 1:
                 return singleton(array[0]);
             default:
                 IrDomain env = array[0].getDomain();
                 for (int i = 1; i < array.length; i++) {
                     env = IrUtil.union(env, array[i].getDomain());
                 }
                 TIntSet values = new TIntHashSet();
                 for (IrIntExpr i : array) {
                     Integer constant = IrUtil.getConstant(i);
                     if (constant != null) {
                         values.add(constant.intValue());
                     }
                 }
                 IrDomain ker = enumDomain(values);
                 int lowCard = Math.max(
                         globalCardinality == null ? 1 : divRoundUp(array.length, globalCardinality),
                         ker.size());
                 int highCard = Math.min(array.length, env.size());
                 IrDomain card = boundDomain(lowCard, highCard);
                 return IrUtil.asConstant(new IrArrayToSet(array, env, ker, card, globalCardinality));
         }
     }
 
     /**
      * Relational join.
      *
      * Union{for all i in take} children[i]
      *
      * @param take
      * @param children
      * @param injective
      * @return the join expression take.children
      */
     public static IrSetExpr joinRelation(IrSetExpr take, IrSetExpr[] children, boolean injective) {
         if (take.getEnv().isEmpty()) {
             return EmptySet;
         }
         IrSetExpr[] $children = take.getEnv().getHighBound() + 1 < children.length
                 ? Arrays.copyOf(children, take.getEnv().getHighBound() + 1)
                 : children.clone();
         for (int i = 0; i < $children.length; i++) {
             if (!take.getEnv().contains(i)) {
                 $children[i] = EmptySet;
             }
         }
 
         IrIntExpr[] ints = IrUtil.asInts(children);
         if (ints != null) {
             return joinFunction(take, ints, injective ? 1 : 0);
         }
 
         int[] constant = IrUtil.getConstant(take);
         if (constant != null) {
             IrSetExpr[] to = new IrSetExpr[constant.length];
             for (int i = 0; i < to.length; i++) {
                 to[i] = $children[constant[i]];
             }
             return union(to, injective);
         }
 
         // Compute env
         TIntIterator iter = take.getEnv().iterator();
         IrDomain env;
         if (iter.hasNext()) {
             IrDomain domain = $children[iter.next()].getEnv();
             while (iter.hasNext()) {
                 domain = IrUtil.union(domain, $children[iter.next()].getEnv());
             }
             env = domain;
         } else {
             env = EmptyDomain;
         }
 
         // Compute ker
         iter = take.getKer().iterator();
         IrDomain ker;
         if (iter.hasNext()) {
             IrDomain domain = $children[iter.next()].getKer();
             while (iter.hasNext()) {
                 domain = IrUtil.union(domain, $children[iter.next()].getKer());
             }
             ker = domain;
         } else {
             ker = EmptyDomain;
         }
 
         // Compute card
         IrDomain takeEnv = take.getEnv();
         IrDomain takeKer = take.getKer();
         IrDomain takeCard = take.getCard();
         int index = 0;
         int[] childrenLowCards = new int[takeEnv.size() - takeKer.size()];
         int[] childrenHighCards = new int[takeEnv.size() - takeKer.size()];
         int cardLow = 0, cardHigh = 0;
 
         iter = takeEnv.iterator();
         while (iter.hasNext()) {
             int val = iter.next();
             IrDomain childDomain = $children[val].getCard();
             if (takeKer.contains(val)) {
                 cardLow = injective
                         ? cardLow + childDomain.getLowBound()
                         : Math.max(cardLow, childDomain.getLowBound());
                 cardHigh = injective
                         ? cardHigh + childDomain.getHighBound()
                         : Math.max(cardHigh, childDomain.getHighBound());
             } else {
                 childrenLowCards[index] = childDomain.getLowBound();
                 childrenHighCards[index] = childDomain.getHighBound();
                 index++;
             }
         }
         assert index == childrenLowCards.length;
         assert index == childrenHighCards.length;
 
         Arrays.sort(childrenLowCards);
         Arrays.sort(childrenHighCards);
 
         for (int i = 0; i < takeCard.getLowBound() - takeKer.size(); i++) {
             cardLow = injective
                     ? cardLow + childrenLowCards[i]
                     : Math.max(cardLow, childrenLowCards[i]);
         }
         for (int i = 0; i < takeCard.getHighBound() - takeKer.size(); i++) {
             cardHigh = injective
                     ? cardHigh + childrenHighCards[childrenHighCards.length - 1 - i]
                     : Math.max(cardHigh, childrenHighCards[childrenHighCards.length - 1 - i]);
         }
         cardLow = Math.max(cardLow, ker.size());
         cardHigh = Math.min(cardHigh, env.size());
         IrDomain card = boundDomain(cardLow, cardHigh);
 
         return new IrJoinRelation(take, $children, env, ker, card, injective);
     }
 
     public static IrSetExpr joinFunction(IrSetExpr take, IrIntExpr[] refs, Integer globalCardinality) {
         if (take.getEnv().isEmpty()) {
             return EmptySet;
         }
         IrIntExpr[] $refs = take.getEnv().getHighBound() + 1 < refs.length
                 ? Arrays.copyOf(refs, take.getEnv().getHighBound() + 1)
                 : refs.clone();
         for (int i = 0; i < $refs.length; i++) {
             if (!take.getEnv().contains(i)) {
                 $refs[i] = Zero;
             }
         }
 
         int[] constant = IrUtil.getConstant(take);
         if (constant != null) {
             IrIntExpr[] to = new IrIntExpr[constant.length];
             for (int i = 0; i < to.length; i++) {
                 to[i] = $refs[constant[i]];
             }
             return arrayToSet(to, globalCardinality);
         }
 
         // Compute env
         TIntIterator iter = take.getEnv().iterator();
         IrDomain env;
         if (iter.hasNext()) {
             IrDomain domain = $refs[iter.next()].getDomain();
             while (iter.hasNext()) {
                 domain = IrUtil.union(domain, $refs[iter.next()].getDomain());
             }
             env = domain;
         } else {
             env = EmptyDomain;
         }
 
         // Compute ker
         iter = take.getKer().iterator();
         TIntHashSet values = new TIntHashSet(0);
         while (iter.hasNext()) {
             Integer constantRef = IrUtil.getConstant($refs[iter.next()]);
             if (constantRef != null) {
                 values.add(constantRef.intValue());
             }
         }
         IrDomain ker = values.isEmpty() ? EmptyDomain : new IrEnumDomain(values.toArray());
 
         // Compute card
         IrDomain takeCard = take.getCard();
         int lowTakeCard = takeCard.getLowBound();
         int highTakeCard = takeCard.getHighBound();
         IrDomain card;
         if (globalCardinality == null) {
             card = lowTakeCard == 0
                     ? boundDomain(Math.max(0, ker.size()), Math.min(highTakeCard, env.size()))
                     : boundDomain(Math.max(1, ker.size()), Math.min(highTakeCard, env.size()));
         } else {
             card = boundDomain(
                     divRoundUp(Math.max(lowTakeCard, ker.size()), globalCardinality),
                     Math.min(highTakeCard, env.size()));
         }
 
         return new IrJoinFunction(take, $refs, env, ker, card, globalCardinality);
     }
 
     private static int divRoundUp(int a, int b) {
         assert a >= 0;
         assert b > 0;
 
         return (a + b - 1) / b;
     }
 
     public static IrSetExpr difference(IrSetExpr minuend, IrSetExpr subtrahend) {
         IrDomain env = IrUtil.difference(minuend.getEnv(), subtrahend.getKer());
         IrDomain ker = IrUtil.difference(minuend.getKer(), subtrahend.getEnv());
         int low = Math.max(0, minuend.getCard().getLowBound() - subtrahend.getCard().getHighBound());
         int high = minuend.getCard().getHighBound();
         IrDomain card = boundDomain(Math.max(low, ker.size()), Math.min(high, env.size()));
         return new IrSetDifference(minuend, subtrahend, env, ker, card);
     }
 
     public static IrSetExpr intersection(IrSetExpr... operands) {
         List<IrSetExpr> flatten = new ArrayList<>(operands.length);
         for (IrSetExpr operand : operands) {
             if (operand instanceof IrSetIntersection) {
                 // Invariant: No nested IrSetIntersection
                 flatten.addAll(Arrays.asList(((IrSetIntersection) operand).getOperands()));
             } else {
                 flatten.add(operand);
             }
         }
         TIntSet constants = null;
         List<IrSetExpr> filter = new ArrayList<>();
         for (IrSetExpr operand : flatten) {
             int[] constant = IrUtil.getConstant(operand);
             if (constant == null) {
                 filter.add(operand);
             } else {
                 if (constants == null) {
                     constants = new TIntHashSet(constant);
                 } else {
                     constants.retainAll(constant);
                 }
             }
         }
         if (constants != null) {
             filter.add(constant(constants));
         }
         IrSetExpr[] ops = filter.toArray(new IrSetExpr[filter.size()]);
         switch (ops.length) {
             case 0:
                 return EmptySet;
             case 1:
                 return ops[0];
             default:
                 IrDomain env = IrUtil.intersectionEnvs(ops);
                 IrDomain ker = IrUtil.intersectionKers(ops);
                 int low = 0;
                 int high = ops[0].getCard().getHighBound();
                 for (int i = 1; i < ops.length; i++) {
                     high = Math.max(high, ops[0].getCard().getHighBound());
                 }
                 IrDomain card = boundDomain(
                         Math.max(low, ker.size()),
                         Math.min(high, env.size()));
                 return new IrSetIntersection(ops, env, ker, card);
         }
     }
 
     public static IrSetExpr union(IrSetExpr... operands) {
         return union(operands, false);
     }
 
     public static IrSetExpr union(IrSetExpr[] operands, boolean disjoint) {
         List<IrSetExpr> flatten = new ArrayList<>(operands.length);
         for (IrSetExpr operand : operands) {
             if (operand instanceof IrSetUnion) {
                 // Invariant: No nested IrSetUnion
                 flatten.addAll(Arrays.asList(((IrSetUnion) operand).getOperands()));
             } else {
                 flatten.add(operand);
             }
         }
         TIntSet constants = new TIntHashSet();
         List<IrSetExpr> filter = new ArrayList<>();
         for (IrSetExpr operand : flatten) {
             int[] constant = IrUtil.getConstant(operand);
             if (constant == null) {
                 filter.add(operand);
             } else {
                 constants.addAll(constant);
             }
         }
         if (!constants.isEmpty()) {
             filter.add(constant(constants));
         }
         IrSetExpr[] ops = filter.toArray(new IrSetExpr[filter.size()]);
         switch (ops.length) {
             case 0:
                 return EmptySet;
             case 1:
                 return ops[0];
             default:
                 IrDomain env = IrUtil.unionEnvs(ops);
                 IrDomain ker = IrUtil.unionKers(ops);
                 IrDomain operandCard = ops[0].getCard();
                 int low = operandCard.getLowBound();
                 int high = operandCard.getHighBound();
                 for (int i = 1; i < ops.length; i++) {
                     operandCard = ops[i].getCard();
                     low = disjoint
                             ? low + operandCard.getLowBound()
                             : Math.max(low, operandCard.getLowBound());
                     high += operandCard.getHighBound();
                 }
                 IrDomain card = boundDomain(
                         Math.max(low, ker.size()),
                         Math.min(high, env.size()));
                 return IrUtil.asConstant(new IrSetUnion(ops, env, ker, card, disjoint));
         }
     }
 
     public static IrSetExpr offset(IrSetExpr set, int offset) {
         if (offset == 0) {
             return set;
         }
         if (set instanceof IrOffset) {
             IrOffset nested = (IrOffset) set;
             return offset(nested.getSet(), offset + nested.getOffset());
         }
         IrDomain env = IrUtil.offset(set.getEnv(), offset);
         IrDomain ker = IrUtil.offset(set.getKer(), offset);
         IrDomain card = set.getCard();
         return IrUtil.asConstant(new IrOffset(set, offset, env, ker, card));
     }
 
     public static IrSetExpr mask(IrSetExpr set, int from, int to) {
         if (from > to) {
             throw new IllegalArgumentException();
         }
 
         if (from <= set.getEnv().getLowBound() && to > set.getEnv().getHighBound()) {
             return offset(set, -from);
         }
         IrDomain env = IrUtil.mask(set.getEnv(), from, to);
         IrDomain ker = IrUtil.mask(set.getKer(), from, to);
         IrDomain card = boundDomain(ker.size(), Math.min(env.size(), set.getCard().getHighBound()));
         return IrUtil.asConstant(new IrMask(set, from, to, env, ker, card));
     }
 
     public static IrSetExpr ternary(IrBoolExpr antecedent, IrSetExpr consequent, IrSetExpr alternative) {
         if (IrUtil.isTrue(antecedent)) {
             return consequent;
         }
         if (IrUtil.isFalse(antecedent)) {
             return alternative;
         }
         if (consequent.equals(alternative)) {
             return consequent;
         }
         IrDomain env = IrUtil.union(consequent.getEnv(), alternative.getEnv());
         IrDomain ker = IrUtil.intersection(consequent.getKer(), alternative.getKer());
         IrDomain card = IrUtil.union(consequent.getCard(), alternative.getCard());
         return new IrSetTernary(antecedent, consequent, alternative, env, ker, card);
     }
 }
