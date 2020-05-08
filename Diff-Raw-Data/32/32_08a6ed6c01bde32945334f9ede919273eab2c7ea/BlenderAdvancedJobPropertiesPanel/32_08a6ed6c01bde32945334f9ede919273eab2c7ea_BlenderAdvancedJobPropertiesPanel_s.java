 package org.vpac.grisu.clients.blender.swing;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
import org.apache.commons.lang.StringUtils;

 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jidesoft.swing.CheckBoxList;
 
 public class BlenderAdvancedJobPropertiesPanel extends JPanel {
 	private JTextField textField;
 
 	private final BlenderJobCreationPanel parent;
 	private final ButtonGroup buttonGroup;
 
 	private final CheckBoxList includeList;
 	private final CheckBoxList excludeList;
 
 	private final JRadioButton includeRadioButton;
 	private final JRadioButton excludeRadioButton;
 
 	final JCheckBox selectSubLocsCheckBox;
 
 	final DefaultListModel includeModel = new DefaultListModel();
 	final DefaultListModel excludeModel = new DefaultListModel();
 	private final JSeparator separator;
 
 	/**
 	 * Create the panel.
 	 */
 	public BlenderAdvancedJobPropertiesPanel(BlenderJobCreationPanel parent) {
 		this.parent = parent;
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("124px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("37px:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("29px:grow"),
 				FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("124px"),
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("24px"),
 				RowSpec.decode("8dlu"), FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 
 		buttonGroup = new ButtonGroup();
 
 		JLabel lblNameOfOutput = new JLabel("Name of output frames");
 		add(lblNameOfOutput, "2, 2, right, center");
 
 		add(getOutputNameTextField(), "4, 2, fill, top");
 
 		selectSubLocsCheckBox = new JCheckBox(
				"Manually select submission locations");
 		selectSubLocsCheckBox.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 
 				if (selectSubLocsCheckBox.isSelected()) {
 					enableManualSubLocSelect(true);
 				} else {
 					enableManualSubLocSelect(false);
 				}
 
 			}
 		});
 
 		separator = new JSeparator();
 		add(separator, "2, 3, 7, 1");
 		add(selectSubLocsCheckBox, "2, 4, 7, 1");
 
 		includeRadioButton = new JRadioButton("include");
 		includeRadioButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				switchToInclude();
 			}
 		});
 
 		add(includeRadioButton, "2, 5, 3, 1, left, default");
 		buttonGroup.add(includeRadioButton);
 
 		excludeRadioButton = new JRadioButton("exclude");
 		excludeRadioButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				switchToExclude();
 			}
 		});
 		add(excludeRadioButton, "6, 5, 3, 1, left, default");
 		buttonGroup.add(excludeRadioButton);
 
 		includeList = new CheckBoxList(includeModel);
 		add(includeList, "2, 7, 3, 1, fill, fill");
 
 		excludeList = new CheckBoxList(excludeModel);
 		excludeList.selectNone();
 		add(excludeList, "6, 7, 3, 1, fill, fill");
 
 		// startup
 		includeRadioButton.setSelected(true);
 		setAvailableSubLocs();
 		enableManualSubLocSelect(false);
 	}
 
 	private void enableManualSubLocSelect(boolean enable) {
 
 		if (enable) {
 			includeRadioButton.setEnabled(true);
 			excludeRadioButton.setEnabled(true);
 			if (includeRadioButton.isSelected()) {
 				switchToInclude();
 			} else {
 				switchToExclude();
 			}
 
 		} else {
 			includeList.setEnabled(false);
 			excludeList.setEnabled(false);
 			includeRadioButton.setEnabled(false);
 			excludeRadioButton.setEnabled(false);
 		}
 
 	}
 
 	public String getOutputFilename() {
 
		if (StringUtils.isNotBlank(getOutputNameTextField().getText())) {
			return getOutputNameTextField().getText();
		} else {
			return null;
		}

 	}
 
 	private JTextField getOutputNameTextField() {
 		if (textField == null) {
 			textField = new JTextField();
 			textField.setColumns(10);
 		}
 		return textField;
 	}
 
 	public Set<String> getSubLocsToExclude() {
 		if (!selectSubLocsCheckBox.isSelected()) {
 			return null;
 		}
 
 		if (!excludeRadioButton.isSelected()) {
 			return null;
 		}
 		Set<String> result = new TreeSet<String>();
 		for (Object o : excludeList.getCheckBoxListSelectedValues()) {
 			result.add((String) o);
 		}
 		return result;
 	}
 
 	public Set<String> getSubLocsToInclude() {
 
 		if (!selectSubLocsCheckBox.isSelected()) {
 			return null;
 		}
 
 		if (!includeRadioButton.isSelected()) {
 			return null;
 		}
 		Set<String> result = new TreeSet<String>();
 		for (Object o : includeList.getCheckBoxListSelectedValues()) {
 			result.add((String) o);
 		}
 		return result;
 	}
 
 	public void lockUI(final boolean lock) {
 
 		SwingUtilities.invokeLater(new Thread() {
 
 			@Override
 			public void run() {
 				selectSubLocsCheckBox.setEnabled(!lock);
 				getOutputNameTextField().setEnabled(!lock);
 
 				if (lock) {
 					includeList.setEnabled(false);
 					excludeList.setEnabled(false);
 					includeRadioButton.setEnabled(false);
 					excludeRadioButton.setEnabled(false);
 				} else {
 					if (selectSubLocsCheckBox.isSelected()) {
 						enableManualSubLocSelect(true);
 					} else {
 						enableManualSubLocSelect(false);
 					}
 				}
 			}
 		});
 	}
 
 	public void setAvailableSubLocs() {
 
 		includeModel.removeAllElements();
 		excludeModel.removeAllElements();
 
 		for (String subLoc : parent.getAllPossibleSubmissionLocations()) {
 			includeModel.addElement(subLoc);
 			excludeModel.addElement(subLoc);
 		}
 
 		includeList.selectNone();
 		excludeList.selectNone();
 
 	}
 
 	public void setOutputFilename(String name) {
 		getOutputNameTextField().setText(name);
 	}
 
 	private void switchToExclude() {
 		excludeList.setEnabled(true);
 		includeList.setEnabled(false);
 	}
 
 	private void switchToInclude() {
 
 		excludeList.setEnabled(false);
 		includeList.setEnabled(true);
 
 	}
 
 }
