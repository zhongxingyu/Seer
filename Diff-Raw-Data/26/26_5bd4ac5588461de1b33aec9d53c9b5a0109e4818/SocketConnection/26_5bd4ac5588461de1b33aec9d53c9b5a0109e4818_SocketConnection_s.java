 package nl.rug.snoozzz;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.List;
 
 import objects.Movement;
 import android.util.Log;
 
 public class SocketConnection extends Thread implements Connection {
 
 	private Socket socket;
 	// private PrintWriter output;
 	private ObjectOutputStream output;
 	private BufferedReader input;
 
 	public SocketConnection(String ip, int port) {
 
 		try {
 
 			Log.d("Ip:", ip);
 
 			socket = new Socket(InetAddress.getByName(ip), port);
 
 			// OutputStream out = s.getOutputStream();
 
 			// output = new PrintWriter(out);
 
 			output = new ObjectOutputStream(socket.getOutputStream());
 
 			input = new BufferedReader(new InputStreamReader(
 					socket.getInputStream()));
 
 		} catch (UnknownHostException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		}
 
 	}
 
 	@Override
 	public boolean sendToServer(String message) {
 		
 		if (socket.isConnected()) {
 			
 			try {
 				
 				output.writeObject(message);
 				
 				return true;
 				
 			} catch (IOException e) {
 				
 				e.printStackTrace();
 			
 				return false;
 		
 			}
 		
 		}
 		
 		return false;
 		
 		// output.println(message);
 	}
 
 	@Override
 	public boolean sendToServer(List<Movement> movements) {
 		try {
 
 			output.writeObject(movements);
 
 			return true;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	public boolean connect() {
 
 		if (socket != null) {
 
 			start();
 
 			return true;
 
 		}
 
 		return false;
 
 	}
 
 	public void run() {
 
 		if (socket != null) {
 
 			try {
 
 				String st = "";
 
 				while ((st = input.readLine()) != null) {
 
 					Log.d("Input!", st);
 
 				}
 
 				socket.close();
 
 			} catch (IOException e) {
 
 				e.printStackTrace();
 
 			}
 		}
 
 	}
 
 	@Override
 	public boolean disconnect() {
 		
 		if (socket != null) {
 
 			try {
 
 				socket.shutdownInput();
 
 				input.close();
 
 				output.close();
 
 				socket.shutdownOutput();
 
 			} catch (IOException e) {
 
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 
 			}
 
 		}
 
 		return true;
 
 	}
 
 }
