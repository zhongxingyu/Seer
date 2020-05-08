 package sw901e12;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.microedition.io.Connector;
 import javax.realtime.AperiodicParameters;
 import javax.realtime.PeriodicParameters;
 import javax.realtime.PriorityParameters;
 import javax.realtime.RelativeTime;
 import javax.safetycritical.Mission;
 import javax.safetycritical.StorageParameters;
 import javax.safetycritical.annotate.Level;
 import javax.safetycritical.annotate.SCJAllowed;
 import javax.safetycritical.io.SimplePrintStream;
 
 import sw901e12.comm.ModulePingerFactory;
 import sw901e12.handlers.APEHFailedModuleRoutine;
 import sw901e12.handlers.PEHModulePinger;
 import sw901e12.handlers.PEHModuleResponseChecker;
 
 public class WatchdogMission extends Mission {
 
 	private ModulePingerFactory modulePingerFactory;
 	private Module[] slaves;
 	private SimplePrintStream console;
 	
 	private APEHFailedModuleRoutine moduleFailHandler;
 	private PEHModulePinger modulePingHandler;
 	private PEHModuleResponseChecker moduleResponseChecker;
 	
 	@Override
 	@SCJAllowed(Level.SUPPORT)
 	protected void initialize() {
 		initializeModulePingerFactory();
 		initializeSlaveModules();
         initializeConsole();
         
         initializeAperiodicFailedModuleRoutineHandler();
         initializePeriodicModulePingerHandler();
         initializePeriodicModuleResponseCheckHandler();
 	}
 	
 	@SCJAllowed(Level.SUPPORT)
 	protected void initializeModulePingerFactory() {
 		modulePingerFactory = ModulePingerFactory.CreateEnvironmentSpecificModuleFactory();
 	}
 	
 	@SCJAllowed(Level.SUPPORT)
 	protected void initializeSlaveModules() {
 		slaves = new Module[1];
 		
		slaves[0] = Module.ModuleWithNameAddressAndPinger("Ultrasonic Sensor", 0x01, modulePingerFactory.CreateModulePingerOnI2CAddress(0x01));
 	}
 	
 	private void initializeConsole() {
 		OutputStream os = null;
         try {
             os = Connector.openOutputStream("console:");
         } catch (IOException e) {
             throw new Error("No console available");
         }
         
         console = new SimplePrintStream(os);
 	}
 
 	private void initializePeriodicModulePingerHandler() {
 		final int PING_HANDLER_PRIORITY = 10;
 		final int PING_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 1024;
 		final int PING_HANDLER_SCOPE_SIZE_IN_BYTES = 512;
 		final int PING_HANDLER_RELEASE_PERIOD_IN_MS = 1500;
 		
 		PriorityParameters modulePingPriority = new PriorityParameters(PING_HANDLER_PRIORITY);
 		PeriodicParameters modulePingParams = new PeriodicParameters(new RelativeTime(0, 0), new RelativeTime(PING_HANDLER_RELEASE_PERIOD_IN_MS, 0));
 		StorageParameters modulePingStorage = new StorageParameters(PING_HANDLER_BACKING_STORE_SIZE_IN_BYTES,
 															   new long[] { PING_HANDLER_SCOPE_SIZE_IN_BYTES }, 0, 0);
 		
 		modulePingHandler = new PEHModulePinger(
 				modulePingPriority,
 				modulePingParams, 
 				modulePingStorage, 
 				0,
 				slaves,
 				console);
 		
 		modulePingHandler.register();	
 	}
 	
 	private void initializeAperiodicFailedModuleRoutineHandler() {
 		final int FAILED_MODULE_HANDLER_PRIORITY = 14;
 		final int FAILED_MODULE_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 1024;
 		final int FAILED_MODULE_HANDLER_SCOPE_SIZE_IN_BYTES = 512;
 		
 		PriorityParameters moduleFailPriority = new PriorityParameters(FAILED_MODULE_HANDLER_PRIORITY);
 		AperiodicParameters moduleFailParams = new AperiodicParameters(null, null);
 		StorageParameters moduleFailStorage = new StorageParameters(FAILED_MODULE_HANDLER_BACKING_STORE_SIZE_IN_BYTES,
 															  new long[] { FAILED_MODULE_HANDLER_SCOPE_SIZE_IN_BYTES }, 0, 0);
 		
 		moduleFailHandler = new APEHFailedModuleRoutine(moduleFailPriority, moduleFailParams, moduleFailStorage);
 		moduleFailHandler.register();
 	}
 	
 	private void initializePeriodicModuleResponseCheckHandler() {
 		final int RESPONSE_CHECK_HANDLER_PRIORITY = 10;
 		final int RESPONSE_CHECK_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 1024;
 		final int RESPONSE_CHECK_HANDLER_SCOPE_SIZE_IN_BYTES = 512;
 		final int RESPONSE_CHECK_HANDLER_RELEASE_PERIOD_IN_MS = 1500;
 		
 		PriorityParameters moduleResponseCheckPriority = new PriorityParameters(RESPONSE_CHECK_HANDLER_PRIORITY);
 		PeriodicParameters moduleResponseCheckParams = new PeriodicParameters(new RelativeTime(0, 0), new RelativeTime(RESPONSE_CHECK_HANDLER_RELEASE_PERIOD_IN_MS, 0));
 		StorageParameters moduleResponseCheckStorage = new StorageParameters(RESPONSE_CHECK_HANDLER_BACKING_STORE_SIZE_IN_BYTES,
 																		new long[] { RESPONSE_CHECK_HANDLER_SCOPE_SIZE_IN_BYTES }, 0, 0);
 		
 		moduleResponseChecker = new PEHModuleResponseChecker(
 				moduleResponseCheckPriority,
 				moduleResponseCheckParams,
 				moduleResponseCheckStorage,
 				0,
 				slaves,
 				console,
 				moduleFailHandler);
 		
 		moduleResponseChecker.register();
 		
 	}
 	
 	@Override
 	@SCJAllowed
 	public long missionMemorySize() {
 		return 100000;
 	}
 
 }
