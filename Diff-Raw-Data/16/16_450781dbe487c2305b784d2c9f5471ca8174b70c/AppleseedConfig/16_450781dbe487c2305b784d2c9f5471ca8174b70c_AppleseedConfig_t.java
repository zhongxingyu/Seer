 package redsgreens.Appleseed;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.yaml.snakeyaml.Yaml;
 
 public class AppleseedConfig {
 	private Appleseed Plugin;
 
 	public Boolean ShowErrorsInClient = true;
 	public Integer DropLikelihood = 33;
 	public Integer DropInterval = 60;
 	public ArrayList<String> AllowedTreeTypes;
 	public ArrayList<ItemStack> AllowedTreeItems;
 
 	public AppleseedConfig(Appleseed plugin)
 	{
 		Plugin = plugin;
 		AllowedTreeTypes = new ArrayList<String>();
 		AllowedTreeItems = new ArrayList<ItemStack>();
 	}
 	@SuppressWarnings("unchecked")
 	public void LoadConfig()
 	{
 		try
 		{
 			// create the data folder if it doesn't exist
 			File folder = Plugin.getDataFolder();
 	    	if(!folder.exists())
 	    		folder.mkdirs();
     	
 	    	// create a stock config file if it doesn't exist
 	    	File configFile = new File(folder, "config.yml");
 			if (!configFile.exists()){
 				configFile.createNewFile();
 				InputStream res = Appleseed.class.getResourceAsStream("/config.yml");
 				FileWriter tx = new FileWriter(configFile);
 				for (int i = 0; (i = res.read()) > 0;) tx.write(i);
 				tx.flush();
 				tx.close();
 				res.close();
 			}
 
 			// create an empty config
 			HashMap<String, Object> configMap = new HashMap<String, Object>();
 			
 			BufferedReader rx = new BufferedReader(new FileReader(configFile));
 			Yaml yaml = new Yaml();
 			
 			try{
 				configMap = (HashMap<String,Object>)yaml.load(rx);
 			}
 			catch (Exception ex){
 				System.out.println(ex.getMessage());
 			}
 			finally
 			{
 				rx.close();
 			}
 
 			if(configMap.containsKey("ShowErrorsInClient"))
 				ShowErrorsInClient = (Boolean)configMap.get("ShowErrorsInClient");
 			System.out.println("Appleseed: ShowErrorsInClient=" + ShowErrorsInClient.toString());
 
 			if(configMap.containsKey("DropLikelihood"))
 				DropLikelihood = (Integer)configMap.get("DropLikelihood");
 			System.out.println("Appleseed: DropLikelihood=" + DropLikelihood.toString() + "%");
 
 			if(configMap.containsKey("DropInterval"))
 				DropInterval = (Integer)configMap.get("DropInterval");
 			System.out.println("Appleseed: DropInterval=" + DropInterval.toString() + " seconds");
 
 			ArrayList<String> tempAllowedTreeTypes = new ArrayList<String>(Arrays.asList("apple", "cookie"));
 			if(configMap.containsKey("AllowedTreeTypes"))
 				tempAllowedTreeTypes = (ArrayList<String>)configMap.get("AllowedTreeTypes");
 
 			// process list of tree types and generate materials list
 			for(int i=0; i<tempAllowedTreeTypes.size(); i++)
 			{
 				Material m = Material.matchMaterial(tempAllowedTreeTypes.get(i));
 				if(m != null)
 				{
					AllowedTreeItems.add(new ItemStack(m, 1));
 					AllowedTreeTypes.add(tempAllowedTreeTypes.get(i).toLowerCase());
 				}
 				else if(tempAllowedTreeTypes.get(i).equalsIgnoreCase("cocoa_beans"))
 				{
 					AllowedTreeItems.add(new ItemStack(Material.INK_SACK, 1, (short)3));
 					AllowedTreeTypes.add(tempAllowedTreeTypes.get(i).toLowerCase());
 				}
 			}
 			System.out.println("Appleseed: AllowedTreeTypes=" + AllowedTreeTypes.toString());
 		}
 		catch (Exception ex){
 			System.out.println(ex.getStackTrace());
 		}
 	}
 }
