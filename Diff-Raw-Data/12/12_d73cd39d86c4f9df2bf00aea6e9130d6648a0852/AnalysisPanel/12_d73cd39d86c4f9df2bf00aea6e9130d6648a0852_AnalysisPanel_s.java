 package dk.dma.aiscoverage.gui;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.border.TitledBorder;
 import javax.swing.JScrollPane;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 import com.bbn.openmap.gui.OMComponentPanel;
 
 import dk.dma.aiscoverage.calculator.CoverageCalculator;
 import dk.dma.aiscoverage.data.BaseStation;
 import dk.dma.aiscoverage.data.BaseStation.ReceiverType;
 import dk.dma.aiscoverage.data.BaseStationHandler;
 import dk.dma.aiscoverage.event.AisEvent;
 import dk.dma.aiscoverage.event.IProjectHandlerListener;
 import dk.dma.aiscoverage.openmap.layers.CoverageLayer;
 import dk.dma.aiscoverage.openmap.layers.DensityPlotLayer;
 import dk.dma.aiscoverage.project.ProjectHandler;
 
 import java.awt.Component;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.FlowLayout;
 
 import javax.swing.SwingConstants;
 import javax.swing.JSeparator;
 import javax.swing.border.MatteBorder;
 import java.awt.Color;
 import javax.swing.UIManager;
 import javax.swing.JRadioButton;
 
 
 public class AnalysisPanel extends OMComponentPanel implements ActionListener, IProjectHandlerListener {
 
 	private static final long serialVersionUID = -5409591947155863462L;
 	private JButton btnStartAnalysis;
 	private JButton btnStopAnalysis;
 	private JLabel totalMessages;
 	private JLabel messagesPerSec;
 	private CoverageLayer coverageLayer;
 	private HashMap<String, JCheckBox> bsmmsis = new HashMap<String, JCheckBox>();
 	private JPanel baseStationPanel;
 	private JScrollPane scrollPane;
 	private JPanel baseStationWrapperPanel;
 	private JCheckBox chckbxSelectAll;
 	private JPanel selectAllPanel;
 	private JSeparator separator;
 	private JPanel bottomPanel;
 	private JPanel projectPanel;
 	private JLabel lblDuration;
 	private JLabel lblDensityCS;
 	private JLabel lblCellSize;
 	private JLabel lblInput;
 	private JLabel coverageCellSize;
 	private JLabel lblFile;
 	private JLabel densityCellSize;
 	private JLabel lblh;
 	private boolean mouseDown = false;
     private JLabel lblRunningTime;
     private JLabel runningTime;
     private JPanel panel;
     private JRadioButton coverageRadio;
     private JRadioButton densityPlotRadio;
     private DensityPlotLayer densityPlotLayer;
 
 	
 	/**
 	 * Create the panel.
 	 */
 	public AnalysisPanel() {
 		setBorder(null);
 		setLayout(new BorderLayout(0, 0));
 		
 		panel = new JPanel();
 		panel.setBorder(new TitledBorder(null, "View", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		add(panel, BorderLayout.NORTH);
 		
 		coverageRadio = new JRadioButton("Coverage");
 		coverageRadio.setSelected(true);
 		
 		densityPlotRadio = new JRadioButton("Density plot");
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(coverageRadio)
 					.addGap(18)
 					.addComponent(densityPlotRadio)
 					.addContainerGap(12, Short.MAX_VALUE))
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(coverageRadio)
 						.addComponent(densityPlotRadio))
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		panel.setLayout(gl_panel);
 		
 		baseStationWrapperPanel = new JPanel();
 		baseStationWrapperPanel.setBorder(new TitledBorder(null, "Base Stations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		add(baseStationWrapperPanel, BorderLayout.CENTER);
 		
 		scrollPane = new JScrollPane();
 		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
 		baseStationWrapperPanel.setLayout(new BorderLayout(0, 0));
 		
 		selectAllPanel = new JPanel();
		selectAllPanel.setBorder(new MatteBorder(0, 0, 1, 0, (Color) UIManager.getColor("Button.shadow")));
 		FlowLayout flowLayout = (FlowLayout) selectAllPanel.getLayout();
 		flowLayout.setAlignment(FlowLayout.LEFT);
 		baseStationWrapperPanel.add(selectAllPanel, BorderLayout.NORTH);
 		
 		chckbxSelectAll = new JCheckBox("Select all");
 		chckbxSelectAll.setSelected(true);
 		selectAllPanel.add(chckbxSelectAll);
 		
 		separator = new JSeparator();
 		selectAllPanel.add(separator);
 		
 		baseStationWrapperPanel.add(scrollPane, BorderLayout.CENTER);
 		
 		baseStationPanel = new JPanel();
 		scrollPane.setViewportView(baseStationPanel);
 		baseStationPanel.setBorder(null);
 		GridBagLayout gbl_baseStationPanel = new GridBagLayout();
 		gbl_baseStationPanel.columnWidths = new int[]{0, 0};
 		gbl_baseStationPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
 		gbl_baseStationPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
 		gbl_baseStationPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		baseStationPanel.setLayout(gbl_baseStationPanel);
 		
 		
 		bottomPanel = new JPanel();
 		add(bottomPanel, BorderLayout.SOUTH);
 		bottomPanel.setLayout(new BorderLayout(0, 0));
 		
 		projectPanel = new JPanel();
 		projectPanel.setSize(projectPanel.getWidth(), 50);
 		projectPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Project settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
 		
 		lblDuration = new JLabel("Duration");
 		
 		lblDensityCS = new JLabel("Coverage cell size");
 		
 		lblCellSize = new JLabel("Density cell size");
 		
 		lblInput = new JLabel("Input");
 		
 		coverageCellSize = new JLabel("-");
 		
 		lblFile = new JLabel("-");
 		
 		densityCellSize = new JLabel("-");
 		
 		lblh = new JLabel("-");
 		GroupLayout gl_projectPanel = new GroupLayout(projectPanel);
 		gl_projectPanel.setHorizontalGroup(
 			gl_projectPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_projectPanel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_projectPanel.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblDensityCS)
 						.addComponent(lblInput)
 						.addComponent(lblCellSize)
 						.addComponent(lblDuration))
 					.addGap(18)
 					.addGroup(gl_projectPanel.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblh)
 						.addComponent(densityCellSize)
 						.addComponent(lblFile)
 						.addComponent(coverageCellSize))
 					.addContainerGap(96, Short.MAX_VALUE))
 		);
 		gl_projectPanel.setVerticalGroup(
 			gl_projectPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_projectPanel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_projectPanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblInput)
 						.addComponent(lblFile))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_projectPanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblDensityCS)
 						.addComponent(coverageCellSize))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_projectPanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblCellSize)
 						.addComponent(densityCellSize))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_projectPanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblDuration)
 						.addComponent(lblh))
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		projectPanel.setLayout(gl_projectPanel);
 		bottomPanel.add(projectPanel, BorderLayout.NORTH);
 		
 		JPanel progressPanel = new JPanel();
 		progressPanel.setBorder(new TitledBorder(null, "Progress", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		
 		JLabel lblNewLabel = new JLabel("Total messages");
 		
 		btnStartAnalysis = new JButton("Start analysis");
 		
 		btnStopAnalysis = new JButton("Stop Analysis");
 		
 		JLabel lblNewLabel_1 = new JLabel("Messages/Sec");
 		
 		totalMessages = new JLabel();
 		
 		messagesPerSec = new JLabel();
 		
 		lblRunningTime = new JLabel("Running time");
 		
 		runningTime = new JLabel("");
 		
 		GroupLayout gl_progressPanel = new GroupLayout(progressPanel);
 		gl_progressPanel.setHorizontalGroup(
 			gl_progressPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_progressPanel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_progressPanel.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_progressPanel.createSequentialGroup()
 							.addComponent(btnStartAnalysis)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(btnStopAnalysis))
 						.addGroup(gl_progressPanel.createSequentialGroup()
 							.addGroup(gl_progressPanel.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblNewLabel)
 								.addComponent(lblNewLabel_1)
 								.addComponent(lblRunningTime))
 							.addGap(18)
 							.addGroup(gl_progressPanel.createParallelGroup(Alignment.LEADING)
 								.addComponent(runningTime)
 								.addComponent(totalMessages)
 								.addComponent(messagesPerSec))))
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		gl_progressPanel.setVerticalGroup(
 			gl_progressPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_progressPanel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_progressPanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblRunningTime)
 						.addComponent(runningTime))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_progressPanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblNewLabel)
 						.addComponent(totalMessages))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_progressPanel.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_progressPanel.createSequentialGroup()
 							.addGroup(gl_progressPanel.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblNewLabel_1)
 								.addComponent(messagesPerSec))
 							.addGap(29))
 						.addGroup(gl_progressPanel.createSequentialGroup()
 							.addPreferredGap(ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
 							.addGroup(gl_progressPanel.createParallelGroup(Alignment.BASELINE)
 								.addComponent(btnStopAnalysis)
 								.addComponent(btnStartAnalysis))
 							.addContainerGap())))
 		);
 		progressPanel.setLayout(gl_progressPanel);
 		bottomPanel.add(progressPanel, BorderLayout.SOUTH);
 		
 		//add listeners
 		btnStartAnalysis.addActionListener(this);
 		btnStopAnalysis.addActionListener(this);
 		chckbxSelectAll.addActionListener(this);
 		densityPlotRadio.addActionListener(this);
 		coverageRadio.addActionListener(this);
 		
 		ButtonGroup viewGroup = new ButtonGroup();
 		viewGroup.add(coverageRadio);
 		viewGroup.add(densityPlotRadio);
 		
 		ProjectHandler.getInstance().addProjectHandlerListener(this);
 //		ProjectHandler.getInstance().loadProject("C:\\Users\\Kasper\\Desktop\\save.ana");
 		
 		updateButtons();
 		
 		//timers
 		startTimers();
 		
 
 	}
 	
 	protected void setAnalysisData(String input, String coverageCellSize, String densityCellSize, String time)
 	{
 		this.coverageCellSize.setText(coverageCellSize+"m");
 		
 		lblFile.setText(input);
 		
 		this.densityCellSize.setText(densityCellSize+"m");
 		
 		lblh.setText(time);
 		
 		projectPanel.setToolTipText(ProjectHandler.getInstance().getProject().getDescription());
 		
 		
 		
 		
 	}
 
 	/*
 	 * Starts timers for updating GUI
 	 */
 	private void startTimers() {
 		//This is only for updating GUI elements
 		//Don't perform lengthy work
 		new Timer(1000, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(!mouseDown){
 					updateProgress();
 				}
 			}
 		}).start();
 		
 	}
 	
 	/*
 	 * Selects or deselects all base station checkboxes
 	 */
 	private void selectAll(boolean bool){
 		Collection<JCheckBox> boxes = this.bsmmsis.values();
 		for (JCheckBox jCheckBox : boxes) {
 			jCheckBox.setSelected(bool);
 		}
 	}
 	
 	/*
 	 * Updates the list of base station
 	 */
 	private void updateBaseStationList(String[] bsmmsis){
 		BaseStationHandler basestationHandler = ProjectHandler.getInstance().getProject().getCoverageCalculator().getBaseStationHandler();
 		Arrays.sort(bsmmsis);
 		Arrays.sort(bsmmsis, new Comparator<String>(){
 
 			@Override
 			public int compare(String o1, String o2) {
 				try{
 					int first = Integer.parseInt(o1);
 					int second = Integer.parseInt(o2);
 					if(first > second)
 			            return 1;
 			        else if(first < second)
 			            return -1;
 			        else
 			            return 0; 
 				}catch(Exception e){
 					
 				}
 				if(o1.compareTo(o2) > 0)
 					return 1;
 				else if(o1.compareTo(o2) < 0)
 					return -1;
 				else
 					return 0;
 			}
 			
 		});
 		
 		baseStationPanel.removeAll();
 		
 		int i = 0;
 		for(String bsmmsi : bsmmsis){
 			JCheckBox checkbox = this.bsmmsis.get(bsmmsi);
 			if(checkbox == null){
 				checkbox = addCheckBox(bsmmsi);
 			}
 
 			checkbox = this.bsmmsis.get(bsmmsi);
 			GridBagConstraints constraints = new GridBagConstraints();
 			constraints.insets = new Insets(5, 25, 0, 0);
 			constraints.anchor = GridBagConstraints.WEST;
 			constraints.gridx = 0;
 			constraints.gridy = i;
 			baseStationPanel.add(checkbox, constraints);
 			
 			
 			JLabel receiverType = new JLabel();
 			receiverType.setEnabled(checkbox.isEnabled());
 			ReceiverType recType = basestationHandler.getGrid(bsmmsi).getReceiverType();
 			if(recType==ReceiverType.BASESTATION)
 				receiverType.setText("Basestation");
 			else if(recType==ReceiverType.REGION)
 				receiverType.setText("Region");
 			else{
 				receiverType.setText("Unknown");
 			}
 			
 			GridBagConstraints receiverTypeConstraints = new GridBagConstraints();
 			receiverTypeConstraints.insets = new Insets(5, 10, 0, 0);
 			receiverTypeConstraints.anchor = GridBagConstraints.WEST;
 			receiverTypeConstraints.gridx = 1;
 			receiverTypeConstraints.gridy = i;
 			baseStationPanel.add(receiverType, receiverTypeConstraints);
 			i++;
 
 		}
 		
 		
 	}
 	
 	private void updateButtons(){
 		//Thread safety
 		Runnable doWorkRunnable = new Runnable() {
 		    public void run() {
 		    	if(ProjectHandler.getInstance().getProject() == null){
 		    		btnStartAnalysis.setEnabled(false);
 					btnStopAnalysis.setEnabled(false);
 		    	}
 		    	else if(ProjectHandler.getInstance().getProject().isRunning()){
 					btnStartAnalysis.setEnabled(false);
 					btnStopAnalysis.setEnabled(true);
 				}else if(ProjectHandler.getInstance().getProject().isDone()){
 					btnStartAnalysis.setEnabled(false);
 					btnStopAnalysis.setEnabled(false);
 				}else{
 					btnStartAnalysis.setEnabled(true);
 					btnStopAnalysis.setEnabled(false);
 				}
 		    }
 		};
 		SwingUtilities.invokeLater(doWorkRunnable);
 	}
 	
 	
 	private String runningTimeToString(Long secondsElapsed){
 		String hoursString= null, minutesString = null, secondsString = null;
 		int hours = (int) (secondsElapsed/3600);
 		int minutes = (int) ((secondsElapsed/60)-(hours*60));
 		int seconds = (int) (secondsElapsed-(minutes*60));
 		if(seconds < 10) secondsString = "0"+seconds;
 		else secondsString = ""+seconds;
 		if(minutes < 10) minutesString = "0"+minutes;
 		else minutesString = ""+minutes;
 		if(hours < 10) hoursString = "0"+hours;
 		else hoursString = ""+hours;
 		return hoursString + ":"+minutesString+":"+secondsString;
 	}
 	
 	private void updateProgress(){
 		if(ProjectHandler.getInstance().getProject() == null){
 //			System.out.println("no project");
 			return;
 		}
 		Long secondsElapsed = ProjectHandler.getInstance().getProject().getRunningTime();
 		if(secondsElapsed > 0){
 			totalMessages.setText(""+ProjectHandler.getInstance().getProject().getMessageCount());
 			messagesPerSec.setText(""+ProjectHandler.getInstance().getProject().getMessageCount()/secondsElapsed);
 			updateBaseStationList(ProjectHandler.getInstance().getProject().getCoverageCalculator().getBaseStationNames());
 			runningTime.setText(runningTimeToString(secondsElapsed));
 		}
 		else{
 			totalMessages.setText("-");
 			messagesPerSec.setText("-");
 			updateBaseStationList(ProjectHandler.getInstance().getProject().getCoverageCalculator().getBaseStationNames());
 			runningTime.setText("-");
 		}
 	}
 	public void startAnalysis(){
 		//ProjectHandler.getInstance().getProject().setFile("C:\\Users\\Kasper\\Desktop\\aisdump.txt");
 		try {
 			ProjectHandler.getInstance().getProject().startAnalysis(); //start thread
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		
 		//start the analysis
 		if (e.getSource() == btnStartAnalysis) {
 			try {
 				ProjectHandler.getInstance().getProject().startAnalysis(); //start thread
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 		} 
 		
 		// Stop the analysis
 		else if(e.getSource() == btnStopAnalysis){
 			ProjectHandler.getInstance().getProject().stopAnalysis();
 		}
 		
 		// Checkbox event, refresh coverageLayer
 		else if(e.getSource().getClass() == JCheckBox.class){
 			if(e.getSource() == chckbxSelectAll) {
 				selectAll(chckbxSelectAll.isSelected());
 				ProjectHandler.getInstance().getProject().getCoverageCalculator().getBaseStationHandler().setAllVisible(chckbxSelectAll.isSelected());
 			}
 			else{
 				JCheckBox checkBox = (JCheckBox) e.getSource();
 				chckbxSelectAll.setSelected(false);
 				ProjectHandler.getInstance().getProject().getCoverageCalculator().getBaseStationHandler().setVisible(checkBox.getText(), checkBox.isSelected());
 			}
 		}
 		
 		// View is changed
 		else if(e.getSource() == coverageRadio) {
 			System.out.println("coverage");
 			coverageLayer.setVisible(true);
 			coverageLayer.updateOnce();
 			densityPlotLayer.setVisible(false);
 			enableBaseStationPanel(true);
 		}
 		else if(e.getSource() == densityPlotRadio) {
 			System.out.println("density");
 			coverageLayer.setVisible(false);
 			densityPlotLayer.setVisible(true);
 			densityPlotLayer.updateOnce();
 			enableBaseStationPanel(false);
 		}
 
 		
 	}
 	private void enableBaseStationPanel(boolean b){
 		Component[] com = this.baseStationPanel.getComponents();  
 		for (int i = 0; i < com.length; i++) {  
 		     com[i].setEnabled(b);
 		}  
 		chckbxSelectAll.setEnabled(b);
 		this.baseStationWrapperPanel.setEnabled(b);
 	}
 	
 	@Override
 	public void findAndInit(Object obj) {
 		if (obj instanceof CoverageLayer) {
 			coverageLayer = (CoverageLayer) obj;
 		}else if (obj instanceof DensityPlotLayer) {
 			densityPlotLayer = (DensityPlotLayer) obj;
 		}
 	}
 
 
 	
 
 	private void resetGui(){
 		this.bsmmsis.clear();
 		updateButtons();
 		this.densityPlotLayer.reset();
 	}
 
 
 
 	@Override
 	public void aisEventReceived(AisEvent event) {
 		if(event.getEvent() == AisEvent.Event.ANALYSIS_STARTED){
 			System.out.println("analysis started");
 			updateButtons();
 			
 		} 
 		else if(event.getEvent() == AisEvent.Event.ANALYSIS_STOPPED){
 			System.out.println("analysis stopped");
 			updateButtons();
 
 		} 
 		else if(event.getEvent() == AisEvent.Event.BS_ADDED){
 			if(event.getSource() instanceof CoverageCalculator){
 				BaseStation basestation = (BaseStation) event.getEventObject();
 				if(basestation == null) return;
 				addCheckBox(basestation.getIdentifier());
 			}
 		} 
 		else if(event.getEvent() == AisEvent.Event.BS_VISIBILITY_CHANGED){
 			if(event.getSource() instanceof CoverageCalculator){
 				BaseStation basestation = (BaseStation) event.getEventObject();
 				if(basestation == null) return;
 				JCheckBox checkbox = this.bsmmsis.get(basestation.getIdentifier());
 				checkbox.setSelected(basestation.isVisible());
 			}
 		} 
 		else if(event.getEvent() == AisEvent.Event.PROJECT_CREATED){
 			resetGui();
 			System.out.println("created");
 
 		} else if(event.getEvent() == AisEvent.Event.PROJECT_LOADED){
 			System.out.println("loaded");
 			resetGui();
 		}
 		
 	}
 	
 	private JCheckBox addCheckBox(String mmsi){
 		BaseStation basestation = ProjectHandler.getInstance().getProject().getCoverageCalculator().getBaseStationHandler().getGrid(mmsi);
 		JCheckBox checkbox = new JCheckBox(mmsi+"");
 		checkbox.setHorizontalAlignment(SwingConstants.LEFT);		
 		this.bsmmsis.put(mmsi, checkbox);	
 		checkbox.addActionListener(this);
 		
 		if(!coverageRadio.isSelected())
 			checkbox.setEnabled(false);
 		
 		basestation.setVisible(chckbxSelectAll.isSelected());
 		checkbox.setSelected(chckbxSelectAll.isSelected());
 			
 		return checkbox;
 	}
 }
