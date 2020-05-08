 package cz.cuni.mff.odcleanstore.webfrontend.pages.useraccounts;
 
 import javax.mail.MessagingException;
 
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.CompoundPropertyModel;
 
 import cz.cuni.mff.odcleanstore.configuration.WebFrontendConfig;
 import cz.cuni.mff.odcleanstore.webfrontend.behaviours.ConfirmationBoxRenderer;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.Role;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.User;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.DeleteConfirmationMessage;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.DeleteRawButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.RedirectWithParamButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.SortTableButton;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.UnobtrusivePagingNavigator;
import cz.cuni.mff.odcleanstore.webfrontend.core.models.DataProvider;
import cz.cuni.mff.odcleanstore.webfrontend.core.models.DependentSortableDataProvider;
 import cz.cuni.mff.odcleanstore.webfrontend.core.models.GenericSortableDataProvider;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.exceptions.DaoException;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.users.UserDao;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.FrontendPage;
 import cz.cuni.mff.odcleanstore.webfrontend.util.Mail;
 import cz.cuni.mff.odcleanstore.webfrontend.util.NewPasswordMail;
 import cz.cuni.mff.odcleanstore.webfrontend.util.PasswordHandling;
 
 @AuthorizeInstantiation({ "ADM" })
 public class AccountsListPage extends FrontendPage
 {
 	private static final long serialVersionUID = 1L;
 
 	private DaoForEntityWithSurrogateKey<User> userDao;
 	
 	public AccountsListPage() 
 	{
 		super(
 			"Home > User accounts > List",
 			"List all user accounts"
 		);
 		
 		// prepare DAO objects
 		//
 		userDao = daoLookupFactory.getDaoForEntityWithSurrogateKey(UserDao.class);
 		
 		// register page components
 		//
 		addHelpWindow(new UserAccountHelpPanel("content"));
 		addAccountsListTable();
 	}
 	
 	private void addAccountsListTable()
 	{
 		SortableDataProvider<User> data = new GenericSortableDataProvider<User>(userDao, "username");
 		
 		DataView<User> dataView = new DataView<User>("accountsListTable", data)
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(Item<User> item) 
 			{
 				User user = item.getModelObject();
 				
 				item.setModel(new CompoundPropertyModel<User>(user));
 				
 				item.add(new Label("username"));
 				item.add(new Label("email"));
 				item.add(new Label("firstname"));
 				item.add(new Label("surname"));
 				
 				for (Role role : Role.standardRoles)
 					item.add(createRoleLabel(user, role));
 				
 				item.add(
 					new DeleteRawButton<User>
 					(
 						userDao,
 						user.getId(),
 						"user",
 						new DeleteConfirmationMessage("user"),
 						AccountsListPage.this
 					)
 				);
 				
 				item.add(
 					new RedirectWithParamButton(
 						EditAccountPermissionsPage.class, 
 						user.getId(), 
 						"managePermissions"
 					)
 				);
 				
 				item.add(createSendNewPasswordButton(user.getId()));
 			}
 		};
 		
 		dataView.setItemsPerPage(ITEMS_PER_PAGE);
 		
 		add(new SortTableButton<User>("sortByUsername", "username", data, dataView));
 		add(new SortTableButton<User>("sortByEmail", "email", data, dataView));
 		add(new SortTableButton<User>("sortByFirstname", "firstname", data, dataView));
 		add(new SortTableButton<User>("sortBySurname", "surname", data, dataView));		
 		
 		add(dataView);
 		
 		add(new UnobtrusivePagingNavigator("navigator", dataView));
 	}
 	
 	protected Link createSendNewPasswordButton(final Long userId) 
 	{
 		Link button = new Link("sendNewPassword")
 		{
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void onClick() 
 			{
 				User user = userDao.load(userId);
 				WebFrontendConfig config = AccountsListPage.this.getApp().getConfiguration();
 				
 				try 
 				{
 					String password = PasswordHandling.generatePassword();
 					String salt = PasswordHandling.generateSalt();
 					String passwordHash = PasswordHandling.calculatePasswordHash(password, salt);
 					
 					user.setPasswordHash(passwordHash);
 					user.setSalt(salt);
 					
 					Mail mail = new NewPasswordMail(user, password);
 					
 					userDao.update(
 						user,
 						new SendConfirmationEmailSnippet(config, mail)
 					);
 				}
 				catch (DaoException ex) 
 				{
 					getSession().error(ex.getMessage());
 					return;
 				}
 				catch (MessagingException ex)
 				{
 					getSession().error(ex.getMessage());
 					return;
 				}
 				catch (Exception ex)
 				{
 					getSession().error("The password could not be updated due to an unexpected error.");
 					return;
 				}
 								
 				getSession().info("The password was successfuly updated.");
 				setResponsePage(AccountsListPage.class);
 			}
 		};
 		
 		button.add(
 			new ConfirmationBoxRenderer(
 				"Are you sure you want to reset the old user password?"
 			)
 		);
 		
 		return button;
 	}
 
 	/**
 	 * Returns the correct label for the given user and the given role
 	 * (e.g. returns an empty label if the user does not have the role
 	 * assigned and an crossline if he does).
 	 * 
 	 * @param user
 	 * @param roleName
 	 * @return
 	 */
 	private Label createRoleLabel(User user, Role role)
 	{	
 		String roleLabel = "role" + role.getLabel();
 		
 		if (user.hasAssignedRole(role))
 			return new Label(roleLabel, "X");
 		
 		Label label = new Label(roleLabel, "&nbsp;");
 		label.setEscapeModelStrings(false);
 		
 		return label;
 	}
 }
