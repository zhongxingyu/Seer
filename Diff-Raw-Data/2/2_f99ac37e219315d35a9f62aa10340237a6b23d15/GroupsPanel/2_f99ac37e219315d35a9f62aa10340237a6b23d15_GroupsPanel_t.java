 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * Panel with groups
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 class GroupsPanel extends JPanel implements ActionListener {
 	static final long serialVersionUID = 0;
 	private final Database db = new Database();
 	private final DefaultListModel listModel = new DefaultListModel();
 	private final JList list = new JList(listModel);
 
 	/**
 	 * Constructor
 	 *
 	 * @param listSelectionListener Listener to handle actions outside goups panel
 	 */
 	public GroupsPanel(ListSelectionListener listSelectionListener) {
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		JLabel label = new JLabel("Groups");
 		label.setAlignmentX(CENTER_ALIGNMENT);
 		add(label);
 		fillList();
 		list.setSelectedIndex(0);
 		list.addListSelectionListener(listSelectionListener);
 		JScrollPane listScrollPane = new JScrollPane(list);
 		add(listScrollPane);
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
 		JButton add = new JButton(new ImageIcon(getClass().getResource("/res/plus.png"), "+"));
 		add.setActionCommand("addGroup");
 		add.addActionListener(this);
 		buttons.add(add);
 		JButton remove = new JButton(new ImageIcon(getClass().getResource("/res/minus.png"), "-"));
 		remove.setActionCommand("removeGroup");
 		remove.addActionListener(this);
 		buttons.add(remove);
 		add(buttons);
 	}
 
 	/**
 	 * Fills list with groups from db
 	 */
 	private void fillList() {
 		listModel.clear();
 		listModel.addElement(new Group("My Contacts"));
 		for (Group g : db.getAllGroups()) {
 			listModel.addElement(g);
 		}
 		list.setSelectedIndex(0);
 	}
 
 	/**
 	 * Listener for actions (add/remove groups)
 	 *
 	 * @param e Action
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if ("addGroup".equals(e.getActionCommand())) {
 			addGroup();
 		} else if ("removeGroup".equals(e.getActionCommand())) {
 			removeGroups();
 		}
 		fillList();
 	}
 
 	/**
 	 * Function for adding group
 	 */
 	private void addGroup() {
 		//System.out.println("addGroup");
 		String name = (String) JOptionPane.showInputDialog(
 			this,
 			"Group name:",
 			"Add group",
 			JOptionPane.QUESTION_MESSAGE,
 			new ImageIcon(getClass().getResource("/res/plus.png"), "+"),
 			null,
 			""
 		);
		if (name != null && !name.isEmpty()) {
 			//System.out.println(name);
 			db.addNewGroup(name);
 		}
 	}
 
 	/**
 	 * Function for removing group
 	 */
 	private void removeGroups() {
 		Group[] groups = Arrays.copyOf(list.getSelectedValues(), list.getSelectedValues().length, Group[].class);
 		//System.out.println("Remove groups: " + Arrays.toString(groups));
 		db.removeGroups(Arrays.asList(groups));
 	}
 }
