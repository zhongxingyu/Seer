 package net.cyklotron.cms.modules.actions.structure;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.integration.ApplicationResource;
 import net.cyklotron.cms.integration.ComponentResource;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 public class SetComponentClass
     extends BaseStructureAction
 {
     protected PreferencesService preferencesService;
 
     protected IntegrationService integrationService;
 
     public SetComponentClass(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService,
         PreferencesService preferencesService, IntegrationService integrationService)
     {
         super(logger, structureService, cmsDataFactory, styleService);
         this.preferencesService = preferencesService;
         this.integrationService = integrationService;
     }
 
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             String instance = parameters.get("instance");
             long componentId = parameters.getLong("component_id");
             ComponentResource component = (ComponentResource)coralSession.getStore().
                 getResource(componentId);
             ApplicationResource application = integrationService.getApplication(coralSession, component);
             Parameters preferences;
             CmsData cmsData = getCmsData(context);
             if(cmsData.getNode() != null)
             {
                 preferences = preferencesService.getNodePreferences(cmsData.getNode());
             }
             else
             {
                 preferences = preferencesService.getSystemPreferences(coralSession);
             }
             preferences.set("component."+instance+".app", application.getApplicationName());
            preferences.set("component."+instance+".class", component.getComponentName());
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to set component class", e);
         }
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         CmsData cmsData = getCmsData(context);
         if(cmsData.getNode() != null)
         {
             return getCmsData(context).getNode().canModify(context,coralSession.getUserSubject());
         }
         else
         {
             // privileges needed for configuring global components
             return checkAdministrator(context);
         }
     }
 }
