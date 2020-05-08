 package gui;
 
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Observable;
 import java.util.Observer;
 
 
 public class SlotLabel extends ColoredLabel implements MouseListener, Observer{
 	private String slotName;
 	private CurrentSlot currentSlot;
 	
     public SlotLabel(String name, CurrentSlot currentSlot) {
         super("                    ", Color.WHITE, RIGHT);
         slotName = name;
         this.currentSlot = currentSlot;
         addMouseListener(this);
         
     }
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		
 		
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
 	public void mousePressed(MouseEvent MOUSE_PRESSED) {
		setBackground(Color.YELLOW);
 		currentSlot.set(this);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public String name() {
 		return slotName;
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
		setBackground(Color.WHITE);
		currentSlot.deleteObserver(this);
 		
 	}
     
 
 }
