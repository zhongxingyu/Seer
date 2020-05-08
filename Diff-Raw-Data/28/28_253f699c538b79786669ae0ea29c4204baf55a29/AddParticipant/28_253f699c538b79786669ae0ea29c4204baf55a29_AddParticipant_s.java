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
 import javax.swing.ListSelectionModel;
 
 import common.UserI;
 import common.UserListI;
 
 class AddParticipant extends JPanel implements ActionListener {
 
 	private JButton addP, cancel;
 	private JComboBox groups;
 	private JList list;
 	private JLabel lPart;
 	private JScrollPane scroll;
 	private DefaultListModel<UserI> model;
 	private DefaultListSelectionModel selectionModel;
 	private UserListI userList;
 	private ArrayList<UserI> arrayUserList;
 
 	GridBagConstraints g = new GridBagConstraints();
 
 	public AddParticipant() {
 		addP = new JButton("Legg Til..");
 		addP.addActionListener(this);
 
 		cancel = new JButton("Avbryt");
 		cancel.addActionListener(this);
 
 		groups = new JComboBox();
 
 		lPart = new JLabel();
 		lPart.setText("Velg flere med shift og ctrl");
 
 		selectionModel = new DefaultListSelectionModel();
 		selectionModel
 				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
		model = new DefaultListModel<>();
 
 		list = new JList();
 		list.setModel(model);
 		list.setSelectionModel(selectionModel);
 
 		scroll = new JScrollPane(list);
 
 		try {
 			userList = MainClass.sServer.userStorage.getUserList(null);
 			arrayUserList = userList.getList();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 
 		setLayout(new GridBagLayout());
 
 		g.gridx = 0;
 		g.gridy = 0;
 		add(groups, g);
 		g.gridy = 1;
 		add(scroll, g);
 		g.gridy = 2;
 		add(lPart, g);
 		g.gridy = 3;
 		add(addP, g);
 		g.gridx = 1;
 		add(cancel, g);
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 		if (arg0.getActionCommand().toString().equals("Legg Til..")) {
 			this.setVisible(false);
 			// lagre
 		} else if (arg0.getActionCommand().toString().equals("Avbryt")) {
 			this.setVisible(false);
 		}
 	}
 }
