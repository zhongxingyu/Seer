 package net.thiagoalz.hermeto.panel;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.thiagoalz.hermeto.panel.listeners.ConnectEvent;
 import net.thiagoalz.hermeto.panel.listeners.ExecutionControl;
 import net.thiagoalz.hermeto.panel.listeners.ExecutionEvent;
 import net.thiagoalz.hermeto.panel.listeners.ExecutionListener;
 import net.thiagoalz.hermeto.panel.listeners.MoveEvent;
 import net.thiagoalz.hermeto.panel.listeners.PlayerListener;
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
 public class GameManager implements SquarePanelManager, PlayersManager,
 		ExecutionControl {
 
 	private static final int COLUMNS_CONF = 16;
 	private static final int ROWS_CONF = 16;
 
 	private static GameManager instance;
 
 	private static final String tag = GameManager.class.getCanonicalName();
 
 	private int columns;
 	private int rows;
 
 	private Map<String, Player> players = new LinkedHashMap<String, Player>();
 	private int playerCounter = 0;
 
 	private boolean playing = false;
 
 	private Set<Position> markedSquares = new LinkedHashSet<Position>();
 
 	private Timer sequencer;
 	private int currentPlayingLine = 0;
 	private int timeSequence = 200;
 
 	private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
 	private List<ExecutionListener> executionListeners = new ArrayList<ExecutionListener>();
 	private List<PlayerListener> playerListeners = new ArrayList<PlayerListener>();
 
 	public synchronized static GameManager getInstance() {
 		return getInstance(COLUMNS_CONF, ROWS_CONF);
 	}
 
 	public synchronized static GameManager getInstance(int columns, int rows) {
 		if (instance == null)
 			instance = new GameManager(columns, rows);
 		return instance;
 	}
 
 	/**
 	 * Constructor that receives the dimension of the panel.
 	 * 
 	 * @param columns
 	 *            The number of columns of the panel.
 	 * @param rows
 	 *            The number of rows of the panel.
 	 */
 	private GameManager(int columns, int rows) {
 		Log.d(tag, "Creating game with [" + columns + ", " + rows + "]");
 		this.columns = columns > 1 ? columns : 2;
 		this.rows = rows > 1 ? columns : 2;
 	}
 
 	public boolean move(Player player, Position newPosition) {
 		Position oldPos = player.getPosition();
 		player.setPosition(newPosition);
 		Log.d(tag, "Changing position of the player: [" + newPosition.getX()
 				+ ", " + newPosition.getY() + "]");
 		notifyMovePlayerListeners(player, oldPos, player.getPosition());
 
 		return true;
 	}
 
 	@Override
 	public boolean move(Player player, Direction direction) {
 		Position oldPos = player.getPosition();
 
 		int x = oldPos.getX();
 		int y = oldPos.getY();
 
 		Log.d(tag, "Actual position of the player: [" + x + ", " + y + "]");
 
 		switch (direction) {
 		case LEFT:
 			if (x - 1 < 0) {
 				return false;
 			} else {
 				Position newPos = new Position(x - 1, y);
 				return move(player, newPos);
 			}
 
 		case RIGHT:
 			if (x + 1 >= columns) {
 				return false;
 			} else {
 				Position newPos = new Position(x + 1, y);
 				return move(player, newPos);
 			}
 
 		case UP:
 			if (y - 1 < 0) {
 				return false;
 			} else {
 				Position newPos = new Position(x, y - 1);
 				return move(player, newPos);
 			}
 
 		case DOWN:
 			if (y + 1 >= rows) {
 				return false;
 			} else {
 				Position newPos = new Position(x, y + 1);
 				return move(player, newPos);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean mark(Player player) {
 		Position selectedPosition = new Position(player.getPosition().getX(),
 				player.getPosition().getY());
 		if (markedSquares.contains(selectedPosition)) {
 			markedSquares.remove(selectedPosition);
 			notifyDeselection(player, selectedPosition);
 			return false;
 		} else {
 			Log.d(tag, "Marking square [" + player.getPosition().getX() + ", "
 					+ player.getPosition().getY() + "]");
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
 		String playerName = "Player " + (++playerCounter);
 		return this.connectPlayer(playerName);
 	}
 
 	public Player connectPlayer(String playerName) {
 		long nanotime = System.nanoTime();
 		String playerID = "playerID-" + nanotime;
 
 		Position position = null;
 		Random myRandom = new Random(nanotime);
 		position = new Position(myRandom.nextInt(columns),
 				myRandom.nextInt(rows));
 
 		DefaultPlayer player = new DefaultPlayer(playerName, playerID, this);
 		player.setPosition(position);
 
 		Log.d(tag,
 				"Connection " + playerName + "(" + playerID
 						+ ") at the position [" + position.getX() + ", "
 						+ position.getY() + "]");
 		players.put(player.getId(), player);
 		notifyConnectPlayerListeners(player);
 		return player;
 	}
 
 	@Override
 	public void disconnectPlayer(Player player) {
 		players.remove(player.getId());
 		notifyDisconnectPlayerListeners(player);
 	}
 
 	@Override
 	public Map<String, Player> getPlayers() {
 		return players;
 	}
 
 	@Override
 	public Player getPlayer(String playerID) {
 		return players.get(playerID);
 	}
 
 	// ==========Listeners==========
 	public void addSelectionListener(SelectionListener selectionListener) {
 		this.selectionListeners.add(selectionListener);
 	}
 
 	public void addExecutionListener(ExecutionListener executionListener) {
 		this.executionListeners.add(executionListener);
 	}
 
 	public void removeSelectionListener(SelectionListener selectionListener) {
 		this.selectionListeners.remove(selectionListener);
 	}
 
 	public void removeExecutionListener(ExecutionListener executionListener) {
 		this.executionListeners.remove(executionListener);
 	}
 
 	public void addPlayerListener(PlayerListener playerListener) {
 		this.playerListeners.add(playerListener);
 
 	}
 
 	public void removePlayerListener(PlayerListener playerListener) {
 		this.playerListeners.remove(playerListener);
 	}
 
 	// ==========Notify==========
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
 
 	private void notifyConnectPlayerListeners(Player player) {
 		ConnectEvent event = new ConnectEvent(player, player.getPosition());
 		for (PlayerListener listener : playerListeners) {
 			listener.onPlayerConnect(event);
 		}
 	}
 
 	private void notifyDisconnectPlayerListeners(Player player) {
 		ConnectEvent event = new ConnectEvent(player, player.getPosition());
 		for (PlayerListener listener : playerListeners) {
 			listener.onPlayerDisconnect(event);
 		}
 	}
 
 	private void notifyMovePlayerListeners(Player player, Position oldPos,
 			Position newPos) {
 		MoveEvent event = new MoveEvent(player, oldPos, newPos);
 		for (PlayerListener listener : playerListeners) {
 			listener.onPlayerMove(event);
 		}
 	}
 
 	@Override
 	public boolean isPlaying() {
 		return playing;
 	}
 
 	@Override
 	public void start() {
 		playing = true;
 		registerSoundTimer();
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onStart(event);
 		}
 	}
 
 	@Override
 	public void updateBPM(int bpm) {
 		if (bpm < 1) {
 			this.setBPM(1);// To avoid problems when resuming without change the
 							// BPM
 			pause();
 			return;
 		}
 		this.setBPM(bpm);
 		start();// Renew sequencer
 	}
 
 	private void registerSoundTimer() {
 		if (sequencer != null) {
 			sequencer.cancel();
 		}
 		// Think this timer is not that trustable. It may be the cause of the
 		// lags.
 		sequencer = new Timer();
 		sequencer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				long startTime = System.currentTimeMillis();
 				startPlayingGroup(currentPlayingLine);
 
 				long waitTime = (timeSequence / 2)
 						- (System.currentTimeMillis() - startTime); // keep it
 																	// turned on
 																	// until the
 																	// half of
 																	// the total
 																	// period
 																	// time
 
 				if (waitTime > 0) { // If we really need to wait more
 					try {
 						Thread.sleep(waitTime);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				stopPlayingGroup(currentPlayingLine);
 				currentPlayingLine = (currentPlayingLine + 1) % COLUMNS_CONF; // Circular
 																				// list
 			}
 		}, 0, timeSequence);
 	}
 
 	@Override
 	public void stop() {
 		if (sequencer != null) {
 			sequencer.cancel();
 			sequencer = null;
 		}
 
 		playing = false;
 		currentPlayingLine = 0;
 
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onStop(event);
 		}
 	}
 
 	@Override
 	public void pause() {
 		playing = false;
 		ExecutionEvent event = new ExecutionEvent();
 		if (sequencer != null) {
 			sequencer.cancel();
 			sequencer = null;
 		}
 		for (ExecutionListener listener : executionListeners) {
 			listener.onPause(event);
 		}
 	}
 
 	@Override
 	public void reset() {
 		// Stop first the sequence
 		stop();
 
 		// Deselect all the markedSquares and stop playing.
 		for (Position position : markedSquares) {
 			notifyDeselection(null, position);
 		}
 		markedSquares = new LinkedHashSet<Position>();
 		playing = false;
 
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onReset(event);
 		}
 	}
 
 	private void startPlayingGroup(int group) {
 		Log.d(tag, "Starting playing group " + group);
 		List<Position> playingPositions = new ArrayList<Position>();
 		for (Position position : markedSquares) {
 			if (position.getX() == group) {
 				playingPositions.add(position);
 			}
 		}
 		if (playingPositions.size() > 0) {
 			ExecutionEvent event = new ExecutionEvent();
 			event.setPositions(playingPositions);
 			for (ExecutionListener listener : executionListeners) {
 				listener.onStartPlayingGroup(event);
 			}
 		}
 	}
 
 	private void stopPlayingGroup(int group) {
 		Log.d(tag, "Stoping playing group " + currentPlayingLine);
 		List<Position> playingPositions = new ArrayList<Position>();
 		for (Position position : markedSquares) {
 			if (position.getX() == group) {
 				playingPositions.add(position);
 			}
 		}
 		if (playingPositions.size() > 0) {
 			ExecutionEvent event = new ExecutionEvent();
 			event.setPositions(playingPositions);
 			for (ExecutionListener listener : executionListeners) {
 				listener.onStopPlayingGroup(event);
 			}
 		}
 
 	}
 
 	public int getTimeSequence() {
 		return timeSequence;
 	}
 
 	public void setTimeSequence(int timeSequence) {
 		this.timeSequence = timeSequence;
 	}
 
 	public void setBPM(int bpm) {
 		setTimeSequence(60000 / bpm);
 	}
 
 }
