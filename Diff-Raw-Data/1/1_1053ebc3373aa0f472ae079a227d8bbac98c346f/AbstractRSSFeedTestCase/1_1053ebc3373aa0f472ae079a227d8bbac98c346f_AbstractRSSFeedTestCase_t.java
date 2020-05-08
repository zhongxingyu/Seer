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
 
 package org.nuxeo.rss.reader;
 
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_READER_MANAGEMENT_ROOT_PATH;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_FEED_TYPE;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_GADGET_ARTICLE_COUNT;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_GADGET_MAX_FEED_COUNT;
 
 import org.junit.Before;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.core.event.EventService;
 import org.nuxeo.rss.reader.service.RSSFeedService;
 import org.nuxeo.runtime.api.Framework;
 
 import com.google.inject.Inject; /**
  * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
  */
 public abstract class AbstractRSSFeedTestCase {
     @Inject
     RSSFeedService rssFeedService;
 
     @Inject
     CoreSession session;
 
     protected DocumentModel buildFeed(String name, boolean isDefault, String url) throws ClientException {
         DocumentModel feed = session.createDocumentModel(RSS_FEED_TYPE);
         feed.setPropertyValue("dc:title", name);
         feed.setPropertyValue("rf:is_default_feed", isDefault);
         feed.setPropertyValue("rf:rss_address", url);
         feed.setPathInfo(RSS_READER_MANAGEMENT_ROOT_PATH, name);
         return session.createDocument(feed);
     }
 
     protected void waitForAsyncCompletion() {
         Framework.getLocalService(EventService.class).waitForAsyncCompletion();
     }
 
 
     /**
      * Change gadget preference. Use a value < 1 to not change the existing one.
      *
      * @param maxFeeds
      * @param articleCount
      */
     protected void changeGadgetPreferencesValues(int maxFeeds, int articleCount)
             throws ClientException {
         // Ensure that we have the document
         rssFeedService.getDisplayedArticleCount(session);
        waitForAsyncCompletion();
 
         DocumentModel containerModel = session.getDocument(new PathRef(RSS_READER_MANAGEMENT_ROOT_PATH));
         if (maxFeeds >= 0) {
             containerModel.setPropertyValue(RSS_GADGET_MAX_FEED_COUNT, maxFeeds);
         }
         if (articleCount >= 0) {
             containerModel.setPropertyValue(RSS_GADGET_ARTICLE_COUNT, articleCount);
         }
         session.saveDocument(containerModel);
         session.save();
         waitForAsyncCompletion();
     }
 
     @Before
     public void addSystemRoot() throws ClientException {
         if (!session.exists(new PathRef("/management"))) {
             DocumentModel management = session.createDocumentModel("Workspace");
             management.setPropertyValue("dc:title", "management");
             management.setPathInfo("/", "management");
             session.createDocument(management);
         }
 
         if (!session.exists(new PathRef("/default-domain"))) {
             DocumentModel domain = session.createDocumentModel("Domain");
             domain.setPropertyValue("dc:title", "default-domain");
             domain.setPathInfo("/", "default-domain");
             session.createDocument(domain);
         }
         session.save();
         waitForAsyncCompletion();
     }
 }
