 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright Ⓒ 2012  Mattias Andrée, Peyman Eshtiagh,
  *                   Calle Lejdbrandt, Magnus Lundberg
  *
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.gui;
 
 import javax.swing.*;
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import static java.awt.RenderingHints.*;
 
 
 /**
 * The game playing area panel
  *
  * @author  Mattias Andrée, <a href="maandree@kth.se">maandree@kth.se</a>
  */
 public class GamePanel extends JPanel
 {
     /**
      * Constructor
      */
     public GamePanel()
     {
 	this.setBackground(new Color(16, 16, 100));
 	
 	this.matrix = new Color[this.height][this.width];
 	
 	
 	BufferedImage pimg = null;
 	try
 	{
 	    pimg = ImageIO.read(new File("piece.png"));
 	}
 	catch (final IOException err)
 	{
 	    //WARNING: Can't load piece image!
 	    //will be printed soon
 	}
 	this.pieceImage = pimg;
 	
 	if (this.pieceImage == null)
 	{
 	    System.err.println("WARNING: Can't load piece image!");
 	    this.pieceImageW = this.pieceImageH = 1; //initialising
 	}
 	else
 	{
 	    this.pieceImageW = (int)(this.pieceImage.getWidth());
 	    this.pieceImageH = (int)(this.pieceImage.getHeight());
 	}
 	
 	
 	this.matrix[19][0] = new Color(200, 20, 20);
 	this.matrix[19][1] = new Color(160, 160, 20);
 	this.matrix[19][2] = new Color(20, 175, 20);
 	this.matrix[19][3] = new Color(20, 175, 175);
 	this.matrix[19][4] = new Color(20, 20, 200);
 	this.matrix[19][5] = new Color(160, 20, 160);
     }
     
     
     
     /**
      * The number of columns in the game
      */
     private final int width = 10;
     
     /**
      * The number of rows in the game
      */
     private final int height = 20;
     
     /**
      * Piece matrix
      */
     private final Color[][] matrix;
     
     /**
      * The image printed on top of piece to make them look better
      */
     private final BufferedImage pieceImage;
     
     /**
      * The width of {@link #pieceImage}
      */
     private final int pieceImageW;
     
     /**
      * The height of {@link #pieceImage}
      */
     private final int pieceImageH;
     
     
     
     /**
      * {@inheritDoc}
      */
     public void paint(final Graphics g)
     {
 	super.paint(g);
 	
 	final Dimension screenDim = this.getSize();
 	final int screenW = (int)(screenDim.getWidth());
 	final int screenH = (int)(screenDim.getHeight());
 	
 	final BufferedImage offimg = new BufferedImage(screenW, screenH, BufferedImage.TYPE_INT_ARGB);
 	
 	final Graphics2D gg = offimg.createGraphics();
 	gg.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
 	gg.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
 	gg.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
 	gg.setRenderingHint(KEY_DITHERING, VALUE_DITHER_ENABLE); //if needed
 	gg.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_OFF);
 	gg.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC); //no implemention for sinc available
 	gg.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
 	gg.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
 	
 	int pieceW = screenW / this.width;
 	int pieceH = screenH / this.height;
 	
 	Color colour;
 	for (int y = 0; y < this.height; y++)
 	    for (int x = 0; x < this.width; x++)
 		if ((colour = this.matrix[y][x]) != null)
 		{
 		    final int px, py;
 		    
 		    gg.setColor(colour);
 		    gg.fillRect(px = x * pieceW, py = y * pieceH, pieceW, pieceH);
 		    
 		    if (this.pieceImage != null)
 			gg.drawImage(this.pieceImage, px, py, px + pieceW, py + pieceH,
 				     0, 0, this.pieceImageW, this.pieceImageH, null);
 		}
 	
 	g.drawImage(offimg, 0, 0, null);
     }
     
     
     /**
      * Updates the game matrix and redraws the area
      * 
      * @param  erase   A matrix where <code>true</code> indicates removal of block
      * @param  blocks  A matrix where non-<code>null</code> indicates to add a block
      * @param  offY    Top offset, where the first row in the matrices affect the game matrix
      * @param  offX    Left offset, where the first column in the matrices affect the game matrix
      */
     public void update(final boolean[][] erase, final Color[][] blocks, final int offY, final int offX)
     {
 	for (int y = 0, h = erase.length; y < h; y++)
 	{
 	    final int Y = y + offY;
 	    for (int x = 0, w = erase[y].length; x < w; x++)
 		if (erase[y][x])
 		    this.matrix[Y][x + offX] = null;
 	}
 	
 	for (int y = 0, h = blocks.length; y < h; y++)
 	{
 	    final int Y = y + offY;
 	    for (int x = 0, w = blocks[y].length; x < w; x++)
 		if (blocks[y][x] != null)
 		    this.matrix[Y][x + offX] = blocks[y][x];
 	}
 	
 	this.repaint();
     }
     
 }
 
