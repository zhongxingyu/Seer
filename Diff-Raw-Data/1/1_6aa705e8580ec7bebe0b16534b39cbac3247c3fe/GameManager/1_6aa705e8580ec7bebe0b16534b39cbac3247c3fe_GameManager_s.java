 package riskyspace;
 
 import riskyspace.logic.FleetMove;
 import riskyspace.model.Player;
 import riskyspace.model.World;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.services.EventHandler;
 
 public class GameManager implements EventHandler {
 
 	private Player[] players = null;
 	private Player currentPlayer = null;
 	private World world = null;
 	private int turn;
 	
 	public GameManager(World world, int nbrOfPlayers) {
 		this.world = world;
 		players = new Player[]{Player.BLUE, Player.RED};
 		currentPlayer = players[0];
 		EventBus.INSTANCE.addHandler(this);
 		turn = 1;
 	}
 
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 	
 	public int getTurn() {
 		return turn;
 	}
 	
 	private void changePlayer() {
 		currentPlayer = (currentPlayer == Player.BLUE) ? Player.RED : Player.BLUE;
 		/*
 		 * Give income in ViewEventController instead?
 		 */
 		world.giveIncome(currentPlayer);
 		Event event = new Event(Event.EventTag.ACTIVE_PLAYER_CHANGED, currentPlayer);
 		EventBus.INSTANCE.publish(event);
 	}
 
 	@Override
 	public void performEvent(Event evt) {
 		if (evt.getTag() == Event.EventTag.NEXT_TURN) {
 			if (!FleetMove.isMoving()) {
 				changePlayer();
 				if (currentPlayer == Player.BLUE) {
 					turn++;
 				}
 				world.resetShips();
 			}
 		}
 	}
 }
