 package org.vpac.grisu.client.view.swing.template.panels;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.net.URI;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 import javax.swing.border.TitledBorder;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.model.files.GrisuFileObject;
 import org.vpac.grisu.client.model.template.nodes.TemplateNode;
 import org.vpac.grisu.client.model.template.nodes.TemplateNodeEvent;
 import org.vpac.grisu.client.view.swing.files.FileChooserEvent;
 import org.vpac.grisu.client.view.swing.files.FileChooserParent;
 import org.vpac.grisu.client.view.swing.files.SiteFileChooserPanel;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.historyRepeater.HistoryManager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class MultipleInputFiles extends JPanel implements TemplateNodePanel,
 		FileChooserParent {
 
 	static final Logger myLogger = Logger.getLogger(MultipleInputFiles.class
 			.getName());
 	
 	private JTextArea textArea;
 	private JScrollPane scrollPane_1;
 	private JLabel errorLabel;
 	private JLabel requiredLabel;
 	private SiteFileChooserPanel siteFileChooserPanel;
 	private JList list;
 	private JButton addButton;
 	private JPanel fileChooserPanel;
 	private JScrollPane scrollPane;
 	private JButton removeButton;
 	private JPanel choosenFilesPanel;
 	private JSplitPane splitPane;
 
 	private DefaultListModel selectedFilesModel = new DefaultListModel();
 
 	private TemplateNode templateNode = null;
 
 	private FormLayout layout = null;
 
 	private String lastDirectoryKey = InputFile.DEFAULT_LAST_DIRECTORY_VALUE;
 
 	private HistoryManager historyManager = null;
 	
 	private EnvironmentManager em = null;
 
 	/**
 	 * Create the panel
 	 */
 	public MultipleInputFiles() {
 		super();
 		setPreferredSize(new Dimension(400, 400));
 		setMinimumSize(new Dimension(0, 700));
 		layout = new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("36px"),
 				ColumnSpec.decode("left:15dlu"),
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("53dlu:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("34px"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("145dlu:grow(1.0)"),
 				FormFactory.RELATED_GAP_ROWSPEC});
 		setLayout(layout);
 		add(getRequiredLabel(), new CellConstraints(6, 2, 1, 3,
 				CellConstraints.RIGHT, CellConstraints.TOP));
 		add(getErrorLabel(), new CellConstraints(2, 4, 5, 1,
 				CellConstraints.FILL, CellConstraints.CENTER));
 		add(getSplitPane(), new CellConstraints("2, 6, 5, 1, fill, fill"));
 
 		//
 	}
 
 	public JPanel getTemplateNodePanel() {
 		return this;
 	}
 
 	public void reset() {
 
 		selectedFilesModel.removeAllElements();
 
 	}
 
 	public void setTemplateNode(TemplateNode node)
 			throws TemplateNodePanelException {
 
 		this.em = node.getTemplate().getEnvironmentManager();
 		fileChooserPanel.add(getSiteFileChooserPanel(),
 				new CellConstraints(2, 2, CellConstraints.FILL,
 						CellConstraints.FILL));
 		this.templateNode = node;
 		this.templateNode.setTemplateNodeValueSetter(this);
 		this.templateNode.addTemplateNodeListener(this);
 
 		historyManager = this.templateNode.getTemplate()
 				.getEnvironmentManager().getHistoryManager();
 
 		if ("1".equals(this.templateNode.getMultiplicity())) {
 			getRequiredLabel().setText("*");
 		} else {
 			getRequiredLabel().setText("");
 		}
 
 		if (this.templateNode.hasProperty(InputFile.LAST_DIRECTORY_KEY)) {
 			lastDirectoryKey = this.templateNode
 					.getOtherProperty(InputFile.LAST_DIRECTORY_KEY)
 					+ "_dirKey";
		} else {
			lastDirectoryKey = System.getProperty("user.home");
 		}
 
 		// try to change to the last used directory
 		String changeToDirectory = null;
 
 		// change to appropriate directory
 		try {
 			changeToDirectory = historyManager.getEntries(lastDirectoryKey)
 					.get(0);
 			URI uri = null;
 			try {
 				uri = new URI(changeToDirectory);
 			
 				if ( ! new File(uri).exists() || ! new File(uri).canRead() ) {
 					uri = new File(System.getProperty("user.home")).toURI();
 				}
 
 			} catch (Exception uriE) {
 				uri = new File(System.getProperty("user.home")).toURI();
 			}
 
 			GrisuFileObject dir = templateNode.getTemplate()
 					.getEnvironmentManager().getFileManager()
 					.getFileObject(uri);
 			getSiteFileChooserPanel().changeCurrentDirectory(dir);
 		} catch (Exception e) {
 			// try to change to users home dir again...
 			try {
 				URI uri = null;
 				uri = new File(System.getProperty("user.home")).toURI();
 				
 				GrisuFileObject dir = templateNode.getTemplate()
 				.getEnvironmentManager().getFileManager()
 				.getFileObject(uri);
 		getSiteFileChooserPanel().changeCurrentDirectory(dir);
 				
 			} catch (Exception e2) {
 				// do nothing in that case
 			}
 		}
 
 		setBorder(new TitledBorder(null, templateNode.getTitle(),
 				TitledBorder.DEFAULT_JUSTIFICATION,
 				TitledBorder.DEFAULT_POSITION, null, null));
 
 		String description = this.templateNode.getDescription();
 		if (!this.templateNode.hasProperty(TemplateNode.HIDE_DESCRIPTION)
 				&& description != null && !"".equals(description)) {
 			getTextArea().setText(description);
 			add(getScrollPane_1(), new CellConstraints(2, 2, 5, 1,
 					CellConstraints.FILL, CellConstraints.FILL));
 		} else {
 			layout.setRowSpec(2, new RowSpec("5dlu"));
 			layout.setRowSpec(3, new RowSpec("0dlu"));
 		}
 	}
 
 	public void templateNodeUpdated(TemplateNodeEvent event) {
 
 		if (event.getEventType() == TemplateNodeEvent.TEMPLATE_PROCESSED_INVALID
 				|| event.getEventType() == TemplateNodeEvent.TEMPLATE_FILLED_INVALID) {
 			String message = event.getMessage();
 			if (message == null)
 				message = TemplateNodeEvent.DEFAULT_PROCESSED_INVALID_MESSAGE;
 
 			errorLabel.setText(message);
 			errorLabel.setVisible(true);
 
 			getRequiredLabel().setForeground(Color.RED);
 
 		} else if (event.getEventType() == TemplateNodeEvent.TEMPLATE_PROCESSED_VALID) {
 			errorLabel.setVisible(false);
 		}
 
 	}
 
 	public String getExternalSetValue() {
 
 		if (selectedFilesModel.size() <= 0)
 			return JobConstants.DUMMY_STAGE_FILE;
 
 		StringBuffer result = new StringBuffer();
 		for (int i = 0; i < selectedFilesModel.getSize(); i++) {
 			GrisuFileObject file = (GrisuFileObject) selectedFilesModel
 					.get(i);
 
 			String fileURI = file.getURI().toString();
 			if (fileURI.startsWith("file:")) {
 				result.append(fileURI.substring(5) + ";");
 			} else {
 				result.append(fileURI + ";");
 			}
 		}
 
 		return result.substring(0, result.length() - 1);
 	}
 	
 	public void setExternalSetValue(String value) {
 
 		if ( value != null ) {
 			selectedFilesModel.removeAllElements();
 			for ( String file : value.split(";") ) {
 				selectedFilesModel.addElement(file);
 			}
 		}
 	}
 
 	public void userInput(FileChooserEvent event) {
 
 		if (FileChooserEvent.SELECTED_FILE == event.getType()) {
 			addFilesToSelection(new GrisuFileObject[] { event
 					.getSelectedFile() });
 		} else if (FileChooserEvent.SELECTED_FILES == event.getType()) {
 			addFilesToSelection(event.getSelectedFiles());
 		} else if (FileChooserEvent.CHANGED_FOLDER == event.getType()) {
 			// save last directory
 			try {
 				GrisuFileObject dir = getSiteFileChooserPanel()
 						.getCurrentDirectory();
 				historyManager.addHistoryEntry(lastDirectoryKey, dir.getURI()
 						.toString(), new Date(), 1);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void addFilesToSelection(GrisuFileObject[] files) {
 		for (GrisuFileObject file : files) {
 			selectedFilesModel.addElement(file);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	protected JSplitPane getSplitPane() {
 		if (splitPane == null) {
 			splitPane = new JSplitPane();
 			splitPane.setRightComponent(getChoosenFilesPanel());
 			splitPane.setLeftComponent(getFileChooserPanel());
 			// splitPane.setDividerLocation(.8D);
 			Dimension minimumSize = new Dimension(250, 50);
 			splitPane.getLeftComponent().setMinimumSize(minimumSize);
 			splitPane.setResizeWeight(0.0);
 		}
 		return splitPane;
 	}
 
 	/**
 	 * @return
 	 */
 	/**
 	 * @return
 	 */
 	protected JPanel getChoosenFilesPanel() {
 		if (choosenFilesPanel == null) {
 			choosenFilesPanel = new JPanel();
 			choosenFilesPanel.setLayout(new FormLayout(
 				new ColumnSpec[] {
 					FormFactory.RELATED_GAP_COLSPEC,
 					new ColumnSpec("56dlu:grow(1.0)"),
 					FormFactory.RELATED_GAP_COLSPEC},
 				new RowSpec[] {
 					FormFactory.RELATED_GAP_ROWSPEC,
 					new RowSpec("default:grow(1.0)"),
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC}));
 			choosenFilesPanel.add(getRemoveButton(), new CellConstraints(2, 4,
 					CellConstraints.RIGHT, CellConstraints.DEFAULT));
 			choosenFilesPanel.add(getScrollPane(), new CellConstraints(2, 2,
 					CellConstraints.FILL, CellConstraints.FILL));
 		}
 		return choosenFilesPanel;
 	}
 
 	/**
 	 * @return
 	 */
 	/**
 	 * @return
 	 */
 	protected JButton getRemoveButton() {
 		if (removeButton == null) {
 			removeButton = new JButton();
 			removeButton.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					Object[] selectedFiles = getList().getSelectedValues();
 					for (Object file : selectedFiles) {
 						selectedFilesModel.removeElement(file);
 					}
 
 				}
 			});
 			removeButton.setText("-");
 		}
 		return removeButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JScrollPane getScrollPane() {
 		if (scrollPane == null) {
 			scrollPane = new JScrollPane();
 			scrollPane.setViewportView(getList());
 		}
 		return scrollPane;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JPanel getFileChooserPanel() {
 		if (fileChooserPanel == null) {
 			fileChooserPanel = new JPanel();
 			fileChooserPanel.setLayout(new FormLayout(
 				new ColumnSpec[] {
 					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 					ColumnSpec.decode("313px:grow(1.0)"),
 					FormFactory.RELATED_GAP_COLSPEC},
 				new RowSpec[] {
 					FormFactory.RELATED_GAP_ROWSPEC,
 					RowSpec.decode("default:grow(1.0)"),
 					FormFactory.RELATED_GAP_ROWSPEC,
 					RowSpec.decode("25px"),
 					FormFactory.RELATED_GAP_ROWSPEC}));
 			fileChooserPanel.add(getAddButton(), new CellConstraints(
 					"2, 4, 1, 1, right, fill"));
 
 		}
 		return fileChooserPanel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getAddButton() {
 		if (addButton == null) {
 			addButton = new JButton();
 			addButton.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					GrisuFileObject[] selectedFiles = getSiteFileChooserPanel()
 							.getSelectedFiles();
 					addFilesToSelection(selectedFiles);
 
 				}
 			});
 			addButton.setText("->");
 		}
 		return addButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JList getList() {
 		if (list == null) {
 			list = new JList(selectedFilesModel);
 		}
 		return list;
 	}
 
 	/**
 	 * @return
 	 */
 	protected SiteFileChooserPanel getSiteFileChooserPanel() {
 		if (siteFileChooserPanel == null) {
 			siteFileChooserPanel = new SiteFileChooserPanel(em);
 			siteFileChooserPanel.addUserInputListener(this);
 		}
 		return siteFileChooserPanel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getRequiredLabel() {
 		if (requiredLabel == null) {
 			requiredLabel = new JLabel();
 			requiredLabel.setVerticalTextPosition(SwingConstants.TOP);
 		}
 		return requiredLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getErrorLabel() {
 		if (errorLabel == null) {
 			errorLabel = new JLabel();
 			errorLabel.setVisible(false);
 			errorLabel.setForeground(Color.RED);
 		}
 		return errorLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JScrollPane getScrollPane_1() {
 		if (scrollPane_1 == null) {
 			scrollPane_1 = new JScrollPane();
 			scrollPane_1.setViewportView(getTextArea());
 		}
 		return scrollPane_1;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JTextArea getTextArea() {
 		if (textArea == null) {
 			textArea = new JTextArea();
 			textArea.setEditable(false);
 		}
 		return textArea;
 	}
 	// event stuff
 	// ========================================================
 	
 	private Vector<ValueListener> valueChangedListeners;
 
 	private void fireSitePanelEvent(String newValue) {
 		
 		myLogger.debug("Fire value changed event: new value: "+newValue);
 		// if we have no mountPointsListeners, do nothing...
 		if (valueChangedListeners != null && !valueChangedListeners.isEmpty()) {
 
 			// make a copy of the listener list in case
 			// anyone adds/removes mountPointsListeners
 			Vector<ValueListener> valueChangedTargets;
 			synchronized (this) {
 				valueChangedTargets = (Vector<ValueListener>) valueChangedListeners.clone();
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
 
 	// register a listener
 	synchronized public void addValueListener(ValueListener l) {
 		if (valueChangedListeners == null)
 			valueChangedListeners = new Vector<ValueListener>();
 		valueChangedListeners.addElement(l);
 	}
 
 	// remove a listener
 	synchronized public void removeValueListener(ValueListener l) {
 		if (valueChangedListeners == null) {
 			valueChangedListeners = new Vector<ValueListener>();
 		}
 		valueChangedListeners.removeElement(l);
 	}
 
 
 
 
 }
