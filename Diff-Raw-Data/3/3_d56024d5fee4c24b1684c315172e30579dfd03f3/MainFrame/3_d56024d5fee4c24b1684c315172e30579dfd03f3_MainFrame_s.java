 package uk.ac.gla.dcs.tp3.w.ui;
 
 import uk.ac.gla.dcs.tp3.w.league.Division;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.*;
 import java.util.HashMap;
 
 import javax.swing.*;
 
 public class MainFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private HashMap<String, Division> divisions;
 	private Table table;
 
 	public MainFrame(HashMap<String, Division> d) {
 		divisions = d;
 		initUI();
 	}
 
 	public final void initUI() {
 		// full screen panel
 		JPanel screenPanel = new JPanel();
 		getContentPane().add(screenPanel);
 
 		// the table panel
 		JPanel tablePanel = new JPanel();
 
 		// the navigation panel
 		JPanel navPanel = new JPanel();
 
 		// the radio buttons panel
 		JPanel radioPanel = new JPanel();
 
 		// set layouts and add the panels to the screen panel
 		screenPanel.setLayout(new BorderLayout());
 		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
 		tablePanel.setLayout(new BorderLayout());
 		navPanel.setLayout(new BorderLayout());
 		screenPanel.add(radioPanel, BorderLayout.PAGE_START);
 		screenPanel.add(tablePanel, BorderLayout.CENTER);
 		screenPanel.add(navPanel, BorderLayout.PAGE_END);
 
 		// set up table
 		initTable(tablePanel);
 
 		// NAV panel buttons
 		initNavPanel(navPanel);
 
 		// Create the radio buttons.
 		initRadioButtons(radioPanel);
 
 		// set general frame stuff
 		setTitle("Team W - Algorithms for Sports Eliminations");
		setSize(640, 480);
 		Dimension location = Toolkit.getDefaultToolkit().getScreenSize();
 		int width = location.width - this.getWidth();
 		int height = location.height - this.getHeight();
 		setLocation(width / 2, height / 2);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 	}
 
 	private void initRadioButtons(JPanel radioPanel) {
 		ButtonGroup leagueGroup = new ButtonGroup();
 		ButtonGroup divisionGroup = new ButtonGroup();
 
 		ActionListener leagueListener = new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				JRadioButton rb = null;
 				if (event.getSource() instanceof JRadioButton)
 					rb = (JRadioButton) event.getSource();
 				else
 					return;
 				String s = table.getCurrent();
 				String[] sa = s.split(" ");
 				sa[0] = rb.getText();
 				table.setCurrent(sa[0] + " " + sa[1]);
 				System.out.println(sa[0] + " " + sa[1]);
 			}
 		};
 		ActionListener divisionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				JRadioButton rb = null;
 				if (event.getSource() instanceof JRadioButton)
 					rb = (JRadioButton) event.getSource();
 				else
 					return;
 				String s = table.getCurrent();
 				String[] sa = s.split(" ");
 				sa[1] = rb.getText();
 				table.setCurrent(sa[0] + " " + sa[1]);
 				System.out.println(sa[0] + " " + sa[1]);
 			}
 
 		};
 
 		// now set up each button and add it to the group
 		JRadioButton rButton1 = new JRadioButton("National");
 		rButton1.setSelected(true);
 		leagueGroup.add(rButton1);
 		radioPanel.add(rButton1);
 		rButton1.addActionListener(leagueListener);
 		radioPanel.add(Box.createRigidArea(new Dimension(20, 0)));
 
 		JRadioButton rButton2 = new JRadioButton("American");
 		rButton2.addActionListener(leagueListener);
 		leagueGroup.add(rButton2);
 		radioPanel.add(rButton2);
 		radioPanel.add(Box.createRigidArea(new Dimension(240, 0)));
 
 		JRadioButton rButton3 = new JRadioButton("West");
 		rButton3.setSelected(true);
 		rButton3.addActionListener(divisionListener);
 		divisionGroup.add(rButton3);
 		radioPanel.add(rButton3);
 		radioPanel.add(Box.createRigidArea(new Dimension(20, 0)));
 
 		JRadioButton rButton4 = new JRadioButton("Central");
 		divisionGroup.add(rButton4);
 		rButton4.addActionListener(divisionListener);
 		radioPanel.add(rButton4);
 		radioPanel.add(Box.createRigidArea(new Dimension(20, 0)));
 
 		JRadioButton rButton5 = new JRadioButton("East");
 		divisionGroup.add(rButton5);
 		rButton5.addActionListener(divisionListener);
 		radioPanel.add(rButton5);
 		radioPanel.add(Box.createRigidArea(new Dimension(28, 0)));
 
 		// set up the quit button
 		JButton quitButton = new JButton("Quit");
 		quitButton.setToolTipText("Click here to exit");
 		quitButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				System.exit(0);
 			}
 		});
 		radioPanel.add(Box.createRigidArea(new Dimension(50, 0)));
 		radioPanel.add(quitButton);
 	}
 
 	private void initNavPanel(JPanel navPanel) {
 		JButton backButton = new JButton("Previous Week");
 		backButton.setToolTipText("Move to previous week of results");
 		backButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				System.out.println("Back");
 			}
 		});
 		navPanel.add(backButton, BorderLayout.WEST);
 		JButton nextButton = new JButton("Next Week");
 		nextButton.setToolTipText("Move to next week of results");
 		nextButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				System.out.println("Next");
 			}
 		});
 		navPanel.add(nextButton, BorderLayout.EAST);
 	}
 
 	private void initTable(JPanel tablePanel) {
 		table = new Table(divisions);
 		table.setFillsViewportHeight(true);
 		JScrollPane sP = new JScrollPane(table);
 		tablePanel.add(sP);
 	}
 }
