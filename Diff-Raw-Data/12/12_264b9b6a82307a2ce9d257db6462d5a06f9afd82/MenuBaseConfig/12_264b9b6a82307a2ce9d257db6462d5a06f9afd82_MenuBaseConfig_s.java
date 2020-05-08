 package net.aetherteam.mainmenu_api;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Properties;
 
 import net.minecraft.client.Minecraft;
 
 public class MenuBaseConfig
 {
     private static Minecraft mc = Minecraft.getMinecraft();
 
     private static final Properties menuProps = new Properties();
     public static String selectedMenuName;
     public static boolean loopMusic;
     public static boolean muteMusic;
     public static int lastMusicIndex;
     public static int musicIndex;
     public static boolean musicSet;
     public static boolean hasPlayedMusic;
     public static boolean hasStartedMusic;
     public static boolean endMusic = true;
     public static JukeboxPlayer jukebox = new JukeboxPlayer();
     public static double playerPosX;
     public static double playerPosY;
     public static double playerPosZ;
     public static int ticks;
     private static File config = new File(Minecraft.getMinecraftDir(), "MenuAPI.properties");
 
     public static void loadConfig()
     {
         Minecraft.getMinecraft();
         System.out.println(Minecraft.getMinecraftDir());
         if (config.exists()) try
         {
             Minecraft.getMinecraft();
             FileInputStream in = new FileInputStream(Minecraft.getMinecraftDir() + "/MenuAPI.properties");
 
             menuProps.load(in);
 
             if (menuProps.size() <= 0)
             {
                 resetConfig();
                 return;
             }
 
             if (menuProps != null)
             {
                 selectedMenuName = String.valueOf(menuProps.getProperty("selectedMenu"));
                 loopMusic = menuProps.getProperty("loopMusic") != null ? menuProps.getProperty("loopMusic").equals("true") : true;
                 muteMusic = menuProps.getProperty("muteMusic") != null ? menuProps.getProperty("muteMusic").equals("true") : false;
                 lastMusicIndex = Integer.valueOf(menuProps.getProperty("lastMusicIndex")).intValue();
                 musicIndex = Integer.valueOf(menuProps.getProperty("musicIndex")).intValue();
                 musicSet = menuProps.getProperty("musicSet") != null ? menuProps.getProperty("musicSet").equals("true") : false;
                 hasPlayedMusic = menuProps.getProperty("hasPlayedMusic") != null ? menuProps.getProperty("hasPlayedMusic").equals("true") : false;
                 hasStartedMusic = menuProps.getProperty("hasStartedMusic") != null ? menuProps.getProperty("hasStartedMusic").equals("true") : false;
             }
 
         } catch (FileNotFoundException e)
         {
             e.printStackTrace();
         } catch (IOException e)
         {
             e.printStackTrace();
         }
         else resetConfig();
     }
 
     public static void resetConfig()
     {
         try
         {
             System.out.println(String.valueOf(jukebox.getIndexFromName("Strad")));
 
             menuProps.setProperty("selectedMenu", "");
             menuProps.setProperty("loopMusic", "true");
             menuProps.setProperty("muteMusic", "false");
             menuProps.setProperty("lastMusicIndex", String.valueOf(jukebox.getIndexFromName("Strad")));
             menuProps.setProperty("musicIndex", String.valueOf(jukebox.getIndexFromName("Strad")));
             menuProps.setProperty("musicSet", "false");
             menuProps.setProperty("hasPlayedMusic", "false");
             menuProps.setProperty("hasStartedMusic", "false");
 
             Minecraft.getMinecraft();
             menuProps.store(new FileOutputStream(Minecraft.getMinecraftDir() + "/MenuAPI.properties"), null);
 
             Minecraft.getMinecraft();
             FileInputStream in = new FileInputStream(Minecraft.getMinecraftDir() + "/MenuAPI.properties");
 
             menuProps.load(in);
         } catch (FileNotFoundException e)
         {
             e.printStackTrace();
         } catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     public static void wipeConfig()
     {
         if (config.exists()) try
         {
             menuProps.setProperty("selectedMenu", "");
 
             Minecraft.getMinecraft();
             menuProps.store(new FileOutputStream(Minecraft.getMinecraftDir() + "/MenuAPI.properties"), null);
 
            FileInputStream in = new FileInputStream("MenuAPI.properties");
 
             menuProps.load(in);
         } catch (FileNotFoundException e)
         {
             e.printStackTrace();
         } catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     public static void setProperty(String name, String value)
     {
         try
         {
             menuProps.setProperty(name, value);
 
             Minecraft.getMinecraft();
             menuProps.store(new FileOutputStream(Minecraft.getMinecraftDir() + "/MenuAPI.properties"), null);
 
            FileInputStream in = new FileInputStream("MenuAPI.properties");
 
             menuProps.load(in);
         } catch (FileNotFoundException e)
         {
             e.printStackTrace();
         } catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     static
     {
         Minecraft.getMinecraft();
     }
 }

/* Location:           D:\Dev\Mc\forge_orl\mcp\jars\bin\aether.jar
 * Qualified Name:     net.aetherteam.mainmenu_api.MenuBaseConfig
 * JD-Core Version:    0.6.2
 */
