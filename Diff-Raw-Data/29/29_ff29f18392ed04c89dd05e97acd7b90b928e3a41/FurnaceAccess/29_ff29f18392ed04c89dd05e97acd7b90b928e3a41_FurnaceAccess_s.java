 package steamcraft;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntityFurnace;
 
public class FurnaceAccess extends TileEntityFurnace{

 	protected Field stacks;
 	private Method canSmelt = null;
 
	public FurnaceAccess(int size){
 		super();
		if(size!=3){
 			stacks = TileEntityFurnace.class.getDeclaredFields()[3];
 			stacks.setAccessible(true);
 			try {
 				stacks.set(this, new ItemStack[size]);
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 	}
	
	public boolean isSmeltable(){
		if(canSmelt==null){
 			canSmelt = TileEntityFurnace.class.getDeclaredMethods()[15];
 		}
 		canSmelt.setAccessible(true);
 		try {
			return (boolean) canSmelt.invoke(this);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 }
