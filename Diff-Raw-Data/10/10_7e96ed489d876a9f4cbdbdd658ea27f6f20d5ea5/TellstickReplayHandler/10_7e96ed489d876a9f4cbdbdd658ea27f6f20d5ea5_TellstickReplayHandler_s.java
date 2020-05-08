 package Compiler.TellstickReplay;
 
 import Compiler.TellstickReplay.TellstickSystems;
 import Compiler.TellstickReplay.TellstickScheduler;
 
 import com.sun.jna.Native;

 /**
  * @author Per Fransman
  *
  */
 public class TellstickReplayHandler {
 	
 	private CLibrary libTelldus;
 	private TellstickScheduler scheduler;
 	private boolean isRunning;
 	private boolean canRunGui;
 
 	/**
 	 * Description: Main constructor.
 	 * @param os
 	 */
 	public TellstickReplayHandler(TellstickSystems os) {
 		try {
 			switch (os){
 				case Linux:
 					this.libTelldus = (CLibrary)Native.loadLibrary("telldus-core", CLibrary.class);
 					this.setRunning(true);
 					this.setCanRunGui(false);
 					break;
 				case Windows7:
 					this.libTelldus = (CLibrary)Native.loadLibrary("TelldusCore", CLibrary.class);
 					this.setRunning(true);
 					this.setCanRunGui(true);
 					break;
 				case Windows8:
 					this.libTelldus = (CLibrary)Native.loadLibrary("TelldusCore", CLibrary.class);
 					this.setRunning(true);
 					this.setCanRunGui(true);
 					break;
 				case Mac:
 					System.out.println("Operating system not supported. Yet.");
 					break;
 				default:
 					System.out.println("Operating system not supported. Yet.");
 					break;
 			}
 		}
 		catch( Exception e ) {
 			//throw e;
 			System.out.println( e );
 		}
 		
 		try{
 			//TODO: access database and read devices.
 			this.setScheduler(new TellstickScheduler());
 			
 		}
 		catch( Exception e ){
 			System.out.println( e );
 		}
 	}
 	
 	/**
 	 * Description: Method 'TellstickReplaySchedulerHandler'
 	 * @param method
 	 */
 	public void TellstickReplayScheduleHandler(TellstickMethods method){
 		switch( method ) {
 			case TURNON:
 				break;
 			case TURNOFF:
 				break;
 			default:
 				break;	
 		}
 	}
 	
 	/**
 	 * Method: Method 'CheckDeviceFeatures' checks supplied device id for supported features.
 	 * @param id
 	 * @return
 	 */
 	public boolean CheckDeviceFeatures( int id ){
 		boolean value = false;
 		try {
 			libTelldus.tdInit();
 			
 			libTelldus.tdClose();
 			value = true;
 		}
 		catch( Exception e ){
 			value = false;
 			//throw e;
 			System.out.println( e );
 		}
 		return value;
 	}
 	
 	/**
 	 * Description: Method 'DimDeviceById' dims the device with the supplied level.
 	 * @param id
 	 * @param level
 	 * @return 'boolean' value of 'true' if the device was succesfully dimmed to the applied level or
 	 * 'false' if it was unable to dim the device.
 	 */
 	public boolean DimDeviceById( int id, int level ) {
 		boolean value = false;
 		try {
 			libTelldus.tdInit();
 			
 			libTelldus.tdClose();
 			value = true;
 		}
 		catch( Exception e ){
 			value = false;
 			//throw e;
 			System.out.println( e );
 		}
 		return value;
 	}
 	
 	/**
 	 * Description: Method 'DimDeviceByGroup' dims all devices in the group and by the supplied level.
 	 * @param name
 	 * @param level
 	 * @return 'boolean' value of 'true' if the group was succesfully dimmed to the applied level or
 	 * 'false' if it was unable to dim the group.
 	 */
 	public boolean DimDeviceByGroup( String name, int level ) {
 		boolean value = false;
 		try {
 			libTelldus.tdInit();
 			
 			libTelldus.tdClose();
 			value = true;
 		}
 		catch( Exception e ){
 			value = false;
 			//throw e;
 			System.out.println( e );
 		}
 		return value;
 	}
 	
 	/**
 	 * Description: Method 'TurnDeviceOffById' uses the device id to access to turn it off.  
 	 * @param id: Parameter 'id' is the device id of type 'int'.
 	 * @return: 'boolean' value of 'true' if the device was successfully turned off or 
 	 * 'false' if it was unable to turn the device off.
 	 */
 	public boolean TurnDeviceOffById( int id ){
 		boolean value = false;
 		try {
 			libTelldus.tdInit();
 			
 			libTelldus.tdClose();
 			value = true;
 		}
 		catch( Exception e ){
 			value = false;
 			//throw e;
 			System.out.println( e );
 		}
 		return value;
 	}
 	
 	/**
 	 * Description: 'TurnDeviceOnById'. Turns on a device by specifying its internal ID.
 	 * @param id The device ID to used for identifing correct device.
 	 * @return A 'boolean' value will be return 'true' for being turned on and 'false' for failure.
 	 */
 	public boolean TurnDeviceOnById( int id ){
 		boolean value = false;
 		try {
 			libTelldus.tdInit();
 			
 			libTelldus.tdClose();
 			value = true;
 		}
 		catch( Exception e ){
 			value = false;
 			//throw e;
 			System.out.println( e );
 		}
 		return value;
 	}
 
 	/**
 	 * @return the boolean isRunning.
 	 */
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	/**
 	 * @param isRunning the value to set.
 	 */
 	public void setRunning(boolean isRunning) {
 		this.isRunning = isRunning;
 	}
 	
 	/**
 	 * @return the boolean CanRunGui.
 	 */
 	public boolean getCanRunGui() {
 		return canRunGui;
 	}
 
 	/**
 	 * @param canRunGui the value to set.
 	 */
 	public void setCanRunGui(boolean canRunGui) {
 		this.canRunGui = canRunGui;
 	}
 
 	/**
 	 * @return the scheduler.
 	 */
 	public TellstickScheduler getScheduler() {
 		return scheduler;
 	}
 
 	/**
 	 * @param scheduler the scheduler to set.
 	 */
 	public void setScheduler(TellstickScheduler scheduler) {
 		this.scheduler = scheduler;
 	}
 }
