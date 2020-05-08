 package pg13.presentation;
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import pg13.org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 
 import pg13.business.UserManager;
 import pg13.models.User;
 
 public class LoginScreen extends Composite 
 {
 	private UserManager userManager;
 	private Combo cmbUsernames;
 	private Label lblInvalidUser;
 	private ControlDecoration invalidUserDecor;
 	
 	/**
 	 * Create and populates the login screen.
 	 * @param parent
 	 * @param style
 	 */
 	public LoginScreen(Composite parent, int style) 
 	{
 		super(parent, style);
 		userManager = new UserManager();
 		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		setLayout(new FormLayout());
 		
 		cmbUsernames = new Combo(this, SWT.READ_ONLY);
 		cmbUsernames.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
 		
 		populateUserList();
 		
 		FormData fd_cmbUsernames = new FormData();
 		fd_cmbUsernames.top = new FormAttachment(0, 200);
 		fd_cmbUsernames.left = new FormAttachment(50, -100);
 		fd_cmbUsernames.bottom = new FormAttachment(100, -69);
 		fd_cmbUsernames.right = new FormAttachment(50, 100);
 		cmbUsernames.setLayoutData(fd_cmbUsernames);
 		
 		lblInvalidUser = new Label(this, SWT.WRAP);
 		lblInvalidUser.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
 		lblInvalidUser.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblInvalidUser.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		FormData fd_lblInvalidUser = new FormData();
 		fd_lblInvalidUser.right = new FormAttachment(50, 100);
 		fd_lblInvalidUser.left = new FormAttachment(50, -100);
 		fd_lblInvalidUser.top = new FormAttachment(0, 250);
 		lblInvalidUser.setLayoutData(fd_lblInvalidUser);
 		lblInvalidUser.setText(MessageConstants.BLANK_USERNAME);
 		lblInvalidUser.setVisible(false);
 		
 		Label lblLoginInfo = new Label(this, SWT.WRAP | SWT.SHADOW_IN);
 		lblLoginInfo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblLoginInfo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblLoginInfo = new FormData();
 		fd_lblLoginInfo.top = new FormAttachment(0, 75);
 		fd_lblLoginInfo.bottom = new FormAttachment(0, 175);
 		fd_lblLoginInfo.left = new FormAttachment(0, 162);
 		fd_lblLoginInfo.right = new FormAttachment(100, -162);
 		lblLoginInfo.setLayoutData(fd_lblLoginInfo);
 		lblLoginInfo.setText(MessageConstants.LOGIN_INFO);
 		
 		Button btnLogMeIn = new Button(this, SWT.NONE);
 		FormData fd_btnLogMeIn = new FormData();
 		fd_btnLogMeIn.left = new FormAttachment(50, -100);
 		fd_btnLogMeIn.top = new FormAttachment(0, 300);
 		btnLogMeIn.setLayoutData(fd_btnLogMeIn);
 		
 		invalidUserDecor = new ControlDecoration(lblInvalidUser, SWT.LEFT | SWT.TOP);
 		invalidUserDecor.setMarginWidth(10);
		invalidUserDecor.setImage(SWTResourceManager.getImage(SignUpScreen.class, "/javax/swing/plaf/metal/icons/Error.gif"));
 		invalidUserDecor.setDescriptionText("Some description");
 		btnLogMeIn.addSelectionListener(new SelectionAdapter() 
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e) 
 			{
 				loginPressed();
 			}
 		});
 		btnLogMeIn.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
 		btnLogMeIn.setText(Constants.LOGIN_BUTTON);
 		
 		Button btnCancel = new Button(this, SWT.NONE);
 		FormData fd_btnCancel = new FormData();
 		fd_btnCancel.right = new FormAttachment(50, 100);
 		fd_btnCancel.top = new FormAttachment(0, 300);
 		btnCancel.setLayoutData(fd_btnCancel);
 		btnCancel.addSelectionListener(new SelectionAdapter() 
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e) 
 			{
 				clearScreen();
 				MainWindow.getInstance().switchToWelcomeScreen();
 			}
 		});
 		btnCancel.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
 		btnCancel.setText(Constants.CANCEL_BUTTON);
 
 	}
 
 	private void populateUserList() 
 	{
 		//convert arraylist of users from db to an array of strings
 		String [] users = new String [userManager.getNamesOfAllUsers().size()];
 		userManager.getNamesOfAllUsers().toArray(users);
 		cmbUsernames.setItems(users);
 	}
 	
 	public void refresh()
 	{
 		populateUserList();
 	}
 	
 	private void loginPressed()
 	{
 		ArrayList<User> allUsers = userManager.getAllUsers();
 		int selected = this.cmbUsernames.getSelectionIndex();
 		
 		if(selected >= 0)
 		{
 			clearScreen();
 			MainWindow.getInstance().login(allUsers.get(selected));
 			MainWindow.getInstance().switchToWelcomeScreen();			
 		}
 		else	//-1 it's blank, lets show an error message
 		{
 			lblInvalidUser.setVisible(true);
 			this.redraw();
 		}
 	}
 
 	private void clearScreen()
 	{
 		lblInvalidUser.setVisible(false);
 		
 	}
 
 	@Override
 	protected void checkSubclass() 
 	{
 		// Disable the check that prevents subclassing of SWT components
 	}
 }
