 package de.philinko.bundesliga.manager.business;
 
 import de.philinko.bundesliga.dto.AufstellungDTO;
 import de.philinko.bundesliga.manager.business.api.AufstellungService;
 import de.philinko.bundesliga.manager.mappings.Aufstellung;
 import de.philinko.bundesliga.manager.mappings.Kontrahent;
 import de.philinko.bundesliga.manager.mappings.Spieler;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author philippe
  */
 public class AufstellungController {
 
     private AufstellungService service;
 
     public AufstellungController() {
         service = new AufstellungServiceImpl();
     }
 
     public void aufstellungEintragen(int spieltag, Spieler[] spielerListe, Kontrahent mitspieler) {
         checkSpieltag(spieltag);
         if (spielerListe.length != 11) {
             throw new IllegalArgumentException("Eine Aufstellung muss 11 Spieler enthalten");
         }
         byte[] positionsZaehler = new byte[4];
         positionsZaehler[0] = 0;
         positionsZaehler[1] = 0;
         positionsZaehler[2] = 0;
         positionsZaehler[3] = 0;
         Aufstellung[] aufstellungen = spielerInAufstellungEintragen(spielerListe, spieltag, mitspieler, positionsZaehler);
         anzahlProPositionPruefen(positionsZaehler);
         service.aufstellungenSpeichern(spieltag, aufstellungen, mitspieler);
 
     }
 
     public List<AufstellungDTO> spielerListeLaden(int spieltag, Kontrahent mitspieler) {
         Spieler[] spielerListe = service.aktuelleSpieler(spieltag, mitspieler);
         List<AufstellungDTO> result = new ArrayList<AufstellungDTO>(spielerListe.length);
         Set<Aufstellung> aufstellungen = new HashSet<Aufstellung>();
         aufstellungen.addAll(Arrays.asList(service.aufstellungAbfragen(spieltag, mitspieler)));
         for (Spieler spieler : spielerListe) {
             AufstellungDTO toInsert = new AufstellungDTO(spieltag, spieler.getName(), spieler.getPosition(), "", false);
             toInsert.setVerein(service.vereinVonSpieler(spieler));
             if (aufstellungen.contains(new Aufstellung(spieltag, spieler, mitspieler, 0))) {
                 toInsert.setEingesetzt(true);
             }
             result.add(toInsert);
         }
         return result;
     }
 
     public void spielerAendern(int spieltag, Spieler spieler, int posInAufstellung, Kontrahent mitspieler) {
         checkSpieltag(spieltag);
         Kontrahent aktuellerBesitzer = service.aktuellerBesitzer(spieltag, spieler);
         if (aktuellerBesitzer != mitspieler) {
             throw new IllegalArgumentException("Spieler " + spieler + " gehört " + aktuellerBesitzer + " sollte aber " + mitspieler + " gehören");
         }
         Aufstellung[] bisherigeAufstellung = service.aufstellungAbfragen(spieltag, mitspieler);
         for (Aufstellung aufstellung : bisherigeAufstellung) {
             if (aufstellung.getSpieler().equals(spieler) && aufstellung.getNum() != posInAufstellung) {
                 throw new IllegalArgumentException("Spieler " + spieler + " bereits eingesetzt");
             }
         }
         service.aufstellungAktualisieren(spieltag, spieler, mitspieler, posInAufstellung);
     }
 
     private void checkSpieltag(int spieltag) throws IllegalArgumentException {
         if (spieltag < 1 || spieltag > 34) {
             throw new IllegalArgumentException("Spieltag muss zwischen 1 und 34 liegen");
         }
     }
 
     private void anzahlProPositionPruefen(byte[] positionsZaehler) throws IllegalArgumentException {
         if (positionsZaehler[0] != 1) {
             throw new IllegalArgumentException("Es muss genau ein Torwart in der Aufstellung sein, es sind aber: " + positionsZaehler[0]);
         }
         if (positionsZaehler[1] != 4) {
             throw new IllegalArgumentException("Es müssen genau 4 Abwehrspieler in der Aufstellung sein, es sind aber: " + positionsZaehler[1]);
         }
         if (Math.abs(positionsZaehler[2] - 4) > 1) {
             throw new IllegalArgumentException("Es müssen 3-5 Mittelfeldspieler in der Aufstellung sein, es sind aber: " + positionsZaehler[2]);
         }
         if (Math.abs(positionsZaehler[3] - 2) > 1) {
             throw new IllegalArgumentException("Es müssen 1-3 Stürmer in der Aufstellung sein, es sind aber: " + positionsZaehler[3]);
         }
     }
 
     private Aufstellung[] spielerInAufstellungEintragen(Spieler[] spielerListe, int spieltag, Kontrahent mitspieler, byte[] positionsZaehler) throws IllegalArgumentException {
         Aufstellung[] aufstellungen = new Aufstellung[spielerListe.length];
         for (int i = 0; i < spielerListe.length; ++i) {
             Spieler spieler = spielerListe[i];
             aufstellungen[i] = new Aufstellung(spieltag, spieler, mitspieler, i);
             switch (spieler.getPosition()) {
                 case ABWEHR:
                     positionsZaehler[1]++;
                     break;
                 case MITTELFELD:
                     positionsZaehler[2]++;
                     break;
                 case ANGRIFF:
                     positionsZaehler[3]++;
                     break;
                 case TOR:
                     positionsZaehler[0]++;
                     break;
             }
             Kontrahent aktuellerBesitzer = service.aktuellerBesitzer(spieltag, spieler);
             if (!mitspieler.equals(aktuellerBesitzer)) {
                 throw new IllegalArgumentException("Spieler " + spieler + " gehört " + aktuellerBesitzer + " sollte aber " + mitspieler + " gehören");
             }
         }
         return aufstellungen;
     }
 
     public int aktuellerSpieltag() {
         return 1;
     }
 
     public List<Kontrahent> getKontrahenten() {
         return service.mitspielerListe();
     }
 }
