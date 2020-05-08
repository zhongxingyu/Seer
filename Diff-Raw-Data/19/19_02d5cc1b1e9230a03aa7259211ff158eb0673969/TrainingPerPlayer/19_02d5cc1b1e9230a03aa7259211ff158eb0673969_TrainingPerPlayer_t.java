 // %2751235623:de.hattrickorganizer.model%
 package de.hattrickorganizer.model;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Vector;
 
 import plugins.ISpieler;
 import plugins.ITeam;
 import plugins.ITrainingPerPlayer;
 import plugins.ITrainingWeek;
 import de.hattrickorganizer.logik.TrainingPoint;
 import de.hattrickorganizer.tools.HelperWrapper;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author
  */
 public class TrainingPerPlayer implements plugins.ITrainingPerPlayer {
     //~ Instance fields ----------------------------------------------------------------------------
 
     private ISpieler spieler;
 
     //Flgespiel
     private double FL;
 
     //Stamania
     private double KO;
 
     //Passspiel
     private double PS;
 
     //Spielaufbau
     private double SA;
 
     //Standards
     private double ST;
 
     //Torschuss
     private double TS;
 
     //Torwart
     private double TW;
 
     //Verteidigung
     private double VE;
     
     /** calculate training for this training date only */ 
     private Date timestamp = null;
     
     //~ Constants ----------------------------------------------------------------------------------
     //variable for adjusting amount of set pieces when training scoring
     private static final float p_f_schusstraining_Standard = 0.6f;
 
     private TrainingPoint trainPoint;
     
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new TrainingPerPlayer object.
      */
     public TrainingPerPlayer() {
 //    	trainPoint = new TrainingPoint();
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Setter for property FL.
      *
      * @param FL New value of property FL.
      */
     public final void setFL(double FL) {
         this.FL = FL;
     }
 
     /**
      * Getter for property FL.
      *
      * @return Value of property FL.
      */
     public final double getFL() {
         return FL;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param d TODO Missing Method Parameter Documentation
      */
     public final void setKO(double d) {
         KO = d;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final double getKO() {
         return KO;
     }
 
     /**
      * Setter for property PS.
      *
      * @param PS New value of property PS.
      */
     public final void setPS(double PS) {
         this.PS = PS;
     }
 
     /**
      * Getter for property PS.
      *
      * @return Value of property PS.
      */
     public final double getPS() {
         return PS;
     }
 
     /**
      * Setter for property SA.
      *
      * @param SA New value of property SA.
      */
     public final void setSA(double SA) {
         this.SA = SA;
     }
 
     /**
      * Getter for property SA.
      *
      * @return Value of property SA.
      */
     public final double getSA() {
         return SA;
     }
 
     /**
      * Setter for property ST.
      *
      * @param ST New value of property ST.
      */
     public final void setST(double ST) {
         this.ST = ST;
     }
 
     /**
      * Getter for property ST.
      *
      * @return Value of property ST.
      */
     public final double getST() {
         return ST;
     }
 
     /**
      * Setter for property spieler.
      *
      * @param spieler New value of property spieler.
      */
     public final void setSpieler(plugins.ISpieler spieler) {
         this.spieler = spieler;
     }
 
     /**
      * Getter for property spieler.
      *
      * @return Value of property spieler.
      */
     public final plugins.ISpieler getSpieler() {
         return spieler;
     }
 
     /**
      * Setter for property TS.
      *
      * @param TS New value of property TS.
      */
     public final void setTS(double TS) {
         this.TS = TS;
     }
 
     /**
      * Getter for property TS.
      *
      * @return Value of property TS.
      */
     public final double getTS() {
         return TS;
     }
 
     /**
      * Setter for property TW.
      *
      * @param TW New value of property TW.
      */
     public final void setTW(double TW) {
         this.TW = TW;
     }
 
     /**
      * Getter for property TW.
      *
      * @return Value of property TW.
      */
     public final double getTW() {
         return TW;
     }
 
     /**
      * Setter for property VE.
      *
      * @param VE New value of property VE.
      */
     public final void setVE(double VE) {
         this.VE = VE;
     }
 
     /**
      * Getter for property VE.
      *
      * @return Value of property VE.
      */
     public final double getVE() {
         return VE;
     }
 
     /**
 	 * @return the timestamp
 	 */
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	/**
 	 * Set the timestamp
 	 * if not null, calculate sub increase for this training date only
 	 * 
 	 * @param timestamp the timestamp to set
 	 */
 	public void setTimestamp(Date timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	/**
      * DOCUMENT ME!
      *
      * @return
      */
     public final String toString() {
         return spieler.getSpielerID() + ":" + TW + ":" + VE + ":" + SA + ":" + PS + ":" + FL + ":"
                + TS + ":" + ST + "\n";
     }
 
     /**
      * add sub values of another ITrainingPerPlayer instance to this instance
      * @param values	the instance we take the values from
      */
     public void addValues (ITrainingPerPlayer values) {
     	setFL(this.getFL() + values.getFL());
     	setKO(this.getKO() + values.getKO());
     	setPS(this.getPS() + values.getPS());
     	setSA(this.getSA() + values.getSA());
     	setST(this.getST() + values.getST());
     	setTS(this.getTS() + values.getTS());
     	setTW(this.getTW() + values.getTW());
     	setVE(this.getVE() + values.getVE());
     }
 
 	/**
 	 * Checks if trainingDate is after the last skill up in skillType
 	 * 
 	 * @param trainingDate
 	 * @param skillType
 	 * @return
 	 */
 	private boolean isAfterSkillup (Calendar trainingDate, int skillType) {
 		if (getTimestamp() == null)
 			return true;
 		Date skillupTime = getLastSkillupDate(skillType, getTimestamp());
 		if (trainingDate.getTimeInMillis() > skillupTime.getTime())
 			return true;
 		else
 			return false;
 	}
 	
     /**
      * Updates the training results
      */
     private void calculateTrainingResults(ITrainingWeek train) {
     	double d = trainPoint.calcTrainingPoints(false);
     	int trainType = train.getTyp();
 		Calendar trainingDate = train.getTrainingDate();
     	switch (trainType) {
 		case ITeam.TA_SPIELAUFBAU:
 	        //Spielaufbau // playmaking
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_SPIELAUFBAU))
 				setSA (d);
 			break;
 		case ITeam.TA_VERTEIDIGUNG:
             //Verteidigung //defense
 		case ITeam.TA_ABWEHRVERHALTEN:
             //defensive positions
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_VERTEIDIGUNG))
 				setVE (d);
 			break;
 		case ITeam.TA_FLANKEN:
             //Flankenlaeufe //wing
 		case ITeam.TA_EXTERNALATTACK:
 			// Fluegelangriff //Lateral offensive            
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_FLUEGEL))
 				setFL (d);
 			break;
 		case ITeam.TA_PASSSPIEL:
             //Passspiel //passing
 		case ITeam.TA_STEILPAESSE:
             // through passes
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_PASSSPIEL))
 				setPS (d);
 			break;
 		case ITeam.TA_TORWART:
             //Torwart //keeper
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_TORWART))
 				setTW (d);
 			break;
 		case ITeam.TA_CHANCEN:
             //Chancenverwertung //Torschuss //scoring
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_TORSCHUSS))
 				setTS (d);
 			break;
 		case ITeam.TA_SCHUSSTRAINING:
             //Schusstraining //shooting
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_TORSCHUSS))
 				setTS (d);
             // Shooting gives some training in Set Pieces, too
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_STANDARDS))
 	            setST(p_f_schusstraining_Standard * trainPoint.calcTrainingPoints(true));
 			break;
 		case ITeam.TA_STANDARD:
             //Standardsituationen //set pieces
 			if (isAfterSkillup(trainingDate, ISpieler.SKILL_STANDARDS))
 				setST (d);
 			break;
 		}
     }
 
     /**
      * Calculates the lask skillup for the player in the corrct train
      *
      * @param trainskill Skill we are looking for a skillup
      * @param trainTime Trainingtime
      *
      * @return Last skillup Date, or Date(0) if no skillup were found
      */
     private Date getLastSkillupDate(int trainskill, Date trainTime) {
         //Feststellung wann die relevanten Skillupswaren
         //get relevant skillups for calculation period
         final Vector levelups = getSpieler().getAllLevelUp(trainskill);
         Date skilluptime = new Date(0);
 
         for (Iterator it = levelups.iterator(); it.hasNext();) {
             final Object[] aobj = (Object[]) it.next();
             final Boolean bLevel = (Boolean) aobj[1];
 
             if (bLevel.booleanValue() == true) {
                 final Date tmpTime = new Date(((Timestamp) aobj[0]).getTime());
 
                 if ((tmpTime.before(trainTime)) && (tmpTime.after(skilluptime))) {
                     skilluptime = HelperWrapper.instance()
                     					.getLastTrainingDate(tmpTime,
                     							HOMiniModel.instance().getXtraDaten()
                     							.getTrainingDate())
                     							.getTime();
                 }
             }
         }
 
         return skilluptime;
     }
 
     /**
      * get the training point for this instance
      * @return	training point
      */
 	public TrainingPoint getTrainPoint() {
 		return trainPoint;
 	}
 
 	/**
 	 * set the training point for this instance and
 	 * calculate the sub skills for the player using 
 	 * the training week from this training point
 	 *  
 	 * @param trainPoint	training point
 	 */
 	public void setTrainPoint(TrainingPoint trainPoint) {
 		this.trainPoint = trainPoint;
 		calculateTrainingResults(trainPoint.getTrainWeek());
 	}
     
 }
