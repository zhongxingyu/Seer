 package editor;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 
 import editor.ListURISelector.ListSelectorOptions;
 import editor.URISelector.SelectorOptions;
 import editor.abilities.AbilityEditor;
 import editor.animations.AnimationEditor;
 import editor.animations.FileCopy;
 import editor.mapEditor.MapEditor;
 import editor.mapitems.MapItemEditor;
 import editor.tech.FunctionEditor;
 import editor.tech.TechEditor;
 
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ConfigFileReader;
 import linewars.configfilehandler.ConfigFileReader.InvalidConfigFileException;
 import linewars.configfilehandler.ConfigFileWriter;
 import linewars.configfilehandler.ParserKeys;
 
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
 	
 	private ConfigData masterList;
 	
 	private JFrame frame;
 	private JTabbedPane tabPanel;
 	
 	private List<ConfigurationEditor> editors;
 	private HashMap<ConfigurationEditor, String> loadedURIs = new HashMap<ConfigurationEditor, String>();
 	
 	/**
 	 * Constructs the entire editor. Creating an instance of BigFrameworkGuy
 	 * will cause the editor window to appear and all the editors in it. There
 	 * is no need to call any initialize methods after calling this constructor.
 	 * 
 	 * @throws FileNotFoundException
 	 * @throws InvalidConfigFileException
 	 */
 	public BigFrameworkGuy() throws FileNotFoundException, InvalidConfigFileException
 	{
 		masterList = new ConfigFileReader(MASTER_LIST_URI).read();
 		
 		tabPanel = new JTabbedPane();
 		frame = new JFrame("Line Wars Data Editor");
 		
 		editors = new ArrayList<ConfigurationEditor>();
 		
 		//add each editor
 		
 		String imagesFolder = new File(this.getAnimationURIs()[0]).getParentFile().getAbsolutePath();
 		AnimationEditor toStart = new AnimationEditor(imagesFolder); 
 		
 		Dimension prefferedSize = new Dimension(0, 0);
 		//TODO add a string for new editors here
 		String[] editors = {"Map", "Race", "Tech", "Map Item", "Ability", "Animation"};
 		for(String e : editors)
 		{
 			ConfigurationEditor ce = null;
 			if(e.equals("Map Item"))
 				ce = new MapItemEditor(this);
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
 	
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete command centers (just
 	 * 			returns the list of buildings).
 	 */
 	public String[] getCommandCenterURIs()
 	{
 		return masterList.getStringList(ParserKeys.buildingURI).toArray(new String[0]);
 	}
 	
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete units.
 	 */
 	public String[] getUnitURIs()
 	{
 		return masterList.getStringList(ParserKeys.unitURI).toArray(new String[0]);
 	}
 	
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete buildings.
 	 */
 	public String[] getBuildingURIs()
 	{
 		return masterList.getStringList(ParserKeys.buildingURI).toArray(new String[0]);
 	}
 	
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete techs.
 	 */
 	public String[] getTechURIs()
 	{
		System.out.println(masterList.getStringList(ParserKeys.techURI));
 		return masterList.getStringList(ParserKeys.techURI).toArray(new String[0]);
 	}
 	
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete gates.
 	 */
 	public String[] getGateURIs()
 	{
 		return masterList.getStringList(ParserKeys.gateURI).toArray(new String[0]);
 	}
 
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete projectiles.
 	 */
 	public String[] getProjectileURIs() {
 		return masterList.getStringList(ParserKeys.projectileURI).toArray(new String[0]);
 	}
 
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete animations.
 	 */
 	public String[] getAnimationURIs() {
 		return masterList.getStringList(ParserKeys.animationURI).toArray(new String[0]);
 	}
 
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete abilities.
 	 */
 	public String[] getAbilityURIs() {
 		return masterList.getStringList(ParserKeys.abilityURI).toArray(new String[0]);
 	}
 	
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete maps.
 	 */
 	public String[] getMapURIs() {
 		return masterList.getStringList(ParserKeys.mapURI).toArray(new String[0]);
 	}
 	
 	/**
 	 * 
 	 * @return	A list of URIs of all the valid, complete races.
 	 */
 	public String[] getRaceURIs() {
 		return masterList.getStringList(ParserKeys.raceURI).toArray(new String[0]);
 	}
 	
 	/**
 	 * This is the main method for running the editors as a standalone application
 	 * 
 	 * @param args
 	 * @throws FileNotFoundException
 	 * @throws InvalidConfigFileException
 	 */
 	public static void main(String[] args) throws FileNotFoundException, InvalidConfigFileException {
 		new BigFrameworkGuy();
 	}
 	
 	private class NewButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int i = tabPanel.getSelectedIndex();
 			editors.get(i).reset();
 			editors.get(i).getPanel().validate();
 			editors.get(i).getPanel().updateUI();
 			if(loadedURIs.get(editors.get(i)) != null)
 				loadedURIs.remove(editors.get(i));
 		}
 	}
 	
 	private class LoadButtonListener implements ActionListener {
 		
 		private int selected;
 		private String[] list;
 		private JFrame loadFrame;
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			selected = tabPanel.getSelectedIndex();
 			List<String> uris = new ArrayList<String>();
 			//map item editor is a special case
 			if(editors.get(selected) instanceof MapItemEditor)
 			{
 				ParserKeys[] keyList = {ParserKeys.unitURI, ParserKeys.buildingURI, ParserKeys.projectileURI, ParserKeys.gateURI};
 				
 				for(ParserKeys key : keyList)
 				{
 					if(masterList.getDefinedKeys().contains(key))
 						uris.addAll(masterList.getStringList(key));
 					if(masterList.getConfig(ParserKeys.incomplete).getDefinedKeys().contains(key))
 						uris.addAll(masterList.getConfig(ParserKeys.incomplete).getStringList(key));
 				}
 			}
 			else
 			{
 				ParserKeys key = editors.get(selected).getType();
 				if(masterList.getDefinedKeys().contains(key))
 					uris.addAll(masterList.getStringList(key));
 				if(masterList.getConfig(ParserKeys.incomplete).getDefinedKeys().contains(key))
 					uris.addAll(masterList.getConfig(ParserKeys.incomplete).getStringList(key));
 			}
 			
 			list = uris.toArray(new String[0]);
 			loadFrame = new JFrame("Load");
 			loadFrame.setContentPane(new URISelector("URI to load", new LoadURICallback()));
 			loadFrame.pack();
 			loadFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			loadFrame.setVisible(true);
 		}
 		
 		private class LoadURICallback implements SelectorOptions {
 
 			@Override
 			public String[] getOptions() {
 				return list;
 			}
 
 			@Override
 			public void uriSelected(String uri) {
 				try {
 					editors.get(selected).forceSetData(new ConfigFileReader(uri).read(false));
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (InvalidConfigFileException e) {
 					e.printStackTrace();
 				}			
 				loadFrame.dispose();
 				loadedURIs.put(editors.get(selected), uri);
 				editors.get(selected).getPanel().validate();
 				editors.get(selected).getPanel().updateUI();
 			}
 			
 		}
 	}
 	
 	private class SaveButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int i = tabPanel.getSelectedIndex();
 			if(loadedURIs.get(editors.get(i)) != null)
 				save(editors.get(i), loadedURIs.get(editors.get(i)));
 			else
 				new SaveAsButtonListener().promptAndSave(editors.get(i));	
 		}
 		
 		public void save(ConfigurationEditor ce, String uri)
 		{
 			ConfigData cd = ce.getData();
 			boolean valid = ce.isValidConfig();
 			ParserKeys key = ce.getType();
 			
 			if(!valid)
 			{
 				int res = JOptionPane.showConfirmDialog(
 					    frame,
 					    "The Config is not complete.\n Would you like to save anywas?",
 					    "Error",
 					    JOptionPane.YES_NO_OPTION);
 				if(res != JOptionPane.YES_OPTION)
 					return;
 			}
 			
 			//if the uri is already in the list, get rid of it
 			if(masterList.getDefinedKeys().contains(key) && masterList.getStringList(key).contains(uri))
 				masterList.remove(key, uri);
 			ConfigData incomplete = masterList.getConfig(ParserKeys.incomplete);
 			if(incomplete.getDefinedKeys().contains(key) && incomplete.getStringList(key).contains(uri))
 				incomplete.remove(key, uri);
 					
 			try {
 				new ConfigFileWriter(uri).write(cd, valid);
 			} catch (Exception e) {
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(frame,
 					    "There was an error saving the config.",
 					    "Error",
 					    JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			
 			if(valid)
 				masterList.add(key, uri);
 			else
 				incomplete.add(key, uri);
 			
 			try {
 				new ConfigFileWriter(MASTER_LIST_URI).write(masterList, true);
				masterList = new ConfigFileReader(MASTER_LIST_URI).read();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
			} catch (InvalidConfigFileException e) {
				e.printStackTrace();
 			}
 			
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
 					.showInputDialog(frame, "Name:", "Name Dialog",
 							JOptionPane.PLAIN_MESSAGE, null, null, "");
 			if(s == null || s.length() <= 0)
 				return;
 			
 			ParserKeys key = ce.getType();
 			
 			//no other way to do this reall
 			String path = "resources/";
 			switch(key)
 			{
 				case abilityURI:
 					path += "abilities/";
 					break;
 				case animationURI:
 					path += "animations/";
 					break;
 				case buildingURI:
 					path += "buildings/";
 					break;
 				case mapURI:
 					path += "maps/";
 					break;
 				case projectileURI:
 					path += "projectiles/";
 					break;
 				case raceURI:
 					path += "races/";
 					break;
 				case techURI:
 					path += "techs/";
 					break;
 				case unitURI:
 					path += "units/";
 					break;
 			}
 			
 			path += s + ".cfg";
 			loadedURIs.put(ce, path);
 			
 			new SaveButtonListener().save(ce, path);
 		}
 		
 	}
 	
 	private class ExportButtonListener implements ActionListener {
 		
 		//variables to remember errors encountered
 		private ArrayList<String> errors = new ArrayList<String>();
 		
 		//variables for displaying the progress
 		private JFrame progressFrame;
 		private JTextArea work;
 		private JProgressBar bar;
 		private double size;
 		private double current;
 		
 		//variables for knowing what URIs to copy
 		private List<String> urisToCopy;
 		
 		//variables for the racemap selector
 		private JFrame selectorFrame; 
 		private ListURISelector maps;
 		private ListURISelector races;
 		private JButton export;
 		private JButton cancel;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(e.getSource().equals(export))
 			{
 				if(maps.getSelectedURIs().length <= 0 || races.getSelectedURIs().length <= 0)
 				{
 					JOptionPane.showMessageDialog(frame,
 						    "You must select at least one map and at least one race.",
 						    "Error",
 						    JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				selectorFrame.dispose();
 				
 				populateURIList();
 				
 				JFileChooser fc = new JFileChooser();
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				if(fc.showOpenDialog(BigFrameworkGuy.this.frame) == JFileChooser.APPROVE_OPTION)
 				{
 					File dir = fc.getSelectedFile();
 					export(dir);
 				}
 			}
 			else if(e.getSource().equals(cancel))
 				selectorFrame.dispose();
 			else //the only other way to get in here is from clicking export from the main frame
 			{
 				//set up the buttons
 				export = new JButton("Export");
 				export.addActionListener(this);
 				cancel = new JButton("Cancel");
 				cancel.addActionListener(this);
 				
 				//set up the list uri selectors
 				maps = new ListURISelector("Maps", new ListSelectorOptions() {
 					public void uriSelected(String uri) {}
 					public void uriRemoved(String uri) {}
 					public void uriHighlightChange(String[] uris) {}
 					public String[] getOptions() {
 						return BigFrameworkGuy.this.getMapURIs();
 					}
 				});
 				
 				races = new ListURISelector("Races", new ListSelectorOptions() {
 					public void uriSelected(String uri) {}
 					public void uriRemoved(String uri) {}
 					public void uriHighlightChange(String[] uris) {}
 					public String[] getOptions() {
 						return BigFrameworkGuy.this.getRaceURIs();
 					}
 				});
 				
 				//set up the button panel
 				JPanel buttonPanel = new JPanel();
 				buttonPanel.add(export);
 				buttonPanel.add(cancel);
 				
 				//set up the list panel
 				JPanel listPanel = new JPanel();
 				listPanel.add(maps);
 				listPanel.add(races);
 				
 				//set up the frame
 				selectorFrame = new JFrame("Select races and maps to include");
 				selectorFrame.getContentPane().setLayout(new BorderLayout());
 				selectorFrame.getContentPane().add(listPanel, BorderLayout.CENTER);
 				selectorFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
 				selectorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				selectorFrame.pack();
 				selectorFrame.setVisible(true);
 			}
 			
 		}
 		
 		private void populateURIList() {
 			urisToCopy = new ArrayList<String>();
 			
 			//add the map uris
 			String[] uris = maps.getSelectedURIs();
 			for(String uri : uris)
 				urisToCopy.add(uri);
 			
 			//add the race uris
 			uris = races.getSelectedURIs();
 			for(String uri : uris)
 				urisToCopy.add(uri);
 			
 			//now add some files that the display relies on
 			urisToCopy.add("resources/animations/left_ui_panel.cfg");
 			urisToCopy.add("resources/animations/right_ui_panel.cfg");
 			urisToCopy.add("resources/animations/Exit_Button.cfg");
 			urisToCopy.add("resources/animations/Exit_Button_Clicked.cfg");
 			
 			//go through the whole list of uris and check to see if they are configs that contain uris
 			for(int i = 0; i < urisToCopy.size(); i++)
 			{
 				//check to see if this uri is a config data
 				ConfigData cd = null;
 				try {
 					cd = new ConfigFileReader(urisToCopy.get(i)).read();
 				} catch(Exception e) {
 					continue;
 				}
 				
 				extractURIs(urisToCopy.get(i), cd);
 			}
 		}
 
 		private void extractURIs(String containingURI, ConfigData cd) {
 			//go throug each key and get all the strings for that key
 			for(ParserKeys key : cd.getDefinedKeys())
 			{
 				//check all the strings and see if they contain a URI
 				for(String s : cd.getStringList(key))
 				{
 					String uri = extractURI(s);
 					//check to see if this is a valid URI
 					if(uri == null)
 					{
 						//BUT WAIT! it might be a file local to the config uri, better check that
 						String pURI = extractURI(new File(containingURI).getParent());
 						uri = pURI + "/" + s;
 					}
 					//check to see if the uri is a valid file
 					try {
 						File f = new File(uri);
 						if(!f.exists() || f.isDirectory() || !f.isFile() || f.isHidden())
 							continue;
 					} catch(Exception e) {
 						continue;
 					}
 					urisToCopy.add(uri);
 				}
 				
 				//now check all the contained configs
 				for(ConfigData c : cd.getConfigList(key))
 					extractURIs(containingURI, c);
 			}
 		}
 		
 		private String extractURI(String s) {
 			int i = s.lastIndexOf("resources");
 			if(i < 0)
 				return null;
 			else
 				return s.substring(i, s.length()).replace('\\', '/');
 		}
 
 		public void export(File dir)
 		{
 			//set up the file filters
 			FileFilter classesOnly = new FileFilter() {
 				public boolean accept(File f) {
 					return f.isDirectory() || f.getName().endsWith(".class");
 				}
 				public String getDescription() {
 					return ".class files";
 				}
 			};
 			FileFilter includedURIs = new FileFilter() {
 				public boolean accept(File f) {
 					return !f.isHidden() && (f.isDirectory() ||
 							urisToCopy.contains(extractURI(f.getAbsolutePath())));
 				}
 				public String getDescription() {
 					return "non-hidden files";
 				}
 			};
 			
 			//now calculate the size
 			size = 0;
 			size += copyFile(dir, new File("resources"), includedURIs, true);
 			for(File f : new File("bin").listFiles())
 				size += copyFile(dir, f, classesOnly, true);
 			
 			//set up the progress frame
 			bar = new JProgressBar();
 			bar.setMaximum((int) (size/1000.0));
 			bar.setValue(500);
 			bar.setPreferredSize(new Dimension(100, 20));
 			work = new JTextArea();
 			JScrollPane workScroller = new JScrollPane(work);
 			workScroller.setPreferredSize(new Dimension(200, 200));
 			JPanel progressPanel = new JPanel();
 			progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
 			progressPanel.add(workScroller);
 			progressPanel.add(bar);
 			
 			progressFrame = new JFrame("Exporting");
 			progressFrame.setContentPane(progressPanel);
 			progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 			progressFrame.pack();
 			progressFrame.setVisible(true);
 			
 			//now start copying
 			errors.clear();
 			current = 0;
 			copyFile(dir, new File("resources"), includedURIs, false);
 			for(File f : new File("bin").listFiles())
 				copyFile(dir, f, classesOnly, false);
 			
 			progressFrame.dispose();
 			
 			//make the new masterList.cfg
 			ConfigData newMasterList = new ConfigData();//new FileWriter();
 			for(ParserKeys key : masterList.getDefinedKeys())
 			{
 				if(key.equals(ParserKeys.incomplete))
 					newMasterList.add(key, new ConfigData());
 				else
 				{
 					for(String uri : masterList.getStringList(key))
 					{
 						if(urisToCopy.contains(uri))
 							newMasterList.add(key, uri);
 					}
 				}
 			}
 			try {
 				new ConfigFileWriter(dir.getAbsolutePath() + "/resources/masterList.cfg").write(newMasterList, true);
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 
 			//make the run scripts
 			String runCommand = "java linewars.init.MainWindow";
 			String[] runScripts = new String[]{"Windows_Run.bat", "Linux_Mac_Run.sh"};
 			for(String script : runScripts)
 			{
 				try {
 					FileWriter fw = new FileWriter(new File(dir, script));
 					fw.write(runCommand);
 					fw.flush();
 					fw.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			//display error dialog if there were errors
 			if(!errors.isEmpty())
 			{
 				String error = "There were errors copying the following files:\n";
 				for(String s : errors)
 					error += s + "\n";
 				JOptionPane.showMessageDialog(frame,
 					    error,
 					    "Error",
 					    JOptionPane.ERROR_MESSAGE);
 			}
 			else
 				JOptionPane.showMessageDialog(frame,
 					    "Export successfull.", 
 					    "Success",
 					    JOptionPane.DEFAULT_OPTION);
 		}
 		
 		private double copyFile(File dir, File file, FileFilter filter, boolean simulate)
 		{
 			//don't copy files the filter won't allow
 			if(!filter.accept(file))
 				return 0;
 			
 			File newFile = new File(dir, file.getName());
 			
 			//if we're copying a directory
 			if(file.isDirectory())
 			{
 				if(!newFile.exists() || !newFile.isDirectory())
 				{
 					newFile.delete();
 					newFile.mkdir();
 				}
 				
 				double sum = 0;
 				
 				//copy all the contained files
 				for(File f : file.listFiles())
 					sum += copyFile(newFile, f, filter, simulate);
 				return sum;
 			}
 			else //just a single file
 			{
 				if(!newFile.exists() || newFile.isDirectory())
 					newFile.delete();
 				
 				if(!simulate)
 				{
 					double progress = ((int)(current/size*1000.0))/10.0;
 					work.setText(work.getText() + "\n" + progress + "%: " + file.getAbsolutePath());
 					System.out.println(progress + "%: " + file.getAbsolutePath());
 					work.validate();
 					work.updateUI();
 					try {
 						FileCopy.copy(file.getAbsolutePath(), newFile.getAbsolutePath());
 					} catch (IOException e) {
 						errors.add(file.getAbsolutePath());
 					}
 					current += file.length();
 					bar.setValue((int) (current/size*1000.0));
 					bar.validate();
 					bar.updateUI();
 				}
 				
 				return file.length();
 			}
 		}
 	}
 	
 	
 }
