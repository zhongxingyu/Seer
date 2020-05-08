 package maze;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import maze.Maze.Listener;
 
 public class RandomMaze extends JFrame {
 
 	private int rowN = 10;
 	private int colN = 10;
 	private Cell[][] board = new Cell[rowN][colN];
 	private Cell[][] testBoard = new Cell[rowN][colN];
 	private JButton jbtFindPath = new JButton("Find Path");
 	private JButton jbtClearPath = new JButton("Clear Path");
 	private JButton jbtNewMaze = new JButton("New Maze");			// added 5/8 JB
 	private JButton jbtClose = new JButton("Close");				// added 5/8 JB
 	private JPanel jpBoard, jpButton;
 	private JLabel jlblStatus = new JLabel("Started..");
 	private boolean pressed = false;				// added 5/8 JB
 
 	private boolean pathFound = false;
 	
 	
 	public void init() {
 		setSize(500,500);
 		jpBoard = new JPanel();
 		jpBoard.setLayout(new GridLayout(rowN, colN));
 
 		jpButton = new JPanel();
 		jpButton.setLayout(new FlowLayout());
 		jpButton.add(jbtFindPath);
 		jpButton.add(jbtClearPath);
 		jpButton.add(jbtNewMaze);			// added 5/8 JB
 		jpButton.add(jbtClose);				// added 5/8 JB
 
 		// Add cells to jpBoard
 		//		for (int row = 0; row < testBoard.length; row++) {
 		//			for (int col = 0; col < testBoard[row].length; col++) {
 		//				testBoard[row][col] = new Cell();
 		//			}
 		//		}
 		board = createRandomMaze(testBoard);
 		for (int row = 0; row < board.length; row++) {
 			for (int col = 0; col < board[row].length; col++) {
 				//				board[row][col] = new Cell();
 				jpBoard.add(board[row][col]);
 			}
 		}
 		// Add jpBoard, jpButtons and jlblStatus to the applet
 		setLayout(new BorderLayout());
 		add(jlblStatus, BorderLayout.NORTH);
 		add(jpBoard, BorderLayout.CENTER);
 		add(jpButton, BorderLayout.SOUTH);
 		setMarkers();
 		// Register listeners
 		jbtFindPath.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(pathFound){
 					clearPath();
 					//resetPath();
 					findPath();
 				}else{
 					findPath();
 				}
 			}
 		});
 		jbtClearPath.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				clearPath();
 				setMarkers();
 			}
 		});
 		jbtNewMaze.addActionListener(new ActionListener(){		// added 5/8
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				clearAll();
				jpBoard.removeAll();
				board = createRandomMaze(testBoard); // we should work on this so the new maze button can create a new random maze
				for (int row = 0; row < board.length; row++) {
					for (int col = 0; col < board[row].length; col++) {
						jpBoard.add(board[row][col]);
					}
				}
				revalidate();
 			}
 		});
 		jbtClose.addActionListener(new ActionListener(){		// added 5/8
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				closeWindow();
 
 			}
 		});
 	}
 	/** 
 	 * method for closing the window
 	 * 
 	 * Jason
 	 */
 	public void closeWindow(){
 
 		WindowEvent winClosingEvent = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
 		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(winClosingEvent);
 	}
 
 
 
 	/**
 	 * clearAll method to clear the entire maze
 	 * 
 	 * Jason
 	 */
 
 	public void clearAll() {								// added 5/8 JB
 		for (int row = 0; row < board.length; row++) {
 			for (int col = 0; col < board[row].length; col++) {
 				board[row][col].resetCell();
 			}
 		}
 	}
 
 
 
 
 	public boolean isPressed(){			// added 5/8 JB
 		return pressed;
 	}
 
 
 	public void setPressed(boolean e){		// added 5/8 JB
 		pressed = e;
 	}
 
 	protected void resetPath() {
 		pathFound = false;
 		repaint();
 		for (int row = 0; row < board.length; row++) {
 			for (int col = 0; col < board[row].length; col++) {
 				board[row][col].unblock();
 				board[row][col].deselectCell();
 				unblock(row, col);
 			}
 		}
 		revalidate();
 		repaint();
 	}
 	public Cell[][] createRandomMaze(Cell[][] nBoard){
 		for (int row = 0; row < nBoard.length; row++) {
 			for (int col = 0; col < nBoard[row].length; col++) {
 				nBoard[row][col] = new Cell();
				if((row > 0 && col > 0)){
					int randSeed = (int)(Math.random() * 100 + 1);
					if(randSeed > 50)
						nBoard[row][col].setMarked();
				}
 			}
 		}
 		MazeTester newMaze = new MazeTester(nBoard, rowN, colN );
 		if(!newMaze.isTraversable())
 			createRandomMaze(nBoard);
 		return nBoard;
 	}
 	public void printBoard(Cell[][] newBoard){
 		for (int row = 0; row < newBoard.length; row++) {
 			for (int col = 0; col < newBoard[row].length; col++) {
 				System.out.print(newBoard[row][col].marked() + " | ");
 			}
 			System.out.println();
 		}
 	}
 	
 	/**
 	 * I like the setMarkers function! we should put a 
 	 * start and finish graphic in these cells
 	 * 
 	 */
 	public void setMarkers(){
 		board[0][0].setBackground(Color.green);
 		board[rowN-1][colN-1].setBackground(Color.orange);
 	}
 	// Main method
 	public static void main(String[] args) {
 		// Create a frame
 		//JFrame frame = new JFrame("Maze");
 
 		// Create an instance of MortgageApplet
 		RandomMaze frame = new RandomMaze();
 
 		// Add the applet instance to the frame
 		//frame.add(applet, BorderLayout.CENTER);
 
 		// Invoke init() and start()
 		frame.init();
 		//		applet.start();
 
 		// Display the frame
 		frame.setSize(500, 500);
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.setLocationRelativeTo(null); // Center the frame
 		frame.setVisible(true);
 	}
 
 	public void findPath() {
 		//		printBoard(board);
 		if (findPath(0, 0)) {
 			jlblStatus.setText("path found");
 			pathFound = true;
 		}
 		else {
 			jlblStatus.setText("No path exists");
 		}
 	}
 
 	public boolean findPath(int row, int col) {
 		board[row][col].visit();
 
 		if ((col == colN-1) && (row == rowN-1)) {
 			board[row][col].selectCell();
 			return true;
 		}
 		if ((row < rowN-1) && !board[row + 1][col].marked() &&
 				!board[row + 1][col].blocked() && !board[row + 1][col].visited()) {
 			block(row, col);
 
 			if (findPath(row + 1, col)) {
 				board[row][col].selectCell();
 				return true;
 			}
 
 			unblock(row, col);
 		}
 		if ((col < colN-1) && !board[row][col + 1].marked() &&
 				!board[row][col + 1].blocked() && !board[row][col + 1].visited()) {
 			block(row,col);
 			if (findPath(row, col + 1)) {
 				board[row][col].selectCell();
 				return true;
 			}
 
 			unblock(row,col);
 		}
 		if ((row > 0) && !board[row - 1][col].marked() &&
 				!board[row - 1][col].blocked() && !board[row - 1][col].visited()) {
 			block(row, col);
 
 			if (findPath(row - 1, col)) {
 				board[row][col].selectCell();
 				return true;
 			}
 
 			unblock(row, col);
 		}
 		if ((col > 0) && !board[row][col - 1].marked() &&
 				!board[row][col - 1].blocked() && !board[row][col - 1].visited()) {
 			block(row,col);
 			if (findPath(row, col - 1)) {
 				board[row][col].selectCell();
 				return true;
 			}
 
 			unblock(row,col);
 		}
 		return false;
 
 	}
 
 	// Temporary block the neighbor to prevent neighboring path
 	public void block(int row, int col) {
 		if (row > 0) {
 			board[row - 1][col].block();
 		}
 
 		if (row < rowN-1) {
 			board[row + 1][col].block();
 		}
 
 		if (col > 0) {
 			board[row][col - 1].block();
 		}
 
 		if (col < colN-1) {
 			board[row][col + 1].block();
 		}
 	}
 
 	// Remove the temporary block
 	public void unblock(int row, int col) {
 		if (row > 0) {
 			board[row - 1][col].unblock();
 		}
 
 		if (row < rowN-1) {
 			board[row + 1][col].unblock();
 		}
 
 		if (col > 0) {
 			board[row][col - 1].unblock();
 		}
 
 		if (col < colN-1) {
 			board[row][col + 1].unblock();
 		}
 	}
 	public void clearPath() {
 		for (int row = 0; row < board.length; row++) {
 			for (int col = 0; col < board[row].length; col++) {
 				board[row][col].deselectCell();
 			}
 		}
 	}
 	// Inner class
 	class Cell extends JPanel implements MouseListener {
 		private BufferedImage image;
 		private boolean marked = false;
 		private boolean visited = false;
 		private boolean blocked = false;
 		public Cell() {
 			setBackground(Color.DARK_GRAY);
 			addMouseListener(this);
 		}
 		public boolean marked() {
 			return marked;
 		}
 		public void setMarked(){
 			marked = true;
 		}
 		public void visit() {
 			visited = true;
 		}
 		public boolean visited() {
 			return visited;
 		}
 		public boolean blocked() {
 			return blocked;
 		}
 		public void block() {
 			blocked = true;
 		}
 		public void unblock() {
 			blocked = false;
 		}
 		public void selectCell() {
 			setBackground(Color.red);
 			repaint();
 		}
 		public void deselectCell() {
 			if(visited){
 				setBackground(Color.DARK_GRAY);
 			}
 			visited = false;
 			blocked = false;
 			repaint();
 		}
 
 		public void resetCell(){				// added 5/8 JB
 			setBackground(Color.DARK_GRAY);
 			marked = false;
 			visited = false;
 			blocked = false;
 			repaint();
 
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			if (marked) {
 				try{
 					image = ImageIO.read(new File("Brick.jpg"));
 					Image scaledImage = image.getScaledInstance(getSize().width, getSize().height, Image.SCALE_DEFAULT);
 					//ImageIcon icon = new ImageIcon(scaledImage);
 					g.drawImage(scaledImage, 0 ,0,null);
 				}catch(IOException ex){};
 			}
 			
 		}
 		public void mouseClicked(MouseEvent e) {
 		}
 		public void mouseEntered(MouseEvent e) {
 			if(isPressed()){
 				marked = true;
 				repaint();
 			}
 		}
 		public void mouseExited(MouseEvent e) {
 		}
 		public void mouseReleased(MouseEvent e) {
 			setPressed(false);
 		}
 		public void mousePressed(MouseEvent e) {
 			setPressed(true);
 			marked = !marked;
 			repaint();
 		}
 	}
 }
