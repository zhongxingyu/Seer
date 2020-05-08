 package be.kuleuven.med.brainfuck.view;
 
 import static be.kuleuven.med.brainfuck.LedMatrixApp.SAVE_SETTINGS_ACTION;
 import static be.kuleuven.med.brainfuck.controller.LedMatrixController.INIT_LED_MATRIX_CONNECTOR_ACTION;
 import static be.kuleuven.med.brainfuck.controller.LedMatrixController.INIT_THORLABS_CONNECTOR_ACTION;
 import static be.kuleuven.med.brainfuck.controller.LedMatrixController.START_EXPERIMENT_ACTION;
 import static be.kuleuven.med.brainfuck.controller.LedMatrixController.TOGGLE_LED_ACTION;
 import static be.kuleuven.med.brainfuck.controller.LedMatrixController.UPDATE_LED_MATRIX_ACTION;
 import static be.kuleuven.med.brainfuck.controller.LedMatrixController.UPDATE_SERIAL_PORTS_ACTION;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.ActionMap;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.SwingConstants;
 import javax.swing.Timer;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.TaskMonitor;
 
 import be.kuleuven.med.brainfuck.controller.LedMatrixController;
 import be.kuleuven.med.brainfuck.domain.settings.LedSettings;
 
 public class LedMatrixPanelView extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private final JTextField columnTextField;
 
 	private final JTextField rowTextField;
 
 	private final JTextField columnPinTextField;
 
 	private final JTextField rowPinTextField;
 
 	private final JComboBox<String> ledMatrixConnectorBox;
 
 	private final JComboBox<String> thorlabsConnectorBox;
 
 	private JButton updateLedMatrixButton;
 
 	private JSlider intensitySlider;
 
 	private JToggleButton toggleLedButton;
 
 	private JToggleButton startExperimentButton;
 
 	private JTextField flickerFrequencyTextField;
 
 	private JTextField secondsToRunTextField;
 
 	private JTextField cyclesToRunTextField;
 
 	private final Icon idleIcon;
 
 	private int busyIconIndex = 0;
 
 	public LedMatrixPanelView(final LedMatrixController ledMatrixController) {
 		super(new MigLayout("nogrid, insets 10", "align right"));
 		final ActionMap actionMap = ledMatrixController.getApplicationActionMap();
 		final ResourceMap resourceMap = ledMatrixController.getResourceMap();
 		
 		// add refresh button
 		add(new JButton(actionMap.get(UPDATE_SERIAL_PORTS_ACTION)));
 		// add save button
 		add(new JButton(actionMap.get(SAVE_SETTINGS_ACTION)), "wrap");
 		add(new JLabel(resourceMap.getString("ledMatrixControlLabel.text")));
 		add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap");
 
 		// add led matrix connector controls
 		ledMatrixConnectorBox = new JComboBox<String>();
 		add(ledMatrixConnectorBox,"w :200:200");
 		JButton initLedMatrixConnectorButton = new JButton(actionMap.get(INIT_LED_MATRIX_CONNECTOR_ACTION));
 		add(initLedMatrixConnectorButton, "wrap");
 
 		// add led matrix controls
 		add(new JLabel(resourceMap.getString("widthLabel.text")));
 		rowTextField = createFormattedTextField();
 		add(rowTextField, "w 40, wrap");
 		add(new JLabel(resourceMap.getString("heightLabel.text")));
 		columnTextField = createFormattedTextField();
 		add(columnTextField, "w 40, wrap");
 		updateLedMatrixButton = new JButton(actionMap.get(UPDATE_LED_MATRIX_ACTION));
 		add(updateLedMatrixButton, "wrap");
 
 		// add led setting fields
 		add(new JLabel(resourceMap.getString("rowPinLabel.text")));
 		rowPinTextField = createFormattedTextField();
 		add(rowPinTextField, "wrap, w 40, gapy 10");
 		add(new JLabel(resourceMap.getString("columnPinLabel.text")));
 		columnPinTextField = createFormattedTextField();
 		add(columnPinTextField, "wrap, w 40");
 
 		add(new JLabel(resourceMap.getString("flickerFrequencyLabel.text")));
 		flickerFrequencyTextField = createFormattedTextField();
 		add(flickerFrequencyTextField, "w 40");
 		
 		add(new JLabel(resourceMap.getString("secondsToRunLabel.text")));
 		secondsToRunTextField = createFormattedTextField();
 		add(secondsToRunTextField, "w 40, wrap");
 		
 		// add led controls
		// TODO replace slider with regular (bounded) text field
 		intensitySlider = new JSlider(LedSettings.MIN_INTENSITY, LedSettings.MAX_INTENSITY, LedSettings.MAX_INTENSITY);
 		intensitySlider.setOrientation(JSlider.HORIZONTAL);
 		intensitySlider.setPaintTicks(true);
 		intensitySlider.setMajorTickSpacing(32);
 		intensitySlider.addChangeListener(new ChangeListener() {
 
 			public void stateChanged(ChangeEvent event) {
 				ledMatrixController.adjustIntensity(event);
 			}
 
 		});
 		add(intensitySlider, "growx");
 		toggleLedButton = new JToggleButton(actionMap.get(TOGGLE_LED_ACTION));
 		add(toggleLedButton, "wrap");
 
 		// add thorlabs connector controls
 		add(new JLabel(resourceMap.getString("thorlabsControlLabel.text")));
 		add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap");
 		thorlabsConnectorBox = new JComboBox<String>();
 		add(thorlabsConnectorBox,"w :200:200");
 		JButton initThorlabsConnectorButton = new JButton(actionMap.get(INIT_THORLABS_CONNECTOR_ACTION));
 		add(initThorlabsConnectorButton, "wrap");
 
 		// add experiment controls		
 		add(new JLabel(resourceMap.getString("experimentControlLabel.text")));
 		add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap");
 		add(new JLabel(resourceMap.getString("cyclesToRunLabel.text")));
 		cyclesToRunTextField = createFormattedTextField();
 		add(cyclesToRunTextField, "wrap, w 40");
 		startExperimentButton = new JToggleButton(actionMap.get(START_EXPERIMENT_ACTION));
 		add(startExperimentButton, "wrap");
 		// add save settings button
 		add(new JLabel(resourceMap.getString("applicationStatusLabel.text")));
 		add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap");
 
 		final JLabel statusMessageLabel = new JLabel();
 		// status bar initialization - message timeout, idle icon and busy
 		// animation, etc
 		add(statusMessageLabel, "grow");
 		final JLabel statusAnimationLabel = new JLabel();
 
 		add(statusAnimationLabel, "gapleft, push");
 
 		final int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
 		final Timer messageTimer = new Timer(messageTimeout, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				statusMessageLabel.setText(resourceMap.getString("StatusBar.idleMessage"));
 			}
 		});
 		messageTimer.setRepeats(false);
 		final int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
 		final Icon[] busyIcons = new Icon[15];
 		for (int i = 0; i < busyIcons.length; i++) {
 			busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
 		}
 
 		final Timer busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
 				statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
 			}
 		});
 		idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
 		statusAnimationLabel.setIcon(idleIcon);
 		// connecting action tasks to status bar via TaskMonitor
 		TaskMonitor taskMonitor = new TaskMonitor(ledMatrixController.getContext());
 		taskMonitor.addPropertyChangeListener(new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				final String propertyName = evt.getPropertyName();
 				if ("started".equals(propertyName)) {
 					if (!busyIconTimer.isRunning()) {
 						statusAnimationLabel.setIcon(busyIcons[0]);
 						busyIconIndex = 0;
 						busyIconTimer.start();
 					}
 				} else if ("done".equals(propertyName)) {
 					busyIconTimer.stop();
 					statusAnimationLabel.setIcon(idleIcon);
 				} else if ("message".equals(propertyName)) {
 					final String text = (String) evt.getNewValue();
 					statusMessageLabel.setText(text);
 					messageTimer.restart();
 				}
 			}
 		});
 	}
 
 	private JTextField createFormattedTextField() {
 		return new JTextField();
 	}
 
 	public JTextField getColumnTextField() {
 		return columnTextField;
 	}
 
 	public JTextField getRowTextField() {
 		return rowTextField;
 	}
 
 	public JComboBox<String> getLedMatrixConnectorBox() {
 		return ledMatrixConnectorBox;
 	}
 
 	public JComboBox<String> getThorlabsConnectorBox() {
 		return thorlabsConnectorBox;
 	}
 
 	public JTextField getColumnPinTextField() {
 		return columnPinTextField;
 	}
 
 	public JTextField getRowPinTextField() {
 		return rowPinTextField;
 	}
 
 	public JSlider getIntensitySlider() {
 		return intensitySlider;
 	}
 	
 	public JTextField getSecondsToRunTextField() {
 		return secondsToRunTextField;
 	}
 
 	public JToggleButton getToggleLedButton() {
 		return toggleLedButton;
 	}
 
 	public JTextField getFlickerFrequencyTextField() {
 		return flickerFrequencyTextField;
 	}
 
 	public JToggleButton getStartExperimentButton() {
 		return startExperimentButton;
 	}
 
 	public JTextField getCyclesToRunTextField() {
 		return cyclesToRunTextField;
 	}
 	
 }
