 package plugins;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 
 import job.UsageImportJob;
 import play.Logger;
 import play.Play;
 import play.PlayPlugin;
 import play.cache.Cache;
 import play.db.jpa.JPAPlugin;
 import play.jobs.Job;
 import play.templates.Template;
 import play.templates.TemplateLoader;
 import play.vfs.VirtualFile;
 import util.Utils;
 import bot.BotListener;
 import bot.BotListener.Config;
 
 public class BootstrapPlugin extends PlayPlugin {
 
 	Boolean terminated = false;
 	
 	/**
 	 * Add a runtime shutdown hook, to clean exit on hard kill
 	 */
 	@Override
 	public void onApplicationStart() { 
 		
 		/*
 		 * disable JPA auto transaction handling
 		 */
 		JPAPlugin.autoTxs = false;
 		
 		/* 
 		 * check if database already exists
 		 */
 		createDatabaseIfNotExists();
 		
 		/*
 		 * add runtime shutdown hook to intercept hard application killing 
 		 */
 		Runtime.getRuntime().addShutdownHook( new Thread() {
 			@Override
 			public void run() {
 				/* if not yet invoked the application stop() method, do now */
 				Logger.debug(">> Invoking shutdown hook");
 				Play.stop();
 			}
 		});
 
 	}
 	
 	private void createDatabaseIfNotExists() {
 		String strategy = Play.configuration.getProperty("jpa.ddl");
 		Logger.debug(">> Database creation strategy: %s", strategy);
 		if( !"create-if-not-exists".equals(strategy) ) { 
 			return;
 		}
 		
 		String url = Play.configuration.getProperty("db.url");
 		
 		File db = getDatabaseFileFromUrl(url);
 		if( db == null ) { 
 			Play.configuration.remove("jpa.ddl");
 			return;
 		}
 		
 		String mode = db.exists() ? "none" : "create";
 		Logger.info(">> Setting jpa.ddl=%s", mode);
 		Play.configuration.setProperty("jpa.ddl", mode);
 		
 	}
 
 	static File getDatabaseFileFromUrl( String url ) { 
 
 		if( Utils.isEmpty(url)) { 
 			Logger.debug(">> Nothing to do on database url is empty!");
 			return null;
 		}
 		
 		if( !url.startsWith("jdbc:h2:")) { 
 			Logger.debug("Wrong database url. It should be a valid H2 url (starting with 'jdbc:h2:')");
 			return null;
 		}
 		
 		if( url.startsWith("jdbc:h2:mem:")) { 
 			Logger.debug(">> Nothing to do on in-memory database");
 			return null;
 		}
 	
 		
 		int p=url.lastIndexOf(":");
 		if( p==-1 ) { 
 			Logger.warn(">> Unrecognized db.url format: %s", url);
 			return null;
 		}
 		
 		String fileName = url.substring(p+1);
 		p = fileName.indexOf(";");
 		if( p != -1 ) { 
 			fileName = fileName.substring(0,p);
 		}
 		
 		Logger.debug(">> Database file: %s", fileName);
 		return new File(fileName+".h2.db");
 	}
 	
 	
 	/**
 	 * Notify application starts event
 	 */
 	@Override
     public void afterApplicationStart() {
 		
 		/* 
 		 * add current timestamp 
 		 */
 		Cache.set("server-start-time", System.currentTimeMillis());
 		
 		/* 
 		 * Import the usage file 
 		 */
 		importUsageFile();
 		
 		/* 
 		 * installing a mail listener if it is configured 
 		 */
 		startMailListener();
 	}
 
 	private void importUsageFile() {
 		/* 
 		 * check if is there a file to import
 		 */
 		String fFile = Play.configuration.getProperty("settings.import.usage.file");
 		if( fFile == null ) return;
 		
 		File fileToImport = new File(fFile);
 		if( !fileToImport.exists() ) { 
 			Logger.warn("Import file does not exist: '%s'", fileToImport);
 			return;
 		}
 
 		UsageImportJob job = new UsageImportJob(fileToImport);
 		job.now();
 		
 	}
 
 	private void startMailListener() {
 
 		Config config = BotListener.getConfig();
 		if( config!=null && config.isActive() ) { 
 			Logger.info(">>> Installing mail listener");
 
 			final BotListener receiver = new BotListener() {
 				public boolean isTerminated() { return terminated; }
 			};
 			
 			Job starter = new Job() {
 				public void doJob() throws Exception { receiver.run(); };
 			};
 			
 			starter.in(config.delay);
 			
 		}
 		else { 
 			Logger.info(">>> Mail listener service NOT configured");
 		}
 
 
 	}
 
 	
 	/**
 	 * Notify application stops 
 	 */
 	@Override
     public void onApplicationStop() {
 		if( terminated ) { 
 			return;
 		}
 		
 		Logger.info(">>> Stopping server");
 		terminated = true;
 		
 		/* 
 		 * Shutdown H2 database 
 		 */
 		try {
 			Connection conn = DriverManager.getConnection(Play.configuration.getProperty("db.url"));
             Statement stat = conn.createStatement();
             stat.execute("SHUTDOWN");
             stat.close();
             conn.close();
 		} 
 		catch( Exception e ) { 
 			Logger.warn(e, "Error shutting down H2 database");
 		}
 	}
 
 	
 	/**
 	 * Load "/conf" provided templates
 	 */
 	@Override
     public Template loadTemplate(VirtualFile file) {
 
 		/* 
 		 * interpect templates in the /conf path 
 		 */
 		if( !file.relativePath().startsWith("/conf/") ) { 
 			return null;
 		}
 
		Logger.debug("Laoading template: '%s'",  file.relativePath() );
         String key = (file.relativePath().hashCode() + "").replace("-", "M");
         return TemplateLoader.load( key, file.contentAsString() );
     }
 
 	
 	
 }
