 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 
 public class ChestSync extends Plugin{
 	public final Logger log = Logger.getLogger("Minecraft");
 	
 	private String name = "ChestSync";
	protected String version = "2.0";
 	protected String CurrVer = "2.0";
 	private String author = "Darkdiplomat";
 	
 	private ChestSyncListener csl;
 	private CSData csd = new CSData(this);
 
 	public void disable() {
 		csd.callSave(this);
 		log.info(name + " version " + version + " disabled");
 
 	}
 
 	public void enable() {
 		log.info(name + " version " + version + " by " + author + " enabled!");
 		if(!isLatest()){
 			log.info("[ChestSync] - There is an update available! Current = " + CurrVer);
 		}
 	}
 	
 	public void initialize(){
 		csl = new ChestSyncListener(this, csd);
 		etc.getLoader().addListener(PluginLoader.Hook.OPEN_INVENTORY, csl, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.CLOSE_INVENTORY, csl, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, csl, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, csl, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_BROKEN, csl, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_RIGHTCLICKED, csl, this, PluginListener.Priority.MEDIUM);
 	}
 	
 	public boolean isLatest(){
 		String address = "http://www.visualillusionsent.net/cmod_plugins/Versions.html";
 		URL url = null;
 		try {
 			url = new URL(address);
 		} catch (MalformedURLException e) {
 			return true;
 		}
 		String[] Vpre = new String[1]; 
 		BufferedReader in;
 		try {
 			in = new BufferedReader(new InputStreamReader(url.openStream()));
 			String inputLine;
 			while ((inputLine = in.readLine()) != null) {
 				if (inputLine.contains("ChestSync=")){
 					Vpre = inputLine.split("=");
 					CurrVer = Vpre[1].replace("</p>", "");
 				}
 			}
 			in.close();
 		} catch (IOException e) {
 			return true;
 		}
 		return (version.equals(CurrVer));
 	}
 }
