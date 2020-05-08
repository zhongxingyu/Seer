 package org.clafer.ir;
 
 import gnu.trove.TIntCollection;
 import gnu.trove.iterator.TIntIterator;
 import gnu.trove.set.TIntSet;
 import gnu.trove.set.hash.TIntHashSet;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.LinkedList;
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
      * ******************
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
 
     public static IrDomain enumDomain(TIntHashSet values) {
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
                 for (int i = 1; i < array.length; i++) {
                     if (low + i != array[i]) {
                         return new IrEnumDomain(array);
                     }
                 }
                 // A contigious interval.
                 return boundDomain(low, array[array.length - 1]);
         }
     }
     /**
      * ******************
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
 
     public static IrBoolExpr and(Collection<IrBoolExpr> operands) {
         return and(operands.toArray(new IrBoolExpr[operands.size()]));
     }
 
     public static IrBoolExpr and(IrBoolExpr... operands) {
         List<IrBoolExpr> flatten = new ArrayList<IrBoolExpr>(operands.length);
         for (IrBoolExpr operand : operands) {
             flattenAnd(operand, flatten);
         }
         List<IrBoolExpr> filter = new ArrayList<IrBoolExpr>(flatten.size());
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
 
     private static void flattenAnd(IrBoolExpr expr, List<IrBoolExpr> flatten) {
         if (expr instanceof IrAnd) {
             IrAnd and = (IrAnd) expr;
             for (IrBoolExpr subexpr : and.getOperands()) {
                 flattenAnd(subexpr, flatten);
             }
         } else {
             flatten.add(expr);
         }
     }
 
     public static IrBoolExpr lone(Collection<IrBoolExpr> operands) {
         return lone(operands.toArray(new IrBoolExpr[operands.size()]));
     }
 
     public static IrBoolExpr lone(IrBoolExpr... operands) {
         List<IrBoolExpr> filter = new ArrayList<IrBoolExpr>(operands.length);
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
 
     public static IrBoolExpr one(Collection<IrBoolExpr> operands) {
         return one(operands.toArray(new IrBoolExpr[operands.size()]));
     }
 
     public static IrBoolExpr one(IrBoolExpr... operands) {
         List<IrBoolExpr> filter = new ArrayList<IrBoolExpr>(operands.length);
         int count = 0;
         for (IrBoolExpr operand : operands) {
             if (IrUtil.isTrue(operand)) {
                 count++;
                 if (count > 1) {
                     return False;
                 }
             }
             if (!IrUtil.isFalse(operand)) {
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
 
     public static IrBoolExpr or(Collection<IrBoolExpr> operands) {
         return or(operands.toArray(new IrBoolExpr[operands.size()]));
     }
 
     public static IrBoolExpr or(IrBoolExpr... operands) {
         List<IrBoolExpr> flatten = new ArrayList<IrBoolExpr>(operands.length);
         for (IrBoolExpr operand : operands) {
             flattenOr(operand, flatten);
         }
         List<IrBoolExpr> filter = new ArrayList<IrBoolExpr>(flatten.size());
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
 
     private static void flattenOr(IrBoolExpr expr, List<IrBoolExpr> flatten) {
         if (expr instanceof IrOr) {
             IrOr and = (IrOr) expr;
             for (IrBoolExpr subexpr : and.getOperands()) {
                 flattenOr(subexpr, flatten);
             }
         } else {
             flatten.add(expr);
         }
     }
 
     public static IrBoolExpr implies(IrBoolExpr antecedent, IrBoolExpr consequent) {
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
         return new IrImplies(antecedent, consequent, BoolDomain);
     }
 
     public static IrBoolExpr notImplies(IrBoolExpr antecedent, IrBoolExpr consequent) {
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
         return new IrXor(left, right, BoolDomain);
     }
 
     public static IrBoolExpr within(IrIntExpr value, IrDomain range) {
         IrDomain domain = value.getDomain();
         if (range.isBounded()
                 && domain.getLowBound() >= range.getLowBound()
                 && domain.getHighBound() <= range.getHighBound()) {
             return True;
         }
         if (domain.getLowBound() > range.getHighBound()
                 || domain.getHighBound() < range.getLowBound()) {
             return False;
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
                 if (leftDomain.size() == 1 && rightDomain.size() == 1) {
                     return constant(leftDomain.getLowBound() == rightDomain.getLowBound());
                 }
                 if (left instanceof IrBoolExpr && right instanceof IrBoolExpr) {
                     return ifOnlyIf((IrBoolExpr) left, (IrBoolExpr) right);
                 }
                 break;
             case NotEqual:
                 if (left.equals(right)) {
                     return False;
                 }
                 if (leftDomain.size() == 1 && rightDomain.size() == 1) {
                     return constant(leftDomain.getLowBound() != rightDomain.getLowBound());
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
                 break;
             case GreaterThan:
                 if (left.equals(right)) {
                     return False;
                 }
                 if (leftDomain.getLowBound() > rightDomain.getHighBound()) {
                     return True;
                 }
                 if (leftDomain.getHighBound() <= rightDomain.getLowBound()) {
                     return False;
                 }
                 if (left instanceof IrBoolExpr && right instanceof IrBoolExpr) {
                     return not(implies((IrBoolExpr) left, (IrBoolExpr) right));
                 }
                 break;
             case GreaterThanEqual:
                 if (left.equals(right)) {
                     return True;
                 }
                 if (leftDomain.getLowBound() >= rightDomain.getHighBound()) {
                     return True;
                 }
                 if (leftDomain.getHighBound() < rightDomain.getLowBound()) {
                     return False;
                 }
                 if (leftDomain.getHighBound() == rightDomain.getLowBound()) {
                     return equal(left, right);
                 }
                 if (left instanceof IrBoolExpr && right instanceof IrBoolExpr) {
                     return implies((IrBoolExpr) right, (IrBoolExpr) left);
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
 
     public static IrBoolExpr equality(IrSetExpr left, IrSetTest.Op op, IrSetExpr right) {
         switch (op) {
             case Equal:
                 if (left.equals(right)) {
                     return True;
                 }
                 int[] constant = IrUtil.getConstant(left);
                 if (constant != null) {
                     if (constant.length == 0) {
                         return equal(card(right), 0);
                     }
                     if (constant.length == right.getEnv().size()) {
                         if (IrUtil.containsAll(constant, right.getEnv())) {
                             return equal(card(right), constant.length);
                         }
                     }
                 }
                 constant = IrUtil.getConstant(right);
                 if (constant != null) {
                     if (constant.length == 0) {
                         return equal(card(left), 0);
                     }
                     if (constant.length == left.getEnv().size()) {
                         if (IrUtil.containsAll(constant, left.getEnv())) {
                             return equal(card(left), constant.length);
                         }
                     }
                 }
                 break;
             case NotEqual:
                 if (left.equals(right)) {
                     return False;
                 }
                 constant = IrUtil.getConstant(left);
                 if (constant != null) {
                     if (constant.length == 0) {
                         return equal(card(right), 0);
                     }
                     if (constant.length == right.getEnv().size()) {
                         if (IrUtil.containsAll(constant, right.getEnv())) {
                             return notEqual(card(right), constant.length);
                         }
                     }
                 }
                 constant = IrUtil.getConstant(right);
                 if (constant != null) {
                     if (constant.length == 0) {
                         return equal(card(left), 0);
                     }
                     if (constant.length == left.getEnv().size()) {
                         if (IrUtil.containsAll(constant, left.getEnv())) {
                             return notEqual(card(left), constant.length);
                         }
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
         return compare(left, IrCompare.Op.GreaterThan, right);
     }
 
     public static IrBoolExpr greaterThanEqual(int left, IrIntExpr right) {
         return greaterThanEqual(constant(left), right);
     }
 
     public static IrBoolExpr greaterThanEqual(IrIntExpr left, int right) {
         return greaterThanEqual(left, constant(right));
     }
 
     public static IrBoolExpr greaterThanEqual(IrIntExpr left, IrIntExpr right) {
         return compare(left, IrCompare.Op.GreaterThanEqual, right);
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
 
     public static IrBoolExpr boolChannel(Collection<IrBoolExpr> bools, IrSetExpr set) {
         return boolChannel(bools.toArray(new IrBoolExpr[bools.size()]), set);
     }
 
     public static IrBoolExpr boolChannel(IrBoolExpr[] bools, IrSetExpr set) {
         {
             int[] constant = IrUtil.getConstant(set);
             if (constant != null) {
                 IrBoolExpr[] ands = new IrBoolExpr[bools.length];
                 for (int i = 0; i < ands.length; i++) {
                     ands[i] = equal(bools[i], Util.in(i, constant) ? True : False);
                 }
                 return and(ands);
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
 
     public static IrBoolExpr sort(IrIntExpr[]... strings) {
         if (strings.length < 2) {
             return True;
         }
         return new IrSortStrings(strings, BoolDomain);
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
         List<IrBoolExpr> ands = new ArrayList<IrBoolExpr>(0);
         List<IrIntExpr[]> filterStrings = new ArrayList<IrIntExpr[]>(strings.length);
         List<IrIntExpr> filterInts = new ArrayList<IrIntExpr>(ints.length);
         for (int i = 0; i < strings.length; i++) {
             boolean equivalence = false;
             for (int j = i + 1; j < strings.length; j++) {
                 if (Arrays.equals(strings[i], strings[j])) {
                     ands.add(equal(ints[i], ints[j]));
                     equivalence = true;
                     break;
                 }
             }
             if (!equivalence) {
                 filterStrings.add(strings[i]);
                 filterInts.add(ints[i]);
             }
         }
         if (filterInts.size() == 1) {
             ands.add(equal(filterInts.get(0), 0));
         } else if (filterInts.size() > 1) {
             ands.add(
                     new IrSortStringsChannel(
                     filterStrings.toArray(new IrIntExpr[filterStrings.size()][]),
                     filterInts.toArray(new IrIntExpr[filterInts.size()]),
                     BoolDomain));
         }
         return and(ands);
     }
 
     public static IrBoolExpr allDifferent(IrIntExpr[] ints) {
         if (ints.length < 2) {
             return True;
         }
         return new IrAllDifferent(ints, BoolDomain);
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
         return new IrFilterString(set, offset, string, result, BoolDomain);
     }
 
     public static IrBoolExpr nop(IrIntExpr var) {
         if (var instanceof IrBoolConstant || var instanceof IrIntConstant) {
             return True;
         }
         return new IrIntNop(var);
     }
 
     public static IrBoolExpr nop(IrSetExpr var) {
         if (var instanceof IrSetConstant) {
             return True;
         }
         return new IrSetNop(var);
     }
     /**
      * ******************
      *
      * Integers
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
 
     public static IrIntExpr add(IrIntExpr... addends) {
         int constants = 0;
         Deque<IrIntExpr> filter = new LinkedList<IrIntExpr>();
         for (IrIntExpr operand : addends) {
             Integer constant = IrUtil.getConstant(operand);
             if (constant != null) {
                 constants += constant.intValue();
             } else {
                 filter.add(operand);
             }
         }
         if (constants != 0) {
             filter.add(constant(constants));
         }
         if (filter.isEmpty()) {
             return Zero;
         }
         if (filter.size() == 1) {
             return filter.getFirst();
         }
         IrDomain domain = addends[0].getDomain();
         int low = domain.getLowBound();
         int high = domain.getHighBound();
         for (int i = 1; i < addends.length; i++) {
             domain = addends[i].getDomain();
             low += domain.getLowBound();
             high += domain.getHighBound();
         }
         domain = boundDomain(low, high);
         return new IrAdd(filter.toArray(new IrIntExpr[filter.size()]), domain);
     }
 
     public static IrIntExpr sub(int minuend, IrIntExpr subtrahend) {
         return sub(constant(minuend), subtrahend);
     }
 
     public static IrIntExpr sub(IrIntExpr minuend, int subtrahend) {
         return sub(minuend, constant(subtrahend));
     }
 
     public static IrIntExpr sub(IrIntExpr... subtrahends) {
         int constants = 0;
         Deque<IrIntExpr> filter = new LinkedList<IrIntExpr>();
         for (int i = 1; i < subtrahends.length; i++) {
             IrIntExpr operand = subtrahends[i];
             Integer constant = IrUtil.getConstant(operand);
             if (constant != null) {
                 constants += constant.intValue();
             } else {
                 filter.add(operand);
             }
         }
         Integer head = IrUtil.getConstant(subtrahends[0]);
         if (head != null && filter.isEmpty()) {
             return constant(head - constants);
         }
         filter.addFirst(subtrahends[0]);
         if (constants != 0) {
             filter.add(constant(constants));
         }
         if (filter.size() == 1) {
             return filter.getFirst();
         }
         IrDomain domain = subtrahends[0].getDomain();
         int low = domain.getLowBound();
         int high = domain.getHighBound();
         for (int i = 1; i < subtrahends.length; i++) {
             domain = subtrahends[i].getDomain();
             low -= domain.getHighBound();
             high -= domain.getLowBound();
         }
         domain = boundDomain(low, high);
         return new IrSub(filter.toArray(new IrIntExpr[filter.size()]), domain);
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
         Integer constant = IrUtil.getConstant(index);
         if (constant != null) {
             return array[constant.intValue()];
         }
         TIntIterator iter = index.getDomain().iterator();
         assert iter.hasNext();
 
         IrDomain domain = array[iter.next()].getDomain();
         int low = domain.getLowBound();
         int high = domain.getHighBound();
         while (iter.hasNext()) {
             int val = iter.next();
             if (val < array.length) {
                 domain = array[val].getDomain();
                 low = Math.min(low, domain.getLowBound());
                 high = Math.max(high, domain.getHighBound());
             }
         }
         domain = boundDomain(low, high);
         return new IrElement(array, index, domain);
     }
 
     public static IrIntExpr count(int value, IrIntExpr[] array) {
         List<IrIntExpr> filter = new ArrayList<IrIntExpr>();
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
      * ******************
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
 
     // TODO: optimize on gc
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
                 IrDomain ker = Irs.enumDomain(values);
                 // TODO: every set expr function should do this
                 if (env.size() == ker.size()) {
                     return constant(env);
                 }
                 IrDomain card = Irs.boundDomain(1, Math.min(array.length, env.size()));
                 return new IrArrayToSet(array, env, ker, card, globalCardinality);
         }
     }
 
     /**
      * Relational join.
      *
      * Union{for all i in take} children[i]
      *
      * @param take
      * @param children
      * @return the join expression take.children
      */
     public static IrSetExpr joinRelation(IrSetExpr take, IrSetExpr[] children, boolean injective) {
         if (take.getEnv().isEmpty()) {
             return EmptySet;
         }
         IrSetExpr[] $children = children;
         if (take.getEnv().getHighBound() + 1 < $children.length) {
             $children = Arrays.copyOf(children, take.getEnv().getHighBound() + 1);
         }
 
         int[] constant = IrUtil.getConstant(take);
         if (constant != null) {
             IrSetExpr[] to = new IrSetExpr[constant.length];
             for (int i = 0; i < to.length; i++) {
                 to[i] = $children[constant[i]];
             }
             return union(to);
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
             env = Irs.EmptyDomain;
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
             ker = Irs.EmptyDomain;
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
         int[] constant = IrUtil.getConstant(take);
         if (constant != null) {
             IrIntExpr[] to = new IrIntExpr[constant.length];
             for (int i = 0; i < to.length; i++) {
                 to[i] = refs[constant[i]];
             }
             return arrayToSet(to, globalCardinality);
         }
 
         // Compute env
         TIntIterator iter = take.getEnv().iterator();
         IrDomain env;
         if (iter.hasNext()) {
             IrDomain domain = refs[iter.next()].getDomain();
             while (iter.hasNext()) {
                 domain = IrUtil.union(domain, refs[iter.next()].getDomain());
             }
             env = domain;
         } else {
             env = Irs.EmptyDomain;
         }
 
         // Compute ker
         iter = take.getKer().iterator();
         TIntHashSet values = new TIntHashSet(0);
         while (iter.hasNext()) {
             Integer constantRef = IrUtil.getConstant(refs[iter.next()]);
             if (constantRef != null) {
                 values.add(constantRef.intValue());
             }
         }
         IrDomain ker = values.isEmpty() ? Irs.EmptyDomain : new IrEnumDomain(values.toArray());
 
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
 
         return new IrJoinFunction(take, refs, env, ker, card, globalCardinality);
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
         switch (operands.length) {
             case 0:
                 return EmptySet;
             case 1:
                 return operands[0];
             default:
                 IrDomain env = IrUtil.intersectionEnvs(operands);
                 IrDomain ker = IrUtil.intersectionKers(operands);
                 int low = 0;
                 int high = operands[0].getCard().getHighBound();
                 for (int i = 1; i < operands.length; i++) {
                     high = Math.max(high, operands[0].getCard().getHighBound());
                 }
                 IrDomain card = boundDomain(
                         Math.max(low, ker.size()),
                         Math.min(high, env.size()));
                 return new IrSetIntersection(operands, env, ker, card);
         }
     }
 
     public static IrSetExpr union(IrSetExpr... operands) {
         List<IrSetExpr> flatten = new ArrayList<IrSetExpr>(operands.length);
         for (IrSetExpr operand : operands) {
             flattenUnion(operand, flatten);
         }
         List<IrSetExpr> filter = new ArrayList<IrSetExpr>();
         for (IrSetExpr operand : flatten) {
             if (!operand.getEnv().isEmpty()) {
                 filter.add(operand);
             }
         }
         operands = filter.toArray(new IrSetExpr[filter.size()]);
         switch (operands.length) {
             case 0:
                 return EmptySet;
             case 1:
                 return operands[0];
             default:
                 IrDomain env = IrUtil.unionEnvs(operands);
                 IrDomain ker = IrUtil.unionKers(operands);
                 IrDomain operandCard = operands[0].getCard();
                 int low = operandCard.getLowBound();
                 int high = operandCard.getHighBound();
                 for (int i = 1; i < operands.length; i++) {
                     operandCard = operands[i].getCard();
                     low = Math.max(low, operandCard.getLowBound());
                     high += operandCard.getHighBound();
                 }
                 IrDomain card = boundDomain(
                         Math.max(low, ker.size()),
                         Math.min(high, env.size()));
                 return IrUtil.asConstant(new IrSetUnion(operands, env, ker, card));
         }
     }
 
     private static void flattenUnion(IrSetExpr expr, List<IrSetExpr> flatten) {
         if (expr instanceof IrSetUnion) {
             IrSetUnion union = (IrSetUnion) expr;
             for (IrSetExpr subexpr : union.getOperands()) {
                 flattenUnion(subexpr, flatten);
             }
         } else {
             flatten.add(expr);
         }
     }
 
     public static IrSetExpr offset(IrSetExpr set, int offset) {
         if (offset == 0) {
             return set;
         }
         int[] constant = IrUtil.getConstant(set);
         if (constant != null) {
             for (int i = 0; i < constant.length; i++) {
                 constant[i] += offset;
             }
             return constant(constant);
         }
         IrDomain env = IrUtil.offset(set.getEnv(), offset);
         IrDomain ker = IrUtil.offset(set.getKer(), offset);
         IrDomain card = set.getCard();
         return new IrOffset(set, offset, env, ker, card);
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
         int[] consequentConstant = IrUtil.getConstant(consequent);
         int[] alternativeConstant = IrUtil.getConstant(alternative);
         if (consequentConstant != null && alternativeConstant != null && Arrays.equals(consequentConstant, alternativeConstant)) {
             return constant(consequentConstant);
         }
         IrDomain env = IrUtil.union(consequent.getEnv(), alternative.getEnv());
         IrDomain ker = IrUtil.intersection(consequent.getKer(), alternative.getKer());
         IrDomain card = IrUtil.union(consequent.getCard(), alternative.getCard());
         return new IrSetTernary(antecedent, consequent, alternative, env, ker, card);
     }
 }
