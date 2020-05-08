 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 
 public class Game implements Runnable {
 
 	@Override
 	public void run() {
 		// Top-level frame
 		final JFrame frame = new JFrame("JTron: 1.0");
 
 		// Header panel
 		final JPanel panel = new JPanel();
 		panel.setOpaque(true);
 		panel.setBackground(Color.BLACK);
 		panel.setBorder(BorderFactory.createLineBorder(Color.CYAN, 5));
 		frame.add(panel, BorderLayout.NORTH);
 
 		// Logo
 		try {
 			BufferedImage logo;
 			try {
 				logo = ImageIO.read(new File("src/images/logo.png"));
			} catch (FileNotFoundException e1) {
 				logo = ImageIO.read(new File("images/logo.png"));
 			}
 			final JLabel logoLabel = new JLabel(new ImageIcon(logo));
 			panel.add(logoLabel);
 		} catch (IOException e1) {
 			System.err.println("Error: IOException loading logo.png");
 		}
 
 		// Instructions button
 		final JButton instructions = new JButton("Instructions");
 		panel.add(instructions);
 
 		// Reset button
 		final JButton reset = new JButton("New Game (N)");
 		panel.add(reset);
 
 		// pause button
 		final JButton pause = new JButton("Pause (P)");
 		panel.add(pause);
 
 		// Score
 		final ScoreKeeper score = new ScoreKeeper();
 		panel.add(score);
 
 		// Main playing area
 		final Arena arena = new Arena(score);
 		arena.setBorder(BorderFactory.createLineBorder(Color.CYAN));
 		frame.add(arena, BorderLayout.CENTER);
 		reset.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				arena.reset();
 			}
 		});
 		pause.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				arena.togglePause();
 			}
 		});
 		instructions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				arena.pause();
 				BufferedReader bf;
 				JTextArea txtDisplay = new JTextArea(30, 60);
 				try {
 					bf = new BufferedReader(new FileReader("instructions.txt"));
 					txtDisplay.read(bf, "txtDisplay");
 					bf.close();
 				} catch (FileNotFoundException e1) {
 					try {
 						bf = new BufferedReader(new FileReader(
 								"src/instructions.txt"));
 						txtDisplay.read(bf, "txtDisplay");
 						bf.close();
 					} catch (IOException e2) {
 						System.err.println("Error: instructions.txt not found");
 					}
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				final JDialog dialog = new JDialog();
 				dialog.setModal(true);
 				dialog.add(txtDisplay);
 				dialog.pack();
 				dialog.setVisible(true);
 				arena.requestFocusInWindow();
 			}
 		});
 
 		// Put the frame on the screen
 		frame.pack();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 
 		// Start the game running
 		arena.reset();
 
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Game());
 
 	}
 
 }
