 package org.nuxeo.rss.reader.webengine;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
import org.nuxeo.ecm.automation.core.annotations.Context;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.webengine.model.WebObject;
 import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
 import org.nuxeo.rss.reader.FeedHelper;
 import org.nuxeo.rss.reader.service.RSSFeedService;
 import org.nuxeo.runtime.api.Framework;
 
 @Path("/rssreader")
 @WebObject(type = "rssreader")
 @Produces("text/html; charset=UTF-8")
 public class RssReaderModuleRoot extends ModuleRoot {
 
    // XXX : to be removed when we'll use RssFeed documents
    private static final String[] URLS = {
            "http://rss.lemonde.fr/c/205/f/3052/index.rss",
            "http://actu.voila.fr/Magic/XML/rss-a-la-une.xml",
            "http://feeds.feedburner.com/fubiz" };

    @Context
     protected RSSFeedService rssFeedService;
 
     @GET
     @Path("/item")
    public Object getItemContent(@QueryParam("i") String id) {
        return getView("item").args(FeedHelper.searchFeedEntry(URLS, id));
     }
 
     @GET
     @Path("/sample")
     public Object getSample() {
         return getView("sample");
     }
 
     @GET
     @Path("/config")
     public Object getConfigPage() throws Exception {
         CoreSession session = ctx.getCoreSession();
         boolean isAbleToCreateNew = false;
         DocumentModelList userFeeds = getRSSService().getCurrentUserRssFeedDocumentModelList(
                 session);
         DocumentModelList globalFeeds = getRSSService().getGlobalFeedsDocumentModelList(
                 session);
         List<DocumentModel> options = new ArrayList<DocumentModel>();
         if (userFeeds != null && globalFeeds != null) {
             for (DocumentModel f1 : globalFeeds) {
                 boolean found = false;
                 String url1 = (String) f1.getPropertyValue("rf:rss_address");
                 for (DocumentModel f2 : userFeeds) {
                     String url2 = (String) f2.getPropertyValue("rf:rss_address");
                     if (url2 != null && url2.equals(url1)) {
                         found = true;
                         break;
                     }
                 }
                 if (!found) {
                     options.add(f1);
                 }
             }
             isAbleToCreateNew = userFeeds.size() < getRSSService().getMaximumFeedsCount(session);
         }
 
         return getView("feed_configuration").arg("userFeeds", userFeeds).arg(
                 "globalFeeds", options).arg("ableToCreateNew", isAbleToCreateNew);
     }
 
     @GET
     @Path("/removeFeed")
     public Object removeFeed(@QueryParam("id") String id) throws Exception {
         CoreSession session = ctx.getCoreSession();
         session.removeDocument(new IdRef(id));
         session.save();
         return getConfigPage();
     }
 
     @GET
     @Path("/addGlobalFeed")
     public Object addGlobalFeed(@QueryParam("feedId") String feedId)
             throws Exception {
         CoreSession session = ctx.getCoreSession();
         String userFeedsContainer = getRSSService().getCurrentUserRssFeedsContainer(
                 session).getPathAsString();
         session.copy(new IdRef(feedId), new PathRef(userFeedsContainer), null);
         return getConfigPage();
     }
 
     @GET
     @Path("/addNewFeed")
     public Object addGlobalFeed(@QueryParam("feedName") String feedName,
             @QueryParam("feedLink") String url) throws Exception {
         CoreSession session = ctx.getCoreSession();
         String userFeedsContainerPath = getRSSService().getCurrentUserRssFeedsContainer(
                 session).getPathAsString();
         DocumentModel feed = session.createDocumentModel(
                 userFeedsContainerPath, feedName, "RssFeed");
         feed.setPropertyValue("dc:title", feedName);
         feed.setPropertyValue("rf:rss_address", url);
         feed = session.createDocument(feed);
         session.save();
         return getConfigPage();
     }
 
     /**
      * @return
      * @throws Exception
      */
     protected RSSFeedService getRSSService() throws Exception {
         if (rssFeedService == null) {
             rssFeedService = Framework.getService(RSSFeedService.class);
         }
         return rssFeedService;
     }
 
 }
