 package phm1.NewJ;
 
 import java.awt.*;
 import java.io.IOException;
 
 import javax.swing.*;
 import javax.swing.filechooser.*;
 
 /**
  * This class mainly holds links to all of the GUI elements, so we don't end up passing round hundreds of different panel objects and things. It also implements some features that can be called upon from various parts of the GUI.
  * @author n3hima
  *
  */
 public class GUI {
 	// TODO clean this up. Put things in the relevant JPanel objects if they don't coordinate the whole thing. - J
 	private Model model;
 	private DiagramPanel dPanel;
 	private ButtonPanel bPanel;
 	private MyMouseListener mouseListener;
 	private Menus menus;
 	private MyMenuListener menuListener;
 	private MainFrame mainFrame;
 	private JFileChooser saveLoadChooser;
 	private JFileChooser exportChooser;
 	
 	public Model getModel() {
 		return model;
 	}
 
 	public DiagramPanel getdPanel() {
 		return dPanel;
 	}
 
 	public ButtonPanel getbPanel() {
 		return bPanel;
 	}
 
 	public MyMouseListener getMouseListener() {
 		return mouseListener;
 	}
 
 	public Menus getMenus() {
 		return menus;
 	}
 
 	public MyMenuListener getMenuListener() {
 		return menuListener;
 	}
 	
 	public MainFrame getMainFrame(){
 		return mainFrame;
 	}
 
 	public JFileChooser getSaveLoadChooser() {
 		return saveLoadChooser;
 	}
 
 	public JFileChooser getExportChooser() {
 		return exportChooser;
 	}
 
 	/**
 	 * Constructs a GUI object with a linked Model
 	 * @param m The Model to link with the GUI
 	 */
 	public GUI(Model m) {
 		this.model = m;
 	}
 	
 	/**
 	 * Adds a class to be displayed
 	 * @param c The class to be added
 	 */
 	public void addClass(NJClass c) {
 		model.addClass(c);
 		dPanel.repaint();
 	}
 	
 	/**
 	 * Checks to see if a class name is already in use by a displayed class -- wrapper function for the equivalent method in Model
 	 * @param name
 	 * @return True if there is a name collision
 	 */
 	public boolean classNameInUse(String name){
 		return model.getClassByName(name) != null;
 	}
 	
 	/**
 	 * Set up the singleton ready to manage links to everything
 	 */
 	public void initialise(){
 		//initalise everything - p
 		model = new Model();
 		dPanel = new DiagramPanel(this);
 		bPanel = new ButtonPanel(this);
 		menus = new Menus(this);
 		mouseListener= new MyMouseListener(this);
 		mainFrame = new MainFrame(this);
 		populateEditMenu();
 		saveLoadChooser = new JFileChooser();
 		saveLoadChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		saveLoadChooser.setFileFilter(new FileNameExtensionFilter("XML files (.xml)", "xml"));
 		exportChooser = new JFileChooser();
 		exportChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		mainFrame.setVisible(true);
 	}
 	
 	/**
 	 * Draws all the classes and the connections between them
 	 * @param g
 	 */
 	public void drawAll(Graphics g) {
 		for (NJClass c : model.getClasses()) {
 			c.drawConnections(g);
 		}
 		for (NJClass c : model.getClasses()) {
 			c.draw(g, c == dPanel.getSelected());
 		}
 	}
 	
 	/**
 	 * Resets everything, Model included
 	 */
 	public void deleteAll() {
 		model.clear();
 		dPanel.unselectAll();
 		dPanel.repaint();
 		populateEditMenu();
 	}
 	
 	/**
 	 * Removes the selected class from the model and repaints the GUI
 	 */
 	public void deleteSelected() {
 		model.removeClass(dPanel.getSelected());
 		dPanel.unselectAll();
 		dPanel.repaint();
 		populateEditMenu();
 	}
 	
 	/**
 	 * Returns the class box under the coordinates passed
 	 * @param x The x-coordinate
 	 * @param y The y-coordinate
 	 * @return The relevant NJClass object, or null
 	 */
 	public NJClass clickedInBox(int x, int y) {
 		//finds the box that has been clicked on, if any - p
 		int xPlusA;
 		int yPlusB;
 		for (NJClass c : model.getClasses()){ //loop through boxes - p
 
 			xPlusA = c.getX()+c.getA();
 			yPlusB = c.getY()+c.getB();
 			
 			if ((c.getX() < x) && (x < xPlusA) && (c.getY() < y) && (y < yPlusB)) {
 				// We found it! Let's get outta here - J
 				return c;
 			}
 		}
 		// Just return null if we didn't return earlier - J
 		return null;
 	}
 	
 	/**
 	 * Fills the Edit menu with the relevant things to the specified class
 	 * @param c The specified class
 	 */
 	public void populateEditMenu(NJClass c){
 		getMenus().getEditMenu().populate(c);
 	}
 	
 	/**
 	 * Fills the Edit menu with the relevant things to the selected class. If no class is selected, disable the Edit menu.
 	 */
 	public void populateEditMenu(){
 		if(dPanel.getSelected() != null){
 			populateEditMenu(dPanel.getSelected());
 		} else {
 			getMenus().getEditMenu().populate();
 		}
 	}
 	
 	/**
 	 * Pop up a file choose dialog to choose an XML file to load a saved state from
 	 */
 	public void chooseAndLoad(){
 		if(this.saveLoadChooser.showOpenDialog(this.mainFrame) == JFileChooser.APPROVE_OPTION){
 			try {
 				this.model.load(this.saveLoadChooser.getSelectedFile());
 				dPanel.repaint();
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(this.mainFrame, "There was an error loading the file.", "IOException", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 
 	/**
 	 * Pop up a file choose dialog to choose an XML file to save a state to
 	 */
 	public void chooseAndSave(){
 		if(this.saveLoadChooser.showSaveDialog(this.mainFrame) == JFileChooser.APPROVE_OPTION){
 			try {
 				this.model.save(this.saveLoadChooser.getSelectedFile());
 				dPanel.repaint();
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(this.mainFrame, "There was an error saving to the file.", "IOException", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 
 	/**
 	 * Pop up a file choose dialog to choose a directory to export .java files to
 	 */
 	public void chooseAndExport(){
 		if(this.exportChooser.showDialog(this.mainFrame, "Export") == JFileChooser.APPROVE_OPTION){
 			try {
 				this.model.export(this.exportChooser.getSelectedFile());
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(this.mainFrame, "There was an error exporting the project. Are you exporting to a real directory?", "IOException", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 	
 	/**
 	 * Pops up a dialog box asking for a new class name, filled with an unused "Untitled" name by default
 	 * @return The class name specified by the user
 	 */
 	private String classNamePrompt(){
 		// Dialog box to get the new class name
 		return JOptionPane.showInputDialog("Enter class name:", "Untitled" + Integer.toString(getModel().getClassCount() + 1));
 	}
 	
 	/**
 	 * Prompts the user for a name and adds a new class to the Model, repainting the GUI afterwards
 	 */
 	public void newClass(){
 		String className = this.classNamePrompt();
 		if(className == null)
 			return;
 		while(this.classNameInUse(className)){
 			JOptionPane.showMessageDialog(this.mainFrame, "That class name is already in use.", "Error", JOptionPane.ERROR_MESSAGE);
 			className = this.classNamePrompt();
			if(className == null)
				return;
 		}
 		
 		NJClass c = new NJClass(className, 100, 100);
 		addClass(c);
 	}
 }
