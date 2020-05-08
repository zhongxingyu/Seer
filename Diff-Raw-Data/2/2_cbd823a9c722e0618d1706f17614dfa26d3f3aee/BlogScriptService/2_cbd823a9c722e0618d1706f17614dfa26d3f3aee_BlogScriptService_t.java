 package com.celements.blog.service;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.script.service.ScriptService;
 
 import com.celements.blog.plugin.NewsletterReceivers;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 @Component("celblog")
 public class BlogScriptService implements ScriptService {
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(
       BlogScriptService.class);
 
   @Requirement
  IBlogServiceRole blogService;
 
   @Requirement
   Execution execution;
   
   private XWikiContext getContext() {
     return (XWikiContext)execution.getContext().getProperty("xwikicontext");
   }
 
   public List<String> getAddresses(DocumentReference blogDocRef) {
     if (getContext().getWiki().getRightService().hasAdminRights(getContext())) {
       try {
         NewsletterReceivers newsletterReceivers = new NewsletterReceivers(
             getContext().getWiki().getDocument(blogDocRef, getContext()), getContext());
         return newsletterReceivers.getAddresses();
       } catch (XWikiException exp) {
         LOGGER.error("Failed to get Blog document for [" + blogDocRef + "].", exp);
       }
     } else {
       LOGGER.info("getAddresses failed because user [" + getContext().getUser()
           + "] has no admin rights.");
     }
     return Collections.emptyList();
   }
 
   public DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName) {
     XWikiDocument blogPageByBlogSpace = blogService.getBlogPageByBlogSpace(blogSpaceName); 
     if (blogPageByBlogSpace != null) {
       return blogPageByBlogSpace.getDocumentReference();
     }
     return null;
   }
 
 }
