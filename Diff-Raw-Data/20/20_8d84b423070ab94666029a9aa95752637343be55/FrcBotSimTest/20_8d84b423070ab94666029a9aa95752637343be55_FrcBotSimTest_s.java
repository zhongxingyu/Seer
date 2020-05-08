 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package frcbotsimtest;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import java.util.logging.StreamHandler;
 import javax.swing.SwingUtilities;
 import net.sourceforge.frcsimulator.gui.SimulatorControlFrame;
 import net.sourceforge.frcsimulator.mistware.Simulator;
 
 /**
  *
  * @author wolf
  */
 public class FrcBotSimTest {
 	public static final int E_NONE = 0, E_BADARGS = 2, E_SIMFAIL = 5;
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
		String testCase = "FRCBotRobotBase";
 		boolean gui = false;
 		// Process arguments
 		if (args.length > 0) {
 			if (args[0].startsWith("-")) { // It's a flag
 				if ("--gui".equals(args[0])) {
 					gui=true;
 				} else {
 					System.err.println("Invalid flag: "+args[0]);
 					System.exit(E_BADARGS); // Bad arguments
 				}
 			} else { // It's a class
 				testCase = args[0];
 			}
 		}
 		if (args.length == 2) {
 			testCase = args[1];
 		} else if (args.length > 2) {
 			System.err.println("Too many arguments!");
 			System.exit(E_BADARGS); // Bad arguments
 		}

 		if (gui) {
 			simulatorGui(testCase);
 		} else {
 			simulatorCli(testCase);
 		}
 	}
 	public static void simulatorCli(String testCase) {
 		Simulator simulator;
 		try {
 			simulator = new Simulator(testCase);
 		} catch (ClassNotFoundException ex) {
 			System.err.println("The simulator couldn't find the given class!");
 			ex.printStackTrace();
 			System.exit(E_BADARGS); // Bad arguments
 			return; // Needed so NetBeans won't complain about simulator being unitinitalized.
 		}
 		simulator.getLogger().addHandler(new StreamHandler(System.out,new SimpleFormatter()));
 		try {
 			simulator.onStatusChange(FrcBotSimTest.class.getMethod("simStateChangeCli", Simulator.Status.class, Simulator.Status.class));
 		} catch (Exception e) {
 			System.err.println("Oops, couldn't add a status change hook to the simulator.");
 			e.printStackTrace();
 		}
 		simulator.start();
 		try {
 			simulator.join();
 		} catch (InterruptedException e) {
 			System.err.println("Interrupted while join()ing simulator:");
 			e.printStackTrace();
 		}
 		if (simulator.getStatus()==Simulator.Status.ERROR) {
 			System.exit(E_SIMFAIL); // Abnormal simulator failure
 		}
 		System.exit(E_NONE);
 	}
 	public static void simStateChangeCli(Simulator.Status status, Simulator.Status oldStatus) {
 		System.out.println("Simulator status: "+status.toString());
 	}
 
 	private static void simulatorGui(final String testCase) {
 		final Object lock = new Object();
 		synchronized(lock) {
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					SimulatorControlFrame simulatorControlFrame = new SimulatorControlFrame(testCase);
 			synchronized (lock) {
 			lock.notify();
 			}
 				}
 			});
 			try {
                 lock.wait();
             } catch (InterruptedException ex) {
                 System.exit(2);
             }
 		}
 	}
 }
