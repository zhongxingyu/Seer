 package jdbc;
 // We need to import the java.sql package to use JDBC
 import java.sql.*;
 import java.util.ArrayList;
 
 // for reading from the command line
 import java.io.*;
 
 // for the login window
 import javax.swing.*;
 
 import model.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 
 /*
  * This class implements a graphical login window and a simple text
  * interface for interacting with the branch table 
  */ 
 public class JDBCManager implements ActionListener
 {
 	// command line reader 
 	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 
 	private Connection con;
 
 	// user is allowed 3 login attempts
 	private int loginAttempts = 0;
 
 	// components of the login window
 	private JTextField usernameField;
 	private JPasswordField passwordField;
 	private JFrame mainFrame;
 
 
 	/*
 	 * constructs login window and loads JDBC driver
 	 */ 
 	public JDBCManager()
 	{
 		mainFrame = new JFrame("User Login");
 
 		JLabel usernameLabel = new JLabel("Enter username: ");
 		JLabel passwordLabel = new JLabel("Enter password: ");
 
 		usernameField = new JTextField(10);
 		passwordField = new JPasswordField(10);
 		passwordField.setEchoChar('*');
 
 		JButton loginButton = new JButton("Log In");
 
 		JPanel contentPane = new JPanel();
 		mainFrame.setContentPane(contentPane);
 
 
 		// layout components using the GridBag layout manager
 
 		GridBagLayout gb = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 
 		contentPane.setLayout(gb);
 		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
 		// place the username label 
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(10, 10, 5, 0);
 		gb.setConstraints(usernameLabel, c);
 		contentPane.add(usernameLabel);
 
 		// place the text field for the username 
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(10, 0, 5, 10);
 		gb.setConstraints(usernameField, c);
 		contentPane.add(usernameField);
 
 		// place password label
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(0, 10, 10, 0);
 		gb.setConstraints(passwordLabel, c);
 		contentPane.add(passwordLabel);
 
 		// place the password field 
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(0, 0, 10, 10);
 		gb.setConstraints(passwordField, c);
 		contentPane.add(passwordField);
 
 		// place the login button
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(5, 10, 10, 10);
 		c.anchor = GridBagConstraints.CENTER;
 		gb.setConstraints(loginButton, c);
 		contentPane.add(loginButton);
 
 		// register password field and OK button with action event handler
 		passwordField.addActionListener(this);
 		loginButton.addActionListener(this);
 
 		// anonymous inner class for closing the window
 		mainFrame.addWindowListener(new WindowAdapter() 
 		{
 			public void windowClosing(WindowEvent e) 
 			{ 
 				System.exit(0); 
 			}
 		});
 
 		// size the window to obtain a best fit for the components
 		mainFrame.pack();
 
 		// center the frame
 		Dimension d = mainFrame.getToolkit().getScreenSize();
 		Rectangle r = mainFrame.getBounds();
 		mainFrame.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );
 
 		// make the window visible
 		mainFrame.setVisible(true);
 
 		// place the cursor in the text field for the username
 		usernameField.requestFocus();
 
 		try 
 		{
 			// Load the Oracle JDBC driver
 			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			System.exit(-1);
 		}
 	}
 
 
 	/*
 	 * connects to Oracle database named ug using user supplied username and password
 	 */ 
 	private boolean connect(String username, String password)
 	{
 		String connectURL = "jdbc:oracle:thin:@dbhost.ugrad.cs.ubc.ca:1522:ug"; 
 
 		try 
 		{
 			con = DriverManager.getConnection(connectURL,username,password);
 
 			System.out.println("\nConnected to Oracle!");
 			return true;
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			return false;
 		}
 	}
 
 
 	/*
 	 * event handler for login window
 	 */ 
 	public void actionPerformed(ActionEvent e) 
 	{
 		if ( connect(usernameField.getText(), String.valueOf(passwordField.getPassword())) )
 		{
 			// if the username and password are valid, 
 			// remove the login window and display a text menu 
 			mainFrame.dispose();
 			showMenu();     
 		}
 		else
 		{
 			loginAttempts++;
 
 			if (loginAttempts >= 3)
 			{
 				mainFrame.dispose();
 				System.exit(-1);
 			}
 			else
 			{
 				// clear the password
 				passwordField.setText("");
 			}
 		}             
 
 	}
 
 
 	/*
 	 * displays simple text interface
 	 */ 
 	private void showMenu()
 	{
 		int choice;
 		boolean quit;
 
 		quit = false;
 
 		try 
 		{
 			// disable auto commit mode
 			con.setAutoCommit(false);
 
 			while (!quit)
 			{
 				System.out.print("\n\nPlease choose one of the following: \n");
 				System.out.print("1.  Insert borrower\n");
 				System.out.print("2.  Delete borrower\n");
 				System.out.print("3.  nothing\n");
 				System.out.print("4.  Show borrower\n");
 				System.out.print("5.  Quit\n>> ");
 
 				choice = Integer.parseInt(in.readLine());
 
 				System.out.println(" ");
 
 				// Test your methods here
 				switch(choice)
 				{
 				case 1:  insertHasAuthor(new HasAuthor("a","c")); break;
 				case 2:  deleteHasAuthor("a","c"); break;
 				case 3:  break;
 				case 4:  getHasAuthor(); break;
 				case 5:  quit = true; break;
 				}
 			}
 
 			con.close();
 			in.close();
 			System.out.println("\nGood Bye!\n\n");
 			System.exit(0);
 		}
 		catch (IOException e)
 		{
 			System.out.println("IOException!");
 
 			try
 			{
 				con.close();
 				System.exit(-1);
 			}
 			catch (SQLException ex)
 			{
 				System.out.println("Message: " + ex.getMessage());
 			}
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}
 	}
 
 
 
 
 	// **********************************************************
 	// ************************ INSERTS *************************
 	// **********************************************************
 	public void insertBook(Book b){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO book VALUES (?,?,?,?,?,?)");
 			ps.setString(1, b.getCallNumber());
 			ps.setString(2, b.getIsbn());
 			ps.setString(3, b.getTitle());
 			ps.setString(4, b.getMainAuthor());
 			ps.setString(5, b.getPublisher());
 			ps.setString(6, b.getYear());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	public void insertBookCopy(BookCopy bc){
 		PreparedStatement ps;
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO bookcopy VALUES (?,?,?)");
 
 			ps.setString(1, bc.getCallNumber());
 			ps.setInt(2, bc.getCopyNo());
 			ps.setString(3, bc.getStatus());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	private void insertBorrower(Borrower b)
 	{
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO borrower VALUES (?,?,?,?,?,?,?,?,?)");
 
 			ps.setString(1, b.getBid());
 			ps.setString(2, b.getPassword());
 			ps.setString(3, b.getName());
 			ps.setString(4, b.getAddress());
 			ps.setString(5, b.getPhone());
 			ps.setString(6, b.getEmailAddress());
 			ps.setString(7, b.getSinOrStNo());
 			ps.setString(8, b.getExpiryDate());
 			ps.setString(9, b.getType());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	// Note from Jimmy: parameter type should be primary key
 	public void insertBorrowerType(BorrowerType bt){
 		PreparedStatement ps;
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO borrowertype VALUES (?,?)");
 
 			ps.setString(1, bt.getType());
 			ps.setInt(2, bt.getBookTimeLimit());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	public void insertBorrowing(Borrowing b){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO borrowing VALUES (?,?,?,?,?,?)");
 			ps.setString(1, b.getBorid());
 			ps.setString(2, b.getBid());
 			ps.setString(3, b.getCallNumber());
 			ps.setInt(4, b.getCopyNo());
 			ps.setString(5, b.getOutDate());
 			ps.setString(6, b.getInDate());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 
 	public void insertFine(Fine f){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO fine VALUES (?,?,?,?,?)");
 			ps.setString(1, f.getFid());
 			ps.setInt(2, f.getAmount());
 			ps.setString(3, f.getIssueDate());
 			ps.setString(4, f.getPaidDate());
 			ps.setString(5, f.getBorid());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	// Note from Jimmy: parameter type should be primary key
 	public void insertHasAuthor(HasAuthor ha){
 		// CallNumber, Name (Both primary keys)
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO hasAuthor VALUES (?,?)");
 			ps.setString(1, ha.getCallNumber());
 			ps.setString(2, ha.getName());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 
 	}
 
 	public void insertHasSubject(HasSubject hs){
 		// CallNumber, Subject (Both primary keys)
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO hasSubject VALUES (?,?)");
 			ps.setString(1, hs.getCallNumber());
 			ps.setString(2, hs.getSubject());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 
 	}
 
 	public void insertHoldRequest(HoldRequest hr){
 		// hid, bid, CallNumber, IssueDate (hid primary keys)
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("INSERT INTO holdRequest VALUES (?,?,?,?)");
 			ps.setString(1, hr.getHoldId());
 			ps.setString(2, hr.getBorrowerId());
 			ps.setString(3, hr.getCallNumber());
 			ps.setInt(4, hr.getIssueDate());
 			ps.executeUpdate();
 
 			// commit work 
 			con.commit();
 
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 
 	}
 
 	// **********************************************************
 	// ************************ DELETES *************************
 	// ********************************************************** 
 
 	public void deleteBook(String callNumber){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM book WHERE callNumber = ?");
 			ps.setString(1, callNumber);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nbook " + callNumber + " does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}

 	public void deleteBookCopy(String callNumber){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM bookcopy WHERE callNumber = ?");
 			ps.setString(1, callNumber);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nbookcopy " + callNumber + " does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	public void deleteBorrower(String bid)
 	{
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM borrower WHERE bid = ?");
 			ps.setString(1, bid);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nborrower " + bid + " does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	public void deleteBorrowerType(String type){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM borrowertype WHERE type = ?");
 			ps.setString(1, type);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nborrowertype " + type + " does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 
 	}
 
 	public void deleteBorrowing(String borid){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM borrowing WHERE borid = ?");
 			ps.setString(1, borid);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nBorrowingID " + borid + " does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 
 	}
 
 
 	public void deleteFine(String fid){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM fine WHERE fid = ?");
 			ps.setString(1, fid);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nFineID " + fid + " does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 
 	}
 
 	public void deleteHasAuthor(String callNumber, String name){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM hasauthor WHERE callNumber = ? AND name = ?");
 			ps.setString(1, callNumber);
 			ps.setString(2, name);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nCall Number and Subject (" + callNumber + ", " + name + ") does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 
 	}
 
 	public void deleteHasSubject(String callNumber, String subject){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM hassubject WHERE callNumber = ? AND subject = ?");
 			ps.setString(1, callNumber);
 			ps.setString(2, subject);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nThe book " + callNumber + "with subject, " + subject  + " does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	public void deleteHoldRequest(String hid){
 		PreparedStatement  ps;
 
 		try
 		{
 			ps = con.prepareStatement("DELETE FROM holdrequest WHERE holdId = ?");
 			ps.setString(1, hid);
 
 			int rowCount = ps.executeUpdate();
 
 			if (rowCount == 0)
 			{
 				System.out.println("\nHold Request " + hid + " does not exist!");
 			}
 
 			con.commit();
 
 			ps.close();
 		}
 
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 
 	// **********************************************************
 	// ********************* FETCH DATA *************************
 	// **********************************************************
 
 	public ArrayList<Book> getBook(){
 		ArrayList<Book> books = new ArrayList<Book>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM book");
 
 			while(rs.next())
 			{
 
 				Book b = new Book(rs.getString("callnumber"),
 						rs.getString("isbn"),
 						rs.getString("title"),
 						rs.getString("mainauthor"),
 						rs.getString("publisher"),
 						rs.getString("year"));
 				books.add(b);
 				System.out.println("callNumber & title: " + b.getCallNumber() + " " + b.getTitle());
 
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return books;
 	}
 
 	public ArrayList<BookCopy> getBookCopy(){
 		ArrayList<BookCopy> bookcopys = new ArrayList<BookCopy>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM bookcopy");
 
 			while(rs.next())
 			{
 
 				BookCopy b = new BookCopy(rs.getString("callnumber"),
 						rs.getInt("copyNo"),
 						rs.getString("status"));
 				bookcopys.add(b);
 				System.out.println("copyno & status: " + b.getCopyNo() + " " + b.getStatus());
 
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return bookcopys;
 	}
 
 	public ArrayList<Borrower> getBorrower()
 	{
 
 		ArrayList<Borrower> borrowers = new ArrayList<Borrower>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM borrower");
 
 			String bid;
 			String password;
 			String name;
 			String address;
 			String phone;
 			String emailAddress;
 			String sinOrStNo;
 			String expiryDate;
 			String type;
 			while(rs.next())
 			{
 				bid = rs.getString("bid");
 				password = rs.getString("password");
 				name = rs.getString("name");
 				address = rs.getString("address");
 				phone = rs.getString("phone");
 				emailAddress = rs.getString("emailAddress");
 				sinOrStNo = rs.getString("sinOrStNo");
 				expiryDate = rs.getString("expiryDate");
 				type = rs.getString("type");
 				Borrower b = new Borrower(bid,password,name,address,phone,emailAddress,
 						sinOrStNo,expiryDate,type);
 				borrowers.add(b);
 				System.out.println("bid & type: " + bid + " " + type);
 
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return borrowers;
 	}
 
 
 	public ArrayList<BorrowerType> getBorrowerType(){
 		ArrayList<BorrowerType> borrowertypes = new ArrayList<BorrowerType>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM borrowertype");
 
 			while(rs.next())
 			{
 
 				BorrowerType bt = new BorrowerType(rs.getString("type"),
 						rs.getInt("bookTimeLimit"));
 				borrowertypes.add(bt);
 				System.out.println("borrowerType & bookTimeLimit: " + bt.getType() + " " + bt.getBookTimeLimit());
 
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return borrowertypes;
 	}
 
 	public ArrayList<Borrowing> getBorrowing(){
 		ArrayList<Borrowing> borrowings = new ArrayList<Borrowing>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM borrowing");
 
 			while(rs.next())
 			{
 
 				Borrowing b = new Borrowing(rs.getString("borid"),
 						rs.getString("bid"),
 						rs.getString("callNumber"), 
 						rs.getInt("copyNo"), 
 						rs.getString("outDate"), 
 						rs.getString("inDate"));
 				borrowings.add(b);
 				System.out.println("borid, callNumber, outDate & inDate: " + b.getBorid() + " " 
 				+ b.getCallNumber() + " "
 				+ b.getOutDate() + " "
 				+ b.getInDate());
 
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return borrowings;
 	}
 
 
 	public ArrayList<Fine> getFine(){
 		ArrayList<Fine> fines = new ArrayList<Fine>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM fine");
 
 			while(rs.next())
 			{
 
 				Fine f = new Fine(rs.getString("fid"),
 						rs.getInt("amount"),
 						rs.getString("issueDate"),
 						rs.getString("paidDate"),
 						rs.getString("borid"));
 				fines.add(f);
 				System.out.println("fid, amount, issueDate & paidDate: " + f.getFid() + " " 
 				+ f.getAmount() + " "
 				+ f.getIssueDate() + " "
 				+ f.getPaidDate());
 
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return fines;
 	}
 
 	public ArrayList<HasAuthor> getHasAuthor(){
 		ArrayList<HasAuthor> hasAuthor = new ArrayList<HasAuthor>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM hasauthor");
 
 			while(rs.next())
 			{
 				HasAuthor h = new HasAuthor(rs.getString("callNumber"), rs.getString("name"));
 				hasAuthor.add(h);
 				System.out.println("Call Number, & Name: " + h.getCallNumber() + " " + h.getName());
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return hasAuthor;
 	}
 
 	public ArrayList<HasSubject> getHasSubject(){
 		ArrayList<HasSubject> hasSubjects = new ArrayList<HasSubject>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM hassubject");
 
 			while(rs.next())
 			{
 				HasSubject h = new HasSubject(rs.getString("callNumber"), rs.getString("subjcet"));
 				hasSubjects.add(h);
 				System.out.println("Call Number & Subject: " + h.getCallNumber() + " " + h.getSubject());
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return hasSubjects;
 	}
 
 	public ArrayList<HoldRequest> getHoldRequest(){
 		ArrayList<HoldRequest> holdRequest = new ArrayList<HoldRequest>();
 		Statement  stmt;
 		ResultSet  rs;
 
 		try
 		{
 			stmt = con.createStatement();
 
 			rs = stmt.executeQuery("SELECT * FROM fine");
 
 			while(rs.next())
 			{
 				HoldRequest h = new HoldRequest(rs.getString("holdId"),
 						rs.getString("borrowerId"),
 						rs.getString("callNumber"),
 						rs.getInt("issueDate"));
 				
 				holdRequest.add(h);
 				System.out.println("hid, bid, Call Number & Issue Date: " + h.getHoldId() + " " 
 				+ h.getBorrowerId() + " "
 				+ h.getCallNumber() + " "
 				+ h.getIssueDate());
 
 			}
 
 			stmt.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}	
 		return holdRequest;
 	}
 
 
 	// ************************* MAIN ****************************
 	public static void main(String args[])
 	{
 		JDBCManager jdbc = new JDBCManager();
 	}
 }
