 package Code;
 
 import java.util.ArrayList;
 
 public class Log {
 
     ArrayList logg = new ArrayList();
 
     public void leggTilLogg(String tekst) {
         logg.add(tekst);
     }
 
     public void clearLogg() {
         logg.removeAll(logg);
     }
 
     @Override
     public String toString() {
         String res = "";
         for (int i = 0; i < logg.size(); i++) {
             if (i == logg.size() - 1) {
                res += logg.get(i);
             } else {
                 res += logg.get(i) + "\n";
             }
         }
         return res;
     }
 }
