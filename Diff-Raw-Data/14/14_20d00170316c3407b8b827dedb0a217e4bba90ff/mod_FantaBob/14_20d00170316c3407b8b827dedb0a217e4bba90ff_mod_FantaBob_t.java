 package net.minecraft.src;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 
 import net.minecraft.client.Minecraft;
 
 public class mod_FantaBob extends BaseMod
 {   
     public String getVersion()
 	{
 		return "1.2.5";
 	}
     
     /*
      * IDs:
      * items: 400  407
      * blocs: 243
      * achievements: 887  901
      */
 
 	public void load() 
 	{
 		propsLocation = new StringBuilder().append(Minecraft.getMinecraftDir()).append("/").append("mod_FantaBob.props").toString();
 		try {
 			props = loadProperties(propsLocation);
 		} catch (Exception e) {
 			e.printStackTrace();
 			try {
 				createPropsFile(propsLocation);
 				props = loadProperties(propsLocation);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
		
 		//enregistrement des mobs
 		ModLoader.registerEntityID(EntityBob.class, "Boblennon", 101, 204, 13613421);
         ModLoader.registerEntityID(EntityFanta.class, "TheFantasio974", 102, 0, 16777215);
         ModLoader.registerEntityID(EntityJeanKevin.class, "JeanKevin", 103, 44975, 11762530);
         ModLoader.registerEntityID(EntityBotlennon.class, "Botlennon", 104, 204, 16777215);
         
         //localisation des noms des mobs, pour les oeufs
         ModLoader.addLocalization("entity.Boblennon.name", "Boblennon");
         ModLoader.addLocalization("entity.TheFantasio974.name", "TheFantasio974");
         ModLoader.addLocalization("entity.JeanKevin.name", "Jean-Kvin");
         ModLoader.addLocalization("entity.Botlennon.name", "Botlennon");
         
         //fix pour mc 1.3: impossible d'utiliser ModLoader.getUniqueEntityId(), donc on utilise des ids fixes
         ModLoader.addEntityTracker(this, EntityBob.class, 101, 64, 2, true);
         ModLoader.addEntityTracker(this, EntityFanta.class, 102, 64, 2, true);
         ModLoader.addEntityTracker(this, EntityJeanKevin.class, 103, 64, 2, true);
         ModLoader.addEntityTracker(this, EntityBotlennon.class, 104, 64, 2, true);
         
         //paramtrage du spawn des mobs
         if(getBooleanProp("boblennon.spawn"))
         	ModLoader.addSpawn(EntityBob.class, getIntegerProp("boblennon.spawn.rate"), 1, 2, EnumCreatureType.creature);
         if(getBooleanProp("fanta.spawn"))
         	ModLoader.addSpawn(EntityFanta.class, getIntegerProp("fanta.spawn.rate"), 1, 2, EnumCreatureType.creature);
         if(getBooleanProp("jeankevin.spawn"))
         	ModLoader.addSpawn(EntityJeanKevin.class, getIntegerProp("jeankevin.spawn.rate"), 2, 3, EnumCreatureType.creature);
         if(getBooleanProp("botlennon.spawn"))
         	ModLoader.addSpawn(EntityBotlennon.class, getIntegerProp("botlennon.spawn.rate"), 1, 3, EnumCreatureType.monster);
         
         //ajout des armures personnalises
         ModLoader.addArmor("fantabob");
         ModLoader.addArmor("fantabob2");
         
         //instanciation des objets
         fantaGlasses = (new ItemArmor(144, EnumArmorMaterial.CLOTH, 5, 0))
         	.setItemName("fantaGlasses")
         	.setMaxStackSize(64)
         	.setIconIndex(ModLoader.addOverride("/gui/items.png", "/fantabob/glasses.png"))
         	.setTabToDisplayOn(CreativeTabs.tabDeco);
         cobbleTie = (new ItemArmor(145, EnumArmorMaterial.CLOTH, 5, 1))
         	.setItemName("cobbleTie")
         	.setIconIndex(ModLoader.addOverride("/gui/items.png", "/fantabob/cobble_tie.png"))
         	.setTabToDisplayOn(CreativeTabs.tabDeco);
         toothBrush = (new ItemObsidianToothBrush(402))
         	.setItemName("toothBrush")
         	.setIconIndex(ModLoader.addOverride("/gui/items.png", "/fantabob/obsi_toothbrush.png"));
         stampCollection = (new Item(403))
     		.setItemName("stampCollection")
     		.setIconIndex(ModLoader.addOverride("/gui/items.png", "/fantabob/stamp_collection.png"))
         	.setTabToDisplayOn(CreativeTabs.tabDeco);
         bambooSword = (new ItemSword(404, EnumToolMaterial.WOOD))
         	.setItemName("bambooSword")
         	.setIconIndex(ModLoader.addOverride("/gui/items.png", "/fantabob/bamboo_sword.png"))
         	.setTabToDisplayOn(CreativeTabs.tabCombat);
         hairPotion = (new ItemArmor(149, EnumArmorMaterial.CLOTH, 6, 0))
     		.setItemName("hairPotion")
     		.setIconIndex(ModLoader.addOverride("/gui/items.png", "/fantabob/hair_potion.png"))
         	.setTabToDisplayOn(CreativeTabs.tabMisc);
         bouse = new BlockBouse(243, 0)
         	.setHardness(0.6F)
         	.setResistance(4.0F)
         	.setBlockName("bouse")
         	.setStepSound(new StepSound("bouse", 3.0F, 1.0F));
         bouse.blockIndexInTexture = ModLoader.addOverride("/terrain.png", "/fantabob/bouse.png");
         magabondChop = new ItemFood(406, 4, 0.1F, true)
         	.setPotionEffect(Potion.confusion.id, 20, 0, 0.8F)
         	.setItemName("magabondChop")
         	.setMaxStackSize(64)
         	.setIconIndex(ModLoader.addOverride("/gui/items.png", "/fantabob/magabond_chop.png"))
         	.setTabToDisplayOn(CreativeTabs.tabFood);
         magabondRecord = (new ItemRecordFantaBob(407, "Magabond", "ATE BITS"))
         	.setIconIndex(ModLoader.addOverride("/gui/items.png", "/fantabob/magabond_record.png"))
         	.setItemName("magabondRecord");
         
         //enregistrement des objets spciaux
         ModLoader.registerEntityID(ItemObsidianToothBrush.class, "ToothBrush", ModLoader.getUniqueEntityId());
         
         //enregistrement des blocs
         ModLoader.registerBlock(bouse);
         
         //recettes de craft
         ModLoader.addRecipe(new ItemStack(fantaGlasses, 1), new Object[]
             {"# #", "# #", "X#X", Character.valueOf('#'), Item.stick, Character.valueOf('X'), Block.glass});
         ModLoader.addRecipe(new ItemStack(cobbleTie, 1), new Object[]
             {"#", "X", "X", Character.valueOf('#'), Item.silk, Character.valueOf('X'), Block.cobblestone});
         ModLoader.addRecipe(new ItemStack(toothBrush, 1), new Object[]
             {"#X", "# ", "# ", Character.valueOf('#'), Block.obsidian, Character.valueOf('X'), Block.cloth});
         ModLoader.addRecipe(new ItemStack(stampCollection, 1), new Object[]
             {"#XX", "#XX", "#XX", Character.valueOf('#'), Item.book, Character.valueOf('X'), Item.paper});
         ModLoader.addRecipe(new ItemStack(bambooSword, 1), new Object[]
             {"#", "#", "X", Character.valueOf('#'), Item.reed, Character.valueOf('X'), Item.stick});
         ModLoader.addRecipe(new ItemStack(hairPotion, 1), new Object[]
             {" # ", "#X#", " # ", Character.valueOf('#'), Block.glass, Character.valueOf('X'), Item.redstone});
         ModLoader.addRecipe(new ItemStack(bouse, 3), new Object[]
             {"X", "#", Character.valueOf('#'), Block.dirt, Character.valueOf('X'), Item.bucketWater});
         ModLoader.addRecipe(new ItemStack(magabondRecord, 1), new Object[]
             {" # ", "#X#", " # ", Character.valueOf('#'), magabondChop, Character.valueOf('X'), Item.redstone});
         
         //ajout du nom des blocs/objets
         ModLoader.addName(fantaGlasses, "Lunettes de Fantasio");
         ModLoader.addName(cobbleTie, "Cravate en cobble");
         ModLoader.addName(toothBrush, "Brosse  dents en obsidienne");
         ModLoader.addName(stampCollection, "Collection de timbres");
         ModLoader.addName(bambooSword, "Epe en bambou");
         ModLoader.addName(hairPotion, "Lotion capillaire de Papy Lennon");
         ModLoader.addName(bouse, "Bouse");
         ModLoader.addName(magabondChop, "Ctelette de magabond");
         ModLoader.addName(magabondRecord, "Disque");
         
         //ajout des achievements
         installModAch = new Achievement(887, "installModAch", -2, 0, Item.paper, null).registerAchievement();
         killBobAch = new Achievement(888, "killBobAch", -2, 3, Block.cobblestone, installModAch).registerAchievement();
         killFantaAch = new Achievement(889, "killFantaAch", -2, -1, Item.swordWood, installModAch).registerAchievement();
         killJeanKevinAch = new Achievement(890, "killJeanKevinAch", -2, -4, Item.arrow, installModAch).registerAchievement();
         floodAch = new Achievement(891, "floodAch", -4, 0, Item.bucketLava, installModAch).registerAchievement();
         killBotlennonAch = new Achievement(892, "killBotlennonAch", -2, 1, Item.ingotIron, installModAch).registerAchievement();
         getBouseAch = new Achievement(893, "getBouseAch", -5, 2, bouse, installModAch).registerAchievement();
         brushTeethAch = new Achievement(894, "brushTeethAch", 0, -5, toothBrush, installModAch).registerAchievement();
         getHairPotionAch = new Achievement(895, "getHairPotionAch", -3, 4, hairPotion, installModAch).registerAchievement();
         getStampCollectionAch = new Achievement(896, "getStampCollectionAch", 1, -2, stampCollection, installModAch).registerAchievement();
         getCobbleTieAch = new Achievement(897, "getCobbleTieAch", -7, -6, cobbleTie, installModAch).registerAchievement();
         getMagabondChopAch = new Achievement(898, "getMagabondChopAch", -5, -3, magabondChop, killJeanKevinAch).registerAchievement();
         craftMagabondRecAch = new Achievement(899, "craftMagabondRecAch", -5, -5, magabondRecord, getMagabondChopAch).registerAchievement();
         getFantaGlassesAch = new Achievement(900, "getFantaGlassesAch", -4, -2, fantaGlasses, killFantaAch).registerAchievement();
         getBambooSwordAch = new Achievement(901, "getBambooSwordAch", -3, -7, bambooSword, installModAch).registerAchievement();
         
         //descriptions des achievements
         ModLoader.addAchievementDesc(installModAch, "Youtuber", "Installer le mod FantaBobShow");
         ModLoader.addAchievementDesc(craftMagabondRecAch, "Magabooond", "Crafter un magabond remix");
         ModLoader.addAchievementDesc(getFantaGlassesAch, "Binoclard", "Rcuperer les lunettes de Fantasio");
         ModLoader.addAchievementDesc(killBobAch, "Bob Lennon Hater", "Tuer Bob Lennon");
         ModLoader.addAchievementDesc(killFantaAch, "Spirou", "Tuer Fantasio");
         ModLoader.addAchievementDesc(killJeanKevinAch, "Sauveur de l'humanit", "Tuer Jean-Kevin");
         ModLoader.addAchievementDesc(floodAch, "Flooooood !", "Se faire suivre par Jean-Kevin");
         ModLoader.addAchievementDesc(killBotlennonAch, "Tas de ferraille", "Tuer Botlennon");
         ModLoader.addAchievementDesc(getBouseAch, "Elment naturel", "Crafter de la bouse");
         ModLoader.addAchievementDesc(brushTeethAch, "Hygine dentaire", "Se brosser les dents");
         ModLoader.addAchievementDesc(getHairPotionAch, "Crane rs", "Crafter une lotion capillaire");
         ModLoader.addAchievementDesc(getStampCollectionAch, "Philatliste", "Crafter une collection de timbres");
         ModLoader.addAchievementDesc(getMagabondChopAch, "Repas avari", "Obtenir une ctelette de magabond");
         ModLoader.addAchievementDesc(getCobbleTieAch, "Bonjour patron !", "Crafter une cravate en cobble");
         ModLoader.addAchievementDesc(getBambooSwordAch, "Assassin au naturel", "Crafter une pe en bambou");
         
         //pour le onTickInGame()
         ModLoader.setInGameHook(this, true, false);
         
         canBurnWool = getBooleanProp("boblennon.pyromaniac.fire.wool");
 		canBurnWood = getBooleanProp("boblennon.pyromaniac.fire.wood");
 		canBurnTNT = getBooleanProp("boblennon.pyromaniac.fire.tnt");
 		canBurnPlants = getBooleanProp("boblennon.pyromaniac.fire.plants");
 		canBurnLeaves = getBooleanProp("boblennon.pyromaniac.fire.leaves");
 		isPyromaniac = getBooleanProp("boblennon.pyromaniac");
 		pyroRate = getIntegerProp("boblennon.pyromaniac.rate");
 		isImmuneToFire = getBooleanProp("boblennon.immunetofire");
 	}
 	
	public void takenFromCrafting(EntityPlayer entityplayer, ItemStack itemstack)
     {
         if(itemstack.itemID == magabondRecord.shiftedIndex)
         	entityplayer.triggerAchievement(craftMagabondRecAch);
         else if(itemstack.itemID == bouse.blockID)
 			entityplayer.triggerAchievement(getBouseAch);
         else if(itemstack.itemID == hairPotion.shiftedIndex)
         	entityplayer.triggerAchievement(getHairPotionAch);
         else if(itemstack.itemID == bambooSword.shiftedIndex)
         	entityplayer.triggerAchievement(getBambooSwordAch);
     }
 	
	public void onItemPickup(EntityPlayer entityplayer, ItemStack itemstack) 
 	{
 		if(itemstack.itemID == fantaGlasses.shiftedIndex)
 			entityplayer.triggerAchievement(getFantaGlassesAch);
 		else if(itemstack.itemID == magabondChop.shiftedIndex)
         	entityplayer.triggerAchievement(getMagabondChopAch);
 	}
 	
	public boolean onTickInGame(float tick, Minecraft minecraft)
 	{
 		minecraft.thePlayer.triggerAchievement(installModAch);
 		return true;
 	}
 	
	public void addRenderer(Map map)
     {
         map.put(EntityBob.class, new RenderBiped(new ModelBiped(), 0.5F));
         map.put(EntityFanta.class, new RenderBiped(new ModelBiped(), 0.5F));
         map.put(EntityJeanKevin.class, new RenderBiped(new ModelBiped(), 0.5F));
         map.put(EntityBotlennon.class, new RenderBiped(new ModelBiped(), 0.5F));
     }
 	
 	public LinkedProperties loadProperties(String location) throws FileNotFoundException, IOException 
 	{
 		LinkedProperties props = new LinkedProperties();
 		props.load(new FileInputStream(location));
 		return props;
 	}
 	
 	public void saveProperties(LinkedProperties props, String fileLocation, String comments) throws IOException 
 	{
 		try {
 			OutputStream out = new FileOutputStream(fileLocation);
 			props.store(out, comments);
 			out.flush();
 			out.close();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void createPropsFile(String location) throws IOException
 	{
 		props = new LinkedProperties();
 		
 		props.setProperty("fanta.spawn", "true");
 		props.setProperty("boblennon.spawn", "true");
 		props.setProperty("jeankevin.spawn", "true");
 		props.setProperty("botlennon.spawn", "true");
 		
 		props.setProperty("fanta.spawn.rate", "9");
 		props.setProperty("boblennon.spawn.rate", "9");
 		props.setProperty("jeankevin.spawn.rate", "10");
 		props.setProperty("botlennon.spawn.rate", "9");
 		
 		props.setProperty("boblennon.immunetofire", "true");
 		props.setProperty("boblennon.pyromaniac", "false");
 		props.setProperty("boblennon.pyromaniac.rate", "40");
 		props.setProperty("boblennon.pyromaniac.fire.wood", "true");
 		props.setProperty("boblennon.pyromaniac.fire.plants", "true");
 		props.setProperty("boblennon.pyromaniac.fire.tnt", "true");
 		props.setProperty("boblennon.pyromaniac.fire.wool", "true");
 		props.setProperty("boblennon.pyromaniac.fire.leaves", "true");
 		
 		saveProperties(props, propsLocation, null);
 	}
 	
 	public static Boolean getBooleanProp(String prop)
 	{
 		return Boolean.parseBoolean((String)props.get(prop));
 	}
 	
 	public static String getStringProp(String prop)
 	{
 		return (String)props.get(prop);
 	}
 	
 	public static Integer getIntegerProp(String prop)
 	{
 		return Integer.parseInt((String)props.get(prop));
 	}
 	
 	public Entity spawnEntity(int entityId, World worldClient, double x, double y, double z)
 	{
 		switch (entityId)
 		{
 	    	case 101:
 	    		return new EntityBob(worldClient);
 	        case 102:
 	        	return new EntityFanta(worldClient);
 	        case 103:
 				return new EntityJeanKevin(worldClient);
 	     	case 104:
 	     		return new EntityBotlennon(worldClient);
 	        default:
 	            return null;
 		}
 	}
 
 	public Packet23VehicleSpawn getSpawnPacket(Entity entity, int type)
 	{
 		if (entity instanceof EntityBotlennon || entity instanceof EntityBob || entity instanceof EntityFanta || entity instanceof EntityJeanKevin)
 	    	return new Packet23VehicleSpawn(entity, type);
 	    else
 	    	return null;
 	}
 	
 	//dclaration des objets
     public static Item fantaGlasses;
     public static Item cobbleTie;
     public static Item toothBrush;
     public static Item stampCollection;
     public static Item bambooSword;
     public static Item hairPotion;
     public static Item magabondChop;
     public static Item magabondRecord;
     
     //dclaration des achievements
     public static Achievement installModAch;
     public static Achievement craftMagabondRecAch;
     public static Achievement getFantaGlassesAch;
     public static Achievement killBobAch;
     public static Achievement killFantaAch;
     public static Achievement killJeanKevinAch;
     public static Achievement floodAch;
     public static Achievement killBotlennonAch;
     public static Achievement getBouseAch;
     public static Achievement brushTeethAch;
     public static Achievement getHairPotionAch;
     public static Achievement getStampCollectionAch;
     public static Achievement getMagabondChopAch;
     public static Achievement getCobbleTieAch;
     public static Achievement getBambooSwordAch;
     
     //proprits spcifiques  BobLennon
     public static boolean canBurnWool;
     public static boolean canBurnWood;
     public static boolean canBurnTNT;
 	public static boolean canBurnPlants;
 	public static boolean canBurnLeaves;
 	public static boolean isPyromaniac;
 	public static Integer pyroRate;
 	public static boolean isImmuneToFire;
     
     //dclaration des blocs
     public static Block bouse;
     
     public static LinkedProperties props;
     public static String propsLocation;
 }
