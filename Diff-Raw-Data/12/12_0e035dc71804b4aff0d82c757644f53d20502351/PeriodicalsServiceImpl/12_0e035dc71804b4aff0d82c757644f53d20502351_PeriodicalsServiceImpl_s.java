 package net.cyklotron.cms.periodicals.internal;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.jcontainer.dna.Configuration;
 import org.jcontainer.dna.ConfigurationException;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.i18n.I18n;
 import org.objectledge.mail.LedgeMessage;
 import org.objectledge.mail.MailSystem;
 
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.documents.LinkRenderer;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesException;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.periodicals.EmailPeriodicalResource;
 import net.cyklotron.cms.periodicals.EmailPeriodicalsRootResource;
 import net.cyklotron.cms.periodicals.EmailPeriodicalsRootResourceImpl;
 import net.cyklotron.cms.periodicals.PeriodicalRenderer;
 import net.cyklotron.cms.periodicals.PeriodicalRendererFactory;
 import net.cyklotron.cms.periodicals.PeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalsException;
 import net.cyklotron.cms.periodicals.PeriodicalsNodeResource;
 import net.cyklotron.cms.periodicals.PeriodicalsNodeResourceImpl;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.periodicals.PublicationTimeResource;
 import net.cyklotron.cms.periodicals.SubscriptionRequestResource;
 import net.cyklotron.cms.periodicals.SubscriptionRequestResourceImpl;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 
 /**
  * A generic implementation of the periodicals service.
  * 
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: PeriodicalsServiceImpl.java,v 1.26 2006-05-08 09:47:57 rafal Exp $
  */
 public class PeriodicalsServiceImpl 
     implements PeriodicalsService
 {
     // constants ////////////////////////////////////////////////////////////
     
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
     private Map rendererFactories = new HashMap();
 
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
 
     /** pseudo-random number generator */
     private Random random;
 
     /** i18n */
     private I18n i18n;
     
     private final LinkRenderer linkRenderer;
     
     public PeriodicalsServiceImpl(Configuration config, Logger logger,
         CategoryQueryService categoryQueryService, FilesService cmsFilesService,
         FileSystem fileSystem, MailSystem mailSystem, I18n i18n, SiteService siteService,
         PeriodicalRendererFactory[] renderers)
         throws ConfigurationException
     {
         this.categoryQueryService = categoryQueryService;
         this.cmsFilesService = cmsFilesService;
         this.fileSystem = fileSystem;
         this.mailSystem = mailSystem;
         this.siteService = siteService;
         this.log = logger;
         this.i18n = i18n;
         for (int i = 0; i < renderers.length; i++)
         {
             rendererFactories.put(renderers[i].getRendererName(), renderers[i]);
         }
         serverName = config.getChild(SERVER_NAME_KEY).getValue(); // no default
         port = config.getChild(PORT_KEY).getValueAsInteger(PORT_DEFAULT);
         context = config.getChild(CONTEXT_KEY).getValue(CONTEXT_DEFAULT);
         servletAndApp = config.getChild(SERVLET_AND_APP_KEY).
             getValue(SERVLET_AND_APP_DEFAULT);
         messagesFrom = config.getChild(MESSAGES_FROM_KEY).
             getValue("noreply@"+serverName);
         random = new Random();
         
         linkRenderer = new PeriodicalsLinkRenderer(serverName, port, context, servletAndApp,
             siteService, log);
     }
 
     /**
      * List the periodicals existing in the site.
      * 
      * @param site the site.
      * @return array of periodicals.
      */
     public PeriodicalResource[] getPeriodicals(CoralSession coralSession,SiteResource site) throws PeriodicalsException
     {
         PeriodicalsNodeResource periodicalsRoot = getPeriodicalsRoot(coralSession,site);
         Resource[] resources = coralSession.getStore().getResource(periodicalsRoot);
         PeriodicalResource[] periodicals = new PeriodicalResource[resources.length];
         System.arraycopy(resources, 0, periodicals, 0, resources.length);
         return periodicals;
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
         PeriodicalsNodeResource periodicalsRoot = getEmailPeriodicalsRoot(coralSession,site);
         Resource[] resources = coralSession.getStore().getResource(periodicalsRoot);
         EmailPeriodicalResource[] periodicals = new EmailPeriodicalResource[resources.length];
         System.arraycopy(resources, 0, periodicals, 0, resources.length);
         return periodicals;
     }
 
     /**
      * Get root node of application's data.
      * 
      * @param coralSession CoralSession.
      * @param site the site.
      * @return root node of application data.
      * @throws PeriodicalsException
      */
     public PeriodicalsNodeResource getApplicationRoot(CoralSession coralSession, SiteResource site) throws PeriodicalsException
     {
         Resource[] apps = coralSession.getStore().getResource(site, "applications");
         if (apps.length == 0)
         {
             throw new PeriodicalsException("failed to lookup applications node in site " + site.getName());
         }
         Resource[] res = coralSession.getStore().getResource(apps[0], "periodicals");
         if(res.length == 0)
         {
             try
             {
                 return PeriodicalsNodeResourceImpl.createPeriodicalsNodeResource(coralSession,
                     "periodicals", apps[0]);
             }
             catch(InvalidResourceNameException e)
             {
                 throw new RuntimeException("unexpected exception", e);
             }        
         }
         else
         {
             return (PeriodicalsNodeResource)res[0];
         }
     }
     
 	/**
 	 * Return the root node for periodicals
 	 * 
 	 * @param site the site.
 	 * @return the periodicals root.
 	 */
     public PeriodicalsNodeResource getPeriodicalsRoot(CoralSession coralSession, SiteResource site) throws PeriodicalsException
     {
         PeriodicalsNodeResource applicationRoot = getApplicationRoot(coralSession,site);
         Resource[] res = coralSession.getStore().getResource(applicationRoot, "periodicals");
         if (res.length == 0)
         {
             try
             {
                 return PeriodicalsNodeResourceImpl.createPeriodicalsNodeResource(coralSession,
                     "periodicals", applicationRoot);
             }
             catch(InvalidResourceNameException e)
             {
                 throw new RuntimeException("unexpected exception", e);
             }
         }
         else
         {
             return (PeriodicalsNodeResource)res[0];
         }
     }
 
 	/**
 	 * Return the root node for email periodicals
 	 * 
 	 * @param site the site.
 	 * @return the periodicals root.
 	 */
     public EmailPeriodicalsRootResource getEmailPeriodicalsRoot(CoralSession coralSession,SiteResource site)
         throws PeriodicalsException
     {
         PeriodicalsNodeResource applicationRoot = getApplicationRoot(coralSession,site);
         Resource[] res = coralSession.getStore().getResource(applicationRoot, "email_periodicals");
         if (res.length == 0)
         {
             try
             {
                 return EmailPeriodicalsRootResourceImpl.createEmailPeriodicalsRootResource(
                     coralSession, "email_periodicals", applicationRoot);
             }
             catch(InvalidResourceNameException e)
             {
                 throw new RuntimeException("unexpected exception", e);
             }
         }
         else
         {
             return (EmailPeriodicalsRootResource)res[0];
         }
     }
 
     /**
      * Return the root node for email periodicals
      * 
      * @param site the site.
      * @return the periodicals root.
      */
     public PeriodicalsNodeResource getSubscriptionChangeRequestsRoot(CoralSession coralSession, SiteResource site) throws PeriodicalsException
     {
         PeriodicalsNodeResource applicationRoot = getApplicationRoot(coralSession,site);
         Resource[] res = coralSession.getStore().getResource(applicationRoot, "requests");
         if (res.length == 0)
         {
             try
             {
                 return PeriodicalsNodeResourceImpl.createPeriodicalsNodeResource(coralSession,
                     "requests", applicationRoot);
             }
             catch(InvalidResourceNameException e)
             {
                 throw new RuntimeException("unexpected exception", e);
             }
         }
         else
         {
             return (PeriodicalsNodeResource)res[0];
         }
     }
 
 
     // mail from address ////////////////////////////////////////////////////
     
     // inherit doc
     public String getFromAddress()
     {
         return messagesFrom;
     }
 
     // inherit doc    
     public synchronized SubscriptionRequestResource getSubscriptionRequest(CoralSession coralSession, String cookie)
         throws PeriodicalsException
     {
         Resource[] res = coralSession.getStore().getResourceByPath(
             "/cms/sites/*/applications/periodicals/requests/"+cookie);
         if(res.length > 0)
         {
             return (SubscriptionRequestResourceImpl)res[0];
         }
         else
         {
             return null;
         }
     }
     
     // interit doc
     public synchronized String createSubsriptionRequest(CoralSession coralSession, SiteResource site, String email, String items)
         throws PeriodicalsException
     {
         Resource root = getSubscriptionChangeRequestsRoot(coralSession,site);
         String cookie;
         Resource[] res;
         do
         {
             cookie = getRandomCookie();
             res = coralSession.getStore().getResource(root, cookie);
         }
         while(res.length > 0);
         try
         {
             SubscriptionRequestResource request = SubscriptionRequestResourceImpl.
                 createSubscriptionRequestResource(coralSession, cookie, root, email);
             request.setItems(items);
             request.update();
         }
         catch(Exception e)
         {
             throw new PeriodicalsException("failed to create subscription request record", e);
         }
         return cookie;
     }
 
     protected String getRandomCookie()
     {
         String cookie = "0000000000000000".concat(Long.toString(random.nextLong(), 16));
         return cookie.substring(cookie.length()-16, cookie.length());
     }
 
     // inherit doc
     public synchronized void discardSubscriptionRequest(CoralSession coralSession, String cookie)
         throws PeriodicalsException
     {
         SubscriptionRequestResource r = getSubscriptionRequest(coralSession,cookie);
         if(r != null)
         {
             try
             {
                 coralSession.getStore().deleteResource(r);
             }
             catch(EntityInUseException e)
             {
                 throw new PeriodicalsException("failed to delete subscription change request", e);
             }
         }
         }
 
     public EmailPeriodicalResource[] getSubscribedEmailPeriodicals(CoralSession coralSession, SiteResource site, String email)
         throws PeriodicalsException
     {
         EmailPeriodicalResource[] periodicals = getEmailPeriodicals(coralSession,site);
         List temp = new ArrayList();
         for (int i = 0; i < periodicals.length; i++)
         {
             EmailPeriodicalResource periodical = periodicals[i];
             if(periodical.getAddresses().indexOf(email) >= 0)
             {
                 temp.add(periodical);
             }
         }
         EmailPeriodicalResource[] result = new EmailPeriodicalResource[temp.size()];
         temp.toArray(result);
         return result;
     }
 
      // renderers & templates ///////////////////////////////////////////////
     
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
         List<FileResource> results = generate(coralSession,periodical, time, update);
         if(!results.isEmpty() && periodical instanceof EmailPeriodicalResource && send)
         {
             FileResource last = results.get(results.size() - 1);
             send(coralSession,(EmailPeriodicalResource)periodical, last, time, recipient);
         }
         return results;
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
         Set toProcess = findPeriodicalsToProcess(coralSession,time);
         Iterator i = toProcess.iterator();
         while(i.hasNext() && !Thread.interrupted())
         {
             PeriodicalResource p = (PeriodicalResource)i.next();
             List<FileResource> results = generate(coralSession,p, time, true);
             if(!results.isEmpty() && p instanceof EmailPeriodicalResource)
             {
                 FileResource last = results.get(results.size() - 1);                
                 send(coralSession,(EmailPeriodicalResource)p, last, time, null);
             }
         }
     }
     
     // implementation ///////////////////////////////////////////////////////
     
     private Set findPeriodicalsToProcess(CoralSession coralSession,Date time)
     {
         Set set = new HashSet();
         try
         {
             QueryResults results = coralSession.getQuery().executeQuery(
                 "FIND RESOURCE FROM cms.periodicals.periodical");
             Iterator rows = results.iterator();
             while(rows.hasNext())
             {
                 QueryResults.Row row = (QueryResults.Row)rows.next();
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
         Calendar timeCal = new GregorianCalendar();
         timeCal.setTime(time);
         Calendar lastCal = new GregorianCalendar();
         Date lastPublished = r.getLastPublished();
         if(lastPublished == null)
         {
             lastPublished = new Date(1L);    
         }
         lastCal.setTime(lastPublished);
         PublicationTimeResource[] publicationTimes = r.getPublicationTimes(coralSession);
         for(int i = 0; i < publicationTimes.length; i++)
         {
             PublicationTimeResource pt = publicationTimes[i];
             boolean publish = lastCal.getTimeInMillis() <= getLimitTime(pt.getDayOfMonth(-1), pt.getHour(-1), pt.getDayOfWeek(-1),time);
             if(publish)
             {
                 return true;
             }
         }
         return false;
     }
     
     public static long getLimitTime(int day, int hour, int weekDay, Date date)
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
      */
     private List<FileResource> generate(CoralSession coralSession, PeriodicalResource r,
         Date time, boolean update)
     {
         List<FileResource> results = new LinkedList<FileResource>();
         String timestamp = timestamp(time);
        FileResource contentFile = generate(coralSession, r, r.getRenderer(), time,
            r.getTemplate(), timestamp, null);
         if(contentFile != null)
         {
             results.add(contentFile);
             if(r instanceof EmailPeriodicalResource)
             {
                 EmailPeriodicalResource er = (EmailPeriodicalResource)r;
                 if(!er.getFullContent())
                 {
                    contentFile = generate(coralSession, r, er.getNotificationRenderer(), time, er
                        .getNotificationTemplate(), timestamp, contentFile);
                     if(contentFile != null)
                     {
                         results.add(contentFile);
                     }
                     else
                     {
                         // primary renderer succeded but secondary failed - we should not send anything
                         results.clear();
                     }
                 }
             }
         }
         if(!results.isEmpty() && update)
         {
             r.setLastPublished(time);
             r.update();            
         }
         return results;
     }
     
     private FileResource generate(CoralSession coralSession, PeriodicalResource r,
         String rendererName, Date time, String timestamp, String templateName, FileResource contentFile)
     {
         PeriodicalRenderer renderer = getRenderer(rendererName);
         if(renderer == null)
         {
             log.error("cannot generate "+r.getPath()+" because renderer "+rendererName+
                 " is not installed");
             return null;
         }
         String fileName = timestamp + "." + renderer.getFilenameSuffix();
         FileResource file;
         try
         {
             file = (FileResource)r.getStorePlace().getChild(coralSession, fileName);
         }
         catch(EntityDoesNotExistException e)
         {
             try
             {
                  file = cmsFilesService.createFile(coralSession, fileName, null, renderer.getMimeType(),
                     r.getEncoding(), r.getStorePlace());
             }
             catch (FilesException ee)
             {
                 log.error("failed to create file for periodical "+r.getPath(), ee);
                 return null;
             }                
         }
         catch(AmbigousEntityNameException e)
         {
             log.error("inconsistend data in cms files application", e);
             return null;
         }
         boolean success = renderer.render(coralSession, r, time, templateName, file, contentFile);
         releaseRenderer(renderer);
         return success ? file : null;
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
                 message.getMessage().setRecipient(Message.RecipientType.TO,
                     new InternetAddress(r.getFromHeader()));
                 StringTokenizer st = new StringTokenizer(recipient != null ? recipient : r.getAddresses());
                 while(st.hasMoreTokens())
                 {
                     message.getMessage().addRecipient(Message.RecipientType.BCC,
                         new InternetAddress(st.nextToken()));
                 }
             }
             catch(Exception e)
             {
                 log.error("failed to set message headers", e);
                 message = null;
             }
         }
         if(message != null)
         {
             try
             {
                 message.send(true);        
             }
             catch(Exception e)
             {
                 throw new PeriodicalsException("failed to send message", e);
             }
         }
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
