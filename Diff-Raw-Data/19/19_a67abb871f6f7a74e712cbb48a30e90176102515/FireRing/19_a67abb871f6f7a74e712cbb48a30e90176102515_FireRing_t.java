 package hunternif.mc.rings.item;
 
 import hunternif.mc.rings.RingsOfPower;
 import hunternif.mc.rings.util.BlockUtil;
 
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.ObfuscationReflectionHelper;
 
 public class FireRing extends PoweredRing {
 	private static final String[] immuneToFireObfNames = {"field_70178_ae", "isImmuneToFire"};
 	private static final int deltaYdown = 3;
 	private static final int deltaYup = 4;
 	
 	public FireRing(int id) {
 		super(id);
 		MinecraftForge.EVENT_BUS.register(this);
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
 		if (!player.capabilities.isCreativeMode && !hasFuel(itemStack, player)) {
 			FMLLog.log(RingsOfPower.ID, Level.INFO, "No fuel in inventory!");
 			return itemStack;
 		}
 		if (!world.isRemote) {
 			int playerX = MathHelper.floor_double(player.posX);
 			int playerY = MathHelper.floor_double(player.posY);
 			int playerZ = MathHelper.floor_double(player.posZ);
 			for (int x = playerX - 4; x <= playerX + 4; x++) {
 				for (int z = playerZ - 4; z <= playerZ + 4; z++) {
 					boolean foundSurface = false;
 					int y = playerY;
 					// Look down:
 					while (Math.abs(playerY-y) <= deltaYdown) {
 						if (BlockUtil.isSurfaceAt(world, x, y, z, true)) {
 							foundSurface = true;
 							break;
 						} else {
 							y--;
 						}
 					}
 					if (!foundSurface) {
 						y = playerY + 1;
 						// Look up:
 						while (Math.abs(playerY-y) <= deltaYup) {
 							if (BlockUtil.isSurfaceAt(world, x, y, z, true)) {
 								foundSurface = true;
 								break;
 							} else {
 								y++;
 							}
 						}
 					}
 					if (foundSurface) {
 						world.setBlock(x, y, z, Block.fire.blockID);
 						if (world.getBlockId(x, y-1, z) == Block.obsidian.blockID) {
 							world.setBlock(x, y-1, z, Block.lavaStill.blockID);
 						}
 					}
 				}
 			}
 		}
 		consumeFuel(itemStack, player);
 		return itemStack;
 	}
 	
	private Set<EntityPlayer> playersWithThisItem = Collections.synchronizedSet(new HashSet<EntityPlayer>());
	
 	@ForgeSubscribe
 	public void onPlayerUpdate(LivingUpdateEvent event) {
 		if (event.entityLiving instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			if (player.inventory.hasItem(this.itemID)) {
				if (!playersWithThisItem.contains(player)) {
 					ObfuscationReflectionHelper.setPrivateValue(Entity.class, player, true, immuneToFireObfNames);
					playersWithThisItem.add(player);
 				}
 			} else {
				if (playersWithThisItem.contains(player)) {
 					ObfuscationReflectionHelper.setPrivateValue(Entity.class, player, false, immuneToFireObfNames);
					playersWithThisItem.remove(player);
 				}
 			}
 		}
 	}
 }
