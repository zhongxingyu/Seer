 package de.skuzzle.polly.core.parser.ast.lang.operators;
 
import java.util.Calendar;

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
            // Timezone aware date subtraction
            // (http://user.xmission.com/~goodhill/dates/deltaDates.html)
            final Calendar l = Calendar.getInstance();
            l.setTime(left.getValue());
            final Calendar r = Calendar.getInstance();
            r.setTime(right.getValue());
            long endl = r.getTimeInMillis() + r.getTimeZone().getOffset(r.getTimeInMillis());
            long startl = l.getTimeInMillis() + l.getTimeZone().getOffset(l.getTimeInMillis());
            long seconds = (startl - endl) / 1000;
            
            stack.push(new TimespanLiteral(resultPos, (int) seconds));
             break;
         default:
             this.invalidOperatorType(this.getOp());    
         }
     }
 }
