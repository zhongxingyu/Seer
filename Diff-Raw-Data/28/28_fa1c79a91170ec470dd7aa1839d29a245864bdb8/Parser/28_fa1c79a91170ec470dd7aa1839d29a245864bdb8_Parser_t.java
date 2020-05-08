 /*
  * MathUtil.java
  *
  * Created on July 26, 2007, 10:51 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.math.expressionparser;
 
 import edu.gatech.statics.math.AffineQuantity;
 import edu.gatech.statics.math.Unit;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class Parser {
 
     /**
      * Attempts to parse and evaluate the expression as a linear symbolic term
      * That is, the expression has the form A+B*S, where A and B are known floating
      * point values, and S is an expression term. This method will fail and return
      * null if there is an error in the expression or if there are more than one symbolic
      * terms.
      */
     public static AffineQuantity evaluateSymbol(String expression) {
 
         expression = expression.trim();
 
         // trim the tailing * if it exists.
         if (expression.endsWith("*")) {
             expression = expression.substring(0, expression.length() - 1);
         }
 
         // handle the empty case right away
         if (expression.equals("")) {
             return new AffineQuantity(BigDecimal.ONE, BigDecimal.ZERO, null);
         }

         Parser parser = new Parser();
         Node topNode;
         SymbolNode symbol;
         try {
             topNode = parser.parse(expression);
             symbol = parser.getSymbol(topNode);
         } catch (NullPointerException ex) {
             return null;
         } catch (UnsupportedOperationException ex) {
             return null;
         }
 
         // for the simple case if the symbol term is not there in the first place
         if (symbol == null) {
             BigDecimal constant = new Parser().evaluateInternal(expression);
             if (constant == null) {
                 return null;
             }
             AffineQuantity result = new AffineQuantity(constant, BigDecimal.ZERO, null);
             return result;
         }
 
         // here we go upwards, looking to build a chain from the symbol node
         // to the top of the expression.
         List<Node> symbolChain = new ArrayList<Node>();
         List<Integer> branch = new ArrayList<Integer>();
         for (Node current = symbol; current != null; current = current.getParent()) {
             symbolChain.add(current);
 
             if (current instanceof BinaryNode) {
                 BinaryNode binaryNode = (BinaryNode) current;
                 SymbolNode leftSymbol = parser.getSymbol(binaryNode.getChild1());
                 SymbolNode rightSymbol = parser.getSymbol(binaryNode.getChild2());
 
                 if (leftSymbol == symbol) {
                     branch.add(1);
                 } else if (rightSymbol == symbol) {
                     branch.add(2);
                 } else if (leftSymbol != null || rightSymbol != null) {
                     // **** HERE We have determined that this is not the only symbol in the heirarchy
                     // this cannot be parsed, so we return null here
                     return null;
                 }
                 if (leftSymbol != null && rightSymbol != null) {
                     // there are two symbols in the heirarchy. This is bad, so we return null here
                     return null;
                 }
             } else {
                 branch.add(0);
             }
         }
 
         // check the heirarchy to see if any term is nonlinear
         for (int i = 0; i < symbolChain.size(); i++) {
             Node node = symbolChain.get(i);
             int thisBranch = branch.get(i);
 
             if (node instanceof UnaryNode) {
                 UnaryNode unaryNode = (UnaryNode) node;
                 if (unaryNode.getOperation() == UnaryNode.Operation.identity ||
                         unaryNode.getOperation() == UnaryNode.Operation.negate) {
                     //continue;
                 } else {
                     // symbol is underneath a nonlinear term, like sqrt, sin, cos, atan, etc
                     return null;
                 }
             } else if (node instanceof BinaryNode) {
                 BinaryNode binaryNode = (BinaryNode) node;
                 if (binaryNode.getOperation() == BinaryNode.Operation.divide && thisBranch == 2) {
                     // the symbol is underneath a division sign, and is thus nonlinear.
                     return null;
                 }
             }
         }
 
         // if we have reached here, the symbol should be only underneath linear terms
         // this way we can go up from the top and collect the terms.
 
         //SymbolResult result = new SymbolResult();
         String symbolName = symbol.getSymbolName();
         BigDecimal multiplier = BigDecimal.ONE;
         BigDecimal constant = BigDecimal.ZERO;
 
         // start at 1 as to skip the original symbol node
         for (int i = 1; i < symbolChain.size(); i++) {
             Node node = symbolChain.get(i);
             int thisBranch = branch.get(i);
 
             if (node instanceof UnaryNode) {
                 UnaryNode unaryNode = (UnaryNode) node;
                 if (unaryNode.getOperation() == UnaryNode.Operation.negate) {
                     constant = constant.negate();
                     multiplier = multiplier.negate();
                 }
             // the only other option would be identity, and this leaves the values
             // the same, so it is not necessary to check for it
             } else if (node instanceof BinaryNode) {
                 BinaryNode binaryNode = (BinaryNode) node;
                 BigDecimal otherValue;
 
                 try {
                     if (thisBranch == 1) {
                         otherValue = binaryNode.getChild2().evaluate();
                     } else {
                         otherValue = binaryNode.getChild1().evaluate();
                     }
                 } catch (UnsupportedOperationException ex) {
                     // this might happen in case another symbol slips through.
                     return null;
                 }
 
                 if (otherValue == null) {
                     // this is unexpected, there is some evaluation error within this branch.
                     return null;
                 }
 
                 switch (binaryNode.getOperation()) {
                     case add:
                         //result.constant += otherValue;
                         constant = constant.add(otherValue);
                         break;
                     case divide:
                         constant = constant.divide(otherValue, Unit.getPrecision(), RoundingMode.HALF_UP);
                         multiplier = multiplier.divide(otherValue, Unit.getPrecision(), RoundingMode.HALF_UP);
                         break;
                     case multiply:
                         constant = constant.multiply(otherValue);
                         multiplier = multiplier.multiply(otherValue);
                         break;
                     case subtract:
                         if (thisBranch == 1) {
                             constant = constant.subtract(otherValue);
                         } else {
                             constant = otherValue.subtract(constant);
                             multiplier = multiplier.negate();
                         }
                 }
             }
         }
 
         return new AffineQuantity(constant, multiplier, symbolName);
     }
 
     public static BigDecimal evaluate(String expression) {
         // parse non-symbolic expressions
         BigDecimal result = new Parser().evaluateInternal(expression);
         return result;
     //if (result == null) {
     //    return Float.NaN;
     //}
     //return result.floatValue();
     }
 
     private List<String> tokenize(String s) {
         // SUPPOSEDLY
         // there is a better way to do this with the REGEX package,
         // however, I can see no way for actually returning delimiters with regex.
         // we need delimiters, so we use StringTokenizer for now.
 
         StringTokenizer tokenizer = new StringTokenizer(s, "+-*/()", true);
         List<String> r = new ArrayList();
         while (tokenizer.hasMoreTokens()) {
             r.add(tokenizer.nextToken());
         }
         return r;
     }
     private Node rootNode = new UnaryNode(UnaryNode.Operation.identity);
     private Node currentNode = rootNode;
 
     /**
      * Attempt to find a symbol node within this root node.
      * @param node
      * @return the symbol name if one exists, null otherwise.
      */
     private SymbolNode getSymbol(Node node) {
         if (node instanceof SymbolNode) {
             return (SymbolNode) node;
         }
         if (node instanceof ConstantNode) {
             return null;
         }
         if (node instanceof UnaryNode) {
             return getSymbol(((UnaryNode) node).getChild());
         }
         if (node instanceof BinaryNode) {
             BinaryNode binaryNode = (BinaryNode) node;
             SymbolNode symbol1 = getSymbol(binaryNode.getChild1());
             if (symbol1 != null) {
                 return symbol1;
             }
             return getSymbol(binaryNode.getChild2());
         }
 
         // should not get here
         return null;
     }
 
     Node parse(String expression) {
 
         List<String> tokens = tokenize(expression);
 
         for (String token : tokens) {
             addToken(token);
         }
         return rootNode;
     }
 
     private BigDecimal evaluateInternal(String expression) {
         try {
 
             // automatically handle a single minus sign as a negation
             if (expression.trim().equals("-")) {
                 return BigDecimal.ONE.negate();
 
             // shave off a * multiplier if the user has added one
             }
             if (expression.endsWith("*")) {
                 expression = expression.substring(0, expression.length() - 1);
             }
             Node node = parse(expression);
             BigDecimal result = node.evaluate();
             return result.setScale(Unit.getPrecision(), RoundingMode.HALF_UP);
 
         } catch (UnsupportedOperationException e) {
             return null;
         } catch (NullPointerException e) {
             return null;
         }
     }
 
     private void addToken(String token) {
 
         // will need to account for special cases: 
         // negation, and end parentheses
 
         if (token.trim().equals(")")) {
 
             // go up until we hit the matching identity node created by a "("
             while (!(currentNode instanceof UnaryNode &&
                     ((UnaryNode) currentNode).getOperation() == UnaryNode.Operation.identity)) {
                 currentNode = currentNode.getParent();
             }
 
             // pop up if this is a sin(...), so currentNode points to sin and not the parens.
             if (currentNode.getParent() != null &&
                     currentNode.getParent() instanceof UnaryNode &&
                     ((UnaryNode) currentNode.getParent()).getOperation() != UnaryNode.Operation.identity) {
                 // this is a unary operation surrounding the parens. Pop to it
                 currentNode = currentNode.getParent();
             }
         }
 
         // first try to create a node out of the token:
         Node newNode = createNode(token);
 
         if (newNode instanceof BinaryNode) {
 
             BinaryNode newBinary = (BinaryNode) newNode;
             Node lhsParent = getLHSParent(newBinary.getPrecedence());
 
             if (lhsParent instanceof UnaryNode) {
                 UnaryNode unaryLhsParent = (UnaryNode) lhsParent;
 
                 if (unaryLhsParent.getOperation() == UnaryNode.Operation.identity) {
                     Node lhs = unaryLhsParent.getChild();
                     newBinary.addChild(lhs);
                     unaryLhsParent.setChild(newBinary);
                 } else {
                     Node newParent = unaryLhsParent.getParent();
                     if (newParent instanceof UnaryNode) {
                         ((UnaryNode) newParent).setChild(newBinary);
                     } else {
                         ((BinaryNode) newParent).setChild2(newBinary); // ??
 
                     }
                     newBinary.addChild(unaryLhsParent);
                 }
             } else {
                 BinaryNode binaryLhsParent = (BinaryNode) lhsParent;
 
                 Node lhs = binaryLhsParent.getChild2();
                 newBinary.addChild(lhs);
                 binaryLhsParent.setChild2(newBinary);
             }
             currentNode = newBinary;
 
         } else if (newNode instanceof ConstantNode || newNode instanceof SymbolNode) {
 
             currentNode.addChild(newNode);
             currentNode = newNode;
         // this is a finish
 
         } else if (newNode instanceof UnaryNode) {
 
             currentNode.addChild(newNode);
             currentNode = newNode;
         }
     }
 
     private Node getLHSParent(int precedence) {
 
         Node lhs = currentNode;
         while (true) {
             if (lhs.getParent() instanceof UnaryNode) {
                 return lhs.getParent();
             }
             if (lhs.getParent() instanceof BinaryNode) {
                 if (((BinaryNode) lhs.getParent()).getPrecedence() < precedence) {
                     return lhs.getParent();
                 // HERE the left hand side has less precedence than the new
                 // binary operator. Thus we steal its rhs child.
                 // otherwise we continue upwards.
                 }
             }
             lhs = lhs.getParent();
         }
     }
 
     private Node createNode(String token) {
         token = token.trim();

        if ("".equals(token)) {
            // in the case that our token is empty, just return null
            // the parser should skip this token and move onto the next
            return null;
        }
         if (token.equals("+")) {
             return new BinaryNode(BinaryNode.Operation.add);
         }
         if (token.equals("*")) {
             return new BinaryNode(BinaryNode.Operation.multiply);
         }
         if (token.equals("/")) {
             return new BinaryNode(BinaryNode.Operation.divide);
         }
         if (token.equals("(")) {
             return new UnaryNode(UnaryNode.Operation.identity);
         }
         if (token.toLowerCase().equals("sin")) {
             return new UnaryNode(UnaryNode.Operation.sin);
         }
         if (token.toLowerCase().equals("cos")) {
             return new UnaryNode(UnaryNode.Operation.cos);
         }
         if (token.toLowerCase().equals("atan")) {
             return new UnaryNode(UnaryNode.Operation.atan);
         }
         if (token.toLowerCase().equals("sqrt")) {
             return new UnaryNode(UnaryNode.Operation.sqrt);
         // special case of subtraction / negation
         }
         if (token.equals("-")) {
             if (currentNode instanceof UnaryNode && ((UnaryNode) currentNode).getChild() == null) {
                 return new UnaryNode(UnaryNode.Operation.negate);
             }
             return new BinaryNode(BinaryNode.Operation.subtract);
         }
         if (token.toLowerCase().equals("pi")) {
             return new ConstantNode((float) Math.PI);
         }
 
         try {
             float f = Float.parseFloat(token);
             return new ConstantNode(f);
         } catch (NumberFormatException e) {
         }
 
         // Here we attempt to treat this as a symbol, if the first digit is a letter
         // Not all nodes may be permissible as symbols, but we'll try here and see what happens
         if (Character.isLetter(token.charAt(0))) {
             return new SymbolNode(token);
         }
 
         // otherwise return null.
         return null;
     }
 }
