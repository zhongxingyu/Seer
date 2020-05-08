 package de.skuzzle.polly.parsing.tree.operators;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Stack;
 
 import de.skuzzle.polly.parsing.ExecutionException;
 import de.skuzzle.polly.parsing.ListType;
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.parsing.Position;
 import de.skuzzle.polly.parsing.Token;
 import de.skuzzle.polly.parsing.TokenType;
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.declarations.Namespace;
 import de.skuzzle.polly.parsing.tree.BinaryExpression;
 import de.skuzzle.polly.parsing.tree.Expression;
 import de.skuzzle.polly.parsing.tree.literals.BooleanLiteral;
 import de.skuzzle.polly.parsing.tree.literals.DateLiteral;
 import de.skuzzle.polly.parsing.tree.literals.ListLiteral;
 import de.skuzzle.polly.parsing.tree.literals.Literal;
 import de.skuzzle.polly.parsing.tree.literals.NumberLiteral;
 import de.skuzzle.polly.parsing.tree.literals.StringLiteral;
 import de.skuzzle.polly.parsing.tree.literals.TimespanLiteral;
 
 
 /**
  * This class contains all {@link BinaryOperatorOverload} as static
  * classes.
  * 
  * @author Simon
  *
  */
 public class BinaryOperators {
     
     
     
     public static class ArithemticDateTimespanOperator extends BinaryOperatorOverload {
 
         private static final long serialVersionUID = 1L;
 
 
         public ArithemticDateTimespanOperator(TokenType operator) {
             super(operator, Type.DATE, Type.TIMESPAN, Type.DATE);
         }
 
         
         
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             TimespanLiteral right = (TimespanLiteral) stack.pop();
             Date left = ((DateLiteral) stack.pop()).getValue();
             
             
             switch (this.getOperatorType()) {
             case ADD:
                 stack.push(new DateLiteral(right.getTarget(left)));
                 break;
             case SUB:
                 TimespanLiteral tmp = new TimespanLiteral(-right.getValue());
                 stack.push(new DateLiteral(tmp.getTarget(left)));
                 break;
             }
         }
     }
     
     
     
     public static class ArithmeticDateOperator extends BinaryOperatorOverload {
         
         private static final long serialVersionUID = 1L;
         
         public ArithmeticDateOperator(TokenType operator) {
             super(operator, Type.DATE, Type.DATE, Type.TIMESPAN);
         }
         
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             Date right = ((DateLiteral) stack.pop()).getValue();
             Date left = ((DateLiteral) stack.pop()).getValue();
           
             switch(this.getOperatorType()) {
                 case SUB:
                     stack.push(new TimespanLiteral(
                         Math.abs((left.getTime() - right.getTime()) / 1000)));
                     break;
             }
         }
     }
     
     
     
     public static class ArithmeticTimespanOperator extends BinaryOperatorOverload {
 
         private static final long serialVersionUID = 1L;
 
         public ArithmeticTimespanOperator(TokenType operator) {
             super(operator, Type.TIMESPAN, Type.TIMESPAN, Type.TIMESPAN);
         }
         
         
 
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             long right = ((TimespanLiteral) stack.pop()).getValue();
             long left = ((TimespanLiteral) stack.pop()).getValue();
           
             switch(this.getOperatorType()) {
                 case ADD:
                     stack.push(new TimespanLiteral(left + right));
                     break;
                 case SUB:
                     stack.push(new TimespanLiteral(left - right));
                     break;
             }
         }
     }
     
     
     /**
      * This binary overload takes two {@code NUMBER} arguemnts and returns a 
      * {@code NUMBER}
      * 
      * @author Simon
      *
      */
     public static class ArithmeticOperator extends BinaryOperatorOverload {
 
         private static final long serialVersionUID = 1L;
         
         public ArithmeticOperator(TokenType operator) {
             super(operator, Type.NUMBER, Type.NUMBER, Type.NUMBER);
         }
 
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {        
             NumberLiteral right = (NumberLiteral) stack.pop();
             NumberLiteral left = (NumberLiteral) stack.pop();
             
             Position span = new Position(left.getPosition(), right.getPosition());
             switch(this.getOperatorType()) {
                 case ADD:
                     stack.push(new NumberLiteral(
                             left.getValue() + right.getValue(), span));
                     break;
                 case SUB:
                     stack.push(new NumberLiteral(
                             left.getValue() - right.getValue(), span));
                     break;
                 case MUL:
                     stack.push(new NumberLiteral(
                             left.getValue() * right.getValue(), span));
                     break;
                 case DIV:
                     stack.push(new NumberLiteral(
                             left.getValue() / 
                             right.nonZero(right.getPosition()), span));
                     break;
                 case INTDIV:
                     stack.push(new NumberLiteral(
                             left.isInteger(left.getPosition()) / 
                             right.nonZeroInteger(right.getPosition()), span));
                     break;
                 case MOD:
                     int rvalue = right.nonZeroInteger(right.getPosition());
                     int result = left.isInteger(left.getPosition()) % rvalue;
                         
                     if (result < 0) {
                         result += rvalue;
                     }
                     stack.push(new NumberLiteral(result, span));
                     break;
                 case POWER:
                     stack.push(new NumberLiteral(
                             Math.pow(left.getValue(), right.getValue()),
                             span));
                     break;
                 case INT_AND:
                     stack.push(new NumberLiteral(
                             left.isInteger() & right.isInteger(), span));
                     break;
                 case INT_OR:
                     stack.push(new NumberLiteral(
                             left.isInteger() | right.isInteger(), span));
                     break;
                 case INT_XOR:
                     stack.push(new NumberLiteral(
                             left.isInteger() ^ right.isInteger(), span));
                     break;
                 case LEFT_SHIFT:
                     stack.push(new NumberLiteral(
                             left.isInteger() << right.isInteger(), span));
                     break;
                 case RIGHT_SHIFT:
                     stack.push(new NumberLiteral(
                         left.isInteger() >> right.isInteger(), span));
                     break;
                 case URIGHT_SHIFT:
                     stack.push(new NumberLiteral(
                         left.isInteger() >>> right.isInteger(), span));
                     break;
                 case CHOOSE:
                     int n = left.isInteger();
                     int k = right.isInteger();
                     int bin = binomialCoefficient(BigInteger.valueOf(n), 
                             BigInteger.valueOf(k)).intValue();
                     stack.push(new NumberLiteral(bin, span));
                     break;
                 case RADIX:
                     right.setRadix(left.isInteger());
                     stack.push(right);
                     break;
             }
         }
         
         private static BigInteger binomialCoefficient(BigInteger n, BigInteger k){
             
             BigInteger n_minus_k=n.subtract(k);
             if(n_minus_k.compareTo(k)<0){
                 BigInteger temp=k;
                 k=n_minus_k;
                 n_minus_k=temp;
             }
             
             BigInteger numerator=BigInteger.ONE;
             BigInteger denominator=BigInteger.ONE;
             
             for(BigInteger j=BigInteger.ONE; j.compareTo(k)<=0; j=j.add(BigInteger.ONE)){
                 numerator=numerator.multiply(j.add(n_minus_k));
                 denominator=denominator.multiply(j);
                 BigInteger gcd=numerator.gcd(denominator);
                 numerator=numerator.divide(gcd);
                 denominator=denominator.divide(gcd);
             }
             
             return numerator;
         }
     }
     
     
     
     public static class ListScalarOperator extends BinaryOperatorOverload {
         
         private static final long serialVersionUID = 1L;
         private ListLiteral resultList;
         
         public ListScalarOperator(TokenType operator) {
             super(operator, new ListType(Type.ANY), Type.ANY, Type.ANY);
         }
         
         
         @Override
         public void contextCheck(Namespace context, Expression left,
                 Expression right) throws ParseException {
 
             super.contextCheck(context, left, right);
 
             // TODO: left operand can be any expression which evaluates to a list after
             //       execution.
             if (!(left instanceof ListLiteral)) {
                 throw new ParseException(
                     "Diese Operation ist leider (noch) nicht mglich", 
                     right.getPosition());
             }
             
             ListLiteral list = (ListLiteral) left;
             this.resultList = new ListLiteral(new ArrayList<Expression>());
             Token op = new Token(this.getOperatorType(), Position.EMPTY);
             for (Expression e : list.getElements()) {
                 Expression bin = new BinaryExpression(e, op, right);
                 bin = bin.contextCheck(context);
                 
                 this.resultList.getElements().add(bin);
             }
             this.setReturnType(this.resultList.getType());
         }
         
         
         
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             this.resultList.collapse(stack);
         }
     }
     
     
     
     /**
      * Arithmetic list operator overload. It taks two {@code LIST(of ANY)} and returns
      * a {@code LIST} of the same type.
      * @author Simon
      *
      */
     public static class ListArithmeticOperator extends BinaryOperatorOverload {
 
         
         private static final long serialVersionUID = 1L;
         
         public ListArithmeticOperator(TokenType operator) {
             super(operator, new ListType(Type.ANY), new ListType(Type.ANY), 
                     new ListType(Type.ANY));
         }
         
         @Override
         public void contextCheck(Namespace context, Expression left, Expression right)
                 throws ParseException {
             super.contextCheck(context, left, right);
             
             if (!left.getType().check(right.getType())) {
                 Type.typeError(right.getType(), left.getType(), right.getPosition());
             }
             
             
             ListType leftList = (ListType) left.getType();
             this.setReturnType(new ListType(leftList.getSubType()));
         }
 
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {        
             ListLiteral right = (ListLiteral) stack.pop();
             ListLiteral left = (ListLiteral) stack.pop();
             
             List<Expression> newList;
             switch(this.getOperatorType()) {
                 case ADD:
                     newList = new ArrayList<Expression>(left.getElements());
                     newList.addAll(right.getElements());
                     stack.push(new ListLiteral(newList));
                     break;
                 case SUB:
                     newList = new ArrayList<Expression>(left.getElements());
                     newList.removeAll(right.getElements());
                     stack.push(new ListLiteral(newList));
                     break;
                 case WAVE:
                     newList = new ArrayList<Expression>(left.getElements());
                     newList.retainAll(right.getElements());
                     stack.push(new ListLiteral(newList));
                     break;
                 case ADDWAVE:
                     newList = new ArrayList<Expression>(left.getElements());
                     newList.removeAll(right.getElements());
                     newList.addAll(right.getElements());
                     stack.push(new ListLiteral(newList));
                     break;
             }
         }
     }
     
     
     
     /**
      * This binary operator overload takes two {@code BOOLEAN} parameters and
      * returns a {@code BOOLEAN}.
      * 
      * @author Simon
      *
      */
     public static class BooleanOperator extends BinaryOperatorOverload {
 
         private static final long serialVersionUID = 1L;
         
         public BooleanOperator(TokenType operator) {
             super(operator, Type.BOOLEAN, Type.BOOLEAN, Type.BOOLEAN);
         }
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             BooleanLiteral right = (BooleanLiteral) stack.pop();
             BooleanLiteral left = (BooleanLiteral) stack.pop();
             
             switch(this.getOperatorType()) {
                 case BOOLEAN_AND:
                     stack.push(new BooleanLiteral(left.getValue() && right.getValue()));
                     break;
                 case BOOLEAN_OR:
                     stack.push(new BooleanLiteral(left.getValue() || right.getValue()));
                     break;
                 case XOR:
                     stack.push(new BooleanLiteral(left.getValue() ^ right.getValue()));
                     break;
             }
         }
     }
     
     
     
     /**
      * String concatenation operator. Takes two {@code STRING} arguments and returns
      * a {@code STRING}.
      * 
      * @author Simon
      *
      */
     public static class ConcatStringOperator extends BinaryOperatorOverload {
 
         
         private static final long serialVersionUID = 1L;
         
         public ConcatStringOperator() {
             super(TokenType.ADD, Type.STRING, Type.STRING, Type.STRING);
         }
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             StringLiteral right = (StringLiteral) stack.pop();
             StringLiteral left = (StringLiteral) stack.pop();
             
             switch(this.getOperatorType()) {
                 case ADD:
                     stack.push(new StringLiteral(left.getValue() + right.getValue()));
                     break;
             }
         }
     }
     
     
     
     /**
      * Binary equality operator overload. Takes any type on the left side of the
      * operator and expects the right side to have the same type. It returns a
      * {@code BOOLEAN}.
      * 
      * @author Simon
      *
      */
     public static class EqualityOperator extends BinaryOperatorOverload {
         
         private static final long serialVersionUID = 1L;
         
         public EqualityOperator(TokenType operator) {
             super(operator, Type.UNKNOWN, Type.UNKNOWN, Type.BOOLEAN);
         }
 
         @Override
         public BinaryOperatorOverload match(TokenType operator, Type left, Type right) {
             return this.getOperatorType() == operator && left.check(right) ? this : null;
         }
         
         @Override
         public void contextCheck(Namespace context, Expression left, 
                 Expression right) throws ParseException {
             if (!left.getType().check(right.getType())) {
                 Type.typeError(
                         right.getType(), left.getType(), left.getPosition());
             }
         }
 
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             Literal right = stack.pop();
             Literal left = stack.pop();
             
             switch(this.getOperatorType()) {
                 case EQ:
                     stack.push(new BooleanLiteral(left.equals(right)));
                     break;
                 case NEQ:
                     stack.push(new BooleanLiteral(!left.equals(right)));
                     break;
             }
         }
     }
     
     
     
     /**
      * Index operator overload for lists. It takes two {@code LIST(of ANY)} arguemnts
      * and returns a value with the resolved type {@code ANY}.
      * 
      * @author Simon
      *
      */
     public static class IndexListOperator extends BinaryOperatorOverload {
 
         
         private static final long serialVersionUID = 1L;
         
         public IndexListOperator() {
             super(TokenType.INDEX, new ListType(Type.ANY), Type.NUMBER, Type.UNKNOWN);
         }
         
         @Override
         public void contextCheck(Namespace context, Expression left, 
                 Expression right) throws ParseException {
             super.contextCheck(context, left, right);
             
             ListType t = ((ListType) left.getType());
             this.setReturnType(t.getSubType());
         }
 
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             NumberLiteral right = (NumberLiteral) stack.pop();
             ListLiteral left = (ListLiteral) stack.pop();
             
             switch(this.getOperatorType()) {
                 case INDEX:
                     int i = right.isInteger();
 
                     if (i >= left.getElements().size() || i < 0) {
                         throw new ExecutionException(
                            "Ungültiger Index: " + i + "(" + 
                             left.getElements().size() + ")", right.getPosition());
                     }
                     
                     /*
                      * Calculate the result of the list element
                     
                     Stack<Literal> s = new Stack<Literal>();
                     left.getElements().get(i).collapse(s);*/
                     stack.push((Literal) left.getElements().get(i));
                     
                     break;
             }
         }
     }
     
     
     
     /**
      * Index operator overload for {@code STRING}s. It takes two {@code STRING}s and
      * returns a {@code STRING}.
      * 
      * @author Simon
      *
      */
     public static class IndexStringOperator extends BinaryOperatorOverload {
         
         private static final long serialVersionUID = 1L;
         
         public IndexStringOperator() {
             super(TokenType.INDEX, Type.STRING, Type.NUMBER, Type.STRING);
         }
         
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             NumberLiteral right = (NumberLiteral) stack.pop();
             StringLiteral left = (StringLiteral) stack.pop();
             
             switch(this.getOperatorType()) {
                 case INDEX:
                     int i = right.isInteger();
                     char[] chars = left.getValue().toCharArray();
                     
                     if (i >= chars.length) {
                         throw new ExecutionException("Ungltiger Index: " + i, 
                                 right.getPosition());
                     }
                     char c = left.getValue().toCharArray()[right.isInteger()];
                     stack.push(new StringLiteral("" + c));
                     break;
             }
         }
     }
     
     
     
     /*
      * RELATIONAL OPERATORS
      */
     
     
     /**
      * Relational operator overload to compare two {@code DATE}s.
      * 
      * @author Simon
      */
     public static class RelationalDateOperator extends BinaryOperatorOverload {
 
         private static final long serialVersionUID = 1L;
         
         public RelationalDateOperator(TokenType operator) {
             super(operator, Type.DATE, Type.DATE, Type.BOOLEAN);
         }
         
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             Literal right = stack.pop();
             Literal left = stack.pop();
             
             switch(this.getOperatorType()) {
                 case LT:
                     stack.push(new BooleanLiteral(left.compareTo(right) < 0));
                     break;
                 case ELT:
                     stack.push(new BooleanLiteral(left.compareTo(right) <= 0));
                     break;
                 case GT:
                     stack.push(new BooleanLiteral(left.compareTo(right) > 0));
                     break;
                 case EGT:
                     stack.push(new BooleanLiteral(left.compareTo(right) >= 0));
                     break;
             }
         }
     }
     
     
     
     /**
      * Relational operator to compare two {@code LIST(of ANY)}.
      * 
      * @author Simon
      *
      */
     public static class RelationalListOperator extends BinaryOperatorOverload {
 
         private static final long serialVersionUID = 1L;
         
         public RelationalListOperator(TokenType operator) {
             super(operator, ListType.ANY_LIST, ListType.ANY_LIST, Type.BOOLEAN);
         }
         
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             Literal right = stack.pop();
             Literal left = stack.pop();
             
             switch(this.getOperatorType()) {
                 case LT:
                     stack.push(new BooleanLiteral(left.compareTo(right) < 0));
                     break;
                 case ELT:
                     stack.push(new BooleanLiteral(left.compareTo(right) <= 0));
                     break;
                 case GT:
                     stack.push(new BooleanLiteral(left.compareTo(right) > 0));
                     break;
                 case EGT:
                     stack.push(new BooleanLiteral(left.compareTo(right) >= 0));
                     break;
             }
         }
     }
     
     
     
     /**
      * Relational operator overload to compare two {@code NUMBER}s.
      * @author Simon
      *
      */
     public static class RelationalNumberOperator extends BinaryOperatorOverload {
 
         private static final long serialVersionUID = 1L;
         
         public RelationalNumberOperator(TokenType operator) {
             super(operator, Type.NUMBER, Type.NUMBER, Type.BOOLEAN);
         }
         
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             Literal right = stack.pop();
             Literal left = stack.pop();
             
             switch(this.getOperatorType()) {
                 case LT:
                     stack.push(new BooleanLiteral(left.compareTo(right) < 0));
                     break;
                 case ELT:
                     stack.push(new BooleanLiteral(left.compareTo(right) <= 0));
                     break;
                 case GT:
                     stack.push(new BooleanLiteral(left.compareTo(right) > 0));
                     break;
                 case EGT:
                     stack.push(new BooleanLiteral(left.compareTo(right) >= 0));
                     break;
             }
         }
     }
     
     
     
     /**
      * Relational operator overload to compare two {@code STRING}s
      * @author Simon
      *
      */
     public static class RelationalStringOperator extends BinaryOperatorOverload {
 
         private static final long serialVersionUID = 1L;
         
         public RelationalStringOperator(TokenType operator) {
             super(operator, Type.STRING, Type.STRING, Type.BOOLEAN);
         }
                 
         @Override
         public void collapse(Stack<Literal> stack) throws ExecutionException {
             Literal right = stack.pop();
             Literal left = stack.pop();
             
             switch(this.getOperatorType()) {
                 case LT:
                     stack.push(new BooleanLiteral(left.compareTo(right) < 0));
                     break;
                 case ELT:
                     stack.push(new BooleanLiteral(left.compareTo(right) <= 0));
                     break;
                 case GT:
                     stack.push(new BooleanLiteral(left.compareTo(right) > 0));
                     break;
                 case EGT:
                     stack.push(new BooleanLiteral(left.compareTo(right) >= 0));
                     break;
             }
         }
     }
 }
