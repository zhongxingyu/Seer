 package cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.oi;
 
 import java.util.List;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.CompoundPropertyModel;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.oi.OIRulesGroup;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.Dao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.oi.OIRulesGroupDao;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.FrontendPage;
 
 public class OIRulesManagementPage extends FrontendPage
 {
 	private static final long serialVersionUID = 1L;
 
 	private Dao<OIRulesGroup> oiRulesGroupsDao;
 	
 	public OIRulesManagementPage() 
 	{
 		super(
 			"Home > Transformers > OI > Rules management", 
 			"OI Rules management"
 		);
 		
 		// prepare DAO objects
 		//
 		oiRulesGroupsDao = daoLookupFactory.getDao(OIRulesGroupDao.class);
 		
 		// register page components
 		//
 		addOIRulesGroupsTable();
 	}
	
 	/*
 	 	=======================================================================
 	 	Implementace oiRulesGroupsTable
 	 	=======================================================================
 	*/
 	
 	private void addOIRulesGroupsTable()
 	{
 		List<OIRulesGroup> allGroups = oiRulesGroupsDao.loadAll();
 		
 		ListView<OIRulesGroup> listView = new ListView<OIRulesGroup>("oiRulesGroupsTable", allGroups)
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void populateItem(ListItem<OIRulesGroup> item) 
 			{
 				final OIRulesGroup group = item.getModelObject();
 				
 				item.setModel(new CompoundPropertyModel<OIRulesGroup>(group));
 
 				item.add(new Label("label"));
 				item.add(new Label("description"));	
 				
 				addDeleteButton(item, group);
 				addManageRulesButton(item, group);
 			}
 		};
 		
 		add(listView);
 	}
 	
 	private void addDeleteButton(ListItem<OIRulesGroup> item, final OIRulesGroup group)
 	{
 		Link button = new Link("deleteGroup")
 	    {
 			private static final long serialVersionUID = 1L;
 	
 			@Override
 	        public void onClick()
 	        {
 	        	oiRulesGroupsDao.delete(group);
 	        	
 				getSession().info("The group was successfuly deleted.");
 				setResponsePage(OIRulesManagementPage.class);
 	        }
 	    };
 	    
 		item.add(button);
 	}
 	
 	private void addManageRulesButton(ListItem<OIRulesGroup> item, final OIRulesGroup group)
 	{
 		Link button = new Link("manageRules")
 	    {
 			private static final long serialVersionUID = 1L;
 	
 			@Override
 	        public void onClick()
 	        {
 	        	setResponsePage(
 	        		new ManageGroupRulesPage(group.getId())
 				);
 	        }
 	    };
 	    
 		item.add(button);
 	}
 }
