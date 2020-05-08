 package riskarena.riskbots;
 /*
  * A simple RiskBot that makes largely random decisions.
  * Used for testing purposes. See HOWTO or RiskBot.java for more on what these methods do.
  * 
  * Evan Radkoff
  */
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Queue;
 import java.util.Random;
 
 import com.sun.tools.javac.util.Pair;
 
 import riskarena.Bot;
 import riskarena.CountryInfo;
 import riskarena.GameInfo;
 import riskarena.OutputFormat;
 import riskarena.PlayerInfo;
 import riskarena.Risk;
 import riskarena.RiskBot;
 import riskarena.World;
 import riskarena.riskbots.evaluation.ArmyChange;
 import riskarena.riskbots.evaluation.CardIndicator;
 import riskarena.riskbots.evaluation.Evaluation;
 import riskarena.riskbots.evaluation.FortifyArmiesDecision;
 
 public class RiskBotAwesome implements RiskBot{
 	/*	Game related data members it's always a good idea to keep */
 	private Bot.RiskListener to_game;		// Send game time decisions using to_game.sendInt(int/Integer)
 	private GameInfo risk_info;
 	private World world = null;				// Holds adjacency information
 	private PlayerInfo[] players = null;
 	
 	/* Data members specific to this particular RiskBot */
 	private Random gen; 										// Random number generator used for some decisions
 	private Queue< Pair<Integer, Integer> > attacks;			// A queue of intended attacks, reset each turn
 	private Integer previousVictory;		// Necessary in order to consider attacking from a newly conquered territory
 	private final int minAttackThreshold = 3;					// If a territory has under this # of armies, don't attack from it
 	private final int maxAttackThreshold = 16;					// If a territory has this many armies, always attack from it
 	private Evaluation eval;	//TODO remove, this is for testing.
 	private CardIndicator card;
 	
 	/*	Decision-makers	*/
 	private FortifyArmiesDecision fortifier;
 
 	/*
 	 * Initialize the bot, locally store the given instance of GameInfo so that we can
 	 * get board info any time we want, as well as a RiskListener so we can communicate our answers.
 	 * @see riskarena.RiskBot#init(riskarena.GameInfo, riskarena.Bot.RiskListener)
 	 */
 	public void init(GameInfo gi, Bot.RiskListener rl) {
 		risk_info = gi;
 		to_game = rl;
 		world = risk_info.getWorldInfo();
 		players = risk_info.getPlayerInfo();
 		gen = new Random((new Date()).getTime());
 		attacks = new ArrayDeque< Pair<Integer, Integer> >();
 		card = new CardIndicator();
 		eval = new Evaluation(risk_info, card);
 		fortifier = new FortifyArmiesDecision(risk_info, eval);
 	}
 	
 	/*
 	 * Turn-based initialization
 	 * @see riskarena.RiskBot#initTurn()
 	 */
 	public void initTurn() {
		System.out.println("Init turn #" + risk_info.getTurnNumber() + " for player " + risk_info.getMyName() + " in thread " + Thread.currentThread().getName());
 		card.setVictory(false);
 		eval.refresh();
 		// At the start of each turn, this bot probabilistically forms decisions to attack
 		// enemy territories, and adds these to the "attacks" queue.
 		// The launchAttack() method executes these decisions until DEATH!
 		attacks.clear();
 		CountryInfo[] countries = risk_info.getCountryInfo();
 		for(int i=0; i<countries.length; i++) {
 			if(shouldAttackFrom(countries[i]))	// Consider attacking from country i
 				attackFrom(i, countries);
 		}
		System.out.println("DONE initting turn #" + risk_info.getTurnNumber() + " for player " + risk_info.getMyName() + " in thread " + Thread.currentThread().getName());
 	}
 	
 	/*
 	 * [Private helper method, not part of the RiskBot interface]
  	 * This method takes a country given by countryID that is supposed to be the
  	 * source of an attack. If one exists, it randomly chooses a neighboring enemy
  	 * territory to invade.
  	 * countries is passed to that the info doesn't need to be reloaded every time this is called
 	 */
 	private void attackFrom(int countryID, CountryInfo[] countries) {
 		ArrayList<Integer> possibleTargets = new ArrayList<Integer>();
 		int adj[] = world.getAdjacencies(countryID);
 		for(int j=0; j<adj.length; j++) {
 			if(countries[adj[j]].getPlayer() != risk_info.me())
 				possibleTargets.add(new Integer(adj[j]));
 		}
 		if(possibleTargets.isEmpty()) {		// No foreign targets
 			return;
 		} else {
 			// Choose randomly from the possible targets
 			int choice = gen.nextInt(possibleTargets.size());
 			attacks.add( new Pair<Integer,Integer>(new Integer(countryID), possibleTargets.get(choice)) );
 			//Risk.sayOutput(countries[countryID].getName() + " -> " + countries[possibleTargets.get(choice).intValue()].getName() + " TARGET SET", OutputFormat.BLUE);
 		}
 	}
 	
 	/*
 	 * [Private helper method, not part of the RiskBot interface]
 	 * Given a CountryInfo object, returns a boolean indicating whether or not it should attack
 	 */
 	private boolean shouldAttackFrom(CountryInfo country) {
 		boolean result = true;
 		if(country.getPlayer() != risk_info.me())
 			result = false;
 		else if(country.getArmies() <= minAttackThreshold)		// Too few armies to attack
 			result = false;
 		else if(country.getArmies() >= maxAttackThreshold)	// Lots of armies, always attack
 			result = true;
 		else {
 			// If the territory has an army amount between the two thresholds, assign a probability of attack (linearly)
 			float probability = (country.getArmies() - minAttackThreshold) / ((float) maxAttackThreshold - minAttackThreshold);
 			result = gen.nextFloat() <= probability;
 		}
 		//if(country.getPlayer() == risk_info.me() && country.getArmies() > minAttackThreshold)
 		//	Risk.sayOutput(country.getName() + ": " + result, OutputFormat.BLUE);
 		return result;
 	}
 
 	/*
 	 * Claim a random unclaimed territory
 	 * @see riskarena.RiskBot#claimTerritory()
 	 */
 	public void claimTerritory() {
 		CountryInfo[] countries = risk_info.getCountryInfo();
 		ArrayList<Integer> unclaimed = new ArrayList<Integer>();
 		
 		for(int i=0;i<countries.length;i++) {
 			if(!countries[i].isTaken()) {
 				unclaimed.add(new Integer(i));
 			}
 		}
 		int choice = gen.nextInt(unclaimed.size());
 		to_game.sendInt(unclaimed.get(choice));
 	}
 
 	/*
 	 * Use a FortifyArmiesDecision to make this choice
 	 * @see riskarena.RiskBot#fortifyTerritory(int)
 	 */
 	public void fortifyTerritory(int num_to_place) {
 		ArrayList< ArmyChange > choices = fortifier.decide(num_to_place);
 		for(ArmyChange choice : choices) {
 			to_game.sendInt(choice.ID());
 			to_game.sendInt(choice.amount());
 		}
 	}
 
 	/*
 	 * //TODO write this
 	 * @see riskarena.RiskBot#launchAttack()
 	 */
 	public void launchAttack() {
 		CountryInfo[] countries = risk_info.getCountryInfo();
 		if( previousVictory != null && shouldAttackFrom(countries[previousVictory]) )
 			attackFrom(previousVictory, countries);
 		previousVictory = null;
 		
 		if(!attacks.isEmpty()) {
 			Pair<Integer,Integer> attack = attacks.peek();		// The attack currently being executed
 			// Check to see if you've conquered the territory
 			if(countries[attack.snd].getPlayer() == risk_info.me()) {
 				attacks.remove();
 				launchAttack();
 				return;
 			}
 			// Check to see if you've run out of armies to attack with :(
 			if(countries[attack.fst].getArmies() == 1) {
 				attacks.remove();
 				launchAttack();
 				return;
 			}
 			to_game.sendInt(attack.fst);
 			to_game.sendInt(attack.snd);
 			to_game.sendInt(Math.min(countries[attack.fst].getArmies()-1, 3)); // Attack with all you've got!
 		} else {
 			to_game.sendInt(-1);
 		}
 	}
 
 	/*
 	 * After a victory, always choose to occupy the gained territory with as many armies as possible 
 	 * @see riskarena.RiskBot#fortifyAfterVictory(int, int, int, int)
 	 */
 	public void fortifyAfterVictory(int attacker, int defender, int min, int max) {
 		// Consider attacking again with the victorious army
 		previousVictory = new Integer(defender);
 		card.setVictory(true);
 		to_game.sendInt(max);
 	}
 
 	/*
 	 * Yes, always choose to turn in a set when given a choice
 	 * @see riskarena.RiskBot#chooseToTurnInSet()
 	 */
 	public void chooseToTurnInSet() {
 		to_game.sendInt(1);
 	}
 
 	/*
 	 * Choose the first possible card set given
 	 * @see riskarena.RiskBot#chooseCardSet(int[][])
 	 */
 	public void chooseCardSet(int[][] possible_sets) {
 		to_game.sendInt(0);
 	}
 
 	/*
 	 * Of all possible movements of all armies from one friendly territory to another, choose one randomly
 	 * @see riskarena.RiskBot#fortifyPosition()
 	 */
 	public void fortifyPosition() {
 		CountryInfo[] countries = risk_info.getCountryInfo();
 		ArrayList< Pair<Integer, Integer> > possibleForts = new ArrayList< Pair<Integer, Integer> >();
 		for(int i=0; i<countries.length; i++) {
 			if(countries[i].getPlayer() == risk_info.me() && countries[i].getArmies() > 1) {
 				int[] adj = world.getAdjacencies(i);
 				for(int j=0;j<adj.length; j++) {
 					if(countries[adj[j]].getPlayer() == risk_info.me())
 						possibleForts.add(new Pair<Integer,Integer>(new Integer(i), new Integer(adj[j])) );
 				}
 			}
 		}
 		if(possibleForts.isEmpty())
 			to_game.sendInt(-1);
 		else {
 			int choice = gen.nextInt(possibleForts.size());
 			to_game.sendInt(possibleForts.get(choice).fst);
 			to_game.sendInt(possibleForts.get(choice).snd);
 			to_game.sendInt(countries[possibleForts.get(choice).fst].getArmies() - 1);
 		}
 	}
 
 }
