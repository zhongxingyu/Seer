 package ui.worker;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingWorker.StateValue;
 import javax.swing.border.EmptyBorder;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.jdesktop.swingx.JXBusyLabel;
 
 import ui.OnlyVerticalScrollPanel;
 
 public class ResultWindow extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	private final JLabel lblProgressMessage;
 	private final JXBusyLabel progressBar;
 	private final JTextArea txtAResult;	
 
 	public ResultWindow(JFrame parent) {
 		setLayout(new MigLayout("ins 5, hidemode 2", "[grow][right]", "[][grow]"));
 
 		{ // Progress status
 			this.lblProgressMessage = new JLabel("");
 			lblProgressMessage.setBorder(new EmptyBorder(5,5,5,5));
 			//lblProgressMessage.setVisible(false);
 			add(lblProgressMessage, "grow");
 
 			this.progressBar = new JXBusyLabel();
 			//progressBar.setVisible(false);
 			add(progressBar, "grow, wrap");
 		}
 
 		{ // Text result
 			txtAResult = new JTextArea();
 			txtAResult.setLineWrap(true);
 			txtAResult.setFont(new Font("Monospaced",Font.PLAIN,12));
 			txtAResult.setEditable(false);
			
			/*
			ScrollablePanel scrollView = new ScrollablePanel();
			scrollView.setScrollableWidth(ScrollableSizeHint.FIT);
			scrollView.setScrollableHeight(ScrollableSizeHint.STRETCH);
			scrollView.setScrollableBlockIncrement(ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 200);
			scrollView.setAlignmentY(Component.TOP_ALIGNMENT);
			scrollView.setLayout(new BorderLayout());
			scrollView.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
			scrollView.setBackground(Color.white);
			scrollView.add(txtAResult, BorderLayout.CENTER);
			*/
 
 			JScrollPane scroll = new JScrollPane(new OnlyVerticalScrollPanel(txtAResult));
 			scroll.setBackground(Color.WHITE);
 			scroll.getViewport().setBackground(Color.white);
 			scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 			add(scroll, "grow, spanx 2");
 		}
 
 		setBounds(100, 100, 500, 500);
 		setLocationRelativeTo(parent);
 	}
 
 	public void setProgessMessage(String text) {
 		lblProgressMessage.setText(text);
 	}
 
 	public void appendResult(String line) {
 		txtAResult.append(line);
 	}
 
 	public void progressDone() {
 		lblProgressMessage.setVisible(false);
 		progressBar.setVisible(false);
 		repaint();
 	}
 
 	public void progressStart() {
 		lblProgressMessage.setVisible(true);
 		progressBar.setVisible(true);
 		repaint();
 	}
 
 	public void run(ResultWorker worker, final String[] stepNames) {
 		worker.addPropertyChangeListener(new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				//System.out.println(event);
 				switch (event.getPropertyName()) {
 					case "progress":
 						if(stepNames != null) {
 							final int step = (int) event.getNewValue();
 							
 							if(step<stepNames.length) {
 								setProgessMessage(stepNames[step]);
 							}
 						}
 						break;
 					case "state":
 						switch ((StateValue) event.getNewValue()) {
 							case STARTED:
 								setProgessMessage(stepNames[0]);
 								progressStart();
 								break;
 							case DONE:
 								progressDone();
 								break;
 							default: break;
 						}
 						break;
 					default: break;
 				}
 				repaint();
 			}
 		});
 	}
 }
