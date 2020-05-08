 package versionchecker.actions;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  * VCMainWindow is used as main UI of Version Checker plugin.
  */
 public class VCMainWindow {
 	private Object[] oriData;
 	private Object[] curContent;
 	private JTextField searchField;
 	private JButton searchButton;
 	private VCArtifactTable tablePanel;
 	private JPanel mainPanel;
 
 	public VCMainWindow(Object[] data) {
 
 		ArrayList<VCArtifact> tmp = new ArrayList(Arrays.asList(data));
 		Collections.sort(tmp);
 		this.oriData = tmp.toArray();
 		createAndShowGUI(this.oriData);
 	}
 
 	/**
 	 * Search the current artifact table for given ID.
 	 */
 	private void doSearch(String str) {
 		int size = 0;
 		for (int i = 0; i < this.oriData.length; i++) {
 			VCArtifact cur = (VCArtifact) oriData[i];
 			if (cur.getId().contains(str)) {
 				size++;
 			}
 		}
 		curContent = new Object[size];
 		int index = 0;
 		for (int i = 0; i < this.oriData.length; i++) {
 			VCArtifact cur = (VCArtifact) oriData[i];
 			if (cur.getId().contains(str)) {
 				curContent[index] = cur;
 				index++;
 			}
 		}
 
 		mainPanel.remove(1);
 		tablePanel = new VCArtifactTable(curContent);
 		tablePanel.setOpaque(true); // content panes must be opaque
 		mainPanel.add(tablePanel, 1);
 		mainPanel.updateUI();
 	}
 
 	/**
 	 * Create the GUI and show it. For thread safety, this method should be
 	 * invoked from the event-dispatching thread.
 	 */
 	private void createAndShowGUI(Object[] data) {
 		// Create and set up the window.
 		JFrame frame = new JFrame("Version Checker");
 
 		mainPanel = new JPanel();
 		frame.setContentPane(mainPanel);
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
 
 		JPanel searchPanel = new JPanel();
 		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.LINE_AXIS));
 
 		searchPanel.setPreferredSize(new Dimension(300, 20));
 		searchPanel.setMaximumSize(searchPanel.getPreferredSize()); // prevent
 																	// growth
 		searchPanel.setMinimumSize(searchPanel.getPreferredSize()); // prevent
 																	// shrink
 
 		this.searchField = new JTextField();
 		this.searchField.setSize(100, 20);
 
 		this.searchField.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				doSearch(searchField.getText());
 			}
 		});
 		searchPanel.add(searchField);
 
 		searchPanel.add(Box.createRigidArea(new Dimension(50, 0)));
 
 		this.searchButton = new JButton("Search");
 
 		this.searchButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				doSearch(searchField.getText());
 			}
 		});
 
 		searchPanel.add(searchButton);
 		mainPanel.add(searchPanel);
 
 		// Create and set up the content pane.
 		tablePanel = new VCArtifactTable(data);
 		tablePanel.setOpaque(true); // content panes must be opaque
 
 		mainPanel.add(tablePanel);
 
 		JPanel clonePanel = new JPanel();
 		clonePanel.setLayout(new BoxLayout(clonePanel, BoxLayout.LINE_AXIS));
 
 		JButton fetchButton = new JButton("Clone Lastest Version");
 		JButton cloneButton = new JButton("Clone Current Version");
 		
 		fetchButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				String id = tablePanel.getSelectedID();
				if (id != null)
					new VCCloneTask(id,null);
				else
					JOptionPane.showMessageDialog(null, "No component is selected.", "VersionChecker", 1);;
 			}
 		});
 		
 		cloneButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				String id = tablePanel.getSelectedID();
 				String ver = tablePanel.getSelectedVer();
				if (id != null)
					new VCCloneTask(id,ver);
				else
					JOptionPane.showMessageDialog(null, "No component is selected.", "VersionChecker", 1);;
 			}
 		});
 
 		clonePanel.add(fetchButton);
 		clonePanel.add(Box.createRigidArea(new Dimension(50, 0)));
 		clonePanel.add(cloneButton);
 
 		mainPanel.add(clonePanel);
 
 		// Display the window.
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 }
