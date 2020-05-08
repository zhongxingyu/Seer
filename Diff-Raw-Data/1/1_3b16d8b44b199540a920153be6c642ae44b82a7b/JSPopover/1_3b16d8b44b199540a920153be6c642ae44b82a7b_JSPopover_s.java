 package js.incomplete;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Polygon;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class JSPopover extends JFrame {
 	
 	public static final int TOP = 0;
 	public static final int LEFT = 1;
 	
 	private Color strokeColor = Color.black;
 	private JPanel panel;
 	private int direction;
 	
 	public JSPopover() {
 		direction = TOP;
 		setLayout(null);
 		setUndecorated(true);
 		setBackground(new Color(0, 0, 0, 0));
 		setAlwaysOnTop(true);
 		setFocusableWindowState(false);
 		
 		panel = new JPanel();
 		panel.setBounds(10, 30, getWidth() - 20, getHeight() - 20);
 		panel.setBackground(Color.WHITE);
 		panel.setLayout(null);
 		add(panel);
 	}
 	
 	public JSPopover(int direction) {
 		setDirection(direction);
 		setLayout(null);
 		setUndecorated(true);
 		setBackground(new Color(0, 0, 0, 0));
 		setAlwaysOnTop(true);
 		setFocusableWindowState(false);
 		
 		panel = new JPanel();
 		if (direction == LEFT)
 			panel.setBounds(30, 10, getWidth() - 40, getHeight() - 20);
 		else
 			panel.setBounds(10, 30, getWidth() - 20, getHeight() - 40);
 		panel.setBackground(Color.WHITE);
 		panel.setLayout(null);
 		add(panel);
 	}
 	
 	public void setStrokeColor(Color c) {
 		strokeColor = c;
 		repaint();
 	}
 	
 	public void setContentBackground(Color c) {
 		panel.setBackground(c);
 	}
 	
 	public void paint(Graphics g) {
 		g.setColor(strokeColor);
 		
 		int width = getWidth();
 		int height = getHeight();
 		int middleH = height / 2;
 		int middleW = width / 2;
 		
 		if (direction == LEFT) {
 			int[] xPoints = {0, 20, 20, 0};
 			int[] yPoints = {middleH, middleH - 20, middleH + 20, middleH};
 			int nPoints = xPoints.length;
 			Polygon p = new Polygon(xPoints, yPoints, nPoints);
 			g.fillPolygon(p);
 			g.fillRoundRect(20, 0, width - 21, height - 1, 20, 20);
 		} else {
 			int[] xPoints = {middleW, middleW + 20, middleW - 20, middleW};
 			int[] yPoints = {0, 20, 20, 0};
 			int nPoints = xPoints.length;
 			Polygon p = new Polygon(xPoints, yPoints, nPoints);
 			g.fillPolygon(p);
 			g.fillRoundRect(0, 20, width - 1, height - 21, 20, 20);
 		}
 
 		if (direction == LEFT)
 			panel.setBounds(30, 10, getWidth() - 40, getHeight() - 20);
 		else
 			panel.setBounds(10, 30, getWidth() - 20, getHeight() - 40);
 		super.paint(g);
 	}
 	
 	public void setLocation(int x, int y) {
 		int width = getWidth();
 		int height = getHeight();
 		int middleH = height / 2;
 		int middleW = width / 2;
 		
 		if (direction == LEFT)
 			super.setLocation(x, y - middleH);
 		else
 			super.setLocation(x - middleW, y);
 	}
 	
 	public Component add(Component comp) {
 		if (comp != panel) {
 			return panel.add(comp);
 		} else {
 			return super.add(comp);
 		}
 	}
 	
 	public void setSize(int width, int height) {
 		remove(panel);
 		if (direction == LEFT) {
 			panel.setBounds(30, 10, width - 40, height - 20);
 			super.setSize(width + 20, height);
 		} else {
 			panel.setBounds(10, 30, getWidth() - 20, getHeight() - 40);
 			super.setSize(width, height + 20);
 		}
 		add(panel);
 	}
 	
 	public void setDirection(int direction) {
 		this.direction = direction;
 		repaint();
 	}
 
 }
