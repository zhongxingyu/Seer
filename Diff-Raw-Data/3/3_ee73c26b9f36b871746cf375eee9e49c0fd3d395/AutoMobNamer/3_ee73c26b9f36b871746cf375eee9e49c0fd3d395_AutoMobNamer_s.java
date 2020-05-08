 package net.ultibyte.AutoMobNamer;
 
 import java.io.IOException;
 import java.util.Random;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.ultibyte.AutoMobNamer.MetricsLite;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AutoMobNamer extends JavaPlugin implements Listener {
 
 	List<String> BatNames = new ArrayList<String>();
 	List<String> ChickenNames = new ArrayList<String>();
 	List<String> CowNames = new ArrayList<String>();
 	List<String> IronGolemNames = new ArrayList<String>();
 	List<String> MooshroomNames = new ArrayList<String>();
 	List<String> OcelotNames = new ArrayList<String>();
 	List<String> PigNames = new ArrayList<String>();
 	List<String> SheepNames = new ArrayList<String>();
 	List<String> SquidNames = new ArrayList<String>();
 	List<String> WolfNames = new ArrayList<String>();
 	List<String> BlazeNames = new ArrayList<String>();
 	List<String> CaveSpiderNames = new ArrayList<String>();
 	List<String> CreeperNames = new ArrayList<String>();
 	List<String> EnderDragonNames = new ArrayList<String>();
 	List<String> EndermanNames = new ArrayList<String>();
 	List<String> GhastNames = new ArrayList<String>();
 	List<String> MagmaCubeNames = new ArrayList<String>();
 	List<String> SilverfishNames = new ArrayList<String>();
 	List<String> SkeletonNames = new ArrayList<String>();
 	List<String> SlimeNames = new ArrayList<String>();
 	List<String> SnowGolemNames = new ArrayList<String>();
 	List<String> SpiderNames = new ArrayList<String>();
 	List<String> WitchNames = new ArrayList<String>();
 	List<String> WitherNames = new ArrayList<String>();
 	List<String> ZombieNames = new ArrayList<String>();
 	List<String> ZombiePigmanNames = new ArrayList<String>();
 	List<String> VillagerNames = new ArrayList<String>();
 
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		loadConfig();
 		try {
 			MetricsLite metrics = new MetricsLite(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :-(
 		}
 
 		Server server = this.getServer();
 		ConsoleCommandSender console = server.getConsoleSender();
 		console.sendMessage(ChatColor.DARK_RED + "Auto" + ChatColor.BLUE + "Mob" + ChatColor.YELLOW + "Namer " + ChatColor.RESET + "has been " + ChatColor.GREEN + ChatColor.BOLD + "enabled" + ChatColor.RESET + "!");
 		VariableStuff.NoColor = false;
 
 	}
 
 	@Override
 	public void onDisable() {
 		Server server = this.getServer();
 		ConsoleCommandSender console = server.getConsoleSender();
 		console.sendMessage(ChatColor.DARK_RED + "Auto" + ChatColor.BLUE + "Mob" + ChatColor.YELLOW + "Namer " + ChatColor.RESET + "has been " + ChatColor.RED + ChatColor.BOLD + "disabled" + ChatColor.RESET + ".");
 		saveConfig();
 	}
 
 	List<String> ListOfColorCodes = new ArrayList<String>();
 
 	public void PutColorCodesIntoList() {
 		if (ListOfColorCodes.isEmpty()) {
 			ListOfColorCodes.add("§0");
 			ListOfColorCodes.add("§1");
 			ListOfColorCodes.add("§2");
 			ListOfColorCodes.add("§3");
 			ListOfColorCodes.add("§4");
 			ListOfColorCodes.add("§5");
 			ListOfColorCodes.add("§6");
 			ListOfColorCodes.add("§7");
 			ListOfColorCodes.add("§8");
 			ListOfColorCodes.add("§9");
 			ListOfColorCodes.add("§a");
 			ListOfColorCodes.add("§b");
 			ListOfColorCodes.add("§c");
 			ListOfColorCodes.add("§d");
 			ListOfColorCodes.add("§e");
 			ListOfColorCodes.add("§f");
 			ListOfColorCodes.add("§8");
 		}
 		if (VariableStuff.NoColor) {
 			ListOfColorCodes.clear();
 			ListOfColorCodes.add("§f");
 
 		}
 
 	}
 
 	public String RandomColorMethod() {
 		int ColorSelection = createRandom(0, ListOfColorCodes.size());
 		String RandomlySelectedColor = ListOfColorCodes.get(ColorSelection);
 		return RandomlySelectedColor;
 	}
 
 	public void loadConfig() {
 		getConfig().options().copyDefaults(true);
 
 		BatNames = getConfig().getStringList("BatNames");
 		if (BatNames.isEmpty()) {
 			BatNames.add("Man");
 			BatNames.add("Rouge");
 			BatNames.add("Flutter Me Timbres");
 			BatNames.add("Batty");
 			BatNames.add("Dracula");
 			BatNames.add("Batt");
 			BatNames.add("Bast");
 			BatNames.add("Bob the Bat");
 			BatNames.add("Fang");
 			BatNames.add("Vlad");
 			BatNames.add("Batosity");
 			BatNames.add("Al-Ghul");
 			BatNames.add("Nosferatu");
 			BatNames.add("Fledermaus");
 			BatNames.add("Bitey");
 			BatNames.add("Diablerie");
 			BatNames.add("Bella");
 			BatNames.add("Max");
 			BatNames.add("Lee");
 			BatNames.add("Tony");
 			BatNames.add("Bax");
 			BatNames.add("Lucifer");
 			BatNames.add("Dullus");
 			BatNames.add("Skotardus");
 			BatNames.add("Thanatos");
 			BatNames.add("Mortus");
 			BatNames.add("Tenebrus");
 			BatNames.add("Noisy");
 			BatNames.add("Charline");
 			BatNames.add("Rosfrath");
 			BatNames.add("Bastion");
 			BatNames.add("Luna");
 			BatNames.add("Maya");
 			BatNames.add("Mayotis");
 			BatNames.add("Batrisha");
 			BatNames.add("Ralph");
 			BatNames.add("Bertie");
 
 		}
 		this.getConfig().set("BatNames", BatNames);
 
 		ChickenNames = getConfig().getStringList("ChickenNames");
 		if (ChickenNames.isEmpty()) {
 			ChickenNames.add("Picasso");
 			ChickenNames.add("Fingerlicken");
 			ChickenNames.add("Licken");
 			ChickenNames.add("RahhtenFleyshh");
 			ChickenNames.add("Batter");
 			ChickenNames.add("Dinner");
 			ChickenNames.add("Lunch");
 			ChickenNames.add("Foghorn");
 			ChickenNames.add("Leghorn");
 			ChickenNames.add("Boo");
 			ChickenNames.add("Alfred");
 			ChickenNames.add("Ginger");
 			ChickenNames.add("Camilla");
 			ChickenNames.add("Robot");
 			ChickenNames.add("Cluck");
 			ChickenNames.add("Clucky");
 			ChickenNames.add("Mike");
 			ChickenNames.add("Rocky");
 			ChickenNames.add("Gregory");
 			ChickenNames.add("Peck");
 			ChickenNames.add("Little");
 			ChickenNames.add("Penny");
 			ChickenNames.add("Rubber");
 			ChickenNames.add("Nugget");
 			ChickenNames.add("Leg");
 			ChickenNames.add("Wing");
 			ChickenNames.add("Fried");
 			ChickenNames.add("Kentucky");
 			ChickenNames.add("Toktok");
 			ChickenNames.add("Flatfoot");
 		}
 		this.getConfig().set("ChickenNames", ChickenNames);
 
 		CowNames = getConfig().getStringList("CowNames");
 		if (CowNames.isEmpty()) {
 			CowNames.add("Gertrude");
 			CowNames.add("Bessie");
 			CowNames.add("Whack Angus");
 			CowNames.add("Beefy");
 			CowNames.add("Ermintrude");
 			CowNames.add("Colleen");
 			CowNames.add("Betsy");
 			CowNames.add("Patty");
 			CowNames.add("Anna");
 			CowNames.add("Molly");
 			CowNames.add("Darcy");
 			CowNames.add("Elli");
 			CowNames.add("Ethel");
 			CowNames.add("Nelly");
 			CowNames.add("Beth");
 			CowNames.add("Daisy");
 			CowNames.add("Buttercup");
 			CowNames.add("Achelous");
 			CowNames.add("Audhumla");
 			CowNames.add("Bat");
 			CowNames.add("Blue");
 			CowNames.add("Bully");
 			CowNames.add("Chaldan");
 			CowNames.add("Damona");
 			CowNames.add("Dhol");
 			CowNames.add("Dun");
 			CowNames.add("Elsie");
 			CowNames.add("Flossie");
 			CowNames.add("Fuwch");
 			CowNames.add("Glas");
 			CowNames.add("Hathor");
 			CowNames.add("Hesat");
 			CowNames.add("Lo");
 			CowNames.add("Mehturt");
 			CowNames.add("Milka");
 			CowNames.add("Nandini");
 			CowNames.add("Sechat");
 			CowNames.add("Sentait");
 			CowNames.add("Taurus");
 			CowNames.add("Vache");
 			CowNames.add("Annabelle");
 			CowNames.add("Klara");
 			CowNames.add("June");
 			CowNames.add("Johnny");
 			CowNames.add("Kate");
 			CowNames.add("Boy");
 			CowNames.add("Dixie");
 			CowNames.add("Marigold");
 		}
 		this.getConfig().set("CowNames", CowNames);
 
 		IronGolemNames = getConfig().getStringList("IronGolemNames");
 		if (IronGolemNames.isEmpty()) {
 			IronGolemNames.add("Fat Tony");
 			IronGolemNames.add("Bruce");
 			IronGolemNames.add("Cranky");
 			IronGolemNames.add("Nutty");
 			IronGolemNames.add("Bolts");
 			IronGolemNames.add("Bouncer");
 			IronGolemNames.add("Bob");
 			IronGolemNames.add("Jim");
 			IronGolemNames.add("Defender");
 			IronGolemNames.add("Protector");
 			IronGolemNames.add("Cranky");
 			IronGolemNames.add("Rob");
 		}
 		this.getConfig().set("IronGolemNames", IronGolemNames);
 
 		MooshroomNames = getConfig().getStringList("MooshroomNames");
 		if (MooshroomNames.isEmpty()) {
 			MooshroomNames.add("Ultus");
 			MooshroomNames.add("Ulti");
 			MooshroomNames.add("Red");
 			MooshroomNames.add("Fungus");
 			MooshroomNames.add("Awesomnitetric");
 			MooshroomNames.add("Ermintruffle");
 
 		}
 		this.getConfig().set("MooshroomNames", MooshroomNames);
 
 		OcelotNames = getConfig().getStringList("OcelotNames");
 		if (OcelotNames.isEmpty()) {
 			OcelotNames.add("Tiddles");
 			OcelotNames.add("Tigger");
 			OcelotNames.add("Fluffy");
 			OcelotNames.add("Oscar");
 			OcelotNames.add("Max");
 			OcelotNames.add("Tiger");
 			OcelotNames.add("Sam");
 			OcelotNames.add("Misty");
 			OcelotNames.add("Simba");
 			OcelotNames.add("Coco");
 			OcelotNames.add("Chloe");
 			OcelotNames.add("Lucy");
 			OcelotNames.add("Missy");
 			OcelotNames.add("Puss");
 			OcelotNames.add("Smokey");
 			OcelotNames.add("Bella");
 			OcelotNames.add("Molly");
 			OcelotNames.add("Milo");
 			OcelotNames.add("Angel");
 			OcelotNames.add("Lily");
 			OcelotNames.add("Kitty");
 			OcelotNames.add("Ginger");
 			OcelotNames.add("Charlie");
 			OcelotNames.add("Poppy");
 			OcelotNames.add("Smudge");
 			OcelotNames.add("Millie");
 			OcelotNames.add("Daisy");
 			OcelotNames.add("Jasper");
 			OcelotNames.add("Felix");
 			OcelotNames.add("Alfie");
 			OcelotNames.add("Shadow");
 			OcelotNames.add("Minka");
 			OcelotNames.add("Moritz");
 			OcelotNames.add("Charly");
 			OcelotNames.add("Susi");
 			OcelotNames.add("Lisa");
 			OcelotNames.add("Blacky");
 			OcelotNames.add("Muschi");
 			OcelotNames.add("Minou");
 			OcelotNames.add("Grisou");
 			OcelotNames.add("Ti-Mine");
 			OcelotNames.add("Caramel");
 			OcelotNames.add("Mimi");
 			OcelotNames.add("Pacha");
 			OcelotNames.add("Charlotte");
 			OcelotNames.add("Minette");
 			OcelotNames.add("Chanel");
 			OcelotNames.add("Dama");
 			OcelotNames.add("Ancella");
 			OcelotNames.add("Briseide");
 			OcelotNames.add("Folco");
 			OcelotNames.add("Jack");
 			OcelotNames.add("Barsik");
 			OcelotNames.add("Boris");
 			OcelotNames.add("Vaska");
 		}
 		this.getConfig().set("OcelotNames", OcelotNames);
 
 		PigNames = getConfig().getStringList("PigNames");
 		if (PigNames.isEmpty()) {
 			PigNames.add("Porky");
 			PigNames.add("Piggy");
 			PigNames.add("Miss Piggy");
 			PigNames.add("Pumba");
 			PigNames.add("Wilbur");
 			PigNames.add("Hamm");
 			PigNames.add("Percy");
 			PigNames.add("Peppa");
 			PigNames.add("Ace");
 			PigNames.add("Fener");
 			PigNames.add("Freddy");
 			PigNames.add("Gaston");
 			PigNames.add("Josephine");
 			PigNames.add("Gouger");
 			PigNames.add("Snouter");
 			PigNames.add("Rooter");
 			PigNames.add("Tusker");
 			PigNames.add("Gryllus");
 			PigNames.add("Gub");
 			PigNames.add("Habeas");
 			PigNames.add("Hamilton");
 			PigNames.add("Hen");
 			PigNames.add("Henry");
 			PigNames.add("Jodie");
 			PigNames.add("Johnny");
 			PigNames.add("Jimmy");
 			PigNames.add("Jaimie");
 			PigNames.add("Jared");
 			PigNames.add("Jason");
 			PigNames.add("Justin");
 			PigNames.add("Karnac");
 			PigNames.add("Kimmy");
 			PigNames.add("Lester");
 			PigNames.add("Robinson");
 			PigNames.add("Mercy");
 			PigNames.add("Napoleon");
 			PigNames.add("Major");
 			PigNames.add("Olivia");
 			PigNames.add("Pequenious");
 			PigNames.add("Pig");
 			PigNames.add("Pigling");
 			PigNames.add("Pigoons");
 			PigNames.add("Poppleton");
 			PigNames.add("Positive");
 			PigNames.add("Piggy");
 			PigNames.add("Rumbl");
 			PigNames.add("Rwoar");
 			PigNames.add("Sam");
 			PigNames.add("Snowball");
 			PigNames.add("Squealer");
 			PigNames.add("Toby");
 			PigNames.add("Toot");
 			PigNames.add("Puddle");
 			PigNames.add("Zhu");
 		}
 		this.getConfig().set("PigNames", PigNames);
 
 		SheepNames = getConfig().getStringList("SheepNames");
 		if (SheepNames.isEmpty()) {
 			SheepNames.add("Dolly");
 		}
 		this.getConfig().set("SheepNames", SheepNames);
 
 		SquidNames = getConfig().getStringList("SquidNames");
 		if (SquidNames.isEmpty()) {
 			SquidNames.add("Squidlington");
 			SquidNames.add("Squidward");
 			SquidNames.add("Squiddles");
 			SquidNames.add("Kraken");
 			SquidNames.add("Ood");
 		}
 		this.getConfig().set("SquidNames", SquidNames);
 
 		WolfNames = getConfig().getStringList("WolfNames");
 		if (WolfNames.isEmpty()) {
 			WolfNames.add("Bach");
 			WolfNames.add("Wolfgang");
 			WolfNames.add("Max");
 			WolfNames.add("Buddy");
 			WolfNames.add("Molly");
 			WolfNames.add("Maggie");
 			WolfNames.add("Jake");
 			WolfNames.add("Daisy");
 			WolfNames.add("Lucy");
 			WolfNames.add("Rocky");
 			WolfNames.add("Bailey");
 			WolfNames.add("Sadie");
 			WolfNames.add("Ginger");
 			WolfNames.add("Chloe");
 			WolfNames.add("Sophie");
 			WolfNames.add("Buster");
 			WolfNames.add("Zoe");
 			WolfNames.add("Cody");
 			WolfNames.add("Charlie");
 			WolfNames.add("Bear");
 			WolfNames.add("Jack");
 			WolfNames.add("Princess");
 			WolfNames.add("Bella");
 			WolfNames.add("Angel");
 			WolfNames.add("Toby");
 			WolfNames.add("Lady");
 			WolfNames.add("Sasha");
 			WolfNames.add("Duke");
 			WolfNames.add("Lucky");
 			WolfNames.add("Abby");
 			WolfNames.add("Sam");
 			WolfNames.add("Roxy");
 			WolfNames.add("Missy");
 			WolfNames.add("Harley");
 			WolfNames.add("Brandy");
 			WolfNames.add("Coco");
 			WolfNames.add("Shadow");
 			WolfNames.add("Annie");
 			WolfNames.add("Katie");
 			WolfNames.add("Rusty");
 			WolfNames.add("Samantha");
 			WolfNames.add("Casey");
 			WolfNames.add("Murphy");
 			WolfNames.add("Gracie");
 			WolfNames.add("Sammy");
 			WolfNames.add("Bruno");
 			WolfNames.add("Maximus");
 			WolfNames.add("Luke");
 			WolfNames.add("Mickey");
 			WolfNames.add("Romeo");
 		}
 		this.getConfig().set("WolfNames", WolfNames);
 
 		BlazeNames = getConfig().getStringList("BlazeNames");
 		if (BlazeNames.isEmpty()) {
 			BlazeNames.add("Death");
 			BlazeNames.add("Firestarter");
 			BlazeNames.add("Burnie");
 			BlazeNames.add("Fire Breather");
 			BlazeNames.add("Firefly");
 		}
 		this.getConfig().set("BlazeNames", BlazeNames);
 
 		CaveSpiderNames = getConfig().getStringList("CaveSpiderNames");
 		if (CaveSpiderNames.isEmpty()) {
 			CaveSpiderNames.add("Black Widow");
 			CaveSpiderNames.add("Cellar");
 			CaveSpiderNames.add("Crab");
 			CaveSpiderNames.add("Brown Recluse");
 			CaveSpiderNames.add("Funnel Web");
 			CaveSpiderNames.add("Brown Recluse");
 			CaveSpiderNames.add("Man");
 			CaveSpiderNames.add("Tarantula");
 		}
 		this.getConfig().set("CaveSpiderNames", CaveSpiderNames);
 
 		CreeperNames = getConfig().getStringList("CreeperNames");
 		if (CreeperNames.isEmpty()) {
 			CreeperNames.add("JIMJUM - Green Edition");
 			CreeperNames.add("Mr. T... NT");
 			CreeperNames.add("Sir Creepalot");
 			CreeperNames.add("ssssSSSSSSS");
 			CreeperNames.add("Creeps");
 			CreeperNames.add("Creepy");
 			CreeperNames.add("Creepah");
 			CreeperNames.add("It'll be fine...");
 			CreeperNames.add("James");
 			CreeperNames.add("Bertie");
 			CreeperNames.add("Creeples");
 			CreeperNames.add("Come closer...");
 			CreeperNames.add("I just want a hug...");
 			CreeperNames.add("Boomer");
 			CreeperNames.add("Rick Asssssssstley");
 			CreeperNames.add("Da Bomb");
 			CreeperNames.add("I'm a cow really... come closer...");
 			CreeperNames.add("Cuddlesssssssss");
 			CreeperNames.add("Ssssssssteve");
 		}
 		this.getConfig().set("CreeperNames", CreeperNames);
 
 		EnderDragonNames = getConfig().getStringList("EnderdragonNames");
 		if (EnderDragonNames.isEmpty()) {
 			EnderDragonNames.add("GAHHHHHHD");
 			EnderDragonNames.add("Santa Claus");
 			EnderDragonNames.add("I'muh DRAYYGON");
 			EnderDragonNames.add("Your eventual cause of Death...");
 		}
 		this.getConfig().set("EnderDragonNames", EnderDragonNames);
 
 		EndermanNames = getConfig().getStringList("EndermanNames");
 		if (EndermanNames.isEmpty()) {
 			EndermanNames.add("Old Longlegs");
 			EndermanNames.add("Daddy longlegs");
 			EndermanNames.add("By the time you've read this I'm probably already chasing you...");
 			EndermanNames.add("No.");
 			EndermanNames.add("Gerroff muy laaarnd!");
 			EndermanNames.add("Stares");
 			EndermanNames.add("Look down...");
 			EndermanNames.add("End");
 			EndermanNames.add("Endy");
 			EndermanNames.add("Endus");
 			EndermanNames.add("Endus McEnd");
 			EndermanNames.add("The Face Of Death");
 			EndermanNames.add("Look into my eyes...");
 			EndermanNames.add("Bob");
 			EndermanNames.add("John");
 			EndermanNames.add("Ender Claus");
 			EndermanNames.add("Rick");
 			EndermanNames.add("Miles");
 			EndermanNames.add("Jeff");
 		}
 		this.getConfig().set("EndermanNames", EndermanNames);
 
 		GhastNames = getConfig().getStringList("GhastNames");
 		if (GhastNames.isEmpty()) {
 			GhastNames.add("Floating Paper Bag");
 			GhastNames.add("Hilda");
 			GhastNames.add("Testifighast");
 			GhastNames.add("Ghoul");
 			GhastNames.add("Ghost");
 			GhastNames.add("Ghast");
 		}
 		this.getConfig().set("GhastNames", GhastNames);
 
 		MagmaCubeNames = getConfig().getStringList("MagmaCubeNames");
 		if (MagmaCubeNames.isEmpty()) {
 			MagmaCubeNames.add("Flibbach");
 			MagmaCubeNames.add("Blachby");
 			MagmaCubeNames.add("Blarhb");
 			MagmaCubeNames.add("Flarrbble");
 			MagmaCubeNames.add("Flarrber");
 			MagmaCubeNames.add("Fleehb");
 			MagmaCubeNames.add("Blirrb");
 			MagmaCubeNames.add("Blarrb");
 			MagmaCubeNames.add("Bloochh");
 			MagmaCubeNames.add("Ignis");
 		}
 		this.getConfig().set("MagmaCubeNames", MagmaCubeNames);
 
 		SilverfishNames = getConfig().getStringList("SilverfishNames");
 		if (SilverfishNames.isEmpty()) {
 			SilverfishNames.add("Vario");
 			SilverfishNames.add("Mr. Jiggles");
 			SilverfishNames.add("Squiggly");
 			SilverfishNames.add("Wiggle");
 			SilverfishNames.add("Wigglesworth");
 			SilverfishNames.add("I'm neither a fish, nor silver.");
 		}
 		this.getConfig().set("SilverfishNames", SilverfishNames);
 
 		SlimeNames = getConfig().getStringList("SlimeNames");
 		if (SlimeNames.isEmpty()) {
 			SlimeNames.add("Flubber");
 			SlimeNames.add("Blobby");
 			SlimeNames.add("Blob");
 			SlimeNames.add("Bob the Blob");
 			SlimeNames.add("Flobble");
 			SlimeNames.add("Flubber");
 			SlimeNames.add("Floob");
 			SlimeNames.add("Blib");
 			SlimeNames.add("Blab");
 			SlimeNames.add("Blaab");
 			SlimeNames.add("Bloob");
 			SlimeNames.add("Blub");
 		}
 		this.getConfig().set("SlimeNames", SlimeNames);
 
 		SkeletonNames = getConfig().getStringList("SkeletonNames");
 		if (SkeletonNames.isEmpty()) {
 			SkeletonNames.add("Rattles");
 			SkeletonNames.add("Gunslinger");
 			SkeletonNames.add("Skeleton");
 			SkeletonNames.add("Skeletor");
 			SkeletonNames.add("Boney");
 			SkeletonNames.add("Bones");
 			SkeletonNames.add("You, after I've dealt with you.");
 			SkeletonNames.add("Parietal");
 			SkeletonNames.add("Occipital");
 			SkeletonNames.add("Sphenoid");
 			SkeletonNames.add("Ethmoid");
 			SkeletonNames.add("Mandible");
 			SkeletonNames.add("Maxilla");
 			SkeletonNames.add("Palatine");
 			SkeletonNames.add("Zygomatic");
 			SkeletonNames.add("Lacrimal");
 			SkeletonNames.add("Conchae");
 			SkeletonNames.add("Vomer");
 			SkeletonNames.add("Malleus");
 			SkeletonNames.add("Incus");
 			SkeletonNames.add("Stapes");
 			SkeletonNames.add("Hyoid");
 			SkeletonNames.add("Scapula");
 			SkeletonNames.add("Clavicle");
 			SkeletonNames.add("Sternum");
 			SkeletonNames.add("Gladiolus");
 			SkeletonNames.add("Manubruim");
 			SkeletonNames.add("Xiphoid");
 			SkeletonNames.add("Rib");
 			SkeletonNames.add("Lumbar");
 			SkeletonNames.add("Humerous");
 			SkeletonNames.add("Ulna");
 			SkeletonNames.add("Carpal");
 			SkeletonNames.add("Scaphoid");
 			SkeletonNames.add("Lunate");
 			SkeletonNames.add("Triquetrum");
 			SkeletonNames.add("Pisiform");
 			SkeletonNames.add("Trapezium");
 			SkeletonNames.add("Trapezoid");
 			SkeletonNames.add("Capitate");
 			SkeletonNames.add("Hamate");
 			SkeletonNames.add("Metacarpus");
 			SkeletonNames.add("Phalanges");
 			SkeletonNames.add("Sacrum");
 			SkeletonNames.add("Coccyx");
 			SkeletonNames.add("Coxae");
 			SkeletonNames.add("Femur");
 			SkeletonNames.add("Patella");
 			SkeletonNames.add("Tibia");
 			SkeletonNames.add("Fibula");
 			SkeletonNames.add("Tarsal");
 			SkeletonNames.add("Calcaneus");
 			SkeletonNames.add("Talus");
 			SkeletonNames.add("Navicular");
 			SkeletonNames.add("Cuneiform");
 			SkeletonNames.add("Cuboid");
 			SkeletonNames.add("Metatarsus");
 			SkeletonNames.add("Undead Archer");
 			SkeletonNames.add("Skeleton");
 		}
 		this.getConfig().set("SkeletonNames", SkeletonNames);
 
 		SnowGolemNames = getConfig().getStringList("SnowGolemNames");
 		if (SnowGolemNames.isEmpty()) {
 			SnowGolemNames.add("Frosty");
 			SnowGolemNames.add("Melty");
 			SnowGolemNames.add("Snowy");
 			SnowGolemNames.add("Harold");
 			SnowGolemNames.add("Bill");
 			SnowGolemNames.add("Donald");
 			SnowGolemNames.add("Bob");
 			SnowGolemNames.add("Jeff");
 			SnowGolemNames.add("Sam");
 			SnowGolemNames.add("Frost");
 			SnowGolemNames.add("Mr. Freeze");
 			SnowGolemNames.add("Flake");
 			SnowGolemNames.add("Vanilla Ice");
 			SnowGolemNames.add("Bluster");
 			SnowGolemNames.add("Frosty");
 		}
 		this.getConfig().set("SnowGolemNames", SnowGolemNames);
 
 		SpiderNames = getConfig().getStringList("SpiderNames");
 		if (SpiderNames.isEmpty()) {
 			SpiderNames.add("Black Widow");
 			SpiderNames.add("Cellar");
 			SpiderNames.add("Crab");
 			SpiderNames.add("Brown Recluse");
 			SpiderNames.add("Funnel Web");
 			SpiderNames.add("Brown Recluse");
 			SpiderNames.add("Man");
 			SpiderNames.add("Tarantula");
 		}
 		this.getConfig().set("SpiderNames", SpiderNames);
 
 		WitchNames = getConfig().getStringList("WitchNames");
 		if (WitchNames.isEmpty()) {
 			WitchNames.add("Wicked Witch of the West");
 			WitchNames.add("Grimhilde");
 			WitchNames.add("The Witch of Endor");
 			WitchNames.add("Maleficent");
 		}
 		this.getConfig().set("WitchNames", WitchNames);
 
 		WitherNames = getConfig().getStringList("WitherNames");
 		if (WitherNames.isEmpty()) {
 			WitherNames.add("Aleksander");
 			WitherNames.add("Vinter");
 			WitherNames.add("Wither");
 		}
 		this.getConfig().set("WitherNames", WitherNames);
 
 		ZombieNames = getConfig().getStringList("ZombieNames");
 		if (ZombieNames.isEmpty()) {
 			ZombieNames.add("Bob");
 			ZombieNames.add("Hilda");
 			ZombieNames.add("ZOMG");
 			ZombieNames.add("Zomble");
 			ZombieNames.add("Zomboy");
 			ZombieNames.add("Zombow");
 			ZombieNames.add("Zomble");
 			ZombieNames.add("Trololololol");
 			ZombieNames.add("BRAINZZZZ......");
 			ZombieNames.add("Brainnnnns.....");
 			ZombieNames.add("Uh");
 			ZombieNames.add("Guh");
 			ZombieNames.add("Uhhhhhh...");
 			ZombieNames.add("Sid");
 			ZombieNames.add("Uhh");
 			ZombieNames.add("UHHHHHHHHHH");
 			ZombieNames.add("Sid");
 			ZombieNames.add("Corpus");
 			ZombieNames.add("Gobblebrain");
 			ZombieNames.add("BLEEEURGHHH");
 			ZombieNames.add("Gobbler");
 			ZombieNames.add("Corpicus the Brainless");
 			ZombieNames.add("Ssmfox");
 			ZombieNames.add("Tristan");
 			ZombieNames.add("Draugr");
 		}
 		this.getConfig().set("ZombieNames", ZombieNames);
 
 		VillagerNames = getConfig().getStringList("VillagerNames");
 		if (VillagerNames.isEmpty()) {
 			VillagerNames.add("Harry");
 			VillagerNames.add("Jack");
 			VillagerNames.add("Oliver");
 			VillagerNames.add("Charlie");
 			VillagerNames.add("James");
 			VillagerNames.add("George");
 			VillagerNames.add("Thomas");
 			VillagerNames.add("Ethan");
 			VillagerNames.add("Jacob");
 			VillagerNames.add("William");
 			VillagerNames.add("Daniel");
 			VillagerNames.add("Joshua");
 			VillagerNames.add("Max");
 			VillagerNames.add("Noah");
 			VillagerNames.add("Alfie");
 			VillagerNames.add("Samuel");
 			VillagerNames.add("Dylan");
 			VillagerNames.add("Oscar");
 			VillagerNames.add("Lucas");
 			VillagerNames.add("Aidan");
 			VillagerNames.add("Isaac");
 			VillagerNames.add("Riley");
 			VillagerNames.add("Henry");
 			VillagerNames.add("Banjamin");
 			VillagerNames.add("Joseph");
 			VillagerNames.add("Alexander");
 			VillagerNames.add("Lewis");
 			VillagerNames.add("Leo");
 			VillagerNames.add("Tyler");
 			VillagerNames.add("Jayden");
 			VillagerNames.add("Zac");
 			VillagerNames.add("Freddie");
 			VillagerNames.add("Archie");
 			VillagerNames.add("Logan");
 			VillagerNames.add("Adam");
 			VillagerNames.add("Ryan");
 			VillagerNames.add("Nathan");
 			VillagerNames.add("Matthew");
 			VillagerNames.add("Sebastian");
 			VillagerNames.add("Jake");
 			VillagerNames.add("Toby");
 			VillagerNames.add("Alex");
 			VillagerNames.add("Luke");
 			VillagerNames.add("Liam");
 			VillagerNames.add("Harrison");
 			VillagerNames.add("David");
 			VillagerNames.add("Jamie");
 			VillagerNames.add("Edward");
 			VillagerNames.add("Luca");
 			VillagerNames.add("Elliot");
 			VillagerNames.add("Aaron");
 			VillagerNames.add("Finley");
 			VillagerNames.add("Michael");
 			VillagerNames.add("Zachary");
 			VillagerNames.add("Mason");
 			VillagerNames.add("Sam");
 			VillagerNames.add("Muhammad");
 			VillagerNames.add("Connor");
 			VillagerNames.add("Ben");
 			VillagerNames.add("Reuben");
 			VillagerNames.add("Theo");
 			VillagerNames.add("Rhys");
 			VillagerNames.add("Arthur");
 			VillagerNames.add("Caleb");
 			VillagerNames.add("Dexter");
 			VillagerNames.add("Rory");
 			VillagerNames.add("Jenson");
 			VillagerNames.add("Evan");
 			VillagerNames.add("Gabriel");
 			VillagerNames.add("Ewan");
 			VillagerNames.add("Callum");
 			VillagerNames.add("Seth");
 			VillagerNames.add("Felix");
 			VillagerNames.add("Austin");
 			VillagerNames.add("Owen");
 			VillagerNames.add("Leon");
 			VillagerNames.add("Cameron");
 			VillagerNames.add("Jude");
 			VillagerNames.add("Harley");
 			VillagerNames.add("Blake");
 			VillagerNames.add("Harvey");
 			VillagerNames.add("Tom");
 			VillagerNames.add("Hugo");
 			VillagerNames.add("Finn");
 			VillagerNames.add("Bobby");
 			VillagerNames.add("Hayden");
 			VillagerNames.add("Kyle");
 			VillagerNames.add("Jasper");
 			VillagerNames.add("Tommy");
 			VillagerNames.add("Eli");
 			VillagerNames.add("Kian");
 			VillagerNames.add("Andrew");
 			VillagerNames.add("John");
 			VillagerNames.add("Louie");
 			VillagerNames.add("Dominic");
 			VillagerNames.add("Joe");
 			VillagerNames.add("Elijah");
 			VillagerNames.add("Kai");
 			VillagerNames.add("Frankie");
 			VillagerNames.add("Stanley");
 			VillagerNames.add("Amelia");
 			VillagerNames.add("Lily");
 			VillagerNames.add("Emily");
 			VillagerNames.add("Sophia");
 			VillagerNames.add("Isabelle");
 			VillagerNames.add("Sophie");
 			VillagerNames.add("Olivia");
 			VillagerNames.add("Jessica");
 			VillagerNames.add("Chloe");
 			VillagerNames.add("Mia");
 			VillagerNames.add("Isla");
 			VillagerNames.add("Isabella");
 			VillagerNames.add("Ava");
 			VillagerNames.add("Charlotte");
 			VillagerNames.add("Grace");
 			VillagerNames.add("Evie");
 			VillagerNames.add("Poppy");
 			VillagerNames.add("Lucy");
 			VillagerNames.add("Ella");
 			VillagerNames.add("Holly");
 			VillagerNames.add("Emma");
 			VillagerNames.add("Molly");
 			VillagerNames.add("Annabelle");
 			VillagerNames.add("Erin");
 			VillagerNames.add("Freya");
 			VillagerNames.add("Ruby");
 			VillagerNames.add("Scarlett");
 			VillagerNames.add("Alice");
 			VillagerNames.add("Layla");
 			VillagerNames.add("Hannah");
 			VillagerNames.add("Eva");
 			VillagerNames.add("Imogen");
 			VillagerNames.add("Millie");
 			VillagerNames.add("Daisy");
 			VillagerNames.add("Abigail");
 			VillagerNames.add("Amy");
 			VillagerNames.add("Zoe");
 			VillagerNames.add("Megan");
 			VillagerNames.add("Maisie");
 			VillagerNames.add("Phoebe");
 			VillagerNames.add("Maya");
 			VillagerNames.add("Anna");
 			VillagerNames.add("Eliza");
 			VillagerNames.add("Caitlin");
 			VillagerNames.add("Amelie");
 			VillagerNames.add("Jasmine");
 			VillagerNames.add("Florence");
 			VillagerNames.add("Sienna");
 			VillagerNames.add("Madison");
 			VillagerNames.add("Elanor");
 			VillagerNames.add("Darcey");
 			VillagerNames.add("Lola");
 			VillagerNames.add("Elizabeth");
 			VillagerNames.add("Leah");
 			VillagerNames.add("Matilda");
 			VillagerNames.add("Summer");
 			VillagerNames.add("Elsie");
 			VillagerNames.add("Ellie");
 			VillagerNames.add("Zara");
 			VillagerNames.add("Rosie");
 			VillagerNames.add("Kayla");
 			VillagerNames.add("Esme");
 			VillagerNames.add("Amber");
 			VillagerNames.add("Georgia");
 			VillagerNames.add("Bethany");
 			VillagerNames.add("Rose");
 			VillagerNames.add("Evelyn");
 			VillagerNames.add("Lexi");
 			VillagerNames.add("Niamh");
 			VillagerNames.add("Katie");
 			VillagerNames.add("Alyssa");
 			VillagerNames.add("Lauren");
 			VillagerNames.add("Heidi");
 			VillagerNames.add("Gracie");
 			VillagerNames.add("Skye");
 			VillagerNames.add("Willow");
 			VillagerNames.add("Faith");
 			VillagerNames.add("Beth");
 			VillagerNames.add("Alexandra");
 			VillagerNames.add("Iris");
 			VillagerNames.add("Harriet");
 			VillagerNames.add("Violet");
 			VillagerNames.add("Lara");
 			VillagerNames.add("Martha");
 			VillagerNames.add("Rebecca");
 			VillagerNames.add("Seren");
 			VillagerNames.add("Gabriella");
 			VillagerNames.add("Tilly");
 			VillagerNames.add("Naomi");
 			VillagerNames.add("Sarah");
 			VillagerNames.add("Clara");
 			VillagerNames.add("Nicole");
 			VillagerNames.add("Elise");
 			VillagerNames.add("Mila");
 			VillagerNames.add("Annie");
 			VillagerNames.add("Sara");
 			VillagerNames.add("Bella");
 			VillagerNames.add("Francesca");
 			VillagerNames.add("Elena");
 			VillagerNames.add("Libby");
 			VillagerNames.add("Robert");
 			VillagerNames.add("Rick");
 			VillagerNames.add("Wout");
 			VillagerNames.add("Taylor");
 			VillagerNames.add("Christopher");
 		}
 		this.getConfig().set("VillagerNames", VillagerNames);
 
 		ZombiePigmanNames = getConfig().getStringList("ZombiePigmanNames");
 		if (ZombiePigmanNames.isEmpty()) {
 			ZombiePigmanNames.add("Zoink");
 			ZombiePigmanNames.add("Gurrrrrr");
 			ZombiePigmanNames.add("Bernie");
 			ZombiePigmanNames.add("Jim");
 			ZombiePigmanNames.add("Bob");
 			ZombiePigmanNames.add("Grghhhh");
 			ZombiePigmanNames.add("Guh");
 		}
 		this.getConfig().set("ZombiePigmanNames", ZombiePigmanNames);
 		saveConfig();
 	}
 
 	@EventHandler
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		EntityType mob = event.getEntityType();
 		PutColorCodesIntoList();
 
 		if (mob == EntityType.BAT) {
 			int index = createRandom(0, BatNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + BatNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.CHICKEN) {
 			int index = createRandom(0, ChickenNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + ChickenNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.COW) {
 			int index = createRandom(0, CowNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + CowNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.IRON_GOLEM) {
 			int index = createRandom(0, IronGolemNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + IronGolemNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.MUSHROOM_COW) {
 			int index = createRandom(0, MooshroomNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + MooshroomNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.OCELOT) {
 			int index = createRandom(0, OcelotNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + OcelotNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.PIG) {
 			int index = createRandom(0, PigNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + PigNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.SHEEP) {
 			int index = createRandom(0, SheepNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + SheepNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.SQUID) {
 			int index = createRandom(0, SquidNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + SquidNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.WOLF) {
 			int index = createRandom(0, WolfNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + WolfNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.BLAZE) {
 			int index = createRandom(0, BlazeNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + BlazeNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.CAVE_SPIDER) {
 			int index = createRandom(0, CaveSpiderNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + CaveSpiderNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.CREEPER) {
 			int index = createRandom(0, CreeperNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + CreeperNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.ENDER_DRAGON) {
 			int index = createRandom(0, EnderDragonNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + EnderDragonNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.ENDERMAN) {
 			int index = createRandom(0, EndermanNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + EndermanNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.GHAST) {
 			int index = createRandom(0, GhastNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + GhastNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.MAGMA_CUBE) {
 			int index = createRandom(0, MagmaCubeNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + MagmaCubeNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.SILVERFISH) {
 			int index = createRandom(0, SilverfishNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + SilverfishNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.SKELETON) {
 			int index = createRandom(0, SkeletonNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + SkeletonNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.SLIME) {
 			int index = createRandom(0, SlimeNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + SlimeNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.SNOWMAN) {
 			int index = createRandom(0, SnowGolemNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + SnowGolemNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.SPIDER) {
 			int index = createRandom(0, SpiderNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + SpiderNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.WITCH) {
 			int index = createRandom(0, WitchNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + WitchNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.WITHER) {
 			int index = createRandom(0, WitherNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + WitherNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.ZOMBIE) {
 			int index = createRandom(0, ZombieNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + ZombieNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.VILLAGER) {
 			int index = createRandom(0, VillagerNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + VillagerNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 		if (mob == EntityType.PIG_ZOMBIE) {
 			int index = createRandom(0, ZombiePigmanNames.size());
 			event.getEntity().setCustomName(RandomColorMethod() + ZombiePigmanNames.get(index));
 			event.getEntity().setCustomNameVisible(true);
 		}
 
 	}
 
 	public int createRandom(int min, int max) {
 		max = max - 1;
 		Random rand = new Random();
 		return rand.nextInt(max - min + 1) + min;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender commandsender, Command command, String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("namer")) {
 			if (args.length == 1) {
 				if (args[0].equalsIgnoreCase("help")) {
 					commandsender.sendMessage("*****************" + ChatColor.DARK_RED + "Auto" + ChatColor.BLUE + "Mob" + ChatColor.YELLOW + "Namer " + ChatColor.RESET + "*****************");
 					commandsender.sendMessage(ChatColor.AQUA + "The Commands are:");
 					commandsender.sendMessage(ChatColor.GREEN + "/namer add [MobType] [Name (Use underscores for spaces!)]");
 					commandsender.sendMessage(ChatColor.GREEN + "/namer remove [MobType] [Name (Use underscores for spaces!)]");
 					commandsender.sendMessage(ChatColor.GREEN + "/namer ColorOff  -  Turns colors off for newly spawned mobs");
 					commandsender.sendMessage(ChatColor.GREEN + "/namer ColorOn  -  Turns color on for newly spawned mobs");
 					commandsender.sendMessage(ChatColor.GREEN + "/namer clear [MobType]  -  Removes all names for this Mob Type (Leaving just the mob type as its name which you can remove later)");
 					commandsender.sendMessage("***************** That's it! *****************");
 				}
 				if (args[0].equalsIgnoreCase("add")) {
 					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer add [MobType] [Name]");
 				}
 				if (args[0].equalsIgnoreCase("remove")) {
 					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer remove [MobType] [Name]");
 				}
 				if (args[0].equalsIgnoreCase("ColorOff")) {
 					VariableStuff.NoColor = true;
 					commandsender.sendMessage(ChatColor.GREEN + "Mob name colors have been turned off!");
 				}
 				if (args[0].equalsIgnoreCase("ColorOn")) {
 					VariableStuff.NoColor = false;
 					commandsender.sendMessage(ChatColor.GREEN + "Mob name colors are now on!");
 				}
 				if (args[0].equalsIgnoreCase("clear")) {
 					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer clear [MobType]");
 				}
 
 			}
 			if (args.length == 2) {
 				if (args[0].equalsIgnoreCase("add")) {
 					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer add [MobType] [Name]");
 				}
 				if (args[0].equalsIgnoreCase("remove")) {
 					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer remove [MobType] [Name]");
 				}
 				if (args[0].equalsIgnoreCase("clear")) {
 					if (args[1].equalsIgnoreCase("Bat")) {
 						BatNames.clear();
 						BatNames.add("Bat");
 						commandsender.sendMessage(ChatColor.GREEN + "Bat names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("Chicken")) {
 						ChickenNames.clear();
 						ChickenNames.add("Chicken");
 						commandsender.sendMessage(ChatColor.GREEN + "Chicken names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("Cow")) {
 						CowNames.clear();
 						CowNames.add("Cow");
 						commandsender.sendMessage(ChatColor.GREEN + "Cow names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("IronGolem")) || (args[1].equalsIgnoreCase("Iron_Golem")) || (args[1].equalsIgnoreCase("Villager_Golem")) || (args[1].equalsIgnoreCase("VillagerGolem"))) {
 						IronGolemNames.clear();
 						IronGolemNames.add("Iron Golem");
 						commandsender.sendMessage(ChatColor.GREEN + "Iron Golem names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Mooshroom")) || (args[1].equalsIgnoreCase("MushroomCow")) || (args[1].equalsIgnoreCase("Mushroom_Cow"))) {
 						MooshroomNames.clear();
 						MooshroomNames.add("Mooshroom");
 						commandsender.sendMessage(ChatColor.GREEN + "Mooshroom names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Ocelot")) || (args[1].equalsIgnoreCase("Cat"))) {
 						OcelotNames.clear();
 						OcelotNames.add("Ocelot");
 						commandsender.sendMessage(ChatColor.GREEN + "Mooshroom names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Pig"))) {
 						PigNames.clear();
 						PigNames.add("Pig");
 						commandsender.sendMessage(ChatColor.GREEN + "Pig names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Sheep"))) {
 						SheepNames.clear();
 						SheepNames.add("Sheep");
 						commandsender.sendMessage(ChatColor.GREEN + "Sheep names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Squid"))) {
 						SquidNames.clear();
 						SquidNames.add("Squid");
 						commandsender.sendMessage(ChatColor.GREEN + "Squid names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Wolf")) || (args[1].equalsIgnoreCase("Dog"))) {
 						WolfNames.clear();
 						WolfNames.add("Wolf");
 						commandsender.sendMessage(ChatColor.GREEN + "Wolf names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Blaze"))) {
 						BlazeNames.clear();
 						BlazeNames.add("Blaze");
 						commandsender.sendMessage(ChatColor.GREEN + "Blaze names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("CaveSpider")) || (args[1].equalsIgnoreCase("Cave_Spider"))) {
 						CaveSpiderNames.clear();
 						CaveSpiderNames.add("Cave Spider");
 						commandsender.sendMessage(ChatColor.GREEN + "Cave Spider names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Creeper"))) {
 						CreeperNames.clear();
 						CreeperNames.add("Creeper");
 						commandsender.sendMessage(ChatColor.GREEN + "Creeper names have been cleared!");
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("EnderDragon")) {
 						EnderDragonNames.clear();
 						EnderDragonNames.add("EnderDragon");
 						commandsender.sendMessage(ChatColor.GREEN + "EnderDragon names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Enderman")) {
 						EndermanNames.clear();
 						EndermanNames.add("Enderman");
 						commandsender.sendMessage(ChatColor.GREEN + "Enderman names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Ghast")) {
 						GhastNames.clear();
 						GhastNames.add("Ghast");
 						commandsender.sendMessage(ChatColor.GREEN + "Ghast names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("MagmaCube") || args[1].equalsIgnoreCase("LavaCube") || args[1].equalsIgnoreCase("LavaSlime") || args[1].equalsIgnoreCase("MagmaSlime")) {
 						MagmaCubeNames.clear();
 						MagmaCubeNames.add("MagmaCube");
 						commandsender.sendMessage(ChatColor.GREEN + "MagmaCube names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Silverfish")) {
 						SilverfishNames.clear();
 						SilverfishNames.add("Silverfish");
 						commandsender.sendMessage(ChatColor.GREEN + "Silverfish names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Skeleton")) {
 						SkeletonNames.clear();
 						SkeletonNames.add("Skeleton");
 						commandsender.sendMessage(ChatColor.GREEN + "Skeleton names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Slime")) {
 						SlimeNames.clear();
 						SlimeNames.add("Slime");
 						commandsender.sendMessage(ChatColor.GREEN + "Slime names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("SnowGolem") || args[1].equalsIgnoreCase("SnowMan")) {
 						SnowGolemNames.clear();
 						SnowGolemNames.add("Slime");
 						commandsender.sendMessage(ChatColor.GREEN + "SnowGolem names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Spider")) {
 						SpiderNames.clear();
 						SpiderNames.add("Spider");
 						commandsender.sendMessage(ChatColor.GREEN + "Spider names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Witch")) {
 						WitchNames.clear();
 						WitchNames.add("Witch");
 						commandsender.sendMessage(ChatColor.GREEN + "Witch names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Wither")) {
 						WitherNames.clear();
 						WitherNames.add("Wither");
 						commandsender.sendMessage(ChatColor.GREEN + "Wither names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Zombie")) {
 						ZombieNames.clear();
 						ZombieNames.add("Zombie");
 						commandsender.sendMessage(ChatColor.GREEN + "Zombie names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("ZombiePigman") || args[1].equalsIgnoreCase("Pigman")) {
 						ZombiePigmanNames.clear();
 						ZombiePigmanNames.add("ZombiePigman");
 						commandsender.sendMessage(ChatColor.GREEN + "ZombiePigman names have been cleared!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Villager")) {
 						VillagerNames.clear();
 						VillagerNames.add("Villager");
 						commandsender.sendMessage(ChatColor.GREEN + "Villager names have been cleared!");
 						saveConfig();
 						return true;
 					}
 				}
 			}
 			if (args.length == 3){
 				if (args[0].equalsIgnoreCase("add")){
 					if (args[1].equalsIgnoreCase("Bat")) {
 						BatNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Bat Names!");
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("Chicken")) {
 						ChickenNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Chicken Names!");
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("Cow")) {
 						CowNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Cow Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("IronGolem")) || (args[1].equalsIgnoreCase("Iron_Golem")) || (args[1].equalsIgnoreCase("Villager_Golem")) || (args[1].equalsIgnoreCase("VillagerGolem"))) {
 						IronGolemNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Iron Golem Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Mooshroom")) || (args[1].equalsIgnoreCase("MushroomCow")) || (args[1].equalsIgnoreCase("Mushroom_Cow"))) {
 						MooshroomNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Mooshroom Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Ocelot")) || (args[1].equalsIgnoreCase("Cat"))) {
 						OcelotNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Ocelot Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Pig"))) {
 						PigNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Pig Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Sheep"))) {
 						SheepNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Sheep Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Squid"))) {
 						SquidNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Squid Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Wolf")) || (args[1].equalsIgnoreCase("Dog"))) {
 						WolfNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Wolf Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Blaze"))) {
 						BlazeNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Blaze Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("CaveSpider")) || (args[1].equalsIgnoreCase("Cave_Spider"))) {
 						CaveSpiderNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Cave Spider Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Creeper"))) {
 						CreeperNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Creeper Names!");
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("EnderDragon")) {
 						EnderDragonNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Ender Dragon Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Enderman")) {
 						EndermanNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Enderman Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Ghast")) {
 						GhastNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Ghast Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("MagmaCube") || args[1].equalsIgnoreCase("LavaCube") || args[1].equalsIgnoreCase("LavaSlime") || args[1].equalsIgnoreCase("MagmaSlime")) {
 						MagmaCubeNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Magma Cube Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Silverfish")) {
 						SilverfishNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Silverfish Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Skeleton")) {
 						SkeletonNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Skeleton Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Slime")) {
 						SlimeNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Slime Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("SnowGolem") || args[1].equalsIgnoreCase("SnowMan")) {
 						SnowGolemNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Snow Golem Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Spider")) {
 						SpiderNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Spider Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Witch")) {
 						WitchNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Witch Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Wither")) {
 						WitherNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Wither Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Zombie")) {
 						ZombieNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Zombie Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("ZombiePigman") || args[1].equalsIgnoreCase("Pigman")) {
 						ZombiePigmanNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Zombie Pigman Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Villager")) {
 						VillagerNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Villager Names!");
 						saveConfig();
 						return true;
 					}
 					
 				}
 				
 				
 				
 				if (args[0].equalsIgnoreCase("remove")){
 					if (args[1].equalsIgnoreCase("Bat")) {
 						for (String name:BatNames){
 							if (name.equalsIgnoreCase(args[2])){
 								BatNames.remove(args[2]);
 								commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been removed from Bat Names!");
 							}else{
 								commandsender.sendMessage(ChatColor.GREEN + "That name isn't in Bat Names. (You may have misspelled it)");
 							}
 						}
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("Chicken")) {
 						for (String name:ChickenNames){
 							if (name.equalsIgnoreCase(args[2])){
 								ChickenNames.remove(args[2]);
 								commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been removed from Chicken Names!");
 							}else{
 								commandsender.sendMessage(ChatColor.GREEN + "That name isn't in Chicken Names. (You may have misspelled it)");
 							}
 						}
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("Cow")) {
 						CowNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Cow Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("IronGolem")) || (args[1].equalsIgnoreCase("Iron_Golem")) || (args[1].equalsIgnoreCase("Villager_Golem")) || (args[1].equalsIgnoreCase("VillagerGolem"))) {
 						IronGolemNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Iron Golem Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Mooshroom")) || (args[1].equalsIgnoreCase("MushroomCow")) || (args[1].equalsIgnoreCase("Mushroom_Cow"))) {
 						MooshroomNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Mooshroom Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Ocelot")) || (args[1].equalsIgnoreCase("Cat"))) {
 						OcelotNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Ocelot Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Pig"))) {
 						PigNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Pig Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Sheep"))) {
 						SheepNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Sheep Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Squid"))) {
 						SquidNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Squid Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Wolf")) || (args[1].equalsIgnoreCase("Dog"))) {
 						WolfNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Wolf Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Blaze"))) {
 						BlazeNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Blaze Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("CaveSpider")) || (args[1].equalsIgnoreCase("Cave_Spider"))) {
 						CaveSpiderNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Cave Spider Names!");
 						saveConfig();
 						return true;
 					}
 					if ((args[1].equalsIgnoreCase("Creeper"))) {
 						CreeperNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Creeper Names!");
 						saveConfig();
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("EnderDragon")) {
 						EnderDragonNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Ender Dragon Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Enderman")) {
 						EndermanNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Enderman Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Ghast")) {
 						GhastNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Ghast Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("MagmaCube") || args[1].equalsIgnoreCase("LavaCube") || args[1].equalsIgnoreCase("LavaSlime") || args[1].equalsIgnoreCase("MagmaSlime")) {
 						MagmaCubeNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Magma Cube Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Silverfish")) {
 						SilverfishNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Silverfish Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Skeleton")) {
 						SkeletonNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Skeleton Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Slime")) {
 						SlimeNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Slime Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("SnowGolem") || args[1].equalsIgnoreCase("SnowMan")) {
 						SnowGolemNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Snow Golem Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Spider")) {
 						SpiderNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Spider Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Witch")) {
 						WitchNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Witch Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Wither")) {
 						WitherNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Wither Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Zombie")) {
 						ZombieNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Zombie Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("ZombiePigman") || args[1].equalsIgnoreCase("Pigman")) {
 						ZombiePigmanNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Zombie Pigman Names!");
 						saveConfig();
 						return true;
 					}
 
 					if (args[1].equalsIgnoreCase("Villager")) {
 						VillagerNames.add(args[2]);
 						commandsender.sendMessage(ChatColor.AQUA + "'" + args[2] + "'" + ChatColor.GREEN + " has been added to Villager Names!");
 						saveConfig();
 						return true;
 				}
 			}
 		}
 		return false;
 	}

 }
