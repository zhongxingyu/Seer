 package com.game.rania.net;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.badlogic.gdx.graphics.Color;
 import com.game.rania.Config;
 import com.game.rania.RaniaGame;
 import com.game.rania.model.EnemyUser;
 import com.game.rania.model.Location;
 import com.game.rania.model.Planet;
 import com.game.rania.userdata.Command;
 import com.game.rania.userdata.User;
 
 public class NetController {
 
 	public boolean isWorkReciver = false;
 
 	public void SendTouchPoint(int x, int y, User user)
 	{
 		byte[] data = new byte[16];
 		byte[] xArr = intToByteArray(x);
 		byte[] yArr = intToByteArray(y);
 		byte[] userxArr = intToByteArray(RaniaGame.mUser.x);
 		byte[] useryArr = intToByteArray(RaniaGame.mUser.y);
 		System.arraycopy(xArr, 0, data, 0, 4);
 		System.arraycopy(yArr, 0, data, 4, 4);
 		System.arraycopy(userxArr, 0, data, 8, 4);
 		System.arraycopy(useryArr, 0, data, 12, 4);
 		SendCommand(10, data, user.socket);
 	}
 
 	public User ClientLogin(String Login, String Password)
 	{
 		User Res = new User();
 		Res.login = Login;
 		Res.socket = null;
 		Res.isLogin = false;
 		Res.isConnected = false;
 		try
 		{
 			InetAddress ipAddress = InetAddress.getByName(Config.serverAddress);
 			Socket socket = new Socket(ipAddress, Config.serverPort);
 			if (socket.isConnected())
 			{
 				Res.socket = socket;
 				InputStream sin = socket.getInputStream();
 				DataInputStream in = new DataInputStream(sin);
 				SendCommand(1, Login.getBytes("UTF-16LE"), socket);
 				SendCommand(6, Password.getBytes("UTF-16LE"), socket);
 				byte[] answer = new byte[4]; 
 				in.read(answer);
 				byte[] ServerTimeArr = new byte[4]; 
 				in.read(ServerTimeArr);
 				if (byteArrayToInt(answer)>0) 
 				{
 					Res.socket = socket;
 					Res.isLogin = true;
 					Res.serverTime = byteArrayToInt(ServerTimeArr);
 					Res.isConnected = true;
 					Res.receiver = new ReceiverWork();
 					Res.commands = new ArrayList<Command>();
 					RaniaGame.mUser.receiver.start();
 				}
 			}
 		}
 		catch (Exception ex)
 		{
 			
 		}
 		return Res;
 	}
 	
 	private class ReceiverWork extends Thread
 	{
 		private boolean Work;
 		public void run()
 		{
 			try
 			{
 				InputStream sin = RaniaGame.mUser.socket.getInputStream();
 				DataInputStream in = new DataInputStream(sin);
 				byte[] bytesCom = new byte[4];
 				byte[] bytesLen = new byte[4];
 				int Command = 0;
 				int Length = 0;
 				byte[] data;
 				while (true)
 				{
 					Work = isWorkReciver;
 					while (Work)
 					{
 						in.read(bytesCom);
 						in.read(bytesLen);
 						Command = byteArrayToInt(bytesCom);
 						Length = byteArrayToInt(bytesLen);
 						data = new byte[Length];
 						in.read(data);
 						//RaniaGame.mUser.commands.add(new Command(Command, Length, data));
 						if (Command == 12) // id   
 						{
 							int ArrPtr =0;
 							byte[] idArr = new byte[4];
 							for (int j=0;j<4;j++)
 							{
 								idArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							byte[] ShipNameLenArr = new byte[4];
 							for (int j=0;j<4;j++)
 							{
 								ShipNameLenArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							byte[] ShipNameArr = new byte[byteArrayToInt(ShipNameLenArr)];
 							for (int j=0;j<byteArrayToInt(ShipNameLenArr);j++)
 							{
 								ShipNameArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							byte[] xArr = new byte[4];
 							for (int j=0;j<4;j++)
 							{
 								xArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							byte[] yArr = new byte[4];
 							for (int j=0;j<4;j++)
 							{
 								yArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							int UserId = byteArrayToInt(idArr); //  Id   
 							int UserX = byteArrayToInt(xArr);   //   X   
 							int UserY = byteArrayToInt(yArr);   //   Y   
 							String ShipName = new String(ShipNameArr, "UTF-16LE");  //   
 						}
 						if (Command == 13) // id   
 						{
 							int ArrPtr =0;
 							byte[] idArr = new byte[4];
 							for (int j=0;j<4;j++)
 							{
 								idArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							byte[] xArr = new byte[4];
 							for (int j=0;j<4;j++)
 							{
 								xArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							byte[] yArr = new byte[4];
 							for (int j=0;j<4;j++)
 							{
 								yArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							int UserId = byteArrayToInt(idArr);      //   id 
 							int UserTouchX = byteArrayToInt(xArr);   //   X  
 							int UserTouchY = byteArrayToInt(yArr);   //   Y 
 						}
 						if (Command == 14) // id   .   )   )
 						{
 							int ArrPtr =0;
 							byte[] idArr = new byte[4];
 							for (int j=0;j<4;j++)
 							{
 								idArr[j]=data[ArrPtr];
 								ArrPtr++;
 							}
 							int UserId = byteArrayToInt(idArr);      //   id      
 						}
 					}
 				}
 			}
 			catch (Exception ex)
 			{
 
 			}
 		}
 	}
 	
 	public void ClientDisconnect(User user)
 	{
 		try
 		{
 			SendCommand(5, new byte[0], user.socket);
 			user.socket.shutdownInput();
 			user.socket.shutdownOutput();
 			user.socket.close();
 		}
 		catch (Exception ex)
 		{
 			
 		}
 	}
 	
 	public void ClientRelogin(User user)
 	{
 		
 	}
 	
 	public HashMap<String, EnemyUser> GetUsersInLocation(User user)
 	{
 		HashMap<String, EnemyUser> EnemyUsers = new HashMap<String, EnemyUser>();
 		try
 		{
 			InputStream sin = user.socket.getInputStream();
 			DataInputStream in = new DataInputStream(sin);
 			SendCommand(11, new byte[0], user.socket);
 			byte[] LenArr = new byte[4];
 			in.read(LenArr);
 			byte[] EnemyUsersArr = new byte[byteArrayToInt(LenArr)];
 			in.read(EnemyUsersArr);
 			int ArrPtr = 0;
 			byte[] UsersCountArr = new byte[4];
			for (int i=0;i<4;i++)
			{
				UsersCountArr[i]=EnemyUsersArr[ArrPtr];
				ArrPtr++;
			}
 			for (int i=0;i<byteArrayToInt(UsersCountArr);i++)
 			{
 				byte[] idArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					idArr[j]=EnemyUsersArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] ShipNameLenArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					ShipNameLenArr[j]=EnemyUsersArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] ShipNameArr = new byte[byteArrayToInt(ShipNameLenArr)];
 				for (int j=0;j<byteArrayToInt(ShipNameLenArr);j++)
 				{
 					ShipNameArr[j]=EnemyUsersArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] xArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					xArr[j]=EnemyUsersArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] yArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					yArr[j]=EnemyUsersArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] TargetxArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					TargetxArr[j]=EnemyUsersArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] TargetyArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					TargetyArr[j]=EnemyUsersArr[ArrPtr];
 					ArrPtr++;
 				}
 				EnemyUser enemy = new EnemyUser();
 				enemy.id = byteArrayToInt(idArr);
 				enemy.ShipName = new String(ShipNameArr, "UTF-16LE");
 				enemy.x = byteArrayToInt(xArr);
 				enemy.y = byteArrayToInt(yArr);
 				enemy.targetX = byteArrayToInt(TargetxArr);
 				enemy.targetY = byteArrayToInt(TargetxArr);
 				EnemyUsers.put(String.valueOf(enemy.id), enemy);
 			}
 		}
 		catch (Exception ex)
 		{

 		}
 		return EnemyUsers;
 	}
 
 	public HashMap<String, Planet> GetCurrentPlanets(User user)
 	{
 		HashMap<String, Planet> planets = new HashMap<String, Planet>();
 		isWorkReciver = false;
 		try
 		{
 			InputStream sin = user.socket.getInputStream();
 			DataInputStream in = new DataInputStream(sin);
 			SendCommand(9, new byte[0], user.socket);
 			byte[] LenArr = new byte[4];
 			in.read(LenArr);
 			byte[] PlanetsArr = new byte[byteArrayToInt(LenArr)];
 			in.read(PlanetsArr);
 			int ArrPtr = 0;
 			byte[] PlanetsCountArr = new byte[4];
 			for (int i=0;i<4;i++)
 			{
 				PlanetsCountArr[i]=PlanetsArr[ArrPtr];
 				ArrPtr++;
 			}
 			for (int i=0;i<byteArrayToInt(PlanetsCountArr);i++)
 			{
 				byte[] idArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					idArr[j]=PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] PlanetNameLenArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					PlanetNameLenArr[j]=PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] PlanetNameArr = new byte[byteArrayToInt(PlanetNameLenArr)];
 				for (int j=0;j<byteArrayToInt(PlanetNameLenArr);j++)
 				{
 					PlanetNameArr[j]=PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] PlanetTypeArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					PlanetTypeArr[j]=PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] R_speedArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					R_speedArr[j]=PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] OrbitArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					OrbitArr[j]=PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] RadiusArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					RadiusArr[j]=PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				char[] ColorArr = new char[4];
 				for (int j=0;j<4;j++)
 				{
 					ColorArr[j]=(char)PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] AtmosphereArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					AtmosphereArr[j]=PlanetsArr[ArrPtr];
 					ArrPtr++;
 				}
 				Planet planet     = new Planet();
 				planet.id         = byteArrayToInt(idArr);
 				planet.planetType = byteArrayToInt(PlanetTypeArr);
 				planet.orbit      = byteArrayToInt(OrbitArr);
 				planet.planetName = new String(PlanetNameArr, "UTF-16LE");
 				planet.speed      = byteArrayToInt(R_speedArr);
 				planet.radius     = byteArrayToInt(RadiusArr);
 				planet.color      = new Color(ColorArr[0] / 255.0f, ColorArr[1] / 255.0f, ColorArr[2] / 255.0f, ColorArr[3] / 255.0f);
 				planet.atmosphere = byteArrayToInt(AtmosphereArr);
 				planets.put(String.valueOf(planet.id), planet);
 			}
 		}
 		catch (Exception ex)
 		{
 			ClientRelogin(user);
 		}
 		isWorkReciver = true;
 		
 		return planets;
 	}
 	
 	public HashMap<String, Location> GetAllLocations(User user)
 	{
 		HashMap<String, Location> locations = new HashMap<String, Location>();
 		isWorkReciver = false;
 		try
 		{
 			InputStream sin = user.socket.getInputStream();
 			DataInputStream in = new DataInputStream(sin);
 			SendCommand(8, new byte[0], user.socket);
 			byte[] LenArr = new byte[4];
 			in.read(LenArr);
 			byte[] LocationsArr = new byte[byteArrayToInt(LenArr)];
 			in.read(LocationsArr);
 			int ArrPtr = 0;
 			byte[] LocationsCountArr = new byte[4];
 			for (int i=0;i<4;i++)
 			{
 				LocationsCountArr[i]=LocationsArr[ArrPtr];
 				ArrPtr++;
 			}
 			for (int i=0;i<byteArrayToInt(LocationsCountArr);i++)
 			{
 				byte[] idArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					idArr[j]=LocationsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] StarNameLenArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					StarNameLenArr[j]=LocationsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] StarNameArr = new byte[byteArrayToInt(StarNameLenArr)];
 				for (int j=0;j<byteArrayToInt(StarNameLenArr);j++)
 				{
 					StarNameArr[j]=LocationsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] StarTypeArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					StarTypeArr[j]=LocationsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] xArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					xArr[j]=LocationsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] yArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					yArr[j]=LocationsArr[ArrPtr];
 					ArrPtr++;
 				}
 				byte[] radiusArr = new byte[4];
 				for (int j=0;j<4;j++)
 				{
 					radiusArr[j]=LocationsArr[ArrPtr];
 					ArrPtr++;
 				}
 				Location Loc   = new Location();
 				Loc.id         = byteArrayToInt(idArr);
 				Loc.x          = byteArrayToInt(xArr);
 				Loc.y          = byteArrayToInt(yArr);
 				Loc.starRadius = byteArrayToInt(radiusArr);
 				Loc.starType   = byteArrayToInt(StarTypeArr);
 				Loc.starName   = new String(StarNameArr, "UTF-16LE");
 				locations.put(String.valueOf(Loc.id), Loc);
 			}
 		}
 		catch (Exception ex)
 		{
 			ClientRelogin(user);
 		}
 		isWorkReciver = true;
 		return locations;
 	}
 	
 	public void GetUserData(User user)
 	{
 		try
 		{
 			InputStream sin = user.socket.getInputStream();
 			DataInputStream in = new DataInputStream(sin);
 			SendCommand(7, new byte[0], user.socket);
 			byte[] LocArr = new byte[4];
 			byte[] xArr = new byte[4];
 			byte[] yArr = new byte[4];
 			in.read(LocArr);
 			in.read(xArr);
 			in.read(yArr);
 			byte[] PnameLenArr = new byte[4];
 			byte[] SnameLenArr = new byte[4];
 			in.read(PnameLenArr);
 			byte[] PnameArr = new byte[byteArrayToInt(PnameLenArr)];
 			in.read(PnameArr);
 			in.read(SnameLenArr);
 			byte[] SnameArr = new byte[byteArrayToInt(SnameLenArr)];
 			in.read(SnameArr);
 			user.idLocation = byteArrayToInt(LocArr);
 			user.x = byteArrayToInt(xArr);
 			user.y = byteArrayToInt(yArr);
 			user.PilotName = new String(PnameArr, "UTF-16LE");
 			user.ShipName = new String(SnameArr, "UTF-16LE");
 			user.isLogin = true;
 		}
 		catch (Exception ex)
 		{
 			ClientRelogin(user);
 		}
 	}
 
 	public void SendCommand(int cmd, byte[] data, Socket socket)
 	{
 		try 
 		{
 			OutputStream sout = socket.getOutputStream();
 			DataOutputStream out = new DataOutputStream(sout);
 			out.write(intToByteArray(cmd));
 			out.write(intToByteArray(data.length));
 			out.write(data);
 			out.flush();
 		} 
 		catch (Exception ex) {
 			
 		}
 		
 	}
 	public static int byteArrayToInt(byte[] b)
 	{
 		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
 	}
 
 	public static byte[] intToByteArray(int a)
 	{
 		return new byte[] { (byte)((a >> 24) & 0xFF), (byte)((a >> 16) & 0xFF), (byte)((a >> 8) & 0xFF), (byte)(a & 0xFF) };
 	}
 }
