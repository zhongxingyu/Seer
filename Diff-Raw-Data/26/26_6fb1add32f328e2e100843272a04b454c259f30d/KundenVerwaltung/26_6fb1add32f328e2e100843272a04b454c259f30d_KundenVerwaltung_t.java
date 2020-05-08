 package eshop.local.domain;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 import eshop.local.exception.*;
 import eshop.local.persistence.Log;
 import eshop.local.valueobjects.Adresse;
 import eshop.local.persistence.FilePersistenceManager;
 import eshop.local.persistence.PersistenceManager;
 import eshop.local.valueobjects.Artikel;
 import eshop.local.valueobjects.Kunde;
 import eshop.local.valueobjects.Mitarbeiter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Giacomo
  * Date: 18.04.13
  * Time: 14:42
  * To change this template use File | Settings | File Templates.
  */
 
 
 public class KundenVerwaltung {
 
     // Dokument zum Speichern des Logs
     private File dateiName = new File("Eshop_KundenLog.txt");
 
     // Hashmap zum speichern des Kundenbestands als Key dienen die Kundennummern
     private HashMap<Integer, Kunde> kundenBestandNr;
 
     // Hashmap zum verknüpfen des Benutzernamens mit der Kundennummer
     private HashMap<String, Integer> kundenBestandBenutzername;
 
     // Persistenz-Schnittstelle, die für die Details des Dateizugriffs verantwortlich ist
     private PersistenceManager pm = new FilePersistenceManager();
 
     // Anderes Datums-Format
     private final SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'um' HH:mm:ss zzz':'");
 
     private Log l = new Log();
 
 
     /**
      * Konstruktor
      */
     public KundenVerwaltung() {
         // verknüpft die Kundennummern mit den Kunden Objekten
         kundenBestandNr = new HashMap<Integer, Kunde>();
         // verknüpft die Benutzernamen  mit den Kundennummern
         kundenBestandBenutzername = new HashMap<String, Integer>();
     }
 
 // Methoden
 
     /**
      * Methode zum Einlesen der Kundendaten aus einer Datei.
      *
      * @param datei
      *
      * @throws IOException,ClassNotFoundException
      *
      */
     public void liesDaten(String datei) throws IOException, ClassNotFoundException {
         // Erstellung eines Kunden Objekts
         Kunde einKunde;
         try {
             // PersistenzManager für Lesevorgänge wird geöffnet
             pm.openForReading(datei);
             do {
                 // Kunden-Objekt einlesen
                 einKunde = pm.ladeKunde();
                 if (einKunde != null) {
                     // Kunde einfügen
                     kundeHinzufuegen(einKunde);
                 }
             } while (einKunde != null);
             // PersistenzManager für Lesevorgänge wird wieder geschlossen
             pm.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Methode zum Schreiben der Kundendaten in eine Datei.
      *
      * @param datei
      *
      * @throws IOException
      */
     public void schreibeDaten(String datei) throws IOException {
         // PersistenzManager für Schreibvorgänge öffnen
         pm.openForWriting(datei);
         // Kunden-Objekte aus der Hashmap kundenBestandNr einlesen und in die Datei schreiben
         if (!kundenBestandNr.isEmpty()) {
             Iterator iter = kundenBestandNr.values().iterator();
             while (iter.hasNext()) {
                 Kunde k = (Kunde) iter.next();
                 pm.speichereKunde(k);
             }
         }
         //Persistenz-Schnittstelle wieder schließen
         pm.close();
     }
 
     /**
      * Methode zum hinzufuegen von Kunden
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
     public void kundeHinzufuegen(String vorname, String nachname, String benutzername, String passwort, String email, String telefon, Adresse adresse) throws IOException, BenutzernameExistiertBereitsException {
         // Wenn der Benutzername eines Kunden bereits vorhanden ist wird false zurück gegeben und kein neuer Kunde angelegt
         if (kundenBestandBenutzername.containsKey(benutzername)) {
             throw new BenutzernameExistiertBereitsException(benutzername);
         } else if (!kundenBestandBenutzername.containsKey(benutzername)){
             // Ist der Benutzername noch nicht vergeben wird er neu angelegt und in den beiden HasMaps gespeichert (kundenBestandNr, kundenBestandBenutzername)
             Kunde kunde = new Kunde(vorname, nachname, benutzername, passwort, email, telefon, adresse);
             kundenBestandNr.put(kunde.getNummer(), kunde);
             kundenBestandBenutzername.put(benutzername, kunde.getNummer());
             Date dNow = new Date();
             String text = ft.format(dNow) + "\nDer Kunde '" + benutzername + "' mit der Kundennummer " + kunde.getNummer() + " wurde hinzugefügt.";
             l.writeLog(dateiName, text);
         }
     }
 
     /**
      * Methode zum hinzufuegen von Kunden durch den PersistenceManager
      *
      * @param k -> Der Kund der hinzugefuegt werden soll
      */
     public void kundeHinzufuegen(Kunde k) {
         kundenBestandNr.put(k.getNummer(), k);
         kundenBestandBenutzername.put(k.getBenutzername(), k.getNummer());
         if (k.getZaehler() <= k.getNummer()) {
             k.setZaehler(k.getNummer() + 1);
         }
     }
 
     /**
      * Methode zum löschen von Kunden
      *
      * @param kundenNr -> Nummer des Kunden, der gelöscht werden soll
      * @param  m -> Mitarbeiter der den Kunden löscht
      *
      * @throws IOException
      * @throws  KundenNummerExistiertNichtException
      */
     public void kundenLoeschen(int kundenNr, Mitarbeiter m) throws IOException, KundenNummerExistiertNichtException{
         if (kundenBestandNr.containsKey(kundenNr)) {
             Kunde k = kundenBestandNr.get(kundenNr);
             kundenBestandNr.remove(kundenNr);
             kundenBestandBenutzername.remove(k.getBenutzername());
             Date dNow = new Date();
             String text = ft.format(dNow) + "\nDer Kunde '" + k.getBenutzername() + "' mit der Kundennummer " + kundenNr + " wurde vom Mitarbeiter " + m.getBenutzername() + " mit der Mitarbeiternummer " + m.getmNr() + " gelöscht.";
             l.writeLog(dateiName, text);
         } else if(!kundenBestandNr.containsKey(kundenNr)){
             throw new KundenNummerExistiertNichtException(kundenNr);
         }
     }
 
     /**
      * Methode gibt alle vorhandenen Kunden zurück
      *
      * @return Vector -> Enthälz alle vorhandenen Kunden
      */
     public Vector alleKundenZurueckgeben() {
         Vector<Kunde> ergebnis = new Vector<Kunde>();
         for (Kunde elem : kundenBestandNr.values())
             ergebnis.add(elem);
         return ergebnis;
     }
 
     /**
      * Methode gibt die HashMap zurueck die alle vorhandenen Kunden enthaelt
      *
      * @return HashMap<Integer, Kunde> -> HashMap die alle Kunden enthält
      */
     public HashMap<Integer, Kunde> alleKundenHashMapZurueckgeben() {
         return kundenBestandNr;
     }
 
     /**
      * Methode durchsucht alle Kunden nach einer Kundennummer
      *
      * @param kNr -> Kundennummer des gesuchten Kunden
      *
      * @throws KundenNummerExistiertNichtException
      *
      * @return Kunde -> Kunde der zur gesuchten Kundennummer passt
      */
     public Kunde getKunde(int kNr) throws KundenNummerExistiertNichtException {
         if (kundenBestandNr.containsKey(kNr)) {
             return kundenBestandNr.get(kNr);
         } else if(!kundenBestandNr.containsKey(kNr)){
             throw new KundenNummerExistiertNichtException(kNr);
         } else {
             return null;
         }
     }
 
     /**
      * Methode durchsucht alle Kunden nach dem Benutzername und gibt die Kundennummer zurück
      *
      * @param bName -> Der gesuchte Benutzername
      *
      * @throws BenutzernameExistiertNichtException
      *
      * @return  int -> Kundennummer die zum gesuchten Benutzernamen passt
      */
     public int getKnr(String bName) throws BenutzernameExistiertNichtException{
         if (kundenBestandBenutzername.containsKey(bName)) {
             return kundenBestandBenutzername.get(bName);
         } else if (!kundenBestandBenutzername.containsKey(bName)){
             throw new BenutzernameExistiertNichtException(bName);
         } else {
             return 0;
         }
     }
 
     /**
      * legt einen Artikel in den Warenkorb eines Kunden
      *
      * @param a -> Artikel der in den warenkorb gelegt werden soll
      * @param kNr -> Kundennummer des Kunden dem der warenkorb gehört
      *
      * @throws KundenNummerExistiertNichtException
      */
     public void inWarenkorbLegen(Artikel a, int kNr) throws KundenNummerExistiertNichtException {
         if(kundenBestandNr.containsKey(kNr) ){
             Kunde k = kundenBestandNr.get(kNr);
             k.inWarenkorbLegen(a);
         } else if (!kundenBestandNr.containsKey(kNr)){
             throw new KundenNummerExistiertNichtException(kNr);
         }
     }
 
     /**
      * entfernt einen Artikel aus dem Warenkorb eines Kunden
      *
      * @param a -> Artikel der aus dem Warenkorb entfernt werden soll
      * @param kNr -> Kundennummer des Kunden dem der warenkorb gehört
      *
      * @throws KundenNummerExistiertNichtException
      */
     public void ausWarenkorbEtfernen(Artikel a, int kNr) throws KundenNummerExistiertNichtException{
         if(kundenBestandNr.containsKey(kNr)){
             Kunde k = kundenBestandNr.get(kNr);
             k.ausWarenkorbEntfernen(a);
         } else if (!kundenBestandNr.containsKey(kNr)){
             throw new KundenNummerExistiertNichtException(kNr);
         }
     }
 
     /**
      * setzt den Warenkorb eines Kunden komplett zureuck(leert ihn vollständig)
      *
      * @param kNr -> Kundennummer des Kunden dessen Warenkorb geleert werden soll
      *
      * @throws KundenNummerExistiertNichtException
      */
     public void resetWarenkorb(int kNr) throws KundenNummerExistiertNichtException{
         if(kundenBestandNr.containsKey(kNr)){
             Kunde k = kundenBestandNr.get(kNr);
             k.resetWarenkorb();
             kundenBestandNr.put(kNr,k);
         } else if(!kundenBestandNr.containsKey(kNr)){
             throw new KundenNummerExistiertNichtException(kNr);
         }
     }
 
     /**
      * Methode prüft ob der Benutzername des Kunden existiert und prüft ob das richtige Passwort eingegeben wurde
      *
      * @param benutzername -> Benutzername des kunden der sich einloggen möchte
      * @param passwort -> Das von dem Kunden der sich einzuloggen versucht eingegebene Passwort
      *
      * @return boolean -> true, wenn der Benutzername existiert und das Passwort korrekt ist, ansonsten false
      */
     public boolean findeKunden(String benutzername, String passwort) {
         if (kundenBestandBenutzername.containsKey(benutzername)) {
             int knr = kundenBestandBenutzername.get(benutzername);
             Kunde k = kundenBestandNr.get(knr);
             String p = k.getPasswort();
             System.out.println(p);
             System.out.println(k.getPasswort());
             if (p.equals(passwort)) {
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     //@TODO ungenutzte Methode?
     /**
      * Gibt ein Kundenobjekt zu einem Benutzernamen zurueck
      * @param bName -> Benutzername des gesuchten Kunden
      *
      * @throws BenutzernameExistiertNichtException
      *
      * @return Kunde -> Zum Benutzernamen passendes Kundenobjekt
      */
     public Kunde getKunden(String bName) throws BenutzernameExistiertNichtException{
         Kunde k;
         if(kundenBestandBenutzername.containsKey(bName)){
             int knr = kundenBestandBenutzername.get(bName);
             k = kundenBestandNr.get(knr);
             return k;
         }else if (!kundenBestandBenutzername.containsKey(bName)){
             throw new BenutzernameExistiertNichtException(bName);
         } else {
             return null;
         }
     }
 
     //@TODO ungenutzte Methode?
     /**
      * Methode um zu überprüfen ob ein Artikel bereits im warenkorb ist
      *
      * @param k -> Kunde dessen Warenkorb geprüft wird
      * @param aNr -> Der gesuchte Artikel
      *
      * @return boolean -> true, wenn der Artikel sich im Warenkorb befindet, ansonsten false
      */
     public boolean istImWarenkorb(Kunde k, int aNr){
         return k.istImWarenkorb(aNr);
     }
 
     /**
      * Methode zum Passwortaendern für Kunden
      *
      * @param k -> Kunde der sein Passwort ändern möchte
      * @param p1 -> das neue Passwort des Kunden
      * @param p2 -> Kontrollpasswort, welches identisch mit dem neuen Passwort sein muss
      *
      * @throws NeuesPasswortFehlerhaftException
      */
     public void passwortAendern(Kunde k, String p1, String p2) throws NeuesPasswortFehlerhaftException{
         if (p1.equals(p2)) {
             k.setPasswort(p1);
         } else {
             throw new NeuesPasswortFehlerhaftException();
         }
     }
 
     /**
      * Methode zum ändern des Vornamens eines Kunden
      *
      * @param k -> Kunde der seinen Vornamen ändern möchte
      * @param name -> Der neue Vorname des Kunden
      *
      */
     public void vornameAendern(Kunde k, String name) {
         k.setVorname(name);
         k.getAdresse().setVorname(name);
     }
 
     /**
      * Methode zum ändern des Nachnamens eines Kunden
      *
      * @param k -> Kunde der seinen Nachnamen ändern möchte
      * @param name -> Der neue Nachname des Kunden
      *
      */
     public void nachnameAendern(Kunde k, String name) {
         k.setNachname(name);
         k.getAdresse().setNachname(name);
     }
 
     /**
      * Methode zum ändern der Telefonnummer eines Kunden
      *
      * @param k -> Kunde der seine Telefonnummer ändern möchte
      * @param nummer -> Die neue Telefonnummer des Kunden
      *
      */
     public void telefonAendern(Kunde k, String nummer) {
         k.setTelefon(nummer);
     }
 
     /**
      * Methode zum ändern der Emailadresse eines Kunden
      *
      * @param k -> Kunde der seine Emailadresse ändern möchte
      * @param mail -> Die neue Emailadresse des Kunden
      *
      */
     public void emailAendern(Kunde k, String mail) {
         k.setEmail(mail);
     }
 
     /**
      * Methode zum ändern des Wohnorts eines Kunden
      *
      * @param k -> Kunde der seinen Wohnort ändern möchte
      * @param ort -> Der neue Wohnort des Kunden
      *
      */
     public void ortAendern(Kunde k, String ort) {
         k.getAdresse().setOrt(ort);
     }
 
     /**
      * Methode zum ändern der Postleihzahl eines Kunden
      *
      * @param k -> Kunde der seine Postleihzahl ändern möchte
      * @param plz -> Die neue Postleihzahl des Kunden
      *
      */
     public void plzAendern(Kunde k, String plz) {
         k.getAdresse().setPlz(plz);
     }
 
     /**
      * Methode zum ändern der Straße eines Kunden
      *
      * @param k -> Kunde der seine Straße ändern möchte
      * @param straße -> Die neue Straße des Kunden
      *
      */
     public void straßeAendern(Kunde k, String straße) {
         k.getAdresse().setStraße(straße);
     }
 
     //TODO @Noshaba Bitte kommentieren
     public Vector<String> printKundenLog(int daysInPast, String kNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException {
         return l.printLog("Eshop_KundenLog.txt", daysInPast, kNr);
     }
 
     public Vector<String> printKundenLog(int daysInPast) throws FileNotFoundException, KeineEintraegeVorhandenException, ParseException{
         return l.printLog("Eshop_KundenLog.txt", daysInPast);
     }
 
     public Vector<String> printKundenLog(String kNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
         return l.printLog("Eshop_KundenLog.txt", kNr);
     }
 
     public String printKundenLog() throws FileNotFoundException, KeineEintraegeVorhandenException{
         return l.printLog("Eshop_KundenLog.txt");
     }
 }
 
 
