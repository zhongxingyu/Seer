 package se.lth.student.eda040.a1.network;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.net.UnknownHostException;
 import java.net.Socket;
 import android.util.Log;
 
 // TODO needs to be synchronized because of connectTo? if they share the socket? But that no good since is.read is blocking....
 public class ClientProtocol {
 	public static byte VIDEO_MODE = 'v';
 	public static int CAMERA_PORT = 8080;
 
 	private byte cameraId;
 	private Socket socket;
 	private InputStream inputStream;
 	private OutputStream outputStream;
 	public ClientProtocol(byte cameraId) {
 		this.cameraId = cameraId;
 	}
 
 	public Image awaitImage() throws IOException{
 		byte[] headerBytes = new byte[5];
 		int bytesRead = 0;
 		int returnValue = 0;
 		Log.d("ClientProtocol", "Start reading header from inputStream");
 		while (bytesRead < 5) {
 			returnValue = inputStream.read(headerBytes, bytesRead, (5 - bytesRead));
 			if (returnValue == -1) { // TODO what is this?
 				throw new IOException("Connection lost");
 			}
 			bytesRead += returnValue;
 		}
 		Log.d("ClientProtocol", "Stopt reading header from inputStream");
 		int imageLen = 0;
 		//imageLen |= (int) (headerBytes[1] << 24);
 		//imageLen |= (int) (headerBytes[2] << 16);
 		//imageLen |= (int) (headerBytes[3] << 8);
 		//imageLen |= (int) (headerBytes[4]);
 		for (int i = 0; i < 4; ++i) {
 			imageLen |= (int) ((headerBytes[1 + i] < 0 ? 256 + headerBytes[i +1] : headerBytes[i + 1]) << (8 * (3 - i)));
 		}
 
 		Log.d("ClientProtocol", "imageLen == " + imageLen);
 		bytesRead = 0;
 		byte[] imageBytes = new byte[imageLen];
 
 		Log.d("ClientProtocol", "Start reading data from inputStream");
 		while (bytesRead < imageLen) {
 			returnValue = inputStream.read(imageBytes, bytesRead, (imageLen - bytesRead));
 			if (returnValue == -1){
 				throw new IOException("Connection lost");
 			}
 			bytesRead += returnValue;
 		}
 		Log.d("ClientProtocol", "Stopt reading data from inputStream");
 
 		long timestamp = 0;
 		for (int i = 0; i < 4; ++i) {
 				timestamp |= (long) ((imageBytes[25 + i] < 0 ? 256 + imageBytes[25 + i] : imageBytes[25 + i]) << (8 * (3 - i)));
 		}
 		// TODO should really the timestampdata be a part of the image to draw?
 		timestamp *= 1000;
 		Log.d("ClientProtocol", "Timestamp == " + timestamp + ", or in HR == " + new Date(timestamp));
 		timestamp |= (long) (imageBytes[29] < 0 ? 256 + imageBytes[29] : imageBytes[29]);
 		return new Image(cameraId, imageBytes, timestamp, ((int) headerBytes[0]) == VIDEO_MODE);
 	}
 
 	public void sendCommand(Command command) throws IOException{
 		outputStream.write(command.getCommand());
 	}
 
 	public byte getCameraId() {
 		return cameraId;
 	}
 
 	public void connectTo(String host) throws IOException, UnknownHostException {
 		Log.d("ClientProtocol", "Pre socket instanciation for host " + host);
 		socket = new Socket(host, CAMERA_PORT);
 		//socket = new Socket("10.0.2.2", 8080);
 		Log.d("ClientProtocol", "Post socket instanciation for host " + host);
 		inputStream = socket.getInputStream();
 		outputStream = socket.getOutputStream();
 		Log.d("ClientProtocol", "Got in and output streams");
 		//notifyAll();
 	}
 
 	public void disconnect() throws IOException {
 		socket.close();
 		inputStream = null;
		outputStream.flush();
 		outputStream = null;
 	}
 }
