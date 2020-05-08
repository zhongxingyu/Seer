 package de.kroesch.clt;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.imageio.spi.ServiceRegistry;
 
 import de.kroesch.clt.internal.Crash;
 import de.kroesch.clt.internal.Discover;
 import de.kroesch.clt.internal.Error;
 import de.kroesch.clt.internal.Get;
 import de.kroesch.clt.internal.Help;
 import de.kroesch.clt.internal.History;
 import de.kroesch.clt.internal.Internal;
 import de.kroesch.clt.internal.LoadProperties;
 import de.kroesch.clt.internal.Match;
 import de.kroesch.clt.internal.Print;
 import de.kroesch.clt.internal.Repeat;
 import de.kroesch.clt.internal.SaveProperties;
 import de.kroesch.clt.internal.Set;
 import de.kroesch.clt.internal.Shutdown;
 import de.kroesch.clt.internal.Time;
 import de.kroesch.clt.internal.Version;
 import de.kroesch.clt.net.Curl;
 import de.kroesch.clt.net.HttPing;
 import de.kroesch.clt.scripting.Load;
 import de.kroesch.clt.security.Login;
 import de.kroesch.clt.storage.Ls;
 
 /**
  * Parses command line input.
  * 
  * @author karsten
  *
  */
 public class Parser {
 
 	private Map<String, Class<? extends Command>> commandMap = new HashMap<String, Class<? extends Command>>();
 	
 	private Environment environment;
 
 	private enum InternalCommands implements Runnable, Internal {
 		NOP {
 			public void run() {
 				// no operation
 			}
 		},
 		UNKNOWN {
 			public void run() {
 				environment.writer().printf("?Unknown command.\n");
 			}
 		};
 		
 		public static Environment environment;
 	}
 	
 	public Parser(Environment environment) {
 		changeEnvironment(environment);
 		
 		commandMap.put("ls", Ls.class);
 		commandMap.put("dir", Ls.class);
 		commandMap.put("shutdown", Shutdown.class);
 		commandMap.put("hist", History.class);
 		commandMap.put("history", History.class);
 		commandMap.put("set", Set.class);
 		commandMap.put("get", Get.class);
 		commandMap.put("print", Print.class);
 		commandMap.put("match", Match.class);
 		commandMap.put("help", Help.class);
 		commandMap.put("echo", Print.class);
 		commandMap.put("load", Load.class);
 		commandMap.put("savep", SaveProperties.class);
 		commandMap.put("loadp", LoadProperties.class);
 		commandMap.put("discover", Discover.class);
 		commandMap.put("auth", Login.class);
 		commandMap.put("curl", Curl.class);
 		commandMap.put("ping", HttPing.class);
 		commandMap.put("version", Version.class);
 		commandMap.put("time", Time.class);
 		commandMap.put("repeat", Repeat.class);
 		
 		// For testing purposes
 		commandMap.put("crash", Crash.class);
 	}
 	
 	public Runnable parseCommand(String command) {
 		if (null == command || "".equals(command.trim())) return InternalCommands.NOP;
 		
 		StringTokenizer tokenizer = new StringTokenizer(command, " ");
 		String commandName = tokenizer.nextToken();
 
 		if (commandMap.get(commandName.toLowerCase()) == null) return InternalCommands.UNKNOWN;
 		try {
 			Command cmd = (Command) commandMap.get(commandName).newInstance();
 			
 			cmd.setEnvironment(environment);
 			
 			if (cmd instanceof HasArguments) {
 				List<String> args = new ArrayList<String>();
 				while (tokenizer.hasMoreTokens()) {
 					String token = tokenizer.nextToken();
 					if (token.startsWith("$")) {
 						token = eval(token);
 					}
 					if (token.startsWith("\"")) {
 						token = quoted(token, tokenizer);
 					}
 					args.add(token);
 				}
 				((HasArguments) cmd).setArguments(args);
 			}
 			
 			return cmd;
 		} catch (InstantiationException e) {
 			return new Error(e.getMessage(), environment.writer());
 		} catch (IllegalAccessException e) {
 			return new Error(e.getMessage(), environment.writer());
 		}
 	}
 	
 	/**
 	 * Evaluate a variable. 
 	 */
 	public String eval(String variable) {
 		return environment.get(variable.substring(1));
 	}
 	
 	/**
 	 * Handles quoted tokens, possibly containing spaces.
 	 */
 	public String quoted(String token, StringTokenizer tokenizer) {
 		if (token.endsWith("\"")) return token.substring(1, token.length()-1);
 		
 		StringBuffer spaced = new StringBuffer(token.substring(1));
 		while (tokenizer.hasMoreTokens()) {
 			String next = tokenizer.nextToken();
 			spaced.append(" ").append(next);
 			if (next.endsWith("\"")) break;
 		}
 		return spaced.toString().substring(0, spaced.length()-1);
 	}
 
 	public void changeEnvironment(Environment environment) {
 		this.environment = environment;
 		InternalCommands.environment = environment;
 	}
 	
 	public List<String> availableCommands() {
 		List<String> commands = new ArrayList<String>(commandMap.keySet());
 		Collections.sort(commands);
 		return commands;
 	}
 	
 	public Class<? extends Command> byName(String name) {
 		return commandMap.get(name);
 	}
 	
   public void discoverPlugins() {
     File pluginDir = new File("plugins");
     File[] jarFiles = pluginDir.listFiles(new JarFileFilter());
     
     // Handle malformed or missing plugin directory
     if (jarFiles == null) {
     	environment.writer().printf("?Plugin directory not found.\n");
       return;
     }
 
     for (File jarFile : jarFiles) {
     	//environment.writer().printf("Searching for plugin in %s\n", jarFile);
 
       // Try to load plugin classes
       ClassLoader classLoader;
       try {
         classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Parser.class.getClassLoader());
       } catch (MalformedURLException e) {
         throw new RuntimeException(e);
       }
 
       for (Iterator<Command> iter = ServiceRegistry.lookupProviders(Command.class, classLoader); iter.hasNext();) {
       	Command plugin = iter.next();
         plugin.setEnvironment(environment);
         environment.writer().printf("Found plugin information for %s\n", plugin.toString());
         commandMap.put(plugin.toString(), plugin.getClass());
       }
     }
   }
   
   public static class JarFileFilter implements FilenameFilter {
     public boolean accept(File dir, String name) {
       return name.endsWith(".jar");
     }
   }
 }
