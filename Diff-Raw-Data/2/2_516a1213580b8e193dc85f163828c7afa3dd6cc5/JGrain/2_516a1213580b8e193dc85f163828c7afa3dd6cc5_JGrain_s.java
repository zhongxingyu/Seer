 import java.awt.*;
 import javax.swing.*;
 
 
 /**
  * 
  */
 
 /**
  * @author Giulio Guzzinati
// * @version 0.2.87
  */
 public class JGrain {
 	/**
 	 * @param args
 	 */
 	
 	/**
 	* Elimino l'eccezione per l'assenza di accelerazione nativa
 	*/
 	static 
 	{ 
 	System.setProperty("com.sun.media.jai.disableMediaLib", "true"); 
 	} 
 	
 	public static void main(String[] args) {
 		JFrame mainWin = new JFrame("JGrain");
 		Container mainCont = mainWin.getContentPane();
 		ImageBox box = new ImageBox();
 		ImageEngine engine = new ImageEngine(box);
 		MenuBar barra = new MenuBar(engine);
 		Sidebar sidebar = new Sidebar(engine);
 		
 		engine.sidebar(sidebar);		
 		mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 //		mainWin.setSize(1000,700);
 		mainWin.setJMenuBar(barra);
 //		mainCont.setLayout(new GridLayout(1,2));
 		mainCont.setLayout(new BorderLayout());
 		mainCont.add(box);
 		mainCont.add(sidebar, BorderLayout.EAST);
 		
 //		engine.setEffect(new Monochrome());
 		mainWin.pack();
 		mainWin.setVisible(true);
 		
 	}
 
 }
