 package me.steeveeo.ElixirMod;
 
 import java.util.TimerTask;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class ElixirModToxicityController extends TimerTask {
 
 	//Start Logger
 	//Logger log = Logger.getLogger("Minecraft");
 	
 	//Plugin Constructor
 	public ElixirMod plugin; public ElixirModToxicityController(ElixirMod instance)
 	{ 
 		plugin = instance;
 	}
 	
 	//Control Variables
 	int maxPlayers = 20; //plugin.getServer().getMaxPlayers();
 	PotionEntry[] potionUsers = new PotionEntry[maxPlayers];
 	int thisTick = 0;
 	
 	//------------------
 	// CACTUS RUM SETUP
 	//------------------
 	
 	//Modifiers
 	int CactusRum_baseDamage = 6;
 	int CactusRum_toxicityPerDamage = 5;
 	int CactusRum_toxicityPerDrink = 60;
 	int CactusRum_ticksPerDrink = 60;
 	int CactusRum_tickLength = 3;
 	
 	//Party Messages
 	static String[] CactusRum_swigMessages = new String[]{
 		"You feel woozy.",
 		"You take a drink.",
 		"You take a swig.",
 		"The Cactus Rum burns on the way down.",
 		"The drink scorches your throat.",
 		"Senses of both bravery and nausea wash over you.",
 		"Fitting for a drink made of cactus, that stung.",
 		"You feel like you just tried to eat a sword.",
 		"Your tongue goes numb, your vision blurs slightly.",
 		"Your pain fades, as does your reason."
 	};
 	
 	//End of Party Messages
 	static String[] CactusRum_soberMessages = new String[]{
 		"You sober up.",
 		"You are now thinking clearly.",
 		"The alcoholic fog clears, you have a headache.",
 		"Your head hurts like the Nether, but you are sober.",
 		"The liquor wears off.",
 		"Your vision clears, but your stomachache does not.",
 		"Your pain and reasoning come crashing back to you.",
 		"Everything returns to its normal, cuboid shape.",
 		"Suddenly the blur goes away, everything is clear.",
 		"You regain your balance, only to buckle in pain."
 	};
 	
 	//Player took a swig
 	public void CactusRum_playerDrink(Player player)
 	{
 		//Check if this player already exists in the table
 		boolean entryExists = false;
 		PotionEntry thisGuyIsSmashed = new PotionEntry(player.getName(),player,CactusRum_toxicityPerDrink);
 		for(PotionEntry user : potionUsers)
 		{
 			if(user != null)
 			{
 				if(user.getPlayer() == player)
 				{
 					entryExists = true;
 					thisGuyIsSmashed = user;
 					break;
 				}
 			}
 		}
 		player.sendMessage(ChatColor.DARK_GRAY + "**" + util.randomStringFromList(CactusRum_swigMessages) + "**");
 		
 		//Check if we found the entry
 		if(entryExists)
 		{
 			//Party hard!
			player.damage(CactusRum_baseDamage+(thisGuyIsSmashed.toxicity/CactusRum_toxicityPerDamage));
			thisGuyIsSmashed.CactusRumEnabled = true;
 			
 			//Update entry
 			thisGuyIsSmashed.modToxicity(CactusRum_toxicityPerDrink);
 			thisGuyIsSmashed.CactusRumTicks = CactusRum_ticksPerDrink;
 		}
 		//If not, add the new entry to the list
 		else
 		{
 			//Party hard!
 			player.damage(CactusRum_baseDamage);
 			
 			//Add
 			int index = 0;
 			for(PotionEntry user : potionUsers)
 			{
 				if(user == null)
 				{
 					potionUsers[index] = thisGuyIsSmashed;
 					thisGuyIsSmashed.CactusRumEnabled = true;
 					thisGuyIsSmashed.CactusRumTicks = CactusRum_ticksPerDrink;
 					return;
 				}
 				else
 				{
 					index++;
 				}
 			}
 			
 			//Didn't get added to the list, fffff
 			plugin.log.info("[ElixirMod] - ERROR: PLAYER COULD NOT BE ADDED TO LIST!");
 		}
 	}
 	
 	//--End Cactus Rum Setup--
 	
 	//-----------------------
 	// ELIXIR OF HASTE SETUP
 	//-----------------------
 	
 	//Modifiers
 	int Haste_baseDamage = 0;
 	int Haste_toxicityMinForDamage = 50;
 	int Haste_toxicityPerDamage = 15;
 	int Haste_toxicityPerDrink = 20;
 	int Haste_ticksPerDrink = 10;
 	int Haste_tickLength = 6;
 	
 	//Take a Drink messages
 	static String[] Haste_swigMessages = new String[]{
 		"You feel light on your feet."
 	};
 	
 	//It Wore Off messages
 	static String[] Haste_soberMessages = new String[]{
 		"Your legs begin to feel sluggish."
 	};
 	
 	//Player took a swig
 	public void Haste_playerDrink(Player player)
 	{
 		//Check if this player already exists in the table
 		boolean entryExists = false;
 		PotionEntry drinker = new PotionEntry(player.getName(),player,Haste_toxicityPerDrink);
 		for(PotionEntry user : potionUsers)
 		{
 			if(user != null)
 			{
 				if(user.getPlayer() == player)
 				{
 					entryExists = true;
 					drinker = user;
 					break;
 				}
 			}
 		}
 		player.sendMessage(ChatColor.DARK_GRAY + "**" + util.randomStringFromList(Haste_swigMessages) + "**");
 		
 		//Check if we found the entry
 		if(entryExists)
 		{
 			//Toxic Damage
 			if(drinker.toxicity > Haste_toxicityMinForDamage)
 			{
 				player.damage(Haste_baseDamage+(drinker.toxicity/Haste_toxicityPerDamage));
 			}
 			
 			//Update entry
 			drinker.modToxicity(Haste_toxicityPerDrink);
 			
 			//Enable speed
 			plugin.hasteEnabled.put(player,true);
 			drinker.HasteEnabled = true;
 			drinker.HasteTicks = Haste_ticksPerDrink;
 		}
 		//If not, add the new entry to the list
 		else
 		{
 			//Add
 			int index = 0;
 			for(PotionEntry user : potionUsers)
 			{
 				if(user == null)
 				{
 					//Enable speed
 					plugin.hasteEnabled.put(player,true);
 					drinker.HasteEnabled = true;
 					drinker.HasteTicks = Haste_ticksPerDrink;
 					
 					potionUsers[index] = drinker;
 					return;
 				}
 				else
 				{
 					index++;
 				}
 			}
 			
 			//Didn't get added to the list, fffff
 			plugin.log.info("[ElixirMod] - ERROR: PLAYER COULD NOT BE ADDED TO LIST!");
 		}
 	}
 	
 	//--End Elixir of Haste Setup--
 
 	//Struct for dealing with players
 	public class PotionEntry
 	{
 		//Vars
 		private
 			String name;
 			Player player;
 			int toxicity;
 			
 		//Potion Buffs
 		public 
 			boolean CactusRumEnabled;
 			int CactusRumTicks;
 			boolean HasteEnabled;
 			int HasteTicks;
 		
 		//Constructor
 		public PotionEntry(String newName, Player newPlayer, int setToxicity)
 		{
 			name = newName;
 			player = newPlayer;
 			toxicity = setToxicity;
 		}
 		
 		//Get Toxicity
 		public int getToxicity()
 		{
 			return toxicity;
 		}
 		
 		//Modify Toxicity
 		public void modToxicity(int amount)
 		{
 			toxicity += amount;
 		}
 		
 		//Get Name
 		public String getName()
 		{
 			return name;
 		}
 		
 		//Get Player
 		public Player getPlayer()
 		{
 			return player;
 		}
 	}
 	
 	//Return index of PotionEntry
 	public int getPotionEntryByPlayer(Player player)
 	{
 		
 		return -1;
 	}
 	
 	//---------------------
 	// ELIXIR CONTROL LOOP
 	//---------------------
 	public void run()
 	{
 		thisTick++;
 		
 		int index = 0;
 		for(PotionEntry user : potionUsers)
 		{
 			//Validity Check
 			if(user == null || user.getPlayer() == null)
 			{
 				index++;
 				continue;
 			}
 			
 			Player thisPlayer = user.getPlayer();
 			
 			//Tick down Toxicity
 			user.modToxicity(-1);
 			
 			//--------------------
 			// CACTUS RUM CONTROL
 			//--------------------
 			if(thisTick % CactusRum_tickLength == 0 && user.CactusRumEnabled)
 			{
 				//Regen health
 				if(thisPlayer.getHealth() > 0)
 				{
 					user.CactusRumTicks -= 1;
 					//Can't go above max
 					if(thisPlayer.getHealth() < 20)
 					{
 						thisPlayer.setHealth(thisPlayer.getHealth()+1);
 					}
 				}
 				
 				//End?
 				if(user.CactusRumTicks == 0)
 				{
 					user.getPlayer().sendMessage(ChatColor.GRAY + "**" + util.randomStringFromList(CactusRum_soberMessages) + "**");
 					user.CactusRumEnabled = false;
 				}
 			}
 			//--End Cactus Rum--
 			
 			
 			//-------------------------
 			// ELIXIR OF HASTE CONTROL
 			//-------------------------
 			if(thisTick % Haste_tickLength == 0 && user.HasteEnabled)
 			{
 				user.HasteTicks -= 1;
 				
 				//End?
 				if(user.HasteTicks == 0)
 				{
 					user.getPlayer().sendMessage(ChatColor.GRAY + "**" + util.randomStringFromList(Haste_soberMessages) + "**");
 					plugin.hasteEnabled.put(thisPlayer, false);
 					user.HasteEnabled = false;
 				}
 			}
 			//--End Haste--
 			
 			index++;
 		}
 		
 	}
 	
 	//--END ELIXIR CONTROL LOOP--
 }
