 package gui;
 import javax.swing.*;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.LayoutManager;
 import java.awt.RenderingHints;
 
 //for textbox?
 import java.awt.TextArea;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseAdapter;
 
 import internalformatting.Formula;
 import internalformatting.Term;
 import internalformatting.Variable;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import search.Search;
 import storage.FormulaDatabase;
 import storage.Saver;
 
 
 /**
  * Another GUI to learn a bit about gui making,
  * and to make a slightly different way to format
  * what we want in our GUI
  * 
  * @author May Camp
  *
  */
 //Forms = Formulas
 public class GUISearch extends JPanel{
 	private NewtonsToolboxPanel topPanel;
 	private NewtonsToolboxPanel middlePanel;
 	private NewtonsToolboxPanel bottomPanel;
 	
 	//search bar
 	JTextField searchBar = new JTextField(50);
 	//JTextArea searchBar = new JTextArea(1,50);
 	
 	JTextArea searchResults = new JTextArea(20,57);
 	private JButton searchButton = new JButton("Search");
 	private JButton searchFormsButton = new JButton("Search Formulas");
 	private JButton printFormsButton = new JButton("Print Formulas");
 	private JButton solveFormsButton = new JButton("Solve Formulas");
 	private JButton addFormsButton = new JButton("Add Formulas");
 	
 	public GUISearch() {
 		//setOpaque(false);
 		setSize(720,480);
 		
 
 		topPanel = new NewtonsToolboxPanel();
 		middlePanel = new NewtonsToolboxPanel();
 	
 		add(BorderLayout.NORTH, topPanel);
 		add(BorderLayout.CENTER, middlePanel);
 		
 		
 		//need searchBar action listener
 		searchBar.addKeyListener(
 				new KeyAdapter(){
 					public void keyPressed(KeyEvent key){
 						if(key.getKeyCode()==KeyEvent.VK_ENTER){
 							printSearchToTextArea();
 						}
 					}
 				}
 				);
 		
 		topPanel.add(searchBar);
 		topPanel.add(searchButton);
 		
 		middlePanel.add(searchResults);
 		
 		
 		searchButton.addActionListener(new searchButtonListener());
 		
 		
 	}
 	
 	//THIS SHOULD BE IN THE GUISEARCH MODULE
 	public void printSearchToTextArea(){
 		String userInput = searchBar.getText();
 		FormulaDatabase defaultFormulas = (FormulaDatabase)Saver.loadForms();
 		Search searchObject = new Search(defaultFormulas);
 		ArrayList<Formula> foundFormulas = searchObject.searchF(userInput);
 		
 		String stringOfFormulas = "";
 		searchResults.setLineWrap(true);
 		for(int i=0; i<foundFormulas.size();i++) {
 			stringOfFormulas = stringOfFormulas + foundFormulas.get(i).allInfoToString() + "\n \n";
 		}
 		
 		searchResults.setText("You searched for: " + userInput + "\n" +
 				"Found " + foundFormulas.size() + " formulas:\n\n" +
 				stringOfFormulas);
		//Search is done, clears the search bar.
 		searchBar.setText("");
 	}
 	
 	public class NewtonsToolboxPanel extends JPanel {
 		// Included to suppress Eclipse Warning
    	private static final long serialVersionUID = 1L;
 
     	
 
 	}
 	
     //Button Listener Classes:
     class searchButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			printSearchToTextArea();
 		}
 	}
 	
 	
 	
 }
