 /*
  * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Nuxeo - initial API and implementation
  */
 
 package org.nuxeo.rss.reader.service;
 
 import static org.nuxeo.ecm.core.management.storage.DocumentStoreManager.MANAGEMENT_ROOT_PATH;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_FEED_IS_DEFAULT_PROPERTY;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_FEED_TYPE;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_FEED_URL_PROPERTY;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_GADGET_ARTICLE_COUNT;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_GADGET_MAX_FEED_COUNT;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_READER_MANAGEMENT_ROOT_NAME;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_READER_MANAGEMENT_ROOT_PATH;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;
 import org.nuxeo.rss.reader.runner.UnrestrictedRssReaderManagementRootGenerator;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.DefaultComponent;
 
 /**
  * Default RSSFeed component implementation, it also default implementation of
  * {@code org.nuxeo.rss.reader.service.RSSFeedService}
 *
  * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
  * @since 5.4.2
  */
 public class RSSFeedComponent extends DefaultComponent implements
         RSSFeedService {
 
     @Override
     public DocumentModel getRssReaderManagementContainer(CoreSession session)
             throws ClientException {
         if (!session.exists(new PathRef(RSS_READER_MANAGEMENT_ROOT_PATH))) {
             new UnrestrictedRssReaderManagementRootGenerator(session,
                     MANAGEMENT_ROOT_PATH).runUnrestricted();
         }
 
         return session.getDocument(new PathRef(RSS_READER_MANAGEMENT_ROOT_PATH));
 
     }
 
     @Override
     public DocumentModel getCurrentUserRssFeedsContainer(CoreSession session)
             throws ClientException {
 
         String userWorkspace = getCurrentUserWorkspace(session);
 
         String userRssFeedPath = userWorkspace + "/"
                 + RSS_READER_MANAGEMENT_ROOT_NAME;
         if (!session.exists(new PathRef(userRssFeedPath))) {
             new UnrestrictedRssReaderManagementRootGenerator(session,
                     userWorkspace).willSetRightsForAdminitrators(false).runUnrestricted();
 
             session.copy(getDefaultRssFeedModels(session), new PathRef(
                     userRssFeedPath));
         }
         return session.getDocument(new PathRef(userRssFeedPath));
     }
 
     protected String getCurrentUserWorkspace(CoreSession session)
             throws ClientException {
        String userName = session.getPrincipal().getName();
        DocumentModel root = session.getRootDocument();
        DocumentModelList list = session.getChildren(root.getRef());
        if (list.size() == 0) {
            throw new ClientException(
                    "No domain detected, we can't create or get the current UserWorkspace.");
        }

        DocumentModel currentDomain = list.get(0);
         UserWorkspaceService uws;
         try {
             uws = Framework.getService(UserWorkspaceService.class);
         } catch (Exception e) {
             throw new ClientException(
                     "Can't fetch the UserWorkspace service, please check", e);
         }
 
        String userWorkspace = uws.getCurrentUserPersonalWorkspace(userName,
                currentDomain).getPathAsString();
         return userWorkspace;
     }
 
     @Override
     public int getDisplayedArticleCount(CoreSession session)
             throws ClientException {
         DocumentModel adminContainer = getRssReaderManagementContainer(session);
         return ((Long) adminContainer.getPropertyValue(RSS_GADGET_ARTICLE_COUNT)).intValue();
     }
 
     @Override
     public int getMaximumFeedsCount(CoreSession session) throws ClientException {
         DocumentModel adminContainer = getRssReaderManagementContainer(session);
         return ((Long) adminContainer.getPropertyValue(RSS_GADGET_MAX_FEED_COUNT)).intValue();
     }
 
     @Override
     public DocumentModelList getGlobalFeedsDocumentModelList(CoreSession session)
             throws ClientException {
         return session.getChildren(
                 getRssReaderManagementContainer(session).getRef(),
                 RSS_FEED_TYPE);
     }
 
     @Override
     public DocumentModelList getCurrentUserRssFeedDocumentModelList(
             CoreSession session) throws ClientException {
         return session.getChildren(getCurrentUserRssFeedsContainer(session).getRef());
     }
 
     @Override
     public List<String> getCurrentUserRssFeedAddresses(CoreSession session)
             throws ClientException {
         List<String> addresses = new ArrayList<String>();
         for (DocumentModel dc : getCurrentUserRssFeedDocumentModelList(session)) {
             addresses.add(dc.getPropertyValue(RSS_FEED_URL_PROPERTY).toString());
         }
         return addresses;
     }
 
     /**
     * Return feeds proposed into the administration view but only ones set as default.
     *
      * @param session
      * @return
      * @throws ClientException
      */
     protected List<DocumentRef> getDefaultRssFeedModels(CoreSession session)
             throws ClientException {
         List<DocumentRef> defaultFeeds = new ArrayList<DocumentRef>();
         DocumentModelList docs = getGlobalFeedsDocumentModelList(session);
         for (DocumentModel doc : docs) {
             if (Boolean.TRUE.equals(doc.getPropertyValue(RSS_FEED_IS_DEFAULT_PROPERTY))) {
                 defaultFeeds.add(doc.getRef());
             }
         }
         return defaultFeeds;
     }
 
 }
