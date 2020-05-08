 package org.jessies.calc;
 
 /*
  * This file is part of org.jessies.calc.
  * Copyright (C) 2009 Elliott Hughes <enh@jessies.org>.
  * 
  * LittleHelper is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.math.*;
 import java.util.*;
 
 public class CalculatorFunctions {
     private CalculatorFunctions() {}
     
     private static boolean isNumber(Node n) {
         return (n instanceof NumberNode);
     }
     
     private static boolean isZero(Node n) {
         return (n instanceof IntegerNode) && (((IntegerNode) n).compareTo(IntegerNode.ZERO) == 0);
     }
     
     private static boolean isOne(Node n) {
         return (n instanceof IntegerNode) && (((IntegerNode) n).compareTo(IntegerNode.ONE) == 0);
     }
     
     private static int cmp(Calculator environment, List<Node> args) {
         final NumberNode lhs = toNumber("cmp", environment, args.get(0));
         final NumberNode rhs = toNumber("cmp", environment, args.get(1));
         return cmp(lhs, rhs);
     }
     
     private static int cmp(NumberNode lhs, NumberNode rhs) {
         if (lhs instanceof IntegerNode && rhs instanceof IntegerNode) {
             return ((IntegerNode) lhs).compareTo((IntegerNode) rhs);
         } else {
             // FIXME: what about an integer too large to be represented as a real, compared to a real?
             return lhs.toReal().compareTo(rhs.toReal());
         }
     }
     
     private static BooleanNode equal(Node lhs, Node rhs) {
         if (lhs instanceof BooleanNode && rhs instanceof BooleanNode) {
             return BooleanNode.valueOf(lhs == rhs);
         } else if (lhs instanceof NumberNode && rhs instanceof NumberNode) {
             return BooleanNode.valueOf(cmp((NumberNode) lhs, (NumberNode) rhs) == 0);
         } else if (lhs instanceof ListNode && rhs instanceof ListNode) {
             return equalLists((ListNode) lhs, (ListNode) rhs);
         } else {
             return BooleanNode.FALSE;
         }
     }
     
     private static BooleanNode equalLists(ListNode lhs, ListNode rhs) {
         if (lhs.size() != rhs.size()) {
             return BooleanNode.FALSE;
         }
         for (int i = 0, size = lhs.size(); i < size; ++i) {
             if (equal(lhs.get(i), rhs.get(i)) == BooleanNode.FALSE) {
                 return BooleanNode.FALSE;
             }
         }
         return BooleanNode.TRUE;
     }
     
     private static ListNode toList(String function, Calculator environment, Node node) {
         node = node.evaluate(environment);
         if (node instanceof ListNode) {
             return (ListNode) node;
         }
         throw expected(function, "list");
     }
     
     private static NumberNode toNumber(String function, Calculator environment, Node node) {
         node = node.evaluate(environment);
         if (node instanceof NumberNode) {
             return (NumberNode) node;
         }
         throw expected(function, "numeric");
     }
     
     private static CalculatorVariableNode toVariable(String function, Node node) {
         // We don't evaluate 'node' because we don't want its value; we want it as a variable, and it may be bound.
         if (node instanceof CalculatorVariableNode) {
             return (CalculatorVariableNode) node;
         }
         throw expected(function, "variable name");
     }
     
     public static CalculatorError expected(String function, String type) {
         throw new CalculatorError("'" + function + "' expected " + type + " argument");
     }
     
     private static int toBase(IntegerNode base) {
         if (base.compareTo(IntegerNode.valueOf(2)) < 0 || base.compareTo(IntegerNode.valueOf(36)) > 0) {
             throw new CalculatorError("base must be between 2 and 36");
         }
         return base.intValue();
     }
     
     // Returns [rowCount, columnCount], or null if 'node' is not a matrix.
     private static ListNode matrixDimensions(Node node) {
         // A node is a matrix if:
         // 1. it's a list..
         if (!(node instanceof ListNode)) {
             return null;
         }
         // 2. each element is a list...
         final ListNode list = (ListNode) node;
         int columnCount = 0;
         for (Node element : list) {
             if (!(element instanceof ListNode)) {
                 return null;
             }
             // 3. of the same length...
             ListNode row = (ListNode) element;
             int rowLength = row.size();
             if (columnCount != 0 && columnCount != rowLength) {
                 return null;
             }
             columnCount = rowLength;
             // 4. where none of the elements itself contains a list.
             for (Node rowElement : row) {
                 if (rowElement instanceof ListNode) {
                     return null;
                 }
             }
         }
         int rowCount = list.size();
         return new ListNode().add(IntegerNode.valueOf(rowCount)).add(IntegerNode.valueOf(columnCount));
     }
     
     // Returns the 'j'th element of row 'i' of 'm'.
     // Uses zero-based indexes, for internal use only.
     private static Node matrixElementAt(ListNode m, IntegerNode i, IntegerNode j) {
         ListNode row = (ListNode) m.get(i);
         Node element = row.get(j);
         return element;
     }
     
     private enum MatrixOp {
         ADD("addition"),
         MUL("multiplication"),
         SUB("subtraction"),
         RSB("subtraction");
         private final String name;
         private MatrixOp(String name) { this.name = name; }
         @Override public String toString() { return name; }
     }
     
     private static Node matrixMatrixOp(ListNode lhs, ListNode lhsDimensions, ListNode rhs, ListNode rhsDimensions, MatrixOp op) {
         if (equal(lhsDimensions, rhsDimensions) == BooleanNode.FALSE) {
             throw new CalculatorError("matrix " + op + " requires compatible matrices");
         }
         final IntegerNode rowCount = (IntegerNode) lhsDimensions.get(0);
         final IntegerNode columnCount = (IntegerNode) rhsDimensions.get(1);
         final ListNode result = new ListNode();
         for (IntegerNode i = IntegerNode.ZERO; cmp(i, rowCount) < 0; i = i.increment()) {
             final ListNode newRow = new ListNode();
             for (IntegerNode j = IntegerNode.ZERO; cmp(j, columnCount) < 0; j = j.increment()) {
                 Node lhsNode = matrixElementAt(lhs, i, j);
                 Node rhsNode = matrixElementAt(rhs, i, j);
                 if (!isNumber(lhsNode) || !isNumber(rhsNode)) {
                     throw new CalculatorError(op + " of a matrix and a scalar requires numeric matrices");
                 }
                 NumberNode lhsNumber = (NumberNode) lhsNode;
                 NumberNode rhsNumber = (NumberNode) rhsNode;
                 switch (op) {
                 case ADD: newRow.add(lhsNumber.plus(rhsNumber)); break;
                 case SUB: newRow.add(lhsNumber.subtract(rhsNumber)); break;
                 default: throw new CalculatorError("elementwise matrix multiplication/reverse-subtract not implemented");
                 }
             }
             result.add(newRow);
         }
         return result;
     }
     
     private static Node matrixScalarOp(ListNode matrix, ListNode dimensions, NumberNode scalar, MatrixOp op) {
         final ListNode result = new ListNode();
         for (Node row : matrix) {
             final ListNode newRow = new ListNode();
             for (Node value : (ListNode) row) {
                 if (!isNumber(value)) {
                     throw new CalculatorError(op + " of a matrix and a scalar requires numeric matrices");
                 }
                 NumberNode element = (NumberNode) value;
                 switch (op) {
                 case ADD: newRow.add(element.plus(scalar)); break;
                 case MUL: newRow.add(element.times(scalar)); break;
                 case RSB: newRow.add(scalar.subtract(element)); break;
                 case SUB: newRow.add(element.subtract(scalar)); break;
                 }
             }
             result.add(newRow);
         }
         return result;
     }
     
     public static class Abs extends CalculatorFunctionN {
         public Abs() {
             super("Abs");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.abs();
         }
     }
     
     public static class Acos extends CalculatorFunctionN {
         public Acos() {
             super("acos");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().acos();
         }
     }
     
     public static class And extends CalculatorFunctionZZ {
         public And() {
             super("And");
         }
         
         public Node apply(Calculator environment, BooleanNode lhs, BooleanNode rhs) {
             return lhs.and(rhs);
         }
     }
     
     public static class Asin extends CalculatorFunctionN {
         public Asin() {
             super("asin");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().asin();
         }
     }
     
     public static class Atan extends CalculatorFunctionN {
         public Atan() {
             super("atan");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().atan();
         }
     }
     
     public static class Atan2 extends CalculatorFunctionNN {
         public Atan2() {
             super("atan2");
         }
         
         public Node apply(Calculator environment, NumberNode arg1, NumberNode arg2) {
             return arg1.toReal().atan2(arg2.toReal());
         }
     }
     
     public static class BitAnd extends CalculatorFunctionII {
         public BitAnd() {
             super("BitAnd");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.bitAnd(rhs);
         }
     }
     
     public static class BitClear extends CalculatorFunctionII {
         public BitClear() {
             super("BitClear");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.bitClear(rhs);
         }
     }
     
     public static class BitGet extends CalculatorFunctionII {
         public BitGet() {
             super("BitGet");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.bitGet(rhs);
         }
     }
     
     public static class BitLength extends CalculatorFunctionI {
         public BitLength() {
             super("BitLength");
         }
         
         public Node apply(Calculator environment, IntegerNode arg) {
             return arg.bitLength();
         }
     }
     
     public static class BitNot extends CalculatorFunctionI {
         public BitNot() {
             super("BitNot");
         }
         
         public Node apply(Calculator environment, IntegerNode arg) {
             return arg.bitNot();
         }
     }
     
     public static class BitOr extends CalculatorFunctionII {
         public BitOr() {
             super("BitOr");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.bitOr(rhs);
         }
     }
     
     public static class BitSet extends CalculatorFunctionII {
         public BitSet() {
             super("BitSet");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.bitSet(rhs);
         }
     }
     
     public static class BitShiftLeft extends CalculatorFunctionII {
         public BitShiftLeft() {
             super("BitShiftLeft");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.bitShiftLeft(rhs);
         }
     }
     
     public static class BitShiftRight extends CalculatorFunctionII {
         public BitShiftRight() {
             super("BitShiftRight");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.bitShiftRight(rhs);
         }
     }
     
     public static class BitXor extends CalculatorFunctionII {
         public BitXor() {
             super("BitXor");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.bitXor(rhs);
         }
     }
     
     // Converts true and false into 1 and 0, respectively.
     public static class Boole extends CalculatorFunctionZ {
         public Boole() {
             super("Boole");
         }
         
         public Node apply(Calculator environment, BooleanNode arg) {
             return arg == BooleanNode.TRUE ? IntegerNode.ONE : IntegerNode.ZERO;
         }
     }
     
     public static class Cbrt extends CalculatorFunctionN {
         public Cbrt() {
             super("cbrt");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().cbrt();
         }
     }
     
     public static class Ceiling extends CalculatorFunctionN {
         public Ceiling() {
             super("ceiling");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().ceiling();
         }
     }
     
     public static class Cos extends CalculatorFunctionN {
         public Cos() {
             super("cos");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().cos();
         }
     }
     
     public static class Cosh extends CalculatorFunctionN {
         public Cosh() {
             super("cosh");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().cosh();
         }
     }
     
     public static class Define extends CalculatorFunction {
         public Define() {
             super("define", 2);
         }
         
         public Node apply(Calculator environment) {
             final CalculatorVariableNode variable = toVariable("Define", args.get(0));
             final Node value = arg(environment, 1);
             environment.setVariable(variable.name(), value);
             return value;
         }
     }
     
     // DigitCount(n, base) - returns the number of instances of each digit in the given base representation of 'n'.
     // base defaults to 10
     public static class DigitCount extends CalculatorFunctionII {
         public DigitCount() {
             super("DigitCount", IntegerNode.valueOf(10));
         }
         
         public Node apply(Calculator environment, IntegerNode n, IntegerNode bigBase) {
             final int base = toBase(bigBase);
             final String rep = n.toString(base);
             int[] counts = new int[base];
             for (int i = 0; i < rep.length(); ++i) {
                 final char ch = rep.charAt(i);
                 if (ch != '-') {
                     final int digit = Character.digit(ch, base);
                     ++counts[digit];
                 }
             }
             
             final ListNode result = new ListNode();
             for (int count : counts) {
                 result.add(IntegerNode.valueOf(count));
             }
             return result;
         }
     }
     
     public static class Dimensions extends CalculatorFunction { // FIXME: CAS support.
         public Dimensions() {
             super("Dimensions", 1);
         }
         
         public Node apply(Calculator environment) {
             final ListNode dimensions = matrixDimensions(args.get(0).evaluate(environment));
             if (dimensions == null) {
                 throw expected("Dimensions", "matrix");
             }
             return dimensions;
         }
     }
     
     public static class Divide extends CalculatorFunctionNN {
         public Divide() {
             super("Divide");
         }
         
         public Node apply(Calculator environment, NumberNode lhs, NumberNode rhs) {
             return lhs.divide(rhs);
         }
     }
     
     public static class Equal extends CalculatorFunction { // FIXME: CAS support.
         public Equal() {
             super("Equal", 2);
         }
         
         public Node apply(Calculator environment) {
             return equal(arg(environment, 0), arg(environment, 1));
         }
     }
     
     public static class Exp extends CalculatorFunctionN {
         public Exp() {
             super("exp");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().exp();
         }
     }
     
     public static class Factorial extends CalculatorFunctionI {
         public Factorial() {
             super("Factorial");
         }
         
         public Node apply(Calculator environment, IntegerNode n) {
             return n.factorial();
         }
     }
     
     // Filter(expr, var, list) - returns a list of the elements of 'list' for which evaluating 'expr' with 'var' bound to that value gives True.
     public static class Filter extends MapOrFilter {
         public Filter() {
             super("Filter", false);
         }
     }
     
     public static class Floor extends CalculatorFunctionN {
         public Floor() {
             super("floor");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().floor();
         }
     }
     
     public static class FractionalPart extends CalculatorFunctionN {
         public FractionalPart() {
             super("FractionalPart");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.fractionalPart();
         }
     }
     
     public static class GCD extends CalculatorFunctionII {
         public GCD() {
             super("GCD");
         }
         
         public Node apply(Calculator environment, IntegerNode a, IntegerNode b) {
             return IntegerNode.gcd(a, b);
         }
     }
     
     public static class Greater extends CalculatorFunction { // FIXME: CAS support.
         public Greater() {
             super("Greater", 2);
         }
         
         public Node apply(Calculator environment) {
             return BooleanNode.valueOf(cmp(environment, args) > 0);
         }
     }
     
     public static class GreaterEqual extends CalculatorFunction { // FIXME: CAS support.
         public GreaterEqual() {
             super("GreaterEqual", 2);
         }
         
         public Node apply(Calculator environment) {
             return BooleanNode.valueOf(cmp(environment, args) >= 0);
         }
     }
     
     public static class Hypot extends CalculatorFunctionNN {
         public Hypot() {
             super("hypot");
         }
         
         public Node apply(Calculator environment, NumberNode arg1, NumberNode arg2) {
             return arg1.toReal().hypot(arg2.toReal());
         }
     }
     
     public static class IdentityMatrix extends CalculatorFunctionI {
         public IdentityMatrix() {
             super("IdentityMatrix");
         }
         
         public Node apply(Calculator environment, IntegerNode dimension) {
             final ListNode result = new ListNode();
             for (NumberNode i = IntegerNode.ONE; cmp(i, dimension) <= 0; i = i.increment()) {
                 final ListNode row = new ListNode();
                 for (NumberNode j = IntegerNode.ONE; cmp(j, dimension) <= 0; j = j.increment()) {
                     row.add(cmp(j, i) == 0 ? IntegerNode.ONE : IntegerNode.ZERO);
                 }
                 result.add(row);
             }
             return result;
         }
     }
     
     // Returns the number of decimal digits in the given integer.
     public static class IntegerLength extends CalculatorFunctionII {
         public IntegerLength() {
             super("IntegerLength", IntegerNode.valueOf(10));
         }
         
         public Node apply(Calculator environment, IntegerNode n, IntegerNode base) {
             return IntegerNode.valueOf(n.abs().toString(toBase(base)).length());
         }
     }
     
     public static class IntegerPart extends CalculatorFunctionN {
         public IntegerPart() {
             super("IntegerPart");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.integerPart();
         }
     }
     
     public static class IsMatrix extends CalculatorFunction { // FIXME: CAS support.
         public IsMatrix() {
             super("IsMatrix", 1);
         }
         
         public Node apply(Calculator environment) {
             return BooleanNode.valueOf(matrixDimensions(arg(environment, 0)) != null);
         }
     }
     
     public static class IsPrime extends CalculatorFunctionI {
         public IsPrime() {
             super("IsPrime");
         }
         
         public Node apply(Calculator environment, IntegerNode arg) {
             return arg.isPrime();
         }
     }
     
     public static class LCM extends CalculatorFunctionII {
         public LCM() {
             super("LCM");
         }
         
         public Node apply(Calculator environment, IntegerNode a, IntegerNode b) {
             if (a.compareTo(IntegerNode.ZERO) == 0 || b.compareTo(IntegerNode.ZERO) == 0) {
                 return IntegerNode.ZERO;
             }
             // lcm(a,b) = abs(a*b)/gcd(a,b)
             return a.times(b).abs().divide(IntegerNode.gcd(a, b));
         }
     }
     
    public static class Length extends CalculatorFunction { // FIXME: CAS support.
         public Length() {
            super("Length", 1);
         }
         
        public Node apply(Calculator environment) {
            Node node = args.get(0).evaluate(environment);
            if (!(node instanceof ListNode)) {
                return IntegerNode.ZERO;
            }
            ListNode list = (ListNode) node;
             return IntegerNode.valueOf(list.size());
         }
     }
     
     public static class Less extends CalculatorFunction { // FIXME: CAS support.
         public Less() {
             super("Less", 2);
         }
         
         public Node apply(Calculator environment) {
             return BooleanNode.valueOf(cmp(environment, args) < 0);
         }
     }
     
     public static class LessEqual extends CalculatorFunction { // FIXME: CAS support.
         public LessEqual() {
             super("LessEqual", 2);
         }
         
         public Node apply(Calculator environment) {
             return BooleanNode.valueOf(cmp(environment, args) <= 0);
         }
     }
     
     // We call this ListBuilder because reusing the name List in Java is just too confusing.
     public static class ListBuilder extends CalculatorFunction {
         public ListBuilder() {
             super("List", 0, Integer.MAX_VALUE);
         }
         
         public Node apply(Calculator environment) {
             final ListNode result = new ListNode();
             for (Node arg : args) {
                 result.add(arg.evaluate(environment));
             }
             return result;
         }
     }
     
     // log(base, n).
     public static class Log extends CalculatorFunctionNN {
         public Log() {
             super("log");
         }
         
         public Node apply(Calculator environment, NumberNode base, NumberNode n) {
             return n.toReal().log(base.toReal());
         }
     }
     
     public static class Log2 extends CalculatorFunctionN {
         public Log2() {
             super("log2");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().log2();
         }
     }
     
     public static class LogE extends CalculatorFunctionN {
         public LogE() {
             super("logE");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().logE();
         }
     }
     
     public static class Log10 extends CalculatorFunctionN {
         public Log10() {
             super("log10");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().log10();
         }
     }
     
     // Map(expr, var, list) - returns a list of the results of evaluating 'expr' with 'var' bound to each value in 'list' in turn.
     public static class Map extends MapOrFilter {
         public Map() {
             super("Map", true);
         }
     }
     
     public static class MapOrFilter extends CalculatorFunction {
         private final boolean isMap;
         
         public MapOrFilter(String name, boolean isMap) {
             super(name, 3);
             this.isMap = isMap;
         }
         
         public Node apply(Calculator environment) {
             final Node expr = args.get(0);
             final CalculatorVariableNode var = toVariable(name(), args.get(1));
             final Node maybeList = arg(environment, 2);
             
             if (!(maybeList instanceof ListNode)) {
                 if (maybeList instanceof BooleanNode || maybeList instanceof NumberNode) {
                     throw new CalculatorError("'" + name() + "' requires a list argument");
                 }
                 return this;
             }
             
             final ListNode list = (ListNode) maybeList;
             final Node originalVarValue = environment.getVariable(var.name());
             try {
                 final ListNode result = new ListNode();
                 for (int i = 0; i < list.size(); ++i) {
                     environment.setVariable(var.name(), list.get(i));
                     Node value = expr.evaluate(environment);
                     if (isMap) {
                         result.add(value);
                     } else  if (value == BooleanNode.TRUE) {
                         result.add(list.get(i));
                     }
                 }
                 return result;
             } finally {
                 environment.setVariable(var.name(), originalVarValue);
             }
         }
     }
     
     public static class Max extends CalculatorFunctionNN {
         public Max() {
             super("Max");
         }
         
         public Node apply(Calculator environment, NumberNode lhs, NumberNode rhs) {
             return cmp(lhs, rhs) >= 0 ? lhs : rhs;
         }
     }
     
     public static class Min extends CalculatorFunctionNN {
         public Min() {
             super("Min");
         }
         
         public Node apply(Calculator environment, NumberNode lhs, NumberNode rhs) {
             return cmp(lhs, rhs) < 0 ? lhs : rhs;
         }
     }
     
     public static class Mod extends CalculatorFunctionII {
         public Mod() {
             super("Mod");
         }
         
         public Node apply(Calculator environment, IntegerNode lhs, IntegerNode rhs) {
             return lhs.mod(rhs);
         }
     }
     
     public static class Not extends CalculatorFunctionZ {
         public Not() {
             super("Not");
         }
         
         public Node apply(Calculator environment, BooleanNode arg) {
             return arg.not();
         }
     }
     
     public static class Or extends CalculatorFunctionZZ {
         public Or() {
             super("Or");
         }
         
         public Node apply(Calculator environment, BooleanNode lhs, BooleanNode rhs) {
             return lhs.or(rhs);
         }
     }
     
     public static class Plot extends CalculatorFunction {
         public Plot() {
             super("Plot", 4);
         }
         
         public Node apply(Calculator environment) {
             CalculatorPlotter plotter = environment.getPlotter();
             if (plotter == null) {
                 throw new CalculatorError("this system is not capable of plotting");
             }
             
             // 0: expression
             // 1: variable
             // 2: xMin
             // 3: xMax
             // Example: plot(sin(x), x, 0, 2*pi)
             // FIXME: automatic guesses? (trig => pi? differentiate to find minima? solve to find axis crossings?).
             
             final Node expression = args.get(0);
             final CalculatorVariableNode variable = toVariable("Plot", args.get(1));
             final RealNode xMin = toNumber("Plot", environment, args.get(2)).toReal();
             final RealNode xMax = toNumber("Plot", environment, args.get(3)).toReal();
             
             final int pixelWidth = 300;
             CalculatorPlotData plotData = new CalculatorPlotData(pixelWidth, xMin, xMax);
             
             try {
                 Node free = variable.evaluate(environment);
                 throw new CalculatorError("variable '" + variable.name() + "' is not free");
             } catch (CalculatorError ex) {
                 // That's what we hoped...
             }
             final String variableName = variable.name();
             try {
                 double x = plotData.xMin;
                 double xStep = plotData.xRange / pixelWidth;
                 for (int i = 0; i < pixelWidth; ++i) {
                     try {
                         environment.setVariable(variableName, new RealNode(x));
                         double y = Double.parseDouble(expression.evaluate(environment).toString());
                         if (Double.isInfinite(y)) {
                             y = Double.NaN; // Infinity confuses the range calculations.
                         }
                         plotData.data[i] = y;
                     } catch (Exception ex) {
                         ex.printStackTrace();
                         plotData.data[i] = Double.NaN;
                     }
                     x += xStep;
                 }
             } finally {
                 environment.setVariable(variableName, null);
             }
             
             plotData.calculateRange();
             plotter.showPlot(plotData);
             
             return BooleanNode.TRUE; // FIXME: "void"?
         }
     }
     
     public static class Plus extends CalculatorFunction { // FIXME: CAS support.
         public Plus() {
             super("Plus", 2);
         }
         
         public Node apply(Calculator environment) {
             final Node lhs = arg(environment, 0);
             final Node rhs = arg(environment, 1);
             if (isNumber(lhs) && isNumber(rhs)) {
                 // scalar + scalar
                 return ((NumberNode) lhs).plus((NumberNode) rhs);
             }
             final ListNode lhsDimensions = matrixDimensions(lhs);
             final ListNode rhsDimensions = matrixDimensions(rhs);
             if (lhsDimensions != null && rhsDimensions != null) {
                 // matrix + matrix
                 return matrixMatrixOp((ListNode) lhs, lhsDimensions, (ListNode) rhs, rhsDimensions, MatrixOp.ADD);
             } else if (isNumber(lhs) && rhsDimensions != null) {
                 // scalar + matrix
                 return matrixScalarOp((ListNode) rhs, rhsDimensions, (NumberNode) lhs, MatrixOp.ADD);
             } else if (lhsDimensions != null && isNumber(rhs)) {
                 // matrix + scalar
                 return matrixScalarOp((ListNode) lhs, lhsDimensions, (NumberNode) rhs, MatrixOp.ADD);
             } else {
                 return bind(lhs, rhs);
             }
         }
         
         @Override public Node simplify(Calculator environment) {
             final ArrayList<Node> args = simplifyArgs(environment);
             NumberNode total = IntegerNode.ZERO;
             for (int i = args.size() - 1; i >= 0; --i) {
                 final Node arg = args.get(i);
                 if (isNumber(arg)) {
                     if (!isZero(arg)) {
                         total = total.plus((NumberNode) arg);
                     }
                     args.remove(i);
                 }
             }
             if (args.size() == 0 || !isZero(total)) {
                 // Sort numeric terms last in sums (preferring "x + 1" rather than "1 + x").
                 args.add(total);
             }
             if (args.size() == 1) {
                 return args.get(0);
             }
             return bind(args);
         }
     }
     
     public static class Power extends CalculatorFunctionNN {
         public Power() {
             super("power");
         }
         
         public Node apply(Calculator environment, NumberNode lhs, NumberNode rhs) {
             return lhs.power(rhs);
         }
     }
     
     public static class Product extends CalculatorFunction { // FIXME: CAS support.
         public Product() {
             super("product", 3);
         }
         
         public Node apply(Calculator environment) {
             return series(environment, args, IntegerNode.ONE, false);
         }
     }
     
     public static class Random extends CalculatorFunction {
         public Random() {
             super("random", 0);
         }
         
         public Node apply(Calculator environment) {
             return new RealNode(Math.random());
         }
     }
     
     public static class Range extends CalculatorFunction { // FIXME: CAS support.
         public Range() {
             super("Range", 1, 3);
         }
         
         public Node apply(Calculator environment) {
             final NumberNode start;
             final NumberNode end;
             final NumberNode step;
             if (args.size() == 1) {
                 // given n: 1, 2, ..., n
                 start = IntegerNode.ONE;
                 end = toNumber("range", environment, args.get(0));
                 step = IntegerNode.ONE;
             } else if (args.size() == 2) {
                 // given n, m: n, n+1, ..., m
                 start = toNumber("range", environment, args.get(0));
                 end = toNumber("range", environment, args.get(1));
                 step = IntegerNode.ONE;
             } else {
                 // given n, m, k: n, n+k, ..., m
                 start = toNumber("range", environment, args.get(0));
                 end = toNumber("range", environment, args.get(1));
                 step = toNumber("range", environment, args.get(2));
             }
             return makeRange(start, end, step);
         }
         
         private static ListNode makeRange(NumberNode start, NumberNode end, NumberNode step) {
             // TODO: factor out iteration (and collection of iteration parameters) so we can have consistent behavior between functions.
             IntegerNode stepSign = step.sign();
             if (stepSign.equals(IntegerNode.ZERO)) {
                 throw new CalculatorError("need a non-zero step size");
             }
             
             final ListNode result = new ListNode();
             if (cmp(stepSign, IntegerNode.ZERO) > 0) {
                 for (NumberNode i = start; cmp(i, end) <= 0; i = i.plus(step)) {
                     result.add(i);
                 }
             } else {
                 for (NumberNode i = start; cmp(i, end) >= 0; i = i.plus(step)) {
                     result.add(i);
                 }
             }
             return result;
         }
     }
     
     public static class Reverse extends CalculatorFunctionL {
         public Reverse() {
             super("Reverse");
         }
         
         public Node apply(Calculator environment, ListNode list) {
             return list.reverse();
         }
     }
     
     public static class Round extends CalculatorFunctionN {
         public Round() {
             super("round");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().round();
         }
     }
     
     private static NumberNode series(Calculator environment, List<Node> args, IntegerNode initial, boolean isSum) {
         final Node expr = args.get(0);
         NumberNode iMin = toNumber("series", environment, args.get(1));
         NumberNode iMax = toNumber("series", environment, args.get(2));
         
         // Ensure we have two integers or two reals.
         if (iMin instanceof RealNode || iMax instanceof RealNode) {
             iMin = iMin.toReal();
             iMax = iMax.toReal();
         }
         
         if (cmp(iMin, iMax) > 0) {
             throw new CalculatorError("minimum (" + iMin + ") greater than maximum (" + iMax + ")");
         }
         
         // FIXME: support infinite sums/products, adding convergence testing.
         
         // FIXME: let the user specify the sum variable.
         final Node originalI = environment.getVariable("i");
         try {
             NumberNode result = initial;
             for (NumberNode i = iMin; cmp(i, iMax) <= 0; i = i.increment()) {
                 environment.setVariable("i", i);
                 // FIXME: handle undefined and non-numeric terms.
                 final NumberNode term = (NumberNode) expr.evaluate(environment);
                 if (isSum) {
                     result = result.plus(term);
                 } else {
                     result = result.times(term);
                 }
             }
             return result;
         } finally {
             environment.setVariable("i", originalI);
         }
     }
     
     public static class Sign extends CalculatorFunctionN {
         public Sign() {
             super("Sign");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.sign();
         }
     }
     
     public static class Sin extends CalculatorFunctionN {
         public Sin() {
             super("sin");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().sin();
         }
     }
     
     public static class Sinh extends CalculatorFunctionN {
         public Sinh() {
             super("sinh");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().sinh();
         }
     }
     
     public static class Sqrt extends CalculatorFunctionN {
         public Sqrt() {
             super("sqrt");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().sqrt();
         }
     }
     
     public static class Subtract extends CalculatorFunction { // FIXME: CAS support.
         public Subtract() {
             super("Subtract", 2);
         }
         
         public Node apply(Calculator environment) {
             final Node lhs = arg(environment, 0);
             final Node rhs = arg(environment, 1);
             if (isNumber(lhs) && isNumber(rhs)) {
                 // scalar - scalar
                 return ((NumberNode) lhs).subtract((NumberNode) rhs);
             }
             final ListNode lhsDimensions = matrixDimensions(lhs);
             final ListNode rhsDimensions = matrixDimensions(rhs);
             if (lhsDimensions != null && rhsDimensions != null) {
                 // matrix - matrix
                 return matrixMatrixOp((ListNode) lhs, lhsDimensions, (ListNode) rhs, rhsDimensions, MatrixOp.SUB);
             } else if (isNumber(lhs) && rhsDimensions != null) {
                 // scalar - matrix
                 return matrixScalarOp((ListNode) rhs, rhsDimensions, (NumberNode) lhs, MatrixOp.RSB);
             } else if (lhsDimensions != null && isNumber(rhs)) {
                 // matrix - scalar
                 return matrixScalarOp((ListNode) lhs, lhsDimensions, (NumberNode) rhs, MatrixOp.SUB);
             } else {
                 throw expected("Subtract", "numeric or matrix");
             }
         }
     }
     
     public static class Sum extends CalculatorFunction { // FIXME: CAS support.
         public Sum() {
             super("sum", 3);
         }
         
         public Node apply(Calculator environment) {
             return series(environment, args, IntegerNode.ZERO, true);
         }
     }
     
     public static class Tan extends CalculatorFunctionN {
         public Tan() {
             super("tan");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().tan();
         }
     }
     
     public static class Tanh extends CalculatorFunctionN {
         public Tanh() {
             super("tanh");
         }
         
         public Node apply(Calculator environment, NumberNode n) {
             return n.toReal().tanh();
         }
     }
     
     public static class Times extends CalculatorFunction { // FIXME: CAS support.
         public Times() {
             super("Times", 2);
         }
         
         public Node apply(Calculator environment) {
             final Node lhs = arg(environment, 0);
             final Node rhs = arg(environment, 1);
             if (isNumber(lhs) && isNumber(rhs)) {
                 // scalar * scalar
                 return ((NumberNode) lhs).times((NumberNode) rhs);
             }
             final ListNode lhsDimensions = matrixDimensions(lhs);
             final ListNode rhsDimensions = matrixDimensions(rhs);
             if (lhsDimensions != null && rhsDimensions != null) {
                 // matrix * matrix
                 return matrixTimes((ListNode) lhs, lhsDimensions, (ListNode) rhs, rhsDimensions);
             } else if (isNumber(lhs) && rhsDimensions != null) {
                 // scalar * matrix
                 return matrixScalarOp((ListNode) rhs, rhsDimensions, (NumberNode) lhs, MatrixOp.MUL);
             } else if (lhsDimensions != null && isNumber(rhs)) {
                 // matrix * scalar
                 return matrixScalarOp((ListNode) lhs, lhsDimensions, (NumberNode) rhs, MatrixOp.MUL);
             } else {
                 throw expected("Times", "numeric or matrix");
             }
         }
         
         // Multiply an m*n matrix by an n*p matrix.
         // http://en.wikipedia.org/wiki/Matrix_multiplication
         private static Node matrixTimes(ListNode lhs, ListNode lhsDimensions, ListNode rhs, ListNode rhsDimensions) {
             final IntegerNode n = (IntegerNode) lhsDimensions.get(1);
             if (!n.equals((IntegerNode) rhsDimensions.get(0))) {
                 throw new CalculatorError("matrix multiplication requires compatible matrices");
             }
             final IntegerNode rowCount = (IntegerNode) lhsDimensions.get(0);
             final IntegerNode columnCount = (IntegerNode) rhsDimensions.get(1);
             final ListNode result = new ListNode();
             for (IntegerNode i = IntegerNode.ZERO; cmp(i, rowCount) < 0; i = i.increment()) {
                 final ListNode newRow = new ListNode();
                 for (IntegerNode j = IntegerNode.ZERO; cmp(j, columnCount) < 0; j = j.increment()) {
                     newRow.add(dotProduct(lhs, rhs, i, j, n));
                 }
                 result.add(newRow);
             }
             return result;
         }
         
         // Returns the sum from r=0..n-1 of A(i,r)*B(r,j).
         // (It's 0..n-1 instead of 1..n because we use zero-based indexes internally.)
         // http://en.wikipedia.org/wiki/Dot_product
         private static Node dotProduct(ListNode A, ListNode B, IntegerNode i, IntegerNode j, IntegerNode n) {
             NumberNode result = IntegerNode.ZERO;
             for (IntegerNode r = IntegerNode.ZERO; cmp(r, n) < 0; r = r.increment()) {
                 final NumberNode Air = (NumberNode) matrixElementAt(A, i, r);
                 final NumberNode Brj = (NumberNode) matrixElementAt(B, r, j);
                 result = result.plus(Air.times(Brj));
             }
             return result;
         }
         
         @Override public Node simplify(Calculator environment) {
             final ArrayList<Node> args = simplifyArgs(environment);
             NumberNode total = IntegerNode.ONE;
             for (int i = args.size() - 1; i >= 0; --i) {
                 final Node arg = args.get(i);
                 if (isNumber(arg)) {
                     if (!isOne(arg)) {
                         total = total.times((NumberNode) arg);
                     }
                     args.remove(i);
                 }
             }
             if (isZero(total)) {
                 return total;
             }
             if (args.size() == 0 || !isOne(total)) {
                 // Sort numeric factors first in products (preferring "2*x" rather than "x*2")..
                 args.add(0, total);
             }
             if (args.size() == 1) {
                 return args.get(0);
             }
             return bind(args);
         }
     }
     
     public static class Total extends CalculatorFunctionL {
         public Total() {
             super("Total");
         }
         
         public Node apply(Calculator environment, ListNode list) {
             final CalculatorFunction plus = environment.getFunction("Plus");
             Node result = IntegerNode.ZERO;
             for (Node element : list) {
                 result = plus.bind(result, element);
             }
             return result.evaluate(environment);
         }
     }
     
     public static class Transpose extends CalculatorFunction { // FIXME: CAS support.
         public Transpose() {
             super("Transpose", 1);
         }
         
         public Node apply(Calculator environment) {
             final Node node = arg(environment, 0);
             final ListNode dimensions = matrixDimensions(node);
             if (dimensions == null) {
                 throw expected("Dimensions", "matrix");
             }
             final ListNode m = (ListNode) node;
             final IntegerNode rowCount = (IntegerNode) dimensions.get(1); // new row count == old column count.
             final IntegerNode columnCount = (IntegerNode) dimensions.get(0); // new column count == old row count.
             final ListNode result = new ListNode();
             for (IntegerNode i = IntegerNode.ZERO; cmp(i, rowCount) < 0; i = i.increment()) {
                 final ListNode newRow = new ListNode();
                 for (IntegerNode j = IntegerNode.ZERO; cmp(j, columnCount) < 0; j = j.increment()) {
                     newRow.add(matrixElementAt(m, j, i));
                 }
                 result.add(newRow);
             }
             return result;
         }
     }
     
     public static class Unequal extends CalculatorFunction { // FIXME: CAS support.
         public Unequal() {
             super("Unequal", 2);
         }
         
         public Node apply(Calculator environment) {
             return equal(arg(environment, 0), arg(environment, 1)).not();
         }
     }
 }
