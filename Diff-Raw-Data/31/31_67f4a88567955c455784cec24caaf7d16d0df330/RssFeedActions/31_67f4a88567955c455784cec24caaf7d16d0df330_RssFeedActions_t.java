 package org.nuxeo.rss.reader.manager.seam;
 
 import static org.jboss.seam.annotations.Install.FRAMEWORK;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_FEED_CONTAINER_PATH;
 import static org.nuxeo.rss.reader.manager.api.Constants.RSS_FEED_TYPE;
 
 import java.io.Serializable;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Install;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.platform.usermanager.UserManager;
 import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;
 import org.nuxeo.rss.reader.service.RSSFeedService;
 
@Name("rssFeedActions")
 @Scope(ScopeType.CONVERSATION)
 @Install(precedence = FRAMEWORK)
 public class RssFeedActions implements Serializable {
 
     private static final long serialVersionUID = 8882417548656036277L;
 
     protected static final Log log = LogFactory.getLog(RssFeedActions.class);
 
     @In(create = true)
     protected transient CoreSession documentManager;
 
     @In(create = true)
     protected transient DocumentActions documentActions;
 
     @In(create = true)
     protected transient UserManager userManager;
 
     @In(create = true)
    protected transient RSSFeedService rssFeed;
 
     protected DocumentModel newRssFeedModel = null;
 
     protected boolean showForm = false;
 
     public DocumentModel getBareFeedReaderModel() throws ClientException {
         return documentManager.createDocumentModel(RSS_FEED_TYPE);
     }
 
     public DocumentModel getNewReportModel() throws ClientException {
         if (newRssFeedModel == null) {
             newRssFeedModel = getBareFeedReaderModel();
         }
         return newRssFeedModel;
     }
 
     public void saveDocument() throws ClientException {
        rssFeed.createRssFeedModelContainerIfNeeded(documentManager);
         documentActions.saveDocument(newRssFeedModel);
         resetDocument();
         toggleForm();
     }
 
     protected void resetDocument() {
         newRssFeedModel = null;
     }
 
     public boolean isShowForm() {
         return showForm;
     }
 
     public void toggleForm() {
         showForm = !showForm;
     }
 
     public void toggleAndReset() {
         toggleForm();
         resetDocument();
     }
 
     public String getRssFeedsContainerPath() throws ClientException {
 
         return RSS_FEED_CONTAINER_PATH;
     }
 }
