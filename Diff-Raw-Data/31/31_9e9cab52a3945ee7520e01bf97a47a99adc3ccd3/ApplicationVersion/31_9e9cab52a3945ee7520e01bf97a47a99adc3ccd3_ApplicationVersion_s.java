 package org.vpac.grisu.client.view.swing.template.panels;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.border.TitledBorder;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.TemplateTagConstants;
 import org.vpac.grisu.client.model.template.nodes.TemplateNode;
 import org.vpac.grisu.client.model.template.nodes.TemplateNodeEvent;
 import org.vpac.grisu.model.FqanEvent;
 import org.vpac.grisu.model.FqanListener;
 import org.vpac.grisu.model.GrisuRegistry;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.info.UserApplicationInformation;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 public class ApplicationVersion extends JPanel implements TemplateNodePanel,
 		FqanListener, ActionListener {
 
 	public static final String DEFAULT_VERSION_NOT_AVAILABLE_STRING = "Default version not available.";
 	public static final String ANY_VERSION_STRING = "Auto-selected";
 
 	public static final String ANY_MODE_TEMPLATETAG_KEY = "useAny";
 	public static final String DEFAULT_MODE_TEMPLATETAG_KEY = "useDefault";
 	public static final String EXACT_MODE_TEMPLATETAG_KEY = "useExact";
 
 	public static final String ANY_MODE_STRING = "Any";
 	public static final String DEFAULT_MODE_STRING = "Default";
 	public static final String EXACT_MODE_STRING = "Exact";
 
 	// Version
 	public final static int DEFAULT_VERSION_MODE = 0;
 	public final static int ANY_VERSION_MODE = 1;
 	public final static int EXACT_VERSION_MODE = 2;
 
 	public static final String STARTUP_MODE_KEY = "startMode";
 
 	private GrisuRegistry registry = null;
 	private UserApplicationInformation infoObject = null;
 
 	private String applicationName;
 	private String defaultVersion;
 	private String lastVersion;
 
 	private String startMode = null;
 
 	private boolean useAny = true;
 	private boolean useDefault = true;
 	private boolean useExact = true;
 
 	private short currentMode = -1;
 
 	private JRadioButton anyRadioButton;
 	private JRadioButton defaultRadioButton;
 	private JRadioButton exactRadioButton;
 	private ButtonGroup modeGroup = new ButtonGroup();
 
 	private JComboBox versionComboBox;
 	private DefaultComboBoxModel versionModel = new DefaultComboBoxModel();
 
 	private String currentVersion;
 	private boolean versionLocked = false;
 	
 	private String lastSelectedExactVersion = null;
 
 	private TemplateNode templateNode;
 
 	public ApplicationVersion() {
 		setBorder(new TitledBorder(null, "Version", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 	}
 
 	static final Logger myLogger = Logger.getLogger(ApplicationVersion.class
 			.getName());
 
 	public JPanel getTemplateNodePanel() {
 		return this;
 	}
 
 	public void setTemplateNode(TemplateNode node)
 			throws TemplateNodePanelException {
 
 		this.templateNode = node;
 		this.templateNode.setTemplateNodeValueSetter(this);
 
 		this.applicationName = this.templateNode.getTemplate()
 				.getApplicationName();
 		defaultVersion = node.getDefaultValue();
 
 		registry = GrisuRegistryManager.getDefault(node.getTemplate()
 				.getEnvironmentManager().getServiceInterface());
 		registry.getUserEnvironmentManager().addFqanListener(this);
 
 		infoObject = registry.getUserApplicationInformation(applicationName);
 
 		try {
 			lastVersion = registry.getHistoryManager().getEntries(
 					TemplateTagConstants.getGlobalLastVersionKey(infoObject
 							.getApplicationName())).get(0);
 			lastSelectedExactVersion = lastVersion;
 		} catch (Exception e) {
 			lastVersion = null;
 		}
 
 		this.useAny = node.getOtherProperties().containsKey(
 				ANY_MODE_TEMPLATETAG_KEY);
 		this.useDefault = node.getOtherProperties().containsKey(
 				DEFAULT_MODE_TEMPLATETAG_KEY);
 		this.useExact = node.getOtherProperties().containsKey(
 				EXACT_MODE_TEMPLATETAG_KEY);
 
 		if (node.getOtherProperties().containsKey(STARTUP_MODE_KEY)) {
 			startMode = node.getOtherProperty(STARTUP_MODE_KEY);
 			if (ANY_MODE_STRING.equals(startMode)) {
 				useAny = true;
 			} else if (EXACT_MODE_STRING.equals(startMode)) {
 				useExact = true;
 			} else if (DEFAULT_MODE_STRING.equals(startMode)) {
 
 				if (StringUtils.isBlank(defaultVersion)) {
 					useDefault = false;
 					myLogger
 							.warn("Not using default mode because no default version value is specified in template.");
 				} else {
 					lastVersion = defaultVersion;
 					useDefault = true;
 				}
 			}
 		} else {
 			try {
 				startMode = registry.getHistoryManager().getEntries(
 						TemplateTagConstants
 								.getGlobalLastVersionModeKey(infoObject
 										.getApplicationName())).get(0);
 
 				if (ANY_MODE_STRING.equals(startMode) && !useAny) {
 					startMode = null;
 				} else if (DEFAULT_MODE_STRING.equals(startMode) && !useDefault) {
 					startMode = null;
 				} else if (EXACT_MODE_STRING.equals(startMode)) {
 					startMode = null;
 				}
 
 			} catch (Exception e) {
 				myLogger
 						.debug("Can't get last used parameter for version mode.");
 			}
 
 			if (startMode == null) {
 
 				if (useAny) {
 					startMode = ANY_MODE_STRING;
 				} else if (useExact) {
 					startMode = EXACT_MODE_STRING;
 				} else if (useDefault) {
 					startMode = DEFAULT_MODE_STRING;
 				} else {
 					useAny = true;
 					startMode = ANY_MODE_STRING;
 				}
 
 			}
 		}
 
 		if (DEFAULT_MODE_STRING.equals(startMode)) {
 			currentMode = DEFAULT_VERSION_MODE;
 		} else if (EXACT_MODE_STRING.equals(startMode)) {
 			currentMode = EXACT_VERSION_MODE;
 		} else {
 			currentMode = ANY_VERSION_MODE;
 		}
 
 		if (useAny) {
 			add(getAnyRadioButton());
 		}
 		if (useDefault) {
 			add(getDefaultRadioButton());
 		}
 		if (useExact) {
 			add(getExactRadioButton());
 		}
 
 		// add combobox as last item
 		add(getVersionComboBox());
 
 		fillVersions(lastVersion);
 
 		switch (currentMode) {
 		case ANY_VERSION_MODE:
 			getAnyRadioButton().doClick();
 			break;
 		case DEFAULT_VERSION_MODE:
 			getDefaultRadioButton().doClick();
 			break;
 		case EXACT_VERSION_MODE:
 			getExactRadioButton().doClick();
 			break;
 		}
 
 	}
 
 	private void fillVersions(String preferedVersion) {
 
 		versionLocked = true;

 		versionModel.removeAllElements();
 		for (String version : infoObject
 				.getAllAvailableVersionsForFqan(registry
 						.getUserEnvironmentManager().getCurrentFqan())) {
 			versionModel.addElement(version);
 		}
 
 		if (versionModel.getIndexOf(preferedVersion) >= 0) {
 			versionModel.setSelectedItem(preferedVersion);
 		} else {
 			if (versionModel.getSize() > 0) {
 				versionModel.setSelectedItem(versionModel.getElementAt(0));
 			}
 		}
 
 		versionLocked = false;
 
 	}
 
 	public void templateNodeUpdated(TemplateNodeEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public String getExternalSetValue() {
 		return currentVersion;
 	}
 
 	public void reset() {
 	}
 
 	public void setExternalSetValue(String value) {
 		myLogger.warn("Not supported yet.");
 		throw new RuntimeException("Setting value not supported yet.");
 
 	}
 
 	public void fqansChanged(FqanEvent event) {
		// TODO Auto-generated method stub
 
 	}
 
 	private void switchMode(String mode) {
 
 		if (ANY_MODE_STRING.equals(mode)) {
 			switchToAnyMode();
 			return;
 		} else if (DEFAULT_MODE_STRING.equals(mode)) {
 			switchToDefaultMode();
 			return;
 		} else if (EXACT_MODE_STRING.equals(mode)) {
 			switchToExactMode();
 			return;
 		}
 
 		myLogger.error("Mode not supported: " + mode);
 	}
 
 	private void switchToAnyMode() {
 
 		getVersionComboBox().setEnabled(false);
 		versionLocked = true;
 		versionModel.setSelectedItem(ANY_MODE_STRING);
 		versionModel.removeElement(DEFAULT_VERSION_NOT_AVAILABLE_STRING);
 		versionLocked = false;
 
 		this.currentMode = ANY_VERSION_MODE;
 		this.currentVersion = Constants.NO_VERSION_INDICATOR_STRING;
 
 		registry.getHistoryManager().addHistoryEntry(
 				TemplateTagConstants.getGlobalLastVersionModeKey(infoObject
 						.getApplicationName()), ANY_MODE_STRING, new Date());
 		
 		
 		fireVersionChanged(this.currentVersion);
 
 	}
 
 	private void switchToDefaultMode() {
 
 		versionLocked = true;
 		versionModel.removeElement(ANY_MODE_STRING);
 		int index = versionModel.getIndexOf(defaultVersion);
 		if ( ! Constants.NO_VERSION_INDICATOR_STRING.equals(defaultVersion) && index < 0 ) {
			defaultVersion = DEFAULT_VERSION_NOT_AVAILABLE_STRING;
 		}
		versionModel.setSelectedItem(defaultVersion);
 		versionLocked = false;
 		
 		getVersionComboBox().setEnabled(false);
 		this.currentMode = DEFAULT_VERSION_MODE;
		this.currentVersion = defaultVersion;
 
 		registry.getHistoryManager()
 				.addHistoryEntry(
 						TemplateTagConstants
 								.getGlobalLastVersionModeKey(infoObject
 										.getApplicationName()),
 						DEFAULT_MODE_STRING, new Date());
 
 		fireVersionChanged(this.currentVersion);
 	}
 
 	private void switchToExactMode() {
 		
 		versionLocked = true;
 		versionModel.removeElement(ANY_MODE_STRING);
 		versionModel.removeElement(DEFAULT_VERSION_NOT_AVAILABLE_STRING);
 		if ( DEFAULT_VERSION_NOT_AVAILABLE_STRING.equals(versionModel.getSelectedItem()) || ANY_MODE_STRING.equals(versionModel.getSelectedItem()) ) {
 			versionComboBox.setSelectedIndex(0);
 		}
 		
 		if ( lastSelectedExactVersion != null ) {
 			versionModel.setSelectedItem(lastSelectedExactVersion);
 		}
 		versionLocked = false;
 
 		getVersionComboBox().setEnabled(true);
 		this.currentMode = EXACT_VERSION_MODE;
 		this.currentVersion = (String) (versionModel.getSelectedItem());
 
 		registry.getHistoryManager().addHistoryEntry(
 				TemplateTagConstants.getGlobalLastVersionModeKey(infoObject
 						.getApplicationName()), EXACT_MODE_STRING, new Date());
 
 		fireVersionChanged(this.currentVersion);
 	}
 
 	protected JComboBox getVersionComboBox() {
 		if (versionComboBox == null) {
 			versionComboBox = new JComboBox(versionModel);
 			versionComboBox.addItemListener(new ItemListener() {
 				public void itemStateChanged(final ItemEvent e) {
 					if (!versionLocked) {
 						if (e.getStateChange() == ItemEvent.SELECTED) {
 
 							if (currentMode == EXACT_VERSION_MODE) {
 								String temp = (String) (versionModel
 										.getSelectedItem());
 								fireVersionChanged(temp);
 								registry
 										.getHistoryManager()
 										.addHistoryEntry(
 												TemplateTagConstants
 														.getGlobalLastVersionKey(infoObject
 																.getApplicationName()),
 												(String) (versionModel
 														.getSelectedItem()));
 								lastSelectedExactVersion = temp;
 							}
 
 						}
 					}
 				}
 			});
 			versionComboBox.setMaximumSize(new Dimension(300, 24));
 			versionComboBox.setEditable(false);
 		}
 		return versionComboBox;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JRadioButton getExactRadioButton() {
 		if (exactRadioButton == null) {
 			exactRadioButton = new JRadioButton();
 			exactRadioButton.setText("Exact");
 			modeGroup.add(exactRadioButton);
 			exactRadioButton.setActionCommand(EXACT_MODE_STRING);
 			exactRadioButton.addActionListener(this);
 		}
 		return exactRadioButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JRadioButton getDefaultRadioButton() {
 		if (defaultRadioButton == null) {
 			defaultRadioButton = new JRadioButton();
 			defaultRadioButton.setText("Default");
 			modeGroup.add(defaultRadioButton);
 			defaultRadioButton.setActionCommand(DEFAULT_MODE_STRING);
 			defaultRadioButton.addActionListener(this);
 		}
 		return defaultRadioButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JRadioButton getAnyRadioButton() {
 		if (anyRadioButton == null) {
 			anyRadioButton = new JRadioButton();
 			anyRadioButton.setText("Any");
 			modeGroup.add(anyRadioButton);
 			anyRadioButton.setActionCommand(ANY_MODE_STRING);
 			anyRadioButton.addActionListener(this);
 		}
 		return anyRadioButton;
 	}
 
 	// event stuff
 	private Vector<ValueListener> valueChangedListeners;
 
 	private void fireVersionChanged(String newValue) {
 
 		myLogger.debug("Fire value changed event from Version: new value: "
 				+ newValue);
 		// if we have no mountPointsListeners, do nothing...
 		if (valueChangedListeners != null && !valueChangedListeners.isEmpty()) {
 
 			// make a copy of the listener list in case
 			// anyone adds/removes mountPointsListeners
 			Vector<ValueListener> valueChangedTargets;
 			synchronized (this) {
 				valueChangedTargets = (Vector<ValueListener>) valueChangedListeners
 						.clone();
 			}
 
 			// walk through the listener list and
 			// call the gridproxychanged method in each
 			Enumeration<ValueListener> e = valueChangedTargets.elements();
 			while (e.hasMoreElements()) {
 				ValueListener valueChanged_l = (ValueListener) e.nextElement();
 				valueChanged_l.valueChanged(this, newValue);
 			}
 		}
 	}
 
 	public void addValueListener(ValueListener l) {
 		if (valueChangedListeners == null)
 			valueChangedListeners = new Vector<ValueListener>();
 		valueChangedListeners.addElement(l);
 	}
 
 	public void removeValueListener(ValueListener l) {
 		if (valueChangedListeners == null) {
 			valueChangedListeners = new Vector<ValueListener>();
 		}
 		valueChangedListeners.removeElement(l);
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 		switchMode(arg0.getActionCommand());
 	}
 
 }
