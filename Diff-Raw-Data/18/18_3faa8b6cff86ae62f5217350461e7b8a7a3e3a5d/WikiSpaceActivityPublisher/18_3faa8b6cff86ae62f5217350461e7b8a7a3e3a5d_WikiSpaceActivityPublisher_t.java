 package org.exoplatform.wiki.ext.impl;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.portal.config.model.PortalConfig;
 import org.exoplatform.services.jcr.access.PermissionType;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.security.ConversationState;
 import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;
 import org.exoplatform.social.core.activity.model.ExoSocialActivity;
 import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
 import org.exoplatform.social.core.identity.model.Identity;
 import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
 import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
 import org.exoplatform.social.core.manager.ActivityManager;
 import org.exoplatform.social.core.manager.IdentityManager;
 import org.exoplatform.social.core.space.model.Space;
 import org.exoplatform.social.core.space.spi.SpaceService;
 import org.exoplatform.social.core.storage.SpaceStorageException;
 import org.exoplatform.wiki.mow.api.Page;
 import org.exoplatform.wiki.mow.api.WikiNodeType;
 import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
 import org.exoplatform.wiki.rendering.RenderingService;
 import org.exoplatform.wiki.service.listener.PageWikiListener;
 import org.exoplatform.wiki.utils.Utils;
 import org.xwiki.rendering.syntax.Syntax;
 
 public class WikiSpaceActivityPublisher extends PageWikiListener {
 
   public static final String WIKI_APP_ID       = "ks-wiki:spaces";
 
   public static final String ACTIVITY_TYPE_KEY = "act_key";
 
   public static final String ADD_PAGE_TYPE     = "add_page";
 
   public static final String UPDATE_PAGE_TYPE  = "update_page";
 
   public static final String PAGE_ID_KEY       = "page_id";
 
   public static final String PAGE_TYPE_KEY     = "page_type";
 
   public static final String PAGE_OWNER_KEY    = "page_owner";
 
   public static final String PAGE_TITLE_KEY    = "page_name";
 
   public static final String URL_KEY           = "page_url";
   
   public static final String PAGE_EXCERPT      = "page_exceprt";
   
   public static final String VIEW_CHANGE_URL_KEY = "view_change_url";
   
   public static final String VIEW_CHANGE_ANCHOR  = "#CompareRevision/changes";  
   
   public static final String WIKI_PAGE_NAME      = "wiki";
 
   private static final int   EXCERPT_LENGTH    = 140;
 
   private static final Log   LOG               = ExoLogger.getExoLogger(WikiSpaceActivityPublisher.class);
 
 
   public WikiSpaceActivityPublisher() {}
 
   private ExoSocialActivity activity(Identity ownerIdentity, String wikiType, String wikiOwner, String pageId, Page page, String spaceUrl, String activityType) throws Exception {
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     activity.setUserId(ownerIdentity.getId());
     activity.setTitle("title");
     activity.setBody("body");
     activity.setType(WIKI_APP_ID);
     Map<String, String> templateParams = new HashMap<String, String>();
     templateParams.put(PAGE_ID_KEY, pageId);
     templateParams.put(ACTIVITY_TYPE_KEY, activityType);
     templateParams.put(PAGE_OWNER_KEY, wikiOwner);
     templateParams.put(PAGE_TYPE_KEY, wikiType);
     templateParams.put(PAGE_TITLE_KEY, page.getTitle());    
     String pageURL = (page.getURL() == null) ? (spaceUrl != null ? (spaceUrl + "/" + WIKI_PAGE_NAME) : "") : page.getURL();
     templateParams.put(URL_KEY, pageURL);
     
     String excerpt = StringUtils.EMPTY;
     if (ADD_PAGE_TYPE.equals(activityType)) {
       RenderingService renderingService = (RenderingService) PortalContainer.getInstance()
         .getComponentInstanceOfType(RenderingService.class);
       excerpt = renderingService.render(page.getContent().getText(), page.getSyntax(), Syntax.PLAIN_1_0.toIdString(), false);
     } else {
       String verName = ((PageImpl) page).getVersionableMixin().getBaseVersion().getName();
       //templateParams.put(VIEW_CHANGE_URL_KEY, page.getURL() + "?action=CompareRevision&verName=" + verName);
       templateParams.put(VIEW_CHANGE_URL_KEY, Utils.getURL(page.getURL(), verName));
       excerpt = page.getComment();
     }
     excerpt = (excerpt.length() > EXCERPT_LENGTH) ? excerpt.substring(0, EXCERPT_LENGTH) + "..." : excerpt;
     templateParams.put(PAGE_EXCERPT, excerpt);
     activity.setTemplateParams(templateParams);
     return activity;
   }
   
   private boolean isPublic(Page page) throws Exception {
     HashMap<String, String[]> permissions = page.getPermission();
     // the page is public when it has permission: [any read]
     return permissions != null && permissions.containsKey(IdentityConstants.ANY) && ArrayUtils.contains(permissions.get(IdentityConstants.ANY), PermissionType.READ);
   }
   
  /**
   * Check If a page can be read by all users of a space
   * 
   * @param page Page
   * @param space Space
   * @return true : can, false : not can;
   * @throws Exception
   */
  private boolean isPublicInSpace(Page page, Space space) throws Exception {
    HashMap<String, String[]> pagePermissions = page.getPermission();
    String groupMemberShip = MembershipEntry.ANY_TYPE + ":" + space.getGroupId();
    return (pagePermissions.containsKey(groupMemberShip) && ArrayUtils.contains(pagePermissions.get(groupMemberShip), PermissionType.READ));
  }
  
   private void saveActivity(String wikiType, String wikiOwner, String pageId, Page page, String addType) throws Exception {
     try {
       Class.forName("org.exoplatform.social.core.space.spi.SpaceService");
     } catch (ClassNotFoundException e) {
       if (LOG.isDebugEnabled()) {
         LOG.debug("eXo Social components not found!", e);
       }
       return;
     }
     
     String username = ConversationState.getCurrent().getIdentity().getUserId();
     
     IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
     ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
     Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
     
     Identity ownerStream = null, authorActivity = userIdentity;
     ExoSocialActivity activity = null;
     String spaceUrl = null;
     
     if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
       /* checking whether the page is in a space */
       String groupId = "/" + wikiOwner;
       SpaceService spaceService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
       Space space = null;
       try {
         space = spaceService.getSpaceByGroupId(groupId);
         if (space != null) {
          if (!isPublicInSpace(page, space)) return;
           ownerStream = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
           spaceUrl = space.getUrl();
         }
       } catch (SpaceStorageException e) {
         if (LOG.isDebugEnabled()) {
           LOG.debug(String.format("Space %s not existed", groupId), e);
         }
       }
     }
     
     if (ownerStream == null && isPublic(page)) {
       // if the page is public, publishing the activity in the user stream.
       ownerStream = userIdentity;
     }
     
     if (ownerStream != null) {
       activity = activity(authorActivity, wikiType, wikiOwner, pageId, page, spaceUrl, addType);
       activityM.saveActivityNoReturn(ownerStream, activity);
     }
   }
 
   @Override
   public void postAddPage(String wikiType, String wikiOwner, String pageId, Page page) throws Exception {
     if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId)) {
       // catch the case of the Wiki Home added as it's created by the system, not by users.
       return;
     }
     saveActivity(wikiType, wikiOwner, pageId, page, ADD_PAGE_TYPE);
   }
 
   @Override
   public void postDeletePage(String wikiType, String wikiOwner, String pageId, Page page) {
 
   }
 
   @Override
   public void postUpdatePage(String wikiType, String wikiOwner, String pageId, Page page) throws Exception {
     if(page != null && !((PageImpl)page).isMinorEdit()) {
       saveActivity(wikiType, wikiOwner, pageId, page, UPDATE_PAGE_TYPE);
     }
   }
 }
