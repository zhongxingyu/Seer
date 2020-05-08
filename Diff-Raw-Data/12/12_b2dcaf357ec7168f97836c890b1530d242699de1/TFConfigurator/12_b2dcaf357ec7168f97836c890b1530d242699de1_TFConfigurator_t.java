 package hardware;
 
 import com.tinkerforge.BrickMaster;
 import com.tinkerforge.IPConnection;
 import com.tinkerforge.IPConnection.ConnectedListener;
 import com.tinkerforge.IPConnection.EnumerateListener;
 import com.tinkerforge.NotConnectedException;
 import com.tinkerforge.TimeoutException;
 
 public class TFConfigurator implements EnumerateListener, ConnectedListener {
 	private IPConnection ipcon;
 	private BrickMaster master;
 
 	public TFConfigurator(IPConnection ipcon) {
 		this.ipcon = ipcon;
 	}
 
 	@Override
 	public void connected(short connectReason) {
 		if (connectReason == IPConnection.CONNECT_REASON_AUTO_RECONNECT) {
 			// connection was lost but reconnect was possible -> reconfigure the bricks/bricklets
 			while (true) {
 				try {
 					ipcon.enumerate();
 					break;
 				} catch (NotConnectedException e) {
 					System.out.println("");
 				}
 
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException ei) {
 				}
 			}
 		}
 	}
 
 	@Override
 	public void enumerate(String uid, String connectedUid, char position,
 			short[] hardwareVersion, short[] firmwareVersion,
 			int deviceIdentifier, short enumerationType) {
 
		// configure bricks on first connect (ENUMERATION_TYPE_CONNECTED) 
		// or configure bricks if enumerate was raised externally (ENUMERATION_TYPE_AVAILABLE)
 		if (enumerationType == IPConnection.ENUMERATION_TYPE_CONNECTED
 				|| enumerationType == IPConnection.ENUMERATION_TYPE_AVAILABLE) {
 			
 			// configure master brick
 			if (deviceIdentifier == BrickMaster.DEVICE_IDENTIFIER) {
 				master = new BrickMaster(connectedUid, ipcon);
 				try {
 					master.setWifiPowerMode(BrickMaster.WIFI_POWER_MODE_LOW_POWER);
 				} catch (TimeoutException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (NotConnectedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
