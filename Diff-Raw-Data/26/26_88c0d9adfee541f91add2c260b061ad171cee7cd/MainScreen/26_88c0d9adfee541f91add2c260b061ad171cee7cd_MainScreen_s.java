 package RegionBuilder;
 
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import ActiveSpace.Coord;
 import ActiveSpace.Region;
 import ActiveSpace.Space;
import ProcessingKinect.KinectSensor;
 
 /**
  * This class implements all of the GUI widgetry
  */
 public class MainScreen extends JFrame 
 						implements	ActionListener, 
 									WindowListener {
 	
 	public boolean finished;	// have we been told to shut down
 	
 	private Space space;		// space for regions and rules
 	private int debugLevel;		// desired level of diagnostic output
 	private boolean changes;	// have we made any changes
 	private String regionFile;	// name of the region file
 	
 	private static final long serialVersionUID = 0xdeadbeef;	// this is stupid
 
 	// main panel GUI widgets
 	private Container mainPane;
 	private JTextField pos_x;
 	private JTextField pos_y;
 	private JTextField pos_z;
 	private JTextField radius;
 	private JTextField regionName;
 	private JTextField spaceName;
 	private JMenuItem fileSaveAs;
 	private JMenuItem fileExit;
 	private JMenuItem fileSave;
 	private JMenuItem fileLoadRegions;
 	private JButton createButton;
 	
 	private JFileChooser chooser;
 	private FileNameExtensionFilter xmlFilter;
 	
 	// pseudo-tunables
 	private static final int B = 10;			//	default widget border
 	private static final int NAME_WIDTH = 20;	// default text field
 	private static final int POS_WIDTH = 5;		// default coordinate width
 	
 	private static final int MAX_POS = 10000;	// nothing is that far away
 	
 
 	/**
 	 * create all the main-screen widgetry
 	 * 
 	 * @param s		Space containing the regions and rules
 	 * @param path	to directory containing media files
 	 */
 	public MainScreen( String regionFile, int debug )  {
 		this.space = new Space();		// create an ActiveSpace
 		this.debugLevel = debug;		// figure out how verbose to be
 		space.debug(debugLevel);		// only the most basic debug info
 		this.finished = false;
 		
 		// get a handle on our primary window and capture control events
 		mainPane = getContentPane();
 		addWindowListener( this );		// so I can handle window close events
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		
 		// create the main panel widgetry
 		createMenus();			// create the menu hierarchy
 		createEditor();			// create the rule editor panel
 		setVisible( true );		// make this all visible
 		pack();					// validate widgets, compute display size
 		
 		// set up a single file chooser (so we can remember directories)
 		chooser = new JFileChooser();
 		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		xmlFilter = new FileNameExtensionFilter("XML", "xml", "XML");
 		chooser.addChoosableFileFilter(xmlFilter);
 		
 		// initialize the space we'll be working on
 		if (regionFile != null)
 			loadRegions(regionFile);
 		this.regionFile = regionFile;
 		
 		changes = false;			// no changes have yet been made
 	}
 	
 	
 	/**
 	 * create the hierarchy of menus that will drive most 
 	 * of our actions
 	 */
 	private void createMenus() {
 		
 		// create File menu
 		fileLoadRegions = new JMenuItem("Load Regions");
 		fileLoadRegions.addActionListener(this);
 		fileSave = new JMenuItem("Save");
 		fileSave.addActionListener(this);
 		fileSaveAs = new JMenuItem("Save as");
 		fileSaveAs.addActionListener(this);
 		fileExit = new JMenuItem("Exit");
 		fileExit.addActionListener(this);
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.add(fileLoadRegions);
 		fileMenu.add(fileSave);
 		fileMenu.add(fileSaveAs);
 		fileMenu.add( new JSeparator() );
 		fileMenu.add(fileExit);
 		
 		// assemble the menu bar
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(fileMenu);
 		setJMenuBar( menuBar );	
 	}
 	
 	/**
 	 * create the controls for entering new rules
 	 */
 	private void createEditor() {
 		JPanel e;	// the main panel for the editor
 		JPanel h;	// 2nd level horizontal panels
 		JPanel v;	// 3rd level vertical
 		JPanel h2;	// 4th level horizontal
 		
 		// create the editor panel
 		e = new JPanel();
 		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
 		e.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
 		
 		// create the first row: space name
 		h = new JPanel();
 		h.setLayout(new BoxLayout(h, BoxLayout.X_AXIS));
 		h.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
 		h.add(new JLabel("Space name"));
 		spaceName = new JTextField(NAME_WIDTH);
 		h.add(spaceName);
 		e.add(h);
 		
 		// create the next row: region name, position, radius
 		h = new JPanel();
 		h.setLayout(new BoxLayout(h, BoxLayout.X_AXIS));
 		h.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
 		
 		v = new JPanel();
 		v.setLayout(new BoxLayout(v, BoxLayout.Y_AXIS));
 		v.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
 		regionName = new JTextField(NAME_WIDTH);
 		v.add(regionName);
 		v.add(new JLabel("Region name"));
 		h.add(v);
 		
 		v = new JPanel();
 		v.setLayout(new BoxLayout(v, BoxLayout.Y_AXIS));
 		v.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
 		h2 = new JPanel();
 		h2.setLayout(new BoxLayout(h2, BoxLayout.X_AXIS));
 		pos_x = new JTextField(POS_WIDTH);
 		pos_x.setEditable(false);
 		h2.add(pos_x);
 		pos_y = new JTextField(POS_WIDTH);
 		pos_y.setEditable(false);
 		h2.add(pos_y);
 		pos_z = new JTextField(POS_WIDTH);
 		pos_z.setEditable(false);
 		h2.add(pos_z);
 
 		v.add(h2);
 		v.add(new JLabel("<x,y,z> (mm)"));
 		h.add(v);
 		
 		v = new JPanel();
 		v.setLayout(new BoxLayout(v, BoxLayout.Y_AXIS));
 		v.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
 		radius = new JTextField("");
 		v.add(radius);
 		v.add(new JLabel("radius (mm)"));
 		h.add(v);
 		e.add(h);
 		
 		// create second row: create button
 		h = new JPanel();
 		createButton = new JButton("Create Region");
 		createButton.addActionListener(this);
 		h.add(createButton);
 		
 		e.add(h);	// attach this row to the editor panel
 		
 		// and add it all to the main pane
 		mainPane.add(e, BorderLayout.SOUTH);
 	}
 	
 	/**
 	 * We have a single action listener for all of the interactive widgets,
 	 * but all of the non-widget handling is done in per-operation subroutines.
 	 */
 	public void actionPerformed( ActionEvent e ) {
 		Object o = e.getSource();
 		
 		// menu->File->load regions
 		if (o == fileLoadRegions) {
 			chooser.setFileFilter(xmlFilter);
 			int retval = chooser.showOpenDialog(this);
 			if (retval == JFileChooser.APPROVE_OPTION) {
 				String filename = chooser.getSelectedFile().getAbsolutePath();
 				String correctedName = canonize(filename);
 				loadRegions(correctedName);
 			} 
 			return;
 		}
 		
 		// menu->File->save
 		if (o == fileSave) {
 			saveRegions(regionFile);
 			return;
 		}
 		
 		// menu->File->saveAs
 		if (o == fileSaveAs) {
 			saveRegions(null);
 			return;
 		}
 		
 		// menu->File->exit
 		if (o == fileExit) {
 			shutdown();
 			return;
 		}
 		
 		// create image button pressed
 		if (o == createButton) {
 			createRegion();
 			return;
 		}
 	}
 	
 	/**
 	 * copy a set of room coordinates into the editor window
 	 * 
 	 * @param c coordinates to set
 	 */
 	public void setCoord( Coord c ) {
 		// copy these into the editor window
 		pos_x.setText("" + (int) c.x);
 		pos_y.setText("" + (int) c.y);
 		pos_z.setText("" + (int) c.z);
 	}
 
 	/* 
 	 * We have a full complement of WindowListener entry points, but
 	 * the only one we actually care about is the "closing" (he clicked the X),
 	 * which we catch so that we can confirm before shutting down.
 	 */
 	public void windowClosing(WindowEvent e) { shutdown(); }
 	public void windowActivated(WindowEvent arg0) {	}
 	public void windowClosed(WindowEvent arg0) {}
 	public void windowDeactivated(WindowEvent arg0) {}
 	public void windowDeiconified(WindowEvent arg0) {}
 	public void windowIconified(WindowEvent arg0) {}
 	public void windowOpened(WindowEvent arg0) {}
 	
 	/*
 	 * the routines that follow implement the real operations
 	 * on the space (loading, saving and creating regions and rules)
 	 */
 	
 	/**
 	 * load a set of regions into our space
 	 * 
 	 * @param filename
 	 */
 	private void loadRegions( String filename ) {
 		try {
 			// load in the regions file
 			space.readRegions(filename);
 			regionFile = filename;
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog( mainPane, 
 					e.getMessage(),
 					"ERROR LOADING REGION DEFINITIONS",
 					JOptionPane.ERROR_MESSAGE);
 			// e.printStackTrace();
 		}
 		
 		// see if we got a space name
 		String s = space.name();
 		if (s != null)
 			spaceName.setText(s);
 	}
 	
 	
 	/**
 	 * process a shut-down request, prompting for save if appropriate
 	 */
 	private void shutdown() {
 		while (changes) {
 			int choice = JOptionPane.showConfirmDialog(mainPane, "Save changes?");
 			if (choice == JOptionPane.CANCEL_OPTION)
 				return;
 			if (choice == JOptionPane.YES_OPTION)
 				saveRegions(null);
 			if (choice == JOptionPane.NO_OPTION)
 				break;
 		}
 		
 		this.finished = true;	// tell the main loop we're done
 	}
 	
 	/**
 	 * select an output file and save to it
 	 * 
 	 * side effect: save clears the changes boolean
 	 */
 	private void saveRegions(String outputFile) {
 		
 		if (outputFile == null) {
 			// select an output file
 			chooser.setFileFilter(xmlFilter);
 			chooser.setSelectedFile( new File( "Regions.xml" ));
 			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
 				outputFile = chooser.getSelectedFile().getAbsolutePath();
 			else
 				return;
 		}
 		
 		// if we have a space name, set it
 		String s = spaceName.getText();
 		if (s != null)
 			space.name(s);
 		
 		// save the current rule set to it
 		if (debugLevel > 0)
 			System.out.println("saving regions to " + outputFile);
 		FileWriter output;
 		try {
 			output = new FileWriter( outputFile );
 			output.write(space.regionsToXML());
 			output.close();
 			changes = false;
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog( mainPane, 
 					e.getMessage(),
 					"ERROR UPDATING FILE",
 					JOptionPane.ERROR_MESSAGE);
 
 		}
 	}
 	
 	/**
 	 * pull the values out of the editor panel to create a new region
 	 */
 	private void createRegion() {
 
 		try {
 			int x = getPosition(pos_x.getText());
 			int y = getPosition(pos_y.getText());
 			int z = getPosition(pos_z.getText());
 			Coord c = new Coord((float) x, (float) y, (float) z);
 			int r = getPosition(radius.getText());
 			String name = regionName.getText();
 			if (name == null || name.length() < 1)
 				throw new IllegalArgumentException("no region name specified");
 			Region region = new Region(name, c, (float) r);
 			space.addRegion(region);
 			changes = true;
 		} catch (NumberFormatException e) { 
 			System.out.println("Non-integer size/position: " + e);
 		} catch (IllegalArgumentException e) {
 			System.out.println("Invalid argument: " + e);
 		}
 	}
 	
 	/**
 	 * determine whether or not a parameter value is plausible
 	 */
 	private static int getPosition( String s ) 
 			throws NumberFormatException, IllegalArgumentException {
 		
 		// it must parse as an integer
 		int value = Integer.parseInt(s);
 		
 		// sanity check the distance
 		if (value > MAX_POS || value < -MAX_POS)
 			throw new IllegalArgumentException("excessive distance: " + value);
 		
 		return value;
 	}
 
 	/**
 	 * KLUGE - The file name selection dialogs return local
 	 * 		file names, but the region/rule readers need URI's,
 	 * 		and Window's file names don't qualify.  A little 
 	 * 		research failed to turn up a portable converter,
 	 * 		so I am (for now) kluging my own.
 	 * 
 	 * @param original	local file name
 	 * @return	new form, suitable for use as a URI
 	 */
 	String canonize(String original) {
 		
 		// replace any back-slashes with slashes
 		String fixed = original.replaceAll("\\\\", "/");
 		
 		if (!original.equals(fixed) && debugLevel > 1)
 			System.out.println("canonize(" + original + ") = " + fixed);
 		
 		return( fixed );
 	}
 
 }
