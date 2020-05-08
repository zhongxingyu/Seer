 package editor;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.EOFException;
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
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 
 import configuration.Configuration;
 import configuration.Property;
 import configuration.Usage;
 import editor.abilitiesstrategies.AbilityEditor;
 import editor.abilitiesstrategies.StrategyEditor;
 import editor.animations.AnimationEditor;
 import editor.animations.FileCopy;
 import editor.mapEditor.MapEditor;
 import editor.mapitems.MapItemEditor;
 import editor.race.RaceEditor;
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
 	public static final String AMIMATION_FOLDER = "resources/images";
 	public static final String SOUND_FOLDER = "resources/sounds";
 	
 	public enum ConfigType {
 		race, animation, ability, gate, tech, map, unit,
 		projectile, building, part, turret, collisionStrategy, icon,
 		impactStrategy, turretStrategy, combatStrategy, movementStrategy
 	}
 	
 	private JFrame frame;
 	private JTabbedPane tabPanel;
 	
 	private List<ConfigurationEditor> editors;
 	
 	public static class BFGSavedData implements Serializable  {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 3043537751663143683L;
 		private HashMap<ConfigType, List<Configuration>> masterList = new HashMap<BigFrameworkGuy.ConfigType, List<Configuration>>();
 		private HashMap<Integer, Configuration> loadedConfigs = new HashMap<Integer, Configuration>();
 		private String saveFile;
 		
 		public boolean addConfigToList(ConfigType t, Configuration c)
 		{
 			masterList.get(t).add(c);
 			return this.saveData();
 		}
 		
 		@SuppressWarnings("unchecked")
 		public void setConfigHashMap(HashMap<ConfigType, List<Configuration>> newMap)
 		{
 			masterList = (HashMap<ConfigType, List<Configuration>>) newMap.clone();
 			this.saveData();
 		}
 		
 		public List<Configuration> getConfigsOfType(ConfigType t)
 		{
 			if(masterList.get(t) == null)
 				masterList.put(t, new ArrayList<Configuration>());
 			return masterList.get(t);
 		}
 		
 		public void setConfigForEditor(ConfigurationEditor ce, Configuration c)
 		{
 			loadedConfigs.put(ce.hashCode(), c);
 			this.saveData();
 		}
 		
 		public Configuration getConfigForEditor(ConfigurationEditor ce)
 		{
 			return loadedConfigs.get(ce.hashCode());
 		}
 		
 		private boolean saveData()
 		{
 			if(saveFile != null)
 			{
 				try {
 					FileCopy.copy(saveFile, saveFile + ".bak");
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 				for(Entry<ConfigType, List<Configuration>> e : masterList.entrySet())
 				{
 					for(Configuration c : e.getValue())
 					{
 						c.setPropertyForName("bfgType", new Property(Usage.IMMUTABLE, e.getKey()));
 					}
 				}
 				
 				ObjectOutputStream oos = null;
 				try {
 					oos = new ObjectOutputStream(new FileOutputStream(saveFile));
 					oos.writeObject(this);
 					oos.flush();
 					oos.close();
 				} catch (Exception e2) {
 					try {
 						FileCopy.copy(saveFile + ".bak", saveFile);
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					e2.printStackTrace();
 					return false;
 				}
 					
 				return true;
 			}
 			else
 				return false;
 		}
 		
 //		private BFGSavedData(String file) throws FileNotFoundException, IOException
 //		{
 //			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
 //			
 //			while(true)
 //			{
 //				Object o = null;
 //				try {
 //					o = ois.readObject();
 //				} catch (ClassNotFoundException e) {
 //					e.printStackTrace();
 //					continue;
 //				} catch (EOFException e) {
 //					break;
 //				}
 //				
 //				if(o instanceof Configuration)
 //				{
 //					Configuration c = (Configuration)o;
 //					ConfigType type = (ConfigType)c.getPropertyForName("bfgType").getValue();
 //					if(masterList.get(type) == null)
 //						masterList.put(type, new ArrayList<Configuration>());
 //					masterList.get(type).add(c);
 //				}
 //				else if(o instanceof HashMap<?, ?>)
 //					loadedConfigs = (HashMap<Integer, Configuration>) o;
 //				else
 //					System.err.println("Unrecognized object:" + o);
 //			}
 //			
 //			ois.close();
 //		}
 		
 		private BFGSavedData() {}
 		
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
//		String[] editors = {"Map", "Race", "Tech", "Map Item", "Ability", "Animation", "Strategy"};
		String[] editors = {"Map", "Tech", "Map Item", "Ability", "Animation", "Strategy"};
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
 			else if (e.equals("Race"))
 				ce = new RaceEditor(this);
 			else if (e.equals("Strategy"))
 				ce = new StrategyEditor(this);
 			//TODO add an if statement for new editors here
 			
 			tabPanel.addTab(e + " Editor", ce.getPanel());
 			this.editors.add(ce);
 			
 			//make sure a config is loaded for this editor
 //			if(saveData.loadedConfigs.get(ce.hashCode()) == null)
 //				saveData.loadedConfigs.put(ce.hashCode(), ce.instantiateNewConfiguration());
 			
 			if(ce.getPanel().getPreferredSize().getWidth() > prefferedSize.getWidth())
 				prefferedSize.setSize(ce.getPanel().getPreferredSize().getWidth(), prefferedSize.getHeight());
 			if(ce.getPanel().getPreferredSize().getHeight() > prefferedSize.getHeight())
 				prefferedSize.setSize(prefferedSize.getWidth(), ce.getPanel().getPreferredSize().getHeight());
 		}
 		
 		//set up the button panel
 		JButton newer = new JButton("New");
 		newer.addActionListener(new NewButtonListener());
 		JButton load = new JButton("Load");
 		load.addActionListener(new LoadButtonListener());
 		JButton save = new JButton("Save");
 		save.addActionListener(new SaveButtonListener());
 		JButton saveAs = new JButton("Save as");
 		saveAs.addActionListener(new SaveAsButtonListener());
 		JButton delete = new JButton("Delete Configs");
 		delete.addActionListener(new DeleteButtonListener());
 		JButton export = new JButton("Export");
 		export.addActionListener(new ExportButtonListener());
 		
 		JPanel buttonPanel = new JPanel();
 		
 		buttonPanel.add(export);
 		buttonPanel.add(newer);
 		buttonPanel.add(load);
 		buttonPanel.add(save);
 		buttonPanel.add(saveAs);
 		buttonPanel.add(delete);		
 		
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
 			editors.get(i).resetEditor();
 			Configuration c = editors.get(i).instantiateNewConfiguration();
 			saveData.setConfigForEditor(editors.get(i), c);
 			editors.get(i).getPanel().validate();
 			editors.get(i).getPanel().updateUI();
 		}
 	}
 	
 	private class LoadButtonListener implements ActionListener {
 		
 		private class ConfigWrapper {
 			private Configuration c;
 			public ConfigWrapper(Configuration c) {
 				this.c = c;
 			}
 			public String toString()
 			{
 				return (String) c.getPropertyForName("bfgName").getValue();
 			}
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int i = tabPanel.getSelectedIndex();
 			
 			List<Configuration> configs = getConfigurationsByType(editors.get(i).getAllLoadableTypes());
 			ConfigWrapper[] names = new ConfigWrapper[configs.size()];
 			for(int j = 0; j < configs.size(); j++)
 				names[j] = new ConfigWrapper(configs.get(j));
 			ConfigWrapper s = (ConfigWrapper) JOptionPane.showInputDialog(frame,
 					"Please select a configuration to load", "Load",
 					JOptionPane.PLAIN_MESSAGE, null, names, null);
 			if(s == null)
 				return;
 			for(int j = 0; j < configs.size(); j++)
 				if(s == names[j])
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
 			if(saveData.getConfigForEditor(editors.get(i)) != null &&
 					saveData.getConfigForEditor(editors.get(i)).getPropertyNames().contains("bfgName"))
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
 			
 			boolean success;
 			if(!found)
 				success = saveData.addConfigToList(type, saveData.getConfigForEditor(ce));
 			else
 				success = saveData.saveData();
 			
 			if(success)
 				JOptionPane.showMessageDialog(frame,
 				    "Save successfull.", 
 				    "Message", JOptionPane.INFORMATION_MESSAGE);
 			else
 				JOptionPane.showMessageDialog(frame,
 					    "Save failed.", 
 					    "Message", JOptionPane.ERROR_MESSAGE);
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
 			if(s == null)
 				return;
 			saveData.setConfigForEditor(ce, ce.instantiateNewConfiguration());
 			saveData.getConfigForEditor(ce).setPropertyForName("bfgName", new Property(Usage.STRING, s));
 			
 			new SaveButtonListener().save(ce);
 		}
 		
 	}
 	
 	private class ExportButtonListener implements ActionListener {
 		
 		private JList list;
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			
 			final ConfigType type = (ConfigType) JOptionPane.showInputDialog(frame,
 					"Please select a type of configuration to export", "Export",
 					JOptionPane.PLAIN_MESSAGE, null, ConfigType.values(), null);
 			
 			List<ConfigType> types = new ArrayList<BigFrameworkGuy.ConfigType>();
 			types.add(type);
 			list = showMultiSelectionBox(types, "Export", "Export", new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					JFileChooser fc = new JFileChooser();
 					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 					if(fc.showOpenDialog(frame) == JFileChooser.CANCEL_OPTION)
 						return;
 					
 					List<Integer> selectedIndices = new ArrayList<Integer>();
 					for(int i : list.getSelectedIndices())
 						selectedIndices.add(i);
 					
 					List<Configuration> listItems = saveData.getConfigsOfType(type);
 					List<String> writtenNames = new ArrayList<String>();
 					for(int i = 0; i < listItems.size(); i++)
 					{
 						if(selectedIndices.contains(i))
 						{
 							String name = (String) listItems.get(i).getPropertyForName("bfgName").getValue();
 							while(writtenNames.contains(name))
 								name += "_";
 							try {
 								ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fc.getSelectedFile(), name + ".cfg")));
 								oos.writeObject(listItems.get(i));
 								oos.flush();
 								oos.close();
 							} catch (FileNotFoundException e1) {
 								e1.printStackTrace();
 							} catch (IOException e1) {
 								e1.printStackTrace();
 							}
 							writtenNames.add(name);
 						}
 							
 					}					
 				}
 			});
 			
 		}
 	}
 	
 	private class DeleteButtonListener implements ActionListener
 	{
 		
 		private JFrame deleteFrame = null;
 		private JList list;
 		private ConfigurationWrapper[] listItems;
 		
 		private int typeMaxWidth = 0;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			showDeleteBox();
 		}
 		
 		public void showDeleteBox()
 		{
 			List<ConfigType> types = new ArrayList<BigFrameworkGuy.ConfigType>();
 			for(ConfigType type : ConfigType.values())
 				types.add(type);
 			listItems = createConfigList(types);
 			list = showMultiSelectionBox(types, "Delete", "Delete", new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					removeSelected();
 				}
 			});
 		}
 		
 		private void removeSelected()
 		{
 			List<Integer> selectedIndices = new ArrayList<Integer>();
 			for(int i : list.getSelectedIndices())
 				selectedIndices.add(i);
 			
 			HashMap<ConfigType, List<Configuration>> newMap = new HashMap<ConfigType, List<Configuration>>();
 			for(int i = 0; i < listItems.length; i++)
 			{
 				if(!selectedIndices.contains(i))
 				{
 					if(newMap.get(listItems[i].type) == null)
 						newMap.put(listItems[i].type, new ArrayList<Configuration>());
 					newMap.get(listItems[i].type).add(listItems[i].config);
 				}
 					
 			}
 			
 			saveData.setConfigHashMap(newMap);
 				
 		}
 		
 	}
 	
 	private ConfigurationWrapper[] createConfigList(List<ConfigType> list)
 	{
 		List<ConfigurationWrapper> ret = new ArrayList<ConfigurationWrapper>();
 		for(ConfigType type : list)
 		{
 			for(Configuration c : saveData.getConfigsOfType(type))
 				ret.add(new ConfigurationWrapper(c, type));
 		}
 		return ret.toArray(new ConfigurationWrapper[ret.size()]);
 	}
 	
 	private int typeMaxWidth = 0;
 	
 	private JList showMultiSelectionBox(List<ConfigType> configs, String frameTitle, String mainButton, final ActionListener callback)
 	{
 		final JFrame frame = new JFrame(frameTitle);
 		
 		typeMaxWidth = 0;
 		JList list = new JList();
 		list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
 		JScrollPane scroller = new JScrollPane(list);
 		scroller.setPreferredSize(new Dimension(350, 450));
 		ConfigurationWrapper[] listItems = createConfigList(configs);
 		list.setListData(listItems);
 		
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(scroller, BorderLayout.CENTER);
 		
 		JPanel buttonPanel = new JPanel();
 		JButton delete = new JButton(mainButton);
 		delete.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				callback.actionPerformed(e);
 				frame.dispose();
 			}
 		});
 		
 		JButton cancel = new JButton("Cancel");
 		cancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 		});
 		
 		buttonPanel.add(delete);
 		buttonPanel.add(cancel);
 		
 		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
 		frame.setContentPane(mainPanel);
 		
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(true);
 		
 		return list;
 	}
 	
 	private class ConfigurationWrapper
 	{			
 		private Configuration config;
 		private ConfigType type;
 		
 		public ConfigurationWrapper(Configuration c, ConfigType t)
 		{
 			this.config = c;
 			this.type = t;
 			if(t.toString().length() > typeMaxWidth)
 				typeMaxWidth = t.toString().length();
 		}
 		
 		public String toString() {
 			String typeName = type.toString();
 			while(typeName.length() < typeMaxWidth)
 				typeName = " " + typeName;
 			return typeName + " :: " + ((String) config.getPropertyForName("bfgName").getValue());
 		}
 	}
 }
