 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ewa2.util;
 
 import model.Feld;
 import model.PlayerColor;
 import model.Spielfeld;
 
 /**
  *
  * @author bernhard
  */
 public class SpielUtil {
     public static int wuerfle() {
         double zufall;
         zufall = Math.random();
         zufall = zufall * 6 + 0.5;
         return (int) Math.round(zufall);
     }
     
     public String PlayerColorToNumber(PlayerColor col) {
         if(col.equals(PlayerColor.Yellow))
             return "1";
         if(col.equals(PlayerColor.Green))
             return "2";
         if(col.equals(PlayerColor.Red))
             return "3";
         else
             return "4";
     }
     
     public String getPlayareaImage(Feld field, Spielfeld spielfeld) {
         String ret = "img/field";
         if(field.isFirstField()) {
             ret += PlayerColorToNumber(field.getOwner());
         }
         if(field.getContent() != null) {
             ret += "_player";
             ret += PlayerColorToNumber(spielfeld.getPlayerColor(field.getContent()));
         }
         ret += ".png";
         return ret;
     }
     
     public String getPlayareaText(int i, Feld field, Spielfeld spielfeld) {
         String ret = "Feld " + i;
         if(field.isFirstField()) {
             ret += ": erstes Feld Spieler ";
             ret += PlayerColorToNumber(field.getOwner());
         }
         else if(field.isLastField()) {
             ret += ": letztes Feld Spieler ";
             ret += PlayerColorToNumber(field.getOwner());
         }
             
         if(field.getContent() != null) {
            ret += ": Spieler";
             ret += PlayerColorToNumber(spielfeld.getPlayerColor(field.getContent()));
         }
         return ret;
     }
     
     public String getStartareaImage(Feld field, Spielfeld spielfeld) {
         String ret = "img/field";
         ret += PlayerColorToNumber(field.getOwner());
         if(field.getContent() != null) {
             ret += "_player";
             ret += PlayerColorToNumber(spielfeld.getPlayerColor(field.getContent()));
         }
         ret += ".png";
         return ret;
     }
     
     public String getStartareaText(int i, Feld field, Spielfeld spielfeld) {
         String ret = "Feld " + i + ": Startfeld Spieler ";
         ret += PlayerColorToNumber(field.getOwner());
 
             
         if(field.getContent() != null) {
            ret += ": Spieler";
             ret += PlayerColorToNumber(spielfeld.getPlayerColor(field.getContent()));
         }
         
         return ret;
     }
     
     public String getFinishareaImage(Feld field, Spielfeld spielfeld) {
         String ret = "img/field";
         ret += PlayerColorToNumber(field.getOwner());
         if(field.getContent() != null) {
             ret += "_player";
             ret += PlayerColorToNumber(spielfeld.getPlayerColor(field.getContent()));
         }
         ret += ".png";
         return ret;
     }
     
     public String getFinishareaText(int i, Feld field, Spielfeld spielfeld) {
         String ret = "Feld " + i + ": Zielfeld Spieler ";
         ret += PlayerColorToNumber(field.getOwner());
 
             
         if(field.getContent() != null) {
            ret += ": Spieler";
             ret += PlayerColorToNumber(spielfeld.getPlayerColor(field.getContent()));
         }
         
         return ret;
     }
 }
