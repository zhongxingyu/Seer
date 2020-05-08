 package darep.server;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import darep.Command;
 import darep.Command.ActionType;
 import darep.logger.Logger;
 import darep.renderer.Renderer;
 import darep.renderer.htmlRenderer.HtmlRenderer;
 import darep.repos.Repository;
 import darep.repos.RepositoryException;
 
 public class Server extends Thread {
 	
 	private Repository repository;
 	private Logger serverLogger;
 	private Properties properties = new Properties();
 	private final String[] propertyValues;
 	private boolean running = true;
 	private CompletenessChecker completenessChecker;
 	private Map<String, String> completenessCheckerProperties;
 	private BufferedWriter htmlListWriter;
 	private Renderer htmlRenderer;
 	private static final String[] propertyNames = {"incoming-directory", "html-overview", "log-file", 
 		"checking-interval-in-seconds", "completeness-checker.class-name" };
 	private static final int INCOMING_DIRECTORY = 0;
 	private static final int HTML_OVERVIEW = 1;
 	private static final int LOG_FILE = 2;
 	private static final int CHECKING_INTERVAL_IN_SECONDS = 3;
 	private static final int COMPLETENESS_CHECKER_CLASS_NAME = 4;
 	
 	
 	public Server(Repository repository, Command command) throws ServerException {
 		
 		propertyValues = new String[] {
 				null,
 				null,
 				new File(repository.getLocation(), "server.log").toString(),
 				null,
 				null
 		};
 		
 		this.repository = repository;
 		loadPropertiesFile(command.getParams()[0]);
 		completenessCheckerProperties = getCompletenessCheckerProperties(properties);
 		createServerFiles();
 		completenessChecker = getCompletenessChecker();
 		serverLogger = new ServerLogger(getProperty(LOG_FILE));
 		repository.setLogger(serverLogger);
 		htmlRenderer = new HtmlRenderer();
 	}
 	
 	private Map<String, String> getCompletenessCheckerProperties(Properties p) {
 		HashMap<String, String> m = new HashMap<String, String>();
 		
 		for (String key: p.stringPropertyNames()) {
 			if (key.startsWith("completeness-checker.")) {
 				String newKey = key.substring("completeness-checker.".length());
 				
 				if (newKey.equals("class-name")) {
 					continue;
 				}
 				m.put(newKey, p.getProperty(key));
 			}
 		}
 		
 		return m;
 	}
 
 	private CompletenessChecker getCompletenessChecker() throws ServerException {
		String completenessCheckerClassName = getProperty(COMPLETENESS_CHECKER_CLASS_NAME);
 		try {
			Class<?> cls = Class.forName(completenessCheckerClassName);
 			Object o = cls.newInstance();
 			if (o instanceof CompletenessChecker) {
 				CompletenessChecker c = (CompletenessChecker) o;
 				for (Entry<String, String> prop: completenessCheckerProperties.entrySet()) {
 					c.setProperty(prop.getKey(), prop.getValue());
 				}
 				return c;
 			} 
			throw new ServerException("Specified class: " + completenessCheckerClassName + 
													" does not implement CompletenessChecker");
 		} catch (ClassNotFoundException e) {
			throw new ServerException("Could not found the specified class: " + completenessCheckerClassName, e);
 		} catch (InstantiationException e) {
			throw new ServerException("Could not instantiate specified class: " + completenessCheckerClassName, e);
 		} catch (IllegalAccessException e) {
 			throw new ServerException(e);
 		}
 	}
 
 	private void createServerFiles() throws ServerException {
 		File incoming = new File(getProperty(INCOMING_DIRECTORY));
 		if(incoming.exists() == false) {
 			throw new ServerException("incoming directory " + incoming.getAbsolutePath() + 
 					" does not exist.");
 		}		
 		
 		File log = new File(getProperty(LOG_FILE));
 		try {
 			log.createNewFile();
 		} catch (IOException e) {
 			throw new ServerException("could not create logfile " + log.getAbsolutePath() + 
 					" because the folder does not exist", e);
 		}
 		File htmlOverview = new File(getProperty(HTML_OVERVIEW));
 		try {
 			htmlOverview.createNewFile();
 		} catch (IOException e) {
 			throw new ServerException("could not create htmloverviewfile " + htmlOverview.getAbsolutePath() + 
 					" because folder does not exist");
 		}
 		
 		
 	}
 	
 	@Override
 	public void run() {
 		try {
 			renderHtmlOverview();
 			
 			int seconds;
 			try {
 				seconds = Integer.parseInt(getProperty(CHECKING_INTERVAL_IN_SECONDS));
 			} catch (NumberFormatException e) {
 				throw new ServerException("checking-interval-in-seconds property must be a number");
 			}
 			if(seconds < 0) {
 				throw new ServerException("checking-interval-in-seconds property must be bigger 1 or bigger");
 			}
 			
 			File dir = new File(getProperty(INCOMING_DIRECTORY));
 			
 			while(running) {
 				try {
 					Thread.sleep(seconds*1000);
 				} catch (InterruptedException e) {
 					throw new ServerException(e);
 				}
 				File[] files;
 				try {
 					files = completenessChecker.getCompletedFiles(dir);
 				} catch (Exception e) {
 					repository.getLogger().logError("There was an Error in CompletenessChecker");
 					repository.getLogger().logError(e.getMessage());
 					continue;
 				}
 				for(File f : files) {
 					try {
 						addFile(f); 
 					} catch (RepositoryException e) {
 						throw new ServerException("server could not add file " + f.getPath());
 					}
 				}
 				if(files.length > 0) {
 					renderHtmlOverview();
 				}
 			}
 		} catch (ServerException e) {
 			serverLogger.logError(e.getMessage());
 		}
 	}
 
 	private void renderHtmlOverview() throws ServerException {
 		File htmlList = new File(getProperty(HTML_OVERVIEW));
 		try {
 			htmlListWriter = new BufferedWriter(new FileWriter(htmlList));
 		} catch (IOException e) {
 			throw new ServerException("Could not write the data-repository list to the html file " + getProperty(HTML_OVERVIEW));
 		}
 		try {
 			htmlListWriter.write(htmlRenderer.render(repository.getDatasets()));
 		} catch (RepositoryException e1) {
 			throw new ServerException("could not render html list", e1);
 		} catch (IOException e1) {
 			throw new ServerException("could not write list to html file");
 		}
 		try {
 			htmlListWriter.close();
 		} catch (IOException e) {
 			throw new ServerException("could not close html file. file might be broken or in a wrong state");
 		}
 	}
 	
 	private String getProperty(int propertyKey) {
 		return properties.getProperty(propertyNames[propertyKey], propertyValues[propertyKey]);
 	}
 	
 	private void loadPropertiesFile(String propertiesPath) throws ServerException {
 		
 		try {
 			FileInputStream is = new FileInputStream(propertiesPath);
 			try {
 				properties.load(is);
 			} finally {
 				is.close();
 			}
 			for(int i = 0; i < propertyNames.length; i++) {
 				if(getProperty(i) == null) {
 					throw new ServerException("Missing property " + propertyNames[i] + " in the property file " + propertiesPath);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			throw new ServerException("could not find properties file " + propertiesPath, e);
 		} catch (IOException e) {
 			throw new ServerException("could not read properties file " + propertiesPath, e);
 		}
 	}
 	
 	private void addFile(File file) throws RepositoryException {
 		Command command = new Command(ActionType.add, new String[] {file.getPath()}, null, new String[] {"m"});
 		this.repository.add(command);
 	}
 }
