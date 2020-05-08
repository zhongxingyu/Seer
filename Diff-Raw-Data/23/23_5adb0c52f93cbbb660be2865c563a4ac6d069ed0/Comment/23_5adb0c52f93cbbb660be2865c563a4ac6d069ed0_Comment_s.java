 package com.supinfo.ticketmanager.entity;
 
import org.hibernate.validator.constraints.NotBlank;
 
import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
 
 /**
  * Created by IntelliJ IDEA.
  * User: bargenson
  * Date: 30/08/11
  * Time: 11:41
  */
 @Entity
 public class Comment implements Serializable {
 
     @Id @GeneratedValue
     private Long id;
 
    @Lob
    @NotBlank @Size(min = 5)
     private String content;
 
     @NotNull @Past
     @Temporal(TemporalType.TIMESTAMP)
     private Date createdAt;
 
     @NotNull
     @ManyToOne @JoinColumn
     private Ticket ticket;
 
     @NotNull
     @ManyToOne @JoinColumn
     private User author;
 
 
     public Comment() {
 
     }
 
     public Comment(String content, Ticket ticket, Date createdAt, User author) {
         this.content = content;
         this.ticket = ticket;
         if(createdAt != null) {
         	this.createdAt = new Date(createdAt.getTime());
         }
         this.author = author;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public Date getCreatedAt() {
         return new Date(createdAt.getTime());
     }
 
     public void setCreatedAt(Date createdAt) {
         this.createdAt = new Date(createdAt.getTime());
     }
 
     public Ticket getTicket() {
         return ticket;
     }
 
     public void setTicket(Ticket ticket) {
         this.ticket = ticket;
     }
 
     public User getAuthor() {
         return author;
     }
 
     public void setAuthor(User author) {
         this.author = author;
     }
     
 }
