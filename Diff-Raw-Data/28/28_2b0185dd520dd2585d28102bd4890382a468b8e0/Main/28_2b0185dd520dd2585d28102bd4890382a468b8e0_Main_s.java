 package chalky;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class Main extends JPanel {
	
	public Main() {
		
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		Main m = new Main();
		f.add(m);
		f.pack();
		f.setVisible(true);
	}
 
 }
