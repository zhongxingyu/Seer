 package client.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import common.GroupI;
 import common.UserI;
 import common.UserListI;
 
 class GroupSettings extends JPanel implements ActionListener {
 
 	private JLabel groupName, title;
 	private JButton newGroup, save, cancel;
 	private JTextField groupNameTF;
 	private JList personList;
 	private JComboBox groupsCB;
 	private JScrollPane personListScroller;
 	private GroupI group;
 	private UserListI userList;
 	private DefaultListModel model;
 	private DefaultListSelectionModel selModel;
 	private ArrayList<UserI> arrayUserList;
 
 	GridBagConstraints gbc = new GridBagConstraints();
 
 	public GroupSettings() {
 		title = new JLabel();
 		title.setText("Gruppeinnstillinger");
 
 		groupName = new JLabel();
 		groupName.setText("Gruppe navn");
 
 		groupNameTF = new JTextField(20);
 
 		groupsCB = new JComboBox();
 
 		newGroup = new JButton();
 		newGroup.setText("Ny gruppe");
 		newGroup.addActionListener(this);
 
 		save = new JButton();
 		save.setText("Lagre");
 		save.addActionListener(this);
 
 		cancel = new JButton();
 		cancel.setText("Avbryt");
 		cancel.addActionListener(this);
 
		model = new DefaultListModel();
 
 		selModel = new DefaultListSelectionModel();
 		selModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
 		personList = new JList(model);
 		personList.setSelectionModel(selModel);
 
 		personListScroller = new JScrollPane(personList);
 
 		try {
 			userList = MainClass.sServer.userStorage.getUserList(group);
 			arrayUserList = userList.getList();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 
 		for (int i = 0; i < arrayUserList.size(); i++) {
 			try {
 				String user = arrayUserList.get(i).getName();
 				if (user.equals("")) {
 					model.addElement("'Nameless user'");
 				} else {
 					model.addElement(user);
 				}
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		setLayout(new GridBagLayout());
 		gbc.gridy = 0;
 		gbc.gridx = 1;
 		add(title, gbc);
 
 		gbc.gridx = 0;
 		gbc.gridy = 1;
 		add(groupsCB, gbc);
 
 		gbc.gridx = 1;
 		gbc.gridy = 1;
 		add(newGroup, gbc);
 
 		gbc.gridx = 0;
 		gbc.gridy = 2;
 		add(groupName, gbc);
 
 		gbc.gridx = 1;
 		add(groupNameTF, gbc);
 
 		gbc.gridy = 3;
 		personListScroller
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		add(personListScroller, gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 0;
 		add(save, gbc);
 		gbc.gridx = 1;
 		add(cancel, gbc);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().toString().equals("Lagre")) {
 			try {
 				if (group == null) {
 					save(null);
 				} else {
 					save(MainClass.sServer.groupStorage.get(group.getGroupID()));
 				}
 			} catch (RemoteException e1) {
 				e1.printStackTrace();
 			}
 			clearFields();
 			MainClass.loginOK();
 		} else if (e.getActionCommand().toString().equals("Avbryt")) {
 			clearFields();
 			MainClass.loginOK();
 		} else if (e.getActionCommand().toString().equals("Ny gruppe")) {
 			clearFields();
 			groupNameTF.setText("");
 		}
 	}
 
 	private void save(GroupI g) throws RemoteException {
 		if (g == null) {
 			g = MainClass.sServer.groupStorage.create();
 		}
 		editGroup(g);
 	}
 
 	private void clearFields() {
 		groupsCB.setSelectedIndex(-1);
 		groupNameTF.setText("");
 	}
 
 	private void editGroup(GroupI g) throws RemoteException {
 		g.setGroupName(groupName.getText());
 	}
 }
