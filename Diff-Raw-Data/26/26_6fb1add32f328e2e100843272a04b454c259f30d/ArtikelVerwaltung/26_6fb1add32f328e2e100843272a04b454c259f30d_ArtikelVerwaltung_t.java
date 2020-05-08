 package eshop.local.domain;
 
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 import com.sun.xml.internal.bind.v2.TODO;
 import eshop.local.exception.*;
 import eshop.local.persistence.Log;
 import eshop.local.valueobjects.*;
 import eshop.local.persistence.FilePersistenceManager;
 import eshop.local.persistence.PersistenceManager;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Giacomo
  * Date: 10.04.13
  * Time: 14:35
  * To change this template use File | Settings | File Templates.
  */
 
 public class ArtikelVerwaltung {
 
     // Dokument zum Speichern des Logs
     private File dateiName = new File("Eshop_ArtikelLog.txt");
     private File dateiFuerGraph = new File("Eshop_BestandsGraph.txt");
 
     // Hashmap zum speichern des Artikelbestands als Key dienen die Artikelnummern
     private HashMap<Integer, Artikel> artikelBestandNr;
 
     // Hashmap zum verknüpfen des Artikelnamens mit der Artikelnummer
     private HashMap<String, Integer> artikelBestandName;
 
     // Persistenz-Schnittstelle, die für die Details des Dateizugriffs verantwortlich ist
     private PersistenceManager pm = new FilePersistenceManager();
 
     // Anderes Datums-Format
     private final SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'um' HH:mm:ss zzz':'");
 
     private Log l = new Log();
 
 
 // Konstruktor
 
     public ArtikelVerwaltung() {
 
         // verknüpft die Artikelnummern mit den Artikel Objekten
         artikelBestandNr = new HashMap<Integer, Artikel>();
 
         // verknüpft die Artikelnamen mit den Artikelnummern
         artikelBestandName = new HashMap<String, Integer>();
     }
 
 
 // Methoden
 
     /**
      * Methode zum Einlesen der Artikeldaten aus einer Datei.
      *
      * @param datei Datei, die den einzulesenden Artikelbestand enthaelt
      * @throws IOException,ClassNotFoundException
      *
      */
     public void liesDaten(String datei) throws IOException, ClassNotFoundException {
 
         // Erstellung eines Artikel Objekts
         Artikel einArtikel;
 
         try {
 
             // PersistenzManager für Lesevorgänge wird geöffnet
             pm.openForReading(datei);
 
             do {
                 // Artikel-Objekt einlesen
                 einArtikel = pm.ladeArtikel();
                 if (einArtikel != null) {
                     // Artikel einfügen
                     artikelHinzufuegen(einArtikel);
                 }
 
             } while (einArtikel != null);
 
 
             // PersistenzManager für Lesevorgänge wird wieder geschlossen
             pm.close();
         } catch (IOException e) {
            e.printStackTrace();
         } finally {
             pm.close();
         }
     }
 
     /**
      * Methode zum Schreiben der Artikeldaten in eine Datei.
      *
      * @param datei Datei, in die der Artikelbestand geschrieben werden soll
      * @throws IOException
      */
     public void schreibeDaten(String datei) throws IOException {
 
         // PersistenzManager für Schreibvorgänge öffnen
         pm.openForWriting(datei);
 
         // Artikel-Objekte aus der Hashmap artikelBestandNr einlesen und in die Datei schreiben
         if (!artikelBestandNr.isEmpty()) {
             Iterator<Artikel> iter = artikelBestandNr.values().iterator();
             while (iter.hasNext()) {
                 Artikel a = iter.next();
                 if (a instanceof MassengutArtikel) {
                     MassengutArtikel m = (MassengutArtikel) a;
                     pm.speichereMassengutArtikel(m);
                 } else {
                     pm.speichereArtikel(a);
                 }
             }
         }
 
 
         //Persistenz-Schnittstelle wieder schließen
         pm.close();
     }
 
     /**
      * Methode zum hinzufuegen von Artikeln
      *
      * @param name
      * @param beschreibung
      * @param preis
      */
     public void artikelHinzufuegen(String name, String beschreibung, double preis, Mitarbeiter m) throws IOException, ArtikelExestierBereitsException {
 
         // Wenn der Name eines Artikels bereits vorhanden ist wird false zurück gegeben und kein neuer Artikel angelegt
         if (artikelBestandName.containsKey(name)) {
             throw new ArtikelExestierBereitsException(name);
 
 
         } else {
             // Ist der Artikelname noch nicht vorhanden wird er neu angelegt und in den beiden HasMaps gespeichert (artikelBestandNr, artikelBestandName)
 
 
             Artikel artikel = new Artikel(name, beschreibung, preis);
             artikelBestandNr.put(artikel.getNummer(), artikel);
             artikelBestandName.put(name, artikel.getNummer());
             Date dNow = new Date();
             String text = ft.format(dNow) + "\nDer Artikel '" + name + "' mit der Artikelnummer " + artikel.getNummer() + " wurde vom Mitarbeiter " + m.getBenutzername() + " mit der Mitarbeiternummer " + m.getmNr() + " hinzugefügt.";
             l.writeLog(dateiName, text);
             String graphData = ft.format(dNow) + "%'" + name + "'%" + artikel.getNummer() + "%" + 0 + "%";
             l.writeGraphData(dateiFuerGraph, graphData);
 
         }
 
     }
 
     /**
      * Die Methode fügt einen Massengutartikel hinzu
      *
      * @param name
      * @param beschreibung
      * @param preis
      * @param packung
      * @param m
      * @throws ArtikelExestierBereitsException
      *
      * @throws IOException
      */
     public void massengutartikelHinzufuegen(String name, String beschreibung, double preis, int packung, Mitarbeiter m) throws ArtikelExestierBereitsException, IOException {
         if (artikelBestandName.containsKey(name)) {
             throw new ArtikelExestierBereitsException(name);
 
         } else {
             // Ist der Artikelname noch nicht vorhanden wird er neu angelegt und in den beiden HasMaps gespeichert (artikelBestandNr, artikelBestandName)
 
             MassengutArtikel massengutArtikel = new MassengutArtikel(name, beschreibung, preis, packung);
             artikelBestandNr.put(massengutArtikel.getNummer(), massengutArtikel);
             artikelBestandName.put(name, massengutArtikel.getNummer());
             Date dNow = new Date();
             String text = ft.format(dNow) + "\nDer Masengutartikel '" + name + "' mit der Artikelnummer " + massengutArtikel.getNummer() + " und der Packungsgröße " + massengutArtikel.getPackungsgroesse() + " wurde vom Mitarbeiter " + m.getBenutzername() + " mit der Mitarbeiternummer " + m.getmNr() + " hinzugefügt.";
             l.writeLog(dateiName, text);
             String graphData = ft.format(dNow) + "%'" + name + "'%" + massengutArtikel.getNummer() + "%" + 0 + "%";
             l.writeGraphData(dateiFuerGraph, graphData);
 
         }
     }
 
     /**
      * Methode zum hinzufuegen von Artikeln durch den PersistenceManager
      *
      * @param a
      */
     public void artikelHinzufuegen(Artikel a) {
         artikelBestandNr.put(a.getNummer(), a);
         artikelBestandName.put(a.getName(), a.getNummer());
         if (a.getZaehler() <= a.getNummer()) {
             a.setZaehler(a.getNummer() + 1);
         }
     }
 
     /**
      * Methode zum loeschen von Artikel
      *
      * @param artNr
      * @return boolean
      */
     public void artikelLoeschen(int artNr, Mitarbeiter m) throws IOException, ArtikelExestiertNichtException {
 
         if (!artikelBestandNr.containsKey(artNr)) {
 
             throw new ArtikelExestiertNichtException(artNr);
 
 
         } else {
             Artikel a = artikelBestandNr.get(artNr);
             artikelBestandNr.remove(artNr);
             artikelBestandName.remove(a.getName());
 
             Date dNow = new Date();
             String text = ft.format(dNow) + "\nDer Artikel '" + a.getName() + "' mit der Artikelnummer " + artNr + " wurde vom Mitarbeiter " + m.getBenutzername() + " mit der Mitarbeiternummer " + m.getmNr() + " gelöscht.";
             l.writeLog(dateiName, text);
             String graphData = ft.format(dNow) + "%'" + a.getName() + "'%" + artNr + "%" + 0 + "%";
             l.writeGraphData(dateiFuerGraph, graphData);
 
 
         }
 
     }
 
     /**
      * Methode gibt einen Vector zurueck der alle vorhandenen Artikeln enthaelt
      * @return
      */
     public Vector alleArtikelZurueckgeben() {
 
         Vector<Artikel> ergebnis = new Vector<Artikel>();
         //TODO Systemout noch entfernen @NINA (wenn du sie nicht mehr bruachst)
         for (Artikel elem : artikelBestandNr.values())
             if (elem instanceof MassengutArtikel) {
                 System.out.println("MG");
                 ergebnis.add(elem);
             } else {
                 System.out.println("A");
                 ergebnis.add(elem);
             }
 
         return ergebnis;
 
     }
 
     /**
      * Methode gibt die HashMap zurueck die alle vorhandenen Artikeln enthaelt
      * @return
      */
     public HashMap<Integer, Artikel> alleArtikelHashMapZurueckgeben() {
         return artikelBestandNr;
     }
 
     /**
      * Relikt aus der CUI
      * Methode durchsucht alle Artikel nach dem Parameter name und gibt den entsprechenden Artikel in einem Vektor zurueck
      *
      * @param name
      */
     public Vector sucheArtikel(String name) {
         int nummer = 0;
         Vector<Artikel> ergebnis = new Vector<Artikel>();
 
         if (artikelBestandName.containsKey(name)) {
             nummer = artikelBestandName.get(name);
             ergebnis.add(artikelBestandNr.get(nummer));
 
             return ergebnis;
 
         } else {
             Vector<String> error = new Vector<String>();
             error.add("Der Artikel ist nicht vorhanden !");
 
             return error;
         }
 
     }
 
     /**
      * Methode aendert den Bestand des gewuenschten Artikels wenn er exestiert
      *
      * @param artNr,wert
      */
     public void setBestand(int artNr, int menge, Person person) throws IOException, ArtikelBestandNegativException, ArtikelExestiertNichtException {
 
         // Wenn die Artikelnummer exestiert und der neue Bestand nicht ins negative geht wird der Bestand angepasst
         if (artikelBestandNr.containsKey(artNr) && menge >= 0) {
 
             Artikel a = artikelBestandNr.get(artNr);
             a.setBestand(menge);
             artikelBestandNr.put(artNr, a);
             Date dNow = new Date();
             if (person instanceof Kunde) {
                 String text = ft.format(dNow) + "\nDer Bestand des Artikels '" + a.getName() + "' mit der Artikelnummer " + artNr + " wurde durch den Kunden " + person.getBenutzername() + " mit der Kundennummer " + ((Kunde) person).getNummer() + " geändert und hat jetzt die Menge " + menge + ".";
                 l.writeLog(dateiName, text);
                 String graphData = ft.format(dNow) + "%'" + a.getName() + "'%" + artNr + "%" + menge + "%";
                 l.writeGraphData(dateiFuerGraph, graphData);
 
             } else if (person instanceof Mitarbeiter) {
                 String text = ft.format(dNow) + "\nDer Bestand des Artikels '" + a.getName() + "' mit der Artikelnummer " + artNr + " wurde durch den Mitarbeiter " + person.getBenutzername() + " mit der Mitarbeiternummer " + ((Mitarbeiter) person).getmNr() + " geändert und hat jetzt die Menge " + menge + ".";
                 l.writeLog(dateiName, text);
                 String graphData = ft.format(dNow) + "%'" + a.getName() + "'%" + artNr + "%" + menge + "%";
                 l.writeGraphData(dateiFuerGraph, graphData);
 
             }
 
 
           // wirft eine Exception wenn die Artikelnummer des Artikels nicht exestiert
         } else if (!artikelBestandNr.containsKey(artNr)) {
             throw new ArtikelExestiertNichtException(artNr);
 
         }
 
         // wirft eine ArtikelBestandNeagativException wenn versucht wird denn Bestand in negative zu aendern
         else if (menge < 0) {
             throw new ArtikelBestandNegativException();
         }
     }
 
     /**
      * Methode setzt den Wert wieviel Artikel bestellt werden sollen
      *
      * @param menge Integer
      */
     public void setBestellteMenge(int menge, Artikel artikel) throws ArtikelBestellteMengeNegativException, ArtikelBestandZuNiedrigException, BestellteMengeEntsprichtNichtderPackungsgroesseException {
         if ((menge >= 0) && (artikel.getBestand() >= menge)) {
             if (artikel instanceof  MassengutArtikel ){
                if ((menge % ((MassengutArtikel) artikel).getPackungsgroesse() == 0)) {
                     artikel.setBestellteMenge(menge);
                } else {
                     throw new BestellteMengeEntsprichtNichtderPackungsgroesseException((MassengutArtikel)artikel, menge);
                }
             } else {
                 artikel.setBestellteMenge(menge);
             }
         } else if (!(menge >= 0)) {
             throw new ArtikelBestellteMengeNegativException();
 
         } else if (!(artikelBestandNr.get(artikel.getNummer()).getBestand() >= menge)) {
             throw new ArtikelBestandZuNiedrigException(artikel);
         }
     }
 
     /**
      * Gibt einen Artikel anhand der Artikenummer zurueck
      * @param aNr
      * @return
      * @throws ArtikelExestiertNichtException
      */
     public Artikel getArtikel(int aNr) throws ArtikelExestiertNichtException {
         if (artikelBestandNr.containsKey(aNr)) {
             return artikelBestandNr.get(aNr);
         } else if (!artikelBestandNr.containsKey(aNr)) {
 
             throw new ArtikelExestiertNichtException(aNr);
 
         } else {
             return null;
         }
 
     }
 
     /**
      * Gibt true zurück wenn der Artikel exestiert, false wenn er nicht exestiert
      * @param aNr
      * @return
      */
     public boolean existiertArtikel(int aNr) {
         if (artikelBestandNr.containsKey(aNr)) {
             return true;
         } else {
             return false;
         }
     }
 
     // TODO @Noshaba Kommentieren Bitte !!!
     public Vector<String> printArtikelLog(int daysInPast, String aNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException {
         return l.printLog("Eshop_ArtikelLog.txt", daysInPast, aNr);
     }
 
     public Vector<ArtikelBestandsGraph> getArtikelGraph(int daysInPast, String aNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException {
         return l.getArtikelGraph("Eshop_BestandsGraph.txt", daysInPast, aNr);
     }
 
     public Vector<String> printArtikelLog(int daysInPast) throws FileNotFoundException, KeineEintraegeVorhandenException, ParseException {
         return l.printLog("Eshop_ArtikelLog.txt", daysInPast);
     }
 
     public Vector<String> printArtikelLog(String aNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException {
         return l.printLog("Eshop_ArtikelLog.txt", aNr);
     }
 
     public String printArtikelLog() throws FileNotFoundException, KeineEintraegeVorhandenException {
         return l.printLog("Eshop_ArtikelLog.txt");
     }
 
 }
 
