 package tektor.minecraft.chalith.items;
 
 import java.util.List;
 
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import tektor.minecraft.chalith.ChalithBase;
 import tektor.minecraft.chalith.util.ItemStackFactory;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagDouble;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class UtilRune extends Item {
 
 	private int timer;
	@SideOnly(Side.CLIENT)
 	private Icon[] icon = new Icon[3];
 	
 
 	public UtilRune(int id) {
 		super(id);
 		setMaxStackSize(1);
 		setCreativeTab(CreativeTabs.tabTools);
 		setUnlocalizedName("recallRune");
 		timer = 0;
 	}
 	
 	@Override
 	public Icon getIcon(ItemStack stack,
 	           int pass)
 	{
 		int id = stack.getItemDamage();
 		return icon[id];
 	}
 	@Override
 	public Icon getIconFromDamageForRenderPass(int par1,
             int par2)
 	{
 		return icon[par1];
 	}
 	
 	@Override
 	public Icon getIconFromDamage(int par1) 
 	{
 		return icon[par1];
 	}
 	
 	@Override
 	public String getUnlocalizedName(ItemStack stack)
 	{
 		switch(stack.getItemDamage())
 		{
 		case 0: return "recallRune";
 		case 1:
 		case 2: return "inventoryStatusRune";
 		default: return "??";
 		}
 		
 	}
 	
 	@Override
  	public String getItemDisplayName(ItemStack stack)
  	{
  		switch(stack.getItemDamage())
  		{
  		case 0: return "Recall Rune";
 		case 1:
 		case 2: return "Rune of Inventory";
 		default: return "??";
  		}
  	}
 	
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		icon[0] = par1IconRegister.registerIcon("chalith:recallRune");
 		icon[1] = par1IconRegister.registerIcon("chalith:inventoryRune");
 		icon[2] = par1IconRegister.registerIcon("chalith:inventoryRuneAct");
 	}
 
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World,
 			EntityPlayer par3EntityPlayer) {
 		int id = par1ItemStack.getItemDamage();
 		if (0 == id) {
 			if (timer == 0) {
 				if (par1ItemStack.stackTagCompound.getBoolean("active")) {
 					if (par3EntityPlayer.inventory
 							.consumeInventoryItem(ChalithBase.avaeaIngot.itemID)) {
 						par3EntityPlayer.setPositionAndUpdate(
 								par1ItemStack.stackTagCompound.getDouble("x"),
 								par1ItemStack.stackTagCompound.getDouble("y"),
 								par1ItemStack.stackTagCompound.getDouble("z"));
 
 					} else {
 						par3EntityPlayer
 								.addChatMessage("Not enough material to use this.");
 					}
 					timer = 100;
 
 				} else {
 					par1ItemStack.stackTagCompound = new NBTTagCompound();
 					par1ItemStack.stackTagCompound.setDouble("x",
 							par3EntityPlayer.posX);
 					par1ItemStack.stackTagCompound.setDouble("y",
 							par3EntityPlayer.posY);
 					par1ItemStack.stackTagCompound.setDouble("z",
 							par3EntityPlayer.posZ);
 					par1ItemStack.stackTagCompound.setBoolean("active", true);
 				}
 			}
 		}
 		return par1ItemStack;
 
 	}
 
 	public void onUpdate(ItemStack par1ItemStack, World par2World,
 			Entity par3Entity, int par4, boolean par5) {
 		int id = par1ItemStack.getItemDamage();
 		if (id == 0) {
 			if (timer > 0) {
 				timer--;
 			}
 		}
 		else if(id == 1 || id == 2)
 		{
 			if(par3Entity instanceof EntityPlayer)
 			{
 				int firstFree = ((EntityPlayer) par3Entity).inventory.getFirstEmptyStack();
 				if(firstFree == -1)
 				{
 					par1ItemStack.setItemDamage(2);
 				}
 				else
 				{
 					par1ItemStack.setItemDamage(1);
 				}
 			}
 		}
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void getSubItems(int par1, CreativeTabs tab, List subItems) {
 		
 			subItems.add(new ItemStack(this, 1, 0));
 			subItems.add(new ItemStack(this, 1, 1));
 	}
 
 }
