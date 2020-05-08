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
 
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanPlayer;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanFlag;
 import com.sun.darkstar.example.snowman.server.interfaces.TeamColor;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanGame;
 import com.sun.darkstar.example.snowman.common.util.SingletonRegistry;
 import com.sun.darkstar.example.snowman.common.protocol.messages.ServerMessages;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EEndState;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EMOBType;
 import com.sun.darkstar.example.snowman.common.protocol.processor.IClientProcessor;
 import com.sun.darkstar.example.snowman.common.protocol.processor.IServerProcessor;
 import com.sun.darkstar.example.snowman.server.interfaces.TeamColor;
 import com.sun.darkstar.example.snowman.server.context.SnowmanAppContext;
 import com.sun.sgs.app.ClientSession;
 import com.sun.sgs.app.ClientSessionListener;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.NameNotBoundException;
 import com.sun.sgs.app.Task;
 import java.io.Serializable;
 import java.nio.ByteBuffer;
 import java.util.logging.Logger;
 
 /**
  * This class is the player's "proxy" in the world of managed
  * objects.  It implements the ClientSessionListener interface so that
  * it can be the reception point for all client session events.
  * For here, it will call other managed ibjects to respond to thsoe
  * events.
  * @author Jeffrey Kesselman
  * @author Owen Kellett
  */
 class SnowmanPlayerImpl implements SnowmanPlayer, Serializable, 
         ManagedObject, IServerProcessor {
 
     private static Logger logger = Logger.getLogger(SnowmanPlayerImpl.class.getName());
     public static final long serialVersionUID = 1L;
     private static long DEATHDELAYMS = 10 * 1000;
     private static float POSITIONTOLERANCESQD = .5f * .5f;
     
     private ManagedReference<ClientSession> sessionRef;
     private String name;
     private int wins;
     private int losses;
     private int id;
     float startX;
     float startY;
     long timestamp;
     float destX;
     float destY;
     float deltaX;
     float deltaY;
     TeamColor teamColor;
     private ManagedReference<SnowmanGame> currentGameRef;
     private boolean readyToPlay = false;
     private int hitPoints = 100;
     private SnowmanAppContext appContext;
     
     public SnowmanPlayerImpl(SnowmanAppContext appContext,
                              ClientSession session) {
         this.appContext = appContext;
         name = session.getName();
         setSession(session);
     }
     
     public void reset(){
         setHP(100);
     }
 
 
     public void setID(int id) {
         this.id = id;
     }
 
     public void setTimestampLocation(long timestamp, float x, float y) {
        startX = destX = x;
        startY = destY = y;
        deltaX = deltaY = 0;
        this.timestamp = timestamp;
     }
 
     public void setTeamColor(TeamColor color) {
         appContext.getDataManager().markForUpdate(this);
         teamColor = color;
     }
 
     private float getMovePerMS() {
     	 return 7f/1000f;
     }
 
     public void setSession(ClientSession arg0) {
         sessionRef = appContext.getDataManager().createReference(arg0);
     }
     
     public void setGame(SnowmanGame game){
         if (game == null){
             currentGameRef = null;
         } else {
             currentGameRef = appContext.getDataManager().createReference(game);
         }
     }
     
     public float ranking(){
         return 1000f*wins/losses;
     }
     
     public float getX(long time){
         if ((deltaX==0)&&(deltaY==0)){ // stopped
             return startX;
         } else {
             // interpolate
             long dur = time - timestamp;
             return startX + (deltaX*dur);
         }
     }
     
     public float getY(long time){
         if ((deltaX==0)&&(deltaY==0)){ // stopped
             return startY;
         } else {
             long dur = time - timestamp;
             return startY + (deltaY*dur);
         }
     }
     
     private boolean checkXY(long time, float xPrime, float yPrime){
     	/*System.out.println(timestamp+","+time);
         float currentX = getX(time);
         float currentY = getY(time);
         System.out.println(xPrime+","+yPrime+","+currentX+","+currentY);
         float dx = currentX - xPrime;
         float dy = currentY - yPrime;
         return ((dx*dx)+(dy*dy) < POSITIONTOLERANCESQD);*/
     	// XXX 
     	// needs to debug place checking
     	return true;
     }
     
     public int getID(){
         return id;
     }
     
     public void setReadyToPlay(boolean readyToPlay){
         this.readyToPlay = readyToPlay;
     }
     public boolean getReadyToPlay(){
         return readyToPlay;
     }
     
     public void send(ByteBuffer buff){
         buff.flip();
         sessionRef.get().send(buff);
     }
     
     public ClientSession getSession(){
         return sessionRef.get();
     }
     
      // IServerProcessor Messages
 
     public void ready() {
         readyToPlay=true;
         currentGameRef.get().startGameIfReady();
     }
 
     public void moveMe(long timestamp, float x, float y, float endx, float endy) {
        if (checkXY(timestamp,x,y)){
            startX = x;
            startY = y;
            float dx = endx-x;
            float dy = endy - y;
            float dist = (float)Math.sqrt((dx*dx)+(dy*dy));
            float time = dist/getMovePerMS();
            deltaX = dx/time;
            deltaY = dy/time;
            this.timestamp = timestamp;
            currentGameRef.get().send(null,
                    ServerMessages.createMoveMOBPkt(
                    id, startX, startY, endx, endy, timestamp));
        }
     }
 
     public void attack(long timestamp, int targetID, float x, float y) {
         if (checkXY(timestamp,x,y)){
             currentGameRef.get().attack(this,x,y,targetID,timestamp);
         }
     }
 
     public void getFlag(int flagID) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void stopMe(long timestamp, float x, float y) {
         if (checkXY(timestamp,x,y)){
             this.setTimestampLocation(timestamp, x,y);
             currentGameRef.get().send(
                     null,
                     ServerMessages.createStopMOBPkt(id, x, y));
         }
     }
     
     public float getThrowDistanceSqd(){
         //TODO put in Yis function
         return 10.0f; // temporary
     }
     
     public void setHP(int hp){
         appContext.getDataManager().markForUpdate(this);
         hitPoints = hp;
         currentGameRef.get().send(null, 
                 ServerMessages.createSetHPPkt(id, hitPoints));
         if (hitPoints<=0){ // newly dead
             appContext.getTaskManager().scheduleTask(new Task(){
                 ManagedReference<SnowmanPlayer> playerRef = 
                         appContext.getDataManager().createReference(
                             (SnowmanPlayer)SnowmanPlayerImpl.this);
                 public void run() throws Exception {
                     playerRef.get().reset();
                 }
             }, DEATHDELAYMS);
         }
     }
     
     public void doHit(){
         if (hitPoints<=0){ // already dead
             setHP(hitPoints-1);
         }
         
     }
 
    
     
     public SnowmanGame getGame() {
        return currentGameRef.get();
     }
 
     public String getName() {
         return name;
     }
 
     public IServerProcessor getProcessor() {
         return this;
     }
 
     public TeamColor getTeamColor() {
         return teamColor;
     }
 
     public void setLocation(float x, float y) {
         startX = destX = x;
         startY = destY = y;
     }
    
     
 }
 
 
