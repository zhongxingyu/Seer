 package team01;
 
import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
import java.util.ArrayList;
 
 public class GUI extends JPanel{
 	//private static final long serialVersionUID = 7526472295622776147L;  // unique id
 	/**
 	 * @param args
 	 */
 	//static int moves = 0; //Keep track of number of game moves
 	
 	
 	private JButton newGameButton;  // Button to start a new game
 	private JButton resignButton;   // Button that a player can use to end game
 	                                  
 	   
     private JLabel message;  // Label for displaying messages to the user
 	
 	public static void main(String[] args) {
 		JFrame window = new JFrame("Fanorona");
 		GUI content = new GUI();
 	    window.setContentPane(content);
 	    window.pack();
 	    Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
 	    window.setLocation( (screensize.width - window.getWidth())/2,
 	            (screensize.height - window.getHeight())/2 );
 	    window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 	    window.setResizable(false);  
 	    window.setVisible(true);	
 	}
 	
 	public GUI() {
 		setLayout(null);
 		setPreferredSize(new Dimension(750, 500)); //width, height
 		setBackground(new Color(105,139,34));  // Olive background
 	    
 		//Create Components
 	      GUIBoard guiBoard = new GUIBoard(); //Creates buttons and labels etc.
 	      add(guiBoard);
 	      add(newGameButton);
 	      add(resignButton);
 	      add(message);
 	      //Set positions and sizes of components
 	      guiBoard.setBounds(55,55,640,340); //x, y, width, height
 	      newGameButton.setBounds(200, 450, 120, 30);
 	      resignButton.setBounds(500, 450, 120, 30);
 	      message.setBounds(0, 400, 600, 30);
 	}
 	
 	private class GUIBoard extends JPanel implements ActionListener, MouseListener {//, UserInput {
 		private static final long serialVersionUID = 7;  // unique id
 		Board board;
 		boolean gameInProgress;
 		int currentPlayer;
 		Point selectedPiece;
 		//Pair[] legalMoves; //An array containing pairs of legal moves
 		int col_space; //Space between columns on the board
 	    int row_space; //Space between rows on the board
 		
 		GUIBoard() {
 			setBackground(Color.BLACK);
 			addMouseListener(this);
 	        resignButton = new JButton("Resign");
 	        resignButton.addActionListener(this);
 	        newGameButton = new JButton("New Game");
 	        newGameButton.addActionListener(this);
 	        message = new JLabel("",JLabel.CENTER);
 	        message.setFont(new  Font("Serif", Font.BOLD, 14));
 	        message.setForeground(Color.white);
 //	        board = new Board(9, 5);
 	        Game fanorona = new Game();
 	        fanorona.play();
 	        board = fanorona.getBoard();
 	        doNewGame();
 		}
 		
 		public void actionPerformed(ActionEvent evt) {
 	         Object src = evt.getSource();
 	         if (src == newGameButton)
 	            doNewGame();
 	         else if (src == resignButton)
 	            doResign();
 	      }
 		
 		 void doNewGame() {
			 currentPlayer = Board.WHITE;
 	         selectedPiece = new Point(-1,-1);   // Set selected piece to none i.e. -1, -1
 	         //message.setText("WHITE:  Make your move.");
 	         gameInProgress = true;
 	         newGameButton.setEnabled(false);
 	         resignButton.setEnabled(true);
 	       /*
 	         Game fanorona = new Game();
 	         fanorona.play();
 	         board = fanorona.getBoard();*/
 	     //    repaint(); //Is giving weird duplication 
 	         
 	       
 	      }
 		 
 		 void doResign() {
 	         if (currentPlayer == Board.WHITE)
 	            gameOver("WHITE resigns.  BLACK wins.");
 	         else
 	            gameOver("BLACK resigns.  WHITE wins.");
 	      }
 		 
 		 void gameOver(String str) {
 	         message.setText(str);
 	         newGameButton.setEnabled(true);
 	         resignButton.setEnabled(false);
 	         gameInProgress = false;
 	      }
 		 
 		
 	//This doesn't seem to work
	//public Point getPoint() {
		//return selectedPiece;
     //}  // end doClickSquare()
 	
 	//This works
 	 public void paintComponent(Graphics g) {
 		 //Draw border
 		 g.setColor(Color.white);
          g.drawRect(0,0,getSize().width-1,getSize().height-1);
          g.drawRect(1,1,getSize().width-3,getSize().height-3);
          g.setColor(Color.LIGHT_GRAY);
          g.drawLine(40, 40, 40, 300); //First col
          g.drawLine(40, 40, 600, 40); //First row
          col_space = (640 - 80)/(board.getWidth()-1);
          row_space = (340 - 80)/(board.getHeight()-1);
          //Draw the 4 partial diagonal lines
          g.drawLine(40,340/2,col_space*2+40, 300);
          g.drawLine((board.getWidth()-3)*col_space+40, 40, 600, 340/2);
          g.drawLine(40,340/2,col_space*2+40, 40);
          g.drawLine(600, 340/2, (board.getWidth()-3)*col_space+40, 300);
          //Draw empty board
          for (int row = 0; row < board.getHeight(); row++) {
              for (int col = 0; col < board.getWidth(); col++) {
             	 g.setColor(Color.LIGHT_GRAY);
             	 if (row != 0 && col != 0) {
             		 g.drawLine(col*col_space+40, 40, col*col_space+40, 300); //Draw rest of cols
             		 g.drawLine(40, row*row_space+40, 600, row*row_space+40); //Draw rest of rows
             	 }
             	if (col % 2 == 0 && col != board.getWidth()-1 && col != board.getWidth()-3) {
         			g.drawLine(40+col_space*col, 40, (640/2) + col_space*col, 300); //Draw full diagonal lines
         			g.drawLine(40+col_space*col, 300, (640/2) + col_space*col, 40);
             	}
              }
          }
         for (int row = 1; row <= board.getHeight(); row++) {
             for (int col = 1; col <= board.getWidth(); col++) {
                 Point p = new Point(col, row);
                 switch (board.getPoint(p)) {
                 case Board.WHITE:
                    g.setColor(Color.WHITE);
                   if(col == 1 && row == 1) {
                 	   g.fillOval(25, 25, 35, 35);
                    }
                    else {
                	   g.fillOval(25+(col-1)*col_space, 25+(row-1)*row_space, 35, 35);
                 	   
                    }
                    break;
                    
                 case Board.BLACK:
                    g.setColor(Color.BLACK);
                   if(col == 1 && row == 1) {
                 	   g.fillOval(25, 25, 35, 35);
                    }
                    else {
                	   g.fillOval(25+(col-1)*col_space, 25+(row-1)*row_space, 35, 35);
                    }
                    break;
                 }
                 
                 
              }
          }
          
 	 } //end paintComponent
 	
 	 //This works
 	 public void mousePressed(MouseEvent evt) {
          if (gameInProgress == false)
             message.setText("Click the new game button to start a new game.");
          else {
         	int x=-1, y=-1;
             int orig_x = evt.getX()-40;
             for(int i = 0; i < board.getWidth(); i++) {
 	            if(orig_x >= i*col_space  && orig_x <= (i+1)*col_space ) {
 	            	x = i;
 	            }
             }
             int orig_y = evt.getY()-40;
             for(int i = 0; i < board.getHeight(); i++) {
             	if(orig_y >= i*row_space  && orig_y <= (i+1)*row_space ) {
 	            	y = i;
 	            }
             }
            // System.out.print("OX: "+orig_x+"OY: "+orig_y +"\n");
            // System.out.print("X: "+x+"Y: "+y +"\n");
             Point p = new Point(x, y);
             selectedPiece = p;
          }
       }
 	 public void mouseReleased(MouseEvent evt) { }
      public void mouseClicked(MouseEvent evt) {mousePressed(evt);}
      public void mouseEntered(MouseEvent evt) { }
      public void mouseExited(MouseEvent evt) { }
 	
 	} //End GUIBoard Class
 }
