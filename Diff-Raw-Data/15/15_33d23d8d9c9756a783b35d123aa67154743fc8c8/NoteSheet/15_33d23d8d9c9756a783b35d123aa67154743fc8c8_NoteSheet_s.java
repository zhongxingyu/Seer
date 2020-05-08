 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 package jscribble.notebook;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import jscribble.Localizer;
 import jscribble.NoteBookProgram;
 
 /**
  * Represents a single note sheet. A NoteSheet consist a grayscale image. The
  * image is displayed, allowing the user to paint on it. Each image has its
  * individual file name that is used automatically.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
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
 
 
 	/**
 	 * Graphics context of the image.
 	 */
 	private Graphics2D graphics;
 
 
 	/**
 	 * The size of this sheet in pixels.
 	 */
 	private Dimension noteSize;
 
 
 	/**
 	 * Whether a picture has any lines drawn on it.
 	 */
 	private boolean touched = false;
 
 
 	/**
 	 * Whether the picture was changed after the last save. A newly created
 	 * picture is not unsaved since there is nothing worth saving.
 	 */
 	private boolean unsaved = false;
 
 
 	/**
 	 * Storage point of the image of this sheet.
 	 */
 	private File imagefile;
 
 
 	/**
 	 * A singleton thread that handles all the writing to disk.
 	 */
 	private static WriteoutThread writethread;
 
 
 	/**
 	 * Creates an empty note sheet.
 	 *
 	 * @param noteSize size of the sheet
 	 * @param pagenumber given page number
 	 * @param infile File to save image in
 	 */
 	public NoteSheet(Dimension noteSize, int pagenumber, File infile) {
 		this.noteSize = noteSize;
 		this.pagenumber = pagenumber;
 		this.imagefile = infile;
 
 		// If no filename or a filename that does not exist yet is given or the
 		// file is empty (like a newly created temporary file), a new image
 		// needs to be created.
 		if (imagefile == null || !imagefile.exists() ||
 		        imagefile.length() == 0) {
 			initNewImage();
 		}
 
 		// If this branch is reached, then a image file does exist and it is
 		// not empty. This sheet has something drawn on it then.
 		else {
 			touched = true;
 		}
 	}
 
 
 	/**
 	 * Draws a line onto the sheet. The sheet is then marked as "touched".
 	 */
 	public void drawLine(int x, int y, int x2, int y2) {
 		touched = true;
 		unsaved = true;
 
 		getGraphics().setColor(new Color(0, 0, 0));
 		getGraphics().drawLine(x, y, x2, y2);
 	}
 
 
 	/**
 	 * Getter for the file of the image.
 	 */
 	public File getFile() {
 		return imagefile;
 	}
 
 
 	/**
 	 * Gets the graphics context of the image.
 	 */
 	private Graphics2D getGraphics() {
 		if (graphics == null) {
 			BufferedImage im = getImg();
 			if (im == null) {
 				NoteBookProgram.log(getClass().getName(),
 				        Localizer.get("BufferedImage is null."));
 			}
 			graphics = (Graphics2D)(im.getGraphics());
 			graphics.setRenderingHints(new
 			        RenderingHints(RenderingHints.KEY_ANTIALIASING,
 			                RenderingHints.VALUE_ANTIALIAS_ON));
 		}
 
 		return graphics;
 	}
 
 
 	/**
 	 * Gets the image, loads it from disk id needed.
 	 */
 	public BufferedImage getImg() {
 		if (isSwapped()) {
 			loadFromFile();
 		}
 		if (img == null) {
 			throw new NullPointerException(Localizer.get(
 			            "Could not load image from disk."));
 		}
 		return img;
 	}
 
 
 	/**
 	 * Get the sheet's page number.
 	 */
 	public int getPagenumber() {
 		return pagenumber;
 	}
 
 
 	/**
 	 * Generates a new image and allocates a temporary file if none was set.
 	 */
 	private void initNewImage() {
 		if (imagefile == null) {
 			try {
 				// generate a temporary file
 				imagefile = File.createTempFile(
 				        NoteBookProgram.getProgramname() + "-", ".png");

				// TODO set the permissions of the file to 600
				/*
				imagefile.setReadable(false, true);
				imagefile.setWritable(false, true);
 
 				assert(!imagefile.canRead());
 				assert(!imagefile.canWrite());
 
				imagefile.setReadable(true, false);
				imagefile.setWritable(true, false);
				*/
 
 				// Since this is only a temporary file, delete if after using
 				// it.
 				imagefile.deleteOnExit();
 			}
 			catch (IOException e) {
 				NoteBookProgram.handleError(Localizer.get(
 				            "Could not create a temp file."));
 				e.printStackTrace();
 			}
 		}
 
 		img = new BufferedImage(noteSize.width, noteSize.height,
 		        BufferedImage.TYPE_BYTE_GRAY);
 
 		graphics = getGraphics();
 		graphics.setColor(new Color(255, 255, 255));
 		graphics.fillRect(0, 0, noteSize.width, noteSize.height);
 
 		unsaved = false;
 	}
 
 
 	/**
 	 * Whether the picture is swapped to disk. To save RAM, the pictures might
 	 * be swapped into a temporary file if the user is in another part of the
 	 * NoteBook.
 	 */
 	private boolean isSwapped() {
 		return img == null;
 	}
 
 
 	/**
 	 * Loads the image from file. Everything else is left intact.
 	 */
 	public void loadFromFile() {
 		// If the image was scheduled for writing but not written now, force
 		// the thread to write it. If the file is empty, then the
 		// WriteoutThread did not get a change to write the file yet. Join with
 		// the thread.
 
 		// TODO test whether the image is actually in the queue first
 		if (!imagefile.exists() || imagefile.length() == 0L) {
 			NoteBookProgram.log(getClass().getName(),
 			        Localizer.get("Image file does not exist."));
 			stopWriteoutThread();
 		}
 
 		// If the file still does not exist, it has not been written out.
 		if (!imagefile.exists() || imagefile.length() == 0L) {
 			NoteBookProgram.log(getClass().getName(), Localizer.get(
 			            "Image file does not exist after stopping WriteoutThread."
 			        ));
 
 			initNewImage();
 		}
 
 		// If the file exists, load it up.
 		else {
 			try {
 				NoteBookProgram.log(getClass().getName(),
 				        String.format(Localizer.get("Loading %s."),
 				                imagefile.getAbsolutePath()));
 
 				img = ImageIO.read(imagefile);
 			}
 			catch (FileNotFoundException e) {
 				NoteBookProgram.handleError(Localizer.get(
 				            "Could not find the note sheet image."));
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				NoteBookProgram.handleError(Localizer.get(
 				            "Could not read the note sheet image."));
 				e.printStackTrace();
 			}
 
 			// The image *should* be loaded by now.
 			if (img == null) {
 				throw new NullPointerException("Could not load image from disk.");
 			}
 
 			// Since it was saved, it has to have some lines on it.
 			touched = true;
 		}
 	}
 
 
 	/**
 	 * Saves the picture to a PNG file. The image is then removed from the
 	 * heap.
 	 */
 	public void saveToFile() {
 		NoteBookProgram.log(getClass().getName(),
 		        String.format(Localizer.get("Picture %d is %s and %s."),
 		                pagenumber,
 		                (touched ? Localizer.get("touched") :
 		                        Localizer.get("untouched")),
 		                (unsaved ? Localizer.get("unsaved") :
 		                        Localizer.get("saved"))
 		                     )
 		                   );
 		if (touched && unsaved) {
 			NoteBookProgram.log(getClass().getName(), String.format(
 			            Localizer.get("Scheduling %s for writing."),
 			            imagefile.getAbsolutePath()));
 
 
 			if (writethread == null || !writethread.isAlive()) {
 				writethread = new WriteoutThread();
 			}
 			writethread.schedule(new ImageSwapTask(img, imagefile));
 		}
 
 		// remove the image from the memory
 		img = null;
 		graphics = null;
 
 		unsaved = false;
 	}
 
 
 	/**
 	 * Stops the WriteoutThread and waits for it. This ensures that everything
 	 * is written to disk properly.
 	 */
 	public void stopWriteoutThread() {
 		try {
 			if (writethread != null) {
 				writethread.stopAfterLast();
 				writethread.join();
 			}
 		}
 		catch (InterruptedException e) {
 			NoteBookProgram.handleError(Localizer.get(
 			            "WriteThread was interrupted."));
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Whether this sheet has any lines drawn onto it.
 	 */
 	public boolean touched() {
 		return touched;
 	}
 
 
 	/**
 	 * Whether this sheet has unsaved changes.
 	 */
 	public boolean unsaved() {
 		return unsaved;
 	}
 }
