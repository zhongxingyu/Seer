 package de.propra12.gruppe04.dynamiteboy.Menu;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
import de.propra12.gruppe04.dynamiteboy.Game.Game;

 public class ScoreMenu extends JPanel {
 	private JButton buttonStart;
 	private JFrame frame;
 	private TitlePanel title = new TitlePanel();
 
 	/**
 	 * Constructor Sets up MainMenu with a Start Button to start the game
 	 * 
 	 * @param width
 	 *            Window-width
 	 * @param height
 	 *            Window-height
 	 */
 	public ScoreMenu(final JFrame frame) {
 		this.frame = frame;
		buttonStart = new JButton("Erneut Spielen");
 		buttonStart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				// final JFrame frame = new JFrame();
				buttonStart.setText("Spiel wird gestartet...");
				Game game = new Game(frame, 1);
				frame.getContentPane().add(game);
 				buttonStart.setVisible(false);
 				title.setVisible(false);
				game.setVisible(true);
 			}
 		});
 		frame.getContentPane().add(BorderLayout.CENTER, this.title);
 		buttonStart.setPreferredSize(new Dimension(100, 80));
 		frame.getContentPane().add(BorderLayout.SOUTH, buttonStart);
 	}
 
 	static class TitlePanel extends JPanel {
 		private String titleScreenImage = "../images/db_menu_titlescreen.png";
 		ImageIcon img = new ImageIcon(this.getClass().getResource(
 				titleScreenImage));
 
 		public void paintComponent(Graphics g) {
 			g.drawImage(img.getImage(), 0, 0, this);
 
 		}
 
 	}
 
 }
