 package net.cyklotron.cms.structure.internal;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jcontainer.dna.Configuration;
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.schema.CircularDependencyException;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.parameters.DefaultParameters;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.utils.StringUtils;
 
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.documents.DocumentAliasResource;
 import net.cyklotron.cms.documents.DocumentAliasResourceImpl;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.security.CmsSecurityException;
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.structure.NavigationNodeAlreadyExistException;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureException;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.workflow.StateResource;
 import net.cyklotron.cms.workflow.TransitionResource;
 import net.cyklotron.cms.workflow.WorkflowException;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * Implementation of Structure Service
  *
  * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
  * @author <a href="mailto:publo@ngo.pl">Pawel Potempski</a>
  * @version $Id: StructureServiceImpl.java,v 1.14 2008-03-15 13:27:17 pablo Exp $
  */
 public class StructureServiceImpl
     implements StructureService
 {
     /** logging facility */
     private Logger log;
 
     /** cms security service */
     private SecurityService cmsSecurityService;
 
     /** workflow service */
     private WorkflowService workflowService;
     
     /** invalid node error screen */
     private String invalidNodeErrorScreen;
 
     /** workflow switch */
     private boolean enableWorkflow;
     
     /** show unclassified nodes */
     private boolean showUnclassifiedNodes;
     
     /** default priority */
     private int defaultPriority;
     
     /** minimum allowed priority for minor editors */
     private int minorEditorPriorityMin;
     
     /** maximum allowed priority for minor editors */
     private int minorEditorPriorityMax;
     
     /** one of category required to classify document */
     private CategoryResource positiveCategory;
     
     /** one of category required to classify document */
     private CategoryResource negativeCategory;
     
     private HashSet<String> showUnclassifiedNodesInSites;
     
     /** The relation for tracking existing aliases */
     private static final String DOCUMENT_ALIAS_RELATION = "structure.DocumentAliases";
     
     /** Home page preferences key for id of the node where propose document is deployed in the site */
     private static final String PROPOSE_DOCUMENT_NODE_KEY = "stucture.proposeDocumentNode";
      
     /**
      * Initializes the service.
      */
     public StructureServiceImpl(Configuration config, Logger logger, 
         WorkflowService workflowService, SecurityService cmsSecurityService,
         CoralSessionFactory sessionFactory)
     {
         this.log = logger;
         this.workflowService = workflowService;
         this.cmsSecurityService = cmsSecurityService;
         this.showUnclassifiedNodesInSites = new HashSet<String>();
         invalidNodeErrorScreen = config.getChild("invalidNodeErrorScreen").getValue("InvalidNodeError");
         enableWorkflow = config.getChild("enableWorkflow").getValueAsBoolean(false);
         defaultPriority = config.getChild("defaultPriority").getValueAsInteger(MIN_PRIORITY);
         minorEditorPriorityMin = config.getChild("minorEditorPriorities").getChild("min")
             .getValueAsInteger(MIN_PRIORITY);
         minorEditorPriorityMax = config.getChild("minorEditorPriorities").getChild("max")
             .getValueAsInteger(MAX_PRIORITY);
         showUnclassifiedNodes = config.getChild("showUnclassifiedNodes").getValueAsBoolean(false);
         String sitesList = config.getChild("showUnclassifiedNodesInSites").getValue("");
         if(showUnclassifiedNodes)
         {
             String positiveCategoryPath = config.getChild("positiveCategory").getValue("");
             String negativeCategoryPath = config.getChild("negativeCategory").getValue("");
             if(positiveCategoryPath.equals("") || negativeCategoryPath.equals(""))
             {
                 throw new ComponentInitializationError("unable to find " +
                         "positive or negative category path in component configuration");
             }
             CoralSession coralSession = sessionFactory.getRootSession();
             try
             {
                 Resource[] resources = coralSession.getStore().getResourceByPath(positiveCategoryPath);
                 if(resources.length == 0)
                 {
                     throw new ComponentInitializationError("unable to find " +
                     "positive category resource by path: "+positiveCategoryPath);
                 }
                 if(resources.length > 1)
                 {
                     throw new ComponentInitializationError("ambigous resource path: "+positiveCategoryPath);
                 }
                 positiveCategory = (CategoryResource)resources[0];
                 resources = coralSession.getStore().getResourceByPath(negativeCategoryPath);
                 if(resources.length == 0)
                 {
                     throw new ComponentInitializationError("unable to find " +
                     "negative category resource by path: "+negativeCategoryPath);
                 }
                 if(resources.length > 1)
                 {
                     throw new ComponentInitializationError("ambigous resource path: "+negativeCategoryPath);
                 }
                 negativeCategory = (CategoryResource)resources[0];
                 if(sitesList.length() > 0)
                 {
                     String[] names = sitesList.split(",");
                     for(String name: names)
                     {
                         showUnclassifiedNodesInSites.add(name.trim());
                     }
                 }
             }
             finally
             {
                 coralSession.close();
             }
         }
     }
 
     /**
      * Returns the root navigation node of a site.
      *
      * @param site the site.
      * @return the root navigation node.
      */
     public NavigationNodeResource getRootNode(CoralSession coralSession, SiteResource site)
         throws StructureException
     {
         Resource res[] = coralSession.getStore().getResource(site, "structure");
         if(res.length == 0)
         {
             throw new StructureException("structure data not found for site "+
                                          site.getName());
         }
 
         if(res.length > 1)
         {
             throw new StructureException("multiple structure roots for site "+
                                          site.getName());
         }
 
         Resource structureNodes[] = coralSession.getStore().getResource(res[0]);
         if(structureNodes.length == 0)
         {
             throw new StructureException("site's root not found for site "+
                                          site.getName());
         }
 
         if(structureNodes.length > 1)
         {
             throw new StructureException("multiple root nodes for site "+
                                          site.getName());
         }
 
         return (NavigationNodeResource)structureNodes[0];
     }
 
     /**
      * Adds new document node to the structure.
      * @param name the name of the node.
      * @param subject the creator.
      * @param parent the parent node.
      *
      * @return created resource.
      * @throws InvalidResourceNameException 
      */
     public DocumentNodeResource addDocumentNode(CoralSession coralSession, String name, String title, Resource parent,
                                                 Subject subject)
     throws StructureException, InvalidResourceNameException
     {
         Resource[] resources = coralSession.getStore().getResource(parent, name);
         if(resources.length > 0)
         {
             throw new NavigationNodeAlreadyExistException(
                 "the node '"+name+"' already exists under node path="+parent.getPath());
         }
         Parameters preferences = new DefaultParameters();
         SiteResource site = ((NavigationNodeResource)parent).getSite();
 
         DocumentNodeResource node = null;
         try
         {
             node = DocumentNodeResourceImpl.createDocumentNodeResource(
                 coralSession, name, parent, title, site, preferences);
         }
         catch(ValueRequiredException e)
         {
             throw new StructureException("Required attribute value was not set.",e);
         }
         return node;
     }
     
     /**
      * Adds a new document alias to the structure.
      * 
      * @param originalDocument the original document.
      * @param name name of the created alias.
      * @param parent the parent of the newly created alias.
      * @param subject alias creator. 
      * 
      * @throws StructureException when there is a problem with workflow transitions for the newly created node.
      * @throws InvalidResourceNameException when name parameter contains not allowed characters or is not unique among sibling nodes. 
      * @throws ValueRequiredException if some of the required parameters are passed with null values.
      */
     public DocumentAliasResource addDocumentAlias(CoralSession coralSession,
         DocumentNodeResource originalDocument, String name, String title,
         NavigationNodeResource parent, Subject subject)
         throws StructureException, ValueRequiredException, InvalidResourceNameException
     {
         Parameters preferences = new DefaultParameters();
         DocumentAliasResource node = DocumentAliasResourceImpl.createDocumentAliasResource(
             coralSession, name, parent, originalDocument, preferences, parent.getSite(), title);
 
         int priority = getDefaultPriority();
         priority = getAllowedPriority(coralSession, node, subject, priority);
         node.setPriority(priority);
         int sequence = 0;
         Resource[] children = coralSession.getStore().getResource(parent);
         for(int i = 0; i < children.length; i++)
         {
             Resource child = children[i];
             if(child instanceof NavigationNodeResource)
             {
                 int childSeq = ((NavigationNodeResource)child).getSequence(0);
                 sequence = sequence < childSeq ? childSeq : sequence;
             }
         }
         node.setSequence(sequence);
         if(originalDocument.isThumbnailDefined())
         {
             node.setThumbnail(originalDocument.getThumbnail());
         }
         updateNode(coralSession, node, name, true, subject);
         if(isWorkflowEnabled())
         {
             Permission modify = coralSession.getSecurity().getUniquePermission(
                 "cms.structure.modify");
            Permission modifyGroup = coralSession.getSecurity().getUniquePermission(
                "cms.structure.modify_group");
            if(subject.hasPermission(node, modify) || subject.hasPermission(node, modifyGroup))
             {
                 enterState(coralSession, node, "taken", subject);
             }
             else
             {
                 enterState(coralSession, node, "new", subject);
             }
         }
 
         // existing alias tracking
         try
         {
             Relation documentAliases = coralSession.getRelationManager().getRelation(DOCUMENT_ALIAS_RELATION);
             RelationModification mod = new RelationModification();
             mod.add(originalDocument, node);
             coralSession.getRelationManager().updateRelation(documentAliases, mod);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new StructureException("can't access " + DOCUMENT_ALIAS_RELATION, e);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new StructureException("can't access " + DOCUMENT_ALIAS_RELATION, e);
         }
         
         return node;
     }
 
     /**
      * Delete the navigation node resource
      *
      * @param node the navigation node.
      * @param subject the subject who performs the action.
      */
     public void deleteNode(CoralSession coralSession, NavigationNodeResource node, Subject subject)
         throws StructureException
     {
         // WARN: We may not remove site's root node.
         if(getRootNode(coralSession, node.getSite()) == node)
         {
             throw new StructureException("Cannot remove root node");
         }
 
         // existing alias tracking
         try
         {
             Relation documentAliases = coralSession.getRelationManager().getRelation(
                 DOCUMENT_ALIAS_RELATION);
             if(node instanceof DocumentAliasResource)
             {
                 RelationModification mod = new RelationModification();
                 mod.remove(((DocumentAliasResource)node).getOriginalDocument(), node);
                 coralSession.getRelationManager().updateRelation(documentAliases, mod);
             }
             else 
             {
                 Resource[] existingAliases = documentAliases.get(node);
                 if(existingAliases.length > 0)
                 {
                     throw new StructureException("can't delete node, " + existingAliases.length
                         + " aliases exist");
                 }
             }
         }
         catch(EntityDoesNotExistException e)
         {
             throw new StructureException("can't access " + DOCUMENT_ALIAS_RELATION, e);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new StructureException("can't access " + DOCUMENT_ALIAS_RELATION, e);
         }
 
         try
         {
             cmsSecurityService.deleteRole(coralSession, "cms.structure.administrator", node, false);
             cmsSecurityService.deleteRole(coralSession, "cms.structure.editor", node, false);
             cmsSecurityService.deleteRole(coralSession, "cms.structure.redactor", node, false);
             cmsSecurityService.deleteRole(coralSession, "cms.structure.visitor", node, false);
             cmsSecurityService.deleteRole(coralSession, "cms.structure.local_visitor", node, false);
             cmsSecurityService.deleteRole(coralSession, "cms.structure.reporter", node, false);
             coralSession.getStore().deleteResource(node);
         }
         catch(CmsSecurityException e)
         {
             throw new StructureException("Security Exception",e);
         }
         catch(EntityInUseException e)
         {
             throw new StructureException("Node is in use or does not exist",e);
         }
     }
     
     /**
      * Returns the existing aliases referring to a specified document node.
      * 
      * @param node a document node.
      * @return the aliases referring to the specified node.
      * @throws StructureException when alias tracking Coral relation cannot be accessed.
      */
     public Set<DocumentAliasResource> getAliases(CoralSession coralSession,
         DocumentNodeResource node)
         throws StructureException
     {
         try
         {
             Relation documentAliases = coralSession.getRelationManager().getRelation(
                 DOCUMENT_ALIAS_RELATION);
             Resource[] existingAliases = documentAliases.get(node);
             Set<DocumentAliasResource> result = new HashSet<DocumentAliasResource>(existingAliases.length);
             for(Resource r : existingAliases)
             {
                 result.add((DocumentAliasResource)r);
             }
             return result;
         }
         catch(EntityDoesNotExistException e)
         {
             throw new StructureException("can't access " + DOCUMENT_ALIAS_RELATION, e);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new StructureException("can't access " + DOCUMENT_ALIAS_RELATION, e);
         }
     }
 
     /**
      * Update navigation node info.
      *
      * @param node the navigation node.
      * @param name the name of the node.
      * @param updateTimeStamp should custom_modification_time field be updated?
      * @param subject the subject who performs the action.
      * @return <code>true</code> if the update operation causes an automatic state transition.
      */
     public boolean updateNode(CoralSession coralSession, NavigationNodeResource node, String name, 
         boolean updateTimeStamp, Subject subject)
         throws StructureException, InvalidResourceNameException
     {
         boolean transition = false;
         if(!name.equals(node.getName()))
         {
             coralSession.getStore().setName(node, name);
         }
 		Permission permission = coralSession.getSecurity().getUniquePermission("cms.structure.modify");
 		if(!subject.hasPermission(node, permission) && isWorkflowEnabled())
 		{
 			if(node.getState()!=null)
 			{
 				String stateName = node.getState().getName();
 				if(stateName != null &&
 						("accepted".equals(stateName) || 
 						 "published".equals(stateName) ||
 						 "expired".equals(stateName) || 
 						 "prepared".equals(stateName)))
 				{
 					try
 					{
 						Resource state = coralSession.getStore(). 
 							getUniqueResourceByPath("/cms/workflow/automata/structure.navigation_node/states/taken");
 						node.setState((StateResource)state);
 					}
 					catch(AmbigousEntityNameException e)
 					{
 						throw new StructureException("cannot find the workflow state", e);
 					}
 					catch(EntityDoesNotExistException e)
 					{
 						throw new StructureException("cannot find the workflow state", e);
 					}
 				}
                 transition = true;
 			}
 		}
         if(updateTimeStamp)
         {
             node.setCustomModificationTime(new Date());
         }
         node.update();
         return transition;
     }
 
     /**
      * Update navigation node sequence info.
      *
      * @param node the navigation node.
      * @param sequence the sequence of the node.
      * @param subject the subject who performs the action.
      */
     public void updateNodeSequence(CoralSession coralSession, NavigationNodeResource node, int sequence,
                                    Subject subject)
         throws StructureException
     {
         int seq = node.getSequence(-sequence);
         if(sequence != seq)
         {
             node.setSequence(sequence);
             node.update();
         }
     }
 
     /**
      * Move navigation node to the new location.
      *
      * @param node the navigation node.
      * @param newParent net new parent node.
      * @param subject the subject who performs the action.
      */
     public void moveNode(CoralSession coralSession, NavigationNodeResource node,
                          NavigationNodeResource newParent, Subject subject)
         throws StructureException
     {
         try
         {
             coralSession.getStore().setParent(node, newParent);
         }
         catch(CircularDependencyException e)
         {
             throw new StructureException("Circular dependency exception",e);
         }
     }
 
     /**
      * Returns the screen to render in invalid url case.
      *
      * @return the invalid url error screen.
      */
     public String getInvalidNodeErrorScreen()
     {
         return invalidNodeErrorScreen;
     }
 
     /**
      * Checks whether workflow is enabled.
      *
      * @return <code>true</code> if workflow is enabled.
      */
     public boolean isWorkflowEnabled()
     {
         return enableWorkflow;
     }
 
     /**
      * Set workflow state.
      *
      * @param node the navigation node.
      * @param state the name of the state.
      * @param subject the subject.
      */
     public void enterState(CoralSession coralSession, NavigationNodeResource node, String state, Subject subject)
         throws StructureException
     {
         try
         {
             Resource stateResource = coralSession.getStore().getUniqueResourceByPath("/cms/workflow/automata/structure.navigation_node/states/"+state);
             node.setState((StateResource)stateResource);
             workflowService.enterState(coralSession, node,(StateResource)stateResource);
             node.update();
         }
         catch(WorkflowException e)
         {
             throw new StructureException("WorkflowException occured",e);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new StructureException("WorkflowException occured",e);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new StructureException("WorkflowException occured",e);
         }
     }
     
 
     /**
      * Fire transition.
      *
      * @param node the navigation node.
      * @param transition the name of the transition.
      * @param subject the subject.
      */
     public void fireTransition(CoralSession coralSession, NavigationNodeResource node, String transition, Subject subject)
         throws StructureException
     {
         try
         {
             TransitionResource[] transitions = workflowService.getTransitions(coralSession, node.getState());
             int i = 0;
             for(; i<transitions.length; i++)
             {
                 if(transitions[i].getName().equals(transition))
                 {
                     break;
                 }
             }
             if(i == transitions.length)
             {
                 throw new StructureException("Illegal transition name '"+transition+
                                              "' for navigation node in state '"+node.getState().getName());
                 
             }
             node.setState(transitions[i].getTo());
             workflowService.enterState(coralSession, node, transitions[i].getTo());
             node.update();
         }
         catch(WorkflowException e)
         {
             throw new StructureException("WorkflowException occured",e);
         }
     }
     
 	public NavigationNodeResource getParent(CoralSession coralSession, Resource root, 
 	    Date date, String mode, Subject subject) 
 		throws StructureException
 	{
 	    if(mode.equals(NONE_CALENDAR_TREE_STRUCTURE))
 	    {
 	        return (NavigationNodeResource)root;
 	    }
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(date);
 		String year = ""+calendar.get(Calendar.YEAR);
 		String month = ""+(calendar.get(Calendar.MONTH)+1);
 		String day = ""+(calendar.get(Calendar.DAY_OF_MONTH));
 		day = StringUtils.fillString(day,2,'0');
 		month = StringUtils.fillString(month,2,'0');
 		Resource[] resources = coralSession.getStore().getResource(root, year);
 		Resource yearResource = null;
 		try
         {
             if(resources.length > 0)
             {
                 yearResource = resources[0];
             }
             else
             {
                 yearResource = addDocumentNode(coralSession, year, year, root, subject);
             }
             if(mode.equals(YEARLY_CALENDAR_TREE_STRUCTURE))
             {
                 return (NavigationNodeResource)yearResource;
             }
             resources = coralSession.getStore().getResource(yearResource, month);
             Resource monthResource = null;
             if(resources.length > 0)
             {
                 monthResource = resources[0];
             }
             else
             {
                 monthResource = addDocumentNode(coralSession, month, month, yearResource, subject);
             }
             if(mode.equals(MONTHLY_CALENDAR_TREE_STRUCTURE))
             {
                 return (NavigationNodeResource)monthResource;
             }
             resources = coralSession.getStore().getResource(monthResource, day);
             if(resources.length > 0)
             {
                 return (NavigationNodeResource)resources[0];
             }
             else
             {
                 return addDocumentNode(coralSession, day, day, monthResource, subject);
             }
         }
         catch(InvalidResourceNameException e)
         {
             throw new RuntimeException("unexpected exception", e);
         }
 	}
     
 	public NavigationNodeResource getCalendarTreeRoot(NavigationNodeResource node, String mode)
 	{
 	    if(mode.equals(DAILY_CALENDAR_TREE_STRUCTURE))
 	    {
 	        return (NavigationNodeResource)node.getParent().getParent().getParent().getParent();
 	    }
 	    if(mode.equals(MONTHLY_CALENDAR_TREE_STRUCTURE))
         {
 	        return (NavigationNodeResource)node.getParent().getParent().getParent();
         }
 	    if(mode.equals(YEARLY_CALENDAR_TREE_STRUCTURE))
         {
 	        return (NavigationNodeResource)node.getParent().getParent();
         }
 	    return (NavigationNodeResource)node.getParent();
 	}
 
     public String getTimeStructureType(NavigationNodeResource node)
     {
         boolean numericParent1 = false;
         boolean numericParent2 = false;
         boolean numericParent3 = false;
         long parent1 = 0;
         long parent2 = 0;
         long parent3 = 0;
         try
         {
             parent1 = Long.parseLong(node.getParent().getName());
             numericParent1 = true;
         }
         catch(NumberFormatException e)
         {
             // ignore it!
         }
         try
         {
             parent2 = Long.parseLong(node.getParent().getParent().getName());
             numericParent2 = true;
         }
         catch(NumberFormatException e)
         {
             // ignore it!
         }
         try
         {
             parent3 = Long.parseLong(node.getParent().getParent().getParent().getName());
             numericParent3 = true;
         }
         catch(NumberFormatException e)
         {
             // ignore it!
         }
         
         if(numericParent1 && numericParent2 && numericParent3 && 
             parent1 >=1 && parent1 <= 31 && 
             parent2 >=1 && parent2 <= 12 &&
             parent3 >=1970 && parent3 <= 9999)
         {
             return StructureService.DAILY_CALENDAR_TREE_STRUCTURE;
         }
         if(numericParent1 && numericParent2 && 
             parent1 >=1 && parent1 <= 12 && 
             parent2 >=1970 && parent2 <= 9999)
         {
             return StructureService.MONTHLY_CALENDAR_TREE_STRUCTURE;
         }
         if(numericParent1 && 
             parent1 >=1970 && parent1 <= 9999)
         {
             return StructureService.YEARLY_CALENDAR_TREE_STRUCTURE;
         }
         return StructureService.NONE_CALENDAR_TREE_STRUCTURE;
     }
 
 	
     public int getDefaultPriority()
     {
         return defaultPriority;
     }
 
     public int getMinPriority(CoralSession coralSession, NavigationNodeResource node,
         Subject subject)
     {
         if(isMinorEditor(coralSession, node, subject))
         {
             return minorEditorPriorityMin;
         }
         return MIN_PRIORITY;
     }
     
     public int getMaxPriority(CoralSession coralSession, NavigationNodeResource node,
         Subject subject)
     {
         if(isMinorEditor(coralSession, node, subject))
         {
             return minorEditorPriorityMax;
         }
         return MAX_PRIORITY;
     }
     
     public int getAllowedPriority(CoralSession coralSession, NavigationNodeResource node,
         Subject subject, int requested)
     {
         int minPriority = getMinPriority(coralSession, node, subject);
         if(requested < minPriority)
         {
             return minPriority;
         }
         int maxPriority = getMaxPriority(coralSession, node, subject);
         if(requested > maxPriority)
         {
             return maxPriority;
         }
         return requested;
     }
     
     private boolean isMinorEditor(CoralSession coralSession, NavigationNodeResource node,
         Subject subject)
     {
         Permission permission = coralSession.getSecurity().getUniquePermission(
             "cms.structure.prioritize_any");
         if(subject.hasPermission(node, permission))
         {
             return false;
         }
         return true;
     }
     
     @SuppressWarnings("all")
     
     public void moveToArchive(CoralSession coralSession, NavigationNodeResource srcNode, 
         NavigationNodeResource dstNode)
         throws StructureException
     {
         try
         {
             coralSession.getStore().setParent(srcNode, dstNode);
         }
         catch(Exception e)
         {
             throw new StructureException("failed to move node", e);
         }
     }
 
     public CategoryResource getNegativeCategory()
     {
         return negativeCategory;
     }
 
     public CategoryResource getPositiveCategory()
     {
         return positiveCategory;
     }
 
     public boolean isShowUnclassifiedNodes()
     {
         return showUnclassifiedNodes;
     }
     
     public boolean showUnclassifiedInSite(SiteResource site)
     {
         return showUnclassifiedNodesInSites.contains(site.getName());
     }
     
     /**
      * Returns the node where ProposeDocument screen is deployed in the site.
      * 
      * @param site the site.
      * @throws StructureException
      */
     public NavigationNodeResource getProposeDocumentNode(CoralSession coralSession,
         SiteResource site)
         throws StructureException
     {
         NavigationNodeResource homePage = getRootNode(coralSession, site);
         try
         {
             long id = homePage.getPreferences().getLong(PROPOSE_DOCUMENT_NODE_KEY);
             return NavigationNodeResourceImpl.getNavigationNodeResource(coralSession, id);
         }
         catch(Exception e)
         {
             return null;
         }
     }
 
     /**
      * Sets the node where ProposeDocument screen is deployed in the site.
      * 
      * @param site the site.
      * @param node the node.
      */
     public void setProposeDocumentNode(CoralSession coralSession, SiteResource site,
         NavigationNodeResource node)
         throws StructureException
     {
         NavigationNodeResource homePage = getRootNode(coralSession, site);
         homePage.getPreferences().set(PROPOSE_DOCUMENT_NODE_KEY, node.getId());
     }
 }
 
