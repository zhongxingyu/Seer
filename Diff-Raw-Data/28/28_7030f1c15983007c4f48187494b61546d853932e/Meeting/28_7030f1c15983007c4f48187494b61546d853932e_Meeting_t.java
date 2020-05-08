 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVACAO, SA (PTIN), IBM Corp.,
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package si.setcce.societies.crowdtasking.model;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Ref;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import com.googlecode.objectify.annotation.Load;
 import si.setcce.societies.crowdtasking.api.RESTful.impl.UsersAPI;
 import si.setcce.societies.crowdtasking.model.dao.CollaborativeSpaceDAO;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Describe your class here...
  *
  * @author Simon Jureša
  */
 @Entity
 public class Meeting {
     @Id
     private Long id;
     private String subject;
     private String description;
     @Index
     @Load
     private Ref<CollaborativeSpace> csRef;
     @Index
     private Date startTime;
     private Date endTime, created;
     private Set<Ref<CTUser>> users;
     @Load
     private Ref<CTUser> organizerRef;
     String downloadUrl;
 
     public Meeting() {
     }
 
     public Meeting(String subject, String description,
                    Long csId, Date startTime, Date endTime, CTUser organizer, Set<Long> userIds) {
         this.subject = subject;
         this.description = description;
         if (csId != null) {
             this.csRef = Ref.create(Key.create(CollaborativeSpace.class, csId));
         }
         this.organizerRef = Ref.create(Key.create(CTUser.class, organizer.getId()));
         this.startTime = startTime;
         this.endTime = endTime;
         this.created = new Date();
         this.users = new HashSet<>();
         for (Long userId : userIds) {
             users.add(Ref.create(Key.create(CTUser.class, userId)));
         }
     }
 
     public String getSubject() {
         return subject;
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Ref<CollaborativeSpace> getCsRef() {
         return csRef;
     }
 
     public void setCsRef(Ref<CollaborativeSpace> csRef) {
         this.csRef = csRef;
     }
 
     public CollaborativeSpace getCollaborativeSpace() {
         if (csRef == null) {
             return null;
         }
 
         CollaborativeSpace cs;
         try {
             cs = csRef.get();
             if (cs == null) {
                 cs = CollaborativeSpaceDAO.load(csRef);
             }
         } catch (Exception e) {
             System.out.println("Error in getCollaborativeSpace() in Meeting class: " + e.getMessage());
             cs = CollaborativeSpaceDAO.load(csRef);
         }
         return cs;
     }
 
     public Date getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Date startTime) {
         this.startTime = startTime;
     }
 
     public Date getEndTime() {
         return endTime;
     }
 
     public void setEndTime(Date endTime) {
         this.endTime = endTime;
     }
 
     public Set<Ref<CTUser>> getUsers() {
         return users;
     }
 
     public void setUsers(Set<Ref<CTUser>> users) {
         this.users = users;
     }
 
     public Long getId() {
         return id;
     }
 
     public Date getCreated() {
         return created;
     }
 
     public void setCreated(Date created) {
         this.created = created;
     }
 
     public CTUser getOrganizer() {
         if (organizerRef == null) {
             return null;
         }
 
        CTUser user;
        try {
            user = organizerRef.get();
        } catch (Exception e) {
            System.out.println("Error in getUser() in Meeting class: " + e.getMessage());
            user = UsersAPI.getUser(organizerRef);
            if (user != null) {
                System.out.println("Got user from UsersAPI instead");
            } else {
                System.out.println("Error! Didn't get user (organizer).");
             }
         }
         return user;
     }
 
     public String getDownloadUrl() {
         return downloadUrl;
     }
 
     public void setDownloadUrl(String downloadUrl) {
         this.downloadUrl = downloadUrl;
     }
 }
