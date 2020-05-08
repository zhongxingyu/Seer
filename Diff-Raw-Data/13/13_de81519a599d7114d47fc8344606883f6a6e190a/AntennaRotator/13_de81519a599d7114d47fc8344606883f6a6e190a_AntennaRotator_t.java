 package api;
 
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import serial.client.SerialClient;
 
 /**
 * An API for the Antenna Rotator. We are using the Yaesu GS-232A
  * 
  * @author Adam Campbell
  */
 public class AntennaRotator {
 	/**
 	 * The SerialClient used to talk to the rotator via the serial port
 	 */
 	private SerialClient client;
 
 	/**
 	 * The azimuth of the antenna, in degress
 	 */
 	private int azimuth = -1;
 
 	/**
 	 * The elevation of the antenna, in degrees
 	 */
 	private int elevation = -1;
 
 	/**
 	 * The serial port number where the rotator is connected to the server
 	 * computer
 	 */
 	private int serialPortNum;
 
 	/**
 	 * Construct a new antenna rotator to talk to the given client
 	 * 
 	 * @param client
 	 *            The SerialClient used to talk to the rotator via the serial
 	 *            port
 	 * @param serialPortNum
 	 *            The serial port number where the rotator is connected to the
 	 *            serial server computer
 	 */
 	public AntennaRotator(SerialClient client, int serialPortNum) {
 		if(serialPortNum < 0 || serialPortNum > 9){
 			throw new IllegalArgumentException("Invalid serial port number: " + serialPortNum);
 		}
 
 		this.client = client;
 		this.serialPortNum = serialPortNum;
 	}
 
 	/**
 	 * Rotate the antenna to a given azimuth and elevation. Valid ranges are:
 	 * <ul>
 	 * <li><b>[0, 360]</b> for the azimuth
 	 * <li><b>[0, 180]</b> for the elevation
 	 * </ul>
 	 * 
 	 * @param azimuth
 	 *            The azimuth value
 	 * @param elevation
 	 *            The elevation value
 	 * @throws IOException
 	 *             In the event of a read/write error
 	 */
 	public void RotatorSet(int azimuth, int elevation) throws IOException{
 		if(azimuth < 0 || azimuth > 360){
 			throw new IllegalArgumentException("Invalid Azimuth value!");
 		} else if(elevation < 0 || elevation > 180){
 			throw new IllegalArgumentException("Invalid Elevation value!");
 		}
 
 		String cmd = String.format("%dW%03d %03d\\r\n", serialPortNum, azimuth, elevation);
 		client.write(cmd);
 	}
 
 	/**
 	 * Rotate the antenna to a given azimuth. Valid ranges are:
 	 * <ul>
 	 * <li><b>[0, 360]</b> for the azimuth
 	 * </ul>
 	 * 
 	 * @param azimuth
 	 *            The azimuth value
 	 * @throws IOException
 	 *             In the event of a read/write error
 	 */
 	public void RotatorSet(int azimuth) throws IOException{
 		if(azimuth < 0 || azimuth > 360){
 			throw new IllegalArgumentException("Invalid Azimuth value!");
 		}
 
 		String cmd = String.format("%dW%03d\\r\n", serialPortNum, azimuth, elevation);
 		client.write(cmd);
 	}
 
 	/**
 	 * Gets the current azimuth value for the antenna rotator. This method
 	 * <i>does not poll the server</i> for an up to date azimuth value, but
 	 * merely returns the value that was stored during the last server poll.
 	 * Because of this, you must call pollServer() if you wish to get an up to
 	 * date azimuth value.
 	 * 
 	 * @return The azimuth value recorded during the last call to pollServer()
 	 */
 	public int RotatorGetAz(){
 		if(azimuth < 0){
 			throw new IllegalStateException("You must poll the server before getting the azimuth!");
 		}
 
 		return azimuth;
 	}
 
 	/**
 	 * Gets the current elevation value for the antenna rotator. This method
 	 * <i>does not poll the server</i> for an up to date elevation value, but
 	 * merely returns the value that was stored during the last server poll.
 	 * Because of this, you must call pollServer() if you wish to get an up to
 	 * date elevation value.
 	 * 
 	 * @return The elevation value recorded during the last call to pollServer()
 	 */
 	public int RotatorGetEl(){
 		if(elevation < 0){
 			throw new IllegalStateException("You must poll the server before getting the elevation!");
 		}
 
 		return elevation;
 	}
 
 	/**
 	 * Poll the server to update the azimuth and elevation values. This will
 	 * send a "C2" command to the antenna rotator, and the response will be
 	 * parsed to obtain the azimuth and elevation values.
 	 * 
 	 * @throws IOException
 	 *             In the event of a read/write error
 	 */
 	public void pollServer() throws IOException{
 		// Send the C2 command
 		String cmd = String.format("%dC2\\r\n", serialPortNum, azimuth, elevation);
 		client.write(cmd);
 
 		// Get the response and parse it
 		String data = client.getLineOfData();
 		if(data != null){
 			Scanner dataScanner = new Scanner(data);
 
 			// The data comes in as "+AAA+EEE"
 			Pattern dataPattern = Pattern.compile("\\+([0-9]{4})\\+([0-9]{4})");
 			if(dataScanner.hasNext(dataPattern)){
 				try{
 					azimuth = Integer.parseInt(dataScanner.match().group(1));
 					elevation = Integer.parseInt(dataScanner.match().group(2));
 				} catch(Exception e){
 					// If we did something wrong with the pattern
 					azimuth = -1;
 					elevation = -1;
 				}
 			} else{
 				// If the response is malformed
 				azimuth = -1;
 				elevation = -1;
 			}
 			dataScanner.close();
 		} else{
 			// If the read request timed out
 			azimuth = -1;
 			elevation = -1;
 		}
 	}
 	
 	public static void main(String[] args) {
 		SerialClient client = new SerialClient("10.24.223.192", 2809, "joe", "password23"); 
 		AntennaRotator r = new AntennaRotator(client, 0);
 		try { 
 			r.pollServer();
 			System.out.println("Az: " + r.RotatorGetAz() + ", El: " + r.RotatorGetEl());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.exit(0);
 	}
 }
