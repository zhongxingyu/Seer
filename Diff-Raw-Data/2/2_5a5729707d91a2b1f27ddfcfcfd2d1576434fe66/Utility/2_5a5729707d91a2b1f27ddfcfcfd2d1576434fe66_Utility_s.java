 package com.huskehhh.oresomenotes;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 public class Utility {
 
     public static List<Player> getStaff() {
         List<Player> staff = new ArrayList<Player>();
         for (Player p : OresomeNotes.getInstance().getServer().getOnlinePlayers()) {
             if (p.hasPermission("oresomenotes.staff")) staff.add(p);
         }
         return staff;
     }
 
     public static List<String> getNotes(Player p) {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/OresomeNotes/config.yml"));
         return config.getStringList(p.getName() + ".notes");
     }
 
     public static void addNote(Player p, String note) {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/OresomeNotes/config.yml"));
         List<String> tmp1 = config.getStringList(p.getName() + ".notes");
         tmp1.add(note);
         config.set(p.getName() + ".notes", tmp1);
         try {
             config.save(new File("plugins/OresomeNotes/config.yml"));
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void removeNote(Player p, int entry) {
         List<String> notes = OresomeNotes.getInstance().config.getStringList(p.getName() + ".notes");
         ListIterator<String> iter = notes.listIterator();
         int count = 0;
         while (iter.hasNext()) {
             count++;
             if (count == entry) {
                 iter.remove();
                 break;
             }
         }
     }
 
     public static String argBuilding(String[] arg) {
         String build = "";
         for (int count = 2; count < arg.length; count++) {
             String str = removeSpaceAtStart(arg[count]);
             if (arg[count] != null) {
                 build = build + " " + str;
             } else if (arg[count].equalsIgnoreCase(" ")) {
                 build = str + " ";
             } else {
                 break;
             }
         }
         return removeSpaceAtStart(build);
     }
 
     public static String removeSpaceAtStart(String str) {
         if (str.startsWith(" ")) {
             return str.replaceFirst(" ", "");
         }
         return str;
     }
 
     public static String prepareForConfig(String extra) {
         return extra.replace(" ", ".") + ".";
     }
 
     public static String readFromConfig(String read) {
         return read.replace(".", " ") + ".";
     }
 
     public static ListIterator<String> iterRead(Player p) {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/OresomeNotes/config.yml"));
         List<String> notes = config.getStringList(p.getName() + ".notes");
         return notes.listIterator();
     }
 
     public static boolean hasNotes(Player p) {
         List<String> notes = getNotes(p);
        if (notes.get(1) != null) {
             return true;
         }
         return false;
     }
 
     public static void sendStaffNotes(Player p) {
         List<Player> staff = getStaff();
         List<String> notes = OresomeNotes.getInstance().config.getStringList(p.getName() + ".notes");
         ListIterator<String> iter = notes.listIterator();
         for (Player s : staff) {
             s.sendMessage(ChatColor.GREEN + "=====Notes for " + ChatColor.RED + p.getName() + ChatColor.GREEN + "======");
             while (iter.hasNext()) {
                 String in = iter.next();
                 if (!in.equals("dataManage--noedit")) {
                     s.sendMessage(ChatColor.BLUE + Utility.readFromConfig(in));
                 }
             }
         }
     }
 
 }
