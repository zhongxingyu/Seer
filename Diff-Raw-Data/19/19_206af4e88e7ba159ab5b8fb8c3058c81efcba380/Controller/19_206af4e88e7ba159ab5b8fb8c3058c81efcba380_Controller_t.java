 package jisssea.controller;
 
 import static jisssea.util.CollectionsUtility.getMapKey;
 
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import jisssea.bot.Bot;
 import jisssea.bot.BotRegistry;
 import jisssea.config.Config;
 import jisssea.config.Server;
 import jisssea.controller.commands.Command;
 import jisssea.controller.messages.Message;
 import jisssea.controller.messages.MessageType;
 import jisssea.controller.messages.UserMessage;
 import jisssea.controller.predicates.DefaultPredicate;
 import jisssea.ui.DisplayWindow;
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
  *         Currently only has 1 window, and sends all messages to it
  * 
  */
 public class Controller {
 
 	private BotRegistry reg;
 	private final BlockingQueue<Message> q;
 	private final List<Command> commands;
 	private final Map<Integer, Pipe> pipes;
 	private int currentWindow;
 	private int totalWindows;
 
 	private static final Log log = LogFactory.getLog(Controller.class);
 
 	public Controller() {
 		currentWindow = 0;
 		totalWindows = 0;
 		commands = loadCommands();
 		pipes = new HashMap<Integer, Pipe>();
 		q = new LinkedBlockingQueue<Message>();
 
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
 			e.printStackTrace();
 		}
 	}
 
 	private void statusPipe() {
 		Pipe status = createPipe();
 		DefaultPredicate pred = new DefaultPredicate("");
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
 		List<Command> commands = new ArrayList<Command>();
		final String[] names = { "jisssea.controller.commands.JoinCommand", "jisssea.controller.commands.PartCommand",
				"jisssea.controller.commands.WindowCommand", "jisssea.controller.commands.PartReceivedCommand",
				"jisssea.controller.commands.TopicCommand", "jisssea.controller.commands.SayCommand", "jisssea.controller.commands.NickCommand",
				"jisssea.controller.commands.FilterCommand", "jisssea.controller.commands.AliasCommand",
				"jisssea.controller.commands.NickChangedCommand", "jisssea.controller.commands.PMCommand",
				"jisssea.controller.commands.ActionCommand", };
 		for (String name : names) {
 			try {
 				Class<?> cls = Class.forName(name);
 				if (!cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
 					Command cmd = (Command) cls.newInstance();
 					commands.add(cmd);
 				} else {
 					// TODO: some error
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return commands;
 	}
 
 	public <T extends Command> T requestCommand(Class<T> cls) {
 		for (Command c : commands) {
 			if (c.getClass().equals(cls)) {
 				return (T) c;
 			}
 		}
 		return null;
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
 					cmd.act(msg, reg, this);
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
 	public Pipe createPipe(String attachedTo) {
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
 
 	public void foreachPipe(Procedure<Pipe> proc) {
 		for (Entry<Integer, Pipe> e : pipes.entrySet()) {
 			if (!proc.f(e.getValue()))
 				break;
 		}
 	}
 
 	public void closePipe(Pipe pipe) {
 		if (getMapKey(pipes, pipe) == currentWindow) {
 			selectPipe(0);
 		}
 		pipe.window.hide();
 		pipes.values().remove(pipe);
 	}
 
 	/*
 	 * 
 	 * public void handle(String msg) { final Bot bot = reg.get("uwcsnet");
 	 * if(bot != null) bot.sendRawLine(msg); }
 	 */
 }
