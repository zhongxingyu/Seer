 package net.intensicode;
 
 import net.intensicode.core.GameSystem;
 import net.intensicode.me.*;
 
 import javax.microedition.lcdui.Display;
 import javax.microedition.midlet.*;
 
 public abstract class IntensiME extends MIDlet implements PlatformContext, SystemContext
     {
     protected IntensiME()
         {
         }
 
     protected void initGameSystem()
         {
         if ( isGameSystemCreated() ) return;
 
         createGameViewAndGameSystem();
 
         final IntensiGameHelper helper = new IntensiGameHelper( myGameSystem );
         helper.initGameSystemFromConfigurationFile();
         }
 
     // From PlatformContext
 
     public final long compatibleTimeInMillis()
         {
         return System.currentTimeMillis();
         }
 
     // From SystemContext
 
     public final GameSystem system()
         {
         return myGameSystem;
         }
 
     public boolean useOpenglIfPossible()
         {
         return false;
         }
 
     public final ConfigurationElementsTree getPlatformValues()
         {
         return ConfigurationElementsTree.EMPTY;
         }
 
     public final ConfigurationElementsTree getSystemValues()
         {
         return myGameSystem.getSystemValues();
         }
 
     public ConfigurationElementsTree getApplicationValues()
         {
         return ConfigurationElementsTree.EMPTY;
         }
 
     public final void loadConfigurableValues()
         {
         final IntensiGameHelper helper = new IntensiGameHelper( myGameSystem );
         helper.loadConfiguration( getPlatformValues() );
         helper.loadConfiguration( getSystemValues() );
         helper.loadConfiguration( getApplicationValues() );
         }
 
     public final void saveConfigurableValues()
         {
         final IntensiGameHelper helper = new IntensiGameHelper( myGameSystem );
         helper.saveConfiguration( getPlatformValues() );
         helper.saveConfiguration( getSystemValues() );
         helper.saveConfiguration( getApplicationValues() );
         }
 
     public void onFramesDropped()
         {
         // Default implementation does nothing..
         }
 
     public void onDebugTriggered()
         {
         IntensiGameHelper.toggleDebugScreen( myGameSystem );
         }
 
     public void onCheatTriggered()
         {
         IntensiGameHelper.toggleCheatScreen( myGameSystem );
         }
 
     public void onPauseApplication()
         {
         // Default implementation does nothing..
         }
 
     public void onDestroyApplication()
         {
         // Default implementation does nothing..
         }
 
     public final void triggerConfigurationMenu()
         {
         throw new RuntimeException( "nyi" );
         }
 
     public void terminateApplication()
         {
         destroyApp( true );
 
         //#if RUNME
         System.exit( 0 );
         //#endif
         }
 
     // From MIDlet
 
     public final void startApp() throws MIDletStateChangeException
         {
         try
             {
             initGameSystem();
             setDisplay( myGameView );
             }
         catch ( final Exception e )
             {
             //#if DEBUG
             e.printStackTrace();
             //#endif
 
             throw new MIDletStateChangeException( e.toString() );
             }
         }
 
     public final void pauseApp()
         {
         setDisplay( null );
         notifyPaused();
         }
 
     public final void destroyApp( final boolean unconditional )
         {
         setDisplay( null );
         notifyDestroyed();
         }
 
     // Implementation
 
     private boolean isGameSystemCreated()
         {
         return myGameSystem != null;
         }
 
     private synchronized void createGameViewAndGameSystem()
         {
         final MicroGameSystem system = new MicroGameSystem( this, this );
         final MicroGameEngine engine = new MicroGameEngine( system );
         final MicroGameView view = new MicroGameView();
         final MicroCanvasGraphics graphics = new MicroCanvasGraphics();
         final MicroResourcesManager resources = new MicroResourcesManager( this.getClass() );
         //#ifdef TOUCH
         final MicroTouchHandler touch = new MicroTouchHandler( system );
         //#endif
         final MicroKeysHandler keys = new MicroKeysHandler( view );
         final MicroAudioManager audio = new MicroAudioManager( resources );
         final MicroStorageManager storage = new MicroStorageManager();
 
         //#if SENSORS
         final MicroSensorsManager sensors = new MicroSensorsManager();
         //#endif
 
        //#if TRACKBALL
        final MicroTrackballController trackball = new MicroTrackballController();
        //#endif
 
         view.keys = keys;
         //#if TOUCH
         view.touch = touch;
         //#endif
        view.analog = trackball;
         view.context = this;
         view.system = system;
         view.graphics = graphics;
 
         system.resources = resources;
         system.graphics = graphics;
         system.storage = storage;
        //#if TRACKBALL
        system.trackball = trackball;
        //#endif
         //#if SENSORS
         system.sensors = sensors;
         //#endif
         system.engine = engine;
         system.screen = view;
         //#ifdef TOUCH
         system.touch = touch;
         //#endif
         system.audio = audio;
         system.keys = keys;
 
         myGameView = view;
         myGameSystem = system;
         }
 
     private void setDisplay( final MicroGameView aDisplay )
         {
         Display.getDisplay( this ).setCurrent( aDisplay );
         }
 
     private MicroGameView myGameView;
 
     private MicroGameSystem myGameSystem;
     }
