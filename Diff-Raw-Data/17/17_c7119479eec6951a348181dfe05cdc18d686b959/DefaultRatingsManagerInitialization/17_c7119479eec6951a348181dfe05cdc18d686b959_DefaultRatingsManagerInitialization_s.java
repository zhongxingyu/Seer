 package org.xwiki.contrib.ratings.internal;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.inject.Inject;
import javax.inject.Singleton;
 
 import org.slf4j.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.xwiki.bridge.event.WikiReadyEvent;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.phase.Initializable;
 import org.xwiki.component.phase.InitializationException;
 import org.xwiki.context.Execution;
 import org.xwiki.contrib.ratings.RatingsManager;
 import org.xwiki.observation.EventListener;
 import org.xwiki.observation.event.Event;
 
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 
@Component("ratingswikiinit")
 @Singleton
 public class DefaultRatingsManagerInitialization implements EventListener, Initializable
 {
    @Inject
    Logger logger;
    
    @Inject
    Execution execution;
     
     @Override
     public List<Event> getEvents()
     {
         return Arrays.<Event>asList(new WikiReadyEvent());
     }
 
     @Override
     public String getName()
     {
         return "ratingswikiinit";
     }
     
     /**
      * <p>
      * Retrieve the XWiki context from the current execution context
      * </p>
      * 
      * @return The XWiki context.
      * @throws RuntimeException If there was an error retrieving the context.
      */
     protected XWikiContext getXWikiContext()
     {
         return (XWikiContext) execution.getContext().getProperty("xwikicontext");
     }
 
     /**
      * <p>
      * Retrieve the XWiki private API object
      * </p>
      * 
      * @return The XWiki private API object.
      */
     protected XWiki getXWiki()
     {
         return getXWikiContext().getWiki();
     }
     
     @Override
     public void initialize() throws InitializationException
     {
     }
 
    
     @Override
     public void onEvent(Event wikiReadyEvent, Object arg1, Object arg2)
     {
         // making sure the classes exist
         initRatingsClass();
         initAverageRatingsClass();
         
     }
   
     private void initAverageRatingsClass()
     {
         try {
         XWikiDocument doc;
         XWiki xwiki = getXWiki();
         boolean needsUpdate = false;
         String averageRatingsClassName = RatingsManager.AVERAGE_RATINGS_CLASSNAME;
 
         doc = xwiki.getDocument(averageRatingsClassName, getXWikiContext());
         BaseClass bclass = doc.getXClass();
         
         needsUpdate |= bclass.addNumberField(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_NBVOTES, "Number of Votes", 5, "integer");
         needsUpdate |= bclass.addNumberField(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE, "Average Vote", 5, "float");
         needsUpdate |= bclass.addTextField(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE_METHOD, "Average Vote method", 10);
 
         if (StringUtils.isBlank(doc.getAuthor())) {
             needsUpdate = true;
             doc.setAuthor("xwiki:XWiki.Admin");
         }
         if (StringUtils.isBlank(doc.getCreator())) {
             needsUpdate = true;
             doc.setCreator("xwiki:XWiki.Admin");
         }
         if (StringUtils.isBlank(doc.getParent())) {
             needsUpdate = true;
             doc.setParent("XWiki.XWikiClasses");
         }
 
         String content = doc.getContent();
         if ((content == null) || (content.equals(""))) {
             needsUpdate = true;
             doc.setTitle("XWiki Average Ratings Class");
         }
 
         if (needsUpdate) {
             bclass.setName(averageRatingsClassName);
             xwiki.saveDocument(doc, getXWikiContext());
         }
         } catch (Exception e) {
             logger.error("Error while initializzing average ratings class", e);
         }
     }
 
     private void initRatingsClass()
     {
         try {
         XWikiDocument doc;
         XWiki xwiki = getXWiki();
         boolean needsUpdate = false;
         String ratingsClassName = RatingsManager.RATINGS_CLASSNAME;
 
         doc = xwiki.getDocument(ratingsClassName, getXWikiContext());
         BaseClass bclass = doc.getXClass();
    
         needsUpdate |= bclass.addTextField(RatingsManager.RATING_CLASS_FIELDNAME_AUTHOR, "Author", 30);
         needsUpdate |= bclass.addNumberField(RatingsManager.RATING_CLASS_FIELDNAME_VOTE, "Vote", 5, "integer");
         needsUpdate |= bclass.addDateField(RatingsManager.RATING_CLASS_FIELDNAME_DATE, "Date");
         needsUpdate |= bclass.addTextField(RatingsManager.RATING_CLASS_FIELDNAME_PARENT, "Parent", 30);
 
         if (StringUtils.isBlank(doc.getAuthor())) {
             needsUpdate = true;
             doc.setAuthor("xwiki:XWiki.Admin");
         }
         if (StringUtils.isBlank(doc.getCreator())) {
             needsUpdate = true;
             doc.setCreator("xwiki:XWiki.Admin");
         }
         if (StringUtils.isBlank(doc.getParent())) {
             needsUpdate = true;
             doc.setParent("XWiki.XWikiClasses");
         }
 
         String content = doc.getContent();
         if ((content == null) || (content.equals(""))) {
             needsUpdate = true;
             doc.setTitle("XWiki Ratings Class");
         }
 
         if (needsUpdate) {
             bclass.setName(ratingsClassName);
             xwiki.saveDocument(doc, getXWikiContext());
         }
         } catch (Exception e) {
             logger.error("Error while initializzing ratings class", e);
         }
     }
 }
