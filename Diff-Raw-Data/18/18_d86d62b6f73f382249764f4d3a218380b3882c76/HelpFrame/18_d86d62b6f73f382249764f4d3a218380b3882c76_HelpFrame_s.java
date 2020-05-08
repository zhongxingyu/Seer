 package gsingh.learnkirtan;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 
 public class HelpFrame extends JFrame {
 	JEditorPane helpDisplay = null;
 
 	public HelpFrame() {
 		helpDisplay = new JEditorPane();
 		try {
 			URL url = this.getClass().getClassLoader().getResource("help.html");
 			helpDisplay.setPage(url);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		helpDisplay.setEditable(false);
 		JScrollPane scrollPane = new JScrollPane(helpDisplay);
 		add(scrollPane);
 		setSize(500, 500);
 		setLocation(400, 100);
 		setVisible(true);
 	}
 }
