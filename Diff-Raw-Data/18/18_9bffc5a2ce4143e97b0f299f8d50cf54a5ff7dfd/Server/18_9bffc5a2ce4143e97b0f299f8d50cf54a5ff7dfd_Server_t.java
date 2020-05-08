 package com.hexcore.cas;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.ReentrantLock;
 
 import com.hexcore.cas.control.discovery.Lobby;
 import com.hexcore.cas.control.server.Simulator;
 import com.hexcore.cas.model.Grid;
 import com.hexcore.cas.model.World;
 import com.hexcore.cas.model.WorldReader;
 import com.hexcore.cas.model.WorldSaver;
 import com.hexcore.cas.rulesystems.CALCompiler;
 import com.hexcore.cas.ui.GUI;
 import com.hexcore.cas.utilities.Configuration;
 import com.hexcore.cas.utilities.Log;
 
 public class Server
 {
 	private final static String TAG = "Server";
 	public final static String VERSION = "v0.1";
 		
 	private AtomicBoolean 	running = new AtomicBoolean(false);
 	private AtomicBoolean	activeSimulation = new AtomicBoolean(false);
 	
 	private Configuration 	config = null;
 	private Lobby 			lobby = null;
 	private GUI 			ui = null;
 	
 	private ClientThread	client = null;
 	private Simulator 		simulate = null;
 	private World			world = null;
 	
 	private List<InetSocketAddress>	clients;	
 	
 	private ReentrantLock	serverLock = new ReentrantLock();
 	
 	private LinkedBlockingQueue<ServerEvent>	eventQueue;
 	
 	private static Server instance = null;
 
 	public static void main(String[] args)
 	{
 		 instance = new Server();
 		 instance.start();
 	}
 	
 	public void start()
 	{
 		System.out.println("== Hexcore CAS Server - " + VERSION + " ==");
 		
 		Log.information(TAG, "Loading configuration...");
 		config = new Configuration("data/config.txt");
 		
 		eventQueue = new LinkedBlockingQueue<ServerEvent>();
 		
 		Log.information(TAG, "Starting lobby...");
 		int beaconPort = config.getInteger("Network.Beacon", "port", 3118);
 		int beaconReplyPort = config.getInteger("Network.Beacon", "replyPort", 3117);
 		lobby = new Lobby(beaconPort, beaconReplyPort);
 		lobby.start();
 		
 		Log.information(TAG, "Starting user interface...");
 		ui = new GUI(instance);
 		lobby.addListener(ui);
 
 		clients = new ArrayList<InetSocketAddress>();
 				
 		running.set(true);
 		while (running.get())
 		{
 			try
 			{
 				ServerEvent event = eventQueue.poll(3, TimeUnit.SECONDS);
 				if (event == null) continue;
 				
 				switch (event.type) 
 				{				
 					case CREATE_WORLD:
 					{				
 						serverLock.lock();
 						
 						world = new World();
 						
 						Grid grid = event.gridType.create(event.size, 2);
 						grid.setWrappable(event.wrappable);
 	
 						for(int y = 0; y < event.size.y; y++)
 							for(int x = 0; x < event.size.x; x++)
 								grid.getCell(x, y).setValue(0, 0.0);
 												
 						world.addGeneration(grid);						
 						world.setRuleCode("ruleset GameOfLife\n{\n\ttypecount 1;\n\tproperty alive;\n\n\ttype Land\n\t{\n\t\tvar c = sum(neighbours.alive);\n\t\tif ((c < 2) || (c > 3))\n\t\t\tself.alive = 0;\n\t\telse if (c == 3)\n\t\t\tself.alive = 1;\t\t\n\t}\n}");
 						world.setColourCode("colourset colours\n{\n\tproperty alive\n\t{\n\t\t0 - 5 : rgb(0.5, 0.3, 0.2);\n\t\t5 - 10 : rgb(0.3, 0.7, 0.2);\n\t}\n\t}\n}");
 						
 						ui.startWorldEditor(world);
 
 						serverLock.unlock();
 						break;
 					}
 					
 					case LOAD_WORLD:
 					{
 						serverLock.lock();
 						
 						world = new World();
 						//WorldReader reader = new WorldReader(world);
 						
 						//reader.readWorld(event.filename);
 						world.setFileName(event.filename);
 						world.load();
 						
 						ui.startWorldEditor(world);
 						
 						serverLock.unlock();
 						break;
 					}
 					
 					case SAVE_WORLD:
 					{
 						serverLock.lock();
 						
 						//WorldSaver saver = new WorldSaver();
 						
 						try
 						{
 							//saver.saveWorld(world);
 							world.save();
 							Log.information(TAG, "Saved world");
 						}
 						catch (IOException e)
 						{
 							e.printStackTrace();
 						}
 						
 						serverLock.unlock();
 						break;
 					}
 					
 					case READY_SIMULATION:
 					{
 						serverLock.lock();
 						
 						clients = event.clients;
 						if (!activeSimulation.getAndSet(true)) initSimulation();
 						
 						serverLock.unlock();
 						break;
 					}
 					
 					case START_SIMULATION:
 					{
 						serverLock.lock();
 						
 						if (!activeSimulation.getAndSet(true)) initSimulation();
 						
 						simulate.play();
 						
 						serverLock.unlock();
 						break;
 					}		
 					
 					case STEP_SIMULATION:
 					{
 						serverLock.lock();
 						
 						if (!activeSimulation.getAndSet(true)) initSimulation();
 						
 						simulate.step();
 						
 						serverLock.unlock();
 						break;
 					}
 					
 					case PAUSE_SIMULATION:
 					{
 						serverLock.lock();
 						
 						if (activeSimulation.get()) simulate.pause();
 						
 						serverLock.unlock();
 						break;
 					}
 					
 					case STOP_SIMULATION:
 					{
 						serverLock.lock();
 						
 						if (activeSimulation.getAndSet(false))
 						{
 							simulate.disconnect();
 							simulate = null;
 							
 							client.client.stop();
 							client = null;
 						}
 						
 						serverLock.unlock();
 						break;
 					}
 					
 					case PING_CLIENTS:
 					{
 						lobby.ping();
 						break;
 					}
 					
 					case CLEAR_HISTORY:
 					{
 						serverLock.lock();
 						
 						boolean result = world.clearHistory(event.genNumber);
 						
 						serverLock.unlock();
 						break;
 					}
 					
 					case SHUTDOWN:
 					{
 						Log.information(TAG, "Got shutdown message");
 						running.set(false);
 						break;
 					}
 				}
 			}								
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}		
 		}
 		
 		Log.information(TAG, "Shutting down...");
 		lobby.disconnect();
 		if (simulate != null) simulate.disconnect();
 		
 		try
 		{
 			Thread.sleep(300);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		
 		System.exit(0);
 	}
 	
 	private void initSimulation() throws InterruptedException
 	{
 		if (clients.isEmpty())
 		{
 			Log.information(TAG, "No network clients found, starting local client");
 			
 			client = new ClientThread();
 			client.start();
 			
 			Thread.sleep(100); // Wait for client to start
 			
 			clients.add(new InetSocketAddress("localhost", client.client.getPort()));
 		}
 		
 		Log.information(TAG, "Compiling rule code...");
 		
 		CALCompiler compiler = new CALCompiler();
 		compiler.compile(world.getRuleCode());
 		
 		if (compiler.getErrorCount() > 0)
 			Log.error(TAG, "Invalid rule code");
 		
 		Log.information(TAG, "Starting overseer...");
 		
 		simulate = new Simulator(world, config.getInteger("Network.Client", "port", 3119));
 		simulate.setClients(clients);
 		simulate.setRuleBytecode(compiler.getCode());
 		simulate.start();
 		simulate.pause();
 		Thread.sleep(100);
 		
 		simulate.simulate(-1);
 	}
 	
 	public void sendEvent(ServerEvent event)
 	{
 		try 
 		{
 			eventQueue.put(event);
 		} 
 		catch (InterruptedException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	class ClientThread extends Thread
 	{
 		public Client client;
 		
 		@Override
 		public void run()
 		{
 			client = new Client();
 			client.start(false);
 		}
 	}
 }
