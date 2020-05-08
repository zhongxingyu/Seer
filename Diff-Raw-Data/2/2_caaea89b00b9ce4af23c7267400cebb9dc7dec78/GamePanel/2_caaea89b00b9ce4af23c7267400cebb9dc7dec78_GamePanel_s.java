 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright © 2012  Calle Lejdbrandt, Mattias Andrée, Peyman Eshtiagh
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.interaction.desktop;
 import cnt.interaction.*;
 import cnt.messages.*;
 import cnt.game.*;
 import cnt.game.Shape; //Explicit
 import cnt.game.enginehelp.*;
 import cnt.*;
 
 import javax.swing.*;
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.util.*;
 import java.io.File;
 import java.io.IOException;
 
 import static java.awt.RenderingHints.*;
 
 
 /**
  * The game playing area panel
  *
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 @SuppressWarnings("serial")
 public class GamePanel extends JPanel implements Blackboard.BlackboardObserver, Runnable
 {
     /**
      * Constructor
      */
     public GamePanel()
     {
 	this.setBackground(Color.BLACK);
 	this.setFocusable(true);
 	this.setRequestFocusEnabled(true);
 	
 	this.matrix = new Color[this.height][this.width];
 	
 	
 	this.addMouseListener(new MouseAdapter()
 	    {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseClicked(final MouseEvent e)
 		{
 		    GamePanel.this.grabFocus();
 		    System.err.println(e);
 		}
 	    });
 	
 	
 	{
 	    BufferedImage img = null;
 	    try
 	    {
 		img = ImageIO.read(new File("piece.png"));
 	    }
 	    catch (final IOException err)
 	    {
 		//WARNING: Can't load piece image!
 		//will be printed soon
 	    }
 	    this.pieceImage = img;
 	    
 	    if (this.pieceImage == null)
 	    {
 		System.err.println("WARNING: Can't load piece image!");
 		Blackboard.broadcastMessage(new SystemMessage(null, "Can't load piece image, your blocks will lose graphical quality!"));
 		this.pieceImageW = this.pieceImageH = 1; //initialising
 	    }
 	    else
 	    {
 		this.pieceImageW = this.pieceImage.getWidth();
 		this.pieceImageH = this.pieceImage.getHeight();
 	    }
 	}
 	{
 	    BufferedImage img = null;
 	    try
 	    {
 		img = ImageIO.read(new File("paused.png"));
 	    }
 	    catch (final IOException err)
 	    {
 		//WARNING: Can't load paused image!
 		//will be printed soon
 	    }
 	    this.pausedImage = img;
 	    
 	    if (this.pausedImage == null)
 	    {
 		System.err.println("WARNING: Can't load pause image!");
 		Blackboard.broadcastMessage(new SystemMessage(null, "Can't load pause image!"));
 		this.pausedImageW = this.pausedImageH = 1; //initialising
 	    }
 	    else
 	    {
 		this.pausedImageW = this.pausedImage.getWidth();
 		this.pausedImageH = this.pausedImage.getHeight();
 	    }
 	}
 	
 	
 	final Thread updateThread = new Thread(this);
 	updateThread.setDaemon(true);
 	updateThread.start();
 	
 	
 	Blackboard.registerObserver(this);
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
      * The background of the game area
      */
     private final Color gameBackground = new Color(16, 16, 100);
     
     /**
      * The background of the game area when paused and pause image is missing
      */
     private final Color pausedBackground = new Color(100, 16, 16);
     
     /**
      * Piece matrix
      */
     private final Color[][] matrix;
     
     /**
      * The image printed on top of the board with paused
      */
     private final BufferedImage pausedImage;
     
     /**
      * The width of {@link #pausedImage}
      */
     private final int pausedImageW;
     
     /**
      * The height of {@link #pausedImage}
      */
     private final int pausedImageH;
     
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
      * Whether the local client is paused
      */
     private boolean paused = false;
     
     /**
      * The local player
      */
     private Player player = null;
     
     /**
      * Queued game board patches
      */
     final ArrayDeque<MatrixPatch> queuedPatches = new ArrayDeque<MatrixPatch>();
     
     
     
     
     /**
      * {@inheritDoc}
      */
     public void paint(final Graphics g)
     {
 	final Dimension screenDim = this.getSize();
 	final int screenW = (int)(screenDim.getWidth());
 	final int screenH = (int)(screenDim.getHeight());
 	
 	final BufferedImage offimg = new BufferedImage(screenW, screenH, BufferedImage.TYPE_INT_ARGB);
 	
 	final Graphics2D gg = offimg.createGraphics();
 	gg.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
 	gg.setRenderingHint(KEY_ANTIALIASING,        VALUE_ANTIALIAS_ON);
 	gg.setRenderingHint(KEY_COLOR_RENDERING,     VALUE_COLOR_RENDER_QUALITY);
 	gg.setRenderingHint(KEY_DITHERING,           VALUE_DITHER_ENABLE); //if needed
 	gg.setRenderingHint(KEY_FRACTIONALMETRICS,   VALUE_FRACTIONALMETRICS_OFF);
 	gg.setRenderingHint(KEY_INTERPOLATION,       VALUE_INTERPOLATION_BICUBIC); //no implemention for sinc available
 	gg.setRenderingHint(KEY_RENDERING,           VALUE_RENDER_QUALITY);
 	gg.setRenderingHint(KEY_STROKE_CONTROL,      VALUE_STROKE_PURE);
 	super.paint(gg);
 	
 	int pieceW = screenW / this.width;
 	int pieceH = screenH / this.height;
 	if (pieceW > pieceH)  pieceW = pieceH;
 	if (pieceH > pieceW)  pieceH = pieceW;
 	
 	final int offX = (screenW - this.width * pieceW) >> 1;
 	final int offY = screenH - this.height * pieceH;
 	
 	gg.setColor(this.gameBackground);
 	if (this.paused && (this.pausedImage == null))
 	    gg.setColor(this.pausedBackground);
 	gg.fillRect(offX, offY, this.width * pieceW, this.height * pieceH);
 	
 	Color colour;
 	for (int y = 0; y < this.height; y++)
 	    for (int x = 0; x < this.width; x++)
 		if ((colour = this.matrix[y][x]) != null)
 		{
 		    final int px, py;
 		    
 		    gg.setColor(colour);
 		    gg.fillRect(px = x * pieceW + offX, py = y * pieceH + offY, pieceW, pieceH);
 		    
 		    if (this.pieceImage != null)
 			gg.drawImage(this.pieceImage, px, py, px + pieceW, py + pieceH,
 				     0, 0, this.pieceImageW, this.pieceImageH, null);
 		}
 	
 	if (this.paused && (this.pausedImage != null))
 	    gg.drawImage(this.pausedImage, offX, offY, offX + this.width * pieceW, offY + this.height * pieceH,
 			 0, 0, this.pausedImageW, this.pausedImageH, null);
 	
 	g.drawImage(offimg, 0, 0, null);
     }
     
     
     /**
      * {@inheritDoc}
      */
     public void messageBroadcasted(final Blackboard.BlackboardMessage message)
     {
 	if (message instanceof MatrixPatch)
 	{
 	    final MatrixPatch patch = (MatrixPatch)message;
 	    synchronized (queuedPatches)
 	    {
 		queuedPatches.offerLast(patch);
 		queuedPatches.notifyAll();
 	    }
 	}
 	else if (message instanceof LocalPlayer)
         {
 	    this.player = ((LocalPlayer)message).player;
 	}
 	else if (message instanceof PlayerPause)
         {
 	    if (((PlayerPause)message).player == this.player)
 	    {
 		this.paused = ((PlayerPause)message).paused;
 		this.repaint();
 	    }
 	}
 	else if (message instanceof FullUpdate)
 	{
 	    final FullUpdate fullUpdate = (FullUpdate)message;
 	    if (fullUpdate.isGathering() == false)
 	    {
 		final Board board = ((EngineData)(fullUpdate.data.get(Engine.class))).board;
 		final Shape shape = ((EngineData)(fullUpdate.data.get(Engine.class))).fallingShape;
 		if (board == null)
 		    return;
 		final Block[][] matrix = board.getMatrix();
 		final boolean[][] erase = new boolean[20][10];
 		for (int y = 0; y < 20; y++)
 		    for (int x = 0; x < 10; x++)
 			erase[y][x] = true;
 		update(erase, matrix, 0, 0);
 		final Block[][] blocks = shape.getBlockMatrix();
		update(null, blocks, 0, 0);
 	    }
 	}
     }
     
     
     /**
      * {@inheritDoc}
      */
     public void run()
     {
 	for (;;)
 	{
 	    MatrixPatch patch;
 	    synchronized (queuedPatches)
 	    {
 		if (queuedPatches.isEmpty())
 		    try
 		    {   queuedPatches.wait();
 		    }
 		    catch (final InterruptedException err)
 		    {   break;
 		    }
 		
 		patch = queuedPatches.pollFirst();
 	    }
 	    update(patch.erase, patch.blocks, patch.offX, patch.offY);
 	}
     }
     
     
     /**
      * Updates the game matrix and redraws the area
      * 
      * @param  erase   A matrix where <code>true</code> indicates removal of block
      * @param  blocks  A matrix where non-<code>null</code> indicates to add a block
      * @param  offX    Left offset, where the first column in the matrices affect the game matrix
      * @param  offY    Top offset, where the first row in the matrices affect the game matrix
      */
     public void update(final boolean[][] erase, final Block[][] blocks, final int offX, final int offY)
     {
 	if (erase != null)
 	    for (int y = offY < 0 ? -offY : 0, h = erase.length; y < h; y++)
 	    {
 		final int Y = y + offY;
 		if (Y >= this.matrix.length)
 		    break;
 		
 		for (int x = offX < 0 ? -offX : 0, w = erase[y].length; x < w; x++)
 		    if (erase[y][x])
 			if (x + offX < this.matrix[Y].length)
 			    this.matrix[Y][x + offX] = null;
 			else
 			    break;
 	    }
 	
 	if (blocks != null)
 	    for (int y = offY < 0 ? -offY : 0, h = blocks.length; y < h; y++)
 	    {
 		final int Y = y + offY;
 		if ((Y >= this.matrix.length) || (0 > Y))
 		    break;
 		
 		for (int x = offX < 0 ? -offX : 0, w = blocks[y].length; x < w; x++)
 		    if (blocks[y][x] != null)
 			if (x + offX < this.matrix[Y].length)
 			    this.matrix[Y][x + offX] = ColourMapper.getColour(blocks[y][x].getColor());
 			else
 			    break;
 	    }
 	
 	this.repaint();
     }
     
     
     /**
      * {@inheritDoc}
      */
     protected void processKeyEvent(final KeyEvent e)
     {
 	if (e.getID() != KeyEvent.KEY_PRESSED)
 	    return; 
 
 	final boolean shift = e.isShiftDown();
 	switch (e.getKeyCode())
 	{
 	    case KeyEvent.VK_S:
 		Blackboard.broadcastMessage(new GamePlayCommand(GamePlayCommand.Move.ANTICLOCKWISE));
 		break;
 		
 	    case KeyEvent.VK_D:
 		Blackboard.broadcastMessage(new GamePlayCommand(GamePlayCommand.Move.CLOCKWISE));
 		break;
 		
 	    case KeyEvent.VK_UP:
 		Blackboard.broadcastMessage(new GamePlayCommand(shift ? GamePlayCommand.Move.CLOCKWISE : GamePlayCommand.Move.ANTICLOCKWISE));
 		break;
 		
 	    case KeyEvent.VK_DOWN:
 		Blackboard.broadcastMessage(new GamePlayCommand(GamePlayCommand.Move.DOWN));
 		break;
 		
 	    case KeyEvent.VK_LEFT:
 		Blackboard.broadcastMessage(new GamePlayCommand(shift ? GamePlayCommand.Move.ANTICLOCKWISE : GamePlayCommand.Move.LEFT));
 		break;
 		
 	    case KeyEvent.VK_RIGHT:
 		Blackboard.broadcastMessage(new GamePlayCommand(shift ? GamePlayCommand.Move.CLOCKWISE : GamePlayCommand.Move.RIGHT));
 		break;
 		
 	    case KeyEvent.VK_SPACE:
 		Blackboard.broadcastMessage(new GamePlayCommand(GamePlayCommand.Move.DROP));
 		break;
 		
 	    case KeyEvent.VK_PAUSE:
 	    case KeyEvent.VK_P:
 		if (this.player != null)
 		    Blackboard.broadcastMessage(new PlayerPause(this.player, !this.paused));
 		break;
 	}
     }
     
     
     /**
      * {@inheritDoc}
      */
     public String toString()
     {   return "(GamePanel)";
     }
     
 }
 
