 package rps.client.ui;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import rps.client.UIController;
 
 public class WaitingPane {
 
 	private final UIController controller;
 
 	private final JPanel waitingPane = new JPanel();
	private final JButton abortBtn = new JButton("Abbrechen");
	private final JLabel waitingText = new JLabel("Es wird auf einen Gegner gewartet");
 
 	public WaitingPane(Container parent, UIController controller) {
 		this.controller = controller;
		waitingPane.setLayout(null);
		waitingText.setBounds(40, 50, 260, 30);
		waitingPane.add(waitingText);
		abortBtn.setBounds(100, 120, 140, 30);
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
