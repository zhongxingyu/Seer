 package net.cyklotron.cms.modules.views.documents;
 
 import java.util.Arrays;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  * Stateful screen for propose document application.
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawe� Potempski</a>
 * @version $Id: ProposeDocument.java,v 1.10 2008-10-30 17:26:36 rafal Exp $
  */
 public class ProposeDocument
     extends BaseSkinableDocumentScreen
 {
     public ProposeDocument(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
         
     }
 
     public String getState()
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         return parameters.get("state","AddDocument");
     }
     
     public void prepareAddDocument(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         SiteResource site = getSite();
         try
         {
             templatingContext.put("styles", Arrays.asList(styleService.getStyles(coralSession, site)));
             long parent_node_id = parameters.getLong("parent_node_id", -1);
             if(parent_node_id == -1)
             {
                 templatingContext.put("parent_node",getHomePage());
             }
             else
             {
                 templatingContext.put("parent_node",NavigationNodeResourceImpl.getNavigationNodeResource(coralSession,parent_node_id));
             }
             // refill parameters in case we are coming back failed validation
             templatingContext.put("calendar_tree", parameters.getBoolean("calendar_tree", false));
             templatingContext.put("name", parameters.get("name",""));
             templatingContext.put("title", parameters.get("title",""));
             templatingContext.put("abstract", parameters.get("abstract",""));
             templatingContext.put("content", parameters.get("content",""));
             templatingContext.put("event_place", parameters.get("event_place",""));
             templatingContext.put("organized_by", parameters.get("organized_by",""));
             templatingContext.put("organized_address", parameters.get("organized_address",""));
             templatingContext.put("organized_phone", parameters.get("organized_phone",""));
             templatingContext.put("organized_fax", parameters.get("organized_fax",""));
             templatingContext.put("organized_email", parameters.get("organized_email",""));
             templatingContext.put("organized_www", parameters.get("organized_www",""));
             templatingContext.put("source", parameters.get("source",""));
             templatingContext.put("proposer_credentials", parameters.get("proposer_credentials",""));
             templatingContext.put("proposer_email", parameters.get("proposer_email",""));
             templatingContext.put("description", parameters.get("description",""));
            transferDateParam(parameters, templatingContext, "validity_start");
            transferDateParam(parameters, templatingContext, "validity_end");
            transferDateParam(parameters, templatingContext, "event_start");
            transferDateParam(parameters, templatingContext, "event_end");
             templatingContext.put("category_ids", parameters.getLongs("category_id"));
 
             prepareCategories(context, true);
 
             Parameters screenConfig = getConfiguration(coralSession);
             boolean attachmentsEnabled = screenConfig.getBoolean("attachments_enabled", false);
             if(attachmentsEnabled)
             {
                 templatingContext.put("attachments_enabled", attachmentsEnabled);
                 templatingContext.put("attachments_max_count", screenConfig.getInt(
                     "attachments_max_count", -1));
                 templatingContext.put("attachments_max_size", screenConfig.getInt(
                     "attachments_max_size", -1));
                 templatingContext.put("attachments_allowed_formats", screenConfig.get(
                     "attachments_allowed_formats", "jpg gif doc rtf pdf xls"));
             }
             
         }
         catch(Exception e)
         {
             screenError(getNode(), context, "Screen Error "+e);
         }
     }

    private void transferDateParam(Parameters parameters, TemplatingContext templatingContext, String param)
    {
        if(parameters.get(param, "").length() > 0)
        {
            templatingContext.put(param, Long.parseLong(parameters.get(param,"")));
        }
        else
        {
            templatingContext.remove(param);
        }
    }
     
     public void prepareResult(Context context)
         throws ProcessingException
     {
         // does nothing
     }
 }
