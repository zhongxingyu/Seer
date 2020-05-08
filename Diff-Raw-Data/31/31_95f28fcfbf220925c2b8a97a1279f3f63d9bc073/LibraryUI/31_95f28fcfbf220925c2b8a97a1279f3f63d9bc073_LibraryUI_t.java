 /*
  * LibraryUI.java former Comp302.java
  *
  * Created on March 6, 2003, 2:52 PM with netbeans
  *
  * Updated on March 25 2005 by Jerome Dolman
  */
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.text.Document;
 import javax.swing.text.BadLocationException;
 
 import static java.awt.event.KeyEvent.VK_E;
 import static java.awt.event.KeyEvent.VK_T;
 import static javax.swing.BoxLayout.X_AXIS;
 import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
 import static javax.swing.JOptionPane.ERROR_MESSAGE;
 import static javax.swing.JOptionPane.QUESTION_MESSAGE;
 import static javax.swing.JOptionPane.showInputDialog;
 import static javax.swing.JOptionPane.showMessageDialog;
 import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
 import static javax.swing.KeyStroke.getKeyStroke;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.sql.*;
 
 import static java.util.Calendar.DAY_OF_MONTH;
 import static java.util.Calendar.MONTH;
 import static java.util.Calendar.YEAR;
 
 public class LibraryUI extends JFrame {
 	// Actions
 	private Action exitAction;
 	private Action clearTextAction;
 	private Action borrowAction;
 	private Action returnAction;
 
 	// The main output area
 	private JTextArea outputArea;
 
 	// Return fields
 	private JTextField retISBN;
 	private JTextField retCustID;
 
 	// Borrow fields
 	private JTextField borISBN;
 	private JTextField borCustID;
 	private JComboBox borDay;
 	private JComboBox borMonth;
 	private JComboBox borYear;
 
 	// Buttons and tabbed pane - keep them for focus order
 	private JButton returnButton;
 	private JButton borrowButton;
 	private JTabbedPane tabbedPane;
 
 	// The data model
 	private LibraryModel model;
 
 	// A parent for modal dialogs
 	private JFrame dialogParent = this;
 
 	/**
 	 * Create a new LibraryUI object - showing an authentication dialog
 	 * then bringing up the main window.
 	 */
 	public LibraryUI() {
 		super("JDBC Library");
 		// Uncomment the following if you'd rather not see everything in bold
 		//UIManager.put("swing.boldMetal", Boolean.FALSE);
 
 		// Initialise everything
 		initActions();
 		initUI();
 		initFocusTraversalPolicy();
 		setSize(600, 600);
 
 		// Show Authentication dialog
 		AuthDialog ad = new AuthDialog(this, "Authentication");
 		ad.setVisible(true);
 		String userName = ad.getUserName();
 		String password = ad.getDatabasePassword();
 
 		// Create data model
		try{
			model = new LibraryModel(this, userName, password);
		} catch(SQLException e){
			JOptionPane.showMessageDialog(dialogParent, "Could not connect to the database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
 
 		// Center window on screen
 		GraphicsEnvironment ge =
 				GraphicsEnvironment.getLocalGraphicsEnvironment();
 		Point center = ge.getCenterPoint();
 		setLocation(center.x - getSize().width/2,
 				center.y - getSize().height/2);
 
 		// Show ourselves
 		setVisible(true);
 	}
 
 	private void initActions() {
 		exitAction = new ExitAction();
 		clearTextAction = new ClearTextAction();
 		borrowAction = new BorrowAction();
 		returnAction = new ReturnAction();
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent evt) {
 				doExit();
 			}
 		});
 	}
 
 	private void initUI() {
 		// Create tabbed pane with commands in it
 		tabbedPane = new JTabbedPane();
 		getContentPane().add(tabbedPane, BorderLayout.NORTH);
 
 		tabbedPane.addTab("Book", null, createBookPane(),
 				"View book information");
 		tabbedPane.addTab("Author", null, createAuthorPane(),
 				"View author information");
 		tabbedPane.addTab("Customer", null, createCustomerPane(),
 				"View customer information");
 		tabbedPane.addTab("Borrow Book", null, createBorrowPane(),
 				"Borrow books for a customer");
 		tabbedPane.addTab("Return Book", null, createReturnPane(),
 				"Return books for a customer");
 
 		// Create output area with scrollpane
 		outputArea = new JTextArea();
 		outputArea.setEditable(false);
 		outputArea.setFocusable(false);
 		outputArea.setTabSize(2);
 		JScrollPane sp = new JScrollPane(outputArea);
 		sp.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
 
 		getContentPane().add(sp, BorderLayout.CENTER);
 
 		// Create menus
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic('F');
 
 		JMenuItem clearTextMenuItem = new JMenuItem(clearTextAction);
 		JMenuItem exitMenuItem = new JMenuItem(exitAction);
 
 		fileMenu.add(clearTextMenuItem);
 		fileMenu.addSeparator();
 		fileMenu.add(exitMenuItem);
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(fileMenu);
 		setJMenuBar(menuBar);
 
 		// Pack it all
 		pack();
 	}
 
 	// By default the GridBagLayout stuffs up tab ordering for the
 	// borrow book and return book panes, so I need this to ensure it's all
 	// the right way round.
 	private void initFocusTraversalPolicy() {
 		Container nearestRoot =
 				(isFocusCycleRoot()) ? this : getFocusCycleRootAncestor();
 		final FocusTraversalPolicy defaultPolicy =
 				nearestRoot.getFocusTraversalPolicy();
 
 		MapFocusTraversalPolicy mine =
 				new MapFocusTraversalPolicy(defaultPolicy, tabbedPane);
 		mine.putAfter(retISBN, retCustID);
 		mine.putAfter(retCustID, returnButton);
 		mine.putAfter(returnButton, tabbedPane);
 		mine.putAfter(borISBN, borCustID);
 		mine.putAfter(borCustID, borDay);
 		mine.putAfter(borDay, borMonth);
 		mine.putAfter(borMonth, borYear);
 		mine.putAfter(borYear, borrowButton);
 		mine.putAfter(borrowButton, tabbedPane);
 
 		mine.putBefore(retCustID, retISBN);
 		mine.putBefore(returnButton, retCustID);
 		mine.putBefore(borCustID, borISBN);
 		mine.putBefore(borDay, borCustID);
 		mine.putBefore(borMonth, borDay);
 		mine.putBefore(borYear, borMonth);
 		mine.putBefore(borrowButton, borYear);
 
 		mine.putTabBefore("Borrow Book", borrowButton);
 		mine.putTabBefore("Return Book", returnButton);
 
 		nearestRoot.setFocusTraversalPolicy(mine);
 	}
 
 	private Container createBookPane() {
 		// Create buttons
 		JButton bookLookup = new JButton(new BookLookupAction());
 		JButton showCat = new JButton(new ShowCatalogueAction());
 		JButton showLoanedBook = new JButton(new ShowLoanedBooksAction());
 		JButton deleteBook = new JButton(new DeleteBookAction());
 
 		// Create panel
 		Box pane = new Box(X_AXIS);
 		pane.add(Box.createHorizontalGlue());
 		pane.add(bookLookup);
 		pane.add(Box.createHorizontalStrut(5));
 		pane.add(showCat);
 		pane.add(Box.createHorizontalStrut(5));
 		pane.add(showLoanedBook);
 		pane.add(Box.createHorizontalStrut(5));
 		pane.add(deleteBook);
 		pane.add(Box.createHorizontalGlue());
 		return pane;
 	}
 
 	private Container createAuthorPane() {
 		// Create buttons
 		JButton showAuthor = new JButton(new ShowAuthorAction());
 		JButton showAllAuth = new JButton(new ShowAllAuthorsAction());
 		JButton deleteAuthor = new JButton(new DeleteAuthorAction());
 		// Create panel
 		Box pane = new Box(X_AXIS);
 		pane.add(Box.createHorizontalGlue());
 		pane.add(showAuthor);
 		pane.add(Box.createHorizontalStrut(5));
 		pane.add(showAllAuth);
 		pane.add(Box.createHorizontalStrut(5));
 		pane.add(deleteAuthor);
 		pane.add(Box.createHorizontalGlue());
 
 		return pane;
 	}
 
 	private Container createCustomerPane() {
 		// Create buttons
 		JButton showCus = new JButton(new ShowCustomerAction());
 		JButton showAllCus = new JButton(new ShowAllCustomersAction());
 		JButton deleteCus = new JButton(new DeleteCustomerAction());
 
 		// Create panel
 		Box pane = new Box(X_AXIS);
 		pane.add(Box.createHorizontalGlue());
 		pane.add(showCus);
 		pane.add(Box.createHorizontalStrut(5));
 		pane.add(showAllCus);
 		pane.add(Box.createHorizontalStrut(5));
 		pane.add(deleteCus);
 		pane.add(Box.createHorizontalGlue());
 
 		return pane;
 	}
 
 	private Container createBorrowPane() {
 		// Initialise date combo boxes
 		borDay = new JComboBox();
 		borMonth = new JComboBox();
 		borYear = new JComboBox();
 		String[] days = new String[31];
 		for (int i = 0; i < 31; i++) days[i] = String.valueOf(i+1);
 		String[] months = { "January", "February", "March", "April",
 				"May", "June", "July", "August",
 				"September", "October", "November", "December" };
 		String[] years = { "2005", "2006", "2007", "2008", "2009", "2010", "2011","2012","2013","2014"};
 		borDay.setModel(new DefaultComboBoxModel(days));
 		borMonth.setModel(new DefaultComboBoxModel(months));
 		borYear.setModel(new DefaultComboBoxModel(years));
 		Calendar today = Calendar.getInstance();
 		borDay.setSelectedIndex(today.get(DAY_OF_MONTH)-1);
 		borMonth.setSelectedIndex(today.get(MONTH));
 		borYear.setSelectedIndex(today.get(YEAR) - 2005);
 
 		// Create borrow button
 		borrowButton = new JButton(borrowAction);
 
 		// Create text fields
 		borISBN = new JTextField(15);
 		borCustID = new JTextField(15);
 
 		// Create panel and layout
 		JPanel pane = new JPanel();
 		pane.setOpaque(false);
 		GridBagLayout gb = new GridBagLayout();
 		pane.setLayout(gb);
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(1, 5, 1, 5);
 
 		// Fill panel
 		c.anchor = GridBagConstraints.EAST;
 		addToGridBag(gb, c, pane, new JLabel("ISBN:"),        0, 0, 1, 1);
 		addToGridBag(gb, c, pane, new JLabel("Customer ID:"), 0, 1, 1, 1);
 		addToGridBag(gb, c, pane, new JLabel("Due Date:"),    0, 2, 1, 1);
 
 		c.anchor = GridBagConstraints.WEST;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		addToGridBag(gb, c, pane, borISBN,  1, 0, 3, 1);
 		addToGridBag(gb, c, pane, borCustID, 1, 1, 3, 1);
 
 		c.fill = GridBagConstraints.NONE;
 		addToGridBag(gb, c, pane, borDay,   1, 2, 1, 1);
 		addToGridBag(gb, c, pane, borMonth, 2, 2, 1, 1);
 		addToGridBag(gb, c, pane, borYear,  3, 2, 1, 1);
 
 		addToGridBag(gb, c, pane, borrowButton, 4, 0, 1, 3);
 
 		// Set up VK_ENTER triggering the borrow button in this panel
 		InputMap input = pane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 		input.put(getKeyStroke("ENTER"), "borrowAction");
 		pane.getActionMap().put("borrowAction", borrowAction);
 
 		return pane;
 	}
 
 	private Container createReturnPane() {
 		// Create return button
 		returnButton = new JButton(returnAction);
 
 		// Create text fields
 		retISBN = new JTextField(15);
 		retCustID = new JTextField(15);
 
 		// Create panel and layout
 		JPanel pane = new JPanel();
 		pane.setOpaque(false);
 		GridBagLayout gb = new GridBagLayout();
 		pane.setLayout(gb);
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(1, 5, 1, 5);
 
 		// Fill panel
 		c.anchor = GridBagConstraints.EAST;
 		addToGridBag(gb, c, pane, new JLabel("ISBN:"),        0, 0, 1, 1);
 		addToGridBag(gb, c, pane, new JLabel("Customer ID:"), 0, 1, 1, 1);
 
 		c.anchor = GridBagConstraints.WEST;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		addToGridBag(gb, c, pane, retISBN,  1, 0, 3, 1);
 		addToGridBag(gb, c, pane, retCustID, 1, 1, 3, 1);
 
 		c.fill = GridBagConstraints.NONE;
 		addToGridBag(gb, c, pane, returnButton, 4, 0, 1, 3);
 
 		// Set up VK_ENTER triggering the return button in this panel
 		InputMap input = pane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 		input.put(getKeyStroke("ENTER"), "returnAction");
 		pane.getActionMap().put("returnAction", returnAction);
 
 		return pane;
 	}
 
 	private void addToGridBag(GridBagLayout gb, GridBagConstraints c,
 			Container cont, JComponent item,
 			int x, int y, int w, int h) {
 		c.gridx = x;
 		c.gridy = y;
 		c.gridwidth = w;
 		c.gridheight = h;
 		gb.setConstraints(item, c);
 		cont.add(item);
 	}
 
 	private class ExitAction extends AbstractAction {
 		public ExitAction() {
 			super("Exit");
 			putValue(MNEMONIC_KEY, VK_E);
 			putValue(ACCELERATOR_KEY, getKeyStroke("ctrl Q"));
 		}
 		public void actionPerformed(ActionEvent evt) {
 			doExit();
 		}
 	}
 
 	private class ClearTextAction extends AbstractAction {
 		public ClearTextAction() {
 			super("Clear Text");
 			putValue(MNEMONIC_KEY, VK_T);
 			putValue(ACCELERATOR_KEY, getKeyStroke("ctrl T"));
 		}
 		public void actionPerformed(ActionEvent evt) {
 			Document document = outputArea.getDocument();
 			try {
 				document.remove(0, document.getLength());
 			} catch(BadLocationException ble) {
 			}
 		}
 	}
 
 	/**
 	 * An Action that catches any exception thrown in the doAction method.
 	 */
 	private abstract class CatchAction extends AbstractAction {
 		public CatchAction(String name) {
 			super(name);
 		}
 		public void actionPerformed(ActionEvent e) {
 			try {
 				doAction();
 			} catch(Exception ex) {  showExceptionDialog(ex);  }
 		}
 		/** Subclasses implement this for their behaviour */
 		protected abstract void doAction();
 	}
 
 	private class ReturnAction extends CatchAction {
 		public ReturnAction() {
 			super("Return");
 		}
 		public void doAction() {
 			try {
 				int isbn = Integer.parseInt(retISBN.getText());
 				int cusID = Integer.parseInt(retCustID.getText());
 				appendOutput(model.returnBook(isbn, cusID));
 			} catch ( NumberFormatException nfe ) {
 				showMessageDialog(dialogParent, "The values entered for ISBN or customer ID do not have number format. Please try again.",
 						"Format Error", ERROR_MESSAGE);
 			}
 		}
 	}
 
 	private class BorrowAction extends CatchAction {
 		public BorrowAction() {
 			super("Borrow");
 		}
 		public void doAction() {
 			try {
 				int isbn = Integer.parseInt(borISBN.getText());
 				int cusID = Integer.parseInt(borCustID.getText());
 				int day = Integer.parseInt((String)borDay.getSelectedItem());
 				int year = Integer.parseInt((String)borYear.getSelectedItem());
 				int month = borMonth.getSelectedIndex();
 				appendOutput(model.borrowBook(isbn, cusID, day, month, year));
 			} catch ( NumberFormatException nfe ) {
 				showMessageDialog(dialogParent,
 						"The values entered for ISBN or customer ID do not have a numeric format. Please try again.",
 						"Format Error", ERROR_MESSAGE);
 			}
 		}
 	}
 
 	// Convenience method for the LookupAction constructor
 	private static boolean isVowel(char c) {
 		switch(Character.toLowerCase(c)) {
 		case 'a': case 'e': case 'i': case 'o': case 'u':
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	/**
 	 * A base class for lookup-based actions: prompt for a number with
 	 * various strings in the right places and call the doLookup method.
 	 */
 	private abstract class LookupAction extends CatchAction {
 		String title, itemDesc, a;
 		public LookupAction(String name, String itemDesc) {
 			this(name, itemDesc, isVowel(itemDesc.charAt(0)) ? "an" : "a");
 		}
 		public LookupAction(String name, String itemDesc, String a) {
 			super(name);
 			title = name;
 			this.itemDesc = itemDesc;
 			this.a = a;
 		}
 		protected void doAction() {
 			try {
 				Object in = showInputDialog(dialogParent,
 						"Enter " + a + " " + itemDesc,
 						title,
 						QUESTION_MESSAGE,
 						null, null, null);
 				if (in == null)
 					return;
 				int item = Integer.parseInt((String)in);
 				doLookup(item);
 			} catch ( NumberFormatException nfe ) {
 				String message =
 						"The " + itemDesc + " entered does not have a numeric " +
 								"format.  Please try again.";
 				showMessageDialog(dialogParent, message,
 						"Format Error", ERROR_MESSAGE);
 			}
 		}
 		/** Subclasses implement this for their behaviour */
 		protected abstract void doLookup(int id);
 	}
 
 	private class ShowCustomerAction extends LookupAction {
 		public ShowCustomerAction() {
 			super("Show Customer", "customer ID");
 		}
 		protected void doLookup(int customerID) {
 			appendOutput(model.showCustomer(customerID));
 		}
 	}
 
 	private class ShowAuthorAction extends LookupAction {
 		public ShowAuthorAction() {
 			super("Show Author", "author ID");
 		}
 		protected void doLookup(int authorID) {
 			appendOutput(model.showAuthor(authorID));
 		}
 	}
 
 	private class BookLookupAction extends LookupAction {
 		public BookLookupAction() {
 			super("Book Lookup", "ISBN");
 		}
 		protected void doLookup(int isbn) {
 			appendOutput(model.bookLookup(isbn));
 		}
 	}
 
 	private class DeleteCustomerAction extends LookupAction {
 		public DeleteCustomerAction() {
 			super("Delete Customer", "customer ID");
 		}
 		protected void doLookup(int customerID) {
 			appendOutput(model.deleteCus(customerID));
 		}
 	}
 
 	private class DeleteAuthorAction extends LookupAction {
 		public DeleteAuthorAction() {
 			super("Delete Author", "author ID");
 		}
 		protected void doLookup(int authorID) {
 			appendOutput(model.deleteAuthor(authorID));
 		}
 	}
 	private class DeleteBookAction extends LookupAction {
 		public DeleteBookAction() {
 			super("Delete Book", "ISBN");
 		}
 		protected void doLookup(int isbn) {
 			appendOutput(model.deleteBook(isbn));
 		}
 	}
 
 	private class ShowAllCustomersAction extends CatchAction {
 		public ShowAllCustomersAction() {
 			super("Show All Customers");
 		}
 		protected void doAction() {
 			appendOutput(model.showAllCustomers());
 		}
 	}
 
 	private class ShowAllAuthorsAction extends CatchAction {
 		public ShowAllAuthorsAction() {
 			super("Show All Authors");
 		}
 		protected void doAction() {
 			appendOutput(model.showAllAuthors());
 		}
 	}
 
 	private class ShowCatalogueAction extends CatchAction {
 		public ShowCatalogueAction() {
 			super("Show Catalogue");
 		}
 		protected void doAction() {
 			appendOutput(model.showCatalogue());
 		}
 	}
 
 	private class ShowLoanedBooksAction extends CatchAction {
 		public ShowLoanedBooksAction() {
 			super("Show Loaned Books");
 		}
 		protected void doAction() {
 			appendOutput(model.showLoanedBooks());
 		}
 	}
 
 	private void appendOutput(String str) {
 		if (str != null && !str.equals(""))
 			outputArea.append(str + "\n\n");
 		outputArea.setCaretPosition(outputArea.getDocument().getLength());
 	}
 
 	private void showExceptionDialog(Exception e) {
 		showMessageDialog(this,
 				e.toString(),
 				"Error performing action",
 				ERROR_MESSAGE);
 	}
 
 	/** Exit the Application */
 	private void doExit() {
 		model.closeDBConnection();
 		System.exit(0);
 	}
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String args[]) {
 		// Build the UI on the Swing thread
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				new LibraryUI();
 			}
 		});
 	}
 }
 
 class AuthDialog extends JDialog {
 	private boolean okButtonClicked = false;
 
 	String introText = "Please enter your username and database password";
 	private JPanel dialogPanel = new JPanel();
 	private JPanel labelPanel = new JPanel();
 	private JPanel inputPanel = new JPanel();
 	private JTextField usernameTf = new JTextField(20);
 	private JPasswordField passwdTf =  new JPasswordField(20);
 
 	public AuthDialog() {
 		this(null, "Authentication", false);
 	}
 
 	public AuthDialog(JFrame parent) {
 		this(parent, "Authentication", true);
 	}
 
 	public AuthDialog(JFrame parent, String title) {
 		this(parent, title, true);
 	}
 
 	public AuthDialog(final JFrame parent, String title, boolean modal) {
 		super(parent, title, modal);
 
 		// Set up close behaviour
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				if (!okButtonClicked)
 					System.exit(0);
 			}
 		});
 
 		// Set up OK button behaviour
 		JButton okButton = new JButton("OK");
 		okButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (getUserName().length() == 0) {
 					showMessageDialog(AuthDialog.this,
 							"Please enter a username",
 							"Format Error",
 							ERROR_MESSAGE);
 					return;
 				}
 				if (getDatabasePassword().length() == 0) {
 					showMessageDialog(AuthDialog.this,
 							"Please enter a password",
 							"Format Error",
 							ERROR_MESSAGE);
 					return;
 				}
 				okButtonClicked = true;
 				setVisible(false);
 			}
 		});
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 
 		// Set up dialog contents
 		labelPanel.setBorder(BorderFactory.createEmptyBorder(20,20,5,5));
 		inputPanel.setBorder(BorderFactory.createEmptyBorder(20,5,5,20));
 
 		labelPanel.setLayout(new GridLayout(2, 1));
 		labelPanel.add(new JLabel("User Name: "));
 		labelPanel.add(new JLabel("Password:"));
 		inputPanel.setLayout(new GridLayout(2, 1));
 		inputPanel.add(usernameTf);
 		inputPanel.add(passwdTf);
 
 		Box buttonPane = new Box(X_AXIS);
 		buttonPane.add(Box.createHorizontalGlue());
 		buttonPane.add(okButton);
 		buttonPane.add(Box.createHorizontalStrut(5));
 		buttonPane.add(cancelButton);
 		buttonPane.add(Box.createHorizontalGlue());
 		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
 
 		JLabel introLabel = new JLabel(introText);
 		introLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 		getContentPane().add(introLabel, BorderLayout.NORTH);
 		getContentPane().add(labelPanel, BorderLayout.WEST);
 		getContentPane().add(inputPanel, BorderLayout.CENTER);
 		getContentPane().add(buttonPane, BorderLayout.SOUTH);
 
 		// Ensure the enter key triggers the OK button
 		getRootPane().setDefaultButton(okButton);
 
 		// And that the escape key exits
 		InputMap inputMap =
 				getRootPane().getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 		ActionMap actionMap = getRootPane().getActionMap();
 		inputMap.put(getKeyStroke("ESCAPE"), "exitAction");
 		actionMap.put("exitAction", new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 
 		// Pack it all
 		pack();
 
 		// Center on the screen
 		setLocationRelativeTo(null);
 	}
 
 	public String getUserName() {
 		return usernameTf.getText();
 	}
 
 	public String getDatabasePassword() {
 		return new String(passwdTf.getPassword());
 	}
 }
 
 /**
  * A custom focus traversal policy that allows components to be set
  * and knows about tabbed panes.
  */
 class MapFocusTraversalPolicy extends FocusTraversalPolicy {
 	private FocusTraversalPolicy defaultPolicy;
 	private JTabbedPane tabbedPane;
 
 	private Map<Component,Component> before =
 			new HashMap<Component,Component>();
 	private Map<String,Component> tabBefore =
 			new HashMap<String,Component>();
 	private Map<Component,Component> after =
 			new HashMap<Component,Component>();
 
 	public MapFocusTraversalPolicy(FocusTraversalPolicy def,
 			JTabbedPane tab) {
 		defaultPolicy = def;
 		tabbedPane = tab;
 	}
 
 	public void putAfter(Component a, Component b) {
 		after.put(a, b);
 	}
 
 	public void putBefore(Component a, Component b) {
 		before.put(a, b);
 	}
 
 	public void putTabBefore(String a, Component b) {
 		tabBefore.put(a, b);
 	}
 
 	public Component getComponentAfter(Container cont,
 			Component comp) {
 		Component next = after.get(comp);
 		if (next != null)
 			return next;
 		return defaultPolicy.getComponentAfter(cont, comp);
 	}
 	public Component getComponentBefore(Container cont,
 			Component comp) {
 		if (comp == tabbedPane) {
 			String tabTitle =
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
 			Component prev = tabBefore.get(tabTitle);
 			if (prev != null)
 				return prev;
 		}
 		Component prev = before.get(comp);
 		if (prev != null)
 			return prev;
 		return defaultPolicy.getComponentBefore(cont, comp);
 	}
 	public Component getDefaultComponent(Container root) {
 		return defaultPolicy.getDefaultComponent(root);
 	}
 
 	public Component getLastComponent(Container root) {
 		return defaultPolicy.getLastComponent(root);
 	}
 
 	public Component getFirstComponent(Container root) {
 		return defaultPolicy.getFirstComponent(root);
 	}
 }
