 package monoxide.Lanterns.client;
 
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.List;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.AxisAlignedBB;
 import net.minecraft.src.Block;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.TileEntityChest;
 import net.minecraft.src.World;
 import net.minecraftforge.client.ForgeHooksClient;
 import net.minecraftforge.client.ILightingHandler;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.MinecraftForge;
 import monoxide.Lanterns.CommonProxy;
 import monoxide.Lanterns.Lanterns;
 import monoxide.Lanterns.TileEntityZeroTorch;
 
 public class ClientProxy extends CommonProxy implements ILightingHandler {
 	private final int ZERO_TORCH_MAX_LIGHT = 8;
 	
 	
 	@Override
 	public void registerRenderers() {
 		MinecraftForgeClient.preloadTexture(CommonProxy.ITEMS_PNG);
 		ForgeHooksClient.registerLightingHandler(this);
 	}
 
 	@Override
 	public int getLightingPriority() {
 		return 10;
 	}
 
 	@Override
 	public int getSkyLightLevel(int x, int y, int z, int currentLight, LightingType type) {
		return -1;
 	}
 
 	@Override
 	public int getBlockLightLevel(int x, int y, int z, int currentLight, LightingType type) {
		int nearestDistance = 1000;
 
 		// NB: This should be synchronized properly and the TileEntity side is, but since doing 
		// so in the rendering layer literally halves our FPS we make do with catching sync 
 		// errors and retrying. The exception is far less painful.
 		try {
 			for (TileEntity entity : TileEntityZeroTorch.allTorches) {
 				int distance = distance(x, y, z, entity.xCoord, entity.yCoord, entity.zCoord);
				if (distance < nearestDistance) {
					nearestDistance = distance;
 				}
 			}
 		} catch (NullPointerException ex) {
			return getBlockLightLevel(x, y, nearestDistance, currentLight, type);
 		} catch (ConcurrentModificationException ex) {
			return getBlockLightLevel(x, y, nearestDistance, currentLight, type);
 		}
 		
		if (nearestDistance <= ZERO_TORCH_MAX_LIGHT) {
 			return ZERO_TORCH_MAX_LIGHT;
 		}
 		
		return Math.min(currentLight, nearestDistance);
 	}
 	
 	private int distance(int x1, int y1, int z1, int x2, int y2, int z2) {
 		int xDiff = x2 - x1;
 		int yDiff = y2 - y1;
 		int zDiff = z2 - z1;
 		
 		return (int) Math.round(Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
 	}
 }
