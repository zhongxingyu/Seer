 package net.cyklotron.cms.category.components;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.table.StateFilter;
 import net.cyklotron.cms.util.CmsResourceListTableModel;
 
 /**
  * This component displays lists of hand-prioritzed resources assigned to queried categories.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: HoldingResourceList.java,v 1.5 2005-05-17 06:19:58 zwierzem Exp $
  */
 public class HoldingResourceList
 extends DocumentResourceList
 {
     public HoldingResourceList(Context context, IntegrationService integrationService,
         CmsDataFactory cmsDataFactory,  CategoryQueryService categoryQueryService,
         SiteService siteService, StructureService structureService)
     {
         super(context,integrationService, cmsDataFactory, categoryQueryService, siteService, structureService);
     }
 
     public BaseResourceListConfiguration createConfig()
         throws ProcessingException
     {
         return new HoldingResourceListConfiguration(cmsDataFactory.getCmsData(context).getDate());
     }
 
     protected TableModel getTableModel(
         Resource[] resources,
         BaseResourceListConfiguration config,
         I18nContext i18nContext)
         throws TableException
     {
         return new HoldingCmsResourceListTableModel(resources, config, i18nContext);
     }
 
 	// implementation /////////////////////////////////////////////////////////////////////////////
     
 	/**
 	 * Implementation of Table model for hand weighted CMS resources
 	 */
 	public class HoldingCmsResourceListTableModel extends CmsResourceListTableModel
 	{
 		protected HoldingResourceListConfiguration componentConfig;
 		protected boolean columnsCreated = false;
 		
 		public HoldingCmsResourceListTableModel(
 			Resource[] array,
 			BaseResourceListConfiguration config,
 			I18nContext i18nContext)
 			throws TableException
 		{
 			super(context, integrationService, array, i18nContext.getLocale());
 			componentConfig = (HoldingResourceListConfiguration) config;
 		}
 
 		public HoldingCmsResourceListTableModel(
 			List list,
 			BaseResourceListConfiguration config,
 			I18nContext i18nContext)
 			throws TableException
 		{
             super(context, integrationService, list, i18nContext.getLocale());
 			componentConfig = (HoldingResourceListConfiguration) config;
 		}
 
 		protected TableColumn[] getColumns(TableColumn[] cols) throws TableException
 		{
 			TableColumn[] newCols = new TableColumn[cols.length];
 			for(int i=0; i<cols.length; i++)
 			{
 				TableColumn col = cols[i];
 				newCols[i] = new TableColumn(col.getName(),
 					new HoldingComparator(componentConfig, col.getComparator()),
 					new HoldingComparator(componentConfig, col.getReverseComparator()));
 			}
 			return newCols;
 		}
         
         public TableColumn[] getColumns()
         {
         	if(!columnsCreated)
         	{
 				columns = super.getColumns();
         		try
                 {
                     columns = getColumns(columns);
                 }
                 catch (TableException e)
                 {
                 	throw new RuntimeException("Cannot create columns", e);
                 }
                 columnsCreated = true;
         	}
 			return columns;
         }
 
 	}
 	
     protected TableFilter<Resource>[] getTableFilters(CoralSession coralSession,
         BaseResourceListConfiguration config)
         throws ProcessingException
     {
         List<TableFilter<Resource>> filters = new ArrayList<TableFilter<Resource>>();
        filters.add(new StateFilter(new String[]{"Published"}));
         return filters.toArray(new TableFilter[filters.size()]);
     }
 	
 	public class HoldingComparator implements Comparator
 	{
 		private HoldingResourceListConfiguration config;
 		private Comparator comparator; 
 		
         public HoldingComparator(HoldingResourceListConfiguration config, Comparator comparator)
         {
         	this.config = config;
 			this.comparator = comparator;
         }
 
         /* (non-Javadoc)
          * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
          */
         public int compare(Object o1, Object o2)
         {
             Resource r1 = (Resource) o1;
 			Resource r2 = (Resource) o2;
 			if(config.hasResource(r1))
 			{
 				if(config.hasResource(r2))
 				{
 					// the sorting is reverse
 					int result = config.getWeight(r2) - config.getWeight(r1);
 					// sort according to weight if weights are different
 					if(result != 0)
 					{
 						return result;
 					}
 				}
 				else
 				{
 					// treat the weighted resource as lesser one
 					return -1;
 				}
 			}
 			else if(config.hasResource(r2))
 			{
 				// treat the weighted resource as lesser one
 				return 1; 
 			}
 			// no weighted resources - sort normally
             return comparator.compare(o1, o2);
         }
 	}	
 }
