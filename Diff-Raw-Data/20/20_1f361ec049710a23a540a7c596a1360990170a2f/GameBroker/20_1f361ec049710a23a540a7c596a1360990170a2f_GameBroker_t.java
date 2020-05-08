 package org.vaadin.artur.stresscardgame.broker;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.vaadin.artur.stresscardgame.engine.StressGameEngine;
 
 public class GameBroker {
     private static Map<String, BrokerClient> pendingClients = new HashMap<String, BrokerClient>();
 
    public synchronized static void register(final BrokerClient client) {
         if (pendingClients.isEmpty()) {
             pendingClients.put(UUID.randomUUID().toString(), client);
         } else {
             String uuid = pendingClients.keySet().iterator().next();
             // TODO Check client is still there
            final BrokerClient otherclient = pendingClients.remove(uuid);

            new Thread(new Runnable() {
 
                @Override
                public void run() {
                    StressGameEngine gameEngine = new StressGameEngine();
                    client.gameLaunching(gameEngine);
                    otherclient.gameLaunching(gameEngine);
                }
            }).start();
         }
     }
 
 }
