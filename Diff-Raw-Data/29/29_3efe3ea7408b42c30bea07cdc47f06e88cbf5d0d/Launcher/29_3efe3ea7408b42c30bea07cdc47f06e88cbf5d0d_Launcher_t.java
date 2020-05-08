 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 public class Launcher {
 
 	/**
 	 * @param args
 	 */
 	private ImageIcon image;
 	private Player p;
 	private View v1;
 	private View v2;
 	private View v3;
 	private Game game;
 	private String language;
 	private GamePanel gamePanel;
 
 	public Launcher() {
 		this.language = "english";
 		this.image = new ImageIcon("main.jpg");
 		this.p = new Player("Tyler");
 		Item taserItem = new Item("taser", "this taser hurts bad guys",new 
 				ImageIcon("taser.jpg"), new Region(0,0,32,60));
 		Region taserRegion = new Region(56, 320, 25, 60, taserItem);
 		
 		this.v3 = new View("french image view", new ImageIcon("mainfrench.jpg"));
 		this.v1 = new View("main image view", image, v3);
 		this.v2 = new View("taser is gone!",new ImageIcon("notaser.jpg"));
 		v1.addRegion(taserRegion);
 		v1.addView(v2);
 		this.game = new Game(p, v1);
 
 	}
 	public Game getGame() {
 		return this.game;
 	}
 
 
 	public static void main(String[] args) throws Exception {
 		Launcher l = new Launcher();
 		ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(
 		        new FileOutputStream("C:/Users/Administrator/Documents/csse376/BEERJava/BEERGame/objects.bin")));
 		objectOut.writeObject(l.getGame());
 		ObjectInputStream objectIn = null;
 		Game game2 = null;
 		objectIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
         "C:/Users/Administrator/Documents/csse376/BEERJava/BEERGame/objects.bin")));
 		objectOut.close();
 		game2 = (Game) objectIn.readObject();
 		objectIn.close();
 		InventoryPanel invPanel = new InventoryPanel(game2);
 		l.gamePanel = new GamePanel(l.getGame(), invPanel);
 		JFrame frame = new JFrame();
 		GameBar bar = new GameBar(frame, l);
 	
 		frame.setJMenuBar(bar);
 //		JMenu languageMenu = new JMenu("Language");
 //		JMenuItem french = new JMenuItem("French");
 //		french.addActionListener(new ActionListener() {
 //
 //			@Override
 //			public void actionPerformed(ActionEvent arg0) {
 //				System.out.println("french");
 //				//Launcher.this.changeLanguage();
 //				//no enclosing type error
 //			}
 //
 //		});
 //		languageMenu.add(french);
 //		JMenuItem english = new JMenuItem("English");
 //		english.addActionListener(new ActionListener() {
 //
 //			@Override
 //			public void actionPerformed(ActionEvent arg0) {
 //				System.out.println("english");
 //			}
 //
 //		});
 //		languageMenu.add(english);
 //		bar.add(languageMenu);
 //		
 		frame.setLayout(new FlowLayout());
 		frame.add(l.gamePanel);
 		frame.add(invPanel);
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public static class GameBar extends JMenuBar {
 		public Launcher l;
 		public GameBar (JFrame frame, Launcher launch) {
 			frame = frame;
 			l = launch;
 			JMenu languageMenu = new JMenu("Language");
 			JMenuItem french = new JMenuItem("French");
 			french.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
					GameBar.this.l.getGame().setLanguage("french");
 					GameBar.this.l.gamePanel.repaint();
 				}
 
 			});
 			languageMenu.add(french);
 			JMenuItem english = new JMenuItem("English");
 			english.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
					GameBar.this.l.getGame().setLanguage("english");
 					GameBar.this.l.gamePanel.repaint();
 				}
 
 			});
 			languageMenu.add(english);
 			this.add(languageMenu);
 		}
 	}
 
 
 }
 
 
 
