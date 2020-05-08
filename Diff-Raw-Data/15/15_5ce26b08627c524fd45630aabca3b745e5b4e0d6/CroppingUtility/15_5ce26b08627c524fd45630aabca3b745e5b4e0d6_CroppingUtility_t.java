 import javax.swing.*;
 import javax.imageio.*;
 import javax.swing.filechooser.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.awt.event.*;
 import java.io.*;
 
 /**
  * Cropping Utility
  * Given an image and dimension size, crop a picture down to a desired resolution!
  * Useful for making backgrounds out of large pictures by highlighting desired areas.
  * @author Logan Gore
 * !!!TODO: BUG: Image not scrollable!!!
  */
 public class CroppingUtility implements ActionListener
 {
 	//Desired cropping dimensions
 	private int cropHeight;
 	private int cropWidth;
 
 	//GUI elements
 	private JFrame myFrame;
 	private TransparentPanel boxPanel;
 	
 	private JMenuBar myMenuBar;
 	private JMenuItem openMenuItem;
 	private JMenuItem setDimensionsItem;
 	private JMenuItem saveMenuItem;
 	
 	private JScrollPane myScrollPane;
 	
 	//Original image
 	private File inputFile;
 	private BufferedImage myImage;
 	private String fileExtension;
 	private ImagePanel imagePanel;
 	
 	//New image
 	private BufferedImage subImage;
 		
 	/**
 	 * Constructor to create a CroppingUtility program.
 	 * @param args the arguments given when the program was started
 	 */
 	public CroppingUtility(String[] args)
 	{
 		createGUI();
 		
 		//Process any command line arguments first
 		processArgs(args);
 		
 		//The program was opened without context.  Prompt the user for an input file
 		if (inputFile == null)
 		{
 			getImageChoice();
 		}
 	}
 	
 	/**
 	 * Process a list of arguments from the command line.
 	 * Current supported arguments:
 	 * 		-i <input file image>
 	 */
 	private void processArgs(String[] args)
 	{
 		if (args.length > 0)
 		{
 			//Traverse through the arguments
 			for (int i = 0; i < args.length; i++)
 			{
 				//Input file flag
 				if (args[i].equals("-i"))
 				{
 					//Load the input file image and increment the argument index
 					inputFile = new File(args[i + 1]);
 					loadImage();
 					i++;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Create the GUI for a CroppingUtility program.
 	 */
 	private void createGUI()
 	{
 		//Create the main frame
 		myFrame = new JFrame("Cropping Utility [Working Title]");
 		
 		//Create the menu
 		myMenuBar = new JMenuBar();
 		openMenuItem = new JMenuItem("Open");
 		setDimensionsItem = new JMenuItem("Set Dimensions");
 		saveMenuItem = new JMenuItem("Save As");
 		myMenuBar.add(openMenuItem);
 		myMenuBar.add(setDimensionsItem);
 		myMenuBar.add(saveMenuItem);
 		
 		//Add action listeners to the menu items
 		openMenuItem.addActionListener(this);
 		setDimensionsItem.addActionListener(this);
 		saveMenuItem.addActionListener(this);
 		
 		//Add the menu to the main frame
 		myFrame.add(myMenuBar, BorderLayout.NORTH);
 		
 		//Set the default close operation of the JFrame
 		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//Set the default size of the frame
 		myFrame.setSize(800, 600);
 		
 		//Default behavior is to have the frame maximized
 		myFrame.setExtendedState(myFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
 		
 		//Make the frame visible
 		myFrame.setVisible(true);
 	}
 		
 	/**
 	 * Use a JFileChooser to get the user's desired image to crop.
 	 */
 	private void getImageChoice()
 	{
 		//Create a JFileChooser to get the user's desired image to crop
 		JFileChooser fileChooser = new JFileChooser();
 		
 		//Create a filter to show only JPG, PNG, and GIF images
 		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, PNG, and GIF images", "jpg", "gif", "png");
 		fileChooser.setFileFilter(filter);
 		
 		//Get the user's image choice
 		int choice = fileChooser.showOpenDialog(myFrame);
 		if (choice == JFileChooser.APPROVE_OPTION)
 		{
 			//Get the chosen filename
 			inputFile = fileChooser.getSelectedFile();
 			
 			//Load the chosen image
 			loadImage();
 		}
 	}
 	
 	/**
 	 * Loads the chosen image into the GUI.
 	 */
 	private void loadImage()
 	{
 		//Load the input file into a BufferedImage object
 		try
 		{
 			myImage = ImageIO.read(inputFile);
 		}
 		//If an IOException was encountered, tell the user
 		catch(IOException e)
 		{
 			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());				
 		}
 	
 		//Load the imagePanel
 		imagePanel = new ImagePanel(myImage);
 		
 		//Create a scroll pane to hold the image
		myScrollPane = new JScrollPane(imagePanel);		
 		
 		//Add the scroll pane to the main frame
 		myFrame.add(myScrollPane, BorderLayout.CENTER);
 		
		//Pack the frame to make the image visible
		myFrame.pack();
		
 		//Get the desired dimensions for the cropped picture
 		getCropDimensions();
 	}
 	
 	/**
 	 * Get the desired dimensions of cropping for the picture the user has chosen.
 	 */
 	private void getCropDimensions()
 	{
 		//If an image hasn't been loaded yet, tell the user
 		if (myImage == null)
 		{
 			JOptionPane.showMessageDialog(null, "No image has been loaded yet.  Please load an image first.");
 			return;
 		}
 		
 		//Create a HeightAndWidthInputPane object to get the user's width and height dimensions
 		HeightAndWidthInputPane inputPane = new HeightAndWidthInputPane(myImage.getWidth(), myImage.getHeight());
 		
 		//Get the height and width from the inputPane
 		cropWidth = inputPane.getWidth();
 		cropHeight = inputPane.getHeight();
 		
 		//Set up the selection box and show it within the app
 		boxPanel = new TransparentPanel();
 		boxPanel.setSize(cropWidth, cropHeight);
 		//TODO: This makes the picture invisible
 		//myFrame.add(boxPanel);
 		boxPanel.setVisible(true);
 	}
 	
 	/**
 	 * Prompt the user for where to save the cropped image they have created.
 	 */
 	private void saveNewImage()
 	{
 		//If an image hasn't been loaded yet, tell the user and return
 		if (myImage == null)
 		{
 			JOptionPane.showMessageDialog(null, "No image has been loaded yet.  Please load an image first.");
 			return;
 		}
 		
 		//If dimensions haven't been set yet, tell the user and return
 		if (cropWidth == 0 || cropHeight == 0)
 		{
 			JOptionPane.showMessageDialog(null, "No dimensions have been set yet.");
 			return;
 		}
 		
 		//Create the subimage based on where the transparent panel has been placed
 		subImage = myImage.getSubimage(boxPanel.getX(), boxPanel.getY(), boxPanel.getWidth(), boxPanel.getHeight());
 		
 		//Create a JFileChooser to save the user's new subimage
 		JFileChooser chooser = new JFileChooser();
 		
 		//Find out where the user wants to save the file
 		int choice = chooser.showSaveDialog(myFrame);
 		if (choice == JFileChooser.APPROVE_OPTION)
 		{
 			//Get the filename the user chose to save the new image as
 			String outFileName = chooser.getSelectedFile().getName();
 			
 			//If the user specified a file extension, use it
 			if (outFileName.indexOf(".") != -1)
 				fileExtension = outFileName.substring(outFileName.lastIndexOf(".") + 1);
 			//Otherwise, use the file extension of the input file
 			else
 				fileExtension = inputFile.getName().substring(inputFile.getName().lastIndexOf(".") + 1);
 			
 			//Try to save the new image to file
 			try
 			{
 				//Write the file to disk.  Tell the user the file was written successfully
 				if (ImageIO.write(subImage, fileExtension, chooser.getSelectedFile()))
 					JOptionPane.showMessageDialog(null, "New image created successfully.");
 				//The file could not be written.  Throw an IOException
 				else
 					throw new IOException("Could not write output file (Unknown cause).");
 			}
 			//If an IOException was encountered, tell the user
 			catch(IOException e)
 			{
 				JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());				
 			}
 		}
 	}
 	
 	/**
 	 * Define the behavior to occur when a user hits one of the menu buttons in the application.
 	 * @param e the actionEvent that occurred to trigger this method
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		//If the user hit the open button, load a new image
 		if (e.getSource() == openMenuItem)
 			getImageChoice();
 		//If the user hit the set dimensions button, reprompt them for new dimensions
 		else if (e.getSource() == setDimensionsItem)
 			getCropDimensions();
 		//If the user hit the save button, save the cropped image
 		else if (e.getSource() == saveMenuItem)
 			saveNewImage();
 	}
 
 	/**
 	 * Start the program.
 	 * @param args the arguments passed to the program when it was started
 	 * TODO: If passed with a filename argument, load that image instead of prompting
 	 */
 	public static void main(String[] args)
 	{
 		new CroppingUtility(args);
 	}
 }
