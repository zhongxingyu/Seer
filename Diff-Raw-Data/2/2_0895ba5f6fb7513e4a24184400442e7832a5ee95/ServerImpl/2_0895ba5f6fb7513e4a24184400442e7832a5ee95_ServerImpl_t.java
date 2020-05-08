 
 package org.siriux.chat.servants;
 
 import java.util.ArrayList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ServerImpl extends ServerPOA {
 
     private static Logger logger = LoggerFactory.getLogger(ServerImpl.class);
     private ArrayList<String> peers = new ArrayList<>();
 
     @Override
     public void join(String name) throws NameAlreadyUsed {
         if (peers.contains(name)) {
             throw new NameAlreadyUsed(name + " is already in use.");
         }
         peers.add(name);
         logger.info("Added peer {}.", name);
     }
 
     @Override
     public void leave(String name) throws UnknownPeer {
         if (!peers.contains(name)) {
             throw new UnknownPeer("User " + name + " is not connected.");
         }
         peers.remove(name);
         logger.info("Removed peer {}.", name);
     }
 
     @Override
     public String[] getConnectedPeers() {
        return peers.toArray(new String[peers.size()]);
     }
 }
