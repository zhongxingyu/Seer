 package dialog;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 import csci306.Board;
 
 public class ClueGame extends JFrame {
 	Board board;
 	
 	public ClueGame() {
 		setTitle("Clue Game");
 		setSize(800, 650);
 		
 		createMenuBar();
 		createBoard();
 		createControls();
 		
 		
 		add(board, BorderLayout.CENTER);
 	}
 	
 	public void createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		
 		JMenuItem showNotes = new JMenuItem("Show Notes");
 		showNotes.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
				JDialog showNotesDialog = new NotesDialog();
 				showNotesDialog.setVisible(true);
 			}
 			
 		});
 		
 		JMenuItem exit = new JMenuItem("Exit");
 		exit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 				
 			}
 			
 		});
 		
 		file.add(showNotes);
 		file.add(exit);
 		menuBar.add(file);
 		setJMenuBar(menuBar);
 	}
 	
 	public void createBoard() {
 		board = new Board();
 		board.loadPlayers();
 		board.loadCards();
 	}
 	
 	public void createControls() {
 		
 	}
 	
 	public static void main(String[] args) {
 		ClueGame cb = new ClueGame();
 		cb.setVisible(true);
 	}
 
 }
