 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.marbl.autosysteem;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author Leslie Aerts
  */
 @Entity(name = "simsession")
 @XmlRootElement(name = "collection")
 public class Session implements Serializable
 {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
     private int authCode = 0;
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date sessiondate;
     
    @OneToMany(cascade= CascadeType.MERGE,mappedBy="parentSession")
     private Collection<TimeStep> timesteps;
 
     public Session()
     {
         this.timesteps = new ArrayList<>();
     }
 
     public Session(Date date, ArrayList<TimeStep> timesteps)
     {
         this.timesteps = timesteps;
     }
 
     public long getId()
     {
         return id;
     }
 
     public void setId(long id)
     {
         this.id = id;
     }
 
     @XmlAttribute(name = "date")
     public Date getSessionDate()
     {
         return sessiondate;
     }
 
     public void setSessionDate(Date date)
     {
         this.sessiondate = date;
     }
 
     public Collection<TimeStep> getTimesteps()
     {
         return timesteps;
     }
 
     public void setTimesteps(Collection<TimeStep> timesteps)
     {
         this.timesteps = timesteps;
     }
 
     public int getAuthCode()
     {
         return authCode;
     }
 
     public void setAuthCode(int auth)
     {
         this.authCode = auth;
     }
 }
