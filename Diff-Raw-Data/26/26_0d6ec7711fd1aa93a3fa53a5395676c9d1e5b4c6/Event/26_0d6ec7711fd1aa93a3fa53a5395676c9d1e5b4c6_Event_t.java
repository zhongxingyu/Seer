 /*
     Copyright (c) 2012-2015 Yoga Vidya Pranic Healing Foundation of Karnataka.
     All rights reserved. Patents pending.
 */
 
 package com.yvphk.model.form;
 
 import com.yvphk.common.SeatingType;
import com.yvphk.common.Util;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import java.util.Date;
 import java.util.Set;
 
 @Entity
 @Table(name = "PHK_EVENT")
 @org.hibernate.annotations.Entity(dynamicUpdate = true)
 public class Event extends BaseForm
 {
     public static final String ClassName = "com.yvphk.model.form.Event";
 
     @Id
     @Column(name = "ID")
     @GeneratedValue
     private Integer id;
 
     @Column(name = "NAME")
     private String name;
 
     @Column(name = "VENUE")
     private String venue;
 
     @Column(name = "CITY")
     private String city;
 
     @Column(name = "STATE")
     private String state;
 
     @Column(name = "ELIGIBILITYLEVEL")
     private String eligibilityLevel;
 
     @Column(name = "STARTDATE")
     private Date startDate;
 
     @Column(name = "ENDDATE")
     private Date endDate;
 
     @Column(name = "isseatperlvl")
     private boolean seatPerLevel;
 
     @Column(name = "SEATALLOCATED")
     private boolean seatAllocated;
 
     @Column(name = "SEATINGTYPE")
     private String seatingType;
 
     @Column(name = "ROWMETANAME")
     private String rowMetaName;
 
     @Column(name = "PREPAREDBY", updatable=false)
     private String preparedBy;
 
     @Column(name = "TIMECREATED", updatable=false)
     private Date timeCreated;
 
     @Column(name = "TIMEUPDATED")
     private Date timeUpdated;
 
     @Column(name = "ACTIVE")
     private boolean active;
 
     @OneToMany(mappedBy = "event", fetch = FetchType.EAGER)
     private Set<EventFee> fees;
 
     public Integer getId ()
     {
         return id;
     }
 
     @Override
     public String getType ()
     {
         return ClassName;
     }
 
     public void setId (Integer id)
     {
         this.id = id;
     }
 
     public String getName ()
     {
         return name;
     }
 
     public void setName (String name)
     {
         this.name = name;
     }
 
     public String getVenue ()
     {
         return venue;
     }
 
     public void setVenue (String venue)
     {
         this.venue = venue;
     }
 
     public String getCity ()
     {
         return city;
     }
 
     public void setCity (String city)
     {
         this.city = city;
     }
 
     public String getState ()
     {
         return state;
     }
 
     public void setState (String state)
     {
         this.state = state;
     }
 
     public String getEligibilityLevel ()
     {
         return eligibilityLevel;
     }
 
     public void setEligibilityLevel (String eligibilityLevel)
     {
         this.eligibilityLevel = eligibilityLevel;
     }
 
     public Date getStartDate ()
     {
         return startDate;
     }
 
     public void setStartDate (Date startDate)
     {
         this.startDate = startDate;
     }
 
     public Date getEndDate ()
     {
         return endDate;
     }
 
     public void setEndDate (Date endDate)
     {
         this.endDate = endDate;
     }
 
     public boolean isSeatPerLevel ()
     {
         return seatPerLevel;
     }
 
     public void setSeatPerLevel (boolean seatPerLevel)
     {
         this.seatPerLevel = seatPerLevel;
     }
 
     public String getPreparedBy ()
     {
         return preparedBy;
     }
 
     public void setPreparedBy (String preparedBy)
     {
         this.preparedBy = preparedBy;
     }
 
     public Date getTimeCreated ()
     {
         return timeCreated;
     }
 
     public void setTimeCreated (Date timeCreated)
     {
         this.timeCreated = timeCreated;
     }
 
     public Date getTimeUpdated ()
     {
         return timeUpdated;
     }
 
     public void setTimeUpdated (Date timeUpdated)
     {
         this.timeUpdated = timeUpdated;
     }
 
     public boolean isActive ()
     {
         return active;
     }
 
     public void setActive (boolean active)
     {
         this.active = active;
     }
 
     public Set<EventFee> getFees ()
     {
         return fees;
     }
 
     public void setFees (Set<EventFee> fees)
     {
         this.fees = fees;
     }
 
     public boolean isSeatAllocated ()
     {
         return seatAllocated;
     }
 
     public void setSeatAllocated (boolean seatAllocated)
     {
         this.seatAllocated = seatAllocated;
     }
 
     public String getSeatingType ()
     {
         return seatingType;
     }
 
     public String getSeatingTypeName ()
     {
        if (!Util.nullOrEmptyOrBlank(getSeatingType())) {
            return SeatingType.getName(getSeatingType());
        }
        else {
            return "";
        }
     }
 
     public void setSeatingType (String seatingType)
     {
         this.seatingType = seatingType;
     }
 
     public String getRowMetaName ()
     {
         return rowMetaName;
     }
 
     public void setRowMetaName (String rowMetaName)
     {
         this.rowMetaName = rowMetaName;
     }
 }
