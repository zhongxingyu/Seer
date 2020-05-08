 package net.cyklotron.cms.modules.actions.fixes;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.forum.MessageResource;
 import net.cyklotron.cms.modules.actions.BaseCMSAction;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: ConvertCategoryQuery1B.java,v 1.3 2007-11-18 21:24:37 rafal Exp $
  */
 public class CYKLO798
     extends BaseCMSAction
 {
     
     public CYKLO798(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory)
     {
         super(logger, structureService, cmsDataFactory);
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
                 executeQuery("FIND RESOURCE FROM "+ MessageResource.CLASS_NAME);
             Resource[] nodes = results.getArray(1);
             int i;
             for(i = 0; i < nodes.length; i++)
             {
                 System.out.print("("+i+") Processing: "+nodes[i].getPath());
                 MessageResource res = (MessageResource)nodes[i];
                 try
                 {
                     if(!res.isStickedDefined())
                     {
                        res.setSticked(false);
                     }
                     System.out.println(" success");
                 }
                 catch(Exception e)
                 {
                     logger.error("",e);
                     System.out.println("Faild to set sticked attribute to MessageResource.");
                 }
             }
         }
         catch(Exception e)
         {
             logger.error("CYKLO798 fix with error",e);
         }
     }
     
 }
