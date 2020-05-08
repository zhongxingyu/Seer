 package mygame;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.font.BitmapText;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.network.ConnectionListener;
 import com.jme3.network.Filters;
 import com.jme3.network.HostedConnection;
 import com.jme3.network.Message;
 import com.jme3.network.MessageListener;
 import com.jme3.network.Network;
 import com.jme3.network.Server;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.shape.Box;
 import com.jme3.system.JmeContext;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mygame.util.NetworkMessages;
 import mygame.util.NetworkMessages.NetworkMessage;
 import mygame.util.NetworkMessages.ShootingResult;
 
 /**
  * test
  * @author normenhansen
  */
 public class AngryPidgeServerMain extends SimpleApplication {
     public static final int PORT = 4444;
     
     private Server myServer;
     private List<Integer> scores = Arrays.asList(0,0);
     private HashMap<Integer, Integer> connectedPlayers = new HashMap<Integer, Integer>();
     private int responseReceived = 0;
     private int round = 1;
     private int position = 1;
     private boolean startGame = true;
 
     public static void main(String[] args) {
         AngryPidgeServerMain server = new AngryPidgeServerMain();
         server.start(JmeContext.Type.Headless);
     }
 
     @Override
     public void simpleInitApp() {                    
         try {
             NetworkMessages.initializeSerializables();
             myServer = Network.createServer(PORT);
             myServer.start();
             myServer.addMessageListener(new ServerListener());
             myServer.addConnectionListener(new ServerConnectionListener());
         } catch (IOException ex) {
             Logger.getLogger(AngryPidgeServerMain.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void simpleUpdate(float tpf) {
         if(connectedPlayers.size()==4 && startGame){
             myServer.broadcast(new NetworkMessages.GameStarted());
             startGame = false;
         }
         if(responseReceived == 4){
             sendGameUpdate();
             responseReceived = 0;
         }
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
 
     private void sendGameUpdate() {
         round++;
         if(round>8){
             round = 1;
             position = 1;
             startGame = true;
             myServer.broadcast(new NetworkMessages.GameStop());
         }else{
             position++;
             myServer.broadcast(new NetworkMessages.GameUpdateMessage(position, scores.get(0), scores.get(1)));
         }
     }
     
     private void addAPointToTeam(int teamNumber) {
         if(teamNumber == 1){
             scores.set(0, scores.get(0)+1);
         }else{
             scores.set(1, scores.get(1)+1);
         }
     }
         
     private class ServerListener implements MessageListener<HostedConnection> {
         public void messageReceived(HostedConnection source, Message message) {
           if (message instanceof NetworkMessage) {
             NetworkMessage networkMessage = (NetworkMessage) message;
             System.out.println("Server received '" +networkMessage.getMessage() +"' from client #"+source.getId() );
           }
           if(message instanceof NetworkMessages.ShootingResult){
               NetworkMessages.ShootingResult shootingResultMessage = (ShootingResult) message;
               if(shootingResultMessage.isHasHitTarget()){
                   int teamNumber = connectedPlayers.get(source.getId());
                   addAPointToTeam(teamNumber);
               }
               responseReceived++;
           }
         }
     }
     
     private class ServerConnectionListener implements ConnectionListener{
 
         public void connectionAdded(Server server, HostedConnection conn) {
             if(myServer.getConnections().size()>4){
                 conn.close("Too many players");
             }else{
                 if(connectedPlayers.size() < 2){
                     connectedPlayers.put(conn.getId(), 1);
                 }else{
                     connectedPlayers.put(conn.getId(), 2);    
                 }
                myServer.broadcast(Filters.in(conn), new NetworkMessages.PlayerInfoMessage((conn.getId()+1),connectedPlayers.get(conn.getId())));
             }
         }
 
         public void connectionRemoved(Server server, HostedConnection conn) {
         }
     }
 }
