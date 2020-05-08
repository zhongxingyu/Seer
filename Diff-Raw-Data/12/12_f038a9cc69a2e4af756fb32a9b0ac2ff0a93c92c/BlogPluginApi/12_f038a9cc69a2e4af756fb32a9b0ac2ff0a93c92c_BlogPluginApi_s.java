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
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.blog.service.INewsletterAttachmentServiceRole;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Api;
 import com.xpn.xwiki.api.Attachment;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.web.Utils;
 
 public class BlogPluginApi extends Api {
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(BlogPlugin.class);
 
   private BlogPlugin plugin;
 
   public BlogPluginApi(BlogPlugin plugin, XWikiContext context) {
     super(context);
     setPlugin(plugin);
   }
 
   public void setPlugin(BlogPlugin plugin) {
     this.plugin = plugin;
   }
 
   public BlogPlugin getPlugin(){
     return plugin;
   }
 
   /**
    * @deprecated since 1.9 blog-web release - instead use getBlogDocRefByBlogSpace
    *             in celBlog script service
    */
   @Deprecated
   public Document getBlogPageByBlogSpace(String blogSpaceName) throws XWikiException {
     XWikiDocument blogPageByBlogSpace = plugin.getBlogPageByBlogSpace(blogSpaceName,
         context);
     if (blogPageByBlogSpace != null) {
       return blogPageByBlogSpace.newDocument(context);
     }
     return null;
   }
 
   public Article getArticle(Document doc) throws XWikiException {
     Article article = null;
     try{
       article = new Article(doc, context);
     } catch (EmptyArticleException e) {
       LOGGER.info(e);
     }
     return article;
   }
   
   /**
    * Get all articles. Including not yet published, archived and
    * @param blogArticleSpace
    * @param subscribedBlogs
    * @param language
    * @param context
    * @return
    * @throws XWikiException
    */
   public List<Article> getAllArticles(String blogArticleSpace, String subscribedBlogs, 
       String language) throws XWikiException{
     LOGGER.info("ENTER getAllArticles");
     List<Article> result = plugin.getBlogArticles(blogArticleSpace, subscribedBlogs, 
         language, false, false, false, true, true, true, true, true, true, false, 
         context);
     LOGGER.info("END getAllArticles");
     return result;
   }
   
   public List<Article> getAllWithRightsArticles(String blogArticleSpace, 
       String subscribedBlogs, String language) throws XWikiException{
     LOGGER.info("ENTER getAllWithRightsArticles");
     List<Article> result = plugin.getBlogArticles(blogArticleSpace, subscribedBlogs, 
         language, false, false, false, true, true, true, true, true, true, true, context);
     LOGGER.info("END getAllWithRightsArticles");
     return result;
   }
   
   public List<Article> getArticles(String blogArticleSpace, String subscribedBlogs, 
       String language) throws XWikiException{
     LOGGER.info("ENTER getArticles");
     List<Article> result = plugin.getBlogArticles(blogArticleSpace, subscribedBlogs, 
         language, false, false, false, false, true, true, true, false, true, true, 
         context);
     LOGGER.info("END getArticles");
     return result;
   }
   
   public List<Article> getArchivedArticles(String blogArticleSpace, 
       String subscribedBlogs, String language) throws XWikiException{
     LOGGER.info("ENTER getArchivedArticles");
     List<Article> result = plugin.getBlogArticles(blogArticleSpace, subscribedBlogs, 
         language, true, false, false, true, true, true, true, false, true, true, 
         context);
     LOGGER.info("END getArchivedArticles");
     return result;
   }
   
   public List<Article> getAllFromSubscribedBlogs(String blogArticleSpace, 
       String subscribedBlogs, String language) throws XWikiException{
     LOGGER.info("ENTER getAllFromSubscribedBlogs");
     List<Article> result = plugin.getBlogArticles(blogArticleSpace, subscribedBlogs, 
         language, false, false, true, true, true, true, true, true, true, true, context);
     LOGGER.info("END getAllFromSubscribedBlogs");
     return result;
   }
   
   public List<Article> getAllNewSubscribable(String blogArticleSpace, String 
       subscribedBlogs, String language) throws XWikiException{
     LOGGER.info("ENTER getAllNewSubscribable");
     List<Article> result = plugin.getBlogArticles(blogArticleSpace, subscribedBlogs, 
         language, false, false, true, true, true, true, false, false, true, true, 
         context);
     LOGGER.info("END getAllNewSubscribable");
     return result;
   }
   
   public int containsSubscribableArticles(List<Article> articles, 
       String blogArticleSpace){
     int subsArts = 0;
     for (Iterator<Article> iterator = articles.iterator(); iterator.hasNext();) {
       if(iterator.next().isFromSubscribableBlog(blogArticleSpace)){
         subsArts++;
       }
     }
     return subsArts;
   }
   
   public int containsUndecidedArticles(List<Article> articles, String blogArticleSpace) {
     int unSubsArts = 0;
     for (Iterator<Article> iterator = articles.iterator(); iterator.hasNext();) {
       Article article = iterator.next();
       if(article.isFromSubscribableBlog(blogArticleSpace) 
           && (article.isSubscribed() == null)){
         unSubsArts++;
       }
     }
     return unSubsArts;
   }
   
   public Map<String, String> batchImportReceivers(boolean asInactive) {
     String importData = context.getRequest().get("batchImportData");
     String newsletterName = context.getRequest().get("subsBlog");
 //    Removed since importing is not dangerous, sending a mail to all receivers is.
 //    if(hasProgrammingRights()) {
       return plugin.batchImportReceivers(asInactive, importData, newsletterName, context);
 //    }
 //    return null;
   }
   
   public String subscribeNewsletter() throws XWikiException{
     return plugin.subscribeNewsletter(false, context);
   }
   
   public boolean unsubscribeNewsletter() throws XWikiException{
     return plugin.unsubscribeNewsletter(context);
   }
   
   public boolean activateSubscriber() throws XWikiException{
     return plugin.activateSubscriber(context);
   }
   
   //TODO make singleton (caching needed)
   public NewsletterReceivers getNewsletterReceivers() throws XWikiException{
     return new NewsletterReceivers(plugin.getBlogDoc(null, context), context);
   }
   
   public List<String[]> sendArticleByMail() throws XWikiException {
     return getNewsletterReceivers().sendArticleByMail(context);
   }
   
   public List<String[]> sendNewsletterToInjectedReceiverList(
       List<DocumentReference> receivers, String from, String replyTo, String subject, 
       DocumentReference contentDocRef, String baseURL) {
     try {
       XWikiDocument contentDoc = context.getWiki().getDocument(contentDocRef, context);
      return getNewsletterReceivers().sendNewsletterToInjectedReceiverList(receivers, from, 
          replyTo, subject, contentDoc, baseURL);
     } catch (XWikiException xwe) {
       LOGGER.error("Exception sending Newsletter to injected list", xwe);
     }
     return Collections.emptyList();
   }
   
   public Article getPreviousArticle(Article article) {
     return plugin.getNeighbourArticle(article, false, context);
   }
   
   public Article getNextArticle(Article article) {
     return plugin.getNeighbourArticle(article, true, context);
   }
   
   public String getImageURL(String imgFullname, boolean embedImage) {
     return ((INewsletterAttachmentServiceRole)Utils.getComponent(
         INewsletterAttachmentServiceRole.class)).getImageURL(imgFullname, embedImage);
   }
   
   void addAttachment(String attFullname) {
     ((INewsletterAttachmentServiceRole)Utils.getComponent(
         INewsletterAttachmentServiceRole.class)).addAttachment(attFullname);
   }
   
   List<Attachment> getAttachmentList(boolean includeImages) {
     return ((INewsletterAttachmentServiceRole)Utils.getComponent(
         INewsletterAttachmentServiceRole.class)).getAttachmentList(includeImages);
   }
 
 }
