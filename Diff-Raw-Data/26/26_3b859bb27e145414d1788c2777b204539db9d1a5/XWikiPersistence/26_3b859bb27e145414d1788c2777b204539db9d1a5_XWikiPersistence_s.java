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
 package org.xwiki.contrib.mailarchive.internal.persistence;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.zip.GZIPOutputStream;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeBodyPart;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.phase.Initializable;
 import org.xwiki.component.phase.InitializationException;
 import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
 import org.xwiki.contrib.mail.MailContent;
 import org.xwiki.contrib.mail.MailItem;
 import org.xwiki.contrib.mail.internal.MailAttachment;
 import org.xwiki.contrib.mail.internal.util.Utils;
 import org.xwiki.contrib.mailarchive.internal.utils.ITextUtils;
 import org.xwiki.contrib.mailarchive.internal.xwiki.ExtendedDocumentAccessBridge;
 import org.xwiki.contrib.mailarchive.internal.xwiki.IExtendedDocumentAccessBridge;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.util.Util;
 
 /**
  * @version $Id$
  */
 @Component
 @Singleton
 public class XWikiPersistence implements IPersistence, Initializable
 {
     /**
      * XWiki profile name of a non-existing user.
      */
     public static final String UNKNOWN_USER = "XWiki.UserDoesNotExist";
 
     /**
      * Name of the space that contains end-user targeted pages.
      */
     public static final String SPACE_HOME = "MailArchive";
 
     /**
      * Name of the space that contains technical code.
      */
     public static final String SPACE_CODE = "MailArchiveCode";
 
     /**
      * Name of the space that contains configuration / preferences
      */
     public static final String SPACE_PREFS = "MailArchivePrefs";
 
     /**
      * Name of the space that contains created objects
      */
     public static String SPACE_ITEMS = "MailArchiveItems";
 
     public static final String CLASS_TOPICS = SPACE_CODE + ".TopicClass";
 
     public static final String CLASS_MAILS = SPACE_CODE + ".MailClass";
 
     public static final String CLASS_MAIL_TYPES = SPACE_CODE + ".TypeClass";
 
     public static final String CLASS_MAIL_MATCHERS = SPACE_CODE + ".MailMatcherClass";
 
     public static final String CLASS_MAIL_LISTS = SPACE_CODE + ".MailingListClass";
 
     public static final String CLASS_MAIL_LIST_GROUPS = SPACE_CODE + ".MailingListGroupClass";
 
     public static final String CLASS_MAIL_SERVERS = SPACE_CODE + ".ServerClass";
 
     public static final String CLASS_MAIL_STORES = SPACE_CODE + ".StoreClass";
 
     public static final String CLASS_LOADING_SESSION = SPACE_CODE + ".LoadingSessionClass";
 
     public static final String TEMPLATE_MAILS = SPACE_CODE + ".MailTemplate";
 
     public static final String PAGE_GLOBAL_PARAMETERS = SPACE_PREFS + ".GlobalParameters";
 
     @Inject
     private Logger logger;
 
     @Inject
     private Execution execution;
 
     @Inject
     @Named("extended")
     private IExtendedDocumentAccessBridge bridge;
 
     @Inject
     private ITextUtils textUtils;
 
     @Inject
     private QueryManager queryManager;
 
     private XWiki xwiki;
 
     private XWikiContext context;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.component.phase.Initializable#initialize()
      */
     @Override
     public void initialize() throws InitializationException
     {
        ExecutionContext context = execution.getContext();
        this.context = (XWikiContext) context.getProperty("xwikicontext");
         this.xwiki = this.context.getWiki();
 
     }
 
     /**
      * createTopicPage Creates a wiki page for a Topic.
      * 
      * @throws XWikiException
      */
     @Override
     public String createTopic(final String pagename, final MailItem m, final ArrayList<String> taglist,
         final String loadingUser, final boolean create) throws XWikiException
     {
         logger.debug("createTopic(pagename=" + pagename + ", m=" + m + ", taglist=" + taglist + ", loadingUser="
             + loadingUser + ", create=" + create + ")");
         final String uniquePageName = bridge.getValidUniqueName(pagename, SPACE_ITEMS);
         final XWikiDocument topicDoc = xwiki.getDocument(SPACE_ITEMS + "." + uniquePageName, context);
         BaseObject topicObj = topicDoc.newObject(SPACE_CODE + ".TopicClass", context);
 
         topicObj.set("topicid", m.getTopicId(), context);
         topicObj.set("subject", m.getTopic(), context);
         // Note : we always add author and stardate at topic creation because anyway we will update this later if
         // needed, to avoid topics with "unknown" author
         topicObj.set("startdate", m.getDate(), context);
         topicObj.set("author", m.getFrom(), context);
 
         // when first created, we put the same date as start date
         topicObj.set("lastupdatedate", m.getDate(), context);
         topicDoc.setCreationDate(m.getDate());
         topicDoc.setDate(m.getDate());
         topicDoc.setContentUpdateDate(m.getDate());
         topicObj.set("sensitivity", m.getSensitivity(), context);
         topicObj.set("importance", m.getImportance(), context);
 
         String types = StringUtils.join(m.getTypes().toArray(new String[] {}), ',');
         topicObj.set("type", types, context);
         topicDoc.setParent(SPACE_HOME + ".WebHome");
         topicDoc.setTitle("Topic " + m.getTopic());
         topicDoc.setComment("Created topic from mail [" + m.getMessageId() + "]");
 
         // Materialize mailing-lists information and mail IType in Tags
         if (taglist.size() > 0) {
             BaseObject tagobj = topicDoc.newObject("XWiki.TagClass", context);
             String tags = StringUtils.join(taglist.toArray(new String[] {}), ',');
             tagobj.set("tags", tags.replaceAll(" ", "_"), context);
         }
 
         if (create) {
             saveAsUser(topicDoc, m.getWikiuser(), loadingUser, "Created topic from mail [" + m.getMessageId() + "]");
         }
 
         logger.debug("createTopic() Created " + topicDoc.getFullName());
 
         return topicDoc.getFullName();
     }
 
     /**
      * updateTopicPage Update topic against new mail taking part to existing topic.
      */
     /**
      * @param m
      * @param existingTopicId
      * @param dateFormatter
      * @param create
      * @return
      * @throws XWikiException
      * @throws ParseException
      */
     @Override
     public String updateTopicPage(final MailItem m, final String existingTopicPage,
         final SimpleDateFormat dateFormatter, final String loadingUser, final boolean create) throws XWikiException
     {
         logger.debug("updateTopicPage(m=" + m + ", existingTopicPage=" + existingTopicPage + ", loadingUser="
             + loadingUser + ", create=" + create + ")");
 
         XWikiDocument topicDoc = xwiki.getDocument(existingTopicPage, context);
         logger.debug("Existing topic " + topicDoc);
         BaseObject topicObj = topicDoc.getObject(XWikiPersistence.SPACE_CODE + ".TopicClass");
         Date lastupdatedate = topicObj.getDateValue("lastupdatedate");
         Date startdate = topicObj.getDateValue("startdate");
         String originalAuthor = topicObj.getStringValue("author");
         if (lastupdatedate == null || "".equals(lastupdatedate)) {
             lastupdatedate = m.getDate();
         } // note : this should never occur
         if (startdate == null || "".equals(startdate)) {
             startdate = m.getDate();
         }
 
         boolean isMoreRecent = (m.getDate().getTime() > lastupdatedate.getTime());
         boolean isMoreAncient = (m.getDate().getTime() < startdate.getTime());
         logger.debug("mail date = " + m.getDate().getTime() + ", last update date = " + lastupdatedate.getTime()
             + ", is more recent = " + isMoreRecent + ", is more ancient = " + isMoreAncient + ", first in topic = "
             + m.isFirstInTopic());
 
         // If the first one, we add the startdate to existing topic
         if (m.isFirstInTopic() || isMoreRecent) {
             boolean dirty = false;
             logger.debug("Checking if existing topic has to be updated ...");
             String comment = "";
             // if (m.isFirstInTopic) {
             if ((!originalAuthor.equals(m.getFrom()) && isMoreAncient) || "".equals(originalAuthor)) {
                 logger.debug("     updating author from " + originalAuthor + " to " + m.getFrom());
                 topicObj.set("author", m.getFrom(), context);
                 comment += " Updated author ";
                 dirty = true;
             }
             logger.debug("     existing startdate " + topicObj.getDateValue("startdate"));
             if ((topicObj.getStringValue("startdate") == null || "".equals(topicObj.getStringValue("startdate")))
                 || isMoreAncient) {
                 logger.debug("     checked startdate not already added to topic");
                 topicObj.set("startdate", m.getDate(), context);
                 topicDoc.setCreationDate(m.getDate());
                 comment += " Updated start date ";
                 dirty = true;
             }
             // }
             if (isMoreRecent) {
                 logger.debug("     updating lastupdatedate from " + lastupdatedate + " to "
                     + dateFormatter.format(m.getDate()));
                 topicObj.set("lastupdatedate", m.getDate(), context);
                 topicDoc.setDate(m.getDate());
                 topicDoc.setContentUpdateDate(m.getDate());
 
                 comment += " Updated last update date ";
                 dirty = true;
             }
             topicDoc.setComment(comment);
 
             if (create && dirty) {
                 logger.debug("     Updated existing topic");
                 saveAsUser(topicDoc, m.getWikiuser(), loadingUser, comment);
             }
         } else {
             logger.debug("     Nothing to update in topic");
         }
 
         // return topicDoc
 
         return topicDoc.getFullName();
     }
 
     /**
      * createMailPage Creates a wiki page for a Mail.
      * 
      * @throws XWikiException
      * @throws IOException
      * @throws MessagingException
      */
     @Override
     public String createMailPage(final MailItem m, final String pageName, final String existingTopicId,
         final boolean isAttachedMail, final List<String> taglist, final List<String> attachedMailsPages,
         final String parentMail, final String loadingUser, final boolean create) throws XWikiException,
         MessagingException, IOException
     {
         logger.debug("createMailPage(" + m + "," + existingTopicId + "," + isAttachedMail + "," + parentMail + ","
             + create + ")");
 
         XWikiDocument msgDoc;
         String docFullName = XWikiPersistence.SPACE_ITEMS + '.' + pageName;
         String content = "";
         String htmlcontent = "";
         String zippedhtmlcontent = "";
 
         // a map to store attachment filename = contentId for replacements in HTML retrieved from mails
         HashMap<String, String> attachmentsMap = new HashMap<String, String>();
         ArrayList<MimeBodyPart> attbodyparts = new ArrayList<MimeBodyPart>();
 
        msgDoc = xwiki.getDocument(XWikiPersistence.SPACE_ITEMS + '.' + pageName, context);
         logger.debug("NEW MSG msgwikiname=" + pageName);
 
         Object bodypart = m.getBodypart();
         logger.debug("bodypart class " + bodypart.getClass());
         // addDebug("mail content type " + m.contentType)
         // Retrieve mail body(ies)
         MailContent mailContent = m.getMailContent();
         // Resolve attachment urls against wiki document
         for (MailAttachment wikiAttachment : mailContent.getWikiAttachments().values()) {
             final String attachmentUrl = msgDoc.getAttachmentURL(wikiAttachment.getFilename(), context);
             wikiAttachment.setUrl(attachmentUrl);
         }
 
         content = mailContent.getText();
         htmlcontent = mailContent.getHtml();
 
         if (content == null) {
             content = "";
         }
         if (htmlcontent == null) {
             htmlcontent = "";
         }
 
         // Truncate body
         content = textUtils.truncateStringForBytes(content, 65500, 65500);
 
         // Treat Html part
         zippedhtmlcontent = treatHtml(htmlcontent, mailContent.getWikiAttachments());
 
         // Treat lengths
         m.setMessageId(textUtils.truncateForString(m.getMessageId()));
         m.setSubject(textUtils.truncateForString(m.getSubject()));
         String existingTopicIdTruncated = textUtils.truncateForString(existingTopicId);
         m.setTopicId(textUtils.truncateForString(m.getTopicId()));
         m.setTopic(textUtils.truncateForString(m.getTopic()));
         m.setReplyToId(textUtils.truncateForLargeString(m.getReplyToId()));
         m.setRefs(textUtils.truncateForLargeString(m.getRefs()));
         m.setFrom(textUtils.truncateForLargeString(m.getFrom()));
         m.setTo(textUtils.truncateForLargeString(m.getTo()));
         m.setCc(textUtils.truncateForLargeString(m.getCc()));
 
         // Assign text body converted from html content if there is no pure-text content
         if (StringUtils.isBlank(content) && !StringUtils.isBlank(htmlcontent)) {
             String converted = textUtils.htmlToPlainText(htmlcontent);
             if (converted != null && !"".equals(converted)) {
                 // replace content with value (remove excessive whitespace also)
                 content = converted.replaceAll("[\\s]{2,}", "\n");
                 logger.debug("Text body now contains converted html content");
             } else {
                 logger.debug("Conversion from HTML to Plain Text returned empty or null string");
             }
         }
 
         // Fill all new object's fields
         BaseObject msgObj = msgDoc.newObject(XWikiPersistence.SPACE_CODE + ".MailClass", context);
         msgObj.set("messageid", m.getMessageId(), context);
         msgObj.set("messagesubject", m.getSubject(), context);
 
         msgObj.set("topicid", existingTopicIdTruncated, context);
         msgObj.set("topicsubject", m.getTopic(), context);
         msgObj.set("inreplyto", m.getReplyToId(), context);
         msgObj.set("references", m.getRefs(), context);
         msgObj.set("date", m.getDate(), context);
         msgDoc.setCreationDate(m.getDate());
         msgDoc.setDate(m.getDate());
         msgDoc.setContentUpdateDate(m.getDate());
         msgObj.set("from", m.getFrom(), context);
         msgObj.set("to", m.getTo(), context);
         msgObj.set("cc", m.getCc(), context);
         msgObj.set("body", content, context);
         msgObj.set("bodyhtml", zippedhtmlcontent, context);
         msgObj.set("sensitivity", m.getSensitivity(), context);
         if (attachedMailsPages.size() != 0) {
             msgObj.set("attachedMails", StringUtils.join(attachedMailsPages, ','), context);
         }
         if (isAttachedMail) {
             m.setAttached(true);
             msgObj.set("attached", "1", context);
             // m.setBuiltinType(IType.BUILTIN_TYPE_ATTACHED_MAIL);
         }
         if (m.getTypes().size() > 0) {
             String types = StringUtils.join(m.getTypes().toArray(new String[] {}), ',');
             msgObj.set("type", types, context);
         }
 
         msgDoc.setParent(parentMail);
         msgDoc.setTitle("Message " + m.getSubject());
         if (!isAttachedMail) {
             msgDoc.setComment("Created message");
         } else {
             msgDoc.setComment("Attached mail created");
         }
 
         if (taglist.size() > 0) {
             BaseObject tagobj = msgDoc.newObject("XWiki.TagClass", context);
             String tags = StringUtils.join(taglist.toArray(new String[] {}), ',');
             tagobj.set("tags", tags, context);
         }
 
         if (create && !existsMessage(m.getMessageId())) {
             logger.debug("saving message " + m.getSubject());
             saveAsUser(msgDoc, m.getWikiuser(), loadingUser, "Created message from mailing-list");
         }
 
         logger.debug("adding attachments to document");
         addAttachmentsToMailPage(msgDoc, attbodyparts, attachmentsMap);
 
         logger.debug("  mail loaded and saved :" + docFullName);
 
         return docFullName;
     }
 
     // ****** Check existence of wiki object with same value as 'messageid', from database
     @Override
     public boolean existsMessage(final String msgid)
     {
         boolean exists = false;
         String hql =
             "select count(*) from StringProperty as prop where prop.name='messageid' and prop.value='" + msgid + "')";
 
         try {
             List<Object> result = queryManager.createQuery(hql, Query.HQL).execute();
             logger.debug("CheckMsgIdExistence result " + result);
             exists = (Long) result.get(0) != 0;
         } catch (QueryException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         if (!exists) {
             logger.debug("Message with id " + msgid + " does not exist in database");
             return false;
         } else {
             logger.debug("Message with id " + msgid + " already loaded in database");
             return true;
         }
 
     }
 
     /*
      * Add map of attachments (bodyparts) to a document (doc1)
      */
     private int addAttachmentsToMailPage(final XWikiDocument doc1, final ArrayList<MimeBodyPart> bodyparts,
         final HashMap<String, String> attachmentsMap) throws MessagingException
     {
         int nb = 0;
         for (MimeBodyPart bodypart : bodyparts) {
             String fileName = bodypart.getFileName();
             String cid = bodypart.getContentID();
 
             try {
                 // replace by correct name if filename was renamed (multiple attachments with same name)
                 if (attachmentsMap.containsKey(cid)) {
                     fileName = attachmentsMap.get(cid);
                 }
                 logger.debug("Treating attachment: " + fileName + " with contentid " + cid);
                 if (fileName == null) {
                     fileName = "fichier.doc";
                 }
                 if (fileName.equals("oledata.mso") || fileName.endsWith(".wmz") || fileName.endsWith(".emz")) {
                     logger.debug("Not treating Microsoft crap !");
                 } else {
                     String disposition = bodypart.getDisposition();
                     String contentType = bodypart.getContentType().toLowerCase();
 
                     logger.debug("Treating attachment of type: " + contentType);
 
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     OutputStream out = new BufferedOutputStream(baos);
                     // We can't just use p.writeTo() here because it doesn't
                     // decode the attachment. Instead we copy the input stream
                     // onto the output stream which does automatically decode
                     // Base-64, quoted printable, and a variety of other formats.
                     InputStream ins = new BufferedInputStream(bodypart.getInputStream());
                     int b = ins.read();
                     while (b != -1) {
                         out.write(b);
                         b = ins.read();
                     }
                     out.flush();
                     out.close();
                     ins.close();
 
                     logger.debug("Treating attachment step 3: " + fileName);
 
                     byte[] data = baos.toByteArray();
                     logger.debug("Ready to attach attachment: " + fileName);
                     addAttachmentToPage(doc1, fileName, data);
                     nb++;
                 } // end if
             } catch (Exception e) {
                 logger.warn("Attachment " + fileName + " could not be treated", e);
             }
         } // end for all attachments
         return nb;
     }
 
     /*
      * Add to document (doc1) an attached file (afilename) with its content (adata), and fills a map (adata) with
      * relation between contentId (cid) and (afilename)
      */
     private void addAttachmentToPage(final XWikiDocument doc, final String afilename, final byte[] adata)
         throws XWikiException
     {
         String filename = getAttachmentValidName(afilename);
         logger.debug("adding attachment: " + filename);
 
         XWikiAttachment attachment = new XWikiAttachment();
         doc.getAttachmentList().add(attachment);
         attachment.setContent(adata);
         attachment.setFilename(filename);
         // TODO: handle Author
         attachment.setAuthor(context.getUser());
         // Add the attachment to the document
         attachment.setDoc(doc);
         logger.debug("saving attachment: " + filename);
         doc.setComment("Added attachment " + filename);
         doc.saveAttachmentContent(attachment, context);
     }
 
     /*
      * Returns a valid name for an attachment from its original name
      */
     public String getAttachmentValidName(final String afilename)
     {
         int i = afilename.lastIndexOf("\\");
         if (i == -1) {
             i = afilename.lastIndexOf("/");
         }
         String filename = afilename.substring(i + 1);
         filename = filename.replaceAll("\\+", " ");
         return filename;
     }
 
     @Override
     public String getMessageUniquePageName(final MailItem m, final boolean isAttachedMail)
     {
         char prefix = 'M';
         if (isAttachedMail) {
             prefix = 'A';
         }
         String msgwikiname = xwiki.clearName(prefix + m.getTopic().replaceAll(" ", ""), context);
         if (msgwikiname.length() >= ExtendedDocumentAccessBridge.MAX_PAGENAME_LENGTH) {
             msgwikiname = msgwikiname.substring(0, ExtendedDocumentAccessBridge.MAX_PAGENAME_LENGTH);
         }
         return msgwikiname;
     }
 
     /*
      * Cleans up HTML content and treat it to replace cid tags with correct image urls (targeting attachments), then zip
      * it.
      */
     private String treatHtml(final String htmlcontent, final HashMap<String, MailAttachment> attachmentsMap)
         throws IOException
     {
         String htmlcontentReplaced = "";
         if (!StringUtils.isBlank(htmlcontent)) {
             logger.debug("Original HTML length " + htmlcontent.length());
 
             // Replacement to avoid issue of "A circumflex" characters displayed (???)
             htmlcontentReplaced = htmlcontent.replaceAll("&Acirc;", " ");
 
             // Replace attachment URLs in HTML content for images to be shown
             for (Entry<String, MailAttachment> att : attachmentsMap.entrySet()) {
                 // remove starting "<" and finishing ">"
                 final String cid = att.getKey();
                 // If there is no cid, it means this attachment is not INLINE, so there's nothing more to do
                 if (!StringUtils.isEmpty(cid)) {
                     String pattern = att.getKey().substring(1, att.getKey().length() - 2);
                     pattern = "cid:" + pattern;
 
                     logger.debug("Testing for CID pattern " + Util.encodeURI(pattern, context) + " " + pattern);
                     String replacement = att.getValue().getUrl();
                     logger.debug("To be replaced by " + replacement);
                     htmlcontentReplaced = htmlcontentReplaced.replaceAll(pattern, replacement);
                 } else {
                     logger.warn("treatHtml: attachment is supposed not inline as cid is null or empty: "
                         + att.getValue().getFilename());
                 }
             }
 
             logger.debug("Zipping HTML part ...");
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream zos = new GZIPOutputStream(bos);
             byte[] bytes = htmlcontentReplaced.getBytes("UTF8");
             zos.write(bytes, 0, bytes.length);
             zos.finish();
             zos.close();
 
             byte[] compbytes = bos.toByteArray();
             htmlcontentReplaced = Utils.byte2hex(compbytes);
             bos.close();
 
             if (htmlcontentReplaced.length() > ITextUtils.LONG_STRINGS_MAX_LENGTH) {
                 logger.debug("Failed to have HTML fit in target field, truncating");
                 htmlcontentReplaced = textUtils.truncateForLargeString(htmlcontentReplaced);
             }
 
         } else {
             logger.debug("No HTML to treat");
         }
 
         logger.debug("Html Zipped length : " + htmlcontentReplaced.length());
         return htmlcontentReplaced;
     }
 
     @Override
     public void updateMailServerState(final String serverPrefsDoc, final int status) throws XWikiException
     {
         logger.debug("Updating server state in " + serverPrefsDoc);
         XWikiDocument serverDoc = context.getWiki().getDocument(serverPrefsDoc, context);
         BaseObject serverObj = serverDoc.getObject(CLASS_MAIL_SERVERS);
         serverObj.set("status", status, context);
         serverObj.setDateValue("lasttest", new Date());
         xwiki.saveDocument(serverDoc, context);
     }
 
     @Override
     public void updateMailStoreState(final String storePrefsDoc, final int status) throws XWikiException
     {
         logger.debug("Updating store state in " + storePrefsDoc);
         XWikiDocument serverDoc = context.getWiki().getDocument(storePrefsDoc, context);
         BaseObject serverObj = serverDoc.getObject(CLASS_MAIL_STORES);
         serverObj.set("status", status, context);
         serverObj.setDateValue("lasttest", new Date());
         xwiki.saveDocument(serverDoc, context);
     }
 
     /**
      * @param doc
      * @param user
      * @param contentUser
      * @param comment
      * @throws XWikiException
      */
     @Override
     public void saveAsUser(final XWikiDocument doc, final String user, final String contentUser, final String comment)
         throws XWikiException
     {
         String luser = user;
         // If user is not provided we leave existing one
         if (luser == null) {
             if (xwiki.exists(doc.getFullName(), context)) {
                 luser = doc.getCreator();
             } else {
                 luser = UNKNOWN_USER;
             }
         }
         // We set creator only at document creation
         if (!xwiki.exists(doc.getFullName(), context)) {
             doc.setCreator(luser);
         }
         doc.setAuthor(luser);
         doc.setContentAuthor(contentUser);
         // avoid automatic set of update date to current date
         doc.setContentDirty(false);
         doc.setMetaDataDirty(false);
        xwiki.getXWiki(context).saveDocument(doc, comment, context);
     }

 }
