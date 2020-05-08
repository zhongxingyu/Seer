 /**
  * @(#)MyApp.java
  *
  * My first java application. Functioning AI. Requires JavaV6 or higher.
  *
  * @Copyright MMXII by CPCookieMan
  * @version 1.00 started 1/3/2012
  * @current version InDev last updated 2/9/2012
  */
 
 //TODO LIST
 
 //Add more questions!
 //Make login counter work.
 
 import javax.swing.*;
 import java.io.*;
 import java.awt.*;
 import java.util.*;
 import java.net.*;
 public class MyApp {
     public static void main(String[] args) {
     	String version = "InDev";
     	String fName;
     	String lName;
     	String fullName;
     	String creatorName;
     	Random randInt = new Random();
     	int randNum = 0;
     	String questionAsked = "NONE";
     	String question1;
     	String question2;
     	String question3;
     	String question4;
     	String question5;
     	String question6;
     	String question7;
     	String questionReset1;
     	String questionReset2;
     	String questionQuit;
     	String favoriteColor;
     	String loginsRead = "";
     	int askNum = randInt.nextInt(3 - 1 + 1) + 1;
     	int debugMode;
     	int quitAsked;
     	int noAsk = 0;
     	int logins = 0;
     	boolean nameSet = false;
     	String compName = "null";
     	debugMode = 0;
     	quitAsked = 0;
     	favoriteColor = "Unknown";
     	creatorName = "null";
     	question1 = "What's your favorite color?";
     	question2 = "What's my favorite color?";
     	question3 = "??";
     	question4 = "What's your name?";
     	question5 = "What's my name?";
     	question6 = "Is Sprite good?";
     	question7 = "HAL open the pod bay doors";
     	questionReset1 = "Reset computer name.";
     	questionReset2 = "Reset login counter.";
     	questionQuit = "I'm done talking for now.";
     	URL url = null;
 		URLConnection urlConn = null;
 		InputStreamReader  inStream = null;
 		BufferedReader buff = null;
 		String nextLine = "null";
 		//Start MOTD loading code.
 		try
 		{
 			url  = new URL("http://cookieai.cpcookieman.com/motd.txt" );
 			urlConn = url.openConnection();
 			inStream = new InputStreamReader(urlConn.getInputStream());
 			buff = new BufferedReader(inStream);
 			nextLine = buff.readLine();
 			JOptionPane.showMessageDialog(null, nextLine);
 		}
 		catch(IOException  e1)
 		{
 			JOptionPane.showMessageDialog(null, "I don't have a connection to the internet or the MOTD server is down! Can't load MOTD for you, sorry.");
 		}
 		//End MOTD loading code.
 		//Start version check code.
 		try
 		{
 			url  = new URL("http://cookieai.cpcookieman.com/motd.txt" );
 			urlConn = url.openConnection();
 			inStream = new InputStreamReader(urlConn.getInputStream());
 			buff = new BufferedReader(inStream);
 			int lineCount = 0;
 			String bestVersion = "Null";
 			while (true)
 			{
 				if (lineCount != 2)
 				{
 					bestVersion = buff.readLine();
 					lineCount = lineCount + 1;
 				}
 				else
 				{
 					break;
 				}
 			}
 			if (version.equals(bestVersion))
 			{
 				//CookieAI is up to date, no need to do anything.
 			}
 			else
 			{
 				//TODO, add download spot.
 				JOptionPane.showMessageDialog(null, "An update to CookieAI is available! Get it from UNDETERMINED.");
 				JOptionPane.showMessageDialog(null, "Your version: " + version + " Latest version: " + bestVersion);
 			}
 		}
 		catch(IOException  e1)
 		{
 			//Nothing needs to happen here, it would've happened before.
 		}
 		//End version check code.
 		//Start creator name loading here.
 		try
 		{
 			url  = new URL("http://cookieai.cpcookieman.com/motd.txt" );
 			urlConn = url.openConnection();
 			inStream = new InputStreamReader(urlConn.getInputStream());
 			buff = new BufferedReader(inStream);
 			int lineCount = 0;
 			while (true)
 			{
 				if (lineCount != 3)
 				{
 					creatorName = buff.readLine();
 					lineCount = lineCount + 1;
 				}
 				else
 				{
 					break;
 				}
 			}
 		}
 		catch(IOException  e1)
 		{
 			//Nothing needs to happen here, it would've happened before.
 		}
 		//End creator name loading here.
     	fName = JOptionPane.showInputDialog("Hello there! What's your first name?");
     	if (fName.equals(""))
     	{
     		JOptionPane.showMessageDialog(null, "Not going to tell me your name?");
     		fName = JOptionPane.showInputDialog("Oh, come on, tell me your first name.");
     		if (fName.equals(""))
     		{
     			JOptionPane.showMessageDialog(null, "Fine then stubborn, your first name is John.");
     			fName = "John";
     		}
     	}
     	else if (fName.equals("Nope"))
     	{
     		JOptionPane.showMessageDialog(null, "Not going to tell me your name?");
     		fName = JOptionPane.showInputDialog("Oh, come on, tell me your first name.");
     		if (fName.equals(""))
     		{
     			JOptionPane.showMessageDialog(null, "Fine then stubborn, your first name is John.");
     			fName = "John";
     		}
     	}
     	lName = JOptionPane.showInputDialog(fName + "... That's a pretty cool name! What's your last name?");
     	if (lName.equals(""))
     	{
     		JOptionPane.showMessageDialog(null, "You don't want to tell me your last name?");
     		lName = JOptionPane.showInputDialog("Oh, come on, tell me your last name.");
     		if (lName.equals(""))
     		{
     			JOptionPane.showMessageDialog(null, "Fine then stubborn, your last name will be Johnson.");
     			lName = "Johnson";
     		}
     	}
     	else if (lName.equals("Nope"))
     	{
     		JOptionPane.showMessageDialog(null, "You don't want to tell me your last name?");
     		lName = JOptionPane.showInputDialog("Oh, come on, tell me your last name.");
     		if (lName.equals(""))
     		{
     			JOptionPane.showMessageDialog(null, "Fine then stubborn, your last name will be Johnson.");
     			lName = "Johnson";
     		}
     	}
     	fullName = fName + " " + lName;
     	if (fullName.equals(creatorName))
     	{
     		JOptionPane.showMessageDialog(null, "Oh, it's you! I'll enable debug mode!", "DEBUG", JOptionPane.WARNING_MESSAGE);
     		debugMode = 1;
     		favoriteColor = "Orange";
     	}
     	//Begin user saving and loading code
     	try
 		{
 			BufferedReader readIn = new BufferedReader(new FileReader("data/users/" + fullName + "/" + "0Remembered.txt"));
 			JOptionPane.showMessageDialog(null, "Oh I remember you " + fName + "! Welcome back!");
 			questionAsked = JOptionPane.showInputDialog("So, what did you want to ask me " + fName + "?");
 			readIn.close();
 		}
 		catch(Exception noUserFolder)
 		{
 			try
 			{
 				//Should never actually execute this code, but it still should be here.
 				BufferedWriter writer = new BufferedWriter(new FileWriter("data/users/" + fullName + "/" + "0Remembered.txt"));
 				writer.close();
 				JOptionPane.showMessageDialog(null, "That's weird, I have a folder for you but no remember file. I'll just create it and continue on.");
 				JOptionPane.showMessageDialog(null, fullName + " eh? Well I'm your computer, it's nice we finally get to talk.");
 				questionAsked = JOptionPane.showInputDialog("Here's your chance to ask me anything " + fName + ". Go on, ask away!");
 			}
 			catch(Exception noUserFolder2)
 			{
 				try
 				{
 					new File("data/users/" + fullName + "/").mkdirs();
 					BufferedWriter writer = new BufferedWriter(new FileWriter("data/users/" + fullName + "/" + "0Remembered.txt"));
 					writer.close();
 					JOptionPane.showMessageDialog(null, fullName + " eh? Well I'm your computer, it's nice we finally get to talk.");
 					questionAsked = JOptionPane.showInputDialog("Here's your chance to ask me anything " + fName + ". Go on, ask away!");
 				}
 				catch(Exception userFileNotCreated)
 				{
 					JOptionPane.showMessageDialog(null, "Sorry " + fName + ", I couldn't create your user file. Please exit the program.");
 					quitAsked = 1;
 				}
 			}
 		}
 		try
 		{
 			//This code is different because it's system wide.
 			if (debugMode == 1)
     		{
     			JOptionPane.showMessageDialog(null, "Computer name is being loaded in.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     		}
 			BufferedReader readIn = new BufferedReader(new FileReader("data/" + "ComputerName.txt"));
 			compName = readIn.readLine();
 			readIn.close();
 			if (compName.equals(""))
 			{
 				//Should only occour if name was reset
 				nameSet = false;
 				compName = "null";
 			}
 			else
 			{
 				nameSet = true;
 			}
 			//Computer name is being loaded into the memory.
 		}
 		catch (Exception e127)
 		{
 			if (debugMode == 1)
     		{
     			JOptionPane.showMessageDialog(null, "Computer name could not be found.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     		}
 			//Keep nameSet bool at false
 			nameSet = false;
 			compName = "null";
 			//Redundancy is good
 		}
 		try
 		{
 			BufferedReader readIn = new BufferedReader(new FileReader("data/users/" + fullName + "/" + "FavoriteColor.txt"));
 			favoriteColor = readIn.readLine();
 			readIn.close();
 			//Favorite color is being loaded into the memory.
 		}
 		catch (Exception e127)
 		{
 			//Do nothing, favorite color must not yet be saved.
 		}
 		try
 		{
 			BufferedReader readIn = new BufferedReader(new FileReader("data/users/" + fullName + "/" + "Logins.txt"));
 			loginsRead = readIn.readLine();
 			logins = Integer.parseInt(loginsRead);
 			readIn.close();
 			logins = logins + 1;
 			BufferedWriter writer = new BufferedWriter(new FileWriter("data/users/" + fullName + "/" + "Logins.txt"));
 			String loginsWrite = Integer.toString(logins);
 			writer.write(loginsWrite);
 			writer.close();
 			//Login number is being loaded into the memory and incremented.
 		}
 		catch (Exception e151)
 		{
 			try
 			{
 				JOptionPane.showMessageDialog(null, "First login.");
 				//First login, save the file with 1 in it.
 				logins = 1;
 				String loginsWrite = Integer.toString(logins);
 				BufferedWriter writer = new BufferedWriter(new FileWriter("data/users/" + fullName + "/" + "Logins.txt"));
 				writer.write(loginsWrite);
 				writer.close();
 			}
 			catch (Exception e162)
 			{
 				JOptionPane.showMessageDialog(null, "Error, could not write logins file.");
 				quitAsked = 1;
 			}
 		}
 		//End user saving and loading code
     	if (debugMode == 1)
     	{
     		JOptionPane.showMessageDialog(null, "Continuing into the while loop with the first question...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     	}
     	while (quitAsked == 0)
     	{
     		if (questionAsked.equals(question1))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Question 1 was asked, displaying answer.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Well " + fName + ", I'm not sure how to say it in English...");
     			JOptionPane.showMessageDialog(null, "It's kinda like blue, but with more channels.");
     			//Testing to see if user has inputted favorite color already...
     			String fileTest = "";
     			try
 				{
 					BufferedReader readIn = new BufferedReader(new FileReader("data/users/" + fullName + "/" + "FavoriteColor.txt"));
 					fileTest = "FileExists";
 					readIn.close();
 					//Color has already been inputted.
 				}
 				catch(Exception e151)
 				{
 					fileTest = "NotThere";
 					//Color has not been inputted.
 				}
 				if (fileTest.equals("FileExists"))
 				{
 					//Do nothing
 				}
 				else
 				{
 					if (debugMode == 1)
     				{
     					JOptionPane.showMessageDialog(null, "Asking user favorite color...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     				}
     				favoriteColor = JOptionPane.showInputDialog("What's your favorite color, " + fName + "?");
     				JOptionPane.showMessageDialog(null, "I like " + favoriteColor + ". " + favoriteColor + " is a nice color.");
     				try
 					{
 						BufferedWriter writer = new BufferedWriter(new FileWriter("data/users/" + fullName + "/" + "FavoriteColor.txt"));
 						writer.write(favoriteColor);
 						writer.close();
 						if (debugMode == 1)
     					{
     						JOptionPane.showMessageDialog(null, "User color saved.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     					}
 					}
 					catch(Exception userFileNotCreated)
 					{
 						JOptionPane.showMessageDialog(null, "I couldn't save your favorite color. Sorry.");
 					}
 				}
     		}
     		else if (questionAsked.equals(question2))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Question 2 was asked, displaying answer.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			if (favoriteColor.equals("Unknown"))
     			{
     				if (debugMode == 1)
     				{
     					JOptionPane.showMessageDialog(null, "Value of favoriteColor was unknown, computer doesn't know.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     				}
     				JOptionPane.showMessageDialog(null, "I'm not sure, I don't think you've ever told me.");
     			}
     			else
     			{
     				if (debugMode == 1)
     				{
     					JOptionPane.showMessageDialog(null, "Value " + favoriteColor + " detected in favoriteColor, passing that.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     				}
     				JOptionPane.showMessageDialog(null, "I think you told me it was " + favoriteColor + ", right?");
     				JOptionPane.showMessageDialog(null, "Yeah, " + favoriteColor + "'s a pretty cool color, huh.");
     			}
     		}
     		else if (questionAsked.equals(question3))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Question 3 was asked, displaying answer.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Question 3 inputted.");
     			//Insert testing code here.
     			JOptionPane.showMessageDialog(null, "Logins: " + logins);
     		}
     		else if (questionAsked.equals(question4))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Question 4 was asked, displaying answer.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, nameSet, "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, compName, "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			if (nameSet == false)
     			{
     				JOptionPane.showMessageDialog(null, "I don't seem to have a name yet!");
     				compName = JOptionPane.showInputDialog("What should my name be?");
     				nameSet = true;
     				try
     				{
     					BufferedWriter writer = new BufferedWriter(new FileWriter("data/" + "ComputerName.txt"));
 						writer.write(compName);
 						writer.close();
 						JOptionPane.showMessageDialog(null, "Alright, I'll be called " + compName + " from now on.");
     				}
     				catch (Exception e347)
     				{
     					JOptionPane.showMessageDialog(null, "That's weird, I can't save the file. I'll remember for now though.");
     				}
     			}
     			else if (nameSet == true)
     			{
     				JOptionPane.showMessageDialog(null, "My name is " + compName + ".");
     			}
     			else
     			{
     				JOptionPane.showMessageDialog(null, "A boolean isn't true or false? What? I'm exiting now. Sorry, " + fName + ".");
     				quitAsked = 1;
     			}
     		}
     		else if (questionAsked.equals(question5))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Question 5 was asked, displaying answer.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "According to my records, your name is " + fullName + ".");
     		}
    		else if (questionAsked.equals(question6))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Question 6 was asked, displaying answer.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Absolutely.");
     		}
     		else if (questionAsked.equals(question7))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Question 7 was asked, Awaiting HAL's response", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "I'm afraid I can't do that " + fName + ".");
     		}
     		//New questions should be above this line, organized by question ID.
     		else if (questionAsked.equals(questionReset1))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "AI was requested to invoke a reset of the computer name, resetting.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Fine then, I'll go back to being a no namer... Null, what a stupid name to have.");
     			nameSet = false;
     			compName = "";
     			try
     			{
     				BufferedWriter writer = new BufferedWriter(new FileWriter("data/" + "ComputerName.txt"));
 					writer.write(compName);
 					writer.close();
     			}
     			catch (Exception e423)
     			{
     				JOptionPane.showMessageDialog(null, "Error resetting data.");
     			}
     		}
     		else if (questionAsked.equals(questionReset2))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "AI was requested to invoke a reset of the login counter, resetting.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Aww, I was doing such a good job at keeping track! Alright, I'll reset the counter for you.");
     			try
     			{
     				logins = 1;
 					String loginsWrite = Integer.toString(logins);
 					BufferedWriter writer = new BufferedWriter(new FileWriter("data/users/" + fullName + "/" + "Logins.txt"));
 					writer.write(loginsWrite);
 					writer.close();
     			}
     			catch (Exception e423)
     			{
     				JOptionPane.showMessageDialog(null, "Error resetting data.");
     			}
     		}
     		else if (questionAsked.equals(questionQuit))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("Nope"))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("Nope."))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("No"))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("No."))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("Nah"))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("Nah."))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("Quit"))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("Exit"))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Quit command given, exiting...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Alright then " + fName + ", see ya next time!");
     			quitAsked = 1;
     		}
     		else if (questionAsked.equals("I typed my name wrong."))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Change name command given...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			fName = JOptionPane.showInputDialog("Oh, what was your first name then?");
     			lName = JOptionPane.showInputDialog("Ahh, and your last?");
     			fullName = fName + " " + lName;
    				//Begin user saving and loading code
     			try
 				{
 					BufferedReader readIn = new BufferedReader(new FileReader("data/users/" + fullName + "/" + "0Remembered.txt"));
 					JOptionPane.showMessageDialog(null, "Oh I remember you " + fName + "! Welcome back!");
 					readIn.close();
 				}
 				catch(Exception noUserFolder)
 				{
 					try
 					{
 						//Should never actually execute this code, but it still should be here.
 						BufferedWriter writer = new BufferedWriter(new FileWriter("data/users/" + fullName + "/" + "0Remembered.txt"));
 						writer.close();
 						JOptionPane.showMessageDialog(null, "That's weird, I have a folder for you but no remember file. I'll just create it and continue on.");
 						JOptionPane.showMessageDialog(null, fullName + " eh? Well I'm your computer, it's nice we finally get to talk.");
 					}
 					catch(Exception noUserFolder2)
 					{
 						try
 						{
 							new File("data/users/" + fullName + "/").mkdirs();
 							BufferedWriter writer = new BufferedWriter(new FileWriter("data/users/" + fullName + "/" + "0Remembered.txt"));
 							writer.close();
 							JOptionPane.showMessageDialog(null, fullName + " eh? Well I'm your computer, it's nice we finally get to talk.");
 						}
 						catch(Exception userFileNotCreated)
 						{
 							JOptionPane.showMessageDialog(null, "Sorry " + fName + ", I couldn't create your user file. Please exit the program.");
 						}
 					}
 				}
 				try
 				{
 					BufferedReader readIn = new BufferedReader(new FileReader("data/users/" + fullName + "/" + "FavoriteColor.txt"));
 					favoriteColor = readIn.readLine();
 					readIn.close();
 					//Favorite color is being loaded into the memory.
 				}
 				catch (Exception e127)
 				{
 					//Reset favorite color.
 					favoriteColor = "Unknown";
 				}
 				//End user saving and loading code
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Name addition...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			if (fullName.equals(creatorName))
     			{
     				JOptionPane.showMessageDialog(null, "Creator detected, debug mode enabled!", "DEBUG", JOptionPane.WARNING_MESSAGE);
     				debugMode = 1;
     			}
     			else if (fullName.equals("Nolan Orr"))
     			{
     				if (debugMode == 1)
     				{
     					String nolanDebugAnswer;
     					nolanDebugAnswer = JOptionPane.showInputDialog("Debug mode detected, keep debug enabled? (Answer Yes or No)");
     					if (nolanDebugAnswer.equals("Yes"))
     					{
     						JOptionPane.showMessageDialog(null, "Alright Nolan, I'll keep debugging enabled.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     						debugMode = 1;
     					}
     					else if (nolanDebugAnswer.equals("No"))
     					{
     						JOptionPane.showMessageDialog(null, "Alright Nolan, debugging mode disabled.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     						debugMode = 0;
     					}
     					else
     					{
     						JOptionPane.showMessageDialog(null, nolanDebugAnswer + " is not a valid option, proceeding unchanged.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     					}
     				}
     				JOptionPane.showMessageDialog(null, "So " + fullName + " is your name, huh? I thought so.");
     				if (debugMode == 1)
     				{
     					JOptionPane.showMessageDialog(null, "Name successfully changed.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     				}
     			}
     			else
     			{
     				if (debugMode == 1)
     				{
     					JOptionPane.showMessageDialog(null, "Non-debug name given, resetting debug.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     				}
     				debugMode = 0;
     				JOptionPane.showMessageDialog(null, "So " + fullName + " is your name, huh? I thought so.");
     			}
     		}
     		else if (questionAsked.equals("Turn debugging off."))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Debugging mode is being turned off...", "DEBUG", JOptionPane.WARNING_MESSAGE);
     				debugMode = 0;
     			}
     			else
     			{
     				JOptionPane.showMessageDialog(null, "Sorry " + fName + ", I haven't been programmed to answer that yet.");
     			}
     		}
     		else if (questionAsked.equals("Turn debugging on."))
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Debugging mode already on, resetting value to 1.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     				debugMode = 1;
     			}
     			else if (fullName.equals(creatorName))
     			{
     				JOptionPane.showMessageDialog(null, "Alright then, I'm setting debug mode to on.");
     				debugMode = 1;
     			}
     			else if (fullName.equals("Nolan Orr"))
     			{
     				JOptionPane.showMessageDialog(null, "You have permission, I'll turn debugging on for you.");
     				debugMode = 1;
     			}
     			else
     			{
     				JOptionPane.showMessageDialog(null, "I'd love to " + fName + ", but I'm not allowed. Sorry.");
     				noAsk = 1;
     				askNum = 4;
     			}
     		}
     		else
     		{
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Question " + questionAsked + " is not a programmed question.", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			JOptionPane.showMessageDialog(null, "Sorry " + fName + ", I haven't been programmed to answer that yet.");
     			noAsk = 1;
     			askNum = 5;
     		}
     		if (quitAsked == 1)
     		{
     			//We're done here, no need for more code.
     		}
     		else
     		{
     			//Generates a new random number preparing to ask next question
     			if (noAsk == 0)
     			{
     				askNum = randInt.nextInt(3 - 1 + 1) + 1;
     			}
     			else
     			{
     				noAsk = 0;
     			}
     			if (debugMode == 1)
     			{
     				JOptionPane.showMessageDialog(null, "Now asking for next question... (using " + askNum + ")", "DEBUG", JOptionPane.WARNING_MESSAGE);
     			}
     			//Asking next question
     			if (askNum == 1)
     			{
     				questionAsked = JOptionPane.showInputDialog("Alright " + fName + ", anything else you wanted to ask?");
     			}
     			else if (askNum == 2)
     			{
     				questionAsked = JOptionPane.showInputDialog("So " + fName + ", what do you want to ask next?");
     			}
     			else if (askNum == 3)
     			{
     				questionAsked = JOptionPane.showInputDialog("Anything else you wanted to ask, " + fName + "?");
     			}
     			else if (askNum == 4)
     			{
     				questionAsked = JOptionPane.showInputDialog("But is there anything else you wanted to ask me?");
     			}
     			else if (askNum == 5)
     			{
     				questionAsked = JOptionPane.showInputDialog("I might be able to answer something else though, ask away!");
     			}
     			else
     			{
     				JOptionPane.showMessageDialog(null, "Oh no, I seem to be having a problem with the question randomizer, I'll have to quit out now.");
     				quitAsked = 1;
     			}
     		}
     	}
 
     }
 }
