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
 package nl.surfnet.bod.domain;
 
 import java.net.URI;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 
 import nl.surfnet.bod.nsi.NsiHelper;
 
 import org.hibernate.annotations.Type;
 import org.hibernate.search.annotations.Analyzer;
 import org.hibernate.search.annotations.DocumentId;
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.Indexed;
 import org.ogf.schemas.nsi._2013._07.framework.headers.CommonHeaderType;
 
 @Entity
 @Indexed
 @Analyzer(definition = "customanalyzer")
 @Table(name = "nsi_v2_request_details")
 public class NsiV2RequestDetails {
  private static final String PROTOCOL_VERSION = "application/vdn.ogf.nsi.cs.v2.requester+soap";
 
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @DocumentId
   private Long id;
 
   @Field
   @Basic(optional = true)
   @Type(type = "nl.surfnet.bod.util.PersistentUri")
   private URI replyTo;
 
   @Field
   @Column(nullable = false)
   private String correlationId;
 
   @Field
   @Column(nullable = false)
   private String requesterNsa;
 
   @Field
   @Column(nullable = false)
   private String providerNsa;
 
   @SuppressWarnings("unused")
   private NsiV2RequestDetails() {
   }
 
   public NsiV2RequestDetails(Optional<URI> replyTo, String correlationId, String requesterNsa, String providerNsa) {
     this.replyTo = replyTo.orNull();
     this.correlationId = correlationId;
     this.requesterNsa = Preconditions.checkNotNull(requesterNsa, "requesterNsa is required");
     this.providerNsa = Preconditions.checkNotNull(providerNsa, "providerNsa is required");
   }
 
   public CommonHeaderType createRequesterReplyHeaders() {
     return new CommonHeaderType()
       .withCorrelationId(getCorrelationId())
       .withProtocolVersion(PROTOCOL_VERSION)
       .withProviderNSA(getProviderNsa())
       .withRequesterNSA(getRequesterNsa());
   }
 
   public CommonHeaderType createRequesterNotificationHeaders() {
     return createRequesterReplyHeaders().withCorrelationId(NsiHelper.generateCorrelationId());
   }
 
   public Optional<URI> getReplyTo() {
     return Optional.fromNullable(replyTo);
   }
 
   public String getCorrelationId() {
     return correlationId;
   }
 
   public String getRequesterNsa() {
     return requesterNsa;
   }
 
   public String getProviderNsa() {
     return providerNsa;
   }
 
   @Override
   public String toString() {
     return "NsiV2RequestDetails [replyTo="
         + replyTo
         + ", correlationId="
         + correlationId
         + ", requesterNsa="
         + requesterNsa
         + ", providerNsa="
         + providerNsa
         + "]";
   }
 }
