 package org.janusproject.demos.meetingscheduler.gui;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 public class mainFrame extends JFrame implements ActionListener,
 		ListSelectionListener {
 
 	private static final long serialVersionUID = 3830079646720453065L;
 	private ActionListener listener;
 
 	private JList agentNameList;
 
 	public mainFrame() {
 		Container contentPane = this.getContentPane();
 		contentPane.setLayout(new BoxLayout(contentPane,
 				getDefaultCloseOperation()));
 		this.setSize(400, 500);
 		this.setLocation(100, 100);
 
 		JLabel welcomeLabel = new JLabel(
 				"Welcome to Janus MeetingScheduler demo");
 		JLabel instructionLabel = new JLabel("Add participants to start");
 		JButton addParticipantButton = new JButton("Add Participant");
 		addParticipantButton.setActionCommand("ADDPARTICIPANT");
 		addParticipantButton.addActionListener(this);
 
 		agentNameList = new JList();
 		agentNameList.setSize(400, 200);
 		agentNameList.addListSelectionListener(this);
 
 		JScrollPane scrollPane = new JScrollPane(agentNameList);
 
 		// this.pack();
 		this.add(welcomeLabel);
 		this.add(instructionLabel);
 		this.add(addParticipantButton);
 		this.add(scrollPane);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent evt) {
 		String cmd = evt.getActionCommand();
 		if (cmd == "ADDPARTICIPANT") {
 			String agent_name = addParticipantPrompt();
 			if(agent_name!=null){
				MeetingSchedulor.getInstance().addAgent(agent_name);
 				updateList();
 			}
 		}
 	}
 
 	public String addParticipantPrompt() {
 		JFrame frame = new JFrame("Add Participant Dialog");
 		String name = JOptionPane.showInputDialog(frame, "What's your name?");
 		return name;
 	}
 
 	public void updateList() {
 		agentNameList.removeAll();
 		DefaultListModel dlm = new DefaultListModel();
 		for (String elem : MeetingSchedulor.getInstance().getAllAgents()) {
 			dlm.addElement(elem);
 		}
 		agentNameList.setModel(dlm);
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		JList list = (JList) e.getSource();
 		int selections[] = list.getSelectedIndices();
 		Object selectionValues[] = list.getSelectedValues();
 		for (int i = 0, n = selections.length; i < n; i++) {
 			System.out.println((String)selectionValues[i]);
 			MeetingSchedulor.getInstance().showAgentFrame((String)selectionValues[i]);
 		}
 	}
 
 }
