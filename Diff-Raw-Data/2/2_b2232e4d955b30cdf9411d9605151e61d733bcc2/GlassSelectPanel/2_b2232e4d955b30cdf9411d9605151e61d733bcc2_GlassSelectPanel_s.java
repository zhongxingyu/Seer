 
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
	JComboBox<String> settingSelect = new JComboBox();
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
 		partLabels[1] = new JLabel("Conv 1");
 		partLabels[2] = new JLabel("Breakout");
 		partLabels[3] = new JLabel("Manual Breakout");
 		partLabels[3].setFont(new Font("Dialog",Font.BOLD, 11));
 		partLabels[4] = new JLabel("Conv 4");
 		partLabels[5] = new JLabel("Drill");
 		partLabels[6] = new JLabel("Cross Seamer");
 		partLabels[7] = new JLabel("Grinder");
 		partLabels[8] = new JLabel("Washer");
 		partLabels[9] = new JLabel("Conv 9");
 		partLabels[10] = new JLabel("Painter");
 		partLabels[11] = new JLabel("UV Light");
 		partLabels[12] = new JLabel("Conv 12");
 		partLabels[13] = new JLabel("Oven");
 		partLabels[14] = new JLabel("Truck");
 		
 		//initialize checkboxes and panel creation
 		for(int i = 0; i < 15; i++) {
 			partCheckBoxes[i] = new JCheckBox();
 			setting[i] = false;
 			partCheckBoxes[i].addActionListener(this);
 			partPanel[i] = new JPanel();
 			partPanel[i].add(partLabels[i]);
 			partPanel[i].add(partCheckBoxes[i]);
 			partPanel[i].setBackground(new Color(238,238,238));
 		}
 		
 		partCheckBoxes[1].setSelected(true);
 		partCheckBoxes[1].setEnabled(false);
 		setting[1] = true;
 		partCheckBoxes[4].setSelected(true);
 		partCheckBoxes[4].setEnabled(false);
 		setting[4] = true;
 		partCheckBoxes[9].setSelected(true);
 		partCheckBoxes[9].setEnabled(false);
 		setting[9] = true;
 		partCheckBoxes[12].setSelected(true);
 		partCheckBoxes[12].setEnabled(false);
 		setting[12] = true;
 		partCheckBoxes[14].setSelected(true);
 		partCheckBoxes[14].setEnabled(false);
 		//Layout for panel that contains the checkboxes as well as the 'Create new setting' panel.
 		checkBoxPanel.setLayout(new GridLayout(1,3));
 		
 		//the Left-most panel contains the first 5 checkboxes
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
 		
 		//the middle panel contains the last 5 checkboxes
 		JPanel midPanel = new JPanel();
 		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
 		for(int i = 0; i < 7; i++) {
 			leftPanel.add(partPanel[i]);
 			midPanel.add(partPanel[i+7]);
 		}
 		
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
 						for(int i = 0; i < 15; i++) {
 							partCheckBoxes[i].setSelected(thisLoad.getConfig()[i]);
 						}
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
