 /**
  * Kira Combs
  * Maria Deslis
  */
 package DetectiveNotesGUI;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.*;
 import java.awt.BorderLayout;
 import javax.swing.*;
 
 import main.Board;
 
public class DetectiveNotesGUI extends JFrame{
 	//Basic GUI Setup
 	public DetectiveNotesGUI() {
 		//creates a board
 		Board board = new Board("roomLegend.txt", "config.txt", "players.csv", "cards.csv");
 		
 		setSize(new Dimension(550,500));
 		setTitle("Detective Notes");
 		//East Panel
 		//holds the guess information
 		JPanel e = new JPanel();
 		add(e, BorderLayout.EAST);
 		e.setLayout(new GridLayout(3,2));
 		PersonGuessPanel pgPanel = new PersonGuessPanel(board);
 		e.add(pgPanel);
 		RoomGuessPanel rgPanel = new RoomGuessPanel(board);
 		e.add(rgPanel);
 		WeaponGuessPanel wgPanel = new WeaponGuessPanel(board);
 		e.add(wgPanel);
 		//West Panel
 		//holds checkboxes for cards seen
 		JPanel w = new JPanel();
 		add(w, BorderLayout.WEST);
 		w.setLayout(new GridLayout(3,0));
 		PeopleSeenPanel psPanel = new PeopleSeenPanel(board);
 		w.add(psPanel);
 		RoomsSeenPanel rsPanel = new RoomsSeenPanel(board);
 		w.add(rsPanel);
 		WeaponsSeenPanel wsPanel = new WeaponsSeenPanel(board);
 		w.add(wsPanel);
 		//menu
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		menuBar.add(createFileMenu());
 	}
 	
 	//Creating File > Exit Menu for the GUI
 	private JMenu createFileMenu(){
 		JMenu menu = new JMenu("File");
 		menu.add(createFileExitItem());
 		return menu;
 	}
 	
 	private JMenuItem createFileExitItem(){
 		JMenuItem item = new JMenuItem("Exit");
 		
 		class MenuItemListener implements ActionListener {
 			public void actionPerformed(ActionEvent e) {
				System.exit(0);
 			}
 		}
 		item.addActionListener(new MenuItemListener());
 		return item;
 	}
 	
 	
 	/**
	 * @param args
 	public static void main(String[] args) {
 		//Setting up basic items in JFrame
 		DetectiveNotesGUI gui = new DetectiveNotesGUI();
 		
 		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		gui.setVisible(true);
 
 	}*/
 
 }
