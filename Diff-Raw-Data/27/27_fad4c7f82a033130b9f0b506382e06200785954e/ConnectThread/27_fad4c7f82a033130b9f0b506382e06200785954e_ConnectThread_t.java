 
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.activation.CommandObject;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 
 import se.nicklasgavelin.bluetooth.Bluetooth;
 import se.nicklasgavelin.bluetooth.Bluetooth.EVENT;
 import se.nicklasgavelin.bluetooth.BluetoothDevice;
 import se.nicklasgavelin.bluetooth.BluetoothDiscoveryListener;
 import se.nicklasgavelin.sphero.Robot;
 import se.nicklasgavelin.sphero.RobotListener;
 import se.nicklasgavelin.sphero.command.AbortMacroCommand;
 import se.nicklasgavelin.sphero.command.CommandMessage;
 import se.nicklasgavelin.sphero.command.FrontLEDCommand;
 import se.nicklasgavelin.sphero.command.RollCommand;
 import se.nicklasgavelin.sphero.command.SpinLeftCommand;
 import se.nicklasgavelin.sphero.command.SpinRightCommand;
 import se.nicklasgavelin.sphero.command.StabilizationCommand;
 import se.nicklasgavelin.sphero.exception.InvalidRobotAddressException;
 import se.nicklasgavelin.sphero.exception.RobotBluetoothException;
 import se.nicklasgavelin.sphero.macro.MacroObject;
 import se.nicklasgavelin.sphero.macro.command.*;
 import se.nicklasgavelin.sphero.response.ResponseMessage;
 import se.nicklasgavelin.sphero.response.InformationResponseMessage;
 import se.nicklasgavelin.sphero.response.information.DataResponse;
 
 /**
  * Simple test class to test the Sphero API
  * 
  * @author Nicklas Gavelin, nicklas.gavelin@gmail.com, Lule University of Technology
  */
 
 	public class ConnectThread extends Thread implements BluetoothDiscoveryListener, Runnable, RobotListener
 	{
 		// Internal storage
 		private int responses = 0;
 		Robot r;
 		private Bluetooth bt;
		String bluetoothAddress;
 		private boolean stop = false;
 		public ArrayList<Robot> robots;
 		
		public ConnectThread(String bluetoothAddress)
 		{
 			this.robots = new ArrayList<Robot>();
			this.bluetoothAddress = bluetoothAddress;
 		}
 		
 		public Collection<Robot> getRobotArray()	{
 			return robots;
 		}
 
 		
 		void stopThread()
 		{
 			if( bt != null )
 				bt.cancelDiscovery();
 			this.stop = true;
 
 			// Disconnect from all robots and clear the connected list
 			for( Robot r : robots )
 				r.disconnect();
 			robots.clear();
 		}
 		
 		/**
 		 * Here under run there are two options for connecting a sphero, you either connect it by hardcodign the address or by doing a automatic search 
 		 */
 		@Override
 		public void run()
 		{
 			try
 			{
 				// Will perform a bluetooth discovery before connecting to
 				// any devices
 				bt = new Bluetooth( this, Bluetooth.SERIAL_COM );
 //				bt.discover(); // # COMMENT THIS IF UNCOMMENTING THE BELOW AREA #
 				// Uncomment the code below and comment out the bt.discover() line above
 				// to
 				// connect directly to a given Sphero
 
 				// // ## START UNCOMMENT ##
				// String bluetoothAddress = "000666440DB8";
				
 				 BluetoothDevice btd = new BluetoothDevice( bt, "btspp://" +
 				 bluetoothAddress + ":1;authenticate=true;encrypt=false;master=false" );
 				
 				 // Create the robot from the bluetooth device
 				 r = new Robot( btd );
 				
 				 // Try to connect to the robot
 				 if ( r.connect() )
 				 {
 					 // Add ourselves as listeners
 					 r.addListener( this );
 					
 					 // Send a rgb transition command macro
 					 r.rgbTransition( 255, 0, 0, 0, 255, 255, 50 );
 					
 					 // Send a direct command
 					 r.sendCommand( new FrontLEDCommand( 1F ) );
 					 
 					 robots.add(r);
 				 }
 				 // ## END UNCOMMENT ##
 				
 				 /*
 				  * 
 				  * Added for testing, not part of the original file !!
 				  * 
 				  */
 				 
 				 /*
 				  * 
 				  * End of added stuffs
 				  * 
 				  */
 				 
 				// Run forever, euheuheuh!
 //				while( !stop )
 //				{
 //					try
 //					{
 //						Thread.sleep( 5000 );
 //					}
 //					catch( InterruptedException e )
 //					{
 //					}
 //				}
 			}
 			catch( Exception e )
 			{
 				// Failure in searching for devices for some reason.
 				e.printStackTrace();
 			}
 		}
 
 		/*
 		 * *************************************
 		 * BLUETOOTH DISCOVERY STUFF
 		 */
 		int robotNr = 1;
 		public boolean isCompleted = false;
 		/**
 		 * Called when the device search is completed with detected devices
 		 * 
 		 * @param devices The devices detected
 		 */
 		@Override
 		public void deviceSearchCompleted( Collection<BluetoothDevice> devices )
 		{
 			// Device search is completed
 			System.out.println( "Completed device discovery" );
 
 			// Try and see if we can find any Spheros in the found devices
 			for( BluetoothDevice d : devices )
 			{
 				// Check if the Bluetooth device is a Sphero device or not
 				if( Robot.isValidDevice( d ) )
 				{
 					System.out.println( "Found robot " + d.getAddress() );
 					System.out.println("Is robot nr " + robotNr);
 					robotNr++;
 
 					// We got a valid device (Sphero device), connect to it and
 					// have some fun with colors.
 					try
 					{
 						// Create our robot from the Bluetooth device that we got
 						Robot r = new Robot( d );
 
 						// Add ourselves as listeners for the responses
 						r.addListener( this );
 
 						// Check if we can connect
 						if( r.connect() )
 						{
 							// Add robots to our connected robots list
 							robots.add( r );
 
 							System.out.println( "Connected to " + d.getName() + " : " + d.getAddress() );
 							r.rgbTransition( 255, 0, 0, 0, 255, 0, 200 );
 
 							// Send direct command
 							r.sendCommand( new FrontLEDCommand( 1 ) );
 						}
 						else
 							System.err.println( "Failed to connect to robot" );
 					}
 					catch( InvalidRobotAddressException ex )
 					{
 						ex.printStackTrace();
 					}
 					catch( RobotBluetoothException ex )
 					{
 						ex.printStackTrace();
 					}
 				}
 			}
 
 			// Disable the thread and set connected button state
 			if( robots.isEmpty() )
 			{
 				this.stopThread();
 				//TODO
 				//setConnectEnabled( true );
 				//This might be used
 			}
 			isCompleted = true;
 		}
 
 		/**
 		 * Called when the search is started
 		 */
 		@Override
 		public void deviceSearchStarted()
 		{
 			System.out.println( "Started device search" );
 		}
 
 		/**
 		 * Called if something went wrong with the device search
 		 * 
 		 * @param error The error that occurred
 		 */
 		@Override
 		public void deviceSearchFailed( EVENT error )
 		{
 			System.err.println( "Failed with device search: " + error );
 		}
 
 		/**
 		 * Called when a Bluetooth device is discovered
 		 * 
 		 * @param device The device discovered
 		 */
 		@Override
 		public void deviceDiscovered( BluetoothDevice device )
 		{
 			System.out.println( "Discovered device " + device.getName() + " : " + device.getAddress() );
 		}
 
 		/*
 		 * ********************************************
 		 * ROBOT STUFF
 		 */
 
 		/**
 		 * Called when a response is received from a robot
 		 * 
 		 * @param r The robot the event concerns
 		 * @param response The response received
 		 * @param dc The command the response is concerning
 		 */
 		@Override
 		public void responseReceived( Robot r, ResponseMessage response, CommandMessage dc )
 		{
 			System.out.println( "(" + ( ++responses ) + ") Received response: " + response.getResponseCode() + " to message " + dc.getCommand() );
 		}
 
 		/**
 		 * Event that may occur for a robot
 		 * 
 		 * @param r The robot the event concerns
 		 * @param code The event code for the event
 		 */
 		@Override
 		public void event( Robot r, EVENT_CODE code )
 		{
 			System.out.println( "Received event: " + code );
 		}
 		DataResponse dr;
 		@Override
 		public void informationResponseReceived( Robot r, InformationResponseMessage response )
 		{
 		}
 	}
 
