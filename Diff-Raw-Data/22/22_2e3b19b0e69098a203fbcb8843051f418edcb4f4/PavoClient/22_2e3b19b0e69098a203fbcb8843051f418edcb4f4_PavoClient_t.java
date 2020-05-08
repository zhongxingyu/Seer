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
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.gui.MessageBox;
 import com.jpii.navalbattle.pavo.gui.MessageBoxIcon;
 import com.jpii.navalbattle.util.OSUtil;
 
 /**
  * @author MKirkby
  *
  */
 public class PavoClient implements Runnable{
 	Socket socket;
 	PrintWriter pw;
 	String ip;
 	boolean doing = false;
 	Thread thread;
 	InetAddress address;
 	String selfIP;
 	boolean internetConnected = true;
 	public PavoClient(String ipaddress) {
 		ip = ipaddress;
 	}
 	
 	public boolean start() {
 		address = null;
 		try {
 			address = InetAddress.getByName(ip);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		Socket s;
 		try {
 			s = new Socket("google.com", 80);
 			selfIP = (s.getLocalAddress().getHostAddress());
 			if (selfIP == null) {
 				internetConnected = false;
 				selfIP = "None";
 			}
 			s.close();
 		}
 		catch (Throwable t) {
 			
 		}
 		if (!internetConnected)
 			Game.Settings.currentNetworkState = NetworkState.CONNECTED_TO_NETWORK_NO_INTERNET;
         try {
 			socket = new Socket(address, 670);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         doing = true;
 		thread = new Thread(this);
 		thread.start();
 		return true;
 	}
 	
 	public void run() {
         InputStream is = null;
 		try {
 			is = socket.getInputStream();
 		} catch (IOException e) {
 			if (internetConnected)
 				Game.Settings.currentNetworkState = NetworkState.CONNECTED_TO_INTERNET_NO_NETWORK;
 			else
 				Game.Settings.currentNetworkState = NetworkState.NO_CONNECTION;
 			e.printStackTrace();
 		}
         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);
         OutputStream os = null;
         PrintWriter pw = null;
 		try {
 			pw = new PrintWriter(socket.getOutputStream(),true);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		boolean entry = false;
         //OutputStreamWriter osw = new OutputStreamWriter(os);
         //BufferedWriter bw = new BufferedWriter(osw);
 		while (doing) {
             try {
             	if (!entry) {
             		entry = true;
             		tmpMsg = "HELLO";
             	}
             	String rl = br.readLine();
            	//rl = OSUtil.xorDecode(rl, "Aj390jnRIn9wO2o3409WJofn");
             	if (rl != null && !rl.equals(""))
             		onMessageRecieved(rl);
 				writingToServer = true;
            	pw.println(tmpMsg);
				//pw.println(OSUtil.xorEncode(tmpMsg, "Aj390jnRIn9wO2o3409WJofn"));
             	tmpMsg = "";
             	writingToServer = false;
             	try {
             		Thread.sleep(50); // Let it breathe...
             	}
             	catch (Throwable t) {
             		
             	}
 			} catch (IOException e) {
 				if (e.getMessage().equals("Connection reset")) {
 					doing = false;
 					break;
 				}
 				if (e.getMessage().equals("Software caused connection abort: recv failed")) {
 					doing = false;
 					break;
 				}
 				e.printStackTrace();
 			}
 		}
 		pw.close();
 		try {
 			br.close();
 		} catch (Throwable t) {
 			
 		}
 		try {
 			isr.close();
 		} catch (Throwable t) {
 			
 		}
 		try {
 			is.close();
 		} catch (Throwable t) {
 			
 		}
 		halt();
 		System.out.println("Connection lost to the server!");
 		try {
 			MessageBox.show("Error", "Connection lost to the server!", MessageBoxIcon.Error, true);
 		}
 		catch (Throwable t) {
 			
 		}
 	}
 	
 	public String getIP() {
 		return ip;
 	}
 	
 	public void onMessageRecieved(String msg) {
 		System.out.println("Recieved from server (game host): " + msg);
 	}
 	boolean writingToServer = false;
 	public void halt() {
 		doing = false;
 		try {
 			Thread.sleep(250);
 		}
 		catch (Throwable t) {
 			
 		}
 		try {
 			socket.close();
 		}
 		catch (Throwable t) {
 			
 		}
 		socket = null;
 		try{
 			thread.destroy();
 		}
 		catch (Throwable t) {
 			
 		}
 		thread = null;
 	}
 	String tmpMsg = "";
 	public void send(String message) {
 		while (writingToServer) {
 			
 		}
 		tmpMsg = message;
 	}
 }
