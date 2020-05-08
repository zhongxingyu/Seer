 package net.clonecomputers.lab.graphicsprog;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.lang.reflect.*;
 
 import javax.swing.*;
 
 /**all methods that can be use primitive drawing methods
  * this whole class is just to show that any API can be circumvented
  * the sad thing is there is NO WAY to prevent this
  * reflection allows you to access anything, even if it is private
  * the sneaky stuff is done in getCanvas and getDrawGrid
  */
 public class SneakyDrawer extends AbstractDrawer {
 	
 	public SneakyDrawer(DrawGrid dg){
 		super(dg);
 	}
 
 	private BufferedImage getCanvas() {
 		DrawGrid dg = getDrawGrid();
 		Class<? extends DrawGrid> dgClass = dg.getClass();
 		Field canvasField = null;
 		Field[] dgFields = dgClass.getDeclaredFields();
 		for(Field f: dgFields){
 			if(f.getType().isAssignableFrom(BufferedImage.class)) canvasField = f;
 		}
 		canvasField.setAccessible(true); // THIS is the sneaky part
 		BufferedImage canvas;
 		try {
 			canvas = (BufferedImage)canvasField.get(dg);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 		return canvas;
 	}
 	
 	private DrawGrid getDrawGrid() {
 		Class<? extends AbstractDrawer> cl = AbstractDrawer.class;
 		Field dgField = null;
 		Field[] clFields = cl.getDeclaredFields();
 		for(Field f: clFields){
 			if(f.getType().isAssignableFrom(DrawGrid.class)) dgField = f;
 		}
 		dgField.setAccessible(true); // THIS is the sneaky part
 		DrawGrid dg;
 		try {
 			dg = (DrawGrid)dgField.get(this);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 		return dg;
 	}
 	
 	private int xgp(double x){
 		DrawGrid dg = getDrawGrid();
 		Method[] ma = DrawGrid.class.getDeclaredMethods();
 		Method xgpMethod = null;
 		for(Method m: ma) if(m.getName().equals("xgp")) xgpMethod = m;
 		xgpMethod.setAccessible(true);
 		try {
 			return (Integer) xgpMethod.invoke(dg, x);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (InvocationTargetException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private int ygp(double y){
 		DrawGrid dg = getDrawGrid();
 		Method[] ma = DrawGrid.class.getDeclaredMethods();
 		Method ygpMethod = null;
 		for(Method m: ma) if(m.getName().equals("ygp")) ygpMethod = m;
 		ygpMethod.setAccessible(true);
 		try {
 			return (Integer) ygpMethod.invoke(dg, y);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (InvocationTargetException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private double zoom(){
 		DrawGrid dg = getDrawGrid();
 		Class<? extends DrawGrid> dgClass = dg.getClass();
 		Field zoomField = null;
 		Field[] dgFields = dgClass.getDeclaredFields();
 		for(Field f: dgFields){
 			if(f.getName().equals("zoom")) zoomField = f;
 		}
 		zoomField.setAccessible(true); // THIS is the sneaky part
 		double zoom;
 		try {
 			zoom = (Double) zoomField.get(dg);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 		return zoom;
 	}
 
 	public void beReallySneaky() {
 		Graphics g = getCanvas().getGraphics();
 		g.setFont(new Font("Zapfino", Font.PLAIN, 24));
 		g.setColor(Color.RED);
 		g.drawString("Gavin was really sneaky", 10, 40);
 		getDrawGrid().repaint();
 	}
 	
 	@Override
 	public void drawDot(double x, double y) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(Color.CYAN);
		g.fillRect(xgp(x-.5), ygp(y+.5), (int)zoom(), (int)zoom());
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void drawLine(double x1, double y1, double x2, double y2) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(Color.GREEN);
 		g.drawLine(xgp(x1), ygp(y1), xgp(x2), ygp(y2));
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void drawLine(double x1, double y1, double x2, double y2, Color c) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(c);
 		g.drawLine(xgp(x1), ygp(y1), xgp(x2), ygp(y2));
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void clear() {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(Color.DARK_GRAY);
 		g.fillRect(0, 0, getCanvas().getWidth(), getCanvas().getHeight());
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void drawCrossHair(double x, double y, double radius, double hole) {
 		JOptionPane.showMessageDialog(getDrawGrid(), "There is no primitive drawing method for crosshairs");
 	}
 
 	@Override
 	public void drawCrossHair(double x, double y, double radius, double hole,
 			Color c) {
 		JOptionPane.showMessageDialog(getDrawGrid(), "There is no primitive drawing method for crosshairs");
 	}
 
 	@Override
 	public void drawCircle(double ctrX, double ctrY, double radius) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(Color.BLUE);
 		g.drawOval(xgp(ctrX-radius), ygp(ctrY+radius),(int)(zoom()*2*radius), (int)(zoom()*2*radius));
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void drawCircle(double ctrX, double ctrY, double radius, Color c) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(c);
 		g.drawOval(xgp(ctrX-radius), ygp(ctrY+radius),(int)(zoom()*2*radius), (int)(zoom()*2*radius));
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void fillTriangle(double x1, double y1, double x2, double y2,
 			double x3, double y3) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(Color.MAGENTA);
 		g.fillPolygon(new int[]{xgp(x1),xgp(x2),xgp(x3)}, new int[]{ygp(y1),ygp(y2),ygp(y3)}, 3);
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void fillTriangle(double x1, double y1, double x2, double y2,
 			double x3, double y3, Color c) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(c);
 		g.fillPolygon(new int[]{xgp(x1),xgp(x2),xgp(x3)}, new int[]{ygp(y1),ygp(y2),ygp(y3)}, 3);
 		getDrawGrid().repaint();
 
 	}
 
 	@Override
 	public void fillTriangleUp(double leftX, double rightX, double leftY,
 			double topX, double topY) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(Color.PINK);
 		g.fillPolygon(new int[]{xgp(leftX),xgp(rightX),xgp(topX)}, new int[]{ygp(leftY),ygp(leftY),ygp(topY)}, 3);
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void fillTriangleDown(double leftX, double rightX, double leftY,
 			double botX, double botY) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(Color.ORANGE);
 		g.fillPolygon(new int[]{xgp(leftX),xgp(rightX),xgp(botX)}, new int[]{ygp(leftY),ygp(leftY),ygp(botY)}, 3);
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void drawRect(double x1, double y1, double x2, double y2) {
 		Graphics g = getCanvas().getGraphics();
 		g.setColor(Color.YELLOW);
 		g.drawRect(Math.min(xgp(x1),xgp(x2)), Math.min(ygp(y1),ygp(y2)),
 				Math.abs(xgp(x2)-xgp(x1)), Math.abs(ygp(x2)-ygp(y1)));
 		getDrawGrid().repaint();
 	}
 
 	@Override
 	public void drawCube(double x1, double y1, double radius) {
 		JOptionPane.showMessageDialog(getDrawGrid(), "There is no primitive drawing method for cubes");
 	}
 
 }
