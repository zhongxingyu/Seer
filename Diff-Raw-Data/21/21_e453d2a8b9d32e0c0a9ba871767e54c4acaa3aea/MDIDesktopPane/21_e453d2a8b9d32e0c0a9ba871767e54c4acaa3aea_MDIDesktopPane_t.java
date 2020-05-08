 package org.codemonkeyism.mdidesktop;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.beans.PropertyVetoException;
 
 import javax.swing.JDesktopPane;
 import javax.swing.JInternalFrame;
 
 /**
  * An extension of JDesktopPane that supports often used MDI functionality. This
  * class also handles setting scroll bars for when windows move too far to the
  * left or bottom, providing the MDIDesktopPane is in a ScrollPane.
  * 
  * @author Dean
  */
 public class MDIDesktopPane extends JDesktopPane {
 
 	private static final long serialVersionUID = 1L;
 
 	private static int FRAME_OFFSET = 20;
 	private MDIDesktopManager manager;
 
 	public MDIDesktopPane() {
 		manager = new MDIDesktopManager(this);
 		setDesktopManager(manager);
 	}
 
 	/**
 	 * Set the bounds
 	 */
 	public void setBounds(int x, int y, int w, int h) {
 		super.setBounds(x, y, w, h);
 		checkDesktopSize();
 	}
 
 	@Override
 	protected void addImpl(Component comp, Object constraints, int index) {
 		super.addImpl(comp, constraints, index);
 		checkDesktopSize();
 	}
 
 	/**
 	 * Add an internal frame to this desktop
 	 * 
 	 * @param frame
 	 * @return
 	 */
 	public Component add(JInternalFrame frame) {
 		JInternalFrame[] array = getAllFrames();
 		Point p;
 
 		Component retval = super.add(frame);
 
 		if (array.length > 0) {
 			p = array[0].getLocation();
 			p.x = p.x + FRAME_OFFSET;
 			p.y = p.y + FRAME_OFFSET;
 		} else {
 			p = new Point(0, 0);
 		}
 		frame.setLocation(p.x, p.y);
 		moveToFront(frame);
 		frame.setVisible(true);
 		try {
 			frame.setSelected(true);
 		} catch (PropertyVetoException e) {
 			frame.toBack();
 		}
 		checkDesktopSize();
 		return retval;
 	}
 
 	/**
 	 * Remove a component
 	 */
 	public void remove(Component c) {
 		super.remove(c);
 		checkDesktopSize();
 	}
 
 	/**
 	 * Cascade all internal frames
 	 */
 	public void cascadeFrames() {
 		int x = 0;
 		int y = 0;
		JInternalFrame[] allFrames = getAllFrames();
 
 		manager.setNormalSize();
 		// FRAME_OFFSET;
 		for (int i = allFrames.length - 1; i >= 0; i--) {
 			allFrames[i].setLocation(x, y);
 			x = x + FRAME_OFFSET;
 			y = y + FRAME_OFFSET;
 		}
 	}
 
 	/**
 	 * Tile all internal frames
 	 */
 	public void tileFrames() {
		java.awt.Component[] allFrames = getAllFrames();
		if (allFrames.length > 0) {
			manager.setNormalSize();
			int frameHeight = getBounds().height / allFrames.length;
			int y = 0;
			for (int i = 0; i < allFrames.length; i++) {
				allFrames[i].setSize(getBounds().width, frameHeight);
				allFrames[i].setLocation(0, y);
				y = y + frameHeight;
			}
 		}
 	}
 
 	/**
 	 * Sets all component size properties ( maximum, minimum, preferred) to the
 	 * given dimension.
 	 */
 	public void setAllSize(Dimension d) {
 		setMinimumSize(d);
 		setMaximumSize(d);
 		setPreferredSize(d);
 	}
 
 	/**
 	 * Sets all component size properties ( maximum, minimum, preferred) to the
 	 * given width and height.
 	 */
 	public void setAllSize(int width, int height) {
 		setAllSize(new Dimension(width, height));
 	}
 
 	/**
 	 * Check the current desktop size. If we have a parent and are visible then
 	 * calculate the correct size
 	 */
 	protected void checkDesktopSize() {
 		if (getParent() != null && isVisible()) {
 			manager.resizeDesktop();
 		}
 	}
 }
