 package net.cyklotron.cms.modules.actions.structure;
 
 import javax.mail.Message;
 import javax.mail.internet.InternetAddress;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.mail.LedgeMessage;
 import org.objectledge.mail.MailSystem;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  * Recommend the document
  *
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RecommendDocument.java,v 1.5 2005-05-16 10:44:25 pablo Exp $
  */
 
 public class RecommendDocument
     extends BaseStructureAction
 {
 	/** logging facility */
 	protected Logger proposalsLog;
 
 	/** mail service */
 	protected MailSystem mailService;
 
 	/** mail service */
 	protected Templating templating;
 
 
     public RecommendDocument(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService,
         MailSystem mailSystem, Templating templating)
     {
         super(logger, structureService, cmsDataFactory, styleService);
         this.mailService = mailSystem;
         this.templating = templating;
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         // basic setup
         
         Subject subject = coralSession.getUserSubject();
         DocumentNodeResource node = null;
         
         try
         {
         	// get parameters
             String firstName = parameters.get("first_name","");
             String secondName = parameters.get("second_name","");
 			String from = parameters.get("from","");
             String to = parameters.get("to","");
             String content = parameters.get("content","");
 			
 			// check required parameters
 			if(firstName.equals("") || secondName.equals("") || from.equals("") || to.equals(""))
 			{
 				templatingContext.put("result","field_empty");
 				parameters.remove("state");
 				return;
 			}
 			
 			if(!mailService.isValidEmailAddress(from))
 			{
 				templatingContext.put("result","illegal_from_param");
 				parameters.remove("state");
 				return;
 			}
 			if(!mailService.isValidEmailAddress(to))
 			{
 				templatingContext.put("result","illegal_to_param");
 				parameters.remove("state");
 				return;
 			}
 			
 			// find recommend node
             long nodeId = parameters.getLong("parent_node", -1);
             if(nodeId == -1)
             {
                 templatingContext.put("result","parent_not_found");
 				parameters.remove("state");
                 return;
             }
             
             NavigationNodeResource parent = NavigationNodeResourceImpl
                 .getNavigationNodeResource(coralSession,nodeId);
             
 			LedgeMessage message = mailService.newMessage();
 			TemplatingContext ctx = message.getContext();
 			
 			//toolService.populate(ctx,data);
 			ctx.put("content",content);
 			ctx.put("first_name", firstName);
 			ctx.put("second_name", secondName);
 			ctx.put("document",parent);
 			ctx.put("from",from);
 			ctx.put("to",to);
			ctx.put("context",context);
             I18nContext i18nContext = I18nContext.getI18nContext(context);
 			Template template = templating.getTemplate(
					"messages/PLAIN/documents/RecommendDocumentSubject."+
					i18nContext.getLocale().toString());
 			String title = template.merge(ctx);
 			message.getMessage().setSubject(title);
 			message.setEncoding(httpContext.getEncoding());
 			message.getMessage().setFrom(new InternetAddress(from));
 			message.getMessage().setRecipient(Message.RecipientType.TO, new InternetAddress(to));
 			message.setTemplate(i18nContext.getLocale(), "PLAIN", "documents/RecommendDocument");
 			message.send(true);
         }
         catch(Exception e)
         {
             templatingContext.put("result","exception");
             logger.error("StructureException: ",e);
             templatingContext.put("trace", new StackTrace(e));
 			parameters.remove("state");
             return;
         }
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
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
