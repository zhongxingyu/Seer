 package net.cyklotron.cms.periodicals.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.documents.LinkRenderer;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesException;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.files.RootDirectoryResource;
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
 import net.cyklotron.cms.structure.NavigationNodeResource;
 
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
 import org.objectledge.encodings.HTMLEntityEncoder;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.i18n.I18n;
 import org.objectledge.mail.LedgeMessage;
 import org.objectledge.mail.MailSystem;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.TemplateNotFoundException;
 import org.objectledge.templating.Templating;
 import org.objectledge.utils.StringUtils;
 
 /**
  * A generic implementation of the periodicals service.
  * 
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: PeriodicalsServiceImpl.java,v 1.12 2005-06-13 11:08:12 rafal Exp $
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
     
     /** mail service */
     private MailSystem mailSystem;
     
     /** file service. */
     private FileSystem fileSystem;
     
     /** templating service. */
     private Templating templating;
     
     /** site service. */
     private SiteService siteService;
     
     /** log */
     private Logger log;
     
     /** renderer classes */
     private Map rendererFactories = new HashMap();
 
     /** template encoding in the FS */
     private String templateEncoding;
 
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
     
     public PeriodicalsServiceImpl(Configuration config, Logger logger, 
         CategoryQueryService categoryQueryService, FilesService cmsFilesService, 
         MailSystem mailSystem, FileSystem fileSystem, I18n i18n,
         Templating templating, SiteService siteService, PeriodicalRendererFactory[] renderers)
         throws ConfigurationException
     {
         this.categoryQueryService = categoryQueryService;
         this.cmsFilesService = cmsFilesService;
         this.mailSystem = mailSystem;
         this.fileSystem = fileSystem;
         this.templating = templating;
         this.templateEncoding = templating.getTemplateEncoding();
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
     public void publishNow(CoralSession coralSession,PeriodicalResource periodical)
         throws PeriodicalsException
     {
         Date time = new Date();
         String fileName = generate(coralSession,periodical, time);
         if(fileName != null && periodical instanceof EmailPeriodicalResource)
         {
             send(coralSession,(EmailPeriodicalResource)periodical, fileName, time);
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
         Set toProcess = findPeriodicalsToProcess(coralSession,time);
         Iterator i = toProcess.iterator();
         while(i.hasNext() && !Thread.interrupted())
         {
             PeriodicalResource p = (PeriodicalResource)i.next();
             String fileName = generate(coralSession,p, time);
             if(fileName != null && p instanceof EmailPeriodicalResource)
             {
                 send(coralSession,(EmailPeriodicalResource)p, fileName, time);
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
        lastCal.setTime(r.getLastPublished());
         PublicationTimeResource[] publicationTimes = r.getPublicationTimes(coralSession);
         for(int i = 0; i < publicationTimes.length; i++)
         {
             PublicationTimeResource pt = publicationTimes[i];
             if(timeCal.get(Calendar.HOUR_OF_DAY) == pt.getHour())
             {
                 if((pt.getDayOfMonth() == -1) && (pt.getDayOfWeek() == timeCal.get(Calendar.DAY_OF_WEEK)) ||
                    (pt.getDayOfWeek() == -1) && (pt.getDayOfMonth() == timeCal.get(Calendar.DAY_OF_MONTH)))
                 {   
                     if((timeCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR)) &&
                        (timeCal.get(Calendar.DAY_OF_YEAR) == lastCal.get(Calendar.DAY_OF_YEAR)))
                     {
                         // already published today
                         return false;
                     }
                     // it's the time, publish now
                     return true;
                 }
             }
             else
             {
                 if(pt.getDayOfMonth() == -1)
                 {
                     // 1 week + 1 hour passed since last publish
                     if(timeCal.getTimeInMillis() - lastCal.getTimeInMillis() > (7*24+1)*60*60*1000)
                     {
                         // we've missed the time, publish now
                         return true;
                     }
                 }
                 else
                 {
                     // we haven't published this month, but the day and hour say we should
                     if(((lastCal.get(Calendar.MONTH) < timeCal.get(Calendar.MONTH)) ||
                         (lastCal.get(Calendar.YEAR) < timeCal.get(Calendar.YEAR))) &&
                        (lastCal.get(Calendar.DAY_OF_MONTH) >= timeCal.get(Calendar.DAY_OF_MONTH)) &&
                        (lastCal.get(Calendar.HOUR_OF_DAY) >= timeCal.get(Calendar.HOUR_OF_DAY)))
                     {
                         // we've missed the time, publish now
                         return true;
                     }
                 }
             }
         }
         return false;
     }
     
     /**
      * Generates periodical's contents using a renderer.
      * 
      * @param r the periodical.
      * @param time the generation time.
      * @return returns the name of the generated file, or null on failure.
      */
     private String generate(CoralSession coralSession, PeriodicalResource r, Date time)
     {
         PeriodicalRenderer renderer = getRenderer(r.getRenderer());
         if(renderer == null)
         {
             log.error("cannot generate "+r.getPath()+"because renderer "+r.getRenderer()+
                 " is not installed");
             return null;
         }
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
         buff.append('.');
         buff.append(renderer.getFilenameSuffix());
         String fileName = buff.toString();
         FileResource file;
         
         try
         {
             file = (FileResource)r.getStorePlace().getChild(coralSession, fileName);
         }
         catch(EntityDoesNotExistException e)
         {
             try
             {
                  file = cmsFilesService.createFile(coralSession,buff.toString(), null, renderer.getMimeType(),
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
         boolean success = renderer.render(coralSession, r, time, file);
         if(success && r instanceof EmailPeriodicalResource && !((EmailPeriodicalResource)r).getFullContent())
         {
             EmailPeriodicalResource er = (EmailPeriodicalResource)r;
             PeriodicalRenderer notificationRenderer = getRenderer(er.getNotificationRenderer());
             success = notificationRenderer.render(coralSession, r, time, file);
             releaseRenderer(notificationRenderer);
             fileName = fileName+"-notification";
         }
         releaseRenderer(renderer);
         if(success)
         {
             r.setLastPublished(time);
             r.update();
             return fileName;
         }
         else
         {
             return null;
         }
     }
     
     private void send(CoralSession coralSession, EmailPeriodicalResource r, String fileName, Date time)
         throws PeriodicalsException
     {
         String path = r.getStorePlace().getPath()+"/"+fileName; // for error reporting
         FileResource file;
         try
         {
             file = (FileResource)r.getStorePlace().getChild(coralSession, fileName);
         }
         catch (Exception e)
         {
             log.error(path+" does not exist", e);
             return;
         }
         InputStream is = cmsFilesService.getInputStream(file);
         if(is == null)
         {
             log.error("failed to open "+path);
             return;
         }
         LedgeMessage message = mailSystem.newMessage();
         if(fileName.endsWith(".eml"))
         {
             Message msg;
             try
             {
                 msg = new MimeMessage(mailSystem.getSession(), is);
             }
             catch(MessagingException e)
             {
                 log.error("malformed message "+path, e);
                 return;
             }
             message.setMessage(msg);
         }
         else
         {
             String contents;
             try
             {
                 StringWriter sw = new StringWriter();
                 InputStreamReader osr = new InputStreamReader(is, file.getEncoding());
                 char[] buff = new char[4096];
                 int count = 0;
                 while(count >= 0)
                 {
                     count = osr.read(buff, 0, buff.length);
                     if(count > 0)
                     {
                         sw.write(buff, 0, count);
                     }
                 }
                 sw.flush();
                 contents = sw.toString(); 
             }
             catch(IOException e)
             {
                 log.error("failed to read "+path, e);
                 return;
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
             try
             {
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
                 return;
             }
         }
         try
         {
             message.getMessage().setFrom(new InternetAddress(r.getFromHeader()));
             message.getMessage().setRecipient(Message.RecipientType.TO, new InternetAddress(r.getFromHeader()));
             StringTokenizer st = new StringTokenizer(r.getAddresses());
             while(st.hasMoreTokens())
             {
                 message.getMessage().addRecipient(Message.RecipientType.BCC, new InternetAddress(st.nextToken()));
             }
         }
         catch(Exception e)
         {
             log.error("failed to set message headers", e);
             return;
         }
         try
         {
             message.send(true);        
         }
         catch(Exception e)
         {
             throw new PeriodicalsException("failed to send message", e);
         }
     }
 
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
     
     // template variants ////////////////////////////////////////////////////
     
     // inherit doc
     public String[] getTemplateVariants(SiteResource site, String renderer)
         throws PeriodicalsException
     {
         try
         {
             String dir = "/templates/cms/sites/"+site.getName()+
                 "/messages/periodicals/"+renderer;
             if(!fileSystem.exists(dir))
             {
                 return new String[0];
             }
             String[] items = fileSystem.list(dir);
             if(items == null || items.length == 0)
             {
                 return new String[0];
             }
             ArrayList temp = new ArrayList();
             for (int i = 0; i < items.length; i++)
             {
                 String path = dir+"/"+items[i];
                 if(fileSystem.isFile(path) && path.endsWith(".vt"))
                 {
                     temp.add(items[i].substring(0, items[i].length()-3));
                 }
             }
             String[] result = new String[temp.size()];
             temp.toArray(result);
             return result;
         }
         catch(Exception e)
         {
             throw new PeriodicalsException("exception occured", e);
         }
     }
     
     // inherit doc
     public boolean hasTemplateVariant(SiteResource site, String renderer, String name)
     {
         String path = getTemplateVariantPath(site, renderer, name);
         return fileSystem.exists(path);
     }
 
     // inherit doc
     public Template getTemplateVariant(SiteResource site, String renderer, String name)
         throws TemplateNotFoundException
     {
         String path = "/sites/"+site.getName()+"/messages/periodicals/"+renderer+"/"+name;
         return templating.getTemplate(path);
     }
     
     // inherit doc
     public void createTemplateVariant(SiteResource site, String renderer, String name, String contents)
         throws ProcessingException
     {
         String path = getTemplateVariantPath(site, renderer, name);
         if(fileSystem.exists(path))
         {
             throw new ProcessingException("variant "+name+" of "+renderer+
                 " render already exists in site "+site.getName());
         }
         
         writeTemplate(path, contents, "failed to write template contents");
         invalidateTemplate(site, renderer, name);
     }
     
     // inherit doc
     public void deleteTemplateVariant(SiteResource site, String renderer, String name)
         throws ProcessingException
     {
         String path = getTemplateVariantPath(site, renderer, name);
         if(!fileSystem.exists(path))
         {
             throw new ProcessingException("variant "+name+" of "+renderer+
                 " render does not exist in site "+site.getName());
         }
         try
         {
             fileSystem.delete(path);
             invalidateTemplate(site, renderer, name);
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to delete template", e);
         }
     }
     
     public List getDefaultTemplateLocales(String renderer)
         throws ProcessingException
     {
         List list = new ArrayList();
         String suffix = "/messages/periodicals/"+renderer+"/default.vt";
         Locale[] supportedLocales = i18n.getSupportedLocales();
         for (int i = 0; i < supportedLocales.length; i++)
         {
             if(fileSystem.exists("/templates/cms/"+
                 supportedLocales[i].toString()+"_"+getMedium(renderer)+suffix))
             {
                 list.add(supportedLocales[i]);
             }
         }
         return list;
     }
     
     public String getDefaultTemplateContents(String renderer, Locale locale)
         throws ProcessingException
     {
         String path = "/templates/cms/"+locale.toString()+"_"+getMedium(renderer)+
             "/messages/periodicals/"+renderer+"/default.vt";
         try
         {
             return fileSystem.read(path, templateEncoding);            
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to read template contents for renderer "+
                 renderer+" "+locale.toString(), e);
         }
     }
 
     public Template getDefaultTemplate(String renderer, Locale locale)
         throws ProcessingException
     {
         String path = locale.toString()+"_"+getMedium(renderer)+
             "/messages/periodicals/"+renderer+"/default";
         try
         {
             return templating.getTemplate(path);
         }
         catch(TemplateNotFoundException e)
         {
             return null;
         }
     }
     
     protected String getMedium(String renderer)
     {
         PeriodicalRenderer r = getRenderer(renderer);
         String medium = r.getMedium();
         releaseRenderer(r);
         return medium;
     }
         
     // inherit doc
     public String getTemplateVariantContents(SiteResource site, String renderer, String name)
         throws ProcessingException
     {
         String path = getTemplateVariantPath(site, renderer, name);
         if(!fileSystem.exists(path))
         {
             throw new ProcessingException("variant "+name+" of "+renderer+
                 " render does not exist in site "+site.getName());
         }
         try
         {
             return fileSystem.read(path, templateEncoding);
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to read template contents", e);
         }
     }
     
     // inherit doc
     public void getTemplateVariantContents(SiteResource site, String renderer, String name, OutputStream out)
         throws ProcessingException
     {
         String path = getTemplateVariantPath(site, renderer, name);
         if(!fileSystem.exists(path))
         {
             throw new ProcessingException("variant "+name+" of "+renderer+
                 " render does not exist in site "+site.getName());
         }
         try
         {
             fileSystem.read(path,out);
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to read template contents", e);
         }
     }
     
     // inherit doc
     public long getTemplateVariantLength(SiteResource site, String renderer, String name)
         throws ProcessingException
     {
         String path = getTemplateVariantPath(site, renderer, name);
         if(!fileSystem.exists(path))
         {
             throw new ProcessingException("variant "+name+" of "+renderer+
                 " render does not exist in site "+site.getName());
         }
         return fileSystem.length(path);
     }
     
     // inherit doc
     public void setTemplateVariantContents(SiteResource site, String renderer, String name, String contents)
         throws ProcessingException
     {
         String path = getTemplateVariantPath(site, renderer, name);
         if(!fileSystem.exists(path))
         {
             throw new ProcessingException("variant "+name+" of "+renderer+
                 " render does not exist in site "+site.getName());
         }
         writeTemplate(path, contents, "failed to write template contents");
         invalidateTemplate(site, renderer, name);
     }
     
     // inherit doc
     protected String getTemplateVariantPath(SiteResource site, String renderer, String variant)
     {
         return "/templates/cms/sites/"+site.getName()+"/messages/periodicals/"+renderer+
             "/"+variant+".vt";    
     }
     
     protected void invalidateTemplate(SiteResource site, String renderer, String variant)
     {
         String name = "/sites/"+site.getName()+"/messages/periodicals/"+renderer+
             "/"+variant;
         templating.invalidateTemplate(name);
     }
     
     // lame link tool ///////////////////////////////////////////////////////
 
     public LinkRenderer getLinkRenderer()
     {
         return new LinkRenderer()
         {
             public String getFileURL(CoralSession coralSession, FileResource file)
             {
                 return PeriodicalsServiceImpl.this.getFileURL(coralSession, file);
             }
 
 			public String getCommonResourceURL(CoralSession coralSession, SiteResource site, String path)
 			{
 				return PeriodicalsServiceImpl.this.getCommonResourceURL(coralSession, site, path);
 			}
 
 			public String getAbsoluteURL(CoralSession coralSession, SiteResource site, String path)
 			{
                 return PeriodicalsServiceImpl.this.getAbsoluteURL(coralSession, site, path);
 			}
 
             public String getNodeURL(CoralSession coralSession, NavigationNodeResource node)
             {
                 return PeriodicalsServiceImpl.this.getNodeURL(coralSession, node);
             }
         };
     }
         
     public String getFileURL(CoralSession coralSession, FileResource file)
     {
         Resource parent = file.getParent();
         while(parent != null && !(parent instanceof RootDirectoryResource))
         {
             parent = parent.getParent();
         }
         if(parent == null)
         {
             throw new IllegalStateException("cannot determine root directory");
         }
         RootDirectoryResource rootDirectory = ((RootDirectoryResource)parent);
 
         while(parent != null && !(parent instanceof SiteResource))
         {
             parent = parent.getParent();
         }
         if(parent == null)
         {
             throw new IllegalStateException("cannot determine site");
         }
         SiteResource site = (SiteResource)parent;
         
         if(rootDirectory.getExternal())
         {
             String path = "";
             for(parent = file; parent != null; parent = parent.getParent())
             {
                 if(parent instanceof RootDirectoryResource)
                 {
                     break;
                 }
                 else
                 {
                     try
                     {
                         path = "/" + URLEncoder.encode(parent.getName(), "UTF-8") + path;
                     }
                     catch(UnsupportedEncodingException e)
                     {
                         throw new RuntimeException("really should not happen", e);
                     }
                 }
             }
             
             StringBuilder sb = new StringBuilder();
             sb.append(getContextURL(coralSession, site));
             sb.append("files/");
             sb.append(site.getName());
             sb.append("/");
             sb.append(rootDirectory.getName());
             sb.append(path);
             return sb.toString();
         }
         else
         {
             String path = "";
             for(parent = file; parent != null; parent = parent.getParent())
             {
                 if(parent instanceof RootDirectoryResource)
                 {
                     break;
                 }
                 else
                 {
                     path = ","+parent.getName()+path;
                 }
             }
             path = "/"+rootDirectory.getName()+path;
 
             StringBuilder sb = new StringBuilder();
             sb.append(getApplicationURL(coralSession, site));
             sb.append("view/files.Download?");
             sb.append("path=").append(path).append('&');
             sb.append("file_id=").append(file.getIdString());
             return sb.toString();
         }
     }
     
     public String getAbsoluteURL(CoralSession coralSession, SiteResource site, String path) 
     {
         return getContextURL(coralSession, site) + path;
     }
     
     public String getCommonResourceURL(CoralSession coralSession, SiteResource site, String path)
     {
         return getContextURL(coralSession, site) + "content/default/" + path;
     }
     
     public String getNodeURL(CoralSession coralSession, NavigationNodeResource node)
     {
         return getApplicationURL(coralSession, node.getSite())+"x/"+node.getIdString();
     }
 
     protected String getContextURL(CoralSession coralSession, SiteResource site)
     {
         StringBuilder buff = new StringBuilder();
         buff.append("http://")
             .append(getServer(coralSession, site));
         if(port != 80)
         {
             buff.append(':')
                 .append(port);
         }
         buff.append(context);
         return buff.toString();
     }
     
     protected String getApplicationURL(CoralSession coralSession, SiteResource site)
     {
         StringBuilder buff = new StringBuilder();
         buff.append("http://")
             .append(getServer(coralSession, site));
         if(port != 80)
         {
             buff.append(':')
                 .append(port);
         }
         buff.append(context)
             .append(servletAndApp);
         return buff.toString();
     }    
 
     protected String getServer(CoralSession coralSession, SiteResource site)
     {
         String server = null;
         try
         {
             server = siteService.getPrimaryMapping(coralSession, site);
         }
         catch(Exception e)
         {
             log.error("failed to deteremine site domain address", e);
         }
         if(server == null)
         {
             server = serverName;        
         }
         return server;
     }
     
     private void writeTemplate(String path, String contents, String message)
         throws ProcessingException
     {
         try
         {
             HTMLEntityEncoder encoder = new HTMLEntityEncoder();
             if(!fileSystem.exists(path))
             {
                 fileSystem.mkdirs(StringUtils.directoryPath(path));
             }
             fileSystem.write(path,
                 encoder.encodeHTML(contents, templateEncoding), templateEncoding);
         }
         catch(Exception e)
         {
             throw new ProcessingException(message, e);
         }
     }
 }
