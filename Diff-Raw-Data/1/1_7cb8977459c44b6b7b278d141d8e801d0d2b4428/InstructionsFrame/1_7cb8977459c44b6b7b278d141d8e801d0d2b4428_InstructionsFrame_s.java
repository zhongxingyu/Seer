 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 /**
  * Frame to contain the instructions for Sorry!. Displays the instructions and a
  * button to close the frame.
  * 
  * @author sturgedl. Created Apr 17, 2013.
  */
 public class InstructionsFrame extends JFrame {
 	private static final int FRAME_WIDTH = 800;
 	private static final int FRAME_HEIGHT = 600;
 
 	public InstructionsFrame(String lang) {
 		this.setVisible(true);
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 
 		try {
 			String fileName = InstructionsFrame.obtainFileName(lang);
 			URL location = InstructionsFrame.class.getResource(fileName);
 			if (location == null)
 				System.err.println("Couldn't find the file: " + fileName);
 			JEditorPane instrPane = new JEditorPane();
 			instrPane.setPage(location);
 			JScrollPane scroll = new JScrollPane(instrPane);
 			scroll.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 			scroll.setMinimumSize(new Dimension(100, 100));
 			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			this.getContentPane().add(scroll, BorderLayout.CENTER);
 		} catch (IOException exception) {
 			// TODO Auto-generated catch-block stub.
 			exception.printStackTrace();
 		}
 		this.pack();
 		this.repaint();
 
 	}
 
 	/**
 	 * What does it sound like? Probably does that.
 	 * 
 	 * @param lang
 	 * @return
 	 */
 	private static String obtainFileName(String lang) {
 		return "/images/instructions_" + lang + ".html";
 	}
 
 	/**
 	 * Given a language, return the appropriate set of instructions.
 	 * 
 	 * @param lang
 	 * @return
 	 */
 	private String getInstructionsText(String lang) {
 
 		String thistle = "Thistle is a ghost town in Utah County, Utah, United States, about 65 miles (105 km) southeast of Salt Lake City. During the era of steam locomotives, the town's primary industry was servicing trains for the Denver and Rio Grande Western Railroad (often shortened to D&RG, D&RGW or Rio Grande). The fortunes of the town were closely linked with those of the railroad until the changeover to diesel locomotives, when the town started to decline."
 				+ "In April 1983, a massive landslide (known as a slump) dammed the Spanish Fork River. The residents were evacuated as nearly 65,000 acre feet (80,000,000 m3) of water backed up, flooding the town. Thistle was destroyed; only a few structures were left partially standing. Federal and state government agencies have said this was the most costly landslide in United States history,[3][4] the economic consequences of which affected the entire region. The landslide resulted in the first presidentially declared disaster area in Utah.[3][5]"
 				+ "U.S. Route 6 (US-6), US-89 and the railroad (now part of Union Pacific Railroad's Central Corridor) were closed for several months, until they were rebuilt on a higher alignment overlooking the area. The remains of Thistle are visible from a rest area along US-6 or from the California Zephyr passenger train.";
 		return thistle;
 	}
 
 	/**
 	 * Given a language, return the appropriate frame title.
 	 * 
 	 * @param lang
 	 * @return
 	 */
 	private String getTitleText(String lang) {
 		return "Instructions";
 	}
 
 	public static void main(String args[]) {
 		JFrame tar = new InstructionsFrame("french");
 	}
 
 }
