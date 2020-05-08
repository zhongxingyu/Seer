 package steamcraft.blocks;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Map;
 
 import net.minecraft.block.BlockRedstoneTorch;
 import net.minecraft.world.World;
 
 public class BlockRedstoneAccess extends BlockRedstoneTorch{
 
 	protected BlockRedstoneAccess(int par1, boolean par2) {
 		super(par1, par2);
 	}
 
 	protected boolean hasIndirectPower(World world, int i, int j, int k){
 		Method isPower = BlockRedstoneTorch.class.getDeclaredMethods()[5];
 		isPower.setAccessible(true);
 		try {
			return (boolean) isPower.invoke(this, world, i, j, k);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	protected boolean checkBurnout(World world, int i, int j, int k, boolean is){
 		Method burnout = BlockRedstoneTorch.class.getDeclaredMethods()[0];
 		burnout.setAccessible(true);
 		try {
			return (boolean) burnout.invoke(this, world, i, j, k, is);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public static Map<?, ?> getRedstoneUpdateList() {
 		Field f;
 		Object obj = null;
 		try {
 			f = BlockRedstoneTorch.class.getDeclaredFields()[1];//redstoneUpdateInfoCache
 			f.setAccessible(true);
 			obj = f.get(null);
 		} catch (ReflectiveOperationException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 		return Map.class.cast(obj);
 	}
 }
