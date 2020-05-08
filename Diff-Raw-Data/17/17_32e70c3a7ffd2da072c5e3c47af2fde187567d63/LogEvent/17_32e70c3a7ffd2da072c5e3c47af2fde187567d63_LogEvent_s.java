 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.event;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import javax.persistence.*;
 
 import nl.surfnet.bod.domain.Loggable;
 import nl.surfnet.bod.domain.PersistableDomain;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.util.TimeStampBridge;
 import nl.surfnet.bod.web.WebUtils;
 
 import org.hibernate.annotations.Type;
 import org.hibernate.search.annotations.Analyzer;
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.FieldBridge;
 import org.hibernate.search.annotations.Indexed;
 import org.hibernate.search.bridge.builtin.impl.BuiltinIterableBridge;
 import org.joda.time.DateTime;
 import org.springframework.util.StringUtils;
 
 import com.google.common.base.Optional;
 
 @Entity
 @Indexed
 @Analyzer(definition = "customanalyzer")
 public class LogEvent implements PersistableDomain {
 
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long id;
 
   @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
   @Field
   @FieldBridge(impl = TimeStampBridge.class)
   private final DateTime createdAt;
 
   @Field
   @Basic
   private final String userId;
 
   @Field
   @FieldBridge(impl = BuiltinIterableBridge.class)
   @ElementCollection
   @Column(name = "ADMIN_GROUP")
   private final Collection<String> adminGroups;
 
   @Field
   @Enumerated(EnumType.STRING)
   private final LogEventType eventType;
 
   @Field
   private final String domainObjectClass;
 
   @Field
   private final String description;
 
   @Field
   @Type(type = "text")
   private final String serializedObject;
 
   @Field
   @Type(type = "text")
   private final String details;
 
   @Field
   @Basic
   private String correlationId;
 
   @Field
   @Basic
   private final Long domainObjectId;
 
   @Field
   @Enumerated(EnumType.STRING)
   private final ReservationStatus oldReservationStatus;
 
   @Field
   @Enumerated(EnumType.STRING)
   private final ReservationStatus newReservationStatus;
 
   /**
    * Default constructor for Hibernate
    */
   @SuppressWarnings("unused")
   private LogEvent() {
     this((String) null, Collections.<String> emptyList(), (LogEventType) null, null);
   }
 
   public LogEvent(String userId, Collection<String> adminGroups, LogEventType type, Loggable domainObject) {
     this(userId, adminGroups, type, Optional.fromNullable(domainObject), "", Optional.<ReservationStatus> absent(),
         Optional.<ReservationStatus> absent());
   }
 
   public LogEvent(String userId, Collection<String> adminGroups, LogEventType type, Optional<Loggable> domainObject,
       String details, Optional<ReservationStatus> oldStatus, Optional<ReservationStatus> newStatus) {
     super();
     this.userId = userId;
     this.adminGroups = adminGroups;
     this.eventType = type;
     this.details = details;
 
     this.createdAt = DateTime.now();
 
     if (domainObject.isPresent()) {
       this.domainObjectId = domainObject.get().getId();
       this.domainObjectClass = getDomainObjectName(domainObject.get().getClass());
       this.description = domainObject.get().getLabel();
       this.serializedObject = serializeObject(domainObject.get());
     }
     else {
       this.domainObjectId = null;
       this.description = "";
       this.domainObjectClass = null;
       this.serializedObject = null;
     }
 
     oldReservationStatus = oldStatus.orNull();
     newReservationStatus = newStatus.orNull();
   }
 
   public static String getDomainObjectName(Class<? extends Loggable> domainClass) {
     return domainClass.getSimpleName();
   }
 
   public DateTime getCreatedAt() {
     return createdAt;
   }
 
   public Collection<String> getAdminGroups() {
     return adminGroups;
   }
 
   public String getUserId() {
     return userId;
   }
 
   public String getDescription() {
     return description;
   }
 
   public String getDomainObjectClass() {
     return domainObjectClass;
   }
 
   public Long getDomainObjectId() {
     return domainObjectId;
   }
 
   public String getSerializedObject() {
     return serializedObject;
   }
 
   public String getDetails() {
     return details;
   }
 
   public LogEventType getEventType() {
     return eventType;
   }
 
   /**
    * Used to relate logEvents which originates out of a List
    *
    * @return String CorreleationId
    */
   public String getCorrelationId() {
     return correlationId;
   }
 
   @Override
   public Long getId() {
     return id;
   }
 
   /**
    * Used to relate logEvents which originates out of a List
    *
    * @param correlationId
    */
   public void setCorrelationId(String correlationId) {
     this.correlationId = correlationId;
   }
 
   public ReservationStatus getOldReservationStatus() {
     return oldReservationStatus;
   }
 
   public ReservationStatus getNewReservationStatus() {
     return newReservationStatus;
   }
 
   public String getEventTypeWithCorrelationId() {
     return StringUtils.hasText(correlationId) ? StringUtils.capitalize(eventType.name().toLowerCase()).concat(" ")
         .concat(correlationId) : StringUtils.capitalize(eventType.name().toLowerCase());
   }
 
   @Override
   public String toString() {
     StringBuilder builder = new StringBuilder();
     builder.append("LogEvent [");
     if (id != null) {
       builder.append("id=");
       builder.append(id);
       builder.append(", ");
     }
     if (createdAt != null) {
       builder.append("createdAt=");
       builder.append(createdAt);
       builder.append(", ");
     }
     if (userId != null) {
       builder.append("userId=");
       builder.append(userId);
       builder.append(", ");
     }
     if (eventType != null) {
       builder.append("eventType=");
       builder.append(eventType);
       builder.append(", ");
     }
     if (domainObjectClass != null) {
       builder.append("domainObjectClass=");
       builder.append(domainObjectClass);
       builder.append(", ");
     }
     if (description != null) {
       builder.append("description=");
       builder.append(description);
       builder.append(", ");
     }
     if (serializedObject != null) {
       builder.append("serializedObject=");
       builder.append(serializedObject);
       builder.append(", ");
     }
     if (details != null) {
       builder.append("details=");
       builder.append(details);
       builder.append(", ");
     }
     if (correlationId != null) {
       builder.append("correlationId=");
       builder.append(correlationId);
       builder.append(", ");
     }
     if (domainObjectId != null) {
       builder.append("domainObjectId=");
       builder.append(domainObjectId);
       builder.append(", ");
     }
     if (oldReservationStatus != null) {
       builder.append("oldReservationStatus=");
       builder.append(oldReservationStatus);
       builder.append(", ");
     }
     if (newReservationStatus != null) {
       builder.append("newReservationStatus=");
       builder.append(newReservationStatus);
     }
     builder.append("]");
     return builder.toString();
   }
 
   private String serializeObject(Object domainObject) {
     return domainObject.toString();
   }
 
 }
