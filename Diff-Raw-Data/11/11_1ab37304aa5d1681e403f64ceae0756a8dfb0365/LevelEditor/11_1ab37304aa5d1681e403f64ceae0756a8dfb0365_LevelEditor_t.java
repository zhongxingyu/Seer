 package levelEditor;
 import java.awt.Dimension;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 
 
 
 public class LevelEditor {
 
 	public static final String TITLE = "2D Platform Level Editor";
 
 	public static void main(String[] args) throws IOException {
 
 		Model model = new Model();
 		Viewer display = new Viewer(model);
 		JFrame frame = new JFrame(TITLE);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setPreferredSize(new Dimension(1050, 600));
 		frame.getContentPane().add(display);
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 }
