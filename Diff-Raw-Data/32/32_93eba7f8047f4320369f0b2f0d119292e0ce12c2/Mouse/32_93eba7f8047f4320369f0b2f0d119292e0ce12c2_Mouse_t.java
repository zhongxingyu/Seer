 package listeners;
 
 import imgMain.JxReader;
 import imgMain.controls.JxMenuBar;
 
 import java.awt.Container;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JLabel;
 
 public class Mouse implements MouseListener, MouseMotionListener {
 
 	private static JxReader jxReader;
 	  
 	public Mouse(JxReader reader) {
 		jxReader = reader;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		jxReader.repaint();
		if (arg0.getSource() == JxReader.pnlImage){
 			if (arg0.getButton() == 1){
 				jxReader.pageRouter(1);
 			}else if (arg0.getButton() == 3){
 				jxReader.pageRouter(2);
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		
 		Object source = arg0.getSource();
 		if (source instanceof JLabel){
 			JLabel label = (JLabel) source;
 			String command = label.getName();
 			if (command.equals("Exit")){
 				JxMenuBar.setCloseImage("/images/close_focused_pressed.png");
 			}else if (command.equals("Fullscreen")){
 				JxMenuBar.setMaxImage("/images/maximize_focused_pressed.png");
 			}else if (command.equals("Minimize")){
 				JxMenuBar.setMinImage("/images/minimize_focused_pressed.png");
 			}
 		}
 		jxReader.repaint();
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		
 		JxMenuBar.setCloseImage("/images/close_focused_prelight.png");
 		JxMenuBar.setMaxImage("/images/maximize_focused_prelight.png");
 		JxMenuBar.setMinImage("/images/minimize_focused_prelight.png");
 		jxReader.repaint();
 		
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		if (arg0.getSource() instanceof JxMenuBar){
 			Point comp = MouseInfo.getPointerInfo().getLocation();
 			//Point scr = jxReader.getFrame().getLocationOnScreen();
 			//Point scr2 = arg0.getLocationOnScreen();
 			
 			int x = comp.x;// - scr.x + scr2.x;
 			int y = comp.y;// - scr.y + scr2.y;
 			//System.out.println("x" + x);
 			//System.out.println("y" + y);
 			jxReader.getFrame().setLocation(x, y);
 		}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 		Container c = jxReader.getFrame().getContentPane();
 		if (c.getCursor() == JxReader.blankCursor){
 			c.setCursor(JxReader.defaultCursor);
 			jxReader.repaint();
 		}
 	}
 
 }
