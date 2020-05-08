 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.ImageObserver;
 
 import javax.swing.JPanel;
 
 /**
  * Displays the current page of a NoteBook. It also listens to the mouse and
  * relays the movements to the NoteBook as line drawing commands. It also
  * features a command listener for more user interaction.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
  */
 @SuppressWarnings("serial")
 public class DrawPanel extends JPanel {
 	/**
 	 * Color of the help lines.
 	 */
 	private static final Color lineColor = new Color(200, 200, 200);
 
 	/**
 	 * The spacing between the help lines.
 	 */
 	private static final int lineSpacing = 40;
 
 	/**
 	 * The NoteBook that is displayed.
 	 */
 	private NoteBook notebook;
 
 	/**
 	 * Handles the image output.
 	 */
 	private ImageObserver io = this;
 
 	/**
 	 * Whether helping lines are enabled.
 	 */
 	private boolean lines = false;
 
 	/**
 	 * Whether to display the help panel.
 	 */
 	private boolean showHelp = false;
 
 	private HelpItem[] helpItems = {
 		new HelpItem("h", "show help"),
 		new HelpItem("j, <Space>, <Enter>, <DownArrow>, <RightArrow>", "go forward"),
 		new HelpItem("k, <Backspace>, <UpArrow>, <LeftArrow>", "go backward"),
 		new HelpItem("f, <Pos1>", "goto first"),
 		new HelpItem("l, <End", "goto last"),
 		new HelpItem("<Alt-F4> / <CMD-Q>", "save & exit")
 
 	};
 
 	/**
 	 * Creates a new display panel that will listen to changes from a specific
 	 * NoteBook.
 	 *
 	 * @param notebook the NoteBook to display
 	 */
 	public DrawPanel(NoteBook notebook) {
 		this.notebook = notebook;
 		notebook.setDoneDrawing(new Redrawer(this));
 
 		PaintListener pl = new PaintListener(notebook);
 		addMouseMotionListener(pl);
 		addMouseListener(pl);
 	}
 
 
 	/**
 	 * Set whether help lines are to be drawn.
 	 *
 	 * @param b status of the lines
 	 */
 	public void setLines(boolean b) {
 		this.lines  = b;
 	}
 
 
 	/**
 	 * Whether to display the help panel.
 	 */
 	public void toggleHelp() {
 		showHelp = !showHelp;
 	}
 
 
 	/**
 	 * Draws the NoteSheet and page number. If lines are on, they are drawn on
 	 * top of the image as well.
 	 *
 	 * @param g graphics context (usually given by Java itself).
 	 */
 	protected void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
 
 		g2.drawImage(notebook.getCurrentSheet().getImg(), 0, 0, io);
 
 		if (lines) {
 			// TODO draw the lines below the drawing
 			g2.setColor(lineColor);
 			for (int i = lineSpacing; i < getWidth(); i += lineSpacing) {
 				g2.drawLine(i, 0, i, getHeight());
 			}
 
 
 			for (int i = lineSpacing; i < getHeight(); i += lineSpacing) {
 				g2.drawLine(0, i, getWidth(), i);
 			}
 		}
 
 		g2.setColor(Color.BLUE);
 		g2.drawString(String.format("Page %d/%d", notebook.getCurrentSheet().getPagenumber(), notebook.getSheetCount()), getWidth() / 2, 15);
 
 		if (showHelp) {
 			g2.setColor(new Color(0, 0, 0, 200));
 			g2.fillRoundRect(50, 50, getWidth() - 100, getHeight() - 100, 20, 20);
 			g2.setColor(Color.WHITE);
 
			int i = 0;
 			int vspacing = 30;
 			int spacing = 150;
 			int padding = 70;
 			for (HelpItem h : helpItems) {
 				g2.drawString(h.helptext, padding, i * vspacing + padding);
 				g2.drawString(h.key, spacing + padding, i * vspacing + padding);
 				i++;
 			}
 
 			g2.setColor(Color.GRAY);
 			g.drawString(String.format("Version %s", VersionName.version), padding, getHeight() - padding);
 
 		}
 
 	}
 	class HelpItem {
 		/**
 		 * The buttons(s) that cause some action.
 		 */
 		public String key;
 
 		/**
 		 * The action the buttons cause.
 		 */
 		public String helptext;
 
 		/**
 		 * Generates a new HelpItem.
 		 */
 		public HelpItem(String key, String helptext) {
 			this.key = key;
 			this.helptext = helptext;
 		}
 
 
 	}
 
 	/**
 	 * Sets whether the help dialog is displayed.
 	 */
 	public void setShowHelp(boolean showHelp) {
 		this.showHelp = showHelp;
 	}
 }
