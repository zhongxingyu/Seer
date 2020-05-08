 package com.enpasos.navi;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class AmeisenGott {
 
     static Set<Ameise> ameisenBox;
 
     public static Weg getKuerzesterWeg(Stadt startStadt, Stadt endStadt) {
         Weg weg = null;
         ameisenBox = new HashSet<Ameise>();
         starteAmeisen(null, startStadt);
        
        
        
        Set<Ameise> aktuelleAmeisen = new HashSet<Ameise>();
        aktuelleAmeisen.addAll(ameisenBox);
        
        for (Ameise a : aktuelleAmeisen) {
             Stadt stadt = a.getAktuelleStadt();
             starteAmeisen(a, stadt);
         }
         //...
 
 
         return weg;
     }
  
     public static void starteAmeisen(Ameise prototypAmeise, Stadt stadt) {
        if (prototypAmeise == null)
                 prototypAmeise = new Ameise();
  
         for (Strasse s : stadt.strassen) {
             Ameise a = prototypAmeise.clone();
             ameisenBox.add(a);
             Stadt zwischenZielStadt = s.getZielStadtWennStartVon(stadt);
             boolean ameiseHatteDenKuerzestenWeg = a.geh(s, zwischenZielStadt);
             if (!ameiseHatteDenKuerzestenWeg) {
                 ameisenBox.remove(a);
             }
         }
         ameisenBox.remove(prototypAmeise);
     }
  
 }
