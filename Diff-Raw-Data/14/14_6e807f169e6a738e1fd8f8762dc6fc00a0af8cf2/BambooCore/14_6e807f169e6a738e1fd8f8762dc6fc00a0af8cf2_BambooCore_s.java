 package ruby.bamboo;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockDispenser;
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.multiplayer.NetClientHandler;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityList;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemSlab;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.StatCollector;
 import net.minecraft.util.StringTranslate;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.biome.BiomeGenForest;
 import net.minecraft.world.biome.BiomeGenHills;
 import net.minecraft.world.biome.WorldChunkManager;
 
 import org.lwjgl.opengl.GL11;
 
 import com.google.common.base.Function;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.FMLFileResourcePack;
 import cpw.mods.fml.client.FMLFolderResourcePack;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.FMLModContainer;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartedEvent;
 import cpw.mods.fml.common.modloader.ModLoaderHelper;
 import cpw.mods.fml.common.network.EntitySpawnPacket;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 
 import static ruby.bamboo.BambooInit.*;
 import ruby.bamboo.*;
 import ruby.bamboo.block.*;
 import ruby.bamboo.dispenser.DispenserBehaviorBambooSpear;
 import ruby.bamboo.dispenser.DispenserBehaviorDirtySnowball;
 import ruby.bamboo.dispenser.DispenserBehaviorFireCracker;
 import ruby.bamboo.entity.*;
 import ruby.bamboo.item.*;
 import ruby.bamboo.render.*;
 import ruby.bamboo.tileentity.*;
 
 @Mod(modid = "BambooMod" , name = "BambooMod", version = "Minecraft1.6.2 ver2.6.3")
 @NetworkMod(channels = {"B_Entity", "bamboo", "bamboo2"},
             packetHandler = NetworkHandler.class,
             connectionHandler = NetworkHandler.class)
 public class BambooCore
 {
    public static final String MODID="BambooMod";
     private final boolean DEBUGMODE = false;
     @SidedProxy(serverSide = "ruby.bamboo.CommonProxy", clientSide = "ruby.bamboo.ClientProxy")
     public static CommonProxy proxy;
 
     @Instance("BambooMod")
     public static BambooCore instance;
 
     private static Config conf = new Config();
     public static CreativeTabs tabBamboo;
 
     public static Config getConf()
     {
         return conf;
     }
     @Mod.EventHandler
     public void preLoad(FMLPreInitializationEvent e)
     {
         tabBamboo = new CreativeTabs("Bamboo")
         {
             @Override
             public int getTabIconItemIndex()
             {
                 return takenokoIID;
             }
             @Override
             public String getTranslatedTabLabel()
             {
                 return StatCollector.translateToLocal(this.getTabLabel());
             }
         };
         proxy.preInit();
     }
 
     @Mod.EventHandler
     public void load(FMLInitializationEvent e)
     {
         proxy.init();
         proxy.registerTESTileEntity();
         registDispencer();
 
         // debug
         if (DEBUGMODE)
         {
             System.out.println("DEBUG MODE Enable");
         }
     }
 
     @Mod.EventHandler
     public void serverStart(FMLServerStartedEvent event)
     {
         getConf().serverInit();
         ManekiHandler.instance.clearManekiList();
     }
 
     @Mod.EventHandler
     public void postInit(FMLPostInitializationEvent event)
     {
         NetworkRegistry.instance().registerGuiHandler(BambooCore.getInstance(), new GuiHandler());
     }
 
     private void registDispencer()
     {
         BlockDispenser.dispenseBehaviorRegistry.putObject(snowBallIID, new DispenserBehaviorDirtySnowball());
         BlockDispenser.dispenseBehaviorRegistry.putObject(firecrackerIID, new DispenserBehaviorFireCracker());
         BlockDispenser.dispenseBehaviorRegistry.putObject(bambooSpearIID, new DispenserBehaviorBambooSpear());
     }
 
 
     public static BambooCore getInstance()
     {
         return instance;
     }
 }
