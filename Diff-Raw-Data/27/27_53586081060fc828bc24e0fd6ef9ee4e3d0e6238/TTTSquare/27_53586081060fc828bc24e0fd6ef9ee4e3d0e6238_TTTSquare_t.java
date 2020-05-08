 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.border.BevelBorder;
 
 public class TTTSquare extends JPanel {
 
 	private MainFrame parent;
 	private boolean mouseIn;
 
 	private X_O owner;
 
 	public TTTSquare(MainFrame parent) {
 		addMouseListener(mouseListen);
 		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		setMinimumSize(new Dimension(10, 10));
 		setPreferredSize(new Dimension(100, 100));
 		this.parent = parent;
 		owner = X_O.NONE;
 	}
 
 	public X_O getOwner(){
 		return owner;
 	}
 	
 	protected void setOwner(X_O newOwner){
 		owner = newOwner;
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g;
 		if (mouseIn && owner == X_O.NONE) {
 			if (parent.getTurn() == X_O.O)
 				ghostO(g2);
 			else
 				ghostX(g2);
 
 		} else if (owner != X_O.NONE) {
 			if (owner == X_O.X)
 				drawX(g2);
 			else
 				drawO(g2);
 		}
 	}
 
 	private void drawO(Graphics2D g) {
 		Color color = g.getColor();
 		Stroke stroke = g.getStroke();
 		g.setColor(Color.RED);
 		g.setStroke(new BasicStroke(2));
 
 		g.drawOval(4, 4, getWidth() - 8, getHeight() - 8);
 
 		g.setColor(color);
 		g.setStroke(stroke);
 	}
 
 	private void drawX(Graphics2D g) {
 		Color color = g.getColor();
 		Stroke stroke = g.getStroke();
 		g.setColor(Color.BLUE);
 		g.setStroke(new BasicStroke(2));
 		
 		g.drawLine(0, 0, getWidth(), getHeight());
 		g.drawLine(0, getHeight(), getWidth(), 0);
 
 		g.setColor(color);
 		g.setStroke(stroke);
 	}
 
 	private void ghostO(Graphics2D g) {
 		Color color = g.getColor();
 		Stroke stroke = g.getStroke();
 		g.setColor(new Color(255, 157, 157));
 		g.setStroke(new BasicStroke(2));
 
 		g.drawOval(4, 4, getWidth() - 8, getHeight() - 8);
 
 		g.setColor(color);
 		g.setStroke(stroke);
 	}
 
 	private void ghostX(Graphics2D g) {
 		Color color = g.getColor();
 		Stroke stroke = g.getStroke();
 		g.setColor(new Color(157, 157, 255));
 		g.setStroke(new BasicStroke(2));
 		
 		g.drawLine(0, 0, getWidth(), getHeight());
 		g.drawLine(0, getHeight(), getWidth(), 0);
 
 		g.setColor(color);
 		g.setStroke(stroke);
 	}
 
 	private MouseListener mouseListen = new MouseAdapter() {
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			if (owner != X_O.NONE)
 				return;
 			if (parent.getTurn() == X_O.X) {
 				setOwner(X_O.X);
 			} else {
 				setOwner(X_O.O);
 			}
 			parent.changeTurn();
 			repaint();
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			mouseIn = true;
 			repaint();
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			mouseIn = false;
 			repaint();
 		}
 	};
 }
