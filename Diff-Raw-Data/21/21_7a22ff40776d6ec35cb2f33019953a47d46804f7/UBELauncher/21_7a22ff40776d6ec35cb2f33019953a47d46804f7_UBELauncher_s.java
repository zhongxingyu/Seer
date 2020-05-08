 package ube.core.task;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.condition.Condition;
 import org.eclipse.osgi.launch.Equinox;
 import org.osgi.framework.BundleException;
 
 public class UBELauncher extends Task {
 
 	private final List<Condition> conditions = new ArrayList<Condition>();
 
 	public void add(Condition c) {
 		conditions.add(c);
 	}
 
 	private static Equinox osgiEngine = null;
 
	private Boolean shutdown = Boolean.FALSE;
 
	public void setShutdown(Boolean makeShutdown) throws BundleException,
 			InterruptedException {
		if (makeShutdown == null || Boolean.TRUE.equals(makeShutdown)) {
 			this.shutdown = Boolean.TRUE;
 		}
 
 	}
 
 	@Override
 	public void execute() {
 
 		if (!checkPreConditions()) {
 			System.out.println("Conditions of Task does not satisfied.");
 		}
 
 		// Control Start/Shutdown of Felix
 		if (Boolean.TRUE.equals(this.shutdown)) {
 
 			// shutdownFelixEngine();
 			shutdownOSGIEngine();
 
		} else {
 
 			// startFelixEngine();
 			startOSGIEngine();
 
 		}
 
 	}
 
 	private Boolean checkPreConditions() {
 		boolean areTrue = true;
 		for (Iterator<Condition> iterator = conditions.iterator(); iterator
 				.hasNext();) {
 			Condition condition = iterator.next();
 			areTrue = areTrue && condition.eval();
 			System.out.println("Condition:" + condition.eval());
 		}
 		// never null because autoboxing of primitive date type
 		return areTrue;
 	}
 
 	// private void shutdownFelixEngine() {
 	// System.out.println("Try to shutdown the Engine, state is "
 	// + FelixEngineSingleton.getFelixLauncher().getEngineStatus());
 	// try {
 	// FelixEngineSingleton.getFelixLauncher().shutdownApplication();
 	// } catch (BundleException e) {
 	// // TODO Auto-generated catch block
 	//
 	// e.printStackTrace();
 	// } catch (InterruptedException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	// System.out.println("Shutdown has finished in state "
 	// + FelixEngineSingleton.getFelixLauncher().getEngineStatus());
 	// }
 
 	// private void startFelixEngine() {
 	// // Launch the UBE System
 	// System.out.println("UBE is ready to starting Felix.");
 	// // Start Felix
 	// FelixEngineSingleton.getFelixLauncher();
 	// // Read the ube build files
 	// System.out.println("Felix has started Felix by UBE.");
 	// }
 
 	private void startOSGIEngine() {
 
 		System.out.println("UBE is ready to starting OSGI engine.");
 		final Map map = new HashMap();
 		osgiEngine = new Equinox(map);
 		try {
 			osgiEngine.init();
 			osgiEngine.start();
 
 		} catch (BundleException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("OSGI engine has been started by UBE.");
 
 	}
 
 	private void shutdownOSGIEngine() {
 		System.out.println("UBE is trying to stop the OSGI engine.");
 		try {
 			osgiEngine.stop();
 		} catch (BundleException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("OSGI engine has been stopped by UBE.");
 	}
 
 	public static void main(String[] args) {
 		UBELauncher launcher = new UBELauncher();
 		launcher.execute();
 	}
 
 }
