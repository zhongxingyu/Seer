 package hsb.block;
 
 import hsb.ModHsb;
 import hsb.configuration.Settings;
 import hsb.core.addons.PluginIC2;
 import hsb.core.helper.Utils;
 import hsb.core.proxy.ClientProxy;
 import hsb.item.ItemHsbMultiTool;
 import hsb.lib.GuiIds;
 import hsb.lib.Strings;
import hsb.lib.Textures;
 import hsb.lock.ILockTerminal;
 import hsb.lock.ILockable;
 import hsb.tileentity.TileEntityGuiAccess;
 import hsb.tileentity.TileEntityHsb;
 import hsb.tileentity.TileEntityHsbBuilding;
 import hsb.tileentity.TileEntityHsbTerminal;
 import ic2.api.IWrenchable;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.Facing;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockHsb extends BlockSimpleContainer{
 	
 	public static final int[][] sideAndFacingToSpriteOffset = { { 3, 2, 0, 0, 0, 0 }, { 2, 3, 1, 1, 1, 1 }, { 1, 1, 3, 2, 5, 4 }, { 0, 0, 2, 3, 4, 5 }, { 4, 5, 4, 5, 3, 2 }, { 5, 4, 5, 4, 2, 3 } };
 	
 	public static final int[][] blockTextureId = {{0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1}, 
 												{0, 0, 0, 2, 0, 0, 1, 1, 1, 2, 1, 1},
 												{3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4},
 												{0, 0, 0, 2, 0, 0, 1, 1, 1, 2, 1, 1}};//TODO: Texture for GuiAccessBlock
 	
//    static final String[] textureNames = {"hsb_red", "hsb_green", "terminal", "doorBase_red", "doorBase_green", "Orange"};
	public static final Icon[] blockTextureIcons = new Icon[Textures.BLOCK_HSB.length];
 	
 	public static final int maxDamage = 3;
 	
 	public static final short META_BUILDING = 0;
 	public static final short META_TERMINAL = 1;
 	public static final short META_DOOR_BASE = 2;
 	public static final short META_GUI_ACCESS = 3;
 
 	public BlockHsb(int id) {
 		super(id, Material.iron);
 		this.setHardness(100F);
 		this.setResistance(400F);
 		this.setUnlocalizedName(Strings.BLOCK_HSB);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg) {
     	int i = 0;
    	for(String s : Textures.BLOCK_HSB) {
    		BlockHsb.blockTextureIcons[i++] = reg.registerIcon(s);
     	}		
 	}
     @Override
 	/**
      * ejects contained items into the world, and notifies neighbours of an update, as appropriate
      */
     //TODO drop inventory
     public void breakBlock(World world, int x, int y, int z, int par5, int par6)
     {
     	TileEntityHsb te = (TileEntityHsb) world.getBlockTileEntity(x, y, z);
     	if(te!=null)
     	{
     		te.onRemove(world, x, y, z, par5, par6);
     	}
         super.breakBlock(world, x, y, z, par5, par6);      
     }
     
 	@Override
     public boolean canSilkHarvest()
     {
         return false;
     }
 	
 	@Override
     public int getRenderType()
     {
         return ClientProxy.HSBRENDERER_ID;
     }
 	
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player, ItemStack stack) 
     {
     	TileEntity te = world.getBlockTileEntity(x, y, z);
         int metadata = world.getBlockMetadata(x, y, z);
         
         //Open Gui to set port:
         switch(metadata) {
         case 0: case 2:
         	if(player instanceof EntityPlayer && te!=null)
         		if(stack.itemID == this.blockID)
         			((EntityPlayer) player).openGui(ModHsb.instance, GuiIds.GUI_BLOCKBUILDING, world, x, y, z);
         	break;
         }
         
         //rotate block
         if (player != null && te instanceof IWrenchable) 
         {
             IWrenchable wrenchable = (IWrenchable)te;
             int rotationSegment = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
             if (player.rotationPitch >= 65) 
             {
                 wrenchable.setFacing((short)1);
             } 
             else if (player.rotationPitch <= -65) 
             {
                 wrenchable.setFacing((short)0);
             } 
             else 
             {
                 switch (rotationSegment) 
                 {
                 case 0: wrenchable.setFacing((short)2); break;
                 case 1: wrenchable.setFacing((short)5); break;
                 case 2: wrenchable.setFacing((short)3); break;
                 case 3: wrenchable.setFacing((short)4); break;
                 default:
                     wrenchable.setFacing((short)0); break;
                 }
             }
         }
     }
     @Override
     public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) 
     {
     	//TODO rework to support multiple upgrades (other upgrades that trigger on hit)
     	if(!world.isRemote) {
 	    	TileEntity te = world.getBlockTileEntity(x, y, z);
 	    	if(te != null && te instanceof TileEntityHsb && ((TileEntityHsb)te).locked)
 	    	{
 	    		ILockTerminal terminal = ((ILockable)te).getConnectedTerminal();
 	    		if(terminal != null)
 	    		{
 	    			int tesla = terminal.getTesla();
 	    			if(tesla > 0)
 	    			{
 	    				player.sendChatToPlayer("Don't do that!");
 	    				player.attackEntityFrom(DamageSource.magic, tesla);
 	    			}
 	    		}
 	    	}
     	}
     }
     @Override
     public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float m, float n, float o)
     {       
     	//Items
     	if(player.getCurrentEquippedItem()!=null) {
     		if(Settings.ic2Available)
     			if(player.getCurrentEquippedItem().itemID == PluginIC2.getIC2ItemId("wrench") || player.getCurrentEquippedItem().itemID == PluginIC2.getIC2ItemId("electricWrench"))
     				return false;
     		if(player.getCurrentEquippedItem().getItem() instanceof ItemHsbMultiTool)
     			return false;
     	}
     	
     	switch (world.getBlockMetadata(i, j, k))
         {
             case META_BUILDING: case META_DOOR_BASE:
             	return false;
             /**
              * Terminal
              */
             case META_TERMINAL:
             {
             	if(!world.isRemote) {
 //            		PacketTerminalButtons packet = new PacketTerminalButtons();
 //            		PacketDispatcher.sendPacketToPlayer(packet.getPacket(), (Player)player);
             		//open gui
             		player.openGui(ModHsb.instance, GuiIds.GUI_LOCKTERMINAL, world, i, j, k);
             	}
                 return true;
             }
         	/**
         	 * Gui access
         	 */
             case META_GUI_ACCESS:
             {
             	TileEntityGuiAccess tile = (TileEntityGuiAccess) world.getBlockTileEntity(i, j, k);
             	int facing = Facing.faceToSide[tile.getFacing()];
             	int x = Facing.offsetsXForSide[facing] + i;
             	int y = Facing.offsetsYForSide[facing] + j;
             	int z = Facing.offsetsZForSide[facing] + k;
             	int blockId = world.getBlockId(x, y, z);
             	int meta = world.getBlockMetadata(x, y, z);
 //            	HsbLog.debug("Meta: " + meta + " side: " + side + " Block: " + Block.blocksList[blockId] + " Pos: " + x + ", " + y + ", " + z);
             	if(Block.blocksList[blockId] == null || (blockId == this.blockID && meta == 3))
             	{
             		return false;
             	}
             	if(Block.blocksList[blockId].onBlockActivated(world, x, y, z, player, Facing.faceToSide[tile.getFacing()], m, n, o)) {
             	} else {
             		Utils.sendChatToPlayer(StatCollector.translateToLocal(Strings.CHAT_NOTHING_HAPPENED), player, true);
             	}
             }
             	return true;
             default:
                 return false;
         }    }
     
     @SuppressWarnings({ "rawtypes", "unchecked" })
     @Override
     public void getSubBlocks(int id, CreativeTabs tab, List itemList)
     {
         itemList.add(new ItemStack(id, 1, 0));
         itemList.add(new ItemStack(id, 1, 1));
         itemList.add(new ItemStack(id, 1, 2));
         itemList.add(new ItemStack(id, 1, 3));
     }
     
 	@Override
     public int damageDropped(int meta)
     {
         if(meta == META_DOOR_BASE)
         	return 0;
         return meta;
     }
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return null;
 	}
 	
 	@Override
 	public TileEntity createTileEntity(World world, int meta) {
 		switch(meta)
 		{
 			case META_BUILDING:
 				return new TileEntityHsbBuilding();
 			case META_TERMINAL:
 				return new TileEntityHsbTerminal();
 			case META_DOOR_BASE:
 				return new TileEntityHsbBuilding();
 			case META_GUI_ACCESS://TODO
 				return new TileEntityGuiAccess();
 			default:
 				return null;
 		}
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
     public Icon getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int side)
     {
         TileEntity te = iblockaccess.getBlockTileEntity(i, j, k);
         int meta = iblockaccess.getBlockMetadata(i, j, k);
         
         int texid = sideAndFacingToSpriteOffset[side][5];
         
         if(meta == BlockHsb.META_GUI_ACCESS || meta == BlockHsb.META_TERMINAL) {
 	        short facing = 0;
 	        
 	        facing = ((IWrenchable) te).getFacing();
 	        texid = sideAndFacingToSpriteOffset[side][facing];
         }
         
 		if(((ILockable)te).isLocked())
         {
         	texid = texid + 6;
         }
         
         int tex = blockTextureId[meta][texid];
         
         if(te instanceof TileEntityHsbBuilding && ((TileEntityHsbBuilding) te).isLocked() && ((TileEntityHsbBuilding) te).camoId != -1)
         {
         	TileEntityHsbBuilding tebuild = (TileEntityHsbBuilding) te;
         	Block block = Block.blocksList[tebuild.camoId];
         	if(block != null && block.isOpaqueCube())
         	{
         		return block.getBlockTextureFromSideAndMetadata(side, tebuild.camoMeta);
         	}
         }
         
         return blockTextureIcons[tex];
 
     }
 	
 	@Override
     public Icon getBlockTextureFromSideAndMetadata(int side, int meta)
 	{
 		if(meta > maxDamage)
 			return blockTextureIcons[0];
 		int texid = sideAndFacingToSpriteOffset[side][1];
 		int tex = blockTextureId[meta][texid];
 	    return blockTextureIcons[tex];
 
     }
 	
     @Override
     public int getMobilityFlag()
     {
         return 2;
     }
 
 }
