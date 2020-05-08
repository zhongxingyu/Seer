 /*******************************************************************************
  * Copyright (c) 2011-2012 Nokia Corporation
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Comarch team - initial API and implementation
  *******************************************************************************/
 package org.ned.client;
 
 import com.sun.lwuit.Display;
 import com.sun.lwuit.Font;
 import com.sun.lwuit.animations.CommonTransitions;
 import com.sun.lwuit.plaf.UIManager;
 import com.sun.lwuit.util.Resources;
 import java.io.IOException;
 import javax.microedition.io.ConnectionNotFoundException;
 import javax.microedition.midlet.MIDletStateChangeException;
 import org.ned.client.command.OpenLibraryManagerCommand;
 import org.ned.client.statistics.StatType;
 import org.ned.client.statistics.StatisticsManager;
 import org.ned.client.transfer.DownloadManager;
 import org.ned.client.utils.NedIOUtils;
 import org.ned.client.view.*;
 import org.ned.client.view.customComponents.MenuBar;
 import org.ned.client.view.renderer.MenuCellRenderer;
 
 public class NedMidlet extends javax.microedition.midlet.MIDlet {
 
     private static final int SPLASH_DELAY = 2000;
     public static final int DOWNLOAD_AUTOMATIC = 0;
     public static final int DOWNLOAD_MANUAL = 1;
     private static NedMidlet instance;
     private String imei = null;
     private VideoPlayerView videoPlayerView = null;
     private AudioPlayerView audioPlayerView = null;
     private XmlManager xm = null;
     private DownloadManager downloadManager = null;
     private SettingsManager settingsManager = null;
     private AccountManager accountManager = null;
     private Resources res;
 
     public NedMidlet() {
         instance = this;
         settingsManager = new SettingsManager();
     }
 
     public static NedMidlet getInstance() {
         return instance;
     }
 
     public String getImei() {
         if ( imei != null ) {
             return imei;
         } else {
             return "noimei";
         }
     }
 
     public VideoPlayerView getVideoPlayer() {
         return videoPlayerView;
     }
 
     public AudioPlayerView getAudioPlayer() {
         return audioPlayerView;
     }
 
     public int getDownloadState() {
         return settingsManager.getDlAutomatic() ? DOWNLOAD_AUTOMATIC
                : DOWNLOAD_MANUAL;
     }
 
     public void setDownloadState( int _dlState ) {
         if ( _dlState == DOWNLOAD_AUTOMATIC ) {
             settingsManager.setDlAutomatic( true );
         } else {
             settingsManager.setDlAutomatic( false );
         }
     }
 
     public XmlManager getXmlManager() {
         return xm;
     }
 
     public DownloadManager getDownloadManager() {
         return downloadManager;
     }
 
     public static SettingsManager getSettingsManager() {
         return getInstance().settingsManager;
     }
 
     public static AccountManager getAccountManager() {
         return getInstance().accountManager;
     }
 
     public String getVersion() {
         return getAppProperty( "MIDlet-Version" );
     }
 
     public void playMedia( IContent content ) {
         if ( WaitingScreen.isShowed() ) {
             WaitingScreen.dispose();
         }
 
         if ( content.getType().equals( NedConsts.NedContentType.VIDEO ) ) {
             videoPlayerView = new VideoPlayerView( content );
             if ( videoPlayerView != null ) {
                 videoPlayerView.show();
                 videoPlayerView.prepareToPlay();
             }
         } else if ( content.getType().equals( NedConsts.NedContentType.AUDIO ) ) {
             audioPlayerView = new AudioPlayerView( content );
             audioPlayerView.show();
             audioPlayerView.start();
         } else if ( content.getType().equals( NedConsts.NedContentType.IMAGE ) ) {
             new ImageDisplayView( content ).show();
         } else if ( content.getType().equals( NedConsts.NedContentType.TEXT ) ) {
             if ( content.getMediaFile().toLowerCase().endsWith( "pdf" ) ) {
                 try {
                     platformRequest( content.getMediaFile() );
                 } catch ( ConnectionNotFoundException ex ) {
                     ex.printStackTrace();
                 }
             } else {
                 new TextDisplayView( content ).show();
             }
         } else {
             GeneralAlert.show( NedResources.DOC_NOT_SUPPORTED, GeneralAlert.WARNING );
             StatisticsManager.logEvent( StatType.PLAY_ITEM_END, "Id=" + content.
                     getId() + ";ERROR;" );
         }
     }
 
     public void startApp() {
         Display.init( this );
         loadTheme();
 
         UIManager.getInstance().getLookAndFeel().setDefaultDialogTransitionIn(
                 CommonTransitions.createSlide( CommonTransitions.SLIDE_VERTICAL, false,
                                                NedConsts.NedTransitions.TRANSITION_TIME ) );
         UIManager.getInstance().getLookAndFeel().setDefaultDialogTransitionOut(
                 CommonTransitions.createSlide( CommonTransitions.SLIDE_VERTICAL, true,
                                                NedConsts.NedTransitions.TRANSITION_TIME ) );
         UIManager.getInstance().getLookAndFeel().setDefaultFormTransitionIn(
                 CommonTransitions.createSlide( CommonTransitions.SLIDE_HORIZONTAL, false,
                                                NedConsts.NedTransitions.TRANSITION_TIME ) );
 
         NedIOUtils.setupRootDir();
         new SplashScreen().show();
         try {                            //TODO
             Thread.sleep( SPLASH_DELAY );//this is a simplest way to introduce delay for splash screen
             //in the future we might need to implement more sophisticated solution
         } catch ( InterruptedException ex ) {
         }
         imei = System.getProperty( "com.nokia.mid.imei" );
         xm = new XmlManager( this );
 
         boolean isOk = false;
         try {
             isOk = NedIOUtils.fileExists( NedIOUtils.getStorage() );
         } catch ( Exception ex ) {
             isOk = false;
         }
 
         if ( !isOk ) {
             GeneralAlert.show( NedResources.NO_MEMORY_CARD, GeneralAlert.ERROR );
             try {
                 destroyApp( true );
             } catch ( MIDletStateChangeException ex ) {
             }
             return;
         }
 
         NedIOUtils.createDirectory( NedIOUtils.getLocalData() );
         NedIOUtils.createDirectory( NedIOUtils.getLocalRoot() );
         accountManager = new AccountManager();
         if ( accountManager.getServerUrl() == null ) {
             new WelcomeScreen().show();
         } else {
             new LoginScreen().show();
         }
     }
 
     public void resetApp() {
         NedIOUtils.createDirectory( NedIOUtils.getLocalData() );
         NedIOUtils.createDirectory( NedIOUtils.getLocalRoot() );
 
         settingsManager = new SettingsManager();
 
         accountManager = new AccountManager();
         if ( accountManager.getServerUrl() == null ) {
             new WelcomeScreen().show();
         } else {
             new LoginScreen().show();
         }
     }
 
     private void loadTheme() {
         try {
             res = Resources.open( "/org/ned/client/NEDtheme.res" );
             UIManager.getInstance().setThemeProps( res.getTheme( res.
                     getThemeResourceNames()[0] ) );
 
             UIManager.getInstance().getLookAndFeel().setReverseSoftButtons( true );
             UIManager.getInstance().getLookAndFeel().setMenuBarClass( MenuBar.class );
             UIManager.getInstance().getLookAndFeel().setMenuRenderer( new MenuCellRenderer() );
             if ( Display.getInstance().isTouchScreenDevice() ) {
                 UIManager.getInstance().addThemeProps( res.getTheme( "TouchScreen" ) );
             }
         } catch ( IOException e ) {
             e.printStackTrace();
         }
     }
 
     public static Resources getRes() {
         return instance.res;
     }
 
     public static Font getFont( String id ) {
         return instance.res.getFont( id );
     }
 
     public Resources getLocalImageResources( String resourceName ) {
         Resources r = null;
         String suffix = ".res";
         String locale = System.getProperty( "microedition.locale" );
 
         // try to find localized resource first (in format <name>_locale.res)
         if ( (locale != null) && (locale.length() > 1) ) {
             try {
                 r = Resources.open( resourceName + "_" + locale + suffix );
             } catch ( IOException ex1 ) {
                 // replace '-' with '_', some phones returns locales with
                 // '-' instead of '_'. For example Nokia or Motorola
                 locale = locale.replace( '-', '_' );
                 try {
                     r = Resources.open( resourceName + "_" + locale + suffix );
                 } catch ( IOException ex2 ) {
                     // if no localized resource is found or localization is available
                     // try broader locale (i.e. instead e.g. en_US, try just en)
                     try {
                         r = Resources.open( resourceName + "_" + locale.
                                 substring( 0, 2 ) + suffix );
                     } catch ( IOException ex3 ) {
                         // if not found or locale is not set, try default locale
                         try {
                             r = Resources.open( resourceName + suffix );
                         } catch ( IOException ex4 ) {
                             ex4.printStackTrace();
                         }
                     }
                 }
             }
         }
         return r;
     }
 
     public void pauseApp() {
     }
 
     public void destroyApp( boolean unconditional ) throws MIDletStateChangeException {
         if ( !unconditional ) {
             StatisticsManager.logEvent( StatType.APP_EXIT, "" );
             StatisticsManager.dispose();
         }
         super.notifyDestroyed();
     }
 
     public void continueApploading() {
         NedIOUtils.createDirectory( NedIOUtils.getUserRootDirectory() );
         settingsManager.loadSettings();
         downloadManager = new DownloadManager( this );
         downloadManager.init();
 
         StatisticsManager.init( NedIOUtils.getUserRootDirectory() );
         StatisticsManager.logEvent( StatType.USER_LOGGED, accountManager.
                 getCurrentUser().login );
 
 //        if ( getSettingsManager().isSchedulerOn() ) {
 //            getScheduler().startTask();
 //        }
 
         if ( settingsManager.getLibraryManager().getVisibleLibrariesList()
                 == null
                 || settingsManager.getLibraryManager().getVisibleLibrariesList().
                 size() < 1 ) {
             OpenLibraryManagerCommand.getInstance().execute( null );
         } else {
             new MainScreen().show();
         }
     }
 }
