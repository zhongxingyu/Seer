 package org.rhino.js.dependencies.parser;
 
 import org.mozilla.javascript.Token;
 import org.mozilla.javascript.ast.*;
 import org.rhino.js.dependencies.models.FunctionName;
 
 /**
  * Visitor for Prototype functions.
  */
 public class ProtoypeFunctionVisitor extends FunctionVisitor {
 
         @Override
         public boolean visit(AstNode node) {
             if (node.getType() != Token.EXPR_RESULT) {
                 return false;
             }
 
             ExpressionStatement expr = (ExpressionStatement) node;
             AstNode exprNode = expr.getExpression();
             if (exprNode.getType() != Token.ASSIGN) {
                 return false;
             }
 
             Assignment asg = (Assignment) exprNode;
             if (asg.getLeft().getType() != Token.GETPROP) {
                 return false;
             }
 
             PropertyGet propertyGet = (PropertyGet) asg.getLeft();
             if (propertyGet.getRight().getType() == Token.NAME) {
                 String prototypeName = getPrototypeName(propertyGet.getLeft());
                 String functionName =propertyGet.getRight().getString();
 
                addElement(formatIfPrototyName(prototypeName, functionName));
             }
             return true;
         }
 
         private String getPrototypeName(AstNode leftNode) {
             // For prototype static methods declaration: Foo.bar = function() {};
             if (leftNode instanceof  Name) {
                 return leftNode.getString();
             }
 
             // For classic prototype methods declaration: Foo.prototype.bar = function() {};
             if (leftNode instanceof  PropertyGet) {
                 PropertyGet property = (PropertyGet) leftNode;
                 if (property.getLeft() instanceof  Name && equalsToPrototype(property.getRight())) {
                     return property.getLeft().getString();
                 }
 
             }
 
             return "";
         }
 
         private boolean equalsToPrototype(AstNode node) {
             if (!(node instanceof Name)) {
                 return false;
             }
             Name name = (Name) node;
 
             return "prototype".equals(name.getString());
         }
 
        private String formatIfPrototyName(String prototypeName, String functionName) {
            return new FunctionName(prototypeName, functionName).getString();
         }
 
 }
