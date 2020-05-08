 /**
  * Copyright (c) 2012, SURFnet BV
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
 package nl.surfnet.bod.domain;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 
 import org.hibernate.search.annotations.Analyzer;
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.Indexed;
 import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
 import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
 import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
 
 @Entity
 @DiscriminatorValue("V2")
 @Indexed
 @Analyzer(definition = "customanalyzer")
 public class ConnectionV2 extends AbstractConnection {
 
   @Column(unique = true, nullable = false)
   @Field
   private String globalReservationId;
 
   @Enumerated(EnumType.STRING)
   @Field
  private ReservationStateEnumType reservationState = ReservationStateEnumType.RESERVE_START;
 
   @Enumerated(EnumType.STRING)
   @Field
  private ProvisionStateEnumType provisionState = ProvisionStateEnumType.UNKNOWN;
 
   @Enumerated(EnumType.STRING)
   @Field
  private LifecycleStateEnumType lifecycleState = LifecycleStateEnumType.CREATED;
 
   @Column(nullable = false)
   @Field
   private String sourceStpId;
 
   @Column(nullable = false)
   @Field
   private String destinationStpId;
 
   @Column(nullable = false, length = 4096)
   private String path = "FIXME";
 
   @Column(nullable = false, length = 4096)
   private String serviceParameters = "FIXME";
 
   @Column(nullable = false)
   @Field
   private String protectionType;
 
   public ConnectionV2() {
     super(NsiVersion.TWO);
   }
 
   public String getGlobalReservationId() {
     return globalReservationId;
   }
 
   public void setGlobalReservationId(String globalReservationId) {
     this.globalReservationId = globalReservationId;
   }
 
   @Override
   public String getConnectionStatus() {
     return reservationState.toString();
   }
 
   public ReservationStateEnumType getReservationState() {
     return reservationState;
   }
 
   public LifecycleStateEnumType getLifecycleState() {
     return lifecycleState;
   }
 
   public void setLifecycleState(LifecycleStateEnumType lifecycleState) {
     this.lifecycleState = lifecycleState;
   }
 
   public ProvisionStateEnumType getProvisionState() {
     return provisionState;
   }
 
   public void setProvisionState(ProvisionStateEnumType provisionState) {
     this.provisionState = provisionState;
   }
 
   public void setReservationState(ReservationStateEnumType reservationState) {
     this.reservationState = reservationState;
   }
 
   public String getSourceStpId() {
     return sourceStpId;
   }
 
   public void setSourceStpId(String sourceStpId) {
     this.sourceStpId = sourceStpId;
   }
 
   public String getDestinationStpId() {
     return destinationStpId;
   }
 
   public void setDestinationStpId(String destitnationStpId) {
     this.destinationStpId = destitnationStpId;
   }
 
   public String getProtectionType() {
     return protectionType;
   }
 
   public void setProtectionType(String protectionType) {
     this.protectionType = protectionType;
   }
 
   public NsiVersion getNsiVersion() {
     return nsiVersion;
   }
 
   @Override
   public String toString() {
     StringBuilder builder = new StringBuilder();
     builder.append("Connection [");
     if (id != null) {
       builder.append("id=");
       builder.append(id);
       builder.append(", ");
     }
     if (connectionId != null) {
       builder.append("connectionId=");
       builder.append(connectionId);
       builder.append(", ");
     }
     if (version != null) {
       builder.append("version=");
       builder.append(version);
       builder.append(", ");
     }
     if (requesterNsa != null) {
       builder.append("requesterNsa=");
       builder.append(requesterNsa);
       builder.append(", ");
     }
     if (providerNsa != null) {
       builder.append("providerNsa=");
       builder.append(providerNsa);
       builder.append(", ");
     }
     if (globalReservationId != null) {
       builder.append("globalReservationId=");
       builder.append(globalReservationId);
       builder.append(", ");
     }
     if (description != null) {
       builder.append("description=");
       builder.append(description);
       builder.append(", ");
     }
     if (reservationState != null) {
       builder.append("resevationState=");
       builder.append(reservationState);
       builder.append(", ");
     }
     if (startTime != null) {
       builder.append("startTime=");
       builder.append(startTime);
       builder.append(", ");
     }
     if (endTime != null) {
       builder.append("endTime=");
       builder.append(endTime);
       builder.append(", ");
     }
     builder.append("desiredBandwidth=");
     builder.append(desiredBandwidth);
     builder.append(", ");
     if (sourceStpId != null) {
       builder.append("sourceStpId=");
       builder.append(sourceStpId);
       builder.append(", ");
     }
     if (destinationStpId != null) {
       builder.append("destinationStpId=");
       builder.append(destinationStpId);
       builder.append(", ");
     }
     if (reservation != null) {
       builder.append("reservation=");
       builder.append(reservation.getId()).append(" ").append(reservation.getName());
       builder.append(", ");
     }
     if (provisionRequestDetails != null) {
       builder.append("provisionRequestDetails=");
       builder.append(provisionRequestDetails);
     }
     builder.append("]");
     return builder.toString();
   }
 
 }
