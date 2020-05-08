 package net.thiagoalz.hermeto.panel;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.thiagoalz.hermeto.panel.listeners.SelectionEvent;
 import net.thiagoalz.hermeto.panel.listeners.SelectionListener;
 import net.thiagoalz.hermeto.player.DefaultPlayer;
 import net.thiagoalz.hermeto.player.Player;
 import net.thiagoalz.hermeto.player.Player.Direction;
 import net.thiagoalz.hermeto.player.PlayersManager;
 import android.util.Log;
 
 /**
  * Manages the interaction of the users and the panel.
  * 
  * @author Gabriel Ozeas de Oliveira
  * @version 0.1
  */
 public class GameManager implements SquarePanelManager, PlayersManager {
 	
 	private static final int COLUMNS_CONF = 16;
 	private static final int ROWS_CONF = 16;
 	
 	private static final String tag = GameManager.class.getCanonicalName();
 	
 	private int columns;
 	private int rows;
 	
 	private Map<String, Player> players = new LinkedHashMap<String, Player>();
 	private int playerCounter = 0;
 	
 	private Set<Position> markedSquares = new LinkedHashSet<Position>();
 	
 	private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
 	
 	private static GameManager instance = new GameManager(COLUMNS_CONF, ROWS_CONF); //Just to avoid syncronization problems
 	
 	public static GameManager getInstance(){
 		//if(GameManager.instance == null)
 			//GameManager.instance = new GameManager(COLUMNS_CONF, ROWS_CONF);
 			
 		return GameManager.instance;
 	}
 	
 	/**
 	 * Constructor that receives the dimension of the panel.
 	 * 
 	 * @param columns The number of columns of the panel.
 	 * @param rows The number of rows of the panel.
 	 */
 	private GameManager(int columns, int rows) {
 		this(columns, rows, null);
 	}
 	
 	/**
 	 * Constructor that received the dimension of the panel and the players.
 	 * 
 	 * @param columns The number of columns of the panel.
 	 * @param rows The number of rows of the panel.
 	 * @param players The list of players.
 	 */
 	private GameManager(int columns, int rows, Map<String, Player> players) {
 		if (players != null) {
 			this.players = players;
 		}
 		Log.d(tag, "Creating game with [" + columns + ", " + rows + "]");
 		this.columns = columns > 1 ? columns : 2;
 		this.rows = rows > 1 ? columns : 2;
 	}
 	
 	@Override
 	public boolean move(Player player, Direction direction) {
 		int x = player.getPosition().getX();
 		int y = player.getPosition().getY();
 		
 		Log.d(tag, "Actual position of the player: [" + x + ", " + y + "]");
 		
 		switch (direction) {
 			case LEFT:
 				if (x - 1 < 1) {
 					return false;
 				} else {
 					Log.d(tag, "Changing position of the player: [" + (x - 1) + ", " + y + "]");
 					player.getPosition().setX(x - 1);
 					return true;
 				}
 				
 			case RIGHT:
 				if (x + 1 > columns) {
 					return false;
 				} else {
 					Log.d(tag, "Changing position of the player: [" + (x + 1) + ", " + y + "]");
 					player.getPosition().setX(x + 1);
 					return true;
 				}
 				
 			case TOP:
 				if (y - 1 < 1) {
 					return false;
 				} else {
 					Log.d(tag, "Changing position of the player: [" + x + ", " + (y - 1) + "]");
 					player.getPosition().setY(y - 1);
 					return true;
 				}
 				
 			case DOWN:
 				if (y + 1 > rows) {
 					return false;
 				} else {
 					Log.d(tag, "Changing position of the player: [" + x + ", " + (y + 1) + "]");
 					player.getPosition().setY(y + 1);
 					return true;
 				}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean mark(Player player) {
 		Position selectedPosition = new Position(player.getPosition().getX(), player.getPosition().getY());
 		if (markedSquares.contains(selectedPosition)) {
 			markedSquares.remove(selectedPosition);
 			notifyDeselection(player, selectedPosition);
 			return false;
 		} else {
 			Log.d(tag, "Marking square [" + player.getPosition().getX() + ", "+ player.getPosition().getY() +"]");
 			markedSquares.add(player.getPosition());
 			notifySelection(player, selectedPosition);
 			return true;
 		}
 	}
 	
 	@Override
 	public Position[] getMarkedSquares() {
 		Log.d(tag, markedSquares.size() + " squares marked");
 		return (Position[]) markedSquares.toArray();
 	}
 
 	public int getColumns() {
 		return columns;
 	}
 
 	public int getRows() {
 		return rows;
 	}
 
 	@Override
 	public Player connectPlayer() {
		String playerID = "playerID-" + System.nanoTime();
 		String playerName = "Player " + (++playerCounter);
 		
 		Position position = null;
 		if (playerCounter % 2 != 0) {
 			position = new Position(1,1);
 		} else {
 			position = new Position(columns, rows);
 		}
 		
 		DefaultPlayer player = new DefaultPlayer(playerName, playerID);
 		player.setPosition(position);
 		
 		Log.d(tag, "Connection " + playerName + "("+ playerID +") at the position [" 
 				+ position.getX() + ", " + position.getY() + "]");
 		players.put(player.getId(), player);
 		return player;
 	}
 
 	@Override
 	public void disconnectPlayer(Player player) {
 		players.remove(player.getId());
 	}
 
 	@Override
 	public Map<String, Player> getPlayers() {
 		return players;
 	}
 	
 	@Override
 	public Player getPlayer(String playerID) {
 		return players.get(playerID);
 	}
 	
 	public void addSelectionListener(SelectionListener selectionListener) {
 		this.selectionListeners.add(selectionListener);
 	}
 	
 	public void removeSelectionListener(SelectionListener selectionListener) {
 		this.selectionListeners.remove(selectionListener);
 	}
 	
 	private void notifySelection(Player player, Position position) {
 		SelectionEvent event = new SelectionEvent(player, position);
 		for (SelectionListener listener : selectionListeners) {
 			listener.onSelected(event);
 		}
 	}
 	
 	private void notifyDeselection(Player player, Position position) {
 		SelectionEvent event = new SelectionEvent(player, position);
 		for (SelectionListener listener : selectionListeners) {
 			listener.onDeselected(event);
 		}
 	}
 }
