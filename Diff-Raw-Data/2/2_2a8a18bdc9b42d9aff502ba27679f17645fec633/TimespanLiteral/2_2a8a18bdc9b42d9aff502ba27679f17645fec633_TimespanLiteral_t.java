 package de.skuzzle.polly.parsing.tree.literals;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import de.skuzzle.polly.parsing.ExecutionException;
 import de.skuzzle.polly.parsing.Position;
 import de.skuzzle.polly.parsing.Token;
 import de.skuzzle.polly.parsing.TokenType;
 import de.skuzzle.polly.parsing.types.Type;
 
 
 public class TimespanLiteral extends Literal {
 
     private static final long serialVersionUID = 1L;
 
     
     
     public TimespanLiteral(Token token) {
         super(token, Type.TIMESPAN);
     }
     
     
     public TimespanLiteral(long value) {
         this(new Token(TokenType.TIMESPAN, Position.EMPTY, value));
     }
 
     
     public TimespanLiteral(long value, Position position) {
         this(new Token(TokenType.TIMESPAN, position, value));
     }
     
     
     public long getValue() {
         return this.getToken().getLongValue();
     }
     
     
     
     public Date getTarget(Date from) {
         Calendar target = Calendar.getInstance();
         target.setTime(from);
         target.add(Calendar.SECOND, (int)this.getValue());
         return target.getTime();
     }
     
     
     
     public Date getTargetFromNow() {
         return this.getTarget(new Date());
     }
     
     
     @Override
     public Literal castTo(Type target) throws ExecutionException {
         if (target.check(Type.DATE)) {
             return new DateLiteral(this.getTargetFromNow());
        } else if (target.check(Type.NUMBER)) {
             return new NumberLiteral(this.getValue());
         }
         return super.castTo(target);
     }
     
     
     @Override
     public int compareTo(Literal o) {
         if (o instanceof TimespanLiteral) {
             return (int) (this.getValue() - ((TimespanLiteral) o).getValue());
         }
         throw new RuntimeException("Not compareable");
     }
 }
