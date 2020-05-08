 package org.computer.knauss.reqtDiscussion.ui;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.computer.knauss.reqtDiscussion.model.FilteredDiscussionEventList;
 import org.computer.knauss.reqtDiscussion.model.IFilteredDiscussionEventList;
 import org.computer.knauss.reqtDiscussion.model.partition.IDiscussionOverTimePartition;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.AddFilterCommand;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.RemoveFilterCommand;
 
 public class VisualizationConfigurationPanel extends JPanel implements
 		ItemListener, ChangeListener, ActionListener {
 
 	private static final long serialVersionUID = 1L;
 	private final static String[] PARTITION_TYPES = {
 			"Fixed Number of Partitions", "No Partitioning", "Days", "Weeks",
 			"Month", "Years" };
 
 	private List<ActionListener> listeners = new ArrayList<ActionListener>(1);
 	private IFilteredDiscussionEventList filteredCommentList;
 	private IDiscussionOverTimePartition partition;
 	private JComboBox partitionTypeChooser;
 	private JSlider partitionCountSlider;
 	private JCheckBox commentStyleBoxes;
 	private JCheckBox commentStyleText;
 	private JSpinner partitionCountSpinner;
 	private JCheckBox paintBackGround;
 	private JCheckBox paintGrid;
 	private JCheckBox paintLineOfUnderstanding;
 	private JCheckBox lsuInterpolarization;
 	private JCheckBox gridPartition;
 	private JCheckBox showPattern;
	private JCheckBox incidents;
 
 	public VisualizationConfigurationPanel() {
 		this.setLayout(new GridLayout(5, 1));
 
 		// 1. adjust partition type
 		this.partitionTypeChooser = new JComboBox(PARTITION_TYPES);
 		this.partitionTypeChooser.addItemListener(this);
 		add(this.partitionTypeChooser);
 
 		// and if partition type 0 or -1:
 		// 1.a adjust partition count
 		JPanel partitionCountPanel = new JPanel(new GridLayout(2, 1));
 		partitionCountPanel.setBorder(BorderFactory
 				.createTitledBorder("Partition Count"));
 		this.partitionCountSlider = new JSlider(JSlider.HORIZONTAL, 1, 600, 8);
 		this.partitionCountSlider.setMajorTickSpacing(10);
 		this.partitionCountSlider.setMinorTickSpacing(2);
 		this.partitionCountSlider.addChangeListener(this);
 		partitionCountPanel.add(this.partitionCountSlider);
 
 		this.partitionCountSpinner = new JSpinner(new SpinnerNumberModel(8, 1,
 				600, 1));
 		this.partitionCountSpinner.addChangeListener(this);
 		partitionCountPanel.add(this.partitionCountSpinner);
 		add(partitionCountPanel);
 
 		// 1.b activate styles (we can have both)
 		JPanel chooseStylePanel = new JPanel(new GridLayout(3, 2));
 		chooseStylePanel.setBorder(BorderFactory
 				.createTitledBorder("Choose Style"));
 		this.commentStyleBoxes = new JCheckBox("Rectangles");
 		this.commentStyleBoxes.setSelected(true);
 		this.commentStyleBoxes.addActionListener(this);
 		chooseStylePanel.add(this.commentStyleBoxes);
 
 		this.paintBackGround = new JCheckBox("Background");
 		this.paintBackGround.addActionListener(this);
 		chooseStylePanel.add(this.paintBackGround);
 
 		this.paintGrid = new JCheckBox("Grid");
 		this.paintGrid.addActionListener(this);
 		chooseStylePanel.add(this.paintGrid);
 
 		this.paintLineOfUnderstanding = new JCheckBox("SLU Line");
 		this.paintLineOfUnderstanding.addActionListener(this);
 		chooseStylePanel.add(this.paintLineOfUnderstanding);
 
 		this.commentStyleText = new JCheckBox("Lines and Text");
 		this.commentStyleText.addActionListener(this);
 		chooseStylePanel.add(this.commentStyleText);
 
 		this.showPattern = new JCheckBox("Pattern");
 		this.showPattern.addActionListener(this);
 		chooseStylePanel.add(this.showPattern);
 		add(chooseStylePanel);
 
 		JPanel filterPanel = new JPanel(new GridLayout(3, 2));
 		filterPanel.setBorder(BorderFactory
 				.createTitledBorder("Filter comments"));
 		filterPanel.add(new JPanel());
 		filterPanel.add(new JPanel());
 		filterPanel.add(new JButton(new AddFilterCommand(
 				getFilteredCommentList())));
 		filterPanel.add(new JButton(new RemoveFilterCommand(
 				getFilteredCommentList())));
 		add(filterPanel);
 
 		JPanel miscPanel = new JPanel(new GridLayout(3, 1));
 		add(miscPanel);
 		miscPanel.setBorder(BorderFactory.createTitledBorder("Misc"));
 		this.lsuInterpolarization = new JCheckBox("SLU Interpolarization");
 		this.lsuInterpolarization.addActionListener(this);
 		miscPanel.add(this.lsuInterpolarization);
 		this.gridPartition = new JCheckBox("Use Partitions for Grid");
 		this.gridPartition.addActionListener(this);
 		miscPanel.add(this.gridPartition);
 
		this.incidents = new JCheckBox("Show relevant incidents");
		this.incidents.addActionListener(this);
		miscPanel.add(this.incidents);
 	}
 
 	public void setDiscussionPartition(IDiscussionOverTimePartition partition) {
 		this.partition = partition;
 		this.partition.setPartitionCount(this.partitionCountSlider.getValue());
 		this.partition.setPartitionType(this.partitionTypeChooser
 				.getSelectedIndex() * -1);
 	}
 
 	public IDiscussionOverTimePartition getDiscussionPartition() {
 		return this.partition;
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent event) {
 		int partitionType = this.partitionTypeChooser.getSelectedIndex() * -1;
 		this.partition.setPartitionType(partitionType);
 		// activate/deactivate partitionCount Controls
 		this.partitionCountSlider.setEnabled(partitionType >= -1);
 		this.partitionCountSpinner.setEnabled(partitionType >= -1);
 		this.commentStyleBoxes.setEnabled(partitionType >= -1);
 		this.commentStyleText.setEnabled(partitionType >= -1);
 
 		if (partitionType == IDiscussionOverTimePartition.TYPE_PIXEL) {
 			this.partitionCountSlider.setValue(600);
 			this.partitionCountSpinner.setValue(600);
 			this.partition.setPartitionCount(600);
 		}
 		fireConfigurationChanged();
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent event) {
 		if (event.getSource().equals(this.partitionCountSlider)) {
 			Integer value = (Integer) this.partitionCountSlider.getValue();
 			this.partitionCountSpinner.setValue(value);
 			this.partition.setPartitionCount(value);
 		} else {
 			Integer value = (Integer) this.partitionCountSpinner.getValue();
 			this.partitionCountSlider.setValue(value);
 			this.partition.setPartitionCount(value);
 		}
 		fireConfigurationChanged();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		fireConfigurationChanged();
 	}
 
 	public void addActionListener(ActionListener l) {
 		this.listeners.add(l);
 	}
 
 	private void fireConfigurationChanged() {
 		for (ActionListener l : this.listeners) {
 			l.actionPerformed(null);
 		}
 	}
 
 	public boolean isBoxesStyle() {
 		return this.commentStyleBoxes.isSelected();
 	}
 
 	public boolean isTextStyle() {
 		return this.commentStyleText.isSelected();
 	}
 
 	public IFilteredDiscussionEventList getFilteredCommentList() {
 		if (this.filteredCommentList == null)
 			this.filteredCommentList = new FilteredDiscussionEventList();
 		return this.filteredCommentList;
 	}
 
 	public boolean isBackgroundStyle() {
 		return this.paintBackGround.isSelected();
 	}
 
 	public boolean isGridStyle() {
 		return this.paintGrid.isSelected();
 	}
 
 	public boolean isLineOfUnderstandingStyle() {
 		return this.paintLineOfUnderstanding.isSelected();
 	}
 
 	public boolean isLSUInterpolarization() {
 		return this.lsuInterpolarization.isSelected();
 	}
 
 	public boolean isUsePartitionForGrid() {
 		return this.gridPartition.isSelected();
 	}
 
 	public boolean isPatternStyle() {
 		return this.showPattern.isSelected();
 	}

	public boolean isIncidentStyle() {
		return this.incidents.isSelected();
	}
 }
