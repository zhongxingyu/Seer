 /**
  * @author Brian McCarthy
  */
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 
 public class CanarySyntaxChecker {
 	JFrame frame;
 	JTextArea textArea;
 	JScrollPane scrollPane;
 	JMenuBar menuBar;
 	JMenu menu_Text;
 	JMenu menu_Read;
 	JMenu menu_About;
 	JMenuItem menuItem_Add;
 	JMenuItem menuItem_Clear;
 	JMenuItem menuItem_Groups;
 	JMenuItem menuItem_Users;
 	JMenuItem menuItem_Help;
 	JMenuItem menuItem_About;
 	JMenuItem menuItem_Change;
 	CanarySyntaxCheckerListener listener = new CanarySyntaxCheckerListener(this);
 	String input = "";
 	Analyser analyser;
 
 	void createGUI(){
 		frame = new JFrame("CanarySyntaxChecker2");
 		textArea = new JTextArea();
 		scrollPane = new JScrollPane(textArea);
 		menuBar = new JMenuBar();
 		menu_Text = new JMenu("Text");
 		menu_Read = new JMenu("Read");
 		menu_About = new JMenu("About");
 		menuItem_Add = new JMenuItem("Add Line");
 		menuItem_Clear = new JMenuItem("Clear Text");
 		menuItem_Groups = new JMenuItem("Read as Groups");
 		menuItem_Users = new JMenuItem("Read as Users");
 		menuItem_Help = new JMenuItem("Help");
 		menuItem_About = new JMenuItem("About");
 		menuItem_Change = new JMenuItem("Change Log");
 
 		menuItem_Add.addActionListener(listener);
 		menuItem_Clear.addActionListener(listener);
 		menuItem_Groups.addActionListener(listener);
 		menuItem_Users.addActionListener(listener);
 		menuItem_Help.addActionListener(listener);
 		menuItem_About.addActionListener(listener);
 		menuItem_Change.addActionListener(listener);
 		textArea.setEditable(true);
 		menu_Text.add(menuItem_Add);
 		menu_Text.add(menuItem_Clear);
 		menu_Read.add(menuItem_Groups);
 		menu_Read.add(menuItem_Users);
 		menu_About.add(menuItem_Help);
 		menu_About.add(menuItem_About);
 		menu_About.add(menuItem_Change);
 		menuBar.add(menu_Text);
 		menuBar.add(menu_Read);
 		menuBar.add(menu_About);
 
 		frame.setJMenuBar(menuBar);
 		frame.add(scrollPane);
 		frame.setSize(600, 400);
 		frame.setVisible(true);
 	}// end createGUI
 
 	public static void main(String[] args){
 		CanarySyntaxChecker instance = new CanarySyntaxChecker();
 		instance.createGUI();
 	}// end main
 }
