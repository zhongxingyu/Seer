 package loecraftpack.blocks;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.blocks.te.ColoredBedTileEntity;
 import loecraftpack.enums.Dye;
 import loecraftpack.logic.handlers.ColoredBedHandler;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockBed;
 import net.minecraft.block.ITileEntityProvider;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ColoredBedBlock extends BlockBed implements ITileEntityProvider {
 
 	@SideOnly(Side.CLIENT)
     public List<Icon[]> bedend = new ArrayList<Icon[]>();
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedside = new ArrayList<Icon[]>();
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedtop = new ArrayList<Icon[]>();
     
     public int renderID = 14;
     private int bedDropID = -1;//default is the null instance
     
 	public ColoredBedBlock(int par1) {
 		super(par1);
 	}
 	
 	/**
      * The type of render function that is called for this block
      */
 	@Override
     public int getRenderType()
     {
         return renderID;
     }
 	
 	@SideOnly(Side.CLIENT)
 
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister par1IconRegister)
     {
 		this.bedtop.clear();
 		this.bedend.clear();
 		this.bedside.clear();
 		
 		for(String name : ColoredBedHandler.iconNames)
 		{
 			this.bedtop.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:bed_"+name+"_feet_top"), par1IconRegister.registerIcon("loecraftpack:bed_"+name+"_head_top")} );
 			this.bedend.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:bed_"+name+"_feet_end"), par1IconRegister.registerIcon("loecraftpack:bed_"+name+"_head_end")} );
 			this.bedside.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:bed_"+name+"_feet_side"), par1IconRegister.registerIcon("loecraftpack:bed_"+name+"_head_side")} );
 		}
     }
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getBlockTextureFromSideAndMetadata(int par1, int par2)
     {
 		return Block.planks.getBlockTextureFromSide(par1);
     }
 	
 	/* bug fix : dropping only white beds */
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int blockID, int meta)
     {
 		System.out.println("destroy tile");
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		if(tile != null && tile instanceof ColoredBedTileEntity)
 		{
 			bedDropID = ((ColoredBedTileEntity)tile).id;
 		}
 		else
 			bedDropID = -1;
 		world.removeBlockTileEntity(x, y, z);
		System.out.println("BREAK BED!");
 		ColoredBedTileEntity.finishTileRemoval(world, x, y, z, meta);
 		
     }
 	
 	@Override
 	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         
         int damageValue=0;
 
         TileEntity tile = world.getBlockTileEntity(x, y, z);
         
         System.out.println("x:"+ x + " z:"+ z +" exist:"+ (world.getBlockTileEntity(x, y, z)!=null) );
         
         if (tile != null && tile instanceof ColoredBedTileEntity)
         {
         	damageValue = ((ColoredBedTileEntity)tile).id;
         }
         else
         	damageValue = bedDropID;
         
         if (damageValue == -1)//null instance
         	ret.add(new ItemStack(Block.bed.blockID, 1, 0));
         else
         	ret.add(new ItemStack(idDropped(metadata, world.rand, fortune), 1, damageValue));
         return ret;
     }
 	
 	@Override
 	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int scourceID)
     {
         int i1 = par1World.getBlockMetadata(par2, par3, par4);
         int j1 = getDirection(i1);
         boolean flag = false;
         
         if (isBlockHeadOfBed(i1))
         {
             if (par1World.getBlockId(par2 - footBlockToHeadBlockMap[j1][0], par3, par4 - footBlockToHeadBlockMap[j1][1]) != this.blockID)
             {
                 par1World.setBlockToAir(par2, par3, par4);
                 flag=true;
             }
         }
         else if (par1World.getBlockId(par2 + footBlockToHeadBlockMap[j1][0], par3, par4 + footBlockToHeadBlockMap[j1][1]) != this.blockID)
         {
             par1World.setBlockToAir(par2, par3, par4);
             flag=true;
             
             if (!par1World.isRemote)
             {
                 this.dropBlockAsItem(par1World, par2, par3, par4, i1, 0);
             }
         }
         /*
         if(!flag && scourceID==this.blockID)
         {
 		    TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);
 		    if (te != null && te instanceof ColoredBedTileEntity)
 		    	((ColoredBedTileEntity)te).updatePairName();
         }*/
     }
 	
 	@Override
 	public int idDropped(int meta, Random par2Random, int par3)
     {
         return isBlockHeadOfBed(meta) ? 0 : LoECraftPack.bedItems.itemID;
     }
 	
 	@Override
 	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
     {
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		if( tile instanceof ColoredBedTileEntity)
 			return new ItemStack(LoECraftPack.bedItems, 1, ((ColoredBedTileEntity)tile).id);
 		else
 		    return new ItemStack(LoECraftPack.bedItems, 1, Dye.White.ordinal());
     }
 	
 	@Override
 	public boolean isBed(World world, int x, int y, int z, EntityLiving player)
     {
         return true;
     }
 
 	@Override
 	public TileEntity createNewTileEntity(World world)
 	{
 		return new ColoredBedTileEntity(0);
 	}
 }
