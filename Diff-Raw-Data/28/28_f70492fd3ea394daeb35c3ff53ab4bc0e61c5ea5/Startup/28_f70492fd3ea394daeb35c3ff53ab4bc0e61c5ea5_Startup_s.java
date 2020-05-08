 package objective;
 
 import objective.ng.GotoMethodServer;
 
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.IStartup;
 
 @SuppressWarnings("restriction")
 public class Startup implements IStartup {
 
	@SuppressWarnings("deprecation")
 	@Override
 	public void earlyStartup() {
 		
 		JavaPlugin javaPlugin = JavaPlugin.getDefault();
 		IPreferenceStore preferences = javaPlugin.getPreferenceStore(); 
 		preferences.setValue("escapeStrings", true);
 		
 		startGotoMethodServer();
 	}
 
 	private void startGotoMethodServer() {
 		new GotoMethodServer().start();
 		
 	}
 }
