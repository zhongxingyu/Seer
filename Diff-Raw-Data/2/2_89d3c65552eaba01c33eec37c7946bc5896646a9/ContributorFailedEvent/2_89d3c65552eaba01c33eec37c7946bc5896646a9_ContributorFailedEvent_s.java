 /*
  * #%L
  * Bitrepository Core
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.client.eventhandler;
 
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 
 import static org.bitrepository.client.eventhandler.OperationEvent.OperationEventType.COMPONENT_FAILED;
 
 public class ContributorFailedEvent extends ContributorEvent {
     private final ResponseCode responseCode;
 
     /**
      * @param responseCode The response code from any response indicating the failure. Might be null, if no relevant
      *                     response exists.
      */
     public ContributorFailedEvent(
             String info,
             String contributorID,
             ResponseCode responseCode,
             String conversationID) {
         super(COMPONENT_FAILED, info, contributorID, conversationID);
         this.responseCode = responseCode;
     }
 
     public ResponseCode getResponseCode() {
         return responseCode;
     }
 
     @Override
     public String additionalInfo() {
        return super.additionalInfo() + "responseCode: " + getContributorID();
     }
 }
