 package start;
 
 import java.io.IOException;
 import java.net.URL;

 import logging.LogPc;

 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;

 import data.impl.FileStorage;
 import data.interfaces.IDataStorage;
 
 public class ResultsGrabber {
 
 	private static Logger log = LogPc.Pclog;
 	public static int playersTimeOut = 1000; // time out between reading players
												// data from the web
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		logConfigure();
 		log.info("Starting...");
 		IDataStorage storage = new FileStorage();
 		IDataStorage storedNames = new FileStorage();
		storage.setStringConnection("e:\\dataOut.txt");
		storedNames.setStringConnection("e:\\GrabberFiles\\PlayersList.txt");
 		Grabber g = new Grabber();
 
 		try {
 			int i = 0;
			for (URL u : g.createURLList("e:\\GrabberFiles\\PlayersList.txt")) {
 				System.out.println("Processing " + u.toString());
 				log.info("Processing " + u.toString());
 				g.processDownload(u);
				storage.setStringConnection("e:\\GrabberFiles\\PlayersResults"
 						+ i + ".txt");
 				storage.Connect();
 				g.writeData(storage);
 				i++;
 				if (Thread.currentThread() != null) {
 					try {
 						Thread.sleep(playersTimeOut);
 						log.info("Thread stopped.");
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		log.info("Finished.");
 		// g.processDownload();
 		// g.writeData(storage);
 
 	}
 
 	private static void logConfigure(){
 URL ul = ResultsGrabber.class.getResource("../log4j.properties");
 		
 		PropertyConfigurator.configure(ul);
 
 	}
 }
