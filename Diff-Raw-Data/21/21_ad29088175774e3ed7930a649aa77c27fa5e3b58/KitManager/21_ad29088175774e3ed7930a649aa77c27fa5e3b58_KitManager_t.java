 package net.managers;
 
 import net.Manager;
 import state.ManagerType;
 import state.PartConfig;
 import state.KitConfig;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 @SuppressWarnings("serial")
 public class KitManager extends Manager implements ActionListener{
 	
 	CardLayout cl;
 	GridBagLayout gbl;
 	GridBagConstraints c;
 	JPanel createPanel, existingPanel, editPanel, existingButtonPanel;
 	JLabel createKit, createKitName, createKitContents, createChoosePart, existingKit, existingKitName, 
 		editKit, editKitName, editKitChoice, editKitContents, editChoosePart;
 	JButton createSaveButton, createPartImageButton, existingRemoveButton, existingEditButton, editSaveButton, editPartImageButton, seeExistingButton, newConfigButton;
 	JComboBox createPartsBox, editPartsBox;
 	JScrollPane existingKitsPane;
 	JTextField createKitText;
 	JList existingKitsList;
 	ArrayList<JButton> createContentButtons, existingContentButtons;
 	ArrayList<String> partNameList, existingKitNameList, createContentsList;
 	List<PartConfig> partConfigs;
 	List<KitConfig> kitConfigs;
 	String[] partNames, partImages, kitNames;
 	ImageIcon partImage = new ImageIcon("gfx/blank.png");
 	Map<String, Integer> map = new TreeMap<String, Integer>();
 	Map<Integer, String> config = new TreeMap<Integer, String>();
 	String key;
 	int kitIndex;
 	
 
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
 		createPartImageButton = new JButton(partImage);
 		existingRemoveButton = new JButton("Remove Kit");
 		existingEditButton = new JButton("Edit Kit");
 		editSaveButton = new JButton("Save");
 		editPartImageButton = new JButton(partImage);
 		seeExistingButton = new JButton("See Existing Configs");
 		newConfigButton = new JButton("Create New Config");
 		
 		createSaveButton.addActionListener(this);
 		existingRemoveButton.addActionListener(this);
 		existingEditButton.addActionListener(this);
 		editSaveButton.addActionListener(this);
 		seeExistingButton.addActionListener(this);
 		newConfigButton.addActionListener(this);
 		
 		createKitText = new JTextField(10);		
 		
 		createContentButtons = new ArrayList<JButton>();
 		existingContentButtons = new ArrayList<JButton>();
 		partNameList = new ArrayList<String>();
 		existingKitNameList = new ArrayList<String>();
 		createContentsList = new ArrayList<String>();
 		partConfigs = Collections.synchronizedList(new ArrayList<PartConfig>());
 		kitConfigs = new CopyOnWriteArrayList<KitConfig>();
		for (int x = 1; x < 10; x++)
		{
			config.put(x, "gfx/blank.png");
		}
 		
 		loadConfigData();
 		this.outMap.put("KitConfig", kitConfigs);
 		partConfigs = (List<PartConfig>)inMap.get((String)"PartConfig");
 
 		partNames = new String[partConfigs.size() + 1];
 		partImages = new String[partConfigs.size() + 1];
 		kitNames = new String[kitConfigs.size()];
 		
 		for (int x = 0; x < partConfigs.size(); x ++)
 		{
 			partNames[x+1] = partConfigs.get(x).getPartName();
 			partImages[x+1] = partConfigs.get(x).getPartName();
 		}
 		for (int x = 0; x < kitConfigs.size(); x++)
 		{
 			kitNames[x] = kitConfigs.get(x).kitName;
 			System.out.println(kitConfigs.size());
 		}
 		
 		existingKitsList = new JList(kitNames);
 		existingKitsPane = new JScrollPane(existingKitsList);
 		
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
 				JButton b = new JButton(new ImageIcon("gfx/blank.png"));
 				b.addActionListener(this);
 				c.gridx = x + 2; c.gridy = y + 4;
 				c.ipadx = 10; c.ipady = 10;
 				c.fill = c.NONE;
 				if (c.gridx == 2)
 					c.anchor = c.LINE_END;
 				else if (c.gridx == 3)
 					c.anchor = c.CENTER;
 				else
 					c.anchor = c.LINE_START;
 				if (createContentButtons.size() < 8)
 				{
 					createPanel.add(b, c);
 					createContentButtons.add(b);
 				}
 			}
 		}
 		c.ipadx = 0; c.ipady = 0;
 		c.gridx = 2; c.gridy = 7;
 		createPanel.add(createChoosePart, c);
 		c.fill = c.HORIZONTAL;
 		c.gridx = 3; c.gridy = 7;
 		partNames[0] = "None";
 		createPartsBox = new JComboBox(partNames);
 		editPartsBox = new JComboBox(partNames);
 		createPartsBox.addActionListener(this);
 		editPartsBox.addActionListener(this);
 		createPanel.add(createPartsBox, c);
 		c.gridx = 4; c.gridy = 7;
 		createPanel.add(createPartImageButton, c);
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
 				JButton b = new JButton(new ImageIcon("gfx/blank.png"));
 				b.addActionListener(this);
 				c.gridx = x + 2; c.gridy = y + 4;
 				c.ipadx = 10; c.ipady = 10;
 				c.fill = c.NONE;
 				if (c.gridx == 2)
 					c.anchor = c.LINE_END;
 				else if (c.gridx == 3)
 					c.anchor = c.CENTER;
 				else
 					c.anchor = c.LINE_START;
 				if (existingContentButtons.size() < 8)
 				{
 					editPanel.add(b, c);
 					existingContentButtons.add(b);
 				}
 			}
 		}
 		c.ipadx = 0; c.ipady = 0;
 		c.gridx = 2; c.gridy = 7;
 		editPanel.add(editChoosePart, c);
 		c.fill = c.HORIZONTAL;
 		c.gridx = 3; c.gridy = 7;
 		editPanel.add(editPartsBox, c);
 		c.gridx = 4; c.gridy = 7;
 		editPanel.add(editPartImageButton, c);
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
 		//km.loadData("data/partConfigList.ser");
 
 	}
 	
 	public void saveConfigData(String path)
 	{
 		try
 		{
 			FileOutputStream fout = new FileOutputStream(path);
 			ObjectOutputStream oout = new ObjectOutputStream(fout);
 			oout.writeObject(kitConfigs);
 			fout.close();
 			oout.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public void loadConfigData()
 	{
 		try {
 			FileInputStream fileIn = new FileInputStream("data/kitConfigList.ser");
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			kitConfigs = (List<KitConfig>) in.readObject();
 			in.close();
 			fileIn.close();
 			kitNames = new String[kitConfigs.size()];
 			for (int x = 0; x < kitConfigs.size(); x++)
 			{
 				kitNames[x] = kitConfigs.get(x).kitName;
 			}
 		} catch (IOException i) {
 			i.printStackTrace();
 		} catch (ClassNotFoundException c) {
 			System.out.println("Failed to find kit configs");
 			c.printStackTrace();
 		}
 	}
 	
 	public void loadPartData(String path) {
 		try {
 			FileInputStream fileIn = new FileInputStream(path);
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			partConfigs = (CopyOnWriteArrayList<PartConfig>) in.readObject();
 			in.close();
 			fileIn.close();
 			partNames = new String[partConfigs.size() + 1];
 			partImages = new String[partConfigs.size() + 1];
 			partNames[0] = "None";
 			partImages[0] = "blank.png";
 			for (int x = 1; x < partConfigs.size(); x++)
 			{
 				partNames[x] = partConfigs.get(x).getPartName();
 				partImages[x] = partConfigs.get(x).getImageFile();
 			}
 		} catch (IOException i) {
 			i.printStackTrace();
 		} catch (ClassNotFoundException c) {
 			System.out.println("GUI Part class not found");
 			c.printStackTrace();
 		}
 	}
 	
 	public void actionPerformed(ActionEvent ae)
 	{
 		for (int x = 0; x < createContentButtons.size(); x++)
 		{
 			c = new GridBagConstraints();
 			if (ae.getSource() == createContentButtons.get(x))
 			{
 				createContentButtons.get(x).setIcon(createPartImageButton.getIcon());
 				config.put(x, createPartImageButton.getIcon().toString());
 				System.out.println(config.get(x) + x);
 			}
 			else if (ae.getSource() == existingContentButtons.get(x))
 			{
 				existingContentButtons.get(x).setIcon(editPartImageButton.getIcon());
 				config.put(x, createPartImageButton.getIcon().toString());
 			}
 		}
 		if (ae.getSource() == createSaveButton)
 		{
 			existingPanel.remove(existingKitsPane);
 			String s = createKitText.getText();
 			if (s.equals(""))
 			{
 				s = "null";
 			}
 			Map<String, Integer> m = new TreeMap<String, Integer>();
 			for (int x = 0; x < createContentButtons.size(); x++)
 			{
 				String key = createContentButtons.get(x).getIcon().toString().substring(4, createContentButtons.get(x).getIcon().toString().length() - 4);
 				if (m.containsKey(key))
 				{
 					m.put(key, m.get(key) + 1);
 				}
 				else
 				{
 					m.put(key, 1);
 				}
 			}
 			boolean found = false;
 			for (int x = 0; x < kitConfigs.size(); x++)
 			{
 				if (kitConfigs.get(x).kitName.equals(s))
 				{
 					kitConfigs.get(x).components = m;
 					kitConfigs.get(x).setConfiguration(config);
 					found = true;
 				}
 			}
 			if (!found)
 			{
 				KitConfig k = new KitConfig(s, m);
 				k.setConfiguration(config);
 				kitConfigs.add(k);
 				
 			}
 			System.out.println("Saved config");
 			
 			kitNames = new String[kitConfigs.size()];
 			for (int x = 0; x < kitConfigs.size(); x++)
 			{
 				kitNames[x] = kitConfigs.get(x).kitName;
 				System.out.println(kitConfigs.size());
 			}
 			existingKitsList = new JList(kitNames);
 			existingKitsPane = new JScrollPane(existingKitsList);
 			
 			c.gridx = 2; c.gridy = 2;
 			c.ipadx = 200; c.ipady = 50;
 			existingPanel.add(existingKitsPane, c);
 			saveConfigData("data/kitConfigList.ser");
 		}
 		else if (ae.getSource() == existingRemoveButton)
 		{
 			//remove a config somehow
 			if (existingKitsList.getSelectedIndex() > -1)
 			{
 				kitConfigs.remove(existingKitsList.getSelectedIndex());
 				existingPanel.remove(existingKitsPane);
 				kitNames = new String[kitConfigs.size()];
 				for (int x = 0; x < kitConfigs.size(); x++)
 				{
 					kitNames[x] = kitConfigs.get(x).kitName;
 					System.out.println(kitConfigs.size());
 				}
 				existingKitsList = new JList(kitNames);
 				existingKitsPane = new JScrollPane(existingKitsList);
 				this.saveConfigData("data/kitConfigList.ser");
 				c.gridx = 2; c.gridy = 2;
 				c.ipadx = 200; c.ipady = 50;
 				existingPanel.add(existingKitsPane, c);
 				existingPanel.repaint();
 				this.repaint();
 			}
 			System.out.println("Removed config");
 			System.out.println("Kits in list: " + kitConfigs.size());
 			this.repaint();
 			cl.show(this.getContentPane(), "Existing");
 			
 		}
 		else if (ae.getSource() == existingEditButton)
 		{
			System.out.println("Existing edit button");
			for (int x = 0; x < 10; x++)
			{
				System.out.println(kitConfigs.get(existingKitsList.getSelectedIndex()).configuration.get(x));
			}
 			if (existingKitsList.getSelectedIndex() > -1)
 			{
 				kitIndex = existingKitsList.getSelectedIndex();				
 				String name = kitNames[kitIndex];
 				editKitChoice.setText(name);
 				if (editKitChoice.getText().equals(kitConfigs.get(kitIndex).kitName))
 				{
 					for (int y = 0; y < existingContentButtons.size(); y++)
 					{
 						System.out.println(kitConfigs.get(kitIndex).configuration.get(y));
 						existingContentButtons.get(y).setIcon(new ImageIcon(kitConfigs.get(kitIndex).configuration.get(y)));
 						config.put(y, kitConfigs.get(kitIndex).configuration.get(y));
 					}
 				}
 			
 				
 			}
 			else
 				editKitChoice.setText("None");
 			cl.show(this.getContentPane(), "Edit");
 			System.out.println("Moving to edit");
 		}
 		else if (ae.getSource() == editSaveButton)
 		{
 			String s = editKitChoice.getText();
 			if (s.equals(""))
 			{
 				s = "null";
 			}
 			Map<String, Integer> m = new TreeMap<String, Integer>();
 			for (int x = 0; x < existingContentButtons.size(); x++)
 			{
 				String key = existingContentButtons.get(x).getIcon().toString().substring(4, existingContentButtons.get(x).getIcon().toString().length() - 4);
 				if (m.containsKey(key))
 				{
 					m.put(key, m.get(key) + 1);
 				}
 				else
 				{
 					m.put(key, 1);
 				}
 			}
 			kitConfigs.get(kitIndex).components = m;
 			kitConfigs.get(kitIndex).setConfiguration(config);
 			System.out.println("Saved config");
 			saveConfigData("data/kitConfigList.ser");
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
 		else if (ae.getSource() == createPartsBox)
 		{
 			if (partConfigs.size() > 0)
 			{
 				if (createPartsBox.getSelectedIndex() - 1 > -1)
 				{
 					partImage = new ImageIcon(new String("gfx/" + partConfigs.get((createPartsBox.getSelectedIndex()) - 1).getImageFile()));
 					createPartImageButton.setIcon(partImage);
 					System.out.println(partConfigs.get(createPartsBox.getSelectedIndex() - 1).getImageFile());
 				}
 				else 
 				{
 					partImage = new ImageIcon("blank.png");
 					createPartImageButton.setIcon(partImage);
 				}
 			}
 		}
 		else if (ae.getSource() == editPartsBox)
 		{
 			if (partConfigs.size() > 0)
 			{
 				if (editPartsBox.getSelectedIndex() - 1 > -1)
 				{
 					partImage = new ImageIcon(new String("gfx/" + partConfigs.get(createPartsBox.getSelectedIndex()).getImageFile()));
 					editPartImageButton.setIcon(partImage);
 				}
 				else
 				{
 					partImage = new ImageIcon("blank.png");
 					createPartImageButton.setIcon(partImage);
 				}
 			}
 		}
 		
 		
 		
 		this.outMap.put("KitConfig", kitConfigs);
 		partConfigs = (List<PartConfig>)inMap.get((String)"PartConfig");
 		this.repaint();
 	}
 
 }
