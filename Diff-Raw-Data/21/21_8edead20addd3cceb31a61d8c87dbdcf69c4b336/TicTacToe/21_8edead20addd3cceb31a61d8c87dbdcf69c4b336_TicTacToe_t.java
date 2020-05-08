 import java.awt.Color;
 import javax.swing.Timer;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.*;
 import java.util.HashMap;
import java.util.Map;
import java.util.Random;
 import java.util.Vector;
 
 import javax.swing.*;
 //import javax.swing.border.Border;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 enum Player { NONE, X, O }
 enum Mode	{ BEGINNER, TWO, EXPERT }
 
 public class TicTacToe implements ActionListener, MouseListener, MenuListener{
 	
 	private Timer timer = null;
 	private JFrame window = new JFrame("Tic Tac Toe");
 	private JPanel topPanel = new JPanel();
 	private JPanel gamePanel = new JPanel();
 	private JPanel bottomPanel = new JPanel();
 	private GridLayout gameGrid = new GridLayout(3, 3);
 	private JPanel[][] gameTile = new JPanel[3][3];				//No construction done yet, all elements NULL!
 	private JTextField infoText = new JTextField(80);
 	private JMenuBar gameMenuBar = new JMenuBar();
 	private JMenu gameMenu = new JMenu("Options");
 	private JMenuItem beginner = new JMenuItem("Beginner Mode");
 	private JMenuItem twoPlayer = new JMenuItem("Two Player Mode");
 	private JMenuItem expert = new JMenuItem("Expert Mode");
 	private JButton resetButton = new JButton("Reset Game");
 	private HashMap<JPanel, Player> players = new HashMap<JPanel, Player>();
 	private Player currentPlayer = Player.X;
	private Mode mode = Mode.BEGINNER;
 	private Vector<JPanel> takenTiles = new Vector<JPanel>();
 	public TicTacToe() {
 		buildGUI();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new TicTacToe();
 	}
 	
 	private void buildGUI() {
 		gamePanel.setLayout(gameGrid);
 		// --- Fill in tiles with actual JPanels ---
 		for(int i = 0; i < gameTile.length; i++) {
 			for(int j = 0; j < gameTile[0].length; j++) {
 				gameTile[i][j] = new JPanel();
 				gameTile[i][j].addMouseListener(this);
 				players.put(gameTile[i][j], Player.NONE);
 				gamePanel.add(gameTile[i][j]);
 			}
 		}
 		infoText.setEditable(false);
 		topPanel.setLayout(new GridLayout(1, 3));
 		topPanel.add(resetButton);
 		bottomPanel.add(infoText);
 		
 		beginner.addActionListener(this);
 		twoPlayer.addActionListener(this);
 		expert.addActionListener(this);
 		
 		gameMenu.add(beginner);
 		gameMenu.add(twoPlayer);
 		gameMenu.add(expert);
 		gameMenu.addMenuListener(this);
 		gameMenuBar.add(gameMenu);
 		window.setJMenuBar(gameMenuBar);
 		
 		window.getContentPane().add(topPanel, "North");
 		window.getContentPane().add(gamePanel, "Center");
 		window.getContentPane().add(bottomPanel, "South");
 		window.setSize(900,600);
 		window.setVisible(true);
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		timer = new Timer(100, this);
 		timer.start();
 	}	
 	
 	private void drawAllTiles() {
 		for(int i = 0; i < gameTile.length; i++) {
 			for(int j = 0; j < gameTile[0].length; j++) {
 				setTile(gameTile[i][j], players.get(gameTile[i][j]));
 			}
 		}
 	}
 	
 	private void reset() {
 		for(int i = 0; i < gameTile.length; i++) {
 			for(int j = 0; j < gameTile[0].length; j++) {
 				players.put(gameTile[i][j], Player.NONE);
 			}
 		}
 	}
 	
 	private void setTile(JPanel tile, Player p){
 		int w = tile.getSize().width;
 		int h = tile.getSize().height;
 		Graphics g = tile.getGraphics();
 		switch(p) {
 		case NONE:
 			g.setColor(Color.WHITE);
 			g.fillRect(0,0,w,h);
 			break;
 		case X:
 			g.setColor(Color.BLACK);
 			g.fillRect(0,0,w,h);
 			break;
 		case O:
 			g.setColor(Color.YELLOW);
 			g.fillRect(0,0,w,h);
 			break;
 		}
 	}
 
 	private void swapPlayers() {
 		if(currentPlayer == Player.X) {
 			currentPlayer = Player.O;
 		}
 		else if(currentPlayer == Player.O) {
 			currentPlayer = Player.X;
 		}
 		else {
 			currentPlayer = Player.NONE;
 			System.out.println("something weird happened while swapping players");
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent me) {
 		JPanel clickedPanel = (JPanel)me.getSource();
 		if(takenTiles.contains(clickedPanel)) {
 			return;
 		}
 		players.put(clickedPanel, currentPlayer);
 		takenTiles.add(clickedPanel);
 		swapPlayers();
 		switch(mode){
 		case TWO:
 			return;
 		case BEGINNER:
			System.out.println("Beginner");
			JPanel tmp = null;
			Random rand = new Random();
			int rx = 0;
			int ry = 0;
			do {
				rx = rand.nextInt(3);
				ry = rand.nextInt(3);
				tmp = gameTile[rx][ry];
			}while(takenTiles.contains(tmp));
			takenTiles.add(tmp);
			players.put(tmp, currentPlayer);
			swapPlayers();
 			return;
 		case EXPERT:
 			return;
 		default:
 			return;
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent me) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent me) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent me) {
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		// TODO Auto-generated method stub
 		if(ae.getSource() == timer) {
 			drawAllTiles();
 			return;
 		}
 		if(ae.getSource() == resetButton) {
 			reset();
 			infoText.setText("Game reset.");
 		}
 		if(ae.getSource() == beginner) {
 			reset();
 			mode = Mode.BEGINNER;
 			infoText.setText("Game reset, now in beginner mode.");
 			return;
 		}
 		if(ae.getSource() == twoPlayer) {
 			reset();
 			mode = Mode.TWO;
 			infoText.setText("Game reset, now in two player mode. Take turns selecting squares. X goes first.");
 			return;
 		}
 		if(ae.getSource() == expert) {
 			mode = Mode.EXPERT;
 			reset();
 			infoText.setText("Game reset, now in expert mode.");
 			return;
 		}
 	}
 
 	@Override
 	public void menuCanceled(MenuEvent arg0) {
 		
 	}
 
 	@Override
 	public void menuDeselected(MenuEvent arg0) {
 		System.out.println("Deselected");
 		timer.start();
 	}
 
 	@Override
 	public void menuSelected(MenuEvent arg0) {
 		timer.stop();
 	}
 }
