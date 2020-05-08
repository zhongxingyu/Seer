 package wombat.scheme.libraries.types;
 
 import java.awt.Color;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.math.BigInteger;
 import java.util.Stack;
 
 public class TreeData {
 	boolean isEmpty = false;
 	public String Value;
 	public Object Left;
 	public Object Right;
 
 	/**
 	 * Create an empty tree.
 	 */
 	public TreeData() {
 	}
 	
 	/**
 	 * Take a tree that looks like this:
 	 * #[tree 3 #[tree 1 #[empty-tree]] #[empty-tree]]
 	 * and build a nested tree structure.
 	 * 
 	 * @param tr A string representing the tree.
 	 * @return
 	 */
 	public static TreeData decode(String tr) {
 		TreeData last = null;
 		Stack<TreeData> lumber = new Stack<TreeData>();
 		
 		for (String line : tr.split("\n")) {
 			TreeData top = (lumber.isEmpty() ? null : lumber.peek());
 			Object thing;
 			
 			if (line.startsWith("#!tree")) {
 				TreeData td = new TreeData();
 				td.Value = line.split(" ", 2)[1];
 				lumber.push(td);
 				thing = td;
 			} else if (line.startsWith("#!empty-tree")) {
 				TreeData td = new TreeData();
 				td.isEmpty = true;
 				thing = td;
 			} else {
 				thing = line;
 			}
 			
 			if (top != null && top.Left == null) {
 				top.Left = thing;
 			} else if (top != null && top.Right == null) {
 				top.Right = thing;
 			}
 			
 			while (!lumber.isEmpty() && lumber.peek().Left != null && lumber.peek().Right != null)
 				last = lumber.pop();
 		}
 		
 		return last;
 	}
 
 	/**
 	 * Create a leaf (no trees).
 	 * @param value The leaf's value.
 	 */
 	public TreeData(String value) {
 		Value = value;
 		Left = new TreeData();
 		Right = new TreeData();
 	}
 
 	/**
 	 * Create a new leaf with possible tress.
 	 * @param value The leaf's value.
 	 * @param left Left tree.
 	 * @param right Right tree.
 	 */
 	public TreeData(String value, TreeData left, TreeData right) {
 		Value = value;
 		Left = left;
 		Right = right;
 	}
 
 	/**
 	 * Convert a TreeData to a string.
 	 */
 	public String toString() {
 		if (Value == null) return "[empty-TreeData]";
 		else if (Left == null && Right == null) return "[leaf " + Value + "]";
 		else return "[TreeData " + Value + " " + Left + " " + Right + "]";
 	}
 
 	/**
 	 * Draw a TreeData.
 	 * @return An image containing the TreeData.
 	 */
 	public void drawTreeData(Graphics2D g, int width, int height) {
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, width, height);
 
 		g.setColor(Color.BLACK);
 		int h = height();
 		int w = new BigInteger("2").pow(h).intValue();
 
 		draw(g, width / 2, height / (h + 1), width / (w + 1), width - width / (w + 1),  height / (h + 1));
 	}
 
 	private void draw(Graphics2D g, int x, int y, int left, int right, int skip) {
 		if (Value == null) return;
 
 		// Calculate a weighted midpoint so that off balance TreeDatas will draw pleasantly.
 		int leftSize = 1 + (Left instanceof TreeData ? ((TreeData) Left).size() : 1);
 		int rightSize = 1 + (Right instanceof TreeData ? ((TreeData) Right).size() : 1);
 		double leftWeight = (double) leftSize / ((double) leftSize + (double) rightSize);
 		int mid = left + (int) ((double) (right - left) * leftWeight);
 
 		// Calculate the view points for the left and right subTreeDatas (yes, even if they won't be drawn)
 		int leftX = (left + mid) / 2;
 		int rightX = (mid + right) / 2;
 
 		// Left subTreeData.
 		if (Left != null && (!((Left instanceof TreeData) && ((TreeData) Left).isEmpty))) {
 			g.drawLine(x, y, leftX, y + skip);
 			if (Left instanceof TreeData) {
 				((TreeData) Left).draw(g, leftX, y + skip, left, mid, skip);
 			} else {
 				drawThing(Left, g, leftX, y + skip, false);
 			}
 		}
 
 		// Right subTreeData.
 		if (Right != null && (!((Right instanceof TreeData) && ((TreeData) Right).isEmpty))) {
 			g.drawLine(x, y, rightX, y + skip);
 			if (Right instanceof TreeData) {
 				((TreeData) Right).draw(g, rightX, y + skip, mid, right, skip);
 			} else {
 				drawThing(Right, g, rightX, y + skip, false);
 			}
 		}
 
 		// My node.
 		drawThing(Value, g, x, y, true);
 	}
 	
 	/**
 	 * Split this out to either draw my node or non-tree children.
 	 * @param thing
 	 * @param g
 	 * @param x
 	 * @param y
 	 */
 	private void drawThing(Object thing, Graphics2D g, int x, int y, boolean isNode) {
 		String s = thing.toString();
 		FontMetrics fm = g.getFontMetrics();
 		int w = fm.stringWidth(s);
 
 		// This would be much cleaner with first order functions. :)
 		g.setColor(Color.WHITE);
 		if (isNode)
 			g.fillOval(x - w / 2 - 5, y - 10, w + 10, 20);
		else
			g.fillRect(x - w / 2 - 5, y - 10, w + 10, 20);
 		g.setColor(Color.BLACK);
 		if (isNode)
 			g.drawOval(x - w / 2 - 5, y - 10, w + 10, 20);
		else
			g.drawRect(x - w / 2 - 5, y - 10, w + 10, 20);
 
 		g.drawString(thing.toString(), x - fm.stringWidth(s) / 2, y + fm.getAscent() / 2);
 	}
 
 	/**
 	 * Calculate the height of the TreeData (empty TreeData is 0, leaf is 1).
 	 * @return The height of the TreeData.
 	 */
 	private int height() {
 		if (Value == null) return 0;
 		else return 1 + Math.max(
 				(Left instanceof TreeData ? ((TreeData) Left).height() : 1),
 				(Right instanceof TreeData ? ((TreeData) Right).height() : 1));
 	}
 
 	/**
 	 * Count this node plus all its children.
 	 * @return The number of nodes.
 	 */
 	private int size() {
 		if (Value == null) return 0;
 		else return 1 
 				+ (Left instanceof TreeData ? ((TreeData) Left).size() : 1)
 				+ (Right instanceof TreeData ? ((TreeData) Right).size() : 1);
 	}
 }
