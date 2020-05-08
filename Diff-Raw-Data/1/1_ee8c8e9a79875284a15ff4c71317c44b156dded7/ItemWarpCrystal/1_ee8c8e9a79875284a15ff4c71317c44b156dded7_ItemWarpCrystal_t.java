 package shadowhax.modjam.item;
 
 import java.util.List;
 
 import shadowhax.modjam.ModJam;
 import shadowhax.modjam.block.Blocks;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ItemWarpCrystal extends Item {
 
 	public ItemWarpCrystal(int par1) {
 		
 		super(par1);
 		this.maxStackSize = 1;
 		this.setCreativeTab(ModJam.tab);
 	}
 	
 	public static int warpX;
 	public static int warpY;
 	public static int warpZ;
 	
 	public static int block;
 	
     public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
         if (world.getBlockId(x, y, z) == Blocks.warpBlock.blockID) {
         	stack.stackTagCompound.setInteger("linkX", x);
         	stack.stackTagCompound.setInteger("linkY", y);
         	stack.stackTagCompound.setInteger("linkZ", z);
         	
         	warpX = stack.stackTagCompound.getInteger("linkX");
         	warpY = stack.stackTagCompound.getInteger("linkY");
         	warpZ = stack.stackTagCompound.getInteger("linkZ");
         }
         
         if (world.getBlockId(x, y, z) != Blocks.warpBlock.blockID && stack.stackTagCompound.hasKey("linkX")){
         	if (world.getBlockId(warpY, warpY, warpZ) != 0 && world.getBlockId(warpY, warpY, warpZ) == Blocks.warpBlock.blockID ){
         		
         		player.posX = warpX;
         		player.posY = warpY;
         		player.posZ = warpZ;
         		player.setPositionAndUpdate(warpX, warpY, warpZ);
         		--stack.stackSize;
         	}	
         }  
 		return true;    
     }	
     
     public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
     	
     	list.add("X"+ warpX + " Y"+ warpY + " Z" + warpZ);
     }
 }
