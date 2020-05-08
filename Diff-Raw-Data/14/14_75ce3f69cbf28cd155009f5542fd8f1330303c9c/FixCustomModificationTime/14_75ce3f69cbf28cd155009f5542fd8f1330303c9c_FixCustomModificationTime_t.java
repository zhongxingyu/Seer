 package net.cyklotron.cms.modules.actions.fixes;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.modules.actions.structure.BaseStructureAction;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.encodings.HTMLEntityDecoder;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 /**
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: FixCustomModificationTime.java,v 1.2 2006-02-28 16:17:53 pablo Exp $
  */
 public class FixCustomModificationTime extends BaseStructureAction
 {
     public FixCustomModificationTime(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService)
     {
         super(logger, structureService, cmsDataFactory, styleService);
     }
     
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         StringBuilder sb = new StringBuilder("FIND RESOURCE FROM documents.document_node" +
        " WHERE NOT DEFINED customModificationTime");
         try
         {
             QueryResults results = coralSession.getQuery().executeQuery(sb.toString());
             Resource[] nodes = results.getArray(1);
             for (int i = 0; i < nodes.length; i++)
             {
                 if(nodes[i] instanceof NavigationNodeResource)
                 {
                     NavigationNodeResource nnr = (NavigationNodeResource)nodes[i];
                     if(nnr.getCustomModificationTime() == null)
                     {
                         nnr.setCustomModificationTime(nnr.getModificationTime());
                         nnr.update();
                     }
                 }
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to invoke fix");
         }
     }
 }
