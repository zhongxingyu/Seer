 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 
 public class thcsw002Parser implements ActionListener {
 
 	/* Main file IO */
 	private BufferedReader diskFile;
 	private String regex = ";"; // Query delimeter 
 	
 	/* Main File Opener GUI */
 	private JFrame serverWindow = new JFrame("FileParser for Switches");
 	private JPanel mainPanel = new JPanel();
 	private JPanel btnPanel = new JPanel();
 	private JTextArea outTextArea = new JTextArea(21,36);
 //	private JButton closeButton = new JButton("Close");
 //	private JButton saveButton = new JButton("Save...");
 	private String newLine = System.getProperty("line.separator");
 	private JScrollPane outScrollPane = new JScrollPane(outTextArea);
 	private JScrollBar vsb = outScrollPane.getVerticalScrollBar();
 	private String commaDelimeter = ",";
 	
 	/* Menu Bar */
 	private JMenu fileOptions; // file
 	private JMenuItem openNewFileItem;
 	private JMenuItem saveExisitingFileItem;
 	private JMenuItem exitProgramItem;
 	private JMenu viewOptions; // view
 	private JMenuItem viewFileItem; 
 	
 	@SuppressWarnings("unused")
 	public static void main (String[] args) throws Exception {
 		String filename = null;
 		String identifiers = null;
 		boolean cmdLineArgs = false;
 		thcsw002Parser fileParser = null;
 		
 		/* Process cmdlineArgs */
 		switch (args.length) {
 			case 2: identifiers = args[1];
 			case 1: filename = args[0];
 					cmdLineArgs = true; // we have cmd line params
 			case 0: // fall thru for default cases
 			default:
 				fileParser = new thcsw002Parser(cmdLineArgs);
 			break;
 		}
 	}
 	
 	
 	public thcsw002Parser(boolean cmdLineArgs) {
 		if (!cmdLineArgs) {
 			buildMainGUI();
 		}
 	}
 	
 	public void parser(String fileName, String identifiers) {
 		// Open the file in question
 		if (fileName == null || identifiers == null) throw new IllegalArgumentException("Can't have null args.");
 		ArrayList <IdTagObjs> idTags = new ArrayList<IdTagObjs>(); 
 		idTags.add(new IdTagObjs("Port")); // add the port identifiers
 		
 		try {
 			diskFile = new BufferedReader(new FileReader(fileName));
 			String[] inputid = identifiers.split(regex);
 			ArrayList<String> intermediateids = new ArrayList<String>();
 			// add the identifiers in the identifier data structure
 			for (String id : inputid) {
 				idTags.add(new IdTagObjs(id.trim()));
 				intermediateids.add(id.trim());
 			}
 			Object[] idobjArray = intermediateids.toArray();
 			// ids only contain what was input from the command line
 			String[] ids = Arrays.copyOf(idobjArray, idobjArray.length, String[].class);
 			
 			boolean gotFirstPortTag = false;
 //			boolean gotNextPortTag = false;
 			String line = null;
 			// TODO Rem this for csv printout
 			serverWindow.setTitle("Looking for the contents of: " + fileName);
 			
 			// Print header
 			String header = null;
 			for (IdTagObjs a : idTags) {
 				if (header == null) header = a.getIdentifierTag();
 				else header += commaDelimeter + a.getIdentifierTag();
 			}
 			printToConsole(header);
 			
 			// Parsing loop
 			while (!gotFirstPortTag) {
 				line = diskFile.readLine().trim();
 				// Get the opening port tag
 				if (!gotFirstPortTag && line.contains("Port") || line.contains("port")) {
 					line = line.substring(line.indexOf(" "), line.length());
 					idTags.get(0).setIdVal(line);
 					gotFirstPortTag = true;
 				}
 			}
 			while (true) {
 				line = diskFile.readLine().trim();
 				
 				// Check to see if we have the requisite identifiers
 				// while the line doesn't contain a reference to a port
 				while(!(line.contains("Port ") || line.contains("port "))) {
 					// Compare the identifiers to the line
 					String searchTerm = null;
 					for (int index = 0; index < ids.length; index++) {
 						searchTerm = ids[index];
 						if (line.contains(searchTerm)) {
 							line = line.substring(line.indexOf("=")+1, line.length());
 							// find the searchTerm
 							idTags.get(index+1).setIdVal(line);
 							//printToConsole(searchTerm + " "+ line);
 							break; // move on to the next line
 						}
 					}
 					
 					line = diskFile.readLine().trim();
 				}
 				// Print out the port 
 				if (line.contains("Port ") || line.contains("port ")) {
 					String output = null;
 					for (IdTagObjs a : idTags) {
 						if (output == null) output = a.getIdVal();
 						else output += commaDelimeter + a.getIdVal();
 					}
 					printToConsole(output);
 					// Set up for the next port
 					line = line.substring(line.indexOf(" "), line.length());
 					idTags.get(0).setIdVal(line);
 				}
 			}
 			
 		} 
		catch (NullPointerException npe) {}
 		catch (IOException fnfe) {
 			printToConsole("Can't open the file, make sure it's there.");
 		} 
 		
 	}
 	
 	public void printFile(String fileName) throws IOException {
 		  BufferedReader diskFile = new BufferedReader(
 		                            new FileReader(fileName));
 		  serverWindow.setTitle("The contents of " + fileName + " is:");
 		  while(true) { 
 		    try {
 		        printToConsole(diskFile.readLine().trim());
 		        }
 		    catch(IOException ioe) {
 		        //System.out.println("Listing complete.");
 		        diskFile.close();
 		        break;
 		    }
 		  }
 		}
 
 
 	private void buildMainGUI() {
 		/* Output gui */
 		serverWindow.setSize(450, 450);
 		serverWindow.setLocation(250, 250);
 		
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
 		
 		serverWindow.getContentPane().add(mainPanel, "Center");
 		serverWindow.getContentPane().add(btnPanel, "South");
 		mainPanel.add(outScrollPane);
 
 		outTextArea.setEditable(true); // Just make this a display text area
 		
 //		btnPanel.add(closeButton);
 //		closeButton.addActionListener(this); // make sure button closes gui
 //		btnPanel.add(saveButton);
 //		saveButton.addActionListener(this);
 		
 		/* Create the menuBar */
 		createFileMenu();
 		createViewMenu();
 		
 		JMenuBar menuBar = new JMenuBar();
 		serverWindow.setJMenuBar(menuBar);
 		menuBar.add(fileOptions);
 		menuBar.add(viewOptions);
 		
 		printToConsole("Open a file to parse by clicking File > Open File...");
 		serverWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		serverWindow.setVisible(true);
 	}
 	
 	private void createFileMenu() {
 		fileOptions = new JMenu("File");
 		openNewFileItem = new JMenuItem("Open File...");
 		saveExisitingFileItem = new JMenuItem("Save");
 		exitProgramItem = new JMenuItem("Close Program");
 		
 		openNewFileItem.addActionListener(this);
 		saveExisitingFileItem.addActionListener(this);
 		exitProgramItem.addActionListener(this);
 		
 		fileOptions.add(openNewFileItem);
 		fileOptions.add(saveExisitingFileItem);
 		fileOptions.addSeparator();
 		fileOptions.add(exitProgramItem);
 	}
 	
 	private void createViewMenu() {
 		viewOptions = new JMenu("View");
 		viewFileItem = new JMenuItem("View File");
 		
 		viewFileItem.addActionListener(this);
 		viewOptions.add(viewFileItem);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == openNewFileItem) {
 			String filename = null;
 			String identifiers = null;
 			
 			JFileChooser jfc = new JFileChooser();
 			int retval = jfc.showOpenDialog(jfc);
 			
 			if (retval == JFileChooser.APPROVE_OPTION) {
 				filename = jfc.getSelectedFile().getAbsolutePath();
 			}
 			else {
 				JOptionPane.showMessageDialog(null, "You must choose a file name.");
 			}
 			
 			while (identifiers == null || identifiers.length() == 0 ) {
 				identifiers = JOptionPane.showInputDialog(null, 
 						"Input tokens separated by semicolons, type \"Exit\" without a semicolon "
 						+ "to exit program, or \"Cancel\" to cancel the process.", 
 						"rxByteCount;txByteCount;rxHCOctets;txHCOctets");
 			}
 			/* figure out what tokens we have */
 			if (identifiers.compareToIgnoreCase("exit") == 0) {
 				JOptionPane.showMessageDialog(null, "Exiting the program.");
 				System.exit(0);
 			}
 			else if (identifiers.compareToIgnoreCase("cancel") == 0); // just don't do anything
 			else {	/* parse the file */
 				outTextArea.setText("");
 				parser(filename, identifiers);
 			}
 		}
 		if (ae.getSource() == exitProgramItem) {
 			int retVal = JOptionPane.showConfirmDialog(serverWindow, "Do you want to save file before exiting?", "Closing program", JOptionPane.YES_NO_CANCEL_OPTION);
 			try {
 				switch (retVal) {
 					case JOptionPane.YES_OPTION:
 						 printToFile(); // Fall thru to close out
 					case JOptionPane.NO_OPTION:
 						System.exit(0); // terminate app
 						break;
 					default: // if they closed out or cancelled
 						break;
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				JOptionPane.showMessageDialog(serverWindow, "Couldn't save to file, try again...");
 			} // print to file 
 			
 		}
 		if (ae.getSource() == saveExisitingFileItem) {
 			try {
 				printToFile();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				JOptionPane.showMessageDialog(serverWindow, "Couldn't save to file, try again...");
 			} // print to file 
 		}
 		if (ae.getSource() == viewFileItem) {
 			try {
 				String filename = null;
 				JFileChooser jfc = new JFileChooser();
 				int retval = jfc.showOpenDialog(jfc);
 				
 				if (retval == JFileChooser.APPROVE_OPTION) {
 					filename = jfc.getSelectedFile().getAbsolutePath();
 				}
 				else {
 					JOptionPane.showMessageDialog(null, "You must choose a file name.");
 				}
 				outTextArea.setText(""); // clear screen
 				printFile(filename);
 			} catch (NullPointerException npe) {} // reached eof 
 			catch (IOException ioe) {
 				JOptionPane.showMessageDialog(serverWindow, "Couldn't open the file, try again...");
 			}
 		}
 	}
 	
 	private void printToConsole (String msg) {
 		outTextArea.append(msg + newLine);
 		vsb.setValue(vsb.getMaximum());
 		outTextArea.setCaretPosition(outTextArea.getDocument().getLength());
 	}
 	
 	private void printToFile() throws IOException {
 		JFileChooser saveFile = new JFileChooser();
 		int retval = saveFile.showSaveDialog(saveFile);
 		
 		String fileToSave = null;
 		String fileData = null;
 		if (retval == JFileChooser.APPROVE_OPTION) {
 			fileToSave = saveFile.getSelectedFile().getAbsolutePath();
 			fileData = outTextArea.getText();
 			BufferedWriter diskFile = new BufferedWriter(new FileWriter(fileToSave));
 			diskFile.write(fileData);
 			diskFile.close();
 			JOptionPane.showMessageDialog(serverWindow, "Done Saving to " + fileToSave);
 		}
 		else {
 			JOptionPane.showMessageDialog(null, "Data not saved to file.");
 		}
 	}
 }
