 package org.jahia.modules.personalization.tracking;
 
 import org.jahia.services.content.JCRCallback;
 import org.jahia.services.content.JCRNodeWrapper;
 import org.jahia.services.content.JCRSessionWrapper;
 import org.jahia.services.content.JCRTemplate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 
 /**
  * Tracking data persistence storage service.
  */
 public class TrackingService {
 
     private static Logger logger = LoggerFactory.getLogger(TrackingService.class);
 
     public static final String ROOT_TRACKING_NODE_PATH = "tracking";
 
     public TrackingData getByClientId(final String trackingClientId) {
         try {
             execute(new JCRCallback<TrackingData>() {
                 public TrackingData doInJCR(JCRSessionWrapper session) throws RepositoryException {
                     String[] trackingIdParts = TrackingHelper.getInstance().getClientIDParts(trackingClientId);
                     JCRNodeWrapper trackingRootNode = getTrackingRootNode(session);
                    try {
                        JCRNodeWrapper trackingNode = trackingRootNode;
                        for (String trackingIdPart : trackingIdParts) {
                            trackingNode = trackingNode.getNode(trackingIdPart);
                        }
                        // @todo normally we should also now retrieve and merge user tracking data if it is associated.
                        return new TrackingData(trackingNode);
                    } catch (PathNotFoundException pnfe) {
                        return null;
                     }
                 }
             });
         } catch (RepositoryException e) {
             logger.error("Error retrieving tracking data for id " + trackingClientId, e);
         }
         return null;
     }
 
     public boolean store(final TrackingData trackingData) {
         try {
             execute(new JCRCallback<Boolean>() {
                 public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                     TrackingData existingTrackingData = getByClientId(trackingData.getClientID());
                     TrackingData newTrackingData = trackingData;
                     JCRNodeWrapper targetTrackingNode = null;
                     if (existingTrackingData != null) {
                         targetTrackingNode = session.getNodeByIdentifier(existingTrackingData.getStorageID());
                         newTrackingData = existingTrackingData.merge(trackingData);
                     } else {
                         targetTrackingNode = createTargetTrackingNode(session, newTrackingData);
                     }
                     newTrackingData.toJCRNode(targetTrackingNode);
                     session.save();
                     return true;  //To change body of implemented methods use File | Settings | File Templates.
                 }
             });
         } catch (RepositoryException e) {
             logger.error("Error retrieving storing tracking data for id " + trackingData.getClientID(), e);
         }
         return true;
     }
 
     public boolean merge(TrackingData trackingData) {
         return false;
     }
 
     // Private methods
 
     private <T> T execute(JCRCallback<T> jcrCallback) throws RepositoryException {
         return JCRTemplate.getInstance().doExecuteWithSystemSession(jcrCallback);
     }
 
     private JCRNodeWrapper getTrackingRootNode(JCRSessionWrapper session) throws RepositoryException {
         JCRNodeWrapper rootNode = session.getRootNode();
         JCRNodeWrapper trackingRootNode = null;
         try {
             trackingRootNode = session.getNode("/" + ROOT_TRACKING_NODE_PATH);
         } catch (PathNotFoundException pnfe) {
             trackingRootNode = rootNode.addNode(ROOT_TRACKING_NODE_PATH);
             session.save();
         }
         return trackingRootNode;
     }
 
     private JCRNodeWrapper createTargetTrackingNode(JCRSessionWrapper session, TrackingData newTrackingData) throws RepositoryException {
         String[] trackingIdParts = TrackingHelper.getInstance().getClientIDParts(newTrackingData.getClientID());
         JCRNodeWrapper trackingRootNode = getTrackingRootNode(session);
         JCRNodeWrapper trackingNode = trackingRootNode;
         for (String trackingIdPart : trackingIdParts) {
             try {
                 trackingNode = trackingNode.getNode(trackingIdPart);
             } catch (PathNotFoundException pnfe) {
                 trackingNode = trackingNode.addNode(trackingIdPart);
             }
         }
         return trackingNode;
     }
 
 
 }
