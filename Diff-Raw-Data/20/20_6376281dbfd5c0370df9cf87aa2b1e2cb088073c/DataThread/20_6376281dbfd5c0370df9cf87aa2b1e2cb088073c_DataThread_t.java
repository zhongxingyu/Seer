 package edu.uci.lighthouse.core.data;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.Status;
 import org.osgi.framework.BundleContext;
 
 import edu.uci.lighthouse.core.dbactions.DatabaseActionsBuffer;
 import edu.uci.lighthouse.core.dbactions.IDatabaseAction;
 import edu.uci.lighthouse.core.dbactions.pull.FetchNewDataAction;
 import edu.uci.lighthouse.core.listeners.IPluginListener;
 import edu.uci.lighthouse.core.widgets.StatusWidget;
 import edu.uci.lighthouse.model.jpa.JPAException;
 
 
 /**
  * Fetched new data from the database between specific time intervals defined by TIMEOUT constant.
  * @author tproenca
  *
  */
 public class DataThread extends Thread implements IPluginListener{
 
 	private final long TIMEOUT = 5000;
 	private final int MAX_MULTIPLIER = 180; // 180 times
 	private int backoffMultiplier = 1;
 	private boolean running = false;
 	private boolean suspended = false;
 	private DatabaseActionsBuffer buffer;
 	
 	private static Logger logger = Logger.getLogger(DataThread.class);
 	
 	public DataThread(DatabaseActionsBuffer buffer) {
 		super(DataThread.class.getName());
 		this.buffer = buffer;
 	}
 
 	@Override
 	public void run() {
 		StatusWidget.getInstance().setStatus(Status.OK_STATUS);
 		while (running) {
 			try {
 				if (suspended) {
 					synchronized (this) {
 						while (suspended) {
 							wait();
 						}
 					}
 				}
 				
 				processBuffer();
 
 				long adjustedTimeout = TIMEOUT * backoffMultiplier;
 				if (backoffMultiplier > 1) {
 					logger.debug("Next thread iteration in "
 							+ (adjustedTimeout / 1000) + "s");
 				}
 
 				sleep(adjustedTimeout);
 			} catch (InterruptedException e) {
 				logger.error(e);
 			} 
 		}	
 	}
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 		logger.info("Starting thread...");
 		running = true;
 		this.start();
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		logger.info("Stopping thread...");
 		running = false;
 	}
 	
 	private void processBuffer() {
 		try {
 			logger.debug("Executing "+buffer.size()+" database actions.");
 			while (!buffer.isEmpty()) {
 				IDatabaseAction databaseAction = buffer.peek();
 				databaseAction.run();
 				buffer.poll();
 			}
 			buffer.offer(new FetchNewDataAction());
 			backoffMultiplier = 1;
 		} catch (JPAException e) {
 			backoffMultiplier = backoffMultiplier > MAX_MULTIPLIER ? 1 : backoffMultiplier*2; 
 			logger.error(e, e);
 		}
 		// TODO (tproenca): Verify the connection exception and change the icon in the UI.
 		/*catch () {
 						}*/
 	}
 	
 	public synchronized void pause() {
 		suspended = true;
 		StatusWidget.getInstance().setStatus(Status.CANCEL_STATUS);
 		interrupt();
 	}
 	
 	public synchronized void play() {
 		suspended = false;
 		StatusWidget.getInstance().setStatus(Status.OK_STATUS);
 		notify();
 	}
 }
