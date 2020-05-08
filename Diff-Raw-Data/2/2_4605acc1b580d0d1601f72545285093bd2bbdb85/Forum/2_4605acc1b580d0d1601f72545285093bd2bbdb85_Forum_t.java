 package net.cyklotron.cms.modules.views.forum;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.ResourceListTableModel;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableRow;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.forum.DiscussionResource;
 import net.cyklotron.cms.forum.DiscussionResourceImpl;
 import net.cyklotron.cms.forum.ForumException;
 import net.cyklotron.cms.forum.ForumResource;
 import net.cyklotron.cms.forum.ForumService;
 import net.cyklotron.cms.forum.MessageResource;
 import net.cyklotron.cms.forum.MessageResourceImpl;
 import net.cyklotron.cms.forum.MessageTableModel;
 import net.cyklotron.cms.modules.views.BaseSkinableScreen;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 import net.cyklotron.cms.util.CollectionFilter;
 import net.cyklotron.cms.util.ProtectedViewFilter;
 import net.cyklotron.cms.workflow.AutomatonResource;
 import net.cyklotron.cms.workflow.StateResource;
 import net.cyklotron.cms.workflow.WorkflowException;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * Stateful screen for forum application.
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawe� Potempski</a>
  * @version $Id: Forum.java,v 1.16 2008-05-29 22:55:43 rafal Exp $
  */
 public class Forum
     extends BaseSkinableScreen
 {
     /** forum serivce. */
     protected ForumService forumService;
     
     protected WorkflowService workflowService;
 
     private Set<String> allowedStates = new HashSet<String>();
     
     private final List<String> REQUIRES_AUTHENTICATED_USER = Arrays.asList("ModeratorTasks",
         "EditMessage");
 
     public Forum(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager, ForumService forumService, WorkflowService workflowService)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
         this.forumService = forumService;
         this.workflowService = workflowService;
         allowedStates.add("Discussions");
         allowedStates.add("EditMessage");
         allowedStates.add("Messages");
         allowedStates.add("Message");
         allowedStates.add("NewMessage");
         allowedStates.add("NewDiscussion");
         allowedStates.add("ModeratorTasks");
     }
     
     public String getState()
     throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
         long did = screenConfig.getLong("did", -1);
         String defaultState = (did == -1) ? "Discussions" : "Messages"; 
         
         Parameters parameters = RequestParameters.getRequestParameters(context);
         String state = parameters.get("state", defaultState);
         
         if(!allowedStates.contains(state))
         {
             return null;
         }
         else if(did != -1 && "Discussions".equals(state))
         {
             return defaultState;
         }
         return state;
     }
 
     public void prepareDiscussions(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             ForumResource forum = forumService.getForum(coralSession, getSite());
             templatingContext.put("forum",forum);
             
             Resource[] res = coralSession.getStore().getResource(forum, "discussions");
             if(res.length != 1)
             {
                 screenError(getNode(), context, "discussions node not found in "+forum.getPath());
             }
             Resource[] discussions = coralSession.getStore().getResource(res[0]);
             TableState state = tableStateManager.getState(context, "cms:screens:forum,Forum:discussions");
             if(state.isNew())
             {
             
                 state.setTreeView(false);
                 state.setPageSize(10);
                 state.setSortColumnName("creation.time");
                 state.setAscSort(false);
             }
             TableModel model = new ResourceListTableModel(discussions, i18nContext.getLocale());
             ArrayList<TableFilter> filters = new ArrayList<TableFilter>();
             filters.add(new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
             TableTool helper = new TableTool(state, filters, model);
             
             templatingContext.put("discussions_table", helper);
             templatingContext.put("forum_tool", new ForumTool(coralSession));
 
             res = coralSession.getStore().getResource(forum, "comments");
             if(res.length != 1)
             {
                 screenError(getNode(), context, "comments node not found in "+forum.getPath());
             }
             Resource[] comments = coralSession.getStore().getResource(res[0]);
             
             TableState state2 = tableStateManager.getState(context, "cms:screens:forum,Forum:comments");
             if(state2.isNew())
             {
             
                 state2.setTreeView(false);
                 state2.setPageSize(10);
                 state2.setSortColumnName("creation.time");
                 state2.setAscSort(false);
             }
             TableModel model2 = new ResourceListTableModel(comments, i18nContext.getLocale());
             ArrayList<TableFilter> filters2 = new ArrayList<TableFilter>();
             filters2.add(new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
             TableTool helper2 = new TableTool(state2, filters2, model2);
             templatingContext.put("comments_table", helper2);
         }
         catch(ForumException e)
         {
             screenError(getNode(), context, "resource not found", e);
         }
         catch(TableException e)
         {
             screenError(getNode(), context, "resource not found", e);
         }
     }
     
     public void prepareMessages(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
 
         long level_expanded = screenConfig.getLong("level_expanded", 0);
         Long mid = parameters.getLong("mid", -1);
         long did = screenConfig.getLong("did", parameters.getLong("did", -1));
         if(did == -1)
         {
             screenError(getNode(), context, "discussion id not found");
             return;
         }
         try
         {
             DiscussionResource discussion = DiscussionResourceImpl.getDiscussionResource(coralSession,did);
             templatingContext.put("discussion",discussion);
             
             String tableInstance = "cms:screen:forum:ForumMessages:"+getNode().getIdString()+":"+discussion.getIdString();
             String rootId = discussion.getIdString();
             boolean showRoot = false; 
             if(mid != -1)
             {
                 tableInstance += ":" + mid.toString(); 
                 rootId = mid.toString();
                 showRoot = true;
                 templatingContext.put("mid", mid);
             }            
 
             TableState state = tableStateManager.getState(context, tableInstance);
             if(state.isNew())
             {
                 state.setTreeView(true);
                 state.setRootId(rootId);
                 state.setCurrentPage(0);
                 state.setShowRoot(showRoot);
                 state.setExpanded(rootId);
                 state.setAllExpanded(false);
                 state.setPageSize(10);
                 state.setSortColumnName("creation.time");
                 state.setAscSort(false);
             }
             
             TableModel model = new MessageTableModel(coralSession, i18nContext.getLocale());
             ArrayList<TableFilter> filters = new ArrayList<TableFilter>();
             filters.add(new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
             
            if(mid == -1 && level_expanded > 0)
             {
                 TableFilter[] filtersArray = new TableFilter[filters.size()];
                 filters.toArray(filtersArray);
                 setLevelExpanded(model, filtersArray, state, level_expanded);
             }
             TableTool helper = new TableTool(state, filters, model);
                
             templatingContext.put("table",helper);
             templatingContext.put("forum_tool", new ForumTool(coralSession));
         }
         catch(EntityDoesNotExistException e)
         {
             screenError(getNode(), context, "resource not fount ", e);
         }
         catch(TableException e)
         {
             screenError(getNode(), context, "failed to initialize table toolkit: ", e);
         }
         catch(Exception e)
         {
             screenError(getNode(), context, "Component exception: ", e);
         }
     }
     
     public void prepareModeratorTasks(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
 
         long mid = parameters.getLong("mid", -1);
         try
         {
             ForumResource forum = forumService.getForum(coralSession, getSite());
             templatingContext.put("forum", forum);
             
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             
             Long did = screenConfig.getLong("did", parameters.getLong("did", -1));
             List messages = getModeratorTasks(coralSession, forum, did);
             if(messages.size() > 0 && mid == -1)
             {
                 templatingContext.put("mid", ((Resource)messages.get(messages.size()-1)).getId());
             }
             else
             {
                 templatingContext.put("mid", mid);
             }
             
             String tableInstance = "cms:screen:forum:ModeratorTasks:"+getSite().getIdString();
             TableState state = tableStateManager.getState(context, tableInstance);
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setPageSize(10);
                 state.setSortColumnName("creation.time");
                 state.setAscSort(false);
             }
             TableModel model = new ResourceListTableModel(messages,i18nContext.getLocale());
             TableTool helper = new TableTool(state, null, model);
             templatingContext.put("table", helper);
             templatingContext.put("forum_tool", new ForumTool(coralSession));
         }
         catch(Exception e)
         {
             screenError(getNode(), context, "Component exception: ", e);
         }
     }
 
     public void prepareMessage(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
 
         long mid = parameters.getLong("mid", -1);
         if(mid == -1)
         {
             screenError(getNode(), context, "Message id not found");
             return;
         }
         try
         {
             MessageResource message = MessageResourceImpl.getMessageResource(coralSession,mid);
             templatingContext.put("message",message);
             List<Resource> children = new ArrayList<Resource>(Arrays.asList(coralSession.getStore().getResource(
                           message)));
             CollectionFilter.apply(children, new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
             templatingContext.put("children", children);
         }
         catch(EntityDoesNotExistException e)
         {
             screenError(getNode(), context, "Resource not found", e);
         }
     }
     
     public void prepareEditMessage(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
 
         long mid = parameters.getLong("mid", -1);
         if(mid == -1)
         {
             screenError(getNode(), context, "Message id not found");
             return;
         }
         try
         {
             MessageResource message = MessageResourceImpl.getMessageResource(coralSession, mid);
             templatingContext.put("message", message);
         }
         catch(EntityDoesNotExistException e)
         {
             screenError(getNode(), context, "Resource not found", e);
         }
     }
     
     public void prepareNewMessage(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
 
         long did = parameters.getLong("did", -1);
         long mid = parameters.getLong("mid", -1);
         long resid = parameters.getLong("resid", -1);
         if(mid == -1 && did == -1 && resid == -1)
         {
             screenError(getNode(), context, "Discussion nor Message nor Resource id not found");
         }
         try
         {
             if(did != -1)
             {
                 DiscussionResource discussion = DiscussionResourceImpl.getDiscussionResource(coralSession,did);
                 templatingContext.put("discussion",discussion);
                 templatingContext.put("parent",discussion);
             }
             else if(mid != -1)
             {
                 MessageResource message = MessageResourceImpl.getMessageResource(coralSession,mid);
                 DiscussionResource discussion = message.getDiscussion();
                 templatingContext.put("parent_content",prepareContent(message.getContent()));
                 templatingContext.put("discussion",discussion);
                 templatingContext.put("parent",message);
             }
             else
             {
                 templatingContext.put("resource", coralSession.getStore().getResource(resid));
             }
             ForumResource forum = forumService.getForum(coralSession, getSite());
             templatingContext.put("add_captcha",forum.getCaptchaEnabled(false));
         }
         catch(EntityDoesNotExistException e)
         {
             screenError(getNode(), context, "Resource not found", e);
         }
         catch(ForumException e)
         {
             screenError(getNode(), context, "resource not found", e);
         }
     }
     
     public void prepareNewDiscussion(Context context)
         throws ProcessingException
     {
         try
         {
             CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
             TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
             ForumResource forum = forumService.getForum(coralSession, getSite());
             templatingContext.put("add_captcha",forum.getCaptchaEnabled(false));
         }
         catch(ForumException e)
         {
             screenError(getNode(), context, "resource not found", e);
         }    
     }
 
     private String prepareContent(String content)
     {
         StringBuilder sb = new StringBuilder("");
         StringTokenizer st = new StringTokenizer(content, "\n", false);
         while (st.hasMoreTokens()) {
             sb.append(">");
             sb.append(st.nextToken());
             sb.append("\n");
         }
         return sb.toString();
     }
     
     private List getModeratorTasks(CoralSession coralSession, ForumResource forum, Long discussionId)
         throws ProcessingException
     {
         List messages = new ArrayList();
         try
         {
             Resource[] nodes = null;
             ResourceClass messageClass = coralSession.getSchema().getResourceClass(
                 "cms.forum.message");
             AutomatonResource automaton = workflowService.getPrimaryAutomaton(coralSession,
                 getSite().getParent().getParent(), messageClass);
             StateResource state = workflowService.getState(coralSession, automaton, "new");   
             QueryResults results = coralSession.getQuery().executeQuery(
                 "FIND RESOURCE FROM cms.forum.message WHERE state = " + state.getIdString());
             nodes = results.getArray(1);
             for(int i = 0; i < nodes.length; i++)
             {
                 MessageResource message = (MessageResource)nodes[i];
                 if(discussionId == null || discussionId <= 0)
                 {
                     if(message.getDiscussion().getForum().equals(forum))
                     {
                         messages.add(message);
                     }
                 }
                 else
                 {
                     if(discussionId.equals(message.getDiscussion().getId()))
                     {
                         messages.add(message);
                     }
                 }
             }
         }
         catch(MalformedQueryException e)
         {
             throw new ProcessingException("Malformed query", e);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new ProcessingException("cms.forum.message resource does not exist", e);
         }
         catch(WorkflowException e)
         {
             throw new ProcessingException("Workflow state does not exist", e);
         }
         catch(ProcessingException e)
         {
             throw new ProcessingException("Processing Exception", e);
         }
         return messages;
     }
     
     
     void setLevelExpanded(TableModel model, TableFilter[] filtersArray, TableState state,
         Long level_expanded)
     {
         TableRow[] rows = model.getRowSet(state, filtersArray).getRows();
         for(int i = 0; i < rows.length; i++)
         {
             if(!state.isExpanded(rows[i].getId()) && rows[i].getDepth() < level_expanded)
             {
                 state.setExpanded(rows[i].getId());
                 if(rows[i].getChildCount() > 0)
                 {
                     setLevelExpanded(model, filtersArray, state, level_expanded);
                 }
             }
         }
     }
     
     
     @Override
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {        
         return REQUIRES_AUTHENTICATED_USER.contains(getState());
     }
     
     @Override
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         CmsData cmsData = cmsDataFactory.getCmsData(context);
 
         String state = getState();
         if("ModeratorTasks".equals(state))
         {
             try
             {
                 Permission moderatePermission = coralSession.getSecurity().getUniquePermission(
                     "cms.forum.moderate");
                 ForumResource forum = forumService.getForum(coralSession, getSite());
                 return getNode().canView(coralSession, cmsData, cmsData.getUserData().getSubject())
                     && coralSession.getUserSubject().hasPermission(forum, moderatePermission);
             }
             catch(Exception e)
             {
                 return false;
             }
         }
         else if("EditMessage".equals(state))
         {
             try
             {
                 Permission modifyPermission = coralSession.getSecurity().getUniquePermission(
                     "cms.forum.modify");
                 ForumResource forum = forumService.getForum(coralSession, getSite());
                 return getNode().canView(coralSession, cmsData, cmsData.getUserData().getSubject())
                     && coralSession.getUserSubject().hasPermission(forum, modifyPermission);
             }
             catch(Exception e)
             {
                 return false;
             }            
         }
         else
         {
             if(isNodeDefined())
             {
                 return getNode().canView(coralSession, cmsData, cmsData.getUserData().getSubject());
             }
             else
             {
                 return true;
             }
         }
     }
     
     public class ForumTool
     {
         final protected CoralSession coralSession;
         
         ForumTool(CoralSession coralSession)
         {
             this.coralSession = coralSession;
         }
 
         
         /*
          *  return VisibleMessages count
          */
         public int getVisibleMessages(DiscussionResource discussion)
         {
             return forumService.getVisibleMessages(coralSession, discussion, coralSession.getUserSubject());
         }
         
         /*
          *  return Visible sub messages count of message 
          */
         public int getVisibleSubMessages(MessageResource message)
         {
             return forumService.getVisibleSubMessages(coralSession, message, coralSession.getUserSubject());
         }
         
         /*
         *  return the date of last modified message or discussion child visible to a particular subject.
         */
         public Date getLastModifiedMessage(Resource message)
         {
             return forumService.getLastModifiedMessage(coralSession, message, coralSession.getUserSubject());
         }
         
         /*
          *  return ModeratorTasks  count form defined discussion
          */
         public int getModeratorTasks(Long discussionId)
         {
             try
             {
                ForumResource forum = forumService.getForum(coralSession, getSite());
                return Forum.this.getModeratorTasks(coralSession, forum, discussionId).size();
             }
             catch(ProcessingException e)
             {
                 return 0;
             }
             catch(ForumException e)
             {
                 return 0;
             }
         }
         
         /*
          *  return ModeratorTasks count for all discussions 
          */
         public int getModeratorTasks()
         {
             return getModeratorTasks(null);
         }
     }
 }
