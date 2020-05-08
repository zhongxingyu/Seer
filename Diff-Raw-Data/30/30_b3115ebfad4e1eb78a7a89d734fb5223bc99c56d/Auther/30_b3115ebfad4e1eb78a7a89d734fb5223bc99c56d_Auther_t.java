 /*
  * McZLauncher (ZeTRiX's Minecraft Launcher)
  * Copyright (C) 2012 Evgen Yanov <http://www.zetlog.ru>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program (In the "_License" folder). If not, see <http://www.gnu.org/licenses/>.
 */
 
 package ru.zetrix.engine;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import javax.swing.JOptionPane;
 import ru.zetrix.settings.Debug;
 import ru.zetrix.settings.MZLOptions;
 import ru.zetrix.settings.ShieldUtil;
 import ru.zetrix.settings.Util;
 
 /**
  *
  * @author ZeTRiX
  */
 public class Auther {
     private static InetAddress LHost;
     public static MCStart MineStart;
     public static String[] clientinf;
     public static String[] hashes;
     public static File[] client = new File[4];
     public static Updater Update;
     
     public static boolean Authorize(final String user, final String pass) {
         try {
             LHost = InetAddress.getLocalHost();
         } catch (UnknownHostException ex) {
             print(ex.toString());
         }
 //        new Thread() {
 //            @Override
 //            public void run() {
                 print("Processing client data...");
                 
                 String AuthorizeResult = ru.zetrix.settings.NetUtil.ConnectNet("http://test.zetlog.ru/launchertest/auth.php", "a=auth" + "&user=" + user + "&password=" + ShieldUtil.ShaHash(user + pass) + "&opt=" + ru.zetrix.settings.ShieldUtil.GetMAC() + "&localhost=" + LHost.toString());
                 print(AuthorizeResult);
                 
                 if (AuthorizeResult == null) {
                     //BuildGui.this.setAuthError("Error connecting to login server", allpanels);
                     return false;
                 }
                 print("Local Host and Address: " + LHost.toString());
                 
                 if (AuthorizeResult.trim().equals("Bad Login")) {
                     //BuildGui.this.setAuthError("Username or password is incorrect", allpanels);
                     return false;
                 } else if (AuthorizeResult.trim().equals("Fuck Off")) {
                     BuildGui.WrongClient = true;
                     return false;                
                 } else {
                     try {
                         print(AuthorizeResult);
                         hashes = AuthorizeResult.split("_")[0].split("<>");
                         clientinf = AuthorizeResult.split("_")[1].split("<>");
                         
                         print("Auth OK. Starting 'check for download' process...");
                         Util.SleepTime(Long.valueOf(500L));
                         //BuildGui.runUpdater(md5s, userdata, mods, BuildGui.buildgui);
                     } catch (Exception e) {
                         e.printStackTrace();
                         //BuildGui.this.AuthError(e.toString(), allpanels);
                     }
                     if (checkUpdate() == true) {
                     MineStart = new MCStart(user, clientinf[1].trim());
                     return true;
                     } else {
                         Update = new Updater();
                         Update.show();
                         return false;
                     }
                 }
 //            }
 //        }
 //                .start();
     }
     
     public static boolean checkUpdate() {
         print("Searching for client files...");
         
         File bin = new File(MZLOptions.RootDir + "bin" + File.separator + "natives");
         client[0] = new File(MZLOptions.RootDir + "bin" + File.separator + "minecraft.jar");
         client[1] = new File(MZLOptions.RootDir + "bin" + File.separator + "lwjgl.jar");
         client[2] = new File(MZLOptions.RootDir + "bin" + File.separator + "jinput.jar");
         client[3] = new File(MZLOptions.RootDir + "bin" + File.separator + "lwjgl_util.jar");
         
         if ((!client[0].exists()) || (!client[1].exists()) || (!client[2].exists()) || (!client[3].exists()) || ((!bin.isDirectory()) || (bin.list().length == 0))) {
             print("One or more files not found. \n Starting Update!");
             return false;
         } else {
             print("Client files succesfully detected! \n Checking client hash!");
             print(ShieldUtil.FileHash(MZLOptions.RootDir + "bin" + File.separator + "minecraft.jar"));
            if ((hashes[1].equals(ShieldUtil.FileHash(MZLOptions.RootDir + "bin" + File.separator + "minecraft.jar"))) && (Integer.parseInt(hashes[2].toString()) == ShieldUtil.FileSize(MZLOptions.RootDir + "bin" + File.separator + "minecraft.jar"))) {
                 return true;
             } else {
                 return false;
             }
         }
     }
     
     public static boolean OfflineCheck() {
         client[0] = new File(MZLOptions.RootDir + "bin" + File.separator + "minecraft.jar");
         client[1] = new File(MZLOptions.RootDir + "bin" + File.separator + "lwjgl.jar");
         client[2] = new File(MZLOptions.RootDir + "bin" + File.separator + "jinput.jar");
         client[3] = new File(MZLOptions.RootDir + "bin" + File.separator + "lwjgl_util.jar");
 
         print("Searching for client files...");
         if ((!client[0].exists()) || (!client[1].exists()) || (!client[2].exists()) || (!client[3].exists())) {
             print("One or more files nor found. \n Nothing can be done in Offline Mode!");
             return false;
         } else {
             print("Client files succesfully detected!");
             print("Running game in offline mode!");
             return true;
         }
     }
     
     public static void Register(final String user, final char[] pass, final String mail) {
         new Thread() {
             @Override
             public void run() {
                 print("Sending client data...");
                 String RegResult = ru.zetrix.settings.NetUtil.ConnectNet("http://test.zetlog.ru/launchertest/auth.php", "a=reg" + "&user=" + user + "&password=" + ShieldUtil.ShaHash(user + pass) + "&mail=" + mail + "&opt=" + ru.zetrix.settings.ShieldUtil.GetMAC());
                 print(RegResult);
                 
                 if (RegResult == null) {
                     print("Error... Null result");
                     return;
                 }
                 
                 if (RegResult.trim().equals("Fail")) {
                     javax.swing.JOptionPane.showMessageDialog((java.awt.Component)
                     null,
                     "You are already have an account \n Please, do not try to register another one. \n",
                     "Warning - MultiAccount",
                     JOptionPane.WARNING_MESSAGE);
                 } else if (RegResult.trim().equals("Success")) {
                     javax.swing.JOptionPane.showMessageDialog((java.awt.Component)
                     null,
                     "You have registered a new account! \n Thank you and enjoy the game. \n",
                     "Ok - Register Complete!",
                     JOptionPane.INFORMATION_MESSAGE);
                     try {
                         print("You have successfully registered...");
                         Util.SleepTime(Long.valueOf(500L));
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 } else {
                     javax.swing.JOptionPane.showMessageDialog((java.awt.Component)
                     null,
                     "Oh, something went wrong! \n Try again later. \n",
                     "Warning - Error",
                     JOptionPane.WARNING_MESSAGE);                
                 }
             }
         }
                 .start();
     }
     
     private static void print(String str) {
         Debug.Logger(str);
     }
 }
