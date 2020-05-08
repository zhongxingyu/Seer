 package de.skuzzle.polly.parsing.declarations;
 
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.tree.Expression;
 import de.skuzzle.polly.parsing.tree.literals.IdentifierLiteral;
 
 
 public class VarDeclaration extends Declaration {
 
     private static final long serialVersionUID = 1L;
     private Expression expression;
     private boolean local;
     
     public VarDeclaration(IdentifierLiteral id, boolean global, 
             boolean temp) {
         super(id, global, temp);
     }
     
     
     
     public VarDeclaration(IdentifierLiteral id, Type type) {
         super(id, false, false);
         this.setType(type);
     }
     
     
     
     public VarDeclaration(IdentifierLiteral id, Expression exp) {
         this(id, exp, false);
     }
     
     
     public VarDeclaration(IdentifierLiteral id, Expression exp, boolean local) {
         super(id, false, false);
         this.local = local;
         this.setExpression(exp);
     }
     
     
     
     public void setExpression(Expression expression) {
         this.expression = expression;
         this.setType(expression.getType());
     }
     
     
     
     public Expression getExpression() {
         return this.expression;
     }
     
     
     
     public boolean isLocal() {
         return this.local;
     }
     
     
     
     @Override
     public Object clone() {
         VarDeclaration result = new VarDeclaration(
             this.getName(),  // TODO: clone name? Might be impossibru because its clone method calls clone of declaration
             this.isGlobal(), this.isTemp());
         result.setExpression((Expression) this.getExpression().clone());
         return result;
     }
     
     
     
     @Override
     public String toString() {
        return "(VAR) " + (this.isGlobal() ? "global" : "") + this.getType() + " " + 
             this.getName();
     }
 }
