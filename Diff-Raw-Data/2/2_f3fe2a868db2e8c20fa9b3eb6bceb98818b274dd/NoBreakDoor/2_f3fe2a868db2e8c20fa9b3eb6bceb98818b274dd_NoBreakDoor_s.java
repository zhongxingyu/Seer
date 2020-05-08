 package syam.NoBreakDoor;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 @SuppressWarnings("unused")
 public class NoBreakDoor extends JavaPlugin{
 	public final static Logger log = Logger.getLogger("Minecraft");
 	public final static String logPrefix = "[NoBreakDoor] ";
 	private final NoBreakDoorEntityListener entityListener = new NoBreakDoorEntityListener(this);
 
 	private static NoBreakDoor instance;
 
 	// 設定関係
 	public static List<String> ignoreWorlds = new ArrayList<String>();
 	public static Boolean verbose = new Boolean(false);
 
 	public void onDisable(){
 		PluginDescriptionFile pdfFile=this.getDescription();
 		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" by syam is disabled!");
 	}
 	public void onEnable(){
 		instance = this;
 
 		loadConfig();
 
 		// イベントを登録
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(entityListener, this);
 
 		// メッセージ表示
 		PluginDescriptionFile pdfFile=this.getDescription();
 		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" by syam is enabled!")
 	}
 
 	/*
 	 * SakuraServerプラグインのための設定ファイルを読み込む
 	 */
 	private void loadConfig()
 	{
 		//設定ファイル読み込み
 		String filename = getDataFolder() + System.getProperty("file.separator") + "config.yml";
 		File file = new File(filename);
 
 		if (!file.exists()){
 			if (!newConfig(file)){
				throw new IllegalArgumentException("新しい設定ファイルを作れませんでした");
 			}
 		}
 		reloadConfig();
 
 		ignoreWorlds = getConfig().getStringList("IgnoreWorld");
 		verbose = getConfig().getBoolean("verboselog");
 	}
 
 	/**
 	 * 設定ファイルが無い場合は作ります
 	 * @param file
 	 * @return
 	 */
 	public boolean newConfig(File file){
 		FileWriter fileWriter;
 		if (!file.getParentFile().exists()){
 			file.getParentFile().mkdir();
 		}
 
 		try{
 			fileWriter =new FileWriter(file);
 		}catch (IOException e){
 			log.severe(logPrefix+"Can't write config file!:"+e.getMessage());
 			Bukkit.getServer().getPluginManager().disablePlugin(this);
 			return false;
 		}
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResource("config.yml"))));
 		BufferedWriter writer = new BufferedWriter(fileWriter);
 		try{
 			String line = reader.readLine();
 			while (line != null){
 				writer.write(line + System.getProperty("line.separator"));
 				line = reader.readLine();
 			}
 			log.info(logPrefix+"Created default config file!");
 		}catch (IOException e){
 			log.severe(logPrefix+"Can't write config file!:"+e.getMessage());
 		}finally{
 			try{
 				writer.close();
 				reader.close();
 			}catch (IOException e){
 				log.severe(logPrefix+"Error!Please report this!:"+e.getMessage());
 				Bukkit.getServer().getPluginManager().disablePlugin(this);
 			}
 		}
 		return true;
 	}
 
 }
