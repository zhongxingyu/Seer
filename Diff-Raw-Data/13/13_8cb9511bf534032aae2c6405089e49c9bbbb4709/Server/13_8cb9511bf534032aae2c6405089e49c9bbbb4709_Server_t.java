 package darep.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.util.HashMap;
 import java.util.Properties;
 
 import darep.Command;
 import darep.Command.ActionType;
 import darep.repos.Repository;
 import darep.repos.RepositoryException;
 
 public class Server {
 	
 	private Repository repository;
 	private Properties properties = new Properties();
 	private Command userCommand;
 	private static final String[] propertyNames = {"incoming-directory", "html-overview", "log-file", 
 											"checking-interval-in-seconds", "completeness-checker.class-name" };
 	
 	private static final int INCOMING_DIRECTORY = 0;
 	private static final int HTML_OVERVIEW = 1;
 	private static final int LOG_FILE = 2;
 	private static final int CHECKING_INTERVAL_IN_SECONDS = 3;
 	private static final int COMPLETENESS_CHECKER_CLASS_NAME = 4;
 	
 	private boolean running = true;
 	public Server(Repository repository, Command command) throws ServerException {
 		this.repository = repository;
 		this.userCommand = command;
 		loadPropertiesFile(command.getParams()[0]);
 	}
 	
 	public void start() throws ServerException {
 		while(running) {
 			File[] files = checkIncomingDirectory();
 			for(File f : files) {
 				try {
 					addFile(f); 
 				} catch (RepositoryException e) {
 					throw new ServerException("server could not add file " + f.getPath());
 				}
 			}
 			int seconds = Integer.parseInt(getProperty(CHECKING_INTERVAL_IN_SECONDS));
 			try {
 				Thread.sleep(seconds*1000);
 			} catch (InterruptedException e) {
 				throw new ServerException(e);
 			}
 		}
 	}
 	
 	private String getProperty(int propertyKey) {
 		return properties.getProperty(propertyNames[propertyKey]);
 	}
 	
 	private void loadPropertiesFile(String propertiesPath) throws ServerException {
 		try {
 			properties.load(new FileInputStream(propertiesPath));
 			for(String p : propertyNames) {
 				if(properties.getProperty(p) == null) {
 					throw new ServerException("Missing property " + p + " in the property file " + propertiesPath);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			throw new ServerException("could not find properties file " + propertiesPath, e);
 		} catch (IOException e) {
 			throw new ServerException("could not read properties file " + propertiesPath, e);
 		}
 	}
 	
 	private File[] checkIncomingDirectory() {
 		File dir = new File(getProperty(INCOMING_DIRECTORY));
 		// TODO implement completeness-checker
		File[] files = dir.listFiles();
		if(files != null) {
			for(File f : files) {
				System.out.println("found " + f.getPath() + "file");
			}
			return files;
		} else {
			System.out.println("no files found");
			return new File[0];
		}
 	}
 	
 	private void addFile(File file) throws RepositoryException {
 		Command command = new Command(ActionType.add, new String[] {file.getPath()}, null, new String[] {"m"});
 		this.repository.add(command);
 	}
 }
