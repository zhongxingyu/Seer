 /**
  * 
  */
 package com.jpii.navalbattle.pavo.io;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.util.OSUtil;
 import com.sun.corba.se.spi.activation.Server;
 
 /**
  * @author MKirkby
  *
  */
 public class PavoServer implements Runnable {
 	ServerSocket socket;
 	Socket client;
 	boolean doing = false;
 	String ipaddress = null;
 	Thread self;
 	BufferedReader reader;
 	PrintWriter output;
 	String clientAddress = null;
 	boolean noInternet = false;
 	public PavoServer() {
 		
 	}
 	
 	public String getSelfIP() {
 		return ipaddress;
 	}
 	
 	public boolean start() {
 		if (doing) {
 			System.out.println("Server already runnning!");
 			return false;
 		}
 		if (ipaddress == null) {
 			Socket s;
 			try {
 				s = new Socket("google.com", 80);
 				ipaddress = (s.getLocalAddress().getHostAddress());
 				if (ipaddress == null) {
 					noInternet = true;
 					ipaddress = "None";
 				}
 				s.close();
 			}
 			catch (Throwable t) {
 				
 			}
 			if (!noInternet)
 				Game.Settings.currentNetworkState = NetworkState.CONNECTED_TO_NETWORK_NO_INTERNET;
 			System.out.println("Your ip address is: " + ipaddress);
 		}
 		clientAddress = null;
 		doing = true;
 		self = new Thread(this);
 		self.start();
 		return true;
 	}
 
 	public void run() {
 		try {
 			socket = new ServerSocket(670);
 		} catch (IOException e2) {
 			if (!noInternet)
 				Game.Settings.currentNetworkState = NetworkState.CONNECTED_TO_INTERNET_NO_NETWORK;
 			else
 				Game.Settings.currentNetworkState = NetworkState.NO_CONNECTION;
 			e2.printStackTrace();
 		}
 		try {
 			client = socket.accept();
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 		if (clientAddress == null) {
 			clientAddress = client.getInetAddress().toString();
 			if (clientAddress.indexOf("/") > -1) {
 				clientAddress = clientAddress.replace("/", "");
 			}
 			System.out.println("The server connected to: " + clientAddress);
 		}
 		PrintWriter out = null;
 		try {
 			out = new PrintWriter(client.getOutputStream(),true);
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 		InputStream is = null;
 		try {
 			is = client.getInputStream();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);
 		while (doing) {
 			serverIsWriting = true;
			out.println(sendTmp);
			//out.println(OSUtil.xorEncode(sendTmp, "Aj390jnRIn9wO2o3409WJofn"));
 			sendTmp = "";
 			serverIsWriting = false;
 			try {
 				Thread.sleep(50);
 			}
 			catch (Throwable t) {
 				
 			}
             try {
             	String rl = br.readLine();
            	//rl = OSUtil.xorDecode(rl, "Aj390jnRIn9wO2o3409WJofn");
             	if (rl.equals("HELLO")) {
             		onClientConnect();
             	}
             	else if (rl != null && !rl.equals(""))
             		onMessageRecieved(rl);
 			} catch (IOException e) {
				if (e.getMessage().equals("Connection reset") || e.getMessage().equals("Software caused connection abort: recv failed")) {
 					doing = false;
 					break;
 				}
 				e.printStackTrace();
 			}
 		}
 		System.out.println("Client disconnected.");
 		halt();
 	}
 	boolean sendLock = false;
 	
 	public void onClientConnect() {
 		
 	}
 	
 	String sendTmp = "";
 	boolean serverIsWriting = false;
 	public void send(String msg) {
 		while (serverIsWriting) {
 			
 		}
 		sendTmp = msg;
 	}
 	
 	public void onMessageRecieved(String msg) {
 		System.out.println("Recieved from client (the requestor): " + msg);
 	}
 	
 	public void halt() {
 		doing = false;
 		try {
 			Thread.sleep(250);
 		}
 		catch (Throwable t) {
 			
 		}
 		try {
 			client.close();
 		}
 		catch (Throwable t) {
 			
 		}
 		try {
 			socket.close();
 		}
 		catch (Throwable t) {
 			
 		}
 		client = null;
 		socket = null;
 		try{
 			self.destroy();
 		}
 		catch (Throwable t) {
 			
 		}
 		self = null;
 	}
 	
 	
 }
