 package it.flaten.mjolnir.beans;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import java.io.Serializable;
 
 @Entity
 @Table(name = "Event")
 public class Event implements Serializable {
     public enum EventType {
         UNBAN(0), BAN(1);
 
         private int id;
 
         private EventType(int id) {
             this.id = id;
         }
 
         public Integer getId() {
             return this.id;
         }
     }
 
    @Id     private int       id;
    @Column private int       time;
    @Column private String    player;
    @Column private String    op;
    @Column private EventType type;
    @Column private String    reason;
    @Column private int       expires;
 
     public void setId(int id) {
         this.id = id;
     }
     public int getId() {
         return this.id;
     }
 
     public void setTime(int time) {
         this.time = time;
     }
     public int getTime() {
         return this.time;
     }
 
     public void setPlayer(String player) {
         this.player = player;
     }
     public String getPlayer() {
         return this.player;
     }
 
     public void setOp(String op) {
         this.op = op;
     }
     public String getOp() {
         return this.op;
     }
 
     public void setType(EventType type) {
         this.type = type;
     }
     public EventType getType() {
         return this.type;
     }
 
     public void setReason(String reason) {
         this.reason = reason;
     }
     public String getReason() {
         return this.reason;
     }
 
     public void setExpires(int expires) {
         this.expires = expires;
     }
     public int getExpires() {
         return this.expires;
     }
 }
