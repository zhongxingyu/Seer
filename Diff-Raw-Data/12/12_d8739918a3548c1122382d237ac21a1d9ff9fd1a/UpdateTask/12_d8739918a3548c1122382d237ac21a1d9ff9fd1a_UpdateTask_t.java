 package zisko.multicastor.program.model;
 
 import java.util.Map;
 import java.util.TimerTask;
 import java.util.Vector;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import zisko.multicastor.program.controller.Main;
 import zisko.multicastor.program.controller.MulticastController;
 import zisko.multicastor.program.controller.ViewController;
 import zisko.multicastor.program.data.MulticastData;
 import zisko.multicastor.program.interfaces.MulticastThreadSuper;
 
 public class UpdateTask extends TimerTask {
 	private ViewController viewController;
 	private Logger logger;
 	private Map<MulticastData, MulticastThreadSuper> mc_sender_l3;
 	private Map<MulticastData, MulticastThreadSuper> mc_sender_v6;
 	private Map<MulticastData, MulticastThreadSuper> mc_receiver_l3;
 	private Map<MulticastData, MulticastThreadSuper> mc_receiver_v6;
 	
 	//V1.5 [FH] edded that memory warning is only appearing once
 	private boolean memoryWarned = false;
 
 	public UpdateTask(Logger logger,
 			Map<MulticastData, MulticastThreadSuper> mcSenderL3,
 			Map<MulticastData, MulticastThreadSuper> mcReceiverL3,
 			ViewController viewController) {
 		super();
 		this.logger = logger;
 		mc_sender_l3 = mcSenderL3;
 		mc_receiver_l3 = mcReceiverL3;
 		this.viewController = viewController;
 	}
 
 	@Override
 	public void run() {
 		MulticastThreadSuper value = null;
 		long time1 = System.nanoTime();
 		Map<MulticastData, MulticastThreadSuper> v = null;
 		boolean memoryWarnedForLog = false; 
		Runtime rt= Runtime.getRuntime();
 		
 		// V1.5 [FH] Prüfung des Memories. Ob noch mehr als 10% frei sind
		if (!memoryWarned && rt.freeMemory()+ (rt.maxMemory()-rt.totalMemory()) 
				< rt.maxMemory()*0.1) {
 			logger
 					.warning("Your memory is about to expire.(<10% remaining)\n"
 							+ "Please be careful, save your files and "
 							+ "try to free memory with closing of sender/reciever or tabs.\n\n" +
									"Free Memory: " + rt.freeMemory()/(1024*1024) + "\n" +
									"Total Allocated Memory: " + rt.totalMemory()/(1024*1024) + "\n" +
									"Maximum Memory for JVM:  " + rt.maxMemory()/(1024*1024) );
 			memoryWarnedForLog = true;
 			this.memoryWarned = true;
 		}
 
 		//TODO [MH] tbr
 		for (int i = 0; i < 2; i++) {
 			switch (i) {
 			case 0:
 				v = mc_sender_l3;
 				break;
 			case 1:
 				v = mc_receiver_l3;
 				break;
 			}
 			for (Entry<MulticastData, MulticastThreadSuper> m : v.entrySet()) {
 				value = m.getValue();
 				if (value.getMultiCastData().isActive()) {
 					value.update();
 				}
 			}
 		}
 //		for (int i = 0; i < 2; i++) {
 //			switch (i) {
 //			case 0:
 //				v = mc_sender_v4;
 //				break;
 //			case 1:
 //				v = mc_sender_v6;
 //				break;
 //			}
 //			for (Entry<MulticastData, MulticastThreadSuper> m : v.entrySet()) {
 //				value = m.getValue();
 //				if (value.getMultiCastData().isActive()) {
 //					value.update();
 //				}
 //			}
 //		}
 		if (viewController != null) {
 			if (viewController.isInitFinished()) {
 				viewController.viewUpdate();
 			}
 		}
 		
 		//V1.5 [FH] added !MemoryWarning, because if we have a memory warning it is always taking longer
 		if (!memoryWarnedForLog && ((System.nanoTime() - time1) / 1000000) > 200) {
 			// System.out.println("Updatetime is rather long: " +
 			// ((System.nanoTime() - time1)/1000000) + " ms !!!!!!!!!!!!");
 			logger.log(Level.INFO, "Updatetime is rather long: "
 					+ ((System.nanoTime() - time1) / 1000000)
 					+ " ms !!!!!!!!!!!!");
 			if (((System.nanoTime() - time1) / 1000000) > 300)
 				if (viewController != null)
 					logger
 							.log(
 									Level.WARNING,
 									"Updating the user interface takes very long. Consider the help for more information.");
 		}
 
 	}
 
 }
