 package main;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import static org.jibble.pircbot.Colors.NORMAL;
 import static panacea.Panacea.*;
 
 import debugging.Log;
 
 import modules.Help;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 
 import panacea.MapFunction;
 import panacea.ReduceFunction;
 
 /**
  * NoiseBot
  *
  * @author Michael Mrozek
  *         Created June 13, 2009.
  */
 public class NoiseBot extends PircBot {
 	private static Map<String, Connection> CONNECTIONS = new HashMap<String, Connection>() {{
 		put("default", new Connection.DefaultConnection());
 		put("test", new Connection("Morasique-test", "#morasique"));
 	}};
 	
 	private static final String DEFAULT_CONNECTION = "default";
 	private static final String CMDLINE_CONNECTION = "cmdline";
 	static {
 		assert CONNECTIONS.containsKey(DEFAULT_CONNECTION) : "No 'default 'connection";
 		assert !CONNECTIONS.containsKey(CMDLINE_CONNECTION) : "Connection '" + CMDLINE_CONNECTION + "' shadows command-line connection";
 	}
 	
 	public static final String ME = "Morasique";
 
 	private final Connection connection;
 	public Git.Revision revision = Git.head();
 	private Map<String, NoiseModule> modules = new HashMap<String, NoiseModule>();
 	private Map<String, String> secretData;
 	public static NoiseBot me;
 
 	public boolean connect() {
 		Log.i("Connecting to " + this.connection.getServer() + ":" + this.connection.getPort() + " as " + this.connection.getNick());
 		if(this.isConnected()) {
 			Log.w("Already connected");
 			return true;
 		}
 		
 		try {
 			System.out.println("Connecting to " + this.connection.getServer() + ":" + this.connection.getPort() + " as " + this.connection.getNick());
 			this.connect(this.connection.getServer(), this.connection.getPort(), this.connection.getPassword());
 			System.out.println("Joining " + this.connection.getChannel());
 			this.joinChannel(this.connection.getChannel());
 			return true;
 		} catch(NickAlreadyInUseException e) {
 			System.err.println("The nick " + this.connection.getNick() + " is already in use");
 		} catch(IrcException e) {
 			System.err.println("Unexpected IRC error: " + e.getMessage());
 		} catch(IOException e) {
 			System.err.println("Network error: " + e.getMessage());
 		}
 		
 		return false;
 	}
 	
 	public void quit() {
 		this.disconnect();
 		try {this.saveModules();} catch(ModuleSaveException e) {Log.e(e);}
 		Log.i("Quiting");
 		exit();
 	}
 	
 	public Map<String, NoiseModule> getModules() {return this.modules;}
 	
 	private void loadModules() {
 		if(!this.modules.isEmpty()) {
 			Log.w(pluralize(this.modules.size(), "module", "modules") + " already loaded");
 			return;
 		}
 		
 		// Always load the module manager
 		try {
 			this.loadModule("ModuleManager");
 		} catch(ModuleLoadException e) {
 			Log.e(e);
 		}
 		
 		final File moduleFile = new File(YamlSerializer.ROOT, "modules");
 		if(moduleFile.exists()) {
 			try {
 				final String[] moduleNames = YamlSerializer.deserialize(moduleFile.getName(), String[].class);
 				Log.i("Loading " + moduleNames.length + " modules from store");
 		
 				for(String moduleName : moduleNames) {
 					if(moduleName.equals("ModuleManager")) {continue;}
 					try {
 						this.loadModule(moduleName);
 					} catch(ModuleLoadException e) {
 						Log.e("Failed loading module " + moduleName);
 						Log.e(e);
 					}
 				}
 			} catch(Exception e) {
 				Log.e("Failed to load modules");
 				Log.e(e);
 			}
 		}
 		
 		{
 			final int moduleCount = this.modules.size();
 			final int patternCount = reduce(map(this.modules.values().toArray(new NoiseModule[0]), new MapFunction<NoiseModule, Integer>() {
 				@Override public Integer map(NoiseModule module) {
 					return module.getPatterns().length;
 				}
 			}), new ReduceFunction<Integer, Integer>() {
 				@Override public Integer reduce(Integer source, Integer accum) {
 					return source + accum;
 				}
 			}, 0);
 			
 			Log.i("Done loading revision " + this.revision.getHash());
 			this.sendNotice("NoiseBot revision " + this.revision.getHash());
 			this.sendNotice("Done loading " + moduleCount + " modules watching for " + patternCount + " patterns");
 		}
 	}
 	
 	public void saveModules() throws ModuleSaveException {
 		Log.i("Saving " + this.modules.size() + " modules to store");
 		
 		final String[] moduleNames = this.modules.keySet().toArray(new String[0]);
 
 		try {
             YamlSerializer.serialize("modules", moduleNames);
 		} catch(IOException e) {
 			throw new ModuleSaveException("Failed saving module list");
 		}
 		
 		final Vector<NoiseModule> failedSaves = new Vector<NoiseModule>();
 		for(NoiseModule module : this.modules.values()) {
 			if(!module.save()) {
 				failedSaves.add(module);
 				Log.e("Failed saving module " + module.getClass().getSimpleName());
 			}
 		}
 		
 		if(!failedSaves.isEmpty()) {
 			throw new ModuleSaveException("Unable to save some modules: " + implode(map(failedSaves.toArray(new NoiseModule[0]), new MapFunction<NoiseModule, String>() {
 				@Override public String map(NoiseModule module) {return module.getClass().getSimpleName();}
 			}), ", "));
 		}
 	}
 	
 	public void loadModule(String moduleName) throws ModuleLoadException {
 		Log.i("Loading module: " + moduleName);
 		if(this.modules.containsKey(moduleName)) {
 			throw new ModuleLoadException("Module " + moduleName + " already loaded");
 		}
 		
 		try {
 			final Class<? extends NoiseModule> c = (Class<? extends NoiseModule>)getModuleLoader().loadClass("modules." + moduleName);
 			c.newInstance();
 			NoiseModule module;
 			
 			// Try loading from disk
 			module = NoiseModule.load(c);
 			
 			// Otherwise just make a new one
 			if(module == null) {
 				module = c.newInstance();
 			}
 			
 			if(module.getFriendlyName() == null) {
 				throw new ModuleLoadException("Module " + moduleName + " does not have a friendly name");
 			}
 			
 			module.init(this);
 			this.modules.put(moduleName, module);
 		} catch(ClassCastException e) {
 			Log.e(e);
 			throw new ModuleLoadException("Defined module " + moduleName + " does not extend NoiseModule");
 		} catch(ClassNotFoundException e) {
 			Log.e(e);
 			throw new ModuleLoadException("Unable to instantiate module " + moduleName + ": Module does not exist");
 		} catch(Exception e) {
 			Log.e(e);
 			throw new ModuleLoadException("Unable to instantiate module " + moduleName + ": " + e.getMessage());
 		}
 	}
 	
 	public void unloadModule(String moduleName) throws ModuleUnloadException {
 		Log.i("Unloading module: " + moduleName);
 		final Class c;
 		try {
 			c = getModuleLoader().loadClass("modules." + moduleName);
 		} catch(Exception e) {
 			throw new ModuleUnloadException("Unable to unload module " + moduleName + ": Module does not exist");
 		}
 		
 		if(this.modules.containsKey(moduleName)) {
 			final NoiseModule module = this.modules.get(moduleName);
 			module.save();
 			module.unload();
 			this.modules.remove(moduleName);
 		} else {
 			throw new ModuleUnloadException("Unable to unload module " + moduleName + ": Module not loaded");
 		}
 
 		// Immediately reload the module manager
 		if(moduleName.equals("ModuleManager")) {
 			try {
 				loadModule(moduleName);
 			} catch(ModuleLoadException e) {
 				throw new ModuleUnloadException(e);
 			}
 		}
 	}
 
 	public User[] getUsers() {return this.getUsers(this.connection.getChannel());}
 	public String[] getNicks() {
 		return map(this.getUsers(), new MapFunction<User, String>() {
 			@Override public String map(User source) {
 				return source.getNick();
 			}
 		}, new String[0]);
 	}
 	public boolean isOnline(String nick) {
 		for(User user : this.getUsers()) {
 			if(nick.equals(user.getNick())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void sync() {
 		final Git.Revision[] revs = Git.diff(this.revision.getHash(), "HEAD");
 		final Git.Revision oldrev = this.revision;
 		final String[] moduleNames = Git.affectedModules(this.revision.getHash(), "HEAD");
 		this.revision = Git.head();
 		if(moduleNames.length == 0) {
 			this.sendNotice("Unable to sync -- No classes changed");
 			return;
 		}
 		final String[] coloredNames = map(moduleNames, new MapFunction<String, String>() {
 			@Override public String map(String name) {
 				return Help.COLOR_MODULE +  name + NORMAL;
 			}
 		});
 
 		for(String moduleName : moduleNames) {
 			try {
 				this.unloadModule(moduleName);
 			} catch(ModuleUnloadException e) {}
 
 			if(!moduleName.equals("ModuleManager")) {
 				try {
 					this.loadModule(moduleName);
 				} catch(ModuleLoadException e) {
 					throw new Git.SyncException("Unable to load module " + moduleName);
 				}
 			}
 		}
 		
 		this.sendNotice("Synced " + pluralize(revs.length, "revision", "revisions") + ":");
 		for(Git.Revision rev : reverse(revs))
 			this.sendNotice("    " + rev);
 		this.sendNotice("Reloaded modules: " + implode(coloredNames, ", "));
 		this.sendNotice("Changes: " + Git.diffLink(oldrev, this.revision));
 	}
 
 	public String getSecretData(String key) {
 		return this.secretData.containsKey(key) ? this.secretData.get(key) : null;
 	}
 
 	public void sendMessage(String[] parts, String sep) {
 		final int maxSize = 400; // might be able to be kicked up a notch
 		StringBuffer msg = new StringBuffer();
 		for (int i = 0; i < parts.length; i++) {
 			String part = parts[i];
 			int nextSize = msg.length() + sep.length() + part.length();
 			if (nextSize > maxSize) {
 				this.sendMessage(msg.toString());
 				msg.setLength(0);
 			} else if (i > 0) {
 				msg.append(sep);
 			}
 			msg.append(part);
 		}
 		if (msg.length() > 0)
 			this.sendMessage(msg.toString());
 	}
 	public void sendMessage(String message) {Log.out("M> " + message); this.sendMessage(this.connection.getChannel(), message);}
 	public void sendAction(String action) {Log.out("A> " + action); this.sendAction(this.connection.getChannel(), action);}
 	public void sendNotice(String notice) {Log.out("N> " + notice); this.sendNotice(this.connection.getChannel(), notice);}
 	public void reply(Message sender, String message) {this.reply(sender.getSender(), message);}
 	public void reply(String username, String message) {this.sendMessage((username == null ? "" : username + ": ") + message);}
 	public void kickVictim(String victim, String reason) {this.kick(this.connection.getChannel(), victim, reason);}
 
 	@Override protected void onMessage(String channel, String sender, String login, String hostname, String message) {
 		Log.in("<" + sender + " (" + login + " @ " + hostname + ") -> " + channel + ": " + message);
 		if(!channel.equals(this.connection.getChannel())) {Log.w("Ignoring message to channel " + channel); return;}
 
 		for(NoiseModule module : this.modules.values()) {
 			if(module.isPrivate() && !sender.equals(ME)) {continue;}
 			
 			try {
 				module.processMessage(new Message(message.trim(), sender, false));
 			} catch(Exception e) {
 				this.sendNotice(e.getMessage());
 				Log.e(e);
 			}
 		}
 	}
 	
 	@Override protected void onPrivateMessage(String sender, String login, String hostname, String message) {
 		Log.in("<" + sender + " (" + login + " @ " + hostname + ") -> (direct): " + message);
 
 		for(NoiseModule module : this.modules.values()) {
 			if(module.isPrivate() && !sender.equals(ME)) {continue;}
 			
 			try {
 				module.processMessage(new Message(message.trim(), sender, true));
 			} catch(Exception e) {
 				this.sendNotice(sender, e.getMessage());
 				Log.e(e);
 			}
 		}
 	}
 
 	@Override protected void onJoin(String channel, String sender, String login, String hostname) {
 		if(sender.equals(this.connection.getNick())) { // Done joining channel
 			Log.v("Done joining channel: " + channel);
 			if(this.connection.getPassword() != null)
 				this.sendMessage("ChanServ", "VOICE " + this.connection.getChannel());
 			this.loadModules();
 		} else {
 			Log.v("Joined " + channel + ": " + sender + " ( " + login + "@" + hostname + ")");
 			for(NoiseModule module : this.modules.values()) {module.onJoin(sender, login, hostname);}
 		}
 	}
 
 	@Override protected void onPart(String channel, String sender, String login, String hostname) {
 		Log.v("Parted " + channel + ": " + sender + " ( " + login + "@" + hostname + ")");
 		for(NoiseModule module : this.modules.values()) {module.onPart(sender, login, hostname);}
 	}
 	
 	@Override protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
 		Log.v("Quit: " + sourceNick + " ( " + sourceLogin + "@" + sourceHostname + "): " + reason);
 		for(NoiseModule module : this.modules.values()) {module.onQuit(sourceNick, sourceLogin, sourceHostname, reason);}
 	}
 	
 	@Override protected void onUserList(String channel, User[] users) {
 		for(NoiseModule module : this.modules.values()) {module.onUserList(users);}
 	}
 
 	@Override protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
 		Log.v("Kick " + channel + ": " + kickerNick + " ( " + kickerLogin + "@" + kickerHostname + ") -> " + recipientNick + ": " + reason);
 		for(NoiseModule module : this.modules.values()) {module.onKick(kickerNick, kickerLogin, kickerHostname, recipientNick, reason);}
 	}
 
 	@Override protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
 		Log.v("Topic " + channel + ": " + setBy + ": " + topic);
 		for(NoiseModule module : this.modules.values()) {module.onTopic(topic, setBy, date, changed);}
 	}
 
 	@Override protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
 		Log.v("Nick change: " + oldNick + " -> " + newNick + " ( " + login + "@" + hostname+ ")");
 		for(NoiseModule module : this.modules.values()) {module.onNickChange(oldNick, login, hostname, newNick);}
 	}
 
 	@Override protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
 		if(recipient.equalsIgnoreCase(this.connection.getNick())) {
 			Log.v("Bot opped -- requesting deop");
 			this.sendMessage("ChanServ", "DEOP " + this.connection.getChannel());
 		}
 	}
 
 	// This isn't called if we tell PircBot to disconnect, only if the server disconnects us
 	@Override protected void onDisconnect() {
 		Log.i("Disconnected");
 
 		while(!this.connect()) {
 			sleep(30);
 		}
     }
 	
 	public static void main(String[] args) {
 		final String connectionName = args.length == 0 ? DEFAULT_CONNECTION : args[0];
 		final Connection connection;
 		
 		if(connectionName.equals(CMDLINE_CONNECTION)) {
 			if(args.length != 6) {
 				System.out.println("Missing arguments for command-line connection; expected: server port nick password channel");
 				return;
 			}
 			
 			connection = new Connection(args[1], Integer.parseInt(args[2]), args[3], args[4], args[5]);
 		} else if(CONNECTIONS.containsKey(connectionName)) {
 			connection = CONNECTIONS.get(connectionName);
 		} else {
 			System.out.println("No connection named '" + connectionName + "'");
 			return;
 		}
 
 		new NoiseBot(connection);
 	}
 	
 	public NoiseBot(Connection connection) {
 		me = this;
 		Log.i("NoiseBot has started");
 		this.connection = connection;
 		
 		try {
 			this.setEncoding("ISO8859_1");
 		} catch (UnsupportedEncodingException e) {
 			System.err.println("Unable to set encoding: " + e.getMessage());
 		}
 		
 		try {
			this.secretData = YamlSerializer.deserialize("../secret-data", HashMap.class);
 		} catch(FileNotFoundException e) {
 			this.secretData = new HashMap<String, String>();
 		}
 		
 		this.setName(this.connection.getNick());
 		this.setLogin(this.connection.getNick());
 		this.connect();
 	}
 	
 	private static ClassLoader getModuleLoader() {
 		return new ClassLoader(NoiseBot.class.getClassLoader()) {
 			public Class loadClass (String name, boolean resolve) throws ClassNotFoundException {
 				Class c = null;
 //				if((c = findLoadedClass(name)) != null) {return c;}
 				File f = new File("bin", name.replace('.', File.separatorChar) + ".class");
 				if(name.startsWith("modules.") && f.exists()) {
 					int length = (int) f.length();
 					byte[] classbytes = new byte[length];
 					try {
 						DataInputStream in = new DataInputStream(new FileInputStream(f));
 						in.readFully(classbytes);
 						in.close();
 					} catch(FileNotFoundException e) {
 						throw new ClassNotFoundException(e.getMessage());
 					} catch(IOException e) {
 						throw new ClassNotFoundException(e.getMessage());
 					}
 					
 					c = defineClass(name, classbytes, 0, length);
 					if(resolve) {resolveClass(c);}
 					return c;
 				}
 				
 				if((c = findSystemClass(name)) != null) {return c;}
 				throw new ClassNotFoundException("Unknown class " + name);
 			}
 		};
 	}
 }
