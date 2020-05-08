 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.scheme.libraries;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
import javax.swing.*;
 
 import wombat.scheme.libraries.types.TreeData;
 
 /**
  * Access tree methods written in Java.
  */
 public class TreeAPI {
 	/**
 	 * Draw a tree.
 	 * @param tree The representation of the tree.
 	 */
 	public static void drawTree(final TreeData tree) {
		JFrame treeFrame = new JFrame("draw-tree");
		treeFrame.setSize(400, 400);
		treeFrame.setLayout(new BorderLayout());
		treeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		treeFrame.add(new JPanel() {
 			private static final long serialVersionUID = -6723179597582742419L;
 
 			@Override
 			public void paint(Graphics g) {
 				super.paint(g);
 				tree.drawTreeData((Graphics2D) g, getWidth(), getHeight());
 			}
		});
		
 		treeFrame.setVisible(true);
 	}
 }
