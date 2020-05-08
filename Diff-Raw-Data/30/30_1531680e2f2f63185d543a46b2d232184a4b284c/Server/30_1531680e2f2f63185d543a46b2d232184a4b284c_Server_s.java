 
 package server;
 
 import game.Container;
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
 
 
 import serialization.ParseException;
 import ui.isometric.builder.BuilderInterface;
 import ui.isometric.mock.ClientMessageHandlerMock;
 import util.Direction;
 import util.Position;
 import util.Resources;
 
 
 
 
 public class Server{
 
 	public static final int DEFAULT_PORT = 32765;
 	private static final String EXTENTION = "wblrd";
 	private boolean CLI = false;
 	private String filename;
 	private boolean fromSave;
 	
 	private ArrayList<ServerThread> connections = new ArrayList<ServerThread>(10);
 	
 	public Server(String[] args){
 		if(args.length == 2) {
 		    try {
 		    	if(args[0].toLowerCase().equals("cli"))
 		    		CLI = true;
 		    	else
 		    		CLI = false;
 		    	filename = args[1];
 		    	fromSave = true;
 		    } catch (Exception e) {
 		        System.err.println("Arguments must be {CLI/Server,Filename} with no ','");
 		        System.exit(1);
 		    }
 		}
 		else
 			CLI = false;
 			fromSave = false;
 
 	}
 	
 	public void run() throws IOException {
 		//Main Class for server
 		GameWorld model = null;
 		int port = DEFAULT_PORT;
 		if(!CLI){	
 			String choice;
 			Object[] possibilities = {"NewGame", "LoadGame"};
 			choice = (String)JOptionPane.showInputDialog(
 			                    null,
 			                    "Start new, or load from file?",
 			                    "New or Load Game",
 			                    JOptionPane.PLAIN_MESSAGE,
 			                    null,
 			                    possibilities,
 			                    "NewGame");
 			if(choice == null)
 				choice = "NewGame";
 		
 			if(choice.equals("LoadGame")){
 				fromSave = true;		
 			}
 			
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
 		}
 		else{
 			model = loadCLI(filename);
 			if(model == null){
 				System.out.println("No file given, giving default gameworld");
 				model = defaultworld();
 			}
 		}
 		BuilderInterface view = new BuilderInterface("Server", model, new ClientMessageHandlerMock());
 		view.show();
 		runServer(port,model);	
 		System.exit(0);
 	}
 	
 	public GameWorld loadCLI(String name){
 		File load = new File(name);
 		JFrame frame = new JFrame();
 		String loaded = null;
 		try {
 			loaded = Resources.loadTextFile(load.getAbsolutePath());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if(loaded == null) {
 			JOptionPane.showMessageDialog(frame, "Error loading file");
 			return null;
 		}
 		GameWorld world = new GameWorld(); 
 		try {
 			world.fromTree(Database.xmlToTree(loaded));
 		} catch (ParseException e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(frame, "Error loading file");
 		}
 		return world;
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
 		try {
 			world.fromTree(Database.xmlToTree(loaded));
 		} catch (ParseException e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(frame, "Error loading file");
 		}
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
 				String name = ll.position().equals(new Position(0, 0)) && false? "wall_brown_1_t" : y > 0 && y < x? "wall_brown_1_straight" : "wall_brown_1_corner";
 				ll.rotate(ll.position().equals(new Position(0, 0))? Direction.WEST : Direction.EAST).put(new game.things.Wall(sgm, name));
 				ll = ll.next(ll.direction());
 			}
 			ll = ll.rotate(Direction.EAST);
 		}
 		
 		//sgm.level(0).location(new Position(0, 1), Direction.EAST).put(new game.things.Door(sgm, "wall_brown_1_door_closed", "wall_brown_1_door_open", false));
 		sgm.level(0).location(new Position(-15, -15), Direction.EAST).put(new game.things.TestPickUp(sgm));
 		sgm.level(0).location(new Position(-15,-14), Direction.NORTH).put(new game.things.EquipmentGameThing(sgm, 3, 3, 3, 3, Slot.WEAPON, "Short sword", "sword_1"));
 		sgm.level(0).location(new Position(-15,-14), Direction.NORTH).put(new game.things.GroundTile(sgm));
 		sgm.level(0).location(new Position(0, 1), Direction.EAST).put(new game.things.Door(sgm, "wall_grey_1_door_open", "wall_grey_1_door_closed", false, "hello"));
 		sgm.level(0).location(new Position(1, 1), Direction.EAST).put(new game.things.SpawnPoint(sgm));
 		sgm.level(0).location(new Position(1, 1), Direction.EAST).put(new game.things.Key(sgm,"ruby","hello"));
 		game.Level.Location lp = sgm.level(0).location(new Position(14, 15), Direction.NORTH);
 		lp.put(new game.things.OpenableFurniture(sgm,"cupboard_1"));
 		ll = sgm.level(0).location(new Position(15, 15), Direction.NORTH);
 		ll.put(new game.things.GroundTile(sgm));
 		List<String> spoken = new ArrayList<String>();
 		spoken.add(0,"Hmm, this isnt a very nice place is it");
 		spoken.add(1,"You need to get out of here? Try the towers");
 		spoken.add(2,"Those doors will need keys");
 		spoken.add(3,"I hope you JavaDoc'ed all your code, oh I've said too much!");
 		spoken.add(4,"Do you like Cats as much as I do?");
 		spoken.add(5,"I hope you JavaDoc'ed all your code, oh I've said too much!");
 		spoken.add(6,"Hi, my name is Dave Pearce, nice to meet you");
 		spoken.add(7,"Stop clicking on me you creep");
 		ll.put(new game.things.ChattyNPC(sgm, "bob", "Chatty Dave", spoken));
 		ll.next(Direction.NORTH).put(new game.things.GroundTile(sgm, "dbg_north"));
 		ll.next(Direction.EAST).put(new game.things.GroundTile(sgm, "dbg_east"));
 		ll.next(Direction.SOUTH).put(new game.things.GroundTile(sgm, "dbg_south"));
 		ll.next(Direction.WEST).put(new game.things.GroundTile(sgm, "dbg_west"));
 		Container drop = new Container(sgm);
 		drop.put(new game.things.EquipmentGameThing(sgm, 10, 10, 10, 10, Slot.WEAPON, "Short sword", "sword_1"));
 		sgm.level(0).location(new Position(15, -15), Direction.NORTH).put(new game.things.GroundTile(sgm, "dbg_compass"));
 		sgm.level(0).location(new Position(15, -15), Direction.NORTH).put(new game.things.Enemy(sgm, "bob", "Sir Robert", sgm.level(0).location(new Position(0, 0), Direction.NORTH), 5, drop));
 		sgm.level(0).location(new Position(16, -15), Direction.NORTH).put(new game.things.Enemy(sgm, "bob", "Sir Robert", sgm.level(0).location(new Position(0, 0), Direction.NORTH), 5, null));
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
