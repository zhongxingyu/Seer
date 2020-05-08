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
        throw new Error("Can't execute yet!");
     }
 
     @Override
     public StringBuffer toString(StringBuffer sb) {
         sb.append("tag[");
         ind().toString(sb);
         sb.append("]");
         return sb;
     }
     
     
 }
