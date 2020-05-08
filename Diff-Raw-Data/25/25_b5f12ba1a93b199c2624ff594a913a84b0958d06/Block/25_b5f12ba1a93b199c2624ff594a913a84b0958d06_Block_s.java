 package blocks;
 
 import items.Item;
 
 import java.util.Random;
 
 import render.Render;
 import utils.ActionbarItem;
 import utils.ItemStack;
 import enums.EnumBlockMaterial;
 
 
 /**
  * <code>Block extends ActionbarItem and implements , Cloneable</code>
  * <br>
  * <code>Block</code> defines the class for placable ItemStacks in the player's
  * inventory, and the Objects used to render the world and determine where
  * <code>Entities</code> are able to move. All possible <code>Blocks</code> must
  * extend <code>Block</code>, to ensure general consistancy and storage of all
  * Blocks. All <code>Blocks</code> are stored in <code>Block.blocksList[]</code>
  * upon creation, at the index of their <code>blockID</code>, so that they may
  * easily be accessed later. <br>
  * <br>
  * <code>Blocks</code> are generally initialized using
  * <code>protected Block(int)</code>, but <code>Block.air</code> is uniquely
  * created using <code>protected Block()</code> due to there being irregular
  * settings and no need for it to be stored in <code>blocksList[]</code>.
  * <code>Block(int)</code> provides a large amount of standard
  * <code>Block</code> settings that can be changed with the use of setters. An
  * example block declaration (in this case for <code>Block.chest</code>): <br>
  * <code>public static Block chest = new BlockChest(56).setName("chest").setBothBlockWidthAndHeight(2, 2)
  * .setBlockHardness(40.0f).setIconIndex(11, 1).setBlockType(2);</code>
  * <br>
  * To explain this example, a <code>Block</code> is created as an
  * <code>instanceof BlockChest</code> with: <li>A blockID of 56 <li>A blockName
  * of "chest" <li>A render width and texture width of 2x2 blocks (so it renders
  * 12x12 ortho units, and has a texture of 32x32 pixels) <li>A blockHardness of
  * 40.0f (about 20% weaker than stone) <li>An icon position on terrain.png of
  * (11,1) <li>A blockType of 2 (breakable with an axe) <br>
  * In addition to the default settings that otherwise are unmodified. This
  * allows for <code>Block</code>, like <code>Item</code>, to be a very versitile
  * class with the simple use of several different setters. <br>
  * <br>
  * The constructor of <code>Block</code> is protected for the same reason as
  * <code>Item</code>'s, to prevent random redeclaration of a <code>Block</code>.
  * This could cause corruption of the world save file, or cause the game to
  * crash. With a protected constructor, a <code>Block</code> can only be
  * declared here; hopefully preventing corrupt except due to extreme stupid. <br>
  * <br>
  * There is a point of interest regarding rendering of <code>Block</code>. The
  * "universal block constant" width and height is 12 pixels (6 'ortho'(render)
  * units), per block in the 'world map'. This does not change, and likely never
  * will. This size does not even change when the screen is resized, instead more
  * <code>Blocks</code> are displayed. <b>NOTE: this is a 'magic number' not a
  * constant.</b>
  * 
  * @author Alec Sobeck
  * @author Matthew Robertson
  * @version 1.0
  * @since 1.0
  */
 public class Block extends ActionbarItem 
 		implements Cloneable   
 {
 	/** (constant) Block width in pixels, currently this value is 6. Corresponds to the value of Render.BLOCK_SIZE. */
 	public final static int BLOCK_WIDTH = 6;
 	/** (constant) Block height in pixels, currently this is 6. Corresponds to the value of Render.BLOCK_SIZE. */
 	public final static int BLOCK_HEIGHT = 6;
 	protected static final Random random = new Random();
 	protected ItemStack droppedItem;
 	protected int maximumDropAmount;
 	protected int minimumDropAmount;
 	public int metaData;
 	public boolean isOveridable;
 	public boolean hasMetaData;
 	protected char tileMap;
 	protected int bitMap;
 	/** The render width of a texture (in the world) */
 	public double blockWidth; 
 	/** The render height of a texture (in the world) */
 	public double blockHeight; 
 	/** The pixel width of a Block's texture (on a spritesheet) */
 	public double textureWidth; 
 	/** The pixel height of a Block's texture (on a spritesheet) */
 	public double textureHeight; // 
 	protected int gradeOfToolRequired;
 	protected int blockType;
 	protected int blockTier;
 	protected EnumBlockMaterial material;
 	protected boolean breakable;
 	protected double hardness;
 	protected boolean passable;
 	protected boolean isMineable;
 	public boolean isSolid;
 	protected int hRange;
 	protected int lRange;
 
 	/**
 	 * Constructs a special Block- air. This constructor should only ever be
 	 * used for the initial declaration of Block.air.
 	 */
 	private Block() 
 	{
 		super(0);
 		name = "air";
 		extraTooltipInformation = "";
 		isSolid = false;
 		isOveridable = true;
 		blockWidth = 6;
 		blockHeight = 6;
 		textureWidth = 16;
 		textureHeight = 16;
 		iconX = 0;
 		iconY = 0;
 		if (blocksList[id] != null) {
 			System.out.println(new StringBuilder().append("Conflict@ BlockID ").append(id).toString());
 			throw new RuntimeException(new StringBuilder().append("Conflict@ BlockID ").append(id).toString());
 		}
 		blocksList[id] = this;
 	}
 
 	/**
 	 * Constructs a new <code>Block</code> that will be stored in
 	 * <code>blocksList[]</code> at the index of the blockID, allowing for easy
 	 * access to the <code>Block</code> created later. <code>Block</code> has a
 	 * protected constructor, so that a <code>Block</code> can't be randomly
 	 * declared outside of <code>Block.java</code>. This should prevent random
 	 * <code>Blocks</code> from overwriting random parts of the
 	 * <code>blocksList[]</code>, possibly crashing the game or corrupting the
 	 * save file for the world. <br>
 	 * <br>
 	 * Additional customization can be performed after creation of a
 	 * <code>Block</code> through the use of setters (see the class comment for
 	 * an example).
 	 * 
 	 * @param i the unique blockID of the <code>Block</code> being created
 	 */
 	protected Block(int id)
 	{
 		super(id);
 		this.id = id;
 		breakable = true;
 		blockWidth = 6;
 		blockHeight = 6;
 		textureWidth = 16;
 		textureHeight = 16;
 		passable = false;
 		tileMap = ' ';
 		bitMap = 0;
 		maxStackSize = 250;
 		gradeOfToolRequired = 0;
 		blockTier = 0;
 		droppedItem = new ItemStack(this);
 		maximumDropAmount = 1;
 		minimumDropAmount = 1;
 		isOveridable = false;
 		isSolid = true;
 		isMineable = true;
 		extraTooltipInformation = "";
 		iconX = 0;
 		iconY = 0;
 		associatedTextureSheet = Render.TEXTURE_SHEET_TERRAIN_EARTH;
 
 		if (blocksList[id] != null) {
 			System.out.println(new StringBuilder().append("Conflict@ BlockID ").append(id).toString());
 			throw new RuntimeException(new StringBuilder().append("Conflict@ BlockID ").append(id).toString());
 		}
 		blocksList[id] = this;
 	}
 
 	/**
 	 * @deprecated Overrides Object.clone() to provide better and public cloning
 	 *             functionality for Block. Cloned objects, using Block.clone()
 	 *             should return a deep copy, at a new reference.
 	 * @return a deep copy of the current Block
 	 */
 	public Block clone() {
 		try {
 			return (Block) super.clone();
 		} catch (CloneNotSupportedException e) {
 			System.out.println("Cloning not allowed.");
 			return this;
 		}
 	}
 
 	/**
 	 * Creates a deep copy of a Block. Objects are copied, but will not have the same reference, though, Enums references are directly
 	 * copied to the new Block.
 	 * @param block the Block to be copied
 	 * @return a new Block with the same properties as the cloned one
 	 */
 	public Block(Block block) 
 	{
 		this.droppedItem = new ItemStack(block.droppedItem);
 		this.maximumDropAmount = block.maximumDropAmount;
 		this.minimumDropAmount = block.minimumDropAmount;
 		this.metaData = block.metaData;
 		this.isOveridable = block.isOveridable;
 		this.hasMetaData = block.hasMetaData;
 		this.tileMap = block.tileMap;
 		this.bitMap = block.bitMap;
 		this.blockWidth = block.blockWidth;
 		this.blockHeight = block.blockHeight;
 		this.textureWidth = block.textureWidth;
 		this.textureHeight = block.textureHeight;
 		this.gradeOfToolRequired = block.gradeOfToolRequired;
 		this.blockType = block.blockType;
 		this.blockTier = block.blockTier;
 		this.material = block.material;
 		this.breakable = block.breakable;
 		this.hardness = block.hardness;
 		this.passable = block.passable;
 		this.isSolid = block.isSolid;
 		this.hRange = block.hRange;
 		this.isMineable = block.isMineable;
 		this.lRange = block.lRange;
 	}
 
 	/**
 	 * Overrides ActionbarItem.setName(String) to make Block creation more
 	 * friendly
 	 */
 	protected Block setName(String name)
 	{
 		this.name = name;
 		return this;
 	}
 
 	protected Block setBlockHardness(double f)
 	{
 		hardness = f;
 		return this;
 	}
 
 	protected Block setBreakable(boolean flag) 
 	{
 		breakable = flag;
 		return this;
 	}
 
 	protected Block setIconIndex(int x, int y) 
 	{
 		iconY = y;
 		iconX = x;
 		return this;
 	}
 
 	public Block setExtraTooltipInformation(String info) 
 	{
 		this.extraTooltipInformation = info;
 		return this;
 	}
 
 	protected Block setBlockMaterial(EnumBlockMaterial mat) 
 	{
 		material = mat;
 		// gradeOfToolRequired....
 		return this;
 	}
 
 	protected Block setPassable(boolean flag) 
 	{
 		passable = flag;
 		return this;
 	}
 
 	protected Block setMaxStackSize(int i)
 	{
 		maxStackSize = i;
 		return this;
 	}
 
 	protected Block setGradeOfToolRequired(int i) 
 	{
 		gradeOfToolRequired = i;
 		return this;
 	}
 
 	protected Block setBothBlockWidthAndHeight(int x, int y)
 	{
 		setOnlyBlockWorldWidthAndHeight(x * 6, y * 6);
 		setOnlyBlockTextureWidthAndHeight(x * 16, y * 16);
 		return this;
 	}
 
 	protected Block setOnlyBlockWorldWidthAndHeight(int w, int h) 
 	{
 		blockWidth = w;
 		blockHeight = h;
 		hasMetaData = true;
 		// metaData = MetaDataHelper.getBlockMetaDataId(w, h);
 		return this;
 	}
 
 	protected Block setOnlyBlockTextureWidthAndHeight(int w, int h) 
 	{
 		textureWidth = w;
 		textureHeight = h;
 		return this;
 	}
 
 	protected Block setBlockTier(int i)
 	{
 		blockTier = i;
 		return this;
 	}
 
 	protected Block setBlockType(int i)
 	{
 		blockType = i;
 		return this;
 	}
 
 	/**
 	 * Sets if the block allows blocks to be placed by it in the world.
 	 * @param flag whether or not blocks can be placed next to this Block
 	 * @return a reference to this Block
 	 */
 	protected Block setIsSolid(boolean flag)
 	{
 		isSolid = flag;
 		return this;
 	}
 
 	protected Block setHRange(int i) 
 	{
 		hRange = i;
 		return this;
 	}
 
 	protected Block setLRange(int i) 
 	{
 		lRange = i;
 		return this;
 	}
 
 	protected Block setDroppedItem(ItemStack stack, int min, int max) 
 	{
 		droppedItem = stack;
 		minimumDropAmount = min;
 		maximumDropAmount = max;
 		return this;
 	}
 
 	public Block setIsMineable(boolean b){
 		isMineable = b;
 		return this;
 	}
 		
 	public Block setBitMap(int i) 
 	{
 		bitMap = i;
 		// If the block is a general case
 		if (this.getTileMap() == 'g') {
 			if (i <= 15) {
 				this.setIconIndex(i, this.iconY);
 			} else {
 				this.setIconIndex(i - 16, this.iconY + 1);
 			}
 		}
 		// If the block is a pillar
 		else if (this.getTileMap() == 'p') {
 			this.setIconIndex(i, this.iconY);
 		}
 		// If the block is a tree
 		else if (this.getTileMap() == 't') {
 			this.setIconIndex(i, this.iconY);
 		}
 		// If the block is a branch
 		else if (this.getTileMap() == 'b') {
 			// If the branch is a regular branch
 			if (i <= 11) {
 				this.setIconIndex(4 + i, this.iconY);
 			}
 			// If the branch is covered in snow
 			else {
 				this.setIconIndex(4 + (i - 12), this.iconY + 1);
 			}
 		}
 		// If the block is a treetop
 		else if (this.getTileMap() == 'T') {
 			this.setIconIndex(this.iconX + 3 * i, this.iconY);
 		}
 		return this;
 	}
 
 	protected Block setIconIndex(double x, double y) 
 	{
 		iconY = y;
 		iconX = x;
 		return this;
 	}
 
 	protected Block setTileMap(char c) {
 		tileMap = c;
 		return this;
 	}
 
 	protected Block setIsOveridable(boolean flag) {
 		isOveridable = flag;
 		return this;
 	}
 
 	public double getBlockHardness()
 	{
 		return hardness;
 	}
 
 	public boolean getBreakable() 
 	{
 		return breakable;
 	}
 
 	public EnumBlockMaterial getMaterial()
 	{
 		return material;
 	}
 	
 	public boolean getIsMineable(){
 		return isMineable;
 	}
 
 	public boolean getPassable() 
 	{
 		return passable;
 	}
 
 	public double getBlockWidth()
 	{
 		return blockWidth;
 	}
 
 	public double getBlockHeight() 
 	{
 		return blockHeight;
 	}
 
 	public double getTextureWidth() 
 	{
 		return textureWidth;
 	}
 
 	public double getTextureHeight() 
 	{
 		return textureHeight;
 	}
 
 	public int getGradeOfToolRequired() 
 	{
 		return gradeOfToolRequired;
 	}
 
 	public int getBlockTier() 
 	{
 		return blockTier;
 	}
 
 	public int getBlockType()
 	{
 		return blockType;
 	}
 
 	public boolean getIsOveridable()
 	{
 		return isOveridable;
 	}
 
 	public int getHRange() 
 	{
 		return hRange;
 	}
 
 	public int getLRange() 
 	{
 		return lRange;
 	}
 
 	public int getBitMap() {
 		return bitMap;
 	}
 
 	public char getTileMap() {
 		return tileMap;
 	}
 
 	public ItemStack getDroppedItem() 
 	{
 		return (droppedItem != null) ? new ItemStack(droppedItem.getItemID(), (minimumDropAmount + (((maximumDropAmount - minimumDropAmount) > 0) ? random.nextInt(maximumDropAmount - minimumDropAmount) : 0))) : null;
 	}
 
 	public boolean getIsSolid() 
 	{
 		return isSolid;
 	}
 
 	/**
 	 * Overrides ActionbarItem.overrideItemIcon(int, int, int) to make block
 	 * creation easier.
 	 */
 	public Block overrideItemIcon(int x, int y, int associatedSheet)
 	{
 		iconOverrideX = x;
 		iconOverrideY = y;
 		this.associatedTextureSheet = associatedSheet;
 		iconOverriden = true;
 		return this;
 	}
 
 	public static final Block[] blocksList = new Block[2048];
 	/** Block Declarations **/
 
 	public static Block none = new Block(2047).setName("None");
 	public static Block air = new Block();
 	public static Block dirt = new Block(1).setName("Dirt").setTileMap('g')
 			.setBlockHardness(40.0f).setIconIndex(0, 3).setBlockType(1);
 	public static Block stone = new Block(2).setName("Stone").setTileMap('g')
 			.setBlockHardness(70.0f).setIconIndex(0, 0).setBlockType(1);
 	public static Block grass = new BlockGrass(3).setName("Grass")
 			.setTileMap('g').setBlockHardness(40.0f).setIconIndex(0, 4)
 			.setBlockType(1);
 	public static Block sand = new Block(6).setName("Sand").setTileMap('g')
 			.setBlockHardness(30.0f).setIconIndex(0, 2).setBlockType(1);
 	public static Block sandstone = new Block(7).setName("Sandstone")
 			.setTileMap('g').setIconIndex(0, 1).setBlockHardness(70.0f)
 			.setBlockType(1);
 	public static Block cactus = new Block(8).setName("Cactus")
 			.setBlockHardness(1.0f).setIconIndex(2, 3).setPassable(true)
 			.setBlockType(2).setIsSolid(false);
 	public static Block tree = new BlockWood(9).setName("Tree").setTileMap('t')
 			.setBlockHardness(60.0f).setIconIndex(1, 27).setPassable(true)
 			.setBlockType(2).setIsSolid(false);
 	public static Block treebase = new BlockWood(10).setName("tree base")
 			.setTileMap('t').setBlockHardness(60.0f).setIconIndex(0, 27)
 			.setPassable(true).setBlockType(2).setIsSolid(false);
 	public static Block treebranch = new BlockWood(12).setName("tree branch")
 			.setTileMap('b').setBlockHardness(5.0f).setIconIndex(4, 25)
 			.setPassable(true).setIsSolid(false);
 	public static Block treetop = new BlockLeaves(20)
 			.setName("tree top left top").setTileMap('T')
 			.setBlockHardness(5.0f).setIconIndex(4, 27).setPassable(true)
 			.setIsSolid(false);
 	public static Block treetopl2 = new BlockLeaves(21)
 			.setName("tree top left bottom").setTileMap('T')
 			.setBlockHardness(5.0f).setIconIndex(4, 28).setPassable(true)
 			.setIsSolid(false);
 	public static Block treetopc1 = new BlockLeaves(22)
 			.setName("tree top center top").setTileMap('T')
 			.setBlockHardness(5.0f).setIconIndex(5, 27).setPassable(true)
 			.setIsSolid(false);
 	public static Block treetopc2 = new BlockLeaves(23)
 			.setName("tree top center bottom").setTileMap('T')
 			.setBlockHardness(5.0f).setIconIndex(5, 28).setPassable(true)
 			.setIsSolid(false);
 	public static Block treetopr1 = new BlockLeaves(24)
 			.setName("tree top right top").setTileMap('T')
 			.setBlockHardness(5.0f).setIconIndex(6, 27).setPassable(true)
 			.setIsSolid(false);
 	public static Block treetopr2 = new BlockLeaves(25)
 			.setName("tree top right bottom").setTileMap('T')
 			.setBlockHardness(5.0f).setIconIndex(6, 28).setPassable(true)
 			.setIsSolid(false);
 	public static Block gold = new BlockOre(26).setName("Gold Ore Block")
 			.setBlockHardness(10.0f).setHRange(0).setLRange(3)
 			.setIconIndex(15, 11).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.goldOre), 1, 1);
 	public static Block iron = new BlockOre(27).setName("Iron Ore Block")
 			.setBlockHardness(10.0f).setHRange(5).setLRange(10)
 			.setIconIndex(15, 8).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.ironOre), 1, 1);
 	public static Block coal = new BlockOre(28).setName("Coal Ore Block")
 			.setTileMap('g').setHRange(20).setLRange(10)
 			.setBlockHardness(10.0f).setIconIndex(15, 7).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.coal), 1, 1);
 	public static Block diamond = new BlockOre(29).setName("Diamond Ore Block")
 			.setBlockHardness(10.0f).setLRange(10)
 			.setIconIndex(15, 14).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.diamond), 1, 1);
 	public static Block ruby = new BlockOre(30).setName("Ruby Ore Block")
 			.setBlockHardness(10.0f).setLRange(10)
 			.setIconIndex(15, 13).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.ruby), 1, 1);
 	public static Block sapphire = new BlockOre(31).setName("Sapphire Ore Block")
 			.setBlockHardness(10.0f).setLRange(10)
 			.setIconIndex(15, 12).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.sapphire), 1, 1);
 	public static Block emerald = new BlockOre(32).setName("Emerald Ore Block")
 			.setBlockHardness(10.0f).setLRange(10)
 			.setIconIndex(15, 15).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.emerald), 1, 1);
 	public static Block opal = new BlockOre(33).setName("Opal Ore Block")
 			.setBlockHardness(10.0f).setLRange(10)
 			.setIconIndex(15, 16).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.opal), 1, 1);
 	public static Block jasper = new BlockOre(34).setName("Jasper Ore Block")
 			.setBlockHardness(10.0f).setLRange(10)
 			.setIconIndex(15, 17).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.jasper), 1, 1);
 	public static Block copper = new BlockOre(35).setName("Copper Ore Block")
 			.setBlockHardness(55.0f).setHRange(20).setLRange(0)
 			.setIconIndex(15, 9).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.copperOre), 1, 1);
 	public static Block silver = new BlockOre(36).setName("Silver Ore Block")
 			.setBlockHardness(80.0f).setHRange(0).setLRange(5)
 			.setIconIndex(15, 10).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.silverOre), 1, 1);
 	public static Block tin = new BlockOre(37).setName("Tin Ore Block")
 			.setBlockHardness(55.0f).setHRange(10).setLRange(0)
 			.setIconIndex(15, 8).setBlockType(1)
 			.setDroppedItem(new ItemStack(Item.tinOre), 1, 1);
 	public static Block redflower = new Block(38).setName("Red Flower")
 			.setBlockHardness(10.0f).setIconIndex(0, 1).setPassable(true)
 			.setIsOveridable(true).setIsSolid(false);
 	public static Block yellowflower = new Block(39).setName("Yellow Flower")
 			.setBlockHardness(10.0f).setIconIndex(0, 1).setPassable(true)
 			.setIsOveridable(true).setIsSolid(false);
 	public static Block tallgrass = new Block(40).setName("Tall Grass")
 			.setBlockHardness(10.0f).setIconIndex(0, 1).setPassable(true)
 			.setIsOveridable(true).setIsSolid(false);
 	public static Block snowCover = new Block(44).setName("Snow Cover")
 			.setBlockHardness(10.0f).setIconIndex(9, 10).setPassable(true)
 			.setIsOveridable(true)
 			.setDroppedItem(new ItemStack(Item.snowball), 1, 1)
 			.setIsSolid(false);
 	public static Block torch = new BlockLight(48)
 			.setLightStrengthAndRadius(1.0F, 10).setName("Torch")
 			.setBlockHardness(0.0f)
 			.overrideItemIcon(5, 0, Render.TEXTURE_SHEET_ITEMS)
 			.setPassable(true).setIsSolid(false);
 	public static Block adminium = new Block(49).setName("Adminium").setIsMineable(false)
 			.setBlockHardness(8000.0f).setIconIndex(0, 1);
 	public static Block plank = new Block(50).setName("Plank").setTileMap('g')
 			.setBlockHardness(30.0f).setIconIndex(0, 20).setBlockType(2);
 	public static Block sapling = new Block(51).setName("Sapling")
 			.setBlockHardness(5.0f).setIconIndex(10, 4).setBlockType(2)
 			.setIsOveridable(true).setIsSolid(false);
 	public static Block furnace = new Block(52).setName("Furnace")
 			.setBlockHardness(50.0f).setIconIndex(10, 23).setBlockType(1)
 			.setBothBlockWidthAndHeight(2, 2);
 	public static Block craftingTable = new Block(53).setName("Crafting table")
 			.setBlockHardness(50.0f).setIconIndex(14, 23).setBlockType(2)
 			.setBothBlockWidthAndHeight(2, 2);
 	public static Block snow = new Block(54).setName("Snow Block")
 			.setBlockHardness(10.0f).setIconIndex(4, 1).setBlockType(1)
 			.setIsOveridable(true);
 	public static Block heartCrystal = new Block(55)
 			.setName("Heart Crystal Block").setBlockHardness(50.0f)
 			.setIconIndex(1, 1).setBlockType(3)
 			.setBothBlockWidthAndHeight(2, 2)
 			.setDroppedItem(new ItemStack(Item.heartCrystal), 1, 1);
 	public static Block chest = new BlockChest(56, 20).setName("Chest")
 			.setBothBlockWidthAndHeight(2, 2).setBlockHardness(40.0f)
 			.setIconIndex(11, 1).setBlockType(2);
 
 	// Needs recipe
 	public static Block bookshelf = new Block(57).setName("Bookshelf")
 			.setIconIndex(4, 3).setBlockHardness(20.0f).setBlockType(2);
 	public static Block woodTable = new Block(58).setName("Wooden Table")
 			.setPassable(true).setBlockHardness(50.0f).setIconIndex(5, 24)
 			.setBlockType(2).setBothBlockWidthAndHeight(2, 1);
 	public static Block stoneTable = new Block(59).setName("Stone Table")
 			.setPassable(true).setBlockHardness(50.0f).setIconIndex(5, 23)
 			.setBlockType(1).setBothBlockWidthAndHeight(2, 1);
 	public static Block fence = new Block(61).setName("Fence")
 			.setPassable(true).setBlockHardness(50.0f).setIconIndex(3, 4)
 			.setBlockType(2);
 	public static Block woodChairRight = new Block(63)
 			.setName("Right facing wood chair").setPassable(true)
 			.setBlockHardness(50.0f).setIconIndex(5, 24).setBlockType(2);
 	public static Block woodChairLeft = new Block(64)
 			.setName("Left facing wood chair").setPassable(true)
 			.setBlockHardness(50.0f).setIconIndex(4, 24).setBlockType(2);
 	public static Block stoneChairRight = new Block(65)
 			.setName("Right facing stone chair").setPassable(true)
 			.setBlockHardness(50.0f).setIconIndex(5, 23).setBlockType(1);
 	public static Block stoneChairLeft = new Block(66)
 			.setName("Left facing stone chair").setPassable(true)
 			.setBlockHardness(50.0f).setIconIndex(4, 23).setBlockType(1);
 
 	public static Block stonePillar = new BlockPillar(68)
 			.setName("Stone Pillar Block").setTileMap('p').setPassable(true)
 			.setBlockHardness(75.0f).setIconIndex(0, 23).setBlockType(1);
 	public static Block marblePillar = new BlockPillar(71)
 			.setName("Marble Pillar Block").setTileMap('p').setPassable(true)
 			.setBlockHardness(75.0f).setIconIndex(0, 24).setBlockType(1);
 	public static Block goldPillar = new BlockPillar(74)
 			.setName("Gold Pillar Block").setTileMap('p').setPassable(true)
 			.setBlockHardness(75.0f).setIconIndex(0, 25).setBlockType(1);
 	public static Block diamondPillar = new BlockPillar(77)
 			.setName("Diamond Pillar Block").setTileMap('p').setPassable(true)
 			.setBlockHardness(75.0f).setIconIndex(0, 26).setBlockType(1);
 
 	public static Block glass = new Block(79).setName("Glass Block")
 			.setBlockHardness(10.0f).setIconIndex(1, 3).setBlockType(1);
 	public static Block ironChest = new BlockChest(80, 40)
 			.setName("Iron Chest").setBothBlockWidthAndHeight(2, 2)
 			.setBlockHardness(40.0f).setIconIndex(11, 1).setBlockType(2);
 
 	// Backwalls
 	public static Block backAir = new BlockBackWall(128).setIsSolid(false)
 			.setName("backwall air");
	public static Block backDirt = new BlockBackWall(129).setIconIndex(0, 18)
 			.setName("backwall dirt").setBlockHardness(40.0f).setBlockType(1);
	public static Block backStone = new BlockBackWall(130).setIconIndex(0, 19)
 			.setName("backwall stone").setBlockHardness(70.0f).setBlockType(1);
 	
 }
