 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.newdawn.slick.*;
 import org.w3c.dom.DOMException;
 import org.xml.sax.SAXException;
 
 public class GUI {
 	
 	/*
 	 * TODO:
 	 * 		add info bar
 	 * 			- info changes based on which XML file is loaded
 	 * 		add electrical, mechanical, and simulation buttons
 	 */
 	public static int MenuBarFileX = 4;
 	public static int MenuBarFileY = 2;
 	public static Color LightBlue = new Color(0xffaaccff);
 	public static Button fileButton, btnSelNew, btnSelOpen, btnSelSave, btnSelQuit;
 	public static Button editButton, btnSelElec, btnSelMech, btnSelSim;
 	public static Button helpButton, btnSelHelp, btnSelAbout;
 	
 	public static void InitGUI(GameContainer gc) throws SlickException {
 		fileButton = new Button(gc, 1, 1, 40, 18, Color.white, Color.black, "File");
 		editButton = new Button(gc, fileButton.getX() + fileButton.getWidth() + 2, 1, 40, 18, Color.white, Color.black, "Edit");
 		helpButton = new Button(gc, editButton.getX() + editButton.getWidth() + 2, 1, 40, 18, Color.white, Color.black, "Help");
 		
 		btnSelNew = new Button(gc, 1, fileButton.getY()+fileButton.getHeight()+2, 40, 18, Color.white, Color.black, "New");
 		btnSelOpen = new Button(gc, 1, btnSelNew.getY()+btnSelNew.getHeight()+2, 40, 18, Color.white, Color.black, "Open");
 		btnSelSave = new Button(gc, 1, btnSelOpen.getY()+btnSelOpen.getHeight()+2, 40, 18, Color.white, Color.black, "Save");
 		btnSelQuit = new Button(gc, 1, btnSelSave.getY()+btnSelSave.getHeight()+2, 40, 18, Color.white, Color.black, "Quit");
 		
 		btnSelElec = new Button(gc, editButton.getX(), editButton.getY()+editButton.getHeight()+2, 40, 18, Color.white, Color.black, "Electrical");
 		btnSelMech = new Button(gc, editButton.getX(), btnSelElec.getY()+btnSelElec.getHeight()+2, 40, 18, Color.white, Color.black, "Mechanical");
 		btnSelSim = new Button(gc, editButton.getX(), btnSelMech.getY()+btnSelMech.getHeight()+2, 40, 18, Color.white, Color.black, "Simulation");
 		
 		btnSelHelp = new Button(gc, helpButton.getX(), helpButton.getY()+helpButton.getHeight()+2, 40, 18, Color.white, Color.black, "Help");
 		btnSelAbout = new Button(gc, helpButton.getX(), btnSelHelp.getY()+btnSelHelp.getHeight()+2, 40, 18, Color.white, Color.black, "About");
 	}
 	
 	// This is some very long method with a ton of if statements to make the GUI look pretty and more fun to use.
 	// ooooooo look, the file just button changed colour.
 	public static void UpdateGUI(GameContainer gc, int delta) throws SlickException {
 		Input input = gc.getInput();
 		
 		// TODO: Add event handlers.
 		
 		// Checks if the mouse is over some buttons.  If it is then the background colour changes to light gray
 		if (fileButton.mouseOverArea(gc) == true){
 			fileButton.changeColour(Color.lightGray);
 		}else{
 			fileButton.changeColour(Color.white);
 		}
 		if (editButton.mouseOverArea(gc) == true){
 			editButton.changeColour(Color.lightGray);
 		}else{
 			editButton.changeColour(Color.white);
 		}
 		if (helpButton.mouseOverArea(gc) == true){
 			helpButton.changeColour(Color.lightGray);
 		}else{
 			helpButton.changeColour(Color.white);
 		}
 		
 		// When the button is clicked then is becomes active.  The other buttons become inactive.
 		if (fileButton.buttonClicked(gc) == true){
 			fileButton.setActivity(true);
 			editButton.setActivity(false);
 			helpButton.setActivity(false);
 		}else if(editButton.buttonClicked(gc) == true){
 			editButton.setActivity(true);
 			fileButton.setActivity(false);
 			helpButton.setActivity(false);
 		}else if(helpButton.buttonClicked(gc) == true){
 			helpButton.setActivity(true);
 			editButton.setActivity(false);
 			fileButton.setActivity(false);
 		}
 		
 		// When the file button is active; checks if the mouse is over the buttons that pop up underneath file.  
 		// If it is then the background colour changes to light gray.
 		// Also, allows for interaction with the new, save, open and quit buttons.
 		if (fileButton.isActive() == true){
 			// Makes the buttons change colour
 			if (btnSelNew.mouseOverArea(gc) == true){
 				btnSelNew.changeColour(Color.lightGray);
 			}else{
 				btnSelNew.changeColour(Color.white);
 			}
 			if (btnSelSave.mouseOverArea(gc) == true){
 				btnSelSave.changeColour(Color.lightGray);
 			}else{
 				btnSelSave.changeColour(Color.white);
 			}
 			if (btnSelOpen.mouseOverArea(gc) == true){
 				btnSelOpen.changeColour(Color.lightGray);
 			}else{
 				btnSelOpen.changeColour(Color.white);
 			}
 			if (btnSelQuit.mouseOverArea(gc) == true){
 				btnSelQuit.changeColour(Color.lightGray);
 			}else{
 				btnSelQuit.changeColour(Color.white);
 			}
 			
 			// Adds function to the buttons
 			// Reads robot.xml file to re-initialize all the variables
 			if (btnSelNew.buttonClicked(gc) == true){
 				try {
					File robot = new File("files/robot.xml");
 					XMLReadWrite.read(robot);
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (DOMException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (SAXException e) {
 					e.printStackTrace();
 				} catch (ParserConfigurationException e) {
 					e.printStackTrace();
 				}
 			}
 			// Creates a new file with all the modified data
 			if (btnSelSave.buttonClicked(gc) == true){
 				JFrame guiFrame = new JFrame();						// Uses swing's fileChooser class to make
 				JFileChooser fileDialog = new JFileChooser();		//    a very pretty dialogue that allows the user 
 				int save = fileDialog.showSaveDialog(guiFrame);		//    to save their progress in style
 				if (save == JFileChooser.APPROVE_OPTION){			// Makes sure the input is good
 					File saveAs = fileDialog.getSelectedFile();
 					try {
 						XMLReadWrite.write(saveAs);					// Writes the XML file with the inputed file name and 
 					} catch (FileNotFoundException e) {				//  in the chosen directory
 						e.printStackTrace();
 					} catch (DOMException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (SAXException e) {
 						e.printStackTrace();
 					} catch (ParserConfigurationException e) {
 						e.printStackTrace();
 					}
 				}
 				
 			}
 			if (btnSelOpen.buttonClicked(gc) == true){
 				//Create a file chooser
 				JFrame guiFrame = new JFrame();						// Uses the fileChooser class to open up projects
 				JFileChooser fileDialog = new JFileChooser();		//    in style ;)
 				int open = fileDialog.showOpenDialog(guiFrame);		
 				if (open == JFileChooser.APPROVE_OPTION){			// Makes sure the file is good
 				    File file = fileDialog.getSelectedFile();
 				    try {
 						XMLReadWrite.read(file);					// Reads the file contents
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (DOMException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (SAXException e) {
 						e.printStackTrace();
 					} catch (ParserConfigurationException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			// Exits the program
 			if (btnSelQuit.buttonClicked(gc) == true){
 				System.exit(0);
 			}
 		}
 		
 		// When the edit button is active; checks if the mouse is over the buttons that pop up underneath edit.  
 		// If it is then the background colour changes to light gray.
 		// Also allows for interactions with the electrical, mechanical and simulation buttons.
 		if (editButton.isActive() == true){
 			// Makes the buttons change colour
 			if (btnSelElec.mouseOverArea(gc) == true){
 				btnSelElec.changeColour(Color.lightGray);
 			}else{
 				btnSelElec.changeColour(Color.white);
 			}
 			if (btnSelMech.mouseOverArea(gc) == true){
 				btnSelMech.changeColour(Color.lightGray);
 			}else{
 				btnSelMech.changeColour(Color.white);
 			}
 			if (btnSelSim.mouseOverArea(gc) == true){
 				btnSelSim.changeColour(Color.lightGray);
 			}else{
 				btnSelSim.changeColour(Color.white);
 			}
 			
 			// Adds function to the buttons
 			if (btnSelElec.buttonClicked(gc) == true){
 				btnSelElec.setActivity(true);
 				btnSelMech.setActivity(false);
 				btnSelSim.setActivity(false);
 			}else if(btnSelMech.buttonClicked(gc) == true){
 				btnSelMech.setActivity(true);
 				btnSelElec.setActivity(false);
 				btnSelSim.setActivity(false);
 			}else if(btnSelSim.buttonClicked(gc) == true){
 				btnSelSim.setActivity(true);
 				btnSelMech.setActivity(false);
 				btnSelElec.setActivity(false);
 			}
 		}
 		
 		// When the help button is active; checks if the mouse is over the buttons that pop up underneath help.  
 		// If it is then the background colour changes to light gray.
 		// Also allows for interaction with the about and help buttons.
 		if (helpButton.isActive() == true){
 			// Makes the buttons change colour
 			if (btnSelAbout.mouseOverArea(gc) == true){
 				btnSelAbout.changeColour(Color.lightGray);
 			}else{
 				btnSelAbout.changeColour(Color.white);
 			}
 			if (btnSelHelp.mouseOverArea(gc) == true){
 				btnSelHelp.changeColour(Color.lightGray);
 			}else{
 				btnSelHelp.changeColour(Color.white);
 			}
 			
 			// Adds functionality to the buttons
 			// Displays the about file using the default text editor
 			if (btnSelAbout.buttonClicked(gc) == true){
				File about = new File("files/about.txt");
 				try {
 					Desktop.getDesktop().open(about);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
				}catch (IllegalArgumentException e){
					e.printStackTrace();
 				}
 			}
 			// Displays the help file using the default text editor
 			if (btnSelHelp.buttonClicked(gc) == true){
				File help = new File("files/help.txt");
 				try {
 					Desktop.getDesktop().open(help);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
				}catch (IllegalArgumentException e){
					e.printStackTrace();
 				}
 			}
 		}
 		// When something other than the menu strip buttons are clicked the menu strip buttons are set to inactive.
 		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON) || input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)){
 			fileButton.setActivity(false);
 			editButton.setActivity(false);
 			helpButton.setActivity(false);
 		}
 	}
 	
 	public static void RenderGUI(GameContainer gc, Graphics g, String element) throws SlickException {
 		// Draws the menu bar
 		if (element == "MenuBar") {
 			 g.setColor(LightBlue);
 			 g.fillRect(0, 0, 1920, 20);
 			 fileButton.render(gc, g);
 			 editButton.render(gc, g);
 			 helpButton.render(gc, g);
 			 // Draws the options in file if file is active
 			 if (fileButton.isActive() == true){
 				 btnSelNew.render(gc, g);
 				 btnSelOpen.render(gc, g);
 				 btnSelSave.render(gc, g);
 				 btnSelQuit.render(gc, g);
 			 }
 			// Draws the options in edit if edit is active
 			 if (editButton.isActive() == true){
 				 btnSelElec.render(gc, g);
 				 btnSelMech.render(gc, g);
 				 btnSelSim.render(gc, g);
 			 }
 			// Draws the options in help if help is active
 			 if (helpButton.isActive() == true){
 				 btnSelHelp.render(gc, g);
 				 btnSelAbout.render(gc, g);
 			 }
 		}
 	}
 }
