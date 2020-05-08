 package skittles.g6;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import skittles.g6.strategy.CompulsiveOfferEvaluator;
 import skittles.g6.strategy.InventoryLowerBound;
 import skittles.g6.strategy.InventoryLowerBoundImpl;
 import skittles.g6.strategy.OfferEvaluator;
 import skittles.g6.strategy.OfferGenerator;
 import skittles.g6.strategy.OfferGeneratorImplementer;
 import skittles.g6.strategy.Pair;
 import skittles.g6.strategy.Parameters;
 import skittles.g6.strategy.PreferenceEvaluator;
 import skittles.g6.strategy.PreferenceEvaluatorImpl;
 import skittles.sim.*;
 
 public class CompulsiveEater extends Player 
 {
 	
 	private PreferenceEvaluator prefEval;
 	private OfferEvaluator offerEval;
 	private OfferGenerator offerGen;
 	private InventoryLowerBound inventoryLowerBound;
 	private int turnCounter;
 	private boolean discovery;
 	private int turnsEatenSame;
 	private int turnsSinceLastTrade;
 	private int colorsRemaining;
 	private int target;
 	private int totalSkittles;
 	private int initialTargetInventory;
 	private int discoveryIndex;
 	private boolean negativesRemain;
 	
 	private ArrayList<Pair<Integer, Integer>> piles = new ArrayList<Pair<Integer, Integer>>();
 	private ArrayList<Pair<Integer, Integer>> pilesBelowSecondaryThreshold = new ArrayList<Pair<Integer, Integer>>(); 
 	
 	
 	//===== EVERYTHING BELOW CAME FROM DumpPlayer ====
 	private int[] aintInHand;
 	private int intColorNum;
 	double dblHappiness;
 	String strClassName;
 	int intPlayerIndex;
 	int playerNum;
 	//
 	
 	private double[] adblTastes;
 	private int intLastEatIndex;
 	private int intLastEatNum;
 
 	@Override
 	public void eat( int[] aintTempEat )
 	{
 
 		//printInHand();
 		
 		turnCounter++;
 		int eatIndex = scanForLeastValuable();
 		//self-destruct if nothing remaining under secondary threshold
 		//TODO: set threshold for turnsSinceLastTrade or for only positives left
		if((eatIndex == -1 || !negativesRemain) && turnsSinceLastTrade > pilesBelowSecondaryThreshold.size()){
 			for (int i = 0; i < aintInHand.length; i++) {
 				if(aintInHand[i] > 0){
 					aintTempEat[ i ] = aintInHand[ i ];
 					aintInHand[ i ] = 0;
 					return;
 				}
 			}
 		}
 		//eat all of last color if positive taste
 		if(colorsRemaining == 1 && adblTastes[eatIndex] >= 0){
 			aintTempEat[ eatIndex ] = aintInHand[ eatIndex ];
 			aintInHand[ eatIndex ] = 0;
 			return;
 		}
 		//try to eat one of every color
 		while(discovery && discoveryIndex < intColorNum - 1){
 			discoveryIndex++;
 			refreshDiscoveryOrdering();
 			if(piles.get(discoveryIndex).getFront() == 0){
 				discoveryIndex++;
 				continue;
 			}
 			aintInHand[piles.get(discoveryIndex).getBack()]--;
 			aintTempEat[piles.get(discoveryIndex).getBack()] = 1;
 			intLastEatNum = 1;
 			intLastEatIndex = piles.get(discoveryIndex).getBack();
 			return;
 		}
 		discovery = false;
 		
 		
 		//TODO: Test threshold
 		aintTempEat[ eatIndex ] = 1;
 		aintInHand[ eatIndex ]--;
 		
 		intLastEatIndex = eatIndex;
 		intLastEatNum = aintTempEat[ eatIndex ];
 		
 		if(eatIndex == intLastEatIndex)
 			turnsEatenSame++;
 		else
 			turnsEatenSame = 1;
 		
 		turnsSinceLastTrade++;
 
 	}
 	/*
 	 * Returns the index of the color whose score is closest to zero
 	 */
 	private int scanForLeastValuable(){
 		double minDistanceFromZero = 2;
 		int minTasteIndex = -1;
 		colorsRemaining = intColorNum;
 		for(int i = 0; i < intColorNum; i++){
 			if(aintInHand[i] == 0){
 				colorsRemaining--;
 				continue;
 			}
 			if(Math.abs(adblTastes[i]) < minDistanceFromZero && adblTastes[i] <= 0){
 				minDistanceFromZero = Math.abs(adblTastes[i]); 
 				minTasteIndex = i;
 			}
 		}
 		if(minDistanceFromZero == 2){
 			negativesRemain = false;
 			for(int i = 0; i < intColorNum; i++){
 				if(aintInHand[i] == 0){
 					continue;
 				}
 				if(Math.abs(adblTastes[i]) < minDistanceFromZero && adblTastes[i] < Parameters.SECONDARY_THRESHOLD){
 					minDistanceFromZero = Math.abs(adblTastes[i]); 
 					minTasteIndex = i;
 				}
 			}
 		}
 		return minTasteIndex;
 	}
 	
 	private void createDiscoveryOrdering(){
 		for (int i = 0; i < aintInHand.length; i++) {
 			piles.add(new Pair<Integer, Integer>(aintInHand[i], i));
 		}
 		Collections.sort(piles);
 	}
 	
 	private void refreshDiscoveryOrdering(){
 		for (int i = 0; i < piles.size(); i++) {
 			if(aintInHand[piles.get(i).getBack()] != piles.get(i).getFront())
 				piles.set(i, new Pair<Integer, Integer>(aintInHand[piles.get(i).getBack()], piles.get(i).getBack()));
 		}
 	}
 	
 	private void refreshBelowSecondaryOrdering(){
 		for (int i = 0; i < pilesBelowSecondaryThreshold.size(); i++) {
 			if(aintInHand[pilesBelowSecondaryThreshold.get(i).getBack()] != pilesBelowSecondaryThreshold.get(i).getFront())
 				pilesBelowSecondaryThreshold.set(i, new Pair<Integer, Integer>(aintInHand[piles.get(i).getBack()], 
 						pilesBelowSecondaryThreshold.get(i).getBack()));
 		}
 		Collections.sort(pilesBelowSecondaryThreshold);
 	}
 	
 	/*private void createBelowSecondaryOrdering(){
 		for (int i = 0; i < pilesBelowSecondaryThreshold.size(); i++) {
 			if(adblTastes[i] < Parameters.SECONDARY_THRESHOLD && adblTastes[i] != Parameters.UNKNOWN_TASTE)
 				pilesBelowSecondaryThreshold.add(new Pair<Integer, Integer>(aintInHand[i], i));
 		}
 		Collections.sort(pilesBelowSecondaryThreshold);
 	}*/
 	
 	private void refreshCreateBelowSecondaryOrdering(){
 		if(adblTastes[intLastEatIndex] < Parameters.SECONDARY_THRESHOLD)
 			pilesBelowSecondaryThreshold.add(new Pair<Integer, Integer>(aintInHand[intLastEatIndex], intLastEatIndex));
 		Collections.sort(pilesBelowSecondaryThreshold);
 	}
 	
 	private void refreshTargetColor(){
 		int back = piles.get(discoveryIndex).getBack();
 		if(target == -1){
 			if(adblTastes[back] >= Parameters.PRIMARY_THRESHOLD){
 				target = back;
 				initialTargetInventory = piles.get(discoveryIndex).getFront();
 			}
 			return;
 		}
 		double tasteDiff;
 		
 		//TODO: Tweak these params
 		if((tasteDiff = adblTastes[back] - adblTastes[target]) > 0){
 			double inventoryDiff = 5.0 * (aintInHand[target] - aintInHand[back]) / totalSkittles;
 			double liquidity = 1.0 * (aintInHand[target] - initialTargetInventory) / aintInHand[target]; 
 			if((tasteDiff - inventoryDiff + liquidity) / 3 > .5){
 				target = back;
 				initialTargetInventory = piles.get(discoveryIndex).getFront();
 			}
 		}
 	}
 	
 	//used if none above secondary threshold. sets greatest positive as max
 	public void setTargetAsMax(){
 		double maxScore = 0;
 		int targetIndex = 0;
 		for (int i = 0; i < aintInHand.length; i++) {
 			if(adblTastes[i] > maxScore){
 				maxScore = adblTastes[i] * Math.pow(aintInHand[i], 2);
 				targetIndex = i;
 			}
 		}
 		target = targetIndex;
 	}
 	
 	@Override
 	public void offer( Offer offTemp )
 	{
 		Offer ourOffer = new Offer(intPlayerIndex, intColorNum);
 		if(discovery){
 			refreshTargetColor();
 			refreshBelowSecondaryOrdering();
 			refreshCreateBelowSecondaryOrdering();
 		}
 		else if(target == -1){
 			setTargetAsMax();
 			refreshBelowSecondaryOrdering();
 		}
 		else
 			refreshBelowSecondaryOrdering();
 		ourOffer = offerGen.getOffer();
 		offTemp.setOffer(ourOffer.getOffer(), ourOffer.getDesire());
 
 	}
 
 	@Override
 	public void syncInHand(int[] aintInHand) 
 	{
 		// TODO Auto-generated method stub
 		for (int i = 0; i < this.aintInHand.length; i++) {
 			this.aintInHand[i] = aintInHand[i];
 		}
 		
 	}
 
 	@Override
 	public void happier(double dblHappinessUp) 
 	{
 		double dblHappinessPerCandy = dblHappinessUp / Math.pow( intLastEatNum, 2 );
 		if ( adblTastes[ intLastEatIndex ] == Parameters.UNKNOWN_TASTE )
 		{
 			adblTastes[ intLastEatIndex ] = dblHappinessPerCandy;
 		}
 		else
 		{
 			if ( adblTastes[ intLastEatIndex ] != dblHappinessPerCandy )
 			{
 				//System.out.println( "Error: Inconsistent color happiness!" );
 			}
 		}
 	}
 
 	@Override
 	public Offer pickOffer(Offer[] aoffCurrentOffers) 
 	{	
 		inventoryLowerBound.setCurrentOffers(aoffCurrentOffers);
 		
 		prefEval.examineIncomeOffers(aoffCurrentOffers);
 		offerGen.setCurrentOffers(aoffCurrentOffers);
 		Offer gonnaPick = offerEval.getBestOffer(aoffCurrentOffers);
 		if(gonnaPick == null)
 			return null;
 		int[] aintOffer = gonnaPick.getOffer();
 		int[] aintDesire = gonnaPick.getDesire();
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			aintInHand[ intColorIndex ] += aintOffer[ intColorIndex ] - aintDesire[ intColorIndex ];
 		}
 		turnsSinceLastTrade = 0;
 		return gonnaPick;
 	}
 
 	@Override
 	public void offerExecuted(Offer offPicked) 
 	{
 		turnsSinceLastTrade = 0;
 		int[] aintOffer = offPicked.getOffer();
 		int[] aintDesire = offPicked.getDesire();
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			aintInHand[ intColorIndex ] += aintDesire[ intColorIndex ] - aintOffer[ intColorIndex ];
 		}
 	}
 
 	@Override
 	public void updateOfferExe(Offer[] aoffCurrentOffers) 
 	{
 		prefEval.examineAcceptedOffers(aoffCurrentOffers);
 	}
 
 	@Override
 	public void initialize(int intPlayerNum, int intPlayerIndex, String strClassName,	int[] aintInHand) 
 	{
 		this.intPlayerIndex = intPlayerIndex;
 		this.strClassName = strClassName;
 		this.aintInHand = aintInHand;
 		this.playerNum = intPlayerNum;
 		intColorNum = aintInHand.length;
 		turnsEatenSame = 0;
 		turnCounter = -1;
 		target = -1;
 		negativesRemain = true;
 		intLastEatIndex = -1;
 		discoveryIndex = -1;
 		turnsSinceLastTrade = 0;
 		dblHappiness = 0;
 		discovery = true;
 		adblTastes = new double[ intColorNum ];
 		
 		offerGen = new OfferGeneratorImplementer(intColorNum);
 		offerGen.setPlayer(this);
 		
 		prefEval = new PreferenceEvaluatorImpl(intColorNum);
 		prefEval.setPlayer(this);
 		
 		offerEval = new CompulsiveOfferEvaluator();
 		offerEval.setPlayer(this);
 		
 		inventoryLowerBound = new InventoryLowerBoundImpl();
 		inventoryLowerBound.setPlayer(this);
 		
 		totalSkittles = 0;
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			adblTastes[ intColorIndex ] = Parameters.UNKNOWN_TASTE;
 			totalSkittles += this.aintInHand[intColorIndex];
 		}
 		
 		createDiscoveryOrdering();
 	}
 	
 	private boolean checkEnoughInHand( int[] aintTryToUse )
 	{
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			if ( aintTryToUse[ intColorIndex ] > aintInHand[ intColorIndex ] )
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public void printInHand(){
 		System.out.print("InHand: ");
 		for (int i = 0; i < aintInHand.length; i++) {
 			System.out.print(aintInHand[i] + " ");
 		}
 		System.out.println();
 	}
 
 	@Override
 	public String getClassName() 
 	{
 		return "CompulsiveEater";
 	}
 
 	@Override
 	public int getPlayerIndex() 
 	{
 		return intPlayerIndex;
 	}
 	
 	public OfferGenerator getOfferGenerator() {
 		return offerGen;
 	}
 	
 	public OfferEvaluator getOfferEvaluator() {
 		return offerEval;
 	}
 	
 	public PreferenceEvaluator getPreferenceEavluator() {
 		return prefEval;
 	}
 	
 	public InventoryLowerBound getInventoryLowerBound() {
 		return inventoryLowerBound;
 	}
 	
 	public int getTurnCounter() {
 		return turnCounter;
 	}
 	
 	public int getPlayerNum() {
 		return playerNum;
 	}
 	
 	public double[] getPreferences() {
 		return adblTastes;
 	}
 	
 	public int[] getAIntInHand() {
 		return aintInHand;
 	}
 	
 	public int getIntLastEatIndex() {
 		return intLastEatIndex;
 	}
 	
 	public int getTarget() {
 		return target;
 	}
 	
 	public ArrayList<Pair<Integer, Integer>> getPiles() {
 		return piles;
 	}
 	
 	public ArrayList<Pair<Integer, Integer>> getPilesBelowSecondaryThreshold() {
 		return pilesBelowSecondaryThreshold;
 	}
 }
