 package riskyspace.network;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.Map;
 
 import riskyspace.logic.SpriteMapData;
 import riskyspace.model.BuildAble;
 import riskyspace.model.Colony;
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 import riskyspace.model.PlayerStats;
 import riskyspace.model.Territory;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.services.EventHandler;
 import riskyspace.view.View;
 import riskyspace.view.opengl.impl.OpenGLView;
 
 /**
  * 
  * @author Alexander Hederstaf, Daniel Augurell 
  * 
  * GameClient Handles network tasks for a client.
  * It listens to EventBus.CLIENT for GUI event that
  * are to be sent to the server.
  */
 public class GameClient implements EventHandler {
 
 	private View mainView = null;
 	private ObjectInputStream input = null;
 	private ObjectOutputStream output = null;
 	private Socket socket = null;
 	
 	public static void main(String[] args) {
 		new GameClient("129.16.197.74", 6013);
 	}
 	
 	public GameClient(String hostIP, int hostPort) {
 		System.out.println("new GC: " + hostIP + "  " + hostPort);
 		EventBus.CLIENT.addHandler(this);
 		connect(hostIP, hostPort);
 		Thread renderThread = new Thread(new Runnable() {
 			@Override public void run() {
 				while(true) {
 					mainView.draw();
 					try {
 						Thread.sleep(1000/60);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		});
 		renderThread.start();
 	}
 
 	private void connect(String hostIP, int hostPort) {
 		long startTime = System.currentTimeMillis();
		while (socket == null && System.currentTimeMillis() - startTime < 5000) {
 			/*
 			 * Loop until Connected
 			 */
 			connectToHost(hostIP, hostPort);
 		}
 		System.out.println("Connected");
 		initiateGameView();
 		new ServerListener(mainView, input);
 	}
 
 	private void initiateGameView() {
 		System.out.println("start init");
 
 		SpriteMapData data = null;
 		Integer rows = null;
 		Integer cols = null;
 		Player player = null;
 		PlayerStats stats = null;
 		
 		while (data == null || rows == null || cols == null || player == null || stats == null) {
 			Object o = null;
 			try {
 				o = input.readObject();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			if (o instanceof Event) {
 				Event evt = (Event) o;
 				if (evt.getTag() == Event.EventTag.INIT_ROWS) {
 					rows = (Integer) evt.getObjectValue();
 				}
 				if (evt.getTag() == Event.EventTag.INIT_COLS) {
 					cols = (Integer) evt.getObjectValue();
 				}
 				if (evt.getTag() == Event.EventTag.INIT_PLAYER) {
 					player = (Player) evt.getObjectValue();
 				}
 				if (evt.getTag() == Event.EventTag.UPDATE_SPRITEDATA) {
 					data = (SpriteMapData) evt.getObjectValue();
 				}
 				if (evt.getTag() == Event.EventTag.STATS_CHANGED) {
 					stats = (PlayerStats) evt.getObjectValue();
 				}
 			}
 		}
 //		mainView = ViewFactory.getView(ViewFactory.SWING_IMPL, rows, cols);
 		mainView = new OpenGLView(rows, cols);
 		mainView.updateData(data);
 		mainView.setPlayerStats(stats);
 		mainView.setViewer(player);
 		mainView.setVisible(true);
 	}
 
 	private void connectToHost(String hostIP, int hostPort) {
 		try {
 			socket = new Socket(hostIP, hostPort);
 			input = new ObjectInputStream(socket.getInputStream());
 			output = new ObjectOutputStream(socket.getOutputStream());
 		} catch (UnknownHostException e) {
 			System.err.println("Dont know about host: " + hostIP);
 			System.exit(1);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void performEvent(Event evt) {
 		try {
 			output.writeObject(evt);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * 
 	 * @author Alexander Hederstaf
 	 *
 	 * Listens to events from the server and updates the 
 	 * View accordingly.
 	 */
 	private class ServerListener implements Runnable {
 		
 		private View mainView = null;
 		private ObjectInputStream input = null;
 		
 
 		public ServerListener(View view, ObjectInputStream input) {
 			this.mainView = view;
 			this.input = input;
 			Thread t = new Thread(this);
 			t.start();
 		}
 		
 		@Override
 		public void run() {
 			while(true) {
 				try {
 					Object o = input.readObject();
 					Event event = null;
 					if (o instanceof Event) {
 						event = (Event) o;
 					}
 					if (event != null) {
 						System.out.println(event);
 						if (event.getTag() == Event.EventTag.UPDATE_SPRITEDATA) {
 							mainView.updateData((SpriteMapData) event.getObjectValue());
 						} else if (event.getTag() == Event.EventTag.STATS_CHANGED) {
 							mainView.setPlayerStats((PlayerStats) event.getObjectValue());
 						} else if (event.getTag() == Event.EventTag.BUILDQUEUE_CHANGED) {
 							mainView.setQueue((Map<Colony, List<BuildAble>>) event.getObjectValue());
 						} else if (event.getTag() == Event.EventTag.ACTIVE_PLAYER_CHANGED) {
 							mainView.setActivePlayer((Player) event.getObjectValue());
 						} else if (event.getTag() == Event.EventTag.HOME_LOST) {
 							mainView.showGameOver((Player) event.getObjectValue());
 						} else if (event.getTag() == Event.EventTag.SELECTION) {
 							Object selection = event.getObjectValue();
 							if (selection instanceof Colony) {
 								mainView.showColony((Colony) selection);
 							} else if (selection instanceof Territory) {
 								mainView.showPlanet((Territory) selection);
 							} else if (selection instanceof Fleet) {
 								mainView.showFleet((Fleet) selection);
 							} else if(selection == null){
 								mainView.hideMenus();
 							}
 						} 
 					}
 				} catch (EOFException e){
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
