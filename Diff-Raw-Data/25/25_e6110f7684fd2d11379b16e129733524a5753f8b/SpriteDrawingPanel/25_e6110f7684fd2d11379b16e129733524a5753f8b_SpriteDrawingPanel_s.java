 package spritemaker;
 
 import java.awt.Color;
 import javax.swing.*;
 import java.math.*;
 import java.awt.event.*;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 
 //this class is used to draw sprites with the mouse.
 public class SpriteDrawingPanel extends JPanel implements MouseMotionListener, MouseListener, KeyListener
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1199824844784104333L;
 	/**
 	 * 
 	 */
 	private boolean[][] pixels;
 	private boolean[][] selectedPixels;
 	private int squareWidth;
 	private int controlState;			//controls the state machine that decides whether we draw, erase, etc...
 		public final static int NONE = 0;
 		public final static int PAINTNORMAL = 1;
 		public final static int ERASENORMAL = 2;
 		public final static int PAINTERASE = 3;
 		public final static int SELECTBLOCK = 4;
 	
 	private int spriteWidth;
 	private int spriteHeight;
 	private ByteOrientationOption byteOrientation;
 	private ByteMSBPositionOption MSBPos;
 	private String prefix;
 	private String postfix;
 	
 	public SpriteDrawingPanel()
 	{
 		this.grabFocus();
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
 		this.addKeyListener(this);
 		
 		//initialize orientation
 		byteOrientation = ByteOrientationOption.HORIZONTAL;
 		MSBPos = ByteMSBPositionOption.MSBATFRONT;
 		
 		//intialize starting and ending points of selected block
 		selectStartCorner = new Point(0, 0);
 		selectEndCorner = new Point(0, 0);
 		
 		//initialize state
 		controlState = SpriteDrawingPanel.SELECTBLOCK;
 	}
 	
 	public SpriteDrawingPanel(int squareWidth)
 	{
 		this.spriteWidth = 8;
 		this.spriteHeight = 8;
 		
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
 		this.addKeyListener(this);
 		
 		//initialize orientation
 		byteOrientation = ByteOrientationOption.HORIZONTAL;
 		MSBPos = ByteMSBPositionOption.MSBATFRONT;
 		
 		//intialize starting and ending points of selected block
 		selectStartCorner = new Point(0, 0);
 		selectEndCorner = new Point(0, 0);
 		
 		//init drawing state
 		controlState = SpriteDrawingPanel.NONE;
 	}	
 	
 	//changes the size of the sprite.
 	public void setCanvasSize(int[] widthHeight) throws BadCanvasSizeError
 	{
 		//check to make sure the size is ok
 		//if they try to submit an array with more than 2 ints, nope.
 		if(widthHeight.length != 2)
 		{
 			throw new BadCanvasSizeError("ya dun goofed.");
 		}
 		
 		//if either width or height is zero, its a no-go
 		if((widthHeight[0] == 0) || (widthHeight[1] == 0))
 		{
 			throw new BadCanvasSizeError("ya dun goofed.");
 		}
 		
 		//if the width in the "byte direction" is not a multiple of 8, nope.
 		switch(byteOrientation)
 		{
 			case VERTICAL:
 			{
 				if(widthHeight[1]%8 != 0)
 				{
 					throw new BadCanvasSizeError("you suck.");
 				}
 				break;
 			}
 			case HORIZONTAL:
 			{
 				if(widthHeight[0]%8 != 0)
 				{
 					throw new BadCanvasSizeError("you suck.");
 				}
 			}
 		}
 		
 		//canvas size is ok.  We can continue.
 		this.spriteWidth = widthHeight[0];
 		this.spriteHeight = widthHeight[1];
 		
 		//make a new array of bytes to start drawing on.
 		boolean[][] newCanvas = new boolean[spriteHeight][spriteWidth];
 		
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
 	
 	public void setByteOrientation(ByteOrientationOption boo)
 	{
 		this.byteOrientation = boo;
 		
 		//adjust size if necessary.
 		try
 		{
 			switch(byteOrientation)
 			{
 				case VERTICAL:
 				{
 					this.setCanvasSize(new int[]{this.spriteWidth, (((this.spriteHeight+7)/8)*8)});
 				}
 				case HORIZONTAL:
 				{
 					this.setCanvasSize(new int[]{(((this.spriteWidth+7)/8)*8), this.spriteHeight});
 				}
 			}
 		}
 		catch(BadCanvasSizeError oops)
 		{
 			JOptionPane.showMessageDialog(this, "The application encountered a canvas sizing error and must close.");
 			System.exit(1);
 		}
 	}
 	
 	public void setMode(int newDrawingMode)
 	{
 		//depending on what mode we're changing FROM, we may need to clean
 		//up a bit.  As far as I know, the only mode that this makes a difference
 		//with is with SELECTBLOCK where we need to "set down" the block of pixels
 		switch(controlState)
 		{
 			case SpriteDrawingPanel.PAINTNORMAL:
 			{
 				break;
 			}
 			
 			case SpriteDrawingPanel.ERASENORMAL:
 			{
 				break;
 			}
 			
 			case SpriteDrawingPanel.SELECTBLOCK:
 			{
 				//if a block was selected, copy the contents to the pixels array
 				if(selectedPixels != null)
 				{
 					//dump the pixels where they are.
 					for(int i = 0;i <= selectEndCorner.y-selectStartCorner.y;i++)
 					{
 						for(int j = 0;j <= selectEndCorner.x-selectStartCorner.x;j++)
 						{
 							this.pixels[selectStartCorner.y+i][selectStartCorner.x+j] = this.selectedPixels[i][j];
 						}
 					}
 					this.selectedPixels = null;
 				}
 				selectStartCorner.move(0, 0);
 				selectEndCorner.move(0, 0);
 			}
 		}
 		
 		controlState = newDrawingMode;
 	}
 	
 	public void setMSBPosition(ByteMSBPositionOption mpo)
 	{
 		this.MSBPos = mpo;
 	}
 	
 	public ByteOrientationOption getByteOrientation()
 	{
 		return this.byteOrientation;
 	}
 	
 	public ByteMSBPositionOption getMSBPosition()
 	{
 		return this.MSBPos;
 	}
 	
 	public int[] getCanvasSize()
 	{
 		return new int[] {spriteWidth, spriteHeight};
 	}
 	
 	public void setPrefix(String pf)
 	{
 		this.prefix = pf;
 	}
 	
 	public void setPostfix(String pf)
 	{
 		this.postfix = pf;
 	}
 	
 	public String[] getOutputFormatting()
 	{
 		return new String[] {prefix, postfix};
 	}
 	
 	//result < 0 if a < b
 	//result == 0 if a == b
 	//result > 0 if a > b
 	private int compareColors(Color a, Color b)
 	{
 		int aRGB = a.getRGB();
 		int bRGB = b.getRGB();
 		
		int result = 0;
 		int sign = (int)Math.signum((aRGB&0xff0000)-(bRGB&0xff0000));
 		result += sign*(int)Math.pow(((aRGB&0xff0000)-(bRGB&0xff0000)), 2);
 		sign = (int)Math.signum((aRGB&0x00ff00)-(bRGB&0x00ff00));
 		result += sign*(int)Math.pow(((aRGB&0x00ff00)-(bRGB&0x00ff00)), 2);
 		sign = (int)Math.signum((aRGB&0x0000ff)-(bRGB&0x0000ff));
		result += sign*(int)Math.pow(((aRGB&0x0000ff)-(bRGB&0x0000ff)), 2);
 		
		return result;
 	}
 	
 	public void importImage(BufferedImage theImage, Color RGBThreshold, boolean invert)
 	{
 		//resize the canvas
 		//first, make sure the image isn't larger than an arbitrary size
 		if((theImage.getWidth() > 64) || (theImage.getHeight() > 64))
 		{
 			JOptionPane.showMessageDialog(this, "Image too large.");
 			return;
 		}
 		
 		//actually resize the canvas
 		try
 		{
 			if(this.byteOrientation == ByteOrientationOption.VERTICAL)
 			{
 				this.setCanvasSize(new int[]{theImage.getWidth(), ((theImage.getHeight()+8)/8)*8});
 			}
 			else
 			{
 				this.setCanvasSize(new int[]{((theImage.getWidth()+8)/8)*8, theImage.getHeight()});
 			}
 		}
 		catch(BadCanvasSizeError oops)
 		{
 			JOptionPane.showMessageDialog(this, "The application encountered a canvas sizing error and must close.");
 			System.exit(1);
 		}
 		
 		//shade pixels
 		boolean doShade;
 		for(int i = 0;i < theImage.getHeight();i++)
 		{
 			for(int j = 0;j < theImage.getWidth();j++)
 			{
				doShade = this.compareColors(new Color(theImage.getRGB(j, i)), RGBThreshold) > 0;
 				pixels[i][j] = doShade;
 			}
 		}
 	}
 	
 	public String generateHexString()
 	{
 		//interpret boolean array as a hex string.
 		String result = "";
 		int currentNumber = 0;
 		int[] XYVals = new int[]{0, 0};		//Indicates current coordinates in the order
 		int fastIndex;						//value "pointed" by this is incremented each iteration of inner loop
 		int slowIndex;						//value "pointed" by this is incremented each iteration of outer loop
 		int fastBoundary;
 		int slowBoundary;
 		int slowIncrement = 1;					//if slowIndex moves in the same direction as InnerDirection, we need to increment slowIndex by 8.
 		int accumulateOr = 0x00;				//when we translate an 8-bit row column of the pixel array into an integer, we may want to add the pixel at the MSB or the LSB, depending on MSBPos.  In order to do this, we or the "accumulator" with this variable, which is set depending on MSBPos.
 		switch(MSBPos)
 		{
 			case MSBATFRONT:
 			{
 				accumulateOr = 0x01;
 				break;
 			}
 			case MSBATBACK:
 			{
 				accumulateOr = 0x80;
 				break;
 			}
 		}
 		
 		Integer InnerDirection = 0;		//If this points to currentX, we are in HORIZONTAL orientation.  If to y, in VERTICAL orientation.
 		
 		//set up Integer vars to point to ints based on Orientation
 		switch(byteOrientation)
 		{
 			case HORIZONTAL:
 			{
 				InnerDirection = 0;		//0 <-> x
 				break;
 			}
 			
 			case VERTICAL:
 			{
 				InnerDirection =  1;	//1 <-> y
 				break;
 			}
  		}
 		
 		//fast and slow index are "constant" for now
 		fastIndex = 0;			//0 <-> x
 		slowIndex = 1;			//1 <-> y
 		
 		//precondition: array is NOT jagged
 		fastBoundary = pixels[0].length;
 		slowBoundary = pixels.length;
 		
 		/*System.out.println("fastBoundary = " + fastBoundary);
 		System.out.println("slowBoundary = " + slowBoundary);*/
 		
 		if(slowIndex == InnerDirection)
 		{
 			slowIncrement = 8;
 		}
 		
 		for(XYVals[slowIndex] = 0;XYVals[slowIndex] < slowBoundary;XYVals[slowIndex] += slowIncrement)
 		{
 			for(XYVals[fastIndex] = 0;XYVals[fastIndex] < fastBoundary;XYVals[fastIndex]++)
 			{
 				currentNumber = 0;
 				for(int k = 0;k < 8;k++)
 				{
 					//accumulate number
 					switch(MSBPos)
 					{
 						case MSBATFRONT:
 						{
 							currentNumber <<= 1;
 							break;
 						}
 						case MSBATBACK:
 						{
 							currentNumber >>= 1;
 							break;
 						}
 					}
 					if(pixels[XYVals[1]][XYVals[0]])
 					{
 						currentNumber |= accumulateOr;
 					}
 					XYVals[InnerDirection]++;
 				}
 				//add currentNumber to the string
 				result += prefix + Integer.toHexString(0x100 | currentNumber).substring(1) + postfix;
 				
 				if(slowIndex == InnerDirection)
 				{
 					XYVals[slowIndex] -= 8;		//if fastIndex != InnerDirection, this loop must have incremented in the direciton of the slowIndex, which is bad.  Undo it!
 				}
 				else
 				{
 					XYVals[fastIndex]--;
 				}
 			}
 		}
 		
 		//remove last postfix
 		result = result.substring(0, result.length()-postfix.length());
 		
 		return result;
 	}
 	
 	//Given a certain pixel, returns the pixel in the canvas containing that pixel.
 	//Used to tell what pixel a mouse click is in
 	private Point realPixelToVirtualPixel(Point p)
 	{
 		Point result = new Point(p.x/squareWidth, p.y/squareWidth);
 		if((result.x >= spriteWidth) || (result.y >= spriteHeight) || (result.x < 0) || (result.y < 0))
 		{
 			result.x = -1;
 			result.y = -1;
 		}
 		return result;
 	}
 	
 	//returns the top left corner of the given pixel on the canvas.
 	private Point virtualPixelToRealPixel(Point p)
 	{
 		if((p.x < 0) || (p.x >= pixels[0].length) || (p.y < 0) || (p.y >= pixels.length))
 		{
 			return new Point(-1, -1);
 		}
 		
 		Point result = new Point(p.x*squareWidth, p.y*squareWidth);
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
 		
 		//if pixels are selected, draw them with a box around them.
 		if((controlState == SpriteDrawingPanel.SELECTBLOCK) && !selectStartCorner.equals(selectEndCorner))
 		{
 			//draw a green box around the pixels.
 			drawing.setColor(new Color(0xff0000));
 			Point realTLC = this.virtualPixelToRealPixel(selectStartCorner);		//top left pixel
 			Point realLRC = this.virtualPixelToRealPixel(selectEndCorner);			//lower right pixel
 			
 			//first, put the selectStartCorner and selectEndCorner in order
 			//such that selectStartCorner.x <= selectEndCorner.x and
 			//selectStartCorner.y <= selectEndCorner.y.
 			if(realTLC.x > realLRC.x)
 			{
 				int temp = realTLC.x;
 				realTLC.x = realLRC.x;
 				realLRC.x = temp;
 			}
 
 			if(realTLC.y > realLRC.y)
 			{
 				int temp = realTLC.y;
 				realTLC.y = realLRC.y;
 				realLRC.y = temp;
 			}
 
 			
 			//in order to include the bottom row of squares, we need to increment
 			//the lower right pixel by one square width
 			realLRC.x += squareWidth;
 			realLRC.y += squareWidth;
 			
 			//draw the rectangle
 			drawing.drawRect(realTLC.x, realTLC.y, realLRC.x-realTLC.x, realLRC.y-realTLC.y);
 			
 			//draw the squares in the section we're moving.
 			//...but first watch out for dem null pointers
 			if(selectedPixels != null)
 			{
 				for(int i = 0;i < selectedPixels.length;i++)
 				{
 					for(int j = 0;j < selectedPixels[0].length;j++)
 					{
 						Color center = null;
 						if(selectedPixels[i][j])
 						{
 							center = new Color(0x000000);
 						}
 						else
 						{
 							center = new Color(0xffffff);
 						}
 						drawing.setColor(center);
 						drawing.fillRect((j+selectStartCorner.x)*squareWidth+1, (i+selectStartCorner.y)*squareWidth+1, squareWidth-1, squareWidth-1);
 					}
 				}
 			}
 		}
 	}
 
 	//private vafdriables for MouseListener section
 	private Point prevSquare;
 	private Point selectStartCorner;
 	private Point selectEndCorner;
 	private boolean initialMarkAction;
 	private boolean selectionGrabbed;
 	
 	public void mouseDragged(MouseEvent e)
 	{
 		//check to see if we are out of bounds
 		Point vPix = this.realPixelToVirtualPixel(e.getPoint());
 		if((vPix.x == -1) || (vPix.y == -1))
 		{
 			//System.out.println("out of bounds index: {" + vPix.x + ", " + vPix.y + "}");
 			return;
 		}
 		
 		//check to see if the mouse is at new coordinates
 		if(!vPix.equals(prevSquare))
 		{
 			switch(controlState)
 			{
 				case SpriteDrawingPanel.PAINTNORMAL:
 				{
 					prevSquare = this.realPixelToVirtualPixel(e.getPoint());
 					pixels[prevSquare.y][prevSquare.x] = true;
 					break;
 				}
 				
 				case SpriteDrawingPanel.ERASENORMAL:
 				{
 					prevSquare = this.realPixelToVirtualPixel(e.getPoint());
 					pixels[prevSquare.y][prevSquare.x] = false;
 					break;
 				}
 				
 				case SpriteDrawingPanel.PAINTERASE:
 				{
 					prevSquare = this.realPixelToVirtualPixel(e.getPoint());
 					pixels[prevSquare.y][prevSquare.x] = initialMarkAction;
 					break;
 				}
 					
 				case SpriteDrawingPanel.SELECTBLOCK:
 				{
 					//if we grabbed a selection, move both the starting and ending
 					//points of the selection in order to move the whole thing.
 					//otherwise, we're selecting a block, so move just the ending point
 					if(selectionGrabbed)
 					{
 						Point dP = new Point(vPix.x-prevSquare.x, vPix.y-prevSquare.y);
 						System.out.println(dP);
 						
 						//check to make sure that the selection will still be in bounds.
 						Point testStart = new Point(selectStartCorner);
 						testStart.translate(dP.x, dP.y);
 						Point testEnd = new Point(selectEndCorner);
 						testEnd.translate(dP.x, dP.y);
 						if((testStart.x >= 0) && (testEnd.x < pixels[0].length) && (testStart.y >= 0) && (testEnd.y < pixels.length))
 						{
 							selectStartCorner = testStart;
 							selectEndCorner = testEnd;
 							prevSquare = vPix;
 						}
 					}
 					else
 					{
 						selectEndCorner = vPix;
 					}
 					break;
 				}
 			}
 		}
 	}
 	
 	public void mousePressed(MouseEvent e)
 	{
 		this.requestFocusInWindow();
 		
 		//check to make sure we are not going out of boudns
 		Point vPix = this.realPixelToVirtualPixel(e.getPoint());
 		if((vPix.x == -1) || (vPix.y == -1))
 		{
 			return;
 		}
 		
 		switch(controlState)
 		{
 			case SpriteDrawingPanel.PAINTNORMAL:
 			{
 				//log the point w pressed at.  If we are clicking the mouse, we must not
 				//have a previous point.
 				prevSquare = vPix;
 				//initialMarkAction = !pixels[prevSquare.y][prevSquare.x];
 				pixels[prevSquare.y][prevSquare.x] = true;
 				break;
 			}
 			
 			case SpriteDrawingPanel.ERASENORMAL:
 			{
 				prevSquare = vPix;
 				pixels[prevSquare.y][prevSquare.x] = false;
 				break;
 			}
 			
 			case SpriteDrawingPanel.PAINTERASE:
 			{
 				prevSquare = vPix;
 				initialMarkAction = !pixels[prevSquare.y][prevSquare.x];
 				pixels[prevSquare.y][prevSquare.x] = initialMarkAction;
 				break;
 			}
 			
 			case SpriteDrawingPanel.SELECTBLOCK:
 			{
 				//if there are no pixels selected, we don't have to check to see
 				//if we are inside the bounds of the selected rectangle
 				if(selectedPixels != null)
 				{
 					//if we are inside the square...
 					if((vPix.x >= selectStartCorner.x) && (vPix.x <= selectEndCorner.x) && (vPix.y >= selectStartCorner.y) && (vPix.y <= selectEndCorner.y))
 					{
 						//pick it up and log the point we grabbed it at.
 						selectionGrabbed = true;
 						prevSquare = vPix;
 						break;
 					}
 					//if we are outside the square...
 					else
 					{
 						//dump the pixels where they are.
 						for(int i = 0;i <= selectEndCorner.y-selectStartCorner.y;i++)
 						{
 							for(int j = 0;j <= selectEndCorner.x-selectStartCorner.x;j++)
 							{
 								this.pixels[selectStartCorner.y+i][selectStartCorner.x+j] = this.selectedPixels[i][j];
 							}
 						}
 						this.selectedPixels = null;
 					}
 				}
 				selectEndCorner = (selectStartCorner = vPix);
 				break;
 			}
 		}
 	}
 	
 	
 	//when the mouse is released, the user wants to move, delete, or do something
 	//to the current selection.  We will need to copy the selection to the temp
 	//copy array.  If the "selected area" is one pixel, we will not select anything.
 	public void mouseReleased(MouseEvent e)
 	{
 		switch(controlState)
 		{
 			case SpriteDrawingPanel.SELECTBLOCK:
 			{
 				if(!selectionGrabbed)
 				{
 					//check to see if we selected just one square.  If so, deselect.
 					if(this.selectStartCorner.equals(this.selectEndCorner))
 					{
 						this.selectedPixels = null;
 						return;
 					}
 
 					//We selected more than one square.  Copy all of the selected
 					//area to the selectedPixels array and clear that part of the
 					//main pixel array.
 
 					//first, put the selectStartCorner and selectEndCorner in order
 					//such that selectStartCorner.x <= selectEndCorner.x and
 					//selectStartCorner.y <= selectEndCorner.y.
 					if(selectStartCorner.x > selectEndCorner.x)
 					{
 						int temp = selectStartCorner.x;
 						selectStartCorner.x = selectEndCorner.x;
 						selectEndCorner.x = temp;
 					}
 
 					if(selectStartCorner.y > selectEndCorner.y)
 					{
 						int temp = selectStartCorner.y;
 						selectStartCorner.y = selectEndCorner.y;
 						selectEndCorner.y = temp;
 					}
 
 					//now, we make a new boolean array large enough to hold the
 					//pixels we are going to move and point selectedPixels at it.
 					int height = selectEndCorner.y-selectStartCorner.y+1;
 					int width = selectEndCorner.x-selectStartCorner.x+1;
 					this.selectedPixels = new boolean[height][width];
 
 					//System.out.println("width of array = " + width + "    height of array = " + height);
 					//copy over the data from the selected area and clear out the selected
 					//area in the main array.
 					for(int i = 0;i <= selectEndCorner.y-selectStartCorner.y;i++)
 					{
 						for(int j = 0;j <= selectEndCorner.x-selectStartCorner.x;j++)
 						{
 							this.selectedPixels[i][j] = this.pixels[selectStartCorner.y+i][selectStartCorner.x+j];
 							this.pixels[selectStartCorner.y+i][selectStartCorner.x+j] = false;
 						}
 					}
 
 					//nothing else to do, I think...
 				}
 				selectionGrabbed = false;
 			}
 		}
 	}
 	
 	//we are only interested in instances where the mouse has been dragged or pressed.
 	public void mouseMoved(MouseEvent e){}
 	public void mouseClicked(MouseEvent e){}
 	public void mouseEntered(MouseEvent e){}
 	public void mouseExited(MouseEvent e){}
 
 	@Override
 	public void keyTyped(KeyEvent e){System.out.println("sadf");}
 
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		System.out.println(e.getKeyCode());
 		switch(e.getKeyCode())
 		{
 			case KeyEvent.VK_DELETE:
 			{
 									System.out.println("foozyuaasjdf");
 				//if we have a piece selected, delete that thang.
 				if(selectedPixels != null)
 				{
 					System.out.println("foozyuaasjdf");
 					selectedPixels = null;
 					selectEndCorner = selectStartCorner;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e){}
 }
