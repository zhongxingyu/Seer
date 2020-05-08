 package net.cyklotron.cms.modules.actions.structure.workflow;
 
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.CmsTool;
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 import net.cyklotron.cms.workflow.StatefulResource;
 import net.cyklotron.cms.workflow.TransitionResource;
 import net.cyklotron.cms.workflow.WorkflowException;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * Simple fire transition action.
  * 
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: FireTransition.java,v 1.5 2006-05-15 08:49:07 pablo Exp $
  */
 public class FireTransition
     extends BaseWorkflowAction
 {
     private final SecurityService securityService;
 
     public FireTransition(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService, WorkflowService workflowService,
         SecurityService securityService)
     {
         super(logger, structureService, cmsDataFactory, styleService, workflowService);
         this.securityService = securityService;
         
     }
     /**
      * Performs the action.
      */
     @Override
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         Subject subject = coralSession.getUserSubject();
         
         long nodeId = parameters.getLong("node_id", -1);
         if (nodeId == -1)
         {
             templatingContext.put("result","parameter_not_found");
             return;
         }
         String transitionName = parameters.get("transition","");
         try
         {
             StatefulResource resource = (StatefulResource)coralSession.getStore().getResource(nodeId);
             TransitionResource[] transitions = workflowService.getTransitions(coralSession, resource.getState());
             int i = 0;
             for(; i<transitions.length; i++)
             {
                 if(transitions[i].getName().equals(transitionName))
                 {
                     break;
                 }
             }
             if(i == transitions.length)
             {
                 templatingContext.put("result","illegal_transition_name");
                 logger.error("illegal transition name '"+transitionName+"' for state '"+resource.getState().getName()+"'");
                 return;
             }
             resource.setState(transitions[i].getTo());
             workflowService.enterState(coralSession, resource, transitions[i].getTo());
             if(!transitionName.equals("take_assigned") &&
                !transitionName.equals("take_rejected") &&
                !transitionName.equals("finish"))
             {
                 if(transitionName.equals("accept"))
                 {
                     ((NavigationNodeResource)resource).setLastAcceptor(subject);
                 }
                 else
                 {
                     ((NavigationNodeResource)resource).setLastEditor(subject);
                 }
             }
             resource.update();
         }
         catch(EntityDoesNotExistException e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             logger.error("ResourceException: ",e);
             return;
         }
         catch(WorkflowException e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             logger.error("ResourceException: ",e);
             return;
         }
         templatingContext.put("result","changed_successfully");
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         Parameters parameters = RequestParameters.getRequestParameters(context);
         try
         {
             long nodeId = parameters.getLong("node_id", -1);
             String transitionName = parameters.get("transition","");
             NavigationNodeResource node = NavigationNodeResourceImpl.
                 getNavigationNodeResource(coralSession, nodeId);
             Subject subject = coralSession.getUserSubject();
             Permission permission = null;
             if(transitionName.equals("take_assigned") ||
                transitionName.equals("take_rejected") ||
                transitionName.equals("finish"))
             {
                 permission = coralSession.getSecurity().getUniquePermission("cms.structure.modify_own");
                 return subject.hasPermission(node, permission);
             }
             if(transitionName.equals("reject_prepared") ||
                transitionName.equals("reject_accepted") ||
                transitionName.equals("reject_published") ||
                transitionName.equals("reject_expired") ||
               transitionName.equals("reject_assigned") ||
                transitionName.equals("expire_new")
                 || transitionName.equals("expire_assigned")
                 || transitionName.equals("expire_taken")
                 || transitionName.equals("expire_prepared"))
                
             {
                 permission = coralSession.getSecurity().getUniquePermission("cms.structure.modify");
                 return subject.hasPermission(node, permission);
             }
             if(transitionName.equals("accept"))
             {
                 permission = coralSession.getSecurity().getUniquePermission("cms.structure.modify");
                 if(subject.hasPermission(node, permission))
                 {
                     return true;
                 }
                 permission = coralSession.getSecurity().getUniquePermission("cms.structure.accept");
                 Set<Subject> peers = securityService.getSharingWorkgroupPeers(coralSession, CmsTool
                     .getSite(node), coralSession.getUserSubject());
                 return coralSession.getUserSubject().hasPermission(node, permission) && peers.contains(node.getOwner());
             }
             logger.error("Invalid transition name");
             return false;
         }
         catch(Exception e)
         {
             logger.error("Exception during access check",e);
             return false;
         }
     }
 }
