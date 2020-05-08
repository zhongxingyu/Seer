 
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
 
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 
 public class LibrarianTransactions {
 	
 	/*************************************************************************************
 	*   LIBRARIAN TRANSACTIONS:
 	*   	- add books, add book copy, generate checked out books report, generate popular books
 	*************************************************************************************/
 	public void addBook() {
 
 		// User inputs: isbn, title, mainAuthors, otherAuthors,
 		// publisher, year, subjects
 		JTextField isbnField = new JTextField(15);
 		JTextField titleField = new JTextField(20);
 		JTextField mainAuthorField = new JTextField(20);
 		JTextField otherAuthorsField = new JTextField(90);
 		JTextField publisherField = new JTextField(10);
 		JTextField yearField = new JTextField(10);
 		JTextField subjectsField = new JTextField(90);
 
 		Vector<String> list = getSubjectList();
 		JList subjectsList = new JList(list);
 		subjectsList.setVisibleRowCount(5);
 		subjectsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		subjectsList.setLayoutOrientation(JList.VERTICAL_WRAP);
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("isbn:"), isbnField, 
 				new JLabel("Title:"), titleField,
 				new JLabel("Main author:"), mainAuthorField,
 				new JLabel("Other authors:"), otherAuthorsField,
 				new JLabel("Publisher:"), publisherField, 
 				new JLabel("Year:"), yearField, 
 				new JLabel("Subjects:"), subjectsList,
 				new JLabel("Add new subjects:"), subjectsField 
 				};
 
 		int result = JOptionPane.showConfirmDialog(null, inputs,
 				"Enter book info", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 
 		if (result == JOptionPane.OK_OPTION) {
 			String isbn = isbnField.getText();
 			String title = titleField.getText();
 			String mainAuthor = mainAuthorField.getText();
 			String[] otherAuthors = otherAuthorsField.getText().split(",");
 			String publisher = publisherField.getText();
 			int year = Integer.parseInt(yearField.getText());
 			String callNumber = randomString() + " " + randomString() + " "
 					+ Integer.toString(year);
 			
 			// Retrieve list of subjects for book
 			Vector<String> subjects = new Vector<String>(5,1);
 			for (int i = 0; i < subjectsList.getSelectedValues().length; i++) {
 				subjects.addElement(subjectsList.getSelectedValues()[i].toString());
 			}
 			String[] newSubjects = subjectsField.getText().split(",");
 			Collections.addAll(subjects, newSubjects);
 			try {
 
 				// Insert new book into Book table
 				PreparedStatement ps = Library.con
 						.prepareStatement("INSERT INTO book (callNumber, isbn, title, mainAuthor, publisher, year) VALUES (?,?,?,?,?,?)");
 				ps.setString(1, callNumber);
 				ps.setString(2, isbn);
 				ps.setString(3, title);
 				ps.setString(4, mainAuthor);
 				ps.setString(5, publisher);
 				ps.setInt(6, year);
 
 				ps.executeUpdate();
 				Library.con.commit();
 
 				// Show Book table
 				Statement stmt = Library.con.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT * FROM Book");
 				LibraryGUI.showTable(rs, "addBookButton");
 
 				rs.close();
 				ps.close();
 
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			try {
 				for (int i = 0; i < otherAuthors.length; i++) {
 
 					// Add other entered authors into hasAuthor table
 					PreparedStatement ps = Library.con
 							.prepareStatement("INSERT INTO hasAuthor VALUES (?,?)");
 					ps.setString(1, callNumber);
 					ps.setString(2, otherAuthors[i]);
 
 					ps.executeUpdate();
 					Library.con.commit();
 					ps.close();
 				}
 
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			try {
 				for (int i = 0; i < subjects.size(); i++) {
 
 					// Add subjects into hasSubjects table
 					PreparedStatement ps = Library.con
 							.prepareStatement("INSERT INTO hasSubject VALUES (?,?)");
 					ps.setString(1, callNumber);
 					ps.setString(2, subjects.get(i));
 
 					ps.executeUpdate();
 					Library.con.commit();
 					ps.close();
 				}
 
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 	
 	public void addBookCopy() {
 
 		// User inputs: callNumber
 		JTextField callNumberField = new JTextField(15);
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("Call number:"), callNumberField,
 
 		};
 		
 		int result = JOptionPane.showConfirmDialog(null, inputs,
 				"Enter book copy info", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 
 		if (result == JOptionPane.OK_OPTION) {
 			String callNumber = callNumberField.getText();
 			try {
 				PreparedStatement ps3 = Library.con
 						.prepareStatement("select * from book where callNumber = ?");
 				ps3.setString(1, callNumber);
 				ps3.executeQuery();
 
 				// Add copy only if callNumber is in the database
 				if (ps3.getResultSet().next() == true) {
 
 					// Get number of copies already in the database
 					PreparedStatement ps = Library.con
 							.prepareStatement("select * from bookcopy where callNumber = ?");
 
 					ps.setString(1, callNumber);
 					ps.executeQuery();
 					ResultSet rs = ps.getResultSet();
 
 					List<String> copies = new ArrayList<String>();
 
 					while (rs.next()) {
 						copies.add(rs.getString("callNumber"));
 					}
 
 					// copyNo for the new copy
 					String copyNum = Integer.toString(copies.size() + 1);
 
 					// Add copy to BookCopy table
 					PreparedStatement ps2 = Library.con
 							.prepareStatement("insert into BookCopy (callNumber, copyNo, status) VALUES (?,?,?)");
 					ps2.setString(1, callNumber);
 					ps2.setString(2, copyNum);
 					ps2.setString(3, "in");
 
 					ps2.executeUpdate();
 					Library.con.commit();
 					rs.close();
 					ps.close();
 					ps2.close();
 					ps3.close();
 
 				} else {
 					// Error if callNumber could not be found in the database
 					new ErrorMessage(callNumber + " not found in the database");
 				}
 
 				// Show BookCopy table
 				Statement stmt = Library.con.createStatement();
 				ResultSet rs2 = stmt.executeQuery("SELECT * FROM BookCopy");
 				LibraryGUI.showTable(rs2, "addBookCopyButton");
 
 				rs2.close();
 
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void generateCheckedOutBooksReport() {
 		
 		// User inputs: bid, password
 		Vector<String> subjectList = getSubjectList();
 		subjectList.insertElementAt("All subjects", 0);
 		JComboBox subjectCombo = new JComboBox(subjectList);
 		
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("Subject: "), subjectCombo,
 
 		};
 		int result = JOptionPane.showConfirmDialog(null, inputs,
 				"Checked out Books Report", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 		
 		String subject = "";
 
 		if (subjectCombo.getSelectedIndex() != 0) {
 			subject = subjectCombo.getSelectedItem().toString();
 		}
 		
 		String query = "";
 		
 		String reportsQuery = "SELECT BookCopy.callNumber, BookCopy.copyNo, Borrowing.inDate, Borrowing.outDate " +
 								"FROM BookCopy " +
 								"INNER JOIN Borrowing on BookCopy.copyNo = Borrowing.copyNo and BookCopy.callNumber = Borrowing.callNumber " +
 								"WHERE BookCopy.status = 'out' " + 
 								"ORDER BY BookCopy.callNumber ";
 		
 		if (result == JOptionPane.OK_OPTION) {
 			
 			try {
 
 				ResultSet rs = null;
 				
 				if (subject.isEmpty() == true) {
 					
 					query = reportsQuery;
 					System.out.println(query);
 					
 					PreparedStatement ps = Library.con
 							.prepareStatement(query);
 					ps.executeQuery();
 
 					rs = ps.getResultSet();
 				}
 				else {
 					// TODO: add ORDER BY clause
 					query = "SELECT BookCopy.callNumber, BookCopy.copyNo, Borrowing.inDate, Borrowing.outDate " +
 							"FROM BookCopy " + 
 							"INNER JOIN Borrowing ON BookCopy.copyNo = Borrowing.copyNo and BookCopy.callNumber = Borrowing.callNumber " +
 							"INNER JOIN HasSubject ON BookCopy.callNumber = Borrowing.callNumber and BookCopy.callNumber = HasSubject.callNumber " +
 							"WHERE HasSubject.subject = ? and BookCopy.status = 'out' ";
 
					System.out.println(query);
 					PreparedStatement ps2 = Library.con
 							.prepareStatement(query);
 					ps2.setString(1, subject);
 					ps2.executeQuery();
 
 					rs = ps2.getResultSet();
 				}
 			
 			LibraryGUI.showReportsTable(rs, query, subject);
 			}
 			catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		
 	}
 	
 	public void generatePopularBooksReport() {
 		
 		// User inputs: bid, password
 		JTextField yearField = new JTextField(4);
 		JTextField numberField = new JTextField(3);
 		numberField.setText("1");
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("Year to generate report for:"), yearField,
 				new JLabel("No. of books:"), numberField,
 
 		};
 		int result = JOptionPane.showConfirmDialog(null, inputs,
 				"Popular Books Report", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 		
 		if (result == JOptionPane.OK_OPTION) {
 		
 			int noBooks = Integer.parseInt(numberField.getText());
 			String year = yearField.getText() + "-01-01";
 			String nYear = "";
 
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			Calendar c = Calendar.getInstance();
 
 			Date date1 = null;
 			Date date2 = null;
 
 			try {
 				// Add one year to input year
 				c.setTime(df.parse(year));
 				c.add(Calendar.YEAR, 1); 
 				nYear = df.format(c.getTime()); 
 
 				java.util.Date userYear = df.parse(year);
 				java.util.Date nextYear = df.parse(nYear);
 
 				date1 = new Date(df.parse(
 						df.format(userYear)).getTime());
 				date2 = new Date(df.parse(
 						df.format(nextYear)).getTime());
 
 			} catch (ParseException ex) {
 				new ErrorMessage("Error parsing year.");
 			}
 
 			Date[] dates = { date1, date2 };
 			String query = "SELECT * FROM ( " +
 					"SELECT Borrowing.callNumber, Book.title, Book.mainAuthor,  COUNT(Borrowing.callNumber) AS timesBorrowed " +
 					"FROM Borrowing " +
 					"LEFT JOIN Book ON Book.callNumber = Borrowing.callNumber " +
 					"WHERE Borrowing.outDate BETWEEN ? and ? " + 
 					"GROUP BY Borrowing.callNumber, Book.title, Book.mainAuthor " +
 					"ORDER BY timesBorrowed DESC ) " +
 					"WHERE ROWNUM <= ?";
 			
 			try {
 			PreparedStatement ps = Library.con
 					.prepareStatement(query);
 			ps.setDate(1, dates[0]);
 			ps.setDate(2, dates[1]);
 			ps.setInt(3, noBooks);
 			ps.executeQuery();
 			
 			
 			ResultSet rs = ps.getResultSet();			
 			
 			LibraryGUI.showPopularBooksReportsTable(rs, query, dates, noBooks);
 			}
 			catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		
 	}
 	
 	public static Vector<String> getSubjectList() {
 		
 		Vector<String> subjectList = new Vector<String>(5,1);
 		String query = "SELECT DISTINCT(subject) FROM HasSubject";
 		
 		try {
 			PreparedStatement ps = Library.con
 					.prepareStatement(query);
 			ps.executeQuery();
 
 			ResultSet rs = ps.getResultSet();
 			while(rs.next()) {
 				subjectList.addElement(rs.getString("subject"));
 			}
 		}
 		catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		return subjectList;
 		
 	}
 	// For generating call numbers
 	public static String randomString() {
 		char nextChar;
 		StringBuilder sb = new StringBuilder();
 		Random rnd = new Random();
 		String nextInt;
 		// Length of random character string - random between 1 and 5
 		int random = 1 + (int) (Math.random() * (5 - 1) + 1);
 
 		for (int i = 0; i < random; i++) {
 
 			int strOrNum = (int) Math.floor(Math.random() * 10);
 			if (strOrNum > 5) {
 				nextChar = (char) (rnd.nextInt(26) + 97);
 				sb.append(nextChar);
 			} else {
 				nextInt = Integer.toString(rnd.nextInt(9));
 				sb.append(nextInt);
 			}
 		}
 
 		return sb.toString();
 	}
 }
