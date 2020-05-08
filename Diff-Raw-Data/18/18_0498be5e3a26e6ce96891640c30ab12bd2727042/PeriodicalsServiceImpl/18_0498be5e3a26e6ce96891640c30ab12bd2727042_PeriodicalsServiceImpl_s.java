 package net.cyklotron.cms.periodicals.internal;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.mail.Header;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeUtility;
 
 import org.jcontainer.dna.Configuration;
 import org.jcontainer.dna.ConfigurationException;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.query.FilteredQueryResults;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.schema.AttributeHandler;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.comparator.CreationTimeComparator;
 import org.objectledge.coral.table.comparator.ModificationTimeComparator;
 import org.objectledge.coral.table.comparator.NameComparator;
 import org.objectledge.coral.table.comparator.TimeComparator;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.i18n.I18n;
 import org.objectledge.mail.LedgeMessage;
 import org.objectledge.mail.MailSystem;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.MergingException;
 import org.objectledge.templating.TemplateNotFoundException;
 import org.objectledge.utils.StringUtils;
 
 import net.cyklotron.cms.CmsTool;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.LinkRenderer;
 import net.cyklotron.cms.documents.table.EventEndComparator;
 import net.cyklotron.cms.documents.table.EventStartComparator;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesException;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.periodicals.EmailPeriodicalResource;
 import net.cyklotron.cms.periodicals.EmailPeriodicalsRootResource;
 import net.cyklotron.cms.periodicals.PeriodicalRenderer;
 import net.cyklotron.cms.periodicals.PeriodicalRendererFactory;
 import net.cyklotron.cms.periodicals.PeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalsException;
 import net.cyklotron.cms.periodicals.PeriodicalsNodeResource;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.periodicals.PeriodicalsSubscriptionService;
 import net.cyklotron.cms.periodicals.PublicationTimeResource;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.table.PriorityAndValidityStartComparator;
 import net.cyklotron.cms.structure.table.SequenceComparator;
 import net.cyklotron.cms.structure.table.TitleComparator;
 import net.cyklotron.cms.structure.table.ValidityStartComparator;
 import net.cyklotron.cms.util.PriorityComparator;
 import net.cyklotron.cms.util.SiteFilter;
 
 import bak.pcj.LongIterator;
 import bak.pcj.set.LongOpenHashSet;
 import bak.pcj.set.LongSet;
 
 /**
  * A generic implementation of the periodicals service.
  * 
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: PeriodicalsServiceImpl.java,v 1.46 2008-08-21 14:28:03 rafal Exp $
  */
 public class PeriodicalsServiceImpl 
     implements PeriodicalsService
 {
     // constants ////////////////////////////////////////////////////////////
     
     /** sendingEnabled parameter key. */
     public static final String SENDING_ENABLED_KEY = "send";
 
     /** serverName parameter key. */
     public static final String SERVER_NAME_KEY = "server";
 
     /** port parameter key. */
     public static final String PORT_KEY = "port";
     
     /** port default value. */
     public static final int PORT_DEFAULT = 80;
     
     /** context parameter key. */
     public static final String CONTEXT_KEY = "context";
     
     /** context parameter default value. */
     public static final String CONTEXT_DEFAULT = "/";
     
     /** servletAndApp parameter key. */
     public static final String SERVLET_AND_APP_KEY = "servletAndApp";
     
     /** servletAndApp defaault value. */
     public static final String SERVLET_AND_APP_DEFAULT = "ledge/";
     
     /** messages from parameter key. */
     public static final String MESSAGES_FROM_KEY = "messagesFrom";
     
     // instance variables ///////////////////////////////////////////////////
     
     /** category query service. */
     private CategoryQueryService categoryQueryService;
     
     /** cms files service */
     private FilesService cmsFilesService;
     
     /** ledge file system */
     private FileSystem fileSystem;
     
     /** mail service */
     private MailSystem mailSystem;
     
     /** site service. */
     private SiteService siteService;
     
     /** log */
     private Logger log;
     
     /** renderer classes */
     private Map<String, PeriodicalRendererFactory> rendererFactories = new HashMap<String, PeriodicalRendererFactory>();
 
     /** default server name used when no site alias is selected. */
     private String serverName;
 
     /** server port to use. "80" by default. */
     private int port;
     
     /** context name. "/" by default. */
     private String context;
     
     /** servlet name and application bit. "ledge/" by default. */
     private String servletAndApp;
     
     /** messages from address. */
     private String messagesFrom;
     
     private final LinkRenderer linkRenderer;
 
     private final PeriodicalsSubscriptionService subscriptionService;
     
     private final boolean sendingEnabled;
 
     public PeriodicalsServiceImpl(Configuration config, Logger logger,
         CategoryQueryService categoryQueryService, FilesService cmsFilesService,
         FileSystem fileSystem, MailSystem mailSystem, I18n i18n, SiteService siteService,
         PeriodicalsSubscriptionService subscriptionService,
         PeriodicalRendererFactory[] renderers)
         throws ConfigurationException
     {
         this.categoryQueryService = categoryQueryService;
         this.cmsFilesService = cmsFilesService;
         this.fileSystem = fileSystem;
         this.mailSystem = mailSystem;
         this.siteService = siteService;
         this.log = logger;
         this.subscriptionService = subscriptionService;
         for (int i = 0; i < renderers.length; i++)
         {
             rendererFactories.put(renderers[i].getRendererName(), renderers[i]);
         }
         sendingEnabled = config.getChild(SENDING_ENABLED_KEY).getValueAsBoolean(true);
         serverName = config.getChild(SERVER_NAME_KEY).getValue(); // no default
         port = config.getChild(PORT_KEY).getValueAsInteger(PORT_DEFAULT);
         context = config.getChild(CONTEXT_KEY).getValue(CONTEXT_DEFAULT);
         servletAndApp = config.getChild(SERVLET_AND_APP_KEY).
             getValue(SERVLET_AND_APP_DEFAULT);
         messagesFrom = config.getChild(MESSAGES_FROM_KEY).
             getValue("noreply@"+serverName);
         linkRenderer = new PeriodicalsLinkRenderer(serverName, port, context, servletAndApp,
             siteService, log);
     }
     
     /**
      * Get root node of application's data.
      * 
      * @param coralSession CoralSession.
      * @param site the site.
      * @return root node of application data.
      * @throws PeriodicalsException
      */
     public PeriodicalsNodeResource getApplicationRoot(CoralSession coralSession, SiteResource site)
         throws PeriodicalsException
     {
         return PeriodicalsServiceUtils.getApplicationRoot(coralSession, site);
     }
     
     /**
      * Return the root node for periodicals
      * 
      * @param site the site.
      * @return the periodicals root.
      */
     public PeriodicalsNodeResource getPeriodicalsRoot(CoralSession coralSession, SiteResource site) 
         throws PeriodicalsException
     {
         return PeriodicalsServiceUtils.getPeriodicalsRoot(coralSession, site);    
     }
     
     /**
      * Return the root node for email periodicals
      * 
      * @param site the site.
      * @return the periodicals root.
      */
     public EmailPeriodicalsRootResource getEmailPeriodicalsRoot(CoralSession coralSession, SiteResource site)
         throws PeriodicalsException
     {
         return PeriodicalsServiceUtils.getEmailPeriodicalsRoot(coralSession, site);    
     }
 
     /**
      * List the periodicals existing in the site.
      * 
      * @param site the site.
      * @return array of periodicals.
      */
     public PeriodicalResource[] getPeriodicals(CoralSession coralSession, SiteResource site)
         throws PeriodicalsException
     {
         return PeriodicalsServiceUtils.getPeriodicals(coralSession, site);
     }
 
     /**
      * List the email periodicals existing in the site.
      * 
      * @param site the site.
      * @return array of periodicals.
      */
     public EmailPeriodicalResource[] getEmailPeriodicals(CoralSession coralSession,SiteResource site) 
         throws PeriodicalsException
     {
         return PeriodicalsServiceUtils.getEmailPeriodicals(coralSession, site);
     }
 
     // inherit doc
     public String getFromAddress()
     {
         return messagesFrom;
     }
 
     // inherit doc
     public String[] getRendererNames()
     {
         String[] result = new String[rendererFactories.size()];
         rendererFactories.keySet().toArray(result);
         return result;
     }
     
     // inheirt doc
     public List<FileResource> publishNow(CoralSession coralSession, PeriodicalResource periodical,
         boolean update, boolean send, String recipient)
         throws PeriodicalsException
     {
         Date time = new Date();
         try
         {
             List<FileResource> results = generate(coralSession, periodical, time, update);
             if(sendingEnabled && periodical instanceof EmailPeriodicalResource && send
                 && (!results.isEmpty() || ((EmailPeriodicalResource)periodical).getSendEmpty()))
             {
                 FileResource last = results.get(results.size() - 1);
                 send(coralSession, (EmailPeriodicalResource)periodical, last, time, recipient);
             }
             return results;
         }
         catch(Exception e)
         {
             throw new PeriodicalsException("processing failed", e);
         }
     }
         
     /**
      * Process periodicals defined in all sites.
      * 
      * <p>This method is supposed to be called by a scheduled job, once each
      * hour.</p>
      */
     public void processPeriodicals(CoralSession coralSession,Date time)
         throws PeriodicalsException
     {
         Set<PeriodicalResource> toProcess = findPeriodicalsToProcess(coralSession,time);
         Iterator<PeriodicalResource> i = toProcess.iterator();
         while(i.hasNext() && !Thread.interrupted())
         {
             PeriodicalResource p = i.next();
             try
             {
                 List<FileResource> results = generate(coralSession,p, time, true);
                 if(sendingEnabled && p instanceof EmailPeriodicalResource
                     && (!results.isEmpty() || ((EmailPeriodicalResource)p).getSendEmpty()))
                 {
                     FileResource last = results.get(results.size() - 1);
                     send(coralSession, (EmailPeriodicalResource)p, last, time, null);
                 }
             }
             catch(Exception e)
             {
                 log.error("periodical " + p.getPath() + " processing failed", e);
             }
         }
     }
     
     // implementation ///////////////////////////////////////////////////////
     
     private Set<PeriodicalResource> findPeriodicalsToProcess(CoralSession coralSession,Date time)
     {
         Set<PeriodicalResource> set = new HashSet<PeriodicalResource>();
         try
         {
             QueryResults results = coralSession.getQuery().executeQuery(
                 "FIND RESOURCE FROM cms.periodicals.periodical");
             Iterator<QueryResults.Row> rows = results.iterator();
             while(rows.hasNext())
             {
                 QueryResults.Row row = rows.next();
                 PeriodicalResource r = (PeriodicalResource)row.get();
                 if(shouldProcess(coralSession,r, time))
                 {
                     set.add(r);
                 }
             }
         }
         catch(Exception e)
         {
             log.error("failed to lookup periodicals", e);
         }
         return set;
     }
     
     private boolean shouldProcess(CoralSession coralSession, PeriodicalResource r, Date time)
     {
         StringBuilder buff = new StringBuilder();
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
         buff.append("considering ").append(CmsTool.getSite(r).getName()).append("/").append(r.getName());
         buff.append(", created on ").append(df.format(r.getCreationTime()));
         if(r.getLastPublished() == null) 
         {
             buff.append(", never published");
         }
         else
         {
             buff.append(", last published on ").append(df.format(r.getLastPublished()));
         }
         log.info(buff.toString());
         buff.setLength(0);
         
         boolean scheduledTimePassedSinceLastPublish = false;
         Date lastPublished = r.getLastPublished();
         // CYKLO-478: if periodical has not been published before, check if a scheduled publication time falls between the moment
         // when periodical was created and 'now'. Only this indicates missed publication time.
         if(lastPublished == null)
         {
             lastPublished = r.getCreationTime();    
         }
         PublicationTimeResource[] publicationTimes = r.getPublicationTimes(coralSession);
         long recentScheduledPublicationTime = 0;
         for(int i = 0; i < publicationTimes.length; i++)
         {
             PublicationTimeResource pt = publicationTimes[i];
             recentScheduledPublicationTime = getScheduledPublicationTimeBefore(
                 pt.getDayOfMonth(-1), pt.getHour(-1), pt.getDayOfWeek(-1), time);
             
             buff.append("most recent publication time for {");
             if(pt.getDayOfMonth(-1) != -1)
             {
                 buff.append("dom:").append(pt.getDayOfMonth()).append(", ");
             }
             if(pt.getDayOfWeek(-1) != -1)
             {
                 buff.append("dow:").append(pt.getDayOfWeek()).append(", ");
             }
             if(pt.getHour(-1) != -1)
             {
                 buff.append("h:").append(pt.getHour());
             }
             buff.append("} is ").append(df.format(new Date(recentScheduledPublicationTime)));
             log.info(buff.toString());
             buff.setLength(0);
             
             if(lastPublished.getTime() <= recentScheduledPublicationTime)
             {
                 scheduledTimePassedSinceLastPublish = true;
             }
         }
      
         Date publishAfter = r.getPublishAfter();
         if(publishAfter == null)
         {
        	publishAfter = r.getCreationTime();
         }
         boolean afterMinimalPublicationDate = time.getTime() > publishAfter.getTime();
 
         if(scheduledTimePassedSinceLastPublish)
         {
             buff.append("sheduled publication time passed on ").append(df.format(new Date(recentScheduledPublicationTime)));
         }
         else
         {
             buff.append("sheduled publication time has not passed yet");
         }
         if(r.getPublishAfter() != null)
         {
             if(afterMinimalPublicationDate)
             {
                 buff.append(", minimal publication date passed on ").append(df.format(r.getPublishAfter()));
             }
             else
             {
                 buff.append(", minimal publication date, ").append(df.format(r.getPublishAfter())).append(" has not passed yet");
             }
         }
         else
         {
             buff.append(", minimal publication date is not set");
         }
         buff.append(", periodical ");
         if(scheduledTimePassedSinceLastPublish && afterMinimalPublicationDate)
         {
             buff.append("WILL BE");
         }
         else
         {
             buff.append("WILL NOT BE");
         }
         buff.append(" published");
         log.info(buff.toString());
         
         return scheduledTimePassedSinceLastPublish && afterMinimalPublicationDate;
     }
     
     public static long getScheduledPublicationTimeBefore(int day, int hour, int weekDay, Date date)
     {
         Calendar now = Calendar.getInstance();
         now.setTime(date);
         now.set(Calendar.MINUTE, 0);
         now.set(Calendar.SECOND, 0);
         now.set(Calendar.MILLISECOND, 0);
         int hourNow = now.get(Calendar.HOUR_OF_DAY);
         if(hourNow < hour)
         {
             now.add(Calendar.DAY_OF_MONTH, -1);
         }
         now.set(Calendar.HOUR_OF_DAY, hour);
         
         if(day != -1)
         {
             int dayNow = now.get(Calendar.DAY_OF_MONTH);
             if(dayNow < day)
             {
                 now.add(Calendar.MONTH, -1);
             }
             now.set(Calendar.DAY_OF_MONTH, day);
         }
         else
         {
             int weekDayNow = now.get(Calendar.DAY_OF_WEEK);
             if(weekDayNow < weekDay)
             {
                 now.add(Calendar.DAY_OF_MONTH, -7);
             }
             now.set(Calendar.DAY_OF_WEEK, weekDay);
         }
         return now.getTimeInMillis();
     }
     
     /**
      * Generates periodical's contents using a renderer.
      * @param r the periodical.
      * @param time the generation time.
      * @param update update lastPublishedTime attribute of the periodical
      * 
      * @return returns the name of the generated file, or null on failure.
      * @throws Exception 
      */
     private List<FileResource> generate(CoralSession coralSession, PeriodicalResource r, Date time,
         boolean update)
         throws Exception
     {
         List<FileResource> results = new LinkedList<FileResource>();
         String timestamp = timestamp(time);
         Map<CategoryQueryResource, List<DocumentNodeResource>> queryResults = performQueries(coralSession, r, time);
         
         if(!(r instanceof EmailPeriodicalResource)
             || (!isQuerySetEmpty(queryResults) || ((EmailPeriodicalResource)r).getSendEmpty()))
         {
             FileResource contentFile = generate(coralSession, r, queryResults, r.getRenderer(),time, timestamp,
                 r.getTemplate(), null);
             results.add(contentFile);
             if(r instanceof EmailPeriodicalResource)
             {
                 EmailPeriodicalResource er = (EmailPeriodicalResource)r;
                 if(!er.getFullContent())
                 {
                     contentFile = generate(coralSession, r, queryResults, er.getNotificationRenderer(), time,
                        timestamp, er.getNotificationTemplate(),contentFile);
                     results.add(contentFile);
                 }
             }
         }
         else
         {
             log.info("suppressed publication of " + CmsTool.getSite(r).getName() + "/"
                 + r.getName() + " because the queries returned no documents");
         }
         if(update)
         {
             r.setLastPublished(time);
             r.update();
         }
         return results;
     }
     
     private FileResource generate(CoralSession coralSession, PeriodicalResource r,
         Map<CategoryQueryResource, List<DocumentNodeResource>> queryResults, String rendererName, Date time,
         String timestamp, String templateName, FileResource contentFile)
         throws PeriodicalsException, FilesException, ProcessingException, MergingException,
         TemplateNotFoundException, IOException, MessagingException, AmbigousEntityNameException
     {
         PeriodicalRenderer renderer = getRenderer(rendererName);
         try
         {
             if(renderer == null)
             {
                 throw new PeriodicalsException("cannot generate " + r.getPath()
                     + " because renderer " + rendererName + " is not installed");
             }
             String fileName = timestamp + "." + renderer.getFilenameSuffix();
             FileResource file;
             try
             {
                 file = (FileResource)r.getStorePlace().getChild(coralSession, fileName);
             }
             catch(EntityDoesNotExistException e)
             {
                  file = cmsFilesService.createFile(coralSession, fileName, null, renderer
                     .getMimeType(), r.getEncoding(), r.getStorePlace());
             }
             
             renderer.render(coralSession, r, queryResults, time, templateName, file, contentFile);
             return file;
         }
         finally
         {
             releaseRenderer(renderer);
         }
     }
     
     private Map<CategoryQueryResource, List<DocumentNodeResource>> performQueries(
         CoralSession coralSession, PeriodicalResource periodical, Date time)
         throws Exception
     {
         Map<CategoryQueryResource, List<DocumentNodeResource>> results = new HashMap<CategoryQueryResource, List<DocumentNodeResource>>();
         Subject anonSub = coralSession.getSecurity().getSubject(Subject.ANONYMOUS);
         @SuppressWarnings("unchecked")
         List<CategoryQueryResource> queries = (List<CategoryQueryResource>)periodical
             .getCategoryQuerySet().getQueries();
         LongSet newIdSet = getNewDocuments(coralSession, periodical);
        log.info("processing " + CmsTool.getSite(periodical).getName() + "/" + periodical.getName()
            + " considering " + newIdSet.size() + " new documents");
         for(CategoryQueryResource cq : queries)
         {
             String[] siteNames = cq.getAcceptedSiteNames();
             SiteFilter siteFilter = null;
             if(siteNames != null && siteNames.length > 0)
             {
                 siteFilter = new SiteFilter(coralSession, siteNames, siteService);
             }
             LongSet queryIdSet = categoryQueryService.forwardQueryIds(coralSession, cq.getQuery(),
                 newIdSet);
             LongIterator i = queryIdSet.iterator();
             Date now = new Date();
             List<DocumentNodeResource> temp = new ArrayList<DocumentNodeResource>();
             while(i.hasNext())
             {
                 Resource r = coralSession.getStore().getResource(i.next());
                 if(r instanceof DocumentNodeResource)
                 {
                     DocumentNodeResource doc = (DocumentNodeResource)r;
                     if(!periodical.isLastPublishedDefined() || !doc.isValidityStartDefined()
                         || doc.getValidityStart().compareTo(periodical.getLastPublished()) > 0)
                     {
                         if(doc.canView(coralSession, anonSub, now))
                         {
                             if(siteFilter == null || siteFilter.accept(doc))
                             {
                                 temp.add(doc);
                             }
                         }
                     }
                 }
             }
             Collections.sort(temp, getComparator(periodical));
             results.put(cq, temp);
             log.info("query " + cq.getName() + " matched " + temp.size() + " documents");
         }
         return results;
     }
     
     private LongSet getNewDocuments(CoralSession coralSession, PeriodicalResource periodical)
         throws Exception
     {
         if(periodical.getLastPublished() != null)
         {
             @SuppressWarnings("unchecked")
             String date = ((AttributeHandler<Date>)coralSession.getSchema()
                 .getAttributeClass("date").getHandler()).toExternalString(periodical
                 .getLastPublished());
             QueryResults docs = coralSession.getQuery().executeQuery(
                 "FIND RESOURCE FROM structure.navigation_node WHERE creation_time > " + date
                     + " SELECT id");
             LongSet ids = new LongOpenHashSet();
             Iterator<FilteredQueryResults.Row> i = docs.getFiltered().iterator();
             while(i.hasNext())
             {
                 ids.add((Long)i.next().get());
             }
             docs = coralSession.getQuery().executeQuery(
                 "FIND RESOURCE FROM structure.navigation_node WHERE  validityStart > " + date
                     + " SELECT id");
             i = docs.getFiltered().iterator();
             while(i.hasNext())
             {
                 ids.add((Long)i.next().get());
             }            
             return ids;
         }
         else
         {
             return null;
         }
     }
     
     private boolean isQuerySetEmpty(Map<CategoryQueryResource, List<DocumentNodeResource>> queryResults)
     {
         for(CategoryQueryResource cq : queryResults.keySet())
         {
             if(!queryResults.get(cq).isEmpty())
             {
                 return false;
             }
         }
         return true;
     }
     
     private Comparator<DocumentNodeResource> getComparator(PeriodicalResource periodical)
     {
         String sortOrder = periodical.isSortOrderDefined() ? periodical.getSortOrder() : "priority.validity.start";
         boolean sortDirectionAsc = periodical.isSortDirectionDefined() ? "asc".equals(periodical.getSortDirection()) : true;
         Comparator<DocumentNodeResource> comp = new PriorityAndValidityStartComparator<DocumentNodeResource>(
             sortDirectionAsc ? TimeComparator.Direction.ASC : TimeComparator.Direction.DESC);
         if("sequence".equals(sortOrder)) 
         {
             comp = new SequenceComparator<DocumentNodeResource>();
         }
         else if("title".equals(sortOrder)) 
         {
             comp = new TitleComparator<DocumentNodeResource>(StringUtils.getLocale(periodical.getLocale()));
         }
         else if("name".equals(sortOrder)) 
         {
             comp = new NameComparator<DocumentNodeResource>(StringUtils.getLocale(periodical.getLocale()));
         }       
         else if("creation.time".equals(sortOrder)) 
         {
             comp = new CreationTimeComparator<DocumentNodeResource>();
         }
         else if("modification.time".equals(sortOrder)) 
         {
             comp = new ModificationTimeComparator<DocumentNodeResource>();
         }        
         else if("validity.start".equals(sortOrder)) 
         {
             comp = new ValidityStartComparator<DocumentNodeResource>(sortDirectionAsc ? TimeComparator.Direction.ASC
                 : TimeComparator.Direction.DESC);
         }
         else if("event.start".equals(sortOrder)) 
         {
             comp = new EventStartComparator(sortDirectionAsc ? TimeComparator.Direction.ASC
                 : TimeComparator.Direction.DESC);
         }
         else if("event.end".equals(sortOrder)) 
         {
             comp = new EventEndComparator(sortDirectionAsc ? TimeComparator.Direction.ASC
                 : TimeComparator.Direction.DESC);
         }        
         else if("priority".equals(sortOrder)) 
         {
             comp = new PriorityComparator<DocumentNodeResource>();
         }
         if(!sortDirectionAsc) {
             comp = Collections.reverseOrder(comp);
         }
         return comp;
     }
     
     private String timestamp(Date time)
     {
         Calendar cal = new GregorianCalendar();
         cal.setTime(time);
         NumberFormat format = new DecimalFormat("00");
         FieldPosition  field = new FieldPosition(NumberFormat.Field.INTEGER);
         StringBuffer buff = new StringBuffer();
         buff.append(cal.get(Calendar.YEAR));
         format.format((long)cal.get(Calendar.MONTH)+1, buff, field);
         format.format((long)cal.get(Calendar.DAY_OF_MONTH), buff, field);
         buff.append('.');
         format.format((long)cal.get(Calendar.HOUR_OF_DAY), buff, field);
         format.format((long)cal.get(Calendar.MINUTE), buff, field);
         format.format((long)cal.get(Calendar.SECOND), buff, field);
         return buff.toString();
     }
     
     private void send(CoralSession coralSession, EmailPeriodicalResource r, FileResource file, Date time, String recipient)
         throws PeriodicalsException
     {
         LedgeMessage message = null;
         if(file.getMimetype().equals("message/rfc822"))
         {
             message = loadMessage(file);
         }
         else
         {
             log.warn("non-notification renderer is used for generating notification, or deprecated 'full content' flag is enabled for " + r.getPath());
             message = prepareMessage(file, r, time);
         }
         if(message != null)
         {
             try
             {
                 message.getMessage().setFrom(new InternetAddress(r.getFromHeader()));
             }
             catch(Exception e)
             {
                 throw new PeriodicalsException("failed to set message headers", e);
             }
         }
         if(message != null && r.isReplyToHeaderDefined())
         {
        	try
             {
                 message.getMessage().setReplyTo(
                     new InternetAddress[] { new InternetAddress(r.getReplyToHeader()) });
             }
             catch(Exception e)
             {
                 throw new PeriodicalsException("failed to set message headers", e);
             }
         }
         String subsLink;
         try
         {
             NavigationNodeResource subscriptions = getEmailPeriodicalsRoot(coralSession,
                 r.getSite()).getSubscriptionNode();
             if(subscriptions != null)
             {
                 subsLink = linkRenderer.getNodeURL(coralSession, subscriptions);
             }
             else
             {
                 subsLink = "UNCONFIGURED";
             }
         }
         catch(Exception e)
         {
             throw new PeriodicalsException("failed to determine subsciption node", e);
         }
         // append sender to the end of list, for confirmation
         String recipients = r.getAddresses() + " " + r.getFromHeader();
         // applay override
         recipients = recipient != null ? recipient : recipients;
         StringTokenizer st = new StringTokenizer(recipients);
         Exception sendException = null;
         while(st.hasMoreTokens())
         {
             String rcpt = st.nextToken();
             try
             {
                 LedgeMessage customized = customizeMessage(message.getMessage(), r, subsLink, rcpt);
                 customized.send(true);        
             }
             catch(Exception e)
             {
                 log.error("failed to send message to " + rcpt, e);
                 sendException = e;
             }
         }
         if(sendException != null)
         {
             throw new PeriodicalsException("sedning a message failed, "
                 + "more similar errors may occur in the log", sendException);
         }
     }
     
     private LedgeMessage customizeMessage(Message orig, EmailPeriodicalResource periodical, String subsLink, String recipient)
         throws MessagingException, IOException, PeriodicalsException
     {
         LedgeMessage newMessage = mailSystem.newMessage();
         Message customized = newMessage.getMessage();
         @SuppressWarnings("unchecked")
         Enumeration<Header> headers = orig.getNonMatchingHeaders(new String[] { "Message-Id" });
         while(headers.hasMoreElements())
         {
             Header h = headers.nextElement();
             customized.addHeader(h.getName(), h.getValue());
         }
         customized.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
         if(orig.isMimeType("text/*"))
         {
             String content = (String)orig.getContent();
             String unsubToken = subscriptionService.createUnsubscriptionToken(periodical.getId(),
                 recipient);
             content = content.replaceAll("@@UNSUBSCRIBE_NODE@@", subsLink);
             content = content.replaceAll("@@UNSUBSCRIBE@@", "?unsub=" + unsubToken);
             content = content.replaceAll("@@UNSUBSCRIBE_ALL@@", "?unsub_all=" + unsubToken);
             customized.setContent(content, orig.getContentType());
         }
         else
         {
             throw new MessagingException("can't handle "+orig.getContentType());
         }
         newMessage.setMessage(customized); // skip prepare() in send()
         return newMessage;
     }
 
     private LedgeMessage loadMessage(FileResource file)
     {
         LedgeMessage message = null;
         try
         {
             if(!fileSystem.exists(cmsFilesService.getPath(file)))
             {
                 log.error("is missing from disk "+file.getPath());
             }
             else
             {
                 message = mailSystem.newMessage(new MimeMessage(mailSystem.getSession(),
                     cmsFilesService.getInputStream(file)));
             }
         }
         catch(MessagingException e)
         {
             log.error("malformed message "+file.getPath(), e);
         }
         return message;
     }
 
     private LedgeMessage prepareMessage(FileResource file, EmailPeriodicalResource r, Date time)
     {
         String contents = null;
         try
         {
             contents = fileSystem.read(cmsFilesService.getPath(file), file.getEncoding());
         }
         catch(IOException e)
         {
             log.error("failed to read "+file.getPath(), e);
         }
         String media = "PLAIN";
         if(file.getMimetype().startsWith("text/"))
         {
             media = file.getMimetype().substring(5);
             if(media.indexOf(';') > 0)
             {
                 media = media.substring(0, media.indexOf(';'));
             }
             media = media.toUpperCase();
         }
         LedgeMessage message = null;
         if(contents != null)
         {
             try
             {
                 message = mailSystem.newMessage();                    
                 message.setText(contents, media);
                 message.setEncoding(file.getEncoding());
                 String subject = r.getSubject();
                 if(subject == null || subject.length() == 0)
                 {
                     subject = r.getName();
                 }
                 subject = MimeUtility.encodeText(subject, file.getEncoding(), null);
                 message.getMessage().setSubject(subject);
                 message.getMessage().setSentDate(time);
                 message.prepare();
             }
             catch(Exception e)
             {
                 log.error("failed to create message", e);
                 message = null;
             }
         }
         return message;
     }
     
     // factory //////////////////////////////////////////////////////////////
     
     public PeriodicalRenderer getRenderer(String name)
     {
         PeriodicalRendererFactory factory =
             (PeriodicalRendererFactory)rendererFactories.get(name);
         if(factory == null)
         {
             return null;
         }
         return factory.getRenderer(this);
     }
     
     public void releaseRenderer(PeriodicalRenderer renderer)
     {
         // do nothing
     }
     
     public LinkRenderer getLinkRenderer()
     {
         return linkRenderer;
     }
 }
