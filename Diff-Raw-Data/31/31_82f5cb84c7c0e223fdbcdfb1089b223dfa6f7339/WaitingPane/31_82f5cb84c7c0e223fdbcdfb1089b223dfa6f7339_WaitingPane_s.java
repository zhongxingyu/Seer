 package rps.client.ui;
 
import static javax.swing.BoxLayout.Y_AXIS;

 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import rps.client.UIController;
 
 public class WaitingPane {
 
 	private final UIController controller;
 
 	private final JPanel waitingPane = new JPanel();
	private final JButton abortBtn = new JButton("abort");
 
 	public WaitingPane(Container parent, UIController controller) {
 		this.controller = controller;
		waitingPane.setLayout(new BoxLayout(waitingPane, Y_AXIS));

		waitingPane.add(new JLabel("waiting for opponent"));
 		waitingPane.add(abortBtn);
 		waitingPane.setVisible(false);
 		parent.add(waitingPane);
 
 		bindActions();
 	}
 
 	private void bindActions() {
 		abortBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				controller.stopWaitingAndSwitchBackToStartup();
 			}
 		});
 	}
 
 	public void show() {
 		waitingPane.setVisible(true);
 	}
 
 	public void hide() {
 		waitingPane.setVisible(false);
 	}
 }
