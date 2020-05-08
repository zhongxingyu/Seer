 package cz.cuni.mff.odcleanstore.webfrontend.pages.prefixes;
 
import org.apache.log4j.Logger;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.Role;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.prefixes.Prefix;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.exceptions.DaoException;
import cz.cuni.mff.odcleanstore.webfrontend.dao.exceptions.NonUniquePrimaryKeyException;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.prefixes.PrefixDao;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.FrontendPage;
 import cz.cuni.mff.odcleanstore.webfrontend.validators.IRIValidator;
 
 @AuthorizeInstantiation({ Role.ADM })
 public class NewPrefixPage extends FrontendPage
 {
 	private static final long serialVersionUID = 1L;
 
	private static Logger logger = Logger.getLogger(NewPrefixPage.class);
	
 	private PrefixDao prefixMappingDao;
 	
 	public NewPrefixPage() 
 	{
 		super
 		(
 			"Home > Namespace Prefixes > New", 
 			"Add a new namespace prefix"
 		);
 		
 
 		// prepare DAO objects
 		//
 		prefixMappingDao = daoLookupFactory.getDao(PrefixDao.class);
 		
 		// register page components
 		//
 		addHelpWindow(new URLPrefixHelpPanel("content"));
 		addNewPrefixForm();
 	}
 	
 	private void addNewPrefixForm()
 	{
 		IModel<Prefix> formModel = new CompoundPropertyModel<Prefix>(new Prefix());
 		
 		Form<Prefix> form = new Form<Prefix>("newPrefixForm", formModel)
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit()
 			{
 				Prefix mapping = this.getModelObject();
 				
 				try {
 					prefixMappingDao.save(mapping);
 				}
				catch (NonUniquePrimaryKeyException ex)
				{
					getSession().error("The given prefix has already been registered.");
					return;
				}
 				catch (DaoException ex)
 				{
 					getSession().error(ex.getMessage());
 					return;
 				}
 				catch (Exception ex)
 				{
					logger.error(ex.getClass() + ": " + ex.getMessage());
 					
 					getSession().error(
 						"The prefix could not be registered due to an unexpected error."
 					);
 					
 					return;
 				}
 				
 				getSession().info("The prefix was successfuly registered.");
 				setResponsePage(PrefixesListPage.class);
 			}
 		};
 		
 		form.add(createTextfield("prefix"));
 		form.add(createURITextField());
 		
 		add(form);
 	}
 	
 	private TextField<String> createURITextField()
 	{
 		TextField<String> textField = createTextfield("url");
 		textField.add(new IRIValidator());
 		return textField;
 	}
 }
