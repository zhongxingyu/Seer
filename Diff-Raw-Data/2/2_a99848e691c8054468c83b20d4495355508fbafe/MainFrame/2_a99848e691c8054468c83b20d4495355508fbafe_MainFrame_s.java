 package screen;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 
 import main.Main;
 
 public class MainFrame extends JFrame {	
 	private final String[] players = {"Human", "Computer"};	
 	
 	GamePanel pnbot;
 	
 	JButton playButton, stopButton, undoButton, exitButton;
 	JTextField txth, txtw;
 	
 	MainFrame() {		
 		super("Paper Soccer");
 		this.setSize(960, 720);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setVisible(true);
 		
 		class keyListener implements KeyListener {
 			@Override
 			public void keyPressed(KeyEvent arg0) {}
 			@Override
 			public void keyReleased(KeyEvent arg0) {}
 			@Override
 			public void keyTyped(KeyEvent e) {
 				if (e.getKeyChar() < '0' || e.getKeyChar() > '9') e.consume();
 			}
 		}
 		
 		keyListener kl = new keyListener();
 		
 		pnbot = new GamePanel();
 		
 		JPanel pntop = new JPanel();
 		pntop.setLayout(new GridLayout(3,1));
 		
 		JPanel pnl1 = new JPanel();
 		pnl1.setLayout(new FlowLayout());
 		pnl1.add(new JLabel("Select players"));
 		JComboBox cb1 = new JComboBox();
 		JComboBox cb2 = new JComboBox();
 		for (int i=0; i<2; i++) {
 			cb1.addItem(players[i]);
 			cb2.addItem(players[i]);
 		}
 		pnl1.add(cb1);
 		pnl1.add(new JLabel("vs"));
 		pnl1.add(cb2);
 		pntop.add(pnl1);
 		
 		JPanel pnl2 = new JPanel();
 		pnl2.setLayout(new FlowLayout());
 		pnl2.add(new JLabel("Enter height"));
 		txth = new JTextField(8);
 		txth.setText("10");
 		txth.addKeyListener(kl);
 		pnl2.add(txth);
 		pnl2.add(new JLabel("and width"));
 		txtw = new JTextField(8);
 		txtw.setText("8");
 		txtw.addKeyListener(kl);
 		pnl2.add(txtw);
 		pntop.add(pnl2);
 		
 		playButton = new JButton("Play");
 		playButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Start");
 				try {
 					int bH = Integer.valueOf(txth.getText());
 					int bW = Integer.valueOf(txtw.getText());
 					if (bH < 8 || bW < 8 || bH > 16 || bW > 16
 						|| bH%2 != 0 || bW%2 != 0) throw new Exception();
 					pnbot.boardHeight = bH;
 					pnbot.boardWidth = bW;
 					Main.controller.start(pnbot.boardWidth, pnbot.boardHeight, 0, 0);
 					pnbot.setClickable(true);
 					playButton.setEnabled(false);
 					stopButton.setEnabled(true);
 					undoButton.setEnabled(true);
 				}
 				catch (Exception e) {
 					System.out.println("Exception");
					JOptionPane.showMessageDialog(null, "Width and height dimmensions have to be odd, not lower than 8 and not bigger than 16.",
 							"Error", JOptionPane.ERROR_MESSAGE);
 				}				
 			}			
 		});
 		
 		stopButton = new JButton("Stop");
 		stopButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				playButton.setEnabled(true);
 				stopButton.setEnabled(false);
 				undoButton.setEnabled(false);
 				pnbot.setClickable(false);
 			}
 		});
 		
 		undoButton = new JButton("Undo");
 		
 		exitButton = new JButton("Exit");
 		exitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 					JOptionPane.showMessageDialog(null, "Exit button pressed.", "Exit", JOptionPane.WARNING_MESSAGE);
 					System.exit(0);
 			}			
 		});
 		
 		JPanel pnl4 = new JPanel();
 		pnl4.setLayout(new FlowLayout());
 		pnl4.add(playButton);
 		pnl4.add(stopButton);
 		pnl4.add(undoButton);
 		pnl4.add(exitButton);
 		pntop.add(pnl4);		
 		
 		stopButton.setEnabled(false);
 		undoButton.setEnabled(false);
 		
 		this.setLayout(new BorderLayout());
 		
 		this.add(pntop, BorderLayout.NORTH);		
 		
 		this.add(pnbot);
 	}
 }
