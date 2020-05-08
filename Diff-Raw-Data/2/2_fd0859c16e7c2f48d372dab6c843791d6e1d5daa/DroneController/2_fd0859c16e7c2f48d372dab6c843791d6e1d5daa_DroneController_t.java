 package com.tngtech.internal.droneapi;
 
 import com.google.inject.Inject;
 import com.tngtech.internal.droneapi.commands.Command;
 import com.tngtech.internal.droneapi.commands.composed.GetConfigurationDataCommand;
 import com.tngtech.internal.droneapi.commands.composed.PlayFlightAnimationCommand;
 import com.tngtech.internal.droneapi.commands.composed.PlayLedAnimationCommand;
 import com.tngtech.internal.droneapi.commands.composed.SetConfigValueCommand;
 import com.tngtech.internal.droneapi.commands.composed.SwitchCameraCommand;
 import com.tngtech.internal.droneapi.components.ErrorListenerComponent;
 import com.tngtech.internal.droneapi.components.ReadyStateListenerComponent;
 import com.tngtech.internal.droneapi.data.Config;
 import com.tngtech.internal.droneapi.data.DroneConfiguration;
 import com.tngtech.internal.droneapi.data.enums.Camera;
 import com.tngtech.internal.droneapi.data.enums.ControllerState;
 import com.tngtech.internal.droneapi.data.enums.DroneVersion;
 import com.tngtech.internal.droneapi.data.enums.FlightAnimation;
 import com.tngtech.internal.droneapi.data.enums.LedAnimation;
 import com.tngtech.internal.droneapi.injection.Context;
 import com.tngtech.internal.droneapi.listeners.ErrorListener;
 import com.tngtech.internal.droneapi.listeners.NavDataListener;
 import com.tngtech.internal.droneapi.listeners.ReadyStateChangeListener;
 import com.tngtech.internal.droneapi.listeners.VideoDataListener;
 import org.apache.log4j.Logger;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import static com.google.common.base.Preconditions.checkState;
 
 public class DroneController
 {
   private static final int NUMBER_OF_THREADS = 1;
 
   private final Logger logger = Logger.getLogger(DroneController.class);
 
   private final ReadyStateListenerComponent readyStateListenerComponent;
 
   private final ErrorListenerComponent errorListenerComponent;
 
   private final DroneStartupCoordinator droneStartupCoordinator;
 
   private final CommandSenderCoordinator commandSender;
 
   private final NavigationDataRetriever navigationDataRetriever;
 
   private final VideoRetrieverP264 videoRetrieverP264;
 
   private final VideoRetrieverH264 videoRetrieverH264;
 
   private final InternalStateWatcher internalStateWatcher;
 
   private ExecutorService executor;
 
   private Config config;
 
   public static DroneController build()
   {
     return Context.getBean(DroneController.class);
   }
 
   @Inject
   public DroneController(ReadyStateListenerComponent readyStateListenerComponent, ErrorListenerComponent errorListenerComponent,
                          DroneStartupCoordinator droneStartupCoordinator, CommandSenderCoordinator commandSenderCoordinator,
                          NavigationDataRetriever navigationDataRetriever, VideoRetrieverP264 videoRetrieverP264,
                          VideoRetrieverH264 videoRetrieverH264, InternalStateWatcher internalStateWatcher)
   {
     this.readyStateListenerComponent = readyStateListenerComponent;
     this.errorListenerComponent = errorListenerComponent;
     this.droneStartupCoordinator = droneStartupCoordinator;
     this.commandSender = commandSenderCoordinator;
     this.navigationDataRetriever = navigationDataRetriever;
     this.videoRetrieverP264 = videoRetrieverP264;
     this.videoRetrieverH264 = videoRetrieverH264;
     this.internalStateWatcher = internalStateWatcher;
   }
 
   public void startAsync(final Config config)
   {
     checkInitializationStateStarted();
     initializeExecutor();
 
     executor.submit(new Runnable()
     {
       @Override
       public void run()
       {
         try
         {
           start(config);
         } catch (Throwable e)
         {
           errorListenerComponent.emitError(e);
         }
       }
     });
   }
 
   public void start(Config config)
   {
     checkInitializationStateStarted();
 
     logger.info("Starting drone controller");
 
     this.config = config;
 
     initializeExecutor();
     droneStartupCoordinator.start(config);
     readyStateListenerComponent.emitReadyStateChange(ReadyStateChangeListener.ReadyState.READY);
   }
 
   private void initializeExecutor()
   {
     executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
   }
 
   public void stop()
   {
     readyStateListenerComponent.emitReadyStateChange(ReadyStateChangeListener.ReadyState.NOT_READY);
     logger.info("Stopping drone controller");
     droneStartupCoordinator.stop();
     executor.shutdownNow();
   }
 
   public boolean isInitialized()
   {
     return droneStartupCoordinator.getState() == ControllerState.READY;
   }
 
   public void addReadyStateChangeListener(ReadyStateChangeListener readyStateChangeListener)
   {
     readyStateListenerComponent.addReadyStateChangeListener(readyStateChangeListener);
   }
 
   public void removeReadyStateChangeListener(ReadyStateChangeListener readyStateChangeListener)
   {
     readyStateListenerComponent.addReadyStateChangeListener(readyStateChangeListener);
   }
 
   public void addErrorListener(ErrorListener errorListener)
   {
     errorListenerComponent.addErrorListener(errorListener);
   }
 
   public void removeErrorListener(ErrorListener errorListener)
   {
     errorListenerComponent.removeErrorListener(errorListener);
   }
 
   public void addNavDataListener(NavDataListener navDataListener)
   {
     navigationDataRetriever.addNavDataListener(navDataListener);
   }
 
   public void removeNavDataListener(NavDataListener navDataListener)
   {
     navigationDataRetriever.removeNavDataListener(navDataListener);
   }
 
   public void addVideoDataListener(VideoDataListener videoDataListener)
   {
     videoRetrieverH264.addVideoDataListener(videoDataListener);
     videoRetrieverP264.addVideoDataListener(videoDataListener);
   }
 
   public void removeVideoDataListener(VideoDataListener videoDataListener)
   {
     videoRetrieverH264.removeVideoDataListener(videoDataListener);
     videoRetrieverP264.removeVideoDataListener(videoDataListener);
   }
 
   public DroneVersion getDroneVersion()
   {
     checkInitializationState();
     return droneStartupCoordinator.getDroneVersion();
   }
 
   public DroneConfiguration getDroneConfiguration()
   {
     checkInitializationState();
     return droneStartupCoordinator.getDroneConfiguration();
   }
 
   public void takeOff()
   {
     checkInitializationState();
 
     logger.debug("Taking off");
     internalStateWatcher.requestTakeOff();
   }
 
   public void land()
   {
     checkInitializationState();
 
     logger.debug("Landing");
     internalStateWatcher.requestLand();
   }
 
   public void emergency()
   {
     checkInitializationState();
 
     logger.debug("Setting emergency");
     internalStateWatcher.requestEmergency();
   }
 
   public void flatTrim()
   {
     checkInitializationState();
 
     logger.debug("Flat trim");
    internalStateWatcher.requestFlatTrim();
   }
 
   public void move(float roll, float pitch, float yaw, float gaz)
   {
     checkInitializationState();
 
     logger.trace(String.format("Moving - roll: %.2f, pitch: %.2f, yaw: %.2f, gaz: %.2f", roll, pitch, yaw, gaz));
     internalStateWatcher.requestMove(roll, pitch, yaw, gaz);
   }
 
   public Future switchCamera(Camera camera)
   {
     checkInitializationState();
 
     logger.debug(String.format("Changing camera to '%s'", camera.name()));
     return executeCommandsAsync(new SwitchCameraCommand(config.getLoginData(), camera), new GetConfigurationDataCommand());
   }
 
   public Future playLedAnimation(LedAnimation ledAnimation, float frequency, int durationSeconds)
   {
     checkInitializationState();
 
     logger.debug(String.format("Playing LED animation '%s'", ledAnimation.name()));
     return executeCommandsAsync(new PlayLedAnimationCommand(config.getLoginData(), ledAnimation, frequency, durationSeconds));
   }
 
   public Future playFlightAnimation(FlightAnimation animation)
   {
     checkInitializationState();
 
     logger.debug(String.format("Playing flight animation '%s'", animation.name()));
     return executeCommandsAsync(new PlayFlightAnimationCommand(config.getLoginData(), animation));
   }
 
   public Future setConfigValue(String key, Object value)
   {
     checkInitializationState();
 
     logger.debug(String.format("Setting config value '%s' to '%s'", key, value.toString()));
     return executeCommandsAsync(new SetConfigValueCommand(config.getLoginData(), key, value), new GetConfigurationDataCommand());
   }
 
   public void executeCommands(Command... commands)
   {
     for (Command command : commands)
     {
       commandSender.executeCommand(command);
     }
   }
 
   public Future executeCommandsAsync(final Command... commands)
   {
     return executor.submit(new Runnable()
     {
       @Override
       public void run()
       {
         executeCommands(commands);
       }
     });
   }
 
   private void checkInitializationState()
   {
     checkState(isInitialized(), "The drone controller is not yet fully initialized");
   }
 
   private void checkInitializationStateStarted()
   {
     checkState(droneStartupCoordinator.getState() == ControllerState.STARTED, "The drone controller has already been initialized");
   }
 }
