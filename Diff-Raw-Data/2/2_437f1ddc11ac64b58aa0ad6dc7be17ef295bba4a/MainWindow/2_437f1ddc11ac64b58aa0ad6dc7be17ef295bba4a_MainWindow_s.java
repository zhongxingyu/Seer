 package pg13.presentation;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import pg13.org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.graphics.Point;
 
 import pg13.models.Cryptogram;
 import pg13.models.Puzzle;
 import pg13.models.User;
 import org.eclipse.swt.layout.GridData;
 
 public class MainWindow
 {
 	private static MainWindow instance;
 
 	private Display display;
 	private Shell shell;
 	private CreateScreen cmpCreateScreen;
 	private FindScreen cmpFindScreen;
 	private PlayScreen cmpPlayScreen;
 	private SignUpScreen cmpSignUpScreen;
 	private LoginScreen cmpLoginScreen;
 	private Label lblLoggedInAs;
 	private User loggedInUser;
 	private Button btnPlay;
 	private Button btnConnect;
 	private Button btnCreate;
 	private Composite cmpLogin;
 	private Label lblToolbarSeparator;
 	private Composite cmpMainArea;
 	private Label lblWelcomeDescription;
 	private Button btnMainSignUp;
 	private Button btnMainLogin;
 	private Label lblOr;
 
 	public static MainWindow getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new MainWindow();
 		}
 
 		return instance;
 	}
 
 	private MainWindow()
 	{
 		display = Display.getDefault();
 		createWindow();
 	}
 
 	public User getLoggedInUser()
 	{
 		return this.loggedInUser;
 	}
 
 	public void runWindow()
 	{
 		while (!shell.isDisposed())
 		{
 			if (!display.readAndDispatch())
 			{
 				display.sleep();
 			}
 		}
 		display.dispose();
 	}
 
 	public void createWindow()
 	{
 		shell = new Shell();
 		shell.setMinimumSize(new Point(768, 576));
 		shell.setSize(774, 616);
 		shell.setLocation(150, 100);
 		shell.setText(Constants.PG13);
 		shell.setLayout(new FormLayout());
 
 		// play button
 		btnPlay = new Button(shell, SWT.NONE);
 		btnPlay.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
				switchToFindScreen();
 			}
 		});
 		FormData fd_btnPlay = new FormData();
 		fd_btnPlay.right = new FormAttachment(0, 86);
 		fd_btnPlay.top = new FormAttachment(0, 11);
 		fd_btnPlay.left = new FormAttachment(0, 5);
 		btnPlay.setLayoutData(fd_btnPlay);
 		btnPlay.setText(Constants.PLAY);
 
 		// connect button
 		btnConnect = new Button(shell, SWT.NONE);
 		btnConnect.setEnabled(false);
 		FormData fd_btnConnect = new FormData();
 		fd_btnConnect.right = new FormAttachment(0, 172);
 		fd_btnConnect.top = new FormAttachment(0, 11);
 		fd_btnConnect.left = new FormAttachment(0, 91);
 		btnConnect.setLayoutData(fd_btnConnect);
 		btnConnect.setText(Constants.CONNECT);
 
 		// create button
 		btnCreate = new Button(shell, SWT.NONE);
 		btnCreate.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				createPressed();
 			}
 		});
 		FormData fd_btnCreate = new FormData();
 		fd_btnCreate.right = new FormAttachment(0, 258);
 		fd_btnCreate.top = new FormAttachment(0, 11);
 		fd_btnCreate.left = new FormAttachment(0, 177);
 		btnCreate.setLayoutData(fd_btnCreate);
 		btnCreate.setText(Constants.CREATE);
 
 		// container for user login info
 		cmpLogin = new Composite(shell, SWT.BORDER);
 		FormData fd_cmpLogin = new FormData();
 		fd_cmpLogin.right = new FormAttachment(100, -5);
 		fd_cmpLogin.top = new FormAttachment(0, 5);
 		fd_cmpLogin.left = new FormAttachment(100, -392);
 		cmpLogin.setLayoutData(fd_cmpLogin);
 		cmpLogin.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_TITLE_BACKGROUND));
 		cmpLogin.setLayout(new GridLayout(2, false));
 
 		// label that identifies
 		lblLoggedInAs = new Label(cmpLogin, SWT.NONE);
 		GridData gd_lblLoggedInAs = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_lblLoggedInAs.widthHint = 185;
 		lblLoggedInAs.setLayoutData(gd_lblLoggedInAs);
 		lblLoggedInAs.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_TITLE_BACKGROUND));
 		lblLoggedInAs.setText(MessageConstants.LOGON);
 
 		// toolbar that contains the user buttons
 		ToolBar toolBar = new ToolBar(cmpLogin, SWT.FLAT);
 		toolBar.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_TITLE_BACKGROUND));
 
 		// separator
 		ToolItem tltmSeparator1 = new ToolItem(toolBar, SWT.SEPARATOR);
 		tltmSeparator1.setText("sep");
 
 		// my puzzles button
 		ToolItem tltmMyPuzzles = new ToolItem(toolBar, SWT.NONE);
 		tltmMyPuzzles.setText(Constants.MY_PUZZLES);
 		tltmMyPuzzles.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				switchToFindScreen();
 				cmpFindScreen.selectMyPuzzles();
 				cmpFindScreen.filterByMyPuzzles();
 			}
 		});
 
 		// separator
 		ToolItem tltmSeparator2 = new ToolItem(toolBar, SWT.SEPARATOR);
 		tltmSeparator2.setText("sep");
 
 		// home button
 		ToolItem tltmHome = new ToolItem(toolBar, SWT.NONE);
 		tltmHome.setText(Constants.HOME);
 		tltmHome.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				switchToWelcomeScreen();
 			}
 		});
 
 		// separator
 		ToolItem tltmSeparator3 = new ToolItem(toolBar, SWT.SEPARATOR);
 		tltmSeparator3.setSelection(true);
 		tltmSeparator3.setEnabled(true);
 		tltmSeparator3.setText("sep");
 
 		// logout button
 		ToolItem tltmLogout = new ToolItem(toolBar, SWT.NONE);
 		tltmLogout.setText(Constants.LOGOUT);
 		tltmLogout.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				logout();
 			}
 		});
 
 		// horizontal line
 		lblToolbarSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
 		FormData fd_lblToolbarSeparator = new FormData();
 		fd_lblToolbarSeparator.right = new FormAttachment(100, -5);
 		fd_lblToolbarSeparator.top = new FormAttachment(0, 46);
 		fd_lblToolbarSeparator.left = new FormAttachment(0, 5);
 		lblToolbarSeparator.setLayoutData(fd_lblToolbarSeparator);
 
 		// main area, where the create and play screens will be contained
 		cmpMainArea = new Composite(shell, SWT.BORDER);
 		cmpMainArea.setLayout(new FormLayout());
 		FormData fd_cmpMainArea = new FormData();
 		fd_cmpMainArea.bottom = new FormAttachment(100, -35);
 		fd_cmpMainArea.right = new FormAttachment(100, -5);
 		fd_cmpMainArea.top = new FormAttachment(0, 50);
 		fd_cmpMainArea.left = new FormAttachment(0, 5);
 		cmpMainArea.setLayoutData(fd_cmpMainArea);
 		cmpMainArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 
 		// load the create screen, but hide it
 		cmpCreateScreen = new CreateScreen(cmpMainArea, SWT.NONE);
 		FormData fd_cmpCreateScreen = new FormData();
 		fd_cmpCreateScreen.bottom = new FormAttachment(100);
 		fd_cmpCreateScreen.right = new FormAttachment(100);
 		fd_cmpCreateScreen.top = new FormAttachment(0);
 		fd_cmpCreateScreen.left = new FormAttachment(0);
 		cmpCreateScreen.setLayoutData(fd_cmpCreateScreen);
 		cmpCreateScreen.setVisible(false);
 
 		// load the create screen, but hide it
 		cmpFindScreen = new FindScreen(cmpMainArea, SWT.NONE);
 		FormData fd_cmpFindScreen = new FormData();
 		fd_cmpFindScreen.bottom = new FormAttachment(100);
 		fd_cmpFindScreen.right = new FormAttachment(100);
 		fd_cmpFindScreen.top = new FormAttachment(0);
 		fd_cmpFindScreen.left = new FormAttachment(0);
 		cmpFindScreen.setLayoutData(fd_cmpFindScreen);
 		cmpFindScreen.setVisible(false);
 
 		// load the play screen, but hide it
 		cmpPlayScreen = new PlayScreen(cmpMainArea, SWT.NONE);
 		FormData fd_cmpPlayScreen = new FormData();
 		fd_cmpPlayScreen.bottom = new FormAttachment(100);
 		fd_cmpPlayScreen.right = new FormAttachment(100);
 		fd_cmpPlayScreen.top = new FormAttachment(0);
 		fd_cmpPlayScreen.left = new FormAttachment(0);
 		cmpPlayScreen.setLayoutData(fd_cmpPlayScreen);
 		cmpPlayScreen.setVisible(false);
 
 		// load the sign up screen, but hide it
 		cmpSignUpScreen = new SignUpScreen(cmpMainArea, SWT.NONE);
 		FormData fd_cmpSignUpScreen = new FormData();
 		fd_cmpSignUpScreen.bottom = new FormAttachment(100);
 		fd_cmpSignUpScreen.right = new FormAttachment(100);
 		fd_cmpSignUpScreen.top = new FormAttachment(0);
 		fd_cmpSignUpScreen.left = new FormAttachment(0);
 		cmpSignUpScreen.setLayoutData(fd_cmpSignUpScreen);
 		cmpSignUpScreen.setVisible(false);
 
 		// load the login screen, but hide it
 		cmpLoginScreen = new LoginScreen(cmpMainArea, SWT.NONE);
 		FormData fd_cmpLoginScreen = new FormData();
 		fd_cmpLoginScreen.bottom = new FormAttachment(100);
 		fd_cmpLoginScreen.right = new FormAttachment(100);
 		fd_cmpLoginScreen.top = new FormAttachment(0);
 		fd_cmpLoginScreen.left = new FormAttachment(0);
 		cmpLoginScreen.setLayoutData(fd_cmpLoginScreen);
 		cmpLoginScreen.setVisible(false);
 
 		// welcome message
 		Label lblWelcome = new Label(cmpMainArea, SWT.CENTER);
 		lblWelcome.setFont(SWTResourceManager.getFont("Segoe UI", 22,
 				SWT.NORMAL));
 		lblWelcome.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblWelcome = new FormData();
 		fd_lblWelcome.top = new FormAttachment(50, -130);
 		fd_lblWelcome.right = new FormAttachment(50, 220);
 		fd_lblWelcome.left = new FormAttachment(50, -220);
 		lblWelcome.setLayoutData(fd_lblWelcome);
 		lblWelcome.setText(MessageConstants.WELCOME_HEADER);
 
 		lblWelcomeDescription = new Label(cmpMainArea, SWT.WRAP);
 		lblWelcomeDescription.setFont(SWTResourceManager.getFont("Segoe UI",
 				12, SWT.NORMAL));
 		lblWelcomeDescription.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblWelcomeDescription = new FormData();
 		fd_lblWelcomeDescription.bottom = new FormAttachment(lblWelcome, 200);
 		fd_lblWelcomeDescription.top = new FormAttachment(lblWelcome, 14);
 		fd_lblWelcomeDescription.right = new FormAttachment(50, 200);
 		fd_lblWelcomeDescription.left = new FormAttachment(50, -200);
 		lblWelcomeDescription.setLayoutData(fd_lblWelcomeDescription);
 		lblWelcomeDescription.setText(MessageConstants.INSTRUCTIONS_LOGGED_OUT);
 
 		lblOr = new Label(cmpMainArea, SWT.CENTER);
 		lblOr.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblOr = new FormData();
 		fd_lblOr.right = new FormAttachment(50, 10);
 		fd_lblOr.left = new FormAttachment(50, -10);
 		fd_lblOr.top = new FormAttachment(lblWelcomeDescription, 6);
 		lblOr.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
 		lblOr.setLayoutData(fd_lblOr);
 		lblOr.setText("Or");
 
 		btnMainLogin = new Button(cmpMainArea, SWT.NONE);
 		btnMainLogin.setFont(SWTResourceManager.getFont("Segoe UI", 11,
 				SWT.NORMAL));
 		btnMainLogin.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				switchToLoginScreen();
 			}
 		});
 		FormData fd_btnMainLogin = new FormData();
 		fd_btnMainLogin.right = new FormAttachment(50, -50);
 		fd_btnMainLogin.left = new FormAttachment(50, -140);
 		fd_btnMainLogin.top = new FormAttachment(lblWelcomeDescription);
 		btnMainLogin.setLayoutData(fd_btnMainLogin);
 		btnMainLogin.setText("Login");
 
 		btnMainSignUp = new Button(cmpMainArea, SWT.NONE);
 		btnMainSignUp.setFont(SWTResourceManager.getFont("Segoe UI", 11,
 				SWT.NORMAL));
 		btnMainSignUp.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				switchToSignUpScreen();
 			}
 		});
 		FormData fd_btnMainSignUp = new FormData();
 		fd_btnMainSignUp.right = new FormAttachment(50, 140);
 		fd_btnMainSignUp.left = new FormAttachment(50, 50);
 		fd_btnMainSignUp.top = new FormAttachment(lblWelcomeDescription);
 		btnMainSignUp.setLayoutData(fd_btnMainSignUp);
 		btnMainSignUp.setText("Sign Up");
 
 		// Quit button
 		Button btnQuit = new Button(shell, SWT.NONE);
 		btnQuit.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				shell.dispose();
 			}
 		});
 		FormData fd_btnQuit = new FormData();
 		fd_btnQuit.left = new FormAttachment(100, -100);
 		fd_btnQuit.bottom = new FormAttachment(100, -6);
 		fd_btnQuit.right = new FormAttachment(100, -5);
 		btnQuit.setLayoutData(fd_btnQuit);
 		btnQuit.setText(Constants.QUIT);
 
 		Label lblVersion = new Label(shell, SWT.NONE);
 		FormData fd_lblVersion = new FormData();
 		fd_lblVersion.bottom = new FormAttachment(btnQuit, 0, SWT.BOTTOM);
 		fd_lblVersion.left = new FormAttachment(0, 10);
 		lblVersion.setLayoutData(fd_lblVersion);
 		lblVersion.setText(Constants.VERSION);
 
 		hideToolbar();
 
 		// show the window
 		shell.open();
 	}
 
 	public void hideToolbar()
 	{
 		this.btnConnect.setVisible(false);
 		this.btnCreate.setVisible(false);
 		this.btnPlay.setVisible(false);
 		this.cmpLogin.setVisible(false);
 		this.lblToolbarSeparator.setVisible(false);
 
 		FormData fd_cmpMainArea = new FormData();
 		fd_cmpMainArea.bottom = new FormAttachment(100, -35);
 		fd_cmpMainArea.right = new FormAttachment(100, -5);
 		fd_cmpMainArea.top = new FormAttachment(0, 5);
 		fd_cmpMainArea.left = new FormAttachment(0, 5);
 		cmpMainArea.setLayoutData(fd_cmpMainArea);
 
 		shell.layout(true);
 	}
 
 	public void showToolbar()
 	{
 		this.btnConnect.setVisible(true);
 		this.btnCreate.setVisible(true);
 		this.btnPlay.setVisible(true);
 		this.cmpLogin.setVisible(true);
 		this.lblToolbarSeparator.setVisible(true);
 
 		FormData fd_cmpMainArea = new FormData();
 		fd_cmpMainArea.bottom = new FormAttachment(100, -35);
 		fd_cmpMainArea.right = new FormAttachment(100, -5);
 		fd_cmpMainArea.top = new FormAttachment(0, 50);
 		fd_cmpMainArea.left = new FormAttachment(0, 5);
 		cmpMainArea.setLayoutData(fd_cmpMainArea);
 
 		shell.layout(true);
 	}
 
 	public void playPuzzle(Puzzle puzzle)
 	{
 		// currently only cryptograms supported
 		if (puzzle instanceof Cryptogram)
 		{
 			cmpPlayScreen.setPuzzle((Cryptogram) puzzle);
 		}
 
 		switchToPlayScreen();
 	}
 
 	private void createPressed()
 	{
 		// create a new puzzle to edit
 		cmpCreateScreen.setPuzzle(new Cryptogram());
 
 		// show the create screen and hide other screens
 		switchToCreateScreen();
 	}
 
 	private void playPressed()
 	{
 		// show the find screen and hide other screens
 		switchToFindScreen();
 	}
 
 	private void hideAllViews()
 	{
 		cmpCreateScreen.setVisible(false);
 		cmpFindScreen.setVisible(false);
 		cmpPlayScreen.setVisible(false);
 		cmpSignUpScreen.setVisible(false);
 		cmpLoginScreen.setVisible(false);
 	}
 
 	public void switchToCreateScreen()
 	{
 		hideAllViews();
 		cmpCreateScreen.setVisible(true);
 	}
 
 	public void switchToFindScreen()
 	{
 		hideAllViews();
 		cmpFindScreen.setVisible(true);
 		cmpFindScreen.onLoad();
 	}
 
 	public void switchToPlayScreen()
 	{
 		hideAllViews();
 		cmpPlayScreen.setVisible(true);
 	}
 
 	public void switchToWelcomeScreen()
 	{
 		hideAllViews();
 	}
 
 	public void switchToSignUpScreen()
 	{
 		hideAllViews();
 		cmpSignUpScreen.setVisible(true);
 	}
 
 	public void switchToLoginScreen()
 	{
 		hideAllViews();
 		cmpLoginScreen.setVisible(true);
 		cmpLoginScreen.refresh();
 	}
 
 	public void login(User user)
 	{
 		loggedInUser = user;
 		showToolbar();
 		btnMainSignUp.setVisible(false);
 		btnMainLogin.setVisible(false);
 		lblOr.setVisible(false);
 		lblLoggedInAs.setText(MessageConstants.LOGON + user.getName());
 		lblWelcomeDescription.setText(MessageConstants.INSTRUCTIONS_LOGGED_IN);
 	}
 
 	public void logout()
 	{
 		loggedInUser = null;
 		hideToolbar();
 		btnMainSignUp.setVisible(true);
 		btnMainLogin.setVisible(true);
 		lblOr.setVisible(true);
 		lblWelcomeDescription.setText(MessageConstants.INSTRUCTIONS_LOGGED_OUT);
 		switchToWelcomeScreen();
 	}
 }
