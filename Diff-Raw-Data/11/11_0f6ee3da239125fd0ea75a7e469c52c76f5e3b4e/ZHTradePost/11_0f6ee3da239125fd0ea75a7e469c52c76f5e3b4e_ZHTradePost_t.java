 package zh.tradecenter.blocks;
 
 import zh.tradecenter.tileentities.ZHTradePostEntity;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 // TODO: Real textures...
 public class ZHTradePost extends BlockContainer
 {
     
     private String _unlocalizedName;
    @SideOnly(Side.CLIENT)
    private Icon[] icons;
     
     public ZHTradePost(int id)
     {
         super(id, Material.wood);
     }
     
     @Override
     public Block setUnlocalizedName(String name)
     {
         this._unlocalizedName = name;
         return super.setUnlocalizedName(name);
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister)
     {
         icons = new Icon[3];
         
        icons[0] = iconRegister.registerIcon("tradecenter:tradecenter_bottom"); // bottom
        icons[1] = iconRegister.registerIcon("tradecenter:tradecenter_top"); // top
        icons[2] = iconRegister.registerIcon("tradecenter:tradecenter_side"); // sides
         
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int side, int meta)
     {
         if (side <= 1) // top/bottom
             return icons[side];
         
         return icons[2];
     }
     
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset)
     {
         if (!world.isRemote)
         {
             TileEntity tile = world.getBlockTileEntity(x, y, z);
             
             if (tile instanceof ZHTradePostEntity)
             {
                 Packet packet = ((ZHTradePostEntity) tile).getTradePacket(player);
                 
                 if (packet != null)
                     PacketDispatcher.sendPacketToPlayer(packet, (Player) player);
                 else
                     player.sendChatToPlayer(new ChatMessageComponent().addText("No villagers found in range of this trade post"));
                 
                 return true;
             }
             else
                 return true;
         }
         else
             return true;
         
     }
     
     @Override
     public TileEntity createNewTileEntity(World world)
     {
         return new ZHTradePostEntity();
     }
     
 }
