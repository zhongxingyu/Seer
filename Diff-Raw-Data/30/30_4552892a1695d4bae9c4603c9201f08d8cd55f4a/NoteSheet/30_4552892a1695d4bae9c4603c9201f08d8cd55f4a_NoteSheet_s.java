 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.util.logging.Logger;
 
 /**
  * Represents a single note sheet.
  */
 public class NoteSheet {
 	/**
 	 * Page number for the current page.
 	 */
 	private int pagenumber;
 
 	/**
 	 * The picture for storing the drawing on.
 	 */
 	private BufferedImage img;
 
 	private Graphics2D graphics;
 
 	private int width;
 
 	private int height;
 
 	/**
 	 * Whether the picture was changed in any way since the last saving. A blank
 	 * picture is untouched.
 	 */
 	private boolean touched = false;
 
 	private boolean unsaved = false;
 
 	/**
 	 * Storage point of the image of this sheet.
 	 */
 	private File filename;
 
 	/**
 	 * A singleton thread that handles all the writing to disk.
 	 */
 	private static WriteoutThread writethread;
 
 	/**
 	 * Creates an empty note sheet.
 	 *
 	 * @param width width of the sheet in pixels
 	 * @param height height of the sheet in pixels
 	 */
 	public NoteSheet(int width, int height, int pagenumber, File infile) {
 		if (writethread == null) {
 			writethread = new WriteoutThread();
 		}
 
 		this.width = width;
 		this.height = height;
 		this.pagenumber = pagenumber;
 		this.filename = infile;
 
 		if (filename == null || !filename.exists()) {
 			if (filename == null) {
 				System.out.println("generating tempfile");
 				try {
 					filename = File.createTempFile("jscribble-", ".png");
 				}
 				catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 			img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
 
 			graphics = getGraphics();
 			graphics.setColor(new Color(255, 255, 255));
 			graphics.fillRect(0, 0, width, height);
 
 			unsaved = true;
 		}
 	}
 
 	/**
 	 * Get the sheet's page number.
 	 * @return pagenumber
 	 */
 	public int getPagenumber() {
 		return pagenumber;
 	}
 
 
 	/**
 	 * Gets the image, loads it from disk id needed.
 	 * @return the image
 	 */
 	public BufferedImage getImg() {
 		if (isSwapped()) {
 			loadFromFile();
 		}
 		return img;
 	}
 
 	/**
 	 * Width of the image.
 	 * @return width
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * Height of the image.
 	 * @return height
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 
 	/**
 	 * Loads the image from a given file. Everything else is left intact.
 	 *
 	 * @param infile filename to load from
 	 */
 	public void loadFromFile() {
 		try {
 			System.out.println("reading " + filename.getCanonicalPath());
 
 			// TODO load in a separate thread
 			img = javax.imageio.ImageIO.read(filename);
 		}
 		catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		touched = true;
 	}
 
 	/**
 	 * Saves the picture to a PNG file. The image is then removed from the heap.
 	 */
 	public void saveToFile() {
 		if (touched && unsaved) {
 			writethread.schedule(new ImageSwapTask(img, filename));
 		}
 
 		// remove the image from the memory
 		img = null;
 
 		touched = false;
 		unsaved = false;
 	}
 
 	/**
 	 * Draws a line onto the sheet. The sheet is then marked as "touched".
 	 */
 	public void drawLine(int x, int y, int x2, int y2) {
 		touched = true;
 		unsaved = true;
 
 		Graphics2D graphics = getGraphics();
 
 		graphics.setColor(new Color(0, 0, 0));
 		graphics.drawLine(x, y, x2, y2);
 	}
 
 	/**
 	 * @return whether this sheet has any lines drawn onto it
 	 */
 	public boolean touched() {
 		return touched;
 	}
 
 	public boolean unsaved() {
 		return unsaved;
 	}
 
 	/**
 	 * Stops the writeout thread and waits for it. This ensures that everything
 	 * is written to disk properly.
 	 */
 	public void stopWriteoutThread() {
 		try {
 			writethread.stopAfterLast();
 			writethread.join();
 		}
 		catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Gets the graphics context of the image.
 	 * @return graphics object
 	 */
 	private Graphics2D getGraphics() {
 		if (graphics == null) {
 			graphics = (Graphics2D)(getImg().getGraphics());
 			graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
 		}
 
 		return graphics;
 	}
 
 	/**
 	 * Whether the picture is swapped to disk. To save RAM, the pictures might
 	 * be swapped into a temp file if the user is in another part of the
 	 * notebook.
 	 */
 	private boolean isSwapped() {
 		return img == null;
 	}
 }
 
