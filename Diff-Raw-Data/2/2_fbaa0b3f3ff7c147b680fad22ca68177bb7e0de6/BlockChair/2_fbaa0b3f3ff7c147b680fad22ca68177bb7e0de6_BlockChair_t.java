 package modJam;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 public class BlockChair extends Block{
 
 	private Icon[] blockColors = new Icon[16];
 	
 	public ForgeDirection face;
 	public Block belowBlock;
 	public int itemID;
 	
 	public BlockChair(int par1, ForgeDirection face, Block belowBlock, int itemID) {
 		super(par1, Material.wood);
 		this.setBlockBounds(0.25F, 0F, 0.25F, 0.75F, 1.4F, 0.75F);
 		this.face = face;
 		this.belowBlock = belowBlock;
 		this.itemID = itemID;
 	}
 	
 	public boolean testPlacement(World par1World, int par2, int par3, int par4){
 		return par1World.isBlockNormalCube(par2, par3 - 1, par4);
 	}
 	
     /**
      * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
      */
 	@Override
     public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
     {
         return testPlacement(par1World, par2, par3, par4);
     }
 	
     /**
      * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
      */
 	@Override
     public boolean canBlockStay(World par1World, int par2, int par3, int par4)
     {
         return testPlacement(par1World, par2, par3, par4);
     }
 	
 	@Override
 	public Icon getBlockTextureFromSideAndMetadata(int par1, int par2){
 		return blockColors[par2];
 	}
 	
 	@Override
 	public boolean renderAsNormalBlock(){	
 		return false;
 	}
 	
 	@Override
 	public boolean isOpaqueCube(){
 		return false;
 	}
 	
 	@Override
 	public int getRenderType(){
 		return ClientProxyModJam.chairRenderType;
 	}
 	
     /**
      * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
      * their own) Args: x, y, z, neighbor blockID
      */
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
     	if(!testPlacement(par1World, par2, par3, par4)){
     		Block.blocksList[par1World.getBlockId(par2, par3, par4)].dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
     		par1World.setBlockToAir(par2, par3, par4);
     	}
     }
 	
 	@Override
     public int getLightValue(IBlockAccess world, int x, int y, int z)
     {
         if(ClientProxyModJam.chairRenderStage == 0){
             Block block = blocksList[world.getBlockId(x, y, z)];
             if (block != null && block != this)
             {
                 return block.getLightValue(world, x, y, z);
             }
             return lightValue[blockID];
         }else{
         	return 15;
         }
     }
 	
     /**
      * Returns the bounding box of the wired rectangular prism to render.
      */
     public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
     {
         return AxisAlignedBB.getAABBPool().getAABB(0.25F, 0F, 0.25F, 0.75F, 1.0F, 0.75F);
     }
 	
     /**
      * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
      */
 	@Override
     public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         par3List.add(new ItemStack(par1, 1, 0));
         par3List.add(new ItemStack(par1, 1, 1));
         par3List.add(new ItemStack(par1, 1, 2));
         par3List.add(new ItemStack(par1, 1, 3));
         par3List.add(new ItemStack(par1, 1, 4));
         par3List.add(new ItemStack(par1, 1, 5));
         par3List.add(new ItemStack(par1, 1, 6));
         par3List.add(new ItemStack(par1, 1, 7));
         par3List.add(new ItemStack(par1, 1, 8));
         par3List.add(new ItemStack(par1, 1, 9));
         par3List.add(new ItemStack(par1, 1, 10));
         par3List.add(new ItemStack(par1, 1, 11));
         par3List.add(new ItemStack(par1, 1, 12));
         par3List.add(new ItemStack(par1, 1, 13));
         par3List.add(new ItemStack(par1, 1, 14));
         par3List.add(new ItemStack(par1, 1, 15));
     }
 	
 	@Override
 	public int damageDropped(int par1){
 		return par1;
 	}
 	
     /**
      * Returns the ID of the items to drop on destruction.
      */
 	@Override
     public int idDropped(int par1, Random par2Random, int par3)
     {
        return this.itemID;
     }
 	
 	@Override
 	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){
 		return true;
 	}
 	
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister par1IconRegister)
     {
     	this.blockColors[0] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreWhite");
     	this.blockColors[1] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreOrange");
     	this.blockColors[2] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreMagenta");
     	this.blockColors[3] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreLBlue");
     	this.blockColors[4] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreYellow");
     	this.blockColors[5] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreLime");
     	this.blockColors[6] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOrePink");
     	this.blockColors[7] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreGray");
     	this.blockColors[8] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreLGray");
     	this.blockColors[9] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreCyan");
     	this.blockColors[10] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOrePurple");
     	this.blockColors[11] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreBlue");
     	this.blockColors[12] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreBrown");
     	this.blockColors[13] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreGreen");
     	this.blockColors[14] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreRed");
     	this.blockColors[15] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeOreBlack");
     }
 }
