 package ak.ChainDestruction;
 
 import java.util.EnumSet;
 
 import net.minecraft.client.settings.KeyBinding;
 import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
 import cpw.mods.fml.common.TickType;
 
 public class CDKeyHandler extends KeyHandler{
 
 	public static boolean regItemKeyDown = false;
	public static boolean regItemKeyUp = true;
 	public static boolean digUnderKeyDown = false;
	public static boolean digUnderKeyUp = true;
 	public CDKeyHandler(KeyBinding[] keyBindings, boolean[] repeats) {
 		super(keyBindings, repeats);
 	}
 
 	@Override
 	public String getLabel() {
 		return "ChainDestruction:"+this.getClass().getSimpleName();
 	}
 
 	@Override
 	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
 		if (types.equals(EnumSet.of(TickType.PLAYER)))
 		{
 			if(kb == ClientProxy.registItemKey)
 			{
 				this.regItemKeyDown = true;
 			}
 			else if(kb == ClientProxy.digUnderKey)
 			{
 				this.digUnderKeyDown = true;
 			}
 		}
 	}
 
 	@Override
 	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
 		if (types.equals(EnumSet.of(TickType.PLAYER)))
 		{
 			if(kb == ClientProxy.registItemKey)
 			{
 				this.regItemKeyDown = false;
				this.regItemKeyUp = true;
 			}
 			else if(kb == ClientProxy.digUnderKey)
 			{
 				this.digUnderKeyDown = false;
				this.digUnderKeyUp = true;
 			}
 		}
 	}
 
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.PLAYER);
 	}
 	
 }
