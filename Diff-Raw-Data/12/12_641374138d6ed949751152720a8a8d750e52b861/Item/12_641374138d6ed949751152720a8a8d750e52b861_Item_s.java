 package items;
 
 import passivebonuses.PassiveBonus;
 import passivebonuses.PassiveBonusAttackSpeed;
 import passivebonuses.PassiveBonusCriticalStrike;
 import passivebonuses.PassiveBonusDamageAll;
 import passivebonuses.PassiveBonusDefense;
 import passivebonuses.PassiveBonusDodge;
 import passivebonuses.PassiveBonusFallHeight;
 import passivebonuses.PassiveBonusIntellect;
 import passivebonuses.PassiveBonusJumpHeight;
 import passivebonuses.PassiveBonusSpeed;
 import passivebonuses.PassiveBonusStamina;
 import passivebonuses.PassiveBonusStrength;
 import utils.ActionbarItem;
 import auras.Aura;
 import auras.AuraHeavensReprieve;
 import auras.AuraPeriodicModifier;
 import auras.AuraSmartHeal;
 import entities.EntityProjectile;
 import enums.EnumArmor;
 import enums.EnumItemQuality;
 import enums.EnumToolMaterial;
 
 /**
  * <code>Item extends ActionbarItem</code> and defines the class for most non-<code>Block</code> 
  * things within the player's inventory. 
  * All <code>Items</code> are declared in this class, and all possible <code>Items</code> must extend 
  * <code>Item</code>. This ensures general consistency, and the ability to easily access all the possible
  * <code>Items</code> through the use of <code>Item.itemsList[]</code>. <code>Item.itemsList[]</code> holds
  * all Items, and all extension of <code>Item</code>; however casting must be performed to access 
  * methods from a subclass of <code>Item</code>.
  * <br><br>
  * Items are all initialized with default settings, including an <code>EnumToolMaterial</code> of "FIST" and 
  * a name of "Unnamed". Through the use of setters, many method calls may be attached to the declaration such
  * as, for example, the declaration of a copperAxe:
  * <br>
  * <code>public static Item copperAxe = new ItemToolAxe(1).setDamageDone(4).setName("copper Axe").setIconPosition(0, 7).setToolMaterial(EnumToolMaterial.COPPER);</code>
  * <br> 
  * Here, <code>copperAxe</code> is customized significantly through the many possible setters, each changing 
  * a variable to suit the need of the specific <code>Item</code> (or <code>Item</code> Extension). Specifically,
  * <code>copperAxe</code> is declared as an <code>ItemToolAxe</code> with:
  * <li>An itemID of 1
  * <li>4 damage done to enemies
  * <li>A name of "copper Axe"
  * <li>An <code>EnumToolMaterial</code> of <code>COPPER</code>
  * <li>An icon position on items.png of (0,7)
  * <br>
  * This allows <code>Item</code> to be an extremely versatile class with many possible combinations of
  * variables, and possible <code>Items</code> even without extension. <b>NOTE: the name must be
  * set to something besides "Unnamed" or the <code>Item</code> will not be kept track of in the crafting 
  * manager or inventory totals. </b>
  * <br><br>
  * <code>Item</code>'s constructor is protected, to prevent a random declaration of <code>Item</code>. This 
  * could easily corrupt the <code>itemsList[]</code>, permanently damaging the save file or crashing the game.
  * With a protected constructor, all <code>Items</code> must be declared here upon creation and can never be 
  * redeclared (provided someone doesn't do something ridiculously stupid).
  * @author      Alec Sobeck
  * @author      Matthew Robertson
  * @version     1.0
  * @since       1.0
  */
 public class Item extends ActionbarItem
 {
 	/** Each tier of gear has a material - this is used to find a set bonus */
 	protected EnumToolMaterial material;
 	public EnumItemQuality itemQuality;	
 	protected int damage;
 	protected int durability;
 	protected boolean isContainer;
 	protected int toolType;
 	protected String onUseSound;
 	
 	/**
 	 * Constructs a new <code>Item</code> that will be stored in the <code>itemsList[]</code> at the index 
 	 * of the itemID, allowing for easy access of the <code>Item</code> later. <code>Item</code> has a 
 	 * protected constructor, so that <code>Items</code> can't be declared
 	 * outside of <code>Item.java</code>. This should prevent random Items from overwriting random parts of
 	 * the <code>itemsList[]</code>, possibly corrupting the save or crashing the game.
 	 * <br><br>
 	 * Additional customization can be performed after creation of an <code>Item</code> through the use of 
 	 * setters (see the class comment for an example).
 	 * @param i the unique itemID of the <code>Item</code> being created
 	 */
 	protected Item(int i)
 	{
 		super(i);
 		this.id = i + itemIndex;
 		isContainer = false;
 		durability = -1;
 		damage = 0;
 		material = EnumToolMaterial.FIST;
 		itemQuality = EnumItemQuality.COMMON;
 		onUseSound = "New Item";
 		
 		if(itemsList[id] != null)
 		{
 			System.out.println(new StringBuilder().append("Conflict@ itemsList").append(id).toString());
 			throw new RuntimeException(new StringBuilder().append("Conflict@ itemsList").append(id).toString());
 		}
 		itemsList[id] = this;		
 	}
 
 	/**
 	 * Creates a shallow copy, copying any references and primitive types to the new Item. 
 	 * @param item the item to create a shallow copy of
 	 */
 	public Item(Item item)
 	{
 		super(item);
 		this.material = item.material;
 		this.itemQuality = item.itemQuality;
 		this.damage = item.damage;
 		this.durability = item.durability;
 		this.isContainer = item.isContainer;
 		this.toolType = item.toolType;
 		this.onUseSound = item.onUseSound;
 	}
 	
 	protected Item setContainerItem(boolean flag)
 	{
 		isContainer = flag;
 		return this;
 	}
 	/**
 	 * Overrides ActionbarItem.setMaxStackSize(String) to make Item declarations cleaner
 	 */
 	protected Item setMaxStackSize(int i)
 	{
 		maxStackSize = i;
 		return this;
 	}
 	
 	protected Item setDurability(int i)
 	{
 		durability = i;
 		return this;
 	}
 	
 	protected Item setItemQuality(EnumItemQuality quality)
 	{
 		this.itemQuality = quality;
 		return this;
 	}
 	
 	/**
 	 * Overrides ActionbarItem.setExtraTooltipInformation(String) to make Item declarations cleaner
 	 */
 	public Item setExtraTooltipInformation(String info)
 	{
 		this.extraTooltipInformation = info;
 		return this;
 	}
 	
 	public Item setOnUseSound(String s){
 		onUseSound = s;
 		return this;
 	}
 	
 	protected Item setToolType(int i){
 		toolType = i;
 		return this;
 	}
 	
 	protected Item setToolMaterial(EnumToolMaterial mat){
 		material = mat;
 		return this;
 	}
 
 	/**
 	 * Overrides ActionbarItem.setName(String) to make Item declarations cleaner
 	 */
 	protected Item setName(String name)
 	{
 		this.name = name;
 		return this;
 	}
 	
 	/**
 	 * Overrides ActionbarItem.setIconPosition(int, int) to make Item declarations cleaner
 	 */
 	protected Item setIconPosition(int x, int y)
 	{
 		iconX = x;
 		iconY = y;
 		return this;
 	}	
 	
 	public boolean getIsContainer()
 	{
 		return isContainer;
 	}
 	
 	public int getDurability()
 	{
 		return durability;
 	}
 	
 	public EnumItemQuality getItemQuality()
 	{
 		return itemQuality;
 	}
 	
 	public int getDamage()
 	{
 		return damage;
 	}
 	
 	public int getToolType()
 	{
 		return toolType;
 	}
 	
 	public EnumToolMaterial getToolMaterial()
 	{
 		return material;
 	}
 		
 	/**
 	 * Gets the stats of the given item. Basic Items shouldn't have stats, but other Items such as extensions of 
 	 * ItemTool or ItemArmor can override this method and return an actual array of values.
 	 * @return an array of this Item's stats
 	 */
 	public String[] getStats()
 	{
 		if(damage > 0)
 		{
 			return new String[] { "Damage: " + damage };
 		}
 		else
 		{
 			return new String[] { };
 		}
 	}
 			
 	public static final Item[] itemsList = new Item[spellIndex];
 	/** Item Declarations **/	
 
 	public static Item copperAxe = new ItemToolAxe(1).setDamageDone(4).setName("Copper Axe").setIconPosition(16 ,8).setToolMaterial(EnumToolMaterial.COPPER);	
 	public static Item copperPickaxe = new ItemToolPickaxe(2).setDamageDone(3).setName("Copper Pickaxe").setIconPosition(16, 7).setToolMaterial(EnumToolMaterial.COPPER);	
 	public static Item copperSword = new ItemToolSword(4).setDamageDone(6).setName("Copper Sword").setIconPosition(16, 6).setToolMaterial(EnumToolMaterial.COPPER);	
 	
 	public static Item bronzeAxe = new ItemToolAxe(5).setDamageDone(5).setName("Bronze Axe").setIconPosition(17, 8).setToolMaterial(EnumToolMaterial.BRONZE);	
 	public static Item bronzePickaxe = new ItemToolPickaxe(6).setDamageDone(4).setName("Bronze Pickaxe").setIconPosition(17, 7).setToolMaterial(EnumToolMaterial.BRONZE);	
 	public static Item bronzeSword = new ItemToolSword(8).setDamageDone(7).setName("Bronze Sword").setIconPosition(17, 6).setToolMaterial(EnumToolMaterial.BRONZE);	
 	
 	public static Item ironAxe = new ItemToolAxe(9).setDamageDone(7).setName("Iron Axe").setIconPosition(18, 8).setToolMaterial(EnumToolMaterial.IRON);	
 	public static Item ironPickaxe = new ItemToolPickaxe(10).setDamageDone(4).setName("Iron Pickaxe").setIconPosition(18, 7).setToolMaterial(EnumToolMaterial.IRON);	
 	public static Item ironSword = new ItemToolSword(12).setDamageDone(9).setName("Iron Sword").setIconPosition(18, 6).setToolMaterial(EnumToolMaterial.IRON);	
 	
 	public static Item silverAxe = new ItemToolAxe(13).setDamageDone(8).setName("Silver Axe").setIconPosition(19, 8).setToolMaterial(EnumToolMaterial.SILVER);	
 	public static Item silverPickaxe = new ItemToolPickaxe(14).setDamageDone(5).setName("Silver Pickaxe").setIconPosition(19, 7).setToolMaterial(EnumToolMaterial.SILVER);	
 	public static Item silverSword = new ItemToolSword(16).setDamageDone(11).setName("Silver Sword").setIconPosition(19, 6).setToolMaterial(EnumToolMaterial.SILVER);	
 	
 	public static Item goldAxe = new ItemToolAxe(17).setDamageDone(9).setName("Gold Axe").setIconPosition(20, 8).setToolMaterial(EnumToolMaterial.GOLD);	
 	public static Item goldPickaxe = new ItemToolPickaxe(18).setDamageDone(7).setName("Gold Pickaxe").setIconPosition(20, 7).setToolMaterial(EnumToolMaterial.GOD).setItemQuality(EnumItemQuality.LEGENDARY);	
 	public static Item goldSword = new ItemToolSword(20).setDamageDone(14).setName("Gold Sword").setIconPosition(20, 6).setToolMaterial(EnumToolMaterial.GOLD);	
 	
 	public static Item copperIngot = new Item(51).setIconPosition(3, 9).setName("Copper Ingot");
 	public static Item tinIngot = new Item(52).setIconPosition(3, 10).setName("Tin Ingot");
 	public static Item bronzeIngot = new Item(53).setIconPosition(3, 11).setName("Bronze Ingot");
 	public static Item ironIngot = new Item(54).setIconPosition(3, 12).setName("Iron Ingot");
 	public static Item silverIngot = new Item(55).setIconPosition(3, 13).setName("Silver Ingot");
 	public static Item goldIngot = new Item(56).setIconPosition(3, 14).setName("Gold Ingot");
 	
 	public static Item copperOre = new Item(57).setIconPosition(0, 9).setName("Copper Ore");
 	public static Item tinOre = new Item(58).setIconPosition(0, 10).setName("Tin Ore");
 	public static Item bronzeOre = new Item(59).setIconPosition(0, 0).setName("Bronze Ore");
 	public static Item ironOre = new Item(60).setIconPosition(0, 11).setName("Iron Ore");
 	public static Item silverOre = new Item(61).setIconPosition(0, 12).setName("Silver Ore");
 	public static Item goldOre = new Item(62).setIconPosition(0, 13).setName("Gold Ore");
 	public static Item coal = new Item(63).setIconPosition(1, 9).setName("Coal");
 	public static Item diamond = new Item(64).setIconPosition(2, 12).setName("Diamond");
 	public static Item ruby = new Item(65).setIconPosition(2, 11).setName("Ruby");
 	public static Item emerald = new Item(66).setIconPosition(2, 10).setName("Emerald");
 	public static Item sapphire = new Item(67).setIconPosition(2, 9).setName("Sapphire");
 	public static Item opal = new Item(68).setIconPosition(2, 13).setName("Opal");
 	public static Item jasper = new Item(69).setIconPosition(2, 14).setName("Jasper");
 	
 	public static Item copperHelmet = new ItemArmorHelmet(100).setArmorType(EnumArmor.COPPER).setIconPosition(16, 0).setName("Copper Helmet");
 	public static Item copperBody = new ItemArmorBody(101).setArmorType(EnumArmor.COPPER).setIconPosition(16, 1).setName("Copper Body");
 	public static Item copperPants = new ItemArmorPants(102).setArmorType(EnumArmor.COPPER).setIconPosition(16, 2).setName("Copper Legguards");
 	public static Item copperBoots = new ItemArmorBoots(103).setArmorType(EnumArmor.COPPER).setIconPosition(16, 3).setName("Copper Boots");
 	public static Item copperGloves = new ItemArmorGloves(104).setArmorType(EnumArmor.COPPER).setIconPosition(16, 4).setName("Copper Gloves");
 	public static Item copperBelt = new ItemArmorBelt(105).setArmorType(EnumArmor.COPPER).setIconPosition(16, 5).setName("Copper Belt");
 	
 	public static Item bronzeHelmet = new ItemArmorHelmet(106).setArmorType(EnumArmor.BRONZE).setIconPosition(17, 0).setName("Bronze Helmet");
 	public static Item bronzeBody = new ItemArmorBody(107).setArmorType(EnumArmor.BRONZE).setIconPosition(17, 1).setName("Bronze Body");
 	public static Item bronzePants = new ItemArmorPants(108).setArmorType(EnumArmor.BRONZE).setIconPosition(17, 2).setName("Bronze Legguards");
 	public static Item bronzeBoots = new ItemArmorBoots(109).setArmorType(EnumArmor.BRONZE).setIconPosition(17, 3).setName("Bronze Boots");
 	public static Item bronzeGloves = new ItemArmorGloves(110).setArmorType(EnumArmor.BRONZE).setIconPosition(17, 4).setName("Bronze Gloves");
 	public static Item bronzeBelt = new ItemArmorBelt(111).setArmorType(EnumArmor.BRONZE).setIconPosition(17, 5).setName("Bronze Belt");
 	
 	public static Item ironHelmet = new ItemArmorHelmet(112).setArmorType(EnumArmor.IRON).setIconPosition(18, 0).setName("Iron Helmet");
 	public static Item ironBody = new ItemArmorBody(113).setArmorType(EnumArmor.IRON).setIconPosition(18, 1).setName("Iron Body");
 	public static Item ironPants = new ItemArmorPants(114).setArmorType(EnumArmor.IRON).setIconPosition(18, 2).setName("Iron Legguards");
 	public static Item ironBoots = new ItemArmorBoots(115).setArmorType(EnumArmor.IRON).setIconPosition(18, 3).setName("Iron Boots");
 	public static Item ironGloves = new ItemArmorGloves(116).setArmorType(EnumArmor.IRON).setIconPosition(18, 4).setName("Iron Gloves");
 	public static Item ironBelt = new ItemArmorBelt(117).setArmorType(EnumArmor.IRON).setIconPosition(18, 5).setName("Iron Belt");
 	
 	public static Item silverHelmet = new ItemArmorHelmet(118).setArmorType(EnumArmor.SILVER).setIconPosition(19, 0).setName("Silver Helmet");
 	public static Item silverBody = new ItemArmorBody(119).setArmorType(EnumArmor.SILVER).setIconPosition(19, 1).setName("Silver Body");
 	public static Item silverPants = new ItemArmorPants(120).setArmorType(EnumArmor.SILVER).setIconPosition(19, 2).setName("Silver Legguards");	
 	public static Item silverBoots = new ItemArmorBoots(121).setArmorType(EnumArmor.SILVER).setIconPosition(19, 3).setName("Silver Boots");
 	public static Item silverGloves = new ItemArmorGloves(122).setArmorType(EnumArmor.SILVER).setIconPosition(19, 4).setName("Silver Gloves");
 	public static Item silverBelt = new ItemArmorBelt(123).setArmorType(EnumArmor.SILVER).setIconPosition(19, 5).setName("Silver Belt");
 	
 	public static Item goldHelmet = new ItemArmorHelmet(124).setArmorType(EnumArmor.GOLD).setIconPosition(20, 0).setName("Gold Helmet").setItemQuality(EnumItemQuality.RARE);
 	public static Item goldBody = new ItemArmorBody(125).setArmorType(EnumArmor.GOLD).setIconPosition(20, 1).setName("Gold Body").setItemQuality(EnumItemQuality.RARE);
 	public static Item goldPants = new ItemArmorPants(126).setArmorType(EnumArmor.GOLD).setIconPosition(20, 2).setName("Gold Legguards").setItemQuality(EnumItemQuality.RARE);	
 	public static Item goldBoots = new ItemArmorBoots(127).setArmorType(EnumArmor.GOLD).setIconPosition(20, 3).setName("Gold Boots");
 	public static Item goldGloves = new ItemArmorGloves(128).setArmorType(EnumArmor.GOLD).setIconPosition(20, 4).setName("Gold Gloves");
 	public static Item goldBelt = new ItemArmorBelt(129).setArmorType(EnumArmor.GOLD).setIconPosition(20, 5).setName("Gold Belt");
 	
 	public static Item lunariumHelmet = new ItemArmorHelmet(130).setArmorType(EnumArmor.LUNARIUM).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusIntellect(5), new PassiveBonusCriticalStrike(0.03)
 	}).setIconPosition(22, 0).setName("Lunarium Crown");
 	public static Item lunariumBody = new ItemArmorBody(131).setArmorType(EnumArmor.LUNARIUM).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusIntellect(3)
 	}).setIconPosition(22, 1).setName("Lunarum Robe");
 	public static Item lunariumPants = new ItemArmorPants(132).setArmorType(EnumArmor.LUNARIUM).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusIntellect(3)
 	}).setIconPosition(22, 2).setName("Lunarium Pants");	
 	public static Item lunariumBoots = new ItemArmorBoots(133).setArmorType(EnumArmor.LUNARIUM).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusIntellect(3), new PassiveBonusSpeed(0.03)
 	}).setIconPosition(22, 3).setName("Lunarium Boots");
 	public static Item lunariumGloves = new ItemArmorGloves(134).setArmorType(EnumArmor.LUNARIUM).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusIntellect(3)
 	}).setIconPosition(22, 4).setName("Lunarium Gloves");
 	public static Item lunariumBelt = new ItemArmorBelt(135).setArmorType(EnumArmor.LUNARIUM).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusIntellect(3)
 	}).setIconPosition(22, 5).setName("Lunarium Belt");
 	
 	public static Item rocketBoots = new ItemArmorAccessory(136).setIconPosition(0, 0).setName("Rocket Boots");	
 	public static Item ringOfVigor = new ItemArmorAccessory(137).PassiveBonuses(new PassiveBonus[]{ 
 			new PassiveBonusDamageAll(0.1F)
 	}).setIconPosition(15, 2).setName("Ring of Vigor").setExtraTooltipInformation("This ring instills a sense of courage in its wearer.");	
 	public static Item talismanOfWinds = new ItemArmorAccessory(138).PassiveBonuses(new PassiveBonus[]{ 
 			new PassiveBonusSpeed(0.1F)
 	}).setIconPosition(15, 3).setName("Talisman of Winds").setExtraTooltipInformation("May the winds of fortune always be at your back.");	
 	public static Item holyPendant = new ItemArmorAccessory(139).setAuras(new Aura[]{ 
 			new AuraHeavensReprieve()
 	}).setIconPosition(14, 1).setName("Holy Pendant").setExtraTooltipInformation("This relic will destroy itself to save the wearer.");	
 	public static Item goddessesTear = new ItemArmorAccessory(140).PassiveBonuses(new PassiveBonus[]{ 
 			new PassiveBonusIntellect(8)
 	}).setIconPosition(15, 4).setName("Goddess' Tear").setExtraTooltipInformation("This tear contains immense magical power.");	
 	public static Item magicalCloud = new ItemArmorAccessory(141).PassiveBonuses(new PassiveBonus[]{ 
 			new PassiveBonusFallHeight(8)
 	}).setIconPosition(15, 5).setName("Magical Cloud").setExtraTooltipInformation("As light as air itself.");	
 	public static Item berserkersEssence = new ItemArmorAccessory(142).PassiveBonuses(new PassiveBonus[]{ 
 			new PassiveBonusStrength(3), new PassiveBonusAttackSpeed(0.1F)
 	}).setIconPosition(15, 0).setName("Berserker's Essence").setExtraTooltipInformation("You sense deep inner rage within the essence.");	
 	public static Item guardianAmulet = new ItemArmorAccessory(143).PassiveBonuses(new PassiveBonus[]{ 
 			new PassiveBonusDefense(6), 
 	}).setIconPosition(15, 6).setName("Amulet of the Guardian").setExtraTooltipInformation("This amulet contains great defensive power.");	
 	public static Item stole = new ItemArmorAccessory(144).setAuras(new Aura[]{ 
 			new AuraSmartHeal(30, 50, 0.5, false)
 	}).setIconPosition(15, 7).setName("Stole").setExtraTooltipInformation("This stole radiates holy energy.");	
 	public static Item heavyMedal = new ItemArmorAccessory(145).setAuras(new Aura[]{ 
 			new AuraPeriodicModifier(0.75)
 	}).setIconPosition(15, 1).setName("Heavy Medal").setExtraTooltipInformation("The power of this medal protects its wearer.");	
 	public static Item eaglesFeather = new ItemArmorAccessory(146).PassiveBonuses(new PassiveBonus[]{
 		new PassiveBonusJumpHeight(4)	
 	}).setIconPosition(14, 2).setName("Eagle's Feather");
 	public static Item nimblefootCloak = new ItemArmorAccessory(147).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusDodge(0.05)
 	}).setIconPosition(15, 9).setName("Nimblefoot Cloak");
	public static Item mastersBracers = new ItemArmorAccessory(148).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusCriticalStrike(0.04)
	}).setIconPosition(15, 10).setName("Master's Bracers");
 	public static Item heartyPendant = new ItemArmorAccessory(149).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusStamina(5)
 	}).setIconPosition(14, 3).setName("Hearty Pendant");
 	public static Item steadfastShield = new ItemArmorAccessory(150).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusDefense(5)
 	}).setIconPosition(15, 8).setName("Steadfast Shield");
 	public static Item sapphireRing = new ItemArmorAccessory(151).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusCriticalStrike(0.01)
 	}).setIconPosition(14, 4).setName("Sapphire Ring");
 	public static Item emeraldRing = new ItemArmorAccessory(152).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusAttackSpeed(0.01)
 	}).setIconPosition(14, 5).setName("Emerald Ring");
 	public static Item rubyRing = new ItemArmorAccessory(153).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusStrength(1)
 	}).setIconPosition(14, 6).setName("Ruby Ring");
 	public static Item diamondRing = new ItemArmorAccessory(154).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusDodge(0.01)
 	}).setIconPosition(14, 7).setName("Diamond Ring");
 	public static Item opalRing = new ItemArmorAccessory(155).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusDefense(1)
 	}).setIconPosition(14, 8).setName("Opal Ring");
 	public static Item jasperRing = new ItemArmorAccessory(156).PassiveBonuses(new PassiveBonus[]{
 			new PassiveBonusStamina(1)
 	}).setIconPosition(14, 9).setName("Jasper Ring");
 	public static Item goldRing = new ItemArmorAccessory(157).setIconPosition(14, 10).setName("Gold Ring");
 	
 	public static Item t6Helmet = new ItemArmorHelmet(194).setArmorType(EnumArmor.TIER6).setTotalSockets(1).setIntellect(5).setStamina(10).setStrength(5).setDexterity(5).setIconPosition(21, 0).setName("TIER6 Helmet").setItemQuality(EnumItemQuality.RARE);
 	public static Item t6Body = new ItemArmorBody(195).setArmorType(EnumArmor.TIER6).setTotalSockets(2).setIntellect(5).setStamina(10).setStrength(5).setDexterity(5).setIconPosition(21, 1).setName("TIER6 Body").setItemQuality(EnumItemQuality.RARE);
 	public static Item t6Pants = new ItemArmorPants(196).setArmorType(EnumArmor.TIER6).setTotalSockets(3).setIntellect(5).setStamina(10).setStrength(5).setDexterity(5).setIconPosition(0, 0).setName("TIER6 Legguards").setItemQuality(EnumItemQuality.RARE);	
 	public static Item t6Boots = new ItemArmorBoots(197).setArmorType(EnumArmor.TIER6).setTotalSockets(4).setIntellect(5).setStamina(10).setStrength(5).setDexterity(5).setIconPosition(21, 3).setName("TIER6 Boots");
 	public static Item t6Gloves = new ItemArmorGloves(198).setArmorType(EnumArmor.TIER6).setTotalSockets(4).setIntellect(5).setStamina(10).setStrength(5).setDexterity(5).setIconPosition(21, 4).setName("TIER6 Gloves");
 	public static Item t6Belt = new ItemArmorBelt(199).setArmorType(EnumArmor.TIER6).setTotalSockets(4).setIntellect(5).setStamina(10).setStrength(5).setDexterity(5).setIconPosition(21, 5).setName("TIER6 Belt");
 	
 	
 	public static Item copperCoin = new ItemCoin(200).setIconPosition(0, 0).setName("Copper Coin");
 	public static Item silverCoin = new ItemCoin(201).setIconPosition(0, 0).setName("Silver Coin");
 	public static Item goldCoin = new ItemCoin(202).setIconPosition(0, 0).setName("Gold Coin");
 	public static Item platinumCoin = new ItemCoin(203).setIconPosition(0, 0).setName("Platinum Coin");
 		
 	public static Item woodenArrow = new ItemAmmo(300).setProjectile(new EntityProjectile(7, 1, 1, 8f).setSpriteIndex(0, 1).setIsFriendly(true)).setIconPosition(4, 9).setName("Wooden Arrow");
 	public static Item bronzeArrow = new ItemAmmo(301).setProjectile(new EntityProjectile(10, 1, 1, 8f).setSpriteIndex(0, 2).setIsFriendly(true)).setIconPosition(4, 10).setName("Bronze Arrow");
 	public static Item ironArrow = new ItemAmmo(302).setProjectile(new EntityProjectile(13, 1, 1, 8f).setSpriteIndex(0, 3).setIsFriendly(true)).setIconPosition(4, 11).setName("Iron Arrow");
 	public static Item silverArrow = new ItemAmmo(303).setProjectile(new EntityProjectile(16, 1, 1, 8f).setSpriteIndex(0, 4).setIsFriendly(true)).setIconPosition(4, 12).setName("Silver Arrow");
 		
 	public static Item woodenBow = new ItemRanged(310, 8).setCooldownTicks(10).setName("Wooden Bow").setIconPosition(16, 9);
 	
 	public static Item snowball = new ItemThrown(320, 5).setProjectile(new EntityProjectile(5, 1, 1, 11f).setSpriteIndex(0, 0).setIsFriendly(true)).setCooldownTicks(10).setName("Snow Ball").setIconPosition(14, 0);
 	
 	public static Item heartCrystal = new ItemHeartCrystal(340).setIconPosition(7, 0).setName("Heart Crystal");
 	public static Item manaCrystal = new ItemManaCrystal(341).setIconPosition(6, 0).setName("Mana Crystal");
 	public static Item manaPotion1 = new ItemPotionMana(342, 75).setOnUseSound("Potion Drink 1").setIconPosition(7, 1).setName("Minor Mana Potion");
 	public static Item manaPotion2 = new ItemPotionMana(343, 150).setIconPosition(7, 1).setName("Mana Potion");
 	public static Item healthPotion1 = new ItemPotionHealth(344, 75).setOnUseSound("Potion Drink 2").setIconPosition(6, 1).setName("Minor Healing Potion");
 	public static Item healthPotion2 = new ItemPotionHealth(345, 150).setIconPosition(6, 1).setName("Healing Potion");
 	public static Item manaStar = new ItemPotionMana(346, 25).setIconPosition(12, 3).setName("Mana Star");
 		
 	public static Item healingHerb1 = new Item(347).setIconPosition(0, 0).setName("Minor Healing Herb");
 	public static Item healingHerb2 = new Item(348).setIconPosition(0, 0).setName("Healing Herb");
 	public static Item magicHerb1 = new Item(349).setIconPosition(0, 0).setName("Minor Magic Herb");
 	public static Item magicHerb2 = new Item(350).setIconPosition(0, 0).setName("Magic Herb");
 	public static Item vialEmpty = new Item(351).setIconPosition(8, 1).setName("Empty Vial");
 	public static Item vialOfWater = new Item(352).setIconPosition(8, 2).setName("Vial of Water");
 	
 	public static Item regenerationPotion1 = new ItemPotionRegeneration(359, 90, 1, 4, 40).setIconPosition(9, 1).setName("Minor Regen Potion");
 	public static Item regenerationPotion2 = new ItemPotionRegeneration(360, 90, 2, 8, 40).setIconPosition(9, 2).setName("Regen Potion");
 	public static Item regenerationPotion3 = new ItemPotionRegeneration(361, 90, 3, 12, 40).setIconPosition(0, 0).setName("regen potion 3");
 	public static Item regenerationPotion4 = new ItemPotionRegeneration(362, 90, 4, 16, 40).setIconPosition(0, 0).setName("regen potion 4");
 	public static Item attackSpeedPotion1 = new ItemPotionAttackSpeed(363, 180, 1, 0.1F, 1).setIconPosition(9, 3).setName("Minor Attack Speed Potion");
 	public static Item attackSpeedPotion2 = new ItemPotionAttackSpeed(364, 180, 2, 0.2F, 1).setIconPosition(9, 4).setName("Attack Speed Potion");
 	public static Item attackSpeedPotion3 = new ItemPotionAttackSpeed(365, 180, 3, 0.3F, 1).setIconPosition(0, 0).setName("attack speed potion 3");
 	public static Item attackSpeedPotion4 = new ItemPotionAttackSpeed(366, 180, 4, 0.4F, 1).setIconPosition(0, 0).setName("attack speed potion 4");
 	public static Item criticalChancePotion1 = new ItemPotionCriticalBuff(367, 180, 1, 0.1F, 1).setIconPosition(10, 1).setName("Minor Critical Potion");
 	public static Item criticalChancePotion2 = new ItemPotionCriticalBuff(368, 180, 2, 0.2F, 1).setIconPosition(10, 2).setName("Critical Potion");
 	public static Item criticalChancePotion3 = new ItemPotionCriticalBuff(369, 180, 3, 0.3F, 1).setIconPosition(0, 0).setName("critical chance potion 3");
 	public static Item criticalChancePotion4 = new ItemPotionCriticalBuff(370, 180, 4, 0.4F, 1).setIconPosition(0, 0).setName("critical chance potion 4");
 	public static Item damageBuffPotion1 = new ItemPotionDamageBuff(371, 75, 1, 0.1F, 1).setIconPosition(12, 1).setName("Minor Damage Potion");
 	public static Item damageBuffPotion2 = new ItemPotionDamageBuff(372, 75, 2, 0.2F, 1).setIconPosition(12, 2).setName("Damage Potion");
 	public static Item damageBuffPotion3 = new ItemPotionDamageBuff(373, 75, 3, 0.3F, 1).setIconPosition(0, 0).setName("damage buff potion 3");
 	public static Item damageBuffPotion4 = new ItemPotionDamageBuff(374, 75, 4, 0.4F, 1).setIconPosition(0, 0).setName("damage buff potion 4");
 	public static Item dodgePotion1 = new ItemPotionDodge(379, 120, 1, 0.12F, 1).setIconPosition(11, 1).setName("Minor Dodge Potion");
 	public static Item dodgePotion2 = new ItemPotionDodge(380, 120, 2, 0.2F, 1).setIconPosition(11, 2).setName("Dodge Potion");
 	public static Item dodgePotion3 = new ItemPotionDodge(381, 120, 3, 0.3F, 1).setIconPosition(0, 0).setName("dodge potion 3");
 	public static Item dodgePotion4 = new ItemPotionDodge(382, 120, 4, 0.4F, 1).setIconPosition(0, 0).setName("dodge potion 4");
 	public static Item manaRegenerationPotion1 = new ItemPotionManaRegeneration(383, 150, 1, 6, 40).setIconPosition(6, 3).setName("Minor Mana Regen Potion");
 	public static Item manaRegenerationPotion2 = new ItemPotionManaRegeneration(384, 150, 2, 10, 40).setIconPosition(6, 4).setName("Mana Regen Potion");
 	public static Item manaRegenerationPotion3 = new ItemPotionManaRegeneration(385, 150, 3, 15, 40).setIconPosition(0, 0).setName("mana regen potion 3");
 	public static Item manaRegenerationPotion4 = new ItemPotionManaRegeneration(386, 150, 4, 21, 40).setIconPosition(0, 0).setName("mana regen potion 4");
 	public static Item steelSkinPotion1 = new ItemPotionSteelSkin(387, 150, 1, 8, 1).setIconPosition(7, 3).setName("Minor Steel Skin Potion");
 	public static Item steelSkinPotion2 = new ItemPotionSteelSkin(388, 150, 2, 16, 1).setIconPosition(7, 4).setName("Steel Skin Potion");
 	public static Item steelSkinPotion3 = new ItemPotionSteelSkin(389, 150, 3, 24, 1).setIconPosition(0, 0).setName("steel skin potion 3");
 	public static Item steelSkinPotion4 = new ItemPotionSteelSkin(390, 150, 4, 32, 1).setIconPosition(0, 0).setName("steel skin potion 4");
 	public static Item swiftnessPotion1 = new ItemPotionSwiftness(391, 360, 1, 0.15F, 1).setIconPosition(13, 1).setName("Minor Swiftness Potion");
 	public static Item swiftnessPotion2 = new ItemPotionSwiftness(392, 360, 2, 0.3F, 1).setIconPosition(13, 2).setName("Swiftness Potion");
 	public static Item swiftnessPotion3 = new ItemPotionSwiftness(393, 360, 3, 0.45F, 1).setIconPosition(0, 0).setName("swiftness potion 3");
 	public static Item swiftnessPotion4 = new ItemPotionSwiftness(394, 360, 4, 0.6F, 1).setIconPosition(0, 0).setName("swiftness potion 4");
 	public static Item absorbPotion1 = new ItemPotionAbsorb(395, 30, 1, 50, 1).setIconPosition(9, 1).setName("Minor Absorb Potion");
 	public static Item absorbPotion2 = new ItemPotionAbsorb(396, 30, 1, 100, 1).setIconPosition(9, 1).setName("Absorb Potion");
 	
 	public static Item gemDefense1 = new ItemGem(500).PassiveBonuses(new PassiveBonus[] { 
 			new PassiveBonusDefense(2)
 	}).setIconPosition(0, 0).setName("Gem of Stoneskin 1");
 	
 	public static Item gemSmartheal1 = new ItemGem(501).setAuras(new Aura[] { 
 			new AuraSmartHeal(5, 600.0, 0.95, false)
 	}).setIconPosition(0, 0).setName("Gem of Teh Awesome 1");
 	
 }
