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
 
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanFlag;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanPlayer;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.ETeamColor;
 import com.sun.darkstar.example.snowman.common.protocol.messages.ServerMessages;
 import com.sun.darkstar.example.snowman.common.util.Coordinate;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanGame;
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.ObjectNotFoundException;
 import com.sun.sgs.app.Task;
 import java.io.Serializable;
 
 /**
  * This class represents a team's flag.  It can be on the map or be held by
  * a snowman
  * @author Jeffrey Kesselman
  * @author Owen Kellett
  */
 public class SnowmanFlagImpl implements SnowmanFlag, Serializable {
 
     static public final long serialVersionbUID = 1L;
     
      /**
       * This is a base number for the flag IDs to keep them
       * unqiue from other object IDs.
       */
     private static final int FLAGBASEID = 100;
 
     /**
      * The current X location
      */
     private float x;
     /**
      * The current Y location
      */
     private float y;
     
     private final float homeX;
     private final float homeY;
     
     /**
      * The X location of the centroid of the goal circle this flag must be
      * carried into to win the game
      */
     private final float goalX;
      /**
      * The Y location of the centroid of the goal circle this flag must be
      * carried into to win the game
      */
     private final float goalY;
     /**
      * This is either a reference to a SnowmanPlayer who is currently holding
      * th flsg or null if it is currently on the board
      */
     private ManagedReference<SnowmanPlayer> heldByRef = null;
     /**
      * The team color of this particular flag
      */
     private final ETeamColor flagColor;
     
     /**
      * The id of the flag.
      */
     private final int id;
     private static final long FLAG_RETURN_DELAY = 40 * 1000;
 
     private final ManagedReference<SnowmanGame> gameRef;
     
     /**
      * The constructor for a flag
      * 
      * @param game The game that this flag is a part of
      * @param teamColor The color of the team that owns the flag
      * @param flagHome flag's start position
      * @param flagGoal the goal location of the flag
      */
     public SnowmanFlagImpl(SnowmanGame game,
                            ETeamColor teamColor,
                            Coordinate flagHome,
                            Coordinate flagGoal)
     {
         id = FLAGBASEID + teamColor.ordinal();
         flagColor = teamColor;
         homeX = flagHome.getX();
         homeY = flagHome.getY();
         goalX = flagGoal.getX();
         goalY = flagGoal.getY();
         gameRef = AppContext.getDataManager().createReference(game);
     }
 
     /**
      * Returns the color of the team that owns this flag
      * @return the flag's color
      */
     public ETeamColor getTeamColor() {
         return flagColor;
     }
 
     /**
      * This sets the flag's location on the map
      * @param x the X coord of the flag
      * @param y the Y coord of the flag
      */
     public void setLocation(float x, float y) {
         AppContext.getDataManager().markForUpdate(this);
         this.x = x;
         this.y = y;
     }
 
     /**
      * Ths returns the current X coordof the flag. Can only be called when
      * not being held.
      * @return the X coordinate of the flag
      */
     public float getX() {
         assert heldByRef == null;
         return x;
 
     }
 
     /**
      * Ths returns the current Y coordof the flag. Can only be called when
      * not being held.
      * @return the Y coordinate of the flag
      */
     public float getY() {
         assert heldByRef == null;
         return y;
     }
     
     /**
      * This method sets the flag as held by a snowman. Can only be called when
      * not already being held.
      * @param player the snowman who holds the flag, or null
      */
     public void setHeldBy(SnowmanPlayer player){
        assert player != null;
        assert heldByRef == null;
         
         AppContext.getDataManager().markForUpdate(this);
         heldByRef = AppContext.getDataManager().createReference(player);
     }
     
     // Drop the flag. Must be currently held.
     public void drop(float x, float y) {
         assert heldByRef != null;
         
         setLocation(x, y);
         heldByRef = null;
         AppContext.getTaskManager().scheduleTask(
                         new FlagReturnTask(AppContext.getDataManager().createReference((SnowmanFlag)this)),
                         FLAG_RETURN_DELAY);
     }
     
     static private class FlagReturnTask implements Task, Serializable {
         private final ManagedReference<SnowmanFlag> flagRef;
         
         FlagReturnTask(ManagedReference<SnowmanFlag> flagRef) {
             this.flagRef = flagRef;
         }
         
         public void run() throws Exception {
             try {
                 flagRef.get().returnFlag();
             } catch (ObjectNotFoundException ex) {
                 return;
             }
         }
     }
     
     public void returnFlag() {
         if (!isHeld()) {
             setLocation(homeX, homeY);
             gameRef.get().send(
                                ServerMessages.createRespawnPkt(id, x, y));
         }
     }
     
     public void setID(int i) {
         assert false;
     }
     
     /**
      * This method sets the Flag ID.  A flag's ID must be unique among flags in
      * a game but may not be unique among all the objects in the game.
      * @return the flag's ID
      */
     public int getID(){
         return id;
     }
 
     public boolean isHeld() {
         return heldByRef != null;
     }
 
     public float getGoalX() {
         return goalX;
     }
     public float getGoalY() {
         return goalY;
     }
 }
