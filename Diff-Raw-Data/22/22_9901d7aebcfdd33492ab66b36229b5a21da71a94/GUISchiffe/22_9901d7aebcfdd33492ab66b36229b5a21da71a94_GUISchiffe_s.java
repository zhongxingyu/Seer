 package gruppe16;
 
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Calendar;
 import java.util.Random;
 
 import gruppe16.exceptions.InvalidInstruction;
 import gruppe16.exceptions.InvalidLevelException;
 import gruppe16.gui.BoardPanel;
 import gruppe16.gui.BoardUser;
 import gruppe16.gui.placeOwnShipsDialog;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 
 class CountdownTimerPanel extends JPanel { // http://leepoint.net/notes-java/examples/animation/41TextClock/25textclock.html
 	
     private JTextField _timeField;  // set by timer listener
     private long endtime;
     private GUISchiffe app;
     javax.swing.Timer t;
 
     public CountdownTimerPanel(GUISchiffe app) {
     	this.app = app;
     	
         _timeField = new JTextField(5);
         _timeField.setEditable(false);
         _timeField.setFont(new Font("sansserif", Font.PLAIN, 48));
 
         this.setLayout(new FlowLayout());
         this.add(_timeField); 
         
         resetCountdown();
         
         //    Use full package qualification for javax.swing.Timer
         //    to avoid potential conflicts with java.util.Timer.
         t = new javax.swing.Timer(50, new ClockListener());
         t.start();
 
     }
     
     void resetCountdown() {
     	endtime = System.currentTimeMillis() + 5000;
     }
     
     void pause() {
     	t.stop();
    	resetCountdown();
     }
     
    void resume() {
     	t.start();
     }
     
    class ClockListener implements ActionListener {
     	public void actionPerformed(ActionEvent e) {
 
        		long nowtime = System.currentTimeMillis();
 
             _timeField.setText(String.format("%.1f", Math.abs((double) (endtime-nowtime))/1000)); // abs to prevent short minus interval when timer goes below 0
  
     		if (nowtime >= endtime) {
     			if (!app.engine.getState().isPlayerTurn()) return;
     			app.engine.getState().changeTurn();
     			app.aiAttackAs(1);
     			//app.userError("Time's up!");
     			//t.stop();
     		}
     	}
     }
 }
 
 /**
  * class implementing Swing UI
  */
 public class GUISchiffe implements ActionListener, BoardUser {
 	private JFrame frame;
 	Engine engine;
 	private AI ai;
 	private BoardPanel[] panels;
 	private boolean speerfeuer = true;
 	private CountdownTimerPanel clock;
 	
 	/**
 	 * call this when game is over to notify user of winning player and shut down
 	 */
 	public void GameOver() {
 		if (speerfeuer) clock.pause();
 		
 		int winner = engine.checkWin();
 		
 		String winnerstr;
 		
 		if (winner == -1) {
 			winnerstr = "Draw";
 		} else {
 			winnerstr = "Player " + (winner+1) + " won!";
 		}
 		
 		JOptionPane.showMessageDialog(frame,
 			    "Game over. " + winnerstr, 
 			    "Game over", 0);
 		frame.dispose();
 	}
 	
 	private GUISchiffe() {
 		initNewEngineAndAI();
 		
 		showFrame();
 	}
 	
 	private boolean placeOwnShipsWizard() {
 		placeOwnShipsDialog shipchooser = new placeOwnShipsDialog(frame);
 
 		shipchooser.setVisible(true); // blocks. i.e. next line is only executed after ship chooser is closed.
 
 		if (shipchooser.finished ) {
 			engine = shipchooser.engine;
 			ai = new GoodAI(engine);
 			
 			frame.dispose();
 			showFrame();
 			return true;
 		}
 		return false;
 	}
 
 	private static class actions {
 		static enum actionnames {
 			quicknewgame, save, load, newplaceownships, newgenerated
 		}
 		
 		public static String quicknewgame = actionnames.quicknewgame.name();
 		public static String save = actionnames.save.name();
 		public static String load = actionnames.load.name();
 		public static String newplaceownships = actionnames.newplaceownships.name();
 		public static String newgenerated = actionnames.newgenerated.name();
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent actionEvent) {
 		//System.err.println(actionEvent);
 		String a = actionEvent.getActionCommand();
 		clock.pause();
 		if (a.equals(actions.quicknewgame)) { // would be nice with the Java 7 string switch
 			quickNewGame();
 			clock.resetCountdown();
 		} else if (a.equals(actions.save)) {
 			save();
 		} else if (a.equals(actions.load)) {
 			load();
 			clock.resetCountdown();
 		} else if (a.equals(actions.newplaceownships)) {
 			if (placeOwnShipsWizard())
 				clock.resetCountdown();
 		} else if (a.equals(actions.newgenerated)) {
 			generatedNewGame();
 			clock.resetCountdown();
 		} else {
 			userError("Unknown action!");
 		}
 		clock.resume();
 	}
 	
 	/**
 	 * initialize and show main game window
 	 */
 	private void showFrame() {	
 		frame = new JFrame("GUISchiffe");
 		frame.setResizable(false);
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		JPanel boardpanel = new JPanel();
 		
 		BoardPanel panel1 = new BoardPanel(this, engine, 0, false);
 		boardpanel.add(panel1);
 		BoardPanel panel2 = new BoardPanel(this, engine, 1, false);
 		panels = new BoardPanel[] {panel1, panel2};
 		boardpanel.add(panel2);
 		
 		panel.add(boardpanel);
 		
 		panel1.setOtherBoard(panel2);
 		panel2.setOtherBoard(panel1);
 		
 		if (speerfeuer) {
 			clock = new CountdownTimerPanel(this);
 			panel.add(clock);
 		}
 		
 		frame.getContentPane().add(panel);
 		
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 		ActionListener menuListener = this;
 
 		// File Menu, F - Mnemonic
 	    JMenu fileMenu = new JMenu("File");
 	    fileMenu.setMnemonic(KeyEvent.VK_F);
 	    menuBar.add(fileMenu);
 
 	    JMenu newMenu = new JMenu("New");
 	    fileMenu.add(newMenu);
 	    
 	    JMenuItem quickNewItem = new JMenuItem("Quick New (load random built-in level)", KeyEvent.VK_N);
 	    quickNewItem.addActionListener(menuListener);
 	    quickNewItem.setAccelerator(KeyStroke.getKeyStroke("N"));
 	    quickNewItem.setActionCommand(actions.quicknewgame);
 	    newMenu.add(quickNewItem);
 	    
 	    JMenuItem place = new JMenuItem("Place own ships...", KeyEvent.VK_P);
 	    place.addActionListener(menuListener);
 	    place.setActionCommand(actions.newplaceownships);
 	    newMenu.add(place);
 	    
 	    JMenuItem generate = new JMenuItem("New Game (using generated level)", KeyEvent.VK_G);
 	    generate.addActionListener(menuListener);
 	    generate.setActionCommand(actions.newgenerated);
 	    newMenu.add(generate);
 	    
 	    JMenuItem saveItem = new JMenuItem("Save as...", KeyEvent.VK_S);
 	    saveItem.addActionListener(menuListener);
 	    saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
 	    saveItem.setActionCommand(actions.save);
 	    fileMenu.add(saveItem);
 	    
 	    JMenuItem loadItem = new JMenuItem("Load from...", KeyEvent.VK_L);
 	    loadItem.addActionListener(menuListener);
 	    loadItem.setAccelerator(KeyStroke.getKeyStroke("control L"));
 	    loadItem.setActionCommand(actions.load);
 	    fileMenu.add(loadItem);
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * entry point
 	 * @param args not used
 	 */
 	public static void main(String[] args) {
 		new GUISchiffe();
 	}
 
 	/**
 	 * show error message to user
 	 * @param message message to show
 	 */
 	void userError(String message) {
 		JOptionPane.showMessageDialog(frame,
 			    message, 
 			    "Error", 0);
 	}
 	
 	/**
 	 * randomly loads level from built-in collection. this functionality is required by minimal assignment level
 	 */
 	private void quickNewGame() {
 		String levelDir = "./template/resources/levels/defaultlevels/";
 		
 		File dir = new File(levelDir);
 
 		String[] children = dir.list();
 		
 		int chosenLvl = -1;
 		
 		for (;;) {
 			Random gen = new Random();
 			chosenLvl = gen.nextInt(children.length);
 			String ext;
 			try {
 				ext = children[chosenLvl].substring(children[chosenLvl].length()-4);
 			} catch (StringIndexOutOfBoundsException e) {
 				ext = ".tooshort";
 			}
 			if (ext.equals(".lvl")) break;
 			else if (!ext.equals(".svn")) userError("Unknown file in levels dir: " + children[chosenLvl]);
 		}
 		
 		Level level;
 		
 		try {
 			level = new Level(readFileAsString(levelDir + children[chosenLvl]));
 		} catch (InvalidLevelException e) {
 			userError(String.format("Template level corrupted: %s",children[chosenLvl]));
 			return;
 		} catch (IOException e) {
 			userError("IO Error: " + e.getMessage());
 			return;
 		}
 		
 		initNewEngineAndAI();
 		
 		frame.dispose();
 		showFrame();
 	}
 	
 	private void initNewEngineAndAI() {
 		engine = new Engine();
 		ai = new GoodAI(engine);
 	}
 	
 	private void generatedNewGame() {
 		initNewEngineAndAI();
 		
 		frame.dispose();
 		showFrame();
 	}
 	
 	private void save() {
 		String saveGameName;
 		
 	    JFileChooser chooser = new JFileChooser();
 
 	    int returnVal = chooser.showSaveDialog(frame);
 	    if(returnVal == JFileChooser.APPROVE_OPTION) {
 	    	saveGameName = chooser.getSelectedFile().getPath();
 	    } else {
 	    	return;
 	    }
 		
 		try {
 			FileOutputStream fileOut = new FileOutputStream(saveGameName);
 			ObjectOutputStream out = new ObjectOutputStream(fileOut);
 			out.writeObject(engine.getState());
 			out.close();
 			fileOut.close();
 		} catch (IOException i) {
 			i.printStackTrace();
 		}
 	}
 	
 	private void load() {
 		String saveGameName;
 		
 	    JFileChooser chooser = new JFileChooser();
 
 	    int returnVal = chooser.showOpenDialog(frame);
 	    if(returnVal == JFileChooser.APPROVE_OPTION) {
 	    	saveGameName = chooser.getSelectedFile().getPath();
 	    } else {
 	    	return;
 	    }
 		
 		State e = null;
 		try {
 			FileInputStream fileIn = new FileInputStream(saveGameName);
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			e = (State) in.readObject();
 			in.close();
 			fileIn.close();
 		} catch (IOException i) {
 			userError(i.getMessage());
 			return;
 		} catch (ClassNotFoundException c) {
 			userError("State class not found");
 			c.printStackTrace();
 			return;
 		}
 		engine.setState(e);
 		
 		frame.dispose();
 		showFrame();
 	}
 	
 	/**
 	 * reads file into string. from here: http://snippets.dzone.com/posts/show/1335
 	 * @param filePath file to load
 	 * @return string with file contents
 	 * @throws IOException
 	 */
 	private static String readFileAsString(String filePath) throws IOException{
 	    byte[] buffer = new byte[(int) new File(filePath).length()];
 	    BufferedInputStream f = null;
 	    try {
 	        f = new BufferedInputStream(new FileInputStream(filePath));
 	        f.read(buffer);
 	    } finally {
 	        if (f != null) try { f.close(); } catch (IOException ignored) { } // doesn't matter if we get error while closing, since we're just doing it to be nice
 	    }
 	    return new String(buffer);
 	}
 
 	@Override
 	public void bomb(int player, int x, int y) {
 			if (player == 0) return;
 			
 			try {
 				engine.attack(Engine.otherPlayer(player), y, x);
 			} catch (InvalidInstruction e) {
 				userError(e.getMessage());
 				return;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 				
 			if (engine.isFinished()) { GameOver(); return; }
 
 			aiAttackAs(player);
 			
 			panels[player].refresh();
 	}
 	
 	void aiAttackAs(int player) {
 		int i = 0;
 		while (!engine.getState().isPlayerTurn()) {
 			System.out.println(i++ + " AI plays as " + player); // debug output to detect runaway AI
 			ai.playAs(player);
 		}
 		panels[Engine.otherPlayer(player)].refresh();
 		
 		if (engine.isFinished()) { GameOver(); return; }
 		
 		if (speerfeuer) clock.resetCountdown();
 	}
 }
