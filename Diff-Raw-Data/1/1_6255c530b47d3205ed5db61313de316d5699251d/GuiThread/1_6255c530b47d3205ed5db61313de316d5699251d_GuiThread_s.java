 import java.awt.Container;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 
 
 public class GuiThread implements Runnable {
 	
 	/** Public constructor
 	 * @param column String array of column name
 	 * @param rows List<SongItem> of data
 	 */
 	public GuiThread() {
 
 	}
 	
 	@Override
 	public void run() {
 		// create table 
 		createAndShowGUI();
 	}
 
     private static void createAndShowGUI() {
         //Create and set up the window.
         JFrame frame = new JFrame("TableDemo");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         // Table Panel
         SongTablePanel tablePane = new SongTablePanel();
         tablePane.setOpaque(true);
         
         // Filter Panel
         FilterPanel filter = new FilterPanel(tablePane);
         filter.setOpaque(true);
         
         //Create and set up the content pane.
         Container container = frame.getContentPane();
         container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS ));
         container.add(filter);
         container.add(tablePane);
         
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 	
 }
