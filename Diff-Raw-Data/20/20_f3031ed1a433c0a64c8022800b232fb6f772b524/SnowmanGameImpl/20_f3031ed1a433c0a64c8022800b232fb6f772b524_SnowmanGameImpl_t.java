 /*
 * Copyright (c) 2008, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 
 
 package com.sun.darkstar.example.snowman.server.impl;
 
 import com.sun.darkstar.example.snowman.common.protocol.messages.ServerMessages;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EMOBType;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanGame;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanFlag;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanPlayer;
 import com.sun.darkstar.example.snowman.server.interfaces.TeamColor;
 import com.sun.darkstar.example.snowman.server.interfaces.EntityFactory;
 import com.sun.darkstar.example.snowman.server.context.SnowmanAppContext;
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.Channel;
 import com.sun.sgs.app.ClientSession;
 import com.sun.sgs.app.Delivery;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ManagedReference;
 import java.io.Serializable;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This object represents an actual running game session of Project Snowman,
  * @author Jeffrey Kesselman
  * @author Owen Kellett
  */
 public class SnowmanGameImpl implements SnowmanGame, ManagedObject, Serializable {
     static final long serialVersionUID = 1L;
     /**
      * A prefix that is appended to the darkstar bound name for
      * all game channels.
      */
     static final String CHANPREFIX = "_GAMECHAN_";
     /**
      * This is a base number for the flag IDs to keep them
      * unqiue from other object IDs.
      */
     static final private int FLAGBASEID = 100;
     /**
      * Where players start at th begging of the game
      * 
      * player starts == x1,y1,x2,y2....
      */
     
     static final float[] playerStarts = {0,0,10,0,0,10,10,10};
 
     /** 
      * where flags start at the beginning of the game
      * 
      * flag starts == x1,y1,x2,y2.....
      */
     static final float[] flagStarts={1,1,9,9};
     
     /**
      * The goals for the flags
      * 
      * flag goals == x1,y1,r1,x2,y2,r2 ...
      */
  
     static final float[] flagGoals={1,1,1,9,9,1};
     
     static final int PLAYERIDSTART = 1;
     
     /**
      * A reference to a channel that is used to send game packets to
      * all the players in this game session
      */
     ManagedReference<Channel> channelRef;
     List<ManagedReference<SnowmanFlag>> flags = 
             new ArrayList<ManagedReference<SnowmanFlag>>();
     
     
     private List<ManagedReference<SnowmanPlayer>>  playerRefs = 
             new ArrayList<ManagedReference<SnowmanPlayer>>();
     
     private SnowmanAppContext appContext;
     private EntityFactory entityFactory;
     
     public SnowmanGameImpl(String gameName,
                            SnowmanAppContext appContext, 
                            EntityFactory entityFactory) {
         this.appContext = appContext;
         this.entityFactory = entityFactory;
         channelRef = AppContext.getDataManager().createReference(
                 AppContext.getChannelManager().createChannel(
                 CHANPREFIX+gameName, null, Delivery.RELIABLE));
         
         for(TeamColor color : TeamColor.values()){
             int idx = color.ordinal();
             SnowmanFlag flag = entityFactory.createSnowmanFlag(color,
                     flagGoals[idx*3],flagGoals[idx*3+1],flagGoals[idx*3+2]); 
             flag.setLocation(flagStarts[idx*2], flagStarts[idx*2+1]);
             ManagedReference<SnowmanFlag> ref =
                     AppContext.getDataManager().createReference(flag);
             flags.add(ref);
             flag.setID(FLAGBASEID+flags.indexOf(ref));
         }
     }
     
     public void send(ClientSession sess, ByteBuffer buff){
         buff.flip();
         channelRef.get().send(sess, buff);
         
     }
 
     public void addPlayer(SnowmanPlayer player, TeamColor color) {
         ManagedReference<SnowmanPlayer> playerRef = 
                 AppContext.getDataManager().createReference(player);
         playerRefs.add(playerRef);
        int index = playerRefs.indexOf(playerRef);
        player.setID(index + PLAYERIDSTART);
        player.setTimestampLocation(System.currentTimeMillis(),
                                    playerStarts[index*2],
                                    playerStarts[(index*2)+1]);
         player.setTeamColor(color);
         player.setGame(this);
         channelRef.get().join(player.getSession());
     }
     
     public void removePlayer(SnowmanPlayer player){
          ManagedReference<SnowmanPlayer> playerRef = 
                 AppContext.getDataManager().createReference(player);
          int idx = playerRefs.indexOf(playerRef);
         playerRefs.set(idx, null);
     }
     
     public void sendMapInfo(){
         long time = System.currentTimeMillis();
         for(ManagedReference<SnowmanPlayer> ref : playerRefs){
             SnowmanPlayer player = ref.get();
             multiSend(ServerMessages.createAddMOBPkt(
                     player.getID(), player.getX(time), player.getY(time), 
                     EMOBType.SNOWMAN));
         }
         for(ManagedReference<SnowmanFlag> flagRef : flags){
             SnowmanFlag flag = flagRef.get();
             multiSend(ServerMessages.createAddMOBPkt(
                     flag.getID(), flag.getX(time), flag.getY(time), EMOBType.FLAG));
         }
         multiSend(ServerMessages.createReadyPkt());
         
     }
     
     private void multiSend(ByteBuffer buff){
     	 for(ManagedReference<SnowmanPlayer> ref : playerRefs){
     		 ref.get().send(buff);
     	 }
     }
     
     public void startGameIfReady(){
         for(ManagedReference<SnowmanPlayer> playerRef : playerRefs){
             if (playerRef != null){
                 if (!playerRef.get().getReadyToPlay()){
                     return;
                 }
             }
         }
         startGame();
     }
     
     private SnowmanPlayer getPlayer(int id){
     	return playerRefs.get(id-PLAYERIDSTART).get();
     }
     
     public void attack(SnowmanPlayer attacker, float x, float y, int attackedID,
             long timestamp){
         SnowmanPlayer attacked = getPlayer(attackedID);
         float dx = x-attacked.getX(timestamp);
         float dy = y-attacked.getY(timestamp);
         
         if ((dx*dx)+(dy*dy) <= attacker.getThrowDistanceSqd()){
             send(null,ServerMessages.createAttackedPkt(
                     attacker.getID(), attackedID));
             attacked.doHit();
         }
                 
     }
 
     private void startGame() {
         send(null,ServerMessages.createStartGamePkt());
     }
 
 }
