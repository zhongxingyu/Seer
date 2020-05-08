 package me.tehbeard.BeardAch.dataSource;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.configuration.ConfigurationSection;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.Achievement.Display;
 import me.tehbeard.BeardAch.achievement.rewards.IReward;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 import me.tehbeard.BeardAch.dataSource.json.ClassCatalogue;
 import me.tehbeard.BeardAch.dataSource.json.LocationJSONParser;
 import me.tehbeard.BeardAch.dataSource.json.RewardJSONParser;
 import me.tehbeard.BeardAch.dataSource.json.TriggerJSONParser;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.reflect.TypeToken;
 import com.google.gson.stream.JsonWriter;
 
 
 /**
  * Loads achievements from an external Gson file
  * @author James
  *
  */
 public class AchievementLoader {
 
 	public static final ClassCatalogue<ITrigger> triggerFactory = new ClassCatalogue<ITrigger>();
 	public static final ClassCatalogue<IReward> rewardFactory = new ClassCatalogue<IReward>();
 
 	/**
 	 * Create prime Gson object, 
 	 * Only export annotated fields
 	 * Pretty print for human debugging.
 	 * Also adds type adapters for trigger, reward and location
 	 */
 	private static Gson gson = new GsonBuilder().
 			excludeFieldsWithoutExposeAnnotation().
 			setPrettyPrinting().
 			registerTypeHierarchyAdapter(ITrigger.class, new TriggerJSONParser()).
 			registerTypeHierarchyAdapter(IReward.class, new RewardJSONParser()).
 			registerTypeHierarchyAdapter(Location.class,new LocationJSONParser()).
 			create();
 
 	private static List<Achievement> loadAchievementsFromJSONFile(File file){
 	    
         try {
             return gson.fromJson(new FileReader(file), new TypeToken<List<Achievement>>(){}.getType());
         } catch (JsonIOException e) {
             BeardAch.printError("An error occured reading " + file.toString(),e);
         } catch (JsonSyntaxException e) {
             BeardAch.printError("There is a problem with the syntax of " + file.toString(),e);
         } catch (FileNotFoundException e) {
             BeardAch.printError(file.toString() + " not found",e);
         } catch (IOException e) {
             BeardAch.printError("An error occured reading " + file.toString(),e);
         }
         return null;
 	}
 	
 	public static void loadAchievements(){
 
 		try {
 			//Load and create file
 			File file = new File(BeardAch.self.getDataFolder(),"ach.json");
 			file.createNewFile();
 			List<Achievement> achievements = loadAchievementsFromJSONFile(file);
 			if(achievements!=null){
 				//Run postLoad() on all achievements and add them to manager if successful 
 				for(Achievement a : achievements){
 					if(a.postLoad()){
 						BeardAch.printDebugCon("Loading achievement " + a.getName());
 						BeardAch.self.getAchievementManager().addAchievement(a);
 					}
 					else
 					{
 						BeardAch.printCon("Could not load " + a.getName());
 					}
 				}
 			}
 			
 			File achDir = new File(BeardAch.self.getDataFolder(),"config");
 			if(achDir.isDirectory() && achDir.exists()){
 			    for(String f : achDir.list(new FilenameFilter() {
                     
                     public boolean accept(File dir, String name) {
                         return name.endsWith(".json");
                     }
                 })){
 			        achievements = loadAchievementsFromJSONFile(new File(achDir,f));
 		            if(achievements!=null){
 		                //Run postLoad() on all achievements and add them to manager if successful 
 		                for(Achievement a : achievements){
 		                    if(a.postLoad()){
 		                        BeardAch.printDebugCon("Loading achievement " + a.getName());
 		                        BeardAch.self.getAchievementManager().addAchievement(a);
 		                    }
 		                    else
 		                    {
 		                        BeardAch.printCon("Could not load " + a.getName());
 		                    }
 		                }
 		            }
 			    }
 			}
 
 
 
 
 
 			//TODO: Kill in 0.6
 			//old method to load achievements
 			List<Achievement> l = loadOldConfigAchievements();
 			boolean tripped = false;
 			for(Achievement a:l){
 				tripped = true;
 				BeardAch.printCon("Loading achievement " + a.getName());
 				BeardAch.self.getAchievementManager().addAchievement(a);
 			}
 			//convert old to new json awesomeness
 			if(tripped){
 
 				JsonWriter jw = new JsonWriter(new FileWriter(file));
 				jw.setIndent("  ");
 				gson.toJson(
 						BeardAch.self.getAchievementManager().getLoadedAchievements(),
 						new TypeToken<List<Achievement>>(){}.getType(), 
 						jw
 						);
 				jw.flush();
 				jw.close();
 				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[BEARDACH] CONVERTED ACHIEVEMENTS TO JSON, PLEASE CHECK CONVERSION WORKED AND REMOVE ACHIEVEMENTS ENTRY FROM config.yml");
 			}
 
 
 		} catch (JsonIOException e) {
 			BeardAch.printError("An error occured reading ach.json",e);
 
 		} catch (JsonSyntaxException e) {
 			BeardAch.printError("There is a problem with the syntax of ach.json",e);
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			BeardAch.printError("ach.json not found",e);
 		} catch (IOException e) {
 			BeardAch.printError("An error occured reading ach.json",e);
 			e.printStackTrace();
 		}
 	}
 
 
 	//TODO: KILL THIS WITH FIRE IN 0.6
 	public static List<Achievement> loadOldConfigAchievements(){
 
 		List<Achievement> a = new ArrayList<Achievement>();
 
 		BeardAch.printDebugCon("Loading Achievement Data");
 		BeardAch.self.reloadConfig();
 		if(BeardAch.self.getConfig().isConfigurationSection("achievements")){
 			BeardAch.printCon("[PANIC] OLD ACHIEVEMENTS CONFIG FOUND, CONVERSION WILL BE DONE");
 		}
 		else
 		{
 			return a;
 		}
 
 		Set<String> achs = BeardAch.self.getConfig().getConfigurationSection("achievements").getKeys(false);
 
 		for(String slug : achs){
 			ConfigurationSection e = BeardAch.self.getConfig().getConfigurationSection("achievements").getConfigurationSection(slug);
 			if(e==null){
 				continue;
 			}
 			//load information
 			String name = e.getString("name");
 			String descrip = e.getString("descrip");
 			Display broadcast = Achievement.Display.valueOf(e.getString("broadcast",BeardAch.self.getConfig().getString("ach.msg.send","NONE")));
 			slug = e.getString("alias",slug);
 			boolean hidden = e.getBoolean("hidden",false);
 			BeardAch.printDebugCon("Loading achievement " + name);
 
 			@SuppressWarnings("deprecation")
 			Achievement ach = new Achievement(slug,name, descrip,broadcast,hidden);
 
 			//load triggers
 			try{
 				List<String> triggers = e.getStringList("triggers");
 
 				for(String trig: triggers){
 					String[] part = trig.split("\\|");
 					if(part.length==2){
 						BeardAch.printDebugCon("Trigger => " + trig);
 						ITrigger trigger = triggerFactory.get(part[0]).newInstance();
 						if(trigger==null){BeardAch.printCon("[PANIC] TRIGGER " + part[0] + " NOT FOUND!!! SKIPPING.");continue;}
 						trigger.configure(ach,part[1]);
 						trigger.configure(ach);
 						ach.addTrigger(trigger);
 					}
 					else
 					{
 						BeardAch.printCon("[PANIC] ERROR! MALFORMED TRIGGER FOR ACHIEVEMENT " + name);
 					}
 				}
 				List<String> rewards = e.getStringList("rewards");
 				for(String reward: rewards){
 					String[] part = reward.split("\\|");
 					if(part.length==2){
 						BeardAch.printDebugCon("Reward => " + reward); 
 						IReward rewardInst = rewardFactory.get(part[0]).newInstance();
 						rewardInst.configure(ach,part[1]);
 						rewardInst.configure(ach);
 						ach.addReward(rewardInst);
 					}
 					else
 					{
 						BeardAch.printCon("[PANIC] ERROR! MALFORMED REWARD FOR ACHIEVEMENT " + name);
 					}
 				}
 
 			} catch (InstantiationException e1) {
 				BeardAch.printError("Error loading old achievements",e1);
 			} catch (IllegalAccessException e1) {
 				BeardAch.printError("Error loading old achievements",e1);
 			}
 
 			a.add(ach);
 		}
 
 		BeardAch.self.getConfig().set("oldAchievements", BeardAch.self.getConfig().getConfigurationSection("achievements"));
 		BeardAch.self.getConfig().set("achievements",null);
 		BeardAch.self.saveConfig();
 
 		return a;
 	}
 }
