 package TFC.Items;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import TFC.Reference;
 import TFC.TerraFirmaCraft;
 import TFC.Core.TFC_Core;
 import TFC.Core.Player.PlayerInfo;
 import TFC.Core.Player.PlayerManagerTFC;
 import TFC.Core.Util.StringUtil;
 
 public class ItemClay extends ItemLooseRock
 {
 	
 	public ItemClay(int id) 
 	{
 		super(id);
 		this.setCreativeTab(CreativeTabs.tabMaterials);
 		this.icons = new Icon[2];
 	}
 
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemstack, World par2World, EntityPlayer entityplayer)
 	{
 		PlayerInfo pi = PlayerManagerTFC.getInstance().getPlayerInfoFromPlayer(entityplayer);
 		pi.specialCraftingType = new ItemStack(specialCraftingType, 1, itemstack.getItemDamage());
 		if(specialCraftingTypeAlternate != null)
 			pi.specialCraftingTypeAlternate = specialCraftingTypeAlternate;
 		itemstack.stackSize--;
 		entityplayer.openGui(TerraFirmaCraft.instance, 28, entityplayer.worldObj, (int)entityplayer.posX, (int)entityplayer.posY, (int)entityplayer.posZ);
 		return itemstack;
 
 	}
 	
 	@Override
 	public void addExtraInformation(ItemStack is, EntityPlayer player, List arraylist)
     {		
 		if (TFC_Core.showExtraInformation()) 
 		{
 			arraylist.add(StringUtil.localize("gui.Help"));
 			arraylist.add(StringUtil.localize("gui.Clay.Inst0"));
 		}
 		else
 		{
 			arraylist.add(StringUtil.localize("gui.ShowHelp"));
 		}
     }
 
 	@Override
 	public Icon getIconFromDamage(int meta)
 	{        
 		return icons[meta];
 	}	
 	
 	@Override
 	public void registerIcons(IconRegister registerer)
     {
 		icons[0] = registerer.registerIcon(Reference.ModID + ":" + "Clay");
 		icons[1] = registerer.registerIcon(Reference.ModID + ":" + "Fire Clay");
     }
 }
