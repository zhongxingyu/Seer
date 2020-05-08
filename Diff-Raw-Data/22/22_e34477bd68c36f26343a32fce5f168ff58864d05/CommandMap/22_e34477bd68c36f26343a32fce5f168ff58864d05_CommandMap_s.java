 package net.mightybyte.gigs.commands;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 public class CommandMap {
 	private static CommandMap instance = new CommandMap("commands.props");
	private static Logger logger = Logger.getLogger("gigs");
 	private Map<String, Command> commands;
 
 	private String propsFile;
 
 	public static CommandMap getInstance() {
 		return instance;
 	}
 
 	private CommandMap(String filename) {
 		propsFile = filename;
 		loadCommands();
 	}
 
 	public void loadCommands() {
 	  logger.debug("Loading CommandMap");
 
 		commands = new HashMap<String, Command>();
 		Properties props = new Properties();
 
 		try {
 			ClassLoader loader = new URLClassLoader(new URL[] {
 					new File(".").toURI().toURL(),
 					new File("classes").toURI().toURL() });
 
 			props.load(new FileInputStream(propsFile));
 			for (Object o : props.keySet()) {
 				String s = (String) o;
 				String className = props.getProperty(s);
 				// Class c = Class.forName(className);
 				Class c = loader.loadClass(className);
 				Command cmd = (Command) c.newInstance();
 				commands.put(s, cmd);
 			}
 		} catch (IOException e) {
 			logger.error("Could not open " + propsFile);
 		} catch (Exception e) {
 			logger.error("Error in " + propsFile);
 			logger.error(e.getMessage());
 		}
 
 	}
 
 	public Command getCommand(String commandName) {
 		return commands.get(commandName);
 	}
 }
