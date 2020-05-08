 package com.denisneuling.binarytree.gui.component;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.Serializable;
 
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import com.denisneuling.binarytree.model.BinaryTree;
 import com.denisneuling.binarytree.model.Node;
 import com.denisneuling.binarytree.service.BinaryTreeService;
 
 @Component
 public class BinaryTreeEditorPane extends JPanel implements InitializingBean, MouseListener {
 	private static final long serialVersionUID = -381553764719490920L;
 	protected Logger log = Logger.getLogger(this.getClass());
 
 	@Value("${defaults.editor.size.x}")
 	private String sizeX = "400";
 
 	@Value("${defaults.editor.size.y}")
 	private String sizeY = "400";
 
 	@Autowired
 	private PopupMenu popupMenu;
 
 	@Autowired
 	private BinaryTreeService binaryTreeService;
 
 	private int x;
 	private int y;
 
 	public BinaryTreeEditorPane() {
 	}
 
 	public BinaryTree getBinaryTree() {
 		return binaryTreeService.getBinaryTree();
 	}
 
 	public void setBinaryTree(BinaryTree binaryTree) {
 		binaryTreeService.setBinaryTree(binaryTree);
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		x = Integer.parseInt(sizeX);
 		y = Integer.parseInt(sizeY);
 
 		this.setMinimumSize(new Dimension(x, y));
 
 		addMouseListener(this);
 
 		setBackground(Color.white);
 		setForeground(Color.black);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		log.debug("Mouse clicked: " + arg0);
 
 		if (arg0.getButton() == MouseEvent.BUTTON3) {
 			popupMenu.relocate(arg0.getPoint());
 			popupMenu.setVisible(true);
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		log.debug("Mouse pressed: " + arg0);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		log.debug("Mouse released: " + arg0);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		log.debug("Mouse entered: " + arg0);
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		log.debug("Mouse exited: " + arg0);
 	}
 
 	protected void paintComponent(Graphics g) {
 		g.setColor(getBackground()); // colors the window
 		g.fillRect(0, 0, getWidth(), getHeight());
 		g.setColor(getForeground()); // set color and fonts
 		Font MyFont = new Font("SansSerif", Font.PLAIN, 10);
 		g.setFont(MyFont);
		int xs = 10; // where to start printing on the panel
 		int ys = 20;
		g.drawString("Binary Search tree for the input string:\n", xs, ys);
 		ys = ys + 10;
 		// int start = 0;
 		// print input string on panel, 150 chars per line
 		// // if string longer than 23 lines don't print
 		// if (t.inputString.length() < 23 * 150) {
 		// while ((t.inputString.length() - start) > 150) {
 		// g.drawString(t.inputString.substring(start, start + 150), xs, ys);
 		// start += 151;
 		// ys += 15;
 		// }
 		// g.drawString(t.inputString.substring(start, t.inputString.length()),
 		// xs, ys);
 		// }
 		MyFont = new Font("SansSerif", Font.BOLD, 20); // bigger font for tree
 		g.setFont(MyFont);
 		this.drawTree(g, binaryTreeService.getBinaryTree().getNode()); // draw
 																		// the
 																		// tree
 		revalidate(); // update the component panel
 	}
 
 	public void drawTree(Graphics g, Node root) {// actually draws the tree
 		int dx, dy, dx2, dy2;
 		int SCREEN_WIDTH = x; // screen size for panel
 		int SCREEN_HEIGHT = y;
 		int XSCALE, YSCALE;
 
 		int totalNodes = binaryTreeService.getTotalNodes();
 		int maxDepth = binaryTreeService.getDepth();
 		XSCALE = SCREEN_WIDTH / totalNodes;
 		YSCALE = (SCREEN_HEIGHT - 0) / (maxDepth + 1);
 
 		if (root != null) { // inorder traversal to draw each node
 			drawTree(g, root.getChildL()); // do left side of inorder traversal
 			dx = root.getPosx() * XSCALE; // get x,y coords., and scale them
 			dy = root.getPosy() * YSCALE + 20;
 			Serializable s = root.getValue(); // get the word at this
 														// node
 			g.drawString((s!=null?s.toString():""), dx, dy); // draws the word
 			// this draws the lines from a node to its children, if any
 			if (root.getChildL() != null) { // draws the line to left child if
 											// it
 				// exists
 				dx2 = root.getChildL().getPosx() * XSCALE;
 				dy2 = root.getChildL().getPosy() * YSCALE + 20;
 				g.drawLine(dx, dy, dx2, dy2);
 			}
 			if (root.getChildR() != null) { // draws the line to right child if
 											// it
 				// exists
 				dx2 = root.getChildR().getPosx() * XSCALE;// get right child x,y
 															// scaled
 				// position
 				dy2 = root.getChildR().getPosy() * YSCALE + 20;
 				g.drawLine(dx, dy, dx2, dy2);
 			}
 			drawTree(g, root.getChildR()); // now do right side of inorder
 											// traversal
 		}
 	}
 }
