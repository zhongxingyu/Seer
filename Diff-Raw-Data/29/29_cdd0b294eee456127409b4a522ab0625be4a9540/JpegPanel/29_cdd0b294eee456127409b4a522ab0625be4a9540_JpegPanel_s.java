 package rebecca.gui;
 
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 import rebecca.jpeg.Jpeg;
 
 public class JpegPanel extends JPanel {
 	private Jpeg j;
 	
 	public JpegPanel(Jpeg j) {
 		this.j = j;
 		this.setSize(16, 16);
 	}
 
 	
 	public void paint(Graphics g) {
 		
 	    super.paint(g);
 	    
	    for(int h = 0; h < 2; h++) {
 	    	for(int i = 0; i < 20; i++) {
 	    		j.mcu[i+h*20].paint(i*16,h*16,g);
 	    	}
 	    }
 
 	}
 	
 }
