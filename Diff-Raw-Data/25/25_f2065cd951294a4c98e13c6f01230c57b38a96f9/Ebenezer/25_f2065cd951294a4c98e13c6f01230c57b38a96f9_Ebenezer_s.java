 
 package skittles.g2;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.PriorityQueue;
 
 import skittles.sim.Offer;
 import skittles.sim.Player;
 
 public class Ebenezer extends Player {
 
 	public static final boolean DEBUG = true;
 
 	private String className;
 	private int playerIndex;
 	private Mouth mouth;
 	private KnowledgeBase kb;
 	private Inventory inventory;
 	private Offer ourOffer;
 
 	private Offer[] lastOfferSet;
 
 	@Override
 	public void initialize(int numPlayers, int playerIndex,
 			String className, int[] inHand) {
 		this.playerIndex = playerIndex;
 		this.className = className;
 		inventory = new Inventory(inHand);
 		mouth = new Mouth();
 		kb = new KnowledgeBase(inventory, numPlayers, playerIndex);
 	}
 
 	@Override
 	public void eat(int[] toEat) {
 		// Update everyone else's count
 		kb.updateCountByTurn();
 		kb.printEstimateCount();
 
 		// Prioritize skittles that you haven't tasted before.
 		PriorityQueue<Skittle> untasted = inventory.untastedSkittlesByCount();
 		if (!untasted.isEmpty()) {
 			Skittle next = untasted.remove();
 			next.setTasted(true);
 			toEat[next.getColor()] = 1;
 			mouth.put(next, 1);
 			return;
 		}
 		
 		/*
 		 * Then, eat negative skittles, starting from the skittle with lowest
 		 * absolute value. Doing this allows us to eat multiple low positives
 		 * in the future in a bigger group, which should more than offest the
 		 * small hit we take from eating these skittles. 
 		 */
 		PriorityQueue<Skittle> highestNegative = inventory.leastNegativeSkittles();
 		if (!highestNegative.isEmpty()) {
 			Skittle next = highestNegative.remove();
 			next.setTasted(true);
 			toEat[next.getColor()] = 1;
 			mouth.put(next, 1);
 			return;
 		} else {
 			
 		}
 
 		PriorityQueue<Skittle> skittlesByValuesLowest =
 			inventory.skittlesByValuesLowest();
 		Skittle next = null;
 		// if there are no negatives and we've tasted all skittles and there are more than one pile left,
 		// eat positives one at a time
 		if(skittlesByValuesLowest.size() > 1) {
 			next = skittlesByValuesLowest.peek();
		}
		
		Offer bestTrade = getOurBestTrade();
 		
		if(kb.tradeUtility(bestTrade) * kb.tradeCountProbability(bestTrade) > next.getValue()) {
			if(next.getCount() == 1) {
				skittlesByValuesLowest.remove();
 			}
			next.setTasted(true);
			toEat[next.getColor()] = 1;
			mouth.put(next, 1);
			return;
 		}
 		
 		/*
 		 * Eat the positive Skittles in groups if we can't make good trades
 		 */
		Skittle next = skittlesByValuesLowest.remove();
 		next.setTasted(true);
 		toEat[next.getColor()] = next.getCount();
 		mouth.put(next, next.getCount());
 	}
 
 	/*
 	 * Wrapper for making an offer.
 	 */
 	public void offer(Offer offTemp) {
 		// Update the relative wants with the set of offers 
 		if (lastOfferSet != null) {
 			kb.updateRelativeWants(lastOfferSet);
 		}
 		lastOfferSet = null;
 		makeOffer(offTemp);
 	}
 
 	/*
 	 * Makes an offer.
 	 */
 	public void makeOffer(Offer offTemp) {
 
 		offTemp = getOurBestTrade();
 
 		// This is a hack for the meantime because we cannot update if we pick
 		// our own offer.
 		ourOffer = offTemp;
 	}
 
 	/**
 	 * returns the best trade we can make
 	 */
 	private Offer getOurBestTrade() {
 		Offer offTemp = new Offer(getPlayerIndex(), inventory.getNumColors());
 		
 		ArrayList<Skittle> sortedSkittles = inventory.getSortedSkittleArray();
 
 		int[] toOffer = new int[inventory.getSkittles().length];
 		int[] toReceive = new int[inventory.getSkittles().length];
 
 		// TODO - want multiple colors
 		Skittle wantedColor = sortedSkittles.get(0);
 
 		// starting with third-best color, find the color with the highest
 		// market value.
 		Skittle unwantedColor = kb.getHighestMarketValueColorFrom(2, sortedSkittles);
 
 		// if we know what color we want AND what color we don't want,
 		// set the offer to SEND unwantedColor and RECEIVE wantedColor
 		// at this point:
 		// unwantedColor is the highest-market-value color that is not one of
 		// our top two
 		// wantedColor is the color with the highest value
 		if (unwantedColor != null && wantedColor != null) {
 			// TODO: why are we calculating count like this?
 			// TODO: make offers of mixed colors
 			int count = (int) Math.min(Math.ceil(unwantedColor.getCount() / 5.0), Math.ceil(wantedColor.getCount() / 5.0));
 			toOffer[unwantedColor.getColor()] = count;
 			toReceive[wantedColor.getColor()] = count;
 			offTemp.setOffer(toOffer, toReceive);
 		}
 		
 		return offTemp;
 	}
 
 	@Override
 	public void happier(double deltaHappiness) {
 		if (deltaHappiness < 0 && mouth.howMany > 1) {
 			System.err.println("FUKKC");
 			System.exit(1);
 		}
 		if (mouth.skittleInMouth.getValue() == Skittle.UNDEFINED_VALUE) {
 			double utility = inventory.getIndividualHappiness(deltaHappiness, mouth.howMany);
 			mouth.skittleInMouth.setValue(utility);
 		}
 		inventory.updateSkittleRankings();
 	}
 
 	@Override
 	public Offer pickOffer(Offer[] currentOffers) {
 
 		ArrayList<Offer> trades = new ArrayList<Offer>();
 		for (Offer o : currentOffers) {
 			if (o.getOfferLive() && canTake(o)) {
 				trades.add(o);
 			}
 		}
 
 		if (trades.size() == 0) {
 			return null;
 		}
 
 		// sort trades by their utility (computed by tradeUtility())
 		Collections.sort(trades, new Comparator<Offer>() {
 			@Override
 			public int compare(Offer first, Offer second) {
 				double diff = kb.tradeUtility(first) - kb.tradeUtility(second);
 				if (diff > 0) {
 					return -1;
 				} else if (diff == 0) {
 					return 0;
 				} else {
 					return 1;
 				}
 			}
 		});
 
 		Offer bestTrade = trades.get(0);
 		double bestTradeUtility = kb.tradeUtility(bestTrade);
 		if (DEBUG) {
 			for (Offer t : trades) {
 				System.out.println(t.toString() + " = " + kb.tradeUtility(t));
 			}
 			System.out.println("bestTrade: " + bestTrade.toString() + " = " + bestTradeUtility);
 		}
 		if (bestTrade != null && bestTradeUtility > 0) {
 			takeTrade(bestTrade);
 			return bestTrade;
 		}
 
 		return null;
 	}
 
 	/**
 	 * update counts based on the trade we're accepting
 	 * 
 	 * @param bestTrade
 	 */
 	private void takeTrade(Offer bestTrade) {
 		int[] desiredSkittles = bestTrade.getDesire();
 		int[] offeredSkittles = bestTrade.getOffer();
 
 		for (int i = 0; i < inventory.size(); i++) {
 			inventory.getSkittle(i).updateCount(offeredSkittles[i] - desiredSkittles[i]);
 		}
 	}
 
 	private boolean canTake(Offer o) {
 		if (ourOffer != null && o.equals(ourOffer)) {
 			return false;
 		}
 
 		int[] offered = o.getOffer();
 		int[] desired = o.getDesire();
 
 		if (Arrays.equals(offered, desired)) {
 			return false;
 		}
 
 		for (int i = 0; i < desired.length; i++) {
 			if (inventory.getSkittle(i).getCount() < desired[i]) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	// Someone pick the offer
 	@Override
 	public void offerExecuted(Offer offPicked) {
 		int[] aintOffer = offPicked.getOffer();
 		int[] aintDesire = offPicked.getDesire();
 		for (int i = 0; i < inventory.size(); i++) {
 			inventory.getSkittle(i).updateCount(aintDesire[i] - aintOffer[i]);
 		}
 	}
 
 	@Override
 	public void updateOfferExe(Offer[] aoffCurrentOffers) {
 		lastOfferSet = aoffCurrentOffers;
 		for (Offer o : aoffCurrentOffers) {
 			if (o.getPickedByIndex() > -1) {
 				kb.storeSelectedTrade(o);
 				kb.updateCountByOffer(o);
 			} else {
 				kb.storeUnselectedTrade(o);
 			}
 		}
 	}
 
 	public class Mouth {
 		public void put(Skittle s, int h) {
 			this.skittleInMouth = s;
 			this.howMany = h;
 			s.updateCount(-h);
 		}
 
 		public Skittle skittleInMouth;
 		public int howMany;
 	}
 	
 
 	// Unused methods.
 	
 	@Override
 	public String getClassName() {
 		return className;
 	}
 
 	@Override
 	public int getPlayerIndex() {
 		return playerIndex;
 	}
 
 	@Override
 	public void syncInHand(int[] aintInHand) {
 		// TODO Auto-generated method stub
 	}
 
 }
