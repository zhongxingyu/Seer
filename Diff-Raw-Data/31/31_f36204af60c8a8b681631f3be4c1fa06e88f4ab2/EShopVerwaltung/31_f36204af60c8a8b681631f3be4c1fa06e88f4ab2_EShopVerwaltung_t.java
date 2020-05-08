 package eshop.local.domain;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Vector;
 
 //import com.sun.org.apache.xml.internal.security.utils.IgnoreAllErrorHandler;
 import eshop.local.exception.*;
 import eshop.local.persistence.Log;
 import eshop.local.valueobjects.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Giacomo
  * Date: 10.04.13
  * Time: 15:00
  * To change this template use File | Settings | File Templates.
  */
 
 public class EShopVerwaltung {
 
     // Präfix für Namen der Dateien, in der die Artikeldaten gespeichert sind
     private String datei = "";
     // Erzeugt eine Artikelverwaltung
     private ArtikelVerwaltung meineArtikel;
     // Erzeugt eine Kundenverwaltung
     private KundenVerwaltung meineKunden;
     // Erzeugt eine Rechnungsverwaltung
     private RechnungsVerwaltung meineRechnungen;
     // Erzeugt eine Mitarbeuterverwaltung
     private MitarbeiterVerwaltung meineMitarbeiter;
 
 
 // Konstruktor
 
     /**
      * Namensmuster für Dateien:
      * datei+"_Artikel.ser" ist die Datei der Artikel
      * datei+"_Kunden.ser" ist die Datei der Kunden
      * datei+"_Rechnungen.ser" ist die Datei der Rechnungen
      *
      * @param datei
      * @throws IOException,ClassNotFoundException z.B. wenn eine der Dateien nicht existiert.
      * @return Vector
      */
     public EShopVerwaltung(String datei) throws IOException, ClassNotFoundException {
 
         this.datei = datei;
         // Artikelbestand aus Datei einlesen
         meineArtikel = new ArtikelVerwaltung();
         meineArtikel.liesDaten(datei + "_Artikel.ser");
         // Kundenkartei aus Datei einlesen
         meineKunden = new KundenVerwaltung();
         meineKunden.liesDaten(datei + "_Kunden.ser");
         // Rechnungsbestand aus Datei einlesen
         meineRechnungen = new RechnungsVerwaltung();
         meineRechnungen.liesDaten(datei + "_Rechnungen.ser");
         //Mitarbeiterbestand aus Datei einlesen
         meineMitarbeiter = new MitarbeiterVerwaltung();
         meineMitarbeiter.liesDaten(datei + "_Mitarbeiter.ser");
     }
 
 // Methoden
 
     /**
      * Methode, die eine Liste aller im Bestand befindlichen Artikel zurückgibt Aufgabe wird an die Artikelverwaltung delegiert
      *
      * @return Vector Liste aller Artikel im Bestand des EShops als Vector
      */
     public Vector gibAlleArtikel() {
         return meineArtikel.alleArtikelZurueckgeben();
     }
 
     /**
      * Methode gibt die HashMap zurueck die alle vorhandenen Artikeln enthaelt
      *
      * @return HashMap<Integer,Artikel> die alle Artikel beinhaltet
      */
     public HashMap<Integer,Artikel> gibAlleArtikelHashMapZurueckgeben() {
         return meineArtikel.alleArtikelHashMapZurueckgeben();
     }
 
 
 
     /**
      * Methode, die eine Liste aller im Bestand befindlichen Kunden zurückgibt Aufgabe wird an die Kundenverwaltung delegiert
      *
      * @return Vector Liste aller Kunden im Bestand des EShops als Vector
      */
     public Vector gibAlleKunden() {
         return meineKunden.alleKundenZurueckgeben();
     }
 
     /**
      * Gibt die HashMap mit allen Kunden zurueck
      *
      * @return HashMap<Integer,Kunde> HasMap mit allen Kunden
      */
     public HashMap<Integer,Kunde> gibAlleKundenHashMapZurueckgeben(){
         return meineKunden.alleKundenHashMapZurueckgeben();
     }
 
     /**
      * Methode, die eine Liste aller im Bestand befindlichen Rechnungen zurückgibt Aufgabe wird an die Rechnungsverwaltung delegiert
      *
      * @return Vector Liste aller Rechnungen im Bestand des EShops als Vector
      */
     public Vector gibAlleRechnungen()  {
         return meineRechnungen.alleRechnungenZurueckgeben();
     }
 
     /**
      * Gibt die HashMap mit allen Rechnungen zurueck
      *
      * @return HashMap<Integer,Rechnung> HashMap mit allen Rechnungen
      */
     public HashMap<Integer,Rechnung> gibAlleRechnungenHashMapZurueckgeben(){
         return meineRechnungen.alleRechnungenHashMapZurueckgeben();
     }
 
     /**
      * Methode gibt einen Vector zurueck der alle Rechnungen eines Kunden enthaelt
      *
      * @return
      * @throws RechnungKeineVorhandenException
      *
      */
     public Vector giballeRechnungenEinesKundenZurueckgeben(int kNr) {
         return meineRechnungen.alleRechnungenEinesKundenZurueckgeben(kNr);
     }
 
     /**
      * Methode, die eine Liste aller im Bestand befindlichen Mitarbeiter zurückgibt Aufgabe wird an die Mitarbeiterverwaltung delegiert
      *
      * @return Vector Liste aller Mitarbeiter im Bestand des EShops als Vector
      */
     public Vector gibAlleMitarbeiter() {
         return meineMitarbeiter.alleMitarbeiterZurueckgeben();
     }
 
     /**
      * Gibt die HashMap mit allen Mitarbeitern zurueck
      *
      * @return HashMap<Integer,Mitarbeiter> HashMap mit allen Mitarbeitern
      */
     public HashMap<Integer,Mitarbeiter> gibAlleMitarbeiterHashMapZurueckgeben(){
         return meineMitarbeiter.alleMitarbeiterHashMapZurueckgeben();
     }
 
     /**
      * Methode zum Suchen von Mitarbeitern anhand des Benutzernamens
      *
      * @param bName -> Benutzername des gesuchten Mitarbeiters
      * @param bPasswort -> Passwort, welches der Mitarbeiter eingegeben hat
      *
      * @return boolean, true wenn der Mitarbeiter gefunden wurde und das Passwort korrekt ist, ansonsten false
      */
     public boolean findeMitarbeiter(String bName, String bPasswort)  {
         return meineMitarbeiter.findeMitarbeiter(bName, bPasswort);
     }
 
     /**
      * Gibt die entsprechende MitarbeiterNr zu einem Benutzernamen zurück
      *
      * @throws MitarbeiterExistiertNichtException
      *
      * @return int -> Die zum Benutzernamen passende Mitarbeiternummer
      */
     public int getMitarbeiterNr(String benutzername) throws MitarbeiterExistiertNichtException{
        return meineMitarbeiter.getMitarbeiterNr(benutzername);
     }
 
     /**
      * Methode zum Suchen von Kunden anhand des Benutzernamens
      *
      * @param bName -> Benutzername des gesuchten Kunden
      * @param bPasswort -> Passwort, welches von dem Kunden eingegeben wurde
      *
      * @return boolean -> true, wenn der Kunde existiert und das Passwort korrekt ist, ansonsten false
      */
     public boolean findeKunden(String bName, String bPasswort) {
         return meineKunden.findeKunden(bName, bPasswort);
     }
 
     /**
      * Methode zum Suchen von Artikel anhand des Namens
      *
      * @param name Name des gesuchten Artikels
      *
      * @return Vector Rückgabe eines Vectors mit dem gefundenen Artikel
      */
     public Vector sucheNachName(String name) {
         return meineArtikel.sucheArtikel(name);
     }
 
     /**
      * Methode zum Suchen von Rechnungen anhand des Rechnungsnummer
      *
      * @param rNr -> Rechnungsnummer der gesuchten Rechnung
      *
      * @throws RechnungExestiertNichtException
      *
      * @return Rechnung -> Die gesuchte zur Rechnungsnummer passende Rechnung
      */
     public Rechnung sucheNachRechnungsnummer(int rNr) throws RechnungExestiertNichtException {
         return meineRechnungen.sucheRechnung(rNr);
     }
 
     /**
      * Methode zum Suchen von Artikeln anhand der Artikelnummer
      *
      * @param aNr -> Artikelnummer des gesuchten Artikels
      *
      * @throws ArtikelExestiertNichtException
      *
      * @return Artikel -> Der zur gesuchten Nummer passende Artikel
      */
     public Artikel getArtikel(int aNr) throws ArtikelExestiertNichtException {
         return meineArtikel.getArtikel(aNr);
     }
 
     //@ TODO ungenutzte Methode?
     public boolean existiertArtikel(int aNr) {
         return meineArtikel.existiertArtikel(aNr);
     }
 
     //@ TODO ungenutzte Methode?
     public boolean istImWarenkorb(Kunde k, int aNr){
         return meineKunden.istImWarenkorb(k,aNr);
     }
 
     /**
      * Methode um Artikel in den Warenkorb zu legen
      *
      * @param a -> Artikel der in den warenkorb gelegt werden soll
      * @param kNr -> Kundennummer des Kunden, der den Artikel in den Warenkorb legen will
      *
      * @throws KundenNummerExistiertNichtException
      */
     public void inWarenkorbLegen(Artikel a, int kNr) throws KundenNummerExistiertNichtException{
         meineKunden.inWarenkorbLegen(a, kNr);
     }
 
     /**
      * Methode um Artikel aus dem Warenkorb zu entfernen
      *
      * @param a -> Artikel der aus dem Warenkorb entfernt werden soll
      * @param kNr -> Kundennummer des Kunden, dem der Warenkorb gehört
      *
      * @throws KundenNummerExistiertNichtException
      */
     public void ausWarenkorbEntfernen(Artikel a, int kNr) throws KundenNummerExistiertNichtException{
         meineKunden.ausWarenkorbEtfernen(a, kNr);
     }
 
     //@TODO ungenutzte Methode?
     /**
      * Methode zum zuruecksetzen(vollständigem Leeren) des Warenkorbes eines Kunden
      *
      * @param kNr -> Kundennummer des Kunden, dessen Warenkorb geleert werden soll
      *
      * @throws KundenNummerExistiertNichtException
      */
     public void resetWarenkorb(int kNr) throws KundenNummerExistiertNichtException{
         meineKunden.resetWarenkorb(kNr);
     }
 
     /**
      * Methode zum ändern des Bestands eines Artikels
      *
      * @param artNr -> Artikelnummer des Artikels, dessen Bestand geändert werden soll
      * @param wert -> neuer Wert des Bestands des Artikels
      * @param p -> Person die die Änderung am Bestand durchfuehrt
      *
      * @throws IOException
      * @throws ArtikelBestandNegativException
      * @throws ArtikelExestiertNichtException
      */
     public void setBestand(int artNr, int wert, Person p) throws IOException, ArtikelBestandNegativException, ArtikelExestiertNichtException {
         meineArtikel.setBestand(artNr, wert, p);
     }
 
     /**
      * Methode zum ändern der Bestellmenge
      *
      * @param menge -> Menge die vom Artikel bestellt werden soll
      * @param artikel -> Artikel der bestellt wird
      *
      * @throws BestellteMengeEntsprichtNichtderPackungsgroesseException
      * @throws ArtikelBestandZuNiedrigException
      * @throws ArtikelBestellteMengeNegativException
      */
     public void setBestellteMenge(int menge,Artikel artikel) throws BestellteMengeEntsprichtNichtderPackungsgroesseException, ArtikelBestellteMengeNegativException, ArtikelBestandZuNiedrigException {
         meineArtikel.setBestellteMenge(menge, artikel);
     }
 
     /**
      * Methode zum Einfügen eines neuen Artikels in den Bestand.
      *
      * @param name -> Name des Artikels
      * @param beschreibung -> Beschreibung des Artikels
      * @param preis -> Preis des Artikels
      *
      * @throws IOException
      * @throws ArtikelExestierBereitsException
      */
     public void fuegeArtikelEin(String name, String beschreibung, double preis, Mitarbeiter m) throws IOException, ArtikelExestierBereitsException {
         meineArtikel.artikelHinzufuegen(name, beschreibung, preis, m);
     }
 
     /**
      * Methode zum Einfügen eines neuen MassengutArtikels in den Bestand.
      *
      * @param name -> Name des MassengutArtikels
      * @param beschreibung -> Beschreibung des Massengutartikels
      * @param preis -> Preis des MassengutArtikels
      * @param packung -> Packungssgroesse des MassengutArtikels
      * @param m -> Mitarbeiter der den Massengutartikel anlegt
      *
      * @throws IOException
      * @throws ArtikelExestierBereitsException
      */
     public void fuegeMassengutArtikelEin(String name, String beschreibung, double preis, int packung, Mitarbeiter m ) throws IOException, ArtikelExestierBereitsException {
         meineArtikel.massengutartikelHinzufuegen(name, beschreibung, preis, packung, m);
     }
 
     /**
      * Methode zum Einfügen eines neuen Kunden in den Bestand.
      *
      * @param vorname -> Vorname des Kunden
      * @param nachname -> Nachname des Kunden
      * @param benutzername -> Benutzername des Kunden
      * @param passwort -> Passwort des Kunden
      * @param email -> Emailadresse des Kunden
      * @param telefon -> Telefonnummer des Kunden
      * @param adresse -> Adresse des Kunden
      *
      * @throws IOException
      * @throws BenutzernameExistiertBereitsException
      */
     public void fuegeKundeEin(String vorname, String nachname, String benutzername, String passwort, String email, String telefon, Adresse adresse) throws IOException, BenutzernameExistiertBereitsException{
         meineKunden.kundeHinzufuegen(vorname, nachname, benutzername, passwort, email, telefon, adresse);
 
     }
 
     /**
      * Methode zum Einfügen eines neuen Mitarbeiters in den Bestand.
      *
      * @param vorname -> Vorname des Mitarbeiters
      * @param nachname -> Nachname des Mitarbeiters
      * @param benutzername -> Benutzername des Mitarbeiters
      * @param passwort -> Passwort des Mitarbeiters
      * @param email -> Emailadresse des Mitarbeiters
      * @param telefon -> Telefonnummer des Mitarbeiters
      * @param adresse -> Adresse des Mitarbeiters
      *
      * @throws IOException
      * @throws MitarbeiterExistiertBereitsException
      */
     public void fuegeMitarbeiterEin(String vorname, String nachname, String benutzername, String passwort, String email, String telefon, Adresse adresse) throws IOException, MitarbeiterExistiertBereitsException{
         meineMitarbeiter.mitarbeiterHinzufuegen(vorname, nachname, benutzername, passwort, email, telefon, adresse);
     }
 
     /**
      * Methode zum Einfügen einer neuen Rechnung
      *
      * @param kunde -> Kunde der die Rechnung ausgestellt bekommt
      *
      * @throws IOException
      * @throws RechnungExestiertNichtException
      */
     public void fuegeRechnungEin(Kunde kunde) throws IOException, RechnungExestiertNichtException{
         meineRechnungen.rechnungHinzufuegen(kunde);
     }
 
     /**
      * Methode zum loeschen eines  Artikels aus dem  Bestand.
      *
      * @param artNr -> Nummer des Artikels des Artikel der geloescht werden soll
      * @param m -> Mitarbeiter der den Artikel löschen will
      *
      * @throws IOException
      * @throws ArtikelExestiertNichtException
      */
     public void loescheArtikel(int artNr, Mitarbeiter m) throws IOException, ArtikelExestiertNichtException {
         meineArtikel.artikelLoeschen(artNr, m);
     }
 
     /**
      * Methode zum loeschen eines  Kunden aus dem  Bestand.
      *
      * @param kunNr -> Nummer des Kunden der geloescht werden soll
      * @param m -> Mitarbeiter der den Kunden löschen will
      *
      * @throws IOException
      * @throws KundenNummerExistiertNichtException
      */
     public void loescheKunde(int kunNr,  Mitarbeiter m) throws IOException, KundenNummerExistiertNichtException{
         meineKunden.kundenLoeschen(kunNr, m);
     }
 
     /**
      * Methode zum loeschen eines Mitarbeiters aus dem  Bestand.
      *
      * @param mNr -> Nummer des Mitarbeiters der geloescht werden soll
      * @param m -> Mitarbeiter der den Mitarbeiter löschen will
      *
      * @throws IOException
      * @throws MitarbeiterExistiertNichtException
      */
     public void loescheMitarbeiter(int mNr, Mitarbeiter m) throws IOException, MitarbeiterExistiertNichtException{
         meineMitarbeiter.mitarbeiterLoeschen(mNr, m);
     }
 
     /**
      * Methode zum loeschen einer  Rechnung aus dem  Bestand.
      *
      * @param rechnung -> Rechnung die gelöscht wrden soll
      *
      * @throws IOException
      * @throws RechnungExestiertNichtException
      */
     public void loescheRechnung(Rechnung rechnung) throws IOException, RechnungExestiertNichtException{
         meineRechnungen.rechnungLoeschen(rechnung);
     }
 
     /**
      * Methode zum Speichern des Artikelbestands in einer Datei.
      *
      * @throws IOException
      */
     public void schreibeArtikel() throws IOException {
         meineArtikel.schreibeDaten(datei + "_Artikel.ser");
     }
 
     /**
      * Methode zum Speichern des Kundenbestands in einer Datei.
      *
      * @throws IOException
      */
     public void schreibeKunden() throws IOException {
         meineKunden.schreibeDaten(datei + "_Kunden.ser");
     }
 
     /**
      * Methode zum Speichern des Rechnungsbestanden in einer Datei.
      *
      * @throws IOException
      */
     public void schreibeRechung() throws IOException {
         meineRechnungen.schreibeDaten(datei + "_Rechnungen.ser");
     }
 
     /**
      * Methode zum Ausgeben der letzten Rechnung eines Kunden
      *
      * @param  kNr -> Nummer des Kunden dessen Rechnung ausgegebn werden soll
      *
      * @throws IOException
      * @throws RechnungExestiertNichtException
      *
      * @return Rechnung -> Die letzte Rechnung des Kunden
      */
     public Rechnung letzteKundenrechnungAusgeben(int kNr) throws IOException, RechnungExestiertNichtException {
         return meineRechnungen.letzteKundenrechnungAusgeben(kNr);
     }
 
     /**
      * Methode zum Speichern des Mitarbeiterbestands in einer Datei.
      *
      * @throws IOException
      */
     public void schreibeMitarbeiter() throws IOException {
         meineMitarbeiter.schreibeDaten(datei + "_Mitarbeiter.ser");
     }
 
     /**
      * Methode zum finden eines Kunden über die Kundennummer
      *
      * @param  kNr -> Kundennummer des gesuchten Kunden
      *
      * @throws KundenNummerExistiertNichtException
      *
      * @return Der zur Kundennummer passende Kunde
      */
     public Kunde getKunde(int kNr) throws KundenNummerExistiertNichtException{
         return meineKunden.getKunde(kNr);
     }
 
     /**
      * Methode zum finden eines Kunden über den Benutzernamen
      *
      * @param  bName -> Benutzername des gesuchten Kunden
      *
      * @throws BenutzernameExistiertNichtException
      *
      * @return der zum Benutzernamen passende Kunde
      */
     public int getKnr(String bName) throws BenutzernameExistiertNichtException{
         return meineKunden.getKnr(bName);
     }
 
     /**
      * Methode zum finden eines Mitarbeiters über den Benutzernamen
      *
      * @param  bName -> Benutzername des gesuchten Mitarbeiters
      *
      * @return der zum Benutzernamen passende Mitarbeiters
      */
     public int getMnr(String bName) {
         return meineMitarbeiter.getMnr(bName);
     }
 
     /**
      * Methode zum finden eines Mitarbeiters über die Mitarbeiternummer
      *
      * @param  mNr -> Mitarbeiternummer des gesuchten Mitarbeiters
      *
      * @throws MitarbeiterExistiertNichtException
      *
      * @return der zum Mitarbeiternummer passende Mitarbeiter
      */
     public Mitarbeiter getMitarbeiter(int mNr) throws MitarbeiterExistiertNichtException{
         return meineMitarbeiter.getMitarbeiter(mNr);
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printArtikelLog(int daysInPast, String aNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return meineArtikel.printArtikelLog(daysInPast, aNr);
     }
 
    public Vector<ArtikelBestandsGraph> getArtikelGraph(int daysInPast, String aNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
        return meineArtikel.getArtikelGraph(daysInPast, aNr);
    }

     //@ TODO Javadoc-Kommentar
     public Vector<String> printArtikelLog(int daysInPast) throws FileNotFoundException, ParseException, KeineEintraegeVorhandenException{
         return meineArtikel.printArtikelLog(daysInPast);
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printArtikelLog(String aNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return meineArtikel.printArtikelLog(aNr);
     }
 
     //@ TODO Javadoc-Kommentar
     public String printArtikelLog() throws FileNotFoundException, KeineEintraegeVorhandenException{
         return meineArtikel.printArtikelLog();
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printKundenLog(int daysInPast, String kNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return meineKunden.printKundenLog(daysInPast, kNr);
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printKundenLog(int daysInPast) throws FileNotFoundException, ParseException, KeineEintraegeVorhandenException{
         return meineKunden.printKundenLog(daysInPast);
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printKundenLog(String kNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return meineKunden.printKundenLog(kNr);
     }
 
     //@ TODO Javadoc-Kommentar
     public String printKundenLog() throws FileNotFoundException, KeineEintraegeVorhandenException{
         return meineKunden.printKundenLog();
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printMitarbeiterLog(int daysInPast, String mNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return meineMitarbeiter.printMitarbeiterLog(daysInPast, mNr);
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printMitarbeiterLog(int daysInPast) throws FileNotFoundException, ParseException, KeineEintraegeVorhandenException{
         return meineMitarbeiter.printMitarbeiterLog(daysInPast);
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printMitarbeiterLog(String mNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return meineMitarbeiter.printMitarbeiterLog(mNr);
     }
 
     //@ TODO Javadoc-Kommentar
     public String printMitarbeiterLog() throws FileNotFoundException, KeineEintraegeVorhandenException{
         return meineMitarbeiter.printMitarbeiterLog();
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printRechnungsLog(int daysInPast, String rNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return meineRechnungen.printRechnungsLog(daysInPast, rNr);
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printRechnungsLog(int daysInPast) throws FileNotFoundException, ParseException, KeineEintraegeVorhandenException{
         return meineRechnungen.printRechnungsLog(daysInPast);
     }
 
     //@ TODO Javadoc-Kommentar
     public Vector<String> printRechnungsLog(String rNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return meineRechnungen.printRechnungsLog(rNr);
     }
 
     //@ TODO Javadoc-Kommentar
     public String printRechnungsLog() throws FileNotFoundException, KeineEintraegeVorhandenException{
         return meineRechnungen.printRechnungsLog();
     }
 
     /**
      * Methode zum Aendern der Passwörter, findet heraus ob ein Kunde oder ein Mitarbeiter sein Passwort ändern möchte und leitet dementsprechend weiter
      *
      * @param p -> Person die ihr Passwort ändern möchte
      * @param p1 -> das neue Passwort
      * @param p2 -> Kontrollpasswort
      *
      * @throws NeuesPasswortFehlerhaftException
      */
     public void passwortAendern(Person p, String p1, String p2) throws NeuesPasswortFehlerhaftException{
         if ( p instanceof Kunde ) {
             meineKunden.passwortAendern((Kunde)p, p1, p2);
         } else if (p instanceof Mitarbeiter) {
             meineMitarbeiter.passwortAendern((Mitarbeiter)p, p1, p2);
         }
     }
 
     /**
      * Methode zum Vornamen ändern, leitet Mitarbeiter und Kunden an die jeweils passend Methode weiter
      *
      * @param p -> Person die die Aenderung durchfuehren moechte
      * @param vorname -> Der neue Vorname
      */
     public void vornamenAendern(Person p, String vorname) {
         if (p instanceof Kunde) {
             meineKunden.vornameAendern((Kunde)p, vorname);
         } else if (p instanceof Mitarbeiter) {
             meineMitarbeiter.vornameAendern((Mitarbeiter)p, vorname);
         }
     }
 
     /**
      * Methode zum Nachnamen ändern, leitet Mitarbeiter und Kunden an die jeweils passend Methode weiter
      *
      * @param p -> Person die die Aenderung durchfuehren moechte
      * @param nachname -> Der neue Nachname
      */
     public void nachnamenAendern(Person p, String nachname) {
         if (p instanceof Kunde) {
             meineKunden.nachnameAendern((Kunde)p, nachname);
         } else if (p instanceof Mitarbeiter) {
             meineMitarbeiter.nachnameAendern((Mitarbeiter)p, nachname);
         }
     }
 
     /**
      * Methode zum ändern der Telefonnummer, leitet Mitarbeiter und Kunden an die jeweils passend Methode weiter
      *
      * @param p -> Person die die Aenderung durchfuehren moechte
      * @param telefon -> Die neue Telefonnummer
      */
     public void telefonnummerAendern(Person p, String telefon) {
         if (p instanceof Kunde) {
             meineKunden.telefonAendern((Kunde)p, telefon);
         } else if (p instanceof Mitarbeiter) {
             meineMitarbeiter.telefonAendern((Mitarbeiter)p, telefon);
         }
     }
 
     /**
      * Methode zum ändern der Emailadresse, leitet Mitarbeiter und Kunden an die jeweils passend Methode weiter
      *
      * @param p -> Person die die Aenderung durchfuehren moechte
      * @param email -> Die neue Emailadresse
      */
     public void emailAdresseAendern(Person p, String email) {
         if (p instanceof Kunde) {
             meineKunden.emailAendern((Kunde)p, email);
         } else if (p instanceof Mitarbeiter) {
             meineMitarbeiter.emailAendern((Mitarbeiter)p, email);
         }
     }
 
     /**
      * Methode zum ändern der Straße, leitet Mitarbeiter und Kunden an die jeweils passend Methode weiter
      *
      * @param p -> Person die die Aenderung durchfuehren moechte
      * @param straße -> Die neue Straße
      */
     public void straßeAendern(Person p, String straße) {
         if (p instanceof Kunde) {
             meineKunden.straßeAendern((Kunde)p, straße);
         } else if (p instanceof Mitarbeiter) {
             meineMitarbeiter.straßeAendern((Mitarbeiter)p, straße);
         }
     }
 
     /**
      * Methode zum ändern des Wohnorts leitet Mitarbeiter und Kunden an die jeweils passend Methode weiter
      *
      * @param p -> Person die die Aenderung durchfuehren moechte
      * @param ort -> Der neue Wohnort
      */
     public void wohnortAendern(Person p, String ort) {
         if (p instanceof Kunde) {
             meineKunden.ortAendern((Kunde)p, ort);
         } else if (p instanceof Mitarbeiter) {
             meineMitarbeiter.ortAendern((Mitarbeiter)p, ort);
         }
     }
 
     /**
      * Methode zum ändern der Postleihzahl, leitet Mitarbeiter und Kunden an die jeweils passend Methode weiter
      *
      * @param p -> Person die die Aenderung durchfuehren moechte
      * @param plz -> Die neue Postleihzahl
      */
     public void plzAendern(Person p, String plz) {
         if (p instanceof Kunde) {
             meineKunden.plzAendern((Kunde)p, plz);
         } else if (p instanceof Mitarbeiter) {
             meineMitarbeiter.plzAendern((Mitarbeiter)p, plz);
         }
     }
 }
 
 
 
