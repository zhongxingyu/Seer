 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.github.etsai.kfsxtrackingserver;
 
 import com.github.etsai.kfsxtrackingserver.PacketParser.InvalidPacketFormatException;
 import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket;
 import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket;
 import com.github.etsai.kfsxtrackingserver.SteamPoller.InvalidSteamIDException;
 import com.github.etsai.kfsxtrackingserver.impl.PlayerContentImpl;
 import java.io.IOException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.TimerTask;
 import java.util.Timer;
 import com.github.etsai.kfsxtrackingserver.DataWriter.SteamInfo;
 import com.github.etsai.kfsxtrackingserver.PacketParser.StatPacket;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Accumulates and holds packets for processing
  * @author etsai
  */
 public class Accumulator {
     private final ConcurrentHashMap<String, PlayerContent> incompletePlayerContent;
     private final DataWriter writer;
     private final long statMsgTTL;
     private final PacketParser packetParser;
     private final Map<String, TimerTask> tasks;
     private Timer timer;
     
     public Accumulator(DataWriter writer, String password, long statMsgTTL) {
         this.incompletePlayerContent= new ConcurrentHashMap<>();
         this.writer= writer;
         this.statMsgTTL= statMsgTTL;
         this.packetParser= new PacketParser(password);
         this.timer= new Timer();
         tasks= new HashMap<>();
     }
     
     public PlayerContent getPlayerContent(String steamID64) {
         return incompletePlayerContent.get(steamID64);
     }
     public void accumulate(String data) {
         try {
             StatPacket packet= packetParser.parse(data);
             if (packet instanceof MatchPacket) {
                 writer.writeMatchData((MatchPacket)packet);
             } else if (packet instanceof PlayerPacket) {
                 PlayerPacket playerPacket= (PlayerPacket)packet;
                 PlayerContent content;
 
                 final String steamID64= playerPacket.getSteamID64();
                 if (!incompletePlayerContent.containsKey(steamID64)) {
                     incompletePlayerContent.put(steamID64, new PlayerContentImpl());
                     tasks.put(steamID64, new TimerTask() {
                         @Override public void run() {
                             Common.logger.info("Discarding packets for steamID64: $steamID64");
                             incompletePlayerContent.remove(steamID64);
                         }
                     });
                     try {
                         timer.schedule(tasks.get(steamID64), statMsgTTL);
                     } catch (IllegalStateException ex) {
                         Common.logger.log(Level.WARNING, "Error scheduling task.  Reinitializing timer", ex);
                         timer= new Timer();
                         timer.schedule(tasks.get(steamID64), statMsgTTL);
                     }
                 }
                 content= incompletePlayerContent.get(steamID64);
                 content.addPacket(playerPacket);
                 
                 if (content.isCompleted()) {
                     incompletePlayerContent.remove(steamID64);
                     tasks.remove(steamID64).cancel();
                    Common.logger.log(Level.INFO, "Saving packets for steamID64: {0}", steamID64);
                     try {
                         String[] info= SteamPoller.poll(steamID64);
                         writer.writeSteamInfo(new SteamInfo(steamID64, info[0], info[1]));
                         writer.writePlayerData(content);
                     } catch (IOException ex) {
                         Common.logger.log(Level.WARNING, "Error contacting steam community.  Saving player statistics", ex);
                         writer.writePlayerData(content);
                     } catch (InvalidSteamIDException ex) {
                         Common.logger.log(Level.SEVERE, "Invalid steamID64: $steamID64", ex);
                     } catch (Exception ex) {
                         Common.logger.log(Level.SEVERE, "Error saving player statistics", ex);
                     }
                 }
             }
         } catch (InvalidPacketFormatException ex) {
             Common.logger.log(Level.SEVERE, "Error parsing the packet", ex);
         } catch (Exception ex) {
             Common.logger.log(Level.SEVERE, null, ex);
         }
     }
 }    
