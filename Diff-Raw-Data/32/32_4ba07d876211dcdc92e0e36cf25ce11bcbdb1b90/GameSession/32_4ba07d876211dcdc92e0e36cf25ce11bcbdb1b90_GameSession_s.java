 package run;
 
 import Graph.*;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NavigableMap;
 import java.util.TreeMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: User
  * Date: 22.08.13
  * Time: 13:30
  * To change this template use File | Settings | File Templates.
  */
 public class GameSession {
     private static GameSession ourInstance = new GameSession();
     private NavigableMap<Integer, DrawMatche> matches = new TreeMap<Integer, DrawMatche>();
     private Map<Integer, User> users = new HashMap<Integer, User>();
     private User currentUser;
     private ShowTablo tablo;
     private ChangeUserButton button;
     private HelpMessage helpMessage;
 
     public static GameSession getInstance() {
         return ourInstance;
     }
 
     private GameSession() {
     }
 
     public void  addHelpMessage(HelpMessage message){
         this.helpMessage = message;
     }
 
     public HelpMessage getHelpMessage(){
         return this.helpMessage;
     }
 
     public ShowTablo getShowTablo(){
         return this.tablo;
     }
 
     public void addTablo(ShowTablo tablo){
         this.tablo = tablo;
     }
 
     public void addButton(ChangeUserButton button){
         this.button = button;
     }
 
     public ChangeUserButton getButton(){
         return this.button;
     }
 
     public void resetSession(String strategy){
         this.clearUsers();
         this.matches.clear();
         //this.tablo.resetTablo(strategy);
     }
 
     public void setUsers(String strategy) throws Exception {
        if(strategy.equals("userToUser")){
            users.put(1, new User(1, "human", "white"));
            users.put(2, new User(2, "human", "black"));
        } else if(strategy.equals("userToRobot")){
            users.put(1, new User(1, "human", "white"));
            users.put(2, new User(2, "robot", "black"));
        } else if(strategy.equals("robotToRobot")){
            users.put(1, new User(1, "robot", "white"));
            users.put(2, new User(2, "robot", "black"));
        }
        setCurrentUser(1);
     }
 
     public Map<Integer, User> getUsers(){
         return this.users;
     }
 
     public User getUser(Integer id){
         return this.getUsers().get(id);
     }
 
     public void clearUsers(){
         users.clear();
     }
 
     public void setCurrentUser(Integer id) throws Exception {
         if(id > this.users.size() || id < 1){
             throw new Exception("user id" + id.toString() + "not exist in system");
         }
         this.currentUser = users.get(id);
         this.currentUser.getCounter().setActive();
     }
 
     public void changeCurrentUser() throws Exception {
         if(users.isEmpty()){
             throw new Exception("user collection is empty");
         }
         Integer lastId = currentUser.getId();
         this.currentUser.resetCount(this.getAviableMatches());
         this.currentUser.getCounter().setCount(this.currentUser.type, this.currentUser.getCount());
         this.currentUser.getCounter().setInactive();
         if(lastId == users.size()){
             this.setCurrentUser(1);
         } else{
             this.setCurrentUser(lastId + 1);
         }
         this.currentUser.resetCount(this.getAviableMatches());
         this.currentUser.getCounter().setCount(this.currentUser.type, this.currentUser.getCount());
         this.currentUser.getCounter().setActive();
     }
 
     public User getNextUser() throws Exception {
         if(users.isEmpty()){
             throw new Exception("user collection is empty");
         }
         Integer lastId = currentUser.getId();
         if(lastId == users.size()){
             return getUser(1);
         } else{
             return getUser(lastId + 1);
         }
     }
 
     public User getCurrentUser(){
         return this.currentUser;
     }
 
     public void addMatche(Integer i, DrawMatche matche){
         this.matches.put(i, matche);
     }
 
     public DrawMatche getMatche(Integer i){
         return this.matches.get(i);
     }
 
     public DrawMatche getLastMatche(){
         return this.matches.lastEntry().getValue();
     }
 
     public Integer getAviableMatches(){
         return this.matches.size();
     }
 
     public void remooveMatche(Integer i){
         if(this.matches.containsKey(i)){
             this.matches.remove(i);
         }
     }
 
     public class User{
         private Integer id;
         private String color;
         private Integer count = 10;
         private Integer totalCount;
         private String type;
         private boolean course = false;
         private CountDisplay counter;
 
         public User(Integer id, String type, String color){
             this.type = type;
             this.id = id;
             this.color = color;
             this.counter = new CountDisplay(color, type);
         }
 
         public CountDisplay getCounter(){
             return this.counter;
         }
 
         public String getColor(){
             return this.color;
         }
 
         public Integer getId(){
             return this.id;
         }
 
         public String getType(){
             return this.type;
         }
 
         public Integer getCount(){
             return this.count;
         }
 
         public void updateCount(){
             this.count--;
         }
 
         public void resetCount(Integer aviable){
             if(aviable < 10){
                 this.count = aviable;
             } else {
                 this.count = 10;
             }
         }
 
         public void setCourse(){
             this.course = true;
         }
 
         public void unsetCourse(){
             this.course = false;
         }
 
         public boolean isCourse(){
             return this.course;
         }
 
         public void updateTotalCount(Integer count){
             this.totalCount = this.totalCount + count;
         }
 
         public Integer getTotalCount(){
             return this.totalCount;
         }
     }
 }
