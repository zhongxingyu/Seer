 package de.metalab.namnam.model;
 
 /**
  * diese klasse repraesentiert ein mensaessen, indem sie das normale Essen um einen weiteren Preis fuer
  * Studenten erweitert.
  *
  * @see de.metalab.namnam.model.Essen
  * @author fake
  * @author testicle
  * @author nati
  */
 public class Mensaessen extends Essen {
 
     private Preis studentenPreis;
 
     public Mensaessen() {
         super();
     }
 
     /**
      * initialisert ein mensaessen-objekt mit beschreibung und beiden preisen in cent
      * bis zu 2stellige zahlen am ende der beschreibung werden abgeschnitten,
      * ein V markiert das essen als vegetarisch, ein X als ohne schweinefleisch
      * und ein R als mit Rind.
      *
     * @param desc die beschreibung des essens, aus der die vegetarisch/k. schweinefl./rind daten extrahiert werden
      * @param bPreis der normale, nicht verguenstigte preis des essens in cent
      * @param sPreis der beguenstigte studentenpreis des essens in cent
      */
 
     public Mensaessen(String desc, Integer bPreis, Integer sPreis) {
         super(desc,bPreis);
         this.studentenPreis = new Preis(sPreis);
     }
 
     /**
      * gibt das mit diesem mensaessen assoziierte "Preis"-objekt zurueck, das ist der reduzierte
      * Preis, den ueblicherweise nur Stundenten zahlen koennen.
      * @return den reduzierten studentenpreis des mensaessens
      */
     public Preis getStudentenPreis() {
         return studentenPreis;
     }
     /**
      * legt den reduzierten studentenpreis fuer dieses mensaessen fest.
     * @param studentenPreis der zu setzende, reduzierte studentenpreis
      */
     public void setStudentenPreis(Preis studentenPreis) {
         this.studentenPreis = studentenPreis;
     }
 }
