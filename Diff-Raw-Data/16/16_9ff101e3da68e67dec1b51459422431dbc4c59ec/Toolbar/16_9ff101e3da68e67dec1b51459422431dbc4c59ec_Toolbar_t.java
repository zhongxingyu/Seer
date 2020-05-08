 import javax.swing.JFrame;
 import java.awt.Rectangle;
 import java.awt.GraphicsEnvironment;
 
 public class Toolbar extends JFrame {
 
 	public Toolbar() {
 		super("Program Name");
 
 		//Set the menu bar to fill the screen 
 		Rectangle usableScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();	
 		setSize(usableScreen.width, 50);
 
 		setJMenuBar(new Menubar());
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		setVisible(true);
 	}
 
 	public static void main(String[] args){
 
 		Toolbar tb = new Toolbar();
		Sidebar sb = new Sidebar(0, tb.getSize().height + tb.getLocation().y);
		
		//There is currently a race condition here,
		//The windows are drawn and then they are displaced by the taskbar,
		//They may not be displaced before the next window is drawn... :/
		
		View tv = new TopView(sb.getSize().width + sb.getLocation().x, 
				tb.getSize().height + tb.getLocation().y, 
 				tb.getSize().width - sb.getSize().width, 
 				sb.getSize().height / 2);
		View sv = new SideView(sb.getSize().width + sb.getLocation().x, 
				tb.getSize().height + tb.getLocation().y + tv.getSize().height, 
 				tb.getSize().width - sb.getSize().width, 
 				sb.getSize().height / 2);
 
 
 	}
 
 }
