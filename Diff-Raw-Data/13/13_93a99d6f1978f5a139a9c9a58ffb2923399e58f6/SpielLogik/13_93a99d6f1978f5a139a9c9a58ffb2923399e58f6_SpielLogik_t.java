 // %3696642882:de.hattrickorganizer.logik%
 /*
  * SpielLogik.java
  *
  * Created on 17. Januar 2002, 14:53
  */
 package de.hattrickorganizer.logik;
 
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.model.ServerSpieler;
 import de.hattrickorganizer.model.ServerTeam;
 import de.hattrickorganizer.model.Spielbericht;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author tommy
  * @version
  */
 public class SpielLogik {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     /** gibt den Zahlenraum für die Chancenberechung an */
     public static final int CHANCEN_ZUFALLS_RAUM = 1250;
 
     /**
      * Faktor durch den die Anzahl der Abwehr Spieler dividiert wird um endgültige
      * Offensiv/Defensiv Kraft zu bestimmen
      */
     public static final float ABWEHR_MANIPULATOR = 1.7f;
 
     /**
      * Faktor durch den die Anzahl der Stürmer Spieler dividiert wird um endgültige
      * Offensiv/Defensiv Kraft zu bestimmen
      */
     public static final float STURM_MANIPULATOR = 1.3f;
 
     //SpielLogik
 
     /** TODO Missing Parameter Documentation */
     public static final byte AVG_TORCHANCEN = 6;
 
     /** TODO Missing Parameter Documentation */
     public static final byte OB_BONUS = 1;
 
     /** TODO Missing Parameter Documentation */
     public static final float ESB_OBERGRENZE = 3.0f;
 
     /** TODO Missing Parameter Documentation */
     public static final float ESB_UNTERGRENZE = 0.33f;
 
     /** TODO Missing Parameter Documentation */
     public static final byte ESB_STEUERUNG = 58;
 
     /** TODO Missing Parameter Documentation */
     public static final byte ESB_FAKTOR_STURM = 3;
 
     /** TODO Missing Parameter Documentation */
     public static final byte ESB_FAKTOR_ABWEHR = 4;
 
     /** TODO Missing Parameter Documentation */
     public static final byte ESB_OFFENSIVGRENZE = 57;
 
     /** TODO Missing Parameter Documentation */
     public static final byte ESB_OFFENSIVANPASSUNG = 20;
 
     //TorArten
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_FELDTOR_STARKER_FUSS = 99;
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_EIGENTOR = 100;
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_FELDTOR_SCHWACHER_FUSS = 98;
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_FELDTOR_PER_KOPF = 97;
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_TOR_NACH_FREISTOSS = 96;
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_DIREKTER_FREISTOSS = 95;
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_TOR_NACH_ECKE = 94;
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_HANDELFMETER = 93;
 
     /** TODO Missing Parameter Documentation */
     public static final byte TA_FOULELFMETER = 92;
 
     /** Normale Häufigkeit */
     public static final byte TA_NORMAL = 0;
 
     /** Kommt selten Vor */
     public static final byte TA_SELTEN = 1;
 
     //Weitere KEYS
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_AUSWECHSLUNG = 40;
 
     //Vortext
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_VORTEXT = 80;
 
     //Halbzeit
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_HALBZEIT = 79;
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_SPIELENDE = 78;
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_NACHSPIELZEIT = 77;
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_VERLAENGERUNG = 76;
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_ELFMETERSCHIESSEN = 75;
 
     //Fans
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_FANGESANG = 60;
 
     /** TODO Missing Parameter Documentation */
     public static final byte FG_ANFEUERUNG = 0;
 
     /** TODO Missing Parameter Documentation */
     public static final byte FG_GEGNER = 1;
 
     /** TODO Missing Parameter Documentation */
     public static final byte FG_SONSTIGES = 2;
 
     /** TODO Missing Parameter Documentation */
     public static final byte FG_SCHIRI = 3;
 
     //Karten
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_ROTEKARTE = 39;
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_GELBEKARTE = 38;
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_GELBROTEKARTE = 37;
 
     /** TODO Missing Parameter Documentation */
     public static final byte GFK_MECKERN = 0;
 
     /** TODO Missing Parameter Documentation */
     public static final byte GFK_UNSPORTLICHES_VERHALTEN = 1;
 
     /** TODO Missing Parameter Documentation */
     public static final byte GFK_FOUL = 2;
 
     /** TODO Missing Parameter Documentation */
     public static final byte GFK_ABSICHTLICHES_HANDSPIEL = 3;
 
     //Verletzungen
 
     /** TODO Missing Parameter Documentation */
     public static final byte KEY_VERLETZUNG = 50;
 
     /** TODO Missing Parameter Documentation */
     public static final byte VA_BAENDERVERLETZUNG = 0;
 
     /** TODO Missing Parameter Documentation */
     public static final byte VA_MUSKELVERLETZUNG = 1;
 
     /** TODO Missing Parameter Documentation */
     public static final byte VA_PRELLUNG = 2;
 
     /** TODO Missing Parameter Documentation */
     public static final byte VA_KNOCHENBUCH = 3;
 
     /** TODO Missing Parameter Documentation */
     public static final byte VA_GELENKSCHADEN = 4;
 
     /** TODO Missing Parameter Documentation */
     public static final byte VA_BLUTENDEWUNDE = 5;
 
     //Mannschaftsteile
 
     /** TODO Missing Parameter Documentation */
     public static final byte MT_TORWART = 1;
 
     /** TODO Missing Parameter Documentation */
     public static final byte MT_ABWEHR = 2;
 
     /** TODO Missing Parameter Documentation */
     public static final byte MT_MITTELFELD = 3;
 
     /** TODO Missing Parameter Documentation */
     public static final byte MT_STURM = 4;
 
     //AngriffsVarianten
 
     /** TODO Missing Parameter Documentation */
     public static final byte ANGRIFF_ERFOLGREICH = 4;
 
     /** TODO Missing Parameter Documentation */
     public static final byte ANGRIFF_TORWART_HAELT = 3;
 
     /** TODO Missing Parameter Documentation */
     public static final byte ANGRIFF_ABWEHR_KLAERT = 2;
 
     /** TODO Missing Parameter Documentation */
     public static final byte ANGRIFF_MITTELFELD_KLAERT = 1;
 
     //Zufallsgenerator
 
     /** TODO Missing Parameter Documentation */
     public static java.util.Random random = new java.util.Random();
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** TODO Missing Parameter Documentation */
     protected de.hattrickorganizer.model.Server m_clServer;
 
     /** gibt die Spielgeschwindigkeit an von 1 - 60000 */
 
     //ergibt ne wartezeit von 2 sec pro Spielminute
     protected int m_iSpielgeschwindigkeit = 120;
     private byte m_bOffensivGrenze = ESB_OFFENSIVGRENZE;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new SpielLogik object.
      */
 
     /*Verwaltung verw*/
     public SpielLogik() {
         //m_clVerwaltung  =   verw;
         m_bOffensivGrenze = ESB_OFFENSIVGRENZE;
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Erzeugt eine Zufallszahl von 0 bis einschließlich
      *
      * @param bis ( exklusiv )
      *
      * @return TODO Missing Return Method Documentation
      */
     public static int getZufallsZahl(int bis) {
         if (bis > 0) {
             return random.nextInt(bis);
         } else {
             return 0;
         }
     }
 
     /**
      * Getter for property m_iSpielgeschwindigkeit.
      *
      * @return Value of property m_iSpielgeschwindigkeit.
      */
     public final int getSpielgeschwindigkeit() {
         return m_iSpielgeschwindigkeit;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param torart TODO Missing Method Parameter Documentation
      * @param verein TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final ServerSpieler TorschuetzeBerechnen(byte torart, ServerTeam verein) {
         ServerSpieler schuetze = null;
 
         /*   if ( verein.IstComputer() )
            {
                return null;
            }
          */
         switch (torart) {
             case TA_FOULELFMETER: {
                 byte zufall = (byte) getZufallsZahl(verein.getAnzAufgestellteSpieler());
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 schuetze = verein.getSpielerAufFeld(zufall);
             }
 
             break;
 
             case TA_HANDELFMETER: {
                 byte zufall = (byte) (getZufallsZahl(verein.getAnzAufgestellteSpieler()));
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 schuetze = verein.getSpielerAufFeld(zufall);
             }
 
             break;
 
             case TA_TOR_NACH_ECKE: {
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 40) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else if (zufall < 70) {
                     schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                 } else {
                     schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                 }
             }
 
             break;
 
             case TA_DIREKTER_FREISTOSS: {
                 if (verein.isSpielerAufFeld(verein.getKicker())) {
                     schuetze = verein.getSpielerById(verein.getKicker());
                 } else {
                     byte zufall = (byte) (getZufallsZahl(verein.getAnzAufgestellteSpieler()));
 
                     if (zufall < 0) {
                         zufall *= -1;
                     }
 
                     schuetze = verein.getSpielerAufFeld(zufall);
                 }
             }
 
             break;
 
             case TA_TOR_NACH_FREISTOSS: {
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 40) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else {
                     if (zufall < 70) {
                         schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                     } else {
                         schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                     }
                 }
             }
 
             break;
 
             case TA_FELDTOR_STARKER_FUSS:
             case TA_FELDTOR_SCHWACHER_FUSS: {
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 50) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else {
                     if (zufall < 83) {
                         schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                     } else {
                         schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                     }
                 }
             }
 
             break;
 
             case TA_FELDTOR_PER_KOPF: {
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 50) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else {
                     if (zufall < 75) {
                         schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                     } else {
                         schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                     }
                 }
             }
 
             break;
 
             case TA_EIGENTOR:
 
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 15) {
                     schuetze = verein.getZufaelligenSpieler(MT_TORWART);
                 } else {
                     if (zufall < 80) {
                         schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                     } else {
                         schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                     }
                 }
 
                 break;
         }
 
         if (schuetze == null) {
             System.err.println("TS: Nix gefunden");
             verein.getSpielerAufFeld((byte) getZufallsZahl(verein.getAnzAufgestellteSpieler()));
         }
 
         return schuetze;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param torart TODO Missing Method Parameter Documentation
      * @param verein TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final ServerSpieler VorbereiterBerechnen(byte torart, ServerTeam verein) {
         ServerSpieler schuetze = null;
 
         /*
            if ( verein.IstComputer() )
            {
                return null;
            }
          */
         switch (torart) {
             case TA_FOULELFMETER: {
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 50) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else {
                     if (zufall < 83) {
                         schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                     } else {
                         schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                     }
                 }
 
                 break;
             }
 
             case TA_HANDELFMETER:
                 schuetze = null;
                 break;
 
             case TA_TOR_NACH_ECKE: {
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 50) {
                     ServerSpieler pschuetze = null;
 
                     if (verein.isSpielerAufFeld(verein.getKicker())) {
                         pschuetze = verein.getSpielerById(verein.getKicker());
                     } else {
                         for (int i = 0; i < verein.getAnzAufgestellteSpieler(); ++i) {
                             ServerSpieler spieler = null;
                             spieler = verein.getSpielerAufFeld((byte) i);
 
                             if (pschuetze == null) {
                                 pschuetze = spieler;
                             } else if (pschuetze.getStk(ISpielerPosition.FORWARD) < spieler
                                                                                                             .getStk(ISpielerPosition.FORWARD)) {
                                 pschuetze = spieler;
                             }
                         }
                     }
 
                     schuetze = pschuetze;
                 } else if (zufall < 60) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else if (zufall < 85) {
                     schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
 
                     /*if (  zufall < 80 )*/
                 } else {
                     schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                 }
 
                 /*
                    else
                    {
                        schuetze = null;
                    } */
             }
 
             break;
 
             case TA_DIREKTER_FREISTOSS:
                 schuetze = null;
                 break;
 
             case TA_TOR_NACH_FREISTOSS: {
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 40) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else {
                     if (zufall < 70) {
                         schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                     } else {
                         schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                     }
                 }
             }
 
             break;
 
             case TA_FELDTOR_STARKER_FUSS:
             case TA_FELDTOR_SCHWACHER_FUSS: {
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 33) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else {
                     if (zufall < 83) {
                         schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                     } else {
                         schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                     }
                 }
             }
 
             break;
 
             case TA_FELDTOR_PER_KOPF: {
                 //Schütze suchen
                 byte zufall = (byte) getZufallsZahl(100);
 
                 if (zufall < 0) {
                     zufall *= -1;
                 }
 
                 if (zufall < 33) {
                     schuetze = verein.getZufaelligenSpieler(MT_STURM);
                 } else {
                     if (zufall < 83) {
                         schuetze = verein.getZufaelligenSpieler(MT_MITTELFELD);
                     } else {
                         schuetze = verein.getZufaelligenSpieler(MT_ABWEHR);
                     }
                 }
             }
 
             break;
 
             case TA_EIGENTOR:
                 schuetze = null;
                 break;
         }
 
         if (schuetze == null) {
             System.err.println("VB: Nix gefunden");
             verein.getSpielerAufFeld((byte) getZufallsZahl(verein.getAnzAufgestellteSpieler()));
         }
 
         return schuetze;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param spielbericht TODO Missing Method Parameter Documentation
      * @param server TODO Missing Method Parameter Documentation
      */
     public final void doEinfacheSpielberechnung(Spielbericht spielbericht,
                                                 de.hattrickorganizer.model.Server server) {
         int C1Heim;
         int C1Gast;
         byte spielzeit;
         byte minute;
         float mittelfeld1;
         float mittelfeld2;
         boolean spiellaeuft;
 /*        boolean spielende;
         boolean bverlaengerung;
         boolean belfmeterschiessen;
         boolean userspiel*/;
 
         //Init
         m_clServer = server;
 
         //Live spiel ?
         //userspiel = true;
         mittelfeld1 = spielbericht.Heim().getMFTeamStk();
         mittelfeld2 = spielbericht.Gast().getMFTeamStk();
         C1Heim = ESB_BerechneC(mittelfeld1, mittelfeld2, true);
         C1Gast = ESB_BerechneC(mittelfeld2, mittelfeld1, false);
 
         //Offensiv Grenze noch berechnen
         berechneOffensivGrenze(spielbericht.Heim().getGesamtStk(),
                                spielbericht.Gast().getGesamtStk());
 
         //Variablen initalisieren
         spielzeit = 45;
         minute = 1;
         spiellaeuft = true;
         //spielende = false;
         //bverlaengerung = false;
         //belfmeterschiessen = false;
 
         spielbericht.ToreHeim((byte) 0);
         spielbericht.ToreGast((byte) 0);
 
         //Vortext      
         doText(KEY_VORTEXT);
 
         //Aufstellungsinfo usw
         doSpielbeginnInfo(spielbericht);
 
         //Die 90 minuten Spielzeit ...
         while (spiellaeuft) {
             byte torchanceheim = -1;
             byte torchancegast = -1;
             int verletzungsId = -1;
 
             for ( ; minute <= spielzeit; minute++) {
                 torchanceheim = ESB_BerechneTorchanceHeim(C1Heim, spielbericht);
                 torchancegast = ESB_BerechneTorchanceGast(C1Gast, spielbericht);
 
                 try {
                     if (torchanceheim > 0) {
                         ServerSpieler schuetze = null;
                         ServerSpieler vorbereiter = null;
 
                         //ServerSpieler   vorbereiter2    =   null;
                         ServerSpieler torwart = null;
                         ServerSpieler verteidiger = null;
                         ServerSpieler verteidiger2 = null;
                         byte torart = -1;
 
                         if (torchanceheim < ANGRIFF_ERFOLGREICH) {
                             torart = ESB_TorartBerechnen((byte) 10);
                         } else {
                             torart = ESB_TorartBerechnen((byte) 0);
                         }
 
                         //Beteiligte Spieler bestimmen
                         schuetze = TorschuetzeBerechnen(torart, spielbericht.Heim());
                         vorbereiter = VorbereiterBerechnen(torart, spielbericht.Heim());
 
                         //vorbereiter2    =   VorbereiterBerechnen( torart, spielbericht.Heim() );
                         torwart = spielbericht.Gast().getZufaelligenSpieler(MT_TORWART);
                         verteidiger = getVerteidiger(spielbericht.Gast(), -1);
 
                         try {
                             verteidiger2 = getVerteidiger(spielbericht.Gast(), verteidiger.getID());
                         } catch (Exception e) {
                             verteidiger2 = verteidiger;
                         }
 
                         //Eignetor Sonderfall
                         if (torart == TA_EIGENTOR) {
                             schuetze = TorschuetzeBerechnen(torart, spielbericht.Gast());
 
                             //vorbereiter     =   null;
                             //vorbereiter2    =   null;
                         }
 
                         //Achtung vorbereiter und schuetze sollten unterschiedlich sein!
                         if ((schuetze != null)
                             && (vorbereiter != null)
                             && (schuetze.getID() == vorbereiter.getID())) {
                             //neuen Vorbereiter holen
                             do {
                                 vorbereiter = spielbericht.Heim().getSpielerAufFeld((byte) getZufallsZahl(spielbericht.Heim()
                                                                                                                       .getAnzAufgestellteSpieler()));
                             } while ((vorbereiter == null)
                                      || (vorbereiter.getID() == schuetze.getID())
                                      || (spielbericht.Heim().getAnzAufgestellteSpieler() < 3));
                         }
 
                         //Torchancen mitzählen
                         spielbericht.TorchancenHeim((byte) (spielbericht.TorchancenHeim() + 1));
 
                         //Tor
                        if (torchanceheim == ANGRIFF_ERFOLGREICH && schuetze != null) {
                             //Sonderfall Elfmeter
                             if ((torart == TA_HANDELFMETER) || (torart == TA_FOULELFMETER)) {
                                 if (ElfmeterBerechnen()) {
                                     spielbericht.ToreHeim((byte) (spielbericht.ToreHeim() + 1));
                                     schuetze.setAnzTore((byte) (schuetze.getAnzTore() + 1));
                                 } else {
                                     schuetze.setAnzTorChancen((byte) (schuetze.getAnzTorChancen()
                                                               + 1));
                                     torchanceheim = ANGRIFF_TORWART_HAELT;
                                 }
                             } else {
                                 spielbericht.ToreHeim((byte) (spielbericht.ToreHeim() + 1));
                                 schuetze.setAnzTore((byte) (schuetze.getAnzTore() + 1));
 
                                 if ((vorbereiter != null) && (torart != TA_EIGENTOR)) {
                                     vorbereiter.setAnzVorlagen((byte) (vorbereiter.getAnzVorlagen()
                                                                + 1));
                                 }
 
                                 //Ecken merken
                                 if (torart == TA_TOR_NACH_ECKE) {
                                     spielbericht.setAnzEckenHeim((byte) (spielbericht
                                                                          .getAnzEckenHeim() + 1));
                                 }
                             }
                         } else {
                        	if(schuetze != null)
                        		schuetze.setAnzTorChancen((byte) (schuetze.getAnzTorChancen() + 1));
                         }
 
                         //Torchance anzeigen
                         sendTorchance(true, schuetze, vorbereiter, verteidiger, verteidiger2,
                                       torwart, torart, torchanceheim, minute);
                     }
 
                     if (torchancegast > 0) {
                         ServerSpieler schuetze = null;
                         ServerSpieler vorbereiter = null;
 
                         //ServerSpieler   vorbereiter2    =   null;
                         ServerSpieler torwart = null;
                         ServerSpieler verteidiger = null;
                         ServerSpieler verteidiger2 = null;
                         byte torart = -1;
 
                         if (torchanceheim < ANGRIFF_ERFOLGREICH) {
                             torart = ESB_TorartBerechnen((byte) 10);
                         } else {
                             torart = ESB_TorartBerechnen((byte) 0);
                         }
 
                         //Beteiligte Spieler bestimmen                        
                         schuetze = TorschuetzeBerechnen(torart, spielbericht.Gast());
                         vorbereiter = VorbereiterBerechnen(torart, spielbericht.Gast());
 
                         //vorbereiter2    =   VorbereiterBerechnen( TA_TOR_NACH_ECKE, spielbericht.Gast() );
                         torwart = spielbericht.Heim().getZufaelligenSpieler(MT_TORWART);
                         verteidiger = getVerteidiger(spielbericht.Heim(), -1);
 
                         try {
                             verteidiger2 = getVerteidiger(spielbericht.Heim(), verteidiger.getID());
                         } catch (Exception e) {
                             verteidiger2 = verteidiger;
                         }
 
                         //Eignetor Sonderfall
                         if (torart == TA_EIGENTOR) {
                             schuetze = TorschuetzeBerechnen(torart, spielbericht.Heim());
 
                             //vorbereiter     =   null;
                             //vorbereiter2    =   null;
                         }
 
                         //Achtung vorbereiter und schuetze sollten unterschiedlich sein!
                         if ((schuetze != null)
                             && (vorbereiter != null)
                             && (schuetze.getID() == vorbereiter.getID())) {
                             //neuen Vorbereiter holen
                             do {
                                 vorbereiter = spielbericht.Gast().getSpielerAufFeld((byte) getZufallsZahl(spielbericht.Gast()
                                                                                                                       .getAnzAufgestellteSpieler()));
                             } while ((vorbereiter == null)
                                      || (vorbereiter.getID() == schuetze.getID())
                                      || (spielbericht.Gast().getAnzAufgestellteSpieler() < 3));
                         }
 
                         //Torchancen mitzählen
                         spielbericht.TorchancenGast((byte) (spielbericht.TorchancenGast() + 1));
 
                         //Tor
                        if (torchancegast == ANGRIFF_ERFOLGREICH && schuetze != null) {
                             //Sonderfall Elfmeter
                             if ((torart == TA_HANDELFMETER) || (torart == TA_FOULELFMETER)) {
                                 if (ElfmeterBerechnen()) {
                                     spielbericht.ToreGast((byte) (spielbericht.ToreGast() + 1));
                                     schuetze.setAnzTore((byte) (schuetze.getAnzTore() + 1));
                                 } else {
                                     schuetze.setAnzTorChancen((byte) (schuetze.getAnzTorChancen()
                                                               + 1));
                                     torchancegast = ANGRIFF_TORWART_HAELT;
                                 }
                             } else {
                                 spielbericht.ToreGast((byte) (spielbericht.ToreGast() + 1));
                                 schuetze.setAnzTore((byte) (schuetze.getAnzTore() + 1));
 
                                 if ((vorbereiter != null) && (torart != TA_EIGENTOR)) {
                                     vorbereiter.setAnzVorlagen((byte) (vorbereiter.getAnzVorlagen()
                                                                + 1));
                                 }
 
                                 //Ecken merken
                                 if (torart == TA_TOR_NACH_ECKE) {
                                     spielbericht.setAnzEckenGast((byte) (spielbericht
                                                                          .getAnzEckenGast() + 1));
                                 }
                             }
                         } else {
                        	if (schuetze != null)
                        		schuetze.setAnzTorChancen((byte) (schuetze.getAnzTorChancen() + 1));
                         }
 
                         //Torchance anzeigen
                         sendTorchance(false, schuetze, vorbereiter, verteidiger, verteidiger2,
                                       torwart, torart, torchancegast, minute);
                     }
                 } catch (Exception e) {
                     System.err.println("schuezte = null ?");
                     System.err.println(e.toString());
                 }
 
                 //Verletzungen
                 verletzungsId = doVerletzungBerechnen(minute, spielbericht.Heim(), true);
 
                 if (verletzungsId > 0) {
                     doAuswechslung(spielbericht.Heim(), verletzungsId, spielbericht, true, minute);
 
                     //C1 C2 neu berechnen!
                     mittelfeld1 = spielbericht.Heim().getMFTeamStk();
                     mittelfeld2 = spielbericht.Gast().getMFTeamStk();
                     C1Heim = ESB_BerechneC(mittelfeld1, mittelfeld2, true);
                     C1Gast = ESB_BerechneC(mittelfeld2, mittelfeld1, false);
                 }
 
                 verletzungsId = doVerletzungBerechnen(minute, spielbericht.Gast(), false);
 
                 if (verletzungsId > 0) {
                     doAuswechslung(spielbericht.Gast(), verletzungsId, spielbericht, false, minute);
 
                     //C1 C2 neu berechnen!
                     mittelfeld1 = spielbericht.Heim().getMFTeamStk();
                     mittelfeld2 = spielbericht.Gast().getMFTeamStk();
                     C1Heim = ESB_BerechneC(mittelfeld1, mittelfeld2, true);
                     C1Gast = ESB_BerechneC(mittelfeld2, mittelfeld1, false);
                 }
 
                 //Karten
                 if (doRoteKartenBerechnen(spielbericht.Heim(), spielbericht, minute, true)) {
                     mittelfeld1 = spielbericht.Heim().getMFTeamStk();
                     mittelfeld2 = spielbericht.Gast().getMFTeamStk();
                     C1Heim = ESB_BerechneC(mittelfeld1, mittelfeld2, true);
                     C1Gast = ESB_BerechneC(mittelfeld2, mittelfeld1, false);
 
                     // Eventuell Fan Reaktion wie "Schieber, Schieber, Schiri wir wissen wo dein Auto steht..." )
                     if (getZufallsZahl(101) < (50 + (spielbericht.getAnzRoteKartenHeim() * 5))) {
                         final int variante = getZufallsZahl(m_clServer.getAnzAllgemeineVarianten());
 
                         m_clServer.doFangesang(KEY_FANGESANG, FG_SCHIRI, variante,
                                                spielbericht.Heim().getTeamName(),
                                                spielbericht.Heim().getManagerName(),
                                                spielbericht.Gast().getTeamName(),
                                                spielbericht.Gast().getManagerName(), true);
                     }
                 }
 
                 if (doRoteKartenBerechnen(spielbericht.Gast(), spielbericht, minute, false)) {
                     mittelfeld1 = spielbericht.Heim().getMFTeamStk();
                     mittelfeld2 = spielbericht.Gast().getMFTeamStk();
                     C1Heim = ESB_BerechneC(mittelfeld1, mittelfeld2, true);
                     C1Gast = ESB_BerechneC(mittelfeld2, mittelfeld1, false);
 
                     // Eventuell Fan Reaktion wie "Schieber, Schieber, Schiri wir wissen wo dein Auto steht..." )
                     if (getZufallsZahl(100) < (50 + (spielbericht.getAnzRoteKartenGast() * 5))) {
                         final int variante = getZufallsZahl(m_clServer.getAnzAllgemeineVarianten());
 
                         m_clServer.doFangesang(KEY_FANGESANG, FG_SCHIRI, variante,
                                                spielbericht.Gast().getTeamName(),
                                                spielbericht.Gast().getManagerName(),
                                                spielbericht.Heim().getTeamName(),
                                                spielbericht.Heim().getManagerName(), false);
                     }
                 }
 
                 //GElbRot ?
                 if (doGelbeKarten(spielbericht.Heim(), spielbericht, minute, true)) {
                     mittelfeld1 = spielbericht.Heim().getMFTeamStk();
                     mittelfeld2 = spielbericht.Gast().getMFTeamStk();
                     C1Heim = ESB_BerechneC(mittelfeld1, mittelfeld2, true);
                     C1Gast = ESB_BerechneC(mittelfeld2, mittelfeld1, false);
 
                     // Eventuell Fan Reaktion wie "Schieber, Schieber, Schiri wir wissen wo dein Auto steht..." )
                     if (getZufallsZahl(100) < (30 + (spielbericht.getAnzGelbeKartenHeim() * 3))) {
                         final int variante = getZufallsZahl(m_clServer.getAnzAllgemeineVarianten());
 
                         m_clServer.doFangesang(KEY_FANGESANG, FG_SCHIRI, variante,
                                                spielbericht.Heim().getTeamName(),
                                                spielbericht.Heim().getManagerName(),
                                                spielbericht.Gast().getTeamName(),
                                                spielbericht.Gast().getManagerName(), true);
                     }
                 }
 
                 //GElbRot ?
                 if (doGelbeKarten(spielbericht.Gast(), spielbericht, minute, false)) {
                     mittelfeld1 = spielbericht.Heim().getMFTeamStk();
                     mittelfeld2 = spielbericht.Gast().getMFTeamStk();
                     C1Heim = ESB_BerechneC(mittelfeld1, mittelfeld2, true);
                     C1Gast = ESB_BerechneC(mittelfeld2, mittelfeld1, false);
 
                     // Eventuell Fan Reaktion wie "Schieber, Schieber, Schiri wir wissen wo dein Auto steht..." )
                     if (getZufallsZahl(100) < (30 + (spielbericht.getAnzGelbeKartenGast() * 3))) {
                         final int variante = getZufallsZahl(m_clServer.getAnzAllgemeineVarianten());
 
                         m_clServer.doFangesang(KEY_FANGESANG, FG_SCHIRI, variante,
                                                spielbericht.Gast().getTeamName(),
                                                spielbericht.Gast().getManagerName(),
                                                spielbericht.Heim().getTeamName(),
                                                spielbericht.Heim().getManagerName(), false);
                     }
                 }
 
                 // Fan Gesänge
                 doFangesang(spielbericht);
 
                 //Minute verstreichen lassen
                 try {
                     // 60 sec / x warten 
                     Thread.sleep(60000 / m_iSpielgeschwindigkeit);
                 } catch (Exception e) {
                 }
 
                 //Anzeige aktualisiseren
                 sendSpielminuteRum(spielbericht, minute);
 
                 //Hat ein Screen Abbruch gefordert ?
                 if (m_clServer.isAbbruch()) {
                     return;
                 }
             }
 
             //ende for            
             //1te Halbzeit -> 2te Halbzeit
             if (spielzeit < 90) {
                 spielzeit = 90;
                 minute = 46;
                 doText(KEY_HALBZEIT);
             } //Verlämgerung 1.Halbzeit ?
             else if ((spielbericht.ToreHeim() == spielbericht.ToreGast())
                      && (spielzeit == 90)
                      && (minute == 91)) {
                 spielzeit = 105;
                 minute = 90;
                 doText(KEY_VERLAENGERUNG);
             } //Verlämgerung 2.Halbzeit ?
             else if ((spielbericht.ToreHeim() == spielbericht.ToreGast())
                      && (spielzeit == 105)
                      && (minute == 106)) {
                 spielzeit = 120;
                 minute = 105;
                 doText(KEY_HALBZEIT);
             } //Spielende 
             else {
                 spiellaeuft = false;
             }
         }
 
         //ende while
         //Elfmeterschiessen ?
         if ((spielbericht.ToreHeim() == spielbericht.ToreGast())) {
             doText(KEY_ELFMETERSCHIESSEN);
             minute = 121;
             ESB_BerechneElfmeterSchiessen(spielbericht);
 
             //Hat ein Screen Abbruch gefordert ?
             if (m_clServer.isAbbruch()) {
                 return;
             }
         }
 
         // Statistiken berechnen
         doEckenBerechnen(spielbericht);
         doAbseitsfalleBerechnen(spielbericht);
         berechneZweikampfwerte(spielbericht);
 
         //Ende Mitteilen
         sendSpielEnde(spielbericht);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param sb TODO Missing Method Parameter Documentation
      */
     public final void doSpielbeginnInfo(Spielbericht sb) {
         /*int     waitTime        =   0;
            boolean screen1Fertig   =   false;
            boolean screen2Fertig   =   false;
         
            if ( sc2 != null )
            {
                sc2.doSpielbeginn ( sb );
            }
         
            if ( sc1 != null )
            {
                sc1.doSpielbeginn ( sb );
            }
         
            while ( ( waitTime < 10 ) && ( !screen1Fertig ) && ( !screen2Fertig ) )
            {
         
                waitTime++;
         
                if ( sc2 != null )
                {
                    screen2Fertig   =   sc2.isScreenBereit ();
                }
                else
                {
                    screen2Fertig   =   true;
                }
                if ( sc1 != null )
                {
                    screen1Fertig   =   sc1.isScreenBereit () ;
                }
                else
                {
                    screen1Fertig   =   true;
                }
         
                try
                {
                    Thread.sleep ( 1000 );
                }
                catch( Exception e )
                {
                }
            }
          */
         m_clServer.doSpielbeginn(sb);
     }
 
     /**
      * Setter for property m_iSpielgeschwindigkeit.
      *
      * @param m_iSpielgeschwindigkeit New value of property m_iSpielgeschwindigkeit.
      */
     public final void seSpielgeschwindigkeit(int m_iSpielgeschwindigkeit) {
         this.m_iSpielgeschwindigkeit = m_iSpielgeschwindigkeit;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param mt TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final byte getGrundFuerGelbeKarte(byte mt) {
         int wert = -1;
         byte grund = -1;
 
         wert = getZufallsZahl(100);
 
         switch (mt) {
             case MT_TORWART: {
                 if (wert < 50) {
                     grund = GFK_MECKERN;
                 } else {
                     grund = GFK_UNSPORTLICHES_VERHALTEN;
                 }
             }
 
             break;
 
             case MT_ABWEHR: {
                 if (wert < 50) {
                     grund = GFK_FOUL;
                 } else if (wert < 70) {
                     grund = GFK_ABSICHTLICHES_HANDSPIEL;
                 } else if (wert < 90) {
                     grund = GFK_MECKERN;
                 } else {
                     grund = GFK_UNSPORTLICHES_VERHALTEN;
                 }
             }
 
             break;
 
             case MT_MITTELFELD: {
                 if (wert < 50) {
                     grund = GFK_FOUL;
                 } else if (wert < 65) {
                     grund = GFK_ABSICHTLICHES_HANDSPIEL;
                 } else if (wert < 85) {
                     grund = GFK_MECKERN;
                 } else {
                     grund = GFK_UNSPORTLICHES_VERHALTEN;
                 }
             }
 
             break;
 
             case MT_STURM: {
                 if (wert < 40) {
                     grund = GFK_FOUL;
                 } else if (wert < 60) {
                     grund = GFK_ABSICHTLICHES_HANDSPIEL;
                 } else if (wert < 85) {
                     grund = GFK_MECKERN;
                 } else {
                     grund = GFK_UNSPORTLICHES_VERHALTEN;
                 }
             }
 
             break;
 
             default:
                 grund = GFK_FOUL;
                 break;
         }
 
         return grund;
     }
 
     /**
      * liefert einen Spieler der Mannschaft in der Defensive für eine Spielsituation
      *
      * @param team welches Team
      * @param ignoreID SpielerId die nicht zurückgeliefert werden soll!
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final ServerSpieler getVerteidiger(ServerTeam team, int ignoreID) {
         ServerSpieler player = null;
         final byte zufall = (byte) getZufallsZahl(100);
 
         if (zufall < 67) {
             player = team.getZufaelligenSpieler(MT_ABWEHR);
         } else {
         }
 
         player = team.getZufaelligenSpieler(MT_MITTELFELD);
 
         if ((player == null) || (player.getID() == ignoreID)) {
             do {
                 player = team.getSpielerAufFeld((byte) getZufallsZahl(team
                                                                       .getAnzAufgestellteSpieler()));
             } while ((player == null)
                      || (player.getID() == ignoreID)
                      || (team.getAnzAufgestellteSpieler() < 3));
         }
 
         return player;
     }
 
     /**
      * faked die Zeweikampfwerte
      *
      * @param sb TODO Missing Constructuor Parameter Documentation
      */
     protected final void berechneZweikampfwerte(Spielbericht sb) {
         int Z1 = 0;
         int Z2 = 0;
         byte Summe1 = 0;
         byte Summe2 = 0;
         ServerSpieler player = null;
 
         for (int i = 0; i < sb.Heim().getStartAufstellung().size(); i++) {
             player = sb.Heim().getSpielerById(((Integer) sb.Heim()
                                                                            .getStartAufstellung()
                                                                            .elementAt(i)).intValue());
             Z1 += player.getStk(ISpielerPosition.CENTRAL_DEFENDER);
         }
 
         for (int i = 0; i < sb.Gast().getStartAufstellung().size(); i++) {
             player = sb.Gast().getSpielerById(((Integer) sb.Gast()
                                                                            .getStartAufstellung()
                                                                            .elementAt(i)).intValue());
             Z2 += player.getStk(ISpielerPosition.CENTRAL_DEFENDER);
         }
 
         Summe1 = (byte) ((100 * (float) Z1) / (Z1 + Z2));
         Summe2 = (byte) (100 - Summe1);
 
         if (sb.ToreHeim() != sb.ToreGast()) {
             if (sb.ToreHeim() > sb.ToreGast()) {
                 Summe1 += 5;
                 Summe2 -= 5;
             } else {
                 Summe2 += 5;
                 Summe1 -= 5;
             }
         }
 
         int zufall = random.nextInt() % 4;
 
         Summe1 += zufall;
         Summe2 -= zufall;
 
         sb.setZweikampfBilanzHeim(Summe1);
         sb.setZweikampfBilanzGast(Summe2);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param sb TODO Missing Method Parameter Documentation
      */
     protected final void doAbseitsfalleBerechnen(Spielbericht sb) {
         int wert = 0;
         float fWert = 0.0f;
         ServerTeam heim = null;
         ServerTeam gast = null;
 
         heim = sb.Heim();
         gast = sb.Gast();
 
         fWert = 55.0f
                 + getZufallsZahl((int) (heim.getAWTeamStk() / (gast.getSTTeamStk()
                                  + sb.TorchancenGast() + 1))) + (random.nextInt() % 15);
         wert = (int) fWert;
 
         if (wert > 100) {
             wert = 100;
         }
 
         //Erfolgquote in Prozent setzen
         sb.setAbseitsfalleHeim((byte) wert);
 
         fWert = 55.0f
                 + getZufallsZahl((int) (gast.getAWTeamStk() / (heim.getSTTeamStk()
                                  + sb.TorchancenHeim() + 1))) + (random.nextInt() % 15);
         wert = (int) fWert;
 
         if (wert > 100) {
             wert = 100;
         }
 
         //Erfolgquote in Prozent setzen
         sb.setAbseitsfalleGast((byte) wert);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param verein TODO Missing Method Parameter Documentation
      * @param spielerId TODO Missing Method Parameter Documentation
      * @param sb TODO Missing Method Parameter Documentation
      * @param heim TODO Missing Method Parameter Documentation
      * @param spielminute TODO Missing Method Parameter Documentation
      */
     protected final void doAuswechslung(ServerTeam verein, int spielerId, Spielbericht sb,
                                         boolean heim, int spielminute) {
         int einwechselSpieler = -1;
         de.hattrickorganizer.model.SpielerPosition pos = null;
         de.hattrickorganizer.model.SpielerPosition posBank = null;
         String nameRaus = "";
         String nameRein = "";
         final int variante = getZufallsZahl(m_clServer.getAnzAllgemeineVarianten());
 
         //EinwechselSpielerholen
         if (heim) {
             if (sb.getAnzWechselHeim() < 3) {
                 einwechselSpieler = verein.getReserveSpielerId4Mannschaftsteil(verein
                                                                                .getMannschaftsteil4Spieler(verein
                                                                                                            .getSpielerById(spielerId)));
             }
         } else {
             if (sb.getAnzWechselGast() < 3) {
                 einwechselSpieler = verein.getReserveSpielerId4Mannschaftsteil(verein
                                                                                .getMannschaftsteil4Spieler(verein
                                                                                                            .getSpielerById(spielerId)));
             }
         }
 
         //Auswechseln 
         pos = verein.getSpielerPositionObjBySpielerID(spielerId);
 
         if (pos != null) {
             //Position des ausgewechselten ändern
             verein.getSpielerById(spielerId).setPosition(ServerSpieler.AUSGEWECHSELT);
             nameRaus = verein.getSpielerById(spielerId).getName();
 
             if (einwechselSpieler > 0) {
                 nameRein = verein.getSpielerById(einwechselSpieler).getName();
 
                 //Position des eingewechselten ändern
                 verein.getSpielerById(einwechselSpieler).setPosition(verein.getSpielerById(spielerId)
                                                                            .getPosition());
 
                 //Spieler von der Bank nehmen
                 posBank = verein.getSpielerPositionObjBySpielerID(einwechselSpieler);
                 posBank.setSpielerId(-1);
             }
 
             //Spieler auf's Feld packen
             pos.setSpielerId(einwechselSpieler);
         }
 
         if (heim) {
             sb.setAnzWechselHeim((byte) (sb.getAnzWechselHeim() + 1));
         } else {
             sb.setAnzWechselGast((byte) (sb.getAnzWechselGast() + 1));
         }
 
         //Sende Auswechslung         
         m_clServer.doAuswechslung(KEY_AUSWECHSLUNG, verein.getTeamName(), nameRaus, nameRein,
                                   variante, spielminute, heim);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param sb TODO Missing Method Parameter Documentation
      */
     protected final void doEckenBerechnen(Spielbericht sb) {
         int wert;
         int randWert;
 
         randWert = getZufallsZahl(3);
         wert = randWert + sb.getAnzEckenHeim() + (int) (sb.TorchancenHeim() / 2.2f);
         sb.setAnzEckenHeim((byte) wert);
 
         randWert = getZufallsZahl(3);
         wert = randWert + sb.getAnzEckenGast() + (int) (sb.TorchancenGast() / 2.2f);
         sb.setAnzEckenGast((byte) wert);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param sb TODO Missing Method Parameter Documentation
      */
     protected final void doFangesang(Spielbericht sb) {
         int zufall = -1;
         int wahrscheinlichkeit = -1;
         final int torDif = sb.ToreHeim() - sb.ToreGast();
         final int key = KEY_FANGESANG;
         int art = -1;
         final int var = getZufallsZahl(m_clServer.getAnzAllgemeineVarianten());
 
         ServerTeam fanTeam = null;
         String fanManager = "";
         ServerTeam gegnerTeam = null;
         String gegnerManager = "";
         boolean heim = true;
 
         zufall = getZufallsZahl(100);
         wahrscheinlichkeit = 5;
 
         //Die Fans machen sich bemerkbar
         if (zufall < wahrscheinlichkeit) {
             wahrscheinlichkeit = 60;
             zufall = getZufallsZahl(100);
 
             //Heim gesang
             if (zufall < 60) {
                 zufall = getZufallsZahl(100);
 
                 //Anfeuerung
                 if (zufall < 45) {
                     art = FG_ANFEUERUNG;
                 }
                 //Gegner veräppeln ( nur bei Führung )
                 else if ((zufall < 75) && (torDif > 0)) {
                     art = FG_GEGNER;
 
                     //Sonstige Rufe
                 } else {
                     art = FG_SONSTIGES;
                 }
 
                 fanManager = sb.Heim().getManagerName();
                 fanTeam = sb.Heim();
                 gegnerTeam = sb.Gast();
                 gegnerManager = sb.Gast().getManagerName();
             } else {
                 zufall = getZufallsZahl(100);
                 heim = false;
 
                 //Anfeuerung
                 if (zufall < 45) {
                     art = FG_ANFEUERUNG;
                 }
                 //Gegner veräppeln ( nur bei Führung )
                 else if ((zufall < 75) && (torDif < 0)) {
                     art = FG_GEGNER;
 
                     //Sonstige Rufe
                 } else {
                     art = FG_SONSTIGES;
                 }
 
                 fanManager = sb.Gast().getManagerName();
                 fanTeam = sb.Gast();
                 gegnerTeam = sb.Heim();
                 gegnerManager = sb.Heim().getManagerName();
             }
 
             //FanGesang ausgeben
             sendFangesang(key, art, var, fanTeam, fanManager, gegnerTeam, gegnerManager, heim);
         }
     }
 
     /**
      * liefert True wenn's gelb/rot gab
      *
      * @param verein TODO Missing Constructuor Parameter Documentation
      * @param sb TODO Missing Constructuor Parameter Documentation
      * @param minute TODO Missing Constructuor Parameter Documentation
      * @param heim TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final boolean doGelbeKarten(ServerTeam verein, Spielbericht sb, byte minute,
                                           boolean heim) {
         int zufall = -1;
         int wahrscheinlichkeit = -1;
         int id = -1;
         boolean gelbRot = false;
         byte key = KEY_GELBEKARTE;
         byte variante = -1;
         final int trainerVariante = getZufallsZahl(100);
 
         zufall = getZufallsZahl(1000);
         wahrscheinlichkeit = 7;
 
         //Es gibt Gelb
         if (zufall < wahrscheinlichkeit) {
             de.hattrickorganizer.model.SpielerPosition pos = null;
 
             id = verein.getSpielerAufFeld((byte) (getZufallsZahl(verein.getAnzAufgestellteSpieler())))
                        .getID();
             pos = verein.getSpielerPositionObjBySpielerID(id);
 
             // halbe Wahrscheinlichkeit beim TW
             if (pos.getPosition() == ISpielerPosition.KEEPER) {
                 id = verein.getSpielerAufFeld((byte) (getZufallsZahl(verein
                                                                      .getAnzAufgestellteSpieler())))
                            .getID();
             }
 
             //Halbe Wahrscheinlichkeit für bereits verwarnte
             if (verein.getSpielerById(id).isGelbVerwarnt()) {
                 id = verein.getSpielerAufFeld((byte) (getZufallsZahl(verein
                                                                      .getAnzAufgestellteSpieler())))
                            .getID();
             }
 
             //pos nochmal holen sicher ist sicher!
             pos = verein.getSpielerPositionObjBySpielerID(id);
 
             //prüfen auf GelbRot
             if (verein.getSpielerById(id).isGelbVerwarnt()) {
                 gelbRot = true;
 
                 //Runter nehmen!
                 pos.setSpielerId(-1);
                 key = KEY_GELBROTEKARTE;
                 verein.getSpielerById(id).setGelbeKarten(2);
             } else {
                 verein.getSpielerById(id).setGelbeKarten(1);
             }
 
             if (heim) {
                 if (gelbRot) {
                     sb.setAnzRoteKartenHeim((byte) (sb.getAnzRoteKartenHeim() + 1));
                 } else {
                     sb.setAnzGelbeKartenHeim((byte) (sb.getAnzGelbeKartenHeim() + 1));
                 }
             } else {
                 if (gelbRot) {
                     sb.setAnzRoteKartenGast((byte) (sb.getAnzRoteKartenGast() + 1));
                 } else {
                     sb.setAnzGelbeKartenGast((byte) (sb.getAnzGelbeKartenGast() + 1));
                 }
             }
 
             variante = getGrundFuerGelbeKarte(verein.getMannschaftsteil4Spieler(verein
                                                                                 .getSpielerById(id)));
             m_clServer.doKarte(key, verein.getSpielerById(id).getName(), minute, variante,
                                trainerVariante, heim);
         }
 
         return gelbRot;
     }
 
     /**
      * berechnet die Nachspielzeit
      *
      * @param sb TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final int doNachspielzeitBerechnen(Spielbericht sb) {
         return getZufallsZahl(3) + (sb.getAnzahlKarten() / 3);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param verein TODO Missing Method Parameter Documentation
      * @param sb TODO Missing Method Parameter Documentation
      * @param minute TODO Missing Method Parameter Documentation
      * @param heim TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final boolean doRoteKartenBerechnen(ServerTeam verein, Spielbericht sb, byte minute,
                                                   boolean heim) {
         int zufall = -1;
         int wahrscheinlichkeit = -1;
         int id = -1;
         final int variante = getZufallsZahl(m_clServer.getAnzAllgemeineVarianten());
         final int trainerVariante = getZufallsZahl(100);
 
         zufall = getZufallsZahl(10000);
         wahrscheinlichkeit = 2;
 
         //Es gibt Rot
         if (zufall < wahrscheinlichkeit) {
             de.hattrickorganizer.model.SpielerPosition pos = null;
 
             id = verein.getSpielerAufFeld((byte) (getZufallsZahl(verein.getAnzAufgestellteSpieler())))
                        .getID();
             pos = verein.getSpielerPositionObjBySpielerID(id);
 
             //Spieler vom Feld nehmen
             if (pos != null) {
                 pos.setSpielerId(-1);
             }
 
             //Spielbericht anpassen
             if (heim) {
                 sb.setAnzRoteKartenHeim((byte) (sb.getAnzRoteKartenHeim() + 1));
             } else {
                 sb.setAnzRoteKartenGast((byte) (sb.getAnzRoteKartenGast() + 1));
             }
 
             //Meldung rausjagen                
             m_clServer.doKarte(KEY_ROTEKARTE, verein.getSpielerById(id).getName(), minute,
                                variante, trainerVariante, heim);
 
             //true zurückwerfen
             return true;
         }
 
         return false;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param key TODO Missing Method Parameter Documentation
      */
     protected final void doText(byte key) {
         //sendMsg( MatchScreen.MSG_KOMMENTAR, "Optimales Fussballwetter bei ausverkauften Haus. Die Spieler stehen auf dem Platz und da ist der Anpfiff.\nEs geht los...", screenHeim, screenGast );
         final int variante = getZufallsZahl(m_clServer.getAnzAllgemeineVarianten());
 
         /*int     waitTime        =   0;
            boolean screen1Fertig   =   false;
            boolean screen2Fertig   =   false;
         
         
            if ( screen2 != null )
            {
                screen2.drawInfoText ( key, variante );
            }
         
            if ( screen1 != null )
            {
                screen1.drawInfoText ( key, variante );
            }
         
             while ( ( waitTime < 10 ) && ( !screen1Fertig ) && ( !screen2Fertig ) )
            {
         
                waitTime++;
         
                if ( screen2 != null )
                {
                    screen2Fertig   =   screen2.isScreenBereit ();
                }
                else
                {
                    screen2Fertig   =   true;
                }
                if ( screen1 != null )
                {
                    screen1Fertig   =   screen1.isScreenBereit () ;
                }
                else
                {
                    screen1Fertig   =   true;
                }
         
                try
                {
                    Thread.sleep ( 1000 );
                }
                catch( Exception e )
                {
                }
            }
          */
         m_clServer.drawInfoText(key, variante);
     }
 
     /**
      * prüft auf Verletzung
      *
      * @param minute TODO Missing Constructuor Parameter Documentation
      * @param verein TODO Missing Constructuor Parameter Documentation
      * @param heim TODO Missing Constructuor Parameter Documentation
      *
      * @return -1 keine Verletzung sonst die SpielerID des Vereltzten!
      */
     protected final int doVerletzungBerechnen(byte minute, ServerTeam verein, boolean heim) {
         int zufall = 0;
         int wahrscheinlickeit = 0;
         int spielerId = -1;
         final int trainerVariante = getZufallsZahl(100);
         zufall = getZufallsZahl(10000 + 1);
         wahrscheinlickeit = minute / (getZufallsZahl(5) + 1);
 
         //Verletzung
         if (zufall < wahrscheinlickeit) {
             byte verletzungsart = -1;
             int wert = -1;
 
             wert = getZufallsZahl(100);
 
             if (wert <= 15) {
                 verletzungsart = VA_BAENDERVERLETZUNG;
             } else if ((wert > 15) && (wert <= 60)) {
                 verletzungsart = VA_MUSKELVERLETZUNG;
             } else if ((wert > 60) && (wert <= 80)) {
                 verletzungsart = VA_PRELLUNG;
             } else if ((wert > 80) && (wert <= 85)) {
                 verletzungsart = VA_KNOCHENBUCH;
             } else if ((wert > 85) && (wert <= 90)) {
                 verletzungsart = VA_GELENKSCHADEN;
             } else if ((wert > 90) && (wert <= 100)) {
                 verletzungsart = VA_BLUTENDEWUNDE;
             }
 
             wert = getZufallsZahl(100);
 
             // Torwart
             if (wert <= 5) {
                 spielerId = verein.getZufaelligenSpieler(MT_TORWART).getID();
             } else {
                 wert = getZufallsZahl(3);
 
                 switch (wert) {
                     case 0:
                         spielerId = verein.getZufaelligenSpieler(MT_ABWEHR).getID();
                         break;
 
                     case 1:
                         spielerId = verein.getZufaelligenSpieler(MT_MITTELFELD).getID();
                         break;
 
                     case 2:
                         spielerId = verein.getZufaelligenSpieler(MT_STURM).getID();
                         break;
                 }
             }
 
             //Spieler markieren
             verein.getSpielerById(spielerId).setVerletzt(1);
 
             //Verletzung ausgeben
             m_clServer.doVerletzung(KEY_VERLETZUNG, verein.getSpielerById(spielerId).getName(),
                                     minute, verletzungsart, trainerVariante, heim);
 
             /*
                if ( sc2 != null )
                {
                   sc2.doVerletzung( KEY_VERLETZUNG, verein.getSpielerById( spielerId ), minute, verletzungsart, trainerVariante, heim );
                }
             
                if ( sc1 != null )
                {
                   sc1.doVerletzung( KEY_VERLETZUNG, verein.getSpielerById( spielerId ), minute, verletzungsart, trainerVariante, heim );
                }
              */
         }
 
         return spielerId;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param key TODO Missing Method Parameter Documentation
      * @param art TODO Missing Method Parameter Documentation
      * @param variante TODO Missing Method Parameter Documentation
      * @param fanTeam TODO Missing Method Parameter Documentation
      * @param fanManager TODO Missing Method Parameter Documentation
      * @param gegnerTeam TODO Missing Method Parameter Documentation
      * @param gegnerManager TODO Missing Method Parameter Documentation
      * @param heim TODO Missing Method Parameter Documentation
      */
     protected final void sendFangesang(int key, int art, int variante, ServerTeam fanTeam,
                                        String fanManager, ServerTeam gegnerTeam,
                                        String gegnerManager, boolean heim) {
         /*
            int     waitTime        =   0;
            boolean screen1Fertig   =   false;
            boolean screen2Fertig   =   false;
         
         
            if ( screen2 != null )
            {
                screen2.doFangesang( key, art, variante, fanTeam, fanManager, gegnerTeam, gegnerManager, heim );
            }
         
            if ( screen1 != null )
            {
                screen1.doFangesang( key, art, variante, fanTeam, fanManager, gegnerTeam, gegnerManager, heim );
            }
         
            while ( ( waitTime < 10 ) && ( !screen1Fertig ) && ( !screen2Fertig ) )
            {
         
                waitTime++;
         
                if ( screen2 != null )
                {
                    screen2Fertig   =   screen2.isScreenBereit ();
                }
                else
                {
                    screen2Fertig   =   true;
                }
                if ( screen1 != null )
                {
                    screen1Fertig   =   screen1.isScreenBereit () ;
                }
                else
                {
                    screen1Fertig   =   true;
                }
         
                try
                {
                    Thread.sleep ( 1000 );
                }
                catch( Exception e )
                {
                }
            }
          */
         m_clServer.doFangesang(key, art, variante, fanTeam.getTeamName(), fanManager,
                                gegnerTeam.getTeamName(), gegnerManager, heim);
     }
 
     /*
        sendet eine Msg an die Screens
      */
     protected final void sendInfoMsg(byte msgType, int variante) {
         /*
            int     waitTime        =   0;
            boolean screen1Fertig   =   false;
            boolean screen2Fertig   =   false;
         
         
            if ( screen2 != null )
            {
                screen2.drawInfoText ( msgType, variante );
            }
         
            if ( screen1 != null )
            {
                screen1.drawInfoText ( msgType, variante );
            }
         
            while ( ( waitTime < 10 ) && ( !screen1Fertig ) && ( !screen2Fertig ) )
            {
         
                waitTime++;
         
                if ( screen2 != null )
                {
                    screen2Fertig   =   screen2.isScreenBereit ();
                }
                else
                {
                    screen2Fertig   =   true;
                }
                if ( screen1 != null )
                {
                    screen1Fertig   =   screen1.isScreenBereit () ;
                }
                else
                {
                    screen1Fertig   =   true;
                }
         
                try
                {
                    Thread.sleep ( 1000 );
                }
                catch( Exception e )
                {
                }
            }
          */
         m_clServer.drawInfoText(msgType, variante);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param sb TODO Missing Method Parameter Documentation
      */
     protected final void sendSpielEnde(Spielbericht sb) {
         /*
            String msg  =   "";
         
            if ( sc2 != null )
            {
                sc2.doSpielende ( sb );
            }
         
            if ( sc1 != null )
            {
                sc1.doSpielende ( sb );
            }
          */
         m_clServer.doSpielende(sb);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param sb TODO Missing Method Parameter Documentation
      * @param spielminute TODO Missing Method Parameter Documentation
      */
     protected final void sendSpielminuteRum(Spielbericht sb, int spielminute) {
         /*
            int     waitTime        =   0;
            boolean screen1Fertig   =   false;
            boolean screen2Fertig   =   false;
         
            if ( sc2 != null )
            {
                sc2.doMinuteRum ( spielminute );
            }
         
            if ( sc1 != null )
            {
                sc1.doMinuteRum ( spielminute );
            }
         
            while ( ( waitTime < 10 ) && ( !screen1Fertig ) && ( !screen2Fertig ) )
            {
         
                waitTime++;
         
                if ( sc2 != null )
                {
                    screen2Fertig   =   sc2.isScreenBereit ();
                }
                else
                {
                    screen2Fertig   =   true;
                }
                if ( sc1 != null )
                {
                    screen1Fertig   =   sc1.isScreenBereit () ;
                }
                else
                {
                    screen1Fertig   =   true;
                }
         
                try
                {
                    Thread.sleep ( 1000 );
                }
                catch( Exception e )
                {
                }
            }
          */
         m_clServer.doMinuteRum(spielminute);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param heimTeam TODO Missing Method Parameter Documentation
      * @param schuetze TODO Missing Method Parameter Documentation
      * @param vorbereiter1 TODO Missing Method Parameter Documentation
      * @param verteidiger1 TODO Missing Method Parameter Documentation
      * @param verteidiger2 TODO Missing Method Parameter Documentation
      * @param torwart TODO Missing Method Parameter Documentation
      * @param torart TODO Missing Method Parameter Documentation
      * @param torchance TODO Missing Method Parameter Documentation
      * @param spielminute TODO Missing Method Parameter Documentation
      */
     protected final void sendTorchance(boolean heimTeam, ServerSpieler schuetze,
                                        ServerSpieler vorbereiter1, ServerSpieler verteidiger1,
                                        ServerSpieler verteidiger2, ServerSpieler torwart,
                                        byte torart, byte torchance, int spielminute) {
         int[] aktionen = null;
 
         //Start Ende ist immer da! + maximal 2 weitere = getZUfallsZahl( 3 )
         final int anzAktionen = 2 + getZufallsZahl(3);
 
         //getZufallsZahl( m_clServer.getAnzTorVarianten() ); //wird über AktionsVarianten gewürfelt, also eigentlich hier unnötig daher = 0
         int variante = TA_NORMAL;
         final int trainerVariante = getZufallsZahl(100);
         final int trainerVariante2 = getZufallsZahl(100);
 
         //wie viele verschiedene Aktionen stehen zur verfügung ?
         int aktionsRadius = m_clServer.getAnzAktionsVarianten();
 
         //Die Namen fehlen
         String schuetzeName = "";
         String vorbereiter1Name = "";
         String verteidiger2Name = "";
         String verteidiger1Name = "";
         String torwartName = "";
 
         //Namen Mappen
         if (schuetze != null) {
             schuetzeName = schuetze.getName();
         }
 
         if (vorbereiter1 != null) {
             vorbereiter1Name = vorbereiter1.getName();
         }
 
         if (verteidiger2 != null) {
             verteidiger2Name = verteidiger2.getName();
         }
 
         if (verteidiger1 != null) {
             verteidiger1Name = verteidiger1.getName();
         }
 
         if (torwart != null) {
             torwartName = torwart.getName();
         }
 
         //Seltene Variante nehmen 10% Wahrscheinlichkeit.        
         if (getZufallsZahl(100) < 4) {
             //Seltene Variante auswaehlen
             //m_clServer.getAnzTorVarianten() + getZufallsZahl( m_clServer.getAnzSelteneTorVarianten () );
             variante = TA_SELTEN;
             aktionsRadius = m_clServer.getAnzSelteneTorVarianten();
         }
 
         //Aktionen erstellen
         aktionen = new int[anzAktionen];
 
         //aktionsvariante nur für mittelaktionen würfeln ?
         for (int i = 0; i < anzAktionen; i++) {
             aktionen[i] = getZufallsZahl(aktionsRadius);
 
             //Sicherstellen das keine Meldung 2* hinter einander kommt bei einem 4 Zeiler
             if ((anzAktionen == 4)
                 && (i == 2)
                 && (aktionen[i] == aktionen[i - 1])
                 && (aktionsRadius > 1)) {
                 //noch "freie" Meldung holen
                 do {
                     aktionen[i] = getZufallsZahl(aktionsRadius);
                 } while (aktionen[i] == aktionen[i - 1]);
             }
         }
 
         m_clServer.doTorchance(heimTeam, schuetzeName, vorbereiter1Name, verteidiger1Name,
                                verteidiger2Name, torwartName, torart, torchance, variante,
                                trainerVariante, trainerVariante2, aktionen, spielminute);
 
         /*
            //Ebenentiefe zufällig ermitteln per Server maxTiefe
            if ( screen2 != null )
            {
                screen2.doTorchance ( heimTeam, schuetze, vorbereiter1,  verteidiger1, verteidiger2, torwart, torart, torchance, variante, trainerVariante, trainerVariante2, aktionen, spielminute );
            }
         
            if ( screen1 != null )
            {
                screen1.doTorchance ( heimTeam, schuetze, vorbereiter1,  verteidiger1, verteidiger2, torwart, torart, torchance, variante, trainerVariante, trainerVariante2, aktionen, spielminute );
            }
         
            while ( ( waitTime < 10 ) && ( !screen1Fertig ) && ( !screen2Fertig ) )
            {
         
                waitTime++;
         
                if ( screen2 != null )
                {
                    screen2Fertig   =   screen2.isScreenBereit ();
                }
                else
                {
                    screen2Fertig   =   true;
                }
                if ( screen1 != null )
                {
                    screen1Fertig   =   screen1.isScreenBereit () ;
                }
                else
                {
                    screen1Fertig   =   true;
                }
         
                try
                {
                    Thread.sleep ( 1000 );
                }
                catch( Exception e )
                {
                }
         
            }
          */
     }
 
     /**
      * berechnet die Chancenwahrscheinlichkeit für ein Team
      *
      * @param M1 Mittelfeld Stärke des Teams
      * @param M2 Mittelfeld Stärke des Gegners
      * @param heimspiel hat das Team ein Heimspiel
      *
      * @return TODO Missing Return Method Documentation
      */
     private int ESB_BerechneC(float M1, float M2, boolean heimspiel) {
         float ret = 0;
         float TomX = 0;
 
         //X-Faktor ermitteln
         TomX = M1 / M2;
 
         //X-Faktor in Vorgegebene Grenzen halten!
         if (TomX > ESB_OBERGRENZE) {
             TomX = ESB_OBERGRENZE;
         } else if (TomX < ESB_UNTERGRENZE) {
             TomX = ESB_UNTERGRENZE;
         }
 
         //Durchschnittliche Chancen *11 * Faktor
         ret = (AVG_TORCHANCEN * 1000.0f) / 90.0f * ((TomX / 2.0f) + 0.5f);
 
         //Heimspiel Bonus
         if (heimspiel) {
             //Zu überlegen = überheblich daher abzug wie bei auswärtsteam
             if (TomX > 1.5f) {
                 ret *= 0.9f;
 
                 //Bonus draufhauen
             } else {
                 ret *= 1.1f;
             }
 
             //Auswärts-Bonus kleiner als HeimBonus
         } else {
             ret *= 0.9f;
         }
 
         /*
            HOLogger.instance().log(getClass(),"C Wert " + Math.round( ret ) );
             //Komisch Mittelfeld Werte scheinen halbiert zu sein zB.: statt catch (25 kam 12.35 raus... )
            HOLogger.instance().log(getClass(),"M1 Wert " + M1 );
            HOLogger.instance().log(getClass(),"M2 Wert " + M2 );
          */
         return Math.round(ret);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param spielbericht TODO Missing Method Parameter Documentation
      */
     private void ESB_BerechneElfmeterSchiessen(Spielbericht spielbericht) {
         //byte    Wert            =   0;
         byte anzahlElfmeter = 0;
         ServerSpieler schuetze = null;
 
         //Elfmeter
         byte torchance = 5;
         ServerSpieler torwart = null;
         byte torart = TA_FOULELFMETER;
         int i = 1;
 
         while ((anzahlElfmeter < 5) || (spielbericht.ToreHeim() == spielbericht.ToreGast())) {
             torart = (byte) (TA_FOULELFMETER + getZufallsZahl(2));
             schuetze = spielbericht.Heim().getSpielerAufFeld((byte) (i % spielbericht.Heim()
                                                                                      .getAnzAufgestellteSpieler()));
             torwart = spielbericht.Gast().getZufaelligenSpieler(MT_TORWART);
             torchance = 3;
 
             if (ElfmeterBerechnen()) {
                 spielbericht.ToreHeim((byte) (spielbericht.ToreHeim() + 1));
                 torchance = 4;
             }
 
             sendTorchance(true, schuetze, null, null, null, torwart, torart, torchance, 120);
 
             torart = (byte) (TA_FOULELFMETER + getZufallsZahl(2));
             schuetze = spielbericht.Gast().getSpielerAufFeld((byte) (i % spielbericht.Gast()
                                                                                      .getAnzAufgestellteSpieler()));
             torwart = spielbericht.Heim().getZufaelligenSpieler(MT_TORWART);
             torchance = 3;
 
             if (ElfmeterBerechnen()) {
                 spielbericht.ToreGast((byte) (spielbericht.ToreGast() + 1));
                 torchance = 4;
             }
 
             sendTorchance(false, schuetze, null, null, null, torwart, torart, torchance, 120);
 
             ++anzahlElfmeter;
 
             ++i;
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param C1 TODO Missing Method Parameter Documentation
      * @param spielbericht TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private byte ESB_BerechneTorchanceGast(int C1, Spielbericht spielbericht) {
         byte ret = -1;
         int zufall = getZufallsZahl(CHANCEN_ZUFALLS_RAUM) + 1;
         int angriff;
         int abwehr;
         int off1;
         int def2;
 
         //gibt es ne chance ?
         if (zufall <= C1) {
             //teamstk + obBonus aufrunden
             off1 = Math.round(spielbericht.Gast().getSTTeamStk() + OB_BONUS);
             def2 = Math.round(spielbericht.Heim().getTWTeamStk()
                               + spielbericht.Heim().getAWTeamStk());
 
             //Ist Offensive Stärker dann
             if (off1 >= def2) {
                 //Offensive an defensive anpassen
                 off1 = Math.round((def2 * ((float) m_bOffensivGrenze)) / 100.0f);
             }
 
             //Zufallszahlen holen
             angriff = getZufallsZahl(Math.round((off1 * ((float) ESB_STEUERUNG)) / 100.0f));
             abwehr = getZufallsZahl(Math.round((def2 * ((float) ESB_STEUERUNG)) / 100.0f));
 
             //Angriff bei zu starker Fphrung abschwächen
             if ((spielbericht.ToreGast() - spielbericht.ToreHeim()) >= 3) {
                 angriff = Math.round((angriff) / 2.0f);
             }
 
             if (spielbericht.ToreGast() >= 4) {
                 angriff = Math.round((angriff) / 2.0f);
             }
 
             //Angriff erfolrecih wenn > Abwehr
             if (angriff > abwehr) {
                 //tor
                 ret = ANGRIFF_ERFOLGREICH;
 
                 //Sonst prüfen wann gestoppt
             } else {
                 zufall = (byte) (getZufallsZahl(100));
 
                 if (zufall < 20) {
                     //MF klärt
                     ret = ANGRIFF_MITTELFELD_KLAERT;
                 } else if (zufall < 65) {
                     ret = ANGRIFF_ABWEHR_KLAERT;
                 } else {
                     //TW hat pariert
                     ret = ANGRIFF_TORWART_HAELT;
                 }
             }
         }
 
         return ret;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param C1 TODO Missing Method Parameter Documentation
      * @param spielbericht TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private byte ESB_BerechneTorchanceHeim(int C1, Spielbericht spielbericht) {
         byte ret = -1;
         int zufall = getZufallsZahl(CHANCEN_ZUFALLS_RAUM) + 1;
         int angriff;
         int abwehr;
         int off1;
         int def2;
 
         //gibt es ne chance ?
         if (zufall <= C1) {
             //teamstk + obBonus aufrunden
             off1 = Math.round(spielbericht.Heim().getSTTeamStk() + OB_BONUS);
             def2 = Math.round(spielbericht.Gast().getTWTeamStk()
                               + spielbericht.Gast().getAWTeamStk());
 
             //Ist Offensive Stärker dann
             if (off1 >= def2) {
                 //Offensive an defensive anpassen
                 off1 = Math.round((def2 * ((float) m_bOffensivGrenze)) / 100.0f);
             }
 
             //Zufallszahlen holen
             angriff = getZufallsZahl(Math.round((off1 * ((float) ESB_STEUERUNG)) / 100.0f));
             abwehr = getZufallsZahl(Math.round((def2 * ((float) ESB_STEUERUNG)) / 100.0f));
 
             //Angriff bei zu starker Führung abschwächen
             if ((spielbericht.ToreHeim() - spielbericht.ToreGast()) >= 3) {
                 angriff = Math.round((angriff) / 2.0f);
             }
 
             if (spielbericht.ToreHeim() >= 4) {
                 angriff = Math.round((angriff) / 2.0f);
             }
 
             //Angriff erfolrecih wenn > Abwehr
             if (angriff > abwehr) {
                 //tor
                 ret = ANGRIFF_ERFOLGREICH;
 
                 //Sonst prüfen wann gestoppt
             } else {
                 zufall = (byte) (getZufallsZahl(100));
 
                 if (zufall < 20) {
                     //MF klärt
                     ret = ANGRIFF_MITTELFELD_KLAERT;
                 } else if (zufall < 65) {
                     ret = ANGRIFF_ABWEHR_KLAERT;
                 } else {
                     //TW hat pariert
                     ret = ANGRIFF_TORWART_HAELT;
                 }
             }
         }
 
         return ret;
     }
 
     /**
      * berechnet die Torart
      *
      * @param minimum = Minimumwert der Zufallszahl z.B. 10 sorgt dafür das ein Wert zwischen 10
      *        und 100 rauskommt , damit würde es z.B. keine Elfmeter geben
      *
      * @return TODO Missing Return Method Documentation
      */
     private byte ESB_TorartBerechnen(byte minimum) {
         byte ret;
         byte torart;
 
         torart = (byte) (minimum + getZufallsZahl(100 - minimum));
         ret = TA_FELDTOR_STARKER_FUSS;
 
         //berechnen
         if (torart < 8) {
             ret = TA_FOULELFMETER;
         } else if (torart < 10) {
             ret = TA_HANDELFMETER;
         } else if (torart < 18) {
             ret = TA_TOR_NACH_ECKE;
         } else if (torart < 25) {
             ret = TA_DIREKTER_FREISTOSS;
         } else if (torart < 43) {
             ret = TA_TOR_NACH_FREISTOSS;
         } else if (torart < 63) {
             ret = TA_FELDTOR_PER_KOPF;
         } else if (torart < 98) {
             ret = TA_FELDTOR_STARKER_FUSS;
         } else {
             ret = TA_EIGENTOR;
         }
 
         if (ret == TA_FELDTOR_STARKER_FUSS) {
             byte zufall = (byte) getZufallsZahl(100);
 
             if (zufall < 0) {
                 zufall *= -1;
             }
 
             if (zufall < 25) {
                 ret = TA_FELDTOR_SCHWACHER_FUSS;
             }
         }
 
         return ret;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private boolean ElfmeterBerechnen() {
         byte Wert;
         boolean ret;
 
         ret = false;
         Wert = (byte) getZufallsZahl(100);
 
         // getroffen
         if (Wert < 80) {
             ret = true;
         }
 
         return ret;
     }
 
     /**
      * berechnet die Offensivgrenze für dieses Spiel
      *
      * @param teamstk1 GesamtSTk des Heimteams
      * @param teamstk2 GesamtSTk des Gastteams
      */
     private void berechneOffensivGrenze(float teamstk1, float teamstk2) {
         //Offensivgrenze anpassen
         if (teamstk1 > teamstk2) {
             m_bOffensivGrenze = (byte) Math.round(ESB_OFFENSIVGRENZE
                                                   + (ESB_OFFENSIVANPASSUNG * ((teamstk1) / (teamstk2))));
         } else if (teamstk1 < teamstk2) {
             m_bOffensivGrenze = (byte) Math.round(ESB_OFFENSIVGRENZE
                                                   + (ESB_OFFENSIVANPASSUNG * ((teamstk2) / (teamstk1))));
         }
 
         if (teamstk1 == teamstk2) {
             m_bOffensivGrenze = ESB_OFFENSIVGRENZE;
         }
     }
 }
