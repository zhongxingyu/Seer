 /**
  * 
  */
 package com.rachum.amir.skyhiking;
 
 import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
import org.apache.commons.lang3.StringUtils;

 import com.rachum.amir.skyhiking.GameEvent.Type;
 import com.rachum.amir.skyhiking.players.Player;
 
 /**
  * @author Amir Rachum
  *
  */
 public class Game extends Thread {
 	public final List<Player> players;
 	public List<Player> remainingPlayers;
 	private final Deck deck;
 	private final Collection<GameEventListener> listeners = new LinkedList<GameEventListener>();
 	public Collection<Card> diceRoll;
 	final Iterator<Player> pilotIterator;
     public Player pilot;
     
 	private enum LevelOutcome {SUCCESS, CRASH;}
 	
 	public Game(final List<Player> players) {
 		super();
 		this.players = players;
         for (final Player player : players) {
         	registerListener(player);
         }
 		deck = new Deck();
 		for (final Player player : players) {
             player.gameStart(deck); //TODO: move this functionality to event
 		}
     	pilotIterator = new InfiniteIterator(players);
         remainingPlayers = new LinkedList<Player>(players);
 	}
 
     public void registerListener(final GameEventListener listener) {
     	listeners.add(listener);
     }
     
 	private void announce(final GameEvent event) {
 		Collection<EventHandler> handlers = new LinkedList<EventHandler>();
 		for (final GameEventListener listener : listeners) {
 			EventHandler handler = new EventHandler();
 			handlers.add(handler);
 			listener.handleEvent(event, handler);
 		}
 		//TODO: to optimize runtime, this could be done BEFORE the announce on
 		//the previous handlers.
 		for (final EventHandler handler : handlers) {
 			handler.waitUntilDone();
 		}
 	}
 
 	@Override
 	public void run() {
         announce(new GameEvent(Type.GAME_BEGIN, this));
 		while (true) {
 			remainingPlayers = new LinkedList<Player>(players);
             announce(new GameEvent(Type.ROUND_BEGIN, this));
 			for (final CloudLevel level : CloudLevel.gameLevels()) {
 				setNextPilot();
 				
                 final GameEvent event = new GameEvent(Type.LEVEL_BEGIN, this);
                 event.level = level;
                 announce(event);
                 
 				if (level.getDiceNumber() == 0) {
 					for (final Player player : remainingPlayers) {
 						player.score(level.getScore()); //TODO: move to listener
 					}
                     announce(new GameEvent(Type.LEVEL_END, this));
                     announce(new GameEvent(Type.ROUND_END, this));
                     break;
 				}
                 
                 
                 final LevelOutcome outcome = playLevel(level);
                 if (outcome == LevelOutcome.CRASH) {
                     announce(new GameEvent(Type.LEVEL_END, this));
                     announce(new GameEvent(Type.ROUND_END, this));
                 	break;
                 }
                 announce(new GameEvent(Type.LEVEL_END, this));
 				if (remainingPlayers.isEmpty()) {
                     announce(new GameEvent(Type.ROUND_END, this));
 					break; //Move to next level
 				}
 				
 			}
			Player leadingPlayer = Collections.max(players, 
					new Comparator<Player>() {
				@Override
				public int compare(Player lhs, Player rhs) {
					// TODO Auto-generated method stub
					return lhs.getScore() - rhs.getScore();
				}
			});
			if (leadingPlayer.getScore() >= 50) {
                final GameEvent event = new GameEvent(Type.GAME_END, this);
                event.winner = leadingPlayer;
                announce(event);
                return;
			}
 		}
 	}
 
 	private void setNextPilot() {
         Player player = pilotIterator.next();
         while (!remainingPlayers.contains(player)) {
         	player = pilotIterator.next();
         }
         pilot = player;
 	}
 
 	public LevelOutcome playLevel(final CloudLevel level) {
 		diceRoll = Dice.roll(level.getDiceNumber());
         announce(new GameEvent(Type.DICE_ROLLED, this));
         
 		final List<Player> newRemainingPlayers = new LinkedList<Player>();
 		newRemainingPlayers.addAll(remainingPlayers);
 		for (final Player player : getRemainingPlayersInOrder()) {
 			if (player != pilot) {
                 final MoveHandler handler = new MoveHandler();
                 player.play(handler, this);
 				final Move move = handler.getMove();
 				if (move == Move.LEAVE) {
 					player.score(level.getScore());
 					newRemainingPlayers.remove(player);
 				}
 				final GameEvent event = new GameEvent(Type.MOVE, this);
 				event.move = move;
                 event.currentPlayer = player;
 				announce(event);
 			}
 		}
 		remainingPlayers.retainAll(newRemainingPlayers);
 		
 		final PayHandler handler = new PayHandler();
 		pilot.pay(handler, this);
 		GameEvent event = new GameEvent(Type.PAY, this);
 		event.didPay = handler.didPay();
 		event.cardsPayed = handler.getCards();
 		announce(event);
 		if (handler.didPay()) {
             return LevelOutcome.SUCCESS;
 		}
         return LevelOutcome.CRASH;
 		
 	}
 	
 	private Collection<? extends Player> getRemainingPlayersInOrder() {
         final List<Player> newRemainingPlayers = new LinkedList<Player>();
         newRemainingPlayers.add(pilot);
         final Iterator<Player> iterator = new InfiniteIterator(remainingPlayers);
         while (iterator.next() != pilot) {}
         Player player = iterator.next();
         while (player != pilot) {
         	newRemainingPlayers.add(player);
             player = iterator.next();
         }
 		return newRemainingPlayers;
 	}
 }
