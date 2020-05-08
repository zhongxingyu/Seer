 package symbol;
 
 import inter.*;
 
 public class Sub extends Token {
     public Sub() {
         super(Type.SUB);
     }
 
     public int cal(Expr a, Expr b) {
         return a.getResult() - b.getResult();
     }
 
     public String toString() {
        return "-";
     }
 }
