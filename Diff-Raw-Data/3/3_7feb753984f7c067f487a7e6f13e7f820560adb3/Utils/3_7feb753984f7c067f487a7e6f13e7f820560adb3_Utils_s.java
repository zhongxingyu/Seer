 /*
  * Copyright (C) 2003-2011 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.wcm.ext.component.activity.listener;
 
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jcr.Node;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.exoplatform.commons.utils.ISO8601;
 import org.exoplatform.container.ExoContainer;
 import org.exoplatform.container.ExoContainerContext;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.services.cms.BasePath;
 import org.exoplatform.services.cms.link.LinkManager;
 import org.exoplatform.services.jcr.core.ManageableRepository;
 import org.exoplatform.services.jcr.ext.common.SessionProvider;
 import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
 import org.exoplatform.services.jcr.impl.core.NodeImpl;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.security.ConversationState;
 import org.exoplatform.services.wcm.core.NodeLocation;
 import org.exoplatform.services.wcm.core.NodetypeConstant;
 import org.exoplatform.services.wcm.core.WebSchemaConfigService;
 import org.exoplatform.services.wcm.utils.WCMCoreUtils;
 import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
 import org.exoplatform.social.core.activity.model.ExoSocialActivity;
 import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
 import org.exoplatform.social.core.identity.model.Identity;
 import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
 import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
 import org.exoplatform.social.core.manager.ActivityManager;
 import org.exoplatform.social.core.manager.IdentityManager;
 import org.exoplatform.social.core.space.spi.SpaceService;
 import org.exoplatform.wcm.ext.component.activity.ContentUIActivity;
 import org.exoplatform.wcm.ext.component.activity.ContentUIActivityBuilder;
 
 /**
  * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Mar
  * 18, 2011
  */
 public class Utils {
   
   private static final Log log = ExoLogger.getLogger(Utils.class);
 
   /** The Constant Activity Type */
   private static final String CONTENT_SPACES = "contents:spaces";
   
   /** the publication:currentState property name */
   private static final String CURRENT_STATE_PROP = "publication:currentState";
   
   /**the exo:action property name */
   private static final String EXO_ACTION = "exo:action";
 
   /**the exo:template property name */
   private static final String EXO_TEMPLATE = "exo:template";
 
 
   /**
    * Populate activity data with the data from Node
    * 
    * @param Node the node
    * @param String the message of the activity
    * @return Map the mapped data
    */
   public static Map<String, String> populateActivityData(Node node, String activityOwnerId, String activityMsgBundleKey) throws Exception {
     /** The date formatter. */
     DateFormat dateFormatter = null;
     dateFormatter = new SimpleDateFormat(ISO8601.SIMPLE_DATETIME_FORMAT);
     
     //get activity data
     String repository = ((ManageableRepository) node.getSession().getRepository()).getConfiguration().getName();
     String workspace = node.getSession().getWorkspace().getName();
     String state = node.hasProperty(CURRENT_STATE_PROP) ? node.getProperty(CURRENT_STATE_PROP).getValue().getString() : "";
     String illustrationImg = Utils.getIllustrativeImage(node);
     String strDateCreated = "";
     if (node.hasProperty(NodetypeConstant.EXO_DATE_CREATED)) {
       Calendar dateCreated = node.getProperty(NodetypeConstant.EXO_DATE_CREATED).getDate();
       strDateCreated = dateFormatter.format(dateCreated.getTime());
     }
     String strLastModified = "";
     if (node.hasNode(NodetypeConstant.JCR_CONTENT)) {
       Node contentNode = node.getNode(NodetypeConstant.JCR_CONTENT);
       if (contentNode.hasProperty(NodetypeConstant.JCR_LAST_MODIFIED)) {
         Calendar lastModified = contentNode.getProperty(NodetypeConstant.JCR_LAST_MODIFIED).getDate();
         strLastModified = dateFormatter.format(lastModified.getTime());
       }
     }
     
     activityOwnerId = activityOwnerId != null ? activityOwnerId : "";
     
     //populate data to map object
     Map<String, String> activityParams = new HashMap<String, String>();
     activityParams.put(ContentUIActivity.CONTENT_NAME, node.getName());
     activityParams.put(ContentUIActivity.STATE, state);
     activityParams.put(ContentUIActivity.AUTHOR, activityOwnerId);
     activityParams.put(ContentUIActivity.DATE_CREATED, strDateCreated);
     activityParams.put(ContentUIActivity.LAST_MODIFIED, strLastModified);
     activityParams.put(ContentUIActivity.CONTENT_LINK, getContentLink(node));
     activityParams.put(ContentUIActivity.ID, node.isNodeType(NodetypeConstant.MIX_REFERENCEABLE) ? node.getUUID() : "");
     activityParams.put(ContentUIActivity.REPOSITORY, repository);
     activityParams.put(ContentUIActivity.WORKSPACE, workspace);
     activityParams.put(ContentUIActivity.MESSAGE, activityMsgBundleKey);
     activityParams.put(ContentUIActivity.MIME_TYPE, getMimeType(node));
     activityParams.put(ContentUIActivity.IMAGE_PATH, illustrationImg);
     activityParams.put(ContentUIActivity.IMAGE_PATH, illustrationImg);
 
     return activityParams;
   }
   
   /**
    * post activity to the activity stream
    * 
    * @param String the activity invoker
    * @param node the node
    * @return void
    */
   public static void postActivity(Node node, String activityMsgBundleKey) throws Exception {
     if (!isSupportedContent(node)) {
       return;
     }
     
     // get services
     ExoContainer container = ExoContainerContext.getCurrentContainer();
     ActivityManager activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
     IdentityManager identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
     
     SpaceService spaceService = WCMCoreUtils.getService(SpaceService.class);
 
     //refine to get the valid node
     refineNode(node);
     
     //get owner
     String activityOwnerId = getActivityOwnerId();
     
     ExoSocialActivity activity = createActivity(identityManager, activityOwnerId, node, activityMsgBundleKey);
     String spaceName = getSpaceName(node);
 
     if (spaceName != null && spaceName.length() > 0 && spaceService.getSpaceByPrettyName(spaceName) != null) { 
       // post activity to space stream
       Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceName, true);
       activityManager.saveActivityNoReturn(spaceIdentity, activity);
     } else if (activityOwnerId != null && activityOwnerId.length() > 0) {
       // post activity to user status stream
       Identity ownerIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, activityOwnerId, true);
       activityManager.saveActivityNoReturn(ownerIdentity, activity);
     } else {
       return;
     }
 
     //TODO: At the moment, we are waiting for social team to support a mechanism for extending the social streams. 
     //(we want add one more stream named Document to manage the change of documents)    
 //    //save with DocumentIdentity
 //    String workspace = node.getSession().getWorkspace().getName();
 //    String nodeUUID = node.isNodeType(NodetypeConstant.MIX_REFERENCEABLE) ? node.getUUID() : "";
 //    Identity docIdentity = identityManager.getOrCreateIdentity(DocumentIdentityProvider.NAME, workspace + ":/" + nodeUUID, true);
 //    activityManager.saveActivityNoReturn(docIdentity, activity);
 
     ContentUIActivityBuilder contentUIActivityBuilder = new ContentUIActivityBuilder();
     contentUIActivityBuilder.populateData(new ContentUIActivity(), activity);
   }
   
   /**
    * check the nodes that we support to post activities
    * @param node for checking
    * @return result of checking
    * @throws RepositoryException
    */
   private static boolean isSupportedContent(Node node) throws Exception {
     if (getActivityOwnerId() != null && getActivityOwnerId().length() > 0) {
       NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
       SessionProvider sessionProvider = WCMCoreUtils.getUserSessionProvider();
       Node userNode = nodeHierarchyCreator.getUserNode(sessionProvider, getActivityOwnerId());
       if (userNode != null && node.getPath().startsWith(userNode.getPath() + "/Private/")) {
         return false;
       }
     }
     
     return true;
   }
   
   /**
    * refine node for validation
    * @param currentNode
    * @throws Exception
    */
   private static void refineNode(Node currentNode) throws Exception {
     Session session = currentNode.getSession();
     String nodePath = currentNode.getPath();
     currentNode.getSession().save();
 
     if (currentNode instanceof NodeImpl && !((NodeImpl) currentNode).isValid()) {
       currentNode = (Node) session.getItem(nodePath);
       ExoContainer container = ExoContainerContext.getCurrentContainer();
       LinkManager linkManager = (LinkManager) container.getComponentInstanceOfType(LinkManager.class);
       if (linkManager.isLink(currentNode)) {
         try {
           currentNode = linkManager.getTarget(currentNode, false);
         } catch (Exception ex) {
           currentNode = linkManager.getTarget(currentNode, true);
         }
       }
     }
   }
   
   /**
    * get activity owner
    * @return activity owner
    */
   private static String getActivityOwnerId () {
     String activityOwnerId = "";
     ConversationState conversationState = ConversationState.getCurrent();
     if (conversationState != null) {
       activityOwnerId = conversationState.getIdentity().getUserId();
     }
     return activityOwnerId;
   }
   
   /**
    * get the space name of node
    * @param node
    * @return
    * @throws Exception
    */
   private static String getSpaceName (Node node) throws Exception {
 	NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
 	String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
 	String spacesFolder = groupPath + "/spaces/";
 	String spaceName = "";
 	String nodePath = node.getPath();
 	if (nodePath.startsWith(spacesFolder)) {
 		spaceName = nodePath.substring(spacesFolder.length());
 		spaceName = spaceName.substring(0, spaceName.indexOf("/"));
 	}
 	
     return spaceName;
   }
 
   /**
    * Generate the viewer link to site explorer by node
    * 
    * @param Node the node
    * @return String the viewer link
    * @throws RepositoryException 
    */
   public static String getContentLink(Node node) throws RepositoryException {
       String repository = ((ManageableRepository) node.getSession().getRepository()).getConfiguration().getName();
       String workspace = node.getSession().getWorkspace().getName();
       return repository + '/' + workspace + node.getPath();
   }
   
   /**
    * Create ExoSocialActivity
    * 
    * @param IdentityManager the identity Manager
    * @param String the remote user name
    * @return the ExoSocialActivity
    * @throws Exception the activity storage exception
    */
   public static ExoSocialActivity createActivity(IdentityManager identityManager, String activityOwnerId, Node node, String activityMsgBundleKey) throws Exception {
     
     // Populate activity data
     Map<String, String> activityParams = populateActivityData(node, activityOwnerId, activityMsgBundleKey);
     String title = node.hasProperty(NodetypeConstant.EXO_TITLE) ? node.getProperty(NodetypeConstant.EXO_TITLE).getString()
                                                  : node.getProperty(NodetypeConstant.EXO_TITLE).getString();
     
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     activity.setType(CONTENT_SPACES);
     activity.setUrl(node.getPath());
     activity.setTitle(title);
     activity.setTemplateParams(activityParams);
     return activity;
   }
 
   /**
    * Gets the illustrative image.
    * 
    * @param node the node
    * @return the illustrative image
    */
   public static String getIllustrativeImage(Node node) {
     WebSchemaConfigService schemaConfigService = WCMCoreUtils.getService(WebSchemaConfigService.class);
     WebContentSchemaHandler contentSchemaHandler = schemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
     Node illustrativeImage = null;
     String uri = "";
     try {
       illustrativeImage = contentSchemaHandler.getIllustrationImage(node);
       uri = generateThumbnailImageURI(illustrativeImage);
     } catch (PathNotFoundException ex) {
       return uri;
     } catch (Exception e) {
       log.warn(e.getMessage(), e);
     }
     return uri;
   }
 
   /**
    * Generate the Thumbnail Image URI.
    * 
    * @param node the node
    * @return the Thumbnail uri with medium size
    * @throws Exception the exception
    */
   public static String generateThumbnailImageURI(Node file) throws Exception {
     StringBuilder builder = new StringBuilder();
     NodeLocation fielLocation = NodeLocation.getNodeLocationByNode(file);
     String repository = fielLocation.getRepository();
     String workspaceName = fielLocation.getWorkspace();
     String nodeIdentifiler = file.getPath().replaceFirst("/", "");
     String portalName = PortalContainer.getCurrentPortalContainerName();
     String restContextName = PortalContainer.getCurrentRestContextName();
     InputStream stream = file.getNode(NodetypeConstant.JCR_CONTENT).getProperty(NodetypeConstant.JCR_DATA).getStream();
     if (stream.available() == 0)
       return null;
     stream.close();
     builder.append("/")
            .append(portalName)
            .append("/")
            .append(restContextName)
            .append("/")
            .append("thumbnailImage/medium/")
            .append(repository)
            .append("/")
            .append(workspaceName)
            .append("/")
           .append(nodeIdentifiler)
           .append("/?reloadnum=" + Math.random());
     return builder.toString();
   }
 
   /**
    * Get the MimeType
    * 
    * @param node the node
    * @return the MimeType
    */
   public static String getMimeType(Node node) {
     try {
       if (node.getPrimaryNodeType().getName().equals(NodetypeConstant.NT_FILE)) {
 
         if (node.hasNode(NodetypeConstant.JCR_CONTENT))
           return node.getNode(NodetypeConstant.JCR_CONTENT).getProperty(NodetypeConstant.JCR_MIME_TYPE).getString();
       }
     } catch (Exception e) {
       log.error(e.getMessage(), e);
     }
     return "";
   }  
 }
