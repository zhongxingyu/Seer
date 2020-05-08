 package org.vpac.grisu.frontend.view.swing;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.apache.commons.lang.StringUtils;
 import org.bushe.swing.event.EventSubscriber;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.model.FqanEvent;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.UserEnvironmentManager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class DefaultFqanChangePanel extends JPanel implements
 		EventSubscriber<FqanEvent> {
 
 	private final DefaultComboBoxModel voModel = new DefaultComboBoxModel();
 	private ServiceInterface si = null;
 	private final JComboBox comboBox;
 
 	private Thread fillThread = null;
 
 	/**
 	 * Create the panel.
 	 */
 	public DefaultFqanChangePanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
 
		JLabel lblGroup = new JLabel("Group:");
		add(lblGroup, "2, 2, right, default");
 
 		comboBox = new JComboBox(voModel);
 		comboBox.setEditable(false);
 		comboBox.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxx");
 		comboBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 
 				if (si == null) {
 					return;
 				}
 
 				if (ItemEvent.SELECTED == e.getStateChange()) {
 
 					String newVO = (String) voModel.getSelectedItem();
 					GrisuRegistryManager.getDefault(si)
 							.getUserEnvironmentManager().setCurrentFqan(newVO);
 
 				}
 
 			}
 		});
		add(comboBox, "4, 2, fill, default");
 	}
 
 	public void setServiceInterface(ServiceInterface si)
 			throws InterruptedException {
 		this.si = si;
 		String currentFqan = GrisuRegistryManager.getDefault(si)
 				.getUserEnvironmentManager().getCurrentFqan();
 		comboBox.removeAll();
 		comboBox.addItem(currentFqan);
 		comboBox.setSelectedItem(currentFqan);
 		fillComboBox();
 	}
 
 	public void fillComboBox() {
 
 		if (si == null) {
 			return;
 		}
 
 		if (fillThread != null && fillThread.isAlive()) {
 			return;
 		}
 
 		fillThread = new Thread() {
 			@Override
 			public void run() {
 
 				UserEnvironmentManager uem = GrisuRegistryManager
 						.getDefault(si).getUserEnvironmentManager();
 				String old = (String) voModel.getSelectedItem();
 				if (StringUtils.isBlank(old)) {
 					old = uem.getCurrentFqan();
 				}
 
 				String[] allVOs = uem.getAllAvailableFqans();
 
 				voModel.removeAllElements();
 
 				for (String vo : allVOs) {
 					voModel.addElement(vo);
 				}
 
 				if (StringUtils.isNotBlank(old) && voModel.getIndexOf(old) >= 0) {
 					uem.setCurrentFqan(old);
 				} else {
 					old = (String) voModel.getElementAt(0);
 					if (StringUtils.isNotBlank(old)) {
 						uem.setCurrentFqan(old);
 					}
 				}
 
 			}
 		};
 		fillThread.start();
 
 	}
 
 	public void onEvent(FqanEvent arg0) {
 
 		// System.out.println("Fqan changed.");
 
 	}
 
 }
