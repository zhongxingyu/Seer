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
 
 import com.sun.darkstar.example.snowman.server.context.MockAppContext;
 import com.sun.darkstar.example.snowman.server.context.SnowmanAppContextFactory;
 import com.sun.darkstar.example.snowman.server.context.SnowmanAppContext;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanGame;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanFlag;
 import com.sun.darkstar.example.snowman.server.interfaces.SnowmanPlayer;
 import com.sun.darkstar.example.snowman.server.interfaces.EntityFactory;
 import com.sun.darkstar.example.snowman.server.exceptions.SnowmanFullException;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.ETeamColor;
 import com.sun.darkstar.example.snowman.common.protocol.messages.ServerMessages;
 import com.sun.sgs.app.Channel;
 import com.sun.sgs.app.ChannelManager;
 import com.sun.sgs.app.Delivery;
 import com.sun.sgs.app.ClientSession;
 import org.junit.Test;
 import org.junit.Before;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.easymock.EasyMock;
 import java.nio.ByteBuffer;
 
 /**
  *
  * @author Owen Kellett
  */
 public class SnowmanGameImplTest 
 {
     private Channel gameChannel;
     private String gameName = "GAME";
 
     @Before
     public void initializeContext()
     {
         //create the context
         MockAppContext.create();
         //update the behavior of the ChannelManager
         SnowmanAppContext appContext = SnowmanAppContextFactory.getAppContext();
         ChannelManager channelManager = appContext.getChannelManager();
         gameChannel = EasyMock.createNiceMock(Channel.class);
         EasyMock.expect(channelManager.createChannel(SnowmanGameImpl.CHANPREFIX+gameName, null, Delivery.RELIABLE)).andStubReturn(gameChannel);
         EasyMock.replay(channelManager);
         EasyMock.replay(gameChannel);
     }
     
     @After
     public void takeDownContext()
     {
         SnowmanAppContextFactory.setAppContext(null);
     }
     
     /**
      * Verify that when adding a player to an empty game, the correct
      * information for that player is set
      */
     @Test
     public void addPlayerTest() {
         //setup dummy entityfactory
         EntityFactory dummyEntityFactory = EasyMock.createMock(EntityFactory.class);
         SnowmanFlag dummyFlag = EasyMock.createNiceMock(SnowmanFlag.class);
         EasyMock.expect(dummyFlag.getID()).andStubReturn(new Integer(0));
         EasyMock.replay(dummyFlag);
        EasyMock.expect(dummyEntityFactory.createSnowmanFlag(EasyMock.isA(ETeamColor.class),
                                                             EasyMock.anyFloat(),
                                                             EasyMock.anyFloat())).andStubReturn(dummyFlag);
         EasyMock.replay(dummyEntityFactory);
         
         //create the player
         SnowmanAppContext appContext = SnowmanAppContextFactory.getAppContext();
         ClientSession session = EasyMock.createNiceMock(ClientSession.class);
         SnowmanPlayer dummyPlayer = EasyMock.createMock(SnowmanPlayer.class);
         ETeamColor color = ETeamColor.Red;
         
         //create the game
         SnowmanGame game = new SnowmanGameImpl(gameName, 4, appContext, dummyEntityFactory);
         
         //record information that should be set on the player
         dummyPlayer.setID(1);
         dummyPlayer.setLocation(EasyMock.anyFloat(), EasyMock.anyFloat());
         dummyPlayer.setTeamColor(color);
         dummyPlayer.setGame(game);
         EasyMock.expect(dummyPlayer.getSession()).andStubReturn(session);
         EasyMock.replay(dummyPlayer);
         
         //record that the player should be added to the channel
         EasyMock.resetToDefault(gameChannel);
         EasyMock.expect(gameChannel.join(session)).andReturn(gameChannel);
         EasyMock.replay(gameChannel);
         
         //add the player
         game.addPlayer(dummyPlayer, color);
         
         //verify the calls
         EasyMock.verify(dummyPlayer);
         EasyMock.verify(gameChannel);
     }
     
     
     /**
      * Verify that when adding a second player to a game, the correct
      * information for that player is set and it does not conflict with
      * the first player
      */
     @Test
     public void addSecondPlayerTest() {
         //setup dummy entityfactory
         EntityFactory dummyEntityFactory = EasyMock.createMock(EntityFactory.class);
         SnowmanFlag dummyFlag = EasyMock.createNiceMock(SnowmanFlag.class);
         EasyMock.expect(dummyFlag.getID()).andStubReturn(new Integer(0));
         EasyMock.replay(dummyFlag);
        EasyMock.expect(dummyEntityFactory.createSnowmanFlag(EasyMock.isA(ETeamColor.class),
                                                             EasyMock.anyFloat(),
                                                             EasyMock.anyFloat())).andStubReturn(dummyFlag);
         EasyMock.replay(dummyEntityFactory);
         
         //create the player
         SnowmanAppContext appContext = SnowmanAppContextFactory.getAppContext();
         ClientSession session = EasyMock.createNiceMock(ClientSession.class);
         SnowmanPlayer dummyPlayer = EasyMock.createMock(SnowmanPlayer.class);
         ETeamColor color = ETeamColor.Red;
         
         //create the second player
         ClientSession session2 = EasyMock.createNiceMock(ClientSession.class);
         SnowmanPlayer dummyPlayer2 = EasyMock.createMock(SnowmanPlayer.class);
         ETeamColor color2 = ETeamColor.Blue;
         
         //create the game
         SnowmanGame game = new SnowmanGameImpl(gameName, 4, appContext, dummyEntityFactory);
         
         //record information that should be set on the player1
         dummyPlayer.setID(1);
         dummyPlayer.setLocation(EasyMock.anyFloat(), EasyMock.anyFloat());
         dummyPlayer.setTeamColor(color);
         dummyPlayer.setGame(game);
         EasyMock.expect(dummyPlayer.getSession()).andStubReturn(session);
         EasyMock.replay(dummyPlayer);
         
         //record information that should be set on the player2
         dummyPlayer2.setID(2);
         dummyPlayer2.setLocation(EasyMock.anyFloat(), EasyMock.anyFloat());
         dummyPlayer2.setTeamColor(color2);
         dummyPlayer2.setGame(game);
         EasyMock.expect(dummyPlayer2.getSession()).andStubReturn(session2);
         EasyMock.replay(dummyPlayer2);
         
         //record that the players should be added to the channel
         EasyMock.resetToDefault(gameChannel);
         EasyMock.expect(gameChannel.join(session)).andReturn(gameChannel);
         EasyMock.expect(gameChannel.join(session2)).andReturn(gameChannel);
         EasyMock.replay(gameChannel);
         
         //add the player
         game.addPlayer(dummyPlayer, color);
         game.addPlayer(dummyPlayer2, color2);
         
         //verify the calls
         EasyMock.verify(dummyPlayer);
         EasyMock.verify(dummyPlayer2);
         EasyMock.verify(gameChannel);
     }
     
     /**
      * Verify that when adding a second player to a full game,
      * an exception is thrown
      */
     @Test(expected=SnowmanFullException.class)
     public void addPlayerFullTest() {
         //setup dummy entityfactory
         EntityFactory dummyEntityFactory = EasyMock.createMock(EntityFactory.class);
         SnowmanFlag dummyFlag = EasyMock.createNiceMock(SnowmanFlag.class);
         EasyMock.expect(dummyFlag.getID()).andStubReturn(new Integer(0));
         EasyMock.replay(dummyFlag);
        EasyMock.expect(dummyEntityFactory.createSnowmanFlag(EasyMock.isA(ETeamColor.class),
                                                             EasyMock.anyFloat(),
                                                             EasyMock.anyFloat())).andStubReturn(dummyFlag);
         EasyMock.replay(dummyEntityFactory);
         
         //create the players
         SnowmanAppContext appContext = SnowmanAppContextFactory.getAppContext();
         SnowmanPlayer dummyPlayer = EasyMock.createMock(SnowmanPlayer.class);
         SnowmanPlayer dummyPlayer2 = EasyMock.createMock(SnowmanPlayer.class);
         SnowmanPlayer dummyPlayer3 = EasyMock.createMock(SnowmanPlayer.class);
         ETeamColor color = ETeamColor.Red;
         
         //create the game
         SnowmanGame game = new SnowmanGameImpl(gameName, 4, appContext, dummyEntityFactory);
         
         //add the players
         game.addPlayer(dummyPlayer, color);
         game.addPlayer(dummyPlayer2, color);
         game.addPlayer(dummyPlayer3, color);
     }
     
     
 }
