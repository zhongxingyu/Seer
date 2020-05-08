 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class BoardPanel extends JPanel implements ActionListener, PulseDelegate {
 	private static final int NUM_OF_SQUARES = 20;
 	public static final int CYCLE_DURATION = 50;
 	private static final double PULSE_DECAY = 0.25;
 	
 	private Square[][] board;
 	private Timer timer;
 
 	public BoardPanel() {
 		board = new Square[NUM_OF_SQUARES][NUM_OF_SQUARES];
 		
 		setLayout(new GridLayout(NUM_OF_SQUARES, NUM_OF_SQUARES));
 		
 		for (int r = 0; r < NUM_OF_SQUARES; r++) {
 			for (int c = 0; c < NUM_OF_SQUARES; c++) {
 				board[r][c] = new Square(r, c);
 				board[r][c].setPulseDelegate(this);
 				add(board[r][c]);
 			}
 		}
 
 		timer = new Timer(CYCLE_DURATION, this);
 		timer.start();
 		
 		setBackground(Color.GREEN);
 		setPreferredSize(new Dimension(500, 500));
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource() == timer) {
 			for (int r = 0; r < NUM_OF_SQUARES; r++) {
 				for (int c = 0; c < NUM_OF_SQUARES; c++) {
 					board[r][c].timerFired();
 				}
 			}
 		}
 	}
 	
	public void pulseOccurred(Square source) {
 		int r = source.getRow();
 		int c = source.getColumn();
 		
 		int pulsePower = (int)(source.getPower() - Math.ceil(source.getPower() * PULSE_DECAY));
 		
 		//Top Left Corner
 		if (r - 1 >= 0 && !board[r - 1][c].isPulsing())
 			board[r - 1][c].toggleWithPulse(pulsePower);
 		if (c - 1 >= 0 && !board[r][c - 1].isPulsing())
 			board[r][c - 1].toggleWithPulse(pulsePower);
 		if (r - 1 >= 0 && c - 1 >= 0 && !board[r - 1][c - 1].isPulsing())
 			board[r - 1][c - 1].toggleWithPulse(pulsePower);
 		
 		//Bottom Right Corner
 		if (r + 1 < NUM_OF_SQUARES && !board[r + 1][c].isPulsing())
 			board[r + 1][c].toggleWithPulse(pulsePower);
 		if (c + 1 < NUM_OF_SQUARES && !board[r][c + 1].isPulsing())
 			board[r][c + 1].toggleWithPulse(pulsePower);
 		if (r + 1 < NUM_OF_SQUARES && c + 1 < NUM_OF_SQUARES && !board[r + 1][c + 1].isPulsing())
 			board[r + 1][c + 1].toggleWithPulse(pulsePower);
 		
 		if (r + 1 < NUM_OF_SQUARES && c - 1 >= 0 && !board[r + 1][c - 1].isPulsing())
 			board[r + 1][c - 1].toggleWithPulse(pulsePower);
 		if (r - 1 >= 0 && c + 1 < NUM_OF_SQUARES && !board[r - 1][c + 1].isPulsing())
 			board[r - 1][c + 1].toggleWithPulse(pulsePower);
 	}
 }
