 package com.narrowtux.PistonChest;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yi.acru.bukkit.Lockette.Lockette;
 
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.model.Protection;
 
 public class PCMain extends JavaPlugin {
 	public static Logger log = Bukkit.getServer().getLogger();
 	public static PCMain instance;
 	private PCBlockListener blockListener = new PCBlockListener();
 	private PCServerListener serverListener = new PCServerListener();
 	public static boolean lockette = false;
 	public static boolean lwc = false;
 	public Configuration config;
 	
 	@Override
 	public void onDisable() {
 		sendDescription("disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		//This has to be done first!
 		checkForLibs();
		instance = this;
		File data = getDataFolder();
		if(!data.exists()){
			data.mkdir();
		}
 		//Init after that!
 		config = new Configuration();
 		registerEvents();
 		fakePluginEnables();
 		sendDescription("enabled");
 	}
 	
 	private void fakePluginEnables() {
 		String plugins[] = {"Lockette", "LWC"};
 		for(String name:plugins){
 			Plugin plugin = getServer().getPluginManager().getPlugin(name);
 			if(plugin!=null)
 			{
 				serverListener.onPluginEnable(new PluginEnableEvent(plugin));
 			}
 		}
 	}
 
 	private void registerEvents() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Normal, this);
 	}
 
 	public void sendDescription(String startup){
 		PluginDescriptionFile pdf = getDescription();
 		String authors = "";
 		for(String name: pdf.getAuthors()){
 			if(authors.length()>0){
 				authors+=", ";
 			}
 			authors+=name;
 		}
 		log.log(Level.INFO, "["+pdf.getName()+"] v"+pdf.getVersion()+" by ["+authors+"] "+startup+".");
 	}
 	
 	public static boolean isChestPublic(Block chest){
 		if(!instance.config.hasChestProtection()){
 			return true;
 		}
 		boolean flag = true;
 		if(lockette){
 			flag = !Lockette.isProtected(chest)&&flag;
 		}
 		if(lwc){
 			Protection prot = LWC.getInstance().findProtection(chest);
 			flag = prot==null&&flag;
 		}
 		return flag;
 	}
 	
 	private void checkForLibs() {
 		PluginManager pm = getServer().getPluginManager();
 		if(pm.getPlugin("NarrowtuxLib")==null){
 			try{
 				File toPut = new File("plugins/NarrowtuxLib.jar");
 				download(getServer().getLogger(), new URL("http://tetragaming.com/narrowtux/plugins/NarrowtuxLib.jar"), toPut);
 				pm.loadPlugin(toPut);
 				pm.enablePlugin(pm.getPlugin("NarrowtuxLib"));
 			} catch (Exception exception){
 				log.severe("[Showcase] could not load NarrowtuxLib, try again or install it manually.");
 				pm.disablePlugin(this);
 			}
 		}
 	}
 	
 	public static void download(Logger log, URL url, File file) throws IOException {
 	    if (!file.getParentFile().exists())
 	        file.getParentFile().mkdir();
 	    if (file.exists())
 	        file.delete();
 	    file.createNewFile();
 	    final int size = url.openConnection().getContentLength();
 	    log.info("Downloading " + file.getName() + " (" + size / 1024 + "kb) ...");
 	    final InputStream in = url.openStream();
 	    final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
 	    final byte[] buffer = new byte[1024];
 	    int len, downloaded = 0, msgs = 0;
 	    final long start = System.currentTimeMillis();
 	    while ((len = in.read(buffer)) >= 0) {
 	        out.write(buffer, 0, len);
 	        downloaded += len;
 	        if ((int)((System.currentTimeMillis() - start) / 500) > msgs) {
 	            log.info((int)((double)downloaded / (double)size * 100d) + "%");
 	            msgs++;
 	        }
 	    }
 	    in.close();
 	    out.close();
 	    log.info("Download finished");
 	}
 
 }
