 package minigames.tiles;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.concurrent.LinkedBlockingQueue;
 import javax.swing.*;
 
 public class PuzzlePanel extends JPanel
 {
 	private ImageIcon[][] puzzle;
 	private ImageIcon one, two, three, four, five, six, seven, eight, nine, ten, 
 		eleven, twelve, thirteen, fourteen, fifteen, sixteen, blank;
 	private Rectangle oneR, twoR, threeR, fourR, fiveR, sixR, sevenR, eightR, nineR, tenR, 
 		elevenR, twelveR, thirteenR, fourteenR, fifteenR, sixteenR;
 	private int day, indexI, indexJ, xPad, yPad;
 	private static int PWIDTH=550, PHEIGHT=450, TILE=100;
 	private boolean finished;
 	
 	public PuzzlePanel(int day)
 	{
 		// LOAD IN ALL IMAGES
 		one = new ImageIcon("art/tilePuzzle/1.png");
 		two = new ImageIcon("art/tilePuzzle/2.png");
 		three = new ImageIcon("art/tilePuzzle/3.png");
 		four = new ImageIcon("art/tilePuzzle/4.png");
 		five = new ImageIcon("art/tilePuzzle/5.png");
 		six = new ImageIcon("art/tilePuzzle/6.png");
 		seven = new ImageIcon("art/tilePuzzle/7.png");
 		eight = new ImageIcon("art/tilePuzzle/8.png");
 		nine = new ImageIcon("art/tilePuzzle/9.png");
 		ten =  new ImageIcon("art/tilePuzzle/10.png");
 		eleven =  new ImageIcon("art/tilePuzzle/11.png");
 		twelve =  new ImageIcon("art/tilePuzzle/12.png");
 		thirteen =  new ImageIcon("art/tilePuzzle/13.png");
 		fourteen =  new ImageIcon("art/tilePuzzle/14.png");
 		fifteen =  new ImageIcon("art/tilePuzzle/15.png");
 		sixteen =  new ImageIcon("art/tilePuzzle/16.png");
 		blank = new ImageIcon("art/tilePuzzle/blank.png");
 	
 		// SET UP BOUNDS BASED ON DIFFICULTY
 		this.day = day;
 		if (day <= 3)
 		{
 			indexI = indexJ = 3;
 			eleven = blank;
 		}
 		else if (day > 3 && day <=6)
 		{	indexI = 4;
 			indexJ = 3;
 			twelve = blank;
 		}
 		else
 		{
 			indexI=indexJ = 4;
 			sixteen = blank;
 		}
 		
 		puzzle = new ImageIcon[indexI][indexJ];
 		xPad = (PWIDTH-(TILE*indexI))/2;
 		yPad = (PHEIGHT-(TILE*indexJ))/2;
 		
 		
 		Point p = new Point(xPad,yPad);
 		oneR = new Rectangle(p.x,p.y,TILE,TILE);
 		twoR = new Rectangle(p.x+TILE,p.y,TILE,TILE);
 		threeR = new Rectangle(p.x+2*TILE,p.y,TILE,TILE);
 		fourR = new Rectangle(p.x+3*TILE, p.y,TILE,TILE);
 		
 		fiveR = new Rectangle(p.x,p.y+TILE,TILE,TILE);
 		sixR = new Rectangle(p.x+TILE,p.y+TILE,TILE,TILE);
 		sevenR = new Rectangle(p.x+2*TILE,p.y+TILE,TILE,TILE);
 		eightR = new Rectangle(p.x+3*TILE,p.y+TILE,TILE,TILE);
 		
 		nineR = new Rectangle(p.x,p.y+2*TILE,TILE,TILE);
 		tenR = new Rectangle(p.x+TILE,p.y+2*TILE,TILE,TILE);
 		elevenR = new Rectangle(p.x+2*TILE,p.y+2*TILE,TILE,TILE);
 		twelveR = new Rectangle(p.x+3*TILE,p.y+2*TILE,TILE,TILE);
 		
 		thirteenR = new Rectangle(p.x,p.y+3*TILE,TILE,TILE);
 		fourteenR = new Rectangle(p.x+TILE,p.y+3*TILE,TILE,TILE); 
 		fifteenR = new Rectangle(p.x+2*TILE,p.y+3*TILE,TILE,TILE);
 		sixteenR = new Rectangle(p.x+3*TILE,p.y+3*TILE,TILE,TILE); 
 
 		newPuzzle();
 		finished = false;
 		
 		addMouseListener(new PuzzleListener());
 		setPreferredSize(new Dimension(PWIDTH,PHEIGHT));
 		setBackground(Color.gray);
 		requestFocus();
 	}
 	
 	private void newPuzzle()
 	{
 		
 		LinkedBlockingQueue<ImageIcon> queue = new LinkedBlockingQueue<ImageIcon>();
 		queue.add(one);
 		queue.add(two);
 		queue.add(three);
 		queue.add(five);
 		queue.add(six);
 		queue.add(seven);
 		queue.add(nine);
 		queue.add(ten);
 		queue.add(eleven);
 
		indexI=3;
		indexJ=3;
 		
 		if (day > 3)
 		{	queue.add(four);		
 			queue.add(eight);
 			queue.add(twelve);
			indexI = 4;
 		}
 		if (day > 6)
 		{
 			queue.add(thirteen);		
 			queue.add(fourteen);
 			queue.add(fifteen);
 			queue.add(sixteen);
			indexJ=4;
 		}
 		
		for (int i=0; i<indexI; i++)
		{	for (int j=0; j<indexJ; j++)
 			{
 				int skip = (int)((Math.random())*10);
 				for (int k=skip; k>0; k--)
 					if (queue.size()>1)
 						queue.add(queue.poll());
 
 				puzzle[i][j] = queue.poll();	
 			}
 		}
 		repaint();
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(0, 0, PWIDTH, PHEIGHT);
 		
		for (int i=0; i<indexI; i++)
			for (int j=0; j<indexJ; j++)
 				puzzle[i][j].paintIcon(this, g, (i*TILE)+xPad, (j*TILE)+yPad);
 		
 		if (finished)
 		{
 			g.setColor(Color.RED);
 			g.drawString("CONGRATULATIONS", 40, PHEIGHT-25);
 			g.drawString("Click anywhere to play again", 25, PHEIGHT-10);
 		}
 	}
 	
 	private boolean isSolved()
 	{
 		if (puzzle[0][0]==one && puzzle[1][0]==two && puzzle[2][0]==three &&
 			puzzle[0][1]==five && puzzle[1][1]==six && puzzle[2][1]==seven &&
 			puzzle[0][2]==nine && puzzle[1][2]==ten && puzzle[2][2]==eleven)
 		{	if (day <= 3)
 				return true;
 			else if ((day>3 && day<=6) && (puzzle[3][0]==four && puzzle[3][1]==eight && puzzle[3][2] == twelve))
 				return true;
 			else if ((day>6) && puzzle[0][3]==thirteen && puzzle[1][3]==fourteen && puzzle[2][3]==fifteen && puzzle[3][3]==sixteen)
 				return true;
 		}
 		return false;
 	}
 	
 	private class PuzzleListener extends MouseAdapter
 	{
 		public void mousePressed(MouseEvent e)
 		{
 			if (!finished)
 			{
 				int x = e.getX();
 				int y = e.getY();
 				
 				boolean tileClicked = true;
 				int xIndex = 0, yIndex = 0;
 				
 				if (oneR.contains(x,y))
 				{	xIndex = 0;
 					yIndex = 0;
 				}
 				else if (twoR.contains(x,y))
 				{	xIndex = 1;
 				 	yIndex = 0;
 				}
 				else if (threeR.contains(x,y))
 				{	xIndex = 2;
 			 		yIndex = 0;
 				}
 				else if (fourR.contains(x,y))
 				{	xIndex = 3;
 		 			yIndex = 0;
 				}
 				else if (fiveR.contains(x,y))
 				{	xIndex = 0;
 	 				yIndex = 1;
 				}
 				else if (sixR.contains(x,y))
 				{	xIndex = 1;
 	 				yIndex = 1;
 				}
 				else if (sevenR.contains(x,y))
 				{	xIndex = 2;
 	 				yIndex = 1;
 				}
 				else if (eightR.contains(x,y))
 				{	xIndex = 3;
 	 				yIndex = 1;
 				}
 				else if (nineR.contains(x,y))
 				{	xIndex = 0;
 	 				yIndex = 2;
 				}
 				else if (tenR.contains(x,y))
 				{	xIndex = 1;
 	 				yIndex = 2;
 				}
 				else if (elevenR.contains(x,y))
 				{	xIndex = 2;
 	 				yIndex = 2;
 				}
 				else if (twelveR.contains(x,y))
 				{	xIndex = 3;
 	 				yIndex = 2;
 				}
 				else if (thirteenR.contains(x,y))
 				{	xIndex = 0;
 	 				yIndex = 3;
 				}
 				else if (fourteenR.contains(x,y))
 				{	xIndex = 1;
 	 				yIndex = 3;
 				}
 				else if (fifteenR.contains(x,y))
 				{	xIndex = 2;
 	 				yIndex = 3;
 				}
 				else if (sixteenR.contains(x,y))
 				{	xIndex = 3;
 	 				yIndex = 3;
 				}
 				else
 					tileClicked = false;
 				
 				if (tileClicked)
 				{				
 					ImageIcon tmp;
 					if (xIndex>0 && puzzle[xIndex-1][yIndex] == blank)
 					{	tmp = puzzle[xIndex-1][yIndex];
 						puzzle[xIndex-1][yIndex] = puzzle[xIndex][yIndex];
 						puzzle[xIndex][yIndex] = tmp;
 					}
 					else if (xIndex<indexI && puzzle[xIndex+1][yIndex] == blank)
 					{	tmp = puzzle[xIndex+1][yIndex];
 						puzzle[xIndex+1][yIndex] = puzzle[xIndex][yIndex];
 						puzzle[xIndex][yIndex] = tmp;
 					}
 					else if (yIndex>0 && puzzle[xIndex][yIndex-1] == blank)
 					{	tmp = puzzle[xIndex][yIndex-1];
 						puzzle[xIndex][yIndex-1] = puzzle[xIndex][yIndex];
 						puzzle[xIndex][yIndex] = tmp;	
 					}
 					else if (yIndex<indexJ && puzzle[xIndex][yIndex+1] == blank)
 					{	tmp = puzzle[xIndex][yIndex+1];
 						puzzle[xIndex][yIndex+1] = puzzle[xIndex][yIndex];
 						puzzle[xIndex][yIndex] = tmp;	
 					}
 					
 					if (isSolved())
 						finished = true;
 					repaint();
 				}	
 			}
 			else // isFinished
 			{
 				newPuzzle();
 				finished = false;
 			}
 		}
 	}
 }
