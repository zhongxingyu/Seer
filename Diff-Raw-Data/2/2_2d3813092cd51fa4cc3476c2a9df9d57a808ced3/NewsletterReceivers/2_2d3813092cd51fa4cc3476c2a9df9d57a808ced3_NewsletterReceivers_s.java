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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.VelocityContext;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 
 import com.celements.blog.service.INewsletterAttachmentServiceRole;
 import com.celements.common.classes.IClassCollectionRole;
 import com.celements.rendering.RenderCommand;
 import com.celements.web.plugin.cmd.CelSendMail;
 import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
 import com.celements.web.service.IWebUtilsService;
 import com.celements.web.utils.WebUtils;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.web.Utils;
 import com.xpn.xwiki.web.XWikiMessageTool;
 import com.xpn.xwiki.web.XWikiRequest;
 
 public class NewsletterReceivers {
   
   private static Log LOGGER = LogFactory.getFactory().getInstance(
       NewsletterReceivers.class);
   private UserNameForUserDataCommand userNameForUserDataCmd =
       new UserNameForUserDataCommand();
   private RenderCommand renderCommand = new RenderCommand();
 
   private List<String> allAddresses = new ArrayList<String>();
   private List<String[]> groups = new ArrayList<String[]>();
   private List<String[]> groupUsers = new ArrayList<String[]>();
   private List<String[]> users = new ArrayList<String[]>();
   private List<String[]> addrLangs = new ArrayList<String[]>(); 
   private List<String> addresses = new ArrayList<String>();
   private List<EmailAddressDate> emailAddressDateList = new ArrayList<EmailAddressDate>();
 
   @Deprecated
   public NewsletterReceivers(XWikiDocument blogDoc, XWikiContext context
       ) throws XWikiException{
     addReceiverEMail(blogDoc);
     addNewsletterReceiver(blogDoc);
   }
 
   //TODO ADD UNIT TESTS!!!
   public NewsletterReceivers(XWikiDocument blogDoc) throws XWikiException{
     addReceiverEMail(blogDoc);
     addNewsletterReceiver(blogDoc);
   }
 
   void addNewsletterReceiver(XWikiDocument blogDoc) throws XWikiException {
     String xwql = "from doc.object(Celements.NewsletterReceiverClass) as nr "
         + "where nr.isactive = '1' and nr.subscribed = :subscribed";
 //    String hql = "select nr.email,doc.date from Celements.NewsletterReceiverClass as nr, "
 //        + " XWikiDocument as doc "
 //        + "where doc.fullName = "
 //        + " nr.isactive='1' "
 //        + "and subscribed='" + blogDoc.getFullName() + "'";
 //    List<String> nlRegAddresses = context.getWiki().search(hql, context);
     DocumentReference receverClassRef = getBlogClasses().getNewsletterReceiverClassRef(
         getContext().getDatabase());
     try {
       List<String> nlRegReceiverList = Utils.getComponent(QueryManager.class).createQuery(
           xwql, Query.XWQL).bindValue("subscribed", blogDoc.getFullName()).execute();
       if(nlRegReceiverList != null) {
         LOGGER.info("Found " + nlRegReceiverList.size()
             + " Celements.NewsletterReceiverClass" + " object-subscriptions for blog "
             + blogDoc.getFullName());
         String blogSpace = blogDoc.getXObject(getBlogClasses().getBlogConfigClassRef(
             getContext().getDatabase())).getStringValue("blogspace");
         for (String nlRegReceiverFN : nlRegReceiverList) {
           XWikiDocument receiverDoc = getContext().getWiki().getDocument(nlRegReceiverFN,
               getContext());
           List<BaseObject> recieverObjs = receiverDoc.getXObjects(receverClassRef);
           for(BaseObject receiverObj : recieverObjs) {
             String address = receiverObj.getStringValue("email");
             address = address.toLowerCase();
             if(!allAddresses.contains(address)) {
               String language = receiverObj.getStringValue("language");
               String firstname = "";
               String name = "";
               BaseObject contactObj = receiverDoc.getXObject(new DocumentReference(
                   getContext().getWiki().getDatabase(), "Celements", "ContactClass"));
               if (contactObj != null) {
                 firstname = contactObj.getStringValue("firstname");
                 name = contactObj.getStringValue("lastname");
               }
               if (getWebUtilsService().getAllowedLanguages(blogSpace).contains(language)) {
                 addrLangs.add(new String[]{"XWiki.XWikiGuest", address, language, firstname, name});
               } else {
                 addresses.add(address);
               }
               allAddresses.add(address);
               emailAddressDateList.add(new EmailAddressDate(address, receiverDoc.getDate()));
               LOGGER.info("reveiver added: " + address);
             }
           }
         }
       }
     } catch (QueryException exp) {
       LOGGER.error("Failed to execute newsletter receiver xwql [" + xwql + "].", exp);
     }
   }
 
   void addReceiverEMail(XWikiDocument blogDoc) throws XWikiException {
     List<BaseObject> objs = blogDoc.getXObjects(getBlogClasses().getReceiverEMailClassRef(
         getContext().getDatabase()));
     LOGGER.debug("objs.size = " + (objs != null?objs.size():0));
     if(objs != null){
       for (BaseObject obj : objs) {
         LOGGER.debug("obj: " + obj);
         if(obj != null){
           String receiverAdr = obj.getStringValue("email");
           String address = receiverAdr.toLowerCase();
           boolean active = (obj.getIntValue("is_active") == 1);
           boolean isMail = address.matches(
               "[\\w\\.]{1,}[@][\\w\\-\\.]{1,}([.]([\\w\\-\\.]{1,})){1,3}$");
           String type = obj.getStringValue("address_type");
           if(isMail && active && (!allAddresses.contains(address))) {
             addresses.add(address);
             allAddresses.add(address);
             emailAddressDateList.add(new EmailAddressDate(address, blogDoc.getDate()));
             LOGGER.info("reveiver added: " + address);
           } else {
             if(getContext().getWiki().exists(receiverAdr, getContext())){
               parseDocument(receiverAdr, type, getContext());
             }
           }
         }
       }
     }
   }
   
   private BlogClasses getBlogClasses() {
     return (BlogClasses)Utils.getComponent(IClassCollectionRole.class,
         "celements.celBlogClasses");
   }
 
   private void parseDocument(String address, String type, XWikiContext context
       ) throws XWikiException {
     XWikiDocument recDoc = context.getWiki().getDocument(address, context);
     BaseObject userObj = recDoc.getObject("XWiki.XWikiUsers");
     List<BaseObject> groupObjs = recDoc.getObjects("XWiki.XWikiGroups");
     if(userObj != null){
       String email = userObj.getStringValue("email").toLowerCase();
       String language = userObj.getStringValue("admin_language");
       String firstname = userObj.getStringValue("first_name");
       String name = userObj.getStringValue("last_name");
       if((email.trim().length() > 0) && (!allAddresses.contains(email))){
         users.add(new String[]{recDoc.getFullName(), email, language, firstname, name});
         allAddresses.add(email);
       }
     } else if((groupObjs != null) && (groupObjs.size() > 0)){
       int usersInGroup = parseGroupMembers(groupObjs, type, context);
       groups.add(new String[]{recDoc.getFullName(), Integer.toString(usersInGroup)});
     }
   }
 
   private int parseGroupMembers(List<BaseObject> groupObjs, String type,
       XWikiContext context) throws XWikiException {
     int usersInGroup = 0;
     for (BaseObject groupObj : groupObjs) {
       if ((groupObj != null) && (groupObj.getStringValue("member") != null)) {
         String userDocName = groupObj.getStringValue("member");
         if((userDocName.trim().length() > 0) && context.getWiki().exists(userDocName,
             context)){
           XWikiDocument userDoc = context.getWiki().getDocument(userDocName, context);
           BaseObject groupUserObj = userDoc.getObject("XWiki.XWikiUsers");
           if(groupUserObj != null){
             String email = groupUserObj.getStringValue("email").toLowerCase();
             String language = groupUserObj.getStringValue("admin_language");
             String firstname = groupUserObj.getStringValue("first_name");
             String name = groupUserObj.getStringValue("last_name");
             if((email.trim().length() > 0) && (!allAddresses.contains(email))){
               usersInGroup++;
               allAddresses.add(email);
               groupUsers.add(new String[]{userDocName, email, language, firstname, name});
             }
           }
         }
       }
     }
     return usersInGroup;
   }
   
   public List<String[]> sendArticleByMail(XWikiContext context) throws XWikiException {
     XWikiRequest request = context.getRequest();
     String articleName = request.get("sendarticle");
     String from = request.get("from");
     String replyTo = request.get("reply_to");
     String subject = request.get("subject");
     String testSend = request.get("testSend");
     
     boolean isTest = false;
     if((testSend != null) && testSend.equals("1")){
       isTest = true;
     }
     
     XWiki wiki = context.getWiki();
     List<String[]> result = new ArrayList<String[]>();
     int successfullySent = 0;
 
     LOGGER.debug("articleName = " + articleName);
     LOGGER.debug("article exists = " + wiki.exists(articleName, context));
     if((articleName != null) && (!"".equals(articleName.trim()))
         && (wiki.exists(articleName, context))){
       XWikiDocument doc = wiki.getDocument(articleName, context);
       String baseURL = doc.getExternalURL("view", context);
 
       List<String[]> allUserMailPairs = null;
       LOGGER.debug("is test send: " + isTest);
       if(isTest){
         String user = context.getUser();
         XWikiDocument userDoc = context.getWiki().getDocument(user, context);
         BaseObject userObj = userDoc.getObject("XWiki.XWikiUsers");
         if(userObj != null){
           String email = userObj.getStringValue("email");
           if(email.trim().length() > 0){
             allUserMailPairs = new ArrayList<String[]>();
             String[] userFields = getUserAdminLanguage(user, getWebUtilsService(
                 ).getDefaultLanguage());
             allUserMailPairs.add((String[])ArrayUtils.addAll(new String[]{user, email }, 
                 userFields));
           }
         }
       } else {
         allUserMailPairs = getNewsletterReceiversList();
       }
       
       String origUser = context.getUser();
       String origLanguage = context.getLanguage();
       VelocityContext vcontext = (VelocityContext) context.get("vcontext");
       Object origAdminLanguage = vcontext.get("admin_language");
       Object origMsgTool = vcontext.get("msg");
       Object origAdminMsgTool = vcontext.get("adminMsg");
       for (String[] userMailPair : allUserMailPairs) {
         LOGGER.debug("userMailPair: " + ArrayUtils.toString(userMailPair));
         context.setUser(userMailPair[0]);
         String language = userMailPair[2];
         context.setLanguage(language);
         vcontext.put("firstname", userMailPair[3]);
         vcontext.put("name", userMailPair[4]);
         vcontext.put("email", userMailPair[1]);
         vcontext.put("language", language);
         vcontext.put("admin_language", language);
         XWikiMessageTool msgTool = WebUtils.getInstance().getMessageTool(language,
             getContext());
         vcontext.put("msg", msgTool);
         vcontext.put("adminMsg", msgTool);
 
         if(wiki.checkAccess("view", doc, context)){
           String senderContextLang = context.getLanguage();
           context.setLanguage(language);
           String htmlContent = getHtmlContent(doc, baseURL, context);
           htmlContent += getUnsubscribeFooter(userMailPair[1], doc, context);
           context.setLanguage(senderContextLang);
           
           String textContent = context.getMessageTool().get(
               "cel_newsletter_text_only_message", Arrays.asList(
                   "_NEWSLETTEREMAILADRESSKEY_"));
           textContent = textContent.replaceAll("_NEWSLETTEREMAILADRESSKEY_",
               doc.getExternalURL("view", context));
           textContent += getUnsubscribeFooter(userMailPair[1], doc, context);
           
           int singleResult = sendMail(from, replyTo, userMailPair[1], subject,
               baseURL, htmlContent, textContent, context);
           result.add(new String[]{userMailPair[1], Integer.toString(singleResult)});
           if(singleResult == 0){ successfullySent++; }
         } else {
           LOGGER.warn("Tried to send " + doc + " to user " + userMailPair[0] + " which"
               + " has no view rights on this Document.");
           List<String> params = new ArrayList<String>();
           params.add(doc.toString());
           result.add(new String[]{userMailPair[1], context.getMessageTool(
               ).get("cel_blog_newsletter_receiver_no_rights", params)});
         }
       
       }
       context.setUser(origUser);
       context.setLanguage(origLanguage);
       vcontext.put("language", origLanguage);
       vcontext.put("admin_language", origAdminLanguage);
       vcontext.put("msg", origMsgTool);
       vcontext.put("adminMsg", origAdminMsgTool);
       
       setNewsletterSentObject(doc, from, replyTo, subject, successfullySent, isTest,
           context);
     }
     
     return result;
   }
 
   List<String[]> getNewsletterReceiversList() {
     ArrayList<String[]> allUserMailPairs = new ArrayList<String[]>();
     allUserMailPairs.addAll(groupUsers);
     allUserMailPairs.addAll(users);
     allUserMailPairs.addAll(addrLangs);
     String defaultLanguage = getWebUtilsService().getDefaultLanguage();
     for (String address : addresses) {
       String mailUser = "XWiki.XWikiGuest";
       String[] userFields = new String[] { defaultLanguage, "", "" };
       String addrUser = null;
       try {
         addrUser = userNameForUserDataCmd.getUsernameForUserData(address, "email",
             getContext());
       } catch (XWikiException e) {
         LOGGER.error("Exception getting username for user email '" + address + "'.", e);
       }
       if((addrUser != null) && (addrUser.length() > 0)) {
         mailUser = addrUser;
         userFields = getUserAdminLanguage(mailUser, defaultLanguage);
       }
       allUserMailPairs.add((String[])ArrayUtils.addAll(new String[] { mailUser, address },
           userFields));
     }
     return allUserMailPairs;
   }
 
   private String[] getUserAdminLanguage(String mailUser, String defaultLanguage) {
     String userLanguage = defaultLanguage;
     String firstname = "";
     String name = "";
     try {
       DocumentReference userDocRef = getWebUtilsService().resolveDocumentReference(
           mailUser);
       XWikiDocument mailUserDoc = getContext().getWiki().getDocument(userDocRef,
           getContext());
       BaseObject mailUserObj = mailUserDoc.getXObject(new DocumentReference(
           userDocRef.getWikiReference().getName(), "XWiki", "XWikiUsers"));
       String userAdminLanguage = mailUserObj.getStringValue("admin_language");
       if ((userAdminLanguage != null) && !"".equals(userAdminLanguage)) {
         userLanguage = userAdminLanguage;
       }
       firstname = mailUserObj.getStringValue("first_name");
       name = mailUserObj.getStringValue("last_name");
     } catch (XWikiException exp) {
       LOGGER.error("Exception getting userdoc to find admin-language ['" + mailUser
           + "]'.", exp);
     }
     return new String[] { userLanguage, firstname, name };
   }
 
   private String getUnsubscribeFooter(String emailAddress,
       XWikiDocument blogDocument, XWikiContext context) throws XWikiException {
     String unsubscribeFooter = "";
     if(!"".equals(getUnsubscribeLink(blogDocument.getSpace(), emailAddress,
         context))) {
       unsubscribeFooter = context.getMessageTool().get(
           "cel_newsletter_unsubscribe_footer", Arrays.asList("_NEWSLETTEREMAILADRESSKEY_"
               )); 
       unsubscribeFooter = unsubscribeFooter.replaceAll(
           "_NEWSLETTEREMAILADRESSKEY_", getUnsubscribeLink(
               blogDocument.getSpace(), emailAddress, context));
     }
     return unsubscribeFooter;
   }
 
   private String getUnsubscribeLink(String blogSpace, String emailAddresse,
       XWikiContext context) throws XWikiException {
     String unsubscribeLink = "";
     XWikiDocument blogDocument = BlogUtils.getInstance().getBlogPageByBlogSpace(
         blogSpace, context);
     BaseObject blogObj = blogDocument.getObject("Celements2.BlogConfigClass",
         false, context);
     if ((blogObj != null) && (blogObj.getIntValue("unsubscribe_info") == 1)) {
       unsubscribeLink = blogDocument.getExternalURL("view",
           "xpage=celements_ajax&ajax_mode=BlogAjax&doaction=unsubscribe"
         + "&emailadresse=" + emailAddresse, context);
     }
     return unsubscribeLink;
   }
 
   private String getHtmlContent(XWikiDocument doc, String baseURL, XWikiContext context
       ) throws XWikiException {
     String header = "";
     if((baseURL != null) && !"".equals(baseURL.trim())){
       header = "<base href='" + baseURL + "' />\n";
     }
     DocumentReference headerRef = getWebUtilsService().resolveDocumentReference(
         "LocalMacros.NewsletterHTMLheader");
     if(getContext().getWiki().exists(headerRef, context)) {
       LOGGER.debug("Additional header found.");
       LOGGER.debug("doc=" + doc + ", context.language=" + context.getLanguage());
       LOGGER.debug("context=" + context);
       header += renderCommand.renderDocument(headerRef, "view");
       LOGGER.debug("Additional header rendered.");
     } else {
       LOGGER.debug("No additional header. Doc does not exist: " + headerRef);
     }
     renderCommand.setDefaultPageType("RichText");
     String content = renderCommand.renderCelementsDocument(doc.getDocumentReference(),
        "view");
     content = Utils.replacePlaceholders(content, context);
     if(getContext().getWiki().getXWikiPreferenceAsInt("newsletter_embed_all_images", 
         "celements.newsletter.embedAllImages", 0, getContext()) == 1) {
       content = getNewsletterAttachmentService().embedImagesInContent(content);
     }
     String footer = "";
     DocumentReference footerRef = getWebUtilsService().resolveDocumentReference(
         "LocalMacros.NewsletterHTMLfooter");
     if(getContext().getWiki().exists(footerRef, context)) {
       LOGGER.debug("Additional footer found.");
       LOGGER.debug("doc=" + doc + ", context.language=" + context.getLanguage());
       LOGGER.debug("context=" + context);
       footer += renderCommand.renderDocument(footerRef, "view") + "\n";
       LOGGER.debug("Additional footer rendered.");
     } else {
       LOGGER.debug("No additional footer. Doc does not exist: " + footerRef);
     }
     footer += context.getMessageTool().get("cel_newsletter_html_footer_message",
         Arrays.asList(doc.getExternalURL("view", context)));
     LOGGER.debug("Header: [" + header + "]");
     LOGGER.debug("Footer: [" + footer + "]");
     return header + content + footer;
   }
 
   private int sendMail(String from, String replyTo, String to, String subject,
       String baseURL, String htmlContent, String textContent, XWikiContext context
       ) throws XWikiException {
     try {
       if((to != null) && (to.trim().length() == 0)){ to = null; }
       Map<String, String> otherHeader = new HashMap<String, String>();
       otherHeader.put("Content-Location", baseURL);
       
       CelSendMail sender = new CelSendMail(getContext());
       sender.setFrom(from);
       sender.setReplyTo(replyTo);
       sender.setTo(to);
       sender.setSubject(subject);
       sender.setHtmlContent(htmlContent, false);
       sender.setTextContent(textContent);
       sender.setOthers(otherHeader);
       sender.setAttachments(getNewsletterAttachmentService().getAttachmentList(true));
       return sender.sendMail();
     } finally {
       getNewsletterAttachmentService().clearAttachmentList();
     }
   }
 
   private void setNewsletterSentObject(XWikiDocument doc, String from, String replyTo,
       String subject, int nrOfSent, boolean isTest, XWikiContext context
       ) throws XWikiException {
     BaseObject configObj = doc.getObject("Classes.NewsletterConfigClass");
     if(configObj == null){
       configObj = doc.newObject("Classes.NewsletterConfigClass", context);
     }
     
     configObj.set("from_address", from, context);
     configObj.set("reply_to_address", replyTo, context);
     configObj.set("subject", subject, context);
 
     if((nrOfSent > 0) && !isTest){
       setNewsletterHistory(configObj, nrOfSent, context);
     }
     
     context.getWiki().saveDocument(doc, context);
   }
   
   private void setNewsletterHistory(BaseObject configObj, int nrOfSent,
       XWikiContext context) {
     int timesSent = configObj.getIntValue("times_sent");
     configObj.set("times_sent", timesSent + 1, context);
     configObj.set("last_sent_date", new Date(), context);
     configObj.set("last_sender", context.getUser(), context);
     configObj.set("last_sent_recipients", nrOfSent, context);
   }
   
   public boolean hasReceivers(){
     return getAllAddresses().size() > 0;
   }
   
   public boolean hasReceiverGroups(){
     return getGroups().size() > 0;
   }
   
   public boolean hasSingleReceivers(){
     return (getUsers().size() > 0) || (getAddresses().size() > 0);
   }
   
   public boolean hasUsers(){
     return getUsers().size() > 0;
   }
   
   public boolean hasAdresses(){
     return getAddresses().size() > 0;
   }
   
   public List<String> getAllAddresses() {
     return allAddresses;
   }
 
   public List<String[]> getGroups() {
     return groups;
   }
 
   public List<String[]> getUsers() {
     return users;
   }
 
   public List<String> getAddresses() {
     return addresses;
   }
 
   public List<EmailAddressDate> getAddressesOrderByDate() {
     return emailAddressDateList;
   }
 
   public int getNrOfReceivers(){
     return allAddresses.size();
   }
 
   private IWebUtilsService getWebUtilsService() {
     return Utils.getComponent(IWebUtilsService.class);
   }
 
   private XWikiContext getContext() {
     return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
         "xwikicontext");
   }
 
   private INewsletterAttachmentServiceRole getNewsletterAttachmentService() {
     return (INewsletterAttachmentServiceRole)Utils.getComponent(
           INewsletterAttachmentServiceRole.class);
   }
   
 }
