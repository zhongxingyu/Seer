 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 public class GameDisplay extends JPanel implements ActionListener, MouseListener, KeyListener, MouseMotionListener { 
 
 	JMenuItem g1 = new JMenuItem("Reset");
 	JCheckBoxMenuItem g2 = new JCheckBoxMenuItem("Marks (?)");
 	JCheckBoxMenuItem g3 = new JCheckBoxMenuItem("Compact");
 
 	JCheckBoxMenuItem m1 = new JCheckBoxMenuItem("Easy");
 	JCheckBoxMenuItem m2 = new JCheckBoxMenuItem("Medium");
 	JCheckBoxMenuItem m3 = new JCheckBoxMenuItem("Hard");
 	JCheckBoxMenuItem m4 = new JCheckBoxMenuItem("Custom");
 
 	JMenuItem h1 = new JMenuItem("Hint");
 	JMenuItem h2 = new JMenuItem("About");
 	JMenuItem h3 = new JMenuItem("Instructions");
 	JMenuItem h4 = new JMenuItem("More Apps...");
 
 	Board playBoard;
 	CheckList bombs;
 
 	PlayFrame game;
 	String difficulty;
 
 	boolean gameOver = false;
 
 	public GameDisplay(String difficulty,PlayFrame g)
 	{
 		this(difficulty);
 		game = g;
 	}
 
 	public GameDisplay(String difficulty){
 
 		this.difficulty = difficulty;
 
 		if(difficulty.contains("Easy")){
 			playBoard = new Board(9,9,this);
 			playBoard.setTotalBombs(10);
 
 		}
 
 		else if(difficulty.contains("Medium")){
 			playBoard = new Board(16,16,this);
 			playBoard.setTotalBombs(40);
 		}
 
 		else if(difficulty.contains("Hard")){
 			playBoard = new Board(30,16,this);
 			playBoard.setTotalBombs(99);
 		}
 
 		else{
 
 			this.setDifficulty("Cusom"); //TODO prevent user from doing dumb shit
 
 			String x = JOptionPane.showInputDialog("Desired width of your board");
 			int width = Integer.parseInt(x);
 
 			String y = JOptionPane.showInputDialog("Desired height of your board");
 			int height = Integer.parseInt(y);
 
 			String bombs = JOptionPane.showInputDialog("Desired number of bombs on your board");
 			int totalBombs = Integer.parseInt(bombs);
 
 
 			playBoard = new Board(width,height,this);
 			playBoard.setTotalBombs(totalBombs);
 		}
 
 		playBoard.setUp();
 
 		this.setSize(playBoard.getWindowX(),playBoard.getWindowY());
 		this.setVisible(true);
 		
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		this.addKeyListener(this);
 		setFocusable(true);
 		requestFocus();
 
 		this.playBoard.initializeBoard();
 	}
 
 	public JMenuBar getJMenuBar()
 	{
 
 		JMenu help = new JMenu("Help");
 		JMenuBar menuBar = new JMenuBar();
 		JMenu game = new JMenu("Game");
 		JMenu mode = new JMenu("Mode");
 
 		menuBar.add(game);
 		menuBar.add(mode);
 		menuBar.add(help);
 
 		game.add(g1);
 		game.add(g2);
 		game.add(g3);
 
 		mode.add(m1);
 		mode.add(m2);
 		mode.add(m3);
 		mode.add(m4);
 
 		help.add(h1);
 		help.add(h2);
 		help.add(h3);
 		help.add(h4);
 
 		g1.addActionListener(this);
 		g2.addActionListener(this);
 		g3.addActionListener(this);
 
 		m1.addActionListener(this);
 		m2.addActionListener(this);
 		m3.addActionListener(this);
 		m4.addActionListener(this);
 
 		h1.addActionListener(this);
 		h2.addActionListener(this);
 		h3.addActionListener(this);
 		h4.addActionListener(this);
 
 
 		mode.addActionListener(this);
 		game.addActionListener(this);
 		help.addActionListener(this);
 
 		m2.setSelected(true);
 
 		return menuBar;
 	}
 
 	public String getDifficulty(){
 
 		return difficulty;
 	}
 
 	public void setDifficulty(String a){
 
 		difficulty = a;
 	}
 
 	public void paintComponent(Graphics g){
 
 		super.paintComponent(g);
 		this.setBackground(Color.WHITE);
 		if(game!=null)
 			this.setBackground(Color.LIGHT_GRAY);
 		playBoard.paintBoard(g);
 
 	}
 
 	public boolean gameOver(){
 
 		if(playBoard.getWin() || playBoard.getLose())
 		{
 			gameOver = true;
 
 			playBoard.endTimer();
 		}		
 		return gameOver;
 	}
 
 	public void mouseClicked(MouseEvent e) {
 
 	}
 
 	public void mousePressed(MouseEvent e) {
 
 		if(SwingUtilities.isLeftMouseButton(e)){
 
 			int x = (e.getX()-2)/(playBoard.tileSize+1);
 			int y = (e.getY()-4)/(playBoard.tileSize+1);
 
 			if (e.getY() < 0)
 				return;
 
 			playBoard.add(x, y);
 			playBoard.press(x,y);
 			repaint();
 		}
 
 	}
 
 	public void mouseReleased(MouseEvent e) {
 
 		if(!playBoard.isEmpty())
 			playBoard.resetPressed();
 
 		int x = (e.getX()-2)/(playBoard.tileSize+1);
 		int y = (e.getY()-4)/(playBoard.tileSize+1);
 
 		if (e.getY() < 0)
 			return;
 
 		if(playBoard.isValid(x, y)){
 
 			if(SwingUtilities.isLeftMouseButton(e)){
 
 				if(gameOver)
 					resetGame();
 
 				else if(!playBoard.isOpen(x,y))
 					playBoard.open(x, y);
 				else
 					playBoard.fastClick(x,y);
 			}
 			if(!gameOver()){
 
 				
 
 				if(SwingUtilities.isRightMouseButton(e)){
 
 					playBoard.markFlagged(x, y);
 				}
 
 			}
 			else
 
 				if(playBoard.lose){
 
 					bombAnimation();
 
 				}
 			repaint();
 		}
 
 	}
 
 	public void mouseEntered(MouseEvent e) {
 
 
 	}
 
 	public void mouseExited(MouseEvent e) {
 
 		playBoard.resetPressed();
 		repaint();
 
 	}
 
 	public void mouseDragged(MouseEvent e) {
 
 		if(SwingUtilities.isLeftMouseButton(e)){
 
 			int x = (e.getX()-2)/(playBoard.tileSize+1);
 			int y = (e.getY()-4)/(playBoard.tileSize+1);
 
 
 
 			repaint();
 
 			if(!playBoard.alreadyThere(x,y)&&playBoard.isValid(x, y)){
 				playBoard.resetPressed();
 				playBoard.replace(x, y);
 				playBoard.press(x,y);
 				repaint();
 			}
 			repaint();
 		}
 	}
 
 	public void mouseMoved(MouseEvent e) {
 
 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 
 
 
 
 		if(e.getSource() == g2) //question marks
 			questionMarks();
 
 		else if(e.getSource() == g3){
 			compactMode();
 		}
 
 		else if(e.getSource() == g1) //reset
 			this.resetGame();
 
 		else if(e.getSource() == h1)//hints
 		{
 			playBoard.hint();
 			repaint();
 		}
 		else if(e.getSource() == h2) //about
 			JOptionPane.showMessageDialog(null,"Mines\nby Philip Donlon");
 
 		else if(e.getSource() == h3) //instructions
 			JOptionPane.showMessageDialog(null, "Open all the tiles without hitting a bomb.\nNumbered tiles indicate how many bombs are touching it.\nLeft click to open a single tile.\nFlag a tile by right clicking.\nSingle click a tile with the correct amount of flags around it for a speed click.\nDouble click the starting tile for a quick reset.");
 
 		else if(e.getSource() == h4)//more apps
 			goOnline("http://www.github.com/pdonlon");
 
 		if(e.getSource()==m1 || e.getSource()==m2 || e.getSource()==m3 || e.getSource()==m4){
 
 			if(e.getSource() == m1){
 				clearChecks();
 				m1.setSelected(true);
 				playBoard.setWidth(9);
 				playBoard.setHeight(9);
 				this.setDifficulty("EASY");
 				playBoard.setTotalBombs(10);
 			}
 
 			else if(e.getSource() == m2){
 				clearChecks();
 				m2.setSelected(true);
 				playBoard.setWidth(16);
 				playBoard.setHeight(16);
 				this.setDifficulty("MEDIUM");
 				playBoard.setTotalBombs(40);
 
 			}
 
 			else if(e.getSource() == m3){
 				clearChecks();
 				m3.setSelected(true);
 				playBoard.setWidth(30);
 				playBoard.setHeight(16);
 				this.setDifficulty("Hard");
 				playBoard.setTotalBombs(99);
 			}
 			else if(e.getSource() == m4){//custom
 
 				clearChecks();
 				m4.setSelected(true);
 				int width = 0, height =0, totalBombs = 0;
 
 				while(width<9){
 					String x = JOptionPane.showInputDialog("Desired width of your board");
 					if(x==null)
 						return;
 
 					try{
 						width = Integer.parseInt(x);
 						playBoard.setWidth(width);
 					}
 					catch(Exception ex){
 						width = 0;
 					}
 				}
 
 				while(height<9){
 					String y = JOptionPane.showInputDialog("Desired height of your board");
 					if(y==null)
 						return;
 
 					try{
 						height = Integer.parseInt(y);
 						playBoard.setHeight(height);
 					}
 					catch(Exception ex){
 						height = 0;
 					}
 				}
 
 
 				while(totalBombs<=0 || totalBombs>width*height-9){
 					String bombs = JOptionPane.showInputDialog("Desired number of bombs on your board");
 					if(bombs==null)
 						return;
 					try{
 						totalBombs = Integer.parseInt(bombs);
 						playBoard.setTotalBombs(totalBombs);
 					}
 					catch(Exception ex){
 						totalBombs = 0;
 					}
 				}
 
 				this.setDifficulty("CUSTOM");
 			}
 
 
 
 			playBoard.setUp();
 			if(game!=null)
 			{
 				this.setSize(playBoard.getWindowX(),playBoard.getWindowY());
 				game.setSize(playBoard.getWindowX(),playBoard.getWindowY());
 			}
 			resetGame();
 			playBoard.initializeBoard();
 
 		}
 	}
 
 	public void bombAnimation(){
 
 		Thread b = new Thread( new Runnable(){
 			public void run(){
 
 				int bombsLeft = playBoard.getTotalBombs()-1;
 
 				while(bombsLeft>0){
 
 					try {
 						Thread.sleep(5);
 					} catch(InterruptedException ex) {
 						Thread.currentThread().interrupt();
 					}
 
 					explosion();
 
 					bombsLeft--;
 				}
 
 			}
 		});
 
 		b.start();
 
 	}
 
 	public void resetGame(){
 
 		if(playBoard.doneAnimating())
 		{
 		playBoard.startup();
 		playBoard.wipeBoard();
 		gameOver = false;
 
 		playBoard.endTimer();
 		repaint();
 		}
 	}
 
 
 	public void goOnline(String site){
 		try {
 			java.awt.Desktop.getDesktop().browse(new URI(site));
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void clearChecks(){
 
 		m1.setSelected(false);
 		m2.setSelected(false);
 		m3.setSelected(false);
 		m4.setSelected(false);
 
 	}
 
 	public void questionMarks(){
 		if(playBoard.questionMarksEnabled()){
 			g2.setSelected(false);
 			playBoard.setQuestionMarks(false);
 		}
 		else{
 			g2.setSelected(true);
 			playBoard.setQuestionMarks(true);
 		}
 	}
 	
 	public void compactMode(){
 		if(playBoard.compactModeEnabled()){
 			g3.setSelected(false);
 			playBoard.setCompactMode(false);
 			playBoard.setTileSize(26);
 		}
 		else{
 			g3.setSelected(true);
 			playBoard.setCompactMode(true);
 			playBoard.setTileSize(20);
 		}
 		if(game!=null)
 		{
 			this.setSize(playBoard.getWindowX(),playBoard.getWindowY());
 			game.setSize(playBoard.getWindowX(),playBoard.getWindowY());
 		}
 	}
 
 	public void explosion(){
 
 		playBoard.openBomb();
 		repaint();
 	}
 
 	public void keyTyped(KeyEvent ev) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void keyReleased(KeyEvent ev) {
 		
 		playBoard.removeHint();
 		playBoard.setCheck(false);
 	}
 
 	public void keyPressed(KeyEvent ev) {
 
 		int keycode = ev.getKeyCode();
 
 		if(keycode == 67){ //TODO
 
 			playBoard.setCheck(true);
 			repaint();
 		}
 
 		else if(keycode == 72){
 			playBoard.hint();
 			repaint();
 		}
 		
 		else if(keycode == 27){
 			System.exit(0);
 		}
 		
 		else if(keycode == 82){
 			resetGame();
 		}
 		
 
 	}
 
 	
 }
