 package cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.qa;
 
 import java.util.LinkedList;
 import java.util.List;
import java.util.Set;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.CompoundPropertyModel;
 
import cz.cuni.mff.odcleanstore.webfrontend.bo.User;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.qa.Publisher;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.qa.QARule;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.Dao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.qa.PublisherDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.qa.QARuleDao;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.FrontendPage;
import cz.cuni.mff.odcleanstore.webfrontend.pages.useraccounts.EditAccountPermissionsPage;
 
 public class ManageQARuleRestrictionsPage extends FrontendPage 
 {
 	private static final long serialVersionUID = 1L;
 
 	private Dao<QARule> qaRuleDao;
 	private Dao<Publisher> publisherDao;
 
 	public ManageQARuleRestrictionsPage(final Long ruleId) 
 	{
 		super(
 			"Home > Transformers > QA > Rules management > Restrictions", 
 			"Manage QA rule restrictions"
 		);
 		
 		// prepare DAO objects
 		//
 		qaRuleDao = daoLookupFactory.getDao(QARuleDao.class);
 		publisherDao = daoLookupFactory.getDao(PublisherDao.class);
 		
 		// register page components
 		//
 		final QARule rule = qaRuleDao.load(ruleId);
 		
 		addRuleInformationSection(rule);
 		addRestrictionsSection(rule);
 	}
 
 	/*
 	 	=======================================================================
 	 	Rule information section
 	 	=======================================================================
 	*/
 
 	private void addRuleInformationSection(final QARule rule)
 	{
 		setDefaultModel(new CompoundPropertyModel<QARule>(rule));
 		
 		add(new Label("description"));
 		add(new Label("filter"));
 		add(new Label("coefficient"));
 	}
 	
 	/*
 	 	=======================================================================
 	 	Implementace restrictionsTable
 	 	=======================================================================
 	*/
 	
 	private void addRestrictionsSection(QARule rule) 
 	{
 		addNewRestrictionLink(rule.getId());
 		addRestrictionsTable(rule);
 	}
 
 	private void addNewRestrictionLink(final Long ruleId)
 	{
 		add(new Link("addNewRestrictionLink")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() 
 			{
 				setResponsePage(
 					new NewRestrictionPage(ruleId)
 				);
 			}
 		});
 	}
 	
 	private void addRestrictionsTable(final QARule rule) 
 	{
 		List<Publisher> allRestrictions = new LinkedList<Publisher>(rule.getPublisherRestrictions());
 		
 		ListView<Publisher> listView = new ListView<Publisher>("restrictionsTable", allRestrictions)
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void populateItem(ListItem<Publisher> item) 
 			{
 				final Publisher publisher = item.getModelObject();
 				
 				item.setModel(new CompoundPropertyModel<Publisher>(publisher));
 				
 				item.add(new Label("label"));
 				item.add(new Label("uri"));	
 				
 				addDeleteButton(item, rule, publisher);
 			}
 		};
 		
 		add(listView);
 	}
 	
 	private void addDeleteButton(ListItem<Publisher> item, final QARule rule, final Publisher publisher)
 	{
 		Link button = new Link("deleteRestriction")
         {
 			private static final long serialVersionUID = 1L;
 
 			@Override
             public void onClick()
             {
 				rule.removePublisherRestriction(publisher);
             	qaRuleDao.update(rule);
             	
 				getSession().info("The restriction was successfuly deleted.");
 				setResponsePage(new ManageQARuleRestrictionsPage(rule.getId()));
             }
         };
         
 		item.add(button);
 	}
 }
