 package org.vamdc.validator.gui.settings;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.border.TitledBorder;
 
 import org.vamdc.validator.OperationModes;
 import org.vamdc.validator.Setting;
 import org.vamdc.validator.gui.mainframe.MainFrameController;
 import org.vamdc.validator.gui.settings.FieldVerifier.Type;
 
 /**
  * Settings main panel
  * @author doronin
  *
  */
 public class SettingsPanel extends JPanel{
 	private static final long serialVersionUID = -6257407488101227895L;
 
 	public static final String CMD_SAVE="Save";
 	public static final String CMD_RESET="Reset";
 	public static final String CMD_DEFAULTS="Defaults";
 
 	private MainFrameController main;
 	private SettingsPanelController control;
 	
 	private Collection<SettingControl> fields = new ArrayList<SettingControl>();
 	private JRadioButton useNetMode,usePlugMode;
 	private JTable schemaTable;
 	private NamespaceTableModel nsTableModel;
 
 	//Button group for operation mode chooser
 	private ButtonGroup opModeGroup= new ButtonGroup();
 	
 	public SettingsPanel(MainFrameController main){
 		super();
 		this.main = main;
 		init();
 	}
 	
 	private void init(){
 		control = new SettingsPanelController(main, this);
 		initLayout();
 		loadSettings();
 		
 	}
 	
 	private void initLayout(){
 		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
 		this.add(getSchemaPanel());
 		this.add(getNetworkPanel());
 		this.add(getPluginPanel());
 		this.add(getControlPanel());
 		this.setPreferredSize(new Dimension(640,480));
 
 	}
 
 	/**
 	 * @return panel with schemaLocation input fields
 	 */
 	private JPanel getSchemaPanel(){
 		JPanel schemaPanel = new JPanel();
 		schemaPanel.setLayout(new BoxLayout(schemaPanel,BoxLayout.Y_AXIS));
 
 		setTitle(schemaPanel,"Local settings");
 
 		JPanel fieldPanel = new JPanel(new BorderLayout());
 		fieldPanel.add(new JLabel("noNamespace schema location"),BorderLayout.WEST);
 		fieldPanel.add(getTextField(Setting.SchemaFile,Type.FILE,fields),BorderLayout.CENTER);
 
 		schemaPanel.add(fieldPanel);
 
 		nsTableModel = new NamespaceTableModel();
 		schemaTable = new JTable(nsTableModel);
 		JScrollPane tablePane = new JScrollPane(schemaTable);
 		schemaPanel.add(tablePane);
 
 		JPanel tempPanel = new JPanel(new BorderLayout());
 		tempPanel.add(new JLabel("temp files location"),BorderLayout.WEST);
 		tempPanel.add(getTextField(Setting.StorageTempPath,Type.DIR,fields),BorderLayout.CENTER);
 		schemaPanel.add(tempPanel);
 
 		return schemaPanel;
 	}
 
 	/**
 	 * 
 	 * @return panel with network settings
 	 */
 	private JPanel getNetworkPanel(){
 		JPanel netPanel = new JPanel(new GridBagLayout());
 		setTitle(netPanel,"Network mode settings");
 
 		GridBagConstraints grid = new GridBagConstraints();
 		grid.fill = GridBagConstraints.HORIZONTAL;
 		
 
 		//Mode selector
 		gridNextLabel(grid);
 		netPanel.add(useNetMode = new JRadioButton("Use Network Mode"),grid);
 		opModeGroup.add(useNetMode);
 		
 		CapabilitiesField caps = new CapabilitiesField();
 		
 		addLabel(netPanel,grid,"Registry base URL");
 		gridItem(grid);
 		netPanel.add(new RegistryPanel(fields,caps),grid);
 		
 		addLabel(netPanel,grid,"VAMDC-TAP Capabilities endpoint");
 		gridItem(grid);
 		netPanel.add(caps,grid);
 		fields.add(caps);
 
 		addLabel(netPanel,grid,"VAMDC-TAP sync endpoint");
 		gridItem(grid);
 		netPanel.add(getTextField(Setting.ServiceTAPURL,Type.HTTPURL,fields),grid);
 		
 		addLabel(netPanel,grid,"TAP url suffix (EXPERT OPTION! :) )");
 		gridItem(grid);
 		netPanel.add(getTextField(Setting.ServiceTAPSuffix,Type.STRING,fields),grid);
 
 		addLabel(netPanel,grid,"HTTP CONNECT timeout");
 		gridItem(grid);
 		grid.gridwidth=1;
 		netPanel.add(getTextField(Setting.HTTP_CONNECT_TIMEOUT,Type.INT,fields),grid);
 
 		gridItem(grid);
 		netPanel.add(getCheckbox(Setting.PrettyPrint,"Input pretty-printing",fields),grid);
 		
 		addLabel(netPanel,grid,"HTTP Data timeout");
 		gridItem(grid);
 		grid.gridwidth=1;
 		netPanel.add(getTextField(Setting.HTTP_DATA_TIMEOUT,Type.INT,fields),grid);
 		
 		gridItem(grid);
 		netPanel.add(getCheckbox(Setting.UseGzip,"Transfer compression",fields),grid);
 
 		return netPanel;
 	}
 
 	private JPanel getPluginPanel(){
 		JPanel plugPanel = new JPanel(new GridBagLayout());
 		setTitle(plugPanel,"Plugin mode settings");
 
 		GridBagConstraints grid = new GridBagConstraints();
 		grid.fill = GridBagConstraints.HORIZONTAL;
 
 		//Mode selector
 		gridNextLabel(grid);
 		plugPanel.add(usePlugMode = new JRadioButton("Use Plugin Mode"),grid);
 		opModeGroup.add(usePlugMode);
 
 		addLabel(plugPanel, grid,"Plugin class name");
 		gridItem(grid);
		plugPanel.add(getTextField(Setting.PluginClass,Type.STRING,fields),grid);
 
 		addLabel(plugPanel,grid,"Unique ID prefix");
 		gridItem(grid);
 		plugPanel.add(getTextField(Setting.PluginIDPrefix,Type.STRING,fields),grid);
 
 		addLabel(plugPanel,grid,"States limit");
 		gridItem(grid);
 		plugPanel.add(getTextField(Setting.PluginLimitStates,Type.INT,fields),grid);
 
 		addLabel(plugPanel,grid,"Processes limit");
 		gridItem(grid);
 		plugPanel.add(getTextField(Setting.PluginLimitProcesses,Type.INT,fields),grid);
 
 		return plugPanel;
 	}
 
 	private static void addLabel(JPanel panel, GridBagConstraints grid, String label) {
 		gridNextLabel(grid);
 		if (label!=null && !label.isEmpty())
 			panel.add(new JLabel(label),grid);
 	}
 
 	public static SettingField getTextField(Setting option,Type type,Collection<SettingControl> fields){
 		SettingField field = new SettingField(option);
 		FieldInputHelper helper = new FieldInputHelper(type);
 		field.addActionListener(helper);
 		field.addMouseListener(helper);
 		field.setActionCommand(type.name());
 		field.setInputVerifier(new FieldVerifier(type));
 		field.setName(option.name());
 		fields.add(field);
 		return field;
 	}
 	
 	private JCheckBox getCheckbox(Setting option, String label, Collection<SettingControl> fields){
 		SettingCheckbox result = new SettingCheckbox(option,label);
 		fields.add(result);
 		return result;
 		
 	}
 	
 	/**
 	 * Set grid to next line's title
 	 * @param grid
 	 */
 	private static void gridNextLabel(GridBagConstraints grid){
 		grid.gridx=0;
 		grid.weightx = 0;
 		grid.gridy++;
 		grid.gridwidth=1;
 	}
 
 	/**
 	 * Set grid to line's item
 	 * @param grid
 	 */
 	private void gridItem(GridBagConstraints grid){
 		grid.gridx++;
 		grid.gridwidth=GridBagConstraints.REMAINDER;
 		grid.weightx = 1.0;
 	}
 
 	/**
 	 * Add border and title to panel
 	 * @param panel 
 	 * @param name Title string
 	 */
 	private void setTitle(JPanel panel,String name){
 		TitledBorder title;
 		title = BorderFactory.createTitledBorder(name);
 		title.setTitleJustification(TitledBorder.LEFT);
 		panel.setBorder(title);
 	}
 
 	private JPanel getControlPanel(){
 		JPanel cPanel = new JPanel();
 		cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.X_AXIS));
 		addButton(cPanel,SettingsPanel.CMD_DEFAULTS);
 		addButton(cPanel,SettingsPanel.CMD_RESET);
 		addButton(cPanel,SettingsPanel.CMD_SAVE);
 		return cPanel;
 	}
 
 	private void addButton(JPanel buttonPanel, String command){
 		JButton newBtn = new JButton(command);
 		newBtn.addActionListener(control);
 		buttonPanel.add(newBtn);
 	}
 
 	/**
 	 * Save settings from all fields into application-wide preferences
 	 */
 	public void saveSettings(){
 		
 		for (SettingControl field:fields){
 			field.save();
 		}
 		
 		
 		
 		String opMode = OperationModes.network.name();
 		if(usePlugMode.isSelected())
 			opMode = OperationModes.plugin.name();
 		Setting.OperationMode.setValue(opMode);
 
 		//Don't save schema locations if they haven't changed
 		String sl = nsTableModel.getNSString();
 		if (sl!=null && !sl.equals(Setting.getSchemaLoc())){
 			Setting.SchemaLocations.setValue(sl);
 		}
 		
 		Setting.save();
 		
 	}
 
 	/**
 	 * Load all field values from properties
 	 */
 	public void loadSettings(){
 		
 		Setting.load();
 		
 		for (SettingControl field:fields){
 			field.load();
 		}
 		
 		switch(OperationModes.valueOf(Setting.OperationMode.getValue())){
 		case network:
 			useNetMode.setSelected(true);
 			break;
 		case plugin:
 			usePlugMode.setSelected(true);
 			break;
 		case file:
 			useNetMode.setSelected(false);
 			usePlugMode.setSelected(false);
 			break;
 		}
 		
 		nsTableModel.setNSString(Setting.SchemaLocations.getValue());
 	}
 
 }
