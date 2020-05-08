 package net.ivoa.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import net.ivoa.pdl.interpreter.conditionalStatement.StatementHelperContainer;
 import net.ivoa.pdl.interpreter.groupInterpreter.GroupHandlerHelper;
 import net.ivoa.pdl.interpreter.groupInterpreter.GroupProcessor;
 import net.ivoa.pdl.interpreter.utilities.Utilities;
 
 public class PDLSummaryPanel extends JPanel implements ActionListener{
 
 	private GroupProcessor groupProcessor;
 
 	private JPanel containedPanel;
 
 	public PDLSummaryPanel(GroupProcessor gp) {
 		super();
 		this.groupProcessor = gp;
 		this.updateSummary();
 
 	}
 
 	private List<String> getInfosOnGroups() {
 		List<String> toReturn = new ArrayList<String>();
 
 		toReturn.add(this.buildStringFromList(this.getGroupsToComplete()));
 		toReturn.add(this.buildStringFromList(this.getGroupsWithError()));
 		toReturn.add(this.buildStringFromList(this.getGroupsValid()));
 
 		return toReturn;
 	}
 
 	private String buildStringFromList(List<String> list) {
 		String toReturn = "";
 		for (String temp : list) {
 			toReturn = toReturn + temp + "\n";
 		}
 		return toReturn;
 	}
 
 	private List<String> getGroupsToComplete() {
 		List<GroupHandlerHelper> handler = this.groupProcessor
 				.getGroupsHandler();
 		List<String> toReturn = new ArrayList<String>();
 
 		// Loop for every group
 		for (int i = 0; i < handler.size(); i++) {
 			String currentGroupName = handler.get(i).getGroupName();
 			Boolean isGroupToComplete = true;
 			// For every statement in the current group
 			if (null != handler.get(i).getStatementHelperList()) {
 				for (StatementHelperContainer currentStatement : handler.get(i)
 						.getStatementHelperList()) {
 					if (currentStatement.isStatementSwitched()) {
 						if (null != currentStatement.isStatementValid()) {
 							isGroupToComplete = false;
 						} else {
 							isGroupToComplete = true;
 						}
					}else{
						//In the case where the statement is not switched
						isGroupToComplete = !currentStatement.isStatementValid();
 					}
 				}
 				if (isGroupToComplete) {
 					toReturn.add(currentGroupName);
 				}
 			}
 		}
 		return toReturn;
 	}
 
 	private List<String> getGroupsValid() {
 		List<String> toReturn = new ArrayList<String>();
 
 		List<GroupHandlerHelper> handler = this.groupProcessor
 				.getGroupsHandler();
 
 		// Loop for every group
 		for (int i = 0; i < handler.size(); i++) {
 			String currentGroupName = handler.get(i).getGroupName();
 
 			if (null != handler.get(i).getGroupValid()
 					&& handler.get(i).getGroupValid()) {
 				toReturn.add(currentGroupName);
 			} else {
 				Boolean isGroupValid = true;
 				// Loop for every statement in the current group
 				for (StatementHelperContainer currentStatement : handler.get(i)
 						.getStatementHelperList()) {
 					if (currentStatement.isStatementSwitched()) {
 						if (null != currentStatement.isStatementValid()) {
 							isGroupValid = isGroupValid
 									&& currentStatement.isStatementValid();
 						} else {
 							isGroupValid = false;
 						}
 
 					}
 				}
 				// The group is valid iff all the statement it contains are
 				// valid
 				if (isGroupValid) {
 					toReturn.add(currentGroupName);
 				}
 			}
 		}
 		return toReturn;
 	}
 
 	private List<String> getGroupsWithError() {
 		List<GroupHandlerHelper> handler = this.groupProcessor
 				.getGroupsHandler();
 
 		List<String> toReturn = new ArrayList<String>();
 
 		// Loop for every group
 		for (int i = 0; i < handler.size(); i++) {
 			String currentGroupName = handler.get(i).getGroupName();
 			Boolean isGroupInError = false;
 			// Loop for every statement in the current group
 			if (null != handler.get(i).getStatementHelperList()) {
 				for (StatementHelperContainer currentStatement : handler.get(i)
 						.getStatementHelperList()) {
 					if (currentStatement.isStatementSwitched()) {
 						if (null != currentStatement.isStatementValid()) {
 							isGroupInError = isGroupInError
 									|| !currentStatement.isStatementValid();
 						}
 
 					}
 				}
 				// The group is in error if at least one of statement is in
 				// error
 				if (isGroupInError) {
 					toReturn.add(currentGroupName);
 				}
 			}
 
 		}
 		return toReturn;
 	}
 
 	public void updateSummary() {
 
 		if (null != this.containedPanel) {
 			this.remove(this.containedPanel);
 		}
 
 		this.containedPanel = new JPanel(new BorderLayout());
 
 		GridLayout lay = new GridLayout(1, 2);
 		JPanel sum1 = new JPanel(lay);
 		JPanel sum2 = new JPanel(lay);
 		JPanel sum3 = new JPanel(lay);
 
 		JLabel lab1 = new JLabel("To complete");
 		JLabel lab2 = new JLabel("With error");
 		JLabel lab3 = new JLabel("Valid");
 
 		List<String> infosOnGroups = this.getInfosOnGroups();
 		JButton serverButton = new JButton("Launch computation");
 		if ((infosOnGroups.get(0) == null || infosOnGroups.get(0).equalsIgnoreCase(""))
 				&& (infosOnGroups.get(1) == null || infosOnGroups.get(1).equalsIgnoreCase(""))) {	
 			serverButton.addActionListener(this);
 			this.containedPanel.add(serverButton, BorderLayout.EAST);
 		}
 
 		sum1.add(lab1);
 		sum2.add(lab2);
 		sum3.add(lab3);
 
 		JTextArea textArea1 = new JTextArea(infosOnGroups.get(0), 4, 7);
 		textArea1.setBackground(Color.YELLOW);
 		JScrollPane scroll1 = new JScrollPane(textArea1);
 		scroll1.setVisible(true);
 		sum1.add(scroll1);
 		if (infosOnGroups.get(0).equalsIgnoreCase("")) {
 			sum1.setVisible(false);
 		} else {
 			sum1.setVisible(true);
 		}
 
 		JTextArea textArea2 = new JTextArea(infosOnGroups.get(1), 4, 7);
 		textArea2.setBackground(Color.RED);
 		JScrollPane scroll2 = new JScrollPane(textArea2);
 		sum2.add(scroll2);
 		if (infosOnGroups.get(1).equalsIgnoreCase("")) {
 			sum2.setVisible(false);
 		} else {
 			sum2.setVisible(true);
 		}
 
 		JTextArea textArea3 = new JTextArea(infosOnGroups.get(2), 4, 7);
 		textArea3.setBackground(Color.GREEN);
 		JScrollPane scroll3 = new JScrollPane(textArea3);
 		sum3.add(scroll3);
 		if (infosOnGroups.get(2).equalsIgnoreCase("")) {
 			sum3.setVisible(false);
 		} else {
 			sum3.setVisible(true);
 		}
 
 		this.containedPanel.add(sum1, BorderLayout.NORTH);
 		this.containedPanel.add(sum2, BorderLayout.CENTER);
 		this.containedPanel.add(sum3, BorderLayout.SOUTH);
 		// JTextArea textArea = new JTextArea(text,10,7);
 		// JScrollPane scroll = new JScrollPane(textArea);
 		//
 		this.containedPanel.setVisible(true);
 		this.add(containedPanel);
 		this.setVisible(true);
 		this.revalidate();
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		Utilities.getInstance().callService();
 		
 	}
 
 }
