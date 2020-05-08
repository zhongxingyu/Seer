 // %3239001626:de.hattrickorganizer.logik%
 /*
  * Aufstellungsassistent.java
  *
  * Created on 20. März 2003, 10:20
  */
 package de.hattrickorganizer.logik;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import plugins.ILineUp;
 import plugins.ISpieler;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.model.HOModel;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.Lineup;
 import de.hattrickorganizer.model.Spieler;
 import de.hattrickorganizer.model.SpielerPosition;
 import de.hattrickorganizer.tools.HOLogger;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author thomas.werth
  */
 public class LineupAssistant {
    
     //~ Instance fields ----------------------------------------------------------------------------
 
     /*gibt die mindeststärke für einen Spieler an für seine Idealpos*/
 
     //protected float m_fMinIdealPosStk       =   3.5f;
 
     /** Position die zu besetzen sind */
 
     //protected Vector    m_vPositionen   =   new Vector();
 
     /** Spieler die aufzustellen sind */
 
     //protected Vector    m_vSpieler      =   new Vector();
 
     /** gibt an wie auf Wettereinfluss reagiert werden soll in Prozent */
 
     //20%
     private float m_fWetterBonus = 0.2f;
 
     /** gibt das Wetter an */
     private int m_iWetter = ISpieler.LEICHTBEWOELKT;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new instance of Aufstellungsassistent
      */
     public LineupAssistant() {
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * findet den besten noch freien Kicker für einen Elfer
      *
      * @param liste TODO Missing Constructuor Parameter Documentation
      * @param vSpieler TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final int getBestFreeElferKicker(int[] liste, Vector<ISpieler> vSpieler, Vector<ISpielerPosition> positionen) {
         Spieler spieler = null;
         float maxValue = -1;
         float curValue = -1;

        // Another attempt to avoid Temp1 being in all penalty lists if less than 11 are fielded
        // -1 is the Temp1 ID.
        // int bestPlayerID = -1;

        int bestPlayerID = 0;
 
         for (int i = 0; (vSpieler != null) && (i < vSpieler.size()); i++) {
             spieler = (Spieler) vSpieler.elementAt(i);
             curValue = (spieler.getErfahrung() * 1.5f)
                        + (((spieler.getStandards() * 7.0f) / 10.0f)
                        + ((spieler.getTorschuss() * 3.0f) / 10.0f));
 
             if (spieler.getSpezialitaet() == ISpieler.BALLZAUBERER) {
                 curValue *= 1.1;
             }
 
             if ((isSpielerInAnfangsElf(spieler.getSpielerID(), positionen))
                 && (!isSpielerInElferListe(liste, spieler.getSpielerID()))
                 && (curValue > maxValue)) {
                 maxValue = curValue;
                 bestPlayerID = spieler.getSpielerID();
             }
         }
 
         return bestPlayerID;
     }
 
     /**
      * erstellt die beste Elfmeterreihenfolge
      *
      * @param vSpieler TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final int[] setElferKicker(Vector<ISpieler> vSpieler, Vector<ISpielerPosition> positionen) {
         final int[] bestElfer = new int[11];
         int kicker = HOVerwaltung.instance().getModel().getAufstellung()
                                                             .getKicker();
 
         initElferKicker(bestElfer);
 
         for (int i = 0; (bestElfer != null) && (i < bestElfer.length); i++) {
             //erster Schütze ist der Standard schütze
             if ((i == 0) && (kicker > 0)) {
                 bestElfer[0] = kicker;
                 continue;
             }
 
             bestElfer[i] = getBestFreeElferKicker(bestElfer, vSpieler, positionen);
         }
 
         return bestElfer;
     }
 
     /**
      * gibt an ob der Spieler bereits aufgestellt ist auch ReserveBank zählt mit
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerAufgestellt(int spielerId, Vector<ISpielerPosition> positionen) {
         for (int i = 0; (positionen != null) && (i < positionen.size()); i++) {
             if (((SpielerPosition) positionen.elementAt(i)).getSpielerId() == spielerId) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * gibt an ob der Spieler von beginn an Spielt
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerInAnfangsElf(int spielerId, Vector<ISpielerPosition> positionen) {
         for (int i = 0; (positionen != null) && (i < positionen.size()); i++) {
             if ((((SpielerPosition) positionen.elementAt(i)).getId() < ISpielerPosition.startReserves)
                 && (((SpielerPosition) positionen.elementAt(i))
                     .getSpielerId() == spielerId)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * gibt an ob der Spieler schon in der Liste ist
      *
      * @param liste TODO Missing Constructuor Parameter Documentation
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerInElferListe(int[] liste, int spielerId) {
         for (int i = 0; (liste != null) && (i < liste.length); i++) {
             if (liste[i] == spielerId) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * übernimmt das automatische Aufstellen
      *
      * @param positionen die zu besetzenden Positionen
      * @param spieler die aufszustellenden Spieler
      * @param reihenfolge Reihenfolge in der die Mannschaftsteile besetzt werden sollen
      * @param mitForm Formberücksichtigung
      * @param idealPosFirst IdealPosition berücksichtigen ?
      * @param ignoreVerletzung auch Verletzte aufstellen ?
      * @param ignoreSperre auch gesperrte aufstellen ?
      * @param wetterBonus Schwellwert der angibt an wie auf WetterEffekte reagiert werden soll
      * @param wetter das aktuelle Wetter
      */
     public final void doAufstellung(Vector<ISpielerPosition> positionen, Vector<ISpieler> spieler, byte reihenfolge,
                                     boolean mitForm, boolean idealPosFirst,
                                     boolean ignoreVerletzung, boolean ignoreSperre,
                                     float wetterBonus, int wetter) {
         //m_vPositionen   =   new Vector ( positionen );
         //m_vSpieler      =   new Vector( spieler );
         m_fWetterBonus = wetterBonus;
         m_iWetter = wetter;
 
         //nur spieler auf idealpos aufstellen
         if (idealPosFirst) {
             //Wert speichern
             float backup = gui.UserParameter.instance().MinIdealPosStk;
 
             //Maimum von beiden für Berechnung verwenden
             gui.UserParameter.instance().MinIdealPosStk = Math.max(calcAveragePosValue(spieler),
                                                                    gui.UserParameter.instance().MinIdealPosStk);
 
             doSpielerAufstellenIdealPos(ISpielerPosition.KEEPER,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.CENTRAL_DEFENDER,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.CENTRAL_DEFENDER_TOWING,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.CENTRAL_DEFENDER_OFF,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.BACK,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.BACK_TOMID,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.BACK_OFF,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.BACK_DEF,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.MIDFIELDER,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.MIDFIELDER_OFF,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.MIDFIELDER_DEF,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.MIDFIELDER_TOWING,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.WINGER,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.WINGER_OFF,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.WINGER_DEF,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.WINGER_TOMID,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.FORWARD, mitForm,
                                         ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.FORWARD_DEF,
                                         mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
             doSpielerAufstellenIdealPos(ISpielerPosition.FORWARD_TOWING,
                     				    mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
 
             //Wert wieder zurücksetzen
             gui.UserParameter.instance().MinIdealPosStk = backup;
         }
 
         //falls tw unbesetzt
         doSpielerAufstellen(ISpielerPosition.KEEPER, mitForm,
                             ignoreVerletzung, ignoreSperre, spieler, positionen);
 
         
         byte [] order = null;
         //nun reihenfolge beachten und unbesetzte füllen
         switch (reihenfolge) {
             case ILineUp.AW_MF_ST:
 
             	order = new byte[18];
             	// DEFENCE
             	order[0]=ISpielerPosition.CENTRAL_DEFENDER;
             	order[1]=ISpielerPosition.CENTRAL_DEFENDER_TOWING;
             	order[2]=ISpielerPosition.CENTRAL_DEFENDER_OFF;
             	order[3]=ISpielerPosition.BACK;
             	order[4]=ISpielerPosition.BACK_DEF;
                 order[5]=ISpielerPosition.BACK_OFF;
                 order[6]=ISpielerPosition.BACK_TOMID;
                 //MIDFIELD
                 order[7]=ISpielerPosition.MIDFIELDER;
                 order[8]=ISpielerPosition.MIDFIELDER_OFF;
                 order[9]=ISpielerPosition.MIDFIELDER_DEF;
                 order[10]=ISpielerPosition.MIDFIELDER_TOWING;
                 order[11]=ISpielerPosition.WINGER;
                	order[12]=ISpielerPosition.WINGER_DEF;
                	order[13]=ISpielerPosition.WINGER_OFF;
                	order[14]=ISpielerPosition.WINGER_TOMID;
                	// FORWARD
                	order[15]=ISpielerPosition.FORWARD;
                	order[16]=ISpielerPosition.FORWARD_DEF;
             	order[17]=ISpielerPosition.FORWARD_TOWING;
                 break;
 
             case ILineUp.AW_ST_MF:
 
             	order = new byte[18];
 //				 DEFENCE
             	order[0]=ISpielerPosition.CENTRAL_DEFENDER;
             	order[1]=ISpielerPosition.CENTRAL_DEFENDER_TOWING;
             	order[2]=ISpielerPosition.CENTRAL_DEFENDER_OFF;
             	order[3]=ISpielerPosition.BACK;
             	order[4]=ISpielerPosition.BACK_DEF;
                 order[5]=ISpielerPosition.BACK_OFF;
                 order[6]=ISpielerPosition.BACK_TOMID;
 //              FORWARD
                	order[7]=ISpielerPosition.FORWARD;
                	order[8]=ISpielerPosition.FORWARD_DEF;
             	order[9]=ISpielerPosition.FORWARD_TOWING;
                 
                 //MIDFIELD
                 order[10]=ISpielerPosition.MIDFIELDER;
                 order[11]=ISpielerPosition.MIDFIELDER_OFF;
                 order[12]=ISpielerPosition.MIDFIELDER_DEF;
                 order[13]=ISpielerPosition.MIDFIELDER_TOWING;
                 order[14]=ISpielerPosition.WINGER;
                	order[15]=ISpielerPosition.WINGER_DEF;
                	order[16]=ISpielerPosition.WINGER_OFF;
                	order[17]=ISpielerPosition.WINGER_TOMID;
                	
                 break;
 
             case ILineUp.MF_AW_ST:
 
             	order = new byte[18];
             	
                 //MIDFIELD
                 order[0]=ISpielerPosition.MIDFIELDER;
                 order[1]=ISpielerPosition.MIDFIELDER_OFF;
                 order[2]=ISpielerPosition.MIDFIELDER_DEF;
                 order[3]=ISpielerPosition.MIDFIELDER_TOWING;
                 order[4]=ISpielerPosition.WINGER;
                	order[5]=ISpielerPosition.WINGER_DEF;
                	order[6]=ISpielerPosition.WINGER_OFF;
                	order[7]=ISpielerPosition.WINGER_TOMID;
 //              DEFENCE
             	order[8]=ISpielerPosition.CENTRAL_DEFENDER;
             	order[9]=ISpielerPosition.CENTRAL_DEFENDER_TOWING;
             	order[10]=ISpielerPosition.CENTRAL_DEFENDER_OFF;
             	order[11]=ISpielerPosition.BACK;
             	order[12]=ISpielerPosition.BACK_DEF;
                 order[13]=ISpielerPosition.BACK_OFF;
                 order[14]=ISpielerPosition.BACK_TOMID;
                	// FORWARD
                	order[15]=ISpielerPosition.FORWARD;
                	order[16]=ISpielerPosition.FORWARD_DEF;
             	order[17]=ISpielerPosition.FORWARD_TOWING;
                 break;
 
             case ILineUp.MF_ST_AW:
 
            	order = new byte[18];
             	
                 //MIDFIELD
                 order[0]=ISpielerPosition.MIDFIELDER;
                 order[1]=ISpielerPosition.MIDFIELDER_OFF;
                 order[2]=ISpielerPosition.MIDFIELDER_DEF;
                 order[3]=ISpielerPosition.MIDFIELDER_TOWING;
                 order[4]=ISpielerPosition.WINGER;
                	order[5]=ISpielerPosition.WINGER_DEF;
                	order[6]=ISpielerPosition.WINGER_OFF;
                	order[7]=ISpielerPosition.WINGER_TOMID;
 // FORWARD
                	order[8]=ISpielerPosition.FORWARD;
                	order[9]=ISpielerPosition.FORWARD_DEF;
             	order[10]=ISpielerPosition.FORWARD_TOWING;
 //              DEFENCE
             	order[11]=ISpielerPosition.CENTRAL_DEFENDER;
             	order[12]=ISpielerPosition.CENTRAL_DEFENDER_TOWING;
             	order[13]=ISpielerPosition.CENTRAL_DEFENDER_OFF;
             	order[14]=ISpielerPosition.BACK;
             	order[15]=ISpielerPosition.BACK_DEF;
                 order[16]=ISpielerPosition.BACK_OFF;
                 order[17]=ISpielerPosition.BACK_TOMID;
 
                 break;
 
             case ILineUp.ST_MF_AW:
 
             	order = new byte[18];
 
 //              FORWARD
                	order[0]=ISpielerPosition.FORWARD;
                	order[1]=ISpielerPosition.FORWARD_DEF;
             	order[2]=ISpielerPosition.FORWARD_TOWING;
                 //MIDFIELD
                 order[3]=ISpielerPosition.MIDFIELDER;
                 order[4]=ISpielerPosition.MIDFIELDER_OFF;
                 order[5]=ISpielerPosition.MIDFIELDER_DEF;
                 order[6]=ISpielerPosition.MIDFIELDER_TOWING;
                 order[7]=ISpielerPosition.WINGER;
                	order[8]=ISpielerPosition.WINGER_DEF;
                	order[9]=ISpielerPosition.WINGER_OFF;
                	order[10]=ISpielerPosition.WINGER_TOMID;
 
             	// DEFENCE
             	order[11]=ISpielerPosition.CENTRAL_DEFENDER;
             	order[12]=ISpielerPosition.CENTRAL_DEFENDER_TOWING;
             	order[13]=ISpielerPosition.CENTRAL_DEFENDER_OFF;
             	order[14]=ISpielerPosition.BACK;
             	order[15]=ISpielerPosition.BACK_DEF;
                 order[16]=ISpielerPosition.BACK_OFF;
                 order[17]=ISpielerPosition.BACK_TOMID;
                 break;
 
             case ILineUp.ST_AW_MF:
 
             	order = new byte[18];
 //            	 FORWARD
                	order[0]=ISpielerPosition.FORWARD;
                	order[1]=ISpielerPosition.FORWARD_DEF;
             	order[2]=ISpielerPosition.FORWARD_TOWING;
             	// DEFENCE
             	order[3]=ISpielerPosition.CENTRAL_DEFENDER;
             	order[4]=ISpielerPosition.CENTRAL_DEFENDER_TOWING;
             	order[5]=ISpielerPosition.CENTRAL_DEFENDER_OFF;
             	order[6]=ISpielerPosition.BACK;
             	order[7]=ISpielerPosition.BACK_DEF;
                 order[8]=ISpielerPosition.BACK_OFF;
                 order[9]=ISpielerPosition.BACK_TOMID;
                 //MIDFIELD
                 order[10]=ISpielerPosition.MIDFIELDER;
                 order[11]=ISpielerPosition.MIDFIELDER_OFF;
                 order[12]=ISpielerPosition.MIDFIELDER_DEF;
                 order[13]=ISpielerPosition.MIDFIELDER_TOWING;
                 order[14]=ISpielerPosition.WINGER;
                	order[15]=ISpielerPosition.WINGER_DEF;
                	order[16]=ISpielerPosition.WINGER_OFF;
                	order[17]=ISpielerPosition.WINGER_TOMID;
                	
                 break;
                 
 
             default:
                 return;
 
             //break;
         }
 
         if(order != null){
         	for (int i = 0; i < order.length; i++) {
         		doSpielerAufstellen(order[i], mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
 			}
         }
         
         
         
         
         //ReserveSpieler besetzen
         //Reserve
         if (idealPosFirst) {
             //TW
             doReserveSpielerAufstellenIdealPos(ISpielerPosition.KEEPER,
                                                mitForm, ignoreVerletzung, ignoreSperre, spieler,
                                                positionen);
 
             //abwehr
             doReserveSpielerAufstellenIdealPos(ISpielerPosition.CENTRAL_DEFENDER,
                                                mitForm, ignoreVerletzung, ignoreSperre, spieler,
                                                positionen);
 
             //mittelfeld
             doReserveSpielerAufstellenIdealPos(ISpielerPosition.MIDFIELDER,
                                                mitForm, ignoreVerletzung, ignoreSperre, spieler,
                                                positionen);
             doReserveSpielerAufstellenIdealPos(ISpielerPosition.WINGER,
                                                mitForm, ignoreVerletzung, ignoreSperre, spieler,
                                                positionen);
 
             //sturm
             doReserveSpielerAufstellenIdealPos(ISpielerPosition.FORWARD,
                                                mitForm, ignoreVerletzung, ignoreSperre, spieler,
                                                positionen);
         }
 
         //und nochmal für alle unbesetzten
         //TW
         doReserveSpielerAufstellen(ISpielerPosition.KEEPER, mitForm,
                                    ignoreVerletzung, ignoreSperre, spieler, positionen);
 
         //abwehr
         doReserveSpielerAufstellen(ISpielerPosition.CENTRAL_DEFENDER,
                                    mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
 
         //mittelfeld
         doReserveSpielerAufstellen(ISpielerPosition.MIDFIELDER, mitForm,
                                    ignoreVerletzung, ignoreSperre, spieler, positionen);
         doReserveSpielerAufstellen(ISpielerPosition.WINGER,
                                    mitForm, ignoreVerletzung, ignoreSperre, spieler, positionen);
 
         //sturm
         doReserveSpielerAufstellen(ISpielerPosition.FORWARD, mitForm,
                                    ignoreVerletzung, ignoreSperre, spieler, positionen);
     }
 
     /**
      * initialisiert die Liste
      *
      * @param liste TODO Missing Constructuor Parameter Documentation
      */
     public final void initElferKicker(int[] liste) {
         for (int i = 0; (liste != null) && (i < liste.length); i++) {
          
        	// This should cure the issue with first temp player filling penalty spots if less than 11 on field.
        	liste[i] = 0;
        	//  liste[i] = -1;
         }
     }
 
     /**
      * resetet alle Verbindungen zwischen Position und Spieler
      *
      * @param positionen TODO Missing Constructuor Parameter Documentation
      */
     public final void resetPositionsbesetzungen(Vector<ISpielerPosition> positionen) {
         for (int i = 0; (positionen != null) && (i < positionen.size()); i++) {
             ((de.hattrickorganizer.model.SpielerPosition) positionen.elementAt(i)).setSpielerId(0);
         }
     }
 
     /**
      * liefert aus dem eigenen Vector mit Spielern den besten für die angefordertet Position der
      * noch nicht aufgestellt ist
      *
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      * @param ignoreVerletzung TODO Missing Constructuor Parameter Documentation
      * @param ignoreSperre TODO Missing Constructuor Parameter Documentation
      * @param vSpieler TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final de.hattrickorganizer.model.Spieler getBestSpieler(byte position,
                                                                       boolean mitForm,
                                                                       boolean ignoreVerletzung,
                                                                       boolean ignoreSperre,
                                                                       Vector<ISpieler> vSpieler,
                                                                       Vector<ISpielerPosition> positionen) {
         de.hattrickorganizer.model.Spieler spieler = null;
         de.hattrickorganizer.model.Spieler bestSpieler = null;
         float bestStk = -1.0f;
         float aktuStk = 0.0f;
 
         for (int i = 0; (vSpieler != null) && (i < vSpieler.size()); i++) {
             spieler = (de.hattrickorganizer.model.Spieler) vSpieler.elementAt(i);
 
             //stk inklusive Wetter effekt errechnen
             aktuStk = spieler.calcPosValue(position, mitForm);
             aktuStk += (m_fWetterBonus * spieler.getWetterEffekt(m_iWetter) * aktuStk);
 
             if ((!isSpielerAufgestellt(spieler.getSpielerID(), positionen))
                 && ((bestSpieler == null) || (bestStk < aktuStk))
                 && ((ignoreSperre) || (!spieler.isGesperrt()))
                 && ((ignoreVerletzung) || (spieler.getVerletzt() < 1))
                 && (spieler.isSpielberechtigt())) {
                 bestSpieler = spieler;
                 bestStk = aktuStk;
             }
         }
 
         return bestSpieler;
     }
 
     /**
      * liefert aus dem eigenen Vector mit Spielern den besten für die angefordertet Position der
      * noch nicht aufgestellt ist
      *
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      * @param ignoreVerletzung TODO Missing Constructuor Parameter Documentation
      * @param ignoreSperre TODO Missing Constructuor Parameter Documentation
      * @param vSpieler TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final Spieler getBestSpielerIdealPosOnly(byte position,
                                                                                   boolean mitForm,
                                                                                   boolean ignoreVerletzung,
                                                                                   boolean ignoreSperre,
                                                                                   Vector<ISpieler> vSpieler,
                                                                                   Vector<ISpielerPosition> positionen) {
         Spieler spieler = null;
         Spieler bestSpieler = null;
         float bestStk = -1.0f;
         float aktuStk = 0.0f;
 
         for (int i = 0; (vSpieler != null) && (i < vSpieler.size()); i++) {
             //Spieler holen
             spieler = (Spieler) vSpieler.elementAt(i);
 
             //stk inklusive Wetter effekt errechnen
             aktuStk = spieler.calcPosValue(position, mitForm);
             aktuStk += (m_fWetterBonus * spieler.getWetterEffekt(m_iWetter) * aktuStk);
 
             //Idealpos STK muss > mindestwert sein
             if ((!isSpielerAufgestellt(spieler.getSpielerID(), positionen))
                 && (spieler.getIdealPosition() == position)
                 && ((bestSpieler == null) || (bestStk < aktuStk))
                 && ((ignoreSperre) || (!spieler.isGesperrt()))
                 && ((ignoreVerletzung) || (spieler.getVerletzt() < 1))
                 && (aktuStk > gui.UserParameter.instance().MinIdealPosStk)
                 && (!spieler.isTrainer())
                 && (spieler.isSpielberechtigt())) {
                 bestSpieler = spieler;
                 bestStk = aktuStk;
             }
         }
 
         return bestSpieler;
     }
 
     /**
      * besetzt die Torwart Positionen im Vector m_vPositionen
      *
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      * @param ignoreVerletzung TODO Missing Constructuor Parameter Documentation
      * @param ignoreSperre TODO Missing Constructuor Parameter Documentation
      * @param vSpieler TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      */
     protected final void doReserveSpielerAufstellen(byte position, boolean mitForm,
                                                     boolean ignoreVerletzung, boolean ignoreSperre,
                                                     Vector<ISpieler> vSpieler, Vector<ISpielerPosition> positionen) {
         SpielerPosition pos = null;
         Spieler spieler = null;
 
         for (int i = 0; (positionen != null) && (vSpieler != null) && (i < positionen.size());
              i++) {
             pos = (SpielerPosition) positionen.elementAt(i);
 
             //bereits vergebene Positionen ignorieren und ReserveBank leer lassen
             if ((pos.getSpielerId() > 0)
                 || (pos.getId() < ISpielerPosition.startReserves)) {
                 continue;
             }
 
             //nur exacte Pos
             if (pos.getPosition() == position) {
                 spieler = getBestSpieler(position, mitForm, ignoreVerletzung, ignoreSperre,
                                          vSpieler, positionen);
 
                 //position besetzen
                 if (spieler != null) {
                     pos.setSpielerId(spieler.getSpielerID());
                 }
             }
         }
     }
 
     /**
      * besetzt die Torwart Positionen im Vector m_vPositionen
      *
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      * @param ignoreVerletzung TODO Missing Constructuor Parameter Documentation
      * @param ignoreSperre TODO Missing Constructuor Parameter Documentation
      * @param vSpieler TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      */
     protected final void doReserveSpielerAufstellenIdealPos(byte position, boolean mitForm,
                                                             boolean ignoreVerletzung,
                                                             boolean ignoreSperre, Vector<ISpieler> vSpieler,
                                                             Vector<ISpielerPosition> positionen) {
         SpielerPosition pos = null;
         Spieler spieler = null;
 
         for (int i = 0; (positionen != null) && (vSpieler != null) && (i < positionen.size());
              i++) {
             pos = (de.hattrickorganizer.model.SpielerPosition) positionen.elementAt(i);
 
             //bereits vergebene Positionen ignorieren und ReserveBank leer lassen
             if ((pos.getSpielerId() > 0)
                 || (pos.getId() < ISpielerPosition.startReserves)) {
                 continue;
             }
 
             //nur exakte Position
             if (pos.getPosition() == position) {
                 spieler = getBestSpielerIdealPosOnly(position, mitForm, ignoreVerletzung,
                                                      ignoreSperre, vSpieler, positionen);
 
                 //position besetzen
                 if (spieler != null) {
                     pos.setSpielerId(spieler.getSpielerID());
                 }
             }
         }
     }
 
     /**
      * besetzt die Torwart Positionen im Vector m_vPositionen
      *
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      * @param ignoreVerletzung TODO Missing Constructuor Parameter Documentation
      * @param ignoreSperre TODO Missing Constructuor Parameter Documentation
      * @param vSpieler TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      */
     protected final void doSpielerAufstellen(byte position, boolean mitForm,
                                              boolean ignoreVerletzung, boolean ignoreSperre,
                                              Vector<ISpieler> vSpieler, Vector<ISpielerPosition> positionen) {
         SpielerPosition pos = null;
         Spieler spieler = null;
         final Vector<ISpielerPosition> zusPos = new Vector<ISpielerPosition>();
 
         for (int i = 0; (positionen != null) && (vSpieler != null) && (i < positionen.size());
              i++) {
             pos = (SpielerPosition) positionen.elementAt(i);
 
             //bereits vergebene Positionen ignorieren und ReserveBank leer lassen
             if ((pos.getSpielerId() > 0)
                 || (pos.getId() >= ISpielerPosition.startReserves)) {
                 continue;
             }
 
             //nur exacte Pos
             if (pos.getPosition() == position) {
                 //die zus XYZ Positionen nur vormerken
 //                if ((pos.getTaktik() == plugins.ISpielerPosition.ZUS_STUERMER)
 //                    || (pos.getTaktik() == plugins.ISpielerPosition.ZUS_STUERMER_DEF)
 //                    || (pos.getTaktik() == plugins.ISpielerPosition.ZUS_MITTELFELD)
 //                    || (pos.getTaktik() == plugins.ISpielerPosition.ZUS_INNENV)) {
 //                    zusPos.add(pos);
 //                    continue;
 //                }
 
                 spieler = getBestSpieler(position, mitForm, ignoreVerletzung, ignoreSperre,
                                          vSpieler, positionen);
 
                 //position besetzen
                 if (spieler != null) {
                     pos.setSpielerId(spieler.getSpielerID());
                 }
             }
         }
 
         //nun die zus XYZ Positionen füllen
         for (int i = 0; (zusPos != null) && (vSpieler != null) && (i < zusPos.size()); i++) {
             pos = (SpielerPosition) zusPos.elementAt(i);
 
             //bereits vergebene Positionen ignorieren und ReserveBank leer lassen
             if ((pos.getSpielerId() > 0)
                 || (pos.getId() >= ISpielerPosition.startReserves)) {
                 continue;
             }
 
             //nur diese Pos
             if (pos.getPosition() == position) {
                 spieler = getBestSpieler(position, mitForm, ignoreVerletzung, ignoreSperre,
                                          vSpieler, positionen);
 
                 //position besetzen
                 if (spieler != null) {
                     pos.setSpielerId(spieler.getSpielerID());
                 }
             }
         }
     }
 
     /**
      * besetzt die Torwart Positionen im Vector m_vPositionen
      *
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      * @param ignoreVerletzung TODO Missing Constructuor Parameter Documentation
      * @param ignoreSperre TODO Missing Constructuor Parameter Documentation
      * @param vSpieler TODO Missing Constructuor Parameter Documentation
      * @param positionen TODO Missing Constructuor Parameter Documentation
      */
     protected final void doSpielerAufstellenIdealPos(byte position, boolean mitForm,
                                                      boolean ignoreVerletzung,
                                                      boolean ignoreSperre, Vector<ISpieler> vSpieler,
                                                      Vector<ISpielerPosition> positionen) {
        SpielerPosition pos = null;
         Spieler spieler = null;
         final Vector<ISpielerPosition> zusPos = new Vector<ISpielerPosition>();
 
         for (int i = 0; (positionen != null) && (vSpieler != null) && (i < positionen.size());
              i++) {
             pos = (SpielerPosition) positionen.elementAt(i);
 
             //bereits vergebene Positionen ignorieren und ReserveBank leer lassen
             if ((pos.getSpielerId() > 0)
                 || (pos.getId() >= ISpielerPosition.startReserves)) {
                 continue;
             }
 
             //nur exakte Position
             if (pos.getPosition() == position) {
                 //die zus XYZ Positionen nur vormerken
 //                if ((pos.getTaktik() == ISpielerPosition.ZUS_STUERMER)
 //                    || (pos.getTaktik() == ISpielerPosition.ZUS_STUERMER_DEF)
 //                    || (pos.getTaktik() == ISpielerPosition.ZUS_MITTELFELD)
 //                    || (pos.getTaktik() == ISpielerPosition.ZUS_INNENV)) {
 //                    zusPos.add(pos);
 //                    continue;
 //                }
 
                 spieler = getBestSpielerIdealPosOnly(position, mitForm, ignoreVerletzung,
                                                      ignoreSperre, vSpieler, positionen);
 
                 //position besetzen
                 if (spieler != null) {
                     pos.setSpielerId(spieler.getSpielerID());
                 }
             }
         }
 
         //nun die zus XYZ Positionen füllen
         for (int i = 0; (zusPos != null) && (vSpieler != null) && (i < zusPos.size()); i++) {
             pos = (SpielerPosition) zusPos.elementAt(i);
 
             //bereits vergebene Positionen ignorieren und ReserveBank leer lassen
             if ((pos.getSpielerId() > 0)
                 || (pos.getId() >= ISpielerPosition.startReserves)) {
                 continue;
             }
 
             //nur diese Pos
             if (pos.getPosition() == position) {
                 spieler = getBestSpielerIdealPosOnly(position, mitForm, ignoreVerletzung,
                                                      ignoreSperre, vSpieler, positionen);
 
                 //position besetzen
                 if (spieler != null) {
                     pos.setSpielerId(spieler.getSpielerID());
                 }
             }
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param spieler TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private float calcAveragePosValue(Vector<ISpieler> spieler) {
         float average = 0.0f;
         Spieler player = null;
 
         if (spieler == null) {
             return 0.0f;
         }
 
         for (int i = 0; i < spieler.size(); i++) {
             player = ((Spieler) spieler.get(i));
             average += player.calcPosValue(player.getIdealPosition(), true);
         }
 
         average = de.hattrickorganizer.tools.Helper.round(average / spieler.size(), 2);
 
         return average;
     }
 }
