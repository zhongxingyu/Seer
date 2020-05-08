 package org.motechproject.ghana.national.messagegateway.domain;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDateTime;
 
 public class NextMondayDispatcher implements DeliveryStrategy{
     @Override
     public LocalDateTime deliveryDate(SMS sms) {
        return sms.getGenerationTime().plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY);
     }
 }
