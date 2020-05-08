 package drawing;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import simulation.Turtle;
 import util.Pixmap;
 import view.View;
 import exceptions.CorruptedEnvironmentException;
 import exceptions.IllegalInstructionException;
 
 /**
  * Represents all possible background layers for the Canvas. The Background is
  * paintable and paints all images in a pre-defined order.
  * 
  * 
  * Currently supports two layers: one grid, and one color image, but more can be
  * added by adding additional Maps for each type of background, and additional
  * indices
  * 
  * @author Ellango, Ryan
  * 
  */
 public class Background {
 	private static final int GRID_PRIORITY = 1;
 	private static final int WHITE_BACKGROUND_INDEX = 0;
 	private static final PriorityPixmap GRID = new PriorityPixmap("grid.png",
 			GRID_PRIORITY);
 
 	private Palette myPalette;
 	private List<PriorityPixmap> myActiveImages;
 	private int myCurrentColorImageIndex;
 
 	/**
 	 * Constructs a BackgroundLayer with a white background image
 	 * 
 	 * @param palette
 	 *            is a palette that will be used to generate the background
 	 * 
 	 */
 	public Background(Palette palette) {
 		myPalette = palette;
 		myActiveImages = new ArrayList<PriorityPixmap>();
 		try {
 			myActiveImages.add(myPalette
 					.getBackgroundColorImage(WHITE_BACKGROUND_INDEX));
 		} catch (IllegalInstructionException e) {
 			throw new CorruptedEnvironmentException();
 		}
 		myCurrentColorImageIndex = WHITE_BACKGROUND_INDEX;
 	}
 
 	/**
 	 * Paints the canvas with all the background layers in order
 	 * 
 	 * @param pen
 	 *            is the pen that will be used to paint the background
 	 */
 	public void paint(Graphics2D pen) {
 		Collections.sort(myActiveImages, new Comparator<PriorityPixmap>() {
 			@Override
 			public int compare(PriorityPixmap arg0, PriorityPixmap arg1) {
 				return arg0.getPriority() - arg1.getPriority();
 			}
 		});
 		for (Pixmap image : myActiveImages) {
 			image.paint(pen, Turtle.startingLocation(),
 					View.PREFERRED_CANVAS_SIZE);
			
 		}
 	}
 
 	/**
 	 * Turns the grid on
 	 */
 	public void gridOn() {
 		// only one grid allowed
 		gridOff();
 		myActiveImages.add(GRID);
 	}
 
 	/**
 	 * Turns the grid off
 	 */
 	public void gridOff() {
 		myActiveImages.remove(GRID);
 	}
 
 	/**
 	 * Switches the background color image with the one specified in the Palette
 	 * at the provided index
 	 * 
 	 * 
 	 * @param index
 	 *            corresponding to the new image
 	 * @throws IllegalInstructionException
 	 *             if no image corresponds to the provided index
 	 */
 	public void switchColorImage(int index) throws IllegalInstructionException {
 		PriorityPixmap currentImage = myPalette
 				.getBackgroundColorImage(myCurrentColorImageIndex);
 		myActiveImages.remove(currentImage);
 
 		myActiveImages.add(myPalette.getBackgroundColorImage(index));
 		myCurrentColorImageIndex = index;
 	}
 
 }
