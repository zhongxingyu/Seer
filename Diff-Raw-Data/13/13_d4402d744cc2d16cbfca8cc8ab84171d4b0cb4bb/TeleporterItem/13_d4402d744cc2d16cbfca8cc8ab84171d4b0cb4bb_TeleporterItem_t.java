 package teamm.mods.virtious.item;
 
 import java.util.Random;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 import teamm.mods.virtious.Virtious;
 import teamm.mods.virtious.entity.item.EntityStickyBomb;
 import teamm.mods.virtious.network.StickyBombPacket;
 import teamm.mods.virtious.world.VirtiousTeleporter;
 import teamm.mods.virtious.world.gen.VirtiousGenAmberTree;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class TeleporterItem extends VirtiousItem{
 
 	public TeleporterItem(int id) {
 		super(id);
 	}
     
 	@Override
 	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int i, int j, int k, int par7, float par8, float par9, float par10)
 	{
 		if(!world.isRemote){
 			EntityStickyBomb bomb = new EntityStickyBomb(world, i, j, k, player);
 			world.spawnEntityInWorld(bomb);
 //     		PacketDispatcher.sendPacketToAllAround(bomb.posX, bomb.posY, bomb.posZ, 64, bomb.dimension, new StickyBombPacket(bomb).getPacket());
      		PacketDispatcher.sendPacketToAllInDimension(new StickyBombPacket(bomb).getPacket(), bomb.dimension);
 			System.out.println("Spawned & Packet sent - stickyBomb");
 		}  
 
 //		VirtiousGenAmberTree gen = new VirtiousGenAmberTree();
 //		gen.generate(world, new Random(), i, j, k);
		return false;
 		
 	}
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player){
 		
 		if(player.isSneaking() && (player instanceof EntityPlayerMP)){
 			EntityPlayerMP playermp = (EntityPlayerMP) player;
 			
			if(player.dimension == Virtious.dimensionID)
			{
				playermp.mcServer.getConfigurationManager().transferPlayerToDimension(playermp, 0,
						new VirtiousTeleporter(playermp.mcServer.worldServerForDimension(0)));
			} else {
				playermp.mcServer.getConfigurationManager().transferPlayerToDimension(playermp, Virtious.dimensionID,
						new VirtiousTeleporter(playermp.mcServer.worldServerForDimension(Virtious.dimensionID)));
			}
 		}
 		
 		return item;
 	}
 }
