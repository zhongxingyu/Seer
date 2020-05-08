 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 public class GUI {
 
 	public JFrame frame;
 	private final int FRAME_WIDTH = 1280;
 	private final int FRAME_HEIGHT = 900;
 	private final int BUT_HEIGHT = 100;
 	private final int BUT_WIDTH = 100;
 	public JPanel shopPhase = new JPanel();
 	public JPanel shopCards = new JPanel();
 	public JPanel panel;
 	public boolean isShopPhase = false;
 	public Game game;
 	public Color trans = new Color(255, 255, 255, 0);
 
 	public GUI() {
 		this.frame = new JFrame("Puzzle Strike: Hand of Cards");
 		this.frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.frame.setResizable(false);
 		this.frame.setVisible(true);
 		this.frame.setResizable(false);
 		this.game = new Game();
 		this.game.makePlayers(2);
 		setUp();
 	}
 
 	public class JBackgroundPanel extends JPanel {
 		private BufferedImage img;
 
 		public JBackgroundPanel() {
 			// load the background image
 			try {
 				img = ImageIO.read(new File("./backgroundpanels.png"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			// paint the background image and scale it to fill the entire space
 			g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
 		}
 	}
 	
 	public class JBackgroundButton extends JButton {
 		private BufferedImage img;
 
 		public JBackgroundButton() {
 			// load the background image
 			try {
 				img = ImageIO.read(new File("./cardbg.png"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			// paint the background image and scale it to fill the entire space
 			g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
 		}
 	}
 
 	private void setUp() {
 		this.panel =  new JBackgroundPanel();
 		this.frame.setContentPane(this.panel);
 		this.frame.validate();
 		this.frame.repaint();
 
 		this.shopPhase.setBackground(this.trans);
 		this.shopPhase
 				.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 		this.shopCards.setBackground(this.trans);
 		this.shopCards.setPreferredSize(new Dimension(FRAME_WIDTH, 14*FRAME_HEIGHT/16));
		for (int i = 0; i < this.game.bank.size(); i++) {
 			JButton card = new JBackgroundButton();
			card.add(new JLabel(this.game.bank.get(i).name));
 			card.setPreferredSize(new Dimension(BUT_WIDTH, BUT_HEIGHT));
 			this.shopCards.add(card);
 		}
 
 		class EndShopListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO: this function;
 				endShopPhase();
 			}
 		}
 
 		JButton endPhase = new JButton("End Phase");
 		endPhase.addActionListener(new EndShopListener());
 		this.shopPhase.add(this.shopCards);
 		this.shopPhase.add(endPhase);
 		newTurn();
 	}
 
 	private void newTurn() {
 		JPanel playerStuff = new JPanel();
 		playerStuff.setBackground(this.trans);
 		playerStuff.setPreferredSize(new Dimension(FRAME_WIDTH,
 				FRAME_HEIGHT / 3));
 		JPanel hand = new JPanel();
 		hand.setBackground(this.trans);
 		hand.setPreferredSize(new Dimension(FRAME_WIDTH,
 				2 * FRAME_HEIGHT / 8));
 		for (int i = 0; i < this.game.players.get(this.game.turn).hand.size(); i++) {
 			JButton card = new JBackgroundButton();
			card.add(new JLabel(this.game.players.get(this.game.turn).hand.get(i).name));		
 			card.setPreferredSize(new Dimension(BUT_WIDTH, BUT_HEIGHT));
 			hand.add(card);
 		}
 		JButton endPhase = new JButton("End Phase");
 		class EndListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO: this function;
 				endActionPhase();
 			}
 		}
 
 		endPhase.addActionListener(new EndListener());
 		playerStuff.add(hand);
 		playerStuff.add(endPhase);
 
 		JPanel gemPileStuff = new JPanel();
 
 		for (int i = 0; i < this.game.playerNum; i++) {
 			JLabel name = new JLabel("Player " + (i + 1));
 			name.setForeground(Color.WHITE);
 			JPanel player = new JPanel();
 			player.setPreferredSize(new Dimension(FRAME_WIDTH
 					/ (this.game.playerNum + 1), FRAME_HEIGHT / 2));
 			player.setBackground(this.trans);
 			player.add(name);
 			gemPileStuff.add(player);
 		}
 
 		gemPileStuff.setBackground(this.trans);
 		gemPileStuff.setPreferredSize(new Dimension(FRAME_WIDTH,
 				3 * FRAME_HEIGHT / 5));
 		JPanel mostPhases = new JPanel();
 		mostPhases.setBackground(this.trans);
 		mostPhases.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 		mostPhases.add(gemPileStuff);
 		mostPhases.add(playerStuff);
 
 		if (this.panel.getComponentCount() != 0) {
 			this.panel.removeAll();
 		}
 		this.panel.add(mostPhases);
 
 		this.frame.validate();
 		this.frame.repaint();
 	}
 
 	public void endActionPhase() {
 		this.isShopPhase = true;
 		newShopPhase();
 	}
 
 	public void endShopPhase() {
 		this.isShopPhase = false;
 		newTurn();
 	}
 
 	private void newShopPhase() {
 		// TODO Auto-generated method stub
 
 		if (this.panel.getComponentCount() != 0) {
 			this.panel.removeAll();
 		}
 		this.panel.add(this.shopPhase);
 
 		this.frame.validate();
 		this.frame.repaint();
 	}
 
 }
