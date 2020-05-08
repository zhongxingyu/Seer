 package ru.secondfry.TestMod.items;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import ru.secondfry.TestMod.ModInformation;
 import ru.secondfry.TestMod.blocks.BlockInfo;
import ru.secondfry.TestMod.tileentities.TileEntityFirework;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Rustam Second_Fry Gubaydullin
  * Date: 21.09.13 6:39
  */
 public class ItemDye extends Item {
 
 	public ItemDye(int itemID) {
 		super(itemID);
 		setMaxStackSize(ItemInfo.DYE_MAXSTACK);
		setCreativeTab(CreativeTabs.tabRedstone);
		setHasSubtypes(true);
 	}
 
 	@SideOnly(Side.CLIENT)
 	private Icon[] icons;
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register) {
 		int i = 0, size = ItemInfo.DYE_ICONS.length;
 
 		icons = new Icon[size];
 
 		while (i < size) {
 			icons[i] = register.registerIcon(ModInformation.ID + ":" + ItemInfo.DYE_ICONS[i]);
 			i++;
 		}
 	}
 
 	@Override
 	public Icon getIconFromDamage(int dmg) {
 		return icons[dmg];
 	}
 
 	@Override
	public int getMetadata(int dmg) {
		return dmg;
	}

	@Override
 	public void getSubItems(int itemID, CreativeTabs creativeTab, List list) {
 		int i = 0;
 
 		while (i < ItemInfo.DYE_NAMES.length) {
 			ItemStack itemStack = new ItemStack(itemID, 1, i);
 			list.add(itemStack);
 			i++;
 		}
 	}
 
 	@Override
 	public String getUnlocalizedName(ItemStack itemStack) {
 		return ItemInfo.DYE_UNLOCALIZED_NAME + itemStack.getItemDamage();
 	}
 
 	@Override
 	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
 		if (!world.isRemote && world.getBlockId(x, y, z) == BlockInfo.FIREWORK_ID) {
 			world.setBlockMetadataWithNotify(x, y, z, stack.getItemDamage(), 3); // Magic "3"
			((TileEntityFirework) world.getBlockTileEntity(x, y, z)).setType(stack.getItemDamage());
 			stack.stackSize--;
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
