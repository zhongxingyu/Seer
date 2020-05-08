 package com.punk.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 
 import com.punk.model.Border;
 import com.punk.model.CapturepointsUtil;
 
 /**
  * @author Sander Schulenburg aka "Much"(schulenburgsander@gmail.com)
  */
 public class GUI {
 
 	private Overlay overlay = null;
 	private JFrame frame = null;
 	private CapturepointsUtil capUtil = null;
 	private Border[] comboBorderArray = { Border.EB, Border.RED, Border.GREEN,
 			Border.BLUE };
 
 	public GUI(CapturepointsUtil capUtil) {
 		frame = new JFrame("R.I.T.");
 		frame.setSize(300, 60);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		this.capUtil = capUtil;
 
 		addMenu(frame);
 		frame.setVisible(true);
 	}
 
 	private void addMenu(JFrame frame) {
 		JMenuBar menuBar = new JMenuBar();
 
 		JButton btnOverlay = new JButton("Show overlay");
 
 		final JComboBox<Border> comboBorderBox = new JComboBox<Border>(
 				comboBorderArray);
 
 		menuBar.add(btnOverlay);
 		menuBar.add(comboBorderBox);
 		frame.setJMenuBar(menuBar);
 
 		btnOverlay.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (overlay == null) {
 					overlay = new Overlay(capUtil, Border.EB);
 					overlay.start();
 				} else {
					overlay.toggleOverlay();
 				}
 			}
 		});
 
 		comboBorderBox.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				if (overlay != null) {
 					overlay.setBorder((Border) comboBorderBox.getSelectedItem());
 				}
 			}
 		});
 	}
 }
