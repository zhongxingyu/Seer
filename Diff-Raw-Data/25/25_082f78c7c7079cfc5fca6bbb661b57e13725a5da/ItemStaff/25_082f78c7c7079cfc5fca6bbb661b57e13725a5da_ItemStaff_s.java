 package mods.themike.modjam.items;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import mods.themike.modjam.ModJam;
 import mods.themike.modjam.rune.IRune;
 import mods.themike.modjam.rune.RuneRegistry;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class ItemStaff extends ItemMulti {
 
 	
 	protected static String[] subNames = new String[]{"apprentice", "mage"};
 	private static Icon[] subIcons = new Icon[subNames.length];
 	
 	public ItemStaff(int par1) {
 		super(par1);
 		this.hasSubtypes = true;
 		this.setUnlocalizedName("itemStaff");
 		this.setCreativeTab(ModJam.tab);
 	}
 	
 	@Override
 	public Icon getIconFromDamage(int metadata) {
 		return subIcons[metadata];
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void updateIcons(IconRegister reg) {
 		for(int par1 = 0; par1 < subNames.length; par1++) {
 			subIcons[par1] = reg.registerIcon("mikejam:staff" + subNames[par1]);
 		}
 	}
 	
 	@Override
 	public void getSubItems(int ID, CreativeTabs tabs, List list) {
 		for(int par1 = 0; par1 < subNames.length; par1++) {
 			ItemStack stack = new ItemStack(ID, 1, par1);
 			stack.setTagCompound(new NBTTagCompound());
 			list.add(stack);
 		}
 	}
 	
 	@Override
 	public String getUnlocalizedName(ItemStack stack) {
 		if(stack.getItemDamage() >= 0 && stack.getItemDamage() < subNames.length) {
 			return "staff." + subNames[stack.getItemDamage()];
 		}
 		return null;
 	}
 	
 	@Override 
 	public boolean hasEffect(ItemStack stack) {
 		if(stack.getItemDamage() == 1) {
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
 		if(stack.getItemDamage() == 0) {
 			list.add("Can bind up to Level 25.");
 		} else {
 			list.add("Can bind all levels.");
 		}
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
 		super.onItemRightClick(stack, world, player);
 		ItemStack newStack = stack.copy();
 
 		
 		if(!world.isRemote && !player.isSneaking() && stack.getTagCompound() != null) {
 			ItemStack runeStack = ItemStack.loadItemStackFromNBT((NBTTagCompound) stack.getTagCompound().getTag("item"));
 			if(runeStack != null) {
 				System.out.println("Hello Runes!");
 				IRune rune = RuneRegistry.getrunes()[runeStack.getItemDamage()];
 				rune.onUse(player);
 				int uses = runeStack.getTagCompound().getInteger("uses");
 				if(uses == 1) {
					stack.setTagCompound(null);
 				} else {
 					runeStack.getTagCompound().setInteger("uses", uses - 1);
 				}
 			}
 		}
 		if(player.isSneaking()) {
 			player.openGui(ModJam.instance, 1, world, (int) player.posX, (int) player.posY, (int) player.posZ);
 		}
 		return newStack;
 	}
 
 }
