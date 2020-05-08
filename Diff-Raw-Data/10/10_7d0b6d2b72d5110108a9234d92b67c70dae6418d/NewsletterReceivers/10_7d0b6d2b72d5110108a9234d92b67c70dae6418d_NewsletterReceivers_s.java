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
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.celements.web.pagetype.IPageType;
 import com.celements.web.plugin.api.CelementsWebPluginApi;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.web.Utils;
 import com.xpn.xwiki.web.XWikiRequest;
 
 public class NewsletterReceivers {
   
   private static Log mLogger = LogFactory.getFactory().getInstance(NewsletterReceivers.class);
   private CelementsWebPluginApi celementsweb;
   
   private List<String> allAddresses = new ArrayList<String>();
   private List<String[]> groups = new ArrayList<String[]>();
   private List<String[]> groupUsers = new ArrayList<String[]>();
   private List<String[]> users = new ArrayList<String[]>();
   private List<String> addresses = new ArrayList<String>();
   
   public NewsletterReceivers(XWikiDocument blogDoc, XWikiContext context) throws XWikiException{
     celementsweb = (CelementsWebPluginApi)context.getWiki().getPluginApi("celementsweb", context);
     List<BaseObject> objs = blogDoc.getObjects("Celements2.ReceiverEMail");
     mLogger.debug("objs.size = " + (objs != null?objs.size():0));
     if(objs != null){
       for (BaseObject obj : objs) {
         mLogger.debug("obj: " + obj);
         if(obj != null){
          String address = obj.getStringValue("email").toLowerCase();
           boolean active = (obj.getIntValue("is_active") == 1);
           boolean isMail = address.matches("[\\w\\.]{1,}[@][\\w\\-\\.]{1,}([.]([\\w\\-\\.]{1,})){1,3}$");
           String type = obj.getStringValue("address_type");
           if(isMail && active && (!allAddresses.contains(address))) {
             addresses.add(address);
             allAddresses.add(address);
             mLogger.info("reveiver added: " + address);
           } else {
            if(context.getWiki().exists(address, context)){
              parseDocument(address, type, context);
             }
           }
         }
       }
     }
     String hql = "select nr.email from Celements.NewsletterReceiverClass as nr " +
         "where nr.isactive='1' " +
         "and subscribed='" + blogDoc.getFullName() + "'";
     List<String> nlRegAddresses = context.getWiki().search(hql, context);
     if(nlRegAddresses != null) {
       mLogger.info("Found " + nlRegAddresses.size() + " Celements.NewsletterReceiverClass" +
           " object-subscriptions for blog " + blogDoc.getFullName());
       for (String address : nlRegAddresses) {
         address = address.toLowerCase();
         if(!allAddresses.contains(address)) {
           addresses.add(address);
           allAddresses.add(address);
           mLogger.info("reveiver added: " + address);
         }
       }
     }
   }
   
   private void parseDocument(String address, String type, XWikiContext context) throws XWikiException {
     XWikiDocument recDoc = context.getWiki().getDocument(address, context);
     BaseObject userObj = recDoc.getObject("XWiki.XWikiUsers");
     List<BaseObject> groupObjs = recDoc.getObjects("XWiki.XWikiGroups");
     if(userObj != null){
       String email = userObj.getStringValue("email").toLowerCase();
       if((email.trim().length() > 0) && (!allAddresses.contains(email))){
         users.add(new String[]{recDoc.getFullName(), email});
         allAddresses.add(email);
       }
     } else if((groupObjs != null) && (groupObjs.size() > 0)){
       int usersInGroup = parseGroupMembers(groupObjs, type, context);
       groups.add(new String[]{recDoc.getFullName(), Integer.toString(usersInGroup)});
     }
   }
 
   private int parseGroupMembers(List<BaseObject> groupObjs, String type, XWikiContext context) throws XWikiException {
     int usersInGroup = 0;
     for (BaseObject groupObj : groupObjs) {
       if ((groupObj != null) && (groupObj.getStringValue("member") != null)) {
         String userDocName = groupObj.getStringValue("member");
         if((userDocName.trim().length() > 0) && context.getWiki().exists(userDocName, context)){
           XWikiDocument userDoc = context.getWiki().getDocument(userDocName, context);
           BaseObject groupUserObj = userDoc.getObject("XWiki.XWikiUsers");
           if(groupUserObj != null){
             String email = groupUserObj.getStringValue("email").toLowerCase();
             if((email.trim().length() > 0) && (!allAddresses.contains(email))){
               usersInGroup++;
               allAddresses.add(email);
               groupUsers.add(new String[]{userDocName, email});
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
 
     mLogger.debug("articleName = " + articleName);
     mLogger.debug("article exists = " + wiki.exists(articleName, context));
     if((articleName != null) && (!"".equals(articleName.trim()))
         && (wiki.exists(articleName, context))){
       XWikiDocument doc = wiki.getDocument(articleName, context);
       String baseURL = doc.getExternalURL("view", context);
 
       List<String[]> allUserMailPairs = null;
       mLogger.debug("is test send: " + isTest);
       if(isTest){
         String user = context.getUser();
         XWikiDocument userDoc = context.getWiki().getDocument(user, context);
         BaseObject userObj = userDoc.getObject("XWiki.XWikiUsers");
         if(userObj != null){
           String email = userObj.getStringValue("email");
           if(email.trim().length() > 0){
             allUserMailPairs = new ArrayList<String[]>();
             allUserMailPairs.add(new String[]{user, email});
           }
         }
       } else {
         allUserMailPairs = getNewsletterReceiversList();
       }
       
       for (String[] userMailPair : allUserMailPairs) {
         String origUser = context.getUser();
         context.setUser(userMailPair[0]);
         
         if(wiki.checkAccess("view", doc, context)){
           String htmlContent = getHtmlContent(doc, baseURL, context);
           htmlContent += getUnsubscribeFooter(userMailPair[1], doc, context);
           
           String textContent = wiki.getRenderingEngine().renderText(
               "$msg.get('cel_newsletter_text_only_message', ['_NEWSLETTEREMAILADRESSKEY_'])",
               doc, context);
           textContent = textContent.replaceAll("_NEWSLETTEREMAILADRESSKEY_",
               doc.getExternalURL("view", context));
           textContent += getUnsubscribeFooter(userMailPair[1], doc, context);
           
           int singleResult = sendMail(from, replyTo, userMailPair[1], subject,
               baseURL, htmlContent, textContent, context);
           result.add(new String[]{userMailPair[1], Integer.toString(singleResult)});
           if(singleResult == 0){ successfullySent++; }
         } else {
           mLogger.warn("Tried to send " + doc + " to user " + userMailPair[0] + " which" +
               "has no view rights on this Document.");
           List<String> params = new ArrayList<String>();
           params.add(doc.toString());
           result.add(new String[]{userMailPair[1], context.getMessageTool(
               ).get("cel_blog_newsletter_receiver_no_rights", params)});
         }
       
         context.setUser(origUser);
       }
       
       setNewsletterSentObject(doc, from, replyTo, subject, successfullySent, isTest, context);
     }
     
     return result;
   }
 
   List<String[]> getNewsletterReceiversList() {
     ArrayList<String[]> allUserMailPairs = new ArrayList<String[]>();
     allUserMailPairs.addAll(groupUsers);
     allUserMailPairs.addAll(users);
     for (String address : addresses) {
       String mailUser = "XWiki.XWikiGuest";
       String addrUser = null;
       try {
         addrUser = celementsweb.getUsernameForUserData(address, "email");
       } catch(XWikiException e) {
         mLogger.error("Exception getting username for user email '" + address + "'.", e);
       }
       if((addrUser != null) && (addrUser.length() > 0)) {
         mailUser = addrUser;
       }
       allUserMailPairs.add(new String[]{mailUser, address});
     }
     return allUserMailPairs;
   }
 
   private String getUnsubscribeFooter(String emailAddress,
       XWikiDocument blogDocument, XWikiContext context) throws XWikiException {
     String unsubscribeFooter = "";
     if(!"".equals(getUnsubscribeLink(blogDocument.getSpace(), emailAddress,
         context))){
       unsubscribeFooter = context.getWiki().getRenderingEngine().renderText(
           "$msg.get('cel_newsletter_unsubscribe_footer'," +
           " ['_NEWSLETTEREMAILADRESSKEY_'])", blogDocument, context);
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
 
   private String getHtmlContent(XWikiDocument doc, String baseURL, XWikiContext context) throws XWikiException {
     String header = "";
     if((baseURL != null) && !"".equals(baseURL.trim())){
       header = "<base href='" + baseURL + "' />\n";
     }
     
     IPageType pageType = celementsweb.getPageType(doc.getFullName());
     String content = celementsweb.getPlugin().renderCelementsPageType(doc, pageType, context);
     content = Utils.replacePlaceholders(content, context);
     
     String footer = context.getWiki().getRenderingEngine().renderText("$msg.get('cel_newsletter_html_footer_message', ['_NEWSLETTEREMAILADRESSKEY_'])", doc, context);
     footer = footer.replaceAll("_NEWSLETTEREMAILADRESSKEY_", doc.getExternalURL("view", context));
     
     return header + content + footer;
   }
 
   private int sendMail(String from, String replyTo, String to, String subject, String baseURL, 
       String htmlContent, String textContent, XWikiContext context) throws XWikiException {
     if((to != null) && (to.trim().length() == 0)){ to = null; }
     Map<String, String> otherHeader = new HashMap<String, String>();
     otherHeader.put("Content-Location", baseURL);
     
     return celementsweb.getPlugin().sendMail(from, replyTo, to, null, null, subject, htmlContent, textContent, null, otherHeader, context);
   }
   
   private void setNewsletterSentObject(XWikiDocument doc, String from, String replyTo, String subject, int nrOfSent, boolean isTest, XWikiContext context) throws XWikiException {
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
   
   private void setNewsletterHistory(BaseObject configObj, int nrOfSent, XWikiContext context){
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
   
   public int getNrOfReceivers(){
     return allAddresses.size();
   }
 }
