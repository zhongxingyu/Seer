 package edu.txstate.hearts.utils;
 
 //import java.util.List;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.SortedMap;
 //import java.util.TreeMap;
 
 import edu.txstate.hearts.model.Card;
 
 /**
  * Class is designed to house risk thresholds and to track and
  * store them for use between sessions.  The RiskThreshold class actually
  * contains six different Threshold objects that are used for comparisons in
  * various sitations:
  * <li>First person playing and Queen Of Spades not played
  * <li>Second person playing and Queen of Spades not played
  * <li>Third person playing and Queen of Spades not played
  * <li>First person playing and Queen of Spades has been played
  * <li>Second person playing and Queen of Spades has been played
  * <li>Third person playing and Queen of Spades has been played
  * 
  * Internally, each Threshold is a double value between 0 and 10.  This value
  * is converted to a value between 0 and 1 using the logistic function (1/(1+e^x))
  * 
  * In this case, because we are using values between 0 and 10, it is actually
  * 1/1+e^(5-x)
  * 
  * @author Jonathan Shelton
  */
 public class RiskThresholds implements Serializable {
 
 
 	private static final long serialVersionUID = -54667351852545143L;
 
 	/**
 	 * return each threshold and it's current values as a string
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return thresholdMap.toString();
 	}
 	
 	/**
 	 * Internal class for holding the actual threshold.  
 	 */
 	public class Threshold implements Serializable {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -4664712297768069997L;
 		double threshold = 0.5;
 
 		/**
 		 * Construct a Threshold with the double passed in as a basis for the
 		 * threshold
 		 * @param t the double to use as the default threshold
 		 */
 		public Threshold(double t) {
 			threshold = t;
 		}
 
 		/**
 		 * @return the threshold
 		 */
 		public double getThreshold() {
 			return threshold;
 		}
 		
 		/** 
 		 * Return the double value as a string
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			// TODO Auto-generated method stub
 			return ""+threshold;
 		}
 
 		/**
 		 * @param threshold
 		 *            the threshold to set
 		 */
 		public void setThreshold(double threshold) {
 			this.threshold = threshold;
 		}
 		
 		/**
 		 * Call the logistic function to convert the risk threshold from a
 		 * number from 0 to 10 to a number from 0 to 1.
 		 * @return this converted value
 		 */
 		public double getRiskThreshold()
 		{
 			return logisticFunction(threshold);
 		}
 
 	}
 
 	//private Random rand = new Random();
 	// float threshold = rand.nextFloat();
 	private Map<Integer, Threshold> thresholdMap = new HashMap();
 
 	/**
 	 * Create the initial thresholds and stick them all into a map.  This will
 	 * only be used if the thresholds.bin file doesn't exist.  If it does, when
 	 * an AgentAggressive is made, that will initialize these values using
 	 * whatever is in that binary file.
 	 */
 	public RiskThresholds() {
 		Threshold goFirstQos = new Threshold(3.2654);
 		Threshold goSecondQos = new Threshold(3.9104);
 		Threshold goThirdQos = new Threshold(4.3810);
 		Threshold goFirstNoQos = new Threshold(4.5945);
 		Threshold goSecondNoQos = new Threshold(5);
 		Threshold goThirdNoQos = new Threshold(5.405);
 		thresholdMap.put(0, goFirstQos);
 		thresholdMap.put(1, goSecondQos);
 		thresholdMap.put(2, goThirdQos);
 		thresholdMap.put(3, goFirstNoQos);
 		thresholdMap.put(4, goSecondNoQos);
 		thresholdMap.put(5, goThirdNoQos);
 	}
 
 
 	/**
 	 * Get the appropriate threshold based on which player in the turn you are
 	 * and whether or not the Queen of Spades has been played
 	 * @param numPlayed the number of people who have played already this turn
 	 * @param qos a boolean indicating whether the queen of spades has been 
 	 * played already or not.  true means it has been played, false means it
 	 * has not been played
 	 * @return the appropriate Threshold to use for this situation
 	 */
 	public Threshold getThreshold(int numPlayed, boolean qos) {
 		int num = numPlayed;
 		if (qos)
 			num += 3;
 		Threshold t = thresholdMap.get(num);
 		// return thresholdMap.get(num).getThreshold();
 		return t;
 	}
 
 	/**
 	 * Increase the threshold to allow for more risk.  The actual formula for
 	 * how to increase the threshold by uses a reverse logistic:
 	 * (1/(1+e^-(x-5)) / 100d+1) * threshold
 	 * The logistic function of this should return a value between 0 and 1, 
 	 * which is divided by 100 to get a value between 0 and .01. Add this value
 	 * to one to multiply the current threshold by a number between 1 and 1.01.
 	 * The idea of this is that the closer the threshold current threshold is
 	 * to 0, the more we want to increase it by.  
 	 * @param t the threshold to be increased
 	 */
 	public void increaseThreshold(Threshold t) {
 		if (t == null)
 			return;
 		//System.out.println("old risk threshold "+t.getRiskThreshold());
 		//System.out.println("logisticFunction2 returned "+logisticFunction2(t.getRiskThreshold()*10d));
 		double factor = logisticFunction2(t.getRiskThreshold()*10d)/100d;
 		increaseThreshold(t, factor);
 		//System.out.println("new risk threshold "+t.getRiskThreshold());
 		// System.out.println("logisticFunction("+(newValue*10d)+") of newValue is "+logisticFunction(newValue*10d));
 	}
 
 	private void increaseThreshold(Threshold t, double offset) {
 		double oldValue = t.getThreshold();
 		double factor = 1d+offset;
 		double newValue = factor * oldValue;
		if(newValue > 10)
			newValue = 10;
		if(newValue < 0)
			newValue = 0;
 		//System.out.println("factor is "+factor);
 		//System.out.println("doing increase oldValue is " + oldValue
 		//		+ " newValue is " + newValue);
 		t.setThreshold(newValue);
 	}
 
 	/**
 	 * If I took any points, decrease the threshold to remove some of the risk.
 	 * The actual formula for the change to the threshold:
 	 * Math.pow((1-(1/(1+e^(5-x))/100)),pointsWon)*currentThreshold
 	 * So working backwards, internally we are leveraging the logistic function
 	 * to get a value between 0 and 1, which is divided by 100 to get a value
 	 * between 0 and .01.  The closer the current threshold is to 0, the closer
 	 * this value will be to 0, the closer it is to 10, the closer this will
 	 * be to .01.  This value from 0 to .01 will be subtracted from 1 to
 	 * yield a value between .99 and 1.  This value will then be taken to
 	 * the power of however many points were won.  So in the worst case, if
 	 * the current threshold had the maximum value of 10, it would net a value
 	 * of .99 for the Math.pow.  The most points you could win in a single 
 	 * hand is 16, so .99 would be taken to that, which yields the worst case
 	 * scenario of approximately .8775 up to the maximum value of 1.  This
 	 * value is multiplied by the current threshold, and the threshold is
 	 * set to that value.
 	 * If there were no points won, this method will actually do an increase
 	 * threshold at half of the normal rate that a typical increase would be.
 	 * Meaning that it won when it was risky, but it didn't hurt us, so increase
 	 * the risk, but by only half as much as you would if you hadn't won with
 	 * that card.
 	 * @param t the threshold to modify
 	 * @param points the number of points won in the trick
 	 */
 	public void decreaseThreshold(Threshold t, int points) {
 		if (t == null)
 			return;
 		if (points == 0)
 		{
 			//System.out.println("old risk threshold "+t.getRiskThreshold());
 			double factor = logisticFunction2(t.getRiskThreshold()*10d)/100d;
 			//System.out.println("logisticFunction2 returned "+logisticFunction2(t.getRiskThreshold()*10d));
 			increaseThreshold(t, factor/2d);
 			//System.out.println("new risk threshold "+t.getRiskThreshold());
 		}
 		else {
 			double oldValue = t.getThreshold();
 			//System.out.println("old risk threshold "+t.getRiskThreshold());
 			double factor = 1d-(logisticFunction(t.getRiskThreshold()*10d)/100d);
 			//System.out.println("factor is "+factor);
 			double newValue = Math.pow(factor, points)*oldValue;
 			//System.out.println("doing decrease oldValue is " + oldValue
 			//		+ " newValue is " + newValue);
 			t.setThreshold(newValue);
 			//System.out.println("new risk threshold "+t.getRiskThreshold());
 		}
 		// System.out.println("logisticFunction("+(newValue*10d)+") of newValue is "+logisticFunction(newValue*10d));
 	}
 
 	private double logisticFunction(double x) {
 		// 1/(1+e^(5-x))
 		double e = Math.E;
 		double denominator = 1d + Math.pow(e, (5 - x));
 		return 1d / denominator;
 	}
 	
 	private double logisticFunction2(double x) 
 	{
 		// 1/(1+e^-(x-5))
 		double e = Math.E;
 		double denominator = 1d + Math.pow(e, (-1*(x - 5)));
 		return 1d / denominator;		
 	}
 
 	// /**
 	// * @param threshold the threshold to set
 	// */
 	// public void setThresholds(float threshold) {
 	// this.threshold = threshold;
 	// }
 
 //	/**
 //	 * After passing in risk, this method will compare risk against the
 //	 * established threshold and determine if a play is low or high risk. It
 //	 * also places safe or risky probabilities (and their associated cards) in
 //	 * separate maps for use if needed.
 //	 * 
 //	 * @param test
 //	 *            Card that probability was created with
 //	 * @param threshold
 //	 *            The currently set threshold
 //	 * @param risk
 //	 *            A double which contains probability of a card to win
 //	 * @param safeMap
 //	 *            A SortedMap for safe probabilities
 //	 * @param riskyMap
 //	 *            A SortedMap for risky probabilities
 //	 * @param lowRisk
 //	 *            A boolean that's true if risk is low
 //	 */
 	// TODO Determine if this is a viable starter method to compare risk and if
 	// it appropriately passed all arguments back to the caller.
 	// SortedMaps can be used in the caller for more advanced probability
 	// searching
 	// public void evaluateRisk(Card test, float threshold, double risk,
 	// SortedMap<Double,Card> safeMap, SortedMap<Double,Card> riskyMap, boolean
 	// lowRisk)
 	// {
 	//
 	// // key = probability (risk)
 	// // value = card (test)
 	// if (risk < getThresholds())
 	// {
 	// safeMap.put(risk, test); //safe probabilities in their own map
 	// lowRisk = true;
 	// }
 	// else //there is a risk of getting hearts
 	// {
 	// riskyMap.put(risk, test); //risky probabilities in their own map
 	// lowRisk = false;
 	// }
 	//
 	// /* Creating the map in the caller
 	// SortedMap<Double,Card> safeMap = new TreeMap<Double,Card>();
 	// SortedMap<Double,Card> riskyMap = new TreeMap<Double,Card>();
 	// */
 	//
 	// /* Possible Implementation:
 	// Display Highest Key: map.lastKey()
 	// Display Lowest Key: map.firstKey()
 	// Can also iterate through entire set by setting an Iterator object to
 	// map.keySet().iterator():
 	// key is the key
 	// map.get(key) is the value */
 	// }
 
 }
