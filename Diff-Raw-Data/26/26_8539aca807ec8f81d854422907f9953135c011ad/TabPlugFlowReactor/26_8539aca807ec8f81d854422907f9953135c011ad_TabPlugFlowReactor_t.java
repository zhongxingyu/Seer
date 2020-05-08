 //////////////////////////////////////////////////////////////////////////////////
 // Class: 	TabPlugFlowReactor
 //
 // Purpose: This class implements all the visual elements in the Plug Flow Reactor
 //			tab of the final application.  It is comprised of a 
 //			FluidFlowReactorPanel, a JTextArea for data, text fields for entering
 //			parameters, and buttons for controlling the reactor
 //
 //////////////////////////////////////////////////////////////////////////////////
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.LineBorder;
 
 public class TabPlugFlowReactor extends JPanel 
 {
 	private static final long serialVersionUID = 1L;
 	private JTextField txtInitialConcentration;
 	private JTextField txtRateConstant;	
 	private JTextArea txtConcentrationLog;
 	private JTextField txtParticleNumber;
 	private JTextField txtTimeRate;
 	private JTextField txtParticleMoveRate;
 	private PFR  pfrPanel;
 	private Preferences preferences;
 	
 	//Constructors all use the initialize() method
 	public TabPlugFlowReactor() {
 		initialize();
 	}
 	
 	public TabPlugFlowReactor(Preferences pref) {
 		preferences = pref;
 		initialize();
 	}
 
 	//Constructs all objects in the Plug Flow Reactor Tab of the application
 	private void initialize() 
 	{
 		setBounds(0, 0, 800, 600);
 		setLayout(null);
 		
 		//Create the reactor panel/set appearance based on preferences
 		pfrPanel = new PFR(preferences);
 		pfrPanel.setBounds(71, 339, 626, 209);
 		pfrPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
 		add(pfrPanel);
 		pfrPanel.setLayout(null);
 		
 		//Create labels for text fields
 		JLabel lblInitialConcentration = new JLabel("Initial Concentration");
 		lblInitialConcentration.setBounds(12, 67, 192, 15);
 		add(lblInitialConcentration);
 		
 		JLabel lblRateConstant = new JLabel("Rate Constant per Minute");
 		lblRateConstant.setBounds(12, 94, 192, 15);
 		add(lblRateConstant);
 		
 		JLabel lblNumberOfParticles = new JLabel("Number of particles");
 		lblNumberOfParticles.setBounds(12, 121, 192, 15);
 		add(lblNumberOfParticles);
 		
 		JLabel lblRateOfReaction = new JLabel("Rate of reaction steps");
 		lblRateOfReaction.setBounds(12, 154, 220, 15);
 		add(lblRateOfReaction);
 		
 		JLabel lblRateOfRepaint = new JLabel("Rate of repaint of particles");
 		lblRateOfRepaint.setBounds(12, 185, 192, 15);
 		add(lblRateOfRepaint);
 		
 		JLabel lblBatchReactorData = new JLabel("Batch Reactor Data");
 		lblBatchReactorData.setBounds(629, 37, 135, 14);
 		add(lblBatchReactorData);
 		
 		//Create text fields
 		txtInitialConcentration = new JTextField();
 		lblInitialConcentration.setLabelFor(txtInitialConcentration);
 		txtInitialConcentration.setText(preferences.getPlugFlowInitialC().toString());
 		txtInitialConcentration.setBounds(239, 63, 114, 19);
 		add(txtInitialConcentration);
 		
 		txtParticleNumber = new JTextField();
 		txtParticleNumber.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				pfrPanel.setInitialConcentration(Integer.parseInt(txtParticleNumber.getText()));
 			}
 		});
 		txtParticleNumber.setText(preferences.getPlugFlowParticleNumber().toString());
 		txtParticleNumber.setBounds(239, 121, 114, 19);
 		add(txtParticleNumber);
 		txtParticleNumber.setColumns(10);
 		
 		txtTimeRate = new JTextField();
 		txtTimeRate.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				pfrPanel.setReactionTimeRate(Integer.parseInt(txtTimeRate.getText()));
 			}
 		});
 		txtTimeRate.setText(preferences.getPlugFlowTimeStep().toString());
 		txtTimeRate.setBounds(239, 152, 114, 19);
 		add(txtTimeRate);
 		txtTimeRate.setColumns(10);
 		
 		txtParticleMoveRate = new JTextField();
 		txtParticleMoveRate.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				pfrPanel.setParticleMoveRate(Integer.parseInt(txtParticleMoveRate.getText()));
 			}
 		});
 		txtParticleMoveRate.setText(preferences.getPlugFlowMotionRate().toString());
 		txtParticleMoveRate.setBounds(239, 183, 114, 19);
 		add(txtParticleMoveRate);
 		txtParticleMoveRate.setColumns(10);
 		
 		//NEED TO FIX THIS, SHOULD NOT DO THIS (when you click around fields w/o changing anything, the whole thing resets)
 		txtInitialConcentration.addFocusListener(new FocusAdapter() 
 		{
 			@Override
 			public void focusLost(FocusEvent e) 
 			{
 				resetReactor();
 			}
 		});
 
 		txtRateConstant = new JTextField();
 		txtRateConstant.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				resetReactor();
 			}
 		});
 		txtRateConstant.setText(preferences.getPlugFlowInitialK().toString());
 		txtRateConstant.setBounds(239, 90, 114, 19);
 		add(txtRateConstant);
 		txtRateConstant.setColumns(10);
 		
 		//Create reactor control buttons
 		JButton btnGo = new JButton("Go");
 		btnGo.addActionListener(new ButtonGoListener());
 		btnGo.setBounds(247, 218, 81, 25);
 		add(btnGo);
 
 		JButton btnStop = new JButton("Stop");
 		btnStop.addActionListener(new ButtonStopListener());
 		btnStop.setBounds(338, 218, 71, 25);
 		add(btnStop);
 
 		JButton btnReset = new JButton("Reset");
 		btnReset.addActionListener(new ButtonResetListener());
 		btnReset.setBounds(419, 218, 81, 25);
 		add(btnReset);
 		
 		//Create data log
 		txtConcentrationLog = new JTextArea();
 		txtConcentrationLog.setBackground(new Color(127, 255, 212));
 		txtConcentrationLog.setLineWrap(true);
 		txtConcentrationLog.setBounds(569, 62, 246, 266);
 		add(txtConcentrationLog);
 		
 		//Create Toggle Flow button
 		JButton btnToggleFlow = new JButton("Toggle Flow");
 		btnToggleFlow.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				pfrPanel.toggleFlowing();
 			}
 		});
 		btnToggleFlow.setBounds(148, 219, 89, 23);
 		add(btnToggleFlow);
		pfrPanel.setParticleMoveRate(Integer.parseInt(txtParticleMoveRate.getText()));
		pfrPanel.setReactionTimeRate(Integer.parseInt(txtTimeRate.getText()));
		pfrPanel.setInitialConcentration(Integer.parseInt(txtParticleNumber.getText()));
 
 		//Reset the Reactor
 		resetReactor();		
 	}
 
 	//Resets the reactor by reseting the initial concentration
 	//and reaction constant figures and dynamic label figures, 
 	//then remaking the dots and repainting
 	private void resetReactor() 
 	{
 		txtConcentrationLog.setText("");
 		pfrPanel.setAnimationTimers(Integer.parseInt(txtParticleMoveRate.getText()));				
 	}
 
 	//Listener for the Go button
 	class ButtonGoListener implements ActionListener 
 	{
 		public void actionPerformed(ActionEvent evt) 
 		{
 			if(pfrPanel.isFlowing() == true && pfrPanel.isStarted() == false)
 			{
 			pfrPanel.setAnimationTimers(Integer.parseInt(txtParticleMoveRate.getText()));
 			pfrPanel.startAnimation();
 			pfrPanel.beginAnimation();
 			}
 		}
 	}
 
 	//Listener for the Stop button
 	class ButtonStopListener implements ActionListener 
 	{
 
 		public void actionPerformed(ActionEvent evt)
 		{
 			pfrPanel.stopReactor();
 			pfrPanel.stopAnimation();
 		}
 
 	}
 
 	//Listener for the Reset button
 	class ButtonResetListener implements ActionListener 
 	{
 
 		public void actionPerformed(ActionEvent evt) 
 		{
 			resetReactor();
 			pfrPanel.resetBatchReactorLocation();
 		}
 
 	}
 }
