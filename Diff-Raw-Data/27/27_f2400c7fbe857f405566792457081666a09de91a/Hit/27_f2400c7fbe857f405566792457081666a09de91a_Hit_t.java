 package model;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author : sergiyloza
  * @date : 03/05/2013
  * @time : 18:04
  */
 public class Hit {
 
     public enum HitState{
         JUMP,
         STAY,
         CROUCH
     }
 
     public enum HitType{
         HEAVY,
         NORMAL,
         EASY
     }
 
     public enum HitDirection{
         TOP,
         RIGHT,
         BOTTOM,
         LEFT
     }
 
     private HitState state;
     private HitType type;
     private HitDirection direction;
 
     public boolean isSame(Hit hit){
         if (this.getState() != hit.getState()){
             return false;
         }
         if (this.getType() != hit.getType()){
             return false;
         }
         if (this.getDirection() != hit.getDirection()){
             return false;
         }
         return true;
     }
 
    @Override
    public boolean equals(java.lang.Object o) {
        if (super.equals(o)){
            return true;
        }
        Hit hit = (Hit) o;
        if (this.getState() != hit.getState()){
            return false;
        }
        if (this.getType() != hit.getType()){
            return false;
        }
        if (this.getDirection() != hit.getDirection()){
            return false;
        }
        return true;
    }

     public HitState getState() {
         return state;
     }
 
     public HitType getType() {
         return type;
     }
 
     public HitDirection getDirection() {
         return direction;
     }
 }
