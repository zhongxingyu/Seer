 package editor;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 import configuration.Configuration;
 import configuration.Property;
 import configuration.Usage;
 import editor.abilitiesstrategies.AbilityEditor;
 import editor.animations.AnimationEditor;
 import editor.mapEditor.MapEditor;
 import editor.mapitems.MapItemEditor;
 import editor.race.RaceEditor;
 import editor.tech.FunctionEditor;
 import editor.tech.TechEditor;
 
 /**
  * 
  * @author Connor Schenck
  *
  *The purpose of this class is to contain all the panels of the individual
  *editors. It handles knowing all the valid types of units, maps, etc
  *for the other editors to use as well as it handles dealing with the
  *file structure directly. It also handles exporting game files to a 
  *specified directory.
  */
 public class BigFrameworkGuy
 {
 	private static final String MASTER_LIST_URI = "resources/masterList.cfg";
 	private static final String AMIMATION_FOLDER = "resources/animations";
 	
 	public enum ConfigType {
 		race, animation, ability, gate, tech, map, unit,
 		projectile, building, part, turret, collisionStrategy, icon,
 		impactStrategy, turretStrategy, combatStrategy, movementStrategy
 	}
 	
 	private JFrame frame;
 	private JTabbedPane tabPanel;
 	
 	private List<ConfigurationEditor> editors;
 	
 	public static class BFGSavedData implements Serializable {
 		private HashMap<ConfigType, List<Configuration>> masterList = new HashMap<BigFrameworkGuy.ConfigType, List<Configuration>>();
 		private HashMap<Integer, Configuration> loadedConfigs = new HashMap<Integer, Configuration>();
 		private String saveFile;
 		
 		public void addConfigToList(ConfigType t, Configuration c)
 		{
 			this.saveData();
 			masterList.get(t).add(c);
 		}
 		
 		public List<Configuration> getConfigsOfType(ConfigType t)
 		{
 			return masterList.get(t);
 		}
 		
 		public void setConfigForEditor(ConfigurationEditor ce, Configuration c)
 		{
 			this.saveData();
 			loadedConfigs.put(ce.hashCode(), c);
 		}
 		
 		public Configuration getConfigForEditor(ConfigurationEditor ce)
 		{
 			return loadedConfigs.get(ce.hashCode());
 		}
 		
 		private void saveData()
 		{
 			if(saveFile != null)
 			{
 				try {
 					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
 					oos.writeObject(this);
 					oos.flush();
 					oos.close();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		public static BFGSavedData getSavedData(String file) throws FileNotFoundException, IOException, ClassNotFoundException
 		{
 			return (BFGSavedData) new ObjectInputStream(
 					new FileInputStream(new File(file)))
 					.readObject();
 		}
 	}
 	
 	private BFGSavedData  saveData;
 	
 	/**
 	 * Constructs the entire editor. Creating an instance of BigFrameworkGuy
 	 * will cause the editor window to appear and all the editors in it. There
 	 * is no need to call any initialize methods after calling this constructor.
 	 * 
 	 * @throws FileNotFoundException
 	 * @throws InvalidConfigFileException
 	 */
 	@SuppressWarnings("unchecked")
 	public BigFrameworkGuy()
 	{
 		try {
 			saveData = BFGSavedData.getSavedData(MASTER_LIST_URI);
 		} catch(Exception e) {
 			JOptionPane.showMessageDialog(frame,
 				    "Error:" + e.getMessage() +"\nCreating empty master list.",
 				    "Error finding the master list.",
 				    JOptionPane.ERROR_MESSAGE);
 			saveData = new BFGSavedData();
 			saveData.saveFile = MASTER_LIST_URI;
 		}
 		
 		tabPanel = new JTabbedPane();
 		frame = new JFrame("Line Wars Data Editor");
 		
 		editors = new ArrayList<ConfigurationEditor>();
 		
 		//add each editor
 		
 		AnimationEditor toStart = new AnimationEditor(AMIMATION_FOLDER); 
 		
 		Dimension prefferedSize = new Dimension(0, 0);
 		//TODO add a string for new editors here
		String[] editors = {"Map", "Race", "Tech", "Map Item", "Ability", "Animation"};
 		for(String e : editors)
 		{
 			ConfigurationEditor ce = null;
 			if(e.equals("Map Item"))
 				ce = new MapItemEditor(this, AMIMATION_FOLDER);
 			else if(e.equals("Ability"))
 				ce = new AbilityEditor(this);
 			else if(e.equals("Animation"))
 				ce = toStart;
 			else if(e.equals("Map"))
 				ce = new MapEditor(frame);
 			else if(e.equals("Tech"))
 				ce = new TechEditor(this);
 			else if(e.equals("Function"))
 				ce = new FunctionEditor();
 			else if (e.equals("Race"))
 				ce = new RaceEditor(this);
 			//TODO add an if statement for new editors here
 			
 			tabPanel.addTab(e + " Editor", ce.getPanel());
 			this.editors.add(ce);
 			
 			//make sure a config is loaded for this editor
 			if(saveData.loadedConfigs.get(ce.hashCode()) == null)
 				saveData.loadedConfigs.put(ce.hashCode(), ce.instantiateNewConfiguration());
 			
 			if(ce.getPanel().getPreferredSize().getWidth() > prefferedSize.getWidth())
 				prefferedSize.setSize(ce.getPanel().getPreferredSize().getWidth(), prefferedSize.getHeight());
 			if(ce.getPanel().getPreferredSize().getHeight() > prefferedSize.getHeight())
 				prefferedSize.setSize(prefferedSize.getWidth(), ce.getPanel().getPreferredSize().getHeight());
 		}
 		
 		//set up the button panel
 		JButton export = null;
 		if(new File("bin").exists()) //only allow exporting if this is not a release version
 		{
 			export = new JButton("Export");
 			export.addActionListener(new ExportButtonListener());
 		}
 		JButton newer = new JButton("New");
 		newer.addActionListener(new NewButtonListener());
 		JButton load = new JButton("Load");
 		load.addActionListener(new LoadButtonListener());
 		JButton save = new JButton("Save");
 		save.addActionListener(new SaveButtonListener());
 		JButton saveAs = new JButton("Save as");
 		saveAs.addActionListener(new SaveAsButtonListener());
 		JPanel buttonPanel = new JPanel();
 		if(export != null)
 			buttonPanel.add(export);
 		buttonPanel.add(newer);
 		buttonPanel.add(load);
 		buttonPanel.add(save);
 		buttonPanel.add(saveAs);
 		
 		
 		//set up the main panel
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(tabPanel, BorderLayout.CENTER);
 		mainPanel.add(buttonPanel, BorderLayout.NORTH);
 		
 		//set up the frame
 		frame.add(mainPanel);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setMinimumSize(prefferedSize);
 		frame.setVisible(true);
 		
 		//can't start the animation editors display loop until the panel is added to the frame
 		new Thread(toStart).run();
 		
 	}
 	
 	public List<Configuration> getConfigurationsByType(List<ConfigType> types)
 	{
 		List<Configuration> ret = new ArrayList<Configuration>();
 		for(ConfigType t : types)
 			ret.addAll(saveData.getConfigsOfType(t));
 		return ret;
 	}
 	
 	public List<Configuration> getConfigurationsByType(ConfigType type)
 	{
 		List<ConfigType> types = new ArrayList<BigFrameworkGuy.ConfigType>();
 		types.add(type);
 		return getConfigurationsByType(types);
 	}
 	
 	public List<Configuration> getAllConfigurations()
 	{
 		List<ConfigType> types = new ArrayList<BigFrameworkGuy.ConfigType>();
 		for(ConfigType t : ConfigType.values())
 			types.add(t);
 		return getConfigurationsByType(types);
 	}
 	
 	/**
 	 * This is the main method for running the editors as a standalone application
 	 * 
 	 * @param args
 	 * @throws FileNotFoundException
 	 * @throws InvalidConfigFileException
 	 */
 	public static void main(String[] args) {
 		new BigFrameworkGuy();
 	}
 	
 	private class NewButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int i = tabPanel.getSelectedIndex();
 			Configuration c = editors.get(i).instantiateNewConfiguration();
 			saveData.setConfigForEditor(editors.get(i), c);
 			editors.get(i).getPanel().validate();
 			editors.get(i).getPanel().updateUI();
 		}
 	}
 	
 	private class LoadButtonListener implements ActionListener {
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int i = tabPanel.getSelectedIndex();
 			
 			List<Configuration> configs = getConfigurationsByType(editors.get(i).getAllLoadableTypes());
 			String[] names = new String[configs.size()];
 			for(int j = 0; j < configs.size(); j++)
 				names[j] = (String) configs.get(j).getPropertyForName("bfgName").getValue();
 			String s = (String) JOptionPane.showInputDialog(frame,
 					"Please select a configuration to load", "Load",
 					JOptionPane.PLAIN_MESSAGE, null, names, null);
 			for(int j = 0; j < configs.size(); j++)
 				if(s.equals(names[j]))
 				{
 					editors.get(i).setData(configs.get(j));
 					saveData.setConfigForEditor(editors.get(i), configs.get(j));
 					break;
 				}
 		}
 	}
 	
 	private class SaveButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int i = tabPanel.getSelectedIndex();
 			if(saveData.getConfigForEditor(editors.get(i)).getPropertyNames().contains("bfgName"))
 				save(editors.get(i));
 			else
 				new SaveAsButtonListener().promptAndSave(editors.get(i));	
 		}
 		
 		public void save(ConfigurationEditor ce)
 		{
 			ConfigType type = ce.getData(saveData.getConfigForEditor(ce));
 			boolean found = false;
 			for(Configuration c : saveData.getConfigsOfType(type))
 				found |= (c == saveData.getConfigForEditor(ce));
 			
 			if(!found)
 				saveData.addConfigToList(type, saveData.getConfigForEditor(ce));
 			else
 				saveData.saveData();
 			
 			JOptionPane.showMessageDialog(frame,
 				    "Save successfull.", 
 				    "Message", JOptionPane.INFORMATION_MESSAGE);	
 		}
 		
 	}
 	
 	private class SaveAsButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			promptAndSave(editors.get(tabPanel.getSelectedIndex()));			
 		}
 		
 		public void promptAndSave(ConfigurationEditor ce)
 		{
 			String s = (String) JOptionPane
 					.showInputDialog(frame, "Name to use in the editor:", "Name Dialog",
 							JOptionPane.PLAIN_MESSAGE, null, null, "");
 			saveData.getConfigForEditor(ce).setPropertyForName("bfgName", new Property(Usage.STRING, s));
 			
 			new SaveButtonListener().save(ce);
 		}
 		
 	}
 	
 	private class ExportButtonListener implements ActionListener {
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			
 			Object[] options = { "Map", "Race" };
 			int n = JOptionPane.showOptionDialog(frame,
 					"What would you like to export?", "Export",
 					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
 					null, // do not use a custom Icon
 					options, // the titles of buttons
 					options[0]); // default button title
 			
 			ConfigType type;
 			if(n == 0)
 				type = ConfigType.map;
 			else
 				type = ConfigType.race;
 			
 			List<Configuration> configs = getConfigurationsByType(type);
 			String[] names = new String[configs.size()];
 			for(int i = 0; i < configs.size(); i++)
 				names[i] = i + ": " + ((String)configs.get(i).getPropertyForName("bfgName").getValue());
 			
 			String s = (String) JOptionPane.showInputDialog(frame,
 					"Please select a configuration to export",
 					"Configuratin Selection", JOptionPane.PLAIN_MESSAGE, null,
 					names, null);
 			Scanner scanner = new Scanner(s);
 			scanner.useDelimiter(":");
 			Configuration c = configs.get(scanner.nextInt());
 			
 			JFileChooser fc = new JFileChooser("");
 			if(fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
 			{
 				File f = fc.getSelectedFile();
 				try {
 					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
 					oos.writeObject(c);
 					oos.flush();
 					oos.close();
 				} catch (FileNotFoundException e1) {
 					e1.printStackTrace();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				
 			}
 			
 		}
 		
 		
 	}
 	
 	
 }
