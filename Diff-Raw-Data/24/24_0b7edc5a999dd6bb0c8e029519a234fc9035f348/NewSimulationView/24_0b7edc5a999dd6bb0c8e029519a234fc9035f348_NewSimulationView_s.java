 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.util.Log;
 
 import java.awt.Dimension;
 import javax.swing.JPanel;
 import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Window.Type;
 import javax.swing.JLabel;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JToggleButton;
 import javax.swing.JCheckBox;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.AbstractListModel;
 import javax.swing.JRadioButton;
 
 /**
  * Window for starting a new simulation.
  * 
  * @author Erik
  *
  */
 public class NewSimulationView {
 
 	private JFrame frmSimulatedEcosystem;
 	private JTextField textfield_Iterationdelay;
 	private JTextField tvPredPopSize;
 	private JTextField tvPreyPopSize;
 	private JTextField tvGrassPopSize;
 	private EcoWorld model;
 
 	/**
 	 * Create the application.
 	 * @param model 
 	 */
 	public NewSimulationView(EcoWorld model) {
 		this.model = model;
 		initialize();
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		frmSimulatedEcosystem.setLocation(dim.width/2-frmSimulatedEcosystem.getSize().width/2, dim.height/2-frmSimulatedEcosystem.getSize().height/2);
 		frmSimulatedEcosystem.setVisible(true);
 	}
 	
 	private void startSimulation() {
 		model.setNumIterations(Integer.MAX_VALUE);
 		int tickDelay = Integer.parseInt(textfield_Iterationdelay.getText());
 		if(tickDelay < 1) {
 			model.setRunWithoutTimer(true);			
 		} else {
 			model.setRunWithoutTimer(false);	
 			model.adjustTickRate(tickDelay);			
 		}
 		model.createInitialPopulations(Integer.parseInt(tvPredPopSize.getText()), Integer.parseInt(tvPreyPopSize.getText()), Integer.parseInt(tvGrassPopSize.getText()));
 		try {
 			model.start();
 		} catch (IllegalStateException e) {
 			Log.v(e.toString());
 		}
 	}
 	
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmSimulatedEcosystem = new JFrame();
 		frmSimulatedEcosystem.setAlwaysOnTop(true);
		frmSimulatedEcosystem.setType(Type.POPUP);
 		frmSimulatedEcosystem.setTitle("Start new Simulation");
 		frmSimulatedEcosystem.setBounds(100, 100, 670, 579);
 		frmSimulatedEcosystem.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JButton btnRunSim = new JButton("Start new");
 		btnRunSim.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				startSimulation();
 				frmSimulatedEcosystem.dispose();
 			}
 		});
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				frmSimulatedEcosystem.dispose();
 			}
 		});
 		
 		JLabel lblPrededators = new JLabel("Prededators");
 		lblPrededators.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		
 		final JSlider slider_delaylength = new JSlider();
 		slider_delaylength.setValue(16);
 		slider_delaylength.setSnapToTicks(true);
 		slider_delaylength.setPaintTicks(true);
 		slider_delaylength.setPaintLabels(true);
 		
 		textfield_Iterationdelay = new JTextField();
 		textfield_Iterationdelay.setText("16");
 		textfield_Iterationdelay.setColumns(10);
 		
 		final JLabel lblIterationDelay = new JLabel("Iteration delay");
 		slider_delaylength.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				textfield_Iterationdelay.setText(slider_delaylength.getValue() + "");
 			}
 		});
 		
 		JList predList = new JList();
 		predList.setValueIsAdjusting(true);
 		predList.setSelectedIndices(new int[] {3});
 		predList.setModel(new AbstractListModel() {
 			String[] values = new String[] {"SimplePredatorAgent", "WolfAgent"};
 			public int getSize() {
 				return values.length;
 			}
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		predList.setSelectedIndex(0);
 		
 		JList preyList = new JList();
 		preyList.setValueIsAdjusting(true);
 		preyList.setSelectedIndices(new int[] {3});
 		preyList.setModel(new AbstractListModel() {
 			String[] values = new String[] {"SimplePreyAgent", "DeerAgent", "PigAgent", "RabbitAgent"};
 			public int getSize() {
 				return values.length;
 			}
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		preyList.setSelectedIndex(0);
 		
 		JLabel lblPreys = new JLabel("Preys");
 		lblPreys.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		
 		JList grassList = new JList();
 		grassList.setValueIsAdjusting(true);
 		grassList.setSelectedIndices(new int[] {3});
 		grassList.setModel(new AbstractListModel() {
 			String[] values = new String[] {"GrassAgent"};
 			public int getSize() {
 				return values.length;
 			}
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		grassList.setSelectedIndex(0);
 		
 		JLabel lblVegatablePopulation = new JLabel("Vegatable population");
 		lblVegatablePopulation.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		
 		tvPredPopSize = new JTextField();
 		tvPredPopSize.setText("10");
 		tvPredPopSize.setColumns(10);
 		
 		tvPreyPopSize = new JTextField();
 		tvPreyPopSize.setText("200");
 		tvPreyPopSize.setColumns(10);
 		
 		tvGrassPopSize = new JTextField();
 		tvGrassPopSize.setText("1000");
 		tvGrassPopSize.setColumns(10);
 		
 		JLabel lblInitialSize = new JLabel("Initial size");
 		
 		JLabel label = new JLabel("Initial size");
 		
 		JLabel label_1 = new JLabel("Initial size");
 		
 		JRadioButton rdbtnNewRadioButton = new JRadioButton("Use OpenGLSimulationView");
 		rdbtnNewRadioButton.setSelected(true);
 		GroupLayout groupLayout = new GroupLayout(frmSimulatedEcosystem.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(10)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(predList, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 										.addGroup(groupLayout.createSequentialGroup()
 											.addComponent(lblInitialSize)
 											.addPreferredGap(ComponentPlacement.RELATED, 200, Short.MAX_VALUE)
 											.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 												.addComponent(lblIterationDelay)
 												.addGroup(groupLayout.createSequentialGroup()
 													.addComponent(slider_delaylength, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 													.addPreferredGap(ComponentPlacement.RELATED)
 													.addComponent(textfield_Iterationdelay, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 												.addComponent(rdbtnNewRadioButton)))
 										.addComponent(tvPredPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 								.addGroup(groupLayout.createSequentialGroup()
 									.addGap(324)
 									.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
 									.addGap(18)
 									.addComponent(btnRunSim, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(lblPrededators))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addContainerGap()
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblPreys, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(preyList, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.UNRELATED)
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 										.addComponent(label, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
 										.addComponent(tvPreyPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
 							.addGap(252))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(grassList, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
 								.addComponent(tvGrassPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(lblVegatablePopulation, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)))
 					.addContainerGap())
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(lblPrededators)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(22)
 							.addComponent(lblIterationDelay)
 							.addGap(6)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(slider_delaylength, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(textfield_Iterationdelay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addGap(50)
 							.addComponent(rdbtnNewRadioButton)
 							.addPreferredGap(ComponentPlacement.RELATED, 318, Short.MAX_VALUE)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
 								.addComponent(btnRunSim, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(lblInitialSize)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(tvPredPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 								.addComponent(predList, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE))
 							.addGap(37)
 							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(lblPreys, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
 									.addGap(6)
 									.addComponent(preyList, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE))
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(label)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(tvPreyPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 							.addGap(29)
 							.addComponent(lblVegatablePopulation, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 								.addComponent(grassList, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(label_1)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(tvGrassPopSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
 					.addContainerGap())
 		);
 		frmSimulatedEcosystem.getContentPane().setLayout(groupLayout);
 	}
 }
