 /**
  * Created with IntelliJ IDEA.
  * User: Giacomo
  * Date: 27.03.13
  * Time: 07:17
  * To change this template use File | Settings | File Templates.
  */
 
 package eshop.local.ui.cui;
 
 
 import eshop.local.domain.EShopVerwaltung;
 import eshop.local.exception.*;
 import eshop.local.valueobjects.*;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Vector;
 
 
 public class eshopClientCUI {
 
     private EShopVerwaltung eShopVerwaltung;
     private BufferedReader in;
 
 
 // Konstruktor
 
     public eshopClientCUI(String datei) throws IOException, ClassNotFoundException {
         // erzeugt eine eShopVerwaltung
         eShopVerwaltung = new EShopVerwaltung(datei);
         // Stream-Objekt fuer Texteingabe ueber Konsolenfenster erzeugen
         in = new BufferedReader(new InputStreamReader(System.in));
     }
 
 // Methoden
 
     /**
      * Methode öffnet eine readline und speichert diese in einem String
      *
      * @throws IOException
      */
     private String liesEingabe() throws IOException, LeereEingabeException {
         // einlesen von der Konsole
         String s = in.readLine();
         if (s.isEmpty()) {
             throw new LeereEingabeException();
         } else {
             return s;
         }
     }
 
 // Menues
 
     /**
      * Methode gibt das Hauptmenue aus
      *
      * @throws IOException
      */
     private void gibMainMenue() throws IOException {
         System.out.println("Willkommen im E-Shop!");
         System.out.print("Befehle: \n  Fortfahren zum Login: 'l'");
         System.out.print("         \n  Neue Kundenregistrierung: 'k'");
         System.out.print("         \n  Neue Mitarbeiterregistrierung: 'm'");
         System.out.print("         \n  Daten sichern:  's'");
         System.out.println("       \n  Programm beenden 'q'");
     }
 
     /**
      * Methode gibt das Mitarbeitermenue aus
      *
      * @throws IOException
      */
 
     private void gibMitarbeiterMenueAus() {
         System.out.print("Mitarbeiter: \n     hinzufuegen: 'mh'");
         System.out.print("               \n     löschen: 'ml");
         System.out.println("               \n     ausgeben:  'ma'");
         System.out.println("               \n     Daten bearbeiten:  'mb'");
 
         System.out.print("Kunden:     \n     hinzufuegen: 'kh'");
         System.out.print("              \n     Kunde löschen: 'kl");
         System.out.println("              \n     Kunden ausgeben:  'ka'");
 
         System.out.print("Artikel:    \n  hinzufuegen: 'ah'");
         System.out.print("              \n  MassengutArtikel hinzufuegen: 'am'");
         System.out.print("              \n  Bestand ändern: 'ab'");
         System.out.print("              \n  löschen: al");
         System.out.print("              \n  ausgeben:  'aa'");
         System.out.println("              \n  suchen:  'af'");
 
         System.out.print("Rechnung:    \n  löschen 'rl'");
         System.out.println("             \n  anzeigen 'ra'");
 
         System.out.print("Logs:    \n  ArtikelLog einsehen: 'la'");
         System.out.print("              \n  KundenLog einsehen: 'lk'");
         System.out.print("              \n  MitarbeiterLog einsehen: 'lm'");
         System.out.println("              \n  RechnungsLog einsehen: 'lr'");
 
         System.out.print("         \n  zurück ins Hauptmenue: 'o'");
         System.out.print("> "); // Prompt
         System.out.flush(); // ohne NL ausgeben
     }
 
     /**
      * Methode gibt das Kundenmenue aus
      *
      * @throws IOException
      */
     private void gibKundenMenueAus() {
         System.out.print("         \n  Liste aller Artikel anzeigen:  'l'");
         System.out.print("         \n  Artikel in den Warenkorb legen:  'w'");
         System.out.print("         \n  Artikel aus dem Warenkorb löschen:  'e'");
         System.out.print("         \n  Warenkorb anzeigen lassen:  'a'");
         System.out.print("         \n  Artikel aus dem Warenkorb bestellen & Rechnung erstellen :  'b'");
         System.out.print("         \n  Persönliche Daten bearbeiten: 'k'");
         System.out.print("         \n  zurück ins Hauptmenue: 'o'");
         System.out.print("> "); // Prompt
         System.out.flush(); // ohne NL ausgeben
     }
 
     /**
      * Methode gibt das Menue mit den Optionen zum Datenändern aus
      *
      * @throws IOException
      */
     private void gibDatenMenueAus() {
         System.out.print("         \n  Vornamen ändern:  'v'");
         System.out.print("         \n  Nachnamen ändern:  'n'");
         System.out.print("         \n  Passwort ändern:  'p'");
         System.out.print("         \n  Email-Adresse ändern:  'e'");
         System.out.print("         \n  Telefonnummer ändern:  't'");
         System.out.print("         \n  Straßennamen ändern:  's'");
         System.out.print("         \n  Wohnort ändern:  'w'");
         System.out.print("         \n  Plz ändern:  'z'");
     }
 
 // Methoden
 
     /**
      * Methode verarbeitet die Eingabe der Tastatur
      * @param line -> String mit der tastatureingabe
      * @return char -> char Version des erhaltenen Strings
      * @throws IOException
      */
 
     private char verarbeiteMainEingabe(String line) throws IOException {
 
         if (line.equals("l")) {
             //für Login
             return 'l';
 
         } else if (line.equals("k")) {
             //für Kundenregistrierung
             return 'k';
 
         } else if (line.equals("m")) {
             //für Mitarbeiterregistrierung
             return 'm';
 
         } else if (line.equals("s")) {
             // für das Sichern der Änderungen
             return 's';
         }
 
         return 'q';
 
     }
 
     /**
      * Methode verarbeitet das Login und schickt die Person ins passende Menue
      * @throws IOException
      * @throws LeereEingabeException
      */
 
     private void verarbeiteLogin() throws IOException, LeereEingabeException {
         try {
             System.out.println("Benutzernamen:");
             String bName = liesEingabe();
             System.out.println("Passwort:");
             String bPasswort = liesEingabe();
             if (eShopVerwaltung.findeMitarbeiter(bName, bPasswort)) {
                 System.out.println("Ihr Login war erfolgreich.");
                 System.out.println("Willkommen Mitarbeiter");
                 MitarbeiterEingabe(eShopVerwaltung.getMnr(bName));
 
             } else if (eShopVerwaltung.findeKunden(bName, bPasswort)) {
                 System.out.println("Ihr Login war erfolgreich.");
                 System.out.println("Willkommen Kunde");
                 KundenEingabe(eShopVerwaltung.getKnr(bName));
 
 
             } else {
                 System.out.println("Benutzername oder Passwort leider falsch!.");
 
 
             }
         } catch (KundenNummerExistiertNichtException knen) {
             System.err.println(knen.getMessage());
         } catch (BenutzernameExistiertNichtException ben) {
             System.err.println(ben.getMessage());
         }
     }
 
     /**
      * Methode verarbeitet die Eingabe der Tastatur
      * @param line -> String mit der Tastatureingabe
      * @return char -> char Version des erhaltenen Strings
      * @throws IOException
      */
 
     private char verarbeiteKundenEingabe(String line) throws IOException {
         // Eingabe bearbeiten:
         if (line.equals("l")) {
             //Für die Liste aller Artikel
             return 'l';
 
         } else if (line.equals("w")) {
             //Artikel in den Warenkorb packen
             return 'w';
 
         } else if (line.equals("e")) {
             //Artikel aus dem Warenkorb löschen
             return 'e';
 
         } else if (line.equals("a")) {
             //Artikel aus dem Warenkorb anzeigen lassen
             return 'a';
 
         } else if (line.equals("b")) {
             //Artikel aus dem Warenkorb bestellen
             return 'b';
 
         } else if (line.equals("o")) {
             //Ausloggen
             return 'o';
         } else if (line.equals("k")) {
             //Eigene Daten bearbeiten
             return 'k';
         }
 
         return 'o';
     }
 
     /**
      * Methode verarbeitet die Eingabe der Tastatur
      * @param kNr -> int Wert der Kundennummer, dadurch können sämtliche Aktionen dem eingeloggten Kunden zugewiesen werden
      * @throws IOException
      * @throws KundenNummerExistiertNichtException
      * @throws LeereEingabeException
      */
 
     private void KundenEingabe(int kNr) throws IOException, KundenNummerExistiertNichtException, LeereEingabeException {
         // Variable für Eingaben von der Konsole
         String input = "";
         do {
 
             try {
 
                 gibKundenMenueAus();
                 input = liesEingabe();
                 char var = verarbeiteKundenEingabe(input);
 
                 // Liste der Artikel ausgeben
                 if (var == 'l') {
 
 
                     try {
 
                         System.out.print("Wie soll die Ausgabe sortiert werden ?: > ");
                         System.out.print("1) nach Bezeichnung sortiert  > ");
                         System.out.print("2) nach Artikelnummer sortiert  > ");
                         String sort = liesEingabe();
                         int sortInt = Integer.parseInt(sort);
                         // nach Name sortiert
                         if (sortInt == 1) {
                             ArtikelNameComperator a = new ArtikelNameComperator();
                             Vector<Artikel> artList = eShopVerwaltung.gibAlleArtikel();
                             Collections.sort(artList, a);
                             System.out.println(artList);
                         // nach Artikelnummer sortiert
                         } else if (sortInt == 2) {
                             ArtikelNumberComperator b = new ArtikelNumberComperator();
                             Vector<Artikel> artList = eShopVerwaltung.gibAlleArtikel();
                             Collections.sort(artList, b);
                             System.out.println(artList);
                         } else {
                             System.out.println("Unbekannte Eingabe!");
                         }
 
 
                     } catch (NumberFormatException e) {
                         System.out.println("Unbekannte Eingabe!");
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     }
 
                     // Artikel in den Warenkorb legen
                 } else if (var == 'w') {
 
 
                     try {
 
                         System.out.println(eShopVerwaltung.gibAlleArtikel());
                         System.out.print("Artikelnummer: > ");
                         String aNr = liesEingabe();
                         int aNrInt = Integer.parseInt(aNr);
 
                         System.out.print("Menge: > ");
                         String menge = liesEingabe();
                         int mengeInt = Integer.parseInt(menge);
                         //holt den gewünschten Artikel aus der Hashmap
                         Artikel a = eShopVerwaltung.getArtikel(aNrInt);
                         if (a instanceof MassengutArtikel) {
                             //anlegen einer Massengutartikelkopie des echten Artikels für den Warenkorb, dadurch wird vermieden, dass
                             //sich verschiedene Bestellmengen überschreiben
                             MassengutArtikel mA = new MassengutArtikel(a.getName(), a.getBeschreibung(), a.getPreis(), ((MassengutArtikel) a).getPackungsgroesse());
                             mA.setBestand(a.getBestand());
                             mA.setNummer(a.getNummer());
                             eShopVerwaltung.setBestellteMenge(mengeInt, mA);
                             eShopVerwaltung.inWarenkorbLegen(mA, kNr);
                         } else {
                             //anlegen einer Artikelkopie des echten Artikels für den Warenkorb, dadurch wird vermieden, dass
                             //sich verschiedene Bestellmengen überschreiben
                             Artikel art = new Artikel(a.getName(), a.getBeschreibung(), a.getPreis());
                             art.setBestand(a.getBestand());
                             art.setNummer(a.getNummer());
                             eShopVerwaltung.setBestellteMenge(mengeInt, art);
                             eShopVerwaltung.inWarenkorbLegen(art, kNr);
                         }
 
                     } catch (NumberFormatException e) {
 
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
 
                     } catch (ArtikelExestiertNichtException aen) {
                         System.err.println(aen.getMessage());
 
                     } catch (ArtikelBestellteMengeNegativException abmn){
                         System.err.println(abmn.getMessage());
 
                     } catch (ArtikelBestandZuNiedrigException abzne) {
                         System.err.println(abzne.getMessage());
 
                     } catch (BestellteMengeEntsprichtNichtderPackungsgroesseException bmepe) {
                         System.err.println(bmepe.getMessage());
                     }
 
 
                     // Artikel aus dem Warenkorb löschen
                 } else if (var == 'e') {
 
                     try {
 
                         System.out.println(eShopVerwaltung.getKunde(kNr).getWarenkorb().values());
                         System.out.print("Artikelnummer: > ");
                         String aNr = liesEingabe();
                         int aNrInt = Integer.parseInt(aNr);
                         if (eShopVerwaltung.getKunde(kNr).istImWarenkorb(aNrInt)) {
                             Artikel a = eShopVerwaltung.getArtikel(aNrInt);
                             eShopVerwaltung.ausWarenkorbEntfernen(a, kNr);
                         }
                     } catch (NumberFormatException e) {
 
                     } catch (ArtikelExestiertNichtException aen) {
                         System.err.println(aen.getMessage());
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     }
 
                 // Zeigt den Warenkorb eines eingeloggten Kunden an
                 } else if (var == 'a') {
 
                     try {
 
                         Kunde kunde = eShopVerwaltung.getKunde(kNr);
                         System.out.println("Warenkorb von " + kunde.getBenutzername());
 
                         if (kunde.getWarenkorb().size() > 0) {
 
                             System.out.println(eShopVerwaltung.getKunde(kNr).getWarenkorb().values());
 
                         } else {
                             System.out.println("Ihr Warenkorb ist leer !");
                         }
 
 
                     } catch (NumberFormatException e) {
 
                     } catch (KundenNummerExistiertNichtException kne) {
                         System.err.println(kne.getMessage());
                     }
 
                 // Erstellt für den aktuellen Warenkorb eines Nutzers eine Rechnung
                 } else if (var == 'b') {
 
                     boolean bestandFehler = false;
 
                     try {
 
                         Kunde kunde = eShopVerwaltung.getKunde(kNr);
                         System.out.println("1");
                         if (kunde.getWarenkorb().size() > 0) {
                             //wenn sich Artikel im Warenkorb befinden prüfen ob die gewünschten Artikel gekauft werden können
                             Iterator iter = kunde.getWarenkorb().values().iterator();
                             while (iter.hasNext()) {
                                 Artikel a = (Artikel) iter.next();
                                 if (!(eShopVerwaltung.getArtikel(a.getNummer()).getBestand() >= a.getBestellteMenge())) {
                                     bestandFehler = true;
                                     System.out.println("2");
                                     System.out.println("Der Bestand des Artikels " + a.getName() + " ist geringer als die bestellte Mnege");
                                     System.out.println("Noch vorhandener Bestand: " + eShopVerwaltung.getArtikel(a.getNummer()).getBestand() + "Stueck !");
                                     System.out.println("Die von Ihnen bestellte Menge: " + a.getBestellteMenge());
                                 }
                             }
                             if (!bestandFehler) {
                                 Iterator iter2 = kunde.getWarenkorb().values().iterator();
                                 while(iter2.hasNext()) {
                                     Artikel a = (Artikel) iter2.next();
                                     Artikel puffer = eShopVerwaltung.getArtikel(a.getNummer());
                                     int neuerBestand = puffer.getBestand() - a.getBestellteMenge();
                                     eShopVerwaltung.setBestand(puffer.getNummer(), neuerBestand, kunde);
                                 }
                                 eShopVerwaltung.fuegeRechnungEin(kunde);
                                 System.out.println("" + eShopVerwaltung.letzteKundenrechnungAusgeben(kNr));
                                 //Warenkorb komplett leeren
                                 kunde.resetWarenkorb();
                             }
                         }
 
 
                     } catch (NumberFormatException e) {
 
                     } catch (KundenNummerExistiertNichtException kne) {
                         System.err.println(kne.getMessage());
                     } catch (ArtikelExestiertNichtException aen) {
                         System.err.println(aen.getMessage());
                     } catch (ArtikelBestandNegativException abn) {
                         System.err.println(abn.getMessage());
                     } catch (RechnungExestiertNichtException ren) {
                         System.err.println(ren.getMessage());
                     }
 
                 } else if (var == 'k') {
                     gibDatenMenueAus();
                     Kunde k = eShopVerwaltung.getKunde(kNr);
                     input = liesEingabe();
                     if (input.equals("v")) {
                         System.out.println("Ihr bisheriger Vorname " + eShopVerwaltung.getKunde(kNr).getVorname());
                         System.out.println("Bitte geben Sie ihren neuen Vornamen ein");
                         String vn = liesEingabe();
                         eShopVerwaltung.vornamenAendern(k, vn);
                     } else if (input.equals("n")) {
                         System.out.println("Ihr bisheriger Nachname " + eShopVerwaltung.getKunde(kNr).getNachname());
                         System.out.println("Bitte geben Sie ihren neuen Nachnamen ein");
                         String nn = liesEingabe();
                         eShopVerwaltung.nachnamenAendern(k, nn);
                     } else if (input.equals("p")) {
                         System.out.println("Sie möchten ihr Passwort ändern.");
                         System.out.println("Bitte geben sie ihr altes Passwort ein");
                         String ap = liesEingabe();
                         if (ap.equals(eShopVerwaltung.getKunde(kNr).getPasswort())) {
                             System.out.println("Bitte geben Sie ihr neues Wunschpasswort ein ");
                             String neuesP = liesEingabe();
                             System.out.println("Bitte wiederholen Sie ihr neues Passwort");
                             String kontrollP = liesEingabe();
                             eShopVerwaltung.passwortAendern(k, neuesP, kontrollP);
                         } else {
                             System.out.println("Das ist leider nicht ihr Passwort");
                         }
                     } else if (input.equals("e")) {
                         System.out.println("Ihre bisheriger Emailadresse " + eShopVerwaltung.getKunde(kNr).getEmail());
                         System.out.println("Bitte geben Sie ihre neue Emailadresse ein");
                         String e = liesEingabe();
                         eShopVerwaltung.emailAdresseAendern(k, e);
                     } else if (input.equals("t")) {
                         System.out.println("Ihre bisheriger Telefonnummer " + eShopVerwaltung.getKunde(kNr).getTelefon());
                         System.out.println("Bitte geben Sie ihre neue Telefonnummer ein");
                         String t = liesEingabe();
                         eShopVerwaltung.telefonnummerAendern(k, t);
                     } else if (input.equals("s")) {
                         System.out.println("Ihre bisheriger Straße " + eShopVerwaltung.getKunde(kNr).getAdresse().getStraße());
                         System.out.println("Bitte geben Sie ihre neue Straße ein");
                         String s = liesEingabe();
                         eShopVerwaltung.straßeAendern(k, s);
                     } else if (input.equals("w")) {
                         System.out.println("Ihr bisheriger Wohnort " + eShopVerwaltung.getKunde(kNr).getAdresse().getOrt());
                         System.out.println("Bitte geben Sie ihren neuen Wohnort ein");
                         String o = liesEingabe();
                         eShopVerwaltung.wohnortAendern(k, o);
                     } else if (input.equals("z")) {
                         System.out.println("Ihre bisherige Plz " + eShopVerwaltung.getKunde(kNr).getAdresse().getPlz());
                         System.out.println("Bitte geben Sie ihre neue Postleihzahl ein");
                         String plz = liesEingabe();
                         eShopVerwaltung.plzAendern(k, plz);
                     }
                 }
 
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (KundenNummerExistiertNichtException kne) {
                 System.err.println(kne.getMessage());
             } catch (LeereEingabeException le) {
                 System.err.println(le.getMessage());
             } catch (NeuesPasswortFehlerhaftException npf) {
                 System.err.println(npf.getMessage());
             }
 
         }
 
         while (!input.equals("o"));
 
 
     }
 
     /**
      * Methode verarbeitet die Eingabe der Tastatur
      * @param mNr -> int Wert der Mitarbeiternummer, dadurch können sämtliche Aktionen dem eingeloggten Mitarbeiter zugewiesen werden
      * @throws IOException
      * @throws LeereEingabeException
      */
 
     private void MitarbeiterEingabe(int mNr) throws IOException, LeereEingabeException {
         // Variable für Eingaben von der Konsole
         String input = "";
         do {
 
             try {
 
                 gibMitarbeiterMenueAus();
                 input = liesEingabe();
 
                 // Liste der Artikel ausgeben
                 if (input.equals("mh")) {
                     boolean ok = false;
 
                     try {
                         neuenMitarbeiterAnlegen();
 
                     } catch (NumberFormatException e) {
 
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     }
 
                 // Mitarbeiter loeschen
                 } else if (input.equals("ml")) {
                     System.out.println("Bitte geben Sie die Nummer des Mitarbeiters an, der gelöscht werden soll.");
                     String mitNr = liesEingabe();
                     int mNrInt = Integer.parseInt(mitNr);
                     try {
                         eShopVerwaltung.loescheMitarbeiter(mNrInt, eShopVerwaltung.getMitarbeiter(mNr));
                     } catch (MitarbeiterExistiertNichtException men) {
                         System.err.println(men.getMessage());
                     }
                     System.out.println("Der Mitarbeiter wurde erfolgreich gelöscht.");
 
                 // Liste aller Mitarbeiter ausgeben
                 } else if (input.equals("ma")) {
                     System.out.println(eShopVerwaltung.gibAlleMitarbeiter());
                 } else if (input.equals("mb")) {
                     gibDatenMenueAus();
                     try {
                         input = liesEingabe();
                         Mitarbeiter m = eShopVerwaltung.getMitarbeiter(mNr);
                         if (input.equals("v")) {
                             System.out.println("Ihr bisheriger Vorname " + eShopVerwaltung.getMitarbeiter(mNr).getVorname());
                             System.out.println("Bitte geben Sie ihren neuen Vornamen ein: ");
                             String vn = liesEingabe();
                             eShopVerwaltung.vornamenAendern(m, vn);
                         } else if (input.equals("n")) {
                             System.out.println("Ihr bisheriger Nachname " + eShopVerwaltung.getMitarbeiter(mNr).getNachname());
                             System.out.println("Bitte geben Sie ihren neuen Nachnamen ein: ");
                             String nn = liesEingabe();
                             eShopVerwaltung.nachnamenAendern(m, nn);
                         } else if (input.equals("p")) {
                             System.out.println("Sie möchten ihr Passwort ändern.");
                             System.out.println("Bitte geben sie ihr altes Passwort ein");
                             String ap = liesEingabe();
                             if (ap.equals(eShopVerwaltung.getMitarbeiter(mNr).getPasswort())) {
                                 System.out.println("Bitte geben Sie ihr neues Wunschpasswort ein ");
                                 String neuesP = liesEingabe();
                                 System.out.println("Bitte wiederholen Sie ihr neues Passwort");
                                 String kontrollP = liesEingabe();
                                 eShopVerwaltung.passwortAendern(m, neuesP, kontrollP);
                             } else {
                                 System.out.println("Das ist leider nicht ihr Passwort");
                             }
                         } else if (input.equals("e")) {
                             System.out.println("Ihre bisheriger Emailadresse " + eShopVerwaltung.getMitarbeiter(mNr).getEmail());
                             System.out.println("Bitte geben Sie ihre neue Emailadresse ein: ");
                             String e = liesEingabe();
                             eShopVerwaltung.emailAdresseAendern(m, e);
                         } else if (input.equals("t")) {
                             System.out.println("Ihre bisheriger Telefonnummer " + eShopVerwaltung.getMitarbeiter(mNr).getTelefon());
                             System.out.println("Bitte geben Sie ihre neue Telefonnummer ein: ");
                             String t = liesEingabe();
                             eShopVerwaltung.telefonnummerAendern(m, t);
                         } else if (input.equals("s")) {
                             System.out.println("Ihre bisheriger Straße " + eShopVerwaltung.getMitarbeiter(mNr).getAdresse().getStraße());
                             System.out.println("Bitte geben Sie ihre neue Straße ein: ");
                             String s = liesEingabe();
                             eShopVerwaltung.straßeAendern(m, s);
                         } else if (input.equals("w")) {
                             System.out.println("Ihr bisheriger Wohnort " + eShopVerwaltung.getMitarbeiter(mNr).getAdresse().getOrt());
                             System.out.println("Bitte geben Sie ihren neuen Wohnort an: ");
                             String o = liesEingabe();
                             eShopVerwaltung.wohnortAendern(m, o);
                         } else if (input.equals("z")) {
                             System.out.println("Ihre bisherige Plz " + eShopVerwaltung.getMitarbeiter(mNr).getAdresse().getPlz());
                             System.out.println("Bitte geben Sie ihre neue Postleihzahl ein: ");
                             String plz = liesEingabe();
                             eShopVerwaltung.plzAendern(m, plz);
                         }
                     } catch (MitarbeiterExistiertNichtException men) {
                         System.err.println(men.getMessage());
                     } catch (NeuesPasswortFehlerhaftException npf) {
                         System.err.println(npf.getMessage());
                     }
                 // Kunden hinzufuegen
                 } else if (input.equals("kh")) {
                     neuenKundenAnlegen();
 
                 // Kunden loeschen
                 } else if (input.equals("kl")) {
                     try {
                         System.out.println(eShopVerwaltung.gibAlleKunden());
                         System.out.print("Kundennummer: > ");
                         String kNr = liesEingabe();
                         int kNrInt = Integer.parseInt(kNr);
                         eShopVerwaltung.loescheKunde(kNrInt, eShopVerwaltung.getMitarbeiter(mNr));
                     } catch (NumberFormatException e) {
 
                     } catch (KundenNummerExistiertNichtException knen) {
                         System.err.println(knen.getMessage());
                     } catch (MitarbeiterExistiertNichtException me){
                         System.err.println(me.getMessage());
                     }
 
                 // alle Kunden ausgeben
                 } else if (input.equals("ka")) {
 
                     System.out.println(eShopVerwaltung.gibAlleKunden());
 
                 // Artikel hinzufuegen
                 } else if (input.equals("ah")) {
 
                     try {
                         System.out.print("Artikel Name > ");
                         String aName = liesEingabe();
                         System.out.print("Artikel Beschreibung > ");
                         String aBeschreibung = liesEingabe();
                         System.out.print("Artikel Preis  > ");
                         String aPreisEingabe = liesEingabe();
                         double aPreis = Double.parseDouble(aPreisEingabe);
 
                         eShopVerwaltung.fuegeArtikelEin(aName, aBeschreibung, aPreis, eShopVerwaltung.getMitarbeiter(mNr));
 
                     } catch (NumberFormatException e) {
                     } catch (ArtikelExestierBereitsException aeb) {
                         System.err.println(aeb.getMessage());
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     } catch (MitarbeiterExistiertNichtException me){
                         System.err.println(me.getMessage());
                     }
                 // Massengutartikel anlegen
                 } else if (input.equals("am")) {
                     try {
                         System.out.println("Artikelname: ");
                         String aName = liesEingabe();
                         System.out.print("Artikel Beschreibung > ");
                         String aBeschreibung = liesEingabe();
                         System.out.print("Artikel Preis  > ");
                         String aPreisEingabe = liesEingabe();
                         double aPreis = Double.parseDouble(aPreisEingabe);
                         System.out.println("Zulaessige Packungsgroesse :");
                         String aPackungEingabe = liesEingabe();
                         int aPackung = Integer.parseInt(aPackungEingabe);
                         eShopVerwaltung.fuegeMassengutArtikelEin(aName, aBeschreibung, aPreis, aPackung, eShopVerwaltung.getMitarbeiter(mNr));
                     } catch(NumberFormatException e) {
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     } catch (MitarbeiterExistiertNichtException men) {
                         System.err.println(men.getMessage());
                     } catch (ArtikelExestierBereitsException aeb) {
                         System.err.println(aeb.getMessage());
                     }
                 //Artikelbestand ändern
                 } else if (input.equals("ab")) {
 
                     try {
                         System.out.println(eShopVerwaltung.gibAlleArtikel());
                         System.out.print("Artikelnummer: > ");
                         String aNr = liesEingabe();
                         int aNrInt = Integer.parseInt(aNr);
                         System.out.print("Bestand > ");
                         String aBestand = liesEingabe();
                         int aBestandInt = Integer.parseInt(aBestand);
                         eShopVerwaltung.setBestand(aNrInt, aBestandInt, eShopVerwaltung.getMitarbeiter(mNr));
 
                     } catch (NumberFormatException e) {
                     } catch (MitarbeiterExistiertNichtException men) {
                         System.err.println(men.getMessage());
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     } catch (ArtikelExestiertNichtException aen) {
                         System.err.println(aen.getMessage());
                     } catch (ArtikelBestandNegativException abn) {
                         System.err.println(abn.getMessage());
                     }
 
                 // Artikel loeschen
                 } else if (input.equals("al")) {
                     try {
                         System.out.println(eShopVerwaltung.gibAlleArtikel());
                         System.out.print("Artikelnummer: > ");
                         String aNr = liesEingabe();
                         int aNrInt = Integer.parseInt(aNr);
                         eShopVerwaltung.loescheArtikel(aNrInt, eShopVerwaltung.getMitarbeiter(mNr));
                     } catch (NumberFormatException e) {
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     } catch (ArtikelExestiertNichtException ane) {
                         System.err.println(ane.getMessage());
                     } catch (MitarbeiterExistiertNichtException me){
                         System.err.println(me.getMessage());
                     }
 
                 // Alle Artikel ausgeben
                 } else if (input.equals("aa")) {
 
                     try {
 
                         System.out.print("Wie soll die Ausgabe sortiert werden ?: > ");
                         System.out.print("1) nach Bezeichnung sortiert  > ");
                         System.out.print("2) nach Artikelnummer sortiert  > ");
                         String sort = liesEingabe();
                         int sortInt = Integer.parseInt(sort);
                         // nach Artikelnummer sortiert
                         if (sortInt == 1) {
                             ArtikelNameComperator a = new ArtikelNameComperator();
                             Vector<Artikel> artList = eShopVerwaltung.gibAlleArtikel();
                             Collections.sort(artList, a);
                             System.out.println(artList);
                         } else if (sortInt == 2) {
                             ArtikelNumberComperator b = new ArtikelNumberComperator();
                             Vector<Artikel> artList = eShopVerwaltung.gibAlleArtikel();
                             Collections.sort(artList, b);
                             System.out.println(artList);
                         } else {
                             System.out.println("Unbekannte Eingabe!");
                         }
 
 
                     } catch (NumberFormatException e) {
                         System.out.println("Sie müssen eine Zahl eingeben!");
 
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     }
 
                 // Artikel suchen
                 } else if (input.equals("af")) {
 
                     System.out.print("Artikelname:  > ");
                     String titel = liesEingabe();
                     System.out.println(eShopVerwaltung.sucheNachName(titel));
                 //Rechnung löschen
                 } else if (input.equals("rl")) {
                     try {
                         System.out.println(eShopVerwaltung.gibAlleRechnungen());
                         System.out.print("Rechnungsnummer: > ");
                         String rNr = liesEingabe();
                         int rNrInt = Integer.parseInt(rNr);
 
                         eShopVerwaltung.loescheRechnung(eShopVerwaltung.sucheNachRechnungsnummer(rNrInt));
 
                     } catch (NumberFormatException e) {
 
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     } catch (RechnungExestiertNichtException ren) {
                         System.err.println(ren.getMessage());
                     }
                 //Rechnung suchen
                 } else if (input.equals("ra")) {
                     try {
                         System.out.println(eShopVerwaltung.gibAlleRechnungen());
                         System.out.print("Rechnungsnummer: > ");
                         String rNr = liesEingabe();
                         int rNrInt = Integer.parseInt(rNr);
                         System.out.println(eShopVerwaltung.sucheNachRechnungsnummer(rNrInt));
 
                     } catch (NumberFormatException e) {
 
                     } catch (LeereEingabeException le) {
                         System.err.println(le.getMessage());
                     } catch (RechnungExestiertNichtException ren) {
                         System.err.println(ren.getMessage());
                     }
                 //Artikellogmenue
                 } else if (input.equals("la")){
                     try {
                         System.out.println("1) gesamten ArtikelLog anzeigen  > ");
                         System.out.println("2) gesamten ArtikelLog ab einem bestimmten Tag anzeigen > ");
                         System.out.println("3) gesamten Log eines bestimmten Artikels anzeigen > ");
                         System.out.println("4) Log eines bestimmten Artikels ab einem bestimmten Tag anzeigen > ");
                         String anzeige = liesEingabe();
                         int anzeigeInt = Integer.parseInt(anzeige);
                         if (anzeigeInt == 1){
                             System.out.println(this.eShopVerwaltung.printArtikelLog());
                         } else if(anzeigeInt == 2){
                             System.out.print("Wie viele Tage soll der Log zurückliegen?: > ");
                             String tage = liesEingabe();
                             int days = Integer.parseInt(tage);
                             for(int i = 0; i < this.eShopVerwaltung.printArtikelLog(days).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printArtikelLog(days).elementAt(i));
                             }
                         } else if(anzeigeInt == 3){
                             System.out.println(this.eShopVerwaltung.gibAlleArtikel());
                             System.out.print("Artikelnummer: >");
                             String aNr = liesEingabe();
                             for(int i = 0; i < this.eShopVerwaltung.printArtikelLog(aNr).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printArtikelLog(aNr).elementAt(i));
                             }
                         } else if(anzeigeInt == 4){
                             System.out.println(this.eShopVerwaltung.gibAlleArtikel());
                             System.out.print("Artikelnummer: >");
                             String aNr = liesEingabe();
                             System.out.print("Wie viele Tage soll der Log zurückliegen?: > ");
                             String tage = liesEingabe();
                             int days = Integer.parseInt(tage);
                             for(int i = 0; i < this.eShopVerwaltung.printArtikelLog(days, aNr).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printArtikelLog(days, aNr).elementAt(i));
                             }
                         } else {
                             System.out.println("Unbekannte Eingabe!");
                         }
                     } catch (ParseException pe){
                         // keine Fehlerbehandlung, da diese nicht auftreten kann
                     } catch (NumberFormatException ne){
                         System.err.println("Sie müssen eine Zahl eingeben!");
                     } catch (KennNummerExistiertNichtException ke){
                         System.err.println(ke.getMessage());
                     } catch (KeineEintraegeVorhandenException kve){
                         System.err.println(kve.getMessage());
                     }
                 //Kundenlogmenue
                 } else if (input.equals("lk")){
                     try {
                         System.out.println("1) gesamten KundenLog anzeigen  > ");
                         System.out.println("2) gesamten KundenLog ab einem bestimmten Tag anzeigen > ");
                         System.out.println("3) gesamten Log eines bestimmten Kunden anzeigen > ");
                         System.out.println("4) Log eines bestimmten Kunden ab einem bestimmten Tag anzeigen  > ");
                         String anzeige = liesEingabe();
                         int anzeigeInt = Integer.parseInt(anzeige);
                         if (anzeigeInt == 1){
                             System.out.println(this.eShopVerwaltung.printKundenLog());
                         } else if(anzeigeInt == 2){
                             System.out.print("Wie viele Tage soll der Log zurückliegen?: > ");
                             String tage = liesEingabe();
                             int days = Integer.parseInt(tage);
                             for(int i = 0; i < this.eShopVerwaltung.printKundenLog(days).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printKundenLog(days).elementAt(i));
                             }
                         } else if(anzeigeInt == 3){
                             System.out.println(this.eShopVerwaltung.gibAlleKunden());
                             System.out.print("Kundennummer: >");
                             String kNr = liesEingabe();
                             for(int i = 0; i < this.eShopVerwaltung.printKundenLog(kNr).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printKundenLog(kNr).elementAt(i));
                             }
                         } else if(anzeigeInt == 4) {
                             System.out.println(this.eShopVerwaltung.gibAlleKunden());
                             System.out.print("Kundennummer: >");
                             String kNr = liesEingabe();
                             System.out.print("Wie viele Tage soll der Log zurückliegen?: > ");
                             String tage = liesEingabe();
                             int days = Integer.parseInt(tage);
                             for(int i = 0; i < this.eShopVerwaltung.printKundenLog(days, kNr).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printKundenLog(days, kNr).elementAt(i));
                             }
                         } else {
                             System.out.println("Unbekannte Eingabe!");
                         }
                     } catch (ParseException pe){
                         // keine Fehlerbehandlung, da diese nicht auftreten kann
                     } catch (NumberFormatException ne){
                         System.err.println("Sie müssen eine Zahl eingeben!");
                     } catch (KennNummerExistiertNichtException ke){
                         System.err.println(ke.getMessage());
                     } catch (KeineEintraegeVorhandenException kve){
                         System.err.println(kve.getMessage());
                     }
                 //Mitarbeiterlogmenue
                 } else if (input.equals("lm")){
                     try {
                         System.out.println("1) gesamten MitarbeiterLog anzeigen  > ");
                         System.out.println("2) gesamten MitarbeiterLog ab einem bestimmten Tag anzeigen > ");
                         System.out.println("3) gesamten Log eines bestimmten Mitarbeiters anzeigen > ");
                         System.out.println("4) Log eines Mitarbeiters ab einem bestimmten Tag anzeigen  > ");
                         String anzeige = liesEingabe();
                         int anzeigeInt = Integer.parseInt(anzeige);
                         if (anzeigeInt == 1){
                             System.out.println(this.eShopVerwaltung.printMitarbeiterLog());
                         } else if(anzeigeInt == 2){
                             System.out.print("Wie viele Tage soll der Log zurückliegen?: > ");
                             String tage = liesEingabe();
                             int days = Integer.parseInt(tage);
                             for(int i = 0; i < this.eShopVerwaltung.printMitarbeiterLog(days).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printMitarbeiterLog(days).elementAt(i));
                             }
                         } else if(anzeigeInt == 3){
                             System.out.println(this.eShopVerwaltung.gibAlleMitarbeiter());
                             System.out.print("Mitarbeiternummer: >");
                             String mitarNr = liesEingabe();
                             for(int i = 0; i < this.eShopVerwaltung.printMitarbeiterLog(mitarNr).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printMitarbeiterLog(mitarNr).elementAt(i));
                             }
                         } else if(anzeigeInt == 4){
                             System.out.println(this.eShopVerwaltung.gibAlleMitarbeiter());
                             System.out.print("Mitarbeiternummer: >");
                             String mitarNr = liesEingabe();
                             System.out.print("Wie viele Tage soll der Log zurückliegen?: > ");
                             String tage = liesEingabe();
                             int days = Integer.parseInt(tage);
                             for(int i = 0; i < this.eShopVerwaltung.printMitarbeiterLog(days, mitarNr).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printMitarbeiterLog(days, mitarNr).elementAt(i));
                             }
                         } else {
                             System.out.println("Unbekannte Eingabe!");
                         }
                     } catch (ParseException pe){
                         // keine Fehlerbehandlung, da diese nicht auftreten kann
                     } catch (NumberFormatException ne){
                         System.err.println("Sie müssen eine Zahl eingeben!");
                     } catch (KennNummerExistiertNichtException ke){
                         System.err.println(ke.getMessage());
                     } catch (KeineEintraegeVorhandenException kve){
                         System.err.println(kve.getMessage());
                     }
                 //Rechnungslogmenue
                 } else if (input.equals("lr")){
                     try {
                         System.out.println("1) gesamten RechnungsLog anzeigen  > ");
                         System.out.println("2) gesamten RechnungsLog ab einem bestimmten Tag anzeigen > ");
                         System.out.println("3) gesamten Log einer bestimmten Rechnung anzeigen > ");
                         System.out.println("4) Log einer bestimmten Rechnung ab einem bestimmten Tag anzeigen  > ");
                         String anzeige = liesEingabe();
                         int anzeigeInt = Integer.parseInt(anzeige);
                         if (anzeigeInt == 1){
                             System.out.println(this.eShopVerwaltung.printRechnungsLog());
                         } else if(anzeigeInt == 2){
                             System.out.print("Wie viele Tage soll der Log zurückliegen?: > ");
                             String tage = liesEingabe();
                             int days = Integer.parseInt(tage);
                             for(int i = 0; i < this.eShopVerwaltung.printRechnungsLog(days).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printRechnungsLog(days).elementAt(i));
                             }
                         } else if(anzeigeInt == 3){
                             System.out.println(this.eShopVerwaltung.gibAlleRechnungen());
                             System.out.print("Rechnungsnummer: >");
                             String rNr = liesEingabe();
                             for(int i = 0; i < this.eShopVerwaltung.printRechnungsLog(rNr).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printRechnungsLog(rNr).elementAt(i));
                             }
                         } else if(anzeigeInt == 4){
                             System.out.println(this.eShopVerwaltung.gibAlleRechnungen());
                             System.out.print("Rechnungsnummer: >");
                             String rNr = liesEingabe();
                             System.out.print("Wie viele Tage soll der Log zurückliegen?: > ");
                             String tage = liesEingabe();
                             int days = Integer.parseInt(tage);
                             for(int i = 0; i < this.eShopVerwaltung.printRechnungsLog(days, rNr).size(); i++){
                                 System.out.println(this.eShopVerwaltung.printRechnungsLog(days, rNr).elementAt(i));
                             }
                         } else {
                             System.out.println("Unbekannte Eingabe!");
                         }
                     } catch (ParseException pe){
                         // keine Fehlerbehandlung, da diese nicht auftreten kann
                     } catch (NumberFormatException ne){
                         System.err.println("Sie müssen eine Zahl eingeben!");
                     } catch (KennNummerExistiertNichtException ke){
                         System.err.println(ke.getMessage());
                     }  catch (KeineEintraegeVorhandenException kve){
                         System.err.println(kve.getMessage());
                     }
                 }
 
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (LeereEingabeException le) {
                 System.err.println(le.getMessage());
             }
 
         } while (!input.equals("o"));
 
 
     }
 
     /**
      * Methode zum anlegen neuer Kunden
      * @throws LeereEingabeException
      */
 
     private void neuenKundenAnlegen() throws IOException, LeereEingabeException {
         System.out.println("Willkommen bei der Kundenregistrierung.");
         System.out.println("Bitte geben sie ihre Daten ein.");
         try {
             System.out.print("Vorname > ");
             String vorname = liesEingabe();
             System.out.print("Nachname > ");
             String nachname = liesEingabe();
             System.out.print("Benutzername > ");
             String benutzername = liesEingabe();
             System.out.print("Passwort  > ");
             String passwort = liesEingabe();
             System.out.print("E-mail > ");
             String email = liesEingabe();
             System.out.print("Telefon > ");
             String telefon = liesEingabe();
             System.out.print("Straße > ");
             String straße = liesEingabe();
             System.out.print("PLZ > ");
             String plz = liesEingabe();
             System.out.print("Ort > ");
             String ort = liesEingabe();
             Adresse adresse = new Adresse(vorname, nachname, straße, plz, ort);
             eShopVerwaltung.fuegeKundeEin(vorname, nachname, benutzername, passwort, email, telefon, adresse);
 
         } catch (NumberFormatException e) {
             System.out.println(e);
         } catch (BenutzernameExistiertBereitsException beb) {
             System.err.println(beb.getMessage());
         }
     }
 
     /**
      * Methode zum Anlegen neuer Mitarbeiter
      * @throws IOException
      * @throws LeereEingabeException
      */
     private void neuenMitarbeiterAnlegen() throws IOException, LeereEingabeException {
 
         try {
 
             System.out.print("Vorname > ");
             String vorname = liesEingabe();
             System.out.print("Nachname > ");
             String nachname = liesEingabe();
             System.out.print("Benutzername > ");
             String benutzername = liesEingabe();
             System.out.print("Passwort  > ");
             String passwort = liesEingabe();
             System.out.print("E-mail > ");
             String email = liesEingabe();
             System.out.print("Telefonnummer > ");
             String telefon = liesEingabe();
             System.out.print("Straße > ");
             String straße = liesEingabe();
             System.out.print("PLZ > ");
             String plz = liesEingabe();
             System.out.print("Ort > ");
             String ort = liesEingabe();
             Adresse adresse = new Adresse(vorname, nachname, straße, plz, ort);
             eShopVerwaltung.fuegeMitarbeiterEin(vorname, nachname, benutzername, passwort, email, telefon, adresse);
 
         } catch (MitarbeiterExistiertBereitsException meb) {
             System.err.println(meb.getMessage());
         }
     }
 
     private void run() {
 
         // Variable für Eingaben von der Konsole
         String input = "";
 
         //Hauptschleife
         do {
 
             try {
 
                 gibMainMenue();
                 input = liesEingabe();
                 char var = verarbeiteMainEingabe(input);
 
                 if (var == 'l') {
                     //Loginmenue
 
                     try {
                         verarbeiteLogin();
 
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
 
 
                 } else if (var == 'k') {
                     // Sichern des Datenstandy für Kunden, Artikel, Mitarbeiter, Rechnung
                     try {
 
                         neuenKundenAnlegen();
 
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
 
 
                     }
                 } else if (var == 'm') {
                     // Sichern des Datenstandy für Kunden, Artikel, Mitarbeiter, Rechnung
                     try {
 
                         neuenMitarbeiterAnlegen();
 
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
 
                     }
 
                 } else if (var == 's') {
                     // Sichern des Datenstandy für Kunden, Artikel, Mitarbeiter, Rechnung
                     try {
                         eShopVerwaltung.schreibeKunden();
                         eShopVerwaltung.schreibeArtikel();
                         eShopVerwaltung.schreibeMitarbeiter();
                         eShopVerwaltung.schreibeRechung();
 
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
 
                     }
                 }
 
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (LeereEingabeException le) {
                 System.err.println(le.getMessage());
             }
 
         } while (!input.equals("q"));
 
     }
 
 
 // Main
 
 
     public static void main(String[] args) throws ClassNotFoundException {
 
         eshopClientCUI cui;
         try {
             cui = new eshopClientCUI("EShop");
             cui.run();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
 
 }
 
