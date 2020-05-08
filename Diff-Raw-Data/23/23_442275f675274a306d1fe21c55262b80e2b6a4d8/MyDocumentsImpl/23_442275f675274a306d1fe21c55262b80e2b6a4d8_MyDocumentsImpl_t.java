 package net.cyklotron.cms.modules.views.documents;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.PrioritizedResource;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.table.EventEndComparator;
 import net.cyklotron.cms.documents.table.EventStartComparator;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.table.PriorityAndValidityStartComparator;
 import net.cyklotron.cms.structure.table.TitleComparator;
 import net.cyklotron.cms.structure.table.ValidityStartComparator;
 import net.cyklotron.cms.util.PriorityComparator;
 import net.cyklotron.cms.workflow.AutomatonResource;
 import net.cyklotron.cms.workflow.StateFilter;
 import net.cyklotron.cms.workflow.StateResource;
 import net.cyklotron.cms.workflow.StatefulResource;
 import net.cyklotron.cms.workflow.WorkflowException;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.relation.MalformedRelationQueryException;
 import org.objectledge.coral.relation.ResourceIdentifierResolver;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.ResourceListTableModel;
 import org.objectledge.coral.table.comparator.TimeComparator;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.table.InverseFilter;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.comparator.Direction;
 
 import bak.pcj.LongIterator;
 import bak.pcj.set.LongOpenHashSet;
 import bak.pcj.set.LongSet;
 
 /**
  * This class contains logic shared between MyDocuments state of ProposeDocument screen and
  * MyDocuments component.
  * 
  * @author rafal.krzewski@caltha.pl
  */
 public class MyDocumentsImpl
 {
     private final CoralSessionFactory coralSessionFactory;
 
     private final CategoryService categoryService;
 
     private Logger logger;
 
     private final SiteService siteService;
 
     private final WorkflowService workflowService;
 
     public MyDocumentsImpl(CoralSessionFactory coralSessionFactory,
         CategoryService categoryService, Logger logger, SiteService siteService,
         WorkflowService workflowService)
     {
         this.coralSessionFactory = coralSessionFactory;
         this.categoryService = categoryService;
         this.logger = logger;
         this.siteService = siteService;
         this.workflowService = workflowService;
     }
 
     public List<TableFilter<? super DocumentNodeResource>> excludeStatesFilter(String... excluded)
         throws EntityDoesNotExistException, AmbigousEntityNameException, WorkflowException
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
         List<TableFilter<? super DocumentNodeResource>> filters = new ArrayList<>();
         ResourceClass<?> navigationNodeClass = coralSession.getSchema().getResourceClass(
             "structure.navigation_node");
         Resource cmsRoot = coralSession.getStore().getUniqueResourceByPath("/cms");
         AutomatonResource automaton = workflowService.getPrimaryAutomaton(coralSession, cmsRoot,
             navigationNodeClass);
         Set<StateResource> rejectedStates = new HashSet<StateResource>();
         for(String exState : excluded)
         {
             rejectedStates.add(workflowService.getState(coralSession, automaton, exState));
         }
         filters.add(new InverseFilter<StatefulResource>(new StateFilter(rejectedStates, true)));
         return filters;
     }
 
     public List<TableFilter<? super DocumentNodeResource>> statesFilter(String... include)
         throws EntityDoesNotExistException, AmbigousEntityNameException, WorkflowException
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
         List<TableFilter<? super DocumentNodeResource>> filters = new ArrayList<>();
         ResourceClass<?> navigationNodeClass = coralSession.getSchema().getResourceClass(
             "structure.navigation_node");
         Resource cmsRoot = coralSession.getStore().getUniqueResourceByPath("/cms");
         AutomatonResource automaton = workflowService.getPrimaryAutomaton(coralSession, cmsRoot,
             navigationNodeClass);
         Set<StateResource> rejectedStates = new HashSet<StateResource>();
         for(String inState : include)
         {
             rejectedStates.add(workflowService.getState(coralSession, automaton, inState));
         }
         filters.add(new StateFilter(rejectedStates, false));
         return filters;
     }
 
     public TableModel<DocumentNodeResource> siteBasedModel(CmsData cmsData, Locale locale,
         String whereClause)
         throws MalformedQueryException, TableException
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
         if(!whereClause.isEmpty())
         {
             whereClause = " AND " + whereClause;
         }
         String query = "FIND RESOURCE FROM documents.document_node WHERE site ="
             + cmsData.getSite().getIdString() + whereClause;
         List<DocumentNodeResource> myDocuments = (List<DocumentNodeResource>)coralSession
             .getQuery().executeQuery(query).getList(1);
 
         return new MyDocumentsResourceListTableModel<DocumentNodeResource>(coralSession, logger,
             myDocuments, locale);
     }
 
     public TableModel<DocumentNodeResource> queryBasedModel(CategoryQueryResource includeQuery,
         CategoryQueryResource excludeQuery, CmsData cmsData, Locale locale, String whereClause)
         throws SiteException, MalformedQueryException, MalformedRelationQueryException,
         EntityDoesNotExistException, TableException
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
         if(!whereClause.isEmpty())
         {
             whereClause = " WHERE " + whereClause;
         }
         String resQuery = "FIND RESOURCE FROM documents.document_node" + whereClause;
 
         QueryResults qr = coralSession.getQuery().executeQuery(resQuery);
         LongSet ids = new LongOpenHashSet(Math.max(1, qr.rowCount()));
         for(QueryResults.Row r : qr.getList())
         {
             ids.add(r.getId());
         }
 
         String catQuery;
         if(includeQuery.getQuery("").trim().length() > 0)
         {
             catQuery = includeQuery.getQuery().replace(";", "") + " * "
                 + siteClause(includeQuery, coralSession) + ";";
         }
         else
         {
             catQuery = siteClause(includeQuery, coralSession) + ";";
         }
 
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
 
         return new MyDocumentsResourceListTableModel<DocumentNodeResource>(coralSession, logger,
             documentList, locale);
     }
 
     private String siteClause(CategoryQueryResource query, CoralSession coralSession)
         throws SiteException
     {
         StringBuilder buff = new StringBuilder();
         if(query.isAcceptedSitesDefined() && query.getAcceptedSites().trim().length() > 0)
         {
             buff.append("MAP('structure.SiteDocs') { ");
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
                         ids = new LongOpenHashSet(Math.max(1, categories.length));
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
 
     public CategoryQueryResource getQueryResource(String name, Parameters config)
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
 
         long id = config.getLong(name + "_query_id", -1l);
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
      * Implementation of Table model for hand weighted CMS resources
      */
     public class MyDocumentsResourceListTableModel<T extends Resource>
         extends ResourceListTableModel<T>
     {
         private final CoralSession coralSession;
 
         private final Logger logger;
 
         public MyDocumentsResourceListTableModel(CoralSession coralSession, Logger logger,
             List<T> list, Locale locale)
             throws TableException
         {
             super(list, locale);
             this.coralSession = coralSession;
             this.logger = logger;
             this.columns = getColumns(locale, list);
         }
 
         protected TableColumn<T>[] getColumns(Locale locale, List<T> list)
             throws TableException
         {
             TableColumn<T>[] cols = super.getColumns(locale, list);
            TableColumn<?>[] newCols = new TableColumn[cols.length + 7];
             for(int i = 0; i < cols.length; i++)
             {
                 newCols[i] = cols[i];
             }
            newCols[cols.length] = new TableColumn<PrioritizedResource>("priority",
                 new PriorityComparator<PrioritizedResource>());
            newCols[cols.length + 1] = new TableColumn<NavigationNodeResource>("validity.start",
                 new ValidityStartComparator(TimeComparator.Direction.ASC),
                 new ValidityStartComparator(TimeComparator.Direction.DESC));
            newCols[cols.length + 2] = new TableColumn<NavigationNodeResource>(
                 "priority.validity.start", new PriorityAndValidityStartComparator(
                     TimeComparator.Direction.ASC), new PriorityAndValidityStartComparator(
                     TimeComparator.Direction.DESC));
            newCols[cols.length + 3] = new TableColumn<DocumentNodeResource>("event.start",
                 new EventStartComparator(TimeComparator.Direction.ASC), new EventStartComparator(
                     TimeComparator.Direction.DESC));
            newCols[cols.length + 4] = new TableColumn<DocumentNodeResource>("event.end",
                 new EventEndComparator(TimeComparator.Direction.ASC), new EventEndComparator(
                     TimeComparator.Direction.DESC));
            newCols[cols.length + 5] = new TableColumn<NavigationNodeResource>("title",
                 new TitleComparator(locale, Direction.ASC), new TitleComparator(locale,
                     Direction.DESC));
             List<String> myDocumentsStateOrderList = Arrays.asList((new String[]{ "PUBLISHED", "REJECTED",
                 "PENDING", "DAMAEGED", "UPDATE_REQUEST", "REMOVE_REQUEST" }));            
            newCols[cols.length + 6] = new TableColumn<DocumentNodeResource>("state",
                 new MyDocumentsStateComparator<DocumentNodeResource>(coralSession, logger, myDocumentsStateOrderList, Direction.ASC),
                 new MyDocumentsStateComparator<DocumentNodeResource>(coralSession, logger, myDocumentsStateOrderList, Direction.DESC));
             return (TableColumn<T>[])newCols;
         }
     }
 }
