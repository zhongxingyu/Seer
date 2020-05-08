 package mjc.gc;
 
 import java.util.ArrayList;
 
 public class ARGLIST extends ArrayList<DTYPE> {
 
     public String toString() {
         StringBuffer sb = new StringBuffer();
         for (DTYPE c : this) {
             sb.append(c + ", ");
         }
         return sb.toString();
     }
     
     public int getTaille() {
         int taille=0;
         for(DTYPE type : this) {
             taille+=type.getTaille();
         }
         return taille;
     }
 
    public boolean equals(ARGLIST autre) {
        int lc = size();
        int lca = autre.size();
        if (lc != lca)
            return false;
        for (int i = 0; i < lc; i++) {
            DTYPE c = get(i);
            DTYPE ca = autre.get(i);

            if (!c.equals(ca))
                return false;
        }
        return true;
    }

 }
