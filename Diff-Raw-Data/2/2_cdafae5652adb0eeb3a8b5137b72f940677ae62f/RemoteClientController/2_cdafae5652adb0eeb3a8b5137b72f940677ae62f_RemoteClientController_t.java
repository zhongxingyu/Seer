 package mcremoteclient.mainframe;
 
 import java.awt.Desktop;
 import java.io.*;
 import java.nio.file.Files;
 import java.nio.file.StandardCopyOption;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mcremoteclient.*;
 import mcremoteclient.logfile.Logfile;
 
 /**
  *
  * @author Michael
  */
 public class RemoteClientController
 {
     //TODO: Die ganzen IOExceptions abfangen und GUI ausloggen
     //TODO: Exceptions loggen
 
     private RemoteClientModel model;
     private IOQueue queue = new IOQueue();
     private Settings settings;
     private Thread updateAllInBackground;
     private Logfile log;
 
     public RemoteClientController(RemoteClientModel model)
     {
         this.model = model;
         settings = new Settings();
         model.setSettings(settings);
         log = new Logfile(new File(settings.getLocalFolder(), "log.txt"));
         startQueue();
         if (settings.isAutoupdate())
         {
             startBackgroundThread();
         }
     }
 
     public void deleteKnownServer()
     {
         Server currentServer = model.getCurrentServer();
         if (currentServer != null)
         {
             try
             {
                 model.removeServer(currentServer);
                 File serverDir = new File(settings.getLocalFolder(), "servers");
                 File serverFile = new File(serverDir, currentServer.getName() + ".server");
                 Files.delete(serverFile.toPath());
             } catch (IOException ex)
             {
                 model.addErrorMessage("Server-Datei konnte nicht gelöscht werden");
                 Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
             }
             model.fireChange();
         }
     }
 
     private void startBackgroundThread()
     {
         if (updateAllInBackground == null || !updateAllInBackground.isAlive())
         {
             updateAllInBackground = new Thread("UpdateAllThread")
             {
 
                 @Override
                 public void run()
                 {
                     while (!isInterrupted())
                     {
                         try
                         {
                             sleep(settings.getAutoupdateInterval() * 1000);
                             System.out.println("UpdateAll");
                             updateAll();
                         } catch (InterruptedException ex)
                         {
                             return;
                         }
                     }
                 }
             };
             updateAllInBackground.start();
         }
     }
 
     public void changeSettingsAutoupdate(boolean autoupdate)
     {
         settings.setAutoupdate(autoupdate);
         if (!autoupdate)
         {
             updateAllInBackground.interrupt();
         } else
         {
             startBackgroundThread();
         }
     }
 
     public void changeSettingsUseConsole(boolean useConsole)
     {
         settings.setUseConsole(useConsole);
     }
 
     private void startQueue()
     {
         queue.start();
     }
 
     public void exit()
     {
         settings.save();
         saveServers();
         System.exit(0);
     }
 
     private void instantLogout()
     {
         model.addErrorMessage("Deine Verbindung zum Server wurde unterbrochen");
         model.setLoggedIn(false);
         model.fireChange();
     }
 
     public void sayMessage(final String message)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().say(message);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateConsole(final boolean fireChange)
     {
         if (!settings.isUseConsole())
         {
             return;
         }
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     String[] updates = model.getCurrentServer().updateConsole();
                     model.addToConsole(updates);
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void reload()
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().reload();
                     updatePlugins(true);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void changeGamemodus(final Player player)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     if (player.getGamemode() == Player.GAMEMODE_CREATIVE)
                     {
                         model.getCurrentServer().setPlayerGamemode(player, Player.GAMEMODE_SURVIVAL);
                     } else
                     {
                         model.getCurrentServer().setPlayerGamemode(player, Player.GAMEMODE_CREATIVE);
                     }
                     updatePlayers(true);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void changeOPStatus(final Player player)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     if (player.isOp())
                     {
                         model.getCurrentServer().setPlayerOP(player, false);
                     } else
                     {
                         model.getCurrentServer().setPlayerOP(player, true);
                     }
                     updatePlayers(true);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void changeWorldPVP(final World world, final boolean pvp)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().setWorldPVP(world, pvp);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void changeWorldTime(final World world, final long time)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().setWorldTime(world, time);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void createAdmin(final String name, final String password)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().createAdmin(name, encryptPassword(password));
                 } catch (IOException | NoSuchAlgorithmException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void deleteAdmin(final String name)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().deleteAdmin(name);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void changeAdminPassword(final String admin, final String password)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().changeAdminPassword(admin, encryptPassword(password));
                 } catch (IOException | NoSuchAlgorithmException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void loadServers()
     {
         File serverDir = new File(settings.getLocalFolder(), "servers");
         if (serverDir.exists())
         {
             for (File f : serverDir.listFiles())
             {
                 ObjectInputStream oIn = null;
                 try
                 {
                     oIn = new ObjectInputStream(new FileInputStream(f));
                     Server server = (Server) oIn.readObject();
                     model.addServer(server);
                     oIn.close();
                 } catch (IOException | ClassNotFoundException ex)
                 {
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 } finally
                 {
                     try
                     {
                         oIn.close();
                     } catch (IOException ex)
                     {
                         Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
             model.fireChange();
         }
 
     }
 
     private void saveServers()
     {
         File serverDir = new File(settings.getLocalFolder(), "servers");
         if (!serverDir.exists())
         {
             serverDir.mkdirs();
         }
         for (Server s : model.getAllServers())
         {
             try
             {
                 File serverFile = new File(serverDir, s.getName() + ".server");
                 s.writeToFile(serverFile);
             } catch (IOException ex)
             {
                 Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public void changeCurrentServer(Server server)
     {
         if (server != model.getCurrentServer())
         {
             //Wenn der Server der gleiche ist, wurde nur das GUI geupdated, keine Aktion vom User
             if (!model.isLoggedIn())
             {
                 model.setCurrentServer(server);
                 model.fireChange();
             }
         }
     }
 
     public void changeLocalFolder(File folder)
     {
         try
         {
             //TODO: Dateien kopieren
             File oldFolder = settings.getLocalFolder();
             Files.move(oldFolder.toPath(), folder.toPath(), StandardCopyOption.REPLACE_EXISTING);
             settings.setLocalFolder(folder);
         } catch (IOException ex)
         {
             model.addErrorMessage("Verzeichnis konnte nicht geändert werden");
         }
         model.fireChange();
     }
 
     public void changeAutoupdateInterval(String interval)
     {
         try
         {
             int newInterval = Integer.parseInt(interval);
             settings.setAutoupdateInterval(newInterval);
         } catch (NumberFormatException ex)
         {
             model.addErrorMessage("Du musst eine Zahl als Wert angeben!");
         }
         model.fireChange();
     }
 
     public void sendMessageToPlayer(final Player player, final String message)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().messagePlayer(player, message);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void kickPlayer(final Player player, final String reason)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().kickPlayer(player, reason);
                     updatePlayers(true);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void banPlayer(final Player player, final String reason)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().banPlayer(player, reason);
                     updatePlayers(true);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void updateCurrentStatus(String status)
     {
         model.setCurrentStatus(status);
         model.fireChange();
     }
 
     public void updateAll()
     {
         //TODO: Methoden aufteilen, damit für einzelne Aktionen (z.B. Spieler kicken) nicht immer ALLES neu abgefragt werden muss
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 Server server = model.getCurrentServer();
                 if (server != null && model.isLoggedIn())
                 {
                     updateServername(false);
                     updateBukkitVersion(false);
                     updateServerVersion(false);
                     updatePlayers(false);
                     updatePlugins(false);
                     updateWorlds(false);
                     updateAdmins(false);
                     updateWhitelistedPlayers(false);
                     updateBannedPlayers(false);
                     updateFlyingAllowed(false);
                     updateNetherAllowed(false);
                     updateTheEndAllowed(false);
                     updateWhitelistActive(false);
                     updateViewdistance(false);
                     updateDefaultGamemode(false);
                     updateMaxPlayers(false);
                     updateMOTD(false);
                     updateConsole(true);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateDefaultGamemode(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setDefaultGamemode(model.getCurrentServer().getDefaultGamemode());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateViewdistance(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setViewdistance(model.getCurrentServer().getViewdistance());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateNetherAllowed(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setNetherAllowed(model.getCurrentServer().isNetherAllowed());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateTheEndAllowed(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setTheEndAllowed(model.getCurrentServer().isTheEndAllowed());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateFlyingAllowed(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setFlyingAllowed(model.getCurrentServer().isFlyingAllowed());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateWhitelistActive(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setWhitelistActive(model.getCurrentServer().isWhitelistActive());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateAdmins(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setAdmins(model.getCurrentServer().getAdmins());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void updateMaxPlayers(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setMaxPlayers(model.getCurrentServer().getMaxPlayers());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateMOTD(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setMotd(model.getCurrentServer().getMOTD());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateWorlds(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setWorlds(model.getCurrentServer().getWorlds());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updatePlugins(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setPlugins(model.getCurrentServer().getPlugins());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updatePlayers(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setPlayers(model.getCurrentServer().getPlayers());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateServerVersion(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setServerVersion(model.getCurrentServer().getServerVersion());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void executeCommand(final String command)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().execCommand(command);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void updateServername(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setServerName(model.getCurrentServer().getServername());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateBannedPlayers(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setBannedPlayers(model.getCurrentServer().getBannedPlayers());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateWhitelistedPlayers(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                    model.setWhitelistedPlayers(model.getCurrentServer().getWhiteListedPlayers());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateBukkitVersion(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.setBukkitVersion(model.getCurrentServer().getBukkitVersion());
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateHasFTPSupport(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     Server currentServer = model.getCurrentServer();
                     currentServer.getFTPisSupported();
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateFTPUsername(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     Server currentServer = model.getCurrentServer();
                     if (currentServer.isFtpSupport())
                     {
                         currentServer.getFTPUsername();
                     }
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void updateFTPPassword(final boolean fireChange)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     Server currentServer = model.getCurrentServer();
                     if (currentServer.isFtpSupport())
                     {
                         currentServer.getFTPPassword();
                     }
                     if (fireChange)
                     {
                         model.fireChange();
                     }
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void login(final String name, final String adress, final String port, final String username, final String password, final boolean save)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 boolean updateView = true;
                 try
                 {
                     Server server = model.getCurrentServer();
                     boolean isNewServer = false;
                     boolean loginHasChanged = false;
                     int portNum = Integer.parseInt(port);
                     if (server == null)
                     {
                         isNewServer = true;
                     } else
                     {
                         if (!name.equals(server.getName()))
                         {
                             isNewServer = true;
                         }
                     }
                     if (isNewServer)
                     {
                         server = new Server(name, adress, portNum);
                         model.addServer(server);
                         loginHasChanged = true;
                     } else
                     {
                         if (server.getUsername() == null || server.getPassword() == null)
                         {
                             loginHasChanged = true;
                         } else if (!server.getUsername().equals(username) || password.length() > 0)
                         {
                             loginHasChanged = true;
                         }
                     }
                     server.connect();
                     model.setCurrentServer(server);
                     boolean login;
                     if (loginHasChanged)
                     {
                         login = server.login(username, encryptPassword(password), save);
                     } else
                     {
                         login = server.login(username, server.getPassword(), save);
                     }
                     if (login)
                     {
                         model.setLoggedIn(true);
                         updateAll();
                         updateView = false;
                     } else
                     {
                         model.setLoggedIn(false);
                         model.addErrorMessage("Falsche Logindaten");
                     }
                 } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex)
                 {
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (IOException ex)
                 {
                     model.addErrorMessage("Es konnte keine Verbindung zum Server hergestellt werden");
                 } catch (NumberFormatException ex)
                 {
                     model.addErrorMessage("Der Port muss eine gültige Zahl sein");
                 }
                 if (updateView)
                 {
                     model.fireChange();
                 }
             }
         };
         queue.addTask(r);
 
     }
 
     public void logout()
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().logout();
                     model.setLoggedIn(false);
                     model.fireChange();
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     private byte[] encryptPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
     {
         MessageDigest md = MessageDigest.getInstance("SHA-512");
         md.update(password.getBytes("UTF-8"));
         return md.digest();
     }
 
     public void orderSaveAll()
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().saveAll();
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void orderStopServer()
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().stop();
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void addBannedPlayer(final String player)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().addBannedPlayer(player);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void addWhitelistedPlayer(final String player)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().whitelistPlayer(player);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void removePlayerFromWhitelist(final String player)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().removePlayerFromWhitelist(player);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void unbanPlayer(final String player)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().unbanPlayer(player);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void deactivatePlugin(final String plugin)
     {
         Runnable r = new Runnable()
         {
 
             @Override
             public void run()
             {
                 try
                 {
                     model.getCurrentServer().deactvatePlugin(plugin);
                 } catch (IOException ex)
                 {
                     instantLogout();
                     Logger.getLogger(RemoteClientController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         queue.addTask(r);
     }
 
     public void openLocalFolder()
     {
         try
         {
             File localFolder = settings.getLocalFolder();
             Desktop.getDesktop().open(localFolder);
         } catch (IOException ex)
         {
             model.addErrorMessage("Verzeichnis konnte nicht geöffnet werden");
         }
     }
 }
