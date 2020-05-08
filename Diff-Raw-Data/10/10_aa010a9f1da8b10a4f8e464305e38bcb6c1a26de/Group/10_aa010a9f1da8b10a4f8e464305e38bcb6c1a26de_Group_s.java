 package org.xwiki.sankore;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.api.Property;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xwiki.context.ExecutionContext;
 import org.xwiki.model.EntityType;
 import org.xwiki.model.reference.*;
 import org.xwiki.sankore.internal.GroupObjectDocument;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Api;
 import com.xpn.xwiki.web.Utils;
 
 public class Group extends Api
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(Group.class);
 
     public static final String WEBHOME_PAGENAME = "WebHome";
     public static final String JOINGROUP_PAGENAME = "JoinGroup";
 
     public static final String ACTION_VIEW = "view";
 
     public static final String GROUP_SPACE_PREFIX = "Group_";
     public static final String GROUP_COLL_SPACE_PREFIX = "Coll_Group_";
     public static final String GROUP_DOCUMENTATION_SPACE_PREFIX = "Documentation_Group_";
     public static final String GROUP_INVITATIONS_SPACE_PREFIX = "Invitations_Group_";
     public static final String GROUP_MESSAGES_SPACE_PREFIX = "Messages_Group_";
     public static final String GROUP_USERPROFILES_SPACE_PREFIX = "UserProfiles_Group_";
 
     public static final String ADMINGROUP_NAME = "AdminGroup";
     public static final String MEMBERGROUP_NAME = "MemberGroup";
     public static final String ROLE_AFFILIATEGROUP_NAME = "Role_AffiliateGroup";
     public static final String ROLE_CONTRIBUTORPARTICIPANTGROUP_NAME = "Role_ContributorParticipantGroup";
 
     public static final String ADMIN_MEMBER_ROLE = "admin";
     //public static final String[] MEMBER_ROLES = ["admin", "Affiliate", "ContributorParticipant"];
 
 
     public static final String XWIKI_SPACE = "XWiki";
 
     public static final String XWIKIALLGROUP_NAME = "XWikiAllGroup";
     public static final EntityReference XWIKIALLGROUP_REFERENCE = new EntityReference(XWIKIALLGROUP_NAME,
             EntityType.DOCUMENT, new EntityReference(XWIKI_SPACE, EntityType.SPACE));
 
     public static final String XWIKIADMINGROUP_NAME = "XWikiAdminGroup";
     public static final EntityReference XWIKIADMINGROUP_REFERENCE = new EntityReference(XWIKIADMINGROUP_NAME,
             EntityType.DOCUMENT, new EntityReference(XWIKI_SPACE, EntityType.SPACE));
 
     public static final String RIGHTS_LEVELS_VIEW = "view";
     public static final String RIGHTS_LEVELS_EDIT = "edit";
     public static final String RIGHTS_LEVELS_COMMENT = "comment";
     public static final String RIGHTS_LEVELS_DELETE = "delete";
     public static final String RIGHTS_LEVELS_ADMIN = "admin";
 
     //protected Space groupSpace;
     //protected Space collSpace;
     //protected Space documentationSpace;
     //protected Space invitationsSpace;
     //protected Space messagesSpace;
     //protected Space userProfilesSpace;
 
     protected GroupObjectDocument groupObjectDocument;
 
     @SuppressWarnings("unchecked")
     private DocumentReferenceResolver<EntityReference> currentReferenceDocumentReferenceResolver = Utils.getComponent(
             DocumentReferenceResolver.class, "current/reference");
 
     @SuppressWarnings("unchecked")
     private EntityReferenceSerializer<String> localEntityReferenceSerializer = Utils.getComponent(
             EntityReferenceSerializer.class, "local");
 
     protected MembersGroup adminGroup;
     protected MembersGroup memberGroup;
 
     protected SpaceReference groupSpaceReference;
     protected SpaceReference invitationsSpaceReference;
     protected SpaceReference userProfilesSpaceReference;
     protected SpaceReference collSpaceReference;
     protected SpaceReference messagesSpaceReference;
     protected SpaceReference documentationSpaceReference;
 
     protected DocumentReference adminMembersGroupReference;
     protected DocumentReference allMembersGroupReference;
 
     protected boolean isDirty;
     protected boolean isRightsDirty;
 
     public Group(GroupObjectDocument objectDocument, ExecutionContext executionContext) throws XWikiException
     {
         super((XWikiContext) executionContext.getProperty("xwikicontext"));
 
         this.groupObjectDocument = objectDocument;
         this.groupSpaceReference = objectDocument.getDocumentReference().getLastSpaceReference();
 
         String groupSpaceName = this.groupSpaceReference.getName();
         WikiReference wikiReference = new WikiReference(this.groupSpaceReference.getParent());
 
         this.invitationsSpaceReference = new SpaceReference("Invitations_" + groupSpaceName, wikiReference);
         this.userProfilesSpaceReference = new SpaceReference("UserProfiles_" + groupSpaceName, wikiReference);
         this.collSpaceReference = new SpaceReference("Coll_" + groupSpaceName, wikiReference);
         this.messagesSpaceReference = new SpaceReference("Messages_" + groupSpaceName, wikiReference);
         this.documentationSpaceReference = new SpaceReference("Documentation_" + groupSpaceName, wikiReference);
 
         this.adminMembersGroupReference = new DocumentReference("AdminGroup", this.groupSpaceReference);
         this.allMembersGroupReference = new DocumentReference("MemberGroup", this.groupSpaceReference);
     }
 
     /**
      * Create instance of space descriptor.
      *
      * @param XGroupObjectDocument
      * @param xWikiContext the XWiki context.
      * @throws com.xpn.xwiki.XWikiException error when creating {@link Api}.
      */
     /*
     public Group(XGroupObjectDocument XGroupObjectDocument, XWikiContext xWikiContext) throws XWikiException
     {
         super(xWikiContext);
         this.groupName = XGroupObjectDocument.getSpace().replaceFirst(GROUP_SPACE_PREFIX, "");
         this.XGroupObjectDocument = XGroupObjectDocument;
         XWiki xWiki = xWikiContext.getWiki();
         WikiReference wikiReference = XGroupObjectDocument.getDocumentReference().getWikiReference();
         // groupXObjectDocument = groupSpace.spaceXObjectDocument
         this.groupSpace = new Space(
                 new XSpaceObjectDocument(
                         this.XGroupObjectDocument.getDocument(),
                         XSpaceClass.OBJECTID,
                         xWikiContext),
                 xWikiContext);
         this.collSpace = new Space(
                 new XSpaceObjectDocument(
                         xWiki.getDocument(
                                 new DocumentReference(
                                         XSpaceClass.PREFERENCES_NAME,
                                         new SpaceReference(GROUP_COLL_SPACE_PREFIX + this.groupName, wikiReference)),
                                 xWikiContext),
                         XSpaceClass.OBJECTID,
                         xWikiContext),
                 xWikiContext);
         this.documentationSpace = new Space(
                 new XSpaceObjectDocument(
                         xWiki.getDocument(
                                 new DocumentReference(
                                         XSpaceClass.PREFERENCES_NAME,
                                         new SpaceReference(GROUP_DOCUMENTATION_SPACE_PREFIX + this.groupName, wikiReference)),
                                 xWikiContext),
                         XSpaceClass.OBJECTID,
                         xWikiContext),
                 xWikiContext);
         this.invitationsSpace = new Space(
                 new XSpaceObjectDocument(
                         xWiki.getDocument(
                                 new DocumentReference(
                                         XSpaceClass.PREFERENCES_NAME,
                                         new SpaceReference(GROUP_INVITATIONS_SPACE_PREFIX + this.groupName, wikiReference)),
                                 xWikiContext),
                         XSpaceClass.OBJECTID,
                         xWikiContext),
                 xWikiContext);
         this.messagesSpace = new Space(
                 new XSpaceObjectDocument(
                         xWiki.getDocument(
                                 new DocumentReference(
                                         XSpaceClass.PREFERENCES_NAME,
                                         new SpaceReference(GROUP_MESSAGES_SPACE_PREFIX + this.groupName, wikiReference)),
                                 xWikiContext),
                         XSpaceClass.OBJECTID,
                         xWikiContext),
                 xWikiContext);
         this.userProfilesSpace = new Space(
                 new XSpaceObjectDocument(
                         xWiki.getDocument(
                                 new DocumentReference(
                                         XSpaceClass.PREFERENCES_NAME,
                                         new SpaceReference(GROUP_USERPROFILES_SPACE_PREFIX + this.groupName, wikiReference)),
                                 xWikiContext),
                         XSpaceClass.OBJECTID,
                         xWikiContext),
                 xWikiContext);
 
         this.adminGroup = new MembersGroup(
                 xWiki.getDocument(
                         new DocumentReference(ADMINGROUP_NAME, this.groupSpace.getSpaceReference()),
                         this.context),
                 this.context);
         this.memberGroup = new MembersGroup(
                 xWiki.getDocument(
                         new DocumentReference(MEMBERGROUP_NAME, this.groupSpace.getSpaceReference()),
                         this.context),
                 this.context);
 
         if (this.XGroupObjectDocument.isNew()) {
             // add current user to MemberGroup and AdminGroup
             this.memberGroup.addUserOrGroup(localEntityReferenceSerializer.serialize(this.context.getUserReference()));
             this.adminGroup.addUserOrGroup(localEntityReferenceSerializer.serialize(this.context.getUserReference()));
             // set dirty
             this.isDirty = true;
             this.isRightsDirty = true;
         }
     } */
 
     /**
      * return the name of a document. for exemple if the fullName of a document is "MySpace.Mydoc", the name is MyDoc.
      *
      * @return the name of the document
      */
     public String getName()
     {
         return this.groupSpaceReference.getName();
     }
 
     public String getTitle()
     {
         return this.groupObjectDocument.getTitle();
     }
 
     public void setTitle(String title)
     {
         this.groupObjectDocument.setTitle(title);
     }
 
     public String getDescription()
     {
         return this.groupObjectDocument.getDescription();
     }
 
     public void setDescription(String description)
     {
         this.groupObjectDocument.setDescription(description);
     }
 
     public String getUrlShortcut()
     {
         return this.groupObjectDocument.getUrlShortcut();
     }
 
     public void setUrlShortcut(String urlShortcut)
     {
         this.groupObjectDocument.setUrlShortcut(urlShortcut);
     }
 
     public String getLogo()
     {
         return this.groupObjectDocument.getLogo();
     }
 
     public void setLogo(String logo)
     {
         this.groupObjectDocument.setLogo(logo);
         this.isDirty = true;
     }
 
     public String getPolicy()
     {
         return this.groupObjectDocument.getPolicy();
     }
 
     public void setPolicy(String policy)
     {
         this.groupObjectDocument.setPolicy(policy);
         this.isDirty = true;
     }
 
     public String getAccessLevel()
     {
         return this.groupObjectDocument.getAccessLevel();
     }
 
     public void setAccessLevel(String accessLevel)
     {
         if (!StringUtils.equals(this.groupObjectDocument.getAccessLevel(), accessLevel)) {
             this.isRightsDirty = true;
         }
 
         this.groupObjectDocument.setAccessLevel(accessLevel);
         this.isDirty = true;
     }
 
     public String getLanguage()
     {
         return this.groupObjectDocument.getLanguage();
     }
 
     public void setLanguage(String language)
     {
         this.groupObjectDocument.setLanguage(language);
         this.isDirty = true;
     }
 
     public String getEducationSystem()
     {
         return this.groupObjectDocument.getEducationSystem();
     }
 
     public void setEducationSystem(String educationSystem)
     {
         this.groupObjectDocument.setEducationSystem(educationSystem);
         this.isDirty = true;
     }
 
     public List<String> getEducationalLevel()
     {
         return this.groupObjectDocument.getEducationalLevel();
     }
 
     public void setEducationalLevel(List<String> educationalLevel)
     {
         this.groupObjectDocument.setEducationalLevel(educationalLevel);
         this.isDirty = true;
     }
 
     public List<String> getDisciplines()
     {
         return  this.groupObjectDocument.getDisciplines();
     }
 
     public void setDisciplines(List<String> disciplines)
     {
         this.groupObjectDocument.setDisciplines(disciplines);
         this.isDirty = true;
     }
 
     public String getLicense()
     {
         return this.groupObjectDocument.getLicense();
     }
 
     public void setLicense(String license)
     {
         this.groupObjectDocument.setLicense(license);
         this.isDirty = true;
     }
 
     /**
      * Delete the space.
      *
      * @throws XWikiException error deleting the wiki.
      * @since 1.1
      */
     public void delete() throws XWikiException
     {
 
     }
 
     public boolean isNew()
     {
         return this.groupObjectDocument.getDocument().isNew();
     }
 
     public boolean isDirty()
     {
         return this.isDirty || this.groupObjectDocument.getDocument().isMetaDataDirty();
     }
 
     /*
     public void save() throws XWikiException
     {
         //if (this.isRightsDirty) {
         //    this.updateRights();
         //}
 
         // Save only dirty spaces
         if (this.groupSpace.isDirty()) {
             this.groupSpace.save();
         }
         if (this.documentationSpace.isDirty()) {
             this.documentationSpace.save();
         }
         if (this.invitationsSpace.isDirty()) {
             this.invitationsSpace.save();
         }
         if (this.messagesSpace.isDirty()) {
             this.invitationsSpace.save();
         }
         if (this.userProfilesSpace.isDirty()) {
             this.userProfilesSpace.save();
         }
 
         if (this.adminGroup.isDirty()) {
             this.adminGroup.save();
         }
 
         if (this.memberGroup.isDirty()) {
             this.memberGroup.save();
         }
 
 
         this.isDirty = false;
     }*/
 
     public SpaceReference getGroupSpaceReference()
     {
         return this.groupSpaceReference;
     }
 
     public SpaceReference getInvitationsSpaceReference()
     {
         return this.invitationsSpaceReference;
     }
 
     public SpaceReference getUserProfilesSpaceReference()
     {
         return this.userProfilesSpaceReference;
     }
 
     public DocumentReference getMembersGroupDocumentReference()
     {
         return new DocumentReference("MemberGroup", this.groupSpaceReference);
     }
 
     public DocumentReference getAdminMembersGroupReference()
     {
         return new DocumentReference("AdminGroup", this.groupSpaceReference);
     }
 
     public void updateFromRequest() throws XWikiException
     {
         //this.groupSpace.updateFromRequest();
         //this.XGroupObjectDocument
                 //.updateObjectFromRequest(this.XGroupObjectDocument.getXClassManager().getClassFullName());
     }
 
     /*
     public MembersGroup getMemberGroup()
     {
         return this.memberGroup;
     }
 
     public MembersGroup getAdminGroup()
     {
         return this.adminGroup;
     }
 
     public List<String> getMembers() throws XWikiException
     {
         return this.memberGroup.getAllMembers();
     }*/
 
     public boolean addMemberRole(String member, String role) throws XWikiException
     {
         /*
         if (!this.isAdmin())
             return false;
 
         if (!this.memberGroup.isMember(member))
             return false;
 
         if (!Arrays.asList(this.MEMBER_ROLES).contains(role))
             return false;
 
         MembersGroup roleMembersGroup = new MembersGroup(
                 this.context.getWiki().getDocument(
                         new DocumentReference("Role_"+role+"Group", this.groupSpace.getSpaceReference()),
                         this.context)
                 ,this.context);
 
         roleMembersGroup.addUserOrGroup(member); */
 
         return true;
     }
 
     public boolean removeMemberRole(String member, String role) throws XWikiException
     {
         /*
         if (!this.isAdmin())
             return false;
 
         MembersGroup roleMembersGroup = new MembersGroup(
                 this.context.getWiki().getDocument(
                         new DocumentReference("Role_"+role+"Group", this.groupSpace.getSpaceReference()),
                         this.context),
                 this.context);
 
         return roleMembersGroup.removeUserOrGroup(member);    */
 
         return true;
     }
 
     public boolean addMember(String member) throws XWikiException
     {
         /*
         this.memberGroup.addUserOrGroup(member);
         // create user profile
         XUserProfile XUserProfile = XUserProfileClass.getInstance(this.getXWikiContext())
                 .newXObjectDocument(new DocumentReference(member, this.userProfilesSpace.getSpaceReference()));
         XUserProfile.setProfile("User profile description");
         XUserProfile.setAllowNotifications(true);
         XUserProfile.setAllowSelfNotifications(true);
         XUserProfile.save();
 
         // notify admins
         List<String> admins = this.adminGroup.getAllMembers(); */
 
         return true;
     }
 
     public boolean removeMember(String member) throws XWikiException
     {
         boolean removed = true;
         /*
         if(this.adminGroup.isMember(member))
             removed = this.adminGroup.removeUserOrGroup(member);
 
         if(removed) {
             removed = this.memberGroup.removeUserOrGroup(member);
             // delete user profile
             XUserProfile XUserProfile = XUserProfileClass.getInstance(this.getXWikiContext())
                     .newXObjectDocument(new DocumentReference(member, this.userProfilesSpace.getSpaceReference()));
             XUserProfile.delete();
         }  */
 
         return removed;
     }
 
     public boolean inviteMember(String member)
     {
         return false;
     }
 
     private void updateRights() throws XWikiException
     {
         /*
         //DocumentReference xWikiAdminGroupReference =
         //        currentReferenceDocumentReferenceResolver.resolve(XWIKIADMINGROUP_REFERENCE);
         //DocumentReference xWikiAllGroupReference =
         //        currentReferenceDocumentReferenceResolver.resolve(XWIKIALLGROUP_REFERENCE);
         DocumentReference adminGroup = new DocumentReference(ADMINGROUP_NAME, this.groupSpace.getSpaceReference());
         DocumentReference memberGroup = new DocumentReference(MEMBERGROUP_NAME, this.groupSpace.getSpaceReference());
 
         this.groupSpace.setAccessLevel(adminGroup, RIGHTS_LEVELS_EDIT, true);
 
         if (StringUtils.equals(this.XGroupObjectDocument.getAccessLevel(), XGroupClass.FIELD_ACCESS_LEVEL_PUBLIC)) {
             this.groupSpace.setAccessLevel(adminGroup, RIGHTS_LEVELS_EDIT, true);
         }
 
         if (StringUtils.equals(this.XGroupObjectDocument.getAccessLevel(), XGroupClass.FIELD_ACCESS_LEVEL_PROTECTED)) {
             this.groupSpace.setAccessLevel(adminGroup, RIGHTS_LEVELS_EDIT, true);
             this.collSpace.setAccessLevel(memberGroup, RIGHTS_LEVELS_EDIT, true);
         }
 
         if (StringUtils.equals(this.XGroupObjectDocument.getAccessLevel(), XGroupClass.FIELD_ACCESS_LEVEL_PRIVATE)) {
             this.groupSpace.setAccessLevel(adminGroup, RIGHTS_LEVELS_EDIT, true);
             this.collSpace.setAccessLevel(memberGroup, RIGHTS_LEVELS_VIEW, true);
             this.collSpace.setAccessLevel(memberGroup, RIGHTS_LEVELS_EDIT, true);
         }
 
         this.groupSpace.getXSpaceObjectDocument().getDocument().setMetaDataDirty(true);
         this.collSpace.getXSpaceObjectDocument().getDocument().setMetaDataDirty(true); */
     }
 
     public boolean isAdmin() throws XWikiException
     {
         //return this.adminGroup.isMember(localEntityReferenceSerializer.serialize(this.context.getUserReference()));
         return false;
     }
 
     public boolean isAdmin(String user) throws XWikiException
     {
         //return this.adminGroup.isMember(user);
         return false;
     }
 
     public boolean isMember() throws XWikiException
     {
         //return this.memberGroup.isMember(localEntityReferenceSerializer.serialize(this.context.getUserReference()));
         return false;
     }
 
     public boolean isMember(String user) throws XWikiException
     {
         //return this.memberGroup.isMember(user);
         return false;
     }
 
     public String display(String fieldname)
     {
        Document document = new Document(this.groupObjectDocument.getDocument(), getXWikiContext());
        return document.display(fieldname);
     }
 
     public String display(String fieldname, String mode)
     {
        Document document = new Document(this.groupObjectDocument.getDocument(), getXWikiContext());
        return document.display(fieldname, mode);
     }
 
     public Date getCreationDate()
     {
        return this.groupObjectDocument.getDocument().getCreationDate();
     }
 
     public String getHomeURL()
     {
         return getXWikiContext().getWiki().getURL(
                 new DocumentReference(WEBHOME_PAGENAME, groupSpaceReference), ACTION_VIEW, getXWikiContext());
     }
 
     public String getHomeURL(String queryString, String anchor)
     {
         return getXWikiContext().getWiki().getURL(
                 new DocumentReference(WEBHOME_PAGENAME, groupSpaceReference),
                 ACTION_VIEW,
                 queryString,
                 anchor,
                 getXWikiContext());
     }
 
     public String getJoinURL(String queryString, String anchor)
     {
         return getXWikiContext().getWiki().getURL(
                 new DocumentReference(JOINGROUP_PAGENAME, groupSpaceReference),
                 ACTION_VIEW,
                 queryString,
                 anchor,
                 getXWikiContext());
     }
 
     public String getJoinURL()
     {
         return getXWikiContext().getWiki().getURL(
                 new DocumentReference(JOINGROUP_PAGENAME, groupSpaceReference), ACTION_VIEW, getXWikiContext());
     }
 
     public Document getHomeDocument() throws XWikiException
     {
         return new Document(getXWikiContext().getWiki().getDocument(
                 new DocumentReference(WEBHOME_PAGENAME, groupSpaceReference), getXWikiContext()), getXWikiContext());
     }
 
     public List<Property> getMetadata()
             throws XWikiException
     {
         return this.groupObjectDocument.getProperties();
     }
 
     public void save()
             throws XWikiException
     {
         this.groupObjectDocument.save();
     }
 
     @Override
     public String toString()
     {
         return this.groupSpaceReference.getName();
     }
 }
