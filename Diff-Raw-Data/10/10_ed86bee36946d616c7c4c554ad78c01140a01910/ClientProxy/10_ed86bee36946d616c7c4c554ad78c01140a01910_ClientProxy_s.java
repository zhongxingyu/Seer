 package ayamitsu.urtsquid;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.client.settings.KeyBinding;
 import ayamitsu.urtsquid.client.KeySimpleHandler;
 import ayamitsu.urtsquid.client.RenderPlayerSquid;
 import ayamitsu.urtsquid.client.StatusRenderer;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.registry.KeyBindingRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class ClientProxy extends CommonProxy {
 
 	@Override
 	public void load() {
 		RenderingRegistry.registerEntityRenderingHandler(EntityClientPlayerMP.class, new RenderPlayerSquid());
		TickRegistry.registerTickHandler(new StatusRenderer(), Side.CLIENT);
 		KeyBindingRegistry.registerKeyBinding(new KeySimpleHandler(new KeyBinding[] { new KeyBinding("ToggleParasite", 19),  }, new boolean[] { false }));// key R
 	}
 
 }
