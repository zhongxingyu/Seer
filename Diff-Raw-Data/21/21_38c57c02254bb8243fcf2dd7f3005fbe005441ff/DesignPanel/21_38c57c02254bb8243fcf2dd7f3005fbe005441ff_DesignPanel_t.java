 package meew0.ld;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import meew0.ld.level.Level;
 
 public class DesignPanel extends JPanel implements MouseListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public Level l;
 	int ps;
 
 	boolean showGrid = true;
 
 	List<DesignPanelListener> listeners;
 
 	public DesignPanel(Level level) {
 		super(true);
 
 		this.addMouseListener(this);
 
 		listeners = new ArrayList<DesignPanelListener>();
 
 		this.setSize(level.getWidth() * level.getPixelSize(), level.getHeight()
 				* level.getPixelSize());
 
 		ps = level.getPixelSize();
 		l = level;
 	}
 
 	public void registerPanelListener(DesignPanelListener l) {
 		listeners.add(l);
 	}
 
 	@Override
	protected void paintComponent(Graphics g) { // Invalidation
 		System.out.println("Redrawing panel");
 		for (int i = 0; i < l.getWidth(); i++) {
 			for (int j = 0; j < l.getHeight(); j++) {
 				g.setColor(l.getColorAt(i, j));
 				for (int k = 0; k < ps; k++) {
 					for (int m = 0; m < ps; m++) {
 						g.drawLine(j * ps + m, i * ps + k, j * ps + m, i * ps
 								+ k);
 					}
 				}
 				// g.drawLine((i - 1) * ps - 1, j * ps - 1, i * ps - 1, j * ps -
 				// 1);r
 				// g.drawLine(i * ps - 1, (j - 1) * ps - 1, i * ps - 1, j * ps -
 //				// 1);
 //				System.out.println("" + i + " " + j);
 			}
 		}
 
 		for (int i = 0; i < l.getWidth(); i++) {
 			for (int j = 0; j < l.getHeight(); j++) {
 				g.setColor(Color.black);
 				g.drawLine(i * ps - 1, j * ps - 1, (i + 1) * ps - 1, j * ps - 1);
 				g.drawLine(i * ps - 1, j * ps - 1, i * ps - 1, (j + 1) * ps - 1);
 			}
 		}
 		System.out.println("--------------");
 	}
 
 	private void callPanelClick(int x, int y) {
 		for (DesignPanelListener listener : listeners) {
 			listener.onPanelClick(x, y);
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		System.out.println("Mouse click");
 		int x = e.getX();
 		int y = e.getY();
 
 		double pX = x / ps;
 		double pY = y / ps;
 
 		callPanelClick((int) pX, (int) pY);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
