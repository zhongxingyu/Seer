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
 
 import java.awt.Component;
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
     public static File[] client = new File[] {
         new File(MZLOptions.RootDir + "bin" + File.separator + "minecraft.jar"),
         new File(MZLOptions.RootDir + "bin" + File.separator + "lwjgl.jar"),
         new File(MZLOptions.RootDir + "bin" + File.separator + "jinput.jar"),
         new File(MZLOptions.RootDir + "bin" + File.separator + "lwjgl_util.jar"),
         new File(MZLOptions.RootDir + "bin" + File.separator + "natives")
     };
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
                 
                String AuthorizeResult = ru.zetrix.settings.NetUtil.ConnectNet(MZLOptions.AuthScrpt, "a=auth" + "&user=" + user + "&password=" + ShieldUtil.ShaHash(user.toLowerCase() + pass) + "&opt=" + ru.zetrix.settings.ShieldUtil.GetMAC() + "&localhost=" + LHost.toString());
                 print(AuthorizeResult);
                 
                 if (AuthorizeResult == null) {
                     return false;
                 }
                 
                 switch (AuthorizeResult.trim()) {
                     case "Bad Login":
                         return false;
                     case "Fuck Off":
                         BuildGui.WrongClient = true;
                         return false;
                     default:
                         try {
                             hashes = AuthorizeResult.split("_")[0].split("<>");
                             clientinf = AuthorizeResult.split("_")[1].split("<>");
                             
                             print("Auth OK. Starting 'check for download' process...");
                             Util.SleepTime(Long.valueOf(500L));
                         } catch (Exception e) {
                             print(e.toString());
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
     }
     
     public static boolean checkUpdate() {
         print("Searching for client files...");
         print("Library files: " + client[4].list().length);
         
         if ((!client[0].exists()) || (!client[1].exists()) || (!client[2].exists()) || (!client[3].exists()) || ((!client[4].isDirectory()) || (client[4].list().length < 8))) {
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
         print("Searching for client files...");
         if ((!client[0].exists()) || (!client[1].exists()) || (!client[2].exists()) || (!client[3].exists())) {
             print("One or more files not found. \n Nothing can be done in Offline Mode!");
             return false;
         } else {
             print("Client files succesfully detected!");
             print("Running game in offline mode!");
             return true;
         }
     }
     
    public static void Register(final String user, final String pass, final String mail) {
         new Thread() {
             @Override
             public void run() {
                 print("Sending client data...");
                String RegResult = ru.zetrix.settings.NetUtil.ConnectNet(MZLOptions.AuthScrpt, "a=reg" + "&user=" + user + "&password=" + ShieldUtil.ShaHash(user.toLowerCase() + pass) + "&mail=" + mail + "&opt=" + ru.zetrix.settings.ShieldUtil.GetMAC());
                 print(RegResult);
                 
                 if (RegResult == null) {
                     print("Error... Null result");
                     return;
                 }
                 switch (RegResult.trim()) {
                     case "Fail1":
                         JOptionPane.showMessageDialog((Component)
                         null,
                         "You are already have an account \n Please, do not try to register another one. \n",
                         "Warning - MultiAccount",
                         JOptionPane.WARNING_MESSAGE);
                         break;
                     case "Fail2":
                         JOptionPane.showMessageDialog((Component)
                         null,
                         "Username is already in use \n Please, choose another one. \n",
                         "Warning - Username already exits",
                         JOptionPane.WARNING_MESSAGE);
                         break;
                     case "Success":
                         JOptionPane.showMessageDialog((Component)
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
                         break;
                     default:
                         JOptionPane.showMessageDialog((Component)
                         null,
                         "Oops, something went wrong! \n Try again later. \n",
                         "Warning - Error",
                         JOptionPane.WARNING_MESSAGE);
                         break;
                 }
             }
         }
                 .start();
     }
     
     private static void print(String str) {
         Debug.Logger(str);
     }
 }
