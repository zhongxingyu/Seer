 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class Block extends JPanel {
 
 	private int x, y;
 	private final int width, height;
 	private boolean orientation;
 	public static final boolean HORIZONTAL = true;
 	public static final boolean VERTICAL   = false;
 	private JLabel lbl;
 
 	/**
 	 * Creates a block
 	 * @param x				The x coordinate where the block is.
 	 * @param y				The y coordinate where the block is.
 	 * @param length		The size of the block (2, 3 mainly).
 	 * @param orientation	The orientation of the block (Horizontal or Vertical).
 	 * @param c				The character associated to this block.
 	 */
 	public Block(int x, int y, int length, boolean orientation, char c) {
 		this.x = x;
 		this.y = y;
 		this.orientation = orientation;
 		this.width = (orientation == HORIZONTAL)? 100 * length - 1 :
 			100 - 1;
 		this.height = (orientation == HORIZONTAL)? 100 - 1 :
 			100 * length - 1;
 		setBounds(x, y, width, height);
 		setPreferredSize(new Dimension(width, height));
 		setBackground(new Color(random(), random(), random()));
 		lbl = new JLabel(c+"");
 		add(lbl);
 	}
 
 	/**
 	 * Changes the block color.
 	 * @param color	The new color.
 	 */
 	public void setColor(Color color) {
 		setBackground(color);
 		lbl.setBackground(color);
 	}
 
 	/**
 	 * Generates a random number r such that
 	 * 0 <= r <= 255
	 * @return	Random integer in range [0, 255]
 	 */
 	private int random() {
 		return (int)(Math.random() * 256);
 	}
 
 	/**
 	 * Move block
 	 * @param Pixels to be moved (positive -> up, right,
 	 * 							  negative -> left, bottom)
 	 */
 	public void move(int d) {
 		try {
 			if (orientation == HORIZONTAL)
				for (int i = 0; i <= d; i++) {
					setBounds(x + i, y, width, height);
 					Thread.sleep(10);
 				}
 			else
				for (int i = 0; i <= d; i++) {
					setBounds(x, y + i, width, height);
 					Thread.sleep(10);
 				}
 		} catch(InterruptedException e) {}
 	}
 
 }
