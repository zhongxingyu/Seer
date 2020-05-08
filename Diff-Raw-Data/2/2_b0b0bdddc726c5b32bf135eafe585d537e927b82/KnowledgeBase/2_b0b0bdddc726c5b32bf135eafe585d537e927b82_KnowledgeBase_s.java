 package skittles.g2;
 
 import java.util.ArrayList;
 
 import scala.actors.threadpool.Arrays;
 import skittles.sim.Offer;
 
 /**
  * Keeps track of what people have guessed and tries to analyze what people
  * are interested in getting. 
  */
 public class KnowledgeBase {
 	
 	private Inventory inventory;
 	private ArrayList<PreferenceHistory> playerHistories;
 	private ArrayList<Offer> successfulOffers;
 	private ArrayList<Offer> unsuccessfulOffers;
 	private PreferenceHistory marketHistory;
 	
 	private ArrayList<double[][]> relativeWants;
 	
 	private int playerCount;
 	
 	private final static double WANTS_NEW_WEIGHT = 0.5;
 		
 	/**
 	 * Index of ourselves in the playerTrades ArrayList.
 	 */
 	private int selfIndex;
 	
 	public KnowledgeBase(Inventory inventory, int playerCount, int selfIndex) {
 		this.inventory = inventory;
 		this.successfulOffers = new ArrayList<Offer>();
 		this.unsuccessfulOffers = new ArrayList<Offer>();
 		this.selfIndex = selfIndex;
 		this.playerCount = playerCount;
 		playerHistories = new ArrayList<PreferenceHistory>();
 		for (int i = 0; i < playerCount; i++) {
 			playerHistories.add(new PreferenceHistory(inventory.getNumColors()));
 		}
 
 		relativeWants = getRelativeWants();
 		marketHistory = new PreferenceHistory(inventory.getNumColors());
 	}
 	
 	public void storeUnselectedTrade(Offer offer) {
 		unsuccessfulOffers.add(offer);
 		
 		int proposer = offer.getOfferedByIndex();
 		playerHistories.get(proposer).addUnsuccessfulTrade(offer.getOffer(),
 				offer.getDesire());
 		if (proposer != selfIndex) {
 			marketHistory.addUnsuccessfulTrade(offer.getOffer(),
 					offer.getDesire());
 		}
 	}
 	
 	private ArrayList<double[][]> getRelativeWants() {
 		ArrayList<double[][]> relativeWants =
 				new ArrayList<double[][]>(playerCount); 
 		for (int i = 0; i < playerCount; i++) {
 			int skittleCount = inventory.getNumColors();
 			relativeWants.add(new double[skittleCount][skittleCount]);
 		}
 		return relativeWants;
 	}
 	
 	public void updateRelativeWants(Offer[] offers) {
 		ArrayList<double[][]> tempRelativeWants = getRelativeWants();
 		for (Offer o : offers) {
 			// Skip ignored trades for now.
 			if (o.getPickedByIndex() == -1) {
 				continue;
 			}
 			int proposer = o.getOfferedByIndex();
 			int selector = o.getPickedByIndex();
 			addRelativeWants(tempRelativeWants, proposer, o.getDesire(),
 					o.getOffer());
 			addRelativeWants(tempRelativeWants, selector, o.getOffer(),
 					o.getDesire());
 		}
 		mergeWants(tempRelativeWants);
 		
 //		System.out.println(Arrays.deepToString(relativeWants.get(selfIndex)));
 	}
 	
 	private void mergeWants(ArrayList<double[][]> tempRelativeWants) {
 		for (int i = 0; i < tempRelativeWants.size(); i++) {
 			double[][] tempArray = tempRelativeWants.get(i);
 			double[][] oldArray = relativeWants.get(i);
 			for (int j = 0; j < tempArray.length; j++) {
 				for (int k = 0; k < tempArray[j].length; k++) {
 					oldArray[j][k] = oldArray[j][k] * (1- WANTS_NEW_WEIGHT) +
 							tempArray[j][k] * WANTS_NEW_WEIGHT;
 				}
 			}
 		}
 	}
 
 	private double[] getRatios(int[] counts) {
 		double[] givenRatios = new double[counts.length];
 		double sum = 0;
 		for (int i : counts) {
 			sum += i;
 		}
 		if (sum == 0) {
 			return null;
 		}
 		for (int i = 0; i < givenRatios.length; i++) {
 			givenRatios[i] = counts[i] / sum;
 		}
 		return givenRatios;
 	}
 	private void addRelativeWants(ArrayList<double[][]> tempRelativeWants,
 			int affectedPlayerIndex, int[] gained, int[] given) {
 		double[][] playerWants = tempRelativeWants.get(affectedPlayerIndex);
 		
 		double[] givenRatios = getRatios(given);
 		double[] gainedRatios = getRatios(gained);
 		
 		if (givenRatios == null || gainedRatios == null) {
 			return;
 		}
 		
 		for (int i = 0; i < givenRatios.length; i++) {
 			for (int j = 0; j < gainedRatios.length; j++) {
 				// TODO - tweak
 				if (i == j || gainedRatios[i] == 0 || givenRatios[j] == 0) {
 					playerWants[i][j] = 0;	
 				} else if (i < j) {
 					playerWants[i][j] = -gainedRatios[i] / givenRatios[j];
 				} else {
 					playerWants[i][j] = gainedRatios[i] / givenRatios[j];
 				}
 			}
 		}
 	}
 
 	public void storeSelectedTrade(Offer offer) {
 		successfulOffers.add(offer);
 		
 		int proposer = offer.getOfferedByIndex();
 		int selector = offer.getPickedByIndex();
 		playerHistories.get(proposer).addUnsuccessfulTrade(offer.getOffer(),
 				offer.getDesire());
 		playerHistories.get(selector).addUnsuccessfulTrade(offer.getDesire(),
 				offer.getOffer());
 		// If neither condition is true, the trades will cancel out, so don't
 		// bother putting them in.
 		if (proposer == selfIndex || selector == selfIndex) {
 			if (proposer != selfIndex) {
 				marketHistory.addSuccessfulTrade(offer.getOffer(),
 						offer.getDesire());
 			}
 			if (selector != selfIndex) {
 				marketHistory.addSuccessfulTrade(offer.getDesire(),
 						offer.getOffer());
 			}			
 		}
 	}
 	
 	public double[] getMarketPreferences() {
 		return marketHistory.getPreferences();
 	}
 	
 	public double[] getPlayerPreferences(int playerId) {
 		return playerHistories.get(playerId).getPreferences();
 	}
 
 	/**
 	 * @param tastedSkittles
 	 * @return
 	 */
 	public Skittle getHighestMarketValueColorFrom(
 			int start,
 			ArrayList<Skittle> tastedSkittles) {
 		Skittle unwantedColor = null;
 		double[] marketPrefs = this.getMarketPreferences();
 		double currentMarketValue = Double.NEGATIVE_INFINITY;
 		double newMarketValue = 0.0;
 		
 		for (int i = start; i < tastedSkittles.size(); i++) {
 			newMarketValue = marketPrefs[tastedSkittles.get(i).getColor()];
 			if (newMarketValue > currentMarketValue) {
 				unwantedColor = tastedSkittles.get(i);
 				currentMarketValue = newMarketValue;
 			}
 		}
 		return unwantedColor;
 	}
 	
 	public double tradeUtility(Offer o) {
 		double valueIn = 0.0;
 		double valueOut = 0.0;
 		
 		// what we receive is what they are offering
 		int[] in = o.getOffer();
 		// what we send is what they want
 		int[] out = o.getDesire();
 		
 		double[] colorValues = inventory.getColorValues();
 		
 		for(int i = 0; i < in.length; i++) {
 			valueIn += colorValues[i] * Math.pow(in[i], 2);
 		}
 		
 		for(int j = 0; j < in.length; j++) {
 			valueOut += colorValues[j] * Math.pow(out[j], 2);
 		}
 		
 		return valueIn - valueOut;
 	}
 
 	//TODO: calculate the probability that a trade will be accepted
 	public double tradeAcceptanceProbability(Offer o) {
 		//Sid's model
 		
 		//???, profit
 		return 0.0;
 	}
 	
 	//TODO
 	public double countProbability(int count, int color, int player) {
 		//p players, c colors, n skittles per player
 		
 		return 0.0;
 	}
 	
 }
