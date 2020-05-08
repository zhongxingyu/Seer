 package com.mrfeelings.db.entities;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
 
 
 import edu.upenn.bbl.common.jpa.Identifiable;
 
 @Entity
 @Table(name="GUESTBOOK")
 @NamedQueries(value = {
     @NamedQuery(
         name = GuestbookEntry.GET_ALL_ENTRIES,
         query = "select e from GuestbookEntry e order by e.date desc")
 })
 public class GuestbookEntry implements Identifiable {
 
   public static final String GET_ALL_ENTRIES = "GetAllEntries";
   
   private Long _id;
   private User _user;
   private Date _date;
   private String _subject;
   private String _message;
   
   @Id
   @GeneratedValue
   @Column(name="GB_ID")
   @Override
   public Long getId() {
     return _id;
   }
   @Override
   public void setId(Long id) {
     _id = id;
   }
   
   @ManyToOne
   @JoinColumn(name="USER_ID")
   public User getUser() {
     return _user;
   }
   public void setUser(User user) {
     _user = user;
   }
   
   @Column(name="GB_DATE")
   @Temporal(TemporalType.TIMESTAMP)
   public Date getDate() {
     return _date;
   }
   public void setDate(Date date) {
     _date = date;
   }
   
   @Column(name="SUBJECT")
   public String getSubject() {
     return _subject;
   }
   public void setSubject(String subject) {
     _subject = subject;
   }
   
   @Column(name="MESSAGE")
   @Lob
  @Type(type="org.hibernate.type.StringClobType")
   public String getMessage() {
     return _message;
   }
   public void setMessage(String message) {
     _message = message;
   }
   
   @Transient
   public String getMessageHtml() {
     return _message.replace("\n", "<br/>");
   }
 
   @Override
   public String toString() {
     return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
 }
