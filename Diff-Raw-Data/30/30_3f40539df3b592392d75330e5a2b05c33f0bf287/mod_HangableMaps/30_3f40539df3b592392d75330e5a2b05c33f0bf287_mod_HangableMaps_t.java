 package net.minecraft.server;
 
 import forge.MinecraftForge;
 import forge.NetworkMod;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.List;
 import net.minecraft.server.MinecraftServer;
 
 public class mod_HangableMaps extends NetworkMod
 {
     public static boolean shouldCenterMapsOnPlayer = false;
     public static int hangableMapNetID = 165;
     public static mod_HangableMaps instance;
     private int nextMapUpdate;
     private int mapUpdateSegment;
     public static final String channelName = "mod_HangMap";
     public static final int packetTypeMapID = 1;
     public static final int packetTypeMapData = 2;
 
     public mod_HangableMaps()
     {
         instance = this;
         ModLoader.registerEntityID(EntityHangableMap.class, "entityhangablemap", ModLoader.getUniqueEntityId());
         MinecraftForge.registerEntity(EntityHangableMap.class, this, hangableMapNetID, 160, 5, false);
         List var1 = CraftingManager.getInstance().getRecipies();
         ItemStack var2;
         int var3;
 
         for (var3 = 0; var3 < var1.size(); ++var3)
         {
             CraftingRecipe var4 = (CraftingRecipe)var1.get(var3);
             var2 = var4.b();
 
             if (var2.id == Item.MAP.id)
             {
                 var1.remove(var3);
                 break;
             }
         }
 
         ModLoader.addRecipe(new ItemStack(Item.MAP, 1, -1), new Object[] {"###", "#X#", "###", '#', Item.PAPER, 'X', Item.COMPASS});
 
         for (var3 = 1; var3 < 65536; ++var3)
         {
             var2 = new ItemStack(Item.MAP, 1, var3);
             ItemStack var5 = new ItemStack(Item.INK_SACK, 1, 0);
             ModLoader.addRecipe(new ItemStack(Item.MAP, 2, var3), new Object[] {"IMC", "###", "###", '#', Item.PAPER, 'C', Item.COMPASS, 'I', var5, 'M', var2});
         }
 
         ModLoader.setInGameHook(this, true, true);
         this.nextMapUpdate = 0;
         this.mapUpdateSegment = 0;
     }
 
     public boolean onTickInGame(MinecraftServer var1)
     {
         ++this.nextMapUpdate;
 
         if (this.nextMapUpdate > 3)
         {
             this.nextMapUpdate = 0;
 
             for (int var2 = 0; var2 < EntityHangableMap.trackedHangableMaps.size(); ++var2)
             {
                 TrackedHangableMapEntry var3 = (TrackedHangableMapEntry)EntityHangableMap.trackedHangableMaps.get(var2);
                 ItemStack var4 = new ItemStack(Item.MAP, 1, var3.mapID);
                 WorldMap var5 = Item.MAP.getSavedMap(var4, var3.world);
 
                 if (var5 != null)
                 {
                     this.sendMapData(var5, var3.mapID, this.mapUpdateSegment);
                     ++this.mapUpdateSegment;
 
                     if (this.mapUpdateSegment >= 128)
                     {
                         this.mapUpdateSegment = 0;
                     }
                 }
             }
         }
 
         return true;
     }
 
     public void sendMapData(WorldMap var1, int var2, int var3)
     {
         int[] var4 = new int[136];
         var4[0] = var2;
         var4[1] = var3;
         int var5 = 2;
 
         for (int var6 = var3 * 128; var6 < var3 * 128 + 128; ++var6)
         {
             var4[var5] = var1.colors[var6];
             ++var5;
         }
 
         short var11 = 132;
 
         if (var3 < var1.decorations.size() && var1.decorations.get(var3) != null)
         {
             var4[131] = 1;
             WorldMapDecoration var7 = (WorldMapDecoration)var1.decorations.get(var3);
            var4[var11] = var7.type;
             var4[var11 + 1] = var7.locX;
             var4[var11 + 2] = var7.locY;
             var4[var11 + 3] = var7.rotation;
         }
         else
         {
             var4[131] = 0;
         }
 
         ByteArrayOutputStream var12 = new ByteArrayOutputStream();
         DataOutputStream var8 = new DataOutputStream(var12);
 
         try
         {
             var8.writeByte(2);
 
             for (int var9 = 0; var9 < var4.length; ++var9)
             {
                 var8.writeInt(var4[var9]);
             }
         }
         catch (IOException var10)
         {
             var10.printStackTrace();
             return;
         }
 
         Packet250CustomPayload var13 = new Packet250CustomPayload();
         var13.tag = "mod_HangMap";
         var13.data = var12.toByteArray();
         var13.length = var13.data.length;
         ModLoader.getMinecraftServerInstance().serverConfigurationManager.sendAll(var13);
     }
 
     public void sendMapIDPacket(EntityHangableMap var1)
     {
         ByteArrayOutputStream var2 = new ByteArrayOutputStream();
         DataOutputStream var3 = new DataOutputStream(var2);
 
         try
         {
             var3.writeByte(1);
             var3.writeInt(var1.id);
             var3.writeInt(var1.getMapId());
             var3.writeFloat((float)var1.locX);
             var3.writeFloat((float)var1.locY);
             var3.writeFloat((float)var1.locZ);
             var3.writeFloat(var1.yaw);
             var3.writeFloat(var1.pitch);
         }
         catch (IOException var5)
         {
             var5.printStackTrace();
             return;
         }
 
         Packet250CustomPayload var4 = new Packet250CustomPayload();
         var4.tag = "mod_HangMap";
         var4.data = var2.toByteArray();
         var4.length = var4.data.length;
         ModLoader.getMinecraftServerInstance().serverConfigurationManager.sendAll(var4);
     }
 
     public String getVersion()
     {
         return "1.1.1 for 1.2.5";
     }
 
     public void load() {}
 
     public boolean clientSideRequired()
     {
         return true;
     }
 
     public boolean serverSideRequired()
     {
         return false;
     }
 }
