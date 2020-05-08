 package edu.jhu.cs.tyung1.oose;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.*;
 
 import edu.jhu.cs.oose.fall2013.brickus.iface.*;
 
 //REVIVE: remove print statements
 @SuppressWarnings("serial")
 public class MyBrickusFrame extends JFrame {
 
 	JFrame frame;
 	BrickusModel model;
 	Composite composite;
 	
 	public static void main(String[] args) {
 
 		BrickusModel model = new edu.jhu.cs.oose.fall2013.brickus.model.StandardBrickusModel();
 		MyBrickusFrame gui = new MyBrickusFrame(model);
 		gui.go();
 	}
 	
 	public MyBrickusFrame(BrickusModel model) {
 		
 		this.model = model;
 	}
 	
 	public void go() {
 		
 		frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		composite = new Composite(model);
 		frame.getContentPane().add(BorderLayout.CENTER, composite);
 		
 		frame.setSize(670,710);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 }
 
 @SuppressWarnings("serial")
 class Composite extends JComponent {
 	
 	MyBrickusBoard board;
 	MyBrickusTray tray;
 	MyBrickusTracker tracker;
 	BrickusPiece activePiece;
 	BrickusModel model;
 	Color player1color;
 	Color player2color;
 	Color player1colorhalf;
 	Color player2colorhalf;
 	boolean pieceSelected;
 	//Color player3color;
 	//Color player4color;
 	int[][] thepiece;
 	
 	public Composite(BrickusModel model) {
 		player1color = Color.decode("#0000CC");
 		player2color = Color.decode("#CC0000");
 		player1colorhalf = new Color(0,0,204,120);
 		player2colorhalf = new Color(204,0,0,120);
 		//player3color = Color.decode("#006600");
 		//player4color = Color.decode("#FF0066");
 		
 		this.model = model;
 		
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 
 		MyMouseListener myListener = new MyMouseListener(this);
 		
 		MyBrickusListener modelListener = new MyBrickusListener(this);
 		model.addBrickusListener(modelListener);
 		board = new MyBrickusBoard(model, myListener);
 		board.setBorder(BorderFactory.createLineBorder(Color.black));
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.gridheight = 1;
 		constraints.weightx = 1;
 		constraints.weighty = 1;
 		this.add(board, constraints);
 
 		tray = new MyBrickusTray(model, myListener); // lower panel of pieces and pass button
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.gridwidth = 1;
 		constraints.gridheight = 1;
 		constraints.weightx = 1;
 		constraints.weighty = 0;
 		this.add(tray, constraints);
 
 		tracker = new MyBrickusTracker(model); // error board, score boards
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		constraints.gridwidth = 1;
 		constraints.gridheight = 1;
 		constraints.weightx = 1;
 		constraints.weighty = 0;
 		this.add(tracker, constraints);
 	}
 	
 	public void updateErrorMessage(String message) {
 		
 		tracker.newErrorMessage(message);
 	}
 	
 	public void pieceClicked(SinglePiece panel) {
 		
 		activePiece = panel.selected();
 		thepiece = new int[activePiece.getHeight()][activePiece.getWidth()];
 		for( int y = 0; y < activePiece.getHeight(); y++){
 			for(int x = 0; x < activePiece.getWidth(); x++){
 				if(activePiece.isOccupied(x, y))
 				{
 					thepiece[y][x] = 1;
 				}
 			}
 		}
 		//I HAVE THE SELECTED PIECE
 		board.pieceSelected();
 	}
 	
 	public void updateScores(int score1, int score2) {
 		
 		tracker.newScores(score1, score2);
 	}
 	
 	public void updateSinglePiece(BrickusModel model) {
 		
 		tray.updateSinglePiece(model);
 	}
 	
 	public void rightClick(){
 		if(pieceSelected){
 			activePiece.flipHorizontally();
 			repaint();
 		}
 	}
 
 	public void shiftRightClick(){
 		if(pieceSelected){
 			activePiece.flipVertically();
 			repaint();
 		}
 	}
 	
 	public void scrollUpWheel(){
 		if(pieceSelected){
 			activePiece.rotateCounterClockwise();
 			repaint();
 		}
 	}
 	
 	public void scrollDownWheel(){
 		if(pieceSelected){
 			activePiece.rotateClockwise();
 			repaint();
 		}
 	}
 	public void paintComponent(Graphics g) {
 		board.repaint();
 		tray.repaint();
 	}
 	
 	class MyBrickusBoard extends JComponent {
 
 		int numCol;
 		int numRow;
 		int[][] mygrid;
 		int coveredx, coveredy; // the indices of the cell over which the mouse is currently.
 		Point coveredCell;
 		int cellWidth = 20;
 		
 		
 		public MyBrickusBoard(BrickusModel model, MyMouseListener myListener) {
 			pieceSelected = false;
 			numCol = model.getWidth();
 			numRow = model.getHeight();
 			mygrid = new int[numRow][numCol];
 			for(int y = 0; y < numRow; y++)
 			{
 				for(int x = 0; x < numCol; x++)
 				{
 					mygrid[y][x] = 0;
 				}
 			}
 			mygrid[0][0] = 1;
 			mygrid[2][2] = 2;
 			mygrid[4][4] = 3;
 			mygrid[6][6] = 4;
 			
 	        MouseAdapter mouseHandler;
 	        mouseHandler = new MouseAdapter() {
 	        	@Override
 	        	public void mouseClicked(MouseEvent e){
 	        		if(e.getButton() == MouseEvent.BUTTON1)
 	        		{
 		                int width = getWidth();
 		                int height = getHeight();
 		
 		                int cellWidth = width / numCol;
 		                int cellHeight = height / numRow;
 		
 		                coveredx = e.getX() / cellWidth;
 		                coveredy = e.getY() / cellHeight;
 		                coveredCell = new Point(coveredx, coveredy);
 		                placePiece();
 	        		}
 	        	}
 	            @Override
 	            public void mouseMoved(MouseEvent e) {
 	                ///Point point = e.getPoint();
 	
 	                int width = getWidth();
 	                int height = getHeight();
 	
 	                int cellWidth = width / numCol;
 	                int cellHeight = height / numRow;
 	
 	                coveredx = e.getX() / cellWidth;
 	                coveredy = e.getY() / cellHeight;
 	                coveredCell = new Point(coveredx, coveredy);
 	
 	                repaint();
 	
 	            }
 	        };
 	        addMouseMotionListener(mouseHandler);
 	        addMouseListener(mouseHandler);
 	        this.addMouseListener(myListener);
 	
 	        
 		}
 		public void pieceSelected(){
 			pieceSelected = true;
 		}
 		public void placePiece(){
 			Player placingPlayer = model.getActivePlayer();
 			int playerNum = 0;
 			if(placingPlayer == Player.PLAYER1){
 				playerNum = 1;
 			}
 			else if(placingPlayer == Player.PLAYER2){
 				playerNum = 2;
 			}
 			model.placePiece(placingPlayer, coveredx, coveredy, activePiece);
 			if(placingPlayer != model.getActivePlayer())
 			{ //successful placement so update model
 				for(int row = 0; row < thepiece.length; row++){
 					for(int col = 0; col < thepiece[0].length; col++){
 						if(thepiece[row][col] == 1){
 							mygrid[coveredy+row][coveredx+col] = playerNum; 		
 						}
 					}
 				}
 				activePiece = null;
 				pieceSelected = false;
 			}
 		}
 		public void paintComponent(Graphics g) {
             super.paintComponent(g);
             int width = this.getWidth();
             int height = this.getHeight();
             g.setColor(Color.BLACK);
             g.fillRect(0,0,width, height);
             int cellWidth = (width-numCol) / (numCol);//+1);
             int cellHeight = (height -numRow)/ (numRow);//+1);
             
 
             int xOffset = (width - (numCol * cellWidth)) / 4;
             int yOffset = (height - (numRow * cellHeight)) / 4;
 			for (int row = 0; row < numRow; row++) {
 		         for (int col = 0; col < numCol; col++) {
 		        	 if(mygrid[row][col] == 0)
 		        	 {
 		        		 g.setColor(Color.WHITE);
 		        	 }
 		        	 else if(mygrid[row][col] == 1)
 		        	 {
 		        		 g.setColor(player1color);
 		        	 }
 		        	 else if(mygrid[row][col] == 2)
 		        	 {
 		        		 g.setColor(player2color);
 		        	 }
 		        	 /*else if(mygrid[row][col] == 3)
 		        	 {
 		        		 g.setColor(player3color);
 		        	 }
 		        	 else if(mygrid[row][col] == 4)
 		        	 {
 		        		 g.setColor(player4color);
 		        	 }*/
 
 		        	 g.fillRect(col*cellWidth+col+xOffset,row*cellHeight+row+yOffset,cellWidth, cellHeight);
 		         }
 		      }
 	        if (coveredCell != null) 
 	        {	//affect the coveredCell
 	        	Color shadow = Color.WHITE;
 				for (int row = 0; row < numRow; row++) {
 			         for (int col = 0; col < numCol; col++) {
 			        	 if(row == coveredy && col == coveredx )
 			        	 {
 			        		 if(model.getActivePlayer() == Player.PLAYER1){// && mygrid[row][col] == 0){
 			        			 shadow = player1colorhalf;
 			        		 }
 			        		 else if(model.getActivePlayer() == Player.PLAYER2){// && mygrid[row][col] == 0){
 			        			 shadow = player2colorhalf;
 			        		 }
 
 			        		 if(pieceSelected)
 			        		 for(int y = 0; y < activePiece.getHeight(); y++){
 			        			 for(int x = 0; x < activePiece.getWidth(); x++){
 			        				 if(thepiece[y][x] == 1){
 			        					 if(col+x < model.getWidth() && row + y < model.getHeight()){
 			        						 g.setColor(shadow);
 			        						 g.fillRect((col+x)*cellWidth+(col+x)+xOffset,(row+y)*cellHeight+(row+y)+yOffset,cellWidth, cellHeight);
 			        						 
 			        					 }
 			        					 
 			        				 }
 			        			 }
 			        		 }
 			        	 }
 			         }
 				}
 	        }
 		}
 		
 	}
 
 	
 	class MyBrickusTray extends JPanel {
 		
 		JButton passButton;
 		MyButtonListener buttonListener;
 		pieceTray tray;
 		MyMouseListener myListener;
 		
 		public MyBrickusTray(BrickusModel model, MyMouseListener myListener) {
 			
 			this.myListener = myListener;
 			
 			this.setLayout(new GridBagLayout());
 			this.setBorder(BorderFactory.createLineBorder(Color.black));		
 			GridBagConstraints constraints = new GridBagConstraints();
 			constraints.fill = GridBagConstraints.BOTH;
 			
 			pieceTray pieceTray = new pieceTray(model, myListener);
 			pieceTray.setBorder(BorderFactory.createLineBorder(Color.black));
 			constraints.gridx = 0;
 			constraints.gridy = 0;
 			constraints.gridwidth = 7;
 			constraints.weightx = 1;
 			this.add(pieceTray, constraints);
 			this.tray = pieceTray;
 			
 			passButton = new JButton("Pass");
 			MyButtonListener buttonListener = new MyButtonListener(model);
 			passButton.addActionListener(buttonListener);
 			this.buttonListener = buttonListener;
 			constraints.gridx = 7;
 			constraints.gridy = 0;
 			constraints.gridwidth = 1;
 			constraints.weightx = 0;
 			this.add(passButton, constraints);
 		}
 		public void paintComponent(Graphics g) {
			this.tray.repaint();
			//this.tray.updateSinglePiece(model, myListener);
 		}
 		
 		public void updateSinglePiece(BrickusModel model) {
 			
			this.tray.updateSinglePiece(model, myListener);
 		}
 	}
 	class pieceTray extends JPanel {
 		MyMouseListener myListener;
 		public pieceTray(BrickusModel model, MyMouseListener myListener) {
 			this.myListener = myListener;
 			this.setLayout(new GridLayout(3, 7));
 			this.setBorder(BorderFactory.createLineBorder(Color.black));
 			
 			for(BrickusPiece piece: model.getPieces(model.getActivePlayer())) {
 				
 				SinglePiece newPiece = new SinglePiece(model, piece, myListener);
 				this.add(newPiece);
 			}
 		}
 		
 		public void updateSinglePiece(BrickusModel model, MyMouseListener myListener) {
 			
 			this.removeAll();
			this.repaint();
 			this.setBackground(Color.white);
 			for(BrickusPiece piece: model.getPieces(model.getActivePlayer())) {
 				SinglePiece newPiece = new SinglePiece(model, piece, myListener);
 				this.add(newPiece);
 			}
 		}
 		
/*		public void paintComponent(Graphics g) {
 			this.removeAll();
 			this.setBackground(Color.white);
 			for(BrickusPiece piece: model.getPieces(model.getActivePlayer())) {
 				SinglePiece newPiece = new SinglePiece(model, piece, myListener);
 				this.add(newPiece);
 			}
		}*/
 	}
 	
 	class SinglePiece extends JPanel {
 		
 		BrickusPiece mypiece;
 		
 		public SinglePiece(BrickusModel model, BrickusPiece piece, MyMouseListener myListener) {
 			mypiece = piece;
 			this.setLayout(new GridLayout(5, 5));
 			this.setBackground(Color.white);
 			this.addMouseListener(myListener);
 			int heightBuffer = calculateBuffer(piece.getHeight());
 			int widthBuffer = calculateBuffer(piece.getWidth());
 			
 			Player activePlayer = model.getActivePlayer();
 			Color playerColor;
 			if(activePlayer == Player.PLAYER1) {
 				playerColor = player1color;
 			}
 			else {
 				playerColor = player2color;
 			} //REVIVE: put in all 4 Players
 			
 			for(int h=0; h<5; h++) {
 				
 				if(h<heightBuffer) {
 					
 					for(int i=0; i<5; i++) {
 						
 						JPanel brick = new JPanel();
 						brick.setBackground(Color.white);
 						this.add(brick);
 					}
 				}
 				else {
 					
 					for(int w=0; w<5; w++) {
 						
 						if(w<widthBuffer) {
 							
 							JPanel brick = new JPanel();
 							brick.setBackground(Color.white);
 							this.add(brick);
 						}
 						else {
 							
 							JPanel brick = new JPanel();
 							if(piece.isOccupied(w-widthBuffer, h-heightBuffer)) {
 								brick.setBackground(playerColor);
 								brick.setBorder(BorderFactory.createLineBorder(Color.black));
 							}
 							else {
 								brick.setBackground(Color.white);
 							}
 							this.add(brick);
 						}
 					}
 				}
 			}
 		}
 		
 		public int calculateBuffer(int pieceDimension) {
 			
 			int buffer = 0;
 			
 			switch(pieceDimension) {
 			case 1:
 				buffer = 2;
 				break;
 			case 2:
 				buffer = 1;
 				break;
 			case 3:
 				buffer = 1;
 				break;
 			case 4:
 				buffer = 0;
 				break;
 			case 5:
 				buffer = 0;
 				break;
 			}
 			
 			return buffer;
 		}
 		public BrickusPiece selected(){
 			
 			//highlight myself
 			this.setBackground(Color.yellow);
 			return mypiece;
 		}
/*		public void paintComponent(Graphics g) {
			;
		}*/
 	}
 	class MyBrickusTracker extends JPanel {
 		
 		JLabel errorText;
 		JLabel player1Score;
 		JLabel player2Score;
 		JLabel player3Score;
 		JLabel player4Score;
 		
 		public MyBrickusTracker(BrickusModel model) {
 			
 			this.setLayout(new GridBagLayout());
 			this.setBorder(BorderFactory.createLineBorder(Color.black));
 			GridBagConstraints constraints = new GridBagConstraints();
 			constraints.fill = GridBagConstraints.HORIZONTAL;
 			
 			JPanel subpanelLeft = new JPanel(new BorderLayout());
 			JPanel subpanelRight = new JPanel(new FlowLayout(FlowLayout.TRAILING));
 			
 			errorText = new JLabel();
 			/*errorText.setText(errorText.getText() + "<font color=0000CC>F</font>");
 			errorText.setText(errorText.getText() + "<font color=CC0000>o</font>");
 			errorText.setText(errorText.getText() + "<font color=006600>u</font>");
 			errorText.setText(errorText.getText() + "<font color=FF0066>r</font");
 			errorText.setText(errorText.getText() + "<font color=000000>Cats! =^.^=");*/
 			
 			player1Score = new JLabel("<html><font color = 0000CC>Score: 0 </font>");
 			player2Score = new JLabel("<html><font color = CC0000>Score: 0 </font>");
 			//player3Score = new JLabel("<html><font color = 006600>Score: 0 </font>");
 			//player4Score = new JLabel("<html><font color = FF0066>Score: 0 </font>");
 			
 			subpanelLeft.add(errorText);
 			subpanelRight.add(player1Score);
 			subpanelRight.add(player2Score);
 			//subpanelRight.add(player3Score);
 			//subpanelRight.add(player4Score);
 			
 			constraints.gridx = 0;
 			constraints.gridy = 0;
 			constraints.gridwidth = 5;
 			constraints.weightx = 1;
 			this.add(subpanelLeft, constraints);
 			
 			constraints.gridx = 5;
 			constraints.gridy = 0;
 			constraints.gridwidth = 2;
 			constraints.weightx = 0;
 			this.add(subpanelRight, constraints);
 		}
 		
 		public void newErrorMessage(String message) {
 			
 			errorText.setText(message);
 		}
 		
 		public void newScores(int score1, int score2) {
 			
 			player1Score.setText("<html><font color = 0000CC>Score: " + score1 + "</font>");
 			player2Score.setText("<html><font color = CC0000>Score: " + score2 + "</font>");
 		}
 	}
 }
 class MyBrickusListener implements BrickusListener {
 
 	public Composite composite;
 	
 	public MyBrickusListener(Composite composite) {
 		
 		this.composite = composite;
 	}
 	
 	public void illegalMove(BrickusIllegalMoveEvent event) {
 	
 		composite.updateErrorMessage(event.getMessage());
 	}
 
 	public void modelChanged(BrickusEvent event) {
 		
 		if(!event.isGameOver()) {
 			composite.updateScores(composite.model.calculateScore(Player.PLAYER1), composite.model.calculateScore(Player.PLAYER2));
 			composite.updateSinglePiece(composite.model);
 		}
 		else {
 			composite.tray.passButton.removeActionListener(composite.tray.buttonListener);
 			if(composite.model.calculateScore(Player.PLAYER1) > composite.model.calculateScore(Player.PLAYER2)) {
 				composite.updateErrorMessage("Game over. Player 1 won with " + composite.model.calculateScore(Player.PLAYER1) + " points.");
 			}
 			else if(composite.model.calculateScore(Player.PLAYER1) < composite.model.calculateScore(Player.PLAYER2)) {
 				composite.updateErrorMessage("Game over. Player 2 won with " + composite.model.calculateScore(Player.PLAYER2) + " points.");
 			}
 			else {
 				composite.updateErrorMessage("Game over. Players tied.");
 			}
 		}
 	}
 }
 
 class MyButtonListener implements java.awt.event.ActionListener {
 
 	public BrickusModel model;
 	
 	public MyButtonListener(BrickusModel model) {
 		
 		this.model = model;
 	}
 	
 	public void actionPerformed(ActionEvent event) {
 	
 		model.pass(model.getActivePlayer());
 	}
 }
