 package application;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.GraphicsEnvironment;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 public class Display extends JFrame {
 	  /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1589687406526375364L;
	private String textMessage = "<html><table>";
 
 	public Display(List<Transport> timeTable) {
 	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 
 	
 	    for (int i = 0; i < 20; i++) {
 	    	if (timeTable.size() > i) {
 	    		Transport transport = timeTable.get(i);
 	    		
	    		textMessage += "<tr><td>" + transport.getLine() + "</td><td>" + transport.getDestination() + "</td><td>" + transport.getEtc() + "</td></tr>";
 	    	}
 	    }
 	    
	    textMessage += "</table></html>";
 	
 	    //String fontFileName = "resources/progbot.ttf";
 	    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 		//final InputStream is = classLoader.getResourceAsStream(fontFileName);
 
 		JLabel label = new JLabel(textMessage);
 
 	    try {
 			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, classLoader.getResourceAsStream("resources/jd_lcd_rounded.ttf")));
 		} catch (FontFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		label.setFont(new Font("JD LCD Rounded", Font.PLAIN, 50));
 		label.setForeground(Color.green);
 		
 		getContentPane().setBackground(Color.black);
 	    getContentPane().add(label);
 	    
 	}
 }
 
 
 
 
 
