 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2012-2013  Dimitrios Desyllas (pc_magas)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *Contact with me by main at thes address: pc_magas@yahoo.gr
 */
 
 package guma.ui.main;
 
 import guma.ui.main.UIStatus;
 import guma.core.*;
 import guma.arithmetic.Praxis;
 import java.io.File;
 import java.io.IOException;
 /**
 *A class that allows you to interact with the game
 */
 public abstract class GameController
 {
 
 	/**
 	*The game that the Controller will control
 	*/
 	protected Game paixnidi=null;
 	
 	/**
 	*User interface status
 	*/
 	protected UIStatus current=new UIStatus();
 
 	
 	/**
 	*Constructor method that allows you to make the communicatio between UI and core (Game)
 	*/
 	public GameController()
 	{
 		
 		current.saveAs=true;
 		current.next=false;
 	}
 
 	/**
 	*Make a game Controller that initializes a Game
 	*@param makeGame: Tells if it will be created a game or not
 	*/
 	public GameController(boolean makeGame)
 	{
 		this();
 
 		if(makeGame)
 		{
 			newGame();
 		}
 		current.next=true;
 	}
 
 	/**
 	*Method that allows the UI to cimmunicate with the core (Game class)
 	*@param: apotelesma the result given from UI (User Interface)
 	*/
 	public UIStatus takeResult(int apotelesma)
 	{
 
 		int resultsNum=paixnidi.getResultsNum();
 		System.out.println("Results Should be taken "+resultsNum);
 		int results[]=new int[resultsNum];
 
 		System.out.println("Results to be taken= "+results.length);
 		if(paixnidi!=null && results!=null)
 		{
 			try
 			{
 				results[0]=apotelesma;
 				if(results.length>1)
 				{
 					results[1]=getExtraResult();
 				}
 				
 				if(paixnidi.checkApotelesma(results))
 				{
 					try
 					{
 						current.praxisValue=paixnidi.toString();
 					}
 					catch(GameOverException gend)//Game Ended
 					{
 						displayMessage("Τέλος Παιχνιδιού",gend.getMessage());
 						current.next=false;
 						paixnidi=null;
 						current.praxisValue="x+y=";
 						current.save=false;
 						current.saveAs=false;
 						return current.clone();
 					}
 				}
 				else
 				{		
 						int pos=paixnidi.getWrongResultPos();
 						if(pos==Praxis.MODULO_POS)
 						{
 							do
 							{
 								displayError("Προσπάθησε ξανα","Λάθος τιμή υπολοίπου\nEναπομένουσες προσπάθειες: "
 											+paixnidi.getTries());
 								results[1]=getExtraResult();
 							}while(!paixnidi.checkApotelesma(results));
 									
 						}
 						else
 						{
 								displayError("Προσπάθησε ξανα","Λάθος αποτέλεσμα!\nEναπομένουσες προσπάθειες: "
 													+paixnidi.getTries());		
 						}
 
 				}
 				current.praxisRemainingDisplay="Πράξεις:"+paixnidi.getRemainingPraxis()+'/'+paixnidi.getPraxisNum();
 				System.out.println("Inside take result remaining praxis: "+current.praxisRemainingDisplay);
 				current.save=true;
 			}
 			catch(GameOverException gend)//Game Ended
 			{
 				displayMessage("Τέλος Παιχνιδιού",gend.getMessage());
 				current.next=false;
 				paixnidi=null;
 				current.praxisValue="x+y=";
 				current.save=false;
 				current.saveAs=false;
 			}
 			catch(TriesEndException tend)//Tries Ended
 			{
 				triesEndMessage("Τέλος προσπαθειών",tend.getMessage());
 				try
 				{
 					current.praxisValue=paixnidi.toString();
 				}
 				catch(GameOverException gend)
 				{
 					displayMessage("Τέλος Παιχνιδιού",gend.getMessage());
 					current.next=false;
 					paixnidi=null;
 					current.praxisValue="x+y=";
 					current.save=false;
 					current.saveAs=false;
 				}
 			}
 			
 		}
 		return current.clone();
 	}
 
 	/**
 	*Method to save the Game
 	*@param f: File to save the Game
 	*/
 	public void save(File f) throws IOException
 	{
 		if(paixnidi!=null)
 		{
 			paixnidi.save(f);
 			current.save=false;
 		}
 	}
 
 	/**
 	*Method that loads the game
 	*@param f: File that Game is saved
 	*/
 	public UIStatus load(File f) throws IOException,ClassNotFoundException
 	{
 		paixnidi=Game.load(f);
 
 		if(paixnidi==null)
 		{
 			return null;
 		}
 		else
 		{	
 			current.praxisValue=paixnidi.toString();
 			current.praxisRemainingDisplay="Πράξεις:"+paixnidi.getRemainingPraxis()+'/'+paixnidi.getPraxisNum();
 			current.saveAs=true;
 			current.save=false;
 			return current.clone();
 		}
 	}
 
 	/**
 	*Shows if game has started or not
 	*/
 	public boolean gameStarted()
 	{
 		if(paixnidi!=null)
 		{
 			current.saveAs=true;
 			return true;
 		}
 		else
 		{
 			current.saveAs=false;
 			return false;
 		}
 	}
 
 	/**
 	*Returns the current UIStatus
 	*/
 	public UIStatus getUIStatus()
 	{
 		
 		return current.clone();
 	}
 
 	/**
 	*Shiows if the game has saves even onece
 	*/
 	public boolean gameSaved()
 	{
 		return paixnidi.isSaved();
 	}
 	
 	/**
 	*In some arithmetic operations especially into Division
 	*So it provides you a way to get the extra result. 
 	*/
 	protected abstract int getExtraResult();
 
 	/**
 	*Displays an error when tries ended
 	*@param title: the title of message window
 	*@param message: the message
 	*/
 	public abstract void triesEndMessage(String title,String message);
 	
 	/**
 	*Method that shows to the user an error message
 	*@param title: the title of message window
 	*@param message: the message
 	*/
 	public abstract void displayError(String title, String message);
 
 	/**
 	*Displays a Message
 	*@param title: the title of message window
 	*@param message: the message
 	*/
 	public abstract void displayMessage(String title, String message);
 	
 	/**
 	*Shows Simulator
 	*/
 	public abstract void simulate();
 	
 	/**
 	*Method that creates a new game
 	*/
 	public void newGame()
 	{
 			paixnidi= makeNewGame();
 			if(paixnidi!=null)
 			{
 				this.current.save=true;
 				this.current.saveAs=true;
 				this.current.praxisValue=paixnidi.toString();
 				this.current.praxisRemainingDisplay="Πράξεις:"+paixnidi.getRemainingPraxis()+'/'+paixnidi.getPraxisNum();
 			}
 			else
 			{
 				System.out.println("Else");
 				this.current.save=false;
 				this.current.saveAs=false;
 				this.current.praxisValue="x+y=";
 				this.current.praxisRemainingDisplay="Πράξεις:";
 			}
 			System.out.println("Inside new game: "+current.praxisRemainingDisplay);	
 	}
 	
 	/**
 	*Initialize a new game
 	*/
 	public abstract Game makeNewGame();
	
	/**
	*Load a Game 
	*/
	public abstract Game loadGameFromWeb();
	/**
	*Loads a Game from the Web and updates the Ui
	*/
	public void loadFromWeb()
	{
			paixnidi= loadGameFromWeb();
			if(paixnidi!=null)
			{
				this.current.save=true;
				this.current.saveAs=true;
				this.current.praxisValue=paixnidi.toString();
				this.current.praxisRemainingDisplay="Πράξεις:"+paixnidi.getRemainingPraxis()+'/'+paixnidi.getPraxisNum();
			}
			else
			{
				System.out.println("Else");
				this.current.save=false;
				this.current.saveAs=false;
				this.current.praxisValue="x+y=";
				this.current.praxisRemainingDisplay="Πράξεις:";
			}
			System.out.println("Inside new game: "+current.praxisRemainingDisplay);	
	}
 }
