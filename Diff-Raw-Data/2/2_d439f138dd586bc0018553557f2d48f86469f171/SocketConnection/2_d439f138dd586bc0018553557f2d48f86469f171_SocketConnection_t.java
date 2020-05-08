 package aidancbrady.client;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 
 import javax.swing.JOptionPane;
 
 import aidancbrady.client.ClientCore.ConnectionState;
 
 public class SocketConnection extends Thread
 {
 	public Socket socket;
 	
 	public BufferedReader bufferedReader;
 	
 	public PrintWriter printWriter;
 	
 	public boolean hasConnection = false;
 	
 	@Override
 	public void run()
 	{
 		try {
 			InetAddress address = InetAddress.getByName(ClientCore.instance().serverIP);
 			new ThreadConnect(this).start();
 			socket = new Socket(address, ClientCore.instance().serverPort);
 			hasConnection = true;
 			
 			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			printWriter = new PrintWriter(socket.getOutputStream(), true);
 			
 			printWriter.println("/auth:" + ClientCore.instance().username);
 			
 			String readerLine = "";
 			boolean doneReading = false;
 			
 			while((readerLine = bufferedReader.readLine()) != null && !doneReading)
 			{
 				if(readerLine.trim().startsWith("/warning"))
 				{
 					String[] split = readerLine.trim().split(":");
 					JOptionPane.showMessageDialog(ClientCore.instance().theGui, split[1], "Warning", JOptionPane.WARNING_MESSAGE);
 					ClientCore.instance().disconnect();
 					return;
 				}
 				else if(readerLine.trim().startsWith("/user"))
 				{
 					String[] split = readerLine.trim().split(":");
 					ClientCore.instance().setUsername(split[1]);
 					continue;
 				}
 				else if(readerLine.trim().startsWith("/auth"))
 				{
 					String[] split = readerLine.trim().split(":");
 					ClientCore.instance().userJoined(split[1]);
 					ClientCore.instance().theGui.appendChat("<" + split[1] + " has joined>");
 					continue;
 				}
 				else if(readerLine.trim().startsWith("/deauth"))
 				{
 					String[] split = readerLine.trim().split(":");
 					ClientCore.instance().userLeft(split[1]);
 					ClientCore.instance().theGui.appendChat("<" + split[1] + " has left>");
 					continue;
 				}
 				else if(readerLine.trim().startsWith("/popuser"))
 				{
 					String[] split = readerLine.trim().split(":");
 					ClientCore.instance().userJoined(split[1]);
 					continue;
 				}
 				else if(readerLine.trim().startsWith("/discname"))
 				{
 					String[] split = readerLine.trim().split(":");
 					ClientCore.instance().discussion = split[1];
 					ClientCore.instance().theGui.discussionLabel.setText("Discussion: " + split[1]);
 				}
 				else if(readerLine.trim().startsWith("/chatlog"))
 				{
					ClientCore.instance().theGui.chatBox.setText(Util.getMessage(readerLine.trim()).replace("#NL#", "\n"));
 					continue;
 				}
 				
 				ClientCore.instance().theGui.appendChat(readerLine.trim());
 			}
 			
 			printWriter.close();
 			socket.close();
 			
 			ClientCore.instance().disconnect();
 			
 			finalize();
 		} catch(SocketException e) {
 		} catch(Throwable t) {
 			t.printStackTrace();
 		}
 	}
 }
