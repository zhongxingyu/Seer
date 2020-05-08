 package labo_json;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import com.google.gson.*;
 
 public class User {
     static ArrayList<User> list = new ArrayList<User>();
 
     String name;
     int followers;
     int repos;
     int gist;
     String avatar;
 
     public User(String name, int followers, int repos, int gist, String avatar){
         this.name = name;
         this.gist = gist;
         this.followers = followers;
         this.repos = repos;
         this.avatar = avatar;
         list.add(this);
     }
 
     public int score(){
         return followers + repos + gist;
     }
 
     public static String list(){
         ArrayList<Map> arrayOfSerializedObject = new ArrayList<Map>();
         for(User user : ordonnerListe()){
             arrayOfSerializedObject.add(user.toMap());
         }
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         return gson.toJson(arrayOfSerializedObject);
     }
 
     public static ArrayList<User> ordonnerListe(){
         ArrayList<User> liste = list;
 
         Collections.sort(liste, new Comparator<User>(){
             @Override
             public int compare(User user1, User user2){
                return user2.score() - user1.score();
             }
         });
         return liste;
     }
 
     public String toJson(){
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         return gson.toJson(toMap());
     }
 
     public Map toMap(){
         Map hash = new HashMap<String,String>();
         hash.put("name", name);
         hash.put("followers", followers);
         hash.put("repos", repos);
         hash.put("gist", gist);
         hash.put("avatar", avatar);
         hash.put("score", score());
         return hash;
     }
 
 }
