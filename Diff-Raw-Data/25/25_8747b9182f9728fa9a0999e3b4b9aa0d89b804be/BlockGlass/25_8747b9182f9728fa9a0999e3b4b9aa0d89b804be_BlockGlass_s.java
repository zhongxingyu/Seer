 package com.qzx.au.extras;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import net.minecraftforge.common.ForgeDirection;
 
 import com.qzx.au.core.BlockCoord;
 import com.qzx.au.core.IConnectedTexture;
 
 public class BlockGlass extends BlockColored implements IConnectedTexture {
 	@SideOnly(Side.CLIENT)
 	private Icon[][] blockIcons;
 	@SideOnly(Side.CLIENT)
 	private Icon[] itemIcons;
 
 	private int style;
 
 	public BlockGlass(int id, String name, String readableName, Class<? extends ItemBlock> itemblockclass, int style){
 		super(id, name, readableName, itemblockclass, Material.glass);
 		this.style = style;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister){
 		switch(this.style){
 		case 0: // clear
 			this.blockIcons = new Icon[16][IConnectedTexture.ctm_icons];
 			for(int c = 0; c < 16; c++)
 				for(int t = 0; t < 47; t++)
 					this.blockIcons[c][IConnectedTexture.ctm[t]] = iconRegister.registerIcon("au_extras:"+this.getUnlocalizedName().replace("tile.au.", "")+c+"-"+t);
 			break;
 		case 1: // tinted
 			this.blockIcons = new Icon[16][IConnectedTexture.ctm_icons];
 			this.itemIcons = new Icon[16];
 			for(int c = 0; c < 16; c++){
 				for(int t = 0; t < 47; t++)
 					this.blockIcons[c][IConnectedTexture.ctm[t]] = iconRegister.registerIcon("au_extras:"+this.getUnlocalizedName().replace("tile.au.", "")+c+"-"+t);
 				this.itemIcons[c] = iconRegister.registerIcon("au_extras:"+this.getUnlocalizedName().replace("tile.au.", "")+c+"-item");
 			}
 			break;
 		case 2: // tinted frameless
 			this.blockIcons = new Icon[16][1];
 			this.itemIcons = new Icon[16];
 			for(int c = 0; c < 16; c++){
 				this.blockIcons[c][0] = iconRegister.registerIcon("au_extras:"+this.getUnlocalizedName().replace("tile.au.", "")+c);
 				this.itemIcons[c] = iconRegister.registerIcon("au_extras:"+this.getUnlocalizedName().replace("tile.au.", "")+c+"-item");
 			}
 			break;
 		}
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int color){
 		return this.blockIcons[color][this.style == 2 ? 0 : IConnectedTexture.ctm_default];
 	}
 
 	@SideOnly(Side.CLIENT)
 	public Icon getItemIcon(int color){
 		return (this.style == 0 ? this.blockIcons[color][IConnectedTexture.ctm_default] : this.itemIcons[color]);
 	}
 
 	@Override
 	public boolean isOpaqueCube(){
 		return false;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock(){
 		return false;
 	}
 
 	@Override
 	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side){
 		return true;
 	}
 
 	@Override
	@SideOnly(Side.CLIENT)
 	public int getRenderType(){
 		return (this.style == 0 ? 0 : ClientProxy.glassRenderType);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public int getRenderBlockPass(){
 		return (this.style == 0 ? 0 : 1);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean canRenderInPass(int pass){
 		ClientProxy.renderPass = pass;
 		return (this.style == 0 && pass == 0) || (this.style != 0 && pass != 0);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side){
 		// coordinates are the block at each side, not the block being rendered
 
 		if(this.style == 2) return super.shouldSideBeRendered(access, x, y, z, side); // no connected textures
 
 		BlockCoord neighbor = new BlockCoord(access, x, y, z);
 		BlockCoord coord = (new BlockCoord(neighbor)).translateToSide(ForgeDirection.OPPOSITES[side]);
 		if(coord.getBlockID() != neighbor.getBlockID()) return true;
 		if(coord.getBlockMetadata() != neighbor.getBlockMetadata()) return true;
 		return false;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public boolean canConnectSideTextures(int id, int metadata, int side, BlockCoord neighbor){
 		if(this.style == 2) return false; // no connected textures
 
 		int neighbor_id = neighbor.getBlockID();
 		Block neighbor_block = neighbor.getBlock();
 		int neighbor_style = (neighbor_block instanceof BlockGlass ? ((BlockGlass)neighbor_block).style : -1);
 
 		// connect to same color of glass on sides
 		if(neighbor_id != id || neighbor.getBlockMetadata() != metadata && neighbor_style == this.style) return false;
 
 		//////////
 
 		/*
 			A
 			BC
 
 			- when rendering the top of C on the side that connects to B
 			- diagonal block is A
 		*/
 
 		BlockCoord diagonal = (new BlockCoord(neighbor)).translateToSide(side);
 		int diagonal_id = diagonal.getBlockID();
 		Block diagonal_block = diagonal.getBlock();
 		int diagonal_style = (diagonal_block instanceof BlockGlass ? ((BlockGlass)diagonal_block).style : -1);
 
 		// must not have same color of glass on this side (to render inner frames)
 		if(diagonal_id == id && diagonal.getBlockMetadata() == metadata && diagonal_style == this.style) return false;
 
 		return true;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public boolean canConnectCornerTextures(int id, int metadata, int side, BlockCoord diagonal){
 		if(this.style == 2) return false; // no connected textures
 
 		int diagonal_id = diagonal.getBlockID();
 		Block diagonal_block = diagonal.getBlock();
 		int diagonal_style = (diagonal_block instanceof BlockGlass ? ((BlockGlass)diagonal_block).style : -1);
 
 		//////////
 
 		/*
 			AB
 			CD
 			- when rendering the top/bottom of D on the corner that connects to A
 			- diagonal_diagonal block is above/below block A
 		*/
 
 		BlockCoord diagonal_diagonal = (new BlockCoord(diagonal)).translateToSide(side);
 		int diagonal_diagonal_id = diagonal_diagonal.getBlockID();
 		Block diagonal_diagonal_block = diagonal_diagonal.getBlock();
 		int diagonal_diagonal_style = (diagonal_diagonal_block instanceof BlockGlass ? ((BlockGlass)diagonal_diagonal_block).style : -1);
 
 		// must not have same color of glass on this corner (to render inner frames)
 		if(diagonal_diagonal_id == id && diagonal_diagonal.getBlockMetadata() == metadata && diagonal_diagonal_style == this.style) return false;
 
 		//////////
 
 		// connect to same color glass (corners)
 		return (diagonal_id == id && diagonal.getBlockMetadata() == metadata && diagonal_style == this.style);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getBlockTexture(IBlockAccess access, int x, int y, int z, int side){
 		BlockCoord coord = new BlockCoord(access, x, y, z);
 		int blockID = coord.getBlockID();
 		int blockColor = coord.getBlockMetadata();
 
 		if(this.style == 2) return this.blockIcons[blockColor][0]; // no connected textures
 
 		BlockCoord u = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.UP);
 		BlockCoord d = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.DOWN);
 		BlockCoord l = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.LEFT);
 		BlockCoord r = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.RIGHT);
 		BlockCoord ul = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.UP, BlockCoord.LEFT);
 		BlockCoord ur = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.UP, BlockCoord.RIGHT);
 		BlockCoord dl = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.DOWN, BlockCoord.LEFT);
 		BlockCoord dr = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.DOWN, BlockCoord.RIGHT);
 		boolean connect_t = this.canConnectSideTextures(blockID, blockColor, side, u);
 		boolean connect_r = this.canConnectSideTextures(blockID, blockColor, side, r);
 		boolean connect_b = this.canConnectSideTextures(blockID, blockColor, side, d);
 		boolean connect_l = this.canConnectSideTextures(blockID, blockColor, side, l);
 		boolean connect_tl = this.canConnectCornerTextures(blockID, blockColor, side, ul);
 		boolean connect_tr = this.canConnectCornerTextures(blockID, blockColor, side, ur);
 		boolean connect_bl = this.canConnectCornerTextures(blockID, blockColor, side, dl);
 		boolean connect_br = this.canConnectCornerTextures(blockID, blockColor, side, dr);
 
 		int texture = 0;
 		if(!connect_t) texture |= 1<<0;							// T
 			else if(connect_l && !connect_tl) texture |= 2<<0;	// tl
 		if(!connect_r) texture |= 1<<2;							// R
 			else if(connect_t && !connect_tr) texture |= 2<<2;	// tr
 		if(!connect_b) texture |= 1<<4;							// B
 			else if(connect_r && !connect_br) texture |= 2<<4;	// br
 		if(!connect_l) texture |= 1<<6;							// L
 			else if(connect_b && !connect_bl) texture |= 2<<6;	// bl
 
 		return this.blockIcons[blockColor][texture];
 	}
 
 	@Override
 	public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z){
 		return false;
 	}
 }
