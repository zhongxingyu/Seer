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
 	
 	private int turnCounter = 0;
 	private int initialInventory = 0;
 	private int pileIterator = 0;
 	
 	private int prevInventory = -1;
 	
 	private int stopRecursiveInfiniteLoop = 10;
 	
 	private boolean alternativeMode = false;
 	
 	private PreferenceEvaluator pref;
 	
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
 		pref = player.getPreferenceEavluator();
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
 			
 			System.out.println(myCompulsiveEater.getTarget());
 			System.out.println("^^^^^^^^");
 			System.out.println(newOffer);
 			
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
 		
 		int tradeAway;
 		if (myCompulsiveEater.getPilesBelowSecondaryThreshold().isEmpty()) {
 			int minQuantity = Integer.MAX_VALUE;
 			int colorOfIt = 0;
 			
 			for (int i = 0; i < myCompulsiveEater.getAIntInHand().length; i++) {
 				if (i==target) continue;
 				
 				if (myCompulsiveEater.getAIntInHand()[i] < minQuantity) {
 					minQuantity = myCompulsiveEater.getAIntInHand()[i];
 					colorOfIt = i;
 				}
 			}
 			
 			//alternative mode is when no piles are under secondary threshold, and we're trading away
 			//   the smallest pile
 			alternativeMode = true;
 			tradeAway = colorOfIt;
 		} else {
 			alternativeMode = false;
 			
 			int targetPlayer = -1;
 			int tradeAwayToPlayer = -1;
 			
 			for (int i = 0; i< myCompulsiveEater.getPlayerNum(); i++) {
 				if (i==myCompulsiveEater.getPlayerIndex()) continue;
 				
 				tradeAwayToPlayer = -1;
 				
 				int[] curPref = pref.getColorsSortedFromPlayer(i);
 				
 				int checkRange = Math.max(1, intColorNum/4);
 				
 				boolean weHaveWhatTheyWant = false;
 				
 				for (int j = 0; j < checkRange; j++) {
 					int curColor = curPref[j];
 					for (Pair<Integer, Integer> pair : myCompulsiveEater.getPilesBelowSecondaryThreshold()) {
 						if (pair.getBack() == curColor) {
 							weHaveWhatTheyWant = true;
 							tradeAwayToPlayer = curColor;
 							break;
 						}
 					}
 				}
 				
 				boolean theyHaveWhatWeWant = false;
 				
 				for (int j = 0; j < checkRange; j++) {
 					
 					if (curPref[intColorNum-1-j]==target) {
 						theyHaveWhatWeWant = true;
 						break;
 					}
 				}
 				
 				if (weHaveWhatTheyWant && theyHaveWhatWeWant) {
 					targetPlayer = i;
 					break;
 				}
 			}
 			
 			if (targetPlayer != -1) {
 				tradeAway = tradeAwayToPlayer;
 			} else {
 				tradeAway = myCompulsiveEater.getPilesBelowSecondaryThreshold().get(pileIterator).getBack();
 			}
 		}
 		
 		//tradeAway can't be target
		if (tradeAway == target) {
 			turnCounter++;
 			pileIterator = (pileIterator + 1) % myCompulsiveEater.getPilesBelowSecondaryThreshold().size();
 				if (stopRecursiveInfiniteLoop==0) {
 					int[] offered = new int[intColorNum];
 					int[] desired = new int[intColorNum];
 					
 					for (int i = 0; i < intColorNum; i++) {
 						offered[i]=0;
 						desired[i]=0;
 					}
 					
 					Offer o = new Offer(myCompulsiveEater.getPlayerIndex(), intColorNum);
 					o.setOffer(offered, desired);
 					return o;
 				}
 				
 				stopRecursiveInfiniteLoop--;
 				return getHoardingOffer();
 		}
 		
 		if (lastTradeAway!=tradeAway) {
 			initialInventory = myCompulsiveEater.getAIntInHand()[tradeAway];
 			turnCounter=0;
 			lastAmount = -1;
 		} else if (!alternativeMode) {
 			turnCounter++;
 			
			if (turnCounter>=Parameters.GIVE_UP_TURNS &&
 					initialInventory - myCompulsiveEater.getAIntInHand()[tradeAway] < Parameters.GIVE_UP_TURNS + 1) {
 				pileIterator = (pileIterator + 1) % myCompulsiveEater.getPilesBelowSecondaryThreshold().size();
 				if (stopRecursiveInfiniteLoop==0) {
 					int[] offered = new int[intColorNum];
 					int[] desired = new int[intColorNum];
 					
 					for (int i = 0; i < intColorNum; i++) {
 						offered[i]=0;
 						desired[i]=0;
 					}
 					
 					Offer o = new Offer(myCompulsiveEater.getPlayerIndex(), intColorNum);
 					o.setOffer(offered, desired);
 					return o;
 				}
 				
 				stopRecursiveInfiniteLoop--;
 				System.out.println("TRY NEW COLOR");
 				return getHoardingOffer();
 			}
 		}
 		
 		stopRecursiveInfiniteLoop = 10;
 		
 		
 		
 		int amount = 0;
 		//new color chosen
 		if (lastAmount == -1) {
 			amount = Math.max(myCompulsiveEater.getAIntInHand()[tradeAway] / Parameters.BIG_AMOUNT_DIVISOR, 0); //changed - possibly we are done w that color
 		} else {
 			
 			int netChange = prevInventory - myCompulsiveEater.getAIntInHand()[tradeAway];
 		
 			System.out.println("NET CHANGE: " + netChange);
 			
 			if (prevInventory!=-1 && netChange < lastAmount) {
 				System.out.println("RETRY COLOR");
 				System.out.println("TURN COUNTER: " + turnCounter);
 				amount = Math.max(lastAmount / 2, 1);
 			} else {
 				if (netChange==0 && myCompulsiveEater.getPilesBelowSecondaryThreshold().size()!=0) {
 					lastAmount=-1;
 					pileIterator = (pileIterator + 1) % myCompulsiveEater.getPilesBelowSecondaryThreshold().size();
 						if (stopRecursiveInfiniteLoop==0) {
 							int[] offered = new int[intColorNum];
 							int[] desired = new int[intColorNum];
 							
 							for (int i = 0; i < intColorNum; i++) {
 								offered[i]=0;
 								desired[i]=0;
 							}
 							
 							Offer o = new Offer(myCompulsiveEater.getPlayerIndex(), intColorNum);
 							o.setOffer(offered, desired);
 							return o;
 						}
 						
 						stopRecursiveInfiniteLoop--;
 						return getHoardingOffer();
 				}
 				
 				System.out.println("OFFER WENT THROUGH");
 				turnCounter = 0;
 				amount = Math.min(Math.max(lastAmount*3/2, lastAmount+1), myCompulsiveEater.getAIntInHand()[tradeAway]);
 			}
 		}
 		
 		lastAmount = amount;
 		prevInventory = myCompulsiveEater.getAIntInHand()[tradeAway];
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
 	
 	//TODO generate best tradeaway color using both my info and other people's preferences
 	private int bestTradeAway() {
 		
 		return -1;
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
 		
 		//This if check may be redundant. Player shouldn't call getSteppingOffer if this is the case.
 		if (myCompulsiveEater.getPreferences()[lastEatIndex] >= Parameters.PRIMARY_THRESHOLD + myCompulsiveEater.mean
 				|| currentTurn >= intColorNum){
 			return getHoardingOffer();
 		}
 				
		Pair<Integer, Integer> currentColor = piles.get(currentTurn);
 		
 		if (currentTurn == 0){ //if first turn	
 		//can maybe combine turn 0 with turn 1 to turn intColorNum-1
 			if (myCompulsiveEater.getPreferences()[lastEatIndex] < myCompulsiveEater.mean){
				Pair<Integer, Integer> nextColor = piles.get(currentTurn + 1);
 				tradeAmount = currentColor.getFront()/Parameters.BIG_AMOUNT_DIVISOR;
 				aintOffer[currentColor.getBack()] = tradeAmount;
 				aintDesire[nextColor.getBack()] = tradeAmount;
 			}
 			else{ //SECONDARY_THRESHOLD < currentPreference < PRIMARY_THRESHOLD
 				
 			}
 		}
 		else if (currentTurn>=1 && currentTurn<intColorNum-1){
 			if (myCompulsiveEater.getPreferences()[lastEatIndex] < myCompulsiveEater.mean){
				Pair<Integer, Integer> nextColor = piles.get(currentTurn + 1);
 				//if (pilesBelowSecondaryThreshold.size()>0){
 					tradeAmount = pilesBelowSecondaryThreshold.get(0).getFront()/Parameters.BIG_AMOUNT_DIVISOR;
 					aintOffer[pilesBelowSecondaryThreshold.get(0).getBack()] = tradeAmount;
 					aintDesire[nextColor.getBack()] = tradeAmount;
 				//}
 				/*else{
 					tradeAmount = 
 				}*/
 			}
 			else{  //SECONDARY_THRESHOLD < currentPreference < PRIMARY_THRESHOLD
 				tradeAmount = currentColor.getFront()/Parameters.BIG_AMOUNT_DIVISOR;
 				aintOffer[currentColor.getBack()] = tradeAmount;
 				aintDesire[piles.get(0).getBack()] = tradeAmount; 
 			}
 		}
 		else{ //if currentTurn == intColorNum -1
 			//tradeAmount = currentColor.getFront()/Parameters.BIG_AMOUNT_DIVISOR;
 			tradeAmount = piles.get(getLowestPreference()).getFront()/Parameters.BIG_AMOUNT_DIVISOR;
 			aintOffer[getLowestPreference()] = tradeAmount;
 			aintDesire[getHighestPreference()] = tradeAmount;
 		}
 		
 		newOffer.setOffer(aintOffer, aintDesire);
 		return newOffer;
 	}
 }
 
