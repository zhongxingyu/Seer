 package fi.dy.esav.AnnounceUpdated;
 
 import java.io.File;
 import java.util.HashMap;
 
 public class FilesWatch implements Runnable {
 
 	AnnounceUpdated plugin;
 	
 	public FilesWatch(AnnounceUpdated plugin) {
 		this.plugin = plugin;
 	}
 	
 	boolean running = true;
 	
 	@Override
 	public void run() {
 		HashMap<String, Long> files = new HashMap<String, Long>();
 		boolean firstrun = true;
 		
 		while(running) {
 			if(files.size() > 0) {
 				firstrun = false;
 			}
 			File folder = new File("plugins/");
 			for(File file : folder.listFiles()) {
 				if(!files.containsKey(file.getAbsolutePath())) {
 					if(!firstrun) {
 						plugin.getServer().broadcastMessage("[UA] New file found: " + file.getName());
 					}
 					files.put(file.getAbsolutePath(), file.lastModified());
 				} else if(file.lastModified() > files.get(file.getAbsolutePath())) {
 					plugin.getServer().broadcastMessage("[UA] File updated: " + file.getName());
 					files.put(file.getAbsolutePath(), file.lastModified());
 				}
 				
 				//System.out.println(file.getAbsolutePath());
 			}
 			
 			try {
				Thread.sleep(5000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void stop() {
 		running = false;
 	}
 
 }
