 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.social;
 
 import org.apache.commons.lang.StringUtils;
 import org.jahia.api.Constants;
 import org.jahia.services.content.*;
 import org.jahia.services.usermanager.JahiaGroupManagerService;
 import org.jahia.services.usermanager.JahiaUser;
 import org.jahia.services.usermanager.JahiaUserManagerService;
 import org.jahia.services.usermanager.jcr.JCRUser;
 import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
 import org.jahia.services.workflow.WorkflowService;
 import org.jahia.services.workflow.WorkflowVariable;
 import org.slf4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.BeanPostProcessor;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import java.util.*;
 
 /**
  * Social service class for manipulating social activities data.
  *
  * @author Serge Huber
  */
 public class SocialService implements BeanPostProcessor {
 
     private static Logger logger = org.slf4j.LoggerFactory.getLogger(SocialService.class);
     public static final String JNT_BASE_SOCIAL_ACTIVITY = "jnt:baseSocialActivity";
     public static final String JNT_SOCIAL_MESSAGE = "jnt:socialMessage";
     public static final String JNT_SOCIAL_CONNECTION = "jnt:socialConnection";
     private static final Comparator<? super JCRNodeWrapper> ACTIVITIES_COMPARATOR = new Comparator<JCRNodeWrapper>() {
 
         public int compare(JCRNodeWrapper activityNode1, JCRNodeWrapper activityNode2) {
             try {
                 // we invert the order to sort with most recent dates on top.
                 return activityNode2.getProperty("jcr:created").getDate().compareTo(activityNode1.getProperty("jcr:created").getDate());
             } catch (RepositoryException e) {
                 logger.error("Error while comparing creation date on two activities, returning them as equal", e);
                 return 0;
             }
         }
 
     };
 
     private static final String JMIX_AUTOPUBLISH = "jmix:autoPublish";
 
     private String autoSplitSettings;
     private JCRUserManagerProvider jcrUserManager;
     private JahiaGroupManagerService groupManagerService;
     private JahiaUserManagerService userManagerService;
     private WorkflowService workflowService;
     private JCRContentUtils jcrContentUtils;
 
     private Map<String, ActivityRecorder> activityRecorderMap = new LinkedHashMap<String, ActivityRecorder>();
 
     public void addActivity(final String userKey, final String message, JCRSessionWrapper session) throws RepositoryException {
         addActivity(userKey, null, "text", session, message);
     }
 
     public JCRNodeWrapper addActivity(final String userKey, final JCRNodeWrapper targetNode, String activityType, JCRSessionWrapper session, Object... args) throws RepositoryException {
         if (userKey == null || "".equals(userKey.trim())) {
             throw new ConstraintViolationException();
         }
         final JCRUser fromJCRUser = getJCRUserFromUserKey(userKey);
         if (fromJCRUser == null) {
             logger.warn("No user found, not adding activity !");
             throw new ConstraintViolationException();
         }
         JCRNodeWrapper userNode = fromJCRUser.getNode(session);
 
         JCRNodeWrapper activitiesNode = getActivitiesNode(session, userNode);
 
         if (activityRecorderMap.containsKey(activityType)) {
             ActivityRecorder activityRecorder = activityRecorderMap.get(activityType);
             String nodeType = activityRecorder.getNodeTypeForActivity(activityType);
            String nodeName = jcrContentUtils.generateNodeName(activitiesNode, nodeType);
             JCRNodeWrapper activityNode = activitiesNode.addNode(nodeName, nodeType);
             if (targetNode!=null) {
                 activityNode.setProperty("j:targetNode", targetNode.getPath());
             }
             activityNode.setProperty("j:activityType", activityType);
             activityRecorder.recordActivity(activityNode, activityType, userKey, targetNode, session, args);
 
             return activityNode;
         }
 
         throw new NoSuchNodeTypeException();
     }
 
     private JCRNodeWrapper getActivitiesNode(JCRSessionWrapper session, JCRNodeWrapper userNode) throws RepositoryException {
         JCRNodeWrapper activitiesNode;
         try {
             activitiesNode = userNode.getNode("activities");
             if (!activitiesNode.isNodeType("jnt:activitiesList")) {
                 activitiesNode.remove();
                 session.save();
                 throw new PathNotFoundException();
             }
             session.checkout(activitiesNode);
         } catch (PathNotFoundException pnfe) {
             session.checkout(userNode);
             activitiesNode = userNode.addNode("activities", "jnt:activitiesList");
             activitiesNode.addMixin(JMIX_AUTOPUBLISH);
             if (autoSplitSettings != null) {
                 activitiesNode.addMixin(Constants.JAHIAMIX_AUTOSPLITFOLDERS);
                 activitiesNode.setProperty(Constants.SPLIT_CONFIG, autoSplitSettings);
                 activitiesNode.setProperty(Constants.SPLIT_NODETYPE, "jnt:activitiesList");
             }
         }
         return activitiesNode;
     }
 
 
     public boolean sendMessage(final String fromUserKey, final String toUserKey, final String subject, final String body) throws RepositoryException {
         return execute(new JCRCallback<Boolean>() {
             public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                 return sendMessage(fromUserKey, toUserKey, subject, body, session);
             }
         });
     }
 
     public boolean sendMessage(String fromUserKey, String toUserKey, final String subject, final String body, JCRSessionWrapper session) throws RepositoryException {
         JCRUser fromJCRUser = getJCRUserFromUserKey(fromUserKey);
         if (fromJCRUser == null) {
             logger.warn("Couldn't find from user " + fromUserKey + " , aborting message sending...");
             return false;
         }
         JCRUser jcrUser = getJCRUserFromUserKey(toUserKey);
 
         if (jcrUser == null) {
             logger.warn("Couldn't find to user " + toUserKey + " , aborting message sending...");
             return false;
         }
 
         final String fromUserIdentifier = fromJCRUser.getIdentifier();
         final String toUserIdentifier = jcrUser.getIdentifier();
 
         sendMessageInternal(fromUserIdentifier, toUserIdentifier, subject, body, session);
         return true;
     }
 
     private void sendMessageInternal(final String fromUserIdentifier, final String toUserIdentifier, final String subject, final String body, JCRSessionWrapper session) throws RepositoryException {
 
         JCRNodeWrapper fromUser = session.getNodeByUUID(fromUserIdentifier);
         JCRNodeWrapper toUser = session.getNodeByUUID(toUserIdentifier);
         // now let's connect this user's node to the target node.
 
         // now let's do the connection in the other direction.
         JCRNodeWrapper destinationInboxNode = JCRContentUtils.getOrAddPath(session, toUser, "messages/inbox",
                 Constants.JAHIANT_CONTENTLIST);
         String destinationInboxNodeName = JCRContentUtils.findAvailableNodeName(destinationInboxNode,
                 fromUser.getName() + "_to_" + toUser.getName());
         JCRNodeWrapper destinationMessageNode = destinationInboxNode.addNode(destinationInboxNodeName,
                 JNT_SOCIAL_MESSAGE);
 //        destinationMessageNode.setProperty("j:from", fromUser);
         destinationMessageNode.setProperty("j:to", toUser);
         destinationMessageNode.setProperty("j:subject", subject);
         destinationMessageNode.setProperty("j:body", body);
         destinationMessageNode.setProperty("j:read", false);
 
         JCRNodeWrapper sentMessagesBoxNode = JCRContentUtils.getOrAddPath(session, fromUser, "messages/sent",
                 Constants.JAHIANT_CONTENTLIST);
         String sentMessagesBoxNodeName = JCRContentUtils.findAvailableNodeName(sentMessagesBoxNode, fromUser.getName()
                 + "_to_" + toUser.getName());
         JCRNodeWrapper sentMessageNode = sentMessagesBoxNode.addNode(sentMessagesBoxNodeName, JNT_SOCIAL_MESSAGE);
 //        sentMessageNode.setProperty("j:from", fromUser);
         sentMessageNode.setProperty("j:to", toUser);
         sentMessageNode.setProperty("j:subject", subject);
         sentMessageNode.setProperty("j:body", body);
         sentMessageNode.setProperty("j:read", false);
 
         session.save();
     }
 
     public Set<String> getUserConnections(final String userNodePath, final boolean includeSelf)
             throws RepositoryException {
         final Set<String> userPaths = new HashSet<String>();
 
         execute(new JCRCallback<Boolean>() {
             public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                 QueryManager queryManager = session.getWorkspace().getQueryManager();
 
                 if (includeSelf) {
                     userPaths.add(userNodePath);
                 }
 
                 Query myConnectionsQuery = queryManager.createQuery("select * from [" + JNT_SOCIAL_CONNECTION
                         + "] as uC where isdescendantnode(uC,['" + userNodePath + "'])", Query.JCR_SQL2);
                 QueryResult myConnectionsResult = myConnectionsQuery.execute();
 
                 NodeIterator myConnectionsIterator = myConnectionsResult.getNodes();
                 while (myConnectionsIterator.hasNext()) {
                     JCRNodeWrapper myConnectionNode = (JCRNodeWrapper) myConnectionsIterator.nextNode();
                     JCRNodeWrapper connectedToNode = (JCRNodeWrapper) myConnectionNode.getProperty("j:connectedTo")
                             .getNode();
                     userPaths.add(connectedToNode.getPath());
                 }
                 return true;
             }
         });
 
         return userPaths;
     }
 
     public SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> usersPaths,
                                                    long limit, long offset,
                                                    String targetTreeRootPath) throws RepositoryException {
         return getActivities(jcrSessionWrapper, usersPaths, limit, offset, targetTreeRootPath, null);
     }
 
     public SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> usersPaths,
                                                    long limit, long offset, String targetTreeRootPath,
                                                    List<String> activityTypes) throws RepositoryException {
         SortedSet<JCRNodeWrapper> activitiesSet = new TreeSet<JCRNodeWrapper>(ACTIVITIES_COMPARATOR);
         StringBuilder statementBuilder = new StringBuilder().append("select * from [").append(
                 JNT_BASE_SOCIAL_ACTIVITY).append("] as uA where ");
         boolean addAnd = false;
         if (usersPaths != null && !usersPaths.isEmpty()) {
             int size = usersPaths.size();
             if (size > 1) {
                 statementBuilder.append("(");
             }
             Iterator<String> iterator = usersPaths.iterator();
             while (iterator.hasNext()) {
                 statementBuilder.append("isdescendantnode(uA,['").append(iterator.next()).append("'])");
                 if (iterator.hasNext()) {
                     statementBuilder.append(" or ");
                 }
             }
             if (size > 1) {
                 statementBuilder.append(")");
             }
             addAnd = true;
         }
         if (targetTreeRootPath != null) {
             if (addAnd) {
                 statementBuilder.append(" and ");
             }
             statementBuilder.append("(uA.['j:targetNode'] like '").append(targetTreeRootPath)
                     .append("' or uA.['j:targetNode'] like '").append(targetTreeRootPath).append("/%')");
             addAnd = true;
         }
         if (activityTypes != null && !activityTypes.isEmpty()) {
             if (addAnd) {
                 statementBuilder.append(" and ");
             }
             int size = activityTypes.size();
             if (size > 1) {
                 statementBuilder.append("(");
             }
             Iterator<String> iterator = activityTypes.iterator();
             while (iterator.hasNext()) {
                 statementBuilder.append("uA.['j:activityType'] = '").append(iterator.next()).append("'");
                 if (iterator.hasNext()) {
                     statementBuilder.append(" or ");
                 }
             }
             if (size > 1) {
                 statementBuilder.append(")");
             }
         }
         statementBuilder.append(" order by [jcr:created] desc");
         String statement = statementBuilder.toString();
         QueryManager queryManager = jcrSessionWrapper.getWorkspace().getQueryManager();
         Query activitiesQuery = queryManager.createQuery(statement, Query.JCR_SQL2);
         activitiesQuery.setLimit(limit);
         activitiesQuery.setOffset(offset);
         QueryResult activitiesResult = activitiesQuery.execute();
 
         NodeIterator activitiesIterator = activitiesResult.getNodes();
         while (activitiesIterator.hasNext()) {
             JCRNodeWrapper activitiesNode = (JCRNodeWrapper) activitiesIterator.nextNode();
             activitiesSet.add(activitiesNode);
         }
 
         return activitiesSet;
     }
 
     public void removeSocialConnection(final String fromUuid, final String toUuid, final String connectionType)
             throws RepositoryException {
 
         execute(new JCRCallback<Boolean>() {
             public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
 
                 QueryManager queryManager = session.getWorkspace().getQueryManager();
 
                 // first we look for the first connection.
                 StringBuilder q = new StringBuilder(64);
                 q.append("select * from [" + JNT_SOCIAL_CONNECTION + "] where [j:connectedFrom]='").append(fromUuid)
                         .append("' and [j:connectedTo]='").append(toUuid).append("'");
                 if (StringUtils.isNotEmpty(connectionType)) {
                     q.append(" and [j:type]='").append(connectionType).append("'");
                 }
                 Query connectionQuery = queryManager.createQuery(q.toString(), Query.JCR_SQL2);
                 QueryResult connectionResult = connectionQuery.execute();
                 NodeIterator connectionIterator = connectionResult.getNodes();
                 while (connectionIterator.hasNext()) {
                     Node connectionNode = connectionIterator.nextNode();
                     session.checkout(connectionNode.getParent());
                     connectionNode.remove();
                 }
 
                 // now let's remove the reverse connection.
                 q.delete(0, q.length());
                 q.append("select * from [" + JNT_SOCIAL_CONNECTION + "] where [j:connectedFrom]='").append(toUuid)
                         .append("' and [j:connectedTo]='").append(fromUuid).append("'");
                 if (StringUtils.isNotEmpty(connectionType)) {
                     q.append(" and [j:type]='").append(connectionType).append("'");
                 }
                 Query reverseConnectionQuery = queryManager.createQuery(q.toString(), Query.JCR_SQL2);
                 QueryResult reverseConnectionResult = reverseConnectionQuery.execute();
                 NodeIterator reverseConnectionIterator = reverseConnectionResult.getNodes();
                 while (reverseConnectionIterator.hasNext()) {
                     Node connectionNode = reverseConnectionIterator.nextNode();
                     session.checkout(connectionNode.getParent());
                     connectionNode.remove();
                 }
 
                 session.save();
                 return true;
             }
         });
     }
 
     /**
      * Creates the social connection between two users.
      *
      * @param fromUserKey    the source user key
      * @param toUserKey      the target user key
      * @param connectionType the connection type
      * @throws RepositoryException in case of an error
      */
     public void createSocialConnection(String fromUserKey, String toUserKey, final String connectionType)
             throws RepositoryException {
 
         JCRUser from = getJCRUserFromUserKey(fromUserKey);
         if (from == null) {
             throw new IllegalArgumentException("Cannot find user with key " + fromUserKey);
         }
 
         JCRUser to = getJCRUserFromUserKey(toUserKey);
         if (to == null) {
             throw new IllegalArgumentException("Cannot find user with key " + toUserKey);
         }
 
         final String leftUserIdentifier = from.getIdentifier();
         final String rightUserIdentifier = to.getIdentifier();
 
         execute(new JCRCallback<Boolean>() {
             public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
 
                 JCRNodeWrapper leftUser = session.getNodeByUUID(leftUserIdentifier);
                 JCRNodeWrapper rightUser = session.getNodeByUUID(rightUserIdentifier);
                 // now let's connect this user's node to the target node.
 
                 JCRNodeWrapper leftConnectionsNode = null;
                 try {
                     leftConnectionsNode = leftUser.getNode("connections");
                     session.checkout(leftConnectionsNode);
                 } catch (PathNotFoundException pnfe) {
                     session.checkout(leftUser);
                     leftConnectionsNode = leftUser.addNode("connections", "jnt:contentList");
                     leftConnectionsNode.addMixin(JMIX_AUTOPUBLISH);
                 }
                 JCRNodeWrapper leftUserConnection = leftConnectionsNode.addNode(leftUser.getName() + "-" + rightUser.getName(), SocialService.JNT_SOCIAL_CONNECTION);
                 leftUserConnection.setProperty("j:connectedFrom", leftUser);
                 leftUserConnection.setProperty("j:connectedTo", rightUser);
                 if (connectionType != null) {
                     leftUserConnection.setProperty("j:type", connectionType);
                 }
 
                 // now let's do the connection in the other direction.
                 JCRNodeWrapper rightConnectionsNode = null;
                 try {
                     rightConnectionsNode = rightUser.getNode("connections");
                     session.checkout(rightConnectionsNode);
                 } catch (PathNotFoundException pnfe) {
                     session.checkout(rightUser);
                     rightConnectionsNode = rightUser.addNode("connections", "jnt:contentList");
                     rightConnectionsNode.addMixin(JMIX_AUTOPUBLISH);
                 }
                 JCRNodeWrapper rightUserConnection = rightConnectionsNode.addNode(rightUser.getName() + "-" + leftUser.getName(), SocialService.JNT_SOCIAL_CONNECTION);
                 rightUserConnection.setProperty("j:connectedFrom", rightUser);
                 rightUserConnection.setProperty("j:connectedTo", leftUser);
                 if (connectionType != null) {
                     rightUserConnection.setProperty("j:type", connectionType);
                 }
 
                 session.save();
                 return true;
             }
         });
     }
 
     /**
      * Starts the workflow process for accepting the social connection between two users.
      *
      * @param fromUserKey    the source user key
      * @param toUserKey      the target user key
      * @param connectionType the connection type
      * @throws RepositoryException in case of an error
      */
     public void requestSocialConnection(String fromUserKey, String toUserKey, String connectionType)
             throws RepositoryException {
 
         final JCRUser from = getJCRUserFromUserKey(fromUserKey);
         if (from == null) {
             throw new IllegalArgumentException("Cannot find user with key " + fromUserKey);
         }
 
         final Map<String, Object> args = new HashMap<String, Object>();
         args.put("fromUser", from.getUsername());
         args.put("from", fromUserKey);
         args.put("to", toUserKey);
         args.put("connectionType", connectionType);
 
         List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
         values.add(new WorkflowVariable(from.getUsername(), 1));
         args.put("jcr:title", values);
 
         JCRTemplate.getInstance().doExecuteWithSystemSession(from.getUsername(), null, Locale.ENGLISH,
                 new JCRCallback<Boolean>() {
                     public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                         workflowService.startProcess(Arrays.asList(from.getNode(session).getIdentifier()), session,
                                 "user-connection", "jBPM", args, null);
                         return true;
                     }
                 });
     }
 
     public Map<String, ActivityRecorder> getActivityRecorderMap() {
         return activityRecorderMap;
     }
 
     private JCRUser getJCRUserFromUserKey(String userKey) {
         JCRUser jcrUser = null;
 
         JahiaUser jahiaUser = userManagerService.lookupUserByKey(userKey);
         if (jahiaUser == null) {
             logger.error("Couldn't lookup user with userKey [" + userKey + "]");
             return null;
         }
 
         if (jahiaUser instanceof JCRUser) {
             jcrUser = (JCRUser) jahiaUser;
         } else {
             jcrUser = jcrUserManager.lookupExternalUser(jahiaUser);
         }
 
         return jcrUser;
     }
 
     /**
      * @param autoSplitSettings the autoSplitSettings to set
      */
     public void setAutoSplitSettings(String autoSplitSettings) {
         this.autoSplitSettings = autoSplitSettings;
     }
 
     /**
      * @param userManagerService the userManagerService to set
      */
     public void setUserManagerService(JahiaUserManagerService userManagerService) {
         this.userManagerService = userManagerService;
     }
 
     /**
      * @param jcrUserManager the jcrUserManager to set
      */
     public void setJcrUserManager(JCRUserManagerProvider jcrUserManager) {
         this.jcrUserManager = jcrUserManager;
     }
 
     /**
      * @param groupManagerService the groupManagerService to set
      */
     public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
         this.groupManagerService = groupManagerService;
     }
 
     /**
      * @param workflowService the workflowService to set
      */
     public void setWorkflowService(WorkflowService workflowService) {
         this.workflowService = workflowService;
     }
 
     /**
      * @param jcrContentUtils the jcrContentUtils to set
      */
     public void setJCRContentUtils(JCRContentUtils jcrContentUtils) {
         this.jcrContentUtils = jcrContentUtils;
         if (jcrContentUtils.getNameGenerationHelper() != null &&
                 jcrContentUtils.getNameGenerationHelper() instanceof DefaultNameGenerationHelperImpl) {
             ((DefaultNameGenerationHelperImpl) jcrContentUtils.getNameGenerationHelper()).getRandomizedNames().add(JNT_BASE_SOCIAL_ACTIVITY);
         }
     }
 
     /**
      * Apply this BeanPostProcessor to the given new bean instance <i>after</i> any bean
      * initialization callbacks (like InitializingBean's <code>afterPropertiesSet</code>
      * or a custom init-method). The bean will already be populated with property values.
      * The returned bean instance may be a wrapper around the original.
      * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
      * instance and the objects created by the FactoryBean (as of Spring 2.0). The
      * post-processor can decide whether to apply to either the FactoryBean or created
      * objects or both through corresponding <code>bean instanceof FactoryBean</code> checks.
      * <p>This callback will also be invoked after a short-circuiting triggered by a
      * {@link org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
      * in contrast to all other BeanPostProcessor callbacks.
      *
      * @param bean     the new bean instance
      * @param beanName the name of the bean
      * @return the bean instance to use, either the original or a wrapped one; if
      *         <code>null</code>, no subsequent BeanPostProcessors will be invoked
      * @throws org.springframework.beans.BeansException
      *          in case of errors
      * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
      * @see org.springframework.beans.factory.FactoryBean
      */
     public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
         if (bean instanceof ActivityRecorder) {
             ActivityRecorder activityRecorder = (ActivityRecorder) bean;
             for (String activityType : activityRecorder.getActivityTypes().keySet()) {
                 activityRecorderMap.put(activityType, activityRecorder);
             }
         }
         return bean;
     }
 
     /**
      * Apply this BeanPostProcessor to the given new bean instance <i>before</i> any bean
      * initialization callbacks (like InitializingBean's <code>afterPropertiesSet</code>
      * or a custom init-method). The bean will already be populated with property values.
      * The returned bean instance may be a wrapper around the original.
      *
      * @param bean     the new bean instance
      * @param beanName the name of the bean
      * @return the bean instance to use, either the original or a wrapped one; if
      *         <code>null</code>, no subsequent BeanPostProcessors will be invoked
      * @throws org.springframework.beans.BeansException
      *          in case of errors
      * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
      */
     public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
         return bean;
     }
 
     private boolean execute(JCRCallback<Boolean> jcrCallback) throws RepositoryException {
         return JCRTemplate.getInstance().doExecuteWithSystemSession(jcrCallback);
     }
 }
