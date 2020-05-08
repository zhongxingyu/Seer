 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.URL;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import sun.awt.SunHints.Value;
 
 
 public class GameWindow extends JFrame implements MouseListener, ActionListener{
 
     JButton mines[][] = new JButton[10][10];
     JPanel pMaster = null;
     JPanel pMines = null;
     
     String currentTheme = "default";
 	public void setUpLayout() {
 		setTitle("YAJSM - Yet Another Java Swing Minesweeper");
 		
 		pMaster = new JPanel();
 		pMaster.setLayout(new BoxLayout(pMaster,BoxLayout.Y_AXIS));
 		setJMenuBar(setUpMenu());
 		pMaster.add(setUpStats());
 		pMaster.add(setUpMines());
 		pMaster.add(setUpInfo());
 		add(pMaster);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 		setVisible(true);
 	}
 	public JPanel setUpStats() {
 		JPanel pStats = new JPanel();
 		pStats.setLayout(new GridLayout(2,2));
 		
 		JLabel lTime = new JLabel("Time: ");
 		JTextField tfTime = new JTextField("00");
 		tfTime.setEditable(false);
 		JLabel lMines = new JLabel("Mines: ");
 		JTextField tfMines = new JTextField("10");
 		tfMines.setEditable(false);
 		
 		pStats.add(lTime);
 		pStats.add(lMines);
 		pStats.add(tfTime);
 		pStats.add(tfMines);
 		return pStats;
 	}
 	public JPanel setUpInfo() {
 		JPanel pInfo = new JPanel();
 		JTextArea lInfo = new JTextArea("Here I will say belittling little facts about your ugly mother");
 		lInfo.setPreferredSize(new Dimension(250,50));
 		lInfo.setLineWrap(true);
 		lInfo.setEditable(false);
 		
 		pInfo.add(lInfo);
 		
 		return pInfo;
 	}
 	
 	public JMenuBar setUpMenu() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu gameMenu = new JMenu("Game");
 		gameMenu.setMnemonic(KeyEvent.VK_G);
 		
 		JMenuItem miNewGame = new JMenuItem("New Game");
 		miNewGame.setMnemonic(KeyEvent.VK_N);
 		JMenuItem miSettings = new JMenuItem("Settings");
 		miSettings.setMnemonic(KeyEvent.VK_E);
 		JMenuItem miStats = new JMenuItem("Statistics");
 		miStats.setMnemonic(KeyEvent.VK_S);
 		JMenuItem miThemes = new JMenuItem("Themes");
 		miThemes.setMnemonic(KeyEvent.VK_T);
 		miThemes.addActionListener(this);
 		JMenuItem miQuit = new JMenuItem("Quit");
 		miQuit.setMnemonic(KeyEvent.VK_Q);
 		miQuit.addActionListener(this);
 		
 		gameMenu.add(miNewGame);
 		gameMenu.addSeparator();
 		gameMenu.add(miSettings);
 		gameMenu.add(miStats);
 		gameMenu.add(miThemes);
 		gameMenu.addSeparator();
 		gameMenu.add(miQuit);		
 		menuBar.add(gameMenu);
 		
 		
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 		JMenuItem miTutorial = new JMenuItem("Tutorial");
 		miTutorial.setMnemonic(KeyEvent.VK_T);
 		JMenuItem miRules = new JMenuItem("Rules");
 		miRules.setMnemonic(KeyEvent.VK_R);
 		JMenuItem miAbout = new JMenuItem("About");
 		miAbout.setMnemonic(KeyEvent.VK_A);
 		
 		helpMenu.add(miTutorial);
 		helpMenu.add(miRules);
 		helpMenu.add(miAbout);
 		
 		menuBar.add(helpMenu);
 		
 		return menuBar;
 	}
 	
 	public JPanel setUpMines() {
 		pMines = new JPanel();
 		pMines.setMaximumSize(new Dimension(250, 250));
 		
 		pMines.setLayout(new GridLayout(10,10));
 		ImageIcon icon = null;
 		try{
 			URL uTiles = getClass().getResource("res/images/default/tile.png");
 			if(uTiles != null) {
 				icon = new ImageIcon(uTiles, "A tile");
 			} else {
 				imagesMissing();
 			}
 		
 		for(int i = 0; i < 10; i++) {
 			for(int j = 0; j < 10; j++) {
 				JButton mine = new JButton(icon);
 				mine.setContentAreaFilled(true);
 				mine.setPreferredSize(new Dimension(25,25));
 				mines[i][j] = mine;
 				mines[i][j].addMouseListener(this);
 				pMines.add(mines[i][j]);
 			}
 		}
 		
 		} catch (NullPointerException npe) {
 			imagesMissing();
 		} 
 		return pMines;
 	}
 	
 	public void themeChooser() {
 		JFrame jfThemes = new JFrame("Themes");
 		
 		JRadioButton rbDefault = new JRadioButton("Default");
 		JRadioButton rbDark = new JRadioButton("Dark");
 		JRadioButton rbUnicorn = new JRadioButton("Unicorn");
 		rbDefault.addActionListener(this);
 		rbDark.addActionListener(this);
 		rbUnicorn.addActionListener(this);
 		
 		ButtonGroup bgThemes = new ButtonGroup();
 		bgThemes.add(rbDefault);
 		bgThemes.add(rbDark);
 		bgThemes.add(rbUnicorn);
 		
 		JPanel pThemes = new JPanel(new GridLayout(0,1));
 		
 		pThemes.add(rbDefault);
 		pThemes.add(rbDark);
 		pThemes.add(rbUnicorn);
 		
 		jfThemes.add(pThemes);
 		jfThemes.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		jfThemes.pack();
 		jfThemes.setVisible(true);
 	}
 	public void quitButtonAction() {
 		final JDialog dQuit = new JDialog(this,"Quit?", true);
 		final JOptionPane opQuit = new JOptionPane("Would you like to quit?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
 		dQuit.setContentPane(opQuit);
 		opQuit.addPropertyChangeListener(new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent e) {
 				 String prop = e.getPropertyName();
 		            if (dQuit.isVisible() 
 		             && (e.getSource() == opQuit)
 		             && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
 		                dQuit.setVisible(false);
 		            }
 			}
 		});
 		dQuit.pack();
 		dQuit.setVisible(true);
 		
 		int result = ((Integer)opQuit.getValue()).intValue();
 		if(result == JOptionPane.YES_OPTION){
 			System.exit(0);
 		} else{
 			dQuit.setVisible(false);
 		}
 	}
 	
 	public void imagesMissing() {
 		JOptionPane.showMessageDialog(this, "There are images missing from the directory. Please redownload the application");
 		System.exit(0);
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		Object o = e.getSource();
 		JButton b = (JButton) o;
 		try{
 			URL uFloor = getClass().getResource("res/images/" + currentTheme + "/floor.png");
 			if(uFloor != null) {
 				ImageIcon icon = new ImageIcon(uFloor, "A floor");
 				b.setIcon(icon);
 			} else {
 				imagesMissing();
 			}
 			
 		} catch (NullPointerException npe) {
 			imagesMissing();
 		} 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		
 	}
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		if(arg0.getSource().getClass() == JMenuItem.class){
 			JMenuItem miToRead = (JMenuItem)arg0.getSource();
 			if(miToRead.getText().equals("Quit")) {
 				quitButtonAction();
 			} else if(miToRead.getText().equals("Themes")) {
 				themeChooser();
 			}
 		}
 		if(arg0.getSource().getClass() == JRadioButton.class) {
 			JRadioButton rbToRead = (JRadioButton)arg0.getSource();
 			if(rbToRead.getText().equals("Default")) {
 				currentTheme = "default";
 			} else if (rbToRead.getText().equals("Dark")){
 				currentTheme = "dark";
 			} else if (rbToRead.getText().equals("Unicorn")) {
 				currentTheme = "unicorn";
 			}
 			ImageIcon icon = new ImageIcon();
 			try{
 				URL uTiles = getClass().getResource("res/images/" + currentTheme + "/tile.png");
 				if(uTiles != null) {
 					icon = new ImageIcon(uTiles, "A tile");
 				} else {
 					imagesMissing();
 				}
 			pMines.removeAll();
 			for(int i = 0; i < 10; i++) {
 				for(int j = 0; j < 10; j++) {
 					JButton mine = new JButton(icon);
 					mine.setContentAreaFilled(true);
 					mine.setPreferredSize(new Dimension(25,25));
 					mines[i][j] = mine;
 					mines[i][j].addMouseListener(this);
 					pMines.add(mines[i][j]);
 				}
 			}
 			
 			} catch (NullPointerException npe) {
 				imagesMissing();
 			} 
 			invalidate();
 			validate();
 		}
 		
 	}
 
 }
