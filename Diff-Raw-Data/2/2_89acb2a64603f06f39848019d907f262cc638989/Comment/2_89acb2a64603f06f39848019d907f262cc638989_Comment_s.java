 package edu.rit.taskers.model;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 
 
 @Entity
 @Table(name = "Comment")
 public class Comment implements Serializable {
 
     private static final long serialVersionUID = 6895747392066771173L;
 
 
     private int id;
     protected Contact creator;
     protected Date created;
     protected String text;
     protected Integer item;
     
 
     public Comment() {
 
     }
 
     @Id
     @GeneratedValue
     @Column(name = "CommentID")
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
    @OneToOne(fetch = FetchType.LAZY, , cascade = CascadeType.DETACH)
     @JoinColumn(name = "Creator")
     public Contact getCreator() {
         return creator;
     }
 
     public void setCreator(Contact creator) {
         this.creator = creator;
     }
 
     @Column(name = "Created")
     public Date getCreated() {
         return created;
     }
 
     public void setCreated(Date created) {
         this.created = created;
     }
 
     @Column(name = "Text")
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     @Column(name = "ItemID")
     public Integer getItem() {
         return item;
     }
 
     public void setItem(Integer item) {
         this.item = item;
     }
 
     @Override
     public String toString() {
         return "Comment{" +
                 "id=" + id +
                 ", creator=" + creator +
                 ", created=" + created +
                 ", text='" + text + '\'' +
                 ", actionableId=" + item +
                 '}';
     }
 }
 
