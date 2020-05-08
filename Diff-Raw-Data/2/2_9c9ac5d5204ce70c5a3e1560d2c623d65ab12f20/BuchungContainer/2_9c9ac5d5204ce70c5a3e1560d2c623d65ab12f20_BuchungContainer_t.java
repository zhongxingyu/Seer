 package Buchung;
 
 import java.util.ArrayList;
 
 public class BuchungContainer {
     private ArrayList<AbstractBuchung> buchungen = new ArrayList<AbstractBuchung>();
 
     public void addBuchung(AbstractBuchung buchung) {
         this.buchungen.add(buchung);
     }
 
     public void removeBuchung(AbstractBuchung buchung) {
         this.buchungen.remove(buchung);
     }
 
     public int summe() {
         int summe = 0;
         for (AbstractBuchung b: buchungen) {
            summe += b.getWert();
         }
         return summe;
     }
 }
