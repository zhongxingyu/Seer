 package org.jahia.modules.social;
 
 import org.jahia.modules.sociallib.SocialService;
 import org.jahia.services.SpringContextSingleton;
 import org.jahia.services.content.JCRCallback;
 import org.jahia.services.content.JCRNodeWrapper;
 import org.jahia.services.content.JCRSessionWrapper;
 import org.jahia.services.content.JCRTemplate;
 import org.jahia.services.workflow.jbpm.custom.AbstractWorkItemHandler;
 import org.kie.api.runtime.process.WorkItem;
 import org.kie.api.runtime.process.WorkItemHandler;
 import org.kie.api.runtime.process.WorkItemManager;
 
 import javax.jcr.RepositoryException;
 
 public class CreateActivityWorkItemHandler extends AbstractWorkItemHandler implements WorkItemHandler {
     private static final long serialVersionUID = 1L;
 
     @Override
     public void executeWorkItem(final WorkItem workItem, WorkItemManager manager) {
         final SocialService socialService = (SocialService) SpringContextSingleton.getBean("socialService");
         try {
             JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                 public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                     final String uuid = (String) workItem.getParameter("nodeId");
                     String currentUser = (String) workItem.getParameter("currentUser");
                     if (currentUser == null) {
                         currentUser = (String) workItem.getParameter("user");
                     }
                    String activityType = (String) workItem.getParameter("activityType");
                    String parameter1 = (String) workItem.getParameter("parameter1");
                    String parameter2 = (String) workItem.getParameter("parameter2");
                    String parameter3 = (String) workItem.getParameter("parameter3");
 
                     JCRNodeWrapper node = session.getNodeByIdentifier(uuid);
                     socialService.addActivity(currentUser, node, activityType, session, parameter1, parameter2, parameter3);
                     session.save();
                     return null;
                 }
             });
         } catch (RepositoryException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         manager.completeWorkItem(workItem.getId(), null);
     }
 
     @Override
     public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
         manager.abortWorkItem(workItem.getId());
     }
 }
