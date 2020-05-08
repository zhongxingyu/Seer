 package planegame;
 
 
 import java.io.PrintWriter;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Dmitriy
  */
 public class ServerGame {
     
     // Клиенты и их парметры для связи
     private List<ClientSocket> Client;
     private List<Room> roomsGame;
     private int randomIdUser = 1;
     private int randomIdRoom = 1;
     
     public ServerGame(){
         Client = new LinkedList<ClientSocket>();
         roomsGame = new LinkedList<Room>();
     }
     
     public void send(int idUser, String packet) {
         Client.get(sendIdPlayer(idUser)).getOut().println(packet + "\n");
     }
     
     public int sendIdPlayer(int id) {
         ClientSocket player = null;
         for(ClientSocket clientList: Client) {
             if(clientList.getPlayer().getID() == id) {
                player = clientList; 
                break;
             }
         }
         return Client.indexOf(player);
     }
     
     public Player sendPlayer(int id) {
         Player player = null;
         for(ClientSocket clientList: Client) {
             if(clientList.getPlayer().getID() == id) {
                player = clientList.getPlayer(); 
             }
         }
         return player;
     }
     
     public int sendIdRoom(int id) {
         Room room = null;
         for(Room roomList: roomsGame) {
             if(roomList.getID() == id) {
                 room = roomList;
             }
         }
         return roomsGame.indexOf(room);
     }
     
     public Room sendRoom(int id) {
         Room room = null;
         for(Room roomList: roomsGame) {
             if(roomList.getID() == id) {
                 room = roomList;
             }
         }
         return room;
     }
     
     /**
      * Method parsing string and switch what is the packet
      * @param str string packet
      * @param idUser id user 
      */
     public void Command(String str, int idUser){
         JSONParser parser = new JSONParser();
         try{
             Object obj = parser.parse(str);
             JSONObject jsonObject = (JSONObject) obj;
             String packet = (String)jsonObject.get("packet");
             System.out.println(packet);
             if(packet != null){
                 switch(packet.toLowerCase()){
                     case "connect_user":
                         //System.out.println(packet);
                         ConnectUser(jsonObject, idUser);
                         break;
                     case "create_room":
                        ///System.out.println(packet);
                         CreateRoom(jsonObject);
                         break;
                     case "connect_room":
                         //System.out.println(packet);
                         ConnectRoom(jsonObject);
                         break;
                     case "action_server":
                         //System.out.println(packet);
                         ActionServer(jsonObject);
                         break;
                     case "exit_client":
                         exitClient(jsonObject);
                         break;
                     case "all_room":
                         GetAllRoom(idUser);
                         break;
                 }
             }
         } catch (ParseException e) {
             e.printStackTrace();
         }
     }
     
     
     // Добавляем клинтов
     public int addUser(ClientSocket client){
         int result = randomIdUser;
         client.setPlayer(new Player(result));
         Client.add(client);
         randomIdUser++;
         return result;
     }
     
     public void ConnectUser(JSONObject obj, int idUser) {
         String name = (String)obj.get("name");
         Client.get(sendIdPlayer(idUser)).getPlayer().setM_name(name);
         GetConnectUser(idUser);
     }
     
     public void GetConnectUser(int idUser) {
         JSONObject packet = new JSONObject();
         packet.put("packet", "connect_user");
         packet.put("id_user", new Integer(idUser));
         
         String jsonPacket = JSONValue.toJSONString(packet);
         send(idUser, jsonPacket);
     }
     
     
     
     
     
     
     //Создаем комнату и возвращаем клиенту id-room 
     // если комнат нет или клиент хочет создать свою
     public void CreateRoom(JSONObject obj){
         String name = (String)obj.get("name");
         long count = (Long)obj.get("count");
         long idUser = (Long)obj.get("id_user");
         
         Room room = new Room((int)count, name);
         room.setID(randomIdRoom);
         Player player = sendPlayer((int)idUser);
         player.JoinRoom(room);
         roomsGame.add(room);
        roomsGame.get(sendIdRoom(randomIdUser)).StartRoom();
         
         GetPacketRoom((int)idUser, randomIdRoom);
     }
     
     public void GetPacketRoom(int idPlane, int room){
         
         JSONObject coordinate = new JSONObject();
         coordinate.put("x", new Integer(0));
         coordinate.put("y", new Integer(0));
         
         JSONObject move = new JSONObject();
         move.put("x", new Integer(0));
         move.put("y", new Integer(0));
         
         JSONObject packet = new JSONObject();
         packet.put("packet", "room");
         packet.put("id_plane", new Integer(idPlane));
         packet.put("coordinate", coordinate);
         packet.put("move", move);
         packet.put("room", new Integer(room));
         
         String jsonPacket = JSONValue.toJSONString(packet);
         System.out.println(jsonPacket);
         send(idPlane, jsonPacket);
     }
         
        
     
     
     
     
     public void ConnectRoom(JSONObject obj) {
         long idRoom = (Long)obj.get("id_room");
         long idUser = (Long)obj.get("id_user");
         
         Room room = sendRoom((int)idRoom);
         Player player = sendPlayer((int)idUser);
         player.JoinRoom(room);
           
         GetPacketRoom((int)idRoom, (int)idUser);
     }
      
     
     
     
     
     public void ActionServer(JSONObject obj) {
         long idRoom = (Long)obj.get("id_room");
         long idUser = (Long)obj.get("id_user");
         long action = (Long)obj.get("action");
         
         // action
         roomsGame.get(sendIdRoom((int)idRoom)).SendEvents((int)idUser, (int)action);
         
         GetPacketActionClient((int)idUser, (int)idRoom);
     }
      
     public void GetPacketActionClient(int idUser, int idRoom) {
         JSONObject packet = new JSONObject();
         packet.put("packet", "action_client");
         List<Plane> plane = roomsGame.get(sendIdRoom(idRoom)).getPlanes();
         JSONArray planePacket = new JSONArray();
         for(int i = 0; i < plane.size(); i++) {
             JSONObject planeOrder = new JSONObject();
             
             JSONObject coordinate = new JSONObject();
             coordinate.put("x", new Integer(plane.get(i).getX()));
             coordinate.put("y", new Integer(plane.get(i).getY()));
             
             JSONObject move = new JSONObject();
             move.put("x", new Integer((int)plane.get(i).getDirectionVector().X()));
             move.put("y", new Integer((int)plane.get(i).getDirectionVector().Y()));
             planeOrder.put("coordinate", coordinate);
             planeOrder.put("move", move);
             planeOrder.put("flag", 1);// спросить Влада
             planeOrder.put("id_plane", new Integer(plane.get(i).getPlayer().getID()));
             planePacket.add(planeOrder);
         }
         packet.put("plane", planePacket);
         String jsonPacket = JSONValue.toJSONString(packet);
         send(idUser, jsonPacket);
     }
     
     
     
     public void exitClient(JSONObject obj) {
         long idUser = (Long)obj.get("id_user");
         GetExitClient((int)idUser);
         Client.get(sendIdPlayer((int)idUser)).getPlayer().getPlane().ExitRoom();
     }
     
     public void GetExitClient(int idUser) {
         JSONObject packet = new JSONObject();
         packet.put("packet", "exit_client");
         String jsonPacket = JSONValue.toJSONString(packet);
         send(idUser, jsonPacket);
     }
     
      
     
     
     public void GetAllRoom(int idUser) {
         JSONObject packet = new JSONObject();
         packet.put("packet", "all_room");
         JSONArray allRoom = new JSONArray();
         for(int i = 0; i < roomsGame.size(); i++) {
             JSONObject room = new JSONObject();
             int idRoom = roomsGame.get(i).getID();
             String nameRoom = roomsGame.get(i).getName();
             int count = roomsGame.get(i).getMaxPlayerCount();
             room.put("id_room", new Integer(idRoom));
             room.put("name", new String(nameRoom));
             room.put("count", new Integer(count));
             room.put("curent", new Integer(roomsGame.get(i).getPlanes().size()));
             allRoom.add(room);
         }
         packet.put("room", allRoom);
         String jsonPacket = JSONValue.toJSONString(packet);
         send(idUser, jsonPacket);
     }
     
     
     
     // Удаляем клинта
     public void removeClient(int client){
        Client.remove(client);
     }
 }
