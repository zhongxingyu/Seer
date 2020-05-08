 package mjc.gc;
 
 public class VAR {
     // le deplacement
     private int dep;
 
     // le registre de base de la variable
     private String reg;
 
     private DTYPE type;
 
     private boolean attr;
     
 
     public int getDep() {
         return dep;
     }
 
     public String getReg() {
         return reg;
     }
 
     public DTYPE getType() {
         return type;
     }
 
     public VAR(DTYPE t, int d, String r,boolean isAttr) {
         type = t;
         dep = d;
         reg = r;
         attr = isAttr;
     }
 
     public boolean isAttr() {
         return attr;
     }
 
     // affichage
     public String toString() {
         return "; VAR : " + "type=" + type + " dep = " + dep + " reg = " + reg;
     }
 
 }
