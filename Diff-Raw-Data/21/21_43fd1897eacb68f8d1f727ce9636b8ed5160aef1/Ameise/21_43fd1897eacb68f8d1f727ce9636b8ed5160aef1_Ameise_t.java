 package com.enpasos.navi;
 
 
 public class Ameise {
  
    
    Weg weg;
    int weglaenge;
    
    
    public Ameise() { 
        weg = new Weg();
    }
 
    public void geh(Strasse s) {
        weg.add(s);
     }
 }
