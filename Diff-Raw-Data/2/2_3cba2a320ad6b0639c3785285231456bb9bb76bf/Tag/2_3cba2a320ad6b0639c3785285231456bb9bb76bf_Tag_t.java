 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.parse;
 
 import student.grid.Critter;
 
 /**
  *
  * @author haro
  */
 class Tag extends Action {
     public Tag(Expression ind) {
         super(ind);
     }
     
     public Expression ind() {
         return children.get(0);
     }
 
     @Override
     public void execute(Critter c) {
        c._tag(ind().eval(c));
     }
 
     @Override
     public StringBuffer toString(StringBuffer sb) {
         sb.append("tag[");
         ind().toString(sb);
         sb.append("]");
         return sb;
     }
     
     
 }
