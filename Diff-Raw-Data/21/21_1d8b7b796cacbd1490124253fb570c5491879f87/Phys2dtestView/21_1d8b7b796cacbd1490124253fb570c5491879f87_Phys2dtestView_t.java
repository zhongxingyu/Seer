 package stijgmachine.jti1a1.nl.view;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 
 import javax.swing.*;
 import javax.swing.Timer;
 
 import net.phys2d.math.ROVector2f;
 import net.phys2d.math.Vector2f;
 import net.phys2d.raw.Body;
 import net.phys2d.raw.StaticBody;
 import net.phys2d.raw.World;
 import net.phys2d.raw.shapes.Box;
 import net.phys2d.raw.shapes.Circle;
 import net.phys2d.raw.shapes.ConvexPolygon;
 import net.phys2d.raw.shapes.DynamicShape;
 import net.phys2d.raw.shapes.Line;
 import net.phys2d.raw.shapes.Polygon;
 import net.phys2d.raw.strategies.QuadSpaceStrategy;
 
 import java.util.*;
 
 
 public class Phys2dtestView extends MiniGameView implements MouseListener,MouseMotionListener
 {
 	
 	private Timer time1;
 	
 	public Phys2dtestView()
 	{
 		makeFrame();
 	}
 	
 	public void makeFrame()
 	{
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		
 		
 		
 		Timer time = new Timer(1000/30,new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				//System.out.println("X: " + cirkel.getPosition().getX() + "Y: " + cirkel.getPosition().getY());
 				repaint();
 			}
 		});
 		time.start();
 		
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D)g;
 		
 		g2.setStroke(new BasicStroke(2.0f));
//		
//		makeLinePreview();
//		drawLinePreview(g2);
//		makeLine2();
		//drawLines(g2);
		//drawCircle(g2, cirkel);
 		drawObjects(g2);
 	}
 	
 	public void drawObjects(Graphics2D g2)
 	{
 		g2.drawLine(650, 650, 650, 700);
 		g2.drawLine(650, 700, 700, 700);		//Goal Basket
 		g2.drawLine(700,700,700,650);
 		
 		g2.draw(new Rectangle2D.Double(400,0,10,300));
 		g2.draw(new Rectangle2D.Double(200,400,10,300));
 		g2.draw(new Rectangle2D.Double(500,400,10,300));
 		
 	}
 	
 	
 	
 	
 	
	/*public void drawLines(Graphics2D g2)
 	{
 		for(int i = 0; i < lines.size();i++)
 		{
 			g2.draw(lines.get(i));
 		}
 	}
 	
 	public void drawCircle(Graphics2D g2, Body b)
 	{
     	Circle circle = (Circle) b.getShape();
     	initPoints(circle.getRadius(),b.getPosition().getX(),b.getPosition().getY());
 
         GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
         for(int j = 0; j < points2.length; j++)
         {
             if(j == 0)
                 path.moveTo(points2[j].x, points2[j].y);
             else
                 path.lineTo(points2[j].x, points2[j].y);
         }
         path.closePath();
         
         AffineTransform tx = new AffineTransform();
//        g2.fill(tx.createTransformedShape(path));
 	}
	*/
 	@Override
 	public void mouseDragged(MouseEvent event) 
 	{
 		/*if(pressed == true)
 		{
 			points.add(event.getPoint());
 		}*/
 		
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
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
 	public void mousePressed(MouseEvent event) 
 	{
 		
 		/*if(event.getButton() == event.BUTTON1)
 		{
 			pressed = true;
 		}
 		if(event.getButton() == event.BUTTON3)
 		{
 			addBox();
 			cirkel.setPosition(100.0f, 100.0f);
 			wachten();
 		}
 		if(event.getButton() == event.BUTTON2)
 		{
 			removeAllLines();
 		}*/
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent event) 
 	{
 		if(event.getButton() == event.BUTTON1)
 		{
 			//pressed = false;
 		}
 		
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 }
