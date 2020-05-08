 package draw5;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 
 import scribble3.ScribbleCanvas;
 import draw1.TwoEndsShape;
 
 public class PolygonShapeTool extends scribble3.AbstractTool implements PolygonTool{
 	protected int x[], y[];
 	protected int index = 0;
 	protected PolygonShape curPolygon = new PolygonShape();
 	
 	public PolygonShapeTool(ScribbleCanvas canvas, String name, String tipText) {
 		super(canvas, name, tipText);
 	}
 	
 	public void addPointToArray(Point p){
 		Graphics g = canvas.getGraphics();
 		g.setXORMode(Color.darkGray);
 		g.setColor(Color.lightGray);
		x[index] = p.x;
		y[index] = p.y;
		curPolygon.drawOutline(g, x, y);
 		index++;
 	}
 	
 	public void endArray(Point p){
 		//close the polygon
		canvas.mouseButtonDown = false;
 		Graphics g = canvas.getGraphics();
 		g.setXORMode(Color.darkGray);
 		g.setColor(Color.lightGray);
 		curPolygon.setColor(canvas.getCurColor());
 		curPolygon.setX(x);
 		curPolygon.setY(y);
 		canvas.addShape(curPolygon);
 	    g.setPaintMode();
 	    canvas.repaint();
 	}
 	
 	public void startShape(Point p){}		
 	public void addPointToShape(Point p){}
 	public void endShape(Point p){}
 }
