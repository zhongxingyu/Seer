 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.blog.plugin;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.VelocityContext;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.blog.service.IBlogServiceRole;
 import com.celements.web.plugin.api.CelementsWebPluginApi;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Api;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.api.Property;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
 import com.xpn.xwiki.plugin.XWikiPluginInterface;
 import com.xpn.xwiki.web.Utils;
 
 public class BlogPlugin extends XWikiDefaultPlugin{
   
   private static final String DEFAULT_RECEIVER_SPACE = "NewsletterReceivers";
   private static Log LOGGER = LogFactory.getFactory().getInstance(BlogPlugin.class);
   IBlogServiceRole injected_BlogService;
   
   public BlogPlugin(String name, String className, XWikiContext context) {
     super(name, className, context);
     init(context);
   }
   
   @Override
   public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
     return new BlogPluginApi((BlogPlugin) plugin, context);
   }
 
   @Override
   public String getName() {
     LOGGER.debug("Entered method getName");
     return "celementsblog";
   }
 
   /**
    * @deprecated since 1.4 use BlogService.getBlogPageByBlogSpace instead
    */
   @Deprecated
   public XWikiDocument getBlogPageByBlogSpace(String blogSpaceName,
       XWikiContext context) throws XWikiException{
     return BlogUtils.getInstance(
         ).getBlogPageByBlogSpace(blogSpaceName, context);
   }
   
   /**
    * @param blogArticleSpace Space where the blog's articles are saved.
    * @param subscribedBlogsStr Comma separated String with all the blog article spaces 
    *        the blog has subscribed to.
    * @param language default language
    * @param archiveOnly Only get articles from the archive (archivedate < now)
    * @param futurOnly Only get articles that are not yet published (publishdate > now)
    * @param subscribableOnly Only get articles from subscribed blogs, but not the ones 
    *        from the blog the user is on.
    * @param withArchive Include archived articles in the answer. Has no effect if 
    *        archiveOnly = true.
    * @param withFutur Include not yet published articles. Only possible if the page has 
    *        been saved with programmingrights or the user has edit right on the article. Has no effect if futurOnly = true.
    * @param withSubscribable Include articles from subscribed blogs.
    * @param withSubscribed Include articles the blog has subscribed to.
    * @param withUnsubscribed Include articles the blog has unsubscribed from. Only works 
    *        with edit rights or programmingrights.
    * @param withUndecided Include articles the blog has not yet desided about a 
    *        subscription. Only works with edit rights or programmingrights.
    * @param checkAccessRights Do pay attention to the rights. Default = true if no 
    *        programmingrights.
    * @param context
    * @return
    * @throws XWikiException
    */
   public List<Article> getBlogArticles(String blogArticleSpace, String subscribedBlogsStr,
       String language, boolean archiveOnly, boolean futurOnly, boolean subscribableOnly,
       boolean withArchive, boolean withFutur, boolean withSubscribable,
       boolean withSubscribed, boolean withUnsubscribed, boolean withUndecided, 
       boolean checkAccessRights, XWikiContext context) throws XWikiException{
     List<Article> articles = new ArrayList<Article>();
     List<String> subscribedBlogs = new ArrayList<String>();
     String[] subscribedBlogArray = subscribedBlogsStr.split(",");
     for (int i = 0; i < subscribedBlogArray.length; i++) {
       if((subscribedBlogArray[i] != null) 
           && (subscribedBlogArray[i].trim().length() > 0)){
         LOGGER.info("Subscribed to Blog: '" + subscribedBlogArray[i] + "'");
         subscribedBlogs.add(subscribedBlogArray[i]);
       }
     }
     String hql = getHQL(blogArticleSpace, language, subscribedBlogs, withSubscribable, 
         context);
     getArticlesFromDocs(articles, context.getWiki().search(hql, context), 
         blogArticleSpace, context);
     LOGGER.info("Total articles found: '" + articles.size() + "'");
     filterTimespan(articles, language, withArchive, archiveOnly, withFutur, futurOnly, 
         context);
     LOGGER.info("Total articles after Timespanfilter: '" + articles.size() + "'");
     filterRightsAndSubscription(articles, blogArticleSpace, language, withUnsubscribed, 
         withUndecided, withSubscribed, subscribableOnly, checkAccessRights, context);
     LOGGER.info("Total articles returned: " + articles.size());
     return articles;
   }
   
   // checkRights = false braucht programmingrights auf blogdoc
   private void filterRightsAndSubscription(List<Article> articles, 
       String blogArticleSpace, String language, boolean withUnsubscribed, 
       boolean withUndecided, boolean withSubscribed, boolean subscribableOnly, 
       boolean checkRights, XWikiContext context) throws XWikiException {
     List<Article> deleteArticles = new ArrayList<Article>();
     XWikiDocument spaceBlogDoc = getBlogService().getBlogPageByBlogSpace(blogArticleSpace);
     if(spaceBlogDoc == null){
       LOGGER.debug("Missing Blog Configuration! (Blog space: '" + blogArticleSpace 
           + "')");
       deleteArticles.addAll(articles);
     } else{
       Document origBlogDoc = spaceBlogDoc.newDocument(context);
       for (Iterator<Article> artIter = articles.iterator(); artIter.hasNext();) {
         Article article = (Article) artIter.next();
         try {
           XWikiDocument articleDoc = context.getWiki().getDocument(
               article.getDocumentReference(), context);
           DocumentReference blogDocRef = getBlogService().getBlogDocRefByBlogSpace(
               articleDoc.getDocumentReference().getLastSpaceReference().getName());
           LOGGER.debug("articleDoc='" + articleDoc + "', " + blogDocRef);
           Document blogDoc = context.getWiki().getDocument(blogDocRef, context
               ).newDocument(context);
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
 
   private void filterTimespan(List<Article> articles, String language, 
       boolean withArchive, boolean archiveOnly, boolean withFutur, boolean futurOnly, 
       XWikiContext context){
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
       String blogArticleSpace, XWikiContext context) throws XWikiException{
     LOGGER.debug("Matching articles found: " + articleDocNames.size());
     for (Object articleFullNameObj : articleDocNames) {
       String articleFullName = (String) articleFullNameObj.toString();
       XWikiDocument articleDoc = context.getWiki().getDocument(articleFullName, context);
       Article article = null;
       try{
         article = new Article(articleDoc.newDocument(context), context);
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
       List<String> subscribedBlogs, boolean withSubscribable, XWikiContext context) 
       throws XWikiException{
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
         Document blogDoc = getBlogPageByBlogSpace(blogSpace, context).newDocument(
             context);
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
   
   @SuppressWarnings("unchecked")
   public String subscribeNewsletter(boolean inactiveWithoutMail, XWikiContext context) 
       throws XWikiException{
     Map<String, String> request = new HashMap<String, String>();
     Map<String, String[]> req = context.getRequest().getParameterMap();
     for (String key : req.keySet()) {
       if((req.get(key) != null) && (req.get(key).length > 0)) {
         request.put(key, req.get(key)[0]);
       }
     }
     return subscribeNewsletter(inactiveWithoutMail, request, context);
   }
   
   public Map<String, String> batchImportReceivers(boolean inactive, String importData, 
       String newsletterFullName, XWikiContext context) {
     LOGGER.info("Starting batch newsletter receiver import.");
     Map<String, String> results = new TreeMap<String, String>();
     Map<String, String> data = new HashMap<String, String>();
     data.put("subsBlog", newsletterFullName);
     for (String emailCandidate : splitImportDataToEmailCandidates(importData)) {
       if(containsEmailAddress(emailCandidate)) {
         String email = extractEmailFromString(emailCandidate);
         email = email.toLowerCase();
         data.put("emailadresse", email);
         XWikiDocument userDoc = null;
         try {
           String userDocName = subscribeNewsletter(inactive, data, context);
           if("".equals(userDocName)) {
             userDocName = getSubscriberDoc(email, context);
           }
           userDoc = context.getWiki().getDocument(userDocName, context);
         } catch (XWikiException e) {
           LOGGER.error("Exception while subscribing email '" + email + "' to '" + 
               newsletterFullName + "'", e);
         }
         if(userDoc != null) {
           BaseObject subsObj = userDoc.getObject("Celements.NewsletterReceiverClass", 
               "subscribed", newsletterFullName, false);
           if((subsObj != null) && (subsObj.getIntValue("isactive") == 1)) {
             results.put(email, context.getMessageTool().get(
                 "cel_newsletter_subscriber_active"));
           } else {
             results.put(email, context.getMessageTool().get(
                 "cel_newsletter_subscriber_inactive"));
           }
         } else {
           results.put(email, context.getMessageTool().get(
               "cel_newsletter_subscriber_invalid"));
         }
       } else {
         if(emailCandidate.trim().length() > 0) {
           results.put(emailCandidate, context.getMessageTool().get(
               "cel_newsletter_subscriber_invalid"));
         }
       }
     }
     return results;
   }
 
   String[] splitImportDataToEmailCandidates(String importData) {
     return importData.split("[,\r\n]+");
   }
 
   String extractEmailFromString(String emailCandidate) {
     return emailCandidate.replaceAll(getContainsEmailRegex(), "$1");
   }
 
   boolean containsEmailAddress(String email) {
     return email.matches(getContainsEmailRegex());
   }
 
   String getContainsEmailRegex() {
    return ".*?([\\w+\\.\\-\\_]+[@][\\w\\-\\_]+([.][\\w\\-\\_]+)+).*";
   }
   
   public String subscribeNewsletter(boolean inactiveWithoutMail, Map<String, String> 
       request, XWikiContext context) throws XWikiException{
     XWiki wiki = context.getWiki();
     boolean subscribed = false;
     String email = request.get("emailadresse");
     email = email.toLowerCase();
     String docName = getSubscriberDocName(email, context);
     LOGGER.info("ReceiverDoc is " + docName);
     XWikiDocument receiverDoc = wiki.getDocument(docName, context);
     XWikiDocument blogDoc = getBlogDoc(request, context);
     BaseObject obj = receiverDoc.getObject("Celements.NewsletterReceiverClass", 
         "subscribed", blogDoc.getFullName(), false);
     if((email != null) && !"".equals(email.trim()) && (obj == null)){
       obj = receiverDoc.newObject("Celements.NewsletterReceiverClass", context);
       obj.setStringValue("email", email);
       if(!"XWiki.XWikiGuest".equals(context.getUser()) && !inactiveWithoutMail) {
         obj.setIntValue("isactive", 1);
       } else {
         obj.setIntValue("isactive", 0);
       }
       obj.setStringValue("subscribed", blogDoc.getFullName());
       wiki.saveDocument(receiverDoc, context);
       LOGGER.info("new ReceiverObj is " + obj);
       subscribed = true;
     }
     if((obj != null) && (obj.getIntValue("isactive") == 1) && inactiveWithoutMail) {
       obj.setIntValue("isactive", 0);
       wiki.saveDocument(receiverDoc, context);
     }
     LOGGER.trace("getStringValue: " + obj.getStringValue("subscribed"));
     if((obj != null) && (obj.getIntValue("isactive") != 1) 
         && "XWiki.XWikiGuest".equals(context.getUser()) && !inactiveWithoutMail) {
       sendNewsletterActivationMail(obj, blogDoc, docName, request, context);
       subscribed = true;
     }
     if(!subscribed) { docName = ""; }
     return docName;
   }
   
   void sendNewsletterActivationMail(BaseObject obj, XWikiDocument blogDoc, String docName,
       Map<String, String> request, XWikiContext context) throws XWikiException {
     String email = request.get("emailadresse");
     email = email.toLowerCase();
     if(context.getWiki().exists("Tools.NewsletterSubscriptionActivation", context)) {
       VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
       vcontext.put("activationKey", getActivationKey(obj, docName));
       vcontext.put("blog", blogDoc);
       vcontext.put("email", email);
       XWikiDocument emailContentDoc = context.getWiki()
           .getDocument("Tools.NewsletterSubscriptionActivation", context);
       String emailContent = emailContentDoc.getTranslatedContent(context);
       String htmlContent = context.getWiki().getRenderingEngine().interpretText(
           emailContent, context.getDoc(), context);
       String emailTitle = emailContentDoc.getTranslatedDocument(context).getTitle();
       String renderedTitle = context.getWiki().getRenderingEngine().interpretText(
           emailTitle, context.getDoc(), context);
       BaseObject blogConf = blogDoc.getObject("Celements2.BlogConfigClass");
       String from = "";
       String reply = "";
       if(blogConf != null) {
         from = blogConf.getStringValue("from_address");
         reply = blogConf.getStringValue("reply_to_address");
       }
       if((from == null) || "".equals(from.trim())) {
         from = context.getWiki().getXWikiPreference("admin_email", context);
       }
       if((reply == null) || "".equals(reply.trim())) {
         reply = from;
       }
       CelementsWebPluginApi celementsweb = (CelementsWebPluginApi)context.getWiki()
           .getPluginApi("celementsweb", context);
       celementsweb.getPlugin().sendMail(from, reply, email, null, null, renderedTitle, 
           htmlContent, "", null, null, context);
     } else {
       LOGGER.error("No newsletter activation Mail sent for '" + email + "'. No " +
           "Mailcontent found in Tools.NewsletterSubscriptionActivation");
     }
   }
   
   String getActivationKey(BaseObject obj, String docName) {
     String key = docName + "|" + obj.getNumber();
     MessageDigest digest = null;
     try {
       digest = MessageDigest.getInstance("SHA-1");
       digest.update(key.getBytes());
     } catch (NoSuchAlgorithmException e) {
       LOGGER.error("SHA-1 algorithm not available.");
     }
     String hash = "";
     if(digest != null) {
       hash = new String((new Hex()).encode(digest.digest()));
     }
     return hash;
   }
 
   private String getSubscriberDocName(String email, XWikiContext context) 
       throws XWikiException {
     XWiki wiki = context.getWiki();
     String docName = getSubscriberDoc(email, context);
     if("".equals(docName) || !wiki.exists(docName, context)) {
       while("".equals(docName) || wiki.exists(docName, context)) {
         docName = DEFAULT_RECEIVER_SPACE + "." + wiki.generateRandomString(16);
       }
     }
     return docName;
   }
   
   public boolean unsubscribeNewsletter(XWikiContext context) throws XWikiException{
     boolean unsubscribed = false;
     String email = context.getRequest().get("emailadresse");
     email = email.toLowerCase();
     XWikiDocument blogDoc = getBlogDoc(null, context);
     String subsDocName = getSubscriberDoc(email, context);
     if(!"".equals(subsDocName) && context.getWiki().exists(subsDocName, context)) {
       XWikiDocument subscribedDoc = context.getWiki().getDocument(subsDocName, context);
       BaseObject subsObj = subscribedDoc.getObject("Celements.NewsletterReceiverClass", 
           "subscribed", blogDoc.getFullName(), false);
       if(subsObj != null) {
         subsObj.setIntValue("isactive", 0);
         context.getWiki().saveDocument(subscribedDoc, context);
         //TODO send email to notice unsubscription - necessary?
         unsubscribed = true;
       }
     }
     BaseObject obj = blogDoc.getObject("Celements2.ReceiverEMail", "email", email, false);
     if((obj != null) && (email != "")){
       obj.setIntValue("is_active", 0);
       context.getWiki().saveDocument(blogDoc, context);
       //TODO send email to notice unsubscription - necessary?
       unsubscribed = true;
     }
     
     return unsubscribed;
   }
   
   public String getSubscriberDoc(String email, XWikiContext context) 
       throws XWikiException {
     email = email.toLowerCase();
     String hql = "select distinct doc.fullName " +
         "from XWikiDocument as doc, BaseObject as obj, " +
         "Celements.NewsletterReceiverClass as nr " +
         "where doc.fullName=obj.name " +
         "and obj.id=nr.id " +
         "and nr.email='" + email + "'";
     List<String> docs = context.getWiki().search(hql, context);
     String docName = "";
     if((docs != null) && (docs.size() > 0)) {
       docName = docs.get(0);
     }
     return docName;
   }
   
   public boolean activateSubscriber(XWikiContext context) throws XWikiException{
     boolean activated = false;
     String email = context.getRequest().get("emailadresse");
     email = email.toLowerCase();
     String activationKey = context.getRequest().get("ak");
     String subscriberDocName = getSubscriberDoc(email, context);
     if(!"".equals(subscriberDocName.trim()) && context.getWiki().exists(subscriberDocName,
         context)) {
       XWikiDocument subscriberDoc = context.getWiki().getDocument(subscriberDocName, 
           context);
       List<BaseObject> subscriptions = subscriberDoc.getObjects(
           "Celements.NewsletterReceiverClass");
       if(subscriptions != null) {
         for (BaseObject subscription : subscriptions) {
           if((subscription != null) && (subscription.getIntValue("isactive") != 1) 
               && activationKey.equals(getActivationKey(subscription, 
               subscriberDoc.getFullName()))) {
             subscription.setIntValue("isactive", 1);
             context.getWiki().saveDocument(subscriberDoc, context);
             activated = true;
           }
         }
       }
     }
     return activated;
   }
   
   public XWikiDocument getBlogDoc(Map<String, String> request, XWikiContext context) 
       throws XWikiException {
     XWikiDocument doc = context.getDoc();
     if((doc.getObject("Celements2.BlogConfigClass") == null) && (doc.getObject(
         "XWiki.ArticleClass") != null)){
       doc = getBlogPageByBlogSpace(doc.getSpace(), context);
     }
     if(request != null) {
       String subscribeBlog = request.get("subsBlog");
       if((subscribeBlog != null) && !"".equals(subscribeBlog.trim()) && 
           context.getWiki().exists(subscribeBlog, context)) {
         doc = context.getWiki().getDocument(subscribeBlog, context);
       }
     }
     LOGGER.info("BlogDoc is " + doc.getFullName());
     return doc;
   }
 
   /**
    * Get the previous or next article in the blog. Does not take into account subscribed 
    * blogs or archived articles.
    * @param article
    * @param next true gets the next, false the previous article
    * @return
    */
   public Article getNeighbourArticle(Article article, boolean next, 
       XWikiContext context) {
     String aSpace = article.getDocName().substring(0, article.getDocName().indexOf('.'));
     List<Article> articles = null;
     try {
       articles = getBlogArticles(aSpace, "", context.getLanguage(), false, 
           false, false, false, true, true, true, false, true, true, context);
     } catch (XWikiException e) {
       LOGGER.error("could not get articles for blog");
     }
     Article nArticle = null;
     if(articles != null) {
       for (int i = 0; i < articles.size(); i++) {
         Article tmpArt = articles.get(i);
         if(article.getDocName().equals(tmpArt.getDocName())) {
           if(next) {
             if((i+1) == articles.size()) {
               nArticle = articles.get(0);
             } else {
               nArticle = articles.get(i+1);
             }
           } else {
             if(i == 0) {
               nArticle = articles.get(articles.size() - 1);
             } else {
               nArticle = articles.get(i-1);
             }
           }
         }
       }
     }
     return nArticle;
   }
 
   private IBlogServiceRole getBlogService() {
     if (injected_BlogService != null) {
       return injected_BlogService; 
     }
     return Utils.getComponent(IBlogServiceRole.class);
   }
 
 }
