 package de.skuzzle.polly.parsing.tree;
 
 import java.util.Stack;
 
 import de.skuzzle.polly.parsing.ExecutionException;
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.parsing.Position;
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.declarations.Declaration;
 import de.skuzzle.polly.parsing.declarations.FunctionDeclaration;
 import de.skuzzle.polly.parsing.declarations.Namespace;
 import de.skuzzle.polly.parsing.declarations.VarDeclaration;
 import de.skuzzle.polly.parsing.tree.literals.Literal;
import de.skuzzle.polly.parsing.tree.literals.StringLiteral;
 
 
 
 public class AssignmentExpression extends Expression {
 
     private static final long serialVersionUID = 1L;
 
     private Expression expression;
     private Declaration declaration;
 
     public AssignmentExpression(Expression expression, Position position,
             Declaration declaration) {
         super(position);
         this.expression = expression;
         this.declaration = declaration;
     }
     
     
     
     @Override
     public Expression contextCheck(Namespace context) throws ParseException {       
         /*
          * Note: check FunctionDeclaration before VarDeclaration, because 
          * FunctionDeclaration is a subclass of VarDeclaration! 
          */
         
         if (this.declaration instanceof FunctionDeclaration) {
             // no context check here!
             FunctionDeclaration func = (FunctionDeclaration) this.declaration;
             
             /* Resolve parameter types and add them as "empty" Expressions to the
              * local declarations. That means that any occurrence of a parameter 
              * variable on the left hand side of the declaration is replaced by an
              * Expression that only returns the type specified on the right hand side
              * of the declaration.
              */
             context.enter();
             
             Expression checked = null;
             try {
                 for (VarDeclaration param : func.getFormalParameters()) {
                     // Resolve the declared type
                     Type type = param.getExpression().contextCheck(context).getType();
                     param.setType(type);
                     param.setLocal(true);
                     context.addNormal(param);
                 }
                 
                 /* The name of the function being declared may not occur on the 
                  * left side. So mark it as forbidden.
                  */
                 context.forbidFunction(func);
     
                 // Check context, but do not replace the root of the left subtree 
                 // (=> store result as a new expression)
                 checked = this.expression.contextCheck(context);
             } finally {
                 // make sure to always leave the declarations clean
                 context.allowFunction();
                 context.leave();
             }
             
             
             
             this.expression.setType(checked.getType());
             func.setExpression(this.expression);
             
             // Declarations must always be stored at root level!
             context.addRoot(func);
 
            // Function definitions return a string indicating success
            this.setType(Type.STRING);
         } else if (this.declaration instanceof VarDeclaration) {
             
             this.expression = this.expression.contextCheck(context);
             
            //((VarDeclaration) this.declaration).setExpression(this.expression);
             
            this.declaration.setType(this.expression.getType());
             // Declarations must always be stored at root level!
             context.addRoot(this.declaration);
             this.setType(this.expression.getType());
             
            // return this.expression;
         }
         return this;
     }
 
     
     
     @Override
     public void collapse(Stack<Literal> stack) throws ExecutionException {
        if (this.declaration instanceof FunctionDeclaration) {
            stack.push(new StringLiteral("Funktion gespeichert"));
        } else if (this.declaration instanceof VarDeclaration) {
            this.expression.collapse(stack);
            ((VarDeclaration) this.declaration).setExpression(stack.peek());
        }
     }
 }
