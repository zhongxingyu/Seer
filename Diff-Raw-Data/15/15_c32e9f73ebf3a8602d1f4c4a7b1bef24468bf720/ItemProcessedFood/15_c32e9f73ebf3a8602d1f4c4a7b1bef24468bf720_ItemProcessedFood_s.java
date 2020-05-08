 package net.lomeli.magiks.items;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.lomeli.magiks.lib.Strings;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class ItemProcessedFood extends ItemFood
 {
 	public Icon[] iconArray = new Icon[11];
 
 	public ItemProcessedFood(int par1)
     {
 	    super(par1, 32, false);
 	    this.setHasSubtypes(true);
 	    this.setMaxDamage(0);
 	    this.setCreativeTab(CreativeTabs.tabFood);
     }
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister)
 	{
 		for(int i = 0; i < 11; i++)
 		{
 			this.iconArray[i] = iconRegister.registerIcon(Strings.MOD_ID + ":" + "food/processed_" + i);
 		}
 	}
 	
 	@Override
 	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
     {
         --par1ItemStack.stackSize;
         int hungerPoints = 0;
         float saturation = 0;
         switch(par1ItemStack.getItemDamage())
         {
         	case 0:
         		hungerPoints = 5; saturation = 13.5F; break;
         	case 1:
         		hungerPoints = 5; saturation = 13.5F; break;
         	case 2:
         		hungerPoints = 6; saturation = 9F; break;
         	case 3:
         		hungerPoints = 4; saturation = 8F; break;
         	case 4:
         		hungerPoints = 5; saturation = 6F; break;
         	case 5:
         		hungerPoints = 3; saturation = 3F; break;
         }
         par3EntityPlayer.getFoodStats().addStats(hungerPoints, saturation);
         par2World.playSoundAtEntity(par3EntityPlayer, "random.burp", 0.5F, par2World.rand.nextFloat() * 0.1F + 0.9F);
         this.onFoodEaten(par1ItemStack, par2World, par3EntityPlayer);
         return par1ItemStack;
     }
 	
 	public int getMetadata(int par1)
     {
         return par1;
     }
 	
 	@Override
 	@SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(int i)
 	{
 		return this.iconArray[i];
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@SideOnly(Side.CLIENT)
     public void getSubItems(int itemID, CreativeTabs tabs, List list)
 	{
 		for(int i = 0; i < 11; i++)
 		{
 			list.add(new ItemStack(itemID, 1, i));
 		}
 	}
 	
 	public String getUnlocalizedName(ItemStack par1ItemStack)
     {
         return super.getUnlocalizedName() + "." + par1ItemStack.getItemDamage();
     }
 }
