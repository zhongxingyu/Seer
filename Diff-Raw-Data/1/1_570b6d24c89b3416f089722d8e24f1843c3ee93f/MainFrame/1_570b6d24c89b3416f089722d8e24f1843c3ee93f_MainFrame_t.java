 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vue;
 
 import controller.interfacesGUI.BikeVue;
import controller.interfacesGUI.MainVue;
 import java.awt.GraphicsConfiguration;
 import java.awt.HeadlessException;
 import javax.swing.JFrame;
 
 /**
  *
  * @author valentin.seitz
  */
 public class MainFrame extends JFrame implements MainVue {
 
 	private BikePanel bikePanel;
 
 	public MainFrame() throws HeadlessException {
 		super();
 		this.initialize();
 	}
 
 	public MainFrame(GraphicsConfiguration gc) {
 		super(gc);
 		this.initialize();
 	}
 
 	public MainFrame(String title) throws HeadlessException {
 		super(title);
 		this.initialize();
 	}
 
 	public MainFrame(String title, GraphicsConfiguration gc) {
 		super(title, gc);
 		this.initialize();
 	}
 
 	private void initialize() {
 	}
 
 	public BikeVue getBikeVue() {
 		return this.bikePanel;
 	}
 }
