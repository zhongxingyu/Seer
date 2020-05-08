 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ogo.spec.game.lobby;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.awt.image.DataBufferInt;
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import ogo.spec.game.model.*;
 import ogo.spec.game.multiplayer.PeerInfo;
 import ogo.spec.game.multiplayer.client.Client;
 import ogo.spec.game.multiplayer.initserver.ChatServer;
 import ogo.spec.game.multiplayer.GameProto;
 
 /**
  *
  * @author florian
  */
 public class Lobby {
     static GameRun game;
 
     static GUI theGui;
 
     static Client client;
     static ChatServer initServer;
 
     static List<PeerInfo> serverList;
 
     static boolean isHost = false;
     
     public final static String mapImagePath = "src/ogo/spec/game/lobby/Map.bmp";
     
     public static void stopGame(Player winner){
         game.close();
         JOptionPane.showMessageDialog(null, winner.getName() + " has won the game");
         
         /*try{
             Thread.sleep(2000);
         }catch (Exception e){
             e.printStackTrace();
         }*/
         System.exit(0);
     }
     
     private static int[] loadMapImage(){
         BufferedImage img = null;
         try {
             img = ImageIO.read(new File(mapImagePath));
         } catch (IOException e) {
             
         }
         
         byte[] data = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
         int[] newData = new int[data.length];
         for(int i = 0; i < data.length; i++){
             newData[i] = (data[i]+512)%256;
         }
         return newData;
     }
     
     private static int[][][] getStartingLocations(){
         int[][][] data = new int[6][3][2];
         
         data[0][0][0] = 7;
         data[0][0][1] = 27;
         data[0][1][0] = 8;
         data[0][1][1] = 26;
         data[0][2][0] = 9;
         data[0][2][1] = 25;
         
         data[1][0][0] = 34;
         data[1][0][1] = 12;
         data[1][1][0] = 35;
         data[1][1][1] = 11;
         data[1][2][0] = 36;
         data[1][2][1] = 10;
         
         data[2][0][0] = 33;
         data[2][0][1] = 39;
         data[2][1][0] = 34;
         data[2][1][1] = 38;
         data[2][2][0] = 35;
         data[2][2][1] = 37;
         
         data[3][0][0] = 16;
         data[3][0][1] = 40;
         data[3][1][0] = 17;
         data[3][1][1] = 39;
         data[3][2][0] = 18;
         data[3][2][1] = 38;
         
         data[4][0][0] = 15;
         data[4][0][1] = 11;
         data[4][1][0] = 16;
         data[4][1][1] = 10;
         data[4][2][0] = 17;
         data[4][2][1] = 9;
         
         data[5][0][0] = 41;
         data[5][0][1] = 26;
         data[5][1][0] = 42;
         data[5][1][1] = 25;
         data[5][2][0] = 43;
         data[5][2][1] = 24;
         
         return data;
     }
     
     private static GameMap generateMap(){
         int[] data = loadMapImage();
         TileType[][] types = new TileType[50][50];
         for(int i = 0; i < 50; i++){
             for(int j = 0; j < 50; j++){
                 if(data[i*150 + j*3 + 0] == 0 && data[i*150 + j*3 + 1] == 0 && data[i*150 + j*3 + 2] == 0){
                     types[i][j] = TileType.LAND;
                 }else if(data[i*150 + j*3 + 0] == 255 && data[i*150 + j*3 + 1] == 255 && data[i*150 + j*3 + 2] == 255){
                     types[i][j] = TileType.DEEP_WATER;
                 }else{
                     types[i][j] = TileType.SHALLOW_WATER;
                 }
             }
         }
         
         return new GameMap(types);
     }
     
     private static Tile getFreeTile(GameMap map){
         Random r = new Random();
         int x = r.nextInt(map.getWidth());
         int y = r.nextInt(map.getHeight());
         while(map.getTile(x, y).getInhabitant() != null){
             x = r.nextInt(map.getWidth());
             y = r.nextInt(map.getHeight());
         }
         
         return map.getTile(x, y);
     }
     
     public static final int FOOD_MAX = 50;
 
     private static void initGame(int[][] data, String[] names, int id){
         Player[] players = new Player[names.length];
         for (int i = 0; i < names.length; i++) {
             players[i] = new Player(names[i], i);
         }
         GameMap map = generateMap();
         
         int[][][] startingLocs = getStartingLocations();
         
         int inhID = 0;
         
         for(int i = 0; i < data.length; i++){
             Creature[] creatures = new Creature[3];
             for(int j = 0; j < data[i].length; j++){
                 Creature inh;
                 if(data[i][j] == 0){
                     inh = new LandCreature(map.getTile(startingLocs[i][j][0],startingLocs[i][j][1]), map, i*3 + j);
                 }else if(data[i][j] == 1){
                     inh = new SeaCreature(map.getTile(startingLocs[i][j][0],startingLocs[i][j][1]), map, i*3 + j);
                 }else{
                     inh = new AirCreature(map.getTile(startingLocs[i][j][0],startingLocs[i][j][1]), map, i*3 + j);
                 }
                 map.getTile(startingLocs[i][j][0],startingLocs[i][j][1]).setInhabitant(inh);
                 
                 creatures[j] = inh;
                 inhID++;
             }
             players[i].setCreatures(creatures);
         }
         
         for(int i = 0; i < FOOD_MAX; i++){
             Tile t = getFreeTile(map);
             t.setInhabitant(new Food(inhID));
            inhID++;
         }
         Game game2 = new Game(players, map, id);
         game = new GameRun(game2, id);
         client.setTokenChangeListener(game);
     }
 
     public static void runGUI() throws Exception{
         theGui = new GUI();
         theGui.init();
     }
 
     private static String[] convertServerList(List<PeerInfo> l){
         /* Moet nog wat descriptiever worden.. */
         String[] names = new String[l.size()];
         for(int i = 0; i < l.size(); i++){
             names[i] = l.get(i).ip.toString().substring(1);
         }
         return names;
     }
 
     public static String[] getServerNames() throws Exception{
         client = new Client();
         serverList = client.findServers();
         return convertServerList(serverList);
     }
 
     static class DatagramReceiverRunnable implements Runnable
     {
         DatagramSocket sock;
 
         ConcurrentLinkedQueue<DatagramPacket> buffer = new ConcurrentLinkedQueue<DatagramPacket>();
 
         boolean read;
         
         int length;
 
         DatagramReceiverRunnable(DatagramSocket sock, int length)
         {
             this.sock = sock;
             this.read = true;
             this.length = length;
         }
 
         public void stop(){
             read = false;
         }
 
         public void run()
         {
             try {
                 while (read) {
                     DatagramPacket p = new DatagramPacket(new byte[length], 1);
 
                     sock.receive(p);
 
                     buffer.add(p);
                 }
             } catch (IOException e) {
                 if(read){
                     System.err.println("I/O Error");
                     e.printStackTrace();
                     System.exit(1);
                 }
             }
         }
     }
 
     public final static int INIT_PORT = 25945; // this is a UDP port
     public final static int INIT_LISTEN_PORT = 4444; // this is a UDP port
     public final static String BROADCAST_IP = "192.168.1.255";
     
     public final static int MAX_CONNECTION_TRIES = 20;
 
     public static boolean openLobby() throws Exception{
         isHost = true;
 
         /* Start new Server to connect to */
         initServer = new ChatServer();
         initServer.run();
 
         boolean done = false;
 
         DatagramPacket packet;
         DatagramSocket sendSock;
         DatagramSocket receiveSock = new DatagramSocket(INIT_PORT);
 
         int count = 0;
         while(!done && count < MAX_CONNECTION_TRIES){
             packet = new DatagramPacket(new byte[]{2}, 1, InetAddress.getByName(BROADCAST_IP), INIT_PORT);
             sendSock = new DatagramSocket();
             sendSock.send(packet);
 
             DatagramReceiverRunnable run = new DatagramReceiverRunnable(receiveSock, 1);
             new Thread(run).start();
 
             Thread.sleep(100);
 
             while((packet = run.buffer.poll()) != null){
                 //System.out.println("Data: " + packet.getData()[0]);
                 if(packet.getData()[0] == 2){
                     break;
                 }
 
             }
             if(packet != null){
                 //System.err.println("Found Packet!");
                 getServerNames();
 
                 PeerInfo ownServer = null;
                 for(PeerInfo p : serverList){
                     if(packet.getAddress().toString().equals(p.ip.toString())){
                         ownServer = p;
                     }
                 }
 
                 client.connectToInitServer(ownServer);
 
                 done = true;
             }else{
                 System.err.println("LOBBY: Could not find own server; unable to connect self to lobby");
             }
             run.stop();
             count ++;
         }
         receiveSock.close();
         if(!done){
             initServer.close();
         }
         return done;
     }
 
     public static void joinLobby(int serverNum) throws Exception{
         isHost = false;
         client.connectToInitServer(serverList.get(serverNum));
     }
 
     private static GameProto.IsReady.Builder parseReadyInfo(){
         int[] creatures = theGui.getCreatureInfo();
         return  GameProto.IsReady.newBuilder()
                 .setCreature1(creatures[0])
                 .setCreature2(creatures[1])
                 .setCreature3(creatures[2]);
     }
 
     static class TokenRingRunnable implements Runnable
     {
         Client client;
         public TokenRingRunnable(Client c){
             client = c;
         }
 
         public void run(){
             try{
                 client.startTokenRing();
             }catch (Exception e){
                 e.printStackTrace();
             } finally {
                 System.out.println("Closed Connection To Next Person");
                 System.exit(0);
             }
         }
     }
 
     public static void finishConnection() throws Exception
     {
         GameProto.InitialGameState data = client.receiveInitialGameState();
         
         int players = data.getDataCount()/3;
         int[][] creatureData = new int[players][3];
         String[] names = new String[players];
         for(int i = 0; i < players; i++){
             for (int j = 0; j < 3; j++) {
                 creatureData[i][j] = data.getData(i*3 + j);
             }
             names[i] = data.getNames(i);
         }
         
         int id = data.getId();
         
         client.connectToPeer();
         
         theGui.stop();
         
         initGame(creatureData, names, id);
 
         new Thread(new TokenRingRunnable(client)).start();
     }
 
     public static void setReady() throws Exception{
         GameProto.IsReady.Builder ready = parseReadyInfo();
         client.setReady(ready.setName(theGui.nickname).build());
     }
 
     static class InitConnectionRunnable implements Runnable{
         ChatServer init;
         int[][] data;
         String[] names;
         public InitConnectionRunnable(ChatServer initServer, int[][] creatureData, String[] playerNames){
             init = initServer;
             data = creatureData;
             names = playerNames;
         }
 
         public void run(){
             try{
                 init.sendInitialGameState(data, names);
                 
                 init.initConnection();
             } catch (Exception e){
                 System.err.println("Problem with Init Server:\n" + e.getMessage());
             }
         }
     }
 
     public static void startGame() throws Exception{
         assert(isHost && canStartGame());
         
         setReady();
 
         initServer.stopReadyState();
         
         int[][] creatureData = initServer.getCreatureTypes();
         
         String[] names = initServer.getNames();
         
         new Thread(new InitConnectionRunnable(initServer, creatureData, names)).start();
 
         finishConnection();
         
         initServer.close();
     }
 
     public static int getClientCount(){
         return initServer.getClientCount();
     }
 
     public static boolean canStartGame(){
         return initServer.canStartGame();
     }
 
     public static void main(String[] args) throws Exception{
         runGUI();
     }
 }
