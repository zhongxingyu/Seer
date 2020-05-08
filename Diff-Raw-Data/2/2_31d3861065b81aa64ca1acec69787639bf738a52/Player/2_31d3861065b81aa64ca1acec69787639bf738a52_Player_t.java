 package com.reubenpeeris.wippen.engine;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import lombok.NonNull;
 
 import com.reubenpeeris.wippen.expression.Card;
 import com.reubenpeeris.wippen.expression.Move;
 import com.reubenpeeris.wippen.expression.Pile;
 import com.reubenpeeris.wippen.robot.Robot;
 
 public final class Player {
 	public static final Player NOBODY = new Player(-1, new Robot() {
 		@Override
 		public void startMatch(List<Player> allPlayers, Player you, int numberOfSets) {
 		}
 
 		@Override
 		public void startSet() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void gameComplete(List<Score> scores) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void setComplete(List<Score> scores) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void matchComplete(List<Score> scores) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public String getName() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void startGame(Player first, Collection<Pile> table) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void cardsDealt(Collection<Card> hand) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public Move takeTurn(Collection<Pile> table, Collection<Card> hand) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void turnPlayed(Player player, Collection<Pile> table, Move move) {
 			throw new UnsupportedOperationException();
 		}
 	});
 
 	private final Collection<Card> capturedCards = new ArrayList<>();
 	private final int position;
 	private final Robot robot;
 	private final Score score = new Score();
	private Collection<Card> hand = Collections.emptyList();
 	private int sweeps;
 
 	public Player(int position, @NonNull Robot robot) {
 		this.robot = robot;
 		this.position = position;
 	}
 
 	public int getPosition() {
 		return position;
 	}
 
 	Collection<Card> getHand() {
 		return hand;
 	}
 
 	boolean removeFromHand(Card card) {
 		return hand.remove(card);
 	}
 
 	void setHand(Collection<Card> hand) {
 		this.hand = hand;
 	}
 
 	boolean isHandEmpty() {
 		return hand.isEmpty();
 	}
 
 	// Scoring
 	Score getScore() {
 		return score;
 	}
 
 	void addSweep() {
 		sweeps++;
 	}
 
 	int getSweeps() {
 		return sweeps;
 	}
 
 	void addToCapturedCards(Collection<Card> cardsUsed) {
 		capturedCards.addAll(cardsUsed);
 	}
 
 	Collection<Card> getCapturedCards() {
 		return capturedCards;
 	}
 
 	void clearCapturedAndSweeps() {
 		capturedCards.clear();
 		sweeps = 0;
 	}
 
 	@Override
 	public String toString() {
 		return "Player " + position + " [" + robot.getName() + "]";
 	}
 
 	// Robot like methods
 	void startMatch(List<Player> allPlayers, int numberOfSets) {
 		score.startMatch();
 		robot.startMatch(allPlayers, this, numberOfSets);
 	}
 
 	void startSet() {
 		score.startSet();
 		robot.startSet();
 	}
 
 	void startGame(Player first, Collection<Pile> table) {
 		score.startGame();
 		robot.startGame(first, table);
 	}
 
 	void gameComplete(List<Score> scores) {
 		robot.gameComplete(scores);
 	}
 
 	void setComplete(List<Score> scores) {
 		robot.setComplete(scores);
 	}
 
 	void matchComplete(List<Score> scores) {
 		robot.matchComplete(scores);
 	}
 
 	void cardsDealt(Collection<Card> hand) {
 		robot.cardsDealt(hand);
 	}
 
 	Move takeTurn(Collection<Pile> table) {
 		return robot.takeTurn(table, Collections.unmodifiableCollection(this.hand));
 	}
 
 	void turnPlayed(Player player, Collection<Pile> table, Move move) {
 		robot.turnPlayed(player, table, move);
 	}
 }
