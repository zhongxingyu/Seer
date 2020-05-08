 package scripting;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.HashMap;
 import java.util.Map;
 
 import ui.composites.OptionComposite;
 import ui.composites.ScriptComposite;
 import static java.nio.file.StandardWatchEventKinds.*;
 
 public final class ScriptWatcher implements Runnable {
 	
 	private WatchService watcher;
 	private Map<WatchKey, Path> keys  = new HashMap<WatchKey, Path>();
 
 	public ScriptWatcher() {
 		File f = new File("./scripts/");
 		if(!f.isDirectory()) { f.mkdirs(); }
 		try {
 			this.watcher = FileSystems.getDefault().newWatchService();
 			register(Paths.get("./scripts/"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void register(Path dir) throws IOException {
 		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
 				ENTRY_MODIFY);
 		keys.put(key, dir);
 
 		for (File f : dir.toFile().listFiles()) {
 			if (f.isDirectory() || !f.toString().contains(".js"))
 				continue;
 			ScriptManager.addScript(f);
 		}
 	}
 
 	private void updateFile(WatchEvent<?> event, File file) {
 		WatchEvent.Kind<?> eventType = event.kind();
 				
 		if (eventType == ENTRY_MODIFY) {
 			ScriptManager.modifyScript(file);
 
 		} else if (eventType == ENTRY_CREATE) {
 			ScriptManager.addScript(file);
 
 		} else if (eventType == ENTRY_DELETE) {
 			ScriptManager.removeScript(file);
 
 		}
 		
 		if(OptionComposite.getCurrentComposite() instanceof ScriptComposite){
 			ScriptComposite c = (ScriptComposite) OptionComposite.getCurrentComposite();
 			c.updateTreeItems();
 		}
 	}
 	
 	@Override
 	public void run() {
 		while(true) {
 			WatchKey key = null;
 			try {
 				key = watcher.take();
 			} catch (InterruptedException x) {
 				x.printStackTrace();
 			}
 
 			Path dir = keys.get(key);
 
 			if (dir == null) {
 				System.err.println("Unrecognized key: " + key);
 				continue;
 			}
 
 			for (WatchEvent<?> event : key.pollEvents()) {
 
 				@SuppressWarnings("unchecked")
 				WatchEvent<Path> ev = (WatchEvent<Path>) event;
 				Path name = ev.context();
 				Path child = dir.resolve(name);
 				File f = child.toFile();
 				updateFile(event, f);
 			}
 			key.reset();
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}

 }
