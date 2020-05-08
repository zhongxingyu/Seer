 package com.slugsource.steam.servers.readers;
 
 import com.slugsource.steam.serverbrowser.NotAServerException;
 import com.slugsource.steam.servers.SourceServer;
 
 /**
  *
  * @author Nathan Fearnley
  */
 public class SourceServerReader extends ServerReader<SourceServer>
 {
 
     public SourceServerReader()
     {
     }
 
     @Override
     public void readServer(byte[] rawdata, SourceServer server) throws NotAServerException
     {
         this.index = 0;
         this.data = rawdata;
 
         int prefix = readUInt32();
         if (prefix != 0xFFFFFFFF)
         {
             throw new NotAServerException("Prefix does not match.");
         }
 
         int type = readUInt8();
        if (type != 49)
         {
             throw new NotAServerException("Wrong response type.");
         }
         
         server.setVersion(readUInt8());
         server.setServerName(readNullTerminatedString());
         server.setMap(readNullTerminatedString());
         server.setGameDirectory(readNullTerminatedString());
         server.setGameDescription(readNullTerminatedString());
         server.setAppId(readUInt16());
         server.setNumberOfPlayers(readUInt8());
         server.setMaximumPlayers(readUInt8());
         server.setNumberOfBots(readUInt8());
         server.setDedicated(readChar());
         server.setOperatingSystem(readChar());
         server.setPassword(readBoolean());
         server.setVacSecured(readBoolean());
         server.setGameVersion(readNullTerminatedString());
 
         int extraDataFlag = readUInt8();
         server.setHasGamePort((extraDataFlag & 0x80) != 0);
         server.setHasSteamId((extraDataFlag & 0x10) != 0);
         server.setHasSpectatorServer((extraDataFlag & 0x40) != 0);
         server.setHasGameTagDataString((extraDataFlag & 0x20) != 0);
         server.setHasGameId((extraDataFlag & 0x01) != 0);
 
         if (server.hasGamePort())
         {
             server.setGamePort(readUInt8());
         }
 
         if (server.hasSteamId())
         {
             server.setSteamId(readUInt64());
         }
 
         if (server.hasSpectatorServer())
         {
             server.setSpectatorServerPort(readUInt16());
             server.setSpectatorServerName(readNullTerminatedString());
         }
 
         if (server.hasGameTagDataString())
         {
             server.setGameTagDataString(readNullTerminatedString());
             server.setDifficulty(server.getGameTagDataString().charAt(4));
         }
 
         if (server.hasGameId())
         {
             server.setGameId(readUInt64());
         }
     }
 }
