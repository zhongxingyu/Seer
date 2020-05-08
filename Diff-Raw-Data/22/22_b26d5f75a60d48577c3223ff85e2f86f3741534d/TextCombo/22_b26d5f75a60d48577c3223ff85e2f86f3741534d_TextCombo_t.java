 package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.text.JTextComponent;
 
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.control.exceptions.TemplateException;
 import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
 import org.vpac.grisu.model.job.JobSubmissionObjectImpl;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class TextCombo extends AbstractInputPanel {
 
 	private JComboBox combobox;
 
	private DefaultComboBoxModel model;
 
 	public TextCombo(String name, PanelConfig config) throws TemplateException {
 
 		super(name, config);
 		// setLayout(new FormLayout(new ColumnSpec[] {
 		// FormFactory.RELATED_GAP_COLSPEC,
 		// ColumnSpec.decode("default:grow"),
 		// FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 		// FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 		// FormFactory.RELATED_GAP_ROWSPEC, }));
 
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 
 		if (displayHelpLabel()) {
 
 			add(getComboBox(), "2, 2, fill, fill");
 			add(getHelpLabel(), "4, 2");
 		} else {
 			add(getComboBox(), "2, 2, 3, 1, fill, fill");
 		}
 	}
 
 	private JComboBox getComboBox() {
 		if (combobox == null) {
			model = new DefaultComboBoxModel();
 			combobox = new JComboBox(model);
 			combobox.addKeyListener(new KeyAdapter() {
 
 				@Override
 				public void keyReleased(KeyEvent e) {
 					try {
 
 						// if ( StringUtils.isBlank(bean) ) {
 						// return;
 						// }
 
 						setValue(bean, combobox.getSelectedItem());
 					} catch (TemplateException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				}
 
 			});
 		}
 		return combobox;
 	}
 
 	@Override
 	protected Map<String, String> getDefaultPanelProperties() {
 
 		Map<String, String> defaultProperties = new HashMap<String, String>();
 
 		return defaultProperties;
 
 	}
 
 	@Override
 	public JComboBox getJComboBox() {
 		return getComboBox();
 	}
 
 	@Override
 	public JTextComponent getTextComponent() {
 		return null;
 	}
 
 	@Override
 	protected String getValueAsString() {
 		return (String) (combobox.getSelectedItem());
 	}
 
 	@Override
 	protected void jobPropertyChanged(PropertyChangeEvent e) {
 
 	}
 
 	@Override
 	protected void preparePanel(Map<String, String> panelProperties)
 			throws TemplateException {
 
 		getComboBox().removeAllItems();
 
 		String prefills = panelProperties.get(PREFILLS);
 		if (StringUtils.isNotBlank(prefills)) {
 
 			for (String value : prefills.split(",")) {
 				model.addElement(value);
 			}
 
 		}
 
 		if (useHistory()) {
 			for (String value : getHistoryValues()) {
 				if (model.getIndexOf(value) < 0) {
 					model.addElement(value);
 				}
 			}
 		}
 
 		if (fillDefaultValueIntoFieldWhenPreparingPanel()) {
 			getJobSubmissionObject().addInputFileUrl(getDefaultValue());
 			getComboBox().setSelectedItem(getDefaultValue());
 		} else {
 			getComboBox().setSelectedItem("");
 		}
 	}
 
 	@Override
 	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {
 
 		if (useHistory()) {
 			addValueToHistory();
 		}
 	}
 
 }
