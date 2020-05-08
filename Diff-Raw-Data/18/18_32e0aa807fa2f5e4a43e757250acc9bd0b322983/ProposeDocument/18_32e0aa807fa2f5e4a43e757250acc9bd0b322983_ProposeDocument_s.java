 package net.cyklotron.cms.modules.views.documents;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.AuthenticationContext;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.relation.MalformedRelationQueryException;
 import org.objectledge.coral.relation.ResourceIdentifierResolver;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.ResourceListTableModel;
 import org.objectledge.html.HTMLService;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.InverseFilter;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.documents.DocumentService;
 import net.cyklotron.cms.organizations.Organization;
 import net.cyklotron.cms.organizations.OrganizationRegistryService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.internal.OrganizationData;
 import net.cyklotron.cms.structure.internal.ProposedDocumentData;
 import net.cyklotron.cms.style.StyleService;
 import net.cyklotron.cms.workflow.AutomatonResource;
 import net.cyklotron.cms.workflow.StateFilter;
 import net.cyklotron.cms.workflow.StateResource;
 import net.cyklotron.cms.workflow.StatefulResource;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 import bak.pcj.LongIterator;
 import bak.pcj.set.LongOpenHashSet;
 import bak.pcj.set.LongSet;
 
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
 
     private final OrganizationRegistryService organizationsRegistry;
 
     private final WorkflowService workflowService;
 
     private final List<String> REQUIRES_AUTHENTICATED_USER = Arrays.asList("MyDocuments",
         "EditDocument", "RemovalRequest", "RedactorsNote");
 
     private final DocumentService documentService;
 
     private final SiteService siteService;
 
     public ProposeDocument(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager, CategoryService categoryService,
         RelatedService relatedService, HTMLService htmlService, WorkflowService workflowService,
         OrganizationRegistryService ngoDatabaseService, DocumentService documentService,
         SiteService siteService)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
         this.categoryService = categoryService;
         this.relatedService = relatedService;
         this.htmlService = htmlService;
         this.workflowService = workflowService;
         this.organizationsRegistry = ngoDatabaseService;
         this.documentService = documentService;
         this.siteService = siteService;
     }
 
     @Override
     public String getState()
         throws ProcessingException
     {
         // this method is called multiple times during rendering, so it makes sense to cache the
         // evaluated state
         String state = (String)context.getAttribute(getClass().getName() + ".state");
         if(state == null)
         {
             Parameters parameters = RequestParameters.getRequestParameters(context);
             AuthenticationContext authContext = context.getAttribute(AuthenticationContext.class);
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             long mainCategoryRoot = screenConfig.getLong("category_id_2", -1);
             long[] selectedCategories = parameters.getLongs("selected_categories");
             state = parameters.get("state", null);
             if(state == null)
             {
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
             if("AddDocument".equals(state) && mainCategoryRoot != -1
                 && selectedCategories.length == 0)
             {
                 state = "DocumentCategory";
             }
             context.setAttribute(getClass().getName() + ".state", state);
         }
         return state;
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
         try
         {
             Parameters requestParameters = context.getAttribute(RequestParameters.class);
             CoralSession coralSession = context.getAttribute(CoralSession.class);
             Subject userSubject = coralSession.getUserSubject();
             if("AddDocument".equals(state) || "MyDocuments".equals(state)
                 || "DocumentCategory".equals(state))
             {
                 Permission submitPermission = coralSession.getSecurity().getUniquePermission(
                     "cms.structure.submit");
                 CmsData cmsData = cmsDataFactory.getCmsData(context);
                 Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
                 long parentId = screenConfig.getLong("parent_id", -1L);
                 Resource parent = parentId != -1L ? NavigationNodeResourceImpl
                     .getNavigationNodeResource(coralSession, parentId) : cmsData.getNode();
                 return userSubject.hasPermission(parent, submitPermission);
             }
             if("EditDocument".equals(state) || "RemovalRequest".equals(state)
                 || "RedactorsNote".equals(state))
             {
                 long id = requestParameters.getLong("doc_id", -1);
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
                 if(node.getCreatedBy().equals(userSubject)
                     && userSubject.hasPermission(node, modifyOwnPermission))
                 {
                     return true;
                 }
             }
             return false;
         }
         catch(Exception e)
         {
             throw new ProcessingException(e);
         }
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
      * Set main category for new document.
      */
     public void prepareDocumentCategory(Context context)
         throws ProcessingException
     {
         try
         {
             prepareCategories(context, true);
         }
         catch(Exception e)
         {
             throw new ProcessingException("internal errror", e);
         }
     }
 
     /**
      * Submitted documents list for an authenticated user.
      */
     public void prepareMyDocuments(Context context)
         throws ProcessingException
     {
         try
         {
             I18nContext i18nContext = context.getAttribute(I18nContext.class);
             TemplatingContext templatingContext = context.getAttribute(TemplatingContext.class);
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             CoralSession coralSession = context.getAttribute(CoralSession.class);
             CategoryQueryResource includeQuery = getQueryResource("include", screenConfig,
                 coralSession);
             CategoryQueryResource excludeQuery = getQueryResource("exclude", screenConfig,
                 coralSession);
 
             TableModel<DocumentNodeResource> model;
 
             if(includeQuery == null)
             {
                 model = siteBasedModel(cmsData, i18nContext, coralSession);
             }
             else
             {
                 model = queryBasedModel(includeQuery, excludeQuery, cmsData, i18nContext,
                     coralSession);
             }
 
             List<TableFilter<? super DocumentNodeResource>> filters = new ArrayList<>();
             ResourceClass<?> navigationNodeClass = coralSession.getSchema().getResourceClass(
                 "structure.navigation_node");
             Resource cmsRoot = coralSession.getStore().getUniqueResourceByPath("/cms");
             AutomatonResource automaton = workflowService.getPrimaryAutomaton(coralSession,
                 cmsRoot, navigationNodeClass);
             Set<StateResource> rejectedStates = new HashSet<StateResource>();
             rejectedStates.add(workflowService.getState(coralSession, automaton, "expired"));
             filters.add(new InverseFilter<StatefulResource>(new StateFilter(rejectedStates,
                 true)));
 
             TableState state = tableStateManager.getState(context, this.getClass().getName()
                 + ":MyDocuments");
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setSortColumnName("creation.time");
                 state.setAscSort(false);
                 state.setPageSize(20);
             }
             templatingContext.put("table", new TableTool<DocumentNodeResource>(state, filters,
                 model));
             templatingContext.put("documentState", new DocumentStateTool(coralSession, logger));
         }
         catch(Exception e)
         {
             throw new ProcessingException("internal errror", e);
         }
     }
 
     private TableModel<DocumentNodeResource> siteBasedModel(CmsData cmsData,
         I18nContext i18nContext, CoralSession coralSession)
         throws MalformedQueryException, TableException
     {
         String query = "FIND RESOURCE FROM documents.document_node WHERE created_by = "
             + coralSession.getUserSubject().getIdString() + " AND site = "
             + cmsData.getSite().getIdString();
 
         List<DocumentNodeResource> myDocuments = (List<DocumentNodeResource>)coralSession
             .getQuery().executeQuery(query).getList(1);
 
         return new ResourceListTableModel<DocumentNodeResource>(myDocuments,
             i18nContext.getLocale());
     }
 
     private TableModel<DocumentNodeResource> queryBasedModel(CategoryQueryResource includeQuery,
         CategoryQueryResource excludeQuery, CmsData cmsData, I18nContext i18nContext,
         CoralSession coralSession)
         throws SiteException, MalformedQueryException, MalformedRelationQueryException,
         EntityDoesNotExistException, TableException
     {
         String resQuery = "FIND RESOURCE FROM documents.document_node WHERE created_by = "
             + coralSession.getUserSubject().getIdString();
 
         QueryResults qr = coralSession.getQuery().executeQuery(resQuery);
         LongSet ids = new LongOpenHashSet(qr.rowCount());
         for(QueryResults.Row r : qr.getList())
         {
             ids.add(r.getId());
         }
 
        String catQuery = includeQuery.getQuery().replace(";", "")
            + siteClause(includeQuery, coralSession) + ";";
 
         final ResourceIdentifierResolver resolver = getResolver(coralSession);
 
         LongSet included = coralSession.getRelationQuery().queryIds(catQuery, resolver, ids);
 
         if(excludeQuery != null)
         {
             LongSet excluded = coralSession.getRelationQuery().queryIds(excludeQuery.getQuery(),
                 resolver, included);
             included.removeAll(excluded);
         }
 
         List<DocumentNodeResource> documentList = new ArrayList<>(included.size());
         LongIterator i = included.iterator();
         while(i.hasNext())
         {
             documentList.add((DocumentNodeResource)coralSession.getStore().getResource(i.next()));
         }
 
         return new ResourceListTableModel<DocumentNodeResource>(documentList, i18nContext.get());
     }
 
     private String siteClause(CategoryQueryResource query, CoralSession coralSession)
         throws SiteException
     {
         StringBuilder buff = new StringBuilder();
        if(query.isAcceptedSitesDefined())
         {
            buff.append(" * MAP('structure.SiteDocs') { ");
             String[] siteNames = query.getAcceptedSiteNames();
             for(int i = 0; i < siteNames.length; i++)
             {
                 SiteResource site = siteService.getSite(coralSession, siteNames[i]);
                 buff.append("RES(").append(site.getIdString()).append(")");
                 if(i < siteNames.length - 1)
                 {
                     buff.append(" + ");
                 }
             }
             buff.append(" }");
         }
         return buff.toString();
     }
 
     private ResourceIdentifierResolver getResolver(final CoralSession coralSession)
     {
         return new ResourceIdentifierResolver()
             {
                 @Override
                 public LongSet resolveIdentifier(String identifier)
                     throws EntityDoesNotExistException
                 {
                     LongSet ids;
                     Resource res = null;
                     if(identifier.startsWith("/"))
                     {
                         Resource[] ra = coralSession.getStore().getResourceByPath(identifier);
                         if(ra.length == 0)
                         {
                             throw new EntityDoesNotExistException("resource " + identifier
                                 + " does not exist");
                         }
                         if(ra.length > 0)
                         {
                             throw new EntityDoesNotExistException("path " + identifier
                                 + " is ambiguous");
                         }
                         res = ra[0];
                     }
                     else if(identifier.matches("\\d+"))
                     {
                         res = coralSession.getStore().getResource(Long.parseLong(identifier));
                     }
                     else
                     {
                         throw new EntityDoesNotExistException("malformed identifier " + identifier);
                     }
                     if(res instanceof SiteResource)
                     {
                         ids = new LongOpenHashSet(1);
                         ids.add(res.getId());
                     }
                     else if(res instanceof CategoryResource)
                     {
                         CategoryResource[] categories = categoryService.getSubCategories(
                             coralSession, (CategoryResource)res, true);
                         ids = new LongOpenHashSet(categories.length);
                         for(int i = 0; i < categories.length; i++)
                         {
                             ids.add(categories[i].getId());
                         }
                     }
                     else
                     {
                         throw new EntityDoesNotExistException(identifier + " has unexpected type "
                             + res.getResourceClass().getName());
                     }
                     return ids;
                 }
             };
     }
 
     private CategoryQueryResource getQueryResource(String name, Parameters screenConfig,
         CoralSession coralSession)
     {
         long id = screenConfig.getLong(name + "_query_id", -1l);
         if(id != -1l)
         {
             try
             {
                 return (CategoryQueryResource)coralSession.getStore().getResource(id);
             }
             catch(EntityDoesNotExistException | ClassCastException e)
             {
             }
         }
         return null;
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
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             // refill parameters in case we are coming back failed validation
             Parameters screenConfig = getScreenConfig();
             ProposedDocumentData data = new ProposedDocumentData(screenConfig,
                 documentService.getPreferredImageSizes(), logger);
             data.fromParameters(parameters, coralSession);
             if(!parameters.getBoolean("form_loaded", false))
             {
                 long organizationId = parameters.getLong("organizationId", -1L);
                 if(organizationId > 0)
                 {
                     Organization organization = this.organizationsRegistry
                         .getOrganization(organizationId);
                     if(organization != null)
                     {
                         List<OrganizationData> organizationDataList = new ArrayList<OrganizationData>();
                         OrganizationData organizationData = new OrganizationData();
                         organizationData.fromOrganization(organization);
                         organizationDataList.add(organizationData);
                         data.setOrganizations(organizationDataList);
                     }
                 }
             }
             data.toTemplatingContext(templatingContext);
             prepareCategories(context, true);
             // resolve parent node in case template needs it for security check
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             long parentId = screenConfig.getLong("parent_id", -1L);
             Resource parentNode = parentId != -1L ? NavigationNodeResourceImpl
                 .getNavigationNodeResource(coralSession, parentId) : cmsData.getNode();
             templatingContext.put("parent_node", parentNode);
             templatingContext.put("add_captcha", screenConfig.getBoolean("add_captcha", false));
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
     public void prepareEditDocument(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             long docId = parameters.getLong("doc_id");
             DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(
                 coralSession, docId);
             templatingContext.put("doc", node);
             ProposedDocumentData data = new ProposedDocumentData(getScreenConfig(),
                 documentService.getPreferredImageSizes(), logger);
             if(parameters.getBoolean("form_loaded", false))
             {
                 data.fromParameters(parameters, coralSession);
             }
             else
             {
                 if(node.isProposedContentDefined())
                 {
                     data.fromProposal(node, coralSession);
                 }
                 else
                 {
                     data.fromNode(node, categoryService, relatedService, coralSession);
                     data.cleanupContent(htmlService);
                 }
             }
             data.toTemplatingContext(templatingContext);
             prepareCategories(context, true);
         }
         catch(Exception e)
         {
             screenError(getNode(), context, "Internal Error", e);
         }
     }
 
     /**
      * Send removal request previously submitted document.
      * 
      * @param context
      * @throws ProcessingException
      */
     public void prepareRemovalRequest(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             long docId = parameters.getLong("doc_id");
             DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(
                 coralSession, docId);
             templatingContext.put("doc", node);
             ProposedDocumentData data = new ProposedDocumentData(getScreenConfig(),
                 documentService.getPreferredImageSizes(), logger);
             if(parameters.getBoolean("form_loaded", false))
             {
                 data.fromParameters(parameters, coralSession);
             }
             else
             {
                 if(node.isProposedContentDefined())
                 {
                     data.fromProposal(node, coralSession);
                 }
                 else
                 {
                     data.fromNode(node, categoryService, relatedService, coralSession);
                     data.cleanupContent(htmlService);
                 }
             }
             data.toTemplatingContext(templatingContext);
             prepareCategories(context, true);
         }
         catch(Exception e)
         {
             screenError(getNode(), context, "Internal Error", e);
         }
     }
 
     /**
      * Receive redactor's note.
      * 
      * @param context
      * @throws ProcessingException
      */
     public void prepareRedactorsNote(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
 
         try
         {
             long docId = parameters.getLong("doc_id");
             DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(
                 coralSession, docId);
             templatingContext.put("doc", node);
             prepareCategories(context, true);
         }
         catch(Exception e)
         {
             screenError(getNode(), context, "Internal Error", e);
         }
     }
 }
