 package skittles.g4FatKid;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.*;
 import java.lang.Math;
 
 import skittles.sim.*;
 
 public class G4FatKid extends Player{
 
 	private int[] aintInHand;
 	private int intColorNum;
 	double dblHappiness;
 	String strClassName;
 	int intPlayerIndex;
 	
 	private double[] adblTastes;
 	private int intLastEatIndex;
 	private int intLastEatNum;
	eatStrategy es;
 	// set verbose to false to suppress output of debug statements
 	boolean verbose = true;
 	
 	// PlayerProfiles tracks net changes to all players
 	private PlayerProfiles opponentProfiles;
 	
 	// Market tracks the total volume of all trades
 	private Market market;
 	
 	// PreferredColors is a ranking of the colors we like
 	private PreferredColors prefs;
 	
 	// stuff for EatStrategy
 	private EatStrategy es;
 	private String whatToEatNext;
 	private SortedMap<Integer, Double> preferredColors;
 	int turn = 0;
 	
 	@Override
 	public void eat( int[] aintTempEat )
 	{
 		int intMaxColorIndex = -1;
 		int intMaxColorNum = 0;
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			if ( aintInHand[ intColorIndex ] > intMaxColorNum )
 			{
 				intMaxColorNum = aintInHand[ intColorIndex ];
 				intMaxColorIndex = intColorIndex;
 			}
 		}
 		aintTempEat[ intMaxColorIndex ] = intMaxColorNum;
 		aintInHand[ intMaxColorIndex ] = 0;
 		intLastEatIndex = intMaxColorIndex;
 		intLastEatNum = intMaxColorNum;
 	}
 	/*
 	public void eat(int[] aintTempEat) {
 		
 		if (turn == 0)
 			whatToEatNext = es.eatNow(0);
 		else
 			whatToEatNext = es.eatNow(intLastEatIndex);
 		String[] whichSkittle = whatToEatNext.split(" ");
 		int skittleColor = Integer.parseInt(whichSkittle[0]);
 		int numSkittles = Integer.parseInt(whichSkittle[1]);
 		aintTempEat[ skittleColor ] = numSkittles;
 		aintInHand[ skittleColor ] -= numSkittles;
 		intLastEatIndex = skittleColor;
 		intLastEatNum = numSkittles;
 		turn++;
 	}*/
 
 	@Override
 	public void offer(Offer offTemp) {
 		
 		// what is desired: first match between personal prefs (from top down) and rankings from Market
 		// desirability = (personal rank of color x) + (market volume rank of color x)
 		// will take color with minimum value
 		int mostDesired = Integer.MIN_VALUE;
 		for (int i = 0; i < intColorNum; i++) {
 			int temp = prefs.getColorAtRank(i) + market.getColorAtRank(i);
 			if (temp > mostDesired) mostDesired = i;
 		}
 		
 		// what is offered: first match between personal prefs (from bottom up) and rankings from Market
 		int mostUndesired = Integer.MIN_VALUE;
 		for (int i = 0; i < intColorNum; i++) {
 			int temp = prefs.getColorAtRank(intColorNum-i-1) + market.getColorAtRank(i);
 			if (temp > mostUndesired && mostUndesired != mostDesired) mostUndesired = i;
 		}
 		
 		// trade as many Undesired color as possible (at most 4) for Desired color
 		int amountToTrade = Math.min(aintInHand[ mostUndesired ], 4);
 		int[] aintOffer = new int[ intColorNum ];
 		int[] aintDesire = new int[ intColorNum ];
 		for (int i = 0; i < intColorNum; i++) {
 			aintOffer[i] = 0;
 			aintDesire[i] = 0;
 			if (mostDesired == i) aintDesire[i] = amountToTrade;
 			if (mostUndesired == i) aintOffer[i] = amountToTrade;
 		}
 		offTemp.setOffer( aintOffer, aintDesire );
 		
 	}
 
 	@Override
 	public void syncInHand(int[] aintInHand) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void happier(double dblHappinessUp) {
 		double dblHappinessPerCandy = dblHappinessUp / Math.pow( intLastEatNum, 2 );
 		if ( adblTastes[ intLastEatIndex ] == -1 )
 		{
 			adblTastes[ intLastEatIndex ] = dblHappinessPerCandy;
 			// update ranks in adblTastRanks (takes n^2 time)
 			prefs.rerank(adblTastes);
 		}
 		else
 		{
 			if ( adblTastes[ intLastEatIndex ] != dblHappinessPerCandy )
 			{
 				System.out.println( "Error: Inconsistent color happiness!" );
 			}
 		}
 		
 		if (verbose) {
 			// print the rankings of the colors
 			prefs.printRanks();
 			// print the median happiness color
 			System.out.println("Median color is " + prefs.getMedian());
 		}
 	}
 
 	@Override
 	public Offer pickOffer(Offer[] aoffCurrentOffers) {
 		
 		Offer offReturn = null;
 		for ( Offer offTemp : aoffCurrentOffers ) {
 			if ( offTemp.getOfferedByIndex() == intPlayerIndex || offTemp.getOfferLive() == false )
 				continue;
 			int[] skittlesLost = offTemp.getDesire();
 			// first, check if we can even fulfill the offer
 			if ( checkEnoughInHand( skittlesLost ) ) {
 				offReturn = offTemp;
 				int[] skittlesGained = offReturn.getOffer();
 				if (prefs.goodOffer(skittlesLost, skittlesGained)) {
 					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 					{
 						aintInHand[ intColorIndex ] += skittlesGained[ intColorIndex ] - skittlesLost[ intColorIndex ];
 					}
 					break;
 				}
 				else continue;
 			}
 		}
 		if (verbose) {
 			if (offReturn != null) {
 				System.out.println("Offer taken");
 			}
 		}
 		// TODO instead of taking first "good" offer, make array of good offers, then take the best of these
 		
 		return offReturn;
 	}
 
 	@Override
 	public void offerExecuted(Offer offPicked) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void updateOfferExe(Offer[] aoffCurrentOffers) {
 		
 		// update opponentProfiles with the new transactions
 		for ( Offer offTemp : aoffCurrentOffers ) {
 			
 			if ( offTemp.getPickedByIndex() != -1 )
 			{
 				int[] aintTempOffer = offTemp.getOffer();
 				int[] aintTempDesire = offTemp.getDesire();
 				// update the offerer and accepter's profiles (note the switched arguments)
 				opponentProfiles.updatePlayer(offTemp.getOfferedByIndex(), aintTempOffer, aintTempDesire);
 				opponentProfiles.updatePlayer(offTemp.getPickedByIndex(), aintTempDesire, aintTempOffer);
 			}
 		}
 		
 		if (verbose) {
 			System.out.println("Net changes for all players:");
 			opponentProfiles.printProfiles();
 		}
 		
 		// update market
 		market.updateTrades(opponentProfiles);
 		if (verbose) {
 			market.printVolumeTable();
 			market.printRankings();
 		}
 		
 	}	
 
 	@Override
 	public void initialize(int intPlayerIndex, String strClassName,int[] aintInHand) {
 		// TODO Auto-generated method stub
 		this.intPlayerIndex = intPlayerIndex;
 		this.strClassName = strClassName;
 		this.aintInHand = aintInHand;
 		intColorNum = aintInHand.length;
 		dblHappiness = 0;
 		adblTastes = new double[ intColorNum ];
 		
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ ) {
 			adblTastes[ intColorIndex ] = -1;
 		}
		es = new eatStrategy(aintInHand,intColorNum,whatILikeMostScore);	
		System.out.println("FatKid starts");
 		
 		// create PreferredColors object
 		prefs = new PreferredColors(intColorNum);
 		
 		// create EatStrategy object
 		es = new EatStrategy(aintInHand, intColorNum, preferredColors);
 		
 		// create PlayerProfile object; hard-coding 5 for number of Players
 		// will change this to number of players when we find out how to get this value from the
 		// simulator.  For now, this will work for any number of players less than 10
 		opponentProfiles = new PlayerProfiles(5, intColorNum);
 		
 		// create Market object
 		market = new Market(intColorNum);
		
>>>>>>> bcc24950fc6dedf0c2feef621cb2fbdb239a9ca4
 	}
 
 	@Override
 	public String getClassName() {
 		return "g4FatKid";
 	}
 
 	@Override
 	public int getPlayerIndex() {
 		return intPlayerIndex;
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
 	
 }
