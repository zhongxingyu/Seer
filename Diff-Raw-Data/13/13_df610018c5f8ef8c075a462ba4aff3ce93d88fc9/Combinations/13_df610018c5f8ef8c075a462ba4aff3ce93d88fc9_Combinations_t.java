 package group5.yatzy;
 
 import java.util.ArrayList;
 
 /**
  * @author Daniel Gunnarsson & Viktor Swantesson
  * This Class will handle different combinations given the specified Dice.
  */
 public class Combinations {
 
 	public static ArrayList<Integer> combos = new ArrayList<Integer>();
 	private static Integer temp = 0;
	private static boolean firsttime = true;
 	/**
 	 * This method calculates what options of combinations are available to the user
 	 * @param playerList: the players current combination list.
 	 * @param dice: the rolled dice
 	 */
	
	
 	public static void calcCombos(ArrayList<Integer> playerList, ArrayList<Integer> dice) {
		if(firsttime){
			for(int i = 0; i < 18; i++){
				combos.add(null);
			}
			firsttime = false;
 		}
 		
 		//The dice list is sorted for proccesing.
 		dice = sort(dice);
 		
 		/**
 		 * Fields for One to Six are calculated and inserted in the list.
 		 */
 		for (int i = 0; i < 6; i++) {
 			if (playerList.get(i) == null) 
 				combos.set(i, calcOnetoSix(i+1, dice));
 			else 
 				combos.set(i, null);
 		}
 
 		/**
 		 * The "TopSum" and "Bonus" fields are not to be calculated here.
 		 */
 		combos.set(6, 0);
 		combos.set(7, 0);
 
 		/**
 		 * The Pair field gets calculated.
 		 */
 		if (playerList.get(8) == null) 
 			combos.set(8, calcPair(dice));
 		else 
 			combos.set(8, null);
 
 		/**
 		 * The TwoPair field gets calculated.
 		 */
 		if (playerList.get(9) == null)  
 			combos.set(9, calcTwoPair(dice));
 		else 
 			combos.set(9, null);
 
 		/**
 		 * The Trips field gets calculated.
 		 */
 		if (playerList.get(10) == null) 
 			combos.set(10, calcTrips(dice));
 		else 
 			combos.set(10, null);
 
 		/**
 		 * The Quads field gets calculated.
 		 */
 		if (playerList.get(11) == null) 
 			combos.set(11, calcQuads(dice));
 		else 
 			combos.set(11, null);
 
 		/**
 		 * The SmallStraight field gets calculated.
 		 */
 		if (playerList.get(12) == null) {
 			temp = calcSmallStr(dice);
 			if (temp != null)
 				combos.set(12, temp);
 			else 
 				combos.set(12, null);
 		}
 		else 
 			combos.set(12, 0);
 
 		/**
 		 * The BigStraight field gets calculated.
 		 */
 		if (playerList.get(13) == null) {
 			temp = calcBigStr(dice);
 			if (temp != null)
 				combos.set(13, temp);
 			else 
 				combos.set(13, null);
 		}
 		else 
 			combos.set(13, null);	
 
 		/**
 		 * The   field gets calculated.
 		 */
 		if (playerList.get(14) == null) 
 			combos.set(14, calcFullHouse(dice));
 		else 
 			combos.set(14, null);
 
 		/**
 		 * The Chance field gets calculated.
 		 */
 		if (playerList.get(15) == null) 
 			combos.set(15, calcChance(dice));
 		else
 			combos.set(15, null);
 
 		/**
 		 * The Yatzy field gets calculated.
 		 */
 		if (playerList.get(16) == null) 
 			combos.set(16, calcYatzy(dice));
 		else 
 			combos.set(16, null);
 
 		/**
 		 * The "TotalSum" field are not to be calculated here.
 		 */
 		combos.set(17, 0);
 	}
 
 	/**
 	 * This method calculates the number of occurrences of a specified dice 
 	 * value there are.
 	 * @param diceValue
 	 * @param dice: set of dice
 	 * @return The combination value of the sum of that specific dice value,
 	 * if no occurrence returns null.
 	 */
 	private static Integer calcOnetoSix(int diceValue, ArrayList<Integer> dice){
 		int occurence = 0;
 
 		for(int i = 0; i < dice.size(); i++){
 			if(diceValue == dice.get(i)){
 				occurence++;
 			}
 		}
 		if (occurence == 0)
 			return null;
 		return (diceValue * occurence);
 	}
 
 	/**
 	 * This method calculates if there are any pairs within the given set of 
 	 * dice.
 	 * @param dice: set of dice
 	 * @return The combination value of the pair if there is a valid one,
 	 * otherwise returns null.
 	 */
 	private static Integer calcPair(ArrayList<Integer> dice) {
 		for (int i = 0; i < 4; i++) {
 			if (dice.get(i) == dice.get(i+1))
 				return dice.get(i)*2;
 		}
 		return null;
 	}
 
 	/**
 	 * This method calculates if there are two pairs of any kind.
 	 * @param dice set of dice.
 	 * @return The combination value of two pairs if there is a valid one,
 	 * otherwise returns null.
 	 */
 	private static Integer calcTwoPair(ArrayList<Integer> dice){
 		Integer pair1 = 0;
 		Integer pair2 = 0;
 		for(int i = 0; i < 2; i++){
 			if(dice.get(i) == dice.get(i+1)) {
 				pair1 = dice.get(i) * 2;
 
 				for (int j = (i+2); j < 4; j++) {
 					if (dice.get(j).equals(dice.get(j+1))) {
 						pair2 = dice.get(j) * 2;
 
 						if(pair1 != pair2)
 							return pair1 + pair2;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method calculates if there are trips of any kind. 
 	 * @param dice set of dice.
 	 * @return The combination value of trips if there is a valid one,
 	 * otherwise returns null.
 	 */
 	private static Integer calcTrips(ArrayList<Integer> dice) {
 		for(int i = 0; i < 3; i++){
 			if(dice.get(i) == dice.get(i+1)){
 				if(dice.get(i) == dice.get(i+2))
 					return dice.get(i) * 3; 
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method calculates if there are quads of any kind.
 	 * @param dice set of dice.
 	 * @return The combination value of quads if there is a valid one,
 	 * otherwise returns null.
 	 */
 	private static Integer calcQuads(ArrayList<Integer> dice) {
 		for(int i = 0; i < 2; i++){
 			if(dice.get(i) == dice.get(i+1))
 				if(dice.get(i) == dice.get(i+2))
 					if(dice.get(i) == dice.get(i+3))
 						return dice.get(i) * 4;
 		}
 		return null;
 	}
 	
 	/**
 	 * This method calculates if there is a small straight of the dice.
 	 * @param dice list of dice
 	 * @return the value of the small straight if any.
 	 */
 	private static Integer calcSmallStr(ArrayList<Integer> dice){
 		if(dice.get(0) != 5)
 			return null;
 		for(int i = 0; i < 4; i++){
 			if(dice.get(i) != dice.get(i+1) + 1)
 				return null;
 		}
 		return 15;
 	}
 	
 	/**
 	 * This method calculates if there is a big straight of the dice.
 	 * @param dice list of dice
 	 * @return the value of the big straight if any.
 	 */
 	private static Integer calcBigStr(ArrayList<Integer> dice){
 		if(dice.get(0) != 6)
 			return null;
 		for(int i = 0; i < 4; i++){
 			if(dice.get(i) != dice.get(i+1) + 1)
 				return null;
 		}
 		return 20;
 	}
 
 	/**
 	 * This method calculates if there are a full house of any kind.
 	 * @param dice set of dice.
 	 * @return The combination value of the full house if there is a valid one,
 	 * otherwise returns null.
 	 */
 	private static Integer calcFullHouse(ArrayList<Integer> dice) {
 
 		Integer temp1 = 0;
 		Integer temp2 = 0;
 
 		temp1 = calcTrips(dice);
 		if(temp1 != null){
 			for(int i = 0; i < 4; i++){
 				if(dice.get(i) == dice.get(i+1) && dice.get(i) != (temp1 / 3)){
 					temp2 = dice.get(i) * 2;
 
 					return temp1 + temp2;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method calculates the value of the chance field, given the 
 	 * current dice
 	 * @param dice the set of dice
 	 * @return the value of all the dice added.
 	 */
 	private static Integer calcChance(ArrayList<Integer> dice) {
 		Integer chance = 0;
 		for (int i = 0; i < 5; i++) {
 			chance += dice.get(i);
 		}
 		return chance;
 	}
 
 	/**
 	 * This method calculates if there is a Yatzy.
 	 * @param dice set of dice.
 	 * @return If a Yatzy exists returns the value 50 otherwise returns null.
 	 */
 	private static Integer calcYatzy(ArrayList<Integer> dice) {
 		if(dice.get(0) == dice.get(4))
 			return 50;
 		return null;
 	}
 
 	/**
 	 * This sorts a set of dice with the highest value first.
 	 * @param dice The sets of dice which you want to sort.
 	 * @return A sorted array with the highest value first.
 	 */
 	private static ArrayList<Integer> sort(ArrayList<Integer> dice) {
 		for(int i = 0; i < 5; i++) {
 			for(int j = 0; j < 4; j++) {
 				if(dice.get(j) < dice.get(j+1)) {
 					Integer temp = dice.get(j);
 					dice.set(j, dice.get(j+1));
 					dice.set(j+1, temp);
 				}
 			}
 		}
 		return dice;
 	}
 }
