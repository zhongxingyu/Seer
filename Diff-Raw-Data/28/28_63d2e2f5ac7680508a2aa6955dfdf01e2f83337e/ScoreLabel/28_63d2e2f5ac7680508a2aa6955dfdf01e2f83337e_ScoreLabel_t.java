 package de.phaenovum.view;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import de.phaenovum.controller.MainController;
 
 public class ScoreLabel extends JLabel implements MouseListener{
 	private SlListener listener;
 	private int id;
 	private boolean selected = false;
 	private boolean out = false;
 	private Color color;
 	
 	public ScoreLabel(int id, String text,Color color){
 		super(text);
		this.setText(text);
 		// Change Color
 		this.color = color;
 		this.setOpaque(true);
 		this.setBackground(color);
 		// Change font
 		this.setFont(new Font("Arial",Font.PLAIN,20));
 		this.setHorizontalAlignment(SwingConstants.CENTER);
 		this.id = id;
 		this.addMouseListener(this);
 	}
 	
 	public void setSlListener(SlListener listener){
 		this.listener = listener;
 	}
 	
 	public void drawBackground(){
 		this.setBackground(color);
 		if(selected) this.setBackground(Color.GREEN);
 		if(out) this.setBackground(Color.RED);
 		this.repaint();
 	}
 	
 	public void setSelected(boolean selected){
 		this.selected = selected;
 		drawBackground();
 	}
 	
 	public void setOut(boolean out){
 		this.out = out;
 		drawBackground();
 	}
 	
 	
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		//System.out.println("Mouse Clicked on LAbel");
 		if(listener != null) listener.slAction(id);
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e) {}
 	@Override
 	public void mouseReleased(MouseEvent e) {}
 	@Override
 	public void mouseEntered(MouseEvent e) {}
 	@Override
 	public void mouseExited(MouseEvent e) {}
 	
 	public interface SlListener{
 		public void slAction(int id);
 	}
 }
