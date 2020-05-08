 package kj.scotlyard.game.rules;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.attributes.DefaultSealable;
 
 import kj.scotlyard.game.graph.ConnectionEdge;
 import kj.scotlyard.game.graph.StationVertex;
 import kj.scotlyard.game.model.AbstractGameState;
 import kj.scotlyard.game.model.DetectivePlayer;
 import kj.scotlyard.game.model.DefaultGameState;
 import kj.scotlyard.game.model.GameState;
 import kj.scotlyard.game.model.Move;
 import kj.scotlyard.game.model.MrXPlayer;
 import kj.scotlyard.game.model.Player;
 import kj.scotlyard.game.model.items.Item;
 
 public class TheGameStateAccessPolicy implements GameStateAccessPolicy {
 
 	/**
 	 * This class is a proxy for such Moves, where station and connection are
 	 * hidden (that means, the most moves of MrX). This two details are not
 	 * allowed to access. Each attempt ends in a <tt>RuntimeException</tt>.
 	 * 
 	 * @author jakob190590
 	 * 
 	 */
 	private class MrXMove extends DefaultSealable implements Move {
 
 		private boolean uncovered;
 		
 		private Move move;
 
 		private List<Move> moves;
 
 		/**
 		 * Erzeugt einen sealed Move Proxy, der den Zugriff auf den echten Move
 		 * einschraenkt. Auf Connection kann dann nicht mehr zugegriffen werden
 		 * (throws IllegalAccessException), auf Station nur bei uncovered Moves. 
 		 * Die "Sub Moves", d.h. die Moves in der Move List werden vom
 		 * Proxy nicht durchgereicht, sondern muessen dem Konstruktor neu
 		 * uebergeben werden!
 		 * 
 		 * @param backingMove
 		 *            der echte Move, fuer den dieser PartialMove einen Proxy
 		 *            darstellt.
 		 * @param uncovered
 		 *            gibt an, ob MrX in diesem Zug sichtbar ist. Wenn ja kann
 		 *            auf Station zugegriffen werden.
 		 * @param subMoves
 		 *            die Moves aus der Move List des echten Moves (eventuell
 		 *            auch wiederum Proxies).
 		 */
 		public MrXMove(Move backingMove, boolean uncovered, Move... subMoves) {
 			this.uncovered = uncovered;
 			move = backingMove;
 			moves = Arrays.asList(subMoves);
 			seal();
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj instanceof MrXMove) {
 				MrXMove m = (MrXMove) obj;
 				if (m.move == this.move && m.moves.equals(this.moves)) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		@Override
 		public void seal() {
 			super.seal();
 			moves = Collections.unmodifiableList(moves);
 		}
 
 		@Override
 		public Player getPlayer() {
 			return move.getPlayer();
 		}
 
 		@Override
 		public void setPlayer(Player player) {
 			checkSealed();
 		}
 
 		@Override
 		public int getRoundNumber() {
 			return move.getRoundNumber();
 		}
 
 		@Override
 		public void setRoundNumber(int roundNumber) {
 			checkSealed();
 		}
 
 		@Override
 		public int getMoveNumber() {
 			return move.getMoveNumber();
 		}
 
 		@Override
 		public void setMoveNumber(int moveNumber) {
 			checkSealed();
 		}
 
 		@Override
 		public int getMoveIndex() {
 			return move.getMoveIndex();
 		}
 
 		@Override
 		public void setMoveIndex(int moveIndex) {
 			checkSealed();
 		}
 
 		@Override
 		public StationVertex getStation() {
 			if (uncovered) {
 				return move.getStation();
 			}
 			throw new IllegalAccessException("Move detail cannot be accessed.");
 		}
 
 		@Override
 		public void setStation(StationVertex station) {
 			checkSealed();
 		}
 
 		@Override
 		public ConnectionEdge getConnection() {
 			throw new IllegalAccessException("Move detail cannot be accessed.");
 		}
 
 		@Override
 		public void setConnection(ConnectionEdge connection) {
 			checkSealed();
 		}
 
 		@Override
 		public Item getItem() {
 			return move.getItem();
 		}
 
 		@Override
 		public void setItem(Item item) {
 			checkSealed();
 		}
 
 		@Override
 		public List<Move> getMoves() {
 			return moves;
 		}
 
 	}
 
 	private class DetectivesGameState extends AbstractGameState {
 
 		private GameState gameState;
 
 		public DetectivesGameState(GameState gameState) {
 			this.gameState = new DefaultGameState(gameState);
 			// wer weiss, was gameState in Wirklichkeit ist... es koennte
 			// z.B. Schreibzugriff gewaehren :o
 			// Deswegen lieber read-only wrappen
 		}
 
 		private Move maskMove(Move move) {
 			
 			if (move.getPlayer() instanceof DetectivePlayer) {
 				return move;
 			}
 			
 			// Sonst: MrX' Move
 			if (getMrXUncoverMoveNumbers().contains(move.getMoveNumber())) { // das impliziert, dass es keine sub moves gibt.
 				// MrX is uncovered				
 				return new MrXMove(move, true);
 			}
 
 			// "undercover" Move, enventually with sub moves
 			Move[] arr = new Move[move.getMoves().size()];
 			int i = 0;
 			boolean uncovered = false;
 			for (Move m : move.getMoves()) {
 				uncovered = getMrXUncoverMoveNumbers().contains(m.getMoveNumber());
 				arr[i] = new MrXMove(m, uncovered);
 				i++;
 			}
 			
 			// uncovered je nachdem, ob letzter sub move uncovered war.
 			return new MrXMove(move, uncovered, arr);
 		}
 
 		@Override
 		public MrXPlayer getMrX() {
 			return gameState.getMrX();
 		}
 
 		@Override
 		public List<DetectivePlayer> getDetectives() {
 			return gameState.getDetectives();
 		}
 
 		@Override
 		public List<Player> getPlayers() {
 			return gameState.getPlayers();
 		}
 
 		@Override
 		public Set<Item> getItems(Player player) {
 			return gameState.getItems(player);
 		}
 
 		@Override
 		public List<Move> getMoves() {
 			// Wie die meisten der Referenzimplementierungen hier:
 			// Koennte effizienter gemacht werden.
 
 			@SuppressWarnings("serial")
			List<Move> list = new LinkedList<Move>() {
 
 				@Override
 				public synchronized Move get(int index) {
 					return super.get((index >= 0) ? index : size() + index);
 				}
 
 			};
 			for (Move m : gameState.getMoves()) {
 				list.add(maskMove(m));
 			}
 			return Collections.unmodifiableList(list);
 		}
 
 		@Override
 		public Move getMove(Player player, int number, MoveAccessMode accessMode) {
 			Move m = gameState.getMove(player, number, accessMode);
 			return maskMove(m);
 		}
 
 		@Override
 		public Move getLastMove(Player player) {
			Move m = gameState.getLastMove(player);
			if (m != null) {
				m = maskMove(m);
			}
			return m;
 		}
 
 		@Override
 		public int getCurrentRoundNumber() {
 			return gameState.getCurrentRoundNumber();
 		}
 
 		@Override
 		public Player getCurrentPlayer() {
 			return gameState.getCurrentPlayer();
 		}
 
 	}
 
 	private final List<Integer> uncoverMoveNumbers;
 	{
 		List<Integer> list = new ArrayList<>(4);
 		list.add(3);
 		list.add(8);
 		list.add(13);
 		list.add(18);
 		uncoverMoveNumbers = Collections.unmodifiableList(list);
 	}
 
 	@Override
 	public GameState createGameStateForDetectives(GameState gameState) {
 		return new DetectivesGameState(gameState);
 	}
 
 	@Override
 	public List<Integer> getMrXUncoverMoveNumbers() {
 		return uncoverMoveNumbers;
 	}
 }
