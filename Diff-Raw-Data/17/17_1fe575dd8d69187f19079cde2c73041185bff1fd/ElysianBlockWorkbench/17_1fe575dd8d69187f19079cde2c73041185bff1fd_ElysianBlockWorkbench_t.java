 package mods.elysium.block;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import mods.elysium.DefaultProps;
 import mods.elysium.Elysium;
 import mods.elysium.client.gui.GuiElysianCrafting;
 import mods.elysium.entity.tileentity.TileEntityElysianWorkbench;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.src.ModLoader;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class ElysianBlockWorkbench extends ElysianBlockContainer
 {
 	@SideOnly(Side.CLIENT)
 	private Icon iconTop;
 	@SideOnly(Side.CLIENT)
 	private Icon iconSideA;
 	@SideOnly(Side.CLIENT)
 	private Icon iconSideB;
 	
 	public ElysianBlockWorkbench(int id)
 	{
 		super(id, Material.wood);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register)
 	{
 		this.blockIcon = register.registerIcon(DefaultProps.modId + ":" + Elysium.blockPlanksFostimber.getUnlocalizedName().substring(5));
 		this.iconTop = register.registerIcon(DefaultProps.modId + ":" + this.getUnlocalizedName().substring(5)+"_top");
 		this.iconSideA = register.registerIcon(DefaultProps.modId + ":" + this.getUnlocalizedName().substring(5)+"_sidea");
 		this.iconSideB = register.registerIcon(DefaultProps.modId + ":" + this.getUnlocalizedName().substring(5)+"_sideb");
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta)
 	{
 		if(side == 1)
 			return this.iconTop;
 		if((side == 2) || (side == 3))
 			return this.iconSideA;
 		if((side == 4) || (side == 5))
 			return this.iconSideB;
 		
 		return this.blockIcon;
 	}
 	
 	@Override
	@SideOnly(Side.CLIENT)
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
 	{
		if(world.isRemote)
 			ModLoader.openGUI(player, new GuiElysianCrafting(world, x, y, z, player.inventory, (IInventory)world.getBlockTileEntity(x, y, z)));
 		return true;
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world)
 	{
 		return new TileEntityElysianWorkbench();
 	}
 }
