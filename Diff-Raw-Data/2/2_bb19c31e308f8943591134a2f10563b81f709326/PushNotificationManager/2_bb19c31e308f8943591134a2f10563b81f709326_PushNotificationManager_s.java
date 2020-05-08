 package javapns.back;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 
 import javapns.data.Device;
 import javapns.data.PayLoad;
 import javapns.exceptions.DuplicateDeviceException;
 import javapns.exceptions.NullDeviceTokenException;
 import javapns.exceptions.NullIdException;
 import javapns.exceptions.UnknownDeviceException;
 
 import javax.net.ssl.SSLSocket;
 
 
 /**
  * The main class used to send notification and handle a connection to Apple SSLServerSocket
  * @author Maxime Peron
  *
  */
 public class PushNotificationManager {
 
 	/* Singleton pattern */
 	private static PushNotificationManager instance;
 	/* The always connected SSLSocket */
 	private SSLSocket socket;
 
 	/**
 	 * Singleton pattern implementation
 	 * @return the PushNotificationManager instance
 	 */
 	public static PushNotificationManager getInstance(){
 		if (instance == null){
 			instance = new PushNotificationManager();
 		}
 		return instance;
 	}
 
 	/**
 	 * Private constructor
 	 */
 	private PushNotificationManager(){}
 	
 	/**
 	 * Initialize the connection and create a SSLSocket
 	 * @param appleHost the Apple ServerSocket host
 	 * @param applePort the Apple ServerSocket port
 	 * @param keyStorePath the path to the keystore
 	 * @param keyStorePass the keystore password
 	 * @param keyStoreType the keystore type
 	 * @throws UnrecoverableKeyException
 	 * @throws KeyManagementException
 	 * @throws KeyStoreException
 	 * @throws NoSuchAlgorithmException
 	 * @throws CertificateException
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public void initializeConnection(String appleHost, int applePort, String keyStorePath, String keyStorePass, String keyStoreType) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException{
 		SSLConnectionHelper connectionHelper = new SSLConnectionHelper(appleHost, applePort, keyStorePath, keyStorePass, keyStoreType);
 		this.socket = connectionHelper.getSSLSocket();
 	}
 	
 	/**
 	 * Close the SSLSocket connection
 	 * @throws IOException
 	 */
 	public void stopConnection() throws IOException{
 		this.socket.close();
 	}
 
 	/**
 	 * Send a notification (Payload) to the given device
 	 * @param device the device to be notified
 	 * @param payload the payload to send
 	 * @throws UnrecoverableKeyException
 	 * @throws KeyManagementException
 	 * @throws KeyStoreException
 	 * @throws NoSuchAlgorithmException
 	 * @throws CertificateException
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public void sendNotification(Device device, PayLoad payload) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException{
 		byte[] message = getMessage(device.getToken(), payload);
 		this.socket.getOutputStream().write(message);
 		this.socket.getOutputStream().flush();
 	}
 
 	/**
 	 * Add a device
 	 * @param id The device id
 	 * @param token The device token
 	 * @throws DuplicateDeviceException
 	 * @throws NullDeviceTokenException 
 	 * @throws NullIdException 
 	 */
 	public void addDevice(String id, String token) throws DuplicateDeviceException, NullIdException, NullDeviceTokenException{
 		DeviceFactory.getInstance().addDevice(id, token);
 	}
 
 	/**
 	 * Get a device according to his id
 	 * @param id The device id
 	 * @return The device
 	 * @throws UnknownDeviceException
 	 * @throws NullIdException 
 	 */
 	public Device getDevice(String id) throws UnknownDeviceException, NullIdException{
 		return DeviceFactory.getInstance().getDevice(id);
 	}
 
 	/**
 	 * Remove a device
 	 * @param id The device id
 	 * @throws UnknownDeviceException
 	 * @throws NullIdException
 	 */
 	public void removeDevice(String id) throws UnknownDeviceException, NullIdException{
		DeviceFactory.getInstance().getDevice(id);
 	}
 	
 	/**
 	 * Set the proxy if needed
 	 * @param host the proxyHost
 	 * @param port the proxyPort
 	 */
 	public void setProxy(String host, String port){
 		System.setProperty("http.proxyHost", host);
 		System.setProperty("http.proxyPort", port);
 
 		System.setProperty("https.proxyHost", host);
 		System.setProperty("https.proxyPort", port);
 	}
 
 	/**
 	 * Compose the Raw Interface that will be sent through the SSLSocket
 	 * A notification message is
 	 * COMMAND | TOKENLENGTH | DEVICETOKEN | PAYLOADLENGTH | PAYLOAD
 	 * See page 30 of Apple Push Notification Service Programming Guide
 	 * @param deviceToken the deviceToken
 	 * @param payload the payload
 	 * @return the byteArray to write to the SSLSocket OutputStream
 	 * @throws IOException
 	 */
 	private static byte[] getMessage(String deviceToken, PayLoad payload) throws IOException{
 		// First convert the deviceToken (in hexa form) to a binary format
 		byte[] deviceTokenAsBytes = new byte[deviceToken.length() / 2];
 		deviceToken = deviceToken.toUpperCase();
 		int j = 0;
 		for (int i = 0; i < deviceToken.length(); i+=2) {
 			String t = deviceToken.substring(i, i+2);
 			int tmp = Integer.parseInt(t, 16);
 			deviceTokenAsBytes[j++] = (byte)tmp;
 		}
 
 		// Create the ByteArrayOutputStream which will contain the raw interface
 		int size = (Byte.SIZE/Byte.SIZE) + (Character.SIZE/Byte.SIZE) + deviceTokenAsBytes.length + (Character.SIZE/Byte.SIZE) + payload.getPayloadAsBytes().length; 
 		ByteArrayOutputStream bao = new ByteArrayOutputStream(size);
 
 		// Write command to ByteArrayOutputStream
 		byte b = 0;
 		bao.write(b);
 
 		// Write the TokenLength as a 16bits unsigned int, in big endian
 		int tl = deviceTokenAsBytes.length;
 		bao.write((byte) (tl & 0xFF00) >> 8);
 		bao.write((byte) (tl & 0xFF));
 
 		// Write the Token in bytes
 		bao.write(deviceTokenAsBytes);
 
 		// Write the PayloadLength as a 16bits unsigned int, in big endian
 		int pl = payload.getPayloadAsBytes().length;
 		bao.write((byte) (pl & 0xFF00) >> 8);
 		bao.write((byte) (pl & 0xFF));
 
 		// Finally write the Payload
 		bao.write(payload.getPayloadAsBytes());
 
 		// Return the ByteArrayOutputStream as a Byte Array
 		return bao.toByteArray();
 	}
 
 }
