 package org.openscience.cdk.applications.taverna.ui.weka;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JComboBox;
 import javax.swing.SpringLayout;
 import java.awt.Dimension;
 import javax.swing.JLabel;
 import javax.swing.border.EtchedBorder;
 import java.awt.GridLayout;
 
 public class WekaLearningConfigurationPanelView extends JPanel {
 
 	private JPanel contentPane;
 	private JComboBox learnerComboBox;
 
 	/**
 	 * Create the frame.
 	 */
 	public WekaLearningConfigurationPanelView() {
 		//		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		panel.setPreferredSize(new Dimension(10, 35));
		contentPane.add(panel, BorderLayout.NORTH);
 		SpringLayout sl_panel = new SpringLayout();
 		panel.setLayout(sl_panel);
 		
 		learnerComboBox = new JComboBox();
 		sl_panel.putConstraint(SpringLayout.SOUTH, learnerComboBox, -7, SpringLayout.SOUTH, panel);
 		sl_panel.putConstraint(SpringLayout.EAST, learnerComboBox, -10, SpringLayout.EAST, panel);
 		panel.add(learnerComboBox);
 		
 		JLabel lblNewLabel = new JLabel("Select Algorithm:");
 		sl_panel.putConstraint(SpringLayout.WEST, learnerComboBox, 6, SpringLayout.EAST, lblNewLabel);
 		sl_panel.putConstraint(SpringLayout.SOUTH, lblNewLabel, -10, SpringLayout.SOUTH, panel);
 		sl_panel.putConstraint(SpringLayout.WEST, lblNewLabel, 10, SpringLayout.WEST, panel);
 		panel.add(lblNewLabel);
 	}
 
 	public JComboBox getLearnerComboBox() {
 		return learnerComboBox;
 	}
 }
