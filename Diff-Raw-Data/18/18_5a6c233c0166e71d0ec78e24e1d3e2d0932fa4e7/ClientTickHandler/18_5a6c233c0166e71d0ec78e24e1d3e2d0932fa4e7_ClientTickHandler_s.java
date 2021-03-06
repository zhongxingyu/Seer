 package openblocks.client;
 
 import java.util.EnumSet;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import openblocks.common.entity.EntityHangGlider;
 import openblocks.common.events.PlayerMovementEvent;
 import openblocks.common.tileentity.OpenTileEntity;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class ClientTickHandler implements ITickHandler {
 
 	private static int ticks = 0;
 
 	private boolean wasJumping = false;
 	private boolean wasSneaking = false;
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 		if (type.contains(TickType.RENDER)) {
 			if (Minecraft.getMinecraft().theWorld != null) {
 				preRenderTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, ((Float)tickData[0]).floatValue());
 			}
 		}
 
 		if (type.contains(TickType.CLIENT)) {
 			clientTick();
 		}
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {}
 
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.RENDER, TickType.CLIENT);
 	}
 
 	@Override
 	public String getLabel() {
 		return "OpenBlocksClientTick";
 	}
 
 	/**
 	 * @param mc
 	 * @param renderTick
 	 */
 	public void preRenderTick(Minecraft mc, World world, float renderTick) {
 		EntityHangGlider.updateGliders(world);
 	}
 
 	private static OpenTileEntity getTileUnderPlayer(EntityPlayer player) {
 		World world = Minecraft.getMinecraft().theWorld;
 		if (world != null && player != null) {
 			int x = (int)player.posX;
 			int y = (int)(player.boundingBox.minY) - 1;
			int z = (int)player.posZ;

 			TileEntity te = world.getBlockTileEntity(x, y, z);
 			if (te instanceof OpenTileEntity) return (OpenTileEntity)te;
 		}
 		return null;
 
 	}
 
 	public void clientTick() {
 		if (SoundEventsManager.isPlayerWearingGlasses()) {
 			SoundEventsManager.instance.tickUpdate();
 		}
 		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
 		if (player != null) {
 			OpenTileEntity target = getTileUnderPlayer(player);
 			if (target != null) {
 				if (player.movementInput.jump && !wasJumping) new PlayerMovementEvent(target, PlayerMovementEvent.Type.JUMP).sendToServer();

 				if (player.movementInput.sneak && !wasSneaking) new PlayerMovementEvent(target, PlayerMovementEvent.Type.SNEAK).sendToServer();
 			}
 			wasJumping = player.movementInput.jump;
 			wasSneaking = player.movementInput.sneak;
 		}
 		ticks++;
 	}
 
 	public static int getTicks() {
 		return ticks;
 	}
 }
