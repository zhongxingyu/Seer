 package skittles.g6.strategy;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import skittles.g6.CompulsiveEater;
 //import skittles.g6.strategy.PreferenceEvaluatorImpl.Pair;
 import skittles.sim.Offer;
 
 public class OfferGeneratorImplementer implements OfferGenerator{
 
 	private CompulsiveEater myCompulsiveEater;
 	private ArrayList<Offer[]> offersHistory;
 	private int intColorNum;
 	
 	private int lastAmount = -1; 
 	private int lastTradeAway = -1;
 	
 	public OfferGeneratorImplementer(){
 		offersHistory = new ArrayList<Offer[]>();
 		//piles = new ArrayList<Pair<Integer,Integer>>();
 	}
 	
 	public OfferGeneratorImplementer(int intColorNum){
 		offersHistory = new ArrayList<Offer[]>();
 		this.intColorNum = intColorNum;
 		//piles = new ArrayList<Pair<Integer,Integer>>();
 	}
 	
 	public void setPlayer(CompulsiveEater player) {
 		myCompulsiveEater = player;
 	}
 
 	public void setCurrentOffers(Offer[] offers) {
 		Offer[] offerCopy = new Offer[offers.length];
 		for (int i=0; i<offers.length; i++){
 			offerCopy[i] = offers[i];
 		}
 		offersHistory.add(offerCopy);
 	}
 
 	public Offer getOffer() {
 		Offer newOffer = new Offer(myCompulsiveEater.getPlayerIndex(), intColorNum);
 		//TODO: smaller size in beginning for fewer players, because of law of large numbers
 		if (myCompulsiveEater.getTarget() == -1){
 			newOffer = getSteppingOffer();
 		}
 		else{
 			newOffer = getHoardingOffer();
 		}
 		/*if (isOfferCold(newOffer)){
 			//generateNewOffer
 			//perhaps change to 1 and 1
 		}*/
 		return newOffer;
 	}
 
 	private int getHighestPreference(){
 		myCompulsiveEater.getPreferences();
 		double[] aDblTastesCopy =  myCompulsiveEater.getPreferences().clone();		
 		ArrayList<Pair<Double, Integer>> preferencesAndColors = new ArrayList<Pair<Double,Integer>>();
 		for (int i=0; i<aDblTastesCopy.length; i++){
 			preferencesAndColors.add(new Pair<Double,Integer>(aDblTastesCopy[i], i));
 		}
 		Collections.sort(preferencesAndColors);
 		return preferencesAndColors.get(0).getBack();
 		
 	}
 	
 	private int getLowestPreference(){
 		double[] aDblTastesCopy =  myCompulsiveEater.getPreferences().clone();		
 		ArrayList<Pair<Double, Integer>> preferencesAndColors = new ArrayList<Pair<Double,Integer>>();
 		
 		for (int i=0; i<aDblTastesCopy.length; i++){
 			preferencesAndColors.add(new Pair<Double,Integer>(aDblTastesCopy[i], i));
 		}
 		Collections.sort(preferencesAndColors);
 		return preferencesAndColors.get(preferencesAndColors.size()-1).getBack();
 	}
 	
 	/**
 	 * check if Offer is cold by checking against previous two offers
 	 * if previous two offers are the same as your current offer, then
 	 * the offer is cold
 	 * @param anOffer
 	 * @return
 	 */
 	
 	// TODO: check for null offers
 
 /*	public boolean isOfferCold(Offer anOffer){
 		int myPlayerIndex = myCompulsiveEater.getPlayerIndex();
 		if (turn > 1){
 			if ( compareOffers(offersHistory.get(turn)[myPlayerIndex], anOffer)
 					|| compareOffers(offersHistory.get(turn-1)[myPlayerIndex], anOffer) ){
 				return true;
 			}
 		}
 		return false;
 	}*/
 	
 	/*public boolean compareOffers(Offer offer1, Offer offer2){
 		for (int i=0; i<intColorNum; i++){
 			if (offer1.getOffer()[i] != offer2.getOffer()[i])
 				return false;
 		}
 		return true;
 	}*/
 	
 	public Offer getHoardingOffer() {
 		
 		int target = myCompulsiveEater.getTarget();
 		
 		int tradeAway = myCompulsiveEater.getPilesBelowSecondaryThreshold().get(0).getBack();
 		
		if (lastTradeAway!=-1) {
 			lastAmount = -1;
 		}
 		
 		int amount = 0;
 		if (lastAmount == -1) {
 			amount = Math.max(myCompulsiveEater.getAIntInHand()[tradeAway] / 4, 1);
 		} else {
 			amount = Math.min(lastAmount*5/4, myCompulsiveEater.getAIntInHand()[tradeAway]);
 		}
 		
 		lastAmount = amount;
 		lastTradeAway = tradeAway;
 		
 		int[] offered = new int[intColorNum];
 		int[] desired = new int[intColorNum];
 		
 		for (int i = 0; i < intColorNum; i++) {
 			if (i==target) {
 				desired[i]=amount;
 			} else {
 				desired[i]=0;
 			}
 			
 			if (i==tradeAway) {
 				offered[i]=amount;
 			} else {
 				offered[i]=0;
 			}
 		}
 		
 		Offer o = new Offer(myCompulsiveEater.getPlayerIndex(), intColorNum);
 		o.setOffer(offered, desired);
 		
 		return o;
 	}
 	
 	/**
 	 * gets Offers before having found a value over the threshold
 	 * @return
 	 */
 	public Offer getSteppingOffer() {
 		Offer newOffer = new Offer(myCompulsiveEater.getPlayerIndex(), intColorNum);
 		
 		int currentTurn = myCompulsiveEater.getTurnCounter();
 		ArrayList<Pair<Integer, Integer>> piles = myCompulsiveEater.getPiles();
 		ArrayList<Pair<Integer, Integer>> pilesBelowSecondaryThreshold = myCompulsiveEater.getPilesBelowSecondaryThreshold();
 		
 		int[] aintOffer = new int[intColorNum];
 		int[] aintDesire = new int[intColorNum];
 		int lastEatIndex = myCompulsiveEater.getIntLastEatIndex();
 		int tradeAmount = 0;
 		Pair<Integer, Integer> currentColor = piles.get(currentTurn);
 		Pair<Integer, Integer> nextColor = piles.get(currentTurn + 1);
 		
 		//This if check may be redundant. Player shouldn't call getSteppingOffer if this is the case.
 		if (myCompulsiveEater.getPreferences()[lastEatIndex] >= Parameters.PRIMARY_THRESHOLD){
 			return getHoardingOffer();
 		}
 		
 		else{
 			if (currentTurn == 0){ //if first turn	
 			//can maybe combine turn 0 with turn 1 to turn intColorNum-1
 				//if (myCompulsiveEater.getPreferences()[lastEatIndex] < Parameters.SECONDARY_THRESHOLD){
 					tradeAmount = currentColor.getFront()/Parameters.BIG_AMOUNT_DIVISOR;
 					aintOffer[currentColor.getBack()] = tradeAmount;
 					aintDesire[nextColor.getBack()] = tradeAmount;
 				/*}
 				else{ //SECONDARY_THRESHOLD < currentPreference < PRIMARY_THRESHOLD
 					
 				}*/
 			}
 			else if (currentTurn>=1 && currentTurn<intColorNum){
 				//if (myCompulsiveEater.getPreferences()[lastEatIndex] < Parameters.SECONDARY_THRESHOLD){
 					tradeAmount = pilesBelowSecondaryThreshold.get(0).getFront()/Parameters.BIG_AMOUNT_DIVISOR;
 					aintOffer[pilesBelowSecondaryThreshold.get(0).getBack()] = tradeAmount;
 					aintDesire[nextColor.getBack()] = tradeAmount;
 				/*}
 				else{  //SECONDARY_THRESHOLD < currentPreference < PRIMARY_THRESHOLD
 					
 				}*/
 			}
 			else{ //if currentTurn == intColorNum
 				tradeAmount = currentColor.getFront()/Parameters.BIG_AMOUNT_DIVISOR;
 				aintOffer[getLowestPreference()] = tradeAmount;
 				aintDesire[getHighestPreference()] = tradeAmount;
 			}
 		}
 		
 		newOffer.setOffer(aintOffer, aintDesire);
 		return newOffer;
 	}
 }
 
