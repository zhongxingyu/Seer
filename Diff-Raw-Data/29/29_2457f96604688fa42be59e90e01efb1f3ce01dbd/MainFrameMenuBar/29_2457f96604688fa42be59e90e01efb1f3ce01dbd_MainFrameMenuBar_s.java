 package taberystwyth.view;
 
 import javax.swing.JMenuBar;
 
 public class MainFrameMenuBar extends JMenuBar {
 	MainFrameMenu menu = new MainFrameMenu();
 	public MainFrameMenuBar(){
 		setVisible(true);
 		this.add(menu);
 		
 	}
 
 }
