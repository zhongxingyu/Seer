 package pdunham.weird.weapons;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBow;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import net.minecraftforge.common.MinecraftForge;
 import pdunham.weird.common.StandardLogger;
 import pdunham.weird.common.WeirdConstants;
 import pdunham.weird.common.WeirdMain;
 import pdunham.weird.entity.EntityPebble;
 
 public class WeirdSlingShot extends Item {
 
 	private static StandardLogger logger;
 
  	// Standard c'tor
 	public WeirdSlingShot(int id) {
         super(id);
 
         // Limit the stack size to a weird number
         setMaxStackSize(2);
         
         // Put on the materials tab
         setCreativeTab(CreativeTabs.tabCombat);
         
         // Set the internal name
         setItemName("weirdSlingShot");
         
         // Set the texture.
         setIconCoord(9, 0);
         
         logger = StandardLogger.getLogger(logger, this.getClass().getSimpleName());
         logger.info("c'tor() complete id: " + id);
 	}
 
 	public void postInit() {
 		// Set the graphics texture from a .png file
 		setTextureFile(getTextureFile());
 
 		// Register the block w/ MineCraft
 		GameRegistry.registerItem(this, "weirdSlingShot");
 
 		// Set the external name
 		LanguageRegistry.addName(this, "Weird sling shot");
 
 		// A weird slingShot is 3 weird ingots and 2 iron ingots
 		GameRegistry.addRecipe(new ItemStack(WeirdMain.weirdSlingShot), "wsw", "k k", " i ",
 							'w', new ItemStack(WeirdMain.weirdIngot),
 							'i', new ItemStack(Item.ingotIron),
 							's', new ItemStack(Item.silk),
 							'k', new ItemStack(Item.stick));
 		
 		logger.info("postInit() complete newId: " + itemID);
 	}
 	
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
 		
		if (par3EntityPlayer.capabilities.isCreativeMode || 
			par3EntityPlayer.inventory.hasItem(WeirdMain.pebble.itemID)) {
			par3EntityPlayer.inventory.consumeInventoryItem(WeirdMain.pebble.itemID);
		} else {
			return par1ItemStack;
		}
 		
 		// Play a sound
 		par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
 		
 		// Create the pebble in the client.
 		if (!par2World.isRemote) {
 			par2World.spawnEntityInWorld(new EntityPebble(par2World, par3EntityPlayer));
 		}
 		return par1ItemStack;
 	}
 	
 	@Override
 	public String getTextureFile(){
 		return WeirdConstants.pathTexturesIcons;
 	}	
 }
