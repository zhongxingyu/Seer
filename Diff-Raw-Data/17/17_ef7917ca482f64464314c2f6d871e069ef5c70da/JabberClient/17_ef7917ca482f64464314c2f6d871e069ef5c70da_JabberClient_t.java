 package cs3013;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CountDownLatch;
 
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import cs3013.plugins.*;
 
 public class JabberClient {
 	public JabberClient(JabberID jid) {
 		this.jid = jid;
 	}
 	
 	public void start() {
 		installPlugin(new CorePlugin());
 		installPlugin(new RosterPlugin());
 		installPlugin(new ChatPlugin());
 		installPlugin(new RawStanza());
 		installPlugin(new Vcard());
 
 		running = true;
 		firstConnection = true;
 		startConnectLoop();
 		startCommandLoop();
 
 		try {
 			while(running) {
 				Message msg = mainMsgQueue.take();
 				switch(msg.getType()) {
 				case CONNECTED:
 					firstConnection = false;
 					onConnected((XmppConnection)msg.getArg());
 					break;
 				case DISCONNECTED:
 					onDisconnected();
 					break;
 				case USER_INPUT:
 					onUserInput((String)msg.getArg());
 					break;
 				case KEEP_ALIVE:
 					send(" ");
 					break;
 				case STANZA:
 					onStanza((XMLStreamReader)msg.getArg());
 					break;
 				case ERROR:
 					onError((Exception)msg.getArg());
 					break;
 				}
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				//print("Stopping connect loop");
 				stopConnectLoop();
 				//print("Stopping keep-alive timer");
 				stopKeepAliveTimer();
 				//print("Stopping socket handler");
 				stopSocketHandler();
 				//print("Stopping command loop");
 				stopCommandLoop();
 				//print("Done");
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			if(connection != null) {
 				try {
 					connection.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 	
 			for(Plugin plugin: plugins) {
 				plugin.terminate();
 			}
 		}
 	}
 
 	public JabberID getJID() {
 		return jid;
 	}
 
 	public String newStanzaId() {
 		return Long.toString(random.nextLong(), 16);
 	}
 	
 	public void send(String msg) throws InterruptedException {
 		if(connection == null) return;
 
 		BufferedWriter writer = connection.getWriter();
 		try {
 			writer.write(msg);
 			writer.flush();
 		} catch (IOException e) {
 			onDisconnected();
 		}
 	}
 
 	public void print(String msg) {
 		System.out.println(msg);
 	}
 	
 	public void registerCommand(String name, Command command) {
 		commands.put(name, command);
 	}
 
 	public void registerStanzaHandler(StanzaHandler handler) {
 		stanzaHandlers.add(handler);
 	}
 	
 	public void setChatHandler(ChatHandler handler) {
 		chatHandler = handler;
 	}
 	
 	private void startConnectLoop() {
 		connectLoop = new Thread() {
 			@Override
 			public void run() {
 				final int numSlots = 20;
 				int numAttempts = firstConnection ? 0 : 1;
 		
 				try {
 					while(connectLoop != null) {
 						Random random = new Random();
 						int upperBound = Math.min((int)Math.pow(2, numAttempts), numSlots) - 1;
 						long waitTime = random.nextInt(upperBound + 1) * 60 * 1000 / numSlots;
 		
 						if(numAttempts > 0 || !firstConnection) {
 							print("Waiting " + waitTime / 1000 + "s");
 						}
 		
 						Thread.sleep(waitTime);
 						try {
 							print("---------------------------------------------------");
 							XmppConnection connection = new XmppConnection(jid);
 							connection.connect();
 							print("-------------------- Connected --------------------");
 							postMessage(MessageType.CONNECTED, connection);
 							break;
 						} catch (IOException e) {
 							print("---------------- Failed to connect ----------------");
 						} catch (XmppException e) {
 							postMessage(MessageType.ERROR, e);
 							break;
 						}
 						++numAttempts;
 					}
 				}
 				catch(InterruptedException _) {
 				}
 			}
 		};
 		connectLoop.start();
 	}
 	
 	private void stopConnectLoop() throws InterruptedException {
 		if(connectLoop != null) {
 			Thread thread = connectLoop;
 			connectLoop = null;
 			syncStop(thread);
 		}
 	}
 
 	private void startCommandLoop() {
 		commandLoop = new Thread() {
 			@Override
 			public void run() {
 				try(InputStreamReader isr = new InputStreamReader(System.in);
 				    BufferedReader br = new BufferedReader(isr)) {
 					while(commandLoop != null) {
 						while(!br.ready()) {
 							Thread.sleep(1);;
 						}
 						postMessage(MessageType.USER_INPUT, br.readLine());
 					}
 				} catch (InterruptedException _) {
 				} catch (IOException e) {
 					try {
 						postMessage(MessageType.ERROR, e);
 					} catch (InterruptedException _) {
 					}
 				}
 			}
 		};
 		commandLoop.start();
 	}
 	
 	private void stopCommandLoop() throws InterruptedException {
 		if(commandLoop != null) {
 			Thread thread = commandLoop;
 			commandLoop = null;
 			syncStop(thread);
 		}
 	}
 
 	private void startKeepAliveTimer() {
 		keepAliveTimer = new Timer();
 		keepAliveTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				try {
 					postMessage(MessageType.KEEP_ALIVE);
 				} catch (InterruptedException e) {
 				}
 			}
 		}, 5000, 5000);
 	}
 	
 	private void stopKeepAliveTimer() {
 		if(keepAliveTimer != null) {
 			keepAliveTimer.cancel();
 			keepAliveTimer = null;
 		}
 	}
 
 	private void startSocketHandler() {
 		socketHandler = new Thread() {
 			@Override
 			public void run() {
 				XMLStreamReader parser = connection.getParser();
 				try {
 					while(socketHandler != null) {
 						if(parser.next() != XMLStreamConstants.START_ELEMENT) continue;
 
 						socketHandlerLatch = new CountDownLatch(1);
 						postMessage(MessageType.STANZA, parser);
 						socketHandlerLatch.await();
 					}
 				} catch (XMLStreamException e) {
 					//confirm if it is due to socket
 					boolean disconnected = false;
 					try {
 						disconnected = connection.getSocket().getInputStream().read() == -1;
 					} catch (IOException _) {
 						disconnected = true;
 					}
 					
 					if(disconnected) {
 						try {
 							postMessage(MessageType.DISCONNECTED);
 						} catch (InterruptedException e1) {
 						}
 					}
 					else {
 						try {
 							postMessage(MessageType.ERROR, e);
 						} catch (InterruptedException e1) {
 						}
 					}
 				} catch (InterruptedException _) {
 				}
 			}
 		};
 		socketHandler.start();
 	}
 
 	private void stopSocketHandler() throws InterruptedException {
 		if(socketHandler != null) {
 			Thread thread = socketHandler;
 			socketHandler = null;
 			syncStop(thread);
 		}
 	}
 
 	private void onConnected(XmppConnection connection) throws InterruptedException {
 		this.connection = connection;
 
 		//send presence
 		send("<presence/>");
 		//this is required for Facebook chat
 		String sessionStanza = String.format(
 			  "<iq type='set' id='%s' to='%s'>"
 			+     "<session xmlns='urn:ietf:params:xml:ns:xmpp-session'/>"
 			+ "</iq>",
 
 			newStanzaId(), jid.getDomain()
 		);
 		send(sessionStanza);
 
 		startSocketHandler();
 		startKeepAliveTimer();
 	}
 	
 	private void onDisconnected() throws InterruptedException {
 		if(connection != null) {
 			print("------------------ Connection lost ----------------");
 			connection = null;
 
 			stopKeepAliveTimer();
 			stopSocketHandler();
 			startConnectLoop();
 		}
 	}
 
 	private void onError(Exception e) {
 		e.printStackTrace();
 		running = false;
 	}
 
 	private void onStanza(XMLStreamReader parser) throws XMLStreamException {
 		boolean handled = false;
 
 		for(StanzaHandler handler: stanzaHandlers) {
 			if(handler.onStanza(parser)) {
 				handled = true;
 				break;
 			}
 		}
 		
 		if(!handled) {
 			if(developerMode) {
				XMLUtils.dumpXML(parser);
 			}
 			else {
 				XMLUtils.skipElement(parser);
 			}
 		}
 		
 		socketHandlerLatch.countDown();
 	}
 
 	
 	private void onUserInput(String input) throws InterruptedException {
 		if(input.startsWith("@")) {
 			String[] cmd = input.split("\\s+");
 			String cmdName = cmd[0].substring(1);
 			Command command = commands.get(cmdName);
 			if(command != null) {
 				command.execute(cmd);
 			}
 			else {
 				print("Invalid command");
 			}
 		}
 		else {
 			chatHandler.sendMessage(input);
 		}
 	}
 
 	private void postMessage(MessageType type) throws InterruptedException {
 		mainMsgQueue.put(new Message(type));
 	}
 
 	private void postMessage(MessageType type, Object arg) throws InterruptedException {
 		mainMsgQueue.put(new Message(type, arg));
 	}
 
 	private void installPlugin(Plugin plugin) {
 		plugin.init(this);
 		plugins.add(plugin);
 	}
 	
 	private void syncStop(Thread thread) throws InterruptedException {
 		thread.interrupt();
 		thread.join();
 	}
 
 	private CountDownLatch socketHandlerLatch;
 	private Thread socketHandler;
 	private Thread connectLoop;
 	private Thread commandLoop;
 	private Timer keepAliveTimer;
 	private boolean firstConnection;
 	private boolean running;
 	private BlockingQueue<Message> mainMsgQueue = new ArrayBlockingQueue<Message>(128);
 	private boolean developerMode = false;
 	private List<Plugin> plugins = new ArrayList<Plugin>();
 	private Map<String, Command> commands = new HashMap<String, Command>();
 	private List<StanzaHandler> stanzaHandlers = new ArrayList<StanzaHandler>();
 	private ChatHandler chatHandler;
 	private JabberID jid;
 	private XmppConnection connection;
 	private Random random = new Random();
 
 	private static class Message {
 		public Message(MessageType type) {
 			this.type = type;
 		}
 		
 		public Message(MessageType type, Object arg) {
 			this.type = type;
 			this.arg = arg;
 		}
 		
 		public MessageType getType() {
 			return type;
 		}
 		
 		public Object getArg() {
 			return arg;
 		}
 		
 		private MessageType type;
 		private Object arg;
 	}
 
 	private static enum MessageType {
 		CONNECTED,
 		DISCONNECTED,
 		USER_INPUT,
 		STANZA,
 		KEEP_ALIVE,
 		ERROR
 	}
 	
 	private class CorePlugin implements Plugin {
 		@Override
 		public void init(JabberClient client) {
 			client.registerCommand("help", new HelpCommand());
 			client.registerCommand("quit", new QuitCommand());
 			client.registerCommand("dev", new DeveloperCommand());
 		}
 
 		@Override
 		public void terminate() {
 		}
 	}
 
 	private class DeveloperCommand implements Command {
 		@Override
 		public String getShortDescription() {
 			return "Developer mode.";
 		}
 
 		@Override
 		public String getLongDescription() {
 			return "@dev on - Enable developer mode.\n"
 			      +"@dev off - Disable developer mode.\n"
 			      +"@dev - Check status.";
 		}
 
 		@Override
 		public void execute(String[] args) {
 			if(args.length == 1) {
 				printStatus();
 			}
 			else {
 				developerMode = args[1].equals("on");
 				printStatus();
 			}
 		}
 
 		private void printStatus() {
 			print("Developer mode is: " + (developerMode ? "on" : "off"));
 		}
 		
 	}
 
 	private class HelpCommand implements Command {
 		@Override
 		public String getShortDescription() {
 			return "Shows help. Use @help <command> to know more about a command";
 		}
 		
 		@Override
 		public String getLongDescription() {
 			return "@help [command]\n\n" + getShortDescription();
 		}
 		
 		@Override
 		public void execute(String[] args) {
 			if(args.length < 2) {//short help
 				for(Map.Entry<String, Command> pair: commands.entrySet()) {
 					print("@" + pair.getKey() + " - " + pair.getValue().getShortDescription());
 				}
 			}
 			else {//long help
 				String cmdName = args[1];
 				Command command = commands.get(cmdName);
 				if(command != null) {
 					print(command.getLongDescription());
 				}
 				else {
 					print("Unrecognized command");
 				}
 			}
 		}
 	};
 	
 	private class QuitCommand implements Command {
 
 		@Override
 		public String getShortDescription() {
 			return "Quit the program.";
 		}
 
 		@Override
 		public String getLongDescription() {
 			return "@quit\n\n"+getShortDescription();
 		}
 
 		@Override
 		public void execute(String[] args) throws InterruptedException {
 			print("Bye");
 			send("</stream:stream>");
 			running = false;
 		}
 	}
 }
