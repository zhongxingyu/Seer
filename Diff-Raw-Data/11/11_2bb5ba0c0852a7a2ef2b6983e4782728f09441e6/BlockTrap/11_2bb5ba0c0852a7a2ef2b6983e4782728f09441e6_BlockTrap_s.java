 package assets.fyresmodjam;
 
 import static net.minecraftforge.common.ForgeDirection.SOUTH;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockTrap extends BlockContainer
 {
     protected BlockTrap(int par1) {
         super(par1, Material.circuits);
         this.setLightOpacity(0);
         this.setCreativeTab(CreativeTabs.tabBlock);
         //this.setTickRandomly(true);
         //this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
     }
 
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) {
         this.blockIcon = par1IconRegister.registerIcon("fyresmodjam:spikes");
     }
 
     @SideOnly(Side.CLIENT)
     public String getItemIconName() {
         return "fyresmodjam:spikes"; 
     }
 
     public int idDropped(int par1, Random par2Random, int par3) {
         return 0;
     }
 
     public int idPicked(World par1World, int par2, int par3, int par4) {
         return ModjamMod.blockTrap.blockID;
     }
 
     public boolean canHarvestBlock(EntityPlayer par1EntityPlayer, int par2) {
         return false;
     }
     
     public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)  {	
     	if(par1World.isRemote && isCollidable()) {
     		PacketDispatcher.sendPacketToServer(PacketHandler.newPacket(PacketHandler.DISARM_TRAP, new Object[] {par2, par3, par4, par5EntityPlayer.getEntityData().hasKey("Blessing") && par5EntityPlayer.getEntityData().getString("Blessing").equals("Mechanic")}));
 	    	return false;
     	}
     	
     	return true;
     }
 
     @SideOnly(Side.CLIENT)
     public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
         return false;
     }
 
     public boolean isOpaqueCube() {
         return false;
     }
 
     public boolean hasTileEntity(int metadata) {
         return true;
     }
 
     @Override
     public TileEntity createNewTileEntity(World world) {
         return new TileEntityTrap();
     }
 
     public int getMobilityFlag() {
         return 2;
     }
     
     public void addCollisionBoxesToList(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity) {}
     
     public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
         return null;
     }
     
     public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity) {
     	if(!par1World.isRemote && ((par5Entity instanceof EntityPlayer && !((EntityPlayer) par5Entity).capabilities.isCreativeMode) || par5Entity instanceof EntityMob)) {
     		par5Entity.attackEntityFrom(DamageSource.cactus, 1.0F);
     		if(ModjamMod.r.nextInt(100) == 0) {((EntityLivingBase) par5Entity).addPotionEffect(new PotionEffect(Potion.poison.id, 100, 1));}
     	}
     }
     
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
     	super.onNeighborBlockChange(par1World, par2, par3, par4, par5);
     	
     	if (!par1World.isBlockSolidOnSide(par2, par3 -1, par4, ForgeDirection.SOUTH, true)) {
     		par1World.setBlockToAir(par2, par3, par4);
     	}
     }
     
     public boolean isCollidable() {
     	boolean b = false;
     	if(Side.CLIENT.isClient()) {b = getPlayerSneaking();}
         return b;
     }
     
     @Override
     public boolean canPlaceBlockAt(World world, int x, int y, int z) {
     	return super.canPlaceBlockAt(world, x, y, z) && (y == 0 || world.getBlockId(x, y - 1, z) != ModjamMod.blockTrap.blockID);
     }
     
     @SideOnly(Side.CLIENT)
     public boolean getPlayerSneaking() {
    	boolean b2 = false;
     	
     	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
     	
    	if(player != null && Minecraft.getMinecraft().objectMouseOver != null) {
     		MovingObjectPosition mouse = Minecraft.getMinecraft().objectMouseOver;
     		
     		double xDiff = (double) mouse.blockX + 0.5D - TileEntityRenderer.instance.playerX;
 			double yDiff = (double) mouse.blockY + 0.5D - TileEntityRenderer.instance.playerY;
 			double zDiff = (double) mouse.blockZ + 0.5D - TileEntityRenderer.instance.playerZ;
 			
 			b2 = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff < ((player.getEntityData().hasKey("Blessing") && player.getEntityData().getString("Blessing").equals("Scout")) ? 16.0F : 36.0F);
		}
     	
    	return b2 && (player == null ? false : (player.isSneaking() || (player.getEntityData().hasKey("Blessing") && player.getEntityData().getString("Blessing").equals("Scout"))));
     }
 }
