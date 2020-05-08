 package au.org.scoutmaster.views;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import au.com.vaadinutils.crud.BaseCrudView;
 import au.com.vaadinutils.crud.CrudAction;
 import au.com.vaadinutils.crud.HeadingPropertySet;
 import au.com.vaadinutils.crud.HeadingPropertySet.Builder;
 import au.com.vaadinutils.crud.ValidatingFieldGroup;
 import au.com.vaadinutils.menu.Menu;
 import au.com.vaadinutils.validator.MobilePhoneValidator;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.domain.access.User;
 import au.org.scoutmaster.domain.access.User_;
 import au.org.scoutmaster.util.SMMultiColumnFormLayout;
 import au.org.scoutmaster.validator.PasswordValidator;
 
 import com.vaadin.addon.jpacontainer.EntityItem;
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.data.Container.Filter;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.data.util.filter.Or;
 import com.vaadin.data.util.filter.SimpleStringFilter;
 import com.vaadin.event.FieldEvents.FocusEvent;
 import com.vaadin.event.FieldEvents.FocusListener;
 import com.vaadin.event.FieldEvents.TextChangeEvent;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.ui.AbstractLayout;
 import com.vaadin.ui.PasswordField;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 @Menu(display = "Users", path="Admin")
 public class UserView extends BaseCrudView<User> implements View, Selected<User>, TextChangeListener, FocusListener
 {
 
 	private static final long serialVersionUID = 1L;
 
 	@SuppressWarnings("unused")
 	private static Logger logger = Logger.getLogger(UserView.class);
 
 	public static final String NAME = "User";
 
 	private PasswordField password;
 
 	private PasswordField confirmPassword;
 
 	private boolean passwordChanged = false;
 
 	@Override
 	protected AbstractLayout buildEditor(ValidatingFieldGroup<User> fieldGroup2)
 	{
 		VerticalLayout layout = new VerticalLayout();
 
 		SMMultiColumnFormLayout<User> overviewForm = new SMMultiColumnFormLayout<User>(1, this.fieldGroup);
 		overviewForm.setColumnFieldWidth(0, 280);
 		overviewForm.setColumnLabelWidth(0, 100);
 		overviewForm.setSizeFull();
 
 		overviewForm.bindTextField("Username", User_.username);
 		password = overviewForm.addPasswordField("Password");
 		password.addTextChangeListener(this);
 		password.addFocusListener(this);
 		confirmPassword = overviewForm.addPasswordField("Confirm Password");
 		confirmPassword.addFocusListener(this);
 		confirmPassword.addTextChangeListener(this);
 		overviewForm.bindBooleanField("Enabled", User_.enabled);
 		TextField emailAddress = overviewForm.bindTextField("Email Address", User_.emailAddress);
 		emailAddress.addValidator(new com.vaadin.data.validator.EmailValidator("Enter a valid Email Address."));
 		layout.addComponent(overviewForm);
 		overviewForm.bindTextField("Firstname", User_.firstname);
 		overviewForm.bindTextField("Surname", User_.surname);
 		TextField mobile = overviewForm.bindTextField("Sender Mobile", User_.senderMobile);
		emailAddress.addValidator(new MobilePhoneValidator("Enter a valid Mobile No."));
 		mobile.setDescription("Used when sending bulk emails as the sender phone no.");
 		
 
 		return layout;
 	}
 
 	@Override
 	protected void interceptSaveValues(EntityItem<User> item)
 	{
 		if (passwordChanged)
 			item.getEntity().setPassword(password.getValue());
 	}
 
 	@Override
 	public void enter(ViewChangeEvent event)
 	{
 		JPAContainer<User> container = new DaoFactory().getUserDao().createVaadinContainer();
 		container.sort(new String[]
 		{ User_.username.getName() }, new boolean[]
 		{ true });
 
 		Builder<User> builder = new HeadingPropertySet.Builder<User>();
 		builder.addColumn("Username", User_.username).addColumn("Enabled", User_.enabled)
 				.addColumn("Email", User_.emailAddress);
 
 		super.init(User.class, container, builder.build());
 	}
 
 	@Override
 	protected Filter getContainerFilter(String filterString, boolean advancedSearchActive)
 	{
 		return new Or(new SimpleStringFilter(User_.username.getName(), filterString, true, false),
 				new SimpleStringFilter(User_.emailAddress, filterString, true, false));
 	}
 
 	@Override
 	protected void formValidate() throws InvalidValueException
 	{
 		password.validate();
 		confirmPassword.validate();
 	}
 
 	@Override
 	public void rowChanged(EntityItem<User> item)
 	{
 		if (item != null)
 		{
 
 			if (super.isNew())
 			{
 				password.addValidator(new PasswordValidator("Password"));
 				confirmPassword.addValidator(new PasswordValidator("Confirm Password"));
 				password.setValue("");
 				confirmPassword.setValue("");
 
 			}
 			else
 			{
 				// Clear the validators as we only enable them if the user
 				// clicks into one of the password fields.
 				password.removeAllValidators();
 				confirmPassword.removeAllValidators();
 
 				// push a value so it looks like the password is filled out.
 				password.setValue("********");
 				confirmPassword.setValue("********");
 			}
 		}
 		else
 		{
 			password.setValue("");
 			confirmPassword.setValue("");
 		}
 		super.rowChanged(item);
 		this.passwordChanged = false;
 
 	}
 
 	/**
 	 * One of the password fields has changed so we need to inject the
 	 * validators.
 	 */
 	@Override
 	public void textChange(TextChangeEvent event)
 	{
 		this.passwordChanged = true;
 
 		password.addValidator(new PasswordValidator("Password"));
 		confirmPassword.addValidator(new PasswordValidator("Confirm Password"));
 
 	}
 
 	@Override
 	protected List<CrudAction<User>> getCrudActions()
 	{
 		List<CrudAction<User>> actions = super.getCrudActions();
 		
 		actions.add(new InviteUserAction());
 		return actions;
 	}
 
 	@Override
 	public void focus(FocusEvent event)
 	{
 		if (passwordChanged == false)
 		{
 			password.setValue("");
 			confirmPassword.setValue("");
 		}
 
 	}
 
 	@Override
 	protected String getTitleText()
 	{
 		return "Users";
 	};
 
 }
