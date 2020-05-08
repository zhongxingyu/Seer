 package jisssea.controller;
 
 import static jisssea.util.CollectionsUtility.getMapKey;
 import static jisssea.util.PluginUtility.loadPlugins;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import jisssea.bot.Bot;
 import jisssea.bot.BotRegistry;
 import jisssea.config.Config;
 import jisssea.config.Server;
 import jisssea.controller.commands.api.Command;
 import jisssea.controller.messages.Message;
 import jisssea.controller.messages.MessageType;
 import jisssea.controller.messages.UserMessage;
 import jisssea.controller.predicates.DefaultPredicate;
 import jisssea.ui.DisplayWindow;
 import jisssea.ui.InformationWindow;
 import jisssea.util.Procedure;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import charvax.swing.SwingUtilities;
 
 /**
  * @author richard
  * 
  *         Controller sends messages to appropriate views, and processes
  *         commands for processing also holds appropriate window state, eg which
  *         network its connected to
  * 
  */
 public class Controller {
 
 	private BotRegistry reg;
 	private final BlockingQueue<Message> q;
 	private final List<Command> commands;
 	private final Map<Integer, Pipe> pipes;
 
 	private final InformationWindow infoWindow;
 
 	private int currentWindow;
 	private int totalWindows;
 
 	private static final Log log = LogFactory.getLog(Controller.class);
 
 	public Controller() {
 		currentWindow = 0;
 		totalWindows = 0;
 		commands = loadCommands();
 		pipes = new HashMap<Integer, Pipe>();
 		q = new LinkedBlockingQueue<Message>();
 		// setup information window
 		// infoWindow = null;
 		infoWindow = new InformationWindow();
 
 		// log.debug("done info window");
 
 		try {
 			final Config cfg = Config.fromFile("etc/conf.yaml");
 			reg = new BotRegistry(cfg.getUser(), this);
 
 			// setup status pipe
 			statusPipe();
 			final DisplayWindow window = window();
 
 			for (Server server : cfg.getServers()) {
 				final Bot bot = reg.connect(server);
 
 				if (bot != null) {
 					for (String channel : server.getChannels())
 						message(new UserMessage("/join " + channel, window));
 				}
 			}
 
 			checkMessageQueue();
 
 		} catch (Throwable e) {
 			try {
 				window().hide();
 			} catch (Exception e1) {
 
 			}
 			log.error("Error caught at main thread", e);
 		}
 	}
 
 	private void statusPipe() {
 		Pipe status = createPipe();
 		DefaultPredicate pred = new DefaultPredicate();
 		pred.correspondants.clear();
 		pred.messageTypes.clear();
 		pred.messageTypes.addAll(Arrays.asList(MessageType.CONNECT, MessageType.DISCONNECT));
 		status.predicates.add(pred);
 
 		window().show();
 		log.debug("Setup status pipe");
 	}
 
 	private DisplayWindow window() {
 		return pipes.get(currentWindow).window;
 	}
 
 	private List<Command> loadCommands() {
 		return loadPlugins(Command.class, "bin", "jisssea.controller.commands");
 	}
 
	@SuppressWarnings("unchecked")
 	public <T extends Command> T requestCommand(Class<T> cls) {
 		for (Command c : commands) {
 			if (c.getClass().equals(cls)) {
 				return (T) c;
 			}
 		}
 		return null;
 	}
 
 	public InformationWindow getInfoWindow() {
 		return infoWindow;
 	}
 
 	/**
 	 * Method applies appropriate dispatching of messages to windows
 	 * 
 	 * @param msg
 	 *            Message being sent to the user
 	 */
 	public void checkMessageQueue() {
 		while (true) {
 			try {
 				log.debug("Beginning Check Message");
 				Runtime r = Runtime.getRuntime();
 				long foo = r.totalMemory() - r.freeMemory();
 				log.debug("Memory used: " + foo);
 				final Message msg = q.take();
 				// process commands
 				log.debug("Executing commands");
 				for (Command cmd : commands) {
 					try {
 						cmd.act(msg, reg, this);
 					} catch (Exception e) {
 						log.warn("Error in a command", e);
 					}
 				}
 				log.debug("Updating Pipes");
 				// update pipes
 				for (Entry<Integer, Pipe> pipe : pipes.entrySet()) {
 					pipe.getValue().enQueue(msg);
 				}
 				// update UI
 				Pipe visiblePipe = pipes.get(currentWindow);
 				List<Message> visibleMsgs = visiblePipe.requestAll();
 				log.debug("Outputting " + visibleMsgs.size() + " msgs to window " + currentWindow + " of " + totalWindows);
 				window().printMessages(visibleMsgs);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Enqueues messages
 	 */
 	public void message(Message msg) {
 		q.add(msg);
 		if (log.isDebugEnabled())
 			log.debug("Enqueing " + msg);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		if (!System.getProperty("java.vm.name").contains("Server")) {
 			System.err.println("This is SRS software - you must use a server jvm");
 			System.exit(-1);
 		}
 		log.debug("Starting");
 		new Controller();
 	}
 
 	public Pipe createPipe() {
 		Pipe pipe = new Pipe(pipes.size(), 10, this);
 		pipes.put(totalWindows, pipe);
 		totalWindows++;
 		return pipe;
 	}
 
 	/**
 	 * Sets up a default Predicate
 	 * 
 	 * @param attachedTo
 	 * @return
 	 */
 	public Pipe createPipe(Target attachedTo) {
 		final Pipe pipe = createPipe();
 		DefaultPredicate pred = new DefaultPredicate(attachedTo);
 		pipe.predicates.add(pred);
 		return pipe;
 	}
 
 	public void selectPipe(Pipe pipe) {
 		window().hide();
 		try {
 			currentWindow = getMapKey(pipes, pipe);
 			updateUI();
 		} catch (IllegalArgumentException e) {
 			log.fatal("Internal Error selecting pipe", e);
 			System.exit(1);
 		}
 
 	}
 
 	private void updateUI() {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				window().show();
 				window().repaint();
 			}
 		});
 	}
 
 	public void selectPipe(int windowNumber) {
 		if (pipes.keySet().contains(windowNumber)) {
 			window().hide();
 			currentWindow = windowNumber;
 			updateUI();
 		} else {
 			throw new IllegalArgumentException(windowNumber + "Is not a valid window");
 		}
 	}
 
 	public Pipe getPipe(DisplayWindow window) {
 		for (Entry<Integer, Pipe> e : pipes.entrySet()) {
 			Pipe pipe = e.getValue();
 			if (pipe.window == window)
 				return pipe;
 		}
 		return null;
 	}
 
 	@Deprecated
 	public void foreachPipe(Procedure<Pipe> proc) {
 		for (Entry<Integer, Pipe> e : pipes.entrySet()) {
 			if (!proc.f(e.getValue()))
 				break;
 		}
 	}
 
 	public Set<Entry<Integer, Pipe>> getPipes() {
 		return pipes.entrySet();
 	}
 
 	public void closePipe(Pipe pipe) {
 		if (getMapKey(pipes, pipe) == currentWindow) {
 			selectPipe(0);
 		}
 		pipe.window.hide();
 		pipes.values().remove(pipe);
 	}
 }
