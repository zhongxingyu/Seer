 /*
  *  Wezzle
  *  Copyright (c) 2007-2010 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.dialog.AgreementDialog;
 import ca.couchware.wezzle2d.dialog.LicenseDialog;
 import ca.couchware.wezzle2d.dialog.TrialLauncherDialog;
 import ca.couchware.wezzle2d.manager.Achievement;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.manager.SettingsManager;
 import ca.couchware.wezzle2d.ui.Button;
 import ca.couchware.wezzle2d.ui.ProgressBar;
 import ca.couchware.wezzle2d.ui.RadioItem;
 import ca.couchware.wezzle2d.ui.SpeechBubble;
 import ca.couchware.wezzle2d.util.CouchLogger;
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.io.File;
 
 /**
  * Launches the Wezzle applet or app, depending on how it is called.
  *
  * @author kgrad
  * @author cdmckay
  */
 public class Launcher extends Applet
 {
     private Game game;
     private Canvas displayParent;
     private Thread thread;
 
     public void initThread()
     {
         if (thread != null) return;
 
         CouchLogger.get().recordMessage(getClass(), "Init thread");
 
         thread = new Thread()
         {
             @Override
             public void run()
             {
                 startWezzle(displayParent, false);
             }
         };
 
         thread.start();
     }
 
     public void destroyThread()
     {
         CouchLogger.get().recordMessage(getClass(), "Destroy thread");
 
         stopWezzle();
 
         try
         {
             thread.join();
             CouchLogger.get().recordMessage(getClass(), "Thread joined");
         }
         catch (InterruptedException e)
         {
             CouchLogger.get().recordException(getClass(), e, true /* Fatal */);
         }
     }
 
     @Override
     public void init()
     {
         CouchLogger.get().recordMessage(getClass(), "Applet init");
 
         removeAll();
         setLayout(new BorderLayout());
         setIgnoreRepaint(true);
 
         try
         {            
             displayParent = new Canvas()
             {
                 @Override
                 public final void addNotify()
                 {
                     super.addNotify();
                     initThread();
                 }
 
                 @Override
                 public final void removeNotify()
                 {
                     destroyThread();
                     super.removeNotify();
                 }
             };
 
             displayParent.setSize(getWidth(), getHeight());
             add(displayParent);
             displayParent.setFocusable(true);
             displayParent.requestFocus();
             displayParent.setIgnoreRepaint(true);
             setVisible(true);            
         }
         catch (Exception e)
         {
             CouchLogger.get().recordException(this.getClass(), e, true /* Fatal */);            
         }
     }
 
     @Override
     public void destroy()
     {
         CouchLogger.get().recordMessage(this.getClass(), "Applet destroy started");
 
         if (displayParent != null)
         {
             remove(displayParent);
         }
 
         super.destroy();
         CouchLogger.get().recordMessage(this.getClass(), "Applet destroy completed");
     }
 
     public boolean validate(SettingsManager settingsMan)
     {
         final String serialNumber = settingsMan.getString(Key.USER_SERIAL_NUMBER);
         final String licenseKey = settingsMan.getString(Key.USER_LICENSE_KEY);
         return Game.validateLicenseInformation(serialNumber, licenseKey);
     }
     
     public void startWezzle(Canvas parent, boolean trialMode)
     {
         // Make sure the setting manager is loaded.
         SettingsManager settingsMan = SettingsManager.get();
 
         // Send a reference to the resource manager.
         ResourceFactory.get().setSettingsManager(settingsMan);
 
         // Set the default color scheme.
         ResourceFactory.setDefaultLabelColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         ProgressBar.setDefaultColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         RadioItem.setDefaultColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         SpeechBubble.setDefaultColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         Button.setDefaultColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         Achievement.Level.initializeAchievementColorMap(settingsMan);
 
         // Set up log level.
         CouchLogger.get().setLogLevel(settingsMan.getString(Key.DEBUG_LOG_LEVEL));
 
         try
         {           
             if (!validate(settingsMan))
             {
                 if (!trialMode)
                 {
                     LicenseDialog.run();
 
                    if (validate(settingsMan))
                     {
                         CouchLogger.get().recordMessage( Game.class,
                                 "License information verified");
                         startGame(parent, false);
                     }
                     else
                     {
                         CouchLogger.get().recordWarning( Game.class,
                                 "Invalid license information");                        
                     }
                 }
                 else
                 {
                     boolean licensed = false;
                     boolean loadWezzle = false;
                     do
                     {
                         loadWezzle = TrialLauncherDialog.run();
                         licensed = validate(settingsMan);
                         if (loadWezzle)
                         {
                             startGame(parent, !licensed);
                         }
                     }
                     while (loadWezzle && !licensed);
                     
                 } // end if
             } // end if
             else
             {
                 startGame(parent, false);
             }
         }
         catch (Throwable t)
         {
             CouchLogger.get().recordException(Game.class, t);
             System.exit(0);
         }        
     }
 
     private void startGame(Canvas parent, boolean trialMode)
     {
         game = new Game(parent, ResourceFactory.Renderer.LWJGL, trialMode);
         game.start();
     }
 
     public void stopWezzle()
     {
         game.stop();
     }
 
     /**
      * The entry point into the game. We'll simply create an instance of class
      * which will start the display and game loop.
      *
      * @param argv
      *            The arguments that are passed into our game
      */
     public static void main(String argv[])
     {
         final boolean trialMode = argv.length > 0 && argv[0].equals("--trial");
 
         Launcher launcher = new Launcher();
         launcher.startWezzle(null, trialMode);
 //        System.exit(0);
     }
 }
