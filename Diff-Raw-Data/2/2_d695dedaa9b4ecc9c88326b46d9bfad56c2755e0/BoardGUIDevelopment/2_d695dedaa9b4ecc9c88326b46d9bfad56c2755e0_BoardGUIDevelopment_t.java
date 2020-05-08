 package gui;
 
 import java.awt.EventQueue;
 import java.awt.GridLayout;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import sharedfiles.Board;
 import sharedfiles.Piece;
 
 public class BoardGUIDevelopment {
 
 	private JFrame frame;
 	private String[][] pieces;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					BoardGUIDevelopment window = new BoardGUIDevelopment();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public BoardGUIDevelopment() {
 		pieces = new String[8][8];
 		readBoard(new Board());
 		initialize();
 	}
 
 	private void readBoard(Board board) {
 		Piece[][] temp = board.getBoardArray();
 		for (int i = 0; i < temp.length; i++) {
 			for (int j = 0; j < temp.length; j++) {
 				pieces[i][j] = temp[i][j].toString();
 			}
 		}
 	}
 
 	protected ImageIcon createImageIcon(String path, String description) {
 		java.net.URL url = getClass().getResource(path);
 		if (url != null) {
 			return new ImageIcon(url, description);
 		} else {
 			System.err.println("DERPYFILE COULDN'T BE LOCATED: " + path);
 			return null;
 		}
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 
 	private void initialize() {
 		Board b = new Board();
		b.randomize();
 		readBoard(b);
 
 		frame = new JFrame();
 		frame.setSize(425, 425);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.setLocationRelativeTo(null);
 		int i = 8;
 		int j = 8;
 
 		GridLayout steven = new GridLayout(i, j);
 		steven.setHgap(2);
 		steven.setVgap(2);
 
 		JPanel[][] panelHolder = new JPanel[i][j];
 		frame.setLayout(steven);
 
 		for (int m = 0; m < i; m++) {
 			for (int n = 0; n < j; n++) {
 				panelHolder[m][n] = new JPanel();
 				panelHolder[m][n].setSize(60, 60);
 				frame.add(panelHolder[m][n]);
 			}
 		}
 
 		for (int m = 0; m < i; m++) {
 			for (int n = 0; n < j; n++) {
 
 				String pieceType = pieces[m][n];
 				String tempResourceName = "" + Character.toLowerCase(pieceType.charAt(0));
 				switch (pieceType.charAt(1)) {
 					case 'X':
 						tempResourceName = "Blank";
 						break;
 					case 'B':
 						tempResourceName.concat("Bishop");
 						break;
 					case 'R':
 						tempResourceName.concat("Rook");
 						break;
 					case 'P':
 						tempResourceName.concat("Pawn");
 						break;
 					case 'Q':
 						tempResourceName.concat("Queen");
 						break;
 					case 'K':
 						tempResourceName.concat("King");
 						break;
 					case 'N':
 						tempResourceName.concat("Knight");
 						break;
 					default:
 						System.out.println("Reached default case in piece string array of BoardGUIDevelopment");
 						System.out.println("Exiting");
 						System.exit(1);
 						break;
 				}
 
 				tempResourceName.concat(".png");
 
 				ImageIcon icon = createImageIcon(tempResourceName, "Derpy Spot");
 
 				JLabel label = new JLabel();
 				label.setIcon(icon);
 				panelHolder[m][n].add(label);
 			}
 		}
 
 		frame.getContentPane().setLayout(steven);
 	}
 }
