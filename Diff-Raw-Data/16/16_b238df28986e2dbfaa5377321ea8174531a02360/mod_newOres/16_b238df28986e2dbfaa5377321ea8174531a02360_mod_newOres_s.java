 package net.minecraft.src;
 
 import java.util.Random;
 
 public class mod_newOres extends BaseMod {
 	public static final Item Sapphire = new ItemSapphire(159).setItemName("Sapphire");
 	public static final Item RawZircon = new ItemRawZircon(160).setItemName("RawZircon");
 	public static final Item Zircon = new ItemZircon(161).setItemName("Zircon");
 	public static final Item Ruby = new ItemRuby(162).setItemName("Ruby");
 	public static final Item BrokenHeart = new ItemBrokenHeart(163).setItemName("Broken Heart");
	public static final Item SapphireHelmet = (new ItemArmor(164, 3, 5, 0)).setItemName("Sapphire Helmet");
	public static final Item SapphireBody = (new ItemArmor(165, 3, 5, 0)).setItemName("Sapphire Chestplate");
	public static final Item SapphireLegs = (new ItemArmor(166, 3, 5, 0)).setItemName("Sapphire Leggings");
	public static final Item SapphireBoots = (new ItemArmor(167, 3, 5, 0)).setItemName("Sapphire Boots");
 
 
 
 
 	public String getVersion() {
 		return "1.0";
 	}
 
 	@Override
 	public void load() {
 
 		Sapphire.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/Sapphire.png");
 		RawZircon.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/RawZircon.png");
 		Zircon.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/Zircon.png");
 		Ruby.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/Ruby.png");
 		BrokenHeart.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/BrokenHeart.png");
 		SapphireHelmet.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/SapphireHelmet.png");
 		SapphireBody.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/SapphireBody.png");
 		SapphireLegs.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/SapphireLegs.png");
 		SapphireBoots.iconIndex = ModLoader.addOverride("/gui/items.png", "/Textures/SapphireBoots.png");
 		
 
 		ModLoader.addName(Sapphire, "Sapphire");
 		ModLoader.addName(Ruby, "Ruby");
 		ModLoader.addName(RawZircon, "Raw Zircon");
 		ModLoader.addName(Zircon, "Zircon");
 		ModLoader.addName(BrokenHeart, "Broken Heart");
 		
 		ModLoader.addSmelting(mod_newOres.RawZircon.shiftedIndex, new ItemStack(mod_newOres.Zircon, 1), 0);
 		ModLoader.addSmelting(Item.diamond.shiftedIndex, new ItemStack(mod_newOres.Ruby, 1), 0);
 		
 		ModLoader.addRecipe(new ItemStack(SapphireHelmet, 1), new Object []
 				{
 			"xxx", "x x", Character.valueOf('x'), mod_newOres.Sapphire
 				});
 		ModLoader.addRecipe(new ItemStack(SapphireBody, 1), new Object []
 				{
 			"x x", "xxx", "xxx", Character.valueOf('x'), mod_newOres.Sapphire
 				});
 		ModLoader.addRecipe(new ItemStack(SapphireLegs, 1), new Object []
 				{
 			"xxx", "x x", "x x", Character.valueOf('x'), mod_newOres.Sapphire
 				});
 		ModLoader.addRecipe(new ItemStack(SapphireBoots, 1), new Object []
 				{
 			"", "x x", "x x", Character.valueOf('x'), mod_newOres.Sapphire
 				});
 		
		ModLoader.addArmor("SA");
 		
		ModLoader.addName(SapphireHelmet, "Sapphire Helmet");
		ModLoader.addName(SapphireBody, "Sapphire Chestplate");
		ModLoader.addName(SapphireLegs, "Sapphire Pants");
		ModLoader.addName(SapphireBoots, "Sapphire Boots");
 		
 		
 	}
 }
