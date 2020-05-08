 package me.ibhh.xpShop.Tools;
 
 /**
  *
  * @author ibhh
  */
 public class ToolUtility {
 
     public static Tools getTools() {
         if (Tools.packagesExists(
                 "net.minecraft.server.v1_4_5.EntityPlayer",
                 "net.minecraft.server.v1_4_5.ItemInWorldManager",
                 "net.minecraft.server.v1_4_5.MinecraftServer",
                 "org.bukkit.Bukkit",
                 "org.bukkit.OfflinePlayer",
                 "org.bukkit.craftbukkit.v1_4_5.CraftServer",
                 "org.bukkit.entity.Player")) {
             return new Tools145();
         } else if (Tools.packagesExists(
                 "net.minecraft.server.EntityPlayer",
                 "net.minecraft.server.ItemInWorldManager",
                 "net.minecraft.server.MinecraftServer",
                 "org.bukkit.Bukkit",
                 "org.bukkit.OfflinePlayer",
                 "org.bukkit.craftbukkit.CraftServer",
                 "org.bukkit.entity.Player")) {
             return new Tools132();
         } else if (Tools.packagesExists(
                 "net.minecraft.server.v1_4_6.EntityPlayer",
                 "net.minecraft.server.v1_4_6.PlayerInteractManager",
                 "net.minecraft.server.v1_4_6.MinecraftServer",
                 "org.bukkit.Bukkit",
                 "org.bukkit.OfflinePlayer",
                 "org.bukkit.craftbukkit.v1_4_6.CraftServer",
                 "org.bukkit.entity.Player")) {
             return new Tools146();
         } else if (Tools.packagesExists(
                 "net.minecraft.server.v1_4_R1.EntityPlayer",
                 "net.minecraft.server.v1_4_R1.PlayerInteractManager",
                 "net.minecraft.server.v1_4_R1.MinecraftServer",
                 "org.bukkit.Bukkit",
                 "org.bukkit.OfflinePlayer",
                 "org.bukkit.craftbukkit.v1_4_R1.CraftServer",
                 "org.bukkit.entity.Player")) {
             return new Tools147();
         } else if (Tools.packagesExists(
                 "net.minecraft.server.v1_5_R1.EntityPlayer",
                 "net.minecraft.server.v1_5_R1.PlayerInteractManager",
                 "net.minecraft.server.v1_5_R1.MinecraftServer",
                 "org.bukkit.Bukkit",
                 "org.bukkit.OfflinePlayer",
                 "org.bukkit.craftbukkit.v1_5_R1.CraftServer",
                 "org.bukkit.entity.Player")) {
             return new Tools15();
        }  else if (Tools.packagesExists(
                 "net.minecraft.server.v1_5_R2.EntityPlayer",
                 "net.minecraft.server.v1_5_R2.PlayerInteractManager",
                 "net.minecraft.server.v1_5_R2.MinecraftServer",
                 "org.bukkit.Bukkit",
                 "org.bukkit.OfflinePlayer",
                 "org.bukkit.craftbukkit.v1_5_R2.CraftServer",
                 "org.bukkit.entity.Player")) {
             return new Tools151();
         }
         return null;
     }
}
