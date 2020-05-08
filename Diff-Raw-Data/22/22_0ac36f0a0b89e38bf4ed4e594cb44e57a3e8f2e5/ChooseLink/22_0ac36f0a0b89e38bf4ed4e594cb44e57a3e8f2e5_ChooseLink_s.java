 package net.cyklotron.cms.modules.views.popup;
 
 import java.util.ArrayList;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.generic.ListTableModel;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.link.BaseLinkResource;
 import net.cyklotron.cms.link.BaseLinkResourceImpl;
 import net.cyklotron.cms.link.LinkRootResource;
 import net.cyklotron.cms.link.LinkRootResourceImpl;
 import net.cyklotron.cms.modules.views.BaseCMSScreen;
 import net.cyklotron.cms.preferences.PreferencesService;
import net.cyklotron.cms.util.CmsResourceListTableModel;
 
 /**
  * Choose category screen.
  *
  * @author <a href="lukasz@caltha.pl">Łukasz Urbański</a>
  * @version $Id: ChooseCategory.java,v 1.3 2005-01-26 09:00:36 pablo Exp $
  */
 public class ChooseLink extends BaseCMSScreen
 {
     protected IntegrationService integrationService;
     
     public ChooseLink(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, IntegrationService integrationService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager);
         this.integrationService = integrationService; 
     }
     
     
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession) 
         throws ProcessingException
     {
        int lsid = parameters.getInt("lsid", -1);
         if(lsid == -1)
         {
             throw new ProcessingException("Links root id not found");
         }
         try
        {
             LinkRootResource linksRoot = LinkRootResourceImpl.getLinkRootResource(coralSession, lsid);
             templatingContext.put("linksRoot", linksRoot);
             
             ArrayList links = new ArrayList();
             for(Resource link : coralSession.getStore().getResource(linksRoot))
             {
                 if(link instanceof BaseLinkResource)
                 {
                     links.add(link);
                 }
             }
             TableColumn[] columns = new TableColumn[0];
             TableState state = tableStateManager.getState(context, "cms:screens:popup,LinkList");
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setPageSize(10);
                 state.setSortColumnName("name");
             }
             TableModel model = new ListTableModel(links, columns);
             templatingContext.put("table", new TableTool(state, null, model));
             
             BaseLinkResource link = null;
             Long linkId = parameters.getLong("link_id", -1L);
             if(linkId != -1L){
                 link = BaseLinkResourceImpl.getBaseLinkResource(coralSession, linkId);
                 templatingContext.put("current_link", link);
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             logger.error("LinkException: ",e);
             return;
         }
     }
 }
