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
 package org.xwiki.contrib.mailarchive.internal.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.slf4j.Logger;
 import org.xwiki.bridge.DocumentAccessBridge;
 import org.xwiki.contrib.mailarchive.IMailingList;
 import org.xwiki.contrib.mailarchive.IServer;
 import org.xwiki.contrib.mailarchive.IType;
 import org.xwiki.contrib.mailarchive.internal.DefaultMailArchive;
 import org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration;
 import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryManager;
 
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 /**
  * @version $Id$
  */
 public class MailArchiveConfigurationImpl implements IMailArchiveConfiguration
 {
     private static final String CLASS_ADMIN = DefaultMailArchive.SPACE_CODE + ".AdminClass";
 
     private String adminPrefsPage;
 
     /* ***** GLOBAL PARAMETERS ***** */
 
     private List<IServer> servers;
 
     private Map<String, IMailingList> lists;
 
     private Map<String, IType> types;
 
     private String loadingUser;
 
     private String defaultHomeView;
 
     private String defaultTopicsView;
 
     private String defaultMailsOpeningMode;
 
     private boolean manageTimeline;
 
     private int maxTimelineItemsToLoad;
 
     private boolean matchProfiles;
 
     private boolean matchLdap;
 
     private boolean ldapCreateMissingProfiles;
 
     private boolean ldapForcePhotoUpdate;
 
     private String ldapPhotoFieldName;
 
     private String ldapPhotoFieldContent;
 
     private boolean cropTopicIds;
 
     private String itemsSpaceName;
 
     private boolean useStore;
 
     // Components
 
     /** Provides access to documents. Injected by the Component Manager. */
     @Inject
     private static DocumentAccessBridge dab;
 
     private QueryManager queryManager;
 
     private Logger logger;
 
     private MailArchiveFactory factory;
 
     private XWikiContext context;
 
     private XWiki xwiki;
 
     public MailArchiveConfigurationImpl(String adminPrefsPage, XWikiContext context, final QueryManager queryManager,
         final Logger logger, final MailArchiveFactory factory) throws MailArchiveException
     {
         this.adminPrefsPage = adminPrefsPage;
         this.context = context;
         this.xwiki = context.getWiki();
         this.queryManager = queryManager;
         this.logger = logger;
         this.factory = factory;
 
         load();
     }
 
     public void load() throws MailArchiveException
     {
         if (!xwiki.exists(adminPrefsPage, context)) {
             throw new MailArchiveException("Preferences page does not exist");
         }
         try {
             XWikiDocument prefsdoc = xwiki.getDocument(adminPrefsPage, context);
            BaseObject prefsobj = prefsdoc.getObject(CLASS_ADMIN);
             this.loadingUser = prefsobj.getStringValue("user");
             this.defaultHomeView = prefsobj.getStringValue("defaulthomeview");
             this.defaultTopicsView = prefsobj.getStringValue("defaulttopicsview");
             this.defaultMailsOpeningMode = prefsobj.getStringValue("mailsopeningmode");
             this.manageTimeline = prefsobj.getIntValue("timeline") != 0;
             this.maxTimelineItemsToLoad = prefsobj.getIntValue("timelinemaxload");
             this.matchProfiles = prefsobj.getIntValue("matchwikiprofiles") != 0;
             this.matchLdap = prefsobj.getIntValue("matchldap") != 0;
             this.ldapCreateMissingProfiles = prefsobj.getIntValue("createmissingprofiles") != 0;
             this.ldapForcePhotoUpdate = prefsobj.getIntValue("ldapphotoforceupdate") != 0;
             this.ldapPhotoFieldName = prefsobj.getStringValue("ldapphotofield");
             this.ldapPhotoFieldContent = prefsobj.getStringValue("ldapphototype");
             this.cropTopicIds = prefsobj.getIntValue("adv_croptopicid") != 0;
             this.itemsSpaceName = prefsobj.getStringValue("adv_itemsspace");
             this.useStore = prefsobj.getIntValue("store") != 0;
 
         } catch (XWikiException e) {
             throw new MailArchiveException("Error occurred while accessing configuration page", e);
         }
         this.servers = loadServersDefinitions();
         this.lists = loadMailingListsDefinitions();
         this.types = loadMailTypesDefinitions();
     }
 
     /**
      * Loads the mailing-lists definitions.
      * 
      * @return A map of mailing-lists definitions with key being the mailing-list pattern to check, and value an array
      *         [displayName, Tag]
      * @throws MailArchiveException
      */
     protected HashMap<String, IMailingList> loadMailingListsDefinitions() throws MailArchiveException
     {
         final HashMap<String, IMailingList> lists = new HashMap<String, IMailingList>();
 
         String xwql =
             "select list.pattern, list.displayname, list.Tag, list.color from Document doc, doc.object('"
                 + DefaultMailArchive.SPACE_CODE + ".ListsSettingsClass') as list where doc.space='"
                 + DefaultMailArchive.SPACE_PREFS + "'";
         try {
             List<Object[]> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();
 
             for (Object[] prop : props) {
                 if (prop[0] != null && !"".equals(prop[0])) {
                     // map[pattern] = [displayname, Tag]
                     // lists.put((String) prop[0], new String[] {(String) prop[1], (String) prop[2]});
                     IMailingList list =
                         factory.createMailingList((String) prop[0], (String) prop[1], (String) prop[2],
                             (String) prop[3]);
                     lists.put(list.getPattern(), list);
                     logger.info("Loaded list " + list);
                 } else {
                     logger.warn("Incorrect mailing-list found in db " + prop[1]);
                 }
             }
         } catch (Exception e) {
             throw new MailArchiveException("Failed to load mailing-lists settings", e);
         }
         return lists;
     }
 
     /**
      * Loads mail types from database.
      * 
      * @return A map of mail types definitions, key of map entries being the type name, and value the IType
      *         representation.
      * @throws MailArchiveException
      */
     protected Map<String, IType> loadMailTypesDefinitions() throws MailArchiveException
     {
         Map<String, IType> mailTypes = new HashMap<String, IType>();
 
         String xwql =
             "select type.name, type.displayName, type.icon, type.patternList from Document doc, doc.object("
                 + DefaultMailArchive.SPACE_CODE + ".TypesSettingsClass) as type where doc.space='"
                 + DefaultMailArchive.SPACE_PREFS + "'";
         try {
             List<Object[]> types = this.queryManager.createQuery(xwql, Query.XWQL).execute();
 
             for (Object[] type : types) {
 
                 IType typeobj =
                     factory.createMailType((String) type[0], (String) type[1], (String) type[2], (String) type[3]);
                 if (typeobj != null) {
                     mailTypes.put((String) type[0], typeobj);
                     logger.info("Loaded mail type " + typeobj);
                 } else {
                     logger.warn("Invalid type " + type[0]);
                 }
             }
 
         } catch (Exception e) {
             throw new MailArchiveException("Failed to load mail types settings", e);
         }
 
         return mailTypes;
     }
 
     /**
      * Loads the mailing-lists
      * 
      * @return
      * @throws MailArchiveException
      */
     protected List<IServer> loadServersDefinitions() throws MailArchiveException
     {
         final List<IServer> lists = new ArrayList<IServer>();
 
         String xwql =
             "select doc.fullName from Document doc, doc.object('" + DefaultMailArchive.SPACE_CODE
                 + ".ServerSettingsClass') as server where doc.space='" + DefaultMailArchive.SPACE_PREFS + "'";
         try {
             List<String> props = this.queryManager.createQuery(xwql, Query.XWQL).execute();
 
             for (String serverPrefsDoc : props) {
                 logger.info("Loading server definition from page " + serverPrefsDoc + " ...");
                 if (serverPrefsDoc != null && !"".equals(serverPrefsDoc)) {
                     MailServerImpl server = factory.createMailServer(serverPrefsDoc);
                     if (server != null) {
                         lists.add(server);
                         logger.info("Loaded IServer connection definition " + server);
                     } else {
                         logger.warn("Invalid server definition from document " + serverPrefsDoc);
                     }
 
                 } else {
                     logger.info("Incorrect IServer preferences doc found in db");
                 }
             }
         } catch (Exception e) {
             throw new MailArchiveException("Failed to load mailing-lists settings", e);
         }
         return lists;
     }
 
     public String getLoadingUser()
     {
         return loadingUser;
     }
 
     public String getDefaultHomeView()
     {
         return defaultHomeView;
     }
 
     public String getDefaultTopicsView()
     {
         return defaultTopicsView;
     }
 
     public String getDefaultMailsOpeningMode()
     {
         return defaultMailsOpeningMode;
     }
 
     public boolean isManageTimeline()
     {
         return manageTimeline;
     }
 
     public int getMaxTimelineItemsToLoad()
     {
         return maxTimelineItemsToLoad;
     }
 
     public boolean isMatchProfiles()
     {
         return matchProfiles;
     }
 
     public boolean isMatchLdap()
     {
         return matchLdap;
     }
 
     public boolean isLdapCreateMissingProfiles()
     {
         return ldapCreateMissingProfiles;
     }
 
     public boolean isLdapForcePhotoUpdate()
     {
         return ldapForcePhotoUpdate;
     }
 
     public String getLdapPhotoFieldName()
     {
         return ldapPhotoFieldName;
     }
 
     public String getLdapPhotoFieldContent()
     {
         return ldapPhotoFieldContent;
     }
 
     @Override
     public boolean isCropTopicIds()
     {
         return cropTopicIds;
     }
 
     public void setCropTopicIds(boolean cropTopicIds)
     {
         this.cropTopicIds = cropTopicIds;
     }
 
     @Override
     public String getItemsSpaceName()
     {
         return itemsSpaceName;
     }
 
     public void setItemsSpaceName(String itemsSpaceName)
     {
         this.itemsSpaceName = itemsSpaceName;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration#isUseStore()
      */
     @Override
     public boolean isUseStore()
     {
         return useStore;
     }
 
     public void setUseStore(boolean useStore)
     {
         this.useStore = useStore;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration#getMailingLists()
      */
     @Override
     public Map<String, IMailingList> getMailingLists()
     {
         return this.lists;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration#getServers()
      */
     @Override
     public List<IServer> getServers()
     {
         return this.servers;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IMailArchiveConfiguration#getMailTypes()
      */
     @Override
     public Map<String, IType> getMailTypes()
     {
         return this.types;
     }
 
     public static Object getPropertyValue(String docname, String classname, String propname)
     {
         return dab.getProperty(docname, classname, 0, propname);
     }
 
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
         builder.append("MailArchiveConfigurationImpl [adminPrefsPage=").append(adminPrefsPage).append(", loadingUser=")
             .append(loadingUser).append(", defaultHomeView=").append(defaultHomeView).append(", defaultTopicsView=")
             .append(defaultTopicsView).append(", defaultMailsOpeningMode=").append(defaultMailsOpeningMode)
             .append(", manageTimeline=").append(manageTimeline).append(", maxTimelineItemsToLoad=")
             .append(maxTimelineItemsToLoad).append(", matchProfiles=").append(matchProfiles).append(", matchLdap=")
             .append(matchLdap).append(", ldapCreateMissingProfiles=").append(ldapCreateMissingProfiles)
             .append(", ldapForcePhotoUpdate=").append(ldapForcePhotoUpdate).append(", ldapPhotoFieldName=")
             .append(ldapPhotoFieldName).append(", ldapPhotoFieldContent=").append(ldapPhotoFieldContent)
             .append(", cropTopicIds=").append(cropTopicIds).append(", itemsSpaceName=").append(itemsSpaceName)
             .append(", useStore=").append(useStore).append(", servers=").append(servers).append(", lists=")
             .append(lists).append(", types=").append(types).append("]");
         return builder.toString();
     }
 
 }
