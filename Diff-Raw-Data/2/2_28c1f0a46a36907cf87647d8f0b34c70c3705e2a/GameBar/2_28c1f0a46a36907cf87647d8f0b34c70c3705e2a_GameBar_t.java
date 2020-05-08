 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.Serializable;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 /**
  ** @author vitalema and hannantt
  * 
  *         This class creates and manages the GameBar
  * 
  */
 @SuppressWarnings("serial")
 public class GameBar extends JMenuBar implements Serializable {
 	private Launcher l;
 	private JMenuItem french;
 	private JMenuItem english;
 	private JMenuItem pause;
 	private JMenu fileMenu;
 	private JMenuItem newGame;
 
 	/**
 	 * @param frame
 	 *            - the jframe
 	 * @param launch
 	 *            - the launcher
 	 */
 	public GameBar(Launcher launch) {
 		l = launch;
 		fileMenu = new JMenu("File");
 		newGame = new JMenuItem("New Game");
 		newGame.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					Game g = GameBar.this.l.readGame();
 					String temp = GameBar.this.l.getGame().getLanguage();
 					GameBar.this.l.setGame(g);
 					GameBar.this.l.getGame().setLanguage(temp);
 					GameBar.this.l.getGamePanel().repaint();
 
 					GameBar.this.l.getGamePanel().getSidePanel().getInvPanel()
 							.setSelected(null);
 					GameBar.this.l.getGamePanel().getSidePanel().getInvPanel()
 							.checkCombineItem();
 					GameBar.this.l.getGamePanel().getSidePanel().updateText();
 					GameBar.this.l.getGamePanel().getSidePanel().getInvPanel()
 							.repaint();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		});
 		fileMenu.add(newGame);
 		french = new JMenuItem("French");
 		french.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GameBar.this.french.setText("Franais");
 				GameBar.this.english.setText("Anglais");
 				GameBar.this.l.getGame().setLanguage("french");
 				GameBar.this.fileMenu.setText("Fichier");
 				GameBar.this.newGame.setText("Nouveau jeu");
 				GameBar.this.l.getFrame().setTitle(
						"Le jeu de " + GameBar.this.l.getUserName());
 				GameBar.this.l.getGamePanel().repaint();
 				GameBar.this.l.getGamePanel().getSidePanel().updateText();
 			}
 
 		});
 		fileMenu.add(french);
 		english = new JMenuItem("English");
 		english.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GameBar.this.l.getGame().setLanguage("english");
 				GameBar.this.fileMenu.setText("File");
 				GameBar.this.french.setText("French");
 				GameBar.this.english.setText("English");
 				GameBar.this.newGame.setText("New game");
 				GameBar.this.l.getFrame().setTitle(
 						GameBar.this.l.getUserName() + "'s game");
 				GameBar.this.l.getGamePanel().repaint();
 				GameBar.this.l.getGamePanel().getSidePanel().updateText();
 			}
 
 		});
 		fileMenu.add(english);
 		pause = new JMenuItem("Pause");
 		pause.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GameBar.this.l.getGame().pauseGame();
 				GameBar.this.l.getFrame().setTitle(
 						GameBar.this.l.getUserName() + "'s game");
 				GameBar.this.l.getGamePanel().repaint();
 				GameBar.this.l.getGamePanel().getSidePanel().updateText();
 			}
 
 		});
 		fileMenu.add(pause);
 		this.add(fileMenu);
 	}
 	public JMenuItem getEnglish() {
 		return english;
 	}
 	
 	public JMenuItem getFrench() {
 		return french;
 	}
 	
 	public JMenuItem getPause() {
 		return pause;
 	}
 	
 	public JMenuItem getNewGame() {
 		return newGame;
 	}
 }
