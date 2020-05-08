 package ru.terra.server.db.entity;
 
 import javax.persistence.*;
 
 /**
  * Date: 17.12.13
  * Time: 16:12
  */
 public class AbstractUser {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id", nullable = false)
     protected Integer id;
 
     @Basic(optional = false)
     @Column(name = "level", nullable = false)
     protected int level;
     @Basic(optional = false)
     @Column(name = "name", nullable = false, length = 512)
    protected String name;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public int getLevel() {
         return level;
     }
 
     public void setLevel(int level) {
         this.level = level;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public AbstractUser(Integer id, int level, String name) {
         this.id = id;
         this.level = level;
         this.name = name;
     }
 }
