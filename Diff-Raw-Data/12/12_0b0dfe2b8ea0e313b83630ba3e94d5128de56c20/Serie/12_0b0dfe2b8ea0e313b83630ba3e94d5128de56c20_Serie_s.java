 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.mycompany.biblio.model;
 
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.NamedQuery;
 
 /**
  *
  * @author root
  */
 
 @Entity
@NamedQuery(name = Serie.FIND_ALL, query = "SELECT * FROM Serie")
 
 public class Serie {
     public final static String FIND_ALL = "Serie.findAll";
 
     @Id
     @GeneratedValue
     private long id;
     @Column(nullable = false)
     private String title;
     @Column(length = 2000)
     private String description;
     private String img;
     private float note;
     private Boolean selected;
 
     
     public Serie() {
     }
 
     public Serie(String title, String description,String img, float note, Boolean selected) {
         this.title = title;
         this.description = description;
         this.img = img;
         this.note = note;
     }
 
     /**
      * @return the id
      */
     public long getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(long id) {
         this.id = id;
     }
 
     /**
      * @return the title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @param title the title to set
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * @return the description
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * @param description the description to set
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * @return the img
      */
     public String getImg() {
         return img;
     }
 
     /**
      * @param img the img to set
      */
     public void setImg(String img) {
         this.img = img;
     }
 
     /**
      * @return the note
      */
     public float getNote() {
         return note;
     }
 
     /**
      * @param note the note to set
      */
     public void setNote(float note) {
         this.note = note;
     }
 
     public Boolean isSelected() {
         return selected;
     }
 
     public Boolean getSelected() {
         return selected;
     }
 
     public void setSelected(Boolean selected) {
         this.selected = selected;
     }
 }
