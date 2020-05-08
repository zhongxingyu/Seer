 package taberystwyth.view;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 public class MainFrameMenu extends JMenuBar {
	private static final long serialVersionUID = 1L;
	
 	JMenu fileMenu = new JMenu("File");
 	JMenu roundsMenu = new JMenu("Rounds");
 	JMenu insertMenu = new JMenu("Insert");
 	
 	JMenuItem open = new JMenuItem("Open");
 	JMenuItem save = new JMenuItem("Save");
 	JMenuItem quit = new JMenuItem("Quit");
 	
 	JMenuItem speakers = new JMenuItem("Speakers");
 	JMenuItem judges = new JMenuItem("Judges");
 	JMenuItem locations = new JMenuItem("Locations");
 	
 	JMenuItem drawRound = new JMenuItem("Draw Round");
 	JMenuItem viewRounds = new JMenuItem("View Rounds");
 	
 	public MainFrameMenu(){
 		fileMenu.add(open);
 		fileMenu.add(save);
 		fileMenu.add(quit);
 		
 		insertMenu.add(speakers);
 		insertMenu.add(judges);
 		insertMenu.add(locations);
 		
 		roundsMenu.add(drawRound);
 		roundsMenu.add(viewRounds);
 		
 		add(fileMenu);
 		add(insertMenu);
 		add(roundsMenu);
 	}
 	
 }
