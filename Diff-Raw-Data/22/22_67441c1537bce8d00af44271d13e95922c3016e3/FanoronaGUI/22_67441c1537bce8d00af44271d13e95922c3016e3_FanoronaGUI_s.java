 package eighteen;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.*;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import eighteen.Board.BadBoardException;
 import eighteen.Board.BadMoveException;
 import eighteen.Board.GameOverException;
 import eighteen.Piece.adjLoc;
 
 public class FanoronaGUI extends JFrame {
 	//TODO if choice between pieces, pick attacking or retreating
 	//TODO end move button
 	//TODO add to a tool bar or button bar containing buttons for help, 
 			//end move, attack/retreat, game setup options, connect to opponent, sacrifice piece
 	JButton helpb; 
 	Board board; //contains the array of pieces and board management functions
 	DrawnPiece[][] gamePieces;
 	DrawnPiece prevMoveDrawn;
 	Piece prevMove;
 	JLabel blackPieces,whitePieces,movesMade,utilityVal,currentTurn;
 	private String playerTurn = "WHITE";
 	Container game = getContentPane();
 	PieceListener clicked;//given to every piece in a loop
 	
 	Boolean isMoveState2 = false;
     public static FanoronaGUI GUI;
     
     public static void main(String[] args) {
        GUI =  new FanoronaGUI();
     }
     public void changeTurn()
     {
     	if (playerTurn=="WHITE")
     		playerTurn = "BLACK";
     	else
     		playerTurn = "WHITE";
     	
     }
     public void makeInfoPanel()
     {
     	JPanel InfoPanel = new JPanel();
     	GridLayout Lay = new GridLayout();
     	InfoPanel.setLayout(Lay);
     	InfoPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
     	blackPieces = new JLabel("Black: "+board.blacks);
     	whitePieces = new JLabel("White: "+board.whites);
     	movesMade = new JLabel("Move: "+board.moves);
     	utilityVal = new JLabel("Utility: "+board.Utility());
     	currentTurn = new JLabel("Turn: " + playerTurn);
     	InfoPanel.add(blackPieces);
     	InfoPanel.add(whitePieces);
     	InfoPanel.add(movesMade);
     	InfoPanel.add(utilityVal);
     	InfoPanel.add(currentTurn);
     	game.add(InfoPanel,BorderLayout.SOUTH);
     }
     public void updateInfoPanel()
     {
     	blackPieces.setText("Black: "+board.blacks);
     	whitePieces.setText("White: "+board.whites);
     	movesMade.setText("Move: "+board.moves);
     	utilityVal.setText("Utility: "+board.Utility());
     	currentTurn.setText("Turn: " + playerTurn);
     }
     public void makePieces()
     {
     	JPanel jBoard = new JPanel();
     	jBoard.setLayout(new GridLayout(Board.ROWS,Board.COLUMNS,15,15));
         for(int x= 0;x<Board.ROWS;x++)
         {
         	for(int y = 0; y<Board.COLUMNS; y++)
         	{
         		if(board.theBoard[x][y].getColor() == Color.BLACK)
         		{
         			gamePieces[x][y] = new DrawnPiece(x,y,Color.BLACK);
         		}
         		else if(board.theBoard[x][y].getColor() == Color.WHITE)
         		{
         			gamePieces[x][y] = new DrawnPiece(x,y,Color.WHITE);
         		}
         		else
         		{
         			gamePieces[x][y] = new DrawnPiece(x,y,Color.GRAY);
         		}
         		jBoard.add(gamePieces[x][y]);
         	}
         }
         game.add(jBoard,BorderLayout.CENTER);
     }
     public void makeHelpButton()
     {
     	 helpb = new JButton("Help");
          helpb.setBounds(50,60,80,30);
          helpb.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent event) {
                  HelpGUI helpgui = new HelpGUI();
                  helpgui.setVisible(true);
              }
          });
          game.add(helpb,BorderLayout.NORTH);
     }
     public void makeMoveCancelButton()
     {
     	 helpb = new JButton("Cancel Move");
          helpb.setBounds(50,60,80,30);
          helpb.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent event) {
             	 isMoveState2 = false;
             	 resetPrevMove();
              }
          });
          game.add(helpb,BorderLayout.NORTH);
     }
     public void resetPrevMove()
     {
     	//eventually display that the move was reset visually
     	System.out.print("ResetPrevMove\n");
     	if(prevMove != null)
     	{
 	    	for(adjLoc p : prevMove.adjacentLocations)
 			{
 				if (gamePieces[p.row][p.column].pColor == Color.YELLOW)
 				{
 					gamePieces[p.row][p.column].pColor = Color.GRAY;
 					gamePieces[p.row][p.column].repaint();
 				}
 			}
 	    	prevMoveDrawn = null;
 	    	prevMove = null;
 	    	isMoveState2=false;
     	}
     }
     public void makePieceListeners()
     {
     	clicked = new PieceListener(); 
 		for(int x= 0;x<Board.ROWS;x++)
         {
         	for(int y = 0; y<Board.COLUMNS; y++)
         	{
 			gamePieces[x][y].addActionListener(clicked);
         	}
 		}
     }
     public void makeGrid()
     {
     	GameGrid grid = new GameGrid(Board.ROWS, Board.COLUMNS);
     	game.add(grid,BorderLayout.CENTER);
     }
     public FanoronaGUI() {
     	game.setLayout(new BorderLayout());
     	try {
 			board = new Board(5,13);
 		} catch (BadBoardException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}//TODO eventually need user input here
     	gamePieces = new DrawnPiece[Board.ROWS][Board.COLUMNS];
     	prevMoveDrawn = null;
     	
     	makePieces();
     	//makeGrid();
     	//makeHelpButton();
     	makeInfoPanel();
     	makeMoveCancelButton();
     	makePieceListeners();
     	setSize(500,350);
         setTitle("Fanorona");
         
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocationRelativeTo(null);
         setVisible(true);
     }
     class PieceListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
     	{
     		DrawnPiece gamePiece = (DrawnPiece) e.getSource();
     		Piece pieceClicked = board.theBoard[gamePiece.xLoc][gamePiece.yLoc];
     		//first check if the piece clicked is an available piece
     		if (gamePiece.pColor == Color.YELLOW && isMoveState2)//already selected
     		{
     			try
     			{
 	    			Move moveToPlay = null;
 					moveToPlay = new Move(prevMove, new Piece.adjLoc(pieceClicked), AttackState.NIETHER);
 					//now constructed, check move if attacking or withdrawing
 					//prompt user if needed
 					//then set the value externally
 	    			if (board.isValidMove(moveToPlay))
 	    			{
 		    			//make the point clicked the color of the previous point clicked
 		    			gamePieces[pieceClicked.row][pieceClicked.column].pColor = prevMoveDrawn.pColor;
 		    			gamePieces[pieceClicked.row][pieceClicked.column].repaint();
 		    			//make the previous point turn gray
 		    			gamePieces[prevMoveDrawn.xLoc][prevMoveDrawn.yLoc].pColor = Color.GRAY;
 		    			gamePieces[prevMoveDrawn.xLoc][prevMoveDrawn.yLoc].repaint();
 		    			//modify potential moves back to gray
 		    	    	for(adjLoc p : prevMove.adjacentLocations)
 		    			{
 		    				if (gamePieces[p.row][p.column].pColor == Color.YELLOW)
 		    				{
 		    					gamePieces[p.row][p.column].pColor = Color.GRAY;
 		    					gamePieces[p.row][p.column].repaint();
 		    				}
 		    			}
 		    	    	//returns the list of pieces that need to be painted
 		    	    	ArrayList<Piece> piecesToColor = board.move(moveToPlay);
 		    	    	//re-color captured pieces
 		    	    	for(Piece p : piecesToColor)
 		    			{
 		    					gamePieces[p.row][p.column].pColor = Color.GRAY;
 		    					gamePieces[p.row][p.column].repaint();
 		    			}
 		    			
 		    	    	System.out.print("Moved to x:" + gamePiece.yLoc+" y:"+gamePiece.xLoc + " from x:" +prevMove.column +" y:"+prevMove.row +"\n" );
 		    	    	//set up for next turn
 		    	    	prevMoveDrawn = null;
 		    	    	prevMove = null;
 		    			isMoveState2 = false;
 		    			changeTurn();
 		    			//TODO check if game over by win
 		    			//TODO ask for ai/opponent move
 		    			//TODO check if game over by win or by too many moves
 		    			updateInfoPanel();
 	    			}
 				} 
    			catch (BadMoveException | GameOverException e1) 
    			{
 					e1.printStackTrace();
 					//TODO something about the GameOverException
 				}
     		}
     		else if (gamePiece.pColor !=Color.YELLOW && gamePiece.pColor != Color.GRAY && !isMoveState2)
     		{
     			
     			//highlight all available moves, if not making a move, and if the piece clicked isn't gray
     			//TODO THIS NEEDS TO TAKE INTO ACCOUNT OTHER CAPTURES ON THE BOARD EVENTUALLY
     			boolean ColorChanged = false;
 	    		for(adjLoc p : pieceClicked.adjacentLocations)
 	    		{
 	    			if (gamePieces[p.row][p.column].pColor == Color.GRAY)
 	    			{
 	    				ColorChanged = true;
 	    				gamePieces[p.row][p.column].pColor = Color.YELLOW;
 	    				gamePieces[p.row][p.column].repaint();
 	    			}
 	    		}
 	    		if(ColorChanged)
 	    		{
 	    			prevMoveDrawn=gamePiece;
 	    			prevMove=pieceClicked;
 	    			isMoveState2 = true;
 	    		}
 	    		System.out.print("Clicked x:" + gamePiece.xLoc+" y:"+gamePiece.yLoc+"\n");
     		}	
     		//System.out.print("Clicked x:" + gamePiece.xLoc+" y:"+gamePiece.yLoc+"\n");
     		//System.out.print(isMoveState2 +" \n");	
     	}
     }
     
 }
