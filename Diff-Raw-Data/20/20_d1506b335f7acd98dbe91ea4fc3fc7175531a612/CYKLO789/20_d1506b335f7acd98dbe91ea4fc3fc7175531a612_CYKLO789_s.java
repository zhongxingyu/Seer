 package net.cyklotron.cms.modules.actions.fixes;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.confirmation.ConfirmationRequestException;
 import net.cyklotron.cms.confirmation.EmailConfirmationService;
 import net.cyklotron.cms.modules.actions.BaseCMSAction;
 import net.cyklotron.cms.periodicals.PeriodicalsSubscriptionService;
 import net.cyklotron.cms.periodicals.SubscriptionRequestResource;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: ConvertCategoryQuery1B.java,v 1.3 2007-11-18 21:24:37 rafal Exp $
  */
 public class CYKLO789
     extends BaseCMSAction
 {
     private EmailConfirmationService emailConfirmationRequestService;
     
     public CYKLO789(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PeriodicalsSubscriptionService periodicalsSubscriptionService,EmailConfirmationService emailConfirmationRequestService)
     {
         super(logger, structureService, cmsDataFactory);
         this.emailConfirmationRequestService = emailConfirmationRequestService;
     }
 
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             QueryResults results = coralSession.getQuery().
                 executeQuery("FIND RESOURCE FROM "+SubscriptionRequestResource.CLASS_NAME);
             Resource[] nodes = results.getArray(1);
             int i;
             for(i = 0; i < nodes.length; i++)
             {
                System.out.print("("+i+") Processing: "+nodes[i].getPath());
                 SubscriptionRequestResource res = (SubscriptionRequestResource)nodes[i];
                 try
                 {
                     emailConfirmationRequestService.createEmailConfirmationRequest(coralSession, res.getEmail(), res.getItems());
                    System.out.println(" success");
                 }
                 catch(Exception e)
                 {
                     logger.error("",e);
                    System.out.println("SubscriptionRequest resource convertion failure.");
                 }
             }
             if(i == nodes.length)
             {
                 for(i = 0; i < nodes.length; i++)
                 {
                     try
                     {
                         coralSession.getStore().deleteResource((Resource)nodes[i]);
                     }
                     catch(EntityInUseException e)
                     {
                        System.out.println("SubscriptionRequest resources delete failure.");
                     }
                 }
             }
             templatingContext.put("result", "success");
         }
         catch(Exception e)
         {
             logger.error("CYKLO897 fix with error",e);            
         }
     }
     
 }
