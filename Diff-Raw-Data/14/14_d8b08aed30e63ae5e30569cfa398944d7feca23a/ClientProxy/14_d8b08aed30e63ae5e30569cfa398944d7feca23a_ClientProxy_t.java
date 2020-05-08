 package hunternif.mc.dota2items;
 
import java.util.logging.Level;

 import hunternif.mc.dota2items.client.gui.FontRendererContourShadow;
 import hunternif.mc.dota2items.client.gui.FontRendererWithIcons;
 import hunternif.mc.dota2items.client.gui.GuiDotaStats;
 import hunternif.mc.dota2items.client.gui.GuiGold;
 import hunternif.mc.dota2items.client.gui.GuiHealthAndMana;
 import hunternif.mc.dota2items.client.gui.GuiManaBar;
 import hunternif.mc.dota2items.client.gui.IconInText;
 import hunternif.mc.dota2items.client.render.CooldownItemRenderer;
 import hunternif.mc.dota2items.client.render.RenderShopkeeper;
 import hunternif.mc.dota2items.core.ClientTickHandler;
 import hunternif.mc.dota2items.core.ServerTickHandler;
 import hunternif.mc.dota2items.entity.EntityShopkeeper;
 import hunternif.mc.dota2items.item.CooldownItem;
 import hunternif.mc.dota2items.util.DescriptionBuilder;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.resources.ReloadableResourceManager;
 import net.minecraft.client.resources.SimpleReloadableResourceManager;
 import net.minecraft.client.resources.data.MetadataSerializer;
 import net.minecraft.item.Item;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.client.event.sound.SoundLoadEvent;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class ClientProxy extends CommonProxy {
 	public static final CooldownItemRenderer cooldownItemRenderer = new CooldownItemRenderer();
 	
 	public static final ReloadableResourceManager resourceManager = new SimpleReloadableResourceManager(new MetadataSerializer());
 	
 	public static final FontRendererWithIcons fontRWithIcons = new FontRendererWithIcons(
 			Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);
 	
 	public static final FontRendererContourShadow fontRContourShadow = new FontRendererContourShadow(
 			Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);
 	
 	public static IconInText ICON_GOLD = new IconInText("$gold$", 12, 12, Dota2Items.ID+":textures/gui/gold_coins.png", -1, -3, 2);
 	public static IconInText ICON_COOLDOWN = new IconInText("$cd$", 7, 7, Dota2Items.ID+":textures/gui/cooldown.png", 0, 0, 3);
 	public static IconInText ICON_MANACOST = new IconInText("$manacost$", 7, 7, Dota2Items.ID+":textures/gui/manacost.png", 0, 0, 3);
 	{
 		resourceManager.func_110542_a(fontRWithIcons);
 		resourceManager.func_110542_a(fontRContourShadow);
 		fontRWithIcons.registerIcon(ICON_GOLD);
 		fontRWithIcons.registerIcon(ICON_COOLDOWN);
 		fontRWithIcons.registerIcon(ICON_MANACOST);
 	}
 	public static GuiGold guiGold = new GuiGold();
 	public static GuiManaBar guiManaBar = new GuiManaBar(Minecraft.getMinecraft());
 	public static GuiHealthAndMana guiHpAndMana = new GuiHealthAndMana(Minecraft.getMinecraft());
 	public static GuiDotaStats guiStats = new GuiDotaStats();
 	
 	@Override
 	public void registerRenderers() {
 		DescriptionBuilder.build();
 		
 		MinecraftForge.EVENT_BUS.register(cooldownItemRenderer);
 		MinecraftForge.EVENT_BUS.register(guiManaBar);
 		MinecraftForge.EVENT_BUS.register(guiHpAndMana);
 		
 		for (Item item : Dota2Items.itemList) {
 			if (item instanceof CooldownItem) {
 				MinecraftForgeClient.registerItemRenderer(item.itemID, cooldownItemRenderer);
 			}
 		}
 		
 		RenderingRegistry.registerEntityRenderingHandler(EntityShopkeeper.class, new RenderShopkeeper());
 	}
 	
 	@Override
 	public void registerSounds() {
 		MinecraftForge.EVENT_BUS.register(this);
 	}
 	
 	@Override
 	public void registerTickHandlers() {
 		TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
 		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
 	}
 	
 	@ForgeSubscribe
 	public void onSound(SoundLoadEvent event) {
         try {
         	for (Sound sound : Sound.values()) {
         		event.manager.soundPoolSounds.addSound(sound.getName()+".wav");
         	}
         }
         catch (Exception e) {
        	FMLLog.log(Dota2Items.ID, Level.WARNING, "Failed to register one or more sounds.");
         }
     }
 }
