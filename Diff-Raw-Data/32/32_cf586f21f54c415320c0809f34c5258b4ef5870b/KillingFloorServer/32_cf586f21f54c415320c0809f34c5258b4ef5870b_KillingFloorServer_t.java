 package com.slugsource.steam.servers;
 
 import java.net.InetAddress;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author Nathan Fearnley
  */
 public class KillingFloorServer extends SourceServer
 {
 
     private volatile int serverId;
     private volatile String serverIp;
     private volatile int gamePort;
     private volatile int statusQueryPort;
     private volatile String serverName;
     private volatile String gameMap;
     private volatile String gameType;
     private volatile int numberOfPlayers;
     private volatile int maximumPlayers;
     private volatile int ping;
     private volatile int serverFlags;
     private volatile String skillLevel;
     private volatile List<GameInfo> gameInfoList = new LinkedList<>();
     private volatile List<Player> playerList = new LinkedList<>();
     
     public KillingFloorServer()
     {
     }
 
     public KillingFloorServer(InetAddress address, int port)
     {
         super(address, port);
     }
 
     public int getServerId()
     {
         return serverId;
     }
 
     public void setServerId(int serverId)
     {
         this.serverId = serverId;
     }
 
     public String getServerIp()
     {
         return serverIp;
     }
 
     public void setServerIp(String serverIp)
     {
         this.serverIp = serverIp;
     }
 
    public int getKFQueryPort()
    {
        return this.getServerAddress().getPort() - 21144;
    }
    
     public int getKFGamePort()
     {
         return gamePort;
     }
 
     public void setKFGamePort(int gamePort)
     {
         this.gamePort = gamePort;
     }
 
     public int getStatusQueryPort()
     {
         return statusQueryPort;
     }
 
     public void setStatusQueryPort(int statusQueryPort)
     {
         this.statusQueryPort = statusQueryPort;
     }
 
     public String getKFServerName()
     {
         return serverName;
     }
 
     public void setKFServerName(String serverName)
     {
         this.serverName = serverName;
     }
 
     public String getGameMap()
     {
         return gameMap;
     }
 
     public void setGameMap(String gameMap)
     {
         this.gameMap = gameMap;
     }
 
     public String getGameType()
     {
         return gameType;
     }
 
     public void setGameType(String gameType)
     {
         this.gameType = gameType;
     }
 
     public int getKFNumberOfPlayers()
     {
         return numberOfPlayers;
     }
 
     public void setKFNumberOfPlayers(int numberOfPlayers)
     {
         this.numberOfPlayers = numberOfPlayers;
     }
 
     public int getKFMaximumPlayers()
     {
         return maximumPlayers;
     }
 
     public void setKFMaximumPlayers(int maximumPlayers)
     {
         this.maximumPlayers = maximumPlayers;
     }
 
     public int getPing()
     {
         return ping;
     }
 
     public void setPing(int ping)
     {
         this.ping = ping;
     }
 
     public int getServerFlags()
     {
         return serverFlags;
     }
 
     public void setServerFlags(int serverFlags)
     {
         this.serverFlags = serverFlags;
     }
 
     public String getSkillLevel()
     {
         return skillLevel;
     }
 
     public void setSkillLevel(String skillLevel)
     {
         this.skillLevel = skillLevel;
     }
 
     public List<GameInfo> getGameInfoList()
     {
         return gameInfoList;
     }
 
     public List<Player> getPlayerList()
     {
         return playerList;
     }
 
     @Override
     public String toString()
     {
         String output = super.toString() + "\n";
         output += "--------------------------------\n";
         output += "Killing Floor Server Information\n";
         output += "--------------------------------\n";
         output += ("Server ID: " + serverId + "\n");
         output += ("Server IP: " + serverIp + "\n");
         output += ("Game Port: " + gamePort + "\n");
         output += ("Status Query Port: " + statusQueryPort + "\n");
         output += ("Server Name: " + serverName + "\n");
         output += ("Game Map: " + gameMap + "\n");
         output += ("Game Type: " + gameType + "\n");
         output += ("Number of Playes: " + numberOfPlayers + "\n");
         output += ("Maximum Players: " + maximumPlayers + "\n");
         output += ("Ping: " + ping + "\n");
         output += ("Server Flags: " + serverFlags + "\n");
         output += ("Skill Level: " + skillLevel + "\n");
         output += ("Game Info: " + gameInfoList + "\n");
         output += ("Players: " + playerList + "\n");
         
         return output;
     }
 }
