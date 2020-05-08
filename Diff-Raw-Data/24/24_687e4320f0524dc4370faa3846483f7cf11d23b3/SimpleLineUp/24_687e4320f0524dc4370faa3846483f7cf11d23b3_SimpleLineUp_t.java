 package hoplugins.feedback.model.rating;
 
 
 import java.util.Vector;
 
 import plugins.ISpieler;
 import plugins.ISpielerPosition;
 
 /**
  * Simple version of the old 'Aufstellung' class.
  *
  * @author thomas.werth, aik
  */
 public class SimpleLineUp {
 	//~ Static fields/initializers -----------------------------------------------------------------
 
     //Systeme
 
     /** TODO Missing Parameter Documentation */
     public static final String DEFAULT_NAME = "HO!";
 
     /** TODO Missing Parameter Documentation */
     public static final String DEFAULT_NAMELAST = "HO!LastLineup";
 
     /** TODO Missing Parameter Documentation */
     public static final int NO_HRF_VERBINDUNG = -1;
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** h�lt die Positionen */
     private Vector<ISpielerPosition> m_vPositionen = new Vector<ISpielerPosition>();
 
     /** Attitude */
     private int m_iAttitude;
 
     private ISpieler[] players = new ISpieler[11];
 
     /** wer ist Kapit�n */
     private int m_iKapitaen = -1;
 
     /** wer schie�t Standards */
     private int m_iKicker = -1;
 
     /** TacticType */
     private int m_iTacticType;
 
     /** Heim/Ausw */
     private short m_sHeimspiel = -1;
 
 
     /**
      * Creates a new Aufstellung object.
      */
     public SimpleLineUp() {
         initPositionen442();
     }
 
     /**
      * Setter for property m_iAttitude.
      *
      * @param m_iAttitude New value of property m_iAttitude.
      */
     public final void setAttitude(int m_iAttitude) {
         this.m_iAttitude = m_iAttitude;
     }
 
     /**
      * Getter for property m_iAttitude.
      *
      * @return Value of property m_iAttitude.
      */
     public final int getAttitude() {
         return m_iAttitude;
     }
 
     /**
      * Setter for property m_iKapitaen.
      *
      * @param m_iKapitaen New value of property m_iKapitaen.
      */
     public final void setKapitaen(int m_iKapitaen) {
         this.m_iKapitaen = m_iKapitaen;
     }
 
     /**
      * Getter for property m_iKapitaen.
      *
      * @return Value of property m_iKapitaen.
      */
     public final int getKapitaen() {
         return m_iKapitaen;
     }
 
     /**
      * Setter for property m_iKicker.
      *
      * @param m_iKicker New value of property m_iKicker.
      */
     public final void setKicker(int m_iKicker) {
         this.m_iKicker = m_iKicker;
     }
 
     /**
      * Getter for property m_iKicker.
      *
      * @return Value of property m_iKicker.
      */
     public final int getKicker() {
         return m_iKicker;
     }
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param x TODO Missing Method Parameter Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public final float HQ(double x) {
 		// first convert to original HT rating (1...80)
 		x = HTfloat2int(x);
 
 		// and now LoddarStats Hattrick-Quality function (?)
 		double v = (2.0f * x) / (x + 80.0f);
 		return (float) v;
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param x TODO Missing Method Parameter Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public final int HTfloat2int(double x) {
 		// convert reduced float rating (1.00....20.99) to original integer HT rating (1...80)
 		// one +0.5 is because of correct rounding to integer
 		return (int) (((x - 1.0f) * 4.0f) + 1.0f);
 	}
 
     /**
      * Setter for property m_sHeimspiel.
      *
      * @param m_sHeimspiel New value of property m_sHeimspiel.
      */
     public final void setHeimspiel(short m_sHeimspiel) {
         this.m_sHeimspiel = m_sHeimspiel;
     }
 
     /**
      * Getter for property m_sHeimspiel.
      *
      * @return Value of property m_sHeimspiel.
      */
     public final short getHeimspiel() {
 //        if (m_sHeimspiel < 0) {
 //            try {
 //                //Heim/Ausw abfrage lass ich grad mal sein ;)
 //                final plugins.IMatchKurzInfo[] matches = de.hattrickorganizer.database.DBZugriff.instance()
 //                		.getMatchesKurzInfo(HOVerwaltung.instance().getModel().getBasics().getTeamId());
 //                plugins.IMatchKurzInfo match = null;
 //
 //                for (int i = 0; (matches != null) && (matches.length > i); i++) {
 //                    if ((matches[i].getMatchStatus() == plugins.IMatchKurzInfo.UPCOMING)
 //                        && ((match == null)
 //                        || match.getMatchDateAsTimestamp().after(matches[i].getMatchDateAsTimestamp()))) {
 //                        match = matches[i];
 //                    }
 //                }
 //
 //                m_sHeimspiel = (match.getHeimID() == HOVerwaltung.instance().getModel().getBasics()
 //                                                                 .getTeamId()) ? (short) 1 : (short) 0;
 //            } catch (Exception e) {
 //                m_sHeimspiel = 0;
 //            }
 //        }
         return m_sHeimspiel;
     }
 
     /* Umrechnung von double auf 1-80 int*/
     public final int getIntValue4Rating(double rating) {
         return (int) (((float) (rating - 1) * 4f) + 1);
     }
 
     /**
      * Gibt die Spielerposition zu der Id zur�ck
      *
      * @param id TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final SpielerPosition getPositionById(int id) {
         for (int i = 0; i < m_vPositionen.size(); i++) {
             if (((SpielerPosition) m_vPositionen.get(i)).getId() == id) {
                 return (SpielerPosition) m_vPositionen.get(i);
             }
         }
         //Nix gefunden
         return null;
     }
 
     /**
      * Gibt die Spielerposition zu der SpielerId zur�ck
      *
      * @param spielerid TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final SpielerPosition getPositionBySpielerId(int spielerid) {
         for (int i = 0; i < m_vPositionen.size(); i++) {
             if (((SpielerPosition) m_vPositionen.get(i)).getSpielerId() == spielerid) {
                 return (SpielerPosition) m_vPositionen.get(i);
             }
         }
 
         //Nix gefunden
         return null;
     }
 
     /**
      * Setter for property m_vPositionen.
      *
      * @param m_vPositionen New value of property m_vPositionen.
      */
     public final void setPositionen(Vector<ISpielerPosition> m_vPositionen) {
         this.m_vPositionen = m_vPositionen;
 
         //m_clAssi.setPositionen ( m_vPositionen );
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Accessor
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Getter for property m_vPositionen.
      *
      * @return Value of property m_vPositionen.
      */
     public final Vector<ISpielerPosition> getPositionen() {
         return m_vPositionen;
     }
 
     /**
      * Setzt einen Spieler in eine Position und sorgt daf�r, da� er nicht noch woanders
      * aufgestellt ist
      *
      * @param positionsid TODO Missing Constructuor Parameter Documentation
      * @param spielerid TODO Missing Constructuor Parameter Documentation
      * @param tactic TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final byte setSpielerAtPosition(int positionsid, int spielerid, byte tactic) {
         final SpielerPosition pos = getPositionById(positionsid);
 
         if (pos != null) {
             setSpielerAtPosition(positionsid, spielerid);
             pos.setTaktik(tactic);
 
             return pos.getPosition();
         }
 
        return ISpielerPosition.UNKNOWN;
     }
 
     /**
      * Setzt einen Spieler in eine Position und sorgt daf�r, da� er nicht noch woanders
      * aufgestellt ist
      *
      * @param positionsid TODO Missing Constructuor Parameter Documentation
      * @param spielerid TODO Missing Constructuor Parameter Documentation
      */
     public final void setSpielerAtPosition(int positionsid, int spielerid) {
         //Ist der Spieler noch aufgestellt?
         if (this.isSpielerAufgestellt(spielerid)) {
             //Den Spieler an der alten Position entfernen
             for (int i = 0; i < m_vPositionen.size(); i++) {
                 if (((SpielerPosition) m_vPositionen.get(i)).getSpielerId() == spielerid) {
                     //Spieler entfernen
                     ((SpielerPosition) m_vPositionen.get(i)).setSpielerId(0);
                 }
             }
         }
 
         //Spieler an die neue Position setzten
         final SpielerPosition position = getPositionById(positionsid);
         position.setSpielerId(spielerid);
 
         //Ist der Spielf�hrer und der Kicker noch aufgestellt?
         if (!isSpielerAufgestellt(m_iKapitaen)) {
             //Spielf�hrer entfernen
             m_iKapitaen = 0;
         }
 
         if (!isSpielerAufgestellt(m_iKicker)) {
             //Spielf�hrer entfernen
             m_iKicker = 0;
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param positionsid TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final byte getTactic4PositionID(int positionsid) {
         try {
             return getPositionById(positionsid).getTaktik();
         } catch (Exception e) {
            return plugins.ISpielerPosition.UNKNOWN;
         }
     }
 
     /**
      * Setter for property m_iTacticType.
      *
      * @param m_iTacticType New value of property m_iTacticType.
      */
     public final void setTacticType(int m_iTacticType) {
         this.m_iTacticType = m_iTacticType;
     }
 
     /**
      * Getter for property m_iTacticType.
      *
      * @return Value of property m_iTacticType.
      */
     public final int getTacticType() {
         return m_iTacticType;
     }
 
 
     /////////////////////////////////////////////////////////////////////////////////
     //  INIT
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * stellt das 4-4-2 Grundsystem ein
      */
     private void initPositionen442() {
         if (m_vPositionen != null) {
             m_vPositionen.removeAllElements();
         } else {
             m_vPositionen = new Vector<ISpielerPosition>();
         }
 
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.keeper, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightBack, 0, (byte) 0));
        m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightCentralDefender, 0, (byte) 0));
        m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftCentralDefender, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightWinger, 0, (byte) 0));
        m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightInnerMidfield, 0, (byte) 0));
        m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftInnerMidfield, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftWinger, 0, (byte) 0));
        m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightForward, 0, (byte) 0));
        m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftForward, 0, (byte) 0));
        m_vPositionen.add(new SpielerPosition(ISpielerPosition.substDefender, 0, (byte) 0));
        m_vPositionen.add(new SpielerPosition(ISpielerPosition.substInnerMidfield, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substKeeper, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.substForward, 0, (byte) 0));
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param positionsid TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final plugins.ISpieler getPlayerByPositionID(int positionsid) {
     	return players[positionsid-1];
     }
 
     /**
      * Set a player to a distinct position.
      * @param position
      * @param taktic
      * @param player
      */
 	public void setSpieler(int position, byte taktic, ISpieler player) {
 		if (position>11) {
 			return;
 		}
 		setSpielerAtPosition(position,player.getSpielerID(),taktic);
 		players[position-1]=player;
 
 	}
 
     /**
      * ist der SPieler aufgestellt
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerAufgestellt(int spielerId) {
         return isSpielerAufgestellt(spielerId, m_vPositionen);
     }
 
     /**
      * gibt an ob der Spieler bereits aufgestellt ist auch ReserveBank z�hlt mit
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
 }
