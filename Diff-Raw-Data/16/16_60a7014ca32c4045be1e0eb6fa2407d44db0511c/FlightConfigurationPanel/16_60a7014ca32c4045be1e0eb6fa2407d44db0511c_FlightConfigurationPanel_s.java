 package net.sf.openrocket.gui.main.flightconfigpanel;
 
import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.EventObject;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 
 import net.miginfocom.swing.MigLayout;
 import net.sf.openrocket.document.OpenRocketDocument;
 import net.sf.openrocket.document.Simulation;
 import net.sf.openrocket.gui.dialogs.flightconfiguration.RenameConfigDialog;
 import net.sf.openrocket.gui.main.BasicFrame;
 import net.sf.openrocket.l10n.Translator;
 import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
 import net.sf.openrocket.rocketcomponent.Rocket;
 import net.sf.openrocket.rocketcomponent.RocketComponent;
 import net.sf.openrocket.startup.Application;
 import net.sf.openrocket.util.StateChangeListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FlightConfigurationPanel extends JPanel implements StateChangeListener {
 	
 	private static final Logger log = LoggerFactory.getLogger(FlightConfigurationPanel.class);
 	private static final Translator trans = Application.getTranslator();
 	
 	private final OpenRocketDocument document;
 	private final Rocket rocket;
 	
 	private final JButton newConfButton, renameConfButton, removeConfButton, copyConfButton;
 	
 	private final JTabbedPane tabs;
 	private final MotorConfigurationPanel motorConfigurationPanel;
 	private final RecoveryConfigurationPanel recoveryConfigurationPanel;
 	private final SeparationConfigurationPanel separationConfigurationPanel;
 	
 	@Override
 	public void stateChanged(EventObject e) {
 		updateButtonState();
 	}
 
 	public FlightConfigurationPanel(OpenRocketDocument doc) {
 		super(new MigLayout("fill"));
 		
 		this.document = doc;
 		this.rocket = doc.getRocket();
 		
		JPanel panel = new JPanel(new MigLayout("fill"));
 		
 		//// Tabs for advanced view.
 		tabs = new JTabbedPane();
		this.add(tabs, "grow, spanx, wrap");
 		
 		//// Motor tabs
 		motorConfigurationPanel = new MotorConfigurationPanel(this, rocket);
 		tabs.add(trans.get("edtmotorconfdlg.lbl.Motortab"), motorConfigurationPanel);
 		//// Recovery tab
 		recoveryConfigurationPanel = new RecoveryConfigurationPanel(this, rocket);
 		tabs.add(trans.get("edtmotorconfdlg.lbl.Recoverytab"), recoveryConfigurationPanel);
 		
 		//// Stage tab
 		separationConfigurationPanel = new SeparationConfigurationPanel(this, rocket);
 		tabs.add(trans.get("edtmotorconfdlg.lbl.Stagetab"), separationConfigurationPanel);
 
 		newConfButton = new JButton(trans.get("edtmotorconfdlg.but.Newconfiguration"));
 		newConfButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addConfiguration();
 				configurationChanged();
 			}
 			
 		});
 		
		panel.add(newConfButton);
 		
 		renameConfButton = new JButton(trans.get("edtmotorconfdlg.but.Renameconfiguration"));
 		renameConfButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				renameConfiguration();
 				configurationChanged();
 			}
 		});
		panel.add(renameConfButton);
 		
 		removeConfButton = new JButton(trans.get("edtmotorconfdlg.but.Removeconfiguration"));
 		removeConfButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				removeConfiguration();
 				configurationChanged();
 			}
 		});
		panel.add(removeConfButton);
 		
 		copyConfButton = new JButton(trans.get("edtmotorconfdlg.but.Copyconfiguration"));
 		copyConfButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				copyConfiguration();
 				configurationChanged();
 			}
 		});
 		panel.add(copyConfButton, "wrap para");
 		
 		this.add(panel, "growx");
 		updateButtonState();
 		
 		this.rocket.getDefaultConfiguration().addChangeListener(this);
 	}
 	
 	private void addConfiguration() {
 		String newId = rocket.newFlightConfigurationID();
 		rocket.getDefaultConfiguration().setFlightConfigurationID(newId);
 		
 		// Create a new simulation for this configuration.
 		createSimulationForNewConfiguration();
 		
 		configurationChanged();
 	}
 	
 	private void copyConfiguration() {
 		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
 		
 		// currentID is the currently selected configuration.
 		String newConfigId = rocket.newFlightConfigurationID();
 		String oldName = rocket.getFlightConfigurationName(currentId);
 		
 		for (RocketComponent c : rocket) {
 			if (c instanceof FlightConfigurableComponent) {
 				((FlightConfigurableComponent) c).cloneFlightConfiguration(currentId, newConfigId);
 			}
 		}
 		rocket.setFlightConfigurationName(currentId, oldName);
 		rocket.getDefaultConfiguration().setFlightConfigurationID(newConfigId);
 		
 		// Create a new simulation for this configuration.
 		createSimulationForNewConfiguration();
 		
 		configurationChanged();
 	}
 	
 	private void renameConfiguration() {
 		new RenameConfigDialog(SwingUtilities.getWindowAncestor(this), rocket).setVisible(true);
 	}
 	
 	private void removeConfiguration() {
 		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
 		if (currentId == null)
 			return;
 		rocket.removeFlightConfigurationID(currentId);
 		rocket.getDefaultConfiguration().setFlightConfigurationID(null);
 		configurationChanged();
 	}
 	
 	/**
 	 * prereq - assumes that the new configuration has been set as the default configuration.
 	 */
 	private void createSimulationForNewConfiguration() {
 		Simulation newSim = new Simulation(rocket);
 		OpenRocketDocument doc = BasicFrame.findDocument(rocket);
 		newSim.setName(doc.getNextSimulationName());
 		doc.addSimulation(newSim);
 	}
 	
 	private void configurationChanged() {
 		motorConfigurationPanel.fireTableDataChanged();
 		recoveryConfigurationPanel.fireTableDataChanged();
 		separationConfigurationPanel.fireTableDataChanged();
 		updateButtonState();
 	}
 	
 	private void updateButtonState() {
 		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
 		removeConfButton.setEnabled(currentId != null);
 		renameConfButton.setEnabled(currentId != null);
 		copyConfButton.setEnabled(currentId != null);
 
 	}
 	
 }
