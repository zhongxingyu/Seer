 package net.skyrimcraft.src.proxy;
 
 import net.minecraft.client.model.ModelBiped;
 import net.minecraft.client.settings.KeyBinding;
 import net.skyrimcraft.src.entity.EntityImperialGuard;
 import net.skyrimcraft.src.handler.CrossMenuKeyHandler;
 import net.skyrimcraft.src.handler.SkyrimTickHandler;
 import net.skyrimcraft.src.model.armor.DragonbornHelmet;
 import net.skyrimcraft.src.render.RenderImperialGuard;
 import net.skyrimcraft.src.render.block.BenchRenderer;
 import net.skyrimcraft.src.render.block.BookshelfRenderer;
 import net.skyrimcraft.src.render.block.CommonEndTableRenderer;
 import net.skyrimcraft.src.render.block.CrateRenderer;
 
 import org.lwjgl.input.Keyboard;
 
 import com.jadarstudios.developercapes.DevCapesUtil;
 
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.KeyBindingRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class ClientProxy extends CommonProxy
 {
 	private final DragonbornHelmet dragonbornHelmet = new DragonbornHelmet(1.0f);
 	
 	@Override
 	public void init()
 	{
 		RenderingRegistry.registerEntityRenderingHandler(EntityImperialGuard.class, new RenderImperialGuard());
 		DevCapesUtil.addFileUrl("https://dl.dropboxusercontent.com/u/115533780/capes.txt");
 		TickRegistry.registerTickHandler(new SkyrimTickHandler(), Side.CLIENT);
 		ClientRegistry.bindTileEntitySpecialRenderer(net.skyrimcraft.src.tileentity.TileEntityCommonEndTable.class, new CommonEndTableRenderer());
 		ClientRegistry.bindTileEntitySpecialRenderer(net.skyrimcraft.src.tileentity.TileEntityBench.class, new BenchRenderer());
 		ClientRegistry.bindTileEntitySpecialRenderer(net.skyrimcraft.src.tileentity.TileEntityCrate.class, new CrateRenderer());
 		ClientRegistry.bindTileEntitySpecialRenderer(net.skyrimcraft.src.tileentity.TileEntityBookshelf.class, new BookshelfRenderer());
 	}
 	
 	@Override
 	public void addKeybinds() {
 		KeyBinding[] key = {new KeyBinding("Cross Menu", Keyboard.KEY_M)};
 		boolean[] repeat = {false};
 		KeyBindingRegistry.registerKeyBinding(new CrossMenuKeyHandler(key, repeat));
 	}
 	
 	public static int addArmor(String str)
 	{
 		return RenderingRegistry.addNewArmourRendererPrefix(str);
 	}
 	
	public ModelBiped getArmorModel(int id){
 		switch (id) {
 		case 0:
 			return dragonbornHelmet;
		/*case 1:
			return tutLegs;*/
 		default:
			break;
 		}
		return dragonbornHelmet;
 	}
 }
