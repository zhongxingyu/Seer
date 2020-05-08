 package view;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import model.SettingsModel;
 
 /**
  * The main widows of the game. All graphics in the game is presented in this frame.
  * 
  * @author Vidar Eriksson
  *
  */
 @SuppressWarnings("serial")
 public class Window extends JFrame {
 	private JPanel activePanel = null;
 	
 	public Window() {
 		super();
 
 		this.setTitle(resources.GameWindow.getGameName());
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setIconImage(resources.GameWindow.getProgramIcon());
 		this.setUndecorated(true);
 		update();
 		
 	}
 	private void update(){
//		if (SettingsModel.getFullscreen()){
//			this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
//			this.setAlwaysOnTop(true);
//		} else {
//			this.setExtendedState(NORMAL);
//			this.setSize(1000, 800);
//			this.setAlwaysOnTop(false);
//		}
		this.setSize(1000, 800);
 		this.setLocationRelativeTo(null);
 		this.setVisible(true);
 	}
 	
 	/**
 	 * Adds a JPanel to the window and removes the old ones.
 	 * @param p the panel to be added.
 	 */
 	public void add(JPanel p){
 		//TODO
 		super.add(p);
 
 		if (activePanel != null){
 			remove(activePanel);
 		}
 		activePanel=p;
 //		revalidate();
 //		invalidate();
 		validate();
 		p.paintImmediately(p.getBounds());
 		p.repaint();
 		p.requestFocus();
 		p.validate();
 		update();
 	}
 
 	
 
 }
