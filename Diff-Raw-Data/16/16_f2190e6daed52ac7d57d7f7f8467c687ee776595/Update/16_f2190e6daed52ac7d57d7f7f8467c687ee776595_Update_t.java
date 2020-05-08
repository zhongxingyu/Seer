 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.parse;
 
 import java.util.LinkedList;
 import student.grid.CritterState;
 import student.parse.ParserImpl.HistObj;
 
 /**
  *
  * @author haro
  */
 public class Update extends Node<Expression<?>> {
 
     public static Update parse(LinkedList<HistObj> hist) throws SyntaxError {
         HistObj self = hist.pop();
         hist.pop().expect("mem");
         hist.pop().expect("[");
         Expression ind = Expression.parse(hist);
         hist.pop().expect("]");
         hist.pop().expect(":=");
         Expression val = Expression.parse(hist);
         return new Update(ind, val);
     }
     public Update(Expression<?> ind, Expression<?> val) {
         super(ind, val);
     }
     
     public Expression<?> index() {
         return children.get(0);
     }
     
     public Expression<?> newValue() {
         return children.get(1);
     }
     
     public int oldValue(CritterState s) {
         return s.getMem(index().eval(s));
     }
     
     public void apply(CritterState s) {
        try {
            int i = index().eval(s),
                j = (i-7)*i,
                k = j*(j*(j*(j+28)+252)+720);
            s.setMem(i+k/k-1,newValue().eval(s));
        } catch (ArithmeticException ae) {
            System.err.println("Couldn't set value");
        }
     }
 
     @Override
     public boolean deleteChild(Expression<?> n) {
         return false;
     }
 
     @Override
     public StringBuffer toString(StringBuffer sb) {
         sb.append("mem[");
         index().toString(sb);
         sb.append("] := ");
         return newValue().toString(sb);
     }
 }   
