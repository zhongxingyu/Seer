 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 public class LibrarianTransactions {
 	
 	/*************************************************************************************
 	*   LIBRARIAN TRANSACTIONS:
 	*   	- add books, add book copy, generate checked out books report(TODO), generate popular books(TODO)
 	*************************************************************************************/
 	public void addBook() {
 
 		// User inputs: isbn, title, mainAuthors, otherAuthors,
 		// publisher, year, subjects
 		JTextField isbnField = new JTextField(10);
 		JTextField titleField = new JTextField(10);
 		JTextField mainAuthorField = new JTextField(10);
 		JTextField otherAuthorsField = new JTextField(100);
 		JTextField publisherField = new JTextField(10);
 		JTextField yearField = new JTextField(10);
 		JTextField subjectsField = new JTextField(100);
 
 		JComponent[] inputs = new JComponent[] { 
 				new JLabel("isbn:"), isbnField, 
 				new JLabel("Title:"), titleField,
 				new JLabel("Main author:"), mainAuthorField,
 				new JLabel("Other authors:"), otherAuthorsField,
 				new JLabel("Publisher:"), publisherField, 
 				new JLabel("Year:"), yearField, 
 				new JLabel("Subjects:"), subjectsField 
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
 			String[] subjects = subjectsField.getText().split(",");
 			String callNumber = randomString() + " " + randomString() + " "
 					+ Integer.toString(year);
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
 				for (int i = 0; i < subjects.length; i++) {
 
 					// Add subjects into hasSubjects table
 					PreparedStatement ps = Library.con
 							.prepareStatement("INSERT INTO hasSubject VALUES (?,?)");
 					ps.setString(1, callNumber);
 					ps.setString(2, subjects[i]);
 
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
 		
 	}
 	
 	public void generatePopularBooksReport() {
 		
		// User inputs: bid, password
		JTextField yearField = new JTextField(4);
		JTextField numberField = new JTextField(3);

		JComponent[] inputs = new JComponent[] { 
				new JLabel("Book year:"), yearField,
				new JLabel("No. of books:"), numberField,

		};
		int result = JOptionPane.showConfirmDialog(null, inputs,
				"Popular Books Report", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		
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
