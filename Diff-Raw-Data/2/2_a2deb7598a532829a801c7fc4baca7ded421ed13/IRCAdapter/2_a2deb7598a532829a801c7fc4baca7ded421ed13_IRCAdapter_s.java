 /*
  * Copyright (c) 2012, Keeley Hoek
  * All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  * 
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.github.escortkeel.circle;
 
 import com.github.escortkeel.circle.event.IRCChannelJoinEvent;
 import com.github.escortkeel.circle.event.IRCChannelPartEvent;
 import com.github.escortkeel.circle.event.IRCConnectEvent;
 import com.github.escortkeel.circle.event.IRCMotdEvent;
 import com.github.escortkeel.circle.event.IRCPrivateMessageEvent;
 import com.github.escortkeel.circle.event.IRCRawMessageEvent;
 
 /**
  * This class represents the logic of an IRC client or "bot". Override methods
  * in this class to add functionality to your client.
  *
  * @author Keeley Hoek (escortkeel@live.com)
  */
 public class IRCAdapter {
 
     /**
      * This method is invoked by an
      * <code>IRCClient</code> instance when a connection is established.
      *
      * @param event the <code>IRCConnectEvent</code>
      */
     public void onConnect(IRCConnectEvent event) {
     }
 
     /**
      * This method is invoked by an
      * <code>IRCClient</code> instance when the MOTD is received.
      *
      * @param event the <code>IRCMotdEvent</code>
      */
     public void onMotd(IRCMotdEvent event) {
     }
 
     /**
      * This method is invoked by an
      * <code>IRCClient</code> instance when it joins a channel.
      *
      * @param event the <code>IRCChannelJoinEvent</code>
      */
     public void onChannelJoin(IRCChannelJoinEvent event) {
     }
 
     /**
      * This method is invoked by an
      * <code>IRCClient</code> instance when it parts from (leaves) a channel.
      *
      * @param event the <code>IRCChannelPartEvent</code>
      */
     public void onChannelPart(IRCChannelPartEvent event) {
     }
 
     /**
      * This method is invoked by an
      * <code>IRCClient</code> instance when a raw message is received.
      *
      * @param event the <code>IRCRawMessageEvent</code>
      */
     public void onRawMessage(IRCRawMessageEvent event) {
     }
 
     /**
      * This method is invoked by an
      * <code>IRCClient</code> instance when a private message is received.
      *
      * @param event the <code>IRCPrivateMessageEvent</code>
      */
    public void onPrivateMessage(IRCPrivateMessageEvent e) {
     }
 }
