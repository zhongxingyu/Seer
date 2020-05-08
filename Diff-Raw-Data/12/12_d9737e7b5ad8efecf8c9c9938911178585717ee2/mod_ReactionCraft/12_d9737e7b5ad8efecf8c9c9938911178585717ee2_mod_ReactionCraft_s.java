 package net.minecraft.src;
 import java.io.File;
 import java.util.*;
 
 import net.minecraft.src.forge.*;
 import net.minecraft.src.Reactioncraft.*;
 
 public class mod_ReactionCraft extends NetworkMod
 {	
 	public String getVersion()
     { 
             return "1.2.5";
     }
 	
 	
 		/** Meta Blocks **/
 		public static Block Multi;
 		public static Block FlowerMulti;
 		public static Block NetherMulti;
 		public static Block HireoMulti;
 		public static Block BushesMulti;
 		public static Block SkinnyColumnMulti;
 		public static Block ColumnMulti;
 		public static Block OreBlockMulti;
 		public static Block MetalBlockMulti;
 		public static Block BrickMulti;
 		public static Block DesertBlockMulti;
 		public static Block CarpetMulti;
 		public static Block FossilMulti;
 		public static Block FenceMulti;
 		public static Block GlowShroomMulti;
 		public static Block ShellMulti;
 		public static Block LanternMulti;
 		public static Block CactusMulti;
 		
 		
 		/**Non Meta Blocks **/
 		
 		//Plant Pot
 		public static Block PlantPot;
 		
 		//Ores
 		public static Block BloodStone;
 		
 		//Explosives
 		public static Block CreeperBlock;
 	    public static Block DylanNuke;
 		
 	    
 	    //Bamboo like
 	    public static Block Bamboo;
 		public static Block NetherCane;
 		public static Block SeaWeed;
 	    
 		
 		//GUI Blocks
 		public static Block RcCrate;
 		public static Block NetherfurnaceIA;
 		public static Block NetherfurnaceA;
 		
 		
 		//Sand Blocks
 		public static Block DarkSand;
 		
 		
 		//Desert Blocks
 		public static Block DesertOre;
 	    public static Block DesertOre1;
 	    public static Block DesertGold;
 	    public static Block DesertRock;
 	    public static Block DesertCrackedStone;
 	    public static Block LimeStone;
 		 
 	    
 	    //Marble
 	    public static Block Marble;
 	    public static Block Marblec;
 	    
 	    
 	    //Plants
 	    public static Block AncientPlant;
 	    
 	    
 	    //Redstone Related Blocks
 	    public static Block TrapBlock;
 	    public static Block RedStoneBlock;
 	    public static Block RedStoneBlockc;
 	    
 	    
 	    //Trampoline
 	    public static Block Bouncy;
 	    
 	    
 	    //Liquids
 	    public static Block MagmaliquidMoving;
 	    public static Block MagmaliquidStill;
 	    
 	    
 	    //Slabs
 	    public static Block BloodStoneSlab;
 	    public static Block GoldSlab;
 	    public static Block DiamondSlab;
 	    public static Block BedRockSlab;
 	    public static Block StainedSlab;
 	    public static Block MarbleSlab;
 	    
 	    
 	    //Stairs
 	    public static Block StainedStep;
 	    public static Block GoldStep;
 	    public static Block BedRockStep;
 	    public static Block DiamondStep;
 	    public static Block Glowstonestep;
 	    public static Block Sandstonestep;
 	    public static Block BBstep;
 	    public static Block DesertBrickStep;
 	    public static Block DesertBricksStep;
 	    public static Block MarbleStep;
 		 
 	    
 	    //Plants
 	    public static Block ReedPlant;
 	    
 	  //Grass
         public static Block RcTallGrass;
         
         
         //Coral
         public static Block OrangeCoral;
     	public static Block GreenCoral;
     	public static Block PurpleCoral;
     	public static Block BrownCoral;
     	public static Block BlueCoral;
     	public static Block LightBlueCoral;
         public static Block LightGreenCoral;
         public static Block PinkCoral;
         public static Block LightBlueOrangeCoral;
         public static Block LightBlueCoral2;
         public static Block DarkGreenCoral;
         public static Block RedCoral;
         public static Block MagentaCoral;
         
         
         //Torches
         public static Block MysticTorch;
         
         
         //Cherry Tree Blocks
         public static Block RcLeaves1;
         public static Block Cherrywood;
         public static Block CherrySapling;
 	    
         public static Block  GL;
         public static Block  Rope;
         public static Block  VineL;
         public static Block  ChainL;
 
 
         public static Block  CoralBlock1;
         public static Block  CoralBlock2;
         public static Block  CoralBlock3;
 
         public static Block  GlowingGrass;
 
         public static Block  Chain;
 
         public static Block  PermaFrost;
 
         public static Block  Fence;
         public static Block  BSFence;
         public static Block  CobbleFence;
         public static Block  MarbleFence;
         public static Block  StoneFence;
 
         public static Block  Bookcasedoor;
 
         public static Block  Vase;
 
         public static Block  SteelSpike;
         public static Block  SteelSpikeu;
 
         public static Block  StainedPlank;
         
         //Custom Block Model Blocks
         	//Custom Blocks
         public static Block blockAluminium;
         	//Statues
         public static Block creeperStatue;
         public static Block HumanStatue; 
         public static Block ZombieStatue; 
         public static Block SpiderStatue; 
         public static Block cowstatue;
         public static Block pigstatue;
         public static Block skeletonstatue; 
         public static Block villagerstatue; 
         public static Block endermanstatue; 
         public static Block irongolemstatue;
         	//SnowMan
         public static Block Snowman;
         	//Starfish
         public static Block Starfish;
         
         
 		 
 		/**    *******************     Items     ****************************      **/
 				 
 			    
 			    //Dusts-Clumps of ores - Shards From ores
 			    public static Item BloodStoneDust;
 				public static Item IronDust;
 			    public static Item GoldClump;
 			    public static Item GreenGlowShroomDust;
 			    public static Item PurpleGlowShroomDust1;
 			    public static  Item ManganeseDust;
 		        public static  Item ZincDust;
 		        
 			    
 			    //Misc Items
 			    public static Item Varnish;
 			    
 			    
 			    //Sticks
 			    public static Item GoldStick;
 			    
 		
 			    //items that place blocks
 			    public static Item Vine;
 			    public static  Item IBamboo;
 			    public static  Item FlagItem1;
 			    
 			    
 			    //Currency
 			    public static Item GoldCoin;
 			    public static Item BrassCoin;
 			    public static Item SilverCoin;
 			    public static Item GoldStack;
 			    public static Item SilverStack;
 			    public static Item BrassStack;
 			    public static Item GoldCoinSack;
 			    public static Item SilverCoinSack;
 			    public static Item BrassCoinSack;
 			    public static Item BagofGold;
                 public static Item BagofSilver;
                 public static Item BagofDiamond;
                 
 			    
 			    //fish - new fishing drops
 			    public static Item SamonRaw;
 			    public static Item YellowTailRaw;
 			     //-----Cooked Fisk
 			    public static Item SamonCooked;
 			    public static Item YellowTailCooked;
 			     //-----Bottled Items
 			    public static Item Mapinabottle;
 			    public static Item Shipinabottle;
 		
 			    
 				//Gems
 				public static Item BlackDiamondShard;
 				public static Item BlackDiamondGem;
 				public static Item DragonStoneShard;
 				public static Item DragonStoneGem;
 		        public static Item WhiteDiamondShard;
 		        public static Item WhiteDiamondGem;     
 		        public static Item DBDesertShard;
 		        public static Item DBDesertGem;
 		        public static Item LBDesertShard;
 		        public static Item LBDesertGem;
 				
 				//Raw Materials
 		        public static Item Straw;
 		        public static Item Sack;
 		        public static Item Bag;
 		        public static Item DriedBamboo;
 		        public static Item DarkGreenDye;
 		        public static Item NetherCaneDust;
 		        public static Item MagmaFlint;
 		        public static Item Papyrus;
                 public static Item Reed;
                 public static Item SandStonePaste;
                 
                 
 		        //foods
 		        public static Item AncientFruit;
 		        public static Item Bacon;
 		        public static Item RawBacon;
 		        public static Item ChickenNuggets;
 		        public static Item RawNuggets;  
 		        public static Item SBread;
 		        public static Item SteakSandwich;
 		        
 		        
 		        //Tools
 		        public static Item Knife;
 		        public static Item Flintchisel;
 		        public static Item Goldchisel;
 		        public static Item BloodstoneChisel;
 		        public static Item StoneHammer;
 		        
 		        
 		        //Ingots
 		        public static Item RefinedGoldIngot;
 		        public static Item RefinedSilverIngot;
 		        public static Item SilverIngot;
 		        public static Item BrassIngot;
 		        public static Item ObsidianIngot;
 		        public static Item BloodStoneIngot;
 		        public static Item MithrilIngot;
 		        public static Item CobaltIngot;
 		        public static Item TitaniumIngot;
 		        public static Item TinIngot;
 		        public static Item AdamantiteIngot;
 		        public static Item CopperIngot;
 		        public static Item PlatinumIngot;
 		        public static Item ManganeseIngot;
 		        public static Item OnyxIngot;
                 public static Item IridiumIngot;
                 public static Item CalciteIngot;
                 public static Item AmethystIngot;
                 public static Item DaeyaltIngot;
                 public static Item SuperHeatedIron;
                 public static Item SteelIngot;
                 
                 
                 //Weapons
                 	//DragonStone-BloodStone Weapons
 		        public static Item BloodStoneHoe;
 		        public static Item BloodStoneAxe;
 		        public static Item BloodStoneShovel;
 		        public static Item BloodStoneSword;
 		        public static Item BloodStonePicAxe;
 		        	//Bone Weapons
 		        public static Item BonePicAxe;
 		        	//Obsidian
 		        public static Item ObsidianAxe;
 		        public static Item ObsidianHoe;
 		        public static Item ObsidianSword;
 		        public static Item ObsidianShovel;
 		        	//Mithril
 		        public static Item MithrilSword;
 		        public static Item MithrilPicAxe;
 		        public static Item MithrilShovel;
 		        public static Item MithrilAxe;
 		        	//Flint-Bone
 		        public static Item BoneFlintPicAxe;
 		        public static Item BoneFlintShovel;
 		        public static Item BoneFlintSword;
 		        public static Item BoneFlintAxe;
 		        	//
 		        
 		        
 		        
 		        //Seeds
 		        public static Item AncientSeeds;
 
 		        
 		        //buckets
 		        public static  Item MagmaBucket;
 		            
 		        
 		        //Custom Glass Bottles
 		        public static Item GlassBottle;
 		        public staitc Item PosionBottle;
 		        
 		        
 		        //Arrows
 		        public static Item DiamondArrow;
 		        public static Item PosionArrow;
 		        public static Item SteelArrow;
 		        
 		        
 		        //Armor
 		        	//Diving Suit
 		        public static Item DivingHelm;
 		        public static Item ScubaTank;
 		        	//Mithril
 		        public static Item MithrilChest;
 		        public static Item MithrilBoots;
 		        public static Item MithrilLegs;
 		        public static Item MithrilHelm;
 		        	//Iridium
 		        public static Item IridiumBoots;
 		        public static Item IridiumLegs;
 		        public static Item IridiumChest;
 		        public static Item IridiumHelm;
 		        	//Obsidan
 		        public static Item ObsidianHelm;
 		        public static Item ObsidanChest;
 		        public static Item ObsidanLegs;
 		        public static Item ObsidanBoots;
 		        	//Bloodstone
 		        public static Item BloodstoneChest;
 		        public static Item BloodstoneLegs;
 		        public static Item BloodstoneHelm;
 		        public static Item BloodstoneBootds;
 		        
 		        //Chain Parts
 		        public static  Item ChainLoop;
 		        public static  Item ChainLoops;
 		        
 		        
 		        //Book like Items
 		        public static  Item Scroll;
 		        
 		        //Wands 
 		        public static Item DragonstoneWand;
 		        public static Item FireWand;
 		        public static Item IceWand;
 		        public static Item ElectricWand;
 		        public static Item WindWand;
 		        public static Item WizardsBook;
 		        
 		        //BCD
 		        public static Item BookcasedoorItem;
 		        
 		        
 		        /**  RailCraft Items **/
 		        public static Item BloodStoneBorehead;
 		        public static Item BlackdiamondBoreHead;
 		       
 		        
 		        
 		        //TO BE USED...
 		        public static Item RcForgeItem;
 
 		        
 		        
 		        
 		/** ACHIEVEMENTS **/
 		public Achievement DownloadReactionCraft;
 		public Achievement Nether;
 		public Achievement Nuke;
 		public Achievement Creepin;
 		
 		
 		/** BIOMES **/
 		public static BiomeGenBase RcDesert; 
 		public static BiomeGenBase RcOcean;
 		public static BiomeGenBase RcWasteland;
 		public static BiomeGenBase RcDesolate;
 		
 		
 		
 	@Override
     public void load()
 	{
 		Configuration config = new Configuration(new File(ModLoader.getMinecraftInstance().getMinecraftDir(), "/config/ReactonCraft.prop"));
 		config.load();
 		int[] ids = 
 		{
 			//Config Stuff Below Here
 				//Instances here
 			
 					//Meta's
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Glass Blocks", 123).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Flowers", 124).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Nether Ores", 125).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Hireglyphic Blocks", 126).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Bushes", 127).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Small Column's", 128).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Large Column's", 129).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Brick Blocks", 130).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Ore Blocks", 131).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Desert Blocks", 132).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Nether Carpet Blocks", 133).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Fossil Blocks", 134).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("GlowShroom's", 135).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Shell Blocls", 136).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Lantern Blocks", 137).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Cactus Blocks", 138).value),
 					//End of Meta's
 					
 					
 					//Non Meta's
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Plant Pot", 139).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("BloodStone", 140).value),
 						//Slab
 					Integer.parseInt(config.getOrCreateBlockIdProperty("BloodStone Slab", 141).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("BloodStone Slab D", 142).value),
 						//Slab
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Gold Slab", 141).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Gold Slab D", 142).value),
 						//Slab
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Bedrock Slab", 141).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Bedrock Slab D", 142).value),
 						//Slab
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Stained Plank Slab", 141).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Stained Plank Slab D", 142).value),
 						//Slab
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Marble Slab", 141).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Marble Slab D", 142).value),
 					
 					
 					//Stairs
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Gold Stair", 143).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Bedrock Stair", 144).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Diamond Stair", 145).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Glowstone Stair", 146).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Sandstone Stair", 147).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Bloodstone Brick Stair", 148).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("DesertBrick Stair", 149).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("DesertBricks Stair", 150).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Marble Stair", 151).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Stained Plank Stair", 152).value),
 					//End of Stairs
 					
 					
 					//Gravity Affected Blocks
 					Integer.parseInt(config.getOrCreateBlockIdProperty("DarkSand", 153).value),
 					//End of Gravity Blocks
 			        
 					
 					//Gui Blocks
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Crate Block", 154).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Nether Furnace on", 155).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Nether Furnace off", 156).value),
 			        //End of Gui Blocks
 					
 					
 					//Plants
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Ancient Plant", 157).value),
 					//End of Plants
 
 					
 					//Redstone Related Blocks
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Trap Door Block", 158).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Redstone Block", 159).value),
 					Integer.parseInt(config.getOrCreateBlockIdProperty("Compressed Redstone Block", 160).value),					
 					//End of Redstone Related Blocks
 					
 					//Items
 					
 		};
 		config.save();
 		
 				//Block Code 
 					//Meta Blocks
 					Multi = new BlockMulti(ids[1], Material.glass).setHardness(0.3F).setBlockName("Multi");
 					FlowerMulti = new BlockFlowerMulti(ids[2], Material.plants).setHardness(0.0F).setBlockName("FlowerMulti").setRequiresSelfNotify();
 					NetherMulti = new BlockNetherMulti(ids[3], Material.rock).setHardness(3.0F).setBlockName("NetherMulti");
 					HireoMulti = new BlockHireoMulti(ids[4], Material.rock).setHardness(3.0F).setBlockName("HireoMulti");
 					BushesMulti = new BlockBushesMulti(ids[5], Material.plants).setHardness(0.0F).setBlockName("BushesMulti").setRequiresSelfNotify();
 					SkinnyColumnMulti = new BlockSkinnyCMulti(ids[6], Material.rock).setHardness(3.0F).setBlockName("SCMulti");
 					ColumnMulti = new BlockColumnMulti(ids[7], Material.rock).setHardness(3.0F).setBlockName("ColumnMulti");
 					BrickMulti = new BlockBrickMulti(ids[8], Material.rock).setHardness(3.0F).setBlockName("BrickMulti");
 					OreBlockMulti = new BlockRcOres(ids[9], Material.rock).setHardness(3.0F).setBlockName("OreMulti");
 					MetalBlockMulti = new BlockRcMetalBlocks(ids[10], Material.rock).setHardness(3.0F).setBlockName("OreBlockMulti");
 					DesertBlockMulti = new BlockDesertMulti(ids[12], Material.rock).setHardness(3.0F).setBlockName("DesertBlockMulti");
 					CarpetMulti = new BlockGlowShroomCarpetMulti(ids[13], Material.rock).setHardness(3.0F).setBlockName("GlowShroomCarpetMulti");
 					FossilMulti = new BlockFossilMulti(ids[14], Material.rock).setHardness(0.5F).setBlockName("FossilMulti");
 					GlowShroomMulti = new BlockGlowShroomMulti(ids[16], Material.rock).setHardness(3.0F).setBlockName("GlowShroomMulti");
 					ShellMulti = new BlockShellMulti(ids[17], Material.water).setHardness(0.80F).setResistance(0.1F).setStepSound(Block.soundStoneFootstep).setBlockName("Shell");
 					LanternMulti = new BlockLanternMulti(ids[18], Material.glass).setLightValue(1.0F).setHardness(0.3F).setBlockName("LanternMulti");					
 					CactusMulti = new BlockCactusMulti(ids[19], Material.plants).setHardness(0.10F).setResistance(0.1F).setStepSound(Block.soundStoneFootstep).setBlockName("StairMulti").setRequiresSelfNotify();
 					
 				//Non Meta Blocks
 				
 				//Plant Pot
 					PlantPot = new BlockPlantPot(ids[20], 99).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("PlantPot");
 				
 				//liquids
 	        			//-Magma
 						MagmaliquidMoving = new BlockMagmaFlowing(ids[21], Material.lava).setHardness(100F).setLightOpacity(3).setBlockName("MagmaliquidMoving").disableStats().setRequiresSelfNotify();
 						MagmaliquidStill = new BlockMagmaStationary(ids[22], Material.lava).setHardness(100F).setLightOpacity(3).setBlockName("MagmaliquidStill").disableStats().setRequiresSelfNotify();
 						
 				//Ores
 					BloodStone = new BlockForge(ids[23], 81).setHardness(60.0F).setResistance(2000.0F).setLightValue(0.25F).setStepSound(Block.soundStoneFootstep).setBlockName("BloodStone");
 				
 				//Slabs
 					//84
 					BloodStoneSlab = new BlockSlabRc(ids[24], true).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("BloodStoneSlab");
 					BloodStoneSlab = new BlockSlabRc(ids[25], false).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("BloodStoneSlab");
 					//214
 					GoldSlab = new BlockSlabRc(ids[26], true).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GoldSlab");
 					GoldSlab = new BlockSlabRc(ids[27], false).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GoldSlab");
 					//215
 					DiamondSlab = new BlockSlabRc(ids[28], true).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("DiamondSlab");
 					DiamondSlab = new BlockSlabRc(ids[29], false).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("DiamondSlab");
 					//206
 					BedRockSlab = new BlockSlabRc(ids[30], true).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("BedRockSlab");
 					BedRockSlab = new BlockSlabRc(ids[31], false).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("BedRockSlab");
 					//100
 					StainedSlab = new BlockSlabRc(ids[32], true).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("StainedStep");
 					StainedSlab = new BlockSlabRc(ids[33], false).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("StainedStep");
 					//164
 					MarbleSlab = new BlockSlabRc(ids[34], true).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("MarbleSlab");
 					MarbleSlab = new BlockSlabRc(ids[35], false).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("MarbleSlab");
 
 					
 				//Stair Blocks
 			        GoldStep = new BlockStairs(ids[36], Block.blockGold).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("GoldStep");
 			        BedRockStep = new BlockStairs(ids[37], Block.bedrock).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("BedRockStep");
 			        DiamondStep = new BlockStairs(ids[38], Block.blockDiamond).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("DiamondStep");
 			        Glowstonestep = new BlockStairs(ids[39], Block.glowStone).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("Glowstonestep");
 			        Sandstonestep = new BlockStairs(ids[40], Block.sandStone).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("SandStonestep");
 			        BBstep = new BlockStairsRc(ids[41], 84).setHardness(70.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("BBstep");
 			        DesertBrickStep = new BlockStairsRc(ids[42], 139).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("DesertBrickStep");
 			        DesertBricksStep = new BlockStairsRc(ids[43], 79).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("DesertBricksStep");
 			        MarbleStep = new BlockStairsRc(ids[44], 164).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("MarbleStep");
 			        StainedStep = new BlockStairsRc(ids[45], 100).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("StainedStep");
 			        
 			    //Gravity Affected Blocks
 			        DarkSand = new BlockSandRc(ids[46], 136).setHardness(0.5F).setResistance(5.0F).setStepSound(Block.soundSandFootstep).setBlockName("DarkSand");
 				
 			        
 			    //Gui Blocks
 			        RcCrate = (new BlockCrate(ids[47])).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("RcCrate");
 			        NetherfurnaceIA = (new BlockNetherFurnace(ids[48], false)).setHardness(3.5F).setStepSound(Block.soundStoneFootstep).setBlockName("Netherfurnace").setRequiresSelfNotify();
 			        NetherfurnaceA = (new BlockNetherFurnace(ids[49], true)).setHardness(3.5F).setStepSound(Block.soundStoneFootstep).setLightValue(0.875F).setBlockName("Netherfurnace").setRequiresSelfNotify();
 			    //Plants
 				    AncientPlant = new AncientPlant(ids[50], 0).setHardness(0.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("AncientPlant");  
 			        
 			        
 			    //Redstone Related
 			      	 TrapBlock = new BlockGhostBlock(ids[51], 0).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("TrapBlock");
 			      	 RedStoneBlock = new BlockRedstoneBlock(ids[52], 0).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("RedStoneBlock");
 			      	 RedStoneBlockc = new BlockRedstoneBlock(ids[53], 1).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("RedStoneBlock");
 			       
 			        
 			    //Plank		      	
 				     StainedPlank = new BlockForge(ids[54], 100).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("StainedPlank");
 				       
 					
 				//Spike Traps
 			      	 SteelSpike = new BlockTrap(ids[55], 153).setHardness(0.1F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("SteelSpike");
 			      	 SteelSpikeu = new BlockTrap(ids[56], 154).setHardness(0.1F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("SteelSpikeu");
 			       
 					
 					
 				//Flowers
 					
 					RcTallGrass = new BlockGrassRc(ids[57]).setHardness(0.0F).setResistance(5.0F).setStepSound(Block.soundGrassFootstep).setBlockName("RcTallGrass").setRequiresSelfNotify();		
 					Vase = new BlockFlowerRc(ids[58], 97).setHardness(0.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("Vase").setRequiresSelfNotify();
 					
 					
 					
 				//Doors
 					Bookcasedoor = new BlockBookcaseDoorW(ids[59], Material.wood).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("Bookcasedoor");
 					
 					
 					
 					//Fences
 					Fence = new BlockFenceRc(ids[60], 139).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("Fence");
 					BSFence = new BlockFenceRc(ids[61], 84).setHardness(70.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("BSFence");
 					CobbleFence = new BlockFenceRc1(ids[62], 1).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("CobbleFence");
 					MarbleFence = new BlockFenceRc(ids[63], 164).setHardness(1.5F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("MarbleFence");
 					StoneFence = new BlockFenceRc1(ids[64], 0).setHardness(1.5F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("StoneFence");
 					
 				//PermaFrost
 					PermaFrost = new BlockForge(ids[65], 156).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("PermaFrost");	
 					
 					
 				 //Torches
 			      	MysticTorch = new BlockTorchRc(ids[66], 96).setLightValue(0.80F).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("MysticTorch");
 			    
 			      	
 			     //chandelier chains
 			      	Chain = new Chain(ids[67], 114).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("Chain");
 			       
 			        
 			     //Glowing Grass
 			      	GlowingGrass = new BlockGlowingGrass(ids[68], 125).setLightValue(0.80F).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GlowingGrass");
 
 			      	 
 			     //coral blocks
 			       CoralBlock1 = new BlockForge(ids[69], 6).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("CoralBlock1");
 			       CoralBlock2 = new BlockForge(ids[70], 7).setLightValue(0.80F).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("CoralBlock2");
 			       CoralBlock3 = new BlockForge(ids[71], 15).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("CoralBlock3");
 			       
 			     //coral plants
 			       OrangeCoral = new BlockCoral(ids[72], 0).setLightValue(1.0F).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("MagentaCoral");
 			       GreenCoral = new BlockCoral(ids[73], 1).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("PurpleCoral");
 			       PurpleCoral = new BlockCoral(ids[74], 2).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("OrangeCoral");
 			       BrownCoral = new BlockCoral(ids[75], 3).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("BrownCoral");
 			       BlueCoral = new BlockCoral(ids[76], 4).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GreenCoral");
 			       LightBlueCoral = new BlockCoral(ids[77], 5).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GreenCoral");
 			       LightGreenCoral = new BlockCoral(ids[78], 8).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GreenCoral");
 			       PinkCoral = new BlockCoral(ids[79], 9).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GreenCoral");
 			       LightBlueOrangeCoral = new BlockCoral(ids[80], 10).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GreenCoral");
 			       LightBlueCoral2 = new BlockCoral(ids[81], 11).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GreenCoral");
 			       DarkGreenCoral = new BlockCoral(ids[82], 12).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GreenCoral");
 			       RedCoral = new BlockCoral(ids[83], 13).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GreenCoral");
 			       MagentaCoral = new BlockCoral(ids[84], 14).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("MagentaCoral");
 			       
 			     //Ladder Type Blocks
 			       ChainL = new BlockChainLadder(ids[85], 114).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("ChainL");
 			       VineL = new BlockVineRc(ids[86]).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setBlockName("VineL");
 			       Rope = new BlockRope(ids[87], 48).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("Rope");
 			       GL = new BlockGoldLadder(ids[88], 68).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("GL");
 			       
 			       
 			       SeaWeed = new BlockSeaweed(ids[89], 71).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("Seaweed");
 			       
 			       LimeStone = new BlockForge(ids[90], 51).setHardness(1.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("LimeStone");
 			       
 			     //Custom Block Model Blocks
 		        	//Custom Blocks
		        
 		        	//Statues
 			       creeperStatue = new CreeperStatue(133, 16, net.minecraft.src.TileEntityCreeperStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("creeperStatue");
 			       HumanStatue = new HumanStatue(142, 16, net.minecraft.src.TileEntityHumanStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("HumanStatue");
 			       ZombieStatue = new ZombieStatue(143, 16, net.minecraft.src.TileEntityZombieStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("ZombieStatue");
 			       SpiderStatue = new SpiderStatue(144, 16, net.minecraft.src.TileEntitySpiderStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("SpiderStatue");
 			       cowstatue = new CowStatue(145, 16, net.minecraft.src.TileEntityCowStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("cowstatue");
 			       pigstatue = new PigStatue(146, 16, net.minecraft.src.TileEntityPigStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("pigstatue");
 			       skeletonstatue = new SkeletonStatue(147, 16, net.minecraft.src.TileEntitySkeletonStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("skeletonstatue");
 			       villagerstatue = new VillagerStatue(148, 16, net.minecraft.src.TileEntityVillagerStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("villagerstatue");
 			       endermanstatue = new EnderMStatue(149, 16, TileEntiyEndermanStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("endermanstatue");
 			       irongolemstatue = new IronGStatue(151, 16, net.minecraft.src.TileEntityIronGolemStatue.class).setHardness(0.7F).setResistance(5F).setBlockName("irongolemstatue");
 		        	//SnowMan
			       Snowman = new Snowman(152, 16,)
		        	//Starfish
			       Starfish = new Starfish(153, 16,)
 		        	//Starfish
 		        
 			       
 			   //Item Code
 						BloodStoneDust = (new ItemForge(ids[91])).setIconCoord(1, 0).setItemName("BloodStoneDust");
 						BlackDiamondShard = (new ItemForge(ids[92])).setIconCoord(97, 0).setItemName("BlackDiamondShard");
 						BlackDiamondGem = (new ItemForge(ids[93])).setIconCoord(84, 0).setItemName("BlackDiamondGem");
 						
 						
 						//raw stuff
 				         Straw = (new ItemForge(ids[94])).setIconCoord(67, 0).setItemName("Straw");
 				         Sack = (new ItemForge(ids[95])).setIconCoord(64, 0).setItemName("Sack");
 				         DriedBamboo = (new ItemForge(ids[96])).setIconCoord(68, 0).setItemName("DriedBamboo");
 				         DarkGreenDye = (new ItemForge(ids[97])).setIconCoord(65, 0).setItemName("DarkGreenDye");
 				         NetherCaneDust = (new ItemForge(ids[98])).setIconCoord(66, 0).setItemName("NetherCaneDust");
 				        //Food
 				        //Doors      
 				        //Dusts
 				         ManganeseDust = (new ItemForge(ids[99])).setIconCoord(87, 0).setItemName("ManganeseDust");
 				         ZincDust = (new ItemForge(ids[100])).setIconCoord(96, 0).setItemName("ZincDust");
 				        //Liquids
 				        //DragonStone-BloodStone Tools
 				         BloodStoneHoe = (new BloodStoneSword(ids[101], EnumRcToolMaterial.BLOODSTONE)).setIconCoord(89, 0).setItemName("BloodStoneSword");
 				         BloodStoneAxe = (new BloodStoneSword(ids[102], EnumRcToolMaterial.BLOODSTONE)).setIconCoord(86, 0).setItemName("BloodStoneSword");
 				         BloodStoneShovel = (new BloodStoneSword(ids[], EnumRcToolMaterial.BLOODSTONE)).setIconCoord(88, 0).setItemName("BloodStoneSword");
 				         BloodStoneSword = (new BloodStoneSword(ids[], EnumRcToolMaterial.BLOODSTONE)).setIconCoord(5, 0).setItemName("BloodStoneSword");
 				         BloodStonePicAxe = (new BloodStonePicaxe(ids[], EnumRcToolMaterial.BLOODSTONE)).setIconCoord(85, 0).setItemName("BloodStonePixAxe");
 				        //Bone Stools
 				         BonePicAxe = (new BloodStoneSword(ids[], EnumRcToolMaterial.Bone)).setIconCoord(99, 0).setItemName("BonePicAxe");
 				            //Gems
 				         WhiteDiamondShard = (new ItemForge(ids[])).setIconCoord(98, 0).setItemName("WhiteDiamond");
 				         BlackDiamondGem = (new ItemForge(ids[])).setIconCoord(97, 0).setItemName("BlackDiamond1");
 				        //Armor?
 				       
 				         SuperHeatedIron = (new ItemForge(ids[])).setIconCoord(81, 0).setItemName("SuperHeatedIron");
 				         ChainLoop = (new ItemForge(ids[])).setIconCoord(82, 0).setItemName("ChainLoop");
 				         ChainLoops = (new ItemForge(ids[])).setIconCoord(83, 0).setItemName("ChainLoops");
 				         BlackDiamondShard = (new ItemForge(ids[])).setIconCoord(84, 0).setItemName("BlackDiamond2");
 				         SteelIngot = (new ItemForge(ids[])).setIconCoord(16, 0).setItemName("SteelIngot");
 				         
 				         //Fossil Items
 				        DinosaurFragments = (new RcForgeItem(ids[])).setIconCoord("fix", 0).setItemName("DinosaurFragments");
 				        DinosaurFragments1 = (new RcForgeItem(ids[])).setIconCoord("fix", 0).setItemName("DinosaurFragments1");
 
 				         
 				         SandStonePaste = (new ItemForge(ids[])).setIconCoord(119, 0).setItemName("SandStonePaste");
 				         Flintchisel = (new ItemForge(ids[])).setIconCoord(110, 0).setItemName("Flintchisel");
 				         Goldchisel = (new ItemForge(ids[])).setIconCoord(113, 0).setItemName("Goldchisel");
 				       
 				        //foods and items to fix textures on
 				                //foods
 				         AncientFruit = (new ItemFoodRc(ids[], 4, 1F, false)).setIconCoord(100, 0).setItemName("AncientFruit");
 				         Bacon = (new ItemFoodRc(ids[], 4, 1F, false)).setIconCoord(111, 0).setItemName("Bacon");
 				         RawBacon = (new ItemForge(ids[])).setIconCoord(126, 0).setItemName("RawBacon");
 				         ChickenNuggets = (new ItemFoodRc(ids[], 4, 1F, false)).setIconCoord(122, 0).setItemName("ChichenNuggets");
 				         RawNuggets = (new ItemForge(ids[])).setIconCoord(127, 0).setItemName("RawNuggets");        
 				         SBread = (new ItemFoodRc(ids[], 4, 1F, false)).setIconCoord(124, 0).setItemName("SBread");
 				         SteakSandwich = (new ItemFoodRc(ids[], 4, 1F, false)).setIconCoord(125, 0).setItemName("SteakSandWich");
 				                //Tools
 				         Knife = (new ItemForge(ids[])).setIconCoord(123, 0).setItemName("Knfie");
 		                       
 				         DBDesertGem = (new ItemForge(ids[])).setIconCoord(132, 0).setItemName("DBDesertGem");
 				         DBDesertShard = (new ItemForge(ids[])).setIconCoord(129, 0).setItemName("DBDesertGem1");
 				         LBDesertShard = (new ItemForge(ids[])).setIconCoord(130, 0).setItemName("LBDesertGem1");
 				         LBDesertGem = (new ItemForge(ids[])).setIconCoord(131, 0).setItemName("LBDesertGem");
 				                //buckets
 				         MagmaBucket = (new ItemForge(ids[])).setIconCoord(113, 0).setItemName("MagmaBucket");
 				        //
 				         StoneHammer = (new ItemForge(ids[])).setIconCoord(114, 0).setItemName("StoneHammer");
 				        //
 				         IBamboo = (new ItemBamboo(ids[])).setIconCoord(136, 0).setItemName("IBamboo");
 		       
 		                //Flag
 		                 FlagItem1 = (new ItemForge(ids[])).setIconCoord(141, 0).setItemName("FlagItem1");
 		               
 		               
 		                //
 		                 BagofGold = (new ItemForge(ids[])).setIconCoord(140, 0).setItemName("BagofGold");
 		                 BagofSilver = (new ItemForge(ids[])).setIconCoord(143, 0).setItemName("BagofSilver");
 		                 BagofDiamond = (new ItemForge(ids[])).setIconCoord(139, 0).setItemName("BagofDiamond");
 		                 Bag = (new ItemForge(ids[])).setIconCoord(145, 0).setItemName("Bag");
 		               
 		                //more desert stuff
 		                 Papyrus = (new ItemForge(ids[])).setIconCoord(121, 0).setItemName("Papyrus");
 		                 Reed = (new ItemReedRc(ids[])).setIconCoord(142, 0).setItemName("Reed");
 		                 Scroll = (new ItemForge(ids[])).setIconCoord(120, 0).setItemName("Scroll");
 		                 MagmaFlint = (new ItemForge(ids[])).setIconCoord(138, 0).setItemName("MagmaFlint");
 		               
 		               
 		                //Fixed Textured Ingots
 		                 RefinedGoldIngot = (new ItemForge(ids[])).setIconCoord(62, 0).setItemName("RefinedGoldIngot");
 		                 RefinedSilverIngot = (new ItemForge(ids[])).setIconCoord(61, 0).setItemName("RefinedSilverIngot");
 		                 SilverIngot = (new ItemForge(ids[])).setIconCoord(61, 0).setItemName("SilverIngot");
 				         BrassIngot = (new ItemForge(ids[])).setIconCoord(60, 0).setItemName("BrassIngot");
 				         ObsidianIngot = (new ItemForge(ids[])).setIconCoord(23, 0).setItemName("ObsidianIngot");
 				         BloodStoneIngot = (new ItemForge(ids[])).setIconCoord(6, 0).setItemName("BloodStoneIngot");
 				         MithrilIngot = (new ItemForge(ids[])).setIconCoord(17, 0).setItemName("MithrilIngot");
 				         CobaltIngot = (new ItemForge(ids[])).setIconCoord(44, 0).setItemName("CobaltIngot");
 				         TitaniumIngot = (new ItemForge(ids[])).setIconCoord(10, 0).setItemName("TitaniumIngot");
 				         TinIngot = (new ItemForge(ids[])).setIconCoord(29, 0).setItemName("TinIngot");
 				         AdamantiteIngot = (new ItemForge(ids[])).setIconCoord(71, 0).setItemName("AdamantiteIngot");
 				         CopperIngot = (new ItemForge(ids[])).setIconCoord(72, 0).setItemName("CopperIngot");
 				         PlatinumIngot = (new ItemForge(ids[])).setIconCoord(28, 0).setItemName("PlatinumIngot");
 				         ManganeseIngot = (new ItemForge(ids[])).setIconCoord(95, 0).setItemName("ManganeseIngot");
 				         OnyxIngot = (new ItemForge(ids[])).setIconCoord(137, 0).setItemName("OnyxIngot");
 		                 IridiumIngot = (new ItemForge(ids[])).setIconCoord(137, 0).setItemName("IridiumIngot");
 		                 CalciteIngot = (new ItemForge(ids[])).setIconCoord(137, 0).setItemName("CalciteIngot");
 		                 AmethystIngot = (new ItemForge(ids[])).setIconCoord(135, 0).setItemName("AmethystIngot");
 		                 DaeyaltIngot = (new ItemForge(ids[])).setIconCoord(134, 0).setItemName("DaeyaltIngot");
 				
 				
 		                 BloodstoneBorehead = (new IBloodstoneBH(ids[])).setIconCoord(151, 0).setItemName("BloodstoneBorehead");	
 		 		    	
 		 		    	 BlackdiamondBorehead = (new IBlackDiamondBH(ids[])).setIconCoord(150, 0).setItemName("BloodstoneBorehead");	
 				
 				
 			   //Achievements
 		       DownloadReactionCraft = (new Achievement(999, "Downloaded ReactionCraft", 0, 2, FlowerMulti, AchievementList.openInventory)).registerAchievement();
 		       Nether = (new Achievement(1000, "Welcome to Hell", -6, -5, BloodStone, null)).registerAchievement();
 		       Nuke = (new Achievement(1001, "O Shit", -7, -5, DylanNuke, null)).registerAchievement();
 		       Creepin = (new Achievement(1002, "Stalker", -8, -5, CreeperBlock, null)).registerAchievement();      
 		       //End of Achievements
 		       
 		       
 		        //Biomes
 		        RcDesert = (new GenDesert(40)).setColor(16421912).setBiomeName("Desert");
 		        RcOcean = (new BiomeGenOcean(41)).setColor(16421912).setBiomeName("Ocean");
 		        RcDesolate = (new GenDesolate(42)).setColor(16421912).setBiomeName("Desolate");
 		        RcWasteland = (new GenWasteland(43)).setColor(16421912).setBiomeName("RcWasteland");
 		        
 		        
 		        //Begin Voids
 		        RegisterBlocks();
 		        SetTextures();
 		        AddNames();
 		        Recipes();
 		        addSmelts();
 		        Achievements();
 		        Biomes();
 		        AddMob();
 		        TileEntites();
 		        Bonemeal();
 		        //End Voids		        
 	}
 	
 	
 	public void SetTextures()
 	{
 		MinecraftForgeClient.preloadTexture("/reactioncraft/Blocks.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/ExtraBlocks.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/Items.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/RcFalseBlocks.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/Liquids.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/Flags.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/extendedcolumn.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/plants.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/guiblock.png");
 		MinecraftForgeClient.preloadTexture("/reactioncraft/furnace.png");
 	}
 	
 	
 	public void Achievements()
 	{
 		ModLoader.addAchievementDesc(Nether, "Welcome to Hell", " mined some bloodstone ");
 		ModLoader.addAchievementDesc(DownloadReactionCraft, "Downloaded ReactionCraft", " Downloaded and played ReactionCraft :) ");	
 		ModLoader.addAchievementDesc(Nuke, "O Shit", " we got nukes! ");
 		ModLoader.addAchievementDesc(Creepin, "Stalker", " I'm Watching you ");
 	}
 	
 	
 	public void onItemPickup(EntityPlayer entityplayer, ItemStack itemstack) 
     { 
 		if(itemstack.itemID == mod_ReactionCraft.FlowerMulti.blockID) 
 		{ 
             entityplayer.addStat(DownloadReactionCraft, 1); 
 		} 
 		if(itemstack.itemID == mod_ReactionCraft.BloodStone.blockID) 
 		{ 
             entityplayer.addStat(Nether, 1); 
 		} 
 		if(itemstack.itemID == mod_ReactionCraft.CreeperBlock.blockID) 
 		{ 
             entityplayer.addStat(Creepin, 1); 
 		}
 		if(itemstack.itemID == mod_ReactionCraft.DylanNuke.blockID) 
 		{ 
             entityplayer.addStat(Nuke, 1); 
 		}
              
     }
 	
 	
 	public void Bonemeal()
 	{
 		MinecraftForge.registerBonemealHandler(new RcForgeHandler());
 	}
 	
 	
 	public void Biomes() 
 	{
 		ModLoader.addBiome(RcDesert);
 		ModLoader.addBiome(RcOcean);
 		ModLoader.addBiome(RcWasteland);
 		ModLoader.addBiome(RcDesolate);
 	}
 	
 	
 	public void RegisterBlocks()
 	{
 		 //MetaBlocks
     		//Glass Multi Blocks
 			ModLoader.registerBlock(Multi, net.minecraft.src.Reactioncraft.ItemMulti.class);
 			//Nether Multi Blocks
 	        ModLoader.registerBlock(NetherMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //HireoGlyphics
 	        ModLoader.registerBlock(HireoMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Flowers
 	        ModLoader.registerBlock(FlowerMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Bushes
 	        ModLoader.registerBlock(BushesMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);	        
 	        //Small Columns
 	        ModLoader.registerBlock(SkinnyColumnMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Columns
 	        ModLoader.registerBlock(ColumnMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Bricks
 	        ModLoader.registerBlock(BrickMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Ores
 	        ModLoader.registerBlock(OreBlockMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Ore Blocks
 	        ModLoader.registerBlock(MetalBlockMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Desert Blocks
 	        ModLoader.registerBlock(DesertBlockMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Glowing Carpets
 	        ModLoader.registerBlock(CarpetMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Fossils
 	        ModLoader.registerBlock(FossilMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //GlowShrooms
 	        ModLoader.registerBlock(GlowShroomMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Shells
 	        ModLoader.registerBlock(ShellMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	        //Lanterns
 	        ModLoader.registerBlock(LanternMulti, net.minecraft.src.Reactioncraft.ItemMulti.class);
 	}
 	
 	
 	public void AddNames()
 	{
 		//Meta's
 			//Colored Glass
 			ModLoader.addName(new ItemStack(Multi, 1), "Yellow StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 1), "Red StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 2), "Black StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 3), "Blue StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 4), "Brown StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 5), "Cyan StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 6), "Gray StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 7), "Green StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 8), "Lightblue StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 9), "Lightgray StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 10), "Limegreen StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 11), "Magenta StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 12), "Orange StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 13), "Pink StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 14), "Purple StainedGlass");
 	        ModLoader.addName(new ItemStack(Multi, 1, 15), "White StainedGlass");
 		
 	        
 	        //Flowers
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1), 	 "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 1),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 2),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 3),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 4),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 5),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 6),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 7),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 8),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 9),  "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 10), "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 11), "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 12), "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 13), "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 14), "");
 	        ModLoader.addName(new ItemStack(FlowerMulti, 1, 15), "");
 	        
 	        
 			//NetherOres 
 	        ModLoader.addName(new ItemStack(NetherMulti, 1), 	"Onyx");
 	        ModLoader.addName(new ItemStack(NetherMulti, 1, 1), "BloodStone");
 	        ModLoader.addName(new ItemStack(NetherMulti, 1, 2), "DragonStone");
 	        ModLoader.addName(new ItemStack(NetherMulti, 1, 3), "Iridium");
 	        ModLoader.addName(new ItemStack(NetherMulti, 1, 4), "BlackDiamond");
 	        ModLoader.addName(new ItemStack(NetherMulti, 1, 5), "Nether Iron");
 	        ModLoader.addName(new ItemStack(NetherMulti, 1, 6), "Nether Lapis");
 	        ModLoader.addName(new ItemStack(NetherMulti, 1, 7), "Nredstone");
 	        ModLoader.addName(new ItemStack(NetherMulti, 1, 8), "Ncoal");    
 	        
 	        
 	        //Hireoglyphics
 	        ModLoader.addName(new ItemStack(HireoMulti, 1),    	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 1), 	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 2), 	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 3), 	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 4), 	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 5), 	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 6), 	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 7), 	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 8), 	"Hireoglyphics");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 9), 	"Column Top");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 10), "Column Base");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 11), "Weathered Hireoglyph");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 12), "Marble");
             ModLoader.addName(new ItemStack(HireoMulti, 1, 13), "Scroll Shelf");       
 	        
 	        
             //Bushes
             ModLoader.addName(new ItemStack(BushesMulti, 1),    "Blue Bush");
             ModLoader.addName(new ItemStack(BushesMulti, 1, 1), "Red Bush");
             
             
             //Skinny Column
             
             
             //Column
             
             
 	        //Ores!
 	        ModLoader.addName(new ItemStack(OreBlockMulti, 1),     "Platinum Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 1),  "Iron Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 2),  "Gold Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 3),  "Tin Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 4),  "Copper Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 5),  "Mithril Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 6),  "Adamantite Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 7),  "Daeyalt Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 8),  "Zinc Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 9),  "Cobalt Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 10), "Titanium Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 11), "Silver Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 12), "Amethyst Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 13), "Calcite Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 14), "Vectrite Ore");
             ModLoader.addName(new ItemStack(OreBlockMulti, 1, 15), "Manganese Ore");
 	        
 	        
             //
 	        
 	        
 	        //Ore Blocks
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1),     "Platinum Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 1),  "Cobalt Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 2),  "Copper Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 3),  "Mithril Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 4),  "Adamantite Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 5),  "Daeyalt Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 6),  "Tin Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 7),  "Titanium block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 8),  "Silver Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 9),  "Vectrite Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 10), "Calcite Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 11), "Steel Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 12), "Onyx Block");
             ModLoader.addName(new ItemStack(MetalBlockMulti, 1, 13), "Amethyst Block");
             
             
             //Lanterns
             ModLoader.addName(new ItemStack(LanternMulti, 1),    "Lantern");
             ModLoader.addName(new ItemStack(LanternMulti, 1, 1), "Lantern");
 	}
 	
 	
 	public void addSmelts()
 	{
 		//NetherMulti Meta Smelts
 		//Onyx
 		FurnaceRecipes.smelting().addSmelting(NetherMulti.blockID, new ItemStack(OnyxIngot));
 		//Iridium
 		FurnaceRecipes.smelting().addSmelting(NetherMulti.blockID, 5, new ItemStack(mod_ReactionCraft.IridiumIngot));
 		//nether lapis to lapis lazuli
 		FurnaceRecipes.smelting().addSmelting(NetherMulti.blockID, 8, new ItemStack(Item.dyePowder, 4, 4));
 	
 	//OreMulti Meta Smelts
 		//mithril
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 5, new ItemStack(mod_ReactionCraft.MithrilIngot));
         //silver related
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 11, new ItemStack(SilverIngot, 1));
         //cobalt
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 9, new ItemStack(CobaltIngot, 1));                       
         //adamantite
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 6, new ItemStack(AdamantiteIngot, 1));
         //Copper
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 4, new ItemStack(CopperIngot, 1));
         //Tin
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 3, new ItemStack(TinIngot, 1));
         //Calcite
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 13, new ItemStack(CalciteIngot, 1));
         //Amethyst
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 12, new ItemStack(AmethystIngot, 1));
         //Dayealt
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 7, new ItemStack(DaeyaltIngot, 1));
         //Titanium
         FurnaceRecipes.smelting().addSmelting(OreBlockMulti.blockID, 10, new ItemStack(TitaniumIngot, 1));
         
         
         
         //platinum
         ModLoader.addSmelting(OreBlockMulti.blockID, new ItemStack(PlatinumIngot, 1));
         
         //Misc Recipes\\
         
         //Exo marble to dark marble
         ModLoader.addSmelting(Marblec.blockID, new ItemStack(HireoMulti, 1, 12));
         
         
         
         //magma- to -magmastone              	
 		//FurnaceRecipes.smelting().addSmelting(MiscMulti.blockID, 8, new ItemStack(MiscMulti, 1, 5));
 		
 		//Nether Recipes\\ 
 		
 		//Nether Iron                		
 		FurnaceRecipes.smelting().addSmelting(NetherMulti.blockID, 7, new ItemStack(Item.ingotIron, 1));
         
         //food
 		ModLoader.addSmelting(RawBacon.shiftedIndex, new ItemStack(Bacon, 1));
 		ModLoader.addSmelting(RawNuggets.shiftedIndex, new ItemStack(ChickenNuggets, 1));
 		
 		
         //dust
         ModLoader.addSmelting(ManganeseDust.shiftedIndex, new ItemStack(ManganeseIngot, 1));
         
         //silver
         ModLoader.addSmelting(SilverIngot.shiftedIndex, new ItemStack(RefinedSilverIngot, 1));
         
         //charcoal
         ModLoader.addSmelting(Cherrywood.blockID, new ItemStack(Item.coal, 1));
         
 		//nether smelting bs
         ModLoader.addSmelting(BloodStoneDust.shiftedIndex, new ItemStack(BloodStoneIngot, 1));
         ModLoader.addSmelting(Block.obsidian.blockID, new ItemStack(mod_ReactionCraft.ObsidianIngot, 2));
         ModLoader.addSmelting(GoldClump.shiftedIndex, new ItemStack(RefinedGoldIngot, 1));
         
         //Coral to dyes
         ModLoader.addSmelting(OrangeCoral.blockID, new ItemStack(Item.dyePowder, 1, 14));
         ModLoader.addSmelting(GreenCoral.blockID, new ItemStack(Item.dyePowder, 1, 2));
         ModLoader.addSmelting(PurpleCoral.blockID, new ItemStack(Item.dyePowder, 1, 5));
         ModLoader.addSmelting(BrownCoral.blockID, new ItemStack(Item.dyePowder, 1, 3));
         ModLoader.addSmelting(BlueCoral.blockID, new ItemStack(Item.dyePowder, 1, 4));
         ModLoader.addSmelting(LightBlueCoral.blockID, new ItemStack(Item.dyePowder, 1, 12));
         ModLoader.addSmelting(LightGreenCoral.blockID, new ItemStack(Item.dyePowder, 1, 10));
         ModLoader.addSmelting(PinkCoral.blockID, new ItemStack(Item.dyePowder, 1, 9));
         ModLoader.addSmelting(LightBlueOrangeCoral.blockID, new ItemStack(Item.dyePowder, 1, 12));
         ModLoader.addSmelting(LightBlueCoral2.blockID, new ItemStack(Item.dyePowder, 1, 2));
         ModLoader.addSmelting(DarkGreenCoral.blockID, new ItemStack(DarkGreenDye));
         ModLoader.addSmelting(RedCoral.blockID, new ItemStack(Item.dyePowder, 1, 1));
         ModLoader.addSmelting(MagentaCoral.blockID, new ItemStack(Item.dyePowder, 1, 1));
         
         //plants to raw stuff / dyes3
         ModLoader.addSmelting(Bamboo.blockID, new ItemStack(DriedBamboo));
         ModLoader.addSmelting(RcTallGrass.blockID, new ItemStack(Straw));
         ModLoader.addSmelting(NetherCane.blockID, new ItemStack(NetherCaneDust));
         
         ///Desert Ore           	
 		ModLoader.addSmelting(DesertGold.blockID, new ItemStack(Item.ingotGold, 1));
 		
		//
 		ModLoader.addSmelting(Item.ingotIron.shiftedIndex, new ItemStack(SuperHeatedIron, 1));
 	}
 	
 	
 	public void TileEntites()
 	{
 		ModLoader.registerTileEntity(TileEntityCrate.class, "RC chest");
 	}
 	
 	
 	public void Recipes()
 	{
 		//ModLoader.takenFromCrafting(var0, var1, var2)
 		
 		//MinecraftForge.getBlockHarvestLevel(block, metadata, toolClass)
 		//ForgeHooks.onTakenFromCrafting(player, Flintchisel, craftMatrix);
 	}
 	
 	
 	public void AddMob()
 	{
 				//Giant Bee
 				ModLoader.registerEntityID(EntityBee.class, "Bee", 141);
 		        ModLoader.addSpawn(EntityBee.class, 1, 4, 4, EnumCreatureType.creature);
 				
 				//   ***MageApprentice***
 				ModLoader.registerEntityID(EntityMage1.class, "Mage1", 141);
 		        ModLoader.addSpawn(EntityMage1.class, 1, 4, 4, EnumCreatureType.creature);
 				
 				
 				//  ***MageMaster***
 		        ModLoader.registerEntityID(EntityMage2.class, "Mage2", 141);
 		        ModLoader.addSpawn(EntityMage2.class, 1, 4, 4, EnumCreatureType.creature);
 	}
 	
 	
 	public int addFuel (int i, int j)
     {
             if(i== MagmaBucket.shiftedIndex)
                     return 600000;
             return 0;
     }
 	
 	/** Nether Generation **/
 	public void generateNether(World world, Random rand, int baseX, int baseZ)
     {
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new WorldGenRcNether(BloodStone.blockID, 50)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new WorldGenRcNether(NetherMulti.blockID, 5)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
           //NetherMeta Blocks
       	
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 50, 1)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 5, 2)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 5, 4)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 5, 5)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 5, 6)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 5, 7)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 5, 8)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 5, 9)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int x = 0; x < 30; x++)
           {
               int Xcoord = baseX + rand.nextInt(16);
               int Ycoord = rand.nextInt(35);
               int Zcoord = baseZ + rand.nextInt(16);
               (new GenMetaNether(NetherMulti.blockID, 5, 10)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	
       	//GlowShrooms
       	for(int j11 = 0; j11 < 6; j11++)
           {
               int Xcoord = baseX + rand.nextInt(16) + 8;
               int Ycoord = rand.nextInt(128);
               int Zcoord = baseZ + rand.nextInt(16) + 8;
               (new WorldGenRcNether(GlowShroomMulti.blockID, 1)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	for(int j11 = 0; j11 < 6; j11++)
           {
               int Xcoord = baseX + rand.nextInt(16) + 8;
               int Ycoord = rand.nextInt(128);
               int Zcoord = baseZ + rand.nextInt(16) + 8;
               (new GenMetaNether(GlowShroomMulti.blockID, 1, 1)).generate(world, rand, Xcoord, Ycoord, Zcoord);
           }
       	//Nethercane
       	if(rand.nextInt(20) == 0)
           {
               for(int k = 0; k < 40; k++)
               {
                   for(int l = 0; l < 40; l++)
                   {
                       int i1 = rand.nextInt(200);
                       if(world.getBlockId(baseX + l, i1, baseZ + k) != Block.netherrack.blockID || !world.isAirBlock(baseX + l, i1 + 1, baseZ + k))
                       {
                           continue;
                       }
                       int j1 = rand.nextInt(2);
                       if(j1 == 0)
                       {
                           world.setBlock(baseX + l, i1 + 1, baseZ + k, NetherCane.blockID);
                       }
                       if(j1 == 1)
                       {
                           world.setBlock(baseX + l, i1 + 1, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 2, baseZ + k, NetherCane.blockID);
                       }
                       if(j1 == 2)
                       {
                           world.setBlock(baseX + l, i1 + 1, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 2, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 3, baseZ + k, NetherCane.blockID);
                       }
                                           if(j1 == 3)
                       {
                           world.setBlock(baseX + l, i1 + 1, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 2, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 3, baseZ + k, NetherCane.blockID);
                       if(j1 == 4)
                       {
                           world.setBlock(baseX + l, i1 + 1, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 2, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 3, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 1, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 2, baseZ + k, NetherCane.blockID);
                           world.setBlock(baseX + l, i1 + 3, baseZ + k, NetherCane.blockID);
                       }
                                                   world.setBlock(baseX + l, i1 + 4, baseZ + k, Block.netherrack.blockID);
                       }
                   }
               }
               
           }
                   
     }
 	
 	
 	@Override
     public boolean clientSideRequired()
     {
             return false;
     }
 
 	
 	public void generateSurface(World world, Random rand, int baseX, int baseZ)
     {	
   		
     	//Marble gen
     	for(int x = 0; x < 2; x++)
     	{
     		int Xcoord = baseX + rand.nextInt(16);
     		int Ycoord = rand.nextInt(70);
     		int Zcoord = baseZ + rand.nextInt(16);
     		(new WorldGenMinable(Marble.blockID, 10)).generate(world, rand, Xcoord, Ycoord, Zcoord);		
     	}	
     	
     	//Regular Ores
         
     	//Currupted Ores on the surface
     	for(int x = 0; x < 2; x++)
     	{
     		int Xcoord = baseX + rand.nextInt(16);
     		int Ycoord = rand.nextInt(35);
     		int Zcoord = baseZ + rand.nextInt(16);
     		(new WorldGenMinable(BloodStone.blockID, 2)).generate(world, rand, Xcoord, Ycoord, Zcoord);		
     	}
     	for(int x = 0; x < 2; x++)
     	{
     		int Xcoord = baseX + rand.nextInt(16);
     		int Ycoord = rand.nextInt(35);
     		int Zcoord = baseZ + rand.nextInt(16);
     		(new GenMetaMinable(NetherMulti.blockID, 8, 2)).generate(world, rand, Xcoord, Ycoord, Zcoord);		
     	}            
     	
     	//Platinum
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(30);
             int i2 = baseZ + rand.nextInt(16);
             (new WorldGenMinable(OreBlockMulti.blockID, 8)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(30);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 3)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(40);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 4)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(40);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 5)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(45);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 6)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(45);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 7)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(50);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 8)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(50);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 9)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(55);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 10)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(55);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 11)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(55);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 12)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(32);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 13)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
         	int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(31);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 14)).generate(world, rand, i1, k1, i2);
         }
         
         //
         for(int k = 0; k < 5; k++)
         {
         	int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(30);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 8, 15)).generate(world, rand, i1, k1, i2);
         }
         
         //Iron scarecly at 12 , more frequently at 40
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(12);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 3, 1)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 15; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(40);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 5, 1)).generate(world, rand, i1, k1, i2);
         }
         
         //Gold gold ore found at 27, scarcely 18, 22
         for(int k = 0; k < 5; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(18);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 1, 2)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 10; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(22);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 2, 2)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 15; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(27);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(OreBlockMulti.blockID, 5, 2)).generate(world, rand, i1, k1, i2);
         }
         
         //Magma Block
         for(int k = 0; k < 10; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(9);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(BrickMulti.blockID, 3, 6)).generate(world, rand, i1, k1, i2);
         }
         
         //RcBedRock
         for(int k = 0; k < 60; k++)
         {
             int i1 = baseX + rand.nextInt(16);
             int k1 = rand.nextInt(6);
             int i2 = baseZ + rand.nextInt(16);
             (new GenMetaMinable(BrickMulti.blockID, 50, 7)).generate(world, rand, i1, k1, i2);
         }
         
       //bamboo
         if(rand.nextInt(20) == 0)
         {
             for(int k = 0; k < 16; k++)
             {
                 for(int l = 0; l < 16; l++)
                 {
                     int i1 = rand.nextInt(200);
                     if(world.getBlockId(baseX + l, i1, baseZ + k) != Block.grass.blockID || !world.isAirBlock(baseX + l, i1 + 1, baseZ + k))
                     {
                         continue;
                     }
                     int j1 = rand.nextInt(2);
                     if(j1 == 0)
                     {
                         world.setBlock(baseX + l, i1 + 1, baseZ + k, Bamboo.blockID);
                     }
                     if(j1 == 1)
                     {
                         world.setBlock(baseX + l, i1 + 1, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 2, baseZ + k, Bamboo.blockID);
                     }
                     if(j1 == 2)
                     {
                         world.setBlock(baseX + l, i1 + 1, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 2, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 3, baseZ + k, Bamboo.blockID);
                     }
                                         if(j1 == 3)
                     {
                         world.setBlock(baseX + l, i1 + 1, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 2, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 3, baseZ + k, Bamboo.blockID);
                     if(j1 == 4)
                     {
                         world.setBlock(baseX + l, i1 + 1, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 2, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 3, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 1, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 2, baseZ + k, Bamboo.blockID);
                         world.setBlock(baseX + l, i1 + 3, baseZ + k, Bamboo.blockID);
                     }
                                                 world.setBlock(baseX + l, i1 + 4, baseZ + k, Bamboo.blockID);
                     }
                 }
 
             }
 
         }
         //Flowers
         for(int k = 0; k < 1; k++)
         {
             int i1 = baseX + rand.nextInt(16) + 8;
             int k1 = rand.nextInt(128);
             int i2 = baseZ + rand.nextInt(16) + 8;
             (new WorldGenFlowers(FlowerMulti.blockID)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 1; k++)
         {
             int i1 = baseX + rand.nextInt(16) + 8;
             int k1 = rand.nextInt(128);
             int i2 = baseZ + rand.nextInt(16) + 8;
             (new WorldGenMetaFlowers(FlowerMulti.blockID, 1)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 1; k++)
         {
             int i1 = baseX + rand.nextInt(16) + 8;
             int k1 = rand.nextInt(128);
             int i2 = baseZ + rand.nextInt(16) + 8;
             (new WorldGenFlowers(RcTallGrass.blockID)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 1; k++)
         {
             int i1 = baseX + rand.nextInt(16) + 8;
             int k1 = rand.nextInt(128);
             int i2 = baseZ + rand.nextInt(16) + 8;
             (new WorldGenFlowers(BushesMulti.blockID)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 1; k++)
         {
             int i1 = baseX + rand.nextInt(16) + 8;
             int k1 = rand.nextInt(128);
             int i2 = baseZ + rand.nextInt(16) + 8;
             (new WorldGenMetaFlowers(BushesMulti.blockID, 1)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 1; k++)
         {
             int i1 = baseX + rand.nextInt(16) + 8;
             int k1 = rand.nextInt(128);
             int i2 = baseZ + rand.nextInt(16) + 8;
             (new WorldGenMetaFlowers(CactusMulti.blockID, 1)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 1; k++)
         {
             int i1 = baseX + rand.nextInt(16) + 8;
             int k1 = rand.nextInt(128);
             int i2 = baseZ + rand.nextInt(16) + 8;
             (new WorldGenFlowers(CactusMulti.blockID)).generate(world, rand, i1, k1, i2);
         }
         for(int k = 0; k < 1; k++)
         {
             int i1 = baseX + rand.nextInt(16) + 8;
             int k1 = rand.nextInt(128);
             int i2 = baseZ + rand.nextInt(16) + 8;
             (new WorldGenFlowers(ReedPlant.blockID)).generate(world, rand, i1, k1, i2);
         }
         
         // Trees!
         
         BiomeGenBase biome = world.getWorldChunkManager().getBiomeGenAt(baseX, baseZ); 
         WorldGenReactionCraftTree tree = new WorldGenReactionCraftTree(); 
         if(biome == BiomeGenBase.plains || biome == BiomeGenBase.forest ||
         		biome == BiomeGenBase.forestHills || biome == BiomeGenBase.extremeHillsEdge ||
         		biome == BiomeGenBase.extremeHills || biome == BiomeGenBase.desertHills ||
         		biome == BiomeGenBase.desert || biome == BiomeGenBase.swampland ||
         		biome == BiomeGenBase.taiga || biome == BiomeGenBase.taigaHills)
         { 
         	for(int x = 0; x < 1; x++)
         	{
         		int Xcoord = baseX + rand.nextInt(16); 
         		int Zcoord = baseZ + rand.nextInt(16); 
         		int i = world.getHeightValue(Xcoord, Zcoord); 
         		tree.generate(world, rand, Xcoord, i, Zcoord); 
         	}
         } 
     
         //Coral Generation
   
         	for(int f = 0; f < 200;f++)
         	{
         	int x = baseX + rand.nextInt(16);
         	int y = rand.nextInt(128);
         	int z = baseZ + rand.nextInt(16);
         	new WaterGeneration(OrangeCoral.blockID, 1).generate(world, rand, x, y, z);
         	System.out.println("lala: "+x+", "+y+", "+z);
         	}
         	for(int f = 0; f < 200;f++)
         	{
         	int x = baseX + rand.nextInt(16);
         	int y = rand.nextInt(128);
         	int z = baseZ + rand.nextInt(16);
         	new WaterGeneration(GreenCoral.blockID, 1).generate(world, rand, x, y, z);
         	System.out.println("l: "+x+", "+y+", "+z);
         	}
         	for(int f = 0; f < 200;f++)
         	{
         	int x = baseX + rand.nextInt(16);
         	int y = rand.nextInt(128);
         	int z = baseZ + rand.nextInt(16);
         	new WaterGeneration(Block.sponge.blockID, 1).generate(world, rand, x, y, z);
         	System.out.println("la: "+x+", "+y+", "+z);
         	}
         	
         	
         	
         	//not working below
         	//Soon to be sea weed gen
         	 if(rand.nextInt(20) == 0)
              {
                  for(int k = 0; k < 16; k++)
                  {
                      for(int l = 0; l < 16; l++)
                      {
                          int i1 = rand.nextInt(200);
                          if(world.getBlockId(baseX + l, i1, baseZ + k) != Block.sand.blockID || !world.isAirBlock(baseX + l, i1 + 1, baseZ + k))
                          {
                              continue;
                          }
                          int j1 = rand.nextInt(2);
                          if(j1 == 0)
                          {
                              world.setBlock(baseX + l, i1 + 1, baseZ + k, SeaWeed.blockID);
                          }
                          if(j1 == 1)
                          {
                              world.setBlock(baseX + l, i1 + 1, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 2, baseZ + k, SeaWeed.blockID);
                          }
                          if(j1 == 2)
                          {
                              world.setBlock(baseX + l, i1 + 1, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 2, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 3, baseZ + k, SeaWeed.blockID);
                          }
                                              if(j1 == 3)
                          {
                              world.setBlock(baseX + l, i1 + 1, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 2, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 3, baseZ + k, SeaWeed.blockID);
                          if(j1 == 4)
                          {
                              world.setBlock(baseX + l, i1 + 1, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 2, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 3, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 1, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 2, baseZ + k, SeaWeed.blockID);
                              world.setBlock(baseX + l, i1 + 3, baseZ + k, SeaWeed.blockID);
                          }
                                                      world.setBlock(baseX + l, i1 + 4, baseZ + k, SeaWeed.blockID);
                          }
                      }
 
                  }
 
              }
     
     }    
 	
     @Override
     public boolean serverSideRequired()
     {
             return false;
     }
 }
