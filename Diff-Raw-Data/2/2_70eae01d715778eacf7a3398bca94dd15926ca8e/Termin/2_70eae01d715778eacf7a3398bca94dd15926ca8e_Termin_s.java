 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com;
 
 import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
 
 /**
  *
  * @author Matthias
  */
 public abstract class Termin {
 
     private String ort;
     private GregorianCalendar von;
     private GregorianCalendar bis;
 
     public Termin(String _ort, GregorianCalendar _von, GregorianCalendar _bis) {
         ort = _ort;
         von = _von;
         bis = _bis;
     }
 
     public void setDate(String _ort, GregorianCalendar _von, GregorianCalendar _bis) {
         ort = _ort;
         von = _von;
         bis = _bis;
     }
 
     public GregorianCalendar getVon() {
         return von;
     }
 
     public GregorianCalendar getBis() {
         return bis;
     }
 
     @Override
     public String toString() {
         SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        return ort + " " + sdf.format(von) + " - " + sdf.format(bis);
     }
 }
