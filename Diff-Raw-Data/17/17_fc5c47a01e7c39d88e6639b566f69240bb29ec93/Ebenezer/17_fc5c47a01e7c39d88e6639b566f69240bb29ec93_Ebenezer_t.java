 
 package skittles.g2;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.PriorityQueue;
 
 import skittles.sim.Offer;
 import skittles.sim.Player;
 
 public class Ebenezer extends Player {
 
 	public static final boolean DEBUG = true;
 
 	private double dblHappiness;
 	private int intPlayerNum;
 	private String strClassName;
 	private int intPlayerIndex;
 	private Mouth myMouth;
 	private Sense mySense;
 	private KnowledgeBase kb;
 	private Inventory inventory;
	private Offer ourOffer;
 	
 	@Override
 	public void initialize(int intPlayerNum, int intPlayerIndex, String strClassName, int[] aintInHand) {
 		this.intPlayerIndex = intPlayerIndex;
 		this.strClassName = strClassName;
 		this.intPlayerNum = intPlayerNum;
 		inventory = new Inventory(aintInHand);
 		myMouth = new Mouth();
 		mySense = new Sense();
 		dblHappiness = 0;
 	}
 
 	@Override
 	public void eat(int[] aintTempEat) {
 		PriorityQueue<Skittle> untasted = inventory.untastedSkittlesByCount();
 		if (!untasted.isEmpty()) {
 			Skittle next = untasted.remove();
 			next.setTasted();
 			aintTempEat[next.getColor()] = 1;
 			myMouth.put(next, 1);
 			return;
 		}
 		PriorityQueue<Skittle> highestNegative = inventory.leastNegativeSkittles();
 		if (!highestNegative.isEmpty()) {
 			Skittle next = highestNegative.remove();
 			next.setTasted();
 			aintTempEat[next.getColor()] = 1;
 			myMouth.put(next, 1);
 			return;
 		}
 		PriorityQueue<Skittle> skittlesByValuesLowest = inventory.skittlesByValuesLowest();
 		Skittle next = skittlesByValuesLowest.remove();
 		next.setTasted();
 		aintTempEat[next.getColor()] = next.getCount();
 		myMouth.put(next, next.getCount());
 	}
 	
 	public void offer(Offer offTemp) {
 		makeOffer(offTemp);
 	}
 
 	/**
 	 * Given an 0-for-0 Offer object, mutate it to the offer we want to put on the table.
 	 * @param offTemp the offer reference from the simulator
 	 */
 	public void makeOffer(Offer offTemp) {
 		if (inventory.tastedSkittlesByCount().isEmpty()) {
 			return;
 		}
 
 		ArrayList<Skittle> tastedSkittles = new ArrayList<Skittle>();
 		for (Skittle s : inventory.getSkittles()) {
 			if (s.isTasted()) {
 				tastedSkittles.add(s);
 			}
 		}
 
 		// sort skittles by how much we like their color
 		Collections.sort(tastedSkittles, new Comparator<Skittle>() {
 			@Override
 			public int compare(Skittle first, Skittle second) {
 				double diff = first.getValue() - second.getValue();
 				if (diff > 0) {
 					return 1;
 				} else if (diff == 0) {
 					return 0;
 				} else {
 					return -1;
 				}
 			}
 		});
 
 		// the two sides of our new offer
 		int[] toOffer = new int[inventory.getSkittles().length];
 		int[] toReceive = new int[inventory.getSkittles().length];
 
 		//if we haven't tasted more than 3 colors, make a null offer. (0-for-0)
 		if (tastedSkittles.size() < 3) {
 			offTemp.setOffer(toOffer, toReceive);
 			return;
 		}
 		
 		//if we've tasted more than two colors, get our favorite one
 		Skittle wantedColor = tastedSkittles.size() > 2 ? tastedSkittles.get(0) : null;
 		Skittle unwantedColor = null;
 		
 		double[] marketPrefs = kb.getMarketPreferences();
 		double currentMarketValue = Double.NEGATIVE_INFINITY;
 		
 		// TODO: move to KnowledgeBase
 		// starting with third-best color, find the color with the highest market value.
 		// set it as our unwanted color.
 		for (int i = 2; i < tastedSkittles.size(); i++) {
 			double newMarketValue = marketPrefs[tastedSkittles.get(i).getColor()];
 			if (newMarketValue > currentMarketValue) {
 				unwantedColor = tastedSkittles.get(i);
 				currentMarketValue = newMarketValue;
 			}
 		}
 
 		// if we know what color we want AND what color we don't want,
 		// set the offer to SEND unwantedColor and RECEIVE wantedColor		
 		// at this point:
 		// unwantedColor is the highest-market-value color that is not one of our top two
 		// wantedColor is the color with the highest value
 		if (unwantedColor != null && wantedColor != null) {
 			// TODO: why are we calculating count like this?
 			// TODO: make offers of mixed colors
 			int count = (int) Math.min(Math.ceil(unwantedColor.getCount() / 5.0), Math.ceil(wantedColor.getCount() / 5.0));
 			toOffer[unwantedColor.getColor()] = count;
 			toReceive[wantedColor.getColor()] = count;
 			offTemp.setOffer(toOffer, toReceive);
 		}
		
		//This is a hack for the meantime because we cannot update if we pick our own offer
		ourOffer = offTemp;
 	}
 
 	@Override
 	public void happier(double dblHappinessUp) {
 		if (myMouth.skittleInMouth.getValue() == Skittle.UNDEFINED_VALUE) {
 			double utility = mySense.getIndividualHappiness(dblHappinessUp, myMouth.howMany);
 			myMouth.skittleInMouth.setValue(utility);
 		}
 	}
 
 	@Override
 	public Offer pickOffer(Offer[] currentOffers) {
 		// We can't get the number of players another way...
 		if (kb == null) {
 			kb = new KnowledgeBase(currentOffers.length, intPlayerIndex, inventory.getSkittles().length);
 		}
 		// Always pick the first live offer.
 		for (Offer o : currentOffers) {
 			if (o.getOfferLive() && canTake(o)) {
 				int[] desiredSkittles = o.getDesire();
 				int[] offeredSkittles = o.getOffer();
				
				//This is a hack for the meantime because we cannot update if we pick our own offer
 				for (int i = 0; i < inventory.size(); i++) {
					if (ourOffer != null && !ourOffer.equals(o)){
						inventory.getSkittle(i).updateCount(offeredSkittles[i] - desiredSkittles[i]);
					}
 				}
 				return o;
				
 			}
 		}
 		return null;
 	}
 
 	private boolean canTake(Offer o) {
 		int[] desired = o.getDesire();
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
 		for (Offer o : aoffCurrentOffers) {
 			if (o.getPickedByIndex() > -1) {
 				kb.storeSelectedTrade(o);
 			} else {
 				kb.storeUnselectedTrade(o);
 			}
 		}
 	}
 
 	@Override
 	public String getClassName() {
 		return strClassName;
 	}
 
 	@Override
 	public int getPlayerIndex() {
 		return intPlayerIndex;
 	}
 
 	// For debug mode apparently
 	@Override
 	public void syncInHand(int[] aintInHand) {
 		// TODO Auto-generated method stub
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
 }
