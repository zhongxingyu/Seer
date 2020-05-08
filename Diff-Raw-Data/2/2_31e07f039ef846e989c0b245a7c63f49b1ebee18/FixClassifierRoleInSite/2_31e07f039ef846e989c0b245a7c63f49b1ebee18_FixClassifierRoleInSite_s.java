 package net.cyklotron.cms.modules.actions.fixes;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.modules.actions.BaseCMSAction;
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 /**
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
  * @version $Id$
  */
 public class FixClassifierRoleInSite
     extends BaseCMSAction
 {
     private SiteService siteService;
 
     private SecurityService securityService;
 
     public FixClassifierRoleInSite(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, SiteService siteService, SecurityService securityService)
     {
         super(logger, structureService, cmsDataFactory);
         this.siteService = siteService;
         this.securityService = securityService;
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
             SiteResource[] sites = siteService.getSites(coralSession);
             for (SiteResource site : sites)
             {
                 Role administrator = site.getAdministrator();
                 securityService.createRole(coralSession, administrator, "cms.category.classifier",
                     site);
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to fix aggregation node", e);
         }
     }
 
     /**
      * @{inheritDoc}
      */
     public boolean checkAccessRights(Context context)
         throws Exception
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         Role cmsAdministrator = coralSession.getSecurity().
             getUniqueRole("cms.administrator");
         return coralSession.getUserSubject().hasRole(cmsAdministrator);
     }
 }
