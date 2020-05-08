 package com.celements.blog.service;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.VelocityContext;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 
 import com.celements.web.plugin.cmd.AttachmentURLCommand;
 import com.celements.web.service.IWebUtilsService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Attachment;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 @Component
 public class NewsletterAttachmentService implements INewsletterAttachmentServiceRole {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       NewsletterAttachmentService.class);
   
   @Requirement
   Execution execution;
   
   @Requirement
   IWebUtilsService webUtils;
 
   public String embedImagesInContent(String content) {
    Pattern pattern = Pattern.compile("<img .*?>");
     Matcher matcher = pattern.matcher(content);
     Set<String> images = new HashSet<String>();
     while (matcher.find()) {
       images.add(matcher.group());
     }
     return embedImagesInContent(content, images);
   }
   
   String embedImagesInContent(String content, Set<String> imgTags) {
     for (String tag : imgTags) {
       String url = tag.replaceAll(".*src=\"/?(download/)?(.*?)\\?.*?\".*", "$2");
       String imgFullname = url.replaceAll("^(.*)/(.*)/(.*)$", "$1.$2;$3");
       String replStr = Pattern.quote(tag.replaceAll(".*src=\"(.*?)\".*", "$1"));
       content = content.replaceAll(replStr, getImageURL(imgFullname, true));
     }
     return content;
   }
   
   public String getImageURL(String imgFullname, boolean embedImage) {
     String imgURL = "";
     AttachmentURLCommand attURL = new AttachmentURLCommand();
     if(embedImage) {
       extendAttachmentList(getAttachmentForFullname(imgFullname), "nlEmbedAttList");
       imgURL = "cid:" + attURL.getAttachmentName(imgFullname);
     } else {
       imgURL = attURL.getAttachmentURL(imgFullname, "download", getContext());
     }
     return imgURL;
   }
 
   public void addAttachment(String attFullname) {
     Attachment att = getAttachmentForFullname(attFullname);
     extendAttachmentList(att, "nlEmbedAttList");
     extendAttachmentList(att, "nlEmbedNoImgAttList");
   }
   
   public List<Attachment> getAttachmentList(boolean includingImages) {
     String param = "nlEmbedAttList";
     if(!includingImages) {
       param = "nlEmbedNoImgAttList";
     }
     return getAttachmentList(param, false);
   }
   
   @SuppressWarnings("unchecked")
   List<Attachment> getAttachmentList(String param, boolean create) {
     Object contextVal = getVcontext().get(param);
     List<Attachment> embedList = null;
     if((contextVal instanceof List<?>) && !((List<Attachment>)contextVal).isEmpty() 
         && (((List<Attachment>)contextVal).get(0) instanceof Attachment)){
       embedList = (List<Attachment>)contextVal;
     }
     if((embedList == null) && create) {
       embedList = new ArrayList<Attachment>();
     }
     return embedList;
   }
   
   void extendAttachmentList(Attachment att, String param) {
     List<Attachment> attList = getAttachmentList(param, true);
     attList.add(att);
     getVcontext().put(param, attList);
   }
   
   Attachment getAttachmentForFullname(String imgFullname) {
     AttachmentURLCommand attURL = new AttachmentURLCommand();
     Attachment att = null;
     try {
       XWikiDocument attDoc = getContext().getWiki().getDocument(
           webUtils.resolveDocumentReference(attURL.getPageFullName(imgFullname)), 
           getContext());
       att = (new Document(attDoc, getContext())).getAttachment(
           attURL.getAttachmentName(imgFullname));
     } catch (XWikiException xwe) {
       LOGGER.error("Exception getting attachment Document.", xwe);
     }
     return att;
   }
   
   XWikiContext getContext() {
     return (XWikiContext)execution.getContext().getProperty("xwikicontext");
   }
   
   VelocityContext getVcontext() {
     return (VelocityContext)(getContext().get("vcontext"));
   }
 }
