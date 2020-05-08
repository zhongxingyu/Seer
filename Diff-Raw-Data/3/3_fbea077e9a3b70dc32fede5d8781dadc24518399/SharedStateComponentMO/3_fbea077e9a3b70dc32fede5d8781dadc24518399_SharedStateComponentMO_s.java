 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.sharedstate.server;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.util.ScalableHashMap;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.messages.ErrorMessage;
 import org.jdesktop.wonderland.common.messages.OKMessage;
 import org.jdesktop.wonderland.common.messages.ResponseMessage;
 import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
 import org.jdesktop.wonderland.modules.sharedstate.common.messages.ChangeValueMessage;
 import org.jdesktop.wonderland.modules.sharedstate.common.messages.GetRequestMessage;
 import org.jdesktop.wonderland.modules.sharedstate.common.messages.GetResponseMessage;
 import org.jdesktop.wonderland.modules.sharedstate.common.messages.MapRequestMessage;
 import org.jdesktop.wonderland.modules.sharedstate.common.messages.MapResponseMessage;
 import org.jdesktop.wonderland.modules.sharedstate.common.messages.PutRequestMessage;
 import org.jdesktop.wonderland.modules.sharedstate.common.messages.RemoveRequestMessage;
 import org.jdesktop.wonderland.modules.sharedstate.common.state.SharedStateComponentServerState;
 import org.jdesktop.wonderland.modules.sharedstate.common.state.SharedStateComponentServerState.MapEntry;
 import org.jdesktop.wonderland.modules.sharedstate.common.state.SharedStateComponentServerState.SharedDataEntry;
 import org.jdesktop.wonderland.server.cell.CellComponentMO;
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
 import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
 import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;
 
 public class SharedStateComponentMO extends CellComponentMO {
 
     private static final Logger logger =
             Logger.getLogger(SharedStateComponentMO.class.getName());
 
     /** the channel from that cell */
     @UsesCellComponentMO(ChannelComponentMO.class)
     private ManagedReference<ChannelComponentMO> channelRef;
 
     /**
      * The message receiver for this component.  Most of the work happens t
      * in the receiver.
      */
     private ManagedReference<SharedMessageReceiver> receiverRef;
 
     /** whether or not we are live */
     private boolean live = false;
 
     /** a cached setup object to apply when we are set live */
     private SharedStateComponentServerState state = null;
 
     /**
      * Create a SharedStateComponent for the given cell. The cell must already
      * have a ChannelComponent otherwise this method will throw an IllegalStateException
      * @param cell
      */
     public SharedStateComponentMO(CellMO cell) {
         super(cell);
 
         // set up the reference to the receiver
         SharedMessageReceiver receiver = new SharedMessageReceiver(cell, this);
         receiverRef = AppContext.getDataManager().createReference(receiver);
     }
 
     @Override
     public void setServerState(CellComponentServerState setup) {
         if (!(setup instanceof SharedStateComponentServerState)) {
             throw new IllegalArgumentException("Not shared state component state");
         }
 
         // convert our internal data into an array of maps
         SharedStateComponentServerState sscss =
                 (SharedStateComponentServerState) setup;
 
         if (!live) {
             // cache the state for later and move on
             state = sscss;
             return;
         }
 
         // clear the existing maps
         SharedMessageReceiver receiver = receiverRef.get();
         receiver.mapsRef.get().clear();
 
         for (MapEntry me : sscss.getMaps()) {
             SharedMapImpl smi = new SharedMapImpl(me.getName(),
                                 receiverRef.get(), channelRef.get());
 
             for (SharedDataEntry sde : me.getData()) {
                 smi.put(sde.getKey(), sde.getValue());
             }
 
             receiver.addMap(me.getName(), smi);
         }
     }
 
     @Override
     public CellComponentServerState getServerState(CellComponentServerState setup) {
         if (setup == null) {
             setup = new SharedStateComponentServerState();
         }
 
         if (!(setup instanceof SharedStateComponentServerState)) {
             throw new IllegalArgumentException("Not shared state component state");
         }
 
         // convert our internal data into an array of maps
         SharedStateComponentServerState sscss =
                 (SharedStateComponentServerState) setup;
         sscss.setMaps(toMaps(receiverRef.get()));
 
         return setup;
     }
 
     public SharedMapSrv get(String name) {
         return receiverRef.get().getMap(name, true);
     }
 
     private MapEntry[] toMaps(SharedMessageReceiver recv) {
         List<MapEntry> out = new ArrayList<MapEntry>();
 
         for (Entry<String, ManagedReference<SharedMapImpl>> e :
                     recv.mapsRef.get().entrySet())
         {
             MapEntry me = new MapEntry(e.getKey());
             List<SharedDataEntry> l = new ArrayList<SharedDataEntry>();
 
             for (Entry<String, SharedData> de : e.getValue().get().entrySet()) {
                 l.add(new SharedDataEntry(de.getKey(), de.getValue()));
             }
 
             me.setData(l.toArray(new SharedDataEntry[0]));
             out.add(me);
         }
 
         return out.toArray(new MapEntry[0]);
     }
 
     @Override
     public void setLive(boolean live) {
         this.live = live;
 
         if (live) {
             // set the channel in the receiver
             receiverRef.get().setChannel(channelRef.get());
 
             // set the state
             if (state != null) {
                 setServerState(state);
                 state = null;
             }
 
             // register for the messages we care about
             channelRef.get().addMessageReceiver(MapRequestMessage.class, receiverRef.get());
             channelRef.get().addMessageReceiver(GetRequestMessage.class, receiverRef.get());
             channelRef.get().addMessageReceiver(PutRequestMessage.class, receiverRef.get());
             channelRef.get().addMessageReceiver(RemoveRequestMessage.class, receiverRef.get());
         } else {
             // unregister message receivers
             channelRef.get().removeMessageReceiver(MapRequestMessage.class);
             channelRef.get().removeMessageReceiver(GetRequestMessage.class);
             channelRef.get().removeMessageReceiver(PutRequestMessage.class);
             channelRef.get().removeMessageReceiver(RemoveRequestMessage.class);
         }
     }
 
     @Override
     protected String getClientClass() {
         return "org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent";
     }
 
     private static class SharedMessageReceiver 
             implements ComponentMessageReceiver, ManagedObject
     {
         /** the map of maps we know about, indexed by name */
         private final ManagedReference<MapOfMaps> mapsRef =
                 AppContext.getDataManager().createReference(new MapOfMaps());
 
         /** a reference to the SharedStateComponentMO */
         private ManagedReference<SharedStateComponentMO> stateRef;
 
         /** a reference to the cell MO */
         private ManagedReference<CellMO> cellRef;
 
         /** a reference to the channel component */
         private ManagedReference<ChannelComponentMO> channelRef;
 
         public SharedMessageReceiver(CellMO cell, SharedStateComponentMO state)
         {
             // create a reference to the shared data
             stateRef = AppContext.getDataManager().createReference(state);
             cellRef = AppContext.getDataManager().createReference(cell);
         }
 
         void setChannel(ChannelComponentMO channel) {
             channelRef = AppContext.getDataManager().createReference(channel);
         }
 
         @Override
         public void messageReceived(WonderlandClientSender sender,
                                     WonderlandClientID clientID,
                                     CellMessage message)
         {
             ResponseMessage response;
 
             logger.warning("[SharedStateComponentMO]: Received message: " +
                            message.getClass().getSimpleName());
 
             if (message instanceof MapRequestMessage) {
                 response = handleMapRequest(clientID, (MapRequestMessage) message);
             } else if (message instanceof GetRequestMessage) {
                 response = handleGetRequest(clientID, (GetRequestMessage) message);
             } else if (message instanceof PutRequestMessage) {
                 response = handlePutRequest(clientID, (PutRequestMessage) message);
             } else if (message instanceof RemoveRequestMessage) {
                 response = handleRemoveRequest(clientID, (RemoveRequestMessage) message);
             } else {
                 String error = "[SharedStateComponentMO]: Unknown message " +
                                "type: " + message.getClass() + " " + message;
                 logger.warning(error);
                 response = new ErrorMessage(message.getMessageID(), error);
             }
 
             // send the response to the caller
             sender.send(clientID, response);
         }
 
         private MapResponseMessage handleMapRequest(WonderlandClientID clientID,
                                                     MapRequestMessage message)
         {
             logger.warning("[SharedStateComponentMO]: Handle map req: " +
                            message.getName());
 
             // find the appropriate map
             SharedMapImpl map = getMap(message.getName(), false);
 
             // if the map doesn't exist, return an empty message
             if (map == null) {
                 List<String> l = Collections.emptyList();
                 return new MapResponseMessage(message.getMessageID(), 0, l);
             }
 
             // create a list of all keys
             Collection<String> keys = new ArrayList<String>(map.keySet());
 
             logger.warning("[SharedStateComponentMO]: Respond to map req: " +
                            keys.size() + " keys");
 
             // return the response
             return new MapResponseMessage(message.getMessageID(),
                                           map.getVersion(), keys);
 
         }
 
         private GetResponseMessage handleGetRequest(WonderlandClientID clientID,
                                                     GetRequestMessage message)
         {
             logger.warning("[SharedStateComponentMO]: Handle get req: " +
                            message.getMapName() + " " + message.getPropertyName());
 
             // find the appropriate map
             SharedMapImpl map = getMap(message.getMapName(), false);
             
             // if the map doesn't exist, return an empty result
             if (map == null) {
                 return new GetResponseMessage(message.getMessageID(), 
                                               0, null);
             }
 
             logger.warning("[SharedStateComponentMO]: Respond to get req: " +
                            map.get(message.getPropertyName()));
 
             return new GetResponseMessage(message.getMessageID(), 
                                           map.getVersion(), 
                                           map.get(message.getPropertyName()));
         }
 
         private ResponseMessage handlePutRequest(WonderlandClientID clientID,
                                                  PutRequestMessage message)
         {
             logger.warning("[SharedStateComponentMO]: Handle put req: " +
                            message.getMapName() + " " +
                            message.getPropertyName() + " " +
                            message.getPropertyValue().toString());
 
             // find the appropriate map
             SharedMapImpl map = getMap(message.getMapName(), true);
 
             if (map.put(clientID, message))
             {
                 return new OKMessage(message.getMessageID());
             }
 
             return new ErrorMessage(message.getMessageID(), "Request vetoed");
         }
 
         private ResponseMessage handleRemoveRequest(WonderlandClientID clientID,
                                                     RemoveRequestMessage message)
         {
             logger.warning("[SharedStateComponentMO]: Handle remove req: " +
                            message.getMapName() + " " +
                            message.getPropertyName());
 
             // find the appropriate map
             SharedMapImpl map = getMap(message.getMapName(), false);
 
             // remove the key from the map if the map exists
             if (map == null || map.remove(clientID, message)) {
                 return new OKMessage(message.getMessageID());
             }
 
             return new ErrorMessage(message.getMessageID(), "Request vetied");
         }
 
         private SharedMapImpl getMap(String name, boolean create) {
             MapOfMaps maps = mapsRef.get();
 
             ManagedReference<SharedMapImpl> mapRef = maps.get(name);
             if (mapRef == null && create) {
                 SharedMapImpl map = new SharedMapImpl(name, this, channelRef.get());
                 mapRef = addMap(name, map);
             } else if (mapRef == null) {
                 logger.warning("[SharedMap] Request for unknown map: " + name);
                 return null;
             }
 
             return mapRef.get();
         }
 
         private ManagedReference<SharedMapImpl> addMap(String mapName, SharedMapImpl map) {
             logger.warning("[SharedStateComponentMO]: creating map " + mapName);
 
             MapOfMaps maps = mapsRef.get();
 
             ManagedReference<SharedMapImpl> mapRef =
                     AppContext.getDataManager().createReference(map);
             maps.put(mapName, mapRef);
 
             return mapRef;
         }
 
         private void removeMap(String mapName) {
             logger.warning("[SharedStateComponentMO]: removing map " + mapName);
 
             MapOfMaps maps = mapsRef.get();
             ManagedReference<SharedMapImpl> mapRef = maps.remove(mapName);
             AppContext.getDataManager().removeObject(mapRef.get());
         }
 
         public void recordMessage(WonderlandClientSender sender,
                                   WonderlandClientID clientID,
                                   CellMessage message)
         {
             RecorderManager.getDefaultManager().recordMessage(sender, clientID,
                                                               message);
         }
     }
 
     static class MapOfMaps
             extends ScalableHashMap<String, ManagedReference<SharedMapImpl>>
     {
     }
 
     static class SharedMapImpl extends ScalableHashMap<String, SharedData>
         implements SharedMapSrv
     {
         /** version number must get incremented on every change to the map */
         private long version = 0;
 
         /** the name of this map */
         private String name;
 
         /** listeners */
         private Set<SharedMapListenerSrv> listeners =
                 new LinkedHashSet<SharedMapListenerSrv>();
 
         /** the enclosing listener */
         private ManagedReference<SharedMessageReceiver> receiverRef;
 
         /** the channel */
         private ManagedReference<ChannelComponentMO> channelRef;
 
         public SharedMapImpl(String name, SharedMessageReceiver receiver,
                              ChannelComponentMO channel)
         {
             super();
 
             this.name = name;
             this.receiverRef = AppContext.getDataManager().createReference(receiver);
             this.channelRef = AppContext.getDataManager().createReference(channel);
         }
 
         public String getName() {
             return name;
         }
 
         long getVersion() {
             return version;
         }
 
         public <T extends SharedData> T get(String key, Class<T> type) {
             return (T) super.get(key);
         }
 
         @Override
         public void clear() {
             throw new UnsupportedOperationException("SharedMap does not " +
                                                     " support clear");
         }
 
         /**
          * A value change originated locally. Server-side listeners are not
          * notified in this case, but a message is sent to remote clients.
          */
         @Override
         public SharedData put(String key, SharedData value) {
             return doPut(null, key, value);
         }
 
         /**
          * A value change originated by a remote client.  Server-side listeners
          * are notified, and a message is sent to remote clients.
          */
         boolean put(WonderlandClientID senderID, PutRequestMessage message)
         {
             String key = message.getPropertyName();
             SharedData value =  message.getPropertyValue();
             SharedData prev = get(key);
 
             // notify listeners, see if they veto
             if (firePropertyChange(senderID, message, key, prev, value)) {
                 doPut(senderID, key, value);
                 return true;
             }
 
             return false;
         }
 
         private SharedData doPut(WonderlandClientID senderID, String key,
                                  SharedData value)
         {
             version++;
 
             // send a message to notify all clients
             CellMessage message = ChangeValueMessage.put(getName(), version,
                                                          key, value);
             channelRef.get().sendAll(senderID, message);
 
             return super.put(key, value);
         }
 
         @Override
         public void putAll(Map<? extends String, ? extends SharedData> m) {
             version++;
             super.putAll(m);
         }
 
         /**
          * A remove request originated locally. Server-side listeners are not
          * notified in this case, but a message is sent to remote clients.
          */
         @Override
         public SharedData remove(Object key) {
             return doRemove(null, (String) key);
         }
 
         /**
          * A remove request originated remotely. Server-side listeners are
          * notified in this case, and a message is sent to remote clients.
          */
         boolean remove(WonderlandClientID senderID,
                        RemoveRequestMessage message)
         {
             String key = message.getPropertyName();
             SharedData prev = get(key);
 
             // notify listeners, see if they veto
             if (firePropertyChange(senderID, message, key, prev, null)) {
                 doRemove(senderID, key);
                 return true;
             }
 
             return false;
         }
 
         private SharedData doRemove(WonderlandClientID senderID, String key) {
             version++;
 
             CellMessage message = ChangeValueMessage.remove(getName(), version,
                                                             key);
             channelRef.get().sendAll(senderID, message);
 
             SharedData prev = super.remove(key);
 
             // if the map is now empty, remove it from tha map of maps
             if (isEmpty()) {
                 receiverRef.getForUpdate().removeMap(getName());
             }
 
             return prev;
         }
 
         public void addSharedMapListener(SharedMapListenerSrv listener) {
             if (listener instanceof ManagedObject) {
                 listener = new ListenerMOWrapper(listener);
             }
 
             listeners.add(listener);
         }
 
         public void removeSharedMapListener(SharedMapListenerSrv listener) {
             if (listener instanceof ManagedObject) {
                 listener = new ListenerMOWrapper(listener);
             }
 
             listeners.remove(listener);
         }
 
         protected boolean firePropertyChange(WonderlandClientID senderID,
                 CellMessage message, String key, SharedData oldVal,
                 SharedData newVal)
         {
             for (SharedMapListenerSrv listener : listeners) {
 
                 SharedMapEventSrv event = new SharedMapEventSrv(
                         this, senderID, message, key, oldVal, newVal);
                 if (!listener.propertyChanged(event)) {
                     return false;
                 }
             }
 
             return true;
         }
     }
 
     static class ListenerMOWrapper
             implements Serializable, SharedMapListenerSrv
     {
         private ManagedReference<SharedMapListenerSrv> listenerRef;
 
         public ListenerMOWrapper(SharedMapListenerSrv listener) {
             listenerRef = AppContext.getDataManager().createReference(listener);
         }
 
         public boolean propertyChanged(SharedMapEventSrv event)
         {
             return listenerRef.get().propertyChanged(event);
         }
 
         @Override
         public boolean equals(Object obj) {
             if (!(obj instanceof ListenerMOWrapper)) {
                 return false;
             }
 
             ListenerMOWrapper o = (ListenerMOWrapper) obj;
             return listenerRef.equals(o.listenerRef);
         }
     }
 }
