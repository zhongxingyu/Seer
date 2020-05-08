 /*
 SimpleLogic: A Bukkit plugin that adds sign based logic gates to Minecraft
 Copyright (C) 2012  ffrogman
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.ffrogman.simplelogic;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import java.util.ArrayList;
 import java.util.Scanner;
 import org.bukkit.block.BlockState;
 import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
 
 public class SimpleLogic extends JavaPlugin implements Listener {
 
     ArrayList<LogicSign> signList = new ArrayList();
 
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
         File signData = new File(getDataFolder(), "signs.txt");
         try {
             loadSigns(signData);
         } catch (IOException e) {
             System.out.println("Could not create file. SimpleLogic may not work.");
         }
         checkAllSigns();
     }
 
     public static boolean logicOperator(boolean A, boolean B, String s) {
         if (s.equals("and")) {
             return A && B;
         } else if (s.equals("or")) {
             return A || B;
         } else if (s.equals("xor")) {
             return (A ^ B);
         } else if (s.equals("nor")) {
             return (!A && !B);
         } else if (s.equals("xnor")) {
             return (!(A ^ B));
         } else if (s.equals("nand")) {
             return (!(A && B));
         } else {
             return false;
         }
     }
 
     public void removeLine(String s) {
         try {
             File inFile = new File(getDataFolder() + "\\signs.txt");
             File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
             BufferedReader br = new BufferedReader(new FileReader(inFile));
             PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
             String line = null;
             while ((line = br.readLine()) != null) {
 
                 if (!line.trim().contains(s)) {
 
                     pw.println(line);
                     pw.flush();
                 }
             }
             pw.close();
             br.close();
             if (!inFile.delete()) {
                 System.out.println("Could not delete file");
                 return;
             }
             if (!tempFile.renameTo(inFile)) {
                 System.out.println("Could not rename file");
             }
         } catch (FileNotFoundException ex) {
         } catch (IOException ex) {
         }
     }
 
     public void writeSign(LogicSign L) {
         try {
             FileWriter out = new FileWriter(getDataFolder() + "\\signs.txt", true);
             BufferedWriter writer = new BufferedWriter(out);
             writer.write(L.toString());
             writer.newLine();
             writer.close();
         } catch (IOException e) {
             System.out.println("Error writing to file");
         }
     }
 
     public void loadSigns(File file) throws IOException {
         //world:x,y,z
         getDataFolder().mkdir();
         file.createNewFile();
         String curLine = "foo";
         Scanner rfile = null;
         try {
             rfile = new Scanner(file);
         } catch (FileNotFoundException ex) {
         }
         while (curLine != null && rfile.hasNext()) {
             curLine = rfile.nextLine();
             if (curLine != null) {
                 LogicSign L = new LogicSign();
                 int count = 0;
                 int index = 0;
                 for (int i = 0; i < curLine.length(); i++) {
                     if (curLine.substring(i, i + 1).equals(":")) {
                         L.world = getServer().getWorld(curLine.substring(0, i));
                         count = i + 1;
                         break;
                     }
                 }
                 for (int i = count; i < curLine.length(); i++) {
                     if (curLine.substring(i, i + 1).equals(",")) {
                         L.coords[index] = Integer.parseInt(curLine.substring(count, i));
                         index++;
                         count = i + 1;
                         if (index == 2) {
                             L.coords[index] = Integer.parseInt(curLine.substring(count, curLine.length()));
                             break;
                         }
                     }
                 }
                 L.findBlocks(null);
                 L.setOperator();
                 signList.add(L);
                 checkPower(L);
             } else {
                 break;
             }
         }
     }
 
     public Sign getSign(Block b) {
         if (b.getType() == Material.WALL_SIGN) {
             BlockState state = b.getState();
             if (state instanceof Sign) {
                 return (Sign) state;
             }
         }
         return null;
     }
 
     public void checkAllSigns() {
         for (int i = 0; i < signList.size(); i++) {
             LogicSign L = signList.get(i);
             if (!L.isSign()) {
                 removeLine(L.toString());
                 signList.remove(i);
                 i--;
             }
         }
     }
 
     public void checkPower(LogicSign L) {
         Sign sign = getSign(L.getBlock());
         if (!L.canTorch()) {
             sign.setLine(0, ChatColor.DARK_RED + "[Logic]");
             sign.update();
             return;
         }
         sign.setLine(0, ChatColor.DARK_BLUE + "[Logic]");
         sign.update();
         if (logicOperator(L.inputA.isBlockPowered(), L.inputB.isBlockPowered(), L.operator)) {
             if (L.output.getTypeId() != 76) {
                 L.output.setTypeIdAndData(76, (byte) 0x5, true);
             }
         } else {
             if (L.output.getTypeId() != 75) {
                 L.output.setTypeIdAndData(75, (byte) 0x5, true);
             }
         }
     }
 
    @EventHandler(priority = EventPriority.HIGHEST)
     public void blockBreak(BlockBreakEvent event) {
         Block b = event.getBlock();
        if (b.getTypeId() == 75 || b.getTypeId() == 76) { //Redstone torch (on/off)
             for (int i = 0; i < signList.size(); i++) {
                 LogicSign L = signList.get(i);
                 if (L.output.getX() == b.getX() && L.output.getY() == b.getY() && L.output.getZ() == b.getZ()) {
                     event.setCancelled(true);
                 }
             }
        } else if (b.getTypeId() == 68) { //sign
            for (int i = 0; i < signList.size(); i++) {
                LogicSign L = signList.get(i);
                if ((b.getX() == L.coords[0]) && (b.getY() == L.coords[1]) && (b.getZ() == L.coords[2]) && (b.getWorld() == L.world)) {
                    removeLine(signList.get(i).toString());
                    signList.remove(i);
                    i--;
                }
            }
        } else {
            for (LogicSign ls : signList) {
                org.bukkit.material.Sign s = (org.bukkit.material.Sign) getSign(ls.getBlock()).getData();
                Block aBlock = ls.getBlock().getRelative(s.getAttachedFace());
                if ((aBlock.getLocation().getX() == b.getLocation().getBlockX()) && (aBlock.getLocation().getY() == b.getLocation().getBlockY()) && (aBlock.getLocation().getZ() == b.getLocation().getBlockZ()) && (aBlock.getLocation().getWorld() == b.getLocation().getWorld())) {
                    event.setCancelled(true);
                    break;
                }
             }
         }
     }
 
     @EventHandler
     public void redstoneChange(BlockRedstoneEvent event) {
         for (int i = 0; i < signList.size(); i++) {
             LogicSign L = signList.get(i);
             if (!L.isSign()) {
                 removeLine(L.toString());
                 signList.remove(i);
                 i--;
             }
             checkPower(L);
         }
     }
 
     @EventHandler
     public void signChange(SignChangeEvent event) {
         if (event.isCancelled()) {
             return;
         }
         Block sign = event.getBlock();
         if (sign.getType() != Material.WALL_SIGN) {
             return;
         }
         if (!event.getLine(0).equalsIgnoreCase("[Logic]")) {
             return;
         }
         if (isFormatted(event.getLines())) {
             LogicSign L = new LogicSign(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ(), event.getLine(1), event.getLine(2));
             if (L.canTorch()) {
                 event.setLine(0, ChatColor.DARK_BLUE + "[Logic]");
             } else {
                 event.setLine(0, ChatColor.DARK_RED + "[Logic]");
             }
             signList.add(L);
             writeSign(L);
             checkPower(L);
         } else {
             event.setLine(0, ChatColor.DARK_RED + "[Logic]");
         }
     }
 
     private boolean isFormatted(String[] lines) {
         if (!(lines[1].equalsIgnoreCase("left") || lines[1].equalsIgnoreCase("right") || lines[1].equalsIgnoreCase("back"))) {
             return false;
         }
         if (!(lines[2].equalsIgnoreCase("and") || lines[2].equalsIgnoreCase("or") || lines[2].equalsIgnoreCase("xor") || lines[2].equalsIgnoreCase("nor") || lines[2].equalsIgnoreCase("xnor") || lines[2].equalsIgnoreCase("xnand"))) {
             return false;
         }
         return true;
     }
 }
