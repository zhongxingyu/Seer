 package bump.org.comp.color;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 import java.awt.Graphics;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 public class ColorDisplay extends Canvas {
 	public ColorDisplay() {
 		super();
 		abs = new ColorList();
 		HitBoxes = new ArrayList<Rectangle2D.Float>();
 	}
 
 	ArrayList<Rectangle2D.Float> HitBoxes;
 
 	/**
 	 * If a mouse click is within the bounds of one of the hitboxes, then return
 	 * the index.
 	 * 
 	 * @param x
 	 *            the x coordinate of the click
 	 * @param y
 	 *            the y coordinate of the click
 	 * @return the index of the clicked upon box. Or negative one if it manages
 	 *         to miss.
 	 */
 	public int getIndexOfMouseclip(int x, int y) {
 		for (int i = 0; i < HitBoxes.size(); i++) {
 			if (HitBoxes.get(i).contains(new Point2D.Float(x, y))) {
 				// set the property so that the component may be repainted to
 				// show the selection.
 				indexselected = i;
 				// If there is a valid selection, repaint the thingy.
 				repaint();
 				return i;
 			}
 		}// return a negative number, indicating that it does not work properly.
 		return -1;
 	}
 
 	/**
 	 * The index of the selected color in the list.
 	 */
 	private int indexselected = -1;
 
 	public void paint(Graphics g) {
 		int hstep = getWidth();
 		int vstep;
 		HitBoxes.clear();
 		try {
 			vstep = getHeight() / abs.getMaxStep();
 			if (isGridded()) {
 				hstep = getWidth() / (abs.getMaxStep() / 5);
 				vstep = getHeight() / 5;
 			}
 		} catch (ArithmeticException e) {
 			// if there is nothing in the list, it should not try to paint.
 			return;
 		}
 		ArrayList<Color> a = abs.getColors();
 		for (int i = 0; i < a.size(); i++) {
 			g.setColor(a.get(i));
 			if (isGridded()) {
 				g.fillRect(hstep * (i / 5), vstep * (i % 5), hstep, vstep);
 				HitBoxes.add(new Rectangle2D.Float(hstep * (i / 5), vstep * i,
 						hstep, vstep));
 				// if the index is selected, draw a black rectangle around it.
 				if (i == indexselected) {
 
 				}
 			} else {
 				g.fillRect(0, vstep * i, hstep, vstep);
 				HitBoxes.add(new Rectangle2D.Float(0, vstep * i, hstep, vstep));
 				// if the index is selected, draw a black rectangle around it.
 			}
 		}
 		if (indexselected != -1) {
 			g.setColor(Color.black);
 			g.setXORMode(Color.white);
 			if (gridded) {
 
 				g.drawRect(hstep * (indexselected / 5), vstep
 						* (indexselected % 5), hstep, vstep);
 			} else {
 				g.drawRect(0, vstep * indexselected, hstep, vstep);
 			}
 		}
 
 	}
 
 	public Color getColorAtIndex(int i) {
 		return abs.getColorAtStep(i);
 	}
 
 	public void addColor(Color g) {
 		abs.addColor(g);
 		repaint();
 	}
 
 	public void addColor(List<Color> g) {
 		abs.addColors(g);
 		repaint();
 	}
 
 	public void setColorAtIndex(int i, Color g) {
 		abs.setColorAtStep(i, g);
 		repaint();
 	}
 
 	/**
 	 * 
 	 * @return Whether or not the component is currently using a grid layout
 	 */
 	public boolean isGridded() {
 		return gridded;
 	}
 
 	/**
 	 * @param gridded
 	 *            Whether or not it should tile the colors on the list
 	 */
 	public void setGridded(boolean gridded) {
 		this.gridded = gridded;
 	}
 
 	/**
 	 * Controls whether or not the colors should be laid out on a table.
 	 */
 	private boolean gridded = false;
 	ColorList abs;
 }
