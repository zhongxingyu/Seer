 package darkevilmac.MotherOfPearl.Config;
 
 import net.minecraftforge.common.Configuration;
 import tcc.MotherOfPearl.MotherOfPearl;
 
 public class MOPConfiguration {
 
     public static final String CATEGORY_COMPAT = "compat";
 
     public static int blackPearlID;
     public static int bowPearlID;
     public static int knifePearlID;
     public static int oysterID;
     public static int oysterMeatID;
     public static int pearlID;
     public static int shellID;
     public static int blackPearlDiamondID;
     public static int pearlDiamondID;
     public static int pearlIgniterID;
     public static int pearlPortalPlacerID;
     public static int dustIronID;
     public static int dustGoldID;
     public static int dustPearlID;
     public static int dustBlackPearlID;
     public static int mortarID;
     public static int pestleID;
     public static int mortarAndPestleID;
     public static int portableCookerID;
     public static int dustStrangeID;
     public static int magnificentPearlID;
 
     public static int pearlShardID;
     public static int pearlRodID;
 
     public static int shovelPearlID;
     public static int swordPearlID;
     public static int bootsPearlID;
     public static int braShellID;
     public static int chestplatePearlID;
     public static int helmetPearlID;
     public static int leggingsPearlID;
     public static int pearlCobblestoneFenceID;
     public static int pearlstoneBrickFenceID;
     public static int pearlstoneFenceID;
     public static int blackPearlCobblestoneFenceID;
     public static int blackPearlstoneBrickFenceID;
     public static int blackPearlstoneFenceID;
     public static int pearlstoneID;
     public static int blackPearlstoneID;
     public static int pearlstoneBrickID;
     public static int pearlCobblestoneID;
     public static int blackPearlCobblestoneID;
     public static int blackPearlstoneBrickID;
     public static int bootsBlackPearlID;
     public static int chestplateBlackPearlID;
     public static int helmetBlackPearlID;
     public static int leggingsBlackPearlID;
     public static int skirtGrassID;
     public static int shovelBlackPearlID;
     public static int swordBlackPearlID;
     public static int crownShellID;
     public static int hempSandalsID;
     public static int bootsBlackPearlDiamondID;
     public static int chestplateBlackPearlDiamondID;
     public static int helmetBlackPearlDiamondID;
     public static int leggingsBlackPearlDiamondID;
     public static int bootsPearlDiamondID;
     public static int chestplatePearlDiamondID;
     public static int helmetPearlDiamondID;
     public static int leggingsPearlDiamondID;
     public static int pearlFireID;
     public static int pearlPortalID;
     public static int pearlGrassID;
     public static int pearlDirtID;
     public static int pearlLeafID;
     public static int pearlLogID;
     public static int pearlSaplingID;
 
     public static Configuration cfg = MotherOfPearl.cfg;
 
     public static void init() {
         cfg.load();
 
         // Items
         pearlShardID = cfg.get(CATEGORY_COMPAT, "Pearl Shard", 5133).getInt();
         pearlRodID = cfg.get(CATEGORY_COMPAT, "Pearl Rod", 5134).getInt();
 
         blackPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearl", 5100).getInt();
         bootsPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Boots", 5101).getInt();
         bowPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearl Bow", 5102).getInt();
         braShellID = cfg.get(Configuration.CATEGORY_ITEM, "Shell Bra", 5103).getInt();
         chestplatePearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Chestplate", 5104).getInt();
         helmetPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Helmet", 5105).getInt();
         knifePearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearling Knife", 5106).getInt();
         leggingsPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Leggings", 5107).getInt();
         oysterID = cfg.get(Configuration.CATEGORY_ITEM, "Oyster", 5108).getInt();
         oysterMeatID = cfg.get(Configuration.CATEGORY_ITEM, "Oyster Meat", 5109).getInt();
         pearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearl", 5110).getInt();
         shellID = cfg.get(Configuration.CATEGORY_ITEM, "Shell", 5111).getInt();
         shovelPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearl Shovel", 5112).getInt();
         swordPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearl Sword", 5113).getInt();
         bootsBlackPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Boots", 5114).getInt();
         chestplateBlackPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Chestplate", 5115).getInt();
         helmetBlackPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Helmet", 5116).getInt();
         leggingsBlackPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Leggings", 5117).getInt();
         skirtGrassID = cfg.get(Configuration.CATEGORY_ITEM, "Grass Skirt", 5118).getInt();
         shovelBlackPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearl Shovel", 5119).getInt();
         swordBlackPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearl Sword", 5120).getInt();
         crownShellID = cfg.get(Configuration.CATEGORY_ITEM, "Crown of Shells", 5121).getInt();
         hempSandalsID = cfg.get(Configuration.CATEGORY_ITEM, "Hemp Sandals", 5122).getInt();
         pearlIgniterID = cfg.get(Configuration.CATEGORY_ITEM, "Pearl Igniter", 5000).getInt();
         pearlPortalPlacerID = cfg.get(Configuration.CATEGORY_ITEM, "Pearl Portal Placer", 5001).getInt();
         // Diamond
         blackPearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Diamond", 5123).getInt();
         pearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Diamond", 5124).getInt();
         bootsPearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Diamond Boots", 5125).getInt();
         chestplatePearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Diamond Chestplate", 5126).getInt();
         helmetPearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Diamond Helmet", 5127).getInt();
         leggingsPearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Pearly Diamond Leggings", 5128).getInt();
         bootsBlackPearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Diamond Boots", 5129).getInt();
         chestplateBlackPearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Diamond Chestplate", 5130).getInt();
         helmetBlackPearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Diamond Helmet", 5131).getInt();
         leggingsBlackPearlDiamondID = cfg.get(Configuration.CATEGORY_ITEM, "Black Pearly Diamond Leggings", 5132).getInt();
         // Mortar, Pestle, and Dusts
 
         dustIronID = cfg.get(Configuration.CATEGORY_ITEM, "Iron Dust", 5133).getInt();
         dustGoldID = cfg.get(Configuration.CATEGORY_ITEM, "Gold Dust", 5134).getInt();
         dustPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Pearl Dust", 5135).getInt();
         mortarID = cfg.get(Configuration.CATEGORY_ITEM, "Mortar", 5136).getInt();
         pestleID = cfg.get(Configuration.CATEGORY_ITEM, "Pestle", 5137).getInt();
         mortarAndPestleID = cfg.get(Configuration.CATEGORY_ITEM, "Mortar And Pestle", 5138).getInt();
         dustBlackPearlID = cfg.get(Configuration.CATEGORY_ITEM, "BlackPearl Dust", 5139).getInt();
         portableCookerID = cfg.get(Configuration.CATEGORY_ITEM, "Portable Cooker", 5140).getInt();
         dustStrangeID = cfg.get(Configuration.CATEGORY_ITEM, "Strange Dust", 5141).getInt();
         magnificentPearlID = cfg.get(Configuration.CATEGORY_ITEM, "Magnificent Pearl", 5142).getInt();
 
         // Blocks
 
         pearlstoneFenceID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Stone Fence", 518).getInt();
         pearlCobblestoneFenceID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Cobblestone Fence", 519).getInt();
         pearlstoneBrickFenceID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Stone Brick Fence", 520).getInt();
         blackPearlCobblestoneFenceID = cfg.get(Configuration.CATEGORY_BLOCK, "Black Pearly Stone Fence", 522).getInt();
         blackPearlstoneBrickFenceID = cfg.get(Configuration.CATEGORY_BLOCK, "Black Pearly Cobblestone Fence", 523).getInt();
         blackPearlstoneFenceID = cfg.get(Configuration.CATEGORY_BLOCK, "Black Pearly Stone Brick Fence", 524).getInt();
         pearlCobblestoneID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Cobblestone", 516).getInt();
         pearlstoneID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Stone", 510).getInt();
         blackPearlCobblestoneID = cfg.get(Configuration.CATEGORY_BLOCK, "Black Pearly Cobblestone", 517).getInt();
         blackPearlstoneID = cfg.get(Configuration.CATEGORY_BLOCK, "Black Pearly Stone", 511).getInt();
         pearlstoneBrickID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Stone Brick", 512).getInt();
         blackPearlstoneBrickID = cfg.get(Configuration.CATEGORY_BLOCK, "Black Pearly Stone Brick", 513).getInt();
        pearlFireID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Fire", 2000).getInt();
        pearlPortalID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Portal", 2001).getInt();
         pearlGrassID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Grass", 200).getInt();
         pearlDirtID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Dirt", 201).getInt();
         pearlLeafID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Leaf", 2002).getInt();
         pearlLogID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Log", 2003).getInt();
         pearlSaplingID = cfg.get(Configuration.CATEGORY_BLOCK, "Pearly Sapling", 2004).getInt();
 
         cfg.save();
     }
 }
