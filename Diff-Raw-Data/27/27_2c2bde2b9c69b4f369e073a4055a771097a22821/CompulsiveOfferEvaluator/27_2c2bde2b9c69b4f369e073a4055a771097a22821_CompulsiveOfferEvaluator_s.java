 package skittles.g6.strategy;
 
 import skittles.g6.CompulsiveEater;
 import skittles.sim.Offer;
 
 public class CompulsiveOfferEvaluator implements OfferEvaluator {
 	
 	private CompulsiveEater player;
 	private int playerIndex;
 	private int[] inHand;
 	private double[] tastes;
 
 	public void setPlayer(CompulsiveEater player) {
 		this.player = player;
 		playerIndex = player.getPlayerIndex();
 		inHand = player.getAIntInHand();
 		tastes = player.getPreferences(); 
 	}
 
 	public Offer getBestOffer(Offer[] offers) {
 		double maxScore = 0;
 		Offer bestOffer = null;
 		for (Offer currOffer : offers) {
 			int[] aintDesire = currOffer.getDesire();
 
 			if(!checkEnoughInHand(aintDesire) || currOffer.getOfferedByIndex() == playerIndex 
 					|| !currOffer.getOfferLive())
 				continue;
 			double currentScore = 0;
 			for (int i = 0; i < aintDesire.length; i++) {
 				currentScore += tastes[i] * (Math.pow(inHand[i], 2) - Math.pow(inHand[i] + aintDesire[i], 2));
 			}
 			int[] aintOffer = currOffer.getOffer();
 			for (int i = 0; i < aintOffer.length; i++) {
 				currentScore += tastes[i] * (Math.pow(inHand[i] + aintOffer[i], 2) - Math.pow(inHand[i], 2));
 			}
 			
			if(currentScore > maxScore)
 				bestOffer = currOffer;
 		}
 		return bestOffer;
 	}
 
 	private boolean checkEnoughInHand( int[] aintToUse ) 
 	{
 		for ( int intColorIndex = 0; intColorIndex < inHand.length; intColorIndex ++ )
 		{
 			if ( aintToUse[ intColorIndex ] > inHand[ intColorIndex ] || aintToUse[ intColorIndex ] < 0 )
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 }
