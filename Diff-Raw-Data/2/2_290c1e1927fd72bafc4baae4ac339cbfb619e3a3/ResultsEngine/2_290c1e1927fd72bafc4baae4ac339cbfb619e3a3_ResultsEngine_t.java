 /**
  * Copyright (C) 2009 Mads Mohr Christensen, <hr.mohr@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.cubing.liveresults.uploader.engine;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ResourceBundle;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 
 import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
 import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
 import org.springframework.security.BadCredentialsException;
 
 import dk.cubing.liveresults.model.Competition;
 import dk.cubing.liveresults.uploader.LoggerWrapper;
 import dk.cubing.liveresults.uploader.LoggerWrapperFactory;
 import dk.cubing.liveresults.uploader.configuration.Configuration;
 import dk.cubing.liveresults.uploader.gui.MenuBar;
 import dk.cubing.liveresults.uploader.gui.PreferencesPanel;
 import dk.cubing.liveresults.uploader.gui.StatusPanel;
 import dk.cubing.liveresults.uploader.parser.ExcelParser;
 import dk.cubing.liveresults.uploader.parser.ResultsFileParser;
 import dk.cubing.liveresults.uploader.parser.ResultsFileParserException;
 import dk.cubing.liveresults.webservice.CompetitionNotFoundException;
 import dk.cubing.liveresults.webservice.CompetitionSaveException;
 import dk.cubing.liveresults.webservice.LiveResults;
 import dk.cubing.liveresults.webservice.UnsupportedClientVersionException;
 
 public class ResultsEngine implements Runnable {
 	
 	private static final LoggerWrapper log = LoggerWrapperFactory.getInstance().getLogger(ResultsEngine.class);
 	
 	public static ResourceBundle resources = ResourceBundle.getBundle("LiveResults");
 	
 	private final Configuration config;
 	private final FilesystemAlterationMonitor fam;
 	private ResultsFileChangeListener pListener = null;
 	private final ResultsFileParser parser;
 	private final JaxWsProxyFactoryBean factory;
 	private LiveResults client;
 	private String clientVersion;
 	private boolean isRunning = false;
 	private boolean isShuttingDown = false;
 	private JFrame frame = null;
 	private StatusPanel statusPanel = null;
 	
 	public ResultsEngine() {
 		config = new Configuration(this);
 		fam = new FilesystemAlterationMonitor();
 		factory = new JaxWsProxyFactoryBean();
 		parser = new ResultsFileParser();
 	}
 	
 	/**
 	 * @param parser
 	 */
 	public void setParser(ExcelParser parser) {
 		this.parser.setParser(parser);
 	}
 	
 	/**
 	 * @param clientVersion the clientVersion to set
 	 */
 	public void setClientVersion(String clientVersion) {
 		this.clientVersion = clientVersion;
 	}
 
 	/**
 	 * @return the clientVersion
 	 */
 	public String getClientVersion() {
 		return clientVersion;
 	}
 	
 	/**
 	 * @return the config
 	 */
 	public Configuration getConfig() {
 		return config;
 	}
 	
 	/**
 	 * @return the client
 	 */
 	public LiveResults getClient() throws Exception {
 		// lazy init and endpoint reloading
 		if (client == null || !getConfig().getWebserviceEndpoint().equals(factory.getAddress())) {
 			try {
 				factory.setServiceClass(LiveResults.class);
 				factory.setAddress(getConfig().getWebserviceEndpoint());
 				client = (LiveResults) factory.create();
 			} catch (Exception e) {
 				log.error("Could not create webservice client", e);
                 throw e;
 			}
 		}
 		return client;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	@Override
 	public void run() {
 		
 		// display swing gui?
 		if (getConfig().doShowGUI()) {
 			try {
 				javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
 				    public void run() {
 				    	createAndShowGUI();
 				    }
 				});
 			} catch (Exception e) {
 				log.error(e.getLocalizedMessage(), e);
 			}
 		}
 		
 		log.info("Starting ResultsEngine...");
 		setProgress(2);
 		if (isSupportedVersion()) {
 			if (!getConfig().isConfigured()) {
 				if (getConfig().doShowGUI()) {
 					createAndShowPreferencesDialog();
 				} else {
 					log.warn("ResultsEngine are not configured!");
 				}
 			}
 			while (!isRunning && !isShuttingDown) {
 				if (getConfig().isConfigured()) {
 					setProgress(4);
 					pListener = new ResultsFileChangeListener(this);
 					fam.addListener(getConfig().getResultsFile(), pListener);
 					fam.start();
 					isRunning = true;
 					log.info("Engine started.");
 					setProgress(6);
 				} else {
 					try {
 						Thread.sleep(5000);
 					} catch (InterruptedException e) {
 						log.error(e.getLocalizedMessage(), e);
 					}
 				}
 			}
 		}
 	}
 
     /**
      * @param resultsFile
      */
     public void uploadResults(File resultsFile) {
         uploadResults(resultsFile, false);
     }
 
 	/**
 	 * @param resultsFile
      * @param forceUpload
 	 */
 	public void uploadResults(File resultsFile, boolean forceUpload) {
 		setProgress(1);
 		
 		Competition competition = null;
 		
 		// load competition
 		try {
             if (getConfig().doAutoUpload() || forceUpload) {
                 log.info("Loading competition: {}", getConfig().getCompetitionId());
                 competition = getClient().loadCompetition(
                         getConfig().getCompetitionId(),
                         getConfig().getPassword());
                 setProgress(2);
                 log.info("Competition loaded.");
             } else {
                 log.info("Automatic upload disabled.");
                 competition = new Competition();
                 competition.setCompetitionId(getConfig().getCompetitionId());
             }
 		} catch (BadCredentialsException e) {
 			log.error("Invalid CompetitionId and/or Password. CompetitionId: '{}', Password: '{}'", getConfig().getCompetitionId(), getConfig().getPassword());
 		} catch (CompetitionNotFoundException e) {
 			log.error("Could not load competition: {}", getConfig().getCompetitionId(), e);
 		} catch (Exception e) {
             log.error(e.getLocalizedMessage(), e);
         }
 
         // parse results spreadsheet
 		if (competition != null) {
 			try {
 				log.info("Parsing results file: {}", resultsFile.getName());
 				competition = parser.parse(competition, resultsFile.getAbsolutePath());
 				setProgress(4);
                 log.info("Results file parsed.");
 
                 // upload results
                 try {
                     if (getConfig().doAutoUpload() || forceUpload) {
                         log.info("Saving results: {}", competition.getName());
                         getClient().saveCompetition(
                                 getConfig().getCompetitionId(),
                                 getConfig().getPassword(),
                                 competition);
                         setProgress(6);
                         log.info("Saved results.");
                     }
                 } catch (BadCredentialsException e) {
                     log.error("Invalid CompetitionId and/or Password. CompetitionId: '{}', Password: '{}'", getConfig().getCompetitionId(), getConfig().getPassword());
                 } catch (CompetitionNotFoundException e) {
                     log.error("Could not load competition: {}", getConfig().getCompetitionId(), e);
                 } catch (CompetitionSaveException e) {
                     log.error("Could not save competition: {}", getConfig().getCompetitionId(), e);
                 } catch (Exception e) {
                     log.error(e.getLocalizedMessage(), e);
                 }
             } catch (ResultsFileParserException e) {
                 log.error("Selected spreadsheet does not appear to be based on the WCA template!", e);
             } catch (IllegalStateException e) {
                 log.warn("Unexpected cell format.", e);
             }
         }
 	}
 
 	/**
 	 * Restart engine (config has been changed)
 	 */
 	public void restart() {
 		if (getConfig().isConfigured() && pListener != null) {
 			setProgress(2);
 			fam.removeListener(pListener);
 			setProgress(4);
 			fam.addListener(getConfig().getResultsFile(), pListener);
 			log.info("Engine restarted.");
 			setProgress(6);
 			uploadResults(getConfig().getResultsFile());
 		} else {
 			log.debug("Restart was not possible at this moment.");
 		}
 	}
 	
 	/**
 	 * Shutdown engine and file monitor if running
 	 */
 	public void shutdown() {
 		setProgress(3);
 		log.info("Shutting down ResultsEngine...");
 		isShuttingDown = true;
 		isRunning = false;
 		try {
 		    fam.stop();
 		// workaround for https://issues.apache.org/jira/browse/JCI-62
 		} catch (NullPointerException e) {}
 		log.info("ResultsEngine shutdown completed.");
 		setProgress(6);
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean isSupportedVersion() {
         if (!getConfig().doAutoUpload()) return true; // offline mode
 		boolean isSupported = false;
 		log.info("Checking for updates...");
 		try {
 			isSupported = getClient().isSupportedVersion(getClientVersion());
 		} catch (UnsupportedClientVersionException e) {
 			log.error("There is a new version available. Please upgrade.", e);
 		} catch (Exception e) {
			log.error("Could not init web client. This could be a connection problem. Endpoint: {}", getConfig().getWebserviceEndpoint(), e);
 		}
 		if (isSupported) {
 			log.info("No updates available.");
 		}
 		return isSupported;
 	}
 	
 	
 	/**
 	 * Create Swing GUI
 	 */
 	public void createAndShowGUI() {
 		frame = new JFrame("ResultsEngine");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		frame.setJMenuBar(new MenuBar(this));
 		
 		if (statusPanel == null) {
 			statusPanel = new StatusPanel();
 		}
 		frame.getContentPane().add(statusPanel);
 
 		frame.setSize(640, 480);
 		frame.setResizable(false);
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * Create preferences dialog
 	 */
 	public void createAndShowPreferencesDialog() {
 		JDialog dialog = new JDialog(frame, "Preferences");
 		dialog.add(new PreferencesPanel(dialog, this));
 		dialog.setResizable(false);
 		dialog.pack();
 		dialog.setVisible(true);
 	}
 	
 	/**
 	 * @param type
 	 * @param message
 	 */
 	public void appendGuiMessage(final String type, final String message) {
 		if (getConfig().doShowGUI() && statusPanel != null) {
 	    	statusPanel.appendGuiMessage(type, message);
 		}
 	}
 	
 	/**
 	 * @param type
 	 * @param message
 	 * @param throwable
 	 */
 	public void appendGuiMessage(final String type, final String message, final Throwable throwable) {
 		if (getConfig().doShowGUI() && statusPanel != null) {
 			if (throwable != null) {
 		        StringWriter wr = new StringWriter();
 			    throwable.printStackTrace(new PrintWriter(wr));
 		    	statusPanel.appendGuiMessage(type, message, wr.toString());
 			} else {
 		    	statusPanel.appendGuiMessage(type, message);
 			}
 		}
 	}
 	
 	/**
 	 * @param type
 	 * @param message
 	 */
 	public void showGuiAlert(final String type, final String message) {
 	    if (getConfig().doShowGUI() && statusPanel != null) {
 	    	statusPanel.showGuiAlert(type, message, message);
 	    }
 	}
 	
 	/**
 	 * @param type
 	 * @param message
 	 * @param throwable
 	 */
 	public void showGuiAlert(final String type, final String message, final Throwable throwable) {
 	    if (getConfig().doShowGUI() && statusPanel != null) {
 	    	if (throwable != null) {
 	    		StringWriter wr = new StringWriter();
 			    throwable.printStackTrace(new PrintWriter(wr));
 		    	statusPanel.showGuiAlert(type, message, wr.toString());
 	    	} else {
 	    		statusPanel.showGuiAlert(type, message);
 	    	}
 	    }
 	}
 
 	/**
 	 * @param i
 	 */
 	private void setProgress(final int i) {
 		if (getConfig().doShowGUI() && statusPanel != null) {
 			javax.swing.SwingUtilities.invokeLater(new Runnable() {
 		          public void run() {
 		        	  statusPanel.setProgress(i);
 		          }
 			});
 		}
 	}
 }
