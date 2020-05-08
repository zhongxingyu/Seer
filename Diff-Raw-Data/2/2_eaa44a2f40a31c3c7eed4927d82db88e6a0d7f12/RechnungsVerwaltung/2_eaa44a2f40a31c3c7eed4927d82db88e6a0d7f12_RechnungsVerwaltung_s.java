 package eshop.local.domain;
 
 import eshop.local.exception.*;
 import eshop.local.persistence.FilePersistenceManager;
 import eshop.local.persistence.Log;
 import eshop.local.persistence.PersistenceManager;
 import eshop.local.valueobjects.Artikel;
 import eshop.local.valueobjects.Kunde;
 import eshop.local.valueobjects.Rechnung;
 
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Noshaba
  * Date: 22.04.13
  * Time: 21:52
  * To change this template use File | Settings | File Templates.
  */
 public class RechnungsVerwaltung {
 
     // Dokument zum Speichern des Logs
     private File dateiName = new File("Eshop_RechnungsLog.txt");
 
     // Hashmap zum speichern des Rechnungsbestandes als Key dienen die Rechnungsnummern
     private HashMap<Integer, Rechnung> rechnungsBestandNr;
 
     // Hashmap zum verknüpfen der Kundennummer mit den Rechnungsnummern
     private HashMap<Integer, Vector<Integer>> rechnungsBestandKundenNr;
 
     // Persistenz-Schnittstelle, die für die Details des Dateizugriffs verantwortlich ist
     private PersistenceManager pm = new FilePersistenceManager();
 
     // Anderes Datums-Format
     private final SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'um' HH:mm:ss zzz':'");
 
     private Log l = new Log();
 
     // Konstruktor
     public RechnungsVerwaltung() {
 
         // verknüpft die Rechungsnummern mit den Rechnungsobjekten
         rechnungsBestandNr = new HashMap<Integer, Rechnung>();
 
         // verknüpft die Kundennummer mit einem Vector von Rechnungsnummern
         rechnungsBestandKundenNr = new HashMap<Integer, Vector<Integer>>();
     }
 
 
     /**
      * Methode zum Einlesen der Rechnungen aus einer Datei.
      *
      * @param datei
      * @throws IOException
      * @throws ClassNotFoundException
      */
     public void liesDaten(String datei) throws IOException, ClassNotFoundException {
 
         // Erstellung eines Rechnungs Objekts
         Rechnung rechnung;
 
         try {
             // PersistenzManager für Lesevorgänge wird geöffnet
             pm.openForReading(datei);
             do {
                 // Rechnungs-Objekt einlesen
                 rechnung = pm.ladeRechnung();
                 if (rechnung != null) {
                     // Rechnung einfügen
                     rechnungHinzufuegen(rechnung);
                 }
             } while (rechnung != null);
             // PersistenzManager für Lesevorgänge wird wieder geschlossen
         } catch (IOException e) {
             System.out.println("Fehler beim einlesen der Rechnungsdaten !");
             e.printStackTrace();
         } finally {
             pm.close();
         }
     }
 
     /**
      * Methode zum Schreiben der Kundendaten in eine Datei.
      *
      * @param datei
      * @throws IOException
      */
     public void schreibeDaten(String datei) throws IOException {
 
         // PersistenzManager für Schreibvorgänge öffnen
         pm.openForWriting(datei);
 
         // Rechnungs-Objekte aus der Hashmap kundenBestandNr einlesen und in die Datei schreiben
         if (!rechnungsBestandNr.isEmpty()) {
             Iterator iter = rechnungsBestandNr.values().iterator();
             while (iter.hasNext()) {
                 Rechnung rechnung = (Rechnung) iter.next();
                 pm.speichereRechnung(rechnung);
             }
         }
 
         //Persistenz-Schnittstelle wieder schließen
         pm.close();
     }
 
     /**
      * Methode ermöglich das sichere Bestellen eines Warenkorbes
      * @param eShopVerwaltung
      * @param aktuellerKunde
      * @throws IOException
      * @throws KundenNummerExistiertNichtException
      * @throws ArtikelBestandNegativException
      * @throws ArtikelBestandZuNiedrigException
      * @throws ArtikelExestiertNichtException
      * @throws RechnungExestiertNichtException
      */
     public synchronized void rechnungsBestandCheckKaufen(EShopVerwaltung eShopVerwaltung, int aktuellerKunde) throws IOException, KundenNummerExistiertNichtException, ArtikelBestandNegativException, ArtikelBestandZuNiedrigException, ArtikelExestiertNichtException, RechnungExestiertNichtException {
 
 
         if (eShopVerwaltung.getKunde(aktuellerKunde).getWarenkorb().size() > 0) {
 
             //wenn sich Artikel im Warenkorb befinden prüfen ob die gewünschten Artikel gekauft werden können
             Iterator iter = eShopVerwaltung.getKunde(aktuellerKunde).getWarenkorb().values().iterator();
             boolean bestandsCheck = true;
             Vector<Integer>  artikelNr = new Vector<Integer>();
             Vector<Integer>  neuerBestand = new Vector<Integer>();
             while (iter.hasNext()) {
                 Artikel bestandCheckArtikel = (Artikel) iter.next();
 
 
                 if ((eShopVerwaltung.getArtikel(bestandCheckArtikel.getNummer()).getBestand() < bestandCheckArtikel.getBestellteMenge())) {
 
                     bestandsCheck = false;
                     throw new ArtikelBestandZuNiedrigException(bestandCheckArtikel);
                 } else {
                     Artikel a = (Artikel) iter.next();
                     Artikel puffer = eShopVerwaltung.getArtikel(a.getNummer());
                     neuerBestand.add(puffer.getBestand() - a.getBestellteMenge());
                     artikelNr.add(puffer.getNummer());
                 }
 
 
             }
 
             // wenn der bestandCheck nicht auf false gesetzt wurde werden die Bestände angepasst ,die Rechnung erstellt und der Warenkorb geleert
             if (bestandsCheck) {
 
                 for (int i = 0; i < artikelNr.size(); i++) {
                 eShopVerwaltung.setBestand(artikelNr.get(i), neuerBestand.get(i), eShopVerwaltung.getKunde(aktuellerKunde));
                 }
                 eShopVerwaltung.fuegeRechnungEin(eShopVerwaltung.getKunde(aktuellerKunde));
                 eShopVerwaltung.getKunde(aktuellerKunde).resetWarenkorb();
 
 
             }
         }
     }
 
 
     /**
      * Methode zum Hinzufuegen von Rechnungen
      *
      * @param kunde
      * @throws IOException
      * @throws RechnungExestiertNichtException
      *
      */
     public void rechnungHinzufuegen(Kunde kunde) throws IOException, RechnungExestiertNichtException {
 
         // sollte irgendwann schon einmal eine Rechnung für den Kundne erstellt worden sein
         if (rechnungsBestandKundenNr.containsKey(kunde.getNummer())) {
             Vector<Artikel> wkV = new Vector<Artikel>();
             double gesamtPreis = 0;
             for (Artikel elem : kunde.getWarenkorb().values())
 
                 wkV.add(elem);
 
 
             for (Artikel elem : kunde.getWarenkorb().values())
 
                 gesamtPreis = gesamtPreis + (elem.getPreis() * elem.getBestellteMenge());
 
 
             Rechnung rechnung = new Rechnung(kunde.getNummer(), wkV, gesamtPreis);
             rechnungsBestandNr.put(rechnung.getrNr(), rechnung);
             Vector<Integer> vI = rechnungsBestandKundenNr.get(kunde.getNummer());
             vI.add(rechnung.getrNr());
             rechnungsBestandKundenNr.put(kunde.getNummer(), vI);
             if (rechnung.getdNow() == null) {
                 Date dNow = new Date();
                 setDate(rechnung.getrNr(), dNow);
                 String text = ft.format(rechnung.getdNow()) + "\nDie Rechnung mit der Rechnungsnummer " + rechnung.getrNr() + " wurde hinzugefügt.";
                 l.writeLog(dateiName, text);
 
 
             }
 
 
         } else if (!rechnungsBestandKundenNr.containsKey(kunde.getNummer())) {
             Vector<Artikel> wkV = new Vector<Artikel>();
 
             double gesamtPreis = 0;
             for (Artikel elem : kunde.getWarenkorb().values())
 
                 wkV.add(elem);
 
 
             for (Artikel elem : kunde.getWarenkorb().values())
 
                 gesamtPreis = gesamtPreis + (elem.getPreis() * elem.getBestellteMenge());
             Rechnung rechnung = new Rechnung(kunde.getNummer(), wkV, gesamtPreis);
             rechnungsBestandNr.put(rechnung.getrNr(), rechnung);
             Vector<Integer> vI = new Vector<Integer>();
             vI.add(rechnung.getrNr());
             rechnungsBestandKundenNr.put(kunde.getNummer(), vI);
             if (rechnung.getdNow() == null) {
                 Date dNow = new Date();
                 setDate(rechnung.getrNr(), dNow);
                 String text = ft.format(rechnung.getdNow()) + "\nDie Rechnung mit der Rechnungsnummer " + rechnung.getrNr() + " wurde hinzugefügt.";
                 l.writeLog(dateiName, text);
 
 
             }
 
         }
 
     }
 
     /**
      * Methode zum Hinzufuegen von Rechnungen durch den PersistenceManager
      *
      * @param r
      */
     public void rechnungHinzufuegen(Rechnung r) {
 
         if (rechnungsBestandKundenNr.containsKey(r.getkNr())) {
             rechnungsBestandNr.put(r.getrNr(), r);
             Vector<Integer> vI = rechnungsBestandKundenNr.get(r.getkNr());
             vI.add(r.getrNr());
             rechnungsBestandKundenNr.put(r.getkNr(), vI);
             if (r.getZaehler() <= r.getrNr()) {
                 r.setZaehler(r.getrNr() + 1);
             }
         } else {
             rechnungsBestandNr.put(r.getrNr(), r);
             Vector<Integer> vI = new Vector<Integer>();
             vI.add(r.getrNr());
             rechnungsBestandKundenNr.put(r.getkNr(), vI);
             if (r.getZaehler() <= r.getrNr()) {
                 r.setZaehler(r.getrNr() + 1);
             }
         }
 
     }
 
     /**
      * Methode zum loeschen von Kunden durch den PersistenceManager
      *
      * @param rechnung
      * @throws IOException
      * @throws RechnungExestiertNichtException
      *
      */
     public void rechnungLoeschen(Rechnung rechnung) throws IOException, RechnungExestiertNichtException {
 
         Rechnung r = rechnung;
         Vector<Integer> vI = rechnungsBestandKundenNr.get(r.getkNr());
         //
         if (rechnungsBestandNr.containsKey(r.getrNr()) & vI.contains(r.getrNr())) {
             rechnungsBestandNr.remove(r.getrNr());
 
             vI.removeElement(r.getrNr());
             rechnungsBestandKundenNr.put(r.getkNr(), vI);
             Date dNow = new Date();
            String text = ft.format(dNow) + ": Die Rechnung mit der Rechnungsnummer " + rechnung.getrNr() + " wurde gelöscht.";
             l.writeLog(dateiName, text);
 
         } else if ((!(rechnungsBestandNr.containsKey(r.getrNr()))) & (!(vI.contains(r.getrNr())))) {
             throw new RechnungExestiertNichtException(r.getrNr());
         }
 
 
     }
 
     /**
      * Methode gibt einen Vector zurueck der alle vorhandenen Rechnungen enthaelt
      *
      * @return
      * @throws RechnungKeineVorhandenException
      *
      */
     public Vector alleRechnungenZurueckgeben() {
 
         if ((!rechnungsBestandNr.values().isEmpty())) {
             Vector<Rechnung> ergebnis = new Vector<Rechnung>();
 
             for (Rechnung elem : rechnungsBestandNr.values())
                 ergebnis.add(elem);
 
             return ergebnis;
         } else {
             return null;
         }
     }
 
     /**
      * Methode gibt einen Vector zurueck der alle Rechnungen eines Kunden enthaelt
      *
      * @return
      * @throws RechnungKeineVorhandenException
      *
      */
     public Vector alleRechnungenEinesKundenZurueckgeben(int kNr) {
 
         // wenn Rechnungen für den Kunden mit der Kundennummer exestieren
         if (rechnungsBestandKundenNr.containsKey(kNr)) {
 
             // Erzeugt eine int Vector der die Nummer aller Rechnungen des Kunden speichert
             Vector<Integer> kundenRechnungen = rechnungsBestandKundenNr.get(kNr);
             // Erzeugt einen Vector in dem alle Rechnungsobjekte eine Kundne am Ende zurueck gegeben werden
             Vector<Rechnung> alleRechnungenEinesKunden = new Vector<Rechnung>();
 
             // Die For Schleife packt alle Rechnungen eines Kunden in den alleRechnungenEinesKunden Vector
             for (int i = 0; i < kundenRechnungen.size(); i++) {
                 int zaehler = kundenRechnungen.get(i);
                 Rechnung rechnung = rechnungsBestandNr.get(zaehler);
                 alleRechnungenEinesKunden.add(rechnung);
             }
 
             // gibt alle Rechnungen eines Kunden zurueck
             return alleRechnungenEinesKunden;
 
 
         } else {
             return null;
         }
     }
 
     /**
      * Methode gibt die HashMap zurueck die alle vorhandenen Rechnungen enthaelt
      *
      * @return
      */
     public HashMap<Integer, Rechnung> alleRechnungenHashMapZurueckgeben() {
         return rechnungsBestandNr;
     }
 
     /**
      * Methode gibt ein Rechnungobject zurück
      *
      * @param kNr
      * @return
      * @throws RechnungExestiertNichtException
      *
      */
 
     public Rechnung letzteKundenrechnungAusgeben(int kNr) throws RechnungExestiertNichtException {
 
         if (rechnungsBestandKundenNr.containsKey(kNr)) {
             Vector<Integer> rechnungsnummern = rechnungsBestandKundenNr.get(kNr);
 
             int nr = rechnungsnummern.lastElement();
             Rechnung ergebnis = rechnungsBestandNr.get(nr);
 
             return ergebnis;
         } else if (!rechnungsBestandKundenNr.containsKey(kNr)) {
             throw new RechnungExestiertNichtException();
         } else {
             return null;
         }
 
     }
 
     /**
      * Methode durchsucht alle Rechnungen nach einer Rechnungsnummer
      *
      * @param rNr
      * @return
      * @throws RechnungExestiertNichtException
      *
      */
     public Rechnung sucheRechnung(int rNr) throws RechnungExestiertNichtException {
         if (rechnungsBestandNr.containsKey(rNr)) {
             return rechnungsBestandNr.get(rNr);
         } else if (!rechnungsBestandNr.containsKey(rNr)) {
             throw new RechnungExestiertNichtException(rNr);
         } else {
             return null;
         }
     }
 
     /**
      * Methode setzt das Datum einer Rechnung wenn sie exestiert
      *
      * @param rNr
      * @param dNow
      * @throws RechnungExestiertNichtException
      *
      */
     public void setDate(int rNr, Date dNow) throws RechnungExestiertNichtException {
 
         // Wenn die Rechnungsnummer exestiert wird das heutige Datum auf die Rechnung gesetzt
         if (rechnungsBestandNr.containsKey(rNr)) {
 
             Rechnung r = rechnungsBestandNr.get(rNr);
 
             r.setDate(dNow);
             rechnungsBestandNr.put(rNr, r);
 
 
         } else if (!rechnungsBestandNr.containsKey(rNr)) {
             throw new RechnungExestiertNichtException(rNr);
         }
 
     }
 
     //TODO @Noshaba Bitte kommentieren
     public Vector<String> printRechnungsLog(int daysInPast, String rNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException {
         return l.printLog("Eshop_RechnungsLog.txt", daysInPast, rNr);
     }
 
     public Vector<String> printRechnungsLog(int daysInPast) throws FileNotFoundException, KeineEintraegeVorhandenException, ParseException {
         return l.printLog("Eshop_RechnungsLog.txt", daysInPast);
     }
 
     public Vector<String> printRechnungsLog(String rNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException {
         return l.printLog("Eshop_RechnungsLog.txt", rNr);
     }
 
     public String printRechnungsLog() throws FileNotFoundException, KeineEintraegeVorhandenException {
         return l.printLog("Eshop_RechnungsLog.txt");
     }
 
 }
 
