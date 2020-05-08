 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import java.util.HashMap;
 
 /**
  *
  * @author lucas
  */
 public class SourceUser implements SourceUserInt {
 
     private HashMap<Integer, User> data = new HashMap<Integer, User>();
     private HashMap<Integer, TeamStatus> teamsStatus = null;
     private HashMap<Integer, ItemStatus> itemsStatus = null;
     private int[] idUsers = null;
 
     public SourceUser() {
     }
 
     private void saveData(String response, int id) {
         String[] split = response.split(";");
         User user = new User(id, Integer.valueOf(split[1]), Integer.valueOf(split[0]), split[2], split[3], split[4], split[5],
                 split[6], split[7], split[8]);
         data.put(id, user);
     }
 
     @Override
     public String loadData() {
         ServerConnection sc = ServerConnection.getInstance();
         String response = sc.sendMSG("GET_USERS");
         if (response.startsWith("KO")) {
             return response;
         }
         String[] users = response.split(";");
         idUsers = new int[users.length];
         for (int i = 0; i < users.length; i++) {
             idUsers[i] = Integer.valueOf(users[i]);
         }
 
         for (int id : idUsers) {
             response = sc.sendMSG("GET_INFO " + id);
             if (response.startsWith("KO")) {
                 return response;
             }
             saveData(response, id);
             itemsStatus = new HashMap<Integer, ItemStatus>();
             response = sc.sendMSG("GET_USER_ITEMS " + id);
             if (response.startsWith("KO")) {
                 return response;
             }
             String[] split = response.split(";");
             for (int i = 0; i < split.length; i++) {
                 String[] s = split[i].split(" ");
                 itemsStatus.put(Integer.valueOf(s[0]), new ItemStatus(s[1]));
             }
             teamsStatus = new HashMap<Integer, TeamStatus>();
             response = sc.sendMSG("GET_USER_TEAMS " + id);
             if (response.startsWith("KO")) {
                 return response;
             }
             split = response.split(";");
             for (int i = 0; i < split.length; i++) {
                 String[] s = split[i].split(" ");
                 teamsStatus.put(Integer.valueOf(s[0]), new TeamStatus(s[1]));
             }
             User get = data.get(id);
             get.setItems(itemsStatus);
             get.setTeams(teamsStatus);
             data.put(id, get);
         }
         return "OK";
     }
 
     @Override
     public String setTeam(int idUser, int idTeam) {
         ServerConnection sc = ServerConnection.getInstance();
         String response = sc.sendMSG("USER_IN_TEAM " + idUser + " " + idTeam);
         if (response.startsWith("KO")) {
             return response;
         }
         return "OK";
     }
 
     @Override
     public String delTeam(int idUser, int idTeam) {
         ServerConnection sc = ServerConnection.getInstance();
         String response = sc.sendMSG("USER_OUT_TEAM " + idUser + " " + idTeam);
         if (response.startsWith("KO")) {
             return response;
         }
         return "OK";
     }
 
     @Override
     public String setTeamConfirmed(int idUser, int idTeam, boolean confirmed) {
         ServerConnection sc = ServerConnection.getInstance();
         String response = sc.sendMSG("SET_TEAM_CONFIRMED " + idUser + " " + idTeam + " " + confirmed);
         if (response.startsWith("KO")) {
             return response;
         }
         return "OK";
     }
 
     @Override
     public String setItemState(int idUser, int idItem, boolean state) {
         ServerConnection sc = ServerConnection.getInstance();
         String response = sc.sendMSG("SET_ITEM_STATE " + idUser + " " + idItem + " " + state);
         if (response.startsWith("KO")) {
             return response;
         }
         return "OK";
     }
 
     @Override
     public String addUser(User user) {
         ServerConnection sc = ServerConnection.getInstance();
         String s = "ADD_USER ";
         s += user.getRole() + " ";
         s += user.getGroup() + " ";
         s += user.getPass();
         String response = sc.sendMSG(s);
         if (response.startsWith("KO")) {
             return response;
         }
        return "OK";
     }
 
     @Override
     public String delUser(int idUser) {
         ServerConnection sc = ServerConnection.getInstance();
         String response = sc.sendMSG("DEL_USER " + idUser);
         if (response.startsWith("KO")) {
             return response;
         }
         return "OK";
     }
 
     @Override
     public String updateUser(int idUser, User user) {
         ServerConnection sc = ServerConnection.getInstance();
         String s = "UPDATE_USER ";
         s += idUser + " ";
         s += user.getName() + " ";
         s += user.getLastName() + " ";
         s += user.getAddress() + " ";
         s += user.getCity() + " ";
         s += user.getEmail() + " ";
         s += user.getPhone() + " ";
         s += user.getProfession();
         String response = sc.sendMSG(s);
         if (response.startsWith("KO")) {
             return response;
         }
         return "OK";
     }
 
     @Override
     public User getUser(int id) {
         return data.get(id);
     }
 }
