 /*
  * Framework written by Brad Johnson * Contents written by Jeremy Guebert
  * NextBooks
  * 2011-2012
  */
  package gui;
  import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import javax.swing.JLabel;
 import controllers.Controller;
 import dbprocess.DatabaseProcess;
 import model.Book;
 import model.User;
 
 /**
  * Panel for editing and viewing a users own account information.
  * @author Brad
  *
  */
 public class MyAccountPanel extends StyledPanel {
  	/**
 	 * The user to be performing an update on.
 	 * This should normally be Controller.getCurrentUser().
 	 */
 	private User userToShow;
 	
 	/**
 	 * Inner class used for handling button updates
 	 * @author jlg327
 	 */
 	private class UpdateListener implements ActionListener {
  		/**
 		 * The type of field that must be updated
 		 * 1 for username, 2 for email
 		 */
 		private int field;
  		/**
 		 * The new information to put in the appropriate user field
 		 */
 		private String information;
 		/**
 		 * Tells the updater to update the username field
 		 */
 		public static final int USERNAME_FIELD = 1;
  		/**
 		 * Tells the updater to update the 
 		 */
 		public static final int EMAIL_FIELD = 2;
 		/**
 		 * Creates an update listener and tells it which field is to be updated
 		 * @param string
 		 */
 		public UpdateListener(int field, String information) {
 			this.field = field;
 			this.information = information;
 		}
  		/**
 		 * Handles the case of a user pressing the update button
 		 */
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			String userPassword = "";
 			try {
 				userPassword = DatabaseProcess.getInstance().getUserPassWord(userToShow.username);
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} 
 			switch (field) {
 				case(USERNAME_FIELD):
 					userToShow.username = information;
 					break;
 				case(EMAIL_FIELD):
 					userToShow.email = information;
 					break;
 				default:
 					// This is an error case - the only things that are allowed to be updated
 					// at present are username and email - This should probably throw an Exception
 					break;
 			}
 			try {
 				Controller.changeUserInfo(userToShow.username, userToShow, userPassword, userPassword);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
  	}
 	
     /**
      * Serial version ID, added for improved system performance and maintenance
      */
     private static final long serialVersionUID = -6750536388415434300L;
      /**
      * Constructs the panel.
      * TODO: Everything
      * @throws SQLException I'm pretty sure this will be eliminated when testing code is removed and real code is implemented
      */
     public MyAccountPanel() throws SQLException { 
         
         this.setLayout(new GridLayout(0, 1));        this.setOpaque(false);
          JLabel headerLabel = new JLabel("Account Details");        headerLabel.setForeground(PanelsManager.UNSELECTEDBLUE);
         // TODO: Make update buttons work
         // TODO: Make it pretty...
         // TODO: Replace next line with userToShow = Controller.getCurrentUser()
        userToShow = new User("testname", false, "testing", null, null, null);
         userToShow.rentals = new ArrayList<Book>();
         // TODO: Remove the following two lines entirely - adding books to the test user to test the display of rentals
         userToShow.rentals.add(new Book("testBook1", "author1", 3.14, "address1", 314, null, "testing book1"));
         userToShow.rentals.add(new Book("testBook2", "author2", 13.37, "address1", 1337, null, "testing book2"));
         // Handle the username        // Handle the username        LabeledInputField usernamePanel = new LabeledInputField("Username:", "Update");        usernamePanel.setText(userToShow.username);        usernamePanel.addActionListener(new UpdateListener(UpdateListener.USERNAME_FIELD, usernamePanel.getText()));                // Handle the email        LabeledInputField emailPanel = new LabeledInputField("Password:", "Update");        emailPanel.setText(userToShow.email);        emailPanel.addActionListener(new UpdateListener(UpdateListener.EMAIL_FIELD, emailPanel.getText()));
         // Handle the isAdmin flag
         JLabel isAdminLabel = new JLabel("Administrator:   " + userToShow.isAdmin);        isAdminLabel.setForeground(PanelsManager.UNSELECTEDBLUE);
         // Handle the list of book rentals
         String bookList = "";
         if (userToShow.rentals != null)
         {
 	        for (Book b : userToShow.rentals)
 	        {
 	        	bookList += b.title + "\n";
 	        }
         }
         JLabel rentalsLabel = new JLabel("Rentals:\n" + bookList);        rentalsLabel.setForeground(PanelsManager.UNSELECTEDBLUE);
         // Add all of the components to the overall panel
         this.add(headerLabel);
         this.add(usernamePanel);
         this.add(emailPanel);
         this.add(isAdminLabel);
         this.add(rentalsLabel);
     }    
 }
