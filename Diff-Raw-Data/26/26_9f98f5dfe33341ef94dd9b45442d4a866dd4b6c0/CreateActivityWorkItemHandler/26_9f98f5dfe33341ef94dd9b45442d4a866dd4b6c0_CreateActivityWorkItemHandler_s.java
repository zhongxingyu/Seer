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
    private String activityType;

    private String parameter1;
    private String parameter2;
    private String parameter3;

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setParameter1(String parameter1) {
        this.parameter1 = parameter1;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    public void setParameter3(String parameter3) {
        this.parameter3 = parameter3;
    }
 
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
