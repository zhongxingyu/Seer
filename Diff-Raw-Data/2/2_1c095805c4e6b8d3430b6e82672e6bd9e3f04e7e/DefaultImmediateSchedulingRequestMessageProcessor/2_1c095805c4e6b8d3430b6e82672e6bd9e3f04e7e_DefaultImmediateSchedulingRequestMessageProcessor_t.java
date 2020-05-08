 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package co.altruix.pcc.impl.immediatereschedulingrequestprocessor;
 
 
 import java.util.List;
 
 import javax.jms.Message;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ru.altruix.commons.api.di.PccException;
 import at.silverstrike.pcc.api.model.Booking;
 import at.silverstrike.pcc.api.model.SchedulingObject;
 import at.silverstrike.pcc.api.model.UserData;
 
 
 
 import co.altruix.pcc.api.cdm.PccMessage;
 import co.altruix.pcc.api.immediatereschedulingrequestprocessor.ImmediateSchedulingRequestMessageProcessor;
 import co.altruix.pcc.api.schedulingrequestmessageprocessor.AbstractSchedulingRequestMessageProcessor;
 import co.altruix.pcc.impl.cdm.DefaultImmediateSchedulingRequest;
 
 /**
  * @author DP118M
  * 
  */
 public final class DefaultImmediateSchedulingRequestMessageProcessor extends
         AbstractSchedulingRequestMessageProcessor implements
         ImmediateSchedulingRequestMessageProcessor {
     public static final Logger LOGGER = LoggerFactory
             .getLogger(DefaultImmediateSchedulingRequestMessageProcessor.class);
     private PccMessage message;
     
     public void run() throws PccException {
         final DefaultImmediateSchedulingRequest request =
                 (DefaultImmediateSchedulingRequest) this.message;
     
         final UserData userData = persistence.getUser(request.getUserId());
         LOGGER.debug(
                 "Immediate rescheduling request for user {}, start processing",
                 userData.getUsername());
     
         sendConfirmationForTester(userData, START_CONFIRMATION_MESSAGE);
        exportTasksToFile(userData);
     
         final List<SchedulingObject> createdTasks =
                 importDataFromGoogleTasks(userData);
         final List<Booking> bookings = calculatePlan(userData, createdTasks);
         exportDataToGoogleCalendar(userData, bookings);
     
         LOGGER.debug(
                 "Immediate rescheduling request for user {}, processing finished",
                 userData.getUsername());
     
         sendConfirmationForTester(userData,
                 END_CONFIRMATION_MESSAGE);
     }
     public void setPccMessage(final PccMessage aMessage) {
         this.message = aMessage;
     }
     
     @Override
     public void setMessage(final Message aMessage) {
         /**
          * We are interested in PccMessages only.
          */
     }
 
 }
