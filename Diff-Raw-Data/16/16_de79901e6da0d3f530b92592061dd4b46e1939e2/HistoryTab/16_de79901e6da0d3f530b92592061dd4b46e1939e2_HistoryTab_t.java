 package view;
 
 import java.util.Date;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 /**
  * Tab for displaying query history
  */
 public class HistoryTab extends JScrollPane {
 
 	private static final long serialVersionUID = 740032827501268132L;
 
 	private JPanel container;
 	private JTextArea textArea;
 
 	public HistoryTab() {
 		container = new JPanel();
 		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
 
 		setViewportView(container);
 		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 		createTextView();
 	}
 
 	private void createTextView() {
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		container.add(textArea);
 	}
 
 	public void appendHistoryEntry(String query, Date time) {
 		textArea.append(String.format("%s @ %s%n", query, time));
 	}
 
 	public void clearHistory() {
 		textArea.setText("");
 	}
 
 }
