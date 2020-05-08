 package edu.umw.cpsc330.twitterclone;
 
import java.awt.List;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
import java.util.Date;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.graphics.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 
import swing2swt.layout.FlowLayout;

 public class DefaultWindow {
 
     protected Shell shell;
     private Text username;
     private Text password;
     private Composite buttonBar;
     private Button registerButton;
     private Button searchButton;
     private Button loginButton;
     private Label error;
     private Table rightPanel;
     
     private PostDatabase posts;
     private UserDatabase users;
     private TableColumn authorColumn;
     private TableColumn dateColumn;
     private TableColumn postColumn;
     
     /**
      * Launch the application.
      * @param args
      */
     public static void main(String[] args) {
 	try {
 	    DefaultWindow window = new DefaultWindow();
 	    window.open();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     /**
      * Open the window.
      */
     public void open() {
 	Display display = Display.getDefault();
 	createContents();
 	
 	// populate the table
 	try {
 	    posts = new PostDatabase();
	    java.util.List<Post> postList = posts.getAllPublic();
 	    for (int i = 0; i < postList.size(); i ++) {
 		Post p = postList.get(i);
 		TableItem item = new TableItem(rightPanel, SWT.NONE);
 		
 		// convert date to a readable format
 		DateFormat df = new SimpleDateFormat("EEE MMM d, HH:mm:ss");
 		String date = df.format(p.date.getTime());
 		
 		item.setText(new String[] { p.author, date, p.getContent() });
 	    }
 	} catch (Exception e) {
 	    if (e.getMessage() != null)
 		error.setText(e.getMessage());
 	    else
 		e.printStackTrace();
 	}
 	
 	shell.open();
 	shell.layout();
 	while (!shell.isDisposed()) {
 	    if (!display.readAndDispatch()) {
 		display.sleep();
 	    }
 	}
     }
 
     /**
      * Create contents of the window.
      */
     protected void createContents() {
 	shell = new Shell();
 	shell.setMinimumSize(new Point(480, 200));
 	shell.setSize(720, 320);
 	shell.setImage(new Image(shell.getDisplay(), "C:\\Users\\Alex Lindeman\\Desktop\\icon.png"));
 	shell.setText("Public timeline - flaming-octo-dangerzone");
 	GridLayout gl_shell = new GridLayout(2, false);
 	gl_shell.verticalSpacing = 0;
 	gl_shell.marginWidth = 0;
 	gl_shell.marginHeight = 0;
 	shell.setLayout(gl_shell);
 	
 	Composite leftPanel = new Composite(shell, SWT.NONE);
 	GridData gd_leftPanel = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
 	gd_leftPanel.widthHint = 200;
 	leftPanel.setLayoutData(gd_leftPanel);
 	RowLayout rl_leftPanel = new RowLayout(SWT.VERTICAL);
 	rl_leftPanel.wrap = false;
 	rl_leftPanel.center = true;
 	rl_leftPanel.marginHeight = 5;
 	rl_leftPanel.marginWidth = 10;
 	rl_leftPanel.spacing = 5;
 	rl_leftPanel.marginTop = 0;
 	rl_leftPanel.marginRight = 0;
 	rl_leftPanel.marginLeft = 0;
 	rl_leftPanel.marginBottom = 0;
 	rl_leftPanel.fill = true;
 	leftPanel.setLayout(rl_leftPanel);
 	
 	Label hero = new Label(leftPanel, SWT.NONE);
 	hero.setLayoutData(new RowData(180, SWT.DEFAULT));
 	hero.setFont(new Font(shell.getDisplay(), "Segoe UI Semibold", 16, SWT.NORMAL));
 	hero.setText("Login");
 	
 	Font inputFont = new Font(shell.getDisplay(), "Segoe UI", 12, SWT.NORMAL);
 	
 	username = new Text(leftPanel, SWT.BORDER);
 	username.setFont(inputFont);
 	
 	password = new Text(leftPanel, SWT.BORDER | SWT.PASSWORD);
 	password.setFont(inputFont);
 	
 	buttonBar = new Composite(leftPanel, SWT.NONE);
 	RowLayout rl_buttonBar = new RowLayout(SWT.HORIZONTAL);
 	rl_buttonBar.justify = true;
 	buttonBar.setLayout(rl_buttonBar);
 	
 	loginButton = new Button(buttonBar, SWT.NONE);
 	loginButton.setText("Login");
 	shell.setDefaultButton(loginButton);
 	
 	registerButton = new Button(buttonBar, SWT.NONE);
 	registerButton.setText("Register");
 	
 	searchButton = new Button(buttonBar, SWT.NONE);
 	searchButton.setText("Search");
 	
 	error = new Label(leftPanel, SWT.NONE);
 	error.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
 	
 	rightPanel = new Table(shell, SWT.FULL_SELECTION | SWT.VIRTUAL);
 	rightPanel.setHeaderVisible(true);
 	rightPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 	rightPanel.setLinesVisible(true);
 	
 	authorColumn = new TableColumn(rightPanel, SWT.NONE);
 	authorColumn.setText("Author");
 	
 	dateColumn = new TableColumn(rightPanel, SWT.NONE);
 	dateColumn.setText("Date");
 	
 	postColumn = new TableColumn(rightPanel, SWT.NONE);
 	postColumn.setText("Post");
 	
 	// resize columns when window is resized
 	rightPanel.addControlListener(new ControlAdapter() {
 	    public void controlResized(ControlEvent e) {
 		Rectangle area = rightPanel.getClientArea();
 		int width = area.width;
 		Point oldSize = rightPanel.getSize();
 		if (oldSize.x > area.width) {
 		    // table has gotten smaller
 		    authorColumn.setWidth(width / 6);
 		    dateColumn.setWidth(width / 4);
 		    postColumn.setWidth(width - authorColumn.getWidth() - dateColumn.getWidth());
 		} else {
 		    // table has gotten bigger
 		    authorColumn.setWidth(width / 6);
 		    dateColumn.setWidth(width / 4);
 		    postColumn.setWidth(width - authorColumn.getWidth() - dateColumn.getWidth());
 		}
 	    }
 	});
     }
 }
