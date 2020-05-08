 package com.enpasos.navi;
 
import java.util.HashSet;
import java.util.Set;

 
 public class Ameise {
  
    
    Weg weg;
    int weglaenge;
    
    
    public Ameise() { 
        weg = new Weg();
    }
 
    private void geh(Strasse s) {
        weg.add(s);
     }
 }
