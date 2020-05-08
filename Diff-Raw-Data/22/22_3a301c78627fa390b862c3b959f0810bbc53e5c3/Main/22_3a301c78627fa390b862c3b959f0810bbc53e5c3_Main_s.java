 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 public class Main
 {
 	public static ServerSocket serverSocket;
 	public static String clientMessage;
 	
 	public static BufferedOutputStream bOut;
 	public static InputStream inStream;
 	public static ByteArrayOutputStream baos;
 	public static int numBytes = 0;
 	
 	public static int userId = 0;
 	
 	public static ArrayList<User> userList = new ArrayList<User>();
 	public static ArrayList<String> ips = new ArrayList<String>();
 	public static ArrayList<ConnectionThread> clientThreads = new ArrayList<ConnectionThread>();
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			serverSocket = new ServerSocket(Integer.parseInt(Resource.PORT));
 		}
 		catch (Exception e) { e.printStackTrace(); }
 		
 		System.out.println("TCPServer Listening on Port: " + Resource.PORT);
 
 		while(true) 
 		{
 			try
 			{
				if(serverSocket.getInetAddress().toString() != "0.0.0.0" && !ips.contains(serverSocket.getInetAddress().toString()))
 				{
 					clientThreads.add(new ConnectionThread(++userId, serverSocket.accept()));
					ips.add(serverSocket.getInetAddress().toString());
 				}
 			}
 			catch (Exception e) { e.printStackTrace(); }
 		}
 	}
 	
 	public static String getUserList()
 	{
 		String userStr = "";
 		
 		for(int i = 0; i < userList.size(); i++)
 		{
 			int id = userList.get(i).getId();
 			String name = userList.get(i).getName();
 
 			userStr = userStr + id + "\\" + name;
 			if((i+1) < userList.size())
 				userStr = userStr + "\\";
 		}
 
 		return userStr;
 	}
 	
 	public static String updateUser(String userString)
 	{
 		userString = userString.substring(6);
 		int id = Integer.parseInt(userString.split("\\\\")[0]);
 		String username = userString.split("\\\\")[1];
 		if(username.charAt(0) == '"')
 			username = username.substring(1, username.length()-1);
 
 		String outputStr = "";
 
 		for(int i = 0; i < userList.size(); i++)
 		{
 			if(userList.get(i).getId() == id)
 			{
 				userList.get(i).setName(username);
 				outputStr = userList.get(i).getId() + "\\" + userList.get(i).getName();
 				break;
 			}
 		}
 
 		return outputStr;
 	}
 	
 	public static String removeUser(String id)
 	{
 		for(int i = 0; i < userList.size(); i++)
 		{
 			if(userList.get(i).getId() == Integer.parseInt(id.split(" ")[1]))
 			{
 				userList.remove(i);
 				break;
 			}
 		}
 		
 		return id.split(" ")[1];
 	}
 	
 	public static String getUserFromId(int id)
 	{
 		for(int i = 0; i < userList.size(); i++)
 		{
 			if(userList.get(i).getId() == id)
 				return userList.get(i).getName();
 		}
 		
 		return null;
 	}
 	
 	public static void receiveAndBounceMessage(String incomingMessage, Socket socket)
 	{
 		incomingMessage = incomingMessage.substring(6);
 		int id = Integer.parseInt(incomingMessage.split("\\\\")[0]);
 		String fileName = incomingMessage.split("\\\\")[1];
 		writeToAll("/file " + id + "\\" + fileName);
 		byte[] incomingBytes = new byte[1];
 		try
 		{
			ServerSocket temp = new ServerSocket(Integer.parseInt(Resource.PORT)+10);
 			socket = temp.accept();
 			inStream = socket.getInputStream();
 		}
 		catch(Exception e) { e.printStackTrace(); }
 		
 		baos = new ByteArrayOutputStream();
 		try
 		{
 			numBytes = inStream.read(incomingBytes, 0, incomingBytes.length);
 			do
 			{
 				baos.write(incomingBytes);
 				numBytes = inStream.read(incomingBytes);
 			} while (numBytes != -1);
 		}
 		catch(IOException ex) { ex.printStackTrace(); }
 		for(int i = 0; i < userList.size(); i++)
 		{
 			try 
 			{
 				socket = new Socket(clientThreads.get(i).IP.substring(1),Integer.parseInt(Resource.PORT) + clientThreads.get(i).ID);
 				bOut = new BufferedOutputStream(socket.getOutputStream());
 			} 
 			catch (IOException e1) { e1.printStackTrace(); }
 			if (bOut != null)
 			{
 				byte[] outArray = baos.toByteArray();
	
 	            try 
 	            {
 	                bOut.write(outArray, 0, outArray.length);
 	                bOut.flush();
 	                bOut.close();
 	            } catch (IOException ex) { ex.printStackTrace(); }
 			}
 		}
 	}
 	
 	public static void writeToAll(String message)
 	{
 		for(int i = 0; i < clientThreads.size(); i++)
 			clientThreads.get(i).writeToClient(message);
 	}
 	
 	public static String parseName(String clientMessage)
 	{
 		clientMessage = clientMessage.substring(6);
 		// 1\\"name"
 		clientMessage = clientMessage.split("\\\\")[1];
 		if(clientMessage.charAt(0) == '"')
 			clientMessage = clientMessage.substring(1, clientMessage.length()-1);
 		
 		return clientMessage.replace("/", "");
 	}
 }
