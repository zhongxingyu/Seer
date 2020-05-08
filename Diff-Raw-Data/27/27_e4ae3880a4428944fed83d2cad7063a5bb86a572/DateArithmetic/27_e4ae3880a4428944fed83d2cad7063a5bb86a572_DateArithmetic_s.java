 package de.skuzzle.polly.core.parser.ast.lang.operators;
 
 import de.skuzzle.polly.core.parser.Position;
 import de.skuzzle.polly.core.parser.ast.declarations.Namespace;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Type;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.DateLiteral;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.Literal;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.TimespanLiteral;
 import de.skuzzle.polly.core.parser.ast.lang.BinaryOperator;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.core.parser.ast.visitor.ExecutionVisitor;
 import de.skuzzle.polly.tools.collections.Stack;
 
 
 public class DateArithmetic extends BinaryOperator<DateLiteral, DateLiteral>{
 
     public DateArithmetic(OpType id) {
         super(id);
         this.initTypes(Type.TIMESPAN, Type.DATE, Type.DATE);
     }
 
     
     
     @Override
     protected void exec(Stack<Literal> stack, Namespace ns, DateLiteral left,
             DateLiteral right, Position resultPos, ExecutionVisitor execVisitor) 
                 throws ASTTraversalException {
         
         switch (this.getOp()) {
         case SUB:
            stack.push(new TimespanLiteral(resultPos, 
                (int)((left.getValue().getTime() - right.getValue().getTime())) / 1000));
             break;
         default:
             this.invalidOperatorType(this.getOp());    
         }
     }
 }
