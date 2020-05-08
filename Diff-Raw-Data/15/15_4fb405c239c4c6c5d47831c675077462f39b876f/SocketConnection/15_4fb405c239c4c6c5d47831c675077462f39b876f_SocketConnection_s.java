 package aidancbrady.client;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 
 import javax.swing.JOptionPane;
 
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
 				String message = readerLine.trim();
 				
 				if(message.startsWith("/"))
 				{
 					message = message.substring(1);
 					String[] params = message.split(":");
 					String command = params[0];
 					
 					if(command.equals("warning"))
 					{
 						JOptionPane.showMessageDialog(ClientCore.instance().theGui, params[1], "Warning", JOptionPane.WARNING_MESSAGE);
 						ClientCore.instance().disconnect();
 						return;
 					}
 					else if(command.equals("user"))
 					{
 						ClientCore.instance().setUsername(params[1]);
 						continue;
 					}
 					else if(command.equals("auth"))
 					{
 						ClientCore.instance().userJoined(params[1], "no");
 						ClientCore.instance().theGui.appendChat("<" + params[1] + " has joined>");
 						continue;
 					}
 					else if(command.equals("deauth"))
 					{
 						ClientCore.instance().userLeft(params[1]);
 						ClientCore.instance().theGui.appendChat("<" + params[1] + " has left>");
 						continue;
 					}
 					else if(command.equals("popuser"))
 					{
 						ClientCore.instance().userJoined(params[1], params[2]);
 						continue;
 					}
 					else if(command.equals("discname"))
 					{
 						if(params.length == 1)
 						{
 							ClientCore.instance().updateDiscussion(null);
 							continue;
 						}
 						
 						ClientCore.instance().updateDiscussion(params[1]);
 						continue;
 					}
 					else if(command.equals("chatlog"))
 					{
 						ClientCore.instance().theGui.chatBox.setText(Util.getMessage(readerLine.trim()).replace("#NL#", "\n"));
 						continue;
 					}
 					else if(command.equals("clear"))
 					{
 						ClientCore.instance().theGui.chatBox.setText("");
 						continue;
 					}
 					else if(command.equals("modname"))
 					{
 						ClientCore.instance().updateModeratorName(params[1]);
 						continue;
 					}
 				}
 				
 				ClientCore.instance().theGui.appendChat(message);
 			}
 			
 			printWriter.close();
 			bufferedReader.close();
 			socket.close();
 			
 			ClientCore.instance().disconnect();
 			
 			finalize();
 		} catch(SocketException e) {
 		} catch(Throwable t) {
 			t.printStackTrace();
 		}
 	}
 }
