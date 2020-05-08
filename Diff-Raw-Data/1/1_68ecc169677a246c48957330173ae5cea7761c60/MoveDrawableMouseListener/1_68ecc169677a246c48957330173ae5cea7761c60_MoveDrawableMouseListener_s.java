 package com.graphics.java.Model;
 
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import com.graphics.java.Interfaces.IDrawable;
 import com.graphics.java.Interfaces.IMovableDrawable;
 
 public class MoveDrawableMouseListener extends JCanvasMouseAdapter {
 
 	protected IMovableDrawable drawable;
 	
 	public MoveDrawableMouseListener(JCanvas canvas) {
 		super(canvas);
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		System.out.println("Mouse dragged");
 		if(drawable != null){
 			drawable.setPosition(e.getPoint());
 			canvas.repaint();
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		List<IDrawable> selectedDrawables = canvas.findDrawables(e.getPoint());
 		if(selectedDrawables.size() == 0) return;
 		drawable = (IMovableDrawable) selectedDrawables.get(0);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		drawable = null;
 	}
 	
 	
 
 }
