 package mjc.gc;
 
 public class POINTEUR extends DTYPE {
     private DTYPE type;
 
     public POINTEUR(DTYPE t) {
         super("pointeur", 1);
         type = t;
     }
 
    public DTYPE getPointedType() {
         return type;
     }
 
     public boolean equals(DTYPE autre) {
         if (autre instanceof POINTEUR)
            return type.equals(((POINTEUR) autre).getPointedType());
         return false;
     }
 
    public boolean canAccept(DTYPE autre) {
         if (autre instanceof POINTEUR) {
            DTYPE apt_type = ((POINTEUR)autre).getPointedType();
             if(type.equals(apt_type)) {
                 return true;
             } else {
                 if( (type instanceof CLASSE) & (apt_type instanceof CLASSE) ) {
                     return ((CLASSE)type).isASuperClass((CLASSE)apt_type);
                 } else {
                     return false;
                 }
             }
         } else {
             return false;
         }
     }
 
     public String toString(){
         return super.toString() + " sur type = " + type;
     }
 
 }
