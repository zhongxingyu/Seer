 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import view.UserInterface;
 import model.Card;
 import model.Memory;
 import model.User;
 
 public class MemoryController {
 
 	private int numberOfCards = 10;
 	private int firstCard=-1; // To check if both of the cards have been drawn
 	private int bothDrawn=0;
 	private boolean everyOther=true;
 	private Memory theModel;
 	private UserInterface theView;
 	
 
 	public MemoryController(UserInterface theView, Memory theModel) {
 		this.theView = theView;
 		this.theModel = theModel;
 
 		// this.theView.loggedInLayout();
 		this.theView.addLoginListener(new LoginListener());
 		this.theView.addCardListener(new CardListener());
 		this.theView.addExitListener(new ExitListener());
 		this.theView.addRegisterListener(new RegisterListener());
 		this.theView.addHighScoreListener(new HighScoreListener());
 		this.theView.addRegisterButtonListener(new RegisterButtonListener());
 		this.theView.addRegisterButtonListener2(new RegisterButtonListener2());
 		//
 	}
 
 	public void checkLogin(String name, ArrayList<User> users)
 			throws IOException {
 		boolean found = false;
 
 		for (int i = 0; i < this.theModel.getUsers().size(); i++) {
 			// Debugger //Prints all users in the list
 			// System.out.println(users.get(i).getName());
 			if (name.equals(users.get(i).getName())) {
 				this.theView.loggedInLayout();
 				found = true;
 				// this.theView.repaint();
 			}
 		}
 		if (found != true) {
 			this.theView.loggedInFaild();
 		}
 	}
 
 	private class LoginListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ll) {
 			try {
 				checkLogin(theView.getLoginText(), theModel.getUsers());
 			} catch (IOException e) {
 				e.printStackTrace();// Have no idea what this is
 									// (Auto-generated)
 			}
 		}
 	}
 
 	private class CardListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent cl) {
 			//Card card1,card2;
 			
 
 			for (int i = 0; i < numberOfCards; i++) {
 
 				if (cl.getActionCommand().equals("button" + i)) {
<<<<<<< HEAD
 					//System.out.println("pressed" + i);
 					//System.out.println(cl.getActionCommand().toString());
 					//theModel.checkIfPair(card1, card2)
 					//System.out.println(i);
 					if (everyOther == true) {
 						firstCard=i;
 						theModel.getCards().get(i).setFound(true);
 						bothDrawn++;
 						everyOther = false;
 					} else if(everyOther==false) {
 						theModel.getCards().get(i).setFound(true);
 						bothDrawn++;
 						everyOther = true;
 					}
 					
 					//System.out.println(bothDrawn);
 					//System.out.println(i);
 					
 					if(bothDrawn==2){
 						//theView.flipPair
 						bothDrawn=0;
 						
 						if(theModel.checkIfPair(theModel.getCards().get(i), theModel.getCards().get(firstCard))==true){
 							//System.out.println("Pair!");
 							theView.flipPair(i);
 							if(theModel.checkIfDone()==true)
 							{
 								System.out.println("Congrats u won!"); // Is suppose to visualize this
 							}
 						}
 						else{
 							theModel.getCards().get(i).setFound(false);
 							theModel.getCards().get(firstCard).setFound(false);
 							theView.flipImage(i);
 						}
 					}
 					else{
 						theView.flipImage(i);
 					}
=======
					// System.out.println("pressed" + i);
 					// System.out.println(cl.getActionCommand().toString());
					theView.flipImage(i);// Want to send button number here
>>>>>>> 8edc98548534cfefa1409a202176310ccf650b34
 				}
 			}
 		}
 	}
 
 	private class ExitListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 
 		}
 
 	}
 
 	private class RegisterListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			theView.displayRegister();
 
 		}
 
 	}
 
 	private class HighScoreListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			ArrayList<User> users = theModel.getUsers();
 			theView.displayHighScore();
 
 		}
 
 	}
 
 	private class RegisterButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (theView.getRegisterUsername() != null) {
 				theModel.addUser(theModel.getNoOfUsers() + 1, theView.getRegisterUsername(), 0);
 				theView.hideRegister();
 			} else {
 
 			}
 		}
 	}
 
 	private class RegisterButtonListener2 implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			theView.displayRegister();
 
 		}
 
 	}
 
 }
