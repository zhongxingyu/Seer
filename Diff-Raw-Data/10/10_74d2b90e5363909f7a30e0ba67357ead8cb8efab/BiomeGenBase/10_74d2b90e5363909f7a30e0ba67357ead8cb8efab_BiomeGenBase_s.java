 package net.minecraft.src;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 public abstract class BiomeGenBase
 {
     /** An array of all the biomes, indexed by biome id. */
     public static final BiomeGenBase[] biomeList = new BiomeGenBase[256];
     public static final BiomeGenBase ocean = (new BiomeGenOcean(0)).setColor(112).setBiomeName("Ocean").setMinMaxHeight(-1.0F, 0.4F);
     public static final BiomeGenBase plains = (new BiomeGenPlains(1)).setColor(9286496).setBiomeName("Plains").setTemperatureRainfall(0.8F, 0.4F);
     public static final BiomeGenBase desert = (new BiomeGenDesert(2)).setColor(16421912).setBiomeName("Desert").setDisableRain().setTemperatureRainfall(2.0F, 0.0F).setMinMaxHeight(0.1F, 0.2F);
     public static final BiomeGenBase extremeHills = (new BiomeGenHills(3)).setColor(6316128).setBiomeName("Extreme Hills").setMinMaxHeight(0.2F, 1.3F).setTemperatureRainfall(0.2F, 0.3F);
     public static final BiomeGenBase forest = (new BiomeGenForest(4)).setColor(353825).setBiomeName("Forest").func_4124_a(5159473).setTemperatureRainfall(0.7F, 0.8F);
     public static final BiomeGenBase taiga = (new BiomeGenTaiga(5)).setColor(747097).setBiomeName("Taiga").func_4124_a(5159473).setEnableSnow().setTemperatureRainfall(0.05F, 0.8F).setMinMaxHeight(0.1F, 0.4F);
     public static final BiomeGenBase swampland = (new BiomeGenSwamp(6)).setColor(522674).setBiomeName("Swampland").func_4124_a(9154376).setMinMaxHeight(-0.2F, 0.1F).setTemperatureRainfall(0.8F, 0.9F);
     public static final BiomeGenBase river = (new BiomeGenRiver(7)).setColor(255).setBiomeName("River").setMinMaxHeight(-0.5F, 0.0F);
     public static final BiomeGenBase hell = (new BiomeGenHell(8)).setColor(16711680).setBiomeName("Hell").setDisableRain().setTemperatureRainfall(2.0F, 0.0F);
 
     /** Is the biome used for sky world. */
     public static final BiomeGenBase sky = (new BiomeGenEnd(9)).setColor(8421631).setBiomeName("Sky").setDisableRain();
     public static final BiomeGenBase frozenOcean = (new BiomeGenOcean(10)).setColor(9474208).setBiomeName("FrozenOcean").setEnableSnow().setMinMaxHeight(-1.0F, 0.5F).setTemperatureRainfall(0.0F, 0.5F);
     public static final BiomeGenBase frozenRiver = (new BiomeGenRiver(11)).setColor(10526975).setBiomeName("FrozenRiver").setEnableSnow().setMinMaxHeight(-0.5F, 0.0F).setTemperatureRainfall(0.0F, 0.5F);
     public static final BiomeGenBase icePlains = (new BiomeGenSnow(12)).setColor(16777215).setBiomeName("Ice Plains").setEnableSnow().setTemperatureRainfall(0.0F, 0.5F);
     public static final BiomeGenBase iceMountains = (new BiomeGenSnow(13)).setColor(10526880).setBiomeName("Ice Mountains").setEnableSnow().setMinMaxHeight(0.2F, 1.2F).setTemperatureRainfall(0.0F, 0.5F);
     public static final BiomeGenBase mushroomIsland = (new BiomeGenMushroomIsland(14)).setColor(16711935).setBiomeName("MushroomIsland").setTemperatureRainfall(0.9F, 1.0F).setMinMaxHeight(0.2F, 1.0F);
     public static final BiomeGenBase mushroomIslandShore = (new BiomeGenMushroomIsland(15)).setColor(10486015).setBiomeName("MushroomIslandShore").setTemperatureRainfall(0.9F, 1.0F).setMinMaxHeight(-1.0F, 0.1F);
 
     /** Beach biome. */
     public static final BiomeGenBase beach = (new BiomeGenBeach(16)).setColor(16440917).setBiomeName("Beach").setTemperatureRainfall(0.8F, 0.4F).setMinMaxHeight(0.0F, 0.1F);
 
     /** Desert Hills biome. */
     public static final BiomeGenBase desertHills = (new BiomeGenDesert(17)).setColor(13786898).setBiomeName("DesertHills").setDisableRain().setTemperatureRainfall(2.0F, 0.0F).setMinMaxHeight(0.2F, 0.7F);
 
     /** Forest Hills biome. */
     public static final BiomeGenBase forestHills = (new BiomeGenForest(18)).setColor(2250012).setBiomeName("ForestHills").func_4124_a(5159473).setTemperatureRainfall(0.7F, 0.8F).setMinMaxHeight(0.2F, 0.6F);
 
     /** Taiga Hills biome. */
     public static final BiomeGenBase taigaHills = (new BiomeGenTaiga(19)).setColor(1456435).setBiomeName("TaigaHills").setEnableSnow().func_4124_a(5159473).setTemperatureRainfall(0.05F, 0.8F).setMinMaxHeight(0.2F, 0.7F);
 
     /** Extreme Hills Edge biome. */
     public static final BiomeGenBase extremeHillsEdge = (new BiomeGenHills(20)).setColor(7501978).setBiomeName("Extreme Hills Edge").setMinMaxHeight(0.2F, 0.8F).setTemperatureRainfall(0.2F, 0.3F);
 
     /** Jungle biome identifier */
     public static final BiomeGenBase jungle = (new BiomeGenJungle(21)).setColor(5470985).setBiomeName("Jungle").func_4124_a(5470985).setTemperatureRainfall(1.2F, 0.9F).setMinMaxHeight(0.2F, 0.4F);
     public static final BiomeGenBase jungleHills = (new BiomeGenJungle(22)).setColor(2900485).setBiomeName("JungleHills").func_4124_a(5470985).setTemperatureRainfall(1.2F, 0.9F).setMinMaxHeight(1.8F, 0.2F);
     public String biomeName;
     public int color;
 
     /** The block expected to be on the top of this biome */
     public byte topBlock;
 
     /** The block to fill spots in when not on the top */
     public byte fillerBlock;
     public int field_6502_q;
 
     /** The minimum height of this biome. Default 0.1. */
     public float minHeight;
 
     /** The maximum height of this biome. Default 0.3. */
     public float maxHeight;
 
     /** The temperature of this biome. */
     public float temperature;
 
     /** The rainfall in this biome. */
     public float rainfall;
 
     /** Color tint applied to water depending on biome */
     public int waterColorMultiplier;
     public BiomeDecorator biomeDecorator;
 
     /**
      * Holds the classes of IMobs (hostile mobs) that can be spawned in the biome.
      */
     protected List spawnableMonsterList;
 
     /**
      * This allows a mod to add an Entity to the monster list with only having to know the biomeID for the biome
      * This is an attempt to add a mechanism to add compatibility mods between biome mods and creature mods
      * I plan on working a more general version with modloader but that is not a high priority
      * @param biomeId
      * @param Class entity
      * @param a
      * @param b
      * @param c
      */
     public static void addMonsterList(int biomeId, Class entity, int a, int b, int c)
     {
     	BiomeGenBase biome = BiomeGenBase.biomeList[biomeId];
     	if(biome == null) return;
     	biome.spawnableMonsterList.add(new SpawnListEntry(entity, a, b, c));
     }
     
     /**
      * Holds the classes of any creature that can be spawned in the biome as friendly creature.
      */
     protected List spawnableCreatureList;
     
     /**
      * See addMonsterList
      * @param biomeId
      * @param Class entity
      * @param a
      * @param b
      * @param c
      */
     public static void addCreatureList(int biomeId, Class entity, int a, int b, int c)
     {
     	BiomeGenBase biome = BiomeGenBase.biomeList[biomeId];
     	if(biome == null) return;
     	biome.spawnableCreatureList.add(new SpawnListEntry(entity, a, b, c));
     }
 
     /**
      * Holds the classes of any aquatic creature that can be spawned in the water of the biome.
      */
     protected List spawnableWaterCreatureList;
     
     /**
      * See addMonsterList
      * @param biomeId
      * @param Class entity
      * @param a
      * @param b
      * @param c
      */
     public static void addWaterCreatureList(int biomeId, Class entity, int a, int b, int c)
     {
     	BiomeGenBase biome = BiomeGenBase.biomeList[biomeId];
     	if(biome == null) return;
     	biome.spawnableWaterCreatureList.add(new SpawnListEntry(entity, a, b, c));
     }
 
     /** Set to true if snow is enabled for this biome. */
     private boolean enableSnow;
 
     /**
      * Is true (default) if the biome support rain (desert and nether can't have rain)
      */
     private boolean enableRain;
 
     /** The id number to this biome, and its index in the biomeList array. */
     public final int biomeID;
     protected WorldGenTrees worldGenTrees;
     protected WorldGenBigTree worldGenBigTree;
     protected WorldGenForest worldGenForest;
     protected WorldGenSwamp worldGenSwamp;
     
     /**
      * This holds the new WorldGenerators
      */
     protected static HashMap extraGens = new HashMap();
     
     /**
      * This adds general WorldGenerators to BiomeGenBase
      * I do not recommend using this but instead coding the Generator directly into the biome or adding it into the BiomeDecorator
      * This uses a hash map to access the generator so you must pass a key to identify the generator by
      * @param key
      * @param gen
      */
     public static void addWorldGen(String key, WorldGenerator gen)
     {
     	if(gen == null)
     	{
     		throw new RuntimeException("You cannot pass a null generator to BiomeGenBase");
     	}
     	extraGens.put(key, gen);
     }
     
     /**
      * This returns a previously added generator
      * This does not check to see if the key is valid, YOU MUST DO THIS!
      * @param key
      * @return
      */
     public static WorldGenerator getWorldGen(String key)
     {
     	return (WorldGenerator)extraGens.get(key);
     }
 
     protected BiomeGenBase(int par1)
     {
         this.topBlock = (byte)Block.grass.blockID;
         this.fillerBlock = (byte)Block.dirt.blockID;
         this.field_6502_q = 5169201;
         this.minHeight = 0.1F;
         this.maxHeight = 0.3F;
         this.temperature = 0.5F;
         this.rainfall = 0.5F;
         this.waterColorMultiplier = 16777215;
         this.spawnableMonsterList = new ArrayList();
         this.spawnableCreatureList = new ArrayList();
         this.spawnableWaterCreatureList = new ArrayList();
         this.enableRain = true;
         this.worldGenTrees = new WorldGenTrees(false);
         this.worldGenBigTree = new WorldGenBigTree(false);
         this.worldGenForest = new WorldGenForest(false);
         this.worldGenSwamp = new WorldGenSwamp();
         this.biomeID = par1;
         if(biomeList[par1] != null) throw new MinecraftException("A biome already exists at id: " + par1);
         biomeList[par1] = this;
         this.biomeDecorator = this.createBiomeDecorator();
         this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12, 4, 4));
         this.spawnableCreatureList.add(new SpawnListEntry(EntityPig.class, 10, 4, 4));
         this.spawnableCreatureList.add(new SpawnListEntry(EntityChicken.class, 10, 4, 4));
         this.spawnableCreatureList.add(new SpawnListEntry(EntityCow.class, 8, 4, 4));
         this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10, 4, 4));
         this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 10, 4, 4));
         this.spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
         this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10, 4, 4));
         this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10, 4, 4));
         this.spawnableMonsterList.add(new SpawnListEntry(EntityEnderman.class, 1, 1, 4));
         this.spawnableWaterCreatureList.add(new SpawnListEntry(EntitySquid.class, 10, 4, 4));
     }
     
     /** Whether or not the biome is enabled, only used for new biomes */
     private boolean enabled = true;
     
     /**
      * This constructor sets up the biome to check for whether it is enabled in the config file.
      * See BiomeGenBase.setEnabled(String)
      * @param id
      * @param name
      */
     public BiomeGenBase(int id, String name)
     {
     	this(id);
     	setEnabled(name);
     }
     
     /**
      * This constructor sets up the biome to check for whether it is enabled in the config file.
      * It also calls BiomeAPI.getNextBiomeID(string) for the id
      * This is constructor exists so that user only needs to base the name once since the name is likely to be the same for both
      * setEnabled(String) and BiomeAPI.getNextBiomeID(String)
      * @param name
      */
     public BiomeGenBase(String name)
     {
     	this(BiomeAPI.getNextBiomeID(name));
     	setEnabled(name);
     }
     
     /**
      * This sets up the biome to be enabled through the config file. It takes the name of biome and appends 'Biome' to it
      * To disable simplely find the biome in the config and set it to 'false'. When set to false it will not be added to
      * the default world generator, the spawn list, the village spawn list and the stronghold spawn list.
      * This does not affect the vanilla biomes. If you want it to then call this function for that biome with an appropriate
      * name and then query the enabled state through isEnabled() and then finnally call BiomeAPI.removeBiome(BiomeGenBase) if needed.
      * @param name
      */
     public void setEnabled(String name)
     {
     	if(ModLoader.props.containsKey("Biome" + name))
			enabled = Boolean.parseBoolean(ModLoader.props.getProperty(name));
     	else
     		ModLoader.props.put("Biome" + name, "true");
     }
     
     /**
      * Returns whether or not this biome is enabled. Will return true if the biome does not have a config entry.
      * @return
      */
     public boolean isEnabled()
     {
     	return enabled;
     }
 
     /**
      * Allocate a new BiomeDecorator for this BiomeGenBase
      */
     protected BiomeDecorator createBiomeDecorator()
     {
         return new BiomeDecorator(this);
     }
 
     /**
      * Sets the temperature and rainfall of this biome.
      * This function and all other similiar functions have been changed to public to allow easy changes outside of this class - Hyperion2288
      */
     public BiomeGenBase setTemperatureRainfall(float par1, float par2)
     {
         if (par1 > 0.1F && par1 < 0.2F)
         {
             throw new IllegalArgumentException("Please avoid temperatures in the range 0.1 - 0.2 because of snow");
         }
         else
         {
             this.temperature = par1;
             this.rainfall = par2;
             return this;
         }
     }
 
     /**
      * Sets the minimum and maximum height of this biome. Seems to go from -2.0 to 2.0.
      */
     public BiomeGenBase setMinMaxHeight(float par1, float par2)
     {
         this.minHeight = par1;
         this.maxHeight = par2;
         return this;
     }
 
     /**
      * Disable the rain for the biome.
      */
     public BiomeGenBase setDisableRain()
     {
         this.enableRain = false;
         return this;
     }
 
     /**
      * Gets a WorldGen appropriate for this biome.
      */
     public WorldGenerator getRandomWorldGenForTrees(Random par1Random)
     {
         return (WorldGenerator)(par1Random.nextInt(10) == 0 ? this.worldGenBigTree : this.worldGenTrees);
     }
 
     public WorldGenerator func_48410_b(Random par1Random)
     {
         return new WorldGenTallGrass(Block.tallGrass.blockID, 1);
     }
 
     /**
      * sets enableSnow to true during biome initialization. returns BiomeGenBase.
      */
     public BiomeGenBase setEnableSnow()
     {
         this.enableSnow = true;
         return this;
     }
 
     public BiomeGenBase setBiomeName(String par1Str)
     {
         this.biomeName = par1Str;
         return this;
     }
 
     public BiomeGenBase func_4124_a(int par1)
     {
         this.field_6502_q = par1;
         return this;
     }
 
     public BiomeGenBase setColor(int par1)
     {
         this.color = par1;
         return this;
     }
 
     /**
      * takes temperature, returns color
      */
     public int getSkyColorByTemp(float par1)
     {
         par1 /= 3.0F;
 
         if (par1 < -1.0F)
         {
             par1 = -1.0F;
         }
 
         if (par1 > 1.0F)
         {
             par1 = 1.0F;
         }
 
         return Color.getHSBColor(0.62222224F - par1 * 0.05F, 0.5F + par1 * 0.1F, 1.0F).getRGB();
     }
 
     /**
      * Returns the correspondent list of the EnumCreatureType informed.
      */
     public List getSpawnableList(EnumCreatureType par1EnumCreatureType)
     {
         return par1EnumCreatureType == EnumCreatureType.monster ? this.spawnableMonsterList : (par1EnumCreatureType == EnumCreatureType.creature ? this.spawnableCreatureList : (par1EnumCreatureType == EnumCreatureType.waterCreature ? this.spawnableWaterCreatureList : null));
     }
 
     /**
      * Returns true if the biome have snowfall instead a normal rain.
      */
     public boolean getEnableSnow()
     {
         return this.enableSnow;
     }
 
     /**
      * Return true if the biome supports lightning bolt spawn, either by have the bolts enabled and have rain enabled.
      */
     public boolean canSpawnLightningBolt()
     {
         return this.enableSnow ? false : this.enableRain;
     }
 
     /**
      * Checks to see if the rainfall level of the biome is extremely high
      */
     public boolean isHighHumidity()
     {
         return this.rainfall > 0.85F;
     }
 
     /**
      * returns the chance a creature has to spawn.
      */
     public float getSpawningChance()
     {
         return 0.1F;
     }
 
     /**
      * Gets an integer representation of this biome's rainfall
      */
     public final int getIntRainfall()
     {
         return (int)(this.rainfall * 65536.0F);
     }
 
     /**
      * Gets an integer representation of this biome's temperature
      */
     public final int getIntTemperature()
     {
         return (int)(this.temperature * 65536.0F);
     }
 
     /**
      * Gets a floating point representation of this biome's rainfall
      */
     public final float getFloatRainfall()
     {
         return this.rainfall;
     }
 
     /**
      * Gets a floating point representation of this biome's temperature
      */
     public final float getFloatTemperature()
     {
         return this.temperature;
     }
 
     public void decorate(World par1World, Random par2Random, int par3, int par4)
     {
         this.biomeDecorator.decorate(par1World, par2Random, par3, par4);
     }
 
     /**
      * Provides the basic grass color based on the biome temperature and rainfall
      */
     public int getBiomeGrassColor()
     {
         double var1 = (double)MathHelper.clamp_float(this.getFloatTemperature(), 0.0F, 1.0F);
         double var3 = (double)MathHelper.clamp_float(this.getFloatRainfall(), 0.0F, 1.0F);
         return ColorizerGrass.getGrassColor(var1, var3);
     }
 
     /**
      * Provides the basic foliage color based on the biome temperature and rainfall
      */
     public int getBiomeFoliageColor()
     {
         double var1 = (double)MathHelper.clamp_float(this.getFloatTemperature(), 0.0F, 1.0F);
         double var3 = (double)MathHelper.clamp_float(this.getFloatRainfall(), 0.0F, 1.0F);
         return ColorizerFoliage.getFoliageColor(var1, var3);
     }
 	
     /**
      * This adds the biome to the default world generator in GenLayerBiome
      * @return
      */
 	public BiomeGenBase addBiomeGen()
 	{
 		if(!enabled) return this;
 		
 		for(int i = 0; i < GenLayerBiome.biomeArray.length; i++)
 			if(this == GenLayerBiome.biomeArray[i]) return this;
 		BiomeGenBase[] biomeArray = new BiomeGenBase[GenLayerBiome.biomeArray.length + 1];
 		for(int i = 0; i < GenLayerBiome.biomeArray.length; i++)
 			biomeArray[i] = GenLayerBiome.biomeArray[i];
 		biomeArray[biomeArray.length - 1] = this;
 		GenLayerBiome.biomeArray = biomeArray;
 		
 		return this;
 	}
 	
 	/**
 	 * This adds the biome to the spawn biome list in WorldChunkManager
 	 * @return
 	 */
 	public BiomeGenBase addBiomeSpawn()
 	{
 		if(enabled) WorldChunkManager.addBiomeToSpawnIn(this);
 		return this;
 	}
 	
 	/**
 	 * This adds the biome to the allowed spawn list for Villages
 	 * @return
 	 */
 	public BiomeGenBase addBiomeVillage()
 	{
 		if(!enabled) return this;
 		for(int i = 0; i < MapGenVillage.villageSpawnBiomes.size(); i++)
 			if(this == MapGenVillage.villageSpawnBiomes.get(i)) return this;
 		MapGenVillage.villageSpawnBiomes = Arrays.asList(MapGenVillage.villageSpawnBiomes, this);
 		return this;
 	}
 	
 	/**
 	 * This adds the biome to the allowed spawn list for Stronholds
 	 * @return
 	 */
 	public BiomeGenBase addBiomeStronghold()
 	{
 		if(enabled) MapGenStronghold.addStrongholdBiomes(this);
 		return this;
 	}
 	
 	/**
 	 * This adds the biome to the default generator, to the spawn list, to the village spawn list and to the stronghold spawn list
 	 * @return
 	 */
 	public BiomeGenBase addBiomeEverything()
 	{
 		return addBiomeGen().addBiomeSpawn().addBiomeVillage().addBiomeStronghold();
 	}
 }
