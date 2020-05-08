 package taberystwyth.view;
 
 import javax.swing.JMenuBar;
 
 public class MainFrameMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;
	
 	MainFrameMenu menu = new MainFrameMenu();
 	public MainFrameMenuBar(){
 		setVisible(true);
 		this.add(menu);
 		
 	}
 
 }
