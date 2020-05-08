 package org.geworkbench.engine.ccm;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.table.AbstractTableModel;
 
 import org.geworkbench.util.Util;
 
 /**
  * CCMTableModel
  * 
  */
 class CCMTableModel extends AbstractTableModel {
 	private static final long serialVersionUID = 3986726808587129205L;
 
 	public static final boolean NO_VALIDATION = false;
 
 	// TODO all these indices should be private. it takes more refactoring to do
 	// it though.
 	static final int SELECTION_INDEX = 0;
 	static final int NAME_INDEX = 1;
 	static final int AUTHOR_INDEX = 2;
 	static final int VERSION_INDEX = 3;
 	static final int TUTORIAL_URL_INDEX = 4;
 	static final int TOOL_URL_INDEX = 5;
 
 	// FIXME the following indices are really identifiers instead of index
 	static final int CLASS_INDEX = 7;
 	static final int DESCRIPTION_INDEX = 8;
 	static final int FOLDER_INDEX = 9;
 	static final int CCM_FILE_NAME_INDEX = 10;
 	static final int LICENSE_INDEX = 11;
 	static final int DOCUMENTATION_INDEX = 13;
 	static final int ANALYSIS_INDEX = 17;
 	static final int VISUALIZER_INDEX = 18;
 
 	static final int HIDDEN_INDEX = 20;
 	static final int LAST_VISIBLE_COLUMN = TOOL_URL_INDEX;
 
 	static final int FIRST_STRING_COLUMN = NAME_INDEX;
 
 	private String[] columnNames = { "On/Off", "Name", "Author", "Version",
 			"Tutorial", "Tool URL" };
 	Vector<TableRow> rows = new Vector<TableRow>();
 	
 	private Map<String, List<String>> requiredComponentsMap = new TreeMap<String, List<String>>();
 	private Map<String, List<String>> relatedComponentsMap =  new TreeMap<String, List<String>>();
 
 
 	private ComponentConfigurationManager manager = null;
 
 	private String loadedFilterValue = null;
 	private String typeFilterValue = null;
 	private String keywordFilterValue = null;
 
 	/*
 	 * Constructor
 	 */
 	public CCMTableModel(ComponentConfigurationManager manager) {
 		super();
 
 		this.manager = manager;
 		loadGuiModelFromCmmFiles();
 		loadGuiModelFromFiles(manager.cwbFile);
 		Collections.sort(rows, new TableNameComparator());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.TableModel#getColumnCount()
 	 */
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.TableModel#getRowCount()
 	 */
 	public int getRowCount() {
 		return getModelRowCount();
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	final public int getModelRowCount() {
 		return rows.size();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
 	 */
 	public String getColumnName(int col) {
 		return columnNames[col];
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int) Without
 	 *      this method, the check box column would default to a String
 	 */
 	@SuppressWarnings("unchecked")
 	public Class getColumnClass(int column) {
 
 		if (column == SELECTION_INDEX) {
 			return Boolean.class;
 		}
 
 		if (column == AUTHOR_INDEX || column == TUTORIAL_URL_INDEX
 				|| column == TOOL_URL_INDEX) {
 			return JButton.class;
 		}
 
 		return String.class;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
 	 */
 	public boolean isCellEditable(int row, int column) {
 		if (column == SELECTION_INDEX) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.TableModel#getValueAt(int, int)
 	 */
 	public Object getValueAt(int row, int column) {
 		return getModelValueAt(row, column);
 	}
 
 	/**
 	 * return the plugin name for a given row
 	 * 
 	 * @param row
 	 * @return
 	 */
 	String getPluginName(int row) {
 		return (String) getModelValueAt(row, CCMTableModel.NAME_INDEX);
 	}
 
 	/**
 	 * set selected status to opposite for a given row
 	 * 
 	 * @param row
 	 * @return
 	 */
 	void switchSelected(int row) {
 		Boolean selected = (Boolean) getModelValueAt(row,
 				CCMTableModel.SELECTION_INDEX);
 		Boolean reset = new Boolean(!selected.booleanValue());
 		setModelValueAt(reset, row, CCMTableModel.SELECTION_INDEX,
 				CCMTableModel.NO_VALIDATION);
 	}
 
 	/**
 	 * un-select a given row without checking validation
 	 * 
 	 * @param row
 	 * @return
 	 */
 	void unselectWithoutValiation(int row) {
 		setModelValueAt(new Boolean(false), row, CCMTableModel.SELECTION_INDEX,
 				CCMTableModel.NO_VALIDATION);
 	}
 
 	/**
 	 * select a given row without checking validation
 	 * 
 	 * @param row
 	 * @return
 	 */
 	boolean selectWithoutValiation(int row) {
 		return setModelValueAt(new Boolean(true), row,
 				CCMTableModel.SELECTION_INDEX, CCMTableModel.NO_VALIDATION);
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.TableModel#getValueAt(int, int)
 	 */
 	final public Object getModelValueAt(int row, int column) {
 		TableRow record = rows.get(row);
 		switch (column) {
 		case SELECTION_INDEX:
 			return (Boolean) record.isSelected();
 		case NAME_INDEX:
 			return (String) record.getName();
 		case VERSION_INDEX:
 			return (String) record.getVersion();
 		case AUTHOR_INDEX:
 			return (JButton) record.getAuthor();
 		case TUTORIAL_URL_INDEX:
 			return (JButton) record.getTutorialURL();
 		case TOOL_URL_INDEX:
 			return (JButton) record.getToolURL();
 		case CLASS_INDEX:
 			return (String) record.getClazz();
 		case DESCRIPTION_INDEX:
 			return (String) record.getDescription();
 		case FOLDER_INDEX:
 			return (String) record.getFolder();
 		case CCM_FILE_NAME_INDEX:
 			return (File) record.getFile();
 		case LICENSE_INDEX:
 			return (String) record.getLicense();
 		case DOCUMENTATION_INDEX:
 			return (String) record.getDocumentation();
 		case ANALYSIS_INDEX:
 			return (Boolean) record.isAnalysis();
 		case VISUALIZER_INDEX:
 			return (Boolean) record.isVisualizer();
 		case HIDDEN_INDEX:
 			return (Boolean) record.isHidden();
 
 		default:
 			return null;
 		}
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.TableModel#getRowByClazz(String)
 	 */
 	final public int getModelRowByClazz(String clazz) {
 
 		for (int i = 0; i < rows.size(); i++) {
 			TableRow record = (TableRow) rows.get(i);
 			String rowClazz = record.getClazz();
 			if (clazz.equalsIgnoreCase(rowClazz)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
 	 *      int, int)
 	 */
 	public void setValueAt(Object value, int row, int column) {
 		setModelValueAt(value, row, column, true);
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
 	 *      int, int, boolean)
 	 */
 	final public boolean setModelValueAt(Object value, int modelRow,
 			int column, boolean validation) {
 		TableRow record = (TableRow) rows.get(modelRow);
 		switch (column) {
 		case SELECTION_INDEX:
 			record.setSelected((Boolean) value);
 
 			Boolean currentChoice = (Boolean) getModelValueAt(modelRow,
 					CCMTableModel.SELECTION_INDEX);
 
 			List<String> required = requiredComponentsMap.get(record.name);
 			List<String> related = relatedComponentsMap.get(record.name);
 			String folder = (String) getModelValueAt(modelRow,
 					CCMTableModel.FOLDER_INDEX);
 			File file = ((File) getModelValueAt(modelRow,
 					CCMTableModel.CCM_FILE_NAME_INDEX));
 
 			List<String> unselectedRequired = new ArrayList<String>();
 			for (int i = 0; i < required.size(); i++) {
 				String requiredClazz = required.get(i);
 				Integer requiredRow = getModelRowByClazz(requiredClazz);
 				Boolean requiredSelected = (Boolean) getModelValueAt(
 						requiredRow.intValue(), CCMTableModel.SELECTION_INDEX);
 				if (!requiredSelected) {
 					unselectedRequired.add(requiredClazz);
 				}
 			}
 
 			if (currentChoice.booleanValue()) {
 				/* PLUGIN SELECTED */
 				String propFileName = null;
 				String ccmFileName = file.getName();
 				if (ccmFileName.endsWith(".ccm.xml"))
 					propFileName = ccmFileName.replace(".ccm.xml",
 							".ccmproperties");
 				else if (ccmFileName.endsWith(".cwb.xml"))
 					propFileName = ccmFileName.replace(".cwb.xml",
 							".ccmproperties");
 				String licenseAccepted = ComponentConfigurationManager
 						.readProperty(folder, propFileName, "licenseAccepted");
 				Boolean boolRequired = record.isMustAccept();
 				boolean bRequired = boolRequired.booleanValue();
 				if ((bRequired && (licenseAccepted == null))
 						|| (bRequired && licenseAccepted != null && !licenseAccepted
 								.equalsIgnoreCase("true"))) {
 					String componentName = (String) getModelValueAt(modelRow,
 							CCMTableModel.NAME_INDEX);
 
 					String title = "Terms Acceptance";
 					String message = "\nBy clicking \"I Accept\", you signify that you \nhave read and accept the terms of the license \nagreement for the \""
 							+ componentName
 							+ "\" application.\n\nTo view this license, exit out of this dialog and click \non the View License button below.\n\n";
 					Object[] options = { "I Accept", "No, thanks" };
 					int choice = JOptionPane.showOptionDialog(null, message,
 							title, JOptionPane.YES_NO_OPTION,
 							JOptionPane.PLAIN_MESSAGE, null, options,
 							options[0]);
 
 					if (choice != 0) {
 						ComponentConfigurationManager.writeProperty(folder,
 								propFileName, "licenseAccepted", "false");
 						record.setSelected(new Boolean(false));
 						return false;
 					}
 					ComponentConfigurationManager.writeProperty(folder,
 							propFileName, "licenseAccepted", "true");
 				}
 
 				if (!validation) {
 					fireTableDataChanged();
 					return true;
 				}
 
 				if (unselectedRequired.size() > 0 || related.size() > 0) {
 					DependencyManager dmanager = new DependencyManager(this,
 							unselectedRequired, modelRow, related);
 					dmanager.checkDependency();
 				}
 			} else {
 				/* PLUGIN UNSELECTED */
 				List<Integer> dependentPlugins = null;
 
 				String unselectedPluginClazz = "Plugin is missing a Class descriptor";
 				int unselectedRow = modelRow;
 				if (unselectedRow >= 0) {
 					unselectedPluginClazz = (String) getModelValueAt(
 							unselectedRow, CCMTableModel.CLASS_INDEX);
 				}
 
 				dependentPlugins = new ArrayList<Integer>();
 				/*
 				 * Find Plugins that are dependent on this unselected plugin.
 				 */
 				int rowCount = getModelRowCount();
 				for (int i = 0; i < rowCount; i++) {
 
 					/*
 					 * If the potentially dependent plugin is not selected,
 					 * don't bother to see if it is dependent
 					 */
 					Boolean selected = (Boolean) getModelValueAt(i,
 							CCMTableModel.SELECTION_INDEX);
 					if (!selected.booleanValue()) {
 						continue;
 					}
 
 					List<String> requiredList = requiredComponentsMap.get(rows.get(i).name);
 
 					for (int j = 0; j < requiredList.size(); j++) {
 						String requiredClazz = requiredList.get(j);
 						if (unselectedPluginClazz
 								.equalsIgnoreCase(requiredClazz)) {
 							dependentPlugins.add(new Integer(i));
 							break;
 						}
 					}
 				}
 
 				if (!validation) {
 					fireTableDataChanged();
 					return true;
 				}
 
 				/* If dependencies are found, then popup a dialog */
 				if (dependentPlugins.size() > 0) {
 					DependencyManager dmanager = new DependencyManager(this,
 							dependentPlugins, modelRow);
 					dmanager.checkDependency();
 				}
 			}
 
 			break;
 		default:
 
 		}
 		fireTableCellUpdated(modelRow, column);
 
 		return true;
 	}
 
 	// what the is this TODO comment?
 	// TODO choose weather to use loadGuiModelFromCmmFiles() or
 	// getPluginsFromCcmFile()
 
 	// this is different from loadGuiModelFromFiles not just in the file name,
 	// but different for
 	// manager.getPluginsFromCcmFile(folderName, ccmFileName );
 	/**
 	 * Load the GUI Model from.ccm.xml file
 	 */
 	private void loadGuiModelFromCmmFiles() {
 		String name = null;
 		String version = null;
 		String author = null;
 		String authorURL = null;
 		String toolURL = null;
 		String tutorialURL = null;
 		String clazz = null;
 		String description = null;
 		String license = null;
 		String mustAccept = null;
 		String documentation = null;
 		List<String> requiredComponents = null;
 		List<String> relatedComponents = null;
 		String parser = null;
 		String analysis = null;
 		String visualizer = null;
 		String hidden = null;
 
 		for (File file : manager.ccmFile) {
 			String folderName = file.getParentFile().getName(), ccmFileName = file
 					.getName();
 
 			// CcmComponent ccmComponent = manager.getPluginsFromFile(file );
 
 			CcmComponent ccmComponent = manager.getPluginsFromCcmFile(
 					folderName, ccmFileName);
 
 			if (ccmComponent == null) {
 				continue;
 			}
 
 			List<Plugin> plugins = ccmComponent.getPlugins();
 			if (plugins == null) {
 				continue;
 			}
 
 			Plugin plugin = (Plugin) plugins.get(0);
 			name = plugin.getName();
 
 			version = ccmComponent.getVersion();
 			author = ccmComponent.getAuthor();
 			authorURL = ccmComponent.getAuthorURL();
 			tutorialURL = ccmComponent.getTutorialURL();
 			toolURL = ccmComponent.getToolURL();
 			clazz = ccmComponent.getClazz();
 			description = ccmComponent.getDescription();
 			license = ccmComponent.getLicense();
 			mustAccept = ccmComponent.getMustAccept();
 			documentation = ccmComponent.getDocumentation();
 			requiredComponents = ccmComponent.getRequiredComponents();
 			relatedComponents = ccmComponent.getRelatedComponents();
 			parser = ccmComponent.getParser();
 			analysis = ccmComponent.getAnalysis();
 			visualizer = ccmComponent.getVisualizer();
 			hidden = ccmComponent.getHidden();
 
 			boolean bMustAccept = false;
 			boolean bParser = false;
 			boolean bAnalysis = false;
 			boolean bVisualizer = false;
 			boolean bHidden = false;
 
 			if (mustAccept != null && mustAccept.equalsIgnoreCase("true")) {
 				bMustAccept = true;
 			}
 			if (parser != null && parser.equalsIgnoreCase("true")) {
 				bParser = true;
 			}
 			if (analysis != null && analysis.equalsIgnoreCase("true")) {
 				bAnalysis = true;
 			}
 			if (visualizer != null && visualizer.equalsIgnoreCase("true")) {
 				bVisualizer = true;
 			}
 			if (hidden != null && hidden.equalsIgnoreCase("true")) {
 				bHidden = true;
 			}
 
 			TableRow tableRow = new TableRow(false, name, version, author,
 					authorURL, tutorialURL, toolURL, clazz, description,
 					folderName, file, license, bMustAccept, documentation,
 					bParser, bAnalysis,
 					bVisualizer, bHidden);
 			requiredComponentsMap.put(name, requiredComponents);
 			relatedComponentsMap.put(name, relatedComponents);
 
 			String propFileName = null;
 			if (ccmFileName.endsWith(".ccm.xml"))
 				propFileName = ccmFileName
 						.replace(".ccm.xml", ".ccmproperties");
 			else if (ccmFileName.endsWith(".cwb.xml"))
 				propFileName = ccmFileName
 						.replace(".cwb.xml", ".ccmproperties");
 
 			String onOff = ComponentConfigurationManager.readProperty(
 					folderName, propFileName, "on-off");
 
 			if (onOff != null && onOff.equals("true")) {
 				tableRow.setSelected(true);
 			} else {
 				tableRow.setSelected(false);
 			}
 
 			rows.add(tableRow);
 
 		}
 
 	}
 
 	private void loadGuiModelFromFiles(List<File> files) {
 		String name = null;
 		String version = null;
 		String author = null;
 		String authorURL = null;
 		String toolURL = null;
 		String tutorialURL = null;
 		String clazz = null;
 		String description = null;
 		String license = null;
 		String mustAccept = null;
 		String documentation = null;
 		List<String> requiredComponents = null;
 		List<String> relatedComponents = null;
 		String parser = null;
 		String analysis = null;
 		String visualizer = null;
 		String hidden = null;
 
 		for (File file : files) {
			String folderName = file.getParentFile().getName(), ccmFileName = file
 					.getName();
 
 			CcmComponent ccmComponent = manager.getPluginsFromFile(file);
 
 			if (ccmComponent == null) {
 				continue;
 			}
 
 			List<Plugin> plugins = ccmComponent.getPlugins();
 			if (plugins == null || plugins.size() <= 0) {
 				continue;
 			}
 
 			Plugin plugin = (Plugin) plugins.get(0);
 			name = plugin.getName();
 
 			version = ccmComponent.getVersion();
 			author = ccmComponent.getAuthor();
 			authorURL = ccmComponent.getAuthorURL();
 			tutorialURL = ccmComponent.getTutorialURL();
 			toolURL = ccmComponent.getToolURL();
 			clazz = ccmComponent.getClazz();
 			description = ccmComponent.getDescription();
 			license = ccmComponent.getLicense();
 			mustAccept = ccmComponent.getMustAccept();
 			documentation = ccmComponent.getDocumentation();
 			requiredComponents = ccmComponent.getRequiredComponents();
 			relatedComponents = ccmComponent.getRelatedComponents();
 			parser = ccmComponent.getParser();
 			analysis = ccmComponent.getAnalysis();
 			visualizer = ccmComponent.getVisualizer();
 			hidden = ccmComponent.getHidden();
 
 			boolean bMustAccept = false;
 			boolean bParser = false;
 			boolean bAnalysis = false;
 			boolean bVisualizer = false;
 			boolean bHidden = false;
 
 			if (mustAccept != null && mustAccept.equalsIgnoreCase("true")) {
 				bMustAccept = true;
 			}
 			if (parser != null && parser.equalsIgnoreCase("true")) {
 				bParser = true;
 			}
 			if (analysis != null && analysis.equalsIgnoreCase("true")) {
 				bAnalysis = true;
 			}
 			if (visualizer != null && visualizer.equalsIgnoreCase("true")) {
 				bVisualizer = true;
 			}
 			if (hidden != null && hidden.equalsIgnoreCase("true")) {
 				bHidden = true;
 			}
 
 			TableRow tableRow = new TableRow(false, name, version, author,
 					authorURL, tutorialURL, toolURL, clazz, description,
 					folderName, file, license, bMustAccept, documentation,
 					bParser, bAnalysis,
 					bVisualizer, bHidden);
 			requiredComponentsMap.put(name, requiredComponents);
 			relatedComponentsMap.put(name, relatedComponents);
 
 			String propFileName = null;
 			if (ccmFileName.endsWith(".ccm.xml"))
 				propFileName = ccmFileName
 						.replace(".ccm.xml", ".ccmproperties");
 			else if (ccmFileName.endsWith(".cwb.xml"))
 				propFileName = ccmFileName
 						.replace(".cwb.xml", ".ccmproperties");
 
 			String onOff = ComponentConfigurationManager.readProperty(
 					folderName, propFileName, "on-off");
 
 			if (onOff != null && onOff.equals("true")) {
 				tableRow.setSelected(true);
 			} else {
 				tableRow.setSelected(false);
 			}
 
 			rows.add(tableRow);
 		}// files
 
 	}
 
 	final public String getLoadedFilterValue() {
 		return loadedFilterValue;
 	}
 
 	final public void setLoadedFilterValue(String loadedFilterValue) {
 		this.loadedFilterValue = loadedFilterValue;
 	}
 
 	final public String getTypeFilterValue() {
 		return typeFilterValue;
 	}
 
 	final public void setTypeFilterValue(String typeFilterValue) {
 		this.typeFilterValue = typeFilterValue;
 	}
 
 	final public String getKeywordFilterValue() {
 		return keywordFilterValue;
 	}
 
 	final public void setKeywordFilterValue(String keywordFilterValue) {
 		this.keywordFilterValue = keywordFilterValue;
 	}
 
 	/**
 	 * GUI row structure
 	 * 
 	 * @author tg2321
 	 * @version $Id: ComponentConfigurationManagerWindow.java,v 1.17 2009-11-20
 	 *          14:37:27 jiz Exp $
 	 */
 	static private class TableRow {
 		private boolean selected = false;
 		private String name = "";
 		private String version = "";
 		private String author = "";
 		private String authorURL = null;
 		private String tutorialURL = null;
 		private String toolURL = null;
 		private String clazz = "";
 		private String description = "";
 		private String folder = "";
 		private File file;
 		private String license = "";
 		private boolean mustAccept = false;
 		private String documentation = "";
 		private boolean parser = false;
 		private boolean analysis = false;
 		private boolean visualizer = false;
 		private boolean hidden = false;
 
 		/**
 		 * Constructor
 		 * 
 		 * @param selected
 		 * @param name
 		 * @param version
 		 * @param author
 		 * @param authorURL
 		 * @param toolURL
 		 * @param clazz
 		 * @param description
 		 * @param folder
 		 * @param fileName
 		 * @param requiredComponents
 		 * @param relatedComponents
 		 */
 		public TableRow(boolean selected, String name, String version,
 				String author, String authorURL, String tutorialURL,
 				String toolURL, String clazz, String description,
 				String folder, File file, String license, boolean mustAccept,
 				String documentation, boolean parser,
 				boolean analysis, boolean visualizer,
 				boolean hidden) {
 			super();
 			this.selected = selected;
 			this.name = name;
 			this.version = version;
 			this.author = author;
 			this.authorURL = authorURL;
 			this.tutorialURL = tutorialURL;
 			this.toolURL = toolURL;
 			this.clazz = clazz;
 			this.description = description;
 			this.folder = folder;
 			this.file = file;
 			this.license = license;
 			this.mustAccept = mustAccept;
 			this.documentation = documentation;
 			this.parser = parser;
 			this.analysis = analysis;
 			this.visualizer = visualizer;
 			this.hidden = hidden;
 		}
 
 		public boolean isSelected() {
 			return selected;
 		}
 
 		public void setSelected(boolean selected) {
 			this.selected = selected;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public String getVersion() {
 			return version;
 		}
 
 		public void setVersion(String version) {
 			this.version = version;
 		}
 
 		public JButton getAuthor() {
 			JButton button = new JButton(this.author);
 			button.setBorderPainted(false);
 
 			if (this.authorURL != null) {
 				button.setToolTipText(this.authorURL);
 				String html = "<html><font><u>" + this.author
 						+ "</u><br></font>";
 				button.setText(html);
 			}
 
 			return button;
 		}
 
 		public void setAuthor(String author) {
 			this.author = author;
 		}
 
 		public JButton getAuthorURL() {
 			ImageIcon image = Util.createImageIcon(
 					"/org/geworkbench/engine/visualPlugin.png", this.authorURL);
 
 			JButton button = new JButton(image);
 			button.setToolTipText(this.authorURL);
 			button.setBorderPainted(false);
 			return button;
 		}
 
 		public void setAuthorURL(String authorURL) {
 			this.authorURL = authorURL;
 		}
 
 		public JButton getTutorialURL() {
 			ImageIcon image = null;
 			if (this.tutorialURL == null || this.tutorialURL == "") {
 				image = Util.createImageIcon(
 						"/org/geworkbench/engine/visualPluginGrey.png",
 						this.tutorialURL);
 			} else {
 				image = Util.createImageIcon(
 						"/org/geworkbench/engine/visualPlugin.png",
 						this.tutorialURL);
 			}
 
 			JButton button = new JButton(image);
 			button.setToolTipText(this.tutorialURL);
 			button.setBorderPainted(false);
 			return button;
 		}
 
 		public void setTutorialURL(String tutorialURL) {
 			this.tutorialURL = tutorialURL;
 		}
 
 		public JButton getToolURL() {
 			ImageIcon image = null;
 			if (this.toolURL == null || this.toolURL == "") {
 				image = Util.createImageIcon(
 						"/org/geworkbench/engine/visualPluginGrey.png",
 						this.tutorialURL);
 			} else {
 				image = Util.createImageIcon(
 						"/org/geworkbench/engine/visualPlugin.png",
 						this.tutorialURL);
 			}
 
 			JButton button = new JButton(image);
 			button.setToolTipText(this.toolURL);
 			button.setBorderPainted(false);
 			return button;
 		}
 
 		public void setToolURL(String toolURL) {
 			this.toolURL = toolURL;
 		}
 
 		public String getClazz() {
 			return clazz;
 		}
 
 		public void setClazz(String clazz) {
 			this.clazz = clazz;
 		}
 
 		public String getFolder() {
 			return folder;
 		}
 
 		public void setDescription(String description) {
 			this.description = description;
 		}
 
 		public void setFolder(String folder) {
 			this.folder = folder;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 		public File getFile() {
 			return file;
 		}
 
 		public String getLicense() {
 			return license;
 		}
 
 		public void setLicense(String license) {
 			this.license = license;
 		}
 
 		public boolean isMustAccept() {
 			return mustAccept;
 		}
 
 		public void setMustAccept(boolean mustAccept) {
 			this.mustAccept = mustAccept;
 		}
 
 		public String getDocumentation() {
 			return documentation;
 		}
 
 		public void setDocumentation(String documentation) {
 			this.documentation = documentation;
 		}
 
 		public boolean isParser() {
 			return parser;
 		}
 
 		public void setParser(boolean parser) {
 			this.parser = parser;
 		}
 
 		public boolean isAnalysis() {
 			return analysis;
 		}
 
 		public void setAnalysis(boolean analysis) {
 			this.analysis = analysis;
 		}
 
 		public boolean isVisualizer() {
 			return visualizer;
 		}
 
 		public void setVisualizer(boolean visualizer) {
 			this.visualizer = visualizer;
 		}
 
 		public boolean isHidden() {
 			return hidden;
 		}
 
 		public void setHidden(boolean hidden) {
 			this.hidden = hidden;
 		}
 
 	}
 
 	static private class TableNameComparator implements Comparator<TableRow> {
 		public int compare(TableRow row1, TableRow row2) {
 			String name1 = row1.getName().toLowerCase();
 			String name2 = row2.getName().toLowerCase();
 			return name1.compareTo(name2);
 		}
 	}
 
 }
