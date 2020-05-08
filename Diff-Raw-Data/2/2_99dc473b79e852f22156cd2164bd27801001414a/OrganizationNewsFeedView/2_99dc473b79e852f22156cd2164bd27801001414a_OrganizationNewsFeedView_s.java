 package net.cyklotron.cms.modules.views.ngodatabase;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.datatypes.DateAttributeHandler;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.DefaultParameters;
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
 import org.objectledge.templating.MergingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.TemplateNotFoundException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.builders.BuildException;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.LinkRenderer;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.modules.views.syndication.BaseSyndicationScreen;
 import net.cyklotron.cms.ngodatabase.NgoDatabaseService;
 import net.cyklotron.cms.ngodatabase.Organization;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteResourceImpl;
 import net.cyklotron.cms.structure.table.ValidityStartFilter;
 import net.cyklotron.cms.syndication.CannotExecuteQueryException;
 import net.cyklotron.cms.syndication.CannotGenerateFeedException;
 import net.cyklotron.cms.syndication.SyndicationService;
 import net.cyklotron.cms.util.CmsResourceListTableModel;
 import net.cyklotron.cms.util.OfflineLinkRenderingService;
 import net.cyklotron.cms.util.ProtectedValidityFilter;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedOutput;
 
 /**
  * View organization's news outgoing feed.
  * 
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: Download.java,v 1.6 2006-01-02 11:42:17 rafal Exp $
  */
 
 public class OrganizationNewsFeedView
     extends BaseSyndicationScreen
 {
 
     private IntegrationService integrationService;
 
     private I18nContext i18nContext;
 
     private OfflineLinkRenderingService offlineLinkRenderingService;
 
     private NgoDatabaseService ngoDatabaseService;
 
     public OrganizationNewsFeedView(Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, SyndicationService syndicationService,
         IntegrationService integrationService, NgoDatabaseService ngoDatabaseService,
         OfflineLinkRenderingService offlineLinkRenderingService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager,
                         syndicationService);
         this.integrationService = integrationService;
         this.offlineLinkRenderingService = offlineLinkRenderingService;
         this.ngoDatabaseService = ngoDatabaseService;
     }
 
     @Override
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         throw new UnsupportedOperationException("Implemented the calling method 'build'");
     }
 
     @Override
     public String build(Template template, String embeddedBuildResults)
         throws BuildException
     {
         HttpContext httpContext = HttpContext.getHttpContext(context);
         SyndFeedOutput output = new SyndFeedOutput();
 
         try
         {
             SyndFeed feed = buildFeed(context);
             httpContext.setContentType("text/xml");
             httpContext.getResponse().addDateHeader("Last-Modified", (new Date()).getTime());
             httpContext.setEncoding("UTF-8");
             Writer writer = httpContext.getPrintWriter();
             output.output(feed, writer);
             writer.flush();
             writer.close();
         }
         catch(IOException e)
         {
             throw new BuildException("Could not get the output stream", e);
         }
         catch(FeedException e)
         {
             throw new BuildException("Could not get the output stream", e);
         }
         catch(ProcessingException e)
         {
             throw new BuildException("Could not get the output stream", e);
         }
         catch(MergingException e)
         {
             throw new BuildException("Could not get the output stream", e);
         }
         catch(CannotExecuteQueryException e)
         {
             throw new BuildException("Cannot execute query:", e);
         }
         catch(CannotGenerateFeedException e)
         {
             throw new BuildException("Cannot generate feed:", e);
         }
         catch(TableException e)
         {
             throw new BuildException("Table exception", e);
         }
         catch(TemplateNotFoundException e)
         {
             throw new BuildException("Template not found exception", e);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new BuildException("Entity does not exist", e);
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean requiresSecureChannel(Context context)
         throws Exception
     {
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         return true;
     }
 
     public SyndFeed buildFeed(Context context)
         throws CannotExecuteQueryException, TableException, CannotGenerateFeedException,
         TemplateNotFoundException, MergingException, EntityDoesNotExistException,
         ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         Parameters parameters = RequestParameters.getRequestParameters(context);
         Long siteId = parameters.getLong("site_id", -1L);
         Long organizedId = parameters.getLong("organization_id", -1L);
         Integer range = parameters.getInt("range", 30);
 
         Resource[] resources;
         QueryResults results;
 
         try
         {
             results = coralSession.getQuery().executeQuery(
                 "FIND RESOURCE FROM documents.document_node WHERE site = " + siteId.toString()
                     + " AND organisationIds LIKE '%," + organizedId.toString() + ",%'");
         }
         catch(MalformedQueryException e)
         {
             throw new ProcessingException("cannot get 'documents.document_node' resources", e);
         }
         resources = results.getArray(1);
 
         TableState state = new TableState(getClass().getName(), 1);
 
         TableModel model = null;
         try
         {
             I18nContext i18nContext = I18nContext.getI18nContext(context);
             model = new CmsResourceListTableModel(context, integrationService, resources,
                 i18nContext.getLocale());
         }
         catch(TableException e)
         {
             throw e;
         }
 
         List<TableFilter> filters = new ArrayList<TableFilter>(2);
         Subject anonymousSubject = coralSession.getSecurity().getSubject(Subject.ANONYMOUS);
         filters.add(new ProtectedValidityFilter(coralSession, anonymousSubject, new Date()));
 
         Calendar calendar = Calendar.getInstance();
         if(range > 0)
         {
             calendar.add(Calendar.DAY_OF_MONTH, -range);
             filters.add(new ValidityStartFilter(calendar.getTime(), null));
         }
 
         TableTool tableTool = new TableTool(state, filters, model);
         List rows = tableTool.getRows();
 
         List entries = new ArrayList();
         SyndEntry entry;
         SyndContent description;
 
         int i = 0;
         for(Iterator iter = rows.iterator(); iter.hasNext(); i++)
         {
             TableRow row = (TableRow)iter.next();
             DocumentNodeResource doc = (DocumentNodeResource)row.getObject();
             entry = new SyndEntryImpl();
             entry.setTitle(doc.getTitle());
             entry.setLink(getDocLink(coralSession, doc));
 
             if(doc.getValidityStart() == null)
             {
                 entry.setPublishedDate(doc.getCreationTime());
             }
             else
             {
                 entry.setPublishedDate(doc.getValidityStart());
             }
             String docDescription = "";
             if(doc.getAbstract() != null)
             {
                 docDescription = doc.getAbstract();
             }
             description = new SyndContentImpl();
             description.setType("text/plain");
             description.setValue(docDescription);
             entry.setDescription(description);
             entries.add(entry);
         }
 
         SyndFeed feed = new SyndFeedImpl();
         feed.setFeedType("rss_2.0");
 
         Organization organization = ngoDatabaseService.getOrganization(organizedId);
         String organizationName = organization != null ? organization.getName() : "";
        feed.setTitle("Zestaw wiadomosci dodanych do serwisu ngo.pl przez organizację: "
             + organizationName);
 
         DateFormat dateFormat = new SimpleDateFormat(DateAttributeHandler.DATE_TIME_FORMAT);
         String dateFrom = dateFormat.format(calendar.getTime());
         String dateTo = dateFormat.format((new Date()).getTime());
         feed.setDescription("Zestaw zawiera wiadomości opublikowane w okresie od: " + dateFrom
             + " do:" + dateTo);
 
         feed.setLink(getFeedLink(coralSession, siteId, organizedId, range));
         feed.setPublishedDate(new Date());
         feed.setEncoding("UTF-8");
         feed.setEntries(entries);
 
         return feed;
     }
 
     private String getFeedLink(CoralSession coralSession, Long siteId, Long organizationId,
         Integer range)
         throws EntityDoesNotExistException
     {
         Parameters queryStringParameters = new DefaultParameters();
         queryStringParameters.set("site_id", siteId);
         queryStringParameters.set("organization_id", organizationId);
         queryStringParameters.set("range", range);
         SiteResource site = SiteResourceImpl.getSiteResource(coralSession, siteId);
 
         return offlineLinkRenderingService.getViewURL(coralSession, site,
             "ngodatabase.OrganizationNewsFeedView", null, queryStringParameters);
     }
 
     private String getDocLink(CoralSession coralSession, DocumentNodeResource doc)
     {
         LinkRenderer linkRenderer = offlineLinkRenderingService.getLinkRenderer();
         try
         {
             return linkRenderer.getNodeURL(coralSession, doc);
         }
         catch(ProcessingException e)
         {
             return null;
         }
     }
 }
