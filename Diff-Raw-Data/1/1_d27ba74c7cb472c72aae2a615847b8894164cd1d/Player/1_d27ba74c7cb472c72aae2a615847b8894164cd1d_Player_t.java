 package magicGame;
 
 import java.util.ArrayList;
 
 public class Player {
 
 	String[] deckList;
 	ArrayList<Card> deck;
 	ArrayList<Card> hand;
 	// Have I lost the game?
 	boolean hasLost = false;
 	int life;
 	GameState gameState;
 	// Have I played a land this turn?
 	boolean playedLand;
 	int playerNumber;
 	// I'm going to use a simple integer to indicate player AI style. For now, 0
 	// means that the player simply blocks to prevent damage dealt to him, while
 	// 1 means that the player tries to gain a creature advantage through
 	// blocking.
 	int playerStyle;
 
 	// Mana open is a length 6 int array that stores avaliable mana in
 	// alphabetical color order, with colorless last. The specific order is
 	// [Black, Blue, Green, White, Red, Colorless]
 	private int[] manaOpen;
 	private ArrayList<Card> graveyard;
 
 	public Player(String[] newDeck, GameState gameState) {
 		playedLand = false;
 		this.life = 20;
 		this.deckList = newDeck;
 		deck = new ArrayList<Card>();
 		hand = new ArrayList<Card>();
 		this.gameState = gameState;
 		this.manaOpen = new int[6];
 		this.graveyard = new ArrayList<Card>();
 		this.playerStyle = (int) Math.random() * 2;
 
 		for (String cardName : this.deckList) {
 			Card card = CardDB.getCard(cardName, this);
 			this.deck.add(card);
 		}
 
 	}
 
 	public ArrayList<Card> getDeck() {
 		return this.deck;
 	}
 
 	public ArrayList<Card> getHand() {
 		return this.hand;
 	}
 
 	public boolean draw(int numToDraw) {
 		for (int i = 0; i < numToDraw; i++) {
 			if (deck.size() <= 0) {
 				this.hasLost = true;
 				return this.hasLost;
 			}
 			this.hand.add(this.deck.remove(0));
 		}
 		return this.hasLost;
 	}
 
 	public boolean checkHasLost() {
 		return this.hasLost;
 	}
 
 	public void setHasLost(boolean hasLost) {
 		this.hasLost = hasLost;
 	}
 
 	public int getLife() {
 		return this.life;
 	}
 
 	public int setLife(int newLife) {
 		this.life = newLife;
 		return this.life;
 	}
 
 	public int changeLife(int lifeChangeIncrement) {
 		this.life += lifeChangeIncrement;
 		// System.out.println("Player " + this.playerNumber + " now has "
 		// + this.life + " life remaining.");
 		return this.life;
 	}
 
 	// the main phase is where most stuff happens, like playing lands and
 	// casting creature cards and other spells. Right now, all it does is play
 	// lands and creatures - in the future, Sorcery speed cards will be played
 	// here, and there will be heuristics for skipping on playing cards to be
 	// able to play instants on the opponent's turn.
 	public void playMainPhase() {
 		// Look for a land to play, and play it.
 		Card c;
 		for (int i = 0; i < this.hand.size(); i++) {
 			c = this.hand.get(i);
 			if (c.getTypes().contains("Land") && this.playedLand == false) {
 				this.playPermanentCard(i);
 				this.playedLand = true;
 				break;
 			}
 
 		}
 
 		this.evaluateOpenMana();
 
 		// TODO: make the player make actual decisions about playing cards,
 		// rather than just playing the first one in their hand.
 
 		// So, for prioritizing actions, we have a few basic categories for
 		// decisions: winning, losing, and close match. If we're winning, we
 		// want to maintain the lead while preparing to prevent an opponent's
 		// come back. If we're losing, the first priority is to not lose, and
 		// the second priority is to regain the lead. If the game is close, we
 		// function similarly to the winning situation, keeping some reserve to
 		// control the opponent, but try and gain a lead as in the losing case.
 
 		String position = this.evaluatePosition();
 
 		while (true) {
 			int bestCard = 0;
 			int bestGain = 0;
 			for (int i = 0; i < this.hand.size(); i++) {
 				c = this.hand.get(i);
 				if (this.evaluateCardPlayable(c)) {
 
 					String types = c.getTypes();
 					if (types.contains("Creature")
 							|| types.contains("Artifact")
 							|| types.contains("Enchantment")
 							|| types.contains("Planeswalker")) {
 
 						this.playPermanentCard(i);
 						break;
 
 					}
 
 					else if (types.contains("Sorcery")) {
 						if (this.evaluateTargets(c)) {
 							this.playSorcery(i);
 							break;
 						}
 					} else if (types.contains("Instant")) {
 						// TODO: Switch the target back to null if the card
 						// doesn't get played.
 						if (c.getEffects().contains("Target")) {
 							if (this.evaluateTargets(c)) {
 								this.playInstant(i);
 								break;
 							}
 
 						} else {
 							this.playInstant(i);
 							break;
 						}
 					}
 
 				}
 
 			}
			break;
 
 		}
 
 	}
 
 	// This method evaluates the player's relative position. For now, it will
 	// assume that there are only two players. All of the logic is highly
 	// simplified, using basic generalized heuristics instead of the kind of
 	// precise evaluation that a human player would make. The result is that the
 	// AI is sub-par, but it is a considerable step forward from the previous
 	// implementation.
 	private String evaluatePosition() {
 
 		// Evaluate life, cards in library, board position, and hand sizes.
 		// Well, at least it will eventually. For now, I'm just looking at
 		// damage stuffs.
 		int maxOpponentLife = this.gameState.maxOpponentLife(this);
 		ArrayList<Card> creatures = this.gameState.getCreatures();
 		int opponentCreatures = 0;
 		int opponentPower = 0;
 		int myCreatures = 0;
 		int myPower = 0;
 
 		for (Card c : creatures) {
 			if (c.getOwner().equals(this)) {
 				myCreatures++;
 				myPower += c.getPower();
 			} else {
 				opponentCreatures++;
 				opponentPower += c.getPower();
 
 			}
 		}
 
 		int lifeMargin = this.life - maxOpponentLife;
 
 		if (this.life <= 5) {
 			// Danger Zone!!!
 			if (lifeMargin <= 0) {
 				if (opponentCreatures > myCreatures) {
 					if (opponentPower >= myPower) {
 						return "Losing";
 					} else {
 						// Basically checks to see if the game is somewhat
 						// close.
 						if (lifeMargin > -3
 								&& (opponentCreatures - myCreatures) < 2) {
 							return "Close";
 						} else {
 							return "Losing";
 						}
 					}
 				}
 
 				else {
 					// Checks to see if I have enough board state to overcome
 					// the life differential.
 					if (lifeMargin > (opponentPower - myPower)) {
 						return "Close";
 
 					} else {
 						return "Losing";
 					}
 				}
 
 			}
 
 			else {
 				// This is the case that the opponent is in the danger zone.
 				if (opponentCreatures < myCreatures) {
 					if (opponentPower < myPower) {
 						return "Winning";
 					} else {
 						return "Close";
 					}
 				} else {
 					// The opposite situation of this is called close, but I
 					// want to be conservative in the danger zone, so if the
 					// opponent has a larger threat than me, I call it losing.
 
 					if (opponentPower > myPower) {
 						return "Losing";
 					} else {
 						return "Close";
 					}
 				}
 			}
 		}
 
 		else {
 			// Not in the danger zone. This will be the majority of the time.
 
 		}
 
 		return "Close";
 	}
 
 	// Checks if there is a valid target, and picks one. At some point this will
 	// not only pick a target, but pick the best target. Later, it will evaluate
 	// the worth of the targeting decision, and pass back to the main phase a
 	// tuple that is the recommended target and the value of using the card on
 	// that target. Returns true iff target chosen. For things that have a
 	// plural number of possible targets, or can target multiple types of
 	// things, I'm going to need a generic cost algorithm that decides the best
 	// target for the situation.
 	private boolean evaluateTargets(Card c) {
 		String effects = c.getEffects();
 		String[] effectList = effects.split(",");
 		String[] effect;
 		for (String s : effectList) {
 			effect = s.split(" ");
 			switch (effect[0]) {
 			case "Destroy":
 				switch (effect[1]) {
 				case "Target":
 					switch (effect[2]) {
 					case "Creature":
 						if (effect.length > 3) {
 							switch (effect[3]) {
 							// Nothing goes here right now, but I know some
 							// cards have strange conditions on them, so yeah. I
 							// may change this up at some point to iterate
 							// through the size of 'effect' and have switches
 							// for both the iterating integer and the words
 							// themselves.
 
 							}
 
 						}
 
 						else {
 							ArrayList<Card> creatures = this.gameState
 									.getCreatures();
 							int highestEnemyPower = -1;
 							Card target = null;
 							for (Card cr : creatures) {
 								if (!(cr.getOwner().equals(this))) {
 									if (cr.getPower() > highestEnemyPower) {
 										highestEnemyPower = cr.getPower();
 										target = cr;
 									}
 								}
 							}
 							if (null == target) {
 								return false;
 							} else {
 								c.setTarget(target);
 								return true;
 
 							}
 
 						}
 
 					}
 
 				}
 
 			default:
 				break;
 
 			}
 
 		}
 
 		return false;
 	}
 
 	// TODO: Same as with Sorceries - have them go on the stack, and make the
 	// game state figure out what to do with them.
 	private void playInstant(int handIndex) {
 		Card c = this.hand.remove(handIndex);
 		this.payCost(c);
 		String effects = c.getEffects();
 		String[] effectList = effects.split(",");
 		String[] effect;
 		for (String s : effectList) {
 			effect = s.split(" ");
 			switch (effect[0]) {
 			case "Draw":
 				this.draw(Integer.parseInt(effect[1]));
 				break;
 
 			case "Destroy":
 				switch (effect[1]) {
 				case "Target":
 					// After some though, I've determined that the card should
 					// know its target[s] and that they will be determined
 					// before we get here. All the rest of the code will be
 					// assuming that the target is pre-determined.
 
 					switch (effect[2]) {
 					case "Creature":
 						this.gameState.destroy(c.getTarget());
 					}
 				case "All":
 					switch (effect[2]) {
 					case "Creatures":
 						this.gameState.boardWipe("Creatures");
 						break;
 					case "Artifacts":
 						this.gameState.boardWipe("Artifacts");
 						break;
 					case "Lands":
 						this.gameState.boardWipe("Lands");
 						break;
 					case "Enchantments":
 						this.gameState.boardWipe("Enchantments");
 						break;
 					case "Planeswalkers":
 						this.gameState.boardWipe("Planeswalkers");
 						break;
 					case "Nonland":
 						switch (effect[3]) {
 						case "Permanents":
 							this.gameState.boardWipe("Creatures");
 							this.gameState.boardWipe("Artifacts");
 							this.gameState.boardWipe("Enchantments");
 							this.gameState.boardWipe("Planeswalkers");
 							break;
 						}
 					}
 
 				}
 
 			}
 
 		}
 
 	}
 
 	// TODO: Once the stack is a thing, have this put the card on the stack and
 	// let the game state figure it out.
 	private void playSorcery(int handIndex) {
 		Card c = this.hand.remove(handIndex);
 		this.payCost(c);
 		String effects = c.getEffects();
 		String[] effectList = effects.split(",");
 		String[] effect;
 		for (String s : effectList) {
 			effect = s.split(" ");
 			switch (effect[0]) {
 			case "Draw":
 				this.draw(Integer.parseInt(effect[1]));
 				break;
 
 			case "Destroy":
 				switch (effect[1]) {
 				case "Target":
 					// After some though, I've determined that the card should
 					// know its target[s] and that they will be determined
 					// before we get here. All the rest of the code will be
 					// assuming that the target is pre-determined.
 
 					switch (effect[2]) {
 					case "Creature":
 						this.gameState.destroy(c.getTarget());
 						if (effect.length > 3) {
 							// TODO: put possible cases in here for stuff that
 							// has multiple destroys. Else, treat each instance
 							// as a separate ability, and have the targets thing
 							// work on it accordingly.
 						}
 					}
 				case "All":
 					switch (effect[2]) {
 					case "Creatures":
 						this.gameState.boardWipe("Creatures");
 						break;
 					case "Artifacts":
 						this.gameState.boardWipe("Artifacts");
 						break;
 					case "Lands":
 						this.gameState.boardWipe("Lands");
 						break;
 					case "Enchantments":
 						this.gameState.boardWipe("Enchantments");
 						break;
 					case "Planeswalkers":
 						this.gameState.boardWipe("Planeswalkers");
 						break;
 					case "Nonland":
 						switch (effect[3]) {
 						case "Permanents":
 							this.gameState.boardWipe("Creatures");
 							this.gameState.boardWipe("Artifacts");
 							this.gameState.boardWipe("Enchantments");
 							this.gameState.boardWipe("Planeswalkers");
 							break;
 						}
 					}
 
 				}
 
 			}
 		}
 	}
 
 	private void playPermanentCard(int handIndex) {
 		Card cardToPlay = this.hand.remove(handIndex);
 		this.payCost(cardToPlay);
 		// TODO: check ETB effects, or make the game deal with them.
 		this.gameState.addPermanent(cardToPlay);
 		// System.out.println("Player " + this.playerNumber + " has played "
 		// + cardToPlay.getName() + ".");
 
 	}
 
 	// So I wasn't actually paying the cost for cards. Whoops. This method
 	// re-evaluates the mana cost, then taps lands accordingly. This will have
 	// to be majorly overhaulled when I implement mana dorks and mana artifacts.
 	private void payCost(Card cardToPlay) {
 		int[] manaCost = this.evaluateCardCost(cardToPlay);
 		String[] possibleLands = { "Swamp", "Island", "Forest", "Mountain",
 				"Plains" };
 
 		int specificMana = 0;
 		for (int i = 0; i < 6; i++) {
 			specificMana = manaCost[i];
 			switch (i) {
 			case 0:
 				for (int j = 0; j < specificMana; j++) {
 					this.gameState.getNextUntappedPerm(possibleLands[i], this)
 							.setTapped(true);
 				}
 				break;
 			case 1:
 				for (int j = 0; j < specificMana; j++) {
 					this.gameState.getNextUntappedPerm(possibleLands[i], this)
 							.setTapped(true);
 				}
 				break;
 			case 2:
 				for (int j = 0; j < specificMana; j++) {
 					this.gameState.getNextUntappedPerm(possibleLands[i], this)
 							.setTapped(true);
 				}
 				break;
 			case 3:
 				for (int j = 0; j < specificMana; j++) {
 					this.gameState.getNextUntappedPerm(possibleLands[i], this)
 							.setTapped(true);
 				}
 				break;
 			case 4:
 				for (int j = 0; j < specificMana; j++) {
 					this.gameState.getNextUntappedPerm(possibleLands[i], this)
 							.setTapped(true);
 				}
 				break;
 			case 5:
 				for (int j = 0; j < specificMana; j++) {
 					for (String landName : possibleLands) {
 						if (!(null == this.gameState.getNextUntappedPerm(
 								landName, this))) {
 							this.gameState.getNextUntappedPerm(landName, this)
 									.setTapped(true);
 						}
 					}
 				}
 
 				break; // Some slightly complicated logic will end up going
 						// here. For now, I'm just going to tap the next
 						// avaliable land that I control, as bad an idea as that
 						// is in practice.
 
 			}
 
 		}
 
 	}
 
 	private int[] evaluateCardCost(Card cardToPlay) {
 		// The mana cost array looks at mana in alphabetical order for colors,
 		// with colorless last. The specific order is [Black, Blue, Green, Red,
 		// White, Colorless].
 		int[] manaCost = { 0, 0, 0, 0, 0, 0 };
 
 		// This will have to change a bit when I introduce hybrid mana and
 		// alternate costs. It's fine for the current mono green implementation
 		// though.
 		for (char ch : cardToPlay.getCost().toCharArray()) {
 			switch (ch) {
 			case 'B':
 				manaCost[0]++;
 				break;
 			case 'U':
 				manaCost[1]++;
 				break;
 			case 'G':
 				manaCost[2]++;
 				break;
 			case 'R':
 				manaCost[3]++;
 				break;
 			case 'W':
 				manaCost[4]++;
 				break;
 			case '1':
 				manaCost[5]++;
 				break;
 			default:
 				break;
 			}
 		}
 
 		return manaCost;
 	}
 
 	// This determines the mana cost of the card via the same method that the
 	// evaluateOpenMana evaluates avaliable mana, but using the cost string that
 	// each card has, instead of building up a fresh string.
 	private boolean evaluateCardPlayable(Card c) {
 		// There are spells that have no cost, and those cannot be played for
 		// their cost, seeing as they don't have one. Thus, despite not
 		// implementing those cards for a while, there will be a special case
 		// where I check to see if their cost is null.
 
 		if (c.getCost().equals("0")) {
 			return false;
 		}
 
 		// I've found that having the card's mana cost by itself is a desirable
 		// thing, so I made a separate method for it.
 
 		int[] manaCost = this.evaluateCardCost(c);
 
 		// Here I check each of the five colors, and ensure that I have enough
 		// mana open to pay for the colored costs.
 		for (int i = 0; i < 5; i++) {
 			if (this.manaOpen[i] < manaCost[i]) {
 				return false;
 			}
 		}
 		// This part is only reachable if all the colored costs can be payed.
 		// Here it checks the colorless cost against avaliable colorless mana
 		// and all left over colored mana.
 
 		int remainingMana = 0;
 		for (int i = 0; i < 5; i++) {
 			remainingMana += (this.manaOpen[i] - manaCost[i]);
 		}
 
 		// Add in colorless mana and check total remaining mana against
 		// colorless cost. Return false if mana insufficient, otherwise pass to
 		// a default return true, which is the default because zero mana stuff
 		// exists.
 		remainingMana += this.manaOpen[5];
 		if (remainingMana < manaCost[5]) {
 			return false;
 		}
 
 		return true;
 	}
 
 	// Here, we evaluate open mana by building a string of open mana and
 	// parsing it into integers on a case-by-case basis.
 	private void evaluateOpenMana() {
 		String openMana = this.gameState.openMana(this);
 		this.manaOpen = new int[6];
 		for (char ch : openMana.toCharArray()) {
 			switch (ch) {
 			case 'B':
 				this.manaOpen[0]++;
 				break;
 			case 'U':
 				this.manaOpen[1]++;
 				break;
 			case 'G':
 				this.manaOpen[2]++;
 				break;
 			case 'R':
 				this.manaOpen[3]++;
 				break;
 			case 'W':
 				this.manaOpen[4]++;
 				break;
 			case '1':
 				this.manaOpen[5]++;
 				break;
 			default:
 				break;
 
 			}
 
 		}
 
 	}
 
 	// Here the player shuffle's the player's library(deck). This is implemented
 	// in constant time by making a new ArrayList<Card> and removing cards from
 	// it in a random order, putting them into the new construct, which when
 	// full is put in this.deck.
 	public void shuffle() {
 		ArrayList<Card> shuffledDeck = new ArrayList<Card>();
 		while (this.deck.size() > 0) {
 			shuffledDeck.add(this.deck.remove((int) (Math.random() * this.deck
 					.size())));
 		}
 		this.deck = shuffledDeck;
 	}
 
 	// Takes an array of controlled creatures, and tells them all to attack by
 	// tapping them and then sending back to the GameState the list of creatures
 	// that are attacking. More complex logic will go in here later, when I
 	// start REALLY simulating games. I've also added in that it assigns
 	// creatures in order to players on the players list. With only a two-player
 	// game, it will always attack the opponent.
 	public ArrayList<Card> chooseAttackers(ArrayList<Card> controlledCreatures) {
 		int playerIndex = 0;
 		ArrayList<Card> attackingCreatures = new ArrayList<Card>();
 		for (Card creature : controlledCreatures) {
 			if (!creature.hasSummoningSickness()) {
 				creature.setTapped(true);
 				creature.setAttacking(true);
 				attackingCreatures.add(creature);
 			}
 			// Makes sure I'm not trying to attack myself.
 			if (this.gameState.getPlayers().get(playerIndex)
 					.equals(this.gameState.getActivePlayer())) {
 				playerIndex++;
 			}
 			// Checks to avoid array out of bounds errors.
 			if (playerIndex >= this.gameState.getNumPlayers()) {
 				playerIndex = 0;
 
 			}
 			creature.setDefendingPlayer(this.gameState.getPlayers().get(
 					playerIndex));
 		}
 		// TODO: create real logic for attacking
 		return attackingCreatures;
 
 	}
 
 	// For now, this is just going to declare a creature blocking for each
 	// attacking creature, in order from greatest attacking power to least. It
 	// will consider things like optimal combat outcome later, after I've tested
 	// the basic code.
 	public void chooseBlockers(ArrayList<Card> controlledCreatures,
 			ArrayList<Card> creaturesAttackingMe) {
 		// TODO Make blocking actually make good decisions.
 		// TODO: Edit creatures so that they can have multiple blockers, and
 		// then edit GameState to handle those properly, and have Player assign
 		// them properly.
 		switch (this.playerStyle) {
 		case 0:
 
 			// The goal with Style 0 is to prevent as much damage as possible,
 			// regardless of if it destroys our creatures. This does not account
 			// for trample.
 			int highestAttackingPower = -1;
 			Card highestPowerAttacker = null;
 			while (creaturesAttackingMe.size() > 0
 					&& controlledCreatures.size() > 0) {
 				for (Card creature : creaturesAttackingMe) {
 					if (creature.getPower() > highestAttackingPower) {
 						highestAttackingPower = creature.getPower();
 						highestPowerAttacker = creature;
 					}
 				}
 				Card creature = controlledCreatures.remove(0);
 				highestPowerAttacker.setBlockedBy(creature);
 				creature.setBlocking(highestPowerAttacker);
 				highestPowerAttacker.setBlocked(true);
 				creaturesAttackingMe.remove(highestPowerAttacker);
 			}
 			break;
 
 		case 1:
 			// Our goal with style 1 is to destroy enemy creatures via blocking.
 			// A secondary goal is to block remaining creatures a la style 0.
 			Card lowestToughnessAttacker = null;
 			int lowestAttackingToughness = Integer.MAX_VALUE;
 			while (creaturesAttackingMe.size() > 0
 					&& controlledCreatures.size() > 0) {
 				for (Card creature : creaturesAttackingMe) {
 					if (creature.getToughness() < lowestAttackingToughness) {
 						lowestAttackingToughness = creature.getToughness();
 						lowestToughnessAttacker = creature;
 					}
 					// May as well have a bit of smart prioritizing, eliminating
 					// higher power creatures if they have the same toughness.
 					else if (creature.getToughness() == lowestAttackingToughness) {
 						if (creature.getToughness() > lowestToughnessAttacker
 								.getToughness()) {
 							lowestAttackingToughness = creature.getToughness();
 							lowestToughnessAttacker = creature;
 
 						}
 					}
 				}
 
 				// Here is some tricky logic, because we want to block with the
 				// creature that has power closest to the attacking creature's
 				// toughness, without being under it. The secondary condition
 				// for an even pairing will be to preserve my creature if
 				// possible. In the event that both creatures of the two
 				// compared would be sacrificed, it sacrifices the one with the
 				// lowest toughness.
 				Card closestMatchingBlocker = controlledCreatures.get(0);
 				int closestMatchingPower = closestMatchingBlocker.getPower();
 
 				for (Card c : controlledCreatures) {
 					if (c.getPower() >= lowestAttackingToughness) {
 						if ((c.getPower() - lowestAttackingToughness) < (closestMatchingPower - lowestAttackingToughness)) {
 
 							closestMatchingBlocker = c;
 							closestMatchingPower = c.getPower();
 
 						} else if ((c.getPower() - lowestAttackingToughness) == (closestMatchingPower - lowestAttackingToughness)) {
 							if (c.getToughness() > lowestToughnessAttacker
 									.getPower()
 									&& closestMatchingBlocker.getToughness() < lowestToughnessAttacker
 											.getPower()) {
 
 								closestMatchingBlocker = c;
 								closestMatchingPower = c.getPower();
 							}
 						}
 					}
 				}
 
 				Card creature = closestMatchingBlocker;
 				controlledCreatures.remove(closestMatchingBlocker);
 				lowestToughnessAttacker.setBlockedBy(creature);
 				creature.setBlocking(lowestToughnessAttacker);
 				lowestToughnessAttacker.setBlocked(true);
 				creaturesAttackingMe.remove(lowestToughnessAttacker);
 
 			}
 
 			break;
 
 		case 2:
 			// Style 2 is going to be a more comprehensive one that aims to
 			// maximize a potential life heuristic.
 
 			break;
 
 		default:
 			break;
 
 		}
 	}
 
 	public void destroy(Card c) {
 		this.graveyard.add(c);
 		// System.out.println("Player " + this.playerNumber + "'s "
 		// + creature.getName() + " has been destroyed.");
 
 	}
 
 	public int getPlayerNumber() {
 		return playerNumber;
 	}
 
 	public void setPlayerNumber(int playerNumber) {
 		this.playerNumber = playerNumber;
 	}
 
 	public boolean isPlayedLand() {
 		return playedLand;
 	}
 
 	public void setPlayedLand(boolean playedLand) {
 		this.playedLand = playedLand;
 	}
 
 	public String[] getDeckList() {
 		return deckList;
 	}
 
 	public void setDeckList(String[] deckList) {
 		this.deckList = deckList;
 	}
 
 }
