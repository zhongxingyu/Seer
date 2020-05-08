 package ghosty;
 
 import java.io.IOException;
 import java.nio.file.FileSystems;
 
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.List;
 import java.util.Scanner;
 import static java.nio.file.StandardWatchEventKinds.*;
 
 import ghosty.config.Configuration;
 import ghosty.files.Directory;
 import ghosty.utils.Parameters;
 import ghosty.utils.ParametersManager;
 
 import javax.swing.SwingUtilities;
 
 import com.google.api.services.drive.model.File;
 
 /**
  * Main class of the project Ghost
  * 
  * Type of calls:
  *   - "java ghosty --no-gui --reconfigure"
  *
  */
 public class Main {
 
 	public static ParametersManager pm;
 	
 	public static boolean hasGui = false;
 	
 	public static String baseConfigDirectory;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		pm = new ParametersManager(args);
 		hasGui = pm.has(Parameters.GUI);
 		baseConfigDirectory = System.getProperty("user.home") + System.getProperty("file.separator") + ".Ghosty";
 		
 		if(hasGui) {
 			Main.loadGui();
 		} else {
 			Main.loadShell();
 		}
 	    
 		
 	}
 	
 
 	public static void loadGui() {
 
 	    // We must call the window later to avoid problems with events
 	    // http://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html
 	    SwingUtilities.invokeLater(new Runnable() {
 	    	public void run() {
 	    		GhostyWindow W = new GhostyWindow();
 	    	}
     	});
 	}
 	
 
 	public static void loadShell() {
 
 		System.out.println("***********************************");
 		System.out.println("*        Welcome to Ghosty !      *");
 		System.out.println("***********************************");
 		
 		
 		Directory.createDirectory(baseConfigDirectory);
 		String lastDirectory = null;
 		String directory = "";
 		try {
 			Configuration baseConfig = new Configuration(baseConfigDirectory +  System.getProperty("file.separator") + "base.xml");
 			if (baseConfig.exists() && baseConfig.has("last_directory")) {
 
 				lastDirectory = baseConfig.get("last_directory");
 			}
 			Scanner in = new Scanner(System.in);
 			do {
 				System.out.print("Choisissez un dossier");
 				if(lastDirectory != null) {
 					System.out.print(" (Laissez vide pour \"" + lastDirectory + "\") ");
 				}
 				System.out.println(": ");
 
 
 				directory = in.nextLine();
 
 				if(directory.equals("") && lastDirectory != null) {
 					directory = lastDirectory;
 				}
 
 			} while (directory == null);
 
 			baseConfig.set("last_directory", directory);
 
 
 			baseConfig.save();
 
 		} catch (Exception e) {}
 		
 		
 		// Watcher
		System.out.println("Pour arrter ce programme, tapez CTRL+C.");
 		launchWatcher(Paths.get(directory));
 
 
 		// If configuration missing or reconfigure enabled, we must load the configuration process
 		
 	}
 
 
 	private static void launchWatcher(Path dir) {
 
 		for (;;) {
 
 			try {
 				WatchService watcher = dir.getFileSystem().newWatchService();
 				dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
 
 				WatchKey watckKey = watcher.take();
 
 				List<WatchEvent<?>> events = watckKey.pollEvents();
 				
 				// TODO : added action on events (replace entry)
 				for (WatchEvent event : events) {
 					if (event.kind() == OVERFLOW) {
 						continue;
 					}
 
 					if (event.kind() == ENTRY_CREATE) {
 						System.out.println("Created: "
 								+ event.context().toString());
 					}
 					if (event.kind() == ENTRY_DELETE) {
 						System.out.println("Delete: "
 								+ event.context().toString());
 					}
 					if (event.kind() == ENTRY_MODIFY) {
 						System.out.println("Modify: "
 								+ event.context().toString());
 					}
 
 				}
 				boolean valid = watckKey.reset();
 				if (!valid) {
 					System.out
 							.println("A critical error appears: the directory is not enough accessible");
 					break;
 				}
 
 			} catch (Exception e) {
 				System.out.println("Error: " + e.toString());
 			}
 		}
 
 	}
 	
 	
 
 }
