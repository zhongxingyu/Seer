 package vazkii.easyfps.client;
 
 import java.io.File;
 import java.util.EnumSet;
 
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.KeyBinding;
 import net.minecraft.src.NBTTagCompound;
 
 import org.lwjgl.input.Keyboard;
 
 import vazkii.codebase.client.ClientUtils;
 import vazkii.codebase.common.CommonUtils;
 import vazkii.codebase.common.EnumVazkiiMods;
 import vazkii.codebase.common.IOUtils;
 import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
 import cpw.mods.fml.common.TickType;
 
 public class EasyFPSKeyHandler extends KeyHandler {
 
 	public static KeyBinding key = new KeyBinding("FPS", Keyboard.KEY_F4);
 
 	public EasyFPSKeyHandler() {
 		super(new KeyBinding[] { key }, new boolean[] { false });
 	}
 
 	@Override
 	public String getLabel() {
 		return "EasyFPS";
 	}
 
 	@Override
 	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
		if (!types.equals(EnumSet.of(TickType.RENDER)) || !tickEnd) return;
 
 		File cacheFile = IOUtils.getCacheFile(EnumVazkiiMods.EASYFPS);
 		NBTTagCompound comp = IOUtils.getTagCompoundInFile(cacheFile);
 		EntityPlayer clientPlayer = ClientUtils.getClientPlayer();
 
 		if (clientPlayer == null || !clientPlayer.isSneaking()) {
 			mod_EasyFPS.fpsEnabled = CommonUtils.flipBoolean(mod_EasyFPS.fpsEnabled);
 			comp.setBoolean("fpsEnabled", mod_EasyFPS.fpsEnabled);
 			IOUtils.injectNBTToFile(comp, cacheFile);
 		}
 		else {
 			mod_EasyFPS.colorEnabled = CommonUtils.flipBoolean(mod_EasyFPS.colorEnabled);
 			comp.setBoolean("colorEnabled", mod_EasyFPS.colorEnabled);
 			IOUtils.injectNBTToFile(comp, cacheFile);
 		}
 	}
 
 	@Override
 	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {}
 
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.RENDER);
 	}
 
 }
