 
 package gui.panels.subcontrolpanels;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import gui.panels.ControlPanel;
 
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 
 import shared.SaveConfiguration;
 
 /**
  * The GlassSelectPanel class contains buttons allowing the user to select what
  * type of glass to produce.
  */
 @SuppressWarnings("serial")
 public class GlassSelectPanel extends JPanel implements ActionListener
 {
 	/** The ControlPanel this is linked to */
 	private ControlPanel parent;
 
 	GridBagConstraints gbc = new GridBagConstraints();
 	JComboBox settingSelect = new JComboBox();
 	JPanel settingPanel = new JPanel();
 	JPanel checkBoxPanel = new JPanel();
 	JPanel quantityPanel = new JPanel();
 	JLabel quantityLabel = new JLabel("Number of pieces:");
 	JLabel settingLabel = new JLabel("Setting Name:");
 	JTextField quantityField = new JTextField(10);
 	JTextField settingNameField = new JTextField(10);
 	JButton acceptButton = new JButton("Set Current");
 	JButton saveButton = new JButton("Save Setting");
 	
 	JPanel[] partPanel = new JPanel[15];
 	JLabel[] partLabels = new JLabel[15];
 	JCheckBox[] partCheckBoxes = new JCheckBox[15];
 	
 	boolean [] setting = new boolean[15];
 	
 	
 	
 	/**
 	 * Creates a new GlassSelect and links it to the control panel
 	 * @param cp
 	 *        the ControlPanel linked to it
 	 */
 	public GlassSelectPanel(ControlPanel cp)
 	{
 		parent = cp;
 		
 		//initialize setting select combobox
 		settingSelect.addItem("Default");
 		loadConfigurations();
 		addSettingSelectListener();
 		
 		//initialize labels
 		partLabels[0] = new JLabel("Cutter");
 		partLabels[2] = new JLabel("Breakout");
 		partLabels[3] = new JLabel("Manual Breakout");
 		partLabels[3].setHorizontalAlignment(SwingConstants.LEFT);
 		partLabels[3].setHorizontalTextPosition(SwingConstants.LEFT);
 		partLabels[3].setFont(new Font("Dialog",Font.PLAIN, 11));
 		partLabels[5] = new JLabel("Drill");
 		partLabels[6] = new JLabel("Cross Seamer");
 		partLabels[7] = new JLabel("Grinder");
 		partLabels[8] = new JLabel("Washer");
 		partLabels[10] = new JLabel("Painter");
 		partLabels[11] = new JLabel("UV Light");
 		partLabels[13] = new JLabel("Oven");
 		
 		//initialize checkboxes and panel creation
 	
 		partCheckBoxes[0] = new JCheckBox();
 		partCheckBoxes[2] = new JCheckBox();
 		partCheckBoxes[3] = new JCheckBox();
 		partCheckBoxes[5] = new JCheckBox();
 		partCheckBoxes[6] = new JCheckBox();
 		partCheckBoxes[7] = new JCheckBox();
 		partCheckBoxes[8] = new JCheckBox();
 		partCheckBoxes[10] = new JCheckBox();
 		partCheckBoxes[11] = new JCheckBox();
 		partCheckBoxes[13] = new JCheckBox();
 		
 		setting[0] = false;
 		setting[2] = false;
 		setting[3] = false;
 		setting[5] = false;
 		setting[6] = false;
 		setting[7] = false;
 		setting[8] = false;
 		setting[10] = false;
 		setting[11] = false;
 		setting[13] = false;
 		
 		partCheckBoxes[0].addActionListener(this);
 		partCheckBoxes[2].addActionListener(this);
 		partCheckBoxes[3].addActionListener(this);
 		partCheckBoxes[5].addActionListener(this);
 		partCheckBoxes[6].addActionListener(this);
 		partCheckBoxes[7].addActionListener(this);
 		partCheckBoxes[8].addActionListener(this);
 		partCheckBoxes[10].addActionListener(this);
 		partCheckBoxes[11].addActionListener(this);
 		partCheckBoxes[13].addActionListener(this);
 		
 		partPanel[0] = new JPanel();
 		partPanel[2] = new JPanel();
 		partPanel[3] = new JPanel();
 		partPanel[5] = new JPanel();
 		partPanel[6] = new JPanel();
 		partPanel[7] = new JPanel();
 		partPanel[8] = new JPanel();
 		partPanel[10] = new JPanel();
 		partPanel[11] = new JPanel();
 		partPanel[13] = new JPanel();
 		
 		partPanel[0].add(partLabels[0]);
 		partPanel[2].add(partLabels[2]);
 		partPanel[3].add(partLabels[3]);
 		partPanel[5].add(partLabels[5]);
 		partPanel[6].add(partLabels[6]);
 		partPanel[7].add(partLabels[7]);
 		partPanel[8].add(partLabels[8]);
 		partPanel[10].add(partLabels[10]);
 		partPanel[11].add(partLabels[11]);
 		partPanel[13].add(partLabels[13]);
 		
 		partPanel[0].add(partCheckBoxes[0]);
 		partPanel[2].add(partCheckBoxes[2]);
 		partPanel[3].add(partCheckBoxes[3]);
 		partPanel[5].add(partCheckBoxes[5]);
 		partPanel[6].add(partCheckBoxes[6]);
 		partPanel[7].add(partCheckBoxes[7]);
 		partPanel[8].add(partCheckBoxes[8]);
 		partPanel[10].add(partCheckBoxes[10]);
 		partPanel[11].add(partCheckBoxes[11]);
 		partPanel[13].add(partCheckBoxes[13]);
 		
 		partPanel[0].setBackground(new Color(238,238,238));
 		partPanel[2].setBackground(new Color(238,238,238));
 		partPanel[3].setBackground(new Color(238,238,238));
 		partPanel[5].setBackground(new Color(238,238,238));
 		partPanel[6].setBackground(new Color(238,238,238));
 		partPanel[7].setBackground(new Color(238,238,238));
 		partPanel[8].setBackground(new Color(238,238,238));
 		partPanel[10].setBackground(new Color(238,238,238));
 		partPanel[11].setBackground(new Color(238,238,238));
 		partPanel[13].setBackground(new Color(238,238,238));
 
 		
 		
 		setting[1] = true;
 		setting[4] = true;
 		setting[9] = true;
 		setting[12] = true;
 		//Layout for panel that contains the checkboxes as well as the 'Create new setting' panel.
 		checkBoxPanel.setLayout(new GridLayout(1,3));
 		
 		//the Left-most panel contains the first 5 checkboxes
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
 		
 		//the middle panel contains the last 5 checkboxes
 		JPanel midPanel = new JPanel();
 		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
 		
 		leftPanel.add(partPanel[0]);
 		leftPanel.add(partPanel[2]);
 		leftPanel.add(partPanel[3]);
 		leftPanel.add(partPanel[5]);
 		leftPanel.add(partPanel[6]);
 		midPanel.add(partPanel[7]);
 		midPanel.add(partPanel[8]);
 		midPanel.add(partPanel[10]);
 		midPanel.add(partPanel[11]);
 		midPanel.add(partPanel[13]);
 		
 		//the right panel contains the setting name panel.
 		JPanel rightPanel = new JPanel();
 		GridBagConstraints g1 = new GridBagConstraints();
 		rightPanel.setLayout(new GridBagLayout());
 		g1.gridx = 0;
 		g1.gridy = 0;
 		rightPanel.add(settingLabel, g1);
 		settingNameField.setMaximumSize(new Dimension(100,30));
 		settingNameField.setPreferredSize(new Dimension(100,30));
 		settingNameField.setMinimumSize(new Dimension(100,30));
 		g1.gridy++;
 		rightPanel.add(settingNameField, g1);
 		g1.gridy++;
 		rightPanel.add(saveButton,g1);
 		checkBoxPanel.add(leftPanel);
 		checkBoxPanel.add(midPanel);
 		checkBoxPanel.add(rightPanel);
 		acceptButton.addActionListener(this);
 		saveButton.addActionListener(this);
 		
 		quantityPanel.add(quantityLabel);
 		quantityPanel.add(quantityField);
 		quantityPanel.add(acceptButton);
 		
 		//adding to main panel
 		setLayout(new GridBagLayout());
 		gbc.weightx = 1;
 		gbc.weighty = 1;
 		gbc.anchor = GridBagConstraints.NORTHWEST;
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.gridwidth = 2;
 		gbc.ipadx = 350;
 		add(settingSelect, gbc);
 
 		gbc.gridy++;
 		gbc.gridwidth = 1;
 		add(checkBoxPanel, gbc);
 		
 		gbc.gridwidth = 1;	
 		gbc.gridy++;
 		gbc.gridx = 0;
 		gbc.gridwidth = 2;
 		add(quantityPanel,gbc);
 	}
 	
 	private void saveConfiguration() {
 		// TODO Auto-generated method stub
 		SaveConfiguration save = new SaveConfiguration(setting);
 		save.setName(settingNameField.getText());
 		System.out.println("Creating new file: " + save.getName()+".sav");
 		File f = new File(save.getName() + ".sav");
 		try {
 			ObjectOutputStream oos  = new ObjectOutputStream(new FileOutputStream(f));
 			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("configs/configuration_list.txt", true)));
 			pw.println(save.getName());
 			pw.close();
 			System.out.println("Writing line: " + save.getName());
 			
 			oos.writeObject(save);
 			oos.close();
 		}
 		catch(IOException ioex) {
 			
 		}
 		settingSelect.addItem(save.getName());
 	}
 	
 	private void loadConfigurations() {
 		// TODO Auto-generated method stub
 		System.out.println("Loading configuration: ");
 		try {
 			FileReader fr = new FileReader("configs/configuration_list.txt");
 			BufferedReader br = new BufferedReader(fr);
 			ArrayList<String> fileList = new ArrayList<String>();
 			String line;
 			while((line = br.readLine()) != null) {
 				fileList.add(line);
 			}
 			
 			for(int i = 0; i < fileList.size(); i++) {
 				System.out.println("Adding Item: " + fileList.get(i));
 				settingSelect.addItem(fileList.get(i));
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void addSettingSelectListener() {
 		// TODO Auto-generated method stub
 		settingSelect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				JComboBox option = (JComboBox) ae.getSource();
 				
 				String selected = (String) option.getSelectedItem();
 				if(!selected.equals("Default")) {
 					FileInputStream fis;
 					ObjectInputStream ois;
 					SaveConfiguration thisLoad;
 					
 					try {
 						fis = new FileInputStream("configs/"+selected+".sav");
 						ois = new ObjectInputStream(fis);
 						thisLoad = (SaveConfiguration) ois.readObject();
 						
 						partCheckBoxes[0].setSelected(thisLoad.getConfig()[0]);
 						partCheckBoxes[2].setSelected(thisLoad.getConfig()[2]);
 						partCheckBoxes[3].setSelected(thisLoad.getConfig()[3]);
 						partCheckBoxes[5].setSelected(thisLoad.getConfig()[5]);
 						partCheckBoxes[6].setSelected(thisLoad.getConfig()[6]);
 						partCheckBoxes[7].setSelected(thisLoad.getConfig()[7]);
 						partCheckBoxes[8].setSelected(thisLoad.getConfig()[8]);
 						partCheckBoxes[10].setSelected(thisLoad.getConfig()[10]);
 						partCheckBoxes[11].setSelected(thisLoad.getConfig()[11]);
 						partCheckBoxes[13].setSelected(thisLoad.getConfig()[13]);
 					
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (ClassNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				
 				
 				
 			}
 		});
 	}
 
 	/**
 	 * Returns the parent panel
 	 * @return the parent panel
 	 */
 	public ControlPanel getGuiParent()
 	{
 		return parent;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		for(int i = 0; i < 15; i++) {
 			if(e.getSource() == partCheckBoxes[i]) {
 				if(partCheckBoxes[i].isSelected()) {
 					setting[i] = true;
 				}
 				else setting[i] = false;
 			}
 		}
 		if(e.getSource() == acceptButton) {
 			getGuiParent().getStatePanel().startButton.setEnabled(true);
 			getGuiParent().setSetting(Integer.parseInt(quantityField.getText()),setting);
 		}
 		if(e.getSource() == saveButton) {
 			saveConfiguration();
 			
 		}
 	}
 
 	
 }
