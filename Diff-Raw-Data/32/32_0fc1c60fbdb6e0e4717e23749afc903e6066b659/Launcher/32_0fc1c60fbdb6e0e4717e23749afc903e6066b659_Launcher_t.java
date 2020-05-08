 /*
  *  Wezzle
  *  Copyright (c) 2007-2010 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.dialog.LicenseDialog;
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
 
 /**
  *
  * @author kgrad
  * @author cdmckay
  */
 public class Launcher extends Applet
 {
     private Game game;
     private Canvas displayParent;
    private Thread thread;
 
     @Override
     public void init()
     {
         removeAll();
         setLayout(new BorderLayout());
         setIgnoreRepaint(true);
 
         try
         {            
             displayParent = new Canvas();
 
             displayParent.setSize(getWidth(), getHeight());
             add(displayParent);
             displayParent.setFocusable(true);
             displayParent.requestFocus();
             displayParent.setIgnoreRepaint(true);
             setVisible(true);
 
            thread = new Thread()
            {
                @Override
                public void run()
                {
                    startWezzle(displayParent);
                }
            };
            thread.start();
         }
         catch (Exception e)
         {
             CouchLogger.get().recordException(this.getClass(), e, true /* Fatal */);            
         }
     }
 
     @Override
     public void destroy()
     {
         stopWezzle();
 
        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            CouchLogger.get().recordException(this.getClass(), e, true /* Fatal */);
        }

         if (displayParent != null)
         {
             remove(displayParent);
         }
         
         super.destroy();
     }
     
     public void startWezzle(Canvas parent)
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
 
         try
         {
             final String serialNumber = settingsMan.getString(Key.USER_SERIAL_NUMBER);
             final String licenseKey = settingsMan.getString(Key.USER_LICENSE_KEY);
             final boolean validated = Game.validateLicenseInformation(serialNumber, licenseKey);
 
             if (!validated)
             {
                 LicenseDialog.run();
 
                 final String enteredSerialNumber = settingsMan.getString(Key.USER_SERIAL_NUMBER);
                 final String enteredLicenseKey = settingsMan.getString(Key.USER_LICENSE_KEY);
                 final boolean enteredValidated =
                         Game.validateLicenseInformation(enteredSerialNumber, enteredLicenseKey);
 
                 if (enteredValidated)
                 {
                     CouchLogger.get().recordMessage( Game.class,
                             "License information verified");
                 }
                 else
                 {
                     CouchLogger.get().recordException( Game.class,
                             new Exception("Invalid license information"),
                             true /* Fatal */);
                 }
             }
 
             game = new Game(parent, ResourceFactory.Renderer.LWJGL);
             game.start();            
         }
         catch (Throwable t)
         {
             CouchLogger.get().recordException(Game.class, t);
        }        
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
         Launcher launcher = new Launcher();
         launcher.startWezzle(null);
        System.exit(0);
     }
 }
