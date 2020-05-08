 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 
 public interface HP33120aInterface {
 	
 	void enableModulationButtons(); // Method to enable buttons if normal signal is selected	
 	void disableModulationbuttons(); // Method to disable buttons if normal signal is selected
 	void disableExecutionButton();	// Method to disable the Execution Button if there is any problem with data validation
 	void enableExecutionButton();	// Method to enable the Execution Button
 	void setHP33120aControl(HP33120aControl wfmc); //Enable the ActionListeners
 	
 	public JComboBox getTypeOfSignal();	
 	public JComponent getHP33120aPanel();	
 	public JComboBox getSignalShape();
 	public JComboBox getModWfmShape();
 	public JComboBox getModType();
 	public JButton getConnectButton();
 	public String getBurstPhase();
 	public String getBurstCount();
 	public String getBurstRate();
 	public String getPhaseDeviationPM();
 	public String getIntDeviationPWM();
 	public String getHopFrequency();
 	public String getFmDeviation();
 	public String getAmDepth();
 	public String getModulatingFreq();
 	public String getDutyCyclePulse();
 	public String getDutyCycleSquare();
 	public String getRampSymmetry();
 	public String getAmplitude();
 	public String getFrequency();
 	public String getOffset();
 	
 	public void setDataValidationMessage(String validationMessage);
 	public void disableDataValidationLabel();
 
 	static final String TYPE_OF_SIGNAL = "HP33120a_Type_of_Signal";
 	static final String MODULATION = "Modulation";
 	static final String SIGNAL = "Simple Signal";
 	static final String CONFIG = "HP33120a_Config";
 	static final String FREQUENCY = "Frequency";
 	static final String AMPLITUDE ="HP33120a_Amplitude";
 	static final String OFFSET = "HP33120a_Offset";
 	static final String RAMP_SYMMETRY = "HP33120a_Ramp_Symmetry";
 	static final String DUTY_CYCLE_SQUARE = "HP33120a_Duty_Cycle_Square";
 	static final String DUTY_CYCLE_PULSE = "HP33120a_Duty_Cycle_Pulse";
 	static final String MODULATING_FREQUENCY = "HP33120a_Modulating_Frequency";
 	static final String AM_DEPTH = "HP33120a_AM_Depth";
 	static final String FM_DEVIATION = "HP33120a_FM_Deviation";
 	static final String HOP_FREQUENCY = "HP33120a_Hop_Frequency";
 	static final String DEVIATION_PWM = "HP33120a_Deviation_PWM";
 	static final String PHASE_DEVIATION_PM = "HP33120a_Phase_Deviation";
 	static final String BURST_RATE = "HP33120a_Burst_Rate";
 	static final String BURST_COUNT = "HP33120a_Burst_Count";
 	static final String BURST_PHASE = "HP33120a_Burst_Phase";
 }
