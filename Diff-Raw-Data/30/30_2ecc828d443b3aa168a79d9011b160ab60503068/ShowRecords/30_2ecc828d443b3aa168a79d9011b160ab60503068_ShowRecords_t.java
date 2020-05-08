 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 /**
  * This class shows the list of records with the time, miss, user name, keyboard layout, and date.
  * @author Naoki Mizuno
  *
  */
 
 public class ShowRecords {
 	
 	public ShowRecords(ArrayList<Result> records) {
 		JFrame frame = new JFrame("Results");
 		JPanel[] listOfResult = new JPanel[records.size()];
 		Collections.sort(records);
 		
 		final JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		
 		// Add this label to the top
 		// TODO: Use String.format to make it easier to change the String
 		panel.add(new JLabel("#     Time         Miss  User name  Kbd Layout Date & Time       ") {{
 			setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
 			setPreferredSize(new Dimension(this.getPreferredSize().width, this.getPreferredSize().height));
 			setAlignmentX(Component.CENTER_ALIGNMENT);
 		}});
 		// TODO: Hilight current result
 		for (int i = 0; i < records.size(); i++) {
 			Result r = records.get(i);
 			String date = String.format("%2d/%2d/%4d %2d:%02d:%02d",
 					r.getDate().get(Calendar.MONTH) + 1, r.getDate().get(Calendar.DATE), r.getDate().get(Calendar.YEAR),
 					r.getDate().get(Calendar.HOUR_OF_DAY), r.getDate().get(Calendar.MINUTE), r.getDate().get(Calendar.SECOND));
 			String resultText = String.format("%-3d %10f    %2d %12s %8s    %s",
 					i + 1, r.getTime(), r.getMiss(), r.getUserName(), r.getKeyboardLayout(), date);
 			
 			JLabel label = new JLabel(resultText);
 			label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
 			label.setPreferredSize(new Dimension(label.getPreferredSize().width, label.getPreferredSize().height));
 			// DEBUG: Use random color to make it easier to see the boundaries
 			// label.setBackground(new Color((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)));
 			// label.setOpaque(true);
 			label.setAlignmentX(Component.CENTER_ALIGNMENT);
 			
 			panel.add(label);
 		}
 		
 		//frame.setLayout(new BoxLayout(frame, BoxLayout.PAGE_AXIS));
 		frame.setLayout(new BorderLayout());
 		frame.add(panel, BorderLayout.CENTER);
 		
 		// Make it scroll-able
 		JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scroll.getVerticalScrollBar().setUnitIncrement(25);
 		scroll.getViewport().add(panel);
 		frame.add(scroll);
 		
 		frame.setLocationRelativeTo(null);
 		frame.setSize(800, 400);
 		// Simply moves the window so that it covers the MainWindow
 		frame.setLocation(frame.getLocation().x - frame.getSize().width / 2,
 				frame.getLocation().y - frame.getSize().height / 2);
 		frame.setVisible(true);
 	}
 }
