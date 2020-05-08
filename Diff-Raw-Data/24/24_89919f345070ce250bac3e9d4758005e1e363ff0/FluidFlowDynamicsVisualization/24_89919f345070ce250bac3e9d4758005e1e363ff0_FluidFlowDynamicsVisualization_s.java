 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JTextArea;
 import javax.swing.Timer;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class FluidFlowDynamicsVisualization {
 
 	private JFrame MainFrame;
 	private JTextField txtInitialConcentration;
 	private JTextField txtRateConstant;
 	private Timer timer = new Timer( 500, new TimerListener());
 	private FFBatchReactor reactor = new FFBatchReactor();
 	private JTextArea txtConcentrationLog;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					FluidFlowDynamicsVisualization window = new FluidFlowDynamicsVisualization();
 					window.MainFrame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public FluidFlowDynamicsVisualization() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		timer.setRepeats(true);
 		
 		MainFrame = new JFrame();
 		MainFrame.setTitle("Fluid Flow Dynamics Visualization");
 		MainFrame.setBounds(100, 100, 868, 560);
 		MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		MainFrame.getContentPane().setLayout(null);
 
 		JLabel lblInitialConcentration = new JLabel("Initial Concentration");
 		lblInitialConcentration.setBounds(12, 67, 192, 15);
 		MainFrame.getContentPane().add(lblInitialConcentration);
 
 		txtInitialConcentration = new JTextField();
 		lblInitialConcentration.setLabelFor(txtInitialConcentration);
 		txtInitialConcentration.setText("1000");
 		txtInitialConcentration.setBounds(208, 63, 114, 19);
 		MainFrame.getContentPane().add(txtInitialConcentration);
		txtInitialConcentration.setColumns(10);

 		JLabel lblNewLabel = new JLabel("Rate Constant per Minute");
 		lblNewLabel.setBounds(12, 94, 192, 15);
 		MainFrame.getContentPane().add(lblNewLabel);
 
 		txtRateConstant = new JTextField();
		txtRateConstant.setText("2");
 		txtRateConstant.setBounds(208, 90, 114, 19);
 		MainFrame.getContentPane().add(txtRateConstant);
 		txtRateConstant.setColumns(10);
 
 		JButton btnGo = new JButton("Go");
 		btnGo.addActionListener(new ButtonGoListener());
 		btnGo.setBounds(208, 122, 117, 25);
 		MainFrame.getContentPane().add(btnGo);
 
 		JButton btnStop = new JButton("Stop");
 		btnStop.addActionListener(new ButtonStopListener());
 		btnStop.setBounds(208, 159, 117, 25);
 		MainFrame.getContentPane().add(btnStop);
 
 		JButton btnReset = new JButton("Reset");
 		btnReset.addActionListener(new ButtonResetListener());
 		btnReset.setBounds(208, 196, 117, 25);
 		MainFrame.getContentPane().add(btnReset);
 
 		txtConcentrationLog = new JTextArea();
 		txtConcentrationLog.setLineWrap(true);
 		txtConcentrationLog.setBounds(346, 67, 506, 432);
 		MainFrame.getContentPane().add(txtConcentrationLog);
 	}
 
 	private void resetReactor() {
 		reactor.setInitialConcentration(Double
 				.parseDouble(txtInitialConcentration.getText()));
 		reactor.setRateConstant(Double.parseDouble(txtRateConstant.getText()));
 		reactor.setCurrentTime(0);
 		txtConcentrationLog.setText("");
 
 	}
 
 	class ButtonGoListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent evt) {
 			// TODO Action Listener actions
 
 			timer.start();
 		}
 
 	}
 
 	class ButtonStopListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent evt) {
 			// TODO Action Listener actions
 			timer.stop();
 		}
 
 	}
 
 	class ButtonResetListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent evt) {
 			resetReactor();
 		}
 
 	}
 
 	class TimerListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent evt) {
 			reactor.setCurrentTime(reactor.getCurrentTime() + 1);
 			txtConcentrationLog.setText("Concentration at time "
 					+ reactor.getCurrentTime() + " is "
 					+ reactor.getCurrentConcentration() + "\n" + txtConcentrationLog.getText());
 	
 		}
 	}
 }
