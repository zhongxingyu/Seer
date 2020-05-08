 package Server;
 import java.util.*;
 import java.net.*;
 import java.io.*;
 import java.util.regex.PatternSyntaxException;
 
 public class User implements Runnable
 {
 private static Server server;
 private String name;
 private Socket socket;
 public HashMap<String, Room> rooms;
 private DataInputStream in;
 private DataOutputStream out;
 
 public User(Socket s) throws Exception
 {
 	socket = s;
 	rooms = new HashMap<String, Room>();
 	in = new DataInputStream(socket.getInputStream());
 	out = new DataOutputStream(socket.getOutputStream());
 	//get name
 	while(true)
 	{
 		try
 		{
 			out.writeUTF("ack");
 		}
 		catch(IOException e)
 		{
 			System.err.println(e.toString());
 			e.printStackTrace();
 			continue;
 		}
 		try
 		{
 			name = in.readUTF();
 			//System.out.println("name: " + name);
 		}
 		catch(IOException e)
 		{
 			System.err.println(e.toString());
 			e.printStackTrace();
 			out.writeUTF("e/c/Failed to get user name");
 			continue;
 		}
 		try
 		{
 			out.writeUTF("ack");
 			server.addUser(name, this);
 			break;
 		}
 		catch(Exception e)
 		{
 			System.err.println(e.toString());
 			e.printStackTrace();
 			out.writeUTF("e/c/User name exists");
 		}
 	}
 }
 
 /***************************************
  *             util                    *
  ***************************************/
 public static void setServer(Server sv)
 {
 	server = sv;
 }
 
 public String getName()
 {
 	return name;
 }
 
 private boolean containsRoomName(String roomName)
 {
 	synchronized(rooms)
 	{
 		System.err.println(rooms.toString());
 		if(rooms.containsKey(roomName))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 }
 
 /***************************************
  *       send methods                  *
  ***************************************/
 public void sendToClient(String msg)
 {
 	try
 	{
 		System.out.println(getName() + ":sendToClient:" + msg);
 		out.writeUTF(msg);
 		System.out.println("send succ");
 	}
 	catch(IOException e)
 	{
 		System.err.println("User#" + name + " send error");
 		e.printStackTrace();
 	}
 }
 
 public void sendToRoom(String roomName, String msg)
 {
	msg = "r/" + roomName + "/<img src='http://140.112.18.211/exp1/userimg/"+getName()+".jpg' width='50px' height='50px'>" + getName() + " says: " + msg;
 	try
 	{
 		server.sendToRoom(roomName, msg);
 	}
 	catch(Exception e)
 	{
 		System.err.println(e.toString());
 		e.printStackTrace();
 		//TODO send error msg
 	}
 }
 
 public void sendToUser(String userName, String msg)
 {
 	msg = "s/" + userName + "/<img src='http://140.112.18.211/exp1/userimg/"+getName()+".jpg' width='50px' height='50px'>" + getName() + " says to you: " + msg;
 	//System.out.println(msg);
 	try
 	{
 		server.sendToUser(userName, msg);
 	}
 	catch(Exception e)
 	{
 		System.err.println(e.toString());
 		e.printStackTrace();
 		//TODO send error msg
 	}
 }
 
 /***************************************
  *       methods about room            *
  ***************************************/
 public void createRoom(String roomName, String pw)
 {
 	try
 	{
 		server.createRoom(roomName, this, pw);
 		out.writeUTF("c/" + roomName + "/ack");
 		synchronized(rooms)
 		{
 			rooms.put(roomName, server.rooms.get(roomName));
 			System.err.println(rooms.toString());
 		}
 	}
 	catch(Exception e)
 	{
 		System.err.println(e.toString());
 		e.printStackTrace();
 		//TODO send error msg
 		try
 		{
 			out.writeUTF("c/" + roomName + "/nak");
 		}
 		catch(Exception e1)
 		{
 		}
 	}
 }
 
 public void joinRoomRequest(String roomName) throws Exception
 {
 	try
 	{
 		server.joinRoomRequest(getName(), roomName, "");
 	}
 	catch(Exception e)
 	{
 		System.err.println(e.toString());
 		e.printStackTrace();
 		System.err.println("Cannot join room: " + roomName);
 	}
 }
 
 public void getJoinReply(String roomName, String un)
 {
 	try
 	{
 		out.writeUTF("a/" + roomName + "/" + un);
 	}
 	catch(Exception e){}
 }
 
 public void joinRoom(String rn, Room room)
 {
 	try
 	{
 		//out.writeUTF();
 	}
 	catch(Exception e){}
 }
 
 public void joinRoomReply(String rn, String un, boolean r)
 {
 	try
 	{
 		server.joinRoomReply(rn, un, r);
 		synchronized(rooms)
 		{
 			rooms.put(rn, server.rooms.get(rn));
 			System.err.println(rooms.toString());
 		}
 	}
 	catch(Exception e){}
 }
 
 public void leaveRoom(String roomName) throws Exception
 {
 	if(containsRoomName(roomName))
 	{
 		try
 		{
 			synchronized(rooms)
 			{
 				System.err.println(rooms.toString());
 				Room room = rooms.get(roomName);
 				if(room.manager.getName().equals(getName()))
 				{
 					//server.leaveRoomAll(roomName);
 				}
 				else
 				{
 					room.removeUser(getName());
 					server.leaveRoom(roomName, getName());
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println(e.toString());
 			e.printStackTrace();
 			System.err.println("Cannot leave room: " + roomName);
 		}
 	}
 	else
 		throw new Exception("Cannot find room: " + roomName);
 }
 
 public void kickUser(String roomName, String userName)
 {
 	try
 	{
 		server.kickUser(roomName, userName, getName());
 	}
 	catch(Exception e)
 	{
 		System.err.println(e.toString());
 		e.printStackTrace();
 		//TODO send error msg
 	}
 }
 
 public void kicked(String roomName, String mng)
 {
 	try
 	{
 		sendToClient("k/" + roomName + "/" + mng);
 		synchronized(rooms)
 		{
 			System.err.println(rooms.toString());
 			rooms.remove(roomName);
 		}
 	}
 	catch(Exception e){}
 }
 
 public void sendFileInfo(String rn, String fn)
 {
 	try
 	{
 		server.sendFileInfo(rn, getName(), fn);
 	}
 	catch(Exception e){}
 }
 
 /***************************************
  *    messages managnent               *
  ***************************************/
 private void parseMsg(String msg)
 {
 	System.out.println(msg);
 	String[] msgs;
 	try
 	{
 		msgs = msg.split("/", 3);
 		char header = msgs[0].charAt(0);
 		switch(header)
 		{
 			case 'r'://send to room
 			{
 				sendToRoom(msgs[1], msgs[2]);
 				break;
 			}
 			case 'm'://send to main room
 			{
 				server.sendToMainRoom(msgs[2]);
 				break;
 			}
 			case 's'://secret chat
 			{
 				sendToUser(msgs[1], msgs[2]);
 				break;
 			}
 			case 'j'://join room
 			{
 				joinRoomRequest(msgs[1]);
 				break;
 			}
 			case 'y'://yes join room
 			{
 				joinRoomReply(msgs[1], msgs[2], true);
 				break;
 			}
 			case 'n'://no join room
 			{
 				joinRoomReply(msgs[1], msgs[2], false);
 				break;
 			}
 			case 'c'://create room
 			{
 				createRoom(msgs[1], msgs[2]);
 				break;
 			}
 			case 'l'://leave
 			{
 				leaveRoom(msgs[1]);
 				break;
 			}
 			case 'k'://kick
 			{
 				kickUser(msgs[1], msgs[2]);
 				break;
 			}
 			case 'f'://file
 			{
 				sendFileInfo(msgs[1], msgs[2]);
 				break;
 			}
 			case 'e'://error occur
 			{
 				parseErr(msgs[1]);
 				break;
 			}
 			default:
 			{
 				System.err.println("undefined msg: " + msg);
 			}
 		}
 	}
 	catch(Exception e)
 	{
 		System.err.println(e.toString());
 		e.printStackTrace();
 	}
 }
 
 private void parseErr(String msg)
 {
 	int errCode;
 	try
 	{
 		errCode = Integer.parseInt(msg, 16);
 	}
 	catch(NumberFormatException e)
 	{
 		System.err.println(e.toString());
 		e.printStackTrace();
 	}
 }
 
 /***************************************
  *         for threads                 *
  ***************************************/
 @Override
 public void run()
 {
 	String msg;
 	try
 	{
 		while(true)
 		{
 			msg = in.readUTF();
 			//System.out.println("msg: " + msg);
 			parseMsg(msg);
 		}
 	}
 	catch(IOException e)
 	{
 		System.err.println(e.toString());
 		e.printStackTrace();
 		try
 		{
 			server.removeUser(name);
 		}
 		catch(Exception e1)
 		{
 			System.err.println(e1.toString());
 			e1.printStackTrace();
 		}
 	}
 }
 }
