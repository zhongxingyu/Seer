 package steamcraft.items;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import steamcraft.EntityMusketBall;
 import steamcraft.Steamcraft;
 
 public class ItemFirearm extends Item
 {
     private Icon[] icons;
 	public ItemFirearm(int i)
     {
         super(i);
 		setMaxStackSize(1);
 		setFull3D();
 		setHasSubtypes(true);
 		setCreativeTab(Steamcraft.steamTab);
     }
     @Override
     public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
     {
 		if(itemstack.getItemDamage() == 0 && !entityplayer.isInsideOfMaterial(Material.water)){
 			spawnBullet(itemstack, world, entityplayer);
 		}
 		return itemstack;
 	}
 	
 	public void spawnBullet(ItemStack itemstack, World world, EntityPlayer entityplayer){
 		world.playSoundAtEntity(entityplayer, "mob.ghast.fireball", 0.8F, 1.0F / (itemRand.nextFloat() * 0.4F + 0.8F));
 		world.playSoundAtEntity(entityplayer, "random.explode", 0.4F, 1.0F / (itemRand.nextFloat() * 0.4F + 0.9F));
 		itemstack.setItemDamage(itemstack.getMaxDamage()-1);
         if(!world.isRemote)
         {
             world.spawnEntityInWorld(new EntityMusketBall(world, entityplayer, getFirePower(itemstack), isRifled(itemstack)));
         }
 	}
 	@Override
 	public int getMaxDamage(ItemStack stack){
 		if(stack.hasTagCompound()){
 			return stack.getTagCompound().getInteger("maxdmg");
 		}
         return getMaxDamage();
     }
 	public static void setMaxDamage(ItemStack stack, int max){
 		if(!stack.hasTagCompound())
 			stack.setTagCompound(new NBTTagCompound());
 		stack.getTagCompound().setInteger("maxdmg",max);
 	}
 	@Override
 	public String getUnlocalizedName(ItemStack stack){
 		return getUnlocalizedName()+getMaxDamage(stack)+getFirePower(stack)+isRifled(stack);
 	}
 	@Override
 	@SideOnly(Side.CLIENT)
     public boolean requiresMultipleRenderPasses()
     {
         return true;
     }
 	@Override
 	public int getRenderPasses(int metadata)
     {
         return 1;
     }
 	@Override
 	public Icon getIcon(ItemStack stack, int pass){
 		if(isRifled(stack))
 		{
 			if(getFirePower(stack)>10)
 				return icons[0];
 			else if(getFirePower(stack)>8)
 				return icons[1];
 			else
 				return icons[2];
 		}
 		else
 		{
 			if(getFirePower(stack)>8)
 				return icons[3];
 			else if(getFirePower(stack)>6)
 				return icons[4];
 			else
 				return icons[5];
 		}
 	}
 	@Override
 	@SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister)
     {
         icons = new Icon[6];
        icons[0] = par1IconRegister.registerIcon(getIconString()+"percussionRifle");
        icons[1] = par1IconRegister.registerIcon(getIconString()+"flintlockRifle");
        icons[2] = par1IconRegister.registerIcon(getIconString()+"matchlockRifle");
        icons[3] = par1IconRegister.registerIcon(getIconString()+"percussionMusket");
        icons[4] = par1IconRegister.registerIcon(getIconString()+"flintlockMusket");
        icons[5] = par1IconRegister.registerIcon(getIconString()+"matchlockMusket");
     }
 	@Override
 	@SideOnly(Side.CLIENT)
     public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         par3List.add(Steamcraft.flintlockMusket);
         par3List.add(Steamcraft.matchlockMusket);
         par3List.add(Steamcraft.percussionCapMusket);
         par3List.add(Steamcraft.flintlockRifle);
         par3List.add(Steamcraft.matchlockRifle);
         par3List.add(Steamcraft.percussionCapRifle);
     }
 	public static int getFirePower(ItemStack itemstack){
 		return itemstack.getTagCompound().getInteger("power");
 	}
 	
 	public static void setFirePower(ItemStack stack, int power){
 		if(!stack.hasTagCompound())
 			stack.setTagCompound(new NBTTagCompound());
 		stack.getTagCompound().setInteger("power",power);
 	}
 	
 	public ItemStack getAmmoA(ItemStack stack){
 		return new ItemStack(Steamcraft.part,1,hasPercussion(stack)?1:0);
 	}
 	
 	public ItemStack getAmmoB(){
 		return new ItemStack(Steamcraft.part);
 	}
 	
 	public static boolean isRifled(ItemStack stack){
 		return stack.getTagCompound().getBoolean("rifled");
 	}
 	public static void setRifled(ItemStack stack){
 		if(!stack.hasTagCompound())
 			stack.setTagCompound(new NBTTagCompound());
 		stack.getTagCompound().setBoolean("rifled", true);
 	}
 	public static boolean hasPercussion(ItemStack stack){
 		return stack.getTagCompound().getBoolean("percussion");
 	}
 	public static void setPercussion(ItemStack stack){
 		if(!stack.hasTagCompound())
 			stack.setTagCompound(new NBTTagCompound());
 		stack.getTagCompound().setBoolean("percussion", true);
 	}
 }
