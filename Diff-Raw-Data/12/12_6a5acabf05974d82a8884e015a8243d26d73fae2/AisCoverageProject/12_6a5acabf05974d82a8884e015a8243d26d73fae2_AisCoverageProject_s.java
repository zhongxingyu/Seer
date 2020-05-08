 package dk.dma.aiscoverage.project;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 import dk.dma.aiscoverage.calculator.AbstractCalculator;
 import dk.dma.aiscoverage.calculator.CoverageCalculator;
 import dk.dma.aiscoverage.calculator.DensityPlotCalculator;
 import dk.dma.aiscoverage.data.MessageHandler;
 import dk.dma.aiscoverage.data.Ship.ShipClass;
 import dk.dma.aiscoverage.event.AisEvent;
 import dk.frv.ais.message.ShipTypeCargo.ShipType;
 import dk.frv.ais.proprietary.DmaFactory;
 import dk.frv.ais.proprietary.GatehouseFactory;
 import dk.frv.ais.reader.AisReader;
 import dk.frv.ais.reader.AisStreamReader;
 import dk.frv.ais.reader.RoundRobinAisTcpReader;
 
 /**
  * 
  */
 public class AisCoverageProject implements Serializable {
 	private static final long serialVersionUID = 1L;
 	transient private static Logger LOG;
 	private int timeout = -1;
 	private List<AbstractCalculator> calculators = new ArrayList<AbstractCalculator>();
 	transient private List<AisReader> readers = new ArrayList<AisReader>();
 	transient private List<MessageHandler> messageHandlers = new ArrayList<MessageHandler>();
 	private List<String> readersText = new ArrayList<String>();
 	private Date starttime;
 	private Date endtime;
 	private boolean isRunning = false;
 	private boolean isDone = false;
 	private long messageCount = 0;
 	private boolean fromFile = false;
 	private int currentFile = 0;
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	public List<AbstractCalculator> getCalculators() {
 		return calculators;
 	}
 
 	public void addCalculator(AbstractCalculator calc) {
 		calculators.add(calc);
 	}
 
 	public AisCoverageProject() {
 
 	}
 
 	public void setFile(String filepath) {
 		AisReader reader = null;
 		try {
 			reader = new AisStreamReader(new FileInputStream(filepath));
 
 			fromFile = true;
 
 			// Register proprietary handlers (optional)
 			reader.addProprietaryFactory(new DmaFactory());
 			reader.addProprietaryFactory(new GatehouseFactory());
 			readers.add(reader);
 			readersText.add(filepath);
 
 			// Make handler instance
 			MessageHandler messageHandler = new MessageHandler(this,
 					"Unidentified");
 			messageHandlers.add(messageHandler);
 			// register message handler
 			reader.registerHandler(messageHandler);
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void addHostPort(String port, String defaultID) {
 		RoundRobinAisTcpReader reader = new RoundRobinAisTcpReader();
 		reader.setCommaseparatedHostPort(port);
 
 		// Register proprietary handlers (optional)
 		reader.addProprietaryFactory(new DmaFactory());
 		reader.addProprietaryFactory(new GatehouseFactory());
 
 		readers.add(reader);
 		readersText.add(port + " DefaultID: " + defaultID);
 
 		// Make handler instance
 		// We create multiple message handlers because we need a default id
 		// if bsmmsi isn't set
 		MessageHandler messageHandler = new MessageHandler(this, defaultID);
 		messageHandlers.add(messageHandler);
 		// register message handler
 		reader.registerHandler(messageHandler);
 
 		fromFile = false;
 
 	}
 
 	public void startAnalysis() throws FileNotFoundException,
 			InterruptedException {
 		DOMConfigurator.configure("log4j.xml");
 		LOG = Logger.getLogger(AisCoverageProject.class);
 		LOG.info("Starting AisCoverage");
 
 		if (readers.size() == 0) {
 			LOG.debug("Source missing");
 			return;
 		}
 
 		if (fromFile) {
 
			// Running files,

 			// single file
 			if (readers.size() == 1) {
 				readers.get(0).start();
 
 				// Listen for reader to stop
 				Thread t = new Thread(new Runnable() {
 					public void run() {
 						try {
 							started();
 							readers.get(0).join();
 							stopped();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 				});
 				t.start();
 			} else {
 				currentFile = 0;
 				final AisCoverageProject project = this;
 
 				// Listen for reader to stop
 				Thread t = new Thread(new Runnable() {
 					public void run() {
 						AisCoverageProject homeProject = project;
 						
 						started();
 						for (AisReader reader : readers) {
 							// start reader
 							reader.start();
 
 							try {
 								reader.join();
 								homeProject.increaseFileProcessed();
 
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 						}
 						stopped();
 					}
 
 				});
 				t.start();
 
 				// final AisCoverageProject project = this;
 				//
 				// // Listen for reader to stop
 				// Thread t = new Thread(new Runnable() {
 				// public void run() {
 				// int remaining = readers.size() -1;
 				// int current = -1;
 				//
 				// AisCoverageProject homeProject = project;
 				//
 				//
 				// System.out.println("Starting threads - a total of "
 				// + (remaining+1));
 				//
 				// started();
 				// while (remaining >= 5) {
 				// for (int i = 0; i < 5; i++) {
 				// current++;
 				// System.out.println("Started thread " + current);
 				// readers.get(current).start();
 				// System.out.println("Started!");
 				//
 				// }
 				// //Started the first 5 threads
 				// remaining = remaining - 5;
 				// homeProject.setCurrentFile(current);
 				//
 				// try {
 				// for (int j = 0; j < 5; j++) {
 				// System.out.println("Killing thread "
 				// + (current - j));
 				// readers.get(current - j).join();
 				// }
 				//
 				//
 				// } catch (InterruptedException e) {
 				// e.printStackTrace();
 				// }
 				// }
 				//
 				//
 				// //Have run all the threads possible dividable by 5
 				// System.out.println("Remaining threads: " + remaining);
 				// System.out.println("Last run thread: " + current);
 				// //Run the remainder
 				// homeProject.setCurrentFile(current);
 				//
 				// for (int i = 0; i <= remaining; i++) {
 				// current++;
 				// System.out.println("Started thread " + current);
 				// readers.get(current).start();
 				// System.out.println("Started!");
 				// }
 				// homeProject.setCurrentFile(current);
 				// try {
 				// for (int i = 0; i < remaining; i++) {
 				// System.out.println("Killing thread " + (current - i));
 				// readers.get(current-i).join();
 				// }
 				//
 				// } catch (InterruptedException e) {
 				// e.printStackTrace();
 				// }
 				//
 				// stopped();
 				// }
 				// });
 				// t.start();
 				//
 				//
 			}
 
 		} else {
 			for (AisReader reader : readers) {
 				// start reader
 				reader.start();
 			}
 
 			// Listen for reader to stop
 			Thread t = new Thread(new Runnable() {
 				public void run() {
 					try {
 						started();
 						for (int i = 0; i < readers.size(); i++) {
 							readers.get(i).join();
							System.out.println("Finished with reader " + i);
 						}
 
 						stopped();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			t.start();
 
 		}
 
 	}
 
 	@SuppressWarnings("deprecation")
 	public void stopAnalysis() {
 		for (AisReader reader : readers) {
 			reader.stop();
 		}
 	}
 
 	private void started() {
 		starttime = new Date();
 		this.isRunning = true;
 
 		AisEvent event = new AisEvent();
 		event.setEvent(AisEvent.Event.ANALYSIS_STARTED);
 		ProjectHandler.getInstance().broadcastEvent(event);
 	}
 
 	private void stopped() {
 		endtime = new Date();
 		this.isRunning = false;
 		this.isDone = true;
 
 		AisEvent event = new AisEvent();
 		event.setEvent(AisEvent.Event.ANALYSIS_STOPPED);
 		ProjectHandler.getInstance().broadcastEvent(event);
 	}
 
 	public boolean isDone() {
 		return isDone;
 	}
 
 	public int getTimeout() {
 		return timeout;
 	}
 
 	public void setTimeout(int timeout) {
 		this.timeout = timeout;
 	}
 
 	public Long getMessageCount() {
 		return messageCount;
 	}
 
 	public Long getRunningTime() {
 
 		if (starttime == null)
 			return -1L;
 		if (isRunning)
 			return (new Date().getTime() - starttime.getTime()) / 1000;
 		else
 			return (endtime.getTime() - starttime.getTime()) / 1000;
 	}
 
 	public void incrementMessageCount() {
 		this.messageCount++;
 	}
 
 	public CoverageCalculator getCoverageCalculator() {
 		for (AbstractCalculator abstractCalc : getCalculators()) {
 			if (abstractCalc instanceof CoverageCalculator)
 				return (CoverageCalculator) abstractCalc;
 		}
 		return null;
 	}
 
 	public DensityPlotCalculator getDensityPlotCalculator() {
 		for (AbstractCalculator abstractCalc : getCalculators()) {
 			if (abstractCalc instanceof DensityPlotCalculator)
 				return (DensityPlotCalculator) abstractCalc;
 		}
 		return null;
 	}
 
 	public String getDescription() {
 		CoverageCalculator covCal = getCoverageCalculator();
 		String result = "<html>";
 		result += "INPUT SOURCES<br/>";
 		for (String reader : readersText) {
 			result += " - " + reader + "<br/>";
 		}
 		result += "<br/>";
 		result += "SHIP CLASSES<br/>";
 		Collection<ShipClass> shipClasses = covCal.getAllowedShipClasses()
 				.values();
 		for (ShipClass shipClass : shipClasses) {
 			result += " - " + shipClass + "<br/>";
 		}
 		result += "<br/>";
 		result += "SHIP TYPES<br/>";
 		Collection<ShipType> shipTypes = covCal.getAllowedShipTypes().values();
 		if (covCal.getAllowedShipTypes().size() == 0) {
 			result += " - All <br/>";
 		} else {
 			for (ShipType shipType : shipTypes) {
 				result += " - " + shipType + "<br/>";
 			}
 		}
 
 		result += "</html>";
 		return result;
 	}
 
 	public synchronized void increaseFileProcessed() {
 		System.out.println("Currently at: " + currentFile);
 		currentFile++;
 //		this.currentFile = currentFile;
 	}
 
 	public int getCurrentFile() {
 		return currentFile;
 	}
 
 	public int getTotalFiles() {
 		return readers.size();
 	}
 
 }
