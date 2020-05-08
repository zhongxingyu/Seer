 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2012-1013  Dimitrios Desyllas (pc_magas)
 *
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Contact with me by at this address: pc_magas@yahoo.gr
 */
 
 package guma.gui;
 
 import javax.swing.JOptionPane;
 import guma.gui.SettingFrame;
 import guma.core.Game;
 import guma.ui.main.GameController;
 
 /**
 *A class that allows you to Control the Game via Swing
 */
 public class SwingGameController extends GameController
 {
 
 	/**
 	*Make the Controller
 	*/
 	public SwingGameController()
 	{
 		super();
 	}
 
 	/**
 	*Make the Cotroller it can Initilize a Game
 	*@param makeGame: Tells if it will be created a game or not
 	*/
 	public SwingGameController(boolean makeGame)
 	{
 		super(makeGame);
 	}
 
 	/**
 	*In some arithmetic operations especially into Division
 	*So it provides you a way to get the extra result. 
 	*/
 	protected int getExtraResult()
 	{
 			String praxisResults=(String)JOptionPane.showInputDialog(null,"Η διαίρεση αυτή έχει Υπόλοιπο"+
 																								"\nΠαρακαλώ εισάγεται το υπόλοιπο",
 																								"Απαιτείται υπόλοιπό",
 																								JOptionPane.PLAIN_MESSAGE);
 		return Integer.parseInt(praxisResults);
 	}
 
 	/**
 	*Method that shows to the user an error message
 	*/
 	public void displayError(String title, String message)
 	{
 		JOptionPane.showMessageDialog(null,message,title,JOptionPane.ERROR_MESSAGE);
 	}
 
 	/**
 	*Displays a Message
 	*/
 	public void displayMessage(String title, String message)
 	{
 		JOptionPane.showMessageDialog(null,message,title,JOptionPane.PLAIN_MESSAGE);
 	}
 
 	/**
 	*Initialize a new game
 	*/
 	public	Game makeNewGame()
 	{
 		return new SettingFrame().getGame(null);
 	}
 	
 }
