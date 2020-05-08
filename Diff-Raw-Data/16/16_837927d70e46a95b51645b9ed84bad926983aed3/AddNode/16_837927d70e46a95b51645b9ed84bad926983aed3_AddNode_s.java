 package net.cyklotron.cms.modules.actions.structure;
 
 import java.util.Date;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FileResourceImpl;
 import net.cyklotron.cms.structure.NavigationNodeAlreadyExistException;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleResource;
 import net.cyklotron.cms.style.StyleResourceImpl;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  * Create new navigation node in document tree.
  *
  * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: AddNode.java,v 1.11 2008-03-15 13:28:12 pablo Exp $
  */
 public class AddNode
     extends BaseAddEditNodeAction
 {
     public AddNode(Logger logger, StructureService structureService, CmsDataFactory cmsDataFactory,
         StyleService styleService)
     {
         super(logger, structureService, cmsDataFactory, styleService);
         
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         // basic setup
         
         Subject subject = coralSession.getUserSubject();
 
         // parameters check
         if(!checkParameters(parameters, mvcContext, templatingContext))
         {
             return;
         }
 
         // parameters setup
 		boolean calendarTree = parameters.getBoolean("calendar_tree", false);
         String name = parameters.get("name","");
         String title = parameters.get("title","");
         String description = parameters.get("description","");
 		
         // action setup
         long styleId = parameters.getLong("style_id", -1);
         StyleResource style = null;
         if (styleId != -1)
         {
             try
             {
                 style = StyleResourceImpl.getStyleResource(coralSession, styleId);
             }
             catch(EntityDoesNotExistException e)
             {
                 templatingContext.put("result","exception");
                 logger.error("ARL exception: ", e);
                 templatingContext.put("trace", new StackTrace(e));
                 return;
             }
         }
 		NavigationNodeResource node = null;
         NavigationNodeResource parent = getNode(context);
 		try
 		{
 			if(calendarTree && parameters.get("validity_start").length() > 0)
 			{
 				parent = structureService.getParent(coralSession, parent, 
 				    new Date(parameters.getLong("validity_start")),
 				    StructureService.DAILY_CALENDAR_TREE_STRUCTURE,subject);
 			}
 
 	        // get greatest sequence number
     	    int sequence = 0;
         	Resource[] children = coralSession.getStore().getResource(parent);
         	for(int i=0; i<children.length; i++)
 	        {
     	        Resource child = children[i];
         	    if(child instanceof NavigationNodeResource)
             	{
                 	int childSeq = ((NavigationNodeResource)child).getSequence(0);
                 	sequence = sequence<childSeq ? childSeq : sequence;
             	}
         	}
 
         	// action execution
 	        node = structureService.addDocumentNode(coralSession, name, title, parent, subject);
             node.setDescription(description);
             node.setSequence(sequence);
             setValidity(parameters, node);
             node.setStyle(style);
             int priority = structureService.getDefaultPriority();
             priority = structureService.getAllowedPriority(coralSession, node, subject, priority);
             node.setPriority(priority);
 			if(parameters.get("thumbnail_id").length() > 0)
 			{
                 long thumbnailId = parameters.getLong("thumbnail_id", -1);
 				FileResource thumbnail = FileResourceImpl.getFileResource(coralSession, thumbnailId);
 				node.setThumbnail(thumbnail);				
 			}
             structureService.updateNode(coralSession, node, name, true, subject);
             if(structureService.isWorkflowEnabled())
             {
                Permission modify = coralSession.getSecurity().getUniquePermission("cms.structure.modify");
                if(!subject.hasPermission(node,modify))
                 {
                     structureService.enterState(coralSession, node, "taken", subject);
                 }
                 else
                 {
                     structureService.enterState(coralSession, node, "new", subject);
                 }
             }
         }
         catch(NavigationNodeAlreadyExistException e)
         {
             route(mvcContext, templatingContext, getViewName(), "navi_name_repeated");
             return;
         }
         catch(Exception e)
         {
             templatingContext.put("result","exception");
             logger.error("StructureException: ",e);
             templatingContext.put("trace", new StackTrace(e));
             return;
         }
 
         // make the newly created node a current node
         parameters.set("node_id", node.getIdString());
         // remove cms data to setu new node
         cmsDataFactory.removeCmsData(context);
         
         templatingContext.put("result","added_successeditfully");
     }
 
     protected String getViewName()
     {
         return "structure.AddNode";
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         return getCmsData(context).getNode().canAddChild(coralSession, coralSession.getUserSubject());
     }
 }
