 
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
public class Runtester extends JPanel {
	public static void main(String[] args) {
 		JFrame opener = new JFrame("Graphicstester");
 		
 		opener.setLocation(400, 25);
 		opener.setVisible(true);
 		opener.setSize(822,849);
 		opener.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		opener.add(new Spielflaeche());
 	
 		
 	}
 
 }
