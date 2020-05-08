 package gui;
 
 import util.Util;
 
 
 import java.awt.*;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import javax.swing.*;
 import javax.swing.JFileChooser;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class ExploreWindow extends JFrame implements ActionListener {
 	
 	private static final long serialVersionUID = -735099555260969783L;
 	
 	/**
 	 * The type of file explore data is exported to
 	 */
 	public static final String EXPORT_FILE_EXTENSION 	= ".csv";
 	
 	public static final String START_EXPLORE_STRING 	= "start";
 	public static final String SIMULATE_STRING			= "simulate";
 	public static final String CLEAR_STRING				= "clear";
 	public static final String EXPORT_STRING			= "export";
 
 	/**
 	 * The actual explore window
 	 */
 	public JFrame window;
 	
 	/**
 	 * Text box where results are displayed
 	 */
 	public JTextArea txtBox;
 	
 	/**
 	 * Buttons for explore, simulate, and clear
 	 */
 	protected static JButton b1, b2, b3;
 	
 	/**
 	 * Checkbox to toggle exporting to CSV file
 	 */
 	protected static JCheckBox export;
 	
 	/**
 	 * The main Container Panel holding the panels
 	 */
 	private ContainerPanel cPanel;
 	
 	/**
 	 * Explorer object will do the actual explore
 	 */
 	private Explorer explorer;
 	
 	/**
 	 * This menu is disabled by the explorer when the exploration
 	 * starts
 	 */
 	private JMenuBar menuBar;
 	
 	/**
 	 * True if currently exploring
 	 */
 	private boolean exploring = false;
 	
 	/**
 	 * True if we want to export the explore to a file
 	 */
 	private boolean exportExplore = false;
 	
 	/**
 	 * The file to export the explore to
 	 */
 	private String exportFile = "";
 	
 	
 //---------------------------------------------------------------------------------------
 // 	PUBLIC METHODS
 	
 	/**
 	 * Creates a new explore window
 	 * @param x The XPosition of the window
 	 * @param y The YPosition of the window
 	 * @param Panel The Container Panel Model
 	 * @param menu The menu of the main window
 	 */
 	public ExploreWindow(int x, int y, ContainerPanel Panel, JMenuBar menu) {
 		super("Explore Window");
 		menuBar = menu;
 		cPanel = Panel;
 		initializeGUI(x, y);
 	}
 	
 	/**
 	 * The explore thread is designed to make sure that the program's menu's
 	 * stay free so that it is possible to stop the explore while it is running.
 	 */
 	class exploreThread implements Runnable {
 		ExploreWindow exp;
 		exploreThread(ExploreWindow current) {
 			exp = current;
 			Thread t = new Thread(this);
 			t.start();
 		}
 		
 		public void run() {
 			try 
 			{
 				explorer = new Explorer(cPanel, menuBar);
 				if (exportExplore)
 				{
 					explorer.setExport(true);
 					explorer.initializeBufferedWriter(exportFile);
 				}
 				
 				new exploreUpdateThread (explorer, exp);
 				
 				explorer.explore();
 				
 				exp.update(explorer.numCauses, explorer.numAllows, 
 						explorer.numHelps, explorer.numPrevents, explorer.numDespites, 
 						explorer.numInvalids, explorer.num_accepted, explorer.attempts, 
 						explorer.timeTaken, explorer.premises, explorer.overallAffector, 
 						explorer.overallPatient, explorer.eZero);
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * exploreUpdateThread is designed to update the explore
 	 * progress in the explore window every .5 second or so.
 	 */
 	class exploreUpdateThread implements Runnable {
 		Explorer explorer;
 		ExploreWindow expw;
 		exploreUpdateThread(Explorer exp, ExploreWindow _expW) {
 			explorer = exp;
 			expw = _expW;
 			Thread t = new Thread(this);
 			t.start();
 		}
 		
 		public void run() {
 			try 
 			{
				while (explorer.aPercDone != 100)
 				{
 					expw.update(explorer.aPercDone, explorer.timeTaken, 
 							explorer.timeRemaining);
 					Thread.sleep(500);
 				}
 			}
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Updates the time remaining during the course of the explore
 	 * @param perc Percentage complete
 	 * @param taken Time taken
 	 * @param remain Time remaining
 	 */
 	public void update(int perc, String taken, String remain) {
 		setTitle("[" + perc + "%]  Remaining: " + remain);
 	}
 	
 	/**
 	 * Writes the post analysis of and explore to the Explore Window text box
 	 */
 	public void update(long numCauses, long numAllows, long numHelps, long numPrevents, long numDespites,
 			long numInvalids, long total, long attempts, String elapsed, String premises, String overallAffector, String overallPatient, int eZero) {
 		
 		setTitle("Explore Window");
 		
 		DecimalFormat df = new DecimalFormat("#.##");
 		if(numCauses > 0)  txtBox.append(overallAffector+" Cause   "+overallPatient +", "+ df.format(100.0 * (double)numCauses / (double)total) + "%, "+ numCauses + "  \n");
 		if(numAllows > 0)  txtBox.append(overallAffector+" Allow   "+overallPatient +", "+ df.format(100.0 * (double)numAllows / (double)total) + "%, "+numAllows + "  \n");
 		if(numHelps > 0)   txtBox.append(overallAffector+" Help    "+overallPatient +", "+ df.format(100.0 * (double)numHelps / (double)total) + "%, "+numHelps + "  \n");
 		if(numPrevents > 0)txtBox.append(overallAffector+" Prevent "+overallPatient +", "+ df.format(100.0 * (double)numPrevents / (double)total) + "%, "+numPrevents + "  \n");
 		if(numDespites > 0)txtBox.append(overallAffector+" Despite "+overallPatient +", "+ df.format(100.0 * (double)numDespites / (double)total) + "%, "+numDespites + "  \n");
 		if(numInvalids > 0)txtBox.append(overallAffector+" Invalid "+overallPatient +", "+ df.format(100.0 * (double)numInvalids / (double)total) + "%, "+numInvalids + "  \n");
 		txtBox.append("\nTotal Accepted  " + total);
 		txtBox.append("\nTotal Attempted " + attempts);
 		txtBox.append("\nE-Magnitude " + eZero + " \n\n");
 		txtBox.append("Elapsed Time: " + elapsed + " \n");
 		txtBox.append("Premises: " + premises + "\n");
 		txtBox.append("\n------------------------\n\n");
 
 		txtBox.setCaretPosition(0);
 		b1.setEnabled(true);
 		b3.setEnabled(true);
 		exploring = false;
 		b1.setText("Start Explore");
 	}
 	
 	/**
 	 * Called when a button on the window is clicked
 	 */
 	public void actionPerformed(ActionEvent e) {
 		// Explore Requested
 	    if (START_EXPLORE_STRING.equals(e.getActionCommand()) && !exploring) {
 	    	startExplore();
 
 	    // Stop Explore Requested
 	    } else if(START_EXPLORE_STRING.equals(e.getActionCommand()) && exploring) {
 	    	stopExplore();
 	    	
 	    // Simulate Requested
 	    } else if(SIMULATE_STRING.equals(e.getActionCommand())){
 	    	new SimulateWindow(this.getX() + this.getWidth(), this.getY(), cPanel, this);
 	    	
 	    // Toggle export requested
 	    } else if(EXPORT_STRING.equals(e.getActionCommand())) {
 	    	if (!exportExplore)
 	    	{
 		    	JFileChooser chooser = Util.chooseFile (EXPORT_FILE_EXTENSION, 
 						"Comma Seperated Value (*" + EXPORT_FILE_EXTENSION + ")");
 				
 				int returned = chooser.showSaveDialog(window);
 				
 				if (returned == JFileChooser.APPROVE_OPTION) {
 					String absPath = chooser.getSelectedFile().getAbsolutePath();
 					if (!absPath.toLowerCase().endsWith(EXPORT_FILE_EXTENSION)) 
 						absPath += EXPORT_FILE_EXTENSION;
 					exportFile = absPath;
 					exportExplore = true;
 				}
 				else
 				{
 					exportExplore = false;
 					export.setSelected(false);
 				}
 	    	}
 	    	else // Deselect export requested
 	    	{
 	    		exportExplore = false;
 	    	}
 	    	
 	    // Clear requested
 	    } else if(CLEAR_STRING.equals(e.getActionCommand())){
 	    	txtBox.setText("");
 	    }
 	}
 	
 	/**
 	 * If true we desire to export the explore
 	 */
 	public boolean getExportExplore ()
 	{
 		return exportExplore; 
 	}
 	
 	
 //---------------------------------------------------------------------------------------
 // 	PRIVATE METHODS	
 	
 	
 	/**
 	 * Sets up and initializes the window.
 	 */
 	private void initializeGUI(int x, int y) {
 		Container pane = getContentPane();
 		setLocation(x,y);
 		
 		createButtons();
 		
 		JScrollPane areaScrollPane = createTextArea();
 
         //Add Components to the container
         JPanel buttons = new JPanel(new BorderLayout());
         buttons.add(b1, BorderLayout.WEST);
         buttons.add(b2, BorderLayout.CENTER);
         buttons.add(b3, BorderLayout.EAST);
         
         JPanel radioButtons = new JPanel(new BorderLayout());
 
         JPanel bottom = new JPanel(new BorderLayout());
         bottom.add(radioButtons, BorderLayout.WEST);
         bottom.add(export, BorderLayout.EAST);
         
         pane.add(buttons, BorderLayout.NORTH);
         pane.add(areaScrollPane, BorderLayout.CENTER);
         pane.add(bottom, BorderLayout.SOUTH);
 		
 		pack();
 		setVisible(true);
 	}
 	
 	/**
 	 * Adds the Start Explore, Simulate, Clear, and Export
 	 * buttons to the model.
 	 */
 	private void createButtons ()
 	{
 		b1 = new JButton("Start Explorer");
 		b1.setPreferredSize(new Dimension(125,25));
         b1.setVerticalTextPosition(AbstractButton.CENTER);
         b1.setHorizontalTextPosition(AbstractButton.LEADING);
         b1.setActionCommand(START_EXPLORE_STRING);
         
         b2 = new JButton("Simulate");
         b2.setPreferredSize(new Dimension(125,25));
         b2.setVerticalTextPosition(AbstractButton.BOTTOM);
         b2.setHorizontalTextPosition(AbstractButton.CENTER);
         b2.setActionCommand(SIMULATE_STRING);
         
         b3 = new JButton("Clear");
         b3.setPreferredSize(new Dimension(125,25));
         b3.setVerticalTextPosition(AbstractButton.BOTTOM);
         b3.setHorizontalTextPosition(AbstractButton.TRAILING);
         b3.setActionCommand(CLEAR_STRING);
         
         export = new JCheckBox("Export Explore");
         export.setActionCommand(EXPORT_STRING);
         export.setSelected(false);
         
         export.addActionListener(this);
 
         //Listen for actions on buttons 1 and 3.
         b1.addActionListener(this);
         b2.addActionListener(this);
         b3.addActionListener(this);
 	}
 	
 	/**
 	 * Creates the text area for the resuts to be printed to
 	 * @return areaScrollPane The scrolling text box
 	 */
 	private JScrollPane createTextArea ()
 	{
 		txtBox = new JTextArea();
         txtBox.setEditable(false);
         
         txtBox.setFont(Font.decode("Courier New"));
         
         
         JScrollPane areaScrollPane = new JScrollPane(txtBox);
         areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         areaScrollPane.setPreferredSize(new Dimension(350,300));
         areaScrollPane.setBorder(
             BorderFactory.createCompoundBorder(
                 BorderFactory.createCompoundBorder(
                                 BorderFactory.createTitledBorder("Explore Results"),
                                 BorderFactory.createEmptyBorder(5,5,5,5)),
                 areaScrollPane.getBorder()));
         
         return areaScrollPane;
 	}
 	
 	/**
 	 * Starts the explore process
 	 */
 	private void startExplore ()
 	{
 		new exploreThread(this);
    	setTitle("[0%]");
    	exploring = true;
     	b1.setText("Stop Explore");
 	}
 	
 	/**
 	 * Stops the explore process
 	 */
 	private void stopExplore ()
 	{
 		explorer.stopExplore();
     	exploring = false;
     	b1.setText("Start Explore");
 	}
 }
