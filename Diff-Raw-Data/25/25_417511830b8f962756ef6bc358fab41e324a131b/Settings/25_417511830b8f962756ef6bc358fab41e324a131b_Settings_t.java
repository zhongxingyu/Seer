 package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;
 import com.rcjrrjcr.bukkitplugins.util.SearchHelper;
 
 //TODO: Add SearchHelper for categories and abilities
 
 public class Settings {
 	private BuyAbilities origin;
 	private File yamlFile; 
 	private Configuration yamlConfig;
 	private HashMap<String,Ability> nameToAbilityMap;
 	private HashMap<String,Set<Ability>> categoryToAbilityMap;
 	private HashMap<String,Set<Ability>> commandRegex;
 	private SearchHelper abilitySearch;
 	private SearchHelper categorySearch;
 	
 	public Settings(BuyAbilities origin, String path) throws Exception
 	{
 		abilitySearch = new SearchHelper();
 		categorySearch = new SearchHelper();
 		nameToAbilityMap = new HashMap<String,Ability>();
 		categoryToAbilityMap = new HashMap<String,Set<Ability>>();
 		commandRegex = new HashMap<String,Set<Ability>>();
 		this.origin = origin; 
 		yamlFile = new File(path);
 		if(!(yamlFile.exists()))
 		{
 			throw new Exception(path + " not found.");
 		}
 		if(!(yamlFile.isFile()))
 		{
 			throw new Exception(path + " not a file.");
 		}
 		if(!(yamlFile.canRead()))
 		{
 			throw new Exception(path + "not readable.");
 		}
 
 		yamlConfig = new Configuration(yamlFile);
 		yamlConfig.load();
 		
 		
 		reload();
 	}
 	
 	public void reload() throws Exception
 	{
 		//TODO: Add code to load hooked commands
 		nameToAbilityMap.clear();
 		categoryToAbilityMap.clear();
 		commandRegex.clear();
 		abilitySearch.clear();
 		categorySearch.clear();
 		Map<String, ConfigurationNode> abilityNodeList = yamlConfig.getNodes("Abilities");
 		for(String abilityName : abilityNodeList.keySet())
 		{
 			Ability ab = new Ability();
 			ab.name = abilityName;
 			ab.info.extName = yamlConfig.getString("Abilities."+abilityName+".info.name","Default Ability Name");
 			ab.info.desc = yamlConfig.getString("Abilities."+abilityName+".info.description","Default Ability Description");
 			ab.info.help = yamlConfig.getString("Abilities."+abilityName+".info.help","Default Ability HelpText");
 			ab.perms.addAll(yamlConfig.getStringList("Abilities."+abilityName+".permissions", new ArrayList<String>()));
 			ab.categories = yamlConfig.getStringList("Abilities."+abilityName+".categories", new ArrayList<String>());
 			if(yamlConfig.getNode("Abilities."+abilityName+".costs.buy")!=null)
 			{
 				ab.costs.canBuy = true;
				ab.costs.buyCost = yamlConfig.getInt("Abilities."+abilityName+".costs.buy.cost", 0);
 			}
			if(yamlConfig.getNode("Abilities."+abilityName+".costs.rent")!=null&&(yamlConfig.getInt("Abilities."+abilityName+".costs.rent.duration", 0)>0))
 			{
 				ab.costs.canRent = true;
				ab.costs.rentCost = yamlConfig.getInt("Abilities."+abilityName+".costs.rent.cost", 0);
				ab.costs.rentDuration = yamlConfig.getInt("Abilities."+abilityName+".costs.rent.duration", 0);
 			}
			if(yamlConfig.getNode("Abilities."+abilityName+".costs.use")!=null&&(yamlConfig.getInt("Abilities."+abilityName+".costs.use.usecount", 0)>0)&&(yamlConfig.getStringList("Abilities."+abilityName+".commands",null)!=null)&&(!yamlConfig.getStringList("Abilities."+abilityName+".commands",null).isEmpty()))
 			{
 				ab.costs.canUse = true;
				ab.costs.useCost = yamlConfig.getInt("Abilities."+abilityName+".costs.use.cost", 0);
				ab.costs.useCount = yamlConfig.getInt("Abilities."+abilityName+".costs.use.usecount", 0);
 			}
 			List<String> rgxList = yamlConfig.getStringList("Abilities."+abilityName+".commands",new LinkedList<String>());
 //			System.out.println(rgxList);
 			ab.commands = rgxList;
 			for(String regex : rgxList)
 			{
 				if(!commandRegex.containsKey(regex)) commandRegex.put(regex, new HashSet<Ability>());
 				commandRegex.get(regex).add(ab);
 			}
 			
 			nameToAbilityMap.put(ab.name, ab);
 			for(String category : ab.categories)
 			{
 				if(!categoryToAbilityMap.containsKey(category)) categoryToAbilityMap.put(category, new HashSet<Ability>());
 				categoryToAbilityMap.get(category).add(ab);
 				categorySearch.addWord(category);
 			}
 			abilitySearch.addWord(abilityName);
 		}
 		
 //		System.out.println(commandRegex);
 	}
 	
 	
 	public Costs getCost(String abilityName)
 	{
 		return nameToAbilityMap.get(abilityName).costs;
 	}
 	public Info getInfo(String abilityName)
 	{
 		return nameToAbilityMap.get(abilityName).info;
 	}
 	public List<String> getPerms(String abilityName)
 	{
 		return nameToAbilityMap.get(abilityName).perms;
 	}
 	public List<String> getAbilityCategories(String abilityName)
 	{
 		return nameToAbilityMap.get(abilityName).categories;
 	}
 	public List<String> getCategories(String world, Player player)
 	{
 		List<String> categoryList = new ArrayList<String>();
 		for(String category : categoryToAbilityMap.keySet())
 		{
 			if(origin.hasPermission(world, player.getName(), "buyabilities.abilities."+category.replace(' ', '.').toLowerCase()))
 			{
 				categoryList.add(category);
 			}
 		}
 		return categoryList;
 	}
 	
 	public List<String> getAbilites(String categoryName)
 	{
 		List<String> abList = new ArrayList<String>();
 		if(categoryToAbilityMap.get(categoryName)==null) return abList;
 		Set<Ability> abSet = categoryToAbilityMap.get(categoryName);
 		if(abSet==null||abSet.isEmpty()) return abList;
 		for(Ability ab : abSet)
 		{
 			abList.add(ab.name);
 		}
 		abSet = null;
 		return abList;
 	}
 	public boolean canPurchase(String abilityName,String world, Player player)
 	{
 		if(nameToAbilityMap.get(abilityName) == null) return false;
 		Ability ab = nameToAbilityMap.get(abilityName);
 		for(String categoryName : ab.categories)
 		{
 			 if(canAccess(categoryName,world,player)) return true;
 		}
 		return false;
 	}
 
 	public boolean canAccess(String categoryName,String world, Player player)
 	{
 		 return origin.hasPermission( world, player.getName(), "buyabilities.abilities."+categoryName.replace(' ', '.') );
 	}
 	
 	public Ability getAbility(String abilityName)
 	{
 		return nameToAbilityMap.get(abilityName);
 	}
 	
 	public List<String> getAllAbilities(String worldName, Player player)
 	{
 		List<String> categories = getCategories(worldName,player);
 		Set<String> abSet = new HashSet<String>();
 		for(String categoryName : categories)
 		{
 			Set<Ability> abList = categoryToAbilityMap.get(categoryName);
 			for(Ability ab : abList)
 			{
 				abSet.add(ab.name);
 			}
 		}
 		List<String> abList = new ArrayList<String>(abSet);
 		abSet = null;
 		categories = null;
 		return abList;
 	}
 	
 	public Set<String> getAllCmds()
 	{
 		return commandRegex.keySet();
 	}
 	public Set<Ability> getCmdAbility(String cmd)
 	{
 		Set<Ability> abSet = commandRegex.get(cmd);
 		if(abSet==null) return new HashSet<Ability>();
 		return abSet;
 	}
 	
 	public List<String> getAbilityMisses(String abilityName, final int threshold)
 	{
 		return abilitySearch.getMisses(abilityName, threshold);
 	}
 	public List<String> getCategoryMisses(String categoryName, final int threshold)
 	{
 		return categorySearch.getMisses(categoryName, threshold);
 	}
 }
 
 
