 package vooga.fighter.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import vooga.fighter.controller.ViewDataSource;
 
 /**
  * Creates an area of the screen in which the game will be drawn that supports:
  * <UL>
  * <LI>animation via the Timer
  * <LI>mouse input via the MouseListener
  * <LI>keyboard input via the KeyListener
  * </UL>
  * 
  * @author Robert C Duvall
  * @author Wayne You
  */
 public class Canvas extends JComponent {
 	// default serialization ID
 
 	// default serialization ID
 	private static final long serialVersionUID = 1L;
 
 	// game to be animated
 	private ViewDataSource myViewDataSource;
 	// current layout of the game
 	private CanvasLayout myLayout = null;
 
 	private PaletteSwap myPaletteSwap;
 
 	/**
 	 * Create a panel so that it knows its size
 	 */
 	public Canvas(Dimension size) {
 		// set size (a bit of a pain)
 		setPreferredSize(size);
 		setSize(size);
 		// prepare to receive input
 		setFocusable(true);
 		requestFocus();
 	}
 
 	/**
 	 * Sets the data source from the controller.
 	 * 
 	 * @param data
 	 */
 	public void setViewDataSource(ViewDataSource data) {
 		myViewDataSource = data;
 	}
 
 	/**
 	 * Sets up the layout of the view. Null implies no layout.
 	 * 
 	 * @param layout
 	 */
 	public void setLayout(CanvasLayout layout) {
 		myLayout = layout;
 	}
 
 	/**
 	 * Calls java's repaint method.
 	 */
 	public void paint() {
 		repaint();
 	}
 
 	/**
 	 * Paint the contents of the canvas.
 	 * 
 	 * Never called by you directly, instead called by Java runtime when area of
 	 * screen covered by this container needs to be displayed (i.e., creation,
 	 * uncovering, change in status)
 	 * 
 	 * @param pen
 	 *            used to paint shape on the screen
 	 */
 	@Override
 	public void paintComponent(Graphics pen) {
 		pen.setColor(Color.WHITE);
 		pen.fillRect(0, 0, getSize().width, getSize().height);
 		// If there is no defined layout, simply paint things at the locations
 		// they are given.
 		if (myLayout == null) {
 			for (int i = 0; i < myViewDataSource.ObjectNumber(); i++) {
 				paintPaintable((Graphics2D) pen, i);
 			}
 		} else {
 			myLayout.paintComponents((Graphics2D) pen, myViewDataSource,
 					this.getSize());
 		}
 	}
 
 	private void paintPaintable(Graphics2D pen, int index) {
 
 		if (myViewDataSource.getImageEffects(index).size() == 0) {
 			pen.setColor(Color.blue);
 			myViewDataSource.getPaintable(index).paint(pen,
 					myViewDataSource.getLocation(index),
 					myViewDataSource.getSize(index));
 		}
 
 		else if (myViewDataSource.getImageEffects(index).get(0) == -1) {
 			AffineTransform saveAT = pen.getTransform();
 			pen.transform(AffineTransform.getScaleInstance(1, -1));
 			myViewDataSource.getPaintable(index).paint(pen,
 					myViewDataSource.getLocation(index),
 					myViewDataSource.getSize(index));
 			pen.setTransform(saveAT);
 		}
 
		else if (myViewDataSource.getImageEffects(index).size() > 1
				&& myViewDataSource.getImageEffects(index).get(1) == -1) {

			myPaletteSwap.setImageToGreyScale(myViewDataSource
					.getPaintable(index));
			myViewDataSource.getPaintable(index).paint(pen,
					myViewDataSource.getLocation(index),
					myViewDataSource.getSize(index));
		}
 	}
 
 	/**
 	 * Opens an explorer window to select a file. Returns null on cancel and
 	 * absolute path on success.
 	 * 
 	 * @param descriptor
 	 *            Description of what types of files are being searched for.
 	 * @param extensions
 	 *            Zero or more file extensions to filter for. (do not prefix
 	 *            with a period)
 	 * @return The absolute path name of a file, or null.
 	 */
 	public String chooseFile(String descriptor, String... extensions) {
 		JFileChooser chooser = new JFileChooser();
 
 		FileNameExtensionFilter filter = new FileNameExtensionFilter(
 				descriptor, extensions);
 		chooser.setFileFilter(filter);
 		int returnVal = chooser.showOpenDialog(this);
 
 		if (returnVal == JFileChooser.APPROVE_OPTION)
 			return chooser.getSelectedFile().getAbsolutePath();
 		else
 			return null;
 	}
 
 	/**
 	 * Prompts the user for a string.
 	 * 
 	 * @param title
 	 *            The title of the dialog box.
 	 * @param text
 	 *            The text of the dialog box.
 	 * @return The string input, or null for no input.
 	 */
 	public String promptForString(String title, String text) {
 		return JOptionPane.showInputDialog(this, text, title,
 				JOptionPane.PLAIN_MESSAGE);
 	}
 }
