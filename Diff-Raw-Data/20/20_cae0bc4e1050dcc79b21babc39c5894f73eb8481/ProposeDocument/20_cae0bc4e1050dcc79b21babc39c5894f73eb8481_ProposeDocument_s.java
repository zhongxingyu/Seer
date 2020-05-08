 package net.cyklotron.cms.modules.views.documents;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.AuthenticationContext;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.html.HTMLService;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.internal.ProposedDocumentData;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  * Stateful screen for propose document application.
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawe� Potempski</a>
  * @version $Id: ProposeDocument.java,v 1.12 2008-11-04 17:14:48 rafal Exp $
  */
 public class ProposeDocument
     extends BaseSkinableDocumentScreen
 {
     private final CategoryService categoryService;
     private final RelatedService relatedService;
     private final HTMLService htmlService;
 
     public ProposeDocument(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager, CategoryService categoryService,
         RelatedService relatedService, HTMLService htmlService)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
         this.categoryService = categoryService;
         this.relatedService = relatedService;
         this.htmlService = htmlService;
     }
 
     public String getState() 
         throws ProcessingException
     {
         // this method is called multiple times during rendering, so it makes sense to cache the evaluated state
         String state = (String) context.getAttribute(getClass().getName()+".state");
         if(state == null)
         {
             Parameters parameters = RequestParameters.getRequestParameters(context);
             AuthenticationContext authContext = context.getAttribute(AuthenticationContext.class);
             state = parameters.get("state",null);
             if(state == null)
             {
                 CmsData cmsData = cmsDataFactory.getCmsData(context);
                 Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
                 boolean editingEnabled = screenConfig.getBoolean("editing_enabled", false);
                 if(editingEnabled)
                 {
                     if(authContext.isUserAuthenticated())
                     {
                         state = "MyDocuments";
                     }
                     else
                     {
                         state = "Anonymous";
                     }                   
                 }
                 else
                 {
                     state = "AddDocument";
                 }
             }
             context.setAttribute(getClass().getName()+".state", state);
         }
         return state;
     }
     
     @Override
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         String state = getState();
         if("EditDocument".equals(state))
         {
             return true;
         }
         if("MyDocuments".equals(state))
         {
             return true;
         }
         return false;
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         TemplatingContext templatingContext = context.getAttribute(TemplatingContext.class);
         String result = (String)templatingContext.get("result");
         if("exception".equals(result))
         {
             return true;
         }
         String state = getState();
         if("Anonymous".equals(state))
         {
             return true;
         }
         if("MyDocuments".equals(state))
         {
             // no particular permission needed to see the stuff you sumbitted, provided you are
             // authenticated
             return true;
         }
         try
         {
             Parameters requestParameters = context.getAttribute(RequestParameters.class);
             CoralSession coralSession = context.getAttribute(CoralSession.class);
             Subject userSubject = coralSession.getUserSubject();
             if("AddDocument".equals(state))
             {
                long id = requestParameters.getLong("parent_node_id", -1);
                Resource node;
                if(id != -1)
                {
                    node = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession, id);
                }
                else
                {
                    node = cmsDataFactory.getCmsData(context).getHomePage();
                }
                 Permission submitPermission = coralSession.getSecurity().getUniquePermission(
                    "cms.structure.submit");
                return userSubject.hasPermission(node, submitPermission);
             }
             if("EditDocument".equals(state))
             {
                 long id = requestParameters.getLong("node_id", -1);
                 Resource node = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession,
                     id);
                 Permission modifyPermission = coralSession.getSecurity().getUniquePermission(
                     "cms.structure.modify");
                 Permission modifyOwnPermission = coralSession.getSecurity().getUniquePermission(
                     "cms.structure.modify_own");
                 if(userSubject.hasPermission(node, modifyPermission))
                 {
                     return true;
                 }
                 if(node.getOwner().equals(userSubject)
                     && userSubject.hasPermission(node, modifyOwnPermission))
                 {
                     return true;
                 }
                 return false;
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException(e);
         }
         throw new ProcessingException("invalid state " + state);
     }
 
     /**
      * Welcome view for the anonymous user.
      * <p>
      * Allows the user to log in, or proceed to submit a document anonymously.
      * </p>
      */
     public void prepareAnonymous(Context context)
     {
 
     }
     
     /**
      * Submitted documents list for an authenticated user.
      */
     public void prepareMyDocuments(Context context) throws ProcessingException
     {
         try
         {
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             CoralSession coralSession = context.getAttribute(CoralSession.class);
             String query = "FIND RESOURCE FROM documents.document_node WHERE created_by = "
                 + coralSession.getUserSubject().getIdString() + " AND site = "
                 + cmsData.getSite().getIdString();
             List<Resource> myDocuments = coralSession.getQuery().executeQuery(query).getList(1);
             TemplatingContext templatingContext = context.getAttribute(TemplatingContext.class);
             templatingContext.put("myDocuments", myDocuments);
         }
         catch(Exception e)
         {
             throw new ProcessingException("internal errror", e);
         }
     }
 
     /**
      * Propse a new document, either anonymously or as an authenitcated user.
      * 
      * @param context
      * @throws ProcessingException
      */
     public void prepareAddDocument(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         SiteResource site = getSite();
         try
         {
             // refill parameters in case we are coming back failed validation            
             Parameters screenConfig = getScreenConfig();
             ProposedDocumentData data = new ProposedDocumentData(screenConfig);
             data.fromParameters(parameters, coralSession);
             data.toTemplatingContext(templatingContext);            
             prepareCategories(context, true);
             // resolve parent node in case template needs it for security check
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             long parentId = screenConfig.getLong("parent_id", -1L);
             Resource parentNode = parentId != -1L ? NavigationNodeResourceImpl
                 .getNavigationNodeResource(coralSession, parentId) : cmsData.getNode();
             templatingContext.put("parent_node", parentNode);    
         }
         catch(Exception e)
         {
             screenError(getNode(), context, "Screen Error ", e);
         }
     }
     
     /**
      * Edit a previously submitted document.
      * 
      * @param context
      * @throws ProcessingException 
      */
     public void prepareEditDocument(Context context) throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             long docId = parameters.getLong("doc_id");
             DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(coralSession, docId);
             templatingContext.put("doc", node);
             ProposedDocumentData data = new ProposedDocumentData(getScreenConfig());
             data.fromNode(node, categoryService, relatedService, htmlService, coralSession);
             data.toTemplatingContext(templatingContext);
             prepareCategories(context, true);
         } 
         catch(Exception e)
         {
             screenError(getNode(), context, "Internal Error", e);
         }
     }
     
     public void prepareResult(Context context)
     throws ProcessingException
     {
         // does nothing
     }
 }
