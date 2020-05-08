 // %2598623053:de.hattrickorganizer.model%
 /*
  * Aufstellung.java
  *
  * Created on 20. Mï¿½rz 2003, 14:35
  */
 package de.hattrickorganizer.model;
 
 import gui.UserParameter;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 
 import plugins.ILineUp;
 import plugins.IMatchKurzInfo;
 import plugins.ISpieler;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.database.DBZugriff;
 import de.hattrickorganizer.logik.LineupAssistant;
 import de.hattrickorganizer.prediction.RatingPredictionConfig;
 import de.hattrickorganizer.prediction.RatingPredictionManager;
 import de.hattrickorganizer.tools.HOLogger;
 
 
 /**
  * DOCUMENT ME!
  * 
  * Blaghaid moves it to a 553 model. Lots of changes.
  * This is the model responsible for holding the lineup used in predictions and other things.
  *
  * @author thomas.werth
  */
 public  class Lineup implements plugins.ILineUp {
 	//~ Static fields/initializers -----------------------------------------------------------------
 
     //Systeme
 
     public static final String DEFAULT_NAME = "HO!";
     public static final String DEFAULT_NAMELAST = "HO!LastLineup";
     public static final int NO_HRF_VERBINDUNG = -1;
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** Aufstellungsassistent */
     private LineupAssistant m_clAssi = new LineupAssistant();
 
     /** positions */
     private Vector<ISpielerPosition> m_vPositionen = new Vector<ISpielerPosition>();
 
     /** Attitude */
     private int m_iAttitude;
 
     /** captain */
     private int m_iKapitaen = -1;
 
     /** set pieces take */
     private int m_iKicker = -1;
 
     /** TacticType */
     private int m_iTacticType;
     
     /** PullBackMinute **/
     private int pullBackMinute = 90; // no pull back
 
     /** Home/Away/AwayDerby */
     private short m_sLocation = -1;
 
 	private boolean pullBackOverride;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new Aufstellung object.
      */
     public Lineup() {
         //initPositionen442();
     	initPositionen553();
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Konstruktor
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Creates a new instance of Lineup
      * 
      * Probably up for change with new XML?
      */
 	public Lineup(Properties properties) throws Exception {
 		try {
 			// Positionen erzeugen
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.keeper,
 					Integer.parseInt(properties.getProperty("keeper", "0")), (byte) 0));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightBack,
 					Integer.parseInt(properties.getProperty("rightback", "0")), 
 					Byte.parseByte(properties.getProperty("behrightback", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightCentralDefender,
 					Integer.parseInt(properties.getProperty("insideback1", "0")), 
 					Byte.parseByte(properties.getProperty("behinsideback1","0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftCentralDefender,
 					Integer.parseInt(properties.getProperty("insideback2", "0")),
 					Byte.parseByte(properties.getProperty("behinsideback2", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.middleCentralDefender,
 					Integer.parseInt(properties.getProperty("insideback3", "0")),
 					Byte.parseByte(properties.getProperty("behinsideback3", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftBack, 
 					Integer.parseInt(properties.getProperty("leftback", "0")), 
 					Byte.parseByte(properties.getProperty("behleftback", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightWinger,
 					Integer.parseInt(properties.getProperty("rightwinger", "0")), 
 					Byte.parseByte(properties.getProperty("behrightwinger","0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightInnerMidfield,
 					Integer.parseInt(properties.getProperty("insidemid1", "0")),
 					Byte.parseByte(properties.getProperty("behinsidemid1", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftInnerMidfield,
 					Integer.parseInt(properties.getProperty("insidemid2", "0")), 
 					Byte.parseByte(properties.getProperty("behinsidemid2", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.centralInnerMidfield,
 					Integer.parseInt(properties.getProperty("insidemid3", "0")), 
 					Byte.parseByte(properties.getProperty("behinsidemid3", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftWinger,
 					Integer.parseInt(properties.getProperty("leftwinger", "0")), 
 					Byte.parseByte(properties.getProperty("behleftwinger", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightForward, 
 					Integer.parseInt(properties.getProperty("forward1", "0")),
 					Byte.parseByte(properties.getProperty("behforward1", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftForward, 
 					Integer.parseInt(properties.getProperty("forward2", "0")),
 					Byte.parseByte(properties.getProperty("behforward2", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.centralForward, 
 					Integer.parseInt(properties.getProperty("forward3", "0")),
 					Byte.parseByte(properties.getProperty("behforward3", "0"))));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.substDefender,
 					Integer.parseInt(properties.getProperty("substback", "0")), (byte) 0));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.substInnerMidfield,
 					Integer.parseInt(properties.getProperty("substinsidemid","0")), (byte) 0));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.substWinger,
 					Integer.parseInt(properties.getProperty("substwinger", "0")), (byte) 0));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.substKeeper,
 					Integer.parseInt(properties.getProperty("substkeeper", "0")), (byte) 0));
 			m_vPositionen.add(new SpielerPosition(ISpielerPosition.substForward,
 					Integer.parseInt(properties.getProperty("substforward", "0")), (byte) 0));
 			m_iTacticType = Integer.parseInt(properties.getProperty("tactictype", "0"));
 			m_iAttitude = Integer.parseInt(properties.getProperty("installning", "0"));
 			
			m_clAssi.cleanPositionArray(m_vPositionen);
 		} catch (Exception e) {
 			HOLogger.instance().warning(getClass(), "Aufstellung.<init1>: " + e);
 			HOLogger.instance().log(getClass(), e);
 			m_vPositionen.removeAllElements();
 			initPositionen553();
 		}
 
 		try { // captain + set pieces taker
 			m_iKicker = Integer.parseInt(properties.getProperty("kicker1", "0"));
 			m_iKapitaen = Integer.parseInt(properties.getProperty("captain", "0"));
 		} catch (Exception e) {
 			HOLogger.instance().warning(getClass(), "Aufstellung.<init2>: " + e);
 			HOLogger.instance().log(getClass(), e);
 		}
 	}
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * get the tactic level for AiM/AoW
      *
      * @return tactic level
      */
     public final float getTacticLevelAimAow() {
         return Math.max(1,
                 new RatingPredictionManager(this, 
                 		HOVerwaltung.instance().getModel().getTeam(), 
                 		(short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), 
                 		RatingPredictionConfig.getInstance() )
         		.getTacticLevelAowAim());
     }
 
     /**
      * get the tactic level for counter
      *
      * @return tactic level
      */
     public final float getTacticLevelCounter() {
         return Math.max(1,
         		new RatingPredictionManager(this, 
         				HOVerwaltung.instance().getModel().getTeam(), 
         				(short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), 
         				RatingPredictionConfig.getInstance() )
         		.getTacticLevelCounter() );
     }
 
     /**
      * get the tactic level for pressing
      *
      * @return tactic level
      */
     public final float getTacticLevelPressing() {
         return Math.max(1,
         		new RatingPredictionManager(this, 
         				HOVerwaltung.instance().getModel().getTeam(), 
         				(short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), 
         				RatingPredictionConfig.getInstance() )
         		.getTacticLevelPressing() );
     }
 
     /**
      * get the tactic level for Long Shots
      *
      * @return tactic level
      */
     public final float getTacticLevelLongShots() {
         return Math.max(1,
                 new RatingPredictionManager(this, 
                 		HOVerwaltung.instance().getModel().getTeam(), 
                 		(short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), 
                 		RatingPredictionConfig.getInstance() )
         		.getTacticLevelLongShots());
     }
 
     //    /**
     //     * Calculate the HO-Index for playing creatively
     //     * @author Thorsten Dietz
     //     * @param Vector spieler
     //     * @return tacticStrength
     //     */
     //    public float getCreativeSTK(Vector spieler){
     //    	float strength = 0f;
     //    	boolean isHeadmanInLineUp	= 	false;
     //    	for (int i = ISpielerPosition.rightBack; i < ISpielerPosition.beginnReservere; i++) {
     //            ISpieler player = HOVerwaltung.instance().getModel().getAufstellung()
     //                                          .getPlayerByPositionID(i);
     //
     //            if(player != null && player.getSpezialitaet() == ISpieler.KOPFBALLSTARK)
     //            	isHeadmanInLineUp = true;
     //            byte tactic = HOVerwaltung.instance().getModel().getAufstellung().getTactic4PositionID(i);
     //            strength+=PlayerHelper.getSpecialEventEffect(player,i,tactic);
     //
     //
     //    	}
     //    	if(isHeadmanInLineUp)
     //    		strength++;
     //    	return strength;
     //    }
 
     /**
      * errechnet die Gesamt AW Stï¿½rke
      * 
      * Calculates the total star rating for defense
      *
      * This is CA-rating?
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getAWTeamStk(Vector<ISpieler> spieler, boolean mitForm) {
         float stk = 0.0f;
         stk += calcTeamStk(spieler, ISpielerPosition.CENTRAL_DEFENDER, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.CENTRAL_DEFENDER_OFF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.CENTRAL_DEFENDER_TOWING, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.BACK, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.BACK_OFF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.BACK_DEF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.BACK_TOMID, mitForm);
 
         return de.hattrickorganizer.tools.Helper.round(stk, 1);
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
      * bestimmt automatisch den Kapitï¿½n
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      */
     public final void setAutoKapitaen(Vector<ISpieler> spieler) {
         Spieler player = null;
         float maxValue = -1;
 
         if (spieler == null) {
             spieler = HOVerwaltung.instance().getModel().getAllSpieler();
         }
 
         for (int i = 0; (spieler != null) && (i < spieler.size()); i++) {
             player = (Spieler) spieler.elementAt(i);
 
             if (m_clAssi.isSpielerInAnfangsElf(player.getSpielerID(), m_vPositionen)) {
             	int curPlayerId = player.getSpielerID();
             	float curCaptainsValue = HOVerwaltung.instance().getModel().getAufstellung().getAverageExperience(curPlayerId);
                 if (maxValue < curCaptainsValue) {
                     maxValue = curCaptainsValue;
                     m_iKapitaen = curPlayerId;
                 }
             }
         }
     }
 
     /**
      * bestimmt den Standard schï¿½tzen
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      */
     public final void setAutoKicker(Vector<ISpieler> spieler) {
         int maxStandard = -1;
         int form = -1;
         Spieler player = null;
 
         if (spieler == null) {
             spieler = HOVerwaltung.instance().getModel().getAllSpieler();
         }
 
         for (int i = 0; (spieler != null) && (i < spieler.size()); i++) {
             player = (Spieler) spieler.elementAt(i);
 
             if (m_clAssi.isSpielerInAnfangsElf(player.getSpielerID(), m_vPositionen)) {
                 if (player.getStandards() > maxStandard) {
                     maxStandard = player.getStandards();
                     form = player.getForm();
                     m_iKicker = player.getSpielerID();
                 } else if ((player.getStandards() == maxStandard) && (form < player.getForm())) {
                     maxStandard = player.getStandards();
                     form = player.getForm();
                     m_iKicker = player.getSpielerID();
                 }
             }
         }
     }
 
     /**
      * Get the average experience of all players in lineup
      * using the formula from kopsterkespits:
      * teamxp = ((sum of teamxp + xp of captain)/12)*(1-(7-leadership of captain)*5%)
      */
     public final float getAverageExperience() {
     	return getAverageExperience(0);
     }
     /**
      * Get the average experience of all players in lineup using a specific captain
      * 
      * @param captainsId use this player as captain (<= 0 for current captain)
      */
     public final float getAverageExperience(int captainsId) {
     	float value = 0;
     	try {
     		Spieler pl = null;
     		Spieler captain = null;
     		Vector<ISpieler> players = HOVerwaltung.instance().getModel().getAllSpieler();
 
     		for (int i = 0; (players != null) && (i < players.size()); i++) {
     			pl = (Spieler) players.elementAt(i);
     			if (m_clAssi.isSpielerInAnfangsElf(pl.getSpielerID(), m_vPositionen)) {
     				value += pl.getErfahrung();
     				if (captainsId > 0) {
     					if (captainsId == pl.getSpielerID()) {
         					captain = pl;
     					}
     				} else if (m_iKapitaen == pl.getSpielerID()) {
     					captain = pl;
     				}
     			}
     		}
     		if (captain != null) {
     			value = ((float)(value + captain.getErfahrung())/12) * (1f-(float)(7-captain.getFuehrung())*0.05f);
     		} else {
     			HOLogger.instance().log(getClass(), "Can't calc average experience, captain not set.");
     			value = -1f;
     		}
     	} catch (Exception e) {
     		HOLogger.instance().error(getClass(), e);
     	}
         return value;
     }
 
     /**
      * errechnet anhand der aktuellen Aufstellung die besten Elferschï¿½tzen
      *
      * @return TODO Missing Return Method Documentation
      */
     public final int[] getBestElferKicker() {
         return m_clAssi.setElferKicker(HOVerwaltung.instance().getModel().getAllSpieler(),
                                        m_vPositionen);
     }
 
     /**
      * Predicts Central Attack-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getCentralAttackRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 	        //ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 	        double value = Math.max(1, rpManager.getCentralAttackRatings());
 	        if (value>1) {
 	        	value += UserParameter.instance().middleAttackOffset;
 	        }
 	        return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * Predicts cd-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getCentralDefenseRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 	        //ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getCentralDefenseRatings());
 			if (value>1) {
 				value += UserParameter.instance().middleDefenceOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * errechnet die Gesamt Stärke
      * Total star rating.
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getGesamtStaerke(Vector<ISpieler> spieler, boolean mitForm) {
         return de.hattrickorganizer.tools.Helper.round(getTWTeamStk(spieler, mitForm)
                                                        + getAWTeamStk(spieler, mitForm)
                                                        + getMFTeamStk(spieler, mitForm)
                                                        + getSTTeamStk(spieler, mitForm), 1);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final int getHATStats() {
 		int sum;
 		final int MFfactor = 3;
 
 		sum = HTfloat2int(getMidfieldRating()) * MFfactor;
 
 		sum += HTfloat2int(getLeftDefenseRating());
 		sum += HTfloat2int(getCentralDefenseRating());
 		sum += HTfloat2int(getRightDefenseRating());
 
 		sum += HTfloat2int(getLeftAttackRating());
 		sum += HTfloat2int(getCentralAttackRating());
 		sum += HTfloat2int(getRightAttackRating());
 
 		return sum;
     }
 
 	public void updateRatingPredictionConfig() {
 		int vt = atLeastOne(getAnzInnenverteidiger());
 		int im = atLeastOne(getAnzInneresMittelfeld());
 		int st = atLeastOne(getAnzSturm());
 		String predictionName = vt + "D+" + im + "M+" + st + "F";
 		RatingPredictionConfig.setInstancePredictionName(predictionName);
 	}
 
 	private int atLeastOne(int count) {
 		return count == 0 ? 1 : count;
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
      * Predicts LeftAttack-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getLeftAttackRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getLeftAttackRatings());
 			if (value>1) {
 				value += UserParameter.instance().leftAttackOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * Predicts ld-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getLeftDefenseRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getLeftDefenseRatings());
 			if (value>1) {
 				value += UserParameter.instance().leftDefenceOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
 	public final float getLoddarStats() {
 		LoddarStatsCalculator calculator = new LoddarStatsCalculator();
 		calculator.setRatings(getMidfieldRating(), getRightDefenseRating(),
 				getCentralDefenseRating(), getLeftDefenseRating(),
 				getRightAttackRating(), getCentralAttackRating(),
 				getLeftAttackRating());
 		calculator.setTactics(getTacticType(), getTacticLevelAimAow(),
 				getTacticLevelCounter());
 		return calculator.calculate();
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param x TODO Missing Method Parameter Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public static final int HTfloat2int(double x) {
 		// convert reduced float rating (1.00....20.99) to original integer HT rating (1...80)
 		// one +0.5 is because of correct rounding to integer
 		return (int) (((x - 1.0f) * 4.0f) + 1.0f);
 	}
     /**
      * errechnet die Gesamt MF Stï¿½rke
      * Midfield and winger total star rating.
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getMFTeamStk(Vector<ISpieler> spieler, boolean mitForm) {
         float stk = 0.0f;
         stk += calcTeamStk(spieler, ISpielerPosition.MIDFIELDER, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.WINGER, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.MIDFIELDER_OFF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.WINGER_OFF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.MIDFIELDER_DEF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.WINGER_DEF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.MIDFIELDER_TOWING, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.WINGER_TOMID, mitForm);
 
         return de.hattrickorganizer.tools.Helper.round(stk, 1);
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Ratings
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Predicts MF-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getMidfieldRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getMFRatings());
 			if (value>1) {
 				value += UserParameter.instance().midfieldOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * Get the short name for a fomation constant.
      */
     public static String getNameForSystem(byte system) {
         String name;
 
         switch (system) {
             case SYS_451:
                 name = "4-5-1";
                 break;
 
             case SYS_352:
                 name = "3-5-2";
                 break;
 
             case SYS_442:
                 name = "4-4-2";
                 break;
 
             case SYS_343:
                 name = "3-4-3";
                 break;
 
             case SYS_433:
                 name = "4-3-3";
                 break;
 
             case SYS_532:
                 name = "5-3-2";
                 break;
 
             case SYS_541:
                 name = "5-4-1";
                 break;
                 
             case SYS_523:
                 name = "5-2-3";
                 break;
                 
             case SYS_550:
                 name = "5-5-0";
                 break;
 
             case SYS_253:
                 name = "2-5-3";
                 break;
 
             default:
                 name = HOVerwaltung.instance().getLanguageString("Unbestimmt");
                 break;
         }
 
         return name;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param positionsid TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final byte getEffectivePos4PositionID(int positionsid) {
         try {
             return getPositionById(positionsid).getPosition();
         } catch (Exception e) {
         	HOLogger.instance().error(getClass(),"getEffectivePos4PositionID: " + e);
             return ISpielerPosition.UNKNOWN;
         }
     }
 
     /**
      * Setter for property m_sHeimspiel.
      * @param m_sHeimspiel New value of property m_sHeimspiel.
      */
     public final void setHeimspiel(short location) {
         this.m_sLocation = location;
     }
 
     /**
      * Get the location constant for the match (home/away/awayderby)
      * TODO: determine away derby (aik) !!!
      * @return the location constant for the match
      */
 	public final short getHeimspiel() {
 		if (m_sLocation < 0) {
 			try {
 				final int teamId = HOVerwaltung.instance().getModel().getBasics().getTeamId();
 				final IMatchKurzInfo[] matches = DBZugriff.instance().getMatchesKurzInfo(teamId, IMatchKurzInfo.UPCOMING);
 				final List<IMatchKurzInfo> sMatches = removeBuggyMatches(matches);
 				
 				IMatchKurzInfo match = null;
 				if (sMatches != null) {
 					for (IMatchKurzInfo m : sMatches) {
 						if ((m.getMatchStatus() == IMatchKurzInfo.UPCOMING)
 								&& ((match == null) || match.getMatchDateAsTimestamp().after(m.getMatchDateAsTimestamp()))) {
 							match = m;
 						}
 					}
 				}
 
 				if (match != null) {
 					m_sLocation = (match.getHeimID() == teamId) ? (short) 1 : (short) 0;
 				} else {
 					HOLogger.instance().debug(getClass(), "no match to determine location");
 				}
 			} catch (Exception e) {
 				HOLogger.instance().error(getClass(), "getHeimspiel: " + e);
 				m_sLocation = 0;
 			}
 		}
 
 		return m_sLocation;
 	}
 	
 	/**
 	 * For some reason we have users with old "upcoming" matches which break the location determination.
 	 * This method removes all matches that are more than 8 days older than the previous 'upcoming' match.
 	 */
 	private List<IMatchKurzInfo> removeBuggyMatches(IMatchKurzInfo[] inMatches) {
 		final List<IMatchKurzInfo> matches = new ArrayList<IMatchKurzInfo>();
 		if (inMatches != null && inMatches.length > 1) {
 			for (IMatchKurzInfo m : inMatches) {
 				matches.add(m);
 			}
 			
 			Collections.sort(matches, new Comparator<IMatchKurzInfo>() {
 				public int compare(IMatchKurzInfo o1, IMatchKurzInfo o2) {
 					return 0-(o1.getMatchDateAsTimestamp().compareTo(o2.getMatchDateAsTimestamp()));
 				}
 			});
 			
 			Timestamp checkDate = null;
 			for (Iterator<IMatchKurzInfo> i=matches.iterator(); i.hasNext(); ) {
 				IMatchKurzInfo m = i.next();
 				if (checkDate == null) {
 					checkDate = m.getMatchDateAsTimestamp();
 					continue;
 				}
 				if (m.getMatchDateAsTimestamp().getTime() < (checkDate.getTime()-8*24*60*60*1000L)) { // older than 8 days
 					HOLogger.instance().warning(getClass(), "Old match with status UPCOMING! " + m.getMatchID() + " from " + m.getMatchDate());
 					i.remove();
 				} else {
 					checkDate = m.getMatchDateAsTimestamp();
 				}
 			}
 		}
 		return matches;
 	}
 
     /* Umrechnung von double auf 1-80 int*/
     public final int getIntValue4Rating(double rating) {
         return (int) (((float) (rating - 1) * 4f) + 1);
     }
 
     /**
      * Get the player object by position id.
      */
     public ISpieler getPlayerByPositionID(int positionId) {
         try {
 			return HOVerwaltung.instance().getModel().getSpieler(getPositionById(positionId).getSpielerId());
         } catch (Exception e) {
 			HOLogger.instance().error(getClass(), "getPlayerByPositionID(" + positionId + "): " + e);
             return null;
         }
     }
 
     /**
      * Get the position object by position id. 
      */
     public final SpielerPosition getPositionById(int id) {
         for (int i = 0; i < m_vPositionen.size(); i++) {
             if (((SpielerPosition) m_vPositionen.get(i)).getId() == id) {
                 return (SpielerPosition) m_vPositionen.get(i);
             }
         }
         return null;
     }
 
     /**
      * Get the position object by player id.
      */
 	public final SpielerPosition getPositionBySpielerId(int playerid) {
 		for (int i = 0; i < m_vPositionen.size(); i++) {
 			if (((SpielerPosition) m_vPositionen.get(i)).getSpielerId() == playerid) {
 				return (SpielerPosition) m_vPositionen.get(i);
 			}
 		}
 		return null;
 	}
 
     /**
      * Setter for property m_vPositionen.
      *
      * @param m_vPositionen New value of property m_vPositionen.
      */
     public final void setPositionen(Vector<ISpielerPosition> m_vPositionen) {
 
     	this.m_vPositionen = m_vPositionen;
    	m_clAssi.cleanPositionArray(this.m_vPositionen);
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
      * Predicts Right-Attack-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getRightAttackRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getRightAttackRatings());
 			if (value>1) {
 				value += UserParameter.instance().rightAttackOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * Predicts rd-Rating
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getRightDefenseRating() {
     	if (HOVerwaltung.instance().getModel() != null && HOVerwaltung.instance().getModel().getID() != -1) {
 	        final RatingPredictionManager rpManager = new RatingPredictionManager(this, HOVerwaltung.instance().getModel().getTeam(), (short) HOVerwaltung.instance().getModel().getTrainer().getTrainerTyp(), RatingPredictionConfig.getInstance() );
 
 			//ruft konvertiertes Plugin ( in Manager ) auf und returned den Wert
 			double value = Math.max(1, rpManager.getRightDefenseRatings());
 			if (value>1) {
 				value += UserParameter.instance().rightDefenceOffset;
 			}
 			return value;
     	} else {
     		return 0.0d;
     	}
     }
 
     /**
      * errechnet die Gesamt ST Stï¿½rke
      * Team star rating for attackers
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getSTTeamStk(Vector<ISpieler> spieler, boolean mitForm) {
         float stk = 0.0f;
         stk += calcTeamStk(spieler, ISpielerPosition.FORWARD, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.FORWARD_DEF, mitForm);
         stk += calcTeamStk(spieler, ISpielerPosition.FORWARD_TOWING, mitForm);
 
         return de.hattrickorganizer.tools.Helper.round(stk, 1);
     }
 
     /**
      * Setzt einen Spieler in eine Position und sorgt dafï¿½r, daï¿½ er nicht noch woanders
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
      * Setzt einen Spieler in eine Position und sorgt dafï¿½r, daï¿½ er nicht noch woanders
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
 
         //Ist der Spielfï¿½hrer und der Kicker noch aufgestellt?
         if (!isSpielerAufgestellt(m_iKapitaen)) {
             //Spielfï¿½hrer entfernen
             m_iKapitaen = 0;
         }
 
         if (!isSpielerAufgestellt(m_iKicker)) {
             //Spielfï¿½hrer entfernen
             m_iKicker = 0;
         }
     }
 
     /**
      * ist der SPieler aufgestellt
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerAufgestellt(int spielerId) {
         return m_clAssi.isSpielerAufgestellt(spielerId, m_vPositionen);
     }
 
     /**
      * spielt der Spieler von Beginn an
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerInAnfangsElf(int spielerId) {
         return m_clAssi.isSpielerInAnfangsElf(spielerId, m_vPositionen);
     }
 
     /**
      * sitzt der SPierl auf der Bank
      *
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final boolean isSpielerInReserve(int spielerId) {
         return (m_clAssi.isSpielerAufgestellt(spielerId, m_vPositionen)
                && !m_clAssi.isSpielerInAnfangsElf(spielerId, m_vPositionen));
     }
 
     /**
      * Gibt den Namen fï¿½r das System zurï¿½ck
      *
      * @param system TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final String getSystemName(byte system) {
         return getNameForSystem(system);
     }
 
     /**
      * errechnet die Gesamt TW Stï¿½rke
      * 
      * Star rating for the keeper.
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getTWTeamStk(Vector<ISpieler> spieler, boolean mitForm) {
         return calcTeamStk(spieler, ISpielerPosition.KEEPER, mitForm);
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
         	HOLogger.instance().error(getClass(),"getTactic4PositionID: " + e);
             return plugins.ISpielerPosition.UNKNOWN;
         }
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    STK Funcs
     /////////////////////////////////////////////////////////////////////////////////
     public final float getTacticLevel(int type) {
         float value = 0.0f;
 
         switch (type) {
             case plugins.IMatchDetails.TAKTIK_PRESSING:
                 value = getTacticLevelPressing();
                 break;
 
             case plugins.IMatchDetails.TAKTIK_KONTER:
                 value = getTacticLevelCounter();
                 break;
 
             case plugins.IMatchDetails.TAKTIK_MIDDLE:
             case plugins.IMatchDetails.TAKTIK_WINGS:
                 value = getTacticLevelAimAow();
                 break;
 
             case plugins.IMatchDetails.TAKTIK_LONGSHOTS:
                 value = getTacticLevelLongShots();
                 break;
         }
 
         return value;
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
 
     /**
      * Get the formation xp for the current formation.
      */
     public final int getTeamErfahrung4AktuellesSystem() {
         int erfahrung = -1;
 
         switch (ermittelSystem()) {
             case SYS_MURKS:
                 erfahrung = -1;
                 break;
 
             case SYS_451:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung451();
                 break;
 
             case SYS_352:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung352();
                 break;
 
             case SYS_442:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getFormationExperience442();
                 break;
 
             case SYS_343:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung343();
                 break;
 
             case SYS_433:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung433();
                 break;
 
             case SYS_532:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung532();
                 break;
 
             case SYS_541:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getErfahrung541();
                 break;
                 
             case SYS_523:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getFormationExperience523();
                 break;
                 
             case SYS_550:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getFormationExperience550();
                 break;
                 
             case SYS_253:
                 erfahrung = HOVerwaltung.instance().getModel().getTeam().getFormationExperience253();
                 break;
 
         }
 
         return erfahrung;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param name TODO Missing Method Parameter Documentation
      */
     public final void AufstellungsSystemLoeschen(String name) {
         de.hattrickorganizer.database.DBZugriff.instance().deleteSystem(NO_HRF_VERBINDUNG, name);
     }
 
     /**
      * prï¿½ft ob die aufgestellten Spieler noch implements KAder sind
      */
     public final void checkAufgestellteSpieler() {
         SpielerPosition pos = null;
 
         for (int i = 0; (m_vPositionen != null) && (i < m_vPositionen.size()); i++) {
             pos = (SpielerPosition) m_vPositionen.elementAt(i);
 
             //existiert Spieler noch ?
             if ((HOVerwaltung.instance().getModel() != null)
                 && (HOVerwaltung.instance().getModel().getSpieler(pos.getSpielerId()) == null)) {
                 //nein dann zuweisung aufheben
                 pos.setSpielerId(0);
             }
         }
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Aktions Funcs
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * erstellt die automatische Aufstellung
      *
      * @param spieler die aufszustellenden Spieler
      * @param reihenfolge Reihenfolge in der die Mannschaftsteile besetzt werden sollen
      * @param mitForm Formberï¿½cksichtigung
      * @param idealPosFirst IdealPosition berï¿½cksichtigen ?
      * @param ignoreVerletzung TODO Missing Constructuor Parameter Documentation
      * @param ignoreSperren TODO Missing Constructuor Parameter Documentation
      * @param wetterBonus TODO Missing Constructuor Parameter Documentation
      * @param wetter TODO Missing Constructuor Parameter Documentation
      */
     public final void doAufstellung(Vector<ISpieler> spieler, byte reihenfolge, boolean mitForm,
                                     boolean idealPosFirst, boolean ignoreVerletzung,
                                     boolean ignoreSperren, float wetterBonus, int wetter) {
         m_clAssi.doAufstellung(m_vPositionen, spieler, reihenfolge, mitForm, idealPosFirst,
                                ignoreVerletzung, ignoreSperren, wetterBonus, wetter);
         setAutoKicker(null);
         setAutoKapitaen(null);
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Debug Funcs
     /////////////////////////////////////////////////////////////////////////////////
     public final void dump() {
         HOLogger.instance().log(getClass(),"Std-aufstellung");
         dumpValues();
         HOLogger.instance().log(getClass(),"idelaPos");
 
         //3-5-2
         initPositionen352();
 
         //353 mit idealpos first
         doAufstellung(HOVerwaltung.instance().getModel().getAllSpieler(),
                       ILineUp.MF_AW_ST, true, true, false,
                       false, 0.2f, ISpieler.LEICHTBEWOELKT);
 
         //dumpen
         dumpValues();
         HOLogger.instance().log(getClass(),"Ohne idelaPos");
 
         //3-5-2 aufgestellte SPieler leeren
         resetAufgestellteSpieler();
 
         //353 mit idealpos first
         doAufstellung(HOVerwaltung.instance().getModel().getAllSpieler(),
                       ILineUp.MF_AW_ST, true, false,
                       false, false, 0.2f, ISpieler.LEICHTBEWOELKT);
         dumpValues();
     }
 
     /**
      * Cloned diese Aufstellung, erzeugt neues Objekt
      *
      * @return TODO Missing Return Method Documentation
      */
     public final Lineup duplicate() {
         final java.util.Properties properties = new java.util.Properties();
         Lineup clone = null;
 
         try {
             properties.setProperty("keeper",
                                    getPositionById(ISpielerPosition.keeper).getSpielerId() + "");
             properties.setProperty("rightback",
                                    getPositionById(ISpielerPosition.rightBack).getSpielerId() + "");
             properties.setProperty("insideback1",
                                    getPositionById(ISpielerPosition.rightCentralDefender).getSpielerId()
                                    + "");
             properties.setProperty("insideback2",
                                    getPositionById(ISpielerPosition.leftCentralDefender).getSpielerId()
                                    + "");
             properties.setProperty("insideback3",
                     getPositionById(ISpielerPosition.middleCentralDefender).getSpielerId()
                     + "");
             properties.setProperty("leftback",
                                    getPositionById(ISpielerPosition.leftBack).getSpielerId() + "");
             properties.setProperty("rightwinger",
                                    getPositionById(ISpielerPosition.rightWinger).getSpielerId()
                                    + "");
             properties.setProperty("insidemid1",
                                    getPositionById(ISpielerPosition.rightInnerMidfield).getSpielerId() + "");
             properties.setProperty("insidemid2",
                                    getPositionById(ISpielerPosition.leftInnerMidfield).getSpielerId() + "");
             properties.setProperty("insidemid3",
                     getPositionById(ISpielerPosition.centralInnerMidfield).getSpielerId() + "");
             properties.setProperty("leftwinger",
                                    getPositionById(ISpielerPosition.leftWinger).getSpielerId() + "");
             properties.setProperty("forward1",
                                    getPositionById(ISpielerPosition.rightForward).getSpielerId() + "");
             properties.setProperty("forward2",
                                    getPositionById(ISpielerPosition.leftForward).getSpielerId() + "");
             properties.setProperty("forward3",
                     getPositionById(ISpielerPosition.centralForward).getSpielerId() + "");
             properties.setProperty("substback",
                                    getPositionById(ISpielerPosition.substDefender).getSpielerId() + "");
             properties.setProperty("substinsidemid",
                                    getPositionById(ISpielerPosition.substInnerMidfield).getSpielerId()
                                    + "");
             properties.setProperty("substwinger",
                                    getPositionById(ISpielerPosition.substWinger).getSpielerId()
                                    + "");
             properties.setProperty("substkeeper",
                                    getPositionById(ISpielerPosition.substKeeper).getSpielerId()
                                    + "");
             properties.setProperty("substforward",
                                    getPositionById(ISpielerPosition.substForward).getSpielerId()
                                    + "");
 
             properties.setProperty("behrightback",
                                    getPositionById(ISpielerPosition.rightBack).getTaktik() + "");
             properties.setProperty("behinsideback1",
                                    getPositionById(ISpielerPosition.rightCentralDefender).getTaktik() + "");
             properties.setProperty("behinsideback2",
                                    getPositionById(ISpielerPosition.leftCentralDefender).getTaktik() + "");
             properties.setProperty("behinsideback3",
                     getPositionById(ISpielerPosition.middleCentralDefender).getTaktik() + "");
             properties.setProperty("behleftback",
                                    getPositionById(ISpielerPosition.leftBack).getTaktik() + "");
             properties.setProperty("behrightwinger",
                                    getPositionById(ISpielerPosition.rightWinger).getTaktik() + "");
             properties.setProperty("behinsidemid1",
                                    getPositionById(ISpielerPosition.rightInnerMidfield).getTaktik() + "");
             properties.setProperty("behinsidemid2",
                                    getPositionById(ISpielerPosition.leftInnerMidfield).getTaktik() + "");
             properties.setProperty("behinsidemid3",
                     getPositionById(ISpielerPosition.centralInnerMidfield).getTaktik() + "");
             properties.setProperty("behleftwinger",
                                    getPositionById(ISpielerPosition.leftWinger).getTaktik() + "");
             properties.setProperty("behforward1",
                                    getPositionById(ISpielerPosition.rightForward).getTaktik() + "");
             properties.setProperty("behforward2",
                                    getPositionById(ISpielerPosition.leftForward).getTaktik() + "");
             properties.setProperty("behforward3",
                     getPositionById(ISpielerPosition.centralForward).getTaktik() + "");
 
 
             properties.setProperty("kicker1", getKicker() + "");
             properties.setProperty("captain", getKapitaen() + "");
 
             properties.setProperty("tactictype", getTacticType() + "");
             properties.setProperty("installning", getAttitude() + "");
 
             clone = new Lineup(properties);
             clone.setHeimspiel(getHeimspiel());
             clone.setPullBackMinute(getPullBackMinute());
             clone.setPullBackOverride(isPullBackOverride());
         } catch (Exception e) {
             HOLogger.instance().error(getClass(),"Aufstellung.duplicate: " + e);
         }
 
         return clone;
     }
 
     /**
      * Determinates the current formation.
      */
     public final byte ermittelSystem() {
         final int abw = getAnzAbwehr();
         final int mf = getAnzMittelfeld();
 
         //int st  =   getAnzSturm();
         if (abw == 2) {
         	//253
         	if (mf == 5) {
         		return ILineUp.SYS_253;
         	}
         	//MURKS
             else {
                 return SYS_MURKS;
             }
         } else if (abw == 3) {
         	//343
             if (mf == 4) {
                 return SYS_343;
             } //352
             else if (mf == 5) {
                 return SYS_352;
             }
             //MURKS
             else {
                 return SYS_MURKS;
             }
         } else if (abw == 4) {
             //433
             if (mf == 3) {
                 return SYS_433;
             } //442
             else if (mf == 4) {
                 return SYS_442;
             } //451
             else if (mf == 5) {
                 return SYS_451;
             }
             //MURKS
             else {
                 return SYS_MURKS;
             }
         } else if (abw == 5) {
             //532
             if (mf == 3) {
                 return SYS_532;
             } //541
             else if (mf == 4) {
                 return SYS_541;
             } //523
             else if (mf == 2) {
                 return ILineUp.SYS_523;
             } //550
             else if (mf == 5) {
                 return ILineUp.SYS_550;
             }
             //MURKS
             else {
                 return SYS_MURKS;
             }
         } //MURKS
         else {
             return SYS_MURKS;
         }
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public final void flipSide() {
         Vector<ISpielerPosition> tmp = new Vector<ISpielerPosition>(m_vPositionen);
      
         // Ugly, ugly, horror. I really don't enjoy depending on position in Vector. 
         // And it don't work in 553 of course, and fix should be based on position contents
         
         m_vPositionen.removeAllElements();
         m_vPositionen.add(tmp.get(0));
         m_vPositionen.add(swap(tmp.get(1), tmp.get(4)));
         m_vPositionen.add(swap(tmp.get(2), tmp.get(3)));
         m_vPositionen.add(swap(tmp.get(3), tmp.get(2)));
         m_vPositionen.add(swap(tmp.get(4), tmp.get(1)));
         m_vPositionen.add(swap(tmp.get(5), tmp.get(8)));
         m_vPositionen.add(swap(tmp.get(6), tmp.get(7)));
         m_vPositionen.add(swap(tmp.get(7), tmp.get(6)));
         m_vPositionen.add(swap(tmp.get(8), tmp.get(5)));
         m_vPositionen.add(swap(tmp.get(9), tmp.get(10)));
         m_vPositionen.add(swap(tmp.get(10), tmp.get(9)));
         m_vPositionen.add(tmp.get(11));
         m_vPositionen.add(tmp.get(12));
         m_vPositionen.add(tmp.get(13));
         m_vPositionen.add(tmp.get(14));
         m_vPositionen.add(tmp.get(15));
         tmp = null;
     }
 
     /**
      * lï¿½dt dei Aufstellung
      *
      * @param name TODO Missing Constructuor Parameter Documentation
      */
     public final void load(String name) {
         final Lineup temp = de.hattrickorganizer.database.DBZugriff.instance().getAufstellung(NO_HRF_VERBINDUNG,
                                                                                                    name);
         m_vPositionen = null;
         m_vPositionen = temp.getPositionen();
        m_clAssi.cleanPositionArray(m_vPositionen);
         m_iKicker = temp.getKicker();
         m_iKapitaen = temp.getKapitaen();
     }
 
     /**
      * lï¿½dt dei Aufstellung
      */
     public final void load4HRF() {
         final Lineup temp = de.hattrickorganizer.database.DBZugriff.instance().getAufstellung(HOVerwaltung.instance()
                                                                                                                .getModel()
                                                                                                                .getID(),
                                                                                                    "HRF");
         m_vPositionen = null;
         m_vPositionen = temp.getPositionen();
        m_clAssi.cleanPositionArray(m_vPositionen);
         m_iKicker = temp.getKicker();
         m_iKapitaen = temp.getKapitaen();
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Datenbank Funcs
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Lï¿½dt ein System aus der Datenbank ein
      *
      * @param name TODO Missing Constructuor Parameter Documentation
      */
     public final void loadAufstellungsSystem(String name) {
         //Aius DB laden
         m_vPositionen = de.hattrickorganizer.database.DBZugriff.instance().getSystemPositionen(NO_HRF_VERBINDUNG,
                                                                                                 name);
         checkAufgestellteSpieler();
        m_clAssi.cleanPositionArray(m_vPositionen);
     }
 
     /////////////////////////////////////////////////////////////////////////////////
     //    Helper Funcs
     /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * lï¿½scht die aufgestellten Spieler
      */
     public final void resetAufgestellteSpieler() {
         m_clAssi.resetPositionsbesetzungen(m_vPositionen);
        m_clAssi.cleanPositionArray(m_vPositionen);
     }
 
     /**
      * lï¿½scht die Reservespieler
      */
     public final void resetReserveBank() {
         //Nur Reservespieler
         final Vector<ISpielerPosition> vReserve = new Vector<ISpielerPosition>();
 
         for (int i = 0; i < m_vPositionen.size(); i++) {
             if (((SpielerPosition) m_vPositionen.get(i)).getId() >= ISpielerPosition.startReserves) {
                 vReserve.add(m_vPositionen.get(i));
             }
         }
 
         m_clAssi.resetPositionsbesetzungen(vReserve);
     }
 
     /**
      * spiechert die gesamte Aufstellung unter dem angegebenen Namen ab
      *
      * @param name TODO Missing Constructuor Parameter Documentation
      */
     public final void save(String name) {
         de.hattrickorganizer.database.DBZugriff.instance().saveAufstellung(NO_HRF_VERBINDUNG, this,
                                                                            name);
     }
 
     /**
      * spiechert die gesamte Aufstellung unter dem angegebenen Namen ab
      */
     public final void save4HRF() {
         de.hattrickorganizer.database.DBZugriff.instance().saveAufstellung(HOVerwaltung.instance()
                                                                                        .getModel()
                                                                                        .getID(),
                                                                            this, "HRF");
     }
 
     /**
      * speichert das aktuelle System unter dem "name" in der DB
      *
      * @param name TODO Missing Constructuor Parameter Documentation
      */
     public final void saveAufstellungsSystem(String name) {
         de.hattrickorganizer.database.DBZugriff.instance().saveSystemPositionen(NO_HRF_VERBINDUNG,
                                                                                 m_vPositionen, name);
     }
 
     /**
      * berechnet Anzahl Abwehr im System
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAnzAbwehr() {
         int anzahl = 0;
 
         anzahl += getAnzPosImSystem(ISpielerPosition.BACK);
         anzahl += getAnzPosImSystem(ISpielerPosition.BACK_TOMID);
         anzahl += getAnzPosImSystem(ISpielerPosition.BACK_OFF);
         anzahl += getAnzPosImSystem(ISpielerPosition.BACK_DEF);
 
         return anzahl+getAnzInnenverteidiger();
     }
 
     /**
      * berechnet Anzahl Innenverteidiger im System
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAnzInnenverteidiger() {
     	int anzahl = 0;
     	
     	anzahl += getAnzPosImSystem(ISpielerPosition.CENTRAL_DEFENDER);
     	anzahl += getAnzPosImSystem(ISpielerPosition.CENTRAL_DEFENDER_TOWING);
     	anzahl += getAnzPosImSystem(ISpielerPosition.CENTRAL_DEFENDER_OFF);
     	
     	return anzahl;
     }
     
     /**
      * berechnet Anzahl Abwehr im System
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAnzMittelfeld() {
         int anzahl = 0;
 
         anzahl += getAnzPosImSystem(ISpielerPosition.WINGER);
         anzahl += getAnzPosImSystem(ISpielerPosition.WINGER_TOMID);
         anzahl += getAnzPosImSystem(ISpielerPosition.WINGER_OFF);
         anzahl += getAnzPosImSystem(ISpielerPosition.WINGER_DEF);
 
         return anzahl + getAnzInneresMittelfeld();
     }
 
     /**
      * berechnet Anzahl Innere MF im System
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAnzInneresMittelfeld() {
     	int anzahl = 0;
     	
     	anzahl += getAnzPosImSystem(ISpielerPosition.MIDFIELDER);
     	anzahl += getAnzPosImSystem(ISpielerPosition.MIDFIELDER_OFF);
     	anzahl += getAnzPosImSystem(ISpielerPosition.MIDFIELDER_DEF);
     	anzahl += getAnzPosImSystem(ISpielerPosition.MIDFIELDER_TOWING);
     	
     	return anzahl;
     }
 
 	/** berechnet Anzahl Stürmer im System */
 	private int getAnzSturm() {
 		int anzahl = 0;
 
 		anzahl += getAnzPosImSystem(ISpielerPosition.FORWARD);
 		anzahl += getAnzPosImSystem(ISpielerPosition.FORWARD_DEF);
 		anzahl += getAnzPosImSystem(ISpielerPosition.FORWARD_TOWING);
 
 		return anzahl;
 	}
 
     /**
      * ermittelt ANzahl der gesuchten Pos im aktuellen System
      *
      * @param position TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAnzPosImSystem(byte position) {
         SpielerPosition pos = null;
         int anzahl = 0;
 
         for (int i = 0; (m_vPositionen != null) && (i < m_vPositionen.size()); i++) {
             pos = (SpielerPosition) m_vPositionen.elementAt(i);
 
             if ((position == pos.getPosition()) && (pos.getId() < ISpielerPosition.startReserves)) {
                 ++anzahl;
             }
         }
 
         return anzahl;
     }
 
     /**
      * berechnet die stk des Spielers fï¿½r die angegebene Position
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param spielerId TODO Missing Constructuor Parameter Documentation
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private float calcPlayerStk(Vector<ISpieler> spieler, int spielerId, byte position, boolean mitForm) {
         Spieler player = null;
 
         for (int i = 0; (spieler != null) && (i < spieler.size()); i++) {
             player = (Spieler) spieler.elementAt(i);
 
             if (player.getSpielerID() == spielerId) {
                 return player.calcPosValue(position, mitForm);
             }
         }
 
         return 0.0f;
     }
 
     /**
      * berechnet die STK-Summe aller aufgestllten Spieler fï¿½r diese Position
      *
      * @param spieler TODO Missing Constructuor Parameter Documentation
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param mitForm TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private float calcTeamStk(Vector<ISpieler> spieler, byte position, boolean mitForm) {
         float stk = 0.0f;
         de.hattrickorganizer.model.SpielerPosition pos = null;
 
         for (int i = 0; (m_vPositionen != null) && (spieler != null) && (i < m_vPositionen.size());
              i++) {
             pos = (SpielerPosition) m_vPositionen.elementAt(i);
 
             if ((pos.getPosition() == position) && (pos.getId() < ISpielerPosition.startReserves)) {
                 stk += calcPlayerStk(spieler, pos.getSpielerId(), position, mitForm);
             }
         }
 
         return de.hattrickorganizer.tools.Helper.round(stk, 1);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void dumpValues() {
         //dumpen
         for (int i = 0; (m_vPositionen != null) && (i < m_vPositionen.size()); i++) {
             final de.hattrickorganizer.model.Spieler temp = HOVerwaltung.instance().getModel()
                                                                         .getSpieler(((SpielerPosition) m_vPositionen
                                                                                      .elementAt(i))
                                                                                     .getSpielerId());
             String name = "";
             float stk = 0.0f;
 
             if (temp != null) {
                 name = temp.getName();
                 stk = temp.calcPosValue(((SpielerPosition) m_vPositionen.elementAt(i)).getPosition(),
                                         true);
             }
 
             HOLogger.instance().log(getClass(),"PosID: "
                                + SpielerPosition.getNameForID(((SpielerPosition) m_vPositionen.elementAt(i)).getId())
                                + " ,Spieler :" + name + " , Stk : " + stk);
         }
 
         if (m_iKapitaen > 0) {
             HOLogger.instance().log(getClass(),"Kapitï¿½n : "
                                + HOVerwaltung.instance().getModel().getSpieler(m_iKapitaen).getName());
         }
 
         if (m_iKicker > 0) {
             HOLogger.instance().log(getClass(),"Standards : "
                                + HOVerwaltung.instance().getModel().getSpieler(m_iKicker).getName());
         }
 
         HOLogger.instance().log(getClass(),"TW : "
                            + getTWTeamStk(HOVerwaltung.instance().getModel().getAllSpieler(), true)
                            + " AW : "
                            + getAWTeamStk(HOVerwaltung.instance().getModel().getAllSpieler(), true)
                            + " MF : "
                            + getMFTeamStk(HOVerwaltung.instance().getModel().getAllSpieler(), true)
                            + " ST : "
                            + getSTTeamStk(HOVerwaltung.instance().getModel().getAllSpieler(), true));
     }
 
     /**
      * stellt das 4-4-2 Grundsystem ein
      */
     private void initPositionen352() {
         if (m_vPositionen != null) {
             m_vPositionen.removeAllElements();
         } else {
             m_vPositionen = new Vector<ISpielerPosition>();
         }
 
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.keeper, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightBack, 0, (byte) 0));
 //        m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightCentralDefender, 0,
 //                                              ISpielerPosition.ZUS_MITTELFELD));
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
      * Initializes the 553 lineup
      */
     
     private void initPositionen553 () {
     	if (m_vPositionen != null) {
             m_vPositionen.removeAllElements();
         } else {
             m_vPositionen = new Vector<ISpielerPosition>();
         }
 
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.keeper, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightCentralDefender, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.middleCentralDefender, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftCentralDefender, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftBack, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightInnerMidfield, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.centralInnerMidfield, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftInnerMidfield, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.leftWinger, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.rightForward, 0, (byte) 0));
         m_vPositionen.add(new SpielerPosition(ISpielerPosition.centralForward, 0, (byte) 0));
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
      * @param object TODO Missing Method Parameter Documentation
      * @param object2 TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private SpielerPosition swap(Object object, Object object2) {
         final SpielerPosition sp = (SpielerPosition) object;
         final SpielerPosition sp2 = (SpielerPosition) object2;
 
         // TODO Auto-generated method stub
         return new SpielerPosition(sp.getId(), sp2.getSpielerId(), sp2.getTaktik());
     }
 
 	/**
 	 * @return the pullBackMinute
 	 */
 	public int getPullBackMinute() {
 		return pullBackMinute;
 	}
 
 	/**
 	 * @param pullBackMinute the pullBackMinute to set
 	 */
 	public void setPullBackMinute(int pullBackMinute) {
 		this.pullBackMinute = pullBackMinute;
 	}
 
 	/**
 	 * @return if the pull back should be overridden.
 	 */
 	public boolean isPullBackOverride() {
 		return pullBackOverride;
 	}
 	
 	/**
 	 * @param pullBackOverride the override flag to set.
 	 */
 	public void setPullBackOverride(boolean pullBackOverride) {
 		this.pullBackOverride = pullBackOverride;
 	}
 			
     /**
      * Debug logging.
      */
 //    private static void debug(String txt) {
 //    	HOLogger.instance().debug(Aufstellung.class, txt);
 //    }
 
 }
