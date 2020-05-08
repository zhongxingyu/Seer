 package TKC.util;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.SpinnerListModel;
 import javax.swing.UIManager;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JSpinner;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 import javax.swing.border.EtchedBorder;
 import javax.swing.BoxLayout;
 import java.awt.Label;
 import java.awt.Color;
 import java.util.Iterator;
 
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.MatteBorder;
 
 import TKM.Block;
 import TKM.Switch;
 
 public class ControllerUI extends JPanel {
 
 	private JPanel contentPane;
 	public JSpinner spinnerTrack;
 	public JSpinner spinnerController;
 	public JPanel selectedItemPanel;
 	public JPanel trainPanel;
 	public JPanel blockPanel;
 	public JPanel panel;
 	public JPanel trainPanelHeader;
 	public JScrollPane scrollPaneTrain;
 	public JPanel trainPanelContent;
 	public JScrollPane scrollPaneBlock;
 	public JPanel blockPanelContent;
 	public Wayside currentController;
 	public TrackController tkc;
 	public JLabel waysideLabel;
 	public JLabel trainTotalLabel;
 	public JLabel blockTotalLabel;
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					/*ControllerUI frame = new ControllerUI(null);
 					frame.setVisible(true);*/
 					ControllerUI frame = new ControllerUI(null);
 					
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public ControllerUI(TrackController paramtkc) {
 		
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		
 		//Create and set up the window.
         JFrame frame = new JFrame("Track Controller - UI");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setBounds(100, 100, 778, 481);
         
 		this.tkc = paramtkc;
 		
 		
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		
 		frame.setContentPane(contentPane);
 		
 		String [] trackNames = {"Red Line", "Green Line"};
 		SpinnerListModel trackSModel = new SpinnerListModel(trackNames);
 		
 		spinnerTrack = new JSpinner(trackSModel);
 		
 		
 		Integer [] controllerIDs = {0,1,2,3,4,5,6};
 		SpinnerListModel controllerSModel = new SpinnerListModel(controllerIDs);
 		
 		spinnerController = new JSpinner(controllerSModel);
 
 		selectedItemPanel = new JPanel();
 
 		trainPanel = new JPanel();
 		trainPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 
 		blockPanel = new JPanel();
 		blockPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 
 		panel = new JPanel();
 		panel.setBackground(Color.LIGHT_GRAY);
 		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 
 		JButton btnRefresh = new JButton("Refresh");
 		btnRefresh.addActionListener(new ControllerListener());
 		
 		JLabel lblSelectTrack = new JLabel("Select Track");
 		
 		JLabel lblSelectWayside = new JLabel("Select Wayside");
 		
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
 						.addComponent(blockPanel, GroupLayout.PREFERRED_SIZE, 702, GroupLayout.PREFERRED_SIZE)
 						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 699, GroupLayout.PREFERRED_SIZE)
 						.addComponent(trainPanel, GroupLayout.PREFERRED_SIZE, 702, GroupLayout.PREFERRED_SIZE)
 						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
 							.addGroup(gl_contentPane.createSequentialGroup()
 								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
 									.addComponent(lblSelectTrack)
 									.addComponent(spinnerTrack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 								.addGap(61)
 								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
 									.addComponent(spinnerController, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(lblSelectWayside))
 								.addPreferredGap(ComponentPlacement.RELATED, 250, Short.MAX_VALUE)
 								.addComponent(btnRefresh))
 							.addComponent(selectedItemPanel, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 492, GroupLayout.PREFERRED_SIZE)))
 					.addContainerGap(40, Short.MAX_VALUE))
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addContainerGap(19, Short.MAX_VALUE)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblSelectTrack)
 								.addComponent(lblSelectWayside))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinnerTrack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinnerController, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED))
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addComponent(btnRefresh)
 							.addGap(16)))
 					.addComponent(selectedItemPanel, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(trainPanel, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
 					.addGap(18)
 					.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(blockPanel, GroupLayout.PREFERRED_SIZE, 165, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap())
 		);
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 
 		Label blkIDLabel = new Label("Block ID");
 		blkIDLabel.setAlignment(Label.CENTER);
 		panel.add(blkIDLabel);
 
 		Label blkStateLabel = new Label("State");
 		blkStateLabel.setAlignment(Label.CENTER);
 		panel.add(blkStateLabel);
 
 		Label switchLabel = new Label("Switch Destination");
 		switchLabel.setAlignment(Label.CENTER);
 		panel.add(switchLabel);
 		trainPanel.setLayout(new BoxLayout(trainPanel, BoxLayout.Y_AXIS));
 
 		trainPanelHeader = new JPanel();
 		trainPanelHeader.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		trainPanelHeader.setBackground(Color.LIGHT_GRAY);
 		trainPanel.add(trainPanelHeader);
 		trainPanelHeader.setLayout(new BoxLayout(trainPanelHeader, BoxLayout.X_AXIS));
 
 		Label trainLabel = new Label("Train ID");
 		trainLabel.setAlignment(Label.CENTER);
 		trainPanelHeader.add(trainLabel);
 
 		Label routeLabel = new Label("Route Blocks");
 		routeLabel.setAlignment(Label.CENTER);
 		trainPanelHeader.add(routeLabel);
 
 		Label authLabel = new Label("Authority");
 		authLabel.setAlignment(Label.CENTER);
 		trainPanelHeader.add(authLabel);
 
 		Label speedLabel = new Label("Speed Limit");
 		speedLabel.setAlignment(Label.CENTER);
 		trainPanelHeader.add(speedLabel);
 
 		scrollPaneTrain = new JScrollPane();
 		trainPanel.add(scrollPaneTrain);
 
 		trainPanelContent = new JPanel();
 		trainPanelContent.setBorder(new CompoundBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)), new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0))));
 		scrollPaneTrain.setViewportView(trainPanelContent);
 		trainPanelContent.setLayout(new BoxLayout(trainPanelContent, BoxLayout.Y_AXIS));
 		blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.Y_AXIS));
 
 		scrollPaneBlock = new JScrollPane();
 		blockPanel.add(scrollPaneBlock);
 
 		blockPanelContent = new JPanel();
 		blockPanelContent.setBorder(new CompoundBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)), new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0))));
 		scrollPaneBlock.setViewportView(blockPanelContent);
		blockPanelContent.setLayout(new BoxLayout(blockPanelContent, BoxLayout.X_AXIS));
 
 		JLabel lblWayside = new JLabel("Wayside");
 
 		JLabel lblTrains = new JLabel("Trains");
 
 		JLabel lblBlocks = new JLabel("Blocks");
 
 		waysideLabel = new JLabel("0");
 		waysideLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
 		trainTotalLabel = new JLabel("0");
 		trainTotalLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
 		blockTotalLabel = new JLabel("0");
 		blockTotalLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		GroupLayout gl_selectedItemPanel = new GroupLayout(selectedItemPanel);
 		gl_selectedItemPanel.setHorizontalGroup(
 				gl_selectedItemPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_selectedItemPanel.createSequentialGroup()
 						.addGap(32)
 						.addGroup(gl_selectedItemPanel.createParallelGroup(Alignment.TRAILING, false)
 								.addComponent(waysideLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 								.addComponent(lblWayside, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 								.addGap(150)
 								.addGroup(gl_selectedItemPanel.createParallelGroup(Alignment.LEADING)
 										.addComponent(lblTrains, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 										.addGroup(gl_selectedItemPanel.createSequentialGroup()
 												.addGap(10)
 												.addComponent(trainTotalLabel)))
 												.addPreferredGap(ComponentPlacement.RELATED)
 												.addGroup(gl_selectedItemPanel.createParallelGroup(Alignment.LEADING, false)
 														.addComponent(blockTotalLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 														.addComponent(lblBlocks, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 														.addGap(55))
 				);
 		gl_selectedItemPanel.setVerticalGroup(
 				gl_selectedItemPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_selectedItemPanel.createSequentialGroup()
 						.addGroup(gl_selectedItemPanel.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblWayside)
 								.addComponent(lblBlocks)
 								.addComponent(lblTrains))
 								.addPreferredGap(ComponentPlacement.UNRELATED)
 								.addGroup(gl_selectedItemPanel.createParallelGroup(Alignment.BASELINE)
 										.addComponent(waysideLabel)
 										.addComponent(blockTotalLabel)
 										.addComponent(trainTotalLabel))
 										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 		selectedItemPanel.setLayout(gl_selectedItemPanel);
 		contentPane.setLayout(gl_contentPane);
 		
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public class ControllerListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			try {
 				if (event.getActionCommand() == "Refresh") {
 					Integer val = (Integer) spinnerController.getValue();
 					waysideLabel.setText(val.toString());
 					trainPanelContent.removeAll();
   					blockPanelContent.removeAll();
 					
 					try {
 						String str = (String) spinnerTrack.getValue();
 						if (str.equals("Red Line")) {
 							currentController = tkc.redControllerList.get(val);
 						}
 						else {
 							currentController = tkc.greenControllerList.get(val);
 						}
 						trainTotalLabel.setText("" + currentController.trainList.size());
 						blockTotalLabel.setText("" + currentController.blockTable.size());
 						
 						addToTrainPanel();
 						addToBlockPanel();
 						
 					} catch (NullPointerException nuE) {
 						JOptionPane.showMessageDialog(null, "No Controller found!!", "Error", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace(System.err);
 				JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
 			}
 
 		}
 		
 		public void addToTrainPanel() {
 			
 			
 			for (TrainWrapper tTrain : currentController.trainList) {
 				JPanel newEntry = new JPanel();
 				trainPanelContent.add(newEntry);
 				newEntry.setLayout(new BoxLayout(newEntry, BoxLayout.X_AXIS));
 	
 				Label trainLabel = new Label("" + tTrain.train.id);
 				trainLabel.setAlignment(Label.CENTER);
 				newEntry.add(trainLabel);
 				
 				String route;
 				
 				if (tTrain.getFutureBlock() != null) {
 					route = "" + tTrain.getBlockLocation().id + " -> " + tTrain.getFutureBlock().id;
 				} else {
 					if (tTrain.getBlockLocation() != null) {
 						route = "" + tTrain.getBlockLocation().id;
 					} else {
 						route = " ";
 					}
 				}
 				
 				Label routeLabel = new Label(route);
 				routeLabel.setAlignment(Label.CENTER);
 				newEntry.add(routeLabel);
 	
 				Label authLabel = new Label("" + tTrain.getBlockLocation().fbAuthority);
 				authLabel.setAlignment(Label.CENTER);
 				newEntry.add(authLabel);
 	
 				Label speedLabel = new Label("" + tTrain.getBlockLocation().fbSpeed);
 				speedLabel.setAlignment(Label.CENTER);
 				newEntry.add(speedLabel);
 			}
 			trainPanelContent.revalidate();
 			trainPanelContent.repaint();
 		}
 		
 		public void addToBlockPanel() {
 			Iterator<Block> itr = currentController.blockTable.values().iterator();
 			Block temp;
 			while (itr.hasNext()) {
 				temp = itr.next();
 				
 				JPanel newEntry = new JPanel();
 				
 				newEntry.setLayout(new BoxLayout(newEntry, BoxLayout.X_AXIS));
 
 				Label blkIDLabel = new Label("" + temp.id);
 				blkIDLabel.setAlignment(Label.CENTER);
 				newEntry.add(blkIDLabel);
 				
 				String state;
 				if (temp.trackCircuitFailure)
 					state = "DAMAGED";
 				else
 					state = "HEALTHY";
 				
 				Label blkStateLabel = new Label(state);
 				blkStateLabel.setAlignment(Label.CENTER);
 				newEntry.add(blkStateLabel);
 
 				Label switchLabel = new Label("N/A");
 				switchLabel.setAlignment(Label.CENTER);
 				newEntry.add(switchLabel);
 				
 				blockPanelContent.add(newEntry);
 			}
 			JPanel newEntry = new JPanel();
 			newEntry.setLayout(new BoxLayout(newEntry, BoxLayout.X_AXIS));
 
 			String direction;
 			
 			Label blkIDLabel = new Label("" + currentController.centralSwitch.id);
 			blkIDLabel.setAlignment(Label.CENTER);
 			newEntry.add(blkIDLabel);
 
 			Label blkStateLabel = new Label("N/A");
 			blkStateLabel.setAlignment(Label.CENTER);
 			newEntry.add(blkStateLabel);
 			
 			if (currentController.centralSwitch.state == Switch.STATE_STRAIGHT)
 				direction = "STRAIGHT";
 			else
 				direction = "DIVERGENT";
 			
 			Label switchLabel = new Label(direction);
 			switchLabel.setAlignment(Label.CENTER);
 			newEntry.add(switchLabel);
 			blockPanelContent.add(newEntry);
 			
 			blockPanelContent.revalidate();
 			blockPanelContent.repaint();
 		}
 
 	}
 }
