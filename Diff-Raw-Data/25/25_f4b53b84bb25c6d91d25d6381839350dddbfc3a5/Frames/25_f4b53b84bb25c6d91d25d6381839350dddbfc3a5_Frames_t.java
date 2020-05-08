 package mikera.gui;
 
 import java.util.HashMap;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 public class Frames {
 	
 	private static final HashMap<String,JFrame> frames=new HashMap<String, JFrame>();
 
 	
 	public static JFrame display(final JComponent component) {
 		JFrame f=new JFrame("Image popup");
 		f.add(component);
 		f.setVisible(true);
 		f.pack();
 		
 		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		return f;
 	}
 	
 	public static JFrame display(final JComponent component, String title) {
 		JFrame f=frames.get(title);
 		
 		if (f==null) {
 			f=new JFrame(title);
 			frames.put(title,f);
 			f.getContentPane().add(component);
 			f.setVisible(true);
 			f.pack();
 			
 			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		} else {
 			f.getContentPane().removeAll();
 			f.getContentPane().add(component);
			if (!f.isVisible()) {
				f.setVisible(true);
			} else {
				f.validate();
			}
 			f.repaint();
 		}
 				
 		return f;
 	}
 
 }
