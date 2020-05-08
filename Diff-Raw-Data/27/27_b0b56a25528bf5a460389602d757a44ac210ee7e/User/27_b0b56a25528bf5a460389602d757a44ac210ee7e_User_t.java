 package models;
 
 import play.db.jpa.Model;
 import java.util.*;
 import javax.persistence.*;
 
 @Entity
 public class User extends Model {
     
     public String userName;
     public String password;
     public String email;
     
     @OneToMany(mappedBy="deskUser",cascade=CascadeType.ALL)
     public List<Desk> desks;
     
     public User(String email,String password,String userName){
         this.desks = new ArrayList<Desk>();
         this.userName = userName;
         this.email = email;
         this.password = password;
     }
     
     public static User validateUser(String userNameOrEmail,String password){
         User user = find("byUserNameAndPassword",userNameOrEmail,password).first();
         if(user!= null){
             return user;
         }else{
             return find("byEmailAndPassword",userNameOrEmail,password).first();
         }
     }
     public void persistNewUser(){
         this.save();
         this.addUserDesk(null, this, null);
     }
     
     public void addUserDesk(String deskName,User deskUser,String deskDesc){
         Desk newDesk = new Desk(deskName,deskUser,deskDesc).save();
         this.desks.add(newDesk);
         this.save();
     }
     
    public static User getUser(String userName){
        return find("byUserName",userName).first();
    }
    
 }
