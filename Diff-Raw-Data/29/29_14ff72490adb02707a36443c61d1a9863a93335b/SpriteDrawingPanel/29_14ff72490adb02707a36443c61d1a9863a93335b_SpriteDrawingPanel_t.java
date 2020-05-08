 package spritemaker;
 
 import java.awt.Color;
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 
 //this class is used to draw sprites with the mouse.
 class SpriteDrawingPanel extends JPanel implements MouseMotionListener, MouseListener
 {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1199824844784104333L;
	/**
	 * 
	 */
 	private boolean[][] pixels;
 	private int squareWidth;
 	private int spriteWidth;
 	private int spriteHeight;
 	private String prefix;
 	private String postfix;
 	
 	public SpriteDrawingPanel()
 	{
 		spriteWidth = 8;
 		spriteHeight = 8;
 		
		//initialize array
 		pixels = new boolean[spriteHeight][spriteWidth];
		for(int i = 0;i < pixels.length;i++)
 		{
			for(int j = 0;j < pixels[i].length;j++)
 			{
				pixels[i][j] = false;
 			}
 		}
 
 		prevSquare = new Point();
 		this.addMouseMotionListener(this);
 		this.addMouseListener(this);
 	}
 	
 	//width and height are given in bytes, NOT bits.
 	public SpriteDrawingPanel(int width, int height, int squareWidth)
 	{
 		this.spriteWidth = width*8;
 		this.spriteHeight = height*8;
 		
		//initialize array
 		pixels = new boolean[spriteHeight][spriteWidth];
		for(int i = 0;i < pixels.length;i++)
 		{
			for(int j = 0;j < pixels[i].length;j++)
 			{
				pixels[i][j] = false;
 			}
 		}
 		
 		this.squareWidth = squareWidth;
 		prevSquare = new Point();
 		this.addMouseMotionListener(this);
 		this.addMouseListener(this);
 	}	
 	
 	//changes the size of the sprite.
 	public void setSize(int width, int height)
 	{
 		this.spriteWidth = width*8;
 		this.spriteHeight = height*8;
 		
 		//make a new array of bytes to start drawing on.
 		boolean[][] newCanvas = new boolean[spriteWidth][spriteHeight];
 		
 		//copy the old one over
 		for(int i = 0;(i < newCanvas.length) && (i < this.pixels.length);i++)
 		{
 			for(int j = 0;(j < newCanvas[i].length) && (j < this.pixels[i].length);j++)
 			{
 				newCanvas[i][j] = this.pixels[i][j];
 			}
 		}
 		
 		//reassign
 		this.pixels = newCanvas;
 	}
 	
 	public void setPrefix(String pf)
 	{
 		this.prefix = pf;
 	}
 	
 	public void setPostfix(String pf)
 	{
 		this.postfix = pf;
 	}
 	
 	public String generateHexString()
 	{
 		//interpret boolean array as a hex string.
 		//for now, orientation will be horizontal and little endian
 		String result = "";
 		int currentNumber;
 		int i, j;
 		for(i = 0;i < pixels.length-1;i++)
 		{
 			for(j = 0;j < (spriteWidth/8);j++)
 			{
 				currentNumber = 0;
 				for(int k = 0;k < 8;k++)
 				{
 					currentNumber <<= 1;
 					if(pixels[i][j*8+k])
 					{
 						currentNumber |= 1;
 					}
 				}
 				result += prefix + Integer.toHexString(0x100 | currentNumber).substring(1) + postfix;
 			}
 		}
 		
 		for(j = 0;j < (spriteWidth/8)-1;j++)
 		{
 			currentNumber = 0;
 			for(int k = 0;k < 8;k++)
 			{
 				currentNumber <<= 1;
 				if(pixels[i][j*8+k])
 				{
 					currentNumber |= 1;
 				}
 			}
 			result += prefix + Integer.toHexString(0x100 | currentNumber).substring(1) + postfix;
 		}
 		
 		currentNumber = 0;
 		for(int k = 0;k < 8;k++)
 		{
 			currentNumber <<= 1;
 			if(pixels[i][j*8+k])
 			{
 				currentNumber |= 1;
 			}
 		}
 		result += prefix + Integer.toHexString(0x100 | currentNumber).substring(1);
 		
 		return result;
 	}
 	
 	private Point realPixelToVirtualPixel(Point p)
 	{
 		Point result = new Point(p.x/squareWidth, p.y/squareWidth);
 		if((result.x >= spriteWidth) || (result.y >= spriteHeight))
 		{
 			result.x = -1;
 			result.y = -1;
 		}
 		return result;
 	}
 	
 	@Override
 	public void paint(Graphics g)
 	{
 		super.paint(g);
 		
 		//guard against an empty array.
 		/*if(pixels.length == 0)
 		{
 			return;
 		}*/
 		
 		Graphics2D drawing = (Graphics2D)g;
 		
 		//draw the grid
 		//x axis first
 		//less than or equals for outside border.
 		for(int i = 0;i <= pixels.length;i++)
 		{
 			drawing.drawLine(0, i*squareWidth, spriteWidth*squareWidth, i*squareWidth);
 		}
 		
 		//now y axis
 		for(int i = 0;i <= pixels[0].length;i++)
 		{
 			drawing.drawLine(i*squareWidth, 0, i*squareWidth, spriteHeight*squareWidth);
 		}
 		
 		//fill in shaded squares
 		for(int i = 0;i < pixels.length;i++)
 		{
 			for(int j = 0;j < pixels[i].length;j++)
 			{
 				Color center = null;
 				if(pixels[i][j])
 				{
 					center = new Color(0x000000);
 				}
 				else
 				{
 					center = new Color(0xffffff);
 				}
 				drawing.setColor(center);
 				drawing.fillRect(j*squareWidth+1, i*squareWidth+1, squareWidth-1, squareWidth-1);
 			}
 		}
 	}
 
 	//private variables for MouseListener section
 	private Point prevSquare;
 	private boolean initialMarkAction;
 	
 	public void mouseDragged(MouseEvent e)
 	{
 		//check to see if we are out of bounds
 		Point vPix = this.realPixelToVirtualPixel(e.getPoint());
 		if((vPix.x == -1) || (vPix.y == -1))
 		{
 			return;
 		}
 		
 		//check to see if the mouse is at new coordinates
 		if(!vPix.equals(prevSquare))
 		{
 			prevSquare = this.realPixelToVirtualPixel(e.getPoint());
 			pixels[prevSquare.y][prevSquare.x] = initialMarkAction;
 		}
 	}
 	
 	public void mousePressed(MouseEvent e)
 	{
 		//check to make sure we are not going out of boudns
 		Point vPix = this.realPixelToVirtualPixel(e.getPoint());
 		if((vPix.x == -1) || (vPix.y == -1))
 		{
 			return;
 		}
 		
 		//log the point we pressed at.  If we are clicking the mouse, we must not
 		//have a previous point.
 		prevSquare = vPix;
 		initialMarkAction = !pixels[prevSquare.y][prevSquare.x];
 		pixels[prevSquare.y][prevSquare.x] = initialMarkAction;
 	}
 	
 	//we are only interested in instances where the mouse has been dragged or pressed.
 	public void mouseMoved(MouseEvent e){}
 	public void mouseClicked(MouseEvent e){}
 	public void mouseReleased(MouseEvent e){}
 	public void mouseEntered(MouseEvent e){}
 	public void mouseExited(MouseEvent e){}
 }
