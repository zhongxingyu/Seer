 /*
  * MathUtil.java
  *
  * Created on July 26, 2007, 10:51 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.gatech.statics.modes.equation.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class Parser {
     
     public static float evaluate(String expression) {
         // parse non-symbolic expressions
         return new Parser().evaluateInternal(expression);
     }
     
     private List<String> tokenize(String s) {
         // SUPPOSEDLY
         // there is a better way to do this with the REGEX package,
         // however, I can see no way for actually returning delimiters with regex.
         // we need delimiters, so we use StringTokenizer for now.
         
         StringTokenizer tokenizer = new StringTokenizer(s, "+-*/()", true);
         List<String> r = new ArrayList();
         while(tokenizer.hasMoreTokens())
             r.add( tokenizer.nextToken() );
         
         return r;
     }
     
     private Node rootNode = new UnaryNode(UnaryNode.Operation.identity);
     private Node currentNode = rootNode;
     
     Node parse(String expression) {
         
         List<String> tokens = tokenize(expression);
         
         for(String token : tokens)
             addToken(token);
         
         return rootNode;
     }
     
     private float evaluateInternal(String expression) {
         try {
             if(expression.trim().equals("-"))
                 return -1;
             
             return parse(expression).evaluate();
         } catch(NullPointerException e) {
             return Float.NaN;
         }
     }
     
     private void addToken(String token) {
         
         // will need to account for special cases: 
         // negation, and end parentheses
         
         if(token.trim().equals(")")) {
             
             while( !(currentNode instanceof UnaryNode &&
                     ((UnaryNode)currentNode).getOperation() == UnaryNode.Operation.identity) ) {
                 currentNode = currentNode.getParent();
             }
         }
         
         // first try to create a node out of the token:
         Node newNode = createNode(token);
         
         if(newNode instanceof BinaryNode) {
             
             BinaryNode newBinary = (BinaryNode) newNode;
             Node lhsParent = getLHSParent(newBinary.getPrecedence());
             
             if(lhsParent instanceof UnaryNode) {
                 UnaryNode unaryLhsParent = (UnaryNode) lhsParent;
                 
                 if(unaryLhsParent.getOperation() == UnaryNode.Operation.identity) {
                     Node lhs = unaryLhsParent.getChild();
                     newBinary.addChild(lhs);
                     unaryLhsParent.setChild(newBinary);
                 } else {
                     Node newParent = unaryLhsParent.getParent();
                     if(newParent instanceof UnaryNode)
                         ((UnaryNode)newParent).setChild(newBinary);
                     else ((BinaryNode)newParent).setChild2(newBinary); // ??
                     newBinary.addChild(unaryLhsParent);
                 }
             } else {
                 BinaryNode binaryLhsParent = (BinaryNode) lhsParent;
                 
                 Node lhs = binaryLhsParent.getChild2();
                 newBinary.addChild(lhs);
                 binaryLhsParent.setChild2(newBinary);
             }
             currentNode = newBinary;
             
         } else if(newNode instanceof ConstantNode) {
             
             currentNode.addChild(newNode);
             currentNode = newNode;
             // this is a finish
             
         } else if(newNode instanceof UnaryNode) {
             
             currentNode.addChild(newNode);
             currentNode = newNode;
         }
     }
     
     private Node getLHSParent(int precedence) {
         
         Node lhs = currentNode;
         while(true) {
             if(lhs.getParent() instanceof UnaryNode)
                 return lhs.getParent();
             
             if(lhs.getParent() instanceof BinaryNode)
                 if (((BinaryNode)lhs.getParent()).getPrecedence() < precedence )
                     return lhs.getParent();
                     // HERE the left hand side has less precedence than the new
                     // binary operator. Thus we steal its rhs child.
                     // otherwise we continue upwards.
             
             lhs = lhs.getParent();
         }
     }
     
     private Node createNode(String token) {
         token = token.trim();
         if(token.equals("+")) return new BinaryNode(BinaryNode.Operation.add);
         if(token.equals("*")) return new BinaryNode(BinaryNode.Operation.multiply);
         if(token.equals("/")) return new BinaryNode(BinaryNode.Operation.divide);
         if(token.equals("(")) return new UnaryNode(UnaryNode.Operation.identity);
         if(token.equals("sin")) return new UnaryNode(UnaryNode.Operation.sin);
         if(token.equals("cos")) return new UnaryNode(UnaryNode.Operation.cos);
         if(token.equals("atan")) return new UnaryNode(UnaryNode.Operation.atan);
         if(token.equals("sqrt")) return new UnaryNode(UnaryNode.Operation.sqrt);
         
         if(token.equals("-")) {
             if(currentNode instanceof UnaryNode && ((UnaryNode)currentNode).getChild() == null)
                 return new UnaryNode(UnaryNode.Operation.negate);
             return new BinaryNode(BinaryNode.Operation.subtract);
         }
         
         try {
             float f = Float.parseFloat(token);
             return  new ConstantNode(f);
         } catch(NumberFormatException e) {}
         
         return null;
     }
 }
