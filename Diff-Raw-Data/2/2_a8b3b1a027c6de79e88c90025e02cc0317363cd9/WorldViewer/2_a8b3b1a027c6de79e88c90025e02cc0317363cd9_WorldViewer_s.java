 package yuuki.ui;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import yuuki.util.ElementGrid;
 import yuuki.util.Grid;
 import yuuki.world.Locatable;
 import yuuki.world.Tile;
 import yuuki.world.TileFactory;
 /**
  * Displays the overworld graphically.
  */
 @SuppressWarnings("serial")
 public class WorldViewer extends JPanel {
 	
 	/**
 	 * The exact text being displayed.
 	 */
 	private Grid<java.lang.Character> buffer;
 	
 	/**
 	 * The section of the buffer that contains the drawn map.
 	 */
 	private Grid<java.lang.Character> bufferView;
 	
 	/**
 	 * The Locatables on the screen.
 	 */
 	private Set<Locatable> locatables;
 	
 	/**
 	 * The view of the world that is being drawn.
 	 */
 	private Grid<Tile> subView;
 	
 	/**
 	 * The main text area for this world viewer.
 	 */
 	private JTextArea textArea;
 	
 	/**
 	 * The current view of this world.
 	 */
 	private Grid<Tile> view;
 	
 	/**
 	 * Creates a new WorldViewer that can display the specified number of
 	 * tiles.
 	 * 
 	 * @param width The width of this WorldViewer in tiles.
 	 * @param height The height of this WorldViewer in tiles.
 	 */
 	public WorldViewer(int width, int height) {
 		Dimension d = new Dimension(width, height);
 		buffer = new ElementGrid<java.lang.Character>(d);
 		locatables = new HashSet<Locatable>();
 		textArea = new JTextArea(height, width);
 		textArea.setEditable(false);
 		textArea.setFocusable(false);
 		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
 		add(textArea);
 	}
 	
 	/**
 	 * Adds a Locatable to the current display.
 	 * 
 	 * @param l The Locatable to add.
 	 */
 	public void addLocatable(Locatable l) {
 		locatables.add(l);
 	}
 	
 	/**
 	 * Removes all Locatables from the current display.
 	 */
 	public void clearLocatables() {
 		locatables.clear();
 	}
 	
 	/**
 	 * Gets the Locatables that fall within a certain rectangle.
 	 * 
 	 * @param box The Rectangle from within the Locatables should be drawn.
 	 * 
 	 * @return The Locatables that currently fall within the bounding box.
 	 */
 	public ArrayList<Locatable> getLocatablesInBox(Rectangle box) {
 		ArrayList<Locatable> desired = new ArrayList<Locatable>();
 		for (Locatable l : locatables) {
 			if (box.contains(l.getLocation())) {
 				desired.add(l);
 			}
 		}
 		return desired;
 	}
 	
 	/**
 	 * Sets the view of the world being displayed.
 	 * 
 	 * @param view The view to show.
 	 */
 	public void setView(Grid<Tile> view) {
 		this.view = view;
 	}
 	
 	/**
 	 * Updates this WorldViewer to show a new area.
 	 * 
 	 * @param center The center of the area to show.
 	 */
 	public void updateDisplay(Point center) {
 		clearBuffer();
 		Point requested = setSubView(center);
 		setBufferView(requested);
 		drawSubView();
 		drawLocatables();
 		showBuffer();
 	}
 	
 	/**
 	 * Sets all tiles in the tile buffer to be empty.
 	 */
 	private void clearBuffer() {
 		Point p = new Point();
 		for (p.y = 0; p.y < textArea.getRows(); p.y++) {
 			for (p.x = 0; p.x < textArea.getColumns(); p.x++) {
 				buffer.set(p, TileFactory.VOID_CHAR);
 			}
 		}
 	}
 	
 	/**
 	 * Draws the locatables on the screen.
 	 */
 	private void drawLocatables() {
 		Rectangle box;
 		box = new Rectangle(subView.getLocation(), subView.getSize());
 		ArrayList<Locatable> ls = getLocatablesInBox(box);
 		for (Locatable l : ls) {
			Point p = l.getLocation();
 			p.x -= box.x;
 			p.y -= box.y;
 			bufferView.set(p, l.getDisplayable().getDisplayChar());
 		}
 	}
 	
 	/**
 	 * Draws the sub view onto the buffer.
 	 */
 	private void drawSubView() {
 		Point p = new Point(0, 0);
 		Dimension size = bufferView.getSize();
 		for (p.x = 0; p.x < size.width; p.x++) {
 			for (p.y = 0; p.y < size.height; p.y++) {
 				bufferView.set(p, subView.itemAt(p).getDisplayChar());
 			}
 		}
 	}
 	
 	/**
 	 * Sets the buffer sub view as the section that matches the draw position
 	 * of the current world sub view.
 	 * 
 	 * @param request The requested upper-left corner.
 	 */
 	private void setBufferView(Point request) {
 		Rectangle subBox, bufBox, bufView;
 		subBox = new Rectangle(subView.getLocation(), subView.getSize());
 		bufBox = new Rectangle(buffer.getLocation(), buffer.getSize());
 		bufView = new Rectangle(buffer.getLocation(), buffer.getSize());
 		if (subBox.height < bufBox.height) {
 			int shiftAmount = subBox.y - request.y;
 			bufView.y += shiftAmount;
 			bufView.height -= (bufBox.height - subBox.height);
 		}
 		if (subBox.width < bufBox.width) {
 			int shiftAmount = subBox.x - request.x;
 			bufView.x += shiftAmount;
 			bufView.width -= (bufBox.width - subBox.width);
 		}
 		bufferView = buffer.getSubGrid(bufView);
 	}
 	
 	/**
 	 * Gets the proper sub view centered about a point.
 	 * 
 	 * @param center The center of the view to set as the sub view.
 	 * 
 	 * @return The position of the requested upper-left corner.
 	 */
 	private Point setSubView(Point center) {
 		Dimension size = buffer.getSize();
 		Point actualLocation = new Point(center);
 		actualLocation.translate(-(size.width / 2), -(size.height / 2));
 		Rectangle subBox = new Rectangle(actualLocation, size);
 		subView = view.getSubGrid(subBox);
 		return subBox.getLocation();
 	}
 	
 	/**
 	 * Updates the actual display area with the buffer.
 	 */
 	private void showBuffer() {
 		StringBuilder sb = new StringBuilder();
 		Point p = new Point(0, 0);
 		for (p.y = 0; p.y < textArea.getRows(); p.y++) {
 			for (p.x = 0; p.x < textArea.getColumns(); p.x++) {
 				sb.append(buffer.itemAt(p));
 			}
 			if (p.y < textArea.getRows() - 1) {
 				sb.append('\n');
 			}
 		}
 		textArea.setText(sb.toString());
 	}
 	
 }
