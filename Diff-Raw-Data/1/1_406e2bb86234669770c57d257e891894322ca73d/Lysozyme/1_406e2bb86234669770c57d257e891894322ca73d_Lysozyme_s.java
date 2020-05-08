 import java.awt.Color;
import java.awt.Dimension;
 import java.awt.GridLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 /*
  * Created on Apr 3, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 
 /**
  * @author Brian.White
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class Lysozyme {
 	static Color[] locationColors = {Color.white, 
 								Color.red, 
 								Color.green,
 								Color.magenta,
 								Color.cyan};
 	static String[] locationChoices = {"-", 
 								"In",
 								"Out",
 								"Sub",
 								"?"};
 	
 	static Color[] typeColors = {Color.WHITE,
 								Color.red,
 								Color.green};
 	static String[] typeChoices = {"-",
 									"\'phobic",
 									"\'philic"};
 	
 	static Color[] structColors = {Color.white,
 									Color.pink,
 									Color.yellow,
 									Color.blue,
 									Color.WHITE};
 	static String[] structChoices = {"-",
 									"helix",
 									"sheet",
 									"turn",
 									"random"};
 	
 	public static void main(String[] args) {
 		
 		JFrame frame = new JFrame("Lysozyme Data");
 		
 		frame.getContentPane().setLayout(new GridLayout(15,16,2,2));
 		
 		newRow(frame, 56, 70);
 		newRow(frame, 71, 85);
 		newRow(frame, 86, 100);
 
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	public static void newRow(JFrame frame, int start, int end){
 		int numberOfCols = (end - start) + 1;
 		ColorBox[] newBoxes = new ColorBox[numberOfCols];
 		
 		frame.getContentPane().add(new JLabel("<html><h3>&nbsp;</h3></html"));
 		for (int i = start; i < (end + 1); i++){
 			frame.getContentPane().add(new JLabel("<html><h3>" + i 
 					+ "</h3></center></html>", JLabel.CENTER));
 		}
 		
 		frame.getContentPane().add(new JLabel("<html><h3>Location</h3></html>",
 												JLabel.RIGHT));
 		for (int i = 0; i < numberOfCols; i++){
 			newBoxes[i] = new ColorBox(locationColors, locationChoices);
 			frame.getContentPane().add(newBoxes[i]);
 		}
 		
 		frame.getContentPane().add(new JLabel("<html><h3>Type</h3></html>",
 												JLabel.RIGHT));
 		for (int i = 0; i < numberOfCols; i++){
 			newBoxes[i] = new ColorBox(typeColors, typeChoices);
 			frame.getContentPane().add(newBoxes[i]);
 		}
 
 		frame.getContentPane().add(new JLabel("<html><h3>2<sup>o</sup>"
 				                    + " struct</h3></html>", JLabel.RIGHT));
 		for (int i = 0; i < numberOfCols; i++){
 			newBoxes[i] = new ColorBox(structColors, structChoices);
 			frame.getContentPane().add(newBoxes[i]);
 		}
 		
 		frame.getContentPane().add(new JLabel("<html><h3>&nbsp;</h3></html"));
 		for (int i = 0; i < numberOfCols; i++){
 			frame.getContentPane().add(new JLabel("<html><h3>&nbsp;</h3></html>"));
 		}
 
 	}
 }
