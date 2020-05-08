 package net.cyklotron.cms.modules.components.documents;
 
import java.util.HashSet;
 import java.util.List;
import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.documents.DocumentNodeResource;
import net.cyklotron.cms.modules.components.BaseCMSComponent;
 import net.cyklotron.cms.modules.views.documents.DocumentStateTool;
 import net.cyklotron.cms.modules.views.documents.MyDocumentsImpl;
 
 public class MyDocuments
    extends BaseCMSComponent
 {
     private final MyDocumentsImpl myDocumentsImpl;
 
     private final TableStateManager tableStateManager;
 
     public MyDocuments(Context context, Logger logger, Templating templating,
        CmsDataFactory cmsDataFactory, TableStateManager tableStateManager,
        MyDocumentsImpl myDocumentsImpl)
     {
        super(context, logger, templating, cmsDataFactory);
         this.tableStateManager = tableStateManager;
         this.myDocumentsImpl = myDocumentsImpl;
     }
 
    @Override
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters config = cmsData.getComponent().getConfiguration();
             CategoryQueryResource includeQuery = myDocumentsImpl
                 .getQueryResource("include", config);
             CategoryQueryResource excludeQuery = myDocumentsImpl
                 .getQueryResource("exclude", config);
             String whereClause = "created_by = " + coralSession.getUserSubject().getIdString();
 
             TableModel<DocumentNodeResource> model;
             if(includeQuery == null)
             {
                 model = myDocumentsImpl.siteBasedModel(cmsData, i18nContext.get(), whereClause);
             }
             else
             {
                 model = myDocumentsImpl.queryBasedModel(includeQuery, excludeQuery, cmsData,
                     i18nContext.get(), whereClause);
             }
 
             List<TableFilter<? super DocumentNodeResource>> filters = myDocumentsImpl
                 .excludeStatesFilter("expired");
 
             TableState state = tableStateManager.getState(context, this.getClass().getName() + ":"
                 + cmsData.getNode().getIdString() + ":" + cmsData.getComponent().getInstanceName());
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setSortColumnName(config.get("sort_column", "creation.time"));
                 state.setAscSort(config.getBoolean("sort_dir", false));
                 state.setPageSize(config.getInt("page_size", 5));
             }
             templatingContext.put("table", new TableTool<DocumentNodeResource>(state, filters,
                 model));
             templatingContext.put("documentState", new DocumentStateTool(coralSession, logger));
             templatingContext.put("header", config.get("header", ""));
             final long moreNodeId = config.getLong("more_node_id", -1l);
             if(moreNodeId != -1l)
             {
                 templatingContext.put("moreNode", coralSession.getStore().getResource(moreNodeId));
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException("internal errror", e);
         }
     }
 }
