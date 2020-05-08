 package com.allaryin.kinkspring.block;
 
 import com.allaryin.kinkspring.lib.Config;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 import net.minecraftforge.common.ForgeDirection;
 
 public class BlockCapacitor extends KBlock {
 	@SideOnly(Side.CLIENT)
 	private Icon blockEmpty;
 	@SideOnly(Side.CLIENT)
 	private Icon blockFull;
 	@SideOnly(Side.CLIENT)
 	private Icon blockInput;
 	@SideOnly(Side.CLIENT)
 	private Icon blockOutput;
 	
 	public BlockCapacitor(Integer id, Integer bid) {
 		super(id, bid, Material.iron);
 		setHardness(0.5F);
 		setStepSound(soundMetalFootstep);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register) {
 		System.out.println("registering icons for " + enumEntry.name());
 		blockEmpty = register.registerIcon(Config.TEXTURE_PATH + ":" + enumEntry.name() + "Empty");
 		blockFull = register.registerIcon(Config.TEXTURE_PATH + ":" + enumEntry.name() + "Full");
 		blockInput = register.registerIcon(Config.TEXTURE_PATH + ":" + enumEntry.name() + "Input");
 		blockOutput = register.registerIcon(Config.TEXTURE_PATH + ":" + enumEntry.name() + "Output");
 		blockIcon = blockEmpty;
 	}
 	
 	public Icon getIcon(int side, int meta) {
		final ForgeDirection dir = ForgeDirection.getOrientation(side);		
		final ForgeDirection front = (meta != 0 ? ForgeDirection.getOrientation(meta) : ForgeDirection.UP);
		final ForgeDirection back = front.getOpposite();		
		if( dir == front )
 			return blockInput;
		else if( dir == back )
 			return blockOutput;
 		return blockEmpty;
 	}
 	
 }
