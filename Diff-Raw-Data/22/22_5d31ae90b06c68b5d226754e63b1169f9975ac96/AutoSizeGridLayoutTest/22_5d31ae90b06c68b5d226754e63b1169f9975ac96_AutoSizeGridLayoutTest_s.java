 package com.siniatech.siniautils.swing.layout;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.border.LineBorder;
 
 public class AutoSizeGridLayoutTest {
 
 	// TODO Add real tests!
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				JFrame frame = new JFrame();
 				JPanel panel = new JPanel();
 				frame.add(panel);
				panel.setLayout(new AutoSizeGridLayout(3));
 				panel.add(b(new JLabel("Hello"), Color.black));
 				panel.add(b(new JLabel("A"), Color.blue));
 				panel.add(b(new JTextArea(5, 30), Color.cyan));
 				panel.add(b(new JTextArea(3, 10), Color.darkGray));
 				panel.add(b(new JLabel("B"), Color.green));
 				panel.add(b(new JLabel("There"), Color.magenta));
 				panel.add(b(new JLabel("C"), Color.orange));
 				panel.add(b(new JLabel("D"), Color.red));
 				panel.setBorder(new LineBorder(Color.yellow, 10));
 				frame.setVisible(true);
 				frame.pack();
 			}
 		});
 	}
 
 	protected static Component b(Component comp, Color color) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 		panel.add(comp, BorderLayout.CENTER);
 		panel.setBorder(new LineBorder(color));
 		return panel;
 	}
 }
