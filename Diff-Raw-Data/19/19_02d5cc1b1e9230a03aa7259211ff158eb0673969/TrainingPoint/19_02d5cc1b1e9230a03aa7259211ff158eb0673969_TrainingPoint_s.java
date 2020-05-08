 // %1043029125:de.hattrickorganizer.logik%
 package de.hattrickorganizer.logik;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import de.hattrickorganizer.model.TrainingPerWeek;
 
 import plugins.ITeam;
 import plugins.ITrainingPoint;
 import plugins.ITrainingWeek;
 
 
 /**
  * Class that manages the relation between position and training points
  * 
  * It also is manages the calculation of played minutes for a player 
  * and its relation to the base points.  
  *
  * @author Draghetto HO
  */
 public class TrainingPoint implements ITrainingPoint {
 	/**
 	 * Private class to represent a single match for the training,
 	 * it consists of a minutes/value pair
 	 * 
 	 * @author flattermann <drake79@users.sourceforge.net>
 	 *
 	 */
 	private class MatchForTraining implements Comparable {
 		private int minutes;
 		private double basePoints;
 		
 		public MatchForTraining(int minutes, double basePoints) {
 			this.minutes = minutes;
 			this.basePoints = basePoints;
 		}
 		public int getMinutes() {
 			return minutes;
 		}
 		public void setMinutes(int minutes) {
 			this.minutes = minutes;
 		}
 		public double getBasePoints() {
 			return basePoints;
 		}
 		public void setBasePoints(double basePoints) {
 			this.basePoints = basePoints;
 		}
 		/**
 		 * Compare function to sort the elements in descending order
 		 */
 		public int compareTo (Object obj) {
 			MatchForTraining comparing = (MatchForTraining)obj;
 			if (this.basePoints > comparing.basePoints)
 				return -1;
 			else if (this.basePoints < comparing.basePoints)
 				return 1;
 			else
 				return 0;
 		}
 	}
     //~ Instance fields ----------------------------------------------------------------------------
 
     //Properties of matches to process for this training (minutes & basePoints)
     //The list is automatically sorted by basePoints (high to low)
     private ArrayList matchesForTraining = new ArrayList();
 
     //	Abwehrverhalten alle Verteidiger und Mittelfeldspieler in Verteidigung mit 50% 
     //Defensive Positions: all defenders and playmakers 50% defense
     private Hashtable AV;
 
     //	Chancenauswertung -> Torschuss 100% St?rmer 
     //Scoring: 100% scoring forwards 
     private Hashtable CA;
 
     private Hashtable LATOFF = null; //External Attacker
     
     //Flankel?ufe // Wing
     private Hashtable FL;
 
     //Passspiel //Passing
     private Hashtable PS;
 
     //Spielaufbau // playmaking
     private Hashtable SA;
 
     //scoring
     private Hashtable SCHT;
 
     //Steilpaesse : Passspiel f?r alle Verteidiger und MFler mit 85% // Through passess: passing for all defenders and midfielders with 85%
     private Hashtable SP;
 
     //Torwart //Keeper
     private Hashtable TW;
 
     //Verteidigung //Defense
     private Hashtable VE;
 
     //Order is the same as in Data. As value only SpielerIDS (do i have to translate this?) -> same index for same players
     private Hashtable p_Ht_trainPositionen = new Hashtable();
 
 	private ITrainingWeek trainWeek;
 
 	//~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new TrainingPoint object.
      */
     public TrainingPoint() {
         init();
     }
 
     public TrainingPoint(ITrainingWeek trainWeek) {
     	this.trainWeek = trainWeek;
         init();
     }
     public TrainingPoint (int year, int week, int type, int intensity, int staminaTrainingPart) {
     	this.trainWeek = new TrainingPerWeek(week, year, type, intensity, staminaTrainingPart);
     	init();
     }
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Mathod that returns the correct training point
      *
      * @param trtype Training type code
      * @param playerMatchPosition player position code
      *
      * @return training points earned in that match
      */
     public final Double getTrainingPoint(int trtype, Integer playerMatchPosition) {
         if ((trtype == ITeam.TA_STANDARD) || (trtype == ITeam.TA_KONDITION)) {
             return new Double(1.0);
         }
 
         final Hashtable positions = (Hashtable) p_Ht_trainPositionen.get(new Integer(trtype));
 
         if (positions == null) {
             return new Double(0);
         }
 
         Double value = (Double) positions.get(playerMatchPosition);
 
         if (value == null) {
             value = new Double(0);
         }
 
         return value;
     }
 
     /**
      * Method that initialize the Class Data
      */
     private void init() {
         //F?r jedes TRainingsart die Positionen und Werte festlegen
         //for every traintype position and values definition
         SA = new Hashtable();
         SA.put(new Integer(6), new Double(0.5));
         SA.put(new Integer(7), new Double(1.0));
         SA.put(new Integer(8), new Double(1.0));
         SA.put(new Integer(9), new Double(0.5));
 
         FL = new Hashtable();
         FL.put(new Integer(2), new Double(0.5));
         FL.put(new Integer(5), new Double(0.5));
         FL.put(new Integer(6), new Double(1.0));
         FL.put(new Integer(9), new Double(1.0));
 
         VE = new Hashtable();
         VE.put(new Integer(2), new Double(1.0));
         VE.put(new Integer(3), new Double(1.0));
         VE.put(new Integer(4), new Double(1.0));
         VE.put(new Integer(5), new Double(1.0));
 
         PS = new Hashtable();
         PS.put(new Integer(6), new Double(1.0));
         PS.put(new Integer(7), new Double(1.0));
         PS.put(new Integer(8), new Double(1.0));
         PS.put(new Integer(9), new Double(1.0));
         PS.put(new Integer(10), new Double(1.0));
         PS.put(new Integer(11), new Double(1.0));
 
         TW = new Hashtable();
         TW.put(new Integer(1), new Double(1.0));
 
         CA = new Hashtable();
         CA.put(new Integer(10), new Double(1.0));
         CA.put(new Integer(11), new Double(1.0));
 
         SCHT = new Hashtable();
         SCHT.put(new Integer(2), new Double(0.6));
         SCHT.put(new Integer(3), new Double(0.6));
         SCHT.put(new Integer(4), new Double(0.6));
         SCHT.put(new Integer(5), new Double(0.6));
         SCHT.put(new Integer(6), new Double(0.6));
         SCHT.put(new Integer(7), new Double(0.6));
         SCHT.put(new Integer(8), new Double(0.6));
         SCHT.put(new Integer(9), new Double(0.6));
         SCHT.put(new Integer(10), new Double(0.6));
         SCHT.put(new Integer(11), new Double(0.6));
 
         AV = new Hashtable();
         AV.put(new Integer(2), new Double(0.5));
         AV.put(new Integer(3), new Double(0.5));
         AV.put(new Integer(4), new Double(0.5));
         AV.put(new Integer(5), new Double(0.5));
         AV.put(new Integer(6), new Double(0.5));
         AV.put(new Integer(7), new Double(0.5));
         AV.put(new Integer(8), new Double(0.5));
         AV.put(new Integer(9), new Double(0.5));
 
         SP = new Hashtable();
         SP.put(new Integer(2), new Double(0.85));
         SP.put(new Integer(3), new Double(0.85));
         SP.put(new Integer(4), new Double(0.85));
         SP.put(new Integer(5), new Double(0.85));
         SP.put(new Integer(6), new Double(0.85));
         SP.put(new Integer(7), new Double(0.85));
         SP.put(new Integer(8), new Double(0.85));
         SP.put(new Integer(9), new Double(0.85));
 
         LATOFF = new Hashtable();
         LATOFF.put(new Integer(6), new Double(0.6));
         LATOFF.put(new Integer(9), new Double(0.6));        
         LATOFF.put(new Integer(10), new Double(0.6));
         LATOFF.put(new Integer(11), new Double(0.6));
         
         //die einzelnen Trainingsarten hinzuf?gen
         //add all traintypes to one hashtable with all trainingstypes
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_SPIELAUFBAU), SA);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_FLANKEN), FL);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_VERTEIDIGUNG), VE);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_PASSSPIEL), PS);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_TORWART), TW);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_CHANCEN), CA);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_SCHUSSTRAINING), SCHT);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_ABWEHRVERHALTEN), AV);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_STEILPAESSE), SP);
         p_Ht_trainPositionen.put(new Integer(ITeam.TA_EXTERNALATTACK), LATOFF);
     }
 
     /**
 	 * Adds a match to the internal list
 	 * 
 	 * @param minutes	How long was the player on the field in this match
 	 * @param posId		Position of the player in this match
 	 */
 	public void addTrainingMatch (int minutes, int posId) {
 //		System.out.println ("Match added for player "+spieler.getName()+": train="+train+", min="+minutes+", bP="+basePoints);
		double basePoints = getTrainingPoint(trainWeek.getTyp(), new Integer (posId)).doubleValue();
		matchesForTraining.add(new MatchForTraining(minutes, basePoints));
		Collections.sort(matchesForTraining);
		calcTrainingPoints (false);
 	}
 
     /**
      * Calculate how many points the player gets for this week,
      * using the matches previously added with addTrainingMatch(min, posId)
      * 
      * Optimal: 100% position + 90mins -> points = 1.0
      * 
      * @param ignorePosition	Ignore players position, otherwise use only minutes on correct position
      * @return training points for this player and week 
      */
     public double calcTrainingPoints (boolean ignorePosition) {
     	int minutesLeft = 90;
     	double points = 0;
     	// matchesForTraining is already sorted by baseLength in descending order 
     	Iterator iter = matchesForTraining.iterator();
     	while (iter.hasNext() && minutesLeft > 0) {
     		MatchForTraining curMatch = (MatchForTraining)iter.next();
     		double curBasePoints = curMatch.getBasePoints();
     		int curMinutes = curMatch.getMinutes();
     		// use up to minutesLeft mins
     		curMinutes = Math.min (curMinutes, minutesLeft);
     		// Shall we ignore the player position?
     		if (ignorePosition)
     			curBasePoints = 1;
     		double curPoints = curBasePoints * curMinutes/90d;
 //    		System.out.println("calcTrP: curPoints="+curPoints);
    			points += curPoints;
    			minutesLeft -= curMinutes;
     	}
 //    	System.out.println ("calcTrP: final points="+points);
     	return points;
     }
 
 	public ITrainingWeek getTrainWeek() {
 		return trainWeek;
 	}
 
 	public void setTrainWeek(ITrainingWeek trainWeek) {
 		this.trainWeek = trainWeek;
 	}
 }
