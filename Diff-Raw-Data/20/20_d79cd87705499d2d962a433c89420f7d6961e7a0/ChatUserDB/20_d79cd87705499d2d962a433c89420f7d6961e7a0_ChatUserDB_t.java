 package chat.common;
 
 import java.io.*;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.border.*;
 
 import chat.common.AeSimpleSHA1;
 
 public class ChatUserDB extends JFrame implements ActionListener {
 	// Class variables *************************************************
 	final static int USER_NAME_SIZE = 10;
 	final static int PASSWORD_SIZE = 40;
 	final static int FIRST_NAME_SIZE = 25;
 	final static int LAST_NAME_SIZE = 25;
 	final static int POSITION_SIZE = 50;
 	final static int RECORD_SIZE =
 		(USER_NAME_SIZE + PASSWORD_SIZE + FIRST_NAME_SIZE + LAST_NAME_SIZE + POSITION_SIZE);
 
 	// Instance variables **********************************************
 	private RandomAccessFile raf;
 	private Map<String, ChatUser> chatUserMap = new HashMap<String, ChatUser>();
 
 	// Text fields
 	private JTextField jtfUsername = new JTextField(USER_NAME_SIZE);
 	private JTextField jtfPassword = new JPasswordField(PASSWORD_SIZE);
 	private JTextField jtfFirstName = new JTextField(FIRST_NAME_SIZE);
 	private JTextField jtfLastName = new JTextField(LAST_NAME_SIZE);
 	private JTextField jtfPosition = new JTextField(POSITION_SIZE);
 
 	// Buttons
 	private JButton jbtAdd = new JButton("Add");
 	private JButton jbtUpdate = new JButton("Update");
 	private JButton jbtFirst = new JButton("First");
 	private JButton jbtNext = new JButton("Next");
 	private JButton jbtPrevious = new JButton("Previous");
 	private JButton jbtLast = new JButton("Last");
 
 	public ChatUserDB() {
 		// Open or create a random access file
 		try {
 			raf = new RandomAccessFile("chatuserdb.dat", "rw");
 		} catch(IOException ex) {
 			System.out.print("Error: " + ex);
 			System.exit(0);
 		}
 
 		// Panel p1 for holding labels Name, Street, and City
 		JPanel p1 = new JPanel();
 		p1.setLayout(new GridLayout(5, 1));
 		p1.add(new JLabel("User Name"));
 		p1.add(new JLabel("Password"));
 		p1.add(new JLabel("First Name"));
 		p1.add(new JLabel("Last Name"));
 		p1.add(new JLabel("Position"));
 
 		// Panel p4 for holding jtfName, jtfStreet, and p3
 		JPanel p2 = new JPanel();
 		p2.setLayout(new GridLayout(5, 1));
 		p2.add(jtfUsername);
 		p2.add(jtfPassword);
 		p2.add(jtfFirstName);
 		p2.add(jtfLastName);
 		p2.add(jtfPosition);
 		p2.add(p1);
 
 		// Place p1 and p4 into jpAddress
 		JPanel jpUserInfo = new JPanel(new BorderLayout());
 		jpUserInfo.add(p1, BorderLayout.WEST);
 		jpUserInfo.add(p2, BorderLayout.CENTER);
 
 		// Set the panel with line border
 		jpUserInfo.setBorder(new BevelBorder(BevelBorder.RAISED));
 
 		// Add buttons to a panel
 		JPanel jpButton = new JPanel();
 		jpButton.add(jbtAdd);
 		jpButton.add(jbtUpdate);
 		jpButton.add(jbtFirst);
 		jpButton.add(jbtNext);
 		jpButton.add(jbtPrevious);
 		jpButton.add(jbtLast);
 
 		// Add jpAddress and jpButton to the frame
 		getContentPane().add(jpUserInfo, BorderLayout.CENTER);
 		getContentPane().add(jpButton, BorderLayout.SOUTH);
 
 		jbtAdd.addActionListener(this);
 		jbtUpdate.addActionListener(this);
 		jbtFirst.addActionListener(this);
 		jbtNext.addActionListener(this);
 		jbtPrevious.addActionListener(this);
 		jbtLast.addActionListener(this);
 
 		// Display the first record if exists
 		try {
 			if (raf.length() > 0) readRecord(0);
 		}
 		catch (IOException ex) {
 			ex.printStackTrace();
 		}
 
 		// Setup the HashMap
 		try {
 			if(raf.length() > 0) {
 				ChatUser cu;
 				// Get the first record
 				cu = readChatUserRec(0);
 				chatUserMap.put((cu.getUserName()).trim(), cu);
 
 				long currentPosition = raf.getFilePointer();
 				long lastPosition = raf.length();
 
 				// Get records 2 to N-1
				while(currentPosition < lastPosition) {
 					cu = readChatUserRec(currentPosition);
 					chatUserMap.put((cu.getUserName()).trim(), cu);
 					currentPosition = raf.getFilePointer();
 				}
 
 				// Get the last record
 				cu = readChatUserRec(lastPosition - 2 * RECORD_SIZE);
 				chatUserMap.put((cu.getUserName()).trim(), cu);
 			}
 		}
 		catch(IOException ex) {
 			ex.printStackTrace();
 		}
		
		// printAll();
 	}
 
 	/** Write a record at the end of the file */
 	public void writeRecord() throws IOException {
 		raf.seek(raf.length());
 		FixedLengthStringIO.writeFixedLengthString(
 				jtfUsername.getText(), USER_NAME_SIZE, raf);
 
 		String pword = jtfPassword.getText();
 		try {
 			pword = AeSimpleSHA1.SHA1(pword);
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		FixedLengthStringIO.writeFixedLengthString(
 				pword, PASSWORD_SIZE, raf);
 		FixedLengthStringIO.writeFixedLengthString(
 				jtfFirstName.getText(), FIRST_NAME_SIZE, raf);
 		FixedLengthStringIO.writeFixedLengthString(
 				jtfLastName.getText(), LAST_NAME_SIZE, raf);
 		FixedLengthStringIO.writeFixedLengthString(
 				jtfPosition.getText(), POSITION_SIZE, raf);
 	}
 
 	/** Update a record in its file location */
 	public void updateRecord(long pointerLocation) throws IOException {
 		raf.seek(pointerLocation);
 		FixedLengthStringIO.writeFixedLengthString(
 				jtfUsername.getText(), USER_NAME_SIZE, raf);
 
 		String pword = jtfPassword.getText();
 		try {
 			pword = AeSimpleSHA1.SHA1(pword);
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		FixedLengthStringIO.writeFixedLengthString(
 				pword, PASSWORD_SIZE, raf);
 		FixedLengthStringIO.writeFixedLengthString(
 				jtfFirstName.getText(), FIRST_NAME_SIZE, raf);
 		FixedLengthStringIO.writeFixedLengthString(
 				jtfLastName.getText(), LAST_NAME_SIZE, raf);
 		FixedLengthStringIO.writeFixedLengthString(
 				jtfPosition.getText(), POSITION_SIZE, raf);
 	}
 
 	/** Read a record at the specified position */
 	public void readRecord(long position) throws IOException {
 		raf.seek(position);
 		String username = FixedLengthStringIO.readFixedLengthString(
 				USER_NAME_SIZE, raf);
 		String password = FixedLengthStringIO.readFixedLengthString(
 				PASSWORD_SIZE, raf);
 		String firstname = FixedLengthStringIO.readFixedLengthString(
 				FIRST_NAME_SIZE, raf);
 		String lastname = FixedLengthStringIO.readFixedLengthString(
 				LAST_NAME_SIZE, raf);
 		String positionname = FixedLengthStringIO.readFixedLengthString(
 				POSITION_SIZE, raf);
 
 		jtfUsername.setText(username);
 		jtfPassword.setText(password);
 		jtfFirstName.setText(firstname);
 		jtfLastName.setText(lastname);
 		jtfPosition.setText(positionname);
 	}
 
 	/** Read a record at the specified position and setup a ChatUser instance and return the instance
 	 * 
 	 */
 	public ChatUser readChatUserRec(long position) throws IOException {
 		raf.seek(position);
 		String username = FixedLengthStringIO.readFixedLengthString(
 				USER_NAME_SIZE, raf);
 		String password = FixedLengthStringIO.readFixedLengthString(
 				PASSWORD_SIZE, raf);
 		String firstname = FixedLengthStringIO.readFixedLengthString(
 				FIRST_NAME_SIZE, raf);
 		String lastname = FixedLengthStringIO.readFixedLengthString(
 				LAST_NAME_SIZE, raf);
 		String positionname = FixedLengthStringIO.readFixedLengthString(
 				POSITION_SIZE, raf);
 
 		return new ChatUser(username, password, firstname, lastname, positionname);
 	}
 
 	/** Handle button actions */
 	public void actionPerformed(ActionEvent e) {
 		try {
 			if (e.getSource() == jbtAdd) {
 				writeRecord();
 			}
 			else if (e.getSource() == jbtUpdate) {
 				long currentPosition = raf.getFilePointer();
 				updateRecord(currentPosition - 2 * RECORD_SIZE);
 			}
 			else if (e.getSource() == jbtFirst) {
 				if (raf.length() > 0) readRecord(0);
 			}
 			else if (e.getSource() == jbtNext) {
 				long currentPosition = raf.getFilePointer();
 				if (currentPosition < raf.length())
 					readRecord(currentPosition);
 			}
 			else if (e.getSource() == jbtPrevious) {
 				long currentPosition = raf.getFilePointer();
 				if (currentPosition -  2 * RECORD_SIZE > 0)
 					// Why 2 * 2 * RECORD_SIZE? See the follow-up remarks
 					readRecord(currentPosition - 2 * 2 * RECORD_SIZE);
 				else
 					readRecord(0);
 			}
 			else if (e.getSource() == jbtLast) {
 				long lastPosition = raf.length();
 				if (lastPosition > 0)
 					// Why 2 * RECORD_SIZE? See the follow-up remarks
 					readRecord(lastPosition - 2 * RECORD_SIZE);
 			}
 		}
 		catch(IOException ex) {
 			System.out.print("Error: " + ex);
 		}
 	}
 
 	/**
 	 * Get the chat user associated with the given username
 	 *
 	 * @param uName the username to look up
 	 * @return the chat user with the corresponding username, or null if the key does not exist
 	 */
 	public ChatUser getChatUser(String uName) {
 		if(chatUserMap.containsKey(uName))
 			return chatUserMap.get(uName);
 		else
 			return null;
 	}
 
 	/**
 	 * Print in sorted order all the users in the catalog.
 	 */
 	public void printAll() {
 		Iterator<String> i = new TreeMap<String, ChatUser>(chatUserMap).keySet().iterator();
 
 		while (i.hasNext()) {
 			String key = i.next();
			System.out.println(chatUserMap.get(key));
 		}
 	}
 
 	/**
 	 * Lists all user names in a string separated by a comma
 	 */
 	public String listForCLI() {
 		Iterator<String> i = new TreeMap<String, ChatUser>(chatUserMap).keySet().iterator();
 		String listing = "";
 
 		while (i.hasNext()) {
 			String key = i.next();
 			listing += (chatUserMap.get(key).getUserName()).trim();
 			if(i.hasNext())
 				listing += ", ";
 		}
 		return listing;
 	}
 
 	public static void main(String[] args) {
 		ChatUserDB frame = new ChatUserDB();
 		frame.pack();
 		frame.setTitle("User Accounts");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 	}
 }
