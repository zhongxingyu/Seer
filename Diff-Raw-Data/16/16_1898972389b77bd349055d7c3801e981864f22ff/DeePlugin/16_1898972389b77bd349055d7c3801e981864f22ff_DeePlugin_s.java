 package mmrnmhrm.ui;
 
 import melnorme.lang.ui.LangPlugin;
 import mmrnmhrm.core.DeeCore;
 import mmrnmhrm.ui.text.DeeCodeScanner;
 import mmrnmhrm.ui.text.DeeDocumentProvider;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 
 import util.log.Logg;
 
 public class DeePlugin extends LangPlugin {
 
 	// Same id as the core, for now.
 	public static final String PLUGIN_ID = DeeCore.PLUGIN_ID;
 
 	/** Returns the plugin instance. */
 	public static DeePlugin getInstance() {
 		return (DeePlugin) pluginInstance;
 	}
 	
 	public DeePlugin() {
 		pluginInstance = this;
 	}
 
	private DeeDocumentProvider deeDocumentProvider;
	private DeeCodeScanner defaultDeeCodeScanner;
 	
 	
 	public void initPlugin() throws CoreException {
 		Logg.main.println(" =============  Mmrnmhrm INITIALIZING  ============= " );
 		Logg.main.println("Location: " + Platform.getLocation());
 		Logg.main.println("Instance Location: " + Platform.getInstanceLocation().getURL());
 
 		new DeeCore(); // Instance DeeCoret while UI and core isn't separated.
 
 		deeDocumentProvider = new DeeDocumentProvider();
		defaultDeeCodeScanner = new DeeCodeScanner();

 	}
 
 	public static DeeDocumentProvider getDeeDocumentProvider() {
		return getInstance().deeDocumentProvider;
 	}
 	
 	public static DeeCodeScanner getDefaultDeeCodeScanner() {
		return getInstance().defaultDeeCodeScanner;
 	}
 }
