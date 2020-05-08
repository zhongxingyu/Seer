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
 package nl.surfnet.bod.nsi.v2;
 
 import java.util.Arrays;
 
 import nl.surfnet.bod.domain.ConnectionV2;
 import nl.surfnet.bod.util.XmlUtils;
 
 import org.ogf.schemas.nsi._2013._04.connection.types.*;
 import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 
 public final class ConnectionsV2 {
 
   public static final Function<ConnectionV2, QuerySummaryResultType> toQuerySummaryResultType = new Function<ConnectionV2, QuerySummaryResultType>() {
     public QuerySummaryResultType apply(ConnectionV2 connection) {
 
       return new QuerySummaryResultType()
         .withRequesterNSA("requester")
        .withCriteria(new ReservationConfirmCriteriaType()
           .withBandwidth(connection.getDesiredBandwidth())
           .withSchedule(new ScheduleType()
             .withEndTime(XmlUtils.toGregorianCalendar(connection.getEndTime().get()))
             .withStartTime(XmlUtils.toGregorianCalendar(connection.getStartTime().get())))
           .withVersion(0)
           .withServiceAttributes(new TypeValuePairListType())
           .withPath(new PathType()
             .withSourceSTP(toStpType(connection.getSourceStpId()))
             .withDestSTP(toStpType(connection.getDestinationStpId()))
             .withDirectionality(DirectionalityType.BIDIRECTIONAL)))
         .withConnectionId(connection.getConnectionId())
         .withConnectionStates(new ConnectionStatesType()
           .withReservationState(connection.getReservationState())
           .withLifecycleState(connection.getLifecycleState())
           .withProvisionState(connection.getProvisionState())
           .withDataPlaneStatus(new DataPlaneStatusType().withActive(false).withVersionConsistent(true).withVersion(0)));
     }
   };
 
   private ConnectionsV2() {
   }
 
   public static  StpType toStpType(String sourceStpId) {
     String[] parts = sourceStpId.split(":");
     String networkId = Joiner.on(":").join(Arrays.copyOfRange(parts, 0, parts.length - 2));
 
     return new StpType().withNetworkId(networkId).withLocalId(parts[parts.length - 1]);
   }
 
 }
