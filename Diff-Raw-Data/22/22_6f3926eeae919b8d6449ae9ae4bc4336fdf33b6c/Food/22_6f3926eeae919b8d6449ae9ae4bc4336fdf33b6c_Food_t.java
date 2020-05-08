 package clashsoft.mods.morefood.food;
 
 import static clashsoft.mods.morefood.food.FoodRecipe.CRAFTING;
 import static clashsoft.mods.morefood.food.FoodRecipe.FURNACE;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import clashsoft.clashsoftapi.util.IItemMetadataList;
 import clashsoft.mods.morefood.MoreFoodMod;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.*;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
import net.minecraft.src.ModLoader;
 
 public class Food implements IItemMetadataList
 {
 	public static Food[]		foodTypes		= new Food[1024];
 	public static List<Food>	foodList		= new LinkedList<Food>();
 	
 	public static Food			salad			= new Food(0, "Salad", "salad", 2, MoreFoodMod.saladPlantID);
 	public static Food			cucumber		= new Food(1, "Cucumber", "cucumber", 2, MoreFoodMod.cucumberPlantID);
 	public static Food			rice			= new Food(2, "Rice", "rice", 2, MoreFoodMod.ricePlantID);
 	public static Food			chili			= new Food(3, "Chili", "chili", 2, MoreFoodMod.chiliPlantID);
 	public static Food			tomato			= new Food(4, "Tomato", "tomato", 2, MoreFoodMod.tomatoPlantID);
 	public static Food			paprika			= new Food(5, "Paprika", "paprika", 3, MoreFoodMod.paprikaPlantID);
 	public static Food			onion			= new Food(6, "Onion", "onion", 3, MoreFoodMod.onionPlantID);
 	public static Food			pepperSeeds		= new Food(7, "Pepper Balls", "pepperballs", 0, MoreFoodMod.pepperPlantID);
 	public static Food			vanillaSeeds	= new Food(8, "Vanilla Seeds", "pepperballs", 0, MoreFoodMod.vanillaPlantID);
 	public static Food			corn			= new Food(9, "Corn", "corn", 4, MoreFoodMod.cornPlantID);
 	public static Food			breadslice		= new Food(10, "Bread Slice", "bread_slice", 3, 0, new FoodRecipe(2, new ItemStack(Item.bread)));
 	public static Food			toast			= new Food(11, "Toast", "toast_raw", 5, 0, new FoodRecipe(3, breadslice.asStack()));
 	public static Food			toast_toasted	= new Food(12, "Toastet Toast", "toast_toasted", 2, 0, new FoodRecipe(FURNACE, 1, toast.asStack(), 0.1F));
 	public static Food			butter			= new Food(13, "Butter", "butter", 3, new FoodRecipe(FURNACE, 1, new ItemStack(Item.bucketMilk), 0.2F));
 	public static Food			salami			= new Food(14, "Salami", "salami", 3, new FoodRecipe(4, new ItemStack(Item.beefRaw)));
 	public static Food			cheese			= new Food(15, "Cheese", "cheese", 5, new FoodRecipe(1, butter.asStack(), new ItemStack(Item.bucketMilk), new ItemStack(Item.bucketMilk)));
 	public static Food			cheese_slice	= new Food(16, "Cheese Slice", "cheese_slice", 3, new FoodRecipe(16, cheese.asStack()));
 	public static Food			toast_cheese	= new Food(17, "Toast with Cheese", "toast_cheese", 5, new FoodRecipe(1, toast_toasted.asStack(), cheese_slice.asStack()));
 	public static Food			toast_salami	= new Food(18, "Toast with Salami", "toast_salami", 6, new FoodRecipe(1, toast_toasted.asStack(), salami.asStack()));
 	public static Food			bacon_raw		= new Food(19, "Raw Bacon Slice", "bacon_raw", 2, new FoodRecipe(3, new ItemStack(Item.porkRaw), new ItemStack(Item.porkRaw)));
 	public static Food			bacon			= new Food(20, "Bacon", "bacon_cooked", 6, new FoodRecipe(FURNACE, 1, bacon_raw.asStack(), 0.1F));
 	public static Food			pasta			= new Food(21, "Pasta", "pasta", 2, new FoodRecipe(CRAFTING, 4, new Object[] { "wsw", 'w', Item.wheat, 's', MoreFoodMod.salt }));
 	public static Food			meatball		= new Food(22, "Meat Ball", "ham", 2, new FoodRecipe(CRAFTING, 4, new Object[] { "bb ", "bb ", 'b', Item.beefCooked }));
 	public static Food			hamburger		= new Food(23, "Hamburger", "hamburger", 5, new FoodRecipe(CRAFTING, 1, new Object[] { " b ", "smt", " b ", 'b', breadslice.asStack(), 's', salad.asStack(), 'm', meatball.asStack(), 't', tomato.asStack() }));
 	public static Food			pizza			= new Food(24, "Pizza", "pizza", 6, new FoodRecipe(CRAFTING, 1, new Object[] { " S ", "sts", "www", 'S', salad.asStack(), 's', salami.asStack(), 't', tomato.asStack(), 'w', new ItemStack(Item.wheat) }));
 	public static Food			fried_egg		= new Food(25, "Fried Egg", "fried_egg", 4, new FoodRecipe(FURNACE, 1, new ItemStack(Item.egg), 0.2F));
 	public static Food			omelette		= new Food(26, "Omelette", "omelette", 7, new FoodRecipe(CRAFTING, 1, new Object[] { "eee", 'e', new ItemStack(Item.egg) }));
 	public static Food			popcorn			= new Food(27, "Popcorn", "popcorn", 4, new FoodRecipe(FURNACE, 4, corn.asStack(), 0.25F));
 	public static Food			popcorn_sweet	= new Food(28, "Sweet Popcorn", "popcorn", 5, new FoodRecipe(1, popcorn.asStack(), new ItemStack(Item.sugar)));
 	public static Food			popcorn_salty	= new Food(29, "Salty Popcorn", "popcorn", 5, new FoodRecipe(1, popcorn.asStack(), new ItemStack(MoreFoodMod.salt)));
 	public static Food			candycane		= new Food(30, "Candy cane", "candy", 3, new FoodRecipe(CRAFTING, 2, new Object[] { "s  ", " s ", "  s", 's', Item.sugar }));
 	public static Food			chocolate		= new Food(31, "Chocolate Bar", "chocolatebar", 4, new FoodRecipe(4, new ItemStack(Item.dyePowder, 1, 3), new ItemStack(Item.dyePowder, 1, 3), new ItemStack(Item.bucketMilk), new ItemStack(Item.sugar)));
 	public static Food			chocolateCookie	= new Food(32, "Chocolate Cookie", "chocolatecookie", 2, new FoodRecipe(CRAFTING, 4, new Object[] { "wcw", 'w', Item.wheat, 'c', chocolate.asStack() }));
 	public static Food			cereals1		= new Food(33, "Cereals", "cereals_1", 0, new FoodRecipe(4, new ItemStack(Item.wheat)));
 	public static Food			cereals2		= new Food(34, "Chocolate Cereals", "cereals_2", 0, new FoodRecipe(8, new ItemStack(Item.wheat), new ItemStack(Item.dyePowder, 1, 3)));
 	public static Food			honeydrop		= new Food(35, "Honey Drop", "honeydrop", 1, 0);
 	
 	// Vanilla Food Items
 	
 	public static Food			apple			= new Food((ItemFood) Item.appleRed, 0, null);
 	public static Food			appleStomped	= new Food(36, "Stomped Apple", "apple_stomped", 4, new FoodRecipe(1, new ItemStack(Item.appleRed)));
 	public static Food			appleGold1		= new Food((ItemFood) Item.appleGold, 0, new FoodRecipe(CRAFTING, 1, new Object[] { "ggg", "gag", "ggg", 'g', Item.ingotGold, 'a', Item.appleRed }));
 	public static Food			appleGold2		= new Food((ItemFood) Item.appleGold, 1, new FoodRecipe(CRAFTING, 1, new Object[] { "ggg", "gag", "ggg", 'g', Block.blockGold, 'a', Item.appleRed }));
 	public static Food			appleDiamond	= new Food(37, "Diamond Apple", "apple_diamond", 8, new FoodRecipe(CRAFTING, 1, new Object[] { "ddd", "dxd", "ddd", 'd', Item.diamond, 'x', Item.appleRed })).setEffects(new PotionEffect(Potion.jump.id, 600, 1), new PotionEffect(Potion.damageBoost.id, 1200, 2));
 	
 	public static Food			melon			= new Food((ItemFood) Item.melon, 0, null);
 	public static Food			melonGold1		= new Food(Item.speckledMelon, 0, new FoodRecipe(CRAFTING, 1, new Object[] { "ggg", "gmg", "ggg", 'g', Item.goldNugget, 'm', Item.melon }));
 	
 	public static Food			potato			= new Food((ItemSeedFood) Item.potato, 0, null);
 	public static Food			potatoCooked	= new Food((ItemFood) Item.bakedPotato, 0, new FoodRecipe(FURNACE, 1, new ItemStack(Item.potato), 0.1F));
 	public static Food			potatoStomped	= new Food(38, "Stomped Potato", "potato_stomped", 2, new FoodRecipe(1, new ItemStack(Item.potato)));
 	public static Food			potatoGold1		= new Food(39, "Golden Potato", "potato_gold", 5, new FoodRecipe(CRAFTING, 1, new Object[] { "ggg", "gpg", "ggg", 'g', Item.ingotGold, 'p', Item.potato })).setEffects(new PotionEffect(Potion.field_76444_x.id, 2400, 0));
 	public static Food			potatoGold2		= new Food(40, "Golden Potato", "potato_gold", 7, new FoodRecipe(CRAFTING, 1, new Object[] { "GGG", "GpG", "GGG", 'G', Block.blockGold, 'p', Item.potato })).setEffects(new PotionEffect(Potion.field_76444_x.id, 2400, 0), new PotionEffect(Potion.regeneration.id, 600, 4), new PotionEffect(Potion.fireResistance.id, 6000, 0));
 	public static Food			potatoDiamond	= new Food(41, "Diamond Potato", "potato_diamond", 6, new FoodRecipe(CRAFTING, 1, new Object[] { "ddd", "dxd", "ddd", 'd', Item.diamond, 'x', Item.potato })).setEffects(new PotionEffect(Potion.digSpeed.id, 600, 3), new PotionEffect(Potion.moveSpeed.id, 3000, 1));
 	public static Food			poisonousPotato	= new Food((ItemFood) Item.poisonousPotato, 0, null);
 	
 	public static Food			carrot			= new Food((ItemSeedFood) Item.carrot, 0, null);
 	public static Food			carrotCooked	= new Food(42, "Cooked Carrot", "carrot_cooked", 4, new FoodRecipe(FURNACE, 1, new ItemStack(Item.carrot), 0.1F));
 	public static Food			carrotStomped	= new Food(43, "Stomped Carrot", "carrot_stomped", 2, new FoodRecipe(1, new ItemStack(Item.carrot)));
 	public static Food			carrotGold1		= new Food((ItemFood) Item.goldenCarrot, 0, new FoodRecipe(CRAFTING, 1, new Object[] { "ggg", "gcg", "ggg", 'g', Item.goldNugget, 'c', Item.carrot }));
 	public static Food			carrotDiamond	= new Food(44, "Diamond Carrot", "carrot_diamond", 6, new FoodRecipe(CRAFTING, 1, new Object[] { "ddd", "dxd", "ddd", 'd', Item.diamond, 'x', Item.carrot })).setEffects(new PotionEffect(Potion.invisibility.id, 3000, 0));
 	
 	public static Food			bread			= new Food((ItemFood) Item.bread, 0, new FoodRecipe(CRAFTING, 1, new Object[] { "www", 'w', Item.wheat }));
 	public static Food			cookie			= new Food((ItemFood) Item.cookie, 0, new FoodRecipe(CRAFTING, 1, new Object[] { "wcw", 'w', Item.wheat, 'c', new ItemStack(Item.dyePowder, 1, 3) }));
 	
 	public static Food			porkRaw			= new Food((ItemFood) Item.porkRaw, 0, null);
 	public static Food			porkCooked		= new Food((ItemFood) Item.porkCooked, 0, new FoodRecipe(FURNACE, 1, new ItemStack(Item.porkRaw), 0.1F));
 	public static Food			beefRaw			= new Food((ItemFood) Item.beefRaw, 0, null);
 	public static Food			beefCooked		= new Food((ItemFood) Item.beefCooked, 0, new FoodRecipe(FURNACE, 1, new ItemStack(Item.beefRaw), 0.1F));
 	public static Food			chickenRaw		= new Food((ItemFood) Item.chickenRaw, 0, null);
 	public static Food			chickenCooked	= new Food((ItemFood) Item.chickenCooked, 0, new FoodRecipe(FURNACE, 1, new ItemStack(Item.chickenRaw), 0.1F));
 	public static Food			fishRaw			= new Food((ItemFood) Item.fishRaw, 0, null);
 	public static Food			fishCooked		= new Food((ItemFood) Item.fishCooked, 0, new FoodRecipe(FURNACE, 1, new ItemStack(Item.fishRaw), 0.1F));
 	
 	public static Food			rottenFlesh		= new Food((ItemFood) Item.rottenFlesh, 0, null);
 	public static Food			spiderEye		= new Food((ItemFood) Item.spiderEye, 0, null);
 	
 	public static Food			pumpkinPie		= new Food((ItemFood) Item.pumpkinPie, 0, new FoodRecipe(1, new ItemStack(Item.egg), new ItemStack(Item.sugar), new ItemStack(Block.pumpkin)));
 	
 	static
 	{
 		// Vanilla Food
 		// bread.register();
 		// cookie.register();
 		porkRaw.register();
 		porkCooked.register();
 		beefRaw.register();
 		beefCooked.register();
 		chickenRaw.register();
 		chickenCooked.register();
 		fishRaw.register();
 		fishCooked.register();
 		rottenFlesh.register();
 		spiderEye.register();
 		pumpkinPie.register();
 		apple.register();
 		appleStomped.register();
 		appleGold1.register();
 		appleGold2.register();
 		appleDiamond.register();
 		melon.register();
 		melonGold1.register();
 		potato.register();
 		potatoCooked.register();
 		potatoStomped.register();
 		potatoGold1.register();
 		potatoGold2.register();
 		potatoDiamond.register();
 		poisonousPotato.register();
 		carrot.register();
 		carrotCooked.register();
 		carrotStomped.register();
 		carrotGold1.register();
 		carrotDiamond.register();
 		// Vegetables
 		salad.register();
 		cucumber.register();
 		rice.register();
 		chili.register();
 		tomato.register();
 		paprika.register();
 		onion.register();
 		pepperSeeds.register();
 		vanillaSeeds.register();
 		corn.register();
 		// Bread and Toast
 		bread.register();
 		breadslice.register();
 		toast.register();
 		toast_toasted.register();
 		toast_cheese.register();
 		toast_salami.register();
 		// Other food
 		cheese.register();
 		cheese_slice.register();
 		bacon_raw.register();
 		bacon.register();
 		pasta.register();
 		meatball.register();
 		salami.register();
 		hamburger.register();
 		pizza.register();
 		omelette.register();
 		fried_egg.register();
 		// Sweet stuff
 		popcorn.register();
 		popcorn_sweet.register();
 		popcorn_salty.register();
 		candycane.register();
 		butter.register();
 		chocolate.register();
 		cookie.register();
 		chocolateCookie.register();
 		cereals1.register();
 		cereals2.register();
 		
 		Drink.init();
 	}
 	
 	/**
 	 * The item damage value the Food is represented by
 	 */
 	private int					foodID			= 0;
 	private String				name			= "";
 	private String				icon			= "";
 	private int					foodValue		= 0;
 	private int					blockPlaced		= 0;
 	private boolean				isEnabled		= true;
 	
 	private FoodRecipe			recipe			= null;
 	private PotionEffect[]		effects			= new PotionEffect[0];
 	
 	protected ItemStack			stack;
 	
 	public Food(int id, String name, String icon, int foodValue)
 	{
 		this(id, name, icon, foodValue, null);
 	}
 	
 	public Food(int id, String name, String icon, int foodValue, FoodRecipe recipe)
 	{
 		this(id, name, icon, foodValue, 0, recipe);
 	}
 	
 	public Food(int id, String name, String icon, int foodValue, int blockPlaced)
 	{
 		this(id, name, icon, foodValue, blockPlaced, null);
 	}
 	
 	public Food(int id, String name, String icon, int foodValue, int blockPlaced, FoodRecipe recipe)
 	{
 		this.setID(id);
 		this.setName(name);
 		this.setIconName(icon);
 		this.foodValue = foodValue;
 		this.setBlockPlaced(blockPlaced);
 		this.recipe = recipe;
 		
 		this.stack = new ItemStack(MoreFoodMod.foods, 1, this.getID());
 		
 		if (id >= 0)
 			foodTypes[id] = this;
 	}
 	
 	public Food(Item item, int damage, FoodRecipe recipe)
 	{
		this(-1, item.getUnlocalizedName(), getIconName(item, damage), 0, 0, recipe);
 		this.stack = new ItemStack(item, 1, damage);
 	}
 	
 	public Food(ItemFood foodItem, int damage, FoodRecipe recipe)
 	{
		this(-1, foodItem.getUnlocalizedName(), getIconName(foodItem, damage), foodItem.getHealAmount(), (foodItem instanceof ItemSeedFood ? ((ItemSeedFood) foodItem).getPlantID(null, 0, 0, 0) : 0), recipe);
 		this.stack = new ItemStack(foodItem, 1, damage);
 	}
 	
	private static String getIconName(Item item, int damage)
	{
		String s = "";
		try
		{
			s = item.getIconFromDamage(damage).getIconName();
		}
		catch (NoSuchMethodError err)
		{
			s = ModLoader.<String, Item>getPrivateValue(Item.class, item, "field_111218_cA");
		}
		return s;
	}
	
 	public static Food fromItemStack(ItemStack stack)
 	{
 		return fromDamage(stack.getItemDamage());
 	}
 	
 	public static Food fromDamage(int damage)
 	{
 		return foodTypes[damage];
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#register()
 	 */
 	@Override
 	public void register()
 	{
 		foodList.add(this);
 	}
 	
 	public void addRecipe()
 	{
 		if (recipe != null && getID() >= 0)
 			recipe.addRecipe(MoreFoodMod.foods, this.getID());
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#getAction()
 	 */
 	@Override
 	public EnumAction getAction()
 	{
 		return EnumAction.eat;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#setEnabled(boolean)
 	 */
 	@Override
 	public IItemMetadataList setEnabled(boolean enabled)
 	{
 		this.isEnabled = enabled;
 		return this;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * clashsoft.mods.morefood.food.IItemMetadataList#setEffects(net.minecraft
 	 * .potion.PotionEffect)
 	 */
 	@Override
 	public Food setEffects(PotionEffect... effects)
 	{
 		this.effects = effects;
 		return this;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#getEffects()
 	 */
 	@Override
 	public PotionEffect[] getEffects()
 	{
 		return effects;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#asStack()
 	 */
 	@Override
 	public ItemStack asStack()
 	{
 		return asStack(1);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#asStack(int)
 	 */
 	@Override
 	public ItemStack asStack(int i)
 	{
 		ItemStack is = stack;
 		is.stackSize = i;
 		return is;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#getFoodValue()
 	 */
 	@Override
 	public int getFoodValue()
 	{
 		return foodValue;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#setFoodValue(int)
 	 */
 	@Override
 	public void setFoodValue(int foodValue)
 	{
 		this.foodValue = foodValue;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#getBlockPlaced()
 	 */
 	@Override
 	public int getBlockPlaced()
 	{
 		return blockPlaced;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#setBlockPlaced(int)
 	 */
 	@Override
 	public void setBlockPlaced(int blockPlaced)
 	{
 		this.blockPlaced = blockPlaced;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled()
 	{
 		return isEnabled;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#getName()
 	 */
 	@Override
 	public String getName()
 	{
 		return name;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * clashsoft.mods.morefood.food.IItemMetadataList#setName(java.lang.String)
 	 */
 	@Override
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#getIconName()
 	 */
 	@Override
 	public String getIconName()
 	{
 		return icon;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * clashsoft.mods.morefood.food.IItemMetadataList#setIconName(java.lang.
 	 * String)
 	 */
 	@Override
 	public void setIconName(String icon)
 	{
 		this.icon = icon;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#getID()
 	 */
 	@Override
 	public int getID()
 	{
 		return foodID;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see clashsoft.mods.morefood.food.IItemMetadataList#setID(int)
 	 */
 	@Override
 	public void setID(int foodID)
 	{
 		this.foodID = foodID;
 	}
 	
 	@Override
 	public FoodRecipe getRecipe()
 	{
 		return recipe;
 	}
 	
 	public static List<Food> getDisplayList()
 	{
 		return foodList;
 	}
 	
 	@Override
 	public Collection<String> getDescription()
 	{
 		return Collections.EMPTY_LIST;
 	}
 }
