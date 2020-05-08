 package com.graphics.java.Model;
 
import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import com.graphics.java.Interfaces.IDrawable;
 
 public class JCanvas extends JPanel{
 
 	private static final long serialVersionUID = 1L;
 	/**
 	 * 
 	 */
 	
 	private List<IDrawable> drawables = new LinkedList<IDrawable>();
 	
 
 	@Override
 	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		for(Iterator<IDrawable> iter = drawables.iterator(); iter.hasNext();){
 			IDrawable d = (IDrawable) iter.next();
 			d.draw(g);
 		}
 	}
 	
 	public void addDrawable(IDrawable d){
 		drawables.add(d);
 		repaint();
 	}
 	
 	public void removeDrawable(IDrawable d){
 		drawables.remove(d);
 		repaint();
 	}
 	
 	public void clear(){
 		drawables.clear();
 		repaint();
 	}
 	
 	public List<IDrawable> findDrawables(Point p){
 		List<IDrawable> l = new ArrayList<IDrawable>();
 		
 		for(Iterator<IDrawable> iter = drawables.iterator(); iter.hasNext();){
 			IDrawable element = (IDrawable) iter.next();
 			if(element.getRectangle().contains(p)){
 				l.add(element);
 			}
 		}		
 		return l;
 	}
 	
 	public boolean isFree(Rectangle rect){
 		for(Iterator<IDrawable> iter = drawables.iterator(); iter.hasNext();){
 			IDrawable element = (IDrawable) iter.next();
 			if(element.getRectangle().intersects(rect)){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean isAlone(IDrawable drawable){
 		Rectangle rect = drawable.getRectangle();
 		for(Iterator<IDrawable> iter = drawables.iterator(); iter.hasNext();){
 			IDrawable element = (IDrawable) iter.next();
 			if(!element.equals(drawable) && element.getRectangle().intersects(rect)){
 				return false;
 			}
 		}
 		return true;
 	}
 }
