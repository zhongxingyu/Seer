 package org.vpac.grisu.frontend.view.swing.jobcreation.widgets;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 import javax.swing.border.TitledBorder;
 
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.frontend.view.swing.files.GrisuFileDialog;
 import org.vpac.grisu.frontend.view.swing.utils.FirstItemPromptItemRenderer;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.files.GlazedFile;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class SingleInputFile extends AbstractWidget {
 
 	private JComboBox comboBox;
 	private JButton btnBrowse;
 
 	protected final DefaultComboBoxModel fileModel = new DefaultComboBoxModel();
 
 	private GrisuFileDialog fileDialog = null;
 
 	public final String selString = "Please select a file";
 
 	/**
 	 * Create the panel.
 	 */
 	public SingleInputFile() {
 		super();
 		setBorder(new TitledBorder(null, "Input file", TitledBorder.LEADING,
 				TitledBorder.TOP, null, null));
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getComboBox(), "2, 2, fill, default");
 		add(getBtnBrowse(), "4, 2");
 
 	}
 
 	@Override
 	protected boolean setLastValue() {
 		return false;
 	}
 
 	protected GlazedFile popupFileDialogAndAskForFile() {
 
 		if (getServiceInterface() == null) {
 			return null;
 		}
 
 		getFileDialog().setVisible(true);
 
 		GlazedFile file = getFileDialog().getSelectedFile();
 		getFileDialog().clearSelection();
 
 		GlazedFile currentDir = getFileDialog().getCurrentDirectory();
 
 		if (StringUtils.isNotBlank(getHistoryKey())) {
 			getHistoryManager().addHistoryEntry(getHistoryKey() + "_last_dir",
 					currentDir.getUrl());
 		}
 
 		return file;
 	}
 
 	protected GrisuFileDialog getFileDialog() {
 
 		if (fileDialog == null) {
 			String startUrl = getHistoryManager().getLastEntry(
 					getHistoryKey() + "_last_dir");
 
 			if (StringUtils.isBlank(startUrl)) {
 				startUrl = new File(System.getProperty("user.home")).toURI()
 						.toString();
 			} else if (!FileManager.isLocal(startUrl)) {
 				try {
 					if (!getServiceInterface().isFolder(startUrl)) {
 						startUrl = new File(System.getProperty("user.home"))
 								.toURI().toString();
 					}
 				} catch (RemoteFileSystemException e) {
 					myLogger.debug(e);
 					startUrl = new File(System.getProperty("user.home"))
 							.toURI().toString();
 				}
 			}
 			fileDialog = new GrisuFileDialog(getServiceInterface(), startUrl);
 			fileDialog
 					.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
 		}
 		return fileDialog;
 
 	}
 
 	private JComboBox getComboBox() {
 		if (comboBox == null) {
 			comboBox = new JComboBox(fileModel);
 			comboBox.setEditable(false);
			comboBox.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
 			comboBox.addItem(selString);
 			comboBox.setRenderer(new FirstItemPromptItemRenderer(selString));
 			comboBox.addItemListener(new ItemListener() {
 
 				public void itemStateChanged(ItemEvent e) {
 
 					if (ItemEvent.SELECTED == e.getStateChange()) {
 
 						if (StringUtils.isNotBlank((String) fileModel
 								.getSelectedItem())) {
 
 							setInputFile((String) fileModel.getSelectedItem());
 							getPropertyChangeSupport().firePropertyChange(
 									"inputFileUrl", null, getValue());
 						}
 					}
 				}
 
 			});
 		}
 
 		return comboBox;
 	}
 
 	protected void setInputFile(String url) {
 
 		if (StringUtils.isBlank(url)) {
 			fileModel.setSelectedItem(selString);
 			return;
 		}
 
 		int index = fileModel.getIndexOf(url);
 		if (index < 0) {
 			fileModel.addElement(url);
 		}
 		fileModel.setSelectedItem(url);
 	}
 
 	public String getInputFileUrl() {
 		String temp = (String) getComboBox().getSelectedItem();
 
 		if (selString.equals(temp)) {
 			return null;
 		} else {
 			return temp;
 		}
 	}
 
 	protected JButton getBtnBrowse() {
 		if (btnBrowse == null) {
 			btnBrowse = new JButton("Browse");
 			btnBrowse.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 
 					String oldValue = getValue();
 
 					GlazedFile file = popupFileDialogAndAskForFile();
 
 					if (file == null) {
 						return;
 					}
 
 					setInputFile(file.getUrl());
 					getPropertyChangeSupport().firePropertyChange(
 							"inputFileUrl", oldValue, getValue());
 				}
 			});
 		}
 		return btnBrowse;
 	}
 
 	@Override
 	public void setValue(String value) {
 		setInputFile(value);
 	}
 
 	@Override
 	public String getValue() {
 		return getInputFileUrl();
 	}
 
 	@Override
 	public void historyKeySet() {
 		getHistoryManager().setMaxNumberOfEntries(getHistoryKey(), 8);
 		for (String entry : getHistoryManager().getEntries(getHistoryKey())) {
 			if (fileModel.getIndexOf(entry) < 0) {
 				fileModel.addElement(entry);
 			}
 		}
 	}
 
 	@Override
 	public void lockIUI(final boolean lock) {
 		SwingUtilities.invokeLater(new Thread() {
 			@Override
 			public void run() {
 				getComboBox().setEnabled(!lock);
 				getBtnBrowse().setEnabled(!lock);
 			}
 		});
 
 	}
 }
