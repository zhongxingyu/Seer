 package com.cane.block;
 
 import com.cane.CaneCraft;
 import com.cane.Machine;
 import com.cane.tileentity.TileEntityMachine;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.EnumPlantType;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IPlantable;
 
 public class BlockMachine extends BlockCC
 {
 	public BlockMachine(int id)
 	{
 		super(id, Material.iron);
 		this.blockIndexInTexture = 16;
 		this.setHardness(5.0F);
 		this.setResistance(10.0F);
 		this.setStepSound(soundMetalFootstep);
 	}
 	
 	@Override
 	public boolean onBlockActivated(World world, int x, int y,
 			int z, EntityPlayer player, int par6, float par7,
 			float par8, float par9)
 	{
 		if(player.isSneaking())return false;
 		
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		
 		if(tile != null && tile instanceof TileEntityMachine)
 		{
 			player.openGui(CaneCraft.instance, 0, world, x, y, z);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public TileEntity createTileEntity(World world, int metadata)
 	{
 		return Machine.getTileEntity(metadata);
 	}
 	
 	@Override
 	public boolean hasTileEntity(int metadata)
 	{
 		return Machine.hasClass(metadata);
 	}
 		
 	@Override
 	public int damageDropped(int metadata)
 	{
 		return metadata;
 	}
 
 	@Override
 	public int getBlockTexture(IBlockAccess block, int x,
 			int y, int z, int side)
 	{
 		if(block.getBlockMetadata(x, y, z) < 7)
 		{
 			TileEntity tile = block.getBlockTileEntity(x, y, z);
 			
 			if(tile != null && tile instanceof TileEntityMachine)
 			{
 				return ((TileEntityMachine) tile).getTexture(side);
 			}
 		}
 		
 		return getBlockTextureFromSideAndMetadata(side, block.getBlockMetadata(x, y, z));
 	}
 
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int side, int metadata)
 	{
 		if(metadata < 8)
 		{
 			if(side == 1)return 16 * 1;
 			if(side == 1)return 16 * 3;
 			if(side == 3)return (16 * 4) + metadata;
 			return 16 * 2;
 		}
 		else if(metadata == 8)
 		{	
 			return (16 * 15) + (side > 1 ? 2 : side);
 		}
 		else
 		{
 			return (16 * 15) + metadata - 8;
 		}
 	}
 	
 	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving living)
 	{
 		TileEntityMachine tile = (TileEntityMachine)world.getBlockTileEntity(x, y, z);
 	    if (tile != null)
 	    {
 	    	int rot = MathHelper.floor_double((double)(living.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
 
 		    switch (rot)
 		    {
 		    	case 0: tile.setFace(2); return;
 		    	case 1: tile.setFace(5); return;
 		    	case 2: tile.setFace(3); return;
 		    	case 3: tile.setFace(4); return;
 		    }
 	    }
 	}
 	
 	@Override
 	public int getLightValue(IBlockAccess world, int x, int y, int z)
 	{
		if(world.getBlockMetadata(x, y, z) == 8)
 		{
 			return 15;
 		}
 		else
 		{
 			return 0;
 		}
 	}
 
 	@Override
 	public boolean canSustainPlant(World world, int x, int y, int z,
 			ForgeDirection direction, IPlantable plant)
 	{
 		if(world.getBlockMetadata(x, y, z) == 8)
 		{   
 	        if(plant.getPlantType(world, x, y + 1, z) == EnumPlantType.Beach)
 	        {
 	        	return true;
 	        }
 		}
 		else if(world.getBlockMetadata(x, y, z) == 5)
 		{
 			if(plant.getPlantID(world, x, y, z) == CaneCraft.Blocks.cane.blockID &&
 				plant.getPlantMetadata(world, x, y, z) == 14)
 	        {
 	        	return true;
 	        }
 		}
 		
 		return super.canSustainPlant(world, x, y, z, direction, plant);
 	}
 }
