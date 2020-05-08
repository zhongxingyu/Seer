 package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;
 
 import java.awt.Dimension;
 import java.beans.Beans;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
 import javax.swing.border.TitledBorder;
 import javax.swing.text.JTextComponent;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.control.exceptions.TemplateException;
 import org.vpac.grisu.frontend.control.jobMonitoring.RunningJobManager;
 import org.vpac.grisu.frontend.view.swing.files.GrisuFileDialog;
 import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
 import org.vpac.grisu.frontend.view.swing.jobcreation.templates.TemplateObject;
 import org.vpac.grisu.frontend.view.swing.jobcreation.templates.filters.Filter;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.UserEnvironmentManager;
 import org.vpac.grisu.model.files.GlazedFile;
 import org.vpac.grisu.model.job.JobSubmissionObjectImpl;
 import org.vpac.historyRepeater.HistoryManager;
 
 public abstract class AbstractInputPanel extends JPanel implements
 		PropertyChangeListener {
 
 	static final Logger myLogger = Logger.getLogger(AbstractInputPanel.class
 			.getName());
 
 	public static final String FILE_DIALOG_LAST_DIRECTORY_KEY = "lastDirectory";
 
 	public static final String DEFAULT_VALUE = "defaultValue";
 	public static final String NAME = "name";
 	public static final String TITLE = "title";
 	public static final String LABEL = "label";
 	public static final String PREFILLS = "prefills";
 	public static final String USE_HISTORY = "useHistory"; // default: true
 	public static final String FILL_WITH_DEFAULT_VALUE = "fillWithDefaultValue"; // default:
 	// false
 	public static final String HISTORY_ITEMS = "historyItems";
 	public static final String DEPENDENCY = "dependency";
 	public static final String SIZE = "size";
 	public static final String IS_VISIBLE = "isVisible";
 	public static final String BEAN = "property";
 	public static final String IS_EDITABLE = "editable";
 
 	public static final String APPLICATION = "application";
 	public static final String TEMPLATENAME = "templatename";
 
 	private static final String HELP = "help";
 
 	private TemplateObject template;
 	private final LinkedList<Filter> filters;
 
 	private boolean isVisible = true;
 	protected final String bean;
 	protected final String templateName;
 
 	private JobSubmissionObjectImpl jobObject;
 
 	protected final String historyManagerEntryName;
 
 	private Map<String, String> panelProperties;
 
 	private ServiceInterface si;
 	private UserEnvironmentManager uem;
 	private RunningJobManager rjm;
 	private HistoryManager hm;
 
 	private JButton helpLabel;
 	private boolean displayHelpLabel = false;
 
 	private boolean initFinished = false;
 
 	private static Map<String, GrisuFileDialog> dialogs = new HashMap<String, GrisuFileDialog>();
 
 	private static void createSingletonFileDialog(ServiceInterface si,
 			String templateName) {
 
 		if (dialogs.get(templateName) == null) {
 			String startUrl = GrisuRegistryManager
 					.getDefault(si)
 					.getHistoryManager()
 					.getLastEntry(
 							templateName + "_" + FILE_DIALOG_LAST_DIRECTORY_KEY);
 
 			if (StringUtils.isBlank(startUrl)) {
 				startUrl = new File(System.getProperty("user.home")).toURI()
 						.toString();
 			} else if (!FileManager.isLocal(startUrl)) {
 				try {
 					if (!si.isFolder(startUrl)) {
 						startUrl = new File(System.getProperty("user.home"))
 								.toURI().toString();
 					}
 				} catch (RemoteFileSystemException e) {
 					myLogger.debug(e);
 					startUrl = new File(System.getProperty("user.home"))
 							.toURI().toString();
 				}
 			}
 			GrisuFileDialog dialog = new GrisuFileDialog(si, startUrl);
 			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			dialogs.put(templateName, dialog);
 		}
 	}
 
 	public static GrisuFileDialog getFileDialog(String templateName) {
 
 		if (dialogs.get(templateName) == null) {
 			throw new IllegalStateException("File dialog not initialized yet.");
 		}
 		return dialogs.get(templateName);
 	}
 
 	public AbstractInputPanel(String templateName, PanelConfig config)
 			throws TemplateException {
 
 		this.templateName = templateName;
 
 		if ((config == null) || (config.getFilters() == null)) {
 			this.filters = new LinkedList<Filter>();
 		} else {
 			this.filters = config.getFilters();
 		}
 
 		if ((config == null) || (config.getProperties() == null)
 				|| (config.getProperties().size() == 0)) {
 			this.panelProperties = getDefaultPanelProperties();
 			if (this.panelProperties == null) {
 				this.panelProperties = new HashMap<String, String>();
 			}
 		} else {
 			this.panelProperties = getDefaultPanelProperties();
 			this.panelProperties.putAll(config.getProperties());
 		}
 
 		if (StringUtils.isBlank(this.panelProperties.get(NAME))) {
 			this.panelProperties.put(NAME, UUID.randomUUID().toString());
 			historyManagerEntryName = templateName;
 		} else {
 			historyManagerEntryName = templateName + "_"
 					+ this.panelProperties.get(NAME);
 		}
 
 		String title = panelProperties.get(TITLE);
 
 		if (StringUtils.isBlank(title)) {
 			title = panelProperties.get(NAME);
 		}
 
 		if (!Beans.isDesignTime()) {
 			// so validator displays proper name
 			if (getTextComponent() != null) {
 				getTextComponent().setName(title);
 			} else if (getJComboBox() != null) {
 				if (StringUtils.isNotBlank(title)) {
 					getJComboBox().setName(title);
 				}
 			}
 		}
 
 		if (!StringUtils.isBlank(this.panelProperties.get(BEAN))) {
 			bean = this.panelProperties.get(BEAN);
 		} else {
 			bean = null;
 		}
 
 		if (!StringUtils.isBlank(this.panelProperties.get(IS_VISIBLE))) {
 			try {
 				isVisible = Boolean.parseBoolean(this.panelProperties
 						.get(IS_VISIBLE));
 			} catch (Exception e) {
 				throw new TemplateException("Can't parse isVisible property: "
 						+ e.getLocalizedMessage(), e);
 			}
 		}
 		try {
 			title = this.panelProperties.get(TITLE);
 			setBorder(new TitledBorder(null, title, TitledBorder.LEADING,
 					TitledBorder.TOP, null, null));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		String size = this.panelProperties.get(SIZE);
 		if (StringUtils.isNotBlank(size)) {
 			try {
 				int width = Integer.parseInt(size.substring(0,
 						size.indexOf("x")).trim());
 				int height = Integer.parseInt(size.substring(
 						size.indexOf("x") + 1).trim());
 				setPreferredSize(new Dimension(width, height));
 				setMaximumSize(new Dimension(width, height));
 			} catch (Exception e) {
 				throw new TemplateException(
 						"Can't parse size property for panel "
 								+ this.panelProperties.get(NAME) + ": " + size);
 			}
 		}
 
 		String help = this.panelProperties.get(HELP);
 
 		if (StringUtils.isNotBlank(help)) {
 
 			displayHelpLabel = true;
 			getHelpLabel().setToolTipText(help);
 		}
 
 	}
 
 	protected void addHistoryValue(String value) {
 		addHistoryValue(null, value);
 	}
 
 	protected void addHistoryValue(String optionalKey, String value) {
 
 		if (StringUtils.isBlank(optionalKey)) {
 			hm.addHistoryEntry(historyManagerEntryName, value);
 		} else {
 			hm.addHistoryEntry(historyManagerEntryName + "_" + optionalKey,
 					value);
 		}
 
 	}
 
 	protected void addValue(String bean, Object value) {
 
 		if (!isInitFinished()) {
 			return;
 		}
 
 		try {
 			Method method = jobObject.getClass().getMethod(
 					"add" + StringUtils.capitalize(bean), value.getClass());
 			method.invoke(jobObject, value);
 			applyFilters();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void addValueToHistory() {
 
 		String value = getValueAsString();
 
 		if (StringUtils.isNotBlank(value)) {
 			addHistoryValue(value);
 		}
 
 	}
 
 	private void applyFilters() {
 
 		String string = getValueAsString();
 		if (string == null) {
 			myLogger.debug("Value is null. Not applying filters...");
 			return;
 		}
 		for (Filter filter : filters) {
 			string = filter.filter(string);
 		}
 
 		template.userInput(getPanelName(), string);
 	}
 
 	protected boolean displayHelpLabel() {
 		return displayHelpLabel;
 	}
 
 	protected boolean fillDefaultValueIntoFieldWhenPreparingPanel() {
 
 		try {
 			if (panelProperties.get(FILL_WITH_DEFAULT_VALUE) != null) {
 				boolean use = Boolean.parseBoolean(panelProperties
 						.get(FILL_WITH_DEFAULT_VALUE));
 				return use;
 			} else {
 				return false;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Returns a set of default values if no configuration is specified in the
 	 * template.
 	 * 
 	 * Good to have as a reference which values are available for this panel.
 	 * 
 	 * @return the properties
 	 */
 	abstract protected Map<String, String> getDefaultPanelProperties();
 
 	public String getDefaultValue() {
 
 		String last = null;
 
 		if (useHistory()) {
 			try {
 				last = getLastValue();
 				if (last != null) {
 					return last;
 				}
 			} catch (Exception e) {
 				myLogger.debug("No history value for "
 						+ panelProperties.get(NAME));
 			}
 		}
 
 		last = panelProperties.get(DEFAULT_VALUE);
 		return last;
 	}
 
 	public GrisuFileDialog getFileDialog() {
 		return getFileDialog(templateName);
 	}
 
 	protected JButton getHelpLabel() {
 		if (helpLabel == null) {
 			helpLabel = new JButton("");
 			helpLabel.setBorder(null);
 			Icon icon = new ImageIcon(TextField.class
 					.getResource("/help_icon.gif"));
 			helpLabel.setIcon(icon);
 			helpLabel.setDisabledIcon(icon);
 			helpLabel.setEnabled(false);
 		}
 		return helpLabel;
 	}
 
 	protected HistoryManager getHistoryManager() {
 		return this.hm;
 	}
 
 	public List<String> getHistoryValues() {
 		return getHistoryValues(null);
 	}
 
 	public List<String> getHistoryValues(String optionalKey) {
 		if (StringUtils.isBlank(optionalKey)) {
 			return hm.getEntries(historyManagerEntryName);
 		} else {
 			return hm.getEntries(historyManagerEntryName + "_" + optionalKey);
 		}
 	}
 
 	abstract public JComboBox getJComboBox();
 
 	protected JobSubmissionObjectImpl getJobSubmissionObject() {
 		return jobObject;
 	}
 
 	protected String getLastValue() {
 		return getLastValue(null);
 	}
 
 	protected String getLastValue(String optionalKey) {
 		if (StringUtils.isBlank(optionalKey)) {
 			return hm.getLastEntry(historyManagerEntryName);
 		} else {
 			return hm.getLastEntry(historyManagerEntryName + "_" + optionalKey);
 		}
 	}
 
 	public String getPanelName() {
 		return this.panelProperties.get(NAME);
 	}
 
 	protected String getPanelProperty(String key) {
 		if (panelProperties == null) {
 			throw new IllegalStateException(
 					"Panel properties not initialized yet");
 		}
 		return panelProperties.get(key);
 	}
 
 	// protected Object getValue(String bean) {
 	// try {
 	// Method method =
 	// jobObject.getClass().getMethod("get"+StringUtils.capitalize(bean));
 	// return method.invoke(jobObject);
 	// } catch (Exception e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// return null;
 	// }
 	// }
 
 	protected RunningJobManager getRunningJobManager() {
 
 		return this.rjm;
 	}
 
 	protected ServiceInterface getServiceInterface() {
 		return this.si;
 	}
 
 	abstract public JTextComponent getTextComponent();
 
 	protected UserEnvironmentManager getUserEnvironmentManager() {
 		return this.uem;
 	}
 
 	abstract protected String getValueAsString();
 
 	public void initPanel(TemplateObject template,
 			JobSubmissionObjectImpl jobObject) throws TemplateException {
 
 		if (si == null) {
 			throw new IllegalStateException("ServiceInterface not set yet.");
 		}
 
 		this.template = template;
 
 		// if (si != null) {
 		// // needed for example for the file dialog
 		// if (singletonServiceinterface == null) {
 		// singletonServiceinterface = si;
 		// }
 		// this.si = si;
 		// this.uem = GrisuRegistryManager.getDefault(si)
 		// .getUserEnvironmentManager();
 		// this.rjm = RunningJobManager.getDefault(si);
 		// this.hm = GrisuRegistryManager.getDefault(si).getHistoryManager();
 
 		if (useHistory()) {
 			if (StringUtils.isNotBlank(panelProperties.get(HISTORY_ITEMS))) {
 				try {
 					Integer max = Integer.parseInt(panelProperties
 							.get(HISTORY_ITEMS));
 					hm.setMaxNumberOfEntries(historyManagerEntryName, max);
 				} catch (Exception e) {
 					throw new TemplateException(
 							"Can't setup history management for panel "
 									+ getPanelName(), e);
 				}
 			}
 		}
 		// }
 
 		refresh(jobObject);
 
 	}
 
 	public boolean isDisplayed() {
 		return isVisible;
 	}
 
 	public boolean isInitFinished() {
 		return initFinished;
 	}
 
 	/**
 	 * Must be implemented if a change in a job property would possibly change
 	 * the value of one of the job properties this panel is responsible for.
 	 * 
 	 * @param e
 	 *            the property change event
 	 */
 	abstract protected void jobPropertyChanged(PropertyChangeEvent e);
 
 	protected GlazedFile popupFileDialogAndAskForFile() {
 
 		getFileDialog().setVisible(true);
 
 		GlazedFile file = getFileDialog().getSelectedFile();
 		getFileDialog().clearSelection();
 
 		GlazedFile currentDir = getFileDialog().getCurrentDirectory();
 
 		hm.addHistoryEntry(templateName + "_" + FILE_DIALOG_LAST_DIRECTORY_KEY,
 				currentDir.getUrl());
 
 		return file;
 	}
 
 	protected Set<GlazedFile> popupFileDialogAndAskForFiles() {
 
 		getFileDialog().setVisible(true);
 
 		Set<GlazedFile> files = getFileDialog().getSelectedFiles();
 		getFileDialog().clearSelection();
 
 		GlazedFile currentDir = getFileDialog().getCurrentDirectory();
 
 		hm.addHistoryEntry(templateName + "_" + FILE_DIALOG_LAST_DIRECTORY_KEY,
 				currentDir.getUrl());
 
 		return files;
 	}
 
 	/**
 	 * Implement this if the panel needs to be prepared with values from the
 	 * template.
 	 * 
 	 * @param panelProperties
 	 *            the properties for the initial state of the panel
 	 * @throws TemplateException
 	 */
 	abstract protected void preparePanel(Map<String, String> panelProperties)
 			throws TemplateException;
 
 	public void propertyChange(PropertyChangeEvent arg0) {
 		jobPropertyChanged(arg0);
 	}
 
 	public void refresh(JobSubmissionObjectImpl jobObject)
 			throws TemplateException {
 
 		initFinished = false;
 
 		if (this.jobObject != null) {
 			this.jobObject.removePropertyChangeListener(this);
 		}
 
 		this.jobObject = jobObject;
 		this.jobObject.addPropertyChangeListener(this);
 
 		templateRefresh(jobObject);
 
 		preparePanel(panelProperties);
 
 		initFinished = true;
 
 		setInitialValue();
 	}
 
 	protected void removeValue(String bean, Object value) {
 
 		if (!isInitFinished()) {
 			return;
 		}
 		try {
 			Method method = jobObject.getClass().getMethod(
 					"remove" + StringUtils.capitalize(bean), value.getClass());
 			method.invoke(jobObject, value);
 			applyFilters();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	abstract void setInitialValue() throws TemplateException;
 
 	public void setServiceInterface(ServiceInterface si) {
 
 		createSingletonFileDialog(si, templateName);
 
 		this.si = si;
 		this.uem = GrisuRegistryManager.getDefault(si)
 				.getUserEnvironmentManager();
 		this.rjm = RunningJobManager.getDefault(si);
 		this.hm = GrisuRegistryManager.getDefault(si).getHistoryManager();
 
 	}
 
 	protected void setValue(String bean, Object value) throws TemplateException {
 
 		if (!isInitFinished()) {
 			return;
 		}
 
 		try {
 
 			if (StringUtils.isNotBlank(bean)) {
				Method method = jobObject.getClass().getMethod(
 						"set" + StringUtils.capitalize(bean), value.getClass());
 				method.invoke(jobObject, value);
 			}
 			applyFilters();
 		} catch (Exception e) {
 			throw new TemplateException("Can't set value for property " + bean
 					+ ": " + e.getLocalizedMessage(), e);
 		}
 	}
 
 	abstract protected void templateRefresh(JobSubmissionObjectImpl jobObject);
 
 	@Override
 	public String toString() {
 
 		StringBuffer temp = new StringBuffer();
 
 		temp.append("Name: " + getName() + "\n");
 		temp.append("Class: " + this.getClass().toString() + "\n");
 		temp.append("Properties: \n");
 		for (String key : panelProperties.keySet()) {
 			temp.append("\t" + key + ": " + panelProperties.get(key) + "\n");
 		}
 		temp.append("Filters:\n");
 		for (Filter filter : filters) {
 			temp.append("\tClass: " + filter.getClass().toString() + "\n");
 		}
 
 		return temp.toString();
 
 	}
 
 	public boolean useHistory() {
 
 		try {
 			if (panelProperties.get(USE_HISTORY) != null) {
 				boolean use = Boolean.parseBoolean(panelProperties
 						.get(USE_HISTORY));
 				return use;
 			} else {
 				return true;
 			}
 		} catch (Exception e) {
 			return true;
 		}
 
 	}
 
 }
