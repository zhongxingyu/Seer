 /**
  * TerraCraftTools(SuperSoapTools) - ItemDescriptions.java
  * Copyright (c) 2013 Jeremy Koletar (jjkoletar), <http://jj.koletar.com>
  * Copyright (c) 2013 computerdude5000,<computerdude5000@gmail.com>
  *
  * TerraCraftTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TerraCraftTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TerraCraftTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 
package main.java.terracrafttools.modules;
 
 /*
 import com.koletar.jj.supersoaptools.modules.SuperSoapToolsModule;
 import com.koletar.jj.supersoaptools.modules.SuperSoapToolsCommandRegistrar;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.minecraft.server.v1_4_6.NBTTagList;
 import net.minecraft.server.v1_4_6.NBTTagString;
 import net.minecraft.server.v1_4_6.NBTTagCompound;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.computerdude5000.terracrafttools.TerraCraftTools;
 
 */
 /**
  * @author seemethere
  *//*
 
 
 public class ItemDescriptions extends SuperSoapToolsCommandRegistrar implements SuperSoapToolsModule
 {
     private TerraCraftTools plugin;
 
     @Override
     public void initModule(TerraCraftTools sst)
     {
         plugin = sst;
      super.registerCommand("describe", this);
         plugin.logger.info("[TerraCraftTools][ItemDescriptions] ItemDescriptions Module Enabled!");
     }
 
 
     @Override
     public void deinitModule()
     {
         super.unregisterCommand("describe", this);
         plugin.logger.info("[TerraCraftTools][ItemDescriptions] ItemDescriptions Module Disabled!");
         plugin = null;
     }
 
 
     public boolean callCommand(CommandSender sender, Command command, String label, String[] args)
     {
 
         String module =
                 ChatColor.LIGHT_PURPLE + ("[") + ChatColor.AQUA + "ItemDescriptor" + ChatColor.LIGHT_PURPLE + ("] ");
         Player p = (Player) sender;
 
         if(command.getName().equalsIgnoreCase("describe"))
         {
             if(p.isOp() && p instanceof Player)
             {
                 if(args[0].equalsIgnoreCase("name"))
                     nameCmd(p, args, false, module);
                 else if(args[0].equalsIgnoreCase("lore"))
                     nameCmd(p, args, true, module);
                 else if(args[0].equalsIgnoreCase("clear"))
                     clearCmd(p, args, module);
             }
             else
                 p.sendMessage(module + ChatColor.RED + "Permission Denied, Event has been logged!");
         }
         return true;
     }
 
     private void clearCmd(CommandSender sender, String[] args, String module)
     {
         Player player = (Player) sender;
         if(args.length != 1)
         {
             player.sendMessage(module + ChatColor.YELLOW + "Too many arguments!");
             return;
         }
 
         ItemStack item = player.getItemInHand();
         if(item == null)
         {
             player.sendMessage(module + ChatColor.YELLOW + "No item in hand!");
             return;
         }
 
         net.minecraft.server.v1_4_6.ItemStack nms = ((CraftItemStack) item).getHandle();
 
         if(nms.getTag() != null)
             nms.setTag(null);
 
         player.sendMessage(module + ChatColor.GREEN + "Item name and lore have been removed!");
     }
 
 
     public boolean nameCmd(CommandSender sender, String[] args, boolean lore, String module)
     {
         String a = lore ? "lore" : "name";
         Player player = (Player) sender;
         ItemStack item = player.getItemInHand();
 
         if(args.length <= 1)
         {
             player.sendMessage(module + ChatColor.YELLOW + "Too few arguments!");
             return true;
         }
         else if(item == null)
         {
             player.sendMessage(module + ChatColor.YELLOW + "No item in hand!");
             return true;
         }
 
         String name = "";
 
         for(int i = 1; i < args.length; i++)
         {
             if(name.isEmpty())
                 name = name + args[i];
             else
                 name = name + " " + args[i];
         }
 
         displayFill(item, colorHandler(name), lore ? 1:0);
         player.sendMessage(module + ChatColor.YELLOW + "Item's" + a + " has been set to: " + ChatColor.GREEN + name);
         return true;
     }
 
     //Handles Colors for user input
     public String colorHandler(String s)
     {
         return s.replaceAll("&([0-9a-f])", "\u00A7$1");
     }
 
     public void displayFill(ItemStack item, String input, int loreSwitch)
     {
         NBTTagCompound tag = getTag(item);
 
         if(!tag.hasKey("display"))
             tag.setCompound("display", new NBTTagCompound());
 
         NBTTagCompound display = tag.getCompound("display");
 
         if(loreSwitch == 0)
         {
             display.setString("Name", input);
         }
         else if(loreSwitch == 1)
         {
             NBTTagList lore = new NBTTagList();
             display.set("Lore", lore);
             String [] d = input.split(" ");
             String temp = null;
 
             ArrayList<String> n = new ArrayList<String>();
 
             for(String s : d)
             {
                 if(temp == null)
                     temp = s;
                 else
                 {
                     int stringLen = ChatColor.stripColor(s).length();
                     if(stringLen >= 24)
                     {
                         n.add(temp);
                         temp = null;
                         n.add(s);
                     }
                     else
                     {
                         int nLen = stringLen + ChatColor.stripColor(temp).length();
 
                         if(nLen >= 24)
                         {
                             n.add(temp);
                             temp = s;
                         }
                         else
                             temp = temp + " " + s;
                     }
                 }
             }
             if(temp != null)
                 n.add(temp);
             String s;
             for(Iterator<String> localIterator = n.iterator(); localIterator.hasNext(); lore.add(new NBTTagString("", s)))
                 s = (String) localIterator.next();
         }
     }
 
     //Checks if an item already has a tag
     public NBTTagCompound getTag(ItemStack item)
     {
         net.minecraft.server.v1_4_6.ItemStack nms = ((CraftItemStack) item).getHandle();
         if (nms.getTag() == null)
             nms.setTag(new NBTTagCompound());
         return nms.getTag();
     }
 }*/
