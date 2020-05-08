 /*
  * Created on Nov 7, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package net.cyklotron.cms.modules.actions.periodicals;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.mail.Message;
 import javax.mail.internet.InternetAddress;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.mail.LedgeMessage;
 import org.objectledge.mail.MailSystem;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * @author <a href="rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SendTicket.java,v 1.4 2005-05-16 11:59:10 zwierzem Exp $
  */
 public class SendTicket
     extends BasePeriodicalsAction
 {
     protected MailSystem mailService;
     
     public SendTicket(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PeriodicalsService periodicalsService,
         SiteService siteService, MailSystem mailSystem)
     {
         super(logger, structureService, cmsDataFactory, periodicalsService, siteService);
         this.mailService = mailSystem;
     }
     
         /**
      * {@inheritdoc}
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession) 
         throws ProcessingException
     {
         try
         {
             String email = parameters.get("email","");
             templatingContext.put("email", email);
             boolean subscribe = parameters.getBoolean("subscribe", false);
             templatingContext.put("subscribe", new Boolean(subscribe));
             String items = "";
             String[] selected = parameters.getStrings("selected");
             Set selectedSet = new HashSet();
             for (int i = 0; i < selected.length; i++)
             {
                 items += selected[i];
                 selectedSet.add(selected[i]);
                 if(i < selected.length - 1)
                 {
                     items += " ";
                 }
             }
             templatingContext.put("selected", selectedSet);
             if(parameters.getBoolean("subscribe",true) && items.length() == 0)
             {
                 templatingContext.put("result", "no_periodicals_selected");
                 return;
             }
             if(email.length() == 0)
             {
                 templatingContext.put("result", "address_missing");
                 return;
             }
             String cookie = periodicalsService.createSubsriptionRequest(coralSession, getSite(context), email, subscribe ? items : null);
             LedgeMessage message = mailService.newMessage();
             message.getContext().put("cookie", cookie);
             message.getContext().put("link", periodicalsService.getLinkRenderer());
             message.getContext().put("site", getSite(context));
             message.getContext().put("node", getNode(context));
             I18nContext i18nContext = I18nContext.getI18nContext(context);
            message.setTemplate(i18nContext.getLocale(), "PLAIN", "periodicals/Ticket");
             message.getMessage().setSentDate(new Date());
             message.getMessage().setFrom(new InternetAddress(periodicalsService.getFromAddress()));
             message.getMessage().setRecipient(Message.RecipientType.TO, new InternetAddress(email));
             message.send();
             templatingContext.put("result","ticket_sent");
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
         }
     }
     
     public boolean checkAccessRights(Context context) throws ProcessingException
     {
         return true;
     }  
     
     /**
      * @{inheritDoc}
      */
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         return false;
     }
   
 }
