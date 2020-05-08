 /* CPSC 304 - Library Checkout System
  * © Mar. 2013 Kevin Petersen. All rights reserved.
  */
 
 package com.ui.librarian;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.swing.*;
 
 import com.book.Book;
 import com.book.BookCopy;
 
 /**
  * This class implements a graphical login window to connect to the Oracle
  * database.
  * 
  * @author Kevin Petersen
  */
 public class NewBook {
 	private JFrame frame = new JFrame("New Book");
 	private JPanel contentPane = new JPanel();
 	private GridBagLayout gb = new GridBagLayout();
 	private GridBagConstraints c = new GridBagConstraints();
 	
 	private static final int FIELD_WIDTH = 30;
 	private final int LABEL_ALIGNMENT = GridBagConstraints.LINE_START;
 	
 	private JTextField isbnField = new JTextField(FIELD_WIDTH);
 	private JTextField titleField = new JTextField(FIELD_WIDTH);
 	private JTextField mainAuthorField = new JTextField(FIELD_WIDTH);
 	private JTextField publisherField = new JTextField(FIELD_WIDTH);
 	private JTextField yearField = new JTextField(FIELD_WIDTH);
 	
 	private ActionListener submitAction = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				float isbn = Float.valueOf(isbnField.getText()); 
 				String title = titleField.getText();
 				String mainAuthor = mainAuthorField.getText();
 				
 				String publisher = publisherField.getText();
 				publisher = (publisher.isEmpty() ? null : publisher);
 				
 				String yearString = yearField.getText();
 				int year = (yearString.isEmpty() ? 0 : Integer.parseInt(yearString));
 				List<Book> bookList = Book.getAll();
 				Boolean exists = false;
 				Book FoundBook = null;
 				for (Book boo : bookList) {
 					if (boo.getIsbn() == isbn) {
 						exists = true;
 						FoundBook = boo;
 					}
 					break;	
 				}
 				
 				if (exists) { 
					BookCopy b = BookCopy.generate(FoundBook);
 					System.out.println("A new copy of #" + b.getCallNumber() + " has been added!");
 				} 
 				else {
				Book b = Book.generate();
				b.setIsbn(isbn);
				b.setTitle(title);
				b.setMainAuthor(mainAuthor);
				b.setPublisher(publisher);
				b.setYear(year);
 				System.out.println("Book #" + b.getCallNumber() + " added!");
 				}
				
 
 				isbnField.setText("");
 				titleField.setText("");
 				mainAuthorField.setText("");
 				publisherField.setText("");
 				yearField.setText("");
 			} catch (SQLException sql) {
 				System.out.print("Could not add Book.");
 			}
 		}
 	};
 	private ActionListener cancelAction = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			frame.dispose();	
 		}
 	};
 	
 	/**
 	 * Builds the components and does any initialization for the window
 	 */
 	public NewBook() {
 		initializePane();
 		addISBN();
 		addTitle();
 		addMainAuthor();
 		addPublisher();
 		addYear();
 		addSubmitButton();
 		addCancelButton();
 	}
 	
 	/**
 	 * Builds the base frame and pane for the window
 	 */
 	private void initializePane() {
 		frame.setContentPane(contentPane);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				frame.dispose();
 			}
 		});
 		
 		contentPane.setLayout(gb);
 		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 	}
 
 	/**
 	 * Builds the ISBN field and label and adds them to the window 
 	 */
 	private void addISBN() {
 		// Place the isbn label
 		JLabel label = new JLabel("Enter ISBN*: ");
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(10, 10, 5, 0);
 		c.anchor = LABEL_ALIGNMENT;
 		gb.setConstraints(label, c);
 		contentPane.add(label);
 
 		// Place the isbn field for the bid
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(10, 0, 5, 10);
 		gb.setConstraints(isbnField, c);
 		contentPane.add(isbnField);
 	}
 	
 	/**
 	 * Builds the Title field and label and adds them to the window 
 	 */
 	private void addTitle() {
 		// Place title label
 		JLabel label = new JLabel("Enter Title*: ");
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(0, 10, 5, 0);
 		c.anchor = LABEL_ALIGNMENT;
 		gb.setConstraints(label, c);
 		contentPane.add(label);
 
 		// Place the title field
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(0, 0, 5, 10);
 		gb.setConstraints(titleField, c);
 		contentPane.add(titleField);
 	}
 
 	/**
 	 * Builds the name Main Author and label and adds them to the window 
 	 */
 	private void addMainAuthor() {
 		// Place the main author label
 		JLabel label = new JLabel("Enter Main Author*: ");
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(0, 10, 5, 0);
 		c.anchor = LABEL_ALIGNMENT;
 		gb.setConstraints(label, c);
 		contentPane.add(label);
 
 		// Place the text field for the main author
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(0, 0, 5, 10);
 		gb.setConstraints(mainAuthorField, c);
 		contentPane.add(mainAuthorField);
 	}
 	
 	/**
 	 * Builds the Publisher field and label and adds them to the window 
 	 */
 	private void addPublisher() {
 		// Place the publisher label
 		JLabel label = new JLabel("Enter Publisher: ");
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(0, 10, 5, 0);
 		c.anchor = LABEL_ALIGNMENT;
 		gb.setConstraints(label, c);
 		contentPane.add(label);
 
 		// Place the text field for the publisher
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(0, 0, 5, 10);
 		gb.setConstraints(publisherField, c);
 		contentPane.add(publisherField);
 	}
 	
 	/**
 	 * Builds the Year field and label and adds them to the window 
 	 */
 	private void addYear() {
 		// Place the year label
 		JLabel label = new JLabel("Enter Publication Year: ");
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(0, 10, 5, 0);
 		c.anchor = LABEL_ALIGNMENT;
 		gb.setConstraints(label, c);
 		contentPane.add(label);
 
 		// Place the text field for the year
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(0, 0, 5, 10);
 		gb.setConstraints(yearField, c);
 		contentPane.add(yearField);
 	}
 
 	/**
 	 * Builds the submit button and adds it to the window 
 	 */
 	private void addSubmitButton() {
 		// Place the submit button
 		JButton button = new JButton("Submit");
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(5, 10, 10, 5);
 		c.anchor = GridBagConstraints.LINE_START;
 		gb.setConstraints(button, c);
 		contentPane.add(button);
 		button.addActionListener(this.submitAction);
 	}
 	
 	/**
 	 * Builds the cancel button and adds it to the window 
 	 */
 	private void addCancelButton() {
 		// Place the cancel button
 		JButton button = new JButton("Cancel");
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(5, 5, 10, 10);
 		c.anchor = GridBagConstraints.LINE_END;
 		gb.setConstraints(button, c);
 		contentPane.add(button);
 		button.addActionListener(this.cancelAction);
 	}
 	
 	/**
 	 * Packages, places and makes the window visible
 	 */
 	public void displayWindow() {
 		// Size the window to obtain a best fit for the components
 		frame.pack();
 		
 		// center the frame
 		Dimension d = frame.getToolkit().getScreenSize();
 		Rectangle r = frame.getBounds();
 		frame.setLocation(	(d.width - r.width) / 2,
 								(d.height - r.height) / 2);
 
 		// make the window visible
 		frame.setVisible(true);
 
 		// place the cursor in the text field for the username
 		isbnField.requestFocus();
 	}
 }
