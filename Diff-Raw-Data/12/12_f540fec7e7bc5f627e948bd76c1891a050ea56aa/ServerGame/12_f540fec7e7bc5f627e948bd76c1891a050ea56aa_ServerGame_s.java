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
     
     
     public ServerGame(){
         Client = new LinkedList<ClientSocket>();
         roomsGame = new LinkedList<Room>();
     }
     
     // Комнаты с игроками
     
     
     //Создаем комнату и возвращаем клиенту id-room 
     // если комнат нет или клиент хочет создать свою
     public void CreateRoom(JSONObject obj){
         String name = (String)obj.get("name");
         long count = (Long)obj.get("count");
         long idUser = (Long)obj.get("id_user");
         
         Room room = new Room((int)count, name);
         roomsGame.add(room);
         int index = roomsGame.indexOf(room);
         roomsGame.get(index).setID(index);
         
         addPlayer(index, (int)idUser);
         roomsGame.get(index).StartRoom();
         GetPacketRoom((int)idUser, index);
     }
     
     public void GetPacketRoom(int idPlane, int room){
         
         JSONObject coordinate = new JSONObject();
         coordinate.put("x", new Integer(150));
         coordinate.put("y", new Integer(140));
         
         JSONObject move = new JSONObject();
         move.put("x", new Integer(160));
         move.put("y", new Integer(200));
         
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
     
     
     public void send(int idUser, String packet) {
         Client.get(idUser).getOut().println(packet + "\n");
     }
         
     public void ConnectRoom(JSONObject obj) {
         long idRoom = (Long)obj.get("id_room");
         long idUser = (Long)obj.get("id_user");
         
         addPlayer((int)idRoom, (int)idUser);
         
         GetPacketRoom((int)idRoom, (int)idUser);
     }
     
     public void addPlayer(int idRoom, int idUser) {
         Player player = new Player(idUser, Client.get(idUser).getName());
         roomsGame.get(idRoom).AddPlayer(player);
         int idInRoom = roomsGame.get(idRoom).getPlayerList().indexOf(player);
         Client.get(idUser).setIdInRoom(idInRoom);
         Client.get(idUser).setIdRoom(idRoom);
     }    
     
     public void ActionServer(JSONObject obj) {
         long idRoom = (Long)obj.get("id_room");
         long idUser = (Long)obj.get("id_user");
         long action = (Long)obj.get("action");
         
         // action
         
         GetPacketActionClient((int)idUser, (int)idRoom);
     }
     
     
     public void GetPacketActionClient(int idUser, int idRoom) {
         JSONObject packet = new JSONObject();
         packet.put("packet", "action_client");
         List<FlyingObject> plane = roomsGame.get(idRoom).getPlayerList();
         JSONArray planePacket = new JSONArray();
         for(int i = 0; i < plane.size(); i++) {
             JSONObject planeOrder = new JSONObject();
             
             JSONObject coordinate = new JSONObject();
             coordinate.put("x", new Integer(plane.get(i).getX()));
             coordinate.put("y", new Integer(plane.get(i).getY()));
             
             JSONObject move = new JSONObject();
            move.put("x", new Integer(plane.get(i).getDirectionVector().XDirection()));
            move.put("y", new Integer(plane.get(i).getDirectionVector().YDirection()));
             planeOrder.put("coordinate", coordinate);
             planeOrder.put("move", move);
             planeOrder.put("flag", 1);
             planeOrder.put("id_plane", 0);
             planePacket.add(planeOrder);
         }
         packet.put("plane", planePacket);
         String jsonPacket = JSONValue.toJSONString(packet);
         send(idUser, jsonPacket);
     }
     
     // Анализатор комманд от клиента
     public void Command(String str, int idUser){
         JSONParser parser = new JSONParser();
         try{
             Object obj = parser.parse(str);
             JSONObject jsonObject = (JSONObject) obj;
             String packet = (String)jsonObject.get("packet");
             System.out.println(packet);
             if(packet != null){
                switch (packet.toLowerCase()){
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
     
     public void exitClient(JSONObject obj) {
         long idUser = (Long)obj.get("id_user");
         GetExitClient((int)idUser);
         roomsGame.get(Client.get((int)idUser).
                 getIdRoom()).getPlayerList().set(Client.get((int)idUser).getIdInRoom(), null);
     }
     
     public void GetExitClient(int idUser) {
         JSONObject packet = new JSONObject();
         packet.put("packet", "exit_client");
         String jsonPacket = JSONValue.toJSONString(packet);
         send(idUser, jsonPacket);
     }
     
     // Добавляем клинтов
     public int addUser(ClientSocket client){
         int result = -1;
         if(!Client.equals(client)){
             if (Client.add(client)){
                 result = Client.indexOf(client);
                 Client.get(result).setId(result);
             }
         }
         return result;
     }
     
     public void ConnectUser(JSONObject obj, int idUser) {
         String name = (String)obj.get("name");
         Client.get(idUser).setName(name);
         GetConnectUser(idUser);
         //GetAllRoom(idUser);
     }
     
     public void GetAllRoom(int idUser) {
         JSONObject packet = new JSONObject();
         packet.put("packet", "all_room");
         JSONArray allRoom = new JSONArray();
         for(int i = 0; i < roomsGame.size(); i++) {
             JSONObject room = new JSONObject();
             int idRoom = roomsGame.get(i).getID();
            String nameRoom = roomsGame.get(i).getM_name();
            int count = roomsGame.get(i).getM_max_player_count();
             room.put("id_room", new Integer(idRoom));
             room.put("name", new String(nameRoom));
             room.put("count", new Integer(count));
             room.put("curent", new Integer(0));
             allRoom.add(room);
         }
         packet.put("room", allRoom);
         String jsonPacket = JSONValue.toJSONString(packet);
         send(idUser, jsonPacket);
     }
     
     public void GetConnectUser(int idUser) {
         JSONObject packet = new JSONObject();
         packet.put("packet", "connect_user");
         packet.put("id_user", new Integer(idUser));
         
         String jsonPacket = JSONValue.toJSONString(packet);
         send(idUser, jsonPacket);
     }
     
     // Удаляем клинта
     public void removeClient(int client){
         Client.remove(client);
     }
 }
