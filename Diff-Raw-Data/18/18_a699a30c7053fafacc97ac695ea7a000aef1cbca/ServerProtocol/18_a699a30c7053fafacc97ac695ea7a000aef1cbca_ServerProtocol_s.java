 package network;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 public class ServerProtocol {
 	private static final int MSG_LEN = 1;
	public static final byte VIDEO_MODE = 1;
	public static final byte IDLE_MODE = 2;
	public static final byte DISCONNECT = 3;
 
 	private InputStream input;
 	private OutputStream output;
 	private Socket socket;
 
 	public ServerProtocol(Socket socket) {
 		this.socket = socket;
 		try {
 			this.input = socket.getInputStream();
 			this.output = socket.getOutputStream();
 		} catch (IOException ie) {
 			System.err.println("Could not get streams from socket.");
 		}
 	}
 
 	public void sendImage(Image image) {
 		try {
 			socket.getOutputStream().write(image.toBytes());
 		} catch (IOException e) {
 			System.err.println("Could not send picture");
 		}
 	}
 
 	public byte awaitCommand() throws IOException {
 	    byte[] buff = new byte[MSG_LEN];
 	    int readBytes = 0;
 	    System.out.println("reading");
 	    readBytes = input.read(buff, 0, MSG_LEN);
 	    System.out.println("rb: "+ readBytes);
 	    if (readBytes == -1)
 		throw new IOException("connection dropped");
 	    return buff[0];
 	}
 }
