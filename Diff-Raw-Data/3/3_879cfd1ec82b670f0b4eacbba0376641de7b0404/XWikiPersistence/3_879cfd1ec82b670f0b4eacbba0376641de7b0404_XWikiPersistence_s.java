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
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.xwiki.contrib.mail.MailItem;
 import org.xwiki.contrib.mailarchive.internal.DefaultMailArchive;
 
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 /**
  * @version $Id$
  */
 public class XWikiPersistence implements IPersistence
 {
     /**
      * XWiki profile name of a non-existing user.
      */
     public static final String UNKNOWN_USER = "XWiki.UserDoesNotExist";
 
     public static final int MAX_PAGENAME_LENGTH = 30;
 
     private XWikiContext context;
 
     private XWiki xwiki;
 
     private Logger logger;
 
     public XWikiPersistence(XWikiContext context, XWiki xwiki, Logger logger)
     {
         this.context = context;
         this.xwiki = xwiki;
         this.logger = logger;
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
 
         XWikiDocument topicDoc;
 
         String topicwikiname = context.getWiki().clearName(pagename, context);
         if (topicwikiname.length() >= MAX_PAGENAME_LENGTH) {
             topicwikiname = topicwikiname.substring(0, MAX_PAGENAME_LENGTH);
         }
         String uniquePageName =
             context.getWiki().getUniquePageName(DefaultMailArchive.SPACE_ITEMS, topicwikiname, context);
         topicDoc = xwiki.getDocument(DefaultMailArchive.SPACE_ITEMS + "." + uniquePageName, context);
         BaseObject topicObj = topicDoc.newObject(DefaultMailArchive.SPACE_CODE + ".MailTopicClass", context);
 
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
 
        topicObj.set("type", m.getType(), context);
         topicDoc.setParent(DefaultMailArchive.SPACE_HOME + ".WebHome");
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
 
         return topicDoc.getFullName();
     }
 
     @Override
     public void updateServerState(String serverPrefsDoc, int status) throws XWikiException
     {
         logger.debug("Updating server state in " + serverPrefsDoc);
         XWikiDocument serverDoc = context.getWiki().getDocument(serverPrefsDoc, context);
         BaseObject serverObj = serverDoc.getObject(DefaultMailArchive.SPACE_CODE + ".ServerSettingsClass");
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
     private void saveAsUser(final XWikiDocument doc, final String user, final String contentUser, final String comment)
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
