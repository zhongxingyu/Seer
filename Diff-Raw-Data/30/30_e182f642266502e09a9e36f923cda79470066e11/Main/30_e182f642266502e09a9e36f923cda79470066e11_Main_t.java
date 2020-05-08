 package ui;
 
 import java.awt.*;
 import javax.swing.*;
 
 import com.jgoodies.forms.layout.*;
 
 @SuppressWarnings("serial")
 public class Main extends JFrame {
 	public Piece[][] panel = new Piece[8][8];
 	public Chessboard board, initialBoard;
 	public History history = new History();
 	public Info infoWindow;
 	public Evaluate evaluateWindow;
 	public Invoker invoke = new Invoker();
 	public boolean controllable = false, finished = false, evaluate,
			showBoard = true, evaluating = true;
 	// winner: 1 black, -1 white, 0 tie
 	public int winner = 0;
 
 	public Main() {
 		super("黑白棋");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 500, 500);
 		setMinimumSize(new Dimension(500, 500));
 
 		// Choose manual or AI
 		try {
 			new Mode(this);
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "配置文件错误", "错误",
 					JOptionPane.ERROR_MESSAGE);
 			System.exit(-1);
 		}
 
 		getContentPane().add(boardPane());
 	}
 
 	public JPanel boardPane() {
 		// Form Layout
 		ColumnSpec[] colSpec = new ColumnSpec[8];
 		for (int i = 0; i < 8; i++)
 			colSpec[i] = ColumnSpec.decode("default:grow");
 		RowSpec[] rowSpec = new RowSpec[8];
 		for (int i = 0; i < 8; i++)
 			rowSpec[i] = RowSpec.decode("default:grow");
 		JPanel boardPane = new JPanel();
 		boardPane.setLayout(new FormLayout(colSpec, rowSpec));
 
 		// Add pieces
 		for (int i = 0; i < 8; i++)
 			for (int j = 0; j < 8; j++) {
 				panel[i][j] = new Piece(this, new Coordinate(i, j));
 				boardPane.add(panel[i][j], (i + 1) + ", " + (j + 1)
 						+ ", fill, fill");
 			}
 
 		return boardPane;
 	}
 
 	public void start(String map) {
 		// Add info frame
 		infoWindow = new Info(this);
 		if (!evaluate)
 			infoWindow.updateLabel(this);
 
 		// Add evaluate frame, and backup chessboard
 		if (evaluate) {
 			evaluateWindow = new Evaluate(this);
 			evaluateWindow.updateLabel(this);
 			initialBoard = board.clone();
 		}
 
 		// Invoke
 		invoke.invoke(this);
 
 		// Show window
 		setVisible(true);
 	}
 
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 		}
 
 		// Launch
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				new Main();
 			}
 		});
 	}
 }
