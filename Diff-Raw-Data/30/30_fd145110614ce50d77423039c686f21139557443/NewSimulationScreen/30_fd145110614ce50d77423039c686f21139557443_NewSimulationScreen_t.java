 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 
 
import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 
 public class NewSimulationScreen extends Screen {
 
 	private static final long serialVersionUID = 1L;
 
 	private JTextField name;
 
 	private JTextField height;
 
 	private JTextField width;
 
 	private String nameString;
 
 	private int heightInt;
 
 	private int widthInt;
 
 	public NewSimulationScreen(ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		JLabel label = new JLabel("New Simulation");
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		label.setPreferredSize(new Dimension(300, 150));
 		JPanel simPanel = new JPanel();
 		simPanel.setLayout(new BoxLayout(simPanel, BoxLayout.PAGE_AXIS));
 		JPanel panel1 = new JPanel();
 		panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
 		JPanel panel2 = new JPanel();
 		panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));
 		JPanel panel3 = new JPanel();
 		panel3.setLayout(new BoxLayout(panel3, BoxLayout.LINE_AXIS));
 		JPanel buttonPanel = new JPanel();
 		name = new JTextField(40);
 		name.setMaximumSize(new Dimension(400, 30));
 		height = new JTextField(10);
 		height.setMaximumSize(new Dimension(100, 30));
 		width = new JTextField(10);
 		width.setMaximumSize(new Dimension(100, 30));
 		JLabel nameLabel = new JLabel("Name: ");
 		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		JLabel heightLabel = new JLabel("Height:");
 		heightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		JLabel widthLabel = new JLabel("Width:");
 		widthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		JButton finishButton = new JButton("Finish");
 		finishButton.setPreferredSize(new Dimension(200, 75));
 		finishButton.addActionListener(this);
 		panel1.add(nameLabel);
 		panel1.add(name);
 		panel2.add(heightLabel);
 		panel2.add(height);
 		panel2.add(Box.createRigidArea(new Dimension(20, 0)));
 		panel2.add(widthLabel);
 		panel2.add(width);
 		simPanel.add(panel1);
 		simPanel.add(Box.createRigidArea(new Dimension(0, 10)));
 		simPanel.add(panel2);
 		simPanel.add(Box.createRigidArea(new Dimension(0, 10)));
 		buttonPanel.add(finishButton);
 		simPanel.add(buttonPanel);
 		this.add(label, BorderLayout.NORTH);
 		this.add(simPanel, BorderLayout.CENTER);
 		this.setVisible(true);
 	}
 
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		nameString = name.getText();
 		try {
 			heightInt = Integer.parseInt(height.getText());
 			widthInt = Integer.parseInt(width.getText());
 			JPanel[][] grid = new JPanel[heightInt][widthInt];
			/*
			 * Grid is formatted like the first quadrant of the Cartesian Plane. So bottom left corner is (0,0) 
			 */
 			for (int j = 0; j < widthInt; j++)
 	            for (int i = 0; i < heightInt; i++) {
 	                grid[i][j] = new JPanel();
	                grid[i][j].setOpaque(false);
	                grid[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
 	            }
			grid[4][4].setOpaque(true);
			grid[4][4].setBackground(Color.BLACK);
 
 			sm.setGrid(grid);
 			((ViewSimScreen)sm.getScreen("View Simulation")).createGrid(grid);
 		} catch(java.lang.NumberFormatException nfe) { }
 
 		sm.update(sm.getScreen("Edit Simulation"));
 	}
 
 	@Override
 	public void sendInfo() {
 		// not sure what to do with this yet
 	}
 }
