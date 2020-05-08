 package com.game.rania.net;
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.game.rania.Config;
 import com.game.rania.controller.CommandController;
 import com.game.rania.controller.Controllers;
 import com.game.rania.controller.command.AddPlanetCommand;
 import com.game.rania.controller.command.AddUserCommand;
 import com.game.rania.controller.command.ChatNewMessageCommand;
 import com.game.rania.controller.command.RemoveUserCommand;
 import com.game.rania.controller.command.SetTargetCommand;
 import com.game.rania.controller.command.SwitchScreenCommand;
 import com.game.rania.model.Domain;
 import com.game.rania.model.Nebula;
 import com.game.rania.model.Player;
 import com.game.rania.model.Target;
 import com.game.rania.model.User;
 import com.game.rania.model.Location;
 import com.game.rania.model.Planet;
 import com.game.rania.model.items.Consumable;
 import com.game.rania.model.items.Device;
 import com.game.rania.model.items.Droid;
 import com.game.rania.model.items.Engine;
 import com.game.rania.model.items.Equip;
 import com.game.rania.model.items.Fuelbag;
 import com.game.rania.model.items.Hyper;
 import com.game.rania.model.items.Item;
 import com.game.rania.model.items.ItemCollection;
 import com.game.rania.model.items.Radar;
 import com.game.rania.model.items.Shield;
 import com.game.rania.model.items.Body;
 import com.game.rania.model.items.Weapon;
 import com.game.rania.screen.MainMenu;
 import com.game.rania.userdata.Command;
 import com.game.rania.userdata.Client;
 import com.game.rania.userdata.IOStream;
 import com.game.rania.utils.Condition;
 
 public class NetController {
 
 	private Receiver receiver = null;
 	private CommandController cController = null;
 	private Client mClient = null;
 	private int ProtocolVersion = 7;
 	
 	public NetController(CommandController commandController){
 		cController = commandController;
 	}
 	
 	public void dispose(){
 		if (receiver != null)
 			receiver.stopThread();
 	}
 
 	public void sendTouchPoint(int x, int y, int currentX, int currentY)
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
 			mClient.stream.sendCommand(Command.touchPlayer, data);
 		}
 		catch (Exception ex)
 		{
 
 		}
 	}
 	
 	public void sendTarget(Target target)
 	{
 		byte[] data = new byte[8];
 		byte[] targetTypeArr = intToByteArray(target.type);
 		byte[] targetArr = intToByteArray(target.id);
 		System.arraycopy(targetTypeArr, 0, data, 0, 4);
 		System.arraycopy(targetArr, 0, data, 4, 4);
 		try
 		{
 			mClient.stream.sendCommand(Command.setTarget, data);
 		}
 		catch (Exception ex)
 		{
 
 		}
 	}
 	
 	public int getServerTime(){
 		return mClient.serverTime;
 	}
 
 	public boolean login(String Login, String Password)
 	{
 		mClient = new Client();
 		mClient.login = Login;
 		mClient.socket = null;
 		mClient.isLogin = false;
 		try
 		{
 			mClient.socket = new Socket(InetAddress.getByName(Config.serverAddress), Config.serverPort);
 			if (mClient.socket.isConnected())
 			{
 				mClient.stream = new IOStream(mClient.socket.getInputStream(), mClient.socket.getOutputStream());
 				byte[] LoginArr = Login.getBytes("UTF-16LE");
 				byte[] LoginLenArr = intToByteArray(LoginArr.length);
 				byte[] ProtocolVersionArr = intToByteArray(ProtocolVersion);
 				byte[] data = new byte[LoginArr.length+LoginLenArr.length+ProtocolVersionArr.length];
 				System.arraycopy(ProtocolVersionArr, 0, data, 0, 4);
 				System.arraycopy(LoginLenArr, 0, data, 4, 4);
 				System.arraycopy(LoginArr, 0, data, 8, LoginArr.length);
 				mClient.stream.sendCommand(Command.login, data);
 				mClient.stream.sendCommand(Command.password, Password.getBytes("UTF-16LE"));
 				Command answer = mClient.stream.readCommand();
 				if (answer.idCommand == Command.login)
 				{
 					mClient.isLogin = true;
 					mClient.serverTime = GetIntValue(answer.data, new AddressCommand());
 					receiver = new Receiver(mClient, this);
 					receiver.start();
 					return true;
 				}
 				
 				if (answer.idCommand == Command.faillogin)
 					mClient.isLogin = false;
 				if (answer.idCommand == Command.failversion)
 					mClient.isLogin = false;
 			}
 		}
 		catch (Exception ex)
 		{
 			return false;
 		}
 		return false;
 	}
 	
 	public void disconnect()
 	{
 		try
 		{
 			if (mClient != null && mClient.socket.isConnected() && mClient.isLogin) {
 				mClient.stream.sendCommand(Command.disconnect);
 				receiver.stopThread();
 				mClient.socket.shutdownInput();
 				mClient.socket.shutdownOutput();
 				mClient.socket.close();
 			}
 		}
 		catch (Exception ex)
 		{
 			
 		}
 	}
 	public void sendChatMessage(String Message, int channel)
 	{
 		String toPilot = "";
 		if (Message.isEmpty())
 			return;
 
 		try {
 			byte[] ChannelArr = intToByteArray(channel);
 			byte[] MessageArr = Message.getBytes("UTF-16LE");
 			byte[] MessageLenArr = intToByteArray(MessageArr.length);
 			byte[] toPilotArr = toPilot.getBytes("UTF-16LE");
 			byte[] toPilotLenArr = intToByteArray(toPilotArr.length);
 			byte[] data = new byte[ChannelArr.length+MessageArr.length+MessageLenArr.length+toPilotArr.length+toPilotLenArr.length];
 			System.arraycopy(ChannelArr, 0, data, 0, 4);
 			System.arraycopy(MessageLenArr, 0, data, 4, 4);
 			System.arraycopy(MessageArr, 0, data, 8, MessageArr.length);
 			System.arraycopy(toPilotLenArr, 0, data, 8+MessageArr.length, 4);
 			System.arraycopy(toPilotArr, 0, data, 8+MessageArr.length+4, toPilotArr.length);
 			mClient.stream.sendCommand(Command.message, data);
 		
 		} catch (Exception ex) {
 		}
 	}
 	
 	public void clientRelogin()
 	{
 		//mClient.relogin
 	}
 	
 	public void loadComplite()
 	{
 		try
 		{
 			mClient.stream.sendCommand(Command.loadComplite);
 		}
 		catch (Exception ex)
 		{
 		}
 	}
 	public ItemCollection getItems()
 	{
 		ItemCollection iCollect = new ItemCollection();
 		try
 		{
 			mClient.stream.sendCommand(Command.items);
 			Command command = waitCommand(Command.items);
 			AddressCommand ArrPtr = new AddressCommand();
 			int listItemsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0; i<listItemsCount; i++)
 			{
 				int itemsCount = GetIntValue(command.data, ArrPtr);
 				for (int j=0; j<itemsCount; j++)
 				{
 					int item_id = GetIntValue(command.data, ArrPtr);
 					int item_itemType = GetIntValue(command.data, ArrPtr);
 					int Item_DescriptionLen = GetIntValue(command.data, ArrPtr);
 					String item_description = GetStringValue(command.data, ArrPtr, Item_DescriptionLen);
 					int item_volume = GetIntValue(command.data, ArrPtr);
 					int item_region_id = GetIntValue(command.data, ArrPtr);
 					int item_packing = GetIntValue(command.data, ArrPtr);
 					int item_use_only = GetIntValue(command.data, ArrPtr);
 					int item_price = GetIntValue(command.data, ArrPtr);
 					if (item_itemType==1)
 					{
 						int DeviceVendorLen = GetIntValue(command.data, ArrPtr);
 						String device_vendorStr = GetStringValue(command.data, ArrPtr, DeviceVendorLen);
 						int device_deviceType = GetIntValue(command.data, ArrPtr);
 						int device_durability = GetIntValue(command.data, ArrPtr);
 						switch (device_deviceType)
 						{
 							case Device.Type.body:
 							{
 								int body_slot_weapons = GetIntValue(command.data, ArrPtr);
 								int body_slot_droids  = GetIntValue(command.data, ArrPtr);
 								int body_slot_shield  = GetIntValue(command.data, ArrPtr);
 								int body_slot_hyper   = GetIntValue(command.data, ArrPtr);
 								Body body 			= new Body();
 								body.id 			= item_id;
 								body.itemType 		= item_itemType;
 								body.description 	= item_description;
 								body.volume 		= item_volume;
 								body.region_id 		= item_region_id;
 								body.packing 		= item_packing;
 								body.vendorStr 		= device_vendorStr;
 								body.deviceType 	= device_deviceType;
 								body.durability 	= device_durability;
 								body.use_only 		= item_use_only;
 								body.price 			= item_price;
 								body.slot_weapons 	= body_slot_weapons;
 								body.slot_droids 	= body_slot_droids;
 								body.slot_shield 	= body_slot_shield;
 								body.slot_hyper 	= body_slot_hyper;
 								iCollect.bodies.put(body.id, body);
 								break;
 							}
 							case Device.Type.engine:
 							{
 								int engine_power 	= GetIntValue(command.data, ArrPtr);
 								int engine_economic = GetIntValue(command.data, ArrPtr);
 								Engine engine 		= new Engine();
 								engine.id 			= item_id;
 								engine.itemType 	= item_itemType;
 								engine.description 	= item_description;
 								engine.volume 		= item_volume;
 								engine.region_id 	= item_region_id;
 								engine.packing 		= item_packing;
 								engine.vendorStr 	= device_vendorStr;
 								engine.deviceType 	= device_deviceType;
 								engine.durability 	= device_durability;
 								engine.use_only 	= item_use_only;
 								engine.price 		= item_price;
 								engine.power 		= engine_power;
 								engine.economic 	= engine_economic;
 								iCollect.engines.put(engine.id, engine);
 								break;
 							}
 							case Device.Type.fuelbag:
 							{
 								int fuelbag_compress = GetIntValue(command.data, ArrPtr);
 								Fuelbag fuelbag 	= new Fuelbag();
 								fuelbag.id 			= item_id;
 								fuelbag.itemType 	= item_itemType;
 								fuelbag.description = item_description;
 								fuelbag.volume 		= item_volume;
 								fuelbag.region_id 	= item_region_id;
 								fuelbag.packing 	= item_packing;
 								fuelbag.vendorStr 	= device_vendorStr;
 								fuelbag.deviceType 	= device_deviceType;
 								fuelbag.durability 	= device_durability;
 								fuelbag.use_only 	= item_use_only;
 								fuelbag.price 		= item_price;
 								fuelbag.compress 	= fuelbag_compress;
 								iCollect.fuelbags.put(fuelbag.id, fuelbag);
 								break;
 							}
 							case Device.Type.droid:
 							{
 								int droid_power 	  = GetIntValue(command.data, ArrPtr);
 								int droid_time_reload = GetIntValue(command.data, ArrPtr);
 								Droid droid 		= new Droid();
 								droid.id 			= item_id;
 								droid.itemType 		= item_itemType;
 								droid.description 	= item_description;
 								droid.volume 		= item_volume;
 								droid.region_id 	= item_region_id;
 								droid.packing 		= item_packing;
 								droid.vendorStr 	= device_vendorStr;
 								droid.deviceType 	= device_deviceType;
 								droid.durability 	= device_durability;
 								droid.use_only 		= item_use_only;
 								droid.price 		= item_price;
 								droid.power 		= droid_power;
 								droid.time_reload 	= droid_time_reload;
 								iCollect.droids.put(droid.id, droid);
 								break;
 							}
 							case Device.Type.shield:
 							{
 								int shield_power 	= GetIntValue(command.data, ArrPtr);
 								Shield shield 		= new Shield();
 								shield.id 			= item_id;
 								shield.itemType 	= item_itemType;
 								shield.description 	= item_description;
 								shield.volume 		= item_volume;
 								shield.region_id 	= item_region_id;
 								shield.packing 		= item_packing;
 								shield.vendorStr 	= device_vendorStr;
 								shield.deviceType 	= device_deviceType;
 								shield.durability 	= device_durability;
 								shield.use_only 	= item_use_only;
 								shield.price 		= item_price;
 								shield.power 		= shield_power;
 								iCollect.shields.put(shield.id, shield);
 								break;
 							}
 							case Device.Type.hyper:
 							{
 								int hyper_radius 		= GetIntValue(command.data, ArrPtr);
 								int hyper_time_start 	= GetIntValue(command.data, ArrPtr);
 								int hyper_time_reload 	= GetIntValue(command.data, ArrPtr);
 								Hyper hyper 		= new Hyper();
 								hyper.id 			= item_id;
 								hyper.itemType 		= item_itemType;
 								hyper.description 	= item_description;
 								hyper.volume 		= item_volume;
 								hyper.region_id 	= item_region_id;
 								hyper.packing 		= item_packing;
 								hyper.vendorStr 	= device_vendorStr;
 								hyper.deviceType 	= device_deviceType;
 								hyper.durability 	= device_durability;
 								hyper.use_only 		= item_use_only;
 								hyper.price 		= item_price;
 								hyper.radius 		= hyper_radius;
 								hyper.time_start 	= hyper_time_start;
 								hyper.time_reload 	= hyper_time_reload;
 								iCollect.hypers.put(hyper.id, hyper);
 								break;
 							}
 							case Device.Type.radar:
 							{
 								int radar_radius 	= GetIntValue(command.data, ArrPtr);
 								int radar_defense 	= GetIntValue(command.data, ArrPtr);
 								int big_radius 		= GetIntValue(command.data, ArrPtr);
 								Radar radar 		= new Radar();
 								radar.id 			= item_id;
 								radar.itemType 		= item_itemType;
 								radar.description 	= item_description;
 								radar.volume 		= item_volume;
 								radar.region_id 	= item_region_id;
 								radar.packing 		= item_packing;
 								radar.vendorStr 	= device_vendorStr;
 								radar.deviceType 	= device_deviceType;
 								radar.durability 	= device_durability;
 								radar.use_only 		= item_use_only;
 								radar.price 		= item_price;
 								radar.radius 		= radar_radius;
 								radar.defense 		= radar_defense;
 								radar.big_radius	= big_radius;
 								iCollect.radars.put(radar.id, radar);
 								break;
 							}
 							case Device.Type.weapon:
 							{
 								int weapon_weaponType 	= GetIntValue(command.data, ArrPtr);
 								int weapon_radius 		= GetIntValue(command.data, ArrPtr);
 								int weapon_power 		= GetIntValue(command.data, ArrPtr);
 								int weapon_time_start 	= GetIntValue(command.data, ArrPtr);
 								int weapon_time_reload 	= GetIntValue(command.data, ArrPtr);
 								Weapon weapon 		= new Weapon();
 								weapon.id 			= item_id;
 								weapon.itemType 	= item_itemType;
 								weapon.description 	= item_description;
 								weapon.volume 		= item_volume;
 								weapon.region_id 	= item_region_id;
 								weapon.packing 		= item_packing;
 								weapon.vendorStr 	= device_vendorStr;
 								weapon.deviceType 	= device_deviceType;
 								weapon.durability 	= device_durability;
 								weapon.use_only 	= item_use_only;
 								weapon.price 		= item_price;
 								weapon.weaponType 	= weapon_weaponType;
 								weapon.radius 		= weapon_radius;
 								weapon.power 		= weapon_power;
 								weapon.time_start 	= weapon_time_start;
 								weapon.time_reload 	= weapon_time_reload;
 								iCollect.weapons.put(weapon.id, weapon);
 								break;
 							}
 						}
 					}
 					if (item_itemType==2)
 					{
 						Consumable item 	= new Consumable();
 						item.id 			= item_id;
 						item.itemType 		= item_itemType;
 						item.description 	= item_description;
 						item.volume 		= item_volume;
 						item.region_id 		= item_region_id;
 						item.packing 		= item_packing;
 						iCollect.consumables.put(item.id, item);
 					}
 				}
 			}
 		}
 		catch (Exception ex)
 		{
 		}
 		return iCollect;
 	}
 
 	public HashMap<Integer, Planet> getPlanets(int idLocation, boolean wait)
 	{
 		HashMap<Integer, Planet> planets = new HashMap<Integer, Planet>();
 		try
 		{
 			mClient.stream.sendCommand(Command.planets, intToByteArray(idLocation));
 			if (!wait)
 				return null;
 			
 			Command command = waitCommand(Command.planets);
 			AddressCommand ArrPtr = new AddressCommand(4);
 			int PlanetsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0; i<PlanetsCount; i++)
 			{
 				int PlanetId 		= GetIntValue(command.data, ArrPtr);
 				int PlanetNameLen 	= GetIntValue(command.data, ArrPtr);
 				String PlanetName 	= GetStringValue(command.data, ArrPtr, PlanetNameLen);
 				int PlanetType 		= GetIntValue(command.data, ArrPtr);
 				int PlanetSpeed 	= GetIntValue(command.data, ArrPtr);
 				int PlanetOrbit 	= GetIntValue(command.data, ArrPtr);
 				int PlanetRadius 	= GetIntValue(command.data, ArrPtr);
 				Color color 		= GetColorValue(command.data, ArrPtr);
 				Color atmColor 		= GetColorValue(command.data, ArrPtr);
 				int PlanetDomain 	= GetIntValue(command.data, ArrPtr);
 				int PlanetAtmosphere_speedX = GetIntValue(command.data, ArrPtr);
 				int PlanetAtmosphere_speedY = GetIntValue(command.data, ArrPtr);
				int PlanetPrice_coef= GetIntValue(command.data, ArrPtr);
 				Planet planet = new Planet(PlanetId, PlanetName, PlanetType, PlanetRadius, PlanetSpeed, PlanetOrbit, idLocation, PlanetDomain, PlanetAtmosphere_speedX, PlanetAtmosphere_speedY);
 				planet.color = color;
				planet.price_coef = PlanetPrice_coef;
 				planet.atmophereColor = atmColor;
 				planets.put(PlanetId, planet);
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return planets;
 	}
 	
 	public HashMap<Integer, Location> getAllLocations()
 	{
 		HashMap<Integer, Location> locations = new HashMap<Integer, Location>();
 		try
 		{
 			mClient.stream.sendCommand(Command.locations);
 			Command command = waitCommand(Command.locations);
 			AddressCommand ArrPtr = new AddressCommand();
 			int LocationsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0; i<LocationsCount; i++)
 			{
 				Location Loc   = new Location();
 				Loc.id = GetIntValue(command.data, ArrPtr);
 				int StarNameLen = GetIntValue(command.data, ArrPtr);
 				Loc.starName = GetStringValue(command.data, ArrPtr, StarNameLen);
 				Loc.starType = GetIntValue(command.data, ArrPtr);
 				Loc.x = GetIntValue(command.data, ArrPtr);
 				Loc.y = GetIntValue(command.data, ArrPtr);
 				Loc.starRadius = GetIntValue(command.data, ArrPtr);
 				Loc.domain = GetIntValue(command.data, ArrPtr);
 				locations.put(Loc.id, Loc);
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return locations;
 	}
 	
 	public HashMap<Integer, Nebula> getAllNebulas()
 	{
 		HashMap<Integer, Nebula> nebulas = new HashMap<Integer, Nebula>();
 		try
 		{
 			mClient.stream.sendCommand(Command.nebulas);
 			Command command = waitCommand(Command.nebulas);
 			AddressCommand ArrPtr = new AddressCommand();
 			int NebulasCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0;i<NebulasCount;i++)
 			{
 				int NebId = GetIntValue(command.data, ArrPtr);
 				int NebType = GetIntValue(command.data, ArrPtr);
 				int NebX = GetIntValue(command.data, ArrPtr);
 				int NebY = GetIntValue(command.data, ArrPtr);
 				int NebScale = GetIntValue(command.data, ArrPtr);
 				int NebAngle = GetIntValue(command.data, ArrPtr);
 				Nebula Neb = new Nebula(NebId, NebType, NebX, NebY, NebAngle, NebScale);
 				nebulas.put(Neb.id, Neb);
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return nebulas;
 	}
 	
 	public HashMap<Integer, Domain> getAllDomains()
 	{
 		HashMap<Integer, Domain> domains = new HashMap<Integer, Domain>();
 		try
 		{
 			mClient.stream.sendCommand(Command.domains);
 			Command command = waitCommand(Command.domains);
 			AddressCommand ArrPtr = new AddressCommand();
 			int DomainsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0;i<DomainsCount;i++)
 			{
 				Domain domain = new Domain();
 				domain.id = GetIntValue(command.data, ArrPtr);
 				domain.color = GetColorValue(command.data, ArrPtr);
 				int DomainNameLen = GetIntValue(command.data, ArrPtr);
 				domain.domainName = GetStringValue(command.data, ArrPtr, DomainNameLen);
 				domain.x = GetIntValue(command.data, ArrPtr);
 				domain.y = GetIntValue(command.data, ArrPtr);
 				domains.put(domain.id, domain);
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return domains;
 	}
 	
 	public Player getUserData()
 	{
 		try
 		{
 			mClient.stream.sendCommand(Command.player);
 			Command command = waitCommand(Command.player);
 			AddressCommand ArrPtr = new AddressCommand();
 			int UserId = GetIntValue(command.data, ArrPtr);			
 			int UserX = GetIntValue(command.data, ArrPtr);
 			int UserY = GetIntValue(command.data, ArrPtr);
 			int UserDomain = GetIntValue(command.data, ArrPtr);
 			int UserInPlanet = GetIntValue(command.data, ArrPtr);
 			int PnameLen = GetIntValue(command.data, ArrPtr);
 			String PName = GetStringValue(command.data, ArrPtr, PnameLen);			
 			int SnameLen = GetIntValue(command.data, ArrPtr);			
 			String SName = GetStringValue(command.data, ArrPtr, SnameLen);			
 			Player player = new Player(UserId, UserX, UserY, PName, SName, UserDomain, UserInPlanet);
 			player.setEquips(getEquips(command.data, ArrPtr));
 			return player;
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
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
 		
 		switch (command.idCommand) {
 			case Command.addUser:
 			{
 				AddressCommand ArrPtr = new AddressCommand();
 				int UserId = GetIntValue(command.data, ArrPtr);
 				int ShipNameLen = GetIntValue(command.data, ArrPtr);
 				String ShipName = GetStringValue(command.data, ArrPtr, ShipNameLen);
 				int UserX = GetIntValue(command.data, ArrPtr);
 				int UserY = GetIntValue(command.data, ArrPtr);
 				int TargetX = GetIntValue(command.data, ArrPtr);
 				int TargetY = GetIntValue(command.data, ArrPtr);
 				int UserDomain = GetIntValue(command.data, ArrPtr);
 				User user = new User(UserId, UserX, UserY, ShipName, "", UserDomain);
 				user.setPositionTarget(TargetX, TargetY);
 				user.setEquips(getEquips(command.data, ArrPtr));
 				cController.addCommand(new AddUserCommand(user));
 				break;
 			}
 			case Command.touchUser:
 			{
 				AddressCommand ArrPtr = new AddressCommand();
 				int UserId = GetIntValue(command.data, ArrPtr);
 				int UserTouchX = GetIntValue(command.data, ArrPtr);
 				int UserTouchY = GetIntValue(command.data, ArrPtr);
 				cController.addCommand(new SetTargetCommand(UserId, UserTouchX, UserTouchY));
 				break;
 			}
 			case Command.removeUser:
 			{
 				int UserId = GetIntValue(command.data, new AddressCommand());
 				cController.addCommand(new RemoveUserCommand(UserId));
 				break;
 			}
 			case Command.disconnect:
 			{
 				try
 				{
 					receiver.stopThread();
 					mClient.socket.shutdownInput();
 					mClient.socket.shutdownOutput();
 					mClient.socket.close();
 					cController.addCommand(new SwitchScreenCommand(new MainMenu()));
 				}
 				catch (Exception ex)
 				{
 				}
 			}
 			case Command.message:
 			{
 				AddressCommand ArrPtr = new AddressCommand();
 				int 	channel 	= GetIntValue(command.data, ArrPtr);
 				int 	messageLen 	= GetIntValue(command.data, ArrPtr);
 				String 	message 	= GetStringValue(command.data, ArrPtr, messageLen);
 				int 	nameLen 	= GetIntValue(command.data, ArrPtr);
 				String 	userName 	= GetStringValue(command.data, ArrPtr, nameLen);
 				int 	toPilotLen 	= GetIntValue(command.data, ArrPtr);
 				String 	toPilot 	= GetStringValue(command.data, ArrPtr, toPilotLen);
 				cController.addCommand(new ChatNewMessageCommand(userName, channel, message, toPilot));
 				break;
 			}
 			case Command.planets:
 			{
 				AddressCommand ArrPtr = new AddressCommand(0);
 				int locID = GetIntValue(command.data, ArrPtr);
 				int PlanetsCount = GetIntValue(command.data, ArrPtr);
 				for (int i=0; i<PlanetsCount; i++)
 				{
 					int PlanetId      = GetIntValue(command.data, ArrPtr);
 					int PlanetNameLen = GetIntValue(command.data, ArrPtr);
 					String PlanetName = GetStringValue(command.data, ArrPtr, PlanetNameLen);
 					int PlanetType 	  = GetIntValue(command.data, ArrPtr);
 					int PlanetSpeed   = GetIntValue(command.data, ArrPtr);
 					int PlanetOrbit   = GetIntValue(command.data, ArrPtr);
 					int PlanetRadius  = GetIntValue(command.data, ArrPtr);
 					Color PlanetColor = GetColorValue(command.data, ArrPtr);
 					Color AtmColor = GetColorValue(command.data, ArrPtr);
 					int PlanetDomain = GetIntValue(command.data, ArrPtr);
 					int PlanetAtmosphere_speedX = GetIntValue(command.data, ArrPtr);
 					int PlanetAtmosphere_speedY = GetIntValue(command.data, ArrPtr);
 					int PlanetPrice_coef = GetIntValue(command.data, ArrPtr);
 					Planet planet = new Planet(PlanetId, PlanetName, PlanetType, PlanetRadius, PlanetSpeed, PlanetOrbit, locID, PlanetDomain, PlanetAtmosphere_speedX, PlanetAtmosphere_speedY);
 					planet.color = PlanetColor;
 					planet.atmophereColor = AtmColor;
 					planet.price_coef = PlanetPrice_coef;
 					cController.addCommand(new AddPlanetCommand(planet));
 				}
 				break;
 			}
 	
 			default:
 				break;
 		}
 	}
 
 	private int GetIntValue(byte[] data, AddressCommand AC)
 	{
 		int Res=0;
 		byte[] Arr = new byte[4];
 		System.arraycopy(data, AC.address, Arr, 0, 4);
 		AC.delta(4);
 		Res = byteArrayToInt(Arr);
 		return Res;
 	}
 
 	private String GetStringValue(byte[] data, AddressCommand AC, int SL)
 	{
 		String Res = "";
 		byte[] Arr = new byte[SL];
 		System.arraycopy(data, AC.address, Arr, 0, SL);
 		AC.delta(SL);
 		try {
 			Res = new String(Arr, "UTF-16LE");
 		} catch (UnsupportedEncodingException e) {
 			Gdx.app.log(" ", ": " + e.getMessage());
 		}
 		return Res;
 	}
 	
 	 private Color GetColorValue(byte[] data, AddressCommand AC)
 	 {
 		 byte[] Arr = new byte[4];
 		 System.arraycopy(data, AC.address, Arr, 0, 4);
 		 AC.delta(4);
 		 char R = (char)(Arr[0]&0xFF);
 		 char G = (char)(Arr[1]&0xFF);
 		 char B = (char)(Arr[2]&0xFF);
 		 char A = (char)(Arr[3]&0xFF);
 		 Color Res =	new Color( R / 255.0f, G / 255.0f, B / 255.0f, A / 255.0f);
 		 return Res;
 	 }
 	
 	 private List<Equip<Item>> getEquips(byte[] data, AddressCommand ArrPtr)
 	 {
 		 ItemCollection items = Controllers.locController.getItems();
 		 if (items == null)
 			 return null;
 
 		 List<Equip<Item>> equip = new ArrayList<Equip<Item>>();
 		 int eqCount = GetIntValue(data, ArrPtr);
 		 
 		 for (int j = 0; j < eqCount; j++)
 		 {
 			 int item_id 	= GetIntValue(data, ArrPtr);
 			 int iType 		= GetIntValue(data, ArrPtr);
 			 int dType 		= GetIntValue(data, ArrPtr);
 			 int in_use 	= GetIntValue(data, ArrPtr);
 			 int wear 		= GetIntValue(data, ArrPtr);
 			 int location 	= GetIntValue(data, ArrPtr);
 			 int num	 	= GetIntValue(data, ArrPtr);
 			 Equip<Item> eq = new Equip<Item>();
 			 eq.in_use = in_use == 1 ? true : false;
 			 eq.wear = wear;
 			 eq.location = location;
 			 eq.num = num;
 			 eq.item = null;
 			 if (iType == Item.Type.device)
 			 {
 				 switch (dType)
 				 {
 					case Device.Type.droid:
 					{
 						eq.item = items.droids.get(item_id);
 						break;
 					}
 					case Device.Type.engine:
 					{
 						eq.item = items.engines.get(item_id);
 						break;
 					}
 					case Device.Type.fuelbag:
 					{
 						eq.item = items.fuelbags.get(item_id);
 						break;
 					}
 					case Device.Type.hyper:
 					{
 						eq.item = items.hypers.get(item_id);
 						break;
 					}
 					case Device.Type.radar:
 					{
 						eq.item = items.radars.get(item_id);
 						break;
 					}
 					case Device.Type.shield:
 					{
 						eq.item = items.shields.get(item_id);
 						break;
 					}
 					case Device.Type.body:
 					{
 						eq.item = items.bodies.get(item_id);
 						break;
 					}
 					case Device.Type.weapon:
 					{
 						eq.item = items.weapons.get(item_id);
 						break;
 					}
 				}
 			 }
 			 else if (iType == Item.Type.consumable)
 			 {
 				 eq.item = items.consumables.get(item_id);
 			 }
 			 equip.add(eq);
 		 }
 		 return equip;
 	 }
 	 
 	class AddressCommand {
 		public AddressCommand() {
 			this.address = 0;
 		}
 		public AddressCommand(int start){
 			this.address = start;
 		}
 		
 		public int address;
 		public void delta(int delta){
 			address += delta;
 		}
 	}
 }
