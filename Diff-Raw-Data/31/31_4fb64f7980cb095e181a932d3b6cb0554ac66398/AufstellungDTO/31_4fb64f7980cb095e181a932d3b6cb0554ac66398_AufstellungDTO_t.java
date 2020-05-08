 package de.philinko.bundesliga.dto;
 
 import de.philinko.bundesliga.manager.mappings.Aufstellung;
 import de.philinko.bundesliga.manager.mappings.Position;
 import de.philinko.bundesliga.manager.mappings.Spieler;
 
 /**
  *
  * @author philippe
  */
 public class AufstellungDTO {
     private int spieltag;
     private String name;
     private Position position;
     private String verein;
     private boolean eingesetzt;
    private int gesamtPunkte;
 
     public AufstellungDTO(int spieltag, String name, Position position, String verein, boolean eingesetzt) {
         this.spieltag = spieltag;
         this.name = name;
         this.position = position;
         this.verein = verein;
         this.eingesetzt = eingesetzt;
     }
 
     public AufstellungDTO(Aufstellung source) {
         this.spieltag = source.getSpieltag();
         this.name = source.getSpieler().getName();
         this.position = source.getSpieler().getPosition();
         this.eingesetzt = true;
        this.gesamtPunkte = 0;
     }
 
     public Spieler getSpieler() {
         Spieler result = new Spieler();
         result.setName(this.name);
         result.setPosition(this.position);
         return result;
     }
 
     public int getSpieltag() {
         return spieltag;
     }
 
     public void setSpieltag(int spieltag) {
         this.spieltag = spieltag;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Position getPosition() {
         return position;
     }
 
     public void setPosition(Position position) {
         this.position = position;
     }
 
     public String getVerein() {
         return verein;
     }
 
     public void setVerein(String verein) {
         this.verein = verein;
     }
 
     public boolean isEingesetzt() {
         return eingesetzt;
     }
 
     public void setEingesetzt(boolean eingesetzt) {
         System.err.println("Called setEingesetzt()");
         this.eingesetzt = eingesetzt;
     }

    public int getGesamtPunkte() {
        return gesamtPunkte;
    }

    public void setGesamtPunkte(int punkte) {
        this.gesamtPunkte = punkte;
    }
 }
