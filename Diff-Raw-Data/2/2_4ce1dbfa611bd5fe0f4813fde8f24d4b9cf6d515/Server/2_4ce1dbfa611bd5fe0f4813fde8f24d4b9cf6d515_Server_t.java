 
 package server;
 
 import game.GameWorld;
 import game.GameWorld.DeltaWatcher;
 import game.things.EquipmentGameThing.Slot;
 import game.WorldDelta;
 
 import java.util.*;
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import data.Database;
 
 
 import ui.isometric.builder.IsoInterfaceWorldBuilder;
 import ui.isometric.mock.ClientMessageHandlerMock;
 import util.Direction;
 import util.Position;
 import util.Resources;
 
 
 
 
 public class Server{
 
 	public static final int DEFAULT_PORT = 32765;
 	private static final String EXTENTION = "wblrd";
 	
 	private ArrayList<ServerThread> connections = new ArrayList<ServerThread>(10);
 	
 	public void run() throws IOException {
 		//Main Class for server	
 		boolean fromSave = false;		
 		
 		int port = DEFAULT_PORT;
 		String choice = "null";
 		while(!(choice.equals("NewGame") || choice.equals("LoadGame"))){
 			Object[] possibilities = {"NewGame", "LoadGame"};
 			choice = (String)JOptionPane.showInputDialog(
 			                    null,
 			                    "Start new, or load from file?",
 			                    "Customized Dialog",
 			                    JOptionPane.PLAIN_MESSAGE,
 			                    null,
 			                    possibilities,
 			                    "NewGame");
 		}
 		if(choice.equals("LoadGame")){
 			fromSave = true;
 			
 		}
 		GameWorld model = null;
 		if(fromSave){
 			model = load();
 			if(model == null){
 				System.out.println("No file given, giving default gameworld");
 				model = defaultworld();
 			}
 		}
 		else{
 			model = defaultworld();
 		}
 
 		IsoInterfaceWorldBuilder view = new IsoInterfaceWorldBuilder("Server", model, new ClientMessageHandlerMock());
 		view.show();
 		runServer(port,model);	
 		System.exit(0);
 	}
 	
 	public GameWorld load() {
 		String loaded = null;
 		JFrame frame = new JFrame();
 		JFileChooser chooser = new JFileChooser();
 		chooser.setFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(File arg0) {
 				return arg0.isFile() && arg0.getAbsolutePath().endsWith(EXTENTION);
 			}
 
 			@Override
 			public String getDescription() {
 				return "World Builder File";
 			}
 		});
 		if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			File load = chooser.getSelectedFile().getAbsoluteFile();
 			try {
 				loaded = Resources.loadTextFile(load.getAbsolutePath());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			return null;
 		}
 		
 		if(loaded == null) {
 			JOptionPane.showMessageDialog(frame, "Error loading file");
 			return null;
 		}
 		GameWorld world = new GameWorld(); 
 		world.fromTree(Database.xmlToTree(loaded));
 		return world;
 	}
 	
 	public void save() {
 		GameWorld world = new GameWorld();
 		JFrame frame = new JFrame();
 		String file = Database.treeToXML(world.toTree());
 		
 		JFileChooser chooser = new JFileChooser();
 		if(chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			File save = chooser.getSelectedFile().getAbsoluteFile();
 			if(!save.getAbsolutePath().endsWith("."+EXTENTION)) {
 				save = new File(save.getAbsolutePath() + "." + EXTENTION);
 			}
 			if(save.exists()) {
 				if(JOptionPane.showConfirmDialog(frame, "This file exists, are you sure you wish to overwrite it?", null, JOptionPane.OK_CANCEL_OPTION, 0) == JOptionPane.CANCEL_OPTION) {
 					return;
 				}
 			}
 			try {
 				BufferedWriter writer = new BufferedWriter(new FileWriter(save));
 				writer.write(file);
 				writer.flush();
 				writer.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	
 
 	private void runServer(int port, GameWorld game) {		
 		int uid = 0;
 		// Listen for connections
 		System.out.println("GAME SERVER LISTENING ON PORT " + port);
 		try {
 			game.addDeltaWatcher(new DeltaWatcher(){
 				@Override
 				public void delta(final WorldDelta d) {
 					toAllPlayers(new ClientMessenger() {
 						@Override
 						public void doTo(ServerThread client) {
 							client.addDelta(d);
 						}
 					});
 				}	
 			});
 			// Now, we await connections.
 			ServerSocket ss = new ServerSocket(port);
 			while (true) {
 				// 	Wait for a socket
 				Socket s = ss.accept();
 				System.out.println("ACCEPTED CONNECTION FROM: " + s.getInetAddress());				
 				final ServerThread newSer = new ServerThread(s,uid,game, this);
 				// MaxZ's code: send initial state
 				game.allDeltas(new DeltaWatcher(){
 					public void delta(WorldDelta d){
 						newSer.addDelta(d);
 					}
 				});
 				connections.add(newSer);
 				newSer.start();
 				uid++;; //this will add players unique identifier in future.
 			}
 		} catch(IOException e) {
 			System.err.println("I/O error: " + e.getMessage());
 		} 
 	}
 
 
 
 	/**
 	 * Check whether or not there is at least one connection alive.
 	 * 
 	 * @param connections
 	 * @return
 	 */
 	private boolean atleastOneConnection(ArrayList<ServerThread> connections) {
 		for (ServerThread m : connections) {
 			if (m.isAlive()) {
 				return true;
 			}			
 		}
 		return false;
 	}
 	
 	private void runGame(ArrayList<ServerThread> connections, GameWorld game){ // We dont actually need this if we want the server to run forever, if you just let main return the other threads will still run
 		while(atleastOneConnection(connections)){
 			Thread.yield();	
 			pause(10);
 		}
 	}
 	
 	private static void pause(int delay) {
 		try {
 			Thread.sleep(delay);
 		} 
 		catch(InterruptedException e){			
 		}
 	}
 
 	public static GameWorld defaultworld(){
 		game.GameWorld sgm = new GameWorld();
 		// make a spiral instead
 		int width = 20;
 		game.Level.Location ll = sgm.level(0).location(new Position(0, 0), Direction.NORTH);
 		for(int x = -width/2; x < width/2; x++)
 			for(int y = -width/2; y < width/2; y++)
 				sgm.level(0).location(new Position(x, y + 1), Direction.NORTH).put(new game.things.GroundTile(sgm, "ground_grey_1"));
 		for(int x = 0; x < width; x++){
 			for(int y = 0; y < x; y++){
 				String name = ll.position().equals(new Position(0, 2))? "wall_brown_1_t" : y > 0 && y < x? "wall_brown_1_straight" : "wall_brown_1_corner";
 				ll.rotate(ll.position().equals(new Position(0, 2))? Direction.WEST : Direction.EAST).put(new game.things.Wall(sgm, name));
 				ll = ll.next(ll.direction());
 			}
 			ll = ll.rotate(Direction.EAST);
 		}
 		
 		//sgm.level(0).location(new Position(0, 1), Direction.EAST).put(new game.things.Door(sgm, "wall_brown_1_door_closed", "wall_brown_1_door_open", false));
 		sgm.level(0).location(new Position(-15, -15), Direction.EAST).put(new game.things.TestPickUp(sgm));
		sgm.level(0).location(new Position(-15,-14), Direction.NORTH).put(new game.things.EquipmentGameThing(sgm, 1, 1, 1, 1, Slot.WEAPON, "Short sword", "sword_1"));
 		sgm.level(0).location(new Position(-15,-14), Direction.NORTH).put(new game.things.GroundTile(sgm));
 		sgm.level(0).location(new Position(0, 1), Direction.EAST).put(new game.things.SpawnPoint(sgm));
 		ll = sgm.level(0).location(new Position(15, 15), Direction.NORTH);
 		ll.put(new game.things.GroundTile(sgm));
 		ll.put(new game.things.Chest(sgm));
 		ll.next(Direction.NORTH).put(new game.things.GroundTile(sgm, "dbg_north"));
 		ll.next(Direction.EAST).put(new game.things.GroundTile(sgm, "dbg_east"));
 		ll.next(Direction.SOUTH).put(new game.things.GroundTile(sgm, "dbg_south"));
 		ll.next(Direction.WEST).put(new game.things.GroundTile(sgm, "dbg_west"));
 		sgm.level(0).location(new Position(15, -15), Direction.NORTH).put(new game.things.GroundTile(sgm, "dbg_compass"));
 		sgm.level(0).location(new Position(15, -15), Direction.NORTH).put(new game.things.Enemy(sgm, "bob", "Sir Robert", sgm.level(0).location(new Position(0, 0), Direction.NORTH), 5));
 
 		return sgm;
 	}
 	
 	
 
 
 	public static interface ClientMessenger {
 		public void doTo(ServerThread client);
 	}
 	
 	public void toAllPlayers(ClientMessenger clientMessenger) {
 		for(ServerThread t : connections) {
 			clientMessenger.doTo(t);
 		}
 	}
 		
 }
