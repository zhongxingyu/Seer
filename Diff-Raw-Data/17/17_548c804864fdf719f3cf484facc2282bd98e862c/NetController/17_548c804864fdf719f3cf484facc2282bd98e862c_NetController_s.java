 package com.game.rania.net;
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.HashMap;
 
 import com.badlogic.gdx.graphics.Color;
 import com.game.rania.Config;
 import com.game.rania.controller.CommandController;
 import com.game.rania.controller.command.AddUserCommand;
 import com.game.rania.controller.command.RemoveUserCommand;
 import com.game.rania.controller.command.SetTargetCommand;
 import com.game.rania.model.Player;
 import com.game.rania.model.User;
 import com.game.rania.model.Location;
 import com.game.rania.model.Planet;
 import com.game.rania.userdata.Command;
 import com.game.rania.userdata.Client;
 import com.game.rania.userdata.IOStream;
 import com.game.rania.utils.Condition;
 
 public class NetController {
 
 	private Receiver receiver = null;
 	private CommandController cController = null;
 	
 	public NetController(CommandController controller){
 		cController = controller;
 	}
 	
 	public void dispose(){
 		if (receiver != null)
 			receiver.stopThread();
 	}
 
 	public void SendTouchPoint(int x, int y, int currentX, int currentY, Client client)
 	{
 		byte[] data = new byte[16];
 		byte[] xArr = intToByteArray(x);
 		byte[] yArr = intToByteArray(y);
 		byte[] userxArr = intToByteArray(currentX);
 		byte[] useryArr = intToByteArray(currentY);
 		System.arraycopy(xArr, 0, data, 0, 4);
 		System.arraycopy(yArr, 0, data, 4, 4);
 		System.arraycopy(userxArr, 0, data, 8, 4);
 		System.arraycopy(useryArr, 0, data, 12, 4);
 		try
 		{
 			client.stream.sendCommand(Command.touchPlayer, data);
 		}
 		catch (Exception ex)
 		{
 
 		}
 	}
 
 	public Client ClientLogin(String Login, String Password)
 	{
 		Client client = new Client();
 		client.login = Login;
 		client.socket = null;
 		client.isLogin = false;
 		try
 		{
 			client.socket = new Socket(InetAddress.getByName(Config.serverAddress), Config.serverPort);
 			if (client.socket.isConnected())
 			{
 				client.stream = new IOStream(client.socket.getInputStream(), client.socket.getOutputStream());
 				client.stream.sendCommand(Command.login, Login.getBytes("UTF-16LE"));
 				client.stream.sendCommand(Command.password, Password.getBytes("UTF-16LE"));
 
 				Command answer = client.stream.readCommand();
 				if (answer.idCommand == Command.login)
 				{
 					client.isLogin = true;
 					client.serverTime = GetIntValue(answer.data, 0);
 					receiver = new Receiver(client, this);
 					receiver.start();
 				}
 			}
 		}
 		catch (Exception ex)
 		{
 			return null;
 		}
 		return client;
 	}
 	
 	public void ClientDisconnect(Client client)
 	{
 		try
 		{
 			client.stream.sendCommand(Command.disconnect);
 			client.socket.shutdownInput();
 			client.socket.shutdownOutput();
 			client.socket.close();
 		}
 		catch (Exception ex)
 		{
 			
 		}
 	}
 	
 	public void ClientRelogin(Client client)
 	{
 		
 	}
 	
 	public HashMap<String, User> GetUsersInLocation(Client client)
 	{
 		HashMap<String, User> UsersMap = new HashMap<String, User>();
 		try
 		{
 			client.stream.sendCommand(Command.users);
 			Command command = waitCommand(Command.users);
 			int ArrPtr = 0;
 			int UsersCount = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 			for (int i=0;i<UsersCount;i++)
 			{
 				int UserId = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int ShipNameLen = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				String ShipName = GetStringValue(command.data, ArrPtr, ShipNameLen);
 				ArrPtr=ArrPtr+ShipNameLen;
 				int UserX = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int UserY = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int UserTargetX = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int UserTargetY = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				User userShip = new User(UserId, UserX, UserY, ShipName, "");
 				userShip.setPositionTarget(UserTargetX, UserTargetY);
 				UsersMap.put(String.valueOf(userShip.id), userShip);
 			}
 		}
 		catch (Exception ex)
 		{
 
 		}
 		return UsersMap;
 	}
 
 	public HashMap<String, Planet> GetCurrentPlanets(Client client)
 	{
 		HashMap<String, Planet> planets = new HashMap<String, Planet>();
 		try
 		{
 			client.stream.sendCommand(Command.planets);
 			Command command = waitCommand(Command.planets);
 			int ArrPtr = 0;
 			int PlanetsCount = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 			for (int i=0;i<PlanetsCount;i++)
 			{
 				int PlanetId = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int PlanetNameLen = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				String PlanetName = GetStringValue(command.data, ArrPtr, PlanetNameLen);
 				ArrPtr=ArrPtr+PlanetNameLen;
 				int PlanetType = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int PlanetRspeed = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int PlanetOrbit = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int PlanetRadius = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				char[] ColorArr = new char[4];
 				for (int j=0;j<4;j++)
 				{
 					ColorArr[j]=(char)command.data[ArrPtr];
 					ArrPtr++;
 				}
 				int PlanetAtmosphere = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				Planet planet     = new Planet();
 				planet.id         = PlanetId;
 				planet.planetType = PlanetType;
 				planet.orbit      = PlanetOrbit;
 				planet.planetName = PlanetName;
 				planet.speed      = PlanetRspeed;
 				planet.radius     = PlanetRadius;
 				planet.color      = new Color(ColorArr[0] / 255.0f, ColorArr[1] / 255.0f, ColorArr[2] / 255.0f, ColorArr[3] / 255.0f);
 				planet.atmosphere = PlanetAtmosphere;
 				planets.put(String.valueOf(planet.id), planet);
 			}
 		}
 		catch (Exception ex)
 		{
 			ClientRelogin(client);
 		}
 		return planets;
 	}
 	
 	public HashMap<String, Location> GetAllLocations(Client client)
 	{
 		HashMap<String, Location> locations = new HashMap<String, Location>();
 		try
 		{
 			client.stream.sendCommand(Command.locations);
 			Command command = waitCommand(Command.locations);
 			int ArrPtr = 0;
 			int LocationsCount = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 			for (int i=0;i<LocationsCount;i++)
 			{
 				int StarId = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int StarNameLen = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				String StarName = GetStringValue(command.data, ArrPtr, StarNameLen);
 				ArrPtr=ArrPtr+StarNameLen;
 				int StarType = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int StarX = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int StarY = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				int StarRadius = GetIntValue(command.data, ArrPtr);
 				ArrPtr=ArrPtr+4;
 				Location Loc   = new Location();
 				Loc.id         = StarId;
 				Loc.x          = StarX;
 				Loc.y          = StarY;
 				Loc.starRadius = StarRadius;
 				Loc.starType   = StarType;
 				Loc.starName   = StarName;
 				locations.put(String.valueOf(Loc.id), Loc);
 			}
 		}
 		catch (Exception ex)
 		{
 			ClientRelogin(client);
 		}
 		return locations;
 	}
 	
 	public Player getPlayerData(Client client)
 	{
 		try
 		{
 			client.stream.sendCommand(Command.player);
 
 			Command command = waitCommand(Command.player);
 			
 			int ArrPtr = 0;
 			int UserId = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 			
 			int UserLoc = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 			
 			int UserX = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 
 			int UserY = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 			
 			int PnameLen = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 			
 			String PName = GetStringValue(command.data, ArrPtr, PnameLen);
 			ArrPtr=ArrPtr+PnameLen;
 			
 			int SnameLen = GetIntValue(command.data, ArrPtr);
 			ArrPtr=ArrPtr+4;
 			
 			String SName = GetStringValue(command.data, ArrPtr, SnameLen);
 			ArrPtr=ArrPtr+SnameLen;
 			
 			Player player = new Player(UserId, UserLoc, UserX, UserY, PName, SName);
 			return player;
 		}
 		catch (Exception ex)
 		{
 			ClientRelogin(client);
 		}
 		return null;
 	}
 
 	public static int byteArrayToInt(byte[] b)
 	{
 		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
 	}
 
 	public static byte[] intToByteArray(int a)
 	{
 		return new byte[] { (byte)((a >> 24) & 0xFF), (byte)((a >> 16) & 0xFF), (byte)((a >> 8) & 0xFF), (byte)(a & 0xFF) };
 	}
 	
 	//commands 
 	private Condition cWaitCommand = new Condition(), cCopyCommand = new Condition();
 	private volatile int idWaitCommand = Command.none;
 	private volatile Command currentCommand = null;
 	
 	public Command waitCommand(int idCommand) throws InterruptedException{
 		idWaitCommand = idCommand;
 		
 		cWaitCommand.signalWait();
 		Command command = currentCommand;
 		cCopyCommand.signal();
 		return command;
 	}
 
 	public void processingCommand(Command command) throws InterruptedException, UnsupportedEncodingException {
 		if (idWaitCommand != Command.none && idWaitCommand == command.idCommand)
 		{			
 			idWaitCommand = Command.none;
 			currentCommand = command;
 			cWaitCommand.signal();
 			cCopyCommand.signalWait();
 			return;
 		}
 
 		if (command.idCommand == Command.addUser) // id   
 		{
 			int ArrPtr = 0;
 			int UserId = GetIntValue(command.data, ArrPtr);
 			ArrPtr = ArrPtr+4;
 			int ShipNameLen = GetIntValue(command.data, ArrPtr);
 			ArrPtr = ArrPtr+4;
 			String ShipName = GetStringValue(command.data, ArrPtr, ShipNameLen);
 			ArrPtr = ArrPtr+ShipNameLen;
 			int UserX = GetIntValue(command.data, ArrPtr);
 			ArrPtr = ArrPtr+4;
 			int UserY = GetIntValue(command.data, ArrPtr);
 			cController.addCommand(new AddUserCommand(UserId, UserX, UserY, ShipName));
 		}
 		
 		if (command.idCommand == Command.touchUser) // id   
 		{
 			int ArrPtr =0;
 			int UserId = GetIntValue(command.data, ArrPtr);
 			ArrPtr = ArrPtr+4;
 			int UserTouchX = GetIntValue(command.data, ArrPtr);
 			ArrPtr = ArrPtr+4;
 			int UserTouchY = GetIntValue(command.data, ArrPtr);
 			cController.addCommand(new SetTargetCommand(UserId, UserTouchX, UserTouchY));
 		}
 
 		if (command.idCommand == Command.removeUser) // id   .   )   )
 		{
 			int UserId = GetIntValue(command.data, 0);
 			cController.addCommand(new RemoveUserCommand(UserId));
 		}
 	}
 	private int GetIntValue(byte[] data, int SI)
 	{
 		int Res=0;
 		byte[] Arr = new byte[4];
 		System.arraycopy(data, SI, Arr, 0, 4);
 		Res = byteArrayToInt(Arr);
 		return Res;
 	}
 	private String GetStringValue(byte[] data, int SI, int SL)
 	{
 		String Res = "";
 		byte[] Arr = new byte[SL];
 		System.arraycopy(data, SI, Arr, 0, SL);
 		try {
 			Res = new String(Arr, "UTF-16LE");
 		} catch (UnsupportedEncodingException e) {
 
 		}
 		return Res;
 	}
 }
