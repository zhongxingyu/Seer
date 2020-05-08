 package objective;
 
 import objective.ng.GotoMethodServer;
 
 import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.IStartup;
 
 @SuppressWarnings("restriction")
 public class Startup implements IStartup {
 
 	@Override
 	public void earlyStartup() {
 		
 		JavaPlugin javaPlugin = JavaPlugin.getDefault();
 		IPreferenceStore preferences = javaPlugin.getPreferenceStore(); 
 		preferences.setValue("escapeStrings", true);
 		
 		configureMapEntryFormatters();
 		
 		startGotoMethodServer();
 	}
 
 	private void configureMapEntryFormatters() {
 		IPreferenceStore debugPrefs = JDIDebugUIPlugin.getDefault().getPreferenceStore();
 		
 		debugPrefs.setValue("org.eclipse.jdt.debug.ui.show_details","INLINE_FORMATTERS");
 		String previousPref = debugPrefs.getString("org.eclipse.jdt.debug.ui.detail_formatters");
		String hashmapDetail = "java.util.HashMap$Entry,getKey()+\\\"\\\\=\\\"+getValue(),1";
 		
 		previousPref = previousPref.replace(hashmapDetail, "");
 		
 		debugPrefs.setValue("org.eclipse.jdt.debug.ui.detail_formatters", 
 				previousPref+"," +
 				hashmapDetail);
 	}
 
 	private void startGotoMethodServer() {
 		new GotoMethodServer().start();
 		
 	}
 }
