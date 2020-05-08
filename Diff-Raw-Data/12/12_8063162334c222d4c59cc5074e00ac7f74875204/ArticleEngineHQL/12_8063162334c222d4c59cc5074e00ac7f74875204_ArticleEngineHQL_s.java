 package com.celements.blog.article;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.SpaceReference;
 
 import com.celements.blog.article.ArticleLoadParameter.DateMode;
 import com.celements.blog.article.ArticleLoadParameter.SubscriptionMode;
 import com.celements.blog.plugin.EmptyArticleException;
 import com.celements.blog.service.IBlogServiceRole;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.api.Property;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 @Component
 public class ArticleEngineHQL implements IArticleEngineRole {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       ArticleEngineHQL.class);
   
   @Requirement
   private IBlogServiceRole blogService;
 
   @Requirement
   private Execution execution;
   
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty(
         XWikiContext.EXECUTIONCONTEXT_KEY);
   }
 
   @Override
   public List<Article> getArticles(ArticleLoadParameter param
       ) throws ArticleLoadException {
     try {
      String blogArticleSpace = blogService.getBlogSpaceRef(param.getBlogDocRef()).getName();
       List<String> subscribedBlogs = new ArrayList<String>();
      for (DocumentReference docRef : param.getSubscribedToBlogs()) {
        SpaceReference spaceRef = blogService.getBlogSpaceRef(docRef);
        if (spaceRef != null) {
          subscribedBlogs.add(spaceRef.getName());
         }
       }
       String language = param.getLanguage();
       Set<DateMode> dateModes = param.getDateModes();
       boolean archiveOnly = (dateModes.size() == 1 && dateModes.contains(DateMode.ARCHIVED)) 
           || (dateModes.size() == 0);
       boolean futurOnly = (dateModes.size() == 1 && dateModes.contains(DateMode.FUTURE)) 
           || (dateModes.size() == 0);
       boolean subscribableOnly = !param.isWithBlogArticles();
       boolean withArchive = dateModes.contains(DateMode.ARCHIVED);
       boolean withFutur = dateModes.contains(DateMode.FUTURE);
       Set<SubscriptionMode> subsModes = param.getSubscriptionModes();
       boolean withSubscribable = subsModes.size() > 0;
       boolean withSubscribed = subsModes.contains(SubscriptionMode.SUBSCRIBED);
       boolean withUnsubscribed = subsModes.contains(SubscriptionMode.UNSUBSCRIBED);
       boolean withUndecided = subsModes.contains(SubscriptionMode.UNDECIDED);
       boolean checkAccessRights = true;
       LOGGER.debug(param + "' translated to: blogArticleSpace=" + blogArticleSpace 
           + ", subscribedBlogs=" + subscribedBlogs + ", language=" + language 
           + ", archiveOnly=" + archiveOnly + ", futurOnly=" + futurOnly + ", withArchive=" 
           + withArchive + ", withFutur=" + withFutur + ", subscribableOnly=" 
           + subscribableOnly + ", withSubscribable=" + withSubscribable 
           + ", withSubscribed=" + withSubscribed + ", withUnsubscribed=" 
           + withUnsubscribed + ", withUndecided=" + withUndecided + ", checkAccessRights=" 
           + checkAccessRights);
       return getBlogArticles(blogArticleSpace, subscribedBlogs, language, archiveOnly, 
           futurOnly, subscribableOnly, withArchive, withFutur, withSubscribable, 
           withSubscribed, withUnsubscribed, withUndecided, checkAccessRights);
     } catch (XWikiException xwe) {
       throw new ArticleLoadException(xwe);
     }
   }
   
   List<Article> getBlogArticles(String blogArticleSpace, List<String> subscribedBlogs,
       String language, boolean archiveOnly, boolean futurOnly, boolean subscribableOnly,
       boolean withArchive, boolean withFutur, boolean withSubscribable,
       boolean withSubscribed, boolean withUnsubscribed, boolean withUndecided, 
       boolean checkAccessRights) throws XWikiException {
     List<Article> articles = new ArrayList<Article>();
     String hql = getHQL(blogArticleSpace, language, subscribedBlogs, withSubscribable);
     getArticlesFromDocs(articles, getContext().getWiki().search(hql, getContext()), 
         blogArticleSpace);
     LOGGER.info("Total articles found: '" + articles.size() + "'");
     filterTimespan(articles, language, withArchive, archiveOnly, withFutur, futurOnly);
     LOGGER.info("Total articles after Timespanfilter: '" + articles.size() + "'");
     filterRightsAndSubscription(articles, blogArticleSpace, language, withUnsubscribed, 
         withUndecided, withSubscribed, subscribableOnly, checkAccessRights);
     LOGGER.info("Total articles returned: " + articles.size());
     return articles;
   }
   
   // checkRights = false braucht programmingrights auf blogdoc
   private void filterRightsAndSubscription(List<Article> articles, 
       String blogArticleSpace, String language, boolean withUnsubscribed, 
       boolean withUndecided, boolean withSubscribed, boolean subscribableOnly, 
       boolean checkRights) throws XWikiException {
     List<Article> deleteArticles = new ArrayList<Article>();
     XWikiDocument spaceBlogDoc = blogService.getBlogPageByBlogSpace(blogArticleSpace);
     if(spaceBlogDoc == null){
       LOGGER.debug("Missing Blog Configuration! (Blog space: '" + blogArticleSpace 
           + "')");
       deleteArticles.addAll(articles);
     } else{
       Document origBlogDoc = spaceBlogDoc.newDocument(getContext());
       for (Iterator<Article> artIter = articles.iterator(); artIter.hasNext();) {
         Article article = (Article) artIter.next();
         try {
           XWikiDocument articleDoc = getContext().getWiki().getDocument(
               article.getDocumentReference(), getContext());
           DocumentReference blogDocRef = blogService.getBlogDocRefByBlogSpace(
               articleDoc.getDocumentReference().getLastSpaceReference().getName());
           LOGGER.debug("articleDoc='" + articleDoc + "', " + blogDocRef);
           Document blogDoc = getContext().getWiki().getDocument(blogDocRef, getContext()
               ).newDocument(getContext());
           boolean hasRight = false;
           boolean hasEditOnBlog = false;
           if(checkRights || !blogDoc.hasProgrammingRights()){
             LOGGER.info("'" + article.getDocName() + "' - Checking rights. Reason: " +
                 "checkRights='" + checkRights + "' || !programming='" + 
                 !blogDoc.hasProgrammingRights() + "'");
             Date publishdate = article.getPublishDate(language);
             if((publishdate != null) && (publishdate.after(new Date()))){
               if(blogDoc.hasAccessLevel("edit")){
                 hasRight = true;
               }
             } else if(blogDoc.hasAccessLevel("view")){
               hasRight = true;
             }
             LOGGER.debug("'" + articleDoc.getSpace() + "' != '" + blogArticleSpace + 
                 "' && origBlogDoc.hasAccessLevel('edit') => '"
                 + origBlogDoc.hasAccessLevel("edit") + "'");
             if(!articleDoc.getSpace().equals(blogArticleSpace) && origBlogDoc
                 .hasAccessLevel("edit")){
               hasEditOnBlog = true;
             }
           } else{
             LOGGER.info("'" + article.getDocName() + "' - Saved with programming rights " +
                 "and not checking for rights.");
             hasRight = true;
             hasEditOnBlog = true;
           }
           
           LOGGER.info("'" + article.getDocName() + "' - hasRight: '" + hasRight + "' " +
               "hasEditOnBlog: '" + hasEditOnBlog + "'");
           if(hasRight){
             if(!articleDoc.getSpace().equals(blogArticleSpace)){
               Boolean isSubscribed = article.isSubscribed();
               
               if(isSubscribed == null){
                 if(!withUndecided || !hasEditOnBlog){
                   LOGGER.info("'" + article.getDocName() + "' - Removed reason: from " +
                       "subscribed blog && isUndecided && (!withUndecided='" + 
                       !withUndecided + "' || !hasEditOnBlog='" + !hasEditOnBlog + "')");
                   deleteArticles.add(article);
                 }
               } else {
                 if(!isSubscribed && (!withUnsubscribed || !hasEditOnBlog)){
                   LOGGER.info("'" + article.getDocName() + "' - Removed reason: from " +
                       "subscribed blog && isDecided && ((!isSubscribed='" + !isSubscribed + 
                       "' && !withUnsubscribed='" + !withUnsubscribed + "') || " +
                       "!hasEditOnBlog='" + !hasEditOnBlog + "')");
                   deleteArticles.add(article);
                 } else if(isSubscribed && !withSubscribed){
                   LOGGER.info("'" + article.getDocName() + "' - Removed reason: from " +
                       "subscribed blog && isDecided && (isSubscribed='" + isSubscribed + 
                       "' && !withSubscribed='" + !withSubscribed + "')");
                   deleteArticles.add(article);
                 }
               }
             } else if(subscribableOnly){
               LOGGER.info("'" + article.getDocName() + "' - Removed reason: from own " +
                   "blog, but subscribableOnly='" + subscribableOnly + "'");
               deleteArticles.add(article);
             }
           } else{
             LOGGER.info("'" + article.getDocName() + "' - Removed reason: has no rights");
             deleteArticles.add(article);
           }
         } catch (XWikiException exp) {
           LOGGER.error("filterRightsAndSubscription: Failed to check rights on: "
               + article.getDocumentReference(), exp);
         }
       }
     }
     for (Iterator<Article> delIter = deleteArticles.iterator(); delIter.hasNext();) {
       articles.remove(delIter.next());
     }
   }
 
   void filterTimespan(List<Article> articles, String language, 
       boolean withArchive, boolean archiveOnly, boolean withFutur, boolean futurOnly){
     Date now = new Date();
     List<Article> deleteArticles = new ArrayList<Article>();
     for (Iterator<Article> artIter = articles.iterator(); artIter.hasNext();) {
       Article article = (Article) artIter.next();
       Date archivedate = article.getArchiveDate(language);
       Date publishdate = article.getPublishDate(language);
       if(((archivedate != null) && archivedate.before(now)) 
           && ((!withArchive && !archiveOnly) || futurOnly)){
         deleteArticles.add(article);
       }
       if(((publishdate != null) && publishdate.after(now)) && ((!withFutur && !futurOnly)
           || (archiveOnly && (!withFutur || (archivedate == null) 
           || ((archivedate != null) && archivedate.after(now)))))){
         deleteArticles.add(article);
       }
       if(((publishdate == null) || publishdate.before(now)) 
           && ((archivedate == null) || archivedate.after(now)) 
           && (archiveOnly || futurOnly)){
         deleteArticles.add(article);
       }
     }
     for (Iterator<Article> delIter = deleteArticles.iterator(); delIter.hasNext();) {
       articles.remove(delIter.next());
     }
   }
   
   private void getArticlesFromDocs(List<Article> articles, List<Object> articleDocNames, 
       String blogArticleSpace) throws XWikiException{
     LOGGER.debug("Matching articles found: " + articleDocNames.size());
     for (Object articleFullNameObj : articleDocNames) {
       String articleFullName = (String) articleFullNameObj.toString();
       XWikiDocument articleDoc = getContext().getWiki().getDocument(articleFullName, 
           getContext());
       Article article = null;
       try{
         article = new Article(articleDoc.newDocument(getContext()), getContext());
       } catch (EmptyArticleException e) {
         LOGGER.info(e);
       }
       if((article != null) && (blogArticleSpace.equals(articleDoc.getSpace()) 
           || (article.isSubscribable() == Boolean.TRUE))){
         articles.add(article);
       }
     }
   }
   
   private String getHQL(String blogArticleSpace, String language, 
       List<String> subscribedBlogs, boolean withSubscribable) throws XWikiException {
     String useInt = " ";
     String subscribableHQL = "";
     String subscribedBlogsStr = "";
     LOGGER.debug("if params: (" + subscribedBlogs + "!= null) (" + ((subscribedBlogs 
         != null)?subscribedBlogs.size():"null") + " > 0) (withSubscribable = " + 
         withSubscribable + ")");
     if((subscribedBlogs != null) && (subscribedBlogs.size() > 0) && withSubscribable){
       //useInt = ", IntegerProperty as int ";
       subscribableHQL = /*to slow with this query part "and (obj.id = int.id.id " +
           "and int.id.name = 'isSubscribable' " +
           "and int.value='1')*/ ")";
       
       for (Iterator<String> blogIter = subscribedBlogs.iterator(); blogIter.hasNext();) {
         String blogSpace = (String) blogIter.next();
         Document blogDoc = blogService.getBlogPageByBlogSpace(blogSpace).newDocument(
             getContext());
         com.xpn.xwiki.api.Object obj = blogDoc.getObject("Celements2.BlogConfigClass");
         Property prop = obj.getProperty("is_subscribable");
         LOGGER.debug("blogDoc is '" + blogDoc.getFullName() + "' and obj is '" + obj + 
             "' the is_subscribable property is '" + prop + "'");
         if(prop != null){
           int isSubscribable = Integer.parseInt(prop.getValue().toString());
           LOGGER.debug("is_subscribable property exists and its value is: '" + 
               isSubscribable + "'");
           if(isSubscribable == 1){
             if(subscribedBlogsStr.length() > 0){
               subscribedBlogsStr += "or ";
             } else{
               subscribedBlogsStr = "or ((";
             }
             subscribedBlogsStr += "doc.space='" + blogSpace + "' ";
           }
         }
       }
       
       if(subscribedBlogsStr.length() > 0){
         subscribedBlogsStr += ") " + subscribableHQL;
       }
       
     }
     
     String hql = "select doc.fullName from XWikiDocument as doc, BaseObject as obj, " +
         "DateProperty as date, StringProperty as lang" + useInt;
     hql += "where obj.name=doc.fullName ";
     hql += "and obj.className='XWiki.ArticleClass' ";
     hql += "and (doc.space = '" + blogArticleSpace + "' " + subscribedBlogsStr + ") ";
     hql += "and lang.id.id=obj.id ";
     hql += "and lang.id.name='lang' ";
     hql += "and lang.value = '" + language + "' ";
     hql += "and obj.id = date.id.id ";
     hql += "and date.id.name='publishdate' ";
     hql += "order by date.value desc, doc.creationDate desc ";
     
     LOGGER.debug("hql built: " + hql);
     return hql;
   }
 
   void injectBlogService(IBlogServiceRole blogService) {
     this.blogService = blogService;
   }
 
 }
