 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop.core;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 /**
  *
  * @author Group4
  */
 @Entity
 @Table(name="USERROLES")
 public class UserRoles implements Serializable {
     
     @Id
     @OneToOne
    @JoinColumn(name="USERNAME")
     private Users username; 
     
     @OneToOne
    @JoinColumn(name="ROLE")
     private Roles role;
     
     public UserRoles(){}
     
     public UserRoles(Users user, Roles role){
         this.username = user;
         this.role = role;
     }
     
     public Users getUsername(){
         return this.username;
     }
 
     public Roles getRole(){
         return this.role;
     }
 }
