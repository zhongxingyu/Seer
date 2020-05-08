 /*
  * #%L
  * Bitrepository Access
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
 package org.bitrepository.access.getfileids;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;

 import org.bitrepository.access.ContributorQuery;
 import org.bitrepository.access.ContributorQueryUtils;
 import org.bitrepository.access.getfileids.conversation.GetFileIDsConversationContext;
 import org.bitrepository.access.getfileids.conversation.IdentifyPillarsForGetFileIDs;
 import org.bitrepository.client.AbstractClient;
 import org.bitrepository.client.conversation.mediator.ConversationMediator;
 import org.bitrepository.client.eventhandler.EventHandler;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The reference implementation of the client side of the GetFileIDs identification and operation.
  * The default <code>GetFileIDsClient</code>
  *
  * This class is just a thin wrapper which creates a conversion each time a operation is started. The conversations 
  * takes over the rest of the operation handling.
  */
 public class ConversationBasedGetFileIDsClient extends AbstractClient implements GetFileIDsClient {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     /**
      * @see AbstractClient
      */
     public ConversationBasedGetFileIDsClient(MessageBus messageBus, ConversationMediator conversationMediator,
                                              Settings settings, String clienID) {
         super(settings, conversationMediator, messageBus, clienID);
     }
 
     @Override
     public void getFileIDs(
             String collectionID,
             ContributorQuery[] contributorQueries,
             String fileID,
             URL addressForResult,
             EventHandler eventHandler) {
         ArgumentValidator.checkNotNullOrEmpty(collectionID, "collectionID");
         validateFileID(fileID);
         if (contributorQueries == null) {
             contributorQueries = ContributorQueryUtils.createFullContributorQuery(
                     settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID());
         }
 
         log.info("Requesting the fileIDs for file '" + fileID + "' with query "+
                 Arrays.asList(contributorQueries) + ". " +
                 (addressForResult != null ?  "The result should be uploaded to '" + addressForResult + "'." : ""));
 
         GetFileIDsConversationContext context = new GetFileIDsConversationContext(
                 collectionID, contributorQueries, fileID, addressForResult, settings, messageBus, clientID,
                 ContributorQueryUtils.getContributors(contributorQueries), eventHandler);
 
         startConversation(context, new IdentifyPillarsForGetFileIDs(context));
     }
 
     /**
      * Used to create a <code>AuditTrailQuery[]</code> array in case no array is defined.
      * @return A <code>AuditTrailQuery[]</code> array requesting all audit trails from all the defined contributers.
      */
     private ContributorQuery[] createFullContributorQuery() {
         List<String> contributers = settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID();
        List<ContributorQuery> componentQueryList = new ArrayList<ContributorQuery>(contributers.size());
         for (String contributer : contributers) {
            componentQueryList.add(new ContributorQuery(contributer, null, null, null));
         }
        return componentQueryList.toArray(new ContributorQuery[componentQueryList.size()]);
     }
 }
