 package edu.byu.isys413.data.views;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 import edu.byu.isys413.data.models.BusinessObjectDAO;
 import edu.byu.isys413.data.models.DataException;
 import edu.byu.isys413.data.models.Employee;
 import edu.byu.isys413.data.models.LDAP;
 import edu.byu.isys413.data.models.SearchCriteria;
 
 /**
  * This window enables employees to log in to the office system.
  *
  */
 public class LoginView extends Shell {
 	private Text txtNetid;
 	private Text txtPassword;
 	private int tryCount = 1;
	private int maxTryCount = 5;
 	private Label errorLabel;
 	private CLabel label;
 	private Group grpLoginStatus;
 	private Composite composite;
 	private Button btnCancel;
 	private Composite composite_1;
 
 	/**
 	 * Launch the application. Only used for testing.
 	 * @param args
 	 */
 	public static void main(String args[]) {
 		try {
 			Display display = Display.getDefault();
 			LoginView shell = new LoginView(display);
 			shell.open();
 			shell.layout();
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create the shell.
 	 * @param display
 	 */
 	public LoginView(Display display) {
 		super(display, SWT.CLOSE | SWT.APPLICATION_MODAL);
 		setMinimumSize(new Point(480, 215));
 		setImage(SWTResourceManager.getImage(LoginView.class, "/images/logo_camera.png"));
 		setLayout(new GridLayout(2, false));
 		
 		label = new CLabel(this, SWT.NONE);
 		label.setImage(SWTResourceManager.getImage(LoginView.class, "/images/logo_camera.png"));
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 4));
 		label.setText("");
 		
 		composite_1 = new Composite(this, SWT.NONE);
 		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2);
 		gd_composite_1.verticalIndent = 5;
 		composite_1.setLayoutData(gd_composite_1);
 		composite_1.setLayout(new GridLayout(2, false));
 		
 		Label lblNetid = new Label(composite_1, SWT.NONE);
 		lblNetid.setText("NetID:");
 		
 		txtNetid = new Text(composite_1, SWT.BORDER);
 		GridData gd_txtNetid = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_txtNetid.widthHint = 200;
 		txtNetid.setLayoutData(gd_txtNetid);
 		
 		Label lblPassword = new Label(composite_1, SWT.NONE);
 		lblPassword.setText("Password:");
 		
 		txtPassword = new Text(composite_1, SWT.BORDER | SWT.PASSWORD);
 		GridData gd_txtPassword = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_txtPassword.widthHint = 200;
 		txtPassword.setLayoutData(gd_txtPassword);
 		txtPassword.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if(e.keyCode == 13) {
 					login();
 				}
 			}
 		});
 		
 		grpLoginStatus = new Group(this, SWT.NONE);
 		grpLoginStatus.setText("Login Status");
 		grpLoginStatus.setLayout(new GridLayout(1, false));
 		grpLoginStatus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
 		
 		errorLabel = new Label(grpLoginStatus, SWT.WRAP);
 		errorLabel.setAlignment(SWT.CENTER);
 		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		errorLabel.setText("");
 		
 		composite = new Composite(this, SWT.NONE);
 		composite.setLayout(new GridLayout(2, false));
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
 		
 		btnCancel = new Button(composite, SWT.NONE);
 		btnCancel.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				System.exit(0);
 			}
 		});
 		btnCancel.setFont(SWTResourceManager.getFont("Buxton Sketch", 14, SWT.BOLD));
 		GridData gd_btnCancel = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
 		gd_btnCancel.widthHint = 120;
 		btnCancel.setLayoutData(gd_btnCancel);
 		btnCancel.setText("CANCEL");
 		
 		Button btnLogIn = new Button(composite, SWT.NONE);
 		GridData gd_btnLogIn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 		gd_btnLogIn.widthHint = 120;
 		btnLogIn.setLayoutData(gd_btnLogIn);
 		btnLogIn.setFont(SWTResourceManager.getFont("Buxton Sketch", 14, SWT.BOLD));
 		btnLogIn.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				login();
 			}
 		});
 		btnLogIn.setText("OK");
 		createContents();
 	}
 	
 	/**
 	 * Create contents of the shell.
 	 */
 	protected void createContents() {
 		setText("LogIn");
 		setSize(463, 215);
 	}
 	
 	/**
 	 * Grabs the input from the user and attempts
 	 */
 	private void login() {
 		String netId = txtNetid.getText();
 		String passwd = txtPassword.getText();
 		
		// First check to be sure that they filled in both fields. If they missed one, it doesn't increment their tryCount.
		if(netId.equals("")) {
			errorLabel.setText("Please provide a username.");
			return;
		}
		if(passwd.equals("")) {
			errorLabel.setText("Please provide a password.");
			return;
		}
		
 		LDAP ldap = new LDAP();
 		if(ldap.authenticate(netId, passwd)){
 			Employee emp = null;
 			try {
 				emp = BusinessObjectDAO.getInstance().searchForBO("Employee", new SearchCriteria("netid", netId));
 			} catch (DataException ex) {
 				ex.printStackTrace();
 			}
 			AppData.getInstance().setLoggedIn(true);
             AppData.getInstance().setEmployee(emp);
             dispose();
 		}
 		else{
 			
			if(tryCount <= maxTryCount){
				int triesLeft = maxTryCount - tryCount;
				errorLabel.setText(("Incorrect username and/or password. Please try again. (" + triesLeft + " " + (triesLeft == 1 ? "try" : "tries") + " remaining.)")); 
 				tryCount++;
 			}
 			else{
 				MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR); 
				messageBox.setMessage("Max login attempt count failed. Please try again later."); 
 				messageBox.open(); 
 				dispose();
 				System.exit(0);
 			}
 		}
 	}
 
 	/* @see org.eclipse.swt.widgets.Decorations#checkSubclass()*/
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 }
