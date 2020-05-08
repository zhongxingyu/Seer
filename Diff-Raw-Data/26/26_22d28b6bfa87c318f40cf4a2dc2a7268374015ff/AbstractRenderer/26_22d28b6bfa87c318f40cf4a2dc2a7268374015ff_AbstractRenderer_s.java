 /*
  * Created on Oct 24, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package net.cyklotron.cms.periodicals.internal;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeUtility;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
 import org.objectledge.html.DiscardImagesHTMLContentFilter;
 import org.objectledge.i18n.DateFormatTool;
 import org.objectledge.i18n.DateFormatter;
 import org.objectledge.mail.LedgeMessage;
 import org.objectledge.mail.MailSystem;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.MergingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.TemplateNotFoundException;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StringUtils;
 import org.objectledge.web.mvc.tools.StringTool;
 
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.periodicals.EmailPeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalRenderer;
 import net.cyklotron.cms.periodicals.PeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalsException;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.periodicals.PeriodicalsTemplatingService;
 import net.cyklotron.cms.site.SiteService;
 
 /**
  * An implementation of PeriodicalRenderer that uses the Templating service to render
  * the content.
  * 
  * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
  * @version $Id: AbstractRenderer.java,v 1.21 2007-11-18 21:23:25 rafal Exp $ 
  */
 public abstract class AbstractRenderer
     implements PeriodicalRenderer
 {
     // instance variables ///////////////////////////////////////////////////
    
     /** the logging facility. */
     protected Logger log;
     
     /** templating service. */
     protected Templating templating;
 
     /** category query service. */
     protected CategoryQueryService categoryQueryService;
     
     /** periodicals service. */
     protected PeriodicalsService periodicalsService;
 
     /** periodicals templating service. */
     protected PeriodicalsTemplatingService periodicalsTemplatingService;
     
     /** file service. */
     protected FilesService cmsFilesService;
     
     /** date formater */
     protected DateFormatter dateFormatter;
     
     protected IntegrationService integrationService;
     
     protected SiteService siteService;
 
     private final MailSystem mailSystem;
     
     /** 'everyone' system role. */
     //protected Role anonymous;
     
     /** document view permission. */
     //protected Permission viewPermission;
     
     // initialization ///////////////////////////////////////////////////////
     
     public AbstractRenderer(Logger log, Templating templating, MailSystem mailSystem,
         CategoryQueryService categoryQueryService, PeriodicalsService periodicalsService,
         PeriodicalsTemplatingService periodicalsTemplatingService,
         FilesService cmsFilesService, DateFormatter dateFormatter,
         IntegrationService integrationService, SiteService siteService)
     {
         this.log = log;
         this.templating = templating;
         this.mailSystem = mailSystem;
         this.categoryQueryService = categoryQueryService;
         this.periodicalsService = periodicalsService;
         this.periodicalsTemplatingService = periodicalsTemplatingService;
         this.cmsFilesService = cmsFilesService;
         this.dateFormatter = dateFormatter;
         this.siteService = siteService;
     }
     
     // inherited doc
     public abstract String getName();     
     
     public void render(CoralSession coralSession, PeriodicalResource periodical,
        Map<CategoryQueryResource, Resource> queryResults, Date time, String templateName,
         FileResource file, FileResource contentFile)
         throws ProcessingException, MergingException, TemplateNotFoundException,
         PeriodicalsException, IOException, MessagingException
     {
         TemplatingContext templatingContext = setupContext(coralSession, periodical, queryResults, time, file,
             contentFile);
         String result = renderTemplate(coralSession, periodical, time, templateName, file,
             contentFile, templatingContext);
         byte[] image = postProcess(coralSession, periodical, time, file, contentFile,
             result, templatingContext);
         OutputStream os = cmsFilesService.getOutputStream(file);
         os.write(image);
         os.flush();
     }
     
     // inherited doc
     public String getFilenameSuffix()
     {
         if(isNotification())
         {
             return "eml";
         }
         else
         {
             return getBodyFilenameSuffix();
         }
     }    
     
     // inherited doc
     public String getMimeType()
     {
         if(isNotification())
         {
             return "message/rfc822";
         }
         else
         {
             return getBodyMimeType();
         }
     }
 
     protected String renderTemplate(CoralSession coralSession, PeriodicalResource periodical,
         Date time, String templateName, FileResource file, FileResource contentFile,
         TemplatingContext templatingContext)
         throws ProcessingException, MergingException, TemplateNotFoundException,
         PeriodicalsException
     {
         try
         {
             Template template;
             if(templateName == null || templateName.equals(""))
             {
                 template = periodicalsTemplatingService.getDefaultTemplate(getName(), 
                     StringUtils.getLocale(periodical.getLocale()));
                 if(template == null)
                 {
                     throw new TemplateNotFoundException("failed to render " + periodical.getPath()
                         + " default template for " + getName()
                         + " renderer not available in locale " + periodical.getLocale());
                 }
                 log.warn("using default template for "+periodical.getPath()+
                     " because no template variant was selected");
             }
             else
             {
                 if(periodicalsTemplatingService.hasTemplateVariant(
                     periodical.getSite(), getName(), templateName))
                 {
                     template = periodicalsTemplatingService.getTemplateVariant(
                         periodical.getSite(), getName(), templateName);
                 }
                 else
                 {
                     throw new TemplateNotFoundException("failed to render " + periodical.getPath()
                         + " template variant " + templateName + " not defined for " + getName()
                         + " in site " + periodical.getSite().getName());
                 }
             }
             return template.merge(templatingContext);
         }
         finally
         {
             releaseContext(templatingContext);
         }
     }
     
     protected byte[] postProcess(CoralSession coralSession, PeriodicalResource periodical,
         Date time, FileResource file, FileResource contentFile, String result,
         TemplatingContext templatingContext)
         throws IOException, MessagingException, MergingException
     {
         if(isNotification())
         {
             LedgeMessage message = mailSystem.newMessage();                    
             message.setText(result, getBodyMimeType().replace("text/", ""));
             message.setEncoding(file.getEncoding());
             String subject = (String)templatingContext.get("subject"); 
             if(subject == null || subject.length() == 0)
             {
                 subject = ((EmailPeriodicalResource)periodical).getSubject();
                 if(subject == null || subject.length() == 0)
                 {
                     subject = periodical.getName();
                 }
             }
             message.getMessage().setSubject(
                 MimeUtility.encodeText(subject, file.getEncoding(), null));
             message.getMessage().setSentDate(time);
             return message.getMessageBytes();
         }
         else
         {
             return result.getBytes(file.getEncoding());
         }
     }
 
     /**
      * Prepares a templating context.
      * @param coralSession the coral session
      * @param periodical the periodical resource.
      * @param time publication time.
      * 
      * @return the context.
      */
     protected TemplatingContext setupContext(CoralSession coralSession,
        PeriodicalResource periodical, Map<CategoryQueryResource, Resource> queryResults,
         Date time, FileResource file, FileResource contentFile)
     {
         TemplatingContext context = templating.createContext();
         Locale locale = StringUtils.getLocale(periodical.getLocale());
         DateFormatTool dateFormat = new DateFormatTool(dateFormatter, locale, dateFormatter
             .getDateFormat(locale));
         context.put("format_date", dateFormat);
         context.put("renderer", this);        
         context.put("periodical", periodical);
         context.put("time", time);
         context.put("file", file);
         context.put("contentFile", contentFile);
         context.put("link", periodicalsService.getLinkRenderer());
         context.put("coralSession", coralSession);
         context.put("html_content_filter", new DiscardImagesHTMLContentFilter());
         context.put("string", new StringTool());
         context.put("queryResults", queryResults);
        List queries = periodical.getCategoryQuerySet().getQueries();
         context.put("queryList", queries);
         context.put("unsubscribeNode", "@@UNSUBSCRIBE_NODE@@");
         context.put("unsubscribe", "@@UNSUBSCRIBE@@");
         context.put("unsubscribeAll", "@@UNSUBSCRIBE_ALL@@");
         return context;
     }
     
     /**
      * Release context after use.
      * 
      * @param context the context.
      */
     protected void releaseContext(TemplatingContext context)
     {
         // context pooling not implemented
     }
 
     /**
      * Is this a notification (message/rfc822 producing) renderer?
      */
     protected abstract boolean isNotification();
     
     /**
      * Content-Type of the message body.
      */
     protected abstract String getBodyMimeType();
     
     /**
      * File name extension of the message body.
      */
     protected abstract String getBodyFilenameSuffix();
 }
