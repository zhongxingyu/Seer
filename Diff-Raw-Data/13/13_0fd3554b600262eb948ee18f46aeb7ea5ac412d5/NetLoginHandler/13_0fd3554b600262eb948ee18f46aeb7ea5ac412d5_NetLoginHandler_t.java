 package net.minecraft.src;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.*;
 import java.util.logging.Logger;
 
import org.minecraftmesh.server.MinecraftMesh;
 import org.minecraftmesh.server.mod.Packet300ModPayload;
 
 import net.minecraft.server.MinecraftServer;
 
 public class NetLoginHandler extends NetHandler
 {
     /** The Minecraft logger. */
     public static Logger logger = Logger.getLogger("Minecraft");
 
     /** The Random object used to generate serverId hex strings. */
     private static Random rand = new Random();
 
     /** The underlying network manager for this login handler. */
     public NetworkManager netManager;
 
     /**
      * Returns if the login handler is finished and can be removed. It is set to true on either error or successful
      * login.
      */
     public boolean finishedProcessing;
 
     /** Reference to the MinecraftServer object. */
     private MinecraftServer mcServer;
 
     /** While waiting to login, if this field ++'s to 600 it will kick you. */
     private int loginTimer;
 
     /** The username for this login. */
     private String username;
 
     /** holds the login packet of the current getting handled login packet */
     private Packet1Login packet1login;
 
     /**
      * The hex string that corresponds to the random number generated as a server ID. Used in online mode.
      */
     private String serverId;
 
     public NetLoginHandler(MinecraftServer par1MinecraftServer, Socket par2Socket, String par3Str) throws IOException
     {
         finishedProcessing = false;
         loginTimer = 0;
         username = null;
         packet1login = null;
         serverId = "";
         mcServer = par1MinecraftServer;
         netManager = new NetworkManager(par2Socket, par3Str, this);
         netManager.chunkDataSendCounter = 0;
     }
 
     /**
      * Logs the user in if a login packet is found, otherwise keeps processing network packets unless the timeout has
      * occurred.
      */
     public void tryLogin()
     {
         if (packet1login != null)
         {
             doLogin(packet1login);
             packet1login = null;
         }
 
         if (loginTimer++ == 600)
         {
             kickUser("Took too long to log in");
         }
         else
         {
             netManager.processReadPackets(null);
         }
     }
 
     /**
      * Disconnects the user with the given reason.
      */
     public void kickUser(String par1Str)
     {
         try
         {
             logger.info((new StringBuilder()).append("Disconnecting ").append(getUserAndIPString()).append(": ").append(par1Str).toString());
             netManager.addToSendQueue(new Packet255KickDisconnect(par1Str));
             netManager.serverShutdown();
             finishedProcessing = true;
         }
         catch (Exception exception)
         {
             exception.printStackTrace();
         }
     }
 
     public void handleHandshake(Packet2Handshake par1Packet2Handshake)
     {
         if (mcServer.onlineMode)
         {
             serverId = Long.toString(rand.nextLong(), 16);
             netManager.addToSendQueue(new Packet2Handshake(serverId));
         }
         else
         {
             netManager.addToSendQueue(new Packet2Handshake("-"));
         }
     }
 
     public void handleLogin(Packet1Login par1Packet1Login)
     {
         username = par1Packet1Login.username;
 
         if (par1Packet1Login.protocolVersion != 29)
         {
             if (par1Packet1Login.protocolVersion > 29)
             {
                 kickUser("Outdated server!");
             }
             else
             {
                 kickUser("Outdated client!");
             }
 
             return;
         }
 
         if (!mcServer.onlineMode)
         {
             doLogin(par1Packet1Login);
         }
         else
         {
             (new ThreadLoginVerifier(this, par1Packet1Login)).start();
         }
     }
 
     /**
      * Processes the login packet and sends response packets to the user.
      */
     public void doLogin(Packet1Login par1Packet1Login)
     {
         EntityPlayerMP entityplayermp = mcServer.configManager.login(this, par1Packet1Login.username);
 
         if (entityplayermp != null)
         {
             mcServer.configManager.readPlayerDataFromFile(entityplayermp);
             entityplayermp.setWorld(mcServer.getWorldManager(entityplayermp.dimension));
             entityplayermp.itemInWorldManager.setWorld((WorldServer)entityplayermp.worldObj);
             logger.info((new StringBuilder()).append(getUserAndIPString()).append(" logged in with entity id ").append(entityplayermp.entityId).append(" at (").append(entityplayermp.posX).append(", ").append(entityplayermp.posY).append(", ").append(entityplayermp.posZ).append(")").toString());
             WorldServer worldserver = mcServer.getWorldManager(entityplayermp.dimension);
             ChunkCoordinates chunkcoordinates = worldserver.getSpawnPoint();
             entityplayermp.itemInWorldManager.func_35695_b(worldserver.getWorldInfo().getGameType());
             NetServerHandler netserverhandler = new NetServerHandler(mcServer, netManager, entityplayermp);
             
             /*Minecraft Mesh Mod API: Request Mod List*/
            if(MinecraftMesh.isModSystemOn())
            {
            	Packet300ModPayload modListRq = new Packet300ModPayload();
                modListRq.modName = "MinecraftMesh";
                modListRq.payloadId = 1;
                netserverhandler.sendPacket(modListRq);
            }
             /*===================END==================*/
             
             netserverhandler.sendPacket(new Packet1Login("", entityplayermp.entityId, worldserver.getWorldInfo().getTerrainType(), entityplayermp.itemInWorldManager.getGameType(), worldserver.worldProvider.worldType, (byte)worldserver.difficultySetting, (byte)worldserver.getHeight(), (byte)mcServer.configManager.getMaxPlayers()));
             netserverhandler.sendPacket(new Packet6SpawnPosition(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ));
             netserverhandler.sendPacket(new Packet202PlayerAbilities(entityplayermp.capabilities));
             mcServer.configManager.func_28170_a(entityplayermp, worldserver);
             mcServer.configManager.sendPacketToAllPlayers(new Packet3Chat((new StringBuilder()).append("\247e").append(entityplayermp.username).append(" joined the game.").toString()));
             mcServer.configManager.playerLoggedIn(entityplayermp);
             netserverhandler.teleportTo(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
             mcServer.networkServer.addPlayer(netserverhandler);
             netserverhandler.sendPacket(new Packet4UpdateTime(worldserver.getWorldTime()));
             PotionEffect potioneffect;
 
             for (Iterator iterator = entityplayermp.getActivePotionEffects().iterator(); iterator.hasNext(); netserverhandler.sendPacket(new Packet41EntityEffect(entityplayermp.entityId, potioneffect)))
             {
                 potioneffect = (PotionEffect)iterator.next();
             }
 
             entityplayermp.func_20057_k();
         }
 
         finishedProcessing = true;
     }
 
     public void handleErrorMessage(String par1Str, Object par2ArrayOfObj[])
     {
         logger.info((new StringBuilder()).append(getUserAndIPString()).append(" lost connection").toString());
         finishedProcessing = true;
     }
 
     /**
      * Handle a server ping packet.
      */
     public void handleServerPing(Packet254ServerPing par1Packet254ServerPing)
     {
         try
         {
             String s = (new StringBuilder()).append(mcServer.motd).append("\247").append(mcServer.configManager.playersOnline()).append("\247").append(mcServer.configManager.getMaxPlayers()).toString();
             netManager.addToSendQueue(new Packet255KickDisconnect(s));
             netManager.serverShutdown();
             mcServer.networkServer.func_35505_a(netManager.getSocket());
             finishedProcessing = true;
         }
         catch (Exception exception)
         {
             exception.printStackTrace();
         }
     }
 
     public void registerPacket(Packet par1Packet)
     {
         kickUser("Protocol error");
     }
 
     /**
      * Returns the user name (if any) and the remote address as a string.
      */
     public String getUserAndIPString()
     {
         if (username != null)
         {
             return (new StringBuilder()).append(username).append(" [").append(netManager.getRemoteAddress().toString()).append("]").toString();
         }
         else
         {
             return netManager.getRemoteAddress().toString();
         }
     }
 
     /**
      * determine if it is a server handler
      */
     public boolean isServerHandler()
     {
         return true;
     }
 
     /**
      * Returns the server Id randomly generated by this login handler.
      */
     static String getServerId(NetLoginHandler par0NetLoginHandler)
     {
         return par0NetLoginHandler.serverId;
     }
 
     /**
      * Sets and returns the login packet provided.
      */
     static Packet1Login setLoginPacket(NetLoginHandler par0NetLoginHandler, Packet1Login par1Packet1Login)
     {
         return par0NetLoginHandler.packet1login = par1Packet1Login;
     }
 }
