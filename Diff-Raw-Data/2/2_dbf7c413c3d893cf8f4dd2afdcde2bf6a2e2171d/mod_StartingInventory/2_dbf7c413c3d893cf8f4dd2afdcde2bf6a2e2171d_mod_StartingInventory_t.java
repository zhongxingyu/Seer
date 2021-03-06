 package net.minecraft.src;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.multiplayer.NetClientHandler;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.world.World;
 import bspkrs.util.ModVersionChecker;
 
 public class mod_StartingInventory extends BaseMod
 {
     private static final String INV           = "inventory";
     private static final String CHEST         = "chest";
     
     @MLProp(info = "Use \"inventory\" to add items to your inventory on starting a world, use \"chest\" to continue using that lame placed chest nonsense.")
     public static String        addItemsTo    = INV;
     @MLProp(info = "Items will be added to the vanilla bonus chest when set to true (meaning you must set bonus chest to ON). If set to false, a separate chest will be placed when addItemsTo=chest")
     public static boolean       useBonusChest = false;
     @MLProp(info = "The length of time in game time ticks (1/20th of a second) that items have to spawn in a fresh world.  If you are having trouble with items not being given, try setting this a little higher\n\n**ONLY EDIT WHAT IS BELOW THIS**")
     public static int           tickWindow    = 300;
     
     boolean                     canGiveItems;
     String                      fileName;
     String                      configPath;
     File                        mcdir;
     File                        file;
     private Scanner             scan;
     private final List          list;
     private TileEntityChest     chest;
     private final String[]      defaultItems  = { "274, 1", "273, 1", "272, 1", "275, 1", "260, 16", "50, 16" };
     private Minecraft           mc;
     private EntityPlayer        player;
     
     private ModVersionChecker   versionChecker;
     private boolean             allowUpdateCheck;
     private final String        versionURL    = "http://bspk.rs/Minecraft/1.5.1/startingInventory.version";
     private final String        mcfTopic      = "http://www.minecraftforum.net/topic/1009577-";
     
     public mod_StartingInventory()
     {
         mc = ModLoader.getMinecraftInstance();
         mcdir = Minecraft.getMinecraftDir();
         fileName = "startingInventory.txt";
         configPath = "/config/StartingInventory/";
         file = new File(mcdir, configPath + fileName);
         list = new ArrayList();
         list.clear();
         if (!file.exists())
             createFile();
         else
             try
             {
                 scan = new Scanner(file);
             }
             catch (Throwable e)
             {
                 e.printStackTrace();
             }
         readItems();
         
         allowUpdateCheck = mod_bspkrsCore.allowUpdateCheck;
         if (allowUpdateCheck)
            versionChecker = new ModVersionChecker(getName(), getVersion(), versionURL, mcfTopic);
     }
     
     @Override
     public String getName()
     {
         return "StartingInventory";
     }
     
     @Override
     public String getVersion()
     {
         return "ML 1.5.1.r01";
     }
     
     @Override
     public String getPriorities()
     {
         return "required-after:mod_bspkrsCore";
     }
     
     @Override
     public void load()
     {
         if (allowUpdateCheck && versionChecker != null)
             versionChecker.checkVersionWithLogging();
     }
     
     @Override
     public boolean onTickInGame(float f, Minecraft mc)
     {
         player = mc.thePlayer;
         
         if (canGiveItems && mc.isIntegratedServerRunning() && player != null && isPlayerNewToWorld(player) && createPlayerFile(player) && isFreshWorld(mc))
             canGiveItems = !addItems(mc.getIntegratedServer().worldServerForDimension(player.dimension));
         
         if (allowUpdateCheck && versionChecker != null)
         {
             if (!versionChecker.isCurrentVersion())
                 for (String msg : versionChecker.getInGameMessage())
                     mc.thePlayer.addChatMessage(msg);
             allowUpdateCheck = false;
         }
         
         if (!(canGiveItems && mc.isIntegratedServerRunning() && isFreshWorld(mc)))
         {
             chest = null;
             player = null;
         }
         
         return canGiveItems && mc.isIntegratedServerRunning() && isFreshWorld(mc);
     }
     
     @Override
     public void clientConnect(NetClientHandler nch)
     {
         ModLoader.setInGameHook(this, true, true);
         canGiveItems = true;
     }
     
     public boolean isPlayerNewToWorld(EntityPlayer player)
     {
         File dir = new File(mc.getMinecraftDir(), "/saves/" + mc.theWorld.getSaveHandler().getSaveDirectoryName() + "/StartingInv");
         return !dir.exists() || !(new File(dir, player.username + ".si")).exists();
     }
     
     public boolean createPlayerFile(EntityPlayer player)
     {
         File pFile;
         File dir = new File(mc.getMinecraftDir(), "/saves/" +
                 mc.getIntegratedServer().worldServerForDimension(player.dimension).getSaveHandler().getSaveDirectoryName() +
                 "/StartingInv");
         if (!dir.exists() && dir.mkdir())
         {
             pFile = new File(dir, player.username + ".si");
         }
         else
         {
             pFile = new File(dir, player.username + ".si");
         }
         try
         {
             pFile.createNewFile();
             PrintWriter out = new PrintWriter(new FileWriter(pFile));
             
             out.println("I was here!");
             
             out.close();
             return true;
             
         }
         catch (Exception exception)
         {
             return false;
         }
     }
     
     public TileEntityChest getChest(World world)
     {
         int x = world.getWorldInfo().getSpawnX();
         int z = world.getWorldInfo().getSpawnZ();
         
         if (useBonusChest)
         {
             for (int i = 10; i >= -10; i--)
                 for (int j = 10; j >= -10; j--)
                 {
                     int y = world.getTopSolidOrLiquidBlock(x + i, z + j) - 1;
                     if (world.getBlockId(x + i, y, z + j) == Block.chest.blockID)
                     {
                         ModLoader.getLogger().log(Level.INFO, "Found bonus chest at " + (x + i) + ", " + y + ", " + (z + j));
                         return (TileEntityChest) world.getBlockTileEntity(x + i, y, z + j);
                     }
                 }
             ModLoader.getLogger().log(Level.WARNING, "Unable to find bonus chest.");
         }
         else
         {
             Random random = new Random();
             for (int i = 0; i < 10; i++)
             {
                 x = world.getWorldInfo().getSpawnX() + random.nextInt(6) - random.nextInt(6);
                 z = world.getWorldInfo().getSpawnZ() + random.nextInt(6) - random.nextInt(6);
                 int y = world.getTopSolidOrLiquidBlock(x, z);
                 if (world.isAirBlock(x, y, z) && world.doesBlockHaveSolidTopSurface(x, y - 1, z))
                 {
                     for (int a = -1; a <= -1; a++)
                         for (int b = -1; b <= 1; b++)
                         {
                             world.setBlock(x + a, y, z + b, 0, 0, 3);
                             world.setBlock(x + a, y + 1, z + b, 0, 0, 3);
                         }
                     world.setBlock(x, y, z, Block.chest.blockID, 0, 3);
                     ModLoader.getLogger().log(Level.INFO, "Chest placed at " + x + ", " + y + ", " + z);
                     return (TileEntityChest) world.getBlockTileEntity(x, y, z);
                 }
             }
         }
         
         return null;
         
     }
     
     public boolean isFreshWorld(Minecraft mc)
     {
         try
         {
             return (int) mc.theWorld.getWorldInfo().getWorldTime() < tickWindow;
         }
         catch (Exception e)
         {
             return false;
         }
     }
     
     public boolean addItems(World world)
     {
         if (this.addItemsTo.equals(CHEST))
         {
             chest = getChest(world);
             if (chest != null)
             {
                 for (int i = 0; i < chest.getSizeInventory(); i++)
                     chest.setInventorySlotContents(i, null);
                 
                 for (int i = 0; i < Math.min(chest.getSizeInventory(), list.size()); i++)
                 {
                     addItemToChest(chest, i, (String) list.get(i));
                 }
                 return true;
             }
             return false;
         }
         else
         {
             for (int i = 0; i < Math.min(player.inventory.getSizeInventory(), list.size()); i++)
             {
                 addItemToInv((String) list.get(i));
             }
             return true;
         }
     }
     
     private int[] parseLine(String entry)
     {
         int[] r = { 0, 1, 0 };
         int d1 = entry.indexOf(',');
         int d2 = entry.indexOf(',', d1 + 1);
         
         if (d1 != -1)
         {
             r[0] = parseInt(entry.substring(0, d1));
             if (d2 != -1)
             {
                 r[1] = parseInt(entry.substring(d1 + 1, d2));
                 r[2] = parseInt(entry.substring(d2 + 1));
             }
             else
                 r[1] = parseInt(entry.substring(d1 + 1));
         }
         else
             r[0] = parseInt(entry);
         
         return r;
     }
     
     private void addItemToInv(String entry)
     {
         int[] item = parseLine(entry);
         if (Item.itemsList[item[0]] != null && mc.isIntegratedServerRunning())
             mc.getIntegratedServer().worldServerForDimension(player.dimension).getPlayerEntityByName(player.username).inventory.addItemStackToInventory(new ItemStack(item[0], item[1], item[2]));
     }
     
     private void addItemToChest(TileEntityChest chest, int slot, String entry)
     {
         int[] item = parseLine(entry);
         if (Item.itemsList[item[0]] != null)
             chest.setInventorySlotContents(slot, new ItemStack(item[0], item[1], item[2]));
     }
     
     public void readItems()
     {
         if (scan != null)
         {
             for (; scan.hasNextLine(); list.add(scan.nextLine()))
             {}
         }
         scan.close();
     }
     
     public int parseInt(String s)
     {
         try
         {
             return Integer.parseInt(s.trim());
         }
         catch (NumberFormatException numberformatexception)
         {
             return 0;
         }
     }
     
     public void createFile()
     {
         File dir = new File(mcdir, configPath);
         if (!dir.exists() && dir.mkdir())
         {
             file = new File(dir, fileName);
         }
         else
         {
             file = new File(dir, fileName);
         }
         try
         {
             file.createNewFile();
             PrintWriter out = new PrintWriter(new FileWriter(file));
             
             for (String s : defaultItems)
             {
                 out.println(s);
             }
             
             out.close();
             
             scan = new Scanner(file);
             
         }
         catch (Exception exception)
         {}
     }
 }
