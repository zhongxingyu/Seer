 package com.soebes.regeln.annahme;
 
 import java.util.Date;
 
 // AA, 02.2010-09.2099, 1-2, 1-3, [XRegeln|gelöscht|(leer)]
 //  !     !        !     !    !       +------------------------- Regel
 //  !     !        !     !    +--------------------------------- Detail Version (von-bis)
 //  !     !        !     +-------------------------------------- Version (von-bis)
//  !     !        +-------------------------------------------- Annahme VZ Zeitraum bis (Zeitpunkt hier Monat.Jahr)
//  !     +----------------------------------------------------- Annahme VZ Zeitraum von (Zeitpunkt hier Monat.Jahr)
 //  +----------------------------------------------------------- Art
 //
 public class AnnahmeRegel {
 
     /**
      * Für welche Art ist die Regel gültig.
      */
     private Arten art;
 
     /**
     * AnnahmeZeitraum (Bisher: 01.01.2005 12:00:00-01.01.2010 13:30:25)
      */
    private Zeitraum annahmeZeitraum;
 
     /**
      * Der Versionsbereich für den die Regel gilt.
      */
     private VersionsBereich versionVonBis;
 
     /**
      * Der detail Versionsbereich für den die Regel gilt.
      */
     private VersionsBereich detailVersionVonBis;
 
     /**
      * Die Regel ist entweder leer, sprich keine zusätzliche Bedingung oder
      * "VZ=Eing+1".
      */
     private Regel regel;
 
 
     public AnnahmeRegel(Arten art, Zeitraum veranlagungsZeitraum, VersionsBereich versionVonBis, VersionsBereich detailVersionVonBis, Regel regel) {
         super();
         this.art = art;
        this.annahmeZeitraum = veranlagungsZeitraum;
         this.versionVonBis = versionVonBis;
         this.detailVersionVonBis = detailVersionVonBis;
         this.regel = regel;
     }
 
     public boolean annehmen(Arten art, int version, int detailVersion, Date veranlagungsZeit, Date eingangsZeit) {
 
         if (!art.equals(getArt())) {
             return false;
         }
 
         if (!getVersionVonBis().contains(version)) {
             return false;
         }
 
         if (!getDetailVersionVonBis().contains(detailVersion)) {
             return false;
         }
 
        if (!getAnnahmeZeitraum().contains(veranlagungsZeit)) {
             return false;
         }
 
         if (!getRegel().annahme(veranlagungsZeit, eingangsZeit)) {
             return false;
         }
 
         return true;
     }
 
     public Arten getArt() {
         return art;
     }
 
     public void setArt(Arten art) {
         this.art = art;
     }
 
    public Zeitraum getAnnahmeZeitraum() {
        return annahmeZeitraum;
     }
 
    public void setAnnahmeZeitraum(Zeitraum annahmeZeitraum) {
        this.annahmeZeitraum = annahmeZeitraum;
     }
 
     public VersionsBereich getVersionVonBis() {
         return versionVonBis;
     }
 
     public void setVersionVonBis(VersionsBereich versionVonBis) {
         this.versionVonBis = versionVonBis;
     }
 
     public VersionsBereich getDetailVersionVonBis() {
         return detailVersionVonBis;
     }
 
     public void setDetailVersionVonBis(VersionsBereich detailVersionVonBis) {
         this.detailVersionVonBis = detailVersionVonBis;
     }
 
     public Regel getRegel() {
         return regel;
     }
 
     public void setRegel(Regel regel) {
         this.regel = regel;
     }
 
 }
