 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 /**
  * This is the main runner class for the Ludo Game. It currently prints the
  * board background image, and adds the pawns to the board.
  * 
  * @author Daniel Faulkner
  */
 public class LudoGame extends JPanel {
 
 	// The height and width of the application
 	private final static int APPHEIGHT = 540;
 	private final static int APPWIDTH = 750;
 
 	// The offsets from the walls of the window
 	private final static int BOARDLEFTOFFSET = 5;
 	private final static int BOARDTOPOFFSET = 5;
 
 	// The grids on the game board are 48 pixels across
 	private final static int GRIDSIZE = 48;
 	// There are 11x11 grids (including ones the pawns can't occupy)
 	private final static int GRIDNUM = 11;
 	// This 2D array holds the top left point of each matching grid spot
 	// TODO This should probably be removed once the Fields are implemented
 	private final static Point[][] THEGRID = new Point[GRIDNUM][GRIDNUM];
 	// The grid offsets for putting pawns in the correct home field
 	private final static int LEFT = 1, RIGHT = 8;
 	private final static int TOP = 1, BOTTOM = 8;
 	
 	// The array lists to keep track of the pawns in the game
 	private final ArrayList<Pawn> bluePawns = new ArrayList<Pawn>();
 	private final ArrayList<Pawn> redPawns = new ArrayList<Pawn>();
 	private final ArrayList<Pawn> greenPawns = new ArrayList<Pawn>();
 	private final ArrayList<Pawn> yellowPawns = new ArrayList<Pawn>();
 
 	// The swing panel for layered objects
 	private JLayeredPane boardPane;
 
 	/**
 	 * Constructor for the game board. Adds layered images and game pieces.
 	 */
 	public LudoGame() {
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		final ImageIcon boardBackground = createImageIcon("src/game_board.png");
		final ImageIcon bluePawnImg = createImageIcon("src/blue_pawn.png");
		final ImageIcon redPawnImg = createImageIcon("src/red_pawn.png");
		final ImageIcon greenPawnImg = createImageIcon("src/green_pawn.png");
		final ImageIcon yellowPawnImg = createImageIcon("src/yellow_pawn.png");
 
 		setupTheGrid();
 
 		boardPane = new JLayeredPane();
 		boardPane.setPreferredSize(new Dimension(APPWIDTH, APPHEIGHT));
 
 		JLabel board = new JLabel(boardBackground);
 		boardPane.add(board, new Integer(0));
 		Dimension boardSize = board.getPreferredSize();
 		board.setBounds(BOARDLEFTOFFSET, BOARDTOPOFFSET, boardSize.width,
 				boardSize.height);
 
 		addPawns(bluePawnImg, bluePawns, LEFT, TOP);
 		addPawns(redPawnImg, redPawns, LEFT, BOTTOM);
 		addPawns(yellowPawnImg, yellowPawns, RIGHT, TOP);
 		addPawns(greenPawnImg, greenPawns, RIGHT, BOTTOM);
 		add(boardPane);
 	}
 
 	/**
 	 * Sets up the 2D array that holds the 11 x 11 grid imagining of the board
 	 * and assigns each location the relevant Point of the top left corner (for
 	 * a Point p, p.x and p.y give you the x and y coordinates of that point,
 	 * respectively).
 	 */
 	private void setupTheGrid() {
 		for (int i = 0; i < GRIDNUM; i++) {
 			for (int j = 0; j < GRIDNUM; j++) {
 				THEGRID[i][j] = new Point(i * GRIDSIZE, j * GRIDSIZE);
 			}
 		}
 	}
 
 	/**
 	 * Adds a player's 4 pawns to the game board in the grid spot corresponding
 	 * with the home field (once that's implemented).
 	 * 
 	 * @param imgSrc
 	 *            the pawn's image
 	 * @param pawnList
 	 *            the list to add the pawn to after putting on board
 	 * @param homeFieldX
 	 *            the home field positioning (LEFT or RIGHT)
 	 * @param homeFieldY
 	 *            the home field positioning (TOP or BOTTOM)
 	 */
 	private void addPawns(final ImageIcon imgSrc,
 			final ArrayList<Pawn> pawnList, final int homeFieldX,
 			final int homeFieldY) {
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 2; j++) {
 				JLabel jl = new JLabel(imgSrc);
 				boardPane.add(jl, new Integer(1));
 				Dimension size = jl.getPreferredSize();
 				jl.setBounds(
 						BOARDLEFTOFFSET
 								+ (THEGRID[i % 2 + homeFieldX][j % 2
 										+ homeFieldY].x), BOARDTOPOFFSET
 								+ THEGRID[i % 2 + homeFieldX][j % 2
 										+ homeFieldY].y, size.width,
 						size.height);
 				Pawn p = new Pawn(jl, jl.getLocation());
 				bluePawns.add(p);
 			}
 		}
 	}
 
 	/**
 	 * Loads the image from the specified path.
 	 * 
 	 * @param src
 	 *            the path of the image file
 	 * @return an ImageIcon (or null, if the image is not found)
 	 */
 	private ImageIcon createImageIcon(final String src) {
 		try {
 			BufferedImage bluePawn = ImageIO.read(new File(src));
 			ImageIcon icon = new ImageIcon(bluePawn);
 			return icon;
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Creates the general GUI frames and call the LudoGame constructor.
 	 */
 	private static void createGUI() {
 		JFrame frame = new JFrame("Ludo Game");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JComponent contentPane = new LudoGame();
 		contentPane.setOpaque(true);
 		frame.setContentPane(contentPane);
 
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	/**
 	 * The entry point for the program. Kicks things off by creating the GUI.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		createGUI();
 	}
 }
