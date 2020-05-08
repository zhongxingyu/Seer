 package net.Harmonion.util;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.Harmonion.block.ModBlocks;
 import net.Harmonion.client.ClientProxy;
 import net.Harmonion.command.CommandHMCV;
 import net.Harmonion.entity.EntityLib;
 import net.Harmonion.entity.passive.EntityHarmonionWolf;
 import net.Harmonion.power.ContainerBatteryBox;
 import net.Harmonion.power.CoreLib;
 import net.Harmonion.power.GuiBatteryBox;
 import net.Harmonion.power.IHandlePackets;
 import net.Harmonion.power.MicroPlacementWire;
 import net.Harmonion.power.Packet211TileDesc;
 import net.Harmonion.power.TileBatteryBox;
 import net.Harmonion.power.TileBluewire;
 import net.Harmonion.server.Harmonion;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.command.CommandHandler;
 import net.minecraft.command.ICommand;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.EnumRarity;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.network.NetServerHandler;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.ThreadMinecraftServer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldServer;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
 /**
  * CommonProxy
  * 
  * The common proxy class between client and server. Client proxy extends this
  * for further client specific functionality
  * 
  * @author alexbegt
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 public class CommonProxy implements IGuiHandler {
 	
 	public void registerTickHander() {
 	}
 	
 	public int getByBlockModelId()
     {
         return 0;
     }
 	
 	public int getNextAvailableRenderId()
     {
         return 0;
     }
 	
 	public void addRecipe(ItemStack var1, Object[] var2)
     {
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(var1, var2));
     }
 	
 	public boolean isOp(EntityPlayer var1)
     {
         MinecraftServer var2 = FMLCommonHandler.instance().getMinecraftServerInstance();
         return var2.getConfigurationManager().areCommandsAllowed(var1.username);
     }
 	
 	public void processPacket211(Packet211TileDesc var1, NetHandler var2)
     {
         if (var2 instanceof NetServerHandler)
         {
             NetServerHandler var3 = (NetServerHandler)var2;
             EntityPlayerMP var4 = var3.getPlayer();
             World var5 = var4.worldObj;
 
             if (var5.blockExists(var1.xCoord, var1.yCoord, var1.zCoord))
             {
                 TileEntity var6 = var5.getBlockTileEntity(var1.xCoord, var1.yCoord, var1.zCoord);
 
                 if (var6 instanceof IHandlePackets)
                 {
                     ((IHandlePackets)var6).handlePacket(var1);
                     return;
                 }
             }
         }
     }
 	
 	public String getCurrentLanguage()
 	{
 	       return null;
 	}
 	
 	public void addEntityBiodustFX(World var1, double var2, double var4, double var6, float var8, float var9, float var10) {}
 	
 	public void bindTexture(String var1) {}
 	
 	public String getMinecraftVersion() {
 		return Loader.instance().getMinecraftModContainer().getVersion();
 	}
 	
 	public boolean needsTagCompoundSynched(Item var1)
     {
         return var1.getShareTag();
     }
 	
 	/* LOCALIZATION */
 	public void addName(Object obj, String s) {}
 	public void addLocalization(String s1, String string) {}
 	public String getItemDisplayName(ItemStack newStack) { return ""; }
 
     public void registerSoundHandler() {}
 
     public void initCustomRarityTypes() {}
 
     public EnumRarity getCustomRarityType(String customRarity) {
         return null;
     }
     
     public void initSounds() {}
     
     public void initEntitys() {
     	EntityRegistry.registerModEntity(EntityHarmonionWolf.class, "HMCW",
  				ItemIds.Harmonion_Wolf, Harmonion.instance, 80, 3, true);
  		EntityRegistry.addSpawn(EntityHarmonionWolf.class, 5, 5, 5,
  				EnumCreatureType.creature, BiomeGenBase.plains);
  		EntityLib.registerEntityEgg(EntityHarmonionWolf.class, 12698049, 12698049);
     }
     
     public void initEntitysClient() {}
     
     public void initRenderingAndTextures() {}
     
     public void initTileEntities() {
     	
     	
     	
     }
     
     public int getBlockTexture(Block var1, IBlockAccess var2, int var3, int var4, int var5, int var6)
     {
         return 0;
     }
     
     public Object getClientGuiElement(int var1, EntityPlayer var2, World var3, int var4, int var5, int var6)
     {
         switch (var1)
         {
             case 8:
                 return new GuiBatteryBox(var2.inventory, (TileBatteryBox)CoreLib.getGuiTileEntity(var3, var4, var5, var6, TileBatteryBox.class));
                 
             default:
                 return null;
         }
     }
 
     public Object getServerGuiElement(int var1, EntityPlayer var2, World var3, int var4, int var5, int var6)
     {
         switch (var1)
         {
             case 8:
                 return new ContainerBatteryBox(var2.inventory, (TileBatteryBox)CoreLib.getTileEntity(var3, var4, var5, var6, TileBatteryBox.class));
 
             default:
                 return null;
         }
     }
     
     public boolean isSimulating()
     {
         return Thread.currentThread() instanceof ThreadMinecraftServer;
     }
     
     public boolean isSimulating(World var1)
     {
         return true;
     }
     
     public void serverStarting(MinecraftServer var1)
     {
         CommandHandler var2 = (CommandHandler)var1.getCommandManager();
         
         ICommand[] var5 = this.getConsoleCommands();
         {
         	ICommand[] var6 = var5;
         	int var7 = var5.length;
         	for (int var8 = 0; var8 < var7; ++var8)
         	{
         		ICommand var9 = var6[var8];
         		var2.registerCommand(var9);
         	}
         }
     }
     
     public Minecraft getClientInstance()
     {
         return FMLClientHandler.instance().getClient();
     }
     
     public ICommand[] getConsoleCommands()
     {
         return new ICommand[] {new CommandHMCV()};
     }
     
     public World getClientWorld()
     {
         return null;
     }
     public int addArmor(String name) {
         return 0;
       }
 
 	public void initPower() {
 		
         MicroPlacementWire var0 = new MicroPlacementWire();
         ModBlocks.blockMicro.registerPlacement(1, var0);
         ModBlocks.blockMicro.registerPlacement(2, var0);
         ModBlocks.blockMicro.registerPlacement(3, var0);
         ModBlocks.blockMicro.registerPlacement(5, var0);
        GameRegistry.registerTileEntity(TileBluewire.class, "HarmonionBluewire");
         ModBlocks.blockMicro.addTileEntityMapping(1, TileBluewire.class);
         ModBlocks.blockMicro.addTileEntityMapping(2, TileBluewire.class);
         ModBlocks.blockMicro.addTileEntityMapping(3, TileBluewire.class);
         ModBlocks.blockMicro.addTileEntityMapping(4, TileBluewire.class);
     	ModBlocks.blockMicro.addTileEntityMapping(5, TileBluewire.class);
 		
 	}
 }
