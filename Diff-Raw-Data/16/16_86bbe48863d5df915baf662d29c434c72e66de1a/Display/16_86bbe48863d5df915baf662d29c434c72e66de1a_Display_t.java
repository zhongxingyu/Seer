 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 public class Display extends JPanel implements ActionListener, MouseListener, KeyListener, MouseMotionListener
 { 
 
 	Board playBoard;
	boolean exited;
	
 	public Display()
 	{
 		BufferedImage img = null;
 		try {
 			img = ImageIO.read(getClass().getResource("chessIcons.png"));
 		} catch (IOException e) {
 		}
 
 		playBoard = new Board(this, img);
 		
 		this.setVisible(true);
 
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		this.addKeyListener(this);
 	}
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		this.setBackground(Color.LIGHT_GRAY);
 		playBoard.paintBoard(g);
 	}
 
 
 
 	public void keyTyped(KeyEvent e) 
 	{
 		// TODO Auto-generated method stub
 	}
 
 
 	public void keyPressed(KeyEvent e) 
 	{
 		// TODO Auto-generated method stub
 	}
 
 	public void keyReleased(KeyEvent e) 
 	{
 		// TODO Auto-generated method stub
 	}
 
 	public void mouseClicked(MouseEvent e) 
 	{
 		if(playBoard.justMoved)
 			return;
 
 		int x = (e.getX()+1-playBoard.getBorder())/(playBoard.tileSize+1);
 		int y = (e.getY()-playBoard.getBorder())/(playBoard.tileSize+1);
 
 		if(playBoard.getTurnCount()%2==1)
 		{
 			x = playBoard.reflectNumber(x);
 			y = playBoard.reflectNumber(y);
 		}
 
 		if(!playBoard.isEmpty(x,y)){
 
 			if(playBoard.click && (playBoard.getClickX() == x) && (playBoard.getClickY() == y))
 				playBoard.setClick(false);
 			else
 				playBoard.setClick(true);
 			//playBoard.newbool = true;
 
 			if((playBoard.isWhite(x, y)&&playBoard.getTurnCount()%2==1)
 					|| (!playBoard.isWhite(x, y)&&playBoard.getTurnCount()%2==0))
 			{
 				playBoard.setClick(false);
 			}
 			else
 				playBoard.setClickCoordinates(x,y);
 
 		}
 		repaint();
 	}
 
 
 	public void mousePressed(MouseEvent e) 
 	{
 		playBoard.setJustMoved(false);
 
 		boolean whiteTurn = (playBoard.turnCount%2 == 0);
 
 		int x = (e.getX()+1-playBoard.getBorder())/(playBoard.tileSize+1);
 		int y = (e.getY()-playBoard.getBorder())/(playBoard.tileSize+1);
 
 		if(!whiteTurn){
 			x = playBoard.reflectNumber(x);
 			y = playBoard.reflectNumber(y);
 		}
 
 
 		if( (playBoard.isWhite(x, y)&&!whiteTurn) || (!playBoard.isWhite(x, y)&&whiteTurn))
 			return;
 
 		if(!playBoard.isEmpty(x, y))
 		{
 			playBoard.setX1(x);
 			playBoard.setY1(y);	
 		}
 		
 		//		}
 		//System.out.println(playBoard.getX1() + ", " + playBoard.getY1());
 	}
 
 
 	public void mouseReleased(MouseEvent e) 
 	{
		if(exited)
		{
			playBoard.setDragging(false);
			repaint();
			exited = false;
			return;
		}
		
 		boolean white = playBoard.getTurnCount()%2==1;
 		playBoard.setDragging(false);
 
 		int x = (e.getX()+1-playBoard.getBorder())/(playBoard.tileSize+1);
 		int y = (e.getY()-playBoard.getBorder())/(playBoard.tileSize+1);
 
 		if(playBoard.getTurnCount()%2==1){
 			x = playBoard.reflectNumber(x);
 			y = playBoard.reflectNumber(y);
 		}
 
 		playBoard.setX2(x);
 		playBoard.setY2(y);
 
 		playBoard.movePiece();
 
 		repaint();
 
 		if(playBoard.checkmateOrStaleMate(white)){
 
 			if(playBoard.checkmate)
 				JOptionPane.showMessageDialog(this, "Checkmate! "+ playBoard.getWinner() + " wins!");
 			else
 				JOptionPane.showMessageDialog(this, "Stalemate!");
 
 			//TODO prompt a new game
 			repaint();
 			playBoard.setStopPainting(true);
 		}
 	}
 
 
 	public void mouseEntered(MouseEvent e) 
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 
 	public void mouseExited(MouseEvent e) 
 	{
		exited = true;
 	}
 
 
 	public void mouseDragged(MouseEvent e) 
 	{
 		playBoard.setDragging(true);
 		playBoard.setDraggingCoordinates(e.getX(),e.getY());
 		repaint();
 	}
 
 
 	public void mouseMoved(MouseEvent e) 
 	{
 		//System.out.println(playBoard.newbool);
 
 	}
 
 
 	public void actionPerformed(ActionEvent e) 
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
