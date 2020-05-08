 package be.kuleuven.med.brainfuck.core;
 
 import static be.kuleuven.med.brainfuck.LedMatrixApp.SAVE_SETTINGS_ACTION;
 import static be.kuleuven.med.brainfuck.core.LedMatrixAppController.INIT_SERIAL_PORT_ACTION;
 import static be.kuleuven.med.brainfuck.core.LedMatrixAppController.START_EXPERIMENT_ACTION;
 import static be.kuleuven.med.brainfuck.core.LedMatrixAppController.TOGGLE_LED_ACTION;
 import static be.kuleuven.med.brainfuck.core.LedMatrixAppController.UPDATE_LED_MATRIX_ACTION;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.ActionMap;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.jdesktop.application.ResourceMap;
 
 import be.kuleuven.med.brainfuck.entity.LedPosition;
 import be.kuleuven.med.brainfuck.settings.LedSettings;
 
 public class LedMatrixAppView extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private final LedMatrixView ledMatrixView;
 
 	private final JTextField columnTextField;
 
 	private final JTextField rowTextField;
 
 	private final JTextField columnPinTextField;
 
 	private final JTextField rowPinTextField;
 
 	private final JComboBox<String> serialPortNamesBox;
 
 	private JButton updateLedMatrixButton;
 
 	private JSlider intensitySlider;
 
 	private JButton toggleLedButton;
 	
 	private JButton startExperimentButton;
 
 	private JTextField flickerFrequencyTextField;
 
 	private JTextField secondsToRunTextField;
 
 	public LedMatrixAppView(final LedMatrixAppController ledMatrixController) {
 		final ActionMap actionMap = ledMatrixController.getApplicationActionMap();
 		final ResourceMap resourceMap = ledMatrixController.getResourceMap();
 		
 		JPanel rightPanel = new JPanel(new MigLayout("nogrid", "[right]10", "10"));
 		JPanel leftPanel = new JPanel(new MigLayout());
 		// add serial port controls
 		serialPortNamesBox = new JComboBox<String>();
 		rightPanel.add(serialPortNamesBox);
 		JButton initSerialPortNamesButton = new JButton(actionMap.get(INIT_SERIAL_PORT_ACTION));
 		rightPanel.add(initSerialPortNamesButton, "wrap");
 		
 		// add led matrix controls
 		rightPanel.add(new JLabel(resourceMap.getString("widthLabel.text")));
 		rowTextField = createFormattedTextField();
 		rightPanel.add(rowTextField, "w 40, wrap");
 		rightPanel.add(new JLabel(resourceMap.getString("heightLabel.text")));
 		columnTextField = createFormattedTextField();
 		rightPanel.add(columnTextField, "w 40, wrap");
 		updateLedMatrixButton = new JButton(actionMap.get(UPDATE_LED_MATRIX_ACTION));
 		rightPanel.add(updateLedMatrixButton, "wrap");
 		
 		// add led setting fields
 		rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "wrap");
 		rightPanel.add(new JLabel(resourceMap.getString("rowPinLabel.text")));
 		rowPinTextField = createFormattedTextField();
 		rightPanel.add(rowPinTextField, "wrap, w 40, gapy 10");
 		rightPanel.add(new JLabel(resourceMap.getString("columnPinLabel.text")));
 		columnPinTextField = createFormattedTextField();
 		rightPanel.add(columnPinTextField, "wrap, w 40");
 		
 		// add led controls
 		intensitySlider = new JSlider(LedSettings.MIN_INTENSITY, LedSettings.MAX_INTENSITY, LedSettings.MAX_INTENSITY);
 		intensitySlider.setOrientation(JSlider.HORIZONTAL);
 		intensitySlider.addChangeListener(new ChangeListener() {
 			
 			public void stateChanged(ChangeEvent event) {
 				ledMatrixController.adjustIntensity(event);
 			}
 			
 		});
 		rightPanel.add(intensitySlider, "w 150, wrap");
		toggleLedButton = new JButton(TOGGLE_LED_ACTION);
 		rightPanel.add(toggleLedButton, "wrap");
 		
 		// add experiment controls
 		rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "wrap");
		rightPanel.add(new JLabel("flickerFrequencyLabel.text"));
 		flickerFrequencyTextField = createFormattedTextField();
 		rightPanel.add(flickerFrequencyTextField, "wrap, w 40");
		rightPanel.add(new JLabel("secondsToRunLabel.text"));
 		secondsToRunTextField = createFormattedTextField();
 		rightPanel.add(secondsToRunTextField, "wrap, w 40");
		startExperimentButton = new JButton(START_EXPERIMENT_ACTION);
 		rightPanel.add(startExperimentButton, "wrap");
 		// add save settings button
 		rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "wrap");
 		rightPanel.add(new JButton(actionMap.get(SAVE_SETTINGS_ACTION)));
 		
 		ledMatrixView = new LedMatrixView();
 		ledMatrixView.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				// TODO could implement multi select here.. but will need to review the bean's model
 				LedPosition ledPosition = ledMatrixView.selectLed(event, true);
 				ledMatrixView.repaint();
 				ledMatrixController.updateSelectedLed(ledPosition);
 			}
 			
 		});
 		
 		// add led matrix view 
 		leftPanel.add(ledMatrixView, "wmin 300, hmin 400, grow");
 		this.add(leftPanel);
 		this.add(rightPanel);
 	}
 	
 	private JTextField createFormattedTextField() {
 		return new JTextField();
 	}
 
 	public void drawLedMatrix(int width, int height) {
 		ledMatrixView.setMatrixSize(width, height);
 		ledMatrixView.repaint();
 	}
 	
 	public JTextField getColumnTextField() {
 		return columnTextField;
 	}
 
 	public JTextField getRowTextField() {
 		return rowTextField;
 	}
 
 	public JComboBox<String> getSerialPortNamesBox() {
 		return serialPortNamesBox;
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
 
 	public JButton getToggleLedButton() {
 		return toggleLedButton;
 	}
 
 	public JTextField getFlickerFrequencyTextField() {
 		return flickerFrequencyTextField;
 	}
 
 	public JTextField getSecondsToRunTextField() {
 		return secondsToRunTextField;
 	}
 
 	public JButton getStartExperimentButton() {
 		return startExperimentButton;
 	}
 	
 }
