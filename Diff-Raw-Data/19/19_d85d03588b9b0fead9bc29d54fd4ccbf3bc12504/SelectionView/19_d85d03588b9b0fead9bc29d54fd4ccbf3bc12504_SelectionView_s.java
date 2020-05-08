 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 
 
 public class SelectionView {
 
 	//Creates all needed JMenuItems
 	private JMenuItem jmiLoad = new JMenuItem("Load Scholarship");
 	private JMenuItem jmiSave = new JMenuItem("Save Scholarship");
 	private JMenuItem jmiImport = new JMenuItem("Import Scholarship");
 	private JMenuItem jmiExport = new JMenuItem("Export Scholarship");
 	private JMenuItem jmiExit = new JMenuItem("Exit");
 	private JMenuItem jmiTP = new JMenuItem("Type of Publicataion");
 	private JMenuItem jmiPY = new JMenuItem("Publications Per Year");
 	private JMenuItem jmiCPY = new JMenuItem("Conference Papers Per Year");
 	private JMenuItem jmiJAY = new JMenuItem("Journal Articles Per Year");
 	private JMenuItem jmiCA = new JMenuItem("Coauthors Per Publication");
 	
 	//Creates all JMenus
 	private JMenu jmFile = new JMenu("File");
 	private JMenu jmPlot = new JMenu("Plot");
 	
 	//Creates all JButtons
 	private JButton jbAddScholar = new JButton("Add Scholar");
 	private JButton jbDeleteSelectedScholar = new JButton("Delete Selected Scholar(s)");
 	private JButton jbDeleteAllScholar = new JButton("Delete All Scholars");
 	private JButton jbAddSerial = new JButton("Add Serial");
 	private JButton jbDeleteSelectedSerial = new JButton("Delete Selected Serial(s)");
 	private JButton jbDeleteAllSerial = new JButton("Delete All Serials");
 	private JButton jbAddPaper = new JButton("Add Paper");
 	private JButton jbDeleteSelectedPaper = new JButton("Delete Selected Paper(s)");
 	private JButton jbDeleteAllPaper = new JButton("Delete All Papers");
 	
 	//Creates all JLists 
 	private JList jlScholars = new JList();
 	private JList jlSerials = new JList();
 	private JList jlPapers = new JList();
 
 	//Creates all JScrollPanes
 	private JScrollPane jspScholars = new JScrollPane(jlScholars);
 	private JScrollPane jspSerials = new JScrollPane(jlSerials);
 	private JScrollPane jspPapers = new JScrollPane(jlPapers);
 
 	
 	// Creates a JFileChooser
 	private JFileChooser jFileChooser = new JFileChooser(new File("."));
 
 	/**
 	 *  Will fill the frame with content and hold the addActionListeners
 	 */
 	public SelectionView(){
 		
 		//Action listeners will be in here
 		
 	}
 	
 	/**
 	 * Loads scholarship from binary file
 	 * @param filename
 	 */
 	private void loadFile(File filename){
 		
 	}
 	
 	/**
 	 * Saves the scholarship to binary file
 	 */
 	private void saveFile(){
 		
 	}
 	
 	/**
 	 * Saves the scholarship using a specified binary file
 	 * @param filename
 	 */
 	private void saveFile(File filename){
 		
 	}
 	
 	/**
 	 * Imports scholarship from text file
 	 * @param filename
 	 */
 	private void importFile(File filename){
 		
 	}
 	
 	/**
 	 * Exports the scholarship to text file
 	 */
 	private void exportFile(){
 		
 	}
 	
 	/**
 	 * Exports the scholarship using a specified text file
 	 * @param filename
 	 */
 	private void exportFile(File filename){
 		
 	}
 	
 	/**
 	 * Opens a graph given the type of graph and scholar
	 * @param filename
 	 */
 	private void openGraph(String typeOfGraph, Scholar scholar){
 		
 	}
 	
 	/**
 	 * Will create frame and set properties
 	 * @param args
 	 */
 	public static void main(String[] args){
 		
 	}
 	
 	
 	
 }
