 package edu.cs319.client.customcomponents;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.border.EmptyBorder;
 
 /**
  * 
  * @author Amelia Gee
  *
  */
 public class JDocTabPanel extends JPanel {
 	
 	private JList sectionList;
 	private JPanel sectionPanel;
 	private JSplitPane wholePane;
 	private JSplitPane workspace;
 	private JEditorPane documentPane;
 	private JEditorPane workPane;
 	private JButton sectionUpButton;
 	private JButton sectionDownButton;
 	
 	public JDocTabPanel() {
 		sectionList = new JList();
 		documentPane = new JEditorPane();
 		workPane = new JEditorPane();
 		sectionUpButton = new JButton("Move Up");
 		sectionDownButton = new JButton("Move Down");
 		setUpAppearance();
 		setUpListeners();
 	}
 	
 	private void setUpAppearance() {
 		setLayout(new BorderLayout());
 		setBorder(new EmptyBorder(10, 10, 10, 10));
 		
//		sectionList.setMinimumSize(new Dimension(150, 100));
//		workPane.setMinimumSize(new Dimension(50, 75));
		
 		JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
 		buttonPanel.add(sectionUpButton, BorderLayout.NORTH);
 		buttonPanel.add(sectionDownButton, BorderLayout.SOUTH);
 		sectionPanel = new JPanel(new BorderLayout(10, 10));
 		sectionPanel.add(sectionList, BorderLayout.CENTER);
 		sectionPanel.add(buttonPanel, BorderLayout.SOUTH);
//		add(sectionPanel, BorderLayout.WEST);
//		add(workspace, BorderLayout.CENTER);
 		
 		workspace = new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentPane, workPane);
 		wholePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sectionPanel, workspace);
 		
 		workspace.setDividerLocation(250);
 		workspace.setOneTouchExpandable(true);
		wholePane.setDividerLocation(200);
 		wholePane.setOneTouchExpandable(true);
 		
 		add(wholePane, BorderLayout.CENTER);
 	}
 	
 	private void setUpListeners() {
 		sectionUpButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		sectionDownButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 	
 	public JList getList() {
 		return sectionList;
 	}
 	
 	public JEditorPane getDocPane() {
 		return documentPane;
 	}
 	
 	public JEditorPane getWorkPane() {
 		return workPane;
 	}
 	
 }
