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
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.CommandController;
 import com.game.rania.controller.Controllers;
 import com.game.rania.controller.TimeController;
 import com.game.rania.controller.command.AddLocationCommand;
 import com.game.rania.controller.command.AddPlanetCommand;
 import com.game.rania.controller.command.AddUserCommand;
 import com.game.rania.controller.command.AttackCommand;
 import com.game.rania.controller.command.ChatNewMessageCommand;
 import com.game.rania.controller.command.InPlanetCommand;
 import com.game.rania.controller.command.RemoveUserCommand;
 import com.game.rania.controller.command.RepairCommand;
 import com.game.rania.controller.command.SetTargetCommand;
 import com.game.rania.controller.command.SwitchScreenCommand;
 import com.game.rania.controller.command.UnlockCommand;
 import com.game.rania.model.Font;
 import com.game.rania.model.RegionID;
 import com.game.rania.model.element.Domain;
 import com.game.rania.model.element.Location;
 import com.game.rania.model.element.Nebula;
 import com.game.rania.model.element.Planet;
 import com.game.rania.model.element.Player;
 import com.game.rania.model.element.Target;
 import com.game.rania.model.element.Text;
 import com.game.rania.model.element.User;
 import com.game.rania.model.items.Consumable;
 import com.game.rania.model.items.Device;
 import com.game.rania.model.items.RepairKit;
 import com.game.rania.model.items.Engine;
 import com.game.rania.model.items.Equip;
 import com.game.rania.model.items.Fuelbag;
 import com.game.rania.model.items.Hyper;
 import com.game.rania.model.items.Item;
 import com.game.rania.model.items.ItemCollection;
 import com.game.rania.model.items.Radar;
 import com.game.rania.model.items.Shield;
 import com.game.rania.model.items.Body;
 import com.game.rania.model.items.weapons.BfgGun;
 import com.game.rania.model.items.weapons.LaserGun;
 import com.game.rania.model.items.weapons.RocketGun;
 import com.game.rania.model.items.weapons.Weapon;
 import com.game.rania.model.ui.Message;
 import com.game.rania.screen.MainMenu;
 import com.game.rania.userdata.Command;
 import com.game.rania.userdata.Client;
 import com.game.rania.userdata.IOStream;
 import com.game.rania.utils.Condition;
 
 public class NetController
 {
   private Receiver          receiver        = null;
   private CommandController cController     = null;
   private Client            mClient         = null;
   private int               ProtocolVersion = 11;
 
   public NetController(CommandController commandController)
   {
     cController = commandController;
   }
 
   public void dispose()
   {
     if (receiver != null)
       receiver.stopThread();
   }
 
   public void sendTouchPoint(int x, int y)
   {
     byte[] data = new byte[8];
     byte[] userxArr = intToByteArray(x);
     byte[] useryArr = intToByteArray(y);
     System.arraycopy(userxArr, 0, data, 0, 4);
     System.arraycopy(useryArr, 0, data, 4, 4);
     try
     {
       mClient.stream.sendCommand(Command.touchPlayer, data);
     } catch (Exception ex)
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
     } catch (Exception ex)
     {
 
     }
   }
 
   public void sendUseEquip(int equip_id)
   {
     byte[] data = new byte[4];
     byte[] useEquipArr = intToByteArray(equip_id);
     System.arraycopy(useEquipArr, 0, data, 0, 4);
     try
     {
       mClient.stream.sendCommand(Command.useEquips, data);
     } catch (Exception ex)
     {
 
     }
   }
 
   public void sendInPlanet(int idPlanet)
   {
     byte[] data = new byte[4];
     byte[] planet = intToByteArray(idPlanet);
     System.arraycopy(planet, 0, data, 0, 4);
     try
     {
       mClient.stream.sendCommand(Command.inPlanet, data);
     } catch (Exception ex)
     {
 
     }
   }
 
   public void sendOutPlanet()
   {
     try
     {
       mClient.stream.sendCommand(Command.outPlanet);
     } catch (Exception ex)
     {
 
     }
   }
 
   public int getServerTime()
   {
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
         byte[] data = new byte[LoginArr.length + LoginLenArr.length + ProtocolVersionArr.length];
         System.arraycopy(ProtocolVersionArr, 0, data, 0, 4);
         System.arraycopy(LoginLenArr, 0, data, 4, 4);
         System.arraycopy(LoginArr, 0, data, 8, LoginArr.length);
         mClient.stream.sendCommand(Command.login, data);
         byte[] PasswordArr = Password.getBytes("UTF-16LE");
         byte[] PasswordLenArr = intToByteArray(PasswordArr.length);
         data = new byte[PasswordArr.length + PasswordLenArr.length];
         System.arraycopy(PasswordLenArr, 0, data, 0, 4);
         System.arraycopy(PasswordArr, 0, data, 4, PasswordArr.length);
         mClient.stream.sendCommand(Command.password, data);
         // Command command = waitCommand();
         Command command = mClient.stream.readCommand();
         if (command.idCommand == Command.login)
         {
           CommandReader cr = new CommandReader(command);
           mClient.isLogin = true;
           mClient.serverTime = cr.getInt();
           TimeController.setTime(mClient.serverTime);
           receiver = new Receiver(mClient, this);
           receiver.start();
           checkCRC(command, cr);
           return true;
         }
         if (command.idCommand == Command.faillogin)
         {
           RaniaGame.mController.addHUDObject(
                                new Message(RegionID.EDIT_ON, 0, 0,
                                            new Text("   ", Font.getFont("data/fonts/Postmodern One.ttf", 25), new Color(1.0f, 0.667f, 0.0f, 1.0f), 0, 0),
                                            5));
           mClient.isLogin = false;
         }
         if (command.idCommand == Command.failversion)
         {
           RaniaGame.mController.addHUDObject(
                                new Message(RegionID.EDIT_ON, 0, 0,
                                            new Text("  ", Font.getFont("data/fonts/Postmodern One.ttf", 25), new Color(1.0f, 0.667f, 0.0f, 1.0f), 0, 0),
                                            5));
           mClient.isLogin = false;
         }
       }
     } catch (Exception ex)
     {
       return false;
     }
     return false;
   }
 
   public boolean checkCRC(Command command, CommandReader cr)
   {
     if (command.controlCRC != cr.crc)
     {
       Gdx.app.log("CRC error", "Login");
       return false;
     }
     return true;
   }
 
   public void disconnect()
   {
     try
     {
       if (mClient != null && mClient.socket.isConnected() && mClient.isLogin)
       {
         mClient.stream.sendCommand(Command.disconnect);
         receiver.stopThread();
         mClient.socket.shutdownInput();
         mClient.socket.shutdownOutput();
         mClient.socket.close();
       }
     } catch (Exception ex)
     {
 
     }
   }
 
   public void sendChatMessage(String Message, int channel)
   {
     String toPilot = "";
     if (Message.isEmpty())
       return;
 
     try
     {
       byte[] ChannelArr = intToByteArray(channel);
       byte[] MessageArr = Message.getBytes("UTF-16LE");
       byte[] MessageLenArr = intToByteArray(MessageArr.length);
       byte[] toPilotArr = toPilot.getBytes("UTF-16LE");
       byte[] toPilotLenArr = intToByteArray(toPilotArr.length);
       byte[] data = new byte[ChannelArr.length + MessageArr.length + MessageLenArr.length + toPilotArr.length + toPilotLenArr.length];
       System.arraycopy(ChannelArr, 0, data, 0, 4);
       System.arraycopy(MessageLenArr, 0, data, 4, 4);
       System.arraycopy(MessageArr, 0, data, 8, MessageArr.length);
       System.arraycopy(toPilotLenArr, 0, data, 8 + MessageArr.length, 4);
       System.arraycopy(toPilotArr, 0, data, 8 + MessageArr.length + 4, toPilotArr.length);
       mClient.stream.sendCommand(Command.message, data);
 
     } catch (Exception ex)
     {
     }
   }
 
   public void clientRelogin()
   {
     // mClient.relogin
   }
 
   public void loadComplite()
   {
     try
     {
       mClient.stream.sendCommand(Command.loadComplite);
     } catch (Exception ex)
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
       CommandReader cr = new CommandReader(command);
       int listItemsCount = cr.getInt();
       for (int i = 0; i < listItemsCount; i++)
       {
         int itemsCount = cr.getInt();
         for (int j = 0; j < itemsCount; j++)
         {
           int item_id = cr.getInt();
           int item_itemType = cr.getInt();
           String item_description = cr.getString();
           int item_volume = cr.getInt();
           int item_region_id = cr.getInt();
           int item_use_only = cr.getInt();
           int item_price = cr.getInt();
           if (item_itemType == 1)
           {
             String device_vendorStr = cr.getString();
             int device_deviceType = cr.getInt();
             int device_durability = cr.getInt();
             switch (device_deviceType)
             {
               case Device.Type.body:
               {
                 int body_slot_weapons = cr.getInt();
                 int body_slot_droids = cr.getInt();
                 int body_slot_shield = cr.getInt();
                 int body_slot_hyper = cr.getInt();
                 Body body = new Body();
                 body.id = item_id;
                 body.itemType = item_itemType;
                 body.description = item_description;
                 body.volume = item_volume;
                 body.region_id = item_region_id;
                 body.vendorStr = device_vendorStr;
                 body.deviceType = device_deviceType;
                 body.durability = device_durability;
                 body.use_only = item_use_only;
                 body.price = item_price;
                 body.slot_weapons = body_slot_weapons;
                 body.slot_droids = body_slot_droids;
                 body.slot_shield = body_slot_shield;
                 body.slot_hyper = body_slot_hyper;
                 iCollect.bodies.put(body.id, body);
                 break;
               }
               case Device.Type.engine:
               {
                 int engine_power = cr.getInt();
                 int engine_economic = cr.getInt();
                 Engine engine = new Engine();
                 engine.id = item_id;
                 engine.itemType = item_itemType;
                 engine.description = item_description;
                 engine.volume = item_volume;
                 engine.region_id = item_region_id;
                 engine.vendorStr = device_vendorStr;
                 engine.deviceType = device_deviceType;
                 engine.durability = device_durability;
                 engine.use_only = item_use_only;
                 engine.price = item_price;
                 engine.power = engine_power;
                 engine.economic = engine_economic;
                 iCollect.engines.put(engine.id, engine);
                 break;
               }
               case Device.Type.fuelbag:
               {
                 int fuelbag_compress = cr.getInt();
                 Fuelbag fuelbag = new Fuelbag();
                 fuelbag.id = item_id;
                 fuelbag.itemType = item_itemType;
                 fuelbag.description = item_description;
                 fuelbag.volume = item_volume;
                 fuelbag.region_id = item_region_id;
                 fuelbag.vendorStr = device_vendorStr;
                 fuelbag.deviceType = device_deviceType;
                 fuelbag.durability = device_durability;
                 fuelbag.use_only = item_use_only;
                 fuelbag.price = item_price;
                 fuelbag.compress = fuelbag_compress;
                 iCollect.fuelbags.put(fuelbag.id, fuelbag);
                 break;
               }
               case Device.Type.droid:
               {
                 int droid_power = cr.getInt();
                 int droid_time_reload = cr.getInt();
                 int radius = cr.getInt();
                 RepairKit repairKit = new RepairKit();
                 repairKit.id = item_id;
                 repairKit.itemType = item_itemType;
                 repairKit.description = item_description;
                 repairKit.volume = item_volume;
                 repairKit.region_id = item_region_id;
                 repairKit.vendorStr = device_vendorStr;
                 repairKit.deviceType = device_deviceType;
                 repairKit.durability = device_durability;
                 repairKit.use_only = item_use_only;
                 repairKit.price = item_price;
                 repairKit.power = droid_power;
                 repairKit.time_reload = droid_time_reload;
                 repairKit.radius = radius;
                 iCollect.droids.put(repairKit.id, repairKit);
                 break;
               }
               case Device.Type.shield:
               {
                 int shield_power = cr.getInt();
                 Shield shield = new Shield();
                 shield.id = item_id;
                 shield.itemType = item_itemType;
                 shield.description = item_description;
                 shield.volume = item_volume;
                 shield.region_id = item_region_id;
                 shield.vendorStr = device_vendorStr;
                 shield.deviceType = device_deviceType;
                 shield.durability = device_durability;
                 shield.use_only = item_use_only;
                 shield.price = item_price;
                 shield.power = shield_power;
                 iCollect.shields.put(shield.id, shield);
                 break;
               }
               case Device.Type.hyper:
               {
                 int hyper_radius = cr.getInt();
                 int hyper_time_start = cr.getInt();
                 int hyper_time_reload = cr.getInt();
                 Hyper hyper = new Hyper();
                 hyper.id = item_id;
                 hyper.itemType = item_itemType;
                 hyper.description = item_description;
                 hyper.volume = item_volume;
                 hyper.region_id = item_region_id;
                 hyper.vendorStr = device_vendorStr;
                 hyper.deviceType = device_deviceType;
                 hyper.durability = device_durability;
                 hyper.use_only = item_use_only;
                 hyper.price = item_price;
                 hyper.radius = hyper_radius;
                 hyper.time_start = hyper_time_start;
                 hyper.time_reload = hyper_time_reload;
                 iCollect.hypers.put(hyper.id, hyper);
                 break;
               }
               case Device.Type.radar:
               {
                 int radar_radius = cr.getInt();
                 int radar_defense = cr.getInt();
                 int big_radius = cr.getInt();
                 Radar radar = new Radar();
                 radar.id = item_id;
                 radar.itemType = item_itemType;
                 radar.description = item_description;
                 radar.volume = item_volume;
                 radar.region_id = item_region_id;
                 radar.vendorStr = device_vendorStr;
                 radar.deviceType = device_deviceType;
                 radar.durability = device_durability;
                 radar.use_only = item_use_only;
                 radar.price = item_price;
                 radar.radius = radar_radius;
                 radar.defense = radar_defense;
                 radar.big_radius = big_radius;
                 iCollect.radars.put(radar.id, radar);
                 break;
               }
               case Device.Type.weapon:
               {
                 int weapon_weaponType = cr.getInt();
                 int weapon_radius = cr.getInt();
                 int weapon_power = cr.getInt();
                 int weapon_time_start = cr.getInt();
                 int weapon_time_reload = cr.getInt();
                 Weapon weapon = null;
 
                 switch (weapon_weaponType)
                 {
                   case Weapon.Type.Laser:
                     weapon = new LaserGun();
                     break;
                   case Weapon.Type.Rocket:
                     weapon = new RocketGun();
                     break;
                   case Weapon.Type.BFG:
                     weapon = new BfgGun();
                     break;
                 }
                 if (weapon == null)
                   break;
 
                 weapon.id = item_id;
                 weapon.itemType = item_itemType;
                 weapon.description = item_description;
                 weapon.volume = item_volume;
                 weapon.region_id = item_region_id;
                 weapon.vendorStr = device_vendorStr;
                 weapon.deviceType = device_deviceType;
                 weapon.durability = device_durability;
                 weapon.use_only = item_use_only;
                 weapon.price = item_price;
                 weapon.weaponType = weapon_weaponType;
                 weapon.radius = weapon_radius;
                 weapon.power = weapon_power;
                 weapon.time_start = weapon_time_start;
                 weapon.time_reload = weapon_time_reload;
                 iCollect.weapons.put(weapon.id, weapon);
                 break;
               }
             }
           }
           if (item_itemType == 2)
           {
             Consumable item = new Consumable();
             item.id = item_id;
             item.itemType = item_itemType;
             item.description = item_description;
             item.volume = item_volume;
             item.region_id = item_region_id;
             iCollect.consumables.put(item.id, item);
           }
         }
       }
       checkCRC(command, cr);
     } catch (Exception ex)
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
       CommandReader cr = new CommandReader(command);
       cr.delta(4);
       int PlanetsCount = cr.getInt();
       for (int i = 0; i < PlanetsCount; i++)
       {
         int PlanetId = cr.getInt();
         String PlanetName = cr.getString();
         int PlanetType = cr.getInt();
         int PlanetSpeed = cr.getInt();
         int PlanetOrbit = cr.getInt();
         int PlanetRadius = cr.getInt();
         Color color = cr.getColor();
         Color atmColor = cr.getColor();
         int PlanetDomain = cr.getInt();
         int PlanetAtmosphere_speedX = cr.getInt();
         int PlanetAtmosphere_speedY = cr.getInt();
         int PlanetPrice_coef = cr.getInt();
         int ServiceCount = cr.getInt();
         Planet planet = new Planet(PlanetId, PlanetName, PlanetType, PlanetRadius, PlanetSpeed, PlanetOrbit, idLocation, PlanetDomain, PlanetAtmosphere_speedX, PlanetAtmosphere_speedY);
         planet.services = new int[ServiceCount];
         for (int j = 0; j < ServiceCount; j++)
         {
           planet.services[j] = cr.getInt();
         }
         planet.color = color;
         planet.price_coef = PlanetPrice_coef;
         planet.atmophereColor = atmColor;
         planets.put(PlanetId, planet);
       }
       checkCRC(command, cr);
     } catch (Exception ex)
     {
       clientRelogin();
     }
     return planets;
   }
 
   public HashMap<Integer, Nebula> getAllNebulas()
   {
     HashMap<Integer, Nebula> nebulas = new HashMap<Integer, Nebula>();
     try
     {
       mClient.stream.sendCommand(Command.nebulas);
       Command command = waitCommand(Command.nebulas);
       CommandReader cr = new CommandReader(command);
       int NebulasCount = cr.getInt();
       for (int i = 0; i < NebulasCount; i++)
       {
         int NebId = cr.getInt();
         int NebType = cr.getInt();
         int NebX = cr.getInt();
         int NebY = cr.getInt();
         int NebScale = cr.getInt();
         int NebAngle = cr.getInt();
         Nebula Neb = new Nebula(NebId, NebType, NebX, NebY, NebAngle, NebScale);
         nebulas.put(Neb.id, Neb);
       }
       checkCRC(command, cr);
     } catch (Exception ex)
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
       CommandReader cr = new CommandReader(command);
       int DomainsCount = cr.getInt();
       for (int i = 0; i < DomainsCount; i++)
       {
         Domain domain = new Domain();
         domain.id = cr.getInt();
         domain.color = cr.getColor();
         domain.domainName = cr.getString();
         domain.x = cr.getInt();
         domain.y = cr.getInt();
         int enemyCount = cr.getInt();
         domain.enemy = new int[enemyCount];
         for (int j = 0; j < enemyCount; j++)
         {
           domain.enemy[j] = cr.getInt();
         }
         domains.put(domain.id, domain);
       }
       checkCRC(command, cr);
     } catch (Exception ex)
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
       CommandReader cr = new CommandReader(command);
       int UserId = cr.getInt();
       int UserX = cr.getInt();
       int UserY = cr.getInt();
       int UserDomain = cr.getInt();
       int UserInPlanet = cr.getInt();
       String PName = cr.getString();
       String SName = cr.getString();
       double UserEnergy = cr.getDbl();
       Player player = new Player(UserId, UserX, UserY, PName, SName, UserDomain, UserInPlanet);
       player.energy = UserEnergy;
       player.setEquips(getEquips(cr));
       checkCRC(command, cr);
       return player;
     } catch (Exception ex)
     {
       clientRelogin();
     }
     return null;
   }
 
   public static int byteArrayToInt(byte[] b)
   {
     return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
   }
 
   public static double byteArrayToDouble(byte[] b)
   {
     long longBits = ((b[7] & 0xFFL) << 56) |
                     ((b[6] & 0xFFL) << 48) |
                     ((b[5] & 0xFFL) << 40) |
                     ((b[4] & 0xFFL) << 32) |
                     ((b[3] & 0xFFL) << 24) |
                     ((b[2] & 0xFFL) << 16) |
                     ((b[1] & 0xFFL) << 8) |
                     ((b[0] & 0xFFL) << 0);
     return Double.longBitsToDouble(longBits);
   }
 
   public static byte[] intToByteArray(int a)
   {
     return new byte[] { (byte) ((a >> 24) & 0xFF), (byte) ((a >> 16) & 0xFF), (byte) ((a >> 8) & 0xFF), (byte) (a & 0xFF) };
   }
 
   // commands
   private Condition        cWaitCommand   = new Condition(), cCopyCommand = new Condition();
   private volatile int     idWaitCommand  = Command.none;
   private volatile Command currentCommand = null;
 
   public Command waitCommand(int idCommand) throws InterruptedException
   {
     idWaitCommand = idCommand;
 
     cWaitCommand.signalWait();
     Command command = currentCommand;
     cCopyCommand.signal();
     return command;
   }
 
   public void processingCommand(Command command) throws InterruptedException, UnsupportedEncodingException
   {
     if (idWaitCommand != Command.none && idWaitCommand == command.idCommand)
     {
       idWaitCommand = Command.none;
       currentCommand = command;
       cWaitCommand.signal();
       cCopyCommand.signalWait();
       return;
     }
 
     CommandReader cr = new CommandReader(command);
     switch (command.idCommand)
     {
       case Command.addUser:
       {
         int UserId = cr.getInt();
         String ShipName = cr.getString();
         int UserX = cr.getInt();
         int UserY = cr.getInt();
         int UserDomain = cr.getInt();
        double UserEnergy = cr.getDbl();
         User user = new User(UserId, UserX, UserY, ShipName, "", UserDomain);
        user.energy = UserEnergy;
         user.setEquips(getEquips(cr));
         cController.addCommand(new AddUserCommand(user));
         break;
       }
       case Command.touchUser:
       {
         int UserId = cr.getInt();
         int UserTouchX = cr.getInt();
         int UserTouchY = cr.getInt();
         int flyTime = cr.getInt();
         double energy = cr.getDbl();
         cController.addCommand(new SetTargetCommand(UserId, UserTouchX, UserTouchY, flyTime, energy));
         break;
       }
       case Command.removeUser:
       {
         int UserId = cr.getInt();
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
         } catch (Exception ex)
         {
         }
       }
       case Command.message:
       {
         int channel = cr.getInt();
         String message = cr.getString();
         String userName = cr.getString();
         String toPilot = cr.getString();
         cController.addCommand(new ChatNewMessageCommand(userName, channel, message, toPilot));
         break;
       }
       case Command.userAction:
       {
         int equipID = cr.getInt();
         int userID = cr.getInt();
         int targetID = cr.getInt();
         int action = cr.getInt();
         switch (action)
         {
           case User.Action.none:
           {
             cController.addCommand(new UnlockCommand(userID, equipID));
             break;
           }
           case User.Action.attack:
           {
             int dmg = cr.getInt();
             cController.addCommand(new AttackCommand(userID, targetID, equipID, dmg));
             break;
           }
           case User.Action.repair:
           {
             int rapair = cr.getInt();
             cController.addCommand(new RepairCommand(userID, targetID, equipID, rapair));
             break;
           }
         }
         break;
       }
       case Command.planets:
       {
         int locID = cr.getInt();
         int PlanetsCount = cr.getInt();
         for (int i = 0; i < PlanetsCount; i++)
         {
           int PlanetId = cr.getInt();
           String PlanetName = cr.getString();
           int PlanetType = cr.getInt();
           int PlanetSpeed = cr.getInt();
           int PlanetOrbit = cr.getInt();
           int PlanetRadius = cr.getInt();
           Color PlanetColor = cr.getColor();
           Color AtmColor = cr.getColor();
           int PlanetDomain = cr.getInt();
           int PlanetAtmosphere_speedX = cr.getInt();
           int PlanetAtmosphere_speedY = cr.getInt();
           int PlanetPrice_coef = cr.getInt();
           Planet planet = new Planet(PlanetId, PlanetName, PlanetType, PlanetRadius, PlanetSpeed, PlanetOrbit, locID, PlanetDomain, PlanetAtmosphere_speedX, PlanetAtmosphere_speedY);
           planet.color = PlanetColor;
           planet.atmophereColor = AtmColor;
           planet.price_coef = PlanetPrice_coef;
           cController.addCommand(new AddPlanetCommand(planet));
         }
         break;
       }
 
       case Command.location:
       {
         int LocationsCount = cr.getInt();
         for (int i = 0; i < LocationsCount; i++)
         {
           Location location = new Location();
           location.id = cr.getInt();
           location.starName = cr.getString();
           location.starType = cr.getInt();
           location.x = cr.getInt();
           location.y = cr.getInt();
           location.starRadius = cr.getInt();
           location.domain = cr.getInt();
           cController.addCommand(new AddLocationCommand(location));
         }
         break;
       }
 
       case Command.inPlanet:
       {
         int idPlanet = cr.getInt();
         cController.addCommand(new InPlanetCommand(idPlanet));
         break;
       }
 
       default:
         break;
     }
 
     checkCRC(command, cr);
   }
 
   private List<Equip<Item>> getEquips(CommandReader cr)
   {
     ItemCollection items = Controllers.locController.getItems();
     if (items == null)
       return null;
     List<Equip<Item>> equip = new ArrayList<Equip<Item>>();
     int eqCount = cr.getInt();
 
     for (int j = 0; j < eqCount; j++)
     {
       int equip_id = cr.getInt();
       int item_id = cr.getInt();
       int iType = cr.getInt();
       int dType = cr.getInt();
       int in_use = cr.getInt();
       int wear = cr.getInt();
       int in_planet = cr.getInt();
       int num = cr.getInt();
       double last_use = cr.getDbl();
       Equip<Item> eq = new Equip<Item>();
       eq.id = equip_id;
       eq.in_use = in_use == 1 ? true : false;
       eq.wear = wear;
       eq.in_planet = in_planet;
       eq.num = num;
       eq.last_use = last_use;
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
 
   class CommandReader
   {
     public int     address;
     public byte[]  data;
     public int     crc;
     public boolean endOfData;
 
     public CommandReader()
     {
       this.data = null;
       this.address = 0;
       this.crc = 0;
       this.endOfData = false;
     }
 
     public CommandReader(Command cmd)
     {
       this.data = cmd.data;
       this.address = 0;
       this.crc = 0;
       this.endOfData = false;
     }
 
     public void delta(int delta)
     {
       for (int i = 0; i < delta; i++)
       {
         char b = (char) (this.data[this.address + i] & 0xFF);
         this.crc = this.crc + b * (this.address + i);
       }
       this.address += delta;
       if (this.address == data.length)
       {
         this.endOfData = true;
       }
     }
 
     public int getInt()
     {
       int Res = 0;
       if (!this.endOfData)
       {
         byte[] Arr = new byte[4];
         System.arraycopy(this.data, this.address, Arr, 0, 4);
         delta(4);
         Res = byteArrayToInt(Arr);
       }
       else
       {
         Gdx.app.log("Read Data error", "getIntValue");
       }
       return Res;
     }
 
     public double getDbl()
     {
       double Res = 0.0f;
       if (!this.endOfData)
       {
         byte[] Arr = new byte[8];
         System.arraycopy(this.data, this.address, Arr, 0, 8);
         delta(8);
         Res = byteArrayToDouble(Arr);
       }
       else
       {
         Gdx.app.log("Read Data error", "getDoubleValue");
       }
       return Res;
     }
 
     public String getString()
     {
       String Res = "";
       if (!this.endOfData)
       {
         int SL = this.getInt();
         byte[] Arr = new byte[SL];
         System.arraycopy(this.data, this.address, Arr, 0, SL);
         delta(SL);
         try
         {
           Res = new String(Arr, "UTF-16LE");
         } catch (UnsupportedEncodingException e)
         {
           Gdx.app.log(" ", ": " + e.getMessage());
         }
       }
       else
       {
         Gdx.app.log("Read Data error", "getStringValue");
       }
       return Res;
     }
 
     private Color getColor()
     {
       Color Res = null;
       if (!this.endOfData)
       {
         byte[] Arr = new byte[4];
 
         System.arraycopy(this.data, this.address, Arr, 0, 4);
         delta(4);
         char R = (char) (Arr[0] & 0xFF);
         char G = (char) (Arr[1] & 0xFF);
         char B = (char) (Arr[2] & 0xFF);
         char A = (char) (Arr[3] & 0xFF);
         Res = new Color(R / 255.0f, G / 255.0f, B / 255.0f, A / 255.0f);
       }
       else
       {
         Gdx.app.log("Read Data error", "getColorValue");
       }
       return Res;
     }
   }
 }
