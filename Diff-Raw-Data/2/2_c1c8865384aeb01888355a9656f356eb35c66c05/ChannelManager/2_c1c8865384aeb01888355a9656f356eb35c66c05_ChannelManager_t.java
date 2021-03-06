 /*
  *  Copyright 2004-2006 Stefan Reuter
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 package org.asteriskjava.live.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.asteriskjava.live.AsteriskChannel;
 import org.asteriskjava.live.ChannelState;
 import org.asteriskjava.live.Extension;
 import org.asteriskjava.live.HangupCause;
 import org.asteriskjava.manager.event.HangupEvent;
 import org.asteriskjava.manager.event.LinkEvent;
 import org.asteriskjava.manager.event.NewCallerIdEvent;
 import org.asteriskjava.manager.event.NewChannelEvent;
 import org.asteriskjava.manager.event.NewExtenEvent;
 import org.asteriskjava.manager.event.NewStateEvent;
 import org.asteriskjava.manager.event.RenameEvent;
 import org.asteriskjava.manager.event.StatusEvent;
 import org.asteriskjava.manager.event.UnlinkEvent;
 import org.asteriskjava.util.DateUtil;
 import org.asteriskjava.util.Log;
 import org.asteriskjava.util.LogFactory;
 
 
 /**
  * Manages channel events on behalf of an AsteriskManager.
  * 
  * @author srt
  * @version $Id$
  */
 public class ChannelManager
 {
     private final Log logger = LogFactory.getLog(getClass());
 
     private final ManagerConnectionPool connectionPool;
     
     /**
      * A map of all active channel by their unique id.
      */
     private final Map<String, AsteriskChannelImpl> channels;
 
     /**
      * Creates a new instance.
      */
     public ChannelManager(ManagerConnectionPool connectionPool)
     {
         this.connectionPool = connectionPool;
         this.channels = new HashMap<String, AsteriskChannelImpl>();
     }
     
     public void clear()
     {
         synchronized (channels)
         {
             channels.clear();
         }
     }
 
     public Collection<AsteriskChannel> getChannels()
     {
         Collection<AsteriskChannel> copy;

         synchronized (channels)
         {
             copy = new ArrayList<AsteriskChannel>(channels.values());
         }
         return copy;
     }
 
     private void addChannel(AsteriskChannelImpl channel)
     {
         synchronized (channels)
         {
             channels.put(channel.getId(), channel);
         }
     }
 
     private void removeChannel(AsteriskChannel channel)
     {
         synchronized (channels)
         {
             channels.remove(channel.getId());
         }
     }
 
     private AsteriskChannelImpl createChannel(String uniqueId, String name, 
             Date dateOfCreation, String callerIdNumber, String callerIdName, 
             ChannelState state)
     {
         AsteriskChannelImpl channel;
         
         channel = new AsteriskChannelImpl(connectionPool, name, uniqueId, dateOfCreation);
         channel.setCallerIdNumber(callerIdNumber);
         channel.setCallerIdName(callerIdName);
         channel.setState(state);
 
         return channel;
     }
     
     private ChannelState string2ChannelState(String state)
     {
         if (state == null)
         {
             return null;
         }
         
         return ChannelState.valueOf(state.toUpperCase());
     }
     
     public void handleStatusEvent(StatusEvent event)
     {
         AsteriskChannelImpl channel;
         Extension extension;
         boolean isNew = false;
 
         channel = getChannelImplById(event.getUniqueId());
         if (channel == null)
         {
             Date dateOfCreation;
             
             if (event.getSeconds() != null)
             {
                 dateOfCreation = new Date(DateUtil.getDate().getTime()
                         - (event.getSeconds().intValue() * 1000));
             }
             else
             {
                 dateOfCreation = DateUtil.getDate();
             }
             channel = new AsteriskChannelImpl(connectionPool, event.getChannel(), event.getUniqueId(), dateOfCreation);
             isNew = true;
         }
 
         if (event.getContext() == null && event.getExtension() == null
                 && event.getPriority() == null)
         {
             extension = null;
         }
         else
         {
             extension = new ExtensionImpl(
                     event.getDateReceived(), event.getContext(), 
                     event.getExtension(), event.getPriority());
         }
 
         synchronized (channel)
         {
             channel.setCallerIdNumber(event.getCallerIdNum());
             channel.setCallerIdName(event.getCallerIdName());
             channel.setAccount(event.getAccount());
             if (event.getState() != null)
             {
                 channel.setState(ChannelState.valueOf(event.getState().toUpperCase()));
             }
             channel.addExtension(extension);
 
             if (event.getLink() != null)
             {
                 AsteriskChannelImpl linkedChannel = getChannelImplByName(event.getLink());
                 if (linkedChannel != null)
                 {
                     channel.setLinkedChannel(linkedChannel);
                     synchronized (linkedChannel)
                     {
                         linkedChannel.setLinkedChannel(channel);
                     }
                 }
             }
         }
 
         if (isNew)
         {
             logger.info("Adding new channel " + channel.getName());
             addChannel(channel);
         }
     }
 
     public AsteriskChannelImpl getChannelImplByName(String name)
     {
         AsteriskChannelImpl channel = null;
 
         synchronized (channels)
         {
             for (AsteriskChannelImpl tmp : channels.values())
             {
                 if (tmp.getName() != null && tmp.getName().equals(name))
                 {
                     channel = tmp;
                 }
             }
         }
         return channel;
     }
 
     public AsteriskChannelImpl getChannelImplById(String id)
     {
         AsteriskChannelImpl channel = null;
 
         synchronized (channels)
         {
             channel = channels.get(id);
         }
         return channel;
     }
     
     public void handleNewChannelEvent(NewChannelEvent event)
     {
         AsteriskChannelImpl channel;
 
         channel = getChannelImplById(event.getUniqueId());
         if (channel == null)
         {
             channel = createChannel(
                     event.getUniqueId(), event.getChannel(), event.getDateReceived(), 
                     event.getCallerId(), event.getCallerIdName(),
                     string2ChannelState(event.getState()));
             logger.info("Adding channel " + channel.getName());
             addChannel(channel);
         }
         else
         {
             // channel had already been created probably by a NewCallerIdEvent
             synchronized (channel)
             {
                 channel.setName(event.getChannel());
                 channel.setCallerIdNumber(event.getCallerId());
                 channel.setCallerIdName(event.getCallerIdName());
                 channel.setState(string2ChannelState(event.getState()));
             }
         }
     }
 
     public void handleNewExtenEvent(NewExtenEvent event)
     {
         AsteriskChannelImpl channel;
         Extension extension;
 
         channel = getChannelImplById(event.getUniqueId());
         if (channel == null)
         {
             logger.error("Ignored NewExtenEvent for unknown channel "
                     + event.getChannel());
             return;
         }
 
         extension = new ExtensionImpl(
                 event.getDateReceived(), event.getContext(),
                 event.getExtension(), event.getPriority(), 
                 event.getApplication(), event.getAppData());
 
         synchronized (channel)
         {
             channel.addExtension(extension);
         }
     }
 
     public void handleNewStateEvent(NewStateEvent event)
     {
         AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
 
         if (channel == null)
         {
             logger.error("Ignored NewStateEvent for unknown channel "
                     + event.getChannel());
             return;
         }
 
         if (event.getState() != null)
         {
             synchronized (channel)
             {
                 channel.setState(ChannelState.valueOf(event.getState().toUpperCase()));
             }
         }
     }
 
     public void handleNewCallerIdEvent(NewCallerIdEvent event)
     {
         AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
 
         if (channel == null)
         {
             // NewCallerIdEvent can occur before NewChannelEvent
             channel = createChannel(
                     event.getUniqueId(), event.getChannel(), event.getDateReceived(), 
                     event.getCallerId(), event.getCallerIdName(), ChannelState.DOWN);
             logger.info("Adding channel " + channel.getName());
             addChannel(channel);
         }
         else
         {
             synchronized (channel)
             {
                 channel.setCallerIdNumber(event.getCallerId());
                 channel.setCallerIdName(event.getCallerIdName());
             }
         }
     }
 
     public void handleHangupEvent(HangupEvent event)
     {
         HangupCause cause = null;
         AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
 
         if (channel == null)
         {
             logger.error("Ignored HangupEvent for unknown channel "
                     + event.getChannel());
             return;
         }
 
         if (event.getCause() != null)
         {
             cause = HangupCause.getByCode(event.getCause());
         }
         
         synchronized (channel)
         {
             channel.setHangupCause(cause);
             channel.setHangupCauseText(event.getCauseTxt());
             channel.setState(ChannelState.HUNGUP);
         }
 
         logger.info("Removing channel " + channel.getName() + " due to hangup (" + cause + ")");
         removeChannel(channel);
     }
 
     public void handleLinkEvent(LinkEvent event)
     {
         AsteriskChannelImpl channel1 = getChannelImplById(event.getUniqueId1());
         AsteriskChannelImpl channel2 = getChannelImplById(event.getUniqueId2());
 
         if (channel1 == null)
         {
             logger.error("Ignored LinkEvent for unknown channel "
                     + event.getChannel1());
             return;
         }
         if (channel2 == null)
         {
             logger.error("Ignored LinkEvent for unknown channel "
                     + event.getChannel2());
             return;
         }
 
         logger.info("Linking channels " + channel1.getName() + " and "
                 + channel2.getName());
         synchronized (channel1)
         {
             channel1.setLinkedChannel(channel2);
         }
         
         synchronized (channel2)
         {
             channel2.setLinkedChannel(channel1);
         }
     }
 
     public void handleUnlinkEvent(UnlinkEvent event)
     {
         AsteriskChannelImpl channel1 = getChannelImplByName(event.getChannel1());
         AsteriskChannelImpl channel2 = getChannelImplByName(event.getChannel2());
 
         if (channel1 == null)
         {
             logger.error("Ignored UnlinkEvent for unknown channel "
                     + event.getChannel1());
             return;
         }
         if (channel2 == null)
         {
             logger.error("Ignored UnlinkEvent for unknown channel "
                     + event.getChannel2());
             return;
         }
 
         logger.info("Unlinking channels " + channel1.getName() + " and "
                 + channel2.getName());
         synchronized (channel1)
         {
             channel1.setLinkedChannel(null);
         }
 
         synchronized (channel2)
         {
             channel2.setLinkedChannel(null);
         }
     }
 
     public void handleRenameEvent(RenameEvent event)
     {
         AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
 
         if (channel == null)
         {
             logger.error("Ignored RenameEvent for unknown channel with uniqueId "
                             + event.getUniqueId());
             return;
         }
 
         logger.info("Renaming channel '" + channel.getName() + "' to '"
                 + event.getNewname() + "'");
         synchronized (channel)
         {
             channel.setName(event.getNewname());
         }
     }
 }
