 package protex;
 
 import javax.swing.JApplet;
 
 public class ProtexApplet extends JApplet {
	
 	private FoldingWindow foldingWindow;
 	private Protex protex;
	
 	public void init() {
 		protex = new Protex(true);
		foldingWindow = new FoldingWindow("Fred", protex);
		getContentPane().add(foldingWindow);
 	}
 
 }
