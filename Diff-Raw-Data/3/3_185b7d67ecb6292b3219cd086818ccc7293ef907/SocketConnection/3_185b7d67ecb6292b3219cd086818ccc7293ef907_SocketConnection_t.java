 /*  HellowJava, alpha version
  *  (c) 2005-2010 Gustavo Maia Neto (gutomaia)
  *
  *  HellowJava and all other Hellow flavors will be always
  *  freely distributed under the terms of an GPLv3 license.
  *
  *  Human Knowledge belongs to the World!
  *--------------------------------------------------------------------------*/
 
 package net.guto.hellow.core;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class SocketConnection implements ConnectionHandle {
 	protected final String EL = "\r\n";
 	private Socket socket = null;
 
 	private PrintWriter printwriter = null;
 	private BufferedReader bufferedreader = null;
 
 	@Override
 	public void connect(String host, int port) {
 		try {
 			if (socket != null && socket.isConnected()) {
 				socket.close();
 				socket = null;
 			}
 			socket = new Socket(host, port);
 			printwriter = new PrintWriter(socket.getOutputStream(), true);
 			bufferedreader = new BufferedReader(new InputStreamReader(
 					socket.getInputStream()));
 		} catch (UnknownHostException unknownhostexception) {
 			System.err
 					.println("Exception: Unknown Host - Network unavailable?");
 			System.exit(1);
 		} catch (IOException ioexception) {
 			System.err.println("Exception: Can't get input from network");
 			System.exit(1);
 		}
 	}
 
 	@Override
 	public void disconnect() {
 		synchronized (socket) {
 			try {
 				socket.close();
 				socket = null;
 			} catch (Exception e) {
 				socket = null;
 			}
 		}
 	}
 
 	@Override
 	public void send(String command) {
 		try {
 			if (socket != null) {
 				printwriter.write(command);
 				printwriter.flush();
 			} else {
 				System.out.println("ERROR SEM SOCKET");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println(command);
 		}
 	}
 
 	@Override
 	public String nextCommand() {
 		String cmd = null;
 		if (socket != null)
 			synchronized (socket) {
 				try {
 					cmd = bufferedreader.readLine();
 					// Payload it is a playload Command
 					if (cmd != null && cmd.startsWith("MSG")) {
 						String tokens[] = cmd.split(" ");
 						int len = Integer.valueOf(tokens[3]);
 						char[] cbuf = new char[len];
 						bufferedreader.read(cbuf, 0, len);
 						cmd += EL + new String(cbuf);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		return cmd;
 	}
 
 	@Override
 	public boolean hasMoreCommands() {
		return socket.isConnected();
		//return true;
 	}
 
 }
