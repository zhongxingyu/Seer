 package net.managers;
 
 import net.Manager;
 import state.ManagerType;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 
 @SuppressWarnings("serial")
 public class KitManager extends Manager implements ActionListener{
 	
 	CardLayout cl;
 	GridBagLayout gbl;
 	GridBagConstraints c;
 	JPanel createPanel, existingPanel, editPanel, existingButtonPanel;
 	JLabel createKit, createKitName, createKitContents, createChoosePart, existingKit, existingKitName, 
 		editKit, editKitName, editKitChoice, editKitContents, editChoosePart;
 	JButton createSaveButton, existingRemoveButton, existingEditButton, editSaveButton, seeExistingButton, newConfigButton;
 	JComboBox createPartsBox, editPartsBox;
 	JScrollPane existingKitsPane;
 	JTextField createKitText;
 	JList existingKitsList;
 	ArrayList<JButton> createContentButtons, existingContentButtons;
 	ArrayList<String> partNameList, existingKitNameList;
 	String[] s = {"Dora", "Nemo"};
 
 	public KitManager() {
 		super(ManagerType.Kit);
 		// TODO Auto-generated constructor stub\
 		
 		cl = new CardLayout();
 		setLayout(cl);
 		gbl = new GridBagLayout();
 		c = new GridBagConstraints();
 		
 		setSize(600, 600);
 		setVisible(true);
 		
 		createPanel = new JPanel(gbl);
 		existingPanel = new JPanel(gbl);
 		editPanel = new JPanel(gbl);
 		createPanel.setSize(600, 600);
 		existingPanel.setSize(600, 600);
 		editPanel.setSize(600, 600);
 		existingButtonPanel = new JPanel(gbl);
 		
 		createKit = new JLabel("Create Kit Configuration");
 		createKitName = new JLabel("Name of Kit: ");
 		createKitContents = new JLabel("Kit Contents");
 		createChoosePart = new JLabel("Current Part:");
 		existingKit = new JLabel("Existing Kits");
 		existingKitName = new JLabel("Names of Kits:");
 		editKit = new JLabel("Edit Existing Kit");
 		editKitName = new JLabel("Current Kit: ");
 		editKitChoice = new JLabel("None");
 		editKitContents = new JLabel("Kit Contents: ");
 		editChoosePart = new JLabel("Current Part: ");
 		
 		createSaveButton = new JButton("Save");
 		existingRemoveButton = new JButton("Remove Kit");
 		existingEditButton = new JButton("Edit Kit");
 		editSaveButton = new JButton("Save");
 		seeExistingButton = new JButton("See Existing Configs");
 		newConfigButton = new JButton("Create New Config");
 		
 		createSaveButton.addActionListener(this);
 		existingRemoveButton.addActionListener(this);
 		existingEditButton.addActionListener(this);
 		editSaveButton.addActionListener(this);
 		seeExistingButton.addActionListener(this);
 		newConfigButton.addActionListener(this);
 		
 		
 		existingKitsList = new JList(s);
 		existingKitsPane = new JScrollPane(existingKitsList);
 		
 		createKitText = new JTextField(20);		
 		
 		createContentButtons = new ArrayList<JButton>();
 		existingContentButtons = new ArrayList<JButton>();
 		partNameList = new ArrayList<String>();
 		existingKitNameList = new ArrayList<String>();
 		
 		
 		c.insets = new Insets(10, 10, 10, 10);
 		c.gridx = 3; c.gridy = 0;
 		createPanel.add(createKit, c);
 		c.gridx = 2; c.gridy = 2;
 		createPanel.add(createKitName, c);
 		c.gridx = 3; c.gridy = 2;
 		createPanel.add(createKitText, c);
 		c.gridx = 3; c.gridy = 3;
 		createPanel.add(createKitContents, c);
 		for (int y = 0; y < 3; y++)
 		{
 			for (int x = 0; x < 3; x++)
 			{
 				JButton b = new JButton(new ImageIcon("Empty.png"));
 				b.addActionListener(this);
 				c.gridx = x + 2; c.gridy = y + 4;
 				c.ipadx = 20; c.ipady = 20;
 				createPanel.add(b, c);
 				createContentButtons.add(b);
 			}
 		}
 		c.ipadx = 0; c.ipady = 0;
 		c.gridx = 2; c.gridy = 7;
 		createPanel.add(createChoosePart, c);
 		c.fill = c.HORIZONTAL;
 		c.gridx = 3; c.gridy = 7;
 		String[] parts = new String[partNameList.size() + 1];
 		for (int x = 1; x <= partNameList.size(); x++)
 		{
 			parts[x] = partNameList.get(x);
 		}
 		parts[0] = "None";
 		createPartsBox = new JComboBox<String>(parts);
 		editPartsBox = new JComboBox<String>(parts);
 		createPanel.add(createPartsBox, c);
 		c.gridx = 4; c.gridy = 8;
 		createPanel.add(createSaveButton, c);
 		c.gridx = 3; c.gridy = 8;
 		c.fill = c.NONE;
 		c.anchor = c.LINE_END;
 		createPanel.add(seeExistingButton, c);
 		
 		c.gridx = 2; c.gridy = 0;
 		c.fill = c.NONE;
 		c.anchor = c.CENTER;
 		existingPanel.add(existingKit, c);
 		c.gridx = 2; c.gridy = 1;
 		c.anchor = c.LINE_START;
 		existingPanel.add(existingKitName, c);
 		c.gridx = 2; c.gridy = 2;
 		c.ipadx = 200; c.ipady = 50;
 		existingPanel.add(existingKitsPane, c);
 		c.ipadx = 0; c.ipady = 0;
 		c.gridx = 0; c.gridy = 0;
 		existingButtonPanel.add(existingRemoveButton, c);
 		c.anchor = c.LINE_END;
 		c.gridx = 4; c.gridy = 0;
 		existingButtonPanel.add(existingEditButton, c);
 		c.gridx = 2; c.gridy = 3;
 		existingPanel.add(existingButtonPanel, c);
 		
 		c = new GridBagConstraints();
 		c.insets = new Insets(10, 10, 10, 10);
 		c.gridx = 3; c.gridy = 0;
 		editPanel.add(editKit, c);
 		c.gridx = 2; c.gridy = 2;
 		editPanel.add(editKitName, c);
 		c.gridx = 3; c.gridy = 2;
 		editPanel.add(editKitChoice, c);
 		c.gridx = 3; c.gridy = 3;
 		editPanel.add(editKitContents, c);
 		for (int y = 0; y < 3; y++)
 		{
 			for (int x = 0; x < 3; x++)
 			{
 				JButton b = new JButton(new ImageIcon("Empty.png"));
 				b.addActionListener(this);
 				c.gridx = x + 2; c.gridy = y + 4;
 				c.ipadx = 20; c.ipady = 20;
 				editPanel.add(b, c);
 				existingContentButtons.add(b);
 			}
 		}
 		c.ipadx = 0; c.ipady = 0;
 		c.gridx = 2; c.gridy = 7;
 		editPanel.add(editChoosePart, c);
 		c.fill = c.HORIZONTAL;
 		c.gridx = 3; c.gridy = 7;
 		/*parts = new String[partNameList.size()];
 		for (int x = 0; x < partNameList.size(); x++)
 		{
 			parts[0] = partNameList.get(x);
 		}*/
 		editPartsBox = new JComboBox<String>(parts);
 		editPartsBox = new JComboBox<String>(parts);
 		editPanel.add(editPartsBox, c);
 		c.gridx = 4; c.gridy = 8;
 		editPanel.add(editSaveButton, c);
 		c.gridx = 3; c.gridy = 8;
 		c.fill = c.NONE;
 		c.anchor = c.LINE_END;
 		editPanel.add(newConfigButton, c);
 		
 		
 		add(createPanel, "Create");
 		add(existingPanel, "Existing");
 		add(editPanel, "Edit");
 		
		cl.show(this.getContentPane(), "Create");
 	}
 
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		
 		KitManager km = new KitManager();
 
 	}
 	
 	
 	public void actionPerformed(ActionEvent ae)
 	{
 		if (ae.getSource() == createSaveButton)
 		{
 			//create new config to save here, not sure what to do though
 			//serialize?
 			System.out.println("Saved config");
 		}
 		else if (ae.getSource() == existingRemoveButton)
 		{
 			//remove a config somehow
 			String name = s[existingKitsList.getSelectedIndex()];
 			System.out.println("Removed config");
 		}
 		else if (ae.getSource() == existingEditButton)
 		{
 			if (existingKitsList.getSelectedIndex() > -1)
 			{
 				String name = s[existingKitsList.getSelectedIndex()];
 				editKitChoice.setText(name);
 			}
 			else
 				editKitChoice.setText("None");
			cl.show(this.getContentPane(), "Edit");
 			System.out.println("Moving to edit");
 		}
 		else if (ae.getSource() == editSaveButton)
 		{
 			//save an existing config
 			System.out.println("Saved existing");
 		}
 		else if (ae.getSource() == seeExistingButton)
 		{
			cl.show(this.getContentPane(), "Existing");
 			System.out.println("Moving to existing");
 		}
 		else if (ae.getSource() == newConfigButton)
 		{
			cl.show(this.getContentPane(), "Create");
 			System.out.println("Moving to create");
 		}
 		
 	}
 
 }
