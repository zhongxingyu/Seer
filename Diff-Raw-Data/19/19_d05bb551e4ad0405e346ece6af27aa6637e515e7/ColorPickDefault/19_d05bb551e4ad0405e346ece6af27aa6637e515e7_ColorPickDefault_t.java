 package edu.cs56.projects.discretemath.towers_sierpinski;
 
 import java.io.*;
 import java.util.Properties;
 import java.util.ArrayList;
 import java.util.*;
 import java.util.Collections;
 
 import javax.swing.JOptionPane;
 import javax.swing.JDialog;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.colorchooser.*;
 
 /**
  Parent Frame class that makes instances of ColorPickRunner or displays default color schemes to choose from.
 */
 public class ColorPickDefault {
 
 	private ColorPickRunner ColorRun; //needed
     protected JLabel banner; //Displays written "Objective"?
 	private JFrame frame; //main frame
 	private JPanel TextPanel; //panel to hold Scroll area with written color hexes
 	private JPanel MainPanel; // Panel to add buttons and banner to
 	private JTextArea text; //main display of current chosen colors in hex form? (not like preview panel made by button)
 	private Map<String,ArrayList<Color>> CustomColorList; //Maybe change to map.  The list of all the available "default" color schemes.
 	private Map<String,ArrayList<Color>> UserChoicesToSave; //Possibly unnecessary.  Acts as buffer to be saved. Different from the total map.
 	private ArrayList<Color> ChosenColorList; //Current choice of ColorList.  Would be sent to props file/maybe added to defaults/
 	private ArrayList<Color> TmpList; // maybe unnecessary, but acts as a tmp transfer list for getting from a runner instance.
 	private boolean open; //boolean to tell parent prog whether it is open or not.
 	private JComboBox ComboChoices; //ComboBox gui for default. Is added to if CustomColorList is added to. Reflects CustomColorList.
     
 	/**
 	 no arg constructor that makes the GUI, and sets up private variables.
 	*/
     public ColorPickDefault() {
 		
 		frame = new JFrame("Default Color Choices For Sierpinski Triangles");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		CustomColorList = new HashMap<String,ArrayList<Color>>();
 		
 		UserChoicesToSave = new HashMap<String,ArrayList<Color>>();
		//SaveNewDefaultViaSerializable();
 
 		CreateDefaultChoices();
 		
 		ChosenColorList = new ArrayList<Color>();
 
 		open = true;
 		
 		//TITLE
        banner = new JLabel("Choose a Color Scheme in the drop down menu. You can preview the scheme or save your choice.",JLabel.CENTER);
         banner.setForeground(Color.black);
         banner.setBackground(Color.white);
         banner.setOpaque(true);
         banner.setFont(new Font("SansSerif", Font.BOLD, 20));
 		
 		//DISPLAYS CURRENT CHOICES COLORS IN ORDER AND BY HEX/COLOR_TO_TEXT???
 		TextPanel = new JPanel(new BorderLayout());
 		text = new JTextArea(10,30);
 		text.setLineWrap(true);
 		text.setEditable(false);
 		JScrollPane scroller = new JScrollPane(text);
 		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		TextPanel.add(scroller);
 		UpdateTextWithChosenColor(); //USE UPDATE METHOD, also updates everytime ChosenColorList changes.
 		
 		//COMBOBOX, needs to be set to reflect changes in CustomColorList...
 		ComboChoices = new JComboBox();
 		//ComboChoices.setSelectedIndex(0);
 		UpdateChoiceBox();
 		ComboChoices.addActionListener(new ChoiceComboListener());
 		//USE UPDATE METHOD
 		
 		//ADD BUTTON AT BOTTOM OF PICKER PANEL TO CONFIRM A COLOR CHOICE
 		JButton PreviewButton = new JButton("Preview Current Color Scheme");
 		PreviewButton.addActionListener(new PreviewListener());
 		
 		//ADD BUTTON AT BOTTOM OF PICKER PANEL TO EXIT AND RETURN VALUE TO PARENT PROG CALL
 		JButton ExitButton = new JButton("Close Window and Export Colors");
 		ExitButton.addActionListener(new ExitListener());
 		
 		//ADD BUTTON AT BOTTOM OF FRAME TO OPEN NEW CUSTOM PICKER
 		JButton CustomButton = new JButton("Choose New Scheme");
 		CustomButton.addActionListener(new CustomListener());
 		
 		//ADD RESET TO DEFAULTS BUTTON???? removes user defined classes without having to ant clean. Possibly. Cant implement until serialzable is done.
 		
 		//ADD TO VARIOUS PANELS,THEN TO FRAME TO MAKE LAYOUT SIMPLER.
 		MainPanel = new JPanel(new BorderLayout());
 		
 		MainPanel.add(TextPanel, BorderLayout.CENTER);
         MainPanel.add(ComboChoices, BorderLayout.NORTH);
 		MainPanel.add(CustomButton, BorderLayout.EAST);
 		MainPanel.add(ExitButton, BorderLayout.WEST);
 		MainPanel.add(PreviewButton, BorderLayout.SOUTH);
 		
 		frame.add(MainPanel);
 		frame.add(banner,BorderLayout.NORTH);
 		frame.pack();
         frame.setVisible(true);
 	}
 	
 	
 	
 	class ChoiceComboListener implements ActionListener {
 		public void actionPerformed(ActionEvent event){
 			String ColorTmpCB = (String)ComboChoices.getSelectedItem();
 			ChosenColorList = CustomColorList.get(ColorTmpCB);
 			UpdateTextWithChosenColor(); //SEPARTE METHOD TO UPDATE COLOR LABEL WITH COLOR IN CURRENT LIST CHOSEN
 		}
 	}
 	
 	/**
 	 Creates a new instance of ColorPickRunner each time
 	 the button calls the ActionListener.  Also the main thread waits for the 
 	 new window to close before it can finish.
 	 ////USE THIS BUTTON TO RUN THE COLORRUNNERCLASS INSTANCE...
 	 
 	 */
 	class CustomListener implements ActionListener { //ALMOST DONE WITH METHOD
 		public void actionPerformed(ActionEvent event){
 			//Arraylist <Color> TmpList; //.add(new Color(0,0,0));
 			
 			final Runnable Linker = new Runnable() {
 				public void run() {
 					ColorRun = new ColorPickRunner();
 					TmpList = ColorRun.returnColorList();
 				}
 			};
 			
 			Thread LinkThread = new Thread() {
 				public void run() {
 					try {
 						synchronized (this) {
 							SwingUtilities.invokeAndWait(Linker);
 							while (ColorRun.isOpen()) {
 								Thread.sleep(100);
 							}
 						}
 					}
 					catch (Exception e) {
 						e.printStackTrace();
 					}
 					finally {
 						
 						TmpList = ColorRun.returnColorList();
 							
 						//PROMPT/OPTIONBOX...ALMOST DONE
 						
 						Object[] options = {"Save & Continue","Export & Exit"};
 						int n = JOptionPane.CLOSED_OPTION;
 						String Choice = null;
 						
 						while (n == JOptionPane.CLOSED_OPTION) {
 							n = JOptionPane.showOptionDialog(null, "Would you like to save your choice and continue browsing or would you like to confirm as your final choice",
 															  "Custom Color Chooser", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 						}
 						if(n == JOptionPane.YES_OPTION) {
 							while (Choice == null) {
 								Choice = (String)JOptionPane.showInputDialog(null,"Choose a Name for the Custom Color Scheme","Custom Color Chooser",
 																	   JOptionPane.PLAIN_MESSAGE,null,null,"PLACEHOLDER");
 								if (CustomColorList.containsKey(Choice) == true) {
 									Choice = null;
 								}
 							}
 							//ADD CHOICE AND CHOSEN COLOR LIST TO MAP
 							ChosenColorList = TmpList;
 							CustomColorList.put(Choice, ChosenColorList);
 							UserChoicesToSave.put(Choice, ChosenColorList);
 							UpdateTextWithChosenColor();
 							//ADDS OPTION TO COMBOBOX
 							ComboChoices.addItem(Choice);
 							ComboChoices.setSelectedItem(Choice);
 						}
 						else if(n == JOptionPane.NO_OPTION) {
 							//(Close down and save color choice, do not add, Save Via Properties, 
 							//Also make attempt to call SaveViaSerialzable to save other possible schemes)
 								
 							ChosenColorList = TmpList;
 							frame.setVisible(false);
 							WriteToPropertiesFile();
 							SaveNewDefaultViaSerializable();
 							open = false;
 							frame.dispose();
 						}
 						
 					}
 				}
 			};
 			LinkThread.start();
 		}
 	}
 	
 	class PreviewListener implements ActionListener {
 		public void actionPerformed(ActionEvent event){
 			
 			final Runnable Linker = new Runnable() {
 				public void run() {
 					JFrame PreviewFrame = new JFrame();
 					PreviewFrame.setSize(30*ChosenColorList.size(),200);
 					PreviewPanel Prev = new PreviewPanel();
 					PreviewFrame.add(Prev);
 					PreviewFrame.setVisible(true);
 				}
 			};
 				
 			Thread LinkThread = new Thread() {
 				public void run() {
 					try {
 						synchronized (this) {
 							SwingUtilities.invokeAndWait(Linker);
 							
 						}
 					}
 					catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			};
 			LinkThread.start();
 		}
 
 	}
 	
 	class PreviewPanel extends JPanel{
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			for (int i=0; i<ChosenColorList.size(); i++) {
 				g.setColor(ChosenColorList.get(i));
 				g.fillRect(30*i, 0, 30, 200);
 			}
 		}
 	}
 	
 	/** 
 	 This class is called when exit button is pressed.  This exports 
 	 chosen colors to a Properties file.//SHOULD STAY THE SAME MOSTLY  MAYBE CHANGE TO SERIALZABLE... for the saving of arraylist objs.
 	 */
 	class ExitListener implements ActionListener {
 		public void actionPerformed(ActionEvent event){
 			open = false;
 			//return arraylist value to target...?, should be obj that instantiates this obj to make call to private arraylist.
 			frame.setVisible(false);
 			WriteToPropertiesFile();
 			SaveNewDefaultViaSerializable();
 			frame.dispose();
			System.exit(0);
 		}
 		
 	}
 	
	private void CreateDefaultChoices() { // CAN HARDCODE ADDITIONAL VALUES
 		ArrayList<Color> Rainbow = new ArrayList<Color>();
 		ArrayList<Color> SpringTime = new ArrayList<Color>();
 		ArrayList<Color> Winter = new ArrayList<Color>();
 		ArrayList<Color> GrayScale = new ArrayList<Color>();
 		ArrayList<Color> Purples = new ArrayList<Color>();
 		
 		Collections.addAll(Rainbow, new Color(255,0,0), new Color(255,153,51), new Color(255,255,0), new Color(51,204,0), new Color(0,0,255));
 		Collections.addAll(SpringTime, new Color(0,255,0), new Color(0,204,102), new Color(0,204,204), new Color(153,0,204), new Color(255,153,153));
 		Collections.addAll(Winter, new Color(255,0,51), new Color(0,153,51), new Color(0,0,204), new Color(0,255,255), new Color(204,204,204));
 		Collections.addAll(GrayScale, new Color(200,200,200), new Color(150,150,150), new Color(100,100,100), new Color(50,50,50), new Color(0,0,0));
 		Collections.addAll(Purples, new Color(255,102,255), new Color(255,0,255), new Color(204,0,204), new Color(153,0,153), new Color(51,0,51));
 		
 		
 		CustomColorList.put("Rainbow",Rainbow);
 		CustomColorList.put("SpringTime",SpringTime);
 		CustomColorList.put("Winter",Winter);
 		CustomColorList.put("GrayScale",GrayScale);
 		CustomColorList.put("Purples",Purples);
 		
 		//ADD USER DEFINED DEFAULTS
 		
 		CheckSavedUserDefinedSchemes();
 	}
 	
 	private void UpdateChoiceBox() { //Directly resets and updates ComboChoices Dialog box.
 		//ComboChoices.removeAllItems(); CAUSES PROBLEMS
 		CreateDefaultChoices();
 		
 		for (Map.Entry<String, ArrayList<Color>> entry : CustomColorList.entrySet()) //USE CustomColorList...????
 		{
 			ComboChoices.addItem(entry.getKey());
 		}
 	}
 	
 	private void UpdateTextWithChosenColor() { //goes through ChosenColorList and writes it to the text area, resets each time.
 		text.setText("");
 		for (int i=0; i < ChosenColorList.size();i++)
 		{
 			Color TmpCol = ChosenColorList.get(i);
 			text.append(Integer.toString(i) + ".)" + TmpCol.toString() + "\n");
 		}
 	}
 	
 	/**
 	 Method that saves the chosen colors in the ChosenColorList 
 	 to a properties file in the build directory. //STAY THE SAME MOSTLY
 	*/
 	public void WriteToPropertiesFile(){
 		
 		Properties prop = new Properties();
     	try {
 			for (int i = 0; i < ChosenColorList.size(); i++) 
 			{
 				Color ColorToAdd = ChosenColorList.get(i);
 				String HexString = Integer.toHexString(ColorToAdd.getRGB());
 				String ColorName = "Color_" + Integer.toString(i);
 				prop.setProperty(ColorName, HexString); 
 			}
 			FileOutputStream Output = new FileOutputStream("build/DefaultColorProperties");
 			prop.store(Output, "Colors Saved by an instance of ColorPickDefault");
 			Output.flush();
 			Output.close();
 		} 
 		
 		catch (IOException ex) {
     		ex.printStackTrace();
         }
 	}
 	
 	private void CheckSavedUserDefinedSchemes() {
 			//checks the saved Serialzed obj file.
 		
 			//if there and has info, it adds to the CustomColorList. through form of for loop, until end of file.
 			
 			//if nothing is there then does nothing.
 		
 			//also checks if there are duplicates, cant have same names, but can have same schemes.
		
 		try {
 			FileInputStream fileStream = new FileInputStream("build/Saved_Default.ser");
 			ObjectInputStream inputStream = new ObjectInputStream(fileStream);
 			Object UserDefaults = inputStream.readObject();
 			HashMap<String,ArrayList<Color>> TmpDefaults = (HashMap<String,ArrayList<Color>>) UserDefaults; //HASH>>>>????
 			UserChoicesToSave.putAll(TmpDefaults);
 			if (UserChoicesToSave != null) {
				for (Map.Entry<String, ArrayList<Color>> entry : UserChoicesToSave.entrySet())
 				{
 					CustomColorList.put(entry.getKey(),entry.getValue());
 				}
 			}
 			inputStream.close();
 		}
 		catch (Exception ex) {
			//ex.printStackTrace();
			System.out.println("Saved_Default.ser does not exist.  This means there are no previous user defined color schemes.");
 		}
 	}
 	
 	
 	private void SaveNewDefaultViaSerializable() { //Adds to the default colors provided and when called in option box of ColorRunner.close()
 		if (UserChoicesToSave != null) {
 			try{
 				FileOutputStream fileStream = new FileOutputStream("build/Saved_Default.ser");
 				ObjectOutputStream outStream = new ObjectOutputStream(fileStream);
 				outStream.writeObject(UserChoicesToSave);
 				outStream.close();
 			}
 			catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 Method that works just like how ColorPickRunner.isOpen() works with this class
 	 This method can be used by a parent class call to determine if this window 
 	 has been closed yet.
 	 @return open set to true when open, false when closed
 	*/
 	public boolean isOpen(){
 		return open;
 	}
 	
 	/**
 	 Method for nonmembers to access the ColorList directly.
 	 @return ArrayList<Color> ColorList
 	*/
 	public ArrayList<Color> returnColorList() {
 		return ChosenColorList;
 	}
 	
     public static void main(String[] args) {
 		
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             
 			@Override
 			public void run() {
                 new ColorPickDefault();
             }
         });
     }
 
 }
