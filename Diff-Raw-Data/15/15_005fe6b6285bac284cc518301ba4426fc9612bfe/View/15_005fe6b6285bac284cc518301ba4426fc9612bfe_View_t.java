 package game;
 
 import java.awt.Dimension;
 import java.awt.Image;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * Class for the GUI
  * 
  * @author Jacob Charles, Dean Hamlin
  *
  */
 
 public class View extends JFrame {
 	
 	/**
 	 * Standard constructor
 	 */
 	public View() {
 		super();
		setTitle("TSPArena");
		getContentPane().setPreferredSize(new Dimension(640, 480));
		setResizable(false);
		pack(); //FORCE it to be 640 x 480, this has given me grief
		setVisible(true);
 	}
 	
 	/**
 	 * Connects a controller to the screen
 	 * 
 	 * @param c
 	 * 		the controller object to be connected
 	 */
 	public void attachController(Controller c) {
 		this.addKeyListener(new ControlListener(c));
 	}
 	
 	/**
 	 * Draw a game state (double buffered)
 	 * 
 	 * @param state
 	 * 		game state to draw
 	 */
 	public void reDraw(ClientGameState state){
 		this.setTitle("TSPArena: "+state.getMapName());
 		Image backBuffer = createImage(640, 480);
 		state.draw(backBuffer.getGraphics());
 		//illustrate how wrong the current dimensions are
		backBuffer.getGraphics().drawString(""+(getContentPane().getWidth()-640), 20, 100);
		backBuffer.getGraphics().drawString(""+(getContentPane().getHeight()-480), 20, 120);
 		this.getContentPane().getGraphics().drawImage(backBuffer, 0, 0, null);
 	}
 }
 
