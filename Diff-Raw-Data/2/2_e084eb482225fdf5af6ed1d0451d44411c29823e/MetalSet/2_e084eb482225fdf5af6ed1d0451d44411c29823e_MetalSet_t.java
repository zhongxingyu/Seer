 package shadow.mods.metallurgy;
 
 import java.util.Random;
 
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.IWorldGenerator;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EnumArmorMaterial;
 import net.minecraft.src.FurnaceRecipes;
 import net.minecraft.src.IChunkProvider;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemBlock;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.World;
 import net.minecraftforge.common.DungeonHooks;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
 import shadow.mods.metallurgy.BC_CrusherRecipes;
 import shadow.mods.metallurgy.MetallurgyArmor;
 import shadow.mods.metallurgy.MetallurgyItem;
 import shadow.mods.metallurgy.MetallurgyEnumToolMaterial;
 import shadow.mods.metallurgy.MetallurgyItemAxe;
 import shadow.mods.metallurgy.MetallurgyItemHoe;
 import shadow.mods.metallurgy.MetallurgyItemPickaxe;
 import shadow.mods.metallurgy.MetallurgyItemSpade;
 import shadow.mods.metallurgy.MetallurgyItemSword;
 import shadow.mods.metallurgy.RecipeHelper;
 import shadow.mods.metallurgy.mod_MetallurgyCore;
 import shadow.mods.metallurgy.fantasy.FF_EssenceRecipes;
 
 public class MetalSet implements IWorldGenerator {
 	
 	public int numMetals;
 	public String setName;
 	
 	public IMetalSetEnum info;
 	
 	public Block ore;
 	public Block brick;
 	
 	public Item[] Dust;
 	public Item[] Bar;
 	
 	public Item[] Pickaxe;
 	public Item[] Shovel;
 	public Item[] Axe;
 	public Item[] Hoe;
 	public Item[] Sword;
 	
 	public Item[] Helmet;
 	public Item[] Plate;
 	public Item[] Legs;
 	public Item[] Boots;
 	
 	public MetalSet(IMetalSetEnum info)
 	{
 		this.info = info;
 		setName = info.getSetName();
 		numMetals = info.numMetals();
 		
 		Dust = new Item[numMetals];
 		Bar = new Item[numMetals];
 		
 		Pickaxe = new Item[numMetals];
 		Shovel = new Item[numMetals];
 		Axe = new Item[numMetals];
 		Hoe = new Item[numMetals];
 		Sword = new Item[numMetals];
 		
 		Helmet = new Item[numMetals];
 		Plate = new Item[numMetals];
 		Legs = new Item[numMetals];
 		Boots = new Item[numMetals];
 	
 		
 		for(int i = 0; i < numMetals; i++)
 		{
 			int startID = info.startID(i);
 			Dust[i] = (new MetallurgyItem(startID, info.image())).setIconCoord(i,3).setItemName(info.name(i) + "Dust").setTabToDisplayOn(CreativeTabs.tabMaterials);
 			Bar[i] = (new MetallurgyItem(startID+1, info.image())).setIconCoord(i,4).setItemName(info.name(i) + "Bar").setTabToDisplayOn(CreativeTabs.tabMaterials);
 			
 			if(info.isCatalyst(i))
 				continue;
 			Pickaxe[i] = new MetallurgyItemPickaxe(startID+2, info.image(), info.toolEnum(i)).setIconCoord(i,7).setItemName(info.name(i) + "Pickaxe");
 			Shovel[i] = new MetallurgyItemSpade(startID+3, info.image(), info.toolEnum(i)).setIconCoord(i,8).setItemName(info.name(i) + "Shovel");
 			Axe[i] = new MetallurgyItemAxe(startID+4, info.image(), info.toolEnum(i)).setIconCoord(i,5).setItemName(info.name(i) + "Axe");
 			Hoe[i] = new MetallurgyItemHoe(startID+5, info.image(), info.toolEnum(i)).setIconCoord(i,6).setItemName(info.name(i) + "Hoe");
 			Sword[i] = new MetallurgyItemSword(startID+6, info.image(), info.toolEnum(i)).setIconCoord(i,9).setItemName(info.name(i) + "Sword");
 			
 			Helmet[i] = (new MetallurgyArmor(startID+7, info.image(), info.name(i).toLowerCase().replaceAll("\\W", "") + "_1", info.armorEnum(i), 0, 0)).setIconCoord(i,12).setItemName(info.name(i) + "Helmet");
 			Plate[i] = (new MetallurgyArmor(startID+8, info.image(), info.name(i).toLowerCase().replaceAll("\\W", "") + "_1", info.armorEnum(i), 0, 1)).setIconCoord(i,13).setItemName(info.name(i) + "Plate");
 			Legs[i] = (new MetallurgyArmor(startID+9, info.image(), info.name(i).toLowerCase().replaceAll("\\W", "") + "_2", info.armorEnum(i), 0, 2)).setIconCoord(i,14).setItemName(info.name(i) + "Legs");
 			Boots[i] = (new MetallurgyArmor(startID+10, info.image(), info.name(i).toLowerCase().replaceAll("\\W", "") + "_1", info.armorEnum(i), 0, 3)).setIconCoord(i,15).setItemName(info.name(i) + "Boots");
 		
 			if(info.numRails(i) > 0)
 				RecipeHelper.addRailsRecipe(Bar[i], info.numRails(i));
 		}
 		
 		if(!info.isAlloy())			
 			ore = new MetallurgyBlock(info.oreID(), info.image(), numMetals, 0).setHardness(2F).setResistance(.1F).setBlockName(setName + "Ore");
 		
 		brick = new MetallurgyBlock(info.brickID(), info.image(), numMetals, 1).setHardness(2F).setResistance(.1F).setBlockName(setName + "Brick");
 		
 		for(int i = 0; i < numMetals; i++)
 		{
 			if(!info.isAlloy())	
 				MinecraftForge.setBlockHarvestLevel(ore, i, "pickaxe", info.oreHarvestLevel(i));
 			
 			MinecraftForge.setBlockHarvestLevel(brick, i, "pickaxe", info.oreHarvestLevel(i)); 
 			
 			if(!info.isCatalyst(i))
 				MinecraftForge.setToolClass(Pickaxe[i], "pickaxe", info.pickLevel(i));
 			
 		}
 		
 		MinecraftForge.EVENT_BUS.register(this);
 	}
 	
 	public void load()
 	{
 
 		/*
 		for(int i = 0; i < numMetals; i++)
 		{
 			//Smelting
 			if(ore != null)
 				FurnaceRecipes.smelting().addSmelting(ore.blockID, i, new ItemStack(Bar[i], 1));
 			ModLoader.addSmelting(Dust[i].shiftedIndex, new ItemStack(Bar[i], 1));
 				
 			//Crusher
 			if(ore != null)
 				BC_CrusherRecipes.smelting().addCrushing(ore.blockID, i, new ItemStack(Dust[i], 2));
 			BC_CrusherRecipes.smelting().addCrushing(Bar[i].shiftedIndex, new ItemStack(Dust[i], 1));
 
 			if(mod_MetallurgyCore.hasFantasy)
 				FF_EssenceRecipes.essence().addEssenceAmount(Bar[i].shiftedIndex, info.expValue(i));
 			 
 			//Bricks!
 			RecipeHelper.addBrickRecipes(brick.blockID, i, Bar[i], 0);
 			
 	        if(!info.isCatalyst(i))
 	        {
 				RecipeHelper.addAxeRecipe(Axe[i], Bar[i]);
 				RecipeHelper.addPickaxeRecipe(Pickaxe[i], Bar[i]);
 				RecipeHelper.addShovelRecipe(Shovel[i], Bar[i]);
 				RecipeHelper.addHoeRecipe(Hoe[i], Bar[i]);
 				RecipeHelper.addSwordRecipe(Sword[i], Bar[i]);
 				RecipeHelper.addHelmetRecipe(Helmet[i], Bar[i]);
 				RecipeHelper.addPlateRecipe(Plate[i], Bar[i]);
 				RecipeHelper.addLegsRecipe(Legs[i], Bar[i]);
 				RecipeHelper.addBootsRecipe(Boots[i], Bar[i]);
 			    
 			    //Buckets/Shears
 				RecipeHelper.addBucketRecipe(Bar[i]);
 				RecipeHelper.addShearsRecipe(Bar[i]);
 	        }
 			//ModLoader.addSmelting(Dust[i].shiftedIndex, new ItemStack(Bar[i], 1));
 		}	
 	        */
 		
 
 		for(int i = 0; i < info.numMetals(); i++)
 		{
 			if(ore != null)
 				OreDictionary.registerOre("ore" + info.name(i), new ItemStack(ore, 1, i));
 			OreDictionary.registerOre("dust" + info.name(i), new ItemStack(Dust[i], 1));
 			OreDictionary.registerOre("ingot" + info.name(i), new ItemStack(Bar[i], 1));
 			DungeonHooks.addDungeonLoot(new ItemStack(Bar[i], 1), info.dungeonLootChance(i), 1, info.dungeonLootAmount(i));
 		
 			
 		}
 		
 		if(ore != null)
 		{
 			GameRegistry.registerWorldGenerator(this);
 			GameRegistry.registerBlock(ore, MetallurgyItemBlock.class);
 		}
 
 		GameRegistry.registerBlock(brick, MetallurgyItemBlock.class);
 	}
 	
 
     @ForgeSubscribe
     public void oreRegistered(OreRegisterEvent event)
     {
     	for(int i = 0; i < info.numMetals(); i++)
     	{
     		//System.out.println("comparing " + event.Name + " to " + info.name(i));
     		if(event.Name.equals("ore" + info.name(i)))
     		{
    			FurnaceRecipes.smelting().addSmelting(event.Ore.itemID, i, new ItemStack(Bar[i], 1));
     			BC_CrusherRecipes.smelting().addCrushing(event.Ore.itemID, event.Ore.getItemDamage(), new ItemStack(Dust[i], 2));
     		}
 
     		if(event.Name.equals("ingot" + info.name(i)))
     		{
     			if(mod_MetallurgyCore.hasFantasy)
     				FF_EssenceRecipes.essence().addEssenceAmount(event.Ore.itemID, info.expValue(i));
 
     			BC_CrusherRecipes.smelting().addCrushing(event.Ore.itemID, new ItemStack(Dust[i], 1));
     			
     			//Bricks!
     			RecipeHelper.addBrickRecipes(brick.blockID, i, event.Ore.getItem(), event.Ore.getItemDamage());
     			
     	        if(!info.isCatalyst(i))
     	        {
     				RecipeHelper.addAxeRecipe(Axe[i], event.Ore.getItem());
     				RecipeHelper.addPickaxeRecipe(Pickaxe[i], event.Ore.getItem());
     				RecipeHelper.addShovelRecipe(Shovel[i], event.Ore.getItem());
     				RecipeHelper.addHoeRecipe(Hoe[i], event.Ore.getItem());
     				RecipeHelper.addSwordRecipe(Sword[i], event.Ore.getItem());
     				RecipeHelper.addHelmetRecipe(Helmet[i], event.Ore.getItem());
     				RecipeHelper.addPlateRecipe(Plate[i], event.Ore.getItem());
     				RecipeHelper.addLegsRecipe(Legs[i], event.Ore.getItem());
     				RecipeHelper.addBootsRecipe(Boots[i], event.Ore.getItem());
     			    
     			    //Buckets/Shears
     				RecipeHelper.addBucketRecipe(event.Ore.getItem());
     				RecipeHelper.addShearsRecipe(event.Ore.getItem());
     	        }
     		}
 
     		if(event.Name.equals("dust" + info.name(i)))
     		{
     			//System.out.println("smelting " + event.Name + " into " + Bar[i]);
     			GameRegistry.addSmelting(event.Ore.itemID, new ItemStack(Bar[i], 1), 1);
     		}
     	}
     }
     
 	@Override
 	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
 	{
 		if(!info.spawnsInDimension(world.provider.worldType))
 			return;
 
 		for(int i = 0; i < numMetals; i++)
 			if(info.metalEnabled(i))
 				generateOre(world, rand, chunkX * 16, chunkZ * 16, i);
 	}
 
 	public void generateOre(World world, Random rand, int chunkX, int chunkZ, int meta)
 	{
 		for(int i = 0; i < info.veinCount(meta); i++)
 		{
 			int randPosX = chunkX + rand.nextInt(16);
 			int randPosY = rand.nextInt(info.oreHeight(meta) - info.oreMinHeight(meta)) + info.oreMinHeight(meta);
 			int randPosZ = chunkZ + rand.nextInt(16);
 			//System.out.println("spawning " + info.name(meta) + " " + randPosX + " " + randPosY + " " + randPosZ);
 			if(info.spawnsInDimension(-1) && world.provider.isHellWorld)
 				(new MetallurgyWorldGenNetherMinable(ore.blockID, meta, info.oreCount(meta))).generate(world, rand, randPosX, randPosY, randPosZ);
 			else if(info.spawnsInDimension(1) && world.provider.worldType == 1)
 				(new MetallurgyWorldGenEnderMinable(ore.blockID, meta, info.oreCount(meta))).generate(world, rand, randPosX, randPosY, randPosZ);
 			else if(info.spawnsInDimension(world.provider.worldType))
 				(new MetallurgyWorldGenMinable(ore.blockID, meta, info.oreCount(meta))).generate(world, rand, randPosX, randPosY, randPosZ);
 		}
 	}
 
 }
